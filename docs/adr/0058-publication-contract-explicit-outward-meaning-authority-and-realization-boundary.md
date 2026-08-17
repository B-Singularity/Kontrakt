# ADR-0058: Publication Contract, Explicit Outward Meaning Authority, and Realization Boundary

## Status

Proposed

## Date

2026-08-17

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0057: Failure Contract, Explicit Machine Failure, Attribution, and Realization Boundary
- ADR-0056: Governance Contract, Policy-World Control, and Selection Boundary
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

A Contract Machine must control not only what meaning becomes authoritative inside its core, but also what meaning may
leave that core with Contract authority.

The inbound side already follows this law. External material does not become Contract meaning merely because an adapter
or another outside mechanism supplied it. It must cross an explicit Input boundary and become authoritative only through
the Contract processing that owns that meaning.

The outward side requires the same discipline.

```text
outside material
    -> adapter / realization
    -> Input Contract
    -> Contract Machine

Contract Machine
    -> Publication
    -> Output Presentation
    -> adapter / realization
    -> outside world
```

The adapter is not Contract Authority on either side. An inbound adapter cannot decide what external material means to
the core, and an outbound adapter cannot decide what the core is authorized to make authoritative outside itself.

Mature engineering practice separates an established internal condition from the later decision that authorizes an
outward disposition. Evidence that an internal determination exists does not by itself create authority to release an
external result. Authorization is also distinct from the physical act that follows it. Kontrakt requires the same
separation without importing any particular industry's release procedure into the Contract.

ADR-0049 introduced Publication as the outward-claim authority and correctly separated it from Output Presentation. It
also made internal Fact material non-public by default. At that stage, however, Publication was modeled narrowly around
one successful Operation return Fact and exact coordinate relations from that Fact into one selected Output
Presentation.

Later Contract work widened the machine model beyond Operation return Facts, especially through the State-Machine axis
and Failure semantics. ADR-0057 also established that an internal Failure may remain internal or may support a different
outward failure meaning appropriate to an external consumer.

```text
internal Failure
    -> remains internal

or

internal Failure
    -> Publication
    -> outward meaning
```

Publication therefore cannot remain a special path for exposing coordinates of one return Fact. It is the Contract
authority over outward Contract meaning.

That authority begins only from Contract meaning already established by the authority that owns it. Publication does not
repeat the judgment that established that meaning. Its question is narrower: what exact outward meaning, if any, is
authorized from that Contract meaning at this outward boundary?

External effects remain outside that question. Any physical effect on an external system belongs to realization outside
the core. Such machinery may carry an already-authorized outward meaning, but it cannot create or revise Publication
authority.

ADR-0058 refines Publication around that boundary.

---

## 2. Problem

Without a separate Publication Contract, internal meaning can become outward meaning merely because implementation makes
it easy to expose.

A host-language return value may be serializable, an adapter may be able to send it, and an external system may be ready
to receive it. None of those implementation facts answers whether the Contract Machine is authorized to establish an
outward meaning.

If implementation reachability determines publication, the Contract boundary collapses into backend shape.

```text
implementation can expose X
    -> X becomes public
```

must instead be:

```text
Contract authority establishes Contract meaning
        ↓
Publication establishes exact outward authority
        ↓
Output Presentation defines the closed outward shape
        ↓
replaceable realization carries that shape outside
```

The earlier Fact-only Publication model also leaves valid machine outcomes outside the model. A Failure may need an
outward failure meaning even though Failure is not an Operation return Fact. Several distinct Failures may support one
outward meaning without becoming one aggregate Failure. Contract meaning may also contain more detail than the outward
Contract should expose.

Publication must therefore judge outward meaning from Contract meaning already authoritative inside the machine. It must
do so without becoming a second acceptance gate. Whether that Contract meaning should stand as authoritative has already
been decided elsewhere.

The same separation matters at the physical boundary. An external acknowledgement or side effect cannot make an
unauthorized meaning valid after the fact, while failure of later transmission cannot retroactively turn a successful
Publication judgment into a different judgment.

Publication must also avoid deriving authority from unfinished processing. This is not a new completeness test owned by
Publication. Material that has not yet been established by its owning authority is not Contract meaning on which
Publication may depend.

Kontrakt must make outward authority explicit before external implementation can act on it.

---

## 3. Decision Drivers

Publication must remain Contract meaning rather than implementation behavior. The outward boundary mirrors the inbound
boundary: external implementation can carry material, but it cannot determine Contract authority.

Contract meaning is non-public by default. Internal authority and outward authority are different relations, so
successful processing or physical observability cannot silently bridge them.

Publication begins from Contract meaning already established by its owning authority and cannot reopen that judgment.

The Publication relation may expose an outward meaning that differs from the Contract meaning on which it depends. That
relation must be declared as Contract meaning rather than hidden in adapter or serializer behavior.

Outward authority is exact rather than global. The same Contract meaning may support one outward meaning at one boundary
and no outward meaning at another.

Publication judgment must remain inside the Contract world already fixed by the authorities that own its selection and
binding.

Several Contract meanings may participate in one Publication judgment without losing their individual identities. The
resulting outward meaning must follow declared Contract relations rather than runtime discovery order.

Publication and Output Presentation remain separate. Publication owns outward meaning; Output Presentation owns the
closed outward shape. Physical interaction with the outside world belongs to realization.

An unsuccessful required Publication judgment is ordinary Failure under ADR-0057, so Publication needs no parallel
result family for that case.

Determinism remains mandatory. Equivalent authoritative material must establish the same Publication meaning across
valid compiler execution modes.

The model must remain independent of the concrete frontend and backend so that the future IR does not inherit
backend-specific structure.

---

## 4. Contract Decision

### 4.1. Publication Is the Outward Meaning Authority

Publication is the Contract authority that determines whether and what Contract meaning may cross the Contract Machine's
outward boundary as authoritative outward meaning.

```text
Contract meaning
        ↓
Publication judgment
        ↓
authorized outward meaning
```

That judgment applies declared Publication law to Contract meaning. It may preserve the source meaning or establish a
different outward meaning without creating internal Contract meaning owned by another authority.

Publication is not the physical act that carries material outside the core. The term `Publication` names a Contract
authority, not an emission mechanism.

The formal concept is `outward meaning`, not only an outward `claim`. A Core output may carry authoritative intent as
well as descriptive meaning without making its external realization part of the Contract.

### 4.2. Contract Meaning Is Non-Public by Default

No Contract meaning receives outward authority merely by existing.

Neither internal authority nor physical availability implies Publication. Contract meaning therefore remains non-public
unless the bound Publication Contract establishes outward meaning from it.

Implementation reachability cannot create outward meaning where the bound Publication Contract establishes none.

### 4.3. Publication Begins From Contract Meaning

Publication is not restricted to one Operation return Fact.

A Publication Contract may depend only on exact Contract meaning already established by the authority that owns that
meaning. Publication uses that meaning without reopening the judgment that produced it.

```text
owning authority
    -> establishes Contract meaning
        ↓
Publication
    -> judges only the outward relation
```

Publication therefore does not decide whether a candidate should be accepted as core truth. Invariant retains that
authority, while every other Contract meaning remains owned by the authority that established it.

Material that is still provisional, unresolved, or merely observed by implementation is not Contract meaning available
to Publication. The frontend and verifier must preserve that boundary rather than making Publication perform a second
validity check at runtime.

### 4.4. Publication May Establish Different Outward Meaning

Publication is not limited to exposing Contract meaning unchanged.

The Publication Contract may declare that exact Contract meaning supports a different exact outward meaning.

```text
Contract meaning A
        ↓
Publication
        ↓
outward meaning B
```

For example:

```text
CapacityFailure
        ↓
Publication
        ↓
TemporarilyUnavailable
```

The outward meaning does not rename or rewrite the Contract meaning on which Publication depends. An internal Failure
therefore remains the same Failure after Publication establishes a consumer-facing meaning from it.

This authority is deliberately narrow. Publication may establish the declared outward meaning at a different level from
its source, but it may not infer new internal Contract meaning that belongs to another authority.

### 4.5. Publication Is Positive and Boundary-Exact Authority

Publication is explicit positive authority at an exact outward boundary.

The same Contract meaning can have different outward relations at different declared boundaries. Neither structural
similarity nor shared implementation makes those relations interchangeable.

```text
Contract meaning X
    -> outward boundary A
        -> outward meaning Y

Contract meaning X
    -> outward boundary B
        -> no outward meaning
```

A Publication relation is therefore not a global `public` flag on Contract meaning. Adding internal meaning cannot
silently widen an existing outward Contract.

### 4.6. Success and Failure Use the Same Publication Authority

Publication owns the outward surface for successful and unsuccessful Contract meaning.

```text
successful Contract meaning
        ↓
Publication
        ↓
authorized outward success meaning

Failure
        ↓
Publication
        ↓
authorized outward failure meaning
```

An internal Failure may remain entirely internal while Diagnostic processing still records or explains it. When an
outward failure meaning is required, Publication determines that external meaning without changing the internal Failure.

A normal host return path cannot bypass Publication merely because the implementation treats it as success, and an
exception path cannot create a separate implicit publication channel for failure.

### 4.7. Several Contract Meanings May Support One Publication Judgment

One Publication judgment may depend on several Contract meanings.

```text
Failure A
Failure B
Failure C
        ↓
Publication
        ↓
TemporarilyUnavailable
```

This does not create a new Failure identity. Each Failure remains distinct under ADR-0057. Publication evaluates the
declared outward relation using those meanings as its basis and establishes only the outward meaning owned by that
judgment.

### 4.8. Publication Does Not Require Whole Machine Termination

A Publication judgment becomes reachable only after the Contract meaning on which it depends has been established.
Unrelated later processing does not block that judgment merely because the Whole Machine has not terminated.

Publication does not turn missing Contract meaning into a semantic placeholder. The exact interaction between active
processing boundary completion and Publication sites remains subject to later Whole-Machine and frontend refinement.

### 4.9. Established Publication Is Non-Retroactive

Once a Publication judgment establishes an outward meaning, later changes to Contract material do not rewrite that
judgment.

Later processing under changed Contract material may establish a different Publication judgment. The earlier outward
meaning remains the meaning established by the earlier judgment.

### 4.10. Publication Does Not Create a New Scope Ontology

The exact outward boundary is part of Publication meaning, but Publication does not introduce a second general scope
system.

Its boundary is resolved against the machine structure and authorities already defined elsewhere. Runtime topology or
adapter placement cannot invent Publication scope.

### 4.11. Publication and Output Presentation Are Separate

Publication owns outward meaning. Output Presentation owns outward shape.

```text
Publication
    -> authorized outward meaning

Output Presentation
    -> closed outward shape for that meaning
```

A presentation shape may exist without granting Publication authority, and Publication does not acquire authority from
the fact that some representation is easy to produce.

The next ADR finalizes the closure rules between an authorized outward meaning and its selected Output Presentation.

### 4.12. Publication and Diagnostic Meaning Are Separate

Diagnostic Evidence explains machine processing. Its existence does not make it outward Contract meaning.

An established Failure can therefore support Diagnostic processing without Publication. If some diagnostic fact must
become part of an outward Contract meaning, that exposure requires an explicit Publication relation rather than direct
leakage from evidence storage or logging.

The later Diagnostic Evidence / Retention ADR decides what evidence exists and what may survive. Publication remains the
authority over outward Contract meaning.

### 4.13. Publication Does Not Own External Effects

Publication ends before external implementation acquires physical control over the outside world.

```text
Contract Machine
    -> Publication
    -> Output Presentation
    -> adapter / realization
    -> external system
```

An external side effect is not Publication Contract meaning. The external mechanism may realize a published meaning, but
changing that mechanism does not change the Publication Contract when the same outward meaning remains in force.

The external system is not trusted as Contract Authority. Its acknowledgement or physical side effect cannot
retroactively establish, cancel, or rewrite Publication.

If material from the external system must later matter to the Contract Machine, it must enter again through an explicit
inbound Contract boundary.

### 4.14. Inbound and Outbound Authority Are Symmetric

Kontrakt applies the same authority discipline in both directions.

```text
outside -> core
    external representation has no Contract authority
    Input processing establishes internal meaning

core -> outside
    external realization has no Contract authority
    Publication establishes outward meaning
```

This symmetry keeps adapters replaceable and prevents external system vocabulary from becoming hidden Contract meaning.

### 4.15. Publication Judges Within the Bound Policy World

Governance selects and binds the Policy World, and Policy defines its Contract composition. Publication begins after
that choice: it evaluates the bound Publication Contract against authoritative Contract meaning produced within the
machine.

The Publication judgment may evaluate declared criteria over those meanings when they determine outward meaning. It may
not use such criteria to choose another Policy World or alter the binding that placed the Publication Contract in force.

### 4.16. Unsuccessful Required Publication Is Failure

Publication does not define a separate denial or stop result family.

Where bound Contract obligations require an outward meaning and the Publication judgment establishes that the
requirement was not satisfied, ADR-0057 governs the resulting Contract Failure. Publication does not create a parallel
failure vocabulary for that case.

Later physical realization failure remains separate. If an already-authorized outward meaning cannot be realized and
Kontrakt can establish that required realization did not complete, that is Realization Failure under ADR-0057 rather
than a revision of the Publication judgment.

---

## 5. Authored and Canonical Publication Material

### 5.1. Publication Is Explicitly Authored Contract Meaning

Unlike Failure, Publication is not intrinsic meaning that can always be derived from another authority.

The existence of internal meaning does not determine whether it may leave the Contract Machine or what outward meaning
should be established from it. Those are application-specific Contract choices, so Publication requires explicit
authoring.

The frontend must let the user declare that relation without requiring the implementation mechanism that realizes it.
The exact IDL placement and reusable declaration form remain open until the semantic model is complete.

A declaration should reference authoritative material already known to the compiler rather than forcing users to repeat
it for backend convenience.

### 5.2. Canonical Publication Material

Canonical Publication material must preserve enough information to reproduce the exact outward judgment without relying
on backend behavior.

At minimum, the semantic model must preserve:

```text
exact Publication declaration
exact Contract meaning or exact set of Contract meanings
exact authorized outward meaning
exact outward boundary
```

These requirements do not prescribe a physical record layout. The backend may represent them through canonical
references or another deterministic compact form as long as no semantic distinction disappears.

Static Publication declaration material and invocation-specific Publication judgment material should remain separable
where the compiler architecture permits it. The compiler must preserve the binding dependency that made the exact
Publication declaration part of the selected Policy World without turning that dependency into a Publication-owned
context object. Reuse is valid only while semantic identity and dependencies remain exact.

### 5.3. Backend Vocabulary Is Not Publication Authority

No host-language or adapter vocabulary defines Publication meaning.

Backend vocabulary may carry stable references to canonical Publication material, but replacing the implementation must
not change the Contract when the same semantic relation remains valid.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Meaning and Representation

The Publication Contract establishes exact outward authority from Contract meaning. Here, Contract meaning is meaning
established under Contract authority, not its compiler or runtime representation.

Publication does not establish the Contract meaning on which it depends. The owning authority establishes that meaning
first, while presentation and physical execution follow Publication under their own boundaries.

### 6.2. Realization

A backend realizes an already-established Publication law.

It may lower Publication into any deterministic mechanism that preserves the same judgment. A valid backend may also
eliminate a runtime Publication object entirely.

Publication is not modeled as a host-language call wrapper with pre/post checks, whether implemented through proxies or
interception. Such machinery cannot define Contract authority.

Physical realization cannot acquire hidden Contract meaning or consult external implementation to decide what should be
public. The semantic judgment must already be determined by canonical Contract material.

### 6.3. Output Presentation Realization

Publication authorization and Output Presentation formation remain distinct even when an optimized backend realizes them
through one physical path.

```text
semantic Publication judgment
        +
semantic Output Presentation contract
        ↓
optimized physical realization
```

Fusion is valid only when both Contract authorities remain semantically recoverable and neither acquires authority from
the other's implementation.

### 6.4. Adapters and External Systems

Adapters stand outside the Contract authority defined here.

An adapter may translate a closed Output Presentation into the mechanism expected by an external system. It may also
translate later external material toward a new Input boundary. Those translations are implementation.

The adapter cannot widen published meaning or reinterpret external acknowledgement as a change to Publication. If a
required external realization does not complete and Kontrakt can establish that fact, ADR-0057 governs the resulting
Realization Failure.

Where execution disappears before the relevant realization outcome can be established, ADR-0057's Crash and
indeterminate-outcome boundary still applies.

### 6.5. Backend Architecture Constraints

This ADR does not choose the concrete Publication IR or its backend representation.

Any backend design must preserve the semantic distinctions established by this ADR and the dependencies needed to
reproduce them deterministically.

Publication lowering cannot derive semantic authority from backend discovery or external-system state.

Caching is reuse rather than authority. A cached Publication compiler product may be reused only while the canonical
material on which its judgment depends remains valid.

V1 may realize Publication through ordinary generated JVM boundaries. V2 may specialize or incrementally reuse those
results, but the Publication Contract must remain unchanged across backend choices.

---

## 7. Verification, Determinism, and Incremental Extensibility

The compiler must resolve every Contract meaning referenced by Publication and every target to an exact declared outward
meaning. Implementation-only coordinates cannot substitute for either side of that relation.

Publication declarations that depend on provisional or unresolved material are invalid. This is a compile-time authority
error where the relation is statically knowable, not a reason to add a runtime validation phase to Publication.

Where several Contract meanings participate in one judgment, the dependency relation must be explicit. Parallel
execution may change when those meanings become physically available, but it cannot change which meanings participate.

Once the authoritative basis and exact Publication relation are the same, every valid compiler and backend execution
must establish the same Publication result.

```text
clean full compilation
incremental compilation
cache reuse
recomputation
single-threaded execution
parallel execution
```

remain semantically equivalent for Publication.

When more than one declared Publication relation can be satisfied at one outward boundary, runtime ordering has no
Publication authority. Declaration order therefore cannot silently become first-match semantics.

Future incremental invalidation must follow the canonical dependencies that determine Publication. A change outside
those dependencies cannot alter Publication semantic identity by itself.

Persisted Publication IR and generated artifacts remain implementation material. Their storage schema or cache version
is separate from Contract Version.

Malformed Publication declarations are compile-time invalidity. Runtime Failure is reserved for unsuccessful machine
judgments and realizations established during actual Contract Machine processing under ADR-0057.

---

## 8. Deferred Decisions

Further work remains in the canonical frontend model. The exact declaration form must preserve Publication judgment
without turning it into a general-purpose rule language. Its ownership within the IDL also remains to be decided after
the semantic model is stable.

The semantic boundary of Publication criteria remains open. The language must be expressive enough to judge declared
relations over Contract meaning without reintroducing algorithmic host-language control flow as Contract authority.

The semantics of simultaneous Publication relations remain open. ADR-0058 does not yet decide whether multiple results
may coexist or require an explicit combining law, and it does not assign semantic priority between them.

The distinction between permission to establish outward meaning and an obligation to establish it also remains open.
Failure already governs an unsuccessful required Contract judgment, but the frontend model need not introduce separate
Publication categories before that distinction proves necessary.

The relation between Publication sites and active processing boundary completion needs additional Whole-Machine work,
particularly for long-running or independently publishable processing. ADR-0058 establishes that Publication can depend
only on Contract meaning already established by its owning authority; it does not yet choose the compiler structure that
makes every valid publication site reachable.

Output Presentation still owns the next unresolved boundary. The following ADR must determine how an authorized outward
meaning closes over a presentation without moving structural representation back into Publication.

Diagnostic Evidence / Retention may later introduce explicit outward use of selected evidence. That work must preserve
the rule that evidence is not public merely because it exists or is retained.

The redesigned frontend and IR must eventually choose a concrete representation for Publication and its dependencies.
Once that work begins, ADR-0049 and older documents must be revised where they still assume the
Operation-return-Fact-only Publication model.

These deferred decisions do not reopen Publication's separation from Contract acceptance, Output Presentation, or
external realization.

---

## 9. Consequences

### Positive

Publication is no longer tied to the shape of one Operation return Fact, so the future frontend can model outward
authority directly instead of reconstructing it from carrier structure.

Keeping prior Contract establishment outside Publication prevents the new Contract from duplicating Invariant or the
State-Machine axis. The same separation also allows Failure to remain intact when a different consumer-facing meaning is
published from it.

External adapters remain replaceable because the semantic handoff is complete before their implementation begins. This
gives the later Output Presentation and backend work a stable boundary.

The resulting canonical dependencies can be reused by incremental compilation without granting authority to cache or
execution state.

### Negative

ADR-0049 is now too narrow where it treats one Operation return Fact as the sole basis for Publication and binds
Publication closely to Output Presentation coordinates. Those parts must be revised after ADR-0058 is accepted.

The more general Publication model requires a new canonical frontend representation before the redesigned IR can be
finalized.

Long-running and independently publishable machine work still requires a precise relation between active processing
boundary completion and Publication reachability.

### Neutral

Publication remains user-authored Contract meaning because outward authority cannot generally be derived from internal
meaning alone.

This ADR fixes the Publication boundary while leaving the remaining contracts and compiler representation to their
owning work.