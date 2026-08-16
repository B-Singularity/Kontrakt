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

These failures arise from different authorities, but they express the same machine-level fact: required meaning could
not continue as required.

That makes the earlier placement of Failure as an ordinary one-dimensional pipeline slot too narrow. ADR-0057 therefore
treats Failure as one Contract kind that crosses the relevant authority boundaries while preserving the authority that
actually established each failure.

---

## 2. Problem

Software commonly leaves Failure meaning implicit behind control flow.

An exception exposes an unsuccessful path, but it does not state the exact machine Failure. The failed obligation and
the authority that owned it still have to be recovered from implementation context.

A stack trace does not solve that problem. It may expose thousands of frames while still leaving the developer to infer
which machine obligation actually failed. More execution history is not a clearer Failure statement, and capturing that
call history also adds work to the failure path.

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

The resulting Failure must also remain local to the boundary that actually failed. A larger scope fails only when its
own required meaning can no longer be satisfied.

The same discipline applies when the outcome cannot be known. An indeterminate result cannot be called Failure merely
because the local execution needs a terminal branch.

---

## 3. Decision Drivers

Failure must remain explicit regardless of how the backend realizes it. Its meaning therefore has to exist before any
implementation mechanism is chosen.

The authority that owns an obligation must also own the judgment that it was not satisfied. Failure records that
unsuccessful result without judging the obligation again.

A Failure must identify the exact authority and meaning that failed. This attribution cannot depend on later inspection
of implementation behavior.

Failure belongs to the scope in which it is established. Another scope has its own Failure only when its own required
meaning can no longer be satisfied.

The cause of a Failure does not define the Failure itself, and the response that follows it is governed separately.

Finally, Failure can be established only while the machine still has enough authority and machinery to state that
result. Where that remains possible, the same authoritative conditions must establish the same Failure.

---

## 4. Contract Decision

### 4.1. Failure Is One Contract Kind

Kontrakt has one Failure Contract kind.

Contract Failure, State-Machine Failure, and Realization Failure identify the authority boundary from which Failure
originates. They do not create three unrelated Failure systems.

Failure does not judge another authority again. It receives an unsuccessful authoritative result and establishes the
explicit machine result that corresponds to it.

```text
source authority
    establishes its own unsuccessful result

Failure
    preserves that result as explicit machine meaning
```

A stopped execution is not enough by itself. Failure exists only when an authority can establish what failed.

### 4.2. Meaning of Failure

Failure exists only in relation to required machine meaning. A machine cannot fail in the abstract; an authority must
first require a specific meaning.

Failure is established when that authority can no longer establish the required meaning as required.

```text
required meaning
        ↓
not established as required
        ↓
Failure
```

Complete loss is not required. A machine may still produce a result and nevertheless fail when that result falls outside
the meaning permitted by the owning authority.

Reliability and safety engineering use the same boundary: an internal fault is not yet a failure when the required
function remains available. Kontrakt applies that principle to machine meaning. An implementation disturbance therefore
remains an implementation fact until it prevents an authoritative requirement from being satisfied.

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

### 4.6. Failure Mode

Once Failure is established, the owning authority may distinguish the way in which its required meaning was not
established. That distinction is the Failure Mode.

Kontrakt does not impose a universal mode taxonomy. If an Admission Contract distinguishes missing required material
from a present value that fails its condition, that distinction already belongs to Admission and Failure preserves it.

Engineering failure analysis separates mode from cause because they answer different questions. The mode states how the
required function failed, while the cause explains why it happened. A backend fault may therefore explain a Realization
Failure without becoming its identity.

The effect of a Failure is separate again. If another authority later loses its own required meaning because of that
effect, it establishes a new Failure of its own.

### 4.7. Exact Attribution

Every Failure Result has an exact origin.

Attribution identifies the authority that established the unsuccessful result and the boundary that could not continue.
Contract context is retained when it changes that meaning.

```text
Failure Result
    origin
    exact authority
    failed meaning
    applicable Contract context
    stopped scope
```

The canonical representation is deferred, but backend evidence cannot replace this attribution.

### 4.8. Scope and Containment

A Failure belongs to the authority and scope in which it is established.

```text
local Failure
    does not imply
higher-scope Failure
```

This follows the containment principle used in high-reliability engineering: a local failure does not become system
failure while the larger required function remains satisfied.

Another authority therefore does not inherit Failure. If it is active and later cannot satisfy its own required meaning,
it establishes a new Failure at its own boundary.

A boundary that is never entered has no Failure merely because earlier work ended.

### 4.9. Failure Is Not an Indeterminate Outcome

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

### 4.10. Failure Does Not Own Recovery

Failure states the unsuccessful machine result. It does not decide what happens afterward.

A retry does not rewrite the earlier Failure. If a Contract-visible response follows, the Contract that owns that
response must authorize it.

Backend recovery remains implementation when it has no independent Contract meaning.

### 4.11. Physical Authority Limit

Failure can be established only while enough machinery remains to establish it.

If the execution substrate disappears before a final result exists, the destroyed execution does not perform one last
Failure judgment.

```text
software can still establish the result
    -> explicit Failure may be produced

the required machinery no longer exists
    -> no fictional final Failure
```

A later execution may reason from durable material that survived the loss, but that later reasoning belongs to the later
execution.

### 4.12. Failure and Diagnostic Meaning

Failure says what failed. Diagnostic Evidence explains the established result.

That distinction changes how failure should be presented. A stack trace is execution evidence, so shortening it does not
produce a semantic Failure summary.

Because Failure already carries exact attribution, tooling can show the machine meaning first.

```text
Failure Summary
    origin
    authority
    failed meaning
    Contract context
    stopped scope
```

Diagnostic Evidence can remain behind that summary until it is actually needed.

Publication still decides whether Failure may become an outward claim, while Output Presentation defines the shape of
that claim. Retention remains separate.

---

## 5. Frontend and Canonical Failure Material

### 5.1. Authoring Location Is Deferred

ADR-0057 decides Failure authority before its user API.

One Failure Contract kind does not imply one project-global declaration, and it does not require every Interface to
repeat the same binding.

The frontend may eventually use explicit Failure declarations or derive identity from an unsuccessful judgment. This ADR
does not choose between those forms.

Filesystem placement cannot become Failure authority.

### 5.2. Canonical Material

Canonical Failure material must preserve the origin and exact authority of the result.

A meaningful distinction already owned by the source authority must survive canonicalization without being redeclared as
a parallel Failure taxonomy.

Scope must identify the progression that can no longer continue. Contract context participates only where it changes the
meaning.

Backend behavior cannot participate in canonical Failure identity.

### 5.3. Backend Error Vocabulary Is Not Contract Authority

A host-language exception does not define Failure.

Replacing exception-based control flow with another realization must leave Failure meaning unchanged.

A backend error signal may help establish a Realization Failure, but the signal itself does not become the Contract
identity.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Meaning

The Failure Contract makes an unsuccessful machine result explicit while preserving its origin and scope.

It does not take over the judgment that produced that result.

It also refuses to represent an unknown outcome as definite Failure.

### 6.2. Realization

Kontrakt's backend already knows where Contract Failure can arise because each 1D Contract produces its own unsuccessful
judgment.

The mirrored realization plan provides the same advantage for implementation failure. A backend node already knows which
Contract meaning it is realizing, so a Realization Failure does not need to be reconstructed from a later exception
path.

That knowledge gives the compiler a direct Failure lowering path.

```text
exact unsuccessful judgment
    -> canonical Failure identity
    -> direct Failure continuation
    -> richer material only when required
```

The runtime representation therefore does not need to be a Failure object. A compact identity can remain sufficient
until a boundary requires richer material.

An adapter can still translate the Failure into an exception when an external API requires one.

This separation also permits a cheaper realization than ordinary exception machinery when the backend can use direct
control flow instead. That is a consequence of explicit Failure meaning, not the reason Failure exists.

Realization itself may still fail. When the backend can establish that fact, the resulting Failure must remain
attributed to the Contract meaning that was being realized.

### 6.3. Recovery

Recovery remains outside Failure authority.

If recovery starts a new Contract interaction, that interaction begins under its own applicable Contracts. Later success
does not rewrite the earlier Failure.

### 6.4. Diagnostics

Diagnostic machinery may observe Failure without defining it.

A stack trace can become Diagnostic Evidence when useful, but Failure identity must remain complete without it.

---

## 7. Verification and Determinism

The compiler must reject Failure meaning that depends on backend-only coordinates.

A Failure identity must resolve to an authority that exists in the selected Contract World. Ambiguous attribution is
invalid rather than resolved by implementation order.

A Realization Failure cannot be rewritten as a Contract Failure for presentation convenience.

Implementation topology cannot widen Failure scope.

The same authoritative material reaching the same unsuccessful judgment must establish the same contract-visible
Failure.

Verification cannot require a destroyed execution to produce a final Failure, and it cannot turn an indeterminate
outcome into certainty.

A compile-time rejection of an invalid Failure definition is not a runtime Failure Result of the machine being defined.

---

## 8. Deferred Decisions

The following questions remain open:

1. What source form declares Failure without creating redundant Interface boilerplate?
2. Which unsuccessful distinctions need explicit Failure material beyond the owning judgment coordinate?
3. What canonical material is required to represent Failure scope?
4. What authority owns an Indeterminate Outcome?
5. How may Publication and Diagnostic Contracts consume Failure without acquiring its authority?
6. What backend material is sufficient to establish Realization Failure without promoting implementation errors into
   Contract meaning?
7. Which earlier ADRs must change once this model is accepted?

These questions do not reopen the separation between Failure meaning and realization mechanism.

---

## 9. Consequences

### Positive

Failure becomes explicit machine meaning rather than an inference from backend behavior.

Its origin remains attributable after lowering, while local failure stays local until another boundary establishes a
larger Failure.

Diagnostic tooling can present semantic Failure before runtime evidence.

Because the compiler already knows Failure meaning, unsuccessful paths can be lowered without mandatory exception
machinery.

### Negative

Failure no longer fits the earlier model of an ordinary one-dimensional Contract-Pipeline slot, so documents that assume
that placement must be revised.

The frontend cannot be finalized until the authoring form is decided.

Distributed realization also needs a separate treatment for outcomes that cannot honestly be classified as success or
Failure.

### Neutral

One Failure Contract kind does not imply one global source declaration.

This ADR does not prescribe how external callers receive Failure.

Existing backend mechanisms remain valid only when they realize this Contract without owning its meaning.