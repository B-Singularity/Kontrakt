# ADR-0070: Realization Axis, Core Realization Closure, and JVM-Ahead Optimization

## Status

Proposed

## Date

2026-09-02

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`
- `docs/todo/v2/kontrakt-v2-reference-architecture-and-v1-foundations.md`
- ADR-0069: Invariant Contract
- ADR-0068: Fact Contract
- ADR-0067: Lowering Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0055: Whole-Machine Pipeline Composition and Contract Concurrency
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Generated Host Interface Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror
- ADR-0044: Unified Runtime Memory Envelope and Pipeline Lifecycle Governance
- ADR-0042: Mechanical Sympathy, Primitive Lifecycle, and Async Ownership Governance
- ADR-0041: Stable Metadata Identity, Digest/HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure
- ADR-0031: Two-Tier Transactional Memoization and Structural Interning
- `docs/design/stable-metadata-identity-protocol.md`
- `docs/design/protocol-owned-metadata-interning.md`
- `docs/design/l1-planner-session-primitive-data-structures.md`
- `docs/design/l2-plan-interner-partitioned-tier2-with-governance.md`

---

## 1. Context

Kontrakt separates Contract from realization.

The Contract axis owns meaning and judgment. The State-Machine axis owns legal movement. The Realization axis performs
the work that makes those decisions executable.

Realization is not a Contract.

Earlier ADRs already allow realization machinery to be replaced, fused, specialized, or optimized when the declared
meaning is preserved. The newer one-dimensional Contract ADRs make this freedom more important because the compiler now
knows much more before execution begins.

Lowering provides the final inbound relation before core entry. External presentation authority ends there, and
successful input Fact establishment gives the core explicit factual material. The user Operation then receives ordinary
host values that realize those established Facts.

This boundary has meaning only if the Operation does not silently reopen the outside world.

A helper may be ordinary realization. A repository read from that helper is different. It introduces information that
did not pass through the declared boundary and therefore bypasses the factual surface that the core claims to own.

Kontrakt must inspect user realization deeply enough to detect such bypasses before execution.

That inspection also gives the compiler valuable knowledge about the program. Kontrakt already knows the Contract world
and the exact factual surfaces around the Operation. Once it also knows the relevant realization structure, it should
use that information to produce a simpler JVM-facing program instead of preserving unnecessary authored object topology.

---

## 2. Problem

An Operation boundary alone does not close the Contract Core.

Consider a core Operation whose input Fact contains all declared pricing information.

```text
PriceFact
    ↓
Operation
    ↓
Result
```

The implementation may still reach outside the core.

```text
PriceFact
    ↓
Operation
    ↓
helper
    ↓
GlobalPriceConfiguration.current
    ↓
Result
```

The helper itself is not the violation. The undeclared outside information is.

The actual result now depends on factual material that did not pass through Adapter formation, Input, Admission,
Canonicalization, Lowering, and Fact establishment. The declared Fact no longer closes the factual basis of the
Operation.

The same problem exists when core code reaches into a database, framework proxy, live clock, environment state,
callback, mutable singleton, or another outside capability.

Those mechanisms may exist outside the core. They may not silently regain influence from inside it.

Kontrakt therefore needs a compile-time realization model strong enough to verify that the core remains closed.

The cost of this verification can be high. The compiler may need the same type, member, call, effect, or
recursive-region knowledge for many Operations and many downstream products. Repeating that work would make richer
Contract semantics produce avoidable compile-time cost.

Preserving every object allocation, reference chain, dispatch boundary, and temporary carrier after performing this
analysis would waste the same knowledge a second time.

The realization architecture must therefore protect core closure and make the resulting information reusable for
optimization.

---

## 3. Decision Drivers

Contract authority and realization must remain separate.

External technology must terminate before the core. Adapter work forms external material into a boundary presentation;
it does not survive as core capability.

Fact is the explicit factual authority inside the Contract Core. Transient implementation values may exist, but they do
not introduce independent factual sources.

Kontrakt must reject a user realization when outside material can influence core computation through an undeclared path.

A realization whose required closure cannot be established under the supported compiler model must not be silently
treated as safe.

Realization topology must remain invisible to Contract authority and outward Publication.

Compiler products must share authoritative semantic material and reusable realization knowledge instead of
reconstructing either independently.

The richer analysis must not make compilation depend on object-heavy, pointer-heavy, repeatedly rebuilt compiler
structures when a denser deterministic representation is available.

Optimization may change physical execution but must preserve the declared Contract result and required diagnostic
attribution.

V1 must provide the identity, publication, analysis, storage, and computation seams required for V2 incremental
compilation.

---

## 4. Decision

Kontrakt will treat Realization as a separate non-authoritative axis with two distinct domains.

```text
Realization Axis
    ├── User-System Realization
    │       the implementation supplied for the Operation
    │
    └── Kontrakt Realization
            compiler analysis, optimization, generated machinery,
            backend lowering, and runtime support
```

The two domains interact but do not share authority.

User-System Realization is checked against the already-declared machine. It does not create Contract meaning.

Kontrakt Realization analyzes and transforms executable material. Its compiler graphs, caches, summaries, tables, IRs,
generated methods, and storage layouts remain replaceable implementation.

The overall direction is:

```text
External World
    ↓
Adapter
    ↓
Input / Admission / Canonicalization / Lowering
    ↓
Established Core Facts
    ↓
User-System Realization
    ↓
result-side Contract processing
```

Kontrakt separately performs:

```text
Declared Contract World
        +
User-System Realization
        ↓
compile-time realization analysis
        ↓
core-closure verification
        ↓
meaning-preserving optimization
        ↓
JVM-facing realization
```

The second pipeline realizes the first. It does not become another Contract pipeline.

---

## 5. User-System Realization

User-System Realization is the implementation supplied for a declared Operation.

It may use ordinary programming structure. Private functions, loops, local temporaries, and internal algorithms remain
realization details when their information is derived from material already available inside the core.

The Operation is not required to manipulate a special runtime `Fact` object. Fact authority is semantic rather than
carrier authority.

The important question is where the information that affects the Operation comes from.

```text
established Fact material
    ↓
local calculation
    ↓
helper
    ↓
Operation result
```

may remain legal.

```text
established Fact material
    +
independently obtained outside information
    ↓
Operation result
```

is not a closed core realization.

User source shape also does not determine the final runtime topology. A class, helper object, or temporary carrier may
disappear after proven equivalent optimization.

---

## 6. Adapter and Core Realization Boundary

External technology ends before the Contract Core.

A database, network client, operating-system facility, framework context, live clock, or similar mechanism belongs
outside core realization. Its information may enter only after the appropriate Adapter and boundary formation have
removed the external dependency itself.

```text
external system
    ↓
Adapter
    ↓
boundary presentation
    ↓
Contract processing
    ↓
Fact
```

The core may use the established information.

It does not keep the database connection, repository, lazy proxy, service capability, or another live external mechanism
that produced that information.

Moving such a mechanism behind a helper does not change the boundary.

This ADR does not redefine Adapter or Input semantics. It only requires User-System Realization to preserve the boundary
already established by those owners.

---

## 7. Core Realization Closure

A user Operation realization must remain closed over the factual material and machine context lawfully available at that
point in the pipeline.

This closure protects Fact and Core meaning. It is not a new Contract kind.

Transient realization values may be created from established material. They remain implementation values and receive no
Fact authority merely because later computation uses them.

A realization violates closure when an outside source can influence a contract-visible result or movement without first
passing through its owning boundary.

The compiler therefore verifies the origin of relevant information rather than merely checking the first call made by
the Operation.

A local helper may be valid because its entire relevant dependency stays inside the core. The same helper becomes
invalid when it reaches a live external source.

Contract judgments and State-Machine movement retain their own authority. Generated checks that execute near the
Operation do not turn those authorities into user implementation dependencies.

---

## 8. Compile-Time Realization Verification

Kontrakt must inspect enough User-System Realization to establish Core Realization Closure before emitting a legal
executable realization.

The analysis follows implementation dependencies only as far as required to decide that question and to support later
compiler work.

A direct helper call may require the helper body to be analyzed. A deeper call cannot be treated as safe merely because
its caller is local.

When Kontrakt establishes that outside material influences core computation, compilation fails.

When the supported V1 analysis cannot establish the required closure, the compiler must not silently assume that the
implementation is closed.

The exact V1 proof model and the exact treatment of constructs that defeat static analysis remain open in this ADR.

Realization verification is a compile-time responsibility. Runtime execution should not depend on a generic monitor that
discovers ordinary core-closure violations after the program has already been accepted.

Runtime Contract judgments that inherently depend on runtime values remain runtime judgments. This ADR does not move
those judgments into compile time.

---

## 9. Realization Graphs and Cycles

Implementation structure may contain cycles even though Contract semantic cycles are forbidden.

A recursive method or cyclic class relation is realization topology. It does not create Contract composition.

Kontrakt must detect such topology so analysis terminates and repeated traversal is avoided.

Earlier stable type identity, cycle keys, bounded traversal, and recursive-region techniques may be reused as
realization-analysis machinery. Their previous semantic assumptions do not automatically carry forward.

```text
Contract semantic cycle
    !=
Realization analysis cycle
```

A Contract semantic cycle remains governed by the Contract laws that forbid it.

A realization cycle may be legal when the whole relevant region remains within the permitted core dependency closure.

The exact recursive-region summary algorithm remains open.

---

## 10. Contract Material and Realization Knowledge

Authoritative Contract material and realization-analysis knowledge must remain separate.

Established Material keeps the authority of the Contract that established it. Fact remains factual Contract material
even when the compiler stores it through a dense table or compact identifier.

A compiler summary saying that a method reads certain values is different. It describes implementation behavior and has
no Contract authority.

The same distinction applies to call graphs, effect summaries, escape information, alias knowledge, class relationships,
and optimization facts.

These materials may be referenced together by compiler analyses, but one must not silently become the identity or
meaning of the other.

```text
Contract meaning
    !=
realization-analysis knowledge
    !=
physical compiler storage
```

Source location and authored topology also remain separate from emitted runtime topology. A safe optimization may
replace the latter without rewriting Contract meaning.

---

## 11. Shared Acquisition and Analysis

Realization verification makes repeated host-program knowledge expensive enough that acquisition and analysis reuse
become architectural requirements.

The compiler should not repeatedly resolve the same class, method, type relation, or recursive region for each Contract
consumer.

Likewise, downstream products should not independently reconstruct Contract meaning from source evidence after that
meaning has already been established.

The preferred direction is:

```text
source / host evidence
    ↓
acquire once
    ↓
stable compiler knowledge
    ↓
shared analyses
    ↓
verification / diagnostics / optimization / backend
```

Reuse does not mean every computation must physically execute once. Independent verification may intentionally recompute
a result when independence is part of the check.

The architecture forbids accidental reconstruction, not deliberate validation.

V1 must expose explicit analysis inputs and results so later consumers can share them without hidden mutable global
state.

The exact Analysis Manager and query API are open for a later compiler-architecture decision.

---

## 12. Frozen and Published Realization Material

Earlier frozen acquisition work remains useful, but `Frozen` does not become another semantic Contract level.

A frozen or published representation is one physical form of compiler knowledge.

The same principle applies to both Contract-side compiler material and User-System Realization knowledge. Their semantic
ownership remains different even if they use common storage machinery.

The V1 direction is to preserve the earlier deterministic publication discipline:

```text
private candidate material
    ↓
complete sizing and placement planning
    ↓
direct materialization
    ↓
verification
    ↓
seal
    ↓
publish
```

Published material must be complete before consumers observe it.

Expensive host acquisition should be paid once when a stable published representation can support later work through
compact references.

Dense tables, primitive slabs, vertical partitioning, and generation-local ordinals remain valid realization techniques
when they improve locality or reduce allocation. None of them becomes semantic identity.

Hot material may be physically separated from cold provenance or diagnostic detail. The split must preserve every
required relation.

The exact published-generation schema remains open.

---

## 13. Identity, Interning, and Cache Reuse

Realization reuse requires stable identity discipline.

Earlier canonical-byte, digest/HID, exact collision verification, and protocol-owned interning techniques remain
available as compiler substrate where their current identity law is still valid.

A HID is an accelerator. It is not semantic equality by itself.

A local table index is faster still, but it is only a physical address inside one published generation.

The architecture therefore keeps these concerns separate:

```text
semantic identity
    !=
realization-analysis identity
    !=
fingerprint
    !=
local ordinal
    !=
cache location
```

Cache state has no authority. A cold compiler and a warm compiler must accept and reject the same program under the same
declared inputs.

Worker-local memoization and bounded shared interning may reduce repeated work. V2 may later persist suitable results
across compiler runs.

Old cache structures are not automatically preserved as the new semantic model. Their reusable laws are carried forward;
obsolete graph semantics are not.

---

## 14. Kontrakt Realization and Backend Responsibility

Kontrakt Realization owns the compiler and runtime machinery that turns the declared machine into executable form.

This includes realization analysis, generated orchestration, optimization, backend lowering, and emitted runtime
support.

These responsibilities may use multiple compiler representations. None of those representations becomes Contract
authority.

The backend must consume already-established semantic meaning. It must not rediscover authority from source classes,
generated object shape, cache state, or planning topology.

The target architecture may therefore distinguish semantic Contract material, User-System Realization knowledge, an
optimized machine representation, and JVM-specific IR.

The exact IR boundaries and names remain open.

What is fixed is the direction of authority:

```text
Contract meaning
    ↓
constrains realization

Realization topology
    ✕
does not define Contract meaning
```

---

## 15. Optimization Authority

A legal User-System Realization becomes optimization material after its required closure has been established.

Kontrakt may change physical execution when it can preserve the declared meaning and required observability.

This freedom exists because authored implementation topology is not Contract topology.

For example, a temporary object that has no observable identity may be removed. A Fact that is semantically required
does not necessarily require a physical Fact object when the same established values can flow directly into their
consumers.

The logical pipeline remains intact even when physical work is fused or discharged.

```text
logical judgment preserved
    !=
mandatory runtime object or call boundary
```

The optimization architecture should exploit information that is specific to Kontrakt and therefore difficult for a
general JVM optimizer to recover. Contract applicability, Fact dependencies, closed Policy Worlds, known State
relations, and Publication boundaries are examples of such information.

Legality must be decided before profitability. A cost model may decline a legal transform, but it may not legalize an
invalid one.

The exact V1 transform catalog and profitability model remain open.

---

## 16. JVM-Ahead Optimization

Kontrakt should hand the JVM a realization that has already used the high-level semantic knowledge available only to
Kontrakt.

The goal is not to replace HotSpot, Graal, or another JVM optimizer.

Kontrakt should simplify the program before those optimizers begin their own work.

An object-oriented user implementation may therefore become a denser execution form when equivalence is established.

Possible physical outcomes include fewer allocations, shorter reference chains, direct calls, primitive specialization,
dense layouts, or simpler branches.

Primitive slabbing and vertical partitioning are especially relevant when Contract analysis proves that host object
identity and alias topology are not observable. They allow hot values to be stored and read without preserving
unnecessary object graphs.

Kontrakt should also avoid emitting a generic runtime Contract interpreter when the applicable Contract world is already
closed enough to specialize the path at compile time.

The emitted result should remain friendly to ordinary JVM optimization. Kontrakt-specific specialization should reduce
work for the JVM rather than reproduce machine-level optimization that the JVM already performs well.

---

## 17. Mechanical Sympathy

The larger compiler and realization scope makes mechanical sympathy part of the architecture rather than a later
cleanup.

Compiler data that is read frequently should be eligible for dense ordinal access and primitive storage after its
semantic content has been sealed.

Read-mostly published generations should avoid unnecessary shared mutation. Mutable accounting or scheduling state
should remain separate from immutable semantic and analysis material.

The optimizer should also be free to improve user execution locality when the transformation is proven equivalent.

This may reduce object creation, reference chasing, cache misses, or unstable branch structure before the JVM receives
the program.

These are optimization goals, not Contract meaning. A particular slab width, cache-line layout, or branch arrangement is
always replaceable realization.

---

## 18. Determinism and Equivalence

Optimization and caching must not create a second semantic execution mode.

Equivalent compilation under the same declared inputs must converge on the same Contract result regardless of cache
warmth, worker scheduling, or physical table placement.

A clean build and a reused build must therefore agree on legality and established meaning.

Optimization on and off must also preserve the required semantic result. Differences in physical layout or generated
method structure are allowed when they remain outside Contract-visible meaning.

V1 should provide reference paths and verification hooks strong enough to test these equivalences.

A generated projection must not be treated as correct merely because the same optimization code produced it. Later
compiler design may use differential verification or translation validation where the risk of a transform justifies
independent checking.

The exact validation strategy for each optimization class remains open.

---

## 19. V1 Foundation for V2

V2 plans to add incremental computation, persistent reuse, shared semantic analyses, whole-machine summaries, and a
stronger pass/query architecture.

V1 must not make those features require a semantic rewrite.

V1 therefore needs stable semantic identity that is independent from storage location. It also needs explicit
fingerprints where compiler reuse depends on result sameness rather than semantic identity.

Major compiler computations should take explicit inputs and produce deterministic immutable results where practical.
Hidden ambient state would make later dependency recording unreliable.

Shared analyses must be able to describe what input they depend on. V1 does not need a complete red/green incremental
engine, but it must not hide dependencies inside mutable compiler objects that cannot later become query edges.

Published compiler material must support replacement by a new generation without changing Contract meaning. Local
ordinals may change between generations while semantic references remain stable.

The V1 realization pipeline should also leave clear boundaries for later persistent cache and whole-machine summary
reuse.

V2 may then add dependency recording, invalidation, early cutoff, persistent cache, and parallel query evaluation
without changing the Contract laws established here.

---

## 20. Open in This ADR

The exact V1 Core Realization Closure analysis remains open.

This includes how Kontrakt handles reflection, dynamic class loading, native calls, runtime-generated bytecode, opaque
third-party implementations, and other constructs whose relevant behavior cannot be statically established by the
ordinary analysis path.

The exact observable-realization boundary is also open. Optimization needs a precise rule for when user-visible
identity, exception behavior, ordering, synchronization, concurrency, or another host behavior must be preserved even
though it is not Contract authority.

The exact realization IR and shared analysis schema remain open. This ADR requires their authority separation and reuse
properties but does not fix their public names or physical layouts.

The exact frozen generation and cache architecture remain open. Earlier primitive, HID, interning, L1/L2, and
publication techniques are inputs to that design rather than automatic final answers.

The exact V1 optimization catalog remains open. Arbitrary user implementation optimization now belongs to the
realization architecture, but each transformation still requires a defined legality boundary before it becomes accepted
implementation work.

The exact JVM backend emission path remains owned by the compiler/backend architecture. This ADR requires an optimized
JVM-facing realization but does not choose one final source-emission or classfile-emission strategy.

These questions must be resolved without weakening the fixed decisions in this ADR: Realization remains
non-authoritative, external technology ends before the core, User-System Realization must preserve core closure, and
Kontrakt may optimize only under proven meaning preservation.

---

## 21. Consequences

The Contract Core now has a matching implementation boundary.

Facts are not merely declared at the edges of an opaque user function. Kontrakt verifies that the user realization does
not regain hidden factual input after core entry.

This increases compiler responsibility. Kontrakt must understand more of the host implementation and must maintain
reusable knowledge about that implementation.

The same cost creates a stronger optimization opportunity. Once realization legality, dependencies, and observable
structure are known, Kontrakt can remove implementation machinery that is not required to preserve meaning.

Earlier frozen acquisition, stable identity, interning, cache, primitive storage, and mechanically sympathetic design
work therefore becomes relevant again at a wider compiler scope.

Those techniques remain realization mechanisms. They do not become Contract authority.

V1 becomes more demanding, but its work directly prepares the V2 incremental compiler. Stable identity, immutable
publication, reusable analyses, explicit dependency seams, and dense storage can be extended instead of replaced.

The resulting division of responsibility is explicit:

```text
Contract
    declares meaning

State-Machine
    declares legal movement

User-System Realization
    performs the core computation

Kontrakt Realization
    verifies, optimizes, lowers, and emits that computation

JVM
    executes the already-specialized program and performs its own lower-level optimization
```

This architecture preserves the reason Kontrakt has a Contract Core while allowing the compiler to use that same
explicit meaning to produce a substantially more efficient realization.