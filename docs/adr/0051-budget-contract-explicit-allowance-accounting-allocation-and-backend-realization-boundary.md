# ADR-0051: Budget Contract, Explicit Allowance, Accounting, Fixed Contract Distribution, and Backend Realization Boundary

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
limit. When those limits exist only in configuration, counters, timeout code, allocators, or scheduler settings, the
implementation may protect itself, but the user's machine has not declared what it is allowed to consume.

Budget makes that allowance explicit.

```text
Budget
    declares how much of one machine-resource quantity
    one declared subject may consume
    inside one declared boundary
```

Budget belongs to the user's machine. It does not describe the internal compilation or verification fuel used by
Kontrakt. The selected backend realizes the contract only when it can observe the declared quantity, connect the
resulting charge to one resolved Budget position, and provide the required allowance-limit guarantee.

Budget remains separate from Capacity, Governance, and Policy.

```text
Budget
    cumulative allowance across a boundary

Capacity
    amount currently held, admitted, or active

Governance
    applicable contract world

Policy
    prepared response selected from established material
```

The contract states the limit. The machinery used to observe, account, and enforce it remains implementation.

---

## 2. Problem

A numeric limit is incomplete when it does not state:

- what subject it governs,
- which quantity and unit it counts,
- where accounting begins and ends,
- how charge changes the position,
- when allowance renews,
- and what must happen when the limit is reached.

The same number can mean different obligations. One hundred milliseconds for one Operation execution is not the same as
one hundred milliseconds for a run, a session, or a rolling window. Four gigabytes allocated across a run is not current
live memory. A timeout does not by itself explain whether late results may still establish State movement or
Publication.

Budget must therefore be explicit before governed work begins. Runtime observation may support a later revision, but it
must not create, enlarge, redistribute, or replace an active Budget.

The current JVM backend also has real limits. It can strongly control Kontrakt-controlled pipeline boundaries and reject
later contract-visible consequences, but it cannot always stop arbitrary user implementation code at the exact physical
instant a limit is reached. This ADR does not hide that difference. It defines the contract guarantee separately from
the mechanism used to realize it.

A cluster-wide total cannot be established by merging local counters after execution. A distributed Budget requires one
authoritative distributed position. Until such a backend exists, the declaration is unsupported rather than
approximately enforced.

A countable value does not become Budget merely because software can observe it. Budget is limited to declared
machine-resource consumption.

---

## 3. Decision

### 3.1. Foundation

A Budget Contract establishes finite consumable allowance before governed work may consume it.

```text
Explicit Budget Declaration
    ->
Established Budget Position
    ->
Governed Consumption and Judgment
```

The user declares Budget meaning. Kontrakt resolves, validates, canonicalizes, lowers, and realizes that meaning.

```text
User authoring
    presents finite contract material

Canonical Budget Contract
    owns Budget meaning and judgment law

Backend
    observes charge
    maintains the position
    performs judgment
    realizes the declared guarantee
```

No Budget is inferred from host limits, deployment shape, current heap size, worker count, or runtime configuration. A
selected Budget slot establishes one exact Budget or explicit absence.

An active Budget remains unchanged for its governed boundary. A different allowance, renewal, distribution, or guarantee
requires new explicit material and Governance activation.

### 3.2. V1 Decisions

V1 closes the following choices:

1. Actual elapsed time is required Budget quantity material.
2. The allowance-limit guarantee uses the vocabulary defined in Section 4.10.
3. Distribution divides one total allowance into fixed limits for explicitly declared contracts before the governed
   boundary begins.
4. Those limits remain unchanged for the active boundary. A different distribution requires new Budget material and
   Governance activation.
5. One Operation-scope Budget position continues across every covered one-dimensional contract boundary. Internal stages
   do not receive copied or renewed positions.
6. A backend that cannot provide the declared quantity meaning and guarantee rejects the realization. It does not weaken
   the contract.

This ADR does not define the Java or Kotlin source syntax.

---

## 4. Budget Contract

### 4.1. Meaning and Scope

A Budget Contract declares how much of one machine-resource quantity one subject may consume inside one boundary.

The subject may be the whole declared machine, one interface machine, one Operation execution, one run, one session, or
another explicit contract boundary. Thread ownership, call-stack position, process identity, package placement, or
current worker must not silently choose the subject.

A whole-machine Budget includes user implementation, Kontrakt runtime work, generated machinery, and other consumption
inside the declared authoritative boundary. A narrower subject is valid when it is explicit.

The current JVM backend admits only a boundary for which one Kontrakt runtime and JVM can maintain the authoritative
position. It does not infer one enforceable total across several JVMs or servers.

Budget does not own domain limits. Payment amount, inventory, account balance, or another business value remains with
the contract that defines its meaning. Maintained aggregates and profiling statistics also remain outside Budget
authority.

A countable event is not automatically a resource quantity. Operation entry, retry, Publication item, verification case,
loop iteration, hash probe, or compiler pass does not become user Budget material merely because software can count it.

### 4.2. Establishment and Position Lifetime

The complete active Budget is established before governed work begins.

```text
Budget identity
subject
boundary
quantity and unit
allowance
distribution or explicit no distribution
accounting law
renewal law
allowance-limit guarantee
applicable contract world
```

These coordinates remain fixed for the boundary. Profiling or environment changes do not modify them.

The Budget position follows the declared boundary, not the internal implementation pipeline. When one Operation-scope
Budget covers several one-dimensional contract boundaries, all covered work consults and updates the same position.
Moving to the next boundary does not reset, copy, or renew it.

A narrower Budget exists only when it is separately declared with its own subject and boundary.

### 4.3. Canonical Material

Each coordinate answers one question.

```text
subject
    whose consumption is governed?

boundary
    when does accounting begin and end?

quantity and unit
    what is consumed and how is it measured?

allowance
    how much may be consumed?

distribution
    how is one total divided into fixed limits for declared contracts?

accounting law
    how does established charge change the position?

renewal law
    when is a new position established?

allowance-limit guarantee
    what result must the machine guarantee at the limit?

applicable contract world
    under which world does this Budget have authority?
```

A boundary may have one terminal condition or several. When several are declared, the first condition that becomes
established ends the boundary. This general law is sufficient for normal completion and deadline expiration; it does not
create a separate timeout family.

The contract does not prescribe how a backend observes, connects, stores, or enforces charge. Those choices remain
realization.

### 4.4. Quantity and Backend Admissibility

A Budget quantity must represent finite machine-resource consumption. The selected backend must be able to:

1. identify the exact quantity and boundary,
2. observe every governed charge without hidden loss or duplication,
3. connect each charge to one resolved Budget position,
4. maintain one authoritative position under concurrency,
5. and provide the declared guarantee.

A measurement API alone does not prove these conditions.

The current V1 position is:

| Quantity                   | JVM V1 status | Reason                                                                                                                                                                 |
|----------------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Actual elapsed time        | Required      | Kontrakt can establish the governed start, terminal conditions, and contract-progress result.                                                                          |
| Cumulative allocated bytes | Conditional   | The backend must prove complete observation and exact connection for the declared subject. This ADR does not assume that ordinary JVM allocation satisfies that proof. |
| Processor execution time   | Deferred      | Observation and safe exact enforcement are not closed for V1.                                                                                                          |
| Read or written bytes      | Deferred      | Support requires one complete declared I/O boundary.                                                                                                                   |
| Multi-runtime total        | Unsupported   | Local positions do not create one authoritative distributed position.                                                                                                  |

The backend proof belongs to implementation admissibility, not to canonical Budget material. This ADR deliberately does
not define support by naming particular storage structures, allocation APIs, or generated artifacts. Any mechanism is
acceptable only if it proves the declared meaning and guarantee.

One-dimensional contracts provide explicit boundaries and result coordinates that let the generated implementation place
checks without guessing contract meaning. Backend profiling may use those coordinates, but profiling does not create
Budget authority or alter an active position.

### 4.5. Time Quantity

Actual elapsed time is valid Budget material.

```text
subject
    one declared machine boundary

boundary
    explicit start
    one or more terminal conditions
    first established terminal condition ends the boundary

quantity
    elapsed time

allowance
    declared duration
```

Example:

```text
one Operation execution
    begins when execution is admitted
    ends when normal completion or the deadline is established first
    may consume at most 100 ms of elapsed time
```

A deadline is not a separate Budget or accounting family. It is one terminal condition derived from the elapsed-time
allowance.

When the deadline is established first, the expired execution loses authority to enter later Kontrakt-controlled
boundaries or establish a successful result, State movement, or Publication. A late return through a familiar interface
does not restore that authority.

Physical execution stopping is separate. The JVM backend may request cancellation or interruption, and a stronger
backend may terminate an isolated execution. Those mechanisms realize stronger guarantees but do not change Budget
meaning.

Timeout affects only its declared boundary. It must not replace identity, ordering, structural boundedness, cache
correctness, restart limits, or another contract's result.

Processor time, no-progress time, and other time meanings remain deferred.

### 4.6. Memory Quantity

`Memory` is not one complete quantity.

```text
cumulative allocated bytes
    consumption accumulated across a boundary
    Budget form

current live bytes
peak live bytes
retained bytes
    amount held or retained at a position
    Capacity form
```

Cumulative allocated bytes may be admitted only when the selected backend proves complete accounting for the declared
subject. The current JVM backend must not claim this guarantee from an approximate heap metric or partial
instrumentation. If complete accounting cannot be shown, the declaration is unsupported.

A whole-machine memory Budget must include all governed consumption inside its declared authoritative boundary. It must
not exclude Kontrakt or generated work while retaining the whole-machine label.

Concrete allocation, layout, containment, and physical-cap strategies remain implementation. Their names and tuning
values are not required authoring fields.

### 4.7. Accounting and Renewal

The basic relation is cumulative consumption.

```text
Established Budget Position
+ Established Charge
    -> BudgetWithinAllowance + Next Budget Position

or

Established Budget Position
+ Established Charge
    -> BudgetWouldExceed
```

A charge may be known before consumption, established during consumption, or established at a later controlled boundary.
The timing does not create a new accounting family. It determines which guarantee the backend can provide.

Different quantities retain separate positions. Elapsed time must not stand in for bytes, current occupancy, structural
limits, or semantic work.

Renewal is separate from accounting. V1 permits finite lowerable renewal such as one execution, one run, one session, a
fixed window, a rolling window, periodic refill, one shot, or no renewal. Arbitrary user algorithms are not admitted.

### 4.8. Fixed Distribution Across Declared Contracts

One established total may be divided into fixed limits for explicitly declared contracts before the governed boundary
begins.

Example:

```text
total allowance
    4 GiB for one run

target limits
    Payment Operation Contract: 1 GiB
    Search Operation Contract: 2 GiB
    Diagnostic Publication Contract: 256 MiB

unassigned remainder
    768 MiB
```

Every target limit shares the total's quantity, unit, accounting law, boundary, renewal, and applicable contract world.
The following conservation law is mandatory.

```text
sum of target limits
+ unassigned remainder
    = established total allowance
```

A limit belongs to a declared contract, not to an implementation unit. Parallel execution does not copy it. A
per-execution allowance is a separate Budget unless one explicit total accounting law preserves a shared position.

The limits and remainder stay unchanged for the active boundary. A different distribution requires new Budget material
and Governance activation for later boundaries.

### 4.9. Budget Judgment

Budget judgment is typed.

```text
BudgetWithinAllowance
    + Next Budget Position

or

BudgetWouldExceed
```

`BudgetWithinAllowance` permits no work by itself. It states only that the established charge remains within allowance.
`BudgetWouldExceed` belongs to Budget and must not be reported as Capacity refusal, State refusal, Invariant violation,
or an unnamed runtime exception.

Policy may select a prepared response after reading the judgment. It may not reverse the result or create new allowance.
Diagnostic and Failure mapping remain separate decisions.

### 4.10. V1 Allowance-Limit Guarantees

`Allowance-limit guarantee` is the canonical coordinate that states the required enforcement strength. V1 closes the
following vocabulary.

#### Charge Refusal

When the backend knows that a prospective charge would exceed allowance, it refuses the charge before it becomes
established.

#### Contract-Progress Cutoff

Once the Budget boundary ends or an over-limit result is established, the affected execution cannot enter later
Kontrakt-controlled boundaries or establish successful contract-visible consequences.

#### Cooperative Cancellation

The backend provides Contract-Progress Cutoff and also requests that the running work stop through the supported
cooperative mechanism. Physical termination is not guaranteed.

#### Isolated Termination

The backend provides Contract-Progress Cutoff and terminates an execution unit that the selected realization can safely
isolate and stop.

A declaration may require the compatible terms needed for its quantity and boundary. A backend must reject unsupported
strength rather than substitute a weaker one.

Observation after execution is not a separate guarantee term. If the backend can only report usage after
contract-visible consequences have already become authoritative, the material is profiling or verification rather than a
V1 enforcing Budget. If a late observation still causes Contract-Progress Cutoff before later consequences, the declared
guarantee remains Contract-Progress Cutoff.

### 4.11. Budget and Capacity

Budget and Capacity answer different questions.

```text
Budget
    how much has been consumed across the governed boundary?

Capacity
    how much may be held, admitted, or active now?
```

One backend control point may consult both. Capacity may refuse current admission; Budget may refuse a prospective
charge or cut off later contract progress. Their positions and judgments remain separate.

Repeated checks at one-dimensional contract boundaries do not create new Budget positions. They consult the position
owned by the declared Budget boundary.

The complete Capacity law is decided in ADR-0052.

### 4.12. V1 Boundary

V1 requires:

- one explicit Budget or explicit absence,
- exact subject, boundary, quantity, unit, and allowance,
- explicit renewal and guarantee,
- one authoritative position for the governed boundary,
- fixed-limit distribution or explicit no distribution,
- and backend rejection when observation, accounting, or guarantee is unsupported.

V1 supports actual elapsed-time Budget and the first-established-terminal-condition boundary law.

Cumulative allocated bytes retain valid Budget shape but remain conditional on backend proof. Current live, peak live,
and retained bytes belong to Capacity. Processor time, I/O bytes, and distributed totals remain outside V1.

Event counts and implementation work units remain outside the runtime Budget catalog.

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

The source must be able to present the subject, boundary, quantity, allowance, distribution choice, renewal, and
required guarantee. Canonical identity, applicable contract world, and lowerable accounting material may be completed by
the compiler.

User code must not define Budget through reporting calls, callbacks, environment reads, or implementation tuning. Such
machinery may exist behind the lowered boundary but cannot own Budget meaning.

The following examples describe meaning only.

### 5.1. Elapsed-Time Example

```text
Budget
    subject:
        one Search Operation execution

    boundary:
        begins when execution is admitted
        ends when normal completion or the deadline is established first

    quantity:
        elapsed time

    allowance:
        100 ms

    renewal:
        one new allowance for each admitted execution

    distribution:
        none

    required guarantee:
        Contract-Progress Cutoff
        Cooperative Cancellation
```

### 5.2. Fixed-Distribution Example

```text
Budget
    subject:
        one system run

    quantity:
        cumulative allocated bytes

    allowance:
        4 GiB

    distribution:
        Payment Operation Contract limit: 1 GiB
        Search Operation Contract limit: 2 GiB
        Diagnostic Publication Contract limit: 256 MiB
        unassigned remainder: 768 MiB
```

This example does not claim that the current JVM backend already supports that memory quantity. Backend admissibility is
a separate validation result.

The final authoring design must still choose naming, absence syntax, source references, and error presentation without
exposing backend shape.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Authority

The contract owns:

```text
subject
boundary
quantity and unit
allowance
fixed distribution or no distribution
accounting law
renewal law
allowance-limit guarantee
applicable contract world
Budget judgment
```

It does not own the mechanisms used to observe, store, schedule, or enforce the Budget.

The backend may replace any realization mechanism only when the replacement preserves the same canonical meaning and
contract-visible result. An unsupported quantity or guarantee makes the realization inadmissible; the Budget is not
weakened for compatibility.

### 6.2. One-Dimensional Pipeline Control

Each selected one-dimensional contract lowers to a Kontrakt-controlled implementation boundary. The backend may
therefore place Budget and Capacity checks along the generated pipeline without adding new contract authority.

```text
enter one-dimensional boundary
    -> consult Capacity when applicable
    -> consult the existing Budget position
    -> perform permitted realization
    -> confirm that the result still has authority
    -> enter the next boundary
```

The same Operation-scope Budget position continues across the covered boundaries. The checks do not create stage-local
Budgets.

The user implementation body is less controllable than the generated boundary around it. A JVM backend may be unable to
stop arbitrary code immediately, but it can still expire that execution's contract authority, reject late results, block
later State movement, and refuse successful Publication. This is the implementation meaning of Contract-Progress Cutoff.

### 6.3. Backend Capability and Fail-Closed Admission

For each supported quantity, the backend must provide a capability proof that covers observation, exact connection to
the resolved position, concurrency, arithmetic, and the declared guarantee.

The proof fails when charge may be lost or duplicated, one authoritative position cannot be maintained, unit conversion
is inexact, arithmetic overflows, the minimal realization cannot fit, or the required guarantee is unavailable.

Unsupported declarations fail during validation or lowering. They must not silently become advisory limits.

### 6.4. Implementation Considerations — Deterministic Realization

Determinism is a Kontrakt implementation law. It is not Budget meaning, a canonical coordinate, or a user option.

For one implementation version, the same canonical Budget material, pinned implementation inputs, established charges,
and terminal-condition material must produce the same plan, authoritative position, judgment, and contract-visible
result.

A backend may partition accounting or use specialized machinery for performance. Worker assignment, callback order,
partition choice, and reconciliation order must not change the final Budget result for the same established material.

Implementation inputs needed for feasibility are resolved before the governed boundary begins. Runtime conditions and
telemetry must not silently re-solve an active Budget.

Elapsed-time observations may differ because the physical environment differs. The backend must still preserve the
declared clock requirement, boundary law, guarantee, and result for the material that becomes established.

Optimization is allowed only after these conditions hold.

---

## 7. Verification

Contract verification must establish that the Budget is complete, finite, and lowerable. Subject, boundary, quantity,
unit, allowance, renewal, guarantee, and applicable world must resolve exactly. Distribution must either be absent or
satisfy the V1 fixed-distribution law.

The verifier must reject business values, event counts, implementation work units, hidden allowance establishment,
stage-local Budget copies, and unsupported distributed totals.

The accounting law must preserve one continuous position for the declared boundary. Tests must cover zero charge, exact
allowance, the smallest over-limit charge, renewal, concurrent charge, and arithmetic failure.

Time tests must cover both terminal orders:

```text
normal completion established first
    -> normal boundary result

deadline established first
    -> Contract-Progress Cutoff
    -> late success, State movement, and Publication rejected
```

Distribution tests must prove conservation and unchanged target limits throughout the active boundary.

Backend conformance is verified separately. It must show that lowering preserves canonical meaning, the quantity
capability proof is valid, unsupported strength fails closed, and optimization does not change the result.

Generated fixtures and property-based tests should exercise the closed contract material and the implementation
boundary.

---

## 8. Deferred Decisions

The following remain open:

- final Java or Kotlin Budget syntax and explicit absence syntax,
- canonical identity bytes,
- exact JVM support proof for cumulative allocated bytes,
- processor-time and I/O-byte quantities,
- authoritative multi-runtime accounting,
- profiling Publication shape,
- Diagnostic linkage,
- Failure mapping,
- and Version qualification.

Capacity is decided in ADR-0052. Governance and Policy are decided in ADR-0053 and ADR-0054.

---

## 9. Consequences

### Positive

Budget becomes an explicit user-machine contract rather than a runtime setting. The declaration stays independent from
measurement and enforcement machinery.

Elapsed-time Budget can provide a strong contract result even when the JVM cannot physically stop arbitrary code: an
expired execution cannot establish later contract-visible consequences.

Budget and Capacity can be combined at Kontrakt-controlled boundaries. Capacity controls current admission or holding;
Budget controls cumulative consumption and later authority.

One Operation-scope Budget remains continuous across the one-dimensional pipeline. Fixed distribution preserves one
total without copying allowance into stages, workers, or concurrent executions.

V1 now has a closed guarantee vocabulary and a finite distribution law.

### Negative

The backend must own enough of the execution boundary to prove the declared quantity and guarantee. Opaque user code,
partial JVM metrics, native work, external systems, or several runtimes may make a declaration unsupported.

Cumulative allocated-byte support requires a stronger proof than the existence of a JVM metric.

A whole-machine Budget must include Kontrakt and generated work inside the declared boundary. Internal cost cannot be
hidden outside the total.

### Neutral

This ADR does not prohibit implementation mechanisms needed to observe or enforce Budget. It prevents them from becoming
the source of Budget meaning.

Profiling may support later Budget revision, but it does not modify an active Budget. Publication, Diagnostic, and
Failure remain separate authorities.