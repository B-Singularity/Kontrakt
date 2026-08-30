# ADR-0056: Governance Contract, Policy-World Control, Whole-Machine Scope, and Selection Boundary

## Status

Proposed

## Date

2026-08-11

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
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

Those worlds can exist as explicit Contract meaning without deciding which one governs an explicit Scope.

Governance owns that Policy-World Binding problem. ADR-0063 supplies the common semantic laws for established material
used by Governance and for later use of Governance results.

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
execution. It establishes the Governance Binding that determines which declared Policy World governs an explicit
boundary.

That requires eight explicit responsibilities:

```text
Decision Basis         what explicit request and situational material one decision judges
Decision Law           how that material determines a declared Governance response
Scope                  what exact governed subject the decision applies to
Selection              which declared Policy World the response establishes
Decision Arbitration   how different-response Direct Governance Overlap is explicitly resolved
Binding                that the Selection applies to that Scope
Replacement            how an existing Binding is explicitly replaced
Withdrawal             how an existing Binding is explicitly removed
```

Governance itself is the Contract Authority for those decisions. External actors, other Contracts, and runtime
mechanisms may carry explicit material toward Governance, but they do not become Governance Authority by doing so.

> Governance is the Contract Authority that establishes and controls which declared Policy World is bound to an explicit
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

An established Binding persists until explicit Replacement or Withdrawal. A later Governance decision does not rewrite
an earlier established Binding. When several independent Contract pipeline flows participate in one already-proceeding
Whole Machine cooperation, realization must preserve the same established Binding across that cooperation rather than
re-resolving the current Governance decision between flows. How that continuity is represented and propagated is
backend-owned. The common non-retroactive law is defined by ADR-0063.

Governance must remain separate from the State-Machine axis. Governance establishes Policy-World Binding; it does not
establish a State or execute a Transition.

Governance must not derive authority from backend mechanisms. Lock order, CAS order, scheduler order, process topology,
service discovery, configuration storage, runtime object identity, and similar realization details cannot decide
Governance meaning.

Governance follows ADR-0063's semantic determinism law. Equivalent Governance meaning must establish the same
authoritative result regardless of source acquisition order, thread timing, hash iteration, or backend representation.

## 4. Contract Decision

### 4.1. Governance Authority

Governance is the Contract Authority that establishes which already-declared Policy World is bound to an explicit
governed Scope.

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

A Governance decision judges one declared Decision Basis.

Decision Basis defines the meaning required by that Governance judgment. It may require explicit request meaning,
situational meaning, or a declared combination of such meaning. These are Governance requirements, not names of the
Contract, State-Machine responsibility, realization component, or other producer that must supply them. They do not
create fixed Governance ontology categories such as `Authorization`, `Readiness`, or `Inhibition`.

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

A source connection is invalid when the material can become applicable only because the same Governance Binding being
decided would first make that source meaning available. Governance cannot establish its own prerequisite through the
Selection it is trying to establish.

**Open in this section:** the exact categories of meaning that Governance may require in Decision Basis and the frontend
form that declares those requirements without naming producer topology remain to be designed.

### 4.4. Decision Law

Decision Law is the user-declared Governance law that judges one complete applicable Decision Basis and determines the
declared Governance response.

Conceptually:

```text
EmergencyRequested
+
explicit situational meaning
        ↓
Governance Decision Law
        ↓
zero or one declared Governance response
```

Decision Law is a deterministic partial law. Under the same Governance definition and the same complete applicable
Decision Basis meaning, it must produce the same Governance result. Zero response is legal when no declared relation in
the law applies. Exactly one response is legal when one declared Governance meaning applies. More than one distinct
response from the same Decision Basis is invalid.

The result is closed over the declared Decision Basis. Material that can affect the Governance judgment must enter
through that Basis under ADR-0063. The Decision Law cannot obtain an additional deciding fact from source discovery,
backend state, a callback, an implementation lookup, or another undeclared path. Missing decision meaning cannot be
repaired by inspecting more of the machine.

Several declared clauses may be satisfied by the same complete Basis, but their evaluation cannot introduce a second
response or an implicit winner. Declaration order, evaluation order, source discovery order, worker completion order,
physical arrival order, and backend iteration order carry no Governance precedence. If no declared relation applies, the
law does not infer a default World or synthesize another response.

Several independently complete Decision Basis applications may each establish their own Governance decision. That is not
an ambiguity in Decision Law. Whether those decisions interact depends on the governed Scope and is handled by the
Direct Governance Overlap and Decision Arbitration laws below.

The Decision Law may express finite, explicit, deterministic, and lowerable conditions over the meaning declared in
Decision Basis. Several source authorities may supply that meaning through composition, but the Decision Law establishes
only Governance meaning. A situation condition may therefore be genuine Governance Contract meaning when the user
declares that relation as part of how the machine responds.

The Decision Law is not a general controller programming surface. It cannot contain arbitrary user algorithms, hidden
runtime queries, scheduler-dependent observation, backend control flow, or undeclared inspection of source internals.
This restriction is semantic rather than syntactic: a frontend form is legal only when it preserves the closed decision
meaning described here.

A Decision Law must not delegate the same decision back into the Policy World whose Binding it is deciding. Material
whose applicability depends on that same Binding cannot establish the prerequisite judgment that causes the Binding.

Governance Contract judgment ends with the Contract meaning of the response. Whether the backend has prepared an image,
distributed a revision, retained an earlier Binding, reached a reclamation point, or can otherwise publish that response
correctly is realization judgment and is not part of the Decision Law.

**Open in this section:** the exact finite Decision Law language and frontend form remain to be designed. The document
also still needs to decide how exact attribution is represented when several declared clauses of one Decision Law are
simultaneously satisfied but all establish the same response.

### 4.5. Governance Scope

Every Governance decision acts over one explicit governed Scope. A Governance Scope is established semantic definition
material that identifies the domain over which that Governance meaning may acquire authority. It is not a runtime
filter, a source container, or a current list of objects.

The earlier rule:

```text
Governance selection scope = exactly one Interface
```

is withdrawn.

One Interface remains a possible Scope, but source containment no longer defines semantic Scope. A Governance
declaration nested inside an Interface, Core, or Whole Machine declaration, if such syntax is retained for authoring
convenience, cannot acquire that Scope merely by nesting.

Scope identity is distinct from the membership used to determine where the Scope applies.

```text
Scope identity
    !=
expanded member set
```

Two separately established Scopes do not become the same Scope merely because their members currently coincide. A Scope
also does not become a different Scope merely because a later compiler representation assigns different ordinals,
addresses, or layout to the same semantic definition.

Governance may refer to a semantic domain whose membership is already established by another authority. In that case the
original authority continues to own that membership meaning; Governance does not re-establish the contents of a Core,
Whole Machine, or another existing domain. When Governance declares a new grouping that has no prior semantic owner, the
new grouping is Governance-owned Scope meaning and Governance owns only that new composition meaning under ADR-0063.

The membership used by Governance must therefore come from established semantic meaning. This ADR does not introduce a
universal `ScopeMembership` record or wrapper. Membership may be part of the established Scope definition or may be
supplied by an established composition owned elsewhere, but source nesting, call structure, runtime reachability,
process placement, object ownership, current machine conditions, or physical pipeline adjacency cannot create it.

For the same established Scope meaning, the same relevant composition, and the same exact governed application,
membership is deterministic and complete. The application is either inside or outside that Scope. An unresolved
membership does not become `outside`; it means the semantic material required to establish or apply that Scope is not
yet complete enough to support Governance authority.

Point-in-time Decision Basis meaning, the currently selected Policy World, and an existing Binding do not mutate Scope
membership. Dynamic situation changes affect Governance judgment through Decision Basis. A different governed domain
requires different established Scope meaning or a different established composition, not an implicit membership change.

Direct Governance Overlap is judged from established Scope meaning when two otherwise-applicable Governance decisions
include the same exact governed application. The overlap relation does not establish a new Scope. Partial, complete, and
contained overlap are shapes of the same relation unless another declared Contract meaning makes that distinction
relevant.

A multi-part Governance meaning may explicitly declare one encompassing Scope when several governed applications must
form one indivisible arrangement. Such a Scope exists only because that grouping was declared and established. Graph
connectivity, repeated co-occurrence, or overlap among independent Scopes cannot create it by inference.

**Open in this section:** the exact allowed Scope kinds, including whether Interface, Flow, Core, Whole Machine, Run, or
another semantic unit is legal, still need to be decided. Their frontend identities and the Governance-owned coordinates
that give Scope definitions stable semantic identity also remain open.

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

Selection identifies one exact declared Policy World response. It does not create Policy meaning, merge Policy contents,
or make the response authoritative for a Scope by itself. Binding remains the Governance result that gives the selected
World governing authority.

A Policy name carries no hidden preference or activation meaning. Names such as `Normal`, `Emergency`, or `Default`,
declaration order, newest Version, filesystem order, registry order, or source proximity cannot choose the Selection.
Unknown or unresolved Worlds cannot be selected by fallback or inference.

Several otherwise-valid Governance decisions may select the same exact Policy World for an overlapping governed
application. Their response is then singular even though the determining Governance judgments remain distinct. Those
judgments are not merged, and their attribution is preserved.

When different selected Worlds are intended to coexist on the same exact governed application, the combined Contract
meaning must already be declared by Policy as its own Policy World. Governance may resolve to that declared World under
Decision Arbitration, but neither Governance nor the compiler may synthesize it from the competing Policy contents.

Governance cannot waive an active Contract. If an exceptional operating arrangement is allowed, that arrangement must
already exist as an explicitly declared Policy World and Governance may select it normally.

**Open in this section:** the exact frontend form and canonical coordinates of Selection remain to be designed after the
Policy World reference and Scope forms are finalized.

### 4.7. Decision Arbitration

Decision Arbitration is the Governance-owned resolution law for different-response Direct Governance Overlap. Several
Governance decisions may coexist without Arbitration. Disjoint governed domains do not compete, and overlapping
decisions that select the same exact Policy World form compatible concurrence rather than a response conflict.

A conflict exists when otherwise-applicable Governance decisions govern the same exact governed application and select
different exact Policy Worlds. Those decisions do not become authoritative Bindings first and get repaired later. The
conflict must be resolved before the affected Governance result can be bound.

Conceptually:

```text
Decision A -> World X ─┐
                       ├─ different-response Direct Overlap
Decision B -> World Y ─┘
                       ↓
             Decision Arbitration
                       ↓
             zero or one response
```

Arbitration judges the complete unordered set of exact competing Governance decisions for one resolution boundary. The
input is not reduced to a set of Policy World names because distinct Governance judgments retain distinct meaning even
when some of them select the same response.

The default resolution boundary is one exact governed application. Overlap graph connectivity does not enlarge it. If A
conflicts with B on one application and B conflicts with C on another, `{A, B, C}` does not become one Arbitration input
merely because the overlap graph is connected.

An explicitly declared indivisible multi-part Governance meaning expands the resolution boundary to its encompassing
Scope. In that case a conflict located in one part is resolved with the complete arrangement rather than by establishing
independent partial results. This expansion comes only from the declared Governance meaning; physical coupling or graph
connectivity cannot infer it.

The complete conflict set contains every otherwise-applicable exact Governance decision for that resolution boundary.
Discovery order cannot cause an early subset to be arbitrated and then revised when another applicable decision is
found. For the same resolution boundary and the same applicable Governance judgments, the Arbitration input is the same
set regardless of declaration, evaluation, arrival, worker-completion, or iteration order.

Arbitration is not a pairwise fold. A law cannot obtain its meaning from whether `(A, B)` happened to be resolved before
`C`, because the grouping of a physical evaluation is not Governance meaning. The complete set is judged as one semantic
input unless the declared Contract meaning itself defines a smaller independent boundary.

For one complete conflict set, Arbitration establishes at most one exact declared Policy World response. The response
may be one of the competing Worlds or a separately declared World that represents their allowed combined Contract
meaning. Automatic Policy composition, generated combined Worlds, and partial Policy merging are forbidden.

If no declared Arbitration law resolves the complete conflict set, the conflict remains unresolved. That state is not
the same as a Decision Law legitimately producing zero response. An unresolved different-response conflict cannot
establish an authoritative Binding for the affected resolution boundary.

Arbitration changes only the semantic domain whose conflict it resolves. It does not rewrite unrelated parts of the
competing Governance decisions. An encompassing multi-part Scope is the explicit exception because its declared meaning
makes the larger arrangement indivisible.

Decision Arbitration resolves Governance decisions only. It does not move machine State, choose between Governance
Contracts, or acquire Policy authority.

**Open in this section:** the exact finite Arbitration language and frontend form remain to be designed. It also remains
to decide whether Governance needs an explicit declared `no Binding` resolution distinct from an unresolved conflict;
this ADR currently gives zero Arbitration response only the meaning of unresolved conflict.

### 4.8. Binding

Selection alone is not enough. Governance establishes a Binding between an exact governed Scope and an exact selected
Policy World after the Governance decision for that boundary is complete.

```text
Scope S
+
resolved Selection A
    ↓
Binding(S, A)
```

Binding is Governance-owned material that establishes which selected Policy World governs its exact Scope. It is not the
general Applicability relation defined by ADR-0063. When another semantic responsibility later depends on a Governance
Binding, ADR-0063 determines whether the exact Binding may participate in that dependent application.

Compatible same-response overlap does not require Arbitration merely because several Governance judgments support the
same effective Policy World. Their judgment identities and attribution remain distinct. Different-response overlap must
first resolve under Decision Arbitration; an unresolved conflict cannot establish contradictory Bindings over the same
resolution boundary.

Binding is not machine movement. It does not establish a State and it is not a State Transition.

An established Binding remains the Governance result for its exact Scope until Governance explicitly establishes a
Replacement or Withdrawal. Elapsed time, restart, configuration reload, threshold change, or backend lifecycle does not
silently create a successor. If time-limited Governance authority is required, the ending condition must participate in
an explicit Governance decision that establishes Replacement or Withdrawal.

ADR-0063's non-retroactive Applicability law applies after such change. Processing already proceeding under an earlier
Binding is not rewritten merely because a later Governance result is established.

**Open in this section:** the exact Binding identity coordinates, frontend form, and canonical shape remain to be
designed. The document also still needs to decide whether compatible overlapping judgments that select the same exact
World are represented by one effective Binding with plural attribution or by several compatible Bindings whose effective
response is singular. The exact semantic boundary at which a later Replacement becomes effective for new Whole Machine
cooperation is also still open and will be decided with Replacement and concurrency.

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

Replacement establishes the later Governance arrangement for that Scope without rewriting the earlier established
Binding. It is not a transition of machine State and does not mutate the Policy World that was previously selected.

Replacement must not divide one already-proceeding cooperation across different Contract Worlds. Processing that belongs
to such a cooperation continues under the Binding already established for it, including later participating pipeline
flows. New cooperation established after the Replacement uses the new Binding.

No threshold crossing, configuration write, runtime restart, newest-loaded value, or backend winner may silently replace
a Governance Binding.

**Open in this section:** the exact Replacement frontend form and the semantic cut between an earlier active Binding and
a successor Binding remain open. The Contract must prevent tearing of one indivisible governed cooperation, but the
precise governed lifetime boundary will be decided after the remaining Governance semantics are closed.

### 4.10. Withdrawal

An established Governance Binding may also be explicitly withdrawn without immediately establishing a replacement.

```text
Binding(S, A)
        ↓
explicit Withdrawal
        ↓
no Governance Binding for later contract processing
```

Withdrawal establishes that the earlier Binding no longer governs later use of that Scope. It does not erase the earlier
Binding, move machine State, or mutate the previously selected Policy World.

If Governance is required for later use of that Scope, absence of a later applicable Binding prevents new affected
contract processing from being established. No hidden fallback World becomes authoritative until another valid Binding
exists. Processing or Whole Machine cooperation already proceeding under the withdrawn Binding is not retroactively
rewritten.

No timeout, process death, missing registry entry, configuration disappearance, or backend cleanup silently withdraws
Governance authority.

**Open in this section:** the exact Withdrawal frontend form remains open. Withdrawal also follows the same unresolved
active-use boundary as Replacement. Whether later processing may proceed with no Binding is governed by the still-open
required or optional Governance law in Section 4.11.

### 4.11. Validity, Singularity, and Complete Decision

An established Governance result must resolve exactly for the semantic boundary it governs.

One complete applicable Decision Basis may establish zero or one response under Section 4.4. Several independent
Decision Basis applications may therefore establish several otherwise-valid Governance decisions in the same Whole
Machine without creating a conflict by their mere existence.

Direct Governance Overlap is evaluated only where those decisions govern the same exact governed application. If every
overlapping decision selects the same exact Policy World, the effective response is singular and the judgments coexist
with their attribution preserved. If different exact Policy Worlds overlap, Decision Arbitration must resolve the
complete conflict set before the affected Governance result can become authoritative.

For one resolution boundary:

```text
no applicable Governance response             -> no response established
same-response Direct Overlap                   -> compatible concurrence
different-response Direct Overlap, resolved    -> one exact response
different-response Direct Overlap, unresolved  -> no Binding for that boundary
ambiguous Decision Law or Arbitration result   -> invalid
```

The selected Policy World must exist, Scope meaning must be established, required membership must resolve completely,
Decision Basis must be complete and applicable, and any required Arbitration must resolve to at most one exact declared
response. Governance may not repair missing or ambiguous material through a hidden default, undeclared fallback, or
runtime arrival order.

A Governance decision becomes authoritative only when its complete Governance meaning is established. Incomplete basis,
unresolved Scope membership, an unresolved conflict, or a partial result of one declared multi-part Governance decision
does not create partial Governance authority.

**Open in this section:** the ADR still needs to decide when Governance is required, optional, or explicitly absent for
a Scope, including Scopes with zero or one selectable Policy World. The Failure meaning associated with missing,
unknown, incomplete, withdrawn, or unresolved Governance material will be aligned with ADR-0057 and the Diagnostic work
after that requirement boundary is fixed.

### 4.12. Exact Attribution

Every established Governance result must be exactly attributable to the Governance meaning that established it. At
minimum, Governance preserves the identity and Version of the governing definition, the exact Scope meaning, the exact
Selection, and the resulting Binding, Replacement, or Withdrawal relation.

When several same-response decisions overlap compatibly, attribution preserves every determining Governance judgment
rather than collapsing them into one anonymous authority. If Decision Arbitration is required, attribution preserves the
complete exact decision set judged by the Arbitration law together with the resolved response. Response singularity does
not erase judgment plurality.

Source-owned material used by Decision Basis keeps the identity, reference, authority, and resolved relation defined by
ADR-0063. This ADR does not introduce a second Governance-specific source identity or provenance system.

Governance owns exact attribution of its own established result to the Governance material that established it.
Presentation, retention, audit storage, and diagnostic lifecycle remain the responsibility of the existing Publication,
Diagnostic Evidence, Diagnostic Retention, and Verification boundaries.

**Open in this section:** the exact Governance-owned semantic identity coordinates for Decision, Scope, Selection,
Arbitration, and Binding material still need to be finalized. Their compiler-local ordinal, fingerprint, storage
address, and generation representation remain outside semantic identity under ADR-0063.

### 4.13. Scope Independence, Direct Governance Overlap, and Cross-Scope Coherence

Governance Scopes are independent unless an explicit Contract says otherwise. Multiple Governance decisions may coexist
in one Whole Machine.

A Direct Governance Overlap exists only where otherwise-applicable Governance decisions include the same exact governed
application in their established Scope meaning. Disjoint Scopes therefore create no Governance overlap. Partial, full,
and contained overlap do not create separate precedence classes by themselves.

Overlap is not conflict. Overlapping decisions that select the same exact Policy World are compatible concurrence. Their
effective response is singular while their determining Governance judgments remain distinct. Overlapping decisions that
select different exact Policy Worlds create a Governance conflict and are subject to Section 4.7.

The Selection for Scope A does not imply, inherit, propagate, override, or constrain the Selection for Scope B merely
because the Scopes are structurally related or belong to one Whole Machine. Structural containment does not make a
Binding for one Scope applicable to another Scope, and an overlap relation does not establish a new Scope or hidden
precedence.

No Direct Governance Overlap does not prove that two independent results are valid together at a higher semantic scope.
If a Selection in Scope A requires, forbids, or constrains a Selection in Scope B as part of a larger composition, that
coherence obligation belongs to the authority that owns the higher-scope composition. Governance does not infer a
cross-Scope compatibility matrix or acquire that composition authority merely because both results occur in one Whole
Machine.

This separates two different questions:

```text
same exact governed application?
    -> Direct Governance Overlap

disjoint Governance domains used together?
    -> higher-scope coherence, when explicitly required
```

Cross-Scope coherence may therefore reject a combination that has no Governance overlap, but that rejection is not a
Governance Arbitration result.

### 4.14. Indivisible Governance Meaning Across Several Parts

Independent Governance Scopes remain independent. Contract-level indivisibility is not created by trying to synchronize
separate Scope Bindings or by taking the transitive closure of an overlap graph.

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

The complete Selection becomes authoritative as one Governance decision for that encompassing Scope. A half-established
subset does not acquire partial Governance authority.

This distinction also separates conflict location from resolution boundary. A conflict may first appear at one exact
member application, but when that member belongs to a declared indivisible multi-part Governance meaning, the complete
encompassing Scope is the resolution boundary. Arbitration must preserve the arrangement as one semantic unit instead of
resolving its parts independently.

Outside such an explicit multi-part declaration, conflict resolution remains local to the exact governed application. A
chain such as `A overlaps B` and `B overlaps C` does not make `A`, `B`, and `C` one indivisible Governance meaning. An
Arbitration result also does not rewrite conflict-free parts outside its resolution boundary.

This law does not make a Binding for the encompassing Scope implicitly propagate into independently governed child
Scopes. The exact Scope and Selection themselves must declare the governed meaning. How a backend makes one such
decision visible across several physical components is implementation.

**Open in this section:** the exact Whole Machine Scope authoring form, multi-part Selection frontend, and canonical
representation of the encompassing arrangement remain to be designed.

### 4.15. Separation from Policy and Other 1D Contracts

Policy owns the contents of a Contract World. Governance owns whether one declared World is bound to an exact Scope.

Governance declares only the meaning required by Decision Basis. Source connection and use follow ADR-0063. Governance
does not name producer topology, rerun the source judgment, reinterpret the source obligation, or acquire the source
authority. It judges only Governance meaning and establishes its own result.

A World change changes the 1D Contracts applicable to contract processing established after that change. Governance does
not perform a second compatibility, migration, or reconciliation judgment.

Material from an earlier World may enter a later flow. The currently applicable Input, Admission, Version, Invariant,
State, Budget, Capacity, and other 1D Contracts judge that material according to their own obligations. If every
applicable obligation is satisfied, processing continues. If one is not satisfied, the owning 1D Contract establishes
the corresponding failure.

Historical World provenance has no implicit meaning. If provenance itself matters, the appropriate Contract must declare
and judge that meaning explicitly.

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

If State-related meaning must affect Governance, Governance declares the required Decision Basis meaning rather than a
State-Machine producer dependency. Composition may satisfy that Basis with State-Machine Established Material.
Governance does not reconstruct State from execution history, events, or backend observation. If a later State-Machine
judgment depends on Governance meaning, each authority keeps its own meaning across that connection.

### 4.17. Governance Is Not an Operation-Level Judgment Slot

Governance is not another ordinary pipeline judge beside Admission, Budget, Capacity, Invariant, State Machine, or
Publication.

The older conceptual form:

```text
Governance admits the contract world
    -> Permit / Refuse continuation
```

is withdrawn.

Governance establishes the required Policy-World Binding before affected contract processing begins. That processing may
fail to be established because required Governance material is invalid, absent, withdrawn, or unresolved, but Governance
is not a generic per-operation `canContinue` Boolean.

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

The Policy Versions identify the Policy Worlds. The Governance Version identifies the Governance law that controls how
they are bound to governed Scopes.

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

**Open in this section:** the exact `.kontrakt` syntax for Governance references remains to be designed.

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
be authoring structure only. The governed Scope must still resolve to established semantic Scope meaning.

Where Scope membership comes from another established domain, the frontend preserves that source authority rather than
copying the membership into a Governance-owned duplicate. Where Governance declares a new grouping, the frontend must
make that grouping explicit enough for Governance to own the new Scope meaning under ADR-0063.

This prevents source layout from acquiring Governance authority and allows the same Governance model to apply beyond one
Interface without hidden Scope inference.

### 5.4. Canonical Governance Material

Canonical Governance material must preserve enough semantic structure for every consumer to reach the same Governance
meaning without reconstructing it from source layout or runtime state. At minimum it must represent Governance identity
and Version, Decision Basis meaning, Decision Law, established Scope identity, the established membership relation or
source reference required by that Scope, exact selectable Policy World references, Selection law, Decision Arbitration
law, Binding law, Replacement law, and Withdrawal law.

The canonical model must also preserve the distinctions established in this ADR. One complete Decision Basis produces
zero or one response. Several independent decisions may coexist. Direct Governance Overlap is based on exact governed
application membership rather than graph connectivity. Same-response overlap keeps plural judgment attribution without
creating a synthetic combined authority. Different-response overlap requires one complete unordered Arbitration input
for the relevant resolution boundary, and an unresolved conflict cannot become a Binding.

An explicit multi-part Scope keeps its encompassing arrangement complete. Canonicalization must not derive that
arrangement from overlap connectivity or create permanent Scope identities for every intersection. Scope identity
remains distinct from its expanded member set, and identical member sets do not collapse separately established Scope
meanings.

Binding persistence, explicit Replacement and Withdrawal, exact Governance attribution, the prohibition on implicit
Scope propagation, and the separation from Policy, State Machine, other 1D Contracts, and ordinary pipeline judgment
slots remain part of the canonical meaning.

Establishment, source authority, semantic identity, exact source reference, Basis Resolution, Applicability, complete
basis, and deterministic composition are common semantic law under ADR-0063. Canonical Governance material must be
compatible with those relations but does not redefine them.

Backend representation remains replaceable after canonical meaning is fixed.

**Open in this section:** the exact Governance-owned identity coordinates, byte-level canonical encoding, compact
reference form, and frontend-to-canonical lowering shape remain to be designed. The compiler architecture must also
decide which ordinary Contract frontend and canonical mechanisms can be reused without aliasing Governance Decision
Basis, Decision Law, Scope, Arbitration, or Binding to another authority. Those choices must preserve the semantic
distinctions above and remain compatible with the V2 stable-identity and incremental architecture requirements.

## 6. Verification

The final Governance verifier must prove that every referenced Policy World resolves exactly and that Governance cannot
construct or modify Policy World contents.

For each Governance definition and application, verification follows the semantic boundaries established above. One
complete applicable Decision Basis may establish at most one response. Scope meaning and every membership required to
apply it must resolve completely. Several independent decisions may coexist, while Direct Governance Overlap is checked
only where those decisions include the same exact governed application.

Same-response overlap is legal and preserves every determining judgment. Different-response overlap requires one
complete deterministic Arbitration input for the relevant resolution boundary. The verifier must reject pairwise or
order-dependent conflict reduction, implicit Policy composition, incomplete conflict sets, transitive conflict closure
that is not declared by a multi-part Scope, and an unresolved conflict that attempts to establish a Binding.

ADR-0063 supplies the common verification law for Establishment, source identity and reference, Basis Resolution,
Applicability, complete basis, observation integrity, source-authority preservation, and deterministic composition. A
Governance application whose required Basis is unresolved, inapplicable, or incomplete cannot acquire partial Governance
authority.

The Governance verifier must also reject:

- implicit Scope inferred from source nesting, request contents, runtime topology, current machine conditions, or
  implementation identity;
- Scope membership inferred from physical structure rather than established semantic meaning;
- two distinct Scope definitions collapsed merely because their expanded members are equal;
- implicit Selection based on declaration order, newest-Version assumptions, names, backend registry order, or
  discovery;
- missing, unknown, or ambiguous Policy World references;
- Decision Laws that use arbitrary user algorithms, hidden runtime queries, implementation lookup, backend control flow,
  physical race order, or undeclared source inspection instead of finite explicit conditions over the declared Decision
  Basis;
- more than one distinct response from one complete Decision Basis;
- a source connection whose meaning becomes applicable only because the same Binding would first make that source
  meaning available;
- different-response Direct Governance Overlap without a complete deterministic Arbitration result;
- Arbitration whose result depends on pairwise grouping, declaration order, discovery order, arrival order, worker
  completion order, or another physical sequence;
- Arbitration that synthesizes or partially merges Policy World meaning instead of selecting one exact declared World;
- an Arbitration result applied outside its exact resolution boundary;
- partial establishment of one declared Governance decision or one declared multi-part arrangement;
- attempts to obtain indivisible Governance meaning by synchronizing independent Scope Bindings instead of declaring one
  complete decision over one exact encompassing Scope;
- hidden fallback, precedence, expiry, Replacement, or Withdrawal rules;
- implicit propagation, inheritance, containment precedence, or override from one Governance Scope to another;
- Governance rules that establish State or execute State Transitions;
- State-Machine rules that implicitly select, arbitrate, replace, or withdraw Governance;
- Governance rules that waive or partially disable an active Policy World instead of selecting another declared World;
- silent Binding expiry or Withdrawal caused only by runtime lifecycle;
- retroactive rewriting of Governance meaning already established for ongoing processing;
- Governance that dynamically selects, replaces, withdraws, or arbitrates between Governance Contracts;
- Governance that dynamically selects its own Version; and
- recursive modeling that aliases Governance Decision Basis, Decision Law, Scope, Arbitration, or Binding to ordinary
  pipeline Admission, Invariant, Publication, or another 1D authority.

If a relation between disjoint Governance Scopes is required by a higher-scope composition, the compiler verifies the
Contract authority that owns that coherence relation rather than inventing a Governance compatibility or propagation
matrix.

Equivalent Governance meaning must produce the same judgment, overlap relation, Arbitration result, and authoritative
response regardless of source acquisition order or backend representation.

Physical races, synchronization, storage, transport, and distributed realization remain implementation concerns. Their
realization must preserve the verified Governance meaning but does not extend this Contract Authority.

**Open in this section:** exact diagnostics and Failure mapping for missing, incomplete, withdrawn, or unresolved
Governance material remain to be aligned with ADR-0057 and the Diagnostic ADRs after the required/optional Governance
boundary is fixed.

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
participate in supplying material, but transport or observation does not create Contract authority. Governance declares
only its required Basis meaning; the common source-material laws are supplied by ADR-0063.

Governance does not own the mechanisms that acquire observations, poll sensors, calculate backend health, prepare
images, coordinate runtime participants, retry, fail over, schedule work, balance load, or recover execution. Raw
realization observation must first receive the semantic meaning required by the Governance judgment under an owning
authority. Backend preparation and publication readiness remain realization conditions even when they determine when an
already-established Governance response can be faithfully realized.

Approval workflow, operator screens, interlock hardware, audit stores, clocks, and timeout mechanisms are likewise not
Governance Authority.

The backend may specialize Policy-World lookup, precompute Bindings, use compact canonical identities, or optimize
Replacement. It may also retain the resolved Binding in internal execution or cooperation context so that participating
pipeline flows do not re-resolve Governance after a Replacement. Such state is realization only: it must not be exposed
as required user Input, Output, Fact, Operation parameters, or user-managed Governance handles.

Those mechanisms must preserve the same canonical Governance meaning.

**Open in this section:** the public Governance control API remains to be designed. It must expose Governance without
turning transport, operator workflow, runtime handles, or realization state into Contract Authority.

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
Several Governance decisions may also apply within one Whole Machine. Disjoint governed domains coexist. Same-response
Direct Governance Overlap is compatible, while different-response overlap must resolve through the complete Arbitration
law before the affected Governance meaning is bound. Later changes do not retroactively rewrite already-established
Governance meaning. Locking, CAS, epochs, transactions, message order, scheduler order, distributed consensus, internal
execution context, or another mechanism used to preserve those rules belongs to implementation.

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

**Open in this section:** the backend realization still needs a separate design for non-stop-the-world World
publication, pinning, distributed preparation, and reclamation of older images. That work must compare the existing
runtime-policy, frozen-image, ContractImage, identity, and future query or incremental mechanisms without turning any of
them into Governance semantics.

## 9. Contract and Implementation Decisions

### 9.1. Decisions Made Here

This ADR currently decides the following boundary:

- Policy declares Contract Worlds; Governance controls their Binding to governed Scopes.
- Governance itself is the Contract Authority for Governance decisions and cannot dynamically govern Governance.
- Governance declares the meaning required by Decision Basis without naming producer topology. Source material is
  supplied under ADR-0063, and Governance does not rediscover or recompute the source judgment.
- Decision Law is a deterministic partial law: one complete applicable Decision Basis establishes zero or one response,
  never several distinct responses.
- Governance Scope is established semantic definition material. Scope identity is distinct from expanded membership, and
  membership used by Governance must come from established semantic meaning.
- Multiple Governance decisions may coexist. Direct Governance Overlap exists only where their governed Scopes include
  the same exact governed application.
- Disjoint Governance Scopes create no Direct Governance Overlap, but their higher-scope coherence is a separate
  composition responsibility when such coherence is required.
- Same-response Direct Governance Overlap is compatible concurrence. The effective response is singular while every
  determining Governance judgment retains its attribution.
- Different-response Direct Governance Overlap is a Governance conflict. Policy meaning is never implicitly composed;
  any permitted combined meaning must already exist as its own declared Policy World.
- Decision Arbitration is a Governance-owned law over one complete unordered conflict set. It does not fold pairwise,
  use physical order, or synthesize Policy meaning. It establishes at most one exact declared Policy World response.
- The default Arbitration boundary is one exact governed application. Overlap graph connectivity does not expand that
  boundary. Only an explicitly declared indivisible multi-part Governance meaning expands it to one encompassing Scope.
- An unresolved different-response conflict cannot establish a Binding for the affected resolution boundary.
- Arbitration and overlap do not create synthetic combined Governance authority or automatic intersection Scopes.
- Binding, Replacement, and Withdrawal remain Governance-owned meaning and do not establish State or mutate Policy
  contents.
- Governance Contract judgment is separate from backend realization judgment; realization readiness cannot select a
  different Policy World or alter the declared Governance response.
- Governance is version-sensitive under ADR-0053, while its own Version is resolved before Governance decisions begin.

Open semantic questions are recorded in the sections that own them.

## 10. Determinism and Verification Boundary

Governance establishment, source connection, Basis Resolution, Applicability, and Composition obey ADR-0063's semantic
determinism law. Governance cannot use hidden runtime order to settle its own authority.

The same Governance definition and complete applicable Decision Basis produce the same Decision Law result. The same
established Scope meaning and relevant composition produce the same membership and Direct Governance Overlap relation.
The same complete unordered Arbitration input under the same Arbitration law produces the same resolved response.

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

The Governance model must provide enough canonical material to identify the required Decision Basis meaning, established
Scope meaning, exact Selection, determining Governance judgments, any complete Arbitration input and result, and the
Binding, Replacement, or Withdrawal relation where each applies. Exact source identity, source reference, resolved
connection, and Applicability of supplied Basis material are preserved under ADR-0063 rather than redefined here.

Where the Contract theory intentionally permits several equivalent physical realizations, that freedom belongs to
implementation rather than to ambiguous Governance meaning.

## 11. Consequences

### Positive

Policy and Governance retain distinct responsibilities. Policy declares operating Contract Worlds; Governance
establishes which declared World is bound to an explicit Scope.

Governance has enough structure to express practical situational response without becoming a general controller.
Decision Basis declares the meaning Governance requires without coupling Governance to producer topology. Source-owned
material may satisfy that Basis without transferring source authority. The user-declared finite Decision Law judges only
that declared Basis. Independent decisions may coexist, and only different-response Direct Governance Overlap requires
explicit Arbitration. Established Bindings cannot silently appear, expire, or change through backend accidents.

Scope is no longer tied to Interface source nesting. Scope identity and established membership are separate, and Direct
Governance Overlap can be judged without creating hierarchy, intersection Scopes, inheritance, or implicit propagation
between Governance Scopes.

Replacement does not split processing or Whole Machine cooperation already proceeding under one Binding across different
Contract Worlds. The backend preserves that Binding internally, while cooperation established after Replacement observes
the new World. Once later processing legitimately begins under that World, its 1D Contracts judge material themselves.
This avoids both Contract-World tearing and a separate cross-World compatibility or migration authority inside
Governance.

The State-Machine axis remains independent, and Whole Machine execution coordination remains outside Governance.

### Negative

Governance remains Proposed because the section-local open questions are not yet closed. They include the exact allowed
Scope kinds and identity coordinates, Decision Basis and Decision Law frontend forms, Arbitration syntax, Binding
cardinality and identity under compatible overlap, required or optional Governance, multi-part authoring, and the
semantic cut for Replacement and Withdrawal.

Several earlier ADRs and `What Contract Is` passages still describe Governance as an operation-level refusal authority,
as the owner of Manifest Worlds, or as an Interface-local selector. Those references require reconciliation after
ADR-0056 settles.

The frontend can no longer rely on Interface nesting alone to define Governance Scope.

### Neutral

This ADR does not require a distributed system, control plane, leader election protocol, transaction manager, actor
system, monitor, or any particular synchronization primitive.

It does not require Governance to discover machine conditions or implement an automatic controller. Governance declares
its required situational meaning, while observation and acquisition mechanisms remain replaceable realization.
Governance itself remains the authority that judges the declared Decision Basis under the declared Decision Law and
establishes the Policy-World Binding.