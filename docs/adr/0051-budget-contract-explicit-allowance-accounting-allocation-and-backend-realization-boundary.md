# ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary

## Status

Accepted

## Date

2026-08-03

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0054: Policy Contract, Explicit Operating Policies, Self-Contained Contract Worlds, and Interface Binding Boundary
- ADR-0056: Governance Contract, Policy-World Control, Whole-Machine Coordination, and Selection Boundary
- ADR-0052: Capacity Contract and Admission Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror
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

This ADR also fixes the V1 Kotlin Budget authoring form and enclosing-interface binding law in Section 5. Java parity,
explicit Budget-absence spelling, and backend API details remain deferred.

---

## 4. Budget Contract

### 4.1. Meaning and Scope

In V1, one Budget declaration belongs to exactly one enclosing Interface. Its subjects may refer only to exact
Interactions and exact one-dimensional Contracts established within that Interface. Budget authority does not cross
Interface boundaries.

Within that Interface, a Budget governs one application of one exact contract subject.

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
allowance magnitude
allowance unit
applicable contract world
```

The exact subject fixes the measurement boundary. The quantity fixes what is limited. Magnitude and unit together
establish the allowance. Different source units that denote the same exact quantity lower to the same canonical
allowance.

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

### 4.4. Allowance Magnitude and Units

A Budget allowance is an exact non-negative magnitude expressed in one exact unit. Zero is valid and means that the
subject may consume none of that quantity. Absence of a Budget is different: it means that Budget does not limit that
`(subject, quantity)` pair.

V1 accepts finite integer, decimal, and scientific-notation magnitudes such as `0`, `1.5`, and `1e-12`. Negative values,
`NaN`, infinity, runtime-computed values, and values whose exact meaning cannot be established at definition time are
invalid. The compiler uses the existing deterministic exact-number canonicalization rules; Budget does not introduce
floating-point contract authority.

Kontrakt provides units as exact nominal library symbols. Unit names do not encode the magnitude, and users do not
define Budget units by convention, inheritance, or string parsing.

For time, the V1 JVM frontend provides SI-prefixed second units from nanoseconds upward:

```text
Nanoseconds
Microseconds
Milliseconds
Centiseconds
Deciseconds
Seconds
Decaseconds
Hectoseconds
Kiloseconds
Megaseconds
Gigaseconds
Teraseconds
Petaseconds
Exaseconds
Zettaseconds
Yottaseconds
Ronnaseconds
Quettaseconds
```

`Nanoseconds` is the smallest V1 authoring unit. This does not promise nanosecond measurement resolution or nanosecond
stop latency. A selected backend must still prove that it can preserve the declared allowance; otherwise compilation
fails under Section 4.8.

Smaller SI time units remain valid future extensions of the unit vocabulary:

```text
Picoseconds
Femtoseconds
Attoseconds
Zeptoseconds
Yoctoseconds
Rontoseconds
Quectoseconds
```

They are not exposed by the V1 JVM frontend. They may be introduced when a backend can preserve their `Relative Elapsed
Time` contract meaning.

Exact-duration units used in science, engineering, industry, economics, and ordinary life are also provided when their
duration is fixed, including:

```text
Minutes
Hours
Days
Weeks
Fortnights
JulianYears
JulianCenturies
```

Calendar and economic periods may exist in the wider Kontrakt unit vocabulary, for example `CalendarMonths`,
`CalendarQuarters`, `CalendarYears`, and `BusinessDays`. They are contextual periods rather than fixed elapsed durations
and are not valid units for `Relative Elapsed Time`. A contract that uses them must establish the calendar or
business-calendar authority separately.

For memory, Kontrakt provides both decimal SI byte units and IEC binary byte units. Decimal units extend from `Bytes`
through
`Quettabytes`; binary units extend from `Kibibytes` through `Quebibytes`. The two families remain distinct.

### 4.5. Relative Elapsed Time

`Relative Elapsed Time` is the actual time that passes between the subject boundaries. It includes execution, waiting,
scheduling delay, garbage-collection pause, and backend work inside the boundary. The cause of elapsed time may appear
in diagnostics, but it does not change the Budget quantity.

Only exact-duration units from Section 4.4 may express this allowance. Absolute timestamps and contextual calendar or
business periods are not `Relative Elapsed Time`.

The allowance is an inclusive maximum.

```text
established elapsed time <= allowance
    -> remains within allowance

allowance reached before the subject result is established
    -> Budget Stop
```

A result established exactly at the allowance remains valid. Once the allowance has been reached without a result, no
further contract progress is permitted.

### 4.6. Memory Use

`Memory Use` is the memory consumed by one application of the exact subject between its Budget boundaries. Its allowance
is an inclusive maximum, under the same law as `Relative Elapsed Time`.

```text
established memory use <= allowance
    -> remains within allowance

allowance reached before the subject result is established
    -> Budget Stop
```

An Interaction may declare an overall memory Budget, and any one-dimensional Contract may declare its own memory Budget.
The same independence, uniqueness, simultaneous-failure, and derived-bound rules in Section 4.7 apply without a separate
accounting or distribution law. Current machine availability remains Capacity.

### 4.7. Interaction and One-Dimensional Budgets

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

### 4.8. Compilation Failure

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

### 4.9. Budget Stop

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

### 4.10. Diagnostic Evidence

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

### 4.11. Budget and Capacity

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

### 4.12. V1 Boundary

V1 requires:

- one Budget declaration bound to exactly one enclosing Interface,
- one exact Interaction or one-dimensional Contract subject established within that Interface,
- `Relative Elapsed Time` or `Memory Use`,
- one finite non-negative exact magnitude and one exact unit forming an inclusive allowance,
- one applicable contract world,
- unique authority for each `(subject, quantity)` pair,
- compilation failure for invalid or unsupported material,
- and `Budget Stop` before further progress would exceed the allowance.

Distribution, renewal, user-selected accounting, enforcement levels, cancellation modes, and implementation-lifecycle
subjects are not part of V1 Budget material.

---

## 5. V1 User Authoring API and Processing Boundary

In V1, Budget is selected exactly once by one enclosing interface, following ADR-0047. The interface binding gives one
exact host declaration the Budget role for that Interface only. The selected Budget cannot govern subjects from another
Interface.

```text
interface CalculateContract {
    budget CalculateBudget

    ...
}
```

`CalculateBudget` is resolved as one exact source symbol. The binding does not select a Budget by annotation, package,
class name, inheritance, structural similarity, runtime lookup, or registration.

### 5.1. Kotlin Budget Declaration

The selected V1 Kotlin source is one uninstantiable class. Each direct nested entry class declares one exact Budget
entry inside that selected Budget source.

```kotlin
package example.calculate.contract

import io.kontrakt.contract.budget.Mebibytes
import io.kontrakt.contract.budget.MemoryUse
import io.kontrakt.contract.budget.Milliseconds
import io.kontrakt.contract.budget.RelativeElapsedTime

class CalculateBudget private constructor() {

    class InteractionElapsed private constructor(
        subject: CalculateInteraction,
        quantity: RelativeElapsedTime,
        allowance: Milliseconds = Milliseconds(100),
    )

    class InteractionMemory private constructor(
        subject: CalculateInteraction,
        quantity: MemoryUse,
        allowance: Mebibytes = Mebibytes(4_096),
    )

    class CanonicalizationElapsed private constructor(
        subject: CalculateCanonicalization,
        quantity: RelativeElapsedTime,
        allowance: Milliseconds = Milliseconds(10),
    )

    class CanonicalizationMemory private constructor(
        subject: CalculateCanonicalization,
        quantity: MemoryUse,
        allowance: Mebibytes = Mebibytes(256),
    )
}
```

`CalculateInteraction` denotes the exact frontend subject handle for the `calculate` Interaction selected from the same
interface contract. `CalculateCanonicalization` denotes the exact selected one-dimensional Contract. These subject
symbols are resolved at definition time; their simple names, package placement, runtime instances, and inheritance do
not create subject identity.

Neither `CalculateBudget` nor its entry classes are instantiated to obtain contract values. The entry constructor
parameters are not properties, and the `allowance` default expression is not runtime defaulting or Budget storage. These
host declarations are restricted source carriers that Kontrakt must completely refine and erase before canonical Budget
authority begins.

One direct entry declaration carries exactly four Budget coordinates:

```text
subject constructor-parameter exact type
    -> exact subject

quantity constructor-parameter exact type
    -> exact quantity

allowance constructor-parameter exact type
    -> allowance unit

numeric source literal inside the allowance default expression
    -> allowance magnitude
```

The direct nested entry class is a nominal source declaration handle. Its name does not encode the subject, quantity,
unit, magnitude, boundary, or canonical Budget identity. Renaming the entry without changing its four resolved
coordinates does not change canonical Budget meaning.

Because an entry is a genuine source symbol, another contract frontend may refer to that exact entry when its own scope
and binding law permits the reference. Definition-time resolution must still end at canonical Budget material; the host
class name does not become final Budget identity or survive as contract authority.

### 5.2. Typed Allowance Literal

The `allowance` coordinate is not a general default-parameter language. Each Budget entry declares exactly one
non-property `allowance` constructor parameter whose exact Kontrakt-owned unit type is paired with exactly one matching
typed default expression containing the Budget magnitude.

```kotlin
val allowance: Milliseconds = Milliseconds(100)
val allowance: Mebibytes = Mebibytes(256)
val allowance: Seconds = Seconds(1.5)
val allowance: Microseconds = Microseconds(2.5e3)
```

The frontend reads the exact parameter type and numeric source literal under the exact-number law in Section 4.4. It
does not obtain authority by invoking the entry constructor, applying Kotlin default-argument semantics, executing a
unit constructor, or evaluating a JVM floating-point result.

The following are rejected:

```kotlin
val allowance: Milliseconds = Milliseconds(BASE)
val allowance: Milliseconds = Milliseconds(50 + 50)
val allowance: Milliseconds = Milliseconds(readLimit())
val allowance: Milliseconds = Milliseconds(-1)
val allowance: Milliseconds = Seconds(1)
val allowance: Milliseconds = Milliseconds(100)
```

A missing allowance expression, a default expression on another coordinate, a property initializer, annotation argument,
parameter-name encoding such as `allowance_100`, or a predeclared numeric type such as `Milliseconds100` does not
establish an allowance.

The unit is the exact Kontrakt-owned nominal symbol named by the `allowance` parameter type. The magnitude is the exact
non-negative numeric material inside the matching typed default expression. No runtime defaulting, string parsing,
naming convention, or property identity is used to recover either coordinate.

### 5.3. Flat Declaration Law

One `CalculateBudget` declaration contains its Budget entries as direct nominal nested classes. Those entry classes form
one flat manifest level; V1 does not repeat them through a second membership declaration.

The selected Budget source therefore has one level of entries:

```text
CalculateBudget
    InteractionElapsed
    InteractionMemory
    CanonicalizationElapsed
    CanonicalizationMemory
```

The outer `CalculateBudget` is the only Budget declaration selected by the enclosing Interface. A direct entry class is
one row of that Budget declaration, not a nested Budget or an independently selectable one-dimensional Contract. An
entry cannot contain another Budget entry. User-defined membership wrappers, inherited Budgets, and recursive contract
composition are not admitted.

Each entry constructor must declare exactly the fixed `subject`, `quantity`, and `allowance` coordinates as non-property
parameters. The subject type must resolve to one exact Interaction or one exact one-dimensional Contract governed by the
same enclosing interface. The quantity type must resolve to one V1 Budget quantity. The `allowance` parameter must use
one exact Kontrakt-owned unit type and one valid typed default expression under Section 5.2. No other constructor
parameter or default expression is admitted.

One exact `(subject, quantity)` pair may appear only once, regardless of entry class name.

### 5.4. Prohibited Authoring Authority

V1 Budget authoring obtains no contract meaning from:

```text
val or var constructor properties
properties or field initializers
non-entry members inside the selected Budget source
annotations
default expressions except the required allowance typed literal
functions or executable Budget members
overload resolution
entry-name conventions
parameter-name value encoding
numeric-per-value unit types
inheritance or subtype discovery
entry nesting beyond the one direct manifest level
runtime instances
callbacks or lambdas
environment reads
runtime registration
reflection order
package scanning
backend configuration
```

The selected Budget class contains only direct Budget entry declarations, and those entries contain only the required
constructor declaration. In particular, branching, loops, helper calls, lookup, mutable state, and arbitrary expressions
are rejected from the Budget definition path. The only admitted default expression is the exact typed allowance literal
defined in Section 5.2.

### 5.5. Refinement and Erasure

The host declaration is source evidence, not final authority.

```text
interface-scope Budget binding
    -> exact Budget source symbol
    -> exact direct entry source symbols
    -> exact subject / quantity / magnitude / unit material
    -> validation
    -> canonical Budget Contract
    -> backend capability proof
    -> lowered enforcement
```

Definition-time references to an exact Budget entry must resolve before host material is erased. After refinement,
Kontrakt erases the selected class, direct entry class names, private constructors, constructor parameters,
default-argument mechanics, typed-literal call shape, and runtime unit objects. Canonical identity and applicable
contract-world material are completed from already established authority.

User code does not implement Budget through reporting calls, callbacks, environment reads, scheduler settings, or
backend tuning. Physical observation and stop machinery remain backend realization under Section 6.

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
`Budget Stop`. The presence of a unit in the V1 authoring vocabulary does not itself prove that every allowance
expressed in that unit is realizable. A nanosecond declaration may still fail compilation when the selected JVM backend
cannot preserve its measurement and stop obligations. Failure is handled by the compilation rule in Section 4.8.

### 6.4. Deterministic Realization

Determinism is an implementation law, not a Budget coordinate or user option.

For the same canonical Budget, implementation inputs, and established quantity observation, lowering and judgment must
produce the same contract-visible result. Physical conditions may change the observation; they do not change the
comparison law or allow runtime telemetry to rewrite the active Budget.

Optimization is allowed only after these conditions hold.

---

## 7. Verification

Contract verification must establish that the Budget is complete, finite, unique, and lowerable. The exact subject,
quantity, allowance magnitude, allowance unit, and applicable world must resolve without ambiguity. Equivalent
exact-unit presentations must canonicalize to the same allowance.

The verifier must reject duplicate `(subject, quantity)` authority, subjects outside the enclosing Interface,
implementation-lifecycle subjects, hidden allowance establishment, unsupported backend capability, and values that
belong to Capacity or another contract.

Authoring verification must also establish that the enclosing interface selects one exact Budget source, the selected
Kotlin class is uninstantiable and contains only direct uninstantiable Budget entry classes. Each entry constructor must
declare exactly the non-property `subject`, `quantity`, and `allowance` parameters. The subject and quantity types must
resolve to permitted exact symbols. The allowance type must resolve to one permitted exact unit symbol and must carry
exactly one matching typed default expression containing the exact non-negative magnitude. Constructor properties,
additional parameters, properties or fields, functions, annotations, other default expressions, overload-based meaning,
name-encoded values, deeper entry nesting, arbitrary expressions, and runtime value acquisition must be rejected.

Elapsed-time tests must cover:

```text
Interaction start at Input Presentation establishment
one-dimensional start at required-material establishment
result before allowance
result exactly at allowance
Budget Stop when allowance is reached first
simultaneous Interaction and one-dimensional Budget Stops
```

Elapsed-time unit tests must cover the V1 SI-prefixed second family from `Nanoseconds` upward, fixed exact-duration
units, equivalent-unit canonicalization, zero allowance, decimal and scientific magnitudes, rejection of sub-nanosecond
units by the V1 JVM frontend, and rejection of contextual calendar or business periods for `Relative Elapsed Time`.

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

- Java parity and explicit Budget-absence syntax,
- exact public spelling and generation of Interaction subject handles,
- canonical identity bytes,
- additional machine-resource quantity profiles,
- exact JVM realization proof for `Memory Use`,
- realization across several runtimes,
- profiling Publication shape,
- Diagnostic linkage,
- Failure mapping,
- and Version qualification.

Capacity is decided in ADR-0052. Policy and Governance ... ADR-0054 and ADR-0056

---

## 9. Consequences

### Positive

Budget becomes an explicit limit on one exact contract application rather than a runtime setting. Interaction and
one-dimensional limits can coexist without allocation, inheritance, or hidden override.

Compile-time rejection prevents contradictory or unrealizable Budget declarations from becoming executable software. A
runtime overrun has one explicit result, exact attribution, and complete pipeline diagnostics.

The user contract remains independent from clock, allocation instrumentation, cancellation, scheduler, process, and
retry mechanisms. The V1 Kotlin surface expresses each Budget entry once as a direct nominal row inside one selected
Budget declaration, without executable Budget methods, annotation authority, value-name parsing, or a second membership
structure. Exact entries remain referable as source symbols while canonical Budget identity stays independent from host
class names.

### Negative

The backend must control enough of the contract boundary to preserve `Budget Stop`. Opaque user code, native work,
external systems, or several runtimes may make a declaration unsupported.

Physical conditions may change whether one data flow reaches a time or memory allowance. This does not change the
contract comparison law.

### Neutral

Profiling may support a later contract revision, but it does not modify an active Budget. Capacity, Publication,
Diagnostic, Failure, Governance, and Policy retain their own authority.