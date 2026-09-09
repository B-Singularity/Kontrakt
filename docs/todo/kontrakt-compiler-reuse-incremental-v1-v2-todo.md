# Kontrakt Compiler Reuse and Incremental Architecture TODO

## Status

**Working TODO. Not an ADR.**

This document defines the work required to make the V1 compiler reusable, cacheable, and V2-compatible without coupling
compiler semantics to one implementation strategy.

It does not define Contract semantics.

`What Contract Is` and Accepted ADRs remain authoritative.

The current compiler architecture map remains the working structural baseline.

---

# 1. Goal

V1 should reuse expensive compiler work where the result is still valid.

V2 should be able to replace the reuse, invalidation, repair, and scheduling mechanisms without rewriting compiler
semantics.

The main rule is:

```text
Compiler-Internal Contract
    ↓ constrains
Compiler Implementation
```

The reverse is not allowed.

---

# 2. Required Separation

Kontrakt needs three different layers.

```text
Domain Contract
    What Contract Is
    1D Contract semantics

Compiler-Internal Contract
    IR contracts
    analysis contracts
    product contracts
    publication contracts
    transform contracts
    backend contracts
    reuse / validity contracts

Compiler Implementation
    tables
    slabs
    HID
    BLAKE3
    L1 / L2
    query engine
    scheduler
    pass manager
    Merkle structure
    incremental repair
    FFM
    classfile encoder
```

The compiler must apply the same Contract / Implementation separation internally that Kontrakt requires from user
systems.

---

# 3. V1 Principle

V1 should freeze the semantic and product boundaries.

It should not freeze one incremental algorithm.

V1 must define:

```text
what a product means
what determines it
what counts as equal
when it is valid
what may consume it
what invalidates it
how complete publication is observed
```

V1 may implement those rules with HID, BLAKE3, frozen material, L1 / L2 reuse, and query-oriented orchestration.

Those mechanisms remain replaceable.

---

# 4. V2 Compatibility Principle

V2 may change:

```text
dependency representation
change propagation
repair strategy
scheduling
persistent storage
materialization policy
cache retention
incremental / full-rebuild selection
```

V2 must not require a semantic rewrite of:

```text
Resolved Contract HIR
Canonical Contract World
Realization Body IR
analysis result meaning
verification result meaning
Contract-Aware Execution IR
JVM Plan / IR
compiler product meaning
```

---

# 5. Product Contract

Every reusable compiler product needs an explicit internal contract.

A product contract should define:

```text
Product Identity
Determining Inputs
Result Meaning
Result Equality
Validity
Dependency Boundary
Publication Rule
Schema Version
```

The product contract must not depend on the physical cache layout.

Example:

```text
Contract-Aware Analysis Result
```

may have one product contract while V1 uses full recomputation and V2 uses incremental repair.

The consumers should not change.

---

# 6. Result Equality

Early cutoff depends on result equality.

Result equality must come from the owning product contract.

```text
Product Contract
    ↓
defines equality

Fingerprint
    ↓
accelerates equality
```

The compiler must not define semantic equality as:

```text
same hash
```

HID or fingerprint equality may be fast evidence.

Exact validation remains available where the product contract requires it.

---

# 7. Identity Separation

Keep these roles separate.

```text
Contract Semantic Identity
Compiler Product Identity
Product Input Key
Product Content Fingerprint
Storage Identity
Publication Generation
Schema Version
Fingerprint Version
Dense Ordinal
Physical Address
```

One BLAKE3 implementation may support several of these roles.

The roles themselves must remain distinct.

---

# 8. Canonical Encoding Before Fingerprinting

Fingerprints must be based on canonical product meaning.

They must not depend on incidental physical representation.

```text
Logical Product Material
    ↓
Canonical Encoding
    ↓
BLAKE3
    ↓
Fingerprint
```

The encoding must exclude incidental values such as:

```text
memory address
worker completion order
temporary ordinal
table insertion order
host object identity
```

Source provenance is included only when the owning product equality requires it.

---

# 9. Fingerprint Domain Separation

Each product family should have an explicit fingerprint domain.

Example:

```text
KONTRAKT / ANALYSIS / CALL-SUMMARY / V1
KONTRAKT / EXECUTION-IR / V1
KONTRAKT / WHOLE-MACHINE-SUMMARY / V1
```

The exact byte format remains implementation detail.

The product contract owns which semantic fields participate.

---

# 10. Fingerprint Versioning

Fingerprinting and canonical encoding may evolve.

Keep these separate:

```text
Contract Version
IDL Language Version
Compiler Product Schema Version
Canonical Encoding Version
Fingerprint Version
```

A compiler upgrade should not silently treat a changed fingerprint algorithm as changed Contract meaning.

V1 should leave an explicit version field at persistent or reusable product boundaries.

---

# 11. BLAKE3 Role

BLAKE3 is the current fingerprint implementation.

It is a strong V1 choice.

Its architectural role is:

```text
deterministic fingerprint
fast equality evidence
parallel hashing primitive
Merkle node hashing primitive
```

BLAKE3 itself is not:

```text
Contract semantic identity law
compiler product validity law
dependency graph
incremental repair algorithm
```

---

# 12. Application-Level Merkle Structure

BLAKE3 has an internal tree hash.

That tree is not the Kontrakt semantic change graph.

Kontrakt may later build an application-level Merkle tree or DAG.

Example:

```text
Whole Machine
├── Core A Summary
├── Core B Summary
└── Core C Summary
```

Each semantic or compiler product node owns its canonical fingerprint.

Parent fingerprints may include child fingerprints.

Shared products may form a DAG instead of a tree.

---

# 13. V1 Merkle Requirement

V1 does not need a persistent compiler-wide Merkle DAG.

V1 should only preserve the seam required to add one later.

Each reusable product should expose enough information for V2 to determine:

```text
stable product identity
input product identities
content fingerprint
schema / fingerprint version
```

This is sufficient for later hierarchical change localization.

---

# 14. Frozen Publication Contract

Frozen publication is an implementation of a compiler publication contract.

The contract is:

```text
complete before publication
immutable to ordinary consumers
deterministic for the same valid inputs
no partial state visible
validity explicitly known
```

The implementation may use:

```text
frozen tables
primitive slabs
sealed indexes
immutable arrays
FFM-backed regions
```

No physical form becomes the semantic contract.

---

# 15. Reuse Contract

Reuse is allowed only when the owning product remains valid.

The reuse contract must answer:

```text
what key locates a candidate result?
what proves that the candidate is valid?
what equality is required?
what generation or input identities does it depend on?
what invalidates it?
```

A cache hit alone is not proof of validity.

---

# 16. L1 / L2 Direction

The existing L1 / L2 work is currently planning-specific.

Do not turn it into one universal compiler cache without a product contract.

Reuse the tiering principle.

```text
L1
    narrow and hot reuse

L2
    wider bounded reuse
```

Each compiler domain owns its own:

```text
key meaning
equality rule
validity rule
lifetime
retention policy
physical representation
```

Possible future domains include:

```text
frontend products
analysis results
verification summaries
optimizer results
Whole-Machine summaries
JVM backend products
```

---

# 17. V1 Early Cutoff

V1 should support local product-level early cutoff.

```text
Input changed
    ↓
Product recomputed
    ↓
Result equal to previous result
    ↓
reuse previous Frozen result
    ↓
stop local downstream propagation
```

This is not yet a compiler-wide incremental propagation engine.

It is a product-boundary optimization.

---

# 18. Unchanged Transform Cutoff

Transforms should explicitly report whether they changed the logical result.

```text
Transform
    ↓
UNCHANGED
    ↓
reuse current IR generation
preserve valid analyses
```

A transform that changes no logical material should not force a new generation only because the pass executed.

The exact mutation strategy remains open.

---

# 19. Summary-Level Cutoff

Whole-Machine work should use summary equality where possible.

```text
Core Body changed
    ↓
Core Summary recomputed
    ↓
Summary unchanged
    ↓
stop Whole-Machine propagation
```

This is a strong V1 candidate.

It keeps full-body changes from forcing unrelated global work.

---

# 20. Analysis Contract

Each analysis needs its own internal contract.

Define:

```text
Analysis Identity
Input Material
Subject / Scope
Context
Result Meaning
Validity
Preservation Rule
```

Possible scopes include:

```text
method
Operation realization
Core
Whole-Machine
```

The analysis contract must not depend on one algorithm.

---

# 21. Analysis Implementation

V1 may use full recomputation for many analyses.

Example:

```text
CFG
    ↓
full SCC computation
```

V2 may replace it with:

```text
changed graph region
    ↓
incremental SCC repair
```

The analysis result contract remains the same.

---

# 22. Analysis Validity

After a transform, an analysis result has three possible paths.

```text
preserved
    → reuse

incrementally maintained
    → publish updated result

not preserved
    → invalidate
```

V1 may implement only `preserved` and `invalidate` for most analyses.

The `incrementally maintained` path is a V2-compatible seam.

---

# 23. Shared Analysis Reuse

The same valid analysis result should be reusable by several consumers.

```text
Realization Body IR
    ↓
Shared Analysis
    ├── Technology Isolation
    ├── Closure Verification
    └── Optimization
```

The analysis remains compiler-owned.

A consumer must not redefine the result for convenience.

---

# 24. Verification Reuse

Verification should publish reusable derived knowledge where useful.

Examples:

```text
closed target set
stable realization binding
non-escaping value
verified origin relation
verified effect relation
```

This knowledge should be an overlay or summary over the exact realization generation.

It should not require a full duplicate IR.

---

# 25. Optimizer Contract

Each transform should define:

```text
Input IR Contract
Precondition
Legality
Preserved Meaning
Output IR Contract
Invalidated Analyses
Observable Equivalence
```

The transform implementation remains replaceable.

Examples of implementation detail:

```text
pattern matcher
worklist
CFG rewrite
copy-on-write
in-place mutation
cost heuristic
```

---

# 26. Optimization Reuse

Optimization results may be reusable products.

A V1 key may depend on:

```text
Execution IR identity
Static Contract context
Optimization configuration
Target capability
```

The exact key encoding is implementation detail.

If the same valid optimized result already exists, the optimizer may reuse it.

---

# 27. Formation-Time Pruning

Do not generate execution alternatives that established Contract meaning already proves impossible.

```text
Established Contract Context
    ↓
Execution Formation
    ↓
only required executable material
```

This is not general DCE.

It is formation-time specialization.

It reduces later optimizer work.

---

# 28. CFG and DCE

Some elimination requires actual realization control-flow analysis.

```text
CFG
+
fixed Contract context
    ↓
infeasible edge
    ↓
CFG simplification
    ↓
unreachable block removal
```

General DCE remains a transform over explicit analysis.

It may run several times.

The exact pass order remains open.

---

# 29. Whole-Machine Contract

Whole-Machine summary products need an explicit internal product contract.

Define:

```text
summary identity
summary semantic content
summary equality
full-body references
validity
consumer set
```

The summary should support selective body opening.

It must not become Contract authority.

---

# 30. Selective Body Opening

V1 should preserve a summary-first seam.

```text
Whole-Machine Summary
    ↓
global decision
    ↓
required body set
    ↓
open only required bodies
```

This follows the same architectural direction as ThinLTO without copying its implementation.

---

# 31. Product Orchestration Contract

Product orchestration should expose:

```text
Product Identity
Explicit Inputs
Published Result
Dependency Boundary
Validity
```

The current V1 implementation may remain query-oriented.

The product contract must not require recursive pull evaluation.

---

# 32. Scheduler Separation

Keep scheduling separate from product meaning and validity.

```text
Product Contract
    ≠
Dependency Representation
    ≠
Repair Strategy
    ≠
Scheduling Strategy
    ≠
Retention Policy
```

This separation is required for V2.

---

# 33. V1 Query Implementation

The V1 query implementation may provide:

```text
typed product request
explicit input capture
in-memory dependency recording
generation-bound reuse
deterministic publication
```

Do not make query topology Contract semantics.

Do not make query topology the permanent V2 incremental model.

---

# 34. V2 Repair Strategies

V2 may use different repair strategies by domain.

Examples:

```text
result equality cutoff
region recomputation
change-frontier propagation
delta maintenance
dynamic SCC repair
lazy full recomputation
full rebuild
```

One compiler-wide repair algorithm is not required.

---

# 35. Incremental vs Full Recompute

Incremental work is not always cheaper.

V2 should be allowed to compare:

```text
incremental repair cost
        vs
full recompute cost
```

The decision may differ by product family.

Examples:

```text
analysis
Whole-Machine summary
optimization
backend artifact
PBT planning
```

Correctness must be identical.

---

# 36. V1 Metrics for V2

V1 should collect enough metrics to support later cost decisions.

Useful metrics include:

```text
product compute time
result size
cache hit / miss
early cutoff count
analysis invalidation count
recompute count
summary change rate
memory cost
artifact size
```

Metrics may influence future scheduling or profitability.

They must not influence Contract meaning.

---

# 37. Persistent Product Store

A persistent product store is deferred.

V1 should not require one.

V2 may add:

```text
cross-session product reuse
content-addressed storage
persistent dependency metadata
persistent summaries
persistent backend artifacts
```

Persistent material remains derived compiler data.

Deleting it must not change Contract meaning.

---

# 38. Input Key and Output Content Identity

Persistent or reusable products should separate:

```text
Input / Action Key
    = what computation was requested

Output Content Fingerprint
    = what result was produced
```

This allows:

```text
input changed
    ↓
recompute
    ↓
output unchanged
    ↓
early cutoff
```

Do not collapse these roles into one HID meaning.

---

# 39. Clean Recompute Path

V1 must preserve a clean recomputation path.

```text
cache off
reuse off
single generation
```

must remain able to produce the same semantic and product result.

V2 incremental correctness will be checked against this path.

---

# 40. Differential Validation

Future incremental execution should be compared against clean recomputation.

Candidate checks:

```text
clean
    ↔ reused

full recompute
    ↔ incremental repair

single worker
    ↔ parallel

reference backend
    ↔ production backend
```

A reuse bug must not silently become semantic truth.

---

# 41. SOTA Principles to Reuse

The following external systems provide useful principles.

They are references, not architecture templates.

## LLVM CAS

Use:

```text
immutable content-addressed product
input-to-output mapping
reference DAG
```

Do not use CAS identity as Contract semantic identity.

## LLVM ThinLTO

Use:

```text
local summary
combined index
global decision
selective body opening
parallel local work
```

Do not require one merged full IR.

## Bazel Skyframe and Buck2 DICE

Use:

```text
stable key
result equality
change pruning
early cutoff
```

Do not copy one compiler-wide dependency engine.

## Build Systems à la Carte

Use:

```text
scheduler
    ≠
rebuild / validity policy
```

This separation is important for V2.

## rustc and Salsa

Use:

```text
input changed
    ≠ result changed

stable result identity
backdating / early cutoff principle
```

Do not freeze recursive pull-style red-green evaluation as Kontrakt architecture.

## Databricks Enzyme

Use:

```text
normalize before fingerprint
version fingerprint schema
incremental vs full recompute cost choice
clean fallback
```

These are strong V2 design references.

## Materialize / Differential Dataflow / DBSP

Use:

```text
shared derived indexes
domain-specific delta maintenance
incremental graph/data-flow repair
```

Apply only where the product domain benefits.

Do not incrementalize every analysis by default.

---

# 42. Forbidden Coupling

Do not allow these couplings.

```text
hash equality
    → Contract equality

cache hit
    → semantic validity

query edge
    → Contract dependency

Merkle edge
    → Contract dependency

storage identity
    → semantic identity

frozen table layout
    → publication contract

analysis algorithm
    → analysis result meaning

incremental scheduler
    → product semantics

optimizer implementation
    → transform legality

generated artifact
    → Contract authority
```

---

# 43. V1 Implementation TODO

## Product Boundaries

- Define reusable product families.
- Define explicit product identity.
- Define determining inputs.
- Define result equality.
- Define validity.
- Define schema version.

## Fingerprinting

- Define canonical encoding per reusable product family.
- Add domain-separated BLAKE3 fingerprints.
- Separate input key from output content fingerprint.
- Add encoding and fingerprint versions.
- Preserve exact equality validation where required.

## Publication

- Define complete frozen publication boundary.
- Prevent partial material visibility.
- Reuse existing frozen / slab infrastructure where suitable.

## Reuse

- Keep current Planning L1 / L2 behavior.
- Extract only the reusable tiering principle.
- Add domain-specific reuse only after its product contract is defined.
- Record cache and cutoff metrics.

## Early Cutoff

- Add `Changed / Unchanged` result reporting.
- Reuse previous frozen result when equality permits.
- Preserve analyses after unchanged transforms.
- Add summary-level cutoff for Whole-Machine products where practical.

## Analysis

- Define analysis identity, scope, context, and validity.
- Share valid analysis results between verifier and optimizer.
- Define preservation and invalidation rules.
- Leave incremental maintenance as a replaceable seam.

## Optimization

- Define transform contracts before pass implementation.
- Keep formation-time pruning separate from DCE.
- Reuse verified and analysis knowledge.
- Do not make optimization results Contract authority.

## Whole-Machine

- Define summary product contract.
- Define summary equality.
- Preserve selective body-opening seam.
- Avoid one giant Whole-Machine IR.

## QA

- Keep clean recompute path.
- Test cache-on / cache-off equality.
- Test unchanged-result cutoff.
- Test deterministic frozen publication.
- Record invalidation and reuse behavior.

---

# 44. V2 Research TODO

Do not select one V2 architecture yet.

Research and benchmark:

```text
persistent product DAG
application-level Merkle DAG
cross-session CAS
change-frontier propagation
push invalidation
pull validation
hybrid scheduling
delta-maintained analysis
dynamic SCC / reachability repair
summary persistence
lazy body materialization
incremental PBT planning
backend artifact reuse
incremental / full-rebuild switching
```

Each candidate must preserve the V1 compiler-internal contracts.

---

# 45. V2 Evaluation Rule

Evaluate V2 mechanisms by domain.

For each product family, compare:

```text
full recompute cost
incremental repair cost
memory retention cost
dependency tracking cost
change frequency
result stability
parallel scalability
implementation complexity
```

Do not choose incremental execution only because it is incremental.

---

# 46. Completion Criteria for V1

V1 is ready for later incremental evolution when:

```text
major compiler products have explicit contracts

result equality does not depend on physical address

canonical fingerprinting is available where useful

frozen publication is complete and deterministic

reuse never changes semantic result

local early cutoff works at selected product boundaries

analysis validity and invalidation are explicit

Whole-Machine summaries can stop unnecessary global work

clean recomputation remains available

query, reuse, scheduling, and repair remain replaceable
```

---

# 47. Final Direction

The V1 architecture should look like this:

```text
Compiler-Internal Contract
        ↓
Canonical Product Meaning
        ↓
V1 Implementation
    HID / BLAKE3
    Frozen Material
    L1 / L2
    Query-Oriented Evaluation
    Full Recompute
    Local Early Cutoff
        ↓
Published Product
```

V2 may replace the lower implementation layer.

```text
Same Compiler-Internal Contract
        ↓
V2 Implementation
    Persistent Product State
    Merkle Localization
    Change Frontier
    Delta Repair
    Domain-Specific Incremental Algorithms
    Cost-Based Full / Incremental Choice
```

The upper contract remains stable.

That is the required V1-to-V2 compatibility boundary.