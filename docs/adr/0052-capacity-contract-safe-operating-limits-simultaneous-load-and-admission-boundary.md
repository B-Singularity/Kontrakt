# ADR-0052: Capacity Contract, Safe Operating Limits, Simultaneous Load, and Admission Boundary

## Status

Proposed

## Date

2026-08-07

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0054: Policy Contract, Established Situation, Response-Contract Selection, and Judgment Boundary
- ADR-0053: Governance and Contract World Activation
- ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics

---

## 1. Context

A real machine has a safe operating range. Electrical, thermal, mechanical, and storage systems fail when the load they
must bear exceeds what they were built to sustain. Software is not exempt from that engineering fact. Too many admitted
operations, too much resident material, or excessive memory pressure can make an otherwise valid machine stop making
useful progress or fail outright.

Capacity makes that operating limit part of the user's machine contract.

```text
Capacity
    declares how much simultaneous load
    one governed part of the machine may safely bear
```

The limit is not defined by a thread pool, queue size, heap flag, semaphore, allocator, or scheduler setting. Those may
be used to realize the contract, but they do not decide what the user's machine is allowed to carry.

Capacity is not only protection against hostile traffic. A valid request burst, pathological input, internal material
explosion, or implementation defect can create the same unsafe load. The cause does not change the operating wall.

Budget and Capacity may govern the same machine-resource quantity because they ask different questions.

```text
Budget
    how much one contract application may consume
    during that application

Capacity
    how much load the governed machine boundary
    may bear at one time
```

This distinction is especially important for memory. One application may have a memory Budget while the enclosing
Interface has a separate limit on simultaneous memory load. Neither declaration replaces the other.

---

## 2. Problem

A finite machine can fail even when every individual contract application is valid and stays inside its own Budget.
Several applications may overlap, internal contract material may grow unexpectedly, or the combined load of otherwise
acceptable work may exceed what the machine can safely sustain.

Implementation limits do not solve the contract problem. A worker count, executor queue, JVM heap threshold, callback
depth, graph traversal depth, or allocator policy describes one realization. If one of those mechanisms is replaced, the
Capacity meaning of the user's machine must remain unchanged.

Capacity also cannot be reduced to a final invariant such as `current <= limit`. For a practical machine, discovering an
unsafe load only after the machine has accepted or established it may be too late. The contract must govern load growth
at the point where the machine takes responsibility for additional load.

At the same time, Capacity must not absorb limits already owned by another contract. Domain cardinality remains an
Invariant when the cardinality is a law of the factual world. Diagnostic retention remains a Retention concern. Budget
continues to own per-application consumable allowance.

---

## 3. Decision

### 3.1. Foundation

A Capacity Contract establishes a finite safe operating limit before Kontrakt produces an executable realization.

```text
Explicit Capacity Declaration
    -> Resolution and Validation
    -> Canonical Capacity Contract
    -> Lowered Admission and Growth Control
```

No Capacity is inferred from host resources, deployment configuration, runtime telemetry, or backend defaults. Absence
of a Capacity declaration means that Capacity places no limit on that exact subject and load dimension.

Capacity judges simultaneous load. A governed boundary must not accept or establish additional load when doing so would
place the boundary beyond its declared Capacity.

This law applies at external admission and inside the machine. Capacity is therefore not a request-gateway feature. It
continues to apply wherever contract-governed load can grow.

### 3.2. V1 Decisions

V1 recognizes three Capacity concerns:

```text
Memory
In-Flight Contract Applications
Established Contract Material Population
```

Memory uses the same exact machine-resource and unit vocabulary already established for Budget. Capacity changes the
question from one application's resource use to the amount simultaneously borne by the governed boundary.

An in-flight application is an application for which the machine has accepted contract responsibility and which has not
yet reached a terminal contract result. Whether the realization is running, waiting, suspended, multiplexed, or assigned
to a particular thread does not change that status.

Established contract material may also be bounded when its population is itself machine load and no other contract
already owns that boundedness. This permits Capacity to stop internal material explosion without turning implementation
objects into contract subjects.

Interface-wide Capacity and narrower Capacity declarations may coexist. They are independent operating walls; one does
not distribute, lend, or transfer Capacity to another.

The exact runtime result spelling for a refused Capacity growth is not fixed by this draft. The semantic requirement is
that the over-capacity load is not established as successful contract progress.

---

## 4. Capacity Contract

### 4.1. Meaning and Scope

One Capacity declaration belongs to exactly one enclosing Interface. It may establish limits for that Interface and for
exact contract subjects declared within it.

Capacity authority does not cross Interface boundaries in V1.

The enclosing Interface is itself a valid Capacity boundary because it is the explicit machine scope that contains the
closed operation set and its shared contract world. This allows a machine-wide wall to coexist with narrower walls.

Examples include:

```text
Interface
    simultaneous Memory <= machine limit
    In-Flight Interaction population <= machine limit

exact Interaction
    In-Flight application population <= operation limit

exact one-dimensional Contract
    In-Flight application population <= contract-position limit

exact contract material kind
    established population <= material limit
```

A thread, worker, process, executor, callback, call stack, queue node, traversal node, or backend task is not a Capacity
subject merely because the implementation can count it.

### 4.2. Safe Operating Limit

Capacity is inclusive.

```text
simultaneous load <= capacity
    admissible

simultaneous load > capacity
    not admissible
```

Zero Capacity is valid. It means that no load of that declared kind may be established while the Capacity applies.
Absence is different: the subject and load dimension are not Capacity-governed.

Capacity does not promise that the machine will always operate at the declared maximum. A backend or environment may
refuse work earlier for reasons outside the user's Capacity Contract. It may not admit work beyond the declared wall and
still claim to preserve that Capacity.

### 4.3. Load Growth and Admission

Capacity judgment occurs whenever new governed load would become the machine's responsibility.

The general law is:

```text
established simultaneous load
    + proposed additional load
    <= declared capacity
```

When the additional amount is known before establishment, the backend must judge the increase before admitting it. When
exact physical growth cannot be known in advance, the selected backend must preserve the same operating wall using a
supported observation and control boundary. A backend that cannot preserve the declared Capacity must reject the
contract during compilation rather than silently weaken the limit.

Capacity admission is therefore broader than accepting an external request. Establishing another in-flight contract
application or additional governed core material is also Capacity growth.

The Capacity Contract does not require a user-visible reservation, commit, borrow, return, or release protocol. Such
mechanisms may exist in a realization. Contract Capacity becomes available again when the governed simultaneous load no
longer exists under its contract-defined lifetime.

### 4.4. Memory

Memory is a machine-resource quantity shared with Budget. Capacity applies it to simultaneous machine load rather than
to the allowance of one application.

For example:

```text
Interaction Budget
    Memory <= 512 MiB during one application

Interface Capacity
    simultaneous Memory <= 2 GiB
```

A single application may satisfy its Budget while several overlapping applications together approach the Interface
Capacity. Conversely, a machine may have ample aggregate Capacity while one application exceeds its own Budget. The two
judgments are independent.

Capacity does not define memory through JVM heap layout, object size, allocation site, garbage-collector region,
off-heap mechanism, or another backend representation. The canonical contract owns the quantity and limit; the backend
must prove that it can attribute and control the required simultaneous memory load closely enough to preserve the
contract.

### 4.5. In-Flight Contract Applications

An application becomes in flight when Capacity admission succeeds and the machine accepts responsibility for carrying
that contract application. It stops being in flight when the application reaches its terminal contract result.

For an Interaction, this lifetime covers the accepted contract passage rather than a physical thread lifetime. Work may
wait, suspend, resume, move between workers, or use a different execution strategy without leaving the in-flight
population.

An Interface may limit the aggregate in-flight Interaction population of its closed operation set. An exact Interaction
may declare a narrower limit for applications of that Interaction. An exact one-dimensional Contract may also be bounded
when its simultaneous application population is itself a meaningful machine load.

These walls are cumulative constraints, not allocations from a shared pool. If both an Interface limit and an exact
Interaction limit apply, both must be satisfied.

The exact ordering discipline for competing admissions under concurrent execution is deferred to a separate machine
concurrency ADR after the one-dimensional contract set is complete. That later realization must preserve Kontrakt's
determinism; backend scheduling or race outcomes do not become Capacity authority.

### 4.6. Established Contract Material Population

Contract material can overload a machine even when the number of admitted Interactions remains small. One valid
Interaction may cause a large amount of core material to become established. Capacity may therefore bound the
simultaneous population of an exact contract material kind when that population represents machine load.

This is not a domain cardinality rule.

```text
Invariant
    asks whether the factual world itself satisfies a declared relation

Capacity
    asks whether this machine may safely bear more established material now
```

A domain may legitimately contain more Facts than one machine realization can safely hold at once. Capacity limits the
machine's operating envelope without declaring those additional Facts false.

Capacity does not duplicate another contract's boundedness. Diagnostic Evidence retention remains governed by the
Diagnostic Retention Contract. State rules remain with the State-Machine Contract. Material-population Capacity is used
only where the boundedness is a machine-load concern not already owned elsewhere.

### 4.7. Internal Machine Protection

Capacity remains active after external admission. A machine must be able to stop unsafe internal growth before that
growth turns into later successful contract progress.

This applies regardless of whether the pressure comes from hostile input, a valid but extreme case, unexpected fan-out,
or a defect in the realization. Capacity judges the resulting load, not the cause of the load.

Algorithm-specific quantities do not become Capacity merely because they can grow. Recursion depth, graph-search depth,
callback count, queue length, planner node count, and similar implementation structures remain realization concerns
unless a separate contract has independently established them as contract material.

### 4.8. Canonical Material and Uniqueness

The canonical V1 Capacity material contains the information required to identify one operating wall:

```text
Capacity identity
governed subject
load dimension
capacity magnitude
capacity unit or count unit
applicable contract world
```

Where a population limit refers to an exact contract kind, that kind is part of the resolved load dimension rather than
a name recovered from source convention.

Within one active contract world, one exact Capacity wall may have only one authority. A duplicate declaration for the
same governed subject and resolved load dimension is a compilation error.

The exact canonical identity bytes and final public names of the V1 load dimensions remain deferred.

### 4.9. Capacity and Budget

Budget and Capacity judge independent obligations.

The decisive distinction is not the resource kind but the form of the question.

```text
Budget
    one contract application
    over its contract boundary
    against its finite allowance

Capacity
    one governed machine boundary
    at the same time
    against its safe operating limit
```

This allows the same Memory quantity to participate in both contracts without merging them.

The distinction also explains why some failure modes require both. Capacity can limit how many applications remain in
flight, while an elapsed-time Budget can prevent one accepted application from occupying that Capacity indefinitely.
Neither law can replace the other.

### 4.10. Capacity and Invariant

Invariant owns truth about established Facts and their declared relations. Capacity owns the machine's ability to bear
additional simultaneous load.

A cardinality rule is therefore an Invariant when exceeding it makes the factual world invalid. It is Capacity when the
world may remain valid but this machine must refuse further establishment to stay inside its safe operating envelope.

The same numeric bound must not be declared under both contracts merely to obtain duplicate enforcement.

### 4.11. Capacity, Governance, and Policy

Governance determines the applicable contract world in which a Capacity declaration is valid. Capacity does not choose
or activate that world.

Policy may later use an established Capacity situation when the Policy Contract explicitly relates to it. Policy does
not measure load, create Capacity, discover available resources, or change the declared limit.

### 4.12. Compilation Failure and Runtime Boundary

Compilation fails when Capacity material is ambiguous, contradictory, refers outside its permitted Interface scope, or
cannot be preserved by the selected backend.

A runtime attempt to exceed Capacity does not establish the proposed load as successful contract progress. If the
attempt arises inside an already admitted Interaction, completed earlier contract results remain what they already are,
but the over-capacity growth cannot be treated as a successful continuation.

The exact runtime result type, diagnostic linkage, and mapping to the wider Failure model remain deferred.

---

## 5. V1 User Authoring API and Processing Boundary

ADR-0047 already fixes the Capacity selection point: one explicit Capacity source is bound at the enclosing Interface
scope. Capacity is not repeated as an operation-local pipeline slot.

```text
interface manifest
    -> exact Capacity source binding
    -> Capacity source evidence
    -> resolution and validation
    -> canonical Capacity material
    -> backend capability proof
    -> lowered admission and growth control
```

The selected host declaration is source evidence, not a runtime Capacity controller. Contract authority must come from
explicit subject, load dimension, exact limit, unit, and applicable-world material rather than from executable
callbacks, runtime discovery, or backend configuration.

The final V1 Kotlin declaration shape is not fixed by this draft. It will be designed after the subject and load model
is accepted. That API must preserve the same frontend rules already established for other one-dimensional contracts: no
annotation authority, identifier parsing, hidden defaults, inheritance-based discovery, runtime singleton identity, or
arbitrary executable control flow.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Authority

The safe operating wall, governed subject, load dimension, limit, and applicable contract world are Capacity authority.
The mechanism used to observe and preserve them is not.

A backend may use counters, permits, atomic state, schedulers, allocation instrumentation, admission structures, or
other machinery. Replacing that machinery must not change the canonical Capacity or its contract-visible judgment.

### 6.2. Physical Resource Realization

A physical resource does not become contract authority merely because a Capacity uses a physical quantity. Memory is an
explicit machine-resource quantity in the contract; JVM heap layout and allocator behavior are not.

The selected backend must state what guarantee it can preserve. If the requested wall cannot be observed or controlled
with sufficient strength, compilation fails. Capacity does not convert an unsupported strong guarantee into a best-
effort warning.

### 6.3. Admission and Growth Control

The realization must place control where additional governed load can become established. External entry is one such
boundary, but internal contract growth is equally subject to Capacity.

Backend decomposition may fuse or split physical execution stages. Those choices do not move the logical Capacity wall
or turn implementation lifecycle events into contract subjects.

### 6.4. Deterministic Realization

Determinism remains a Kontrakt implementation law rather than a user Capacity option.

For the same canonical Capacity, established machine load, and ordered contract inputs, the Capacity judgment must
produce the same contract-visible result. Thread scheduling, lock acquisition, atomic-operation races, worker
completion, or container iteration order may not become semantic authority.

The general concurrency and ordering model needed to preserve this law across simultaneous admissions is deferred to a
separate ADR after the one-dimensional contract set is complete.

---

## 7. Verification

Contract verification must establish that every Capacity declaration resolves to one exact governed subject, one exact
load dimension, one finite non-negative limit, a compatible exact unit, and one applicable contract world. Duplicate
authority and cross-Interface references must fail compilation.

Verification must distinguish machine-load Capacity from neighboring contracts. Tests should prove that per-application
consumption remains Budget, factual truth remains Invariant, and Diagnostic retention remains Retention.

Memory verification must cover zero Capacity, exact-limit admission, attempted growth beyond the limit, coexistence of
Interface and narrower limits, and backend rejection when the required simultaneous-memory guarantee cannot be
preserved.

In-flight application tests must cover entry into the governed population, waiting or suspension without release,
terminal completion, Interface-wide and exact-subject walls, and refusal of additional load when any applicable wall
would be exceeded.

Established-material tests must cover internal growth from an already admitted Interaction, exact material-kind
attribution, release when the material no longer belongs to the governed simultaneous load, and rejection of accidental
overlap with another contract's boundedness.

Failure tests must prove that over-capacity load is never published as successful progress, earlier completed contract
material is not rewritten, and unsupported backend realization fails closed.

Backend conformance must prove that optimization and execution decomposition do not change the Capacity judgment. Full
multi-threaded contention and deterministic winner-order testing is deferred to the dedicated concurrency ADR.

---

## 8. Deferred Decisions

The following remain open:

- final V1 Kotlin Capacity authoring form,
- exact public names for the V1 load dimensions,
- the final set of contract material kinds that may carry population Capacity,
- exact runtime over-capacity result spelling,
- Diagnostic linkage,
- Failure mapping,
- canonical identity bytes,
- exact JVM realization proof for simultaneous Memory,
- release accounting details for backend-managed physical resources,
- realization across several runtimes,
- explicit Capacity-absence syntax,
- and the deterministic multi-threaded admission and ordering model.

The general multi-threaded execution law will be decided in a separate ADR after the one-dimensional contracts are
complete. Governance and Policy remain in ADR-0053 and ADR-0054.

---

## 9. Consequences

### Positive

Capacity becomes an explicit safe operating envelope for the user's machine rather than a collection of backend tuning
values. The same law protects against hostile load, ordinary overload, pathological inputs, and unsafe internal growth.

Interface-wide walls protect the closed operation set as one machine while exact Interaction, one-dimensional, and
material limits can protect narrower pressure points. Memory and application population can be governed without making
threads, queues, executors, or allocator structures part of the contract.

Budget and Capacity remain orthogonal. One limits what an application may consume; the other limits what the machine may
bear simultaneously. Their combination provides stronger protection than either contract alone.

### Negative

The backend must control enough of external admission and internal growth to preserve the declared operating wall.
Opaque user code, native work, external systems, or weak resource attribution may make some Capacity declarations
unrealizable.

Population Capacity requires precise contract lifetimes. Physical execution may be concurrent even though backend race
order is forbidden from becoming contract authority, so the later concurrency ADR must provide a deterministic
realization model.

### Neutral

Capacity does not decide business truth, scheduling strategy, retry policy, diagnostic retention, or which contract
world is active. Those meanings remain with their existing contracts or backend realization boundaries.