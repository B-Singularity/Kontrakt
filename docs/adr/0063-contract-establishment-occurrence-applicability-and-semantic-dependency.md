# ADR-0063: Contract Establishment, Occurrence, Applicability, and Semantic Dependency

## Status

Proposed

## Date

2026-08-29

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

Kontrakt has several authorities that establish meaning and several compiler subsystems that consume that meaning.

The existing Contracts already rely on a common distinction: material can exist before it has authority, and material
that has authority is not automatically applicable to every later judgment. A lowered candidate, for example, does not
become Fact until the required judgments succeed.

This distinction becomes more important in the compiler architecture planned for V2. Verification, test synthesis,
diagnostics, optimization, linking, and realization must be able to consume the same Contract meaning without rebuilding
it independently or taking authority from its source.

The common law must therefore answer two related questions.

```text
When does Contract material become authoritative?

When may another responsibility rely on that material?
```

The answer must remain independent from the compiler mechanism used to store, query, cache, schedule, or lower that
meaning.

This ADR defines that common law.

It does not replace the source-specific semantics owned by the individual Contracts or the State-Machine axis.

---

## 2. Problem

Kontrakt currently has the pieces of an establishment model, but the common relation is not yet explicit.

A source declaration is not authoritative merely because it was parsed. Candidate material is not authoritative merely
because it was computed. An established result is not valid basis for a later judgment merely because the compiler can
reach it.

Without a common law, cross-authority use becomes ambiguous.

```text
source material
    ↓
another responsibility reads it
    ↓
new judgment
```

The compiler must know whether the source material is authoritative, whether it is applicable to the new judgment, and
which authority still owns its meaning.

The same problem affects determinism. If semantic meaning can depend on worker completion order, object identity, cache
state, source discovery order, or physical storage position, clean and incremental compilation can disagree even when
the Contract meaning is unchanged.

The V2 compiler therefore needs a semantic foundation that can support stable identity, exact cross-reference, shared
analysis, incremental reuse, and deterministic parallel execution without turning those compiler mechanisms into
Contract authority.

---

## 3. Decision Drivers

Establishment must describe semantic authority rather than implementation completion.

The same authoritative basis under the same applicable Contract meaning must produce the same established meaning.

Source authority must remain exact after material crosses subsystem or Contract boundaries.

Applicability must be determined from declared semantic relations rather than physical availability.

Semantic identity must remain independent from source location, compiler generation, and storage address.

The representation must not require host-language object identity or a compiler-wide object graph.

Cross-authority relations must be representable through compact exact references without introducing a universal
material wrapper.

Incomplete required basis must not create partial authority.

Later changes may affect later applicability without rewriting earlier established meaning.

Compiler query state, cache validity, and physical scheduling must remain outside Contract semantics.

---

## 4. Established Material

### 4.1. Establishment

**Establishment** is the semantic boundary at which material receives the authority owned by its source responsibility.

The boundary is defined by the law that owns the meaning being established.

```text
required semantic basis
        ↓
owning law
        ↓
Establishment
        ↓
Established Material
```

Physical completion does not define this boundary.

A method return, object allocation, cache insertion, or storage write may participate in realization, but none grants
Contract authority by itself.

---

### 4.2. Established Material

**Established Material** is source-owned semantic material whose required authority has been completed through
Establishment.

It is a common semantic category, not a common compiler type.

A Fact remains Fact. A Failure remains Failure. A Governance Binding keeps Governance meaning. The State-Machine axis
keeps its own result vocabulary.

The common term exists so that another responsibility can state:

> this basis must already carry the authority owned by its source.

It does not create a universal `EstablishedMaterial` object, wrapper, container, or schema.

---

### 4.3. Definition Material and Occurrence Material

Establishment applies to two different kinds of semantic material.

**Established Definition Material** is the authoritative Contract meaning obtained after authored material has been
resolved into the complete Kontrakt-owned definition required by that authority.

**Established Occurrence Material** is the source-specific result established when such a definition is applied under
the semantic conditions required for that application.

```text
authored material
    ↓
resolution and semantic completion
    ↓
Established Definition Material
    ↓
semantic application
    ↓
Established Occurrence Material
```

These are related but not interchangeable.

The Canonical Contract World primarily represents authoritative definition material and its semantic relations. It does
not pre-create every later Contract occurrence.

Compiler publication of a completed representation also remains separate from Contract Establishment. Publication of a
compiler generation controls visibility of compiler data; it does not create Contract meaning merely by making bytes or
records visible.

---

### 4.4. Contract Occurrence

A **Contract Occurrence** is a distinct semantic application of an Established Definition under the coordinates required
by that authority.

Occurrence is not defined by a compiler query, runtime call, thread, timestamp, or retry attempt.

Two occurrences may establish equal result material and still remain distinct where later Contract meaning needs that
distinction.

The owning authority determines which semantic coordinates distinguish its applications. This ADR does not create one
universal occurrence key.

---

### 4.5. Judgment and Establishment

A **Judgment** is an authoritative evaluation owned by a Contract or State-Machine responsibility.

Judgment and Establishment are different.

A result may be established after several required judgments have succeeded even when the authority receiving the final
meaning does not perform another Boolean judgment of its own.

Fact is the representative case. Invariant and State-Machine judgments may be required before a proposed Fact receives
factual authority, but neither judgment becomes the source of Fact meaning.

---

### 4.6. Candidate Material

Candidate material has no authority merely because it is complete enough to inspect.

```text
candidate
    !=
Established Material
```

A candidate becomes authoritative only through the establishment boundary owned by the meaning it seeks to receive.

The absence of a known contradiction is also insufficient. Material is not established merely because current analysis
has not rejected it.

---

### 4.7. Source Authority

Established Material keeps the authority that established its meaning.

A consumer may use source material to establish a new conclusion under another law.

```text
Authority A
    establishes M

Authority B
    uses M as basis
    establishes N
```

`N` belongs to B. The meaning of `M` still belongs to A.

A consumer must not rewrite the source meaning or attribute its own conclusion to the source authority.

---

## 5. Deterministic Establishment

### 5.1. Semantic Determinism

Establishment is determined only by semantically relevant Contract material.

If the owning law, required established basis, and applicable semantic coordinates are equal under their owning equality
laws, the established meaning must also be equal.

```text
same owning law
+
same authoritative basis
+
same applicable meaning
        ↓
same established meaning
```

This is the primary determinism law of this ADR.

Implementation state may decide how the result is computed. It cannot decide what the result means.

---

### 5.2. Hidden State Is Not Semantic Input

A semantic result must not depend on compiler state that is absent from the declared or established basis.

A warm cache and a cold cache must therefore represent the same Contract meaning. The same applies to different worker
counts or different discovery order.

If some state can change the required semantic result, that state must first become explicit semantic material owned by
an applicable authority.

---

### 5.3. Physical Order Is Not Semantic Order

Worker completion order cannot decide which semantic result becomes authoritative.

```text
physical completion order
    !=
semantic establishment order
```

Where order changes Contract meaning, the relevant authority must own that ordering relation explicitly.

This keeps parallel evaluation and deterministic semantics compatible.

---

### 5.4. Deterministic Composition

A result derived from several established sources must also be deterministic.

The same applicable source material under the same composition law must establish the same composed meaning regardless
of the order in which the sources were computed or discovered.

This rule applies before any compiler optimization or parallel scheduling decision is considered.

---

## 6. Identity and Reference

### 6.1. Semantic Identity

Established Definition Material must have exact semantic identity where another compiler responsibility needs to refer
to it.

The owning identity law decides which meaning participates in that identity.

This ADR does not define one universal identity algorithm.

It requires only that incidental implementation state cannot own semantic identity.

---

### 6.2. Distinct Identities Stay Distinct

The compiler must not collapse the following concepts.

| Concept                | Meaning                                                                 |
|------------------------|-------------------------------------------------------------------------|
| Semantic identity      | Which Contract meaning this is                                          |
| Occurrence distinction | Which semantic application produced or used occurrence-specific meaning |
| Source provenance      | Where authored material came from                                       |
| Fingerprint            | Whether relevant compiler input or result changed                       |
| Compiler generation    | Which published compiler view contains a representation                 |
| Local address          | Where material is stored inside that view                               |

A change in one relation does not automatically change the others.

Moving a declaration in source, for example, may change provenance while leaving its Contract meaning unchanged.

---

### 6.3. Source Provenance Is Not Authority

Source location helps diagnostics and tooling locate authored material.

It does not define Contract identity.

A source-only change must not create different Contract meaning when the semantic definition remains the same.

Likewise, two pieces of material must not be merged merely because their source text or physical encoding happens to
match.

---

### 6.4. Cross-Authority Reference

A semantic reference must identify the source meaning precisely enough for the consumer to recover the authority and
meaning it is allowed to use.

The reference must not depend on object identity or an ad-hoc string lookup.

Different authorities may use different physical reference forms. This ADR requires exactness, not one universal
reference type.

---

## 7. Applicability

### 7.1. Applicable Context

Established Material is not automatically usable by every later responsibility.

The **Applicable Context** of an occurrence is the source-specific Contract material needed to interpret that occurrence
correctly.

Only coordinates required by the owning law belong to that context.

There is no universal nullable context record.

---

### 7.2. Applicability Relation

**Applicability** determines whether particular Established Material may be used as basis for a dependent semantic
application.

```text
Established source material
        +
dependent responsibility
        +
required applicable context
        ↓
applicable basis
```

Applicability belongs to this relation. It is not a permanent Boolean property of the source material.

The same established material may therefore be applicable to one later occurrence and not another.

---

### 7.3. Applicability Is Deterministic

Applicability must depend only on the semantic coordinates declared by the participating authorities.

Physical reachability, cache presence, recent computation, or current storage location cannot make material applicable.

Given the same established source material and the same relevant context, the applicability result must be the same.

---

### 7.4. Later Applicability Does Not Rewrite Earlier Meaning

A later establishment may change what is applicable to later occurrences where an owning law permits that relation.

It does not change the identity, meaning, or context of an earlier establishment.

```text
earlier establishment remains what it was

later establishment may govern later use
```

This removes the need for a universal mutable `current` result.

Succession, replacement, withdrawal, and compatibility remain owned by the Contracts that define those relations.

---

## 8. Semantic Dependency and Composition

### 8.1. Semantic Dependency

A **Semantic Dependency** exists when a responsibility requires source-owned established meaning as part of its semantic
basis.

The dependency is to the source meaning, not to the physical value that happens to encode it.

```text
consumer responsibility
        ↓ requires
source authority and source meaning
        ↓
applicable Established Material
```

This distinction is required before the compiler can safely derive query or build dependencies from Contract meaning.

---

### 8.2. Required Basis Must Be Complete

The owning law decides which basis is required for Establishment.

If several parts are required, authority appears only when the required basis is complete.

```text
required:
    A + B + C

available:
    A + B

result:
    not established
```

The owning Contract may define another complete alternative. It may not silently treat an incomplete case as a weaker
success.

Missing basis also does not create Failure or another synthetic result unless an authority explicitly establishes that
meaning.

---

### 8.3. Higher-Scope Meaning Requires an Owning Law

Several established local results do not automatically create a larger semantic result.

They may become basis for an authority that owns the combined meaning.

```text
MA + MB
    ↓
owning composition law
    ↓
MC
```

The composition step preserves the source authority of `MA` and `MB` while establishing only the new meaning owned by
the composing authority.

---

### 8.4. One Source May Have Many Consumers

Established source material may be consumed by several independent responsibilities.

The consumers do not form an authority chain merely because they reuse the same source meaning.

A shared compiler analysis may also serve several products, but that analysis remains compiler-derived material unless a
Contract authority separately establishes the meaning it represents.

---

## 9. Integrity Boundaries

### 9.1. Observation Is Not Establishment

Observed realization state has no Contract authority merely because the compiler or runtime can read it.

Where observation affects a Contract judgment, an applicable authority must first establish or qualify the semantic
meaning required by that judgment.

```text
observation
    ↓
owned semantic qualification
    ↓
Established Material
```

This ADR does not create one universal Observation Contract.

---

### 9.2. Non-Establishment Has No Implicit Result

If a semantic result is not established, no replacement meaning appears automatically.

An unreached stage does not create `Skipped` or `Blocked` merely to complete the pipeline.

Failure is established only under Failure law.

---

### 9.3. Source Relation Must Remain Honest

A later consumer must preserve the source relation needed to interpret Established Material correctly.

Material from another occurrence may support a new judgment, but it cannot be presented as though it belonged to the
earlier source occurrence.

This keeps later reconstruction distinct from occurrence-time meaning.

---

### 9.4. Availability and Retention Are Separate

Established meaning may later become unavailable to a particular consumer.

That does not erase the earlier Establishment.

Keeping material longer also does not increase its authority.

Retention and persistence therefore remain separate responsibilities.

---

### 9.5. Establishment Does Not Grant Outward Authority

Internal authority does not imply external exposure.

Publication remains responsible for outward authorization, and Output remains responsible for the outward result shape.

Established Material may be preserved or used internally without becoming publishable.

---

## 10. Canonical Contract World

### 10.1. Authoritative Semantic Substrate

The Canonical Contract World is the compiler-owned representation of authoritative Contract definition material and the
semantic relations required to interpret it.

It is not the source of Contract authority.

Its job is to preserve authority that has already been determined by the applicable Contract semantics.

```text
Contract semantics
    ↓
authoritative definition material
    ↓
Canonical Contract World
```

The compiler must not reconstruct that authority independently inside each product subsystem.

---

### 10.2. Definition Material and Runtime Occurrence Are Different Products

The Canonical Contract World records the definitions and relations needed to judge or realize later occurrences.

A runtime Contract Occurrence is not a pre-existing node that must be materialized for every possible execution.

Likewise, a compiler query over a definition is not a Contract Occurrence.

This distinction allows one stable definition substrate to support many later applications.

---

### 10.3. Derived Compiler Material

A compiler analysis may derive useful facts from the Canonical Contract World.

Examples include reachability, dependency closure, or optimization legality.

Such material is compiler knowledge.

It does not become source Contract authority merely because several compiler products depend on it.

Only material established under an applicable Contract or State-Machine authority enters the Established Material
category defined by this ADR.

---

### 10.4. Independent Consumers

Verifier, test synthesis, diagnostics, optimization, linking, and realization may consume the same authoritative
substrate.

No subsystem needs to become the semantic source for the next subsystem.

```text
Canonical Contract World
    ├── Verification
    ├── Test Synthesis
    ├── Diagnostics
    ├── Optimization
    └── Realization
```

Shared derived analyses are allowed where their ownership remains compiler-internal and their dependency on source
meaning stays explicit.

---

## 11. Low-Level Representation Requirements

This section constrains compiler representation without selecting a concrete storage mechanism.

### 11.1. Semantic Meaning Must Not Require Object Identity

The semantic core must be representable without relying on host-language object identity.

A Contract meaning cannot depend on which Kotlin or JVM object happens to carry it.

Compiler code may use objects as temporary construction or ergonomic API mechanisms. Those objects do not become
semantic identity or the required canonical storage model.

---

### 11.2. No Required Object Graph

Established Definition Material and the relations defined by this ADR must be representable without traversing a
compiler-wide object-pointer graph.

The core representation must permit exact value-level references to the semantic material a consumer requires.

This keeps the semantic model compatible with compact low-level storage and direct access.

---

### 11.3. Stable Identity and Local Address Are Different

Stable semantic identity must remain separate from the address used by a particular compiler representation.

```text
stable semantic identity
    !=
generation-local address
```

A local address may change when the same semantic world is rebuilt or republished.

That change must not create new Contract meaning.

The local address may be an ordinal, offset, slot, or another compact representation chosen by the compiler.

---

### 11.4. Relations Must Admit Compact References

Semantic Dependency, authority reference, applicability input, and source relation must be representable through compact
exact references.

The physical form of those references may vary by authority or compiler generation.

Their semantic interpretation must remain stable.

A cross-subsystem consumer should therefore be able to identify the exact source material it needs without
reconstructing meaning from a wrapper hierarchy or generic object graph.

---

### 11.5. No Universal Established-Material Container

This ADR does not require every authority to store its material in one common record.

Source-specific meaning should remain source-specific.

A common container would force unrelated authorities to share fields, null states, or payload conventions that do not
belong to their semantics.

The commonality established here is authority and relation, not physical shape.

---

### 11.6. Deterministic Materialization

When the compiler assigns physical order or local addresses to equivalent semantic input, incidental execution order
must not affect the resulting semantic references.

Any ordering that becomes visible to another compiler responsibility must be derived from stable semantic coordinates or
another explicit deterministic rule.

This allows different physical layouts while preserving clean, incremental, and parallel equivalence.

---

## 12. Relation to Existing Contract Authorities

This ADR defines shared laws only. The owning ADRs keep their local meaning.

| Authority                           | Relation to Establishment                                                                                                     |
|-------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| Input / Canonicalization / Lowering | Each stage owns only the meaning declared for that stage. Candidate core material remains distinct from later Fact authority. |
| Fact / Invariant / State Machine    | Required judgments may jointly permit one coordinated change while each authority keeps its own meaning.                      |
| Budget / Capacity                   | Resource judgments remain source-owned material that later responsibilities may use only when applicable.                     |
| Policy / Governance                 | Governance may use established source material as Decision Basis while Governance alone owns the Binding it establishes.      |
| Failure                             | Failure is established only under Failure law and keeps the context of that establishment.                                    |
| Version                             | Later Version meaning may affect later applicability without reinterpreting an earlier establishment.                         |
| Publication / Output                | Outward authority is separate from internal Establishment.                                                                    |
| Diagnostic Evidence                 | Diagnostic may refer to an exact source establishment while establishing only the Diagnostic meaning it owns.                 |

The table is not a new pipeline definition.

---

## 13. Verification Requirements

### 13.1. Semantic Verification

The verifier must reject a semantic world that permits a result to claim authority without the establishment law owned
by its source.

A dependent judgment must not claim source material as basis when the required authority or applicability relation
cannot be resolved.

Where complete basis is required, partial authority is invalid.

Composition must preserve the original authority of its source material.

Later context must not be substituted for the context of an earlier establishment.

---

### 13.2. Determinism Verification

Compiler verification and QA must be able to detect semantic dependence on incidental execution state.

Equivalent semantic inputs must not produce different established meaning because work was scheduled differently or
because the compiler reused a cached computation.

Where compiler ordering affects references visible to later compiler stages, that ordering must have a deterministic
derivation.

---

### 13.3. Representation Verification

Lowering must preserve the source authority, semantic identity, applicability relation, and semantic dependency required
by this ADR.

Backend-specific shape cannot be the only place from which those relations can be reconstructed.

A compiler representation must not expose incomplete material as though it belonged to a completed authoritative world.

---

## 14. V1 Foundation Requirements

V1 must implement enough of this model that V2 can extend it without replacing the semantic foundation.

V1 therefore requires stable compiler-owned semantic identity after resolution, with provenance stored separately from
that identity.

Cross-authority dependency and applicability must have explicit representation seams. They cannot exist only as callback
order or hidden mutable state.

Major semantic computations must take explicit inputs and return deterministic results.

Candidate construction must remain separate from immutable compiler publication. A failed candidate cannot partially
enter the published semantic world.

The canonical core must not require object identity or a universal object graph. Its APIs and representations must leave
a direct path toward compact low-level references and source-specific storage.

Shared analyses must be able to serve multiple product subsystems without becoming Contract authority.

V1 QA must test semantic equivalence across clean recomputation and different worker counts wherever those execution
modes are supported.

---

## 15. V2 Consequences

V2 may use the stable relations defined here as inputs to demand-driven queries and incremental dependency tracking.

A Contract Semantic Dependency may inform query dependency, but the two remain different relations.

A fingerprint may decide whether a compiler result can be reused, but it does not become semantic identity or proof of
applicability.

Incremental reuse may stop when recomputation yields unchanged semantic results. Clean and incremental compilation must
still converge on the same Contract meaning.

Parallel query execution may change physical completion order. It must not change establishment, applicability,
identity, or deterministic publication.

Persistent caches may retain compiler products across sessions. Losing the cache may reduce performance, but it must not
remove semantic truth.

Summary-driven Whole-Machine analysis may use compact source summaries as compiler inputs. A summary does not create
higher-scope Contract authority by itself.

The compiler may specialize physical representation aggressively because stable semantic identity is separate from local
address and storage layout.

The exact query engine, cache design, compact layout, parallel executor, and backend realization remain outside this
ADR.

---

## 16. Rejected Directions

### 16.1. Universal `EstablishedMaterial` Object or Store

Rejected because Established Material is a semantic category, not one physical shape.

A compiler-wide wrapper would couple unrelated authorities and make low-level source-specific representation harder.

### 16.2. Host Object Identity as Semantic Identity

Rejected because JVM allocation and object lifetime are realization details.

Replacing the host representation must not change Contract meaning.

### 16.3. Compiler Query or Cache State as Authority

Rejected because query execution and cache reuse are compiler computation mechanisms.

A cache hit cannot establish Contract meaning or make otherwise inapplicable material applicable.

### 16.4. Observation as Authority

Rejected because visibility of realization state does not establish its Contract interpretation.

The required semantic meaning needs an owning authority.

### 16.5. Automatic Partial or Higher-Scope Meaning

Rejected because incomplete basis has no partial authority and local results do not automatically establish their
composition.

The applicable owning law must establish the result.

### 16.6. Universal Context or Semantic Lifetime

Rejected because different authorities require different context, and applicability already expresses whether old
material may be used later.

A universal lifetime would mix semantic use with memory or retention concerns.

### 16.7. Physical Ordering as Semantic Ordering

Rejected because discovery order, hash iteration, and worker completion are not Contract meaning.

Any semantic ordering must come from an explicit authority.

---

## 17. Consequences

Kontrakt gains one common meaning for authoritative Contract material without forcing the individual Contracts into a
shared result schema.

Governance can depend directly on source-owned established material. Diagnostic can explain the same source material
without becoming an intermediate authority.

The Canonical Contract World can serve as a stable semantic substrate for independent compiler products while keeping
derived analysis separate from Contract authority.

Stable identity, applicability, and semantic dependency become available to V2 incremental and parallel infrastructure
without being defined by that infrastructure.

The representation remains compatible with low-level compiler-core storage because semantic meaning does not depend on
wrappers, object identity, or a universal object graph.

The cost is that the compiler must preserve more explicit relations. Identity, source authority, applicability,
dependency, and provenance can no longer be reconstructed casually from execution order or backend shape.

That cost is intentional. It is the foundation required for deterministic verification, shared analysis, reproducible
compilation, and later incremental reuse.