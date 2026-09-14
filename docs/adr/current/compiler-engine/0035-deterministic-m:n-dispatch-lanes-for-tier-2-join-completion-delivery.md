# ADR-0035: Deterministic M:N Dispatch Lanes for Tier-2 Join Completion Delivery

- Status: Accepted
- Date: 2026-04-11
- Deciders: Kontrakt Planning/Runtime Architecture
- Supersedes: None
- Superseded by: None

## Context

Kontrakt Planning Tier-2 (L2) structural interning has already moved away from polling-era blocking waits.

The current architectural baseline is:

- joined waiting must remain non-blocking
- worker threads must not be monopolized by joined waits
- completion delivery must be adapter-owned infrastructure
- resumed work must proceed through fresh-session restart rather than stale worker-local session retention
- shared-slot lifecycle truth, waiter lifecycle truth, builder-handle lifecycle truth, commit-right truth, and region
  lifecycle truth must remain explicitly separated

This creates a new architectural requirement:

**we must define how joined-wait completion delivery is executed physically, without violating the existing logical
authority model.**

More specifically, we must decide how to map:

- shard-level routing and lifecycle authority
- joined-wait continuation registration
- terminal sweep delivery
- timeout delivery
- restart-ready callback execution

onto a physical execution model that remains deterministic, bounded, and operationally realistic on the JVM.

At this point, three broad classes of designs are possible:

1. a single global dispatch plane
2. a 1:1 shard-to-thread delivery model
3. a deterministic M:N lane model where shards remain logical authority units but delivery execution is grouped into a
   fixed number of adapter-owned lanes

We also explicitly reject a return to polling-era blocking or worker-thread wait ownership, because that would violate
the already-adopted non-blocking join / fresh-session restart direction.

## Decision

We adopt a **deterministic M:N dispatch-lane model** for Tier-2 joined-wait completion delivery.

In this model:

- `L2Shard` remains the authority unit for routing, exact-match coordination, in-flight admission, governance reaction,
  and bucket re-verification
- completion delivery is executed by a fixed number of adapter-owned dispatch lanes
- shard-to-lane mapping is deterministic and static for the lifetime of the adapter instance
- dispatch lanes are execution units only; they do not own lifecycle truth
- the dispatch plane is adapter-internal infrastructure and is not a Domain Core port

This decision standardizes the execution boundary for non-blocking join completion delivery while preserving shard-local
authority and avoiding both global delivery contention and thread explosion.

This decision also requires that the dispatch plane use explicit operational lifecycle law rather than scattered
implicit
branch legality.

At minimum, the dispatch implementation must define:

- one delivery-entry operational lifecycle axis
- one dispatch-lane operational lifecycle axis

These axes are adapter-owned operational state, not semantic lifecycle truth.

## Decision Rules

### 1. Shards remain authority units

`L2Shard` remains the owner of:

- exact-match pre-screening
- in-flight coordination
- builder vs join branching
- governance reaction mapping
- authoritative bucket re-verification

Dispatch lanes must not assume or absorb those responsibilities.

### 2. Lanes are delivery-execution units only

A dispatch lane may own:

- continuation registration
- terminal sweep scheduling
- timeout scheduling
- restart-ready callback execution
- lane-local shutdown / quiescence mechanics

A dispatch lane must not own:

- shared-slot terminal truth
- waiter terminal truth
- builder-handle truth
- commit-right truth
- region lifecycle truth
- bucket re-verification
- fault taxonomy decisions

### 3. Lane routing is derived from shard routing

Dispatch lanes must not introduce an independent routing authority.

The routing law is:

1. `route64` determines the shard
2. `shardIndex` deterministically determines the lane
3. the dispatch plane follows that result only

Therefore, lane routing is a deterministic function of shard routing and must not re-derive or reinterpret semantic
routing independently.

### 4. Shard-to-lane affinity is static

Shard-to-lane mapping must be fixed for the lifetime of the adapter instance.

Allowed:

- static bootstrap-time lane count
- static bootstrap-time shard-to-lane mapping

Forbidden:

- live adaptive rebalance
- work stealing across lanes
- runtime migration of shard delivery ownership

Any future change to lane count or mapping policy must occur only at a new bootstrap / policy-epoch boundary, not during
the lifetime of a running adapter instance.

### 5. Sequential affinity is required

For a given shard, delivery work must always be executed by the same lane.

This does **not** imply a global total callback order across all shards.

The contractual requirement is:

- shard-affine sequential delivery ownership
- not global cross-shard callback total ordering

### 6. Global delivery structures are forbidden

The following are forbidden as architectural choices for this delivery model:

- a single global dispatch registry
- a single global unpartitioned dispatch queue
- a global `ConcurrentHashMap`-based waiter registration registry
- a global blocking queue acting as the sole completion bottleneck

Delivery infrastructure must instead use lane-local bounded structures.

### 7. Caller-thread direct callback execution is forbidden

A joined-wait completion callback must not execute directly on:

- the caller thread that performed registration
- the shard thread that detected already-terminal visibility
- arbitrary incidental control-path threads

Restart-ready callback execution must remain adapter-owned and lane-owned.

### 8. Boundedness is required

Registration storage and delivery queues must be bounded.

Unbounded growth is forbidden because:

- the delivery plane is infrastructure, not semantic authority
- joined waiting is a bounded operational concern
- memory discipline must remain explicit and predictable

### 9. Primitive-friendly implementation is preferred

This ADR does not hard-code a single concrete data structure implementation.

However, the architectural direction is explicit:

- lane-local structures should be primitive-friendly
- packed state, array-backed structures, fixed-capacity rings, and deterministic indexing are preferred
- implementation choices that reintroduce global dynamic contention surfaces are disallowed

Concrete implementation details are intentionally delegated to a separate Design document.

### 10. Explicit operational state machines are required

The dispatch plane must use explicit closed operational lifecycle vocabularies.

At minimum, the implementation must define:

- one delivery-entry lifecycle axis
- one dispatch-lane lifecycle axis

These operational axes are intentionally distinct from:

- `SharedSlotState`
- `WaiterState`
- `BuilderHandleState`
- `CommitRightState`
- `PartitionRegionState`

The delivery-entry axis models callback-delivery progress only.
The dispatch-lane axis models lane-owned execution lifecycle only.

Transition legality for these axes must not remain implicit in scattered branch logic.
The implementation must provide explicit transition-law surfaces and runtime enforcement.

Rationale:

- keep operational legality visible
- prevent ad hoc lifecycle drift
- preserve consistency with the broader closed-state discipline already used in the planning runtime

### 11. Lane-owned final clear is required

Lane-owned mutable delivery state must be cleared only by the owning lane thread.

Forbidden:

- external thread direct clearing of lane-owned registration state
- external thread direct clearing of lane-owned ready-queue state
- external thread direct clearing of lane-owned timeout ownership state

External administrative authority may:

- publish close intent
- stop fresh admission
- wait for bounded convergence
- fail closed if bounded convergence does not complete

But final abandonment, reclamation, and final clear of lane-owned mutable state must remain lane-owned.

Rationale:

- preserve single-writer authority
- avoid close/reclaim races
- prevent external corruption of in-flight delivery state

### 12. Quiescence must be judged from published state only

Administrative quiescence judgment must not inspect lane-owned mutable arrays directly.

Quiescence must instead be derived from lane-published operational state, such as:

- lane lifecycle state
- command-ring emptiness
- ready-queue published size
- active callback count
- live operational registration count
- dirty-shard published count

Rationale:

- avoid visibility ambiguity
- preserve lane ownership boundaries
- keep administrative observation bounded and mechanically explicit

### 13. Administrative authority must be serialized

Administrative operations that publish close / drop / shutdown intent must be serialized.

Forbidden:

- overlapping concurrent shutdown authority
- multiple concurrent quiescence-wait authorities for the same adapter instance

The implementation may choose a single administrative gate or equivalent serialized-authority mechanism.

Rationale:

- keep close/drop semantics singular
- avoid competing terminalization coordinators
- prevent ambiguous shutdown ownership

## Rationale

This decision was chosen because it best preserves all already-adopted planning/runtime principles simultaneously.

A single global dispatch plane was rejected because it collapses all shard-local isolation into a single downstream
contention point. Even if the logical L2 world is partitioned correctly, a single global delivery registry/queue would
reintroduce global coordination pressure exactly where we intended to remove it.

A 1:1 shard-to-thread model was rejected because it over-projects logical authority boundaries onto physical thread
ownership. Shards are the correct authority unit for routing and lifecycle orchestration, but they are not required to
imply a dedicated thread each. A strict 1:1 thread model would scale poorly as shard count rises and would overfit the
delivery mechanism to physical execution resources.

The deterministic M:N lane model keeps the important part of shard isolation—the authority boundary—while choosing a
more realistic physical execution model for the JVM. It provides stable affinity, avoids a single global delivery
bottleneck, and avoids thread explosion.

This model also provides a clean place to enforce single-writer operational law.

Because lanes are execution units only, the design can keep:

- semantic lifecycle truth outside the lane layer,
- operational delivery progression inside the lane layer,
- and administrative quiescence observation on published operational state only.

That separation is essential to preserve both authority clarity and bounded runtime behavior.

## Rejected Alternatives

### A. Single global dispatch plane

Rejected because it would:

- reintroduce a single delivery contention point
- undermine shard-local isolation benefits
- centralize completion delivery pressure into one global structure
- conflict with the broader shared-nothing direction already established in Tier-2 hot-path design

### B. 1:1 shard-to-thread delivery

Rejected because it would:

- bind logical authority too tightly to physical thread ownership
- scale thread count linearly with shard count
- create thread explosion risk under larger shard counts
- provide more physical coupling than the current laws require

### C. Adaptive rebalance / work stealing between lanes

Rejected because it would:

- introduce runtime policy drift inside one adapter lifetime
- complicate determinism reasoning and debugging
- weaken the fixed-affinity model
- solve an operational tuning problem by mutating the architectural authority surface

### D. Polling-era blocking joined wait

Rejected because it would:

- violate non-blocking join requirements
- retain worker threads during joined waits
- conflict with fresh-session restart law
- regress toward the earlier model that this runtime redesign explicitly replaced

### E. Advanced synchronization-first designs as the baseline

Examples include:

- flat combining as the primary delivery model
- SIMD-driven polling as the primary delivery model
- other highly specialized synchronization schemes as the default architectural baseline

Rejected for this cut because the primary architectural requirement is clear authority separation and deterministic
bounded execution, not maximal synchronization sophistication. Such techniques may be evaluated later in
design/optimization work, but they are not adopted here as the baseline architectural choice.

### F. External-thread direct lane cleanup

Rejected because it would:

- violate lane single-writer ownership
- introduce close/reclaim races against in-flight delivery work
- weaken the distinction between close publication and lane-owned final convergence
- make quiescence harder to reason about mechanically

## Consequences

### Positive consequences

- preserves non-blocking joined-wait semantics
- keeps shard authority intact
- avoids a single global completion bottleneck
- avoids 1:1 shard-thread coupling
- gives a deterministic, inspectable delivery ownership model
- aligns with adapter-owned completion dispatch and fresh-session restart law

### Negative consequences

- lane-count sizing becomes a bootstrap/policy concern
- hot-lane skew remains possible when multiple hot shards map to the same lane
- implementation is more complex than a single global queue
- bounded queue and registration-capacity behavior must be designed explicitly

## Non-Goals

This ADR does not:

- specify the exact concrete registry table implementation
- specify the exact concrete queue implementation
- require a particular JVM collection or primitive table type by name
- define the telemetry model for hot-lane detection
- introduce live lane rebalance
- redefine shard routing
- move lifecycle truth ownership into the dispatch plane

Those items belong to the companion Design document.

## Design Follow-Up

A separate Design document must define:

- lane count constraints and selection rules
- shard-to-lane mapping function
- lane-local registration storage structure
- lane-local bounded queue structure
- timeout scheduling mechanics
- shutdown and quiescence protocol
- delivery failure handling
- optional shard-affine sub-queue servicing strategy inside one lane
- future hardening path toward more primitive / packed implementations

## Migration Notes

Implementation work following this ADR should proceed in this order:

1. introduce a shard-aware, lane-based adapter-owned dispatch plane
2. remove polling-era join parameters from `PartitionRegion` / adapter bootstrap
3. make shard-to-lane mapping explicit and static
4. ensure region close / partition drop use lane-owned terminal sweep delivery
5. keep `StructuralPlannerCore` and `PlanInterner` aligned with the already-adopted non-blocking join / fresh-session
   restart model

## Authority Statement

This ADR defines the architectural execution model for Tier-2 join completion delivery.

It is normative for:

- shard vs lane authority separation
- fixed shard-to-lane affinity
- rejection of single global delivery structures
- rejection of 1:1 shard-to-thread delivery
- rejection of live adaptive lane rebalance
- adapter-owned callback execution

It is intentionally non-normative for concrete low-level data structure selection, which must be handled by the
corresponding Design document.