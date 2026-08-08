# ADR-0053: Version Contract, Sovereign Meaning Identity, Version Claims, and Authority Boundary

## Status

Proposed

## Date

2026-08-08

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0055: Policy Contract, Established Situation, Response-Contract Selection, and Judgment Boundary
- ADR-0054: Governance, Contract World Applicability, and Activation Boundary
- ADR-0052: Capacity Contract, Explicit Safe Operating Memory Limits, and Realization Boundary
- ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication

---

## 1. Context

A contract may remain present while its meaning changes over time. An Input Presentation can change what it accepts. A
Lowering Contract can establish different Facts from the same presented material. An Invariant can judge an established
Fact differently. Publication can change which claim may leave the machine.

Those changes cannot be left to deployment order, adapter behavior, schema naming, or the newest executable artifact.
Material established under an earlier meaning must not silently acquire the authority of a later meaning merely because
the current machine can read it.

Version makes the identity of contract meaning explicit.

A Version does not tell the machine whether two meanings are compatible. It identifies the exact meaning whose authority
is being claimed. The Contract that owns that meaning retains the authority to recognize its own Version. External
material may present a Version claim, but the claim does not grant authority by itself.

This makes Version a sovereignty boundary rather than a release label.

---

## 2. Problem

Software uses version labels at many layers, from released artifacts and protocols to stored schemas and deployed
software. Those values may be useful operationally, but none of them can define the authority of a Kontrakt Contract.
Changing a compiler, library, deployment, or backend must not silently change which contract meaning governs existing
material.

The opposite problem is equally serious. If old material carries no exact meaning identity, a newer Contract may read
that material as though it had been established under the newer law. The values may have the same physical shape while
the judgment that made them valid has changed.

A Version must therefore identify contract meaning without absorbing the separate questions that follow from a mismatch.
Whether an earlier Version may still participate, whether material must be judged again, whether a replacement path is
available, and what response should follow are different authorities. Version must expose the identity difference
without deciding those questions itself.

---

## 3. Decision

### 3.1. Foundation

Version is the sovereign identity of contract meaning.

```text
Exact Contract Authority
    + Exact Version Identity
    -> Version-Qualified Contract Meaning
```

The Version belongs to the Contract whose meaning it identifies. It does not obtain authority from a deployment,
compiler, adapter, registry, transport header, package version, or runtime object.

A Version claim carried into a Contract says which meaning is being claimed for the material. The receiving Contract
compares that claim with its own authoritative Version identity. The claim is evidence presented to the Contract; it is
not an instruction that changes the Contract's authority.

### 3.2. Identity Only

Version answers one question:

```text
Under exactly which Contract meaning was this material,
judgment, evidence, or claim established?
```

Version does not answer what a difference between two identities permits or what should happen next. It also does not
decide which version-qualified Contracts may govern one application together.

Those decisions remain outside Version. Governance decides applicability of contract authorities and Policy selects a
response from already established situations. If material must be transformed or judged again, the Contract responsible
for that transformation or judgment retains that authority.

### 3.3. No Global Version Authority

V1 does not treat one Interface release number as a substitute for the Version identity of every Contract inside that
Interface.

Each exact Contract authority owns the identity of its own meaning. A change to one Contract does not force unrelated
Contracts to acquire a new Version merely to keep a shared release number synchronized.

One Contract's Version cannot grant, replace, or imply the Version authority of another Contract. A later Governance ADR
may establish that several version-qualified Contracts are applicable together, but that relationship does not collapse
their individual identities into one global Version.

### 3.4. No Ordering Semantics

Version identity does not imply that one meaning is newer, greater, safer, preferred, or compatible with another.

A source spelling such as `V1` or `V2` may be convenient for users, but Kontrakt does not derive an ordering law from
that spelling. Major, minor, patch, date, sequence, or lexical structure has no contract authority unless another
Contract explicitly defines a relation that uses it.

For Version itself, equality is the required relation.

### 3.5. Meaning Change and Version Reuse

A Contract may keep the same Version while its realization changes, provided the contract-visible meaning remains the
same. Replacing a cache, changing a memory layout, fusing implementation stages, changing generated code, or selecting a
different backend does not require a new Version when the Contract still establishes the same meaning.

A contract-visible meaning change must not reuse the same sovereign Version identity. Reusing one Version identity for
two different meanings would make previously established material ambiguous and would allow a later machine to claim an
authority that did not exist when the material was produced.

Version therefore follows contract meaning rather than implementation history.

---

## 4. Version Contract

### 4.1. Sovereign Contract Identity

A Version is meaningful only together with the exact Contract authority that owns it.

```text
CalculateLowering + V3
```

and

```text
CalculateInvariant + V3
```

are different version-qualified authorities even when the source spelling of the Version is the same. `V3` alone does
not identify a Contract meaning.

This prevents a shared version token from becoming a hidden global namespace. The authoritative identity is the exact
Contract together with its exact Version.

### 4.2. Presented Version Claim

Material that crosses a version-sensitive contract boundary may carry a Version claim showing the meaning under which it
was established.

The claim remains declarative. It cannot change the Version owned by the receiving Contract and cannot force that
Contract to accept the material.

```text
presented material claims Contract@V2

receiving Contract owns Contract@V2
    -> Version identity agrees

receiving Contract owns Contract@V3
    -> Version identity differs
```

Exact agreement establishes only that the material presents the same sovereign meaning identity. The receiving Contract
must still perform its own substantive judgment. Version agreement does not make an invalid Input admissible, satisfy an
Invariant, establish a State transition, or authorize Publication.

### 4.3. Version Difference

A Version difference is an established identity difference, not a compatibility judgment.

When the presented Version and the receiving Contract's authoritative Version differ, Version does not silently
reinterpret the material and does not infer a failure policy. It preserves the exact identities involved so that the
authority responsible for applicability or response can act on that established situation.

This prevents an old judgment from becoming a new judgment merely because its values can still be decoded.

### 4.4. No Implicit Compatibility

Version contains no backward-compatibility, forward-compatibility, range, substitution, or migration law.

A different Version may later be accepted by an explicit Governance relation. A Policy may select a response to an
established mismatch. A Lowering, replacement, or other responsible Contract may establish new material from earlier
material. None of those permissions are implied by Version identity itself.

The absence of an explicit compatibility authority therefore cannot be replaced by version-name comparison, numeric
ordering, latest-version preference, or backend convention.

### 4.5. Authority of Established Material

When material, a judgment, diagnostic evidence, or a published claim must retain the meaning under which it was
established, its Version coordinate must remain attached to that authority-bearing record or be recoverable from
canonical material without ambiguity.

The backend may encode that coordinate compactly, but it may not discard it and later reconstruct meaning from whichever
Contract happens to be current.

The exact retention requirements differ by Contract role. Version defines the identity law; the Contract that produces
or consumes the material defines where that identity must be carried.

### 4.6. Relation to Contract World

Version does not create a Contract World and does not select one.

A Contract World may contain several independently version-qualified Contract authorities. Governance decides whether
those authorities are applicable together for one machine application. Version contributes exact sovereign identities to
that decision and nothing more.

This keeps Version independent from deployment rollout and avoids turning one global version number into a second
Interface manifest.

### 4.7. Canonical Material

The canonical Version material is deliberately small. It contains the exact Contract authority and the exact Version
identity owned by that Contract.

```text
exact Contract authority
exact Version identity
```

A transport spelling, source declaration name, numeric representation, package location, generated host type, registry
entry, or backend integer ID is not canonical Version authority by itself. Those forms may carry or reference the
canonical identity after resolution.

Within one resolved Contract definition, the same exact Contract authority cannot own two different authoritative
Versions at the same time. If several Versions of one logical Contract are retained for coexistence, each must resolve
as a distinct version-qualified authority rather than as ambiguous current meaning.

---

## 5. V1 User Authoring and Processing Boundary

The V1 frontend must make both coordinates of Version authority explicit: the exact Contract whose meaning is being
identified and the exact Version identity assigned to that Contract.

```text
restricted Version source evidence
    -> exact Contract resolution
    -> exact Version identity resolution
    -> validation
    -> canonical Version material
    -> use by version-sensitive contract processing
```

The exact Interface placement and selection form for Version is intentionally deferred in this draft. Whatever source
form is selected, enclosure must not supply an omitted Contract subject and must not create a global Interface Version.
Every Version entry must identify its exact Contract authority explicitly.

The source form must remain declarative. Host-language mechanisms that acquire meaning through execution, inheritance,
metadata discovery, runtime lookup, or hidden defaults cannot establish Version authority. A Version value also cannot
be computed by arbitrary user code or recovered from a naming convention.

The exact Kotlin and Java authoring syntax is deferred with that placement decision. The syntax must express only the
two canonical coordinates required here and must follow the same restricted frontend discipline used by the other
Contract presentations. The compiler must be able to resolve and erase the host source form without executing it.

Source names may be used for reading and diagnostics, but a name such as `V2`, `2026`, or `Stable` acquires no ordering,
compatibility, migration, or policy meaning from its spelling.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Authority

The exact Contract and exact Version identity are contract authority. Operational version labels attached to builds,
compilers, deployments, storage revisions, schemas, or transport carriers are not substitutes for that authority.

An implementation may carry the canonical Version through an integer, hash, interned ID, table index, generated
constant, or other compact representation. Replacing that representation must not change which Contract meaning the
Version identifies.

### 6.2. External Carriers

A protocol field, persisted record, request header, or adapter may carry a Version claim into Kontrakt. That carrier is
transport evidence only. The frontend and runtime realization must resolve it to exact contract-owned Version material
before it can participate in contract judgment.

Unknown, malformed, or ambiguous carrier values cannot be silently mapped to the current Version. The exact failure and
response path remain outside Version and will be assigned to the responsible Governance, Policy, Diagnostic, or boundary
contract.

### 6.3. Realization Changes

A backend optimization does not create a new Contract Version when contract-visible meaning is preserved. Conversely, a
backend or deployment cannot keep an old Version identity while deliberately changing the meaning that the Contract
establishes.

Version therefore protects the replaceability boundary in both directions: implementation change alone does not force
contract evolution, and implementation choice cannot disguise contract evolution.

### 6.4. Deterministic Resolution

Deterministic resolution remains a Kontrakt implementation law rather than Version material. The same resolved Contract
and Version source evidence must produce the same canonical Version identity.

Runtime discovery order, registration order, classpath order, or deployment timing may not decide which Version a
Contract owns.

---

## 7. Verification

Verification must establish that every Version entry resolves to one exact Contract authority and one exact Version
identity without ambiguity. The Version must belong to that Contract rather than to a runtime object, implementation
stage, backend artifact, or unrelated Interface.

The verifier must reject duplicate or contradictory current Version authority for the same exact Contract definition. It
must also reject source forms whose meaning depends on ordering, numeric comparison, implicit latest-version choice,
runtime computation, or hidden defaults.

Version-boundary tests must prove that exact identity agreement is preserved, a mismatch remains an explicit mismatch,
and neither case performs the substantive judgment of the receiving Contract. A mismatch must not be silently accepted,
rewritten to the current Version, or treated as compatible merely because the physical data shape is readable.

Where canonical material retains Version provenance, tests must prove that serialization, caching, lowering,
publication, and backend replacement do not erase or substitute that identity. Compact backend encodings must round-trip
to the same exact Contract and Version coordinates.

Cross-build detection of accidental Version reuse for changed contract meaning may require retained historical canonical
material or an external publication discipline. The exact mechanism is deferred; the semantic rule against reuse is not.

---

## 8. Deferred Decisions

The exact Kotlin and Java authoring syntax remains to be completed after the semantic model is accepted. That work must
also decide explicit absence, canonical identity bytes, and which Contract roles are required to retain Version
material.

ADR-0054 will define how version-qualified Contract authorities form an applicable Contract World and how that world is
fixed for one application. ADR-0055 will define responses to already established situations. Compatibility, rejudgment,
replacement, migration, and fallback are therefore not added to Version merely to anticipate those later contracts.

The transport representation of Version claims and any retained history used to detect cross-build identity reuse also
remain separate decisions. Diagnostic, Failure, and Publication integration will be specified by their own authority
boundaries.

---

## 9. Consequences

### Positive

Version becomes a small contract with one clear purpose. Every participating Contract can retain control over the exact
meaning it recognizes without delegating that authority to deployment, schema, adapter, or compiler conventions.

Old material cannot silently become valid under a new meaning. At the same time, Version does not overreach into
compatibility or migration, so Governance, Policy, Lowering, Invariant, Publication, and other Contracts can keep their
own judgment authority.

Independent Contract Versions also avoid unnecessary global churn. A change to one Contract does not require unrelated
Contracts to change identity when their meaning is unchanged.

### Negative

Version provenance must remain available wherever later processing depends on the meaning under which material was
established. That adds canonical material and verification work compared with systems that simply assume the newest
schema or implementation is authoritative.

A Version mismatch is intentionally not self-resolving. Systems that need coexistence, compatibility, rejudgment, or
migration require the later Governance and Policy contracts or another explicit responsible Contract.

### Neutral

Version does not turn release order, deployment preference, compatibility handling, transformation, or response
selection into identity authority. Those capabilities may exist elsewhere without becoming part of Version.