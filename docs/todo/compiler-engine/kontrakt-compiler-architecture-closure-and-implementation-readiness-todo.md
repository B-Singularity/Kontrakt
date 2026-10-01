# Kontrakt Compiler Architecture Closure and Implementation Readiness TODO

## Status

**Planning / TODO document. Not an ADR. Not Contract authority.**

This document is the current master worklist for moving Kontrakt from architecture closure into implementation without
repeating another whole-system rewrite.

It does not define new Contract semantics.

`What Contract Is` and Accepted ADRs remain authoritative. The current compiler architecture map remains the working
whole-compiler design baseline. Authority-specific ADRs remain responsible for their own semantic laws.

The purpose of this TODO is to make the remaining work visible, order the work by dependency, and ensure that frontend,
IR, Establishment, realization analysis, verification, optimization, backend, QA, security, observability, and
incremental seams are closed before implementation choices become expensive to reverse.

---

# 1. Current Position

Kontrakt is no longer a runtime framework architecture.

The current direction is a Contract compiler with two major semantic/realization axes.

```text
Contract Authoring
    ↓
Resolution
    ↓
Resolved Contract HIR
    ↓
Authority-Owned Establishment
    ↓
Canonical Contract World
```

and:

```text
User JVM Realization
    ↓
Realization Acquisition
    ↓
Realization Body IR
    ↓
Analysis / Verification
    ↓
Execution Formation
    ↓
Contract-Aware Execution IR
    ↓
Optimization / Lowering
    ↓
JVM Product
```

The main remaining problem is not inventing another macro-architecture.

The remaining work is to close the semantic and compiler boundaries deeply enough that implementation does not force a
third architectural rewrite.

The working rule remains:

```text
Contract meaning first.

Compiler semantic material may exist before authority.

Compiler knowledge consumes established meaning where authority is required.

Optimization preserves the meaning owned by its input level.

Physical realization remains replaceable.
```

---

# 2. Completion Standard

Architecture closure does not mean deciding every data structure before implementation.

A question belongs in the pre-implementation closure when getting it wrong can force several compiler subsystems to
change together.

Questions that normally must be closed before implementation include:

```text
Who owns the meaning?
What is the semantic subject?
What is its exact identity?
What are its semantic determinants?
What Basis is required?
When is that Basis applicable?
Does the authority own occurrence meaning?
What exactly becomes Established?
What may another responsibility legally observe?
What information may be discarded?
Who owns refusal or unsuccessful result meaning?
What makes a result invalid?
What must remain deterministic?
What may optimization legally change?
What provenance must survive?
What resource bounds must hold?
What security boundary must be preserved?
```

Questions that normally remain Design or implementation choices include:

```text
exact slab size
array growth factor
hash-table probing policy
worker-count heuristic
physical CFG numbering
specific cache eviction policy
exact arena layout
exact instruction container
specific compression format
```

unless one of those choices becomes necessary to preserve a semantic, security, determinism, or resource obligation.

---

# 3. P0 — Reclose the Inbound Airlock

The current Accepted 1D ADR family was largely written around:

```text
Input
→ Admission
→ Canonicalization, if selected
→ Lowering
```

The current redesign is investigating:

```text
Input
→ Canonicalization, if selected
→ Admission
→ Lowering
```

with the omitted branch:

```text
Input
→ Admission
→ Lowering
```

Do not continue the `What Contract Is` pipeline rewrite until this is closed.

## ADR-0048

Reopen the shared inbound-airlock composition law.

Close:

- logical Contract ordering;
- authority handoff;
- optional Canonicalization branch;
- omitted Canonicalization branch;
- earliest authoritative stop;
- refusal ownership;
- Budget / Capacity interaction;
- State legality ordering;
- execution-formation consequences;
- Lowering source authority;
- Core-entry handoff.

## ADR-0066 — Canonicalization

Re-audit:

- semantic subject;
- Definition determinants;
- exact Input Basis;
- Binding and applicability;
- occurrence/application meaning;
- stable representative authority;
- canonical-byte authority, if retained;
- omission semantics;
- refusal;
- bounded work;
- Budget / Capacity interaction;
- determinism;
- downstream protocol;
- Admission consumption.

Canonicalization must remain representative selection under declared equivalence.

It must not silently become cleanup, repair, sanitization, generic parsing, business computation, arbitrary conversion,
or Lowering.

## ADR-0065 — Admission

Re-audit Admission after Canonicalization.

The central question is:

```text
When Canonicalization is selected,
what exact authoritative material does Admission judge?
```

Separate:

```text
Admission Definition dependency
Admission Binding dependency
Admission runtime determining material
Admission Occurrence determinant
```

Do not collapse these into one generic dependency relation.

## ADR-0064 / ADR-0067

Repair upstream and downstream assumptions after the ordering law closes.

Input must not acquire Canonicalization or Admission authority.

Lowering must continue to own the final inbound representation relation and Core-entry boundary.

---

# 4. P0 — Complete the 1D HIR / Establishment Audit

The current 1D master checklist remains a critical active document.

Apply it to every working 1D authority.

Current working family:

```text
Input
Admission
Canonicalization
Lowering
Fact
Invariant
State
State Transition
Explicit State Machine Manifest
Failure
Publication
Output Presentation
Diagnostic Evidence
Diagnostic Retention
Version
Policy
Budget
Capacity
Governance
```

Audit the Interface Surface separately as a primary/control semantic surface where appropriate.

For every authority, explicitly decide whether the following exists and who owns it:

```text
Definition Candidate
Definition Identity
Definition Determinants
Definition Equality
IDL Binding Candidate
Required Basis
Basis Binding
Applicability
Applicable Context
Occurrence / Application
Occurrence Identity
Occurrence Determinants
Establishment Unit
Established Material
Established Protocol
Failure Relation
Diagnostic Requirement
PBT Requirement
Verification Requirement
Optimization Knowledge
Version Relation
Reuse Relation
```

Do not add a category merely because another 1D uses it.

## Required closure

- Repair the Output Presentation catalog drift.
- Close `HIR Bundle` for every applicable 1D.
- Close `Establishment Bundle` for every applicable 1D.
- Close `Established Protocol` for every applicable 1D.
- Resolve duplicate / collision / coverage / merge / singularity ownership per 1D.
- Re-audit ambient `current` usage.
- Re-audit Definition vs Occurrence meaning.
- Re-audit exact Required Basis and Applicable Context.
- Re-audit semantic prerequisite cycles.
- Re-audit authority-specific completeness units.

Exit only when downstream compiler work no longer needs to invent missing Contract meaning.

---

# 5. P0 — Close Common Compiler Result Boundaries

## ADR-0074

Close or explicitly reassign the compiler unsuccessful-result responsibilities currently still provisional.

At minimum:

```text
exact unsuccessful-result owner
first owning rejection
unentered later stage
recovery
trust loss / indeterminate result
result availability
diagnostic consumption
Compiler Result observation
```

Compiler unsuccessful result and Contract Failure must remain distinct.

## ADR-0075

Close the compiler-wide producer / consumer architecture.

Required decisions include:

- logical compiler-result subject;
- producer ownership;
- legal observation surface;
- product/result identity;
- product generation or occurrence, where needed;
- exact inputs;
- producer-owned equality;
- validity;
- information loss;
- projection vs derived summary;
- legal cross-responsibility consumption;
- reuse comparison;
- cancellation and partial publication;
- stale-result rejection;
- relation to query/cache/persistence;
- determinism across clean/reused/parallel formation.

Do not create one universal `CompilerProduct` semantic model.

The common law should remain:

```text
producer owns result meaning
    ↓
producer exposes legal observation
    ↓
consumer may rely on that observation
    ↓
consumer may derive its own compiler result
```

Authority does not transfer through consumption.

---

# 6. P0 — Freeze the HIR → Establishment → Contract World Seam

ADR-0071 and ADR-0063 already provide a strong semantic baseline.

Before implementation freezes, close remaining integration questions.

## HIR

Verify that Visible Resolved HIR has:

- exact resolved semantic subjects;
- exact candidate references;
- producer-owned semantic equality;
- provenance kept adjacent rather than inside semantic identity;
- no recovery/poison material masquerading as valid HIR;
- no query/cache/layout meaning;
- no Contract-owned distinction erased by frontend refinement;
- legal fine-grained projections;
- a stable Establishment-facing protocol.

## Establishment

Verify:

- exact owning authority;
- exact Definition identity;
- exact Required Basis;
- exact Basis Binding;
- exact Applicability;
- exact occurrence attribution where occurrence exists;
- no synthetic global Establishment transaction;
- no compiler topology filling missing semantic relations.

## Canonical Contract World

Close:

- terminology;
- Definition-world contents;
- occurrence-material boundary;
- exact downstream references;
- publication / visibility unit;
- stale-generation rejection;
- world linking;
- provenance relation;
- independent consumer access;
- storage independence.

Do not create a second universal semantic model merely for compiler convenience.

---

# 7. P1 — Realization Acquisition as a Real Compiler Frontend

The realization frontend should no longer depend on KSP as the core semantic path.

The preferred core boundary is JVM classfile or another exact JVM realization product.

KSP may remain optional source tooling.

## Required architecture

```text
JVM Classfile
    ↓
bounded classfile acquisition
    ↓
decoded method/type material
    ↓
basic blocks
    ↓
CFG
    ↓
value / stack reconstruction
    ↓
Realization Body IR
```

## Required JVM coverage decisions

Explicitly classify:

```text
invokevirtual
invokeinterface
invokestatic
invokespecial
invokedynamic
MethodHandle
reflection
native / JNI
exceptions
monitors
volatile
arrays
boxing
class initialization
ClassLoader interaction
dynamic proxies
Kotlin-generated JVM patterns
Kotlin Metadata
```

For each, decide whether V1 understands, delegates, requires Adapter, treats as opaque, or rejects it.

Do not infer Contract meaning from compiler-specific bytecode patterns.

The realization frontend may recognize supported Java/Kotlin compiler patterns only to refine them into Kontrakt-owned
realization semantics.

---

# 8. P1 — Realization Body IR

Define a genuine compiler IR for JVM user realization analysis.

Do not use JVM object topology as the IR model.

Required semantic capabilities should include, as needed:

```text
basic blocks
CFG
exceptional CFG
value flow
SSA or SSA-like value identity
call sites
memory effects
origin / capability flow
alias information
escape information
exception flow
synchronization effects
external-resource reachability
```

## Required questions

- What invariant is true when Realization Body IR becomes visible?
- What bytecode distinctions are still required?
- What JVM mechanics may be erased?
- What information must survive for diagnostics?
- What information must survive for verification?
- What information must survive for optimization?
- What is local analysis?
- What requires interprocedural analysis?
- What requires Whole-Machine summaries?
- Which unsupported cases fail closed?

---

# 9. P1 — First-Class IR Verification

Every major representation boundary should eventually have an explicit verifier.

Candidate family:

```text
HIR Verifier
Established World Verifier
Realization Body IR Verifier
Execution IR Verifier
JVM Plan / LIR Verifier
Classfile Product Verifier
```

A verifier checks the invariant owned by its representation.

It does not become a second semantic authority.

## Realization / Execution examples

Check that:

- CFG edges are valid;
- exceptional edges are valid;
- value definitions dominate legal uses where required;
- Contract references are exact and live;
- external authority does not silently cross the Airlock;
- Fact authority cannot be fabricated by realization IR;
- failure attribution remains owned;
- State movement remains explicit;
- erased information is no longer needed by legal consumers;
- optimization has not detached diagnostics from their semantic origin.

Debug and QA modes should support:

```text
verify before transform
transform
verify after transform
```

for relevant pass boundaries.

---

# 10. P1 — Analysis Ownership and Invalidation Framework

Kontrakt needs an explicit analysis lifecycle comparable in rigor to its semantic lifecycle.

Define:

```text
Analysis Requirement
Analysis Producer
Analysis Result
Analysis Inputs
Analysis Validity
Preservation Claim
Invalidation
Recompute
Optional Incremental Update
```

A transformation must state which analysis results remain valid.

Do not keep consuming an analysis result after one of its determinants has changed.

Keep separate:

```text
Contract semantic validity
compiler-product validity
analysis validity
cache validity
provenance validity
```

## Required framework properties

- legality and profitability remain separate;
- one transform need not use one universal proof technology;
- preserved analyses remain explicit;
- invalidated analyses cannot remain accidentally reachable as current;
- recomputation must be deterministic;
- analysis summaries have producer-owned equality;
- analysis caching remains optional machinery;
- pass scheduling must not become semantic authority.

---

# 11. P1 — Contract-Aware Execution IR

Execution IR should be the first realization IR whose vocabulary can directly exploit already-established Contract
knowledge.

It must not copy the entire Canonical Contract World into every node.

Use exact compact references where high-level Contract meaning must remain recoverable.

Candidate semantic operations may include concepts such as:

```text
established material read
Contract judgment
Admission judgment
Canonicalization realization
Lowering realization
Fact establishment boundary
State / Transition judgment
Publication judgment
external operation
user Operation call
declared failure edge
```

The exact vocabulary remains a later IR decision.

## Required closure

- stage invariant;
- Contract-reference preservation;
- value/effect model;
- exceptional control flow;
- State-visible effects;
- Failure routing;
- provenance requirements;
- optimization-observable semantics;
- lowering boundary to JVM-oriented IR.

---

# 12. P1 — Initial Optimizer Architecture

Do not attempt to compete with HotSpot or Graal on generic machine optimization.

Focus first on information Kontrakt uniquely owns.

Separate two axes.

## Contract machinery optimization

Candidate work:

```text
static discharge
stage fusion
wrapper elimination
direct binding
dead Contract machinery removal
dense lookup specialization
representation-barrier removal
known-policy specialization
known-version specialization
```

The semantic boundaries remain even when the physical machinery is fused.

## User realization optimization

Candidate work:

```text
known-condition specialization
unreachable branch elimination
exact-target devirtualization
call-target pruning
redundant judgment removal
bounded scalarization
constant propagation from established Contract context
wrapper elimination
```

## First optimizer quality law

Every transform family must identify:

```text
required semantic / analysis inputs
legality condition
profitability condition
preservation obligation
validation evidence
invalidated analyses
diagnostic / provenance consequences
```

Start with conservative passes whose preservation relation can be tested aggressively.

---

# 13. P1 — JVM Legalization and Direct Classfile Backend

The long-term production path is direct classfile generation.

Generated Java/Kotlin source may remain useful for bootstrap, reference, debugging, or differential testing.

## Required backend closure

Define:

```text
JVM-oriented LIR or typed method plan
JVM type legality
operand stack planning
local-slot planning
exception table formation
StackMapTable / frame formation
constant-pool ownership
bootstrap method formation
class initialization semantics
method-size bounds
classfile-version policy
deterministic constant-pool / artifact formation
JVM verifier compatibility
```

The backend must reject a Contract/Execution requirement that it cannot preserve.

Do not silently weaken semantics to make bytecode emission easier.

---

# 14. P1 — Compiler Observability

Observability is infrastructure, not release polish.

Kontrakt should be able to explain its own compilation.

Provide machine-readable forms first.

Candidate outputs:

```text
resolved HIR dump
Canonical Contract World dump
query trace
dependency trace
Realization CFG dump
SSA / value-flow dump
analysis-result dump
verification trace
Execution IR dump
optimization log
pass timing
resource-budget report
memory / allocation counters
backend plan dump
classfile emission trace
semantic/provenance relation trace
```

Important questions must become answerable:

```text
Why was this product recomputed?
Why was this analysis invalidated?
Why was this method rejected?
Which Contract relation justified this specialization?
Which pass removed this branch?
Which upstream material does this diagnostic explain?
Why was this cached result not reusable?
Which resource bound stopped this compiler work?
```

Observability must not create a second semantic authority.

---

# 15. P1 — QA Architecture

QA should be designed at the same time as subsystem boundaries.

## Golden vectors

```text
source → HIR
HIR → Established meaning
canonical representation
semantic identity
realization bytecode → Realization IR
Execution IR
JVM plan
classfile product
diagnostic output
```

## Equivalence testing

```text
cache off == cache on
clean build == reused build
1 worker == N workers
optimization off == optimization on for Contract-observable meaning
reference backend == production backend where both are defined
full rebuild == incremental rebuild when incremental mode exists
```

## Differential testing

Use independent implementations where useful:

```text
reference judgment vs generated judgment
reference classfile/source backend vs direct classfile backend
clean formation vs reuse path
unoptimized vs optimized realization
```

## Metamorphic testing

Perturb things that must not affect meaning:

```text
source formatting
provenance-only movement
worker scheduling
allocation order
table layout
cache state
legal pass scheduling
non-semantic declaration ordering
```

## Fuzzing

At minimum:

```text
parser
resolver
HIR protocol
Established protocol
classfile parser
Kotlin Metadata reader, if supported
CFG formation
IR verifier
classfile emitter
diagnostic rendering
persistent format, when introduced
```

## Translation validation

Keep an explicit seam for validating individual transformation families without requiring one universal formal proof
system.

---

# 16. P1 — Security and Adversarial Compiler Inputs

Security must be part of frontend and analysis architecture.

The compiler must not assume trusted source or trusted classfiles.

Threat families include:

```text
malformed classfile
constant-pool explosion
huge method
pathological CFG
pathological exception table
deep generic signatures
attribute bombs
invokedynamic bootstrap graphs
malicious Kotlin Metadata
jar / zip bombs
duplicate class definitions
classpath ambiguity
ClassLoader tricks
reflection escape
native escape
symlink / path traversal
resource exhaustion
diagnostic amplification
adversarial canonicalization input
adversarial regex / library behavior where admitted
```

Required properties:

```text
bounded parsing
bounded graph formation
bounded analysis
bounded diagnostic production
explicit resource accounting
fail-closed unsupported input
no hidden external capability
no partial authoritative publication
```

Security limits that change Contract meaning belong to the owning Contract law.

Compiler operational limits remain compiler resource governance.

Do not mix the two.

---

# 17. P1 — Determinism and Reproducibility

Determinism remains a first-class architecture requirement.

For the same explicit semantic inputs:

```text
worker completion order
allocation order
hash iteration order
cache hit / miss
legal query scheduling
physical table layout
debug mode
legal optimization scheduling
```

must not change observable semantic results.

Build reproducibility additionally requires explicit handling of compiler version, frontend profile, target JVM version,
platform semantic basis, backend capabilities, environment-sensitive tooling inputs, and persistent schema version where
they actually affect artifact validity.

---

# 18. P1 — Resource Ownership

Every expensive compiler subsystem needs explicit boundedness.

Audit:

```text
parser
resolution
HIR formation
Establishment
Canonical Contract World publication
query evaluation
classfile acquisition
CFG / SSA formation
interprocedural analysis
verification
PBT generation
diagnostic generation
optimization
backend emission
persistence
```

For each expensive family decide:

- resource owner;
- work unit;
- memory owner;
- lifetime;
- cancellation behavior;
- partial-result rule;
- retry/restart behavior;
- cleanup/reclamation boundary.

Compiler Budget is not automatically a Contract Budget.

Keep compiler resource governance separate from user-declared machine Budget/Capacity.

---

# 19. P2 — Incremental and Persistent Evolution Seam

V1 should establish the seam.

V2 may choose the algorithm.

## V1 obligations

```text
query-oriented orchestration
explicit product inputs
in-memory dependency recording
producer-owned equality
explicit validity
semantic/provenance dependency separation
immutable published results
generation/revision boundary
clean recomputation path
cache-disabled correctness
```

## V2 candidates

```text
persistent dependencies
persistent semantic projections
early cutoff
red/green-style validation where appropriate
domain-local delta maintenance
dynamic dependency repair
incremental parsing
incremental resolution
incremental analysis
incremental verification
incremental PBT planning
adaptive repair vs rebuild
remote/content-addressed reuse where justified
```

Do not choose one compiler-wide incremental algorithm prematurely.

The legal requirement remains:

```text
incremental / cached / parallel execution
    ↓
same legal consumer-visible result
as deterministic clean execution
```

---

# 20. First Complete Vertical Slice

Before broad implementation, close one complete path deeply enough to pressure-test the architecture.

Recommended slice:

```text
.kontrakt
    ↓
frontend syntax / resolution
    ↓
Resolved Contract HIR
    ↓
HIR verification
    ↓
Authority-Owned Establishment
    ↓
Canonical Contract World
    ↓
generated host / Operation surface
    ↓
host compilation
    ↓
JVM classfile acquisition
    ↓
Realization Body IR
    ↓
CFG / value analysis
    ↓
realization verification
    ↓
Execution Formation
    ↓
Contract-Aware Execution IR
    ↓
minimal verified optimization
    ↓
JVM plan
    ↓
direct classfile product
    ↓
JVM verification
    ↓
Reference / differential / determinism QA
```

The first slice should use a minimal Contract surface, but the architecture must use the real boundaries rather than
temporary shortcuts that would later become permanent.

---

# 21. Vertical Slice Acceptance Gates

The first end-to-end slice is not complete until it demonstrates:

```text
no source object as Contract authority
no implementation class as Contract authority
no cache/query topology as Contract authority
no hidden external capability entering Core meaning
exact HIR → Establishment observation
exact downstream Established observation
deterministic repeated compilation
1-worker / N-worker semantic equivalence
cache-off clean path
structured unsuccessful compiler result
structured diagnostics with provenance
Realization IR verification
Execution IR verification
optimization preservation check
direct classfile JVM verification
reference/differential agreement
bounded malformed-input handling
```

Performance measurements begin here.

Do not optimize a representation whose semantic and validity boundary is still changing.

---

# 22. Documentation Consolidation

This document should become the **master implementation-readiness TODO**.

It should not absorb every specialized research document.

## Keep as active specialized documents

Keep documents whose detailed research is still independently useful, including:

```text
kontrakt-1d-hir-establishment-master-checklist.md
kontrakt-v2-incremental-architecture-research-todo.md
kontrakt-contract-aware-realization-optimization-todo.md
Kontrakt Query-Oriented Compiler / Object-Free Core design material
compiler material / IR architecture review checklist
current compiler total architecture map
Modern Compiler Architecture 01–15
```

These documents answer narrower questions than this master TODO.

## Consolidation rule

A specialized TODO can be retired when all of the following are true:

```text
1. Every still-valid obligation has moved into an Accepted ADR,
   a current Design document,
   this master TODO,
   or another clearly owned active document.

2. No unresolved decision exists only in that TODO.

3. No current ADR or Design document depends on it as the only explanation
   of a still-valid requirement.

4. Its old terminology would create more confusion than historical value.
```

When historical reasoning may still matter, prefer moving the file to an archive rather than leaving it in the active
TODO set.

---

# 23. Current TODO Retirement Candidates

The following are strong retirement candidates after their remaining useful material is checked against this master TODO
and current ADRs.

## A. `kontrakt-verifier-implementation-plan.md`

Reason:

- explicitly describes itself as a candidate implementation plan rather than current architecture;
- contains terminology predating the current HIR / Establishment / Canonical Contract World boundaries;
- predates the current realization-axis, platform-boundary, producer/consumer protocol, and direct-classfile direction;
- its still-valid QA and verifier requirements should be moved into current verifier/IR design rather than kept as a
  competing architecture.

Recommended action:

```text
extract still-valid verifier and QA requirements
→ verify they exist in current docs / this TODO
→ archive or delete
```

## B. `kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`

Reason:

- it is a broad candidate checklist rather than an authority document;
- the current compiler total architecture map is a more current whole-compiler baseline;
- this master TODO absorbs the implementation-readiness and SOTA-gap checklist role;
- specialized areas remain owned by narrower current documents.

Recommended action:

```text
compare remaining P0/P1/P2 items
→ migrate missing active items
→ retire
```

## C. `kontrakt-established-contract-world-architecture-todo.md`

Reason:

- several questions that motivated it have since moved into ADR-0063 and ADR-0071;
- current architecture already distinguishes Resolved Contract HIR, Authority-Owned Establishment, and Canonical
  Contract World;
- KSP removal / classfile realization acquisition and query-oriented V1 direction are now part of the wider
  architecture;
- its still-open Contract World publication, occurrence-material, provenance, and consumer-boundary questions belong in
  current semantic/protocol closure work.

This one should **not** be deleted blindly.

Recommended action:

```text
migrate every still-open P0/P1 item
→ confirm no unique unresolved semantic question remains
→ archive or delete
```

## D. `kontrakt-compiler-reuse-incremental-v1-v2-todo.md`

Reason:

- its common producer/reuse/validity concerns are increasingly owned by ADR-0075;
- V2 incremental algorithm research has its own dedicated research TODO;
- retaining three overlapping reuse/incremental documents risks divergence.

Recommended action:

```text
common protocol obligations
    → ADR-0075 / current compiler architecture

V1 seam obligations
    → this master TODO / query design

V2 research
    → kontrakt-v2-incremental-architecture-research-todo.md

then retire this overlap document
```

---

# 24. Documents That Should Not Be Retired Yet

Do not currently delete these merely to reduce document count:

```text
kontrakt-1d-hir-establishment-master-checklist.md
kontrakt-contract-aware-realization-optimization-todo.md
kontrakt-v2-incremental-architecture-research-todo.md
kontrakt-v2-reference-architecture-and-v1-foundations.md
kontrakt-compiler-total-architecture-map-design-draft.md
```

Reasons differ.

The 1D checklist is part of the current semantic closure process.

The optimization TODO contains specialized optimization questions not replaced by this master worklist.

The V2 incremental research document preserves alternatives and failure modes that should remain open until V2 design.

The V2 reference architecture is a future-compatibility constraint document rather than a redundant immediate
implementation TODO.

The compiler total architecture map is the current whole-compiler working baseline.

---

# 25. Immediate Work Order

Use this order unless a newly discovered semantic dependency forces an earlier return.

```text
1. Reclose inbound Airlock ordering.
2. Repair ADR-0048 / 0066 / 0065 / 0064 / 0067.
3. Complete all 1D HIR / Establishment / Established Protocol audits.
4. Repair the 1D catalog drift.
5. Close ADR-0074.
6. Close ADR-0075.
7. Freeze HIR → Establishment → Canonical Contract World observation seams.
8. Design direct classfile Realization Acquisition.
9. Design Realization Body IR.
10. Design representation verifiers.
11. Design analysis ownership / invalidation.
12. Design Contract-Aware Execution IR.
13. Define the first conservative optimizer pass set.
14. Define JVM legalization / direct classfile backend.
15. Define compiler observability products.
16. Close QA / fuzz / differential / determinism matrices.
17. Close compiler security and resource-bound matrices.
18. Implement the first complete vertical slice.
19. Measure.
20. Only then deepen caching, persistent reuse, parallel scheduling,
    and V2 incremental mechanisms.
```

---

# 26. Final Rule

The current phase ends when Kontrakt has enough architecture to implement without allowing implementation convenience to
invent missing semantics.

The next phase is not reached by producing more diagrams.

It is reached when one complete compiler slice can pass through:

```text
Contract source
→ resolved semantic material
→ Contract authority
→ verified user realization
→ Contract-aware execution material
→ verified transformation
→ direct JVM product
```

while preserving:

```text
authority
identity
determinism
failure ownership
provenance
security
resource bounds
diagnosability
verification
replaceability
```

At that point, further compiler engineering should primarily refine implementation and performance rather than redefine
what the machine means.