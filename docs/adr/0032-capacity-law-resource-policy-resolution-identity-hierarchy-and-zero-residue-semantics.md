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
    - `SMALL_HEAP`
    - `DEFAULT`
    - `SERVER`

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
    - bulk-drop policy.

Governance changes MUST NOT alter semantic output.
They may alter only:

- sharing,
- retention,
- throughput,
- latency,
- memory survival behavior.

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
    - clarify `maxApproxBytes` as governance estimate.

3. Add compliance tests
    - `PrimitiveLedgerComplianceTest`
    - `CapacitySolverDeterminismTest`
    - `RollbackReachabilityComplianceTest`
    - `SentinelInitializationComplianceTest`
    - `ColdHotCacheBlindDeterminismTest`
    - `GateOffOnAutoEquivalenceTest`
    - `ArithmeticOverflowGuardTest`