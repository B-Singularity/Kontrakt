# ADR-0057: Failure Contract, Explicit Machine Failure, Attribution, and Realization Boundary

## Status

Proposed

## Date

2026-08-16

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
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

Failure is ordinary Contract Machine flow. Within a governed Contract or State-Machine judgment boundary, discovering
one Failure does not abandon other authoritative judgments that are still valid and reachable without requiring failed
meaning. The governed boundary completes those judgments, preserves every established judgment result, and only then
decides whether normal processing may proceed. If one or more Failures were established, the next normal 1D processing
is not entered.

This rule follows Contract Authority rather than implementation location. A Budget, Capacity, Admission, Invariant, or
other governed judgment remains subject to this rule even when Kontrakt backend code performs the physical evaluation.
It does not require unrelated compiler machinery or arbitrary realization work to continue merely to collect more
implementation failures.

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
containment, subsequent-processing semantics, or observable ordering.

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
complete. Failure preserves the resulting unsuccessful machine meaning.

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

When an authority establishes that its required meaning was not satisfied, Failure must be established explicitly.

An authoritative unsuccessful judgment cannot disappear into a log entry, exception path, sentinel value, `null`,
backend status, or another implementation behavior without remaining explicit Failure meaning.

The establishment of one Failure does not by itself complete the current governed judgment boundary. Boundary completion
follows the governed judgment-completion law below.

#### 4.3.2. Exactness and Judgment Cardinality

Failure preserves the exact unsuccessful judgment established by its source.

Each authoritative unsuccessful judgment establishes exactly one semantic Failure. Multiple distinct authoritative
unsuccessful judgments remain distinct Failures, including when they are established within the same governed judgment
boundary.

Failure does not create a second taxonomy, combine distinct unsuccessful judgments into a vague error bucket, or split
one unsuccessful judgment into Failure-owned subcategories.

The owning authority determines each judgment. Failure preserves the identity and cardinality of the judgments that were
actually established.

#### 4.3.3. Governed Boundary Judgment Completion

A governed Contract or State-Machine judgment boundary must complete every authoritative judgment that is declared for
that boundary and remains valid and reachable without requiring meaning that has already failed.

The rule follows authority, not execution location. A judgment does not become Kontrakt-internal merely because
generated or backend code evaluates it. Budget, Capacity, Admission, Invariant, Policy-controlled judgment, and other
Contract-visible judgments remain governed machine judgments.

Discovering one Failure cannot suppress another such judgment merely because the first Failure was discovered earlier.

```text
current governed judgment boundary
    ↓
complete every valid and reachable authoritative judgment
    ↓
preserve every established judgment result
    ↓
deterministically fix all established Failures
    ├─ none
    │    -> next declared 1D processing
    │
    └─ one or more
         -> next normal 1D processing is not entered
         -> internal Failure processing
```

Judgments that require failed meaning and therefore are not reachable are not executed and do not produce speculative
Failures.

The complete membership and canonical order of the Failures established in the governed boundary must not depend on
discovery order, worker completion order, parallel scheduling, or another implementation accident.

Where governed judgments share accounting, admission, or another ordering-sensitive resource, the semantic evaluation
order must come from the canonical governed plan rather than runtime scheduling. Where judgments are independent, a
backend may evaluate them in parallel only if the same complete result set and canonical order are preserved.

The Failures established in one governed boundary do not form a new aggregate Failure identity. Every Failure retains
its own source, subject, unsuccessful judgment, applicable context, and boundary.

This law does not require Kontrakt's own compiler/backend machinery to continue unrelated internal work after an
internal implementation failure, and it does not require arbitrary realization operations to continue after Realization
Failure. Those are implementation execution questions outside the governed judgment set.

#### 4.3.4. Finality

Once Failure is established, that established machine meaning is final.

A later retry, recovery, replacement execution, or successful attempt cannot erase the earlier Failure, rewrite it as
Success, or reassign it to another source. A later attempt establishes its own later result.

Finality concerns established machine meaning. It does not require a particular immutable runtime object representation.

#### 4.3.5. Preservation and Availability

The exact meaning of an established Failure must survive every valid representation and handoff that carries that
Failure.

Canonicalization, lowering, compaction, interning, caching, backend-specific representation, Diagnostic handoff,
Publication handoff, or other valid transformation may change physical form but cannot weaken or reconstruct Failure
meaning from less authoritative implementation evidence.

While internal Failure processing, Diagnostic processing, or Publication is authorized to consume a Failure, the Failure
meaning required by that processing must remain available. Long-term persistence is separate and belongs to Retention.

#### 4.3.6. Containment and Subsequent Processing

Failure belongs to the source and boundary in which it is established.

Another authority does not inherit that Failure. It establishes its own Failure only if it independently establishes
that its own required meaning was not satisfied.

The current governed judgment boundary still completes the judgments required by the Governed Governed Boundary Judgment
Completion law. After that boundary is complete, no later declared processing whose validity depends on the failed
boundary may be entered while that processing remains under Kontrakt control.

For a 1D Contract boundary with one or more established Failures, the next normal 1D processing is therefore not
entered; the machine moves into internal Failure processing after the current governed boundary has completed its
remaining valid and reachable judgments.

#### 4.3.7. Boundary Completion

A governed judgment boundary completes successfully only when its required judgments complete without establishing
Failure.

If one or more Failures are established after every judgment required by the Governed Governed Boundary Judgment
Completion law has completed, the governed boundary completes unsuccessfully with those exact Failures preserved.

```text
governed judgment boundary
    ↓
required judgments completed
    ├─ no Failure
    │    -> successful boundary completion
    │
    └─ one or more Failures
         -> unsuccessful boundary completion
         -> internal Failure processing
```

This completion is why Failure remains ordinary machine flow rather than abnormal escape.

Crash is different because execution may disappear before any final authoritative machine result is established. An
Indeterminate Outcome is also different because the relevant authority cannot establish whether the required result
succeeded or failed.

### 4.4. Contract Failure

A Contract Failure originates when a Contract Authority establishes that one of its required obligations is not
satisfied.

The owning Contract remains the judge. If Admission determines that required material is absent, Failure preserves that
Admission result rather than performing another Admission judgment.

The Governed Governed Boundary Judgment Completion and Containment laws apply at this Contract boundary. Other valid and
reachable governed judgments in the same boundary still complete, while another admitted flow remains governed by its
own Contracts.

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

The Governed Governed Boundary Judgment Completion law does not require other arbitrary realization operations to
continue after this Failure. Any ordering or independence among realization operations belongs to realization design,
not to Failure judgment collection.

### 4.7. Failure Kind Belongs to the Owning Authority

A richer Contract may distinguish several unsuccessful meanings for the same authority. Those distinctions belong to the
authority that owns the requirement, not to Failure.

If Admission distinguishes missing required material from a value that violates its condition, Admission owns that
distinction. Failure preserves the exact unsuccessful judgment that Admission established.

The same rule applies outside the Contract Pipeline. State-Machine Failure preserves the unsuccessful State-Machine
judgment, while Realization Failure remains tied to the exact realization boundary whose required realization did not
complete.

The Exactness and Judgment Cardinality law therefore preserves those distinctions exactly as the owning authority
established them. Failure neither replaces them with Failure-owned categories nor collapses them into a broader error
bucket.

### 4.8. Exact Attribution

Every Failure Result must preserve enough meaning to identify the failure without reconstructing it from backend
evidence.

The semantic material is currently:

```text
Failure Result
    source
    subject
    unsuccessful judgment
    applicable context
    boundary
```

`source` identifies the exact Contract or State-Machine authority, or the exact realization point, that established the
unsuccessful result. `subject` identifies what that result was about.

The `unsuccessful judgment` is the semantic identity of the unsuccessful result. It does not contain the runtime value,
physical cause, stack history, or other material used to explain why that result occurred. Such material belongs to
Diagnostic Evidence.

`applicable context` retains only the Contract material required to interpret the Failure correctly.

`boundary` identifies the already-established machine boundary in which the Failure exists. Failure does not define its
own parallel scope hierarchy.

Contract Failure, State-Machine Failure, and Realization Failure remain useful origin classifications, but `origin` is
derived from `source` rather than being an independent semantic coordinate.

A physical canonical representation may still carry an origin tag when an opaque source representation makes that
useful. Such a tag is derived representation material and must not become an independently selectable Contract value.

The exact canonical representation remains deferred. These semantic coordinates may be collapsed only when one can be
derived from another without losing meaning.

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

Failure is an ordinary unsuccessful completion path of the governed machine boundary in which it is established.

For Contract and State-Machine judgment boundaries, the first established Failure does not immediately end the current
governed boundary. The machine first completes every other authoritative judgment that remains valid and reachable
without requiring failed meaning, and preserves every result that those judgments establish.

```text
current governed judgment boundary
    ↓
complete all valid and reachable authoritative judgments
    ↓
preserve all established judgment results
    ↓
any Failure established?
    ├─ no
    │    ↓
    │   next declared 1D processing
    │
    └─ yes
         ↓
       fix all established Failures deterministically
         ↓
       next normal 1D processing is not entered
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

A judgment that requires meaning already established as failed is not reachable and is not executed merely to
manufacture another Failure.

For a 1D Contract Failure, this means the current governed 1D boundary completes all remaining valid and reachable
judgments before the Contract Pipeline decides whether to enter the next declared 1D processing. Budget and Capacity
follow this rule even though their physical evaluation is performed by Kontrakt-owned machinery, because their judgments
remain Contract-visible machine meaning.

Realization Failure does not impose the same collect-all rule on arbitrary implementation work. Once required
realization does not complete, later processing that depends on that realization is not entered; whether unrelated
realization work continues is a backend or realization-design decision.

Internal Failure processing carries every Failure established in the completed governed boundary without retrying the
failed obligations or resuming normal processing that required the boundary to succeed.

This processing path realizes the Governed Governed Boundary Judgment Completion and Containment laws; it is not Failure
propagation.

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

A Failure is an established machine result. A crash is a physical realization event in which execution is abruptly lost
or terminated before the machine can complete the processing needed to establish a final result.

```text
required meaning cannot be satisfied
    +
machinery can still establish that fact
        ↓
Failure

execution machinery disappears or terminates
before the result can be established
        ↓
Crash
```

Crash is not another Failure origin and is not a Failure Result variant. The crashed execution must not fabricate
semantic certainty after the machinery required to establish that certainty is gone.

A backend may establish a Realization Failure and then terminate or discard an unsafe realization resource. In that case
the Failure already exists; the later termination is an implementation response, not a second Failure.

Crash has no Failure-processing or Publication path in the destroyed execution. Surviving crash material such as an exit
status, signal, core dump, last known location, stack snapshot, or runtime record may later become Diagnostic Evidence
and may be shown to a developer or operator as a crash diagnostic.

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

This is the authority limit behind the distinction between Failure and Crash. A later execution may reason from durable
material that survived the loss, but that later reasoning belongs to the later execution.

### 4.16. Failure, Diagnostic, and Publication Meaning

Failure says what failed. Diagnostic Evidence explains the established result.

That distinction changes how failure should be presented to developers and operators. A stack trace is execution
evidence, so shortening it does not produce a semantic Failure summary.

Because Failure already preserves its source, subject, unsuccessful judgment, applicable context, and boundary, tooling
can identify the machine failure first instead of asking the developer to reconstruct it from execution history.

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
explicit required meaning
        ↓
exact unsuccessful result
        ↓
intrinsic Failure
```

This does not make Failure implicit. Its meaning is deterministically derived from already-explicit machine material
rather than manually repeated in a second declaration.

### 5.2. Canonical Material

Canonical Failure material must preserve the meaning defined by exact attribution.

The source's unsuccessful judgment must survive canonicalization without being redeclared as a parallel Failure
taxonomy. The judgment coordinate identifies the semantic unsuccessful result only; runtime values and causes remain
Diagnostic Evidence.

`origin` is derived from `source` at the semantic level. If an opaque physical source identity requires a separate
origin tag for efficient realization, the tag must remain derived and consistent with that source.

`subject` and `boundary` remain separate semantic coordinates unless later canonical design proves that one can be
derived from the other without losing meaning.

Applicable context participates only where it changes interpretation of the Failure.

Canonical Failure identity must remain stable independently of discovery order, allocation address, worker scheduling,
runtime registration order, or cache population order. A backend may assign compact physical identifiers, including
interned or table-local identifiers, only as deterministic realizations of canonical Failure meaning.

Compile-time Failure-site material and invocation-specific runtime material must remain separable. Static meaning may be
frozen, cached, interned, or lowered independently of concrete runtime subjects and observations when the backend
architecture permits it.

Backend behavior cannot participate in canonical Failure identity.

### 5.3. Backend Error Vocabulary Is Not Contract Authority

A host-language exception does not define Failure.

Replacing exception-based control flow with another realization must leave Failure meaning unchanged.

A backend error signal may help establish a Realization Failure, but the signal itself does not become the Contract
identity.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Meaning

The Failure Contract requires an authoritative unsuccessful judgment to become explicit Failure and requires that
established Failure to remain exact, final, preserved, contained, and usable throughout valid internal Failure
processing.

Failure is a normal Contract Machine outcome and an explicit unsuccessful boundary completion.

It does not take over the judgment that produced that result, prescribe termination of the realization resource,
aggregate or split the owning authority's judgments, or represent an unknown outcome as definite Failure.

### 6.2. Realization

Kontrakt's backend does not discover Contract Failure from a raw boolean, sentinel, exception class, or another backend
convention. The compiler preserves each 1D Contract's exact judgment meaning in canonical IR material, and Failure
lowering is derived from that canonical representation.

The mirrored realization plan provides the same advantage for implementation failure. The backend knows which
established Contract meaning a realization point is realizing, so a Realization Failure does not need to be
reconstructed from a later exception path.

That knowledge gives the compiler a direct Failure-processing path without making any specific IR shape Contract
Authority. For governed Contract and State-Machine judgment boundaries, lowering must preserve Governed Boundary
Judgment Completion: establishing one Failure cannot short-circuit another valid and reachable authoritative judgment in
the same governed boundary.

```text
canonical IR material for governed boundary judgments
    -> establish exact judgment results
    -> deterministically retain every established Failure
    -> governed boundary completion
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

Failure meaning, Governed Boundary Judgment Completion, and Failure-processing semantics must remain lowerable into
deterministic immutable compiler products. The implementation cannot require mutable runtime registries, allocation
identity, discovery order, or worker arrival order to establish semantic Failure identity or to decide which governed
authoritative judgments are completed.

For governed Contract and State-Machine judgments, a backend optimization may eliminate only judgments proven
unreachable or semantically unnecessary under the declared machine structure. It cannot introduce fail-fast behavior
that suppresses another valid and reachable authoritative judgment or changes which Failures are established.

Kontrakt's own compiler/backend machinery may fail fast when an internal implementation failure makes further internal
work invalid or unnecessary and no additional governed judgment is required to be established. That internal fail-fast
behavior is an implementation policy, not Failure Contract semantics.

Internal fail-fast must itself be deterministic where its result is observable, and it must never suppress a governed
Contract or State-Machine judgment merely because that judgment happens to be physically evaluated by Kontrakt code. If
ordering-sensitive governed judgments such as Budget or Capacity share accounting or admission state, their semantic
order comes from the canonical governed plan rather than thread or worker scheduling.

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

Verification must enforce the Failure Contract obligations as well as the physical lowering. An authoritative
unsuccessful judgment represented by canonical compiler material must not disappear without Failure.

The compiler and backend must preserve the owning authority's judgment cardinality. Distinct authoritative unsuccessful
judgments cannot be collapsed into one Failure identity, and one authoritative unsuccessful judgment cannot be split
into several Failure-owned semantic identities.

Within one governed Contract or State-Machine judgment boundary, every authoritative judgment that remains valid and
reachable without requiring failed meaning must be completed and its established result preserved. A backend cannot use
the first discovered Failure, worker completion order, or another scheduling event to suppress such a judgment.

The membership and canonical order of all Failures established in that governed boundary must therefore be
deterministic. Parallel execution may change when an independent judgment finishes, but it cannot change which required
governed judgments are completed or which Failures are established. Ordering-sensitive governed judgments must follow
their canonical semantic order.

This requirement does not force Kontrakt's unrelated internal compiler/backend work to collect every possible
implementation failure. Internal machinery may fail fast under the backend architecture constraints above, provided that
doing so cannot change any governed Contract or State-Machine result that the machine is required to establish.

Once established, a Failure cannot be rewritten as Success, erased by later recovery, or reassigned to another source.
Every valid representation and handoff that carries the Failure must preserve its exact semantic meaning.

For the same authoritative Contract, State-Machine, realization, and applicable context material, every valid compiler
execution must produce the same complete governed judgment results, the same Failure membership and canonical order, and
the same subsequent-processing semantics. Compilation order, parallel scheduling, worker arrival order, memory address,
hash-table iteration, runtime registration order, and cache population order cannot participate in that result.

This law extends to future compilation modes. A full clean compilation, an incremental compilation, a cache hit, a cache
miss followed by recomputation, and valid single-threaded or parallel execution must be semantically equivalent for
Failure. Incremental compilation is an implementation optimization, not a second Failure semantics.

The compiler must reject Failure meaning that depends on backend-only coordinates.

A Failure source must resolve to the exact Contract or State-Machine authority, or to the exact realization point, from
which the unsuccessful result was established. Ambiguous attribution is invalid rather than resolved by implementation
order.

`origin` cannot vary independently from `source`. Any physical origin tag must agree with the source from which it is
derived.

Canonical or lowered physical identifiers cannot acquire semantic authority from assignment order. If a backend uses an
HID, interned integer, table index, slab coordinate, or another compact identifier, that identifier must be
deterministically derived from or bound to canonical Failure meaning.

A Realization Failure cannot be rewritten as a Contract Failure for presentation convenience.

Implementation topology cannot move a Failure to a different machine boundary, and Failure semantics cannot require
termination of a thread, worker, process, coroutine, or other realization resource.

The same source, subject, unsuccessful judgment, applicable context, and boundary must establish the same
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
execution to produce a final Failure, and it cannot turn an indeterminate outcome into certainty.

Any outward failure claim must be authorized by Publication. Failure itself cannot acquire Publication authority merely
because the failed meaning is important to an external consumer.

Because Failure is intrinsic and derived, a compile-time rejection of invalid Contract material is not a runtime Failure
Result of the machine being defined.

---

## 8. Deferred Decisions

The following questions remain open:

1. What canonical representation should encode `source`, `subject`, `unsuccessful judgment`, `applicable context`, and
   `boundary` without retaining derivable material redundantly?
2. How should canonical Failure material represent the existing boundary of each source axis without creating a new
   shared boundary ontology?
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

The Failure Contract now has explicit obligations: authoritative non-satisfaction must establish Failure; the owning
judgment's exact meaning and cardinality are preserved; every valid and reachable governed judgment in the current
Contract or State-Machine boundary is completed and its result preserved; established Failure is final; and every valid
processing path or lowering must preserve that meaning.

Its exact source remains attributable after lowering, while its origin can be derived without becoming a second semantic
coordinate. Local failure stays local until another boundary establishes its own Failure.

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