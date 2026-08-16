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

established machine meaning cannot be realized correctly
    -> Realization Failure
```

These failures arise at different machine boundaries, but they express the same machine-level fact: required meaning
could not continue as required.

Failure is nevertheless an ordinary machine outcome. An unsuccessful judgment does not escape the Contract Machine as
abnormal control flow. It establishes Failure, ends the continuation that depended on the failed meaning, and enters an
explicit Failure continuation.

That makes the earlier placement of Failure as an ordinary one-dimensional pipeline slot too narrow. ADR-0057 therefore
treats Failure as one Contract kind that crosses Contract, State-Machine, and realization boundaries while preserving
the exact source that established each failure.

---

## 2. Problem

Software commonly leaves Failure meaning implicit behind abnormal control flow.

An exception exposes an unsuccessful path, but it does not state the exact machine Failure. The failed obligation and
the source that established the unsuccessful result still have to be recovered from implementation context. If no
handler accepts that control transfer, the implementation may unwind out of its execution entry and terminate a thread
or another realization resource, but that lifetime behavior is not the meaning of Failure.

A stack trace does not solve that problem. It may expose thousands of frames while still leaving the developer to infer
which machine obligation actually failed. More execution history is not a clearer Failure statement, and capturing that
call history also adds work to the failure path.

Failure must remove that semantic reconstruction. It does not need to contain every runtime value or physical cause that
explains the result; those belong to Diagnostic Evidence.

Applications often compensate by giving exception hierarchies domain-like names and interpreting them through
centralized interception. In failure handling, AOP becomes a workaround for meaning that was never explicit at the
failure boundary. This repeats the same implementation-leakage problem seen when proxy structure is allowed to stand in
for Contract meaning.

Kontrakt does not need that reconstruction because the failing authority already knows what it was judging or realizing.

```text
authoritative unsuccessful result
        ↓
explicit Failure
        ↓
replaceable realization
```

Making Failure explicit must not transfer judgment authority into Failure itself. Admission still decides Admission. The
State Machine still decides legal movement. Realization remains responsible only for whether already-established meaning
can be carried out correctly.

The resulting Failure must also remain local to the boundary that actually failed. Another boundary has its own Failure
only when its own required meaning can no longer be satisfied.

The same discipline applies when the outcome cannot be known. An indeterminate result cannot be called Failure merely
because the local execution needs a terminal branch.

---

## 3. Decision Drivers

Failure must remain explicit regardless of how the backend realizes it. Its meaning therefore has to exist before any
implementation mechanism is chosen.

The authority that owns an obligation must also own the judgment that it was not satisfied. Failure records that
unsuccessful result without judging the obligation again.

A Failure must identify the exact source and meaning that failed. This attribution cannot depend on later inspection of
implementation behavior.

Failure belongs to the machine boundary in which it is established. Failure does not define a second scope system over
the Contract Pipeline, State-Machine axis, or Implementation Pipeline. Another boundary has its own Failure only when
its own required meaning can no longer be satisfied.

Failure is part of ordinary Contract Machine control flow. It stops only the continuation whose required meaning can no
longer be satisfied; it does not require termination of a thread, worker, process, coroutine, or any other realization
resource.

The cause of a Failure does not define the Failure itself, and the response that follows it is governed separately. A
crash is also separate: abrupt loss of realization execution does not become Failure merely because execution stopped.

An established Failure may remain internal, or it may need to become an outward machine claim. Failure does not own that
exposure. Publication decides whether Failure meaning may cross an external boundary.

Finally, Failure can be established only while the machine still has enough authority and machinery to state that
result. Where that remains possible, the same authoritative conditions must establish the same Failure.

---

## 4. Contract Decision

### 4.1. Failure Is One Contract Kind

Kontrakt has one Failure Contract kind.

Contract Failure, State-Machine Failure, and Realization Failure classify the source from which Failure originates. They
do not create three unrelated Failure systems.

Failure does not judge another authority again. Contract and State-Machine sources provide their own unsuccessful
judgments, while a realization source establishes that already-authorized meaning could not be realized correctly.
Failure preserves the resulting unsuccessful machine meaning.

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

### 4.3. Contract Failure

A Contract Failure originates when a Contract Authority establishes that one of its required obligations is not
satisfied.

The owning Contract remains the judge. If Admission determines that required material is absent, Failure preserves that
Admission result rather than performing another Admission judgment.

The Contract progression that depended on the failed obligation cannot continue. Another admitted flow remains governed
by its own Contracts.

### 4.4. State-Machine Failure

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

### 4.5. Realization Failure

A Realization Failure exists when authoritative machine meaning has already been established but the backend cannot
realize it correctly.

```text
authoritative machine meaning
        ↓
backend realization
        ↓
required realization cannot complete correctly
        ↓
Realization Failure
```

This prevents implementation inability from being rewritten as a Contract judgment.

If Governance establishes that an Emergency Policy World must apply and the backend cannot realize that binding, the
Governance decision remains authoritative. The Failure belongs to realization.

The backend may not substitute a different semantic result simply because that result is easier to realize.

### 4.6. Failure Kind Belongs to the Owning Authority

A richer Contract may distinguish several unsuccessful meanings for the same authority. Those distinctions belong to the
authority that owns the requirement, not to Failure.

If Admission distinguishes missing required material from a value that violates its condition, Admission owns that
distinction. Failure preserves the exact unsuccessful judgment that Admission established.

The same rule applies outside the Contract Pipeline. State-Machine Failure preserves the unsuccessful State-Machine
judgment, while Realization Failure remains tied to the exact realization boundary that could not complete correctly.

Failure therefore does not impose a second taxonomy over existing judgments. It makes their unsuccessful meaning
explicit without replacing it with Failure-owned categories.

### 4.7. Exact Attribution

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

### 4.8. Boundary and Containment

A Failure belongs to the source and machine boundary in which it is established.

```text
Failure at one boundary
    does not imply
Failure at another boundary
```

The preceding Whole-Machine analysis examined how the same Contract, State-Machine, and Implementation axes cooperate
across Cores and pipelines. It did not establish a separate Whole-Machine Scope contract concept. Failure therefore
refers only to the relevant boundary already established by those axes and their participating pipelines.

This follows the containment principle used in high-reliability engineering: a local failure does not become machine
failure while the larger required function remains satisfied.

Another authority or realization boundary therefore does not inherit Failure. If it is active and later cannot satisfy
its own required meaning, it establishes a new Failure at its own boundary.

A boundary that is never entered has no Failure merely because earlier work ended.

### 4.9. Failure Continuation Is Normal Machine Flow

Failure is an ordinary terminal branch of the machine meaning that could not continue.

```text
judgment
    ├─ satisfied
    │      ↓
    │   required continuation
    │
    └─ unsuccessful
           ↓
       Failure established
           ↓
       failed continuation ends
           ↓
       explicit Failure continuation
```

The Failure continuation preserves the established Failure and allows the surrounding machine to complete the handling
that is valid after that boundary ends. It does not retry the failed obligation or silently resume the continuation that
depended on it.

This is not Failure propagation. Another active authority establishes its own Failure only if its own required meaning
later becomes unsatisfied.

### 4.10. Failure Does Not Terminate Realization Resources

Failure terminates contractual continuation at the failed boundary, not the realization resource that happened to
execute it.

A thread, worker, process, coroutine, or other backend resource may remain available for unrelated work after a Failure.
Conversely, a backend may discard a realization resource when implementation integrity requires it, but that decision is
not Failure meaning.

```text
Failure
    -> failed machine continuation ends

thread / worker / process lifetime
    -> backend realization decision
```

An exception may be used as a backend adapter for Failure when an external API requires it. Unwinding a stack or
terminating an uncaught thread is therefore a possible realization mechanism, never a Contract requirement.

### 4.11. Failure Is Not a Crash

A Failure is an established machine result. A crash is a physical realization event in which execution is abruptly lost
or terminated before that execution can complete the machine continuation needed to establish its result.

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

Crash material such as an exit status, signal, core dump, or surviving runtime record may later become Diagnostic
Evidence. A supervising or later execution may also establish its own Failure if one of its own requirements becomes
unsatisfied because the observed execution disappeared.

### 4.12. Failure Is Not an Indeterminate Outcome

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

### 4.13. Failure Does Not Own Recovery

Failure states the unsuccessful machine result. It does not decide what happens afterward.

A retry does not rewrite the earlier Failure. If a Contract-visible response follows, the Contract that owns that
response must authorize it.

Backend recovery remains implementation when it has no independent Contract meaning.

### 4.14. Physical Authority Limit

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

### 4.15. Failure, Diagnostic, and Publication Meaning

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
Failure from the exact realization point whose required meaning could not be carried out correctly.

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

Backend behavior cannot participate in canonical Failure identity.

### 5.3. Backend Error Vocabulary Is Not Contract Authority

A host-language exception does not define Failure.

Replacing exception-based control flow with another realization must leave Failure meaning unchanged.

A backend error signal may help establish a Realization Failure, but the signal itself does not become the Contract
identity.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Meaning

The Failure Contract makes an unsuccessful machine result explicit while preserving the exact source meaning.

Failure is a normal Contract Machine outcome. It ends the continuation that depended on the failed meaning and enters an
explicit Failure continuation.

It does not take over the judgment that produced that result, prescribe termination of the realization resource, or
represent an unknown outcome as definite Failure.

### 6.2. Realization

Kontrakt's backend already knows where Contract Failure can arise because each 1D Contract produces its own unsuccessful
judgment.

The mirrored realization plan provides the same advantage for implementation failure. A backend node already knows which
Contract meaning it is realizing, so a Realization Failure does not need to be reconstructed from a later exception
path.

That knowledge gives the compiler a direct Failure lowering path.

```text
exact unsuccessful judgment identity
    -> canonical Failure identity
    -> direct Failure continuation
    -> Diagnostic Evidence only when required
```

The runtime representation therefore does not need to be a Failure object. A compact identity can remain sufficient for
semantic Failure meaning, while runtime values, causes, stack history, and other explanatory material are materialized
separately only when Diagnostic Evidence requires them.

An adapter can still translate the Failure into an exception when an external API requires one.

A direct Failure continuation is therefore the natural lowering for known machine Failure. The backend does not need to
unwind to an uncaught exception or terminate the executing thread merely because a Contract or State-Machine judgment
was unsuccessful.

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

---

## 7. Verification and Determinism

The compiler must reject Failure meaning that depends on backend-only coordinates.

A Failure source must resolve to the exact Contract or State-Machine authority, or to the exact realization point, from
which the unsuccessful result was established. Ambiguous attribution is invalid rather than resolved by implementation
order.

`origin` cannot vary independently from `source`. Any physical origin tag must agree with the source from which it is
derived.

A Realization Failure cannot be rewritten as a Contract Failure for presentation convenience.

Implementation topology cannot move a Failure to a different machine boundary, and Failure semantics cannot require
termination of a thread, worker, process, coroutine, or other realization resource.

The same source, subject, unsuccessful judgment, applicable context, and boundary must establish the same
contract-visible Failure regardless of the Diagnostic Evidence later attached to it.

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
7. Which earlier ADRs must change once this model is accepted?

These questions do not reopen the separation between Failure meaning and realization mechanism or the decision that
Failure itself is intrinsic rather than user-authored.

---

## 9. Consequences

### Positive

Failure becomes explicit machine meaning rather than an inference from backend behavior.

Its exact source remains attributable after lowering, while its origin can be derived without becoming a second semantic
coordinate. Local failure stays local until another boundary establishes its own Failure.

Failure becomes an ordinary explicit machine branch rather than a requirement to unwind or terminate the executing
realization resource.

Crash is separated from established Failure, so abrupt execution loss cannot fabricate a semantic result.

Diagnostic tooling can present semantic Failure before runtime evidence, while Publication independently controls which
failure meaning may become an outward claim.

Because the compiler already knows Failure meaning, unsuccessful paths can be lowered without mandatory exception
machinery.

### Negative

Failure no longer fits the earlier model of an ordinary one-dimensional Contract-Pipeline slot, so documents that assume
that placement or require explicit Failure authoring must be revised.

The canonical boundary vocabulary still has to be resolved across the three machine axes.

Distributed realization also needs a separate treatment for outcomes that cannot honestly be classified as success or
Failure, and crash evidence needs a representation outside Failure identity.

### Neutral

Failure has no separate user declaration merely because it is one Contract kind.

This ADR establishes that an outward failure claim must pass Publication, but the Publication syntax and external
success/failure outcome vocabulary are decided by the Publication Contract ADR.

Existing backend mechanisms remain valid only when they realize this Contract without owning its meaning.