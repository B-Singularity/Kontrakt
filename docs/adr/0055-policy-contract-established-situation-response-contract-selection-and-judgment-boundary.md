# ADR-0055: Policy Contract, Established Situation, Response-Contract Selection, and Judgment Boundary

## Status

Proposed

## Date

2026-08-03

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0053: Governance and Contract World Activation
- ADR-0052: Capacity Contract and Admission Boundary
- ADR-0051: Budget Contract, Explicit Allowance, Accounting, Allocation, and Backend Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary

---

## 1. Context

One machine may meet different established situations while remaining the same machine and while all fixed laws remain
unchanged.

A compiler may preserve one call and specialize another. A router may keep one path and prefer another. A physical
machine may continue ordinary operation, reduce output, delay optional work, or enter a prepared conservative mode.
These responses are not created by the situation itself. They must already exist as declared response contracts.

If the relation between situation and response remains hidden inside branches, callbacks, strategy objects,
configuration, or runtime lookup, the machine has no explicit Policy authority that Kontrakt can inspect or verify.

```text
Policy
    selects the prepared response contract that applies to an established situation
```

Policy does not own Budget accounting, Capacity admission, Governance activation, State movement, or execution of the
selected response. It reads only declared established material and produces its own selection judgment.

This ADR establishes Policy meaning, situation material, response-contract selection, applicability, judgment, and the
separation between canonical Policy material and its realization. Budget is decided in ADR-0051. Capacity and Governance
remain separate contracts in ADR-0052 and ADR-0053.

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

### 5.1. Policy Contract Meaning

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

### 5.2. Policy Judgment Forms

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

### 5.3. V1 Candidate Boundary

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

### 5.4. Relation to Other Contract Authorities

Policy may use explicitly established contract material only when the Policy situation declares that relation.

When situation material originates from Budget, Capacity, Governance, State, Invariant, Admission, Publication, Failure,
or another explicit Contract, Policy receives only the result material that contract established. It does not acquire
the originating contract's judgment or mutation authority.

Policy therefore may not account consumption, admit growth, activate a contract world, move State, rejudge an Invariant,
republish a Fact, or execute the selected response.

The exact Capacity and Governance materials available to Policy remain dependent on ADR-0052 and ADR-0053. ADR-0051
already owns Budget meaning and Budget judgment; this ADR owns only the later use of an established Budget result as
declared Policy situation material.

---

## 6. Policy Contract

### 6.1. Established Situation

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
Budget Contract in ADR-0051 and the Capacity Contract in ADR-0052.

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

### 6.2. Declared Response Contracts

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

### 6.3. Selection Law

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

### 6.4. Applicability

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

### 6.5. Governance and Active Policy

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

### 6.6. Policy and Purpose

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

### 6.7. Policy and Fixed Laws

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

### 6.8. Policy and Implementation

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

### 6.9. More Than One Policy

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

### 6.10. No Hidden Override

A Policy judgment may not be overridden by debug mode, administrator identity, exception handling, environment variable,
or runtime flag unless the contract explicitly declares such a relation.

This ADR does not admit that relation in V1.

An implementation escape hatch is not Policy material. If it changes the contract result, implementation has acquired
contract authority.

### 6.11. Policy Identity

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

## 7. Policy Judgment

### 7.1. Inputs

One Policy judgment requires explicit material.

```text
Policy handle
established situation with exact contract-material bindings
proposed response contract or declared response-contract set
applicable contract world
```

If any required material is absent, unresolved, corrupt, or unavailable, the machine must not fabricate a judgment.

The later Failure Contract will decide how that condition is classified.

### 7.2. Result

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

### 7.3. Meaning of Permission and Refusal

```text
PolicyPermitted
    means only that the proposed response contract may apply under the Policy

PolicyRefused
    means the proposed response contract must not apply under the Policy
```

Permission does not prove that the whole interaction must continue. The machine must preserve which authority stopped
the interaction.

### 7.4. Determinism

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

## 8. Authoring and Processing Boundary

This draft does not fix the Java or Kotlin Policy authoring API.

ADR-0047 remains the later authoring constraint. This ADR fixes only what such an authoring surface must eventually
carry:
one exact Policy identity, one finite situation projection, exact bindings to established contract results, declared
response-contract material, applicability, and a closed selection law.

Resolution must remove ambiguous references. Validation must reject behavior, hidden fallback, and undeclared situation
inputs. Canonicalization and lowering must preserve the Policy relation without retaining host-language, source-order,
or backend authority. A backend may evaluate the lowered relation and realize an allowed response, but it does not
define the Policy.

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

A later Policy authoring design must answer:

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

## 9. Contract and Implementation Boundary

### 9.1. Decisions Made Here

This draft decides:

```text
Policy belongs in contract theory because a realistic machine must respond within finite established conditions.

Policy remains independent from Budget, Capacity, and Governance.

Policy selects the prepared response contract that applies to an established situation.

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

Ordinary software Policy systems are examples and warnings, not the source of the general definition.
```

### 9.2. Decisions Not Made Here

This draft does not decide:

```text
Policy Java or Kotlin authoring surface

Policy relation vocabulary

V1 Policy result family beyond the conservative candidate

how several Policies are combined

Policy canonical identity bytes

Failure mapping

Diagnostic evidence and retention

Version qualification

exact source-binding syntax between one Policy situation coordinate and its establishing contract result

the Capacity contract material and admission law reserved to ADR-0052

Governance material, authoring, and activation law
```

### 9.3. No Hardware or Backend Authority

Physical capability, scheduler behavior, resource discovery, optimizer implementation, storage layout, cache state,
thread count, allocator, clock implementation, and deployment configuration do not define Policy meaning.

They may produce situation material under another declared Contract or realize a Policy judgment. A replacement
realization must preserve the Policy judgment for the same canonical Policy material.

A generated branch, decision table, mask, specialized evaluator, adapter, strategy, optimizer, controller, profiler, or
collector may realize declared Policy material. None may become the source of Policy meaning.

---

## 10. Determinism and Verification

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

Closed Policy material should permit compiler-generated fixtures and property-based judgment checks.

When the Policy vocabulary permits complete enumeration at practical size, the compiler may generate full decision-table
tests.

Policy optimization is admissible only when the canonical judgment remains unchanged.

```text
Policy
    canonical decision table
    specialized branches
    bit masks
    precomputed indexes
    compiled predicates
```

These are backend forms. If their result differs from canonical Policy material, the backend is wrong.

---

## 11. Deferred Decisions

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

Capacity and Governance must be decided in ADR-0052 and ADR-0053 before Policy can close its references to their
material and before this ADR can become Accepted.

---

## 12. Consequences

### Positive

Policy receives a general meaning that covers finite situation-based response decisions inside machine operation,
routing, treatment, replenishment, traffic control, energy management, building operation, compiler work, spacecraft
operation, irrigation, and other decision systems.

The definition is not limited to safety or refusal.

The machine can declare how it should respond before the situation occurs, while implementation remains free to realize
the result through suitable machinery.

The established contract world becomes the complete declared source from which Policy situations are projected. Business
results, pipeline judgments, machine state, limits, failures, publication, and other contract material remain equally
available without surrendering their separate authority.

The reason for Policy becomes explicit. A realistic machine meets an established situation and selects one prepared
response inside the contract world it can actually sustain.

### Negative

The Policy authoring surface cannot be copied from ordinary Java or Kotlin strategy patterns.

A small and clear response-contract set must be designed. This is more work than accepting callbacks or arbitrary
predicates.

The general Policy definition is broader than the likely V1 implementation, so the ADR must keep contract meaning
separate from initial realization. Continuous optimization, open-ended planning, rebuilding an existing schedule, and
ranking produced by a learned model remain outside the current V1 boundary.

Goals, learned models, optimizers, and heuristics cannot silently own Policy authority. Systems that depend on them will
need an explicit boundary before their results become contract material.

### Neutral

This ADR does not prohibit strategies, optimizers, rule engines, controllers, profilers, collectors, decision tables,
generated branches, masks, indexes, or compiled predicates.

It prevents those mechanisms from becoming the source of Policy meaning.

Capacity and Governance remain to be completed before this ADR can close its references to their authoritative material.