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

The inbound side already follows this law. External material does not become Contract meaning merely because an adapter,
serializer, host object, database row, network message, or another outside mechanism supplied it. It must cross an
explicit Input boundary and become authoritative only through the Contract processing that owns that meaning.

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
the core, and an outbound adapter cannot decide what the core is authorized to say outside itself.

ADR-0049 introduced Publication as the outward-claim authority and correctly separated it from Output Presentation. It
also made internal Fact material non-public by default. At that stage, however, Publication was modeled narrowly around
one successful Operation return Fact and exact coordinate relations from that Fact into one selected Output
Presentation.

Later Contract work widened the machine model. State-Machine processing, Policy, Governance, Version, Budget, Capacity,
and especially Failure can establish machine meaning that cannot be reduced to one Operation return Fact. ADR-0057 also
established that an internal Failure may remain internal, may be published directly, or may support a different outward
failure meaning appropriate to an external consumer.

```text
internal Failure
    -> remains internal

or

internal Failure
    -> Publication
    -> outward Failure meaning

or

internal Failure
    -> Publication
    -> different outward meaning
```

Publication therefore cannot remain a special path for exposing coordinates of one return Fact. It is the Contract
authority over outward machine meaning.

This does not make external effects part of the Publication Contract. A database write, Kafka emission, file operation,
network send, actuator command, serializer invocation, or another physical action outside the core is realization. Such
machinery may carry an already-authorized outward meaning, but its existence, capability, success, failure, transaction,
or transport semantics cannot become Publication authority.

ADR-0058 refines Publication around that boundary.

---

## 2. Problem

Without a separate Publication Contract, internal meaning can become outward meaning merely because implementation makes
it easy to expose.

A host-language return value can be serialized. A serializer can see a field. An adapter can send a message. A database
client can execute a statement. A generated API carrier can contain a value. None of those facts answers the Contract
question of whether the machine is authorized to make that meaning outwardly authoritative.

If implementation reachability determines publication, the Contract boundary collapses into backend shape.

```text
implementation can expose X
    -> X becomes public
```

must instead be:

```text
machine establishes internal meaning X
        ↓
Publication judges whether outward meaning Y is authorized
        ↓
Output Presentation defines the closed outward shape
        ↓
replaceable realization carries that shape outside
```

The earlier Fact-only Publication model also leaves several cases unresolved.

An established Failure may need to become an outward failure outcome even though Failure is not an Operation return
Fact. Several independently established Failures may support one outward meaning without becoming one aggregate Failure.
An internal meaning may be too detailed, too implementation-specific, or simply different in purpose from the meaning
the machine should expose externally. Conversely, some established internal meaning may have no outward authority at
all.

Publication must therefore answer two questions explicitly:

```text
may anything be stated outwardly from this established machine meaning?

if so, what exact outward meaning is authorized?
```

It must answer those questions without becoming Output Presentation, a serializer, a transport, an adapter, a database
transaction, an external effect system, a diagnostic channel, or a second Failure taxonomy.

Publication must also avoid premature outward authority. Material can physically exist before the machine has finished
the processing required to establish the meaning on which the outward claim depends. Publication cannot allow a later
independent or required judgment to retroactively invalidate an already-authorized claim merely because the claim was
made too early.

At the same time, Publication cannot require universal Whole Machine termination. A long-running machine may establish
an outward meaning whose complete authoritative basis is already fixed even while unrelated later processing remains
possible. Publication finality must therefore follow the exact meaning being published rather than an implementation
notion of process completion.

Finally, external implementation must not be allowed to reintroduce authority after the Contract boundary. The fact that
a database commit, broker acknowledgement, file rename, socket write, or external service call succeeds does not create
or amend the Publication judgment. Those are realization events outside the Contract authority defined here.

Kontrakt must make the outward authority explicit before that implementation begins.

---

## 3. Decision Drivers

Publication must remain Contract meaning rather than implementation behavior.

The outward boundary must mirror the inbound boundary. External material is untrusted as Contract meaning on entry, and
external realization is untrusted as Contract authority on exit. Adapter code may acquire, encode, transmit, persist,
translate, or otherwise realize material, but it cannot decide what the Contract Machine knows, accepts, publishes, or
means.

Established machine meaning is non-public by default. Core membership, successful processing, Failure importance,
serializer reachability, host return compatibility, persistence, cache presence, transport support, or backend
capability grants no outward authority.

Publication may consume only authoritative machine meaning already established by the Contract Machine. Temporary
values, proposals, unresolved judgments, backend observations, cache entries, exceptions, stack traces, adapter state,
and other implementation material cannot become Publication source authority merely because they are available at
runtime.

Publication is not limited to successful Operation return Facts. Any source kind accepted by Publication must retain its
own authority and identity. Publication does not turn Failure into Fact, State into Fact, realization evidence into
Failure, or several sources into a new aggregate source merely to make outward publication convenient.

Publication may authorize outward meaning that differs from its internal source meaning. That mapping is itself Contract
meaning. It cannot be delegated to a mapper callback, serializer, exception handler, adapter, transport, or other
backend code.

```text
internal meaning
    -> Publication relation
    -> outward meaning
```

The outward meaning does not rewrite its source. Internal Failure remains the same Failure after Publication authorizes
`TemporarilyUnavailable`; an internal successful meaning remains the same internal meaning after Publication authorizes
a consumer-facing success result.

When several established meanings participate in one Publication judgment, their semantic relation must be explicit and
deterministic. First-discovered Failure, first worker completion, exception arrival order, hash iteration, cache order,
or another implementation accident cannot decide which outward meaning is authorized.

Publication must judge only meaning whose authoritative basis is complete enough for the exact outward meaning being
published. It must not expose provisional material while unresolved processing can still change the required basis of
that claim. This does not require unrelated future processing or the Whole Machine itself to terminate.

Publication and Output Presentation remain separate. Publication decides whether and what outward meaning is authorized.
Output Presentation decides the closed outward shape in which that authorized meaning may appear. Physical formation,
serialization, transmission, persistence, database interaction, broker interaction, or another external effect remains
realization.

Publication does not select Policy World, Governance Binding, Contract Version, State, or Transition. Those authorities
remain separate. Their already-established material may be applicable context for Publication where the declared
Publication meaning depends on it.

Publication must remain deterministic across valid compiler and backend executions. The same authoritative machine
material and applicable Contract context must establish the same Publication result regardless of discovery order,
thread scheduling, worker arrival, allocation address, hash iteration, runtime registration, cache population, or
backend representation.

V1 does not require the final Publication IR, persistent incremental artifacts, or a specific physical egress mechanism.
The model must nevertheless remain compatible with V2 incremental compilation. Publication identity and dependencies
cannot depend on hidden mutable compiler state or backend-only structure that would make an incremental build
semantically different from a clean full build.

---

## 4. Contract Decision

### 4.1. Publication Is the Outward Meaning Authority

Publication is the Contract authority that determines whether and what established machine meaning may cross the
Contract Machine's outward boundary as authoritative outward meaning.

```text
established machine meaning
        ↓
Publication judgment
        ↓
authorized outward meaning
```

Publication is not the physical act of sending, persisting, serializing, returning, printing, committing, emitting, or
otherwise moving material outside the core.

The term `Publication` therefore names a Contract authority, not a transport operation.

### 4.2. Established Meaning Is Non-Public by Default

No established machine meaning receives outward authority merely by existing.

The following do not authorize Publication:

```text
core membership
Fact authority
successful Operation completion
Failure establishment
State establishment
Transition completion
persistence
cache presence
host return compatibility
serializer reachability
adapter capability
transport availability
external-system support
```

If no applicable Publication Contract authorizes an outward meaning, no authoritative outward meaning exists for that
source under that Publication boundary.

This absence does not require a second runtime result such as `PublicationStop`, `Blocked`, `Skipped`, or `Deferred`.
Publication simply has not authorized an outward meaning.

### 4.3. Publication Source Is Established Machine Meaning

Publication source authority is not restricted to one Operation return Fact.

A Publication Contract may refer only to exact machine meaning already established by the authority that owns that
meaning. Publication consumes that meaning without rejudging its source authority.

```text
Contract authority
    -> established Contract meaning

State-Machine authority
    -> established State-Machine meaning

Failure
    -> established Failure meaning

Publication
    -> may use explicitly declared applicable source meaning
```

The exact admissible source categories and their frontend syntax are decided with the Publication frontend and canonical
model, but implementation artifacts are never source authority.

A raw exception, stack trace, runtime object, temporary value, cache record, serializer field, thread state, socket
state, database status, or adapter callback cannot become Publication source merely because it is observable.

### 4.4. Publication May Establish Different Outward Meaning

Publication is not limited to exposing its source unchanged.

The Publication Contract may declare that one exact established internal meaning supports a different exact outward
meaning.

```text
internal meaning A
        ↓
Publication
        ↓
outward meaning B
```

For example:

```text
DatabaseWriteRealizationFailure
        ↓
Publication
        ↓
TemporarilyUnavailable
```

The outward meaning is not a renamed Failure and does not rewrite the internal Failure. Publication establishes only the
outward meaning that the machine is authorized to make available beyond its outward Contract boundary.

The same rule applies to successful internal meaning. Internal success does not automatically define its outward success
surface.

Publication therefore owns the relation between authoritative internal meaning and authoritative outward meaning. A
mapper implementation may realize that relation physically, but it cannot invent, widen, narrow, or replace it.

### 4.5. Publication Is Positive Outward Authority

Publication is explicit positive authority.

An internal coordinate, Failure detail, State, Version, Policy material, Governance material, diagnostic observation, or
other machine meaning omitted from the applicable Publication relation receives no outward authority through that
relation.

Equal names, equal host types, serializer compatibility, structural similarity, reflection visibility, common package
membership, or existing adapter support do not create an implicit Publication relation.

Adding new internal machine meaning does not silently enlarge an existing outward contract.

```text
new internal material
    -> remains non-public

until

Publication Contract
    -> explicitly grants the required outward meaning
```

### 4.6. Success and Failure Use the Same Publication Authority

Publication owns the outward success and failure surface.

Failure does not create a separate outward error channel, and successful machine meaning does not bypass Publication
merely because a host API expects a normal return value.

```text
established success meaning
        ↓
Publication
        ↓
authorized outward success meaning

established Failure
        ↓
Publication
        ↓
authorized outward failure meaning
```

An internal Failure may remain entirely internal. Diagnostic processing may still observe and explain it without
Publication.

When outward failure meaning is required, Publication decides what may be exposed. That meaning may preserve the exact
internal Failure, may expose only a declared portion of its meaning, or may establish a different consumer-facing
meaning, subject to the final Publication model and Output Presentation contract.

### 4.7. Multiple Sources Do Not Become Aggregate Failure

One Publication judgment may need to consider several already-established machine meanings.

For example, one active processing boundary may have established several independent Failures under ADR-0057.
Publication may use that complete applicable source set to determine one outward meaning.

```text
Failure A
Failure B
Failure C
        ↓
Publication
        ↓
TemporarilyUnavailable
```

This does not create `AggregateFailure`, `PrimaryFailure`, `RootFailure`, or another Failure identity.

Every internal Failure remains distinct and retains its exact source, failure meaning, applicable context, and active
boundary. Publication owns only the outward relation.

Where multiple sources participate, the declared Publication law must make the relation deterministic. Worker order,
first Failure, first exception, source discovery order, collection iteration, or another backend accident cannot choose
the outward meaning.

### 4.8. Publication Requires Meaning Complete for the Exact Outward Claim

Publication cannot authorize outward meaning from material that remains provisional for that exact claim.

The source meaning and every authoritative dependency required by the Publication relation must already be established.
If unresolved processing can still change whether the outward meaning is valid, Publication has not yet reached a valid
judgment boundary for that meaning.

```text
required source meaning established
+ every required dependency for this outward meaning resolved
        ↓
Publication may judge
```

This is claim-relative completion, not universal process termination.

Unrelated later processing does not prevent Publication merely because the machine remains alive or another independent
flow may continue. Conversely, physical availability of a partial result does not permit Publication while required
machine processing for that exact outward meaning remains unresolved.

The exact relation between active processing boundary completion and each Publication site remains subject to further
frontend and whole-machine refinement, but implementation timing cannot replace this semantic requirement.

### 4.9. Established Publication Is Non-Retroactive

Once a Publication judgment establishes an outward meaning under its exact applicable Contract context, later unrelated
machine changes do not rewrite that already-established publication.

A later Version, Policy World, Governance Binding, State, Transition, or independent machine result may affect a later
Publication judgment, but it does not retroactively change the Contract meaning under which an earlier publication was
established.

The applicable context required to interpret Publication must therefore remain recoverable from the authoritative
material that applied when the Publication judgment was established.

This does not require every Publication to carry one universal fixed context schema. Only context actually applicable to
the Publication meaning participates semantically.

### 4.10. Publication Does Not Create a New Scope Ontology

Publication applies at an explicit outward Contract boundary, but it does not introduce a second general scope system.

The boundary identifies where authoritative outward meaning is established relative to the machine structure already
defined by Interface, Core, Operation, Whole Machine composition, Governance Scope, and the other existing authorities.

Publication must not infer its subject or authority from package nesting, runtime topology, transport endpoint, adapter
instance, process boundary, service boundary, or network location.

### 4.11. Publication and Output Presentation Are Separate

Publication owns outward meaning.

Output Presentation owns outward shape.

```text
Publication
    -> whether an outward meaning is authorized
    -> what outward meaning is authorized

Output Presentation
    -> the closed external shape in which that meaning may appear
```

Publication does not own JSON fields, HTTP status codes, protobuf layouts, CLI text, database columns, serializer
schema, byte encoding, network frames, or another physical presentation mechanism merely because one realization uses
them.

Output Presentation likewise cannot grant Publication authority. A presentation shape may exist while no outward meaning
is authorized for the current machine result.

The exact closure rules between Publication meaning and Output Presentation coordinates are finalized by the Output
Presentation ADR.

### 4.12. Publication and Diagnostic Meaning Are Separate

Diagnostic Evidence explains machine processing. Publication decides what meaning may cross an outward Contract
boundary.

Diagnostic Evidence is not automatically public merely because it exists, is retained, or is useful to an operator.
Likewise, an established Failure does not require Publication merely to be available to Diagnostic processing.

```text
Failure
    -> Diagnostic Evidence / Retention

Failure
    -> Publication, only when outward meaning is authorized
```

A stack trace, backend exception, actual violating value, runtime location, external-system response, or another piece
of Diagnostic Evidence cannot silently become an outward Contract claim.

The later Diagnostic Evidence / Retention ADR decides what evidence exists and what may survive. Publication remains the
only authority for turning machine meaning into an outward Contract meaning.

### 4.13. Publication Does Not Own External Effects

Publication ends before external implementation acquires physical control over the outside world.

A database write, transaction commit, Kafka or message-broker emission, filesystem mutation, HTTP transmission, socket
write, external API call, actuator command, process invocation, or another outside effect is not Publication Contract
meaning.

```text
Contract Machine
    -> Publication
    -> Output Presentation
    -> adapter / realization
    -> external system
```

The adapter may realize the published meaning using any compatible implementation. Replacing JDBC with another database
client, Kafka with another broker, HTTP with another transport, or one external service with another must not change the
Publication Contract merely because the physical realization changed.

The external system is not trusted as Contract Authority. Its acknowledgement, transaction state, error code, response,
or physical side effect cannot retroactively create, cancel, or rewrite a Publication judgment.

If external realization later returns material that must matter to the Contract Machine, that material must re-enter
through the appropriate explicit inbound Contract boundary rather than acquiring authority directly from the external
implementation.

### 4.14. Inbound and Outbound Authority Are Symmetric

Kontrakt applies the same authority discipline in both directions.

```text
outside -> core
    outside representation has no Contract authority
    Input processing establishes internal meaning

core -> outside
    outside realization has no Contract authority
    Publication establishes outward meaning
```

An adapter may translate representation on either side, but translation capability is not authority over machine
meaning.

This symmetry prevents external frameworks, serialization systems, databases, message brokers, network protocols, host
classes, or generated APIs from becoming hidden parts of the Contract.

### 4.15. Policy, Governance, Version, and State Remain Separate Authorities

Publication does not choose the Policy World under which it operates, select Governance, bind a Version, establish a
State, or execute a Transition.

Where Publication meaning depends on those authorities, it uses their already-established applicable material.

```text
Governance
    -> applicable Policy World

Version
    -> applicable Contract definition

State / Transition
    -> applicable machine condition or movement

Publication
    -> judges outward meaning under that already-established context
```

Different Policy Worlds may therefore select or parameterize different Publication Contracts or outward relations where
that variation is explicitly declared, but Publication itself does not decide which World governs.

### 4.16. Publication Does Not Own Recovery

Publication states outward meaning; it does not recover from the internal condition that produced that meaning and does
not repair an external realization that later fails.

A later recovery action may establish new machine meaning and may lead to a later Publication judgment. That later
publication does not rewrite the earlier one.

---

## 5. Derived and Canonical Publication Material

### 5.1. Publication Is Explicitly Authored Contract Meaning

Unlike Failure, Publication is not intrinsic meaning that can always be derived from another authority.

The fact that internal meaning exists does not determine whether that meaning is allowed to leave the Contract Machine,
and it does not determine what consumer-facing meaning should be authorized. Those are application-specific Contract
choices.

Publication therefore requires explicit user-authored Contract meaning.

The frontend must let the user declare the Publication relation without requiring the implementation mechanism that
realizes it. The exact IDL placement, operation/interface ownership, reusable declaration form, and syntax remain open
until the semantic model is complete.

The declaration must not require users to restate source meaning that is already canonically available merely to satisfy
implementation convenience. It must refer to exact authoritative machine material and exact outward meaning.

### 5.2. Canonical Publication Material

Canonical Publication material must preserve enough information to reproduce the exact outward judgment without relying
on backend behavior.

At minimum, the canonical model must preserve the semantic relation among:

```text
exact Publication declaration
exact established source meaning or source set
exact authorized outward meaning
applicable Publication conditions or alternatives, when declared
applicable Contract context required by that Publication judgment
exact outward boundary to which the Publication authority applies
```

These are semantic requirements, not a required physical record layout.

The backend may use canonical references, interned identities, compact indexes, tables, primitive arrays, shared
context, or another deterministic representation. Physical deduplication does not permit semantic information to
disappear.

Publication identity cannot depend on source discovery order, object identity, generated method identity, adapter
registration order, serializer field order, transport order, or cache population order.

Static Publication declaration material and invocation-specific Publication judgment material must remain separable
where the backend architecture permits it. A compiler may freeze and cache declaration relations independently from the
active machine material to which those relations are later applied.

### 5.3. Backend and External Vocabulary Are Not Publication Authority

A host-language return type, exception type, HTTP status, protobuf message, serializer field, database statement, broker
topic, file path, generated adapter method, or runtime callback does not define Publication meaning.

Replacing any of those implementation mechanisms must leave the Publication Contract unchanged when the same outward
meaning and Output Presentation remain valid.

Backend or adapter vocabulary may carry stable references to Publication material, but it cannot become the source of
that authority.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Meaning

The Publication Contract requires authoritative outward meaning to be explicit.

It decides whether established machine meaning may cross an outward Contract boundary and, when authorized, what exact
outward meaning is established there.

Publication does not physically emit that meaning, define its final external shape, establish its internal source,
create Failure, select Governance or Policy, choose Version, establish State, retain Diagnostic Evidence, or perform
external effects.

### 6.2. Realization

A backend realizes an already-established Publication law.

The backend may lower a Publication judgment into direct branches, tables, generated adapters, specialized mappings,
compact identities, precomputed alternatives, or another deterministic mechanism. None of those physical forms becomes
Contract Authority.

A valid backend may avoid allocating a Publication object entirely. It may also share static Publication material across
operations or compilation products when that sharing preserves exact semantic identity and dependency boundaries.

Physical realization begins from already-established machine meaning and an already-resolved Publication Contract. It
cannot acquire hidden source material, query an external service for additional Contract meaning, inspect arbitrary
runtime objects, or use serializer behavior to decide what should be public.

### 6.3. Output Presentation Realization

Publication authorization and Output Presentation formation are separate even when one optimized backend realizes them
in one physical path.

A backend may fuse physical steps only when the fused realization preserves both authorities exactly.

```text
semantic Publication judgment
        +
semantic Output Presentation contract
        ↓
optimized physical realization
```

Optimization cannot make presentation reachability grant Publication authority or allow Publication to invent an
undeclared presentation coordinate.

### 6.4. Adapters and External Systems

Adapters stand outside the Contract authority defined here.

An adapter may translate a closed Output Presentation into HTTP, gRPC, database commands, broker messages, files, IPC,
CLI output, or another external mechanism. It may also translate external material back toward a later Input boundary.

Those translations are implementation.

The adapter cannot widen the published meaning, acquire additional internal machine meaning, suppress required outward
meaning while claiming semantic success, or reinterpret external acknowledgement as a change to the already-established
Publication judgment.

Where an adapter or external system fails during required realization and Kontrakt retains enough authority to establish
that required realization did not complete, ADR-0057 governs Realization Failure. The Publication judgment itself does
not become a Failure merely because later physical transmission failed.

If the relevant realization disappears before its outcome can be established, ADR-0057's Crash and indeterminate-outcome
boundaries remain applicable. Publication does not fabricate semantic completion from missing physical evidence.

### 6.5. Backend Architecture Constraints

This ADR does not choose the concrete Publication IR, table layout, identifier encoding, generated port ABI, cache
format, mapper shape, branch structure, adapter API, or Output Presentation carrier representation.

Any backend design must preserve explicit positive authority, exact source dependencies, deterministic outward meaning,
non-public default behavior, and the separation among Publication, Output Presentation, Diagnostic processing, and
external realization.

Publication lowering must not depend on reflection scanning, runtime registration order, class identity, mutable global
registries, adapter discovery, serializer behavior, or external-system availability to decide semantic publication.

A backend optimization may eliminate Publication work only when the same outward judgment is provably established or
provably impossible from canonical machine material. It cannot infer an outward relation merely because the physical
representation happens to contain compatible fields.

Caching is reuse, not authority. A cached Publication-related compiler product may be reused only while its
authoritative sources, Publication declaration, applicable context dependencies, and Output Presentation dependencies
remain valid. Cache presence cannot establish an outward meaning by itself.

Kontrakt's existing deterministic canonicalization, frozen material, compact identity, table-oriented processing,
primitive-oriented layouts, and compiler caches remain valid implementation directions, but this ADR does not make any
of them part of Publication meaning.

V1 may realize Publication through ordinary generated JVM boundaries. V2 may use more aggressive specialization,
incremental reuse, or different adapter machinery. The Publication Contract must remain the same across those backend
choices.

---

## 7. Verification, Determinism, and Incremental Extensibility

Publication processing is deterministic.

The compiler must reject Publication meaning that depends on implementation-only coordinates or unresolved authority.
Every Publication source must resolve to exact authoritative machine material, and every outward meaning must resolve to
an exact declared outward meaning accepted by the Publication model.

An implementation field, method, exception class, runtime object, serializer path, external endpoint, database result,
or adapter lookup cannot substitute for an unresolved Publication source.

Where one Publication judgment depends on several established machine meanings, the complete dependency relation must be
explicit. Parallel execution may change when those sources become physically available, but it cannot change which
source meanings participate or which outward meaning is established.

First-arrival and first-Failure behavior is invalid unless the Contract itself explicitly defines an equivalent semantic
law without depending on runtime arrival order.

For the same authoritative source material, Publication declaration, applicable Contract context, and outward boundary,
every valid compiler and backend execution must establish the same Publication result.

```text
clean full compilation
incremental compilation
cache hit
cache miss and recomputation
single-threaded compilation
parallel compilation
valid alternative scheduling
```

must remain semantically equivalent for Publication.

Canonical ordering may be used where several source meanings or outward alternatives require deterministic
representation. That ordering does not create semantic priority unless the Publication Contract explicitly declares a
priority relation as Contract meaning.

Publication cannot consume source meaning before that meaning is established. It also cannot authorize an outward
meaning while unresolved authoritative dependencies required by that exact Publication relation remain capable of
changing the judgment.

An already-established Publication judgment is non-retroactive. Later changes to unrelated State, Version, Policy,
Governance, external implementation, cache state, or adapter state cannot rewrite its earlier meaning.

Publication-related compiler products must expose the dependencies that determine them. Future incremental invalidation
must be able to distinguish changes to source meaning, Publication law, applicable context, and Output Presentation from
unrelated compiler material.

A change to external adapter implementation must not invalidate Publication semantic identity unless the change also
changes an explicit Contract input to Publication. A change to Output Presentation may require re-verification of
Publication-to-presentation closure without redefining the internal source meaning from which Publication begins.

Likewise, a change to Diagnostic wording, retained stack evidence, transport encoding, database driver, Kafka producer,
HTTP library, or another external implementation cannot change Publication meaning by itself.

Any persisted compiler cache, serialized Publication IR, generated adapter artifact, or backend mapping table is
implementation material. Its schema or invalidation version is separate from Contract Version.

Verification must preserve non-public-by-default behavior. If an existing machine gains new Facts, Failure detail,
States, Policy material, Diagnostic Evidence, or other internal meaning, those additions cannot silently become outward
meaning through an unchanged Publication Contract.

Because Publication is explicit user-authored Contract meaning, malformed or incomplete Publication declarations are
compile-time invalidity rather than runtime Publication Failure. Runtime Failure remains determined by the authorities
that actually fail during machine processing or realization.

---

## 8. Deferred Decisions

The following questions remain open for further research and design discussion:

1. What exact canonical Publication model should represent source meaning, source sets, outward meaning, applicability,
   applicable Contract context, and outward boundary without over-generalizing Publication into a universal mapping
   language?
2. What exact user-authored IDL syntax and ownership boundary should declare Publication after the earlier
   operation-local Fact-only model is removed or generalized?
3. Should the formal Publication vocabulary continue to use `outward claim`, or should it use a broader term such as
   `outward meaning` so command-like, event-like, request-like, and other Core outputs are covered without confusing
   their external realization with Contract meaning?
4. What exact completion law connects Publication to active processing boundary completion, especially for streaming,
   long-running, or independently publishable machine work?
5. How should one Publication declaration consume several established source meanings while preserving each source's
   independent identity and avoiding an accidental aggregate-result ontology?
6. What exact semantic material defines the authorized outward meaning, and how much of the earlier ADR-0049
   source-to-target coordinate relation belongs to Publication versus the next Output Presentation Contract?
7. What diagnostic or evidence-derived outward meaning, if any, may be declared once Diagnostic Evidence / Retention is
   fully defined, while keeping Diagnostic Evidence non-public by default?
8. How should Publication declaration identity and applicable context interact with Contract Version and Policy World
   changes across long-running machine activity?
9. Which concrete Publication IR, frozen representation, compact identity, dependency index, cache key, and generated
   realization form best fit the redesigned frontend and IR while preserving the constraints in this ADR?
10. Which Publication-specific parts of ADR-0049, `What Contract Is`, and earlier frontend documents must be revised
    once this model is accepted?

These questions do not reopen the separation between Publication and external realization, the non-public-by-default
rule, the requirement that Publication consume authoritative machine meaning, the separation from Output Presentation,
or the requirement that Publication remain deterministic.

---

## 9. Consequences

### Positive

Publication becomes a general outward Contract authority rather than a special serializer-facing path from one Operation
return Fact.

Internal success, Failure, and other explicitly admissible established machine meanings can share one outward authority
without collapsing their source identities or creating a separate outward error system.

The machine becomes non-public by default. New internal meaning cannot leak outward merely because implementation can
reach, serialize, persist, or transmit it.

Publication can authorize a consumer-facing meaning different from the internal source while preserving the source
unchanged. This allows internal Failure detail and external failure meaning to remain separate without hiding either
inside exception handlers or adapters.

Publication, Output Presentation, and external realization have explicit boundaries. The Contract decides what outward
meaning is allowed, the next Contract decides its closed outward shape, and replaceable adapters realize that shape
outside the core.

Databases, brokers, network stacks, serializers, filesystems, external APIs, actuator systems, and other outside
implementations cannot become Contract Authority merely because they perform the physical effect.

The outbound boundary now mirrors the inbound boundary: external implementation has no Contract authority in either
direction.

Multiple internal Failures can participate in one outward Publication decision without creating aggregate or priority
Failure semantics.

The Publication model remains compatible with deterministic frozen compiler products, explicit dependency tracking,
cache reuse, parallel compilation, and future incremental invalidation without requiring one physical IR layout.

### Negative

The Publication-specific model in ADR-0049 is now too narrow where it requires one established Operation return Fact as
the sole Publication source and treats Publication primarily as exact coordinate exposure into Output Presentation.
Those parts must be revised after ADR-0058 is accepted.

Publication now requires a more general canonical source and outward-meaning model, which must be designed before the
new frontend and IR can be finalized.

The exact boundary between semantic outward meaning in Publication and structural outward material in Output
Presentation still requires the next ADR.

The exact completion rule for Publication in long-running and streaming machines requires further discussion so that
premature publication is forbidden without introducing unnecessary Whole Machine finality.

### Neutral

Publication remains user-authored Contract meaning because outward authority cannot generally be derived from the mere
existence of internal machine meaning.

Publication does not require a runtime `Publication` object, mapper object, serializer callback, database transaction,
message producer, or network emitter.

External realization may use transactions, acknowledgements, atomic file replacement, retries, buffering, durable logs,
or other mechanisms when required by that external system. Those mechanisms remain implementation and do not alter the
Publication Contract.

This ADR establishes the semantic boundary of Publication but deliberately leaves Output Presentation details,
Diagnostic Evidence / Retention, the concrete frontend syntax, and the physical compiler IR for later work.