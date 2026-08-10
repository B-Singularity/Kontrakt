# ADR-0054: Governance Contract, Explicit Operating Modes, Named Interface Manifests, and Selection Boundary

## Status

Proposed

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

A machine often needs more than one operating mode while remaining the same machine.

The same Interface may run normally, under emergency restrictions, or in a compatibility mode without changing its
public operation surface. What changes is the Contract arrangement governing those operations.

Governance makes these operating modes explicit. Each named Manifest represents one mode and carries the exact Contract
bindings for that mode.

```text
Interface Authority
    -> Governance Contract
        -> Manifest Normal
        -> Manifest Emergency
        -> Manifest Legacy
```

Selecting a Manifest establishes the Contract World for that operating mode. Governance does not decide which mode is
desirable; it accepts an explicit selection and establishes what that selection names.

---

## 2. Problem

ADR-0046 gave one Interface a single manifest binding, which effectively gives that Interface one operating mode.

A machine with several modes needs several Contract arrangements without duplicating the Interface or moving the choice
into hidden configuration and runtime code. The Interface must remain one authority while the governing arrangement
changes explicitly.

Version cannot own that choice. ADR-0053 identifies an established meaning of each sovereign Contract Authority, but it
does not decide which established Contracts govern together.

Policy also begins after a Contract World is already established. It may choose a prepared response for an established
situation, but it does not choose the machine's operating mode.

Governance fills only this gap: it declares the available modes of one Interface as named Manifests and establishes the
Manifest selected explicitly.

---

## 3. Contract Decision

### 3.1. Governance Authority

Governance is an independently versioned user-sovereign Contract Authority for the operating modes of one Interface.

Its Manifests define the modes that may govern that Interface. An exact Manifest selection establishes the corresponding
Contract World.

```text
Governance
    -> declared Manifests
    -> explicit selection
    -> active Contract World
```

Governance does not interpret operating conditions or decide which mode should be chosen. It also does not execute the
Contracts contained by the selected Manifest.

Its Version follows ADR-0053. Changing Governance meaning requires a new Governance Version.

### 3.2. Manifest

A Manifest is Governance-owned canonical material representing one operating mode. It is not an independent Contract
Authority and has no independent Version.

Each Manifest has one user-authored nominal identity. Names such as `Normal`, `Emergency`, `Legacy`, or `Blue` carry no
built-in priority, ordering, or behavior. Kontrakt does not infer semantics from the spelling.

A Manifest contains the same Contract bindings that the Interface IDL previously carried as one manifest arrangement.
Governance does not introduce a second configuration language around those bindings.

Conceptually:

```text
interface PaymentContract {
    operation authorize(...)
    operation capture(...)
    operation cancel(...)

    governance PaymentGovernance {
        Stable: Version

        manifest Normal {
            // existing Interface Contract bindings
        }

        manifest Emergency {
            // existing Interface Contract bindings
        }

        manifest Legacy {
            // existing Interface Contract bindings
        }
    }
}
```

The exact frontend grammar may evolve, but the semantic shape is fixed: one Interface operation surface and several
named Manifest binding sets.

### 3.3. Interface Surface Is Declared Once

A Manifest does not create another Interface.

The Interface owns its public operation set once. Every Manifest governs that same Interface Authority.

```text
Interface PaymentContract
    operations
        authorize
        capture
        cancel

    Governance
        Manifest Normal
        Manifest Emergency
        Manifest Legacy
```

If an operating mode requires a different public operation surface or a different Interface responsibility, it is not
another Manifest of the same Interface. It requires another Interface Authority.

Changing the selected Manifest changes the operating Contract World, not the identity of the Interface surface.

### 3.4. Contract Binding

A Manifest binds established Contract material through the existing Interface Contract vocabulary.

Different modes may bind different Contracts, but a Manifest does not redefine the meaning owned by those Contracts.

Conceptually:

```text
manifest Normal {
    operation authorize {
        input PaymentInput
        admission NormalAdmission
        lowering PaymentLowering
        invariant PaymentInvariant
        publication NormalPublication
        machine PaymentMachine
        budget NormalBudget
        capacity NormalCapacity
        policy NormalPolicy
    }
}
```

```text
manifest Emergency {
    operation authorize {
        input PaymentInput
        admission EmergencyAdmission
        lowering PaymentLowering
        invariant PaymentInvariant
        publication RestrictedPublication
        machine PaymentMachine
        budget EmergencyBudget
        capacity EmergencyCapacity
        policy EmergencyPolicy
    }
}
```

Resolving a bound Contract also resolves the Version declared by that Contract. The Manifest does not repeat or derive
Version identity.

A Manifest binding does not transfer Contract authority into Governance. Budget still owns allowance, Capacity owns its
admission wall, Machine owns legal State movement, and each other Contract keeps its own obligation.

### 3.5. Explicit Selection

An operating mode becomes active only through an explicit Manifest Identity supplied to Governance.

```text
selected Manifest Identity
    -> Governance
    -> exact declared Manifest
    -> active Contract World
```

The selection may arrive through a CLI, runtime control surface, operator action, control plane, or another realization.
Those mechanisms carry the choice but do not own Governance meaning.

For example, `--manifest Normal` and a runtime request to `select Emergency` both reduce to the same Contract-level
material: an exact Manifest Identity.

Kontrakt does not derive the active mode from naming, declaration order, discovery order, runtime telemetry, environment
inspection, or a hidden default. If a selection is required and none is supplied, Governance does not guess.

### 3.6. Runtime Reselection

A machine may change operating mode at runtime through another explicit Manifest selection.

```text
Normal
    -> explicit selection of Emergency
    -> Emergency
```

The event or system that causes this request stays outside Governance. Monitoring, Policy, an operator, or a control
plane may decide that another mode should be requested, but Governance receives only the resulting Manifest Identity.

Conditions such as `high load -> Emergency` are therefore not Governance declarations. The condition is evaluated
elsewhere and reduced to an explicit selection before Governance consumes it.

This ADR does not yet define restrictions between one active Manifest and another.

### 3.7. Contract World

Operating mode is the user-facing idea. Contract World is its exact Contract meaning.

One selected Manifest of one Governance Version establishes one Contract World from its resolved bindings.

```text
Interface Authority
+ Governance Version
+ Manifest Identity
+ resolved Manifest bindings
    -> one active Contract World
```

A runtime object, registry entry, configuration file, or deployment name may carry or realize that selection, but it
does not own the Contract World.

Two Manifests remain distinct modes even when some bound Contract Versions are shared.

### 3.8. Separation from Policy

Governance establishes the machine's operating mode by establishing the Contract World named by a Manifest.

Policy operates inside that already-established world. Its response selection does not change the active Manifest.

```text
Governance
    explicit Manifest selection
    -> operating Contract World

Policy
    established situation inside that world
    -> prepared response Contract
```

Governance therefore contains no situation-matching algorithm. The exact Policy model remains ADR-0055.

---

## 4. Frontend and Resolution

### 4.1. IDL Placement

Governance extends the IDL-first Interface model rather than replacing it.

ADR-0046's single manifest arrangement becomes a named Manifest inside Governance. The Interface operation surface stays
outside the Manifest and is authored once.

The V1 frontend must preserve a direct relation between:

```text
Interface operation
Manifest
bound Contract source symbol
```

References remain compile-time source symbols and must resolve to exact Contract Authorities. Runtime class handles,
strings, service lookup, and reflection do not become binding authority.

### 4.2. Canonical Governance Material

Lowered Governance material must preserve at least:

```text
Governance Authority
Governance Version Identity
Interface Authority
Manifest Identity set
Contract bindings of each Manifest
```

The physical representation is backend work.

Generated constants, integer IDs, tables, hashes, indexes, selection handles, or specialized dispatch paths may be used
after canonical meaning is fixed. None may invent or rewrite a Manifest binding.

### 4.3. Selection Resolution

Manifest selection is exact.

An unknown Manifest Identity, ambiguous Governance source, or binding that cannot resolve to exact Contract material is
a failure. Kontrakt does not substitute a close name, current Version, preferred Version, or another Manifest.

The selected Manifest establishes one exact Contract World from already-declared material.

---

## 5. Verification

Compilation must establish that the Governance Contract has exactly one Version, belongs to one Interface Authority, and
contains a finite set of uniquely named Manifests.

Every Manifest must bind only Contracts permitted by the Interface Contract model. Every binding must resolve to exact
Contract material, including the Version owned by that Contract.

The verifier must reject duplicate Manifest identities, hidden or runtime-computed Manifest definitions, bindings to a
different Interface surface, unresolved Contract sources, and backend-specific material presented as Governance
authority.

Selection verification must reject unknown or ambiguous Manifest identities. No Manifest may become active through
implicit priority, declaration order, environment inspection, or fallback.

Generated fixtures and property-based tests should prove that the same Governance Version, Manifest Identity, and
resolved Contract material establish the same Contract World regardless of discovery order or realization layout.

---

## 6. Contract and Implementation Boundary

Governance owns the machine's declared operating modes as named Manifests and establishes the selected mode as a
Contract World.

It does not own the mechanism that supplies selection input. CLI parsing, runtime menus, operator consoles, remote
control planes, configuration transport, monitoring, event delivery, and generated selectors are replaceable realization
or integration mechanisms.

Governance also does not own the algorithms that decide when another Manifest should be requested.

The backend may erase Manifest names from hot paths, replace them with canonical IDs, prebuild one execution image per
Manifest, or specialize selection to constant dispatch. These changes are valid only when the selected Manifest
establishes the same Contract World.

---

## 7. Deferred Decisions

The following remain open:

- compatibility rules among Versioned Contracts bound by a Manifest,
- whether compatibility is declared directly by Governance or by separate Contract material,
- restrictions on runtime movement from one active Manifest to another,
- the exact boundary at which a runtime Manifest reselection becomes effective,
- explicit Governance-absence syntax when only one Contract arrangement exists,
- exact `.kontrakt` grammar for Governance and named Manifests,
- external selection and trigger transport APIs,
- diagnostic attribution for selection and reselection failure,
- and integration with Contract History snapshots.

ADR-0055 decides Policy meaning and response selection. Governance must not absorb situation judgment merely because a
Policy or external trigger can request another Manifest.

---

## 8. Consequences

### Positive

One Interface Authority can expose several explicit operating modes without duplicating its public surface or hiding the
mode choice in configuration code.

Each Manifest keeps one mode's Contract arrangement inspectable while reusing the existing Interface Contract binding
vocabulary. Because activation requires an exact selection, CLI and runtime control mechanisms remain replaceable
carriers rather than Contract authority.

### Negative

Interfaces that need several operating modes must author and maintain each Manifest explicitly. Changing a Manifest
binding changes Governance meaning and therefore requires a new Governance Version.

Runtime reselection introduces a later activation-boundary decision that must be completed before unrestricted switching
between Manifests can be guaranteed.

### Neutral

Governance makes operating modes explicit but does not decide when one mode is preferable to another.

Policy, Version compatibility, trigger logic, monitoring, deployment control, and backend dispatch remain separate
responsibilities unless a later ADR explicitly assigns a narrower part of them to Governance.