# ADR-0072: Input Collection Presentation, Aggregate Semantics, and Deterministic Equality

## Status

Proposed

## Date

2026-09-14

## Extends

- ADR-0064: Input Contract, Explicit Boundary Presentation, and External-Authority Boundary

## Related

- *What Contract Is*
- ADR-0046: IDL-First Interface Contract Frontend and 1D Contract Catalog
- ADR-0047: One-Dimensional Contract Presentations and Pipeline-Slot Selection
- ADR-0053: Version Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0065: Admission Contract
- ADR-0066: Canonicalization Contract
- ADR-0067: Lowering Contract
- ADR-0068: Fact Contract
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- Kontrakt Compiler Total Architecture Map
- Kontrakt V2 Incremental Architecture Research TODO
- *Modern Compiler Architecture 01–15*

---

# 1. Context

ADR-0064 defines Input as the external boundary presentation Contract.

It already permits a collection to remain one direct Input coordinate when its complete presentation law is explicit,
closed, immutable, and deterministic.

It also rejects a different model.

A host `List`, `Set`, `Map`, array, iterator, comparator, `equals`, `hashCode`, or collection implementation does not
create Input meaning merely because the selected carrier exposes that behavior.

That boundary is correct, but it is incomplete.

Ordinary software interfaces need collections. A Contract system that supports only scalar boundary values forces users
to flatten ordinary request structures outside the Contract or to bypass the Contract for common data. That weakens the
reason to use Kontrakt.

The missing law is therefore not whether collections are allowed.

The missing law is:

> **What exact Input meaning does a collection have before any backend data structure, host equality rule, iteration
> order, or optimization strategy is allowed to realize it?**

This question reaches the Contract frontend and Resolved Contract HIR directly.

ADR-0071 requires every 1D Contract to define the complete candidate meaning that HIR must preserve. It also requires
the
owning 1D law to define the determinants of that candidate meaning.

ADR-0063 then requires Establishment to judge that resolved meaning without reopening authored source or recovering
semantic decisions from compiler representation.

Collection semantics must therefore be closed before Input HIR and Definition Establishment can be considered complete.

---

# 2. Problem

A collection carries more meaning than “several values.”

For the same constituent values, different collection laws can disagree about whether order matters, whether repeated
values remain visible, whether one key may appear more than once, and what makes two presented keys the same.

If Kontrakt leaves those decisions to the host collection, external implementation becomes Contract authority.

For example, a host map may already have merged two keys before the Interaction reaches Kontrakt. If the host considered
the keys equal but the declared Input presentation would distinguish them, the lost distinction cannot be reconstructed
at the Input boundary.

The opposite error is also dangerous. Kontrakt could silently sort, deduplicate, or apply a last-write-wins rule at the
Input boundary. That would make Input perform normalization or arbitration that belongs to no declared Input law.

A third failure appears in the compiler.

If collection meaning is represented only as `java.util.Map`, a Kotlin collection type, a comparator callback, or a
backend layout, HIR cannot state the candidate independently of the current implementation. Establishment then depends
on realization shape, and later optimization cannot replace that shape without risking semantic drift.

The collection law must be explicit before those implementation choices begin.

---

# 3. Decision Drivers

The collection model must preserve Kontrakt's determinism-first law.

The same explicit valid Contract inputs must establish the same Input collection meaning regardless of worker order,
cache state, allocation order, host hash-table iteration, query scheduling, or physical storage.

The model must remain useful for ordinary interfaces. Sequence, set-like, and key/value presentations are normal
boundary
data rather than exceptional escape hatches.

The model must also leave the compiler free to optimize. A semantic Set must not force a tree. A semantic Map must not
force a hash table. A deterministic Contract must not require one permanent physical ordering.

Finally, V1 must not hard-code the future V2 incremental engine. Stable semantic products and producer-owned equality
come first. Query nodes, fingerprints, physical partitions, and persistent reuse remain compiler realization.

---

# 4. Decision

## 4.1. Collection Presentation Remains Input Authority

This ADR does not create a new pipeline stage or a new independent Contract authority.

Collection presentation is a part of the Input Contract.

```text
Input Definition
    ↓
owns one direct Input coordinate
    ↓
that coordinate may own a Collection Presentation Meaning
```

A collection does not become a child Input Contract.

Its constituent values do not receive authority merely because they are elements, keys, or values of the collection.

The Input Definition remains the authority-bearing Definition established under ADR-0064 and ADR-0063.

This ADR defines the additional Input-owned meaning required when one of its coordinates is collection-shaped.

---

## 4.2. Collection Presentation Families

V1 defines three closed collection presentation families.

| Family        | Semantic order                | Repetition meaning                         | Association law                                 |
|---------------|-------------------------------|--------------------------------------------|-------------------------------------------------|
| `Sequence<E>` | Position is semantic.         | Repeated equal elements are allowed.       | One ordered element sequence.                   |
| `Set<E>`      | No element order is semantic. | A semantic element may occur at most once. | One unordered unique-element collection.        |
| `Map<K,V>`    | No entry order is semantic.   | A semantic key may occur at most once.     | One value is associated with each semantic key. |

These are Contract meanings, not host collection categories.

A Java `List` does not establish `Sequence`. A Kotlin `Set` does not establish `Set`. A `HashMap` does not establish
`Map`.

A frontend may use those host forms as evidence only when it can refine them completely into the selected collection
presentation law.

Other collection families require an explicit later Contract decision. They are not inferred from unfamiliar host
collection behavior.

---

## 4.3. Cardinality Is Presentation Meaning

Every Input collection has a finite declared cardinality law.

The law states the collection extent that belongs to the declared presentation. V1 does not admit an unbounded
collection presentation whose maximum extent is absent from the Contract meaning.

A fixed-size collection is the case where the minimum and maximum cardinality are equal.

An empty collection is a collection value when the cardinality law permits zero elements. It is not the same as an
absent
Input coordinate.

```text
coordinate absent
    ≠
coordinate present with empty collection
```

Cardinality is not a processing budget.

Input owns the allowed presentation extent. Budget and Capacity continue to own resource allowance and simultaneous
operating limits. A collection can satisfy its Input cardinality law and still encounter an independently applicable
Budget or Capacity stop.

---

## 4.4. Constituent Presentation Is Closed Before HIR Visibility

A collection constituent must have one complete Input-visible presentation meaning before the collection Definition
Candidate can satisfy the HIR resolution invariant.

A constituent may be a supported scalar presentation, a finite alternative, another bounded collection, or a closed
aggregate presentation.

A **closed aggregate presentation** is a finite data-only structure whose constituent presentation is completely
declared
before runtime Input judgment.

It does not create a nested Contract authority.

```text
Sequence<OrderItemPresentation>
```

may therefore be a legal collection presentation when `OrderItemPresentation` is already a finite, immutable, acyclic,
closed presentation law.

This does not authorize recursive discovery of arbitrary user objects.

The declared presentation graph must be finite and acyclic in V1. Every nested collection in that graph must also have a
finite cardinality law.

Host object identity and shared-reference topology are not visible distinctions of the aggregate unless a later Contract
explicitly owns such meaning. V1 Input does not add that authority.

This preserves the flat authority topology of ADR-0064 without forcing a scalar-only value topology.

```text
flat Input authority topology
    ≠
scalar-only Input value topology
```

---

## 4.5. Input Presentation Value Equality

Collection processing requires an exact law for deciding when two presented constituent values are the same for Input
purposes.

This ADR names that relation **Input Presentation Value Equality**.

The relation belongs to the Input presentation law.

It is not host object equality. It is not Fact sameness. It is not Canonicalization equality. It is not a hash match.

Input Presentation Value Equality compares only the distinctions that the applicable Input presentation meaning exposes.

If two values differ in an Input-visible distinction, Input equality keeps them different.

If another Contract later declares that those two presentations have the same canonical meaning, that later law does not
retroactively change Input equality.

A user callback, comparator object, locale-dependent rule, runtime service, or host `equals` implementation cannot
define
this equality.

The frontend must resolve the constituent presentation far enough that equality is deterministic before the collection
candidate becomes Visible HIR.

---

## 4.6. Collection Value Equality

Collection value equality follows from the collection family and constituent Input Presentation Value Equality.

A Sequence is equal to another Sequence only when both have the same cardinality and corresponding positions carry equal
presented values.

A Set is equal to another Set when they contain the same semantic element classes. Physical iteration order does not
participate.

A Map is equal to another Map when they contain the same semantic key classes and each equal key is associated with an
equal presented value.

These rules describe presented value equality.

They do not merge Input Definition identities. Two independently identified Input Definitions do not become one
Definition merely because their collection payloads are semantically equal.

---

## 4.7. Hashing Does Not Define Equality

Hashing is not part of collection Contract meaning.

A backend may use a hash, HID, fingerprint, sorted index, tree, linear scan, or another data structure to accelerate a
collection operation.

A match produced by that mechanism does not replace Input Presentation Value Equality.

```text
fast candidate match
    ↓
exact Input presentation equality
```

is a valid realization shape.

```text
same hash
    ↓
therefore same Input value
```

is not.

The exact equality-acceleration strategy belongs to the later deterministic collection realization design.

---

## 4.8. Semantic Order and Deterministic Observation Order Are Different

A Sequence owns semantic element order.

Set and Map do not.

The compiler may still need a deterministic order when it serializes a compiler product, emits diagnostics, produces a
stable test artifact, or constructs a backend representation.

That order is compiler realization unless a Contract explicitly makes the order observable.

```text
semantic collection order
    ≠
deterministic compiler observation order
    ≠
physical storage order
```

No host insertion order, hash-bucket order, pointer order, filesystem order, or worker completion order may become Set
or Map meaning.

If an Interaction requires key/value pairs whose order is itself contract-visible, V1 may express that meaning as a
Sequence of entry presentations. This ADR does not silently convert an unordered Map into an ordered one.

A dedicated ordered associative family may be introduced later without changing the meaning of V1 `Map`.

---

## 4.9. Duplicate Law

A Sequence preserves duplicate elements because position is part of its meaning.

Set and Map are different.

If actual Input material presents two Set elements that are equal under Input Presentation Value Equality, the Set
presentation is malformed and Input refuses it.

If actual Input material presents two Map keys that are equal under the key presentation equality, the Map presentation
is malformed and Input refuses it.

Input does not silently remove the duplicate.

It also does not use first-write-wins or last-write-wins.

Those rules would either discard an externally visible distinction or make an otherwise unordered collection depend on
an undeclared order.

A later Contract may define another collection family, including unordered multiplicity, or another associative
presentation with a different duplicate law. That law must be explicit. It is not inferred from a serializer or host
collection implementation.

---

## 4.10. Host Collection Loss Must Be Accounted Before Authority

A direct host carrier is legal only when pre-boundary host behavior has not already destroyed a distinction that the
Input collection law requires.

This requirement is especially important for Set and Map carriers.

A host equality relation may be finer than the declared Input equality. In that case the host preserves at least the
distinctions that Input needs, and Kontrakt may still detect that several host-distinct entries become semantic
duplicates under Input equality.

A host equality relation must not be coarser than the declared Input equality in a way that merges two Input-distinct
values before Kontrakt observes them.

The required preservation condition is therefore:

```text
host treats A and B as equal
    ↓
Input must not require A and B to remain distinct
```

When that implication cannot be proven for a direct Set or Map carrier, the frontend cannot ratify that carrier as a
lossless direct presentation of the selected collection law.

The user may still supply the same logical data through another supported presentation or adapter that preserves all
Input-visible distinctions before the Input boundary.

This is a semantic preservation requirement.

The proof mechanism, generated bridge, copy strategy, or specialized carrier belongs to compiler design.

---

## 4.11. Presence, Null, and Empty Collection

Collection presence follows ADR-0064.

The collection law does not reinterpret host `null`, missing properties, default values, or absent serializer fields.

An optional collection coordinate has an explicit presence law.

If constituent absence or null-like alternatives are allowed, that distinction must belong to the constituent
presentation itself.

An empty present collection remains distinct from an absent coordinate.

No host-language default may collapse those states.

---

## 4.12. Boundary Material Must Be Complete and Immutable

Input judges one complete collection presentation.

A live view, lazy iterator, stream, future, supplier, callback-driven sequence, concurrently mutable collection, or
resource-dependent traversal is not collection Input material.

Kontrakt Input does not repair such material by inventing a snapshot after the boundary.

The material reaching the authoritative Input judgment must already support one coherent immutable observation under the
selected presentation law.

This is a semantic requirement, not a mandatory copying rule.

A compiler or generated boundary may prove that existing storage is already safe, or it may form controlled immutable
material before the judgment. The physical strategy remains replaceable.

One Input application must not observe a mixture of collection states that never existed as one complete presentation.

---

# 5. Frontend and Resolved Contract HIR

## 5.1. Role-Qualified Refinement

The explicit Input use selects Input authority before the frontend interprets a collection carrier.

The frontend does not discover a `Map` and then infer an Input collection Contract from its host shape.

```text
explicit Input role
    +
selected authored material
        ↓
Input-owned collection refinement
        ↓
Resolved Input Definition Candidate
```

The selected authoring route may be `.kontrakt`, a supported host declaration, or another future frontend.

Different authoring routes may converge on the same resolved collection candidate meaning. Their provenance may remain
different.

---

## 5.2. Collection Meaning in the Input Definition Candidate

Collection presentation is not a separate 1D Definition Candidate.

It is part of the complete meaning of the containing Input Definition Candidate.

For a collection coordinate, HIR must preserve the semantic information required to interpret that coordinate exactly.
That includes the collection family, its constituent presentation meaning, cardinality law, nested closed structure, and
the family-owned order or multiplicity distinctions.

The equality and duplicate results follow from those established Input laws. HIR does not carry an arbitrary executable
comparator as a substitute for the law.

When constituent presentation refers to another resolved semantic profile or typed presentation meaning, HIR preserves
the exact current semantic reference required by that law.

HIR does not replace that reference with source text or a host class.

---

## 5.3. Collection Definition Determinants

The collection part of an Input Definition is determined only by Input-owned semantic material.

For one collection coordinate, the determining meaning is the selected family together with its exact constituent
presentation, finite cardinality law, closed aggregate structure where present, and the family semantics defined by this
ADR.

A host collection implementation is not a determinant.

Neither are runtime iteration order, object identity, source location, table offset, dense handle, hash seed, cache
state, or backend layout.

Interaction, Policy, Governance, and Operation context do not become collection determinants merely because they select,
apply, or consume the containing Input Definition. ADR-0064 remains the owner of any broader Input Definition
determinant
law.

Equal collection payload does not merge distinct Input Definition Candidates.

---

## 5.4. HIR Information-Loss Boundary

Visible Resolved HIR must contain enough collection meaning that Definition Establishment never needs to reopen the host
collection declaration to answer a semantic question.

Establishment must not ask:

```text
Which Java collection was this?
What comparator object did it use?
What did its iterator return first?
What did host equals/hashCode decide?
```

If the owning collection law needs a distinction, frontend refinement resolves and preserves that distinction before HIR
visibility.

If the distinction is only source or implementation detail, it remains outside Primary HIR meaning.

---

## 5.5. HIR Determinism

Collection HIR obeys ADR-0071 determinism-first law.

The same explicit valid collection declaration and the same exact resolution environment produce the same observable
collection HIR meaning.

Construction order does not become semantic order.

A Set or Map declaration therefore cannot receive a different HIR meaning because a frontend traversed carrier members,
files, or hash buckets in another order.

An invalid or unresolved collection candidate does not enter Visible HIR as a fabricated placeholder.

---

## 5.6. Definition Candidate and IDL Use Candidate Remain Separate

The same Input Definition Candidate may be selected by more than one exact IDL use when ADR-0064 says that the differing
use context does not change Input Definition meaning.

Collection payload is stored in the Definition meaning.

The exact Interaction-side use remains an IDL Use Candidate under ADR-0071.

The use relation does not copy the collection Definition meaning and does not make Interaction identity part of
collection
identity for convenience.

This separation remains important when Policy or Governance changes which Input Definition is selected for an
Interaction. Such a use change does not retroactively redefine an otherwise unchanged collection presentation law.

---

# 6. Definition Establishment

## 6.1. Establishment Input

Collection Definition Establishment consumes the Resolved Input Definition Candidate produced under this ADR and
ADR-0064.

The collection portion is complete only when its family and constituent meanings are exact, its finite cardinality law
is
known, its declared aggregate graph is closed and acyclic, and no required Input distinction depends on unresolved host
behavior.

Definition Establishment does not discover collection semantics.

It judges the already-resolved candidate under the owning Input law.

---

## 6.2. Establishment Result

Successful Input Definition Establishment grants authority to the complete Input Definition, including its collection
presentation meaning.

```text
Resolved Input Definition Candidate
    ↓
Input-owned Definition judgment
    ↓
Established Input Definition
```

The result says what collection presentation is authoritative for that Input coordinate.

It does not establish a backend collection object.

It does not establish canonical bytes, hash layout, sorting strategy, probe sequence, JVM collection class, generated
wrapper, or storage address.

The Canonical Contract World may expose the established collection meaning through the Input Definition's exact semantic
surface. Its physical representation remains compiler realization.

---

## 6.3. No Independent Collection Authority

This ADR does not create `Established Collection Definition` as a second authority alongside the Input Definition.

A physical compiler implementation may intern, split, or share collection-law payloads when profitable. That sharing
does not merge Input Definitions or create a new Contract authority.

Likewise, this ADR does not create an independent Collection Occurrence.

When ADR-0064 gives one Input application established occurrence meaning, the exact collection presentation belongs to
that containing Input occurrence or judgment result. Collection membership alone does not create another occurrence
identity.

---

# 7. Invocation-Time Input Judgment

## 7.1. Collection Judgment

At the Input boundary, actual collection material is judged under the already-established Input Definition.

The judgment verifies the declared presentation rather than reconstructing the Definition.

For a collection coordinate, it must preserve the selected family semantics, respect the finite cardinality law, judge
constituent presentations, and enforce the duplicate law where the family requires uniqueness.

A successful collection judgment means only that the actual collection material realizes the declared Input
presentation.

It does not mean the values are admissible for the operation.

Admission remains the next authority.

---

## 7.2. Input Refusal

Input refuses an invocation when the actual collection does not realize its established presentation law.

A cardinality outside the declared presentation, an invalid constituent presentation, a semantic duplicate in Set or
Map, or collection material that cannot provide one coherent immutable observation may therefore cause Input refusal.

That refusal is an Input-owned unsuccessful judgment.

It does not become Admission rejection, Budget exhaustion, Capacity refusal, or backend failure merely because the same
physical processing encountered those systems.

Failure may later consume the exact Input-owned unsuccessful meaning under the Failure Contract. Diagnostic systems may
explain it from authoritative material and provenance. Neither system re-runs collection semantics to invent another
answer.

---

# 8. Relationship to Later Contracts

## 8.1. Admission

Successful collection Input remains boundary presentation material.

Admission may judge whether the correctly presented values may continue.

Input collection equality does not grant Admission authority and does not encode business acceptance rules.

---

## 8.2. Canonicalization

Input does not sort an unordered collection into semantic order, normalize text keys, fold case, coerce numbers, or
remove duplicate values in order to make later work easier.

If declared-equivalent presentations must be reduced to one stable representative, the Canonicalization Contract owns
that meaning.

A compiler may impose a deterministic physical observation order for its own products. That implementation order is not
Canonicalization Contract meaning.

---

## 8.3. Lowering and Fact

A collection presentation is not automatically a Fact collection.

Lowering may consume established inbound presentation according to its own explicit relation and form candidate Fact
material where the Contract permits it.

Input does not infer core entity identity, reference authority, or factual sameness from Set or Map membership.

---

## 8.4. Policy and Governance

Collection meaning does not select its own Policy World or Governance result.

The exact applicable Input use must already be determined by the Contract relations that own that selection before the
Input judgment relies on it.

A not-yet-established collection occurrence cannot be used to circularly establish the very Input use needed to
interpret that same collection application.

ADR-0063's semantic prerequisite and cycle law applies.

---

# 9. Generated Interaction API Boundary

Generated host APIs are downstream products of established Contract meaning.

A generated Interaction surface may expose a host collection type when that type can preserve the declared collection
presentation law.

The generated type does not become the semantic source.

If a familiar host Set or Map would lose an Input-visible distinction before Kontrakt can judge it, the generated API or
adapter must use another loss-preserving surface rather than pretending that the host type is equivalent.

The exact Java or Kotlin signature remains compiler design.

Changing that signature is permitted when the new generated surface preserves the same established Input meaning and the
applicable compatibility law permits the change.

---

# 10. Semantic Equality, Reuse, and V2

## 10.1. Equality Owners

Several equalities must remain separate.

The Input Contract owns presentation value equality.

The Input Definition law owns equality of complete resolved Input Definition meaning.

ADR-0071 owns HIR semantic equality and projection equality at their corresponding producer boundaries.

A compiler cache, serialized artifact, HID, fingerprint, or backend container does not define a weaker substitute.

---

## 10.2. Reuse Does Not Create Meaning

V2 may reuse collection-related HIR products, established Definition projections, generated APIs, or backend artifacts
when their exact validity inputs remain compatible.

A source move may change provenance without changing collection semantic meaning.

A physical collection layout may change without changing the Input Definition.

A changed collection law may invalidate only consumers that actually depend on the changed semantic projection.

The exact projection granularity remains open until measured compiler behavior justifies it.

This ADR does not turn each collection element or field into an incremental query node.

---

## 10.3. Clean Deterministic Computation Remains the Reference

A reused or incrementally repaired collection product must be semantically equivalent to clean deterministic
computation over the same explicit inputs.

Cache presence cannot make an invalid collection valid.

Repair order cannot change collection meaning.

Removing all reuse may reduce performance. It must not change the established Contract result.

---

# 11. Realization and Optimization Boundary

The semantic law is intentionally more complete than the realization design.

After Establishment, the compiler knows the collection family, constituent presentation meaning, finite extent law,
semantic order law, and exact equality obligations. That information is sufficient to support later specialization
without requiring this ADR to choose the optimizer or backend shape.

```text
Established Input Collection Meaning
    ↓
open realization / analysis / optimization design
    ↓
JVM-target product
```

A later deterministic collection realization design may choose different strategies for small and large collections,
primitive and aggregate constituents, read-mostly and lookup-heavy uses, or cold and hot execution paths.

It may use linear storage, sorted storage, flat hashing, dense indexes, primitive slabs, structure-of-arrays layouts,
vectorized comparison, precomputed signatures, or other techniques when legal and profitable.

None of those techniques is fixed here.

Likewise, this ADR does not freeze a MIR family, LIR operation set, SSA form, collection kernel API, pass order, hash
algorithm, collision strategy, deterministic traversal encoding, zero-copy rule, or cache structure.

Those decisions belong to the separate implementation Design document after the relevant middle-end and backend
boundaries are mature enough to justify them.

The downstream design must still preserve four obligations from this ADR:

1. established collection meaning is not reinterpreted by the backend;
2. observable results remain deterministic;
3. host collection behavior cannot regain semantic authority;
4. optimization legality is decided before profitability.

The specific physical implementation remains open.

---

# 12. Rejected Directions

## 12.1. Scalar-Only V1 Input

Rejecting ordinary collections would simplify the first backend, but it would push normal interface structure outside
the
Contract boundary.

That cost is too high.

Collection presentation is part of V1 Input rather than a later convenience feature.

---

## 12.2. Host Collection Semantics as Input Law

Kontrakt does not define Set or Map meaning by delegating to `equals`, `hashCode`, comparator callbacks, insertion
order,
or implementation-specific iteration.

That would move authority into external code and would make the same Contract depend on the current host library.

---

## 12.3. Silent Deduplication or Last-Write-Wins

Input does not normalize Set duplicates away and does not resolve Map duplicate keys by arrival order.

Those choices hide information loss and can make an unordered collection depend on accidental physical order.

Malformed unique collections are refused instead.

---

## 12.4. Mandatory Sorted Representation

Determinism does not require a semantic Set or Map to be stored in sorted form.

Sorting may be useful for a compiler product, a serializer, or a later canonical representation. It is not the
collection
Contract itself.

Freezing sorted storage here would unnecessarily constrain future performance work.

---

## 12.5. Arbitrary User Equality or Comparator Callback

V1 does not accept executable user equality or comparison callbacks as collection meaning.

Such callbacks can observe hidden mutable state, environment, locale, time, external services, or behavior not tracked
by
the Contract.

A richer equality vocabulary requires a later explicit declarative Contract law rather than a callback escape hatch.

---

## 12.6. Recursive Collection Authority Graph

Nested collection values do not create nested Input Contracts or runtime authority recursion.

V1 permits finite declared nesting of presentation values, not recursive semantic discovery of object graphs.

This keeps Definition formation and Input judgment finite and inspectable.

---

# 13. Consequences

Kontrakt can represent ordinary collection-shaped requests without giving Java or Kotlin collection implementations
semantic authority.

The frontend must do more work than a simple host-type check. It must resolve collection family and constituent meaning,
preserve the exact presentation distinctions in HIR, and reject carriers whose hidden behavior would lose those
distinctions.

Definition Establishment becomes stronger because the complete collection law is already available before authority is
granted.

Later compiler work gains useful static information without receiving a frozen physical design. Cardinality, semantic
order, uniqueness, and constituent presentation are established knowledge that can support specialization after the
optimizer architecture is ready.

Some familiar host collection surfaces will not qualify for zero-adapter use. This is intentional when the host has
already applied incompatible equality, hidden mutation, lazy traversal, or another information-losing convention.

The user is not required to abandon collection-shaped input. Kontrakt or an adapter must instead expose a
loss-preserving
presentation compatible with the declared law.

---

# 14. Intentionally Open

This ADR closes the Contract meaning through frontend resolution, Resolved Contract HIR, Definition Establishment, and
the Input judgment boundary.

The physical realization after that point remains open.

The next Design document should investigate deterministic collection formation, internal collection material, equality
execution, duplicate detection, collision resistance, bounded work, data-oriented layout, specialization, JVM-oriented
lowering, and V2 reuse.

That Design must use this ADR as semantic input. It must not feed a preferred container implementation back into the
Contract meaning.

No choice of MIR, LIR, optimizer subsystem, hash table, tree, sort order, vector layout, primitive slab, or generated
loop
is made by this ADR.

---

# 15. Non-Normative Research Notes

The decision follows project law first. External systems are evidence for failure modes and engineering quality rather
than Contract authority.

WebAssembly Component Model / WIT is useful because it separates abstract interface value types from their Canonical ABI
realization. Its current gated `map` design also demonstrates that a type named “map” does not make uniqueness and order
self-evident; those semantics must be stated by the type system or surrounding protocol.

Protocol Buffers similarly separates repeated entry encoding from map language bindings and does not guarantee map
serialization order. This supports keeping semantic order separate from host or wire iteration order.

CBOR's deterministic encoding requirements give an especially strong precedent for the same distinction. CBOR maps do
not gain semantic ordering merely because a deterministic encoding sorts their keys. Duplicate-key validity is treated
separately from deterministic representation.

LLVM documents nondeterministic compiler output caused by unordered iteration and recommends deterministic observation
when output order matters. LLVM's `SetVector` also shows the cost of obtaining deterministic iteration by maintaining
more than one physical structure. Determinism therefore does not justify freezing one expensive container shape into the
Contract.

Abseil Swiss Tables and current Rust `HashMap` show the performance value of flat open-addressing, compact metadata, and
SIMD-oriented lookup. Recent 2025 open-addressing research continues to improve theoretical probe bounds. These systems
are reasons to leave backend representation open rather than to make a tree or one hash scheme part of the ADR.

RocksDB treats comparator semantics as persistent compatibility-sensitive behavior. A comparator change can invalidate
the interpretation of an existing key space. The lesson for Kontrakt is not to expose a comparator callback, but to keep
key equality and ordering semantics explicit, stable, and owned by the Contract rather than by an interchangeable data
structure.

Buck2 DICE reports that incorrect key or value equality and untracked mutable data are major incremental-computation
failure modes. That experience supports producer-owned semantic equality and the prohibition against hidden mutable
state in reusable collection material.

PostgreSQL MVCC and Linux RCU provide a separate systems lesson. Readers should observe one coherent state rather than
a mixture of partially changed states, while publication, pinning, copying, and reclamation remain implementation
mechanisms. That supports the Input requirement for one coherent immutable collection observation without making any
particular snapshot or memory-management strategy Contract law.

Recent reproducible-build research on Java reports ordering and environment-dependent artifact differences as recurring
causes of unreproducibility. Recent work on canonicalization failures likewise shows that checking one representation
and
later interpreting another can create security failures. Both results reinforce Kontrakt's separation between semantic
meaning, deterministic representation, and later canonicalization.

Recent verified CBOR/CDDL work also demonstrates that deterministic map representation can enable faster validated
lookup. That is useful optimization evidence for the future Design document, but it does not justify moving
deterministic
encoding into Input Contract meaning.

---

# 16. Final Rule

A collection entering Kontrakt is not a Java or Kotlin collection with Contract annotations attached to it.

It is an Input presentation whose collection family, constituent meaning, finite extent, equality, ordering, and
duplicate semantics are already explicit before authority is established.

```text
Host collection evidence
    ↓
loss-accounted deterministic frontend refinement
    ↓
Resolved Input Definition Candidate
    ↓
Input-owned Definition Establishment
    ↓
Established Input collection meaning
    ↓
replaceable deterministic realization
```

The Contract decides what the collection means.

The compiler decides how to realize that meaning efficiently.