# ADR-0063: Contract Establishment, Occurrence, Applicability, and Semantic Dependency

## Status

Proposed

## Date

2026-08-30

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-v2-reference-architecture-and-v1-foundations-en.md`
- ADR-0046: Interface Contract Frontend
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0049: Flow Contract Processing — Fact, Invariant, and Publication
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0051: Budget Contract
- ADR-0052: Capacity Contract
- ADR-0053: Version Contract
- ADR-0055: Policy Contract
- ADR-0056: Governance Contract
- ADR-0057: Failure Contract
- ADR-0058: Publication Contract
- ADR-0059: Output Presentation Contract
- ADR-0060: Diagnostic Evidence and Retention Contract

---

## 1. Context

Kontrakt has many authorities, but they do not work in isolation.

One authority may establish meaning that another authority later needs. The compiler also derives several products from
the same Contract World. Those products must share the original meaning without rebuilding it independently.

The missing common law is simple:

> When has Contract material become authoritative, and when may another responsibility rely on it?

The current design already depends on this distinction. A lowered candidate does not become Fact until the required
judgments succeed. The same principle must work for every other authority without being redefined in each ADR.

V2 makes this requirement stricter. Several compiler products must consume the same semantic source without rebuilding
its authority.

```text
Canonical Contract World
    ├── Verification
    ├── Test Synthesis
    ├── Diagnostics
    ├── Optimization
    ├── Linking
    └── Realization
```

None of these products becomes the source of the Contract meaning it consumes.

This ADR defines the common establishment model that supports that architecture.

---

## 2. Problem

Material can exist before it is authoritative.

Parsing a declaration does not establish its Contract meaning. Computing a candidate does not establish the result that
candidate seeks to become. Keeping old material in memory does not make it valid for a new judgment.

These distinctions become dangerous when one authority consumes another authority's result.

```text
Authority A
    establishes M

Authority B
    uses M
    establishes N
```

The compiler needs to know that `M` is authoritative. It also needs to know whether `M` is valid basis for B. None of
that may be inferred from the physical object that happens to carry `M`.

The same problem appears during incremental and parallel compilation. A semantic result must not change because a cache
was warm, a worker finished first, or a physical address changed.

Kontrakt therefore needs one model that preserves authority while leaving compiler realization replaceable.

---

## 3. Decision Drivers

The model must make Contract authority explicit before another responsibility can rely on it.

The same semantic basis must always establish the same meaning. Compiler execution may change how work is performed, but
it cannot change the result required by the Contract.

Semantic identity must survive changes to physical representation. Source location and compiler generation also remain
separate from that identity.

The compiler core must be able to represent these relations without depending on host-object identity. The design must
therefore admit direct low-level references while leaving the exact storage form open.

Finally, this ADR must stay below the individual Contracts. It defines common authority relations without replacing
source-specific result shapes.

---

# 4. Established Material

## 4.1. Establishment

**Establishment** is the semantic boundary at which material receives the authority owned by its source responsibility.

```text
required basis
    ↓
owning law
    ↓
Establishment
    ↓
Established Material
```

The owning law decides what must be complete before that boundary is crossed.

Physical completion is not enough. A realization event may carry the result, but it does not grant the authority.

---

## 4.2. Established Material

**Established Material** is semantic material that has received the authority owned by its source.

This is a semantic category. It is not a common compiler container.

A Fact still has Fact meaning after establishment. A Failure still has Failure meaning. Governance keeps ownership of
its Binding.

The common term exists only so that later responsibilities can require material that already carries source authority.

---

## 4.3. Definition Material

Authored source is not final authority.

A source declaration becomes **Established Definition Material** only after the compiler has resolved the meaning
required by that definition and has accepted it as Kontrakt-owned semantic material.

```text
authored source
    ↓
semantic resolution
    ↓
Established Definition Material
```

This material is what the Canonical Contract World represents.

Publishing a compiler representation does not create that meaning. Publication only makes a completed compiler view
available to consumers.

---

## 4.4. Occurrence Material

An Established Definition may later be applied to a concrete semantic situation.

The result of that application is **Established Occurrence Material** when the owning authority completes the required
judgment.

```text
Established Definition
    ↓
semantic application
    ↓
Established Occurrence Material
```

Definition material and occurrence material are different products.

The first states authoritative Contract meaning. The second records meaning established by one application of that
definition.

---

## 4.5. Contract Occurrence

A **Contract Occurrence** is a distinct semantic application of an Established Definition.

The authority itself decides which semantic coordinates distinguish one application from another.

A compiler query is not an occurrence. A runtime call is not enough to define one either.

Two occurrences may establish equal result material and still remain distinct where later meaning needs that
distinction.

---

## 4.6. Judgment

A **Judgment** is the authoritative evaluation owned by a Contract or the State-Machine axis.

Judgment and Establishment are not identical.

One result may depend on several judgments before its own authority can be established. Fact is the representative case:
Invariant and State-Machine judgments may be required, while Fact remains the owner of factual meaning.

---

## 4.7. Candidate Material

A candidate is material that may become authoritative later.

Its existence proves only that the compiler or machine has enough material to continue processing.

```text
candidate
    !=
Established Material
```

A candidate becomes authoritative only at the establishment boundary owned by the target meaning.

The same rule excludes optimistic establishment. Lack of a known contradiction does not grant authority.

---

## 4.8. Source Authority

Established Material keeps its source authority after another responsibility reads it.

```text
Authority A
    establishes M

Authority B
    uses M
    establishes N
```

B owns `N`. A continues to own `M`.

A consumer may draw a new conclusion under its own law. It may not rewrite the meaning that the source established.

---

# 5. Deterministic Establishment

## 5.1. Semantic Determinism

Establishment depends only on semantic material that the owning law treats as relevant.

When that material is equal under its Contract meaning, the established result must also be equal.

```text
same semantic basis
    ↓
same established meaning
```

This rule is stronger than reproducible execution. It defines what the Contract result is allowed to depend on.

---

## 5.2. Hidden Compiler State

Compiler state that is absent from the semantic basis cannot change the established meaning.

A cache hit and a recomputation must therefore agree on the Contract result.

The same rule applies when work is scheduled differently.

If some state can legitimately change the result, that state must first appear as semantic material under an authority
that owns its meaning.

---

## 5.3. Physical Order

The order in which compiler work completes has no semantic authority.

```text
physical completion order
    !=
semantic establishment order
```

If ordering changes Contract meaning, the owning authority must declare that ordering relation.

This keeps parallel execution compatible with deterministic semantics.

---

## 5.4. Composition

Composition follows the same determinism law.

If the same source meanings are applicable under the same composition law, the composed meaning must be the same.

The compiler may discover or compute those sources in any order.

---

# 6. Identity and Reference

## 6.1. Semantic Identity

Established Definition Material needs exact semantic identity when another compiler responsibility must refer to it.

The identity law belongs to the meaning being identified.

No universal encoding is prescribed here. Incidental implementation state still cannot become part of semantic identity.

---

## 6.2. Identity Boundaries

The compiler must keep semantic identity separate from other coordinates.

| Coordinate             | What it answers                            |
|------------------------|--------------------------------------------|
| Semantic identity      | Which Contract meaning is this?            |
| Occurrence distinction | Which semantic application is this?        |
| Source provenance      | Where did the authored material come from? |
| Fingerprint            | Did relevant compiler material change?     |
| Compiler generation    | Which published compiler view contains it? |
| Local address          | Where is it stored in that view?           |

Changing one coordinate does not automatically change another.

A source edit that only moves a declaration may change provenance while preserving semantic identity.

---

## 6.3. Semantic Reference

A later compiler responsibility needs a precise way to refer to source meaning.

That reference must recover the intended semantic identity without relying on host-object identity.

The physical form of the reference is not fixed here. Different representations may use different compact values while
preserving the same semantic relation.

---

# 7. Applicability

## 7.1. Applicable Context

Established Material is authoritative for its source meaning, but it is not automatically valid basis everywhere.

The **Applicable Context** of an occurrence is the Contract material needed to interpret that occurrence correctly.

Each authority defines only the context it actually needs.

There is no universal context object.

---

## 7.2. Applicability

**Applicability** answers whether particular Established Material may be used by a dependent semantic application.

```text
Established Material
    ↓
applicability under the dependent law
    ↓
valid basis
```

Applicability belongs to the relation between source material and the dependent responsibility.

It is not a permanent flag stored on the source result.

---

## 7.3. Deterministic Applicability

Applicability follows semantic meaning, not compiler reachability.

If the same source material is considered under the same relevant context, the applicability result must be the same.

Whether the source is cached or recently computed is irrelevant.

---

## 7.4. Later Applicability

A later establishment may change what is valid for later use.

The earlier establishment remains unchanged.

```text
earlier meaning stays fixed

later meaning may affect later use
```

The Contract that owns succession decides when later material replaces or withdraws what was previously applicable.

No universal mutable `current` result is introduced.

---

# 8. Semantic Dependency

## 8.1. Dependency Meaning

A **Semantic Dependency** exists when one responsibility requires established meaning owned by another authority.

The dependency points to source meaning, not to its current physical encoding.

```text
consumer
    ↓ requires
source-owned meaning
```

This relation is part of Contract semantics.

A compiler query dependency may later be derived from it, but the two are not the same relation.

---

## 8.2. Complete Basis

The owning law decides what basis must be present before Establishment.

When several source meanings are required, the result receives no authority until that basis is complete.

```text
required:
    A + B

available:
    A

result:
    not established
```

A different complete alternative is allowed only when the owning law declares it.

Missing basis does not invent another result.

---

## 8.3. Composition Authority

Established local meanings do not automatically create a larger meaning.

A new authority must own the law that combines them.

```text
MA + MB
    ↓
composition law
    ↓
MC
```

The new result belongs to the composing authority.

The source results keep their original authority.

---

## 8.4. Shared Consumption

One source meaning may be consumed by several compiler products.

They can share the same source reference. They may also share compiler-derived analysis.

Neither form of reuse transfers Contract authority.

---

# 9. Integrity Boundaries

## 9.1. Observation

Observation does not establish Contract meaning.

When observed realization state matters to a Contract judgment, an authority must first give that observation the
semantic meaning required by the judgment.

```text
observation
    ↓
owned semantic qualification
    ↓
Established Material
```

No general Observation Contract is introduced.

---

## 9.2. Non-Establishment

If a result is not established, no replacement meaning appears automatically.

An unreached stage has no synthetic result unless an authority explicitly defines one.

Failure remains governed by Failure law.

---

## 9.3. Source Integrity

A later consumer must keep enough source relation to interpret material honestly.

Material taken from another occurrence may support a new judgment. It may not be presented as though it belonged to the
earlier occurrence.

This law prevents later reconstruction from changing occurrence-time meaning.

---

## 9.4. Retention

Retention controls whether material remains available after its original processing boundary.

Whether the material was established was already decided at its source boundary.

Removing retained data therefore does not undo earlier authority.

---

## 9.5. Outward Authority

Establishment does not grant permission to expose material outside the machine.

Publication owns outward authorization.

Output owns the outward result shape.

Internal use of Established Material does not bypass either boundary.

---

# 10. Canonical Contract World

## 10.1. Role

The Canonical Contract World is the compiler-owned semantic substrate built from authoritative Contract definitions.

It preserves the meaning and relations that later compiler products need.

Storage only preserves the semantic substrate; it does not create its authority.

```text
Contract authority
    ↓
Canonical Contract World
    ↓
compiler consumers
```

---

## 10.2. Definition Substrate

The Canonical Contract World represents established definition meaning.

It also preserves the semantic relations needed to use that meaning correctly.

Runtime occurrences are not pre-created as part of this substrate.

One stable definition may support many later occurrences.

---

## 10.3. Derived Compiler Knowledge

Shared analyses can derive compiler knowledge from the Canonical Contract World.

Those results remain compiler knowledge unless a Contract authority separately establishes their meaning.

For example, a reachability analysis can help several compiler products without becoming State authority.

---

## 10.4. Independent Consumers

Compiler products consume the same source substrate directly.

```text
Canonical Contract World
    ├── Verification
    ├── Test Synthesis
    ├── Diagnostics
    ├── Optimization
    └── Realization
```

No product becomes the semantic source for the next product merely because both need the same analysis.

---

# 11. Core Representation Requirements

## 11.1. Value-Based Representation

The semantic core must be representable without host-object identity.

A Kotlin or JVM object may provide a temporary API view. Its identity is not Contract identity.

This leaves the core free to use lower-level representations without changing semantics.

---

## 11.2. Direct Semantic Reference

A consumer must be able to reach the semantic material it needs through an exact compact reference.

The reference should not require traversal of a compiler-wide object graph.

This requirement applies to source authority as well as cross-authority relations.

---

## 11.3. Local Address

A compiler generation may assign a local address to semantic material for efficient access.

That address is physical.

```text
semantic identity
    !=
local address
```

Rebuilding the same semantic world may assign a different local address without creating different Contract meaning.

---

## 11.4. Source-Specific Shape

Each authority keeps the physical shape appropriate to its own semantic material.

No shared record is required for all Established Material.

The common law is expressed through identity and reference, not through a universal payload layout.

---

## 11.5. Deterministic Materialization

Physical ordering that becomes visible to another compiler responsibility must be derived deterministically.

Worker completion order cannot assign meaning-bearing references.

A later generation may use a different physical layout while preserving the same semantic relations.

---

# 12. Relation to Existing Authorities

Existing Contracts keep their own semantics.

| Authority           | Common establishment relation                                                                 |
|---------------------|-----------------------------------------------------------------------------------------------|
| Input               | Input establishes only boundary meaning owned by Input.                                       |
| Canonicalization    | Canonicalization establishes only its representative meaning.                                 |
| Lowering            | Lowering may produce candidate core material without granting Fact authority.                 |
| Fact                | Fact owns factual authority after its required basis succeeds.                                |
| Invariant           | Invariant owns the integrity judgment required by its definition.                             |
| State Machine       | State and Transition keep movement authority separate from Contract authority.                |
| Budget              | Budget judgments remain source-owned basis for later responsibilities.                        |
| Capacity            | Capacity judgments remain source-owned basis for later responsibilities.                      |
| Policy              | Policy keeps ownership of its Contract World meaning.                                         |
| Governance          | Governance may consume source meaning while owning only the Binding it establishes.           |
| Failure             | Failure keeps the meaning fixed at the occurrence where Failure law establishes it.           |
| Version             | Later Version meaning may change later applicability without reinterpreting earlier material. |
| Publication         | Publication owns outward authorization rather than source meaning.                            |
| Output              | Output owns the outward result shape rather than source meaning.                              |
| Diagnostic Evidence | Diagnostic may refer to source establishment while owning only Diagnostic meaning.            |

The detailed law of each authority remains in its own ADR.

---

# 13. Verification Requirements

The verifier must reject any semantic world in which authority can appear without the establishment law owned by its
source.

A dependent judgment is invalid when its required source meaning is unresolved or inapplicable.

Where the owning law requires complete basis, the verifier must reject partial authority.

Composition must preserve the identity of every source meaning it consumes.

A compiler representation is also invalid if backend shape becomes the only way to recover these semantic relations.

Determinism belongs to the same verification surface. Equivalent semantic inputs must not produce different meaning
because compiler execution changed.

---

# 14. V1 Foundation Requirements

V1 must implement this model in a form that V2 can extend without replacing the semantic foundation.

Resolved definitions need stable compiler-owned semantic identity. Source provenance must remain separate.

Semantic dependency and applicability need explicit representation seams. Hidden callback order is not sufficient.

Major semantic computations need explicit inputs and deterministic results.

Candidate compiler work must remain private until the compiler has a complete published view.

The canonical core must support direct low-level references without requiring object identity.

Shared analyses may serve several compiler products, but their compiler ownership must remain distinct from Contract
authority.

V1 QA must compare clean recomputation with supported alternate execution modes so that hidden nondeterminism is found
before V2 adds incrementality and broader parallelism.

---

# 15. V2 Consequences

The V2 query system may use semantic identity as part of stable compiler keys.

Semantic Dependency may guide query dependency. Query dependency still remains compiler infrastructure.

Fingerprints may decide whether a computed result can be reused. They do not replace semantic identity or Applicability.

Incremental compilation may stop propagation when recomputation yields unchanged semantic meaning. A clean build and an
incremental build must still converge on the same Contract result.

Parallel execution may change when work completes. It must not change which meaning is established.

Persistent caches may preserve compiler products across sessions. Cache loss may cost time, but it cannot remove
semantic truth.

Whole-Machine analysis may use compact summaries. A summary remains compiler material until an owning Contract judgment
establishes any higher-scope meaning derived from it.

The exact query engine and storage layout remain outside this ADR.

---

# 16. Rejected Directions

## 16.1. Universal Established-Material Object

Rejected because the common concept is semantic authority, not a shared host-language shape.

A common wrapper would make unrelated authorities depend on the same physical model.

---

## 16.2. Object Identity as Semantic Identity

Rejected because allocation belongs to realization.

Changing the JVM representation must not create new Contract meaning.

---

## 16.3. Compiler State as Authority

Rejected because cache state and query state describe compiler work.

They do not establish Contract meaning.

---

## 16.4. Observation as Authority

Rejected because observed realization state needs an owning semantic interpretation before a Contract can rely on it.

---

## 16.5. Automatic Composition

Rejected because local authority does not imply authority for a larger result.

The larger meaning needs its own law.

---

## 16.6. Universal Context or Lifetime

Rejected because each authority owns the context it needs.

Applicability already explains whether old material may be used later.

---

## 16.7. Physical Order as Semantic Order

Rejected because scheduling belongs to realization.

Semantic order exists only where an authority declares it.

---

# 17. Consequences

Kontrakt gains one common law for material that has acquired source authority.

That law gives Governance and other Contracts a direct way to depend on source-owned meaning without introducing another
authority in between.

The same model gives every compiler product a stable source substrate.

```text
source meaning
    ↓
shared semantic substrate
    ↓
independent compiler products
```

Determinism becomes part of semantic correctness rather than an optimization property.

The compiler can pursue compact low-level representation because Contract identity no longer depends on wrappers or
object graphs.

V2 can build incremental reuse and parallel evaluation on top of the same model without redefining Contract meaning.

The compiler must therefore preserve source identity and applicability explicitly. That extra discipline is intentional
because hidden reconstruction would make deterministic reuse impossible.