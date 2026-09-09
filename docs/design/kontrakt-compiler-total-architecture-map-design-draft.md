# Kontrakt Compiler Architecture Map

## Status

**Working design draft. Not an ADR.**

This document gives one concise view of the current Kontrakt compiler architecture.

It does not define new Contract semantics.

`What Contract Is` and Accepted ADRs remain authoritative.

The purpose is to keep the whole V1 compiler visible before individual frontend, IR, verifier, optimizer, backend, QA,
and V2 decisions are finalized.

---

# 1. Core Direction

Kontrakt separates Contract meaning from compiler realization.

```text
Contract
    ↓ constrains
Realization
```

Compiler structures may represent, verify, optimize, lower, cache, or publish Contract meaning.

They do not define it.

```text
JVM shape
IR storage
query graph
cache
object topology
backend layout
    ≠
Contract authority
```

---

# 2. Stage and IR Rule

A compiler stage exists because a new invariant becomes true.

A new IR level is justified when one of these changes:

```text
semantic vocabulary
equivalence relation
persistent invariant
information that may now be discarded
target vocabulary
```

The following alone do not justify a new IR level:

```text
verification completed
optimization ran
material was frozen
storage changed
a cache entry was created
a generation changed
```

Logical stages and physical materialization are separate.

---

# 3. Total Architecture

The compiler has a semantic material graph and a build / product dependency graph.

They are related.

They are not the same graph.

```text
┌───────────────────────────────────────────────────────────────────────────┐
│                        Compiler Driver / Session                          │
│ request / target / product demand / resources / generations / reuse      │
│ worker ownership / cancellation / diagnostics / artifact publication      │
└──────────────────────────────────┬────────────────────────────────────────┘
                                   │
                  ┌────────────────┴────────────────┐
                  │                                 │
                  ▼                                 ▼
          Contract Frontend                 Realization Acquisition
                  │                                 │
             .kontrakt                     User JVM classfiles
                  │                                 │
      Source / Syntax / Recovery                    │
                  │                                 ▼
             Resolution                    Realization Body IR
                  │                                 │
        Resolved Contract HIR              Local Structural Analysis
                  │                                 │
      Authority-Owned Establishment                 │
                  │                                 │
                  ▼                                 │
       Canonical Contract World                     │
                  │                                 │
                  │                    Admitted Realization Binding
                  │                                 │
                  └────────────────┬────────────────┘
                                   ▼
                        Contract-Aware Analysis
                                   │
                  ┌────────────────┼────────────────┐
                  │                │                │
                  ▼                ▼                ▼
         Closure Verification  Specialization  Whole-Machine /
                  │             Knowledge       IPA Knowledge
                  ▼                │                │
        Verification Overlay      └────────┬───────┘
                  │                       │
                  └───────────────┬───────┘
                                  ▼
                         Execution Formation
                                  │
                     static alternatives omitted
                     runtime judgments retained
                                  │
                                  ▼
                    Contract-Aware Execution IR
                                  │
                        Analysis / Transform Loop
                                  │
                                  ▼
                     Execution IR New Generation
                                  │
                                  ▼
                            JVM Plan / IR
                                  │
                                  ▼
                          Classfile Product
                                  │
                                  ▼
                           HotSpot / Graal
```

The build / product dependency has an additional edge.

Generated host APIs are compiler products.

User implementation compilation consumes those products.

```text
.kontrakt
    ↓
Contract Frontend
    ↓
Canonical Contract World
    ↓
Generated Operation / Interaction API Product
    ↓
Host Compiler
    ↓
User Classfiles
    ↓
Realization Acquisition
```

A clean build therefore does not assume that the two frontend paths are fully independent.

Some work may overlap when a compatible generated API product and user classfile product already exist.

That is a scheduling opportunity.

It is not a semantic dependency rule.

`Canonical Contract World` must exist before Contract-aware verification or optimization can use Contract meaning.

---

# 4. Main Material Flow

The whole compiler can be reduced to a small set of major material families.

```text
Contract Source
    ↓
Resolved Contract HIR
    ↓
Canonical Contract World

User Realization
    ↓
Realization Body IR
    ↓
Admitted Realization Binding

Canonical Contract World
+
Realization Body IR
+
Admitted Realization Binding
+
Derived Knowledge
    ↓
Verified Realization
    ↓
Contract-Aware Execution IR
    ↓
JVM Plan / IR
    ↓
Classfile
```

The exact number of physical representations remains open.

The build / product graph may contain more products than this semantic material flow.

---

# 5. Contract Frontend

The Contract frontend converts authored Contract source into resolved semantic material.

```text
.kontrakt
    ↓
Source Manager / Provenance
    ↓
Syntax / Recovery Material
    ↓
Module / Name / Symbol Resolution
    ↓
Resolved Contract HIR
```

Its main outputs are:

```text
source identity
source provenance
parsed Contract structure
recovery / poison state where required
exact authority references
resolved slot bindings
resolved semantic relations
module / import resolution
```

Source location is not semantic identity.

Parser recovery does not establish Contract meaning.

Recovered or poisoned source material must not silently become authoritative semantic material.

Downstream consumers should not repeat source-name lookup.

The frontend language version is separate from Contract Version.

```text
IDL Language Version
    = grammar and frontend-language compatibility

Contract Version
    = Contract semantic coordinate
```

Feature gates, language compatibility, source spans, and origin chains belong to frontend infrastructure.

They do not define Contract meaning.

---

# 6. Resolved Contract HIR

`Resolved Contract HIR` is the high-level semantic representation before Establishment.

It preserves rich Contract vocabulary.

It may represent exact references to:

```text
Input
Admission
Canonicalization
Lowering
Fact
Invariant
State / Transition
Budget
Capacity
Version
Policy
Governance
Failure
Publication
Output
```

The main invariant is:

```text
source ambiguity resolved
```

The important limit is:

```text
Resolved Contract HIR
    ≠
Contract authority
```

Resolution does not establish Contract meaning.

---

# 7. Authority-Owned Establishment

Establishment is a Contract semantic boundary.

It is not ordinary compiler lowering.

```text
Resolved Contract HIR
    ↓
Required Basis complete
    ↓
Owning Contract Law
    ↓
Establishment
    ↓
Established Definition Material
```

Each authority keeps its own meaning.

Kontrakt must not replace all authorities with one universal `EstablishedMaterial` model.

---

# 8. Canonical Contract World

The `Canonical Contract World` is the compiler substrate for already-established Contract definition meaning.

It is not an ordinary optimization IR.

It exposes exact authoritative material and relations to downstream consumers.

Working read surface:

```text
exact definitions
exact semantic relations
Basis relations
Applicability relations
Version-aware meaning
Policy / Governance context
State surface
source provenance references
```

Occurrence material remains separate unless the owning Contract defines occurrence meaning.

---

# 9. Frozen Publication

Establishment and freezing are different.

```text
Establishment
    = Contract authority boundary

Freeze / Seal / Publish
    = compiler publication boundary
```

Working publication pattern:

```text
private construction
    ↓
verification
    ↓
seal / freeze
    ↓
publish
    ↓
read-only consumers
```

A separate `Frozen IR` is not required merely because material is immutable.

Existing HID, frozen publication, dense storage, and slab work may be reused behind this boundary.

---

# 10. Canonical Contract World Products

The Canonical Contract World feeds multiple sibling products.

```text
Canonical Contract World
    ├── Reference Judgment
    ├── PBT / Fixture / Unit-Test Planning
    ├── Contract Coverage
    ├── Diagnostics
    ├── Generated APIs
    ├── Contract-Aware Analysis
    └── Execution Formation
```

These products do not define one another.

```text
Verifier
    ≠ PBT authority

Diagnostics
    ≠ backend authority

Reference Judgment
    ≠ Contract authority
```

---

# 11. Realization Frontend

The realization frontend acquires the user implementation as compiler facts.

A classfile-centered V1 path is the production direction.

```text
Generated Operation / Interaction API
    ↓
Host compilation
    ↓
User JVM classfiles
    ↓
Realization acquisition
    ↓
Published Realization Body IR
```

External composition and implementation binding are separate from classfile acquisition.

```text
External Composition / DI
        ↓
Effective implementation binding
        ↓
Realization Admission Boundary
        ↓
Admitted Realization Binding
```

The admission boundary is also the realization airlock.

External technology may exist outside the governed core through explicit adapters.

Hidden technology must not become an undeclared factual ingress into the governed realization.

Examples include:

```text
reflection / dynamic proxy
MethodHandle / invokedynamic
JNI / FFM downcall
filesystem / clock / network / DB access
dynamic class loading
framework interception
```

The exact supported feature matrix remains a verifier decision.

The architecture rule is stable:

```text
external technology
    ↓
explicit adapter / composition boundary
    ↓
realization admission
    ↓
inspectable governed realization
```

The exact acquisition mechanism remains replaceable.

User implementation structure does not create Contract meaning.

---

# 12. Realization Body IR

The Realization Body IR is the published analyzable representation of user implementation structure.

It exists for implementation analysis and verification.

It should expose enough structure for:

```text
control flow
value definition/use
calls
effects
origin
unsupported constructs
```

A working representation may contain:

```text
method/type tables
per-function CFG
value relations
call/effect sites
summary references
```

SSA is not fixed yet.

The important requirement is explicit analyzable control and data relation.

Acquisition-local mutable state is not the published realization product.

Publication should produce stable read-only realization material before shared analysis consumes it.

---

# 13. Early Realization Processing

Some work can happen before Contract-aware verification.

Examples:

```text
classfile normalization
structural validation
CFG formation
def-use construction
obvious constant cleanup
contract-independent canonicalization
```

This work must not hide a realization violation.

Aggressive Contract-based elimination should not occur before the Contract context and required legality are known.

The factual realization should remain recoverable for verification and diagnostics.

---

# 14. Local Structural Analysis

Realization-local analysis does not require Contract meaning.

Possible products include:

```text
reachability
dominance
call graph
call targets
SCC
raw effect summary
raw origin summary
def-use
basic escape information
```

Analysis identity should include its logical subject and validity context.

A working view is:

```text
analysis kind
+
subject / scope
+
analysis context
+
input generation / identity
```

Possible scopes include:

```text
method
Operation realization
Core
Whole-Machine
```

These are derived compiler knowledge.

They are not Contract authority.

---

# 15. Contract-Aware Analysis

Contract-aware analysis begins only after both sides are available.

```text
Canonical Contract World
+
Realization Body IR
+
Local Structural Analysis
    ↓
Contract-Aware Analysis
```

This is a central Kontrakt middle-end boundary.

It may derive:

```text
fixed Policy World context
fixed Contract Version context
Governance Binding context
known applicable State surface
exact admitted realization binding
Contract-relative effect classification
Contract-relative origin classification
known impossible alternatives
static judgment candidates
specialization contexts
closed call-target knowledge
```

This knowledge is shared by verification, execution formation, and optimization.

---

# 16. Core Realization Closure Verification

Closure verification depends on established Contract meaning.

```text
Canonical Contract World
+
Realization Body IR
+
Admitted Realization Binding
+
valid Contract-Aware Analysis
    ↓
Core Realization Closure Verification
```

Possible results:

```text
verified
proven violation
unsupported / inconclusive
```

Closure reasoning must distinguish different target classes.

```text
exact admitted target
known finite target set
open / unresolved virtual target
opaque external target
unsupported dynamic target
```

Unknown optimization opportunity and unknown verification evidence are different.

```text
optimizer cannot prove exact target
    → keep dynamic behavior

verifier cannot prove required closure
    → may refuse under the V1 support rule
```

Reflection, native calls, dynamic loading, framework interception, and other opaque capabilities therefore need an
explicit support rule.

Under the selected V1 support rule, unsupported realization may cause compile refusal.

Verification machinery remains compiler realization.

It does not define Contract meaning.

---

# 17. Verification Overlay

Verification does not require a full second IR.

A working model is:

```text
Realization Body IR Generation G
+
Verification Result / Overlay
```

The overlay may publish verified summaries such as:

```text
exact call target
closed reachable graph
no external factual dependency
non-escaping value
stable realization binding
origin/effect proof result
```

Downstream consumers should reuse valid verified knowledge instead of repeating closure analysis.

---

# 18. Specialization Knowledge

Some established Contract context is fixed before governed execution.

Examples:

```text
Policy World
Contract Version
Governance Binding
known State surface
exact realization binding
```

These are strong specialization inputs.

Example:

```text
selected Policy World = A
    ↓
Policy World B impossible
```

The compiler may use this knowledge to avoid materializing B-specific execution machinery.

The optimizer consumes the selection.

It does not make the selection.

---

# 19. Formation-Time Pruning

Not every removable path needs to be generated and later deleted.

When established Contract meaning already proves an alternative impossible:

```text
static Contract context
    ↓
Execution Formation
    ↓
only applicable execution material
```

This is better understood as formation-time pruning or specialization.

It is not general DCE.

Examples:

```text
unselected Policy worlds
impossible Version alternatives
unselected Governance bindings
statically impossible Contract machinery
known State-surface alternatives
```

---

# 20. CFG-Based Elimination

Some dead paths cannot be removed from Contract meaning alone.

They require realization control-flow analysis.

Typical flow:

```text
Realization CFG
+
fixed Contract context
    ↓
constant / applicability propagation
    ↓
infeasible edge
    ↓
CFG simplification
    ↓
unreachable block removal
```

The CFG therefore remains an important proof and optimization substrate.

---

# 21. General DCE

General dead-code elimination usually needs more than Contract context.

Typical knowledge:

```text
CFG
def-use
liveness
effect knowledge
reachability
```

DCE may run several times.

```text
specialization
    ↓
constant propagation
    ↓
CFG simplification
    ↓
DCE
    ↓
inlining
    ↓
new dead material
    ↓
DCE
```

The exact pass order remains open.

---

# 22. Execution Formation

Execution Formation is where established Contract meaning and verified user realization become executable compiler
material.

```text
Canonical Contract World
+
Verified Realization
+
Specialization Knowledge
    ↓
Execution Formation
    ↓
Contract-Aware Execution IR
```

Execution Formation may already:

```text
omit impossible alternatives
resolve exact bindings
materialize only runtime-required judgments
preserve exact authority references
```

It should not lower away high-level Contract knowledge too early.

---

# 23. Contract-Aware Execution IR

This IR is distinct from both the Canonical Contract World and the Realization Body IR.

It represents executable Contract-aware semantics.

Possible vocabulary includes:

```text
runtime Contract judgment
realization call
value
control flow
Failure relation
State movement reference
Publication relation
Output relation
exact Contract authority reference
```

Important distinctions remain visible.

```text
State Transition
    ≠ CFG edge

Contract Failure
    ≠ JVM exception

Publication
    ≠ return instruction
```

JVM stack/local details do not belong here.

---

# 24. Execution IR Analysis

Execution IR has its own analysis layer.

Possible analyses include:

```text
CFG reachability
dominance
SSA / value relations where useful
effect refinement
alias / escape where justified
Contract-context propagation
specialization opportunities
cost inputs
```

Analysis results have explicit validity.

They are reusable only while their input generation remains valid.

---

# 25. Contract-Specific Optimization

Kontrakt should optimize where it has knowledge that the JVM does not naturally have.

Candidate V1 transformations include:

```text
static judgment discharge
fixed-context specialization
exact binding
unreachable Contract-path removal
generated wrapper removal
temporary carrier elimination
simple stage fusion
dead realization alternative removal
cheap verification-derived scalarization
```

These transformations preserve Contract meaning.

They do not establish new Contract meaning.

---

# 26. Generic Cleanup

Contract-specific transforms may expose ordinary compiler opportunities.

Examples:

```text
constant propagation
CFG simplification
DCE
small inlining
allocation removal
GVN-like simplification where useful
```

Kontrakt should use generic cleanup when it is cheap and useful.

It should not attempt to reimplement the full HotSpot/Graal optimizer.

---

# 27. Legality and Profitability

Every optimization has two separate decisions.

```text
Legality
    = does it preserve required meaning?

Profitability
    = is the legal transform worth applying?
```

A cost model cannot make an illegal transform legal.

Runtime profile information is not static Contract truth.

---

# 28. Analysis Reuse and Invalidation

Analysis is shared while valid.

```text
Material Generation G
    ↓
Analysis Result
    ├── Verifier
    ├── Optimizer
    └── other consumers
```

After a transform, an analysis result has three possible validity paths.

```text
preserved
    → reuse

incrementally maintained
    → publish updated analysis result

not preserved
    → invalidate
```

Invalidated analysis may be recomputed on demand.

Stale analysis is not valid compiler knowledge.

The exact Analysis Manager implementation remains open.

V2 may use domain-specific repair rather than one universal invalidation algorithm.

---

# 29. Optimization Generations

Optimization does not automatically create a new IR level.

```text
Execution IR Generation N
    ↓
meaning-preserving transform
    ↓
Execution IR Generation N+1
```

If vocabulary and equivalence remain the same, both generations satisfy the same Execution IR Contract.

---

# 30. Whole-Machine Analysis

Whole-Machine work should not require one giant full IR.

Working direction:

```text
Core A Summary ┐
Core B Summary ├──→ Whole-Machine Summary / Index
Core C Summary ┘
```

The summary is derived compiler knowledge.

It is not Contract authority.

Whole-Machine analysis may support:

```text
closure decisions
cross-Core dependency reasoning
fixed-context specialization
global target pruning
local optimization decisions
```

The summary should also drive selective body opening.

```text
Whole-Machine Summary / Index
        ↓
global decision
        ↓
required body set
        ↓
selective materialization
        ↓
local / parallel work
```

Full body material is opened only where needed.

Summary identity and body identity remain separate.

---

# 31. Verification and Optimization Summaries

Verification and optimization may need different summaries.

```text
Verification Summary
    = soundness-critical verifier knowledge

Optimization Summary
    = transform planning knowledge
```

They may share backing storage.

They should not be treated as one authority.

---

# 32. JVM Backend Boundary

JVM-specific lowering begins after Contract-specific simplification.

```text
Optimized Contract-Aware Execution IR
    ↓
JVM Capability / Legalization
    ↓
JVM Plan / IR
```

The backend does not re-resolve Contract meaning.

It lowers already-resolved execution material into JVM vocabulary.

Kontrakt owns the correctness of the classfile product it emits.

A JVM verifier, class loader, JIT, or external classfile API is not Contract authority and is not a substitute for
Kontrakt backend correctness.

External encoding facilities may remain replaceable adapters.

JDK or third-party object models should not become the semantic model of the backend.

---

# 33. JVM Plan / IR

The JVM Plan / IR is target-specific.

A useful logical unit is a typed JVM method plan.

Possible vocabulary:

```text
JVM value forms
invocation forms
branches
returns
throws
locals
exception regions
CFG relations
type states
required frame states
constant-pool references
classfile constraints
```

The same backend analysis should be reused where possible.

```text
JVM legalization / planning
    ↓
Typed JVM Method Plan
    ├── instructions
    ├── CFG
    ├── type states
    ├── exception edges
    └── required frame states
```

This avoids discarding compiler knowledge and reconstructing it later only for classfile completion.

This is a separate logical level because target vocabulary has changed.

The exact physical schema remains open.

---

# 34. Classfile Emission

The V1 production backend direction is direct classfile construction.

```text
Typed JVM Method Plan
    ↓
Classfile Construction
    ├── bytecode
    ├── StackMapTable
    ├── exception table
    ├── constant pool
    └── attributes
    ↓
internal structural / compliance checks
    ↓
classfile encoding
    ↓
valid JVM classfile
    ↓
JVM verifier / loader
```

Frame and metadata derivation should reuse JVM planning facts where possible.

It should not require an avoidable second reconstruction of CFG and type-state knowledge.

A generated Java / Kotlin source backend may exist as:

```text
bootstrap path
reference backend
debug path
differential-testing backend
```

It is not the V1 production backend baseline.

The exact direct encoder remains replaceable.

---

# 35. JVM Handoff

Kontrakt should perform semantic simplification that depends on Kontrakt knowledge.

The JVM should keep its strengths.

Kontrakt:

```text
Contract specialization
static discharge
exact Contract binding
generated machinery simplification
```

HotSpot / Graal:

```text
runtime speculation
profile-guided inlining
generic escape analysis
register allocation
instruction selection
machine optimization
```

Target-aware physical planning remains a separate derived optimization seam.

```text
Derived Access Profile
+
Target Hardware Profile
    ↓
Physical Layout Plan
    ↓
FFM-backed / slab realization
```

Target layout does not become Contract meaning.

The target profile is not assumed to be the compiler host profile.

---

# 36. IR Classification

| Material                        |                   IR? | Role                                              |
|---------------------------------|----------------------:|---------------------------------------------------|
| Source / Syntax Material        | source representation | authored structure                                |
| **Resolved Contract HIR**       |               **Yes** | resolved Contract semantics before Establishment  |
| **Canonical Contract World**    |                **No** | established authority substrate                   |
| Frozen World Generation         |                    No | publication state                                 |
| **Realization Body IR**         |               **Yes** | analyzable user realization                       |
| Local / Contract-Aware Analysis |                    No | derived compiler knowledge                        |
| Verification Overlay            |                    No | verified property over one realization generation |
| **Contract-Aware Execution IR** |               **Yes** | executable Contract + realization representation  |
| Optimized Execution Material    |       usually same IR | new Execution IR generation                       |
| Whole-Machine Summary           |                    No | derived global index                              |
| **JVM Plan / IR**               |               **Yes** | target-specific representation                    |
| Classfile                       |                    No | target artifact                                   |
| Query / Product Result          |         not by itself | compiler product                                  |
| HID / Dense Ordinal             |                    No | identity / lookup / addressing mechanism          |
| Source Provenance               |                    No | source relation                                   |

---

# 37. Current Strongest IR Family

```text
Contract Source
    ↓
Resolved Contract HIR
    ↓
Establishment
    ↓
Canonical Contract World

User Implementation
    ↓
Realization Body IR

Canonical Contract World
+
Verified Realization
    ↓
Contract-Aware Execution IR
    ↓
JVM Plan / IR
    ↓
Classfile
```

The exact number of internal sublevels remains open.

---

# 38. Lowering Map

Compiler lowering and the 1D `Lowering Contract` are different.

```text
Source
    ↓ parse / resolve
Resolved Contract HIR

Resolved Contract HIR
    ↓ Establishment
Canonical Contract World

User Classfile
    ↓ realization acquisition
Realization Body IR

Canonical Contract World
+
Verified Realization
    ↓ Execution Formation
Contract-Aware Execution IR

Execution IR
    ↓ JVM target lowering
JVM Plan / IR

JVM Plan / IR
    ↓ emission
Classfile
```

---

# 39. Product and Query Orchestration

Kontrakt produces several major compiler products.

A query-oriented V1 interface is currently selected.

The important architecture is:

```text
Product Identity
+
Explicit Inputs
+
Published Result
+
Dependency Recording
+
Generation Validity
```

Passes remain local processing mechanisms.

The compiler should not make one global pass pipeline the owner of every product.

Not every calculation needs to be a query.

A query is useful when demand, reuse, dependency tracking, or invalidation precision justify the boundary.

Product publication also needs lifecycle information.

```text
producer / schema version
target identity
input identity
artifact identity
publication generation
compatibility / corruption check
stale artifact handling
```

Persistent compiler products are derived material.

Deleting them may reduce performance.

It must not change Contract meaning.

---

# 40. Query Architecture Is Replaceable

The current query-oriented V1 decision does not make one traversal algorithm permanent.

Keep these roles separate.

```text
HID
    → identity / equality evidence / early cutoff

Merkle structure
    → hierarchical change localization

Query
    → computation / demand / dependency interface

Cache tier
    → reusable-result retention

Incremental repair
    → how changed derived material is repaired
```

The following repair strategies must remain replaceable:

```text
pull validation
push invalidation
change-frontier propagation
delta maintenance
hybrid scheduling
domain-local repair
priority worklists
incremental / full-rebuild switching
```

V1 should preserve product and dependency boundaries.

V2 is not assumed to use one compiler-wide repair algorithm.

Different compiler domains may use different algorithms.

---

# 41. Reuse Layers

Reuse occurs at several levels.

```text
Canonical Contract World
    → shared semantic substrate

Shared Analysis
    → verifier / optimizer reuse

Verification Overlay
    → no full verified IR copy

IR Backing
    → shared immutable storage / overlay
```

The currently implemented L1 / L2 structure is planning-specific.

```text
Planning L1
    → worker / session-local hot planning state

Planning L2
    → wider canonical planning-result reuse
```

They are not yet one compiler-wide generic cache hierarchy.

Future frontend, analysis, verifier, optimizer, and backend domains may reuse the same tiering principle.

Each domain must own its own:

```text
key meaning
equality / validation rule
lifetime
invalidation boundary
physical representation
retention policy
```

A future persistent tier may provide cross-session reuse for selected products.

Cache is work avoidance.

It is not authority.

---

# 42. Identity Separation

Keep these separate:

```text
Contract semantic identity
IR semantic identity
source provenance identity
HID / fingerprint
generation identity
dense ordinal
table row
memory address
JVM object identity
```

Typical roles:

```text
Semantic Identity
    → meaning

HID
    → lookup / equality evidence

Dense Ordinal
    → local addressing

Generation
    → validity boundary
```

---

# 43. Graph Separation

Kontrakt contains several different graphs.

```text
Contract semantic graph
realization call/effect/origin graph
CFG / data-flow graph
analysis dependency graph
compiler product dependency graph
build / artifact dependency graph
diagnostic provenance graph
Whole-Machine summary graph
incremental propagation graph
```

They must not collapse into one universal graph.

```text
CFG edge
    ≠ Contract State Transition

query dependency
    ≠ Contract dependency

realization call edge
    ≠ Required Basis relation

build dependency
    ≠ semantic dependency
```

A compressed propagation graph, SCC-condensed graph, reachability index, or transitive reduction is derived compiler
structure.

It must not silently replace semantic dependency meaning.

---

# 44. Diagnostics

Diagnostics are structured compiler products.

They may consume:

```text
frontend result
Established Contract material
verification result
optimization result
backend result
source provenance
```

Working shape:

```text
Diagnostic Code
Semantic Reference
Provenance Reference
Arguments
Related Notes
```

Rendering is separate.

Evidence kind depends on the failure class.

```text
Contract Judgment Failure
    → Contract authority evidence is primary

Realization Verification Failure
    → deterministic provenance witness is primary

User Implementation Failure
    → implementation provenance / stack trace is primary

Compiler / runtime Crash
    → operational failure evidence
```

A verification provenance witness is not a runtime stack trace.

A Contract Failure is not a JVM exception.

Contract Diagnostic Evidence remains distinct from compiler diagnostics.

---

# 45. Reference Judgment

Reference Judgment is a sibling of the optimized path.

It should remain simpler and sufficiently independent.

Definition material alone is not enough for an actual judgment.

```text
Canonical Contract World
+
actual candidate / established occurrence material
+
applicable execution context
    ↓
Reference Judgment
    ↓
Reference Result
```

It may intentionally recompute selected judgments.

Its purpose is validation independence, not runtime performance.

Reference Judgment does not create Contract authority.

---

# 46. PBT / Fixture / Unit-Test Products

Generated tests come from exact Contract obligations.

```text
Established Obligation
    ↓
Deterministic Test Plan
    ↓
Cases
    ↓
Reference Judgment
    ↓
Expected Result
    ↓
Generated Test Artifact
```

Expected behavior must not be learned from user realization.

PBT is not verifier authority.

---

# 47. Compiler QA

Compiler QA is separate from generated user Contract tests.

V1 should include:

```text
frontend regression
parser fuzzing
semantic regression
Establishment regression
realization closure regression
IR verifier tests
analysis tests
optimizer source-target comparison
JVM codegen tests
reference-vs-optimized differential tests
determinism tests
cache-on / cache-off equivalence
performance baselines
```

Production compiler development also needs observability.

```text
stage / query timing
memory and allocation metrics
optimization remarks
invalidation / reuse trace
backend diagnostics
crash reproducer
reducer support where practical
```

Observability is not Contract authority.

Compiler correctness must not depend on one verifier.

---

# 48. Transform Validation

IR verification is an architecture boundary, not only a test category.

Possible checkpoints include:

```text
Realization Body IR publication
Execution Formation
meaning-preserving transform
JVM planning
classfile construction
```

Important transforms also need independent preservation checks.

Possible mechanisms:

```text
IR verifier
local validator
translation validation
differential execution
Reference comparison
metamorphic testing
```

The validator should not simply reuse the transform's own legality code.

Heavy checks may be configurable.

The invariant itself is not optional.

---

# 49. Determinism and Publication

The compiler should prefer:

```text
immutable published material
worker-local mutation
explicit merge
stable identity
deterministic publication
```

Product dependency and physical scheduling dependency are different.

```text
product A depends on product B
    ≠
worker A must run immediately after worker B
```

Parallel work should preserve deterministic semantic publication.

Worker completion order must not decide:

```text
Contract meaning
semantic identity
verification result
required output ordering
```

The Driver / Session also owns compilation resources.

```text
memory envelope
worker ownership
scratch lifetime
cancellation
published generation
old-generation reclamation
artifact publication
```

Cancellation must not expose a partially published generation as a successful compiler product.

Clean, cached, parallel, cancelled-and-retried, and reused compilation must preserve the same semantic result for the
same valid inputs.

---

# 50. V1 Required Skeleton

V1 should preserve at least these architecture boundaries.

```text
Source Manager / Provenance
Syntax / Recovery
Module / Name / Symbol Resolution
Contract Frontend
Resolved Contract HIR
Authority-Owned Establishment
Canonical Contract World
Frozen Publication
Generated API Product Boundary

Realization Acquisition
Realization Admission / Airlock
Admitted Realization Binding
Realization Body IR
Local Structural Analysis
Contract-Aware Analysis
Core Closure Verification
Verification Overlay
Dynamic / Opaque Capability Support Rule

Execution Formation
Contract-Aware Execution IR
IR Verification
Analysis / Transform Infrastructure
Contract-Specific Optimization
Generic Cleanup
Whole-Machine Summary / Selective Body-Opening Seam

JVM Legalization / Planning
Typed JVM Method Plan
Direct Classfile Construction / Emission

Reference Judgment
PBT / Test Planning
Structured Diagnostics
Compiler QA
Observability / Reproducer

Driver / Session
Resource Ownership
Stable Identity
Generation Validity
Product / Query Boundaries
Dependency Recording
Artifact / Product Publication
Existing Planning L1 / L2 Reuse
V2 Incremental Evolution Seam
```

The exact physical split remains open where a semantic or target-level boundary does not require another representation.

---

# 51. V2 Evolution Seam

V2 should extend this architecture rather than replace it.

Possible additions:

```text
persistent product state
cross-session reuse
multiple immutable generations
Merkle change localization
incremental analysis repair
change-frontier propagation
delta-maintained analysis
lazy materialization
summary persistence
artifact reuse
incremental test planning
incremental / full-rebuild switching
advanced scheduling
prediction-guided profitability or scheduling
profile-guided profitability
```

No one incremental algorithm is fixed.

Different compiler domains may use different repair strategies.

The common requirement is smaller:

```text
explicit inputs
stable product identity
deterministic computation
frozen publication
clear dependency boundary
replaceable reuse / repair policy
```

Prediction or historical telemetry may change work order or profitability decisions.

It must not change compiler correctness or Contract meaning.

---

# 52. Intentionally Open

This document does not freeze:

```text
exact IR count
exact HIR schema
exact Execution IR operations
SSA form
CFG physical layout
analysis manager API
pass manager API
query scheduler
pull / push / hybrid incremental execution
red-green adoption
fingerprint algorithm
final HID encoding
exact FFM / slab layout
Whole-Machine summary schema
frozen table layout
direct classfile encoder implementation
optimizer pass order
cost model
persistent cache design
prediction / scheduling model
```

Some directions are no longer open at the same level.

```text
V1 production backend
    → direct classfile path

Generated source backend
    → reference / bootstrap / debug / differential role

high-cardinality physical direction
    → FFM-backed primitive slabbing by default
```

The exact physical layout, migration boundary, target profile, and encoder remain replaceable.

---

# 53. 1D ADR Review Map

When reviewing each 1D Contract, the architecture needs only the semantic material that authority actually owns.

| Question                        | 1D ADR must decide                                                 |
|---------------------------------|--------------------------------------------------------------------|
| Definition meaning              | What does this authority declare?                                  |
| Established Definition Material | What enters the Canonical Contract World?                          |
| Occurrence meaning              | Does this authority own occurrence material?                       |
| Required Basis                  | What must exist before judgment?                                   |
| Applicability                   | When may the material be used?                                     |
| Establishment result            | What exactly becomes authoritative?                                |
| Failure relation                | How does refusal connect to Failure?                               |
| Execution need                  | What exact relation must Execution Formation consume?              |
| Diagnostic need                 | What authoritative material must diagnostics explain?              |
| PBT obligation                  | What semantic partitions or witnesses follow?                      |
| Verification need               | What must user realization verification prove?                     |
| Optimization value              | What established context may become static optimization knowledge? |

If downstream work needs semantic material that does not exist, return to the owning ADR.

Do not invent it in the compiler subsystem.

---

# 54. Final Working View

```text
Contract semantics
    ↓
Resolved Contract HIR
    ↓
Authority-Owned Establishment
    ↓
Canonical Contract World
    ↓
Generated API Product
    ↓
Host Compilation / Realization Acquisition
    ↓
Realization Admission / Airlock
    ↓
Contract-Aware Analysis
    ↓
Realization Verification
    ↓
Static Specialization Knowledge
    ↓
Execution Formation
    ↓
Contract-Aware Execution IR
    ↓
Analysis / Transform Loop
    ↓
JVM Legalization / Typed Method Planning
    ↓
Direct Classfile Product
```

The supporting architecture is:

```text
identity
provenance
language / source management
generation
frozen publication
analysis validity
product dependencies
build / artifact dependencies
reuse
resource ownership
deterministic scheduling
external-technology airlock
diagnostics
Reference
PBT
QA
observability
V2 incremental seams
```

The central rule remains:

```text
Contract meaning first.

Compiler knowledge consumes it.

Optimization preserves it.

Physical realization remains replaceable.
```

---

# 55. Basis of This Draft

This draft is aligned with the current Kontrakt direction from:

```text
What Contract Is
ADR-0063
ADR-0070

Kontrakt Established Contract World Architecture TODO
Kontrakt Contract-Aware Realization Optimization TODO
Kontrakt IR Subsystem Contract / Implementation Separation Design
Kontrakt V1 Commercial Compiler Foundation Candidate Architecture
Kontrakt V2 Reference Architecture and V1 Foundations
Kontrakt V2 Incremental Architecture Research TODO
Kontrakt Query-Oriented Compiler Design
Kontrakt Verifier Candidate Implementation Plan

Modern Compiler Architecture 01–15
```

The modern compiler material contributes general engineering principles:

```text
stage invariants
multi-level IR
logical / physical separation
CFG / SSA / data-flow analysis
analysis / transformation separation
analysis reuse and invalidation
context-sensitive and interprocedural analysis
summary-driven whole-program work
progressive lowering
legality / profitability separation
Reference and differential checking
structured diagnostics
compiler QA
resource ownership
incremental architecture as a cross-cutting concern
JVM / JIT handoff
```

Those principles do not override Kontrakt Contract semantics.