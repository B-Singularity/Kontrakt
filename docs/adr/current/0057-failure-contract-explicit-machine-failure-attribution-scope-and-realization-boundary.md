# ADR-0057: Failure Contract, Explicit Machine Failure, Attribution, and Realization Boundary

## Status

Accepted

## Date

2026-08-16

## Related

- `../../the-most-important-thing/what-contract-is.md`
- `../../todo/kontrakt-verifier-implementation-plan.md`
- ADR-0056: Governance Contract, Policy-World Control, and Selection Boundary
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

Failure is unavoidable in a real machine, so a Contract Machine must be able to state failure as explicitly as it states
the obligations that were meant to succeed. If failure meaning has to be recovered from implementation behavior, the
mechanism of failure has already become part of the Contract.

`What Contract Is` introduced declared failure for this reason, and ADR-0046 carried it into the Contract vocabulary.
Later work refined the machine boundaries enough to show that Failure is broader than an ordinary Contract-Pipeline
result. A Contract judgment can fail, a State-Machine judgment can fail, and an already-established meaning can fail
during realization.

```text
Contract obligation cannot be satisfied
    -> Contract Failure

State-Machine result cannot be established
    -> State-Machine Failure

required realization of established machine meaning does not complete
    -> Realization Failure
```

These failures arise at different machine boundaries, but they express the same machine-level fact: required machine
meaning was not established as required.

Failure is nevertheless an ordinary machine outcome. An unsuccessful judgment does not escape the Contract Machine as
abnormal control flow. It establishes Failure, stops the normal subsequent processing that depends on the failed
meaning, and enters internal Failure processing.

That makes the earlier placement of Failure as an ordinary one-dimensional pipeline slot too narrow. ADR-0057 therefore
treats Failure as one Contract kind that crosses Contract, State-Machine, and realization boundaries while preserving
the exact source that established each failure.

---

## 2. Problem

Software commonly leaves Failure meaning implicit behind implementation control flow.

An exception exposes an unsuccessful path, but it does not state the exact machine Failure. The failed obligation and
the source that established the unsuccessful result still have to be reconstructed from implementation context. If no
handler accepts that control transfer, execution may unwind out of its entry and terminate a thread or another
realization resource, even though that lifetime behavior says nothing by itself about the machine meaning that failed.

A stack trace does not solve the semantic problem. It may expose thousands of frames while still leaving the developer
to infer which machine obligation actually failed. More execution history is not a clearer Failure statement, and
capturing that history also adds work to the failure path.

Applications often compensate by giving exception hierarchies domain-like names and interpreting them through
centralized handlers or interception. In failure handling, AOP can become a workaround for meaning that was never
explicit at the failure boundary. This repeats the same implementation-leakage problem seen when proxy or inheritance
structure is allowed to stand in for Contract meaning.

The result is that software often asks later machinery to reconstruct Failure from exception type, stack shape, handler
placement, logs, or other backend evidence. Runtime values and physical causes may still be useful for diagnosis, but
they cannot substitute for an explicit statement of what machine meaning failed.

Kontrakt must remove that reconstruction step.

```text
implementation evidence
        ↓
reconstruct Failure meaning

must become

authoritative unsuccessful result
        ↓
explicit Failure
        ↓
replaceable realization
```

---

## 3. Decision Drivers

Failure meaning must exist independently of the mechanism used to realize it. The authority that owns an obligation must
remain the authority that decides whether its required meaning was satisfied; Failure records that unsuccessful result
without judging the obligation again. Exact attribution therefore cannot depend on later inspection of exception
hierarchy, stack shape, runtime registration, or another implementation artifact.

Failure must remain local to the source and machine boundary in which it is established. It does not define a second
scope system over the Contract Pipeline, State-Machine axis, or Implementation Pipeline, and another boundary does not
inherit the Failure merely because its later processing depends on earlier work.

Failure is ordinary Contract Machine flow. Within one active processing boundary, Contract-Pipeline processing,
additional applicable Contracts such as Budget or Capacity, State-Machine processing, and required realization may
overlap. Discovering one Failure does not abandon other applicable processing that remains independently reachable
without requiring failed meaning. The boundary completes that processing, preserves every established result, and only
then decides whether normal processing may proceed. If one or more Failures were established, later normal processing
that requires the boundary to have succeeded is not entered.

This rule follows machine authority and dependency rather than implementation location or axis. Budget, Capacity,
Admission, Invariant, State-Machine work, or realization remains subject to the active-boundary rule when it belongs to
the user's Contract Machine, even when Kontrakt backend code performs the physical execution. Only Kontrakt's own
unrelated compiler or backend implementation machinery may use internal fail-fast semantics.

Failure does not require termination of a thread, worker, process, coroutine, or another realization resource. Cause,
Diagnostic Evidence, recovery, and later effects remain separate authorities. A crash is also separate because abrupt
execution loss is not itself an established Failure, and an indeterminate outcome cannot be called Failure when the
relevant result cannot be known.

An established Failure may remain internal or may need to become an outward machine claim. Failure does not own that
exposure. Publication decides whether Failure meaning may cross an external boundary, and Output Presentation later owns
the external form.

Failure can be established only while enough authority and machinery remain to state the unsuccessful result. Where that
remains possible, the same authoritative conditions must establish the same Failure; where that machinery is lost, the
machine must not fabricate a final Failure.

Failure processing must be deterministic. Discovery order, thread scheduling, memory address, hash-table iteration,
runtime registration order, cache reuse, or another implementation accident cannot change Failure meaning, identity,
containment, Failure membership, or subsequent-processing semantics. Where representation or Diagnostic presentation
requires an order, that order must also be deterministic without becoming semantic priority.

V1 does not require persistent incremental Failure artifacts, but its Failure model and lowering must remain compatible
with V2 incremental compilation. Failure processing therefore cannot depend on hidden mutable compiler state or
representation choices that prevent stable identity, explicit dependency tracking, cache reuse, deterministic
recomputation, or dependency-local invalidation later.

---

## 4. Contract Decision

### 4.1. Failure Is One Contract Kind

Kontrakt has one Failure Contract kind.

Contract Failure, State-Machine Failure, and Realization Failure classify the source from which Failure originates. They
do not create three unrelated Failure systems.

Failure does not judge another authority again. Contract and State-Machine sources provide their own unsuccessful
judgments, while a realization source establishes that the required realization of already-established meaning did not
complete. Failure preserves the exact failure meaning established by that source.

```text
exact source
    establishes an unsuccessful result
        ↓
Failure
    preserves that result as explicit machine meaning
```

A stopped execution is not enough by itself. Failure exists only when an authority can establish what failed.

### 4.2. Meaning of Failure

Failure exists only in relation to required machine meaning. A machine cannot fail in the abstract; an authority must
first require a specific meaning.

Failure is established when the relevant source can establish that the required meaning was not satisfied.

```text
required meaning
        ↓
not satisfied
        ↓
Failure
```

A machine can still produce a result and fail if that result does not satisfy the required meaning.

Reliability and safety engineering use the same boundary. A fault is not yet a failure while the required function
remains available. Kontrakt applies that principle to machine meaning, so an implementation fault becomes a Failure only
when an authoritative requirement can no longer be satisfied.

This also separates Failure from its cause and later effect. Failure states which required meaning was not satisfied.
The cause may explain why, while any later consequence belongs to the boundary in which that consequence becomes
meaningful.

### 4.3. Failure Contract Obligations

Failure is intrinsic rather than separately authored, but it is still a Contract. Every conforming Contract Machine and
backend must preserve the following obligations whenever Failure applies.

#### 4.3.1. Establishment

When a Contract or State-Machine authority establishes that its required meaning was not satisfied, Failure must be
established explicitly.

An authoritative unsuccessful judgment cannot disappear into a log entry, exception path, sentinel value, `null`,
backend status, or another implementation behavior without remaining explicit Failure meaning.

When a realization point establishes that the required realization of already-established machine meaning did not
complete, Realization Failure must likewise be established explicitly. The realization does not rejudge the established
Contract or State-Machine meaning.

The establishment of one Failure does not by itself complete the current active processing boundary. Boundary completion
follows the Boundary Processing Completion law below.

#### 4.3.2. Exactness and Failure Cardinality

Failure preserves the exact failure meaning established by its source.

For a Contract or State-Machine source, that failure meaning is the exact unsuccessful judgment established by the
owning authority. Each such authoritative unsuccessful judgment establishes exactly one semantic Failure.

For a Realization source, that failure meaning is the exact required realization established not to have completed. Each
exact required realization established not to have completed establishes exactly one semantic Failure.

Multiple distinct failure meanings remain distinct Failures, including when they are established within the same active
processing boundary.

Failure does not create a second taxonomy, combine distinct failure meanings into a vague error bucket, or split one
source-established failure meaning into Failure-owned subcategories.

Multiple Failures established in the same boundary have no Failure-owned semantic priority. Failure does not define a
primary, secondary, first, root, or preferred Failure among them. Any canonical order used for deterministic
representation, storage, or Diagnostic presentation is not semantic priority.

The source determines each failure meaning. Failure preserves its identity and cardinality exactly.

#### 4.3.3. Boundary Processing Completion

An active processing boundary may contain Contract-Pipeline processing, additional applicable Contracts such as Budget
or Capacity, State-Machine processing, and required realization at the same machine point. Input, Admission, Invariant,
and other 1D processing can therefore coexist with obligations from the other axes rather than forming isolated serial
boundaries.

The boundary must complete every applicable part of that machine processing that remains valid and reachable without
requiring meaning that has already failed. This rule follows machine authority and dependency, not implementation
location or axis.

```text
active processing boundary
    ├─ current 1D processing
    ├─ other applicable Contract obligations
    ├─ State-Machine processing
    └─ required realization
            ↓
complete every applicable and reachable part
that does not require failed meaning
            ↓
preserve every established result
            ↓
deterministically fix all established Failures
```

Discovering one Failure cannot suppress another independently reachable part of the same active processing boundary
merely because that Failure was discovered earlier. A Realization Failure follows the same rule: other applicable work
in the boundary continues when it does not depend on the failed realization.

Processing that requires failed meaning is not reachable and is not entered. No additional semantic result is
established merely because that processing was not reached.

The complete Failure membership of the boundary must not depend on discovery order, worker completion order, parallel
scheduling, or another implementation accident. Where applicable work shares accounting, admission, state movement, or
another ordering-sensitive dependency, its semantic order must come from the canonical machine plan rather than runtime
scheduling. Independent work may execute in parallel only when the same established results and Failure membership are
preserved.

Successful results established while the boundary is being completed remain material of their own Contract or
State-Machine authority, or of the realization that produced them. Failure does not absorb or redefine those successful
results.

The Failures established in one active processing boundary do not form a new aggregate Failure identity. Every Failure
retains its own source, failure meaning, applicable context, and boundary.

This law applies to the Contract Machine processing that Kontrakt realizes for the user. It does not require Kontrakt's
own unrelated compiler or backend implementation machinery to continue after an internal implementation failure. Such
internal machinery may fail fast under the implementation constraints defined later in this ADR.

#### 4.3.4. Finality

Once Failure is established, that established machine meaning is final.

A later retry, recovery, replacement execution, or successful attempt cannot erase the earlier Failure, rewrite it as
Success, or reassign it to another source. A later attempt establishes its own later result.

Finality concerns established machine meaning during valid Contract Machine execution and valid handoff of that meaning.
It does not require a particular immutable runtime object representation, and it does not impose post-Crash completion
or preservation obligations on destroyed execution machinery.

#### 4.3.5. Preservation and Availability

The exact meaning of an established Failure must survive every valid representation and handoff that carries that
Failure.

Canonicalization, lowering, compaction, interning, caching, backend-specific representation, Diagnostic handoff,
Publication handoff, or other valid transformation may change physical form but cannot weaken or reconstruct Failure
meaning from less authoritative implementation evidence.

While internal Failure processing, Diagnostic processing, or Publication is authorized to consume a Failure, the Failure
meaning required by that processing must remain available. Long-term persistence is separate and belongs to Retention.

#### 4.3.6. Containment and Subsequent Processing

Failure belongs to the source and active processing boundary in which it is established.

Another authority does not inherit that Failure. It establishes its own Failure only if it independently establishes its
own failure meaning.

The current active processing boundary still completes every applicable and reachable part required by the Boundary
Processing Completion law. A Failure blocks only processing that requires the failed meaning; independent work in the
same boundary remains eligible to complete.

After the active processing boundary completes, one or more established Failures prevent entry into later normal
processing that requires the boundary to have succeeded. The machine then enters internal Failure processing.

Processing that was not reached because it required failed meaning has no separate Failure or other semantic result
merely because it was not executed.

#### 4.3.7. Boundary Completion

An active processing boundary completes successfully only when every applicable and reachable part required by the
Boundary Processing Completion law completes without establishing Failure.

If one or more Failures are established after that reachable processing has completed, the boundary completes
unsuccessfully with every established Failure preserved.

```text
active processing boundary
    ↓
all applicable and reachable processing completed
    ├─ no Failure
    │    -> successful boundary completion
    │    -> next normal processing may begin
    │
    └─ one or more Failures
         -> unsuccessful boundary completion
         -> next normal processing is not entered
         -> internal Failure processing
```

Failure membership is semantic; canonical ordering is only a deterministic representation of that membership and does
not rank Failures by importance.

This completion is why Failure remains ordinary machine flow rather than abnormal escape.

Crash is different. Crash breaks the running Contract Machine before normal boundary completion and ends this ADR's
obligation to complete or preserve the destroyed execution's remaining processing. Only material that independently
survives the Crash may later be used as Diagnostic Evidence.

### 4.4. Contract Failure

A Contract Failure originates when a Contract Authority establishes that one of its required obligations is not
satisfied.

The owning Contract remains the judge. If Admission establishes an unsuccessful judgment, Failure preserves that exact
Admission judgment rather than performing Admission again.

The Boundary Processing Completion and Containment laws apply at the active processing boundary. Other applicable and
reachable Contract, State-Machine, and realization processing in that boundary still completes when it does not require
the failed Admission meaning.

### 4.5. State-Machine Failure

A State-Machine Failure originates when the State-Machine axis cannot establish its required result.

A refused Transition is therefore a State-Machine Failure because the failed meaning concerns legal movement.

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

The refusal does not create a replacement State. The established State remains authoritative until a valid Transition
changes it.

If the State-Machine judgment succeeds but the backend cannot carry out the authorized movement, the failure belongs to
realization instead.

### 4.6. Realization Failure

A Realization Failure exists when authoritative machine meaning has already been established but the required
realization of that meaning does not complete.

```text
authoritative machine meaning
        ↓
required realization entered
        ↓
required realization does not complete
        ↓
Realization Failure
```

This prevents implementation inability from being rewritten as a Contract judgment.

If Governance establishes that an Emergency Policy World must apply and the backend cannot complete that binding
realization, the Governance decision remains authoritative. The Failure belongs to realization.

The backend may not substitute a different semantic result simply because that result is easier to realize.

If realization completes and returns material to the next Contract or State-Machine authority, that authority judges the
returned material under its own law. A later rejection is that authority's Failure, even when a realization defect is
later found to be its cause. Realization Failure is reserved for the case in which the required realization itself did
not complete at its realization boundary.

Realization Failure participates in the same Boundary Processing Completion law as other Failure sources. Other
applicable processing in the active boundary continues when it remains independently reachable without the failed
realization meaning. Processing that requires the failed realization is not entered.

### 4.7. Failure Meaning Belongs to Its Source

A source may establish more than one distinct failure meaning under its own law. For a Contract or State-Machine source,
those distinctions belong to the authority that owns the requirement, not to Failure.

If Admission distinguishes missing required material from a value that violates its condition, Admission owns that
distinction. Failure preserves the exact unsuccessful Admission judgment rather than performing Admission again.

The same rule applies outside the Contract Pipeline. State-Machine Failure preserves the exact unsuccessful
State-Machine judgment, while Realization Failure preserves the exact required realization established not to have
completed.

The Exactness and Failure Cardinality law therefore preserves those source-owned meanings exactly as they were
established. Failure neither replaces them with Failure-owned categories nor collapses them into a broader error bucket.

### 4.8. Exact Attribution

Every Failure must preserve enough meaning to identify and interpret the failure without reconstructing it from backend
evidence.

The semantic material is currently:

```text
Failure
    source
    failure meaning
    applicable context
    boundary
```

`source` identifies the exact Contract or State-Machine authority, or the exact realization point, that established the
Failure.

The failed subject is not an independent Failure coordinate when it is already determined by the canonical material of
`source`. Contract subject, State or Transition subject, and the established machine meaning assigned to a realization
point therefore remain owned by their source definitions rather than being repeated in Failure.

`failure meaning` identifies the exact unsuccessful machine meaning established by the source. For Contract and
State-Machine sources, it is the exact unsuccessful judgment. For a Realization source, it is the exact required
realization established not to have completed.

`failure meaning` does not contain the runtime value, physical cause, stack history, backend exception, or other
material used to explain why the failure occurred. Such material belongs to Diagnostic Evidence.

`applicable context` explicitly preserves the contract material that was actually applicable when the Failure was
established and is needed to interpret that Failure in its contract world. The applicable material is source-specific
rather than one universal fixed context schema. It may include Contract Version, the selected Policy or Policy World,
applicable State or Transition context, Governance selection or binding when relevant, and other contract material that
applied to that Failure.

Applicable context is fixed at Failure establishment. Later Version, Policy, State, Governance, or other machine changes
cannot rewrite the context of an already-established Failure.

The semantic model may preserve applicable context explicitly even when some of that material can be reached through
other canonical relations. A backend may physically deduplicate, intern, reference, or collapse such material when the
exact context remains deterministically recoverable. Physical storage minimization must not weaken the explicit Failure
meaning.

`boundary` identifies the exact active processing boundary in which the Failure was established. It is not the static
declaration container of `source`, and Failure does not define its own parallel scope hierarchy. The same source may
therefore establish Failure in different active boundaries.

Contract Failure, State-Machine Failure, and Realization Failure remain useful origin classifications, but `origin` is
derived from `source` rather than being an independent semantic coordinate.

A physical canonical representation may still carry an origin tag when an opaque source representation makes that
useful. Such a tag is derived representation material and must not become an independently selectable Contract value.

The exact canonical representation remains deferred.

### 4.9. Boundary and Containment

The containment law uses the machine boundary already established by the Contract, State-Machine, or Implementation axis
in which Failure occurs.

The preceding Whole-Machine analysis examined how those same three axes cooperate across Cores and pipelines. It did not
establish a separate Whole-Machine Scope contract concept, so Failure does not introduce a second scope hierarchy.

```text
Failure at one boundary
    does not imply
Failure at another boundary
```

A local Failure therefore does not become machine-wide Failure while a larger required function remains satisfied. A
boundary that is never entered has no Failure merely because earlier work ended.

### 4.10. Failure Processing Is Normal Machine Flow

Failure is an ordinary unsuccessful completion path of the active processing boundary in which it is established.

The first established Failure does not immediately end that boundary. Contract-Pipeline processing, additional
applicable Contracts such as Budget or Capacity, State-Machine processing, and required realization may coexist at the
same active boundary. Every part that remains applicable and independently reachable without requiring failed meaning
completes before the boundary result is fixed.

```text
active processing boundary
    ↓
complete all applicable and reachable processing
that does not require failed meaning
    ↓
preserve all established results
    ↓
any Failure established?
    ├─ no
    │    ↓
    │   successful boundary completion
    │    ↓
    │   next normal processing
    │
    └─ yes
         ↓
       preserve all established Failures
         ↓
       unsuccessful boundary completion
         ↓
       next normal processing is not entered
         ↓
       internal Failure processing
           ├─ Diagnostic
           │      -> developer / operator
           │
           └─ outward failure meaning required
                  ↓
              Publication
                  ↓
              Output Presentation
                  ↓
              outward failure outcome
```

Failure blocks only processing that requires the failed meaning. Processing that becomes unreachable is simply not
entered and does not acquire a separate semantic result.

Multiple Failures established in the boundary remain equally explicit Failure meaning. Failure processing does not
choose a primary Failure. All established Failures remain available to Diagnostic processing; deterministic ordering may
be used for representation or presentation without creating semantic priority.

Internal Failure processing carries the Failures established by the completed boundary without retrying failed
obligations or resuming later normal processing that required the boundary to succeed.

This processing path realizes the Boundary Processing Completion and Containment laws; it is not Failure propagation.

### 4.11. Failure Does Not Terminate Realization Resources

Failure stops the normal processing that depends on the failed meaning, not the realization resource that happened to
execute it.

A thread, worker, process, coroutine, or other backend resource may remain available for unrelated work after a Failure.
Conversely, a backend may discard a realization resource when implementation integrity requires it, but that decision is
not Failure meaning.

```text
Failure
    -> dependent normal processing stops

thread / worker / process lifetime
    -> backend realization decision
```

An exception may be used as a backend adapter for Failure when an external API requires it. Unwinding a stack or
terminating an uncaught thread is therefore a possible realization mechanism, never a Contract requirement.

### 4.12. Failure Is Not a Crash

A Failure is an established machine result produced while the Contract Machine remains able to execute its required
semantics. A crash is a physical realization event that breaks the running Contract Machine before normal machine
completion.

```text
required meaning cannot be satisfied
    +
machinery remains able to establish and process that fact
        ↓
Failure

execution machinery disappears or terminates
before normal machine completion
        ↓
Crash
```

Crash is not another Failure origin and is not a Failure variant. The crashed execution must not fabricate semantic
certainty after the machinery required to establish that certainty is gone.

Crash ends this ADR's obligation for the destroyed execution to complete the active processing boundary, finish Failure
processing, perform Publication, or preserve semantic material that did not independently survive the loss. Kontrakt
cannot be required to continue Contract Machine semantics after the Contract Machine itself has been broken by Crash.

A backend may establish a Realization Failure and then terminate or discard an unsafe realization resource while the
surrounding Contract Machine remains valid. In that case the Failure already exists; the later resource termination is
an implementation response, not a Crash of the Contract Machine and not a second Failure.

Surviving crash material such as an exit status, signal, core dump, last known location, stack snapshot, durable Failure
material, or runtime record may later become Diagnostic Evidence and may be shown to a developer or operator as a crash
diagnostic. Survival of such material does not mean that the destroyed execution completed Failure semantics.

A supervising or later execution may establish its own Failure if one of its own requirements becomes unsatisfied
because the observed execution disappeared. Any outward failure outcome produced after that point belongs to the
surviving authority's own Failure and Publication, not to the crashed execution.

### 4.13. Failure Is Not an Indeterminate Outcome

Failure requires enough authority to know that the required result did not occur as required.

Distributed realization can lose that knowledge. A remote effect may have happened even when its acknowledgement is no
longer available.

```text
outcome is known to have failed
    -> Failure may be established

outcome cannot be known
    -> Indeterminate Outcome
```

Kontrakt must not manufacture certainty to force a binary result.

The authority and canonical form of an Indeterminate Outcome are deferred. This ADR decides only that it is not Failure.

### 4.14. Failure Does Not Own Recovery

Failure states the unsuccessful machine result. It does not decide what happens afterward.

The Finality law still applies across recovery: a retry or later successful execution establishes a later result and
cannot rewrite the earlier Failure.

If a Contract-visible response follows, the Contract that owns that response must authorize it. Backend recovery remains
implementation when it has no independent Contract meaning.

### 4.15. Physical Authority Limit

Failure can be established only while enough machinery remains to establish it.

If the execution substrate disappears before a final result exists, the destroyed execution does not perform one last
Failure judgment.

```text
software can still establish the result
    -> explicit Failure may be produced

the required machinery no longer exists
    -> no fictional final Failure
```

This is the authority limit behind the distinction between Failure and Crash. Once Crash breaks the running Contract
Machine, this ADR imposes no semantic completion obligation on the destroyed execution. A later execution may reason
from durable material that survived the loss, but that later reasoning belongs to the later execution.

### 4.16. Failure, Diagnostic, and Publication Meaning

Failure states the exact failure meaning established by its source. Diagnostic Evidence explains that established
Failure.

That distinction changes how failure should be presented to developers and operators. A stack trace is execution
evidence, so shortening it does not produce a semantic Failure summary.

Because Failure already preserves its source, failure meaning, source-specific applicable contract context, and active
boundary, tooling can identify the machine failure first instead of asking the developer to reconstruct it from
execution history. When one completed boundary establishes multiple Failures, all of them must be handed to Diagnostic
processing and remain visible to the developer or operator. Diagnostic may group or order them deterministically, but it
cannot discard one merely by inventing a primary or secondary Failure.

The actual value that violated a requirement, a backend error, a physical cause, crash material, and stack history
remain Diagnostic Evidence. They can explain a Failure or an execution loss without becoming part of Failure semantic
identity.

Publication has a different authority. An established Failure does not require Publication merely to exist or to be
available to diagnostic tooling. When a service consumer or another external boundary must receive failure meaning,
however, that outward claim must be authorized by Publication.

```text
established Failure
    ├─ internal observation
    │      ↓
    │   Diagnostic Evidence / Retention
    │
    └─ outward failure meaning required
           ↓
       Publication
           ↓
       Output Presentation
```

Publication may expose the Failure meaning directly or authorize a different outward meaning that is appropriate for the
external consumer. It therefore owns the external success and failure outcome surface; Failure does not define a second
outward error catalog.

Output Presentation owns the external shape of the authorized claim, and Retention remains responsible for how
Diagnostic Evidence is kept.


---

## 5. Derived and Canonical Failure Material

### 5.1. Failure Is Not User-Authored

Failure is intrinsic machine law, not an ordinary user-selected one-dimensional Contract.

The user declares the required meaning in the Contract and State-Machine authorities that own it. Kontrakt derives
Contract and State-Machine Failure from their exact unsuccessful judgments, while the backend establishes Realization
Failure from the exact realization point whose required realization did not complete.

The user therefore does not register a second Failure name, category, or mapping for meaning that is already explicit.
Operational grouping, logging categories, severity, messages, and evidence policy belong to their own later authorities
rather than becoming Failure declarations. When users must declare which failures are visible to an external consumer,
that declaration belongs to Publication rather than to Failure.

```text
explicit machine meaning / exact realization point
        ↓
source establishes exact failure meaning
        ↓
intrinsic Failure
```

This does not make Failure implicit. Its meaning is deterministically derived from already-explicit machine material
rather than manually repeated in a second declaration.

### 5.2. Canonical Material

Canonical Failure material must preserve the meaning defined by exact attribution.

The source's failure meaning must survive canonicalization without being redeclared as a parallel Failure taxonomy. For
Contract and State-Machine sources, this preserves the exact unsuccessful judgment. For a Realization source, it
preserves the exact required realization established not to have completed. Runtime values and causes remain Diagnostic
Evidence.

`origin` is derived from `source` at the semantic level. If an opaque physical source identity requires a separate
origin tag for efficient realization, the tag must remain derived and consistent with that source.

The failed subject is derived from the canonical material referenced by `source` and is not repeated as an independent
Failure coordinate.

Applicable context explicitly preserves the source-specific contract material that applied when Failure was established.
It is not a universal fixed schema. Version, selected Policy or Policy World, State or Transition context, Governance
selection or binding, and other applicable Contract material participate when they were actually part of that Failure's
contract world. That context is frozen with the Failure and cannot be rewritten by later machine changes.

The semantic presence of applicable context does not require physical duplication. A backend may use canonical
references, interning, compact identities, or other deterministic representation to share material while keeping the
exact source-specific context recoverable.

`boundary` remains semantically distinct from `source` because it identifies the active processing boundary in which
Failure was established rather than the static source definition.

Canonical Failure identity must remain stable independently of discovery order, allocation address, worker scheduling,
runtime registration order, or cache population order. A backend may assign compact physical identifiers, including
interned or table-local identifiers, only as deterministic realizations of canonical Failure meaning.

Compile-time Failure-site material and invocation-specific runtime material must remain separable. Static source and
failure meaning may be frozen, cached, interned, or lowered independently of active boundary identity and runtime
applicable context when the backend architecture permits it.

Backend behavior cannot participate in canonical Failure identity.

### 5.3. Backend Error Vocabulary Is Not Contract Authority

A host-language exception does not define Failure.

Replacing exception-based control flow with another realization must leave Failure meaning unchanged.

A backend error signal may help establish a Realization Failure, but the signal itself does not become the Contract
identity.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Meaning

The Failure Contract requires source-established failure meaning to become explicit Failure and requires that
established Failure to remain exact, final, preserved, contained, and usable throughout valid internal Failure
processing.

Failure is a normal Contract Machine outcome and an explicit unsuccessful active-boundary completion.

It does not take over the Contract or State-Machine judgment that produced a failure meaning, rejudge realization
correctness, prescribe termination of a realization resource, aggregate or rank established Failures, create results for
processing that was not reached, or represent an unknown outcome as definite Failure.

### 6.2. Realization

Kontrakt's backend does not discover Contract Failure from a raw boolean, sentinel, exception class, or another backend
convention. The compiler preserves each 1D Contract's exact judgment meaning in canonical IR material, and Failure
lowering is derived from that canonical representation.

The mirrored realization plan provides the same advantage for implementation failure. The backend knows which
established Contract meaning a realization point is realizing, so a Realization Failure does not need to be
reconstructed from a later exception path.

That knowledge gives the compiler a direct Failure-processing path without making any specific IR shape Contract
Authority. Lowering must preserve Boundary Processing Completion across the Contract, State-Machine, and Realization
axes: establishing one Failure cannot short-circuit other applicable and independently reachable machine processing in
the same active boundary.

```text
canonical IR material for active-boundary processing
    -> execute applicable and reachable machine work
    -> establish exact results and Failures
    -> preserve complete Failure membership
    -> active-boundary completion
    -> internal Failure processing when any Failure exists
    -> Diagnostic Evidence only when required
```

The runtime representation therefore does not need to be a Failure object. A compact identity can remain sufficient for
each semantic Failure, while runtime values, causes, stack history, and other explanatory material are materialized
separately only when Diagnostic Evidence requires them.

Failure establishment must not require eager object allocation, message construction, stack capture, or rich Diagnostic
material. Those costs may be introduced only by the later authority that needs them.

An adapter can still translate the Failure into an exception when an external API requires one.

A direct Failure-processing path is therefore the natural lowering for known machine Failure. The backend does not need
to unwind to an uncaught exception or terminate the executing thread merely because a Contract or State-Machine judgment
was unsuccessful.

Failure does not require arbitrary realization that has already been entered to be forcibly revoked. Once control
returns to a Kontrakt-owned boundary, every later dependent processing step can still be denied entry. Effects that
realization has already produced outside Kontrakt authority cannot be retroactively cancelled by Failure; any stronger
control over outward effects must come from the Contract boundaries that own those effects.

This separation also permits a cheaper realization than ordinary exception machinery when the backend can use direct
control flow instead. That is a consequence of explicit Failure meaning, not the reason Failure exists.

Realization itself may still fail. When the backend can establish that fact, the resulting Failure must remain
attributed to the Contract meaning that was being realized. If the realization machinery disappears before that result
can be established, the event is Crash rather than a fabricated Realization Failure.

### 6.3. Recovery

Recovery remains outside Failure authority.

If recovery starts a new Contract interaction, that interaction begins under its own applicable Contracts. Later success
does not rewrite the earlier Failure.

### 6.4. Diagnostics

Diagnostic machinery may observe Failure without defining it.

A stack trace, the actual value that violated a requirement, an underlying backend error, or surviving crash material
can become Diagnostic Evidence when useful, but Failure identity must remain semantically complete without them.

Diagnostic observation does not require Publication. Publication is required only when Failure meaning, or a claim
derived from it, is authorized to cross an outward machine boundary.

### 6.5. Backend Architecture Constraints

This ADR does not choose the concrete Failure IR, table layout, identifier encoding, cache format, slab layout, or
generated JVM control-flow shape. Those decisions belong to the compiler and backend design after the new frontend and
IR architecture are established.

Any such design must preserve the following properties.

Failure meaning, Boundary Processing Completion, and Failure-processing semantics must remain lowerable into
deterministic immutable compiler products. The implementation cannot require mutable runtime registries, allocation
identity, discovery order, or worker arrival order to establish semantic Failure identity or to decide which applicable
machine processing in an active boundary is completed.

For Contract Machine processing, a backend optimization may eliminate only work proven unreachable or semantically
unnecessary under the declared machine structure. It cannot introduce fail-fast behavior that suppresses another
applicable and independently reachable Contract, State-Machine, or Realization operation or changes which Failures are
established.

Kontrakt's own compiler/backend machinery may fail fast when an internal implementation failure makes further internal
work invalid or unnecessary. That internal fail-fast behavior is an implementation policy, not Failure Contract
semantics, and it must not be confused with Kontrakt code that physically evaluates user Contract Machine obligations
such as Budget or Capacity.

Internal fail-fast must itself be deterministic where its result is observable, and it must never suppress machine
processing merely because that processing happens to be physically executed by Kontrakt code. If ordering-sensitive
obligations such as Budget or Capacity share accounting or admission state, or if State-Machine or realization work has
explicit dependencies, their semantic order comes from the canonical machine plan rather than thread or worker
scheduling.

Failure processing must expose the semantic dependencies required to determine a Failure site. Hidden implementation
dependencies cannot participate in Failure meaning. This allows later incremental compilation to invalidate and
recompute affected Failure products without changing Failure semantics or forcing unrelated products to become
semantically dependent.

Caching is an implementation reuse mechanism, not Failure authority. A cached Failure-related compiler product may be
reused only when its authoritative inputs and dependencies remain valid; cache presence cannot establish a Failure by
itself.

The representation must permit separation between stable compile-time Failure-site material and invocation-specific
runtime material. It must also permit Failure semantics to remain separate from Diagnostic Evidence and Diagnostic
Presentation so that changes to explanatory or presentation material do not redefine Failure identity.

Kontrakt's existing deterministic canonicalization, frozen material, table-oriented lowering, compact identities,
primitive-oriented layouts, and compiler caches are valid realization directions, but none of them is made Contract
Authority by this ADR. Future IR forms and backends may realize the same requirements differently.

V1 must not require implementation choices that would make V2 incremental compilation semantically different from a full
compilation. The introduction of incremental reuse, persistent compiler artifacts, or parallel recomputation must not
require a change to Failure meaning.

---

## 7. Verification, Determinism, and Incremental Extensibility

Failure processing is deterministic.

Verification must enforce the Failure Contract obligations as well as the physical lowering. Failure meaning represented
by authoritative or canonical machine material must not disappear without Failure.

The compiler and backend must preserve source-established Failure cardinality. Distinct authoritative unsuccessful
Contract or State-Machine judgments cannot be collapsed into one Failure identity, and one such judgment cannot be split
into several Failure-owned semantic identities. Distinct required realizations established not to have completed
likewise remain distinct Realization Failures.

Within one active processing boundary, every applicable Contract, State-Machine, or Realization operation that remains
valid and reachable without requiring failed meaning must be completed and its established result preserved. A backend
cannot use the first discovered Failure, worker completion order, or another scheduling event to suppress independently
reachable machine processing.

Failure membership for the completed boundary must therefore be deterministic. Parallel execution may change when
independent work finishes, but it cannot change which required machine processing completes or which Failures are
established. Ordering-sensitive work must follow its canonical semantic order.

Canonical ordering of multiple Failures must also be deterministic when representation or Diagnostic presentation
requires an order, but that order cannot become semantic priority. Every established Failure remains equally explicit
and available to Failure and Diagnostic processing.

Processing that becomes unreachable because it requires failed meaning must not establish a fabricated Failure or
another semantic result merely because it was not entered.

This requirement does not force Kontrakt's unrelated internal compiler/backend work to collect every possible
implementation failure. Internal machinery may fail fast under the backend architecture constraints above, provided that
doing so cannot change any Contract Machine result that the machine is required to establish.

Once established, a Failure cannot be rewritten as Success, erased by later recovery, or reassigned to another source.
Every valid representation and handoff that carries the Failure must preserve its exact semantic meaning.

For the same authoritative Contract, State-Machine, realization, active boundary, and applicable contract context, every
valid compiler execution must produce the same complete required machine results, the same Failure membership, and the
same subsequent-processing semantics. Where an ordered representation is required, it must also produce the same
canonical order. Compilation order, parallel scheduling, worker arrival order, memory address, hash-table iteration,
runtime registration order, and cache population order cannot participate in that result.

This law extends to future compilation modes. A full clean compilation, an incremental compilation, a cache hit, a cache
miss followed by recomputation, and valid single-threaded or parallel execution must be semantically equivalent for
Failure. Incremental compilation is an implementation optimization, not a second Failure semantics.

The compiler must reject Failure meaning that depends on backend-only coordinates.

A Failure source must resolve to the exact Contract or State-Machine authority, or to the exact realization point, from
which the failure meaning was established. Ambiguous attribution is invalid rather than resolved by implementation
order.

The failed subject remains derivable from `source` rather than becoming a second Failure coordinate, and a static
declaration boundary must not be confused with the active processing `boundary`. Applicable contract context is
intentionally explicit semantic material and may be physically shared or referenced rather than semantically omitted.

`origin` cannot vary independently from `source`. Any physical origin tag must agree with the source from which it is
derived.

Canonical or lowered physical identifiers cannot acquire semantic authority from assignment order. If a backend uses an
HID, interned integer, table index, slab coordinate, or another compact identifier, that identifier must be
deterministically derived from or bound to canonical Failure meaning.

A Realization Failure cannot be rewritten as a Contract Failure for presentation convenience.

Implementation topology cannot move a Failure to a different machine boundary, and Failure semantics cannot require
termination of a thread, worker, process, coroutine, or other realization resource.

The same source, failure meaning, source-specific applicable context, and active boundary must establish the same
contract-visible Failure regardless of the Diagnostic Evidence later attached to it.

Failure-related compiler products must be able to expose the dependencies that determine them. Future incremental
invalidation must be able to distinguish changed authoritative inputs from unrelated material rather than treating
hidden global compiler state as a semantic dependency.

Cached compiler products do not establish Failure authority. Reuse is valid only when the authoritative material and
dependency relation that produced the cached product remain valid.

Semantic Failure material, Diagnostic Evidence, and Diagnostic Presentation must remain independently invalidatable
where their dependencies differ. A diagnostic wording or formatting change cannot require a different Failure identity
merely because both are emitted from the same compiler run.

If diagnostics have a contract-visible or tool-visible order, parallel execution cannot make that order
nondeterministic. The ordering rule must come from deterministic machine or compiler coordinates rather than worker
completion order.

Any persisted compiler cache, serialized IR, or Failure-related artifact format is implementation material. Its schema
compatibility and invalidation versioning are separate from Contract Version and cannot become Contract meaning.

A crash cannot be rewritten as Failure solely because execution stopped. Verification cannot require a destroyed
Contract Machine to complete its active boundary, finish Failure processing, preserve unsafely lost semantic material,
produce Publication, or fabricate a final Failure. It also cannot turn an indeterminate outcome into certainty.

Any outward failure claim must be authorized by Publication. Failure itself cannot acquire Publication authority merely
because the failed meaning is important to an external consumer.

Because Failure is intrinsic and derived, a compile-time rejection of invalid Contract material is not a runtime Failure
of the machine being defined.

---

## 8. Deferred Decisions

The following questions remain open:

1. What canonical representation should encode `source`, `failure meaning`, source-specific `applicable context`, and
   the active `boundary` while permitting physical deduplication without weakening explicit Failure meaning?
2. How should canonical Failure material identify the active boundary of each source axis without confusing it with the
   static source definition or creating a new shared boundary ontology?
3. What authority owns an Indeterminate Outcome?
4. How will Publication declare the outward success and failure outcome surface without duplicating internal Failure
   meaning?
5. What backend material is sufficient to establish Realization Failure before execution loss, without promoting
   implementation errors into Contract meaning?
6. What surviving material may later describe Crash as Diagnostic Evidence without inventing a Failure for the destroyed
   execution?
7. Which concrete Failure IR, frozen representation, compact identity, table or slab layout, cache key, and
   Failure-processing form best fit the redesigned frontend and IR while preserving the constraints in this ADR?
8. Which earlier ADRs must change once this model is accepted?

These questions do not reopen the separation between Failure meaning and realization mechanism, the decision that
Failure itself is intrinsic rather than user-authored, or the requirement that Failure processing remain deterministic
and incrementally extensible.

---

## 9. Consequences

### Positive

Failure becomes explicit machine meaning rather than an inference from backend behavior.

The Failure Contract now has explicit obligations: source-established failure meaning must become explicit Failure; its
exact meaning and cardinality are preserved; every applicable and independently reachable Contract, State-Machine, and
Realization operation in the active processing boundary completes and preserves its established result; unreachable
processing establishes no fabricated result; multiple Failures have no semantic priority; established Failure is final
during valid machine execution; and every valid processing path or lowering must preserve that meaning.

Its exact source remains attributable after lowering, while its origin and failed subject can be derived without
becoming additional semantic coordinates. The active boundary remains distinct from the static source, and local failure
stays local until another boundary establishes its own Failure.

Failure becomes an ordinary explicit machine processing path rather than a requirement to unwind or terminate the
executing realization resource.

Crash is separated from established Failure, so abrupt execution loss cannot fabricate a semantic result.

Diagnostic tooling can present semantic Failure before runtime evidence, while Publication independently controls which
failure meaning may become an outward claim.

Because the compiler already knows Failure meaning, unsuccessful paths can be lowered without mandatory exception
machinery.

The Failure model remains compatible with frozen, table-oriented, compact, primitive-friendly, and cached compiler
products without requiring any one of those physical forms.

V1 can remain a full-compilation implementation while preserving the stable identity, explicit dependency, and
immutable-product boundaries needed for V2 incremental compilation.

### Negative

Failure no longer fits the earlier model of an ordinary user-authored one-dimensional Contract-Pipeline slot, so
documents that assume explicit Failure authoring must be revised.

The canonical boundary vocabulary still has to be resolved across the three machine axes.

The concrete Failure compiler architecture must be designed together with the new frontend and IR rather than fixed
independently in this ADR.

Distributed realization also needs a separate treatment for outcomes that cannot honestly be classified as success or
Failure, and crash evidence needs a representation outside Failure identity.

### Neutral

Failure has no separate user declaration merely because it is one Contract kind.

This ADR establishes that an outward failure claim must pass Publication, but the Publication syntax and external
success/failure outcome vocabulary are decided by the Publication Contract ADR.

Existing backend mechanisms remain valid only when they realize this Contract without owning its meaning.