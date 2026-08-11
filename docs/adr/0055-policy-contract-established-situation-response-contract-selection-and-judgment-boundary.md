# ADR-0055: Policy Contract, Established Situation, Explicit Choice, and Judgment Boundary

## Status

Proposed

## Date

2026-08-11

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0054: Governance Contract
- ADR-0053: Version Contract
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

A machine can remain the same machine while meeting situations that call for different choices.

A compiler may preserve one call and inline another. A production line may choose which ready job to run next. A
reservoir may hold water in one situation and release more in another. A traffic controller may give one movement
priority when ordinary timing would no longer serve the current conditions.

The fixed laws of those machines do not disappear when the situation changes. Correctness, physical limits, State
movement, Budget, Capacity, and other established authorities continue to hold. Policy exists where more than one choice
remains after those laws have done their work.

The word `policy` is used at different levels across engineering. In control and operations work it often means a rule
that maps an observed state to an action. In safety and organizational systems it can name a higher-level principle that
guides later decisions. In software it may mean anything from authorization to configuration or a strategy object.

Kontrakt needs a narrower contract meaning that can be made explicit and verified.

```text
Contract World already established
        ↓
Established Situation
        ↓
Policy
        ↓
Explicit Choice
```

Policy does not establish the Contract World, create the situation, or execute the chosen result. It declares how a
choice is resolved from material that is already established under contract authority.

This ADR defines that judgment boundary. The exact V1 choice surface and authoring syntax remain open.

---

## 2. Problem

Calling something a policy does not make its authority clear.

A rule may live in YAML, a callback, a strategy object, a rule engine, or ordinary branching code. Moving it out of the
main implementation can improve organization, but the important questions remain unanswered:

- What established situation may the rule inspect?
- What choice does it own?
- Which laws constrain that choice before Policy runs?
- Which Contract World makes the Policy applicable?
- What happens when several rules appear relevant?

Without a strict boundary, Policy absorbs unrelated responsibilities. Budget becomes a policy limit. Capacity becomes a
policy check. Governance becomes policy activation. State movement becomes a policy action. An optimizer or learned
model becomes policy merely because it recommends something.

That is too broad for Kontrakt.

Across engineering, the useful common point is not permission, denial, routing, scheduling, or optimization by itself.
The common point is that an established situation leaves a real choice to be resolved under a rule prepared in advance.

> Policy is the Contract that declares how an explicit choice is resolved under an established situation inside one
> Contract World.

This definition must remain separate from the procedure that carries out the result and from stronger laws that may
already have removed invalid choices.

---

## 3. Decision Drivers

Policy must describe more than prohibition without becoming a general algorithm language.

The same machine state can appear in different situations, and those situations may justify different choices. The
chosen result must therefore depend on explicit established material rather than hidden runtime context.

Policy must also preserve the authority of other Contracts. If Capacity rejects an admission, Policy cannot reinterpret
the Capacity result. If an Invariant is violated, Policy cannot declare it satisfied. If Governance has established one
Contract World, Policy cannot silently move to another Manifest. If a State movement is illegal, Policy cannot make it
legal.

The general Policy meaning must be broader than one particular V1 result shape. Response selection is an important case,
but engineering Policy can also express a declared preference or another finite decision where the choice itself is the
authority being modeled. The exact admitted forms must be decided separately.

Policy material must remain finite and inspectable. Arbitrary callbacks, hidden search, open-ended optimization, or
learned runtime ranking would move the authority back into implementation.

Determinism is mandatory.

```text
same Policy
same established situation
same declared choice material
same Contract World
    -> same Policy Judgment
```

Thread timing, discovery order, hidden mutable state, remote availability, or random model output may not change that
judgment unless such material first becomes part of the explicit contract basis under a separately declared authority.

---

## 4. Policy Across Engineering

Policy is used differently across engineering fields, but several recurring forms are useful for Kontrakt.

### 4.1. Control, Robotics, and Autonomous Operation

Control and robotics often use `policy` for a decision law from an observed state to an action. The idea is direct: the
system has an established view of its condition and must choose what to do next.

Kontrakt can use this form only after separating observation from authority. Sensor readings, estimator output, or model
state do not become Policy input merely because a controller can read them. The relevant material must first cross a
declared boundary and become established contract material.

A learned or probabilistic controller may help design a Policy, but a hidden adaptive model is not V1 Policy authority.

### 4.2. Safety-Critical and Physical Plant Operation

Nuclear plants, spacecraft, reservoirs, microgrids, and other physical systems operate under fixed protection laws and
hard limits. Within those walls, operators and control systems still make choices about output, timing, conservation, or
fallback behavior.

The distinction is important. A protection threshold that leaves only one legal response is not a Policy choice. Policy
belongs to the remaining decision space where several prepared alternatives are still allowed.

A reservoir, for example, may face high storage before expected rain. Physical limits and flood rules remove unsafe
releases, while an operating Policy can resolve how the remaining acceptable release choices should be handled. A
spacecraft may conserve power or delay optional work under a declared situation, but the Policy does not redefine
thermal or power limits.

### 4.3. Production, Maintenance, and Operations Research

Manufacturing, inventory control, maintenance, and scheduling use policies to resolve repeated operational choices from
current conditions.

A production Policy may decide which ready job receives priority when several legal jobs can run. An inventory Policy
may decide whether to replenish when the inventory position reaches a declared condition. A maintenance Policy may
choose inspection, continued operation, or prepared maintenance after equipment condition has been established.

These fields also show why Policy must not be confused with an optimizer. A search procedure may compare thousands of
schedules or maintenance plans. The search algorithm is not automatically the Policy. Policy authority exists only where
the choice law itself is declared and inspectable.

### 4.4. Infrastructure and Shared-Service Operation

Traffic control, building systems, communication networks, and energy infrastructure often use Policy to express how
competing valid concerns are resolved.

A traffic Policy can give emergency movement priority without redefining the physical signal constraints. A building
Policy can prefer ventilation over energy saving in an established air-quality situation. A routing Policy can prefer
one valid path over another while reachability and link constraints remain separate.

This form shows that Policy is not always best described as `condition -> response contract`. Sometimes the authority
being expressed is the rule that resolves a priority or preference among already valid alternatives.

### 4.5. Compilers and Software Machines

Compilers provide the same pattern in a non-physical machine. Correctness is fixed. Target capabilities and declared
limits constrain the available transformations. Inside that legal set, Policy may resolve whether a prepared
optimization choice applies to an established compile situation.

A hot call site may justify inlining under one Policy while the same call is preserved under another. The backend
performs the transformation; the Policy owns only the declared choice.

A full cost model, arbitrary candidate search, or learned inlining model is not made Policy by naming its output. If
such machinery contributes to a Policy judgment, the contract must expose the authoritative decision law rather than
hide it behind the recommendation.

### 4.6. Common Form

Across these fields, the reusable structure is:

```text
Fixed laws and limits remain in force.

The relevant situation is established.

Policy resolves a declared choice that is still open.

Other Contract authorities retain their own judgments.

Implementation realizes the resulting machine behavior.
```

Goals and criteria may explain why a Policy was designed, but they are not the Policy by themselves. A goal such as
safety, cost reduction, delivery speed, or energy conservation becomes Policy only when it is expressed as an explicit
decision law over declared situation material and a declared choice surface.

---

## 5. Decision

### 5.1. Policy Contract Meaning

A Policy Contract declares how one explicit choice is resolved under an established situation inside one Contract World.

```text
Policy Contract:
    established situation
        -> explicit choice judgment
```

A shorter statement is:

> Policy is the declared law for resolving a choice under an established situation.

This replaces the earlier temporary description of Policy as activation of judgment criteria. Policy does not change the
judgment law of another Contract, and it does not decide which Contract World is active.

The Contract World is already established before Policy judgment. When Governance exists, the selected Manifest
establishes that world for the pipeline. Without Governance, the Interface uses its static Contract World.

Policy does not discover or create the situation. It consumes a declared basis made from material that has already been
established under contract authority.

Policy also does not perform the chosen behavior. Its output is a Policy Judgment. State movement, publication, resource
admission, execution, or another effect remains with the authority that owns that effect.

### 5.2. Established Situation Is a Judgment Basis

`Established Situation` is not a new Contract kind and does not introduce a new runtime object hierarchy.

It is the finite, declared basis of one Policy judgment.

```text
Established Situation:
    exact established contract material
    explicitly bound for one Policy judgment
```

A Policy may use only material that has a declared contractual meaning and is suitable for later judgment. Current
Machine State, established Facts, and typed judgments from other Contracts are representative examples. The exact
admissible surface remains part of the Policy authoring design.

The important law is not that every Contract result is automatically visible to Policy. Nothing creates an implicit
global context. Each coordinate used by Policy must be explicitly bound to the authority that established its meaning.

Policy receives that material as immutable input. It may use the material to make its own judgment, but it cannot
rewrite the meaning established by the originating Contract.

### 5.3. Explicit Choice

Policy requires a declared choice surface.

The general contract theory does not yet restrict that surface to one form. A choice may eventually be represented as
selection among prepared responses, judgment of one proposed response, an explicit priority, or another finite decision
form that survives the same authority tests.

The choice must be real contract material. It cannot be an unbounded set discovered from implementations or a wrapper
around a hidden optimizer result.

Policy therefore cannot invent a new alternative while judging one situation, scan plugins for candidates, or ask a
runtime service what should be chosen.

The exact V1 choice forms are deferred.

### 5.4. Policy Judgment Forms

The general Policy result is a typed judgment that resolves the declared choice.

A direct-selection form would be:

```text
Established Situation
    + Declared Alternatives
        -> Selected Alternative
```

A smaller V1 surface may instead judge one proposed alternative:

```text
Established Situation
    + Proposed Alternative
        -> Permit
        or Refuse
```

These are different surfaces over the same Policy meaning. This ADR does not yet choose between them.

Preference, priority, ranking, scoring, and prescription are not automatically admitted or rejected merely because other
fields call them policy. Each must be judged by whether its meaning can be made finite, explicit, deterministic, and
separate from implementation.

### 5.5. V1 Candidate Boundary

The conservative V1 candidate remains:

```text
one exact proposed choice
+ one established situation
+ one exact Policy
+ one Contract World
    -> Permit
    or Refuse
```

Under this candidate, another contract surface presents one proposed choice and Policy judges whether it may apply in
the established situation.

V1 does not need open-ended candidate search, continuous optimization, schedule rebuilding, arbitrary cost models, or
learned runtime ranking to support this form.

This remains a candidate rather than a final V1 decision.

### 5.6. Relation to Other Contract Authorities

Policy can consume established material without acquiring the authority that produced it.

Budget owns allowance and exhaustion. Capacity owns occupancy admission. Governance establishes the Contract World.
Machine owns State meaning and legal movement. Admission, Invariant, Publication, and other judgment Contracts own their
own results.

Policy may use an established result from one of those authorities only when that result is explicitly bound into the
Policy situation and remains meaningful after the originating judgment. It cannot reverse the result or pretend that it
was produced under Policy authority.

Policy therefore cannot spend Budget, admit Capacity, select a Governance Manifest, move State, rejudge an Invariant, or
republish a Fact merely because related material appears in its situation basis.

ADR-0053 also applies normally: Policy is a version-sensitive Contract Authority and must carry its own explicit Version
under the Version law. Version identity does not imply preference, compatibility, or activation.

---

## 6. Policy Contract

### 6.1. Established Situation

A Policy judges one finite declared situation basis.

The basis contains exact contract material already established in the current Contract World. It is not a mutable
context object, a registry, or a population that Policy may scan.

```text
Established contract material
        +
explicit Policy binding
        ->
Policy situation coordinate
```

The binding must identify the contractual source of meaning closely enough to avoid ambiguity. A Fact kind alone is not
sufficient when materially different established results of that kind could exist in the same relevant scope.

The source syntax and canonical binding representation remain open.

Policy may use current Machine State as situation material when it is explicitly bound. It may not inspect hidden
transition history or derive movement authority from the State representation.

External observations such as time, sensor data, profiler measurements, environment values, repository state, or network
information do not enter Policy directly. They must first cross the appropriate boundary and become explicit material
under a declared authority.

Backend objects are never situation coordinates merely because they contain useful values.

```text
Allowed shape
    established Fact
    established Machine State
    typed result from a declared Contract

Forbidden shape
    profiler implementation field
    private capacity counter
    mutable singleton state
    service lookup result
```

The examples describe the authority boundary, not a final exhaustive list of Policy-readable Contract kinds.

### 6.2. Declared Choice Surface

A Policy owns one declared choice surface.

The surface must be closed enough for the compiler to know what Policy can decide. It may not be populated by scanning
host types, service registries, plugins, callbacks, or runtime objects.

A prepared response contract is one valid candidate model for such a choice surface, but it is not yet the only possible
Policy form.

Whatever form is chosen, Policy cannot create a new choice during judgment. The alternatives and the meaning of the
judgment must already be part of authoritative contract material.

The choice surface also does not grant execution authority. Selecting or permitting an alternative does not itself
perform the operation represented by that alternative.

### 6.3. Choice Law

A Policy is prepared before the particular judgment.

Its law relates the declared situation basis to the declared choice surface. The same canonical inputs must produce the
same Policy Judgment.

The source language must therefore remain restricted. Exact equality, finite membership, closed Boolean relations, and
comparisons against declared bounds are examples of forms that may be suitable. The final vocabulary remains open.

A Policy situation coordinate must describe established material, not hide the Policy answer inside the input.

```text
Situation material
    currentLoad = High
    callFrequencyClass = Hot
    queueLength = 120

Hidden Policy result disguised as input
    bestChoice = Inline
    weightedUtility = 0.73
    optimizerRecommendation = PreserveCall
```

A derived value is not neutral merely because it was computed earlier. If the value already encodes which choice is
better under the purpose of the Policy, the Policy judgment has been moved outside the Policy surface.

This boundary does not forbid precomputation. It forbids hiding decision authority in material presented as neutral
situation state.

Arbitrary methods, loops, callbacks, repository access, reflection, or user-defined evaluators are not Policy material.

### 6.4. Applicability

Policy participation must be explicit in the Contract World.

Applicability may not be inferred from package placement, host class names, registration order, thread identity,
environment names, or the backend that loaded the Policy.

A Policy that does not participate produces no hidden permission or refusal. The exact representation of Policy absence
remains part of the authoring design.

The same Policy definition may be bound where the contract model allows it, but binding does not rewrite Policy identity
or Version.

### 6.5. Governance and Contract World

Policy never makes itself active.

Governance, when present, establishes one Manifest for the Interface and thereby establishes the Contract World used by
new pipeline flows. Policy operates only inside the world already established for that flow.

```text
Governance
    establishes Contract World

Policy
    resolves a choice inside that world
```

Policy cannot select another Manifest, change Governance Version, or treat a Manifest name as a Policy response.

An external control system may later use a Policy result as part of a larger control decision, but Whole Machine
coordination, Governance selection triggers, and cross-Interface control are deferred to the later concurrency and Whole
Machine work. They are not Policy authority in this ADR.

### 6.6. Policy and Purpose

Purpose explains why a Policy exists. It does not by itself determine a Policy judgment.

A system may care about safety, cost, throughput, latency, energy, comfort, or mission value. Those concerns become
Policy only when they are reflected in an explicit choice law.

This distinction matters for priorities. Engineering policies often express that one concern should take precedence in a
particular situation. Kontrakt does not reject that idea, but the priority must itself be explicit contract material if
it becomes authoritative. Hidden weights or a score produced by an optimizer do not satisfy that requirement.

A learned model or optimizer may help a designer produce Policy material. It may not remain the hidden runtime source of
the Policy judgment in V1.

### 6.7. Policy and Fixed Laws

Policy operates only where a choice remains.

A stronger Contract judgment cannot be weakened or erased by Policy. If Capacity has refused admission, an Invariant is
violated, or a State movement is illegal, Policy cannot convert that result into the opposite result.

If such a judgment is later presented as situation material for another valid Policy judgment, Policy may respond to the
fact that it occurred. That later judgment still cannot rewrite the original result.

This gives two boundary tests:

> Policy may respond to established contract material. It may not redefine that material.

> When another authority has already fixed the only legal result, Policy does not recreate that result as its own
> choice.

### 6.8. Policy and Implementation

Policy judgment and realization are separate.

```text
Policy
    produces its own judgment

Other Contract authorities
    judge effects they own

Implementation
    realizes the resulting behavior
```

A backend may lower Policy into a decision table, specialized branch structure, masks, indexes, or another deterministic
evaluator. These forms are implementation.

A scheduler, optimizer, controller, strategy object, profiler, collector, or service may realize surrounding work or
help establish declared input material. None becomes the source of Policy meaning merely because the backend uses it.

Replacing one backend mechanism with another must preserve the same Policy judgment for the same canonical Policy
inputs.

### 6.9. More Than One Policy

More than one Policy may eventually participate in the same larger decision, but their relation cannot be inferred.

Kontrakt must not use file order, registration order, newest-Version-wins, callback order, or an unstated priority
convention to combine Policy authority.

Whether V1 permits more than one Policy in the same relevant judgment scope, and what explicit combination law would be
required if it does, remain deferred.

### 6.10. No Hidden Override

Debug mode, administrator identity, environment variables, exception paths, or runtime flags may not override a Policy
judgment unless a later Contract design explicitly gives such material authority.

An implementation escape hatch that changes the contract result is an implementation that has acquired contract
authority.

### 6.11. Policy Identity and Version

A Policy has its own Contract Authority identity and explicit Version under ADR-0053.

Its canonical definition must distinguish the declared situation basis, choice surface, and choice law. The exact
canonical identity bytes remain deferred until the authoring and lowering surface is fixed.

The active Contract World is judgment context and binding context. It is not automatically part of the intrinsic Policy
Authority identity.

Host class names, object identity, constructor calls, source-file order, reflection handles, or generated evaluator
types cannot define Policy identity.

---

## 7. Policy Judgment

### 7.1. Inputs

One Policy judgment requires explicit inputs.

```text
exact Policy
established situation basis
exact declared choice material
Contract World established for the flow
```

If required material is absent, unresolved, corrupt, or unavailable, Kontrakt must not fabricate a Policy judgment.
Failure classification remains with the appropriate Failure design.

### 7.2. Result

The result must be typed as Policy judgment rather than returned as an unnamed Boolean or arbitrary host value.

The general result resolves the declared choice. A direct-selection surface may identify one exact alternative. A
conservative V1 surface may instead produce `PolicyPermitted` or `PolicyRefused` for one proposed alternative.

The result must retain enough canonical relation to identify the Policy and the choice that was judged. Diagnostic text
is not identity.

### 7.3. Permission and Refusal Candidate

If V1 adopts the proposed-choice surface:

```text
PolicyPermitted
    the proposed choice may apply under this Policy judgment

PolicyRefused
    the proposed choice must not apply under this Policy judgment
```

Permission does not prove that the whole pipeline must continue. Other Contract authorities retain their own ability to
stop or reject what they own.

### 7.4. Determinism

Policy judgment must be repeatable from canonical inputs.

```text
same canonical Policy
same canonical situation basis
same canonical choice material
same canonical Contract World
    -> same canonical Policy Judgment
```

A learned, adaptive, or externally updated system may help produce a later Policy definition through an explicit
contract-authoring process. It may not remain an undeclared runtime source of the judgment.

Probabilistic Policy is outside V1.

---

## 8. Authoring and Processing Boundary

This ADR does not fix the final `.kontrakt`, Java, or Kotlin Policy authoring API.

The authoring surface must eventually carry the semantic material established here: Policy identity and Version, a
finite situation basis, exact bindings to established contract material, a closed choice surface, an explicit choice
law, and the binding that makes Policy participate in a Contract World.

Resolution must remove ambiguous references. Validation must reject hidden inputs, hidden fallback, and executable
behavior. Canonicalization and lowering must preserve Policy meaning without giving authority to source order,
host-language representation, or backend layout.

The following cannot define Policy authority:

```text
fun interface Policy
class PolicyImpl : Policy
lambda predicate
callback chain
annotation-discovered rule
service-loaded strategy
reflection-based policy scan
arbitrary user evaluator
```

Such mechanisms may exist behind the contract boundary, but they do not define the Policy.

The later authoring design still needs to decide how Policy is named, how situation coordinates bind to exact
established material, what choice forms V1 admits, what relation vocabulary is legal, and how Policy participation is
represented in both static Contract Worlds and Governance Manifests.

---

## 9. Contract and Implementation Boundary

### 9.1. Decisions Made Here

This draft decides that Policy is an explicit Contract Authority for resolving a declared choice under an established
situation inside one already-established Contract World.

Established Situation is the finite declared basis of a Policy judgment, not a new Contract kind or an implicit runtime
context.

Policy may consume established material without acquiring the authority that established it. It cannot activate
Governance, change State movement law, perform Budget or Capacity accounting, rewrite another judgment, or execute the
chosen behavior.

Policy choice and implementation procedure are separate. The backend may specialize the Policy evaluator, but
optimization cannot change the canonical judgment.

Policy is broader than authorization and refusal, but it is narrower than arbitrary optimization, planning, scheduling,
or control software. Those systems become Policy authority only to the extent that their actual decision law is
presented as finite explicit contract material.

### 9.2. Decisions Not Made Here

This draft does not yet decide:

- the final Policy authoring syntax;
- the exact V1 choice surface;
- whether V1 selects from a finite set or judges one proposed choice;
- whether explicit priority is admitted as a first-class Policy choice;
- the final relation vocabulary;
- whether and how more than one Policy can participate in one decision;
- canonical identity bytes;
- Failure mapping and diagnostic representation;
- the exact source form for binding situation coordinates;
- the exact Policy absence representation.

Whole Machine coordination, cross-pipeline Policy use, Governance selection triggers, and contract concurrency are
outside this ADR and remain deferred to the later Whole Machine and concurrency work.

### 9.3. No Hardware or Backend Authority

Hardware capability, scheduler behavior, storage layout, cache state, thread count, allocator behavior, deployment
configuration, or optimizer implementation do not define Policy meaning.

Those mechanisms may help establish declared situation material or realize the final behavior. Replacing them must
preserve the Policy judgment for the same canonical inputs.

---

## 10. Determinism and Verification

Before Policy material becomes authoritative, Kontrakt must be able to verify at least these properties:

- Policy identity and Version resolve exactly.
- Policy participation in the Contract World is explicit.
- The situation basis is finite and contains only declared established material.
- Every situation coordinate resolves without backend identity or hidden runtime lookup.
- Situation material is read-only to Policy and cannot be rewritten under another Contract's authority.
- The choice surface is finite and explicit enough for the selected Policy form.
- The choice law uses only the admitted declarative vocabulary.
- No hidden score, optimizer recommendation, or preselected answer is disguised as neutral situation material.
- No executable host behavior or undeclared fallback participates in judgment.
- Policy does not claim authority owned by Governance, Machine, Budget, Capacity, Invariant, Admission, Publication, or
  another Contract.
- Lowered evaluation is equivalent to canonical Policy material.

Closed Policy material should permit compiler-generated fixtures and property-based judgment checks. When the admitted
vocabulary and choice space are small enough, complete decision-table testing may also be generated.

Backend specialization is admissible only when the canonical result remains unchanged.

---

## 11. Deferred Decisions

The following questions remain open for ADR-0055 work:

1. What exact kinds of choice should the general Policy model admit?
2. What is the smallest V1 choice surface: one proposed choice with Permit/Refuse, or direct selection from a finite
   set?
3. Should explicit priority be a first-class Policy result, or should it be represented through another declared choice
   form?
4. Which established Contract material may be bound into a Policy situation, and what exact binding material identifies
   its source of meaning?
5. What closed relation vocabulary is expressive enough without becoming an algorithm language?
6. Can more than one Policy participate in one judgment scope, and if so, what explicit combination law is allowed?
7. How is Policy absence represented without creating an implicit default?
8. Where and how is Policy bound in the static Contract World and in each Governance Manifest?
9. What canonical material defines Policy identity beyond the authority and Version law already established by ADR-0053?

The Governance, Version, Budget, and Capacity meanings referenced by this ADR are already established by ADR-0051
through ADR-0054. This ADR no longer depends on redefining their responsibilities.

---

## 12. Consequences

### Positive

Policy receives a meaning broad enough to cover recurring engineering decisions without turning every rule, optimizer,
or operating procedure into Policy.

The model works for physical machines and software machines because it separates the same three things in both: the
established situation, the choice authority, and the mechanism that realizes the result.

Governance now has a clean relation to Policy. Governance establishes the Contract World; Policy resolves a choice
inside that world.

The same separation protects other Contract authorities. Policy can react to established material without taking
ownership of the law that established it.

### Negative

The final Policy authoring surface cannot simply reuse ordinary strategy patterns or arbitrary predicates.

A useful V1 will require a deliberately small choice surface and relation vocabulary. More powerful decision systems may
need to expose a finite contractual boundary before Kontrakt can treat their result as Policy authority.

The general Policy definition is intentionally broader than the likely V1 realization, so later work must avoid
confusing a narrow first implementation with the full contract meaning.

### Neutral

This ADR does not prohibit optimizers, planners, schedulers, controllers, profilers, learned models, rule engines,
generated decision tables, or specialized evaluators.

It decides only where their authority stops. They may help establish explicit inputs or realize a Policy judgment, but
they do not define Policy meaning unless the relevant decision law itself is presented as contract material.