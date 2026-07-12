# ADR-0049: Flow Contract Processing — Fact Acceptance and Publication

## Status

Draft

## Date

2026-07-12

## Related

- `docs/what-contract-is.md`
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Generated Host Interface Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication

---

## 1. Context

ADR-0048 defines the first half of flow processing.

Input establishes judgeable boundary presentation. Admission decides whether that material may continue.
Canonicalization establishes a stable representative. Lowering forms a core-owned candidate under a declared contract
world.

That candidate is not yet truth.

A candidate may be internally shaped for the core and still make a false, incoherent, conflicting, or unauthorized claim.
The machine therefore needs a separate boundary for factual meaning, invariant acceptance, and publication.

This ADR defines the remaining flow contracts:

```text
Fact
Invariant
Publication
```

These contracts answer three different questions:

```text
Fact:
    what factual meaning does the candidate claim?

Invariant:
    may the core believe and accept that claim?

Publication:
    what outward claim may be produced from accepted material?
```

Failure and diagnostic representation, movement, and bounds remain separate category concerns. This ADR preserves their
attribution boundaries but does not define their complete processing profiles.

---

## 2. Problem

Without a separate Fact contract, a lowered carrier or backend layout can become the factual authority by accident.

Without a separate Invariant contract, successful conversion can be mistaken for truth. A candidate may have the right
shape and still violate identity, coherence, ownership, state, or relation law.

Without a separate Publication contract, accepted core material can leak outward merely because it is immutable or easy
to serialize. That would couple external users to internal representation and would collapse core knowledge into public
claim.

The machine needs a clear point at which a candidate receives accepted core authority and a separate point at which some
presentation of that accepted material may leave the machine.

---

## 3. Decision Drivers

Fact must be contract material, not a class, row, DTO, record, object graph, or storage layout.

Lowering must remain distinct from acceptance. Structural availability is not truth.

Invariant must judge a declared claim under explicit identity, version, and core coordinates. It must not wander through
implementation object graphs or use runtime references as authority.

Accepted fact must remain immutable under its contract identity.

Publication must be an explicit judgment. Immutability does not make core material public.

External publication must expose a publication presentation, not the internal Fact carrier or backend representation.

A backend may optimize acceptance and emission, but it may not change factual meaning, acceptance law, or publication
permission.

---

## 4. Decision

ADR-0049 defines the second half of flow processing:

```text
lowered core-owned candidate
-> Fact claim
-> Invariant judgment
-> accepted immutable core material
-> Publication judgment
-> outward claim or declared publication stop
```

The Fact contract names the factual meaning under which the candidate asks to be judged. It does not itself make the
claim true.

The Invariant contract is the acceptance authority. Acceptance grants the candidate the right to stand as immutable core
material under the selected contract world. Rejection produces no accepted fact.

The Publication contract is a later outward-claim authority. Accepted core material does not leave the machine directly.
Publication decides whether an outward presentation is permitted and which information that presentation may contain.

The common authority law is:

```text
Lowering creates a candidate.
Invariant grants or refuses core belief.
Publication grants or refuses outward claim.
```

No implementation class, generated serializer, repository row, object reference, cache entry, or frozen storage layout may
replace any of those judgments.

---

## 5. Flow Processing Profiles

### 5.1. Fact Contract

Fact is core factual material.

A fact is not the object, row, message, or value that may carry it in software. Those are carriers or realizations. The
fact is the material the core may stand on.

Kontrakt lowers the fact contract into the law for factual material: how it is identified, what meaning it belongs to,
and why it remains immutable once accepted.

A candidate fact becomes an accepted immutable fact only after the invariant accepts it.

### 5.2. Invariant Contract

Invariant is the acceptance judgment over candidate material.

Lowering says what the candidate claims to be. Invariant decides whether the machine may believe that claim.

A lowered candidate must arrive with a declared meaning. It may claim to stand under a named fact contract, with identity
material, version meaning, and the core coordinates this pipeline has already named for judgment. Those coordinates are
not object references. They do not invite graph wandering. They only name the accepted core material this judgment is
allowed to consider.

The invariant decides whether the claim is coherent, whether identity collides with accepted material, and whether
accepting the candidate would make the core lie. If no declared meaning fits, or more than one meaning fits, the machine
must stop with declared failure.

Invariant is not a validator drawer. It is the point where a candidate either becomes believable core material or stops.

### 5.3. Publication Contract

Publication is the outward claim.

Accepted core material is not automatically public material. The machine may know more than it is allowed to say.

Kontrakt lowers publication into the law that permits or denies a public claim from accepted material. Diagnostic
material remains internal unless publication allows a public diagnostic claim.

A backend may serialize or emit the claim. Emission is not publication authority.


## 6. Cross-Profile Boundaries

### 6.1. Candidate and Factual Claim

A lowered candidate must name the Fact meaning it claims to realize. The claim must be represented through Kontrakt-owned
identity, version, and core coordinates rather than through a class token, runtime object identity, or backend handle.

Naming a Fact contract is not acceptance. It only makes the candidate judgeable under one declared factual meaning.

### 6.2. Fact and Invariant

Fact and Invariant must not collapse into one role.

Fact declares the factual basis and identity under which material can stand. Invariant decides whether a particular
candidate coheres with that basis and whether accepting it would make the core lie.

A candidate becomes accepted Fact material only after Invariant succeeds. An invariant failure leaves no accepted fact
and must not be repaired by publication, diagnostics, or backend storage.

### 6.3. Accepted Material and Publication

Accepted Fact material remains internal core material until Publication permits an outward claim.

Publication may derive an external presentation from accepted material, but the presentation is not the Fact itself. Core
fields, internal identity material, backend coordinates, and diagnostic evidence remain hidden unless the Publication
contract explicitly permits a corresponding public claim.

The fact that accepted material is immutable does not authorize direct exposure.

### 6.4. Publication and Diagnostics

Diagnostic evidence may explain Fact acceptance, Invariant refusal, or Publication refusal, but evidence does not create
or override any of those judgments.

Retention decides what diagnostic material may survive. If retained diagnostic material is ever exposed, Publication must
judge that outward claim separately. Failure, evidence, retention, and publication therefore remain distinct contracts
even when one runtime path realizes them together.

### 6.5. Handoff from ADR-0048

This ADR begins only after ADR-0048 has produced a successfully lowered core-owned candidate.

It does not repeat Input refinement, Input formation, Admission, Canonicalization, or Lowering. Any defect that should have
been stopped by those contracts remains a defect in the earlier stage; Invariant is not a catch-all validator for malformed
boundary material or failed conversion.

---

## 7. Deferred Decisions

This ADR does not decide the final authoring syntax for Fact, Invariant, or Publication bodies.

It does not define the complete state and transition sets for candidate submission, acceptance, refusal, or publication.
Those movement surfaces must be designed with the complete flow, failure, diagnostic, bounds, and governance categories.

It also does not define final persistence layout, cache layout, public serialization schema, or emitter implementation.
Those are replaceable realizations behind the contract boundaries fixed here.

---

## 8. Consequences

A successfully lowered candidate is no longer confused with accepted truth.

Fact meaning is explicit and implementation-erased. Invariant has one visible acceptance authority. Accepted material is
immutable under its contract identity. Publication becomes a separate outward judgment rather than an automatic side
effect of acceptance or serialization.

Internal Fact types do not need to appear in Input or public output contracts. External users may receive publication
presentations while core factual representation remains replaceable.

The split between ADR-0048 and ADR-0049 also keeps optimization honest. Early stages reject malformed or inadmissible
material before core acceptance cost is paid. Invariant performs the remaining core-coherence judgment. Publication pays
outward transformation and emission cost only after accepted material exists and an outward claim is permitted.