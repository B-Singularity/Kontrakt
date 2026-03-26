# Design Note: L2 Join Lifecycle State Machine Mechanics for Planning Tier-2 Cache

- Status: Accepted
- Date: 2026-03-26
- Owner: Kontrakt Planning / Runtime / Compiler Core
- Companion ADR: ADR-0034
- Scope: Planning-only Tier-2 cache / in-flight join lifecycle mechanics
- Non-Goal: Framework-wide generic state-machine platform

---

## 1. Purpose

This document defines the **mechanical implementation model** for the L2 join lifecycle introduced by ADR-0034.

ADR-0034 defines the lifecycle law:

- top-level states,
- legal / illegal transitions,
- terminalization authority,
- race arbitration,
- publication-before-completion,
- panic/isolation semantics,
- builder supervision,
- commit-right arbitration,
- grace-aware reclamation,
- and evolution rules.

This design note defines the **runtime mechanics** required to realize that law on the JVM without semantic drift:

- primitive state layout,
- atomic transition surfaces,
- slot / waiter / lease / builder / commit-right memory ownership,
- release/acquire visibility points,
- attach-vs-close atomic coordination,
- stale-callback neutralization,
- completion-dispatch integration,
- delivery reliability handling,
- contention escalation handling,
- false-sharing mitigation,
- and verification implications for the implementation.

This document is normative for implementation mechanics under ADR-0034.  
If any detail here conflicts with ADR-0034, the ADR wins.

---

## 2. Authority Boundary

### 2.1 ADR vs Design Note

ADR-0034 is the source of truth for:

- lifecycle legality,
- transition meaning,
- single terminalization authority,
- builder supervision law,
- commit-right arbitration law,
- grace-aware reclamation law,
- and when a change requires ADR amendment.

This design note is the source of truth for:

- state encoding,
- field ownership,
- attach/close coordination mechanics,
- completion / timeout / drop / delivery interaction at the implementation level,
- builder supervision mechanics,
- commit-right arbitration mechanics,
- reclamation barrier mechanics,
- and the concrete memory-visibility model expected from the JVM implementation.

### 2.2 Planning-only scope

This document applies only to the **planning bounded context**, specifically the L2 cache / in-flight join path.

It is intentionally **not** a framework-wide lifecycle-mechanics template.

Future bounded contexts MAY reuse its principles, but MUST define their own lifecycle ADR and their own companion design
notes when:

- top-level states differ,
- terminalization authority differs,
- or publication semantics differ.

---

## 3. Design Goals

The implementation must satisfy all of the following simultaneously.

### 3.1 Semantic determinism

Governance changes may affect sharing, latency, attach admission, timeout, restart, throughput, or duplicate-build
suppression.

They MUST NOT affect:

- final IR topology,
- canonical signatures,
- exact-match correctness,
- truncation choice,
- or `treeSemanticCostUpperBound`.

### 3.2 Explicit multi-axis lifecycle

The implementation must make the following axes first-class and explicit:

- shared-slot lifecycle,
- waiter lifecycle,
- builder-handle lifecycle,
- commit-right lifecycle,
- partition-region lifecycle,
- and speculative lease lifecycle where applicable.

No implementation is compliant if one axis is derived implicitly from another.

### 3.3 Allocation-free hot-path transitions

Hot-path state transitions must not allocate transient state objects.

State transitions must complete through primitive-field CAS and ordinary non-allocating field stores.

### 3.4 Publication-before-completion

A waiter that resumes from shared success must be able to observe the authoritative committed winner immediately through
the bucket path.

Success is therefore both:

- a semantic terminal state,
- and a memory-visibility boundary.

### 3.5 Zombie-transition resistance

Late timeout callbacks, stale completion callbacks, stale drop sweep events, duplicate builder completions, or recycled
stale waiter references must not be able to resurrect or overwrite terminal state.

### 3.6 Mechanical sympathy

The implementation must respect JVM/JIT reality:

- avoid boxed-key routing on hot paths,
- minimize state-object churn,
- minimize false sharing,
- avoid global lock convoying,
- and keep contention local to the routed shard / slot.

### 3.7 Convergent liveness

No issued lifecycle authority may remain indefinitely outstanding without a lawful convergence path.

In particular:

- builder handles must converge,
- commit-right must converge,
- visible attached waiters must converge,
- and region reclamation must converge only after lawful terminal work becomes unreachable as pending work.

---

## 4. Non-Goals

This design note does not attempt to define:

- framework-wide lifecycle abstractions,
- a generic reusable state-machine library,
- cost-center identity bands,
- or policy-resolution algorithms.

It also does not require one exact API such as:

- `VarHandle` only,
- `AtomicLongFieldUpdater` only,
- or monitor-only synchronization.

However, it does require that the chosen implementation satisfy the same visibility, atomicity, sealing, and convergence
law.

This note also does not attempt to preserve historical implementation convenience if that convenience conflicts with
lifecycle law.

---

## 5. Runtime Objects and Ownership

The runtime is split into the following objects.

### 5.1 PartitionRegion

`PartitionRegion` owns:

- region lifecycle state,
- shard array,
- governance snapshot visibility for the region,
- close publication,
- panic publication,
- drop sweep orchestration,
- grace-barrier coordination,
- and final reclamation escalation.

`PartitionRegion` is the owner of region lifecycle, not of individual waiter lifecycle.

### 5.2 L2Shard

`L2Shard` owns:

- route selection,
- pre-screen lookup,
- in-flight slot lookup / installation,
- bucket publication,
- authoritative re-verification,
- duplicate-build admission decisions,
- and delegation to lifecycle authorities.

`L2Shard` is orchestration-only.  
It is **not** the semantic owner of lifecycle truth.

### 5.3 InFlightSlot

`InFlightSlot` is the lifecycle host for one routed shared key.

It owns:

- shared-slot terminal state,
- attached waiter count,
- waiter registry root,
- shared completion payload / failure payload visibility,
- speculative lease registry for that slot,
- builder handle registry or builder supervisory linkage,
- commit-right arbitration state,
- delivery-pending visibility for terminal outcomes,
- and slot terminalization coordination.

`InFlightSlot` must no longer be treated as a thin `future + counter` helper.

### 5.4 WaiterCell

Each successful attach creates or reuses one `WaiterCell`.

A waiter cell owns:

- waiter terminal state,
- waiter-local cancellation/timeout race coordination,
- result delivery bookkeeping,
- and callback delivery linkage.

A waiter cell is **not** a semantic owner of shared-slot state.

The waiter registry must be **bounded** by slot-level governance.  
A compliant implementation must enforce a maximum admitted waiter count per slot, and attach rejection on waiter-cap
exhaustion must occur before a waiter becomes semantically attached.

### 5.5 BuilderHandle

A builder handle owns:

- exactly-once commit/abort authority for the builder winner path,
- supervisory convergence bookkeeping,
- and builder identity needed for orphan detection where applicable.

It is logically separate from:

- shared-slot terminal state,
- waiter state,
- and commit-right state.

### 5.6 CommitRight

Commit-right is an orthogonal publication authority surface.

It owns:

- exactly-one winner election for authoritative publication,
- publication admission exclusion between speculative builders,
- and release after publication convergence.

It is not equivalent to builder progress and must not be inferred from builder existence alone.

### 5.7 SpeculativeLease

A speculative lease is **slot-owned**.

It is not:

- session-owned,
- waiter-owned,
- handle-owned,
- or commit-right-owned.

It exists only if speculative promotion is enabled and granted.

### 5.8 DeliveryPlane

Delivery is adapter-owned, not lifecycle-authoritative.

The delivery plane owns:

- completion queue or mailbox,
- overflow/pending-delivery registry,
- retry sweeper,
- bounded drain coordination,
- and shutdown/drain sequencing.

Delivery-plane failure must not rewrite lifecycle truth.

---

## 6. Mechanical State Encoding

### 6.1 Guiding rule

Only the fields that must change atomically together should be packed into the same atomic word.

This note therefore distinguishes:

- **shared-slot state + admitted waiter count** as one atomic concern,
- **per-waiter terminal state** as a separate atomic concern,
- **builder-handle state** as a separate concern,
- **commit-right state** as a separate arbitration concern,
- **speculative lease lifecycle** as an orthogonal concern,
- **delivery-pending mechanics** as an operational concern.

This follows the law from ADR-0034:

- top-level states stay closed,
- orthogonal sub-machines stay orthogonal,
- but attach admission and slot terminal closure must be coordinated strongly enough to prevent zombie waiters.

### 6.2 Slot header word

A compliant implementation MUST represent the **shared-slot top-level state** and the **current admitted waiter count**
in one atomic primitive field.

Recommended reference shape:

````kotlin
@JvmInline
value class SlotWord(val bits: Long)
````

#### Important interpretation rule

The **coupling law** is normative:

- shared-slot state and admitted waiter count must participate in one atomic authority surface.

The **exact bit layout below is illustrative**, not constitutional.  
Implementations may choose another packed arrangement if they preserve the same law and visibility guarantees.

Recommended illustrative packing:

- bits `0..2`   : `SharedSlotStateCode`
- bits `3..23`  : `attachedWaiterCount`
- bits `24..31` : hot flags
- bits `32..63` : reserved / implementation-defined

Recommended state code mapping:

- `0` = `PENDING`
- `1` = `SUCCESS`
- `2` = `FAILED`
- `3` = `DROPPED`

Recommended hot flags:

- `FROZEN`
- `PANIC_ISOLATED`
- `COMPLETION_VISIBLE`
- `DELIVERY_PENDING`
- implementation-defined local optimization bits

#### CLOSE visibility note

`PartitionRegion` remains the authoritative owner of region lifecycle and close publication.

If an implementation keeps a slot-local close-visible bit, that bit is:

- an optimization or derived mirror,
- never the source of truth,
- and never a replacement for authoritative region-state observation.

Attach admission must still treat region lifecycle as authoritative.  
A slot-local close bit may only be used as a lawful optimization after or alongside authoritative region-state
visibility.

#### Why state and waiter count are coupled

Attach admission and slot terminalization race on the same semantic boundary:

- attach success creates a lawful attached waiter,
- terminal slot closure forbids future attachment.

If these concerns are split across unrelated atomic objects, the implementation can admit a waiter while terminal
closure has already become authoritative.

That outcome is illegal.

Therefore:

- slot state and admitted waiter count MUST be coordinated through one atomic authority field,
- attach success MUST be impossible if the slot has already terminalized,
- and terminal closure MUST freeze further attachment without requiring a second best-effort repair pass for
  correctness.

The exact packed layout is implementation-defined.  
The coupling law is not.

#### One-way terminal law and ABA immunity

A slot terminal state is one-way:

- `PENDING -> SUCCESS`
- `PENDING -> FAILED`
- `PENDING -> DROPPED`

No terminal slot may ever transition back to `PENDING`.

Because the shared-slot top-level state is one-way and terminally sealed, the lifecycle authority does not rely on a
`PENDING -> terminal -> PENDING` cycle.  
Accordingly, the implementation need not introduce an epoch-style ABA discriminator merely to defend against re-opening
of the same slot lifecycle.

This does **not** remove the need for ordinary stale-reference and stale-callback defense.  
It only means the top-level slot state machine itself is one-way and therefore not cyclic at the authority surface.

### 6.3 Waiter word

Each waiter cell MUST represent waiter terminalization with its own primitive atomic field.

Recommended reference shape:

````kotlin
@JvmInline
value class WaiterWord(val bits: Int)
````

Recommended illustrative packing:

- bits `0..1` : `WaiterStateCode`
- bit  `2`    : `DELIVERY_QUEUED`
- bit  `3`    : `DELIVERY_DONE`
- bits `4..31`: reserved / implementation-defined

Recommended state code mapping:

- `0` = `ATTACHED`
- `1` = `RESUMED`
- `2` = `TIMED_OUT`
- `3` = `CANCELLED`

This is the sole terminalization authority for the waiter axis.

### 6.4 Builder handle word

Builder-handle state MAY be represented by an `Int` or `Long` primitive field.

Recommended mapping:

- `0` = `OPEN`
- `1` = `COMMITTED`
- `2` = `ABORTED`

No handle may transition twice.

A compliant implementation may additionally track supervisory deadline or lease linkage outside the builder-handle state
word.

### 6.5 Commit-right word

Commit-right arbitration SHOULD be represented by a separate primitive field.

Recommended mapping:

- `0` = `UNCLAIMED`
- `1` = `CLAIMED`
- `2` = `RELEASED`

This field is the only authority for entry into authoritative publication.

It must not be collapsed into builder-handle state because:

- multiple builders may exist,
- but only one commit-right winner may publish.

### 6.6 Speculative lease word

Speculative leases are orthogonal to slot state, waiter state, builder-handle state, and commit-right state.

A slot MAY encode speculative-lease count or issuance bits separately.

Recommended conceptual states:

- `ISSUED`
- `RELEASED`

Lease absence is modeled as absence of a lease record, not as a top-level lease state code.

The implementation MAY use:

- a primitive counter,
- a lease bitmap,
- or an equivalent slot-local primitive surface,

provided it satisfies:

- exact-once release,
- force-release on slot terminalization,
- and bounded duplicate-builder governance.

### 6.7 Delivery-pending surface

Delivery reliability is operational and must not redefine lifecycle truth.

A compliant implementation MUST have a slot-local or queue-local surface capable of representing at least:

- terminal lifecycle truth is already decided,
- delivery has not yet completed,
- retry / drain remains necessary.

This MAY be encoded via:

- a `DELIVERY_PENDING` bit in the slot word,
- a delivery queue record,
- an overflow list node,
- or an equivalent durable operational marker.

The exact representation is implementation-defined.  
The law is:

- delivery lag is legal,
- delivery loss is not allowed to mutate lifecycle truth,
- reclamation must wait until pending delivery becomes unreachable as pending work.

---

## 7. Reference Runtime Shape

The following is an illustrative reference shape only.

````kotlin
internal final class InFlightSlot<N : Any> {
    @Volatile
    private var slotWord: Long = initialPendingWord()

    @Volatile
    private var waiterHead: WaiterCell? = null

    @Volatile
    private var successNode: N? = null

    @Volatile
    private var terminalFailure: Throwable? = null

    @Volatile
    private var builderHead: BuilderHandleCell? = null

    @Volatile
    private var commitRightWord: Int = UNCLAIMED_COMMIT_RIGHT

    @Volatile
    private var leaseWord: Long = 0L

    // padding or @Contended handled separately
}
````

````kotlin
internal final class WaiterCell {
    @Volatile
    private var waiterWord: Int = ATTACHED_WORD

    @Volatile
    var next: WaiterCell? = null

    @Volatile
    var outcomeRef: Any? = null

    @Volatile
    var generation: Long = 0L
}
````

````kotlin
internal final class BuilderHandleCell {
    @Volatile
    var handleWord: Int = OPEN_HANDLE

    @Volatile
    var supervisoryDeadlineNanos: Long = 0L

    @Volatile
    var next: BuilderHandleCell? = null
}
````

Important notes:

- `successNode` / `terminalFailure` are payload holders, not lifecycle authorities.
- `slotWord` is the shared-slot authority.
- `waiterWord` is the waiter authority.
- `commitRightWord` is the publication authority.
- builder linkage and waiter linkage are operational state, not semantic authority.
- the payload fields must obey the visibility law described below.

---

## 8. Atomic Transition APIs

External code must not manipulate raw state words directly.

The implementation should expose command-style lifecycle APIs.

Recommended reference surfaces:

````kotlin
internal interface SharedSlotLifecycle<N : Any> {
    fun tryAttachWaiter(waiter: WaiterCell): AttachDecision
    fun tryPublishSuccess(node: N): Boolean
    fun tryFailShared(cause: Throwable): Boolean
    fun tryDropShared(cause: Throwable): Boolean
    fun readSharedStateAcquire(): SharedSlotState
}
````

````kotlin
internal interface WaiterLifecycle {
    fun tryResume(result: Any): Boolean
    fun tryTimeout(): Boolean
    fun tryCancel(reason: Throwable): Boolean
    fun readWaiterStateAcquire(): WaiterState
}
````

````kotlin
internal interface BuilderHandleLifecycle {
    fun tryCommit(): Boolean
    fun tryAbort(): Boolean
}
````

````kotlin
internal interface CommitRightLifecycle {
    fun tryClaim(): Boolean
    fun tryRelease(): Boolean
    fun readCommitRightAcquire(): CommitRightState
}
````

`AttachDecision` and equivalent transition-result surfaces must not introduce avoidable hot-path allocation.  
A compliant implementation should use:

- enum singletons,
- primitive codes,
- inline/value wrappers,
- or an equivalent zero-allocation result surface.

This preserves three properties:

- CAS remains internal,
- the caller uses semantic commands,
- and state-machine law stays centralized.

---

## 9. Attach Admission and Post-Insertion Reconciliation

### 9.1 Required attach law

Attach is successful only if:

- the slot is still `PENDING`,
- region close/drop has not already become authoritative for rejection,
- waiter count can be advanced lawfully,
- waiter-cap governance is not exceeded,
- and exactly one future terminal waiter outcome remains reachable.

### 9.2 Attach algorithm

A compliant attach implementation should follow the sequence below.

#### Step A — preflight read

- read region state with acquire semantics
- reject immediately if the region is already effectively closed for admission
- read `slotWord` with acquire semantics
- reject immediately if shared slot is already terminal
- reject immediately if admitted waiter count is already at cap

#### Step B — atomic reservation

Attempt CAS on `slotWord`:

- expected state = `PENDING`
- expected waiter count = current count
- new state = `PENDING`
- new waiter count = current count + 1

If this CAS fails, re-read and retry or reject according to the latest state.

This is the decisive attach-reservation step.

If the latest state reveals:

- terminal shared state,
- close-published rejection,
- or waiter-cap exhaustion,

the result is attach rejection, not waiter creation.

#### Step C — waiter installation

Once slot reservation succeeds:

- install the waiter cell into the waiter registry
- use a lock-free intrusive stack/list or another bounded lawful registry
- the registry itself is not the semantic authority; it is only delivery bookkeeping

The registry must be bounded by the same semantic waiter-cap discipline that governed reservation.

#### Step D — post-insertion reconciliation

After installation, re-read the shared slot state with acquire semantics.

If the slot is no longer `PENDING`, the implementation MUST do one of:

- immediately deliver the terminal signal to the waiter, or
- remove the waiter and convert the outcome into attach rejection if the install did not become semantically visible.

No successful attach may remain in a state where no terminal signal is reachable.

### 9.3 Why the reservation comes before waiter linking

If waiter linking happens before attach reservation, the implementation can leave unreachable waiter debris.

If waiter linking happens after reservation but without reconciliation, the implementation can still miss a concurrent
terminalization edge.

Therefore the correct order is:

1. atomic attach reservation on slot word,
2. waiter installation,
3. mandatory post-insertion reconciliation.

This is the minimum correct shape.

---

## 10. Shared Success Path

### 10.1 Publication authority

The committed bucket insertion point remains the only authoritative publication point.

The shared slot must not declare success first and publish later.

### 10.2 Commit-right authority

Before any contender enters authoritative publication, it must successfully claim commit-right.

This means:

- multiple builders may race to build,
- but exactly one contender may race to publish.

A contender that loses commit-right may continue or finish local build work, but it must not enter authoritative
publication.

### 10.3 Success sequence

A compliant success path must follow this order:

1. claim commit-right
2. perform bucket-level exact re-check and publication under the authoritative bucket path
3. store the published winner payload
4. execute a release publication boundary
5. CAS `slotWord` from `PENDING` to `SUCCESS`
6. publish completion visibility for attached waiters
7. mark delivery pending if needed
8. schedule waiter delivery or wake-up work
9. attached waiters race to `RESUMED` through waiter-state CAS only
10. release commit-right

#### Reference sequence sketch

````text
if tryClaimCommitRight() fails:
    abort or degrade contender publication path

bucket.putIfAbsentOrGet(...)
winnerPayload := authoritativeWinner
releaseFence()
CAS(slotWord, PENDING -> SUCCESS|FROZEN|COMPLETION_VISIBLE)
markDeliveryPendingIfNecessary()
enqueue completion delivery
releaseCommitRight()
````

### 10.4 Visibility law

A resumed waiter must see:

- the terminal `SUCCESS`,
- and the authoritative winner payload,
- in that semantic order.

The implementation may use:

- `VarHandle.setRelease`,
- CAS with release semantics,
- or an equivalent lawful JVM primitive.

The exact primitive is implementation-defined.  
The visibility law is not.

---

## 11. Shared Failure Path

### 11.1 Meaning

Shared failure means the slot terminalizes unsuccessfully as a shared event.

This is not waiter-local timeout.  
This is not waiter-local cancellation.

### 11.2 Failure sequence

A compliant failure path must:

1. publish the failure payload/cause
2. execute release visibility for that failure
3. CAS `slotWord` from `PENDING` to `FAILED`
4. make terminal completion visible to all attached waiters
5. mark delivery pending if needed
6. force lease release
7. allow each attached waiter to race from `ATTACHED` to `RESUMED` through waiter-state CAS, with an exceptional outcome
   payload

### Important semantic point

Waiters attached to a `FAILED` shared slot do **not** become `CANCELLED` or `TIMED_OUT` merely because the shared slot
failed.

They become terminal by **observing the authoritative shared terminal signal**.  
Therefore they converge through `RESUMED` with exceptional payload delivery, unless a waiter-local timeout/cancel
already won earlier.

This preserves waiter-state meaning and keeps timeout/cancel waiter-local.

---

## 12. Shared Drop Path

### 12.1 Meaning

Drop is region-driven terminalization caused by close/drop governance.

It is not equivalent to shared operational failure.

### 12.2 Drop sequence

A compliant drop path must:

1. make close publication authoritative at the region axis
2. identify visible in-flight slots
3. publish drop cause/payload for each slot
4. CAS each slot from `PENDING` to `DROPPED`
5. make terminal completion visible to attached waiters
6. mark delivery pending if needed
7. force-release leases
8. ensure attached waiters converge before reclamation completes

Waiters observing drop also converge through `RESUMED` with exceptional terminal payload, unless waiter-local
timeout/cancel already won earlier.

---

## 13. Waiter Timeout and Cancellation

### 13.1 Timeout

Timeout is waiter-local.

The timeout path must:

1. observe waiter still `ATTACHED`,
2. race through waiter CAS only,
3. transition `ATTACHED -> TIMED_OUT` if it wins,
4. never mutate shared-slot terminal state.

If it loses to completion or cancellation, it becomes a no-op for waiter state.

### 13.2 Cancellation

Cancellation is waiter-local.

The cancellation path must:

1. observe waiter still `ATTACHED`,
2. race through waiter CAS only,
3. transition `ATTACHED -> CANCELLED` if it wins,
4. never mutate shared-slot terminal state.

If it loses to completion or timeout, it becomes a no-op for waiter state.

### 13.3 Why waiters still use `RESUMED` on shared failure/drop

The waiter axis models **how the waiter terminalized**, not which shared cause existed.

From the waiter’s perspective:

- timeout = local deadline won,
- cancel = local cancellation won,
- resumed = shared terminal signal won.

The cause carried by resumed delivery may represent:

- success,
- shared failure,
- or drop.

That distinction belongs to payload / reason, not top-level waiter state.

---

## 14. Re-verification and Panic Isolation

### 14.1 Re-verification rule

A waiter resuming from shared `SUCCESS` must re-verify through the authoritative bucket path.

If the waiter observes a terminal shared success signal but cannot observe the committed winner through the bucket path,
this is a hard integrity failure.

### 14.2 Panic escalation

Re-verification failure must not degrade to:

- miss,
- transient retry,
- bypass,
- or “probably a race”.

Instead the implementation must:

1. mark the region/shard as panic-isolated
2. reject further mutable admission
3. propagate panic/isolation to partition governance
4. fail the current operation/session deterministically
5. initiate a panic delivery sweep for any still-visible attached waiters

The exact panic-plumbing API may vary, but its semantic outcome must be stronger than ordinary circuit-open degradation.

### 14.3 Waiter handling during panic

A detecting waiter must not remain indefinitely `ATTACHED` after triggering panic isolation.

After panic isolation is published, the implementation must converge still-visible attached waiters by authoritative
integrity-failure delivery.

Operationally, that means:

- the detecting waiter must either win a lawful waiter-terminal transition immediately,
- or be included in the panic delivery sweep,
- but it must not be left attached as a lingering waiter.

### 14.4 Why this is necessary

If publication-before-completion is violated even once, correctness is no longer a latency concern.  
It is a protocol integrity break.

Treating that as a recoverable operational miss would hide corruption.

---

## 15. Builder Supervision Mechanics

### 15.1 Supervisory requirement

An `OPEN` builder handle must not remain indefinitely outstanding.

A compliant implementation MUST establish a lawful supervisory regime that detects at least:

- abandoned builder authority,
- lost builder progress,
- or builder convergence beyond the allowed supervisory deadline.

### 15.2 Supervisory response

If supervisory detection determines that an `OPEN` handle has failed to converge, the supervisor must force the handle
to `ABORTED`.

That force-abort is a lawful builder-handle terminalization source.

### 15.3 Shared-slot effect of supervisory abort

Supervisory force-abort does not by itself mean panic.

Instead it must converge through the same lawful shared failure or duplicate-publication-safe resolution path as any
other non-integrity builder failure, unless the surrounding failure reason is integrity-class.

### 15.4 Why supervision exists

The lifecycle law cannot depend on indefinite builder cooperation.

Supervision turns lost builder progress from an unbounded assumption into a bounded convergence mechanism.

---

## 16. Commit-Right Mechanics

### 16.1 Purpose

Commit-right exists to separate:

- permission to build,
- from permission to publish.

This separation is required whenever:

- speculative builders are allowed,
- duplicate builders are tolerated,
- or builder execution may outlive earlier arbitration expectations.

### 16.2 Claim rule

Only a contender that successfully transitions commit-right from `UNCLAIMED` to `CLAIMED` may enter authoritative
publication.

All losing contenders must treat publication as forbidden, even if they completed local build work.

### 16.3 Release rule

Commit-right must transition to `RELEASED` when the authoritative publication episode has converged.

No subsequent contender may re-claim it for that publication episode.

### 16.4 Exact-once effect

Commit-right is an exact-once publication gate.  
It is not an advisory hint.

---

## 17. Speculative Lease Mechanics

### 17.1 Ownership

Speculative leases are slot-owned.

A waiter or builder may hold the right to act under a lease, but the lease itself belongs to the slot lifecycle.

### 17.2 Issuance

A lease may be issued only while the slot is still compatible with speculative promotion policy.

Issuance must be bounded by governance quota.

### 17.3 Release

A lease must be released exactly once by one of:

- successful speculative-builder completion,
- speculative-builder abort,
- shared-slot terminalization,
- region drop,
- panic isolation.

### 17.4 Force-release on slot terminalization

If a slot terminalizes while a speculative lease is still live, slot terminalization must force-release it.

The implementation must not depend solely on builder/handle discipline for lease release correctness.

---

## 18. Delivery Reliability Mechanics

### 18.1 Delivery truth vs lifecycle truth

Lifecycle truth is decided by lifecycle authority fields.

Delivery truth is operational and must not override lifecycle truth.

That means:

- enqueue failure does not revert terminal state,
- callback failure does not reopen terminal state,
- queue delay does not change terminal legality.

### 18.2 Retryable infrastructure event semantics

Enqueue failure is a retryable infrastructure event, not a semantic timeout and not a semantic cancellation.

A compliant implementation must not reinterpret delivery backlog as waiter timeout.

### 18.3 Retry ownership

Retry ownership belongs to the adapter-owned delivery plane.

At minimum, a compliant delivery plane must provide:

- terminal outcome persistence or re-discoverability,
- a retry sweeper or equivalent recovery path,
- an overflow/pending-delivery representation,
- and bounded shutdown/drain semantics.

### 18.4 Retry limit semantics

This design does **not** permit arbitrary “max N retries then convert to timeout” behavior.

If delivery cannot converge, the runtime must eventually escalate as:

- infrastructure failure,
- panic/isolation,
- or shutdown failure,

but not as a waiter-local timeout substitute.

### 18.5 Pending delivery and reclamation

Region reclamation must not complete while terminal delivery remains reachable as pending operational work.

This requirement may be satisfied by:

- queue drain accounting,
- slot-local delivery-pending bits,
- generation barriers,
- or an equivalent reachability test.

---

## 19. Completion Dispatch Integration

### 19.1 Ownership

Completion-dispatch infrastructure is adapter-owned, not:

- slot-owned,
- waiter-owned,
- worker-owned,
- or region-owned.

### 19.2 Order dependency

The runtime must establish:

1. explicit slot/waiter state machine,
2. waiter CAS terminalization authority,
3. only then timeout scheduler / completion executor / completion queue.

Attaching dispatch infrastructure earlier is forbidden.

### 19.3 Delivery policy

Delivery may use:

- direct completion execution,
- a completion mailbox,
- a completion executor,
- or a similar bounded adapter-owned queue.

But delivery is never the lifecycle authority.

It is only the mechanism that carries out already-authoritative terminalization.

### 19.4 Shutdown and drop interaction

Adapter shutdown must:

1. stop new attach admission,
2. force terminal shared visibility where required,
3. keep dispatch infrastructure alive long enough to drain already-enqueued terminal deliveries,
4. and only then shut down dispatch infrastructure.

Partition drop must not destroy adapter-wide dispatch infrastructure.  
Region reclamation must not complete until the drop-induced terminal deliveries required for visible attached waiters
have become unreachable as pending work.

In other words:

- drop may enqueue delivery,
- reclamation waits for lawful convergence,
- adapter shutdown drains delivery before infra teardown.

---

## 20. Grace-Aware Reclamation Mechanics

### 20.1 Goal

Reclamation must prevent stale timeout signals, stale completion callbacks, and recycled lifecycle hosts from
re-entering a reclaimed region.

### 20.2 Preferred model

The preferred JVM-friendly model is:

- no immediate pooling of waiter cells or lifecycle hosts,
- generation tagging where needed,
- drain/barrier coordination,
- and grace-aware delayed reuse.

### 20.3 Generation quarantine

A compliant implementation should maintain at least:

- region generation,
- and, where reuse is possible, object generation or equivalent quarantine metadata.

Any stale callback observing a generation mismatch must become a no-op.

### 20.4 Reclaimed-after-grace rule

`RECLAIMED` must only be entered after all of the following hold:

- visible waiters have converged,
- pending terminal deliveries have become unreachable as pending work,
- slot-owned speculative leases are released,
- and the implementation’s grace barrier has completed.

### 20.5 Pooling policy

The simplest compliant initial implementation is:

- no waiter-cell pooling,
- no immediate slot-host reuse,
- and delayed reuse only after grace completion.

Pooling may be introduced later only if it preserves the same stale-generation safety law.

---

## 21. Contention Escalation Mechanics

### 21.1 Goal

Fast-path coordination should remain lock-free in the common case.

However, pathological CAS retry storms must not produce unbounded contention amplification.

### 21.2 Thresholded escalation

A compliant implementation SHOULD define a bounded retry threshold `K`.

Recommended initial values:

- `K = 8`
- or `K = 16`

After `K` consecutive CAS failures on the same authority surface, the contender should enter a slot-local serialized
slow path.

### 21.3 Slow-path scope

The slow path must remain **slot-local**, not shard-global, unless a stronger design note explicitly justifies wider
serialization.

Preferred choices:

- slot-local queue gate,
- slot-local monitor gate,
- or equivalent bounded serialized arbitration.

### 21.4 Semantic invariance

The slow path must not introduce new lifecycle semantics.

It may only reduce contention while preserving:

- the same state meanings,
- the same winner-takes-all law,
- and the same authority surface.

### 21.5 Slow-path failure

Failure inside the slow path must not silently degrade lifecycle semantics.

It must resolve through the same lifecycle law as the fast path:

- terminal state observation,
- lawful rejection,
- or infrastructure failure escalation.

---

## 22. False Sharing and Layout Isolation

### 22.1 Requirement

Hot state-bearing fields must be layout-isolated strongly enough that false sharing does not become:

- a correctness risk under reordering assumptions,
- or a severe contention amplifier under expected concurrency.

### 22.2 Recommended practice

A compliant implementation may use one or more of the following:

- dedicated padded holder classes,
- `@Contended` where allowed,
- manual long padding fields,
- hot/cold field segregation,
- shard-local slot placement discipline.

### 22.3 Minimum rule

At minimum, the following hot fields should not be densely interleaved with unrelated hot mutable fields under
contention:

- `slotWord`
- `waiterWord`
- `commitRightWord`
- region lifecycle state
- shard-local route tables under heavy mutation

### 22.4 Numeric guidance for manual padding

If manual padding is used, the implementation should target at least **64 bytes** of effective separation for hot
state-bearing fields, and **128 bytes** is preferred where it materially reduces contention bleed or line ping-pong on
the target runtime/hardware profile.

A common practical guideline is to reserve approximately **8 to 16 `Long` fields** of padding around a hot authority
word when no better runtime-supported isolation mechanism is available.

This numeric guidance is an implementation recommendation, not a protocol law.

---

## 23. Preferred JVM Primitive Surface

This document prefers the following priority order for JVM mechanics:

1. `VarHandle` on primitive volatile fields
2. `AtomicLongFieldUpdater` / `AtomicIntegerFieldUpdater` where justified
3. a mechanically equivalent low-level primitive that preserves the same law

This preference exists because the implementation requires:

- acquire reads,
- release writes,
- CAS on primitive fields,
- and no boxed state-object indirection.

### 23.1 Preferred access discipline

Where `VarHandle` is used, the preferred default discipline is:

- state reads: `getAcquire`
- state transitions: `compareAndSet`
- payload publication preceding state visibility: `setRelease`

A compliant implementation may use a stronger mode where necessary, but should not weaken below the required ordering
law.

The exact access mode mix may still vary if the implementation preserves the same correctness law.

---

## 24. Verification Strategy

### 24.1 Unit-level invariants

The implementation must have focused tests for:

- attach rejection creates no waiter lifecycle object,
- timeout never mutates shared-slot state,
- cancellation never mutates shared-slot state,
- shared failure/drop complete visible waiters,
- double terminalization attempts lose cleanly,
- speculative lease force-release works,
- supervisory force-abort closes orphaned handles,
- commit-right admits exactly one publication winner.

### 24.2 Stress-level invariants

The implementation must include contention-heavy stress tests for:

- attach vs success race,
- attach vs failure race,
- attach vs drop race,
- timeout vs completion race,
- cancellation vs completion race,
- re-verification failure panic isolation,
- region close publication before reclamation,
- delivery overflow/retry convergence,
- commit-right contention,
- builder orphan supervision,
- and completion-dispatch shutdown sequencing.

### 24.3 Visibility-level validation

Publication-before-completion must not be validated solely by ordinary unit tests.

Validation must include:

- repeated concurrent stress,
- visibility-sensitive harnesses,
- and observation tests that ensure no waiter sees `SUCCESS` without authoritative winner visibility.

### 24.4 Allocation regression guards

The hot routing and lifecycle path must include regression checks ensuring that:

- boxed 64-bit key routing does not reappear,
- hot-path transition code does not allocate state objects,
- attach decision surfaces do not allocate on the hot path,
- and lifecycle delivery does not accidentally retain worker-local planning state.

---

## 25. Reference Pseudocode

### 25.1 Attach

````text
read region state (acquire)
if region not open-for-attach -> reject

loop:
  oldSlotWord = read slotWord (acquire)
  if oldSlotWord.state != PENDING -> reject
  if oldSlotWord.waiterCount >= maxWaitersPerSlot -> reject

  newSlotWord = oldSlotWord.incrementWaiterCount()
  if CAS(slotWord, oldSlotWord, newSlotWord) succeeds:
      install waiter cell
      observe slotWord again (acquire)
      if terminal now visible:
          reconcile immediately
      return attached
````

### 25.2 Publish success

````text
if tryClaimCommitRight() fails:
    abort or degrade contender publication path

winner = authoritativeBucketPublishOrGet(...)
store success payload with release publication
CAS(slotWord, pendingWord, successWordFrozen)
markDeliveryPendingIfNecessary()
enqueue or trigger waiter delivery
releaseCommitRight()
````

### 25.3 Waiter timeout

````text
if CAS(waiterWord, ATTACHED, TIMED_OUT) succeeds:
    deliver timeout outcome
else:
    no-op
````

### 25.4 Shared drop

````text
publish region close
for each visible slot:
    store drop payload with release publication
    CAS(slotWord, PENDING, DROPPED|FROZEN)
    markDeliveryPendingIfNecessary()
    enqueue waiter delivery
force-release leases
after waiter convergence and pending-delivery drain become unreachable as pending work, reclaim region
````

### 25.5 Resume path re-verification

````text
waiter observes shared success signal
acquire read winner path from authoritative bucket
if winner absent:
    panic isolate region/shard
    enqueue integrity-failure delivery sweep
    detecting waiter must not remain ATTACHED
else:
    CAS(waiterWord, ATTACHED, RESUMED)
    deliver winner
````

### 25.6 Supervisory orphan handling

````text
for each OPEN builder handle:
    if supervisory deadline exceeded:
        if CAS(handleWord, OPEN, ABORTED) succeeds:
            publish lawful abort reason
            converge slot through ordinary failure path if still required
````

---

## 26. Change Classification

The following changes should normally remain within this design note and implementation:

- packed field layout refinement,
- `VarHandle` vs updater choice,
- waiter registry structure,
- padding strategy,
- dispatch executor structure,
- lease counter encoding,
- retry queue structure,
- slow-path gate implementation,
- generation tagging details.

The following changes require ADR-0034 amendment rather than mere design-note update:

- new top-level shared state,
- new top-level waiter state,
- changed meaning of `RESUMED`,
- changed publication-before-completion law,
- changed single terminalization authority rule,
- changed attach/close zombie-waiter prevention law,
- removal of builder supervision,
- collapse of build right and publish right,
- weakening of grace-aware reclamation law.

---

## 27. Final Mechanical Statement

A compliant planning L2 implementation must realize ADR-0034 through:

- primitive-field lifecycle state,
- atomic coupling of slot closure and attach admission,
- waiter-local terminalization through waiter CAS only,
- supervisory convergence of builder handles,
- exact-once commit-right arbitration,
- release/acquire publication-before-completion semantics,
- force-release of slot-owned speculative leases,
- adapter-owned completion dispatch,
- delivery reliability that does not rewrite lifecycle truth,
- panic isolation on re-verification failure,
- grace-aware reclamation,
- slot-local contention escalation,
- and physically sealed terminal state that rejects zombie writes.

Any implementation that preserves only the surface behavior while weakening these mechanical laws is non-compliant.