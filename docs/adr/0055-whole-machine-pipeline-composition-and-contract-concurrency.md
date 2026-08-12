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
process and one failure boundary. ADR-0055 does not yet decide which of these situations belong to Contract meaning. It
records them first so that each can be examined without silently assuming that distribution is harmless.

This inventory is intentionally broad. An item in the table is not a new Contract Authority and is not a required
Kontrakt feature. Later work must decide whether each situation changes Whole Machine meaning, belongs only to
realization, requires backend validation, or remains outside the supported failure model.

#### Communication and Reachability

| Situation                              | Whole Machine risk to examine                                                                         |
|----------------------------------------|-------------------------------------------------------------------------------------------------------|
| One Core becomes unreachable           | Another Core cannot know whether the machine is unavailable, slow, or no longer participating.        |
| Full network partition                 | Participating Cores continue operating without being able to exchange required information.           |
| Partial partition                      | Some Cores can communicate while other required paths are unavailable.                                |
| Asymmetric partition                   | A message can travel in one direction while the return path is unavailable.                           |
| Intermittent connectivity              | A relation repeatedly disappears and returns while work remains in progress.                          |
| Message loss                           | Required material never reaches the receiving side.                                                   |
| Unbounded or extreme delay             | Material arrives after the condition that made it useful has changed.                                 |
| Large latency variation                | A relation that usually completes quickly can exceed a boundary without any participant failing.      |
| Duplicate delivery                     | The same transmitted material reaches the receiver more than once.                                    |
| Message reordering                     | Material arrives in a different order from the order in which it was sent.                            |
| Corrupted or truncated transfer        | The receiver obtains material that is incomplete or no longer represents what the sender produced.    |
| Connection reset during an exchange    | The transport ends after only part of an interaction has completed.                                   |
| Half-open connection                   | One participant believes a connection still exists after the other side has disappeared.              |
| Network black hole                     | Traffic is accepted by the network path but never reaches a useful destination or returns an error.   |
| Congestion or bandwidth collapse       | Communication remains technically available but can no longer carry required work at the needed rate. |
| Stale routing                          | A request follows an old route after ownership or placement has changed.                              |
| Stale service discovery                | A caller continues using an endpoint that is no longer the correct participant.                       |
| Misrouting                             | Material reaches a valid machine instance that is not the intended contractual participant.           |
| Reconnection to a replacement instance | A caller resumes communication with a new realization that may not share the prior volatile context.  |

#### Participant Failure and Lifecycle

| Situation                        | Whole Machine risk to examine                                                                                |
|----------------------------------|--------------------------------------------------------------------------------------------------------------|
| Crash-stop failure               | One participant stops permanently while the rest of the Whole Machine continues.                             |
| Crash-recovery failure           | A participant disappears and later returns with durable state, partial state, or no volatile context.        |
| Long process pause               | A participant is alive but makes no progress long enough to look failed to others.                           |
| Runtime stall                    | Deadlock, livelock, runtime pauses, or local resource starvation stop useful progress without a clean crash. |
| Restart with lost volatile state | The process returns without remembering in-flight work that other Cores still consider active.               |
| Local durable-storage failure    | A Core can execute but cannot safely read or persist state needed for recovery.                              |
| Durable-storage latency          | Persistence succeeds too slowly for another participant to distinguish progress from failure.                |
| Persisted-state corruption       | Recovery material exists but no longer represents a valid prior machine condition.                           |
| In-flight state loss             | Work that existed only in memory disappears during restart.                                                  |
| Duplicate live instance          | Two realizations concurrently believe they represent the same participant.                                   |
| Zombie participant               | An old instance resumes after another instance has already taken over its role.                              |
| Incomplete graceful shutdown     | New work stops, but existing work does not reach a known terminal condition before the participant exits.    |
| Startup dependency failure       | A Core starts while another machine or required dependency is not yet usable.                                |
| Overlapping maintenance          | Several participants are intentionally restarted or replaced at the same time.                               |
| Instance replacement             | A new process, host, or replica assumes a role that previously belonged to another realization.              |
| Recovery replay                  | A participant re-applies durable work that may already have affected another Core before the failure.        |
| Orphaned held state              | A participant disappears while another Core still treats its reservation, ownership, or session as active.   |

#### Time and Temporal Observation

| Situation                               | Whole Machine risk to examine                                                                             |
|-----------------------------------------|-----------------------------------------------------------------------------------------------------------|
| Clock skew                              | Different participants assign different wall-clock times to the same period of real execution.            |
| Clock drift                             | The difference between participant clocks grows while the Whole Machine runs.                             |
| Clock jump                              | A local clock moves forward or backward because its time source is corrected.                             |
| Time-source failure                     | A participant loses the source on which a time-sensitive guarantee depends.                               |
| Timeout ambiguity                       | A caller stops waiting even though the remote work may still be running or may already have completed.    |
| Deadline propagation error              | Different participants interpret the remaining time of one logical interaction differently.               |
| Lease-expiry ambiguity                  | An old holder may still act while another participant believes the lease has expired.                     |
| Pause across a deadline                 | A suspended process resumes after time-based authority or applicability has already changed.              |
| Event time differs from processing time | Material represents an earlier real event but is processed after newer events.                            |
| Late material                           | Valid material arrives after the Whole Machine has already advanced based on its absence.                 |
| No natural global instant               | Facts observed from several Cores do not automatically describe one simultaneous Whole Machine condition. |
| Inconsistent snapshot time              | Different parts of one decision are observed at different logical moments.                                |
| Timestamp reuse or collision            | Two distinct events cannot be safely distinguished by time alone.                                         |
| Lost scheduled action                   | A time-triggered action disappears during failure, restart, or ownership movement.                        |
| Duplicate scheduled action              | Recovery or ownership movement causes the same logical timer action to fire more than once.               |
| Stale scheduled action                  | A timer fires after the condition that originally authorized it has already changed.                      |

#### Interaction Identity, Retry, and Delivery Semantics

| Situation                             | Whole Machine risk to examine                                                                                            |
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

| Situation                             | Whole Machine risk to examine                                                                    |
|---------------------------------------|--------------------------------------------------------------------------------------------------|
| Send order differs from receive order | A receiver observes interactions in another order than the sender issued them.                   |
| No global total order                 | Different participants can validly observe concurrent events in different orders.                |
| Cross-channel reordering              | Two communication paths preserve local order but not their order relative to each other.         |
| Causal dependency arrives late        | An effect is observed before the information on which it contractually depends.                  |
| Retry changes order                   | A later attempt overtakes earlier work after a communication failure.                            |
| Failover changes order                | A replacement participant observes or applies pending work in a different order.                 |
| Concurrent valid interleavings        | Several orders are physically possible even though only some may preserve Whole Machine meaning. |
| Equal or incomparable timestamps      | Time metadata cannot establish the required order between two events.                            |
| Fan-out completion skew               | Several branches of one larger activity finish at different times.                               |
| Missing fan-in participant            | A relation waits for one branch that has failed or will never produce the expected material.     |
| Late event after transition           | Material valid for an earlier state arrives after the machine has entered a later state.         |
| Repeated collaboration cycle          | A relation can return to an earlier Core and create repeated or cyclic dependencies.             |

#### Observation, Replication, and Consistency

| Situation                     | Whole Machine risk to examine                                                                              |
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

| Situation                                | Whole Machine risk to examine                                                                            |
|------------------------------------------|----------------------------------------------------------------------------------------------------------|
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
| Optimistic conflict retry                | A conflict forces re-execution after the world has changed.                                              |
| Deadlock                                 | Participants wait on each other and no required progress occurs.                                         |
| Livelock                                 | Participants continue reacting to each other without reaching useful progress.                           |
| Starvation                               | One valid interaction is repeatedly prevented from progressing by other work.                            |
| Stale lock holder                        | A participant continues acting after another participant considers its ownership expired.                |
| Ownership-transfer race                  | Old and new owners overlap or leave a gap while authority moves.                                         |

#### Atomicity, Commit, and Recovery

| Situation                              | Whole Machine risk to examine                                                                                       |
|----------------------------------------|---------------------------------------------------------------------------------------------------------------------|
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
| Replay after uncertain commit          | Recovery repeats work because the previous final outcome is unknown.                                                |

#### Replication, Leadership, and Membership

| Situation                     | Whole Machine risk to examine                                                                                  |
|-------------------------------|----------------------------------------------------------------------------------------------------------------|
| Split brain                   | Two groups independently behave as if each were the authoritative continuation of one participant.             |
| Multiple active leaders       | More than one realization accepts work under the same leadership role.                                         |
| No leader                     | A replicated participant remains available at the process level but cannot accept authority-bearing work.      |
| Stale leader                  | A former leader continues acting after leadership has moved elsewhere.                                         |
| Leader-election delay         | No participant can make progress while leadership is being re-established.                                     |
| Minority-side activity        | A participant separated from quorum continues serving work that the majority will not accept.                  |
| Quorum loss                   | Too few replicas remain mutually reachable to preserve the selected consistency or progress guarantee.         |
| Replica divergence            | Replicated histories cease to describe one state machine.                                                      |
| Follower lag                  | A replica remains alive but is too far behind to safely answer some observations.                              |
| Failover data gap             | Work visible before failure is absent from the replacement leader.                                             |
| Membership change             | The set of replicas or voters changes while the machine is running.                                            |
| Inconsistent membership view  | Participants disagree about who currently belongs to the replication group.                                    |
| Reconfiguration overlap       | Old and new membership configurations are active during the same period.                                       |
| Replica join                  | A new replica becomes visible before it has caught up enough to preserve required meaning.                     |
| Replica removal               | A removed replica continues serving or accepting work.                                                         |
| Snapshot restore              | A replica resumes from a snapshot that is older than other surviving state.                                    |
| Log replay duplication        | Recovery applies a committed command more than once.                                                           |
| Log truncation after failover | Uncommitted local history disappears when a new authoritative history is chosen.                               |
| Leader lease uncertainty      | A leader relies on time-based authority while another participant may already consider that authority expired. |
| Consensus liveness limit      | Required agreement may preserve safety while progress becomes impossible under the current failures.           |

#### Partitioning, Sharding, and Ownership

| Situation                    | Whole Machine risk to examine                                                                |
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

| Situation                                     | Whole Machine risk to examine                                                                              |
|-----------------------------------------------|------------------------------------------------------------------------------------------------------------|
| Stale membership                              | A participant acts on an old view of which machines currently exist.                                       |
| Inconsistent membership                       | Different Cores have different current membership views.                                                   |
| Rapid membership churn                        | Participants repeatedly enter and leave before the system stabilizes.                                      |
| Address reuse                                 | A network address now identifies a different realization from the one cached by a caller.                  |
| DNS staleness                                 | Name resolution continues returning an old endpoint after placement changes.                               |
| Load balancer sends work to draining instance | New work reaches a participant that is intentionally leaving service.                                      |
| Health-check false positive                   | A participant is declared healthy even though it cannot preserve the required machine meaning.             |
| Health-check false negative                   | A participant that could safely serve work is removed from participation.                                  |
| Readiness mismatch                            | Operational readiness differs from the contractual condition required by another Core.                     |
| Control-plane failure                         | Placement or membership control is unavailable while existing data-plane work may still continue.          |
| Data-plane failure                            | Control infrastructure remains healthy while required machine interactions cannot proceed.                 |
| Zone failure                                  | Many supposedly independent realizations disappear under one infrastructure failure.                       |
| Region failure                                | A broad geographic failure removes several participants and communication paths together.                  |
| Correlated failure                            | A common cause defeats several redundant participants at once.                                             |
| Shared dependency failure                     | Independent Cores fail together because they rely on the same hidden service or resource.                  |
| Failover-target overload                      | Surviving locations cannot absorb the work redirected from a failed location.                              |
| Dependency cycle                              | Several machines depend on each other's availability or completion and cannot independently make progress. |

#### Load, Capacity, and Failure Amplification

| Situation                       | Whole Machine risk to examine                                                                               |
|---------------------------------|-------------------------------------------------------------------------------------------------------------|
| Participant overload            | A Core remains reachable but cannot process work within useful bounds.                                      |
| Queue buildup                   | Work accumulates faster than a participant can complete it.                                                 |
| Missing backpressure            | A faster producer overwhelms a slower consumer.                                                             |
| Backpressure propagation        | Pressure in one Core travels upstream and changes the behavior of otherwise healthy Cores.                  |
| Head-of-line blocking           | Slow work prevents unrelated work behind it from progressing.                                               |
| Retry storm                     | Failure causes enough retries to increase the original overload.                                            |
| Multi-layer retry amplification | Several layers independently retry one logical interaction and multiply the load.                           |
| Synchronized retry              | Many callers retry at the same moment and create a new traffic spike.                                       |
| Thundering herd                 | Many participants wake or reconnect together after one shared event.                                        |
| Connection storm                | Recovery creates a sudden surge of new connections before useful work begins.                               |
| Resource exhaustion             | CPU, memory, queues, sockets, file descriptors, or other finite resources are consumed by distributed work. |
| Load imbalance                  | Some realizations are overloaded while equivalent capacity remains idle elsewhere.                          |
| Slow consumer                   | One receiver cannot drain material at the rate it is produced.                                              |
| Unbounded buffering             | The system preserves incoming work by consuming memory or storage without a stable limit.                   |
| Bounded-buffer loss             | Work is dropped or rejected after a queue reaches its configured limit.                                     |
| Noisy neighbor                  | Unrelated work on shared infrastructure removes capacity needed by a participating Core.                    |
| Autoscaling lag                 | New capacity arrives after overload has already changed Whole Machine behavior.                             |
| Scale-out warm-up               | Newly added instances exist but are not yet capable of carrying normal work.                                |
| Scale-in interruption           | Capacity removal terminates or relocates work that was still in progress.                                   |
| Cascading failure               | Failure or overload in one participant increases pressure until other participants fail.                    |
| Metastable overload             | The Whole Machine remains overloaded even after the original traffic spike has ended.                       |
| Capacity mismatch across Cores  | One stage can admit work faster than the next required Core can safely accept it.                           |
| Fan-out amplification           | One admitted interaction creates enough downstream work to overload otherwise healthy participants.         |
| Straggler amplification         | Completion of a larger relation is dominated by its slowest required participant.                           |

#### Version, Schema, Configuration, and Contract World Skew

| Situation                      | Whole Machine risk to examine                                                                      |
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
| Rollback after migration       | An older realization resumes after state or protocol meaning has moved forward.                    |
| Mixed replica versions         | Replicas of what should be one machine do not execute equivalent semantics.                        |
| Downgrade incompatibility      | A failed upgrade cannot safely return to the previous version.                                     |

#### Availability and Degraded Operation

| Situation                   | Whole Machine risk to examine                                                                     |
|-----------------------------|---------------------------------------------------------------------------------------------------|
| Partial availability        | Some Whole Machine functions remain possible while other required Cores are unavailable.          |
| Read-only degradation       | Observation remains possible while mutation can no longer be safely accepted.                     |
| Reduced-function mode       | The machine continues with a deliberately smaller set of guarantees or operations.                |
| Fail-open behavior          | The system continues despite being unable to establish a normally required condition.             |
| Fail-closed behavior        | The system rejects work whenever a required condition cannot be established.                      |
| Fallback semantic change    | A fallback path returns a different quality or meaning from the normal relation.                  |
| Stale fallback              | Cached or replicated material is used because the authoritative participant is unavailable.       |
| Alternate-region behavior   | Failover reaches a location with different latency, capacity, version, or available dependencies. |
| Load shedding               | Some admitted or incoming work is deliberately rejected to preserve the rest of the machine.      |
| Brownout                    | Optional work is disabled so that required work can continue.                                     |
| Partial result              | A caller receives only the part of the larger result that could be established.                   |
| Best-effort completion      | The Whole Machine continues even though some participating obligations remain unresolved.         |
| Recovery from degraded mode | Normal operation resumes after different participants may have advanced under weaker conditions.  |

#### Streaming, Event, and Queue-Based Realizations

| Situation                                   | Whole Machine risk to examine                                                                                  |
|---------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| Out-of-order event                          | Event material arrives in a different order from its event-time order.                                         |
| Late event                                  | An event arrives after a window, decision, or state progression has already closed around its absence.         |
| Duplicate event                             | A broker, producer, consumer, or recovery path causes one event to be processed repeatedly.                    |
| Missing event                               | A consumer never observes material that another part of the system considers published.                        |
| Incorrect progress marker                   | A watermark, offset, or equivalent progress claim advances beyond material that may still arrive.              |
| Event-time and processing-time disagreement | Results differ depending on whether the machine reasons about when an event occurred or when it was processed. |
| Checkpoint failure                          | Recovery state cannot be established at the intended progress point.                                           |
| Checkpoint timeout                          | A system abandons a global progress attempt while work continues around it.                                    |
| Restore replay                              | Recovery intentionally reprocesses in-flight material from before the failure.                                 |
| External sink outside checkpoint boundary   | Internal exactly-once recovery does not prevent duplicate effects in an external system.                       |
| Consumer rebalance                          | Ownership of event partitions moves while records are being processed.                                         |
| Offset committed before effect              | Recovery skips work whose external effect never completed.                                                     |
| Effect completed before offset commit       | Recovery repeats work whose effect already occurred.                                                           |
| Partition-local ordering only               | Each partition has order while no equivalent global order exists across partitions.                            |
| Repartitioning                              | A change in partition ownership or keying changes where order and local state are maintained.                  |
| Backlog growth                              | Event processing falls behind real-world event production.                                                     |
| Poison material                             | One item repeatedly fails and blocks or destabilizes normal progress.                                          |
| Dead-letter divergence                      | Failed material leaves the normal flow and no longer participates in the same completion semantics.            |
| Join waits on absent stream                 | A multi-input relation cannot finish because one input never arrives.                                          |
| Idle input stalls progress                  | One silent partition or stream prevents a global progress condition from advancing.                            |

#### Security, Trust, and Arbitrary Faults

| Situation                          | Whole Machine risk to examine                                                                 |
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

| Situation                                     | Whole Machine risk to examine                                                                                     |
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

| Situation                           | Whole Machine risk to examine                                                                      |
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
| Nested retry policies               | The Whole Machine and an external dependency both retry, multiplying attempts and ambiguity.       |

#### Determinism and Realization Leakage

| Situation                              | Whole Machine risk to examine                                                                                |
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

| Situation                                                                  | Whole Machine risk to examine                                                                                                        |
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

The inventory above is a review surface, not the Whole Machine contract model. Each row must be removed, narrowed, or
converted into an explicit Contract question before ADR-0055 can close. Implementation mechanisms such as consensus
algorithms, transaction protocols, retries, queues, locks, leases, replication strategies, and network topology do not
become Contract Authorities merely because they may be used to address one of these situations.

---

## 4. Verification

Verification must reject any Whole Machine definition whose meaning depends on undeclared realization structure.

The Core guidance in Section 3.2 does not authorize Kontrakt to guess or enforce one correct decomposition. It guides
the engineering judgment used to keep one Core understandable as one machine.

Verification must also preserve the distinction between Core participation and relation scope. Participation in one
Whole Machine must not make all Contract material of one Core implicitly available to another Core or to every Whole
Machine relation.

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
- cycles and repeated collaboration;
- Contract World continuity across collaborating Cores;
- the meaning of physical separation when it is itself an obligation;
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