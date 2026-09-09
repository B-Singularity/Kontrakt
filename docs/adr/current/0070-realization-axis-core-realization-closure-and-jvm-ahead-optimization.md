# ADR-0070: Realization Axis, Core Realization Closure, and JVM-Ahead Optimization

## Status

Accepted

## Date

2026-09-03

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`
- `docs/todo/v2/kontrakt-v2-reference-architecture-and-v1-foundations.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- `docs/quality/TESTING_STRATEGY.md`
- `docs/constitution/compiler-core-protocols.md`
- `docs/constitution/canonical-ir-stage-and-lowering-protocol.md`
- ADR-0069: Invariant Contract
- ADR-0068: Fact Contract
- ADR-0067: Lowering Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0055: Whole-Machine Pipeline Composition and Contract Concurrency
- ADR-0052: Capacity Contract
- ADR-0051: Budget Contract
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
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

## Supersedes and Reopens

This ADR supersedes only the earlier V1 assumption in ADR-0048 that arbitrary user core realization remains opaque to
Kontrakt and outside V1 optimization.

It does not give Lowering authority over user core realization. ADR-0067 continues to own the Lowering relation and its
own realization boundary. Analysis and optimization after the legal core handoff belong to the Realization axis defined
here.

This ADR reopens the V1 Capacity enforcement assumption that depended on user realization being categorically opaque. It
does not decide that all memory used by user realization is governed Capacity. Exact attribution and backend control
remain Capacity decisions.

ADR-0051 Budget meaning is unchanged. A Budget remains realizable only where the selected backend can preserve the
declared Budget result.

---

## 1. Context

Kontrakt separates Contract from realization.

The Contract axis declares explicit machine meaning and owns Contract judgment. The State-Machine axis owns legal
movement. The Realization axis performs the computation that makes the declared machine executable.

Realization is not a Contract.

Contract meaning is stated through the explicit Kontrakt Contract surface. The IDL is the primary authoring surface for
that meaning. User implementation does not acquire Contract authority merely because the compiler can inspect it.

This separation does not make realization invisible to the compiler. Kontrakt must know enough about user realization to
determine whether the declared Core remains true in the actual program.

The need comes from Fact authority.

External technology is removed before the Core. Adapters form outside information into the presentation that enters the
Contract pipeline. After the inbound boundary has completed, the Core operates from established Fact material rather
than from the technology that originally produced it.

That statement would be false if an Operation could silently read another source from inside its implementation.

For example, a helper may calculate from established Fact values without changing the Core boundary. The same helper
cannot read a live repository and let that value influence the Operation result. The repository value would have entered
the Core without passing through the declared boundary.

Kontrakt must therefore inspect user realization before execution and reject a realization that breaks this closure.

This inspection gives Kontrakt knowledge that a general JVM optimizer does not have. Kontrakt knows the declared
Contract world and the exact Fact surfaces around the Operation. Once it has also verified the relevant realization, it
can use that knowledge to simplify the executable result before handing it to the JVM.

---

## 2. Problem

An Operation boundary does not by itself protect the Core.

A declared flow may appear to be:

```text
PriceFact
    ↓
Operation
    ↓
Result
```

while the implementation actually does this:

```text
PriceFact
    ↓
Operation
    ↓
helper
    ↓
live external configuration
    ↓
Result
```

The helper is not the problem. The new factual source is.

The result now depends on information that did not pass through Adapter formation and the declared inbound Contracts.
The established Fact no longer contains the complete factual basis of the Core computation.

Kontrakt must detect that condition at compile time whenever the realization is accepted as a legal Kontrakt Core
realization.

This added work can make compilation expensive. The same host type or method may be relevant to several Operations.
Verification and optimization may also need the same knowledge. Reacquiring and recomputing that material for each
consumer would make the richer Contract model unnecessarily costly.

The compiler must therefore preserve one direction of production and consumption. Earlier work produces material without
depending on the subsystem that will later use it. Later subsystems consume that material and may derive new realization
knowledge from it.

The optimizer must follow the same rule. It may transform executable realization, but it must not change how Contract
meaning was established.

---

## 3. Decision Drivers

Realization must remain separate from Contract authority.

The Core must not regain external technology after that technology has been removed at the boundary.

Established Fact material must remain the factual basis of the Operation. Internal temporary values are allowed when
they are derived from lawful Core material.

Kontrakt must reject a realization when outside information can influence the Core through a path that bypasses its
owning boundary.

A realization that cannot be verified under the supported compiler model must not be silently accepted as closed.

Realization topology is compiler-visible but is not part of the outward Contract surface.

Ordinary user realization must not depend on the verifier or optimizer that Kontrakt happens to use. Changing those
compiler techniques must not create a new Contract requirement for otherwise legal user code.

Verification machinery must remain realization. A proof system may help Kontrakt establish a required property, but the
proof system does not define the machine.

A later compiler subsystem must not force an earlier producer to reinterpret its result for that subsystem's purpose.

The compiler must not reconstruct the same already-produced meaning or reacquire the same valid realization knowledge
for each downstream subsystem. Deliberate independent verification remains allowed when independence is part of the
check.

The mechanism used to avoid accidental duplicate work remains replaceable implementation.

Optimization must preserve the established Contract meaning. The technique used to achieve that optimization is not
Contract meaning and may be replaced as better techniques become available.

V1 must create a foundation that lets V2 add stronger incremental reuse without requiring the Contract architecture to
change.

---

## 4. Decision

Kontrakt defines Realization as a separate non-authoritative axis with two domains.

```text
Realization Axis
    ├── User-System Realization
    │       the implementation supplied for an Operation
    │
    └── Kontrakt Realization
            the compiler and runtime machinery that produces
            the executable realization
```

User-System Realization performs the user's Core computation. Its source structure does not create Contract meaning.

Kontrakt Realization examines that implementation and produces the executable form. Its own internal structures also
have no Contract authority.

The semantic direction remains:

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

Kontrakt separately performs compiler work over the declared machine and its realization:

```text
Declared Contract World
        +
User-System Realization
        ↓
compile-time verification
        ↓
legal Core realization
        ↓
Contract-preserving optimization
        ↓
JVM-facing realization
```

The second flow implements the first. It does not become another Contract pipeline.

---

## 5. User-System Realization

User-System Realization is the implementation supplied for a declared Operation.

It may contain ordinary computation. A private helper or a local temporary does not become Contract material merely
because Kontrakt analyzes it.

Fact authority also does not require a particular runtime object shape. The Operation may work with host values that
realize the established Fact material.

The important question is the origin of the information that affects the Operation.

```text
established Fact material
    ↓
internal calculation
    ↓
Operation result
```

may remain legal.

```text
established Fact material
    +
new outside information
    ↓
Operation result
```

is not a closed Core realization.

User-authored source topology does not have to survive execution. A helper object or temporary carrier may disappear
when Kontrakt can prove that the change preserves the required meaning.

---

## 6. Explicit Contract Surface and User-System Independence

Kontrakt keeps Contract declaration separate from ordinary user implementation.

The IDL is the primary place where the user states the machine's explicit Contract meaning. A supported host declaration
may provide source evidence when an owning Contract ADR allows it, but that host declaration does not gain authority
from its class or runtime behavior.

Kontrakt does not discover missing Contract meaning from the shape of user implementation. A helper name or class
hierarchy does not become a Contract because the compiler can read it. Verification-oriented source material has the
same limit.

The user system should need Kontrakt knowledge only at an explicit integration boundary. A generated User API or an
Adapter is such a boundary. Ordinary Core implementation should not need to call a Kontrakt checker or carry
Kontrakt-specific proof machinery.

Compiler inspection is one-way. Kontrakt may read user realization to verify and optimize it, but the user realization
does not become valid by depending on the current verifier or optimizer.

A replacement verifier or optimizer must therefore be able to consume the same declared Contract and legal user
realization without requiring those sources to adopt the replacement's private model.

---

## 7. Adapter and Core Boundary

External technology ends before the Core.

A live external capability belongs outside Core realization. A database connection or system clock is an example. Its
information may enter only after the applicable Adapter and inbound Contract processing have formed lawful Core
material.

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

The Core may use the established information. It does not keep the live external mechanism that produced it.

Moving an external access behind another method does not change this rule.

This ADR does not redefine Adapter, Input, or Lowering meaning. It requires User-System Realization to preserve the
boundary those owners already establish.

---

## 8. Core Realization Closure

A user Operation realization must remain closed over the factual material and machine context lawfully available at its
Core position.

Core Realization Closure is a rule over implementation. It is not a new Contract kind.

Internal values may be derived from established material. Those values remain realization material and do not acquire
independent Fact authority.

Closure fails when an outside source can affect a Contract-visible result or State-Machine movement without first
passing through the boundary that owns that information.

Kontrakt therefore checks the origin of relevant information rather than treating a local call boundary as proof of
safety.

The exact set of machine material lawfully available to each Core realization remains owned by the Contracts and
State-Machine decisions that establish that material. This ADR does not create a second source of machine meaning.

---

## 9. Compile-Time Realization Verification

Kontrakt must inspect enough User-System Realization to establish Core Realization Closure before accepting the
executable realization.

The analysis may follow a helper because the helper influences the Operation result. A deeper call cannot be treated as
safe merely because its caller is local.

When Kontrakt establishes that outside information affects Core computation through an illegal path, compilation fails.

When the supported analysis cannot establish closure, Kontrakt must not silently assume that closure exists.

Runtime Contract judgments that require runtime values remain runtime judgments. This ADR does not move those judgments
into compile time.

The exact V1 verification method is deferred to follow-up realization design. The verification boundary and the
fail-closed requirement established here do not depend on that method.

---

## 10. Verification Machinery Remains Realization

Kontrakt may use mathematical or static verification techniques to decide whether realization satisfies an
already-declared requirement. Those techniques remain part of Kontrakt realization.

A verification condition is not Contract meaning. Solver guidance and verification-only state remain compiler material
even when a verifier depends on them.

The current verifier must not make its preferred proof structure a hidden requirement on the user's algorithm. If one
verifier needs a different internal model, Kontrakt should change that realization machinery rather than silently
turning the model into a new user Contract.

Failure to establish a required property is also distinct from establishing a violation. Kontrakt may refuse compilation
when required realization evidence cannot be obtained, but diagnostics must preserve the difference between a proven
violation and an inconclusive or unsupported analysis.

Kontrakt must not treat an unchecked assumption as Contract satisfaction. A verification shortcut cannot create
authority that the declared machine did not establish.

Formal proof may still be used to check Kontrakt's own transformations. In that role it protects realization correctness
and remains replaceable with the compiler machinery that uses it.

---

## 11. No Implicit Runtime Interception

Kontrakt does not preserve Core Realization Closure by placing a general interception layer around user implementation.

A proxy or wrapper that watches ordinary calls at runtime would leave the user realization opaque until execution. It
would also introduce another execution path whose presence is not part of the declared Contract.

Core-closure verification therefore belongs to compilation. If the supported compiler model cannot establish the
required closure, Kontrakt does not silently defer that uncertainty to a runtime proxy.

This does not remove Contract judgments that depend on runtime values. Such judgments remain part of the declared
machine and are emitted in the executable form required by their owning Contract.

The runtime form of those judgments does not need to preserve a proxy, wrapper, or general Contract interpreter merely
to mirror the source-level Contract structure.

---

## 12. Realization Topology and Cycles

User implementation structure is not Contract authority. This ADR therefore does not classify a call or type cycle as
Contract-valid or Contract-invalid merely because the cycle exists in implementation.

Ordinary JVM code can lead the compiler back to implementation material it has already seen. Recursion is one example.
Class and call relationships may create the same analysis problem.

Kontrakt must detect that return so analysis terminates and already-acquired realization knowledge is not rebuilt
without need.

Cycle detection does not change the user's algorithm and does not turn implementation topology into Contract meaning.

A Contract semantic cycle remains governed by the Contract laws that own semantic dependency.

Earlier cycle-detection work may be reused for compiler traversal. The technique remains replaceable realization.

The exact recursive-region analysis is deferred to follow-up realization design. Compiler traversal must still terminate
without turning implementation recursion into Contract semantics.

---

## 13. Contract Material and Realization Material

Contract material and realization material must remain distinct even when the compiler stores or references them
together.

Established Contract material keeps the authority of the Contract that established it.

Knowledge learned from user implementation describes realization. It does not gain Contract authority because it is
useful to verification or optimization.

The same rule applies to compiler storage. A table index or another compact handle may make access cheaper, but it does
not become semantic identity merely because many subsystems use it.

The direction is always:

```text
Contract meaning
    ↓
constrains realization
```

and never:

```text
realization topology
    ↓
defines Contract meaning
```

This separation lets Kontrakt change compiler representation without changing the declared machine.

---

## 14. Producer and Consumer Direction

Material produced earlier in compilation must not depend on which later subsystem will consume it.

A source or semantic producer establishes its own result. It does not change that result because an optimizer, verifier,
diagnostic subsystem, or backend happens to exist.

Later compiler subsystems consume already-produced material. They may derive new realization material for their own
work, but that derivation does not rewrite the authority or meaning of the source they consumed.

For example, a verifier and an optimizer may read the same produced material. Neither consumer changes how that material
was established.

This direction prevents each subsystem from creating a separate semantic path from the same source.

When an earlier producer has already established a result, downstream compiler work must consume that result instead of
reconstructing the same meaning for its own purpose. A later subsystem may derive new realization knowledge only from
the material it is allowed to consume.

This also keeps future subsystems replaceable. Adding a new optimizer must not require the producer to gain
optimizer-specific meaning.

The exact mechanism used to reuse a produced result is implementation. Independent verification may intentionally
recompute a result when independence is part of the check.

---

## 15. Replaceable Compiler Realization

No compiler technique named in current Kontrakt design work becomes Contract meaning through this ADR.

V1 selects query-oriented compiler orchestration as its compiler architecture. Product requests are evaluated through
explicit query boundaries, and those queries coordinate reusable compiler work. This is a V1 realization decision. It
does not create a Query Contract in the user machine and does not make query execution part of Contract authority.

Passes remain local processing mechanisms inside the computation that produces a query result. A global pass pipeline is
not the top-level architecture for the whole Kontrakt compiler because Kontrakt produces verification, test, diagnostic,
realization, and backend products that do not form one linear semantic chain.

The query architecture itself remains replaceable realization. A later compiler architecture may replace the
orchestration mechanism through a later architecture decision as long as the published compiler products preserve the
same required meaning and dependency laws.

The same rule applies to storage and publication. Current design work may use sealed publication, HID-backed lookup,
primitive slabs, dense tables, or FFM-backed storage. Those mechanisms may be replaced when another realization
satisfies the same publication, identity, boundedness, and determinism requirements.

This replaceability is required for optimization in particular. Kontrakt is expected to adopt stronger compiler
techniques as they become practical. The declared Contract meaning and a legal user realization must not need to change
merely because that machinery changes.

---

## 16. Query-Oriented Reuse and Compiler Cost

Realization verification must not make Kontrakt repeatedly pay for the same valid compiler knowledge without reason.

V1 therefore uses query-oriented orchestration for major compiler products and expensive derived results. A query has a
stable logical identity, explicit inputs, an explicit result, recorded compiler dependencies, and a validity relation to
the compiler generation from which the result was produced.

A published query result is immutable to ordinary consumers. Cache presence does not make that result true. The owning
compiler subsystem establishes the result, and the query engine records and reuses it.

The query dependency graph is not a Contract semantic graph. A query that reads another query creates a compiler
recomputation dependency. It does not create a missing Contract dependency or a new Required Basis relation.

The same separation applies to identity. Semantic identity, query identity, result fingerprint, HID lookup material,
dense reference, and physical address answer different questions. None becomes interchangeable merely because one
representation is faster.

Verification and optimization should consume already-produced compatible realization summaries instead of repeating the
same acquisition or graph traversal. Diagnostics and PBT may consume the same compatible derived analysis when
independence is not part of the check.

V1 records dependencies in memory and binds reusable results to an exact compiler generation or equivalent revision
boundary. Product storage remains typed by the subsystem that owns the result rather than using one universal object
payload.

The current acquisition and identity work remains useful under this architecture. Stable realization identity can
recognize material already acquired. Cycle handling can prevent recursive JVM topology from causing unbounded
acquisition. Primitive storage can keep the query and dependency substrate compact. These remain realization techniques
behind the query architecture.

Compiler cost is part of realization quality. Rich Contract semantics must not force every downstream consumer to reopen
source material, rebuild equivalent indexes, or recalculate already-valid derived knowledge.

---

## 17. Optimization Boundary

Optimization belongs entirely to realization.

Kontrakt does not prescribe the algorithm chosen by the user implementation. A lawful algorithm does not become a
Kontrakt algorithm merely because the compiler inspects it.

Optimization acts on the executable realization around that computation. Intermediate material does not have to keep the
authored object topology when that topology carries no required meaning.

For example, a temporary carrier may disappear when its identity is not observable and the same required value reaches
the same consumer. The calculation remains the user's realization while its intermediate physical form changes.

The logical Contract pipeline also does not require a separate runtime object or call for every logical judgment.
Physical work may be combined when the same judgments and results are preserved.

Kontrakt must establish realization legality before using that knowledge for optimization.

No particular optimization technique is fixed by this ADR. Kontrakt may replace its optimization machinery when the
replacement preserves the required result.

The exact V1 optimization set is deferred to optimization design and release planning. This ADR fixes the optimization
boundary and preservation law rather than a permanent transform catalog.

---

## 18. JVM-Ahead Optimization

Kontrakt must use proven Contract-specific and realization knowledge before the JVM begins its own optimization.

A general JVM optimizer can see program structure and runtime behavior. It does not begin with Kontrakt's explicit
knowledge of Fact meaning or Contract applicability.

Kontrakt can therefore remove work that is unnecessary only because the declared machine is known.

The result handed to the JVM must already reflect that knowledge when a safe transformation removes work that would
otherwise have to be rediscovered or interpreted at runtime.

Kontrakt must not hand a general Contract interpreter to the JVM when compile-time knowledge can produce the same
required behavior directly. Runtime Contract judgments still remain where runtime values are required, but their
executable form should use the information already established before execution.

A user may write an object-oriented realization while Kontrakt emits a simpler execution form. The emitted program does
not have to preserve allocation or reference topology that carries no required meaning.

Current design work on primitive slabbing and mechanically sympathetic layout may be used for such transformations when
their preconditions are proven. These are implementation techniques, not requirements of the Contract model.

Kontrakt does not replace HotSpot, Graal, or another JVM optimizer. It performs the semantic simplification that depends
on Kontrakt knowledge and leaves ordinary lower-level optimization to the JVM.

The exact JVM emission strategy is deferred to backend design. JVM emission remains downstream of the Contract-aware
optimization boundary established here.

---

## 19. Determinism and Meaning Preservation

Compiler reuse and optimization must not create another semantic mode of Kontrakt.

A legal program must not become illegal because a reusable compiler result was present, and an illegal program must not
become legal because a cache was warm.

Changing worker scheduling or physical placement must not change established Contract meaning.

Optimization may change emitted structure. It may not change the Contract result that the unoptimized legal realization
is required to preserve.

Kontrakt must provide enough independent checking to detect incorrect transformations at the risk level appropriate to
each optimization class.

The exact validation technique remains implementation. A later compiler may replace one checking method with another
without changing this decision.

---

## 20. V1 Foundation and V2 Evolution

V1 owns Core realization verification and Contract-aware optimization before JVM emission.

V1 also selects a query-oriented compiler architecture from the beginning. Major compiler products are requested through
explicit query boundaries. Dependencies are recorded in memory, reusable results have generation validity, and
publication remains deterministic.

The minimum V1 query substrate is intentionally smaller than the V2 incremental system. V1 needs stable logical query
identity, explicit inputs and results, dependency recording, generation-bound validity, typed result publication, and
fingerprints where reuse validation benefits from them. The exact physical tables and cache policy remain
implementation.

Verification is not the endpoint. After a realization is accepted, V1 may use established Contract knowledge and
verified realization knowledge before JVM lowering. The exact minimum optimization set is deferred to optimization
design and release planning.

V1 must preserve logical compiler stages without requiring a complete object graph for every stage. The same backing
material may be published with additional derived products when the semantic vocabulary has not changed. A new
representation is required only when a new logical level actually needs a different vocabulary, invariant set, or
equivalence relation.

V2 extends the V1 architecture rather than replacing it. V2 may persist query results and dependency records across
sessions. It may add red-green validation, semantic early cutoff, lazy materialization, summary-driven linking, multiple
immutable compiler generations, and parallel demand evaluation.

Different V2 subsystems may use different incremental algorithms. A relational analysis does not need the same repair
strategy as a JVM artifact product. Incremental state, cache state, dependency state, and scheduling remain derived
compiler material.

V2 may replace any particular incremental mechanism when another implementation preserves the same semantic results,
invalidation correctness, determinism, and publication laws.

---

## 21. Compiler Architecture

This section defines the architecture that ADR-0070 requires for V1 and the extension seam that V2 must preserve.

The architecture is not a new Contract hierarchy. It is the compiler structure that consumes established Contract
authority and user realization without allowing either compiler topology or optimization machinery to become Contract
meaning.

### 21.1. Top-Level Material Flow

The Contract side and the realization side remain separate until explicit execution formation consumes both.

```text
                         CONTRACT FRONTEND

.kontrakt
    ↓
Source / Syntax Material
    ↓
Resolution
    ↓
Resolved Contract HIR
    ↓
Authority-owned Establishment
    ↓
════════════════════════════════════════════════════
                  CONTRACT AUTHORITY

Canonical Contract World
    │
    ├── Contract Verification
    ├── Reference Judgment
    ├── PBT / Fixture / Unit-Test Synthesis
    ├── Contract Coverage
    ├── Compiler Diagnostics
    ├── Diagnostic Evidence Realization Planning
    └── Execution Formation

════════════════════════════════════════════════════
                  USER REALIZATION

User JVM Implementation
    ↓
Realization Acquisition
    ↓
Realization Body Material
    ↓
Realization Analysis / Summaries
    ↓
Core Realization Closure Verification
    ├── proven violation → compile refusal
    ├── unsupported      → compile refusal under the selected V1 support rule
    └── verified
            ↓
      Verification Result / Overlay
            ↓
      Execution Formation
            ↓
Contract-Aware Execution IR
            ↓
Contract-Preserving Optimization
            ↓
JVM Realization Lowering
            ↓
JVM-facing Product
            ↓
JVM
```

The compiler may share physical backing storage across logical stages when the later stage only adds a verified property
or derived index. The diagram defines logical boundaries, not a requirement to allocate a full copy at every arrow.

### 21.2. Canonical Contract World Is the Authority Substrate

ADR-0063 remains the semantic owner of Establishment and the Canonical Contract World.

This ADR does not introduce a second `Established Contract World`. `Canonical Contract World` is the architecture term
used for the compiler-owned substrate that exposes already-established Contract definition meaning to downstream
products.

The Canonical Contract World is not an ordinary optimization IR. It does not flatten every one-dimensional Contract into
a universal node shape.

Input remains owned by Input. Fact remains Fact. Governance keeps ownership of the Binding it establishes. Failure
remains owned by Failure. The common world gives downstream consumers exact references to authority-owned material and
the relations already established by their owning laws.

Occurrence-specific material remains separate where the owning Contract semantics require it. A diagnostic or runtime
consumer does not create a new Contract occurrence merely because it needs an exact reference.

Source provenance remains adjacent to semantic material rather than part of semantic identity. A source-only change may
therefore refresh provenance without forcing an unchanged semantic definition to become a different Contract meaning.

### 21.3. One-Dimensional Authorities Publish Distinct Material

The compiler must preserve the distinctions established by the one-dimensional Contract ADRs.

The inbound authorities establish the lawful movement from external presentation toward Core material. Fact and
Invariant provide factual and judgment material without requiring the host object topology to survive. State and
Transition remain separate from ordinary realization control flow.

Policy, Governance, Version, Budget, and Capacity provide applicable machine context under their own laws. Compiler
scheduling, cache policy, or memory layout cannot substitute for those Contract meanings.

Failure, Publication, and Output preserve refusal and outward claim meaning through execution formation and lowering.
Diagnostic Evidence and Retention remain Contract material where their owning laws establish them, while compiler
explanation and rendering remain separate compiler products.

A downstream consumer reads only the authority material and derived relations it requires. Consumer convenience does not
add fields to an authority-owned Contract simply because one subsystem would prefer them nearby.

### 21.4. Product Subsystems Are Siblings

Kontrakt does not have one semantic pipeline in which Verifier produces PBT, PBT produces Diagnostics, and Diagnostics
produce Backend meaning.

The major product subsystems consume the Canonical Contract World as siblings.

```text
                  Canonical Contract World
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
    Verifier          Test Synthesis      Diagnostics
        │                  │                  │
        └──────────────┬───┴───────┬──────────┘
                       │           │
                       ▼           ▼
              Shared Derived    Reference
                 Knowledge      Judgment
                       │
                       ▼
                Execution Formation
```

Shared derived knowledge is allowed when the same result is valid for several consumers. The subsystem that establishes
the analysis owns that result. The consumers do not acquire authority over the Contract material from which it was
derived.

Reference Judgment remains a sibling product rather than the semantic source of the others. The exact amount of derived
analysis it may share with optimized paths is deferred to Reference Judgment and compiler-QA design because that choice
affects the independence of differential checking.

### 21.5. V1 Query Architecture Owns Product Orchestration

V1 uses query-oriented orchestration for compiler products.

```text
Compiler Product Request
        ↓
Query Boundary
        ↓
Owning Compiler Computation
        ↓
Published Result
```

A major query result has an explicit logical key and explicit inputs. Query evaluation records the results it consumed.
A published result is immutable to ordinary consumers and has a validity relation to the compiler generation that
produced it.

The query engine coordinates work. It does not own Contract semantics.

Passes and local transform pipelines execute inside the computation that produces a query result. They do not become the
top-level architecture for products that have no semantic reason to run in one linear order.

The query graph, Contract semantic relations, realization call/effect relations, and diagnostic provenance relations
remain separate graph families. Their cycles, identities, lifetimes, and invalidation rules are not interchangeable.

Query result storage remains typed by product family. A verifier result, an execution product, a diagnostic product, and
a JVM product do not become one universal `Object` result merely because the query engine can address all of them.

### 21.6. Realization Acquisition Is a Separate Frontend

The Contract frontend and the realization frontend remain separate compiler subsystems.

The realization frontend starts from the explicit host realization binding and acquires the JVM implementation required
for Core verification. Kotlin or Java implementation topology does not participate in Contract name resolution.

The acquired realization form must make compiler analysis possible without promoting that form into Contract meaning. At
minimum, the logical realization body representation must support explicit control flow, exact value relations,
classified call/effect sites, source provenance, and explicit unsupported constructs.

Recursive host call topology is handled by compiler traversal and analysis. It does not change the Contract rule that
semantic cycles are illegal where the owning Contract law forbids them.

Backend-native handles do not become published semantic authority. A compiler may keep temporary handles during
acquisition, but later consumers must not need to reopen the acquisition backend to reconstruct the same valid
realization knowledge.

### 21.7. Verification Adds a Property; It Does Not Require a Full New IR

Core Realization Closure verification consumes the acquired realization and the established Contract material that
constrains it.

Verification produces an explicit result tied to the exact realization generation it checked. Proven violation,
unsupported analysis, and successful establishment must remain distinguishable.

`Verified Realization` is a logical boundary. V1 does not require a full second copy of the realization IR merely
because verification succeeded.

A lawful physical implementation may use:

```text
Realization Body Material
        +
Verification Result / Index
```

when the semantic vocabulary of the realization has not changed.

Verification-derived summaries may be published for later consumers. A backend or optimizer should not rerun Core
closure analysis merely to rediscover a result that the verifier has already published and that remains valid for the
same generation.

The exact supported JVM subset and the treatment of opaque implementation are deferred to the realization capability
matrix and follow-up verifier design.

### 21.8. Shared Analysis Has Explicit Ownership and Validity

Analysis and transformation are different compiler work.

Analysis reads a material generation and establishes derived compiler knowledge. Transformation changes realization
material or lowers it into a different logical level.

A shared analysis result has an owner, an input generation, and a validity boundary. When a transformation changes
material on which the analysis depended, the transformation must either preserve that analysis explicitly or cause later
consumers to request a valid replacement.

This rule applies whether V1 stores the result in a compact analysis table or another structure. The physical cache is
not the architectural meaning.

Deliberate independent recomputation remains allowed when independence is part of verification. Ordinary duplicate
recomputation is not the default architecture.

### 21.9. Contract-Aware Execution IR Is a Separate Logical Level

Execution formation is where established Contract meaning and verified user realization are combined into an executable
compiler representation.

This requires a logical level distinct from both the Canonical Contract World and the acquired user realization.

The Contract-Aware Execution IR must make runtime-required judgments explicit and retain exact references to the
Contract authority that owns those judgments. It must preserve the relations needed for Failure, State movement,
Publication, Output, and required diagnostic attribution.

It must not duplicate the Canonical Contract World into every IR operation. An execution operation keeps only the exact
reference needed to reach the authoritative material.

JVM-specific stack shape, local-slot placement, bytecode encoding, or physical object layout do not belong to this
level.

Optimization normally produces another generation satisfying the same Execution IR contract. An optimization generation
is not automatically a new semantic IR level.

### 21.10. Optimization Uses Contract and Verification Knowledge Before JVM Lowering

V1 contains an explicit Contract-aware optimization stage before JVM-specific lowering.

The optimizer may consume fixed established Contract context and valid realization analyses. It may simplify generated
Contract machinery or physical realization only when the required meaning is preserved.

Existing Kontrakt optimization work remains the baseline. Stable acquisition, exact identity, sealed publication,
bounded reuse, direct materialization, dense storage, and mechanically sympathetic layout remain usable implementation
techniques.

The architecture also permits higher-level Contract-aware optimization. Static discharge, specialization,
unreachable-path reduction, exact binding use, intermediate removal, and judgment fusion remain candidates where their
legality is established.

The exact minimum transform set required from V1 is deferred to optimization design and release planning. The optimizer
stage and its preservation boundary are fixed by this ADR.

Legality and profitability remain separate. Contract and realization preservation decide whether a transform may run. A
cost model decides whether a legal transform is worth applying.

### 21.11. JVM Lowering Is a Separate Target Boundary

Kontrakt retains high-level Contract knowledge until Contract-specific simplification is complete.

Only then does JVM lowering choose target-specific execution form.

The JVM lowering boundary may introduce JVM value forms, invocation forms, control transfer, classfile constraints, and
backend capability checks. It must not reinterpret the Contract meaning that justified the earlier execution form.

The emitted product should be friendly to HotSpot or Graal without attempting to replace their general low-level
optimization role.

Physical layout planning may be fused with backend work or may exist as a distinct derived product. Its target profile,
slab layout, alignment, and storage mechanism remain realization. They do not define semantic identity.

### 21.12. Published Compiler Material Uses Complete Generations

A consumer must not observe half-built compiler material.

The V1 architecture retains the existing construction discipline:

```text
private construction
    ↓
verification
    ↓
seal
    ↓
publish
```

The exact physical mechanism may use frozen tables, immutable slabs, or another representation. The architectural
property is complete publication.

Generation identity is compiler state rather than Contract identity. Two compiler generations may contain the same
semantic Contract World.

High-cardinality material must not require one JVM object per logical entity. Published hot material must remain
lowerable to dense tables, primitive columns, compact ranges, or another index-addressable representation without
changing its logical contract.

Logical stage boundaries do not require full physical copies. A verification overlay may share the realization body. An
execution region may be materialized only when the requested product requires it.

### 21.13. Whole-Machine Work Uses Explicit Summaries

Whole-Machine work has an explicit summary boundary from V1.

A summary is derived compiler material. It is not a second Contract authority and does not replace the full unit when
full material is required.

The summary boundary must allow global decisions to be made without eagerly merging every complete Contract unit or
realization body into one giant graph.

V1 may compute summaries eagerly and keep them only in memory. V2 may persist them, use them for incremental linking,
and materialize full units only when a requested downstream product requires them.

The exact summary schema belongs to design and follows the final Contract semantics owned by the relevant ADRs.

### 21.14. Diagnostics, PBT, and Compiler QA Remain Separate Products

Compiler diagnostics consume semantic subjects, source provenance, and compiler-derived evidence. They do not reopen the
IDL and independently reconstruct Contract meaning.

Contract Diagnostic Evidence remains separate from compiler diagnostics. The first is Contract material where declared
by its owning law. The second is a compiler product explaining compilation and realization results.

PBT, fixture generation, and unit-test synthesis are product subsystems. Generated cases identify the exact Contract
obligation from which they were derived. Test generation does not become production Contract authority.

Compiler QA remains separate from generated user tests. Differential testing, fuzzing, golden vectors, and translation
validation may check the compiler and its transformations without becoming part of the user Contract model.

### 21.15. V2 Extends the Same Product Graph

V2 keeps the V1 product and authority boundaries.

It extends query execution with persistent result and dependency state. Recomputed results may stop propagation when
their consumer-visible result is unchanged. Source provenance may invalidate independently from unchanged semantic
material.

V2 may add lazy semantic and realization materialization, persistent summaries, multiple immutable compiler generations,
parallel demand evaluation, and selected persistent caches.

Incremental granularity is product-specific. The compiler does not require one universal incremental algorithm for
semantic queries, relational analyses, whole-machine summaries, and backend artifacts.

HID or another compact fingerprint may support reuse validation. Merkle structure may localize structural changes.
Dependency graphs record computational consumers. Cache tiers decide retention. These mechanisms remain distinct.

The same equivalence laws apply across clean and reused execution:

```text
clean build
    ==
incremental build

cold cache
    ==
warm cache

one worker
    ==
multiple workers
```

The equality concerns semantic and required product results. Physical scheduling and cache state may differ.

### 21.16. Deferred Decisions Do Not Reopen This Architecture

The architecture above is complete without fixing every later realization policy or implementation choice. Section 22
records decisions that belong to follow-up realization, verifier, optimization, backend, or compiler-QA design.

Those later decisions do not reopen the Canonical Contract World, query-oriented V1 orchestration, sibling product
structure, separate realization frontend, verification overlay, shared-analysis validity law, Contract-Aware Execution
IR, JVM lowering boundary, or V2 incremental extension seam.

---

## 22. Deferred Realization Decisions

The following decisions are intentionally deferred because they depend on concrete realization, verifier, optimizer,
backend, or compiler-QA construction. They are not acceptance blockers for this ADR.

The exact host-language observability boundary for transformed user realization is deferred. Later design must state
which supported host-language behavior outside declared Contract meaning must be preserved by a particular
transformation. That decision may refine optimizer legality without changing Contract authority.

The V1 realization capability matrix is deferred. Reflection, method handles, `invokedynamic`, native execution, dynamic
class loading, generated bytecode, and opaque third-party implementation must be classified when the realization
frontend and verifier define the exact subset they can establish safely. Unsupported material must continue to fail
closed where Core Realization Closure is required.

The trusted-summary boundary is deferred. User-declared trust cannot manufacture Contract satisfaction. A later verifier
or backend decision may define whether Kontrakt-owned or backend-owned verified intrinsic summaries can represent
implementation material that cannot be inspected directly.

The exact V1 optimization obligation set is deferred to optimization design and release planning. Section 21 already
establishes the optimization stage, its inputs, its preservation boundary, and the requirement that the architecture
remain able to accept stronger later optimization.

The exact Reference Judgment independence boundary is deferred to Reference Judgment and compiler-QA design. Later work
may decide which derived analyses are safe to share while preserving the independent-checking value of the reference
path.

These decisions refine realization. They do not redefine the architecture accepted here.

Realization remains non-authoritative. External technology still ends before the Core. User-System Realization must
preserve Core Realization Closure.

V1 remains query-oriented. Query state and cached products remain compiler realization rather than Contract authority.

Compiler techniques remain replaceable. Optimization may proceed only when the required meaning is preserved.

---

## 23. Consequences

The Core boundary now applies to the actual user realization rather than stopping at the Operation signature.

Kontrakt must inspect enough implementation to detect factual input that bypasses the declared boundary. This increases
compiler work, but it makes Fact authority real inside the Core rather than merely descriptive at its edges.

Core closure is therefore not enforced by surrounding ordinary user calls with an implicit proxy or monitoring layer.

The Contract remains explicit in the Kontrakt Contract surface rather than being scattered through proof structures
inside ordinary implementation. This keeps the user system independent from the verifier and optimizer chosen by
Kontrakt.

The same compiler knowledge must be reused for optimization after legality has been established when it proves a safe
simplification. Kontrakt therefore does not stop at verification and restore the authored realization unchanged by
default.

V1 organizes that compiler work through query-oriented product boundaries. Verification, diagnostics, test synthesis,
execution formation, optimization, and backend products can therefore request and reuse valid upstream material without
forming one semantic authority chain.

Kontrakt may simplify the physical execution around a lawful user computation before JVM emission without making its
current technique part of Contract meaning.

Earlier publication and reuse work remains useful. Mechanically sympathetic storage may also be reused where it still
fits the new realization model.

This ADR does not preserve those mechanisms by name. It preserves the reason they were useful and allows a better
implementation to replace them.

Compiler material now follows one direction. A producer creates material under its own meaning. Later subsystems consume
it without pushing their own purpose back into the producer.

This keeps Contract authority explicit while allowing later compiler subsystems to evolve without changing the producer
that established their input.

The resulting direction is:

```text
explicit Contract meaning
        +
legal State-Machine movement
        ↓
User-System Realization
        ↓
Kontrakt verification and optimization
        ↓
JVM-facing realization
        ↓
JVM execution
```

Kontrakt can therefore make stronger guarantees about the integrity of the Core while continuing to replace its compiler
and optimization machinery as better techniques become available.