# ADR-0056: Governance Contract, Policy-World Control, Whole-Machine Scope, and Selection Boundary

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
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure
- `docs/design/planner-budget-resolution-and-worker-lifecycle.md`

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
Declared Policy Worlds
        ↓
Governance
        ↓
one explicit Governance Binding
        ↓
explicit governed Scope
```

The earlier ADR-0054 model placed Governance inside one Interface. The enclosing Interface therefore implicitly
determined the governed Scope, while Governance selected among the Policy Worlds declared for that Interface. That model
could govern one Interface, but it could not express an explicit Governance Scope beyond the enclosing Interface. Scope
must therefore be Contract meaning rather than a consequence of source nesting.

ADR-0055 closes the relevant Whole Machine flow law. Contract pipelines remain independent and one-way. One admitted
Input establishes one flow, that flow is not rewritten by later external material, and each 1D Contract judges only its
own declared obligation. Whole Machine composition introduces no Contract-level waiting, joining, or synchronization
mechanism.

A Governance change therefore does not need a separate compatibility controller. The selected Policy World determines
the 1D Contracts that apply when later contract processing begins, and those 1D Contracts accept or reject material
according to their own authorities.

This ADR establishes the minimum Governance authority needed above that flow model.

## 2. Problem

Once several Policy Worlds exist, the machine needs explicit authority over which World applies.

A Policy cannot solve that problem itself without becoming recursive.

```text
Policy Normal
    cannot own the rule that decides whether
    Policy Normal or Policy Emergency governs
```

An environment variable, CLI option, feature flag, service lookup, deployment setting, operator action, or runtime
object may carry material toward Governance, but none becomes Contract Authority merely by carrying that material.

The earlier Governance design solved only the smallest case:

```text
one Interface
+ one selected Manifest identity
    -> one active Contract World
```

ADR-0054 moved World declaration to Policy. ADR-0055 then made Whole Machine composition explicit without turning
independent Contract pipelines into one communicating runtime graph. Governance therefore does not coordinate pipeline
execution. It establishes Policy-World applicability at explicit governed boundaries.

That requires eight explicit responsibilities:

```text
Decision Basis         what explicit request and situational material one decision judges
Decision Law           how that material determines a declared Governance response
Scope                  what exact governed subject the decision applies to
Selection              which declared Policy World the response establishes
Decision Arbitration   which valid decision governs when several compete
Binding                that the Selection applies to that Scope
Replacement            how an existing Binding is explicitly replaced
Withdrawal             how an existing Binding is explicitly removed
```

Governance itself is the Contract Authority for those decisions. External actors, other Contracts, and runtime
mechanisms may carry explicit material toward Governance, but they do not become Governance Authority by doing so.

> Governance is the Contract Authority that establishes and controls which declared Policy World applies to an explicit
> Scope.

Policy still owns every selected World. The 1D Contracts selected by that World remain solely responsible for their own
obligations.

## 3. Decision Drivers

Governance must remain separate from Policy and from the 1D Contracts inside a selected World.

```text
Policy
    declares one operating Contract World

Governance
    establishes whether that World applies to an explicit Scope

selected 1D Contracts
    judge their own obligations
```

Governance Scope cannot be derived from source nesting, runtime topology, request contents, implementation identity, or
backend discovery. If Scope is Contract meaning, its identity must be explicit.

A Governance decision must have an explicit Decision Basis and an explicit Decision Law. The Basis contains the request
and situational material presented to that decision. Material already established by another Contract Authority or by
the State-Machine axis remains owned by that authority; Governance may reference that established meaning but does not
re-establish it merely because the material participates in a Governance decision. Realization-originated material may
participate only through an explicit declared boundary whose meaning is fixed independently of the Selection being
decided. The Decision Law is user-declared Contract meaning that judges the declared Basis and determines the declared
Governance response. Governance does not infer hidden machine conditions, inspect another authority's internal
representation, or obtain undeclared material through runtime lookup.

Contract judgment and realization judgment are separate. The Governance Contract decides which Policy World should
govern under the explicit request and situation. Kontrakt and its backend separately decide how to realize that
already-established meaning correctly and deterministically. Backend readiness cannot silently alter the Governance
decision or select another World.

When several otherwise-valid Governance decisions compete for the same exact Scope, hidden runtime order cannot choose
the winner. Governance must either reject the conflict or apply an explicit deterministic Decision Arbitration law.

Different Governance Scopes have no implicit hierarchy, inheritance, propagation, or override relation. Structural
overlap creates no precedence. One exact Contract application must resolve to one applicable Governance Binding.

An established Binding persists until explicit Replacement or Withdrawal. Replacement is non-retroactive: contract
processing already proceeding under one Binding is not rewritten by a later Governance decision. When several
independent Contract pipeline flows participate in one already-proceeding Whole Machine cooperation, realization must
preserve the same established Binding across that cooperation rather than re-resolving the current Governance decision
between flows. How that continuity is represented and propagated is backend-owned.

Governance must remain separate from the State-Machine axis. Governance controls Policy-World applicability; it does not
establish a State or execute a Transition.

Governance must not derive authority from backend mechanisms. Lock order, CAS order, scheduler order, process topology,
service discovery, configuration storage, runtime object identity, and similar realization details cannot decide
Governance meaning.

Determinism remains mandatory. The same canonical Governance material must establish the same authoritative result
regardless of source acquisition order, thread timing, hash iteration, or backend representation.

## 4. Contract Decision

### 4.1. Governance Authority

Governance is the Contract Authority over applicability of already-declared Policy Worlds.

Its minimum structure is:

```text
Decision Basis
        ↓
Decision Law
        +
Explicit Scope
        ↓
Selection
        ↓
Decision Arbitration, when required
        ↓
Governance Binding
```

Governance does not create, edit, inherit, synthesize, waive, or partially override the Policy World it selects.

Policy Worlds are the direct governed subjects of this Contract. Governance does not directly control Admission, Budget,
Capacity, State Machine, Invariant, Version, Publication, or another 1D Contract. A selected World determines which 1D
Contracts apply when affected contract processing begins, and each of those Contracts retains its own authority.

The shape of Governance decision processing may resemble ordinary Contract pipeline processing, but Governance
responsibilities are not aliases for Input, Admission, Invariant, Publication, or another 1D Contract authority.
Decision Basis and Decision Law are not Admission or Invariant, and Binding is not Publication. Governance must not
become a recursive ordinary Contract Pipeline merely to reuse those semantic names. Compiler and frontend machinery may
later reuse implementation mechanisms such as resolution, finite-set checking, canonicalization, or deterministic
lowering where their meaning remains unchanged.

### 4.2. Governance Is Its Own Decision Authority

Governance itself establishes Governance decisions.

An operator, controller, another machine, another Contract, or runtime identity may provide explicit material to
Governance. Providing that material does not make the producer a Governance Authority.

Several Governance 1D Contracts may exist in one larger machine, but their authority positions must be structurally
resolved before decisions under them are used. Governance cannot dynamically select, bind, replace, withdraw, or
arbitrate between Governance Contracts, including itself.

```text
Governance G
    may arbitrate Decision A vs Decision B

Governance G
    may not choose Governance G1 vs Governance G2
```

Decision Arbitration therefore resolves decisions under one already-established Governance Contract. It does not choose
which Governance Contract is authoritative.

The same law applies to Governance Version. Governance identity and Version are resolved before Governance decisions are
established; Governance cannot dynamically choose the law that governs itself.

### 4.3. Decision Basis

A Governance decision judges explicit material presented at its decision boundary.

Decision Basis contains the material needed by that decision. It may contain an explicit Governance request, material
already established by another Contract Authority or by the State-Machine axis, realization-originated material that
arrives through an explicit declared boundary, or a declared combination of those sources. These are not fixed
Governance ontology categories such as `Authorization`, `Readiness`, or `Inhibition`; they remain material whose
coordinates and meaning are owned by their declaring or establishing authorities.

Conceptually:

```text
request
    EmergencyRequested

situation
    ThermalCritical
    CoolingUnavailable
    MaintenanceActive
```

The names above stand for declared Governance inputs, not for sensors, callbacks, logs, or backend probes. Governance
does not discover hidden conditions through callbacks, service lookup, sensors, thread state, backend objects, or other
runtime observation. Material that matters to Governance must cross an explicit Contract boundary before the Decision
Law uses it.

When another authority has already established material, Governance references that established material and its exact
semantic relation. It does not recompute the source judgment, copy its authority into Governance, or inspect arbitrary
internal fields merely because those fields are available to one implementation. A source cannot become an open object
graph for Governance conditions; the material used by the Decision Law must be explicitly declared as part of the
Decision Basis.

Realization may observe or measure machine conditions, but raw observation does not become Governance Authority by
proximity. If realization-originated information participates in a Governance decision, the material and the boundary by
which it becomes eligible for that decision must be explicit, stable, and independently established before the Selection
it helps determine. The exact admissible realization-originated material remains deferred.

One Governance decision has one fixed Decision Basis. The complete material for that decision is fixed at its explicit
decision boundary. Material established after that boundary does not extend, replace, or alter the Basis of the already-
established decision; it may participate only in another Governance decision.

This is a Contract boundary law, not a required epoch, snapshot, locking, or storage mechanism. Established Contract
material remains immutable, and realization remains free to preserve that fixed decision meaning by any valid mechanism.

Any material used by one Governance decision must already be authoritative independently of the Selection that the same
decision is trying to establish. A Contract that becomes applicable only because the requested Policy World is selected
cannot provide a prerequisite judgment for that same Selection.

### 4.4. Decision Law

Decision Law is the user-declared Governance law that judges one complete Decision Basis and determines the declared
Governance response.

Conceptually:

```text
EmergencyRequested
+
explicit situational material
        ↓
Governance Decision Law
        ↓
Selection Emergency
```

The Decision Law may express finite, explicit, deterministic, and lowerable conditions over the material declared in
Decision Basis. It may relate material established by several owning authorities, but those source meanings remain owned
by their original authorities; the Decision Law establishes only the Governance response. A situation condition may
therefore be genuine Governance Contract meaning when the user declares that relation as part of how the machine
responds.

The Decision Law is not a general controller programming surface. It cannot contain arbitrary user algorithms,
callbacks, hidden runtime queries, implementation lookup, scheduler-dependent observation, backend control flow, or
undeclared inspection of source internals. It also cannot read additional material that was not declared in the fixed
Decision Basis for that decision. The exact finite condition language and frontend form remain deferred.

A Decision Law must not delegate the same decision back into the Policy World whose applicability it is deciding. A
Contract whose authority exists only after one Selection becomes applicable cannot establish the prerequisite judgment
that causes that same Selection.

Governance Contract judgment ends with the Contract meaning of the response. Whether the backend has prepared an image,
distributed a revision, retained an earlier Binding, reached a reclamation point, or can otherwise publish that response
correctly is realization judgment and is not part of the Decision Law.

### 4.5. Governance Scope

Every Governance Binding has one explicit governed Scope.

The earlier rule:

```text
Governance selection scope = exactly one Interface
```

is withdrawn.

One Interface remains a possible Scope, but source containment no longer defines semantic Scope. A Governance
declaration nested inside an Interface, Core, or Whole Machine declaration, if such syntax is retained for authoring
convenience, cannot acquire that Scope merely by nesting.

Scope identity must not be inferred from request contents, runtime topology, implementation ownership, process
placement, or current machine conditions.

The exact allowed Scope kinds and their frontend identities remain deferred.

### 4.6. Selection

Governance selects only among Policy Worlds already declared for the governed meaning.

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

Unknown or unresolved Worlds cannot be selected by fallback or inference.

Governance also cannot waive an active Contract. If an exceptional operating arrangement is allowed, that arrangement
must already exist as an explicitly declared Policy World and Governance may select it normally.

### 4.7. Decision Arbitration

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

Decision Arbitration resolves Governance decisions only. It does not move machine State and does not choose between
Governance Contracts.

### 4.8. Binding

Selection alone is not enough. Governance establishes a Binding between one exact Scope and one exact selected Policy
World.

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

Once contract processing begins under an applicable Governance Binding, a later Replacement or Withdrawal does not
retroactively change that already-established applicability. The same rule extends across an already-proceeding Whole
Machine cooperation even when that cooperation is realized through several independent Contract pipeline flows.

```text
Binding(S, Normal)
        ↓
A -> B -> C -> D begins under Normal
        ↓
Replacement(S, Emergency)
        ↓
the already-proceeding cooperation remains under Normal
```

### 4.9. Replacement

An established Governance Binding may be replaced only by another explicit Governance decision for the same governed
Scope.

```text
Binding(S, Normal)
        ↓
new explicit Governance decision
        ↓
Binding(S, Emergency)
```

Replacement changes applicability for contract processing established after the Replacement. It is not a transition of
machine State and does not mutate the Policy World that was previously selected.

Replacement must not divide one already-proceeding cooperation across different Contract Worlds. Processing that belongs
to such a cooperation continues under the Binding already established for it, including later participating pipeline
flows. New cooperation established after the Replacement uses the new Binding.

No threshold crossing, configuration write, runtime restart, newest-loaded value, or backend winner may silently replace
a Governance Binding.

### 4.10. Withdrawal

An established Governance Binding may also be explicitly withdrawn without immediately establishing a replacement.

```text
Binding(S, A)
        ↓
explicit Withdrawal
        ↓
no Governance Binding for later contract processing
```

Withdrawal removes applicability authority. It does not move machine State and does not mutate the previously selected
Policy World.

If Governance is required for later use of that Scope, absence of an applicable Binding prevents new affected contract
processing from being established. No hidden fallback World becomes authoritative until another valid Binding exists.
Withdrawal has no retroactive effect on processing or Whole Machine cooperation already proceeding under the withdrawn
Binding.

No timeout, process death, missing registry entry, configuration disappearance, or backend cleanup silently withdraws
Governance authority.

### 4.11. Validity, Singularity, and Complete Decision

An established Governance result must resolve exactly.

For one governed Scope at one applicable boundary:

```text
required Governance + no applicable Binding -> affected contract processing cannot begin
one exact Binding                            -> valid
competing valid decisions                    -> explicit Arbitration or invalid
ambiguous authoritative result               -> invalid
```

Where Governance is required, a missing applicable Binding does not establish a partial, fallback, or default governed
use.

The selected Policy World must exist, Scope must resolve explicitly, Decision Basis must be satisfied, and any required
Arbitration must resolve exactly. Governance may not repair missing or ambiguous material through a hidden default,
undeclared fallback, or runtime arrival order.

A Governance decision is authoritative only when all material declared as part of that decision is complete. Partial
establishment does not create partial Governance authority.

### 4.12. Exact Attribution

Every established Governance Binding must be exactly attributable to the canonical Governance material that established
it. At minimum, attribution must preserve:

- Governance identity and Version;
- the fixed Decision Basis, including the exact owning authority and semantic coordinate relation for each referenced
  source item;
- exact Scope;
- exact Selection;
- any applied Decision Arbitration result; and
- the resulting Binding, Replacement, or Withdrawal identity.

Governance owns this attribution requirement. Presentation, retention, audit storage, and diagnostic lifecycle remain
the responsibility of the existing Publication, Diagnostic Evidence, Diagnostic Retention, and Verification boundaries.

### 4.13. Scope Independence and Applicability Overlap

Governance Scopes are independent unless an explicit Contract says otherwise.

```text
Scope A -> Policy Emergency

Scope B -> ?
```

The Selection for Scope A does not imply, inherit, propagate, override, or constrain the Selection for Scope B merely
because the Scopes are structurally related, overlap, or belong to one Whole Machine. Structural containment does not
make a Binding for one Scope applicable to another Scope.

Structural overlap is allowed, but it creates no precedence. One exact Contract application must resolve to exactly one
applicable Governance Binding. If Bindings from distinct Scopes would simultaneously govern that same application, the
model is ambiguous and invalid.

If a valid Selection in Scope A requires, forbids, or constrains a Selection in Scope B, that cross-Scope relation must
be declared as an explicit Whole Machine Contract obligation outside Governance. Governance does not invent a transitive
propagation law.

### 4.14. Indivisible Governance Meaning Across Several Parts

Independent Governance Scopes remain independent. Contract-level atomicity is not created by trying to synchronize
separate Scope Bindings.

If several Policy-World selections must change as one indivisible Governance meaning, they must be represented by one
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

This law does not make a Binding for the encompassing Scope implicitly propagate into independently governed child
Scopes. The exact Scope and Selection themselves must declare the governed meaning. How a backend makes one such
decision visible across several physical components is implementation.

The exact Whole Machine Scope authoring form and canonical representation of a multi-part Selection remain deferred.

### 4.15. Separation from Policy and Other 1D Contracts

Policy owns the contents of a Contract World. Governance owns whether one declared World applies to an exact Scope.

When material established by a 1D Contract participates in Decision Basis, Governance consumes that established meaning
through its declared relation. It does not rerun the 1D judgment, reinterpret the source's obligation, or acquire the
source Contract's authority. The same established material may be used by other compiler or machine responsibilities
without being duplicated into Governance meaning.

A World change changes the 1D Contracts applicable to contract processing established after that change. Governance does
not perform a second compatibility, migration, or reconciliation judgment.

Material from an earlier World may enter a later flow. The currently applicable Input, Admission, Version, Invariant,
State, Budget, Capacity, and other 1D Contracts judge that material according to their own obligations. If every
applicable obligation is satisfied, processing continues. If one is not satisfied, the owning 1D Contract establishes
the corresponding failure.

Historical World provenance has no implicit meaning. If provenance itself matters, the appropriate 1D Contract must
declare and judge it explicitly.

### 4.16. Separation from the State-Machine Axis

Governance and the State Machine are independent Contract axes.

```text
Governance
    establishes which Policy World applies

State Machine
    establishes current State
    and legal Transitions within the applicable World
```

Governance does not set current State, execute a Transition, or use State-machine movement as an implicit side effect of
Selection, Arbitration, Binding, Replacement, or Withdrawal.

The State Machine does not automatically select, replace, withdraw, or arbitrate Governance because a particular State
was established.

If State-related meaning must affect Governance, Governance may use only explicitly established State-Machine material
that crosses the declared Decision Basis boundary. It does not reconstruct State from execution history, events, or
backend observation. If Governance-selected meaning must constrain later State-Machine judgment, that relation likewise
must cross an explicit Contract boundary and remain owned by the appropriate Contract Authorities.

### 4.17. Governance Is Not an Operation-Level Judgment Slot

Governance is not another ordinary pipeline judge beside Admission, Budget, Capacity, Invariant, State Machine, or
Publication.

The older conceptual form:

```text
Governance admits the contract world
    -> Permit / Refuse continuation
```

is withdrawn.

Governance establishes applicability before affected contract processing begins. That processing may fail to be
established because required Governance material is invalid, absent, withdrawn, or unresolved, but Governance is not a
generic per-operation `canContinue` Boolean.

Earlier ADRs that still present Governance as an ordinary per-interaction refusal authority require reconciliation.

### 4.18. Governance Version

Governance is version-sensitive under ADR-0053 because Decision Basis, Scope, Selection, Decision Arbitration, Binding,
Replacement, and Withdrawal are Contract meaning.

A Governance Version does not own Policy Versions.

```text
Governance / G2

Payment.Policy.Normal / N4
Payment.Policy.Emergency / E7
```

The Policy Versions identify the Policy Worlds. The Governance Version identifies the Governance law that controls their
applicability.

Changing a Policy does not automatically revise Governance. Changing Governance meaning does not rewrite Policy history.

Governance cannot dynamically select its own Version. Governance identity and Version are resolved before decisions
under that Governance Contract are established.

The exact canonical Governance definition cannot close until the frontend forms of Decision Basis, governed Scope,
Decision Arbitration, Binding, Replacement, and Withdrawal are finalized.

## 5. Frontend and Resolution

### 5.1. Policy World References

Governance refers to declared Policy Authorities and their resolved Contract Worlds.

Those references must resolve exactly within their authority and Version context.

A runtime string, configuration key, enum ordinal, object reference, generated class, service-registry entry, or source
nesting cannot become Policy identity merely because one Governance realization uses it.

The exact `.kontrakt` syntax for Governance is deferred.

### 5.2. No Governance-Owned World Declaration

Governance source must not contain a hidden duplicate of Policy World contents.

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

The World must already exist as Policy:

```text
policy Emergency {
    ... complete Contract World ...
}

governance Main {
    ... refers to Policy Emergency ...
}
```

Governance selects and binds a declared World; it does not define that World.

### 5.3. Scope Is Not Source Containment

The earlier Interface-local Governance syntax cannot remain a semantic shortcut for Scope.

If Governance syntax is physically nested inside an Interface, Core, or Whole Machine declaration, that containment may
be authoring structure only. The governed Scope must still be explicit Contract material.

This prevents source layout from acquiring Governance authority and allows the same Governance model to apply beyond one
Interface without hidden Scope inference.

### 5.4. Canonical Governance Material

Canonical Governance material must eventually preserve at least:

- Governance identity and Version;
- Decision Basis material and boundary law;
- the exact declared source authority and semantic coordinate relation for each Decision Basis item;
- Decision Law;
- exact governed Scope identity;
- exact selectable Policy World references;
- Selection law;
- Decision Arbitration law;
- Binding law;
- Replacement law;
- Withdrawal law;
- the one-decision fixed Decision Basis boundary law;
- the prohibition on using a Selection-dependent Contract to establish that same Selection;
- the prohibition on arbitrary source introspection, hidden source lookup, or recomputation of another authority's
  judgment inside Governance;
- the non-retroactive Binding continuity law;
- the one-applicable-Binding law for each exact Contract application;
- the one-scope complete-decision law for indivisible multi-part Governance meaning;
- the separation from Policy, the 1D Contracts, the State-Machine axis, and ordinary pipeline judgment slots; and
- the structural validity, singularity, persistence, attribution, non-propagation, and non-recursion requirements.

The exact byte-level identity and frontend forms remain deferred.

Backend representation remains replaceable after canonical meaning is fixed.

## 6. Verification

The final Governance verifier must prove that every referenced Policy World resolves exactly and that Governance cannot
construct or modify Policy World contents.

For every established Binding, it must verify one explicit Scope, one fixed Decision Basis, one valid user-declared
Decision Law, one exact Selection after any required Arbitration, and exact attribution to the canonical Governance
material that established it.

It must reject:

- implicit Scope inferred from source nesting, request contents, runtime topology, or implementation identity;
- implicit Selection based on declaration order, newest-Version assumptions, names, backend registry order, or
  discovery;
- missing, unknown, or ambiguous Policy World references;
- missing or incomplete Decision Basis material required by the declared Decision Law;
- Decision Basis references to undeclared source coordinates, arbitrary source internals, or material whose owning
  authority cannot be resolved exactly;
- attempts to recompute or re-establish another Contract or State-Machine judgment inside Governance instead of
  referencing the already-established source material;
- realization observation used as Governance Authority without an explicit declared material boundary that is
  independently established before the Selection it helps determine;
- attempts to extend, replace, or alter the fixed Decision Basis of an already-established Governance decision with
  material established after its decision boundary;
- Decision Laws that use arbitrary user algorithms, callbacks, hidden runtime queries, implementation lookup, backend
  control flow, physical race order, or undeclared source inspection instead of finite explicit lowerable conditions
  over the fixed declared Decision Basis;
- prerequisite material whose authority exists only after the same Selection that the material is used to establish;
- competing valid decisions without explicit deterministic Arbitration;
- more than one applicable Binding for one exact Contract application;
- required-Governance application that attempts to begin without one applicable Binding;
- partial establishment of one declared Governance decision;
- attempts to obtain indivisible Governance meaning by synchronizing independent Scope Bindings instead of declaring one
  complete decision over one exact encompassing Scope;
- hidden fallback, precedence, expiry, Replacement, or Withdrawal rules;
- implicit propagation, inheritance, containment precedence, or override from one Governance Scope to another;
- Governance rules that establish State or execute State Transitions;
- State-Machine rules that implicitly select, arbitrate, replace, or withdraw Governance;
- Governance rules that waive or partially disable an active Policy World instead of selecting another declared World;
- silent Binding expiry or Withdrawal caused only by runtime lifecycle;
- retroactive Replacement or Withdrawal of applicability already established for ongoing processing or Whole Machine
  cooperation;
- Governance that dynamically selects, replaces, withdraws, or arbitrates between Governance Contracts;
- Governance that dynamically selects its own Version;
- cross-World compatibility logic that belongs to the 1D Contracts of the newly applicable World; and
- recursive modeling that aliases Governance Decision Basis, Decision Law, or Binding to ordinary pipeline Admission,
  Invariant, Publication, or another 1D authority.

If a relation between two Governance Scopes is required, the compiler must verify the explicit Whole Machine Contract
that owns that relation rather than inventing a Governance compatibility or propagation matrix.

The same canonical Governance material must produce the same authoritative Binding regardless of source acquisition
order or backend representation.

Physical races, synchronization, storage, transport, and distributed realization remain implementation concerns. Their
realization must preserve the verified Governance meaning but does not extend this Contract Authority.

## 7. Contract and Implementation Boundary

Governance owns Decision Basis, Decision Law, Scope, Selection, Decision Arbitration, Binding, Replacement, and
Withdrawal meaning. It does not own the physical mechanism that observes, stores, transports, synchronizes, or applies
that meaning.

Governance Contract judgment and Governance realization judgment are distinct.

```text
Governance Contract judgment
    explicit request + explicit situational material
        -> user-declared Decision Law
        -> declared Policy-World response

Governance realization judgment
    already-established Governance decision
        -> can the backend realize that exact meaning correctly and deterministically?
```

A realization check may determine that an established Governance decision cannot yet be faithfully realized. It cannot
replace that decision with another Policy World, weaken its conditions, or reinterpret the Decision Law. Failure to
realize the declared result is a realization problem, not a new Governance Selection.

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
supply material toward Governance. The carrier or producer does not become Governance Authority. Material used in
Decision Basis must cross an explicit declared boundary, preserve the authority that owns its meaning, and be fixed
independently of the Selection it helps determine.

Governance does not own the mechanisms that acquire observations, poll sensors, calculate backend health, prepare
images, coordinate runtime participants, retry, fail over, schedule work, balance load, or recover execution. If a
machine situation is established by another Contract Authority or by the State-Machine axis, Governance may reference
that established material without duplicating the underlying judgment. If realization-originated information is needed,
realization must provide it through the explicit material boundary required by Decision Basis rather than exposing raw
observation or backend state as Governance Authority. How realization obtains and faithfully supplies that material
remains outside Governance Contract Authority. Backend preparation and publication readiness are realization conditions
even when they determine when an already-established decision can be applied.

Approval workflow, operator screens, interlock hardware, audit stores, clocks, and timeout mechanisms are likewise not
Governance Authority. Governance uses only material explicitly admitted by Decision Basis and crossing its declared
boundary.

The backend may specialize Policy-World lookup, precompute Bindings, use compact canonical identities, or optimize
Replacement. It may also retain the resolved Binding in internal execution or cooperation context so that participating
pipeline flows do not re-resolve Governance after a Replacement. Such state is realization only: it must not be exposed
as required user Input, Output, Fact, Operation parameters, or user-managed Governance handles.

Those mechanisms must preserve the same canonical Governance meaning.

## 8. Relationship to Whole Machine and Concurrency

ADR-0055 establishes the relevant Whole Machine flow boundary.

Independent Contract pipelines flow one way. One admitted Input establishes one flow. That flow is not altered by later
external material, and Whole Machine composition adds no Contract-level waiting, joining, buffering, or synchronization
mechanism.

At the Whole Machine boundary, Governance establishes the Policy World that applies when cooperation begins rather than
coordinating pipeline execution. That World supplies the 1D Contracts used by the participating Contract flows.

If one Whole Machine cooperation proceeds through several independent flows such as `A -> B -> C -> D`, a Governance
Replacement between `B` and `C` must not cause `C` to re-enter under a different World. The cooperation already
proceeding under the earlier Binding remains under that Binding; cooperation established after the Replacement observes
the new Binding. Kontrakt realizes this continuity internally. It is not a user-authored carrier, pipeline coordinate,
or new Contract axis.

Material published by a completed earlier cooperation becomes ordinary external material when presented to later
processing. A later World does not need a separate Governance compatibility or migration rule for that material. Its 1D
Contracts accept or reject the material by their own obligations. Historical Governance provenance matters only when
some 1D Contract declares that provenance as explicit Contract meaning.

Several Governance Scopes may belong to one Whole Machine, but no Scope automatically controls another. Cross-Scope
selection requirements, if any, must be expressed by explicit Whole Machine Contract obligations outside Governance. If
several changes are one indivisible Governance meaning, they must instead be declared as one complete Governance
decision over one exact encompassing Scope.

A Governance Replacement or Withdrawal and the start of new contract processing may be concurrent in one realization.
Several Governance decisions may also compete for the same exact Scope. The Contract rules are that explicit Decision
Arbitration resolves any permitted competition, one exact Binding applies, and later changes do not retroactively
rewrite processing or Whole Machine cooperation already proceeding under that Binding. Locking, CAS, epochs,
transactions, message order, scheduler order, distributed consensus, internal execution context, or another mechanism
used to preserve those rules belongs to implementation.

Distributed realization may make Governance difficult to implement, but those threats and their responses are not new
Governance obligations.

### 8.1. Backend World-Replacement Problem

A naive backend can preserve this ADR by stopping new cooperation, waiting for every cooperation under the previous
Binding to finish, publishing the replacement, and then admitting work again. That drain-and-switch strategy is a valid
correctness baseline, but it is effectively stop-the-world at the governed Whole Machine boundary and is not the target
realization.

The backend must instead be designed so that a replacement can become available without splitting already-proceeding
cooperation across old and new Contract Worlds. The implementation problem is therefore a deterministic multi-version
publication problem, not a new Governance Contract obligation.

These checks do not participate in Governance Decision Law. They determine only whether the backend can faithfully
realize an already-established Governance response. A backend that is not ready to publish the requested World must not
silently substitute another World or turn implementation readiness into hidden Governance meaning.

Earlier Kontrakt backend work contains several relevant implementation precedents:

- the pre-contract-theory runtime-policy design pins one immutable resolved snapshot for one logical run and permits a
  newer snapshot only for later runs;
- `FrozenMetamodelImage` and `ContractImage` use validate-before-publication and immutable-image boundaries;
- ADR-0041 separates exact canonical identity from digest and compact routing identity, so a hash cannot silently become
  semantic equality authority; and
- ADR-0040 keeps query/incremental computation as a compatible future direction rather than mixing it into the current
  frozen foundation.

These mechanisms predate the current Contract theory and are implementation precedents only. They do not constrain the
backend architecture required by this ADR. The later backend design may reuse, revise, or replace them after deriving
its requirements from the current Contract model. What must survive is the deterministic semantic requirement: one
already-proceeding cooperation is not torn across different applicable Contract Worlds.

The later design should avoid creating unnecessary parallel identity, image, or publication machinery where an existing
mechanism still satisfies the new requirements, but reuse is not presumed. Any implementation-local publication revision
must remain distinct from ADR-0053 Contract Version and must not be conflated with historical runtime, run, backing, or
backend-session epoch identities merely because those identities already exist.

Candidate implementation families for that design include:

```text
immutable multi-version image publication
    old cooperation remains pinned to the old complete image
    later cooperation observes the newly published complete image

RCU-style grace-period reclamation
    old images remain readable until no earlier cooperation can still observe them

MVCC-style snapshot pinning
    several immutable world revisions may remain live while different cooperation completes

prepare-then-publish consistent update
    distributed participants prepare the next complete revision before one publication boundary admits it

query / incremental rebuilding with structural sharing
    only affected derived material is recomputed while unchanged immutable image material is reused
```

These names identify implementation candidates only. This ADR does not select an algorithm, memory-reclamation scheme,
publication primitive, distributed protocol, internal revision representation, or structural-sharing layout. The later
backend design must compare them against Kontrakt's existing frozen, planning, identity, cache, and publication
machinery with determinism as the first acceptance criterion. Completion order, thread winner, node arrival order, hash
iteration, or another physical race must never determine which Contract World one cooperation observes.

## 9. Contract and Implementation Decisions

### 9.1. Decisions Made Here

This ADR decides the following boundary:

- Policy declares Contract Worlds; Governance controls their applicability.
- Governance itself is the Contract Authority for Governance decisions and cannot dynamically govern Governance.
- Governance has eight responsibilities: Decision Basis, Decision Law, Scope, Selection, Decision Arbitration, Binding,
  Replacement, and Withdrawal.
- Decision Basis contains explicit declared material. Material already established by another Contract Authority or the
  State-Machine axis remains owned by that authority, and realization-originated material may participate only through
  an explicit declared boundary. Governance does not rediscover, recompute, or introspect source meaning merely to use
  it as Decision Basis.
- Decision Law is the user-declared finite, deterministic, lowerable law that judges the fixed declared Basis and
  determines the Governance response.
- Governance Contract judgment is separate from backend realization judgment; realization readiness cannot select a
  different Policy World or alter the declared Governance response.
- Scope is explicit Contract material; structural containment creates neither Scope nor precedence.
- one exact Contract application resolves to one applicable Binding.
- an established Binding persists until explicit Replacement or Withdrawal, and Replacement is non-retroactive to
  processing or Whole Machine cooperation already proceeding under the previous Binding.
- indivisible multi-part Governance meaning is one complete decision over one exact encompassing Scope, not synchronized
  independent Scope Bindings.
- Governance does not alter Policy contents, move State, or perform cross-World compatibility judgments owned by the
  selected World's 1D Contracts.
- implementation mechanisms may realize Governance but cannot own its meaning.
- Governance is version-sensitive under ADR-0053, while its own Version is resolved before Governance decisions begin.

### 9.2. Decisions Not Made Here

This ADR does not yet decide:

- the exact set of allowed governed Scope kinds and how applicability is derived for each kind without introducing
  user-managed Binding or execution material;
- the exact set of admissible Decision Basis source kinds, the stable semantic coordinates each source may expose to
  Governance, and the frontend representation of those references and the finite lowerable Governance Decision Law;
- the exact canonical syntax for Selection, Decision Arbitration, Binding, Replacement, and Withdrawal;
- the exact Whole Machine Scope authoring and canonical representation for one indivisible multi-part Governance
  decision;
- when Governance is required, optional, or explicitly absent for an exact Scope;
- the public Governance control API;
- failure and Diagnostic Evidence presentation; or
- canonical identity bytes beyond ADR-0053 authority and Version requirements; and
- the backend design for deterministic non-stop-the-world World publication, pinning, distributed preparation, and old
  image reclamation, including which historical runtime-policy, frozen-image, ContractImage, identity, and future
  query/incremental mechanisms should be reused, revised, or replaced under the current Contract theory.

These questions must be settled before this ADR becomes Accepted.

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

The Governance model must provide enough canonical material to identify one exact Decision Basis, including the exact
source authority and semantic coordinate relation of every referenced Basis item, and one exact Scope, Selection,
Decision Arbitration result, Binding, Replacement, and Withdrawal result where each is applicable.

Where the Contract theory intentionally permits several equivalent physical realizations, that freedom belongs to
implementation rather than to ambiguous Governance meaning.

## 11. Deferred Decisions

The following Governance questions remain open:

1. Which exact Contract Scope kinds may be governed: Interface, Core, Whole Machine, Flow, or another explicitly defined
   Scope, and how is applicability derived for each allowed kind without exposing internal Binding continuity to users?
2. What canonical material identifies one governed Scope without relying on source containment or runtime topology?
3. Which exact established source kinds and semantic coordinates may participate in Decision Basis, how are those
   references declared without exposing arbitrary source internals, and what finite, explicit, deterministic, lowerable
   Decision Law can judge them without introducing a general-purpose controller programming language?
4. What finite Decision Arbitration forms are permitted: conflict rejection, explicit precedence, or another closed law?
5. What exact frontend form declares Selection, Binding, Replacement, and Withdrawal without duplicating Policy
   contents?
6. What exact canonical material identifies one complete Governance decision and its attribution?
7. What exact Whole Machine Scope authoring and canonical Selection form represent one indivisible multi-part Governance
   decision when several Policy-World selections must form one meaning?
8. Which compiler or frontend mechanisms may be reused from ordinary Contract pipeline processing without aliasing
   Governance Decision Basis, Decision Law, or Binding to Admission, Invariant, Publication, or another semantic
   Contract authority?
9. When is Governance required, optional, or explicitly absent for an exact Scope, including Scopes with zero or one
   selectable Policy World, and how is absence represented without a hidden default Binding?
10. What failure and Diagnostic Evidence are established when Governance material is missing, unknown, incomplete,
    withdrawn, or ambiguous?
11. What public control API exposes Governance while keeping transport and realization outside Contract Authority?
12. What canonical identity material is required beyond ADR-0053 Version identity?
13. Which deterministic multi-version publication and reclamation design should realize non-retroactive World
    Replacement without stop-the-world draining, and which historical image, epoch, canonical identity, and
    query/incremental mechanisms should be retained, revised, or replaced without conflating their identities?

Whole Machine execution coordination, pipeline waiting, distributed synchronization, retry, failover, and runtime
replacement mechanisms are not deferred Governance semantics. They remain realization concerns under ADR-0055 and later
backend implementation design.

## 12. Consequences

### Positive

Policy and Governance retain distinct responsibilities. Policy declares operating Contract Worlds; Governance
establishes which declared World applies to an explicit Scope.

Governance has enough structure to express practical situational response without becoming a general controller.
Decision Basis can reuse explicitly established machine material without transferring the source authority into
Governance or requiring Governance to rediscover the same condition. The user-declared finite Decision Law judges only
that fixed declared Basis, competing decisions require explicit Arbitration, and established Bindings cannot silently
appear, expire, or change through backend accidents.

Scope is no longer tied to Interface source nesting. Larger Scopes can be considered without creating hierarchy,
inheritance, or implicit propagation between Governance Scopes.

Replacement does not split processing or Whole Machine cooperation already proceeding under one Binding across different
Contract Worlds. The backend preserves that Binding internally, while cooperation established after Replacement observes
the new World. Once later processing legitimately begins under that World, its 1D Contracts judge material themselves.
This avoids both Contract-World tearing and a separate cross-World compatibility or migration authority inside
Governance.

The State-Machine axis remains independent, and Whole Machine execution coordination remains outside Governance.

### Negative

Governance remains Proposed because the exact allowed Scope kinds, admissible Decision Basis source kinds and semantic
coordinates, Decision Basis and Decision Law frontend forms, permitted Arbitration laws, frontend syntax, and canonical
Binding/Replacement/Withdrawal material are still open.

Several earlier ADRs and `What Contract Is` passages still describe Governance as an operation-level refusal authority,
as the owner of Manifest Worlds, or as an Interface-local selector. Those references require reconciliation after
ADR-0056 settles.

The frontend can no longer rely on Interface nesting alone to define Governance Scope.

### Neutral

This ADR does not require a distributed system, control plane, leader election protocol, transaction manager, actor
system, monitor, or any particular synchronization primitive.

It does not require Governance to discover machine conditions or implement an automatic controller. Situational material
used by Governance must be explicit and must preserve the authority that established its meaning, while observation and
acquisition mechanisms remain replaceable realization. Governance itself remains the authority that judges the fixed
declared Decision Basis under the declared Decision Law and establishes Policy-World applicability.