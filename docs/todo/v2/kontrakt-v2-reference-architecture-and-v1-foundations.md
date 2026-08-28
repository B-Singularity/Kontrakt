# Kontrakt V2 Reference Architecture and V1 Foundation Requirements

## Status

**Reference architecture for future design. Not an ADR. Not Contract authority.**

This document does not define the final V2 architecture and does not replace any accepted Contract semantics.

Its purpose is narrower and more practical:

> **Define the architectural foundations and extension seams that V1 must establish so that V2 can evolve into a SOTA
commercial-grade compiler platform with incremental compilation, verification, test synthesis, diagnostics,
optimization, parallelism, caching, tooling, and replaceable backends without requiring a second architectural
rewrite.**

The exact shapes of the Frontend, Semantic IR, Canonical representation, and the Establishment / Occurrence /
Applicability model remain subject to later ADR decisions.

Therefore, concrete type names, IR names, query names, and package names in this document are **candidate vocabulary**,
not final semantics.

The architectural ordering should remain:

```text
correct Contract semantics
        ↓
stable semantic representation
        ↓
shared compiler analyses
        ↓
product projections
        ↓
replaceable realization
        ↓
incremental / parallel / cached execution
```

Compiler architecture must adapt to Contract semantics.

Contract semantics must never be distorted to make the compiler easier to implement.

---

# 1. V2 Is a Contract Compiler Platform, Not a Linear Code Generator

Kontrakt V2 should not be modeled as only:

```text
Frontend
→ IR
→ Optimizer
→ JVM
```

Kontrakt derives multiple independent products from one explicit Contract World.

A Canonical Contract World may feed at least:

```text
Canonical Contract World
        │
        ├── Contract Verification
        ├── Reference Judgment
        ├── PBT / Fixture / Unit-Test Synthesis
        ├── Contract Coverage
        ├── Enforcement Projection
        ├── Diagnostic Projection
        ├── Diagnostic Evidence Realization Planning
        ├── Publication / Output Projection
        ├── Generated API / ABI
        ├── Optimization / Specialization
        └── JVM Realization
```

These subsystems must not form an accidental authority chain such as:

```text
Verifier
   ↓
PBT
   ↓
Diagnostic
   ↓
Backend
```

Instead, they should consume the same stable semantic substrate.

```text
                 Canonical Contract World
                           │
        ┌──────────────────┼───────────────────┐
        │                  │                   │
        ▼                  ▼                   ▼
    Verifier          Test Synthesis       Diagnostics
        │                  │                   │
        ├─────────────┐    │    ┌─────────────┤
        │             │    │    │             │
        ▼             ▼    ▼    ▼             ▼
 Reference       Shared Semantic Analyses   Evidence /
 Judgment                                  Explanation
        │
        └──────────────────┬───────────────────┘
                           │
                  Realization Projection
                           │
                     Optimization
                           │
                       Backend
```

Sub-systems may share derived analyses.

They must not silently acquire the semantic authority owned by the originating Contract authority.

---

# 2. Engineering Principles to Import from External Systems

External systems are used here as engineering references.

Their implementation vocabulary must not be copied into Contract semantics.

The useful material is their architectural discipline.

---

## 2.1 LLVM / MLIR — Transformation Infrastructure

Relevant principles:

- Separate IR abstraction levels explicitly.
- Separate analysis from transformation.
- Cache expensive analyses and reuse them across passes.
- Make analysis preservation and invalidation explicit.
- Allow lowering targets to define legal and illegal representation.
- Make completion of a lowering pipeline mechanically verifiable.
- Treat pass identity, timing, statistics, before/after dumps, bisection, and reproducers as compiler infrastructure.
- Treat IR verification as a normal correctness boundary.
- Do not destroy high-level semantic information earlier than required.

Kontrakt implication:

```text
Semantic Contract meaning
    ≠
Contract-Machine realization IR
    ≠
JVM IR
```

and:

```text
analysis result
    ≠
transformation
```

must remain explicit.

---

## 2.2 rustc / Swift Request Evaluator / Kotlin K2 — Semantic Computation

Relevant principles:

- Compiler knowledge can be expressed as demand-driven computation.
- Queries or requests have explicit keys and explicit results.
- Dependencies between computations can be tracked.
- Immutable deterministic results are easier to memoize and incrementally reuse.
- Attaching mutable analysis state directly to AST nodes creates hidden dependencies.
- CLI compiler and IDE should reuse one semantic frontend.
- Compiler work should not depend on one global mutable pipeline object.

Kontrakt implication:

```text
Compiler Semantic Database
    = compiler computation infrastructure

Canonical Contract World
    = Contract-derived semantic product
```

The semantic database is not Contract authority.

---

## 2.3 Bazel Skyframe / Buck2 DICE — Incremental Computation

Relevant principles:

- Stable keys identify computations.
- Computations produce reusable values.
- Dependencies form an explicit graph.
- Work is evaluated on demand.
- Recalculation may stop propagating when a recomputed result is unchanged.
- Logical dependency and physical scheduling are separate.
- Parallel completion order must not define meaning.
- Deterministic inputs and outputs enable persistent and remote reuse.
- Metadata/result caches and content-addressed artifact storage serve different purposes.

Kontrakt must keep the following relations distinct:

```text
semantic dependency
    ≠
query dependency
    ≠
build dependency
    ≠
runtime flow dependency
```

---

## 2.4 PostgreSQL MVCC — Immutable Snapshot Discipline

Relevant principles:

- A reader observes one coherent snapshot.
- Incomplete concurrent updates are not visible.
- Old and new generations can coexist.
- A writer can progress without corrupting an active reader's view.

Compiler application:

```text
Compilation Candidate
        ↓
verify / seal
        ↓
Published Compiler Generation
```

IDE, verifier, optimizer, backend, and tooling consumers should never observe a half-built semantic world.

MVCC is an implementation analogy, not Contract semantics.

---

## 2.5 Linux RCU — Publication and Reclamation

Relevant principles:

- Construct the new representation before publication.
- Publish a completed view.
- Allow existing readers to continue using an older view.
- Separate publication from reclamation.

Possible V2 daemon/server realization:

```text
candidate build
    ↓
fully verified immutable snapshot
    ↓
publish generation
    ↓
old readers finish
    ↓
reclaim old physical storage
```

Again, this belongs to compiler realization, not Contract Theory.

---

## 2.6 RocksDB / Immutable Storage Pipelines — Persistent Cache Discipline

Relevant principles:

- Separate mutable staging from immutable persisted segments.
- Compaction must not alter logical value.
- Manifest/schema metadata describes physical generations.
- Hot in-memory and cold persisted representations may differ.
- Physical storage maintenance must not define semantic identity.

Kontrakt implication:

```text
semantic identity
    ≠
cache entry identity
    ≠
physical file location
    ≠
compaction generation
```

A persistent cache is an optimization.

It must never become semantic authority.

---

## 2.7 Operating Systems — Resource Ownership and Isolation

Relevant principles:

- Resource ownership must be explicit.
- Admission and accounting are distinct concerns.
- One subsystem must not consume unbounded resources owned by another.
- Physical scheduling must not define workload meaning.
- Cancellation, memory pressure, and failure must not lead to partial publication.

Kontrakt compiler resource control may eventually distinguish:

```text
CompilerParseBudget
CompilerSemanticWorkBudget
CompilerVerificationBudget
CompilerTestGenerationBudget
CompilerDiagnosticBudget
CompilerOptimizationBudget
CompilerBackendBudget
CompilerMemoryEnvelope
```

These are compiler-realization resource controls.

They are not user-declared Contract Budget semantics.

---

## 2.8 ThinLTO — Summary-Driven Whole-Program Analysis

Relevant principles:

- Whole-program analysis does not require full IR of every module to be merged eagerly.
- Compact summaries can drive global decisions.
- A summary index can identify which units need full materialization.
- Backend work can remain independently parallelizable.
- Whole-program optimization and incremental compilation can coexist.

Candidate Kontrakt summaries:

```text
ContractUnitSummary
InterfaceSummary
AuthoritySummary
StateMachineSummary
PolicyWorldSummary
PublicationSummary
DependencySummary
CapabilityRequirementSummary
```

The exact schema must be derived from final Contract semantics.

---

## 2.9 Alive2 / Compiler Fuzzing / Property-Based Systems — Independent Checking

Relevant principles:

- Transformation logic and correctness checking should be separable.
- Differential testing is highly effective for generated and optimized code.
- Deterministic bounded fuzzing improves reproducibility.
- Failure minimization improves regression quality.
- Generation and shrinking are independent algorithmic responsibilities.

Kontrakt can exploit:

```text
Reference Judgment
      │
      ├── Generated Enforcement Differential Check
      ├── Backend Differential Check
      ├── PBT Oracle
      └── Translation Validation
```

---

# 3. Five-Plane V2 Architecture

A useful conceptual model is five planes.

```text
┌─────────────────────────────────────────────────────────────┐
│ Plane 1 — Contract Authority                                │
│                                                             │
│ Input / Admission / Canonicalization / Lowering / Fact      │
│ Invariant / State / Transition / Failure                    │
│ Policy / Governance / Budget / Capacity / Version           │
│ Publication / Output / Diagnostic Evidence / Retention      │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
╔═════════════════════════════════════════════════════════════╗
║ Plane 2 — Semantic Compiler Core                           ║
║                                                             ║
║ Source / Syntax / Resolution                               ║
║ Semantic Contract IR                                       ║
║ Semantic Query Database                                    ║
║ Stable Identity / Provenance                               ║
║ Canonical Contract World                                   ║
║ Whole-Machine Linking                                      ║
║ Shared Semantic Analysis                                   ║
╚═══════════════════════════╤═════════════════════════════════╝
                            │
        ┌───────────────────┼───────────────────────┐
        │                   │                       │
        ▼                   ▼                       ▼
┌────────────────┐  ┌──────────────────┐   ┌──────────────────┐
│ Plane 3        │  │ Plane 3          │   │ Plane 3          │
│ Verification   │  │ Test Synthesis   │   │ Diagnostics      │
│ / Reasoning    │  │ / Coverage       │   │ / Explanation    │
└───────┬────────┘  └────────┬─────────┘   └────────┬─────────┘
        │                    │                      │
        └────────────┬───────┴──────────────┬───────┘
                     │                      │
                     ▼                      ▼
            Product Projections      Publication /
                     │               Output Projection
                     └───────────┬───────────┘
                                 ▼
┌─────────────────────────────────────────────────────────────┐
│ Plane 4 — Realization                                      │
│                                                             │
│ Enforcement Kernel / Test Executor / Evidence Runtime       │
│ Contract-Machine IR / Optimization / JVM IR / Classfile     │
│ Generated API / Reporting / Host adapters                   │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ Plane 5 — Compiler Infrastructure                           │
│                                                             │
│ Query / Incremental / Cache / Pass / Analysis / Rewrite     │
│ Parallel Executor / Resource Control / Diagnostics Engine   │
│ Reproducer / Tracing / Profiling / Build / IDE / QA         │
└─────────────────────────────────────────────────────────────┘
```

Plane 5 must never become semantic authority for Plane 1.

Query engines, caches, schedulers, files, threads, objects, JFR records, and JVM layouts are implementation mechanisms.

---

# 4. Shared Semantic Substrate

All major V2 product subsystems should converge on one shared backend-independent semantic product.

Conceptually:

```text
Canonical Contract World

- backend-independent
- immutable after publication
- exact semantic identity
- explicit authority relations
- explicit applicability relations
- deterministic representation
- source provenance separated from identity
- compact cross-reference support
- serialization / summary generation seam
```

The following must remain distinct:

```text
Authored Source
    ≠
Semantic Contract IR
    ≠
Canonical Contract World
    ≠
Published Physical Snapshot
```

A source edit may leave semantic meaning unchanged.

A new physical snapshot generation may carry identical semantic identities.

---

# 5. Semantic Query Database

## 5.1 Responsibility

The V2 query system should own reusable compiler computation.

Candidate queries may include:

```text
parse(SourceFileId)
resolve(SymbolId)
semanticDefinition(DefinitionId)
canonicalDefinition(DefinitionId)

policyWorld(InterfaceId, WorldId)
stateMachine(StateMachineId)

contractSummary(UnitId)
linkedInterface(InterfaceId)
linkedWholeMachine(WholeMachineId)

verifyDefinition(DefinitionId)
verifyWorld(WorldId)

obligationsOf(AuthorityId)
testObjectives(AuthorityId)
diagnosticProjection(JudgmentId)

realizationPlan(WholeMachineId, TargetCapabilityId)
```

The exact query catalog should be designed separately.

---

## 5.2 Query-Compatible Computation Laws

A query-compatible compiler computation should move toward:

```text
Explicit Key
Explicit Inputs
Deterministic Result
Explicit Dependency Reads
Immutable Published Result
No Hidden Ambient State
No User-Visible Side Effect During Computation
```

Avoid:

```kotlin
fun analyze() {
    GLOBAL_DIAGNOSTICS += "error"
    GLOBAL_CACHE["..."] = "..."
    GLOBAL_WORLD.mutate("...")
}
```

Computation and publication should be separate operations.

---

## 5.3 Query Keys

Query keys must not be JVM object identities or ad-hoc strings.

Candidate shape:

```text
QueryKey
    QueryKind
    StableSubjectIdentity
    SemanticConfigurationIdentity
    TargetConfigurationIdentity when applicable
```

Example:

```text
VerifyPolicyWorld(
    InterfaceSemanticId,
    PolicyWorldSemanticId,
    ContractVersionSetId
)
```

---

## 5.4 Identity and Fingerprint

Always distinguish:

```text
Stable Semantic Identity
    ≠
Input Fingerprint
    ≠
Result Fingerprint
```

Identity answers:

> What semantic entity is this?

Fingerprint answers:

> Is this input/result materially unchanged?

---

# 6. Incremental Compilation

V2 should target semantic-result-level early cutoff rather than only file-level invalidation.

```text
Input changed
    ↓
affected query may be dirty
    ↓
recompute dependency if required
    ↓
new result fingerprint == previous result?
    ├─ yes → GREEN → stop downstream invalidation
    └─ no  → RED   → reconsider dependents
```

Example:

```text
comment moved
    ↓
source provenance changed
    ↓
Semantic Contract Definition unchanged
    ↓
Verifier unchanged
PBT objective unchanged
Backend plan unchanged
    ↓
Diagnostic source projection refreshed only
```

Another example:

```text
Policy World meaning changed
    ↓
Policy semantic fingerprint RED
    ↓
Governance analysis
Verifier
PBT objectives
Enforcement projection
Backend specialization
affected diagnostics
```

Only affected products should be recomputed.

---

# 7. Keep Dependency Graphs Distinct

V2 will contain many graphs.

They must not be collapsed into one generic graph ontology.

## 7.1 Source / Syntax Relations

```text
source node
token
syntax parent/child
```

## 7.2 Contract Semantic Dependency

```text
Authority B requires established material from Authority A
```

## 7.3 Compiler Query Dependency

```text
VerifyWorld(W)
    depends on
LinkedWorld(W)
```

## 7.4 Build Dependency

```text
module B
    depends on
module A artifact/summary
```

## 7.5 Whole-Machine Composition Dependency

A Contract-Theory relation.

## 7.6 Realization CFG / Def-Use Dependency

A low-level execution representation.

Each graph has different identity, cycle, lifetime, and ownership laws.

Avoid a universal semantic `Node / Edge / Graph` abstraction.

---

# 8. Compilation Revision, Candidate, Snapshot, and Generation

Database and RCU-style publication patterns suggest a useful realization boundary.

```text
Workspace Revision
        ↓
Compilation Revision
        ↓
Private Candidate Work
        ↓
Verification Barrier
        ↓
Seal
        ↓
Published Generation
```

---

## 8.1 Published Generation

A published generation should be:

- immutable;
- complete;
- free of worker-private state;
- free of backend-native handles in semantic material;
- coherent for every consumer.

---

## 8.2 Generation Is Not Semantic Identity

```text
Generation 180
Generation 181
```

may contain identical semantic worlds.

Therefore:

```text
GenerationId
    ≠
ContractSemanticId
```

---

## 8.3 Commit / Abort

A failed candidate must not partially replace a published generation.

```text
candidate
    ↓
reject
    ↓
no partial publication
```

This extends V1 freeze/seal/publication discipline toward V2 daemon and incremental compilation.

---

# 9. Multi-Level IR

Do not use one universal IR for every concern.

Candidate levels:

```text
Source Syntax Representation
        ↓
Semantic Contract IR
        ↓
Canonical Contract Material
        ↓
Linked Contract World
        ↓
Contract-Machine High IR
        ↓
Lowered Contract-Machine IR
        ↓
JVM Realization IR
        ↓
Classfile Artifact
```

Product subsystems may maintain independent projections:

```text
Canonical Contract World
    ├── Verification Projection
    ├── Test Objective / Generation Plan
    ├── Diagnostic Projection
    ├── Evidence Realization Plan
    └── Enforcement Projection
```

---

## 9.1 Syntax Representation

Owns:

- source text relations;
- tokens;
- grammar structure;
- source spans;
- parse recovery state;
- formatter/IDE-oriented structure.

Does not own:

- final Contract authority;
- backend lowering;
- JVM identity.

---

## 9.2 Semantic Contract IR

Owns:

- resolved declaration meaning;
- exact Contract symbols;
- Contract roles;
- semantic references;
- source-origin handles.

Does not own:

- canonical physical encoding;
- runtime tables;
- JVM operations.

---

## 9.3 Canonical Contract Material

Owns:

- semantic equality;
- canonical identity;
- normalized relation;
- deterministic encoding boundary;
- backend-neutral meaning.

Does not own:

- source presentation;
- runtime object lifetime;
- optimizer layout.

---

## 9.4 Frozen / Published Representation

`Frozen` need not become another Contract abstraction level.

A better model is:

```text
Canonical Semantic Material
    ├── compiler object representation
    ├── dense table representation
    ├── serialized persistent representation
    └── published frozen snapshot
```

The important laws are:

```text
immutable after publication
no half-built state
stable identity preserved
backend handles erased
```

---

## 9.5 Linked Contract World

Owns:

- cross-definition resolution;
- module/interface binding;
- Whole-Machine relations;
- Policy / Version / Governance relations;
- state-machine linkage;
- Publication / Output relations;
- inputs to global coherence checks.

It does not require one eager giant object graph.

Summary-driven linking should remain possible.

---

## 9.6 Contract-Machine High IR

This level represents executable semantic judgment relations rather than source declarations.

Candidate vocabulary:

```text
RequireInput
JudgeAdmission
Canonicalize
Lower
EstablishFact
JudgeInvariant
RequireState
JudgeTransition
CheckBudget
CheckCapacity
SelectGovernanceBinding
EstablishFailure
JudgePublication
ProjectOutput
```

No JVM opcodes should appear here.

---

## 9.7 JVM Realization IR

Target-specific concepts become legal here.

Candidate vocabulary:

```text
BasicBlock
VirtualValue
Load
Store
Compare
Branch
Invoke
Return
EvidenceEmit
```

The JVM operand stack should preferably remain an emission concern rather than a middle-end design constraint.

---

# 10. Whole-Machine Summary Architecture

Whole-Machine compilation should not require eager full loading of every unit.

Candidate model:

```text
Compilation Unit
    ↓
Contract Summary
    ↓
Summary Index
    ↓
Whole-Machine Global Analysis
    ↓
Required Unit Materialization
```

Possible summary contents:

```text
stable unit identity
exported authority identities
required authority identities
world membership
state-machine boundary summary
failure/publication surface summary
capability requirements
semantic dependency summary
fingerprint
```

Benefits:

- lower Whole-Machine linking memory;
- incremental linking;
- parallel backend work;
- partial IDE analysis;
- remote/shared cache compatibility.

V1 does not need a complete summary engine.

It should avoid APIs that require the full object graph for every cross-unit operation.

---

# 11. Shared Semantic Analysis

Verifier, PBT, Diagnostics, and Optimizer should reuse analysis products.

Candidate analyses:

```text
AuthorityDependencyAnalysis
ApplicabilityAnalysis
EstablishedMaterialDependencyAnalysis

StateReachabilityAnalysis
TransitionLegalityAnalysis

InvariantDependencyAnalysis
FailureReachabilityAnalysis
PublicationReachabilityAnalysis

PolicyWorldCoherenceAnalysis
GovernanceCompletenessAnalysis

BudgetCompatibilityAnalysis
CapacityCompatibilityAnalysis

BackendCapabilityRequirementAnalysis
DiagnosticObservabilityAnalysis
```

An analysis product may conceptually have:

```text
AnalysisKey
SemanticSubjectId
InputFingerprint
Result
ResultFingerprint
```

---

# 12. Query / Pass / Analysis / Rewrite Separation

The query system and pass system solve different problems.

Queries are suitable for:

```text
"What is X?"
"What does X depend on?"
"Is X valid?"
"What is the summary of X?"
```

Passes are suitable for:

```text
IR A
    ↓ transform
IR B
```

---

## 12.1 Pass Metadata

Candidate pass metadata:

```text
PassId
PassVersion

InputIRKind
OutputIRKind
Scope

RequiredAnalyses
PreservedAnalyses

LegalityRequirement
ContractPreservationRequirement

DeterminismRequirement
ResourceClass
```

---

## 12.2 Analysis Invalidation

A transform must be able to state:

```text
all preserved
some preserved
none preserved
```

Updating an analysis result and invalidating it are distinct actions.

---

## 12.3 Rewrite Legality

A lowering target may define:

```text
legal
illegal
conditionally legal
```

representations.

Example:

```text
JVM Lowering Target

legal:
    PrimitiveGate
    StaticTableLookup
    DirectFailureCode

illegal:
    UnresolvedGovernanceSelection
    AbstractPublicationSelection
    HostReflectionLookup
```

Lowering completion means:

> No illegal representation remains.

Not merely:

> The pass returned successfully.

---

# 13. Verifier Architecture

The Contract Verifier is not one compiler pass.

Recommended V2 verification layers:

```text
Contract Material Verification
Composition / Whole-World Verification
Semantic Dependency Verification
Lowering Preservation Verification
Published Snapshot Verification
Enforcement Projection Verification
Backend Capability Verification
Realization Conformance Verification
```

A separate:

```text
IR Structural Verifier
```

also exists.

Therefore:

```text
ContractVerifier
    ≠
IRVerifier
```

---

# 14. Reference Judgment

The existing V1 Reference Judgment direction is highly valuable.

Its exact representation should be re-derived from the final Contract semantics.

Its role is:

```text
Canonical Contract World
        ↓
simple deterministic reference judgment
```

The reference path is optimized for clarity and correctness rather than hot-path performance.

It can serve:

```text
Reference Judgment
    ├── PBT oracle
    ├── Generated Gate differential verification
    ├── Backend differential verification
    ├── Optimization translation validation
    └── Regression oracle
```

The optimized/generated implementation should not share so much internal logic with the reference path that differential
checking becomes circular.

---

# 15. Translation Validation

Important transforms should be independently checkable where practical.

```text
Before IR
    ↓
Optimization
    ↓
After IR
    ↓
Independent Preservation Check
```

Preservation obligations differ by pass.

### Gate Fusion

May need to preserve:

```text
judgment result
failure attribution
required evidence correlation
Budget accounting relation
```

### Table Layout Optimization

May only need to preserve:

```text
semantic lookup relation
canonical identity relation
```

Not every pass requires SMT proof.

V1 should establish the verification hooks.

V2 may attach stronger translation validation to high-risk transforms.

---

# 16. PBT / Fixture / Unit-Test Synthesis

This is a product subsystem, not compiler QA.

Candidate architecture:

```text
Canonical Contract World
        ↓
Obligation Extraction
        ↓
Test Objective Planning
        ↓
Generation Domain Construction
        ↓
Case Generation
        ↓
Fixture Materialization
        ↓
Reference Judgment
        ↓
Expected Result
        ↓
Generated Unit/PBT Product
```

---

## 16.1 Test Objectives

Generated cases should map to exact Contract obligations rather than source lines.

Examples:

```text
Admission:
    accepted witness
    rejected witness
    boundary witness

Invariant:
    satisfying witness
    violating witness

State:
    legal transition
    illegal transition
    terminal condition

Policy/Governance:
    world-specific decision
    binding boundary

Budget/Capacity:
    below limit
    exact boundary
    exceeded boundary

Failure:
    exact source attribution

Publication:
    publishable
    non-publishable
```

---

## 16.2 Deterministic Generation

Reproducible generation may require:

```text
TestObjectiveId
GeneratorVersion
ExplicitSeed
GenerationBudget
World/Version Context
```

Randomness must not define semantic truth.

---

## 16.3 Shrinking

Shrinking should be Contract-aware.

Examples:

```text
preserve the same Failure source
while reducing input size

preserve the required Policy World

preserve the State precondition

preserve the semantic boundary being tested
```

---

## 16.4 Contract Coverage

Line coverage is insufficient.

Candidate Contract coverage coordinates:

```text
obligation covered?
boundary covered?
legal/illegal transition covered?
Failure source covered?
Policy World covered?
Budget boundary covered?
Publication outcome covered?
```

Coverage should reuse semantic analyses already produced by verification and test planning.

---

# 17. Generated Test Execution

Test synthesis and execution are separate.

```text
Test Product
    ↓
Test Execution Adapter
    ↓
Implementation / Generated Contract Machine
    ↓
Observed Result
    ↓
Expected Contract Judgment comparison
```

JUnit may be an adapter.

JUnit is not Contract authority and must not define Kontrakt's test lifecycle.

Future runners, Gradle workers, or IDE execution should consume the same semantic test product.

---

# 18. Diagnostic Architecture

V2 must distinguish at least:

```text
1. Compiler Diagnostics

2. Contract Diagnostic Evidence / Retention

3. Generated-System Operational Diagnostics
```

They should not collapse into one logger.

---

## 18.1 Compiler Diagnostics

Examples:

```text
parse error
resolution error
invalid Contract composition
verifier rejection
backend capability refusal
optimization remark
incremental invalidation explanation
internal compiler error
```

Structured records should precede textual rendering.

---

## 18.2 Semantic / Provenance / Presentation Separation

Diagnostics should separate:

```text
semantic result
source provenance
rendered presentation
```

A source-only edit may allow:

```text
semantic diagnostic      GREEN
source projection        RED
human text rendering     refreshed
```

---

## 18.3 No Diagnostic Side Effects in Core Queries

Correctness paths should not depend on:

```text
println
global logger append
terminal rendering
```

Queries/passes should produce structured diagnostic products.

Final publication should occur after deterministic merge for the active compilation revision.

---

## 18.4 Rich Explanation on Demand

Persist cheaply:

```text
stable code
semantic subject
source relation
cause relation
small structured arguments
```

Generate only when required:

```text
large conflict reduction
full pass history
IR snapshots
proof object
deep reproducer
JIT/OS operational attachment
```

Diagnostic richness must not bloat hot compiler representations.

---

# 19. Diagnostic Evidence Realization

Contract Diagnostic Evidence semantics belong to the relevant ADRs.

Compiler architecture should only provide realization seams.

Keep:

```text
Required Contract Evidence
        ≠
Optional Operational Telemetry
```

Required evidence must not be silently sampled or dropped by a backend.

Optional telemetry may use mechanisms such as:

```text
JFR
stack traces
OS tracing
hardware counters
external observability
```

Stable correlation may join the two layers without merging their authority.

---

# 20. Enforcement Projection

Runtime enforcement is not the verifier.

```text
Canonical World
    ↓
Enforcement Projection
    ↓
Realization Planner
    ↓
Generated Gate / Table / Kernel
```

A separate verifier checks:

```text
Generated Projection
    ↓
Conformance / Differential Verification
```

Do not let the same implementation both generate a projection and be the only authority declaring it correct.

---

# 21. Optimization Architecture

Kontrakt should not reimplement a general JVM compiler.

Its strongest optimizations are high-level Contract-aware transformations.

Candidate priority:

```text
Static Resolution
Static Discharge
Dependency Slicing
Common Judgment Elimination
Partial Evaluation
Predicate Simplification
World Specialization
State/Transition Table Specialization
Pipeline Fusion
Dead Realization Elimination
Primitive Layout Specialization
Allocation Reduction
```

---

## 21.1 Legality vs Profitability

Always separate:

```text
Candidate transform
        ↓
Is it legal?
        ├─ no → reject
        ↓ yes
Would it be profitable?
        ├─ no → skip
        ↓ yes
Apply
```

A cost model cannot legalize an invalid transformation.

---

## 21.2 Optimization Remarks

Useful result categories:

```text
applied
blocked by legality
blocked by diagnostic preservation
blocked by backend capability
declined by cost model
declined by resource budget
```

---

## 21.3 JVM JIT-Friendly Realization

Kontrakt should emit material that HotSpot/Graal can optimize effectively.

Avoid:

```text
reflection
megamorphic generic dispatch
boxing
temporary object graphs
opaque callbacks
generic Map<String, Any>
```

Prefer:

```text
primitive data
stable direct/static call
predictable branch
small compact table
few allocations
specialized path
```

---

# 22. JVM Backend

Candidate hierarchy:

```text
Contract-Machine IR
        ↓
JVM Lowering
        ↓
JVM CFG / Virtual Value IR
        ↓
JVM-specific optimization
        ↓
stack/local planning
        ↓
Classfile Emitter
```

A standard class-file library may implement the emitter.

It must not define backend architecture or Contract semantics.

---

# 23. Backend Capability Model

The target backend should provide an explicit capability snapshot.

Possible capability material:

```text
supported classfile level
available evidence persistence
supported resource enforcement
generated API capability
runtime instrumentation capability
target JVM constraints
```

Compilation checks:

```text
Contract Requirement
        vs
Backend Capability
```

Unsupported guarantees must not be silently weakened.

They should fail at the capability boundary.

---

# 24. Persistent Cache Architecture

Logical cache layers:

```text
L0 — compilation-session memoization

L1 — local persistent semantic/query cache

L2 — shared / remote cache
```

Not every query must be persistable.

---

## 24.1 Cache Key Material

Candidate material:

```text
QueryKind
Stable Query Key
Compiler Semantic Schema
Language Version
Target Configuration
Backend Capability Version
Relevant Input Fingerprints
```

---

## 24.2 Separate Stores

Potential future separation:

```text
Query Result Metadata Store

Content-Addressed Blob Store

Generated Artifact Store

Diagnostic/Reproducer Store
```

Large blobs can then be shared across multiple logical results.

---

## 24.3 Cache Non-Authority

Always preserve:

```text
cache hit
    ≠
semantic proof
```

A lost cache must only reduce performance.

It must not prevent recomputation of semantic truth.

---

## 24.4 Cache GC / Compaction

Cache pruning, file migration, or compaction must not modify semantic identity.

Storage maintenance is physical maintenance only.

---

# 25. Serialization and Schema Evolution

V2 may eventually contain several independent version domains:

```text
Kontrakt Language Version
Contract Version
Semantic IR Schema Version
Canonical Encoding Version
Query Cache Schema Version
Summary Schema Version
Diagnostic Schema Version
Backend Artifact Schema Version
```

Do not collapse them.

Persistent formats should provide:

- explicit schema versions;
- fail-closed malformed decoding;
- bounded decoding;
- upgrade/migration boundaries;
- golden vectors;
- round-trip tests.

---

# 26. Parallel Execution

Parallel execution is realization only.

Worker completion order must never define semantic order.

Preferred pattern:

```text
Worker A → private candidate
Worker B → private candidate
Worker C → private candidate
             ↓
      deterministic merge
             ↓
          verify
             ↓
           seal
             ↓
          publish
```

---

## 26.1 Worker-Local Ownership

Prefer:

```text
worker-local arena
worker-local temporary tables
worker-local diagnostics
worker-local test candidates
worker-local rewrite buffers
```

Avoid global mutable hot structures shared by all workers.

---

## 26.2 Deterministic Merge

Derive merge ordering from stable coordinates such as:

```text
semantic identity
stable source identity
explicit ordinal law
```

Never from:

```text
finish time
thread id
hash iteration order
```

---

## 26.3 Cancellation

Cancellation should discard the active candidate.

```text
cancel
    ↓
discard private candidate
```

It must not partially mutate a published generation.

---

# 27. Compiler Resource Model

The compiler itself should be resource-aware.

This is distinct from Contract Budget and Capacity.

Candidate categories:

```text
SourceBytes
SyntaxNodes
SemanticNodes
QueryWork
QueryCacheBytes

VerificationWork
SolverWork
TestGenerationWork
ShrinkWork

DiagnosticBytes
DiagnosticEnrichmentWork

RewriteWork
OptimizationWork
BackendWork

InMemorySemanticBytes
TemporaryArenaBytes
PersistentCacheBytes
```

---

## 27.1 Resource Planning

Heavy subsystems need explicit:

```text
admission
budget
accounting
failure
cleanup
```

boundaries.

Pathological PBT generation or rewrite convergence must remain bounded.

---

## 27.2 Memory Layout

V1 may prefer clear immutable domain objects.

V2 hot representation can later lower into:

```text
dense ordinal
IntArray
LongArray
byte slab
SoA table
bitset
interned compact handle
```

Always preserve:

```text
semantic identity
    ≠
dense local ordinal
```

---

# 28. Build-System Integration

Gradle must not own compiler semantics.

Preferred layering:

```text
Gradle Plugin
      ↓
Compiler Service API
      ↓
Compiler Driver
      ↓
Semantic Query / Product Compiler
```

Future daemon, persistent cache, remote cache, or remote execution should not alter the core semantic model.

---

## 28.1 Hermetic Compiler Input Perimeter

Compiler output should be derived from explicit inputs such as:

```text
source files
language version
compiler version/schema
target capability
explicit build options
declared host evidence
```

Avoid hidden influence from:

```text
current time
filesystem enumeration order
username
hostname
locale
worker count
temporary path
unfixed random seed
cache state
```

---

## 28.2 Reproducibility

Strong target:

```text
same authoritative inputs
    →
same semantic world
same canonical identity
same diagnostics
same generated behavior
```

Bit-identical artifacts should be preferred when practical.

Test at least:

```text
clean build
incremental build
warm cache
cold cache

1 worker
N workers

different discovery order
different hash iteration order
```

---

# 29. IDE / LSP Architecture

Do not build a separate semantic engine for IDE use.

```text
                     Semantic Query Database
                      /                    \
                     /                      \
              Command-line Compiler        IDE / LSP
```

Possible IDE queries:

```text
definitionAt(position)
semanticAuthorityAt(position)
referencesOf(authority)
applicableContracts(interaction)
policyWorlds(interface)
stateTransitions(state)
whyInvalid(subject)
diagnosticsFor(file, revision)
```

CLI and IDE should not become two different semantic compilers.

---

# 30. Compiler Diagnostics and Tooling

A commercial-grade compiler needs internal inspection surfaces.

Possible tools:

```text
--dump-ir=<stage>

--print-before=<pass>
--print-after=<pass>
--verify-each

--time-passes
--pass-statistics

--trace-query=<key>
--explain-invalidation=<key>

--explain-link=<subject>
--explain-world=<world>
--explain-backend-capability=<subject>

--reproduce=<bundle>
```

Exact CLI syntax can be decided later.

V1 should preserve the architecture needed to expose these views.

---

# 31. Crash Reproducer

An internal compiler error should not end with only a stack trace.

Candidate reproducer material:

```text
compiler schema/version
normalized compiler options
target capability snapshot identity

source/input manifest
relevant semantic fingerprint set

last verified IR boundary
pass pipeline
query trace summary

optional IR snapshot
optional minimized input
```

Deep data can be generated on demand.

It does not need to be retained for every successful build.

---

# 32. Compiler QA

Commercial compiler quality depends heavily on infrastructure, not only optimization count.

Required test families eventually include:

```text
lexer/parser tests
resolution tests
semantic tests
canonical golden vectors

query dependency tests
incremental invalidation tests

IR verifier tests
pass regression tests
rewrite legality tests

verification tests
reference-judgment tests

PBT generator tests
shrinker tests
contract coverage tests

diagnostic structured tests
diagnostic rendering tests
provenance tests

backend differential tests
classfile verification tests

cache corruption tests
serialization fuzzing

parallel determinism tests
resource exhaustion tests

end-to-end tests
```

---

## 32.1 Fuzzing

Candidate fuzz targets:

```text
.kontrakt parser
malformed semantic input
canonical decoder
IR parser/serializer
rewrite engine
whole-machine linker
diagnostic renderer
cache decoder
backend classfile emitter
```

---

## 32.2 Differential Testing

```text
Reference Judgment
    vs
Generated Gate

Clean Build
    vs
Incremental Build

Cold Cache
    vs
Warm Cache

1 Worker
    vs
N Workers

Source Backend A
    vs
Equivalent Source Backend B
```

---

## 32.3 Metamorphic Testing

Semantics-preserving perturbations:

```text
source declaration ordering
irrelevant whitespace/comment changes
worker schedule
cache state
filesystem traversal order
```

should preserve the relevant semantic result.

---

# 33. V1 Foundations Required for V2

V1 does not need to implement the entire V2 system.

It does need to avoid architectural decisions that make V2 impossible without rewriting everything.

---

## 33.1 Frontend

### V1-P0

- Real `SourceManager`.
- Byte-accurate `SourceSpan`.
- Source identity separated from filesystem path.
- Lexer, parser, and semantic resolution separated.
- Contract-owned symbol identity.
- Source provenance handle.

### V2

- Incremental parsing.
- IDE-grade lossless syntax.
- Source revision database.
- Lazy semantic analysis.

---

## 33.2 Semantic Model

### V1-P0

- Derive a new Semantic Contract IR from current Contract semantics.
- Do not promote legacy runtime/test metamodel structures into semantic authority.
- Exact Contract authority identity.
- Source reference separated from canonical identity.
- Explicit seam for semantic dependency representation.

### V2

- Query-granular semantic products.
- Lazy semantic materialization.
- Persistent semantic cache.

---

## 33.3 Establishment / Occurrence / Applicability

### V1-P0

The common semantics currently being designed should allow stable distinction between:

```text
Definition
Occurrence
Judgment
Established Result
Applicable Context
Semantic Dependency
Succession / Supersession
```

These Contract concepts must not be defined in terms of compiler queries.

### V2

The resulting semantic identities should be representable as stable query keys/results and reusable analysis
dependencies.

---

## 33.4 Canonical Identity

### V1-P0

- Semantic identity protocol.
- Canonical encoding.
- HID/fingerprint separation.
- Exact collision verification.
- Deterministic interning law.
- Physical ordinal separated from semantic ID.
- Golden vectors.

### V2

- Persistent cache keys.
- Content-addressed artifact reuse.
- Cross-session semantic reuse.
- Fine-grained invalidation.

---

## 33.5 Immutable Publication

### V1-P0

- Builder/candidate separated from immutable published result.
- Freeze / seal / verify / publish boundary.
- No incomplete result publication.
- Failure produces zero partial publication.
- Generation identity separated from semantic identity.

### V2

- MVCC/RCU-like compiler snapshot management.
- Long-lived daemon reader pinning.
- Concurrent IDE/build generations.
- Safe reclamation of old generations.

---

## 33.6 Query Seam

### V1-P0

A full red/green engine is not required.

Major compiler computations should already use:

```text
explicit input
explicit result
no hidden global state
```

### V1-P1

A small in-memory query facade may be introduced.

### V2

- Dependency recording.
- Memoization.
- Persistent result cache.
- Red/green invalidation.
- Early cutoff.
- Parallel evaluation.

---

## 33.7 IR Architecture

### V1-P0

Conceptually separate at least:

```text
Semantic Contract IR
Canonical Contract Material
Linked Contract World
Lowered Contract-Machine IR
JVM IR
```

The physical class count may be smaller.

### V1-P0

Each IR level should define:

- owner;
- legal vocabulary;
- verifier;
- provenance behavior;
- serialization/debug form.

### V2

- Partial lowering.
- Lazy materialization.
- Pattern rewrite.
- IR textual tooling.
- Persistent binary IR.

---

## 33.8 Pass / Analysis

### V1-P0

- Pass identity.
- Analysis identity.
- Pass input/output boundary.
- Required/preserved analysis representation.
- `verify-each` hook.
- Deterministic pass ordering.

### V1-P1

- Simple pass manager.
- Simple analysis cache.
- Before/after dumps.
- Timing/statistics.

### V2

- Fine-grained invalidation.
- Parallel nested pipelines.
- Cost-model integration.
- Translation validation.

---

## 33.9 Verifier

### V1-P0

- Contract Material verification.
- Composition verification.
- Lowering/preservation verification.
- Enforcement projection verification boundary.
- Backend capability refusal boundary.

### V1-P1

- Deterministic conflict set.
- Reference Judgment candidate implementation.
- Differential checking for generated gates.

### V2

- Richer proof/check products.
- Stronger conflict reduction.
- Translation validation.
- Incremental verification reuse.

---

## 33.10 PBT / Fixture / Unit Test

### V1-P0

Keep this as a product subsystem with an independent lifecycle.

Do not place it under compiler QA.

### V1-P0

Generated test cases should carry identity linking them to exact Contract obligations.

### V1-P1

- Valid / invalid / boundary witnesses.
- State legal/illegal cases.
- Deterministic seed.
- Failure attribution.
- Basic fixture generation.
- Basic Contract coverage.

### V2

- Incremental test-plan reuse.
- Richer constraint-directed generation.
- Semantic shrinking.
- Targeted generation.
- Persistent failing-case database.

---

## 33.11 Diagnostics

### V1-P0

Compiler diagnostics should provide:

- structured records;
- stable diagnostic codes;
- semantic subjects;
- primary/related source anchors;
- deterministic merge;
- rendering separated from semantics;
- provenance mapping;
- no global string logger as architecture.

### V1-P0

Keep Contract Diagnostic Evidence separate from compiler diagnostics.

### V2

- Independent semantic/provenance/presentation invalidation.
- Persistent diagnostic cache.
- On-demand explanation.
- Richer IDE projection.
- Optimization remarks.
- Automated reproducers.

---

## 33.12 Optimization

### V1-P0

- Optimization never changes canonical authority.
- Passes expose preservation requirements.
- Legality and profitability are separate.
- Optimization decisions are deterministic.

### V1-P1

- Static resolution.
- Basic simplification.
- Specialization.
- Dependency slicing.
- Selected generated-path fusion.

### V2

- Shared-analysis-driven optimizer.
- Cost model.
- Translation validation.
- Possible PGO.
- Broader specialization.

---

## 33.13 Whole-Machine Linking

### V1-P0

- Linker separated from frontend/resolution.
- Explicit cross-unit identity.
- Deterministic link ordering.
- Cycle / invalid composition handling.
- Whole-Machine verification boundary.

### V1-P1

- APIs capable of producing unit summaries.

### V2

- Summary index.
- Thin-style global analysis.
- Lazy full-unit materialization.
- Parallel backend.
- Incremental linking.

---

## 33.14 JVM Backend

### V1-P0

- Backend port.
- Target capability object.
- No JVM vocabulary in semantic IR.
- Generated artifacts are non-authoritative.
- Deterministic emission.

### V1-P1

- Small JVM IR.
- Classfile emission adapter.
- Generated artifact verification.

### V2

- Richer CFG/value IR.
- JVM-specific analysis.
- Specialization.
- Stackification.
- HotSpot/Graal-friendly output.

---

## 33.15 Cache

### V1-P0

- Cache-hit/cache-miss semantic equivalence tests.
- Cache-key material separated from semantic identity.
- Cache corruption fails closed.
- Bounded cache lifetime.

### V2

- Persistent local cache.
- CAS/blob store.
- Remote cache.
- Cache pruning/compaction.
- Safe cross-workspace reuse where justified.

---

## 33.16 Parallelism

### V1-P0

- Worker/lane-private staging.
- Deterministic merge.
- Publication barrier.
- Worker-count equivalence testing.
- Cancellation discards candidates.

### V2

- Work stealing.
- Query-parallel execution.
- Whole-Machine parallel analysis.
- Backend parallelism.
- Adaptive scheduling.

---

## 33.17 Resource Discipline

### V1-P0

Compiler resource policy must remain distinct from user Contract Budget/Capacity at type and namespace boundaries.

### V1-P0

- Physical work accounting.
- Semantic work accounting.
- Memory cap.
- Diagnostic cap.
- Fixture-generation cap.
- Fail-closed cleanup.

### V2

- Subsystem-specific resource controllers.
- Critical-path scheduling.
- Memory-pressure-aware materialization.
- Bounded persistent cache.

---

## 33.18 Build / Reproducibility

### V1-P0

- Normalized explicit compiler inputs.
- Stable output ordering.
- No hidden time/random/path contamination.
- Repeat-build equivalence.
- Generated artifact stability testing.
- CI reproducibility checks.

### V2

- Persistent daemon.
- Shared cache.
- Remote cache/execution if justified.
- Build manifest.
- Content-addressed artifact reuse.

---

## 33.19 IDE / Tooling

### V1-P0

Compiler APIs must not be coupled to terminal output.

### V1-P1

- Machine-readable diagnostics.
- Source-manager queries.
- IR dump tooling.

### V2

- LSP.
- Incremental semantic queries.
- Live diagnostics.
- Semantic navigation/refactoring.

---

# 34. Structures V1 Should Avoid

The following decisions would make V2 significantly harder:

```text
one giant mutable CompilationContext

storing all semantic results as mutable AST fields

String-based semantic identity

global println/logger diagnostics

backend objects retained inside canonical material

cache objects becoming semantic truth

Verifier independently reconstructing Contract meaning

PBT directly traversing Verifier-private object graphs

Diagnostics re-evaluating semantic judgments

Optimizer mutating source/Contract authority objects

JVM operand-stack shape leaking into semantic middle-end design

worker completion order assigning semantic ordinals

file path / object address / hash iteration defining identity

all Whole-Machine queries requiring one eager full-world object graph

generated-test lifecycle equated with JUnit lifecycle

compiler internal resource policy equated with user Budget Contract
```

---

# 35. Candidate V2 Module Topology

Exact package names are not decided.

The intended ownership split may resemble:

```text
compiler/

    driver/
    source/
    syntax/
    symbol/

    semantic/
        definition/
        authority/
        applicability/
        occurrence/
        provenance/

    query/
        key/
        engine/
        dependency/
        fingerprint/
        incremental/

    canonical/
        identity/
        encoding/
        interning/
        snapshot/

    linking/
        unit/
        summary/
        wholemachine/

    analysis/
        shared/
        state/
        policy/
        governance/
        publication/
        capability/

    verification/
        material/
        composition/
        preservation/
        conformance/
        reference/

    testing/
        objective/
        generation/
        fixture/
        shrink/
        coverage/
        execution/

    diagnostics/
        compiler/
        projection/
        provenance/
        renderer/
        remark/
        reproducer/

    ir/
        semantic/
        machine/
        lowered/
        jvm/

    pass/
        manager/
        analysis/
        preservation/
        instrumentation/

    rewrite/
        pattern/
        legality/
        conversion/

    optimization/
        simplify/
        slicing/
        specialize/
        fusion/
        discharge/

    realization/
        enforcement/
        evidence/
        publication/
        output/

    backend/
        capability/
        jvm/
            lowering/
            analysis/
            optimization/
            classfile/

    cache/
        session/
        persistent/
        cas/

    parallel/
        workgraph/
        executor/
        publication/

    resource/
        work/
        memory/
        storage/

    tooling/
        cli/
        irtool/
        lsp/
        build/
```

The exact directory structure matters less than the ownership boundaries.

---

# 36. Core Ownership Rules

## Rule 1

```text
Contract semantics
    owns meaning
```

Compiler mechanisms do not.

## Rule 2

```text
Semantic Query Database
    owns computation and reuse
```

It does not own Canonical Contract authority.

## Rule 3

```text
Shared Analysis
    owns derived compiler facts
```

It does not redefine source authority.

## Rule 4

```text
Verifier
    owns verification products
```

PBT and Diagnostics must not treat Verifier-private representation as semantic authority.

## Rule 5

```text
Test Synthesis
    owns test objective and case products
```

A generated test does not create new Contract meaning.

## Rule 6

```text
Diagnostic subsystem
    owns explanation and projection products
```

It does not re-establish the source semantic result.

## Rule 7

```text
Optimizer
    owns realization transformations
```

It does not own Contract meaning.

## Rule 8

```text
Backend
    owns target realization
```

JVM shape is not Contract meaning.

## Rule 9

```text
Cache
    owns reusable physical storage
```

Cache presence is never correctness authority.

## Rule 10

```text
Scheduler
    owns physical work ordering
```

Physical order must not become semantic order.

---

# 37. Important V2 Invariants

Even if the final architecture changes, these invariants are strong candidates for permanence.

## 37.1 Clean / Incremental Equivalence

```text
Clean Build
    ==
Incremental Build
```

for semantic results.

## 37.2 Cache Equivalence

```text
Cache Hit
    ==
Cache Miss + Recompute
```

## 37.3 Parallel Equivalence

```text
1 Worker
    ==
N Workers
```

## 37.4 Equivalent Evidence Frontends

Two frontends establishing equivalent Contract facts should converge on the same canonical meaning.

## 37.5 Reference / Generated Equivalence

Within the supported domain:

```text
Reference Judgment
    ==
Generated Enforcement Result
```

## 37.6 Optimized / Unoptimized Equivalence

```text
Unoptimized Contract-Machine Meaning
    ==
Optimized Realization Meaning
```

## 37.7 Diagnostic Correctness

Incremental reuse, optimization, and caching must not produce stale semantic subjects or causal relations.

## 37.8 No Partial Publication

Failed compilation, linking, verification, pass execution, or backend realization must not partially enter the published
world.

---

# 38. Questions to Ask During Future V2 Design

Every new Contract or compiler subsystem should be tested against the following questions.

## Semantic

```text
Who owns this meaning?
Can it have an exact stable identity?
Are Definition and Occurrence distinct?
What is the applicable context?
What semantic dependencies exist?
```

## Compiler Representation

```text
Can this be an immutable product?
Can it become a query key/result?
Can source provenance be separated from semantic identity?
```

## Incremental

```text
What invalidates this result?
What changes do not invalidate it?
If the result fingerprint is unchanged, can propagation stop?
```

## Product Subsystems

```text
Which of Verifier / PBT / Diagnostic / Optimizer consume it?
Are they recomputing the same analysis?
```

## Parallel

```text
Can worker completion order leak into meaning?
Is there a private-candidate / publication boundary?
```

## Resource

```text
Is work bounded?
Is memory bounded?
Does cancellation leave partial state?
```

## Backend

```text
Has target-specific vocabulary leaked into semantic layers?
Is an unsupported guarantee being silently weakened?
```

## QA

```text
Can clean/incremental differential testing cover this?
Can reference/generated differential testing cover this?
Can it be fuzzed and reproduced?
```

---

# 39. Recommended V1 Implementation Order

Because Contract semantics are still being finalized, a safe implementation sequence is:

```text
1. Finalize Contract semantics
        ↓
2. SourceManager / Symbol / Provenance foundation
        ↓
3. New Semantic Contract IR
        ↓
4. Canonical Identity / Encoding
        ↓
5. Immutable Published Contract Snapshot
        ↓
6. Whole-Machine Link Boundary
        ↓
7. Shared Analysis API
        ↓
8. Query-Compatible Compiler API
        ↓
9. Contract Verifier + Reference Judgment seam
        ↓
10. PBT / Fixture / Coverage product seam
        ↓
11. Structured Compiler Diagnostics
        ↓
12. Lowered Contract-Machine IR
        ↓
13. Pass / Analysis / Verify-Each infrastructure
        ↓
14. Enforcement / Diagnostic / Output projections
        ↓
15. JVM IR / Backend
        ↓
16. Deterministic Parallel Execution
        ↓
17. Stable V1 Foundation
```

V2 can then extend with:

```text
persistent query engine
red/green invalidation
semantic early cutoff
persistent cache
summary-driven linking
parallel query evaluation
richer translation validation
richer test synthesis
IDE live analysis
shared/remote cache
```

---

# 40. Final Architecture Statement

Kontrakt V2 should not be defined merely as a fast JVM compiler.

Its architectural target is:

> **A Contract Compiler Platform that turns explicit Contract semantics into one stable Canonical Contract World,
derives verification, test synthesis, diagnostics, enforcement, optimization, publication, and backend realization as
independent compiler products, and executes those computations through deterministic queries, reusable analyses,
immutable published generations, incremental dependency tracking, bounded parallelism, and replaceable realization
layers.**

The shortest representation is:

```text
                        Explicit Contracts
                               │
                               ▼
                   Semantic Contract Compiler
                               │
                               ▼
                    Canonical Contract World
                               │
         ┌─────────────────────┼────────────────────────┐
         │                     │                        │
         ▼                     ▼                        ▼
   Verification          Test Synthesis             Diagnostics
         │                     │                        │
         ├─────────────┐       │       ┌────────────────┤
         │             │       │       │                │
         ▼             ▼       ▼       ▼                ▼
 Reference        Shared Semantic Analyses       Evidence / Explain
 Judgment
         │
         └─────────────────────┬────────────────────────┘
                               │
                        Product Projections
                               │
                 ┌─────────────┼─────────────┐
                 │             │             │
                 ▼             ▼             ▼
            Enforcement   Optimization   Publication/Output
                 │             │             │
                 └─────────────┼─────────────┘
                               ▼
                         JVM Realization
```

Under the entire platform:

```text
Stable Identity
Semantic Query Database
Dependency Tracking
Incremental Invalidation
Analysis Reuse
Pass / Rewrite Infrastructure
Immutable Published Generations
Persistent Cache
Deterministic Parallelism
Compiler Resource Discipline
Structured Diagnostics
Verification / Translation Validation
Fuzzing / Reproducer
Build / IDE Integration
```

V1 does not need to implement every V2 feature.

Its more important responsibility is:

> **Ensure that Semantic IR, subsystem ownership, identity, publication, diagnostics, verification, PBT, and backend
boundaries do not need to be dismantled when V2 capabilities are added.**

The intended relationship is:

```text
V1:
    correct boundaries
    stable identities
    explicit products
    deterministic publication
    query-compatible computation
    real verifier/PBT/diagnostic seams
    multi-level IR
    backend isolation

V2:
    fine-grained incrementality
    persistent semantic reuse
    summary-driven whole-machine compilation
    large-scale parallelism
    richer verification
    richer test synthesis
    deeper optimization
    IDE/build integration
    shared/remote cache
```

If that relationship is preserved, V1 is not a throwaway prototype.

It becomes the first implementation of the V2 commercial compiler architecture.