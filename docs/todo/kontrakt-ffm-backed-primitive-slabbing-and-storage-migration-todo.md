# Kontrakt FFM-Backed Primitive Slabbing and Target-Aware Physical Layout TODO

## Status

Planning / TODO document.

This document is not an ADR.

It does not define Contract semantics.

It records the current physical-storage direction for the Kontrakt compiler and the architecture seams that must remain
open before the storage model is promoted into accepted architecture.

The current direction is:

```text
semantic material
    ↓
typed semantic references
    ↓
dense physical ordinals / offsets
    ↓
primitive slabbing
    ↓
FFM-backed MemorySegment storage
    ↓
target-aware physical layout
```

Primitive slabbing and FFM are not competing designs.

Primitive slabbing defines how compiler material is organized.

FFM provides the memory substrate used to realize that organization with explicit alignment, offsets, layout, and
lifetime control.

Target-aware physical layout decides how that physical material should be arranged for an admitted target profile.

---

## 1. Position

High-cardinality compiler-owned material should not default to JVM object graphs.

The V1 target is an object-light, primitive-oriented compiler core.

FFM-backed primitive slabs are the current default physical direction for hot and high-cardinality material.

Physical layout may later depend on target hardware characteristics.

This is a realization decision.

It must not become Contract authority.

The same semantic material must preserve the same identity, ordering, judgment, and publication result even if the
physical storage backend or target layout is replaced.

---

## 2. Semantic References and Physical References Must Stay Separate

Kontrakt uses references heavily because the same established material is consumed by multiple compiler subsystems.

That is desirable.

The semantic layer should keep explicit typed references such as:

```text
OperationRef
FactRef
InvariantRef
PolicyRef
BudgetRef
CapacityRef
```

These references identify semantic material.

They are not byte offsets.

Physical lowering may represent them as dense ordinals or offsets.

```text
OperationRef
    ↓ physical lowering
operationOrdinal
    ↓
operationSlabBase + ordinal * stride
```

The physical address is replaceable.

The semantic reference is not defined by that address.

The following identities must remain distinct:

```text
Contract semantic identity
Compiler typed reference
Image-local ordinal
Byte offset
HID / persistent fingerprint
```

An ordinal or byte offset must never become semantic identity merely because it is fast to access.

---

## 3. Default V1 Physical Direction

The target V1 compiler storage shape is:

```text
FFM Arena / Segment ownership
        │
        ├── fixed-stride hot slabs
        ├── variable-length range slabs
        ├── cold side slabs
        ├── canonical byte slabs
        └── index / HID / lookup slabs
```

A hot entity should normally be represented by primitive fields and compact references.

A variable-length relation should normally be represented by an offset and count into a contiguous range slab.

Example:

```text
OperationSlab[41]

factStart      = 120
factCount      = 3
invariantStart = 340
invariantCount = 2
```

with:

```text
FactRefSlab[120..123]
InvariantRefSlab[340..342]
```

This avoids per-entity collection objects.

It also keeps traversal predictable.

The exact field layout remains a storage decision.

---

## 4. FFM Is the Backing Substrate, Not the Semantic Model

FFM should be used to obtain physical control that ordinary heap objects do not provide reliably.

The relevant physical controls include:

```text
base alignment
byte offsets
field layout
stride
padding
large-region allocation
bulk lifetime control
```

The compiler must not expose `MemorySegment`, `Arena`, native addresses, or FFM layout details as semantic vocabulary.

The lawful direction is:

```text
semantic relation
    ↓
physical lowering
    ↓
FFM-backed slab
```

The reverse direction is forbidden:

```text
FFM layout
    ↓
infer semantic meaning
```

---

## 5. Target-Aware Physical Layout

Cache-line layout should not be hard-coded into unrelated compiler subsystems.

A dedicated Physical Layout boundary should own target-dependent placement decisions.

The intended seam is:

```text
compiler analysis
    ↓
derived Access Profile
        +
explicit Target Hardware Profile
        ↓
Physical Layout Planning
        ↓
derived Physical Layout Plan
        ↓
FFM-backed slab realization
```

The Physical Layout Plan is derived material.

It is replaceable.

It is non-authoritative.

Changing a target profile may change the physical layout.

It must not change Contract meaning.

---

## 6. Access Profile

A subsystem should not directly request a 64-byte cache line or a specific field offset.

It should publish the access information needed by the physical planner.

The exact schema is not fixed yet.

The material may include information such as:

```text
fields usually read together
sequentially scanned ranges
cold fields
read-mostly material
worker-local writes
shared writes
expected working-set shape
```

This is compiler-derived access information.

It is not a Contract declaration.

The access profile may be recomputed when analysis or implementation changes.

---

## 7. Target Hardware Profile

The compiler host hardware must not be assumed to be the execution target.

The architecture must distinguish:

```text
Compiler Host
    runs Kontrakt

Target Hardware Profile
    describes the intended execution environment
```

Local compilation may derive a profile from the host when that is explicitly the target.

Cross compilation must use an explicit target profile.

Reproducible builds should be able to pin a normalized profile.

A target profile may eventually describe:

```text
cache-line size
cache hierarchy
page size
vector alignment
NUMA characteristics
other backend-relevant physical properties
```

V1 does not need to model all of these.

A small deterministic profile is sufficient at first.

The profile should have a stable physical identity or fingerprint so that target-dependent products can participate in
cache validation.

---

## 8. Physical Layout Plan

The Physical Layout Plan combines storage requirements with the target profile.

It may decide:

```text
alignment
stride
field offsets
hot/cold partitioning
AoS / SoA / AoSoA representation
cache-line packing
false-sharing separation
range placement
padding
```

The plan must remain separate from semantic material.

A verifier, diagnostic subsystem, or test subsystem may consume the same semantic authority while receiving different
derived physical projections.

That is allowed.

The semantic source stays the same.

---

## 9. Cache-Line Locality

Cache-line placement is a physical optimization concern.

It is not Contract law.

The compiler should keep values that are normally read together physically close when measurement supports that choice.

Cold material should not occupy hot cache lines merely because it belongs to the same semantic entity.

A typical split may be:

```text
Hot Operation Slab
    fact range
    invariant range
    compact flags
    frequently-read refs

Cold Side Slab
    source locations
    human-readable names
    rich diagnostic metadata
```

Exact cache-line size must remain target dependent.

A 64-byte line may be a common profile value.

It must not become a semantic invariant.

Alignment, stride, and padding are separate decisions.

A 64-byte aligned slab does not imply that each entity should consume 64 bytes.

Read-mostly tables may pack several entries into one line.

Write-heavy worker-local state may require separation to avoid false sharing.

The physical choice must follow measured access patterns.

---

## 10. Reference Depth and Derived Hot Views

A normalized semantic store may contain many explicit relations.

That does not mean hot consumers should repeatedly chase the full reference graph.

This shape is acceptable as semantic structure:

```text
Operation
    → applicable Contract material
    → related authority material
```

This shape is undesirable as a repeated hot physical path:

```text
ARef
    → BRef
        → CRef
            → DRef
                → target
```

A subsystem may materialize a direct derived view when the relation is already established.

Example:

```text
Established Contract World
        ↓
VerifierProjection
        ↓
direct operation-local ranges and refs
```

The projection must remain:

```text
derived
recomputable
non-authoritative
```

Flattening may remove physical indirection.

It must not create new semantic meaning.

---

## 11. Fragmentation and Allocation Policy

FFM must not be used as a general per-object native allocator.

Many independent small native allocations would recreate allocator fragmentation and lifecycle complexity outside the
JVM heap.

The target model is region/slab allocation.

```text
large aligned MemorySegment
        ↓
compiler-owned suballocation
        ↓
fixed or append-only slabs
```

V1 should prefer bump-style allocation inside an owning region where practical.

Individual entities should not normally be freed.

The owning region should be reclaimed as a unit.

This minimizes external fragmentation.

Internal fragmentation still exists.

Padding, alignment, and fixed stride can waste memory.

That cost must be measured instead of hidden.

The compiler should not use one-cache-line-per-entity as a general rule.

---

## 12. Lifetime Classes

Storage lifetime must follow compiler ownership.

At minimum, the design should distinguish:

```text
persistent / frozen material
compilation-generation material
worker-local scratch material
```

The exact implementation is still open.

The intended V1 direction is:

```text
Persistent / Frozen Region
    long-lived reusable material

Generation Region
    compilation-owned material
    reclaimed at generation end

Worker Region
    temporary scratch
    reset or closed with worker/task lifetime
```

A long-lived build daemon must not retain generation-local or worker-local slabs after their owner is finished.

A segment slice must not accidentally keep a much larger obsolete region alive without an explicit ownership reason.

---

## 13. V1 Reclamation

V1 should prefer coarse lifetime reclamation.

The ordinary pattern is:

```text
allocate region
    ↓
populate slabs
    ↓
seal / use
    ↓
close or reset entire owning region
```

This is simpler than general free-list allocation.

It also fits compiler phase and generation lifetimes.

Published frozen material may require a longer lifetime.

Its ownership must be explicit.

Reclamation must not depend on semantic object reachability inferred from the JVM heap.

---

## 14. V2 Generation Model

V2 incremental compilation changes the lifetime problem.

Not every compilation product can be discarded at the end of one invocation.

The likely direction is immutable generation material:

```text
Generation N frozen segments
        +
Generation N+1 new segments
        ↓
reachable generation retention
        ↓
old generation reclamation
```

V2 should avoid patching arbitrary old slabs in place.

Persistent query results and immutable compiler products fit better with generation-oriented publication.

The exact reclamation model remains open.

Possible mechanisms may include epoch retirement, generation reference tracking, or another deterministic ownership
protocol.

This TODO does not select one yet.

---

## 15. Host JDK and Target JVM Must Remain Separate

If FFM is the V1 compiler storage default, the Kontrakt compiler host JDK must support the selected finalized FFM API.

That does not by itself require generated applications to use the same JVM baseline.

The architecture should preserve:

```text
Compiler Host JDK
    runs Kontrakt compiler
    may use FFM internally

Target JVM
    runs generated application
    follows backend target profile
```

A Java 17 target remains possible if FFM does not leak into the generated runtime ABI or required runtime support
library.

If a future generated runtime deliberately uses FFM, that runtime profile must declare its own minimum target JVM.

Compiler storage policy and generated runtime policy must not be conflated.

---

## 16. Persistent Disk Reuse Is a Separate Concern

In-process FFM layout and on-disk persistence are related but not identical problems.

A slab being efficient in memory does not automatically make its raw bytes a stable persistent format.

Persistent products require explicit versioning, fingerprints, compatibility rules, and corruption handling.

The V1 compiler may reuse selected frozen products from disk.

The persistent cache must remain derived and recomputable.

Deleting the cache may make a build slower.

It must not change Contract meaning.

A future memory-mapped slab format is possible.

It should be introduced only after the persistent format is independently defined.

Target-dependent products must include the relevant target profile in reuse validation.

---

## 17. Material Families to Evaluate First

The first migration candidates should be high-cardinality material with repeated reads and stable ownership.

Likely candidates include:

```text
Established Contract World tables
resolved relation tables
HID and identity indexes
Fact / Invariant reference ranges
verification summaries
call / effect / origin summaries
test-synthesis plans
backend lowering tables
```

Cold sparse configuration does not need to be forced into slabs merely for consistency.

The storage architecture should optimize the dominant access pattern.

It should not maximize the percentage of code using FFM.

---

## 18. Benchmark Requirement

The storage direction must be validated with measured compiler workloads.

At least three physical shapes should be compared where practical:

```text
object graph
heap primitive slab
FFM-backed primitive slab
```

The purpose is not to prove that FFM is universally faster.

The purpose is to determine where explicit layout and lifetime control produce a material gain.

Measurements should cover:

```text
allocation and GC pressure
resident memory
hot-loop throughput
cache-miss behavior
pointer-chasing sensitivity
alignment and false-sharing effects
reclamation and daemon retention
```

Target-aware layout experiments should also compare different legal physical plans for the same semantic material.

A physical optimization must not become an architectural requirement only because it wins one microbenchmark.

---

## 19. Required Correctness Tests

Every physical backend or layout migration must preserve semantic equivalence.

At minimum, test:

```text
same canonical bytes
same HID derivation
same semantic equality
same stable ordering
same Contract judgments
same diagnostic identity
same generated product meaning
```

Layout-specific tests should additionally verify:

```text
bounds-safe offset arithmetic
alignment assumptions
segment lifetime safety
no use-after-close
no cross-generation contamination
no daemon retention after teardown
```

Different Target Hardware Profiles may produce different physical layouts.

They must preserve the same Contract meaning.

Performance evidence does not replace equivalence tests.

---

## 20. Existing Documents That Need Later Migration

The current repository contains older physical assumptions that may no longer match the intended V1 direction.

These should not be silently rewritten while the storage model is still under review.

They should be migrated together once this TODO is ready to become accepted storage architecture.

### ADR-0041

Current material should be reviewed if it treats heap primitive arrays as the V1 baseline and aligned `MemorySegment`
storage as an optional later backend.

The semantic identity law should remain unchanged.

Only the physical baseline and backend guidance require review.

### ADR-0042

Current material should be reviewed if it places `MemorySegment` and related physical acceleration mainly on a later
track.

The separation between semantic law and physical backend remains valid.

Existing lifecycle material should be reused rather than duplicated.

### Release Readiness TODO

Any deferred post-V1 item that treats off-heap identity tables or full value-slab migration as inherently post-V1 must
be reviewed if FFM-backed slabbing becomes the accepted V1 baseline.

Daemon hygiene and slab reclamation work remain relevant.

---

## 21. Migration Target

This TODO should eventually be split into two layers.

```text
Architecture / ADR
    semantic-vs-physical boundary
    primitive-slabbing default
    target-aware layout boundary
    storage ownership
    lifetime classes
    physical equivalence requirements
    V1 / V2 extension seam

Design document
    exact MemoryLayout
    exact slab families
    exact field offsets
    stride
    padding
    allocator implementation
    target-profile schema
    planner heuristics
    benchmark-selected AoS / SoA / AoSoA choices
```

Exact Java classes and field offsets should remain outside the ADR unless a future compatibility requirement makes them
externally stable.

---

## 22. Open Decisions

The following items remain open and should be closed with implementation evidence:

```text
compiler host JDK baseline
exact Arena ownership strategy
fixed-size versus growable segment policy
Access Profile schema
Target Hardware Profile schema
Physical Layout Plan schema
hot slab AoS / SoA / AoSoA selection
cache hierarchy depth modeled in V1
NUMA support timing
profile-guided layout timing
persistent frozen-image layout
V2 immutable generation reclamation
memory-mapped reuse policy
fallback behavior when FFM allocation is unavailable
```

These are physical design questions.

They must not be answered by changing Contract semantics.

---

## 23. V1 Boundary

V1 does not need a sophisticated hardware optimizer.

The minimum useful architecture is:

```text
derived Access Profile
        +
small deterministic Target Hardware Profile
        ↓
simple Physical Layout Planner
        ↓
FFM-backed slab plan
```

The important requirement is the seam.

V1 may use simple rules.

Later versions may add richer hardware profiles, cost models, measured access information, or profile-guided layout
without changing the Contract model.

The planner must remain replaceable.

---

## 24. Exit Criteria

The storage migration is ready to leave TODO status when:

```text
FFM-backed primitive slabbing has a stable ownership model
high-cardinality material families have explicit slab layouts
Access Profile ownership is defined
Target Hardware Profile ownership is defined
Physical Layout Plan is explicitly non-authoritative
hot/cold separation is measured
fragmentation and reclamation behavior are documented
daemon retention tests pass
semantic-equivalence tests pass
representative compiler benchmarks exist
host-JDK and target-JVM boundaries are documented
V2 generation extension does not require a storage rewrite
```

At that point, the accepted portions should move into the appropriate ADR and design documents.

Until then, this file remains the working physical-storage and target-layout plan.