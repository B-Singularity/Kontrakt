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
interpretation of Java/Kotlin source operations. This ADR establishes the common temporal meaning used by Contract
judgments and by the compiler itself.

The physical observation of time also matters to the compiler and its generated machine. Reliable measurement is
necessary for resource enforcement and for engineering tests. The meaning of a measurement includes what is measured
and what can be established about the result. It is not determined by the hardware used to obtain it.

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

Two measurements can report the same duration while describing different parts of an execution. A measured value may
also differ from the actual physical quantity. The compiler needs to express that difference without treating an
estimate as an exact value of the measured quantity.

Kontrakt therefore needs a common basis that explains both the author's temporal operations and the time of the running
machine. The existing 1D Contracts remain responsible for deciding which temporal meanings they may use.

## 3. Decision

### 3.1. Common Meaning and Contract Authority

**Kontrakt owns the common meaning of time and its basic operations.** The same meaning is used to interpret Source
Operations and to describe the compiler's internal machine. A calculation has a defined operand domain and result law.
The same law and determining inputs produce the same legally observable result. The chosen implementation cannot
silently change that result. An observation of physical time is not a closed calculation merely because the recorded
result can subsequently be used in one.

This is shared temporal meaning, not a new Time 1D Contract. The owning 1D still establishes its own judgment and
Material. In particular, an interpreted duration does not establish a Budget observation, and a comparison of instants
does not establish an Admission or Governance result by itself.

A completed temporal datum may be interpreted without qualifying the operation that originally produced it. Using a
temporal operation inside governed Contract source is a separate decision under ADR-0073, ADR-0079, and ADR-0084. An
operation supported in user Realization is not thereby permitted in 1D authoring.

#### 3.1.1. Fundamental temporal contracts

The temporal basis consists of seven contracts. Each supplies a law that can be used with the other contracts to
explain a temporal value or operation. A composed meaning is not a new basic contract merely because it has a name
in a library or protocol.

| Basic contract              | Law subject                                                           | Definition |
|-----------------------------|-----------------------------------------------------------------------|------------|
| **Temporal Quantity**       | What a temporal amount means                                          | §3.1.2     |
| **Temporal Reference**      | What a position in a temporal system means                            | §3.1.3     |
| **Temporal Correspondence** | How temporal meanings relate or convert                               | §3.1.4     |
| **Temporal Occurrence**     | How time relates to events and their boundaries                       | §3.1.5     |
| **Temporal Source**         | How a time source produces readings and progresses                    | §3.1.6     |
| **Temporal Measurement**    | What is measured and how observations establish a measurement result  | §3.1.7     |
| **Temporal Uncertainty**    | What can be asserted about an uncertain temporal quantity or position | §3.1.8     |

These contracts are not mutually exclusive value families. Their number is not a claim of mathematical minimality.
They do not prescribe the representation or physical subsystem structure. The same laws can be realized by a host
carrier or by a specialized compiler representation, provided the required observations are preserved.

The existing common laws remain in force where a temporal meaning uses them:

| Existing law | Responsibility                         |
|--------------|----------------------------------------|
| ADR-0078     | Numeric values and arithmetic          |
| ADR-0079     | Type interpretation and Source binding |
| ADR-0080     | Text and character meaning             |
| ADR-0081     | Structured values and their operations |

A composition must establish the applicability of each law it uses. The result of one operation must satisfy the
operand law of the next. Any determining rule data or context must be available under §3.10. Source composition
preserves the observations required by the selected operation, including intermediate results when they can affect
the outcome (ADR-0073 and ADR-0078). A composition cannot assert a stronger physical guarantee than its evidence
supports (§3.1.8). Explanation of a Source Operation does not by itself qualify it for use (§§3.1 and 3.9).

#### 3.1.2. Temporal Quantity

A Temporal Quantity gives a magnitude its temporal meaning. An amount may be declared directly or derived from
other values. The way it was obtained does not define a separate kind of temporal quantity. Time quantities use the
applicable Type and Numeric Laws under ADR-0079 and ADR-0078 rather than a separate temporal type or arithmetic system.

| Law                | Required meaning                                                                       |
|--------------------|----------------------------------------------------------------------------------------|
| Quantity Formation | What the magnitude represents and which temporal meaning establishes it                |
| Unit Semantics     | How the unit interprets the magnitude and when a change of unit preserves the quantity |
| Quantity Relations | The relations specific to temporal quantities and the conditions under which they hold |

**Quantity Formation.** A numeric datum alone does not establish a temporal quantity. The applicable domain must
admit that datum as the magnitude of the quantity. For example, a reserved protocol field value can indicate that a
correction is out of range rather than denote a large duration. A field encoded as an exponent requires its specified
decoding law before it denotes an amount.

A duration may describe elapsed time, while a displacement may relate two temporal positions. A quantity can also
have a meaning under a particular time scale or domain model. A coordinate-time difference and a physical proper-time
interval need not agree. An equivalent processing time defined by a physical model need not equal the time actually
spent processing. The model determines what that amount represents; its scientific law is not a general law of time.

The applicable Type and Numeric Laws determine the admitted magnitudes. An extended domain may admit infinity only
when its meaning and operations are defined. A maximum finite amount is not infinity. An unknown amount and a wait
without a finite deadline do not thereby denote infinite quantities. The domain of a recorded estimate may also
differ from that of the measured quantity (§3.1.8).

**Unit Semantics.** The SI second is the reference unit for physical time quantities. The SI prefixes and fixed-duration
units use their established definitions. ADR-0051 §4.4 defines the corresponding Budget unit vocabulary and its narrower
V1 authoring scope. That scope does not limit the meaning of other temporal quantities.

A unit's meaning follows its definition, not its spelling. A fixed day of 86,400 seconds differs from a calendar day.
Different unit conventions can use the same name for different magnitudes. The applicable definition must therefore be
known before interpreting or converting a quantity.

An exact change of unit preserves the quantity under the established relation between the units.
For example, one second equals 1,000 milliseconds. A conversion to a finite representation may still require rounding
under §3.7. The unit is not the representation's numerical resolution. A binary fraction of a second need not be
expressed as an integer number of nanoseconds.

Some unit correspondences depend on a physical model rather than an exact defining factor. For example, a conversion
from Planck time to SI seconds depends on the value of the gravitational constant. Such a conversion must retain its
applicable correspondence and uncertainty (§§3.1.4 and 3.1.8). An estimated factor cannot be treated as exact.

**Quantity Relations.** This law supplies the temporal meaning of an operation. ADR-0078 supplies its numerical
semantics. An operation must establish what its operands represent and what the result denotes. Equal dimensions or
units alone do not establish that the operands can be substituted in a temporal operation.

| Relation              | Temporal meaning                                                                                                                                                               |
|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Frequency and period  | A fixed positive frequency of a periodic progression determines its period by the reciprocal relation.                                                                         |
| Rate ratio and amount | An established ratio of progression rates can relate amounts under the applicable reference correspondence.                                                                    |
| Phase                 | A phase or phase displacement requires the applicable cycle law. Conversion to a temporal displacement requires the necessary relation to the cycle.                           |
| Calendar amount       | An amount such as one month belongs to its calendar law. Application requires that Reference and a starting position; conversion to an elapsed amount follows §§3.1.4 and 3.4. |

An event rate does not by itself establish a fixed interval between individual events. A tick count is a progression
count, not a physical time unit. Its conversion to an elapsed quantity requires the applicable correspondence; the
source's actual progression is governed by §3.1.6. A calendar amount with several components uses the structured value
laws of ADR-0081. Measurement attribution and accumulation belong to §3.1.7.

These examples establish relations, not a closed inventory of temporal quantity types. The laws do not introduce a
separate quantity hierarchy or a general system of physical dimensions. A domain-specific relation must be established
by its own model before participating in a temporal calculation. The supported operation determines the result domain
and any promotion under ADR-0078. No universal temporal promotion or rounding rule is introduced. See §3.7 for
representation boundaries.

#### 3.1.3. Temporal Reference

A Temporal Reference defines how a temporal position is interpreted. A position has meaning in its selected reference
system. Kontrakt does not require all positions to use a common time scale. The choice follows the temporal domain and
the intended operation (§3.3).

| Law                     | Required meaning                                                                |
|-------------------------|---------------------------------------------------------------------------------|
| Reference Determination | The reference system and the laws needed to interpret the position              |
| Position Formation      | What constitutes an admitted position in that system                            |
| Positional Relations    | The relations that hold between positions and their conditions of applicability |

**Reference Determination.** The necessary reference law must be established for the value and the selected operation.
The following determinants are used where they affect that meaning:

| Determinant              | Meaning                                                                        |
|--------------------------|--------------------------------------------------------------------------------|
| Origin                   | The basis for a position expressed relative to a starting point                |
| Scale                    | The law under which the coordinate progresses                                  |
| Calendar and civil rules | The chronology or other calendar basis governing fields of a position          |
| Reference scope          | The system to which a position belongs, such as a JVM instance or media stream |
| Cycle law                | The modulus or other rule governing repeating coordinates                      |
| Logical relation         | The law relating logical positions without requiring a physical time scale     |

These determinants are not mutually exclusive reference types. An origin and a scale can vary independently.
Neither is required where it does not determine the meaning. A cyclic coordinate may also belong to a source-relative
reference. A determinant already fixed by the applicable
domain does not need to be repeated in every value. A calculation does not require unrelated reference data. The
rules governing the acquisition and reuse of determining data are in §3.10.

**Position Formation.** A position must satisfy the applicable reference law. The existing Type and Numeric Laws
supply its component domains. The calendar law can impose further constraints on the combination of fields; for
example, a Gregorian date cannot contain a day outside its month. A value such as a YearMonth or MonthDay can be
complete in its own domain without representing an entire date. A conversion requiring another position or additional
context follows §3.1.4.

A cyclic coordinate does not by itself establish its unwrapped position. For example, readings of 250 and 10 from
an eight-bit counter can be separated by 16 ticks or by 16 ticks plus any admissible number of full cycles, assuming
forward progression. Recovering a unique displacement requires a law and sufficient determining context. The numeric
residue alone is insufficient.

**Positional Relations.** The reference law establishes which positional comparisons and operations are meaningful.
For a reference that supports displacement, applying an amount to a position can produce another position. The
difference between two positions need not denote a physical elapsed interval. A conversion to such an amount follows
the applicable Quantity and Correspondence laws. Not every position supports addition or subtraction. For example, Java
`LocalTime` can advance from 23:00 to 01:00
by adding two hours, while its `until` operation in hours returns −22 for those positions. The comparison and
difference laws need not recover the amount originally applied.

A pairwise comparison need not establish a global ordering. For example, in an eight-bit cyclic space, a comparison
using forward distances smaller than half a cycle can place 0 before 100, 100 before 200, and 200 before 0. Positions
separated by exactly half a cycle have no order under that rule. ADR-0081 governs use of a comparison for operations
that require an ordering. The temporal comparison does not establish those properties by itself.

A physical source can realize a reference without being identical to it (§3.1.6). A coordinate also does not identify
an occurrence (§3.1.5). The temporal value distinctions remain in §3.2.

#### 3.1.4. Temporal Correspondence

A Temporal Correspondence states a relation between temporal meanings. A conversion must identify the source and
target domains. It must establish the rule and the conditions in which the rule applies. The relation can be total
or partial. A position may have no counterpart or more than one counterpart in the target domain.

An inverse or a composition is valid only where the participating laws establish its meaning. An implementation may
use an offset, a rate conversion or a more complex rule without defining a different basic contract. A calendar rule
can also establish a correspondence without using a linear equation.

An exact relation under a selected rule differs from a physical relation inferred from observations. A calibration may
provide a conversion law whose coefficients are recorded exactly, while its correspondence to physical time remains
uncertain. The conversion uses this contract; the observation and uncertainty follow §§3.1.7–3.1.8. The availability
of several counterparts under an exact rule is not measurement uncertainty.

A descriptor identifies a rule or reference by its admitted descriptive meaning. It does not establish the rule's
contents or authorize its acquisition. Named zones are governed by §3.5. External rule data follows §3.10.

#### 3.1.5. Temporal Occurrence

A Temporal Occurrence relates an event to time. An event and the position attributed to it are distinct. Two events
can occupy the same recorded position without becoming the same event. The applicable law identifies the event
or subject and states the temporal relation being asserted.

| Law         | Meaning                                                                                                 |
|-------------|---------------------------------------------------------------------------------------------------------|
| Association | Which event a temporal value describes; the event of acquisition or delivery may differ from that event |
| Boundary    | Which event begins or ends the selected extent                                                          |
| Ordering    | Which ordering relation applies to the selected occurrences                                             |
| Attribution | Which subject or activity the occurrence belongs to                                                     |
| Recurrence  | How additional occurrences relate to a temporal schedule or another occurrence                          |

Coordinate order does not establish causal order by itself. Logical order may represent causal constraints without
measuring an elapsed duration. Encounter order and Publication order keep the meanings of their owning laws. This
contract does not establish general event Identity or confer execution control authority.

An event extent can be formed from selected boundaries under the applicable reference law. Interval formation and
structural operations follow ADR-0081. Measurement of that extent follows §3.1.7. A scheduled occurrence does not
establish that the corresponding actual event occurred.

#### 3.1.6. Temporal Source

A Temporal Source supplies readings under a law of progression. The source and the reference system of its readings
are separate. Two sources can realize the same time scale without being synchronized.

The source law settles the following properties whenever a use depends on them:

| Property                | Required distinction                                                                     |
|-------------------------|------------------------------------------------------------------------------------------|
| Reading                 | The meaning of the produced value under its reference law                                |
| Progression             | Whether and under what conditions readings can advance, remain equal or move backward    |
| Rate                    | How the source progresses relative to a specified reference                              |
| Suspension              | Whether the source continues to advance during the relevant suspension                   |
| Adjustment              | The effect of a change in rate or a discontinuous change in position                     |
| Control                 | Whether a declared operation can change the source's progression                         |
| Continuity of reference | When prior readings or established relations remain applicable after disruption or reset |

A fixed or controlled source can model time without observing the running machine. A monotonic source can support
elapsed-time measurement without establishing civil time. Conversely, a civil timestamp does not establish that two
readings form a monotonic interval. Readings from different domains require the correspondence of §3.1.4 before they
can be used in a common calculation.

Synchronization is a relation to a reference or another source. Disruption or loss of that reference may invalidate
an established relation even when the source continues producing readings. Any claim that remains valid during
holdover depends on the applicable uncertainty law (§3.1.8). The actual source capabilities require qualification
under §3.11.

#### 3.1.7. Temporal Measurement

A Temporal Measurement relates a measured quantity or position to the observations used to establish it. The
measurand defines what the measurement is about. A measurement of physical elapsed time must identify the particular
execution and the events bounding the selected extent. Its unit alone does not identify those events.

| Law                   | Required meaning                                                                                 |
|-----------------------|--------------------------------------------------------------------------------------------------|
| Measurand and scope   | The subject or activity being measured, and which portions belong to it                          |
| Observation           | The recorded datum, its acquisition conditions and its association with the intended event       |
| Observation boundary  | Where acquisition occurs relative to the event being measured                                    |
| Measurement model     | How the applicable observations determine an estimate or other measurement result                |
| Attribution           | Which subject receives a measured extent, independently of any resource charge                   |
| Accumulation          | How selected extents form the requested amount, including the treatment of overlap               |
| Sampling              | Which occurrences or population the result describes, and how selected observations represent it |
| Evidence and validity | What supports the result and under which conditions it is usable                                 |

An observation can be received or retained independently of the source that produced it. A recorded datum is distinct
from the physical quantity that it is used to estimate. An external assertion about time is distinct from the
observations that verify it. Authenticating that assertion does not establish its physical accuracy or current
validity. Acquisition and derivation must preserve the relevant provenance and conditions of the claim.

A measurement need not span one continuous interval. Consider three activities on a common time axis with extents
`[0, 8]`, `[2, 10]` and `[4, 8]` milliseconds:

| Measurement law | Result | Meaning                                                                |
|-----------------|--------|------------------------------------------------------------------------|
| Span            | 10 ms  | The extent from the earliest start to the latest end                   |
| Union           | 10 ms  | Time during which at least one selected activity is present            |
| Sum             | 20 ms  | The sum of the selected activity extents, retaining their multiplicity |
| Intersection    | 4 ms   | Time shared by all three extents                                       |

These are different measurands regardless of whether some results agree numerically. Exclusive time requires a law
identifying the excluded activity. CPU time requires a basis attributing time to actual CPU usage; a sum of worker
elapsed intervals does not establish it. Critical-path time depends on the selected dependency relation and its
weights. The general structural operations belong to ADR-0081. Their temporal meaning is selected by the measurand.

The same distinction applies to queueing and service measurements, to the transfer of a message and to the execution
that handles it. A sample of measured latencies may describe a population under a selected sampling law. Missing
observations and model-generated samples must not be represented as actual observations. A claim about the population
must retain its supporting conditions (§3.1.8).

Measurement quality is not inferred from the output unit. The observation boundary can differ from the intended
physical boundary. Instrumentation may itself affect the activity being measured. Both effects must be accounted for
when they determine a claimed result. The scope and attribution of a measurement do not decide what a Budget charges;
that authority remains with ADR-0051.

#### 3.1.8. Temporal Uncertainty

Temporal Uncertainty describes what can be established about a temporal quantity or position when its exact physical
value is not known. The recorded numeric value, its estimate of a physical quantity and the guarantee about that
quantity are distinct. An exact calculation on the recorded datum does not make the physical estimate exact.

| Property    | Meaning                                                                                                  |
|-------------|----------------------------------------------------------------------------------------------------------|
| Estimate    | A value derived under a specified observation or estimation law                                          |
| Error       | The difference from a specified reference value; the actual error need not be known                      |
| Accuracy    | Agreement with the intended quantity or reference                                                        |
| Precision   | Agreement among repeated observations under specified conditions                                         |
| Resolution  | The smallest distinction admitted by a representation or distinguishable by an instrument, as applicable |
| Bias        | A systematic component of error                                                                          |
| Uncertainty | The incomplete knowledge of the intended value under the applicable model                                |
| Dependency  | A relation between uncertain inputs that constrains their joint values                                   |
| Validity    | The conditions under which the uncertainty statement or guarantee applies                                |

A nanosecond timestamp does not establish nanosecond measurement accuracy. Drift describes change relative to a
reference over time; its meaning combines Quantity, Source and Correspondence. Jitter requires an identified reference
and variation law. Neither implies an error bound without the corresponding evidence.

The following statements about a temporal result are not interchangeable:

| Form of statement        | Meaning                                                                                         |
|--------------------------|-------------------------------------------------------------------------------------------------|
| Determined value         | A value fixed by its selected law                                                               |
| Possible extent          | Values consistent with the stated constraints                                                   |
| Guaranteed bound         | A bound on the intended quantity under stated premises                                          |
| Estimated bound          | A bound inferred under an estimation law; its guarantee is limited to what that law establishes |
| Coverage interval        | An interval with a stated coverage probability under the applicable model                       |
| Probability distribution | A model assigning probabilities to possible values                                              |
| Confidence interval      | An interval produced by a statistical procedure with a stated confidence level                  |
| Observed range           | The extent of the actual samples; it does not bound unobserved cases                            |

A range need not be continuous or symmetric around an estimate. Its structure may use the existing law of an interval
or another admitted structured form. The uncertainty law determines what the range asserts. A range of values
indistinguishable at a given resolution does not by itself establish a physical error bound. No
mandatory probability model or universal uncertainty representation is implied.

An operation on uncertain values must preserve the meaning of the resulting assertion. In a bound-based calculation,
let `S` be the admissible joint values of its operands. The possible results of an applicable operation `f` are the
values `f(x)` for joint values `x` in `S`. A claimed enclosing bound must contain those results. This does not require
one algorithm for finding the bound, nor does it authorize a wider result where the selected operation requires an
exact extent. A probability-based calculation follows its own model and result law.

For example, let two observed positions be `100 + e` and `150 + e`, in nanoseconds, with a common offset error `e`
in `[-5, 5]`. Their difference is 50 ns under that model. If the two errors are not constrained to agree, the same
individual bounds only justify an enclosing difference of `[40, 60]` ns from the bounds alone. Neither independence
nor cancellation can be assumed without a basis. Unavailable or invalid observations cannot be treated as a zero
uncertainty result.

Observation and conversion may establish new information about a value. Combining such information must preserve the
applicable dependencies and validity conditions. A source adjustment or a disruption can invalidate a calibration.
Continued holdover can change the uncertainty of a current-time estimate without changing the meaning of a previously
recorded coordinate.

For two valid enclosing bounds on event positions in a common reference, a strict separation establishes their
coordinate order. Overlapping bounds do not establish equality or a unique order.

A statement that a limit is met requires evidence sufficient for that judgment. A statistical latency percentile
or an observed maximum cannot replace a guaranteed physical upper bound. When available evidence does not establish
the required claim, the owning Contract determines the permissible outcome (§3.1). This does not introduce
probabilistic Budget compliance or require uncertainty fields on values that have no such meaning.

The concrete estimation laws, representation and propagation rules remain subject to §5.

### 3.2. Temporal Value Distinctions

The following values illustrate completed temporal meanings under §3.1.1. When one of these values is an Input Leaf,
its exact presentation and Sameness remain those of ADR-0064 §7.9.5.

| Meaning                  | Required distinction                                                                                                              |
|--------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| ISO Date                 | A calendar date in the selected proleptic Gregorian domain. It has no time of day.                                                |
| Local Time               | A civil time of day. It has no date, offset, or instant.                                                                          |
| Local Date-Time          | A civil date and local time. It does not identify an instant.                                                                     |
| Numeric UTC Offset       | A fixed numeric displacement from the applicable UTC reference. It is not a named zone.                                           |
| Offset Date-Time         | A local date-time together with its offset. Both remain observable.                                                               |
| Instant                  | An exact position in an established instant reference. Its coordinate law determines the applicable epoch, range, and resolution. |
| Duration                 | An exact amount of elapsed time under a fixed unit and resolution law.                                                            |
| Zone Descriptor          | An identified zone descriptor. It does not contain or assert the current zone rules.                                              |
| Resolved Zoned Date-Time | A local date-time, offset, and Zone Descriptor whose retained distinctions remain observable.                                     |
| Calendar Period          | A calendar-relative amount whose application requires a calendar and a temporal starting context. It is not an elapsed Duration.  |

ADR-0064 currently defines the first nine meanings for its Input inventory. Its Instant profile requires a fixed
Kontrakt epoch coordinate. The relationship of that profile to this common basis is subject to §3.3. **Calendar Period
is a common calculation subject proposed here**, not an additional V1 Input Leaf. Supporting it
does not amend the Input presentation inventory.

An Instant is a position under a Temporal Reference; a Duration is a Temporal Quantity. Neither is an absolute or
relative measurement merely because a host type has that name (§3.1.7). A `TimeMark` or other reading tied to a
running clock is not a transferable Instant unless a qualified correspondence establishes the relevant time scale
and correlation (§3.1.4).

### 3.3. Epochs, Time Scales, and Units

**No single time scale governs the common temporal basis.** A temporal operation follows the Reference that determines
its meaning. User-facing Source Operations retain the time-scale laws of their selected libraries or domains under
§3.9. They are not converted to a Kontrakt-chosen scale merely because the compiler interprets them. The compiler's
own choice of reference follows the purpose of its operation (§3.13).

Under §3.1.3, the origin and scale of an Instant determine the interpretation of its coordinate. A nanosecond count
without that context is not enough to establish an Instant. The same written epoch in different scales need not denote
the same position. An exact correspondence must establish the relevant relation before converting a position or a
scale-dependent difference. Named-zone rules follow §3.5.

A correspondence between time scales (§3.1.4) must include any leap-second treatment required by the selected semantics.
For example, a UTC position can distinguish an inserted leap second that a POSIX coordinate does not separately
represent. A smeared scale can have a different rate from TAI during a leap adjustment. A conversion may also require
historical rules rather than the current offset between scales. See §3.7 when the result is not exactly representable.

Java `java.time.Instant` and Kotlin `kotlin.time.Instant` are not assumed interchangeable merely because both expose
epoch-based coordinates. Their specified ranges and time-scale laws must be examined for the selected profiles. A Source
conversion that copies the same seconds and fractional-second fields does not by itself establish physical equivalence.

The unit law for a quantity is under §3.1.2. Calendar days across a time-zone transition are illustrated in §3.4.
The coordinate scale and unit must not be confused with the representation's numerical resolution (§3.7).

ADR-0064 §7.9.5 was established before the common temporal basis. Its fixed Kontrakt Instant epoch requirement is
not a requirement to normalize all temporal values in this ADR. The Instant Leaf profile and its relationship to
admitted temporal reference domains require a separate revision of ADR-0064. The presentation must use an established
temporal meaning rather than define a new common time scale. Its retained distinctions and Presentation Sameness must
remain defined by the Input law. Until that revision is decided, the existing Input law remains in force; this ADR
neither admits new Input Leaves nor changes their Sameness. A calculation cannot proceed with an undetermined epoch or
other determinant on which it depends.

### 3.4. Calendar Arithmetic and Elapsed-Time Arithmetic

The exact source operation determines the arithmetic. Calendar arithmetic modifies calendar fields under its selected
rules. Elapsed-time arithmetic applies a Duration to an instant under the applicable reference law. A scale-dependent
coordinate displacement is not necessarily a physical elapsed duration (§3.1.3).

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
time between two instants does not require converting them to local civil time when their reference and the selected
difference law already establish the elapsed quantity.

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
| Instant coordinate equality                | Two Instants have the same coordinate under the same applicable reference law.          |
| Same-instant comparison                    | Two resolved temporal values denote the same Instant under the selected conversion law. |
| Local date-time equality                   | Their retained civil fields are equal; no instant is implied.                           |
| Presentation Sameness                      | Only the distinctions fixed by the owning presentation law are compared.                |
| Source `equals`, `compareTo`, or `isEqual` | The result of that exact Java/Kotlin operation.                                         |

For example, Java `OffsetDateTime.equals` can return `false` for `2024-01-01T00:00+09:00` and `2023-12-31T15:00Z`, while
`isEqual` returns `true`. Replacing one operation with the other changes the source meaning.

The result of a same-instant comparison does not revise Input Presentation Sameness. Nor does it authorize Fact
uniqueness, collection key identity, or Canonicalization equivalence. Those judgments belong to their existing owners.

### 3.7. Precision, Range, and Unsuccessful Calculation

Every supported value has a defined domain. Temporal calculations follow ADR-0078 §§3.3–3.7 for numeric result laws.
The applicable bounds and numerical resolution must be preserved. Resolution need not be uniform over the domain.
A decimal fraction of a second and an integer nanosecond field have different representation laws. Neither establishes
the accuracy of a measurement (§3.1.8).

The mathematical meaning of a quantity does not by itself establish support for a Numeric Format. For example,
`2^-32` seconds is a defined amount that is not an integer number of nanoseconds. An ideal period of `1/48000` seconds
cannot be represented as a finite decimal number of seconds. ADR-0078 §3.7 leaves Rational Numeric Formats
subject to a separate decision. An operation may establish a result in an admitted format under its own law, but it may
not claim unsupported exact Datum representation. If an operation rounds each intermediate amount, its later result
must reflect those rounding points rather than a single rounding after exact accumulation.

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

A time-zone calculation can consume released zone rules. Locale-sensitive formatting can consume locale data. A
time-scale
conversion can consume historical leap-second or other reference data. Such data may be a closed Platform Semantic Basis
when its exact contents and applicability have been qualified under ADR-0073 §7.

The system default zone, current system time, mutable provider state, and data chosen by an ambient runtime setting are
not closed inputs merely because the author wrote a familiar method name. They must not be smuggled into a deterministic
1D judgment.

A call such as `Instant.now()` or `Clock.System.now()` observes an external time source. It cannot be replaced by a
compiler-chosen timestamp and treated as a pure calculation. An already-formed Instant supplied through an eligible
Input is different: its later comparison may be deterministic even when the external event that produced it was not.

The applicable 1D law decides whether an explicit observed value is legal judgment material. Admission's restrictions on
hidden observation remain in ADR-0065 §8. Fact-local Invariant restrictions remain in ADR-0069 §15. This ADR grants
neither contract a new right to read the Clock.

When a compiler calculation actually consumes temporal rule data, the resulting product depends on that selected
basis. Its reuse and execution under changed data follow ADR-0073 §7 and ADR-0075. A library or JVM upgrade is not proof
that two bases are semantically compatible.

### 3.11. Physical Measurement and Backend Qualification

The distinction between a calculation and physical observation follows §3.1. The meaning of a measurement is governed
by §3.1.7 and its uncertainty by §3.1.8. The provider's qualification must be sufficient for the actual obligation.
In particular, it must account for source behavior (§3.1.6) and the actual observation boundary. Observing a threshold
crossing does not establish that execution stopped at the threshold (§3.12).

A reading that is unavailable or no longer valid cannot silently become a successful measurement. Measurement failure
cannot be reported as a confirmed limit violation without the evidence required by the owning Contract.

For the `Relative Elapsed Time` Budget, ADR-0051 defines which physical interval is limited and what `Budget Stop`
means. A measurement mechanism is adequate only if the selected backend can preserve that obligation. Failure to support
the declared boundary is a compilation failure under ADR-0051 §4.8, not permission to lower the obligation to advisory
timeout behavior. A fake Clock can test judgment logic, but it cannot establish the physical capability of the deployed
backend.

The physical acquisition and qualification of measurements belong to compiler and backend realization work. That work
must satisfy the selected measurement law, not redefine it. Failure handling and the physical enforcement mechanism
remain subject to their owning Contract laws.

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

The temporal basis in §3.1.1 can describe the meaning of a resolved Source Operation and the compiler's own temporal
operations. An IR or other compiler representation may compose these contracts or preserve their meaning in a
specialized form. A resolved calculation contributes to the HIR candidate only after its meaning is sufficient for
the receiving 1D. ADR-0071 governs the candidate boundary. Establishment belongs to the owning 1D under ADR-0063.

The compiler may replace a qualified platform call with another calculation only when the legally observable result and
failure behavior remain the same. It may also preserve an eligible platform execution under ADR-0073. Neither choice
gives the JVM authority to reinterpret the established Contract.

Time used for compiler scheduling, query waiting, profiling, or performance experiments is not a semantic input to
Contract Identity merely because the compiler records it. If the compiler actually uses a temporal datum as a
determining semantic input, that dependence must instead be represented according to its owning law and the established
compiler-result reuse rules. Reuse of a derivation over retained observations does not establish that a new physical
observation occurred.

The compiler selects its internal reference and source according to the purpose of the operation. The common law in
§3.3 does not require an absolute wall-clock scale for internal calculations. The following choices govern the direction
of internal Time and Measurement Design:

| Purpose                                             | Default basis                                                                                              |
|-----------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| Execution-time measurement                          | A monotonic source for elapsed time, including compiler profiling and verification performance measurement |
| Relative Elapsed Time Budget                        | A monotonic source qualified for the actual boundary under ADR-0051 and §3.11                              |
| CPU-time measurement                                | A source or measurement law that accounts for actual CPU usage (§3.1.7)                                    |
| Ordinary external timestamps in journals and logs   | Unix Time                                                                                                  |
| Event order                                         | The applicable logical or occurrence order, not a wall timestamp as a substitute for it                    |
| Precise absolute-time work                          | A scale suitable for the required domain, such as TAI or the PTP time scale                                |
| Deterministic simulation and property-based testing | An established model reference and controlled source where time is part of the model                       |

Monotonicity is a source property (§3.1.6), not the name of an international absolute time scale. The concrete
reference, handling of suspension and provider qualification remain to be established for each internal use. A journal's
external timestamp does not itself establish execution order. Relating an internal measurement to an external timestamp
requires the applicable correspondence. It does not turn a derived timestamp into a fresh external observation.

The planned Time and Measurement subsystems use this basis. Their detailed contracts and realization remain to be
established. Shared meaning does not require a common service call or passage through the Query Engine. The physical
choice between JVM Clock APIs, native operating-system facilities, and hardware measurements remains in Design. An
adapter may concentrate platform dependencies, but its existence alone does not establish timing conformance.
Qualification must cover the actual backend and its execution conditions.

## 4. Consequences

The basis in §3.1.1 supplies common meaning for both 1D judgments and internal compiler operations. It also lets the
compiler state what its physical measurements describe and what the resulting claims establish. Individual time values
and operations can be defined through composition without duplicating the basic laws.

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
The supporting design must also examine the basis against the time systems and measurement practices it is intended
to explain. At minimum, the conformance work should exercise these cases:

| Case                                              | Required distinction                                                                                                 |
|---------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| DST spring gap and autumn overlap                 | Adjustment, strict rejection, and offset selection depend on the operation.                                          |
| Calendar day versus 24-hour Duration              | Results can differ across a zone transition.                                                                         |
| Same Instant with different offsets               | Instant equality does not replace source or Input equality.                                                          |
| Invalid date and parsing pattern                  | Failure occurs according to the selected parser or constructor.                                                      |
| Range and precision boundaries                    | Overflow, saturation, truncation, and rounding preserve the source law.                                              |
| Kotlin/Java Duration and Instant interoperation   | Infinite, bounded, and exceptional outcomes are not collapsed.                                                       |
| TZDB or locale-data change                        | Products depending on the old basis are not reused without the required compatibility.                               |
| System Clock or default-zone read                 | External observation is not mistaken for a closed calculation.                                                       |
| Timer expiration and cancellation                 | Timeout response is distinguished from the actual end of work.                                                       |
| Physical Budget boundary                          | Measurement and stop capability are checked separately from simulated judgment logic.                                |
| Cyclic counter or protocol timestamp              | A residue does not establish its unwrapped position without the required context.                                    |
| Logical and physical ordering                     | A logical relation does not imply an elapsed interval.                                                               |
| Source adjustment or holdover                     | Continued readings do not establish validity of the former correlation or guarantee.                                 |
| Disjoint and overlapping measurement extents      | Span, union, accumulation and subject attribution retain their respective measurands.                                |
| Acquisition and event boundary                    | A delayed or indirect observation is not the event itself.                                                           |
| Correlated measurement errors                     | A shared error can cancel only under an established dependency law.                                                  |
| Physical bound and statistical range              | The type of claim and its premises determine what it can establish.                                                  |
| Authenticated external time assertion             | Authentication does not by itself establish accuracy or continued validity.                                          |
| Clock correlation and precision                   | Exact conversion arithmetic does not prove an exact physical relation.                                               |
| Fractional time amounts and intermediate rounding | Binary fractional seconds and an ideal media period retain their mathematical meaning; support and loss follow §3.7. |
| Protocol quantity fields                          | Exponent decoding, reserved correction values, and error fields are interpreted under their specified domains.       |
| Units with different defining conventions         | Fixed month conventions, calendar amounts, and uncertain physical unit correspondences retain their meanings.        |
| Scale-dependent and model-derived amounts         | Coordinate-time differences, proper time, and equivalent processing time are not conflated.                          |
| Clock cycles and physical-time constraints        | An adequate relation is established before judging combined cycle and elapsed-time requirements.                     |
| Nonuniform samples and attributed time            | Equal total time does not establish equal intermediate positions or equal measurands.                                |
| Partial calendar position and cyclic comparison   | A complete position in its own domain need not denote an Instant or admit a global order.                            |
| Epoch, leap seconds, and scale conversion         | Equal coordinate fields do not establish equivalence across POSIX, UTC, TAI, and smeared scales.                     |
| Internal and external time references             | Internal elapsed measurements and external timestamps retain their respective meanings and correspondence.           |

The remaining decisions are owned as follows:

| Owner                                  | Work still requiring a separate decision or design                                                                                                            |
|----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ADR-0064 and the relevant temporal law | Revisit the fixed Instant Leaf profile under §3.3. Complete necessary reference and representation determinants before publishing an eligible Leaf profile.   |
| ADR-0079 / ADR-0084 and SDK Design     | Qualify the exact temporal callable inventory for declared JDK and Kotlin profiles, including applicable external data.                                       |
| ADR-0051 and Backend Conformance       | Demonstrate that the selected backend can measure and enforce its actual elapsed-time boundaries.                                                             |
| Each other 1D ADR                      | Decide whether new time-dependent judgments or time-based resource quantities are needed.                                                                     |
| ADR-0082                               | Complete the remaining temporal domains and operation laws. Settle their semantic representation and detailed conversion laws.                                |
| ADR-0078                               | Decide additional Numeric Formats if required for exact temporal Datum support; do not infer Rational support from §3.7.                                      |
| ADR-0082 and Measurement Design        | Establish the concrete measurement models and uncertainty laws, including propagation, dependency and validity.                                               |
| Compiler Time and Measurement Design   | Realize the purpose-specific choices of §3.13. Choose qualified providers and instrument actual execution boundaries; examine JVM and native implementations. |
| Verification and QA Design             | Distinguish simulated-time tests from physical timing tests, and evaluate instrumentation overhead.                                                           |

This Draft makes no claim that the entire standard-library callable inventory has been qualified, or that a JVM backend
already satisfies hard elapsed-time Budget enforcement. Those are evidence requirements for execution, not facts
established by accepting a common temporal-semantics ADR.