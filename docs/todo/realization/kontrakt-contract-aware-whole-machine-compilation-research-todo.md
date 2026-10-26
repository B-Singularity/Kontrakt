# Kontrakt Contract-Aware Whole-Machine Compilation Research TODO

## Status and Scope

**Research and design-review TODO. This document is not an ADR and does not establish new Contract semantics or compiler architecture decisions.**

This document records the research required to preserve independent compilation of Kontrakt Contracts and Java/Kotlin user Operations while making full use of Contract-derived optimization opportunities when the two results are combined. Implementation difficulty is not grounds for excluding an architectural candidate in advance. Equally, the document does not declare high-level IR integration superior in measured performance before the relevant experiments have been conducted.

`What Contract Is` and the applicable Accepted ADRs take precedence. `kontrakt-compiler-total-architecture-map-design-draft.md` remains the current working baseline for the compiler architecture, and `kontrakt-contract-aware-realization-optimization-todo.md` remains the existing TODO for Contract-aware optimization. This document neither replaces those documents nor reinterprets their existing provisions. It addresses the design questions that remain open concerning **Host Compiler cooperation, cross-boundary optimization, and final JVM Classfile generation**.

Section 11 of the current Architecture Map identifies Classfile-based Realization Acquisition as the baseline V1 path. This research does not decide to remove that path. The additional candidate to investigate is the acquisition, to the extent required, of Operation semantics after the Host Compiler has resolved names, types, and calls but before final JVM lowering. The information available through each path, its verification capability, runtime performance, and compilation cost must be compared before a decision is made.

## 1. Assumptions to Preserve

Kontrakt forms Contract candidates from `.kontrakt` declarations and selected 1D Carriers, builds Resolved Contract HIR, and obtains the Canonical Contract World through authority-owned Establishment. Contract meaning and identity are not reconstructed from user implementation classes or JVM Classfiles.

A 1D Carrier is a frontend authoring representation. `javac` or `kotlinc` may be used to check its syntax or types, but the resulting Host Classfile does not become 1D Contract authority. The cost of Host Compilation for small Carriers is something to measure; it is not, by itself, a reason to implement a new Java/Kotlin semantic analyzer first. Kontrakt's backend can lower the generated execution code for the 1D pipeline directly into Classfiles.

A user Operation is subject to the ordinary Java/Kotlin type checks, overload resolution, and language-semantic analysis. Its execution is a separate Realization, and Kontrakt is responsible for verifying the eligibility of the actual execution paths of the selected implementation. Selection of an implementation by external Composition or DI remains separate from Contract Definition and 1D Selection.

The JVM loads, verifies, and executes final Classfiles. The production of a Classfile by `javac` does not mean the user program has already been executed once by the JVM. Running the Kontrakt compiler itself in a JVM process is also distinct from running the final user program in a JVM. Intermediate analysis or regeneration may constitute **additional compilation work**, but it does not cause **the same user program to pass through JVM execution twice**.

The optimization direction agreed so far separates what can be established from the Contract alone from what requires combining it with a user Operation. Contract-only analysis should not wait for final integration when its work can already be completed. Conversely, optimizations inside an Operation must not be decided prematurely without establishing the actual value and call relationships.

## 2. The Problem to Resolve

```text
Contract Authoring                     Java / Kotlin Operation Source
       │                                          │
       ▼                                          ▼
Kontrakt Frontend                         Host Compiler
       │                                          │
       ▼                                          ▼
Resolved Contract HIR                Resolved Host Meaning
       │                                          │
       ▼                                 ┌────────┴─────────┐
Authority-Owned Establishment            │                  │
       │                          High-Level IR       JVM Classfile
       ▼                                 │                  │
Canonical Contract World                 └────────┬─────────┘
       │                                          │
       └────────────────┬─────────────────────────┘
                        ▼
           Contract-Aware Whole-Machine Work
                        │
         Verification · Joint Analysis · Optimization
                        │
                        ▼
          JVM Planning / Final Classfiles
                        │
                        ▼
                       JVM
```

This diagram illustrates possible information flows. It does not require both forms of Host data to be produced or read in every compilation. The exact acquisition points, the range of high-level semantics to retain, the owner of final Classfile generation, and the conditions under which existing Classfiles remain unchanged have not yet been decided.

The questions are concrete. Can a JVM Classfile already produced by the Host Compiler provide sufficient evidence to verify the execution structure? Which Source-Semantic Operations and type relationships must be acquired before that point? When these are combined with Contract-side execution IR, how much more of the Contract's expressive power and how much actual performance can the compiler exploit? Which compiler products and analysis boundaries will allow final integration to perform more than packaging without recompiling the entire program redundantly?

Maximizing the set of possible optimizations is not the same as achieving the fastest build in every case. This TODO frames the objective as **maximizing the range of legal optimizations enabled by Contract meaning, while measuring execution performance, compilation time, and JVM JIT behavior to select the actual realization strategy**.

## 3. Architectures to Compare

### 3.1. A — Classfile-Based Integration and Optimization

The Host Compiler generates Classfiles for user Operations in the ordinary way. Kontrakt analyzes actual methods, CFGs, calls, and effect relationships, then combines that analysis with Contract Execution IR. Only methods worth transforming are modified; other Classfiles are retained after verification.

This path is close to the V1 baseline in the current Architecture Map. It allows direct analysis of actual JVM execution preserved by Classfiles and does not require tight integration with the Host Compiler. However, some Source Type relationships, Kotlin-specific high-level distinctions, and value provenance prior to lowering may be difficult to recover. Contract-only optimization and post-integration Bytecode optimization remain possible, but the full range of transformations that depend on original high-level relationships cannot be guaranteed.

**Research task:** Establish experimentally which facts can and cannot be proved from Classfiles alone for each supported Operation surface. Distinguish information needed only to estimate optimization profitability from information required to prove execution eligibility.

### 3.2. B — Classfiles Combined with Separate Source Semantic Evidence

Acquire the required Source Operations, types, call sites, and mappings to eventual JVM targets from the Host Compiler's resolved semantics. Use Classfiles for analysis of actual execution, and consume additional evidence only where a required distinction has been lost.

Source Evidence is compiler material verified by Kontrakt, not Contract authority. Evidence cannot be used if the Host Compiler version, inputs, or build conditions under which it was produced cannot be matched to the final Classfile.

**Research task:** Verify the generation and validation costs of this evidence, the precision of its mappings, the actual acquisition points in Kotlin FIR/IR and javac attribution, and version dependencies. Do not use this method as a default correctness basis unless the required completeness and trustworthiness can be established.

### 3.3. C — Retained High-Level Operation IR with Whole-Machine Integration

After the Host Compiler has resolved the user language's types, symbols, and overloads, Kontrakt retains the necessary Operation structure before final JVM lowering in an independently owned Realization IR. Contract Execution IR and the verified Realization are consumed in a common optimization context to analyze value relationships, effects, aliasing, call targets, and execution context. Final JVM code generation follows the results of cross-boundary optimization.

This is a high-priority research candidate. It can use Contract-established ranges and identities, provenance from actual Facts to Operation parameters, and high-level control- and data-flow relationships together. It must nevertheless be demonstrated that the required information can be acquired and that the observable semantics of actual JVM execution can be preserved. Acquiring high-level IR does not automatically verify synthetic methods, bridges, class initialization, exception paths, or synchronization introduced by JVM lowering.

**Research task:** Identify the appropriate point of acquisition for Kotlin and Java separately, determine how to trust or verify Host transformations already performed, and decide how the owner of final class generation could be selected. Compare the additional high-level optimization opportunities with the Classfile-based path.

### 3.4. D — Selective Integrated Code Generation with Classfile Pass-Through

Even where high-level IR is available, Kontrakt need not regenerate every method. Select only those functions or regions where optimization in the joint Contract context is advantageous for integrated code generation. Use the Host Compiler's Classfiles unchanged, or apply limited transformations, for the remaining code.

This candidate combines the information advantage of C with the existing execution path of A. However, replacing even some methods in one class requires preservation of ABI, internal access rules, class initialization, `StackMapTable`, exception tables, debugging and metadata, and JVM linkage. Kotlin metadata or synthetic methods must not become inconsistent with other parts of the final artifact.

**Research task:** Compare method, class, closure, and caller/callee sets as regeneration units. Retain multiversion generation as a candidate where a specialized call path and existing Host code both need to remain available.

### 3.5. Secondary Alternative — Owning the Complete Semantics of Host Languages

For comparison, do not categorically exclude reimplementing Java/Kotlin name resolution, type checking, and all language lowering in Kontrakt. However, demonstrate which new optimization capabilities would actually be gained by repeating semantic work that Host Compilers already perform correctly. This is not a selected baseline; it must neither be rejected merely because it is difficult to implement nor adopted merely because it appears maximally independent.

## 4. Technical Questions to Resolve Before a Decision

### 4.1. Which Semantics Belong to the Host Compiler?

- [ ] Identify the information Kontrakt actually consumes from Java overload resolution, generic types, capture conversion, boxing, bridge methods, default methods, and results after annotation processing.
- [ ] Analyze the correspondence between Kotlin K2 FIR resolution, backend IR, inlining, value classes, default arguments, coroutine and suspend lowering, smart casts, extension calls, and actual JVM methods.
- [ ] Classify cases that require both source-level semantics and final Bytecode semantics. Assign ownership so that the same meaning is not redundantly stored in two representations.
- [ ] Determine whether a common evidence format can be interpreted differently across `javac` or `kotlinc` versions, and specify the versioned basis needed to detect such differences.
- [ ] If Host Compiler APIs are used, evaluate read-only acquisition of semantics separately from participation in IR transformation or code generation.

### 4.2. When and in What Form Is Operation IR Acquired?

- [ ] Specify the exact supported scope and preserved semantics of pre-JVM-lowering acquisition, reconstruction from Classfiles, and hybrid acquisition.
- [ ] Investigate how to transfer exact references, CFGs, value relations, call targets, and effects into Kontrakt-owned primitive representations without retaining Kotlin FIR/IR objects or javac AST objects as long-lived compiler products.
- [ ] When both high-level IR and final Classfiles are acquired, design a procedure to validate exact mappings from Source Operation to Host IR value or call to JVM method or instruction.
- [ ] Define the immutable evidence and verified boundary used to represent execution semantics for methods that will not be regenerated.
- [ ] Determine how to support or reject user libraries, older Classfiles, and external dependencies for which high-level IR is unavailable or only partially available.

### 4.3. To Which Values and Paths Do Contract Guarantees Apply?

- [ ] Distinguish Established Definition Meaning from guarantees of runtime Occurrence Judgments, and identify the exact producer-owned material that a compiler consumer may use.
- [ ] Explicitly trace which Facts become Established on successful Input, Admission, and Lowering paths and which fields are transferred to particular Operation parameters or SSA values.
- [ ] Distinguish Policy World, Contract Version, Governance Binding, State Surface, and Implementation Binding conditions fixed before compilation from those established only at runtime.
- [ ] Preserve application context and dominance so that a range or nonzero condition true only on a particular branch cannot leak into another path, Occurrence, or implementation.
- [ ] Do not infer identical values, Facts, or Contract guarantees solely from equal types or names. Specify the required identity, coordinate, and attribution proofs.

### 4.4. How Are Realization Admission and DI Connected?

- [ ] Determine how external Composition explicitly selects the exact implementation when multiple user implementations satisfy the same Operation Contract.
- [ ] Ensure that an unselected implementation does not become an execution candidate or change Contract meaning merely because it is on the Classpath.
- [ ] Verify the selected method's exact JVM target, possible virtual call targets, class initialization, injected fields, and helper closure.
- [ ] Avoid unnecessary regeneration of the Contract World after a Binding change, while invalidating verification and optimization products that depend on that Binding.
- [ ] Decide whether runtime implementation replacement or dynamic providers are supported. If so, distinguish preverified alternative execution paths from the range of optimizations valid for each.

### 4.5. How Far Does Whole-Machine Analysis Extend?

- [ ] Design module-, interface-, operation-, method-, and region-level summaries and selective body opening without assuming that Whole-Machine analysis requires every body to be merged into one enormous IR.
- [ ] Separate the roles of verification summaries and optimization summaries, reusing only foundational material that may legitimately be shared.
- [ ] Identify the producers and consumers of Contract-only, Realization-only, and cross-boundary analyses. Do not let summaries become Contract authority or independent arbiters of meaning.
- [ ] Distinguish the precision required for deterministic closure verification from additional call-graph, alias, or effect analysis performed only to improve optimization profitability.
- [ ] Confirm that relations across multiple Interfaces and Cores are consumed only through explicitly permitted bases. Whole-Machine analysis must not introduce hidden Contract dependencies.

### 4.6. Which Cross-Boundary Optimizations Are Actually Beneficial?

- [ ] Specify the conditions under which Contract-derived constant and range propagation can eliminate unreachable branches or redundant computation inside Operations.
- [ ] Investigate direct calls and specialization based on exact Operation Bindings and call-target sets. Do not arbitrarily close JVM virtual dispatch that remains open.
- [ ] Investigate physical fusion across 1D pipelines and Operations. Preserve the order, reachedness, failure attribution, and subsequent Publication meaning of logical Judgments.
- [ ] Before eliminating physical Fact or Carrier objects, verify equality, observable identity, aliasing, escape, class initialization, exception, and synchronization semantics.
- [ ] Investigate how typed SSA, CFG analysis, conditional rewriting, and e-graph-based equality saturation can legally consume Contract-derived knowledge.
- [ ] Investigate whether scalar, bitwise, and SIMD execution planning and selective materialization of intermediate data should be decided at integration time. Do not assume every SIMD candidate preserves Judgment order.
- [ ] Measure actual benefits from transformations that overlap with generic inlining, loop optimization, and escape analysis already performed effectively by the JVM JIT.

### 4.7. Who Generates Final JVM Classfiles, and for Which Scope?

- [ ] Preserve direct generation of Kontrakt Contract execution code from JVM Plan/LIR, while analyzing whether the same backend can handle user methods optimized through integration.
- [ ] Guarantee class, package, and module boundary consistency, method descriptors, and ABI compatibility when using Host-generated Classfiles alongside Kontrakt-generated Classfiles.
- [ ] Verify whether integrated code generation can fully preserve the observable semantics of Java/Kotlin lowering. Otherwise retain Host Classfiles within a demonstrably safe scope.
- [ ] Treat `StackMapTable`, exception handlers, constant pools, bootstrap methods, `invokedynamic`, JVM verification, metadata preservation, and class-loader behavior as independent validation requirements.
- [ ] Distinguish JVM target version, JDK libraries, Kotlin runtime, Java Memory Model, GC, and JIT-dependent execution conditions as explicit optimization bases.

### 4.8. When May Information Be Discarded?

- [ ] Identify the consumers that still require Host source syntax, resolved Host meaning, Operation Body IR, JVM mappings, or Contract Execution IR.
- [ ] Distinguish information replaceable by an analysis summary from information that must be reconstructed exactly. Do not treat physical retention of high-level Source IR as an objective in itself.
- [ ] Compare cases where early JVM lowering prevents subsequent optimizations with cases where early lowering reduces code-generation cost.
- [ ] Final generated code need not embed all original Source Meaning, but must retain runtime Judgments, Diagnostics, Failure Attribution, and required provenance.
- [ ] Do not confuse deferred physical materialization with deferred Establishment of Contract meaning.

### 4.9. How Is the Legality of Optimization Established?

- [ ] Prevent the original Realization's eligibility verification scope from being narrowed arbitrarily by optimization. Do not approve an original implementation solely because dead-code elimination removed a prohibited call.
- [ ] Preserve the legally observable order of Contract success and failure Judgments, First-Wins outcomes, unreached stages, State Transitions, Publication, and Diagnostics.
- [ ] Compare JVM exception ordering, null checks, class initialization, synchronization, object identity, floating-point and integer arithmetic, dynamic dispatch, and memory-model behavior before and after a transformation.
- [ ] Separate transformation legality proofs from profitability estimates. Profiling and runtime observations must not establish otherwise unproved Contract meaning.
- [ ] Design independent IR validation, transformation validation, reference paths, and differential tests. Keep verification independent enough that a validator need not reproduce the transformer's own defects.

### 4.10. How Are Analysis and Transformation Products Stored and Reused?

- [ ] Distinguish Contract Meaning Identity, Realization Code Identity, Binding Identity, Host Semantic Evidence Basis, and Target JVM Basis.
- [ ] Identify the smallest set of products that must be recomputed when only an Operation body changes and the Contract World remains unchanged.
- [ ] Conversely, determine whether Host Compilation and relevant Body IR can be reused when Contract Definitions change but the generated ABI and user methods remain unchanged.
- [ ] Define exact reuse conditions using Frozen/HID and producer-owned semantic projections. Do not establish Contract-level identity from byte equality or a matching hash alone.
- [ ] Verify full-recomputation reference paths and product reuse in V1, while retaining replaceable seams for different incremental scheduling and repair algorithms in V2.
- [ ] Manage analysis invalidation by IR generation and transformation. Do not reuse stale CFG, dominance, or alias summaries after structural changes.

## 5. Cost Model and Performance Verification

The best runtime performance is not necessarily produced by executing the greatest number of optimization passes. Spending compilation time on transformations that the JVM JIT performs more effectively may increase build time and generated code size. By contrast, propagating guarantees known only to the Contract World into user Operations can provide static optimization evidence unavailable to a general Host Compiler or JIT.

All candidates must therefore be compared under the same Contract meaning, user Operations, and JVM environment. A verification failure or semantic difference must never be counted as a performance improvement.

### 5.1. Costs to Measure Separately

- [ ] Measure the cold-build time spent separately in Contract Frontend processing, Establishment, Host Compilation, Realization Acquisition, closure verification, Whole-Machine analysis, and final code generation.
- [ ] In incremental builds, distinguish changes confined to the Contract, an Operation, a DI Binding, a JDK/Kotlin version, or the platform catalog.
- [ ] Record peak RSS, heap usage, FFM/off-heap memory, GC pauses, I/O, snapshot retention, and artifact size together.
- [ ] Measure JVM startup, latency of the first Interaction, warm-up, steady-state throughput, allocation rate, JIT compilation time, and code-cache effects.
- [ ] Examine the effect of code expansion, inlining, and specialized kernel calls on further optimization by JVM C2/Graal. Do not judge profitability from final Classfile size alone.

### 5.2. Implementations That Must Be Compared

**Baseline A:** Combine the Host Compiler's original Classfiles with minimally generated Contract execution code. Do not omit Contract eligibility verification; omit only cross-boundary transformation.

**Baseline B:** Perform Classfile-based Realization analysis and Contract-aware optimization. Transform only the methods that require Bytecode-level changes.

**Candidate C:** Acquire high-level semantics separately from the Host Compiler and combine them with Contract Execution IR. Perform integrated code generation for selected methods after optimization.

**Candidate D:** Use global summaries to choose optimization targets, apply high-level integration and integrated code generation only to selected regions, and retain the remaining original Classfiles.

All candidates must retain equivalent eligibility verification and Contract Judgments. Even when one candidate leads in runtime performance, report the combined costs of compilation time and memory separately for each usage scenario. A server build with frequently changing Contract contexts and a long-running service compiled once may favor different candidates.

### 5.3. Test Workloads

- [ ] Establish the lower bound of optimization overhead using a simple numeric pipeline with mostly statically determined Judgments and a single Operation.
- [ ] Measure a case where a numeric range established by successful Admission is propagated through multiple calls and aliases to a branch inside an Operation.
- [ ] Verify that fusion does not compromise Diagnostics in pipelines with complex failure attribution and extensive cold paths.
- [ ] Analyze an Operation with multiple available implementations where DI explicitly selects one. Measure recompilation effects when the Binding changes.
- [ ] Test semantic mappings for Operations that use Kotlin inlining, coroutines, default arguments, value classes, generic erasure, and Java bridge methods.
- [ ] Compare collections of many independent elements, Unicode and numeric operations, bitsets, and candidate SIMD kernels.
- [ ] With large call graphs, compare compilation time and peak memory for summary-only analysis, selective body opening, and complete body materialization.
- [ ] Deliberately construct counterexamples in which optimization expands code and degrades JVM JIT performance.

## 6. Implementation Representations and Physical Boundaries

Even when Kontrakt uses Host Compiler AST, FIR, or IR information, it must not adopt those object graphs as the physical model of Contract HIR or long-lived compiler products. The required structure should be convertible into exact semantic references separated from generation-local handles, packed opcodes, indexed CFGs, def-use relations, call/effect tables, sparse/dense/chunked bitsets, and offset-based storage.

FFM slabs and primitive tables are baseline candidates for frequent lookup and analysis. Physical storage must not determine Contract Identity. Layouts may let consumers access only the columns they require, and some cold material may be physically materialized on demand. This does not permit meaning or Judgments required by the Contract to be omitted merely for analytical convenience.

Separate the versioning, lifetime, and verification of the high-level IR acquisition protocol from physical storage layout. Changes to internal Kotlin Compiler representations must not force changes to the Contract HIR protocol. Define exact producers and consumers across Source Evidence Acquisition, Realization Body IR publication, summary consumption, and final JVM planning. Consumption of these materials must follow ADR-0075's protocol-mediated consumption and validity rules.

## 7. External Systems to Study and Questions to Verify

The systems listed here are not templates to reproduce wholesale. Only techniques that can be verified and justified independently should be adopted.

**LLVM LTO / ThinLTO.** Investigate the order of per-module summaries, the global index, function import, post-import optimization, and code generation. Verify whether the assumptions carry over to Kontrakt when its inputs are JVM Classfiles and Host Semantic Evidence rather than LLVM bitcode.

**Kotlin K2 and javac.** Investigate the material accessible after type resolution, the possibility of retaining or exporting IR, the version stability of Compiler Plugin APIs, and the scope of lowering performed by existing JVM backends. Do not assume that conventional compiler APIs provide persistent IR or integrated code-generation functions that they do not actually expose.

**R8 / ProGuard family.** Verify the available call analysis, inlining, whole-program transformations, and artifact generation using Classfiles that have already been lowered. Separate cases where source IR offers little additional benefit from optimizations where loss of Source Meaning matters.

**MLIR Bufferization and multi-level lowering.** Investigate how values and memory representations can remain distinct until analysis determines which buffers or materializations are required. Do not import MLIR dialects or object storage models into the representation of Kontrakt Contract meaning.

**Graal Partial Evaluation, Escape Analysis, and HotSpot C2.** Compare interpreter, object, and dispatch overhead removable through static execution contexts against code expansion caused by excessive specialization. Measure whether Kontrakt gains anything by repeating optimizations the JVM already performs.

**E-graphs, equality saturation, and translation validation.** Investigate typed rewrites, preservation of effects, equivalence that depends on path context, extractor cost models, and validation of transformations. As SSA cycles and execution order can introduce soundness issues, generic merging of equivalent nodes must not be treated as authoritative Contract identity.

Starting points for publicly available references:

- LLVM ThinLTO: https://clang.llvm.org/docs/ThinLTO.html
- LLVM Whole Program Devirtualization: https://llvm.org/docs/WholeProgramDevirt.html
- MLIR One-Shot Bufferization: https://mlir.llvm.org/docs/Bufferization/ , https://mlir.llvm.org/docs/Bufferization/OneShotAnalysis.html
- Kotlin Compiler Plugins: https://kotlinlang.org/docs/compiler-plugins.html
- javac Compiler API: https://docs.oracle.com/en/java/javase/25/docs/api/jdk.compiler/com/sun/source/util/JavacTask.html
- R8 Repository: https://r8.googlesource.com/r8/
- Graal Compiler: https://www.graalvm.org/latest/reference-manual/java/compiler/
- Alive2: https://github.com/AliveToolkit/alive2

The versions and actual capabilities of the referenced materials must be checked again during the corresponding research task. The existence of a reference alone does not establish the feasibility of a particular Kontrakt integration method.

## 8. Decisions to Make Later

This document records **acquisition of high-level Operation IR and its integration with Contract information as a leading research candidate**. Neither its implementation method nor its scope of adoption has been decided. The following decisions require distinct evidence.

1. **Acquisition of Operation meaning.** Decide whether to use only Classfiles, acquire additional Source Evidence, or obtain Host Compiler high-level IR as a required or optional input.
2. **Depth of Host Compiler cooperation.** Decide whether integration stops at reading semantic information, extends to selected IR transformations, or gives Kontrakt ownership of code generation for selected methods.
3. **Optimization granularity.** Determine the scope of combined analysis across Contract pipelines and Operations, and the actual units on which transformations may operate.
4. **Retained material and consumption protocols.** Specify the exact fields transferred among Source Semantic Mapping, Realization IR, Contract Execution IR, summaries, and final JVM artifacts.
5. **Fallback and supported scope.** Decide whether a Classfile-based path can be used for code whose eligibility can still be proved without high-level material. Do not approve code for performance reasons when mandatory verification is impossible.
6. **Selection criteria for compilation and execution performance.** Define a cost model and measurements to select integrated code generation for workloads where it is advantageous and preserve existing Classfiles elsewhere.
7. **V1 and V2 scope.** Separate the protocols and verification boundaries that must be closed in V1 from analysis, scheduling, and incremental repair strategies replaceable in V2. Do not defer every advanced optimization to V2 by default.

For each decision, record the difference from retaining the current Architecture Map's Classfile-based V1 path, the exact ADR or design sections that would need to change, the impact on execution semantics and verification, and measured compilation and runtime costs. Treat implementation difficulty only as the final comparison criterion.

## 9. Research Sequence and Completion Criteria

### P0 — Verify Semantic and Realization Boundaries

- [ ] Reconcile the separation of Contract Authority and Realization Acquisition against ADR-0046, ADR-0063, ADR-0071, ADR-0073, ADR-0075, and the current Architecture Map.
- [ ] Use concrete Java/Kotlin Operation examples to identify required Source Semantic information and information retained by JVM Classfiles.
- [ ] Construct a minimal example with exact mappings among selected DI Binding, Source Operation, Host Method, and JVM call target.

**Completion criterion:** It must be possible to explain which verifications are sufficiently supported by Classfiles and which cannot be established without high-level evidence.

### P1 — Demonstrate the Acquisition Paths

- [ ] Acquire at least one valid Operation in each of Kotlin and Java at the Host Compiler's semantic resolution stage and transfer it into a Kontrakt primitive-based Realization IR.
- [ ] Compare the result with Classfile-based Body IR analysis of the same Operation.
- [ ] Validate correspondence and information loss across inlining, generics, virtual dispatch, exceptions, and constructors or initialization.

**Completion criterion:** Reproducible tests must demonstrate whether high-level IR acquisition is feasible and how its information corresponds to actual JVM execution.

### P2 — Demonstrate Minimal Cross-Boundary Optimizations

- [ ] Implement a Contract-only baseline, Classfile-based integrated optimization, and high-level-IR-based integrated optimization using identical Contract and Operation examples.
- [ ] Verify a transformation that propagates a range from a successful Judgment to an Operation SSA value and removes an unreachable branch.
- [ ] Verify elimination of physical intermediate Carrier materialization and direct-call transformation as separate cases.
- [ ] Independently compare failure order, State, Publication, Diagnostics, and JVM execution semantics.

**Completion criterion:** Identify additional optimizations enabled specifically by high-level integration and provide verifiable preservation evidence for at least one transformation.

### P3 — Compare Performance and Material Lifetimes

- [ ] Measure compilation time, execution latency, warm-JVM throughput, JIT behavior, code size, and peak memory.
- [ ] Compare opening only Whole-Machine summaries, opening selected method bodies, and physically materializing the entire IR.
- [ ] Measure Frozen Product reuse and rebuild costs for different scopes of change.
- [ ] Find counterexamples where omitting an optimization is faster and design a cost-based fallback.

**Completion criterion:** Explain which path is appropriate using measured build, deployment, and execution costs, not merely theoretical optimization potential.

### P4 — Prepare the Architectural Decision

- [ ] Compare the verified results for candidates A, B, C, and D and present a final candidate.
- [ ] Demonstrate how the selected approach meets ADR-0073's requirements for Source-Semantic distinctions and execution closure.
- [ ] Identify precisely which parts of the existing Architecture Map, Realization Optimization TODO, and JVM Backend design would need revision.
- [ ] Document why rejected candidates failed and the conditions under which they should be reconsidered.

**Completion criterion:** Do not mandate a particular Host Compiler Plugin, IR format, whole-program recompilation scheme, or custom Java/Kotlin compiler without empirical validation. The decision must be explainable without changing established Contract meaning.

## 10. Matters Not Decided at This Stage

This TODO does not decide to adopt Kotlin Compiler Plugins or javac Plugins. It also does not fix the extent of Host Compiler integration, the role of a separate source parser, the storage format of high-level IR, the introduction stage of SSA, the scope of e-graph usage, code-generation ownership, or policies for regenerating individual methods.

Whole-Machine analysis is not grounds for merging every Interface and Operation into one IR. Compiler query graphs must not be reinterpreted as Contract dependencies, nor should Host Compiler type systems or object identities become Contract authority. Conversely, evidence required for exact verification must not be invented merely because it is absent from Classfiles.

**The present conclusion is a research priority, not an architectural selection.** If implementation difficulty is disregarded, combining high-level Operation meaning with Established Contract Knowledge is expected to offer the widest range of optimization opportunities. This does not establish that it is best for every JVM workload. Retain the Classfile-based path as an exact comparison baseline and a fallback wherever it can be supported, measure the additional value of actual cross-boundary optimization, and only then close the architectural decision.

---

## Related Documents

- `What Contract Is` — Highest authority for Contract meaning.
- `0046-idl-first-interface-contract-frontend-1d-catalog-backend-discipline.md` — Generated Operation Surface and the user Realization boundary.
- `0063-contract-establishment-occurrence-applicability-and-semantic-dependency.md` — Establishment, Occurrences, and semantic dependency.
- `0071-resolved-contract-hir-semantic-boundary-deterministic-publication-lifecycle-and-reuse.md` — Resolved HIR meaning, retention, and consumption rules.
- `0073-jvm-platform-native-surface-contract-preservation-and-external-technology-boundary.md` — Source-Semantic Surface, platform use, closure, and preservation.
- `0075-compiler-result-ownership-legal-consumption-deterministic-realization-and-contract-implementation-separation.md` — Compiler product consumption and reuse boundaries.
- `kontrakt-compiler-total-architecture-map-design-draft.md` — Existing V1-centered working baseline for compiler architecture.
- `kontrakt-contract-aware-realization-optimization-todo.md` — Existing TODO on Contract-aware optimization.
- `kontrakt-v2-incremental-architecture-research-todo.md` — Incremental architecture candidates and replaceable V1 foundations.

All examples and structures in this document are provided for design review. They do not replace the legal Judgments or sequencing defined by existing ADRs.
