# ADR-0056: Governance Contract, Policy-World Control, Whole-Machine Coordination, and Selection Boundary

## Status

Proposed

## Date

2026-08-11

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0054: Policy Contract, Explicit Operating Modes, Self-Contained Contract Worlds, and Interface Binding Boundary
- ADR-0053: Version Contract, Sovereign Contract Revision History, and Realization Boundary
- ADR-0052: Capacity Contract, Explicit Safe Operating Memory Limits, and Realization Boundary
- ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror

---

## 1. Context

ADR-0054 separates the declaration of one operating Contract World from the authority that controls which world governs
the machine.

A Policy declares one self-contained operating world for one Interface.

```text
Policy Normal
    -> Contract World Normal

Policy Emergency
    -> Contract World Emergency
```

Those worlds can exist as explicit Contract meaning without deciding when either world should govern.

Governance owns that larger control problem.

```text
Declared Policy Worlds
        ↓
Governance
        ↓
Governing Policy-World arrangement
        ↓
new Contract pipeline flows
```

This is broader than the earlier ADR-0054 model. That design treated Governance as an Interface-local owner of named
Manifests and as a selector that simply received one explicit Manifest identity. The Manifest meaning now belongs to
Policy, and the final scope of Governance must be able to grow beyond one Interface.

A real machine may coordinate several Interfaces. A change in operating condition may require different Policy worlds to
govern different parts of that machine together. World changes may also race with new pipeline flows or with other
Governance changes.

Those questions are governance questions because they concern how the machine is ruled as a whole, not what one Policy
world contains.

This ADR establishes that separation and the minimum Governance authority. The complete Whole Machine and concurrency
law remains open, so this ADR stays Proposed.

---

## 2. Problem

Once several Policy worlds exist, the machine needs authority over their governing status.

A Policy cannot solve that problem itself without becoming recursive.

```text
Policy Normal
    cannot own the rule that decides whether
    Policy Normal or Policy Emergency governs
```

If a Policy selected another Policy, the selected world's meaning and the selection law would collapse into the same
authority. Policy would stop being one self-contained operating world and become a controller over worlds.

Ordinary runtime configuration is also insufficient. An environment variable, CLI option, feature flag, service lookup,
deployment setting, or operator action may carry a control request, but those mechanisms do not by themselves explain
which world is authoritative or how a change relates to flows already in progress.

The earlier Governance design solved only the smallest case:

```text
one Interface
+ one selected Manifest identity
    -> one active Contract World
```

That was useful, but it made several assumptions that no longer hold.

It made Governance own the world declarations themselves. ADR-0054 now assigns that responsibility to Policy.

It also fixed Governance scope to one Interface and deliberately kept trigger logic outside Governance. That is too
narrow if Governance is the Contract that represents actual machine rule and coordinated operating response.

Whole Machine control may need to establish a coordinated arrangement such as:

```text
Payment
    -> Policy Emergency

Inventory
    -> Policy Restricted

Shipping
    -> Policy Normal
```

The exact law for forming and changing such an arrangement is not yet decided. It depends on later Whole Machine and
concurrency work. What can be decided now is the authority boundary.

> Governance is the Contract Authority that controls which declared Policy worlds govern a machine scope and how that
> governing arrangement changes.

Governance does not define those Policy worlds. It governs them.

---

## 3. Decision Drivers

Governance must remain separate from Policy.

```text
Policy
    declares one operating Contract World

Governance
    controls governing status across declared Policy worlds
```

Governance must also remain separate from the laws inside those worlds. It may establish that one Policy world governs
an Interface, but it cannot change the Admission, Budget, Capacity, Machine, Invariant, Publication, or other Contract
meaning declared by that Policy.

The final Governance scope must not be hard-coded to one Interface. A useful machine may coordinate several Interfaces,
and the governing arrangement may need to change across them together.

At the same time, this ADR must not invent a Whole Machine ontology merely for symmetry. The exact Whole Machine
identity, collaboration law, distributed boundary, and concurrency semantics remain later work.

A pipeline flow still needs a stable Contract World. Once a flow begins under one established world, a later Governance
change must not silently rewrite the meaning of that already-running flow.

Governance must not derive authority from backend mechanisms. Lock order, CAS order, scheduler order, process topology,
service discovery, leader election implementation, configuration storage, and runtime object identity are realization
concerns unless a later Contract explicitly gives one of their results meaning.

Determinism remains mandatory. The same canonical Governance meaning and the same explicit governing basis must not
produce a different authoritative arrangement because of file order, thread timing, hash iteration, or backend
representation.

---

## 4. Contract Decision

### 4.1. Governance Authority

Governance is a user-sovereign Contract Authority over governing Policy-world arrangements.

Its smallest responsibility is:

```text
Declared Policy Worlds
    -> Governance
    -> Governing Policy-World arrangement
```

A governing arrangement identifies which already-declared Policy world governs each scope participating in that
Governance decision.

Governance does not create a Policy, edit a Policy, inherit from a Policy, or substitute individual Contract bindings
inside one Policy world.

If a required operating world does not exist as declared Policy material, Governance cannot invent it at runtime.

### 4.2. Policy-World Control

Governance controls worlds that Policy has already defined.

```text
Policy
    defines World A

Policy
    defines World B

Governance
    establishes which world governs
```

The former Governance-owned Manifest layer is removed. Governance refers to Policy identities and their resolved
Contract Worlds directly.

A Policy name carries no hidden activation meaning. Names such as `Normal`, `Emergency`, or `Default` do not cause
Governance to prefer or automatically establish that Policy.

Likewise, declaration order, newest Version, filesystem order, registry order, or source proximity cannot determine
governing status.

### 4.3. Governance Does Not Redefine a Contract World

A Governance decision may change which world governs. It may not rewrite the world it selects.

```text
Allowed
    governing Policy changes from Normal to Emergency

Not allowed
    keep Policy Normal identity
    but silently replace its Budget or Machine binding
```

A different world must already be represented by a different Policy definition or by a new Version of the relevant
authority under ADR-0053.

Governance therefore cannot act as an override layer over Policy.

### 4.4. Governance Scope

The final governed scope is not fixed to one Interface by this ADR.

The earlier rule:

```text
Governance selection scope = exactly one Interface
```

is withdrawn.

One-Interface Governance remains a valid small case, but Governance must be capable of a later Whole Machine model in
which several Interface Policy worlds are controlled together.

Conceptually:

```text
Governed machine scope
    Interface A -> Policy A2
    Interface B -> Policy B1
    Interface C -> Policy C4
```

This ADR does not yet define `Whole Machine` as a new standalone Contract type, nor does it decide how machine
membership is authored. Those questions are deferred.

The decision here is only that Governance authority is not semantically limited to one Interface.

### 4.5. Governing Arrangement

A Governance result establishes a governing arrangement from declared Policy worlds.

For a one-Interface case:

```text
Payment
    -> Policy Emergency
```

For a later multi-Interface case:

```text
Payment
    -> Policy Emergency

Inventory
    -> Policy Restricted

Shipping
    -> Policy Normal
```

The exact canonical structure of a multi-Interface arrangement remains open.

A governing arrangement is not itself a replacement Contract World that merges all participating Interface worlds into
one recursive mega-Contract. Each Policy continues to own the Contract World of its Interface.

Governance owns the relation that says which of those worlds govern together.

### 4.6. Establishment and Change

Governance must make governing status explicit.

When Governance participates, the machine may not infer the governing Policy from its name, count, source order,
currently loaded implementation, environment label, or last successful flow.

The final form of Governance may receive an explicit control request, judge established machine conditions, or use
another explicitly declared control basis. This ADR does not yet choose that law.

The earlier rule that Governance must remain a deliberately dumb selector and that all trigger logic must always stay
outside Governance is therefore withdrawn.

What remains mandatory is the authority boundary:

```text
observation / operator action / controller result
    does not become Governance authority by itself

Governance Contract
    owns the declared law by which governing status is established or changed
```

If external machinery supplies input to Governance, that input must cross an explicit boundary before it can affect
Contract authority.

### 4.7. One Pipeline Flow, One Contract World

A Contract pipeline flow is governed by the Contract World established for it when the flow begins.

That world remains the world of the flow until the flow reaches its declared end.

```text
Flow A begins under Policy Normal
    Input
      -> ...
      -> Output

Governance establishes Policy Emergency for later flows

Flow A remains under Normal

Flow B begins under Emergency
    Input
      -> ...
      -> Output
```

A Governance change does not rewrite a flow already in progress.

This preserves one coherent contract meaning from Input through Output and prevents a single flow from crossing between
worlds merely because control changed while it was running.

### 4.8. Concurrent Flow Start and Governance Change

The semantic rule is that each flow has one exact governing world.

This ADR does not decide the physical or concurrency protocol that determines which world a flow receives when flow
start races with a Governance change.

```text
Governance change
        ||
new flow start
```

Locking, CAS, epochs, transactions, actor order, message order, scheduler order, or another realization strategy must
not become hidden Contract meaning.

The later concurrency ADR must define the deterministic Contract boundary required to establish one exact result without
tying that meaning to one backend mechanism.

### 4.9. Runtime Re-Governance

Governance may change the governing arrangement while the system is running.

That change is not a mutation of Policy meaning. It is a new Governance establishment over already-declared Policy
worlds.

The reason for the change may come from operator control, automation, machine conditions, another Contract result, or
later Whole Machine logic. Which of those forms become direct Governance input remains open.

No implementation may silently change the governing arrangement merely because a threshold was crossed or a
configuration value changed unless the declared Governance law gives that event authority through an explicit boundary.

### 4.10. Separation from Policy

The separation is absolute.

```text
Policy
    owns one operating world's complete Contract arrangement

Governance
    owns which declared operating world or coordinated arrangement governs
```

Policy does not activate itself.

Governance does not define the contents of a Policy world.

A change in Policy meaning is a Policy revision. A change in which Policy governs is a Governance event or judgment
under the later completed Governance law.

### 4.11. Governance Is Not an Operation-Level Judgment Slot

Governance is not another ordinary pipeline judge beside Admission, Budget, Capacity, Invariant, Machine, or
Publication.

The older conceptual form:

```text
Governance admits the contract world
    -> Permit / Refuse continuation
```

is too small and is withdrawn.

Governance establishes the world under which pipeline judgments have meaning. It therefore sits at a control boundary
around pipeline flows rather than acting as one more local condition inside each flow.

A pipeline may fail because no governing world can be established or because Governance control is invalid, but that
does not turn Governance into a generic `canContinue` Boolean.

Earlier ADRs that still present Governance as an ordinary per-interaction refusal authority require later
reconciliation.

### 4.12. Governance Version

Governance is version-sensitive under ADR-0053 because its own control law is Contract meaning.

A Governance Version does not own the Versions of the Policies it may govern.

```text
Governance / G2

Payment.Policy.Normal / N4
Payment.Policy.Emergency / E7
Inventory.Policy.Restricted / R3
```

The Policy Versions identify the worlds. The Governance Version identifies the Governance law that controls their
governing status.

Changing a Policy does not automatically revise Governance. Changing Governance control meaning does not rewrite Policy
history.

The exact canonical Governance definition cannot close until the Governance input basis, Whole Machine scope, and
coordination law are finalized.

---

## 5. Frontend and Resolution

### 5.1. Policy References

Governance refers to declared Policy Authorities, not to former Manifest identities.

Those references must resolve exactly within their Interface authority and Version context.

A runtime string, configuration key, enum ordinal, object reference, generated class, or service-registry entry cannot
become the identity of a Policy merely because Governance realization uses it.

The exact `.kontrakt` syntax for Governance is deferred.

### 5.2. No Governance-Owned World Declaration

Governance source must not contain a hidden duplicate of Policy world bindings.

Conceptually wrong:

```text
governance Main {
    mode Emergency {
        admission EmergencyAdmission
        budget EmergencyBudget
        ...
    }
}
```

The world must already exist as Policy:

```text
policy Emergency {
    ... complete Contract World ...
}

governance Main {
    ... refers to Policy Emergency ...
}
```

The exact Governance control law remains open, but world definition does not move back into Governance.

### 5.3. Canonical Governance Material

Canonical Governance material must eventually preserve at least:

- Governance Authority identity;
- Governance Version;
- the governed scope;
- exact Policy references that may participate;
- the declared basis for establishing or changing governing status;
- and the exact resulting governing arrangement semantics.

The last three items are not fully specified by this ADR because Whole Machine and control-law design are incomplete.

Backend representation remains replaceable after canonical meaning is fixed.

---

## 6. Verification

The final Governance verifier must be able to prove that every Policy reference resolves exactly and that Governance
cannot construct or modify Policy world contents.

It must reject implicit world choice based on declaration order, newest-Version assumptions, name conventions, host
object identity, backend registry order, or configuration discovery.

For every established pipeline flow, one exact governing Contract World must be attributable. A flow may not silently
combine parts of two Policy worlds because Governance changed during execution.

When several Interfaces later participate in one governed machine scope, the compiler must verify the declared
coordination relation without inventing a compatibility matrix or hidden priority scheme.

The same canonical Governance inputs and Contract material must produce the same authoritative governing arrangement
regardless of source acquisition order or backend representation.

Complete verification requirements for concurrent changes, simultaneous control requests, Whole Machine membership, and
distributed realization remain deferred because their semantic laws are not yet closed.

---

## 7. Contract and Implementation Boundary

Governance owns governing status and its declared control law. It does not own the physical mechanism that observes,
stores, transports, synchronizes, or applies that status.

Possible realization mechanisms include:

```text
atomic references
CAS protocols
locks
transactions
WAL records
actors
control-plane services
message logs
replicated state machines
configuration stores
```

None is Governance authority merely because one backend uses it.

Likewise, an operator UI, CLI, deployment controller, scheduler, monitor, or automation system may request a governance
change. The request mechanism is not the Governance Contract.

If future Governance uses established machine conditions to decide an operating response, the observation mechanism that
produced those conditions still does not become Governance authority.

The backend may specialize Policy-world lookup, precompute coordinated arrangements, use compact canonical identities,
or optimize the change path. Those optimizations must preserve the same canonical Governance meaning.

---

## 8. Relationship to Whole Machine and Concurrency

Governance is the first current Contract whose natural authority may extend across several Interface worlds.

That exposes work intentionally deferred by the earlier single-Interface design.

The later Whole Machine and concurrency work must decide at least:

```text
what constitutes one governed machine scope

how several Interfaces participate in that scope

whether a Governance change may establish several Policy worlds as one coordinated result

what material can justify or request that change

how simultaneous Governance changes are resolved

how a flow start is ordered against a Governance change at the Contract level

how already-running flows preserve their original worlds

how local and distributed realization preserve the same meaning
```

This ADR does not solve those problems by inventing lock, epoch, scheduler, or network semantics.

It establishes that they belong on the Governance side of the architecture rather than being hidden inside Policy or
implementation.

---

## 9. Contract and Implementation Decisions

### 9.1. Decisions Made Here

This ADR decides:

- Governance and Policy are separate Contract Authorities.
- Policy declares one self-contained operating Contract World.
- Governance controls which declared Policy worlds govern a machine scope.
- Governance no longer owns a semantic Manifest entity.
- Governance does not redefine Policy world contents.
- Governance is not semantically restricted to exactly one Interface.
- one pipeline flow keeps one exact Contract World from its start through its declared end;
- later Governance change does not rewrite an already-running flow;
- Governance is not an ordinary per-operation `Permit`/`Refuse` judgment slot;
- implementation mechanisms cannot own governing status merely because they store or transport it;
- Governance has its own Version when its control law becomes authoritative.

### 9.2. Decisions Not Made Here

This ADR does not yet decide:

- the exact Whole Machine ontology or authoring surface;
- the exact governed-scope identity;
- whether Governance control is expressed primarily as explicit command, situation-based law, another declared control
  basis, or a constrained combination;
- the exact Governance judgment/result types;
- how several Interface Policy worlds are established atomically or otherwise coordinated;
- how simultaneous Governance requests are ordered contractually;
- the exact concurrency law between Governance change and flow start;
- the public control API;
- distributed Governance realization;
- failure and diagnostic presentation;
- canonical identity bytes beyond ADR-0053 authority and Version requirements.

These questions must be settled before this ADR becomes Accepted.

---

## 10. Determinism and Verification Boundary

Governance cannot use hidden runtime order to settle authority.

The following may not determine Governance meaning by themselves:

```text
thread scheduling
lock acquisition order
CAS winner identity
message arrival accident
hash iteration
filesystem order
service discovery order
process startup order
runtime object identity
latest-loaded configuration
```

A backend may use such mechanisms only to realize a separately declared deterministic Contract law.

The final Governance model must provide enough canonical material to identify one exact governing result for the
relevant scope.

Where the Contract theory intentionally permits several equivalent physical realizations, that freedom belongs to
implementation rather than to ambiguous Governance meaning.

---

## 11. Deferred Decisions

The following Governance questions remain open:

1. What exact Contract material defines one governed Whole Machine scope?
2. How are Interface Authorities enrolled in that scope without making runtime topology authoritative?
3. What is the smallest Governance input: an explicit requested Policy arrangement, established machine conditions, or
   another closed control form?
4. When Governance may judge an appropriate response from machine conditions, what finite law is expressive enough
   without becoming an arbitrary controller language?
5. How is a multi-Interface Policy-world arrangement represented canonically?
6. What is the Contract-level rule when a Governance change races with a new flow start?
7. How are simultaneous Governance changes resolved without giving authority to scheduler or lock order?
8. How are partial failure and distributed realization handled without allowing one machine part to silently enter an
   undeclared world?
9. How is static operation represented when no dynamic Governance participates?
10. What exact frontend and control APIs expose Governance while keeping transport and implementation outside Contract
    authority?

The existence of these open questions is intentional. The previous Interface-local Manifest selector looked complete
only because it excluded the larger governance problem. This ADR keeps that larger problem visible instead of hiding it
in implementation.

---

## 12. Consequences

### Positive

Policy and Governance now match their distinct machine responsibilities.

Policy declares an operating world. Governance governs which declared worlds are in force and how that rule changes
across the machine.

The model can grow toward Whole Machine control without turning Policy into a recursive selector or duplicating Policy
bindings inside Governance.

One pipeline flow retains one coherent Contract World even when the machine is re-governed while that flow is running.

The design also keeps concurrency and distributed control honest. Their realization may be difficult, but the difficulty
is no longer hidden behind an Interface-local selector that pretends machine governance is only a configuration lookup.

### Negative

Governance is no longer complete enough to remain Accepted. Whole Machine scope, control input, coordination, and
concurrency semantics must be designed before the Contract can close.

Several earlier ADRs and `What Contract Is` passages still describe Governance as an operation-level refusal authority
or as the owner of Manifest worlds. Those references must be reconciled after ADR-0054 and ADR-0056 settle.

The eventual Governance implementation will be more demanding than a simple atomic selected-Manifest field because the
Contract meaning may span several Interface worlds and concurrent flows.

### Neutral

This ADR does not require a distributed system, control plane, leader election protocol, transaction manager, actor
system, or any particular synchronization primitive.

It also does not require automatic mode switching. Manual control, static establishment, or later automatic Governance
may all be realizable forms if their Contract meaning is explicit.

The key decision is narrower: world definition belongs to Policy; machine rule over those worlds belongs to Governance.