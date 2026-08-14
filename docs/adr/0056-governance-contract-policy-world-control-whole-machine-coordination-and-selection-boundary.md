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
Decision Authority Law   who may establish the Governance decision
Decision Basis           what explicit material permits that decision
Scope                    what the decision governs
Selection                which declared alternative is requested
Decision Arbitration     which valid decision governs when several compete
Binding                  that the selection applies to that scope
Replacement              how an existing binding is explicitly replaced
Withdrawal               how an existing binding is explicitly removed
```

Validity, singularity, complete decision, binding persistence, exact attribution, and non-propagation follow as
structural laws around those responsibilities.

> Governance is the Contract Authority that establishes which already-declared governed alternative applies to an
> explicit scope under an explicit decision authority and decision basis.

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

A Governance decision must also have an explicit Decision Basis. Governance does not calculate readiness, safety,
health, load, failure count, or another machine condition in order to create that basis. It consumes explicit material
established by the authority that owns that meaning.

Decision Authority must be a law, not merely one identity field. If one decision requires concurrence from several
declared authorities, that requirement is Governance meaning and must not be inferred from call order or backend
coordination.

When several otherwise-valid Governance decisions compete for the same exact Scope, hidden runtime order cannot choose
the winner. Governance must either reject the conflict or apply an explicit deterministic Decision Arbitration law.

Different Governance scopes have no implicit hierarchy, inheritance, propagation, or override relation. If a selection
in one scope constrains what may be selected in another scope, that relation must be owned by an explicit Whole Machine
Contract obligation outside Governance.

Every governed use needs one stable applicable Governance Binding fixed at an explicit governed-use boundary. An
established Binding remains authoritative for its exact Scope until it is explicitly replaced or withdrawn, and later
Replacement or Withdrawal does not retroactively rewrite a governed use that has already begun. A Contract pipeline flow
is the currently established concrete case of this law.

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
Decision Authority Law
        +
Decision Basis
        +
Explicit Scope
        +
Declared Alternatives
        +
Explicit Selection
        ↓
Decision Arbitration, when required
        ↓
Governance Binding
```

Governance does not create, edit, inherit, synthesize, waive, or partially override the alternative it selects.

Policy Worlds are the present primary governed alternatives. Governance is not semantically limited to Policy, but any
additional governed subject must itself be an explicitly declared Contract alternative. Governance cannot manufacture a
new subject merely because a backend can switch it.

The shape of Governance decision processing may resemble ordinary Contract pipeline processing, but Governance
responsibilities are not aliases for Input, Admission, Invariant, Publication, or another one-dimensional Contract
authority. Decision Authority Law is not Admission, Decision Basis is not Invariant, and Binding is not Publication.
Governance must not become a recursive ordinary Contract Pipeline merely to reuse those semantic names. Compiler and
frontend machinery may later reuse implementation mechanisms such as resolution, finite-set checking, canonicalization,
or deterministic lowering where their meaning remains unchanged.

### 4.2. Decision Authority Law

A Governance decision must be established only by explicitly declared Contract authority.

Decision Authority is a law rather than merely one runtime identity. The law may require one declared authority or an
explicit finite concurrence of several declared authorities.

```text
Authority A
    -> sufficient

or

Authority A
    +
Authority B
    -> required concurrence
```

The exact frontend form remains deferred. Runtime identity, process ownership, deployment location, request origin, UI
identity, service registry, or arrival order cannot create Decision Authority.

If concurrence is required, the order in which authoritative material arrives has no Contract meaning unless an explicit
Governance law gives that order meaning.

Decision Authority is exact to the declared Governance law and governed Scope. Authority for one decision or Scope does
not implicitly escalate, inherit, delegate, or confer authority over another. Any such relation must itself be explicit
Contract meaning.

### 4.3. Decision Basis

A Governance decision must have explicit established material that permits that decision.

Decision Basis may include explicit requests, authorizations, readiness results, inhibitions, or other Contract material
whose meaning is owned elsewhere. Governance consumes that material; it does not calculate it.

Decision Basis composition is deliberately closed and finite. One Decision Basis contains only:

- a finite set of required established material; and
- a finite set of blocking established material.

The Decision Basis is satisfied only when every required item is established and no blocking item is established.

Conceptually:

```text
required
    EmergencyRequested
    EmergencyPermitted

blocking
    EmergencyInhibited

all required established
+ no blocking material established
    -> Governance Decision Basis satisfied
```

Decision Basis does not admit a general Boolean or controller expression language. Nested conjunction, disjunction,
negation, threshold comparison, predicate evaluation, control flow, runtime query, and inferred absence are not Decision
Basis composition. In particular, absence of material is not interpreted as `NOT material`; a prohibition or inhibit
must be represented by explicit established blocking material.

If several independent basis alternatives may authorize the same governed Selection, those alternatives must be declared
separately rather than encoded as one expression such as `(A AND B) OR (C AND NOT D)`. Any competition between distinct
Governance decisions remains subject to Decision Arbitration.

One Governance decision has one fixed Decision Basis. The complete required and blocking material for that decision is
fixed at its explicit decision boundary. Material established after that boundary does not extend, replace, or alter the
Decision Basis of the already-established decision; it may participate only in another Governance decision.

This is a Contract boundary law, not a required epoch, snapshot, locking, or storage mechanism. Established Contract
material remains immutable, and realization remains free to preserve that fixed decision meaning by any valid mechanism.

Governance does not calculate conditions such as:

```text
failure count > N
latency > threshold
load > limit
network is degraded
state is unsafe
```

Those judgments belong to the Contract Authority or external mechanism that owns their meaning. Decision Basis cannot be
used as an alternate path for arbitrary controller logic.

### 4.4. Governance Scope

Every Governance binding has an explicit governed Scope.

The earlier rule:

```text
Governance selection scope = exactly one Interface
```

is withdrawn.

One Interface remains a valid possible Scope, but source containment no longer defines semantic Scope. A Governance
declaration nested inside an Interface, Core, or Whole Machine declaration, if such syntax is retained for authoring
convenience, cannot acquire that Scope merely by nesting.

The exact allowed Scope kinds and their frontend identities remain deferred.

Scope identity must not be inferred from request contents, runtime topology, implementation ownership, process
placement, or current machine conditions.

### 4.5. Selection

Governance selects only among already-declared governed alternatives.

For Policy World control:

```text
Policy
    defines World A

Policy
    defines World B

Governance
    selects A or B for one explicit Scope
```

A Policy name carries no hidden preference or activation meaning. Names such as `Normal`, `Emergency`, or `Default`,
declaration order, newest Version, filesystem order, registry order, or source proximity cannot choose the Selection.

Unknown or unresolved alternatives cannot be selected by fallback or inference.

Governance also cannot waive an active Contract. If an exceptional operating arrangement is allowed, that arrangement
must already exist as an explicitly declared governed alternative and Governance may select it normally.

### 4.6. Decision Arbitration

More than one otherwise-valid Governance decision may compete for the same exact Scope. Singularity requires one
authoritative result, but Singularity alone does not decide which result governs.

Governance therefore owns an explicit Decision Arbitration law whenever such competition is permitted.

```text
Decision A ─┐
            ├─ Decision Arbitration ─> one authoritative decision
Decision B ─┘
```

The Arbitration law may explicitly reject competing decisions or declare a finite deterministic precedence relation. It
must not depend on thread scheduling, lock acquisition order, CAS winner identity, message arrival accident, timestamp
accident, registry order, or another backend race.

Decision Arbitration does not move machine State. It decides which Governance decision has authority for the exact
Scope.

### 4.7. Binding

Selection alone is not enough. Governance establishes a Binding between one exact Scope and one exact selected
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

An established Binding remains authoritative for its exact Scope until it is explicitly replaced or withdrawn. It does
not silently expire because of elapsed time, restart, configuration reload, threshold change, or backend lifecycle. If a
time-limited authority is required, the ending condition must become explicit Contract material that can establish
Replacement or Withdrawal.

Every governed use must have an explicit boundary at which its applicable Governance Binding is fixed. Once that
governed use begins under one Binding, later Replacement or Withdrawal does not retroactively change that
already-established use. A Scope kind that cannot provide such an explicit governed-use boundary cannot be assumed to
inherit this law merely by analogy; whether it is a valid Governance Scope must be decided explicitly.

A Contract pipeline flow is the currently established concrete case of this general law.

```text
Binding(S, Normal)
        ↓
Flow A begins
        ↓
Flow A remains under Normal
```

A later Governance decision cannot rewrite that already-established flow.

### 4.8. Replacement

An established Governance Binding may be replaced only by another explicit Governance decision for the same governed
Scope.

```text
Binding(S, Normal)
        ↓
new explicit Governance decision
        ↓
Binding(S, Emergency)
```

Replacement changes applicability for subsequent governed use. It is not a transition of machine State and does not
mutate the alternative that was previously selected.

Already-established governed uses keep the Binding fixed at their governed-use boundary. A Contract flow is the current
concrete case: an already-established flow keeps the Binding under which it began, and a new flow uses the Binding
applicable when it begins.

No threshold crossing, configuration write, runtime restart, newest-loaded value, or backend winner may silently replace
a Governance Binding.

### 4.9. Withdrawal

An established Governance Binding may also be explicitly withdrawn without immediately establishing a replacement.

```text
Binding(S, A)
        ↓
explicit Withdrawal
        ↓
no Governance Binding for later governed use
```

Withdrawal removes applicability authority. It does not move machine State and does not mutate the previously selected
alternative.

If Governance is required for later use of that Scope, absence of an applicable Binding prevents the governed-use
boundary from being crossed. No new governed use is established, and no governed alternative becomes authoritative for
that use, until another valid Binding exists. Already-established governed uses keep the Binding fixed at their
governed-use boundary. A Contract flow is the current concrete case of this rule.

No timeout, process death, missing registry entry, configuration disappearance, or backend cleanup silently withdraws
Governance authority.

### 4.10. Validity, Singularity, and Complete Decision

An established Governance result must resolve exactly.

For one governed Scope at one applicable boundary:

```text
missing required Binding       -> governed-use boundary cannot be crossed
one exact Binding              -> valid
competing valid decisions      -> explicit Arbitration or invalid
ambiguous authoritative result -> invalid
```

Where Governance is required, a missing applicable Binding does not establish a partial, fallback, or default governed
use. The governed use simply does not become established at that boundary.

The selected alternative must exist, Scope must resolve explicitly, Decision Authority Law must be satisfied, Decision
Basis must be satisfied, and any required Arbitration must resolve exactly. Governance may not repair missing or
ambiguous material through a hidden default, undeclared fallback, or runtime arrival order.

A Governance decision is authoritative only when all material declared as part of that decision is complete. Partial
establishment does not create partial Governance authority. If one Governance decision contains several required
coordinates or bindings, all of them must be established before that decision has authority.

### 4.11. Exact Attribution

Every established Governance Binding must be exactly attributable to the canonical Governance material that established
it. At minimum, attribution must preserve:

- Governance Authority and Version;
- Decision Authority material;
- Decision Basis;
- exact Scope;
- exact Selection;
- any applied Decision Arbitration result;
- and the resulting Binding, Replacement, or Withdrawal identity.

Governance owns this attribution requirement. Presentation, retention, audit storage, and diagnostic lifecycle remain
the responsibility of the existing Publication, Diagnostic Evidence, Diagnostic Retention, and Verification boundaries.

### 4.12. No Implicit Scope Propagation

Governance scopes are independent unless an explicit Contract says otherwise.

```text
Scope A -> Policy Emergency

Scope B -> ?
```

The Selection for Scope A does not imply, inherit, propagate, override, or constrain the Selection for Scope B merely
because the scopes are structurally related, overlap, or belong to one Whole Machine. Structural containment does not
make a Binding for one Scope applicable to another Scope. Governance applicability is exact to the declared Scope
identity.

One governed-use boundary must resolve to exactly one applicable Governance Binding. If Bindings from distinct Scopes
would simultaneously apply to the same governed-use boundary, structural containment or overlap does not create
precedence between them. The model is ambiguous and invalid. If several governed parts must be ruled as one meaning,
that meaning must instead be represented by one explicit encompassing Governance Scope and one complete Selection under
§4.13.

If a valid Selection in Scope A requires, forbids, or constrains a Selection in Scope B, that cross-scope relation must
be declared as an explicit Whole Machine Contract obligation outside Governance. Governance consumes the resulting
explicit Contract meaning; it does not invent a transitive propagation law.

### 4.13. Atomic Governance Meaning Across Several Scopes

Independent Governance scopes remain independent. Contract-level atomicity is therefore not created by trying to
synchronize separate Scope Bindings.

If several governed alternatives must change as one indivisible Governance meaning, they must be represented by one
explicit Governance decision over one exact encompassing Scope with one complete Selection.

Conceptually:

```text
Commerce Scope

Selection Normal
    Payment   -> Normal
    Inventory -> Normal

Selection Emergency
    Payment   -> Emergency
    Inventory -> Emergency
```

The complete Selection becomes authoritative as one Governance decision for that encompassing Scope. A half-established
subset does not acquire partial Governance authority.

This law does not make a Binding for the encompassing Scope implicitly propagate into independent child Scopes. The
exact Scope and Selection themselves must declare the governed meaning. How a backend makes one such decision visible
across several physical components is implementation.

The exact Whole Machine Scope authoring form and canonical representation of a multi-part Selection remain deferred.

### 4.14. Separation from Policy

The separation is absolute.

```text
Policy
    owns one operating world's complete Contract arrangement

Governance
    owns whether a declared governed alternative applies to an explicit Scope
```

Policy does not select itself.

Governance does not define or partially rewrite Policy contents.

A change in Policy meaning is a Policy revision. A change in applicable Policy World is a Governance Binding,
Replacement, or Withdrawal decision.

### 4.15. Separation from the State-Machine Axis

Governance and the State Machine are independent Contract axes.

```text
Governance
    establishes which Contract authority applies

State Machine
    establishes current State
    and legal Transitions within that world
```

Governance does not set current State, execute a Transition, or use State-machine movement as an implicit side effect of
Selection, Arbitration, Binding, Replacement, or Withdrawal.

The State Machine does not automatically select, replace, withdraw, or arbitrate Governance because a particular State
was established.

If State-related meaning must affect Governance, or Governance-selected meaning must constrain later State-machine
judgment, that relation must cross an explicit Contract boundary and remain owned by the appropriate Contract
Authorities. Neither axis acquires the other's authority by implication.

### 4.16. Governance Is Not an Operation-Level Judgment Slot

Governance is not another ordinary pipeline judge beside Admission, Budget, Capacity, Invariant, State Machine, or
Publication.

The older conceptual form:

```text
Governance admits the contract world
    -> Permit / Refuse continuation
```

is withdrawn.

Governance establishes applicability before the governed use crosses its explicit governed-use boundary. A governed use
may fail to be established because required Governance material is invalid, absent, withdrawn, or unresolved, but
Governance is not a generic per-operation `canContinue` Boolean. A Contract pipeline flow is the current concrete case.

Earlier ADRs that still present Governance as an ordinary per-interaction refusal authority require reconciliation.

### 4.17. Governance Version

Governance is version-sensitive under ADR-0053 because Decision Authority Law, Decision Basis, Scope, Selection,
Decision Arbitration, Binding, Replacement, and Withdrawal are Contract meaning.

A Governance Version does not own the Versions of the governed alternatives.

```text
Governance / G2

Payment.Policy.Normal / N4
Payment.Policy.Emergency / E7
```

The Policy Versions identify the Policy Worlds. The Governance Version identifies the law by which one declared
alternative becomes authoritative for one governed Scope.

Changing a Policy does not automatically revise Governance. Changing Governance meaning does not rewrite Policy history.

The exact canonical Governance definition cannot close until the frontend forms of Decision Authority Law, Decision
Basis, governed Scope, Decision Arbitration, Binding, Replacement, and Withdrawal are finalized.

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
- Decision Authority Law;
- Decision Basis law;
- exact governed Scope identity;
- exact governed alternatives;
- Selection law;
- Decision Arbitration law;
- Binding law;
- Replacement law;
- Withdrawal law;
- the closed required/blocking Decision Basis composition law;
- the one-decision fixed Decision Basis boundary law;
- the governed-use Binding capture law;
- the one-applicable-Binding law for each governed-use boundary;
- the one-scope complete-decision law for indivisible multi-part Governance meaning;
- the semantic separation from ordinary Contract pipeline authorities;
- and the structural validity, singularity, persistence, attribution, and non-propagation requirements.

The exact byte-level identity and frontend forms remain deferred.

Backend representation remains replaceable after canonical meaning is fixed.

---

## 6. Verification

The final Governance verifier must be able to prove that every governed alternative resolves exactly and that Governance
cannot construct or modify the contents of that alternative.

It must verify that every Governance Binding has one explicit Scope, a satisfied Decision Authority Law, a satisfied
Decision Basis, one exact authoritative Selection after any required Arbitration, and exact attribution to the canonical
Governance material that established it.

It must reject:

- implicit scope inferred from source nesting, request contents, runtime topology, or implementation identity;
- implicit selection based on declaration order, newest-Version assumptions, names, backend registry order, or
  discovery;
- missing, unknown, or ambiguous governed alternatives;
- unsatisfied or ambiguous Decision Authority Law;
- missing or unsatisfied Decision Basis;
- attempts to extend, replace, or alter the fixed Decision Basis of an already-established Governance decision with
  material established after its decision boundary;
- Decision Basis expressions that use disjunction, nested Boolean expressions, negation, predicates, thresholds, runtime
  queries, control flow, or inferred absence instead of the closed required/blocking form;
- incomplete required concurrence;
- implicit authority escalation, inheritance, delegation, or transfer;
- competing valid decisions without explicit deterministic Arbitration;
- multiple authoritative bindings for the same governed Scope at the same applicable boundary;
- Bindings from distinct Scopes that would simultaneously apply to the same governed-use boundary;
- required-Governance boundaries that attempt to establish a governed use without one applicable Binding;
- partial establishment of one declared Governance decision;
- attempts to obtain atomic Governance meaning by synchronizing independent Scope Bindings instead of declaring one
  complete decision over one exact encompassing Scope;
- hidden fallback, precedence, expiry, or withdrawal rules;
- implicit propagation, inheritance, containment, overlap, or override from one Governance scope to another;
- Governance rules that attempt to establish State or execute State Transitions;
- State-Machine rules that implicitly select, arbitrate, replace, or withdraw Governance;
- Governance rules that waive or partially disable an active governed Contract instead of selecting a declared
  alternative;
- silent Binding expiry or Withdrawal caused only by runtime lifecycle;
- retroactive replacement or withdrawal of the Binding fixed for an already-established governed use;
- and recursive modeling that aliases Governance Decision Authority, Decision Basis, or Binding to ordinary pipeline
  Admission, Invariant, or Publication authority.

If a relation between two Governance scopes is required, the compiler must verify the explicit Whole Machine Contract
that owns that relation rather than inventing a Governance compatibility or propagation matrix.

The same canonical Governance material must produce the same authoritative binding regardless of source acquisition
order or backend representation.

Physical races, synchronization, storage, transport, and distributed realization remain implementation concerns. Their
realization must preserve the verified Governance meaning but does not extend this Contract Authority.

---

## 7. Contract and Implementation Boundary

Governance owns Decision Authority Law, Decision Basis, Scope, Selection, Decision Arbitration, Binding, Replacement,
and Withdrawal meaning. It does not own the physical mechanism that observes, stores, transports, synchronizes, or
applies that meaning.

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

Governance does not own monitoring, threshold evaluation, health calculation, readiness calculation, retry, failover,
circuit breaking, scheduling, load balancing, or recovery mechanisms. Those concerns may produce explicit material that
later enters Decision Basis, but their implementation does not become Governance law.

Likewise, approval workflow, operator screens, interlock hardware, audit stores, clocks, and timeout mechanisms do not
become Governance Authority merely because a realization uses them. Their results matter only when explicit Contract
material gives those results Decision Authority or Decision Basis meaning.

The backend may specialize lookup, precompute bindings, use compact canonical identities, or optimize replacement. Those
optimizations must preserve the same canonical Governance meaning.

---

## 8. Relationship to Whole Machine and Concurrency

ADR-0055 establishes the relevant Whole Machine flow boundary.

Independent Contract pipelines flow one way. One admitted Input establishes one flow. That flow is not altered by later
external material, and Whole Machine composition adds no Contract-level waiting, joining, buffering, or synchronization
mechanism.

Governance therefore does not coordinate pipeline execution. It establishes which declared Contract authority applies to
an explicit Scope before a governed use crosses its governed-use boundary. A Contract pipeline flow is the current
concrete case.

Several Governance scopes may belong to one Whole Machine, but no scope automatically controls another. Cross-scope
selection requirements, if any, must be expressed by explicit Whole Machine Contract obligations outside Governance. If
several changes are one indivisible Governance meaning, they must instead be declared as one complete Governance
decision over one exact encompassing Scope; atomic meaning is not created by synchronizing independent Scope Bindings.

A Governance Replacement or Withdrawal and a new governed use may be concurrent in one realization. Several Governance
decisions may also compete for the same exact Scope. The Contract rules are that explicit Decision Arbitration resolves
any permitted competition, one exact Binding is fixed at the governed-use boundary, and later changes do not
retroactively rewrite that use. A Contract flow is the current concrete case. Locking, CAS, epochs, transactions,
message order, scheduler order, distributed consensus, or another mechanism used to preserve those rules belongs to
implementation.

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
- Governance owns eight contract responsibilities: Decision Authority Law, Decision Basis, Scope, Selection, Decision
  Arbitration, Binding, Replacement, and Withdrawal.
- Decision Authority Law may require explicit finite concurrence from several declared authorities.
- Decision Authority does not implicitly escalate, inherit, delegate, or transfer across Governance decisions or Scopes.
- Governance does not define, waive, or partially override the contents of a governed alternative.
- governed Scope is explicit Contract material and is not inferred from source nesting or runtime facts.
- Governance scopes have no implicit hierarchy, inheritance, containment, overlap, propagation, or override relation.
- cross-scope constraints belong to explicit Whole Machine Contract obligations outside Governance.
- competing valid Governance decisions require explicit deterministic Arbitration or are invalid.
- an established Governance result must resolve to one exact Binding for its governed Scope.
- one Governance decision has no partial authority until all declared required material is complete.
- an established Binding persists until explicit Replacement or Withdrawal.
- every established Binding is exactly attributable to its canonical Governance material.
- Governance does not judge machine conditions; it consumes explicit Decision Basis material.
- one Governance decision fixes its complete Decision Basis at its explicit decision boundary; later material cannot
  alter that already-established decision.
- where Governance is required, absence of an applicable Binding prevents a new governed use from crossing its
  governed-use boundary and no governed alternative becomes authoritative for that use.
- one governed-use boundary resolves to exactly one applicable Governance Binding; overlapping Bindings from distinct
  Scopes are invalid rather than ordered by containment or implicit precedence.
- Governance and the State-Machine axis are separate authorities.
- every governed use fixes one exact Governance Binding at an explicit governed-use boundary;
- a Contract pipeline flow is the current concrete case of that rule;
- later Governance Replacement or Withdrawal does not retroactively rewrite an already-established governed use;
- Governance is not an ordinary per-operation `Permit`/`Refuse` judgment slot;
- Governance Decision Authority Law, Decision Basis, and Binding are not semantic aliases for Admission, Invariant, or
  Publication, even if compiler mechanisms may later be reused;
- implementation mechanisms cannot own Governance meaning merely because they carry or apply it;
- Governance is version-sensitive under ADR-0053.

### 9.2. Decisions Not Made Here

This ADR does not yet decide:

- the exact set of allowed governed Scope kinds and the governed-use boundary each allowed kind provides;
- the exact frontend representation of Decision Authority Law and its finite concurrence form;
- the exact frontend representation of the closed required/blocking Decision Basis form;
- the exact canonical syntax for Selection, Decision Arbitration, Binding, Replacement, and Withdrawal;
- the exact Whole Machine Scope authoring and canonical representation for one indivisible multi-part Governance
  decision;
- when Governance is required, optional, or explicitly absent for an exact Scope;
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

The Governance model must provide enough canonical material to identify one exact Decision Authority Law, Decision
Basis, Scope, Selection, Decision Arbitration result, Binding, Replacement, and Withdrawal result where each is
applicable.

Where the Contract theory intentionally permits several equivalent physical realizations, that freedom belongs to
implementation rather than to ambiguous Governance meaning.

---

## 11. Deferred Decisions

The following Governance questions remain open:

1. Which exact Contract Scope kinds may be governed: Interface, Core, Whole Machine, Flow, or another explicitly defined
   Scope, and what explicit governed-use boundary does each allowed kind provide?
2. What canonical material identifies one governed Scope without relying on source containment or runtime topology?
3. What exact Contract material identifies Decision Authority Law?
4. What finite canonical concurrence form is sufficient when one decision requires several declared authorities?
5. What exact frontend material names the finite required and blocking sets of Decision Basis without introducing a
   general Boolean or controller expression language?
6. What finite Decision Arbitration forms are permitted: conflict rejection, explicit precedence, or another closed law?
7. What exact frontend form declares Selection, Binding, Replacement, and Withdrawal without duplicating governed
   Contract contents?
8. What exact canonical material identifies one complete Governance decision and its attribution?
9. What exact Whole Machine Scope authoring and canonical Selection form represent one indivisible multi-part Governance
   decision when several governed alternatives must change as one meaning?
10. Which compiler or frontend mechanisms may be reused from ordinary Contract pipeline processing without aliasing
    Governance Decision Authority, Decision Basis, or Binding to Admission, Invariant, Publication, or another semantic
    Contract authority?
11. When is Governance required, optional, or explicitly absent for an exact Scope, including scopes with zero or one
    governed alternative, and how is absence represented without a hidden default Binding?
12. What failure and Diagnostic Evidence are established when Governance material is missing, unknown, unauthorized,
    incomplete, withdrawn, or ambiguous?
13. What public control API exposes Governance while keeping transport and realization outside Contract Authority?
14. What canonical identity material is required beyond ADR-0053 Version identity?

Whole Machine execution coordination, pipeline waiting, distributed synchronization, retry, failover, and runtime
replacement mechanisms are not deferred Governance semantics. They are realization concerns governed by ADR-0055's
Contract boundary and later backend implementation design.

---

## 12. Consequences

### Positive

Policy and Governance retain distinct responsibilities.

Policy declares an operating Contract World. Governance establishes whether an already-declared alternative applies to
an explicit scope under an explicit Decision Authority.

Governance now has enough structure to describe practical machine rule without becoming a general controller: Decision
Authority Law, Decision Basis, Scope, Selection, Decision Arbitration, Binding, Replacement, and Withdrawal are
explicit, while condition calculation and realization remain outside. Decision Basis itself is also closed to finite
required and blocking material rather than becoming a Boolean rule engine.

Competing legitimate decisions no longer require backend timing to choose authority. Explicit Arbitration either
resolves the competition deterministically or makes it invalid.

An established Binding cannot silently expire, and Governance can explicitly withdraw authority without pretending that
Withdrawal is a State Transition. Where Governance is required, Withdrawal cannot expose a hidden fallback world: later
governed use cannot cross its boundary until another applicable Binding exists.

One Governance decision also has one fixed Decision Basis. Later material cannot alter an already-established decision;
it can only participate in another decision.

Scope is no longer accidentally tied to Interface source nesting. This allows Governance to remain valid for larger
machine scopes without making source layout authoritative.

Cross-scope behavior cannot leak in through implicit hierarchy or propagation. Any required relation between scopes must
become explicit Whole Machine Contract meaning. Structural overlap also creates no precedence: one governed-use boundary
must have one exact applicable Governance Binding, or the model is invalid.

The State-Machine axis remains independent. Governance changes applicability; State Machine changes machine State.

Every governed use retains the Governance Binding fixed at its explicit governed-use boundary. A Contract pipeline flow
is the current concrete case, and later Replacement or Withdrawal does not retroactively rewrite it.

Indivisible multi-part Governance meaning can be expressed as one complete decision over one exact encompassing Scope
without turning independent Scope Bindings into a hidden synchronization protocol.

### Negative

Governance remains Proposed because the exact allowed Scope kinds, Decision Authority and Decision Basis forms,
permitted Arbitration laws, frontend syntax, and canonical Binding/Replacement/Withdrawal material are still open.

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