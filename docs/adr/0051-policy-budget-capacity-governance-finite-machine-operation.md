# ADR-0051: Policy, Budget, Capacity, Governance, and Finite-Machine Operation

## Status

Proposed

## Date

2026-08-03

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary

---

## 1. Context

A real machine is finite.

It cannot accept every load, spend without limit, keep every result, or use every rule at once. It works in a changing
world with limited time, material, energy, memory, attention, and authority.

A good machine does not pretend that these limits do not exist. It knows the conditions under which it can operate,
keeps its work inside declared limits, and changes its response when the situation changes.

This is true for software as well as physical machinery.

A software machine may have enough raw computing power to continue and still need to delay work, choose a cheaper path,
reduce its activity, refuse a request, or use another operating rule. If those decisions remain hidden inside ordinary
code, the real machine law is scattered across branches, callbacks, configuration, and runtime objects.

Policy, Budget, Capacity, and Governance belong in contract theory for this reason.

```text
Policy
    selects the prepared response contract that applies to an established situation

Budget
    declares finite consumable allowance

Capacity
    declares the wall the machine may currently admit

Governance
    declares which contract world is valid
```

These contracts describe a machine that understands its limits and can act within them.

They are grouped because they serve that common purpose. They do not form one master contract, and none may absorb the
others.

This ADR records the shared reason for the group and establishes the current Policy and Budget foundations. Budget,
Capacity, and Governance remain independent contracts. This revision decides the basic Budget meaning, explicit
allocation law, accounting position, quantity boundary, and separation from Capacity. Capacity and Governance remain
next. Policy will return after their material, judgment, and authority laws are fixed.

---

## 2. Problem

Software uses the word `policy` for many unrelated things.

```text
authorization rule
configuration file
security check
cost limit
routing preference
compiler heuristic
deployment restriction
retention setting
business rule
operating procedure
```

The name often means only that someone moved an `if` statement out of ordinary code.

The condition may now live in YAML, a callback, a strategy object, a rule engine, or a small language. That move can
make code easier to organize, but it does not explain what the rule means, what situation it reads, what result it owns,
how it combines with other rules, or who makes it active.

Software work does contain strong Policy systems in narrow areas. Authorization is the clearest example. Those systems
become precise by closing one question, one input shape, one result family, and one rule-combination law.

That does not give Kontrakt a general Policy definition.

Kontrakt needs Policy for more than access control or safety. A compiler may change its response to a call site. A
router may change its preferred path. A reservoir may change its release plan. A factory may change the next job it
runs. A spacecraft may delay one task and protect another.

The common point is not denial, permission, or selection by itself.

The common point is this:

> The machine meets an established situation, and a rule prepared in advance selects the response contract that applies
> to
> that situation.

Without this boundary, Policy becomes another name for every Boolean condition in the system. Budget and Capacity become
Policy limits. Governance becomes a registry that both chooses and runs rules. Objectives, safety laws, and
implementation steps become mixed into one evaluator.

Kontrakt requires a smaller meaning.

---

## 3. Decision Drivers

Policy must cover more than prohibition.

The same machine can meet different situations while remaining in the same machine state. Each situation may require a
different prepared response contract.

A Policy must therefore express a stable relation between:

```text
an established situation
and
one declared response contract
```

The selected contract may permit a proposed action, use one operating mode, delay work, reduce output, or prefer one
path.

The selection is still only a judgment. Policy does not perform the action or decide whether state movement is legal.

Policy must remain finite enough to inspect, validate, compare, test, and lower. Arbitrary executable code would make
the implementation the only readable source of meaning.

Policy authority must not live in a lambda, callback, strategy object, service lookup, environment variable,
registration order, or backend optimizer.

The general contract meaning may be broader than the first Kontrakt version. V1 may support only a small response form
if that is what can be fully checked and lowered.

Determinism remains mandatory.

```text
same Policy
same established situation
same declared response-contract material
same contract world
    -> same Policy Judgment
```

A response that changes because of thread timing, file order, remote availability, hidden mutable state, or random model
output has not been fully presented to the contract machine.

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

## 4. Policy in Real Machines and Decision Systems

Policy is easier to understand by looking at machines that must keep working while their surroundings change.

The names differ across fields. One field may say `operating rule`, another may say `control strategy`, `dispatch rule`,
`decision rule`, or `sequence of operation`. The useful question is not what the field calls it. The useful question is:

> What situations does the system meet, what matters in those situations, and what response has been prepared for each
> one?

These examples show places where a finite Policy decision may exist. They do not mean that every decision system in the
field is Policy or that Kontrakt V1 can express the whole field.

### 4.1. Nuclear Operation

A nuclear plant must value reactor stability, cooling margin, equipment condition, radiation control, grid demand, and
the ability to shut down safely.

It may meet situations such as:

```text
electricity demand rises while all operating margins remain healthy

coolant temperature rises faster than expected

one pump or cooling path becomes unavailable

a maintenance condition reduces the allowed operating range

an abnormal signal approaches a protection threshold
```

The operating Policy may relate those situations to responses such as:

```text
raise power only by an approved step

hold the current output

reduce output

move to a more conservative operating mode

begin an orderly shutdown
```

The important point is not that the plant chooses from a list at the last moment. The important point is that the
response for each known operating situation has been prepared before the situation occurs and is applied while the plant
is running.

Once an absolute protection condition is reached, the answer is no longer a Policy choice. A separate protection law and
its machinery must take control.

### 4.2. Reservoir and Water Operation

A reservoir may serve flood control, drinking water, irrigation, power generation, and river ecology at the same time.
The important criteria include current storage, expected inflow, season, downstream demand, flood risk, and the amount
of water that must remain for later use.

It may meet situations such as:

```text
heavy rain is forecast while storage is already high

a dry season begins with low storage

downstream demand rises

power demand is high but water supply must be preserved

river flow must be kept above an ecological minimum
```

The Policy may connect those situations to responses such as releasing more water, holding water, moving water to
another reservoir, reducing power generation, or reserving water for later demand.

The dam and gates only carry out the result. The operating Policy explains why the machine responds differently to the
same physical reservoir at different times.

### 4.3. Manufacturing and Production Control

A factory must balance delivery time, product quality, setup cost, machine wear, queue length, material availability,
and predictable output.

It may meet situations such as:

```text
an urgent order enters a long queue

a bottleneck machine begins to fall behind

a tool approaches its wear limit

defect measurements begin to drift

several jobs are ready but require different setups
```

The Policy may respond by running the urgent job first, grouping jobs that share one setup, lowering speed, changing a
process recipe, sending work to another line, or stopping for maintenance.

Machine limits determine what can be done. The production Policy determines how the factory responds while several
usable responses remain.

### 4.4. Traffic-Signal Operation

A traffic controller may value road safety, total delay, pedestrian access, public transport priority, emergency access,
and stable traffic flow.

It may meet situations such as:

```text
one direction develops a long queue

pedestrian demand rises

an emergency vehicle approaches

a bus is behind schedule

an accident blocks part of an intersection
```

The Policy may extend one green phase, shorten another, give an emergency path priority, hold traffic for pedestrians,
or switch to a safer fixed sequence.

The controller hardware changes the lights. The Policy relates the traffic situation to the response the controller
should apply.

### 4.5. Energy and Microgrid Operation

A local energy system must consider electrical stability, cost, battery life, expected demand, local generation, and the
ability to keep important loads running during failure.

It may meet situations such as:

```text
demand rises sharply

solar generation exceeds current use

the main grid becomes unavailable

battery charge becomes low

electricity price rises during a peak period
```

The Policy may import power, export power, charge storage, discharge storage, reduce optional loads, or separate from
the main grid.

Component ratings and electrical law limit the available responses. Policy decides how the system should respond within
those limits.

### 4.6. Building Operation

A building control system balances comfort, air quality, energy use, equipment life, occupancy, and the need to recover
from faults.

It may meet situations such as:

```text
rooms become occupied after a quiet period

carbon dioxide rises in one zone

outdoor temperature changes quickly

energy price enters a peak period

one heating or cooling unit becomes unavailable
```

The Policy may increase ventilation, change a setpoint, start another unit, reduce optional conditioning, or move to a
fallback sequence.

Sensors establish the situation. Controllers and actuators perform the response. The declared operating Policy connects
the two.

### 4.7. Spacecraft Operation

A spacecraft must protect mission value while working within power, thermal, communication, storage, and fault limits.
Inside those limits, it may use prepared operating responses for known classes of situation.

It may meet situations such as:

```text
communication with the ground is lost

available power drops

a component becomes too warm

a short observation window opens

one planned activity takes longer than expected
```

A finite Policy may delay optional work, protect power for communication, enter a prepared safe operating mode, use a
declared observation window, or refuse optional work that would threaten later mission goals.

The spacecraft does not invent these responses from nothing. The mission prepares the relation between known situations
and acceptable responses before launch, then the onboard system applies it when the situation is established. Open-ended
planning, rebuilding an existing schedule, activity search, and resource optimization are separate problems and are not
promised by this Policy Contract.

### 4.8. Irrigation and Agricultural Control

An irrigation system may value crop health, water conservation, energy cost, soil condition, weather, and the limited
water available for the whole growing period.

It may meet situations such as:

```text
soil becomes dry while rain is unlikely

soil is dry but heavy rain is expected soon

one field has a higher crop need than another

water supply falls below the seasonal plan

high wind would waste sprayed water
```

The Policy may irrigate now, delay irrigation, reduce the amount, change the field order, or preserve water for a more
important growth stage.

The pump does not define that Policy. It only carries out the declared response.

### 4.9. Compiler Optimization

A compiler must preserve program meaning. After that fixed law is protected, it may use prepared Policy responses for
finite compile situations involving execution speed, generated code size, compile time, target-machine conditions, and
information from previous runs.

It may meet situations such as:

```text
a small function is called very often

a transformation would speed one loop but enlarge the program

compile time is becoming expensive

the target machine has a useful instruction for one pattern

profile data shows that one branch is rarely taken
```

A finite Policy may select or judge a prepared response such as `InlineCall`, `PreserveCall`, `ApplyTransformation`,
`SkipTransformation`, or `UseCheaperAnalysis`.

The compiler backend performs the transformation. Policy declares how the compiler should respond to the established
compile situation after correctness has already been protected. A full cost model, candidate search, weighted score, or
learned inlining decision is not made Policy merely because it affects the compiler.

### 4.10. Network Routing

A network may value reachability, low delay, low cost, stable paths, trusted links, and balanced use of available
connections.

It may meet situations such as:

```text
several routes can reach the same destination

the shortest route becomes unstable

a low-cost route becomes congested

an important service needs lower delay

one route crosses a link the operator wishes to avoid
```

The Policy may prefer one route, lower another route's priority, keep the current route for stability, or move important
traffic to a faster path.

The router installs and uses the path. Policy declares how route conditions correspond to a routing response.

### 4.11. Treatment Decisions

A treatment system may consider current symptoms, past response, side effects, test results, age, other illness, and the
risk of delaying treatment.

It may meet situations such as:

```text
the first treatment is not working

side effects become severe

a test result changes the likely diagnosis

the patient improves without intervention

several treatments remain reasonable but carry different risks
```

The Policy may continue treatment, change treatment, lower the dose, request another test, or wait and observe.

The medical act remains separate. The Policy records how established patient information should guide the next response.

### 4.12. Economic Operation

An economic authority may value stable prices, employment, financial stability, sustainable growth, and the ability to
respond to later shocks.

It may meet situations such as:

```text
inflation rises while employment remains strong

inflation falls but financial stress grows

growth weakens while prices remain stable

a market shock threatens short-term funding

previous action has not yet had time to show its full effect
```

The Policy may raise a rate, lower it, hold it, provide short-term liquidity, or wait for more evidence.

The important point is again the relation between situation and response. The final action is carried out by separate
institutions and mechanisms.

### 4.13. Common Form

Across these fields, Policy has the same basic place.

```text
A situation is established.

Governance has already established the active Policy world.

The Policy selects the declared response contract that applies to the situation.

If that response requires movement, the State Machine judges whether the movement is legal.

Implementation realizes the authoritative results.
```

The criteria differ by field.

```text
nuclear operation
    stability, cooling, equipment condition, safe shutdown

reservoir operation
    flood risk, water supply, power, ecology

manufacturing
    quality, delivery, setup, wear, throughput

traffic control
    safety, delay, pedestrians, public transport, emergency access

energy operation
    stability, cost, storage life, resilience

building operation
    comfort, air quality, energy, equipment life

spacecraft operation
    mission value, power, heat, communication, fault risk

compiler optimization
    execution speed, code size, compile time

routing
    reachability, delay, cost, stability, trust
```

Those criteria help design the Policy. They are not the Policy by themselves.

They matter only after fixed laws have removed invalid response contracts. Correctness, legal limits, physical
impossibility, and required safety walls are not criteria that Policy may balance against speed, cost, output, or
comfort.

These examples cover only finite situation-based response decisions inside those fields. They do not claim that every
optimizer, planner, scheduler, continuous control system, cost model, or learned decision system is a Policy Contract.

A Policy is the declared law that selects which prepared response contract applies when the relevant situation is
established.

---

## 5. Decision

### 5.1. Finite-Machine Operating Contracts

Policy, Budget, Capacity, and Governance are retained as independent contracts.

They belong in contract theory because a realistic machine must understand the world in which it operates, accept its
limits, and respond without pretending that unlimited work is possible.

```text
Policy
    selects the prepared response contract that applies to an established situation

Budget
    declares explicit finite allowance allocated before governed consumption

Capacity
    limits what the machine may admit

Governance
    establishes the valid contract world
```

The group is not limited to refusal.

A machine may reduce output, delay optional work, use another route, preserve a call, change an operating mode, or
refuse a proposal. Each result must remain explicit and attributable to the contract that owns it.

Budget is not merely a post-execution threshold. The applicable allowance must be explicitly declared and established
before governed work may consume it. Kontrakt may partition that allowance among generated machinery, runtime services,
diagnostics, caches, and user execution, but only inside the declared total and under an explicit allocation law.

### 5.2. Policy Contract Meaning

A Policy Contract declares which prepared response contract applies when one declared situation is established.

```text
Policy Contract:
    the contract that relates one established situation
    to one declared response contract
```

A shorter statement is:

> Policy is the declared law for selecting a response contract from an established situation.

This decision replaces the earlier temporary description of Policy as the activation of judgment criteria. Policy does
not change the judgment law of another contract. It selects one declared response contract inside the active contract
world.

The selection is a contract judgment. It is not the physical act that carries out the response.

Policy does not discover the situation. It consumes material that has already been resolved and established for the
judgment.

A Policy situation is one declared finite projection of the established contract world. Any material established under
an explicit Contract may participate. No contract kind is privileged merely because it represents business work,
pipeline processing, machine state, judgment, limit, failure, publication, or another part of machine operation.

Policy declares which established contract material may participate in that situation. It names the contract material
and, where necessary to remove ambiguity, the contract result that establishes it. It does not name the backend,
profiler, collector, storage mechanism, or runtime object that realizes that contract result.

Established contract material becomes evidence for one Policy judgment through this declared relation. Evidence is not a
separate material hierarchy and does not acquire authority over the contract that established the material.

Policy receives situation material as immutable input. It may compare, match, and use that material to select a response
contract. It may not modify, replace, reclassify, reinterpret, or republish that material under the authority of the
Contract that established it.

Policy may read the current machine state when that state is presented as one declared part of the situation. Reading
the state does not give Policy authority over state meaning or state movement.

Policy does not perform the response. It selects the response contract that applies. Implementation may realize that
contract only after all applicable contract authorities allow continuation.

Policy is not merely a goal or a list of criteria.

```text
Goal
    explains what the designer wishes to protect or improve

Criteria
    identify what matters when the situation is judged

Policy
    selects the prepared response contract that applies to the situation
```

### 5.3. Policy Judgment Forms

The general Policy judgment selects one exact declared response contract.

```text
Established Situation
    -> Selected Response Contract
```

The selected contract is not a physical act. Selecting `ReducedOperation` does not reduce power. Selecting
`LowLatencyRoute` does not install a route. Selecting `PreserveCall` does not rewrite compiler output.

A smaller Policy surface may judge one proposed response contract instead of selecting from a closed set.

```text
Permission
    the proposed response contract may apply

Refusal
    the proposed response contract must not apply
```

This ADR does not define preference, ranking, scoring, or prescription as Policy judgment forms. Policy connects an
established situation to one exact response contract prepared for that situation.

### 5.4. V1 Candidate Boundary

The conservative V1 candidate is:

```text
one exact proposed response contract
+ one established situation
+ one exact Policy
+ one applicable contract world
    -> Permit
    or Refuse
```

Under this form, another contract surface presents one proposed response contract. Policy judges whether that contract
matches the established situation.

Policy does not search an open set, run an optimizer, schedule work, or execute the result.

V1 does not claim to express:

```text
continuous optimization

weighted score calculation

open-ended candidate search

automatic planning or rebuilding a schedule

learned response ranking

arbitrary cost models

a response-contract set created during judgment
```

This form fits the current Interaction Manifest and keeps lowering finite. It remains a candidate, not the final V1
decision.

### 5.5. Budget Foundation and Deferred Capacity and Governance Scope

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
User
    declares the subject, quantity, boundary, allowance, and required guarantee

Kontrakt
    establishes the Budget position
    derives any internal allocation
    measures or establishes charge material
    performs accounting
    produces Budget judgment
    realizes enforcement
```

The user does not place `consume`, `remaining`, `checkpoint`, allocator, scheduler, or process-control calls inside the
Operation implementation merely to make Budget work.

Budget is explicit. Kontrakt must not infer that a machine has a Budget merely because resources are finite or
measurable. A selected Budget slot must declare one exact Budget or explicit absence. The final absence syntax remains
deferred.

This revision does not create separate `Deadline`, `Windowed`, `Aggregate`, `CurrentHeld`, `Concurrent`, or
`GrowthBound`
families.

```text
elapsed-time deadline
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

Capacity and Governance remain independent contracts.

```text
Capacity
    declares the admissible machine wall and owns its judgment law

Governance
    establishes the valid contract world and owns its activation law
```

Policy may use only the material that Budget, Capacity, and Governance explicitly establish and only when the Policy
situation declares that relation. Policy may not perform accounting, measure the machine, discover resources, allocate
allowance, or activate its own world.

The next design sequence is Capacity and Governance. Policy will return after their material, judgment, and authority
laws are fixed.

---

## 6. Budget Contract

### 6.1. Budget Meaning

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

### 6.2. Explicit Allocation Before Consumption

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

### 6.3. Canonical Budget Material

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

required enforcement level

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

required enforcement level
    what control must the realization provide?

applicable contract world
    under which declared world does this Budget have authority?
```

These coordinates must not be replaced by a collection of named Budget families. A deadline, time window, parent quota,
or current occupancy does not become a new family merely because the same fields take different values.

The exact authoring syntax and canonical identity bytes remain deferred.

### 6.4. Budget Quantity Sources

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

### 6.5. Time Quantity

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

### 6.6. Memory Quantity

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

### 6.7. Accounting and Renewal

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

### 6.8. Allocation and Composition

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

### 6.9. Reservation as a Possible Accounting Extension

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

### 6.10. Budget Judgment

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

### 6.11. Budget and Implementation

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

### 6.12. Budget and Capacity

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

### 6.13. Budget Determinism

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

### 6.14. V1 Budget Boundary

V1 must provide an explicit user-facing Budget surface. Budget must not be an implicit runtime mode.

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

explicit required enforcement level

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

## 7. Policy Contract

### 7.1. Established Situation

A Policy judges one established situation.

A situation is one declared finite projection of the established contract world. The established contract world is the
material and judgments established under explicit Contract authority. It is not a mutable context object, a registry, or
an implicit population that Policy may scan.

```text
Established Situation:
    one closed finite projection
    of established contract material
    presented to one Policy judgment
```

Any explicit Contract may establish material that participates in a Policy situation. This includes presentation and
boundary material, admission and refusal judgments, reference and resolution results, canonicalization and lowering
results, Facts and Operation results, Invariant judgments, State and Transition material, Policy judgments, Budget and
Capacity results, Governance and Version material, Failure and Publication judgments, and Diagnostic material when that
material is explicitly established.

This does not give every Contract the same obligation. It gives material established by every Contract the same
eligibility to participate in Policy when the relation is explicitly declared. Business work is not privileged over
pipeline processing. Pipeline processing is not privileged over machine state, limits, failures, publication, or any
other contract authority.

Examples include:

```text
compiler call-site shape and established call-frequency profile

current inventory position and material established by Budget

patient history and Admission judgment selected for one treatment decision

route conditions and material established by Capacity

operating mode, current State, Invariant judgment, and machine-condition Facts

Publication judgment and the established Operation result to which it applies
```

Situation material may come from any declared contract surface that establishes a result suitable for later judgment. It
may also include profile material produced by an ordinary Operation from an explicit finite Fact input and established
through the normal contract pipeline.

Established material is not inherently owned by Policy and is not inherently Policy evidence. It becomes evidence for
one Policy judgment only through a declared situation binding.

```text
Established Contract Material
    +
Declared relation to one Policy judgment
        ->
Policy Evidence
```

This evidence role does not change the material's meaning, owner, identity, or authority. The same established material
may serve as evidence for Policy, Publication, diagnostics, verification, or another declared judgment without becoming
a new kind of material.

Policy must bind each situation coordinate to exact contract material, not to the machinery that happens to produce it.

```text
Allowed
    Result Fact established by a declared Operation result
    Admission judgment established by Admission
    canonical result established by Canonicalization
    Invariant judgment established by Invariant
    CurrentState established by the State Machine
    material established by Budget
    material established by Capacity
    Publication judgment established by Publication

Forbidden
    JfrProfiler.hotness
    RingBuffer.currentSize
    BudgetCounterImpl.remaining
    CapacityBackend.privateHeadroom
    StateBackend.privateFlag
```

The allowed Budget and Capacity forms do not assume their final result shapes. The exact material remains owned by the
Budget and Capacity contracts that will be decided next.

A Fact kind alone is not sufficient when more than one contract result could establish materially different Facts of
that kind. The binding must remain exact enough to identify the contract law that gives the material its meaning. The
final source syntax and canonical binding material remain deferred.

Policy receives established material as immutable input.

```text
Policy may
    read
    compare
    match
    use as situation material

Policy may not
    modify
    replace
    reclassify
    reinterpret
    invalidate
    republish under the original Contract authority
```

Policy produces only its own Policy judgment. It cannot write another Contract's result surface.

The current machine state may appear as one declared situation coordinate.

```text
operatingState = Running
```

Policy may use that value together with temperature, load, demand, or other established material. It may not inspect the
State Machine, derive legal target states, read hidden transition history, or decide whether movement is legal.

```text
Policy
    uses current state as part of the situation

State Machine
    owns state meaning and judges movement
```

When the current state alone fixes the only legal result, Policy has no separate decision to make. Policy belongs where
the same state can meet different situations and those situations require different prepared response contracts.

Policy must not directly read a clock, environment variable, file, network service, sensor, repository, registry, random
generator, mutable singleton, profiler, collector, counter, cache, or backend object.

Such information must first cross its proper boundary and become explicit material under declared contract authority. A
backend may observe, count, store, or reduce information only as a realization of those declared contracts. If the
required situation cannot be established, Policy judgment cannot be formed.

### 7.2. Declared Response Contracts

A Policy requires a declared response-contract surface.

The smallest form is one exact proposed response contract.

```text
Proposed Response Contract:
    one named response contract presented for Policy judgment
```

A broader form may declare a finite set of prepared response contracts.

```text
Declared Response Contracts:
    the closed response contracts that one Policy may select
```

Response contracts must be named, finite, and already declared. Policy may not create a new contract, discover one by
scanning implementations, load one from a plugin, call a callback, query a service, or inspect arbitrary host types.

Policy may select only within the active contract world. It may not replace that world, select another Policy, or
rewrite a response contract.

The response-contract surface does not mean that Policy performs the response. It only gives the judgment a closed and
inspectable meaning.

A response contract must describe a real prepared machine response. It must not be a name wrapped around one result from
a scoring scheme or optimizer. If response contracts multiply only to encode changing weights, score ranges, or
calculated combinations, the model is hiding an optimization function and is outside this Policy Contract.

### 7.3. Selection Law

A Policy is prepared before the particular interaction.

It declares which prepared response contract applies to each relevant situation. It does not invent a new contract or a
new rule while judging one instance.

```text
same Policy
same established situation
same declared response-contract material
same contract world
    -> same selected response contract
```

The law must be finite, named, inspectable, and explicitly bound.

The source may use a restricted vocabulary such as:

```text
exact coordinate equality
finite membership
required or forbidden marker
closed Boolean relation
one declared situation coordinate compared with one declared bound
explicit default or explicit absence
```

A situation coordinate must describe the established world. It must not already rank, score, or choose response
contracts.

```text
Situation material
    currentLoad = High
    callFrequencyClass = Hot
    estimatedCodeGrowth = Large
    queueLength = 120

Not situation material
    inlineScore = 82
    bestResponse = Inline
    optimizerRecommendation = PreserveCall
    weightedUtility = 0.73
```

A threshold may compare one declared situation coordinate with one declared bound. It may not hide a formula that
combines several criteria, assigns weights, ranks response contracts, calls an optimizer, or uses a learned
recommendation.

Computing a value before Policy judgment does not make it neutral. If changing the Policy purpose or the importance
given to its criteria changes that value, the value contains Policy judgment and may not be presented as situation
material.

Two tests keep the boundary clear:

> If a value says what the established world is like without judging response contracts, it may be situation material.

> If a value says which response contract is better, preferred, or already selected, it belongs to Policy and must not
> be
> hidden as situation material.

The final V1 vocabulary remains open. Arbitrary methods, loops, callbacks, host-language control flow, reflection,
repository access, and user-defined evaluators are not Policy material.

### 7.4. Applicability

A Policy must have a declared area in which it applies.

That area may include an Operation, presentation, machine, operating mode, contract version, or another explicit
coordinate chosen by the final authoring model.

Applicability may not be guessed from package location, class name, registration order, call site, current thread,
environment name, or the backend that loaded the Policy.

A Policy that does not apply must not silently produce permission or refusal.

```text
NotApplicable
    != Permit
    != Refuse
```

The exact representation of absence remains open. Hidden fallback is rejected.

### 7.5. Governance and Active Policy

A Policy does not make itself active.

Governance establishes which Policy material is valid for the machine and interaction. Policy may not select another
Policy, change its own version, search a registry for a replacement, or fall back to the newest declaration.

```text
Governance
    establishes the active Policy world

Policy
    selects one response contract inside that world
```

A configuration file or deployment value may help realize Governance. It does not become contract authority merely by
containing a Policy name.

Governance may not obtain its authority from the Policy that it activates. Policy may not select, validate, or create
the Governance that makes it active.

```text
Forbidden cycle
    Governance activates Policy
    Policy declares that Governance valid
```

The complete Governance law remains deferred, but authority may not be created by such a cycle.

### 7.6. Policy and Purpose

Purpose guides every contract. Policy gives purpose one explicit operating form when different established situations
can call for different response contracts that remain allowed.

A purpose explains why a Policy was designed.

```text
reduce code size
protect cooling margin
lower cost
improve delivery time
preserve water
increase comfort
```

The purpose does not automatically determine one Policy. Different Policies can pursue the same purpose while giving
different importance to the allowed criteria.

That balance must already be visible in the explicit selection law. Policy judgment may not calculate a hidden score or
ask an optimizer which response contract is best.

Kontrakt does not infer Policy from a score function, optimizer, or learned model. A score or recommendation that
already ranks response contracts may not enter as if it were neutral situation material. The active selection law must
be explicit.

### 7.7. Policy and Fixed Laws

Every established contract material has equal eligibility to participate in a Policy situation. That does not give
Policy shared authority over the Contract that established it.

Policy cannot weaken, reverse, erase, or rewrite a result that another Contract has already made final.

If a response contract is impossible, unlawful, outside the available allowance, outside the admissible wall, or
requires movement that is illegal from the current machine state, Policy cannot make it valid.

Policy may read an Invariant violation and select a response to that violation. It may not reclassify the violation as
satisfaction. Policy may read a Capacity or Budget result and select a response to it. It may not replace that result
with one produced under Policy authority.

```text
Allowed
    Invariant Violated
        -> select one declared refusal or recovery response contract

Forbidden
    Invariant Violated
        -> treat the Invariant as Satisfied
```

Policy may read the current state as part of the situation, but it does not decide which state movements are legal. That
judgment remains with the State Machine.

Policy serves the part of machine operation where the same established contract world can admit more than one prepared
response contract and the declared situation selects which one applies.

This gives two simple tests:

> Policy may respond to established contract material. It may not redefine that material.

> When a stronger law has already fixed the only legal result, Policy must not recreate that result as a Policy
> selection.

### 7.8. Policy and Implementation

Policy selection and implementation are separate.

```text
Policy
    selects the response contract that applies

State Machine
    judges any movement required by that response contract

Implementation
    performs, skips, routes, stores, publishes, or actuates accordingly
```

The implementation may use generated branches, decision tables, masks, specialized code, static gates, or backend
adapters. None owns Policy meaning.

Replacing those mechanisms must not change the judgment for the same canonical material.

A scheduler, optimizer, router, controller, strategy object, callback, plugin, service, or command handler may propose
or realize work around a selected response contract. A profiler or collector may realize declared observation and
material-establishment contracts. None may become the source of Policy meaning.

Policy may name the established material it requires and the contract result that gives that material meaning. It may
not name the backend producer that collects, stores, calculates, or publishes the material.

```text
Policy
    consumes an established Profile Fact
    bound to the contract result that establishes it

Compiler and backend
    derive probes, counters, bounded storage, aggregation,
    and access machinery from the complete contract graph
```

Replacing instrumentation with sampling, one bounded storage layout with another, or one generated evaluator with
another must not require a Policy change when the same canonical material is established.

A full optimizer, planner, scheduler, or continuous control system is not converted into Policy by giving its output a
response name. Only an explicit finite situation-to-response law is Policy material under this ADR.

### 7.9. More Than One Policy

More than one Policy may appear relevant to one situation. Their relation cannot be guessed.

Possible laws include:

```text
one exact Policy only

all active Policies must permit

explicit refusal dominates permission

one declared order combines several Policy judgments
```

Kontrakt must either establish one exact Policy or declare one exact combination law. File order, registration order,
priority convention, last-write-wins, newest-version-wins, and callback order may not decide authority.

The V1 law remains open.

### 7.10. No Hidden Override

A Policy judgment may not be overridden by debug mode, administrator identity, exception handling, environment variable,
or runtime flag unless the contract explicitly declares such a relation.

This ADR does not admit that relation in V1.

An implementation escape hatch is not Policy material. If it changes the contract result, implementation has acquired
contract authority.

### 7.11. Policy Identity

A Policy requires canonical identity.

The identity must distinguish at least:

```text
Policy kind
area of application
selection-law material
combination material
applicable contract world
```

Host class name, object identity, constructor call, source-file order, reflection handle, and generated evaluator type
may not define Policy identity.

The exact identity material remains deferred until the authoring and lowering surface is fixed.

---

## 8. Policy Judgment

### 8.1. Inputs

One Policy judgment requires explicit material.

```text
Policy handle
established situation with exact contract-material bindings
proposed response contract or declared response-contract set
applicable contract world
```

If any required material is absent, unresolved, corrupt, or unavailable, the machine must not fabricate a judgment.

The later Failure Contract will decide how that condition is classified.

### 8.2. Result

The result must be typed as Policy judgment, not returned as an unnamed Boolean.

The general result identifies one exact selected response contract.

```text
PolicySelectedResponseContract
```

A conservative V1 surface may instead judge one proposed response contract.

```text
PolicyPermitted

PolicyRefused
```

The result must retain enough identity to show which Policy, situation material, response contract, and contract world
produced it. Diagnostic text is not the identity.

### 8.3. Meaning of Permission and Refusal

```text
PolicyPermitted
    means only that the proposed response contract may apply under the Policy

PolicyRefused
    means the proposed response contract must not apply under the Policy
```

Permission does not prove that the whole interaction must continue. The machine must preserve which authority stopped
the interaction.

### 8.4. Determinism

Policy judgment must be repeatable from its canonical inputs.

```text
same canonical Policy
same canonical situation
same canonical response-contract material
same canonical contract world
    -> same canonical Policy Judgment
```

A learned, adaptive, or externally updated system may help design Policy material. It may not remain a hidden source of
runtime contract judgment.

Probabilistic Policy is outside V1.

---

## 9. Authoring and Processing Boundary

This draft does not fix the Java or Kotlin Budget or Policy authoring API.

Both authoring surfaces must follow ADR-0047.

```text
Host declaration
    carries finite source material

Resolution
    identifies exact Budget, Policy, subject, quantity,
    situation, response-contract, and referenced coordinates

Validation
    rejects unsupported shape, behavior, ambiguity,
    hidden allocation, and unsupported guarantees

Canonicalization
    removes host-language and source-order authority

Lowering
    produces machine-usable Budget and Policy material

Backend
    realizes measurement, accounting, judgment,
    enforcement, and response without owning meaning
```

Budget authoring must allow the user to declare at least:

```text
one exact Budget or explicit absence

subject

boundary

quantity and unit

allowance

allocation source and composition law where applicable

renewal law

attribution law

required enforcement level
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

The following forms are rejected as Policy authority:

```text
fun interface Policy

class PolicyImpl : Policy

lambda predicate

callback chain

annotation-driven rule discovery

service-loaded strategy

reflection-based policy scan

arbitrary expression evaluator supplied by the user
```

They may be implementation techniques outside the contract surface. They may not define Policy meaning.

The final Budget API must answer:

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

How is required enforcement declared?

How is explicit Budget absence represented?

How does validation reject a realization that cannot provide the required guarantee?
```

The final Policy API must answer:

```text
How is one Policy named?

How is its situation surface declared?

How does each situation coordinate bind to exact established contract material?

How can every Contract expose result material suitable for later judgment without creating an implicit global context?

When is material-kind identity sufficient, and when must the establishing contract result also be identified?

How is the evidence relation between established material and one Policy judgment represented?

How is backend producer identity excluded from that binding?

How does the authoring and canonical model make every situation coordinate read-only to Policy?

How does validation prevent Policy from constructing, replacing, reclassifying, or republishing another Contract's result?

How is one proposed response contract or finite response-contract set declared?

Which restricted relation vocabulary is permitted?

How is applicability bound?

How are several Policies combined?

How does the Interaction Manifest select Policy participation or absence?
```

---

## 10. Contract and Implementation Boundary

### 10.1. Decisions Made Here

This draft decides:

```text
Policy, Budget, Capacity, and Governance belong in contract theory
because a realistic machine must work within finite conditions.

The four remain independent contracts.

Policy selects the prepared response contract that applies to an established situation.

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

Policy consumes one declared finite projection of the established contract world and explicit response-contract material.

Material established by every explicit Contract has equal eligibility to participate in a Policy situation. No contract kind
is privileged merely because it represents business work, pipeline processing, state, judgment, limit, failure, or
publication.

Established contract material becomes Policy evidence through a declared relation to one Policy judgment. Evidence is a
role in that relation, not a separate material hierarchy or authority.

Policy names contract material and its establishing contract law, never the backend producer that realizes it.

Policy receives situation material as immutable input and cannot construct, replace, reclassify, reinterpret, invalidate,
or republish another Contract's result under that Contract's authority.

Observed or profiled information may participate only after existing contract machinery establishes it as explicit material.

Policy judgment and implementation remain separate.

Policy does not activate itself.

Policy cannot weaken another contract's final refusal.

Ordinary software Policy and Budget systems are examples and warnings,
not the source of the general definitions.
```

### 10.2. Decisions Not Made Here

This draft does not decide:

```text
the final Budget Java or Kotlin authoring API

the final V1 physical quantity catalog beyond elapsed time
and the current cumulative allocated-byte candidate

whether processor time, I/O bytes, monetary quantities,
or other physical quantities belong in V1

the exact clock, allocation, instrumentation,
or containment backend

the exact enforcement-level result family

whether V1 requires reservation

the complete child-allocation, unused-return,
borrowing, and reclamation vocabulary

Capacity material, judgment, acquisition, release,
reservation, and admissibility law

Governance material, authoring, and activation law

Policy Java or Kotlin API

Policy relation vocabulary

V1 Policy result family beyond the conservative candidate

how several Policies are combined

Budget and Policy canonical identity bytes

Failure mapping

Diagnostic evidence and retention

Version qualification

exact source-binding syntax between one Policy situation coordinate and its establishing contract result
```

### 10.3. No Hardware or Backend Authority

Physical capability, scheduler behavior, resource discovery, optimizer implementation, storage layout, cache state,
thread count, allocator, clock implementation, and deployment configuration do not define Policy or Budget meaning.

Actual elapsed time and actual memory may be Budget quantities. The hardware, clock, allocator, process boundary,
operating system, instrumentation, or generated machinery used to observe and control them remain realization.

They may establish explicit charge material under declared Budget authority, produce situation material under another
declared Contract, or realize a Policy judgment. Replacing them must not change what the contract means for the same
canonical material.

If a replacement cannot provide the declared Budget guarantee, the replacement is inadmissible. The Budget is not
weakened to preserve backend compatibility.

---

## 11. Determinism and Verification

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

Kontrakt must also be able to verify at least the following before Policy material becomes authoritative.

```text
Policy identity is unique in its declared scope.

Applicability is finite and unambiguous.

Situation coordinates resolve exactly.

The situation is a closed finite projection and does not scan an implicit population of Facts, judgments, or runtime
objects.

Every situation coordinate resolves to exact established contract material.

Every situation coordinate is read-only to Policy.

Policy output can contain only Policy judgment material and cannot construct, replace, reclassify, reinterpret, invalidate,
or republish another Contract's result.

No backend producer, profiler, collector, storage type, counter, or runtime object participates in that binding.

When one material kind can be established by more than one materially different contract result, the source binding is not
ambiguous.

Response-contract handles resolve exactly.

The relation uses only the admitted vocabulary.

Situation material does not contain a hidden ranking, score, optimizer recommendation, or selected response contract.

Every response contract is a prepared machine response, not a named score point or optimizer result.

Every threshold compares one declared situation coordinate with one declared bound.

No executable host behavior participates.

No undeclared default or fallback exists.

The combination law is exact when more than one Policy participates.

Policy does not claim authority owned by another contract.

The lowered evaluator is equivalent to canonical Policy material.
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

When the Policy vocabulary permits complete enumeration at practical size, the compiler may generate full decision-table
tests.

Optimization is allowed only after meaning is preserved.

```text
Budget
    precomputed allocation plan
    static reservation
    specialized accounting
    fused measurement boundary
    generated limit checks

Policy
    canonical decision table
    specialized branches
    bit masks
    precomputed indexes
    compiled predicates
```

These are backend forms. If their result differs from canonical Budget or Policy material, the backend is wrong.

---

## 12. Deferred Decisions

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

What exact enforcement levels are admitted?

How does a backend prove that it can provide the required enforcement?

Which Budget judgments and positions may participate in later Policy situations?

How are Budget identity bytes represented without making measurement machinery authoritative?
```

The following Policy questions remain open.

```text
Does Kontrakt V1 accept only one proposed response contract -> Permit or Refuse?

Does V1 support a finite response-contract set and direct selection?

How is one finite Policy situation projected from the established contract world without creating an implicit
population or mutable context?

What exact canonical material binds one situation coordinate to its establishing contract result?

Which Budget and Capacity results are suitable for later Policy situations?

How is the evidence relation between established contract material and one Policy judgment represented?

How is the read-only boundary enforced in the Policy authoring model, canonical material, and lowering?

How is explicit Policy absence represented?

What happens when no Policy participates?

Does V1 allow more than one Policy per interaction?

If so, what exact combination law is supported?

What closed relation vocabulary is sufficient without becoming an algorithm language?

How are Policy rule identities represented without making diagnostics authoritative?

Where is Policy declared: interface scope, contract scope, or another flat manifest?

How does Governance establish Policy before Version is fully decided?
```

The next design sequence is Capacity and Governance. Their complete material, judgment, and authority laws must be
decided before Policy can close its references to them and before ADR-0051 can become Accepted.

Budget may return during that sequence where Capacity acquisition, release, reservation, or Governance activation
requires one exact shared law. The basic Budget meaning, explicit allocation requirement, accounting position, and
implementation boundary are decided here.

---

## 13. Consequences

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

Policy receives a general meaning that covers finite situation-based response decisions inside machine operation,
routing, treatment, replenishment, traffic control, energy management, building operation, compiler work, spacecraft
operation, irrigation, and other decision systems.

The definition is not limited to safety or refusal.

The machine can declare how it should respond before the situation occurs, while implementation remains free to realize
the result through suitable machinery.

The established contract world becomes the complete declared source from which Policy situations are projected. Business
results, pipeline judgments, machine state, limits, failures, publication, and other contract material remain equally
available without surrendering their separate authority.

The reason for Policy, Budget, Capacity, and Governance becomes explicit. A realistic machine accepts its limits and
keeps its response inside the world it can actually sustain.

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

The Policy authoring surface cannot be copied from ordinary Java or Kotlin strategy patterns.

A small and clear response-contract set must be designed. This is more work than accepting callbacks or arbitrary
predicates.

The general Policy definition is broader than the likely V1 implementation, so the ADR must keep contract meaning
separate from initial realization. Continuous optimization, open-ended planning, rebuilding an existing schedule, and
ranking produced by a learned model remain outside the current V1 boundary.

Goals, learned models, optimizers, and heuristics cannot silently own Policy authority. Systems that depend on them will
need an explicit boundary before their results become contract material.

### Neutral

This ADR does not prohibit clocks, allocators, arenas, schedulers, process isolation, operating-system quotas, compiler
instrumentation, generated checkpoints, runtime counters, strategies, optimizers, rule engines, controllers, profilers,
collectors, or decision tables.

It prevents those mechanisms from becoming the source of Budget or Policy meaning.

Kontrakt may optimize allocation and enforcement aggressively after canonical Budget meaning is preserved.

Reservation remains open. Capacity and Governance remain to be completed before this ADR becomes Accepted.