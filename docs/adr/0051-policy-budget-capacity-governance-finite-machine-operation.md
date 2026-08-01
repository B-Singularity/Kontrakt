# ADR-0051: Policy, Budget, Capacity, Governance, and Finite-Machine Operation

## Status

Proposed

## Date

2026-08-01

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
    declares how the machine should respond to an established situation

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

This ADR records the shared reason for the group and begins the Policy Contract. Budget, Capacity, and Governance remain
independent contracts. Their complete material and judgment laws will be added after the Policy boundary is settled.

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

> The machine meets an established situation, and a rule prepared in advance declares the response that belongs to that
> situation.

Without this boundary, Policy becomes another name for every Boolean condition in the system. Budget and Capacity become
Policy limits. Governance becomes a registry that both chooses and runs rules. Objectives, safety laws, and
implementation steps become mixed into one evaluator.

Kontrakt requires a smaller meaning.

---

## 3. Decision Drivers

Policy must cover more than prohibition.

A machine often has several responses that are all possible in the broad physical or technical sense. The correct
response depends on the situation and on what the machine is trying to preserve or improve.

A Policy must therefore express a stable relation between:

```text
an established situation
and
one declared response judgment
```

The response may say that a proposed action is permitted, that one operating mode should be used, that work should be
delayed, that output should be reduced, or that one path should be preferred.

The response is still only a judgment. Policy does not perform the action.

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
same declared response material
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

A spacecraft must balance mission value, power, thermal condition, communication windows, storage, fault risk, and the
chance to complete later work.

It may meet situations such as:

```text
communication with the ground is lost

available power drops

a component becomes too warm

a short observation window opens

one planned activity takes longer than expected
```

The Policy may delay a low-priority task, protect power for communication, enter a safe operating mode, use the
observation window, or cancel work that threatens later mission goals.

The spacecraft does not invent these responses from nothing. The mission prepares the relation between known situations
and acceptable responses before launch, then the onboard system applies it when the situation is established.

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

A compiler must preserve program meaning while balancing execution speed, generated code size, compile time, target
machine behavior, and information from previous runs.

It may meet situations such as:

```text
a small function is called very often

a transformation would speed one loop but enlarge the program

compile time is becoming expensive

the target machine has a useful instruction for one pattern

profile data shows that one branch is rarely taken
```

The Policy may inline a call, keep the call, apply a transformation, skip it, or use a cheaper analysis path.

The compiler backend performs the transformation. Policy declares how the compiler should respond to the established
compile situation after correctness has already been protected.

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

The machine identifies the declared Policy that applies.

The Policy relates that situation to one response judgment.

The machine realizes the response through separate implementation.
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
    correctness, speed, code size, compile time

routing
    reachability, delay, cost, stability, trust
```

Those criteria help design the Policy. They are not the Policy by themselves.

A Policy is the declared relation that says how the machine should respond when the relevant situation is established.

---

## 5. Decision

### 5.1. Finite-Machine Operating Contracts

Policy, Budget, Capacity, and Governance are retained as independent contracts.

They belong in contract theory because a realistic machine must understand the world in which it operates, accept its
limits, and respond without pretending that unlimited work is possible.

```text
Policy
    relates an established situation to a declared response judgment

Budget
    limits finite consumption

Capacity
    limits what the machine may admit

Governance
    establishes the valid contract world
```

The group is not limited to refusal.

A machine may reduce output, delay optional work, use another route, preserve a call, change an operating mode, or
refuse a proposal. Each result must remain explicit and attributable to the contract that owns it.

### 5.2. Policy Contract Meaning

A Policy Contract declares how the machine should respond when one declared situation is established.

```text
Policy Contract:
    the contract that relates one established situation
    to one declared Policy Judgment
```

A shorter statement is:

> Policy is a declared response law for an established situation.

Policy does not discover the situation. It consumes material that has already been resolved and established for the
judgment.

Policy does not perform the response. It establishes a judgment that implementation may realize after all applicable
contract authorities allow continuation.

Policy is not merely a goal or a list of criteria.

```text
Goal
    explains what the designer wishes to protect or improve

Criteria
    identify what matters when the situation is judged

Policy
    declares the response that belongs to the situation
```

### 5.3. Policy Judgment Forms

Different policies may produce different forms of response judgment.

```text
Permission
    a proposed response may continue

Refusal
    a proposed response must not continue

Preference
    one declared response should be favored over another

Direction
    one declared response should guide the machine

Prescription
    one declared response is required by the Policy
```

These are judgments, not physical acts.

A `ReduceOutput` judgment does not itself change power. A `PreferRouteA` judgment does not install a route. A
`PreserveCall` judgment does not rewrite compiler output.

This ADR does not yet accept every form into Kontrakt V1. It records the general family so that Policy is not reduced to
access control or safety refusal.

### 5.4. V1 Candidate Boundary

The conservative V1 candidate is:

```text
one exact proposed response
+ one established situation
+ one exact Policy
+ one applicable contract world
    -> Permit
    or Refuse
```

Under this form, another contract surface presents one proposed response. Policy judges whether that response matches
the established situation.

Policy does not search an open set, run an optimizer, schedule work, or execute the result.

This form fits the current Interaction Manifest and keeps lowering finite. It remains a candidate, not the final V1
decision.

### 5.5. Budget, Capacity, and Governance Scope in This Draft

This draft does not decide the complete Budget, Capacity, or Governance contracts.

It retains only their place beside Policy.

```text
Budget
    states whether declared allowance remains

Capacity
    states whether the proposed load stays inside the declared wall

Governance
    establishes which Policy and related contract material are valid
```

Policy may use their already established results only if the final contract model declares that relation. Policy may not
perform accounting, measure the machine, discover resources, or activate its own world.

---

## 6. Policy Contract

### 6.1. Established Situation

A Policy judges one established situation.

The situation is a finite set of resolved contract coordinates and established Fact material selected for that judgment.
It is not a mutable context object.

```text
Established Situation:
    the closed material presented to one Policy judgment
```

Examples include:

```text
compiler call-site shape and profile class

current inventory position and review point

patient history selected for one treatment decision

route conditions selected for one routing decision

operating mode and machine-condition facts
```

Policy must not directly read a clock, environment variable, file, network service, sensor, repository, registry, random
generator, mutable singleton, or backend object.

Such information must first cross its proper boundary and become explicit material. If the required situation cannot be
established, Policy judgment cannot be formed.

### 6.2. Declared Response Material

A Policy requires a declared response surface.

The smallest form is one exact proposed response.

```text
Proposed Response:
    one named response presented for Policy judgment
```

A broader form may declare a finite response vocabulary.

```text
Declared Responses:
    the closed responses that one Policy may judge
```

Responses must be named and finite. Policy may not discover them by scanning implementations, loading plugins, calling a
callback, querying a service, or inspecting arbitrary host types.

The response surface does not mean that Policy performs the response. It only gives the judgment a closed and
inspectable meaning.

### 6.3. Response Law

A Policy is prepared before the particular interaction.

It declares how relevant situations relate to responses. It does not invent a new rule while judging one instance.

```text
same Policy
same established situation
same declared response material
same contract world
    -> same Policy Judgment
```

The law must be finite, named, inspectable, and explicitly bound.

The source may use a restricted vocabulary such as:

```text
exact coordinate equality
finite membership
required or forbidden marker
closed Boolean relation
finite threshold taken from declared material
explicit preference relation
explicit default or explicit absence
```

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
    judges one situation under that world
```

A configuration file or deployment value may help realize Governance. It does not become contract authority merely by
containing a Policy name.

### 6.6. Policy and Purpose

A purpose explains why a Policy was designed.

```text
reduce code size
protect cooling margin
lower cost
improve delivery time
preserve water
increase comfort
```

The purpose does not automatically determine one Policy. Different policies can pursue the same purpose with different
trade-offs.

Kontrakt does not infer Policy from a score function, optimizer, or learned model. The active response law must be
explicit.

### 6.7. Policy and Fixed Laws

Policy cannot weaken a result that another contract has already made final.

If a response is impossible, unlawful, outside the available allowance, outside the admissible wall, or illegal from the
current machine condition, Policy cannot make it valid.

Policy serves the part of machine operation where the established situation still admits a meaningful response decision.

This gives a simple test:

> When a stronger law has already fixed the only legal result, Policy must not recreate that result as an operating
> choice.

### 6.8. Policy and Implementation

Policy judgment and implementation are separate.

```text
Policy
    establishes the Policy Judgment

Implementation
    performs, skips, routes, stores, publishes, or actuates accordingly
```

The implementation may use generated branches, decision tables, masks, specialized code, static gates, or backend
adapters. None owns Policy meaning.

Replacing those mechanisms must not change the judgment for the same canonical material.

A scheduler, optimizer, router, controller, strategy object, callback, plugin, service, or command handler may realize a
Policy. It may not become the source of Policy meaning.

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
response-law material
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
established situation
proposed response or declared response vocabulary
applicable contract world
```

If any required material is absent, unresolved, corrupt, or unavailable, the machine must not fabricate a judgment.

The later Failure Contract will decide how that condition is classified.

### 7.2. Result

The result must be typed as Policy judgment, not returned as an unnamed Boolean.

The final result family remains open. A conservative candidate is:

```text
PolicyPermitted

PolicyRefused
```

A broader future family may include:

```text
PolicyPreference
PolicyDirection
PolicyPrescription
```

The result must retain enough identity to show which Policy and contract world produced it. Diagnostic text is not the
identity.

### 7.3. Meaning of Permission and Refusal

```text
PolicyPermitted
    means only that Policy did not stop the proposed response

PolicyRefused
    means Policy stopped the proposed response under its declared law
```

Permission does not prove that the whole interaction must continue. The machine must preserve which authority stopped
the interaction.

### 7.4. Determinism

Policy judgment must be repeatable from its canonical inputs.

```text
same canonical Policy
same canonical situation
same canonical response material
same canonical contract world
    -> same canonical Policy Judgment
```

A learned, adaptive, or externally updated system may help design Policy material. It may not remain a hidden source of
runtime contract judgment.

Probabilistic Policy is outside V1.

---

## 8. Authoring and Processing Boundary

This draft does not fix the Java or Kotlin Policy authoring API.

The authoring surface must follow ADR-0047.

```text
Host declaration
    carries finite source material

Resolution
    identifies exact Policy symbols and referenced coordinates

Validation
    rejects unsupported shape, behavior, and ambiguity

Canonicalization
    removes host-language and source-order authority

Lowering
    produces machine-usable Policy material

Backend
    realizes the judgment without owning its meaning
```

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

The final API must answer:

```text
How is one Policy named?

How is its situation surface declared?

How is one proposed response or finite response vocabulary declared?

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
Policy, Budget, Capacity, and Governance belong in contract theory
because a realistic machine must work within finite conditions.

The four remain independent contracts.

Policy is a declared response law for an established situation.

Safety and authorization are special Policy uses, not the general definition.

Policy consumes explicit situation and response material.

Policy judgment and implementation remain separate.

Policy does not activate itself.

Policy cannot weaken another contract's final refusal.

Ordinary software Policy systems are examples and warnings,
not the source of the general definition.
```

### 9.2. Decisions Not Made Here

This draft does not decide:

```text
Budget material and accounting law

Capacity material and admissibility law

Governance authoring and activation law

Policy Java or Kotlin API

Policy relation vocabulary

V1 result family beyond the conservative candidate

whether V1 supports preference, direction, or prescription

how several Policies are combined

Policy canonical identity bytes

Failure mapping

Diagnostic evidence and retention

Version qualification

runtime enforcement mechanism
```

### 9.3. No Hardware or Backend Authority

Physical capability, scheduler behavior, resource discovery, optimizer implementation, storage layout, cache state,
thread count, and deployment configuration do not define Policy.

They may produce explicit material or realize a Policy judgment. If replacing them changes Policy meaning for the same
canonical inputs, the boundary has failed.

---

## 10. Determinism and Verification

Kontrakt must be able to verify at least the following before Policy material becomes authoritative.

```text
Policy identity is unique in its declared scope.

Applicability is finite and unambiguous.

Situation coordinates resolve exactly.

Response handles resolve exactly.

The relation uses only the admitted vocabulary.

No executable host behavior participates.

No undeclared default or fallback exists.

The combination law is exact when more than one Policy participates.

Policy does not claim authority owned by another contract.

The lowered evaluator is equivalent to canonical Policy material.
```

Verification should support generated fixtures and property-based tests over the closed material. When the Policy
vocabulary permits complete enumeration at practical size, the compiler may generate full decision-table tests.

Optimization is allowed only after meaning is preserved.

```text
canonical decision table
specialized branches
bit masks
precomputed indexes
compiled predicates
```

These are backend forms. If their result differs from canonical Policy material, the backend is wrong.

---

## 11. Deferred Decisions

The following questions remain open for the next revision of this ADR.

```text
Does Kontrakt V1 accept only one proposed response -> Permit or Refuse?

Does V1 support a finite response vocabulary?

Are preference, direction, and prescription distinct Policy kinds?

How is situation material selected from Facts and interaction coordinates?

Can a Policy consume prior Budget or Capacity judgments?

How is explicit Policy absence represented?

What happens when no Policy participates?

Does V1 allow more than one Policy per interaction?

If so, what exact combination law is supported?

What closed relation vocabulary is sufficient without becoming an algorithm language?

How are Policy rule identities represented without making diagnostics authoritative?

Where is Policy declared: interface scope, contract scope, or another flat manifest?

How does Governance establish Policy before Version is fully decided?
```

Budget, Capacity, and Governance require their own complete sections before ADR-0051 can become Accepted.

---

## 12. Consequences

### Positive

Policy receives a general meaning that covers real machine operation, optimization, routing, treatment, replenishment,
traffic control, energy management, building operation, spacecraft operation, irrigation, and other decision systems.

The definition is not limited to safety or refusal.

The machine can declare how it should respond before the situation occurs, while implementation remains free to realize
the result through suitable machinery.

The reason for Policy, Budget, Capacity, and Governance becomes explicit. A realistic machine accepts its limits and
keeps its response inside the world it can actually sustain.

### Negative

The Policy authoring surface cannot be copied from ordinary Java or Kotlin strategy patterns.

A small and clear response vocabulary must be designed. This is more work than accepting callbacks or arbitrary
predicates.

The general definition is broader than the likely V1 implementation, so the ADR must keep contract meaning separate from
initial realization.

Goals, learned models, optimizers, and heuristics cannot silently own Policy authority. Systems that depend on them will
need an explicit boundary before their results become contract material.

### Neutral

This ADR does not prohibit runtime strategies, schedulers, optimizers, rule engines, controllers, or decision tables.

It prevents those mechanisms from becoming the source of Policy meaning.