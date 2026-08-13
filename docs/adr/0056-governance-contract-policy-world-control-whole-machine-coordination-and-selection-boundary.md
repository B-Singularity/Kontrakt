# ADR-0056: Governance Contract, Policy-World Control, Whole-Machine Coordination, and Selection Boundary

## Status

Proposed

## Date

2026-08-11

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0055: Whole Machine, Pipeline Composition, and Contract Concurrency
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

ADR-0054 separates the declaration of one operating Contract World from the authority that controls which world governs.

A Policy declares one self-contained operating world for one Interface.

```text
Policy Normal
    -> Contract World Normal

Policy Emergency
    -> Contract World Emergency
```

Those worlds can exist as explicit Contract meaning without deciding which one applies to a governed scope.

Governance owns that applicability problem.

```text
Declared governed alternatives
        ↓
Governance
        ↓
one explicit Governance binding
        ↓
governed scope
```

Policy World control is the current concrete use of Governance, but Governance is not defined as a Policy-only
mechanism. Its general responsibility is to establish which already-declared governed alternative applies to an explicit
scope. Governance never defines the alternative it selects.

The earlier ADR-0054 model placed Governance inside one Interface, so its scope was implicitly the enclosing Interface.
That assumption no longer holds once Governance may govern a larger machine scope. Scope must therefore become explicit
Contract meaning rather than being inferred from source nesting.

ADR-0055 also closes the relevant Whole Machine flow law. Contract pipelines remain independent and one-way. One
admitted Input establishes one flow, that flow is not rewritten by later external material, and each Contract boundary
judges only its own declared obligations. Whole Machine composition introduces no Contract-level waiting, joining, or
synchronization mechanism.

This ADR establishes the minimum Governance authority needed above that flow model.

---

## 2. Problem

Once more than one governed alternative exists, the machine needs explicit authority over which alternative applies.

A Policy cannot solve that problem itself without becoming recursive.

```text
Policy Normal
    cannot own the rule that decides whether
    Policy Normal or Policy Emergency governs
```

Likewise, an environment variable, CLI option, feature flag, service lookup, deployment setting, operator action, or
runtime object may carry a Governance request, but none becomes Contract Authority merely by carrying that request.

The earlier Governance design solved only the smallest case:

```text
one Interface
+ one selected Manifest identity
    -> one active Contract World
```

ADR-0054 moved world declaration to Policy. ADR-0055 then made Whole Machine composition explicit without turning
independent Contract pipelines into one communicating runtime graph. Governance therefore does not need to coordinate
pipeline execution. It needs to establish applicability above those independent flows.

That requires more than Selection alone. Governance must make explicit:

```text
Decision Authority   who may establish the Governance decision
Scope                what the decision governs
Selection            which declared alternative applies
Binding              that the selection applies to that scope
Replacement          how an existing binding is explicitly replaced
```

Validity, singularity, and non-propagation follow as structural laws around those responsibilities.

> Governance is the Contract Authority that establishes which already-declared governed alternative applies to an
> explicit scope under an explicit decision authority.

Policy World control is one governed subject. Governance does not define Policy contents, calculate machine conditions,
move State, or implement the mechanism that carries or applies the decision.

---

## 3. Decision Drivers

Governance must remain separate from Policy.

```text
Policy
    declares one operating Contract World

Governance
    establishes whether that declared world applies to a governed scope
```

Governance must also remain separate from the laws inside a selected world. It may establish that one Policy world
applies, but it cannot change the Admission, Budget, Capacity, State Machine, Invariant, Publication, or other Contract
meaning declared by that Policy.

Governance scope cannot be derived from source nesting, runtime topology, request contents, implementation identity, or
backend discovery. If Governance scope is Contract meaning, its identity must be explicit.

Different Governance scopes have no implicit hierarchy, inheritance, propagation, or override relation. If a selection
in one scope constrains what may be selected in another scope, that relation must be owned by an explicit Whole Machine
Contract obligation outside Governance.

A pipeline flow still needs one stable applicable Contract World. A Governance binding may be replaced for later use,
but an already-established flow keeps the binding under which it began.

Governance must remain separate from the State-Machine axis. Governance changes which declared Contract authority
applies; it does not establish a State or execute a Transition. The State Machine establishes and moves machine State
within the applicable Contract World; it does not choose that world.

Governance must not derive authority from backend mechanisms. Lock order, CAS order, scheduler order, process topology,
service discovery, configuration storage, runtime object identity, and similar realization details cannot decide
Governance meaning.

Determinism remains mandatory. The same canonical Governance material must establish the same authoritative binding
regardless of source acquisition order, thread timing, hash iteration, or backend representation.

---

## 4. Contract Decision

### 4.1. Governance Authority

Governance is a user-sovereign Contract Authority over applicability of already-declared governed alternatives.

Its minimum structure is:

```text
Decision Authority
        +
Explicit Scope
        +
Declared Alternatives
        +
Explicit Selection
        ↓
Governance Binding
```

Governance does not create, edit, inherit, synthesize, or partially override the alternative it selects.

Policy Worlds are the present primary governed alternatives. Governance is not semantically limited to Policy, but any
additional governed subject must itself be an explicitly declared Contract alternative. Governance cannot manufacture a
new subject merely because a backend can switch it.

### 4.2. Decision Authority

A Governance decision must have an explicit Contract-authoritative basis.

Governance does not decide who is authoritative by inspecting runtime identity, process ownership, deployment location,
request origin, operator UI, service registry, or similar implementation facts.

The exact frontend representation of Decision Authority remains deferred, but one established Governance binding must be
attributable to an explicitly declared decision authority.

Decision Authority does not mean that Governance calculates whether a machine condition is true. A monitor, operator,
another Contract result, or another machine may provide explicit material. Governance receives that material through its
declared boundary; it does not infer authority from the mechanism that produced it.

### 4.3. Governance Scope

Every Governance binding has an explicit governed scope.

The earlier rule:

```text
Governance selection scope = exactly one Interface
```

is withdrawn.

One Interface remains a valid possible scope, but source containment no longer defines the semantic scope. A Governance
declaration nested inside an Interface, if such syntax is retained for authoring convenience, cannot make that Interface
the governed scope merely by nesting.

The exact allowed scope kinds and their frontend identities remain deferred.

Scope identity must not be inferred from request contents, runtime topology, implementation ownership, process
placement, or current machine conditions.

### 4.4. Selection

Governance selects only among already-declared governed alternatives.

For Policy World control:

```text
Policy
    defines World A

Policy
    defines World B

Governance
    selects A or B for one explicit scope
```

A Policy name carries no hidden preference or activation meaning. Names such as `Normal`, `Emergency`, or `Default`,
declaration order, newest Version, filesystem order, registry order, or source proximity cannot choose the selection.

Unknown or unresolved alternatives cannot be selected by fallback or inference.

### 4.5. Binding

Selection alone is not enough. Governance establishes a binding between one exact scope and one exact selected
alternative.

```text
Scope S
+
Selection A
    ↓
Binding(S, A)
```

Binding is an applicability relation, not machine movement. It does not establish a State and it is not a State
Transition.

For a Contract pipeline flow, the applicable Governance binding must already be established when that flow begins. The
flow keeps that binding through its one-way Contract processing.

```text
Binding(S, Normal)
        ↓
Flow A begins
        ↓
Flow A remains under Normal
```

A later Governance decision cannot rewrite that already-established flow.

### 4.6. Replacement

An established Governance binding may be replaced only by another explicit Governance decision for the same governed
scope.

```text
Binding(S, Normal)
        ↓
new explicit Governance decision
        ↓
Binding(S, Emergency)
```

Replacement changes applicability for subsequent governed use. It is not a transition of machine State and does not
mutate the Policy World that was previously selected.

Already-established flows keep the binding under which they began. New flows use the binding applicable when they begin.

No threshold crossing, configuration write, runtime restart, newest-loaded value, or backend winner may silently replace
a Governance binding.

### 4.7. Validity and Singularity

An established Governance result must resolve exactly.

For one governed scope at one applicable boundary:

```text
missing binding      -> invalid
one exact binding    -> valid
ambiguous bindings   -> invalid
```

The selected alternative must exist, the scope must resolve explicitly, and the decision authority must be attributable.
Governance may not repair missing or ambiguous material through a hidden default, fallback, precedence rule, or runtime
arrival order.

### 4.8. No Implicit Scope Propagation

Governance scopes are independent unless an explicit Contract says otherwise.

```text
Scope A -> Policy Emergency

Scope B -> ?
```

The selection for Scope A does not imply, inherit, propagate, override, or constrain the selection for Scope B merely
because the scopes are structurally related, overlap, or belong to one Whole Machine. Structural containment does not
make a binding for one scope applicable to another scope. Governance applicability is exact to the declared Scope
identity.

If a valid selection in Scope A requires, forbids, or constrains a selection in Scope B, that cross-scope relation must
be declared as an explicit Whole Machine Contract obligation outside Governance. Governance consumes the resulting
explicit Contract meaning; it does not invent a transitive propagation law.

### 4.9. Governance Does Not Judge Machine Conditions

Governance is not an arbitrary controller language.

It does not calculate conditions such as:

```text
failure count > N
latency > threshold
load > limit
network is degraded
state is unsafe
```

Those judgments belong to the Contract Authority or external mechanism that owns their meaning. Governance may receive
an explicit result or control request and apply its declared selection law.

The earlier possibility that Governance itself might judge established machine conditions is withdrawn.

### 4.10. Separation from Policy

The separation is absolute.

```text
Policy
    owns one operating world's complete Contract arrangement

Governance
    owns whether a declared governed alternative applies to an explicit scope
```

Policy does not select itself.

Governance does not define or partially rewrite Policy contents.

A change in Policy meaning is a Policy revision. A change in applicable Policy World is a Governance replacement.

### 4.11. Separation from the State-Machine Axis

Governance and the State Machine are independent Contract axes.

```text
Governance
    establishes which Contract World applies

State Machine
    establishes current State
    and legal Transitions within that world
```

Governance does not set the current State, execute a Transition, or use State-machine movement as an implicit side
effect of selection.

The State Machine does not automatically select or replace Governance because a particular State was established.

If State-related meaning must affect Governance, or Governance-selected meaning must constrain later State-machine
judgment, that relation must cross an explicit Contract boundary and remain owned by the appropriate Contract
Authorities. Neither axis acquires the other's authority by implication.

### 4.12. Governance Is Not an Operation-Level Judgment Slot

Governance is not another ordinary pipeline judge beside Admission, Budget, Capacity, Invariant, State Machine, or
Publication.

The older conceptual form:

```text
Governance admits the contract world
    -> Permit / Refuse continuation
```

is withdrawn.

Governance establishes applicability before the governed flow uses the selected Contract World. A flow may fail to be
established because required Governance material is invalid, but Governance is not a generic per-operation
`canContinue` Boolean.

Earlier ADRs that still present Governance as an ordinary per-interaction refusal authority require reconciliation.

### 4.13. Governance Version

Governance is version-sensitive under ADR-0053 because Decision Authority, Scope law, Selection law, Binding law, and
Replacement law are Contract meaning.

A Governance Version does not own the Versions of the governed alternatives.

```text
Governance / G2

Payment.Policy.Normal / N4
Payment.Policy.Emergency / E7
```

The Policy Versions identify the Policy Worlds. The Governance Version identifies the law by which one declared
alternative becomes applicable to one governed scope.

Changing a Policy does not automatically revise Governance. Changing Governance meaning does not rewrite Policy history.

The exact canonical Governance definition cannot close until the frontend forms of Decision Authority, governed Scope,
and Replacement are finalized.

---

## 5. Frontend and Resolution

### 5.1. Governed Alternative References

Governance refers to declared Contract Authorities. For current Policy World control, it refers to declared Policy
Authorities and their resolved Contract Worlds.

Those references must resolve exactly within their authority and Version context.

A runtime string, configuration key, enum ordinal, object reference, generated class, service-registry entry, or source
nesting cannot become Contract identity merely because one Governance realization uses it.

The exact `.kontrakt` syntax for Governance is deferred.

### 5.2. No Governance-Owned Alternative Declaration

Governance source must not contain a hidden duplicate of the Contract meaning it governs.

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

The Policy World must already exist as Policy:

```text
policy Emergency {
    ... complete Contract World ...
}

governance Main {
    ... refers to Policy Emergency ...
}
```

The same law applies to any future governed subject: Governance selects declared alternatives; it does not define them.

### 5.3. Scope Is Not Source Containment

The earlier Interface-local Governance syntax cannot remain a semantic shortcut for scope.

If Governance syntax is physically nested inside an Interface, Core, or Whole Machine declaration, that containment may
be authoring structure only. The governed Scope must still be explicit Contract material.

This prevents source layout from acquiring Governance Authority and allows the same Governance model to apply beyond one
Interface without hidden scope inference.

### 5.4. Canonical Governance Material

Canonical Governance material must eventually preserve at least:

- Governance Authority identity;
- Governance Version;
- declared Decision Authority material;
- exact governed Scope identity;
- exact governed alternatives;
- Selection law;
- Binding law;
- Replacement law;
- and the structural validity and singularity requirements.

The exact byte-level identity and frontend forms remain deferred.

Backend representation remains replaceable after canonical meaning is fixed.

---

## 6. Verification

The final Governance verifier must be able to prove that every governed alternative resolves exactly and that Governance
cannot construct or modify the contents of that alternative.

It must verify that every Governance binding has one explicit Scope and one attributable Decision Authority.

It must reject:

- implicit scope inferred from source nesting, request contents, runtime topology, or implementation identity;
- implicit selection based on declaration order, newest-Version assumptions, names, backend registry order, or
  discovery;
- missing, unknown, or ambiguous governed alternatives;
- multiple authoritative bindings for the same governed scope at the same applicable boundary;
- hidden fallback or precedence rules;
- implicit propagation, inheritance, containment, overlap, or override from one Governance scope to another;
- Governance rules that attempt to establish State or execute State Transitions;
- State-Machine rules that implicitly select or replace Governance;
- and replacement of an already-established flow's binding after that flow begins.

If a relation between two Governance scopes is required, the compiler must verify the explicit Whole Machine Contract
that owns that relation rather than inventing a Governance compatibility or propagation matrix.

The same canonical Governance material must produce the same authoritative binding regardless of source acquisition
order or backend representation.

Physical races, synchronization, storage, transport, and distributed realization remain implementation concerns. Their
realization must preserve the verified Governance meaning but does not extend this Contract Authority.

---

## 7. Contract and Implementation Boundary

Governance owns Decision Authority, Scope, Selection, Binding, and Replacement meaning. It does not own the physical
mechanism that observes, stores, transports, synchronizes, or applies that meaning.

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

None becomes Governance Authority merely because one backend uses it.

Likewise, an operator UI, CLI, deployment controller, scheduler, monitor, automation system, or another machine may
supply Governance input. The carrier or producer is not Governance Authority unless the Contract explicitly gives its
material that role.

Governance does not own monitoring, threshold evaluation, health calculation, retry, failover, circuit breaking,
scheduling, load balancing, or recovery mechanisms. Those concerns may produce explicit material that later enters
Governance, but their implementation does not become Governance law.

The backend may specialize lookup, precompute bindings, use compact canonical identities, or optimize replacement. Those
optimizations must preserve the same canonical Governance meaning.

---

## 8. Relationship to Whole Machine and Concurrency

ADR-0055 establishes the relevant Whole Machine flow boundary.

Independent Contract pipelines flow one way. One admitted Input establishes one flow. That flow is not altered by later
external material, and Whole Machine composition adds no Contract-level waiting, joining, buffering, or synchronization
mechanism.

Governance therefore does not coordinate pipeline execution. It establishes which declared Contract authority applies to
an explicit scope before a governed flow uses that authority.

Several Governance scopes may belong to one Whole Machine, but no scope automatically controls another. Cross-scope
selection requirements, if any, must be expressed by explicit Whole Machine Contract obligations outside Governance.

A Governance replacement and a new flow may be concurrent in one realization. The Contract rule is only that the flow
begins under one exact established binding and keeps it. Locking, CAS, epochs, transactions, message order, scheduler
order, distributed consensus, or another mechanism used to preserve that rule belongs to implementation.

Distributed realization may make Governance difficult to implement, but those threats and their responses are not new
Governance obligations.

---

## 9. Contract and Implementation Decisions

### 9.1. Decisions Made Here

This ADR decides:

- Governance and Policy are separate Contract Authorities.
- Policy declares one self-contained operating Contract World.
- Governance is not defined as a Policy-only mechanism; it governs applicability of already-declared alternatives.
- Policy Worlds are the current primary governed alternatives.
- Governance owns five contract responsibilities: Decision Authority, Scope, Selection, Binding, and Replacement.
- Governance does not define or partially override the contents of a governed alternative.
- governed Scope is explicit Contract material and is not inferred from source nesting or runtime facts.
- Governance scopes have no implicit hierarchy, inheritance, containment, overlap, propagation, or override relation.
- cross-scope constraints belong to explicit Whole Machine Contract obligations outside Governance.
- an established Governance result must resolve to one exact binding for its governed scope.
- Governance does not judge machine conditions; it consumes explicit Governance input.
- Governance and the State-Machine axis are separate authorities.
- one pipeline flow keeps the exact Governance binding under which it begins;
- later Governance replacement does not rewrite an already-established flow;
- Governance is not an ordinary per-operation `Permit`/`Refuse` judgment slot;
- implementation mechanisms cannot own Governance meaning merely because they carry or apply it;
- Governance is version-sensitive under ADR-0053.

### 9.2. Decisions Not Made Here

This ADR does not yet decide:

- the exact set of allowed governed Scope kinds;
- the exact frontend representation of Decision Authority;
- whether multiple declared authorities may be required to establish one Governance decision;
- the exact canonical syntax for Selection, Binding, and Replacement;
- the public Governance control API;
- failure and diagnostic presentation;
- or canonical identity bytes beyond ADR-0053 authority and Version requirements.

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

The Governance model must provide enough canonical material to identify one exact Decision Authority, Scope, Selection,
Binding, and Replacement result where each is applicable.

Where the Contract theory intentionally permits several equivalent physical realizations, that freedom belongs to
implementation rather than to ambiguous Governance meaning.

---

## 11. Deferred Decisions

The following Governance questions remain open:

1. Which exact Contract scope kinds may be governed: Interface, Core, Whole Machine, Flow, or another explicitly defined
   scope?
2. What canonical material identifies one governed Scope without relying on source containment or runtime topology?
3. What exact Contract material identifies Decision Authority?
4. Can one Governance decision require concurrence from more than one declared Decision Authority, and if so, what is
   the finite canonical form?
5. What exact frontend form declares Selection, Binding, and Replacement without duplicating governed Contract contents?
6. What failure and Diagnostic Evidence are established when Governance material is missing, unknown, unauthorized, or
   ambiguous?
7. What public control API exposes Governance while keeping transport and realization outside Contract Authority?
8. What canonical identity material is required beyond ADR-0053 Version identity?

Whole Machine execution coordination, pipeline waiting, distributed synchronization, retry, failover, and runtime
replacement mechanisms are not deferred Governance semantics. They are realization concerns governed by ADR-0055's
Contract boundary and later backend implementation design.

---

## 12. Consequences

### Positive

Policy and Governance retain distinct responsibilities.

Policy declares an operating Contract World. Governance establishes whether an already-declared alternative applies to
an explicit scope under an explicit Decision Authority.

Governance now has enough structure to describe ordinary machine rule without becoming a general controller: Decision
Authority, Scope, Selection, Binding, and Replacement are explicit, while condition calculation and realization remain
outside.

Scope is no longer accidentally tied to Interface source nesting. This allows Governance to remain valid for larger
machine scopes without making source layout authoritative.

Cross-scope behavior cannot leak in through implicit hierarchy or propagation. Any required relation between scopes must
become explicit Whole Machine Contract meaning.

The State-Machine axis remains independent. Governance changes applicability; State Machine changes machine State.

One pipeline flow retains one coherent Governance binding even if that binding is replaced for later flows.

### Negative

Governance remains Proposed because the exact allowed Scope kinds, Decision Authority representation, frontend syntax,
and canonical Replacement material are still open.

Several earlier ADRs and `What Contract Is` passages still describe Governance as an operation-level refusal authority,
as the owner of Manifest worlds, or as an Interface-local selector. Those references require reconciliation after
ADR-0056 settles.

The frontend can no longer rely on Interface nesting alone to define Governance scope.

### Neutral

This ADR does not require a distributed system, control plane, leader election protocol, transaction manager, actor
system, monitor, or any particular synchronization primitive.

It also does not require automatic mode switching or machine-condition calculation inside Governance.

Policy World control remains the current primary use, but the Governance definition is intentionally broader: it governs
applicability of explicitly declared alternatives without owning their contents.