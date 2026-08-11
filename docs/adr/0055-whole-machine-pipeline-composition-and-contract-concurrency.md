# ADR-0055: Whole Machine, Pipeline Composition, and Contract Concurrency

## Status

Proposed

## Date

2026-08-12

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0056: Governance Contract, Policy-World Control, Whole-Machine Coordination, and Selection Boundary
- ADR-0054: Policy Contract, Explicit Operating Policies, Self-Contained Contract Worlds, and Interface Binding Boundary
- ADR-0053: Version Contract, Sovereign Contract Revision History, and Realization Boundary
- ADR-0052: Capacity Contract, Explicit Safe Operating Memory Limits, and Realization Boundary
- ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror

---

## 1. Context

The earlier ADRs already define the local Operation model. Contract meaning is explicit, State-Machine meaning is
separate, and realization has no authority over either.

ADR-0055 addresses the point beyond that local model. It asks what becomes Contract meaning when complete Operation
flows and Interface boundaries participate in a larger machine.

---

## 2. Problem

Physical concurrency by itself is not a Contract concern. Threads, workers, scheduling, algorithms, and hardware belong
to realization.

The one-dimensional Contract pipeline does not create a separate composition problem. For one Operation flow, its
established pipeline is immutable. Its one-dimensional Contract Authorities are independent, and their results are
deterministic under the same contractual condition. Contractual contention, merge, or execution order therefore does not
arise at that level.

A different problem begins when complete flows are related to one another. At that boundary, dependency, ordering,
coexistence, transfer, or completion may become observable machine meaning. Those relations must be defined before their
realization is considered.

---

## 3. Contract Decision

### 3.1. One-Dimensional Contract Processing

ADR-0055 does not introduce a separate concurrency model for one-dimensional Contracts.

Within an Operation flow, the established Contract pipeline remains immutable and its one-dimensional Contract
Authorities remain independent. The same applicable Contract material, contractual input, Contract World, and
established State produce the same contract-visible result.

No contractual contention, merge relation, or implicit ordering is inferred from physical execution. The remaining
decisions in this ADR begin at Whole Machine composition.

### 3.2. Contract-First Scope

Whole Machine composition is defined as Contract meaning before any backend mechanism is selected.

No thread model, scheduler, communication mechanism, deployment topology, or algorithm may supply missing semantics. A
later realization may choose any mechanism that preserves the Contract and State-Machine meaning established by this
ADR.

---

## 4. Verification

Verification must reject any Whole Machine definition whose meaning depends on undeclared realization structure.

The exact verification rules for composed flows remain open until their Contract relations are decided.

---

## 5. Deferred Decisions

The following remain open for the continuation of ADR-0055:

- the existence condition and identity of a Whole Machine;
- the Contract relation between complete Operation flows and Interface boundaries;
- fan-out, fan-in, and completion;
- cross-flow Contract material and authority boundaries;
- coexistence and ordering where they are observable;
- cycles and repeated collaboration;
- Contract World continuity across collaborating flows;
- the meaning of physical separation when it is itself an obligation;
- distributed realization after the Contract model is complete;
- and determinism for valid Whole Machine compositions.

These questions are decided in Contract terms first. Realization follows afterward.

---

## 6. Consequences

### Positive

ADR-0055 does not duplicate the independence and determinism already established for one-dimensional Contracts. It can
focus on the first boundary where relations between complete flows may become Contract meaning.

### Negative

The Whole Machine model remains incomplete until the deferred composition questions are resolved.

### Neutral

The realization axis remains intentionally open while the Contract model is being defined.