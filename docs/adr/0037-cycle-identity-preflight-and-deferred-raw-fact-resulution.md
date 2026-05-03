# ADR-0037: Cycle Identity Preflight and Deferred Raw Fact Resolution

## Status

Proposed

## Date

2026-04-22

## Related

- ADR-0030: Edge-Aware Deterministic Cycle Truncation Strategy
- ADR-0031: Two-Tier Transactional Memoization and Structural Interning
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics
- ADR-0036: Joined-Wait Planning-Run Suspension Bridge and Fresh-Session Restart Authority
- `docs/design/deterministic-active-member-projection-and-ordering-protocol.md`
- `docs/design/l1-planner-session-primitive-data-structures.md`

## Context

Kontrakt's planning pipeline is being refactored from a broad legacy metamodel path into a staged, compiler-like
expansion pipeline.

The legacy shape was roughly:

``````text
TypeReference
-> TypeFactsProvider.resolveFacts(...)
-> CanonicalSignatureProvider.deriveSignature(facts)
-> PlannerSession.enterOrDetectCycle(...)
-> ActiveMemberOrderingGate.ratify(facts)
-> IterateMembersFrame
``````

During active-member refactoring, an intermediate shape was considered:

``````text
TypeReference
-> TypeShapeProvider.resolveTypeShape(...)
-> RawTypeFactsProvider.resolveRawFacts(...)
-> TypeExpansionPreflightDecision.CompositePreflight(rawFacts)
-> PlannerSession.enterOrDetectCycle(...)
-> ActiveMemberProjector.project(...)
-> ActiveMemberOrderer.order(...)
-> IterateMembersFrame
``````

That intermediate shape correctly separates raw facts from projected/ordered traversal input, but it is not optimal.

The problem is that `RawTypeFactsDTO` can be expensive to produce.

A raw type-fact DTO may require:

- constructor enumeration,
- constructor parameter enumeration,
- property enumeration,
- type-reference rendering for parameter/property types,
- visibility extraction,
- mutability extraction,
- storage-kind inference,
- declaration ordinal handling,
- raw-fact deterministic sequencing,
- duplicate detection,
- normalization validation.

However, active-cycle detection does not need those facts.

Cycle detection needs only stable type identity material:

- a normalized cycle identity,
- a primitive 64-bit identity for indexing/routing,
- an exact canonical signature for collision verification,
- algorithm id/version metadata for drift detection.

Therefore, resolving raw facts before cycle detection creates avoidable wasted work on cycle-hit paths.

Example:

``````text
A -> B -> A
``````

When the second `A` is encountered, the planner can determine that `A` is already active using cycle identity alone.
It should not first enumerate constructors and properties of `A`.

This ADR ratifies a stricter staged pipeline:

``````text
Shape
-> Cycle Identity
-> Active-Cycle Detection
-> Raw Facts
-> Active-Member Projection
-> Active-Member Ordering
-> Traversal
``````

## Problem

The planner must satisfy all of the following:

1. Detect active cycles deterministically.
2. Avoid resolving expensive raw metamodel facts on cycle-hit paths.
3. Avoid letting active-member ordering influence cycle identity.
4. Keep Reflection, KSP, bytecode, and static-source adapters replaceable.
5. Preserve deterministic breakpoint selection from ADR-0030.
6. Preserve the rule that traversal consumes only frozen ordered active members.
7. Preserve session accounting boundaries from ADR-0032.
8. Preserve the rule that `TypeExpansionPipeline` must not mutate `PlannerSession` directly.

The intermediate `CompositePreflight(rawFacts)` design violates item 2.

It also risks blurring identity and fact responsibilities:

- cycle identity answers: **"Who is this type for active-cycle detection?"**
- raw facts answer: **"What constructors/properties are available?"**
- projection answers: **"Which members are active under the current capability profile?"**
- ordering answers: **"What is the canonical traversal order?"**

Those stages must not collapse.

## Decision

Introduce a separate cycle-identity preflight stage.

The planner MUST use the following order for one node-expansion episode:

1. Resolve coarse type shape.
2. Resolve cycle identity.
3. Validate shape and cycle-identity subject continuity.
4. Enter or detect active cycle using cycle identity.
5. If active cycle is detected:
    - perform deterministic cycle truncation / breakpoint handling;
    - DO NOT resolve raw facts for the current cycle-hit type.
6. If no active cycle is detected:
    - resolve raw facts;
    - validate raw-fact subject continuity;
    - project active members;
    - order active members;
    - freeze ordered traversal input;
    - enter member traversal.

Normative pipeline:

``````text
TypeReference
-> TypeShapeProvider.resolveTypeShape(reference)
-> TypeCycleIdentityProvider.resolveCycleIdentity(reference)
-> PlannerSession.enterOrDetectCycle(identityBits64, canonicalSignature)
-> if cycle hit:
       deterministic cycle truncation
       no RawTypeFactsProvider call
   else:
       RawTypeFactsProvider.resolveRawFacts(reference)
       ActiveMemberProjector.project(rawFacts, capabilityProfile)
       ActiveMemberOrderer.order(projection)
       IterateMembersFrame(OrderedActiveMembers)
``````

## Core Rule

Cycle detection MUST be driven by `TypeCycleIdentity`.

Cycle detection MUST NOT be driven by:

- `RawTypeFactsDTO`,
- constructor facts,
- property facts,
- active-member projection results,
- active-member ordering results,
- declaration ordinal,
- reflection enumeration order,
- KSP declaration order,
- capability profile,
- wall clock,
- UUID,
- object identity,
- adapter handle identity.

Raw type facts MUST be resolved only after active-cycle detection has concluded that the current type is not an
active-stack cycle hit.

## TypeCycleIdentity

### Required Schema

`TypeCycleIdentity` is a domain value object representing the minimal deterministic identity needed for active-cycle
detection.

Illustrative shape:

``````kotlin
class TypeCycleIdentity private constructor(
    val subject: TypeReference,
    val identityBits64: Long,
    val canonicalSignature: CanonicalSignature,
    val identityAlgorithmId: String,
    val identityAlgorithmVersion: Long,
)
``````

Required semantics:

- `subject`
    - the `TypeReference` this cycle identity belongs to;
    - must match the requested reference by id, cycle id, and signature continuity rules.

- `identityBits64`
    - primitive lowered identity used for hot-path indexing/routing;
    - must not be the only equality authority;
    - must be paired with `canonicalSignature`.

- `canonicalSignature`
    - exact canonical signature used to validate identity equality and protect against 64-bit collision;
    - must be stable across Reflection/KSP/bytecode/static-source adapters when the same canonical type is described.

- `identityAlgorithmId`
    - stable identifier for the identity derivation law.

- `identityAlgorithmVersion`
    - monotonic protocol version for identity derivation;
    - must change when the derivation law changes.

### TypeReference Ratification Dependency — AMENDED

`TypeCycleIdentity` is derived from an already-ratified `TypeReference`.

`TypeCycleIdentityProvider` is not a raw type-text normalization authority.

It MUST NOT:

* normalize raw type text;
* repair non-NFC text;
* inspect Unicode categories directly;
* call ICU4J / JDK normalization APIs from Planning Core;
* enumerate constructors;
* enumerate properties;
* perform active-member projection;
* perform active-member ordering.

Required dependency chain:

``````text
Raw adapter/source/reflection/KSP type text
-> NormalizationEngine.inspectCanonicalTypeText(...)
-> CanonicalTypeText.ratify(...)
-> TypeReference
-> TypeCycleIdentityProvider.resolveCycleIdentity(reference)
-> TypeCycleIdentity
``````

The provider may use adapter-local handles only behind the adapter boundary.

The value returned to Planning Core MUST be pure domain material:

``````text
TypeCycleIdentity(
    subject = TypeReference,
    identityBits64 = primitive routing identity,
    canonicalSignature = exact verification material,
    identityAlgorithmId = pinned algorithm id,
    identityAlgorithmVersion = pinned algorithm version
)
``````

Cycle identity continuity checks compare returned identity material against the requested ratified `TypeReference`.

A cycle identity provider that returns identity material for a different `TypeReference` must fail closed.

Reason:

Cycle detection asks:

> Is this already-ratified type identity active on the current planning stack?

It does not ask:

> How should raw source/backend type text be normalized?

### Required Provider

Introduce a new outbound port:

``````kotlin
interface TypeCycleIdentityProvider {
    fun resolveCycleIdentity(
        reference: TypeReference,
    ): TypeCycleIdentity
}
``````

Architectural role:

- Hexagonal driven port.
- Implemented by reflection, KSP, bytecode, or static-source adapters.
- Planning Core does not know which backend produced the identity.

Compiler-style role:

- Computes minimal identity material for active-cycle detection.
- Does not enumerate constructors.
- Does not enumerate properties.
- Does not project active members.
- Does not order active members.
- Does not create plan nodes.

## Identity Law

### Nullability

Cycle identity MUST strip usage-site nullability.

The following must map to the same cycle identity unless a later ADR explicitly ratifies a different law:

``````text
User
User?
``````

Reason:

Nullability changes whether a value may be absent.
It does not create a different active-cycle type for structural recursion detection.

### Generics

Generic arguments MUST be represented deterministically.

The following may map to different cycle identities:

``````text
Node<String>
Node<Int>
``````

Reason:

Generic arguments can change reachable child shape and therefore structural recursion.

Generic rendering must be canonical and adapter-independent.

Forbidden:

- adapter-native `toString()` as identity authority,
- reflection raw type name as identity authority,
- KSP raw symbol spelling as identity authority,
- iteration-order-dependent generic rendering.

### Type Aliases

Type aliases MUST either:

1. be resolved to their canonical target type, or
2. be explicitly encoded by a ratified normalization rule.

Implementations MUST NOT let Reflection and KSP disagree silently on alias handling.

### Platform Types

Platform nullability uncertainty MUST NOT affect cycle identity.

Platform nullability may affect raw facts and projection policy, but cycle identity uses the nullability-stripped type
identity law.

### Declaration Order

Declaration order MUST NOT participate in cycle identity.

Declaration order may participate in active-member ordering when available, but not in active-cycle detection.

### Capability Profile

Capability profile MUST NOT participate in cycle identity.

A type's active-member set can vary by capability profile, but the question "is this type currently active on the
stack?"
must remain stable across capability profiles.

## Adapter Independence

This ADR requires the same Planning Core algorithm to work under:

- Reflection adapter,
- KSP adapter,
- bytecode adapter,
- static-source adapter.

Adapters may differ in how they obtain identity material, but they must converge to the same `TypeCycleIdentity` under
the same canonical type identity law.

Reflection implementation example:

``````text
Reflection TypeReference
-> hidden KType handle
-> canonical TypeReference.cycleId/signature rendering
-> TypeCycleIdentity
``````

KSP implementation example:

``````text
KSP TypeReference
-> symbol table lookup
-> generated/precomputed canonical type identity
-> TypeCycleIdentity
``````

The Planning Core must depend only on:

``````kotlin
TypeShapeProvider
TypeCycleIdentityProvider
RawTypeFactsProvider
ActiveMemberProjector
ActiveMemberOrderer
``````

It must not depend on:

- reflection classes,
- KSP classes,
- bytecode parser classes,
- adapter-local handles.

## Adapter-Local Memoization

Adapters MAY memoize:

- type shape,
- cycle identity,
- raw facts.

However, adapter-local memoization is not semantic authority.

All returned values must still pass:

- DTO/value-object factory validation,
- subject-continuity checks,
- identity algorithm id/version checks,
- raw-fact continuity checks,
- planner session budget accounting.

Adapter caches MUST NOT be used to bypass domain validation.

## Staged Type Expansion Protocol

### Phase 1: Type Shape

Resolve coarse expansion shape:

``````text
TypeReference
-> ResolvedTypeShape
``````

The shape answers only:

``````text
What expansion strategy category does this type have?
``````

It must not contain:

- constructor facts,
- property facts,
- active members,
- ordered traversal view.

### Phase 2: Cycle Identity

Resolve minimal active-cycle identity:

``````text
TypeReference
-> TypeCycleIdentity
``````

This stage must be cheaper than raw-fact resolution.

It must not require constructor/property enumeration.

### Phase 3: Active-Cycle Detection

The core calls:

``````kotlin
PlannerSession.enterOrDetectCycle(
    identityBits = cycleIdentity.identityBits64,
    signature = cycleIdentity.canonicalSignature,
)
``````

If an active cycle is detected:

- the core performs ADR-0030 deterministic truncation / breakpoint logic;
- the core must not call `RawTypeFactsProvider` for the current cycle-hit type;
- the core must not project/order active members for the current cycle-hit type.

If no active cycle is detected:

- the core binds the incoming edge metadata at the current active depth;
- the core proceeds to raw-fact resolution.

### Phase 4: Raw Facts

Only after a cycle miss:

``````text
TypeReference
-> RawTypeFactsProvider.resolveRawFacts(reference)
-> RawTypeFactsResolution
``````

Raw-fact retrieval must distinguish:

- cache hit,
- actual resolution.

This remains governed by ADR-0032 and the Type Expansion cost-center family.

### Phase 5: Active-Member Projection

Only after raw facts pass continuity checks:

``````text
RawTypeFactsDTO
+ CapabilityProfile
-> ActiveMemberProjectionResult
``````

Projection is a semantic-also metered stage.

### Phase 6: Active-Member Ordering

Only after projection:

``````text
ActiveMemberProjectionResult
-> OrderedActiveMembers
``````

Ordering is a semantic-also metered stage.

### Phase 7: Traversal

Only `OrderedActiveMembers` may be handed to traversal frames.

Traversal frames must not retain:

- `RawTypeFactsDTO`,
- raw constructor collections,
- raw property collections,
- mutable projected member collections,
- active-member projection result as recomputation input.

## Continuity Checks

The pipeline MUST verify continuity at three boundaries.

### Shape Continuity

``````text
ResolvedTypeShape.subject == requested TypeReference
``````

At minimum:

- id must match,
- cycle id must match,
- signature must match.

### Cycle Identity Continuity

``````text
TypeCycleIdentity.subject == requested TypeReference
``````

And:

``````text
TypeCycleIdentity.identityAlgorithmId == pipeline identity algorithm id snapshot
TypeCycleIdentity.identityAlgorithmVersion == pipeline identity algorithm version snapshot
``````

### Raw Facts Continuity

``````text
RawTypeFactsDTO.typeIdentity64 == expected identity derived from requested TypeReference
RawTypeFactsDTO.typeIdentityAlgorithmId == pipeline identity algorithm id snapshot
RawTypeFactsDTO.typeIdentityAlgorithmVersion == pipeline identity algorithm version snapshot
``````

Raw facts continuity is checked only after the cycle miss path reaches raw-fact resolution.

## Metering

This ADR amends ADR-0032's Type Expansion metering boundary.

### New Required Cost Centers

The Type Expansion band must include:

- `TYPE_CYCLE_IDENTITY_RESOLUTION`
    - `BudgetTrack.PHYSICAL_ONLY`
    - emitted after cycle identity resolution succeeds.

- `TYPE_CYCLE_IDENTITY_CONTINUITY_CHECK`
    - `BudgetTrack.PHYSICAL_ONLY`
    - emitted after cycle identity subject/algorithm continuity check succeeds.

### Existing Cost Centers Retained

The following remain valid:

- `TYPE_SHAPE_RESOLUTION`
- `TYPE_SHAPE_LOWERING`
- `COMPOSITE_RAW_FACT_CACHE_HIT`
- `COMPOSITE_RAW_FACT_RESOLVE`
- `COMPOSITE_RAW_FACT_SUBJECT_CONTINUITY_CHECK`
- `COMPOSITE_ACTIVE_MEMBER_PROJECTION`
- `COMPOSITE_ACTIVE_MEMBER_ORDERING`
- `CONTAINER_EXPANSION_DECISION`
- `ATOMIC_EXPANSION_DECISION`

### Accounting Timing

Work events are recorded after the corresponding stage succeeds.

If a later stage fails, already-recorded successful work remains consumed.

Reason:

Runtime metering is monotonic.
Rollback and reset do not rewind physical or semantic counters.

### Failure Events

This ADR does not introduce failure-path cost centers.

Failure-path accounting must be added explicitly if needed.
Successful-stage events must not be pre-charged to approximate failure cost.

## Interaction with ADR-0030

ADR-0030 remains the authority for deterministic cycle truncation and breakpoint selection.

This ADR changes when raw facts and active-member ordering may be computed.

Required interaction:

- active-cycle detection uses `TypeCycleIdentity`;
- deterministic breakpoint selection still uses active-stack edge metadata;
- previously entered frames may already own `OrderedActiveMembers`;
- the current cycle-hit type does not need raw facts to detect the cycle.

Back-edge metadata must still be available on the incoming frame.

The current cycle-hit frame must carry enough incoming-edge metadata for ADR-0030 breakpoint comparison, including any
ratified edge rank / stage tag / member index fields required by the current breakpoint protocol.

## Interaction with ADR-0032

This ADR extends the Type Expansion cost family.

Type-cycle identity resolution is not L2 cache governance.

It must not be charged through `L2_CACHE`.

Cycle identity resolution and continuity checks are physical-only.

Raw-fact actual resolution, active-member projection, and active-member ordering remain semantic-also where already
ratified by ADR-0032 amendments.

## Interaction with Active-Member Projection Protocol

Active-member projection and ordering must not participate in active-cycle detection.

The active-member protocol begins only after:

1. type shape has been resolved,
2. cycle identity has been resolved,
3. active-cycle detection reports no active-stack cycle hit,
4. raw facts have been resolved or retrieved,
5. raw-fact continuity has been validated.

This prevents active-member selection policy from contaminating cycle detection.

## Non-Goals

This ADR does not define:

- final interface implementation-resolution policy,
- final abstract-class handling policy,
- full container/array/map traversal frame design,
- KSP generated metadata file format,
- adapter-local cache eviction policy,
- L2 interning behavior,
- deterministic breakpoint ranking rules from ADR-0030.

## Consequences

### Positive

- Cycle-hit paths avoid raw-fact resolution.
- Active-member projection/order are skipped entirely for cycle-hit nodes.
- Cycle detection no longer depends on declaration order.
- Cycle detection no longer depends on capability profile.
- Reflection and KSP can share the same Planning Core algorithm.
- Raw facts are only resolved when traversal can actually proceed.
- Budget diagnostics become cleaner:
    - shape,
    - cycle identity,
    - raw facts,
    - projection,
    - ordering
      are separately accountable.

### Negative

- One additional provider contract is introduced.
- Adapters must implement `TypeCycleIdentityProvider`.
- Continuity checks become stricter.
- The expansion pipeline has more phases.
- Incorrect adapter implementations may now fail earlier.

### Mitigations

- Reflection adapter can derive cycle identity from the existing reflection-backed `TypeReference` without scanning
  members.
- KSP adapter can precompute cycle identity from generated symbol metadata.
- Adapter-local memoization may avoid duplicate shape/identity/fact work.
- Strong continuity checks prevent adapter drift from reaching canonical planning.

## Implementation Impact

Expected new files:

``````text
planning/domain/port/outgoing/TypeCycleIdentityProvider.kt
planning/domain/expansion/TypeCycleIdentity.kt
planning/domain/expansion/TypeExpansionPreflightDecision.kt
metamodel/adapter/reflection/ReflectionTypeCycleIdentityProvider.kt
``````

Expected modified files:

``````text
planning/domain/expansion/TypeExpansionPipeline.kt
planning/domain/expansion/TypeExpansionWorkEvent.kt
planning/domain/expansion/TypeExpansionCostCenterMapper.kt
planning/domain/protocol/RequiredCostCentersSpec.kt
planning/domain/service/StructuralPlannerCore.kt
metamodel/adapter/reflection/ReflectionTypeShapeProvider.kt
metamodel/adapter/reflection/ReflectionRawTypeFactsProvider.kt
docs/adr/0030-edge-aware-deterministic-cycle-truncation-strategy.md
docs/adr/0032-capacity-law-resource-policy-resolution-identity-hierarchy-and-zero-residue-semantics.md
docs/design/deterministic-active-member-projection-and-ordering-protocol.md
docs/design/l1-planner-session-primitive-data-structures.md
``````

## Compliance Rules

A compliant implementation must satisfy:

1. `RawTypeFactsProvider` is not called on active-cycle-hit paths.
2. `ActiveMemberProjector` is not called on active-cycle-hit paths.
3. `ActiveMemberOrderer` is not called on active-cycle-hit paths.
4. Cycle detection is driven by `TypeCycleIdentity`.
5. Cycle identity excludes declaration order.
6. Cycle identity excludes capability profile.
7. Cycle identity strips nullability.
8. Cycle identity reifies generic arguments deterministically.
9. `TypeCycleIdentity.identityBits64` is paired with exact `CanonicalSignature`.
10. Identity algorithm id/version are snapshotted by the expansion pipeline.
11. Shape continuity is checked.
12. Cycle identity continuity is checked.
13. Raw facts continuity is checked only after cycle miss and raw-fact retrieval.
14. Type expansion metering records successful stages after success.
15. `TypeExpansionPipeline` does not depend on or mutate `PlannerSession`.

## Required Tests

Add tests for:

- cycle-hit path does not call `RawTypeFactsProvider`;
- cycle-hit path does not call `ActiveMemberProjector`;
- cycle-hit path does not call `ActiveMemberOrderer`;
- cycle identity nullability stripping:
    - `T` and `T?` map to the same cycle identity;
- generic identity distinction:
    - `Node<String>` and `Node<Int>` are distinct when generic arguments are part of the law;
- shape subject mismatch fail-fast;
- cycle identity subject mismatch fail-fast;
- cycle identity algorithm id mismatch fail-fast;
- cycle identity algorithm version mismatch fail-fast;
- raw facts identity mismatch fail-fast after cycle miss;
- work-meter event order:
    - shape resolution,
    - cycle identity resolution,
    - cycle identity continuity check,
    - cycle detection,
    - raw facts hit/resolve only on cycle miss,
    - projection only on cycle miss,
    - ordering only on cycle miss;
- no `PlannerSession` dependency in `TypeExpansionPipeline`;
- Reflection and KSP fixtures produce equivalent `TypeCycleIdentity` for the same canonical type.

## Open Questions

1. Should abstract classes receive a separate `TypeKind.ABSTRACT_COMPOSITE` later?
2. Should interface implementation resolution have its own ADR?
3. Should KSP generated metadata include precomputed `identityBits64` or only canonical signature material?
4. Should adapter-local cycle identity memoization be bounded by the same Type Expansion budget family?
5. Should raw-fact provider result include provider-generation epoch for stronger stale-data detection?

## Final Rule

Cycle detection MUST be identity-first and fact-lazy.

Raw facts, projection, and ordering are traversal preparation.
They are not prerequisites for determining whether the current type is already active on the stack.