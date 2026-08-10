# ADR-0054: Governance Contract, Named Interface Manifests, Explicit Contract World Selection, and Activation Boundary

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

One Interface may need to operate under several explicit Contract configurations without becoming several different
Interfaces.

Normal operation, emergency operation, compatibility operation, or another user-defined operating arrangement may keep
the same public Interface and operation set while changing the Contracts bound to those operations.

Duplicating the Interface for each arrangement would split one Interface Authority merely because its operating Contract
World changed.

Governance gives that variation one explicit place.

```text
Interface Authority
    -> Governance Contract
        -> named Manifest
        -> named Manifest
        -> named Manifest
```

Each Manifest presents one complete Contract binding arrangement for the same Interface. Governance does not decide
which arrangement is desirable. It establishes the arrangement named by explicit selection.

---

## 2. Problem

ADR-0046 gave one Interface an explicit manifest binding. That is sufficient when one Contract arrangement governs every
use of the Interface.

It is insufficient when the same Interface must support several declared operating arrangements.

Without Governance, users must either duplicate the Interface, rewrite its bindings between environments, or move the
choice into configuration and runtime code. The first destroys Interface continuity. The others hide Contract World
selection outside Contract authority.

Version does not solve this problem. ADR-0053 lets every sovereign Contract Authority identify its own established
meaning, but it does not say which established Contracts form the active Contract World.

Policy does not solve it either. Policy selects a prepared response for an established situation inside an already
applicable Contract World. It does not establish that world.

Governance therefore needs a narrow responsibility: present the valid named Contract arrangements of one Interface and
establish one of them from explicit selection.

---

## 3. Contract Decision

### 3.1. Governance Authority

Governance is an independently versioned user-sovereign Contract Authority.

A Governance Contract belongs to one Interface Authority and declares the named Manifests under which that Interface may
operate.

```text
Governance
    -> declared Manifest set
    -> explicit Manifest selection
    -> active Contract World
```

Governance does not infer operating conditions, rank Manifests, choose a preferred Manifest, or execute the Contracts
inside one.

Its Version follows ADR-0053. Changing Governance meaning requires a new Governance Version.

### 3.2. Manifest

A Manifest is Governance-owned canonical material. It is not an independent Contract Authority and has no independent
Version.

Each Manifest has one user-authored nominal identity.

```text
Normal
Emergency
Legacy
Blue
```

The spelling carries no built-in priority, safety level, environment meaning, chronology, or fallback behavior.
`Normal` is not automatically preferred and `Emergency` is not automatically exceptional.

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

If an operating arrangement requires a different public operation surface or a different Interface responsibility, it is
not another Manifest of the same Interface. It requires another Interface Authority.

Manifest variation changes the Contract World, not the identity of the Interface surface.

### 3.4. Contract Binding

A Manifest binds established Contract material through the existing Interface Contract vocabulary.

It may select different Contracts for different operating arrangements, but it does not redefine the meaning owned by
those Contracts.

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

Governance receives an explicit Manifest Identity as selection material.

```text
selected Manifest Identity
    -> Governance
    -> exact declared Manifest
    -> active Contract World
```

The source of that selection is not Governance authority. A CLI, runtime control surface, operator action, external
control plane, or another realization may provide the input.

Conceptually:

```text
--manifest Normal
```

and:

```text
select Emergency
```

present the same Contract-level fact to Governance: an exact Manifest Identity was selected.

Kontrakt must not derive the selection from a Manifest name, environment name, registration order, discovery order,
runtime telemetry, or hidden default.

If selection is required and no exact Manifest Identity is supplied, Governance does not guess one.

### 3.6. Runtime Reselection

A later explicit selection may request another declared Manifest.

```text
Normal
    -> explicit selection of Emergency
    -> Emergency
```

Governance does not decide when that request should be made. A monitoring system, operator, Policy result, control
plane, or other external mechanism may cause the request, but the trigger source does not become Governance meaning.

The trigger must be reduced to explicit Manifest selection before Governance consumes it.

Automatic conditions such as the following are therefore not Governance declarations:

```text
if load is high -> Emergency
if latency rises -> Degraded
if client is old -> Legacy
```

The logic that decides to request another Manifest belongs outside Governance.

This ADR does not yet define restrictions between one active Manifest and another.

### 3.7. Contract World

For Governance, a Contract World is the exact established Contract arrangement represented by one selected Manifest of
one Governance Version.

```text
Interface Authority
+ Governance Version
+ Manifest Identity
+ resolved Manifest bindings
    -> one active Contract World
```

The Contract World is not a runtime object, registry entry, configuration file, or deployment name. Those may realize or
carry the selection, but they do not own its meaning.

Two Manifests may bind some of the same Contract Versions. They remain different named Contract Worlds when their
Manifest Identities differ.

### 3.8. Separation from Policy

Governance establishes which Contract World is in force.

Policy operates inside an already established Contract World and selects a prepared response Contract for an established
situation.

```text
Governance
    explicit Manifest selection
    -> active Contract World

Policy
    established situation inside that world
    -> prepared response Contract
```

Governance contains no situation-matching algorithm. Policy does not replace the active Manifest merely because it
selects a different response.

The exact Policy model remains ADR-0055.

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

Governance owns named Manifest meaning and explicit Contract World establishment.

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

One Interface Authority can support several explicit operating Contract Worlds without duplicating its public surface or
moving Contract selection into hidden configuration.

Named Manifests keep each operating arrangement inspectable and versioned through one Governance Authority while reusing
the existing Interface Contract binding vocabulary.

Explicit selection prevents environment guesses, implicit defaults, and runtime heuristics from becoming Contract
authority. CLI and runtime control mechanisms remain replaceable.

### Negative

Interfaces that need several operating Contract Worlds must author and maintain each Manifest explicitly. Changing a
Manifest binding changes Governance meaning and therefore requires a new Governance Version.

Runtime reselection introduces a later activation-boundary decision that must be completed before unrestricted switching
between Manifests can be guaranteed.

### Neutral

Governance does not decide which operating mode is desirable. It exposes declared Manifests and establishes the one
named by explicit selection.

Policy, Version compatibility, trigger logic, monitoring, deployment control, and backend dispatch remain separate
responsibilities unless a later ADR explicitly assigns a narrower part of them to Governance.