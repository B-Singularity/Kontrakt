# ADR-0070: Realization Axis, Core Realization Closure, and JVM-Ahead Optimization

## Status

Proposed

## Date

2026-09-03

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`
- `docs/todo/v2/kontrakt-v2-reference-architecture-and-v1-foundations.md`
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

## 6. Adapter and Core Boundary

External technology ends before the Core.

A database connection, network client, framework context, live clock, or another external capability belongs outside
Core realization. Its information may enter only after the applicable Adapter and inbound Contract processing have
formed lawful Core material.

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

## 7. Core Realization Closure

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

## 8. Compile-Time Realization Verification

Kontrakt must inspect enough User-System Realization to establish Core Realization Closure before accepting the
executable realization.

The analysis may follow a helper because the helper influences the Operation result. A deeper call cannot be treated as
safe merely because its caller is local.

When Kontrakt establishes that outside information affects Core computation through an illegal path, compilation fails.

When the supported analysis cannot establish closure, Kontrakt must not silently assume that closure exists.

This verification is a compile-time responsibility. Runtime execution should not depend on a general monitor that
discovers ordinary Core-closure violations after the realization has already been accepted.

Runtime Contract judgments that require runtime values remain runtime judgments. This ADR does not move those judgments
into compile time.

The exact V1 proof model remains open.

---

## 9. Realization Topology and Cycles

Implementation topology may contain cycles even though Contract semantic cycles are forbidden.

A recursive method is realization structure. It does not create recursive Contract authority.

Kontrakt must detect realization cycles so analysis terminates and the same region is not traversed without bound.

A realization cycle may be legal when the relevant region remains inside Core Realization Closure.

A Contract semantic cycle remains illegal under the Contract laws that own semantic dependency.

Earlier cycle-detection work may be reused to implement this analysis. The algorithm is realization and may be replaced.

The exact recursive-region analysis remains open.

---

## 10. Contract Material and Realization Material

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

## 11. Producer and Consumer Direction

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

## 12. Replaceable Compiler Realization

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
techniques as they become practical. The declared Contract meaning must remain stable while the optimization machinery
evolves.

---

## 13. Reuse and Compiler Cost

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

## 14. Optimization Boundary

Optimization belongs entirely to realization.

Kontrakt may transform a legal User-System Realization when the transformation preserves the established Contract
meaning and every host behavior that the selected realization must preserve.

The authored object structure is not protected merely because it appeared in source code.

For example, a temporary object may disappear when its identity cannot be observed and the same required values reach
the same lawful consumers. This changes execution structure without changing Contract meaning.

The logical Contract pipeline also does not require a separate runtime object or call for every logical judgment.
Physical work may be combined when the same judgments and results are preserved.

Kontrakt must decide legality before deciding whether an optimization is profitable.

No particular optimization algorithm is fixed by this ADR. The optimizer may be replaced when the replacement preserves
the required result.

The exact V1 optimization set remains open.

---

## 15. JVM-Ahead Optimization

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

The exact JVM emission strategy remains open.

---

## 16. Determinism and Meaning Preservation

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

## 17. V1 Foundation and V2 Evolution

V1 owns Core realization verification and Contract-aware optimization before JVM emission.

Verification is not the endpoint. After a realization is accepted, V1 must use the proven knowledge available to it to
remove avoidable realization cost before handing execution to the JVM. The exact transformation set remains open and may
evolve without changing Contract meaning.

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

## 18. Open in This ADR

The exact V1 Core Realization Closure proof model remains open. It must define what the compiler can prove about user
code before that code is accepted as a Kontrakt Core realization.

The treatment of implementation that cannot be inspected precisely also remains open. Reflection and native execution
are two examples that need an explicit V1 rule.

The observable host-behavior boundary remains open. Optimization needs a clear decision about which behavior outside
Contract meaning must still be preserved because the selected realization exposes it to the user system.

The exact compiler material used to analyze realization remains open. This includes the representation used for user
code and the form of reusable analysis results.

The exact reuse mechanism remains open. Existing frozen, cache, interning, and identity techniques are candidates, not
requirements.

The exact V1 optimization set remains open. Each accepted transformation needs a defined legality condition and a way to
verify that its result preserves the required meaning.

The exact JVM emission path remains open.

These open questions must not weaken the decisions already made here.

Realization remains non-authoritative, and external technology still ends before the Core. User-System Realization must
preserve Core Realization Closure.

Compiler techniques remain replaceable. Optimization may proceed only when the required meaning is preserved.

---

## 19. Consequences

The Core boundary now applies to the actual user realization rather than stopping at the Operation signature.

Kontrakt must inspect enough implementation to detect factual input that bypasses the declared boundary. This increases
compiler work, but it makes Fact authority real inside the Core rather than merely descriptive at its edges.

The same compiler knowledge must be reused for optimization after legality has been established when it proves a safe
simplification. Kontrakt therefore does not stop at verification and restore the authored realization unchanged by
default.

The optimizer may simplify execution before JVM emission without making its current technique part of Contract meaning.

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