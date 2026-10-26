# Kontrakt Contract-Aware Whole-Machine Compilation Research TODO

## Status and Scope

**Research and design-review TODO. This document is not an ADR and does not establish new Contract semantics or compiler
architecture decisions.**

The existing compiler Architecture Map already defines a Realization Frontend, a published **Realization Body IR**,
local structural analysis, Contract-aware analysis and closure verification, Execution Formation, Contract-Aware
Execution IR, Whole-Machine summaries, optimization, JVM planning, and direct Classfile emission. The existence of these
responsibilities and their intended separation are **not discoveries or competing whole-compiler architectures proposed
by this TODO**. They are the current working-design baseline, subject to Accepted ADRs. A design baseline is not an
implementation-complete claim or an Accepted ADR.

This TODO investigates a narrower unresolved problem: **which Host Compiler result should be used to construct the
already-planned Realization Body IR; when should information be acquired before Java/Kotlin lowering discards
distinctions; and how should this material be correlated with the actual JVM execution artifact and used for
verification, Contract-aware optimization, and final code generation?** It also compares the additional compilation
cost, runtime cost, and code-generation choices that follow from that acquisition decision. It does not reopen the
established producer-owned Contract semantics or ask whether Kontrakt needs an Operation/Realization IR in the first
place.

`What Contract Is` and applicable Accepted ADRs take precedence.
`kontrakt-compiler-total-architecture-map-design-draft.md` is the current working architectural baseline, particularly
§§11–16, 22–34, and 50–52. `kontrakt-contract-aware-realization-optimization-todo.md` remains the existing optimization
research inventory. This document refines their **Host Operation acquisition and integration decision**, rather than
replacing their pipeline, analysis, Protocol, or optimization responsibilities.

The Architecture Map's §11 uses Classfile-centered Realization Acquisition as the V1 production direction while leaving
the acquisition mechanism replaceable. High-level Host semantic acquisition is therefore a potential **change to the
acquisition/representation path**, not the invention of Whole-Machine analysis or a separate compiler. Implementation
difficulty alone does not exclude a candidate; nor does greater semantic richness prove better runtime performance. The
supported verification scope and measured whole-build cost must determine what is actually adopted.

### Existing Baseline and Unresolved Questions

| Area                                 | Existing decision or working design                                                                                 | Research still required here                                                                                                                   |
|--------------------------------------|---------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| Contract authority                   | HIR candidates and authority-owned Establishment form the Canonical Contract World; consumers use legal projections | No new Contract authority decision; identify only the Contract-derived inputs needed by Operation-specific analysis                            |
| User Operation representation        | Architecture Map §12 already defines the published Realization Body IR                                              | Choose and validate Classfile-derived, Host-semantic-derived, or hybrid construction and the exact preserved execution meaning                 |
| Realization analysis and eligibility | §§13–16 already separate local analysis, Contract-aware analysis, and closure verification                          | Demonstrate evidence completeness and correspondence for actual executable targets and compiler-introduced behavior                            |
| Joint execution and optimization     | §§22–31 already define Execution IR, analyses, optimizations, summaries, and selective body opening                 | Measure the additional transformations enabled by each acquisition strategy and their cost                                                     |
| JVM output                           | §§32–34 already define JVM planning and direct Classfile emission for Kontrakt-generated execution                  | Determine if and when Host-generated Operation methods are preserved, selectively transformed, re-emitted, or lowered through the Host backend |
| Reuse and incremental evolution      | ADR-0075 and the Architecture Map already define validity, producer ownership, and V1/V2 seams                      | Determine acquisition-specific keys, invalidations, build dependencies, and physical lifetime/cost                                             |

## 1. Assumptions to Preserve

Kontrakt forms Contract candidates from `.kontrakt` declarations and selected 1D Carriers, builds Resolved Contract HIR,
and obtains the Canonical Contract World through authority-owned Establishment. Contract meaning and identity are not
reconstructed from user implementation classes or JVM Classfiles.

A 1D Carrier is a frontend authoring representation. `javac` or `kotlinc` may be used to check its syntax or types, but
the resulting Host Classfile does not become 1D Contract authority. The cost of Host Compilation for small Carriers is
something to measure; it is not, by itself, a reason to implement a new Java/Kotlin semantic analyzer first. Kontrakt's
backend can lower the generated execution code for the 1D pipeline directly into Classfiles.

A user Operation is subject to the ordinary Java/Kotlin type checks, overload resolution, and language-semantic
analysis. Its execution is a separate Realization, and Kontrakt is responsible for verifying the eligibility of the
actual execution paths of the selected implementation. Selection of an implementation by external Composition or DI
remains separate from Contract Definition and 1D Selection.

The JVM loads, verifies, and executes final Classfiles. The production of a Classfile by `javac` does not mean the user
program has already been executed once by the JVM. Running the Kontrakt compiler itself in a JVM process is also
distinct from running the final user program in a JVM. Intermediate analysis or regeneration may constitute **additional
compilation work**, but it does not cause **the same user program to pass through JVM execution twice**.

The optimization direction agreed so far separates what can be established from the Contract alone from what requires
combining it with a user Operation. Contract-only analysis should not wait for final integration when its work can
already be completed. Conversely, optimizations inside an Operation must not be decided prematurely without establishing
the actual value and call relationships.

The Host Compiler integration concern is concentrated at **user Operation Realization**. Kontrakt forms the other 1D
execution machinery and its JVM realization from established Contract meaning. This does not mean an Operation must
inspect or depend on other 1D Contract definitions. Instead, Kontrakt's compiler consumers can use separately acquired
Operation structure together with legally projected established Contract information. Runtime Occurrence judgments
remain runtime obligations where the Contract requires them; compile-time analysis may optimize their realization but
cannot silently declare them discharged.

## 2. The Problem to Resolve

The already-planned compiler path is:

```text
Contract Authoring                         Java / Kotlin Operation Source
       │                                               │
       ▼                                               ▼
Kontrakt Frontend                               Host Compiler
       │                                               │
       ▼                                               ▼
Resolved Contract HIR                      Resolved Host Meaning
       │                                               │
       ▼                                acquisition before or after JVM lowering
Authority-Owned Establishment                          │
       │                                               ▼
       ▼                                    Kontrakt Realization Frontend
Canonical Contract World                              │
       │                                               ▼
       │                                     Published Realization Body IR
       │                                               │
       │                                   Local Structural Analysis
       │                                               │
       └──────────────────────┬────────────────────────┘
                              ▼
               Contract-Aware Analysis / Closure Verification
                              │
                              ▼
                       Execution Formation
                              │
                              ▼
                   Contract-Aware Execution IR
                              │
                              ▼
               Whole-Machine Analysis / Optimization
                              │
                              ▼
                    JVM Planning / Classfiles
                              │
                              ▼
                             JVM
```

The disputed point is the input to the **existing** Realization Frontend. In the current V1 design, `javac`/`kotlinc`
emits Classfiles and Kontrakt reconstructs analyzable body structure from them. That path is valid for supported
executable behavior, and its actual JVM representation is a valuable verification basis. Its limitation is that
source-selected types, calls, high-level value relations, and other distinctions may already have been lowered. Some
lost distinctions could improve Contract-aware optimization; others may be needed to prove the original Realization's
eligibility for a supported surface. If required meaning cannot be recovered, that case must not be silently accepted.

The research does **not** ask whether to create Realization Body IR, Whole-Machine analysis, or the Contract-Aware
Execution IR. It asks where the Host Compiler's resolved semantics can be intercepted or exported **without repeating
the Host Parser and type checker**, which information must be retained in Kontrakt-owned IR or companion evidence, and
how that information is verified against the Host-produced or Kontrakt-produced JVM artifact.

Acquisition timing, IR construction, verification of the original realization, Contract-aware analysis, and final code
generation are distinct choices. Early acquisition does not require immediate joint optimization. Nor does
Kontrakt-owned Realization IR necessarily require recompiling every user method or abandoning the JVM backend's
established responsibilities. The generated Operation ABI must be available before dependent Host Compilation;
otherwise, Contract-side compilation and Host compilation may run or be reused independently when their actual
dependencies allow it.

The optimization scope may span the pipeline even though Host semantic acquisition concerns Operation implementations.
The 1D Contracts do not need to understand Operation source or compiler IR. Kontrakt's downstream analysis consumes each
side through its legal material/protocol boundaries. In particular, the execution and outward meaning of Failure, State,
Publication, and Output must be preserved without treating those contracts as Host-language analyzers.

**Research objective:** preserve the current compiler architecture while finding the least redundant, sound acquisition
and code-generation strategy that captures useful Operation meaning, verifies executable closure, and makes
Contract-relative optimizations possible. Evaluate missed optimization, redundant Host/Kontrakt analysis, generation
cost, JVM runtime behavior, and mandatory verification coverage separately.

## 3. Realization Acquisition and Code-Generation Strategies to Compare

The alternatives below are **ways to populate and use the already-defined Realization Body IR and to generate final
methods**. They are not alternative definitions of Contract HIR, Establishment, Whole-Machine analysis, or the overall
compiler pipeline. A strategy may acquire more evidence without ever modifying the Host compiler's IR. Conversely, an
in-compiler transformation may be feasible for one Host language but not another.

### 3.1. A — Existing Classfile-Derived Realization Body IR

The Host Compiler compiles user Operations into JVM Classfiles. The existing Kontrakt Realization Frontend reads the
actual methods, CFGs, calls, and effects, produces the published Realization Body IR, and uses it with established
Contract information in the already-planned analysis and execution formation. Kontrakt's generated Contract execution
code and any selected transformed methods are emitted through the planned JVM backend.

This is the current Classfile-centered V1 working path, **not a rejected architecture**. It permits concrete inspection
of JVM execution and is independent of unstable Host Compiler internals. It may nevertheless require reconstruction of
high-level information or fail to retain distinctions unavailable from Classfiles. A failed optional optimization and
insufficient mandatory verification evidence are different outcomes.

**Research task:** Inventory the actual source-to-Classfile losses for Java and Kotlin, quantify the corresponding lost
Contract-specific transformations, and identify which supported Realization eligibility checks remain provable from
Classfiles alone. Use this as the correctness and performance baseline.

### 3.2. B — Existing Realization Body IR plus Host Semantic Evidence

During Host semantic analysis, acquire only additional resolved Source-Semantic Operations, types, symbols, call
selections, and source-to-JVM mappings that the Classfile-derived Body IR does not reliably preserve. The **same
Kontrakt-owned Realization Body IR** remains the central published analyzable representation; companion evidence may be
kept in a separately owned material family when its validity or consumers differ.

The evidence is compiler-produced information, not Contract authority. It must be tied to the exact Host Compiler
version, compilation inputs, relevant transformations, and corresponding generated artifact. Evidence acquired before
lowering does not by itself prove that the final executable closure contains only those operations.

**Research task:** Test actual `javac` and Kotlin K2 access points, semantic completeness, mapping precision, version
compatibility, extraction/validation cost, and which analyses improve beyond A. Avoid turning every Host semantic object
into a permanent mirrored graph.

### 3.3. C — Existing Realization Body IR Constructed from Resolved High-Level Operation Meaning

Instead of first reconstructing the Operation body solely from JVM instructions, obtain resolved Host Operation
structure **before the distinctions needed by Kontrakt are lowered away** and translate it into the already-planned
Kontrakt-owned Realization Body IR. This can retain richer typed value, call, control-flow, and source-semantic
relationships. Subsequent Realization analysis, closure verification, and Contract-aware optimization remain the
responsibilities already specified in the Architecture Map.

This is a high-priority **acquisition strategy**, not a proposal to introduce Realization IR or joint optimization for
the first time. The exact acquisition point may differ between `javac` and K2. The resulting IR must have an explicit
semantic vocabulary and sufficient linkage to the JVM execution that will actually run. Host lowering may introduce
bridges, synthetic paths, class initialization, exception behavior, synchronization, or other work not apparent in an
earlier representation. That behavior must be incorporated into an exact verification basis.

**Research task:** Determine whether and how each Host Compiler can expose sufficient resolved body semantics; specify
the minimal Kontrakt-owned Realization IR/evidence needed; establish actual JVM correspondence; measure costs and gains
against A and B. Do not assume that acquiring the body dictates who must generate final bytecode.

### 3.4. D — Selective Method/Region Transformation and Code Generation

This is a **downstream code-generation choice**, compatible with A, B, or C rather than a competing Whole-Machine
architecture. Kontrakt already owns JVM generation of its Contract execution machinery. Investigate when a verified
Operation method or region should also be emitted by Kontrakt, when a Host Compiler backend can realize an appropriately
transformed Host IR, and when the original verified Classfile is retained unchanged.

Preserving some methods and replacing others requires consistent ABI, access control, class initialization,
`StackMapTable`, exception tables, metadata, linking, and Classfile generation. For Kotlin, compiler-generated synthetic
code and metadata must remain coherent. An attractive transformation is not admissible if these properties cannot be
demonstrated.

**Research task:** Compare method-, class-, closure-, and caller/callee-level regeneration units; assess whether Host-IR
transformation can preserve the needed Contract and JVM properties; retain multiversion execution as a candidate where
specialized and original paths are both valid. Report both runtime improvement and duplicate code-generation work.

### 3.5. Secondary Alternative — Reimplement Host-Language Semantic Work

For completeness, compare a Kontrakt-owned Java/Kotlin parser, type checker, or full lowering implementation against
using the Host Compiler's existing resolved meaning. This is **not a requirement for Operation IR ownership**. It must
be justified by a verified capability missing from Host acquisition interfaces or by measured total-system benefit. Do
not exclude it purely due to implementation difficulty, and do not mistake duplication of language semantics for an
inherently more advanced Contract compiler.

### 3.6. Orthogonal Choices That Must Not Be Conflated

- **Acquisition:** Classfile reconstruction, supplemental Source Evidence, or high-level resolved body import.
- **Representation:** published Kontrakt Realization Body IR, with separately valid optional Host/JVM evidence and
  mapping products.
- **Verification:** eligibility of the original selected implementation and correspondence of its actual executable
  closure, independently of later optimization.
- **Analysis:** existing local, Contract-aware, Execution IR, and Whole-Machine consumers; summaries may avoid opening
  unrelated bodies.
- **Emission:** retain Host-generated methods, apply safe Classfile transforms, use selected Kontrakt code generation,
  or test Host-IR transformation and Host backend lowering.

Do not evaluate these dimensions as one indivisible A/B/C/D package. A result on one dimension need not predetermine the
others.

## 4. Technical Questions to Resolve Before a Decision

### 4.1. Which Semantics Belong to the Host Compiler?

- [ ] Identify the information Kontrakt actually consumes from Java overload resolution, generic types, capture
  conversion, boxing, bridge methods, default methods, and results after annotation processing.
- [ ] Analyze the correspondence between Kotlin K2 FIR resolution, backend IR, inlining, value classes, default
  arguments, coroutine and suspend lowering, smart casts, extension calls, and actual JVM methods.
- [ ] Classify cases that require both source-level semantics and final Bytecode semantics. Assign ownership so that the
  same meaning is not redundantly stored in two representations.
- [ ] Determine whether a common evidence format can be interpreted differently across `javac` or `kotlinc` versions,
  and specify the versioned basis needed to detect such differences.
- [ ] If Host Compiler APIs are used, evaluate read-only acquisition of semantics separately from participation in IR
  transformation or code generation.

### 4.2. How Is the Existing Realization Body IR Constructed?

- [ ] Specify the construction of the Architecture Map §12 Realization Body IR from pre-JVM-lowering Host meaning,
  Classfile reconstruction, or hybrid evidence. Do not present the existence of this IR as an open architectural
  question.
- [ ] Identify the acquisition window separately for `javac` and Kotlin K2: which semantic judgments are already
  resolved, which lowerings have already occurred, and which distinctions disappear at the next transition.
- [ ] Investigate how to transfer exact references, CFGs, value relations, call targets, and effects into Kontrakt-owned
  primitive representations without retaining Kotlin FIR/IR objects or javac AST objects as long-lived compiler
  products.
- [ ] When both high-level IR and final Classfiles are acquired, design a procedure to validate exact mappings from
  Source Operation to Host IR value or call to JVM method or instruction.
- [ ] Define the immutable evidence and verified boundary used to represent execution semantics for methods that will
  not be regenerated.
- [ ] Determine how to support or reject user libraries, older Classfiles, and external dependencies for which
  high-level IR is unavailable or only partially available.

### 4.3. To Which Values and Paths Do Contract Guarantees Apply?

- [ ] Distinguish Established Definition Meaning from guarantees of runtime Occurrence Judgments, and identify the exact
  producer-owned material that a compiler consumer may use.
- [ ] Explicitly trace which Facts become Established on successful Input, Admission, and Lowering paths and which
  fields are transferred to particular Operation parameters or SSA values.
- [ ] Distinguish Policy World, Contract Version, Governance Binding, State Surface, and Implementation Binding
  conditions fixed before compilation from those established only at runtime.
- [ ] Preserve application context and dominance so that a range or nonzero condition true only on a particular branch
  cannot leak into another path, Occurrence, or implementation.
- [ ] Do not infer identical values, Facts, or Contract guarantees solely from equal types or names. Specify the
  required identity, coordinate, and attribution proofs.

### 4.4. How Are Realization Admission and DI Connected?

- [ ] Determine how external Composition explicitly selects the exact implementation when multiple user implementations
  satisfy the same Operation Contract.
- [ ] Ensure that an unselected implementation does not become an execution candidate or change Contract meaning merely
  because it is on the Classpath.
- [ ] Verify the selected method's exact JVM target, possible virtual call targets, class initialization, injected
  fields, and helper closure.
- [ ] Avoid unnecessary regeneration of the Contract World after a Binding change, while invalidating verification and
  optimization products that depend on that Binding.
- [ ] Decide whether runtime implementation replacement or dynamic providers are supported. If so, distinguish
  preverified alternative execution paths from the range of optimizations valid for each.

### 4.5. Which Existing Whole-Machine Consumers Need Which Acquired Operation Information?

- [ ] Use the Architecture Map §§30–31 summary and selective body-opening design as the starting point. Determine which
  existing module-, interface-, operation-, method-, and region-level summaries need richer Operation source-semantic
  information; refine summary schemas only where consumer requirements justify it.
- [ ] Separate the roles of verification summaries and optimization summaries, reusing only foundational material that
  may legitimately be shared.
- [ ] Map which existing Contract-only, Realization-only, and cross-boundary analysis consumers gain information from
  each acquisition path. Do not recreate their established producer/consumer ownership or let summaries become Contract
  authority.
- [ ] Distinguish the precision required for deterministic closure verification from additional call-graph, alias, or
  effect analysis performed only to improve optimization profitability.
- [ ] Confirm that relations across multiple Interfaces and Cores are consumed only through explicitly permitted bases.
  Whole-Machine analysis must not introduce hidden Contract dependencies.

### 4.6. Which Cross-Boundary Optimizations Are Actually Beneficial?

- [ ] Specify the conditions under which Contract-derived constant and range propagation can eliminate unreachable
  branches or redundant computation inside Operations.
- [ ] Investigate direct calls and specialization based on exact Operation Bindings and call-target sets. Do not
  arbitrarily close JVM virtual dispatch that remains open.
- [ ] Investigate physical fusion across 1D pipelines and Operations. Preserve the order, reachedness, failure
  attribution, and subsequent Publication meaning of logical Judgments.
- [ ] Before eliminating physical Fact or Carrier objects, verify equality, observable identity, aliasing, escape, class
  initialization, exception, and synchronization semantics.
- [ ] Investigate how typed SSA, CFG analysis, conditional rewriting, and e-graph-based equality saturation can legally
  consume Contract-derived knowledge.
- [ ] Investigate whether scalar, bitwise, and SIMD execution planning and selective materialization of intermediate
  data should be decided at integration time. Do not assume every SIMD candidate preserves Judgment order.
- [ ] Measure actual benefits from transformations that overlap with generic inlining, loop optimization, and escape
  analysis already performed effectively by the JVM JIT.

### 4.7. How Are Final Operation Methods Generated Alongside Kontrakt JVM Output?

- [ ] Treat direct generation of Kontrakt Contract execution code from JVM Plan/LIR as the Architecture Map baseline.
  Investigate when optimized user Operation methods can be emitted by this backend, transformed at Classfile level, or
  lowered by the Host Compiler after a verified Host-IR transformation.
- [ ] Guarantee class, package, and module boundary consistency, method descriptors, and ABI compatibility when using
  Host-generated Classfiles alongside Kontrakt-generated Classfiles.
- [ ] Verify whether integrated code generation can fully preserve the observable semantics of Java/Kotlin lowering.
  Otherwise retain Host Classfiles within a demonstrably safe scope.
- [ ] Treat `StackMapTable`, exception handlers, constant pools, bootstrap methods, `invokedynamic`, JVM verification,
  metadata preservation, and class-loader behavior as independent validation requirements.
- [ ] Distinguish JVM target version, JDK libraries, Kotlin runtime, Java Memory Model, GC, and JIT-dependent execution
  conditions as explicit optimization bases.

### 4.8. When Can Acquired Host Information Be Discarded?

- [ ] Identify the existing consumers that require Host source syntax, resolved Host meaning, Realization Body IR,
  source-to-JVM mappings, or Contract Execution IR. Separate material lifetimes without redefining established Protocol
  boundaries.
- [ ] Distinguish information replaceable by an analysis summary from information that must be reconstructed exactly. Do
  not treat physical retention of high-level Source IR as an objective in itself.
- [ ] Compare cases where early JVM lowering prevents subsequent optimizations with cases where early lowering reduces
  code-generation cost.
- [ ] Final generated code need not embed all original Source Meaning, but must retain runtime Judgments, Diagnostics,
  Failure Attribution, and required provenance.
- [ ] Do not confuse deferred physical materialization with deferred Establishment of Contract meaning.

### 4.9. How Is the Legality of Optimization Established?

- [ ] Prevent the original Realization's eligibility verification scope from being narrowed arbitrarily by optimization.
  Do not approve an original implementation solely because dead-code elimination removed a prohibited call.
- [ ] Preserve the legally observable order of Contract success and failure Judgments, First-Wins outcomes, unreached
  stages, State Transitions, Publication, and Diagnostics.
- [ ] Compare JVM exception ordering, null checks, class initialization, synchronization, object identity,
  floating-point and integer arithmetic, dynamic dispatch, and memory-model behavior before and after a transformation.
- [ ] Separate transformation legality proofs from profitability estimates. Profiling and runtime observations must not
  establish otherwise unproved Contract meaning.
- [ ] Design independent IR validation, transformation validation, reference paths, and differential tests. Keep
  verification independent enough that a validator need not reproduce the transformer's own defects.

### 4.10. How Are Analysis and Transformation Products Stored and Reused?

- [ ] Distinguish Contract Meaning Identity, Realization Code Identity, Binding Identity, Host Semantic Evidence Basis,
  and Target JVM Basis.
- [ ] Identify the smallest set of products that must be recomputed when only an Operation body changes and the Contract
  World remains unchanged.
- [ ] Conversely, determine whether Host Compilation and relevant Body IR can be reused when Contract Definitions change
  but the generated ABI and user methods remain unchanged.
- [ ] Define exact reuse conditions using Frozen/HID and producer-owned semantic projections. Do not establish
  Contract-level identity from byte equality or a matching hash alone.
- [ ] Verify full-recomputation reference paths and product reuse in V1, while retaining replaceable seams for different
  incremental scheduling and repair algorithms in V2.
- [ ] Manage analysis invalidation by IR generation and transformation. Do not reuse stale CFG, dominance, or alias
  summaries after structural changes.
- [ ] Track Host Compiler inputs, versions, IR extraction stage, synthetic/lowering changes, and exact generated
  Classfile identity as distinct validity dimensions where actually consumed.

## 5. Cost Model and Performance Verification

The best runtime performance is not necessarily produced by executing the greatest number of optimization passes.
Spending compilation time on transformations that the JVM JIT performs more effectively may increase build time and
generated code size. By contrast, propagating guarantees known only to the Contract World into user Operations can
provide static optimization evidence unavailable to a general Host Compiler or JIT.

All candidates must therefore be compared under the same Contract meaning, user Operations, and JVM environment. A
verification failure or semantic difference must never be counted as a performance improvement.

### 5.1. Costs to Measure Separately

- [ ] Measure the cold-build time spent separately in Contract Frontend processing, Establishment, Host Compilation,
  Realization Acquisition, closure verification, Whole-Machine analysis, and final code generation.
- [ ] In incremental builds, distinguish changes confined to the Contract, an Operation, a DI Binding, a JDK/Kotlin
  version, or the platform catalog.
- [ ] Record peak RSS, heap usage, FFM/off-heap memory, GC pauses, I/O, snapshot retention, and artifact size together.
- [ ] Measure JVM startup, latency of the first Interaction, warm-up, steady-state throughput, allocation rate, JIT
  compilation time, and code-cache effects.
- [ ] Examine the effect of code expansion, inlining, and specialized kernel calls on further optimization by JVM
  C2/Graal. Do not judge profitability from final Classfile size alone.

### 5.2. Implementations That Must Be Compared

Run the **same existing Realization IR → verification → Contract-aware Execution IR → optimization architecture** with
different acquisition and code-generation arrangements. Keep Contract meaning, original Realization eligibility
requirements, JVM target, and diagnostic observables identical. Report the marginal benefit of richer Host knowledge
rather than treating the existence of Contract-aware optimization as a new candidate.

**Reference A0:** Host Compiler Classfiles plus generated Contract machinery and exact Realization eligibility checking,
without elective cross-boundary transforms. This is an essential reference execution path.

**Current V1-oriented A1:** Classfile-derived Realization Body IR with existing Contract-aware analysis and selected
transformations. Measure reconstruction work and optimization reach.

**Acquisition B:** A1 plus targeted resolved Source-Semantic Evidence. Hold final code-generation policy fixed where
possible, so the benefit and cost of evidence can be isolated.

**Acquisition C:** Construct the same Kontrakt-owned Realization Body IR from high-level resolved Host meaning, with
exact JVM correspondence verified. Compare against B and A1 under equivalent transform and emission policies.

**Codegen variants D:** For applicable A1/B/C results, independently compare preservation of Host-generated methods,
selected Classfile rewriting, Kontrakt emission of selected methods, and supported Host-IR transformation followed by
Host backend lowering.

**Summary/materialization variants:** Compare summary-only selection, selective body opening, and complete body
materialization without changing semantic decisions. Do not assume that the full IR must be loaded to perform
Whole-Machine analysis.

All candidates must retain equivalent eligibility verification and Contract Judgments. Even when one candidate leads in
runtime performance, report combined compilation, memory, and artifact costs separately. A frequently rebuilt service
and a long-running deployment may favor different acquisition/emission combinations.

### 5.3. Test Workloads

- [ ] Establish the lower bound of optimization overhead using a simple numeric pipeline with mostly statically
  determined Judgments and a single Operation.
- [ ] Measure a case where a numeric range established by successful Admission is propagated through multiple calls and
  aliases to a branch inside an Operation.
- [ ] Verify that fusion does not compromise Diagnostics in pipelines with complex failure attribution and extensive
  cold paths.
- [ ] Analyze an Operation with multiple available implementations where DI explicitly selects one. Measure
  recompilation effects when the Binding changes.
- [ ] Test semantic mappings for Operations that use Kotlin inlining, coroutines, default arguments, value classes,
  generic erasure, and Java bridge methods.
- [ ] Compare collections of many independent elements, Unicode and numeric operations, bitsets, and candidate SIMD
  kernels.
- [ ] With large call graphs, compare compilation time and peak memory for summary-only analysis, selective body
  opening, and complete body materialization.
- [ ] Deliberately construct counterexamples in which optimization expands code and degrades JVM JIT performance.

## 6. Implementation Representations and Physical Boundaries

When Kontrakt acquires Host Compiler AST, FIR, or IR information to form the **existing Realization Body IR**, it must
not adopt those Host object graphs as the physical model of Contract HIR or long-lived compiler products. The required
structure should be convertible into exact semantic references separated from generation-local handles, packed opcodes,
indexed CFGs, def-use relations, call/effect tables, sparse/dense/chunked bitsets, and offset-based storage.

FFM slabs and primitive tables are baseline candidates for frequent lookup and analysis. Physical storage must not
determine Contract Identity. Layouts may let consumers access only the columns they require, and some cold material may
be physically materialized on demand. This does not permit meaning or Judgments required by the Contract to be omitted
merely for analytical convenience.

Separate the versioning, lifetime, and verification of the high-level IR acquisition protocol from physical storage
layout. Changes to internal Kotlin Compiler representations must not force changes to the Contract HIR protocol. Use the
already-defined producer/consumer boundaries across Source Evidence Acquisition, Realization Body IR publication,
summary consumption, and final JVM planning; specify only the new acquisition-specific projections and mapping evidence.
Consumption of these materials must follow ADR-0075's protocol-mediated consumption and validity rules.

## 7. External Systems to Study and Questions to Verify

The systems listed here are not templates to reproduce wholesale. Only techniques that can be verified and justified
independently should be adopted.

**LLVM LTO / ThinLTO.** Investigate the order of per-module summaries, the global index, function import, post-import
optimization, and code generation. Verify whether the assumptions carry over to Kontrakt when its inputs are JVM
Classfiles and Host Semantic Evidence rather than LLVM bitcode.

**Kotlin K2 and javac.** Investigate the material accessible after type resolution, the possibility of retaining or
exporting IR, the version stability of Compiler Plugin APIs, and the scope of lowering performed by existing JVM
backends. Do not assume that conventional compiler APIs provide persistent IR or integrated code-generation functions
that they do not actually expose.

**R8 / ProGuard family.** Verify the available call analysis, inlining, whole-program transformations, and artifact
generation using Classfiles that have already been lowered. Separate cases where source IR offers little additional
benefit from optimizations where loss of Source Meaning matters.

**MLIR Bufferization and multi-level lowering.** Investigate how values and memory representations can remain distinct
until analysis determines which buffers or materializations are required. Do not import MLIR dialects or object storage
models into the representation of Kontrakt Contract meaning.

**Graal Partial Evaluation, Escape Analysis, and HotSpot C2.** Compare interpreter, object, and dispatch overhead
removable through static execution contexts against code expansion caused by excessive specialization. Measure whether
Kontrakt gains anything by repeating optimizations the JVM already performs.

**E-graphs, equality saturation, and translation validation.** Investigate typed rewrites, preservation of effects,
equivalence that depends on path context, extractor cost models, and validation of transformations. As SSA cycles and
execution order can introduce soundness issues, generic merging of equivalent nodes must not be treated as authoritative
Contract identity.

Starting points for publicly available references:

- LLVM ThinLTO: https://clang.llvm.org/docs/ThinLTO.html
- LLVM Whole Program Devirtualization: https://llvm.org/docs/WholeProgramDevirt.html
- MLIR One-Shot
  Bufferization: https://mlir.llvm.org/docs/Bufferization/ , https://mlir.llvm.org/docs/Bufferization/OneShotAnalysis.html
- Kotlin Compiler Plugins: https://kotlinlang.org/docs/compiler-plugins.html
- javac Compiler API: https://docs.oracle.com/en/java/javase/25/docs/api/jdk.compiler/com/sun/source/util/JavacTask.html
- R8 Repository: https://r8.googlesource.com/r8/
- Graal Compiler: https://www.graalvm.org/latest/reference-manual/java/compiler/
- Alive2: https://github.com/AliveToolkit/alive2

The versions and actual capabilities of the referenced materials must be checked again during the corresponding research
task. The existence of a reference alone does not establish the feasibility of a particular Kontrakt integration method.

## 8. Decisions to Make Later

This TODO does **not** reopen the decision to have a Realization Body IR, Contract-aware Execution IR, Contract-aware
optimization, Whole-Machine summaries, or Kontrakt JVM backend. These are current Architecture Map responsibilities. The
unresolved design is **how the existing Realization IR receives and proves the necessary Host Operation meaning, and how
final user methods are emitted**. The following choices require evidence:

1. **Host semantic acquisition point.** For Java and Kotlin separately, identify the earliest point at which necessary
   names, types, calls, and operation semantics are resolved and the latest point before required distinctions
   disappear.
2. **Operation IR construction and fidelity.** Select Classfile-derived, Host-semantic-derived, or hybrid construction
   of the existing Realization Body IR, including separately retained evidence only when justified by its consumers and
   lifetime.
3. **Source-to-JVM correspondence.** Prove that original selected Operation semantics and the actual executable closure
   correspond across inlining, bridges, default methods/arguments, exception and initialization code, and generated
   helpers.
4. **Mandatory verification versus elective optimization.** Establish exactly which missing information must reject a
   realization and which merely prevents a transform. Optimization must not narrow original realization eligibility
   retroactively.
5. **Level of Host cooperation.** Compare read-only semantic extraction, selected Host IR transformation, and
   Kontrakt-only transformation. Do not conflate acquisition integration with ownership of final code generation.
6. **Emission unit and fallback.** Compare method/class/closure-level re-emission, verified Host Classfile pass-through,
   Bytecode rewriting, and supported Host backend reuse without violating JVM metadata or Contract observation.
7. **Marginal cost and reuse.** Measure duplicate analysis/compilation, retained IR size, extraction/invalidation costs,
   build performance, JIT behavior, and contract-specific execution gains across the selected arrangements.
8. **V1/V2 placement.** Decide which acquisition and correspondence obligations must close for supported V1
   realizations, and which caches, summaries, scheduling policies, and incremental repair strategies remain replaceable
   in V2.

For each decision, identify precisely whether a change concerns an existing Architecture Map design path, an
implementation choice, or a conflict that actually requires an ADR review. Record the prior Classfile-centered baseline,
execution and verification implications, and measured costs. Do not promote a research TODO into Contract law.

## 9. Research Sequence and Completion Criteria

### P0 — Verify the Existing Design and Isolate the Actual Gap

- [ ] Trace Architecture Map §§11–16, 22–34 and 50–52, ADR-0046, ADR-0063, ADR-0071, ADR-0073, and ADR-0075. Mark
  existing Realization Body IR, execution analysis, Whole-Machine work, and codegen ownership as **already designed**,
  not new choices.
- [ ] With actual Java/Kotlin Operation examples, inventory meaning present in Host resolution and meaning preserved or
  lost in final Classfiles, including the exact support surfaces of the existing §12 IR.
- [ ] Construct an exact mapping from selected DI Binding to Source Operation, Host compiler-resolved target, generated
  JVM method (s), and actual runtime reachability.
- [ ] Separate missing knowledge that blocks required Original Realization Admission from knowledge that would merely
  improve Contract-aware optimization.

**Completion criterion:** A precise gap inventory shows what the existing Classfile-derived Realization IR can already
support, where Host evidence changes verification or optimization, and which established designs require no change.

### P1 — Demonstrate Alternative Inputs to the Same Realization IR

- [ ] Acquire at least one valid Java and Kotlin Operation after Host semantic resolution without separately re-parsing
  or re-type-checking the complete Host language, then translate it into the current §12 Kontrakt Realization Body IR
  model.
- [ ] Compare Classfile reconstruction, targeted Source Evidence augmentation, and high-level Host-derived construction
  for the **same** Operation and the **same** requested Realization IR observations.
- [ ] Verify Host-to-JVM correspondence for inlining, generics, virtual dispatch, exception flow, initialization,
  default arguments, synthetic methods, value classes, and coroutine lowering where supported.
- [ ] Measure time, allocation, memory, persistence size, and Host Compiler API/version instability for each acquisition
  path.

**Completion criterion:** Reproducible tests show that one or more Host acquisition points can safely populate or
augment the existing Realization IR and identify where a final executable verification path is still required.

### P2 — Quantify Additional Contract-Aware Optimization, Not Re-invent It

- [ ] Run the Architecture Map's existing Contract-aware Analysis → Execution Formation → Execution IR → Optimization
  path using each acquired Realization IR variant.
- [ ] Verify propagation of a successful runtime-path Contract guarantee to the exactly related Operation SSA value;
  eliminate an unreachable branch only when application, dominance, aliasing, and failure observables permit it.
- [ ] Compare selected Carrier/Fact physical-materialization elimination, exact-call specialization, and cross-boundary
  scalarization as separate tests.
- [ ] Independently compare original realization eligibility, failure order, State, Publication, Diagnostics, and final
  JVM execution behavior. Do not infer eligibility from optimized-away illegal constructs.

**Completion criterion:** Attribute each additional legal transformation and its cost to the specific Host information
gained, without crediting the new acquisition path for optimization features the Architecture Map already defines.

### P3 — Compare Final Emission, Performance, and Product Lifetimes

- [ ] With the acquisition path held constant where possible, compare original Host Method pass-through, selected
  Classfile rewrite, Kontrakt code emission, and supported Host Backend codegen after verified IR transformation.
- [ ] Measure cold and incremental build time, execution latency, warm JVM throughput, JIT behavior, code size, peak
  memory, and duplicate Host/Kontrakt semantic work.
- [ ] Compare opening Whole-Machine summaries, selected method bodies, and full IR materialization without altering
  analysis scope or Contract meaning.
- [ ] Measure Frozen/HID result reuse and changes to Host inputs, generated ABI, Operation body, DI Binding, compiler
  versions, and Contract World separately.
- [ ] Find counterexamples where retained Host Classfiles or omitted transformations are faster; make fallback decisions
  explicit.

**Completion criterion:** Identify useful acquisition/emission combinations using measured costs and exact verification
coverage rather than maximizing retained IR by default.

### P4 — Prepare a Targeted Architecture-Map Revision

- [ ] Compare acquisition A/B/C and emission D variants independently; preserve the existing Whole-Machine compiler
  topology unless evidence identifies a specific defect.
- [ ] Show how the chosen acquisition proves ADR-0073 Source-Semantic and actual JVM closure requirements, together with
  ADR-0075 validity and consumption constraints.
- [ ] Identify the **specific** Architecture Map §§11–12, Host integration, verification mapping, and JVM backend
  details that need revision; distinguish new design choices from already-established responsibilities.
- [ ] Document rejected paths, unsupported Host versions/features, safe fallback scopes, and conditions for
  reconsideration.

**Completion criterion:** The result is a precise choice of inputs and generation strategy for the existing Realization
IR, not a second definition of the Kontrakt compiler architecture. No Host plugin, IR format, or codegen owner is
mandated without verification and measurement.

## 10. Matters Not Decided at This Stage

The existence and purpose of Kontrakt's Realization Body IR, Contract-aware analysis, Contract-Aware Execution IR,
Whole-Machine summaries, and JVM backend are **existing working architecture**, not open choices created by this TODO.
Their semantics and ownership remain constrained by the Accepted ADRs and the Architecture Map's design status.

This TODO does not select Kotlin Compiler Plugins, `javac` Plugins, a durable Host IR export protocol, a separate Source
Parser, mandatory high-level IR for every dependency, the level of SSA, e-graph usage, full Host-method regeneration, or
Host Compiler versus Kontrakt emission for every transformed Operation. It does not assert that higher-level acquisition
is universally faster or more complete than Classfile analysis.

The Host-to-Kontrakt information boundary is primarily the user Operation Realization boundary. Operation does not gain
Contract authority or become aware of all 1D contracts. Kontrakt's analysis uses valid material from both sides.
Source-semantic meaning acquired early and actual JVM executable meaning verified later must not be confused. A compiler
optimization may remove redundant physical work but must not erase a required runtime Occurrence judgment or change
Contract failure and output observables.

**The current research priority is to improve or replace the Classfile-only input to the already-planned Realization
Body IR where doing so provides provable verification coverage or measurable optimization value.** Retain the existing
Classfile path as a supported comparison and possible fallback where eligibility is provable. Revise only the
acquisition, correspondence, and emission portions of the current design that evidence demonstrates must change.

---

## Related Documents

- `What Contract Is` — Highest authority for Contract meaning.
- `0046-idl-first-interface-contract-frontend-1d-catalog-backend-discipline.md` — Generated Operation Surface and the
  user Realization boundary.
- `0063-contract-establishment-occurrence-applicability-and-semantic-dependency.md` — Establishment, Occurrences, and
  semantic dependency.
- `0071-resolved-contract-hir-semantic-boundary-deterministic-publication-lifecycle-and-reuse.md` — Resolved HIR
  meaning, retention, and consumption rules.
- `0073-jvm-platform-native-surface-contract-preservation-and-external-technology-boundary.md` — Source-Semantic
  Surface, platform use, closure, and preservation.
-
`0075-compiler-result-ownership-legal-consumption-deterministic-realization-and-contract-implementation-separation.md` —
Compiler product consumption and reuse boundaries.
- `kontrakt-compiler-total-architecture-map-design-draft.md` — Existing working baseline; especially §§11–16
  (Realization Body IR and verification), §§22–34 (Execution IR, Whole-Machine analysis, JVM output), and §§50–52
  (V1/V2/open decisions).
- `kontrakt-contract-aware-realization-optimization-todo.md` — Existing TODO on Contract-aware optimization.
- `kontrakt-v2-incremental-architecture-research-todo.md` — Incremental architecture candidates and replaceable V1
  foundations.

All examples and structures in this document are provided for design review. They do not replace the legal Judgments or
sequencing defined by existing ADRs.