# Kontrakt V2 Incremental Architecture Research TODO

## Status

Research / TODO document.

This document does not select one incremental architecture.

It records current production techniques, recent research, and candidate combinations that may be evaluated for Kontrakt
V2.

This document does not define Contract semantics.

Incremental state, cache state, dependency state, prediction state, and physical graph state are derived compiler
material.

They must not become Contract authority.

---

## 1. Existing Kontrakt Direction

Kontrakt already has several useful foundations.

```text
stable semantic identity
    ↓
HID

large structured identity
    ↓
Merkle-style composition

published compiler material
    ↓
frozen / immutable products

short-lived reuse
    ↓
L1

process / wider reuse
    ↓
L2

future persistent reuse
    ↓
cross-session products
```

V2 does not need to replace this model.

The main open question is how change propagation, dependency tracking, partial recomputation, and persistent reuse
should be organized around it.

The following distinction should remain explicit:

```text
HID
    = identity / equality evidence / reuse validation

Merkle structure
    = hierarchical change localization

dependency graph
    = which derived computations depend on which material

incremental algorithm
    = how changed material is repaired or recomputed

cache tier
    = where reusable products are retained
```

These are related.

They are not the same subsystem.

---

## 2. Research Baseline

The current literature and production systems do not converge on one universal incremental algorithm.

Different systems use different models because their dependency shape and workload differ.

The main families found in the survey are:

```text
demand-driven red/green validation
push invalidation with change pruning
memoized dynamic dependency graphs
delta / differential maintenance
semi-naive recursive maintenance
elastic incremental / full-rebuild switching
priority worklists
heuristic incremental search
prediction-guided dynamic algorithms
graph reduction and reachability indexing
lazy invariant rebuilding
operation-level dependency tracing
shared maintained indexes
persistent structural knowledge reuse
```

Kontrakt can therefore treat V2 incrementality as a family of mechanisms rather than one compiler-wide algorithm.

---

# Part I. Production Compiler and Build-System Techniques

## 3. rustc Red-Green Query Evaluation

Current `rustc` uses a demand-driven query system.

Query execution discovers dependencies dynamically.

The previous query DAG is retained across compilation sessions.

The incremental algorithm attempts to prove that requested query results remain unchanged.

The basic flow is:

```text
need Q
    ↓
load previous reads(Q)
    ↓
validate dependencies in recorded order
    ↓
all green?
    ├─ yes → Q remains green
    └─ no  → recompute Q
                 ↓
             compare result fingerprint
                 ↓
          same → green
          changed → red
```

The dependency order is significant.

A query may conditionally execute another query.

A changed earlier dependency may change the later control path.

For that reason `rustc` does not freely reorder dependency validation inside a query.

### Techniques worth preserving as candidates

`rustc` provides several techniques that are independent of the exact red-green implementation.

#### Result fingerprinting

A recomputed result can stop downstream propagation when its stable fingerprint remains unchanged.

```text
input changed
    ↓
Q recomputed
    ↓
result fingerprint unchanged
    ↓
downstream cutoff
```

Kontrakt already has HID.

This makes the same principle directly expressible without adopting the whole rustc query model.

#### Projection queries

`rustc` uses small projection queries as change-propagation firewalls around volatile monolithic results.

```text
large product changed
        ↓
small projection A changed
small projection B unchanged
small projection C unchanged
        ↓
only A consumers continue
```

This is relevant to Kontrakt subsystem publication boundaries.

#### Selective hashing and evaluation

`rustc` supports cases where dependency tracking or result hashing is not profitable.

A cheap or almost-always-changing query may simply run again.

This supports a general rule:

```text
incremental bookkeeping cost
    >
recomputation cost
        ↓
recompute
```

#### Coarse backend integration

The LLVM backend does not use the normal query machinery at full granularity.

Codegen units are tracked as coarse products.

A green codegen unit can reuse its artifact.

A non-green codegen unit is rebuilt.

This is evidence that one incremental granularity does not fit every compiler subsystem.

### Limitation to keep visible

A rustc-style previous-run dependency trace is a dynamic execution trace.

It is not the same as an explicit semantic dependency graph.

This limits arbitrary greedy reordering.

A Kontrakt domain with explicit stable dependencies may not have the same restriction.

---

## 4. Salsa

Salsa uses revisioned incremental computation.

Inputs and tracked functions participate in a dependency graph.

Tracked function results are memoized.

A new revision checks whether dependencies changed.

The same broad red-green principle is used.

The useful lesson is not the library API.

The useful architectural ideas are:

```text
explicit inputs
pure-ish tracked computation
memoized result
dependency recording
revision identity
early reuse
```

Salsa remains a useful reference for small demand-driven incremental domains.

It does not imply that all Kontrakt subsystems should become Salsa-style queries.

---

## 5. Buck2 DICE

Buck2 uses DICE as a dynamic incremental computation engine.

A computation key can request other keys.

Those requests become dependencies.

DICE exposes equality behavior for recomputed values.

If an invalidated node recomputes to an equal value, dependent computation can be resurrected rather than propagated as
changed.

This is close to HID-based early cutoff.

```text
node invalidated
    ↓
recompute
    ↓
equal result
    ↓
dependents can remain reusable
```

DICE also distinguishes transient values that should not be reused across versions.

This is relevant to Kontrakt products that are valid only inside one generation.

Buck2 also reduces graph pressure through structures such as transitive sets.

The system does not treat every logical transitive relation as a fully expanded dependency graph.

Buck2 supports dynamic dependencies where some dependency information becomes known only after another artifact is
produced.

This is relevant to any Kontrakt realization analysis whose reachable implementation graph is discovered during
analysis.

### DICE details worth investigating

```text
result equality behavior
generation / transaction model
transient-result handling
dynamic dependencies
graph reduction
paged or serialized values
invalidation provenance
```

The current DICE source also tracks normal and high-priority invalidation source paths.

That mechanism is for invalidation provenance.

It should not be confused with work scheduling priority.

---

## 6. Bazel Skyframe

Skyframe uses dependency invalidation and change pruning.

Changed input material invalidates dependent nodes.

A rebuilt node whose value remains equal can stop further change propagation.

The important distinction is:

```text
dirty
    ≠
changed
```

This is directly compatible with HID.

Skyframe also demonstrates a practical bottom-up invalidation model.

It is useful as a comparison against rustc-style lazy validation.

The architecture question for Kontrakt is not whether push or pull is universally better.

The question is which dependency domains have enough explicit structure to make push invalidation cheaper than recursive
validation.

---

## 7. Pants Engine

Pants builds a runtime dependency graph from typed rules.

Rule results are recursively memoized and invalidated.

The rule graph is also statically analyzed at startup.

This is useful because it separates two kinds of knowledge:

```text
static possible dependency structure
        +
runtime computation graph
```

Kontrakt may have a similar distinction.

Some dependencies are explicit from compiler structure.

Some realization dependencies are only discovered after target resolution or analysis.

---

# Part II. Incremental Database and Dataflow Techniques

## 8. Differential Dataflow

Differential Dataflow does not treat incremental work primarily as cache invalidation.

It represents changes as differences.

```text
(data, logical time, diff)
```

Operators consume input differences and produce output differences.

The central model is:

```text
old state
    +
Δinput
    ↓
Δoutput
```

rather than:

```text
input changed
    ↓
invalidate result
    ↓
recompute whole result
```

Differential Dataflow supports iterative computations.

It has been used for graph algorithms including SCC-style iterative problems.

This makes it relevant to compiler analyses based on relations or fixpoints.

Possible Kontrakt candidates include:

```text
points-to relations
effect relations
reachability
provenance relations
call-target sets
dependency sets
applicability sets
```

These are candidates only when their mathematical update structure supports useful deltas.

---

## 9. Shared Arrangements

Differential Dataflow uses maintained indexed representations called arrangements.

Multiple operators can share one maintained index.

Multiple dataflows can also share the same arrangement.

The purpose is to avoid repeated construction of identical indexes.

```text
relation
    ↓
shared maintained arrangement
    ├─ consumer A
    ├─ consumer B
    └─ consumer C
```

This is directly relevant to Kontrakt.

Several subsystems may need the same relation in indexed form.

Examples may include:

```text
Operation → Fact
Fact → consumer
caller → callee
callee → callers
authority → occurrences
HID → dense ref
```

A shared index or arrangement layer could prevent each subsystem from rebuilding equivalent indexes.

This is separate from result caching.

---

## 10. DBSP

DBSP gives a general incremental view maintenance construction for a rich query language.

It supports relational algebra, sets, multisets, aggregation, nested relations, and recursive computations.

Its important property is compositional incrementalization.

A complex computation can be transformed into an incremental form from the incremental forms of its components.

This suggests a possible Kontrakt direction for relational analysis domains:

```text
analysis expression
    ↓
incrementalized analysis plan
    ↓
delta-maintained products
```

This is very different from a compiler-wide memoized query engine.

It may be useful only inside selected analysis domains.

---

## 11. Soufflé Semi-Naive Evaluation

Soufflé uses semi-naive evaluation for recursive Datalog.

New tuples from the previous round are isolated as delta relations.

A recursive rule is evaluated using the new delta rather than repeatedly joining the full old relation with itself.

Conceptually:

```text
R
+
ΔR
    ↓
new consequences from ΔR
```

This is relevant to recursive program analysis.

It is especially relevant to:

```text
call graph closure
points-to
effect propagation
reachability
recursive provenance
```

when represented relationally.

---

## 12. Elastic Incrementalization

Soufflé research on elastic incrementalization explicitly compares incremental repair against full recomputation.

Small changes may use incremental update.

High-impact changes may use a bootstrap or full-recompute strategy.

```text
change impact small
    ↓
incremental update

change impact large
    ↓
bootstrap / recompute
```

The published work reports that one incremental strategy is not optimal across all update sizes.

This directly supports a Kontrakt V2 mode switch.

The switch should be a performance decision.

It must not affect correctness.

---

# Part III. Dynamic Graph Algorithms

## 13. Priority Worklists

Galois provides soft-priority worklists.

Ordered By Integer Metric groups work into integer-priority bins.

Priority inversion is allowed when it does not affect correctness.

This avoids the cost of a strict total-order scheduler.

The useful pattern is:

```text
active work
    ↓
small number of priority buckets
    ↓
higher-value work first
```

For Kontrakt this suggests bucketed scheduling rather than a heap for every dirty node.

Possible scheduling metrics are still open.

Examples to measure include:

```text
expected downstream work
cut potential
result stability
delta magnitude
recompute cost
fan-out
critical-path contribution
```

No one metric should be assumed correct before benchmark evidence.

---

## 14. GraphIt Ordered Processing

GraphIt supports ordered graph algorithms.

Its priority extension allows dynamic processing order.

Bucket fusion reduces synchronization between priority rounds.

This is relevant if Kontrakt uses priority frontiers in parallel.

A strict round barrier for every priority level may erase the benefit of prioritization.

Possible lesson:

```text
soft ordered frontier
+
bucket fusion
+
parallel chunks
```

may be preferable to a globally synchronized priority heap.

---

## 15. Residual Scheduling

Residual scheduling processes the work item with the largest remaining inconsistency or expected update effect.

The idea appears in iterative probabilistic graph algorithms and related worklist methods.

The important distinction is:

```text
high fan-out
    ≠
high actual change
```

A node with many descendants may produce no output delta.

A smaller node may produce a large delta.

Kontrakt may therefore distinguish structural impact from observed residual.

Possible metrics include:

```text
structural fan-out
historical output delta
current input delta
historical cutoff rate
```

---

## 16. Lifelong Planning A* and D* Lite

Incremental heuristic search reuses previous search state.

It does not repair the whole graph after every change.

It focuses on inconsistent vertices that matter to restoring the requested solution.

This supports two useful ideas.

### Inconsistency frontier

```text
changed graph
    ↓
locally inconsistent states
    ↓
repair frontier
```

### Goal-directed repair

A changed region does not always need immediate full repair.

Repair can be restricted to material needed for the requested result.

This is relevant to demand-driven compilation.

An affected compiler product does not necessarily need to be rebuilt until a requested output depends on it.

---

## 17. Dynamic Transitive Reduction

ICALP 2025 introduced fully dynamic algorithms for maintaining transitive reduction in directed graphs.

A transitive reduction preserves reachability while removing redundant edges.

This does not mean a compiler dependency graph can blindly delete all transitive edges.

A direct dependency may carry meaning beyond reachability.

However, derived propagation graphs may contain edges whose only purpose is reachability.

Those graphs may benefit from reduction.

Potential use:

```text
semantic dependency graph
    ↓
derived propagation graph
    ↓
reachability-preserving reduction
```

The semantic graph remains authoritative.

The reduced graph is a derived optimization structure.

---

## 18. Incremental Reachability Index

SEA 2025 studies an incremental reachability index for append-only DAGs.

It provides immutable per-node index data and different query-time / memory trade-offs.

This is relevant if Kontrakt maintains large DAG-like dependency domains.

A reachability index could answer:

```text
does changed product A affect product B?
```

without traversing the graph each time.

The cost is index memory and update work.

This should therefore be benchmarked against direct traversal.

---

## 19. SCC Condensation

Compiler analysis graphs may contain legal cycles.

User realization call graphs are one example.

Contract semantic cycles remain a different problem and may be illegal.

For legal recursive analysis graphs, SCC condensation produces a DAG.

```text
general graph
    ↓
SCC condensation
    ↓
DAG of SCC regions
```

This enables two levels of incremental processing.

```text
inside SCC
    → local fixpoint or specialized delta algorithm

between SCCs
    → DAG propagation
```

Recent 2026 work also studies incremental SCC maintenance with predictions.

This provides a current research direction for SCC-heavy domains.

---

# Part IV. Prediction and Learning-Augmented Algorithms

## 20. Dynamic Graph Algorithms with Predictions

SODA 2024 studies dynamic graph algorithms with predictions.

The algorithm receives an imperfect prediction of future updates.

Good predictions improve runtime.

The bounds degrade smoothly with prediction error.

The key architecture lesson is not machine learning itself.

The useful pattern is:

```text
prediction
    ↓
fast path

prediction error
    ↓
bounded degradation / safe fallback
```

Prediction must not carry correctness authority.

---

## 21. Incremental Topological Ordering with Predictions

ICML 2024 applies predictions to incremental topological ordering and cycle detection.

The work is directly relevant because compiler dependency graphs often require topological structure and cycle handling.

It combines prediction with robust algorithmic behavior.

This suggests possible use of historical build/update order without making the history authoritative.

Potential prediction inputs include:

```text
which files usually change together
which products usually change together
edge insertion order
historical invalidation paths
historical topological disturbance
```

---

## 22. Incremental SCC with Predictions

SWAT 2026 presents a learned data structure for incremental SCC.

Good predictions approach near-offline behavior.

Performance degrades with prediction error.

The authors also report practical implementation results.

The paper is relevant to Kontrakt only for graph domains where SCC maintenance itself is important.

It does not justify a prediction layer across every compiler subsystem.

---

## 23. Historical Stability as a Lightweight Prediction

Kontrakt does not need a machine-learning model to experiment with prediction-guided scheduling.

Simple historical material can act as a prediction.

```text
dirtyCount
changedCount
averageComputeCost
averageOutputDelta
averageDownstreamPropagation
cutoffCount
```

This can produce a derived score.

The score may affect scheduling only.

A wrong score must not change the result.

---

# Part V. Other Incremental Techniques

## 24. Self-Adjusting Computation and Traceable Data Types

Self-adjusting computation traditionally traces low-level reads and writes.

Research on traceable data types showed that dependency tracking at memory-cell granularity can create too many
dependencies.

Tracing abstract data-type operations can reduce dependency count and improve asymptotic behavior.

This is highly relevant to Kontrakt.

A dependency graph should not automatically contain one edge for every primitive memory read.

Possible dependency levels include:

```text
field read
table range
semantic projection
published product
abstract operation
subsystem result
```

The correct level should reflect invalidation precision and tracking cost.

This supports product-level dependency compression.

---

## 25. Lazy Rebuilding from e-Graphs

`egg` delays restoration of expensive e-graph invariants.

Multiple updates are accumulated.

A later rebuilding phase restores invariants in a batch.

The point is:

```text
maintain invariant after every update
    may be expensive

batch updates
    ↓
repair once
```

Kontrakt may have derived indexes or summaries whose invariants do not need immediate repair after every low-level
update.

A generation barrier or publication boundary may be a legal repair point.

This applies only to derived compiler structures.

Authoritative Contract meaning must not become temporarily inconsistent.

---

## 26. Incremental SAT Knowledge Reuse

Incremental SAT and recent incremental model-counting work reuse learned information across related solver instances.

Recent 2026 work on incremental model counting also investigates persistent component caching and branching heuristics.

The broad lesson is:

```text
do not retain only final results

retain reusable structural knowledge
```

For Kontrakt, analogous reusable knowledge may include:

```text
SCC summaries
reachability indexes
exact-binding summaries
arrangements
dominance summaries
stable partitioning
specialization facts
```

The usefulness depends on invalidation cost.

---

# Part VI. Techniques to Evaluate for Kontrakt

## 27. HID Early Cutoff

This is the most direct extension of the current HID model.

```text
producer recomputed
    ↓
new HID == old HID
    ↓
do not propagate change
```

This is present in different forms in rustc, DICE, Skyframe, and incremental build systems.

The main TODO is granularity.

A large product HID may change too often.

A product split into too many HIDs may create excessive graph and hashing overhead.

---

## 28. Merkle Change Localization

Large structured products can use hierarchical HID composition.

```text
Root HID
    ├─ Region A HID
    │    ├─ leaf
    │    └─ leaf
    └─ Region B HID
```

One changed leaf updates only its Merkle path.

This can localize changed regions before dependency propagation.

The Merkle hierarchy does not replace dependency edges.

It answers a different question.

```text
Merkle
    → what structural region changed?

dependency graph
    → what computation consumes it?
```

---

## 29. Projection Firewalls

A volatile large product can publish stable small projections.

```text
Large Product
    ├─ Projection A
    ├─ Projection B
    └─ Projection C
```

Consumers depend on the smallest projection that contains the required meaning.

This reduces false invalidation.

This is supported directly by rustc experience.

For Kontrakt it may map naturally to explicit subsystem material.

---

## 30. Product-Level Dependency Compression

Cross-subsystem dependencies do not need to expose every internal query edge.

```text
Verifier internal graph
        ↓
Verified Binding Summary HID
        ↓
Optimizer
```

This can reduce global graph size.

Fine-grained dependency tracking can remain local to the owning subsystem.

The summary granularity remains an open design choice.

---

## 31. Shared Arrangements and Indexes

If multiple analyses need the same relation index, maintain it once.

Possible shared derived structures include:

```text
reverse dependency index
caller / callee index
Fact consumer index
authority occurrence index
HID index
reachability index
```

The index should have explicit ownership and lifetime.

Shared indexes must not become hidden semantic authority.

---

## 32. Active Change Frontier

Instead of recursively proving unchanged nodes, a domain can start from confirmed changed material.

```text
confirmed changed products
        ↓
active frontier
        ↓
consumers
```

A consumer is recomputed or delta-updated.

If its HID remains unchanged, propagation stops.

```text
changed input
    ↓
consumer update
    ↓
same HID
    → stop

different HID
    → activate consumers
```

This is a push-oriented alternative to rustc-style pull validation.

It is most natural where reverse dependencies are explicit and stable.

---

## 33. One-Shot Generation Marking

An affected node should not be repeatedly scheduled in the same generation without a semantic reason.

A primitive generation stamp can record scheduling state.

```text
scheduledGeneration[node]
processedGeneration[node]
```

This can eliminate repeated visited-set hashing.

The exact state machine depends on whether a node can receive multiple deltas before processing.

Batching may be required.

---

## 34. Soft-Priority Frontiers

A priority frontier can schedule high-value repair work first.

Strict heaps may add large overhead.

Bucketed soft priority is a candidate based on graph-processing systems.

Potential categories could include:

```text
cut boundary
high expected downstream cost
ordinary
low-value expensive work
```

The exact buckets must be measured.

Priority must affect performance only.

---

## 35. Cut Potential

A node may be valuable to evaluate early when an unchanged result would block a large region.

A derived metric may estimate:

```text
downstream work avoided
×
probability of unchanged result
÷
recompute cost
```

This formula is only an example.

The exact metric is not selected.

Possible inputs include:

```text
fan-out
dominator-like region size
historical cutoff rate
average consumer cost
product stability
```

The metric should not require graph work larger than the work it saves.

---

## 36. Residual / Delta Magnitude

Structural impact alone may be misleading.

A high-fan-out node can produce no output change.

A low-fan-out node can produce a large change.

A scheduler may therefore use current or historical delta magnitude.

```text
structural impact
        +
observed delta
```

This can be evaluated against pure fan-out scheduling.

---

## 37. Elastic Incremental / Full-Rebuild Switch

Incremental repair is not always cheaper.

A domain can estimate:

```text
incremental detection cost
+
repair cost
+
bookkeeping cost
```

and compare it with:

```text
full recompute cost
```

A threshold can switch modes.

This is supported by dynamic-graph and Datalog research.

The threshold may be static in V1 prototypes.

It may become profile-based in V2 experiments.

---

## 38. Delta-Maintained Analysis

Some derived products can be updated from deltas.

```text
old relation
+
added refs
-
removed refs
    ↓
updated relation
```

This should be considered for relational and recursive analysis.

It should not be forced onto arbitrary black-box compiler computations.

A product needs an update algebra before delta maintenance is meaningful.

---

## 39. Specialized Dynamic Graph Algorithms

Some graph products should use a problem-specific dynamic algorithm rather than generic invalidation.

Candidates include:

```text
topological order
cycle detection
SCC
reachability
transitive reduction
shortest-path-like cost propagation
```

The current literature contains specialized incremental and fully dynamic algorithms for these problems.

A general query engine should not replace a better domain algorithm.

---

## 40. Lazy Derived-Structure Repair

Some derived indexes can accumulate updates and repair at a publication barrier.

This can reduce repeated maintenance.

Possible form:

```text
many local updates
    ↓
dirty derived index
    ↓
single rebuild before consumer publication
```

This follows the same broad principle as e-graph rebuilding.

It must not expose stale material to a consumer that requires a current result.

---

## 41. Prediction-Guided Scheduling

Historical change patterns can predict which node or region is likely to matter.

Current dynamic-graph research provides algorithms that use predictions with robust fallback.

Possible Kontrakt uses include:

```text
frontier priority
prewarming likely products
prefetching persistent cache
choosing incremental vs rebuild mode
choosing graph region order
```

Prediction must remain optional derived material.

No correctness path may depend on a prediction being accurate.

---

## 42. Reachability and Graph Reduction

A large dependency graph may benefit from a derived compressed propagation structure.

Possible techniques include:

```text
SCC condensation
transitive reduction
reachability labels
2-hop-style reachability index
region summaries
```

Each structure trades memory, maintenance cost, and query speed.

The semantic dependency graph should remain separate from any compressed propagation graph.

---

# Part VII. Candidate V2 Architectures

## 43. Candidate A — HID Red-Green Query Architecture

This is the closest architecture to rustc and Salsa.

```text
requested product
    ↓
query lookup
    ↓
validate recorded dependencies
    ↓
all unchanged?
    ├─ yes → reuse
    └─ no  → recompute
                 ↓
             compare HID
                 ↓
          same → cutoff
          changed → propagate on demand
```

### Components

```text
HID
Merkle input identity
query key
recorded dependency order
memoized result
persistent query graph
projection firewalls
selective no-cache / no-hash policy
```

### Suitable conditions

This candidate fits computations whose dependency set is discovered dynamically during evaluation.

It also fits domains where only a small requested subset of the compiler graph is normally demanded.

### Costs to measure

```text
recursive validation cost
dependency-order storage
query memoization overhead
stable HID cost
persistent graph size
dynamic control-flow dependency changes
```

### V2 extension

Fine-grained persistence and result-HID early cutoff can be added without changing the basic pull model.

---

## 44. Candidate B — HID Change-Frontier Architecture

This candidate starts from confirmed changed products.

It uses reverse dependencies.

```text
Merkle/HID detects changed products
        ↓
active changed frontier
        ↓
consumer update
        ↓
new consumer HID
    ├─ same → stop
    └─ changed → enqueue consumers
```

### Components

```text
Merkle change localization
reverse dependency index
generation stamps
active frontier
HID early cutoff
subsystem-local graph
optional soft-priority buckets
```

### Suitable conditions

This candidate fits dependency domains where reverse edges are explicit and stable.

It also fits workloads where the changed set is much smaller than the total graph.

### Costs to measure

```text
reverse-index maintenance
work performed for outputs not requested
frontier duplication
high-fan-out propagation
parallel scheduling overhead
```

### Demand filtering

A top-level demanded-region mask can be combined with the push frontier.

```text
changed frontier
        ∩
currently demanded region
```

This can avoid repairing unrelated outputs.

---

## 45. Candidate C — Domain-Local Hybrid Architecture

This candidate does not use one compiler-wide propagation algorithm.

Each subsystem owns its local incremental model.

Subsystem boundaries publish explicit products with HIDs.

```text
Frontend Domain
    local incremental algorithm
        ↓
Published Frontend Product HID
        ↓
Analysis Domain
    local incremental algorithm
        ↓
Published Analysis Product HID
        ↓
Verification Domain
        ↓
Optimization Domain
        ↓
Backend Domain
```

### Possible local algorithms

```text
Frontend
    → projection queries / Merkle products

Graph Analysis
    → delta + SCC-local algorithms

Verifier
    → query reuse + exact summary HIDs

Optimizer
    → selective memoization / recompute

Backend
    → method/class product reuse
```

### Cross-domain law

A downstream domain sees only the published dependency material it consumes.

It does not need the full internal graph of the producer.

### Suitable conditions

This candidate fits Kontrakt's existing subsystem ownership model.

It also allows different domains to adopt different SOTA algorithms.

### Costs to measure

A coarse boundary can create false invalidation.

A very fine boundary can recreate a global query graph.

Published product granularity therefore becomes a primary design variable.

---

## 46. Candidate D — Differential Analysis + Query Shell

This candidate uses delta maintenance inside relation-heavy analyses.

Other compiler work remains query- or product-based.

```text
source / IR product changes
        ↓
Δfacts / Δrelations
        ↓
Differential Analysis Domain
    shared arrangements
    semi-naive / differential updates
        ↓
Analysis Summary HID
        ↓
Verifier / Optimizer / Backend
    ordinary query or product reuse
```

### Candidate analysis products

```text
call relation
points-to
effect relation
provenance
reachability
applicability relation
```

### Components

```text
delta streams
shared arrangements
relation indexes
recursive incremental evaluation
HID summary boundaries
ordinary backend cache
```

### Suitable conditions

This candidate fits analyses with explicit relational structure.

It avoids forcing delta algebra onto ordinary tree transforms or backend emission.

### Costs to measure

```text
persistent relation-state memory
arrangement maintenance
delete handling
recursive delta amplification
translation cost from IR to relation deltas
```

---

## 47. Candidate E — Elastic Multi-Mode Architecture

This candidate selects a maintenance mode per product or domain.

```text
change arrives
    ↓
estimate update size / repair cost
    ↓
┌─────────────────────────────┐
│ reuse                       │
│ delta update                │
│ local recompute             │
│ region rebuild              │
│ full-domain rebuild         │
└─────────────────────────────┘
```

### Components

```text
HID/Merkle
cost telemetry
change-size estimate
incremental/full threshold
product-specific update strategy
```

### Suitable conditions

This candidate fits workloads with highly variable change sizes.

It follows elastic Datalog and dynamic-graph processing research.

### Required constraint

The mode selector controls cost only.

All modes must produce equivalent semantic output.

---

## 48. Candidate F — Prediction-Guided Cut-First Hybrid

This candidate adds prediction only to scheduling and mode selection.

The underlying correctness algorithm remains deterministic.

```text
confirmed affected candidates
        ↓
derive priority
        ↓
cut-first soft-priority frontier
        ↓
recompute / delta update
        ↓
HID unchanged?
    ├─ yes → cut downstream
    └─ no  → continue
```

### Possible priority inputs

```text
historical stability
historical cutoff rate
average recompute cost
estimated downstream work
current delta magnitude
fan-out
region position
predicted change correlation
```

### Safe fallback

```text
prediction weak or wrong
    ↓
ordinary frontier / ordinary recompute
```

### Suitable conditions

This candidate fits domains where work ordering does not affect correctness.

It is not suitable for dynamic dependency traces whose recorded order is semantically required for safe replay.

### Research basis

The design is related to:

```text
algorithms with predictions
soft-priority graph worklists
residual scheduling
incremental heuristic search
```

No single cited system implements this exact compiler architecture.

It is a candidate combination of established techniques.

---

# Part VIII. Valid Candidate Combinations

## 49. Combination 1 — Conservative Compiler Baseline

```text
HID + Merkle
+
rustc-style demand-driven validation
+
projection firewalls
+
selective persistence
+
coarse backend product reuse
```

This combination has strong precedent in compiler and build-system practice.

It minimizes new graph-algorithm machinery.

It retains dynamic dependency discovery.

Main comparison target:

```text
rustc / Salsa class of systems
```

---

## 50. Combination 2 — Explicit-Graph Change Propagation

```text
HID + Merkle
+
reverse dependency CSR
+
generation-marked active frontier
+
HID early cutoff
+
soft-priority cut boundaries
+
elastic full-rebuild threshold
```

This combination uses dynamic-graph and build-system techniques.

It assumes that dependency edges are explicit enough for push propagation.

Main comparison targets:

```text
Skyframe-style invalidation
Galois-style worklists
elastic dynamic graph processing
```

---

## 51. Combination 3 — Domain-Specialized Hybrid

```text
small common HID/reuse kernel
+
subsystem-local dependency domains
+
projection boundaries
+
relational delta maintenance where applicable
+
query reuse elsewhere
+
coarse direct backend products
```

This combination uses different algorithms by domain.

Main comparison targets:

```text
rustc backend split
Buck2 DICE
Differential Dataflow
DBSP
Soufflé
```

---

## 52. Combination 4 — Differential Middle-End

```text
Merkle source / IR products
+
Δ relation generation
+
shared arrangements
+
semi-naive / differential recursive analyses
+
HID summary cutoff
+
ordinary optimizer/backend caching
```

This combination moves most sophisticated incrementality into the middle-end analysis layer.

Frontend and backend remain simpler.

Main comparison targets:

```text
Differential Dataflow
DBSP
Soufflé static analysis
```

---

## 53. Combination 5 — Prediction-Guided Elastic Hybrid

```text
Domain-Specialized Hybrid
+
historical telemetry
+
prediction-guided soft priority
+
cut-potential scheduling
+
incremental/full rebuild switching
+
safe deterministic fallback
```

This combination adds learning-augmented techniques after a deterministic incremental baseline exists.

Main comparison targets:

```text
ICML 2024 incremental topological ordering with predictions
SODA 2024 dynamic graph algorithms with predictions
SWAT 2026 incremental SCC with predictions
Galois / GraphIt ordered processing
elastic Datalog
```

---

## 54. Combination 6 — Compressed Graph Hybrid

```text
Domain-Specialized Hybrid
+
SCC condensation
+
derived transitive reduction where legal
+
reachability index
+
region summary HIDs
+
demand-filtered frontier
```

This combination focuses on reducing graph traversal cost before adding scheduling heuristics.

Main comparison targets:

```text
ICALP 2025 dynamic transitive reduction
SEA 2025 incremental reachability indexing
traceable data types
Buck2 transitive-set graph reduction
```

---

# Part IX. Questions That Must Be Measured

## 55. Dependency Shape

Measure real Kontrakt graphs.

Required distributions include:

```text
node count
edge count
fan-out
fan-in
depth
SCC count
SCC size
cross-subsystem edge count
transitive redundancy
dynamic-dependency frequency
```

Without this data, graph algorithm selection is speculative.

---

## 56. Change Shape

Measure change workloads.

At minimum:

```text
single declaration edit
single Operation implementation edit
Policy-only edit
Governance-only edit
Fact shape edit
large interface edit
backend target-profile change
compiler version change
generated product-only change
```

Measure both syntactic delta and semantic HID delta.

---

## 57. Stability

For each reusable product, record:

```text
dirty count
actual HID change count
recompute cost
result size
downstream fan-out
downstream measured work
cache hit rate
persistent hit rate
```

This data can evaluate whether prediction-guided scheduling is useful.

---

## 58. Tracking Cost

Incrementality has its own cost.

Measure:

```text
dependency edge recording
HID computation
Merkle update
reverse-index maintenance
persistent graph I/O
priority bookkeeping
generation marking
cache lookup
cache serialization
```

Compare the sum against plain recomputation.

---

## 59. Memory Cost

Different architectures retain different state.

Measure:

```text
query results
dependency graph
reverse graph
arrangements
reachability indexes
historical telemetry
persistent generations
Merkle nodes
```

A faster incremental compile may still be unsuitable if daemon memory becomes unstable.

---

# Part X. Required Prototype Matrix

## 60. Reference Implementations

Build small prototypes against the same deterministic workload.

Candidates:

```text
R0  full recompute

R1  HID coarse cache

R2  rustc-style pull red-green

R3  push change frontier

R4  domain-local hybrid

R5  relational delta middle-end

R6  elastic incremental/full switch

R7  prediction-guided soft-priority frontier
```

R7 should be tested only after its deterministic fallback is available.

---

## 61. Synthetic Graph Shapes

Use synthetic dependency graphs in addition to real projects.

Required shapes:

```text
deep chain
wide fan-out
wide fan-in
diamond graph
many stable cut points
few cut points
highly transitive DAG
SCC-heavy graph
dynamic conditional dependencies
large changed region
tiny changed region
```

This prevents one project shape from deciding the architecture accidentally.

---

## 62. Change Ratios

At minimum test:

```text
0.01%
0.1%
1%
5%
20%
50%
100%
```

The crossover point between incremental repair and full recomputation should be measured.

It should not be assumed.

---

## 63. Scheduling Comparisons

For frontier-based candidates compare:

```text
FIFO
LIFO
topological order
fan-out order
cut-potential order
residual / delta order
bucketed historical-stability order
prediction-guided order
```

Measure scheduler overhead separately from work saved.

---

## 64. Graph Compression Comparisons

Compare:

```text
raw graph
SCC-condensed graph
region-summary graph
derived transitive reduction
reachability-index-assisted graph
```

Track both update and query cost.

---

# Part XI. V1 Compatibility Requirements

## 65. V1 Should Not Require the Final V2 Engine

V1 can continue to use current HID-based L1/L2 reuse.

The V1 implementation should preserve seams needed by all candidate V2 architectures.

Required properties are:

```text
explicit product inputs
deterministic computation
frozen outputs
stable HID
clear product ownership
clear lifetime
replaceable cache tier
no hidden global mutable dependency state
```

These properties are useful regardless of which V2 candidate wins.

---

## 66. Frontend Requirement

Frontend products should expose stable granular identities.

A volatile source file should not force every downstream consumer to depend on one monolithic identity if smaller stable
semantic products can be published.

Do not select the final projection granularity yet.

Measure it.

---

## 67. IR Requirement

IR is not the incremental graph.

IR is compiler representation.

A query, delta engine, or change frontier may produce or consume IR products.

IR nodes should not be forced to become query nodes merely to support V2.

---

## 68. Middle-End Requirement

Analysis products should declare whether they are:

```text
cheap recompute
memoizable
delta-maintainable
recursive / SCC-based
shared-index consumer
persistent candidate
```

This classification can remain design metadata.

It is not Contract semantics.

---

## 69. Backend Requirement

Backend products should remain directly reusable at a natural granularity.

Possible granularity includes:

```text
method
class
interface product
target-profile product
```

The exact granularity is not selected.

The backend should not require a compiler-wide fine-grained query graph if method/class product reuse is sufficient.

---

# Part XII. Architecture Laws to Preserve Across Candidates

## 70. Authority Separation

```text
Contract authority
    ≠
incremental cache

Contract authority
    ≠
dependency graph

Contract authority
    ≠
prediction

Contract authority
    ≠
historical profile
```

Deleting all incremental state may make compilation slower.

It must not change Contract meaning.

---

## 71. Prediction Separation

Prediction may change:

```text
work order
prefetch order
repair mode
priority
```

Prediction must not change:

```text
semantic result
legality
Contract judgment
Failure meaning
```

---

## 72. Graph Separation

A propagation graph may be reduced or indexed.

The semantic dependency relation must remain available independently when it carries semantic meaning.

A transitive reduction is therefore a derived physical graph unless a later design proves otherwise.

---

## 73. Recompute Fallback

Every specialized incremental algorithm should have a deterministic recomputation path.

This provides:

```text
correctness reference
debug path
differential test oracle
fallback for large updates
recovery from invalid cache state
```

---

## 74. Incremental Equivalence Testing

Every candidate architecture must be tested against full recomputation.

```text
same input generation
    ↓
incremental result
full-recompute result
    ↓
same canonical result / HID
```

PBT can generate change sequences.

This is separate from Contract-generated user testing.

---

# Part XIII. Open Research Questions

## 75. Global Graph or Domain Graphs

Open question:

```text
one compiler-wide dependency graph
```

versus:

```text
multiple local dependency graphs
+
published HID boundaries
```

Both models exist in current systems.

The answer should follow measured dependency shape and memory cost.

---

## 76. Pull, Push, or Hybrid

Open question:

```text
pull validation
push invalidation
push change frontier
demand-filtered push
domain-specific hybrid
```

No universal winner is established by current literature.

---

## 77. Static and Dynamic Dependencies

Some Kontrakt dependencies may be explicit before execution.

Others may be discovered during realization analysis.

The architecture may need two dependency classes.

```text
declared stable dependency
dynamic discovered dependency
```

They may use different incremental algorithms.

---

## 78. Priority Metric

Open candidates include:

```text
fan-out
downstream estimated cost
cut potential
historical stability
current delta magnitude
critical path
prediction score
```

The metric should not be fixed before workload measurement.

---

## 79. Incremental / Rebuild Threshold

The threshold may be based on:

```text
changed node ratio
changed edge ratio
estimated affected work
measured frontier growth
delta cardinality
historical cost model
```

A fixed threshold is simpler.

A profile-driven threshold may be more accurate.

Both should be tested.

---

## 80. Persistent State

Open question:

Which material is worth persisting?

Candidates include:

```text
HIDs
Merkle tree
published products
dependency graph
reverse graph
analysis arrangements
SCC summaries
reachability index
historical cost profile
backend products
```

Persistence cost must be included in benchmark results.

---

