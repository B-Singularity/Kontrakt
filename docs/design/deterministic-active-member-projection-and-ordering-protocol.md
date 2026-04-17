# Design: Deterministic Active Member Projection and Ordering Protocol

- Status: Draft
- Date: 2026-04-16
- Scope: Planning-domain projection, ordering, traversal-input freezing, and raw-fact boundary obligations
- Audience: Planning Core, Metamodel Adapters, Interner Boundary, Runtime / Verification
- Normative Dependencies:
    - ADR-0030: Edge-Aware Deterministic Cycle Truncation Strategy
    - ADR-0031: Two-Tier Transactional Memoization and Structural Interning

---

## 1. Purpose

This design note defines the **deterministic protocol** that transforms raw structural type facts into the frozen,
ordered traversal input consumed by the Planning Core.

It exists because the current architecture already commits to the following:

- the Active Member Set means:
    - the parameters of one deterministically selected constructor,
    - plus all eligible properties;
- uniqueness must be verified inside that Active Member Set;
- ordering must follow the canonical comparator rules;
- the Core must not trust arbitrary adapter list order;
- and the outbound fact boundary must remain a raw normalized fact boundary, not a semantic-choice boundary.

Those commitments are already architecturally correct, but they are not yet closed as one end-to-end protocol.
This document closes that gap.

---

## 2. Design Goals

### 2.1 Deterministic planning semantics

For one fixed:

- root type,
- normalization/version tuple,
- capability profile,
- entropy version,
- and runtime policy epoch,

the system MUST produce the same:

- constructor choice,
- property eligibility result,
- Active Member Set,
- canonical ordering,
- traversal sequence,
- cycle-break candidate set,
- canonical key inputs,
- and planner-visible diagnostic evidence order.

### 2.2 Boundary clarity

This protocol must preserve clear boundaries among:

- raw fact transport,
- semantic choice,
- canonical ordering,
- traversal consumption,
- memoization/interner key issuance,
- and runtime lifecycle concerns.

### 2.3 Compiler-style structure

The protocol must fit the compiler-style frame machine already used by planning:

- raw fact resolution occurs once,
- semantic selection occurs once,
- ordering occurs once,
- traversal consumes a frozen ordered set,
- and rollback must not recompute semantic choice for the same expansion episode.

### 2.4 Cache-blind semantics

Tier-2 cache state, join timing, publication timing, callback timing, worker assignment, and fresh-session restart
timing MUST NOT alter:

- Active Member Set contents,
- canonical ordering,
- breakpoint candidate ordering,
- or semantic traversal choice.

---

## 3. Non-Goals

This document does NOT define:

- the final concrete metamodel adapter implementation strategy for each backend,
- the final code-level package reshuffle,
- the final lifecycle implementation for fresh-session restart,
- or the final exact telemetry sink schema.

Those belong to implementation work and adjacent runtime design notes.

---

## 4. Vocabulary

### 4.1 Raw Structural Facts

Raw structural facts are normalized, version-bound facts obtained from the outbound fact boundary.

Examples:

- owner type identity
- constructor candidate descriptors
- constructor parameter descriptors
- property descriptors
- nullability certainty
- default-value presence
- origin provenance
- declaration ordinal availability
- signature provenance/version

Raw structural facts are **not** semantic planning decisions.

### 4.2 Projected Active Member Set

The Projected Active Member Set is the semantic result of:

1. deterministic constructor selection,
2. property eligibility evaluation,
3. capability-based demotion,
4. and projection of selected constructor parameters + eligible properties.

This set is still unordered until canonical ordering ratification has completed.

### 4.3 Ordered Active Member View

The Ordered Active Member View is the frozen traversal input produced after:

1. Active Member Set projection,
2. uniqueness verification,
3. canonical ordering ratification.

Traversal MUST consume this view only.

### 4.4 Semantic Choice Boundary

The semantic choice boundary owns:

- constructor selection,
- property eligibility/demotion,
- Active Member Set projection,
- uniqueness verification,
- canonical ordering ratification.

This boundary belongs to the Planning Core.

### 4.5 Raw Fact Boundary

The raw fact boundary owns:

- normalized structural fact extraction,
- backend-specific reconciliation,
- explicit unavailable sentinel emission,
- precomputed routing/identity facts such as `nodeIdentity64`.

This boundary belongs to outbound adapters.

---

## 5. Existing Type Contract: `TypeReference`

This document uses `TypeReference` as an **existing immutable metamodel-domain value object**, not as a raw adapter
string.

Normative rule:

- `TypeReference` MUST already be normalized and version-bound when it crosses the raw-fact boundary;
- the Core MUST NOT normalize raw type strings itself;
- adapters MUST construct `TypeReference` using the currently ratified normalization law before emitting DTOs;
- any signature material exposed through or alongside `TypeReference` MUST be deterministic and bound to the active
  signature-normalization version surface.

Implication:

- if a backend discovers raw type text,
  normalization belongs to the adapter before DTO emission;
- the Core consumes `TypeReference` as already-normalized semantic fact material.

Reason:

Without this rule, DTO schema and normalization responsibility would conflict.

---

## 6. Architectural Boundary Model

### 6.1 Fact Boundary

`TypeFactsProvider` returns normalized raw structural facts only.

It must not return:

- already-selected Active Member Sets,
- pre-demoted capability results,
- finalized planner ordering decisions,
- memoization/interner key issuance artifacts,
- or semantic-choice shortcuts.

### 6.2 Semantic Choice Boundary

The Planning Core owns all semantic decisions over the raw facts.

Those decisions include:

- constructor selection,
- property eligibility/demotion,
- Active Member Set projection,
- uniqueness verification,
- canonical ordering.

### 6.3 Traversal Boundary

Traversal never consumes raw facts directly.

Traversal consumes only the frozen ordered active-member view.

### 6.4 Interner Boundary

Memoization/interner key issuance occurs after semantic choice and semantic assembly inputs are known.

The interner boundary must not influence constructor selection, property eligibility, or ordering.

### 6.5 Runtime Boundary

Fresh-session restart, joined wait, dispatch lanes, and callback timing are runtime concerns only.

They must not alter the semantic projection or ordering protocol defined here.

---

## 7. Deterministic Protocol Overview

For one node-expansion episode, the protocol is:

1. resolve raw normalized structural facts
2. derive or validate the stripped node identity
3. perform deterministic constructor selection
4. evaluate eligible properties against capability profile
5. project the Active Member Set
6. verify uniqueness inside that Active Member Set
7. ratify canonical ordering
8. freeze the ordered member view
9. hand the frozen ordered view to traversal
10. forbid any later semantic recomputation for that same expansion episode

This protocol executes before child expansion begins.

---

## 8. Deterministic Execution Timing

### 8.1 Selection Point

Selection occurs immediately after fact resolution and before any child traversal.

Forbidden:

- selecting constructor lazily during expansion,
- projecting properties after some children have already been materialized,
- revisiting semantic member choice based on cache/publication timing.

### 8.2 Projection Freeze Point

Once the ordered active-member view is constructed for one expansion episode, it is frozen.

Rollback MAY restore traversal position.
Rollback MUST NOT mutate or recompute:

- constructor selection,
- property eligibility result,
- canonical ordering,
- or the frozen ordered active-member view

for that same expansion episode.

### 8.3 Traversal Consumption Point

Traversal frames consume only the frozen ordered active-member view.

Raw structural facts must not remain the live traversal authority once the view has been frozen.

---

## 9. Target DTO Model

The current DTO surface is insufficient to close all deterministic obligations.
The target DTO model therefore separates constructor candidates from property facts explicitly.

### 9.1 TypeFactsDTO

````kotlin
class TypeFactsDTO private constructor(
    val nodeIdentity64: Long,
    val ownerTypeFqcn: String,
    val normalizationVersion: Long,
    val constructors: List<ConstructorCandidateFact>,
    val properties: List<PropertyFact>,
)
````

### 9.2 ConstructorCandidateFact

`````kotlin
class ConstructorCandidateFact private constructor(
    val ownerTypeFqcn: String,
    val constructorSignature: String,
    val constructorSignatureNormalizationVersion: Long,
    val declarationOrdinal: DeclarationOrdinal,
    val visibility: VisibilityKind,
    val origin: MemberOrigin,
    val parameters: List<ConstructorParameterFact>,
)
`````

### 9.3 ConstructorParameterFact

````kotlin
class ConstructorParameterFact private constructor(
    val ownerTypeFqcn: String,
    val name: String,
    val typeReference: TypeReference,
    val parameterIndex: Int,
    val nullability: NullabilityKind,
    val defaultValuePresence: DefaultValuePresence,
    val typeSignatureNormalizationVersion: Long,
)
````

### 9.4 PropertyFact

`````kotlin
class PropertyFact private constructor(
    val ownerTypeFqcn: String,
    val name: String,
    val typeReference: TypeReference,
    val declarationOrdinal: DeclarationOrdinal,
    val nullability: NullabilityKind,
    val declaredVisibility: VisibilityKind,
    val setterVisibility: VisibilityKind?,
    val origin: MemberOrigin,
    val mutability: PropertyMutability,
    val storageKind: PropertyStorageKind,
    val typeSignatureNormalizationVersion: Long,
)
`````

---

## 10. Required Closed Vocabularies

The DTO family must not hide uncertainty behind ad hoc defaults.

### 10.1 NullabilityKind

````kotlin
enum class NullabilityKind {
    NON_NULL,
    NULLABLE,
    UNKNOWN,
}
````

### 10.2 DefaultValuePresence

````kotlin
enum class DefaultValuePresence {
    PRESENT,
    ABSENT,
    UNKNOWN,
}
````

### 10.3 VisibilityKind

````kotlin
enum class VisibilityKind {
    PUBLIC,
    PROTECTED,
    INTERNAL,
    PRIVATE,
    UNKNOWN,
}
````

### 10.4 PropertyMutability

````kotlin
enum class PropertyMutability {
    READ_ONLY,
    MUTABLE,
    UNKNOWN,
}
````

### 10.5 PropertyStorageKind

````kotlin
enum class PropertyStorageKind {
    BACKING_FIELD,
    LATEINIT,
    DELEGATED,
    COMPUTED,
    UNKNOWN,
}
````

### 10.6 MemberKind

This is not a raw adapter fact.
It is a planner-semantic classification used when assembling canonical ordering inputs.

````kotlin
enum class MemberKind {
    CTOR_PARAM,
    PROPERTY,
}
````

### 10.7 ProjectionSourceKind

````kotlin
enum class ProjectionSourceKind {
    SELECTED_CONSTRUCTOR_PARAMETER,
    ELIGIBLE_PROPERTY,
}
````

### 10.8 DeclarationOrdinalAvailability

`````kotlin
enum class DeclarationOrdinalAvailability {
    PRESENT,
    UNAVAILABLE,
}
`````

### 10.9 DeclarationOrdinal

`DeclarationOrdinal` is a semantic fact, not a raw sentinel integer.

`````kotlin
class DeclarationOrdinal private constructor(
    val availability: DeclarationOrdinalAvailability,
    val ordinal: Int,
)
`````

Normative meaning:

* `availability == PRESENT` means `ordinal >= 0` is meaningful.
* `availability == UNAVAILABLE` means no deterministic declaration ordinal could be reconstructed.

Forbidden semantic interpretations of `UNAVAILABLE`:

* first declaration,
* zero,
* backend-native enumeration order,
* or invented fallback local order.

A compliant implementation MAY encode `UNAVAILABLE` as `-1` only at primitive lowering / hot-path storage boundaries.
That encoding is mechanical only and MUST NOT replace the semantic fact model.

---

## 11. Sentinel Law

The protocol requires explicit unavailable/unknown sentinels.

### 11.1 Declaration ordinal unavailable

`DeclarationOrdinal.Unavailable` means:

* the adapter could not deterministically reconstruct a declaration ordinal.

It does NOT mean:

* first declaration,
* zero,
* fallback ordinal,
* backend omission silently treated as ordinary order,
* or raw reflection / backend enumeration order treated as semantic truth.

A compliant implementation MAY lower `DeclarationOrdinal.Unavailable` to `-1` only at primitive storage or
ordering-lowering boundaries.

That encoding is mechanical only.
The semantic protocol fact remains `Unavailable`.

### 11.2 Unknown nullability

`UNKNOWN` nullability is a first-class fact.

It must not be silently collapsed into nullable or non-nullable.

### 11.3 Unknown default-value presence

If a backend cannot tell whether a constructor parameter has a default value,
that uncertainty must be explicit.

### 11.4 Unknown visibility / writability facts

Where backend capability is insufficient,
that insufficiency must cross the fact boundary explicitly.

---

## 12. Source Artifact Reconciliation

Different backends may expose different raw discovery surfaces.

Examples:

- source/KSP-style analysis,
- reflection,
- bytecode scan,
- future compiler-plugin derived facts.

This protocol requires all backends to reconcile to one planning-relevant semantic fact surface.

### 12.1 Required reconciliation duties

An adapter must:

- normalize all names/signatures,
- reconcile declaration ordinals where possible,
- reconcile synthetic/member-origin meaning,
- emit unavailable sentinels where deterministic reconstruction is impossible,
- and never leak unstable backend iteration order into semantic planner meaning.

### 12.2 Reflection-specific requirement

Reflection enumeration order is **never admissible evidence** of stable declaration order.

Normative rule:

- if deterministic declaration order can be reconstructed from stronger metadata
  (for example bytecode order or equivalent stable source-derived metadata),
  the adapter MUST reconstruct it;
- if no deterministic reconstruction source exists in the active environment,
  the adapter MUST emit the unavailable declaration-order sentinel (`-1`);
- the adapter MUST NOT preserve raw reflection enumeration order as if it were stable.

### 12.3 Backend parity requirement

Equivalent semantic types under the same normalization/version tuple must converge to the same planning-relevant fact
meaning.

---

## 13. Capability Profile

Property eligibility and constructor admissibility depend on a Core-owned immutable capability profile.

This document does not freeze the final code shape,
but it freezes the required semantic content.

### 13.1 CapabilityProfile meaning

`CapabilityProfile` is the versioned domain policy surface that maps raw structural facts into planner-semantic
eligibility and demotion decisions.

It is identified by `capabilityProfileVersion`.

### 13.2 CapabilityProfile responsibility

At minimum, the capability profile must define deterministic rules for:

- constructor visibility admissibility,
- property visibility admissibility,
- mutable-property disposition,
- delegated-property disposition,
- computed-property disposition,
- lateinit-property disposition,
- inherited-property disposition where relevant,
- and unknown-fact handling.

### 13.3 CapabilityProfile boundary

`CapabilityProfile` is not provided by `TypeFactsProvider`.
It is a Core-owned policy input.

The adapter provides facts.
The Core evaluates those facts under the active capability profile.

### 13.4 CapabilityProfile version law

Any change to capability-profile behavior that can alter:

- constructor eligibility,
- property eligibility,
- demotion outcome,
- projected Active Member Set contents,
- canonical ordering input domain,
- or planner-visible diagnostics

MUST change `capabilityProfileVersion`.

---

## 14. Constructor Selection Protocol

The Core selects exactly one constructor candidate for one node-expansion episode.

### 14.1 Candidate eligibility

A constructor candidate is eligible only if it is:

- visible under the active capability profile,
- not synthetic,
- and not ruled ineligible by unknown-fact conservative law.

### 14.2 Deterministic selection tuple

The constructor selection tuple remains:

1. `#StrongSatisfiable`
2. `#DefaultAvailable`
3. `#NullableAvailable`
4. `#TotalParams`
5. `SignatureStability`

This document does not replace ADR-0030.
It defines the protocol inputs and closure rules that make that tuple deterministic.

### 14.3 Required facts for constructor selection

To evaluate the tuple deterministically, the Core must have explicit access to:

- parameter nullability certainty,
- default-value presence,
- constructor visibility,
- constructor origin,
- constructor signature identity and version.

### 14.4 SignatureStability definition

`SignatureStability` is the final deterministic comparison dimension for constructor selection.

Normative definition:

- compare the **normalized constructor signature** using deterministic lexicographic byte order over its normalized
  UTF-8
  form;
- this comparison occurs only after dimensions 1-4 are equal;
- locale-sensitive or JVM-incidental string comparison behavior MUST NOT be used.

### 14.5 Final tie closure

If two constructor candidates remain equal across the full deterministic selection tuple, including
`SignatureStability`,
constructor selection MUST fail closed.

Normative rule:

- there is no hidden post-signature tie-break;
- such a tie is an **AmbiguousConstructorSelection** condition.

Reason:

A constructor tie that survives the full deterministic selection tuple is semantic ambiguity, not an ordering problem.

---

## 15. Unknown Fact Conservative Rules

Unknown facts must follow explicit conservative rules.

### 15.1 Unknown nullability

As already ratified by ADR-0030:

- `UNKNOWN` nullability is treated as `STRONG` for the purpose of cycle-break and capability reasoning.

### 15.2 Unknown default-value presence

For constructor selection metrics:

- `DefaultValuePresence.UNKNOWN` counts as **not default-available**.

Reason:

Default-availability is an enabling fact.
Unknown cannot be treated as a positive capability.

### 15.3 Unknown constructor visibility

If constructor visibility is `UNKNOWN`:

- the constructor is ineligible unless an explicitly versioned capability profile ratifies otherwise.

### 15.4 Unknown property access / mutability / storage

For the default conservative rule set:

- unknown property visibility,
- unknown mutability,
- unknown storage kind,
- or unknown writability-relevant fact

MUST demote that property to `IGNORED` unless an explicitly versioned capability profile ratifies a different
deterministic disposition.

### 15.5 Unknown declaration ordinal

If declaration ordinal is unavailable:

- retain sentinel `-1`,
- do not invent fallback local ordinals,
- and let canonical ordering consume the sentinel under the documented ordering law.

---

## 16. Property Eligibility and Demotion Protocol

Property eligibility remains a Core-owned semantic decision.

### 16.1 Property classes

The protocol distinguishes at minimum:

- read-only properties,
- mutable properties,
- delegated properties,
- computed properties,
- lateinit properties,
- inaccessible properties,
- inherited vs declared properties,
- and unknown/unavailable cases.

### 16.2 Capability-based demotion

Capability profile evaluation may demote a property from:

- otherwise-eligible
- to ignored

but this demotion must be:

- deterministic,
- recorded,
- and diagnostic-order-stable.

### 16.3 Unknown fact handling

Unknown mutability/storage/access facts MUST follow the explicit conservative rules of Section 15.
They must not become backend-specific guesses.

---

## 17. Active Member Projection Protocol

Once:

- one constructor has been selected,
- and eligible properties have been classified,

the Core projects the Active Member Set.

### 17.1 Projected set contents

The Active Member Set consists of:

- the parameters of the selected constructor,
- plus all eligible properties.

### 17.2 ProjectedActiveMember schema

Projection converts raw constructor-parameter facts and raw property facts into one planner-facing active-member model.

`````kotlin
class ProjectedActiveMember private constructor(
    val ownerTypeFqcn: String,
    val memberKind: MemberKind,
    val name: String,
    val typeReference: TypeReference,
    val typeSignatureNormalizationVersion: Long,
    val declarationOrdinal: DeclarationOrdinal,
    val nullability: NullabilityKind,
    val sourceRef: ProjectionSourceRef,
)
`````

### 17.3 ProjectionSourceRef schema

````kotlin
sealed interface ProjectionSourceRef {

    class SelectedConstructorParameterRef private constructor(
        val constructorSignature: String,
        val constructorSignatureNormalizationVersion: Long,
        val constructorDeclarationIndex: Int,
        val parameterIndex: Int,
        val defaultValuePresence: DefaultValuePresence,
        val origin: MemberOrigin,
        val visibility: VisibilityKind,
    ) : ProjectionSourceRef

    class EligiblePropertyRef private constructor(
        val propertyDeclarationIndex: Int,
        val origin: MemberOrigin,
        val declaredVisibility: VisibilityKind,
        val setterVisibility: VisibilityKind?,
        val mutability: PropertyMutability,
        val storageKind: PropertyStorageKind,
    ) : ProjectionSourceRef
}
````

### 17.4 `DeclarationOrdinal` meaning in projected members

`ProjectedActiveMember.declarationOrdinal` is unified but interpreted deterministically:

* for `CTOR_PARAM`, it represents the constructor-parameter ordinal within the selected constructor;
* for `PROPERTY`, it represents the source-stable property declaration ordinal;
* if a deterministic ordinal cannot be reconstructed, it MUST remain `Unavailable`.

The semantic protocol MUST carry this as `DeclarationOrdinal`, not as an invented fallback integer.

### 17.5 Projection is semantic, not adapter-owned

Adapters do not project the Active Member Set.
Projection belongs to the Core because it is a semantic choice boundary.

---

## 18. Uniqueness Verification Protocol

Uniqueness verification occurs after projection and before traversal.

### 18.1 CanonicalEdgeKey uniqueness

Inside the Active Member Set,
duplicate `CanonicalEdgeKey` is a fail-fast ambiguity.

### 18.2 EntropyTargetKey uniqueness

Inside the Active Member Set,
duplicate `EntropyTargetKey` is also a fail-fast ambiguity.

### 18.3 No ordering-based ambiguity masking

Ordering is not permitted to "pick a winner" among colliding members.

A deterministic order over an ambiguous set is still an ambiguous model.

---

## 19. Canonical Ordering Ratification

Ordering occurs after projection and uniqueness verification.

### 19.1 Primary canonical tuple

The canonical ordering tuple remains:

1. `MemberKind` (`CTOR_PARAM < PROPERTY`)
2. `Name`
3. `Full Normalized TypeSignature`
4. `DeclarationOrdinal` under the following rule:

* `Present(n)` is ordered by ascending `n`
* `Unavailable` is ordered by the protocol's unavailable-ordinal rule only

`Unavailable` is a semantic fact.
A primitive implementation MAY encode that unavailable-ordering position using sentinel `-1`,
but the semantic protocol MUST remain expressed in terms of `DeclarationOrdinal`.

### 19.3 Full-tuple collision rule

If two projected active members are equal across the full primary canonical tuple, including the
`DeclarationOrdinal` availability/value rule above, that condition is **always** semantic ambiguity and MUST fail
closed.

Normative rule:

* there is no hidden post-tuple semantic tie-break;
* any further deterministic ordering used for diagnostics is non-semantic and must not rescue traversal legality.

### 19.2 Ordering scope

Ordering applies only to the projected, already-distinct Active Member Set.

It does not apply to:

- all constructors globally,
- all properties globally,
- or backend raw discovery order.

### 19.3 Full-tuple collision rule

If two projected active members are equal across the full primary canonical tuple, that condition is **always**
semantic ambiguity and MUST fail closed.

Normative rule:

- there is no hidden post-tuple semantic tie-break;
- any further deterministic ordering used for diagnostics is non-semantic and must not rescue traversal legality.

### 19.4 CanonicalEdgeKey lowering boundary

The canonical tuple is semantic.
Lowering into `CanonicalEdgeKey` / `edgeRank` is deterministic, version-bound infrastructure-facing lowering.

The lowering boundary must not alter semantic ordering meaning.

---

## 20. Ordered Active Member View

After ordering, the Core materializes the frozen traversal input.

### 20.1 Invariants

The ordered active-member view MUST guarantee:

- exactly one deterministically selected constructor is represented,
- only eligible properties are represented,
- canonical order is already ratified,
- traversal consumes this view only,
- the view is immutable/frozen.

### 20.2 Traversal obligation

The planner must not consult raw structural lists once this view exists.

### 20.3 Rollback obligation

Rollback may restore traversal cursor state.
Rollback must not recompute the view for the same expansion episode.

---

## 21. Freeze Enforcement Mechanism

The prohibition against recomputation is enforced structurally, not merely by comment.

### 21.1 One-way frame transition

The planner must use a one-way transition model equivalent to:

1. `PlanNodeFrame` owns raw fact resolution,
2. projection/order completes,
3. the runtime transitions into an `IterateMembers`-class frame carrying only the frozen ordered view.

There must be no lawful transition back from the traversal frame to raw-fact semantic choice for the same expansion
episode.

### 21.2 Raw fact drop rule

After the ordered active-member view has been frozen:

- raw `TypeFactsDTO` and raw constructor/property candidate collections MUST NOT remain retained as live traversal
  authority in the frame state.

### 21.3 Immutable ordered view rule

`OrderedActiveMembers` (or equivalent) must be immutable by construction.

Traversal may move:

- cursor/index,
- child materialization cursor,
- rollback pointer,

but not the frozen member ordering itself.

### 21.4 Verification consequence

Tests must verify that:

- rollback restores cursor only,
- no code path re-enters projection/order for the same expansion episode,
- and no stale raw-fact reference can rewrite traversal order after freeze.

---

## 22. Primitive Lowering Rule for DeclarationOrdinal

The semantic protocol uses `DeclarationOrdinal`.

Primitive hot-path structures MAY lower that fact into an integer representation for storage or comparator execution.

Allowed lowering:

* `Present(n)` -> `n`
* `Unavailable` -> `-1`

Constraints:

* this lowering MUST be local to primitive implementation concerns;
* semantic DTOs and semantic protocol law MUST continue to speak in terms of `DeclarationOrdinal`;
* no implementation may reinterpret `-1` as ordinary declaration order.

Reason:

Kontrakt requires explicit semantic law with primitive-friendly mechanical realization.

## 23. Diagnostic Determinism

Diagnostics are part of protocol behavior and must be deterministic.

### 23.1 Applies to

- demotion records,
- collision evidence,
- constructor candidate tie evidence where surfaced,
- property rejection evidence,
- truncation evidence,
- candidate lists,
- and equivalent future planner-visible evidence sets.

### 23.2 Ordering requirement

Every evidence family must define one deterministic ordering rule before truncation/rendering.

### 23.3 Truncation requirement

Truncation must occur after deterministic ordering.
The `truncated` flag must reflect post-order truncation only.

### 23.4 Forbidden

- backend iteration order as evidence order,
- race timing as evidence order,
- unstable list construction as evidence order.

---

## 24. Iteration Stability

No semantic decision may depend on unstable iteration order.

### 24.1 Adapter-side rule

Adapters may use any internal data structure,
but unstable iteration order must not cross the fact boundary as semantic truth.

### 24.2 Core-side rule

Even if a backend offers apparently stable iteration order,
the Core must still treat it as non-authoritative for semantic traversal order.

### 24.3 End-to-end rule

Only the frozen ordered active-member view is authoritative traversal input.

---

## 25. Entropy Exclusion

Planning semantics must exclude non-semantic entropy.

Forbidden semantic inputs include:

- wall-clock time,
- random UUID generation,
- mutable global RNG streams,
- object identity / memory address effects,
- backend incidental iteration order,
- mutable global counters used as semantic choice inputs.

This law does not mean every identifier must be content-hash based.

It means:

- routing identity,
- semantic identity,
- local dense ordinals,
- and runtime episode identifiers

must remain explicitly separated.

---

## 26. Integer Arithmetic

Planning protocol arithmetic must remain integer-domain deterministic.

This includes:

- capacity thresholds,
- semantic work accounting,
- physical step accounting,
- ordering-lowering integer domains,
- threshold tables,
- and deterministic counters.

Floating-point arithmetic must not become a hidden semantic choice surface.

---

## 27. Mutable Global Contamination Prohibition

Mutable global state must not participate in semantic planning.

Forbidden:

- mutable static scratch state,
- singleton mutable planning residue,
- previous-run frontier residue,
- mutable global clocks/RNGs as semantic input,
- mutable global semantic-choice caches outside explicit runtime boundaries.

Allowed:

- immutable constants,
- immutable protocol tables,
- immutable version metadata,
- immutable snapshot registries.

All mutable planning state must remain inside explicit session/frame/runtime ownership.

---

## 28. Version Surface

The protocol depends on explicit version axes.

At minimum:

- `normalizationVersion`
- `typeSignatureNormalizationVersion`
- `edgeOrderingVersion`
- `capabilityProfileVersion`
- `entropyVersion`

Any change that alters:

- constructor selection meaning,
- property eligibility meaning,
- projection semantics,
- canonical ordering meaning,
- or entropy target semantics

must flow through an explicit version boundary.

### 28.1 Future projection-version axis

If constructor/property projection semantics evolve materially,
the system should introduce an explicit projection-related version axis rather than relying on undocumented behavioral
drift.

---

## 29. Transitional Compatibility with Current Code

The current code still exposes a simplified shape such as:

- `TypeFactsDTO.members`
- `MemberFact`
- `ActiveMemberOrderingGate.ratify(facts: TypeFactsDTO): OrderedActiveMembers`

This document treats that as a transitional surface only.

### 29.1 Transitional rule

As long as the current simplified surface remains:

- adapters must still behave as if richer raw facts existed,
- the Core must still preserve the laws in this document,
- and no implementation may use the simplified surface as an excuse to hide semantic choice inside adapters.

### 29.2 Required refactoring direction

The target direction is:

- richer raw DTO families,
- explicit projection law,
- explicit ordering law,
- frozen ordered traversal input,
- and removal of any boundary ambiguity between facts and semantic choice.

---

## 30. Expected Type / File Impact

This design implies future introduction or refactoring of types equivalent to:

- `TypeFactsDTO`
- `ConstructorCandidateFact`
- `ConstructorParameterFact`
- `PropertyFact`
- `ProjectedActiveMember`
- `ProjectionSourceRef`
- `OrderedActiveMembers`
- `NullabilityKind`
- `DefaultValuePresence`
- `VisibilityKind`
- `PropertyMutability`
- `PropertyStorageKind`
- `CapabilityProfile`
- `ActiveMemberProjectionLaw`
- `ActiveMemberOrderingLaw`

The exact packaging may evolve.
The boundary meaning defined here must not.

---

## 31. Migration Order

Recommended implementation order:

1. enrich raw DTO vocabularies with explicit sentinel-bearing facts
2. separate constructor candidates from property facts
3. introduce Core-owned projection law
4. introduce Core-owned ordering law
5. freeze ordered traversal input explicitly
6. update `StructuralPlannerCore` to consume the frozen ordered view
7. only then remove transitional compatibility surfaces

---

## 32. Compliance Tests

At minimum, the verification plan must include:

- constructor selection determinism across repeated runs
- constructor full-tuple tie failure tests
- property demotion determinism across repeated runs
- backend parity tests across multiple fact adapters
- reflection-order unreliability defense tests
- unavailable sentinel stability tests
- canonical ordering determinism tests
- full-tuple collision fail-fast tests
- diagnostic evidence ordering stability tests
- rollback freeze integrity tests
- hot/cold cache equivalence for projection/order/traversal semantics
- concurrency-blind semantic equivalence tests

---

## 33. Final Statement

Deterministic planning does not end at deterministic cache keys or deterministic interner publication.

It also requires deterministic:

- raw fact boundaries,
- constructor selection timing,
- constructor tie closure,
- property eligibility evaluation,
- projection timing,
- uniqueness enforcement,
- canonical ordering,
- traversal-input freezing,
- backend reconciliation,
- diagnostic evidence ordering,
- and unknown/unavailable fact treatment.

From this point onward, any implementation that leaves those steps implicit is non-compliant with the planning protocol.