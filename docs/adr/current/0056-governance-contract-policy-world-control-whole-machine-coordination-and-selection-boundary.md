# ADR-0056: Governance Contract, Policy-World Control, Whole-Machine Scope, and Selection Boundary

## Status

Proposed

## Date

2026-08-11

## Related

- `../../the-most-important-thing/what-contract-is.md`
- `../../todo/kontrakt-verifier-implementation-plan.md`
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
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
- `../../design/planner-budget-resolution-and-worker-lifecycle.md`

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

Those worlds can exist as explicit Contract meaning without deciding which one governs an explicit Scope.

Governance owns that Policy-World Binding problem. A single-part Scope may bind one World, while an explicit multi-part
Scope may bind one complete arrangement of exact Policy Worlds. ADR-0063 supplies the common semantic laws for
established material used by Governance and for later use of Governance results.

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

A Governance change therefore does not need a separate implicit compatibility controller. The applicable Binding
identifies the complete Policy-World arrangement for later governed processing, while each selected 1D Contract accepts
or rejects material under its own authority. Any larger coherence requirement remains an explicit obligation of the
authority that owns that composition.

This ADR establishes the minimum Governance authority needed above that flow model.

## 2. Problem

Once several Policy Worlds exist, the machine needs explicit authority over which complete Policy-World arrangement
applies to an exact governed Scope.

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
execution. It establishes the Governance Binding that determines which complete declared Policy-World arrangement
governs an explicit boundary.

That requires nine explicit responsibilities:

```text
Decision Basis         what exact Established Material one decision judges
Decision Law           how that complete Basis establishes one explicit Decision Outcome
Scope                  what exact governed domain the decision applies to
Selection              which complete declared Policy-World arrangement the response carries
Decision Arbitration   how different-response Direct Governance Overlap is explicitly resolved
Binding                which complete Selection gains governing authority for that Scope
Replacement            how one exact Binding becomes the predecessor of a successor Binding
Withdrawal             how one exact Binding receives explicit termination meaning without a successor
Binding Transition     where later Binding applicability changes without rewriting earlier applications
```

Governance itself is the Contract Authority for those decisions. External actors, other Contracts, and runtime
mechanisms may carry explicit material toward Governance, but they do not become Governance Authority by doing so.

> Governance is the Contract Authority that establishes and controls which complete declared Policy-World arrangement
> is bound to an explicit Scope.

Policy still owns every selected World. The 1D Contracts contained in each selected World remain solely responsible for
their own obligations.

## 3. Decision Drivers

Governance must remain separate from Policy and from the 1D Contracts inside a selected World.

```text
Policy
    declares one operating Contract World

Governance
    establishes whether that World or complete World arrangement applies to an explicit Scope

selected 1D Contracts
    judge their own obligations
```

Governance Scope cannot be derived from source nesting, runtime topology, request contents, implementation identity, or
backend discovery. If Scope is Contract meaning, its identity must be explicit.

A Governance decision must have an explicit Decision Basis and an explicit Decision Law. Governance declares the meaning
required by that Basis; it does not name the Contract that must produce it. How source-owned material is established,
connected, resolved, and found applicable follows ADR-0063. Governance neither reconstructs the source judgment nor
acquires its authority.

Raw realization observation cannot satisfy Governance Decision Basis merely because the backend can observe it. The
Decision Law judges only declared semantic material. Governance does not infer hidden machine conditions, inspect
another authority's internal representation, or obtain undeclared material through runtime lookup.

Contract judgment and realization judgment are separate. The Governance Contract decides which Policy World should
govern under the explicit request and situation. Kontrakt and its backend separately decide how to realize that
already-established meaning correctly and deterministically. Backend readiness cannot silently alter the Governance
decision or select another World.

Several otherwise-valid Governance decisions may coexist. Hidden runtime order cannot decide whether they conflict or
which response governs. Direct Governance Overlap exists only where their established Scopes include the same exact
governed application. Same-response overlap is compatible; different-response overlap must resolve through the explicit
deterministic Decision Arbitration law before the affected Governance result is bound.

Different Governance Scopes have no implicit hierarchy, inheritance, propagation, or override relation. Structural
relation and graph connectivity create no precedence. Disjoint Governance domains create no Direct Governance Overlap,
while any separately required higher-scope coherence remains the responsibility of the authority that owns that
composition.

Governance authoring and semantic expression remain flat. Exact semantic material and exact direct relations may, when
viewed together, form a graph and the semantic-establishment subset is acyclic, but graph shape, traversal, recursive
reference following, source containment, and higher-order Contract invocation do not establish Governance meaning.
Derived dependency, invalidation, scheduling, or diagnostic graphs remain compiler or subsystem knowledge.

An established Binding is immutable. Replacement and Withdrawal establish later Governance meaning without rewriting
that Binding, while Binding Transition establishes the semantic cut at which later applications may acquire different
Binding applicability. For one exact Binding, competing succession or termination authority cannot establish, and for
one exact Replacement or Withdrawal, competing Transition cuts cannot establish. Once one exact governed application has
acquired an applicable Binding, later Governance change does not replace that Binding inside the same application. How a
backend preserves that continuity is realization. The common non-retroactive law remains owned by ADR-0063.

Governance must remain separate from the State-Machine axis. Governance establishes Policy-World Binding; it does not
establish a State or execute a Transition.

Governance must not derive authority from backend mechanisms. Lock order, CAS order, scheduler order, process topology,
service discovery, configuration storage, runtime object identity, and similar realization details cannot decide
Governance meaning.

Governance follows ADR-0063's semantic determinism law. Equivalent Governance meaning must establish the same
authoritative result regardless of source acquisition order, thread timing, hash iteration, or backend representation.

## 4. Contract Decision

### 4.1. Governance Authority

Governance is the Contract Authority that establishes which already-declared Policy-World arrangement is bound to an
explicit governed Scope.

Its minimum semantic progression is:

```text
Decision Basis
        ↓
Decision Law
        ↓
Governance Decision
    Complete Selection | No Selection
        ↓
Direct Governance Overlap
        ↓
Decision Arbitration, when different responses conflict
    Resolved Selection | No Resolution
        ↓
Governance Binding
        ↓
Replacement or Withdrawal, when explicitly established
        ↓
Binding Transition
        ↓
later Binding Applicability under ADR-0063
```

Governance does not create, edit, inherit, synthesize, waive, or partially override the Policy World it selects.

Policy Worlds are the direct governed subjects of this Contract. Governance does not directly control Admission, Budget,
Capacity, State Machine, Invariant, Version, Publication, or another 1D Contract. A Binding gives one already-declared
Policy-World arrangement Governance authority for its exact governed Scope. Each selected 1D Contract keeps its own
authority when dependent processing later uses that Binding.

The shape of Governance decision processing may resemble ordinary Contract pipeline processing, but Governance
responsibilities are not aliases for Input, Admission, Invariant, Publication, or another 1D Contract authority.
Decision Basis and Decision Law are not Admission or Invariant, and Binding is not Publication. Governance must not
become a recursive ordinary Contract Pipeline merely to reuse those semantic names. Compiler and frontend machinery may
reuse resolution, finite-set checking, canonicalization, or deterministic lowering only where the authority meaning
remains unchanged.

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

A Governance decision judges one declared Decision Basis.

Decision Basis defines the meaning required by that Governance judgment. It may require any explicit semantic meaning
that can legally participate as Established Material under ADR-0063. Governance does not define a closed ontology of
request, authorization, readiness, inhibition, State, Capacity, or other producer categories. It declares the meaning it
needs, not the authority that must produce that meaning.

Conceptually:

```text
required Governance meaning
    EmergencyRequested
    ThermalCritical
    CoolingUnavailable
    MaintenanceActive
        ↓
ADR-0063 basis and applicability laws
        ↓
complete applicable Decision Basis
        ↓
Governance Decision Law
```

ADR-0063 owns Establishment, source reference, Basis Resolution, Applicability, and complete-basis law. Governance uses
the material resolved under those laws without choosing a producer, reconstructing the source judgment, or acquiring the
source authority.

The names above stand for semantic meaning, not for sensors, callbacks, logs, backend probes, or producer identities.
Raw realization observation cannot directly satisfy Decision Basis.

One Governance decision uses one complete applicable Decision Basis. Material established later does not rewrite that
earlier Governance occurrence; it may participate in another Governance decision when valid for that later application.
This semantic boundary does not require an epoch, snapshot, lock, or storage mechanism.

After Composition resolves the declared source connections, the resulting establishment relation must be acyclic. A
Decision cannot require material whose establishment requires that same Decision, its Selection, its Binding, or an
indirect result that returns to the same not-yet-established judgment. An already-established earlier occurrence may be
explicit basis for a later judgment because that relation is succession rather than circular establishment.

**Open in this section:** the exact frontend form for declaring required Decision Basis meaning remains to be designed.
It must preserve explicit source-independent meaning and must not encode producer topology.

### 4.4. Decision Law

Decision Law is the user-declared Governance law that judges one complete applicable Decision Basis and establishes one
explicit Governance Decision Outcome.

```text
complete applicable Decision Basis
        ↓
Governance Decision Law
        ↓
exactly one Decision Outcome

Decision Outcome
    Complete Selection
    or
    No Selection
```

`No Selection` is an explicit Governance judgment outcome. It is not `null`, missing evaluation, failed evaluation,
unresolved conflict, implicit fallback, retention of an existing Binding, Withdrawal, or another hidden meaning. When a
complete applicable Basis is validly judged and no declared Selection applies, the Decision establishes `No Selection`
rather than disappearing.

A Governance Decision is Governance-owned Established Occurrence Material under ADR-0063. It is established when one
exact Decision Law judges one complete applicable Decision Basis for one exact governed application and determines
exactly one Decision Outcome. Incomplete Basis, an illegal Selection, or ambiguous distinct Outcomes establish no
Governance Decision and gain no partial authority.

The Decision Law is deterministic. The same established Governance definition, the same Decision Law meaning, the same
exact governed application meaning where occurrence distinction matters, and the same complete applicable Basis must
establish the same Outcome. The result is closed over the declared Decision Basis. Hidden source discovery, backend
state, callbacks, implementation lookup, scheduler state, or another undeclared path cannot affect the judgment.

Several declared law elements may be satisfied by the same Basis. If they all determine the same Outcome, they establish
one Governance Decision and every determining law element remains exactly attributable to that judgment. If they
determine distinct Outcomes, the Decision Law is ambiguous and no Decision is established. Declaration order, evaluation
order, source discovery order, worker completion order, physical arrival order, and backend iteration order carry no
Governance precedence.

Several independently complete Decision Basis applications may establish their own Governance Decisions. That is not an
ambiguity in one Decision Law. Whether those Decisions interact depends on the governed Scope and is handled by Direct
Governance Overlap and Decision Arbitration.

The Decision Law may express only finite, explicit, deterministic, and lowerable relations over declared semantic
meaning. It is not a general controller programming surface. Contract inheritance, higher-order Contract indirection,
recursive Contract expansion, fixed-point evaluation, arbitrary user algorithms, hidden runtime queries, and backend
control flow cannot be used to discover Governance meaning. A circular establishment relation is illegal Contract
semantics even if a compiler implementation could iterate to a fixed point.

Governance Contract judgment ends with the Decision Outcome. Backend preparation, distributed publication, retention,
reclamation, or realization readiness does not change that Outcome.

**Open in this section:** the exact finite Decision Law language and frontend form remain to be designed. Independently
attributable law elements must receive stable Governance-version-local semantic coordinates that do not depend on
declaration order or compiler generation.

### 4.5. Governance Scope

Every Governance decision acts over one explicit governed Scope. Scope is one exact governed-domain semantic coordinate
under one resolved Governance Authority and Version. It is not a universal `Scope Contract`, runtime filter, source
container, semantic parent, or current member list.

The earlier rule:

```text
Governance selection scope = exactly one Interface
```

is withdrawn.

A Scope may use an already-established semantic domain. In that case the original authority continues to own the domain
identity and membership, while Governance establishes only the explicit relation that this exact domain is the governed
coordinate for its Governance meaning. Governance does not re-establish, contain, inherit, or copy the contents of an
Interface, Core, Whole Machine, or another existing domain.

Governance may instead declare one explicit grouping when several established semantic subjects must form one
indivisible Governance meaning and no earlier authority owns that grouping. That grouping receives one exact Scope
semantic coordinate under the Governance Authority and Version. This does not create a separate universal Scope
authority or `ScopeMembership` Contract.

Scope identity remains distinct from expanded membership.

```text
Scope identity
    !=
expanded member set
```

Two Scope coordinates do not become the same merely because their current members coincide. Compiler-expanded membership
is derived knowledge and may be cached, indexed, or recomputed without becoming new Contract authority.

The membership used by Governance must come from established semantic meaning. Source nesting, call structure, runtime
reachability, process placement, object ownership, current machine conditions, physical adjacency, or overlap graph
connectivity cannot create membership. Dynamic situation changes belong in Decision Basis rather than Scope membership.

For the same established domain meaning, the same Governance Scope coordinate, the same relevant composition, and the
same exact governed application, membership is deterministic and complete. Unresolved membership does not become
`outside`; the material required for that use is incomplete.

Governance defines no closed enum such as `InterfaceScope`, `CoreScope`, `WholeMachineScope`, `FlowScope`, or
`RunScope`. An Interface, Core, Whole Machine, or future semantic domain may be used when it already exists as an exact
established domain. A flow, run, session, operation, process, thread, deployment, or runtime object does not become
Governance Scope merely because execution occurs through it. If a future Contract authority separately establishes such
a semantic domain, its admissibility follows the same law rather than a name-based exception.

Under the current Policy law, an Operation also cannot be used to split one Interface-complete Policy World into
independently activated operation-level Worlds. If future Policy semantics introduces such a domain, that change must be
owned and versioned by Policy rather than inferred by Governance from operation execution.

Direct Governance Overlap is judged over established Scope meaning and does not establish a new intersection Scope. A
multi-part Governance meaning may explicitly declare one encompassing Scope when its parts must form one complete
arrangement. Graph connectivity, repeated co-occurrence, or overlap among independent Scopes cannot infer that grouping.

**Open in this section:** the exact frontend form for using existing semantic domains and declaring Governance-owned
grouping coordinates remains to be designed.

### 4.6. Selection

Selection is the exact Policy-World response coordinate carried by an established Governance Decision or Arbitration
Judgment. It is not an independently established Contract Authority, occurrence type, Version, or activation event.

A Selection is one complete declared Policy-World arrangement for its governed Scope. A single-part Selection is the
one-entry case. An explicit multi-part Scope may select one exact Policy World for each distinct Policy-bearing part.

```text
single-part
    Payment -> Emergency / E7

multi-part
    Payment   -> Emergency / E7
    Inventory -> Restricted / R3
    Shipping  -> Emergency / E7
```

Each selected target is an exact resolved Policy World Definition Reference. Arrangement order has no semantic meaning.
Declaration order, filesystem order, newest Version, registry order, source proximity, `latest`, `current`, or another
floating lookup cannot choose a Selection.

Each governed part has exactly one Policy World inside one complete Selection. If several Policy meanings must coexist
for the same exact part, Policy must already declare the combined meaning as its own Policy World. Governance and the
compiler cannot synthesize, merge, waive, or partially override Policy contents.

A multi-part Selection must be complete for the parts required by its encompassing Scope. A partial arrangement does not
establish partial Governance Decision authority.

Selection alone does not make the arrangement governing. Binding owns that later relation.

Several independent Governance Decisions may carry the same exact Selection. Their judgment identities remain distinct;
Selection equality does not collapse the Decisions that established it.

**Open in this section:** the exact Selection frontend form remains to be designed. Byte-level canonical representation
of its order-independent arrangement belongs to Section 5.4.

### 4.7. Decision Arbitration

Decision Arbitration is the Governance-owned resolution law for different-response Direct Governance Overlap. Disjoint
governed domains do not compete, and same-response overlap is compatible concurrence rather than a response conflict. No
Arbitration Judgment is synthesized merely to record compatible concurrence.

A conflict exists when otherwise-applicable Governance Decisions govern the same exact governed application and carry
different complete Selections. Those Decisions establish their own judgment meaning first. They do not become
contradictory Bindings and get repaired later.

Arbitration judges the complete unordered set of exact competing Governance Decision occurrences for one resolution
boundary. Its input is not reduced to Policy World names because distinct Decisions retain distinct authority and
attribution even when some selected targets happen to coincide.

The default resolution boundary is one exact governed application. Overlap graph connectivity does not enlarge it. An
explicit indivisible multi-part Governance meaning expands the boundary to its encompassing Scope, because that declared
arrangement is itself one semantic unit. Physical coupling or graph connectivity cannot infer such expansion.

Arbitration is not a pairwise fold. The same complete conflict set under the same Arbitration Law must establish the
same result regardless of declaration, discovery, arrival, worker-completion, grouping, or iteration order.

A Governance Arbitration Judgment is Governance-owned Established Occurrence Material. It is established when one exact
Arbitration Law judges one complete unordered conflict set for one exact resolution boundary and determines exactly one
Arbitration Outcome.

```text
Arbitration Outcome
    Resolved Selection
    or
    No Resolution
```

`No Resolution` is an explicit authoritative outcome. It is not `null`, absence of Arbitration, Failure, fallback,
Withdrawal, or an implicit `No Binding` decision. An incomplete conflict set, ambiguous distinct outcomes, or an illegal
or incomplete Selection establishes no Arbitration Judgment.

Several Arbitration-law elements may determine the same Outcome. They establish one Arbitration Judgment with plural
exact attribution. Distinct Outcomes from the same complete conflict set are ambiguity and therefore invalid.

A Resolved Selection may be one competing Selection or another already-declared complete Policy-World arrangement. If a
combined World is required for one exact governed part, that World must already be declared by Policy. Arbitration never
creates Policy meaning.

A Resolved Arbitration Judgment still does not establish governing authority. Binding owns that later meaning. `No
Resolution` cannot establish a Binding for the affected boundary.

**Open in this section:** the exact finite Arbitration language and frontend form remain to be designed.

### 4.8. Binding

Governance Binding is Governance-owned Established Occurrence Material. It establishes one complete Selection as the
governing Policy-World arrangement for one exact Governance Scope.

```text
exact Scope
+
complete effective Selection
+
complete determining Governance judgment
        ↓
Governance Binding
```

A Binding is a positive governing relation. `No Selection` and `No Resolution` do not become `null Binding`, synthetic
`NoBinding`, implicit retention, Withdrawal, or fallback. Whether another Contract application requires a Binding is
owned by that dependent law under ADR-0063.

Binding establishment requires a complete effective Selection. One Decision may supply it directly. Several
same-response Decisions may support the same effective Selection and then converge into one Binding while every
determining Decision remains attributed. If different-response overlap exists, one Resolved Arbitration Judgment must
first establish the effective Selection. Contradictory or partial Bindings are never established and repaired later.

Binding establishment and Binding Applicability are separate. Establishing Binding `B` does not mean every current or
future application has already acquired `B`, does not perform a runtime switch, and does not create a global mutable
`current Binding`. A dependent semantic application establishes its own use of one exact applicable Binding under
ADR-0063.

A Binding is immutable. A later Policy, Governance Version, Decision, Replacement, Withdrawal, or Transition does not
rewrite its Scope, Selection, determining judgment relation, or historical meaning.

Binding establishment does not silently establish higher-scope coherence. If several governed applications must satisfy
a larger compatibility obligation, that obligation must be explicitly declared by the authority that owns the larger
composition.

**Open in this section:** the exact Binding frontend form, byte-level canonical shape, and compact occurrence-reference
representation remain to be designed.

### 4.9. Replacement

Replacement is Governance-owned Established Occurrence Material that establishes exact Binding succession. It relates
one exact already-established predecessor Binding to one exact already-established successor Binding for the same
governed Scope.

```text
Binding B1 established
Binding B2 established

explicit Replacement R
    predecessor = B1
    successor   = B2

succession:
    B1 -> B2
```

A new Binding does not replace an earlier Binding merely because it is newer, visible, loaded, or available. Replacement
must establish the exact predecessor-successor relation in semantic meaning. `current`, `latest`, timestamp order,
registry state, physical completion order, or backend winner cannot determine the predecessor or successor.

Replacement does not establish B2 and does not mutate B1 or B2. B2 first has its own complete Binding meaning. The
Replacement occurrence then establishes only the complete succession relation from B1 to B2. A successor Binding without
an Established Replacement has no predecessor lineage, and a proposed lineage without two legal established Bindings
gains no partial Replacement authority.

Replacement is about Binding succession rather than Policy-World inequality. B2 may carry the same exact Selection as B1
and still be a distinct successor when its Governance occurrence meaning or determining judgment differs.

For one exact established Binding, at most one distinct Replacement or Withdrawal may establish with that Binding as the
predecessor or target. A Replacement to B2, a different Replacement to B3, and a Withdrawal of B1 are mutually exclusive
meanings for the same predecessor boundary. If the complete applicable Governance meaning supports distinct competing
succession or termination results, none of those competing Replacement or Withdrawal occurrences establishes. Governance
does not choose among them by priority, declaration order, recency, Arbitration, physical completion, or backend state.

Once one Replacement or Withdrawal has validly established for B1, no later distinct Replacement or Withdrawal may
establish for that same B1. This is not physical first-wins semantics. It follows from already-established immutable
Governance history: the exact predecessor boundary already has one authoritative succession or termination meaning. A
later Binding may itself become the predecessor of another Replacement, so legal succession may continue as a
non-branching chain without reopening an earlier predecessor.

Replacement is local to its exact Scope. Several independent replacements do not become one atomic multi-part change by
synchronization or graph connectivity. If several parts must change as one Governance meaning, one explicit encompassing
multi-part Scope must declare that arrangement as one Governance meaning.

Replacement establishes succession, not the applicability cut. Binding Transition owns the later semantic boundary at
which later applications may acquire the successor Binding. Replacement therefore does not rewrite applications that
already acquired B1.

There is no universal `No Replacement` material. The absence of an Established Replacement says only that no Replacement
authority was established. If a future explicit total Replacement Law requires a negative outcome, that law must declare
the negative meaning rather than obtaining it from absence.

**Open in this section:** the exact Replacement frontend form remains to be designed.

### 4.10. Withdrawal

Withdrawal is Governance-owned Established Occurrence Material that establishes explicit termination meaning for one
exact already-established Binding without a successor in that judgment.

```text
Binding B1 established

explicit Withdrawal W1
    target = B1

termination meaning:
    B1 has no successor through W1
```

Withdrawal does not erase or mutate B1, move machine State, rewrite its Policy World, or create `Binding(null)` or a
synthetic empty Binding. B1 remains exact historical Established Material. A later unrelated Binding does not
retroactively turn the earlier Withdrawal into Replacement.

Withdrawal targets one exact Binding. Policy disappearance, `No Selection`, `No Resolution`, timeout, restart, process
death, missing registry state, configuration cleanup, or another realization absence cannot imply Withdrawal.

Withdrawal and Replacement are mutually exclusive succession meanings for one exact predecessor Binding under Section
4.9. If distinct competing Withdrawal or Replacement results are supported for the same Binding boundary, none
establishes. Once one of those meanings has established, no later distinct Replacement or Withdrawal may establish for
that same predecessor.

An indivisible multi-part Binding is withdrawn as one exact Binding. Withdrawal cannot change only one part of the same
Binding and leave the remainder of that Binding authoritative.

Withdrawal establishment and the applicability cut are separate. Withdrawal establishes the termination judgment;
Binding Transition establishes the semantic boundary after which later applications can no longer newly acquire the
withdrawn Binding. Already-established applicability is not rewritten.

There is no universal `No Withdrawal` material. Absence of Withdrawal does not mean Governance explicitly judged that a
Binding must be retained. An explicit total law, if one is introduced, must declare any negative outcome it requires.

**Open in this section:** the exact Withdrawal frontend form remains to be designed.

### 4.11. Binding Transition and Active Binding Continuity

Binding Transition is Governance-owned Established Occurrence Material that establishes the semantic applicability cut
for one exact already-established Replacement or Withdrawal. It does not compete with Replacement or Withdrawal and does
not choose a successor. Succession or termination meaning must already be singular before a Transition can establish.

```text
Replacement R
    B1 -> B2
        ↓
Binding Transition T
        ↓
later applications may acquire B2

Withdrawal W
    target B1
        ↓
Binding Transition T
        ↓
later applications may no longer acquire B1
```

At most one distinct Binding Transition may establish for one exact Replacement or Withdrawal. If the complete
applicable Transition meaning supports different applicability cuts for that same triggering occurrence, no competing
Transition establishes. Governance does not choose a cut by declaration order, timestamp, recency, physical completion,
runtime publication state, or backend priority. Once one Binding Transition has validly established for an exact
Replacement or Withdrawal, no later distinct Transition may establish for that same triggering occurrence.

A Transition is not a wall-clock timestamp, thread switch, CAS success, publication write, deployment generation,
network acknowledgement, scheduler event, or backend epoch. Those may realize or record the semantic boundary but do not
define it.

Once Binding Applicability has been established for one exact governed application, later Replacement, Withdrawal, or
Binding Transition does not change that Binding inside the same application. The application retains one complete
Binding meaning for its governed lifetime.

```text
one exact governed application
    -> one complete applicable Binding
    -> no semantic tearing
```

The continuity boundary is the exact governed application determined by the Governance Scope and the dependent semantic
use. Kontrakt does not introduce one universal request, flow, run, session, process, thread, or object-lifetime
boundary. An explicit multi-part Governance application retains one complete multi-part Binding as one indivisible
meaning.

Applications under earlier and later Bindings may coexist physically. Stop-the-world, drain-before-start, global locks,
epochs, RCU, MVCC, immutable-image pinning, or prepare-then-publish are possible realization families, not Contract law.
A backend may even use mixed physical generations when it can prove that one exact governed application still observes
one complete Binding meaning and every applicable higher-scope obligation is preserved.

Per-application Binding continuity does not create implicit cross-application compatibility. If applications under
different Bindings participate in a larger composition whose combination is constrained, that constraint must already be
declared as an explicit Contract obligation by the authority that owns the larger composition. Governance and the
compiler do not infer incompatibility from observed interaction, shared state, topology, or old/new coexistence.

Transition does not decide when old representation can be reclaimed. Retention counts, pins, grace periods, memory
reclamation, cache eviction, and class unloading remain backend concerns.

**Open in this section:** the exact IDL and canonical form for explicitly declaring the semantic Transition law and its
applicability-cut relation remains to be designed. Physical time or publication state cannot substitute for that
Contract meaning.

### 4.12. Validity, Singularity, and Complete Decision

Governance validity is not one universal Boolean. Definition validity, judgment establishment, Binding establishment,
Binding Applicability, and explicitly declared higher-scope coherence remain separate responsibilities under their
owning laws.

Whether one dependent application requires Governance is determined by that dependent Contract meaning. A universal
`required`, `optional`, or `absent` flag is not attached to Governance Scope. If the dependent law requires an
applicable Governance Binding as Required Basis, exactly one Binding must be applicable to that exact application. If it
does not require Governance, Binding absence does not become an error or a hidden fallback rule.

A Policy World definition does not become authoritative merely because it exists. When a dependent application requires
a governed Policy-World arrangement, an applicable Governance Binding is required even if only one Policy World is
selectable. Candidate cardinality never creates implicit authority; the Decision Law must explicitly establish the
Selection or `No Selection`.

For one exact dependent application that requires Governance:

```text
0 applicable Bindings
    -> required basis incomplete
    -> dependent application not established

1 applicable Binding
    -> singular Governance authority

2+ applicable Bindings
    -> invalid ambiguity
```

Two Bindings remain ambiguous even when they carry the same exact Selection because their lineage, Governance Version,
Withdrawal, Transition, and occurrence meaning may differ. Same-response plurality is preserved at Decision attribution
and converges before Binding.

Many immutable Bindings may exist historically or physically for one Scope. Singularity applies to the exact dependent
application, not to the total number of established Binding occurrences. Binding Applicability singularity is separate
from the succession singularity of Sections 4.9-4.11: at most one distinct Replacement or Withdrawal may establish for
one exact predecessor boundary, and at most one distinct Transition may establish for that succession or termination
occurrence.

Binding absence is not represented by a universal `NoBinding` material. `No Selection`, `No Resolution`, Withdrawal,
non-applicability, and a required Binding that does not exist remain distinct semantic facts or non-establishment
conditions. Missing required Binding does not automatically establish Failure; ADR-0057 and the owning Failure law
determine Failure meaning.

A Governance definition that structurally cannot select any legal Policy World for a dependent use known to require a
Binding is invalid when that fact is determinable during definition or composition verification. This differs from a
valid Decision Law establishing `No Selection` for one complete occurrence Basis.

Failure and Diagnostic treatment for missing required Binding, ambiguous applicable Bindings, non-establishment,
Withdrawal, `No Resolution`, and later succession or Transition ambiguity is outside this Governance semantic decision
and remains open under Section 6.

### 4.13. Exact Attribution

Every Governance-established occurrence must remain exactly attributable to the direct semantic material and direct
semantic relations that determined its establishment. Attribution is Contract meaning. It is not reconstructed from
current state, names, declaration order, timestamps, logs, compiler generations, storage history, or backend object
structure.

Governance semantic expression remains flat. Exact semantic material and exact direct semantic relations are explicit;
no Governance material is required to contain or own a list of references, producers, dependencies, parents, children,
or downstream consumers. The Canonical Contract World preserves those relations in its shared semantic substrate without
becoming a new Governance Authority. A relation may connect exact semantic material without making either endpoint
contain the other. The physical representation of those relations remains replaceable.

Cross-authority source connection remains owned by Composition under ADR-0063. Governance declares Required Basis
meaning and judges the complete applicable Basis produced from those explicit connections. Governance does not absorb
the producer topology or re-establish source-owned material merely because that material participates in a Decision.

A Governance Decision remains directly attributable to its exact Governance Authority and Version, Decision Law semantic
coordinate, exact governed application, complete applicable Decision Basis, Decision Outcome, and every determining law
element. Independently attributable Decision-Law elements require stable Governance-version-local semantic coordinates.
Source line, declaration ordinal, array position, compiler generation, or runtime object identity cannot define those
coordinates. Several elements that determine the same Outcome establish one Decision with plural element attribution;
elements that determine distinct Outcomes make the judgment ambiguous.

Selection has no independent Contract Authority or Version. It is an exact canonical semantic value whose equality is
the complete order-independent arrangement of exact resolved Policy World meanings. Scope likewise has its own exact
semantic coordinate under Governance or preserves the source authority of an existing semantic domain; expanded
membership is derived analysis and does not become Scope identity.

An Arbitration Judgment remains directly attributable to its exact Arbitration Law, resolution boundary, complete
unordered set of competing Governance Decision occurrences, Arbitration Outcome, and every determining Arbitration-law
element. It never reduces attribution to selected Policy World names or declaration order.

A Binding remains directly attributable to its exact Scope, complete effective Selection, and immediate determining
Governance judgment. Same-response concurrence preserves every determining Decision relation. Conflict resolution
preserves the direct relation from Binding to the exact Arbitration Judgment; the Arbitration Judgment separately
preserves its own exact Decision relations. Binding does not duplicate the full ancestry.

Replacement establishes the explicit predecessor-successor relation between its exact Bindings. Withdrawal establishes
the explicit termination relation for its exact target Binding. Binding Transition establishes the explicit relation to
its exact triggering Replacement or Withdrawal and its exact semantic applicability cut. Those relations are direct and
complete; they do not require recursive ownership or reference chasing to gain Contract meaning.

A dependent semantic application owns its exact use of one applicable Binding under ADR-0063. Binding does not
accumulate a mutable list of future consumers, and Governance does not acquire authority over every dependent occurrence
merely because they use its Binding.

The explicit direct relations may form a graph when viewed together. The subset that participates in semantic
establishment is acyclic because circular semantic authority is illegal. That graph or DAG is an analytical consequence
of the flat explicit relations, not the Contract authoring model, authority model, or semantic evaluation mechanism.
Transitive closure, graph traversal, dependency graphs, invalidation graphs, scheduling graphs, and Diagnostic
provenance graphs are derived subsystem knowledge.

Semantic Identity, canonical value equality, Exact Attribution, and compiler identity remain different. Definition
Reference and Occurrence Reference remain ADR-0063 semantic relations that identify authoritative definition meaning or
one exact semantic application. They do not require pointer-bearing Contract objects or recursive reference structures.
Fingerprints, digests, HIDs, cache keys, trace IDs, storage addresses, timestamps, compiler-local ordinals, intern
handles, and physical pointers may support realization but cannot create Contract authority or semantic identity.

Physical reevaluation, cache misses, repeated query execution, or backend replay cannot manufacture another Contract
occurrence when the same already-established occurrence is being represented or recomputed. Conversely, equal Outcome,
Selection, or other value meaning does not collapse occurrences established by distinct Governance judgments. Occurrence
distinction remains owned by ADR-0063 and the exact Governance law that establishes each occurrence.

### 4.14. Scope Independence, Direct Governance Overlap, and Cross-Scope Coherence

Governance Scopes are independent unless an explicit Contract says otherwise. Multiple Governance Decisions may coexist
in one Whole Machine.

A Direct Governance Overlap exists only where otherwise-applicable Governance Decisions include the same exact governed
application in their Scope meaning. Disjoint Scopes therefore create no Governance overlap. Partial, full, and contained
overlap do not create separate precedence classes by themselves.

Overlap is not conflict. Overlapping Decisions that carry the same exact Selection are compatible concurrence. Their
effective response is singular while their determining Governance judgments remain distinct. Overlapping Decisions that
carry different Selections create a Governance conflict and are subject to Section 4.7.

The Selection for Scope A does not imply, inherit, propagate, override, or constrain the Selection for Scope B merely
because the Scopes are structurally related or belong to one Whole Machine. Structural containment does not make a
Binding for one Scope applicable to another Scope, and overlap does not establish a new Scope or hidden precedence.

No Direct Governance Overlap does not prove higher-scope coherence. If results from disjoint Governance domains must
satisfy a relation when used together, that relation must already be declared as an explicit Contract obligation by the
authority that owns the larger composition. The compiler verifies that declared obligation. Governance and the compiler
must not invent a compatibility matrix from runtime interaction, shared State, topology, physical coexistence, or
old/new Binding generations.

This separates two different questions:

```text
same exact governed application?
    -> Direct Governance Overlap

disjoint Governance domains used together?
    -> explicit higher-scope Contract obligation, when the larger meaning requires one
```

A higher-scope obligation may reject a combination that has no Governance overlap. That result belongs to the authority
that owns the larger composition and is not Governance Arbitration.

### 4.15. Indivisible Governance Meaning Across Several Parts

Independent Governance Scopes remain independent. Contract-level indivisibility is not created by synchronizing separate
Scope Bindings or taking the transitive closure of an overlap graph.

If several Policy-World selections must form one indivisible Governance meaning, they must be represented by one
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

The complete Selection becomes one Governance Decision Outcome for that encompassing Scope. A half-established subset
does not acquire partial Governance authority.

This also separates conflict location from resolution boundary. A conflict may appear at one member application, but
when that member belongs to a declared indivisible multi-part Governance meaning, the complete encompassing Scope is the
resolution boundary. Arbitration preserves the arrangement as one semantic unit instead of resolving its parts
independently.

Outside such an explicit declaration, conflict resolution remains local to the exact governed application. A chain such
as `A overlaps B` and `B overlaps C` does not make `{A, B, C}` one conflict set. An Arbitration result does not rewrite
conflict-free parts outside its resolution boundary.

The same completeness law applies to Binding and Binding Transition. One indivisible multi-part application acquires one
complete multi-part Binding and cannot observe an old/new mixture inside that application.

This law does not make an encompassing Binding implicitly propagate into other independently governed Scopes. The exact
Scope and Selection must declare the governed meaning.

**Open in this section:** the exact encompassing-Scope authoring form, multi-part Selection frontend, and canonical
representation remain to be designed.

### 4.16. Separation from Policy and Other 1D Contracts

Policy owns the contents of a Contract World. Governance owns whether one declared World arrangement is bound to an
exact Scope.

Governance declares only the meaning required by Decision Basis. Source connection and use follow ADR-0063. Governance
does not name producer topology, rerun the source judgment, reinterpret the source obligation, or acquire the source
authority. It judges only Governance meaning and establishes its own result.

A World change changes the 1D Contracts applicable to later processing only through an applicable Governance Binding and
its Transition law. Governance does not perform a second implicit compatibility, migration, or reconciliation judgment.

Material from an earlier World may enter later processing. The currently applicable Input, Admission, Version,
Invariant, State, Budget, Capacity, and other 1D Contracts judge that material according to their own obligations. If a
higher-scope combination itself requires coherence, that obligation must be explicitly declared by the authority that
owns that larger composition.

Historical World provenance has no implicit meaning. If provenance itself matters to another Contract judgment, that
meaning must be explicitly required and supplied under ADR-0063.

### 4.17. Separation from the State-Machine Axis

Governance and the State Machine are independent Contract axes.

```text
Governance
    establishes which Policy World arrangement applies

State Machine
    establishes current State
    and legal Transitions within the applicable World
```

Governance does not set current State, execute a State Transition, or use State-machine movement as an implicit side
effect of Selection, Arbitration, Binding, Replacement, Withdrawal, or Binding Transition.

The State Machine does not automatically select, replace, withdraw, arbitrate, or transition Governance because a
particular State was established.

If State-related meaning must affect Governance, Governance declares the required Decision Basis meaning rather than a
State-Machine producer dependency. Composition may satisfy that Basis with State-Machine Established Material. If a
later State-Machine judgment depends on Governance meaning, each authority keeps its own meaning across that explicit
connection.

Any circular establishment relation across Governance, State Machine, or another authority is illegal and causes
compilation failure after the semantic connections are resolved.

### 4.18. Governance Is Not an Operation-Level Judgment Slot

Governance is not another ordinary pipeline judge beside Admission, Budget, Capacity, Invariant, State Machine, or
Publication.

The older conceptual form:

```text
Governance admits the contract world
    -> Permit / Refuse continuation
```

is withdrawn.

Governance establishes Decisions, optional Arbitration Judgments, Bindings, succession or termination judgments, and
Binding Transitions at their own semantic boundaries. A dependent application may fail to be established because its
Required Basis lacks one applicable Binding, but Governance is not a generic per-operation `canContinue` Boolean.

Earlier ADRs that still present Governance as an ordinary per-interaction refusal authority require reconciliation.

### 4.19. Governance Version

Governance is version-sensitive under ADR-0053. One Governance Version fixes one complete Governance meaning, including
Decision Basis meaning, Decision Law, Scope semantic coordinates, selectable exact Policy World meanings, Arbitration
Law, and every declared Replacement, Withdrawal, or Binding Transition law meaning owned by that Governance Version.

Policy Version and Governance Version are independent authorities.

```text
Governance / G2

Payment.Policy.Normal / N4
Payment.Policy.Emergency / E7
```

G2 continues to mean the exact resolved targets N4 and E7 even if `Normal / N5` or `Emergency / E8` is later declared.
The existence of a newer Policy Version does not mutate an older Governance Version.

If a Governance definition changes its resolved Policy World target from N4 to N5, its Governance meaning has changed
and a new Governance Version is required. Reusing G2 for a different resolved target is a Version conflict and must fail
compilation.

Governance target meaning cannot come from `latest`, `current`, `preferred`, Version ranges, registry order, or runtime
lookup. Frontend syntax may avoid repeating a Version token when another declaration already fixes it, but semantic
resolution must end in one exact Policy Authority and Version meaning under ADR-0063.

A Policy World Version likewise fixes the exact Contract arrangement it owns. Later versions of Budget, Capacity,
Admission, State, or another constituent Contract do not rewrite an established Policy World Version. If Policy chooses
a new constituent meaning, that requires its own new Policy Version before Governance can target it.

Governance Decision, Arbitration Judgment, Binding, Replacement, Withdrawal, and Binding Transition are occurrence
material under one resolved Governance Authority and Version. They do not each acquire a second independent Contract
Version. Their exact Governance Authority and Version relation must remain recoverable.

Governance uses fine-grained semantic coordinates for exact meaning that must be independently distinguishable or
attributable, such as Decision Law, Scope, Arbitration Law, and independently attributable law elements. Those
coordinates remain part of one flat Governance Authority/Version domain; they do not introduce semantic parent/child
ownership, independent sub-Versions, or recursive Contract structure.

A semantic coordinate under G2 and the corresponding coordinate under G3 are not the same exact versioned Contract
meaning merely because their resolved local material is equal. Compiler realization may recognize equal resolved local
canonical structure across Governance Versions and reuse immutable representation or analysis. That structural equality
or reuse identity is compiler-owned and does not merge the Contract Semantic Identity of the two Versioned meanings.

A Binding preserves the exact Policy World Version carried by its Selection. A later Policy or Governance Version does
not rewrite that Binding or invalidate applications that already established its applicability.

```text
new Policy Version
    !=
new Governance Version
    !=
Replacement or Withdrawal
    !=
Binding Transition
```

Version revision and governing-authority succession are separate semantic events. New Governance Version existence does
not implicitly replace, withdraw, or transition an existing Binding.

Governance does not infer Version compatibility from equal names, similar contents, hashes, fingerprints, or small
diffs. If a future Contract meaning requires compatibility, that meaning needs its own explicit authority. Contract
Semantic Identity remains distinct from compiler structural identity, HIDs, fingerprints, cache keys, compiler
generations, storage revisions, ordinals, intern handles, and backend publication revisions.

## 5. Frontend and Resolution

### 5.1. Policy World References

Governance uses already-declared Policy Authorities and their resolved Contract Worlds.

An authored Policy symbol is not itself canonical Governance meaning. Resolution must end in the exact Policy Authority
and Policy Version meaning required by ADR-0063. `Definition Reference` here denotes that exact semantic target; it does
not require a pointer field, object link, nested reference structure, or runtime lookup inside Governance material.

`latest`, `current`, `preferred`, Version ranges, runtime strings, configuration keys, enum ordinals, object references,
generated classes, service-registry entries, source nesting, declaration proximity, or backend lookup cannot become
Policy identity or choose a Governance target.

The physical representation of the resolved target may later use indexes, ordinals, HIDs, intern handles, table slots,
offsets, or another compiler-owned form. None of those representations creates Policy or Governance authority.

**Open in this section:** the exact `.kontrakt` syntax for Governance Policy World references remains to be designed.

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

Governance authoring is a flat manifest at the Contract semantic boundary. Source syntax may group declarations for
readability, but physical nesting, textual containment, inheritance, or declaration placement cannot create semantic
Scope, authority, precedence, or applicability.

The earlier Interface-local Governance syntax therefore cannot remain a semantic shortcut for Scope. The governed Scope
must resolve to one exact Scope semantic coordinate under the resolved Governance Authority and Version, or to one exact
already-established semantic domain whose source authority remains unchanged.

Where Scope uses another established semantic domain, the explicit semantic relation preserves that domain's exact
identity and source authority instead of copying its membership into Governance. Where Governance declares a new
grouping, the grouping itself must be explicit enough to receive one exact Governance Scope coordinate. No semantic
parent/child containment is introduced in either case.

A runtime flow, run, operation, session, process, thread, deployment, object, or source container does not become Scope
because execution or syntax happens inside it. The frontend follows established semantic-domain meaning rather than a
closed Scope-kind enum or physical topology.

Exact Scope relations may contribute to a conceptual graph when considered with other semantic relations. That graph is
not the frontend model and is not traversed to discover Scope meaning. Scope meaning comes from the flat explicit
manifest and exact established relations.

The exact flat frontend form for existing semantic domains and Governance-owned grouping coordinates remains owned by
the open frontend decision in Section 4.5.

### 5.4. Canonical Governance Material

Canonical Governance material preserves the flat explicit Contract meaning established by this ADR. Every consumer must
be able to reach the same semantic result without reconstructing meaning from source layout, runtime state, logs,
timestamps, backend generations, pointer topology, recursive object ownership, or graph traversal.

Governance Authority and Version define one flat semantic domain. Within that domain, independently distinguishable
Versioned semantic subjects include Governance Definition meaning, Decision Law, Governance Scope, and Arbitration Law.
Independently attributable Decision and Arbitration law elements and required-basis entries use stable
Governance-version-local semantic coordinates but do not become independent Contract Authorities or sub-Versions.
Whether future Replacement, Withdrawal, or Transition frontend material becomes an independently addressable law subject
is decided only if its final Contract form requires independent reference or attribution; compiler optimization alone
cannot create a new Contract identity boundary.

Complete Decision Basis, Selection, Decision Outcome, complete Arbitration input, and Arbitration Outcome are canonical
semantic values rather than independent Contract Authorities. Their equality is defined by their exact semantic meaning.
Selection equality is the complete order-independent arrangement of exact resolved Policy World meanings. Complete
Arbitration input equality is the complete unordered set of exact competing Decision occurrences for its resolution
boundary. Compiler interning or hashing of those values remains realization.

Governance Decision, Arbitration Judgment, Binding, Replacement, Withdrawal, and Binding Transition remain distinct
Established Occurrence Material. Equal Selection, Outcome, Scope, or other value meaning does not collapse occurrences
that were established by distinct Governance judgments. Physical reevaluation or cache behavior likewise cannot create a
new Contract occurrence for meaning that has already been established.

Canonical Governance semantics preserves exact direct semantic relations between exact semantic material. Participating
material is not required to contain or own reference lists to other material. Cross-authority source connection remains
owned by Composition under ADR-0063; Governance-specific relations preserve only the Governance meaning established by
this ADR. Each relation preserves the exact semantic meaning or exact semantic application identified under its owning
law, while the physical encoding of that relation remains replaceable compiler representation.

The explicit relations may form a graph when viewed together. The semantic-establishment subset is acyclic because
circular semantic authority is illegal. A graph or DAG is therefore a consequence or analytical view of the flat
relations, not the Contract authoring model, authority model, data structure, or semantic evaluation mechanism. No
Contract meaning is obtained by parent/child traversal, recursive reference chasing, higher-order Contract invocation,
callback evaluation, fixed-point computation, or transitive graph walking.

`Definition Reference` and `Occurrence Reference` remain ADR-0063 semantic relations for identifying authoritative
definition meaning or one exact semantic application. Governance does not introduce a separate `Canonical Reference`
Contract category. Pointer fields, object links, indexes, table slots, HIDs, ordinals, intern handles, hashes, and
fingerprints are possible compiler representations, not Contract relations or semantic identity.

Canonical material preserves the completeness and singularity laws without creating synthetic absence material.
Same-response Decisions may remain plural while converging into one Binding. Different-response Direct Governance
Overlap requires one complete Arbitration Judgment before Binding. At most one distinct Replacement or Withdrawal may
establish for one exact predecessor Binding, and at most one distinct Binding Transition may establish for one exact
Replacement or Withdrawal. Competing succession, termination, or Transition meanings establish none rather than being
repaired by priority, order, recency, Arbitration, or physical completion. Multi-part Selection, Binding, succession
meaning, and Transition remain indivisible for their explicit encompassing Scope.

Scope does not take identity from its expanded member set. Compiler membership expansion, overlap discovery,
conflict-set analysis, transitive attribution graphs, dependency graphs, invalidation graphs, scheduling graphs,
Diagnostic slices, and other traversals are derived analysis rather than new Contract authority. They may be computed
from the flat semantic material and explicit relations but cannot feed hidden meaning back into the Contract.

Semantic Identity, canonical value equality, compiler structural equality, and backend technical identity remain
separate. Different Governance Versions remain different exact Versioned Contract meanings even when some resolved local
canonical material is equal. V2 may reuse such equal local canonical structure, derive compiler-owned query keys, and
reuse deterministic analysis products without merging Contract Semantic Identity. HIDs, fingerprints, hashes, cache
keys, compiler generations, physical addresses, and storage revisions remain realization knowledge.

Backend representation remains replaceable after canonical meaning is fixed. The Contract does not prescribe recursive
byte embedding, pointer chains, graph-node ownership, table layout, arena layout, or any other physical structure.

**Open in this section:** byte-level canonical encoding, compact realization forms for Definition and Occurrence
targets, frontend-to-canonical lowering shape, physical representation of explicit semantic relations, and exact V2
storage or query representation remain to be designed. Reuse of ordinary Contract compiler machinery is allowed only
where it preserves the Governance authority and flat semantic distinctions above.

## 6. Verification Boundary

This ADR defines Governance Contract legality and semantic meaning. It does not define the architecture, pass structure,
query model, cache strategy, scheduling, diagnostic pipeline, or physical algorithms of the future Verification
subsystem.

A later Verification subsystem must consume the laws established here without inventing repair semantics. Illegal or
ambiguous Governance meaning cannot be made legal by declaration order, runtime priority, fixed-point recovery,
recursive query evaluation, fallback, latest-value selection, backend state, or physical completion order. The subsystem
may derive whatever analysis structure it needs, but that structure remains implementation knowledge and cannot become
Governance authority.

The owning sections of this ADR already define the Contract distinctions that later verification must preserve,
including complete Decision Basis, deterministic Decision Outcome, exact Scope, complete Selection, Direct Governance
Overlap, Arbitration, Binding Singularity, succession and Transition singularity, Version exactness, acyclic semantic
establishment, flat explicit semantic relations, source-authority preservation, and non-retroactivity. This section does
not duplicate those laws as verifier procedures.

ADR-0063 remains the common source for Establishment, Definition and Occurrence target identity, Basis Resolution,
Applicability, complete basis, Composition Authority, occurrence integrity, source-authority preservation, and semantic
determinism. ADR-0057 remains responsible for Failure meaning when a Governance-related condition is itself declared as
a Failure trigger.

**Open in this section:** the Verification subsystem design, exact diagnostic codes, user-facing explanations, and
Failure mapping for incomplete required Basis, ambiguous applicable Bindings, `No Selection`, `No Resolution`,
Withdrawal, succession or Transition ambiguity, and occurrence non-establishment remain to be designed with the
Verification and Diagnostic subsystems. They must not redefine the Governance semantics established here.

## 7. Contract and Implementation Boundary

Governance owns its Decision Basis meaning, Decision Law, Governance Decision, Scope, Selection, Decision Arbitration,
Binding, Replacement, Withdrawal, Binding Transition, and the Governance-specific direct semantic relations established
by those laws. Cross-authority source connection remains owned by Composition under ADR-0063. Governance does not own
the physical mechanism that observes, stores, transports, synchronizes, publishes, pins, or reclaims realization.

Governance Contract judgment and Governance realization judgment are distinct.

```text
Governance Contract judgment
    explicit established semantic basis
        -> user-declared Governance law
        -> established Decision / Arbitration / Binding / succession meaning

Governance realization judgment
    already-established Governance meaning
        -> can the backend realize that exact meaning correctly and deterministically?
```

A realization check may determine that established Governance meaning cannot yet be faithfully realized. It cannot
replace that meaning with another Policy World, weaken its conditions, reinterpret its Decision Law, manufacture a
fallback Binding, or alter the semantic Transition cut.

Possible realization mechanisms include atomic references, CAS protocols, locks, transactions, WAL records, actors,
control-plane services, message logs, replicated state machines, immutable images, configuration stores, RCU-style
reclamation, MVCC-style pinning, and prepare-then-publish protocols. None becomes Governance Authority merely because
one backend uses it.

Likewise, an operator UI, CLI, deployment controller, scheduler, monitor, automation system, or another machine may
participate in supplying material. Transport or observation does not create Contract authority. Raw realization
observation must first receive the semantic meaning required by the Governance judgment under an owning authority.

The backend may derive compact handles for exact semantic targets, memoize deterministic Decision or Arbitration
computation, retain immutable Bindings, or pin the Binding applicable to one governed application. Reusable computation
does not collapse distinct Governance occurrences. Physical generation, handle, cache entry, fingerprint, trace ID,
publication revision, pointer, or retained image cannot become semantic identity or Contract relation.

A backend may allow old and new physical generations to coexist, and may even use mixed physical generation internally,
when it proves that every exact governed application still observes one complete Binding meaning and all explicit
higher-scope obligations remain satisfied. Contract-visible semantic mixing remains forbidden.

Binding does not accumulate mutable consumer state. The dependent semantic application owns its exact use of an
applicable Binding under ADR-0063. Retention counts, pins, grace periods, session handles, epoch counters, and
reclamation state remain implementation details and must not become required user Input, Output, Fact, Operation
parameters, or user-managed Governance handles.

**Open in this section:** the public Governance control API remains to be designed. It must expose Governance without
turning transport, operator workflow, runtime handles, realization generations, or backend state into Contract
Authority.

## 8. Relationship to Whole Machine and Concurrency

ADR-0055 establishes the relevant Whole Machine flow boundary. Independent Contract pipelines flow one way, and Whole
Machine composition does not gain a hidden Contract-level scheduler, join protocol, or synchronization authority.

Governance continuity is defined by exact governed application rather than by a universal flow or Whole Machine runtime
unit. Once one dependent application establishes Binding Applicability, that application retains the same complete
Binding meaning for its governed lifetime. A later Binding Transition changes eligibility for later applications and
does not re-resolve the Binding inside the earlier application.

This law covers a Whole Machine cooperation when that cooperation is the exact governed application of its Scope. It
also covers an explicit multi-part Governance application. It does not imply that every pipeline flow, run, session, or
Whole Machine instance is automatically a Governance Scope.

Earlier- and later-Binding applications may execute concurrently. Their physical coexistence is not itself Governance
conflict. If their combination is constrained by a larger semantic composition, that constraint must be an explicit
Contract obligation owned by the higher-scope authority. Governance and the compiler do not infer that relation from
shared State, interaction, topology, generation age, or physical overlap.

Material published by completed processing remains ordinary material when presented to later Contract processing unless
another Contract explicitly requires its Governance provenance. Later 1D Contracts judge that material under their own
obligations and authority.

Several Governance Scopes may belong to one Whole Machine, but no Scope automatically controls another. Cross-Scope
requirements must be explicit higher-scope Contract obligations. If several Policy-World changes are one indivisible
Governance meaning, they are declared as one complete Governance decision over one encompassing Scope rather than as
synchronized independent Bindings.

Replacement, Withdrawal, Transition, and new application establishment may be physically concurrent where the already-
established semantic relations permit that coexistence. Physical concurrency cannot make competing succession,
termination, or Transition judgments valid. Locking, CAS, epochs, transactions, message order, distributed consensus,
immutable images, or execution context may preserve the required meaning but never determine it.

### 8.1. Backend World-Replacement Problem

A simple backend can preserve the Contract by stopping admission of later governed applications, allowing every earlier
application to finish under its already-applicable Binding, realizing the next Binding Transition, and then admitting
later work. That drain-and-switch strategy is a valid correctness baseline, but it is effectively stop-the-world at the
relevant governed boundary and is not the target realization.

The stronger target is multi-version realization. Earlier applications may keep the old complete Binding while later
applications acquire the successor Binding after the semantic Transition. No Contract law requires both generations to
switch in one physical instant.

Earlier Kontrakt backend work contains useful implementation precedents: immutable frozen images, stable identity work,
validate-before-publication boundaries, Contract images, caching, and future query or incremental architecture. These
mechanisms predate the current Governance semantics and are implementation assets only. They may be reused, revised, or
replaced after the backend requirements are derived from this ADR.

Candidate implementation families include:

```text
immutable multi-version image publication
RCU-style grace-period reclamation
MVCC-style snapshot pinning
prepare-then-publish consistent update
query / incremental rebuilding with structural sharing
```

These names do not constrain Contract semantics. In particular, RCU does not by itself provide a multi-part semantic
snapshot, MVCC snapshot granularity must match the actual governed meaning, and independent component publication must
not expose partial old/new Binding arrangements. The backend begins from the Contract continuity requirement and selects
or combines mechanisms only after proving that requirement.

A V2 backend may reuse deterministic Decision and Arbitration computation when semantic inputs are unchanged while still
establishing distinct occurrence material where occurrence identity matters. It may also exploit physical mixed-
generation execution when meaning preservation is proven. What it may not do is infer Governance meaning from query
completion order, cache presence, hash equality, node arrival, or implementation revision.

Any implementation-local publication revision remains distinct from ADR-0053 Contract Version and from Governance
Decision, Binding, Replacement, Withdrawal, or Binding Transition identity. Old representation reclamation occurs only
after the backend no longer needs it for faithful realization; that reclamation boundary is not Governance meaning.

**Open in this section:** the backend realization still needs a separate design for non-stop-the-world publication,
version pinning, distributed preparation, safe mixed-generation realization, and reclamation of older images. That work
must compare current frozen-image and identity machinery with future query and incremental mechanisms without turning
any of them into Governance semantics.

## 9. Contract and Implementation Decisions

### 9.1. Decisions Made Here

This ADR decides the following Governance boundary:

- Policy declares complete Contract Worlds; Governance establishes their authority for exact governed Scopes.
- Governance declares Required Basis meaning rather than producer topology. Source material remains owned and connected
  under ADR-0063, and cross-authority connection remains Composition-owned.
- Governance authoring and semantic expression are flat. Exact direct semantic relations may collectively form a graph
  or acyclic establishment DAG, but parent/child ownership, recursive reference chasing, higher-order Contract
  invocation, callback discovery, and graph traversal do not establish Governance meaning.
- Decision Law deterministically establishes one explicit Decision Outcome for one complete applicable Basis: one
  complete Selection or `No Selection`.
- Governance Decision and Arbitration Judgment are Governance-owned Established Occurrence Material. `No Selection` and
  `No Resolution` are explicit outcomes rather than hidden absence.
- Scope is one exact semantic coordinate under a resolved Governance Authority and Version. It may use an existing
  established semantic domain or declare one Governance-owned grouping; no closed Scope-kind enum, source-containment
  meaning, semantic hierarchy, or universal membership wrapper is introduced.
- Selection is one complete order-independent canonical semantic value of exact Policy World meanings and has no
  independent Contract Authority or Version.
- Same-response Direct Governance Overlap is compatible concurrence with plural judgment attribution. Different-response
  overlap requires one complete unordered Arbitration Judgment over the exact resolution boundary.
- Arbitration does not fold pairwise, use physical order, or synthesize Policy meaning. It establishes a Resolved
  Selection or explicit `No Resolution`.
- One Binding establishes one complete governing relation. Same-response plurality converges before Binding, and one
  exact dependent application that requires Governance may have exactly one applicable Binding.
- Replacement relates one exact already-established predecessor Binding to one exact already-established successor
  Binding. It does not establish the successor Binding. Withdrawal establishes exact termination meaning without a
  successor and without deleting its target or manufacturing an empty Binding.
- For one exact predecessor Binding, at most one distinct Replacement or Withdrawal may establish. Competing succession
  or termination meanings establish none; Governance does not choose a winner by priority, order, recency, Arbitration,
  or physical completion.
- Binding Transition is separate from Replacement and Withdrawal and follows one already-established succession or
  termination judgment. For one exact Replacement or Withdrawal, at most one distinct Transition may establish;
  competing applicability cuts establish none.
- One exact governed application keeps one complete Binding meaning. Multi-part Governance meaning remains indivisible,
  while old and new applications may coexist physically.
- Cross-Scope or cross-application coherence is never inferred. When the larger composition constrains a combination,
  its owning authority must declare that obligation explicitly.
- Contract inheritance, higher-order Contract indirection, recursive Contract structures, and direct or indirect
  semantic-establishment cycles are illegal. Earlier Established Material may be explicit basis for later acyclic
  judgments; no fixed-point or recursive Contract establishment is introduced.
- Governance requirement is expressed through the dependent law's Required Basis rather than a universal
  required/optional Scope flag. Binding absence, `No Selection`, `No Resolution`, Withdrawal, and non-establishment
  remain distinct.
- Exact Attribution is preserved by exact direct semantic relations in the flat semantic model rather than by each
  material owning dependency or reference lists. Transitive provenance and dependency graphs are derived subsystem
  knowledge.
- Governance uses fine-grained exact semantic coordinates where Contract meaning requires independent distinction or
  attribution, while canonical value equality, compiler structural equality, HIDs, fingerprints, hashes, storage IDs,
  ordinals, and compiler generations remain separate.
- Governance Version fixes the complete Governance meaning and exact Policy World Version targets. Equal local resolved
  structure across different Governance Versions may be reused by V2 compiler realization without merging Versioned
  Contract Semantic Identity. Floating `latest/current/range` Version resolution is forbidden.
- Governance Contract judgment remains separate from backend realization. Synchronization, publication, pinning,
  reclamation, caching, query evaluation, graph representation, and distributed protocols may realize Governance but
  cannot create its meaning.
- Detailed Verification subsystem architecture and Diagnostic/Failure mapping are deferred to their owning subsystem
  work; they consume this ADR rather than extending Governance semantics here.

Remaining work is recorded only in the sections that own it.

## 10. Determinism and Verification Boundary

Governance Establishment, source connection, Basis Resolution, Applicability, and Composition obey ADR-0063's semantic
determinism law. Governance cannot use hidden runtime order to settle authority.

The same Governance meaning, Decision Law, complete applicable Decision Basis, and relevant occurrence meaning establish
the same Decision Outcome. The same Scope meaning and relevant composition establish the same membership and Direct
Governance Overlap relation. The same complete unordered conflict set under the same Arbitration Law establishes the
same Arbitration Outcome. Equivalent Binding, Replacement, Withdrawal, and Binding Transition meaning does not vary with
physical realization order.

For one exact predecessor Binding, distinct competing Replacement or Withdrawal meanings do not become deterministic by
choosing a winner; none establishes. For one exact Replacement or Withdrawal, distinct competing Transition cuts
likewise establish none. An already-established succession or Transition relation is immutable semantic history rather
than a physical first-wins result.

The following cannot determine Governance meaning:

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
wall-clock ordering by itself
cache presence or compiler generation
pointer topology or graph traversal order
```

A backend may use those mechanisms only to realize separately declared deterministic Contract meaning.

The Governance model preserves exact semantic coordinates, canonical value equality, and exact direct semantic relations
where they carry Contract meaning. `Definition Reference` and `Occurrence Reference` under ADR-0063 identify exact
authoritative definition meaning or one exact semantic application; they do not require a pointer-bearing or recursively
linked Contract representation. Semantic identity is not derived from fingerprints, digests, ordinals, hashes,
timestamps, physical addresses, intern handles, or compiler structural reuse.

Explicit semantic relations may be viewed as a graph. Semantic-establishment relations are acyclic, while transitive
graphs used for verification, diagnostics, query evaluation, invalidation, or scheduling are derived subsystem
knowledge. Contract meaning is not assigned through recursive traversal, callback evaluation, evaluation order,
recursion recovery, or fixed-point iteration.

Where Contract theory permits several equivalent physical realizations, that freedom belongs to implementation rather
than to ambiguous Governance meaning. Detailed Verification subsystem realization remains outside this ADR as stated in
Section 6.

## 11. Consequences

### Positive

Policy and Governance retain distinct responsibilities. Policy defines complete operating Contract Worlds; Governance
establishes exact Decisions, conflict resolution, Binding, succession, termination, and later applicability transition
without taking ownership of the selected 1D Contracts.

Governance remains explicit without becoming a general controller. Decision Basis uses Established Material supplied
under ADR-0063, Decision Law produces explicit Outcomes, absence is not hidden in `null`, and exact direct semantic
relations preserve attribution for later Diagnostic and V2 analysis without embedding dependency topology into each
Contract material.

Scope is no longer tied to Interface source nesting or a fixed hierarchy. Existing semantic domains keep their original
authority, while Governance can explicitly own a new grouping only when it declares genuinely new governed composition
meaning.

Same-response plurality is preserved where it matters and converges before Binding. Different-response conflict is
resolved before governing authority appears. One predecessor Binding cannot be the subject of competing established
succession or termination meanings, and one succession or termination judgment cannot be the trigger of competing
established Transition cuts. One exact governed application therefore observes one complete Binding, while old and new
applications may coexist without forcing stop-the-world realization.

Immutable occurrence material, exact Versioned semantic coordinates, flat direct semantic relations, canonical value
equality, and acyclic semantic establishment provide a stable substrate for deterministic diagnostics, PBT, future
verification, query-based analysis, incremental reuse, and multi-version backend realization without turning those
compiler techniques into Contract authority.

### Negative

Governance remains Proposed because several concrete design layers are still open. The exact Decision Basis, Decision
Law, Scope, Selection, Arbitration, Binding, Replacement, Withdrawal, Transition, and Policy-reference frontend forms
must still be designed. Multi-part authoring and byte-level canonical encoding also remain open.

Verification subsystem design and Failure/Diagnostic mapping are intentionally not completed here. Later subsystem work
must define exact checking architecture and user-facing treatment for incomplete Basis, `No Selection`, `No Resolution`,
ambiguous Binding applicability, Withdrawal, succession or Transition ambiguity, and occurrence non-establishment
without changing this Governance Contract meaning.

The public Governance control API remains undecided, and the backend still needs a dedicated non-stop-the-world design
for multi-version publication, pinning, distributed preparation, safe mixed-generation realization, and reclamation.

Several earlier ADRs and `What Contract Is` passages still describe Governance as an operation-level refusal authority,
the owner of Manifest Worlds, or an Interface-local selector. Those references require reconciliation after ADR-0056 is
accepted.

### Neutral

This ADR does not require a distributed system, control plane, leader election protocol, transaction manager, actor
system, monitor, or any particular synchronization primitive.

It does not require Governance to discover machine conditions or implement an automatic controller. Governance declares
its required semantic Basis, while observation and acquisition remain replaceable realization. The compiler must reject
implicit or cyclic Contract meaning even when a backend technique could execute it.
