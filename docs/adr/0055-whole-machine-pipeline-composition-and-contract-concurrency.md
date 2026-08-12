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

An Interface is related to that work, but it is not the Core itself. An Interface makes a contractual relationship
explicit. It naturally gathers Operations and the Contracts needed for that relationship into a coherent surface.
Because Core formation also depends on keeping related Contracts coherent around one machine, an Interface often becomes
a convenient authoring surface for a Core. The two concepts still remain distinct.

An Interface may expose most of a Core when that relationship needs most of the machine. It may also expose only the
part that is relevant to one relationship. The size of an Interface therefore does not determine the size of the Core.

ADR-0055 addresses the point beyond one local machine. It asks what becomes Contract meaning when several Cores must
work together as one larger machine.

---

## 2. Problem

Physical concurrency by itself is not a Contract concern. Execution mechanisms belong to realization. Threads and
workers do not define Contract meaning. Scheduling and hardware do not define it either.

The one-dimensional Contract pipeline does not create a separate composition problem. For one Operation flow, its
established pipeline is immutable. Its one-dimensional Contract Authorities are independent, and their results are
deterministic under the same contractual condition. This level therefore has no contractual contention. It also does not
define merge relations or execution order.

An Operation is a contractual interaction surface. Its boundary is determined by the interaction being contracted, not
by the boundaries of functions or methods used to realize it. A realization may use many internal functions to fulfill
one Operation. If a machine needs several separately meaningful contractual interactions, several Operations may be
declared instead. The call structure used by the realization does not become a Contract pipeline merely because several
functions or internal steps are involved.

Before Whole Machine relations can be defined, the role of a Core must remain clear. Contract theory must not turn Core
formation into a decomposition algorithm. It still needs to explain how one explicit and practical contract machine
should be examined as its Contracts grow.

A different problem begins when several Cores must work together for a larger machine purpose. The complete Contract
material of each Core does not need to participate in every relation. One relation may concern only a particular
Interface or another relevant contract surface of each Core. What matters is whether the larger machine purpose creates
a Contract relation that did not belong to any participating Core alone.

When that relation becomes observable machine meaning, the Contract must define it before realization is considered.

---

## 3. Contract Decision

### 3.1. One-Dimensional Contract Processing

ADR-0055 does not introduce a separate concurrency model for one-dimensional Contracts.

Within an Operation flow, the established Contract pipeline remains immutable and its one-dimensional Contract
Authorities remain independent. The same applicable Contract material, contractual input, Contract World, and
established State produce the same contract-visible result.

Physical execution does not create contractual contention. It also does not create a merge relation or an implicit
order.

A realization may divide one Operation into any number of internal steps. It may also combine internal work when that
preserves Contract meaning. Those choices belong to the implementation pipeline. They do not create additional
Operations or additional Contract pipeline stages by themselves.

A Contract Pipeline is one-way. External material enters through the Input Airlock, proceeds through the established
Contract processing path, and leaves only through Publication / Output. Material does not re-enter an earlier Contract
stage and the pipeline does not form a cycle. If published material later enters the same Core or another Core, that
admission begins a new pipeline flow through that Core's Input Airlock. It does not continue or loop the earlier
pipeline.

One Contract Pipeline also does not know another Contract Pipeline. Whole Machine composition can therefore relate
explicit Contract surfaces, but it cannot connect pipeline internals or create direct Contract-level communication
between them.

The remaining decisions in this ADR begin where several Cores participate in one larger machine.

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

### 3.3. Interface and Core

An Interface is a Contract for a relationship. It is not the identity of the Core that participates in that
relationship.

An Interface is still a natural place to gather related Operations and their Contracts. That authoring pattern is useful
because the same concern for coherence also appears when a Core is understood as one machine. This practical overlap
does not make Interface and Core the same concept.

One Interface may describe a relationship that uses most of a Core. Another may expose only a smaller part of the same
Core. A Core may therefore participate through more than one Interface when different relationships require different
contract surfaces.

The Contract does not infer Core boundaries from the number or shape of Interfaces. An Interface remains a relationship
surface. The Core remains the machine whose Contract material gives that relationship meaning.

### 3.4. Whole Machine

A Whole Machine exists when several Cores must work together to fulfill a larger machine purpose that no participating
Core fulfills alone.

The participating Cores remain machines of their own. Their Facts do not become one shared Fact set merely because the
Cores participate in the same Whole Machine. Their State-Machine meaning also remains local unless a later Whole Machine
decision explicitly establishes a relation that requires otherwise.

Whole Machine participation is therefore not a merger of complete Core contents. The Whole Machine concerns only the
Contract relations required by the larger purpose.

A relation may involve an Interface that exposes a coherent part of one Core. It may involve another contract surface
when that surface is the part that matters to the larger machine. The rest of the Core remains outside that relation.

This distinction is intentional. Whole Machine composition is understood at the level of Cores, while each particular
Whole Machine relation may concern only the relevant parts of those Cores.

The existence of several implementation calls between Cores does not establish a Whole Machine relation by itself. The
relation belongs to the Contract only when the larger machine purpose requires it as machine meaning.

### 3.5. Contract-First Scope

Whole Machine composition is defined as Contract meaning before any backend mechanism is selected.

No backend mechanism may supply missing semantics. A thread model cannot do so, and neither can a scheduler.
Communication and deployment choices also remain realization concerns. A later realization may choose any mechanism that
preserves the Contract and State-Machine meaning established by this ADR.

### 3.6. Distributed Whole Machine Problem Inventory

A distributed realization exposes failure surfaces that do not appear when every participating machine shares one
process and one failure boundary. ADR-0055 records them before deciding their final treatment so that distribution does
not silently add or change Contract meaning.

Each Contract Pipeline is closed within its Core. It does not know another Contract Pipeline and cannot directly observe
another Core's Facts, State, intermediate judgments, or internal Contract material. Material from outside a Core enters
the Contract Pipeline only through its Input Airlock. Contract-visible material leaves the Core only through its
Publication / Output boundary.

If material produced by one Core later participates in another Core, the first Core must first publish or output it.
Once it leaves that Core's authority, it is external material. The receiving Core may admit it only through its own
Input Airlock. Whole Machine composition therefore relates explicit Contract surfaces; it does not connect Contract
Pipeline internals and does not introduce a Contract-level communication channel between pipelines.

A backend may realize the movement of external material with an in-process transfer, IPC, RPC, a queue, a distributed
log, a network protocol, or another mechanism. Reachability, routing, connection state, packet or message transport, and
peer discovery belong to that realization. They are not Whole Machine Contract meaning merely because a distributed
backend uses them.

The same boundary excludes local execution failures that do not create a separate Whole Machine relation. A long runtime
pause, deadlock, livelock, or starvation remains a local realization problem. When such a condition consumes elapsed
time covered by an existing Time Budget, Budget owns the contractual limit and its enforcement boundary; ADR-0055 does
not define another time rule for it.

ADR-0055 defines no independent Lifecycle Contract. The contractual progress of a one-dimensional Contract Pipeline is
already determined by its applicable Contracts: when the current obligation is satisfied, processing may proceed to the
next applicable Contract; when it is not satisfied, Failure is established and that flow does not proceed through later
Contract obligations. Runtime lifecycle mechanisms such as startup, shutdown, restart, recovery, resume, replacement, or
checkpointing are realization concerns and are not specified by this ADR.

ADR-0055 does not decide whether retry, isolation, circuit breaking, or failover has any Contract-visible role. Their
control logic is not adopted as Whole Machine Contract meaning here. ADR-0056 Governance will revisit only whether an
explicit trigger or Contract World selection involving such a situation belongs to Governance.

The one-way pipeline law also excludes cyclic pipeline composition. A later interaction may return published material to
a Core only as new external input through that Core's Input Airlock. That is a new flow, not re-entry into the earlier
pipeline and not a Contract Pipeline cycle.

For this review, every remaining situation is placed in only one of two preliminary classes:

- **Contract** — the situation contains a machine-meaning choice that the backend must not decide on its own.
- **Implementation** — the situation belongs to realization. The compiler or backend must validate, prevent, recover
  from, or otherwise handle it while preserving the existing Contract.

The classification is intentionally provisional. It exists so that each row can now be challenged and moved or removed
one at a time. The remaining problem statements are preserved in the table, while the final column records the
implementation work that may still be required even when a row is classified as Contract.

#### Removed by Established Pipeline Laws

- One Core becomes unreachable
- Full network partition
- Partial partition
- Asymmetric partition
- Intermittent connectivity
- Message loss
- Unbounded or extreme delay
- Large latency variation
- Duplicate delivery
- Message reordering
- Corrupted or truncated transfer
- Connection reset during an exchange
- Half-open connection
- Network black hole
- Congestion or bandwidth collapse
- Stale routing
- Stale service discovery
- Misrouting
- Reconnection to a replacement instance
- Crash-recovery failure
- Long process pause
- Runtime stall
- Pause across a deadline
- Deadlock
- Livelock
- Starvation
- Repeated collaboration cycle
- Dependency cycle
- Restart with lost volatile state
- In-flight state loss
- Duplicate live instance
- Zombie participant
- Incomplete graceful shutdown
- Startup dependency failure
- Overlapping maintenance
- Instance replacement
- Recovery replay

- Crash-stop failure
- Local durable-storage failure
- Durable-storage latency
- Persisted-state corruption
- Retry changes order
- Failover changes order
- Optimistic conflict retry
- Replay after uncertain commit
- Failover data gap
- Snapshot restore
- Log replay duplication
- Log truncation after failover
- Load balancer sends work to draining instance
- Failover-target overload
- Rollback after migration
- Checkpoint failure
- Checkpoint timeout
- Restore replay

#### Deferred to Governance Review

The following situations are not decided by ADR-0055. They remain open only for ADR-0056 to determine whether an
explicit Governance trigger or Contract World selection is required.

- Retry storm
- Multi-layer retry amplification
- Synchronized retry
- Recovery from degraded mode
- Nested retry policies

#### Participant Failure

| Situation           | Classification | Whole Machine risk to examine                                                                              | Implementation detail to inspect                                                                                                                   |
|---------------------|----------------|------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| Orphaned held state | Implementation | A participant disappears while another Core still treats its reservation, ownership, or session as active. | The backend needs expiry, recovery ownership, or reconciliation so held state cannot remain indefinitely authoritative after its owner disappears. |

#### Time and Temporal Observation

| Situation                               | Classification | Whole Machine risk to examine                                                                             | Implementation detail to inspect                                                                                                                                                         |
|-----------------------------------------|----------------|-----------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Clock skew                              | Implementation | Different participants assign different wall-clock times to the same period of real execution.            | The backend should use monotonic time for elapsed bounds and validate any required wall-clock accuracy. A target that cannot meet the declared temporal guarantee must be rejected.      |
| Clock drift                             | Implementation | The difference between participant clocks grows while the Whole Machine runs.                             | The backend should use monotonic time for elapsed bounds and validate any required wall-clock accuracy. A target that cannot meet the declared temporal guarantee must be rejected.      |
| Clock jump                              | Implementation | A local clock moves forward or backward because its time source is corrected.                             | The backend should use monotonic time for elapsed bounds and validate any required wall-clock accuracy. A target that cannot meet the declared temporal guarantee must be rejected.      |
| Time-source failure                     | Implementation | A participant loses the source on which a time-sensitive guarantee depends.                               | The backend should use monotonic time for elapsed bounds and validate any required wall-clock accuracy. A target that cannot meet the declared temporal guarantee must be rejected.      |
| Timeout ambiguity                       | Contract       | A caller stops waiting even though the remote work may still be running or may already have completed.    | The Contract must decide which temporal observation has meaning. The backend then needs monotonic time, versioned observations, or an equivalent mechanism that preserves that decision. |
| Deadline propagation error              | Implementation | Different participants interpret the remaining time of one logical interaction differently.               | Deadline state must be propagated from one authoritative boundary rather than recomputed independently by each participant.                                                              |
| Lease-expiry ambiguity                  | Implementation | An old holder may still act while another participant believes the lease has expired.                     | The backend must carry enough temporal metadata to distinguish event time from processing delay without letting local clocks define Contract meaning.                                    |
| Event time differs from processing time | Contract       | Material represents an earlier real event but is processed after newer events.                            | The Contract must decide which temporal observation has meaning. The backend then needs monotonic time, versioned observations, or an equivalent mechanism that preserves that decision. |
| Late material                           | Contract       | Valid material arrives after the Whole Machine has already advanced based on its absence.                 | The Contract must decide which temporal observation has meaning. The backend then needs monotonic time, versioned observations, or an equivalent mechanism that preserves that decision. |
| No natural global instant               | Contract       | Facts observed from several Cores do not automatically describe one simultaneous Whole Machine condition. | The Contract must decide which temporal observation has meaning. The backend then needs monotonic time, versioned observations, or an equivalent mechanism that preserves that decision. |
| Inconsistent snapshot time              | Contract       | Different parts of one decision are observed at different logical moments.                                | The Contract must decide which temporal observation has meaning. The backend then needs monotonic time, versioned observations, or an equivalent mechanism that preserves that decision. |
| Timestamp reuse or collision            | Implementation | Two distinct events cannot be safely distinguished by time alone.                                         | The backend must carry enough temporal metadata to distinguish event time from processing delay without letting local clocks define Contract meaning.                                    |
| Lost scheduled action                   | Implementation | A time-triggered action disappears during failure, restart, or ownership movement.                        | Timer ownership and durable scheduling need stable identity so recovery cannot silently lose or duplicate one logical scheduled action.                                                  |
| Duplicate scheduled action              | Implementation | Recovery or ownership movement causes the same logical timer action to fire more than once.               | Timer ownership and durable scheduling need stable identity so recovery cannot silently lose or duplicate one logical scheduled action.                                                  |
| Stale scheduled action                  | Contract       | A timer fires after the condition that originally authorized it has already changed.                      | The Contract must decide which temporal observation has meaning. The backend then needs monotonic time, versioned observations, or an equivalent mechanism that preserves that decision. |

#### Interaction Identity, Retry, and Delivery Semantics

| Situation                             | Classification | Whole Machine risk to examine                                                                                            | Implementation detail to inspect                                                                                                                                                                                                |
|---------------------------------------|----------------|--------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Retried interaction                   | Implementation | One logical request produces several physical attempts.                                                                  | The realization must map physical attempts to one logical Interaction and preserve the terminal result across transport failure, cancellation, and client abandonment.                                                          |
| Duplicate request                     | Implementation | The same logical interaction reaches a participant more than once.                                                       | The realization must map physical attempts to one logical Interaction and preserve the terminal result across transport failure, cancellation, and client abandonment.                                                          |
| Ambiguous interaction identity        | Contract       | Participants cannot tell whether two attempts represent the same contractual interaction.                                | The Contract must determine the logical Interaction and the allowed visible effect of repetition or uncertainty. The backend then preserves it with stable identity, deduplication, transactional apply, or equivalent support. |
| Reused interaction identity           | Implementation | An identifier is recycled and an old attempt is confused with a new interaction.                                         | The realization must map physical attempts to one logical Interaction and preserve the terminal result across transport failure, cancellation, and client abandonment.                                                          |
| Delayed duplicate                     | Implementation | An old duplicate arrives after a newer interaction has already changed the machine.                                      | The realization must map physical attempts to one logical Interaction and preserve the terminal result across transport failure, cancellation, and client abandonment.                                                          |
| Response loss after success           | Contract       | The remote side completes successfully but the caller never receives the success result.                                 | The Contract must determine the logical Interaction and the allowed visible effect of repetition or uncertainty. The backend then preserves it with stable identity, deduplication, transactional apply, or equivalent support. |
| Failure response after remote success | Contract       | The caller observes failure even though the callee has already established an effect.                                    | The Contract must determine the logical Interaction and the allowed visible effect of repetition or uncertainty. The backend then preserves it with stable identity, deduplication, transactional apply, or equivalent support. |
| Client abandonment                    | Implementation | The caller stops caring about the result while the remote work continues.                                                | The realization must map physical attempts to one logical Interaction and preserve the terminal result across transport failure, cancellation, and client abandonment.                                                          |
| Cancellation race                     | Contract       | Cancellation and successful completion cross so that each side can observe a different terminal story.                   | The Contract must determine the logical Interaction and the allowed visible effect of repetition or uncertainty. The backend then preserves it with stable identity, deduplication, transactional apply, or equivalent support. |
| Hedged execution                      | Implementation | Several concurrent attempts race to complete one logical interaction.                                                    | Retry and hedging must share one logical Interaction identity and a bounded retry policy so extra attempts cannot become extra Contract effects.                                                                                |
| Duplicate side effect                 | Contract       | Repetition creates an effect that cannot safely occur more than once.                                                    | The Contract must determine the logical Interaction and the allowed visible effect of repetition or uncertainty. The backend then preserves it with stable identity, deduplication, transactional apply, or equivalent support. |
| At-most-once loss                     | Contract       | Suppressing duplicates causes uncertain work to be discarded even when the original attempt may never have taken effect. | The Contract must determine the logical Interaction and the allowed visible effect of repetition or uncertainty. The backend then preserves it with stable identity, deduplication, transactional apply, or equivalent support. |
| At-least-once duplication             | Contract       | Retrying until acknowledgement allows the same effect to occur more than once.                                           | The Contract must determine the logical Interaction and the allowed visible effect of repetition or uncertainty. The backend then preserves it with stable identity, deduplication, transactional apply, or equivalent support. |
| Exactly-once scope mismatch           | Contract       | A subsystem provides exactly-once handling inside its own boundary while an external effect remains duplicable.          | The Contract must determine the logical Interaction and the allowed visible effect of repetition or uncertainty. The backend then preserves it with stable identity, deduplication, transactional apply, or equivalent support. |
| Idempotency mismatch                  | Contract       | One layer treats an interaction as repeatable while another layer does not.                                              | The Contract must determine the logical Interaction and the allowed visible effect of repetition or uncertainty. The backend then preserves it with stable identity, deduplication, transactional apply, or equivalent support. |
| Deduplication-window expiry           | Implementation | A very late retry arrives after the receiver has forgotten prior interaction identity.                                   | Deduplication material needs a retention and recovery strategy that covers every retry window the backend claims to support.                                                                                                    |
| Deduplication-state loss              | Implementation | Recovery removes the material needed to recognize a repeated attempt.                                                    | Deduplication material needs a retention and recovery strategy that covers every retry window the backend claims to support.                                                                                                    |

#### Ordering and Causality

| Situation                             | Classification | Whole Machine risk to examine                                                                    | Implementation detail to inspect                                                                                                                                                                       |
|---------------------------------------|----------------|--------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Send order differs from receive order | Implementation | A receiver observes interactions in another order than the sender issued them.                   | The backend must not treat send order, receive order, retry order, or failover order as Contract authority unless a declared ordering law requires it.                                                 |
| No global total order                 | Implementation | Different participants can validly observe concurrent events in different orders.                | The backend must not treat send order, receive order, retry order, or failover order as Contract authority unless a declared ordering law requires it.                                                 |
| Cross-channel reordering              | Implementation | Two communication paths preserve local order but not their order relative to each other.         | The backend must not treat send order, receive order, retry order, or failover order as Contract authority unless a declared ordering law requires it.                                                 |
| Causal dependency arrives late        | Contract       | An effect is observed before the information on which it contractually depends.                  | The Contract must state the dependency, allowed interleavings, or completion condition that changes meaning. The backend may enforce it with sequencing, causal metadata, buffering, or serialization. |
| Concurrent valid interleavings        | Contract       | Several orders are physically possible even though only some may preserve Whole Machine meaning. | The Contract must state the dependency, allowed interleavings, or completion condition that changes meaning. The backend may enforce it with sequencing, causal metadata, buffering, or serialization. |
| Equal or incomparable timestamps      | Implementation | Time metadata cannot establish the required order between two events.                            | Timestamps alone should not establish order. The backend needs sequence, version, or causal metadata when an implementation ordering guarantee is required.                                            |
| Fan-out completion skew               | Implementation | Several branches of one larger activity finish at different times.                               | Branch completion should be tracked by logical participant identity rather than wall-clock arrival. Missing work must not be guessed complete.                                                         |
| Missing fan-in participant            | Contract       | A relation waits for one branch that has failed or will never produce the expected material.     | The Contract must state the dependency, allowed interleavings, or completion condition that changes meaning. The backend may enforce it with sequencing, causal metadata, buffering, or serialization. |
| Late event after transition           | Contract       | Material valid for an earlier state arrives after the machine has entered a later state.         | The Contract must state the dependency, allowed interleavings, or completion condition that changes meaning. The backend may enforce it with sequencing, causal metadata, buffering, or serialization. |

#### Observation, Replication, and Consistency

| Situation                     | Classification | Whole Machine risk to examine                                                                              | Implementation detail to inspect                                                                                                                                                                         |
|-------------------------------|----------------|------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Stale read                    | Contract       | A participant observes an older value while a newer authoritative value already exists.                    | The Contract must determine what observation may be treated as authoritative for the relation. The backend then needs the corresponding read consistency, snapshot, durability, or visibility guarantee. |
| Replica lag                   | Implementation | Different replicas of one participant expose different points in its history.                              | Replica selection must meet the required read guarantee. Lagging replicas should be fenced from observations they cannot answer safely.                                                                  |
| Read-your-writes violation    | Contract       | A participant cannot immediately observe a write it previously completed.                                  | The Contract must determine what observation may be treated as authoritative for the relation. The backend then needs the corresponding read consistency, snapshot, durability, or visibility guarantee. |
| Monotonic-read violation      | Contract       | A later read returns an older view than an earlier read.                                                   | The Contract must determine what observation may be treated as authoritative for the relation. The backend then needs the corresponding read consistency, snapshot, durability, or visibility guarantee. |
| Non-repeatable read           | Implementation | The same observation changes while one larger decision is still being formed.                              | The backend should provide the consistency level required by existing Contract meaning and reject a target whose observation model is too weak.                                                          |
| Fractured read                | Contract       | Related values are observed from different committed versions and never existed together.                  | The Contract must determine what observation may be treated as authoritative for the relation. The backend then needs the corresponding read consistency, snapshot, durability, or visibility guarantee. |
| Cross-Core snapshot skew      | Contract       | Facts from several Cores are individually valid but do not belong to one coherent Whole Machine condition. | The Contract must determine what observation may be treated as authoritative for the relation. The backend then needs the corresponding read consistency, snapshot, durability, or visibility guarantee. |
| Eventual-convergence delay    | Implementation | Replicas are expected to converge but remain different long enough to affect Whole Machine behavior.       | The backend should provide the consistency level required by existing Contract meaning and reject a target whose observation model is too weak.                                                          |
| Lost acknowledged state       | Contract       | A participant reports success for material that later disappears after failover or recovery.               | The Contract must determine what observation may be treated as authoritative for the relation. The backend then needs the corresponding read consistency, snapshot, durability, or visibility guarantee. |
| Visibility before durability  | Contract       | Another Core acts on state that has become visible but is not yet safely recoverable.                      | The Contract must determine what observation may be treated as authoritative for the relation. The backend then needs the corresponding read consistency, snapshot, durability, or visibility guarantee. |
| Durable state not yet visible | Implementation | Recovery material exists while current readers still observe an older condition.                           | Acknowledgement, visibility, and durability points must be ordered so recovery cannot contradict a result already exposed as established.                                                                |
| Stale cache                   | Implementation | A cache preserves a value after the source authority has changed.                                          | Cached material needs version or invalidation checks against the source authority before it can be used for a Contract-sensitive judgment.                                                               |
| Cache invalidation delay      | Implementation | Different participants invalidate old material at different times.                                         | Cached material needs version or invalidation checks against the source authority before it can be used for a Contract-sensitive judgment.                                                               |
| Different-replica observation | Implementation | Two callers reach different replicas and receive mutually inconsistent views.                              | Replica selection must meet the required read guarantee. Lagging replicas should be fenced from observations they cannot answer safely.                                                                  |
| Incomplete global observation | Implementation | No participant has enough current information to know the Whole Machine condition directly.                | The backend should provide the consistency level required by existing Contract meaning and reject a target whose observation model is too weak.                                                          |
| Uncommitted observation       | Contract       | One participant reacts to intermediate material that the producing side later abandons.                    | The Contract must determine what observation may be treated as authoritative for the relation. The backend then needs the corresponding read consistency, snapshot, durability, or visibility guarantee. |

#### Concurrent Change and Cross-Core Invariants

| Situation                                | Classification | Whole Machine risk to examine                                                                            | Implementation detail to inspect                                                                                                                                                                                                     |
|------------------------------------------|----------------|----------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Lost update                              | Implementation | Two writers base changes on the same prior value and one change silently overwrites the other.           | The backend must detect concurrent conflicts before committing an effect that would violate established Fact, State, or invariant meaning.                                                                                           |
| Check-then-act race                      | Implementation | A condition is true when checked but changes before the dependent action occurs.                         | The backend must detect concurrent conflicts before committing an effect that would violate established Fact, State, or invariant meaning.                                                                                           |
| Time-of-check to time-of-use gap         | Implementation | A remote fact becomes invalid between observation and use.                                               | The backend must detect concurrent conflicts before committing an effect that would violate established Fact, State, or invariant meaning.                                                                                           |
| Write skew                               | Contract       | Several changes are individually valid but jointly violate a rule.                                       | The Contract must define the invariant, transition law, cancellation result, or ordering-sensitive meaning. The backend then prevents invalid interleavings with validation, serialization, reservation, or equivalent coordination. |
| Conflicting state transitions            | Contract       | Different interactions concurrently establish transitions that cannot both be part of one valid history. | The Contract must define the invariant, transition law, cancellation result, or ordering-sensitive meaning. The backend then prevents invalid interleavings with validation, serialization, reservation, or equivalent coordination. |
| Duplicate transition                     | Contract       | Replay or retry applies the same logical movement more than once.                                        | The Contract must define the invariant, transition law, cancellation result, or ordering-sensitive meaning. The backend then prevents invalid interleavings with validation, serialization, reservation, or equivalent coordination. |
| Conflicting Fact establishment           | Contract       | Several participants establish material that cannot all be authoritative together.                       | The Contract must define the invariant, transition law, cancellation result, or ordering-sensitive meaning. The backend then prevents invalid interleavings with validation, serialization, reservation, or equivalent coordination. |
| Cross-Core invariant violation           | Contract       | Every Core satisfies its local law while their combined condition violates a Whole Machine law.          | The Contract must state the Whole Machine law. The backend may use transactions, validation, reservation, or another equivalent protocol to preserve it.                                                                             |
| Concurrent cancellation and completion   | Contract       | One path terminates work while another establishes a successful result.                                  | The Contract must define the invariant, transition law, cancellation result, or ordering-sensitive meaning. The backend then prevents invalid interleavings with validation, serialization, reservation, or equivalent coordination. |
| Concurrent workflows on the same subject | Implementation | Several Whole Machine activities contend over the same factual or state material.                        | The backend must detect concurrent conflicts before committing an effect that would violate established Fact, State, or invariant meaning.                                                                                           |
| Non-commutative changes                  | Contract       | The final meaning depends on which valid concurrent change is applied first.                             | The Contract must define the invariant, transition law, cancellation result, or ordering-sensitive meaning. The backend then prevents invalid interleavings with validation, serialization, reservation, or equivalent coordination. |
| Conflict-resolution semantic drift       | Contract       | A resolver picks a winner in a way that changes contract-visible meaning.                                | The Contract must define the invariant, transition law, cancellation result, or ordering-sensitive meaning. The backend then prevents invalid interleavings with validation, serialization, reservation, or equivalent coordination. |
| Stale lock holder                        | Implementation | A participant continues acting after another participant considers its ownership expired.                | Ownership needs epochs or fencing so stale holders cannot continue after transfer. Physical locks alone must not become Contract authority.                                                                                          |
| Ownership-transfer race                  | Implementation | Old and new owners overlap or leave a gap while authority moves.                                         | Ownership needs epochs or fencing so stale holders cannot continue after transfer. Physical locks alone must not become Contract authority.                                                                                          |

#### Atomicity, Commit, and Recovery

| Situation                              | Classification | Whole Machine risk to examine                                                                                       | Implementation detail to inspect                                                                                                                                                                                                            |
|----------------------------------------|----------------|---------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Partial commit                         | Contract       | One Core establishes its part of a larger change while another does not.                                            | The Contract must decide whether the larger effect is atomic, partially observable, compensable, or terminally uncertain. The backend may then use a transaction protocol, durable log, outbox, compensation, or an equivalent realization. |
| Uncertain commit result                | Contract       | A participant cannot determine whether a distributed change committed or aborted.                                   | The Contract must decide whether the larger effect is atomic, partially observable, compensable, or terminally uncertain. The backend may then use a transaction protocol, durable log, outbox, compensation, or an equivalent realization. |
| Coordinator failure                    | Implementation | The participant coordinating a larger change disappears during the decision.                                        | Commit coordination needs durable decision state and recovery rules so participant or coordinator failure cannot create two final decisions.                                                                                                |
| Participant failure before decision    | Implementation | One participant stops after receiving work but before the Whole Machine knows its outcome.                          | Commit coordination needs durable decision state and recovery rules so participant or coordinator failure cannot create two final decisions.                                                                                                |
| Participant failure after local commit | Implementation | One participant has committed locally before the remaining participants finish.                                     | Commit coordination needs durable decision state and recovery rules so participant or coordinator failure cannot create two final decisions.                                                                                                |
| Commit acknowledgement loss            | Implementation | Commit succeeds but the initiating participant does not receive confirmation.                                       | The implementation must preserve one durable final outcome across commit, acknowledgement loss, timeout, and replay.                                                                                                                        |
| Recovery disagreement                  | Contract       | Participants recover with different beliefs about the final decision.                                               | The Contract must decide whether the larger effect is atomic, partially observable, compensable, or terminally uncertain. The backend may then use a transaction protocol, durable log, outbox, compensation, or an equivalent realization. |
| Blocking commit                        | Implementation | Remaining participants cannot safely finish because a required decision-maker is unavailable.                       | Commit coordination needs durable decision state and recovery rules so participant or coordinator failure cannot create two final decisions.                                                                                                |
| Rollback failure                       | Implementation | One participant cannot restore its prior condition after the larger activity fails.                                 | Rollback or compensation is a realization strategy and must be validated against effects that may already be externally visible or irreversible.                                                                                            |
| Partial rollback                       | Contract       | Some effects are reversed while others remain established.                                                          | The Contract must decide whether the larger effect is atomic, partially observable, compensable, or terminally uncertain. The backend may then use a transaction protocol, durable log, outbox, compensation, or an equivalent realization. |
| Compensation failure                   | Implementation | A later compensating action fails after earlier effects have become visible.                                        | Rollback or compensation is a realization strategy and must be validated against effects that may already be externally visible or irreversible.                                                                                            |
| Compensation race                      | Implementation | New work begins before compensation for earlier work has finished.                                                  | Rollback or compensation is a realization strategy and must be validated against effects that may already be externally visible or irreversible.                                                                                            |
| Non-invertible side effect             | Contract       | An effect cannot be exactly undone even if the larger activity fails.                                               | The Contract must decide whether the larger effect is atomic, partially observable, compensable, or terminally uncertain. The backend may then use a transaction protocol, durable log, outbox, compensation, or an equivalent realization. |
| External non-transactional effect      | Contract       | A remote payment, notification, device command, or other side effect cannot join the same atomic boundary.          | The Contract must decide whether the larger effect is atomic, partially observable, compensable, or terminally uncertain. The backend may then use a transaction protocol, durable log, outbox, compensation, or an equivalent realization. |
| State-and-message gap                  | Contract       | Durable state changes without the related outbound material, or outbound material appears without the state change. | The Contract must decide whether the larger effect is atomic, partially observable, compensable, or terminally uncertain. The backend may then use a transaction protocol, durable log, outbox, compensation, or an equivalent realization. |
| Cross-shard atomicity                  | Implementation | One logical change spans independently owned partitions.                                                            | The backend needs a cross-owner atomic protocol when the existing Contract requires one logical commit across independently owned partitions.                                                                                               |
| Transaction timeout during commit      | Implementation | A caller reaches its time boundary while participants are still deciding the commit result.                         | The implementation must preserve one durable final outcome across commit, acknowledgement loss, timeout, and replay.                                                                                                                        |

#### Replication, Leadership, and Membership

| Situation                    | Classification | Whole Machine risk to examine                                                                                  | Implementation detail to inspect                                                                                                                     |
|------------------------------|----------------|----------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| Split brain                  | Implementation | Two groups independently behave as if each were the authoritative continuation of one participant.             | The backend must preserve one Core authority with quorum, fencing, epochs, or an equivalent mechanism. Two active histories cannot both be accepted. |
| Multiple active leaders      | Implementation | More than one realization accepts work under the same leadership role.                                         | Leadership must be fenced so only one realization can establish authority-bearing effects for the same epoch.                                        |
| No leader                    | Implementation | A replicated participant remains available at the process level but cannot accept authority-bearing work.      | The backend must stop authority-bearing work or use an equivalent safe path until a valid leader exists. Mere process availability is not enough.    |
| Stale leader                 | Implementation | A former leader continues acting after leadership has moved elsewhere.                                         | Every authority-bearing write should carry a current epoch or fencing token so a former leader is rejected after handoff.                            |
| Leader-election delay        | Implementation | No participant can make progress while leadership is being re-established.                                     | Leadership and quorum are realization mechanisms. They need fencing and durable epochs so only the accepted continuation can establish Core effects. |
| Minority-side activity       | Implementation | A participant separated from quorum continues serving work that the majority will not accept.                  | Replication must remain an implementation of one Core authority. Replica placement or role must not create additional Contract Authorities.          |
| Quorum loss                  | Implementation | Too few replicas remain mutually reachable to preserve the selected consistency or progress guarantee.         | Leadership and quorum are realization mechanisms. They need fencing and durable epochs so only the accepted continuation can establish Core effects. |
| Replica divergence           | Implementation | Replicated histories cease to describe one state machine.                                                      | The replication layer must reconcile or reject divergent histories before exposing them as one Core state.                                           |
| Follower lag                 | Implementation | A replica remains alive but is too far behind to safely answer some observations.                              | Replication must remain an implementation of one Core authority. Replica placement or role must not create additional Contract Authorities.          |
| Membership change            | Implementation | The set of replicas or voters changes while the machine is running.                                            | Membership changes need versioned configurations and overlap rules so old and new groups cannot both establish authority independently.              |
| Inconsistent membership view | Implementation | Participants disagree about who currently belongs to the replication group.                                    | Membership changes need versioned configurations and overlap rules so old and new groups cannot both establish authority independently.              |
| Reconfiguration overlap      | Implementation | Old and new membership configurations are active during the same period.                                       | Membership changes need versioned configurations and overlap rules so old and new groups cannot both establish authority independently.              |
| Replica join                 | Implementation | A new replica becomes visible before it has caught up enough to preserve required meaning.                     | Membership changes need versioned configurations and overlap rules so old and new groups cannot both establish authority independently.              |
| Replica removal              | Implementation | A removed replica continues serving or accepting work.                                                         | Membership changes need versioned configurations and overlap rules so old and new groups cannot both establish authority independently.              |
| Leader lease uncertainty     | Implementation | A leader relies on time-based authority while another participant may already consider that authority expired. | Leadership and quorum are realization mechanisms. They need fencing and durable epochs so only the accepted continuation can establish Core effects. |
| Consensus liveness limit     | Implementation | Required agreement may preserve safety while progress becomes impossible under the current failures.           | Replication must remain an implementation of one Core authority. Replica placement or role must not create additional Contract Authorities.          |

#### Partitioning, Sharding, and Ownership

| Situation                    | Classification | Whole Machine risk to examine                                                                | Implementation detail to inspect                                                                                                                                          |
|------------------------------|----------------|----------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Hot shard                    | Implementation | One ownership partition receives disproportionate load and becomes the limiting participant. | Shard ownership, movement, and routing are realization concerns. Ownership versions or fencing should prevent overlap, gaps, and stale routing during resharding.         |
| Uneven distribution          | Implementation | Work or state is spread unevenly across partitions.                                          | Shard ownership, movement, and routing are realization concerns. Ownership versions or fencing should prevent overlap, gaps, and stale routing during resharding.         |
| Stale shard routing          | Implementation | A request goes to the former owner after ownership has moved.                                | Shard ownership, movement, and routing are realization concerns. Ownership versions or fencing should prevent overlap, gaps, and stale routing during resharding.         |
| Duplicate ownership          | Implementation | Two participants concurrently believe they own the same partition.                           | Shard ownership, movement, and routing are realization concerns. Ownership versions or fencing should prevent overlap, gaps, and stale routing during resharding.         |
| Ownership gap                | Implementation | No participant currently accepts authority for a partition.                                  | Shard ownership, movement, and routing are realization concerns. Ownership versions or fencing should prevent overlap, gaps, and stale routing during resharding.         |
| Rebalance in progress        | Implementation | Work arrives while ownership is being redistributed.                                         | Shard ownership, movement, and routing are realization concerns. Ownership versions or fencing should prevent overlap, gaps, and stale routing during resharding.         |
| Shard split                  | Implementation | One ownership unit becomes several while interactions remain active.                         | Shard ownership, movement, and routing are realization concerns. Ownership versions or fencing should prevent overlap, gaps, and stale routing during resharding.         |
| Shard merge                  | Implementation | Several ownership units become one while their histories differ.                             | Shard ownership, movement, and routing are realization concerns. Ownership versions or fencing should prevent overlap, gaps, and stale routing during resharding.         |
| Cross-shard ordering         | Implementation | Related interactions span partitions that do not share one natural order.                    | Partition-local order must not be mistaken for Whole Machine order. The backend needs an explicit cross-partition ordering mechanism only when the Contract requires one. |
| Cross-shard invariant        | Implementation | A rule depends on state held by independently updated partitions.                            | The backend must coordinate independently owned partitions strongly enough to preserve the existing invariant. Shard boundaries cannot weaken the law.                    |
| Cross-shard transaction      | Implementation | One atomic requirement spans separate partition owners.                                      | Cross-shard commit support must be validated before an atomic Contract guarantee is accepted for the selected backend.                                                    |
| Resharding with version skew | Implementation | Ownership changes while old and new participants understand the material differently.        | Shard ownership, movement, and routing are realization concerns. Ownership versions or fencing should prevent overlap, gaps, and stale routing during resharding.         |

#### Discovery, Topology, and Failure Domains

| Situation                   | Classification | Whole Machine risk to examine                                                                     | Implementation detail to inspect                                                                                                                                                |
|-----------------------------|----------------|---------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Stale membership            | Implementation | A participant acts on an old view of which machines currently exist.                              | Discovery and topology must bind each required relation to the correct live participant. Placement metadata itself remains outside Contract authority.                          |
| Inconsistent membership     | Implementation | Different Cores have different current membership views.                                          | Discovery and topology must bind each required relation to the correct live participant. Placement metadata itself remains outside Contract authority.                          |
| Rapid membership churn      | Implementation | Participants repeatedly enter and leave before the system stabilizes.                             | Discovery and topology must bind each required relation to the correct live participant. Placement metadata itself remains outside Contract authority.                          |
| Address reuse               | Implementation | A network address now identifies a different realization from the one cached by a caller.         | Discovery and topology must bind each required relation to the correct live participant. Placement metadata itself remains outside Contract authority.                          |
| DNS staleness               | Implementation | Name resolution continues returning an old endpoint after placement changes.                      | Discovery and topology must bind each required relation to the correct live participant. Placement metadata itself remains outside Contract authority.                          |
| Health-check false positive | Implementation | A participant is declared healthy even though it cannot preserve the required machine meaning.    | Operational health signals must not substitute for Contract conditions. The backend may use them only to select a realization that can still preserve the declared relation.    |
| Health-check false negative | Implementation | A participant that could safely serve work is removed from participation.                         | Operational health signals must not substitute for Contract conditions. The backend may use them only to select a realization that can still preserve the declared relation.    |
| Readiness mismatch          | Implementation | Operational readiness differs from the contractual condition required by another Core.            | Operational health signals must not substitute for Contract conditions. The backend may use them only to select a realization that can still preserve the declared relation.    |
| Control-plane failure       | Implementation | Placement or membership control is unavailable while existing data-plane work may still continue. | Discovery and topology must bind each required relation to the correct live participant. Placement metadata itself remains outside Contract authority.                          |
| Data-plane failure          | Implementation | Control infrastructure remains healthy while required machine interactions cannot proceed.        | Discovery and topology must bind each required relation to the correct live participant. Placement metadata itself remains outside Contract authority.                          |
| Zone failure                | Implementation | Many supposedly independent realizations disappear under one infrastructure failure.              | Failure-domain placement should be validated against any declared independence guarantee. Hidden common dependencies must not defeat a guarantee the backend claims to realize. |
| Region failure              | Implementation | A broad geographic failure removes several participants and communication paths together.         | Failure-domain placement should be validated against any declared independence guarantee. Hidden common dependencies must not defeat a guarantee the backend claims to realize. |
| Correlated failure          | Implementation | A common cause defeats several redundant participants at once.                                    | Failure-domain placement should be validated against any declared independence guarantee. Hidden common dependencies must not defeat a guarantee the backend claims to realize. |
| Shared dependency failure   | Implementation | Independent Cores fail together because they rely on the same hidden service or resource.         | Failure-domain placement should be validated against any declared independence guarantee. Hidden common dependencies must not defeat a guarantee the backend claims to realize. |

#### Load, Capacity, and Failure Amplification

| Situation                      | Classification | Whole Machine risk to examine                                                                               | Implementation detail to inspect                                                                                                                                                                  |
|--------------------------------|----------------|-------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Participant overload           | Implementation | A Core remains reachable but cannot process work within useful bounds.                                      | The backend must coordinate admission, Capacity, and backpressure across the physical relation so overload cannot silently change Contract results.                                               |
| Queue buildup                  | Implementation | Work accumulates faster than a participant can complete it.                                                 | Queues need explicit physical bounds and backpressure. The backend must reject or delay work before hidden buffering defeats Budget or Capacity guarantees.                                       |
| Missing backpressure           | Implementation | A faster producer overwhelms a slower consumer.                                                             | The backend must coordinate admission, Capacity, and backpressure across the physical relation so overload cannot silently change Contract results.                                               |
| Backpressure propagation       | Implementation | Pressure in one Core travels upstream and changes the behavior of otherwise healthy Cores.                  | The backend must coordinate admission, Capacity, and backpressure across the physical relation so overload cannot silently change Contract results.                                               |
| Head-of-line blocking          | Implementation | Slow work prevents unrelated work behind it from progressing.                                               | The backend must coordinate admission, Capacity, and backpressure across the physical relation so overload cannot silently change Contract results.                                               |
| Thundering herd                | Implementation | Many participants wake or reconnect together after one shared event.                                        | Retries and reconnection need jitter, budgets, and throttling so failure handling does not amplify load beyond the Capacity the backend can safely realize.                                       |
| Connection storm               | Implementation | Recovery creates a sudden surge of new connections before useful work begins.                               | Retries and reconnection need jitter, budgets, and throttling so failure handling does not amplify load beyond the Capacity the backend can safely realize.                                       |
| Resource exhaustion            | Implementation | CPU, memory, queues, sockets, file descriptors, or other finite resources are consumed by distributed work. | The backend must coordinate admission, Capacity, and backpressure across the physical relation so overload cannot silently change Contract results.                                               |
| Load imbalance                 | Implementation | Some realizations are overloaded while equivalent capacity remains idle elsewhere.                          | The backend must coordinate admission, Capacity, and backpressure across the physical relation so overload cannot silently change Contract results.                                               |
| Slow consumer                  | Implementation | One receiver cannot drain material at the rate it is produced.                                              | The backend must coordinate admission, Capacity, and backpressure across the physical relation so overload cannot silently change Contract results.                                               |
| Unbounded buffering            | Implementation | The system preserves incoming work by consuming memory or storage without a stable limit.                   | Queues need explicit physical bounds and backpressure. The backend must reject or delay work before hidden buffering defeats Budget or Capacity guarantees.                                       |
| Bounded-buffer loss            | Contract       | Work is dropped or rejected after a queue reaches its configured limit.                                     | Existing admission and Capacity meaning must decide whether work may be dropped or admitted across the relation. The backend must enforce that decision before overload changes visible behavior. |
| Noisy neighbor                 | Implementation | Unrelated work on shared infrastructure removes capacity needed by a participating Core.                    | The backend must coordinate admission, Capacity, and backpressure across the physical relation so overload cannot silently change Contract results.                                               |
| Autoscaling lag                | Implementation | New capacity arrives after overload has already changed Whole Machine behavior.                             | Scaling is realization. New or retiring instances must become eligible only when they can preserve current state, version, and Capacity requirements.                                             |
| Scale-out warm-up              | Implementation | Newly added instances exist but are not yet capable of carrying normal work.                                | Scaling is realization. New or retiring instances must become eligible only when they can preserve current state, version, and Capacity requirements.                                             |
| Scale-in interruption          | Implementation | Capacity removal terminates or relocates work that was still in progress.                                   | Scaling is realization. New or retiring instances must become eligible only when they can preserve current state, version, and Capacity requirements.                                             |
| Cascading failure              | Implementation | Failure or overload in one participant increases pressure until other participants fail.                    | The backend needs isolation, admission control, and bounded retries so one overloaded participant cannot consume the resources needed for recovery elsewhere.                                     |
| Metastable overload            | Implementation | The Whole Machine remains overloaded even after the original traffic spike has ended.                       | The backend needs isolation, admission control, and bounded retries so one overloaded participant cannot consume the resources needed for recovery elsewhere.                                     |
| Capacity mismatch across Cores | Contract       | One stage can admit work faster than the next required Core can safely accept it.                           | Existing admission and Capacity meaning must decide whether work may be dropped or admitted across the relation. The backend must enforce that decision before overload changes visible behavior. |
| Fan-out amplification          | Implementation | One admitted interaction creates enough downstream work to overload otherwise healthy participants.         | The backend must coordinate admission, Capacity, and backpressure across the physical relation so overload cannot silently change Contract results.                                               |
| Straggler amplification        | Implementation | Completion of a larger relation is dominated by its slowest required participant.                           | The backend must coordinate admission, Capacity, and backpressure across the physical relation so overload cannot silently change Contract results.                                               |

#### Version, Schema, Configuration, and Contract World Skew

| Situation                      | Classification | Whole Machine risk to examine                                                                      | Implementation detail to inspect                                                                                                                                         |
|--------------------------------|----------------|----------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Rolling version skew           | Implementation | Old and new realizations participate in the Whole Machine at the same time.                        | Deployment sequencing must keep every simultaneously active realization semantically compatible with the Contract versions it can encounter.                             |
| Contract version skew          | Contract       | Collaborating Cores resolve different revisions of a Contract that must relate.                    | The Contract must decide which version combinations may relate. The compiler or backend should reject bindings that cannot preserve that decision.                       |
| Operation-surface mismatch     | Contract       | One participant expects an interaction that another version no longer provides or has changed.     | The required interaction surface is Contract meaning. Binding and deployment validation should reject a participant that cannot provide the selected Operation contract. |
| Fact-shape mismatch            | Contract       | The same named material is represented under incompatible definitions.                             | Fact identity and canonical field meaning are contractual. Cross-version adapters may exist only when they preserve that meaning exactly.                                |
| Serialization mismatch         | Implementation | Material can no longer be decoded or interpreted consistently across versions.                     | The backend needs versioned encoding and migration gates so stored or transmitted material is not interpreted under the wrong schema.                                    |
| Data-schema skew               | Implementation | Stored state has not migrated to the schema expected by every active participant.                  | The backend needs versioned encoding and migration gates so stored or transmitted material is not interpreted under the wrong schema.                                    |
| Protocol negotiation mismatch  | Implementation | Participants select incompatible versions or disagree about the selected version.                  | Protocol and rollout machinery must select only combinations that preserve the already chosen Contract meaning.                                                          |
| Configuration drift            | Implementation | Equivalent participants run with different effective settings.                                     | Configuration and feature rollout must be validated so equivalent realizations do not silently execute different semantics.                                              |
| Feature-selection skew         | Implementation | A feature is active in only part of the Whole Machine.                                             | Configuration and feature rollout must be validated so equivalent realizations do not silently execute different semantics.                                              |
| Policy World skew              | Contract       | Participating Cores operate under incompatible Contract Worlds.                                    | The Whole Machine must define whether collaborating Cores may use different Worlds. The backend then verifies or coordinates the allowed combination.                    |
| Governance-selection skew      | Contract       | Different parts of one intended Whole Machine believe different governing arrangements are active. | Governance meaning must determine the active arrangement. Runtime rollout must not let different participants silently act under incompatible selections.                |
| Stale Contract World cache     | Implementation | A participant continues using a previously valid world after governance has changed it.            | Protocol and rollout machinery must select only combinations that preserve the already chosen Contract meaning.                                                          |
| Partial policy rollout         | Implementation | Rules that should change together become active at different times.                                | Protocol and rollout machinery must select only combinations that preserve the already chosen Contract meaning.                                                          |
| Upgrade before state migration | Implementation | New code interprets state that still has old meaning.                                              | The backend needs versioned encoding and migration gates so stored or transmitted material is not interpreted under the wrong schema.                                    |
| Mixed replica versions         | Implementation | Replicas of what should be one machine do not execute equivalent semantics.                        | Deployment sequencing must keep every simultaneously active realization semantically compatible with the Contract versions it can encounter.                             |
| Downgrade incompatibility      | Implementation | A failed upgrade cannot safely return to the previous version.                                     | Deployment sequencing must keep every simultaneously active realization semantically compatible with the Contract versions it can encounter.                             |

#### Availability and Degraded Operation

| Situation                 | Classification | Whole Machine risk to examine                                                                     | Implementation detail to inspect                                                                                                                                                                                              |
|---------------------------|----------------|---------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Partial availability      | Contract       | Some Whole Machine functions remain possible while other required Cores are unavailable.          | The degraded behavior changes what the Whole Machine promises or returns, so that meaning must be explicit. The backend may enter the mode only under the declared condition and must preserve its weaker guarantees exactly. |
| Read-only degradation     | Contract       | Observation remains possible while mutation can no longer be safely accepted.                     | The degraded behavior changes what the Whole Machine promises or returns, so that meaning must be explicit. The backend may enter the mode only under the declared condition and must preserve its weaker guarantees exactly. |
| Reduced-function mode     | Contract       | The machine continues with a deliberately smaller set of guarantees or operations.                | The degraded behavior changes what the Whole Machine promises or returns, so that meaning must be explicit. The backend may enter the mode only under the declared condition and must preserve its weaker guarantees exactly. |
| Fail-open behavior        | Contract       | The system continues despite being unable to establish a normally required condition.             | The degraded behavior changes what the Whole Machine promises or returns, so that meaning must be explicit. The backend may enter the mode only under the declared condition and must preserve its weaker guarantees exactly. |
| Fail-closed behavior      | Contract       | The system rejects work whenever a required condition cannot be established.                      | The degraded behavior changes what the Whole Machine promises or returns, so that meaning must be explicit. The backend may enter the mode only under the declared condition and must preserve its weaker guarantees exactly. |
| Fallback semantic change  | Contract       | A fallback path returns a different quality or meaning from the normal relation.                  | The degraded behavior changes what the Whole Machine promises or returns, so that meaning must be explicit. The backend may enter the mode only under the declared condition and must preserve its weaker guarantees exactly. |
| Stale fallback            | Contract       | Cached or replicated material is used because the authoritative participant is unavailable.       | The degraded behavior changes what the Whole Machine promises or returns, so that meaning must be explicit. The backend may enter the mode only under the declared condition and must preserve its weaker guarantees exactly. |
| Alternate-region behavior | Implementation | Failover reaches a location with different latency, capacity, version, or available dependencies. | Failover placement and routing are implementation choices. The backend must verify that the alternate target still satisfies the Contract version, dependencies, Budget, and Capacity required by the relation.               |
| Load shedding             | Contract       | Some admitted or incoming work is deliberately rejected to preserve the rest of the machine.      | The degraded behavior changes what the Whole Machine promises or returns, so that meaning must be explicit. The backend may enter the mode only under the declared condition and must preserve its weaker guarantees exactly. |
| Brownout                  | Contract       | Optional work is disabled so that required work can continue.                                     | The degraded behavior changes what the Whole Machine promises or returns, so that meaning must be explicit. The backend may enter the mode only under the declared condition and must preserve its weaker guarantees exactly. |
| Partial result            | Contract       | A caller receives only the part of the larger result that could be established.                   | The degraded behavior changes what the Whole Machine promises or returns, so that meaning must be explicit. The backend may enter the mode only under the declared condition and must preserve its weaker guarantees exactly. |
| Best-effort completion    | Contract       | The Whole Machine continues even though some participating obligations remain unresolved.         | The degraded behavior changes what the Whole Machine promises or returns, so that meaning must be explicit. The backend may enter the mode only under the declared condition and must preserve its weaker guarantees exactly. |

#### Streaming, Event, and Queue-Based Realizations

| Situation                                   | Classification | Whole Machine risk to examine                                                                                  | Implementation detail to inspect                                                                                                                                                      |
|---------------------------------------------|----------------|----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Out-of-order event                          | Implementation | Event material arrives in a different order from its event-time order.                                         | The streaming backend must preserve logical event identity and any declared ordering while tolerating duplicate, late, missing, or reordered physical delivery.                       |
| Late event                                  | Implementation | An event arrives after a window, decision, or state progression has already closed around its absence.         | The streaming backend must preserve logical event identity and any declared ordering while tolerating duplicate, late, missing, or reordered physical delivery.                       |
| Duplicate event                             | Implementation | A broker, producer, consumer, or recovery path causes one event to be processed repeatedly.                    | The streaming backend must preserve logical event identity and any declared ordering while tolerating duplicate, late, missing, or reordered physical delivery.                       |
| Missing event                               | Implementation | A consumer never observes material that another part of the system considers published.                        | The streaming backend must preserve logical event identity and any declared ordering while tolerating duplicate, late, missing, or reordered physical delivery.                       |
| Incorrect progress marker                   | Implementation | A watermark, offset, or equivalent progress claim advances beyond material that may still arrive.              | The streaming backend must preserve logical event identity and any declared ordering while tolerating duplicate, late, missing, or reordered physical delivery.                       |
| Event-time and processing-time disagreement | Contract       | Results differ depending on whether the machine reasons about when an event occurred or when it was processed. | The Contract must decide the event-time, completion, or external-effect semantics. Queue offsets, watermarks, checkpoints, and retries may realize that meaning but cannot choose it. |
| External sink outside checkpoint boundary   | Contract       | Internal exactly-once recovery does not prevent duplicate effects in an external system.                       | The Contract must decide the event-time, completion, or external-effect semantics. Queue offsets, watermarks, checkpoints, and retries may realize that meaning but cannot choose it. |
| Consumer rebalance                          | Implementation | Ownership of event partitions moves while records are being processed.                                         | Partition ownership changes need epochs and state handoff so old and new consumers cannot both process the same authority-bearing range.                                              |
| Offset committed before effect              | Implementation | Recovery skips work whose external effect never completed.                                                     | Offset advancement and external effect completion need an atomic or replay-safe relationship so recovery cannot lose or repeat work silently.                                         |
| Effect completed before offset commit       | Implementation | Recovery repeats work whose effect already occurred.                                                           | Offset advancement and external effect completion need an atomic or replay-safe relationship so recovery cannot lose or repeat work silently.                                         |
| Partition-local ordering only               | Implementation | Each partition has order while no equivalent global order exists across partitions.                            | The streaming backend must preserve logical event identity and any declared ordering while tolerating duplicate, late, missing, or reordered physical delivery.                       |
| Repartitioning                              | Implementation | A change in partition ownership or keying changes where order and local state are maintained.                  | Partition ownership changes need epochs and state handoff so old and new consumers cannot both process the same authority-bearing range.                                              |
| Backlog growth                              | Implementation | Event processing falls behind real-world event production.                                                     | The queue backend needs bounded backlog, retry limits, and isolation of repeatedly failing material so one item cannot destroy progress for unrelated work.                           |
| Poison material                             | Implementation | One item repeatedly fails and blocks or destabilizes normal progress.                                          | The queue backend needs bounded backlog, retry limits, and isolation of repeatedly failing material so one item cannot destroy progress for unrelated work.                           |
| Dead-letter divergence                      | Contract       | Failed material leaves the normal flow and no longer participates in the same completion semantics.            | The Contract must decide the event-time, completion, or external-effect semantics. Queue offsets, watermarks, checkpoints, and retries may realize that meaning but cannot choose it. |
| Join waits on absent stream                 | Contract       | A multi-input relation cannot finish because one input never arrives.                                          | The Contract must decide the event-time, completion, or external-effect semantics. Queue offsets, watermarks, checkpoints, and retries may realize that meaning but cannot choose it. |
| Idle input stalls progress                  | Contract       | One silent partition or stream prevents a global progress condition from advancing.                            | The Contract must decide the event-time, completion, or external-effect semantics. Queue offsets, watermarks, checkpoints, and retries may realize that meaning but cannot choose it. |

#### Security, Trust, and Arbitrary Faults

| Situation                          | Classification | Whole Machine risk to examine                                                                 | Implementation detail to inspect                                                                                                                                                                                       |
|------------------------------------|----------------|-----------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Sender spoofing                    | Implementation | Material appears to come from an authoritative participant when it does not.                  | The transport and identity layer need authenticated integrity so unauthoritative or modified material is rejected before it enters Contract processing.                                                                |
| Authentication disagreement        | Contract       | Different Cores disagree about the identity behind one interaction.                           | Identity, authorization, replay validity, tenant authority, or protocol strength changes machine meaning and must follow explicit Contract or Policy law. The backend enforces it cryptographically and operationally. |
| Authorization skew                 | Contract       | One participant accepts an action that another active policy would reject.                    | Identity, authorization, replay validity, tenant authority, or protocol strength changes machine meaning and must follow explicit Contract or Policy law. The backend enforces it cryptographically and operationally. |
| Credential expiry under clock skew | Implementation | Participants disagree about whether a time-limited credential remains valid.                  | Rotation and expiry need overlapping trust windows or coordinated rollout so valid participants do not silently diverge in authentication capability.                                                                  |
| Replay attack                      | Contract       | Previously valid material is presented again outside its intended interaction.                | Identity, authorization, replay validity, tenant authority, or protocol strength changes machine meaning and must follow explicit Contract or Policy law. The backend enforces it cryptographically and operationally. |
| Message tampering                  | Implementation | Material is modified between authoritative production and consumption.                        | The transport and identity layer need authenticated integrity so unauthoritative or modified material is rejected before it enters Contract processing.                                                                |
| Compromised participant            | Implementation | A participant deliberately or arbitrarily violates its expected behavior.                     | Ordinary crash-fault backends are insufficient for arbitrary behavior. Byzantine detection or agreement is required only if that stronger failure model is supported.                                                  |
| Equivocation                       | Implementation | One participant sends incompatible claims to different peers.                                 | Ordinary crash-fault backends are insufficient for arbitrary behavior. Byzantine detection or agreement is required only if that stronger failure model is supported.                                                  |
| Arbitrary corrupted output         | Implementation | A participant continues responding but produces invalid material rather than simply crashing. | Ordinary crash-fault backends are insufficient for arbitrary behavior. Byzantine detection or agreement is required only if that stronger failure model is supported.                                                  |
| Certificate rotation skew          | Implementation | Some participants trust a new identity while others still require the old one.                | Rotation and expiry need overlapping trust windows or coordinated rollout so valid participants do not silently diverge in authentication capability.                                                                  |
| Secret rotation skew               | Implementation | Authentication material changes at different times across the Whole Machine.                  | Rotation and expiry need overlapping trust windows or coordinated rollout so valid participants do not silently diverge in authentication capability.                                                                  |
| Revoked credential acceptance      | Contract       | A participant keeps accepting authority after revocation should have taken effect.            | Identity, authorization, replay validity, tenant authority, or protocol strength changes machine meaning and must follow explicit Contract or Policy law. The backend enforces it cryptographically and operationally. |
| Cross-tenant identity mix-up       | Contract       | Material from one tenant or authority domain is attributed to another.                        | Identity, authorization, replay validity, tenant authority, or protocol strength changes machine meaning and must follow explicit Contract or Policy law. The backend enforces it cryptographically and operationally. |
| Protocol downgrade                 | Contract       | Participants are induced to use weaker semantics than the intended Whole Machine relation.    | Identity, authorization, replay validity, tenant authority, or protocol strength changes machine meaning and must follow explicit Contract or Policy law. The backend enforces it cryptographically and operationally. |

#### Diagnostics, Attribution, and Audit

| Situation                                     | Classification | Whole Machine risk to examine                                                                                     | Implementation detail to inspect                                                                                                                                                          |
|-----------------------------------------------|----------------|-------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Client reports failure while server succeeded | Contract       | The two sides record different terminal outcomes for one interaction.                                             | When terminal outcome, interaction identity, failure attribution, or required evidence is contract-visible, the diagnostic record must preserve that meaning across retries and recovery. |
| Logs are temporally reordered                 | Implementation | Unsynchronized clocks make diagnostic records appear in a misleading sequence.                                    | The observability backend should carry stable interaction identity and causal metadata. Monitoring loss or clock skew must not be mistaken for a machine-state change.                    |
| Missing telemetry                             | Implementation | The observation system loses evidence from one participant or network segment.                                    | The observability backend should carry stable interaction identity and causal metadata. Monitoring loss or clock skew must not be mistaken for a machine-state change.                    |
| Duplicate telemetry                           | Implementation | Retry or replay produces repeated diagnostic evidence for one underlying event.                                   | The observability backend should carry stable interaction identity and causal metadata. Monitoring loss or clock skew must not be mistaken for a machine-state change.                    |
| Broken trace continuity                       | Implementation | Retry, failover, or asynchronous work loses the relation between parts of one interaction.                        | Trace context must survive asynchronous hops, retries, and failover so implementation work can still be attributed to the correct logical interaction.                                    |
| Inconsistent interaction identifiers          | Contract       | Different participants attribute the same activity to different identities.                                       | When terminal outcome, interaction identity, failure attribution, or required evidence is contract-visible, the diagnostic record must preserve that meaning across retries and recovery. |
| Failure attribution ambiguity                 | Contract       | Several plausible failure points exist and no participant has a complete causal view.                             | When terminal outcome, interaction identity, failure attribution, or required evidence is contract-visible, the diagnostic record must preserve that meaning across retries and recovery. |
| Stale health signal                           | Implementation | Monitoring reports a condition that is no longer true.                                                            | The observability backend should carry stable interaction identity and causal metadata. Monitoring loss or clock skew must not be mistaken for a machine-state change.                    |
| Conflicting health signals                    | Implementation | Different observers report incompatible views of one participant.                                                 | The observability backend should carry stable interaction identity and causal metadata. Monitoring loss or clock skew must not be mistaken for a machine-state change.                    |
| Monitoring-plane partition                    | Implementation | The machine may continue while its diagnostic system cannot observe it.                                           | The observability backend should carry stable interaction identity and causal metadata. Monitoring loss or clock skew must not be mistaken for a machine-state change.                    |
| Partial audit trail                           | Contract       | Some authority-bearing actions are durable while their required evidence is missing.                              | When terminal outcome, interaction identity, failure attribution, or required evidence is contract-visible, the diagnostic record must preserve that meaning across retries and recovery. |
| Duplicate audit record                        | Contract       | Replay records one authoritative action more than once.                                                           | When terminal outcome, interaction identity, failure attribution, or required evidence is contract-visible, the diagnostic record must preserve that meaning across retries and recovery. |
| Unknown terminal outcome                      | Contract       | Recovery and evidence cannot establish whether the logical interaction succeeded, failed, or remained incomplete. | When terminal outcome, interaction identity, failure attribution, or required evidence is contract-visible, the diagnostic record must preserve that meaning across retries and recovery. |

#### External Dependencies and Irreversible Effects

| Situation                           | Classification | Whole Machine risk to examine                                                                      | Implementation detail to inspect                                                                                                                                                         |
|-------------------------------------|----------------|----------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Third-party timeout                 | Implementation | An external dependency may complete after the Whole Machine has stopped waiting.                   | The adapter must preserve an uncertain outcome when acknowledgement is missing and reconcile later completion instead of assuming that the external effect failed.                       |
| Third-party duplicate processing    | Implementation | Retry causes an external dependency to apply the same request more than once.                      | Adapters need stable external idempotency keys or reconciliation when available. If the dependency cannot support the required effect semantics, the backend must reject that guarantee. |
| External system without idempotency | Implementation | Kontrakt-controlled retry cannot prevent repeated external effects.                                | Adapters need stable external idempotency keys or reconciliation when available. If the dependency cannot support the required effect semantics, the backend must reject that guarantee. |
| External rate limiting              | Implementation | A dependency remains healthy but rejects work because its own capacity boundary is reached.        | External rate limits need to feed admission and retry control so the local machine does not amplify rejection into overload.                                                             |
| External version change             | Implementation | A dependency changes behavior or schema outside the Whole Machine deployment cycle.                | The backend should validate the dependency version and documented guarantees before binding it to a required Whole Machine relation.                                                     |
| External stale data                 | Implementation | A remote source returns material that is valid but no longer current.                              | The backend should validate the dependency version and documented guarantees before binding it to a required Whole Machine relation.                                                     |
| Webhook replay                      | Implementation | An external caller resends an earlier notification after the local machine has advanced.           | Adapters need stable external idempotency keys or reconciliation when available. If the dependency cannot support the required effect semantics, the backend must reject that guarantee. |
| Delayed callback                    | Implementation | A valid response arrives after the state in which it was requested has ended.                      | The adapter must preserve an uncertain outcome when acknowledgement is missing and reconcile later completion instead of assuming that the external effect failed.                       |
| Human approval delay                | Implementation | A person completes a required decision after the machine context has changed.                      | The realization cannot promise exact rollback for an irreversible or human effect. Any stronger Contract guarantee must be rejected or expressed with a different terminal model.        |
| Human duplicate action              | Implementation | The same approval or command is submitted more than once.                                          | Adapters need stable external idempotency keys or reconciliation when available. If the dependency cannot support the required effect semantics, the backend must reject that guarantee. |
| Irreversible external effect        | Implementation | A payment, physical actuation, notification, or legal action cannot be exactly rolled back.        | The realization cannot promise exact rollback for an irreversible or human effect. Any stronger Contract guarantee must be rejected or expressed with a different terminal model.        |
| External acknowledgement loss       | Implementation | The effect occurs but the Whole Machine receives no reliable confirmation.                         | The adapter must preserve an uncertain outcome when acknowledgement is missing and reconcile later completion instead of assuming that the external effect failed.                       |
| Unknown dependency failure model    | Implementation | The Whole Machine relies on another distributed system whose guarantees are weaker or unspecified. | The backend should validate the dependency version and documented guarantees before binding it to a required Whole Machine relation.                                                     |

#### Determinism and Realization Leakage

| Situation                              | Classification | Whole Machine risk to examine                                                                                | Implementation detail to inspect                                                                                                                       |
|----------------------------------------|----------------|--------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scheduler-dependent result             | Contract       | Different thread or process scheduling changes a contract-visible result.                                    | This is forbidden realization leakage. The compiler or runtime must canonicalize the choice or reject a plan whose schedule can change the result.     |
| Network-race winner                    | Contract       | The first arriving response determines meaning even though arrival order is only physical.                   | Arrival order may select a result only when the Contract explicitly authorizes that race. Otherwise the backend needs deterministic arbitration.       |
| Replica-choice result                  | Contract       | Reading from a different valid replica changes the contract-visible answer.                                  | Equivalent replicas must not change Contract meaning. The backend must strengthen the read or reject a replica choice that can alter the result.       |
| Leader-location result                 | Contract       | Leadership placement changes the meaning of an otherwise identical interaction.                              | Leadership placement is realization. The compiler must reject or normalize any path where leader location changes contractual output.                  |
| Retry-count result                     | Contract       | A different number of physical attempts changes contractual output or State.                                 | Physical attempt count must not alter the logical Interaction result unless repetition is explicitly part of the Contract.                             |
| Recovery-path result                   | Contract       | Normal execution and recovery produce different contract-visible meaning from the same established material. | Recovery must re-establish the same Contract-visible meaning as normal execution from the same authoritative material.                                 |
| Topology-dependent result              | Contract       | Placement in one host, zone, or region changes Contract meaning without an explicit obligation requiring it. | Host, zone, and region placement must remain invisible unless the Contract explicitly gives topology semantic meaning.                                 |
| Nondeterministic conflict winner       | Contract       | Several valid contenders exist and the realization chooses one without Contract law.                         | A winner that changes meaning needs Contract law. Otherwise the backend must use a deterministic rule that preserves the declared semantics.           |
| Unspecified tie breaking               | Contract       | Equal candidates produce different results across conforming realizations.                                   | Equal contractual candidates need an explicit law or a canonical deterministic backend rule that cannot change observable meaning.                     |
| Message-order-dependent meaning        | Contract       | Two physically valid delivery orders produce different contractual outcomes without declared ordering law.   | If order matters, the Contract must say so. Otherwise the backend must prevent physical delivery order from choosing different results.                |
| Partial-failure-dependent meaning      | Contract       | The realization invents a new semantic result only because a particular node or network path failed.         | Failure may cause a declared Failure or unavailable realization, but the backend cannot invent a new semantic result from topology or node loss alone. |
| Local randomness or wall-clock leakage | Contract       | A backend-local source changes meaning that the Contract did not authorize it to choose.                     | Backend-local entropy and time cannot choose Contract meaning unless the Contract explicitly authorizes those inputs.                                  |

#### Fundamental Guarantee Boundaries

| Situation                                                                  | Classification | Whole Machine risk to examine                                                                                                        | Implementation detail to inspect                                                                                                                        |
|----------------------------------------------------------------------------|----------------|--------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| Failure cannot be distinguished from delay                                 | Implementation | A fully asynchronous environment does not provide a perfect general test for whether a silent participant is dead or merely slow.    | The backend must use a declared failure model and conservative timeouts. It cannot claim perfect failure detection in a fully asynchronous target.      |
| Consensus cannot always terminate under full asynchrony with crash failure | Implementation | Agreement may preserve safety while no algorithm can guarantee progress under the strongest asynchronous assumptions.                | A requested agreement and liveness guarantee needs stronger timing or failure assumptions. The backend must reject an unrealizable combination.         |
| Partition forces a consistency or availability choice                      | Implementation | A Whole Machine cannot assume both unrestricted availability and one strongly consistent shared view during arbitrary partition.     | The backend must validate the requested consistency and availability guarantees against the supported partition model before deployment.                |
| Global state is not obtained by ordinary local reads                       | Implementation | A coherent distributed snapshot requires additional semantics or realization support.                                                | A coherent Whole Machine observation requires snapshot, version, or equivalent coordination support when the Contract needs one shared condition.       |
| Hard deadline over unbounded delay                                         | Implementation | No backend can guarantee remote completion within a fixed time if communication delay has no bound.                                  | The compiler must reject a remote hard deadline guarantee when the target provides no sufficient bound on communication and processing delay.           |
| Progress without required participants                                     | Implementation | A contract cannot require completion if the participants whose agreement or effects are necessary are permanently unavailable.       | Required progress must be rejected as unrealizable when permanently unavailable participants are necessary to establish the result.                     |
| Exactly-once end-to-end over arbitrary effects                             | Implementation | Local deduplication or transactional processing does not automatically make every external effect occur exactly once.                | The backend must check the complete effect boundary. Local transactions or deduplication cannot justify an end-to-end exactly-once claim by themselves. |
| Atomicity across non-transactional participants                            | Implementation | A strong all-or-nothing guarantee may be unrealizable when a required participant cannot take part in an equivalent commit boundary. | The backend must reject strong all-or-nothing semantics when a required participant cannot join an equivalent atomic protocol.                          |
| Perfect global clock assumption                                            | Implementation | Distributed participants cannot treat unsynchronized local clocks as one exact source of global event order.                         | The backend must use bounded clock uncertainty or logical ordering when required. Unsynchronized wall clocks cannot establish exact global order.       |
| Byzantine tolerance assumption                                             | Implementation | Arbitrary or malicious faults require a stronger failure model than ordinary crash and omission failures.                            | The selected backend must explicitly support the stronger fault model before any Byzantine-tolerant guarantee is accepted.                              |

The inventory above is a review surface, not the final Whole Machine contract model. A Contract row still needs an exact
Contract form before ADR-0055 can close. An Implementation row must remain outside Contract Authority even when the
compiler or backend needs substantial validation or runtime machinery to preserve the Contract.

Implementation mechanisms such as consensus algorithms, transaction protocols, retries, queues, locks, leases,
replication strategies, and network topology do not become Contract Authorities merely because they may be used to
address one of these situations.

---

## 4. Verification

Verification must reject any Whole Machine definition whose meaning depends on undeclared realization structure.

The Core guidance in Section 3.2 does not authorize Kontrakt to guess or enforce one correct decomposition. It guides
the engineering judgment used to keep one Core understandable as one machine.

Verification must also preserve the distinction between Core participation and relation scope. Participation in one
Whole Machine must not make all Contract material of one Core implicitly available to another Core or to every Whole
Machine relation.

Verification must reject a Contract Pipeline that directly observes another pipeline's internal Contract material,
re-enters an earlier Contract stage, or forms a cycle. Published material that later returns to a Core must be treated
as new external material admitted through that Core's Input Airlock.

The exact verification rules for Whole Machine relations remain open until those relations are decided.

---

## 5. Deferred Decisions

The following remain open for the continuation of ADR-0055:

- the identity and exact boundary of a Whole Machine;
- the exact Contract forms by which participating Core surfaces relate;
- whether existing Interface material is sufficient for every Whole Machine relation;
- fan-out, fan-in, and completion;
- cross-Core Contract material and authority boundaries;
- coexistence and ordering where they are observable;
- Contract World continuity across collaborating Cores;
- the meaning of physical separation when it is itself an obligation;
- whether retry, isolation, circuit breaking, failover, or related resilience controls expose any Contract-visible
  Governance trigger or Contract World selection;
- distributed realization after the Contract model is complete;
- and determinism for valid Whole Machine compositions.

These questions are decided in Contract terms first. Realization follows afterward.

---

## 6. Consequences

### Positive

ADR-0055 does not duplicate the independence and determinism already established for one-dimensional Contracts. It now
separates Core from Interface while preserving the practical reason that Interface authoring often aligns naturally with
Core formation. It also establishes that Whole Machine composition is about relations among Cores without treating every
Contract inside those Cores as part of every relation.

The remaining work can therefore focus on the first boundary where relations among participating Core surfaces become
Contract meaning.

### Negative

The Whole Machine model remains incomplete until the deferred relation and composition questions are resolved.

### Neutral

The realization axis remains intentionally open while the Contract model is being defined.