# ADR-0054: Policy Contract, Explicit Operating Policies, Self-Contained Contract Worlds, and Interface Binding Boundary

## Status

Accepted

## Date

2026-08-11

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0055: Governance Contract, Policy-World Control, Whole-Machine Coordination, and Selection Boundary
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

Engineering systems may face different operating situations, and the same rules are not necessarily appropriate for all of them. A system may therefore need different policies that define how it is expected to operate under different circumstances.

In Kontrakt, those individual rules are already separated into explicit Contract Authorities. Policy does not restate their laws. It makes one operating policy explicit by declaring the exact arrangement of Contract Authorities that together govern one Interface under that policy.

```text
Interface Authority
    -> Policy Normal
        -> Contract World Normal

    -> Policy Emergency
        -> Contract World Emergency

    -> Policy Legacy
        -> Contract World Legacy
```

Resolving one Policy establishes one self-contained Contract World. The Contract World is the complete contractual meaning under which the Interface and its Operations are interpreted and judged while that Policy governs.

A Policy is not a branch inside one pipeline and is not a generic rule that returns `Permit` or `Refuse`. It does not inspect the current situation and decide that its own world should govern. That earlier Policy model mixed Policy with ordinary Contract law and with Governance.

Governance is separate. Policy defines what governs under one operating policy. Governance decides and manages which declared Policy world or coordinated arrangement of Policy worlds governs the machine.

An Interface that does not need explicitly named Policies keeps the static Contract World already established by the earlier Interface frontend work. Policy is therefore optional.

---

## 2. Problem

ADR-0046 gives an Interface one explicit Contract arrangement. That is sufficient when the Interface always runs under the same Contract World.

Some machines need several legitimate operating policies while keeping one Interface and one Operation surface. Duplicating the Interface would incorrectly make an operating difference look like a different Interface authority. Hiding the difference in runtime configuration would move Contract meaning outside the Contract model.

The earlier Governance design solved this by placing named `Manifest` worlds inside Governance and then also making Governance responsible for selecting among them. That combined two different responsibilities.

```text
world definition
+
world control
    -> one Governance Contract
```

The combination looked reasonable while Governance was limited to one Interface. It becomes wrong once Governance is treated as actual machine governance. Whole-machine control, coordinated changes across Interfaces, and concurrent world changes are larger responsibilities than declaring the contents of one Policy world.

Version does not solve world definition either. ADR-0053 identifies a revision of one sovereign Contract Authority. Version does not say which other Contract Authorities belong together as one operating policy.

Ordinary pipeline Contracts do not solve it. Admission owns admission. Budget owns allowance. Capacity owns current occupancy admission. Machine owns State and movement. Invariant owns factual law. Publication owns outward claims. None should become a miscellaneous container merely because one operating policy needs a particular combination of them.

Policy fills this gap.

> Policy is the Contract Authority that declares one named, self-contained arrangement of Contract Authorities for one Interface, thereby establishing one Contract World.

This makes the operating policy explicit without giving Policy the law owned by the Contracts it binds.

---

## 3. Contract Decision

### 3.1. Policy Authority

Policy is an optional, independently versioned user-sovereign Contract Authority belonging to one Interface.

A Policy expresses one operating policy by binding the exact Contract material that forms its Contract World.

```text
Policy Normal
    -> exact bindings
    -> Contract World Normal
```

The individual Contracts remain sovereign. Policy does not own a second judgment over them. It does not rejudge Admission, recalculate Budget, admit Capacity, change State movement law, rejudge an Invariant, or decide Publication.

It owns the arrangement.

```text
Policy
    owns which exact Contract Authorities form this Contract World

Bound Contracts
    retain the meaning and judgment authority of their own obligations
```

Policy also does not determine whether the current situation calls for that Policy. It does not inspect load, time, tenant, State, environment, operator intent, or another situation and decide that its own world should govern.

World control belongs to Governance in ADR-0055.

### 3.2. Policy Identity and Contract World

Each Policy has a user-authored nominal identity local to its Interface authority.

The same Policy name used by another Interface has no shared identity or implied relation.

```text
Payment.Policy.Normal
    !=
Inventory.Policy.Normal
```

A Policy identity carries no built-in order, preference, compatibility, or activation meaning. Kontrakt does not derive behavior from names such as `Normal`, `Emergency`, `Default`, `Stable`, or `Legacy`.

The Policies declared by one Interface form a finite, closed set of named authorities. Every Policy identity must be unique and unambiguous within that Interface. Runtime discovery, registration, configuration, or implementation state cannot add another Policy to that set.

One Policy declares one Contract World for its Interface.

```text
Policy identity
    -> one self-contained Contract World
```

There is no separate semantic `Manifest` entity between Policy and Contract World.

The former Governance-owned Manifest role is replaced by Policy.

### 3.3. Self-Contained Contract World

Each Policy declares a self-contained logical Contract World.

It does not inherit from another Policy, override another Policy, use another Policy as a fallback, or depend on another Policy to fill missing bindings.

Conceptually:

```text
policy Normal {
    operation authorize {
        input PaymentInput
        admission NormalAdmission
        lowering PaymentLowering
        invariant PaymentInvariant
        publication PaymentPublication
        machine PaymentMachine
        budget NormalBudget
        capacity NormalCapacity
    }

    operation capture {
        input CaptureInput
        admission NormalCaptureAdmission
        lowering CaptureLowering
        publication CapturePublication
        budget NormalCaptureBudget
    }
}

policy Emergency {
    operation authorize {
        input PaymentInput
        admission EmergencyAdmission
        lowering PaymentLowering
        invariant PaymentInvariant
        publication PaymentPublication
        machine PaymentMachine
        budget EmergencyBudget
        capacity EmergencyCapacity
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

The repeated bindings are intentional. Source-level completeness is preferable to making one Policy depend on another for meaning.

The backend may physically share identical canonical material after Policy meaning has been established. Physical sharing does not create semantic inheritance.

Optional Contract slots keep the absence rules already defined by their own frontend and lowering laws. Omitting an optional slot never means that the binding should be inherited from another Policy.

### 3.4. Interface without Policy

An Interface without Policy keeps one static Contract World.

```text
Interface
    -> static Contract bindings
    -> one Contract World
```

This is the single-world form already established by the earlier Interface frontend work.

Policy is required only when the contract author needs one or more explicitly named operating policies rather than that single static arrangement.

Whether an Interface uses the static single-world form or declares named Policy worlds is part of its compiled Contract structure. It is not runtime governing state. Governance may control which already-declared Policy world governs, but it cannot add or remove Policy structure or switch an Interface between those two forms at runtime.

Once an Interface uses Policy worlds, there is no hidden base Policy whose bindings are inherited by the named Policies.

```text
Interface without Policy
    -> one static Contract World

Interface with Policy worlds
    -> Policy A -> Contract World A
    -> Policy B -> Contract World B
```

This ADR does not decide how a Policy world becomes the governing world at runtime or whether later frontend work permits a build-time fixed Policy binding without dynamic Governance. Those are activation questions and belong to ADR-0055 and later frontend work.

The important boundary is already fixed: declaring a Policy defines a world; it does not activate that world.

### 3.5. Interface and Operation Boundary

Policy has no authority over the Interface operation surface.

It cannot redefine which Operations the Interface exposes or what those Operations are called. The generated host-language parameter and return surface also remains outside Policy authority.

A Policy covers the whole Interface. It therefore determines Contract bindings for the Operations already declared by that Interface. One Policy may contain bindings for several Operations, but those entries refer to existing Operation handles; they do not create or alter Operations.

```text
Interface
    exposes Operation handles once

Policy Normal
    binds Contract World for those Operations

Policy Emergency
    binds another Contract World for the same Operations
```

When an Operation executes, its Contract pipeline is interpreted under the Contract World that Governance or the applicable static establishment has made governing for that flow.

If the Operation surface itself must change, that is an Interface change rather than a Policy change.

### 3.6. Contract Binding

A Policy binds established Contract material through the existing Interface Contract vocabulary.

Its bindings are organized by the Operations already declared by the Interface, and the Policy must determine its Contract World without depending on another Policy.

Any Contract binding that is valid for the Interface Contract model may differ between Policies. Policy does not impose a cross-Policy equality rule on particular Contract kinds.

```text
policy Normal {
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

policy Offline {
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

The fixed Operation implementation surface is separate from these pipeline presentations. Different Input or Output Contracts do not imply that the host Operation signature changes at runtime.

Resolving a bound Contract also resolves the Version owned by that Contract. Policy does not infer, order, or rewrite those Version identities.

The normal compiler pipeline verifies the resolved bindings according to the laws already owned by each Contract boundary. Policy does not introduce a separate compatibility relation between the Contracts in one world.

### 3.7. Policy Is Not an Operation-Level Judgment Slot

Policy no longer means an Operation-level rule that consumes an established situation and returns a Policy judgment.

The earlier provisional model:

```text
Established Situation
    -> Policy
    -> selected response / Permit / Refuse
```

is rejected by this ADR.

That model overlaps ordinary Contract law. A condition that changes Admission belongs to Admission. A condition that changes State movement belongs to Machine. A condition that changes Budget or Capacity judgment belongs to those Contracts. Which Policy world should govern belongs to Governance. A choice that does not change contract meaning remains implementation freedom.

Policy therefore does not appear as another Contract judgment beside Admission, Budget, Capacity, Invariant, Machine, or Publication.

It is the authority that declares the Contract arrangement under one operating policy.

This requires later reconciliation of earlier documents that still describe Policy as a pipeline `Permit` or `Refuse` authority or as an ordinary operation-level 1D slot.

### 3.8. Policy Version

Policy is independently versioned under ADR-0053.

A Policy Version identifies one exact revision of that Policy authority.

```text
Payment.Policy.Normal / Version N3
    -> exact declared world meaning
```

Changing the Contract arrangement declared by a Policy changes Policy meaning and therefore requires a new Policy Version.

This does not mean that every referenced Contract must receive a new Version merely because the Policy changes. Each bound Contract retains its own sovereign Version history.

Likewise, changing one Policy does not automatically revise another Policy of the same Interface.

```text
Policy Normal / N4
Policy Emergency / E7
```

They are separate authorities even when they bind some identical Contract material.

### 3.9. Contract World

For a Policy, the Contract World is the exact Contract meaning obtained from that Policy's identity, Version, and resolved bindings.

```text
Interface Authority
+ Policy Authority
+ Policy Version
+ resolved Policy bindings
    -> Contract World
```

Two Policies remain distinct authorities even when they resolve to some or all of the same Contract material. Backend sharing does not merge their declared identities.

A Policy definition is not a runtime object and a Contract World is not whatever configuration happens to be loaded in a process. The world must be recoverable from canonical Contract material.

### 3.10. Separation from Governance

Policy and Governance own different questions.

```text
Policy
    which exact Contract Authorities together define this operating policy?

Governance
    which declared Policy world or coordinated arrangement of Policy worlds governs the machine now?
```

Policy does not choose among Policies. It does not decide when another Policy should replace it. It does not coordinate several Interfaces or settle concurrent world changes.

Governance does not rewrite Policy contents. It controls the governing status of already declared Policy worlds.

This distinction replaces the earlier design in which Governance both owned named Manifests and selected among them.

---

## 4. Frontend and Resolution

### 4.1. IDL Placement

Policy extends the IDL-first Interface model rather than replacing it.

Without Policy, the Interface keeps the existing single static Contract arrangement. With Policy worlds, named Policies declare the alternative complete arrangements. There is no base arrangement that a Policy inherits and modifies.

The Interface operation surface remains authored once.

The exact `.kontrakt` grammar is deferred to later IDL work. The conceptual source form may use `policy` directly, but this ADR does not freeze punctuation, nesting, qualification, or file placement.

```text
interface Payment {
    operations { ... }

    policy Normal { ... }
    policy Emergency { ... }
}
```

The old Governance-owned `manifest Normal` semantic form is not retained. Later frontend work must represent the same meaning as Policy rather than introducing a second world-declaration entity under another name.

Contract references remain compile-time source symbols that resolve to exact Contract Authorities. Runtime discovery mechanisms do not become binding authority.

### 4.2. Canonical Policy Material

Lowered Policy material must preserve:

- the owning Interface Authority;
- the Policy Authority identity;
- the Policy Version;
- the existing Operation handles to which bindings apply;
- the exact bound Contract Authorities and their resolved Versions;
- explicit absence where the relevant Contract frontend requires it.

Policy identities must remain distinguishable even when their resolved Contract material is identical.

Canonicalization may share repeated material physically, but it may not erase a user-declared Policy or invent inheritance, fallback, priority, or equivalence between Policies.

After canonical meaning is fixed, the backend may replace source-facing material with a more efficient representation. That representation remains implementation.

### 4.3. Binding Resolution

Policy binding is exact.

A reference that cannot resolve to one exact Contract Authority and Version cannot become part of the Policy world. Kontrakt does not silently choose a nearby name, newest Version, compatible-looking Contract, or runtime implementation.

The compiler must distinguish a Policy's own identity from the identities of the Contracts it binds. Referencing a Contract does not transfer that Contract's authority into Policy.

---

## 5. Verification

Compilation must verify each Policy as one versioned authority belonging to one Interface.

The Policy must determine the Contract bindings for every Operation in the scope required by the Interface model without depending on another Policy. Optional slots keep their existing absence rules, so this does not require every optional Contract to be written in source.

A binding that names unavailable Contract material fails through normal frontend and Contract-resolution rules. Once resolved, the existing compiler pipeline verifies the Contracts at their proper boundaries; Policy does not add a second compatibility system on top of those checks.

The verifier must also preserve the separation between Policy and the Interface operation surface. Policy cannot create, remove, rename, or dynamically reshape Operations.

The same Policy Authority, Policy Version, and resolved Contract material must produce the same canonical Contract World regardless of discovery order, host representation, source file order, allocation history, hash iteration, or backend layout.

Verification must reject hidden inheritance, implicit fallback, runtime discovery, and any source form whose meaning depends on another Policy without an explicit Contract relation already admitted by the model.

Policy does not require a runtime `Policy Judgment`, response selector, situation projection, priority evaluator, or `Permit`/`Refuse` result.

---

## 6. Contract and Implementation Boundary

Policy owns the declared operating Contract World. It does not own the mechanism that stores, activates, switches, distributes, caches, or executes that world.

The backend may compile each Policy into a specialized execution image, intern identical material shared by several Policies, precompute indexes, or replace user-facing names with compact internal identities. These optimizations are valid only if they preserve the declared Policy meaning.

A configuration object, dependency injection container, service registry, environment variable, feature flag, runtime map, generated class, callback, or strategy object may help realize a selected world. None becomes Policy authority merely because an implementation uses it.

The same boundary applies to Operation implementation. A Policy may bind Contracts for several Operations because it declares an Interface-wide world, but it does not control the procedure inside the Operation body.

Replacing the backend realization must not change which Contract World the same canonical Policy declares.

---

## 7. Deferred Decisions

The following remain open:

- exact `.kontrakt` grammar and source placement for named Policies;
- how the frontend represents the existing single static Contract World beside optional Policy worlds;
- whether a build may statically establish one named Policy without dynamic Governance and, if so, how that establishment is declared without an implicit default;
- the exact control API by which Governance refers to Policy identities;
- whole-machine composition and cross-Interface coordination;
- concurrent and distributed Governance realization;
- diagnostics for unavailable or invalid Policy-world establishment;
- and the later cleanup of earlier ADRs and `What Contract Is` passages that still describe Policy as a pipeline judgment or operation-level 1D slot.

ADR-0055 owns Governance meaning. It must not move Contract World definition back into Governance merely because Governance controls which Policy world is in force.

---

## 8. Consequences

### Positive

An Interface can remain one authority while explicitly declaring several operating policies and their Contract Worlds.

Each Policy stands on its own, so its meaning never depends on another Policy or on hidden configuration. Policies may differ wherever the user needs a real Contract difference, while each bound Contract retains its own sovereign law.

The distinction between world definition and world control is now explicit. Policy declares one world. Governance controls which world or coordinated arrangement of worlds governs the machine.

The Operation implementation surface remains stable because Policy changes the surrounding Contract World rather than the Operation itself.

Removing the old situation-to-response Policy model also prevents Policy from becoming a second generic judgment language over Admission, Budget, Capacity, Machine, Invariant, Publication, or other already-defined Contracts.

### Negative

A Policy must be a complete logical world. When several Policies share most of their Contracts, source may repeat bindings that the backend later deduplicates.

Policy can no longer be used as a convenient name for arbitrary conditional logic, routing preferences, strategy objects, or implementation heuristics. If such behavior is contractually meaningful, it must belong to the Contract Authority that owns that obligation or to a later explicitly defined authority.

The frontend and several earlier documents require reconciliation because previous drafts treated Policy and Governance as operation-level judgment slots.

### Neutral

Policy makes operating Contract Worlds explicit but does not decide which one should govern at a particular time.

Governance, Whole Machine control, and deterministic concurrent world changes remain separate work. Existing Contract verification continues to judge each bound Contract at the boundary that already owns its law.