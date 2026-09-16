# ADR-0072: JVM Collection Contract Preservation, Aggregate Semantics, and Deterministic Equality

## Status

Migrated

ADR-0072 was superseded as an independent Contract ADR.

Collection boundary legality and platform preservation are now owned by
ADR-0064 and ADR-0073.

Collection-specific compiler semantic mapping, aggregate-family modeling,
platform support matrices, HIR payload design, verification material,
and physical realization are migrated to the Collection/Aggregate Design.

No independent Collection Contract authority is created.

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

That boundary remains correct, but collection support now has a wider requirement.

Java and Kotlin already define collection contracts that users deliberately select when they write an interface.
A supported `List`, `Set`, `Map`, sequenced collection, sorted collection, queue, deque, array-like aggregate, or
standard
implementation can carry observable guarantees about equality, encounter order, indexing, uniqueness, association,
navigation, or other collection behavior.

Kontrakt must not erase those guarantees merely to fit one smaller internal collection model.

At the same time, implementation behavior that the declared JVM surface does not guarantee must not become Contract
meaning accidentally. Hash-bucket order, allocation order, undocumented implementation details, and current backend
layout remain outside authority unless the declared platform contract makes them observable.

The frontend therefore has two duties.

It must resolve the declared JVM collection contract completely enough to preserve every Contract-visible distinction.
It must also lower that resolved meaning into compiler-semantic material that no longer depends on reopening the Java or
Kotlin declaration during Establishment or later compilation.

ADR-0071 requires complete candidate meaning before Visible HIR. ADR-0063 then requires Establishment to judge that
resolved meaning without recovering semantic decisions from compiler representation.

Collection semantics must therefore be explicit before Input HIR and Definition Establishment can be considered
complete.

This ADR defines the shared collection data-model law used by Input and later compiler stages. It does not make
collection
a new independent Contract authority.

---

# 2. Problem

A collection carries more meaning than “several values.”

The same structural shape can expose different contracts. A `HashMap`-compatible surface and a `SequencedMap`-compatible
surface are both key/value collections, but only one of them guarantees encounter order. A sorted or navigable
collection
adds still more observable law.

If Kontrakt collapses all of those declarations into only `Sequence`, `Set`, or `Map`, it can silently remove behavior
that the user selected and the JVM library promises.

The opposite error is also dangerous.

Kontrakt must not treat every behavior of the current runtime implementation as Contract meaning. An unspecified
iteration order or incidental storage strategy is not promoted to authority merely because one JDK build happens to
expose it consistently.

A third failure appears at Contract boundaries.

Input and user realization must preserve the equality and other observable collection law of the declared platform
surface. A later 1D Contract may reject, canonicalize, lower, or replace that meaning under its own explicit authority,
but it must not pretend that the earlier platform contract always meant the later result.

A fourth failure appears in the compiler.

If collection meaning is represented only as a Java class, a Kotlin type name, a comparator object, or a backend layout,
HIR cannot state the candidate independently of the current implementation. Establishment then depends on realization
shape, and optimization cannot replace that shape safely.

The collection law must preserve the declared platform contract while still allowing Kontrakt to own deterministic
internal realization.

---

# 3. Decision Drivers

The collection model must preserve Kontrakt's determinism-first law.

The same explicit valid Contract inputs must establish the same collection meaning regardless of worker order, cache
state, allocation order, query scheduling, or physical storage.

Kontrakt must also preserve the observable contract of supported Java and Kotlin collection surfaces. Supporting a
platform collection does not authorize Kontrakt to weaken its equality, ordering, association, or other guaranteed
behavior.

The model must remain useful for ordinary JVM code. Standard collection interfaces and implementations provided by
supported Java and Kotlin versions are compatibility targets rather than exceptional adapters.

Other 1D Contracts remain sovereign over their own meaning. They may reject or explicitly transform a collection after
Input, but a new authority boundary must own that change.

The compiler must remain free to optimize. A resolved Set law must not force a tree. A Map law must not force a hash
table. A platform-visible encounter order must be preserved without requiring one permanent physical layout.

Finally, V1 must not hard-code the future V2 incremental engine. Stable semantic products and producer-owned equality
come first. Query nodes, fingerprints, physical partitions, and persistent reuse remain compiler realization.

---

# 4. Decision

## 4.1. Collection Data Model Does Not Create New Authority

This ADR does not create a new pipeline stage or a new independent Contract authority.

When a collection appears in Input, the Input Definition remains the authority-bearing Definition established under
ADR-0064 and ADR-0063.

```text
Input Definition
    ↓
owns one direct Input coordinate
    ↓
that coordinate may carry resolved collection meaning
```

A collection does not become a child Input Contract.

Its constituents do not receive Contract authority merely because they are elements, keys, or values.

The same collection data model may later be consumed by Fact, Lowering, user realization, Output, verification, or
backend realization. Those consumers do not inherit Input authority merely by using the same collection meaning.

A later 1D Contract may establish different meaning under its own law. That change must occur at that explicit authority
boundary rather than by silently reinterpreting the earlier collection.

---

## 4.2. Structural Collection Shape Does Not Replace the Platform Contract

V1 recognizes `Sequence`, `Set`, and `Map` as basic structural collection shapes.

They are not an exhaustive catalog of JVM collection contracts.

A complete resolved collection meaning also preserves every observable distinction required by the declared platform or
Kontrakt-owned collection law. Ordering, equality, uniqueness, association, navigation, and other required behavior are
not discarded merely because two types share the same basic shape.

For example, a Java `LinkedHashMap<K,V>` can refine to a Map shape with an encounter-order obligation. A `TreeSet<E>`
can
refine to a Set shape with its declared total-order behavior.

The structural shape is therefore a semantic projection, not a replacement type.

A frontend may map a supported Java or Kotlin collection surface into these shared structural distinctions only when the
full declared contract remains representable.

An unfamiliar or newer standard collection is not forced into a weaker shape by omission. Kontrakt either resolves its
complete supported contract or does not claim support for that platform surface.

---

## 4.3. Cardinality Is Preserved When It Is Contract Meaning

Every actual collection occurrence judged by Input must be finite and completely observable at that boundary.

A declared minimum, maximum, fixed extent, or other cardinality restriction is part of collection meaning when the
selected platform or Kontrakt Contract makes that restriction observable.

V1 does not require every ordinary Java or Kotlin collection declaration to invent a maximum cardinality merely to
become
representable.

A fixed-size array-like surface keeps its fixed extent when that extent belongs to the declared type or Contract.

An empty collection is a collection value when its governing law permits zero elements. It is not the same as an absent
Input coordinate.

```text
coordinate absent
    ≠
coordinate present with empty collection
```

Cardinality is not a processing budget.

Input owns any presentation extent that it explicitly declares. Budget and Capacity continue to own resource allowance
and simultaneous operating limits. A collection may satisfy its presentation law and still encounter an independently
applicable Budget or Capacity stop.

---

## 4.4. Constituent Presentation Is Closed Before HIR Visibility

A collection constituent must have one complete Input-visible presentation meaning before the containing Input
Definition Candidate can satisfy the HIR resolution invariant.

A constituent may be a supported scalar presentation, a finite alternative, another collection, or a closed aggregate
presentation.

A **closed aggregate presentation** is a finite data-only structure whose constituent presentation is completely
resolved before runtime Input judgment.

It does not create a nested Contract authority.

```text
Sequence<OrderItemPresentation>
```

may therefore be legal when `OrderItemPresentation` is already an immutable, acyclic, closed presentation law.

This does not authorize recursive discovery of arbitrary user objects.

The declared semantic presentation graph must be finite and acyclic in V1. An actual collection occurrence must also be
finite at the Input boundary.

Host object identity and shared-reference topology are not visible distinctions of the aggregate unless the resolved
platform contract or a later Contract explicitly owns such meaning.

This preserves the flat authority topology of ADR-0064 without forcing a scalar-only value topology.

```text
flat Input authority topology
    ≠
scalar-only Input value topology
```

---

## 4.5. Equality at Platform-Facing Boundaries Is Preserved

Collection processing requires an exact law for deciding when presented constituent values are equal.

When the collection is declared through a supported Java or Kotlin platform surface, Input does not invent that law.
It preserves the equality semantics required by the resolved platform contract.

The same preservation requirement applies when Kontrakt exposes a supported collection surface to user realization.
An internal optimization cannot change what equality means to user code while claiming to provide the same platform
surface.

Kontrakt may verify that an equality or ordering dependency is compatible with deterministic Contract execution. If the
required platform behavior cannot satisfy the applicable Kontrakt boundary law, the boundary is rejected or a later
explicit Lowering must target different meaning.

Platform equality is not Fact sameness. It is not Canonicalization equality. It is not HID equality.

A later 1D Contract may own a different equality relation. That later law does not retroactively rewrite the earlier
platform-facing meaning.

For a collection meaning authored directly in Kontrakt rather than inherited from a platform surface, the owning
Contract must define equality explicitly enough for deterministic judgment before the candidate becomes Visible HIR.

---

## 4.6. Collection Value Equality Follows the Resolved Collection Contract

Collection value equality is not derived from structural shape alone.

When a supported platform surface defines equality behavior, that behavior is preserved exactly. An encounter-ordered
Map may therefore expose encounter order while its platform equality still compares mappings independently of that
order.

When a Kontrakt-owned collection law defines equality directly, the owning law determines how constituent equality and
collection structure participate.

For the intrinsic structural projections used by this ADR, positional Sequence equality compares corresponding
positions, Set equality compares the represented unique elements, and Map equality compares key/value associations.
These projections do not override a stronger or different equality contract carried by the declared platform surface.

These rules describe value equality.

They do not merge Input Definition identities. Two independently identified Definitions do not become one Definition
merely because their collection values compare equal.

---

## 4.7. Hashing and Indexing Do Not Define Equality

A backend hash table, HID, fingerprint, tree index, sorted index, or another acceleration mechanism does not define
collection equality.

If a supported platform surface exposes `hashCode` behavior as part of its observable contract, Kontrakt must preserve
that observable result when it exposes the same surface. That obligation still does not make the backend indexing hash
the authority for equality.

```text
fast candidate match
    ↓
exact resolved equality law
```

is a valid realization shape.

```text
same backend hash
    ↓
therefore same Contract value
```

is not.

The exact equality-acceleration strategy belongs to the later deterministic collection realization design.

---

## 4.8. Platform Order, Deterministic Observation Order, and Storage Order Are Different

Collection order is whatever the resolved platform or Kontrakt-owned collection law guarantees.

A List-like surface may own positional order. A sequenced or sorted surface may own encounter or ordering guarantees.
An unordered surface does not acquire semantic order merely because one implementation traverses it consistently today.

The compiler may still need a deterministic order when it serializes a compiler product, emits diagnostics, produces a
stable test artifact, or constructs a backend representation.

That compiler order is realization unless the resolved Contract makes it observable.

```text
platform / Contract-visible order
    ≠
deterministic compiler observation order
    ≠
physical storage order
```

No hash-bucket order, pointer order, filesystem order, worker completion order, or undocumented implementation order may
become Contract meaning.

Conversely, when the declared platform surface guarantees encounter or sorted order, Kontrakt must not erase that law in
the name of internal determinism.

---

## 4.9. Duplicate Behavior Comes From the Resolved Collection Law

Kontrakt does not impose one universal duplicate-formation rule on every JVM collection surface.

A valid Set-like or Map-like value must satisfy the uniqueness law promised by its resolved contract. How a platform
factory, constructor, adapter, or other formation surface reacts to duplicate source material is preserved when that
behavior is part of the declared surface contract.

Input does not invent silent deduplication, first-write-wins, last-write-wins, or refusal merely because one backend
representation prefers that policy.

For a Kontrakt-owned collection law that requires uniqueness and defines duplicate presentation as invalid, Input may
refuse the malformed presentation under that explicit law.

The important rule is that duplicate behavior is resolved before authority is established. It is not recovered later
from serializer order or backend container behavior.

---

## 4.10. Platform Collection Loss Must Be Accounted Before Authority

A direct platform carrier is legal only when it preserves every distinction guaranteed by the declared collection
surface and every additional distinction required by the containing Input law.

The runtime implementation does not gain permission to weaken the declared interface contract.

A more specific declared platform type may add guarantees that the frontend must preserve. A concrete implementation
detail that is not part of the declared contract remains non-authoritative.

If pre-boundary behavior has already collapsed a distinction that the resolved collection contract requires, Kontrakt
cannot reconstruct that distinction after the fact.

When complete preservation cannot be established, the frontend cannot ratify that carrier as a lossless direct
presentation of the selected collection law.

The user may still supply the logical data through another supported platform surface or adapter that preserves the
required distinctions before the Input judgment.

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

## 4.12. Boundary Material Must Be Complete and Coherent

Input judges one complete collection presentation.

A mutable Java or Kotlin collection can be a supported boundary carrier. Its mutability does not authorize one Input
judgment to observe several different states.

The material used by that judgment must form one coherent observation under the resolved collection contract, and later
mutation of the source carrier must not retroactively change the established Input occurrence.

A lazy iterator, stream, future, supplier, callback-driven sequence, or another computation that does not itself denote
one complete collection value is not treated as an already-formed collection merely because it can eventually produce
elements.

Concurrent standard collections are supported only when the applicable boundary machinery can obtain a coherent
observation consistent with their declared contract and the Input law. Otherwise that invocation cannot establish the
required Input presentation.

This is a semantic requirement, not a mandatory copying rule.

A compiler or generated boundary may prove existing storage safe, pin a stable view, or form controlled material before
the judgment. The physical strategy remains replaceable.

One Input application must not observe a mixture of collection states that never existed as one complete presentation.

---

## 4.13. Supported JVM Collection Surface

Kontrakt treats the standard Java and Kotlin collection surfaces of each supported platform version as a compatibility
target.

Support is not limited to the historical `List`, `Set`, and `Map` names. A newer standard interface or implementation
must be audited for its observable equality, ordering, association, navigation, mutation, and other collection
guarantees
before Kontrakt claims support for the platform version that provides it.

The platform mapping is version-sensitive compiler knowledge.

A Java or Kotlin upgrade may add a new binding to existing Kontrakt semantic distinctions without changing those
distinctions. If the new platform type introduces genuinely new observable meaning, Kontrakt must represent that meaning
before claiming complete support.

Third-party or user-defined collection types may also be supported when their complete observable contract can be mapped
without heuristic omission. Unknown collection behavior is not approximated as a familiar standard type.

This platform-support mapping does not become a new Contract authority. It is the compiler knowledge required to
preserve
the Contract selected by the user.

---

# 5. Frontend and Resolved Contract HIR

## 5.1. Role-Qualified Platform Refinement

The explicit Input use selects Input authority before the frontend interprets a collection declaration.

The frontend then resolves the selected Java, Kotlin, or Kontrakt-authored collection surface into complete
compiler-semantic meaning.

```text
explicit Input role
    +
declared collection surface
        ↓
platform-aware collection resolution
        ↓
Resolved Input Definition Candidate
```

Resolution does not reduce a platform type to a familiar structural name and discard the rest of its contract.

The selected authoring route may be `.kontrakt`, a supported host declaration, or another future frontend.

Different authoring routes may converge on semantically equivalent collection meaning while retaining distinct
provenance
or platform-facing compatibility obligations.

---

## 5.2. Collection Meaning in the Input Definition Candidate

Collection presentation is not a separate 1D Definition Candidate.

It is part of the complete meaning of the containing Input Definition Candidate.

For a collection coordinate, HIR must preserve the semantic information required to interpret that coordinate exactly.
This includes its structural shape, constituent presentation, applicable cardinality law, nested closed structure, and
every equality, order, uniqueness, association, navigation, or other observable distinction required by the resolved
collection contract.

When the collection was declared through a supported JVM platform surface, HIR also preserves the exact resolved
platform
surface identity or semantic reference needed to reproduce those obligations without reopening source.

HIR does not carry an arbitrary runtime comparator, class object, or concrete container instance as a substitute for the
resolved law.

When constituent presentation refers to another resolved semantic profile or typed presentation meaning, HIR preserves
the exact current semantic reference required by that law.

HIR does not replace that reference with source text or runtime object identity.

---

## 5.3. Collection Definition Determinants

The collection part of an Input Definition is determined only by Contract-visible semantic material.

For one collection coordinate, determining meaning includes the complete resolved collection contract: structural shape,
constituent presentation, any declared cardinality law, closed aggregate structure, and every platform or Kontrakt-owned
observable distinction required to interpret the value.

A runtime collection implementation is not a determinant merely because it carries the value.

A specifically declared platform type may be a determinant when its contract adds observable meaning that a wider
surface does not guarantee.

Source location, table offset, dense handle, cache state, allocation order, hash seed, and backend layout are not
determinants.

Interaction, Policy, Governance, and Operation context do not become collection determinants merely because they select,
apply, or consume the containing Input Definition. ADR-0064 remains the owner of any broader Input Definition
determinant
law.

Equal collection payload does not merge distinct Input Definition Candidates.

---

## 5.4. HIR Information-Loss Boundary

Visible Resolved HIR must contain enough collection meaning that Definition Establishment never needs to reopen the
Java,
Kotlin, or `.kontrakt` declaration to answer a semantic question.

Establishment must not ask:

```text
Which runtime collection class happened to arrive?
Which undocumented iteration order did it expose?
Which backend container would be convenient?
```

If the declared platform or Contract collection law owns a distinction, frontend resolution preserves that distinction
before HIR visibility.

If a behavior is only incidental runtime or implementation detail, it remains outside Primary HIR meaning.

---

## 5.5. HIR Determinism

Collection HIR obeys ADR-0071 determinism-first law.

The same explicit valid collection declaration and the same exact resolution environment produce the same observable
collection HIR meaning.

Construction order does not become Contract-visible order unless the resolved platform or Contract surface actually
guarantees that order.

An unordered collection declaration therefore cannot receive a different HIR meaning because a frontend traversed
carrier members, files, or hash buckets in another order.

A sequenced or sorted declaration must retain its declared ordering obligation regardless of frontend traversal order.

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

The collection portion is complete only when its constituent meaning is exact, its declared platform or Kontrakt-owned
collection obligations are resolved, its aggregate graph is closed and acyclic, and no required distinction depends on
unresolved runtime behavior.

Definition Establishment does not discover collection semantics.

It judges the already-resolved candidate under the owning Input law.

---

## 6.2. Establishment Result

Successful Input Definition Establishment grants authority to the complete Input Definition, including its resolved
collection presentation meaning and any preserved platform-facing obligations.

```text
Resolved Input Definition Candidate
    ↓
Input-owned Definition judgment
    ↓
Established Input Definition
```

The result says what collection presentation is authoritative for that Input coordinate.

It does not establish a backend collection object.

It does not establish a hash-table layout, sorting strategy, probe sequence, JVM container instance, generated wrapper,
or storage address.

When the established meaning came from a supported platform collection surface, later compiler realization may replace
its representation but may not weaken the preserved observable contract while claiming to expose the same surface.

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

For a platform-declared collection, the judgment preserves the resolved platform equality, ordering, uniqueness,
association, and other observable value law while also applying any additional Input-owned constraints that were
established explicitly.

A successful collection judgment means that one coherent actual collection value realizes the declared Input
presentation.

It does not mean the values are admissible for the operation.

Admission remains the next authority.

---

## 7.2. Input Refusal

Input refuses an invocation when the actual collection does not realize its established presentation law.

An explicitly declared cardinality violation, invalid constituent presentation, violation of the resolved platform
collection contract, or inability to obtain one coherent observation may therefore cause Input refusal.

Input does not refuse merely because a backend-preferred collection representation would use different equality,
ordering, or storage.

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

The preserved platform collection contract does not grant Admission authority and does not encode business acceptance
rules.

---

## 8.2. Canonicalization

Input does not silently replace platform equality, reorder a sequenced collection, normalize text keys, fold case,
coerce
numbers, or otherwise change the meaning that it just established.

If declared-equivalent presentations must be reduced to one stable representative, the Canonicalization Contract owns
that new judgment.

Canonicalization may establish a different relation from the platform-facing equality preserved by Input. That later law
does not claim that the original platform surface always had the canonicalized meaning.

A compiler may impose a deterministic physical observation order for its own products. That implementation order is not
Canonicalization Contract meaning.

---

## 8.3. Lowering and Fact

A platform collection presentation is not automatically a Fact collection.

Lowering may consume established inbound presentation according to its own explicit relation and form candidate Fact
material where the Contract permits it.

If Lowering changes equality, ordering, mutability, association, or another observable collection distinction, the
target
must be a different explicit meaning. The compiler must not expose the changed value as though the original platform
contract were still being preserved.

Fact may impose its own laws, including immutability and factual sameness. A collection surface legal at Input can
therefore be rejected or transformed before it becomes Fact material.

Input does not infer core entity identity, reference authority, or factual sameness from platform collection membership.

---

## 8.4. Policy and Governance

Collection meaning does not select its own Policy World or Governance result.

The exact applicable Input use must already be determined by the Contract relations that own that selection before the
Input judgment relies on it.

A not-yet-established collection occurrence cannot be used to circularly establish the very Input use needed to
interpret that same collection application.

ADR-0063's semantic prerequisite and cycle law applies.

---

## 8.5. Later 1D Authority Is Explicit

Input and platform-facing user realization preserve the collection contract that the user selected.

Other 1D Contracts remain free to impose Kontrakt law.

They may reject a collection, establish stronger constraints, canonicalize it, lower it to another collection meaning,
or
derive Fact material when their own Contract authorizes that result.

That is not a reinterpretation of the earlier platform contract.

The authority transition is explicit:

```text
preserved platform-facing meaning
    ↓
later 1D judgment or Lowering
    ↓
new established meaning
```

No later Contract may silently rewrite prior meaning while claiming that no authority transition occurred.

---

# 9. Platform API Continuity and User Realization Boundary

Supported platform collections are not Input-only adapters.

A collection surface that Kontrakt claims to support may appear at Contract-visible JVM positions where the owning
Contract permits that surface, including generated Interaction APIs and user realization boundaries.

When Kontrakt exposes the same Java or Kotlin collection surface back to user code, the surface must satisfy the
observable platform contract that was resolved for it.

The compiler may use a completely different internal representation.

```text
supported JVM collection surface
    ↓
resolved and established collection meaning
    ↓
Kontrakt internal representation
    ↓
compatible JVM collection surface for user code
```

Internal lowering does not authorize a wrapper that changes equality, encounter order, navigation, or another guaranteed
behavior while retaining the same declared type.

A different 1D Contract may make the original surface illegal. For example, a mutable collection surface may conflict
with a Fact law. In that case Kontrakt rejects the incompatible binding or explicitly lowers to a different target
meaning. It does not silently weaken the original platform contract.

The exact generated Java or Kotlin class, wrapper, view, or bridge remains compiler design.

Platform-version support must remain maintainable as Java and Kotlin evolve. When a supported JDK or Kotlin version adds
a new standard collection surface, Kontrakt must resolve and preserve its contract before advertising complete support
for that platform version.

---

# 10. Semantic Equality, Reuse, and V2

## 10.1. Equality Owners

Several equalities must remain separate.

A supported Java or Kotlin collection surface owns the platform equality behavior that Input and user realization must
preserve when that surface is selected.

Input owns the judgment that the presented value satisfies that resolved equality law and any additional Input-owned
constraints. It does not gain authority to replace the platform law.

A later 1D Contract may own a different equality relation for its own established meaning.

ADR-0071 owns HIR semantic equality and projection equality at their corresponding producer boundaries.

A compiler cache, serialized artifact, HID, fingerprint, or backend container does not define a weaker substitute.

```text
platform-facing equality
    ≠
later 1D equality
    ≠
HIR / product equality
    ≠
backend match
```

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

After Establishment, the compiler knows the complete resolved collection obligations needed by downstream work. Those
obligations can include structural shape, constituent meaning, cardinality, equality, encounter or sorted order,
uniqueness, association, navigation, and platform-facing compatibility.

That information is sufficient to support later specialization without requiring this ADR to choose the optimizer or
backend shape.

```text
Established collection meaning
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
algorithm, collision strategy, deterministic traversal encoding, zero-copy rule, platform-bridge implementation, or
cache structure.

Those decisions belong to the separate implementation Design document after the relevant middle-end and backend
boundaries are mature enough to justify them.

The downstream design must still preserve four obligations from this ADR:

1. established collection meaning is not reinterpreted by the backend;
2. observable results remain deterministic;
3. a supported platform surface keeps every guarantee that Kontrakt claims to preserve when it is exposed to user code;
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

## 12.2. Treating the JVM Collection Contract as Mere Carrier Detail

Kontrakt does not reduce a supported platform collection to a weaker generic shape and discard guarantees that the
declared Java or Kotlin surface makes observable.

That would make Kontrakt override a contract that the user deliberately selected.

The opposite rule also holds.

Undocumented behavior of one runtime implementation does not become Contract meaning merely because the implementation
currently exhibits it.

Kontrakt preserves the declared platform contract, not arbitrary implementation accident.

---

## 12.3. Invented Duplicate or Arbitration Semantics

Input does not invent silent deduplication, first-write-wins, last-write-wins, or refusal when the resolved platform or
Kontrakt-owned collection law specifies different behavior.

Those choices can discard information or introduce an ordering dependency that the selected surface never promised.

Duplicate and arbitration behavior must come from the resolved collection law.

---

## 12.4. Mandatory Sorted Representation

Determinism does not require a semantic Set or Map to be stored in sorted form.

Sorting may be useful for a compiler product, a serializer, or a later canonical representation. It is not the
collection
Contract itself.

Freezing sorted storage here would unnecessarily constrain future performance work.

---

## 12.5. Arbitrary Equality or Comparator as New Kontrakt Authority

Kontrakt does not accept an arbitrary executable equality or comparator callback as a shortcut for defining new Contract
meaning.

A supported platform surface may itself depend on equality or ordering behavior defined by the Java or Kotlin contract.
When Kontrakt claims to support that surface, it preserves the required behavior and verifies that the boundary can
satisfy Kontrakt's determinism and safety laws.

That platform obligation is different from allowing an untracked callback to create a new Kontrakt equality law.

A new Kontrakt-owned equality or ordering relation requires an explicit declarative Contract decision.

---

## 12.6. Recursive Collection Authority Graph

Nested collection values do not create nested Input Contracts or runtime authority recursion.

V1 permits finite declared nesting of presentation values, not recursive semantic discovery of object graphs.

This keeps Definition formation and Input judgment finite and inspectable.

---

## 12.7. Input-Only Standard Collection Support

Kontrakt does not claim support for a standard collection type only at the external Input edge and then require user
code
to abandon that platform surface everywhere else.

Where the owning Contract permits the collection surface, user realization and other Contract-visible JVM boundaries may
use it without losing its declared platform contract.

Internal representation remains free to differ.

---

# 13. Consequences

Kontrakt can accept ordinary Java and Kotlin collection declarations without forcing users to replace them with
Kontrakt-specific collection APIs.

The frontend must do more work than a host-type check. It must resolve the complete platform collection contract,
preserve its observable distinctions in HIR, and reject or redirect a carrier that cannot satisfy those distinctions at
the applicable boundary.

Definition Establishment becomes stronger because the collection law is already explicit before authority is granted.

Input and user realization preserve the equality and other platform-facing behavior that Kontrakt claims to support.
Other 1D Contracts may still impose Kontrakt law at their own explicit boundary.

Later compiler work gains useful static information without receiving a frozen physical design. Structural shape,
ordering guarantees, uniqueness, constituent presentation, and platform compatibility can support specialization after
the optimizer architecture is ready.

Some mutable, concurrent, lazy, or specialized platform surfaces may require generated formation or bridging rather than
zero-adapter reuse. That is acceptable when the bridge preserves the declared contract.

Java and Kotlin version upgrades now require a collection-compatibility audit. Supporting a platform version includes
supporting the standard collection contracts that Kontrakt advertises for that version, not merely recognizing their
class names.

---

# 14. Intentionally Open

This ADR closes collection meaning through platform-aware frontend resolution, Resolved Contract HIR, Definition
Establishment, the Input judgment boundary, and the semantic obligation at user realization boundaries.

The physical realization remains open.

The next Design document should investigate standard-library type recognition, deterministic collection formation,
internal collection material, equality execution, platform-compatible views, duplicate handling, collision resistance,
bounded work, data-oriented layout, specialization, JVM-oriented lowering, and V2 reuse.

That Design must use this ADR as semantic input. It must not feed a preferred container implementation back into the
Contract meaning.

No choice of MIR, LIR, optimizer subsystem, hash table, tree, sort order, vector layout, primitive slab, generated loop,
platform-profile storage, or adapter implementation is made by this ADR.

The exact compiler mechanism for maintaining Java and Kotlin version-specific collection support also remains open. The
semantic requirement to preserve supported platform contracts does not.

---

# 15. Non-Normative Research Notes

The decision follows project law first. External systems are evidence for failure modes and engineering quality rather
than Contract authority.

The Java Collections Framework is direct evidence for platform-contract preservation. Since JDK 21, sequenced collection
interfaces make encounter order explicit across lists, deques, sequenced sets, and sequenced maps. Sorted and navigable
interfaces add further observable obligations. A compiler that collapses those surfaces into only unordered Set or Map
meaning would lose library contract.

Kotlin provides read-only and mutable collection interfaces around List, Set, and Map, and its standard implementations
can add order guarantees that the base interfaces do not require. This reinforces the same rule: Kontrakt must preserve
the contract of the selected surface while keeping incidental implementation behavior separate.

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

A JVM collection entering Kontrakt keeps the contract that the supported Java or Kotlin surface actually guarantees.

Kontrakt resolves that contract into explicit compiler-semantic material. It does not weaken the platform contract, and
it does not promote undocumented implementation behavior into authority.

```text
declared JVM collection surface
    ↓
platform-aware deterministic resolution
    ↓
Resolved Input Definition Candidate
    ↓
Input-owned Definition Establishment
    ↓
established collection meaning
    ↓
replaceable deterministic internal realization
    ↓
platform-compatible user surface when required
```

Input and user realization preserve the applicable platform-facing equality and observable collection law.

A later 1D Contract may reject or explicitly establish different meaning under its own authority.

The Contract decides when meaning changes.

The compiler decides how to realize each established meaning efficiently.