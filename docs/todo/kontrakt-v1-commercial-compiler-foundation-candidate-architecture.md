# Kontrakt V1 — Candidate SOTA Commercial Compiler Foundation

## Status

Candidate architecture checklist for review.

This document is **not an ADR** and does not accept every item below. It is intentionally broader than the immediate V1
implementation plan so that the final V1 architecture can be reviewed against modern production compiler practice before
the structure becomes difficult to change.

The main requirement is stronger than ordinary compiler modularity:

> Determinism and Contract Authority must survive every compiler phase.
> Source syntax, compiler scheduling, caches, JVM layout, generated artifacts, and optimization machinery must never
> become contract meaning.

### Compiler-internal constitutional rule

Kontrakt compiler internals must follow the same separation used by the contract model itself.

- **P0 — Explicit subsystem authority:** every compiler subsystem owns a narrow, declared semantic responsibility.
- **P0 — Mechanism is replaceable:** threads, queues, caches, storage layout, host objects, libraries, and emission
  mechanisms cannot define compiler meaning.
- **P0 — Deterministic phase law:** the same explicit compiler inputs must establish the same semantic outputs
  regardless of scheduling, allocation, cache state, or discovery order.
- **P0 — Explicit phase boundaries:** data crossing a compiler phase is validated material, not an implicit
  shared-object graph.
- **P0 — Optimization cannot weaken authority:** a transform may change realization shape only when contract-observable
  meaning is preserved.
- **P0 — Cache and artifact non-authority:** persisted material is reusable evidence or realization state; it never
  becomes semantic truth merely because it was stored.

### Obligation, evidence, and realization law

Every compiler-internal design must be described in three separate layers:

```text
explicit obligation
    -> required evidence / verification
    -> replaceable realization
```

- **P0 — Obligation first:** define what the compiler must preserve, establish, reject, bound, or publish before
  choosing a mechanism.
- **P0 — Evidence is not authority:** a verifier, translation validator, differential test, solver, golden vector, or
  proof object establishes confidence or correctness evidence; it does not become semantic authority.
- **P0 — Realization is replaceable:** dense tables, primitive slabs, persistent structures, MVCC-like storage,
  copy-on-write, arenas, locks, CAS, Class-File APIs, or other mechanisms may implement an obligation but must not
  define it.
- **P0 — Realistic guarantee:** Kontrakt must state only guarantees it can actually establish within its owned boundary.
  Unsupported environmental or backend guarantees fail closed rather than becoming aspirational semantics.
- **P0 — Determinism belongs to meaning:** deterministic semantic output is required; a particular thread schedule,
  memory layout, merge algorithm, or storage mechanism is not.

This rule applies to the compiler itself exactly as Contract Authority separation applies to compiled machines.

---

## 1. Current Kontrakt Baseline

Kontrakt already has or is actively establishing several foundations that many compilers add much later.

### Contract and semantic foundations

- Contract Authority is separated from implementation and backend shape.
- `.kontrakt` is the IDL-first Interface contract frontend.
- Operation roles are selected by explicit contract slots rather than host-language shape guessing.
- Contract, implementation, and state-machine axes are separated.
- Contract material is refined and lowered before backend realization.
- Host classes, reflection handles, KSP symbols, annotations, and runtime objects do not own semantic identity.
- V1 optimization is restricted to machinery Kontrakt owns rather than arbitrary user implementation code.

### Deterministic acquisition and canonical material

- Backend-neutral acquisition and lowering boundary.
- Explicit frozen acquisition lifecycle.
- Explicit readiness, restart, stale-generation defense, and publication authority.
- Completion order is not semantic order.
- Frozen publication rejects incomplete or backend-contaminated material.
- Planning consumes backend-erased frozen material.
- Dense ordinal/table-oriented planning is the target shape.
- Canonical bytes are distinct from display/debug/source representations.
- Stable identity is moving toward BLAKE3-based HID plus exact collision verification.
- Protocol-owned interning is separated from JVM object interning.
- Deterministic stable intern-ID assignment is independent of insertion order and physical table placement.
- Contract graph canonicalization and deterministic SCC sealing have reserved integration points.

### Planning, resource, and cache foundations

- Planning and frozen acquisition are separate passes.
- Planning is intended to operate on dense frozen tables rather than backend objects.
- Physical and semantic work are separately bounded.
- Run-scoped remaining execution budget is separated from immutable policy configuration.
- Runtime policy epochs are pinned rather than rediscovered during admitted work.
- Planner/runtime storage and join governance already have explicit bounded-lifecycle concepts.
- Caching is treated as a deterministic compiler concern rather than ordinary object memoization.
- Budget and Capacity are explicit contract dimensions rather than hidden runtime tuning values.

### Verification and product foundations

- Generated verification and generated enforcement are first-class V1 targets.
- Contract-derived fixtures and property-based tests are V1 product capabilities.
- Failure attribution and Diagnostic Evidence are explicit semantic concepts.
- Release planning already includes deterministic publication, golden vectors, memory/resource bounds, and isolation
  concerns.

These foundations should be retained. The remaining work is to place them inside a complete commercial compiler
architecture rather than replacing them.

---

# 2. Target V1 Compiler Shape

A mature V1 should approximately have the following structural boundaries even when some advanced engines are still
minimal implementations.

```text
                        Kontrakt Source World
                               |
                 +-------------+-------------+
                 |                           |
          .kontrakt Frontend          Host Evidence Frontends
                 |                    Kotlin/Java/KSP/etc.
                 +-------------+-------------+
                               |
                    Source / Syntax Representation
                               |
                    Resolution + Semantic Analysis
                               |
                      Semantic Contract IR
                               |
                   Canonical Contract Material
                               |
                     Frozen Contract Image
                               |
                 Contract / World Linking Layer
                               |
                    Lowered Contract-Machine IR
                               |
             +-----------------+-----------------+
             |                                   |
       Verification Projection             Realization Planning
             |                                   |
    PBT / Fixtures / Gates / Oracle        Target Capability Check
                                                 |
                                         JVM Realization Lowering
                                                 |
                                             JVM IR
                                                 |
                                      JVM Optimization Pipeline
                                                 |
                              +------------------+------------------+
                              |                                     |
                        Source Emitter                       Classfile Emitter
                              |                                     |
                         javac/kotlinc                    OpenJDK Class-File API
                              |                                     |
                              +------------------+------------------+
                                                 |
                                           JVM Artifacts
```

Cross-cutting infrastructure:

```text
SourceManager / Provenance
Diagnostics / Remarks / Explain
Stable Identity / Interning
Query / Dependency Graph
Incremental Fingerprints
Pass / Analysis Manager
IR Verification
Rewrite / Conversion Framework
Deterministic Parallel Work Graph
Compiler Resource Budgets
Artifact / Cache Store
Reproducibility Manifest
Testing / Fuzzing / Translation Validation
Tracing / Metrics / Crash Reproducer
Compatibility / Schema Evolution
Compiler Driver / Session
```

---

# 3. Priority Labels

- **P0 — V1 structural requirement:** the architecture should contain this boundary in V1.
- **P1 — V1 minimal implementation:** the structure and a useful minimal implementation should exist in V1.
- **P2 — V1 hook:** reserve a clean extension point, but full functionality can wait.
- **POST — Post-V1:** do not expand V1 implementation scope unless later evidence requires it.

---

# 4. Frontend and Language Infrastructure

## 4.1 Source and syntax

- **P0 — SourceManager:** one compiler-owned source registry with stable file identity.
- **P0 — SourceSpan:** byte-accurate source ranges independent of line rendering.
- **P0 — Origin chain:** preserve where semantic material came from after refinement and lowering.
- **P0 — Lexer/parser separation:** syntax recognition must not perform semantic authority decisions.
- **P0 — Syntax representation:** CST/AST or equivalent compiler-owned syntax model.
- **P1 — Error recovery:** malformed input should produce useful diagnostics without uncontrolled cascades.
- **P1 — Deterministic parser:** locale, hash iteration, file enumeration, and thread scheduling must not change parse
  results.
- **P1 — Syntax diagnostics:** keep syntax failures separate from semantic contract failures.
- **P1 — Stable pretty printer:** deterministic debug/source-normalization output for tests and reproductions.
- **P2 — Lossless syntax tree:** useful for future formatter, refactoring, and IDE operations.
- **P2 — Incremental parsing boundary:** design syntax ownership so unchanged regions can later be reused.
- **P2 — Formatter boundary:** allow a future canonical `.kontrakt` formatter without making formatting semantic.

## 4.2 Language evolution

- **P0 — Language version:** every source unit must be parsed under an explicit language version.
- **P0 — Feature gates:** experimental syntax/semantics must be explicitly enabled.
- **P0 — No hidden defaults:** language-version-dependent defaults must not silently change contract meaning.
- **P1 — Deprecation pipeline:** diagnostics for syntax or semantic forms scheduled for removal.
- **P1 — Compatibility test matrix:** old valid source should have defined behavior under supported compiler versions.
- **P2 — Edition-style migration hooks:** reserve explicit migrations if language evolution later requires larger
  semantic changes.

## 4.3 Names, modules, and symbols

- **P0 — Compiler symbol model:** ContractSymbol must be distinct from JVM/Kotlin/Java symbols.
- **P0 — Scope model:** explicit namespaces and lookup boundaries.
- **P0 — Name resolution phase:** source references become exact compiler-owned references before authority begins.
- **P0 — Import/module graph:** deterministic dependency discovery and cycle handling.
- **P0 — Visibility/export model:** separate public contract surface from compiler-private material.
- **P1 — Unresolved-symbol recovery:** continue diagnostics without creating placeholder authority.
- **P1 — Duplicate-definition diagnostics:** deterministic ownership and source attribution.
- **P1 — Module identity:** stable identity independent of filesystem traversal order.
- **P2 — Cross-module summary format:** reserve compact semantic summaries for future incremental compilation.

## 4.4 Host evidence frontends

- **P0 — Adapter boundary:** reflection, KSP, compiler-static indexes, and future host frontends terminate before
  semantic authority.
- **P0 — Frontend capability declaration:** every evidence frontend states exactly what it can prove/read.
- **P0 — Backend-handle erasure:** no host handle survives canonical/frozen publication.
- **P1 — Equivalent-frontend tests:** different evidence backends producing the same contract facts must converge to the
  same canonical meaning.
- **P2 — KSP V2 migration seam:** do not let current reflection/KSP layout become semantic IR shape.

---

# 5. Multi-Level IR Architecture

A single universal IR should be avoided. Modern production compilers preserve abstraction and lower progressively.

## 5.1 Recommended levels

- **P0 — Syntax IR:** source-facing structure and source provenance.
- **P0 — Semantic Contract IR:** resolved contract meaning; target-independent.
- **P0 — Canonical Contract Material:** identity-bearing, deterministic semantic material.
- **P0 — Frozen Contract Image:** immutable, backend-erased, publication-safe representation.
- **P0 — Linked Contract/World IR:** resolved cross-contract and cross-module/world bindings.
- **P0 — Lowered Contract-Machine IR:** executable semantic obligations without JVM concepts.
- **P0 — JVM Realization IR:** target-specific physical execution plan.
- **P2 — Backend-neutral implementation IR:** only if later versions begin optimizing user realization bodies.
- **POST — Native machine IR:** only if Kontrakt gains a non-JVM native backend.

## 5.2 IR laws

- **P0 — Explicit ownership:** every IR level owns a defined abstraction and nothing below it.
- **P0 — No upward leakage:** JVM operations cannot appear in semantic IR.
- **P0 — No source authority after refinement:** source AST/PSI/KSP forms cannot survive as semantic truth.
- **P0 — Deterministic iteration:** semantically visible iteration order is canonical.
- **P0 — Stable IDs:** internal identity must not depend on allocation address or insertion order.
- **P0 — Source provenance:** lowering must preserve diagnostic origin without preserving source authority.
- **P0 — Closed operation vocabulary per IR:** unknown operations fail rather than becoming generic callbacks.
- **P0 — IR verifier:** every level has structural and semantic invariants.
- **P1 — Illegal-operation tracking:** each lowering target declares what operations are still legal.
- **P1 — Explicit conversion boundary:** a lowering is complete only when forbidden higher-level operations are gone.
- **P1 — Canonical textual form:** deterministic debug/test representation.
- **P1 — Round-trip parser/printer for critical IR:** useful for isolated pass tests.
- **P1 — Binary serialization:** persistent IR/cache format must be versioned and bounded.
- **P1 — Schema version:** persistent IR is never assumed compatible by accident.
- **P1 — Upgrade reader:** old cache/IR schema either upgrades explicitly or is rejected.
- **P2 — IR snapshots:** snapshot before/after selected transformations for diagnostics and debugging.
- **P2 — Semantic equivalence metadata:** reserve a way to state what meaning a transform claims to preserve.

- **P0 — Semantic identity / physical address separation:** stable Contract or IR identity must remain distinct from
  local table position, local ordinal, arena offset, JVM object reference, and memory address.
- **P0 — Dense-addressability capability:** published hot-path IR must permit deterministic bounded addressing without
  requiring its semantic model to be expressed as object-pointer graphs.
- **P1 — Dense published substrate:** hot published representations should be lowerable to local ordinals,
  ordinal-indexed tables, SoA/primitive columns, compact stable-identity references, and backend-erased records where
  profiling justifies them.
- **P1 — Local ordinal law:** a dense ordinal is valid only inside the frozen/published generation that defines it and
  must never replace canonical semantic identity.

---

# 6. Contract Linking and Separate Compilation

- **P0 — Contract linker:** resolve independently compiled contract units without using JVM class loading as semantic
  linking.
- **P0 — Exact Version binding:** linking resolves authority + version to exact canonical definition.
- **P0 — Policy/World resolution:** world composition occurs before realization.
- **P0 — Duplicate authority detection:** reject conflicting same-identity definitions.
- **P0 — Missing dependency detection:** fail before artifact publication.
- **P0 — SCC handling:** contract graph cycles use deterministic explicit rules, not traversal accidents.
- **P1 — Separate compilation summary:** compile reusable semantic summaries for dependencies.
- **P1 — Link-time verification:** re-check cross-module assumptions and capabilities.
- **P2 — Thin-summary architecture:** reserve lightweight cross-module summaries analogous to modern thin whole-program
  compilation.
- **POST — Whole-program realization optimization:** only after user-realization optimization is admitted by later
  contract decisions.

## 6.1 Whole-Machine and cross-pipeline composition seam

The exact contract semantics belong to ADR-0055, but V1 compiler structure should not make single-pipeline execution the
only representable shape.

- **P0 — Composition IR seam:** reserve target-neutral representation for declared relations between
  Interface/Interaction flows.
- **P0 — Dependency without transport:** causal dependency, required establishment, ordering, and coexistence must not
  be encoded as RPC, queue, thread, process, or message-broker mechanics.
- **P0 — Fan-out/fan-in representability:** the IR must be able to represent one established result feeding multiple
  flows and multiple established results being required by another flow.
- **P0 — World provenance across flows:** collaborating material retains exact Contract/Policy/Version provenance.
- **P1 — Composition verifier:** reject missing, circular, ambiguous, or impossible dependencies according to the final
  ADR-0055 laws.
- **P2 — Distributed realization lowering:** monolith, multi-threaded, multi-process, and distributed backends remain
  alternative realizations of the same composition meaning where the contract does not distinguish them.

---

# 7. Pass, Analysis, and Rewrite Infrastructure

## 7.1 Pass manager

- **P0 — Pass abstraction:** compiler transformations are named, versioned, inspectable units.
- **P0 — Explicit pass pipeline:** pipeline order must not be hidden in scattered call chains.
- **P0 — Deterministic pipeline construction:** registry order must not affect pass order.
- **P0 — Analysis vs transform separation:** analyses compute facts; transforms mutate/lower representations.
- **P0 — Preserved-analysis declaration:** a transform states which cached analyses remain valid.
- **P0 — Analysis invalidation:** stale analysis results cannot survive a semantic mutation.
- **P1 — Nested pass scopes:** module/interface/operation/IR-region passes should have explicit scopes.
- **P1 — Pass isolation law:** a parallel pass cannot mutate unrelated sibling regions.
- **P1 — Pipeline configuration object:** one immutable compilation-session pipeline definition.
- **P1 — Optimization levels:** explicit optimization policy for Kontrakt-owned realization optimization.
- **P1 — Mandatory correctness passes:** cannot be disabled by ordinary optimization settings.
- **P1 — Pass statistics:** counts, transformed nodes, misses, and failure reasons.
- **P1 — Per-pass timing:** compile-time performance must be attributable.
- **P1 — Per-pass allocation/memory accounting:** prevent invisible compiler memory regressions.
- **P2 — Pass plugin seam:** reserve but do not allow plugins to create Contract Authority.
- **P2 — Pipeline serialization:** reproducible description of the exact pass pipeline used for an artifact.

## 7.2 Rewrite and conversion

- **P0 — Rewrite API:** transforms use one controlled mutation/replacement mechanism.
- **P0 — Rewrite preconditions:** pattern applicability is explicit.
- **P0 — Meaning-preservation declaration:** optimization rewrites must declare what observable meaning they preserve.
- **P0 — Contract Preservation Invariant:** every semantics-affecting transform must identify the Contract obligations
  that must remain unchanged across the transform.
- **P1 — Pattern registry:** deterministic registration and priority.
- **P1 — Conversion target:** define legal/illegal operations for progressive lowering.
- **P1 — Type/value conversion:** target conversion cannot smuggle target layout into semantic types.
- **P1 — Rewrite boundedness:** rewrite loops require deterministic termination limits.
- **P1 — Rewrite provenance:** diagnostics can identify which transform produced a problematic shape.
- **P2 — Cost-aware rewrite selection:** reserve deterministic cost models.
- **POST — Equality saturation:** possible later optimizer; do not make V1 dependent on e-graphs.

- **P0 — Pass-specific preservation obligation:** each semantics-affecting pass must state exactly which observable
  Contract properties it preserves; one generic notion of "equivalence" is not sufficient for all passes.
- **P0 — Preservation evidence boundary:** the pass contract and the mechanism used to validate it remain separate.
  Static checking, translation validation, differential execution, exhaustive finite checking, solver-backed reasoning,
  fuzzing, or golden vectors may be selected according to the obligation.
- **P0 — No false proof claim:** testing or fuzzing evidence must never be represented as a mathematical proof, and an
  unprovable property must not be silently assumed.

---

# 8. IR Verification and Translation Validation

- **P0 — Verify on phase boundaries:** malformed IR never silently reaches the next compiler layer.
- **P0 — Contract adherence check:** before optimized/lowered material is published, verify that every source semantic
  obligation still has an equivalent or stronger realization obligation and that no optimizer-created path can bypass
  it.
- **P0 — Pass preservation gate:** a pass that cannot establish its declared Contract Preservation Invariant must fail
  compilation or be rejected from the production pipeline.
- **P1 — `verify-each` debug mode:** run verifier after every transformation pass.
- **P1 — Pre/post lowering checks:** verify that every required semantic obligation has a target realization.
- **P1 — Capability verification:** backend cannot claim a guarantee it cannot realize.
- **P1 — Reference semantic evaluator:** keep a simple correctness-oriented judgment projection where useful.
- **P1 — Generated realization differential checks:** generated gate/result must agree with reference semantics.
- **P1 — Emitter differential tests:** source-emitted and direct-classfile realizations must agree on contract-visible
  behavior.
- **P1 — Translation-validation hook:** transformations may be validated independently of the optimizer that produced
  them.
- **P1 — Arithmetic and failure-semantics checks:** overflow, narrowing, exceptional paths, evaluation boundaries, and
  failure attribution must be included where a target transform can change observable behavior.
- **P2 — Transform proof/evidence channel:** allow future formal or solver-backed validation without redesigning the
  pass API.
- **POST — Fully formally verified optimizer:** outside V1.

Recent compiler-testing research strongly supports combining transformation-targeted fuzzing with translation
validation. Kontrakt should preserve the structural seam even if V1 uses simpler validators.

## 8.1 Pass-specific preservation evidence

Different transformations preserve different kinds of meaning. The framework should allow each pass family to declare
the appropriate obligation and evidence rather than forcing every transform through one universal proof system.

Examples:

```text
canonical transformation
    -> canonical-equivalence verification

semantic lowering
    -> declared-obligation preservation

static gate specialization
    -> differential verification against reference judgment

dead-path elimination
    -> proof or validation that the removed path is contract-unobservable

JVM numeric lowering
    -> overflow, narrowing, exceptional-path, and failure-semantics preservation

physical layout transformation
    -> lookup/result equivalence

parallel execution
    -> deterministic publication equivalence
```

The required result is fail-closed preservation of Contract meaning. The chosen validation technology remains
replaceable compiler implementation.

---

# 9. Query, Dependency, and Incremental Architecture

Kontrakt already has deterministic cache foundations. V1 should avoid turning those caches into a dead-end API.

## 9.1 Query/request model

- **P0 — Query boundary:** expensive semantic computations should be expressible as keyed compiler computations.
- **P0 — Explicit inputs:** a query may only depend on declared compiler inputs or other queries.
- **P0 — Immutable result preference:** query results should be immutable or published as immutable snapshots.
- **P0 — Deterministic query law:** same key + same explicit inputs = same semantic result.
- **P1 — Dependency recording:** query-to-query reads form an inspectable dependency graph.
- **P1 — Query cycle detection:** cycles fail or use an explicitly designed SCC protocol.
- **P1 — Query diagnostics context:** failures retain the semantic key that triggered them.
- **P1 — Query memory policy:** not every result must remain resident.
- **P1 — Cheap-vs-expensive result policy:** some results should be recomputed instead of serialized.
- **P2 — Demand-driven evaluation:** compute only requested semantic products.
- **P2 — Parallel query execution:** only after deterministic merge/publication rules are proven.

## 9.2 Incremental compilation hooks

- **P0 — Stable fingerprints:** source, semantic IR, canonical material, and important query results require stable
  hashing.
- **P0 — Dependency graph schema:** V1 structure must not prevent future red/green-style invalidation.
- **P0 — Full-build/incremental equivalence law:** incremental mode may not create different semantic output.
- **P1 — Content-addressed persistent cache boundary.**
- **P1 — Compiler/environment fingerprint:** target, feature set, language version, backend capabilities, and relevant
  toolchain versions belong in cache validity.
- **P1 — Schema-aware invalidation:** incompatible compiler/IR/cache versions invalidate cleanly.
- **P2 — Red/green invalidation engine.**
- **P2 — Early cutoff when changed input produces unchanged semantic output.**
- **P2 — Remote cache compatibility.**
- **POST — Fully distributed incremental build execution.**

---

# 10. Determinism and Reproducibility Infrastructure

This is a first-class Kontrakt requirement, not release polish.

## 10.1 Deterministic semantics

- **P0 — No hash-map iteration authority.**
- **P0 — No filesystem enumeration authority.**
- **P0 — No thread/completion-order authority.**
- **P0 — No memory-address/object-identity authority.**
- **P0 — No clock/time authority unless explicitly an input.**
- **P0 — No locale/timezone authority.**
- **P0 — No ambient environment-variable authority.**
- **P0 — Randomness requires explicit deterministic seed material.**
- **P0 — Diagnostic ordering is deterministic.**
- **P0 — Artifact naming is deterministic.**
- **P0 — Cache-key construction is canonical and domain-separated.**
- **P0 — Parallel work publishes through deterministic merge/seal.**

## 10.2 Reproducible artifacts

- **P0 — Reproducibility perimeter:** explicitly define which environment facts are allowed inputs.
- **P0 — Build manifest:** compiler version, language version, target, backend capabilities, dependency digests, and
  feature flags.
- **P0 — Stable output ordering.**
- **P0 — Normalize or exclude timestamps.**
- **P0 — Normalize build paths where they are not semantic.**
- **P0 — Normalize archive/class metadata that can vary accidentally.**
- **P1 — Bit-for-bit artifact reproducibility target where JVM/toolchain permits it.**
- **P1 — Rebuild-under-variation CI:** vary path, locale, timezone, file order, parallelism, and worker count.
- **P1 — Reproducibility digest report.**
- **P1 — Clean-build vs daemon-build equivalence test.**
- **P1 — Single-thread vs parallel-build semantic/artifact equivalence test.**
- **P2 — Diverse build verification in release infrastructure.**

## 10.3 Published semantic generation law

- **P0 — Immutable publication:** once semantic compiler material is sealed and published, later passes must not mutate
  that published authority in place.
- **P0 — Private construction is allowed:** a pass may use bounded private mutable builders, arenas, or temporary
  structures while constructing candidate output.
- **P0 — Candidate-before-authority:** candidate material becomes visible only after validation, required preservation
  checks, canonicalization, deterministic merge, seal, and publication.
- **P0 — Generation identity is not semantic identity:** multiple physical generations may carry the same semantic
  meaning, and a generation identifier must not become Contract identity.
- **P0 — Schedule-independent publication:** concurrent candidate construction may vary physically, but publication must
  not depend on worker, queue, or completion order.
- **P1 — Replaceable persistence model:** copy-on-write, persistent structures, generation images, delta logs, epoch
  publication, or MVCC-like storage remain implementation choices behind this law.

V1 should establish this publication contract without defining MVCC, copy-on-write, or any other storage strategy as
compiler semantics.

---

# 11. Deterministic Parallel Compilation

- **P0 — Semantic order independent of execution order.**
- **P0 — Worker-local mutable state must never define IDs or ordering.**
- **P0 — Deterministic final merge/seal.**
- **P0 — Parallel completion cannot alter diagnostics or canonical material.**
- **P1 — Explicit parallel work graph.**
- **P1 — Phase-specific concurrency capability declarations.**
- **P1 — Bounded worker count and memory admission.**
- **P1 — Cancellation must not publish partial material.**
- **P1 — Stale worker result rejection through generation/epoch identity.**
- **P1 — Parallel stress tests with randomized scheduling.**
- **P1 — Repeat-build race detector corpus.**
- **P2 — Work stealing only behind deterministic publication boundaries.**

- **P0 — Published-state mutation prohibition:** concurrent work may construct private candidate state but must not
  mutate already-published semantic IR or canonical images.
- **P0 — Deterministic commit boundary:** concurrent candidate results enter published compiler state only through a
  validated deterministic merge/seal boundary.
- **P0 — Concurrency mechanism non-authority:** MVCC, epochs, locks, lock-free structures, work stealing, or persistent
  collections are realization options and cannot define semantic ordering.

---

# 12. Diagnostic Architecture

Compiler diagnostics and Contract Diagnostic Evidence are related but must remain separate layers.

## 12.1 Structured compiler diagnostic engine

- **P0 — Stable diagnostic code.**
- **P0 — Severity:** error, warning, note, remark.
- **P0 — Primary source span.**
- **P0 — Secondary labeled spans.**
- **P0 — Phase identity:** parser, resolver, semantic, verifier, lowering, backend, linker, runtime-generation.
- **P0 — Contract Authority attribution where applicable.**
- **P0 — Deterministic causal chain.**
- **P0 — Deterministic diagnostic sorting and deduplication.**
- **P0 — Human rendering separated from diagnostic data.**
- **P1 — `note` and `help` attachments.**
- **P1 — Structured fix-it suggestions.**
- **P1 — Fix-it applicability level.**
- **P1 — Machine-readable diagnostic output for IDE/CI.**
- **P1 — Source excerpt renderer with stable width behavior.**
- **P1 — Diagnostic suppression/cascade control.**
- **P1 — Maximum-error budget.**
- **P1 — Related authority/version information.**
- **P1 — Diagnostic golden/UI tests.**
- **P1 — Fix-it application tests that recompile the fixed source.**
- **P2 — Stable diagnostic schema version for external tooling.**
- **P2 — Localization only if it can remain completely outside semantic identity.**

## 12.2 Compiler remarks and explainability

- **P1 — Optimization remarks:** what optimization happened.
- **P1 — Missed optimization remarks:** why a legal optimization did not happen.
- **P1 — Analysis remarks:** facts affecting transformation decisions.
- **P1 — Capability remarks:** why a backend path was selected or rejected.
- **P1 — Cache/query remarks:** hit, miss, invalidation reason, recomputation reason.
- **P1 — Determinism remarks:** identify suppressed non-deterministic input/order.
- **P1 — `--explain <diagnostic-code>`.**
- **P2 — Machine-readable optimization record for profiling tooling.**

---

# 13. Compiler Driver and Compilation Session

- **P0 — CompilerDriver:** orchestration only; never Contract Authority.
- **P0 — CompilationSession:** explicit lifetime for one compilation.
- **P0 — Immutable options snapshot.**
- **P0 — Explicit target configuration.**
- **P0 — Explicit language/feature configuration.**
- **P0 — Explicit backend capability snapshot.**
- **P0 — Source set and dependency set snapshots.**
- **P0 — Diagnostic sink.**
- **P0 — Artifact sink.**
- **P0 — Cache/query services passed explicitly rather than ambient singletons.**
- **P0 — Compiler resource policy snapshot.**
- **P1 — Cancellation token with safe phase boundaries.**
- **P1 — Compilation deadline/watchdog as orchestration, not semantic authority.**
- **P1 — Session statistics and trace ID.**
- **P1 — Reproducer manifest capture.**
- **P1 — Deterministic CLI option normalization.**
- **P2 — Compiler daemon session reuse with strict cross-run isolation.**

---

# 14. Compiler Resource and Memory Architecture

Kontrakt already has stronger resource discipline than many compilers. Preserve it across the full compiler.

- **P0 — Per-phase memory accounting.**
- **P0 — Per-run physical and semantic work budgets.**
- **P0 — Immutable configured caps separated from mutable remaining budgets.**
- **P0 — Bounded parser nesting and input sizes.**
- **P0 — Bounded rewrite iterations.**
- **P0 — Bounded diagnostic material.**
- **P0 — Bounded interning collision escalation.**
- **P0 — Publication only after complete validation.**
- **P1 — Arena/region lifetime by compiler phase.**
- **P1 — Hot/cold data split.**
- **P1 — Dense ordinal-indexed hot tables.**
- **P1 — Explicit large temporary-buffer lifetime.**
- **P1 — Memory high-water telemetry.**
- **P1 — Daemon leak tests.**
- **P1 — ClassLoader and generated-artifact lifetime tests.**
- **P2 — Off-heap/slab realization only after profiling proves value.**

- **P0 — Semantic identity never equals storage identity:** HID, canonical identity, and stable protocol identity must
  remain separate from dense ordinal, slab offset, array index, object address, and cache location.
- **P1 — Published hot-data lowering:** after semantic material is sealed, high-frequency compiler data should be
  eligible for dense ordinal/table/SoA lowering without changing the semantic model.

---

# 15. JVM Backend Foundation

## 15.1 Target boundary

- **P0 — Backend-neutral Lowered Machine IR ends before JVM concepts appear.**
- **P0 — JVM target descriptor:** classfile version, JDK baseline, relevant target features.
- **P0 — JVM capability matrix:** exact guarantees supported by this backend.
- **P0 — Fail-closed capability matching.**
- **P0 — JVM realization lowering is separate from artifact emission.**

## 15.2 JVM IR

- **P0 — Class/module representation.**
- **P0 — Method representation.**
- **P0 — Field representation.**
- **P0 — JVM type/descriptor representation.**
- **P0 — Basic-block / control-flow representation.**
- **P0 — Values and constants.**
- **P0 — Calls/invocations.**
- **P0 — Branch/return/throw operations.**
- **P0 — Local/field access operations.**
- **P1 — Exception-region representation if generated machinery requires it.**
- **P1 — Stack/local-frame derivation or explicit model.**
- **P1 — Constant-pool abstraction behind emitter.**
- **P1 — Source-origin attachment for generated bytecode diagnostics/debugging.**
- **P1 — JVM IR verifier.**
- **P1 — Deterministic JVM IR textual dump.**
- **P2 — JVM IR serialization for isolated backend tests.**

## 15.3 Emission

- **P1 — Source emitter:** safe transitional path through javac/kotlinc.
- **P1 — Direct classfile emitter:** narrow but real V1 path.
- **P1 — OpenJDK Class-File API abstraction:** preferred standard JDK integration where baseline permits.
- **P1 — Classfile structural verification after emission.**
- **P1 — Deterministic class/member/attribute ordering where format semantics permit.**
- **P1 — Generated artifact digest.**
- **P1 — Source-vs-classfile emitter differential tests.**
- **P2 — Wider direct-classfile coverage.**

## 15.4 JVM-owned optimization

Only Kontrakt-owned generated machinery is in V1 scope.

- **P1 — Constant propagation.**
- **P1 — Constant Contract/Policy/Version binding elimination.**
- **P1 — Branch simplification.**
- **P1 — Dead generated path removal.**
- **P1 — Primitive specialization.**
- **P1 — Boxing avoidance.**
- **P1 — Generic dispatch elimination where semantics are closed.**
- **P1 — Static gate specialization.**
- **P1 — Small generated-method fusion when it preserves diagnostics and attribution.**
- **P1 — Allocation-shape reduction for Kontrakt-owned carriers.**
- **P2 — Backend cost model.**
- **P2 — PGO input/output hooks.**
- **POST — Arbitrary user implementation optimization.**
- **POST — Native instruction selection/register allocation.**

Opcode selection belongs at the end of JVM lowering. Opcode names must never appear in Contract IR or semantic lowering.

## 15.5 Realization environment boundary

The compiler must not pretend that arbitrary OS or hardware failures are controllable contract semantics. Environmental
guarantees cross an explicit backend capability boundary instead.

- **P0 — Explicit environmental inputs:** time, locale, filesystem state, host capabilities, and similar facts influence
  generated behavior only when admitted as explicit contract/realization inputs.
- **P0 — Capability-based realization:** the backend states which environmental guarantees it can observe, isolate, or
  enforce; unsupported required guarantees fail closed.
- **P0 — No physical-effect authority in semantic IR:** OOM, scheduler behavior, disk failure, RPC, and OS mechanisms do
  not become Contract Authority merely because the JVM realization encounters them.
- **P1 — Controlled effect adapters:** where Kontrakt owns an effect boundary, generated adapters provide explicit
  failure attribution and deterministic contract-visible outcomes.
- **P2 — Stronger sandbox/isolation backends:** process, container, or hardware isolation may be added as realizations
  without changing semantic IR.

---

# 16. Generated API, ABI, and Compatibility

- **P0 — Generated host interface is artifact, not authority.**
- **P0 — Deterministic generated names.**
- **P0 — Generated public API signature snapshot.**
- **P1 — Binary compatibility validation for generated JVM APIs.**
- **P1 — Source compatibility validation where promised.**
- **P1 — Compiler/runtime compatibility matrix.**
- **P1 — Classfile/JDK baseline compatibility tests.**
- **P1 — Artifact metadata schema version.**
- **P1 — Old generated artifacts fail clearly when runtime/compiler compatibility is impossible.**
- **P2 — Library-evolution policy for long-lived generated APIs.**

---

# 17. Verification, PBT, Fixture, and Test-Generation Product

These are product subsystems, not merely QA for the compiler.

- **P0 — Contract-derived valid witness generation.**
- **P0 — Contract-derived invalid witness generation.**
- **P0 — Boundary witness generation.**
- **P0 — Deterministic fixture generation.**
- **P0 — Reproducible seed/material identity.**
- **P0 — Failure attribution from generated cases.**
- **P0 — State-machine legal/illegal transition generation.**
- **P0 — Budget/Capacity boundary cases.**
- **P0 — Policy/World-specific cases.**
- **P1 — Generated case deduplication.**
- **P1 — Bounded generation plans.**
- **P1 — Coverage accounting by contract obligation rather than only source line.**
- **P1 — Differential reference-vs-generated enforcement execution.**
- **P1 — Deterministic report format.**
- **P1 — Fixture lifecycle and reclamation.**
- **P1 — Parallel case execution with deterministic merge.**
- **P1 — JUnit/Gradle adapter boundary.**
- **P2 — Shrinking/minimization architecture.**
- **POST — Full compiler-style mocking engine unless V1 scope is changed.**

---

# 18. Compiler QA Infrastructure

Compiler QA is separate from Kontrakt-generated user verification.

## 18.1 Test classes

- **P0 — Unit tests for isolated data structures/protocols.**
- **P0 — Parser tests.**
- **P0 — Resolver tests.**
- **P0 — Semantic acceptance/rejection tests.**
- **P0 — IR verifier tests.**
- **P0 — Lowering golden tests.**
- **P0 — Pass regression tests.**
- **P0 — Diagnostic/UI golden tests.**
- **P0 — End-to-end project tests.**
- **P0 — Reproducibility tests.**
- **P0 — Performance regression suite.**
- **P0 — Memory regression suite.**
- **P0 — Generated-program execution tests.**
- **P0 — PBT/fixture engine self-tests.**
- **P1 — Cross-JDK matrix.**
- **P1 — Cross-OS/path/locale/timezone matrix.**
- **P1 — daemon/repeated-compilation tests.**
- **P1 — clean-vs-incremental equivalence tests.**
- **P1 — single-worker-vs-multi-worker equivalence tests.**

## 18.2 Fuzzing and adversarial validation

- **P1 — Lexer/parser fuzzing.**
- **P1 — Malformed binary/IR/cache fuzzing.**
- **P1 — Semantic graph fuzzing.**
- **P1 — Rewrite/pass fuzzing.**
- **P1 — JVM emitter fuzzing.**
- **P1 — Differential fuzzing between emitters.**
- **P1 — Optimization-directed fuzzing harness.**
- **P1 — Metamorphic tests:** irrelevant source/order changes must not alter meaning.
- **P1 — Deterministic schedule perturbation.**
- **P1 — Hash-collision/interning stress.**
- **P1 — Resource-exhaustion adversarial corpus.**
- **P1 — Deep nesting/large identifier/large graph corpus.**
- **P2 — Automatic test-case minimizer.**
- **P2 — Continuous translation validation for selected transforms.**

Recent PLDI-era work such as optimization-directed fuzzing and targeted optimization fuzzing reinforces the need to test
individual transformations, not only full compiler pipelines.

---

# 19. Crash Reproduction and Compiler Debuggability

- **P1 — Automatic crash reproducer bundle.**
- **P1 — Capture source subset, normalized options, compiler version, target, capability snapshot, and dependency
  identities.**
- **P1 — Deterministic IR dump on internal compiler error.**
- **P1 — Last successful phase/pass identity.**
- **P1 — Optimization/pass bisection tool.**
- **P1 — `--verify-each`.**
- **P1 — `--dump-ir=<stage>`.**
- **P1 — `--print-before/after=<pass>`.**
- **P1 — `--time-passes`.**
- **P1 — `--pass-statistics`.**
- **P1 — `--explain-cache-key`.**
- **P1 — `--explain-world`.**
- **P1 — `--explain-backend-choice`.**
- **P2 — Automatic reduced reproducer/minimization.**

---

# 20. Observability and Performance Engineering

- **P0 — Phase timers.**
- **P0 — Peak memory by phase.**
- **P0 — Allocation volume by major subsystem.**
- **P0 — Cache/query hit and invalidation metrics.**
- **P0 — Canonicalization/interning collision metrics.**
- **P0 — Frozen-image size and publication cost.**
- **P0 — Planning physical/semantic work metrics.**
- **P0 — Generated artifact size.**
- **P0 — Runtime-gate overhead benchmarks.**
- **P1 — Per-pass transformation counters.**
- **P1 — Missed-optimization reasons.**
- **P1 — Cold/warm compiler benchmark modes.**
- **P1 — Small/medium/large representative project corpus.**
- **P1 — Synthetic worst-case corpus.**
- **P1 — CI thresholds for compile-time and memory regressions.**
- **P1 — Benchmark result schema and historical comparison.**
- **P2 — Profile import/export hooks.**
- **P2 — Deterministic cost-model framework.**

---

# 21. Tooling and IDE Foundation

Full IDE tooling can remain post-V1, but source and semantic infrastructure must not block it.

- **P0 — Stable source spans and symbol identities.**
- **P0 — Machine-readable diagnostics.**
- **P1 — Compiler query API suitable for IDE requests.**
- **P1 — `kontraktc` driver CLI.**
- **P1 — `kontrakt-opt`-style IR/pass debugging tool.**
- **P1 — IR verifier CLI.**
- **P1 — artifact/contract-world inspection CLI.**
- **P2 — LSP server boundary.**
- **P2 — go-to-definition.**
- **P2 — find references.**
- **P2 — hover exact Contract Authority / Version / Policy binding.**
- **P2 — semantic completion.**
- **P2 — rename with authority-safe checks.**
- **P2 — code actions/fix-its.**
- **P2 — incremental IDE analysis server.**

---

# 22. Build-System and Artifact Integration

- **P0 — Gradle integration boundary.**
- **P0 — deterministic generated-source/class output directories.**
- **P0 — dependency declaration model.**
- **P0 — incremental-input/output declarations even before full incremental compiler support.**
- **P0 — artifact manifest and digests.**
- **P0 — generated artifact cleanup law.**
- **P1 — worker/daemon isolation.**
- **P1 — deterministic parallel build integration.**
- **P1 — build cache key schema.**
- **P1 — dependency/toolchain lock information.**
- **P1 — reproducible packaging.**
- **P1 — compiler/runtime artifact compatibility validation.**
- **P2 — remote build cache.**
- **P2 — remote execution compatibility.**

---

# 23. Security and Reliability Foundation

- **P0 — Treat all source and cached binary material as hostile input.**
- **P0 — No unbounded recursive parser/decoder paths.**
- **P0 — Length-prefix and allocation bounds before allocation.**
- **P0 — Checked integer arithmetic for offsets/sizes.**
- **P0 — Fail closed on malformed persistent compiler artifacts.**
- **P0 — Never deserialize JVM object graphs as compiler authority.**
- **P0 — Dependency version pinning for release builds.**
- **P1 — Supply-chain/license checks.**
- **P1 — Secure temporary-file handling.**
- **P1 — Crash isolation in daemon mode.**
- **P1 — stale cache corruption detection.**
- **P1 — checksum/digest verification for persistent artifacts.**
- **P0 — Cache trust boundary:** a cache hit may reuse computation but must never bypass schema, identity, dependency,
  or IR verification required for a clean computation.
- **P1 — Content/input binding:** persistent entries bind canonical input/dependency fingerprints to the stored result
  so unrelated or stale material cannot be accepted under another key.
- **P2 — Authenticated remote cache/provenance:** signatures or authenticated transport are required only when the
  deployment threat model treats the cache producer/store as an untrusted security boundary; they are not Contract
  Authority.
- **P1 — fuzz all binary readers.**
- **P2 — sandbox external tools if future frontends invoke them.**

---

# 24. Schema and Evolution Discipline

Kontrakt has several different kinds of versioning. They must remain separate.

- **P0 — Contract Version:** semantic authority revision.
- **P0 — Kontrakt language version:** source grammar/semantics.
- **P0 — Compiler version:** implementation/toolchain release.
- **P0 — IR schema version:** persisted compiler IR.
- **P0 — Cache schema version.**
- **P0 — Artifact metadata version.**
- **P0 — JVM/classfile target version.**
- **P0 — Diagnostic machine-schema version if diagnostics become API.**
- **P1 — Explicit compatibility matrix between these versions.**
- **P1 — deterministic migration or explicit invalidation; never guessed compatibility.**
- **P1 — golden vectors for every stable binary/canonical protocol.**

## 24.1 Contract data evolution boundary

Contract Version does not imply compatibility and must not automatically synthesize data migration. V1 should
nevertheless reserve a clean boundary for explicit future migration laws.

- **P0 — Version is identity, not migration:** no compatibility, upgrade, downgrade, or fallback is inferred from
  Version names or order.
- **P1 — Explicit migration seam:** if a later Contract Authority defines migration, the compiler can lower a declared
  source-version → target-version transformation without changing either version's meaning.
- **P1 — Migration provenance:** migrated material records exact source authority/version, migration law
  identity/version, and target authority/version.
- **P1 — Migration verification:** generated bridges are checked like other lowering transformations and cannot bypass
  Input, Admission, Invariant, or Publication laws.
- **P2 — Generated schema bridges:** automatic code generation is allowed only from explicit migration material;
  canonicalization alone must not invent compatibility.

---

# 25. Research-Driven Extension Hooks Worth Reserving

These are useful SOTA directions, but most should **not** become mandatory V1 implementation work.

## 25.1 Translation validation

- **P1 hook:** validate important transformations independently from the transformation implementation.
- Particularly suitable for Kontrakt because contract-preserving transformations have explicit semantic material to
  compare.

## 25.2 Targeted optimization fuzzing

- **P1 hook:** invoke individual passes against generated IR patterns rather than only fuzzing end-to-end compilation.
- Recent work shows full pipelines can miss optimization-specific interactions.

## 25.3 Equality saturation / persistent e-graphs

- **P2 hook only.**
- Potentially useful for deterministic search over equivalent Kontrakt-owned realizations.
- Must have explicit bounds, deterministic extraction, and a contract-preserving equivalence relation.
- V1 should not depend on e-graph saturation.

## 25.4 Cost models

- **P2 hook:** cost-model API for choosing among semantically equivalent realizations.
- Inputs must be explicit and deterministic for release compilation.
- Learned or runtime-dependent models must not silently become Contract Authority.

## 25.5 Profile-guided optimization

- **P2 hook:** profile schema/import seam.
- Profile data may guide realization optimization only where contract meaning is unchanged.
- Reproducible release mode must explicitly declare whether profile data is part of the build input.

## 25.6 Formal transformation proofs

- **P2 hook:** allow later solver/proof evidence to attach to transformation validation.
- Do not block V1 on full formal verification.

---

# 26. SOTA Baselines Used for This Checklist

The architecture above borrows **patterns**, not semantics, from mature compiler systems.

### LLVM / Clang

Useful patterns:

- New Pass Manager.
- analysis preservation and invalidation.
- explicit optimization pipelines.
- machine-specific MIR for isolated code-generation testing.
- per-pass verification.
- optimization remarks: passed, missed, analysis.
- pass statistics and timing.
- optimization bisection.
- automatic crash reproducers.
- unit, regression, and whole-program test layers.

### MLIR

Useful patterns:

- progressive multi-level lowering.
- explicit conversion targets and legal/illegal operations.
- pattern-based rewrites.
- operation-local verifiers.
- nested pass management with parallel-safety restrictions.
- textual and bytecode IR.
- IR bytecode versioning and upgrade hooks.
- dedicated optimization/debug tools.

### rustc

Useful patterns:

- AST/HIR/MIR separation.
- SourceMap/Span infrastructure.
- demand-driven query system.
- explicit query dependency DAG.
- deterministic query requirement.
- red/green incremental invalidation.
- stable fingerprints.
- compiler session/driver separation.
- UI diagnostic golden tests and executable fix-it tests.

### Swift

Useful patterns:

- explicit compiler architecture stages.
- SIL as a compiler-owned semantic/lowered IR.
- request evaluator for dependency tracking and cached compiler computations.
- separation of source/module evolution and ABI concerns.

### Kotlin K2

Useful patterns:

- rewritten frontend centered on a richer unified semantic representation.
- frontend architecture shared with IDE analysis.
- continuing work on finer-grained incremental compilation.

### GCC

Useful patterns:

- multiple IR abstraction levels such as GENERIC, GIMPLE, and RTL.
- clear separation between language frontend representation, optimization IR, and low-level target representation.
- broad regression and target test infrastructure.

### OpenJDK

Useful pattern:

- JEP 484 Class-File API for standard parsing, generation, and transformation of JVM class files.

### Graal

Useful patterns:

- language-independent graph IR.
- phase-oriented optimization.
- aggressive specialization/inlining behind language semantics.
- compiler graph dumps.
- PGO integration in production AOT compilation.

### Reproducible Builds

Useful discipline:

- make the build deterministic first.
- explicitly define the build-environment perimeter.
- normalize time, path, locale, randomness, archive metadata, and input/output order.
- record tool versions and build inputs.
- verify artifacts by rebuild and cryptographic comparison.

### Recent research to keep in view

- **Optimuzz, PLDI 2025:** optimization-directed fuzzing combined with continuous translation validation.
- **TargetFuzz, 2025:** targeted testing of individual optimizations through grammar-level composition patterns.
- **eqsat, 2025:** equality saturation represented directly inside compiler IR.
- **Persistent e-graph compiler abstraction, 2026:** preserving equivalence information across multiple IR levels.

Kontrakt should import only ideas compatible with explicit Contract Authority, bounded execution, and deterministic
compilation.

---

# 27. Candidate V1 Non-Negotiable Foundation

If V1 is intended to become a commercial compiler rather than a prototype, the following structures should be treated as
the strongest candidates for mandatory inclusion:

1. Real SourceManager and source provenance.
2. Proper lexer/parser + recovery.
3. Symbol/module/name-resolution layer.
4. Semantic Contract IR.
5. Canonical/Frozen Contract representation.
6. Contract linker/world resolver.
7. Lowered Contract-Machine IR.
8. IR verifier at every major boundary.
9. Pass Manager.
10. Analysis Manager + explicit invalidation.
11. Rewrite/conversion framework.
12. Compiler Driver + immutable CompilationSession.
13. Stable diagnostic engine with machine-readable diagnostics.
14. Compiler remarks/explain infrastructure.
15. Query/request abstraction compatible with future incremental compilation.
16. Stable fingerprint/dependency architecture.
17. Deterministic compiler work graph and deterministic merge/publication.
18. Reproducible-build manifest and environmental normalization.
19. JVM realization-lowering boundary.
20. Minimal JVM IR.
21. Source emitter plus a narrow direct Class-File emitter.
22. Backend capability matrix and fail-closed realization check.
23. Structured compiler tracing, pass timing, statistics, and memory accounting.
24. Crash reproducer and pass/optimization bisection support.
25. Layered compiler QA: unit, regression, UI, end-to-end, performance.
26. Parser/IR/pass/backend fuzzing.
27. Translation-validation/differential-testing seam.
28. Contract Preservation Invariant and pass-level contract-adherence gates.
29. Whole-Machine/cross-pipeline composition IR seam independent of transport/runtime topology.
30. Cache trust boundary where persistence can never bypass clean semantic verification.
31. Realization-environment capability boundary rather than implicit OS/hardware semantics.
32. Contract-derived PBT and fixture engine.
33. Generated enforcement/reference differential verification.
34. API/ABI compatibility checks for generated JVM surfaces.
35. Persistent schema versioning for IR/cache/artifact formats.
36. Daemon/repeated-run isolation and leak testing.
37. Clean/single-thread/parallel/repeated-build determinism matrix.
38. Stable canonical protocols with golden vectors.
39. Explicit compiler resource budgets and bounded failure behavior.

40. Semantic identity / physical address separation across every IR and cache boundary.
41. Immutable published semantic generations with candidate-build → verify → canonical merge → seal → publish
    discipline.
42. Pass-specific preservation obligations with replaceable verification evidence mechanisms.

---

# 28. Strong V1 Skeletons, Full Implementation Later

The following should preferably have a clean structural seam in V1 without forcing the full feature into V1:

- Full red/green incremental compiler.
- persistent query cache.
- remote build cache.
- deterministic parallel query executor.
- wider direct-classfile backend.
- rich JVM cost model.
- PGO.
- whole-program user-code optimization.
- e-graph/equality-saturation optimizer.
- advanced formal translation validation.
- full LSP/IDE server.
- compiler plugin system.
- off-heap/slab IR storage.
- multi-backend target support.
- native code generation.
- arbitrary implementation-body IR.
- aggressive alias/escape/effect analysis over user code.

---

# 29. Things V1 Should Explicitly Avoid

- One giant IR spanning source syntax to JVM bytecode.
- Contract meaning encoded in JVM opcodes or class layout.
- Kotlin/Java source generation as the definition of the backend architecture.
- hidden global compiler state.
- global mutable interning as semantic identity.
- cache keys based on object identity or insertion order.
- pass order inherited from registry discovery order.
- diagnostics built from ad-hoc strings only.
- unversioned persistent IR/cache formats.
- backend capability assumptions without proof.
- optimizer transforms without verification boundaries.
- parallel compilation where completion order changes artifacts.
- incremental compilation that can disagree with a clean build.
- random fuzz/PBT output without reproducible seeds.
- compiler daemons that retain semantic or mutable run state across compilation boundaries.
- direct dependence on thread, coroutine, lock, CAS, queue, or JVM scheduling as compiler semantics.
- premature SSA/native backend for arbitrary user realization code.
- importing MLIR/LLVM/Graal concepts merely because they are fashionable; every imported structure must serve Kontrakt's
  contract and determinism laws.
- Treating dense ordinals, BLAKE3 HID, SoA tables, slabs, arenas, MVCC, copy-on-write, epochs, locks, CAS, or any other
  current mechanism as Contract meaning rather than replaceable realization.
- Using local ordinals, table positions, generation IDs, cache addresses, or physical storage identity as stable
  semantic identity.
- Requiring one universal proof technology for every transformation instead of declaring pass-specific preservation
  obligations and appropriate evidence.

---

# 30. Final Target Principle

Kontrakt V1 does not need to implement every optimization used by LLVM, rustc, Swift, GCC, Graal, or MLIR.

It **does** need the architectural seams that prevent V1 decisions from blocking those classes of compiler engineering
later.

The target is:

```text
explicit contract source
-> deterministic semantic resolution
-> stable canonical authority
-> immutable frozen publication
-> deterministic linking
-> verified progressive lowering
-> target capability proof
-> target-specific realization IR
-> deterministic artifact generation
-> generated verification
-> reproducible release artifact
```

with:

```text
queryable
incremental-ready
diagnosable
verifiable
fuzzable
profileable
resource-bounded
parallelizable without semantic order leakage
```

as architectural properties from the beginning.

The most important invariant remains:

> Any compiler optimization, cache, parallel schedule, backend representation, or generated artifact may change physical
> realization. None of them may silently change the Contract World that was established before realization.