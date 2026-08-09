# ADR-0053: Version Contract, Sovereign Contract Revision History, and Realization Boundary

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
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication

---

## 1. Context

Version is a common engineering device for preserving continuity while an engineered product is revised. Materials,
interfaces, dimensions, control laws, manufacturing limits, or other defining details may change between revisions
without requiring the design authority to pretend that nothing changed. Each revision can remain identifiable as part of
the same product history until the responsible authority decides that the change establishes a different product
instead.

A Contract needs the same distinction, but with stronger authority semantics. A one-dimensional Contract may be revised
while remaining under the same sovereign Contract Authority. The meaning established at each Version is different when
the Contract has been modified, yet the revision remains part of the explicit history of that authority because the
author declared that continuity.

This is different from inheritance or type inclusion. Those mechanisms can place different meanings under one structural
or set relation and then treat the relation itself as evidence of sameness or substitutability. Version does not combine
meanings in that way. It records that one Contract Authority has been explicitly revised and keeps each revision visible
as its own complete authoritative meaning.

---

## 2. Problem

A Contract that changes without an explicit Version boundary can silently overwrite its own history. Material
established under an earlier meaning may then be interpreted as though the current meaning had always governed it, even
when the Contract was different at the time that material was judged or published.

The opposite error is to treat every modification as a new unrelated Contract. That destroys the continuity that Version
exists to express and makes it impossible for the Contract to state that an earlier and later meaning belong to the same
sovereign history.

Kontrakt must therefore preserve two facts at once: each Version has its own complete meaning, and several Versions may
belong to one continuing Contract Authority when the author explicitly declares that relationship. Version must express
that continuity without becoming inheritance, compatibility analysis, migration logic, or a release label for the
enclosing Interface.

---

## 3. Contract Decision

### 3.1. Contract Authority and Version

A Contract Authority is the continuing sovereign authority of one explicitly declared Contract. It answers which
Contract owns the right to establish and revise the meaning being governed.

A Version Identity identifies one explicit revision of that authority's meaning. Version therefore acts as a coordinate
in the Contract's sovereign history rather than as a second Contract name or a global software release number.

```text
Contract Authority
    + Version Identity
    -> one Versioned Contract Meaning
```

`Sovereign` means that continuity belongs to the Contract declaration itself. A compiler release, generated artifact,
runtime type, deployment label, or external carrier may represent that continuity, but none may decide that two meanings
belong to the same Contract Authority or split one authority into another on the user's behalf.

### 3.2. Complete Meaning at Each Version

Each Version establishes the complete authoritative meaning of the Contract at that Version. A later Version may change
part or all of that meaning, but it does not inherit the earlier meaning, override a parent Contract, or become a
subtype of a previous revision.

```text
Contract Authority: CalculateInvariant

V1
    complete contract definition at V1

V2
    complete contract definition at V2
```

The contract definitions at `V1` and `V2` may differ because the Contract was modified. Version does not hide that
difference. It records the modification while preserving the sovereignty of `CalculateInvariant` across those revisions.

The Contract Authority is not a larger Contract formed by combining the definitions at `V1` and `V2`, a common body
shared by them, or a set that contains their meanings. Its continuity carries no inherited obligation from one Version
to another.

### 3.3. Explicit Revision Ownership

The author must explicitly assign a Version Identity to every executable one-dimensional Contract meaning. There is no
implicit `V1`, compiler-generated Version, inherited Version, or default supplied by the enclosing Interface.

When the author modifies a Contract and intends that modification to remain part of the same Contract Authority, the new
meaning is established under a different Version Identity of that authority. The earlier Version remains unchanged.

```text
V1
    authoritative contract definition at V1

V2
    authoritative contract definition at V2
```

Reusing `V1` for the later contract definition would erase the declared history and is therefore contradictory. A
Version Identity, once attached to one authoritative meaning of its Contract Authority, must not later denote another
meaning.

### 3.4. Continuing an Authority or Declaring a New Contract

The author decides whether a modification continues an existing Contract Authority or establishes a different Contract.
Kontrakt does not infer that choice from structural similarity, the size of the change, host-language names, or shared
implementation material.

This decision must be made before Version authoring is reduced to an API operation. When a Contract is modified, the
author must deliberately judge whether the modification still belongs to the sovereign history of that Contract or
whether it establishes another Contract Authority. Assigning a new Version is the explicit declaration of the first
choice; declaring another Contract Authority is the explicit declaration of the second.

The distinction has no mechanical threshold. A substantial revision may remain part of one Contract Authority, while a
small textual or structural change may accompany the decision to establish another Contract. The author owns that
semantic boundary, and the authoring API must expose the choice rather than make it implicitly.

### 3.5. Version Relation

Version comparison is defined only inside one Contract Authority.

```text
same Contract Authority
+ same Version Identity
    -> Version Agreement

same Contract Authority
+ different Version Identity
    -> Version Difference

different Contract Authority
    -> no Version relation
```

Agreement identifies the same revision position of the same sovereign Contract. Difference establishes only that the
Contract Authority has distinct declared revisions. Neither result states whether the revisions are compatible,
substitutable, migratable, newer, preferred, or acceptable for a particular application.

A Version token therefore has no global meaning by itself. The same spelling may be used independently by different
Contract Authorities without creating a relationship between them.

### 3.6. Historical Stability and Authority Boundary

A later Version does not rewrite or invalidate the meaning of an earlier Version. Historical material may remain
attributable to the revision under which it was established even when new work uses a later one.

Version Identity is opaque. A spelling such as `V2`, `2026`, or `Stable` does not by itself establish chronology,
compatibility, deprecation, or preference. The Contract's revision history exists because the author explicitly declared
those Versions under one Authority, not because Kontrakt interprets their names.

Version stops at sovereign revision identity. Governance determines which versioned Contract Authorities are applicable
in a Contract World. Policy chooses responses to established situations, and any transformation between revisions
remains with the Contract that owns that transformation.

---

## 4. Kontrakt Realization

This section defines how Kontrakt realizes the Version Contract. The material and mechanisms below preserve the laws in
Section 3; they do not become an independent source of Contract or Version authority.

### 4.1. Canonical Versioned Contract Meaning

Kontrakt resolves the complete authoritative material of one Contract at one Version into canonical form. This canonical
material represents the Versioned Contract Meaning that the user declared.

It contains the contract coordinates and laws that belong to that Versioned meaning. It excludes host-language carrier
names, generated artifacts, deployment state, Governance applicability, and backend layout because those mechanisms do
not define what the Contract means.

Kontrakt may derive an implementation identity from the canonical material so that one Versioned meaning can be
compared, retained, and referenced efficiently. ADR-0041 provides the canonical identity and HID machinery for that
realization. The derived identity is not the Contract Authority and does not create the Version relationship.

### 4.2. Canonical Authority-Version Binding

Kontrakt realizes the user's declaration by binding one Contract Authority and one Version Identity to the canonical
meaning established for that revision.

```text
(Contract Authority, Version Identity)
    -> one Canonical Versioned Contract Meaning
```

If retained historical material proves that the same Authority and Version were previously bound to a different
canonical meaning, Kontrakt can reject the contradiction. The check enforces the history declared by the user; it does
not decide whether two differently written meanings are mathematically equivalent.

Canonical identity machinery may accelerate this comparison, but fingerprints and interned identifiers remain physical
representations. They do not define sovereign continuity and cannot decide that a modification should become a new
Version or a new Contract Authority.

### 4.3. Authoring and Compilation Lifecycle

Version authority is authored first and realized second.

```text
AUTHORING

explicit Contract Authority
    + explicit Version Identity
    + complete Contract meaning

        ↓

COMPILATION

resolve the declared authority and Version
    ↓
lower the Versioned meaning to canonical material
    ↓
derive implementation identity where needed
    ↓
establish the canonical Authority-Version binding
    ↓
preserve that exact revision in resolved Interface bindings
```

The compiler does not inspect the new meaning and decide whether it is a revision or a different Contract. That decision
must already be present in the author's declaration before realization begins.

### 4.4. Interface Resolution

The Interface manifest selects one-dimensional Contracts according to ADR-0046 and ADR-0047. Resolution must preserve
the Contract Authority and the exact Version selected for executable use.

The Interface does not become a second owner of Version history. It assembles already declared contract authority rather
than reassigning Version identities. The exact source syntax for selecting one revision or exposing several revisions is
deferred, but every binding used by the backend must resolve to one explicit Versioned Contract meaning before
execution.

Whether several revisions may coexist in one executable, how one becomes applicable, and how an application is pinned to
a particular Contract World belong to Governance rather than Version.

### 4.5. Provenance Realization

Kontrakt must keep Version provenance recoverable wherever later authoritative interpretation depends on knowing which
revision established material, evidence, or a judgment. This requirement does not imply a Version wrapper on every host
value.

A backend may retain provenance in an enclosing canonical record, publication material, execution context, or another
compact representation when the exact Contract Authority and Version remain recoverable without inference. Material that
leaves such a context and is expected to be interpreted later under its original authority must retain a resolvable link
to that provenance.

External request fields, persisted records, or protocol headers may present Version claims. Such claims are evidence
until resolved against the Contract Authority and Version material recognized by Kontrakt.

### 4.6. Runtime Resolution

At runtime, the backend may resolve a presented claim or retained provenance against the exact Contract Authority and
Version required by later processing.

Physical realization may use generated constants, canonical tables, interned integers, HIDs, or specialized branches.
Changing those mechanisms must not change whether two references resolve to the same Authority and Version.

Unknown or ambiguous material cannot be silently mapped to a current or preferred revision. Version realization may
establish agreement, difference, absence of a Version relation, or failed resolution. The authority responsible for the
next judgment decides what that result means for execution.

### 4.7. Historical Verification

When previous Authority-Version bindings are retained or supplied, Kontrakt may compare them with a new compilation to
detect accidental reuse of one Version Identity for a different canonical meaning. Without historical material, V1 does
not claim knowledge of declarations that the machine has never retained or received.

Canonical identity stability is an implementation obligation. Unchanged canonical material must not acquire a different
physical identity merely because compiler internals changed. Such a defect must be corrected in the realization rather
than hidden by forcing the user to declare a new Version.

---

## 5. V1 User Authoring and Processing Boundary

V1 requires an explicit user-authored Contract Authority and Version Identity for every executable one-dimensional
Contract meaning. The exact Kotlin, Java, or `.kontrakt` syntax remains open, but omission is illegal and no frontend
may synthesize continuity or Version on the user's behalf.

The authoring model must express the semantic decision established in Section 3.4 directly: revise an existing Contract
Authority under a new Version, or declare a different Contract Authority. That choice must not be encoded through
inheritance, type inclusion, runtime registration, reflection, or similarity analysis.

Version material must remain declarative and statically resolvable. Arbitrary code cannot compute the Version relation
at runtime, and host-language names cannot acquire authority merely because the backend can observe them.

The Interface author does not repeat Version information that already belongs to the selected contract declaration. If
future syntax exposes several revisions for one Authority, each executable choice must still resolve to one explicit
Versioned Contract meaning before backend use.

---

## 6. Contract and Implementation Boundary

The Version Contract owns the continuity of one Contract Authority across explicitly declared revisions, the immutable
meaning assigned to each Version, and the distinction between Version Agreement, Version Difference, and absence of a
Version relation.

Kontrakt realizes those laws by canonicalizing Versioned meanings, deriving efficient physical identities, preserving
Authority-Version bindings, carrying provenance, and resolving claims. These mechanisms may be replaced without changing
the Version Contract.

No realization mechanism may decide that two Contracts share an Authority, split one Authority into another, invent a
Version, or reinterpret the user's declared history. Likewise, Version realization must not turn revision identity into
compatibility, Contract World applicability, migration, or response policy merely because the backend has enough data to
perform those computations.

Deterministic resolution remains an implementation law. The same authored Authority, Version, and Contract meaning must
produce the same canonical realization independent of classpath order, registration order, discovery timing, or backend
layout.

---

## 7. Verification

### 7.1. Contract Verification

Compilation must reject executable one-dimensional Contract material with no explicit Version, an unresolved Version, or
a Version whose authority depends on hidden defaults or runtime computation.

Verification must preserve the user's declared boundary between revision and replacement. A new Version under an
existing Contract Authority is treated as a revision of that authority; a separately declared Authority has no Version
relation to it merely because the two meanings resemble each other.

Version relation tests must prove that agreement requires the same Contract Authority and Version Identity, that a
different Version under the same Authority yields Version Difference, and that different Authorities do not participate
in one Version comparison.

Historical provenance tests must show that material remains attributable to the Version under which it was established
even when later code contains another revision of the same Contract Authority.

### 7.2. Realization Verification

The verifier must derive canonical material for each resolved Versioned Contract meaning and establish the corresponding
Authority-Version binding. A conflicting binding fails when the historical comparison material needed to prove the
conflict is available.

Canonical comparison must use the complete authoritative material of the selected Version and exclude host carrier
names, Interface assembly, Governance applicability, and backend realization. Fingerprints may accelerate equality
checks, but physical hash behavior cannot override canonical identity.

Backend conformance must prove that compact encodings round-trip to the same Contract Authority, Version Identity, and
canonical meaning and that provenance survives every supported path that promises later authoritative interpretation.

---

## 8. Deferred Decisions

The Kotlin, Java, and `.kontrakt` syntax for declaring a Contract Authority and its Versions remains open. The same work
must decide how an author naturally expresses a revision of an existing Authority versus the declaration of a new
Authority without introducing a separate user-managed stable identifier.

ADR-0054 will define how versioned Contract Authorities participate in Contract Worlds, including coexistence,
selection, activation, and application lifetime. ADR-0055 will define responses to established situations.
Compatibility, fallback, rejudgment, and migration therefore remain outside Version.

The external transport spelling of Version claims, publication format for historical bindings, and exact retention rules
for each authority-bearing material kind remain integration decisions. Any such mechanism must preserve the revision
history fixed here without turning Version into a wrapper attached indiscriminately to every value.

---

## 9. Consequences

### Positive

Version gives a Contract an explicit sovereign history without pretending that different revisions have the same
meaning. The author can preserve continuity when a Contract is revised or deliberately end that continuity by declaring
a new Contract Authority.

Each Version remains complete and immutable, so historical material can be attributed to the exact meaning that governed
it. The model avoids inheritance and type-set interpretation because no Version receives meaning from another and the
Contract Authority is not a supertype or shared contract body.

Kontrakt can realize revision identity through existing canonical identity machinery while keeping authorship in the
Contract. Backend storage, hashing, lookup, and provenance strategies remain replaceable.

### Negative

Every executable one-dimensional Contract meaning requires explicit Version authoring, and the user must decide when a
modification continues an existing Authority or begins another one. Kontrakt deliberately refuses to infer that semantic
choice from similarity.

Cross-build drift detection requires previous Authority-Version binding material to be retained or supplied. Kontrakt
cannot verify history that is unavailable to it.

### Neutral

Version records one Contract Authority's explicit revision history. Governance decides where particular revisions apply,
Policy decides responses to established situations, and transformation remains with the Contract that owns it.