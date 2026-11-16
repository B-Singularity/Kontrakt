# ADR-0082: Temporal Values, Operations, Sources, and Measurement

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
- ADR-0058 — Publication Contract
- ADR-0060 — Diagnostic Evidence and Retention Contract
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

A Contract may calculate with temporal values without observing the running machine. Other obligations depend
on physical time. ADR-0051 already defines a `Relative Elapsed Time` Budget for an exact subject.

These uses require a common temporal basis rather than a single host-library model.

ADR-0064 §7.9.5 defines nine temporal Input Leaves. ADR-0079 and ADR-0084 govern Java/Kotlin source
interpretation. This ADR supplies the temporal meaning for both Contract judgments and the compiler itself.

The compiler and its generated machine also need reliable measurements for resource enforcement and
engineering tests. The meaning of those measurements must be established independently of the hardware that
realizes them.

## 2. Problem

A temporal expression can have an incomplete determining basis. Even a familiar callable can depend on
external rule data or the current environment (§§3.9–3.10).

Calendar arithmetic and elapsed-time arithmetic can give different results. A comparison can also preserve
different distinctions of the same value (§§3.4 and 3.6).

At the execution boundary, an inaccurate interpretation of a timeout can establish the wrong judgment even
with an accurate Clock (§3.12).

For engineering measurements, equal durations may describe different activities. The recorded result and the
physical claim require separate meanings (§§3.1.7–3.1.8).

The common basis must resolve these problems without assuming the authority of an owning 1D Contract.

## 3. Decision

The following decision index identifies the governing provisions. The referenced sections define the operative
laws.

1. Shared temporal meaning and 1D authority (§§3.1–3.1.1).
2. The seven fundamental temporal laws (§§3.1.2–3.1.8).
3. Temporal value distinctions and preservation of Input presentation (§3.2).
4. Purpose-specific reference selection (§§3.3 and 3.13).
5. Distinct calendar and elapsed-time arithmetic (§3.4).
6. Exact resolver, comparison and conversion behavior (§§3.5–3.9).
7. Closed determining data versus external observation (§3.10).
8. Qualified measurement at the applicable judgment boundary (§3.11).
9. Logical control and physical execution as distinct obligations (§3.12).

### 3.1. Common Meaning and Contract Authority

**Kontrakt owns the common meaning of time and its basic operations.** This meaning applies to Source
Operations and the compiler's internal machine. Each calculation has a defined operand domain and result law.
The same law and determining inputs must produce the same legally observable result, regardless of
implementation. The distinction between a calculation and physical observation follows §3.10.

This ADR introduces shared laws, not a Time 1D Contract. The owning 1D establishes its judgment and Material.
Interpreting a duration does not establish a Budget observation. Comparing instants does not establish an
Admission or Governance result.

A completed temporal datum can be interpreted without qualifying its producer. Eligibility of an operation for
governed Contract source is a separate matter (§3.9).

#### 3.1.1. Fundamental Temporal Laws

The seven laws below explain distinct aspects of temporal meaning. Each can be used in combination with the
others. A named library or protocol composition does not require another fundamental law.

| Fundamental law             | Law subject                                                                                       | Definition |
|-----------------------------|---------------------------------------------------------------------------------------------------|------------|
| **Temporal Quantity**       | What a temporal amount means                                                                      | §3.1.2     |
| **Temporal Reference**      | What a position in a temporal system means                                                        | §3.1.3     |
| **Temporal Correspondence** | How a counterpart is established across temporal domains or under an additional temporal relation | §3.1.4     |
| **Temporal Occurrence**     | What time describes about events and the relations between them                                   | §3.1.5     |
| **Temporal Source**         | How a time source produces readings and progresses                                                | §3.1.6     |
| **Temporal Measurement**    | What is measured and how observations establish a measurement result                              | §3.1.7     |
| **Temporal Uncertainty**    | What can be asserted about an uncertain temporal quantity or position                             | §3.1.8     |

The laws are neither mutually exclusive value families nor a claim of mathematical minimality. They prescribe
no physical subsystem or representation. A host carrier or a specialized compiler representation may realize
them if it preserves the required observations.

The existing common laws remain in force where a temporal meaning uses them:

| Existing law | Responsibility                         |
|--------------|----------------------------------------|
| ADR-0078     | Numeric values and arithmetic          |
| ADR-0079     | Type interpretation and Source binding |
| ADR-0080     | Text and character meaning             |
| ADR-0081     | Structured values and their operations |

A composed operation must satisfy these conditions:

- Each participating law applies to its operands. Every intermediate result satisfies the next operand law.
- Determining data is available under §3.10.
- The composition preserves the selected Source Operation's required observations. Intermediate results are preserved
  where they affect the outcome (ADR-0073 and ADR-0078).
- A physical claim is limited to its evidence (§3.1.8).

#### 3.1.2. Temporal Quantity

A Temporal Quantity gives a magnitude its temporal meaning. A declared amount and a derived amount use the
same law. Their acquisition methods do not create separate quantity kinds. Type and Numeric Laws follow
§3.1.1.

| Law                | Required meaning                                                                       |
|--------------------|----------------------------------------------------------------------------------------|
| Quantity Formation | What the magnitude represents and which temporal meaning establishes it                |
| Unit Semantics     | How the unit interprets the magnitude and when a change of unit preserves the quantity |
| Quantity Relations | The relations specific to temporal quantities and the conditions under which they hold |

**Quantity Formation.** The domain must admit the datum as a magnitude of the quantity. A reserved protocol
value can signal an out-of-range correction instead of denoting a large duration. An exponent-encoded field
must be decoded under its specified law.

Quantities can describe different things even when they have the same unit:

| Quantity               | Meaning                                                                          |
|------------------------|----------------------------------------------------------------------------------|
| Duration               | An amount of elapsed time.                                                       |
| Displacement           | An amount relating positions under their reference.                              |
| Scale-dependent amount | A coordinate-time difference can differ from physical proper time.               |
| Model-derived amount   | An equivalent processing time need not equal the time actually spent processing. |

A domain model supplies its own scientific law. That law is not a general law of time.

The applicable Type and Numeric Laws determine the admitted magnitudes. An extended domain may admit infinity
only with a defined meaning and operations. These cases must remain distinct:

- An infinite amount.
- A maximum finite amount.
- An unknown amount.
- A wait with no finite deadline.

The domain of an estimate may differ from that of the measured quantity (§3.1.8).

**Unit Semantics.** The SI second is the reference unit for physical time quantities. SI prefixes and
fixed-duration units retain their established definitions. ADR-0051 §4.4 defines a narrower V1 Budget
vocabulary. It does not limit other temporal quantities.

A unit follows its definition rather than its name. For example, different fixed-month conventions can denote
different amounts. Calendar amounts follow §3.4. The applicable unit definition must be known before
interpretation or conversion.

An exact unit conversion preserves the quantity. One second equals 1,000 milliseconds. A finite representation
may still require rounding under §3.7. The unit and the representation's resolution are distinct; a binary
fractional second need not be an integer number of nanoseconds.

Some unit conversions use a physical model instead of an exact defining factor. For example, expressing Planck
time in SI seconds depends on the value of the gravitational constant. The applicable correspondence and its
uncertainty follow §§3.1.4 and 3.1.8.

**Quantity Relations.** The operation must establish what its operands represent and what its result denotes.
Equal dimensions or units alone are insufficient for substitution. ADR-0078 supplies the numeric result law.

| Relation              | Temporal meaning                                                                                                                                                               |
|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Frequency and period  | A fixed positive frequency of a periodic progression determines its period by the reciprocal relation.                                                                         |
| Rate ratio and amount | An established ratio of progression rates can relate amounts under the applicable reference correspondence.                                                                    |
| Phase                 | A phase or phase displacement requires the applicable cycle law. Conversion to a temporal displacement requires the necessary relation to the cycle.                           |
| Calendar amount       | An amount such as one month belongs to its calendar law. Application requires that Reference and a starting position; conversion to an elapsed amount follows §§3.1.4 and 3.4. |

An event rate alone does not fix the interval between individual events. A tick count denotes a progression
count rather than a physical time unit. Conversion to an elapsed amount requires §3.1.4; actual progression
follows §3.1.6. Structured calendar amounts follow ADR-0081. Measurement attribution follows §3.1.7.

The relations above are not a closed inventory of quantity types. No quantity hierarchy or general system of
physical dimensions is introduced. A domain-specific relation requires its own established model. The selected
operation determines the result domain and any promotion under ADR-0078. Representation and rounding follow
§3.7.

#### 3.1.3. Temporal Reference

A Temporal Reference gives a position its meaning. Reference selection follows the domain and intended
operation (§3.3).

| Law                     | Required meaning                                                                |
|-------------------------|---------------------------------------------------------------------------------|
| Reference Determination | The reference system and the laws needed to interpret the position              |
| Position Formation      | What constitutes an admitted position in that system                            |
| Positional Relations    | The relations that hold between positions and their conditions of applicability |

**Reference Determination.** The reference law must be established for the value and the selected operation.
The following determinants apply only where they affect the meaning:

| Determinant              | Meaning                                                                        |
|--------------------------|--------------------------------------------------------------------------------|
| Origin                   | The basis for a position expressed relative to a starting point                |
| Scale                    | The law under which the coordinate progresses                                  |
| Calendar and civil rules | The chronology or other calendar basis governing fields of a position          |
| Reference scope          | The system to which a position belongs, such as a JVM instance or media stream |
| Cycle law                | The modulus or other rule governing repeating coordinates                      |
| Logical relation         | The law relating logical positions without requiring a physical time scale     |

These determinants can coexist. Origin and scale can vary independently; neither is required unless it
determines the meaning. A cyclic coordinate can also be source-relative. A determinant fixed by the domain
need not be repeated in each value. Acquisition and reuse of determining data follow §3.10.

**Position Formation.** A position must satisfy the reference law. Its component domains follow the Type and
Numeric Laws. The calendar can further constrain their combination; a Gregorian date must have a day admitted
by its month. `YearMonth` and `MonthDay` can be complete in their own domains. Conversion that needs another
position or context follows §3.1.4.

The selected field-resolution operation determines which fields participate and how their relations are
resolved. Individually admitted fields may fail to describe a common position. The participating fields must
satisfy that operation's consistency conditions. Merely acquiring unresolved fields does not establish the
position.

A cyclic coordinate alone is insufficient to recover its unwrapped position. For example, an eight-bit counter
advancing from 250 to 10 can traverse 16 ticks plus any admissible number of full cycles. A unique
displacement requires a law and sufficient context.

**Positional Relations.** The reference law determines which operations apply. Applying an amount may move a
position where displacement is supported. A positional difference need not denote physical elapsed time; such
a conversion follows §§3.1.2 and 3.1.4. Addition and subtraction are not universal operations on positions.

For example, Java `LocalTime` advances from 23:00 to 01:00 when two hours are added. Yet `until` in hours
returns −22. The difference law need not recover the applied amount.

A temporal field can observe a stored component or a value derived under the reference law. Field and unit
applicability can depend on the position and selected operation. A field range can also depend on them. A
reported range is not a guarantee that every included value is admitted by the formation or adjustment law.

Adjustment to a temporal boundary requires the formation law for those boundaries and the law for selecting
one. Fixed spacing may require an origin. Changing that origin can change the boundaries even when the spacing
stays the same. Calendar boundaries follow the applicable calendar and civil rules. ADR-0078 governs numerical
rounding; the temporal law supplies the positions to which it applies.

Pairwise comparison need not supply a global ordering. In an eight-bit cyclic space, a rule using forward
distances smaller than half a cycle can order 0 before 100, 100 before 200, and 200 before 0. Positions
exactly half a cycle apart are unordered under that rule. Ordering-dependent operations must satisfy ADR-0081.

For the distinction between reference and source, see §3.1.6. For the association of an event with a
coordinate, see §3.1.5.

#### 3.1.4. Temporal Correspondence

A Temporal Correspondence relates a value to a counterpart in another domain or under an additional relation
within its domain. For example, it can relate two time scales or turn a counter displacement into an elapsed
amount. Intrinsic quantities and positions follow §§3.1.2–3.1.3. Event relations follow §3.1.5.

| Law                          | Required meaning                                                                             |
|------------------------------|----------------------------------------------------------------------------------------------|
| Correspondence Determination | The participating domains and the conditions under which a counterpart exists                |
| Conversion Semantics         | What a selected operation does with the correspondence                                       |
| Correspondence Composition   | The conditions under which inverse relations and composed conversions preserve their meaning |

**Correspondence Determination.** The relation identifies its source and target domains. It also establishes
the applicable rule. It may apply to only part of the source domain. A value can have zero or several
counterparts, without a fixed upper bound on their number. The relation need not be finite or enumerable.
Collection-valued results follow ADR-0081.

An offset, a rate or a calendar rule can establish a correspondence without introducing another fundamental
law. A relation can apply only to a reference segment. For example, a spacecraft counter that repeats values
after reset can require a partition to identify the applicable correlation.

A calibration may have exactly recorded coefficients while its relation to physical time remains uncertain.
Physical claims follow §3.1.8; observation follows §3.1.7.

The applicability of a physical correspondence depends on the intended claim:

| Claim                           | Required basis                                                                                |
|---------------------------------|-----------------------------------------------------------------------------------------------|
| Position                        | A relation valid for the selected position. An earlier observation is insufficient by itself. |
| Elapsed interval                | A relation supporting the difference between the selected boundaries.                         |
| Property throughout an interval | Evidence sufficient for the claim over the interval.                                          |

The basis for one claim need not establish another (§3.1.8).

A descriptor identifies a rule or reference through its admitted meaning. Its contents and acquisition require
a separate basis under §3.10.

**Conversion Semantics.** The selected conversion determines what to do with the correspondence. Its result
may follow any of these forms, according to the selected law:

- Return the available counterparts.
- Select a counterpart.
- Adjust the operand. An adjusted result need not be a counterpart of the original position (§3.5).
- Reject the operand.

| Condition                   | Distinction                                                                                   |
|-----------------------------|-----------------------------------------------------------------------------------------------|
| No counterpart              | The established relation has no result for the supplied value.                                |
| Several counterparts        | More than one result satisfies the established relation. This is not measurement uncertainty. |
| Insufficient basis          | The data or context needed to establish the relation is unavailable.                          |
| Uncertain physical relation | The correspondence is subject to the conditions of §3.1.8.                                    |
| Unrepresentable result      | A counterpart exists but the required result domain cannot represent it under §3.7.           |

These conditions describe meanings, not a failure taxonomy. The resolved operation owns the result law (§3.9);
the owning Contract determines the treatment of an unsuccessful calculation.

**Correspondence Composition.** An inverse relation need not be a function. A distinguishing value lost by
conversion may be unrecoverable. This also applies to reversible mathematical relations when their
representation loses information (§3.7).

Composition follows §3.1.1. For example, adding and then subtracting a calibration offset cancels only when
the two offsets are established to agree. A different calibration can prevent cancellation despite identical
formulas. Shared uncertain inputs follow §3.1.8.

A chain and a direct correspondence can establish different results or guarantees. A chain is usable when it
establishes the required relation. Substitution must preserve the selected operation's observations under
§3.1.1. The existence of a path is insufficient. No graph or path-selection algorithm is prescribed.

#### 3.1.5. Temporal Occurrence

A Temporal Occurrence relates an event to time. The event is distinct from its position. Its owning event law
establishes its identity; this ADR does not define general event Identity.

| Law                    | Required meaning                                                        |
|------------------------|-------------------------------------------------------------------------|
| Occurrence Association | Which event the temporal meaning describes                              |
| Occurrence Boundary    | Which events delimit the selected temporal extent                       |
| Occurrence Relations   | Which temporal relations hold between events and on what basis          |
| Occurrence Recurrence  | How repeated events relate to a temporal rule or to earlier occurrences |

**Occurrence Association.** The time described by a value can differ from the time of acquisition or delivery.
For example, an operation's occurrence may precede the publication of its record. Two events recorded at the
same position remain distinct.

A temporal association may describe an actual occurrence or a position assigned to a future or hypothetical
event. A predicted position is not evidence that the event occurred. Subject identity follows the owning law;
measurement attribution follows §3.1.7.

**Occurrence Boundary.** An extent uses selected boundary events in an applicable reference. For example,
request-to-delivery extent can differ from execution-start-to-delivery extent. ADR-0081 governs interval
formation and structural operations. Measurement uses §3.1.7. Resource-enforcement boundaries follow the
owning Contract (§3.11).

**Occurrence Relations.** A relation identifies the events it relates and its determining basis. For example,
a relation can establish precedence or occurrence within an extent. Use the positional law (§3.1.3), any
necessary correspondence (§3.1.4) and the applicable evidence law (§3.1.8).

The following distinctions are necessary for an event relation:

| Evidence or relation     | Limit of the claim                                                         |
|--------------------------|----------------------------------------------------------------------------|
| Equal recorded positions | Physical simultaneity requires its own basis.                              |
| Coordinate order         | Causal order requires its own basis.                                       |
| Logical order            | It can express causal constraints without measuring elapsed time.          |
| Incomparable events      | The selected relation supplies no order for the pair.                      |
| Insufficient evidence    | The relation is undetermined; this differs from evidence that it is false. |

Incomparability and insufficient evidence are not evidence of simultaneity.

General ordering and relation laws follow ADR-0081. Encounter order and Publication order retain their owning
meanings. Causal constraints and required execution order belong to their owners. Observing an execution order
does not make it required. A temporal constraint need not establish a total order unless its owning law
requires one. Deterministic realization follows ADR-0075.

Progress of a time coordinate does not establish that all earlier events have been received. A completeness
claim requires the applicable data or result law and an identified scope.

**Occurrence Recurrence.** A recurrence defines the positions of repeated events. It may use an original
scheduled position or the outcome of an earlier occurrence. These bases can differ: Java
`LocalDate.datesUntil` can anchor its progression at the first date, unlike repeated additions to the previous
date after end-of-month adjustment (§3.4).

A rescheduled event can retain its original scheduled position. The revised position and the actual occurrence
remain separately associated under this law. ADR-0081 governs the progression structure. A schedule is not
evidence of execution.

Execution authority and the handling of missed or overlapping repetitions follow §3.12.

#### 3.1.6. Temporal Source

A Temporal Source supplies readings under a progression law. Its reference is distinct from the source.
Sources using the same scale need not be synchronized.

| Law                | Required meaning                                                        |
|--------------------|-------------------------------------------------------------------------|
| Source Reading     | What a source provides and under which conditions it provides a reading |
| Source Progression | How the source advances and how changes to its progression take effect  |
| Source Continuity  | When the progression relation remains valid across readings             |

**Source Reading.** A reading has the meaning of its reference (§3.1.3). The selected operation determines how
the reading is obtained. It may return a retained value or obtain a new one. A fixed or controlled source can
model time without physical observation. A reading alone does not establish a fresh physical observation (§3.1.7).

Different calls on one source can yield different results. For example, a Java offset Clock can apply the
offset after conversion to milliseconds in one call and before that conversion in another. Preserve the
selected operation under §3.9. Type Law distinguishes valid readings from unavailable or uninitialized
results, including those encoded numerically.

**Source Progression.** The source law establishes the following properties whenever a use depends on them:

| Property    | Required distinction                                                                  |
|-------------|---------------------------------------------------------------------------------------|
| Progression | Whether and under what conditions readings can advance, remain equal or move backward |
| Rate        | How the source progresses relative to a specified reference                           |
| Suspension  | Whether the source continues to advance during the relevant suspension                |
| Adjustment  | The effect of a change in rate or a discontinuous change in position                  |
| Control     | Whether a declared operation can change the source's progression                      |

A source need not be monotonic or advance at a constant rate. Monotonicity means readings do not decrease
under the applicable conditions; it is not strict increase. Monotonicity alone supplies neither a civil-time
reference nor a guarantee of fidelity to physical elapsed time. A civil timestamp alone is not evidence of
monotonic progression. Two monotonic sources can also differ in their treatment of suspension. Qualification
for an extent follows §3.11.

Changing the progression rate differs from changing the position immediately. A declared control operation can
alter model-time progression as permitted by its source law. For example, it can advance model time. It can
also pause model time or reverse its direction. These are progression properties rather than separate kinds of
time. A later change to the source leaves an established reading unchanged. Model-time qualification follows
§3.11.

**Source Continuity.** The source must establish when its progression law relates earlier and later readings.
A reset or disruption can invalidate that relation. For example, live VM migration can change the underlying
counter while leaving the time-source interface intact. Interface identity alone does not establish
continuity. Hardware replacement can preserve continuity through a valid compensation law.

Cross-domain calculations require §3.1.4. Synchronization establishes a relation to a reference or another
source. Losing synchronization can invalidate that relation without disrupting local progression. The validity
of a guarantee during holdover follows §3.1.8.

Observing a source relation and adjusting a source are different operations. Synchronization may use the
former to change progression. The correspondence and uncertainty must reflect the relevant conditions before
and after the adjustment (§§3.1.4 and 3.1.8).

No particular source hierarchy, state machine or disruption marker is prescribed (§3.1.1). Qualify actual
capabilities under §3.11.

#### 3.1.7. Temporal Measurement

A Temporal Measurement relates a measurand to the observations used to establish a result. The measurand
identifies what is measured even if the recorded datum happens to equal it. For claims about physical
accuracy, see §3.1.8.

| Law                     | Required meaning                                                              |
|-------------------------|-------------------------------------------------------------------------------|
| Measurement Target      | The measurand, its scope and the subject to which it is attributed            |
| Measurement Observation | What is observed and under which conditions the observation provides evidence |
| Measurement Formation   | How the observations establish the requested result                           |

**Measurement Target.** Identify the measurement subject and the activity included in its scope. An
elapsed-time measurement must identify the specific execution and its boundary events (§3.1.5). The unit alone
is insufficient.

A measurement can cover more than one interval. The following targets require different extents or inclusion
laws:

| Target             | Required distinction                                                                   |
|--------------------|----------------------------------------------------------------------------------------|
| Service            | Waiting for service differs from executing the service.                                |
| Message processing | Transfer extent can differ from handling extent.                                       |
| Exclusive time     | The excluded activity must be identified.                                              |
| CPU time           | Actual CPU usage must be attributed; summing worker elapsed intervals is insufficient. |

Attribution identifies the subject receiving a measured extent. A shared computation may run once while each
participating subject receives its inclusive time. Summing those attributions need not recover the physical
execution time. ADR-0051 determines Budget charges.

Elapsed time includes all time between the selected boundaries. An activity inside that extent may be
unrelated to the subject's logical work. Inclusion does not attribute the activity's cost or responsibility
for delay to that subject. Selected activity time requires its own inclusion law.

In a concurrent execution, the following questions establish different temporal meanings:

| Question                                               | Required basis                                                          |
|--------------------------------------------------------|-------------------------------------------------------------------------|
| How long did the subject take?                         | The selected elapsed-time boundaries.                                   |
| How much time was spent in a selected execution state? | Occurrence extents and the applicable state classification.             |
| Which activities overlapped?                           | The relevant positions or extents in a comparable reference.            |
| How much did an activity change the subject's latency? | Evidence and a model sufficient to establish its performance influence. |

Performance influence requires more than logical dependency or overlap. Work without a dependency can cause
delay through shared resources. Interrupting one activity need not delay completion of the larger parallel
computation.

Noise depends on the target and experimental conditions. An interruption can be a factor to isolate
experimentally while remaining part of elapsed time under the inclusion law above. Time in a state is
insufficient to establish the latency reduction if that state were avoided.

A sampled result must identify its occurrences or population. A population of completed executions need not
represent failed or unfinished executions.

**Measurement Observation.** An observation establishes a recorded datum under its acquisition conditions. Its
acquisition event can differ from the intended physical event (§3.1.5). For example, a handler timestamp need
not describe arrival at the network interface. Preserve that distinction where it affects the target.

Observations may be received or retained independently of the producing source. An external assertion and the
observations that verify it are distinct. Authentication alone does not establish physical accuracy or
continuing validity (§3.1.8).

The observation law identifies the samples actually acquired and their selection. A missing sample must not be
represented as acquired. Partial evidence can still support a claim: a device operating at the end of a test
gives a lower bound on lifetime, not its failure time. Missing completion evidence establishes neither
continuing execution nor cessation (§3.12).

Instrumentation can change the activity being measured. An inference about execution without instrumentation
requires its own model. Account for acquisition effects and the observation boundary wherever they affect the
claimed result.

Execution perturbation differs from observation error. Under the elapsed-time inclusion law, instrumentation
delay inside the boundary is included. Merely subtracting instrumentation time does not establish the
uninstrumented result.

**Measurement Formation.** A model determines the result for the selected target from the available
observations. It may derive an amount from positions or estimate one from partial observations. Keep inferred
results distinct from direct observations. For example, performance-model samples for unobserved requests must
remain distinguishable from acquired samples. Correspondence follows §3.1.4.

Consider three activities on a common time axis with extents `[0, 8]`, `[2, 10]` and `[4, 8]` milliseconds:

| Measurement law | Result | Meaning                                                                |
|-----------------|--------|------------------------------------------------------------------------|
| Span            | 10 ms  | The extent from the earliest start to the latest end                   |
| Union           | 10 ms  | Time during which at least one selected activity is present            |
| Sum             | 20 ms  | The sum of the selected activity extents, retaining their multiplicity |
| Intersection    | 4 ms   | Time shared by all three extents                                       |

These results describe different measurands even when their numbers agree. Accumulation follows the target's
treatment of multiplicity and overlap. Critical-path time follows the selected dependency relation and its
weights. Apply ADR-0081 to the structure and ADR-0078 to its numeric operations.

Reuse requires a result whose meaning supports the next calculation. An amount accumulated since a starting
point differs from an amount since the previous observation. Adding successive cumulative totals can double
count an extent. Resetting accumulation need not interrupt source progression.

Combining aggregates requires a law preserving the target population. For example, combining sample means can
require sample counts. Individual percentiles generally cannot determine a combined percentile. Retain the
sampling and aggregation conditions that affect the claim.

Marginal results alone can be insufficient to establish a result over corresponding occurrences. The same
individual stage latency distributions can yield different combined distributions. The formation law
determines whether the relation between corresponding occurrences must be retained.

A decomposition must define its components. An exhaustive partition covers the whole extent without double
counting. Incomplete observations must leave unexplained portions distinct from established causes.
Independent estimates of performance influence need not be additive. Structural laws follow ADR-0081.

The claimed result determines the required observation material. A summary may be sufficient for one formation
law but not another. Consuming a derived result follows ADR-0075. Complete observation-history retention is
not a common measurement obligation; retention belongs to its owning law.

A measurement derivation must preserve its relevant provenance and acquisition conditions. Identify the
observations used and the model that gives the result meaning. Preserve the premises of its physical or
population claim (§3.1.8). Reusing retained observations does not produce a new physical observation.

#### 3.1.8. Temporal Uncertainty

Temporal Uncertainty describes what can be established when the intended physical quantity or position is not
exactly known. Keep three meanings distinct:

- The recorded numeric datum.
- The estimate of the intended physical value.
- The guarantee about that value.

Exact arithmetic on the datum does not make the estimate exact.

| Law                     | Required meaning                                                        |
|-------------------------|-------------------------------------------------------------------------|
| Uncertainty Semantics   | What a statement establishes about the intended temporal value          |
| Uncertainty Composition | What follows from dependent uncertain values under a selected operation |
| Uncertainty Validity    | The conditions under which a statement supports a judgment              |

**Uncertainty Semantics.** The meaning of an uncertainty statement depends on the quantity or position it
describes. The following properties are relevant where they affect that statement:

| Property    | Meaning                                                                                                            |
|-------------|--------------------------------------------------------------------------------------------------------------------|
| Estimate    | A value derived under a specified observation or estimation law (§3.1.7)                                           |
| Error       | The difference from a specified reference value; the actual error need not be known                                |
| Accuracy    | Agreement with the intended quantity or reference                                                                  |
| Precision   | Agreement among repeated observations under specified conditions                                                   |
| Resolution  | The distinctions admitted by a representation or distinguishable by an instrument, as applicable (§§3.7 and 3.1.7) |
| Bias        | A systematic component of error                                                                                    |
| Uncertainty | The incomplete knowledge of the intended value under the applicable model                                          |
| Dependency  | A relation between uncertain inputs that constrains their joint values                                             |
| Validity    | The conditions under which the uncertainty statement or guarantee applies                                          |

A nanosecond timestamp alone cannot establish nanosecond accuracy. Correcting an estimated bias does not
establish that all error was removed.

| Property | Required basis                                                       |
|----------|----------------------------------------------------------------------|
| Drift    | Change relative to a reference over time (§§3.1.2, 3.1.4 and 3.1.6). |
| Jitter   | An identified reference and variation law.                           |

Neither property supplies an error bound without evidence.

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

The table is not a closed classification. A range may be asymmetric or disconnected. It uses the admitted
interval or structured value laws of ADR-0081. A claimed enclosing range may include impossible values; it is
an exact set of possible values only if that set is established. Values indistinguishable at a given
resolution do not alone establish a physical error bound. No universal probability model or uncertainty
representation is prescribed.

**Uncertainty Composition.** Preserve the meaning of the resulting assertion. In a bound-based calculation,
let `S` contain the admissible joint operand values. For an applicable operation `f`, the possible results are
`f(x)` for `x` in `S`. An enclosing bound must contain those results. Required unsuccessful outcomes follow
§3.7.

A result range must account for the operation's behavior over the whole admitted domain. For example, squaring
values in `[-2, 3]` includes zero, although neither endpoint maps to it.

A finite-format enclosing bound must still contain all required values after rounding under ADR-0078. A
rounding that excludes possible values is not an enclosing bound. This law prescribes no bounding algorithm
and does not authorize widening when the operation requires an exact extent. A probability calculation follows
its own model and result law; a sampled range is not a guaranteed bound.

Consider positions `100 + e` and `150 + e` in nanoseconds with a common offset error `e` in `[-5, 5]`. Their
difference is 50 ns. Without a constraint equating the errors, the individual bounds alone justify an
enclosing difference of `[40, 60]` ns. Neither independence nor cancellation may be assumed. Sharing a source
is insufficient to establish that errors are common. Statistical calculations must likewise account for
established dependencies that affect the result.

Several valid enclosing bounds need not identify the selected operation's lawful observable result (§3.1.1).
Numeric and structured operations retain their existing laws.

**Uncertainty Validity.** State the basis for an uncertainty claim and its conditions of applicability.
Invalid or unavailable observations must not be treated as zero uncertainty. An unreported error contribution
cannot be assumed to vanish.

New observations or conversions may refine knowledge of a value. Their combination must preserve dependencies
and validity conditions. Source disruption and adjustment can invalidate calibration (§§3.1.4 and 3.1.6).
During holdover, the uncertainty of a current-time estimate may change while earlier recorded coordinates
retain their meaning. Later evidence may refine an earlier coordinate's physical estimate while leaving the
recorded observation intact. Reuse under changed determining data follows §3.10.

Constraints combined for one intended value must apply to that value. An empty set of admissible values
signals inconsistency when the claims conflict. It cannot justify an arbitrary result. Inconsistent evidence
differs from insufficient evidence; the owning law determines its treatment.

Two valid enclosing bounds in a common reference establish coordinate order if strictly separated. Overlap
alone establishes neither equality nor a unique order. An established dependency may supply sufficient
additional evidence, as in the shared-offset example.

Evidence for a physical limit claim must support that claim. For a valid enclosing bound and an inclusive
upper limit:

| Relation to the limit         | What the bound establishes               |
|-------------------------------|------------------------------------------|
| Entirely at or below          | Numerical compliance.                    |
| Entirely above                | The quantity exceeds the limit.          |
| Contains values on both sides | Neither conclusion from the bound alone. |

A statistical latency percentile or observed maximum cannot substitute for a guaranteed physical upper bound.
If evidence is insufficient, the owning Contract determines the permissible outcome (§3.1).

A Contract may instead judge an accepted measurement result (§3.11) without making the stronger physical
claim. This law introduces neither probabilistic Budget compliance nor mandatory uncertainty fields for values
that have no uncertainty meaning.

The concrete estimation laws, representation and propagation rules remain subject to §5.

### 3.2. Temporal Value Distinctions

The following values illustrate completed meanings under §3.1.1. For an Input Leaf, ADR-0064 §7.9.5 owns the
exact presentation and Sameness.

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

ADR-0064 defines the first nine meanings for its Input inventory. Its Instant profile follows §3.3 of this
ADR. **Calendar Period is a common calculation subject proposed here**, not an additional V1 Input Leaf.

An `Instant` is a position under §3.1.3; Duration is a quantity under §3.1.2. A `TimeMark` or another
running-clock reading becomes a transferable Instant only through a qualified correspondence supplying the
relevant scale and correlation (§3.1.4). For measurement claims, see §3.1.7.

### 3.3. Epochs, Time Scales, and Units

**No single time scale governs the common temporal basis.** The operation follows the Reference that
determines its meaning. Source Operations retain the selected library or domain's scale (§3.9), rather than a
scale chosen by the compiler. Internal choices follow §3.13.

An Instant coordinate requires the origin and scale applicable under §3.1.3. A bare nanosecond count cannot
establish it. Identically written epochs in different scales need not identify the same position. Converting a
position or scale-dependent difference requires the exact applicable correspondence (§3.1.4).

A time-scale correspondence must include the leap-second treatment required by its selected law. UTC can
distinguish an inserted leap second that POSIX does not separately represent. The rate of a smeared scale can
differ from TAI during adjustment. Conversion may require historical rules rather than the current offset. For
inexact representation, see §3.7.

For the Java and Kotlin Instant comparison, see §3.9.

Unit definitions follow §3.1.2. Representation resolution follows §3.7; calendar-day calculations are
illustrated in §3.4.

ADR-0064 §7.9.5 predates this basis. Its fixed Kontrakt Instant epoch is an Input profile, not a normalization
rule for all temporal values. Revision of that Leaf profile belongs to ADR-0064. The profile must adopt an
established temporal meaning and retain the distinctions required by Input Presentation Sameness. Until
revision, the existing Input law governs; this ADR changes neither the admitted Leaf inventory nor its
Sameness. A calculation requiring an undetermined epoch or another missing determinant cannot proceed.

### 3.4. Calendar Arithmetic and Elapsed-Time Arithmetic

Calendar arithmetic modifies calendar fields under the selected rule. Elapsed-time arithmetic applies a
Duration to an instant under its reference law. The exact operation governs both (§3.9). For coordinate
displacement that is not an elapsed duration, see §3.1.3.

Consider a zoned date-time at noon in New York on 2024-03-09. Under the relevant New York rules:

| Source operation           | Result                        | Elapsed interval |
|----------------------------|-------------------------------|------------------|
| Add one local calendar day | 2024-03-10 12:00 at UTC−04:00 | 23 hours         |
| Add 24 elapsed hours       | 2024-03-10 13:00 at UTC−04:00 | 24 hours         |

A fixed day denotes 86,400 seconds. A local calendar day follows its calendar and reference, as the example
shows.

End-of-month adjustment depends on the selected callable; it is not a universal rule for Calendar Period.
General preservation of intermediate results and unsuccessful outcomes follows §§3.1.1 and 3.7.

Intermediate calendar adjustment can change composition. Under Java `LocalDate` month arithmetic:

| Starting date | Operation             | Result     |
|---------------|-----------------------|------------|
| 2024-01-31    | Apply one month twice | 2024-03-29 |
| 2024-01-31    | Apply two months once | 2024-03-31 |

The intermediate adjustment must be preserved (§3.1.1). Recovery by an inverse operation follows §3.1.4.

A comparison of dates does not require conversion to Instant. Elapsed-time calculation between instants
requires no civil-time conversion when the reference and difference law already establish the amount.

### 3.5. Named Zones and Local-Time Resolution

Zone Descriptor and applicable zone rules are separate inputs. Named-zone resolution requires the rules for
the local date-time or instant being interpreted; see §3.10 for rule-data acquisition.

The available counterparts and the behavior of a resolver are distinct under §3.1.4. Local-time resolution has
three relevant cases:

| Case                | Available offset interpretations | Consequence                                                                           |
|---------------------|----------------------------------|---------------------------------------------------------------------------------------|
| Ordinary local time | One                              | The rules can determine one instant for the local date-time.                          |
| Gap                 | None                             | The source operation may reject the value or apply a specified adjustment.            |
| Overlap             | More than one                    | The source operation must determine how an offset is selected, retained, or rejected. |

For `2024-03-10T02:30` in `America/New_York`, Java `LocalDateTime.atZone` adjusts forward into the valid
interval. `ZonedDateTime.ofStrict` can reject the invalid local-time/offset combination.

During an overlap, projection to local date-time can discard the selected offset. Projecting
`2024-11-03T01:30-05:00` in `America/New_York` and then applying `atZone` can select UTC−04:00 instead. The
original instant is not recovered.

Calendar arithmetic on a resolved zoned value may require resolution after calendar calculation. Preserve the
selected resolver at each required intermediate position (§3.4). Rule-set validity follows §3.10.

An established resolved presentation must not be silently reinterpreted against current TZDB. ADR-0064
preserves the submitted date-time together with its offset and Zone Descriptor. A later consistency check
against zone rules is a separate operation with its own basis.

Zone identifiers require a qualified comparison or conversion law. Do not substitute aliases or fixed offsets
for named regions or provider-defined identifiers merely by name. Comparing descriptor spellings, comparing
rules and comparing resulting instants answer distinct questions; see §3.6.

### 3.6. Comparison and Sameness

There is no universal temporal equality operator. Common comparisons follow their selected law; Source
comparisons follow their resolved callable (§3.9).

| Comparison                                 | What it establishes                                                                     |
|--------------------------------------------|-----------------------------------------------------------------------------------------|
| Instant coordinate equality                | Two Instants have the same coordinate under the same applicable reference law.          |
| Same-instant comparison                    | Two resolved temporal values denote the same Instant under the selected conversion law. |
| Local date-time equality                   | Their retained civil fields are equal; no instant is implied.                           |
| Presentation Sameness                      | Only the distinctions fixed by the owning presentation law are compared.                |
| Source `equals`, `compareTo`, or `isEqual` | The result of that exact Java/Kotlin operation.                                         |

For example, Java `OffsetDateTime.equals` can return `false` for `2024-01-01T00:00+09:00` and
`2023-12-31T15:00Z`, while `isEqual` returns `true`. Replacing one operation with the other changes the source
meaning.

Same-instant comparison supplies no authority over Input Presentation Sameness or another owner's judgment:

- Fact uniqueness belongs to ADR-0068.
- Collection key identity belongs to ADR-0081.
- Canonicalization equivalence belongs to ADR-0066.

Input Sameness remains with ADR-0064 (§3.2).

### 3.7. Precision, Range, and Unsuccessful Calculation

Each supported temporal domain has defined bounds and numerical resolution. Resolution need not be uniform. A
decimal fractional second and an integer nanosecond field have different representation laws. Accuracy of a
measurement follows §3.1.8. Numeric results follow ADR-0078 §§3.3–3.7; there is no universal temporal
promotion or rounding rule.

A mathematically defined time amount need not have a supported exact Datum representation:

| Amount                            | Representation limit                    |
|-----------------------------------|-----------------------------------------|
| `2^-32` seconds                   | Not an integer number of nanoseconds.   |
| Ideal period of `1/48000` seconds | Not a finite decimal number of seconds. |

Rational Numeric Formats remain a separate decision under ADR-0078 §3.7. An operation may establish an
admitted result under its own law; it must not claim unsupported exact representation. When intermediate
amounts are rounded, the composed result must preserve those rounding points (§3.1.1).

For each supported operation, the result law must account for the following when they affect a legal
observation:

| Condition                   | Meaning to preserve                                               |
|-----------------------------|-------------------------------------------------------------------|
| Out-of-range date or time   | The source's rejection or other explicitly permitted outcome.     |
| Arithmetic overflow         | The chosen operation's exceptional or bounded result.             |
| Truncation or rounding      | Its direction, unit, and point of application.                    |
| Precision-losing conversion | The information lost and the exact resulting value.               |
| Invalid temporal text       | The selected parser's unsuccessful result and exception behavior. |
| Ambiguous zone resolution   | The selected resolver's actual choice or rejection.               |

The Java/Kotlin domain and outcome differences are collected in §3.9.

Source-observable failure is part of the selected result law. Its attribution follows ADR-0057.

A failed calculation supplies no valid fallback merely because its representation can store one. Repair
invalid operands only where the selected operation defines the repair.

### 3.8. Temporal Text and Calendar Systems

Parsing and formatting operate on Text and temporal values. Text meaning remains with ADR-0080.

The selected parser's determining basis may include:

- The pattern.
- Resolver rules.
- Locale data.
- Calendar data.

Parsing `2024-03-10` as an ISO date differs from locale-dependent parsing under an unspecified host default (§3.10).

The applicable chronology governs calendar calculation. A civil date label can be valid without the date
occurring in a named zone. Conversion to an extent requires §§3.1.5 and 3.5.

Formatting can lose distinctions needed for round-trip conversion (§3.1.4). Omitting an offset can prevent
recovery of the original Offset Date-Time. A seconds-only pattern can discard fractional precision.

Non-ISO chronologies in `java.time.chrono` require their own qualified operations and semantic data. Their
dates are not ISO Input Leaves unless the selected conversion establishes an exact lawful result.

### 3.9. Java/Kotlin Source Operations

**A temporal Source Operation retains the semantics of its exact resolved callable.** The author's selected
overload and language binding determine the operation. Distinguish an overload accepting a `Clock` from an
overload reading the system clock. Likewise, distinguish a calendar-field operation from a Duration operation.
Resolve Kotlin extensions and Java members under their respective language rules. Composition follows §3.1.1.

Resolution and qualification follow ADR-0079, ADR-0084 and ADR-0073. Recognition is not authoring eligibility.
The owning 1D decides whether the qualified operation may participate in its judgment; support in user
Realization does not grant 1D authoring authority.

The following differences require exact profile-specific interpretation. They are not a common replacement law
for the selected callable:

| Subject                                         | Java / Kotlin distinction                                                                                                                                                                                                           |
|-------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `java.time.Instant` and `kotlin.time.Instant`   | Both expose epoch-based coordinates. Their specified ranges and time-scale laws must be checked for the selected profiles. Copying seconds and fractional-second fields does not by itself establish physical equivalence (§3.1.4). |
| `java.time.Duration` and `kotlin.time.Duration` | Their domains differ. Kotlin supports infinity and arithmetic that can saturate. Conversion of Kotlin infinity to a large finite Java Duration does not preserve value identity; conversion back can lose information.              |
| Out-of-range Instant arithmetic                 | A Kotlin operation that clamps a result cannot be replaced by a Java operation that throws (§3.7).                                                                                                                                  |

The SDK interpretation inventory includes:

| Source family                             | Qualification concern                                                        |
|-------------------------------------------|------------------------------------------------------------------------------|
| `java.time` and `java.time.zone`          | Temporal values, arithmetic, rules, resolution, and time scales.             |
| `java.time.format` and `java.time.chrono` | Parsing, calendars, locale-sensitive results.                                |
| Kotlin `kotlin.time`                      | Duration, Instant, Clock, TimeSource, and Java interoperation.               |
| `java.util.Date`, `Calendar`, `TimeZone`  | Mutable state, lenient fields, legacy conversions.                           |
| `java.sql` and XML temporal types         | Domain and precision differences from modern time values.                    |
| Java concurrency and I/O timeouts         | Waiting, cancellation, and side effects rather than closed value arithmetic. |

The inventory identifies qualification work, not an authoring allow-list. Apply the eligibility law above even
to standard-library Clock and timer operations.

Third-party libraries, including `kotlinx-datetime`, are not part of ADR-0084's default V1 standard-library
inventory merely because they provide temporal operations.

### 3.10. External Semantic Data and Current-Time Observation

A closed Platform Semantic Basis can include released temporal rule data when its exact contents and
applicability are qualified under ADR-0073 §7. Examples include:

- Time-zone rules.
- Locale data for formatting.
- Historical leap-second or other reference data for scale conversion.

The following are not closed inputs merely because a familiar callable obtains them:

- The system default zone.
- Current system time.
- Mutable provider state.
- Data selected by ambient runtime settings.

Do not introduce these as hidden inputs to a deterministic 1D judgment.

`Instant.now()` and `Clock.System.now()` observe an external source. They cannot be replaced with a
compiler-chosen timestamp and treated as closed calculations. In contrast, comparing an already formed Instant
supplied as eligible Input may be deterministic despite its external origin.

Explicit observed material requires eligibility under the owning 1D. In particular, Admission follows ADR-0065
§8 for hidden observation. Fact-local Invariant follows ADR-0069 §15. Neither receives new Clock-reading
authority here.

A product consuming temporal rule data depends on that selected basis. Reuse and execution under changed data
follow ADR-0073 §7 and ADR-0075. A library or JVM upgrade is insufficient evidence of semantic compatibility.

### 3.11. Physical Measurement and Backend Qualification

Qualification must support the actual temporal obligation. Use the applicable laws:

- Source behavior (§3.1.6).
- Measurement and observation boundaries (§3.1.7).
- Evidence validity (§3.1.8).
- Time-based control (§3.12).

An unavailable or invalid reading cannot establish a successful measurement. Report a limit violation only
when the evidence required by the owning Contract establishes it. Measurement failure is not a confirmed
violation.

Physical measurement must pursue the accuracy and precision required by its use. Qualification must account
for factors that affect the judgment:

- Numerical or instrumental resolution.
- Acquisition delay.
- Measurement uncertainty.
- Cost and interference introduced by acquisition.

Apply the distinctions in §§3.1.7–3.1.8.

ADR-0051's `Relative Elapsed Time` quantity describes the actual elapsed interval of its exact subject. The
judgment compares the measurement accepted for that boundary with the declared allowance under the owning
Budget law. Measurement uncertainty alone neither extends the allowance nor requires a conservative offset.
This is a judgment on an accepted measurement, not a stronger physical claim under §3.1.8.

**A measurement used for judgment must describe the relevant boundary.** An arbitrary earlier Clock reading
cannot justify later result establishment. The backend must establish applicability under the selected
acquisition and control law (§3.12). This does not require a physically atomic Clock read and state change or
prescribe the observation mechanism.

ADR-0051 owns `Relative Elapsed Time` boundaries and `Budget Stop`. The measurement-based judgment and
establishment law in this Draft require alignment of ADR-0051 §§4.5, 4.9 and 6.2 before adoption. Inability to
support the declared boundary or judgment remains a compilation failure under ADR-0051 §4.8. Simulated Clock
tests can verify judgment logic, but physical backend capability requires separate evidence.

Acquisition and physical qualification belong to realization (§3.13). Failure handling and enforcement must
satisfy their owning Contract laws.

### 3.12. Time-Based Control Is a Separate Operation

The following concepts have different meanings:

| Concept      | Meaning                                                         |
|--------------|-----------------------------------------------------------------|
| Timeout      | Commonly limits how long a caller waits.                        |
| Deadline     | A position relative to a specified Clock or execution schedule. |
| Cancellation | A request to stop work.                                         |

Their control guarantees follow §3.12.4; the effect of Failure on established results and external effects
follows §3.12.3.

#### 3.12.1. Time Conditions and Control Occurrences

A time condition is distinct from the action taken on it. Reaching a deadline, observing it and acting on that
observation need not occur at the same time. The relevant occurrences are:

| Occurrence               | Meaning                                                                                         |
|--------------------------|-------------------------------------------------------------------------------------------------|
| Deadline reached         | The limit is reached in its applicable reference.                                               |
| Threshold observed       | A qualified observation supplies the basis for evaluating the time condition.                   |
| Failure established      | The owning Contract establishes its Failure and stops the normal processing that depends on it. |
| Cancellation requested   | The realization requests the selected work to stop, if necessary.                               |
| Execution ceased         | The selected work actually stops.                                                               |
| Resource occupancy ended | The relevant resource is no longer held under its owning law.                                   |

The selected control law determines which occurrences take place and their relations. Timer notification is
not evidence that its intended action ran (§3.1.5). Publication and delivery are distinct from result
establishment under ADR-0057 and ADR-0058.

A later observation does not establish the time of an earlier event. Notification arrival leaves the original
deadline unchanged. The recurrence law in §3.1.5 distinguishes missed scheduled events from actual
occurrences. The execution law decides whether missed events are skipped, combined or acted upon later.

Communicating a deadline across time domains requires the applicable correspondence. A remaining duration is
relative to the boundary at which it was established. Starting it again at receipt does not necessarily
preserve the original deadline. The guarantee requires §§3.1.4 and 3.1.8.

#### 3.12.2. Time Judgment and Failure Establishment

Time-limit judgment and control-outcome establishment are distinct. `Relative Elapsed Time` uses the accepted
measurement under §3.11. Its allowance retains the inclusive meaning of ADR-0051.

A later time observation or cancellation cannot invalidate an already lawfully established terminal result.
Such an observation is not proof that the earlier result was late. The absence of an established `Budget Stop`
cannot authorize a successful result prohibited by the time judgment. The result establishment path must apply
the Budget law directly. It cannot rely only on the arrival of a timeout notification.

The owning Contract establishes its result. Competing result and stop establishment for one subject must have
one consistent order. Failure establishment is the boundary at which the stopped subject loses authority over
dependent normal progress. Establishing the stop and withdrawing that authority form one logical transition.
Apply the boundary observation law of §3.11. A backend race cannot change the comparison law (ADR-0075 and
ADR-0051 §6.4).

| Condition at the applicable establishment boundary                                    | Required treatment                                                                              |
|---------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| Successful terminal result already lawfully established                               | Preserve it under the non-retroactivity rule above.                                             |
| A result is being established and the accepted measurement satisfies the Budget law   | Permit establishment only if the other Contract judgments permit it and no stop is established. |
| The accepted measurement requires `Budget Stop` and no terminal result is established | Establish the stop under ADR-0051.                                                              |
| `Budget Stop` already established                                                     | Apply the withdrawal of authority above.                                                        |
| Required observation unavailable or invalid                                           | Apply the measurement-failure law of §3.11.                                                     |

ADR-0051 still owns exact equality and simultaneous-failure laws. Where one observation establishes violations
of multiple applicable Budgets, preserve every required Failure. A competing transition must not discard
required Failure evidence.

#### 3.12.3. Contract Stop and Physical Cessation

`Budget Stop` may be established before unfinished work ceases. The prohibition of dependent normal progress
follows §3.12.2. Failure does not itself authorize rollback of established results or external effects (ADR-0057). A
completed operation requires no cancellation merely because a later observation exceeds the
allowance.

Failure establishment need not wait for subsequent handling:

- Diagnostic analysis.
- Result delivery.
- Cancellation.
- Cleanup.

Their required relations follow the owning laws. Publication follows ADR-0058. A response does not establish
that resource occupancy has ended.

Time-based control requires its own measurement. For example, the following extents can describe the same
execution without denoting the same result:

| Measured extent               | Boundaries                                                           |
|-------------------------------|----------------------------------------------------------------------|
| Threshold detection latency   | Deadline to the observation of its crossing.                         |
| Failure establishment latency | Deadline to the establishment of the applicable Failure.             |
| Terminal result latency       | Subject start to its terminal result establishment.                  |
| Response latency              | The applicable request boundary to result delivery.                  |
| Cessation latency             | The selected stop or request boundary to actual execution cessation. |

These extents are Measurement Targets under §3.1.7. Apply its elapsed-time inclusion law to measurement and
control work inside the subject boundary. Later handling can have its own extent. An allowance is not a cap on
the observed elapsed amount: detection and enforcement can continue after the limit.

A stop preserves the evidence required by ADR-0051 and ADR-0060. Keep the observed elapsed amount and exact
subject distinct from explanatory factors. Under the applicable evidence and retention laws, preserve the
material needed to explain execution and interference within the qualified observation scope. Common
observations can explain several failures. An explanation must state its evidentiary limits; missing causal
evidence cannot establish that the subject caused the delay. Measurement attribution follows §3.1.7.
Explanation leaves the time judgment unchanged.

#### 3.12.4. Control Guarantees and Authority

A limit can govern result establishment, response delivery or physical cessation. Each guarantee concerns its
own event (§3.12.1). A physical bound on detection, response or cessation requires the evidence law in §3.1.8.

For `Relative Elapsed Time`, the existing Budget law has the following scope:

| Obligation or claim                                                  | Basis                                                                                         |
|----------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| Judgment on the subject's declared elapsed-time allowance            | §3.11 and ADR-0051.                                                                           |
| Eligibility of a normal result or dependent progress                 | §3.12.2.                                                                                      |
| Detection, response delivery or physical cessation by that allowance | Not established by the Budget judgment alone. Each physical bound requires its own guarantee. |
| Resource release or rollback of external effects                     | §3.12.3 and the owning resource or effect law.                                                |

A control result must not conceal that a deadline may have been missed. Claim timely occurrence only on
evidence that establishes it (§3.1.8). Apply the distinction between insufficient evidence and a confirmed
limit violation in §§3.1.8 and 3.11.

The owning obligation determines the needed qualification and control. A timed response requirement may need a
separate allowance for detection and response work. It cannot authorize reducing another Budget's declared
allowance. The owning law determines the domain-specific treatment of missed deadlines.

A circuit breaker can reject new work based on earlier observed failures. That decision belongs to its control
law, not Duration arithmetic. A timeout or circuit-breaker library grants no new Budget or Governance
authority.

This ADR introduces no Timeout 1D Contract and no time-dependent extension to Capacity. Ownership remains as
follows:

| Owner                                     | Responsibility                                                              |
|-------------------------------------------|-----------------------------------------------------------------------------|
| ADR-0051                                  | Budget judgment and enforcement.                                            |
| ADR-0057                                  | Failure.                                                                    |
| ADR-0058                                  | Publication.                                                                |
| Governance and planned Whole World Design | System-wide execution policy, interference control and failure propagation. |

The temporal basis supplies facts for these decisions; it selects no policy. Changes to a 1D Contract belong
to its own ADR.

### 3.13. Compiler and Realization Boundary

The temporal basis applies to resolved Source Operations and internal compiler operations (§3.1). A resolved
calculation contributes to a HIR candidate only once its meaning is sufficient for the receiving 1D (ADR-0071). The
owning 1D establishes Material under ADR-0063.

The selected platform operation follows ADR-0073 and ADR-0084. The JVM cannot reinterpret an established
Contract. Physical realization follows §3.1.1.

Time recorded by the compiler is not automatically a Contract Identity input. This applies, for example, to:

- Scheduling.
- Query waiting.
- Profiling.
- Performance experiments.

If the compiler actually uses a temporal datum as a determining semantic input, represent that dependence
under its owning law and compiler-result reuse rules. Retained observation reuse follows §3.1.7.

The following purpose-specific references guide Time and Measurement Design under §3.3. They select neither a
physical provider nor a measurement mechanism.

| Purpose                                             | Default basis                                                                                              |
|-----------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| Execution-time measurement                          | A monotonic source for elapsed time, including compiler profiling and verification performance measurement |
| Relative Elapsed Time Budget                        | A monotonic source qualified for the actual boundary under ADR-0051 and §3.11                              |
| CPU-time measurement                                | A source or measurement law that accounts for actual CPU usage (§3.1.7)                                    |
| Ordinary external timestamps in journals and logs   | Unix Time                                                                                                  |
| Event order                                         | The applicable logical or occurrence order, not a wall timestamp as a substitute for it                    |
| Precise absolute-time work                          | A scale suitable for the required domain, such as TAI or the PTP time scale                                |
| Deterministic simulation and property-based testing | An established model reference and controlled source where time is part of the model                       |

Monotonicity is a progression property (§3.1.6), not an international absolute time scale. The actual
reference, suspension behavior and provider qualification remain to be established for each use. Event-order
claims follow §3.1.5. Relating an internal measurement to an external timestamp requires §3.1.4; a derived
timestamp is not a fresh external observation (§3.1.7).

The planned Time and Measurement subsystems use these laws. Their detailed contracts and realization remain
open. Shared meaning requires neither a centralized physical service nor Query Engine access. Provider
selection, execution mechanisms and optimization belong to Design. Qualification applies to the actual backend
and execution conditions under §3.11.

## 4. Consequences

The common temporal laws allow new composed meanings without another fundamental law (§3.1.1). Contract
judgments and engineering measurements use the same basis while retaining their own authorities.

Temporal products can depend on a selected semantic basis. Changes to rule data are handled under §§3.5 and
3.10.

The compiler must be able to report unsupported or insufficiently qualified operations without inventing a
Contract violation. The qualification and result boundaries follow §3.9, ADR-0084 and ADR-0074.

Backend conformance must establish physical measurement and control capability separately from interpretation
and simulated-time tests (§§3.11–3.12).

## 5. Conformance and Remaining Decisions

The temporal SDK interpretation work must compare qualified meaning with the exact selected Java/Kotlin
operations. Supporting design must also examine the applicable time systems and measurement practices.

At minimum, the conformance work should exercise the following cases. The referenced law supplies the expected
result. For a Source Operation, use the exact qualified callable profile. The tables identify test coverage;
they introduce no new judgment law.

| Case                                                                                             | Check                                                                                                                                 | Governing law    |
|--------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|------------------|
| DST spring gap and autumn overlap; correspondence and resolver outcome                           | Exercise a gap under an adjusting and a strict resolver. Project an overlap value and resolve it again. Compare the selected results. | §§3.1.4, 3.5     |
| Calendar day versus 24-hour Duration                                                             | Run the New York transition example.                                                                                                  | §3.4             |
| Same Instant with different offsets                                                              | Compare the two Offset Date-Time values using each specified comparison. Check Input Sameness independently.                          | §§3.2, 3.6       |
| Invalid date and parsing pattern                                                                 | Run the selected parser or constructor on invalid operands. Check the defined unsuccessful outcome.                                   | §§3.1.3, 3.7–3.9 |
| Range and precision boundaries                                                                   | Exercise each numeric outcome in the §3.7 table at its domain boundary.                                                               | §3.7             |
| Kotlin/Java Duration and Instant interoperation                                                  | Exercise infinity conversion and out-of-range Instant arithmetic under each selected profile.                                         | §3.9             |
| TZDB or locale-data change                                                                       | Change the determining data and check product reuse eligibility.                                                                      | §3.10            |
| System Clock or default-zone read                                                                | Compare an ambient call with a calculation on eligible supplied data.                                                                 | §3.10            |
| Cyclic counter or protocol timestamp; partial calendar position and cyclic comparison            | Exercise the eight-bit displacement and ordering examples. Check complete partial calendar positions in their own domains.            | §3.1.3           |
| Logical and physical ordering; temporal relations and execution order                            | Compare coordinate, logical and required execution relations for the same events.                                                     | §§3.1.3, 3.1.5   |
| Fractional time amounts and intermediate rounding                                                | Exercise both fractional amounts in §3.7 and a composition with intermediate rounding.                                                | §§3.1.1, 3.7     |
| Protocol quantity fields                                                                         | Exercise exponent decoding, a reserved correction value and a field defined as an error.                                              | §§3.1.2, 3.1.8   |
| Units with different defining conventions                                                        | Compare fixed-month conventions with a calendar amount. Exercise a physical-model conversion.                                         | §§3.1.2, 3.4     |
| Scale-dependent and model-derived amounts                                                        | Compare the quantity meanings in the Quantity Formation table.                                                                        | §3.1.2           |
| Clock cycles and physical-time constraints                                                       | Judge a combined requirement with and without the required cycle-to-time relation.                                                    | §§3.1.2, 3.1.4   |
| Epoch, leap seconds, and scale conversion                                                        | Exercise equal coordinate fields and a leap adjustment across the applicable POSIX, UTC, TAI and smeared-scale profiles.              | §3.3             |
| Internal and external time references                                                            | Relate a monotonic measurement to an external timestamp using a qualified correspondence.                                             | §§3.1.4, 3.13    |
| Inverse conversion and discarded distinctions                                                    | Attempt round-trip recovery after a many-to-one projection or intermediate rounding.                                                  | §§3.1.4, 3.7–3.8 |
| Composed calibration and reference segments; correlation at a later position or over an interval | Vary the calibration or reference segment. Exercise direct and chained conversions for each physical claim in §3.1.4.                 | §§3.1.4, 3.1.8   |
| Occurrence and record association                                                                | Compare the scheduled, actual and recorded positions of an event.                                                                     | §3.1.5           |
| Recurrence and completion; missed recurring deadline                                             | Compare anchored and prior-occurrence-based repetitions. Exercise a missed occurrence.                                                | §§3.1.5, 3.12.1  |
| Temporal completeness                                                                            | Exercise a time frontier or watermark with incomplete occurrences in the claimed scope.                                               | §3.1.5           |
| Repeated calendar adjustment; calendar operation with zone resolution                            | Run the month-addition example. Exercise the selected zone resolver at an intermediate position.                                      | §§3.1.1, 3.4–3.5 |

The following cases exercise physical and model-time observations. They also cover measurement claims and
time-based control:

| Case                                                                                 | Check                                                                                                                                   | Governing law         |
|--------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|-----------------------|
| Distinct read operations and model time                                              | Compare retained, derived and controlled readings with a fresh observation.                                                             | §§3.1.6–3.1.7         |
| Monotonic progression and suspension                                                 | Compare nondecreasing sources with different rates or suspension behavior.                                                              | §3.1.6                |
| Source disruption and continuity                                                     | Exercise migration, reset and source replacement with and without valid compensation.                                                   | §3.1.6                |
| Source adjustment or holdover; clock adjustment and correlation                      | Change progression after a correlation. Check the validity of the former relation or guarantee during holdover.                         | §§3.1.4, 3.1.6, 3.1.8 |
| Disjoint and overlapping measurement extents; nonuniform samples and attributed time | Exercise the measurement extent table. Compare equal totals with different intermediate positions or targets; include disjoint extents. | §3.1.7                |
| Acquisition and event boundary                                                       | Use delayed or indirect acquisition relative to the intended event. Check the resulting claim.                                          | §3.1.7                |
| Missing and partial measurements                                                     | Compare an unavailable reading with a censored observation.                                                                             | §§3.1.7, 3.11         |
| Measurement model and instrumentation; measurement perturbation                      | Compare acquired and model-generated samples. Exercise the instrumented elapsed extent and a claim about uninstrumented execution.      | §3.1.7                |
| Shared-computation attribution                                                       | Attribute one computation to multiple subjects; check the physical total and Budget charge separately.                                  | §3.1.7 and ADR-0051   |
| Aggregate and cumulative measurement reuse                                           | Compose samples with counts and successive cumulative totals, including a reset.                                                        | §3.1.7                |
| Marginal and joint latency results                                                   | Compare equal marginal distributions with different underlying pairings.                                                                | §3.1.7                |
| Parallel execution and interference                                                  | Exercise overlap, interruption and claims of causal latency influence.                                                                  | §3.1.7                |
| Incomplete execution decomposition                                                   | Exercise overlapping components and an unexplained portion of the extent.                                                               | §3.1.7                |
| Correlated measurement errors; overlapping uncertainty bounds                        | Run the shared-offset example with and without its dependency. Compare order claims under overlapping bounds.                           | §3.1.8                |
| Physical bound and statistical range                                                 | Evaluate claims based on each applicable statement in the Uncertainty Semantics table.                                                  | §3.1.8                |
| Authenticated external time assertion                                                | Check its physical claim separately from the authentication result.                                                                     | §§3.1.7–3.1.8         |
| Clock correlation and precision                                                      | Convert with exactly recorded coefficients but an uncertain physical correlation.                                                       | §§3.1.4, 3.1.8        |
| Nonlinear uncertainty and finite bounds                                              | Run the square example and check containment after finite-format rounding.                                                              | §3.1.8                |
| Inconsistent and incomplete uncertainty evidence                                     | Exercise conflicting constraints and an unreported error contribution.                                                                  | §3.1.8                |
| Uncertainty at a limit; accepted observation near the allowance                      | Exercise all relations to the limit in §3.1.8. Check the separate measurement-based Budget judgment.                                    | §§3.1.8, 3.11         |
| Timer expiration and cancellation; failure and later cleanup                         | Exercise response and Failure establishment before actual cessation or resource release.                                                | §§3.12.1, 3.12.3      |
| Physical Budget boundary                                                             | Verify simulated judgment logic separately from physical boundary and stop capability.                                                  | §§3.11–3.12           |
| Deadline and delayed notification; delayed monitor and late result attempt           | Delay notification until after a result establishment attempt at a prohibited time.                                                     | §3.12.2               |
| Observation followed by delayed establishment                                        | Use an earlier observation that lacks applicability to the result establishment boundary.                                               | §3.11                 |
| Concurrent completion and Failure establishment                                      | Exercise both competing establishment orders.                                                                                           | §3.12.2               |
| One observation violating multiple Budgets                                           | Exercise the stop transition with several required Failures.                                                                            | §3.12.2               |
| Shared interference and several time failures                                        | Exercise common explanatory evidence and the separate Failure evidence for each subject.                                                | §3.12.3               |
| Deadline communicated across time domains                                            | Transmit a remaining duration and check the original deadline after receipt.                                                            | §3.12.1               |
| Time-control latency and qualified bounds                                            | Compare an observed control delay with a claim of a future physical bound.                                                              | §§3.1.8, 3.12.4       |

The remaining decisions are owned as follows:

| Owner                                | Work still requiring a separate decision or design                                                                                                                    |
|--------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ADR-0064                             | Revisit the Instant Leaf profile under §3.3. Complete its reference and representation determinants before publishing an eligible Leaf profile.                       |
| ADR-0079 / ADR-0084 and SDK Design   | Qualify the exact temporal callable inventory for declared JDK and Kotlin profiles, including applicable external data.                                               |
| ADR-0051 and Backend Conformance     | Align the elapsed-time judgment and stop law with §§3.11–3.12; qualify boundary observations and competing establishment.                                             |
| Each other 1D ADR                    | Decide whether new time-dependent judgments or time-based resource quantities are needed.                                                                             |
| ADR-0082                             | Complete any remaining temporal-domain and operation laws under the established fundamental laws. Settle their semantic representation and detailed conversion laws.  |
| ADR-0078                             | Decide additional Numeric Formats if required for exact temporal Datum support; do not infer Rational support from §3.7.                                              |
| ADR-0082 and Measurement Design      | For concrete measurement models and uncertainty operations, establish their result laws and representations under §§3.1.7–3.1.8.                                      |
| Compiler Time and Measurement Design | Realize the purpose-specific bases of §3.13. Qualify providers and execution boundaries. Establish control, evidence acquisition and physical measurement mechanisms. |
| Verification and QA Design           | Distinguish simulated-time tests from physical timing tests. Verify establishment races, delayed observation and instrumentation overhead.                            |
| Governance and Whole World Design    | Establish system-wide execution and failure-containment policy using the temporal evidence; no such policy is selected here.                                          |

This Draft makes no claim that the entire standard-library callable inventory has been qualified, or that a
JVM backend already satisfies hard elapsed-time Budget enforcement. Those are evidence requirements for
execution, not facts established by accepting a common temporal-semantics ADR.