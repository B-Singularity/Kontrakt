# ADR-0053: Version Contract, Sovereign Contract Revision History, and Realization Boundary

## Status

Accepted

## Date

2026-08-08

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0054: Policy Contract, Explicit Operating Policies, Self-Contained Contract Worlds, and Interface Binding Boundary
- ADR-0056: Governance Contract, Policy-World Control, Whole-Machine Coordination, and Selection Boundary
- ADR-0052: Capacity Contract, Explicit Safe Operating Memory Limits, and Realization Boundary
- ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication

---

## 1. Context

A Contract may change while remaining the same sovereign Contract Authority. Version records that revision history
without treating revisions as inheritance, compatibility, or Interface releases.

Each established Version is complete and immutable. A later Version may change any part of the Contract, but it does not
inherit, override, or contain an earlier one. The user decides whether a change continues an existing Contract Authority
or creates a different Contract.

---

## 2. Problem

Without an explicit Version boundary, a changed Contract can overwrite its own history and make earlier material appear
to have been governed by a meaning that did not yet exist. Treating every change as a new unrelated Contract causes the
opposite failure by destroying intentional continuity.

Kontrakt must preserve both facts: each Version has its own complete meaning, while several Versions may belong to one
continuing Contract Authority when the user declares that continuity.

---

## 3. Contract Decision

### 3.1. Authority, Version, and Meaning

A Contract Authority is the continuing sovereign authority of one explicitly declared Contract. A Version Identity names
one explicit revision of that authority.

```text
Contract Authority + Version Identity
    -> one Versioned Contract Meaning
```

Version is a coordinate of a Contract, not another one-dimensional Contract.

Within one Authority, the same Version Identity means Version Agreement and a different Version Identity means Version
Difference. Different Authorities have no Version relation merely because their names or meanings resemble each other.
The same Version Identity spelling may therefore be used independently by different Contract Authorities.

Version Identity is opaque and case-sensitive. `V2`, `Stable`, `Production`, or `Blue` carry no automatic order,
compatibility, preference, deprecation, or chronology. A new Authority may begin with any valid Version Identity; no
`V1` convention is required.

Changing only the Version Identity creates a different Version even when all other Contract material is identical. An
established Version name is never renamed in history.

Each Version contains the complete Contract meaning at that revision. Historical material remains attributable to the
Version under which it was established. Selecting the same Version again is allowed only when it denotes the same
already-established meaning.

### 3.2. Version Ownership

Every independently versioned, user-sovereign Contract Authority declares exactly one Version Identity.

| Own Version          | Ownership                             |
|----------------------|---------------------------------------|
| Fact                 | each independent Fact                 |
| Input Presentation   | each independent Input Contract       |
| Output Presentation  | each independent Output Contract      |
| Admission            | each independent Admission Contract   |
| Lowering             | each independent Lowering Contract    |
| Invariant            | each independent Invariant Contract   |
| Publication          | each independent Publication Contract |
| Machine              | the complete Machine Contract         |
| Budget               | each independent Budget Contract      |
| Capacity             | each independent Capacity Contract    |
| Governance           | each independent Governance Contract  |
| Policy               | each independent Policy Contract      |
| Declared Failure     | each independent failure Contract     |
| Diagnostic Evidence  | each independent evidence Contract    |
| Diagnostic Retention | each independent retention Contract   |

Material owned by another Contract does not gain its own Version. State and Transition belong to Machine; Raw/DTO
belongs to Input; Guard is Admission's role; Admitted is an internal result; Reference resolution is backend
realization; Lifecycle and Scope are not Contracts. Explicit `none` is Kontrakt canonical vocabulary and has no user
Version. Kontrakt-provided Canonicalization follows Kontrakt's own versioning responsibility.

A Fact's declared identity material belongs to that Fact's Version. A Machine Version covers its State names and
Transition declarations, including each Transition name, source, and target.

### 3.3. User Authoring

Kontrakt provides `Version` as reserved contract vocabulary. The declaration name is the Version Identity.

Carrier-shaped Contracts place it with their coordinates; order does not matter.

```kotlin
class CalculateInput private constructor(
    val x: Int,
    val Stable: Version,
    val y: Int,
)
```

Body-shaped Contracts use the same vocabulary in the body.

```kotlin
abstract class AccountMachine {
    abstract val Stable: Version

    abstract fun freeze(
        source: Open,
        target: Frozen,
    )
}
```

Exactly one `Version` declaration is required. Kontrakt does not synthesize `V1`, `Default`, a hash, or any fallback.

The Version declaration is contract material, not a factual, input, output, or runtime payload coordinate. Its runtime
representation may be erased or specialized by the backend.

The V1 Kotlin/JVM frontend accepts only backend-safe ASCII Version identifiers and rejects Kontrakt-reserved names.
Exact identifier grammar belongs to frontend realization, not Version semantics.

### 3.4. Revision and Contract Continuity

The user owns the boundary between revision and replacement. Kontrakt must not infer continuity from similarity, change
size, field overlap, stronger or weaker predicates, or implementation shape.

Normal modification of an existing declaration continues the same Authority. If its Contract meaning changes, a
different Version Identity is required.

An ordinary Contract name rename creates a new Authority. Preserving the same Authority across a rename requires a
Kontrakt-aware rename operation and a new Version.

```text
ordinary rename
    CalculateInput -> RequestInput
    = new Authority

Kontrakt-aware rename
    CalculateInput -> RequestInput
    = same Authority + new Version
```

File and package movement do not change Authority. If an Authority disappears from the current project snapshot and the
same Contract name later reappears, it continues the historical Authority by default. Creating a genuinely new Authority
under that name requires an explicit new-Contract operation.

### 3.5. Canonical Versioned Material

Version Identity is preserved in canonical Contract material.

```text
Versioned Contract Material
    Contract Authority
    Version Identity
    Contract-specific canonical material
```

Two Versions with identical contract-specific material remain different Versioned meanings when their Version Identities
differ.

Internal Authority IDs, history revision IDs, parent links, HIDs, storage keys, cache keys, paths, and other realization
material are not part of canonical Contract meaning. They may record an already-declared history but may never decide
it.

### 3.6. Airlock Rule

Input and Admission form the Airlock. Any change to their contract-bearing source material requires a new Version even
when later lowering would produce equivalent internal meaning.

For Contracts inside the Airlock, source representation may change without a Version change when lowered canonical
Contract meaning remains identical. Historical Version reselection uses the same rule.

---

## 4. V1 Contract History

### 4.1. Persistent History, Not Cache

V1 persists Contract history so established Versions survive compiler processes, clean builds, CI, and source checkout.
This is separate from the general disk cache and incremental compilation planned for V2. History is not disposable
optimization material and cannot be governed by cache eviction.

### 4.2. Immutable Version Records and Project History DAG

V1 history has two logical layers:

1. immutable Version records preserve every established Contract Version;
2. a project-level History DAG records each successful authoritative compilation as a complete Contract snapshot.

```text
Version records
    CalculateInput / Alpha
    CalculateInput / Beta
    CalculateBudget / Stable

R18 snapshot
    CalculateInput  -> Beta
    CalculateBudget -> Stable
```

A History Revision is logically complete, not a delta. Physical storage may reuse unchanged records or use
content-addressed structures.

The snapshot contains Contract information only: selected Authorities, Version Identities, canonical Contract material,
and explicit canonical values such as `none`. Compiler version, generated code, cache state, runtime layout, timestamps,
paths, and internal history IDs are not Contract snapshot material.

The Version record store retains all established historical Versions. A History Revision points only to the Contract set
selected in that revision.

### 4.3. Establishment and Publication

History changes only after a successful authoritative compilation has resolved, lowered, verified, and assembled a
publishable ContractImage. New Version records and the new History Revision are then published atomically. Failed
compilation leaves history unchanged.

The first successful authoritative compilation creates the Contract History Artifact. It is Kontrakt-generated project
material and is committed with the source repository.

Users do not directly edit historical records, rewrite Authority continuity, delete established Versions, or reset
Version reservations. Kontrakt provides no normal history reset, clear, or unreserve operation. Detected corruption,
mutation, or contradiction fails authoritative compilation.

A repository that deliberately removes all retained history cannot be distinguished from a genuinely new repository
without an external trust source. V1 does not make Git history or a remote registry part of Contract authority.

### 4.4. Absence and Reselection

Removing a Contract from the current snapshot does not remove its Authority or historical Versions.

```text
R17    CalculateBudget -> Stable
R18    CalculateBudget -> absent
R19    CalculateBudget -> Stable
```

`R19` reselects the existing immutable `Stable` Version when its authored material satisfies the same Version equality
rule; it does not establish another `Stable` record.

Explicit `none` is preserved as canonical snapshot material. It is neither missing history nor a Versioned Contract
Authority.

### 4.5. Branch and Merge

The project History DAG may branch and merge like version-control history. Parent and merge relations are history
realization metadata, not Version semantics.

Within one non-divergent history line, an established Version Identity is permanently reserved for its established
meaning. Divergent branches may independently establish the same Version Identity.

On merge:

```text
different Version Identity
    -> preserve both histories

same Version Identity + same meaning
    -> share the same historical fact

same Version Identity + different meaning
    -> semantic merge conflict
```

A conflict never overwrites either branch. The user resolves it by establishing a new Version for the merged Contract
meaning; both branch histories remain immutable ancestors.

```text
        Alpha
        /   \
   Beta(A)  Beta(B)
        \   /
         Gamma
```

Merge itself does not require a Version change. A new Version is required only when the merged Contract meaning is new
or resolves a Version conflict.

The History Artifact should be merge-friendly so the surrounding VCS can transport and combine generated records.
Kontrakt validates the semantic result; it does not replace the VCS.

CLI or IDE tooling may display establishment order, branches, and merge ancestry for inspection. That history does not
give Version names any ordering semantics.

---

## 5. Resolution and Realization Boundary

### 5.1. Interface Resolution

The Interface manifest selects Contracts according to ADR-0046 and ADR-0047. Version belongs to the selected Contract
declaration, so the Interface does not repeat it.

```text
contractPipeline {
    input CalculateInput
    admission XGreaterThanOne
    canonicalization DefaultPrimitiveCanonicalization
    lowering CalculateLowering
    invariant CalculateInvariant
    publication CalculatePublication
}
```

Resolving a Contract also resolves its declared Version. Interface assembly does not become another Version authority.
Which established Versions coexist, become active, or apply in a Contract World is deferred to ADR-0054.

### 5.2. Provenance and Claims

Kontrakt keeps Version provenance recoverable wherever later authoritative interpretation depends on knowing which
Authority and Version established material, evidence, or a judgment. This does not require a Version wrapper on every
host value.

A backend may keep provenance in canonical records, publication material, execution context, or another compact form
when exact Authority and Version remain recoverable without inference.

External request fields, persisted records, or protocol headers may present Version claims. Such claims are evidence
until resolved against Version material recognized by Kontrakt. Unknown or ambiguous claims must not be silently mapped
to a current or preferred Version.

### 5.3. Internal Identity

Kontrakt may use internal Authority IDs, canonical identities, HIDs, generated constants, tables, or interned integers
to preserve and resolve history efficiently.

An internal Authority ID may record continuity after an explicit Kontrakt-aware rename. It is not authored Contract
material, does not enter canonical Contract meaning, and cannot decide continuity by itself.

Changing realization mechanisms must not change the authored Authority, Version Identity, or Contract meaning.

---

## 6. Verification

Compilation must reject missing or duplicate Version declarations, runtime-computed or hidden Version authority,
conflicting reuse on one history line, unresolved semantic merge conflicts, detected history mutation, and unknown or
ambiguous Version material where exact resolution is required.

Verification must prove that Version Agreement requires the same Authority and Version Identity; different Version
Identities remain different even with identical contract-specific material; different Authorities have no Version
relation; historical Versions survive later revisions, absence, branch, and merge; reselection does not duplicate
Version records; explicit `none` remains canonical snapshot material; failed compilation leaves history unchanged; and
backend identities never enter canonical Versioned meaning.

Canonical identity and storage optimizations may accelerate these checks but cannot override canonical equality or
user-declared authority.

---

## 7. Contract and Implementation Boundary

Version owns sovereign revision identity and immutable historical meaning. It does not own compatibility, migration,
preferred/latest selection, Contract World applicability, cache policy, storage layout, compiler release identity, or
runtime dispatch.

Kontrakt realizes Version through canonicalization, persistent history, provenance, internal identities, and
deterministic resolution. These mechanisms are replaceable. The same authored Authority, Version, and Contract meaning
must resolve identically regardless of discovery order, registration order, classpath order, or backend layout.

The Version Contract contains no JVM-specific identifier grammar, disk format, internal ID format, hash layout, cache
key, file permission rule, or VCS command. Unchanged canonical material must not acquire a different physical identity
solely because compiler internals changed; that is a realization defect, not a reason to declare a new Version.
Transformation between revisions likewise remains with the Contract that owns that transformation.

---

## 8. Deferred Decisions

ADR-0054 will decide how established Versioned Contracts participate in Contract Worlds, including applicability,
coexistence, activation, and selection. Version itself does not define `latest`, `preferred`, `active`, `deprecated`, or
compatibility.

The exact History Artifact encoding, integrity mechanism, merge-friendly physical layout, internal revision identity,
atomic file publication strategy, CLI commands, IDE integration, and V2 disk-cache reuse remain implementation work.

External Version-claim syntax and transport-specific provenance encoding remain integration decisions. They must
preserve the Authority and Version established here without turning transport representation into Contract authority.

---

## 9. Consequences

### Positive

Version gives each user-sovereign Contract an explicit revision history while keeping every established meaning complete
and immutable. The committed History Artifact preserves revisions across builds and supports branch and merge without
rewriting the past. Its immutable Version records and complete project snapshots also provide a stable base for V2
incremental compilation and disk-cache reuse.

### Negative

Every independently versioned user Contract requires explicit Version authoring. Users must change Version when Contract
meaning changes and use explicit Kontrakt operations for rename continuity or a genuinely new Authority under an
existing historical name. Divergent branches may also require semantic Version merge resolution.

V1 cannot prove that a repository with all history deliberately erased was not created as a new repository unless an
external trust source is introduced.

### Neutral

Version records revision identity and history only. Governance, Policy, migration, compatibility, runtime optimization,
and storage engineering remain separate responsibilities.