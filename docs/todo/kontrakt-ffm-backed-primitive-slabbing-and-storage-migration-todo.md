# Kontrakt FFM-Backed Primitive Slabbing and Physical Storage Migration TODO

## Status

Planning / TODO document.

This document is not an ADR.

It does not define Contract semantics.

It records the current physical-storage direction for the Kontrakt compiler and the migration work that must be reviewed
before the storage model is promoted into accepted architecture.

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
```

Primitive slabbing and FFM are not competing designs.

Primitive slabbing defines the physical data organization.

FFM provides the memory substrate used to realize that organization with explicit alignment, offsets, lifetime, and
layout control.

---

## 1. Position

High-cardinality compiler-owned material should not default to JVM object graphs.

The V1 target is an object-light, primitive-oriented compiler core.

FFM-backed primitive slabs are the current default physical direction for hot and high-cardinality material.

This is a realization decision.

It must not become Contract authority.

The same semantic material must preserve the same identity, ordering, judgment, and publication result even if the
physical storage backend is replaced.

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

Physical lowering may represent them as dense ordinals or offsets:

```text
OperationRef
    ↓ physical lowering
operationOrdinal: int
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

An image-local ordinal or byte offset must never become semantic identity merely because it is fast to access.

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

Relevant capabilities include:

```text
explicit base alignment
explicit byte offsets
explicit field layout
explicit stride
explicit padding
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

## 5. Cache-Line Locality

Cache-line placement is a physical optimization concern.

It is not Contract law.

The compiler should control the layout of hot slabs so that values normally read together are physically close.

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

Exact cache-line size must remain target/profile dependent.

A 64-byte line may be the common target on an admitted platform profile, but it must not be hard-coded as a semantic
invariant.

Alignment, stride, and padding are separate decisions.

A 64-byte aligned slab does not imply that each entity should consume 64 bytes.

Read-mostly tables may pack several entries into one line.

Write-heavy worker-local state may require separation to avoid false sharing.

The physical choice must follow measured access patterns.

---

## 6. Reference Depth and Derived Hot Views

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

## 7. Fragmentation and Allocation Policy

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

## 8. Lifetime Classes

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

## 9. V1 Reclamation

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

## 10. V2 Generation Model

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

Possible mechanisms include epoch retirement, generation reference tracking, or another deterministic ownership
protocol.

This TODO does not select one yet.

---

## 11. Host JDK and Target JVM Must Remain Separate

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

## 12. Persistent Disk Reuse Is a Separate Concern

In-process FFM layout and on-disk persistence are related but not identical problems.

A slab being efficient in memory does not automatically make its raw bytes a stable persistent format.

Persistent products require explicit versioning, fingerprints, compatibility rules, and corruption handling.

The V1 compiler may reuse selected frozen products from disk.

The persistent cache must remain derived and recomputable.

Deleting the cache may make a build slower.

It must not change Contract meaning.

A future memory-mapped slab format is possible.

It should be introduced only after the persistent format is independently defined.

---

## 13. Material Families to Evaluate First

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

The storage architecture should optimize the dominant access pattern, not maximize the percentage of code using FFM.

---

## 14. Benchmark Requirement

The storage direction must be validated with measured compiler workloads.

At least three shapes should be compared where practical:

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
resident memory / footprint
hot-loop throughput
cache-miss behavior
pointer-chasing sensitivity
alignment and false-sharing effects
reclamation / daemon retention
```

Benchmarks should use both representative projects and synthetic high-cardinality fixtures.

A physical optimization must not be promoted to an architectural requirement only because it wins one microbenchmark.

---

## 15. Required Correctness Tests

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

Performance evidence does not replace equivalence tests.

---

## 16. Existing Documents That Need Later Migration

The current repository contains older physical assumptions that no longer match the intended V1 direction.

These should not be silently rewritten while the storage model is still under review.

They should be migrated together once this TODO is ready to become accepted storage architecture.

### ADR-0041

Current text treats heap primitive arrays as the portable V1 baseline and explicitly aligned `MemorySegment` / off-heap
storage as an optional high-performance backend.

The new direction makes FFM-backed primitive slabbing the intended V1 default for relevant compiler-owned material.

The semantic identity law in ADR-0041 should remain unchanged.

Only the physical baseline and backend guidance require review.

### ADR-0042

Current text allows the ordinary V1 backend to be heap primitive arrays and places `MemorySegment` and related physical
acceleration mainly on the later track.

That physical timeline must be reviewed.

The separation between semantic law and physical backend remains valid.

The lifecycle material in ADR-0042 should be reused rather than duplicated.

### Release Readiness TODO

The current deferred post-V1 list includes:

```text
Advanced off-heap / direct-memory identity tables
Full value-slab migration
```

Those entries conflict with the current V1 direction if FFM-backed slabbing becomes the default compiler substrate.

They must be reclassified when the V1 storage architecture is accepted.

The existing daemon-hygiene and slab-reclamation work remains relevant.

---

## 17. Migration Target

This TODO should eventually be split into two layers.

```text
Architecture / ADR
    semantic-vs-physical boundary
    primitive-slabbing default
    storage ownership
    lifecycle classes
    physical equivalence requirements
    V1 / V2 extension seam

Design document
    exact MemoryLayout
    exact slab families
    field offsets
    stride
    padding
    allocator implementation
    benchmark-selected AoS / SoA choices
```

Exact Java classes and field offsets should remain outside the ADR unless a future compatibility requirement makes them
externally stable.

---

## 18. Open Decisions

The following items remain open and should be closed with implementation evidence:

```text
compiler host JDK baseline
exact Arena ownership strategy
fixed-size versus growable segment policy
hot slab AoS / SoA / AoSoA selection
cache-line profile discovery
persistent frozen-image layout
V2 immutable generation reclamation
memory-mapped reuse policy
fallback behavior when FFM allocation is unavailable
```

These are physical design questions.

They must not be answered by changing Contract semantics.

---

## 19. V1 Exit Criteria

The storage migration is ready to leave TODO status when:

```text
FFM-backed primitive slabbing has a stable ownership model
high-cardinality material families have explicit slab layouts
hot/cold separation is measured
fragmentation and reclamation behavior are documented
Gradle/Maven daemon retention tests pass
semantic-equivalence tests pass
representative compiler benchmarks exist
host-JDK and target-JVM boundaries are documented
V2 generation extension does not require a storage rewrite
```

At that point, the accepted portions should move into the appropriate ADR and design documents.

Until then, this file remains the working storage plan.