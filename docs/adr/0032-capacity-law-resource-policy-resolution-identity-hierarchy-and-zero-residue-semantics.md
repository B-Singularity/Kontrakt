# ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics

Date: 2026-03-13

Status: Accepted

## Context

Kontrakt’s planning engine is a deterministic compiler-style state machine.
The framework already commits to:

- cache-blind semantic determinism,
- worker-local primitive planning state,
- fail-closed protocol enforcement,
- exact identity verification after fast routing,
- zero-residue cleanup on all exit paths,
- policy / protocol separation.

However, the current capacity handling remains partially heuristic and underspecified:

1. **Policy / Protocol Mixing**
    - `PlannerSessionConfig.issue(...)` still risks mixing protocol invariants with numeric policy defaults.
    - The protocol must not derive runtime capacities from implicit environment reads.

2. **Implicit or Bundled Memory Accounting**
    - Capacity is still vulnerable to bundled approximations (`rmqBytes`, `nodeBytes`, magic ratios, undocumented
      ceilings)
      instead of a line-item primitive byte ledger aligned with actual worker-local structures.

3. **Ambiguous Reset Semantics**
    - “Zero-residue” is not yet formally split into:
        - semantic non-reachability,
        - optional physical byte wiping.

4. **Identity Ambiguity**
    - Fast routing identities (`route64`, `planKey64`, `nodeIdentity64`) and authoritative semantic identities
      must be normatively separated to avoid accidental correctness coupling.

5. **Cross-Cutting Scope**
    - The problem spans:
        - L1 planner-session primitive structures,
        - `NodeIdIndexer`,
        - RMQ / cycle-truncation arrays,
        - L2 governance and routing,
        - resource-profile resolution at the adapter boundary.

## Decision

### 1. SSOT vs Policy Boundary

The following are **Protocol / SSOT**:

- determinism invariants,
- fail-closed conditions,
- cost-center tick semantics,
- exact-match verification law,
- zero-residue semantics,
- primitive byte ledger law shape,
- capacity solver safety constraints.
- cost-center family banding and budget-track assignment,
- monotonic physical / semantic metering counter semantics,
- type-expansion metering boundary,
- raw-fact resolution hit/miss metering distinction.

The following are **Policy**:

- numeric defaults,
- resource profile mapping,
- desired node/depth preference curves,
- fixed headroom reserve magnitude,
- secure wipe enablement,
- environment-aware budget resolution strategy.

Policy changes MUST NOT alter semantic output.

### 2. Resource Policy Resolution MUST live outside the Core

Environment discovery is not part of the Domain Core.

Rules:

- `PlannerSessionConfig.issue(...)` MUST accept only already-resolved numeric values.
- The core MUST NOT call:
    - `Runtime.getRuntime()`,
    - cgroup readers,
    - OS memory APIs,
    - container introspection APIs.

- A high-level `ResourceProfile` surface MAY exist:
    - `AUTO`
    - `SMALL`
    - `STANDARD`
    - `LARGE`

- Resolution from `ResourceProfile -> ResolvedSessionBudget` MUST happen in a Port/Adapter boundary.

- `AUTO` resolution MUST occur at bootstrap or equivalent stable policy-resolution time.
  It MUST NOT be recomputed mid-session.

- The resolved numeric budget MUST be immutable for the lifetime of the `PlannerSession`.

### 3. Explicit Primitive Byte Ledger (L1 SSOT)

Capacity reverse-calculation MUST be derived from an explicit primitive byte ledger.

The minimum ledger line items are:

| Line Item                      | Meaning                                                                |
|--------------------------------|------------------------------------------------------------------------|
| `nodeIdIndexerTableBytes`      | open-addressing routing storage                                        |
| `nodeIdIndexerDenseBytes`      | dense metadata indexed by `nodeId`                                     |
| `activeStackBytes`             | explicit traversal stack                                               |
| `depthOfNodeIdBytes`           | depth cache / direct node-depth mapping                                |
| `incomingEdgeRankAtDepthBytes` | structural ordering state at each active depth                         |
| `floorLog2Bytes`               | precomputed RMQ log table                                              |
| `flatMinEdgeRankUpBytes`       | RMQ / binary-lifting sparse table tier 1                               |
| `flatArgminUpBytes`            | RMQ / binary-lifting sparse table tier 2                               |
| `undoLogBytes`                 | transactional rollback log                                             |
| `signatureSlabBytes`           | canonical signature byte slab                                          |
| `fixedHeadroomBytes`           | conservative reserve for runtime noise / padding / non-ledger overhead |

Rules:

- Each line item MUST be documented in the L1 design note with an explicit formula.
- No bundled “heuristic bytes” category may replace the required ledger line items.
- `signatureSlabBytes` MUST be reserved separately and MUST NOT be double-counted.
- Headroom is policy-calibrated reserve, not semantic storage.

### 4. Adaptive Capacity Solver (Desired vs Feasible)

Capacity solving MUST distinguish:

- **Desired Capacity**
    - policy preference,
    - for example a preferred node/depth ratio,
    - preferred undo density,
    - preferred growth curve.

- **Feasible Capacity**
    - the maximum capacity that fits inside the injected memory budget under the primitive byte ledger.

Rules:

- Final capacities MUST be determined by a deterministic solver.
- No unexplained hard ceiling may exist inside the protocol core.
- Any ceiling that remains MUST be explicitly declared as policy and documented as such.
- The solver MUST be monotonic with respect to feasible capacity assumptions.
- Identical:
    - version tuple,
    - resolved policy inputs,
    - resolved numeric budgets

  MUST produce identical capacity outputs.

### 5. Solver Safety Constraints

The capacity solver MUST fail closed under any of the following:

- minimal valid layout cannot fit,
- arithmetic overflow during byte calculation,
- `nextPowerOfTwo(...)` overflow or invalid input,
- `maxSignatureBytes < maxSignatureLen`,
- `maxSignatureBytes > Int.MAX_VALUE`,
- any derived cap would exceed the addressable primitive index range,
- any sentinel initialization law would be violated.

The solver MUST compute intermediate byte totals in `Long`.
Silent wraparound is constitutionally forbidden.

### 6. Semantic Zero-Residue Law

“Zero-residue” is defined as **Semantic Non-reachability**.

Protocol requirements:

- `resetToCleanState()` MUST execute from a hardcoded `finally` block.
- rollback/reset MUST restore the active frontier such that stale state is semantically unreachable.
- no reachable table chain may reference `nodeId >= currentNodeCount`.
- no reachable signature slice may read beyond `currentSlabPtr`.
- no reachable membership / traversal structure may retain active-state evidence from the discarded branch/session.

Physical zero-filling of arrays/slabs is NOT a protocol requirement.

### 6.1 Monotonic Runtime Metering Is Outside Rollback and Zero-Residue

Runtime metering counters are monotonic execution ledgers.

The following counters, or their equivalent implementation representation, MUST remain outside rollback-scoped planner
snapshots:

- cumulative physical step count,
- cumulative semantic work count.

Rollback and reset may restore:

- frame cursor state,
- active traversal frontier,
- rollback-local checkpoints,
- semantic reachability frontiers,
- worker-local reusable structure baselines.

Rollback and reset MUST NOT erase already-consumed physical or semantic work.

Reason:

- rollback is control-flow recovery, not physical time reversal;
- already-performed CPU work must remain charged;
- semantic work already ratified before a failure remains part of the run's consumed budget unless the implementation
  explicitly defines a stronger run-level abort policy outside the rollback boundary.

Zero-residue remains defined as semantic non-reachability of discarded planner state.
It does not mean runtime metering counters are physically or logically rewound.

If a logical planning run spans multiple fresh worker-local sessions, any request/run-scoped remaining execution budget
MUST carry forward both:

- remaining physical steps,
- remaining semantic work units,

unless a separate ADR explicitly ratifies physical-only carry-forward.

A single `remainingPhysicalSteps` value is insufficient for a dual-track budget model.

### 7. Optional Secure Wipe Policy

Physical zero-filling MAY be offered as a policy mode for secure or regulated environments.

Rules:

- secure wipe is a policy option, not a semantic correctness requirement;
- enabling secure wipe MUST NOT change semantic output;
- secure wipe MAY change latency / throughput / reset cost.

### 8. Rollback Restore Boundary Law

`rollback(snapPtr)` and `rollbackCount(targetCount, targetSigPtr)` together define one logical restore boundary.

Required invariants after restore:

- all routing-table mutations after `snapPtr` are undone;
- `_nextId == targetCount`;
- `sigSlabPtr == targetSigPtr`;
- all reachable heads/chains satisfy `nodeId < targetCount`;
- all reachable signature references satisfy `offset + length <= targetSigPtr`.

Stale bytes may remain physically present but MUST remain semantically unreachable.

### 9. Identity Hierarchy

Kontrakt defines two identity classes:

#### 9.1 Routing Identity

Examples:

- `route64`
- `planKey64`
- `nodeIdentity64`

Properties:

- fast,
- deterministic,
- collision-tolerant,
- non-authoritative,
- used only for routing, sharding, probing, or bucket location.

#### 9.2 Semantic Identity

Examples:

- `PlanCacheKey` full semantic tuple,
- canonical signature bytes,
- equivalent authoritative identity material.

Properties:

- authoritative,
- immutable,
- exact-match based.

### 10. Verification Law

A routing hit MUST ALWAYS be followed by authoritative verification before reuse or publication.

Implications:

- routing collision is a normal condition;
- routing collision is NOT a repository fault;
- exact mismatch under the same routing key means “continue scan or miss”;
- correctness MUST NOT depend on routing uniqueness.

### 11. L2 Governance Boundary

L2 governance and L1 planner-session sizing are related but distinct.

- L1 budgets govern worker-local planning structures.
- L2 governance budgets govern repository survival and sharing efficiency:
    - `maxEntries`,
    - `maxApproxBytes`,
    - per-partition caps,
    - circuit-open thresholds,
    - bulk-drop policy,
    - resolved join-wait deadlines,
    - per-key waiter caps,
    - speculative-build quotas.

Governance changes MUST NOT alter semantic output.
They may alter only:

- sharing,
- retention,
- throughput,
- latency,
- memory survival behavior.

Additional normative rule:

- join-timeout expiration is a waiter lifecycle event, not a semantic failure of the canonical result,
- L2 wait/join governance MUST remain outside L1 planner-session sizing and primitive byte-ledger math.

### 11.1 Type Expansion Metering Boundary

Type expansion work is part of planner runtime metering, but it is not Tier-2 cache governance.

The planner distinguishes the following cost-center families:

1. **L1 Session**
   Worker-local runtime substrate work such as frame dispatch, node-id indexing, rollback replay, and primitive state
   management.

2. **Graph**
   Direct graph traversal and graph-topology expansion work.

3. **L2 Cache / Governance**
   Plan interning, route derivation, shard/bucket lookup, in-flight slot lifecycle, waiter lifecycle, and L2
   survival/governance reactions.

4. **Type Expansion**
   Metamodel-to-planning semantic lowering work, including:
    - type-shape resolution,
    - shape lowering,
    - raw type-fact cache hit observation,
    - raw type-fact actual resolution,
    - raw-fact subject continuity checks,
    - active-member projection,
    - active-member ordering,
    - container/atomic expansion decisions.

Type Expansion MUST NOT be charged through the `L2_CACHE` band.

Reason:

L2 cache governance controls sharing/reuse infrastructure.
Type expansion controls semantic preparation for graph expansion.
Merging these two cost families would make cache policy appear to affect semantic preparation cost and would obscure
budget diagnostics.

#### 11.1.1 Raw Fact Cache Hit vs Actual Resolution

Raw type-fact retrieval MUST distinguish cache hit from actual resolution.

A cached raw-fact hit is not semantic work by itself.
It is already-ratified fact reuse and MUST be charged as physical-only validation / retrieval work.

Actual raw-fact resolution is semantic lowering input acquisition and MAY be charged as semantic-also work.

Therefore the protocol MUST NOT define one ambiguous `COMPOSITE_RAW_FACT_RESOLUTION` cost center that is used for both
hit and miss paths.

Required split:

- `COMPOSITE_RAW_FACT_CACHE_HIT`
    - `BudgetTrack.PHYSICAL_ONLY`
    - used when a previously ratified raw-fact DTO is retrieved and revalidated.

- `COMPOSITE_RAW_FACT_RESOLVE`
    - `BudgetTrack.SEMANTIC_ALSO`
    - used when the adapter/backend performs actual fact discovery/reconciliation for the current expansion episode.

If the implementation cannot tell whether raw facts came from a hit or actual resolution, it MUST conservatively treat
the path as actual resolution until the provider contract is refined.

#### 11.1.2 Type Expansion Budget Tracks

Type Expansion cost centers use the same `BudgetTrack` model as the rest of the planner.

`PHYSICAL_ONLY` applies to:

- type-shape resolution,
- shape lowering,
- raw-fact cache hit observation,
- raw-fact subject continuity checks,
- atomic expansion decisions,
- container expansion decisions.

`SEMANTIC_ALSO` applies to:

- actual raw type-fact resolution,
- active-member projection,
- active-member ordering.

Projection and ordering are semantic-also because they ratify constructor/property traversal inputs and can affect final
graph topology.

Continuity checks and dispatch decisions are physical-only because they validate or route already-known facts without
creating a new semantic choice.

#### 11.1.3 Interface Expansion Cost Deferral

`INTERFACE_EXPANSION_DECISION` MUST NOT be introduced as a ratified cost center until the interface/implementation
resolution path exists.

Before that path is implemented, interface shape handling MUST fail closed or remain explicitly unsupported.

Forbidden:

- charging a cost center for a path that has no executable implementation,
- silently falling through from interface shape to composite expansion,
- treating abstract-class handling and interface-resolution policy as the same decision without a ratified rule.

#### 11.1.4 Composite Expansion Plan Issuance Is Not a Separate Cost Center

`CompositeExpansionPlan.issue(...)` is validation and object issuance around already-ratified projection/order outputs.

It MUST NOT receive a separate cost center in the initial protocol.

Its cost is accounted as part of:

- active-member ordering, or
- the surrounding physical frame dispatch / lowering step.

A future implementation MAY add a dedicated cost center only if measurement shows plan issuance is a meaningful and
independently diagnosable cost surface.

#### 11.1.5 Session Accounting Boundary

`TypeExpansionPipeline` MUST NOT mutate `PlannerSession` directly.

Instead, the caller MUST provide a session-bound `TypeExpansionWorkMeter` or equivalent bridge that maps closed
`TypeExpansionWorkEvent` values to ratified `CostCenter` values and then calls the session's authoritative accounting
gate.

This preserves the boundary:

- `TypeExpansionPipeline` owns deterministic type-expansion stage ordering.
- `PlannerSession` owns runtime metering mutation.
- `RequiredCostCentersSpec` owns cost-center and budget-track semantics.

Implementation constraint:

`TypeExpansionPipeline` MUST NOT accept `PlannerSession` in its constructor or factory.
Any future constructor/factory shape that directly depends on `PlannerSession` is non-compliant.

### 12. Consequences

#### Positive

- sharper Hexagonal boundary,
- explicit and auditable capacity law,
- stronger determinism under injected budgets,
- scalable capacity without undocumented structural walls,
- precise identity hierarchy,
- O(1)-class semantic resets under pooled primitive state.

#### Negative

- more architecture surface area,
- more required compliance tests,
- tighter coupling between design documents and implementation formulas,
- explicit need for calibration policy outside the core.

## Required Follow-up

1. Amend `docs/design/l1-planner-session-primitive-data-structures.md`
    - full primitive byte ledger,
    - solver law,
    - reset / rollback reachability invariants,
    - sentinel initialization law,
    - arithmetic overflow guard requirements.

2. Amend `docs/design/l2-plan-interner-partitioned-tier2-with-governance.md`
    - align routing-vs-authority hierarchy,
    - clarify collision vs fault,
    - clarify governance cap scope,
    - clarify `maxApproxBytes` as governance estimate,
    - add async join governance,
    - separate shared-slot state from waiter lifecycle,
    - specify partition-drop wake-up completeness and attach/drop race handling.

3. Add compliance tests
    - `PrimitiveLedgerComplianceTest`
    - `CapacitySolverDeterminismTest`
    - `RollbackReachabilityComplianceTest`
    - `SentinelInitializationComplianceTest`
    - `ColdHotCacheBlindDeterminismTest`
    - `GateOffOnAutoEquivalenceTest`
    - `ArithmeticOverflowGuardTest`

4. Amend `docs/design/deterministic-active-member-projection-and-ordering-protocol.md`
    - clarify type-expansion metering,
    - split raw-fact cache hit from actual raw-fact resolution,
    - bind projection/order to semantic-also accounting,
    - forbid PlannerSession mutation inside TypeExpansionPipeline.

5. Amend `docs/design/l1-planner-session-primitive-data-structures.md`
    - add the `TYPE_EXPANSION` cost-center band,
    - add required Type Expansion cost centers,
    - document that decode bounds must be derived from actual protocol IDs or band maxima, not hand-maintained as a
      stale literal.

6. Amend `docs/design/planner-budget-resolution-and-worker-lifecycle.md`
    - clarify that physical and semantic counters are monotonic and rollback-resistant,
    - clarify that fresh-session restart must carry forward both remaining physical and remaining semantic budget if
      semantic work is bounded for the logical run.