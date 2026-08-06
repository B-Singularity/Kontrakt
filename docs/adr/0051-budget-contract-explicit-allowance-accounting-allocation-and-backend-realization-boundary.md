# ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary

## Status

Proposed

## Date

2026-08-03

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0054: Policy Contract, Established Situation, Response-Contract Selection, and Judgment Boundary
- ADR-0053: Governance and Contract World Activation
- ADR-0052: Capacity Contract and Admission Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics
- `docs/design/planner-budget-resolution-and-worker-lifecycle.md`
- `docs/design/l1-planner-session-primitive-data-structures.md`

---

## 1. Context

A real machine works within finite conditions. Time, memory, and other machine resources cannot be consumed without
limit. When those limits exist only in configuration, timeout code, counters, allocators, or scheduler settings, the
implementation may protect itself, but the user's machine has not declared what it is allowed to consume.

Budget makes one execution limit explicit.

```text
Budget
    declares how much of one machine-resource quantity
    one exact contract subject may consume
    during one application of that subject
```

Budget belongs to the user's machine. It does not describe the compilation or verification fuel used internally by
Kontrakt. The selected backend may realize the contract only when it can preserve the declared boundary, quantity,
limit, and result.

Budget remains separate from Capacity, Governance, and Policy.

```text
Budget
    limit for one application of a declared contract subject

Capacity
    amount that may be admitted, held, or active now

Governance
    applicable contract world

Policy
    response selected from established contract material
```

The contract owns the limit. Observation and enforcement remain implementation.

---

## 2. Problem

A number alone is not a Budget. It does not say which contract it governs, what is measured, or when the measurement
begins and ends.

Implementation lifecycle units cannot close those questions. They may change without changing the user's contract and
must not silently become the Budget subject or boundary.

A declared limit also cannot be weakened when a backend cannot preserve it. An invalid, contradictory, or unsupported
Budget must fail during compilation. After compilation, a runtime overrun must stop the current contract pipeline rather
than become a warning, a late successful result, or an unnamed exception.

A countable value does not become Budget merely because software can observe it. Business values, input size, result
cardinality, current occupancy, and implementation work units remain with the contracts that define those meanings.

---

## 3. Decision

### 3.1. Foundation

A Budget Contract establishes a finite allowance before Kontrakt produces an executable realization.

```text
Explicit Budget Declaration
    -> Resolution and Validation
    -> Canonical Budget Contract
    -> Lowered Enforcement
```

No Budget is inferred from deployment limits, host configuration, heap size, worker count, or backend defaults. The
selected Budget surface establishes one exact Budget declaration or explicit absence.

The canonical declaration remains fixed in its applicable contract world. Runtime observation may support diagnostics
and later revision, but it cannot create, enlarge, replace, or redistribute an active allowance.

### 3.2. V1 Decisions

V1 admits two complete quantity profiles for an exact Interaction or one-dimensional Contract:

```text
Relative Elapsed Time
Memory Use
```

An Interaction Budget is optional. A one-dimensional Contract may have its own Budget whether or not the containing
Interaction has one. When both apply, both contracts remain authoritative.

For one active contract world, one exact `(subject, quantity)` pair may have only one Budget. Duplicate authority is a
compilation error.

Invalid or unrealizable Budget material fails during compilation. Runtime execution has one Budget failure result:
`Budget Stop`.

This ADR defines meaning. It does not define the Java or Kotlin authoring syntax.

---

## 4. Budget Contract

### 4.1. Meaning and Scope

A Budget governs one application of one exact contract subject.

```text
Interaction Budget
    governs one data flow through the selected Interaction

one-dimensional Budget
    governs one application of that exact Contract
    inside the same data flow
```

One presented data item passes through the selected Interaction once. Each selected one-dimensional Contract applies
once in that data flow. A later data item starts another application of the same contract law; it does not renew or
refill the previous allowance.

V1 does not admit a run, session, thread, worker, process, executor, call stack, or backend task as a Budget subject.

Budget does not own domain limits. Payment amount, inventory, account balance, input shape, output cardinality, and
other business or structural values remain with the contracts that define them. Current admission or holding limits
belong to Capacity.

### 4.2. Canonical Material and Uniqueness

The canonical V1 Budget material is:

```text
Budget identity
exact subject
quantity
allowance
applicable contract world
```

The exact subject fixes the measurement boundary. Each quantity profile fixes its unit and measurement meaning.

Within one active contract world, the following key must be unique:

```text
exact subject
+ exact quantity
```

The following declarations conflict and prevent compilation:

```text
CalculateCanonicalization + Relative Elapsed Time = 10 ms
CalculateCanonicalization + Relative Elapsed Time = 15 ms
```

The same quantity may be declared for an Interaction and one of its one-dimensional Contracts because the subjects
differ.

### 4.3. Subject Boundaries

An Interaction Budget begins when the Input Presentation is established at the contract boundary. Admission is therefore
inside the Interaction Budget. The boundary ends when the Interaction establishes its terminal result.

```text
Input Presentation established
    -> Interaction Budget begins

Interaction terminal result established
    -> Interaction Budget ends
```

A one-dimensional Budget begins when the material required by that Contract is established. It ends when the Contract
establishes its declared result or `Budget Stop`.

```text
required contract material established
    -> exact one-dimensional Budget begins

declared result or Budget Stop established
    -> exact one-dimensional Budget ends
```

Backend function entry, return, callback placement, allocation scope, or generated stage boundaries do not define these
limits.

### 4.4. Relative Elapsed Time

`Relative Elapsed Time` is the actual time that passes between the subject boundaries. It includes execution, waiting,
scheduling delay, garbage-collection pause, and backend work inside the boundary. The cause of elapsed time may appear
in diagnostics, but it does not change the Budget quantity.

The allowance is an inclusive maximum.

```text
established elapsed time <= allowance
    -> remains within allowance

allowance reached before the subject result is established
    -> Budget Stop
```

A result established exactly at the allowance remains valid. Once the allowance has been reached without a result, no
further contract progress is permitted.

Absolute calendar time is not Budget. A business rule tied to a date or timestamp belongs to another contract authority.

### 4.5. Memory Use

`Memory Use` is the memory consumed by one application of the exact subject between its Budget boundaries. Its allowance
is an inclusive maximum, under the same law as `Relative Elapsed Time`.

```text
established memory use <= allowance
    -> remains within allowance

allowance reached before the subject result is established
    -> Budget Stop
```

An Interaction may declare an overall memory Budget, and any one-dimensional Contract may declare its own memory Budget.
The same independence, uniqueness, simultaneous-failure, and derived-bound rules in Section 4.6 apply without a separate
accounting or distribution law. Current machine availability remains Capacity.

### 4.6. Interaction and One-Dimensional Budgets

Interaction and one-dimensional Budgets judge their own exact subjects independently.

```text
Interaction remains within its allowance
and
all applicable one-dimensional Contracts remain within their allowances
    -> Budget does not stop the data flow
```

A one-dimensional Budget does not borrow from, return allowance to, or receive an allocation from the Interaction
Budget. Neither declaration overrides the other.

When an Interaction Budget exists and same-quantity one-dimensional Budgets completely cover that Interaction, Kontrakt
may derive the strongest complete upper bound for planning and verification:

```text
min(
    Interaction allowance,
    sum of the covering one-dimensional allowances
)
```

This derived value is not a new Budget and has no authority without complete coverage.

If the same established observation violates an Interaction Budget and a one-dimensional Budget, both exact
`Budget Stop`
results are retained. Kontrakt does not select a representative failure or hide one contract behind the other.

### 4.7. Compilation Failure

Budget errors that can be established from contract and backend capability material are compilation errors.

Compilation fails when:

- one `(subject, quantity)` pair has duplicate Budget authority,
- required Budget material is missing, contradictory, non-finite, or invalid,
- the declared material proves that the Budget cannot be satisfied,
- or the selected backend cannot preserve the declared boundary, quantity, and stop result.

Kontrakt emits a contract diagnostic naming the exact subject, quantity, conflicting or unsupported material, and
required correction. It does not produce an executable artifact or lower the declaration to an advisory limit.

V1 therefore has no runtime `Budget Refusal`. A Budget that cannot become an executable obligation is rejected before
execution.

### 4.8. Budget Stop

`Budget Stop` is established when a compiled Budget reaches its allowance before the subject establishes its required
result. The same law applies to every Budget quantity; only the observed quantity and unit differ.

```text
Budget Stop
    ends the exact Budget subject without a successful result
    forbids later contract progress from that subject
    forbids successful Publication for the stopped Interaction
```

A one-dimensional `Budget Stop` prevents that Contract from establishing its successful result. Because the pipeline
cannot continue without the required preceding result, the containing Interaction ends with an explicit terminal stop
attributed to that Budget.

An Interaction `Budget Stop` ends the whole current data flow. It does not terminate the server, process, or later data
flows. Retry, resubmission, routing, and recovery remain infrastructure decisions.

### 4.9. Diagnostic Evidence

A stopped Interaction must preserve enough evidence to show:

```text
completed one-dimensional Contracts
exact stopped subject
violated Budget or Budgets
allowance and established observation
one-dimensional Contracts not entered
```

Earlier completed contract results remain historical facts of that data flow. They are not published as a partial
successful result. Diagnostic Evidence may explain how far the pipeline progressed and why it stopped.

### 4.10. Budget and Capacity

Budget and Capacity answer different questions.

```text
Budget
    did one contract application remain within its declared execution limit?

Capacity
    may this material or work be admitted, held, or active now?
```

One does not substitute for the other. When both authorities establish violations, both exact results and diagnostics
are retained. A Capacity violation does not imply a Budget violation, and a Budget overrun does not create a Capacity
result.

The complete Capacity law is decided in ADR-0052.

### 4.11. V1 Boundary

V1 requires:

- one exact Interaction or one-dimensional Contract subject,
- `Relative Elapsed Time` or `Memory Use`,
- one finite inclusive allowance,
- one applicable contract world,
- unique authority for each `(subject, quantity)` pair,
- compilation failure for invalid or unsupported material,
- and `Budget Stop` before further progress would exceed the allowance.

Distribution, renewal, user-selected accounting, enforcement levels, cancellation modes, and implementation-lifecycle
subjects are not part of V1 Budget material.

---

## 5. Authoring and Processing Boundary

This ADR defines meaning, not Java or Kotlin syntax. The later authoring surface follows ADR-0047 and carries only
finite contract data.

```text
Source declaration
    -> Resolution
    -> Validation
    -> Canonicalization
    -> Lowering
    -> Backend realization
```

The source presents the exact subject, quantity, and allowance. Canonical identity and applicable contract-world
material may be completed by the compiler from already declared authority.

User code does not define Budget through reporting calls, callbacks, environment reads, runtime registration, scheduler
settings, or backend tuning.

The following examples describe meaning only.

### 5.1. Interaction Example

```text
Budget
    subject:
        Calculate Interaction

    quantity:
        Relative Elapsed Time

    allowance:
        100 ms
```

### 5.2. One-Dimensional Example

```text
Budget
    subject:
        CalculateCanonicalization

    quantity:
        Relative Elapsed Time

    allowance:
        10 ms
```

### 5.3. Memory Example

```text
Budget
    subject:
        Calculate Interaction

    quantity:
        Memory Use

    allowance:
        4 GiB

Budget
    subject:
        CalculateCanonicalization

    quantity:
        Memory Use

    allowance:
        256 MiB
```

The Interaction declaration is optional. When both declarations exist, they are judged under the same law as the time
Budgets in Sections 5.1 and 5.2.

The final authoring design must still choose naming, explicit absence syntax, source references, numeric literal form,
and error presentation without exposing backend shape.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Authority

The canonical material and `Budget Stop` law defined in Section 4 are contract authority. Clock and allocation APIs,
instrumentation, generated guards, interruption mechanisms, process lifetime, and retry policy are not.

A backend mechanism may be replaced only when the replacement preserves the same canonical meaning and contract-visible
result. Unsupported realization makes compilation fail; it does not weaken the Budget.

### 6.2. Pipeline Control

Kontrakt may place implementation checks around lowered contract boundaries because those boundaries already have
explicit contract meaning. A JVM realization may be unable to kill arbitrary user code at an exact physical instant.
That does not permit a late successful result; an admissible backend must prevent the stopped passage from establishing
later contract-visible consequences.

### 6.3. Backend Capability and Fail-Closed Compilation

Backend capability validation must prove the declared boundaries, quantity observation, exact subject attribution, and
`Budget Stop`. Failure is handled by the compilation rule in Section 4.7.

### 6.4. Deterministic Realization

Determinism is an implementation law, not a Budget coordinate or user option.

For the same canonical Budget, implementation inputs, and established quantity observation, lowering and judgment must
produce the same contract-visible result. Physical conditions may change the observation; they do not change the
comparison law or allow runtime telemetry to rewrite the active Budget.

Optimization is allowed only after these conditions hold.

---

## 7. Verification

Contract verification must establish that the Budget is complete, finite, unique, and lowerable. The exact subject,
quantity, allowance, and applicable world must resolve without ambiguity.

The verifier must reject duplicate `(subject, quantity)` authority, implementation-lifecycle subjects, hidden allowance
establishment, unsupported backend capability, and values that belong to Capacity or another contract.

Elapsed-time tests must cover:

```text
Interaction start at Input Presentation establishment
one-dimensional start at required-material establishment
result before allowance
result exactly at allowance
Budget Stop when allowance is reached first
simultaneous Interaction and one-dimensional Budget Stops
```

Memory tests must cover Interaction and one-dimensional boundaries, exact allowance, `Budget Stop`, simultaneous
Interaction and one-dimensional stops, and compilation failure when the quantity cannot be preserved.

Failure tests must prove that completed earlier Contracts remain visible to diagnostics, later Contracts are not
entered, partial material is not published as success, and the stopped passage cannot establish later State movement or
Publication.

Backend conformance must show that unsupported realization fails closed and optimization does not change the result.
Generated fixtures and property-based tests should exercise the closed contract material and its lowered boundary.

---

## 8. Deferred Decisions

The following remain open:

- final Java or Kotlin Budget syntax and explicit absence syntax,
- canonical identity bytes,
- additional machine-resource quantity profiles,
- exact JVM realization proof for `Memory Use`,
- realization across several runtimes,
- profiling Publication shape,
- Diagnostic linkage,
- Failure mapping,
- and Version qualification.

Capacity is decided in ADR-0052. Governance and Policy are decided in ADR-0053 and ADR-0054.

---

## 9. Consequences

### Positive

Budget becomes an explicit limit on one exact contract application rather than a runtime setting. Interaction and
one-dimensional limits can coexist without allocation, inheritance, or hidden override.

Compile-time rejection prevents contradictory or unrealizable Budget declarations from becoming executable software. A
runtime overrun has one explicit result, exact attribution, and complete pipeline diagnostics.

The user contract remains independent from clock, allocation instrumentation, cancellation, scheduler, process, and
retry mechanisms.

### Negative

The backend must control enough of the contract boundary to preserve `Budget Stop`. Opaque user code, native work,
external systems, or several runtimes may make a declaration unsupported.

Physical conditions may change whether one data flow reaches a time or memory allowance. This does not change the
contract comparison law.

### Neutral

Profiling may support a later contract revision, but it does not modify an active Budget. Capacity, Publication,
Diagnostic, Failure, Governance, and Policy retain their own authority.