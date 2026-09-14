# Kontrakt Established Contract World Architecture TODO

## Status

**Architecture TODO / design review document. Not an ADR. Not Contract authority.**

This document records work that must be completed before the compiler freezes the semantic substrate between the Contract frontend and downstream compiler products.

It does not introduce a new Contract kind. It does not replace ADR-0063. Where current documents use different names for the same candidate boundary, this TODO records the conflict instead of silently choosing one.

---

# 1. Why This TODO Exists

Kontrakt now has a clearer separation between source representation, Contract authority, and realization IR.

The compiler needs a stable place where already-established Contract definition meaning can be consumed without reopening source syntax or reconstructing authority in every subsystem.

ADR-0063 already defines `Established Material` as a semantic property owned by each source authority. It also defines the `Canonical Contract World` as the compiler-owned semantic substrate that represents Established Definition Material and preserves the authority relations required by later compiler products.

ADR-0070 uses the working architecture term `Established Contract World` for the upstream Contract authority checkpoint consumed by verifier, diagnostics, test synthesis, and realization work.

These terms now need one explicit architecture decision.

The immediate problem is not storage format. The first problem is to state exactly what this world represents, how it differs from IR, and which compiler products may consume it.

---

# 2. Current Semantic Baseline

This TODO must preserve ADR-0063 rather than redefine it.

Establishment is the semantic boundary where material receives the authority owned by its source responsibility.

```text
required basis
    ↓
owning law
    ↓
Establishment
    ↓
Established Material
```

`Established Material` is not a universal compiler type.

Fact remains Fact after establishment. Failure remains Failure. Governance keeps ownership of the Binding it establishes.

The common property is that the owning authority has completed the semantic work required to give that material authority.

ADR-0063 also distinguishes definition meaning from occurrence meaning.

```text
Established Definition Material
        ≠
Established Occurrence Material
```

The Canonical Contract World currently represents Established Definition Material. Occurrence-specific material remains a separate semantic concern and must not be inserted into a universal world container merely because diagnostics or runtime execution may need to refer to it.

---

# 3. Working Architecture Distinction

The compiler should distinguish three kinds of material.

```text
Contract Frontend Representation
        ↓
Contract Authority Material
        ↓
Realization / Execution Representation
```

A candidate concrete shape is:

```text
.kontrakt source
    ↓
Lexer / Parser
    ↓
Syntax Material
    ↓
Resolution
    ↓
Resolved Contract HIR
    ↓
Authority-owned Establishment
    ↓
Canonical / Established Contract World
```

The realization side remains separate.

```text
User JVM realization
    ↓
Realization Acquisition
    ↓
Realization IR
    ↓
Verification
    ↓
Execution IR
    ↓
JVM IR
```

The two sides meet through explicit semantic and realization bindings. They do not become one graph or one IR.

---

# 4. The Contract World Is Not an IR Level

The current working direction is that the Contract World should **not** be treated as another ordinary IR level.

HIR is an intermediate compiler representation. It exists so source syntax can be resolved into exact high-level semantic material before establishment.

The Contract World has a different role. It represents authoritative Contract definition meaning after establishment.

```text
Resolved Contract HIR
    = intermediate compiler representation

Canonical / Established Contract World
    = authoritative semantic substrate
```

An IR can be replaced as long as the replacement preserves its IR Contract.

The physical representation of the Contract World can also be replaced, but the established meaning represented there cannot change because a compiler representation changes.

The distinction should remain explicit:

```text
IR semantics
    ≠
Contract semantics

IR physical representation
    ≠
IR semantics

Contract World storage
    ≠
Contract authority
```

The Contract World is therefore closer to a compiler-owned **semantic authority store or semantic substrate** than to an optimization IR.

This wording still must respect ADR-0063's rule that no universal `EstablishedMaterialStore` becomes a second semantic model. The logical world may present a unified view while the physical implementation remains authority-owned tables or stores.

---

# 5. Terminology Must Be Reconciled Before Implementation

Current documents use several overlapping names:

```text
Semantic Contract IR
Canonical Contract Material
Frozen Contract Image
Linked Contract World
Canonical Contract World
Established Contract World
Lowered Contract-Machine IR
```

Some of these names were written before ADR-0063 clarified Establishment, Definition, Occurrence, and authority ownership.

The documentation must now decide which names remain valid.

The strongest current candidate is:

```text
Resolved Contract HIR
    → frontend intermediate representation

Canonical Contract World
    → ADR-0063 name for the semantic substrate representing Established Definition Material

Established Contract World
    → either an architecture alias for Canonical Contract World,
      or a term that should be removed to avoid two names for one boundary
```

Do not create two physical or semantic layers merely to preserve old document vocabulary.

## TODO

- Decide whether `Established Contract World` is an alias, a broader logical view, or obsolete terminology.
- Keep `Canonical Contract World` aligned with ADR-0063 unless a later ADR intentionally changes the term.
- Audit old TODO documents that call the authority substrate `Semantic Contract IR`.
- Audit `Linked Contract World` wording so linking does not appear to establish unchanged source meaning again.
- Keep `Lowered Contract-Machine IR` separate from authoritative Contract definition material if that IR remains useful.

---

# 6. Resolved Contract HIR Boundary

The HIR should preserve the rich Contract vocabulary authored in `.kontrakt` without giving source syntax final authority.

By the time material enters HIR, semantic names needed by later Contract processing should already be exact references. Lexical scope search should not continue downstream.

A useful boundary is:

```text
Syntax Material
    ↓
name resolution
scope resolution
explicit absence normalization
exact Contract references
    ↓
Resolved Contract HIR
```

HIR should still distinguish Contract concepts such as Fact, Invariant, Failure, Governance, Publication, and State rather than collapsing them into generic branches or booleans.

HIR does not establish final Contract authority by itself.

## TODO

- Define the exact semantic invariant required before material is admitted into HIR.
- Define which authored distinctions remain visible in HIR.
- Define which syntax-only distinctions are intentionally erased.
- Define the exact HIR-to-Establishment input contract.
- Ensure HIR contains no user-realization call graph or JVM topology.
- Ensure HIR does not infer semantic relations from object containment or parent pointers.

---

# 7. Contract World Responsibility

The Contract World should let downstream compiler products consume established Contract definition meaning without reopening source text or depending on another product's private representation.

It should preserve exact authority identity and the direct semantic relations that have already been established.

It should also preserve the result of semantic resolution needed to interpret those relations.

The world must not invent new transitive Contract meaning merely because the compiler can derive a graph from direct relations.

A compiler may derive reachability, dependency, or optimization knowledge from the world. That derived knowledge remains compiler-owned.

## TODO

Define the minimum world surface required for:

- exact definition lookup;
- authority lookup;
- direct semantic relation lookup;
- Version-aware definition meaning;
- Basis Resolution results where composition has already established the source connection;
- applicability checks required by downstream semantic work;
- source provenance projection through compact references;
- independent compiler product consumption.

Do not add a field merely because one consumer finds it convenient.

---

# 8. Definition Material and Occurrence Material Must Stay Distinct

The Contract World must not erase the distinction introduced by ADR-0063.

```text
Established Definition
    ↓ semantic application
Established Occurrence Material
```

A Contract occurrence is not a compiler query. It is also not defined by a JVM method call.

The owning authority decides whether occurrence-specific meaning exists and which semantic coordinates distinguish it.

Diagnostics must not force every Contract to gain an occurrence model.

## TODO

- Decide whether the Canonical Contract World contains only Established Definition Material, as ADR-0063 currently states.
- Define the separate publication/storage boundary for Established Occurrence Material where an owning Contract actually requires it.
- Define how Definition References and Occurrence References are represented without requiring object pointers.
- Ensure runtime or compiler recomputation does not create a new Contract occurrence by accident.
- Ensure retention does not redefine whether an occurrence was established.

---

# 9. Source Provenance Is Adjacent, Not Part of Semantic Identity

Rich diagnostics require source fidelity, but source location must not become Contract identity.

The intended relation is:

```text
Established semantic material
        │
        └── compact provenance reference
                    ↓
              Provenance Store
```

A source-only edit may change line positions while leaving established definition meaning unchanged.

V2 should be able to reuse semantic material while refreshing source projection.

## TODO

- Keep SourceManager and Provenance Store separate from semantic identity storage.
- Preserve enough origin information for diagnostics after lowering and optimization.
- Do not copy source strings or path objects into every semantic row.
- Define provenance invalidation independently from semantic invalidation.
- Add tests where comments or whitespace move source spans without changing semantic results.

---

# 10. Diagnostics Consume the World; They Do Not Reconstruct It

Kontrakt diagnostics should speak in the vocabulary of the declared Contract Machine.

That requires access to authoritative semantic material, source provenance, and compiler-derived evidence.

```text
Canonical Contract World
           │
           ▼
      Diagnostics
       ▲       ▲
       │       │
Provenance   Compiler Evidence
```

Compiler evidence may include a realization call path or a failed verification relation. That evidence does not become source Contract authority.

A diagnostic should not reopen the IDL and independently decide what the Contract meant.

It should also not create a Contract occurrence merely because an explanation needs an exact source relation.

## TODO

- Define the semantic subject reference used by compiler diagnostics.
- Keep semantic dependency and source-provenance dependency separate.
- Make diagnostic generation a structured compiler product rather than a global string side effect.
- Preserve enough cheap semantic anchors to generate richer explanation on demand.
- Ensure stale provenance cannot survive when source projection changes.
- Ensure an unchanged source span cannot preserve a diagnostic after its semantic subject changes.

---

# 11. Verifier, PBT, Reference Judgment, and Execution Formation Share the Same Authority Substrate

One established Contract definition should not be reconstructed separately by each consumer.

The intended direction is:

```text
                  Canonical Contract World
                  /       |       |       \
                 ▼        ▼       ▼        ▼
             Verifier   PBT   Diagnostics  Reference
                  \       |       /        /
                   \      |      /        /
                    └── Execution Formation
```

The products remain siblings. One product must not become the authority source for another.

A verifier may derive proof or conflict material. PBT may derive witnesses. Diagnostics may derive explanations. None of those products rewrites the established Contract definition.

## TODO

- Define the smallest shared read surface needed by each consumer.
- Prevent consumer-specific fields from being added to authority material.
- Reuse derived analysis through query products when several consumers need the same result.
- Permit deliberate independent recomputation only when independence is part of verification.

---

# 12. Contract Semantic Relations, Realization Graphs, and Query Graphs Must Remain Separate

Kontrakt now has several graph-shaped structures with different meanings.

```text
Contract semantic relations
Realization call/effect/origin graph
Compiler query dependency graph
Diagnostic provenance graph
```

They must not be merged into one universal graph.

The Contract side is derived from IDL semantics and explicit composition. User implementation topology cannot add Contract authority edges.

The realization side starts from an explicit Interface or Operation realization binding and follows actual implementation calls.

The query graph records compiler recomputation dependencies.

## TODO

- Give each graph family its own owner and identity rules.
- Make the Contract-to-realization bridge explicit.
- Forbid implementation topology from creating missing Contract meaning.
- Forbid query dependency edges from becoming Contract semantic dependency.
- Keep derived transitive graphs outside the Contract authority store unless an owning Contract law explicitly establishes equivalent meaning.

---

# 13. Query-Oriented V1 Integration

Older V2 planning documents allowed a small V1 query seam and deferred full dependency recording to V2.

Current compiler design discussion has moved further.

The stronger candidate direction is:

> V1 should already use query-oriented compiler orchestration. Full persistent red/green reuse remains V2 work.

The query layer should request products from stable semantic material rather than drive the Contract semantics.

```text
Compiler Product Request
        ↓
Query Interface
        ↓
Contract / IR / Analysis Consumer
        ↓
Published Compiler Product
```

The query engine remains compiler realization.

Cache presence, query execution, and query generation cannot create Contract authority or Contract occurrences.

## V1 TODO

- Define stable logical query identities for major compiler products.
- Make query inputs and results explicit.
- Record dependencies during evaluation.
- Keep query results immutable after publication.
- Bind result validity to a compiler generation or equivalent revision boundary.
- Introduce fingerprints only as reuse evidence, never as semantic identity.
- Keep semantic dependencies distinct from provenance dependencies.
- Make query cancellation or invalidation incapable of leaving partially published semantic products.

## V2 TODO

- Persist query results and dependencies across compiler sessions.
- Add red/green validation.
- Add early cutoff when recomputation produces an equivalent consumer-visible result.
- Add lazy semantic materialization.
- Add parallel demand evaluation.
- Add revision-aware IDE queries without creating a second semantic engine.

---

# 14. Contract Version and Incremental Reuse

Contract Version is semantic material, not a compiler cache version.

When a Version change affects an established definition or its applicability, dependent semantic products must be reevaluated.

Unrelated compiler analyses do not need global invalidation merely because a Contract Version changed.

The query graph should therefore derive invalidation from actual semantic inputs.

```text
Version binding changes
        ↓
affected established meaning changes
        ↓
dependent query products invalidate
```

If a downstream result recomputes to the same consumer-visible result, V2 may stop propagation through early cutoff.

## TODO

- Define which world products are Version-sensitive by owning semantics.
- Keep Versioned semantic identity separate from structural compiler reuse.
- Allow structurally equal material from different Contract Versions to share physical storage only when semantic identity remains distinct.
- Test that Version changes do not act as a global compiler reset signal.

---

# 15. Physical Representation Must Not Become the World Model

The Contract World should be implementable without one JVM object per semantic entity.

The compiler core should prefer compact index-addressable storage for high-cardinality material.

A candidate physical direction is:

```text
semantic identity / HID lookup
        ↓
exact validation when required
        ↓
dense generation-local reference
        ↓
authority-owned frozen tables
```

Possible storage mechanisms include primitive arrays, dense tables, slabs, and compact ranges.

Those mechanisms remain replaceable implementation.

## TODO

- Do not create one `EstablishedMaterial` object per semantic entity.
- Do not create one giant `EstablishedContractWorld` object graph.
- Keep authority-owned material in separate logical tables where ownership differs.
- Use dense references only as generation-local access handles.
- Keep HID, fingerprint, ordinal, row number, and local address separate from semantic identity.
- Define exact collision verification wherever HID or digest lookup can affect correctness.
- Use build/verify/seal/publish or an equivalent boundary so consumers never observe incomplete world material.

---

# 16. World Publication and Compiler Generations

Compiler publication makes completed representation visible. It does not create Contract authority.

A published world generation should be immutable to ordinary consumers.

```text
construction
    ↓
verification
    ↓
seal
    ↓
publish
    ↓
read-only consumers
```

V2 may keep several compiler generations alive for IDE or daemon use.

Generation identity must remain physical compiler state rather than Contract identity.

## TODO

- Define the V1 publication boundary.
- Define which tables can be published independently and which require one coherent world boundary.
- Define stale-generation rejection.
- Define safe reference use across compiler generations.
- Leave a clean seam for V2 snapshot pinning and reclamation.

---

# 17. IR Interaction

The world should be referenced by IR rather than copied into every IR node.

For example, an Execution IR operation that realizes a Contract judgment may carry a compact exact reference to the relevant established Contract material.

```text
Execution IR operation
        ↓ exact Contract reference
Canonical Contract World
```

The Execution IR should not duplicate Version, Governance, Failure, State, and diagnostic material merely to keep those concepts nearby.

This preserves rich Contract knowledge for verification, diagnostics, reference judgment, and Contract-aware optimization without turning the IR into a second semantic database.

## TODO

- Define which IR levels may retain exact Contract references.
- Define reference preservation obligations during lowering and optimization.
- Make transforms invalidate or preserve provenance explicitly.
- Keep IR-level analysis metadata outside the authority world.
- Reuse one IR generation across read-only consumers where they share the same semantic level.
- Create a new IR level only when semantic vocabulary, invariants, or equivalence actually change.

---

# 18. Realization Acquisition Is a Separate Frontend

The Contract frontend and realization acquisition should remain separate compiler subsystems.

```text
Contract Frontend
    .kontrakt
    → syntax
    → resolution
    → HIR
    → establishment

Realization Frontend
    JVM realization
    → method/type acquisition
    → CFG
    → call/effect/origin knowledge
```

Kotlin or Java implementation topology does not participate in Contract name resolution.

The generated Interface/Operation surface may identify the realization root. The host artifact remains a realization bridge, not Contract authority.

Current design direction also removes KSP from the compiler core path. KSP may remain optional Kotlin tooling if later needed, but core Contract semantics and realization verification should not depend on KSP symbol objects.

## TODO

- Audit old TODO documents that still present KSP as a core V2 frontend.
- Define classfile or equivalent JVM realization acquisition as the core realization boundary.
- Keep optional source tooling replaceable behind that boundary.

---

# 19. V1 Foundation Tasks

The following work should be completed before the semantic substrate becomes difficult to change.

## P0 — Semantic boundary

- Reconcile `Canonical Contract World` and `Established Contract World` terminology.
- Define the exact boundary between Resolved Contract HIR and Establishment.
- Preserve ADR-0063 Definition, Occurrence, Judgment, Applicability, and authority distinctions.
- Prevent a universal Established Material type from becoming a second Contract model.

## P0 — Consumer architecture

- Make verifier, diagnostics, PBT, Reference Judgment, and execution formation consume the same authoritative semantic substrate.
- Prevent product-to-product authority chains.
- Define exact compact references from downstream material back to Contract authority.

## P0 — Query seam

- Use query-oriented orchestration for major compiler products.
- Record V1 dependencies in memory.
- Keep cache and query state non-authoritative.
- Keep semantic and provenance invalidation distinguishable.

## P0 — Provenance

- Build a real SourceManager and provenance service.
- Keep provenance outside semantic identity.
- Preserve compact origin handles across compiler boundaries where diagnostics can arise.

## P0 — Determinism

- Ensure clean, repeated, cached, and parallel compilation establish the same Contract meaning.
- Ensure worker completion order does not decide publication order or semantic identity.

## P1 — Physical core

- Prefer primitive tables and dense generation-local references for high-cardinality semantic data.
- Remove object-per-semantic-entity APIs from hot compiler paths.
- Keep storage layout hidden behind Contract World access surfaces.

## P1 — Debug and verification

- Add world verifier checks.
- Add deterministic world dump/debug format.
- Add structured tracing for query dependencies and invalidation.

---

# 20. V2 Extension Tasks

V2 should extend the V1 contracts rather than replace them.

The main V2 work is incremental and persistent realization.

- Persistent semantic query cache.
- Persistent dependency records.
- Red/green validation.
- Early cutoff.
- Incremental parsing and source revision tracking.
- Lazy HIR and semantic materialization.
- Persistent world segments or equivalent semantic snapshots.
- Concurrent compiler generations for IDE/build use.
- Safe reclamation of old generations.
- Parallel demand scheduling.
- Persistent diagnostics with independent semantic and provenance invalidation.
- Incremental verification reuse.
- Incremental PBT/test-plan reuse.
- Persistent IR or summary products only where their schema and validity rules are explicit.
- Schema versioning and corruption detection for every persisted compiler format.

None of these mechanisms may redefine Contract meaning.

---

# 21. Testing Requirements

The Contract World boundary needs dedicated tests because errors here can contaminate every downstream product.

At minimum, test these equivalences:

```text
clean build
    ==
incremental build

cold cache
    ==
warm cache

1 worker
    ==
N workers
```

Also test source-only changes separately from semantic changes.

One useful case is a comment inserted above a declaration. The source span changes, but the established semantic definition should remain reusable when its meaning is unchanged.

Another useful case is a Contract Version change. The source range may stay fixed while semantic products that depend on that Version must invalidate.

## TODO

- Golden vectors for semantic identity and canonical encoding.
- World linking identity tests.
- Query dependency tests.
- Incremental invalidation tests.
- Provenance-only invalidation tests.
- Cache-disabled versus cache-enabled equivalence tests.
- Parallel determinism tests.
- Malformed/corrupt persistent cache rejection tests.
- Differential tests between reference and production world readers where useful.

---

# 22. Documentation Audit Required

Several older architecture documents were written before the current Establishment and IR distinctions were clear.

They must be reviewed without deleting still-valid content.

## ADR-0063

Keep it as the semantic owner of Establishment, Established Material, Definition, Occurrence, Applicability, and Canonical Contract World meaning.

Do not move query, storage, or IR implementation details into this ADR.

## ADR-0070

Review the candidate diagram that uses `Established Contract World`.

Clarify its relationship to ADR-0063 `Canonical Contract World` before acceptance.

Keep the Contract authority side separate from the Realization axis.

## ADR-0061

Keep compiler diagnostics as a consumer of structured semantic and provenance products.

Update any wording that implies a generic Semantic IR is itself Contract authority.

## V2 Reference Architecture TODO

Review the old sequence:

```text
Semantic Contract IR
Canonical Contract Material
Linked Contract World
Lowered Contract-Machine IR
```

Replace only the parts that conflict with the new authority/IR distinction.

The V2 quality bar remains useful: query granularity, incremental reuse, diagnostics, persistent identity, IR verification, reproducibility, and parallel determinism should remain.

The Query Seam section should also be reviewed because current design work now favors dependency recording in V1 rather than waiting until V2.

## V1 Commercial Compiler Foundation TODO

Keep SourceManager, real frontend stages, stable identity, diagnostics, query/dependency infrastructure, IR verification, QA, and reproducibility goals.

Audit old `Semantic Contract IR` terminology and any architecture that treats KSP as a core Contract source.

## Frontend / Refactor TODOs

Replace legacy runtime/test metamodel assumptions with the new source → HIR → Establishment direction.

Do not introduce Kotlin object identity as the canonical semantic model.

## IR Subsystem Design Memo

Add the Contract World as an upstream semantic authority substrate that IRs may reference but must not replace.

Keep the existing law that IR meaning and physical representation are separate.

---

# 23. Questions That Must Be Closed Before Implementation Freezes

The next design review should answer these questions directly.

### Naming

Is `Established Contract World` the same logical boundary as ADR-0063 `Canonical Contract World`?

If not, what new semantic responsibility justifies a second world?

### Contents

Does the world contain only Established Definition Material, as ADR-0063 currently states?

Where does authority-owned Established Occurrence Material live when it exists?

### HIR boundary

Which semantic work is complete before HIR publication, and which work belongs only to Establishment?

### Linking

Does linking only preserve and connect already-established source definitions, or does a higher-scope owning law establish new composed meaning?

The two cases must stay separate.

### Applicability

Which applicability relations are stored directly because they are authoritative, and which are recomputed or derived for a consumer?

### Query granularity

What is the smallest useful stable query product for Definition, Occurrence, Operation, Core, and Whole-Machine work without creating excessive query overhead?

### Persistence

Which V1 world products are worth persisting in V2, and what exact validity proof is required before reuse?

### Diagnostics

Which source references are sufficient for rich diagnostics without retaining large object graphs or full IR histories?

### Realization bridge

What exact artifact binds an established Operation to the JVM realization root without making the generated host artifact authoritative?

---

# 24. Proposed Exit Criteria

This TODO can be considered closed when all of the following are true.

- `Canonical Contract World` and `Established Contract World` terminology has one unambiguous documented relation.
- HIR is clearly defined as intermediate frontend representation rather than Contract authority.
- Established Definition Material has one authoritative compiler substrate that downstream products can consume directly.
- Established Occurrence Material is represented only where owning semantics require it.
- No universal Established Material object model exists.
- Contract semantic relations, realization graphs, and query dependencies are separate.
- Source provenance is independently addressable and independently invalidatable.
- V1 query orchestration and in-memory dependency recording have a defined boundary.
- V2 persistent reuse can be added without changing Contract semantics or rewriting major compiler subsystem interfaces.
- High-cardinality semantic storage does not require JVM object identity.
- Verifier, diagnostics, PBT, Reference Judgment, and execution formation can share the same authoritative semantic substrate.
- IR levels reference Contract authority without becoming the authority store themselves.
- Clean, cached, incremental, and parallel compilation have explicit equivalence tests.
- Old TODO and ADR terminology has been audited against ADR-0063 and ADR-0070.

---

# 25. Working Architecture Summary

The current strongest candidate is:

```text
                        CONTRACT FRONTEND

.kontrakt
    ↓
Source / Syntax
    ↓
Resolution
    ↓
Resolved Contract HIR
    ↓
Authority-owned Establishment
    ↓
════════════════════════════════════════════
              CONTRACT AUTHORITY SUBSTRATE

Canonical Contract World
    Established Definition Material
    exact authority relations
    exact semantic references
    provenance handles
    applicability / resolved basis relations where owned

    ├────────→ Verifier
    ├────────→ Diagnostics
    ├────────→ PBT / Test Synthesis
    ├────────→ Reference Judgment
    └────────→ Execution Formation

════════════════════════════════════════════
                    REALIZATION

User JVM realization
    ↓
Realization Acquisition
    ↓
Realization IR
    ↓
Core Realization Verification
    ↓
Execution IR
    ↓
Optimization
    ↓
JVM IR
    ↓
JVM Product
```

Cross-cutting compiler realization:

```text
Query / Dependency Engine
Source Provenance
Stable Identity / HID Lookup
IR Analysis / Passes
Diagnostics
Testing / Differential Verification
Deterministic Publication
Incremental Fingerprints
```

These cross-cutting systems may observe and reuse Contract meaning.

They do not establish Contract authority merely because they compute, cache, verify, serialize, or publish a representation of it.

---

# 26. Final Rule

The key distinction to preserve is:

> **HIR represents resolved declarations on the way to authority. The Canonical Contract World represents already-established authoritative Contract definition meaning. Realization and Execution IRs represent how that meaning is verified, realized, transformed, and lowered. Query, cache, HID, provenance, and physical storage make those products efficient and reusable, but none of them becomes Contract authority.**

V1 should freeze this separation before implementation APIs spread object topology or old IR terminology across the compiler.

V2 should deepen reuse around the same boundary rather than introduce a second semantic compiler.
