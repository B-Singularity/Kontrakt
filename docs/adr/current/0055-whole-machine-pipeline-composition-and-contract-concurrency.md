# ADR-0055: Whole Machine, Pipeline Composition, and Contract Concurrency

## Status

Accepted

## Date

2026-08-12

## Related

- `../../the-most-important-thing/what-contract-is.md`
- `../../todo/kontrakt-verifier-implementation-plan.md`
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

Physical concurrency and execution mechanisms are realization concerns. ADR-0055 therefore does not define threads,
scheduling, communication, waiting, or synchronization as Contract meaning.

The Contract problem begins only when independent Cores participate in a larger machine purpose. The question is not how
their realizations coordinate. The question is what Contract surfaces may relate while each Contract Pipeline remains
independent, one-way, and authoritative only over its own flow.

An Operation remains one explicit contractual interaction. Internal functions, workers, calls, or transport steps used
to realize it do not create additional Contract flows or Whole Machine meaning.

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

A Whole Machine exists when independent Cores participate in a larger machine purpose.

Each admitted Input starts one independent Contract flow through one Contract Pipeline. Once admitted, that flow
proceeds without Contract-level interference from other flows or pipelines. It sees only material admitted through its
own Input Airlock and is judged only by the Contracts applicable at its own boundaries. Each applicable Contract either
satisfies its obligation and allows the flow to continue, or establishes Failure.

Whole Machine composition adds no waiting, joining, producer identity, source counting, or arrival-order semantics. A
boundary cares only whether the material presented there satisfies its declared Contract. One realization, several
realizations, or any combination of them may produce that material; if the same Contract is satisfied, the producer
topology is irrelevant unless provenance itself was explicitly declared as Contract material.

Published material may later become external material for another Core. If it satisfies that Core's Input Contract, it
starts a new independent flow there. Material presented later never extends or modifies a flow that was already
admitted.

Whole Machine relations are therefore acyclic relations among explicit Contract surfaces. Contract Pipelines do not
share internals, communicate directly, wait for one another, or merge their execution. Any synchronization, transport,
aggregation, buffering, or waiting needed to realize those relations belongs to implementation.

### 3.5. Contract-First Scope

ADR-0055 defines only the Contract relations above. A backend may realize them by any mechanism that preserves those
relations and may not supply missing Contract meaning from execution structure, communication, timing, or topology.

### 3.6. Distributed Whole Machine Realization Threat Inventory

Distributed realization can still fail to preserve the simple Contract model above. The tables in this section record
implementation threats only so later backend design does not overlook them. They do not create Contract concepts and
they do not prescribe mitigation, recovery, protocol, topology, or runtime mechanisms.

The backend implementation document must review the threats that apply to its architecture and define its concrete
responses there.

ADR-0055 defines no independent Lifecycle Contract. Startup, shutdown, restart, resume, checkpointing, local recovery,
and equivalent runtime lifecycle work remain backend concerns.

ADR-0055 also does not decide whether retry, isolation, circuit breaking, or failover exposes any Contract-visible
Governance concern. ADR-0056 may revisit only that Governance question; the mechanisms themselves remain realization.

#### Excluded by Established Contract Laws

- Repeated collaboration cycle
- Dependency cycle
- Causal dependency arrives late
- Late event after transition
- Late material
- Missing fan-in participant
- Join waits on absent stream
- Idle input stalls progress

#### Deferred to Governance Review

The following situations are not decided by ADR-0055. ADR-0056 may consider only whether they expose an explicit
Governance trigger or Contract World selection. Their resilience mechanisms are not defined here.

- Retry storm
- Multi-layer retry amplification
- Synchronized retry
- Recovery from degraded mode
- Nested retry policies

#### Deferred to Backend Lifecycle Work

- Crash-recovery failure
- Long process pause
- Runtime stall
- Pause across a deadline
- Deadlock
- Livelock
- Starvation
- Restart with lost volatile state
- In-flight state loss
- Incomplete graceful shutdown
- Startup dependency failure
- Overlapping maintenance
- Instance replacement
- Recovery replay
- Crash-stop failure
- Local durable-storage failure
- Durable-storage latency
- Persisted-state corruption
- Snapshot restore
- Log replay duplication
- Log truncation after failover
- Rollback after migration
- Checkpoint failure
- Checkpoint timeout
- Restore replay

#### Communication and Reachability

| Situation                              | Threat to preserve Contract meaning against                                                                                         |
|----------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| One Core becomes unreachable           | A realization endpoint needed to carry external material to a declared Contract surface cannot be reached.                          |
| Full network partition                 | The selected distributed realization cannot transfer required external material across separated locations.                         |
| Partial partition                      | Some realization paths for required external material remain available while others are unavailable.                                |
| Asymmetric partition                   | A transfer path works in one direction while its acknowledgement or return path is unavailable.                                     |
| Intermittent connectivity              | A realization path repeatedly disappears and returns while material transfer or related work remains in progress.                   |
| Message loss                           | External material produced for a declared Input boundary is not delivered to that boundary.                                         |
| Unbounded or extreme delay             | External material reaches an Input boundary much later than the realization expected.                                               |
| Large latency variation                | Transfer and processing time varies enough to violate assumptions made by the distributed realization.                              |
| Duplicate delivery                     | The same external material is presented to an Input boundary more than once.                                                        |
| Message reordering                     | External material is presented in a different physical order from the order in which the realization emitted it.                    |
| Corrupted or truncated transfer        | Material presented at an Input boundary is incomplete or no longer represents the material produced for that boundary.              |
| Connection reset during an exchange    | The transport ends while the realization is still carrying material or a result across a Contract boundary.                         |
| Half-open connection                   | One realization endpoint treats a transport path as usable after the opposite endpoint is no longer usable.                         |
| Network black hole                     | A transport path accepts traffic but neither delivers the material nor returns a useful failure signal.                             |
| Congestion or bandwidth collapse       | A transport path remains present but cannot carry external material at the rate assumed by the realization.                         |
| Stale routing                          | External material is sent through a route that no longer leads to the realization currently bound to the intended Contract surface. |
| Stale service discovery                | The realization continues targeting an endpoint that is no longer bound to the intended Contract surface.                           |
| Misrouting                             | External material is presented to a live realization that is not bound to the intended Contract surface.                            |
| Reconnection to a replacement instance | A transport path reconnects to a replacement realization whose local execution context may differ from the previous endpoint.       |

#### Participant Failure

| Situation               | Threat to preserve Contract meaning against                                                                |
|-------------------------|------------------------------------------------------------------------------------------------------------|
| Duplicate live instance | Two realizations concurrently believe they represent the same participant.                                 |
| Zombie participant      | An old instance resumes after another instance has already taken over its role.                            |
| Orphaned held state     | A participant disappears while another Core still treats its reservation, ownership, or session as active. |

#### Time and Temporal Observation

| Situation                               | Threat to preserve Contract meaning against                                                               |
|-----------------------------------------|-----------------------------------------------------------------------------------------------------------|
| Clock skew                              | Different participants assign different wall-clock times to the same period of real execution.            |
| Clock drift                             | The difference between participant clocks grows while the Whole Machine runs.                             |
| Clock jump                              | A local clock moves forward or backward because its time source is corrected.                             |
| Time-source failure                     | A participant loses the source on which a time-sensitive guarantee depends.                               |
| Timeout ambiguity                       | A caller stops waiting even though the remote work may still be running or may already have completed.    |
| Deadline propagation error              | Different participants interpret the remaining time of one logical interaction differently.               |
| Lease-expiry ambiguity                  | An old holder may still act while another participant believes the lease has expired.                     |
| Event time differs from processing time | Material represents an earlier real event but is processed after newer events.                            |
| No natural global instant               | Facts observed from several Cores do not automatically describe one simultaneous Whole Machine condition. |
| Inconsistent snapshot time              | Different parts of one decision are observed at different logical moments.                                |
| Timestamp reuse or collision            | Two distinct events cannot be safely distinguished by time alone.                                         |
| Lost scheduled action                   | A time-triggered action disappears during failure, restart, or ownership movement.                        |
| Duplicate scheduled action              | Recovery or ownership movement causes the same logical timer action to fire more than once.               |
| Stale scheduled action                  | A timer fires after the condition that originally authorized it has already changed.                      |

#### Interaction Identity, Retry, and Delivery Semantics

| Situation                             | Threat to preserve Contract meaning against                                                                              |
|---------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| Retried interaction                   | One logical request produces several physical attempts.                                                                  |
| Duplicate request                     | The same logical interaction reaches a participant more than once.                                                       |
| Ambiguous interaction identity        | Participants cannot tell whether two attempts represent the same contractual interaction.                                |
| Reused interaction identity           | An identifier is recycled and an old attempt is confused with a new interaction.                                         |
| Delayed duplicate                     | An old duplicate arrives after a newer interaction has already changed the machine.                                      |
| Response loss after success           | The remote side completes successfully but the caller never receives the success result.                                 |
| Failure response after remote success | The caller observes failure even though the callee has already established an effect.                                    |
| Client abandonment                    | The caller stops caring about the result while the remote work continues.                                                |
| Cancellation race                     | Cancellation and successful completion cross so that each side can observe a different terminal story.                   |
| Hedged execution                      | Several concurrent attempts race to complete one logical interaction.                                                    |
| Duplicate side effect                 | Repetition creates an effect that cannot safely occur more than once.                                                    |
| At-most-once loss                     | Suppressing duplicates causes uncertain work to be discarded even when the original attempt may never have taken effect. |
| At-least-once duplication             | Retrying until acknowledgement allows the same effect to occur more than once.                                           |
| Exactly-once scope mismatch           | A subsystem provides exactly-once handling inside its own boundary while an external effect remains duplicable.          |
| Idempotency mismatch                  | One layer treats an interaction as repeatable while another layer does not.                                              |
| Deduplication-window expiry           | A very late retry arrives after the receiver has forgotten prior interaction identity.                                   |
| Deduplication-state loss              | Recovery removes the material needed to recognize a repeated attempt.                                                    |

#### Ordering and Causality

| Situation                             | Threat to preserve Contract meaning against                                                      |
|---------------------------------------|--------------------------------------------------------------------------------------------------|
| Retry changes order                   | A later attempt overtakes earlier work after a communication failure.                            |
| Failover changes order                | A replacement participant observes or applies pending work in a different order.                 |
| Send order differs from receive order | A receiver observes interactions in another order than the sender issued them.                   |
| No global total order                 | Different participants can validly observe concurrent events in different orders.                |
| Cross-channel reordering              | Two communication paths preserve local order but not their order relative to each other.         |
| Concurrent valid interleavings        | Several orders are physically possible even though only some may preserve Whole Machine meaning. |
| Equal or incomparable timestamps      | Time metadata cannot establish the required order between two events.                            |
| Fan-out completion skew               | Several branches of one larger activity finish at different times.                               |

#### Observation, Replication, and Consistency

| Situation                     | Threat to preserve Contract meaning against                                                                |
|-------------------------------|------------------------------------------------------------------------------------------------------------|
| Stale read                    | A participant observes an older value while a newer authoritative value already exists.                    |
| Replica lag                   | Different replicas of one participant expose different points in its history.                              |
| Read-your-writes violation    | A participant cannot immediately observe a write it previously completed.                                  |
| Monotonic-read violation      | A later read returns an older view than an earlier read.                                                   |
| Non-repeatable read           | The same observation changes while one larger decision is still being formed.                              |
| Fractured read                | Related values are observed from different committed versions and never existed together.                  |
| Cross-Core snapshot skew      | Facts from several Cores are individually valid but do not belong to one coherent Whole Machine condition. |
| Eventual-convergence delay    | Replicas are expected to converge but remain different long enough to affect Whole Machine behavior.       |
| Lost acknowledged state       | A participant reports success for material that later disappears after failover or recovery.               |
| Visibility before durability  | Another Core acts on state that has become visible but is not yet safely recoverable.                      |
| Durable state not yet visible | Recovery material exists while current readers still observe an older condition.                           |
| Stale cache                   | A cache preserves a value after the source authority has changed.                                          |
| Cache invalidation delay      | Different participants invalidate old material at different times.                                         |
| Different-replica observation | Two callers reach different replicas and receive mutually inconsistent views.                              |
| Incomplete global observation | No participant has enough current information to know the Whole Machine condition directly.                |
| Uncommitted observation       | One participant reacts to intermediate material that the producing side later abandons.                    |

#### Concurrent Change and Cross-Core Invariants

| Situation                                | Threat to preserve Contract meaning against                                                              |
|------------------------------------------|----------------------------------------------------------------------------------------------------------|
| Optimistic conflict retry                | A conflict forces re-execution after the world has changed.                                              |
| Lost update                              | Two writers base changes on the same prior value and one change silently overwrites the other.           |
| Check-then-act race                      | A condition is true when checked but changes before the dependent action occurs.                         |
| Time-of-check to time-of-use gap         | A remote fact becomes invalid between observation and use.                                               |
| Write skew                               | Several changes are individually valid but jointly violate a rule.                                       |
| Conflicting state transitions            | Different interactions concurrently establish transitions that cannot both be part of one valid history. |
| Duplicate transition                     | Replay or retry applies the same logical movement more than once.                                        |
| Conflicting Fact establishment           | Several participants establish material that cannot all be authoritative together.                       |
| Cross-Core invariant violation           | Every Core satisfies its local law while their combined condition violates a Whole Machine law.          |
| Concurrent cancellation and completion   | One path terminates work while another establishes a successful result.                                  |
| Concurrent workflows on the same subject | Several Whole Machine activities contend over the same factual or state material.                        |
| Non-commutative changes                  | The final meaning depends on which valid concurrent change is applied first.                             |
| Conflict-resolution semantic drift       | A resolver picks a winner in a way that changes contract-visible meaning.                                |
| Stale lock holder                        | A participant continues acting after another participant considers its ownership expired.                |
| Ownership-transfer race                  | Old and new owners overlap or leave a gap while authority moves.                                         |

#### Atomicity, Commit, and Recovery

| Situation                              | Threat to preserve Contract meaning against                                                                         |
|----------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| Replay after uncertain commit          | Recovery repeats work because the previous final outcome is unknown.                                                |
| Partial commit                         | One Core establishes its part of a larger change while another does not.                                            |
| Uncertain commit result                | A participant cannot determine whether a distributed change committed or aborted.                                   |
| Coordinator failure                    | The participant coordinating a larger change disappears during the decision.                                        |
| Participant failure before decision    | One participant stops after receiving work but before the Whole Machine knows its outcome.                          |
| Participant failure after local commit | One participant has committed locally before the remaining participants finish.                                     |
| Commit acknowledgement loss            | Commit succeeds but the initiating participant does not receive confirmation.                                       |
| Recovery disagreement                  | Participants recover with different beliefs about the final decision.                                               |
| Blocking commit                        | Remaining participants cannot safely finish because a required decision-maker is unavailable.                       |
| Rollback failure                       | One participant cannot restore its prior condition after the larger activity fails.                                 |
| Partial rollback                       | Some effects are reversed while others remain established.                                                          |
| Compensation failure                   | A later compensating action fails after earlier effects have become visible.                                        |
| Compensation race                      | New work begins before compensation for earlier work has finished.                                                  |
| Non-invertible side effect             | An effect cannot be exactly undone even if the larger activity fails.                                               |
| External non-transactional effect      | A remote payment, notification, device command, or other side effect cannot join the same atomic boundary.          |
| State-and-message gap                  | Durable state changes without the related outbound material, or outbound material appears without the state change. |
| Cross-shard atomicity                  | One logical change spans independently owned partitions.                                                            |
| Transaction timeout during commit      | A caller reaches its time boundary while participants are still deciding the commit result.                         |

#### Replication, Leadership, and Membership

| Situation                    | Threat to preserve Contract meaning against                                                                    |
|------------------------------|----------------------------------------------------------------------------------------------------------------|
| Failover data gap            | Work visible before failure is absent from the replacement leader.                                             |
| Split brain                  | Two groups independently behave as if each were the authoritative continuation of one participant.             |
| Multiple active leaders      | More than one realization accepts work under the same leadership role.                                         |
| No leader                    | A replicated participant remains available at the process level but cannot accept authority-bearing work.      |
| Stale leader                 | A former leader continues acting after leadership has moved elsewhere.                                         |
| Leader-election delay        | No participant can make progress while leadership is being re-established.                                     |
| Minority-side activity       | A participant separated from quorum continues serving work that the majority will not accept.                  |
| Quorum loss                  | Too few replicas remain mutually reachable to preserve the selected consistency or progress guarantee.         |
| Replica divergence           | Replicated histories cease to describe one state machine.                                                      |
| Follower lag                 | A replica remains alive but is too far behind to safely answer some observations.                              |
| Membership change            | The set of replicas or voters changes while the machine is running.                                            |
| Inconsistent membership view | Participants disagree about who currently belongs to the replication group.                                    |
| Reconfiguration overlap      | Old and new membership configurations are active during the same period.                                       |
| Replica join                 | A new replica becomes visible before it has caught up enough to preserve required meaning.                     |
| Replica removal              | A removed replica continues serving or accepting work.                                                         |
| Leader lease uncertainty     | A leader relies on time-based authority while another participant may already consider that authority expired. |
| Consensus liveness limit     | Required agreement may preserve safety while progress becomes impossible under the current failures.           |

#### Partitioning, Sharding, and Ownership

| Situation                    | Threat to preserve Contract meaning against                                                  |
|------------------------------|----------------------------------------------------------------------------------------------|
| Hot shard                    | One ownership partition receives disproportionate load and becomes the limiting participant. |
| Uneven distribution          | Work or state is spread unevenly across partitions.                                          |
| Stale shard routing          | A request goes to the former owner after ownership has moved.                                |
| Duplicate ownership          | Two participants concurrently believe they own the same partition.                           |
| Ownership gap                | No participant currently accepts authority for a partition.                                  |
| Rebalance in progress        | Work arrives while ownership is being redistributed.                                         |
| Shard split                  | One ownership unit becomes several while interactions remain active.                         |
| Shard merge                  | Several ownership units become one while their histories differ.                             |
| Cross-shard ordering         | Related interactions span partitions that do not share one natural order.                    |
| Cross-shard invariant        | A rule depends on state held by independently updated partitions.                            |
| Cross-shard transaction      | One atomic requirement spans separate partition owners.                                      |
| Resharding with version skew | Ownership changes while old and new participants understand the material differently.        |

#### Discovery, Topology, and Failure Domains

| Situation                                     | Threat to preserve Contract meaning against                                                       |
|-----------------------------------------------|---------------------------------------------------------------------------------------------------|
| Load balancer sends work to draining instance | New work reaches a participant that is intentionally leaving service.                             |
| Failover-target overload                      | Surviving locations cannot absorb the work redirected from a failed location.                     |
| Stale membership                              | A participant acts on an old view of which machines currently exist.                              |
| Inconsistent membership                       | Different Cores have different current membership views.                                          |
| Rapid membership churn                        | Participants repeatedly enter and leave before the system stabilizes.                             |
| Address reuse                                 | A network address now identifies a different realization from the one cached by a caller.         |
| DNS staleness                                 | Name resolution continues returning an old endpoint after placement changes.                      |
| Health-check false positive                   | A participant is declared healthy even though it cannot preserve the required machine meaning.    |
| Health-check false negative                   | A participant that could safely serve work is removed from participation.                         |
| Readiness mismatch                            | Operational readiness differs from the contractual condition required by another Core.            |
| Control-plane failure                         | Placement or membership control is unavailable while existing data-plane work may still continue. |
| Data-plane failure                            | Control infrastructure remains healthy while required machine interactions cannot proceed.        |
| Zone failure                                  | Many supposedly independent realizations disappear under one infrastructure failure.              |
| Region failure                                | A broad geographic failure removes several participants and communication paths together.         |
| Correlated failure                            | A common cause defeats several redundant participants at once.                                    |
| Shared dependency failure                     | Independent Cores fail together because they rely on the same hidden service or resource.         |

#### Load, Capacity, and Failure Amplification

| Situation                      | Threat to preserve Contract meaning against                                                                 |
|--------------------------------|-------------------------------------------------------------------------------------------------------------|
| Participant overload           | A Core remains reachable but cannot process work within useful bounds.                                      |
| Queue buildup                  | Work accumulates faster than a participant can complete it.                                                 |
| Missing backpressure           | A faster producer overwhelms a slower consumer.                                                             |
| Backpressure propagation       | Pressure in one Core travels upstream and changes the behavior of otherwise healthy Cores.                  |
| Head-of-line blocking          | Slow work prevents unrelated work behind it from progressing.                                               |
| Thundering herd                | Many participants wake or reconnect together after one shared event.                                        |
| Connection storm               | Recovery creates a sudden surge of new connections before useful work begins.                               |
| Resource exhaustion            | CPU, memory, queues, sockets, file descriptors, or other finite resources are consumed by distributed work. |
| Load imbalance                 | Some realizations are overloaded while equivalent capacity remains idle elsewhere.                          |
| Slow consumer                  | One receiver cannot drain material at the rate it is produced.                                              |
| Unbounded buffering            | The system preserves incoming work by consuming memory or storage without a stable limit.                   |
| Bounded-buffer loss            | Work is dropped or rejected after a queue reaches its configured limit.                                     |
| Noisy neighbor                 | Unrelated work on shared infrastructure removes capacity needed by a participating Core.                    |
| Autoscaling lag                | New capacity arrives after overload has already changed Whole Machine behavior.                             |
| Scale-out warm-up              | Newly added instances exist but are not yet capable of carrying normal work.                                |
| Scale-in interruption          | Capacity removal terminates or relocates work that was still in progress.                                   |
| Cascading failure              | Failure or overload in one participant increases pressure until other participants fail.                    |
| Metastable overload            | The Whole Machine remains overloaded even after the original traffic spike has ended.                       |
| Capacity mismatch across Cores | One stage can admit work faster than the next required Core can safely accept it.                           |
| Fan-out amplification          | One admitted interaction creates enough downstream work to overload otherwise healthy participants.         |
| Straggler amplification        | Completion of a larger relation is dominated by its slowest required participant.                           |

#### Version, Schema, Configuration, and Contract World Skew

| Situation                      | Threat to preserve Contract meaning against                                                        |
|--------------------------------|----------------------------------------------------------------------------------------------------|
| Rolling version skew           | Old and new realizations participate in the Whole Machine at the same time.                        |
| Contract version skew          | Collaborating Cores resolve different revisions of a Contract that must relate.                    |
| Operation-surface mismatch     | One participant expects an interaction that another version no longer provides or has changed.     |
| Fact-shape mismatch            | The same named material is represented under incompatible definitions.                             |
| Serialization mismatch         | Material can no longer be decoded or interpreted consistently across versions.                     |
| Data-schema skew               | Stored state has not migrated to the schema expected by every active participant.                  |
| Protocol negotiation mismatch  | Participants select incompatible versions or disagree about the selected version.                  |
| Configuration drift            | Equivalent participants run with different effective settings.                                     |
| Feature-selection skew         | A feature is active in only part of the Whole Machine.                                             |
| Policy World skew              | Participating Cores operate under incompatible Contract Worlds.                                    |
| Governance-selection skew      | Different parts of one intended Whole Machine believe different governing arrangements are active. |
| Stale Contract World cache     | A participant continues using a previously valid world after governance has changed it.            |
| Partial policy rollout         | Rules that should change together become active at different times.                                |
| Upgrade before state migration | New code interprets state that still has old meaning.                                              |
| Mixed replica versions         | Replicas of what should be one machine do not execute equivalent semantics.                        |
| Downgrade incompatibility      | A failed upgrade cannot safely return to the previous version.                                     |

#### Availability and Degraded Operation

| Situation                 | Threat to preserve Contract meaning against                                                       |
|---------------------------|---------------------------------------------------------------------------------------------------|
| Partial availability      | Some Whole Machine functions remain possible while other required Cores are unavailable.          |
| Read-only degradation     | Observation remains possible while mutation can no longer be safely accepted.                     |
| Reduced-function mode     | The machine continues with a deliberately smaller set of guarantees or operations.                |
| Fail-open behavior        | The system continues despite being unable to establish a normally required condition.             |
| Fail-closed behavior      | The system rejects work whenever a required condition cannot be established.                      |
| Fallback semantic change  | A fallback path returns a different quality or meaning from the normal relation.                  |
| Stale fallback            | Cached or replicated material is used because the authoritative participant is unavailable.       |
| Alternate-region behavior | Failover reaches a location with different latency, capacity, version, or available dependencies. |
| Load shedding             | Some admitted or incoming work is deliberately rejected to preserve the rest of the machine.      |
| Brownout                  | Optional work is disabled so that required work can continue.                                     |
| Partial result            | A caller receives only the part of the larger result that could be established.                   |
| Best-effort completion    | The Whole Machine continues even though some participating obligations remain unresolved.         |

#### Streaming, Event, and Queue-Based Realizations

| Situation                                   | Threat to preserve Contract meaning against                                                                    |
|---------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| Out-of-order event                          | Event material arrives in a different order from its event-time order.                                         |
| Late event                                  | An event arrives after a window, decision, or state progression has already closed around its absence.         |
| Duplicate event                             | A broker, producer, consumer, or recovery path causes one event to be processed repeatedly.                    |
| Missing event                               | A consumer never observes material that another part of the system considers published.                        |
| Incorrect progress marker                   | A watermark, offset, or equivalent progress claim advances beyond material that may still arrive.              |
| Event-time and processing-time disagreement | Results differ depending on whether the machine reasons about when an event occurred or when it was processed. |
| External sink outside checkpoint boundary   | Internal exactly-once recovery does not prevent duplicate effects in an external system.                       |
| Consumer rebalance                          | Ownership of event partitions moves while records are being processed.                                         |
| Offset committed before effect              | Recovery skips work whose external effect never completed.                                                     |
| Effect completed before offset commit       | Recovery repeats work whose effect already occurred.                                                           |
| Partition-local ordering only               | Each partition has order while no equivalent global order exists across partitions.                            |
| Repartitioning                              | A change in partition ownership or keying changes where order and local state are maintained.                  |
| Backlog growth                              | Event processing falls behind real-world event production.                                                     |
| Poison material                             | One item repeatedly fails and blocks or destabilizes normal progress.                                          |
| Dead-letter divergence                      | Failed material leaves the normal flow and no longer participates in the same completion semantics.            |

#### Security, Trust, and Arbitrary Faults

| Situation                          | Threat to preserve Contract meaning against                                                   |
|------------------------------------|-----------------------------------------------------------------------------------------------|
| Sender spoofing                    | Material appears to come from an authoritative participant when it does not.                  |
| Authentication disagreement        | Different Cores disagree about the identity behind one interaction.                           |
| Authorization skew                 | One participant accepts an action that another active policy would reject.                    |
| Credential expiry under clock skew | Participants disagree about whether a time-limited credential remains valid.                  |
| Replay attack                      | Previously valid material is presented again outside its intended interaction.                |
| Message tampering                  | Material is modified between authoritative production and consumption.                        |
| Compromised participant            | A participant deliberately or arbitrarily violates its expected behavior.                     |
| Equivocation                       | One participant sends incompatible claims to different peers.                                 |
| Arbitrary corrupted output         | A participant continues responding but produces invalid material rather than simply crashing. |
| Certificate rotation skew          | Some participants trust a new identity while others still require the old one.                |
| Secret rotation skew               | Authentication material changes at different times across the Whole Machine.                  |
| Revoked credential acceptance      | A participant keeps accepting authority after revocation should have taken effect.            |
| Cross-tenant identity mix-up       | Material from one tenant or authority domain is attributed to another.                        |
| Protocol downgrade                 | Participants are induced to use weaker semantics than the intended Whole Machine relation.    |

#### Diagnostics, Attribution, and Audit

| Situation                                     | Threat to preserve Contract meaning against                                                                       |
|-----------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| Client reports failure while server succeeded | The two sides record different terminal outcomes for one interaction.                                             |
| Logs are temporally reordered                 | Unsynchronized clocks make diagnostic records appear in a misleading sequence.                                    |
| Missing telemetry                             | The observation system loses evidence from one participant or network segment.                                    |
| Duplicate telemetry                           | Retry or replay produces repeated diagnostic evidence for one underlying event.                                   |
| Broken trace continuity                       | Retry, failover, or asynchronous work loses the relation between parts of one interaction.                        |
| Inconsistent interaction identifiers          | Different participants attribute the same activity to different identities.                                       |
| Failure attribution ambiguity                 | Several plausible failure points exist and no participant has a complete causal view.                             |
| Stale health signal                           | Monitoring reports a condition that is no longer true.                                                            |
| Conflicting health signals                    | Different observers report incompatible views of one participant.                                                 |
| Monitoring-plane partition                    | The machine may continue while its diagnostic system cannot observe it.                                           |
| Partial audit trail                           | Some authority-bearing actions are durable while their required evidence is missing.                              |
| Duplicate audit record                        | Replay records one authoritative action more than once.                                                           |
| Unknown terminal outcome                      | Recovery and evidence cannot establish whether the logical interaction succeeded, failed, or remained incomplete. |

#### External Dependencies and Irreversible Effects

| Situation                           | Threat to preserve Contract meaning against                                                        |
|-------------------------------------|----------------------------------------------------------------------------------------------------|
| Third-party timeout                 | An external dependency may complete after the Whole Machine has stopped waiting.                   |
| Third-party duplicate processing    | Retry causes an external dependency to apply the same request more than once.                      |
| External system without idempotency | Kontrakt-controlled retry cannot prevent repeated external effects.                                |
| External rate limiting              | A dependency remains healthy but rejects work because its own capacity boundary is reached.        |
| External version change             | A dependency changes behavior or schema outside the Whole Machine deployment cycle.                |
| External stale data                 | A remote source returns material that is valid but no longer current.                              |
| Webhook replay                      | An external caller resends an earlier notification after the local machine has advanced.           |
| Delayed callback                    | A valid response arrives after the state in which it was requested has ended.                      |
| Human approval delay                | A person completes a required decision after the machine context has changed.                      |
| Human duplicate action              | The same approval or command is submitted more than once.                                          |
| Irreversible external effect        | A payment, physical actuation, notification, or legal action cannot be exactly rolled back.        |
| External acknowledgement loss       | The effect occurs but the Whole Machine receives no reliable confirmation.                         |
| Unknown dependency failure model    | The Whole Machine relies on another distributed system whose guarantees are weaker or unspecified. |

#### Determinism and Realization Leakage

| Situation                              | Threat to preserve Contract meaning against                                                                  |
|----------------------------------------|--------------------------------------------------------------------------------------------------------------|
| Scheduler-dependent result             | Different thread or process scheduling changes a contract-visible result.                                    |
| Network-race winner                    | The first arriving response determines meaning even though arrival order is only physical.                   |
| Replica-choice result                  | Reading from a different valid replica changes the contract-visible answer.                                  |
| Leader-location result                 | Leadership placement changes the meaning of an otherwise identical interaction.                              |
| Retry-count result                     | A different number of physical attempts changes contractual output or State.                                 |
| Recovery-path result                   | Normal execution and recovery produce different contract-visible meaning from the same established material. |
| Topology-dependent result              | Placement in one host, zone, or region changes Contract meaning without an explicit obligation requiring it. |
| Nondeterministic conflict winner       | Several valid contenders exist and the realization chooses one without Contract law.                         |
| Unspecified tie breaking               | Equal candidates produce different results across conforming realizations.                                   |
| Message-order-dependent meaning        | Two physically valid delivery orders produce different contractual outcomes without declared ordering law.   |
| Partial-failure-dependent meaning      | The realization invents a new semantic result only because a particular node or network path failed.         |
| Local randomness or wall-clock leakage | A backend-local source changes meaning that the Contract did not authorize it to choose.                     |

#### Fundamental Guarantee Boundaries

| Situation                                                                  | Threat to preserve Contract meaning against                                                                                          |
|----------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| Failure cannot be distinguished from delay                                 | A fully asynchronous environment does not provide a perfect general test for whether a silent participant is dead or merely slow.    |
| Consensus cannot always terminate under full asynchrony with crash failure | Agreement may preserve safety while no algorithm can guarantee progress under the strongest asynchronous assumptions.                |
| Partition forces a consistency or availability choice                      | A Whole Machine cannot assume both unrestricted availability and one strongly consistent shared view during arbitrary partition.     |
| Global state is not obtained by ordinary local reads                       | A coherent distributed snapshot requires additional semantics or realization support.                                                |
| Hard deadline over unbounded delay                                         | No backend can guarantee remote completion within a fixed time if communication delay has no bound.                                  |
| Progress without required participants                                     | A contract cannot require completion if the participants whose agreement or effects are necessary are permanently unavailable.       |
| Exactly-once end-to-end over arbitrary effects                             | Local deduplication or transactional processing does not automatically make every external effect occur exactly once.                |
| Atomicity across non-transactional participants                            | A strong all-or-nothing guarantee may be unrealizable when a required participant cannot take part in an equivalent commit boundary. |
| Perfect global clock assumption                                            | Distributed participants cannot treat unsynchronized local clocks as one exact source of global event order.                         |
| Byzantine tolerance assumption                                             | Arbitrary or malicious faults require a stronger failure model than ordinary crash and omission failures.                            |

The inventory above is not the Whole Machine Contract model. It is a retained implementation review surface for later
distributed backend work. A backend may use consensus, transactions, queues, retries, locks, leases, replication,
routing, or another mechanism, but ADR-0055 does not select or prescribe those mechanisms.

The implementation document that realizes a distributed Whole Machine must review the threats that apply to its selected
architecture and record the chosen response there. Those responses remain realization and must not become Contract
Authority merely because they are necessary to preserve the Contract.


---

## 4. Verification

Verification must reject Whole Machine definitions that depend on undeclared realization structure.

A Contract Pipeline must remain one-way and acyclic, must not observe another pipeline's internals, and must admit
external material only through its Input Airlock. One admitted Input establishes one flow, and later material cannot
modify that flow.

Input meaning must come only from the applicable Contract. Producer identity, producer count, arrival order, waiting,
and upstream topology have no authority unless explicitly declared as Contract material.

The threat inventory in Section 3.6 adds no Contract semantics by itself. Concrete distributed-backend checks belong to
later implementation design.

The exact verification rules for the remaining Whole Machine relations stay open until those relations are decided.

## 5. Deferred Decisions

The following remain open for the continuation of ADR-0055:

- the identity and exact boundary of a Whole Machine;
- the exact Contract forms by which participating Core surfaces relate;
- whether existing Interface material is sufficient for every Whole Machine relation;
- cross-Core Contract material and authority boundaries;
- coexistence and ordering where they are observable;
- Contract World continuity across collaborating Cores;
- the meaning of physical separation when it is itself an obligation;
- whether retry, isolation, circuit breaking, failover, or related resilience controls expose any Contract-visible
  Governance trigger or Contract World selection;
- the distributed backend implementation design that will consume the threat inventory in Section 3.6;
- and determinism for valid Whole Machine compositions.

These questions are decided in Contract terms first. Realization follows afterward.

---

## 6. Consequences

### Positive

Whole Machine composition stays small. Independent Contract Pipelines remain independent flows, each boundary is judged
only by its applicable Contracts, and implementation topology cannot acquire Contract Authority. Waiting,
synchronization, transport, buffering, and producer coordination remain backend concerns.

Distributed realization threats remain visible without placing their responses in the ADR.

### Negative

The remaining Whole Machine relation and authority questions still need to be decided, and distributed realizations
still require separate backend design against the threat inventory.

### Neutral

The realization axis remains intentionally open. Section 3.6 records threats for later implementation work without
selecting responses.