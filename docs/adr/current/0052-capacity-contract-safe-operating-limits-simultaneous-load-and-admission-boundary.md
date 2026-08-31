# ADR-0052: Capacity Contract, Explicit Safe Operating Memory Limits, and Realization Boundary

## Status

Accepted

## Date

2026-08-08

## Related

- `../../the-most-important-thing/what-contract-is.md`
- `../../todo/kontrakt-verifier-implementation-plan.md`
- ADR-0054: Policy Contract, Explicit Operating Policies, Self-Contained Contract Worlds, and Interface Binding Boundary
- ADR-0056: Governance Contract, Policy-World Control, Whole-Machine Coordination, and Selection Boundary
- ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics

---

## 1. Context

A real machine has an operating range. Software is no exception: memory is finite, and a machine that keeps accepting
more resident load eventually loses the ability to operate correctly.

Capacity makes that safety boundary explicit. It does not describe how much work an application may cumulatively
consume. That is Budget. Capacity states how much governed load may be borne at the same time.

In V1, the contract-visible Capacity quantity is Memory Occupancy. Kontrakt already realizes much of its core through
bounded, pre-resolved memory structures, so that memory can be planned and controlled without making physical layout
part of the contract.

---

## 2. Problem

Software engineering has to acknowledge limits before failure exposes them. Memory pressure may come from normal load,
pathological input, an internal expansion, or deliberate exhaustion. The cause does not change the engineering problem:
additional resident load can push the machine beyond a safe operating line.

That line must not be hidden in heap settings or implementation defaults. At the same time, Kontrakt must not promise
control over memory it does not own. V1 leaves the user's realization body opaque and does not rewrite or optimize it,
so V1 Capacity cannot claim to govern arbitrary memory retained by that implementation.

Capacity therefore needs an explicit contract limit and an equally explicit enforcement boundary.

---

## 3. Decision

### 3.1. Foundation

A Capacity Contract establishes the maximum Memory Occupancy that a governed contract boundary may bear at one time.

```text
Explicit Capacity Declaration
    -> Resolution and Validation
    -> Canonical Capacity Contract
    -> Backend Capacity Realization
```

Capacity is optional. No limit is inferred from available heap, deployment size, profiling data, or backend defaults.
Absence means that no Capacity law governs that exact subject.

The limit is inclusive. A governed boundary may remain at its declared Capacity but must not establish additional
Kontrakt-controlled memory that would take it beyond that wall.

### 3.2. V1 Enforcement Boundary

V1 Capacity governs only memory whose lifetime and growth Kontrakt directly owns or can control while realizing the
selected contract boundary.

The user's realization body is outside that authority in V1. Kontrakt does not embed Capacity machinery in user code,
and an Operation Capacity does not imply that arbitrary implementation memory is governed.

A later realization profile may extend coverage only when Kontrakt actually takes responsibility for transforming or
controlling that realization. That extension belongs to V2 realization work and does not alter the V1 guarantee.

### 3.3. No Implementation-Stage Allocation

Capacity is attached to logical contract subjects, not to the physical pipeline that happens to realize them.

A backend may change its execution decomposition without receiving separate contractual memory quotas. Memory used by
Kontrakt-owned processing is attributed to the logical subject whose contract work caused that occupancy. Internal stage
boundaries remain replaceable implementation structure.

---

## 4. Capacity Contract

### 4.1. Declaration Scope and Subject

A V1 Capacity declaration belongs to exactly one enclosing Interface. ADR-0047 already establishes that Capacity is
selected once by that Interface rather than through an operation-local pipeline slot.

Each Capacity entry names its governed subject explicitly. The subject may be that Interface, an exact Interaction in
it, or an exact one-dimensional Contract established inside the same Interface. The enclosing Capacity selection does
not supply an omitted subject and is not used to infer one.

Cross-Interface Capacity authority is not permitted in V1.

### 4.2. Memory Occupancy

V1 defines Capacity over Memory Occupancy.

Memory Occupancy is the amount of memory currently borne by a governed subject under Kontrakt's V1 enforcement coverage.
It is not cumulative allocation. Memory that was allocated and later ceased to belong to that governed occupancy no
longer contributes to Capacity, even though the same allocation may already have contributed to a Memory Use Budget.

This distinction is fundamental:

```text
Memory Use Budget
    cumulative allocation charge across one application boundary

Memory Capacity
    current governed memory occupancy at one time
```

Capacity does not derive memory meaning from JVM object or garbage-collector representation. V1 instead relies on
regions whose extent and lifetime the selected backend can govern strongly enough to preserve the declared wall.

### 4.3. Growth Judgment

Capacity is judged when Kontrakt-controlled memory attributed to a governed subject would increase.

```text
current governed occupancy
    + proposed governed growth
    <= declared capacity
```

Where the backend knows the physical growth before establishment, the judgment occurs before that growth is committed.
Where a bounded Kontrakt-owned structure grows according to a resolved capacity schedule, its required high-water memory
must remain within the applicable Capacity before the new region becomes usable.

V1 does not define a user-visible shared-pool protocol. A backend may use one internally if it preserves the same
Capacity judgment without becoming contract authority.

### 4.4. Attribution

Memory is charged to the logical contract subject that currently owns responsibility for the Kontrakt-controlled region.
Attribution follows contract responsibility rather than the implementation stage that allocated the bytes.

A temporary region used only while realizing one exact Operation may therefore be attributed to that Operation. Memory
owned by the enclosing Interface across Operations is attributed to the Interface. A backend-shared structure that is
not owned by one contract subject remains backend realization memory and must not be duplicated across several user
Capacity accounts merely to make accounting convenient.

Attribution must be fixed by the selected realization before runtime enforcement begins. Runtime scheduler choice,
allocation order, cache placement, or garbage collection may not redefine which contract subject owns a region.

### 4.5. Interface and Subject-Specific Walls

An Interface Capacity and a Capacity for a more specific subject inside that Interface may coexist. Each applicable wall
is judged on its own governed occupancy.

A subject-specific declaration is not a slice reserved from the Interface wall. V1 does not create a parent pool or
redistribute unused Capacity between subjects. If both laws apply, both must remain satisfied.

This preserves replaceability: improving an implementation may reduce actual occupancy without rewriting the contract,
and changing an internal stage layout does not require reallocating contractual memory between stages.

### 4.6. Capacity Limit

A declared Capacity is a maximum permitted by the contract, not a promise that every environment can provide that much
usable memory. A backend or deployment may reject a realization that cannot safely provide the requested wall.

Zero is valid and prevents any governed Memory Occupancy from becoming established for that subject. Negative limits are
invalid. Absence means that Capacity does not govern the subject.

### 4.7. Capacity and Budget

Budget and Capacity may refer to memory while preserving different laws.

Memory Use Budget counts the allocation charge attributable to one contract application across its boundary. Releasing
or collecting previously allocated memory does not erase that charge. Capacity instead observes the memory currently
borne by its governed subject.

The two laws are therefore independent. A run may remain under its Memory Use Budget while current occupancy reaches a
Capacity wall, and it may allocate substantial memory over time while keeping simultaneous occupancy low enough to
remain inside Capacity.

### 4.8. Capacity and Invariant

Capacity does not define whether established Facts or relations are true. A cardinality rule belongs to Invariant when
crossing the number makes the factual world invalid.

V1 does not introduce an `Established Contract Material Population` Capacity quantity. If a large amount of contract
material threatens the machine because of the memory it occupies, Memory Occupancy is the Capacity concern. This avoids
turning domain cardinality or backend representation counts into a second resource vocabulary.

### 4.9. External Load and Infrastructure

Capacity does not claim authority over traffic that has not entered Kontrakt's controllable machine boundary. Traffic
pressure rejected before contract processing remains a responsibility of the surrounding infrastructure.

Likewise, V1 does not define an `In-Flight Contract Application Population` Capacity quantity. Execution counts are not
used as proxies for memory pressure. If a later execution model establishes an independent contract-native population,
its Capacity meaning must be decided separately rather than inferred from the backend.

### 4.10. Canonical Material

The canonical V1 Capacity material records the identity of the Capacity declaration, its exact governed subject, Memory
Occupancy as the quantity, the capacity magnitude and unit, and the applicable contract world.

Within one active contract world, the same exact Capacity subject may have only one Memory Occupancy authority.
Contradictory duplicate declarations fail compilation.

The canonical contract contains no physical memory-layout or capacity-scheduling detail from the backend.

---

## 5. V1 User Authoring and Processing Boundary

The V1 frontend lowers the selected Capacity declaration into canonical material before backend realization.

```text
interface Capacity binding
    -> restricted source evidence
    -> resolution and validation
    -> canonical Capacity material
    -> backend realization check
    -> enforcement over Kontrakt-owned memory
```

The Interface-level selection names the Capacity declaration set. It does not provide the subject of an individual
Capacity entry.

```text
interface CalculateContract {
    budget CalculateBudget
    capacity CalculateCapacity

    operation calculate(input: CalculateInput): CalculateOutput
}
```

Budget and Capacity are selected independently. The Budget line is shown only to make the enclosing Interface surface
complete; its contract law remains in ADR-0051.

The V1 Kotlin presentation is a named, non-instantiable declaration class. Each permitted method states one exact
subject type and `MemoryOccupancy`, returns one approved memory unit, and constructs that unit from one exact literal
magnitude.

```kotlin
class CalculateCapacity private constructor() {

    fun interfaceMemory(
        subject: CalculateContract,
        quantity: MemoryOccupancy,
    ): Mebibytes =
        Mebibytes(2_048)

    fun calculateMemory(
        subject: CalculateInteraction,
        quantity: MemoryOccupancy,
    ): Mebibytes =
        Mebibytes(512)

    fun canonicalizationMemory(
        subject: CalculateCanonicalization,
        quantity: MemoryOccupancy,
    ): Mebibytes =
        Mebibytes(128)
}
```

The method name does not own Capacity meaning. Renaming `interfaceMemory` or `calculateMemory` without changing the
resolved subject, quantity, unit, or magnitude leaves the canonical Capacity unchanged. Parameter names likewise carry
no authority. The frontend reads the exact parameter and return types and the literal unit construction, then lowers
that source evidence into canonical material. These methods are not runtime callbacks and are not invoked to determine a
limit.

The generated `CalculateContract` host interface may be used as the explicit Interface subject because its name is
frontend reference material for the authoritative IDL Interface from which it was generated. This does not make the
generated interface a second source of contract meaning. Interaction and one-dimensional subject types are interpreted
through the same frontend resolution rule.

Capacity authoring does not admit alternate forms whose meaning comes from host execution or structure. An annotation,
inherited role, runtime object, constructor state, property, callback, control-flow branch, runtime lookup, or hidden
default cannot establish a wall, and the magnitude cannot be computed from environment state or user code. These forms
are rejected for the same reason they are rejected from other one-dimensional Contract presentations: contract authority
must remain in explicit source material that the frontend can resolve and lower without executing it.

This authoring surface does not enlarge the V1 enforcement boundary established in Section 3.2.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Authority

The contract owns the governed subject and Memory Occupancy limit. The backend owns measurement, memory-region design,
preallocation, growth control, and physical enforcement.

Changing an arena into primitive arrays, changing a table capacity schedule, or fusing several implementation stages
must not change the canonical Capacity law.

### 6.2. Kontrakt-Owned Memory

Kontrakt may use bounded memory structures whose maximum physical extent is resolved before they become part of a
realization. V1 may use those physical facts to prove that a Capacity can be preserved, but they remain implementation
facts rather than user contract material.

When several such regions are attributable to one governed subject, their applicable high-water occupancy is combined
for Capacity enforcement. The calculation must avoid double-counting shared storage and must include transient overlap
when a growth or replacement operation temporarily requires both old and new regions to coexist.

### 6.3. Realization Feasibility

The backend must reject a realization when its Kontrakt-owned memory plan cannot preserve a declared Capacity. This can
be decided before runtime when resolved table sizes, slabs, arenas, or other bounded regions already prove that the wall
would be crossed.

The same check must not be extended to arbitrary user implementation memory in V1. Lack of authority over that memory is
a declared product boundary, not a reason to approximate it or silently weaken the Capacity contract.

Across several Interfaces, a backend may sum compatible declared maxima and its own bounded shared regions when doing so
provides a valid physical feasibility bound. That derived total is a realization calculation, not a new global Capacity
Contract. Shared memory must be accounted for once, and JVM or environment headroom that Kontrakt does not own remains
an environment requirement rather than user Capacity material.

### 6.4. Runtime Enforcement

Runtime enforcement applies only where the selected backend can observe and control Kontrakt-owned growth strongly
enough to keep the wall intact. A Capacity violation prevents the new governed memory from becoming successful
contract-machine progress.

The exact failure result and Diagnostic mapping remain deferred. Earlier established results are not retroactively
invalidated merely because later growth is refused.

### 6.5. Deterministic Realization

Determinism remains a Kontrakt implementation law rather than a Capacity option. The same canonical Capacity, governed
occupancy, and ordered contract input must produce the same contract-visible judgment.

Backend race order may not become Capacity authority. The general multi-threaded execution and ordering law will be
specified separately after the one-dimensional contract set is complete.

---

## 7. Verification

Verification must prove that each Capacity declaration resolves its explicitly named subject to one exact permitted
Interface, Interaction, or one-dimensional Contract in the enclosing Interface, together with a finite non-negative
Memory Occupancy limit, a valid memory unit, and one applicable contract world. Omitted subjects, duplicate authority,
and forbidden cross-Interface references fail compilation.

Boundary tests must cover zero Capacity, exact-limit occupancy, attempted growth past the wall, coexistence of Interface
and subject-specific walls, and removal of occupancy when contract responsibility for a governed Kontrakt-owned region
ends.

Memory tests must keep cumulative Memory Use Budget separate from simultaneous Capacity. Backend tests must prove that
resolved bounded regions include transient high-water requirements and shared memory is not charged twice.

V1 conformance must also prove the negative boundary: arbitrary user realization memory is not reported as governed
Capacity, and user code requires no embedded Capacity machinery.

---

## 8. Deferred Decisions

The exact over-capacity result, Diagnostic mapping, canonical identity bytes, and explicit absence syntax remain open.

V2 will revisit the enforcement boundary when Kontrakt begins to optimize or otherwise govern user realization. Only at
that point may a stronger profile include realization memory in Operation-level Capacity accounting.

Storage Occupancy remains a possible future Capacity quantity if Kontrakt later defines contract-owned retained storage
independently of database rows, files, or other backend representations. Multi-threaded execution and deterministic
admission ordering remain subjects of a separate ADR.

---

## 9. Consequences

### Positive

Capacity becomes a small and enforceable V1 contract instead of a collection of implementation limits. Memory pressure
from Kontrakt-owned processing can be bounded without promoting execution structure into contract vocabulary.

The design matches Kontrakt's current implementation direction. Bounded tables and storage regions may be used to prove
physical feasibility, while their binary layouts and capacity schedules remain replaceable backend choices.

The V1 guarantee also stays honest. Kontrakt governs the memory it controls and does not claim that an opaque user
realization is covered before the compiler owns that realization.

### Negative

Operation-level Capacity in V1 does not bound arbitrary memory used inside the user's implementation body. A user who
needs an end-to-end realization memory guarantee must wait for a backend profile that actually controls that code.

Strong Capacity enforcement also requires explicit attribution and bounded memory planning inside Kontrakt. Shared and
transient regions must be modeled carefully enough that the physical high-water calculation remains safe.

### Neutral

Capacity does not absorb traffic control, domain truth, scheduling, diagnostics, or contract-world selection. Those
responsibilities remain outside this contract.