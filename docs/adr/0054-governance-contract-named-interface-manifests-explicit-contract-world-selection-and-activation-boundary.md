# ADR-0054: Governance Contract, Explicit Operating Modes, Named Interface Manifests, and Selection Boundary

## Status

Accepted

## Date

2026-08-10

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0055: Policy Contract, Established Situation, Response-Contract Selection, and Judgment Boundary
- ADR-0053: Version Contract, Sovereign Contract Revision History, and Realization Boundary
- ADR-0052: Capacity Contract, Explicit Safe Operating Memory Limits, and Realization Boundary
- ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary

---

## 1. Context

A machine may need more than one operating mode while remaining the same machine.

The same Interface may run under a normal operating arrangement, an emergency arrangement, or another explicitly named
mode. The Interface and its Operations remain the same. What changes is the Contract World applied when an Operation
execution enters the Contract pipeline.

Governance makes this variation explicit. A Governance Contract owns the operating modes of one Interface, and each
Manifest describes the Contract World for one of those modes.

```text
Interface Authority
    -> Governance Contract
        -> Manifest Normal
        -> Manifest Emergency
        -> Manifest Legacy
```

Governance is optional. An Interface that does not need operating modes keeps one static Contract World and has no
Manifest-selection boundary.

When Governance exists, the active Contract World is not inferred. It is established by an explicit Manifest selection.

---

## 2. Problem

ADR-0046 gives an Interface one explicit Contract arrangement. That is sufficient when the Interface always runs under
the same Contract World.

Some machines need to change that arrangement while keeping the same Interface and Operation implementation surface.
Duplicating the Interface would incorrectly make an operating difference look like a different Interface authority.
Hiding the difference in runtime configuration would move Contract meaning outside the Contract model.

Version does not solve this problem. ADR-0053 identifies the established meaning of a sovereign Contract Authority, but
a Version does not decide which Contracts should govern an Interface at a particular operating mode.

Policy also has a different responsibility. It judges an established situation inside an already established Contract
World. It does not define the operating mode that established that world.

Governance fills only this gap. It lets one Interface declare explicit operating modes and lets an external selection
choose which declared Contract World applies to new pipeline flows.

---

## 3. Contract Decision

### 3.1. Governance Authority

Governance is an optional, independently versioned user-sovereign Contract Authority owned by one Interface.

When present, it declares the operating modes available to that Interface as Manifests. Governance does not inspect the
environment or decide which mode is appropriate. It receives an explicit Manifest selection and resolves that selection
to the Contract World already declared by the selected Manifest.

Its Version follows ADR-0053. Governance is versioned as one whole Contract. A change to its Manifest set or to the
meaning declared by any Manifest changes Governance meaning and therefore requires a new Governance Version.

### 3.2. Manifest

A Manifest is Governance-owned canonical material representing one operating mode. It is not an independent Contract
Authority and has no independent Version.

Each Manifest has a user-authored nominal identity local to its owning Governance. The same Manifest name used by
another Interface has no shared identity or implied relation. A Manifest identity carries no built-in order or
preference, and Kontrakt does not derive behavior from its spelling.

Each Manifest is a self-contained logical Contract World. It does not inherit from another Manifest, override another
Manifest, or use another Manifest as a fallback.

```text
manifest Normal {
    operation authorize {
        input PaymentInput
        admission NormalAdmission
        lowering PaymentLowering
        invariant PaymentInvariant
        publication PaymentPublication
        machine PaymentMachine
        budget NormalBudget
        capacity NormalCapacity
        policy NormalPolicy
    }

    operation capture {
        input CaptureInput
        admission NormalCaptureAdmission
        lowering CaptureLowering
        publication CapturePublication
        budget NormalCaptureBudget
    }
}

manifest Emergency {
    operation authorize {
        input PaymentInput
        admission EmergencyAdmission
        lowering PaymentLowering
        invariant PaymentInvariant
        publication PaymentPublication
        machine PaymentMachine
        budget EmergencyBudget
        capacity EmergencyCapacity
        policy NormalPolicy
    }

    operation capture {
        input CaptureInput
        admission EmergencyCaptureAdmission
        lowering CaptureLowering
        publication CapturePublication
        budget EmergencyCaptureBudget
    }
}
```

The repeated bindings above are intentional. Source-level completeness is preferable to making one Manifest depend on
another for meaning. The backend may physically share identical canonical material after that meaning has been
established.

Optional Contract slots keep the absence rules already defined by their own frontend and lowering laws. Omitting an
optional slot never means that the value should be inherited from another Manifest.

### 3.3. Interface without Governance

An Interface without Governance has one static Contract World. There is no Manifest and no runtime selection to
establish before its Contract pipeline begins.

```text
Interface
    -> static Contract bindings
    -> one Contract World
```

This is the single-world form already established by the earlier Interface frontend work.

Declaring Governance changes the contract structure. The Interface no longer has a separate base Contract World that its
Manifests modify. Each Manifest declares its own self-contained world.

```text
Interface without Governance
    -> one static Contract World

Interface with Governance
    -> Governance
        -> Manifest A -> Contract World A
        -> Manifest B -> Contract World B
```

Whether an Interface has Governance is part of its compiled Contract definition. It is not a runtime operating mode and
cannot be switched on or off by Manifest selection.

### 3.4. Interface and Operation Boundary

Governance has no authority over the Interface operation surface.

It cannot redefine which Operations the Interface exposes or what those Operations are called. The generated
host-language parameter and return surface also remains outside Governance authority.

A Manifest governs the whole Interface. It therefore determines the Contract bindings for each Operation already
declared by that Interface. One Manifest may contain bindings for several Operations, but those entries refer to the
existing Operation handles; they do not create or alter Operations.

When an Operation implementation executes, the corresponding Contract pipeline runs under the Contract World selected
for that Interface. Governance changes the Contract World applied to that flow, not the existence or identity of the
Operation itself.

```text
Operation execution
    -> Contract pipeline begins
    -> selected Contract World applies
    -> Input ... Output
```

If the operation surface itself must change, that is an Interface change rather than a Governance mode change.

### 3.5. Contract Binding

A Manifest binds established Contract material through the existing Interface Contract vocabulary. Its bindings are
organized by the Operations already declared by the Interface, and the Manifest must determine the Contract World for
each of those Operations without depending on another Manifest. Governance does not redefine the meaning owned by any
bound Contract.

Any Contract binding that is valid for the Interface Contract model may differ between Manifests. Governance does not
impose a cross-Manifest equality rule on particular Contract kinds.

If two modes use the same Contract, the user may bind that Contract explicitly in both Manifests. If the modes differ,
the user expresses that difference by binding different Contract material.

```text
manifest Normal {
    operation authorize {
        input NormalAuthorizeInput
        admission NormalAuthorizeAdmission
        output NormalAuthorizeOutput
        budget NormalAuthorizeBudget
    }

    operation capture {
        input NormalCaptureInput
        admission NormalCaptureAdmission
        output NormalCaptureOutput
        budget NormalCaptureBudget
    }
}

manifest Offline {
    operation authorize {
        input OfflineAuthorizeInput
        admission OfflineAuthorizeAdmission
        output OfflineAuthorizeOutput
        budget OfflineAuthorizeBudget
    }

    operation capture {
        input OfflineCaptureInput
        admission OfflineCaptureAdmission
        output OfflineCaptureOutput
        budget OfflineCaptureBudget
    }
}
```

The fixed Operation implementation surface is separate from these pipeline presentations. Different Input or Output
Contracts do not imply that the host Operation signature changes at runtime.

Resolving a bound Contract also resolves the Version owned by that Contract. A Manifest does not repeat, infer, or order
those Version identities.

The normal compiler pipeline verifies the resolved bindings according to the laws already owned by each Contract
boundary. Governance does not introduce a separate compatibility relation between the Contracts in a Manifest.

### 3.6. Explicit Selection

A governed Interface needs an established Manifest selection before a new Contract pipeline can begin. Manifest
selection is scoped to the Interface, not to an individual Operation. All new pipeline flows of that Interface resolve
through the Interface's established Manifest selection, while another Interface may establish a different Manifest
independently.

```text
selected Manifest Identity
    -> Governance
    -> exact declared Manifest
    -> Contract World
```

A Manifest does not become active because of its name or declaration position. Even a Governance with only one Manifest
still requires an explicit selection.

If no Manifest has been selected, Governance does not guess. A new pipeline flow for that Interface cannot begin until
an explicit selection is established.

The mechanism that supplies the selection is not Contract authority. Governance consumes the explicit Manifest identity
carried by whatever integration provides the selection.

The exact selection syntax, control API, and transport mechanism are deferred to later user API work.

### 3.7. Pipeline Application Boundary

The application unit of a selected Manifest is one complete Contract pipeline flow from Input through Output.

A flow resolves the Manifest selected for its Interface when that flow begins. The resulting Contract World remains the
world of that flow until it reaches Output.

```text
Pipeline A begins under Normal
    Input
      -> ...
      -> Output

Manifest selection changes to Emergency

Pipeline B begins under Emergency
    Input
      -> ...
      -> Output
```

A later selection does not rewrite the Contract World of a flow already in progress. It applies to later flows after the
new selection has been established.

How Kontrakt coordinates a selection change with pipelines that begin concurrently is not Governance meaning. The later
concurrency ADR handles that deterministic realization together with the existing backend rules.

### 3.8. Runtime Reselection

A governed Interface may receive another explicit Manifest selection while the system is running. Governance treats that
as a new selection of an already declared operating mode.

The reason for requesting another mode stays outside Governance. Another system may produce the request, but its trigger
condition is not part of the Governance Contract.

Governance therefore contains no rule such as `high load -> Emergency`. Such a condition must be evaluated elsewhere and
reduced to an explicit Manifest selection before Governance receives it.

### 3.9. Contract World

Operating mode is the user-facing idea. Contract World is the exact Contract meaning established for one pipeline flow.

For a governed Interface, that meaning is identified by the selected Manifest within one Governance Version and by the
exact Contract material that Manifest resolves.

```text
Interface Authority
+ Governance Version
+ Manifest Identity
+ resolved Manifest bindings
    -> Contract World
```

Two Manifests remain distinct operating modes even if they bind some or all of the same Contract material. Backend
sharing does not merge their declared identities.

A runtime mechanism may carry the selection, but that mechanism does not own the Contract World.

### 3.10. Separation from Policy

Governance establishes the operating Contract World. Policy acts inside that world after the relevant situation has been
established.

```text
Governance
    explicit Manifest selection
    -> operating Contract World

Policy
    established situation inside that world
    -> prepared response Contract
```

Policy may contribute to an external decision that later requests another Manifest, but that does not move
mode-selection authority into Policy or Governance. The exact Policy model remains ADR-0055.

---

## 4. Frontend and Resolution

### 4.1. IDL Placement

Governance extends the IDL-first Interface model rather than replacing it.

Without Governance, the Interface keeps the existing single static Contract arrangement. With Governance, named
Manifests take the place of that single arrangement. There is no base arrangement that Manifests inherit and modify.

The Interface operation surface remains outside Governance and is authored once.

The exact `.kontrakt` grammar is deferred to later IDL work. That work will decide the source spelling and qualification
of Interface-local Manifest identities and how their bindings are written. Selection and control syntax remain separate
deferred work. The resulting syntax must preserve the semantic boundary between the Interface and its Governance, and
between a Manifest and the Contracts it binds.

Contract references remain compile-time source symbols that resolve to exact Contract Authorities. Runtime discovery
mechanisms do not become binding authority.

### 4.2. Canonical Governance Material

Lowered Governance material must preserve the Governance Authority within its Version and owning Interface. It must also
preserve every Manifest's bindings for the Operations declared by that Interface.

Manifest identities must remain distinguishable even when their resolved Contract material is identical.
Canonicalization may share the repeated material physically, but it may not erase a user-declared mode or invent a
relation between modes.

After canonical meaning is fixed, the backend may replace source-facing material with a more efficient representation.
That representation remains an implementation detail.

### 4.3. Selection Resolution

Manifest selection is exact.

An input that does not resolve to a Manifest declared by the target Governance cannot establish a Contract World.
Kontrakt does not silently choose a nearby name or substitute another Manifest.

The Manifest set declared by one Governance is the closed set of valid Manifest identities for that Interface. Reusing
the same spelling in another Interface does not create a shared mode identity. The exact source or runtime
representation of those identities is deferred; a successful selection must resolve to one exact Manifest of the
intended Interface Governance.

---

## 5. Verification

Compilation must verify the Governance Contract as one versioned authority owned by one Interface. Its Manifest set must
be finite, and every Manifest identity must be unambiguous within that Governance.

Each Manifest must determine the Contract bindings for every Operation declared by its Interface without depending on
another Manifest. Optional slots keep their existing absence rules, so this does not require every optional Contract to
be written in source. A binding that names unavailable Contract material fails through the normal frontend and
Contract-resolution rules. Once resolved, the existing compiler pipeline verifies the Contracts at their proper
boundaries; Governance does not add a second compatibility system on top of those checks.

The verifier must also preserve the separation between Governance and the Interface operation surface. Backend-specific
handles or runtime configuration cannot be accepted as Governance authority merely because they are convenient for
implementation.

When Governance is present, a pipeline cannot begin without an established selection. Selection itself must resolve
exactly to a declared Manifest.

The same Governance Version, Manifest identity, and resolved Contract material must establish the same Contract World
regardless of discovery order or backend representation.

Deterministic behavior under concurrent selection and pipeline execution is verified under the later concurrency work
rather than by adding scheduling or synchronization semantics to this Contract.

---

## 6. Contract and Implementation Boundary

Governance owns the explicit operating modes of one Interface and the Contract World declared by each Manifest across
that Interface's Operations. It does not own the mechanism that transports a Manifest selection or decides that a
different mode should be requested.

The backend may compile each Manifest into a specialized execution image, intern identical material shared by several
Manifests, or replace user-facing names with compact internal identities. These optimizations are valid only if they
preserve the declared Governance meaning.

Concurrency machinery remains a backend concern. It must realize the pipeline and selection laws without becoming
Contract Authority.

The same boundary applies to the Operation implementation. A Manifest may bind Contracts for several Operations because
Governance covers the whole Interface, but Governance does not control whether those Operations exist or execute. It
only supplies the Contract World under which each corresponding Contract pipeline is evaluated.

---

## 7. Deferred Decisions

The following remain open:

- exact `.kontrakt` grammar for optional Governance and named Manifests,
- CLI and runtime APIs for selecting a Manifest for a particular Interface,
- diagnostics for invalid or unavailable selections,
- whole-machine and cross-pipeline collaboration, including concurrent or distributed realization,
- and the later concurrency rules for deterministic selection and pipeline contention.

ADR-0055 decides Policy meaning and response selection. Governance must not absorb situation judgment merely because a
Policy result or another external system may request a different Manifest.

---

## 8. Consequences

### Positive

An Interface can remain a single authority while explicitly describing several operating Contract Worlds. Interfaces
that do not need this capability remain simple and keep one static world.

Each Manifest stands on its own, so its meaning never depends on another Manifest or on hidden configuration. Modes may
differ wherever the user needs to express a real Contract difference. The backend may still share Contract material that
is identical.

The Operation implementation surface remains stable because Governance changes the surrounding Contract World rather
than the Operation itself. One Manifest can govern several Operation pipelines while preserving the Interface-defined
Operation surface.

### Negative

A governed Interface must maintain each Manifest as a complete logical world. When several modes share most of their
Contracts, the source may repeat bindings that the backend later deduplicates.

A governed Interface also requires an explicit Manifest selection before a new pipeline can begin, even when only one
Manifest is declared.

### Neutral

Governance makes operating modes explicit but does not decide when one mode is preferable to another.

The exact user API and deterministic multithreaded realization remain separate work. Existing Contract verification
continues to judge the bindings selected by each Manifest at the boundaries that already own those laws.