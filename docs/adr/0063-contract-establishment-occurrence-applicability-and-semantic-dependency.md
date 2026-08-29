# ADR-0063: Contract Establishment, Occurrence, Applicability, and Semantic Dependency

## Status

Proposed

## Date

2026-08-29

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-v2-reference-architecture-and-v1-foundations-en.md`
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

Kontrakt already relies on establishment across the Contract Machine.

Different authorities establish different meanings, and later responsibilities may depend on those results. The common
problem is that a result can be authoritative for its source occurrence without being applicable everywhere else.

The existing Contracts already expose this distinction. A lowered candidate does not become Fact until the required
judgments succeed, and later processing does not rewrite meaning that was already established under an earlier
occurrence.

The same law is needed by Governance and Diagnostic. Governance may use material established by another authority
without taking ownership of that material. Diagnostic may explain an earlier result without reconstructing the source
meaning from later observation.

Without one common law, each Contract would need its own meaning for establishment, applicability, and later use. That
would make cross-Contract reasoning inconsistent and would let implementation details leak back into semantics.

This ADR defines the common relation between:

```text
Definition
    ↓
Occurrence
    ↓
Judgment where required
    ↓
Establishment
    ↓
Established Material
    ↓
later applicable use
```

The individual Contracts still own the meaning of their own results.

---

## 2. Problem

A Contract Definition declares a responsibility. It does not represent one application of that responsibility.

Material may also exist before it has authority. A value can be observed, computed, or stored without being established
under any Contract. Even an established result may later become inapplicable to another occurrence while remaining the
valid result of its original occurrence.

If these distinctions collapse, implementation state starts deciding Contract meaning.

The machine therefore needs common answers to four questions:

1. What is one exact application of a Contract Definition?
2. When does material gain the authority owned by that Contract?
3. When may another occurrence use previously established material as basis?
4. What remains true when later machine state changes?

The answer must work across the 1D Contract pipeline, the State-Machine axis, Governance, Failure, Publication, Output,
and Diagnostic.

It must also remain independent from compiler caches, runtime objects, and physical scheduling.

---

## 3. Decision Drivers

The common law must preserve source authority after results cross Contract boundaries.

It must distinguish a Definition from each occurrence of that Definition.

Basis material must remain separate from the result established from that basis.

Applicability must prevent material from being reused under the wrong Contract meaning.

Incomplete required basis must not create partial authority.

Later occurrences must not rewrite earlier establishments.

Observation and storage must remain outside establishment unless a Contract explicitly gives them semantic meaning.

The law must remain small enough that each Contract can keep its own result shape and context.

Compiler mechanisms may represent these relations, but they must not define them.

---

## 4. Decision

### 4.1. Definition and Occurrence

A **Contract Definition** declares one Contract responsibility.

A **Contract Occurrence** is one exact application of that responsibility to a semantic situation governed by the
Definition.

```text
Contract Definition
        ↓
one or more distinct Occurrences
```

Two occurrences can establish equal material without becoming the same occurrence.

Occurrence is semantic. It is not defined by a runtime call, object identity, timestamp, thread, or generated
identifier.

No universal occurrence object is required. A realization only needs to preserve the distinction where later Contract
meaning depends on it.

---

### 4.2. Judgment and Establishment

A **Judgment** is an authoritative evaluation owned by a Contract or State-Machine responsibility.

**Establishment** is the boundary at which source-specific meaning becomes authoritative for one occurrence.

```text
required basis
    ↓
owning judgment where required
    ↓
establishment
    ↓
authoritative material
```

A Contract can depend on judgments owned by other authorities without transferring ownership of its own result.

Fact is the main example. Invariant and State-Machine judgments may be required before a proposed Fact gains authority,
but those judgments do not become the source of Fact meaning.

---

### 4.3. Candidate Material Has No Authority by Itself

Material does not become authoritative merely because an implementation has produced a complete candidate.

```text
candidate
    !=
established result
```

Lowering can form candidate Fact material before Fact is established. The same distinction applies whenever a later
authority must still decide whether produced material may receive its meaning.

Being unrefuted is also insufficient. A candidate does not gain authority because current analysis has found no
contradiction.

Establishment occurs only when the requirements owned by the relevant semantic boundary are satisfied.

---

### 4.4. Established Material Keeps Its Source Authority

**Established Material** is the common term used here for source-specific meaning that has received authority.

It is not a universal Contract type.

A Fact remains Fact. A Failure remains Failure. A Governance Binding keeps Governance meaning. Other Contracts keep
their own result kinds.

When another authority uses established material, the source meaning remains owned by its original authority.

```text
Authority A
    establishes M

Authority B
    uses M as basis
    establishes N
```

B may establish its own conclusion from `M`. It may not claim that A established B's conclusion or rewrite what A
established.

This rule also applies when the consumer is a verifier, Diagnostic, test synthesis, or optimization subsystem.

---

### 4.5. Basis and Result Are Different

Material used by a judgment is not the result of that judgment.

An authority may establish a new conclusion from source material only through its own declared law.

For example:

```text
A establishes:
    temperature = 110

B declares:
    temperature > 100 -> unsafe

B establishes:
    unsafe
```

The new meaning belongs to B. A still owns only the temperature result.

This prevents a later consumer from strengthening the source meaning retroactively.

---

### 4.6. Observation Does Not Establish Contract Meaning

Observed material has no Contract authority merely because it is visible to the machine.

If realization-originated information is allowed to affect a Contract judgment, a declared authority must qualify the
meaning needed by the dependent Contract.

```text
observation
    ↓
declared qualification or judgment
    ↓
established semantic material
```

The exact qualifying authority depends on the Contract being designed. This ADR only establishes that raw observation
cannot become authority by itself.

---

### 4.7. Applicable Context Is Source-Specific

Every occurrence is interpreted under the Contract material that is actually relevant to that occurrence.

**Applicable Context** contains only the coordinates needed to interpret the occurrence honestly.

For one authority that may include Version. Another may also depend on Governance or State.

There is no universal nullable context record.

Any coordinate required by the owning law must be fixed for the occurrence before its result is established. The
physical mechanism used to preserve that fixed meaning remains an implementation choice.

---

### 4.8. Establishment and Applicability Are Different

Establishment makes material authoritative for its source occurrence.

It does not make the material valid basis for every later occurrence.

**Applicability** is the relation that decides whether established material may be used by a dependent occurrence under
the Contract meaning that governs that later occurrence.

```text
Established M
        +
Dependent Occurrence O
        ↓
M is applicable to O
        ↓
O may use M as basis
```

The same material can therefore be applicable to one occurrence and not another.

Applicability is not a permanent Boolean property of the source result.

---

### 4.9. Semantic Dependency

A **Semantic Dependency** exists when one occurrence declares established meaning from another authority as part of its
basis.

A dependent occurrence may rely on that material only when the source meaning has been established and is applicable to
the dependent occurrence.

```text
Occurrence A
    establishes M

Occurrence B
    requires M
```

Physical availability does not satisfy this relation.

The dependency also does not transfer authority. B owns only the result established by B.

---

### 4.10. Required Basis and Establishment Are Complete

The owning Contract decides which basis is required for an occurrence.

If the law requires several parts, all required parts must be satisfied before the result receives authority.

```text
required:
    A + B + C

available:
    A + B

result:
    not established
```

A Contract may define another complete alternative. It may not silently weaken an incomplete case.

The same rule applies when several independent judgments must agree on one coordinated change. Physical work may
complete in pieces, but authority belongs only to the complete semantic result.

Missing basis does not create a synthetic result. Failure or another meaning exists only when the authority that owns
that meaning establishes it.

---

### 4.11. Higher-Scope Meaning Requires Its Own Authority

Established local results do not automatically create a higher-scope result.

```text
A establishes MA
B establishes MB

MA + MB
    ↓
higher-scope owning judgment
    ↓
MC established
```

The local results become basis for the higher-scope authority.

This prevents implicit Whole-Machine aggregation and gives Governance a clear way to combine material from several
source authorities without taking ownership of the source meanings.

---

### 4.12. One Source Result May Have Many Consumers

One established result may be used by several independent responsibilities.

The source authority remains singular even when Governance, verification, test synthesis, Diagnostic, and optimization
all depend on the same meaning.

Each consumer owns only the result produced by its own responsibility.

This allows shared analysis without creating an authority chain between subsystems.

---

### 4.13. Non-Establishment Has No Implicit Meaning

If an occurrence does not establish a result, the machine does not invent another result simply to fill the pipeline.

A later position that is never reached has no separate `Skipped` or `Blocked` semantic result unless an authority
explicitly owns such meaning.

Likewise, failure to establish one result does not by itself establish Failure.

This keeps non-establishment distinct from every declared result kind.

---

### 4.14. Later Occurrences Do Not Rewrite Earlier Establishments

An established result keeps the meaning and applicable context of its own occurrence.

A later occurrence may establish new material for later use, but it does not mutate the earlier result.

```text
Occurrence O1
    under Context C1
    establishes M1

later

Occurrence O2
    under Context C2
    establishes M2
```

`M2` may replace what is applicable next where an owning Contract allows that relation. It does not change what `O1`
established.

This is the common law behind retry, Version succession, Governance rebinding, and later Output transport.

---

### 4.15. Source Context Must Remain Honest

Later explanation or reconstruction must preserve the source relation of an earlier establishment.

Material from another occurrence must not be presented as though it belonged to the original source occurrence.

```text
result from A
context from B
    !=
one source establishment
```

A later authority may explicitly establish a new conclusion from both. It must not rewrite the earlier source record to
create that conclusion.

This rule also preserves temporal fidelity for Diagnostic Evidence.

---

### 4.16. Establishment Is Separate from Availability and Retention

Establishment concerns semantic authority.

Availability concerns whether material can currently be obtained. Retention concerns whether it remains preserved after
the point where it could otherwise be discarded.

These relations do not redefine one another.

An established result does not become unestablished when stored material is removed. Retaining material longer also does
not increase its authority.

Persistence and archival policy remain outside this ADR.

---

### 4.17. Establishment Does Not Grant Outward Authority

Internal authority is not Publication authority.

An established result may remain entirely inside the Contract Machine.

Publication decides which established meaning may leave the Core. Output determines the outward result shape where an
Output Contract applies.

Internal storage or Diagnostic access cannot bypass those boundaries.

---

## 5. Contract and State-Machine Axes

The Contract Pipeline and State-Machine axis remain separate authorities.

Where one change depends on both axes, their independent judgments may become basis for one coordinated establishment.

```text
proposed Fact change
+
proposed State movement
        ↓
required Contract and State judgments
        ↓
complete accepted proposal
        ↓
Fact authority and legal movement established
```

No judgment acquires another authority's meaning merely because both were required for the same proposal.

---

## 6. Relation to the Contract Pipeline

The common law applies to the existing pipeline without redefining each Contract.

| Authority            | Establishment relation                                                                                                                                                 |
|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Input                | Outside material gains Input meaning only at the Input boundary owned by the selected Contract.                                                                        |
| Admission            | Admission establishes its continuation judgment, not Fact authority.                                                                                                   |
| Canonicalization     | Canonicalization establishes the representative owned by its equivalence law.                                                                                          |
| Lowering             | Lowering may establish its declared relation and produce candidate core material; that candidate is not yet Fact.                                                      |
| Fact / Invariant     | Invariant owns integrity judgment. Fact owns factual authority after all required judgments for the proposal succeed.                                                  |
| Budget / Capacity    | Each Contract owns its resource judgment. Another authority may use the result only through declared dependency and applicability.                                     |
| Policy / Governance  | Governance may use established source material as Decision Basis, while Governance alone owns the Binding it establishes.                                              |
| Failure              | Failure exists only when Failure law establishes it. Later processing does not rewrite its source occurrence.                                                          |
| Publication / Output | Publication grants outward authorization for established source meaning. Output establishes the outward shape without taking ownership of the internal source meaning. |

The detailed laws of each row remain in the owning ADR.

---

## 7. Diagnostic Relation

Diagnostic relies on this ADR for source occurrence, establishment, applicability, and non-retroactivity.

A Diagnostic occurrence may refer to established source material and may also use qualified observation where Diagnostic
law allows it.

It establishes only Diagnostic meaning.

```text
Source authority
    establishes M

Diagnostic
    refers to M
    establishes E
```

`E` does not replace `M`.

Later observation must remain distinguishable from material that belonged to the source occurrence. Retention of
Diagnostic material also has no effect on whether the source result was established.

ADR-0060 owns the exact Diagnostic Evidence and Retention rules built on these common relations.

---

## 8. Determinism and Concurrency

Establishment follows semantic prerequisites rather than incidental scheduling.

A different worker schedule must not change which meaning becomes authoritative when the same Contract basis applies.

```text
physical completion order
    !=
semantic establishment order
```

If order itself carries Contract meaning, the authority that owns that order must declare it.

Locks, transactions, epochs, and other coordination mechanisms remain realization choices.

---

## 9. Version Relation

Version remains owned by ADR-0053.

An established result keeps the Version meaning that applied to its occurrence.

A later Version may change what material is applicable next, but it does not reinterpret the earlier establishment.

Stable bytes or host representation therefore do not prove that old material remains semantically applicable.

---

## 10. Semantic Lifetime

This ADR does not introduce a separate `SemanticLifetime` object.

The required semantic relation is already expressed by establishment, applicability, and any succession law owned by
another Contract.

Material may remain historically established after it stops being applicable to later occurrences.

Memory lifetime and retention duration remain separate concerns.

---

## 11. Realization and Canonical Representation

The compiler must preserve the semantic distinctions defined here, but this ADR does not prescribe one physical layout.

Where the meaning requires it, compiler-owned material must be able to preserve the Definition, source authority,
applicable context, and semantic dependency. Occurrence distinction must also remain available where later meaning
depends on it.

These relations do not need one universal record or graph.

Runtime object identity is not semantic identity. Source position is not semantic identity. A cache entry is not
Contract authority.

The physical representation may change as long as the Contract meaning remains unchanged.

---

## 12. V1 Compiler Requirements

V1 must establish enough structure that V2 can reuse these semantics without another rewrite.

Resolved Contract Definitions need stable compiler-owned identity.

A dependency on another authority's established material must survive lowering as an explicit semantic relation rather
than hidden execution order.

Candidate construction must remain separate from immutable published compiler products.

Source provenance must remain separate from semantic identity so that Diagnostic and tooling can move independently from
Contract meaning.

Verifier, PBT, Diagnostic, and optimization should be able to reuse shared analysis while retaining the exact source
authority.

These are compiler requirements derived from the Contract law. They do not add new Contract semantics.

---

## 13. V2 Compiler Consequences

V2 may use query systems, incremental invalidation, persistent caches, summaries, and immutable compiler generations to
implement these relations efficiently.

Those compiler concepts remain separate from Contract semantics.

```text
Contract Occurrence
    !=
Compiler Query Invocation

Semantic Dependency
    !=
Query Dependency

Contract Applicability
    !=
Cache Validity
```

The separation allows the compiler to reuse unchanged semantic material while invalidating only the analyses whose
dependent context changed.

It also allows one source meaning to feed verification, test synthesis, Diagnostic, and optimization without recomputing
ownership.

Higher-scope establishment leaves room for summary-driven Whole-Machine analysis because a unit summary can be basis
without becoming Whole-Machine authority.

Candidate separation also leaves room for independent preservation checks before transformed compiler material is
published.

The exact V2 mechanisms remain in the V2 architecture document.

---

## 14. Product-Subsystem Boundary

Product subsystems consume Contract meaning without becoming its source authority.

The Verifier checks whether declared meaning or lowering is valid.

PBT and fixture generation derive cases from Contract meaning.

Diagnostic explains an exact source occurrence.

Optimization changes realization while preserving meaning.

Each subsystem owns its own product. None may replace the source Contract result it consumes.

---

## 15. Frontend Boundary

No user-facing `establish` statement is introduced by this ADR.

Users continue to declare the Contracts that own actual meaning.

The compiler derives establishment relations after resolution from those definitions and their explicit bindings.

Compiler query keys, runtime IDs, cache keys, and backend handles must not become user-facing authority.

---

## 16. Consequences

Failure, Governance, Diagnostic, Publication, and later Contracts can rely on one shared establishment law while keeping
their own result semantics.

Governance can use source-owned material directly as Decision Basis without making Diagnostic an intermediate authority.

Diagnostic can focus on the evidence and explanation it owns because source occurrence and source authority are already
defined above it.

The compiler gains a stable semantic seam for shared analysis and incremental reuse without making those mechanisms part
of Contract Theory.

The main cost is that lowering and verification must preserve source authority and applicability explicitly enough for
later consumers to use them correctly.

---

## 17. Rejected Directions

### 17.1. Implementation Completion as Establishment

Object construction, method return, and similar implementation events are rejected as the definition of establishment
because the same Contract meaning can be realized without them.

Observation is rejected for the same reason. Visibility alone does not grant semantic authority.

### 17.2. Universal Context or Occurrence Objects

A universal context record is rejected because different authorities need different coordinates.

A universal occurrence identifier is also rejected because semantic distinction does not require one prescribed runtime
representation.

### 17.3. Automatic Derived Meaning

Local results do not automatically establish higher-scope meaning.

Missing basis also does not automatically create `Unknown` or Failure.

The authority that owns the derived meaning must establish it.

### 17.4. Compiler Validity as Contract Meaning

Compiler cache validity and query state are rejected as definitions of Contract applicability.

They may implement reuse decisions, but they do not decide semantic authority.

### 17.5. Universal Lifetime or Graph

A universal semantic lifetime is unnecessary because establishment and applicability already express the needed
relation.

A universal establishment graph is also rejected because source relation, semantic dependency, control flow, and
compiler dependency are different structures.

---

## 18. Verification Requirements

The verifier must reject a representation that allows a dependent judgment to claim established basis without resolving
the required source authority and applicability.

It must reject partial authority where the owning law requires complete basis.

Composition must preserve the source authority of every input result.

A later Version or Governance decision must not make an earlier occurrence appear to have been established under later
meaning.

Diagnostic projection must preserve the distinction between source material and later observation.

Lowering must not rely on backend-specific shape as the only source from which these semantic relations can be
reconstructed.

---

## 19. Implementation Note

This ADR intentionally leaves physical realization open.

V1 may use direct immutable structures. V2 may use more advanced compiler infrastructure.

The boundary remains:

```text
If replacing the mechanism changes
the declared Contract meaning,
the missing rule belongs in Contract semantics.

If the meaning stays the same,
the mechanism belongs to realization.
```