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

It cannot accept every load, spend without limit, keep every result, apply every rule at once, or behave as though every
choice were equally suitable in every situation. It must know the world in which it is operating, recognize what that
world permits, and remain inside declared limits while pursuing its purpose.

This is not merely a concern for hardware. A software machine also runs under finite memory, finite time, finite work,
finite evidence retention, finite publication surfaces, and finite human and institutional authority. Even when the
backend can physically continue, the declared machine may still have to choose another action, defer work, refuse a
proposal, or stop.

A good machine does not deny these limits. It exposes them before pressure discovers them accidentally. It also does
more than reject excess. It responds within the world it can actually sustain and chooses among the actions that remain
meaningful there.

Policy, Budget, Capacity, and Governance belong in contract theory for that reason.

```text
Policy
    declares how an established situation is related to declared choices

Budget
    declares finite consumable allowance

Capacity
    declares an admissible limit

Governance
    declares which contract world is valid
```

The four contracts answer different questions.

```text
Policy
    Given this situation, what choice judgment applies?

Budget
    How much declared consumption remains available?

Capacity
    Can the proposed load remain inside the declared wall?

Governance
    Which Policy, Budget, Capacity, Version, and contract bindings are authoritative here?
```

They are grouped because a realistic machine must operate under all four, not because they form one master contract.
None may absorb the others.

This ADR establishes the shared reason for the group and begins the Policy Contract. Budget, Capacity, and Governance
are retained as independent contract presentations, but their detailed material, authoring surface, and judgment laws
remain open in this draft.

---

## 2. Problem

Software uses the word `policy` for many unrelated things.

```text
authorization rule
configuration file
admission hook
security check
cost limit
dependency rule
routing preference
compiler heuristic
retention setting
deployment restriction
organizational procedure
```

The name often tells us only that a condition was moved out of ordinary code. It does not tell us what question the rule
owns, which material it may inspect, whether it rejects or selects, how several rules combine, or who makes the rule
active.

Moving an `if` statement into YAML, a callback, a rule engine, or a dedicated DSL does not by itself create a contract.
The behavior may still depend on registration order, runtime lookup, mutable configuration, hidden defaults, callback
control flow, or the implementation that happens to evaluate it.

Recent Policy-as-Code studies report broad and heterogeneous use across governance, configuration control, security,
workflow automation, cost concerns, documentation, and other project practices. They also report differences between
tools and limited interoperability. These studies describe current practice; they do not establish one general semantic
model of software Policy.[1][2]

It would therefore be too strong to claim that software research has no rigorous Policy work. Cedar is a clear
counterexample. Cedar gives authorization Policy a formal semantics, schema validation, analyzable composition, and a
verified model linked to production implementation.[3][4]

Cedar is still deliberately narrow.

```text
principal
action
resource
context
    -> authorization decision
```

Its precision comes from closing one decision domain. It does not define the general boundary between Policy, Admission,
Invariant, State, Budget, Capacity, Governance, Failure, and implementation.

Kontrakt cannot inherit the ordinary software meaning of Policy because that meaning is too broad to preserve contract
authority. It also cannot copy Cedar as the complete model because Kontrakt must describe decisions beyond
authorization.

The general meaning has to be derived from a wider body of practice.

Across decision theory, control, compiler optimization, routing, inventory control, adaptive treatment, maintenance,
water-resource operation, manufacturing dispatch, traffic control, energy management, building operation, spacecraft
autonomy, irrigation, and monetary policy, the same core structure appears repeatedly:

```text
an established situation
an available or proposed choice
an explicit decision law
an outcome that guides action
```

The domains differ, but the role is recognizable. A Policy is not merely a limit. It is a law prepared before the
particular decision that relates a known situation to a choice judgment.

Without that distinction, several errors follow.

```text
Policy may become a generic Boolean for every contract question.

Budget and Capacity may be renamed as Policy limits.

Governance may become a registry that both selects and executes rules.

Objectives and heuristics may hide inside callbacks.

Implementation capability may be mistaken for permission.

A missing rule may silently become a default choice.
```

Kontrakt needs a smaller and more general definition.

---

## 3. Decision Drivers

Policy must remain broad enough to describe more than safety and authorization.

A compiler may use Policy to choose whether to inline a legal call site. A router may use Policy to prefer one
acceptable route. An inventory system may use Policy to decide when and how much to replenish. A treatment regime may
map current patient history to a recommended intervention. A monetary policy rule may relate economic conditions to a
prescribed instrument value.[5][6][7][8][9]

These examples are not all refusals. They include filtering, preference, selection, and prescription.

Policy must nevertheless remain narrower than general behavior. Not every decision belongs to Policy.

```text
Admission
    judges whether presented material may enter a declared boundary

Canonicalization
    establishes one representative form

Lowering
    establishes machine-usable contract material

Invariant
    judges whether proposed factual establishment satisfies declared law

State-Machine Axis
    judges whether one selected movement is legal from the current establishment condition

Budget
    judges finite consumable allowance

Capacity
    judges an admissible wall

Governance
    judges which contract world is valid

Implementation
    realizes the authoritative results
```

Policy must not repeat those questions under a more fashionable name.

Policy authority must not live in an implementation class, lambda, callback, strategy object, runtime registry, service
lookup, environment variable, file order, or backend optimizer.

A Policy must be finite enough to inspect, validate, canonicalize, compare, test, and lower. Arbitrary executable logic
would make the implementation the only readable source of meaning.

The general contract meaning and the first Kontrakt realization need not have equal breadth. Contract theory may define
Policy as a general choice law while V1 supports only a smaller judgment form that can be completely verified and
specialized.

Determinism remains mandatory. If the same Policy, established situation, declared choice material, and contract world
can produce different judgments because of time, thread scheduling, map order, remote availability, or model randomness,
the Policy has not been fully presented to the contract machine.

---

## 4. Research Position

### 4.1. Conservative Claim About Software Policy

This ADR does not claim that all software Policy work is vague or that no formal Policy theory exists.

The narrower claim is supported by current evidence:

> Software engineering practice uses `policy` across several different purposes, and no single widely adopted software
> Policy system provides the general contract boundary Kontrakt needs.

Recent Policy-as-Code studies support the first part. They observe substantial diversity in tool use and intent across
open-source projects. Their taxonomies include governance, configuration, security, workflow, cost, and documentation.
They also identify tool-specific ecosystems and interoperability problems.[1][2]

These are empirical descriptions of practice. They do not prove that a general theory is impossible. They do show that
ordinary Policy-as-Code usage is not a sufficient semantic foundation for Kontrakt.

Cedar supports the second part from the opposite direction. It demonstrates that Policy can be precise when the decision
domain, syntax, composition, validator, and evaluator are tightly closed. Its scope is authorization, not general
machine choice.[3][4]

Kontrakt therefore uses software Policy systems as evidence and technique, not as the source of the general definition.

### 4.2. Sources Outside Ordinary Policy-as-Code

The broader concept is drawn from several fields that use `policy` as a decision relation.

#### Decision Theory and Reinforcement Learning

A policy relates a perceived state to an action or to a distribution over actions. The definition is not inherently
about safety, access, or refusal. It describes how a decision-maker acts under a situation.[5]

#### Compiler Optimization

LLVM can replace hand-written optimization heuristics with machine-learned advisors for inlining and register
allocation. The legal transformation and the choice to apply it are separate. The advisor decides among legal
alternatives according to an optimization purpose; it does not define program semantics.[6]

#### Network Routing

BGP leaves route installation and precedence to local policy. Several routes may be structurally valid, while local
policy determines which one is installed or preferred.[7]

#### Adaptive Treatment

A dynamic treatment regime is a sequence of decision rules. Each rule maps current patient information and history to a
recommended treatment. The policy is neither the patient record nor the treatment mechanism.[8]

#### Inventory and Maintenance

Inventory policies map inventory position and review conditions to an ordering decision. Condition-based maintenance
policies similarly relate equipment condition and timing to maintenance action. The machinery, stock, cost model, and
physical limits inform the decision but are not the policy itself.[9][10]

#### Monetary Policy Rules

Monetary policy rules relate observed or estimated economic variables to a prescribed policy rate or change. Central
banks may use several rules as benchmarks rather than follow one mechanically, which also shows that the decision law,
the objective, the observed material, and the authority that adopts the rule are distinct.[11]

#### Water-Resource and Reservoir Operation

Reservoir operation uses operating rules or policies to relate storage, inflow, forecast, season, downstream demand, and
system purpose to release, transfer, or retention decisions. The same physical reservoir may serve water supply, flood
control, hydropower, and ecological objectives. Its hydraulic limits constrain the alternatives; the operating rule
decides how the available alternatives are used under the established situation.[12]

This distinction matters to Kontrakt. Storage volume is not Policy. A release objective is not Policy. The declared rule
that relates reservoir conditions to one release decision occupies the Policy role.

#### Manufacturing Dispatch and Production Control

A dispatching rule observes the production queue and relevant shop conditions, then selects the next job or job group to
admit. Different rules may optimize tardiness, makespan, throughput, predictability, or workload balance. More than one
rule can be valid for the same machines and jobs, and the preferred rule can change with the performance measure.[13]

The queue is situation material. Machine availability and processing limits constrain the choice. The dispatch rule
relates that material to an operational selection. The equipment controller performs the selection afterward.

#### Transportation and Traffic-Signal Control

Adaptive traffic-signal control relates queue length, density, current phase, and other traffic material to the next
phase or green-time decision. Different control policies may trade mean queue, stopped time, travel time, emissions, or
transit priority. The road layout and controller hardware define what can be executed; they do not by themselves choose
the next signal action.[14]

This domain also shows why the objective must remain separate. Two policies may observe the same traffic state while
pursuing different balances among delay, emissions, and priority.

#### Energy and Microgrid Operation

Microgrid control and energy-management strategies relate demand, local generation, storage state, price, and network
condition to decisions such as charge, discharge, import, export, curtailment, or islanding. Electrical feasibility and
component ratings constrain those choices. The operating strategy decides among the choices that remain feasible.[15]

Generation capacity is therefore not Policy. A cost or resilience objective is not Policy. The rule that connects the
established energy situation to one operating choice is the relevant decision law.

#### Building Operation

Building automation uses sequences of operation to relate occupancy, zone conditions, outdoor conditions, equipment
availability, and operating mode to setpoints, staging, ventilation, and heating or cooling decisions. ASHRAE Guideline
36 standardizes detailed HVAC sequences intended to improve energy efficiency and performance, maintain control
stability, and support fault detection and diagnostics.[16]

The sequence is distinct from the sensors that establish situation material and from the controllers and actuators that
realize the resulting command. A building can keep the same declared sequence while replacing those mechanisms.

#### Spacecraft and Mission Autonomy

Autonomous spacecraft planning uses mission intent, spacecraft state, sensed environment, priorities, and available
resources to select activities or revise a plan without waiting for a ground command. JPL work on autonomous spacecraft
operations also treats intent capture, onboard decision-making, execution, reconstruction, and explanation as separate
problems.[17]

These systems are more complex than the Policy Contract proposed here, but they expose the same boundary. Intent and
objectives guide the decision. Spacecraft state and resource material describe the situation. A planner or executor
realizes the selected activities. None of those elements alone is the decision law.

#### Agricultural and Irrigation Control

Intelligent irrigation systems relate soil moisture, weather, crop condition, and timing to irrigation decisions. The
choice may concern whether to irrigate, when to start, or how much water to apply. Water allowance, pump capability, and
field hydraulics place separate limits on that decision.[18]

This provides a simple non-safety example. The policy does not create water or measure the soil. It relates established
agricultural material to an irrigation choice.

#### Safety and Regulated Engineering

Safety engineering supplies important special cases. It distinguishes operating rules, absolute safety constraints,
protective mechanisms, and regulatory authority. This prevents Policy from becoming the only barrier against every
failure. Safety evidence is therefore useful for Policy boundaries, but it must not define Policy only as prohibition.

### 4.3. Common Structure

Across these domains, the stable common form is not `configuration` and not `deny`.

```text
Situation
    established material relevant to one decision

Alternatives
    declared actions, choices, or one exact proposed choice

Policy
    the decision law prepared for that class of situation

Choice Judgment
    the authoritative relation produced for the alternatives

Realization
    the mechanism that performs or enforces the resulting choice
```

The objective may explain why the Policy was designed, but it is not the Policy.

```text
Objective
    reduce code size

Policy
    under these call-site conditions, prefer or select PreserveCall

Implementation
    retain the call
```

The same separation applies to safety, cost, quality, routing, treatment, replenishment, reservoir release, production
dispatch, traffic control, energy management, building operation, spacecraft activity, and irrigation.

The fields do not share one vocabulary. They use terms such as `policy`, `operating rule`, `dispatching rule`, `control
strategy`, and `sequence of operation`. This ADR does not treat those names as equivalent by themselves. It uses them
only where they occupy the same semantic place: established situation material is related to a declared choice judgment
before a separate mechanism realizes the result.

The wider comparison also exposes several stable boundaries.

```text
measurement
    establishes situation material

feasibility and hard limits
    remove impossible or illegal alternatives

objective
    explains what the designer values

Policy
    relates the remaining choice surface to one judgment

planning and execution
    realize or sequence the chosen action

Governance
    establishes which Policy world is authoritative
```

A field may combine these responsibilities in one controller, handbook, model, or organization. That practical packaging
is not evidence that they are one contract. Kontrakt separates them because the machine must preserve attribution when a
judgment changes or fails.

---

## 5. Decision

### 5.1. Finite-Machine Operating Contracts

Policy, Budget, Capacity, and Governance are retained as independent contracts.

They belong in contract theory because a realistic machine must acknowledge the conditions under which it can pursue its
purpose.

```text
Policy
    chooses or judges among declared alternatives for an established situation

Budget
    limits finite consumption

Capacity
    limits admissible load or retained material

Governance
    establishes the valid contract world
```

The group is not limited to refusal.

A machine may respond to its finite world by selecting a cheaper path, preserving a call, reducing exposure, choosing a
route, using a different treatment, delaying optional work, or refusing a proposal. The response must remain explicit
and attributable to the contract that owns it.

The contracts may participate around the same interaction, but their results do not override each other.

```text
Policy selection
    does not create Budget

Budget availability
    does not prove Capacity

Capacity availability
    does not select Policy

Governance activation
    does not perform Policy judgment
```

### 5.2. Policy Contract Meaning

A Policy Contract declares an explicit decision law that relates an established situation and declared choice material
to one authoritative Choice Judgment.

```text
Policy Contract:
    the contract that declares how an established situation
    is related to one declared choice or declared alternatives
```

A shorter statement is:

> Policy is a declared law for choosing under a situation.

The Policy does not discover the situation. It consumes resolved contract material.

The Policy does not perform the selected action. It establishes a judgment that implementation may realize only after
all other applicable contract authorities permit continuation.

The Policy is not merely its criteria. Criteria are material used by the decision law.

```text
situation coordinates
choice coordinates
criteria
composition law
    -> Policy material

choice judgment
    -> Policy result
```

The Policy is also not the objective that motivated it.

```text
Objective
    states what is valued

Policy
    states what choice judgment follows in declared situations
```

### 5.3. General Choice Judgments

The general concept of Policy may produce different kinds of choice judgment.

```text
Eligibility
    whether an alternative remains available

Preference
    how alternatives relate in order or priority

Selection
    which exact alternative is chosen

Prescription
    which alternative is required by the declared decision law
```

Safety and authorization Policy are usually eligibility or selection over alternatives such as `Continue` and `Stop`, or
`Grant` and `Deny`.

Compiler optimization, routing, replenishment, and treatment Policy may use preference, selection, or prescription.

This ADR does not yet accept all four judgment forms into Kontrakt V1. It records them so that the general definition is
not incorrectly reduced to `Permit` and `Refuse` before the V1 surface is chosen.

### 5.4. V1 Candidate Boundary

The conservative V1 candidate is:

```text
one exact proposed choice
+ one established situation
+ one exact Policy
+ one applicable contract world
    -> Permit
    or Refuse
```

Under this form, Policy does not generate candidates, search alternatives, optimize, schedule, or execute. Another
contract surface presents one exact proposed choice, and Policy judges whether that choice remains available in the
established situation.

This shape fits the existing Interaction Manifest and Operation pipeline, keeps lowering finite, and avoids turning
Policy into a generic optimizer.

It is not yet final. The ADR must still decide whether finite preference or exact selection is required in V1. Until
then,
`Permit` and `Refuse` remain a candidate realization, not the complete theoretical meaning.

### 5.5. Budget, Capacity, and Governance Scope in This Draft

This draft does not decide the detailed Budget, Capacity, or Governance contracts.

It retains only their boundary around Policy.

```text
Budget
    may make a proposed choice unavailable because declared allowance is exhausted

Capacity
    may make a proposed choice unavailable because declared load would exceed an admissible wall

Governance
    selects which Policy and related contract material are valid
```

Policy may consume established Budget or Capacity results if the final contract model explicitly allows that relation.
It may not perform accounting, measure the machine, discover resources, or activate its own world.

---

## 6. Policy Contract

### 6.1. Established Situation

A Policy judges from an established situation.

The situation is not a mutable context object. It is a finite set of resolved contract coordinates and established Fact
material selected for that Policy judgment.

```text
Established Situation:
    the closed material presented to one Policy judgment
```

Examples include:

```text
compiler call-site shape and profile class

candidate dependency version and compatibility facts

current inventory position and review coordinate

patient history material selected for one treatment decision

route attributes selected for one routing decision

operating mode and machine-condition facts
```

The Policy must not reach outside that material to read:

```text
clock
environment variables
thread-local state
filesystem
network service
sensor
repository
registry
random generator
mutable singleton
backend object
```

Such observations must first cross the appropriate boundary and become explicit material. If they cannot be established,
Policy judgment cannot be formed.

### 6.2. Declared Choice Material

A Policy requires a choice surface.

The minimum form is one exact proposed choice.

```text
Proposed Choice:
    one named alternative presented for Policy judgment
```

A broader form may present a finite alternative set.

```text
Declared Alternatives:
    the closed set over which one Policy may establish
    eligibility, preference, selection, or prescription
```

Alternatives must be named and finite. A Policy may not discover them by scanning implementations, loading plugins,
calling a callback, querying a service, or inspecting arbitrary host types.

If only one meaningful action can legally exist, Policy adds no authority. The action belongs to a stronger contract law
or to implementation realization.

Policy exists where the contract admits a genuine decision.

### 6.3. Choice Law

A Policy is prepared before the particular interaction.

It declares how relevant situations relate to choices. It does not invent a new rule while judging one instance.

```text
same Policy
same established situation
same declared choice material
same contract world
    -> same Choice Judgment
```

The law must be finite, named, inspectable, canonicalizable, and explicitly bound.

The source may use a restricted vocabulary such as:

```text
exact coordinate equality
finite membership
required or forbidden marker
closed Boolean relation
finite threshold selected from declared material
explicit preference relation
explicit default or explicit absence
```

The final V1 vocabulary remains open. Arbitrary methods, loops, callbacks, host-language control flow, reflection,
repository access, or user-defined evaluators are not Policy material.

### 6.4. Applicability

A Policy must have a declared applicability domain.

Applicability identifies the class of situations and choice surfaces for which the Policy may judge. It may include an
Operation, presentation, machine, mode, contract version, or other explicit coordinate selected by the final authoring
model.

Applicability may not be inferred from:

```text
package location
class name
registration order
call site
current thread
environment name
which backend loaded the Policy
```

A Policy that is not applicable does not silently produce `Permit` or `Refuse`.

```text
NotApplicable
    != Permit
    != Refuse
```

Whether `NotApplicable` is represented as explicit Policy absence, definition rejection, or another closed result
remains open. Hidden fallback is rejected.

### 6.5. Policy Selection and Governance

A Policy does not activate itself.

Governance owns which Policy material is authoritative for a machine and interaction. A Policy may not select another
Policy, change its own version, inspect a registry to find a replacement, or fall back to the newest declaration.

```text
Governance
    selects the applicable Policy world

Policy
    judges one situation under that selected world
```

A configuration file, deployment value, or runtime registry may help realize Governance. It does not become contract
authority merely by containing a Policy name.

### 6.6. Policy and Objective

An objective explains what a designer wishes to improve or preserve.

```text
performance
code size
cost
safety
fairness
quality
availability
clinical outcome
inventory level
```

A Policy is the declared decision law chosen in light of that objective.

The objective does not automatically determine one Policy. Different policies may pursue the same objective with
different trade-offs. The active Policy must therefore be explicit and governed.

Kontrakt does not infer Policy from an objective function. It also does not accept an optimizer as hidden Policy
authority.

### 6.7. Policy and Stronger Contract Laws

Policy cannot make another contract's refusal disappear.

```text
Admission refused
    Policy cannot permit entry

Invariant refused
    Policy cannot establish the Fact anyway

State-Machine movement refused
    Policy cannot create movement authority

Budget exhausted
    Policy cannot mint allowance

Capacity refused
    Policy cannot expand the wall

Publication refused
    Policy cannot expose the claim
```

A Policy may choose among alternatives that remain meaningful under those contracts. It may not redefine their law.

This establishes a useful test:

> If another contract already determines that only one legal result exists, Policy must not recreate that result as a
> discretionary choice.

### 6.8. Policy and Implementation

Policy judgment and enforcement are separate.

```text
Policy
    establishes the Choice Judgment

Implementation
    performs, skips, routes, publishes, stores, or actuates accordingly
```

The implementation may use generated branches, decision tables, masks, specialized code, static gates, or backend
adapters. None owns Policy meaning.

Replacing those mechanisms must not change the Choice Judgment for the same canonical material.

Policy is not:

```text
scheduler
optimizer
router implementation
cache implementation
controller
strategy object
callback
plugin
service
command handler
```

Those mechanisms may realize a Policy or produce material for it.

### 6.9. Composition

More than one Policy may appear relevant to one decision. Their composition cannot be inferred.

Possible laws include:

```text
one exact Policy only

all selected Policies must permit

explicit refusal dominates permission

one declared preference relation combines the selected Policies
```

Cedar demonstrates the value of a closed composition law for authorization, but its `permit` and `forbid` semantics are
not adopted automatically for every Policy domain.[3]

Kontrakt must either select one exact Policy or declare one exact composition law. File order, registration order,
priority number convention, last-write-wins, newest-version-wins, or callback order may not decide authority.

The V1 composition law remains open.

### 6.10. No Hidden Override

A Policy judgment may not be overridden by debug mode, administrator identity, exception handling, environment variable,
or runtime flag unless the contract explicitly declares an override relation.

This ADR does not admit such a relation in V1.

An implementation escape hatch is not Policy material. If it changes the contract result, the implementation has
acquired contract authority.

### 6.11. Policy Identity

A Policy requires canonical identity.

The identity must distinguish at least:

```text
Policy kind
applicability domain
choice-law material
composition material
applicable contract world
```

Host class name, object identity, constructor call, source-file order, reflection handle, or generated evaluator type
may not define Policy identity.

The exact identity material remains deferred until the authoring and lowering surface is fixed.

---

## 7. Policy Judgment

### 7.1. Inputs

One Policy judgment requires explicit material.

```text
Policy handle
established situation
proposed choice or declared alternatives
applicable contract world
```

If any required material is absent, unresolved, corrupt, or unavailable, the machine must not fabricate a Choice
Judgment.

The later Failure Contract will decide how such conditions are classified.

### 7.2. Result

The result must be typed as Policy judgment, not returned as an unqualified Boolean.

The final result family remains open. A conservative candidate is:

```text
PolicyPermitted

PolicyRefused
```

A broader future family may include:

```text
PolicyEligibility
PolicyPreference
PolicySelection
PolicyPrescription
```

The Policy result must preserve enough identity to attribute which Policy and contract world produced it. Diagnostic
text is not the identity.

### 7.3. Relation to Interaction Continuation

Policy judgment is one authority among several.

```text
PolicyPermitted
    means only that Policy did not stop the proposed choice

PolicyRefused
    means Policy stopped the proposed choice under its own declared law
```

`PolicyPermitted` does not imply that Admission, Budget, Capacity, Invariant, State Machine, Publication, or
implementation must continue.

The pipeline must preserve which authority stopped the interaction.

### 7.4. Determinism

Policy judgment must be repeatable from its canonical inputs.

```text
same canonical Policy
same canonical situation
same canonical choice material
same canonical contract world
    -> same canonical Choice Judgment
```

A probabilistic, adaptive, learned, or externally updated mechanism may help design Policy material. It may not remain
an unpresented source of runtime contract judgment.

If Kontrakt later supports probabilistic Policy, the probability distribution, sampling law, entropy material, and
result semantics must themselves become explicit contract material. This is outside V1.

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

They may be ordinary implementation techniques outside the contract surface. They may not define Policy meaning.

The final API must answer:

```text
How is one Policy named?

How is its situation surface declared?

How is one proposed choice or alternative vocabulary declared?

Which restricted relation vocabulary is permitted?

How is applicability bound?

How is composition declared?

How does the Interaction Manifest select Policy participation or absence?
```

---

## 9. Contract and Implementation Boundary

### 9.1. Contract Decisions Made Here

This draft decides:

```text
Policy, Budget, Capacity, and Governance belong in contract theory
because a realistic machine must operate under finite conditions.

The four remain independent contracts.

Policy is a declared decision law for choosing under an established situation.

Safety and authorization are special Policy domains, not the general definition.

Policy consumes explicit situation and choice material.

Policy judgment and implementation realization are separate.

Policy does not activate itself.

Policy cannot override another contract's refusal.

Policy-as-Code practice is evidence, not the semantic foundation.

Cedar is a narrow formal reference, not the complete general model.
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

whether V1 supports preference or selection

Policy composition law

Policy canonical identity bytes

Failure mapping

Diagnostic evidence and retention

Version qualification

runtime enforcement mechanism
```

### 9.3. No Hardware or Backend Authority

Physical capability, scheduler behavior, resource discovery, optimizer implementation, storage layout, cache state,
thread count, and deployment configuration do not define Policy.

They may produce explicit material or realize a Policy result. If replacing them changes the Policy meaning for the same
canonical inputs, the boundary has failed.

---

## 10. Determinism and Verification

Kontrakt must be able to verify at least the following before Policy material becomes authoritative.

```text
Policy identity is unique in its declared scope.

Applicability is finite and unambiguous.

Situation coordinates resolve exactly.

Choice handles resolve exactly.

The relation uses only the admitted vocabulary.

No executable host behavior participates.

No undeclared default or fallback exists.

Composition is exact when more than one Policy participates.

Policy does not claim authority owned by another contract.

The lowered evaluator is equivalent to canonical Policy material.
```

Verification should support generated fixtures and property-based tests over the closed material. When the Policy
vocabulary permits exhaustive enumeration at practical size, the compiler may generate complete decision-table tests.

Optimization is allowed only after equivalence is preserved.

```text
canonical decision table
specialized branches
bit masks
precomputed indexes
compiled predicates
```

are backend forms. If their result differs from canonical Policy material, the backend is wrong.

---

## 11. Deferred Decisions

The following questions remain open for the next revision of this ADR.

```text
Does Kontrakt V1 accept only one proposed choice -> Permit / Refuse?

Does V1 support a finite alternative set?

Are preference, selection, and prescription distinct Policy kinds?

How is situation material selected from Facts and interaction coordinates?

Can a Policy consume prior Budget or Capacity judgments?

How is explicit Policy absence represented?

What is the default when no Policy participates?

Does V1 allow more than one Policy per interaction?

If so, what exact composition law is supported?

What closed expression vocabulary is sufficient without becoming an algorithm language?

How are Policy rule identities represented without making diagnostics authoritative?

Where is Policy declared: interface scope, contract scope, or another flat manifest?

How does Governance select Policy before Version is fully decided?
```

Budget, Capacity, and Governance require their own complete sections before ADR-0051 can become Accepted.

---

## 12. Consequences

### Positive

Policy receives a general meaning that covers safety, authorization, optimization, routing, treatment, replenishment,
water operation, production dispatch, traffic control, energy management, building operation, spacecraft autonomy,
irrigation, and other decision domains without turning every rule into Policy.

The definition is small enough to preserve the boundaries of Admission, Invariant, State Machine, Budget, Capacity,
Governance, and implementation.

Current Policy-as-Code practice is treated conservatively. Heterogeneous usage is acknowledged without claiming that all
software Policy research is invalid.

Cedar contributes formal semantics, explicit composition, validation, and verification method while remaining correctly
scoped to authorization.

The finite-machine reason for Policy, Budget, Capacity, and Governance becomes explicit. The machine does not merely
know how to reject. It knows how to choose within the world it can actually sustain.

### Negative

The Policy authoring surface cannot be copied from ordinary Java or Kotlin strategy patterns.

A useful restricted decision vocabulary must be designed. This is more work than accepting callbacks or arbitrary
predicates.

The general definition is broader than the likely V1 implementation, so the ADR must clearly separate contract meaning
from initial realization.

Objectives, learned models, optimizers, and heuristics cannot silently own Policy authority. Systems that currently
depend on them will need an explicit lowering or qualification boundary.

### Neutral

This ADR does not prohibit ordinary runtime strategies, schedulers, optimizers, or rule engines. It prevents them from
being the source of contract meaning.

A system may still use Cedar, OPA, a compiler advisor, a routing engine, or another mechanism behind the contract
boundary if it can prove that the mechanism realizes the selected canonical Policy material.

---

## 13. Research References

1. Patrick Loic Foalem, Foutse Khomh, Leuson Da Silva, and Ettore Merlo, “An Empirical Study of Policy-as-Code Adoption
   in Open-Source Software Projects,” 2026. <https://arxiv.org/abs/2601.05555>
2. Mark R. Opdebeeck et al., “An Empirical Study of Policy as Code,” MSR
   2026. <https://joaoff.com/publication/2026/MSR/msr26-policy-as-code.pdf>
3. Joseph W. Cutler et al., “Cedar: A New Language for Expressive, Fast, Safe, and Analyzable Authorization,” OOPSLA
   2024. <https://dl.acm.org/doi/10.1145/3649835>
4. Craig Disselkoen et al., “How We Built Cedar: A Verification-Guided Approach,”
   2024. <https://arxiv.org/abs/2407.01688>
5. Richard S. Sutton and Andrew G. Barto, *Reinforcement Learning: An Introduction*, second
   edition. <https://incompleteideas.net/book/the-book-2nd.html>
6. LLVM Project, “Machine Learning Guided Optimization.” <https://llvm.org/docs/MLGO.html>
7. IETF, RFC 4271, “A Border Gateway Protocol 4 (BGP-4).” <https://datatracker.ietf.org/doc/html/rfc4271>
8. Eric B. Laber et al., “Dynamic Treatment Regimes: Technical Challenges and Applications,” *Electronic Journal of
   Statistics*,
   2014. <https://projecteuclid.org/journals/electronic-journal-of-statistics/volume-8/issue-1/Dynamic-treatment-regimes-Technical-challenges-and-applications/10.1214/14-EJS920.full>
9. Andrea Visentin et al., “Computing Optimal (R, s, S) Policy Parameters by a Hybrid of Branch-and-Bound and Stochastic
   Dynamic Programming,” 2020. <https://arxiv.org/abs/2012.14167>
10. C. Drent, S. Kapodistria, and J. A. C. Resing, “Condition Based Maintenance Policies under Imperfect Maintenance at
    Scheduled and Unscheduled Opportunities,” 2019. <https://arxiv.org/abs/1903.10235>
11. Board of Governors of the Federal Reserve System, “Policy Rules and How Policymakers Use
    Them.” <https://www.federalreserve.gov/monetarypolicy/policy-rules-and-how-policymakers-use-them.htm>
12. Jay R. Lund, “Derived Operating Rules for Reservoirs in Series or in Parallel,” *Journal of Water Resources Planning
    and Management*, 1999. <https://ascelibrary.org/doi/10.1061/%28ASCE%290733-9496%281999%29125%3A3%28143%29>
13. Emma Salatiello, Silvestro Vespoli, Guido Guizzi, and Andrea Grassi, “Long-Sighted Dispatching Rules for
    Manufacturing Scheduling Problem in Industry 4.0 Hybrid Approach,” *Computers & Industrial Engineering*,
    2024. <https://doi.org/10.1016/j.cie.2024.110006>
14. Wade Genders and Saiedeh Razavi, “Policy Analysis of Adaptive Traffic Signal Control Using Reinforcement Learning,”
    *Journal of Computing in Civil Engineering*, 2020. <https://doi.org/10.1061/%28ASCE%29CP.1943-5487.0000859>
15. P. Mohammadi et al., “Comparative Analysis of Control Strategies for Microgrid Energy Management,”
    2024. <https://ieeexplore.ieee.org/document/10749831>
16. ASHRAE, *Guideline 36-2024: High-Performance Sequences of Operation for HVAC
    Systems*. <https://www.ashrae.org/technical-resources/standards-and-guidelines/titles-purposes-and-scopes>
17. Federico Rossi et al., “Workflows, User Interfaces, and Algorithms for Operations of Autonomous Spacecraft,” *IEEE
    Aerospace Conference*, 2023. <https://ai.jpl.nasa.gov/public/documents/papers/rossi-vaquero-et-al-IEEE2023.pdf>
18. H. Hammouch et al., “A Systematic Review and Meta-Analysis of Intelligent Irrigation Systems,”
    2024. <https://ieeexplore.ieee.org/document/10577970>