# ADR-0051: Budget Contract, Explicit Allowance, Accounting, Allocation, and Backend Realization Boundary

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

---

## 1. Context

A real machine cannot spend without limit.

Its work consumes finite time, memory, energy, bandwidth, material, and other quantities. A machine that does not
declare those limits leaves its actual operating boundary inside configuration, counters, schedulers, allocators,
timeout code, and other realization mechanisms.

Budget belongs in contract theory because the finite allowance is part of what the machine is allowed to do, not merely
a backend setting used to protect one implementation.

```text
Budget
    declares finite consumable allowance
```

A Budget identifies the governed subject, the counted quantity, the applicable boundary, the allowance established
before consumption, and the judgment produced when charge material is applied.

Budget remains independent from Capacity, Governance, and Policy. Capacity decides what may be admitted or held,
Governance establishes the applicable contract world, and Policy selects a prepared response contract. This ADR decides
only Budget.

This ADR establishes Budget meaning, explicit allocation, accounting position, quantity boundaries, judgment, required
guarantee, and the separation between canonical Budget material and backend realization.

---

## 2. Problem

A software limit is often represented only by a configuration value, runtime counter, callback, scheduler setting,
allocator choice, timeout, operating-system control, or user-written `consume` call. Those mechanisms may enforce one
implementation, but they do not by themselves explain whose allowance exists, when it begins, what quantity it limits,
how consumption is attributed, how the position changes, or what guarantee the machine owes.

Budget must describe the finite resource obligation of the user's machine, not merely the resources used internally by
Kontrakt.

The user's project is itself a machine. It may declare a Budget over the whole system, one Operation, one run, one
session, or another explicit machine boundary. Kontrakt must then measure, account, judge, and control that Budget
through a suitable realization.

Budget must be explicit. Kontrakt may derive an internal allocation plan inside a declared allowance, but it may not
invent, enlarge, or silently activate a Budget that the contract did not declare.

The limit is contract. Clocks, counters, allocators, process isolation, compiler instrumentation, generated checkpoints,
schedulers, and operating-system controls are realization.

Budget must not be defined by algorithmic complexity, backend loop count, hash probes, graph visits, compiler passes, or
another implementation-specific work unit. Such material may remain realization fuel. It becomes user Budget charge
material only when the contract world itself declares the counted event or quantity.

---

## 3. Decision

### 3.1. Budget Foundation and Separate Contract Scope

This revision decides the basic Budget Contract.

```text
Budget
    declares explicit finite consumable allowance
    allocated to one declared machine boundary
    before governed work may consume it
```

The Budget belongs to the user's machine. It may apply to the whole project, one Operation, one run, one session, or
another declared subject. It is not limited to Kontrakt's compiler or runtime machinery.

Kontrakt owns the realization work needed to enforce the declaration.

```text
User contract
    establishes the subject, quantity, boundary, allowance, and required guarantee

Kontrakt realization
    establishes the Budget position
    derives any permitted internal allocation
    measures or establishes charge material
    performs accounting
    produces Budget judgment
    realizes the required guarantee
```

This decision does not define the later Java or Kotlin authoring surface. It defines the contract material that any
later authoring surface must be able to present.

Budget is explicit. Kontrakt must not infer that a machine has a Budget merely because resources are finite or
measurable. A selected Budget slot must establish one exact Budget or explicit absence. The final absence syntax remains
deferred.

This revision does not create separate `Deadline`, `Windowed`, `Aggregate`, `CurrentHeld`, `Concurrent`, or
`GrowthBound`
families.

```text
elapsed-time limit
    quantity + boundary + allowance

fixed or rolling window
    renewal law

parent and child allowance
    allocation and composition law

current held amount and concurrent count
    Capacity quantity

growth refusal
    Capacity admission judgment
```

Reservation remains a possible protocol extension because `reserve -> commit or release` changes accounting position
before consumption. Its V1 inclusion remains deferred.

Capacity, Governance, and Policy remain independent contracts. This ADR does not define their material or judgment laws.

---

## 4. Budget Contract

### 4.1. Budget Meaning

A Budget Contract declares explicit finite consumable allowance allocated to one declared machine boundary before
governed work may consume the quantity.

```text
Budget Contract:
    the contract that establishes
    how much of one declared quantity
    one declared machine boundary may consume
```

The user's project is itself a machine. Budget therefore describes the finite resource obligation of that machine, not
merely the resources consumed internally by Kontrakt.

A Budget may apply to:

```text
the whole user system

one interface machine

one Operation execution

one run

one session

one declared child machine boundary
```

The subject must be explicit. Package placement, thread ownership, call-stack position, process identity, classloader,
current worker, or backend registration must not silently decide the subject.

Budget is not algorithmic complexity.

```text
Not Budget Contract material
    O(n)
    O(n log n)
    loop iteration count
    hash probe count
    graph visit count
    compiler pass count
    machine instruction count
```

Those describe or meter a realization. They do not declare the user's machine obligation merely because they are finite.

Budget may count a declared machine event only when the event itself belongs to the contract world and has the same
meaning across admissible realizations.

```text
Allowed candidate
    one declared RetryAttempt

Forbidden as user Budget
    one backend loop iteration
```

### 4.2. Explicit Allocation Before Consumption

Budget is based on explicit allocation.

```text
Explicit Budget Declaration
    ->
Established Budget Position
    ->
Governed Work May Consume
```

The applicable allowance must be established before governed work begins. Budget is not created after execution by
observing what the implementation happened to consume.

No Budget is implicit.

Kontrakt may support an explicitly selected named mode or an explicitly delegated automatic resolution surface in a
later authoring model. Such a surface must still resolve to exact canonical Budget material before authority begins. A
backend must not inspect the host and silently create, enlarge, or replace a Budget.

Contract allocation and physical reservation are different.

```text
Contract allocation
    establishes the allowance position
    that the machine is permitted to consume

Physical reservation
    secures physical resources
    through one realization mechanism
```

Every Budget requires contract allocation. Physical reservation is a realization technique that may be used when the
resource and backend permit it.

A time allowance can be allocated before execution even though time itself is not stored in advance. A memory
realization may reserve an arena, process limit, region, or another bounded resource before execution. The difference
does not change Budget meaning.

If the required enforcement cannot be realized for the selected backend, Kontrakt must reject that realization or report
the unsupported guarantee. It must not weaken the Budget to fit the backend.

### 4.3. Canonical Budget Material

One Budget requires at least the following canonical material:

```text
Budget identity

subject

boundary

quantity and unit

allowance

allocation law

accounting law

renewal law

attribution law

required guarantee

applicable contract world
```

The fields answer different questions.

```text
subject
    whose Budget is this?

boundary
    when does governed accounting begin and end?

quantity and unit
    what is consumed and in what unit?

allowance
    how much may be consumed?

allocation law
    where does the allowance come from,
    who owns it,
    and may it be partitioned or returned?

accounting law
    how does one established charge change the Budget position?

renewal law
    when and how does a new allowance become available?

attribution law
    which child work, generated machinery,
    runtime work, and external work belong to this Budget?

required guarantee
    what must every admissible realization guarantee?

applicable contract world
    under which declared world does this Budget have authority?
```

These are contract coordinates. They define Budget meaning and required results. They do not prescribe the clock,
allocator, counter, scheduler, isolation boundary, or other machinery used to realize them.

These coordinates must not be replaced by a collection of named Budget families. A deadline, time window, parent quota,
or current occupancy does not become a new family merely because the same fields take different values.

The exact authoring syntax and canonical identity bytes remain deferred.

### 4.4. Budget Quantity Sources

Budget charge material may come from two sources.

```text
Kontrakt-measured physical quantity

Contract-established declared quantity
```

A physical quantity is admissible only when Kontrakt can define its subject, boundary, unit, attribution, accounting,
and enforcement meaning without requiring user implementation code to carry Budget control flow.

A declared quantity is admissible only when the contract world itself establishes the event or amount. The user may
declare a finite Budget over retries, requests, publications, cases, monetary amounts, or another explicit quantity when
one unit has a stable contract meaning.

```text
Declared quantity
    RetryAttempt
    ExternalRequest
    PublishedFact
    VerificationCase
    PaymentAmount

Not declared quantity
    internal loop
    optimizer visit
    private cache access
    hidden repository call
```

A generic `step` is not automatically Budget material.

A step may become charge material only when the contract declares exactly what one step means. Kontrakt's existing
physical or semantic planner steps remain realization fuel unless a separate user-machine contract explicitly gives an
event that meaning.

### 4.5. Time Quantity

Actual elapsed time is a valid Budget quantity.

```text
subject
    one declared machine boundary

boundary
    explicit start
    -> explicit end

quantity
    elapsed time

allowance
    declared duration
```

For example:

```text
one Operation execution
from Admission establishment
through terminal result establishment
may consume at most 100 ms of elapsed time
```

`Deadline` is not a separate accounting family. It is an elapsed-time quantity with an explicit boundary and allowance.

Different time boundaries remain different Budget declarations.

```text
queue wait

one execution attempt

all retries of one Operation

no-progress interval

whole machine run
```

The final V1 time catalog remains to be closed. This ADR establishes actual elapsed time as required Budget material and
does not yet decide whether processor time, no-progress time, or another time quantity belongs in V1.

Physical observations may differ across executions because the environment differs. Determinism means that the clock
source, boundary, attribution, unit conversion, accounting law, and judgment are fixed by explicit material.

```text
same established elapsed-time charge
same established Budget position
same contract world
    -> same Budget judgment
```

### 4.6. Memory Quantity

Actual memory use may be declared, but `memory` alone is not a complete quantity.

The declaration must distinguish the accounting meaning.

```text
cumulative allocated bytes
    bytes allocated during one declared boundary
    and added to a consumable Budget position

current live bytes
    bytes currently held at one position

peak live bytes
    maximum current live bytes observed during one boundary

retained bytes
    bytes still held after a declared boundary
```

Cumulative allocated bytes have Budget form because they accumulate as consumption over the boundary.

Current live, peak live, and retained bytes have Capacity form because they describe what the machine holds or may hold
at one position. Their complete law remains deferred to Capacity.

A whole-system memory declaration must account for the whole declared subject.

```text
whole user system
    includes user implementation
    Kontrakt runtime machinery
    generated guards and adapters
    cache
    diagnostics
    declared child work
```

A declaration may define a narrower subject, but it must not call itself a whole-system limit while silently excluding
Kontrakt or generated machinery.

The backend may realize the memory obligation through a bounded arena, controlled allocator, generated layout, isolated
process, operating-system limit, static reservation, or another mechanism. Those mechanisms are implementation.

### 4.7. Accounting and Renewal

The basic Budget accounting relation is cumulative consumption.

```text
Established Budget Position
+ Established Charge
    ->
Next Budget Position

or

Would Exceed Budget
```

The exact remaining amount need not be a public user-facing result. The canonical Budget position must nevertheless
retain enough material for the next judgment to be deterministic.

Accounting law and renewal law are separate.

```text
accounting law
    how charge changes the current position

renewal law
    when a new allowance position is established
```

Possible renewal values may include:

```text
one shot

per execution

per run

per session

fixed window

rolling window

periodic refill

no renewal
```

`Windowed` is not a separate Budget family. It is one renewal law.

This ADR does not yet admit arbitrary user-defined refill algorithms. Renewal must remain finite, explicit, inspectable,
and lowerable.

### 4.8. Allocation and Composition

A Budget may be allocated from a larger Budget.

This is not merely attribution. Allocation changes who may consume which part of an already finite allowance.

```text
Project Budget
    ->
Operation Budget
    +
Kontrakt Runtime Budget
    +
Cache Budget
    +
Diagnostic Budget
    +
Unallocated Headroom
```

Allocation must preserve allowance.

```text
sum of active child allocations
+ unallocated parent allowance
    <= parent allowance
```

The compiler may derive an internal allocation plan from the complete contract graph, selected backend, environment
material, and workload evidence. It may not exceed or silently redefine the declared parent allowance.

Allocation law must make at least the following explicit:

```text
allocation source

owner of the allocated allowance

partition rule

whether unused allowance returns

when it returns

whether borrowing is permitted

whether child consumption also changes the parent position

how double counting is prevented
```

A whole-system Budget gives Kontrakt authority to allocate inside the declared total. It does not give Kontrakt
authority to invent a larger total.

The internal allocation plan is realization material derived under Budget authority. The declared total, subject,
quantity, boundary, and allocation law remain contract material.

### 4.9. Reservation as a Possible Accounting Extension

Reservation is not merely a future admission time.

A reservation changes the available position before final consumption.

```text
Available
    -> Reserved
    -> Consumed

or

Available
    -> Reserved
    -> Released
```

This is a real accounting extension because `reserve`, `commit`, and `release` have different effects.

Reservation is not established as a separate Budget family. It is a possible extension to the accounting protocol.

This ADR does not yet decide whether V1 requires reservation. The decision must be based on concrete Budget and Capacity
cases that cannot be represented safely by direct allocation and consumption.

### 4.10. Budget Judgment

Budget judgment must be typed.

The basic judgment form is:

```text
Established Budget Position
+ Established Charge
+ Applicable Contract World
    ->
BudgetWithinAllowance
    + Next Budget Position

or

BudgetWouldExceed
```

`BudgetWithinAllowance` does not perform the work. It means only that the established charge may be accepted under the
current Budget position.

`BudgetWouldExceed` belongs to Budget. It must not be reported as Capacity refusal, State refusal, Invariant violation,
or an unnamed runtime exception.

Policy may select a prepared response contract after reading the Budget judgment. Policy may not reverse the Budget
result or manufacture new allowance.

The exact Failure and Diagnostic mapping remains deferred.

### 4.11. Budget and Implementation

The user declares the machine limit. Kontrakt owns the machinery that realizes it.

```text
Forbidden user obligation
    budget.consume()
    budget.remaining()
    budget.checkpoint()
    budget.isExhausted()
```

Such calls make Budget meaning depend on where implementation chose to report work.

Kontrakt may realize Budget through:

```text
generated accounting

compiler instrumentation

controlled allocation

static reservation

adapter-owned I/O accounting

scheduler boundary

isolated process

operating-system containment

post-execution measurement when that is the declared guarantee
```

A realization must declare the guarantee it can provide. Pre-execution refusal, hard during-execution stop, cooperative
stop, and post-execution judgment are not interchangeable.

If the declared Budget requires hard control and the selected realization cannot provide it, that realization is
inadmissible.

Opaque user code does not weaken the contract. It may require analysis, instrumentation, containment, a different
backend, or rejection.

### 4.12. Budget and Capacity

Budget and Capacity remain separate.

```text
Budget
    finite allowance consumed across a declared boundary

Capacity
    finite amount the machine may currently hold, admit, or keep active
```

Examples:

```text
total allocated bytes during one Operation
    Budget

live bytes currently held
    Capacity

requests accepted during one hour
    Budget

requests active at the same time
    Capacity

bytes written during one run
    Budget

temporary bytes currently retained
    Capacity
```

Current-held amount, concurrent count, and growth refusal are not separate Capacity families. They are Capacity
quantities and the Capacity admission judgment.

The complete Capacity position, acquisition, release, reservation, and admission laws remain deferred.

### 4.13. Budget Determinism

Budget authority must be repeatable from canonical material.

```text
same Budget
same established Budget position
same established charge
same applicable contract world
    -> same Budget judgment
```

A physical measurement backend may observe different charge values in different executions. It must not change what is
measured, where the boundary lies, whose consumption is counted, how units are converted, or how judgment is produced.

Hidden environment values, thread timing, classpath order, callback order, allocator identity, or backend-private
counters must not redefine Budget material.

### 4.14. V1 Budget Boundary

V1 must require explicit Budget authoring. Budget must not be an implicit runtime mode. The exact authoring surface
remains deferred.

The required V1 foundation is:

```text
one explicit Budget or explicit absence

explicit subject

explicit boundary

explicit quantity and unit

explicit allowance allocated before governed work

deterministic accounting position and judgment

explicit renewal law

explicit attribution law

explicit required guarantee

whole-system allocation support when the declared subject is the whole user system
```

V1 must support actual elapsed-time Budget.

V1 must support an actual memory Budget form whose quantity and accounting law are exact. Cumulative allocated bytes are
the current Budget candidate; live and retained memory belong to the Capacity decision that follows.

V1 may support contract-established declared charge kinds. A generic implementation step counter is not sufficient.

The inclusion of processor time, I/O bytes, monetary quantities, domain-specific charge kinds, reservation, and
additional physical quantities remains deferred until their measurement, attribution, accounting, and enforcement laws
are closed.

---

## 5. Authoring and Processing Boundary

This draft does not fix the Java or Kotlin Budget authoring API.

The later authoring surface must follow ADR-0047.

```text
Host declaration
    carries finite source material

Resolution
    identifies exact Budget, subject, quantity,
    boundary, allowance, and referenced coordinates

Validation
    rejects unsupported shape, behavior, ambiguity,
    hidden allocation, and unsupported guarantees

Canonicalization
    removes host-language and source-order authority

Lowering
    produces machine-usable Budget material

Backend
    realizes measurement, accounting, judgment,
    allocation, and enforcement without owning meaning
```

Any later Budget authoring surface must be able to carry at least:

```text
one exact Budget or explicit absence

subject

boundary

quantity and unit

allowance

allocation source and composition law where applicable

renewal law

attribution law

required guarantee
```

The following forms are rejected as Budget authority:

```text
BudgetContext passed into the Operation

consume() calls placed by user code

remaining() queries

user-written checkpoints

backend loop count presented as machine Budget

algorithmic complexity declaration

runtime configuration silently activated as Budget

environment-derived allowance with no explicit Budget declaration
```

Those forms may appear inside one realization. They may not define the user's Budget Contract.

A later Budget authoring design must answer:

```text
How is one Budget named?

How is the whole user system or one smaller subject selected?

How are start and end boundaries declared?

Which physical quantities belong to V1?

How is one contract-established charge kind declared?

How is actual elapsed time represented?

Which exact memory quantity is Budget and which belongs to Capacity?

How is a parent allowance allocated to child subjects?

How are unused allowance, return, borrowing, and double counting represented?

How is renewal declared without creating separate Budget families?

How is the required guarantee declared?

How is explicit Budget absence represented?

How does validation reject a realization that cannot provide the required guarantee?
```

---

## 6. Contract and Implementation Boundary

### 6.1. Decisions Made Here

This draft decides:

```text
Budget belongs in contract theory because a realistic machine must work within finite conditions.

Budget remains independent from Policy, Capacity, and Governance.

Budget declares explicit finite consumable allowance allocated to one declared machine boundary before governed work may
consume it.

Budget describes the finite resource obligation of the user's machine, not merely the resources used internally by
Kontrakt.

No Budget is implicit.

The user declares the limit. Kontrakt owns measurement, accounting, judgment, allocation realization, and enforcement.

Budget does not require BudgetContext, consume(), remaining(), or checkpoint() calls inside user Operation code.

Actual elapsed time is valid Budget quantity material.

Actual memory use is valid only after the exact accounting meaning is declared.

Cumulative allocated bytes have Budget form.

Current live, peak live, and retained bytes have Capacity form.

A generic implementation step is not user Budget.

A declared machine event may become Budget charge material when one unit has stable contract meaning across admissible
realizations.

Deadline is elapsed-time quantity plus boundary and allowance, not a separate family.

Fixed and rolling windows are renewal laws, not separate families.

Parent and child allowance require an explicit allocation and composition law.

Allocation must preserve the declared parent allowance and prevent double counting.

A whole-system Budget includes Kontrakt runtime and generated machinery unless a narrower subject is explicitly declared.

Reservation is a possible accounting protocol extension, not a separate Budget family.

BudgetWithinAllowance and BudgetWouldExceed are Budget judgments.

Budget refusal must not be collapsed into Capacity refusal, State refusal, Invariant violation, or an unnamed runtime
exception.

Ordinary software Budget systems are examples and warnings, not the source of the general definition.
```

### 6.2. Decisions Not Made Here

This draft does not decide:

```text
the final Budget Java or Kotlin authoring surface

the final V1 physical quantity catalog beyond elapsed time
and the current cumulative allocated-byte candidate

whether processor time, I/O bytes, monetary quantities,
or other physical quantities belong in V1

the exact clock, allocation, instrumentation,
or containment backend

the exact required-guarantee vocabulary

whether V1 requires reservation

the complete child-allocation, unused-return,
borrowing, and reclamation vocabulary

Capacity material, judgment, acquisition, release,
reservation, and admissibility law

Budget canonical identity bytes

Failure mapping

Diagnostic evidence and retention

Version qualification
```

### 6.3. No Hardware or Backend Authority

Physical capability, scheduler behavior, resource discovery, storage layout, cache state, thread count, allocator, clock
implementation, and deployment configuration do not define Budget meaning.

Actual elapsed time and actual memory may be Budget quantities. The hardware, clock, allocator, process boundary,
operating system, instrumentation, or generated machinery used to observe and control them remain realization.

They may establish explicit charge material under declared Budget authority. Replacing them must not change what the
contract means for the same canonical material.

If a replacement cannot provide the declared Budget guarantee, the replacement is inadmissible. The Budget is not
weakened to preserve backend compatibility.

---

## 7. Determinism and Verification

Kontrakt must be able to verify at least the following before Budget material becomes authoritative.

```text
Budget identity is unique in its declared scope.

One exact Budget or explicit absence is selected.

Subject and boundary resolve exactly.

Quantity and unit are exact.

Allowance is explicit and valid for the quantity.

No environment observation silently creates or enlarges the Budget.

Allocation source and owner resolve exactly.

Child allocations cannot exceed the parent allowance.

Unused allowance, return, and borrowing are explicit when present.

Parent and child accounting cannot count one charge twice.

Whole-system Budget attribution includes Kontrakt runtime and generated machinery unless a narrower subject is explicit.

Time Budget boundaries and clock semantics are exact.

Memory Budget declares the exact accounting meaning and does not hide Capacity material as Budget.

Contract-established charge kinds have stable declared meaning.

No backend loop, compiler step, cache access, or private counter acquires user-Budget authority.

Accounting and renewal laws are finite and lowerable.

Required enforcement is supported by the selected realization.

Budget judgment is typed and preserves the next canonical position.

The lowered accounting and enforcement plan is equivalent to canonical Budget material.
```

Verification should support generated fixtures and property-based tests over the closed material.

Budget verification should include at least:

```text
exact-boundary charge

zero charge

charge exactly equal to allowance

charge that would exceed by the smallest unit

renewal boundary

parent and child allocation conservation

unused allowance return

whole-system attribution

clean failure when the required enforcement is unavailable
```

Optimization is allowed only after meaning is preserved.

```text
Budget
    precomputed allocation plan
    static reservation
    specialized accounting
    fused measurement boundary
    generated limit checks
```

These are backend forms. If their result differs from canonical Budget material, the backend is wrong.

---

## 8. Deferred Decisions

The following Budget questions remain open for the next revision of this ADR.

```text
What exact source syntax declares one Budget and explicit Budget absence?

Which exact memory quantity belongs in V1 Budget?

Which live-memory and retained-memory quantities belong in Capacity?

Does V1 include processor time?

Does V1 include read and written bytes?

How are contract-established charge kinds declared without becoming arbitrary user counters?

What exact allocation vocabulary is sufficient for parent and child Budgets?

When does unused allowance return?

Is borrowing admitted in V1?

Does V1 require reserve -> commit or release?

What exact required guarantees are admitted?

How does a backend prove that it can provide the required guarantee?

Which Budget judgments and positions may participate in later Policy situations?

How are Budget identity bytes represented without making measurement machinery authoritative?
```

Capacity is decided separately in ADR-0052. Governance is decided separately in ADR-0053. Policy is decided separately
in ADR-0054.

Budget may return where Capacity acquisition, release, reservation, or Governance activation requires one exact shared
law. The basic Budget meaning, explicit allocation requirement, accounting position, and implementation boundary are
decided here.

---

## 9. Consequences

### Positive

Budget becomes a real user-machine contract rather than a Kontrakt runtime setting.

A user can treat the whole project, one Operation, one run, or another declared boundary as a finite machine and declare
the allowance that machine may consume.

The declaration remains small.

```text
User
    declares the limit and boundary

Kontrakt
    allocates, measures, accounts, judges, and enforces
```

Actual elapsed time and actual memory use can be represented without making clocks, allocators, schedulers, or operating
systems contract authority.

Budget no longer depends on where user implementation calls `consume` or `checkpoint`.

Deadline, window, aggregate quota, and current occupancy are not multiplied into overlapping families. Their differences
remain in quantity, boundary, allocation, renewal, attribution, and Capacity law.

A whole-system Budget can give Kontrakt one explicit total from which it derives internal allocation for runtime
machinery, generated code, caches, diagnostics, and user execution.

Parent and child Budgets preserve the declared total instead of copying the same allowance into every worker or
Operation.

Budget exhaustion remains attributable to Budget rather than being collapsed into Capacity, State, Invariant, or a
generic runtime exception.

The reason for Budget becomes explicit. A realistic machine accepts its limits and keeps its work inside the allowance
it can actually sustain.

### Negative

Kontrakt must own or control enough of the execution boundary to provide the declared Budget guarantee.

Actual time and memory support require exact boundary, attribution, measurement, and enforcement laws. A single
ambiguous
`time` or `memory` field is not sufficient.

Opaque user code may require compiler analysis, instrumentation, controlled allocation, process isolation,
operating-system containment, another backend, or rejection.

Whole-system Budget accounting must include Kontrakt runtime and generated machinery. Internal cost cannot be hidden
outside the user's declared system merely because Kontrakt produced it.

Parent and child allocation introduce conservation, return, borrowing, and double-counting problems that must be
verified.

The final V1 quantity catalog must remain small enough to implement honestly. Adding a measurable quantity without
stable attribution or enforcement would create false guarantees.

### Neutral

This ADR does not prohibit clocks, allocators, arenas, schedulers, process isolation, operating-system quotas, compiler
instrumentation, generated checkpoints, or runtime counters.

It prevents those mechanisms from becoming the source of Budget meaning.

Kontrakt may optimize allocation and enforcement aggressively after canonical Budget meaning is preserved.

Reservation remains open. Capacity remains to be completed before all shared acquisition and reservation questions can
be closed.