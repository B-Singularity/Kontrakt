# ADR-0057: Failure Contract, Explicit Machine Failure, Attribution, Scope, and Realization Boundary

## Status

Proposed

## Date

2026-08-16

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0056: Governance Contract, Policy-World Control, Whole-Machine Scope, and Selection Boundary
- ADR-0055: Whole Machine, Pipeline Composition, and Contract Concurrency
- ADR-0054: Policy Contract, Explicit Operating Modes, Self-Contained Contract Worlds, and Interface Binding Boundary
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

Failure is part of a real machine.

A machine that can refuse input, exhaust an allowance, reject a State movement, or fail to realize an established
Contract meaning must not leave those endings to be inferred from an exception, missing output, process exit, or another
implementation accident.

`What Contract Is` therefore requires declared failure. ADR-0046 later included Failure in the closed Contract
vocabulary as the Contract that declares contract-governed stop results.

The Contract model has since become more precise.

ADR-0045 separates the Contract Pipeline, the State-Machine Pipeline, and the Implementation Pipeline. ADR-0050 gives
State and Transition an independent movement authority. ADR-0055 preserves independent Contract flows when several Cores
participate in a Whole Machine. ADR-0056 further distinguishes a valid Governance judgment from failure of a backend to
realize that judgment.

Failure can therefore arise at three different authority boundaries without those boundaries becoming the same thing.

```text
Contract obligation cannot be satisfied
    -> Contract Failure

State-Machine movement cannot be established
    -> State-Machine Failure

already-established Contract meaning cannot be realized correctly
    -> Realization Failure
```

These are not three unrelated error systems. They are three origins of machine failure.

The earlier treatment of Failure as an ordinary one-dimensional pipeline slot is no longer sufficient. Failure is
encountered by the Contract axis, the State-Machine axis, and the realization boundary. At the same time, creating three
independent Failure Contracts would fragment one machine property into unrelated vocabularies.

ADR-0057 therefore treats Failure as one Contract kind whose authority is to make machine failure explicit without
taking judgment authority away from the part of the machine that actually failed.

---

## 2. Problem

Software often communicates failure indirectly.

A function throws. A worker disappears. A value is absent. A Transition does not occur. An output is never emitted.
Someone outside the failed authority then interprets the observed behavior and assigns meaning to it.

That approach confuses mechanism with Contract.

```text
implementation phenomenon
        ↓
external interpretation
        ↓
assumed failure meaning
```

Kontrakt requires the opposite direction.

```text
authoritative failure condition
        ↓
explicit Failure meaning
        ↓
replaceable realization
```

Making Failure explicit still leaves several problems.

The authority that discovers an unsatisfied Admission obligation must remain Admission. A Failure Contract must not
re-run that Admission judgment.

The same rule applies to State movement. Transition legality belongs to the State-Machine axis rather than to Failure.

Realization creates a different problem. Contract meaning may already be valid while the backend is unable to carry it
out. Reporting that condition as a Contract Failure would falsely attribute an implementation inability to the user's
Contract.

Failure scope also matters. A failed component does not automatically mean that its larger machine has failed. A failed
network stream need not invalidate its connection. One failed build task can prevent dependent work while independent
work remains meaningful. Failure therefore needs an exact boundary rather than an assumption that every failure is
global.

A final problem appears when the machine cannot determine the outcome. A remote effect may have happened even though its
acknowledgement was lost. Calling that condition Failure would claim knowledge the machine does not have.

The Failure Contract must express these distinctions without becoming a recovery engine, diagnostic system, exception
hierarchy, or runtime lifecycle manager.

---

## 3. Decision Drivers

Failure must remain explicit even when its realization changes.

The authority that owns the failed requirement must retain that judgment. Failure may preserve and expose the result,
but it cannot reinterpret another Contract or State-Machine law.

A Failure Result needs enough information to identify where the failure belongs. It must not depend on stack shape,
thread identity, transport status, or another backend artifact to recover that meaning later.

Failure is local before it is global. A higher-level failure exists only when a requirement at that higher boundary is
also no longer satisfied.

Cause and failure are different. The reason something failed may be useful evidence, but it does not replace the
contractual statement of what failed.

Response is also separate. Restarting a worker or moving a machine into a safe State happens because of a failure; those
actions are not the Failure itself.

Kontrakt must not promise an explicit final result after the physical means required to produce that result have ceased
to exist.

Determinism applies wherever Failure remains under software control. The same authoritative material cannot become a
different Failure merely because a different worker, arrival order, or runtime representation was used.

---

## 4. Contract Decision

### 4.1. Failure Is One Contract Kind

Kontrakt has one Failure Contract kind.

Contract Failure, State-Machine Failure, and Realization Failure are not independent Contract types. They identify where
one Failure originates.

The Failure Contract does not become a superior authority over the Contracts or State Machines from which failures
arise.

Its responsibility begins after the relevant authority has established the fact that its required progression cannot
continue, or at a realization boundary where the backend can still establish that an already-authorized meaning cannot
be realized correctly.

The Failure Contract owns the explicit meaning of that ending.

```text
source authority
    establishes its own unsuccessful judgment

Failure Contract
    establishes the explicit Failure Result
    without changing that judgment
```

A Failure Result must not be synthesized from the mere observation that execution stopped.

### 4.2. Meaning of Failure

Failure means that a required machine obligation or an already-authorized machine action cannot continue as required at
an exact authority boundary.

Failure is therefore relational. Something specific had to be required before its failure can have meaning.

```text
required obligation or authorized action
        ↓
cannot continue as required
        ↓
Failure
```

The Failure Contract does not define a machine as failed merely because an unusual event occurred.

A Fault may exist without causing Failure. An internal error may be contained. A temporary disturbance may leave every
required obligation satisfied.

Failure begins only where a required meaning is no longer established.

### 4.3. Contract Failure

A Contract Failure originates when a Contract Authority establishes that one of its required obligations is not
satisfied.

The owning Contract remains the judge.

```text
Admission
    judges Admission

Budget
    judges Budget

Invariant
    judges Invariant
```

Failure does not repeat those judgments.

If Admission determines that required material is absent, the Failure Result is attributed to that Admission judgment.
If Budget determines that the applicable allowance has been exhausted, the Failure Result belongs to that Budget
application.

A Contract Failure stops the Contract progression that requires the failed obligation.

This does not make every other flow fail. ADR-0055 continues to govern the independence of admitted flows.

### 4.4. State-Machine Failure

A State-Machine Failure originates in the State-Machine axis.

A requested movement may be refused because the declared source condition does not permit the selected Transition. The
machine may also be unable to establish one unambiguous current State under the applicable State-Machine law.

Those failures remain State-Machine failures because the failed meaning concerns legal movement or State establishment.

```text
established State
        +
requested Transition
        ↓
State-Machine judgment
        ↓
movement refused
        ↓
State-Machine Failure
```

The refusal does not invent a new State.

The previously established State remains authoritative unless another valid Transition establishes a replacement.

A physical inability to perform a movement that the State-Machine Contract already authorized is not a State-Machine
Failure. It belongs to Realization Failure.

### 4.5. Realization Failure

A Realization Failure exists when Contract or State-Machine meaning has already been established but Kontrakt or its
backend cannot correctly realize that meaning.

```text
authoritative Contract result
        ↓
backend realization
        ↓
required realization cannot be completed correctly
        ↓
Realization Failure
```

This boundary prevents implementation inability from being rewritten as Contract meaning.

If Governance establishes that an Emergency Policy World must apply and the backend cannot publish that World correctly,
the Governance decision remains valid. The failure belongs to realization.

If Publication authorizes a claim but the backend cannot carry the already-authorized claim through its required
realization boundary, Publication has not retroactively refused the claim.

A backend may not select another semantically different result merely because that result is easier to realize.

### 4.6. Failure Mode Is Not Failure Cause

Failure needs to preserve how a required function failed when that distinction is part of the authoritative result.

A required input may be missing. A declared condition may instead be present and unsatisfied. A movement can be refused
without any material being absent. An authorized realization may begin correctly and later become impossible.

Those are different Failure modes when the owning authority distinguishes them.

Kontrakt does not impose one universal engineering taxonomy on every Contract.

An exact judgment coordinate may already carry the complete mode distinction. When it does, the Failure Contract must
not require the user to declare the same distinction again under another name.

A Failure cause is different.

The exhausted operating-system resource that caused a backend write to fail may help explain a Realization Failure, but
that resource event is not the contractual identity of the failed realization. Such explanatory material belongs to
Diagnostic Evidence unless another Contract independently gives it authority.

The same separation applies to Failure effect. A local Failure may later cause a larger machine requirement to fail, but
the later Failure is established at the later boundary rather than copied from the first one.

### 4.7. Exact Attribution

Every Failure Result has an exact origin.

Attribution identifies the authority that established the unsuccessful result and the obligation or realization boundary
that could not continue.

Where Contract World or Version changes the meaning of that authority, the applicable identities are retained as part of
the Failure attribution.

Attribution must survive lowering and backend replacement. It cannot be reconstructed from a stack trace, object
identity, package name, or physical worker.

Conceptually:

```text
Failure Result
    origin
    exact authority coordinate
    exact failed obligation or realization boundary
    applicable Contract identity when required
```

The canonical representation is deferred, but loss of this information is not permitted.

### 4.8. Failure Scope, Containment, and Propagation

Failure applies first to the exact progression that depended on the failed requirement.

A Contract Failure inside one admitted flow terminates that flow's contractual continuation. It does not reach sideways
into another admitted flow.

A State-Machine Failure terminates the requested movement. It does not make unrelated Operations invalid.

A Realization Failure invalidates the realization that required the failed boundary. Whether a larger machine has also
failed depends on the Contracts of that larger boundary.

```text
local Failure
    does not imply
higher-scope Failure
```

A higher scope fails only when its own required function can no longer be satisfied.

This rule makes containment explicit. Redundancy, alternate physical paths, replicated workers, or another backend
strategy may prevent a local implementation failure from becoming a higher-level Failure. Those mechanisms do not erase
the local Failure that occurred.

Propagation is therefore not an automatic chain of copied Failure flags.

Each boundary establishes its own Failure when its own obligation is no longer satisfied.

### 4.9. Failure Is Not an Indeterminate Outcome

Failure requires enough authority to establish that the required progression did not succeed as required.

Some distributed realizations cannot establish that fact.

A request may have produced an irreversible remote effect immediately before communication was lost. The caller may know
that it no longer has a usable result while still being unable to know whether the effect occurred.

```text
effect definitely did not occur
    -> may establish Failure

effect may or may not have occurred
    -> Indeterminate Outcome
```

Kontrakt must not convert uncertainty into Failure merely to obtain a binary result.

The exact Contract authority and canonical representation for an Indeterminate Outcome are deferred. ADR-0057 decides
only that established Failure and unknown outcome are semantically different.

### 4.10. Failure Does Not Own Recovery

Failure declares the failed machine result. It does not own the mechanism chosen afterward.

A retry does not alter the Failure that caused the first attempt to stop. A replacement worker does not make the earlier
Realization Failure disappear.

Likewise, moving equipment into a safe condition is a response to Failure rather than the definition of Failure. In
Kontrakt, an explicit machine-State response must still satisfy the State-Machine Contract that governs that movement.

Governance may establish another Policy World only when its own Decision Law authorizes that change. Failure cannot
silently change Governance meaning.

Automatic restart, backoff, failover, replay, rollback, and compensation remain backend concerns unless another explicit
Contract gives some resulting behavior independent Contract meaning.

### 4.11. Failure Has a Physical Authority Limit

A running machine can establish Failure only while sufficient machinery remains available to establish it.

The process may disappear before it can produce a final result. Power may be lost. The physical execution substrate may
cease to exist.

The destroyed execution does not perform one last Failure judgment.

```text
software still has authority and machinery
    -> explicit Failure may be established

machinery required to establish the result no longer exists
    -> no fictional final Failure
```

Contracts may require durable material to have been established before such a loss. A later execution may inspect that
material under its own Contracts.

Those are obligations of living executions on either side of the physical loss. They are not a final action attributed
to an execution that no longer exists.

### 4.12. Failure, Diagnostic Evidence, Publication, and Output Presentation

Failure establishes the authoritative unsuccessful result.

Diagnostic Evidence explains or supports that result without becoming its authority.

Publication decides whether a Failure or a fact derived from it may become an outward claim.

Output Presentation declares the outward shape through which an authorized claim appears.

```text
Failure
    what failed and where

Diagnostic Evidence
    evidence that explains the established result

Publication
    whether an outward claim is authorized

Output Presentation
    the shape of that authorized claim
```

The Failure Contract therefore does not own stack traces, human-readable messages, external status codes, or API payload
shape.

It also does not decide retention. Diagnostic Retention governs whether permitted evidence remains after the relevant
processing ends.

---

## 5. Engineering Model

### 5.1. Reliability and Aerospace Engineering

Reliability engineering distinguishes the failed function from the reason that initiated the failure and from the effect
seen by a larger system.

This distinction is useful to Kontrakt because a backend resource fault can cause a Realization Failure without becoming
the identity of that Failure.

Failure analysis also examines the level at which an effect becomes visible. A local equipment failure may be contained
while the mission-level function remains available.

ADR-0057 adopts that boundary principle rather than importing a hardware failure taxonomy.

The exact requirement that stopped remains authoritative. A larger failure must be established separately when the
larger machine can no longer fulfill its own purpose.

### 5.2. Nuclear and Industrial Safety Engineering

Safety-critical engineering cannot assume that one component failure is identical to failure of the protected system.

The system may contain that failure and preserve its safety function.

A safe shutdown is also not the same event as the failure that required it. One is the unsuccessful condition; the other
is a controlled machine response.

Kontrakt follows the same separation.

```text
Failure
    establishes what could not continue

State Machine or other explicit Contract
    establishes the permitted response
```

A backend may realize the response through hardware, process control, or distributed coordination. Those mechanisms do
not change which authority owns the response meaning.

### 5.3. Network and Distributed Systems

Network protocols provide a useful example of Failure scope.

A failure confined to one logical stream does not necessarily invalidate the whole connection. A transport failure also
has different authority from an application-level refusal.

Distributed execution adds another distinction. Loss of a response does not prove that the remote effect failed.

ADR-0057 therefore treats both Failure scope and outcome certainty as semantic concerns rather than as properties to be
guessed from a connection closing.

### 5.4. Build and Infrastructure Systems

Build systems routinely distinguish a failed unit of work from the larger build.

Dependent work may become impossible while unrelated work can remain valid. Infrastructure systems likewise separate a
container failure from the policy that decides whether the container is restarted.

These systems demonstrate two boundaries used by ADR-0057.

Failure propagation follows actual dependency rather than physical proximity.

Failure response remains separate from the failure result.

### 5.5. Programming Languages

Programming languages provide a smaller but useful precedent for explicit failure channels.

Some languages expose ordinary recoverable errors as values or typed result alternatives. Others distinguish those paths
from unrecoverable program termination.

Kontrakt does not adopt any of those language mechanisms as Contract meaning.

```text
Result type
error value
error union
exception
panic
```

are realization choices.

Their useful lesson is narrower: software can expose unsuccessful outcomes explicitly rather than force callers to infer
them from hidden control flow.

The Failure Contract applies that lesson at the Contract Machine level without binding the Contract to a host language.

---

## 6. Frontend and Canonical Failure Material

### 6.1. Authoring Location Is Not Decided Here

ADR-0057 decides the semantic authority of Failure before deciding its user API.

Failure is one Contract kind, but this does not imply one project-global source declaration.

It also does not imply that every Interface must repeat the same Failure binding.

The frontend must eventually express enough material to make all required Failure distinctions explicit without turning
filesystem location, Interface nesting, package structure, or generated code into Failure authority.

The following remain candidates for later frontend work:

```text
explicit named Failure declarations

Failure identities derived from exact unsuccessful judgment coordinates

a shared Failure declaration referenced by several Contract surfaces

compiler-established intrinsic Failure structure with user-declared modes
```

This ADR chooses none of them.

### 6.2. Canonical Material Requirements

The canonical Failure representation must preserve the distinction between the three Failure origins.

It must identify the exact authority position to which the Failure belongs.

If the owning authority defines several semantically distinct unsuccessful outcomes, the canonical form must preserve
that distinction without requiring a redundant second taxonomy.

Scope must be sufficient to determine which contractual progression can no longer continue.

Contract World and Version participate when they are necessary to identify the meaning of the failed Contract
application.

No canonical Failure identity may depend on backend accident.

### 6.3. No Backend Error Vocabulary as Contract Authority

Host-language exceptions do not define Failure identities.

Transport status values do not define them either.

The same prohibition applies to process exit codes, operating-system errors, database driver errors, queue error codes,
and similar implementation surfaces.

A backend may map those signals into the realization logic that establishes or carries a canonical Failure Result.

The mapping remains replaceable.

---

## 7. Contract and Implementation Boundary

### 7.1. Contract Meaning

The Failure Contract owns the fact that an unsuccessful machine result is explicit.

It owns the distinction between Contract Failure, State-Machine Failure, and Realization Failure.

It preserves exact attribution and local scope.

It also prevents uncertain outcome from being mislabeled as established Failure.

Failure does not acquire the judgment authority of the Contract or State Machine that produced the unsuccessful result.

### 7.2. Realization

A backend chooses how Failure is represented and transported internally.

One backend may use generated branches and primitive identifiers. Another may use a typed result object. A boundary
adapter may temporarily map a Failure to a host-language exception where an external API requires that mechanism.

Those choices are not the Contract.

The backend also owns physical detection mechanisms used to determine that realization can no longer continue. Resource
probes, transport failures, storage responses, process supervision, and execution monitoring are realization machinery.

Where that machinery can still establish a Realization Failure, it must map the event to the canonical Failure meaning
without inventing a different Contract result.

### 7.3. Recovery Mechanisms

Retry and restart remain outside Failure authority.

So do physical failover, replay, compensation, rollback, circuit breaking, worker replacement, and process
reconstruction.

If one of those mechanisms produces a new Contract interaction, that interaction begins under its own applicable
Contracts.

A Failure Result from the earlier interaction is not rewritten simply because later recovery succeeds.

### 7.4. Diagnostics and Operational Observation

Logs and traces may observe a Failure.

They are not the Failure.

Stack frames, host exceptions, node identities, timestamps, journal positions, and runtime metrics may become Diagnostic
Evidence when permitted.

Failure identity must remain valid if every one of those mechanisms is replaced.

---

## 8. Verification and Determinism

The compiler must reject a Failure declaration that depends on runtime object identity, physical execution order, or
another backend-only coordinate for its Contract meaning.

An exact Failure identity cannot name an authority that does not exist in the selected Contract World.

Failure attribution must resolve to one exact source authority. Ambiguous attribution is not repaired by selecting the
first candidate encountered during compilation or execution.

The backend must preserve the established Failure origin. A Realization Failure cannot be rewritten as an Admission,
Budget, State-Machine, Governance, or Publication Failure because such a label would be more convenient for an external
API.

Failure scope cannot expand from implementation topology alone.

If two independent Contract flows are running and one fails, scheduler placement or shared worker ownership does not
make the other flow contractually failed.

When the same canonical material reaches the same authoritative unsuccessful judgment, the contract-visible Failure
Result must remain the same regardless of thread timing or acquisition order.

Verification cannot require an execution that has physically ceased to produce a final Failure Result.

It also cannot convert an indeterminate remote outcome into definite Failure merely because the backend needs a terminal
local branch.

An invalid Failure definition is rejected during compilation or Contract resolution. That rejection is not itself a
runtime Failure Result of the machine being defined.

---

## 9. Deferred Decisions

The following questions remain open:

1. What exact source form declares Failure and where that declaration is bound without creating a project-global
   authority or repetitive Interface boilerplate?
2. Does every user-visible Failure receive an explicit name, or can exact unsuccessful judgment coordinates provide
   canonical Failure identity directly?
3. Which Failure distinctions require explicit mode material beyond the owning judgment coordinate?
4. What exact canonical Scope material is needed for Contract, State-Machine, and Realization Failure?
5. What Contract authority owns an Indeterminate Outcome, and how is that result represented without pretending it is
   either success or Failure?
6. Which Failure material may Publication authorize for external use, and which parts remain internal attribution?
7. What Diagnostic Evidence may be attached to each Failure origin without making evidence authoritative?
8. What Retention rules apply to Failure-related evidence after the originating processing has ended?
9. What finite material may a backend use to establish Realization Failure without exposing host exceptions or runtime
   infrastructure as Contract Authority?
10. Which passages in `What Contract Is`, ADR-0046, ADR-0047, ADR-0049, and ADR-0050 must be revised after this model is
    accepted, especially earlier language that treats Failure as an ordinary one-dimensional pipeline slot or describes
    declared failure only from the Admission perspective?

These questions do not reopen the distinction between failure meaning and implementation mechanism.

---

## 10. Consequences

### Positive

Failure becomes an explicit machine result rather than an inference drawn from backend behavior.

Contract, State-Machine, and Realization failures retain their own authority boundaries while sharing one Failure
Contract vocabulary.

A failure remains attributable after lowering because its identity no longer depends on the mechanism that carried it.

Local failure does not silently become whole-machine failure. Larger scopes fail only when their own required meaning
can no longer be established.

Diagnostic Evidence can explain a failure without becoming the source of its authority.

Recovery mechanisms remain replaceable because Failure does not prescribe retry, restart, failover, rollback, or another
backend response.

The model can represent the difference between established Failure and an outcome that cannot be known.

### Negative

Failure is no longer a simple ordinary slot in the one-dimensional Contract Pipeline catalog. ADRs and planning
documents that assume that placement must be revised.

The frontend cannot be finalized until Failure naming, binding, and canonical identity are decided.

Realization Failure requires a disciplined mapping from backend events into contract-visible meaning without promoting
backend error vocabularies into Contract Authority.

Distributed execution exposes cases where the machine cannot honestly produce either success or Failure. That requires a
separate treatment of indeterminate outcomes.

### Neutral

Failure being one Contract kind does not require one global declaration.

This ADR does not determine how failures appear to external callers.

It does not prescribe an exception model, result type, process supervision strategy, or transport protocol.

The existing backend may already contain useful failure-detection mechanisms. Their continued use depends on whether
they can realize this Contract without owning its meaning.