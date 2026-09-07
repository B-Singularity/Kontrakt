# Kontrakt Contract-Aware Realization Optimization TODO

## Status

Planning / TODO document.

This document is not an ADR.

It does not define Contract semantics.

It records the current optimization direction for Kontrakt realization work.

The focus is Contract-aware optimization before JVM lowering.

The optimization consumes already-established Contract meaning.

It must not establish new Contract meaning.

---

## 1. Position

Kontrakt has optimization opportunities that a general JVM optimizer does not have.

The reason is simple.

Kontrakt knows the Contract.

It may know the selected Policy World.

It may know the applicable Governance binding.

It may know the Contract Version.

It may know the applicable State-machine surface.

It may know an exact admitted Operation realization.

It may also know that some runtime alternatives are impossible before governed execution begins.

This knowledge can remove work.

The optimizer must use that knowledge without becoming Contract authority.

The intended direction is:

```text
Established Contract World
        +
Verified User Realization
        +
Derived Analysis
        ↓
Contract-Aware Optimization
        ↓
Optimized Realization Material
        ↓
JVM Realization Lowering
```

The optimizer consumes facts.

It does not create them.

---

## 2. Two Optimization Axes

Kontrakt should distinguish two major optimization axes.

### 2.1 Contract Machinery Optimization

Kontrakt realizes the declared 1D Contract machine.

The user does not implement that machinery.

The compiler therefore has a global view of material that would otherwise be handwritten as validators, mappers, guards,
wrappers, adapters, and runtime coordination.

This creates opportunities to optimize the machinery as one compiled system.

Candidate work includes:

```text
static discharge
stage fusion
wrapper elimination
direct binding
dense lookup
dead material removal
specialization
representation-barrier removal
```

The semantic boundaries remain.

The physical machinery may become smaller.

### 2.2 User Realization Optimization

The user implements the Operation.

Kontrakt may still know facts that the user code does not encode directly.

Those facts may allow safe simplification of the user realization before JVM lowering.

Candidate work includes:

```text
redundant judgment removal
known-condition specialization
unreachable branch elimination
exact-target devirtualization
call-target pruning
bounded scalarization
wrapper elimination
constant propagation from established Contract context
```

This optimization is not a replacement for HotSpot or Graal.

It should focus on knowledge that comes from Contract semantics and realization verification.

---

## 3. Pre-Execution Fixed Contract Context

Some Contract context is established and fixed before governed execution.

This is a major optimization source.

Possible examples include:

```text
selected Policy World
applicable Governance binding
Contract Version
known State-machine surface
exact realization binding
other fixed applicable Contract context
```

The exact list is owned by the relevant Contract ADRs.

This TODO does not redefine those semantics.

The optimization rule is:

> Contract material that is already lawfully established and fixed before governed execution may be consumed as
> optimization knowledge.

A fixed context may make runtime alternatives unreachable.

The optimizer may then remove the physical work associated with those alternatives.

Example:

```text
selected Policy World = A
        ↓
Policy World B is unreachable
        ↓
B-specific branch and lookup material may be removed
```

The optimizer did not select Policy World A.

The owning Contract authority already established it.

The optimizer only consumes that result.

---

## 4. Unreachable Material

Unreachable material should be treated as derived realization knowledge.

It is not a new semantic state.

The compiler may derive:

```text
unreachable branch
unreachable realization target
unreachable state path
unreachable Contract machinery path
unreachable failure path
```

when the required proof already exists.

The transformation must preserve the meaning of the established Contract World.

A path must remain if its reachability depends on runtime material that is not fixed yet.

The optimizer must not turn uncertainty into unreachability.

---

## 5. Static Discharge

Some Contract judgments may already be decided before runtime.

The backend should not repeat a judgment whose result is already fixed by established Contract material.

The intended form is:

```text
Contract judgment
        ↓
already decided before execution
        ↓
static discharge
        ↓
runtime check removed
```

Static discharge removes physical work.

It does not remove the Contract law.

The diagnostic system may still need enough derived provenance to explain why a runtime check does not exist.

That evidence requirement must be considered during design.

---

## 6. Redundant Judgment Elimination in User Realization

User code may repeat a judgment already guaranteed by the Contract machine.

This is a useful optimization opportunity.

Example:

```text
Contract establishes:
currency = KRW

User realization:
if currency == KRW
    path A
else
    path B
```

If the established Contract context makes the condition exact, the user realization may be specialized.

```text
path A
```

The same principle may apply to:

```text
Policy lookup
Version branch
State applicability check
exact realization selection
other repeated Contract-derived conditions
```

The optimizer must prove that the user condition is semantically equivalent to the already-established fact before
removing it.

Name similarity is not enough.

Type similarity is not enough.

The proof must come from explicit analysis.

---

## 7. Verification-Derived Optimization Knowledge

Core Realization Verification already needs analysis.

That analysis should be reused.

The optimizer should not rerun expensive proof work when the verifier has already established the required fact.

Candidate verified summaries include:

```text
exact call target
no external factual dependency
non-escaping local value
identity not observed
pure helper
result provenance
stable realization binding
reachable call graph summary
```

The intended flow is:

```text
Realization Analysis
        ↓
Core Realization Verification
        ↓
Verified Summaries
        ↓
Contract-Aware Optimization
```

Verification does not perform the transformation.

Optimization consumes the verified result.

This keeps the responsibilities separate.

---

## 8. Exact Binding and Devirtualization

Realization Admission may establish an exact effective Operation implementation.

If that binding is stable for the governed lifetime, the optimizer may use it.

Example:

```text
OrderOperation
        ↓
OrderService
```

may allow:

```text
interface dispatch
        ↓
direct target
```

The exact physical form is backend-owned.

A binding that may change after admission must not be treated as exact.

A new admitted realization generation must invalidate optimization material that depended on the old binding.

---

## 9. Closed Realization Graph Pruning

Realization verification may establish a closed reachable graph for a governed Operation.

That graph may contain implementation alternatives that are not reachable in the admitted realization.

The optimizer may prune them from the specialized product.

The intended flow is:

```text
admitted realization
        ↓
reachable graph
        ↓
exact targets
        ↓
dead realization alternatives removed
```

This may expose further optimization opportunities.

Examples include:

```text
direct calls
constant propagation
wrapper elimination
smaller dispatch structures
smaller backend material
```

The graph analysis remains a realization concern.

It must not create Contract dependencies that the Contract model does not declare.

---

## 10. Contract Machinery Fusion

The logical Contract pipeline may contain many stages.

The physical backend does not need one object or one call per stage.

The optimizer may fuse stages when their semantic boundaries remain preserved.

Example:

```text
Input
    ↓
Admission
    ↓
Canonicalization
    ↓
Lowering
    ↓
Fact establishment
```

may become a compact physical path.

The exact realization may use:

```text
locals
register-friendly values
direct primitive movement
specialized branches
precomputed tables
```

The compiler must still preserve the owning authority of each judgment.

Physical fusion must not merge semantic authority.

---

## 11. Representation-Barrier Removal

Generated carriers and compiler-owned wrappers may create physical barriers that are not semantically observable.

These should be candidates for removal.

Example:

```text
Input presentation
    ↓
generated intermediate carrier
    ↓
Lowering carrier
    ↓
Operation parameter
```

may be lowered without allocating every conceptual carrier.

The backend may move the required primitive values directly when legality is proven.

This is especially important for:

```text
generated Contract machinery
generated boundary material
compiler-owned temporary realization material
```

Arbitrary user domain objects require a stronger proof.

---

## 12. Bounded User-Local Scalarization

User-local wrapper objects may be optimized when the proof is cheap.

This should not become a general-purpose whole-program escape optimizer.

The intended law is:

> Kontrakt may transform a user-local representation when non-observability follows cheaply from analysis already
> required for realization verification, together with bounded local analysis.

Candidate conditions include:

```text
local lifetime
no escape
no identity observation
no external effect
lawful factual provenance
```

If the proof becomes expensive, leave the code to the JVM optimizer.

Kontrakt should not duplicate HotSpot for marginal gains.

---

## 13. Contract Constant Propagation and Specialization

Established Contract context may act as a specialization source.

Possible coordinates include:

```text
Policy World
Version
Governance binding
known applicable State surface
fixed realization generation
exact Contract selection
```

The optimizer may create a specialized realization product for that fixed context.

The cache key must include the context that justified specialization.

A product specialized for one context must not be reused under another context.

---

## 14. Failure-Path Optimization

Failure semantics remain authoritative even when some failure paths are physically unreachable.

A failure path may be removed or moved cold only when its unreachability is proven under the fixed applicable context.

The optimizer must not erase evidence required to distinguish:

```text
Contract failure
implementation failure
unsupported realization
```

Cold-path placement is an optimization.

Failure meaning is not.

---

## 15. Diagnostic Preservation

Optimization must not destroy the ability to explain Contract behavior.

A removed runtime branch may still require provenance showing why the branch was statically discharged.

A fused path may still require authority attribution.

A direct call may still need realization-generation identity.

The optimizer may compress diagnostic material.

It must not make required diagnostic meaning unrecoverable.

Diagnostic preservation should be treated as an optimization constraint.

---

## 16. Interaction with PBT and Reference Judgment

PBT, Reference Judgment, Verification, and Optimization may consume the same established Contract material.

They should not derive competing semantic models.

The optimizer may consume test- or verifier-relevant summaries when those summaries are explicitly published for reuse.

It must not depend on test execution to establish production Contract meaning.

PBT may validate optimization equivalence.

It is not optimization authority.

---

## 17. Interaction with Physical Layout Optimization

Contract-aware optimization and physical layout optimization are different layers.

This TODO owns semantic and realization simplification.

The FFM / Primitive Slabbing / Target-Aware Physical Layout TODO owns physical storage and layout.

The intended order is:

```text
Established Contract knowledge
        +
Verified Realization
        ↓
Contract-Aware Optimization
        ↓
optimized logical / realization material
        ↓
Physical Layout Planning
        ↓
FFM-backed slab realization
        ↓
JVM lowering
```

The exact backend pipeline may fuse some implementation stages.

The ownership boundary must remain clear.

---

## 18. Interaction with JVM and JIT Optimization

Kontrakt should not compete with the JVM on generic runtime optimization.

HotSpot or Graal already have strengths in:

```text
profile-guided inlining
escape analysis
GVN
LICM
loop optimization
vectorization
register allocation
instruction scheduling
machine-specific optimization
```

Kontrakt should optimize where it has unique information.

That includes:

```text
established Contract context
exact Contract authority relations
verified realization closure
exact admitted binding
known unreachable Contract alternatives
generated Contract machinery
```

The target is:

```text
Contract-rich program
        ↓
Kontrakt specialization
        ↓
simpler JVM product
        ↓
JIT optimization
```

Kontrakt should make the JVM's job easier.

It should not reimplement the entire JVM optimizer.

---

## 19. V1 Boundary

V1 should support useful Contract-aware optimization without requiring a large optimization framework.

The minimum useful V1 scope is:

```text
static discharge
fixed-context branch pruning
exact binding specialization
generated wrapper removal
simple stage fusion
cheap verification-derived scalarization
dead realization alternative removal
```

V1 should prefer transformations with:

```text
clear proof
low analysis cost
high expected payoff
simple invalidation
```

A transform that requires a new expensive analysis framework should normally wait.

---

## 20. V2 Direction

V2 may make Contract-aware optimization more persistent and more selective.

Possible extensions include:

```text
persistent optimization summaries
dependency-aware invalidation
fine-grained specialization reuse
early cutoff
profile-guided profitability
multiple target-specific products
incremental call-graph refinement
```

V2 may also combine fixed Contract context with richer target knowledge.

The semantic Contract model should not need to change for these improvements.

---

## 21. Persistent Cache Requirements

A specialized optimization product must be keyed by all material that justified it.

Possible inputs include:

```text
Contract semantic fingerprint
selected Contract context
verified realization fingerprint
compiler version
optimization configuration
target profile
```

The exact key schema is not fixed yet.

Cached optimization products are derived material.

They are not authority.

Deleting the cache may increase compilation time.

It must not change Contract meaning.

---

## 22. Profitability

Legality and profitability are separate.

A transformation may be legal and still not be useful.

The optimizer should eventually consider:

```text
runtime work removed
code-size growth
branch reduction
allocation reduction
lookup reduction
cache pressure
specialization multiplicity
compile-time cost
```

V1 may use simple deterministic heuristics.

Later versions may use richer cost models.

The profitability model must not affect Contract truth.

---

## 23. Required Correctness Tests

Every optimization must preserve Contract meaning.

At minimum, test:

```text
same accepted and refused Interaction space
same established Facts
same State / Transition meaning
same Failure attribution
same Publication and Output meaning
same applicable Contract context
same required diagnostics
```

User realization optimization must additionally preserve ordinary host-language observable behavior inside the governed
boundary.

Optimization tests should include:

```text
optimized vs unoptimized differential execution
PBT-derived equivalence cases
fixed-context specialization cases
realization-generation invalidation
cache reuse validation
```

A performance win does not excuse semantic drift.

---

## 24. Material to Review During ADR Migration

This TODO should be used when Realization and Optimization ADRs are migrated.

The review should locate material that currently treats these topics independently:

```text
Policy specialization
Governance-fixed execution
Version specialization
State-surface pruning
static discharge
dead realization removal
verification-derived optimization
wrapper elimination
JVM-ahead optimization
```

These should be unified under one ownership model.

The relevant ADR should state the architecture law.

Detailed transforms should remain in design documentation.

---

## 25. Migration Target

This TODO should eventually become two documents.

```text
Architecture / ADR
    optimization ownership
    fixed-context specialization law
    verification-to-optimizer publication boundary
    Contract/JVM optimization boundary
    V1/V2 extension seam

Design document
    transform catalog
    optimization IR/material
    profitability model
    cache keys
    invalidation
    pass/query organization
    implementation-specific rewrites
```

Exact bytecode rewrites should not be frozen into the ADR.

---

## 26. Open Decisions

The following items remain open:

```text
exact fixed-context material schema
optimizer input representation
verified-summary publication schema
specialization key granularity
user-local scalarization boundary
diagnostic preservation representation
optimization query/pass structure
V1 profitability heuristics
V2 cost-model scope
interaction with profile-guided optimization
```

These are compiler realization questions.

They must not be answered by changing Contract semantics.

---

## 27. Exit Criteria

This TODO is ready to leave planning status when:

```text
Contract Machinery Optimization ownership is defined
User Realization Optimization ownership is defined
pre-execution fixed context has an explicit consumer boundary
verification publishes reusable optimization facts
static discharge rules are explicit
unreachable-path rules are explicit
JVM/JIT responsibility boundary is documented
diagnostic preservation is tested
specialization cache validation is defined
V1 transform set is measured on representative workloads
V2 extension does not require semantic redesign
```

At that point, the accepted architecture should move into the relevant ADRs.

Detailed transforms should move into the compiler design documents.

Until then, this file remains the working Contract-aware optimization plan.