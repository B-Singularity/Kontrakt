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

Those Contracts do not exist as an arbitrary collection. They describe a machine. A Core is the contract scope in which
the Contracts that describe one machine are brought together.

ADR-0055 addresses the point beyond that local machine. It asks what becomes Contract meaning when complete Cores
participate in a larger machine.

---

## 2. Problem

Physical concurrency by itself is not a Contract concern. Execution mechanisms belong to realization. Threads and
workers do not define Contract meaning. Scheduling and hardware do not define it either.

The one-dimensional Contract pipeline does not create a separate composition problem. For one Operation flow, its
established pipeline is immutable. Its one-dimensional Contract Authorities are independent, and their results are
deterministic under the same contractual condition. This level therefore has no contractual contention. It also does not
define merge relations or execution order.

Before Whole Machine relations can be defined, the role of a Core must remain clear. Contract theory must not turn Core
formation into a decomposition algorithm. It still needs to explain how one explicit and practical contract machine
should be examined as its Contracts grow.

A different problem begins when complete Cores are related to one another. One Core may depend on another. Some
relations may require order while others may allow coexistence. Material may cross a boundary, and completion may depend
on more than one Core. When any of these relations becomes observable machine meaning, the Contract must define it
before realization is considered.

---

## 3. Contract Decision

### 3.1. One-Dimensional Contract Processing

ADR-0055 does not introduce a separate concurrency model for one-dimensional Contracts.

Within an Operation flow, the established Contract pipeline remains immutable and its one-dimensional Contract
Authorities remain independent. The same applicable Contract material, contractual input, Contract World, and
established State produce the same contract-visible result.

Physical execution does not create contractual contention. It also does not create a merge relation or an implicit
order. The remaining decisions in this ADR begin at Whole Machine composition.

### 3.2. Core Engineering Guidance

A Core is the contract scope in which the Contracts that describe one machine are brought together.

Contract theory does not prescribe the correct Core boundary. Doing so would prescribe how the machine should be divided
rather than state what its Contract means. Implementation structure does not decide that boundary either.

This does not make Core formation arbitrary. Kontrakt is based on a contract theory that treats software as an explicit
and practical machine. A machine exists to fulfill a purpose. The theory therefore gives engineering guidance for
examining a Core without turning that guidance into a decomposition rule.

As the Contracts of a machine become explicit, ask:

- Do these Contracts still make sense as parts of the same machine?
- Are these Contracts still aligned with the purpose of that machine?
- As the machine changes, does the Core still describe one understandable machine?

A difficult answer does not require the Core to be split. It is a reason to examine the machine again. The machine may
still be poorly understood. A Contract may be misplaced. The machine's purpose may need a clearer statement. Another
machine may also be beginning to appear.

Machines evolve. A Core is therefore not a boundary that is solved once at the beginning. As new Contract material
becomes explicit, the same questions should be asked again.

This guidance is not an implementation method. It follows from the contract theory itself. The purpose is to keep the
explicit Contracts understandable as one practical machine built to fulfill its purpose.

The exact condition under which several Cores form one Whole Machine remains a separate decision in this ADR.

### 3.3. Contract-First Scope

Whole Machine composition is defined as Contract meaning before any backend mechanism is selected.

No backend mechanism may supply missing semantics. A thread model cannot do so, and neither can a scheduler.
Communication and deployment choices also remain realization concerns. A later realization may choose any mechanism that
preserves the Contract and State-Machine meaning established by this ADR.

---

## 4. Verification

Verification must reject any Whole Machine definition whose meaning depends on undeclared realization structure.

The Core guidance in Section 3.2 does not authorize Kontrakt to guess or enforce one correct decomposition. It guides
the engineering judgment used to keep one Core understandable as one machine.

The exact verification rules for composed Cores remain open until their Contract relations are decided.

---

## 5. Deferred Decisions

The following remain open for the continuation of ADR-0055:

- the existence condition and identity of a Whole Machine;
- the Contract relation between participating Cores and their complete Operation flows;
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

ADR-0055 does not duplicate the independence and determinism already established for one-dimensional Contracts. It now
also states how Core boundaries should be examined without turning contract theory into an implementation decomposition
method. The remaining work can focus on the first boundary where relations between complete Cores may become Contract
meaning.

### Negative

The Whole Machine model remains incomplete until the deferred composition questions are resolved.

### Neutral

The realization axis remains intentionally open while the Contract model is being defined.