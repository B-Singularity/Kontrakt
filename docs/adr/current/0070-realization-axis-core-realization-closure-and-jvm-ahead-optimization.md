# ADR-0070: Realization Axis, Core Realization Closure, and JVM-Ahead Optimization

## Status

Proposed

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

External frameworks may compose around a Kontrakt Interaction. They must not silently participate in a governed
realization.

Kontrakt must defend the supported execution envelope strongly. Known intervention that violates closure must be
rejected. A host mechanism that mutates or bypasses an admitted realization outside that envelope is outside Kontrakt's
guarantee.

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

Representation-changing optimization must stay inside an admitted closed region. Kontrakt should reuse analysis already
needed for verification before paying for additional realization analysis.

Kontrakt must not duplicate expensive generic JVM optimization merely to remove arbitrary user allocations.

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

External framework work may surround this flow. It remains outside the governed realization.

```text
External host / framework
        ↓
boundary formation
        ↓
Kontrakt Interaction
        ↓
closed governed realization
        ↓
Output
        ↓
external host / effect
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

External frameworks may compose around a Kontrakt Interaction. Their runtime machinery must not silently enter the
governed realization.

A host transaction may remain open while the Interaction executes.

```text
begin transaction
    ↓
read external state
    ↓
form lawful boundary input
    ↓
Kontrakt Interaction
    ↓
Output
    ↓
apply external effect
    ↓
commit
```

The transaction remains host behavior. The Core does not gain permission to read a repository or ambient transaction
state merely because the host keeps that transaction open.

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

Closure also fails when undeclared external runtime behavior participates inside the governed realization. Framework
interception does not become lawful merely because it is attached to an Operation implementation.

Runtime substitution that can change the participating realization is not part of a closed region unless the supported
compiler and backend model can establish the required stability.

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

Known metadata that requests external runtime participation inside the governed realization is also rejected. Annotation
syntax alone is not the rule; metadata that is inert under the supported model does not violate closure merely because
it exists.

When the supported analysis cannot establish closure, Kontrakt must not silently assume that closure exists. The
realization is not admitted.

Runtime Contract judgments that require runtime values remain runtime judgments. This ADR does not move those judgments
into compile time.

The exact V1 verification method remains open.

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

## 11. No Implicit Runtime Interception Inside the Governed Realization

Kontrakt does not preserve Core Realization Closure by placing a general interception layer around user implementation.

A proxy or wrapper that watches ordinary calls at runtime would leave the user realization opaque until execution. It
would also introduce another execution path whose presence is not part of the declared Contract.

The same boundary applies to external frameworks. A framework may intercept host code outside a Kontrakt Interaction,
but it may not silently insert runtime behavior into the governed realization.

Core-closure verification therefore belongs to compilation. If the supported compiler model cannot establish the
required closure, Kontrakt does not silently defer that uncertainty to a runtime proxy.

Kontrakt should reject known intervention paths before execution. If a supported runtime check detects that an admitted
realization has changed, execution must fail closed or the affected product must be invalidated before use.

Kontrakt does not claim to control arbitrary host mutation outside the supported execution envelope. If an unsupported
host mechanism bypasses that envelope and changes an admitted realization without a supported validation path, the
resulting execution is outside Kontrakt's guarantee.

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

The exact recursive-region analysis remains open.

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

Current design work already contains useful realization techniques. Frozen publication is one example. Primitive storage
is another.

These techniques may reduce repeated work or improve machine behavior, but they remain realization.

Kontrakt may replace them when a better technique satisfies the same required result.

For example, current publication may use a build-then-seal structure. A later implementation may use another way to
ensure that consumers never observe incomplete material. The Contract architecture does not depend on the name `Frozen`.

The same rule applies to incremental compilation. The current V2 plan may use query-style dependency recording. Query is
an implementation strategy, not a Contract concept. A later incremental engine may replace it without changing the
Contract laws in this ADR.

This replaceability is required for optimization in particular. Kontrakt is expected to adopt stronger compiler
techniques as they become practical. The declared Contract meaning and a legal user realization must not need to change
merely because that machinery changes.

---

## 16. Reuse and Compiler Cost

Realization verification must not make Kontrakt repeatedly pay for the same host-program knowledge without reason.

If several Operations depend on the same valid class or method knowledge, the compiler must not reacquire that knowledge
separately for each Operation without a reason.

The same rule applies to derived realization knowledge. Verification and optimization must consume an already-produced
compatible result instead of repeating the same source traversal merely because they are different subsystems.

This requirement does not prescribe a cache hierarchy or a query engine.

Earlier acquisition and identity work may be reused to satisfy it. For example, stable realization identity can let the
compiler recognize material it has already acquired, while cycle-detection work can keep recursive implementation
topology from causing repeated traversal. Those techniques remain replaceable realization.

Earlier publication and reuse work may also provide useful V1 machinery. Its existing algorithms remain implementation
choices and may be improved or replaced.

Compiler cost is therefore part of realization quality. Rich Contract semantics must not cause already-established
meaning to be reconstructed without need. Valid host-program knowledge must also not be reacquired merely because
another compiler consumer needs it.

The exact V1 reuse architecture remains open.

---

## 17. Optimization Boundary

Optimization belongs entirely to realization.

Kontrakt does not prescribe the algorithm chosen by the user implementation. A lawful algorithm does not become a
Kontrakt algorithm merely because the compiler inspects it.

Optimization acts only inside a realization region whose legality has already been established. Intermediate material
does not have to keep authored object topology when that topology carries no required observable meaning.

Kontrakt-owned wrappers and carriers are the strongest representation targets because Kontrakt owns their physical form.

Inside a verified Operation-local region, a wrapper or adapter may also disappear when it adds no required behavior and
its removal cannot be observed outside that region.

A user-local object remains a candidate only when non-observability and non-escape follow cheaply from already-produced
analysis and bounded local checks. Kontrakt does not perform expensive whole-program analysis solely to remove an
arbitrary user allocation.

If the required proof is unavailable or inconclusive, the user representation is preserved. Ordinary JVM optimization
may still remove that cost later.

Kontrakt does not redesign arbitrary user object models.

For example, a temporary carrier may disappear when its identity is not observable and the same required value reaches
the same consumer. The calculation remains the user's realization while its intermediate physical form changes.

The logical Contract pipeline also does not require a separate runtime object or call for every logical judgment.
Physical work may be combined when the same judgments and results are preserved.

Kontrakt must establish realization legality before using that knowledge for optimization.

No particular optimization technique is fixed by this ADR. Kontrakt may replace its optimization machinery when the
replacement preserves the required result.

The exact V1 optimization set remains open.

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

A user may write an object-oriented realization while Kontrakt emits a simpler execution form inside an admitted closed
region. The emitted program does not have to preserve allocation or reference topology that carries no required
observable meaning.

Current design work on primitive slabbing and mechanically sympathetic layout may be used for Kontrakt-owned execution
material when their preconditions are proven. That internal representation does not require Kontrakt to flatten
arbitrary user objects in the JVM-facing product.

Kontrakt should not duplicate expensive generic optimization that HotSpot, Graal, or another JVM optimizer can perform
without Contract-specific knowledge. A realization transform is most valuable when Contract knowledge exposes a
simplification or when already-required verification makes representation overhead cheap to remove.

Kontrakt does not replace the JVM optimizer. Ordinary lower-level optimization remains the JVM's responsibility.

The exact JVM emission strategy remains open.

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

Verification is not the endpoint. After a realization is accepted, V1 must use the proven knowledge available to it to
remove avoidable realization cost before handing execution to the JVM. The exact transformation set remains open and may
evolve without changing Contract meaning.

This does not require V1 to duplicate generic JVM analyses. A transform may be skipped when its legality or
profitability would require expensive analysis that is not otherwise justified by realization verification.

V1 must therefore establish stable boundaries between produced compiler material and the subsystems that consume it. A
consumer must be able to reuse a valid earlier result without changing the meaning of the producer.

V1 must also keep semantic identity separate from temporary storage location. Otherwise reuse across compiler
generations would require semantic rewriting.

These requirements prepare V2 without fixing the V2 implementation.

The current V2 plan includes incremental dependency tracking and persistent reuse. Those mechanisms may be implemented
through a query system, but this ADR does not require query architecture.

What V1 must preserve is the information needed for later incremental reuse: what result was produced, what inputs
determined it, and whether a later result is still the same for the consumer that needs it.

V2 may replace the current incremental design if another technique provides the same or stronger guarantees without
changing Contract meaning.

---

## 21. Architecture Review Before Acceptance

This section records architecture that must be reviewed before this ADR becomes Accepted.

It does not make the candidate stage names or the current optimization techniques part of Contract meaning. The purpose
is to ensure that V1 has a real compiler structure capable of supporting the current Contract model and the stronger V2
compiler without another architectural rewrite.

The review must consider the whole Contract compiler. Realization optimization is only one consumer. Establishment,
verification, diagnostics, generated tests, and backend realization must fit the same direction of material flow without
becoming an accidental authority chain.

### 21.1. Candidate Material Flow

The architecture should be evaluated against a shape close to the following.

```text
                         CONTRACT AUTHORITY

      IDL / selected external Contract evidence
                         │
                         ▼
             Resolution and Establishment
                         │
                         ▼
              Established Contract World
                         │
           ┌─────────────┼─────────────┐
           │             │             │
           ▼             ▼             ▼
       Verifier      Diagnostics    Test Synthesis
           │             │          / Coverage
           │             │             │
           └───────┬─────┴─────┬───────┘
                   │           │
                   ▼           ▼
             Shared Derived   Reference
                Knowledge     Judgment

════════════════════════════════════════════════════
                         REALIZATION

User implementation
        │
        ▼
Realization Acquisition
        │
        ▼
Published Realization Knowledge
        │
        ▼
Core Realization Verification
        │
        ├── illegal or unsupported → compile refusal
        │
        ▼
Verified Realization
        │
        ├───────────────┐
        │               ▼
        │        Shared Derived Knowledge
        │               │
        ▼               │
Contract-Aware Execution Material
        │◄──────────────┘
        ▼
Optimization
        │
        ▼
Optimized Realization Material
        │
        ▼
JVM Realization Lowering
        │
        ▼
JVM-facing Product
```

The diagram is a candidate architecture rather than a fixed IR taxonomy.

The important point is the direction. Contract authority is established before product subsystems consume it. User
realization is inspected separately. Verification of that realization does not rewrite the Contract that constrains it.

The physical compiler may combine stages when a separate materialization adds no value. It may also materialize a stage
lazily. The logical boundary must remain visible even when two stages share storage or one stage is produced directly
from another.

### 21.2. Establishment Must Remain the Upstream Authority Checkpoint

The current one-dimensional Contract work makes Establishment central to compiler architecture.

Each Contract authority establishes its own meaning. The compiler must not replace those authority-specific results with
one generic `EstablishedMaterial` object whose shape becomes a second semantic model.

Downstream subsystems should consume established material through exact identity and declared relations. They must not
reopen authored source and establish the same meaning again for their own purpose.

Definition, occurrence, applicable context, and semantic dependency must remain distinguishable because different
compiler products use them differently. A verifier may need the definition and its applicable world. Diagnostic
projection may also need source provenance. Runtime realization may only need compact execution material.

This separation is also important for V2. A source-location change should not invalidate an unchanged semantic result
merely because diagnostic provenance changed. Semantic material and source projection therefore need independent
physical evolution even when they remain related.

### 21.3. One-Dimensional Contract Authorities Need Distinct Material

The compiler architecture must respect the ownership already established by the one-dimensional Contract ADRs.

`Established Contract World` must not mean that every Contract is flattened into one universal node shape. Each owning
authority keeps the material needed to express its own judgment. Shared compiler infrastructure may index or relate that
material, but the index does not become a new authority.

For architecture review, the current Contract surface can be grouped by the kind of compiler relation it creates.

| Contract area                                 | Architecture that must remain possible                                                                                                               |
|-----------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| Input, Admission, Canonicalization, Lowering  | preserve the exact inbound boundary and the lawful formation of core material without reopening external representation later                        |
| Fact and Invariant                            | expose pure factual coordinates and exact judgment dependency without requiring runtime object topology to survive                                   |
| State and Transition                          | preserve legal movement separately from ordinary control-flow structure                                                                              |
| Policy, Governance, Version, Budget, Capacity | provide applicable machine context without turning compiler scheduling or storage policy into Contract meaning                                       |
| Failure, Publication, Output                  | preserve declared stop and outward-claim meaning through optimization and backend lowering                                                           |
| Diagnostic Evidence and Retention             | keep explanation tied to its owning judgment while allowing cold provenance and presentation material to remain outside hot execution representation |

These groups are only compiler-review groupings. They do not create Contract hierarchy or composition.

The architecture should let a consumer ask for the exact material it needs without forcing every consumer to load or
copy every Contract coordinate. This becomes more important as the Contract surface grows.

V2 should be able to reuse unchanged authority-specific material independently. A change in Diagnostic presentation
should not require rebuilding an unchanged Fact definition, while a changed Policy World should invalidate products
whose applicability actually depends on that world.

### 21.4. Product Subsystems Must Share the Contract World Without Forming an Authority Chain

Kontrakt produces more than executable code.

The verifier checks declared obligations. Test synthesis derives concrete verification products from those same
obligations. Diagnostics explain compiler and Contract results. The backend produces an executable realization. None of
these products should become the semantic input of another merely because the current implementation happens to run them
in that order.

The architecture should allow this relation:

```text
Established Contract World
        ├── Verifier
        ├── Reference Judgment
        ├── PBT / Fixture / Unit-Test Synthesis
        ├── Contract Coverage
        ├── Compiler Diagnostic Projection
        ├── Diagnostic Evidence Realization Planning
        ├── Publication / Output Projection
        └── Realization Planning
```

Shared analysis may sit between the established material and these consumers when several of them need the same derived
knowledge.

The verifier must not become the authority from which PBT learns Contract meaning. PBT must not become the oracle that
defines backend correctness. Diagnostics must not rewrite semantic material to make explanation easier.

A Reference Judgment path should remain deliberately simple enough to serve as an independent oracle for generated
gates, backend products, PBT, and selected optimizations. Sharing all of its internal logic with the optimized path
would weaken differential checking.

### 21.5. Diagnostics Need Two Separate Compiler Relations

Compiler diagnostics and Contract Diagnostic Evidence are different products.

Compiler diagnostics explain why compilation, verification, optimization, or backend realization succeeded or failed.
Contract Diagnostic Evidence is declared Contract material associated with a judgment and its retention law.

The realization architecture must support both without merging them.

Diagnostic richness should not force provenance strings or explanation objects into every hot semantic record. Stable
semantic material should be able to remain compact while diagnostic projection reaches related provenance when an
explanation is requested.

V2 should be able to refresh source projection or diagnostic rendering without invalidating unchanged Contract meaning.
This requires a structure where semantic identity, provenance, and presentation are related but not physically
inseparable.

### 21.6. Generated PBT, Fixtures, and Unit Tests Are a Product Subsystem

Generated tests must be derived from declared Contract obligations rather than from incidental implementation behavior.

The architecture must give test synthesis access to the same established material and reusable analyses used by
verification. Generated cases should retain identity linking them to the exact obligation they exercise.

V1 needs a deterministic product boundary for basic valid, invalid, and boundary cases. State-machine legality and
Failure attribution must also be representable where their owning Contracts require them.

V2 should be able to reuse unchanged test plans, perform stronger constraint-directed generation, and shrink failing
cases using semantic knowledge. Persistent failing cases may be stored as compiler products, but they do not become
Contract authority.

Compiler QA remains separate. Fuzzing the compiler and generating user Contract tests are different activities even when
they reuse generators or reference judgments.

### 21.7. Existing Optimization Work Is the V1 Baseline, Not the Final Architecture

Earlier Kontrakt optimization work remains valuable even though much of it predates the V2 incremental plan.

The current acquisition, identity, publication, cache, and mechanically sympathetic storage work should be treated as
the V1 baseline. ADR-0070 must not force those exact mechanisms to remain forever, but the new architecture must
preserve the places where they can be applied.

The following evolution should be reviewed.

| Existing direction                                 | V1 use                                                                                               | V2-aware extension to evaluate                                                                                   |
|----------------------------------------------------|------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| bounded class/type acquisition and cycle detection | avoid repeated host traversal and terminate JVM topology analysis                                    | dependency-aware reuse with fine-grained invalidation of changed realization knowledge                           |
| stable identity and exact collision verification   | recognize already-produced semantic or realization material without using object address as identity | persistent fingerprints, content-addressed artifacts, and cross-session reuse without changing semantic identity |
| build, verify, seal, publish                       | keep incomplete compiler material invisible to consumers                                             | concurrent immutable generations with safe reader pinning and later reclamation                                  |
| L1/L2-style bounded reuse                          | remove repeated work inside one compiler run                                                         | separate local, persistent, and optional remote reuse with explicit invalidation and cache non-authority         |
| direct-to-final materialization                    | avoid temporary object chains and repeated copying                                                   | lazy or partial materialization when a summary or unchanged result is enough                                     |
| primitive slabs and dense tables                   | reduce pointer chasing and GC pressure on hot compiler material                                      | adaptive hot/cold layout, sparse materialization, and representation selected from measured access patterns      |
| worker-local ownership and deterministic merge     | reduce contention while preserving deterministic results                                             | parallel incremental evaluation over independent result regions                                                  |
| canonical compact references                       | make hot access cheap without changing semantic identity                                             | summary indexes and thin whole-machine analysis that avoid loading complete units without need                   |

The V2 extension is not a requirement to implement a particular database, cache, or storage engine. It states what
future techniques the V1 boundaries should be capable of receiving.

The review should compare this evolution against several production compiler patterns without importing their vocabulary
into Contract semantics. Useful references include a verified canonical checkpoint before optimization, reusable
analysis with explicit invalidation, summary-driven whole-program work, incremental early cutoff, independent
translation validation, and JVM-friendly specialization. The current implementations of those ideas may resemble Swift,
LLVM/MLIR, rustc-style incremental computation, ThinLTO, Alive2-style validation, or Graal-oriented lowering. They
remain engineering references rather than required Kontrakt mechanisms.

### 21.8. Contract-Aware Optimization Must Expand Beyond the Old Storage Optimizations

The older work mainly makes compiler material cheaper to acquire, store, and reuse. A verified closed realization gives
Kontrakt a second optimization opportunity.

The compiler should evaluate higher-level transformations while it still knows Contract meaning. Candidate families
include static discharge of judgments whose result is already known and dependency slicing that removes material no
applicable judgment can observe.

Specialization should also be evaluated where the selected Contract world removes runtime choice. A known Policy World
or State-machine surface may permit a smaller execution path without changing the user algorithm.

Representation overhead may be removed inside the closed region when the required behavior remains observable in the
same way. Kontrakt-owned carriers are the strongest candidates. Verified Operation-local wrappers and adapters may also
be reduced when they add no required behavior.

A user-local object remains only a conditional candidate. V1 should use already-produced verification knowledge and
bounded local analysis rather than start an expensive generic escape-analysis effort solely to remove that object.

Physical Contract machinery may also be combined when separate runtime calls do not carry separate required meaning.
Such fusion must preserve judgment result and required Failure or Diagnostic relations.

These candidates extend the existing mechanical-sympathy work rather than replacing it. The older work reduces the cost
of compiler and runtime representation. The newer work uses Contract knowledge to reduce how much realization must exist
in the first place.

### 21.9. V2 Optimization Should Add Incremental and Summary-Driven Cost Reduction

V2 should not merely add more local optimization passes.

A major V2 optimization target is avoiding compilation work that no longer needs to happen.

Stable producer/result boundaries should allow the compiler to record which earlier results influenced a later result. A
changed input can then invalidate only the dependent work. Recalculation should be able to stop when a recomputed
semantic result is unchanged.

Whole-Machine work should also be able to begin from compact summaries. Full materialization of every Contract unit or
realization body should not be required when a summary can prove that the unit is irrelevant to the current decision.

This direction should be evaluated together with persistent result reuse. A persistent cache is useful only when
identity, schema, and invalidation remain separate from Contract authority.

V2 may also add stronger cost models and profile-guided decisions. Runtime profile data may influence profitability but
must not make an otherwise illegal transform legal.

### 21.10. Analysis and Transformation Need an Explicit Relationship

Several subsystems may need the same dependency or applicability knowledge. The compiler should calculate that knowledge
once when the same result is valid for all of them.

Analysis must remain distinguishable from transformation. A transformation changes realization material. Analysis
describes material it consumed.

When a transformation changes the facts on which an analysis depended, later consumers must not continue using the old
analysis as if it were valid.

The exact mechanism remains open. V1 may use a small analysis cache. V2 may use finer invalidation. The architecture
should make both possible without changing Contract meaning.

Optimization should also separate legality from profitability. Contract preservation decides whether a transformation
may occur. A cost model decides whether the legal transformation is worth applying.

### 21.11. Important Transforms Need Independent Preservation Checking

The optimized path should not be the only implementation that declares its own transformation correct.

V1 should provide verification hooks around important transformations. A simple reference path or structural verifier
may be enough for some classes of change.

V2 should be able to attach stronger translation validation where the risk justifies it. The validation method may
differ by transform because layout rewriting and judgment fusion preserve different relations.

This does not make mathematical proof part of Contract authority. It is independent checking of realization.

### 21.12. JVM Lowering Must Preserve High-Level Knowledge Long Enough to Use It

Kontrakt should not lower to generic JVM structure so early that Contract-specific optimization becomes impossible.

A Contract-aware execution form should retain enough information to know which values come from established Facts and
which judgments can observe them. It should also preserve the relations needed for Failure and Diagnostic correctness.

Only after those high-level decisions are complete should JVM lowering choose the physical form needed by the target
backend.

That lowering should aim to produce material friendly to HotSpot or Graal. Avoidable reflection and megamorphic dispatch
should not be reintroduced after Kontrakt has already resolved the relevant relation.

The target backend should expose its actual capabilities to planning. A transform that requires a capability the backend
cannot preserve must be rejected or left unapplied.

### 21.13. Logical Stages Must Not Require Full Physical Materialization

A real architecture needs named material boundaries, but each boundary does not need a new object graph.

A verified realization may share backing storage with acquired realization knowledge and add only a verified result or
index. A later execution form may materialize only the regions needed by the backend.

This freedom matters because Kontrakt already has a large Contract surface. Creating a full object-heavy copy at every
logical stage would make the architecture itself the source of compile-time cost.

The architecture should therefore define what each stage knows and who may consume it before deciding whether the stage
requires independent storage.

### 21.14. Candidate V1 Structural Requirements to Review

Before this ADR becomes Accepted, the V1 architecture should be checked for the following capabilities.

```text
Contract authority can publish stable established material.

User realization can be acquired once and reused by later compiler work.

Core-closure verification produces an explicit accepted or refused result.

Verifier, diagnostics, test synthesis, and backend can consume the same Contract world without forming an authority chain.

Shared derived knowledge has an owner and a validity boundary.

Verified realization is distinguishable from later transformed realization.

High-level Contract-aware optimization can occur before JVM-specific lowering.

Semantic identity remains separate from physical location and reuse keys.

Published material can use dense or mechanically sympathetic physical representation without changing its meaning.

Important transforms can be independently checked.

Whole-Machine compilation has a summary seam even if V1 performs eager compilation initially.

Compiler work and memory cost can be attributed to the stage that caused them.

An expensive produced result has enough identity and input relation for V2 to add incremental reuse later.
```

These are architecture capabilities rather than commitments to one current implementation technique.

### 21.15. Candidate V2 Extensions to Keep Open

V2 should be able to add incremental invalidation without changing the Contract model established in V1.

It should also be able to retain compiler generations long enough for IDE and build consumers to read a coherent
published world while a new candidate is being produced.

Persistent reuse should remain possible for semantic products, summaries, test plans, and selected analysis results.
Different products may need different storage policies rather than one universal cache.

Whole-Machine compilation should be able to use summaries before loading full units. Backend work should remain
independently parallelizable after global decisions are known.

Optimization may gain stronger cost models, broader specialization, or profile-guided profitability. None of those
extensions may change the legality rule established by Contract meaning.

Diagnostic explanation may become on-demand and incrementally refreshed. PBT plans may also be reused when their exact
Contract obligations remain unchanged.

The specific V2 engine remains realization and can be replaced.

### 21.16. Questions That Must Be Closed Before Acceptance

The exact material stages still need review. The first question is whether `Published Realization Knowledge`,
`Verified Realization`, and `Contract-Aware Execution Material` need independent physical forms or only independent
logical boundaries.

The compiler also needs an ownership decision for shared analysis. That decision must explain how a result becomes
valid, how later transformation invalidates it, and how independent verification avoids circular reuse.

The unit of realization verification remains open. Operation-local analysis may be sufficient for some checks, while
Core or Whole-Machine relations may require wider summaries.

The Whole-Machine summary boundary also needs a concrete V1 decision. V1 may build summaries eagerly, but the format
should not prevent V2 from using them for incremental linking and lazy materialization.

The supported execution envelope needs concrete V1 enforcement rules. The boundary decision is already fixed: external
frameworks may surround an Interaction but may not silently participate inside its governed realization. Review must
decide which host mutation mechanisms are supported and how a detected realization change invalidates the affected
product.

The optimizer needs a first V1 legality set. Each accepted transform should state what relation it preserves and what
independent check can detect a bad rewrite.

The verifier, Reference Judgment, PBT, diagnostics, and backend need a final producer-consumer map. That map should
identify which material each subsystem consumes without giving one product authority over another.

Compiler resource ownership also needs to be attached to the architecture. The richer compiler must be able to identify
where memory and semantic work are spent so optimization of user execution does not make compilation itself unbounded.

These questions should be closed from Contract semantics outward. The architecture must not change the Contract model
merely to make one current compiler technique easier to implement.

---

## 22. Open in This ADR

The exact V1 Core Realization Closure verification model remains open. It must define what the compiler can establish
about user code before that code is accepted as a Kontrakt Core realization.

The treatment of implementation that cannot be inspected precisely also remains open. Reflection and native execution
are two examples that need an explicit V1 rule. Any supported rule must preserve the compile-time boundary in Section 11
rather than silently falling back to runtime interception.

The host/framework boundary is no longer semantically open. External framework work belongs outside the Interaction
boundary, and hidden participation in governed realization is not admitted. What remains open is the V1 enforcement
mechanism for supported runtime mutation and intervention.

The exact host-facing API shape remains open where existing User API and Adapter decisions do not already fix it. Any
such surface must keep framework composition outside the governed realization and must not spread verifier or optimizer
knowledge into ordinary Core implementation.

The exact compiler material used to analyze realization remains open. This includes the representation used for user
code and the form of reusable analysis results.

The exact reuse mechanism remains open. Existing reuse and identity machinery may be reused, but no current technique is
required by this ADR.

The exact V1 optimization set remains open. Each accepted transformation needs a defined legality condition and a way to
verify that its result preserves the required meaning.

The exact JVM emission path remains open.

These open questions must not weaken the decisions already made here.

Realization remains non-authoritative, and external technology still ends before the Core. User-System Realization must
preserve Core Realization Closure.

Compiler techniques remain replaceable. Optimization may proceed only when the required meaning is preserved.

---

## 23. Consequences

The Core boundary now applies to the actual user realization rather than stopping at the Operation signature.

Kontrakt must inspect enough implementation to detect factual input that bypasses the declared boundary. This increases
compiler work, but it makes Fact authority real inside the Core rather than merely descriptive at its edges.

External framework compatibility becomes explicit. A host framework may surround a Kontrakt Interaction and keep its own
runtime behavior outside the governed realization. A usage pattern that injects hidden framework behavior into a
governed Operation is intentionally rejected.

Kontrakt defends the supported execution envelope but does not claim authority over arbitrary host mutation that
bypasses that envelope. A detected violation fails closed. Execution after an unsupported bypass is outside the Kontrakt
guarantee.

Core closure is therefore not enforced by surrounding ordinary user calls with an implicit proxy or monitoring layer.

The Contract remains explicit in the Kontrakt Contract surface rather than being scattered through proof structures
inside ordinary implementation. This keeps the user system independent from the verifier and optimizer chosen by
Kontrakt.

The same compiler knowledge must be reused for optimization after legality has been established when it proves a safe
simplification. Kontrakt therefore does not stop at verification and restore the authored realization unchanged by
default.

A closed governed region gives Kontrakt a stable place to remove representation overhead. Kontrakt-owned material and
verified Operation-local wrappers are the primary targets. User-local objects remain optional candidates when the
required proof is cheap.

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