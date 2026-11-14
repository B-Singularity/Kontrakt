# ADR-0082: Dates, Times, and Time Zones

**Status:** Draft

**Date:** 2026-10-25

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0047 — One-Dimensional Contract Presentations and Source Carriers
- ADR-0051 — Budget Contract, especially §§4.3–4.9
- ADR-0052 — Capacity Contract, especially §§4.2–4.7
- ADR-0054 — Policy Contract
- ADR-0056 — Governance Contract
- ADR-0057 — Failure Contract
- ADR-0063 — Contract Establishment
- ADR-0064 — Input Contract, especially §7.9.5
- ADR-0065 — Admission Contract, especially §§7–10
- ADR-0066 — Canonicalization Contract
- ADR-0068 — Fact Contract
- ADR-0069 — Invariant Contract
- ADR-0071 — Resolved Contract HIR
- ADR-0073 — JVM Platform-Native Surface, especially §§7 and 9
- ADR-0075 — Compiler Result Ownership and Legal Consumption
- ADR-0078 — Deterministic Numeric Value and Operation Semantics
- ADR-0079 — Type Interpretation and Semantic Binding (*Accepted Frontend document*)
- ADR-0080 — Characters, Strings, and Unicode
- ADR-0081 — Collections and Structured Data
- ADR-0084 — One-Dimensional Standard-Library Semantic Acquisition
- `docs/design/overview/kontrakt-compiler-total-architecture-map-design-draft.md` — working design

---

## 1. Context

A Contract may use a date, compare two instants, or calculate a period without observing the running machine. Other
obligations depend on physical time. ADR-0051 already defines a `Relative Elapsed Time` Budget that measures the actual
interval between an exact subject's boundaries.

These uses cannot be interpreted as one operation merely because Java or Kotlin represents them with temporal types. A
local date-time does not identify an instant. A calendar day can differ from twenty-four elapsed hours. A Clock reading
depends on the machine or service providing it.

ADR-0064 §7.9.5 already defines the presentation meaning of nine temporal Input Leaves. ADR-0079 and ADR-0084 govern
interpretation of Java/Kotlin source operations. This ADR establishes the common temporal calculations needed across
Contract judgments without taking over either responsibility.

The physical observation of time also matters to the compiler and its generated machine. Reliable measurement is
necessary for resource enforcement and for engineering tests. This ADR states the semantic boundary that such
measurements must respect; it does not choose the measurement hardware or a runtime scheduling design.

## 2. Problem

A temporal operation can change its result because the selected callable has different rules, because the relevant
time-zone data changed, or because it reads the current environment. Treating these cases as ordinary deterministic
arithmetic permits the compiler to establish a result on an incomplete basis.

For example, adding one calendar day to a zoned date-time can cross a daylight-saving transition. Adding twenty-four
elapsed hours to the same instant can produce a different local time. Similarly, two offset date-times can denote one
instant while remaining unequal under their source `equals` operation.

There is a second problem at the execution boundary. A timeout returned to a caller does not prove that the underlying
operation stopped. A precise timestamp does not prove that a stop occurred at the timestamp. If a Budget depends on
either assumption, its judgment may be wrong even when the Clock is accurate.

Kontrakt therefore needs common temporal meaning that preserves the author's exact operation and keeps physical
measurement distinct from calculation. The existing 1D Contracts must remain responsible for deciding which temporal
values and operations they may use.

## 3. Decision

### 3.1. Common Meaning and Contract Authority

**Kontrakt distinguishes temporal values, temporal calculations, and observations of a running clock.** A common
calculation has a defined operand domain and result law. The same law and determining inputs produce the same legally
observable result. The chosen implementation cannot silently change that result.

This is shared calculation meaning, not a new Time 1D Contract. The owning 1D still establishes its own judgment and
Material. In particular, an interpreted duration does not establish a Budget observation, and a comparison of instants
does not establish an Admission or Governance result by itself.

A completed temporal datum may be interpreted without qualifying the operation that originally produced it. Using a
temporal operation inside governed Contract source is a separate decision under ADR-0073, ADR-0079, and ADR-0084. An
operation supported in user Realization is not thereby permitted in 1D authoring.

### 3.2. Temporal Value Distinctions

The following distinctions govern common calculations. When one of these values is an Input Leaf, its exact presentation
and Sameness remain those of ADR-0064 §7.9.5.

| Meaning                  | Required distinction                                                                                                             |
|--------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| ISO Date                 | A calendar date in the selected proleptic Gregorian domain. It has no time of day.                                               |
| Local Time               | A civil time of day. It has no date, offset, or instant.                                                                         |
| Local Date-Time          | A civil date and local time. It does not identify an instant.                                                                    |
| Numeric UTC Offset       | A fixed numeric displacement from the applicable UTC reference. It is not a named zone.                                          |
| Offset Date-Time         | A local date-time together with its offset. Both remain observable.                                                              |
| Instant                  | An exact coordinate on a specified time scale with a fixed epoch, range, and resolution.                                         |
| Duration                 | An exact amount of elapsed time under a fixed unit and resolution law.                                                           |
| Zone Descriptor          | An identified zone descriptor. It does not contain or assert the current zone rules.                                             |
| Resolved Zoned Date-Time | A local date-time, offset, and Zone Descriptor whose retained distinctions remain observable.                                    |
| Calendar Period          | A calendar-relative amount whose application requires a calendar and a temporal starting context. It is not an elapsed Duration. |

The first nine meanings are already defined for Input by ADR-0064. **Calendar Period is a common calculation subject
proposed here**, not an additional V1 Input Leaf. Supporting it does not amend the Input presentation inventory.

An Instant and a Duration are different kinds of quantity. Neither is an absolute or relative measurement merely because
a host type has that name. A `TimeMark` or other reading tied to a running clock is also not a transferable Instant
unless a qualified conversion establishes the relevant time scale and correlation.

No universal date-time wrapper or JVM representation is required by these meanings.

### 3.3. Epochs, Time Scales, and Units

The origin and scale of an Instant determine the interpretation of its coordinate. A nanosecond count without that
context is not enough to establish an Instant. An offset alone does not establish the rules for a named zone.

A fixed-duration unit has an exact conversion factor within its supported domain. Calendar units such as months or
calendar years do not have a constant duration in seconds. A calendar day is likewise distinct from an elapsed interval
across a time-zone transition.

An operation converting between time scales must have a qualified law for that conversion. This includes any leap-second
treatment required by the selected source semantics. Java `java.time.Instant` and Kotlin `kotlin.time.Instant` are not
assumed interchangeable merely because both expose epoch-based coordinates; their specified range, time-scale behavior,
and conversion rules must be examined for the selected profiles.

ADR-0064 fixes the existence of the Input Instant epoch, range, unit, and resolution determinants. This ADR does not
substitute the host's default epoch for an unresolved Kontrakt determinant. A calculation that needs a determinant
cannot proceed until the applicable law has fixed it.

### 3.4. Calendar Arithmetic and Elapsed-Time Arithmetic

The exact source operation determines the arithmetic. Calendar arithmetic modifies calendar fields under its selected
rules. Elapsed-time arithmetic changes an instant by an exact Duration.

Consider a zoned date-time at noon in New York on 2024-03-09. Under the relevant New York rules:

| Source operation           | Result                        | Elapsed interval |
|----------------------------|-------------------------------|------------------|
| Add one local calendar day | 2024-03-10 12:00 at UTC−04:00 | 23 hours         |
| Add 24 elapsed hours       | 2024-03-10 13:00 at UTC−04:00 | 24 hours         |

These operations are not interchangeable. An optimizer cannot substitute one for the other merely because their numeric
magnitudes both appear to be one day.

Every supported temporal operation must preserve its actual source law for field adjustment, normalization, evaluation
order, and failure. For example, adding one month to a date near the end of January may require an end-of-month
adjustment. Whether that adjustment occurs depends on the selected operation; it is not a universal Calendar Period
rule.

A date-only value can be compared as a date without first resolving it to an Instant. Conversely, calculating elapsed
time between two instants does not require converting them to local civil time.

### 3.5. Named Zones and Local-Time Resolution

A Zone Descriptor and the rules used to resolve it are separate semantic inputs. Named-zone resolution requires rules
applicable to the local date-time or instant being interpreted.

Local-time resolution has three relevant cases:

| Case                | Available offset interpretations | Consequence                                                                           |
|---------------------|----------------------------------|---------------------------------------------------------------------------------------|
| Ordinary local time | One                              | The rules can determine one instant for the local date-time.                          |
| Gap                 | None                             | The source operation may reject the value or apply a specified adjustment.            |
| Overlap             | More than one                    | The source operation must determine how an offset is selected, retained, or rejected. |

For example, `2024-03-10T02:30` does not occur in `America/New_York`. Java `LocalDateTime.atZone` adjusts that local
time forward into the valid interval, whereas `ZonedDateTime.ofStrict` can reject an invalid local-time/offset
combination. The compiler must preserve the selected callable's behavior.

The resolved presentation must not be silently reinterpreted against the current TZDB each time it is observed. ADR-0064
expressly preserves its submitted date-time, offset, and descriptor. A later operation that checks consistency with zone
rules is a separate calculation with its own semantic basis.

Zone aliases, fixed offsets, named regions, and provider-defined zone identifiers must not be treated as interchangeable
without a qualified conversion or comparison law. Equality of descriptor spellings, equality of rules, and equality of
resulting instants answer different questions.

### 3.6. Comparison and Sameness

There is no universal temporal equality operator. The comparison used must be the one selected by the common law or the
exact source callable.

| Comparison                                 | What it establishes                                                                     |
|--------------------------------------------|-----------------------------------------------------------------------------------------|
| Instant coordinate equality                | Two Instants have the same coordinate under the same time-scale law.                    |
| Same-instant comparison                    | Two resolved temporal values denote the same Instant under the selected conversion law. |
| Local date-time equality                   | Their retained civil fields are equal; no instant is implied.                           |
| Presentation Sameness                      | Only the distinctions fixed by the owning presentation law are compared.                |
| Source `equals`, `compareTo`, or `isEqual` | The result of that exact Java/Kotlin operation.                                         |

For example, Java `OffsetDateTime.equals` can return `false` for `2024-01-01T00:00+09:00` and `2023-12-31T15:00Z`, while
`isEqual` returns `true`. Replacing one operation with the other changes the source meaning.

The result of a same-instant comparison does not revise Input Presentation Sameness. Nor does it authorize Fact
uniqueness, collection key identity, or Canonicalization equivalence. Those judgments belong to their existing owners.

### 3.7. Precision, Range, and Unsuccessful Calculation

Every supported value has a defined domain. Temporal calculations must preserve the applicable bounds and their actual
resolution. A decimal fraction of a second, an integer nanosecond field, and a measurement that merely reports
nanoseconds are not assertions of the same accuracy.

For each supported operation, the result law must account for the following when they affect a legal observation:

| Condition                   | Meaning to preserve                                               |
|-----------------------------|-------------------------------------------------------------------|
| Out-of-range date or time   | The source's rejection or other explicitly permitted outcome.     |
| Arithmetic overflow         | The chosen operation's exceptional or bounded result.             |
| Truncation or rounding      | Its direction, unit, and point of application.                    |
| Precision-losing conversion | The information lost and the exact resulting value.               |
| Invalid temporal text       | The selected parser's unsuccessful result and exception behavior. |
| Ambiguous zone resolution   | The selected resolver's actual choice or rejection.               |

Java `java.time.Duration` and Kotlin `kotlin.time.Duration` do not have identical domains. Kotlin supports an infinite
Duration and has arithmetic that can saturate. Converting Kotlin infinity to a very large finite Java Duration does not
establish value identity. A corresponding conversion back may lose information.

Likewise, a Kotlin `Instant` operation that clamps an out-of-range result cannot be replaced by a Java `Instant`
operation that throws. Source-observable failure is part of the result law and follows ADR-0057 for attribution.

A failed calculation does not yield a valid fallback temporal datum merely because the representation can store one.
Kontrakt must not repair invalid operands unless the selected operation actually defines that repair.

### 3.8. Temporal Text and Calendar Systems

Parsing and formatting are operations over Text and temporal values. They do not determine the meaning of Text itself,
which remains with ADR-0080.

A temporal parser can depend on its pattern and resolver rules. Locale or calendar data may also determine the result.
Parsing `2024-03-10` as an ISO date is therefore a different operation from parsing locale-dependent text under an
unspecified host default.

Formatting may erase distinctions needed for a later round trip. For example, a format that omits an offset cannot be
assumed to reconstruct the original Offset Date-Time. A pattern that emits only seconds can lose fractional precision.

ISO chronology does not silently authorize every `java.time.chrono` calendar. Non-ISO chronologies may be supported when
their operations and required semantic data are qualified. Their dates do not become ISO Input Leaves through a host
conversion unless the conversion has an exact lawful result.

### 3.9. Java/Kotlin Source Operations

A temporal source expression retains the semantics of its resolved callable. The frontend must distinguish an overload
that accepts a `Clock` from one that reads the system clock, and a calendar-field operation from one that applies a
Duration. Kotlin extensions and Java members must be resolved according to their respective language rules.

ADR-0079, ADR-0084, and ADR-0073 govern the exact resolution and qualification process. This ADR adds no separate
library interpretation authority. The relevant source inventory includes:

| Source family                             | Qualification concern                                                        |
|-------------------------------------------|------------------------------------------------------------------------------|
| `java.time` and `java.time.zone`          | Temporal values, arithmetic, rules, resolution, and time scales.             |
| `java.time.format` and `java.time.chrono` | Parsing, calendars, locale-sensitive results.                                |
| Kotlin `kotlin.time`                      | Duration, Instant, Clock, TimeSource, and Java interoperation.               |
| `java.util.Date`, `Calendar`, `TimeZone`  | Mutable state, lenient fields, legacy conversions.                           |
| `java.sql` and XML temporal types         | Domain and precision differences from modern time values.                    |
| Java concurrency and I/O timeouts         | Waiting, cancellation, and side effects rather than closed value arithmetic. |

This inventory identifies interpretation work, not an authoring allow-list. An exact callable may be recognized but
still be unlawful for an owning 1D judgment. Conversely, the presence of a clock or timer API in a standard library does
not oblige Kontrakt to admit that call into a closed judgment.

Third-party libraries, including `kotlinx-datetime`, are not part of ADR-0084's default V1 standard-library inventory
merely because they provide temporal operations.

### 3.10. External Semantic Data and Current-Time Observation

A time-zone calculation can consume released zone rules. Locale-sensitive formatting can consume locale data. Such data
may be a closed Platform Semantic Basis when its exact contents and applicability have been qualified under ADR-0073 §7.

The system default zone, current system time, mutable provider state, and data chosen by an ambient runtime setting are
not closed inputs merely because the author wrote a familiar method name. They must not be smuggled into a deterministic
1D judgment.

A call such as `Instant.now()` or `Clock.System.now()` observes an external time source. It cannot be replaced by a
compiler-chosen timestamp and treated as a pure calculation. An already-formed Instant supplied through an eligible
Input is different: its later comparison may be deterministic even when the external event that produced it was not.

The applicable 1D law decides whether an explicit observed value is legal judgment material. Admission's restrictions on
hidden observation remain in ADR-0065 §8. Fact-local Invariant restrictions remain in ADR-0069 §15. This ADR grants
neither contract a new right to read the Clock.

When a compiler calculation actually consumes time-zone or calendar data, the resulting product depends on that selected
basis. Its reuse and execution under changed data follow ADR-0073 §7 and ADR-0075. A library or JVM upgrade is not proof
that two bases are semantically compatible.

### 3.11. Measured Time Is Not a Temporal Calculation

A measurement of physical elapsed time is an observation of a particular execution boundary. It must identify which
events bound the interval. Its unit does not identify the events by itself.

A monotonic Clock can support an elapsed-time measurement without establishing civil time. A civil timestamp can
identify a date and time without proving that two readings form a monotonic interval. Readings from different Clock
domains cannot be subtracted as if they shared one origin or rate without qualified correlation.

A physical measurement also has limitations that are not properties of a temporal value. The relevant distinction is:

| Property                | Question                                                               |
|-------------------------|------------------------------------------------------------------------|
| Resolution              | What time differences can the instrument distinguish?                  |
| Accuracy or uncertainty | How closely does the observation bound the intended physical interval? |
| Observation boundary    | At what point in the real execution was the reading taken?             |
| Clock behavior          | Can the source be adjusted, and does it count system suspend?          |
| Validity                | Is the reading usable under the claimed measurement conditions?        |

A nanosecond timestamp does not establish nanosecond measurement accuracy. Nor does observing a threshold crossing
establish that execution stopped at the threshold. A statistical latency percentile cannot stand in for a guaranteed
physical upper bound.

The measurement provider's qualification must be sufficient for the actual obligation. A reading that is unavailable or
no longer valid cannot silently become a successful measurement. Measurement failure also cannot be reported as a
confirmed limit violation without the evidence required by the owning Contract.

For the `Relative Elapsed Time` Budget, ADR-0051 defines which physical interval is limited and what `Budget Stop`
means. A measurement mechanism is adequate only if the selected backend can preserve that obligation. Failure to support
the declared boundary is a compilation failure under ADR-0051 §4.8, not permission to lower the obligation to advisory
timeout behavior. A fake Clock can test judgment logic, but it cannot establish the physical capability of the deployed
backend.

This ADR does not add an uncertainty field to every temporal value, nor does it introduce probabilistic Budget
compliance. The design of trustworthy measurement, its platform qualification, and its failure handling belong to the
compiler and backend realization work that must satisfy the owning Contract law.

### 3.12. Time-Based Control Is a Separate Operation

A timeout commonly limits how long a caller waits. A deadline identifies a point relative to a specified Clock or
execution schedule. Cancellation requests work to stop. None of these operations alone proves that the work has ceased
or that its external effects have been undone.

Likewise, a circuit breaker can reject new work based on previously observed failures. Its state and decision are not
consequences of Duration arithmetic. Ordinary service timeout and circuit-breaker libraries therefore provide useful
realization techniques, not additional Budget or Governance authority.

A generated machine may use a timer, cancellation, isolation, or another implementation mechanism when that mechanism
preserves the selected Contract. It must distinguish caller-visible completion from continued work or effects behind
that boundary. ADR-0051 determines whether a Budget Stop is lawful; ADR-0057 and ADR-0058 govern the resulting failure
and outward publication.

This ADR does not define a new Timeout Contract or extend Capacity to time-dependent load. A future change to a 1D
Contract must be decided in that Contract's owning ADR.

### 3.13. Compiler and Realization Boundary

A resolved temporal calculation contributes to the HIR candidate only after its meaning is sufficient for the receiving
1D. ADR-0071 governs the candidate boundary. Establishment belongs to the owning 1D under ADR-0063.

The compiler may replace a qualified platform call with another calculation only when the legally observable result and
failure behavior remain the same. It may also preserve an eligible platform execution under ADR-0073. Neither choice
gives the JVM authority to reinterpret the established Contract.

Time used for compiler scheduling, query waiting, profiling, or performance experiments is not a semantic input to
Contract Identity merely because the compiler records it. If the compiler actually uses a temporal datum as a
determining semantic input, that dependence must instead be represented according to its owning law and the established
compiler-result reuse rules.

The physical choice between JVM Clock APIs, native operating-system facilities, and hardware measurements remains in
Design. An adapter may concentrate platform dependencies, but its existence alone does not establish timing conformance.
Qualification must cover the actual backend and its execution conditions.

## 4. Consequences

Common temporal calculations can be reused across 1D judgments without copying their meaning into each Contract.
Calendar arithmetic and elapsed-time arithmetic remain distinct even when host APIs use similar units.

A compiler may rely on a selected time-zone rule set, but it cannot silently treat the current runtime's zone database
as the same semantic basis. Previously established temporal presentations are not rewritten when the database changes.

Temporal source recognition becomes broader than executable eligibility. The compiler must be able to report an
unsupported or insufficiently qualified operation without turning that compiler limitation into an invented Contract
violation. ADR-0084 and ADR-0074 govern the corresponding result boundary.

Physical measurement and time-based execution control require independent backend evidence. This is especially important
for the existing Budget obligation: returning a timeout or recording a timestamp does not by itself satisfy
`Budget Stop`.

## 5. Conformance and Remaining Decisions

The temporal SDK interpretation work must compare the qualified meaning with the exact selected Java/Kotlin operations.
At minimum, the supporting design should exercise these cases:

| Case                                            | Required distinction                                                                   |
|-------------------------------------------------|----------------------------------------------------------------------------------------|
| DST spring gap and autumn overlap               | Adjustment, strict rejection, and offset selection depend on the operation.            |
| Calendar day versus 24-hour Duration            | Results can differ across a zone transition.                                           |
| Same Instant with different offsets             | Instant equality does not replace source or Input equality.                            |
| Invalid date and parsing pattern                | Failure occurs according to the selected parser or constructor.                        |
| Range and precision boundaries                  | Overflow, saturation, truncation, and rounding preserve the source law.                |
| Kotlin/Java Duration and Instant interoperation | Infinite, bounded, and exceptional outcomes are not collapsed.                         |
| TZDB or locale-data change                      | Products depending on the old basis are not reused without the required compatibility. |
| System Clock or default-zone read               | External observation is not mistaken for a closed calculation.                         |
| Timer expiration and cancellation               | Timeout response is distinguished from the actual end of work.                         |
| Physical Budget boundary                        | Measurement and stop capability are checked separately from simulated judgment logic.  |

The remaining decisions are owned as follows:

| Owner                                  | Work still requiring a separate decision or design                                                                      |
|----------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| ADR-0064 and the relevant temporal law | Complete any not-yet-fixed epoch, range, unit, or resolution determinant before publishing an eligible Leaf profile.    |
| ADR-0079 / ADR-0084 and SDK Design     | Qualify the exact temporal callable inventory for declared JDK and Kotlin profiles, including applicable external data. |
| ADR-0051 and Backend Conformance       | Demonstrate that the selected backend can measure and enforce its actual elapsed-time boundaries.                       |
| Each other 1D ADR                      | Decide whether new time-dependent judgments or time-based resource quantities are needed.                               |
| Compiler Measurement Design            | Choose qualified Clock providers and instrument actual execution boundaries; examine JVM and native implementations.    |
| Verification and QA Design             | Distinguish simulated-time tests from physical timing tests, and evaluate instrumentation overhead.                     |

This Draft makes no claim that the entire standard-library callable inventory has been qualified, or that a JVM backend
already satisfies hard elapsed-time Budget enforcement. Those are evidence requirements for execution, not facts
established by accepting a value-semantics ADR.