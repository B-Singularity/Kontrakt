# Kontrakt Query-Oriented Compiler Engine and Object-Free Core Representation

> Purpose: Design reference for implementing the Kontrakt compiler with V2 incremental compilation in mind.
>
> This document does not establish new Accepted Contract law. It summarizes the current architecture direction for:
>
> - query-oriented compiler orchestration,
> - HID-backed lookup and compact keys,
> - explicit dependency recording,
> - V1 in-memory reuse,
> - V2 persistent incremental reuse,
> - pass integration,
> - and object-free / primitive-oriented core compiler storage.

---

# 1. Core Direction

Kontrakt should be **query-oriented from V1**, rather than building the entire compiler around one global pass pipeline and attempting to retrofit a query/incremental engine later.

The recommended top-level model is:

```text
Compiler Product Request
        ↓
Query Interface
        ↓
Query Engine
        ↓
Analysis / Verification / Transform / Lowering
        ↓
Published Compiler Product
```

Examples:

```text
getOperationVerification(OperationId)

getOptimizedExecution(OperationId)

getJvmProduct(CoreId)

getDiagnostic(ResultId)

getTestPlan(ObligationId)
```

The query system owns compiler-wide:

```text
product orchestration
dependency tracking
validity
reuse
generation
fingerprint comparison
incremental recomputation
```

Passes remain local processing mechanisms inside query implementations.

---

# 2. Query and Pass Are Different Responsibilities

The query system answers:

> What compiler product or derived fact is required?

A pass answers:

> What processing step should be applied to a material unit?

Recommended relationship:

```text
Query
    ↓
local analysis / transform pipeline
    ↓
result
```

Example:

```text
getOptimizedExecution(Operation A)
        ↓
form execution IR
        ↓
Static Discharge
        ↓
Inlining
        ↓
GVN
        ↓
DCE
        ↓
IR Verification
        ↓
Optimized Execution Result
```

Therefore:

```text
Query = product / dependency / reuse orchestration

Pass = local processing unit
```

The compiler should not use a global pass manager as the main architecture for all products.

---

# 3. Why Query-Oriented V1 Is Preferable

Kontrakt does not produce only one output.

It has multiple sibling products and subsystems:

```text
Verifier
Diagnostics
PBT
Reference Judgment
Execution
Optimization
JVM Product
Whole-Machine Summary
Coverage
```

A single linear pass pipeline tends to force these into one artificial order.

A query graph models them more naturally:

```text
                 Established Contract World
                 /        |         \
                /         |          \
        Verification     PBT      Diagnostics
             |
        Execution
             |
        Optimization
             |
         JVM Product
```

Only required branches need to execute.

---

# 4. HID Is a Key Substrate, Not the Query Engine

Kontrakt already uses HID-like compact identity / lookup mechanisms.

This is highly compatible with a query engine, but the two concepts must remain separate.

```text
HID
    = compact identity lookup / candidate matching mechanism

Query Engine
    = computation + dependency + validity + reuse mechanism
```

HID does not itself provide:

```text
dependency recording
generation validity
invalidation
early cutoff
persistent reuse
query scheduling
```

Also:

```text
HID != semantic authority
```

A HID may accelerate lookup, but semantic identity remains defined by the owning semantic law.

---

# 5. Query Subsystem Must Also Separate Contract and Implementation

The Query subsystem should itself follow Kontrakt's contract-oriented architecture.

```text
Query Subsystem
│
├── Contract Plane
│   ├── query identity
│   ├── input meaning
│   ├── result meaning
│   ├── dependency semantics
│   ├── validity law
│   ├── publication law
│   └── cycle / refusal law
│
└── Implementation Plane
    ├── HID lookup
    ├── dense tables
    ├── dependency edge storage
    ├── worker scheduling
    ├── cache
    ├── persistent storage
    └── eviction
```

Concrete cache, storage, and scheduling mechanisms must not acquire semantic authority.

---

# 6. Minimal V1 Query Contract

The V1 Query Contract should establish at least the following laws.

```text
1. Every query has a stable logical identity.

2. A query result is a published compiler product or derived fact.

3. Query computation may depend only on explicit inputs or query results read during evaluation.

4. Dependencies are recorded explicitly.

5. Cached reuse must never change semantic results.

6. Query validity is bound to the exact input/material generation on which it was computed.

7. Invalidated dependencies invalidate dependent results unless an accepted preservation rule applies.

8. Result equality or a stable result fingerprint may stop further invalidation.

9. Query storage, cache layout, HID lookup, and scheduling are not semantic authority.

10. Compiler query recursion/cycles are distinct from Contract semantic cycles.
```

---

# 7. Dependency Recording Is the Most Important V1 Seam

The V1 engine should record dependencies while evaluating a query.

Example:

```text
getOperationVerification(O1)
```

reads:

```text
getFunctionSummary(F1)
getFunctionSummary(F2)
getApplicableContractMaterial(C7)
```

The engine records:

```text
OperationVerification(O1)
    depends on
        FunctionSummary(F1)
        FunctionSummary(F2)
        ApplicableContractMaterial(C7)
```

This enables V2 incremental invalidation without redesigning subsystem boundaries.

---

# 8. Contract Version as a Semantic Invalidation Input

Contract Version is one of the semantic inputs that may affect analysis, verification, execution formation, and transforms.

Example:

```text
Contract Version
        ↓
Version Binding
        ↓
Established Contract Meaning
        ↓
Analysis
        ↓
Verification
        ↓
Execution Formation
        ↓
Optimization
```

When Version changes, the compiler reevaluates only query results whose declared or recorded dependencies include:

```text
that Version
or
semantic material derived from that Version
```

This must not become:

```text
Version changed
    → invalidate the entire compiler unconditionally
```

Instead:

```text
Version change
    → dependency-driven invalidation
    → recompute affected material
    → early cutoff if derived result is unchanged
```

---

# 9. Contract Dependency Graph and Query Dependency Graph Must Remain Separate

These are not the same graph.

Example of Contract semantic dependency:

```text
Invariant B requires established Fact A
```

Example of compiler computation dependency:

```text
OperationVerification(O)
    requires FunctionSummary(F)
```

Therefore:

```text
Contract Graph
    = semantic authority / required basis

Query Graph
    = compiler recomputation dependency
```

The Query engine may observe Contract relations and record computational dependencies, but:

```text
Query Graph != Contract semantic authority
```

---

# 10. Query Granularity

A query should not be too coarse:

```text
compileWholeProject()
```

because V2 incremental reuse becomes ineffective.

It should also not be excessively fine-grained:

```text
getInstructionOperand(123)
```

because query overhead and dependency volume become too high.

Recommended initial granularity for Kontrakt:

```text
Definition
Occurrence
Function
SCC
Operation
Core
Whole-Machine Summary
Execution Product
Diagnostic Product
PBT Plan
JVM Product
```

Exact granularity should be decided per subsystem.

---

# 11. Candidate V1 Query Families

## Contract-Side Queries

```text
resolveDefinition(DefinitionId)

establishDefinition(DefinitionId)

establishOccurrence(OccurrenceId)

getApplicableContractMaterial(OccurrenceId)
```

## Realization-Side Queries

```text
acquireFunction(FunctionId)

analyzeFunction(FunctionId)

summarizeScc(SccId)

verifyOperation(OperationId)

summarizeCore(CoreId)
```

## Execution / Backend Queries

```text
formExecution(OperationId)

optimizeExecution(OperationId)

lowerJvm(CoreId)

emitJvmProduct(CoreId)
```

## Product Queries

```text
buildDiagnostic(ResultId)

buildTestPlan(ObligationId)

buildCoverage(ContractUnitId)
```

These names are design examples, not final API commitments.

---

# 12. Pull-Based Product Creation

The V1 compiler should prefer pull-based product creation.

Example:

```text
getJvmProduct(Core A)
```

causes only the upstream products required for that JVM product to be evaluated.

If the user does not request PBT generation:

```text
PBT materialization
```

does not need to run.

If only diagnostics are needed:

```text
diagnostic-related upstream work
```

can be evaluated without forcing unrelated backend products.

---

# 13. V1 Query Engine Can Be Small

V1 does not need a complete rustc-style persistent incremental engine.

A minimal V1 engine can contain:

```text
Query Identity
Query State
Query Result Reference
Dependency Recording
Generation
Fingerprint
In-Memory Cache
```

Conceptually:

```text
query(key):
    if cached result is valid:
        return cached result

    begin dependency recording

    result = compute(key)

    fingerprint result

    publish result

    record dependencies

    return result
```

The architecture seam matters more than implementing every V2 feature immediately.

---

# 14. V2 Extends the Same Query Contracts

V2 should extend the same architecture with:

```text
persistent result cache
persistent dependency graph
cross-session fingerprints
red-green validation
early cutoff
immutable generations
summary-first loading
lazy materialization
parallel demand scheduling
optional remote cache
```

The goal is:

```text
V1
    = in-memory, generation-bound query execution

V2
    = persistent incremental query execution
```

without changing the logical Query Contracts.

---

# 15. Early Cutoff

Example:

```text
Input changed
    ↓
query recomputed
    ↓
new result fingerprint == old result fingerprint
```

Then downstream consumers may remain valid.

Example:

```text
Function body changed
    ↓
FunctionSummary recomputed
    ↓
FunctionSummary unchanged
    ↓
OperationVerification need not recompute
```

This is one of the most important V2 capabilities.

---

# 16. Query Cycle Handling Is Not Contract Cycle Handling

Kontrakt Contract semantic cycles remain illegal where already decided.

Example:

```text
Contract A requires B
Contract B requires A
```

is a Contract compile error.

Compiler analysis recursion may still be valid.

Example:

```text
Function A calls B
Function B calls A
```

may be analyzed through:

```text
SCC condensation
fixed-point summary analysis
```

Therefore:

```text
Contract semantic cycle
    = semantic illegality

Compiler query / analysis recursion
    = implementation / computation concern
```

The Query engine must not conflate the two.

---

# 17. Object-Free Core Compiler Policy

Kontrakt should avoid high-cardinality JVM object graphs in hot compiler paths.

The intended rule is not:

```text
the compiler may never allocate an object
```

The intended rule is:

> Logical compiler entities do not imply heap object identity, and high-cardinality semantic / analysis material should use compact index-addressable storage by default.

Avoid patterns such as:

```text
1 Definition = 1 object
1 Occurrence = 1 object
1 IR op = 1 object
1 SSA value = 1 object
1 Query = 1 object
1 Dependency = 1 object
1 Analysis Result = 1 object
```

These create:

```text
object headers
pointer chasing
allocation traffic
GC scanning
boxing
iterator allocation
virtual dispatch
poor locality
```

---

# 18. Core Areas That Should Prefer Primitive / Table-Oriented Storage

The following areas should strongly prefer compact primitive / table / slab representation:

```text
Established semantic material
IR
Query engine
Dependency graph
Analysis results
CFG
SSA values
Call graph
SCC graph
Verification results
Optimizer metadata
HID / index tables
Incremental fingerprints
Whole-Machine summaries
```

---

# 19. Logical Entity Does Not Mean JVM Object

For example, logically the compiler may have:

```text
Query
Result
Dependency
Generation
Fingerprint
```

but physically these do not need to be:

```text
Query object
Result object
Dependency object
Generation object
Fingerprint object
```

A table-oriented representation is preferable.

---

# 20. Candidate Query Table Layout

Instead of:

```text
Map<QueryKey, QueryResult>
List<Dependency>
```

a compact representation may use:

```text
queryKind[]
keyHidHi[]
keyHidLo[]
generation[]
state[]
resultRef[]
fingerprintHi[]
fingerprintLo[]
firstDependency[]
dependencyCount[]
```

Dependency edges:

```text
dependencyQueryRef[]
```

A `QueryRef` may simply be a dense integer handle.

---

# 21. HID to Dense Internal Reference

A useful hot-path flow is:

```text
Semantic / Compiler Identity
        ↓
HID candidate lookup
        ↓
exact validation if required
        ↓
dense internal ref
```

Example:

```text
HID
    ↓ lookup
QueryRef = 8241
```

Then the hot path uses:

```text
queryState[8241]
resultRef[8241]
dependencyStart[8241]
dependencyCount[8241]
```

rather than carrying a wide identity object through every operation.

---

# 22. Dense Reference Is Not Semantic Identity

This distinction must remain explicit.

```text
dense ref
ordinal
table offset
HID
```

are physical mechanisms.

They must not become semantic identity.

Therefore:

```text
QueryRef == 8241
```

means:

```text
row 8241 in this physical query table
```

not:

```text
semantic identity 8241
```

---

# 23. Candidate CFG / SSA Physical Direction

Object-heavy CFG:

```text
BasicBlock object
    List<Instruction>
    List<BasicBlock> successors
```

can eventually be represented as:

```text
blockFirstOp[]
blockOpCount[]

opCode[]
opFirstOperand[]
opOperandCount[]

blockFirstEdge[]
blockEdgeCount[]
edgeTarget[]
```

SSA value / use material may similarly use:

```text
valueDefOp[]
valueType[]
useStart[]
useCount[]
useData[]
```

The logical CFG / SSA Contract remains independent from this storage.

---

# 24. Why V1 Should Already Avoid Object-Centric Core APIs

A common migration strategy is:

```text
V1 = object graph
V2 = primitive slab
```

For Kontrakt's core compiler representations, this is risky.

If V1 spreads APIs such as:

```text
query.result.dependencies
irNode.parent
instruction.operands
analysisResult.values
```

throughout the compiler, converting to table / slab storage later becomes a broad rewrite.

Therefore:

> V1 should already avoid exposing object topology as the logical API of core compiler material.

Even if an early implementation temporarily uses objects internally, the Contract / access surface must not depend on them.

---

# 25. Object Use Is Still Acceptable in Cold / Low-Cardinality Areas

Objects are not categorically forbidden.

They may be reasonable for:

```text
CompilerDriver
CLI configuration
build orchestration
rare configuration descriptors
cold diagnostic renderer objects
one-per-service stateless helpers
low-cardinality plugin descriptors
temporary non-hot presentation objects
```

The main prohibition is against object-per-semantic-unit design in hot / high-cardinality paths.

---

# 26. Candidate Object-Free Architecture Law

```text
Logical compiler entities do not imply heap object identity.

High-cardinality compiler material is represented by compact,
index-addressable storage by default.

Object identity, allocation topology, and host-language reference
structure do not participate in compiler semantic identity.

Object-oriented representations may be used only where cardinality,
lifetime, and access frequency make their cost irrelevant or where
they remain outside hot semantic and analysis paths.
```

---

# 27. Query Contract vs Query Storage

Logical Query Contract:

```text
Query Identity
Required Inputs
Result Meaning
Validity
Dependencies
Publication
```

Physical implementation:

```text
primitive query table
flat dependency edge slab
HID index
generation array
fingerprint words
result handles
cache metadata
```

The logical Contract must not expose physical storage topology.

---

# 28. Query Result Storage Should Be Typed by Product Family

Avoid a universal object such as:

```text
QueryResult {
    Object payload
}
```

Instead, query rows should point to typed product storage.

Example:

```text
resultKind[]
resultRef[]
```

where `resultRef` addresses:

```text
FunctionSummaryTable
VerificationTable
ExecutionProductTable
DiagnosticTable
JvmProductTable
```

depending on `resultKind`.

This preserves compactness and subsystem ownership.

---

# 29. Generation Model

A query result is valid only against the material generation from which it was derived.

Example:

```text
Execution IR
Generation 17

    ├── Dominance@17
    ├── OriginAnalysis@17
    └── Verification@17
```

After transform:

```text
Execution IR
Generation 18
```

the old results are not automatically valid.

A transform may explicitly preserve selected analyses.

---

# 30. Generation Storage Can Remain Primitive

Generation identity does not require an object.

Example:

```text
materialGeneration[]
analysisGeneration[]
queryGeneration[]
```

may use integer or fixed-width primitive storage.

The logical validity law remains independent from the numeric encoding.

---

# 31. Fingerprint Model

Fingerprints support:

```text
cache lookup
result comparison
early cutoff
persistent reuse
```

but must not become semantic authority.

Candidate physical form:

```text
fingerprintHi[]
fingerprintLo[]
```

or another fixed-width representation.

Where collision risk affects correctness:

```text
fingerprint match
    → candidate equality
    → exact or stronger validation when required
```

The exact final law depends on the product.

---

# 32. Analysis and Transform Integration

Analysis queries may produce derived knowledge:

```text
getDominance(Function)
getOriginSummary(Function)
getSccSummary(SCC)
```

Transform queries may consume those results:

```text
getOptimizedExecution(Operation)
```

and internally run:

```text
Static Discharge
Inlining
GVN
DCE
```

Transforming material produces a new generation.

Analysis validity is then preserved or invalidated explicitly.

---

# 33. Query Engine Should Not Become a Semantic Authority

A dangerous architecture is:

```text
query cache contains result
    therefore result is true
```

The correct direction is:

```text
compiler subsystem establishes result according to its Contract
        ↓
Query engine stores / reuses that result
```

The Query engine owns computation management, not semantic authority.

---

# 34. Query Engine Should Not Infer Undeclared Semantic Dependencies

The engine may record:

```text
query A called query B
```

as a computational dependency.

It must not invent Contract semantic relations from execution topology.

For example:

```text
Query X reads Y
```

does not imply:

```text
Contract X semantically depends on Contract Y
```

This is the same separation required between:

```text
Required Basis
```

and:

```text
compiler recomputation dependency
```

---

# 35. Parallel Query Evaluation

V2 may schedule independent ready queries in parallel.

The architecture should prefer:

```text
immutable published input
worker-local scratch
worker-local result construction
deterministic publication / merge
```

rather than:

```text
global mutable object graph
global ConcurrentHashMap of mutable results
```

Worker completion order must not determine semantic identity or required user-visible order.

---

# 36. Candidate Primitive-Oriented Dependency Storage

Instead of:

```text
DependencyNode objects
```

use flat edge storage.

Example:

```text
queryFirstDep[]
queryDepCount[]
depTargetQueryRef[]
```

Reverse dependencies for invalidation may use a second table:

```text
queryFirstReverseDep[]
queryReverseDepCount[]
reverseDepSourceQueryRef[]
```

or be generated / indexed according to the selected V1 / V2 strategy.

---

# 37. V1 Does Not Need Full Persistent Machinery

Recommended V1 implementation scope:

```text
in-memory query registry
dense query refs
generation-bound result validity
explicit dependency recording
stable fingerprints where useful
deterministic publication
typed result tables
```

Optional V1 additions:

```text
selected persistent summaries
selected JVM product cache
```

Do not force a complete persistent database into V1 if it delays correctness architecture.

---

# 38. V2 Target

V2 should add:

```text
cross-session query state
persistent dependency records
persistent fingerprints
red-green validation
fine-grained early cutoff
immutable compiler generations
lazy material loading
summary-first whole-machine analysis
parallel demand scheduling
product-specific persistent caches
```

without changing the Query Contract Plane.

---

# 39. Recommended V1 / V2 Boundary

```text
V1:
    Query-Oriented Architecture
    In-Memory Dependency Recording
    Generation Validity
    Primitive-Oriented Storage
    HID-Based Lookup
    Typed Product Tables
    Deterministic Evaluation

V2:
    Persistent Query State
    Cross-Session Reuse
    Red-Green
    Early Cutoff
    Immutable Multi-Generation Reuse
    Lazy Materialization
    Parallel Demand Scheduling
```

This is preferable to:

```text
V1:
    global pass pipeline + object graph

V2:
    rewrite compiler around query engine
```

---

# 40. Architecture Summary

Recommended overall model:

```text
                         QUERY SUBSYSTEM
                               │
                    product / dependency / reuse
                               │
       ┌───────────────────────┼────────────────────────┐
       │                       │                        │
       ▼                       ▼                        ▼
   Analysis                Verification             Products
       │                       │                        │
       └──────────────┐        │                        │
                      ▼        ▼                        │
                    Execution Formation                │
                           │                           │
                           ▼                           │
                     Transform Passes                  │
                           │                           │
                           ▼                           │
                       JVM Lowering ───────────────────┘
```

Cross-cutting physical substrate:

```text
HID lookup
dense refs
primitive tables
flat dependency slabs
generation arrays
fingerprint arrays
typed result storage
```

---

# 41. Key Design Laws

```text
Query-oriented does not mean object-oriented.

HID-backed does not mean HID-defined semantics.

A query result is a compiler product, not semantic authority.

Dependency recording is computational, not Contract-semantic authority.

Passes remain local processors inside query implementations.

V1 should already expose query-oriented logical boundaries.

V1 should already avoid object-topology-dependent core APIs.

High-cardinality compiler material should be index-addressable and
primitive / table-oriented by default.

V2 should extend validity and persistence, not replace the V1 architecture.
```

---

# 42. Implementation Review Checklist

Before implementing the Query engine:

```text
[ ] Is Query identity defined logically before physical key layout?

[ ] Is HID used only as lookup / key substrate?

[ ] Is semantic identity independent from QueryRef / ordinal / table row?

[ ] Are dependencies recorded explicitly?

[ ] Are Contract semantic dependencies separate from Query dependencies?

[ ] Is result validity generation-bound?

[ ] Can result equality / fingerprint support early cutoff?

[ ] Are result products stored in typed tables rather than Object payloads?

[ ] Does the query API avoid exposing object topology?

[ ] Are high-cardinality Query / Dependency / Result entities object-free?

[ ] Can the V1 engine run entirely in memory?

[ ] Can the same Query Contracts support persistent V2 reuse?

[ ] Can passes run inside query implementations without becoming global orchestration authority?

[ ] Can worker-local evaluation be added without changing semantic identity?

[ ] Does cache presence never change compiler semantics?
```

---

# 43. Status

This document captures the current design direction.

Items still requiring dedicated design work:

```text
- exact Query identity schema
- exact HID / dense-ref lookup path
- query state machine
- dependency recording representation
- reverse dependency index strategy
- generation model
- fingerprint law per product family
- result equality / early-cutoff rules
- query cycle handling
- parallel evaluation model
- V1 persistent-cache scope, if any
- V2 persistent storage format
- exact typed result tables
- interaction with IR Contract / Lowering Contract subsystem
- integration with compiler diagnostics and self-profile
```
