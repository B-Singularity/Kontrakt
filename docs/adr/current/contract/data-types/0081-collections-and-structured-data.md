# ADR-0081: Collections and Structured Data

**Status:** Draft

**Date:** 2026-10-25

## Related

- `docs/the-most-important-thing/what-contract-is.md`, especially §§4 and 17
- ADR-0046: IDL-First Interface Contract Frontend
- ADR-0047: One-Dimensional Contract Presentations and Realization Boundary
- ADR-0055: Whole Machine, Pipeline Composition, and Contract Concurrency
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0064: Input Contract, especially §§7.3 and 7.9
- ADR-0065: Admission Contract, especially §7
- ADR-0068: Fact Contract, especially §10
- ADR-0069: Invariant Contract, especially §15
- ADR-0071: Resolved Contract HIR
- ADR-0073: JVM Platform-Native Contract Ratification
- ADR-0075: Compiler-Produced Material, Legal Consumption, and Reuse
- ADR-0078: Deterministic Numeric Value and Operation Semantics
- ADR-0079: Type Interpretation and Semantic Binding
- ADR-0080: Characters, Strings, and Unicode
- ADR-0084: One-Dimensional Standard-Library Semantic Acquisition

---

## 1. Context

Java and Kotlin offer extensive collection libraries. A user may construct a collection through a mutable builder, use a
library view, or compose several operations before obtaining the value needed by a 1D Contract. These are ordinary ways
to express a calculation. They do not confer Contract authority on a Java or Kotlin object.

Kontrakt must solve two different problems.

1. It must interpret the collection operation the author actually selected. Java and Kotlin define that operation's
   source meaning.
2. It must establish stable collection results for its own judgments and compiler products. Kontrakt owns the meaning of
   those results.

A result can be represented by a host object without adopting that object's identity, lifecycle, or class hierarchy. The
applicable 1D Contract determines what the represented value means.

ADR-0078 and ADR-0080 establish the corresponding separation for Numeric and Text calculations. ADR-0079 and ADR-0084
govern source resolution and standard-library interpretation. This ADR applies those boundaries to collections and
states the collection-specific preservation obligations.

## 2. Problem

Collection APIs do not share one value or calculation law. Two operations may return the same host interface while
differing in the values they retain, the order they expose, or the point at which they evaluate an element.

For example:

| Source operation                                | Relevant difference                                                              |
|-------------------------------------------------|----------------------------------------------------------------------------------|
| Java `Set.of` / `Set.copyOf`                    | Duplicate rejection versus an unspecified retained representative                |
| Java `Map.of` / Kotlin `mapOf`                  | Duplicate-key rejection versus retaining the last value                          |
| Kotlin `distinctBy` / `associateBy`             | First representative versus last value for a selected key                        |
| Java `Arrays.asList` / Kotlin `asReversed()`    | A backed view rather than an independent value                                   |
| Kotlin `Iterable.map` / `Sequence.map`          | Eager calculation versus deferred calculation                                    |
| Kotlin `mapKeys` / Java `Collectors.toMap`      | Replacement after a key collision versus duplicate rejection or explicit merging |
| Java downstream `filtering` / upstream `filter` | An existing empty group versus no group                                          |
| Kotlin `runningFold` / `fold`                   | A sequence of prefix results versus one final accumulator                        |
| `HashSet` / `TreeSet` / `IdentityHashMap`       | Different element or key discrimination rules                                    |

A compiler that replaces these operations with one generic collection algorithm can change the author's calculation. A
compiler that retains a live host object as Contract material can also change an already established result when that
object is mutated.

Neither problem is solved by requiring authors to use immutable collection classes. Java and Kotlin do not make the
complete contents of a collection immutable merely because its public interface is read-only.

The design must preserve source meaning while preventing host object behavior from becoming Contract meaning. It must
also allow V1 and V2 to change their physical collection algorithms without changing a result already made available to
an authorized consumer.

## 3. Decision

### 3.1. Shared Collection Meaning and Authority

This ADR governs common collection value interpretation and calculation. It does not establish a new 1D Contract or an
independent collection judgment authority.

Each 1D decides which collection observations it may consume. Its existing law determines whether a particular
calculation is legal. A shared collection calculation cannot grant an Input distinction, establish Fact sameness, or
select a Canonicalization equivalence.

The selected operation determines the calculation it performs. The owning Contract determines which part of that result
has Contract meaning. These decisions must not be substituted for one another.

This ADR does not make a host `List`, `Set`, `Map`, array, or class into a universal Contract value family. Any common
value family must be defined by the distinctions it preserves, not by the interface used to carry it. The remaining
family-specific decisions are recorded in §4.

### 3.2. Host Objects Are Representations

Where its 1D permits the source form, Java and Kotlin objects may express values and calculations. A record, data class,
ordinary class, or collection can serve that purpose. The object does not thereby become a Contract entity.

A declared Contract value is determined by its owning law and the explicit source meaning that law admits. The host
class does not automatically contribute its members, inherited obligations, or `equals` method to that value.

Object-oriented properties must not determine Contract authority. In particular:

| Host property                             | Contract treatment                                    |
|-------------------------------------------|-------------------------------------------------------|
| Object reference or allocation identity   | Not a value or sameness determinant                   |
| Inheritance and override hierarchy        | Does not inherit or change a Contract obligation      |
| Virtual dispatch                          | Does not select an undeclared Contract law            |
| Encapsulated mutable instance state       | Cannot remain an authority over established material  |
| Exact, closed value-producing method      | May be interpreted under the owning 1D and ADR-0084   |
| Declared Contract reference or occurrence | Retains the identity law of its actual Contract owner |

The prohibition concerns the use of object-oriented behavior as Contract meaning. It is not a ban on reference types or
member-access syntax.

A Java record or Kotlin data class may represent declared constituents. Neither its generated `equals` nor its
constructor establishes Contract sameness. A source comparison may still be used if its exact calculation resolves to a
relation allowed by the owning 1D.

The source language's operators must retain their actual meanings. Java reference `==` must not be silently rewritten as
value equality. Kotlin `==` must not be assumed to be a valid Contract equality merely because it invokes `equals`. A
comparison whose result requires host reference identity is not a lawful Contract judgment. A null comparison may
instead express a legitimate presence test. A nominally fixed case, such as an enum constant, may be interpreted as a
declared case value where its source meaning supports that interpretation.

An object-identity dependency may be eliminated only when the applicable source and 1D laws justify the resulting
calculation. Removing a dependency is not permission to introduce object identity into Contract meaning.

User Operation realization is separate. It may use the JVM object model subject to ADR-0073 and the existing realization
laws. This section does not rewrite the semantics of an unchanged, already admissible platform call.

### 3.3. Exact Java and Kotlin Collection Operations

Kontrakt interprets the selected source callable before assigning collection calculation meaning. ADR-0079 owns type
interpretation, overload resolution, generics, receivers, and language interoperability. ADR-0084 owns qualification of
the selected standard-library operation.

For each supported Java or Kotlin SDK profile, the source-visible standard-library surface must be inventoried so that
collection operations are not missed because they belong to another package or are exposed through an extension or
producer API. Inventory coverage is not equivalent to completed semantic qualification. Nor does successful
qualification make an operation legal for every 1D.

A collection operation's interpretation must preserve the distinctions that affect its actual calculation. The
applicable SDK meaning determines which of these distinctions are required:

| Aspect         | Examples of required interpretation                                                   |
|----------------|---------------------------------------------------------------------------------------|
| Construction   | Accepted inputs, result family, duplicate and source-null handling                    |
| Transformation | Element selection, cardinality change, derived positions, grouping and prefix results |
| Comparison     | The equality or ordering relation actually used                                       |
| Traversal      | Encounter order, tie behavior and element selection                                   |
| Evaluation     | Deferred work, short-circuiting, callback invocation and permitted elision            |
| State          | Destination contents, partial mutation, backed views and aliasing                     |
| Completion     | Exceptions, termination and resource-dependent behavior                               |

A familiar type name or return signature is not sufficient evidence of any row in this table. One generic collection
model cannot replace operations whose source laws differ.

When a source operation permits several behaviors, ADR-0078 §3.3 and ADR-0073 §5.7 govern any deterministic refinement.
The compiler cannot invent an otherwise unsupported choice and call it the source operation's meaning.

### 3.4. Collection Values and Observable Results

A collection calculation preserves the distinctions required by its selected source law and the legal observations of
its consuming Contract. The output of a transformation need not preserve every distinction of its input. It must
preserve the result that the transformation actually defines.

The distinctions relevant to a result depend on its operation. For example:

| Result                  | Required distinction when legally observable                                                           |
|-------------------------|--------------------------------------------------------------------------------------------------------|
| Sequence or slice       | Position and repeated occurrences; an index-based slice may select one position more than once         |
| Zip or filter           | The selected elements and any defined encounter order; unmatched or excluded elements need not survive |
| Set-like result         | The applicable membership relation and retained representative, when observable                        |
| Association or grouping | Key relation, chosen value, entry order, group presence and constituent order as actually defined      |
| Prefix or window result | Each produced intermediate result and its source-defined lifetime                                      |

Two Kotlin Sets can compare equal while exposing different encounter orders. A later lawful first-element selection can
distinguish them. Collection equality is therefore not sufficient proof of equivalent observations.

A derived index belongs to the traversal that produced it. It does not create an Input coordinate or give an unordered
Input Membership an undeclared position (ADR-0065 §7.4). Likewise, a source operation that permits an unordered choice
may leave the membership of its output underdetermined, not merely its iteration order. Any refinement follows §3.3.

No universal sort is imposed to manufacture determinism. The compiler may discard a distinction only when no remaining
legal calculation needs it. ADR-0078 §4.3 and ADR-0080 §3.7 govern that preservation obligation.

### 3.5. Immutable Results at Contract Boundaries

A collection may be constructed or calculated through temporary mutable state. That state is not automatically Contract
material.

**Any collection result made available to an authorized later Contract stage or other consumer must be a complete,
immutable semantic value.** Later mutation of the producer's working state, original carrier, or another alias must not
change the result. This obligation covers every constituent the consumer may legally observe.

The result must represent one coherent observation of its source. Combining incompatible observations into an immutable
container does not satisfy this rule. A read-only wrapper, `final` field, or Kotlin `val` does not establish closure by
itself.

A source operation may mutate an existing destination or leave earlier changes in place before failing. Those facts
remain part of its interpreted source meaning. A failed local calculation does not produce a successful Contract result,
and this ADR does not require a transactional rollback implementation. Only the owning 1D can permit the temporary
computation (ADR-0065 §7.2, §7.5; ADR-0073).

This boundary is semantic, not a requirement to copy a container at every physical phase. A compiler may use local
mutable buffers, structural sharing, or another representation when the exposed result has the required meaning.

The existing Input boundary is narrower: ADR-0064 §7.3 requires an immutable presentation **before** Input judgment.
Input does not repair a mutable carrier by freezing it during judgment. Kontrakt-controlled acquisition or formation may
establish a legal value before that boundary, subject to ADR-0073 §5.2.

An incomplete lazy view or a live mutable alias cannot be treated as an established collection value merely because its
current contents were once observed. Its use requires a lawful completed result under the owning boundary.

### 3.6. Source Null, Contract Absence, and Lookup

Java and Kotlin null handling belongs to the exact source operation. A Map can distinguish an absent key from a key
mapped to `null`, while a particular lookup may return `null` for both. Kontrakt must preserve the selected operation's
result. It must not recover a distinction that the operation did not produce.

Contract absence instead follows the explicit law of its owner. A source `null` is not a universal Contract value, and
it does not become Absence without a complete declared interpretation (ADR-0064 §7.6). An empty collection is likewise
not an absent collection. A missing Map association is not automatically a missing Input coordinate.

Absence need not be a member of every value type. A declared coordinate may lack a value under its own presence law. An
operation that requires the value can consume it only when its domain is established. Missing material, unresolved
meaning, and unsuccessful computation are not Absence (ADR-0063; ADR-0071).

Defaults and partial lookups retain their source-defined behavior. A lookup's default does not create an Input
constituent. A host exception does not become `Rejected` or `Admitted` in Admission (ADR-0065 §7.5). How an IR
represents a declared absence is an implementation choice, not a new universal Nullable type.

### 3.7. Equality, Ordering, and Membership

No universal host equality becomes the common collection sameness law.

The selected SDK operation keeps its actual comparison semantics. A source `HashSet`, a sorted collection, and an
identity-based collection need not discriminate elements in the same way. Those differences must be understood before
the operation is used in a governed judgment.

The receiving 1D owns its own relation. In particular, ADR-0064 §7.9.1 owns Input Presentation Sameness, and ADR-0068
§10 owns Fact sameness. Host `equals`, `hashCode`, a comparator, or a collection constructor cannot replace either law.

An Admission-local calculation may use a different explicit comparison when ADR-0065 §7 permits it. That comparison does
not revise Input's membership or ordering law. ADR-0069 §15 separately restricts comparisons and collection operations
in Invariant source; this ADR does not broaden that surface.

A comparator's equality with `equals` is not assumed. If a relation is to serve as Contract sameness or a total
ordering, it must satisfy the properties required by the owning law on its actual legal domain. Host inheritance,
dynamic dispatch, or mutable comparator state cannot supply an undeclared relation.

### 3.8. Duplicate Handling and Representative Selection

Duplicate handling follows the selected operation. A constructor may reject duplicate keys. A conversion may retain one
representative. A sequence operation may preserve repeated occurrences. These are different results.

In an association or grouping result, the key representative, selected value, entry order and presence of a group are
separate observations. For example, filtering within a Java grouping collector can leave an empty group. Filtering
before grouping can omit that group. A Kotlin key transformation can replace the value at a colliding key without
thereby promising which equal host key instance remains.

When an operation selects a representative, preserve its selection law wherever the representative can affect a legal
later observation. A result may require first-wins, last-wins or stable tie behavior, but none of these rules is
universal. Source implementation evidence cannot be elevated into an SDK guarantee without qualification (ADR-0084).

Kontrakt-owned calculations cannot let physical traversal order, thread completion order or hash-table layout decide an
observably different representative. If candidates are indistinguishable under every permitted observation, their
physical choice need not become Contract meaning. Otherwise the owning calculation requires a sufficient selection law
or a justified refinement under §3.3.

This does not amend Input's rule against silent deduplication or representative choice at its boundary (ADR-0064
§7.9.1).

### 3.9. Deferred and Unlawful Calculations

A recognized SDK call can still be unlawful for a 1D. Its calculation may require host object identity, an unclosed
callback, external state or an observation that the receiving Contract does not admit. ADR-0084 §3.4 governs incomplete
source interpretation. The owning 1D decides legal use and judgment failure.

Deferred computation cannot be presented as a complete collection result before the consumer's required observation has
been lawfully resolved. Java Stream and Kotlin Sequence differ in evaluation and reuse. In particular, Java Stream may
elide callbacks, and a Kotlin Sequence may be one-shot. Result order does not prove callback execution order, even for
an order-preserving parallel operation.

Each source operation determines the permitted callback invocations and failure behavior. A prefix calculation may
expose intermediate results, but mutable accumulator aliasing does not authorize the compiler to invent independent
snapshots that the source operation did not produce. Once a lawful result is passed to another Contract stage, §3.5
applies.

The existing partial-operation, completion and resource laws remain in force (ADR-0065 §7.4–7.5; ADR-0073). No
Collection-specific runtime policy engine, proxy verifier, or fallback execution of unknown user code is introduced.

### 3.10. Contract Pipeline and Composition

The IDL selects the applicable 1D subjects. The Frontend resolves permitted collection expressions into the meaning
required by each selected 1D. Before a complete Resolved Contract HIR candidate becomes visible, that meaning must be
sufficient for Establishment. Establishment does not rerun Java/Kotlin source resolution or SDK qualification (ADR-0071;
ADR-0063).

A shared collection calculation may support several 1D definitions. It does not join their authority or alter their
order of judgment. The ordinary pipeline remains one-way, with no access to another flow's internal stages.

Whole Machine composition relates declared Contract surfaces. It cannot insert a collection operation between another
Core's stages or share a mutable collection as Contract state across flows. Material that lawfully leaves one Core and
later enters another starts a new flow through the latter's Input boundary (ADR-0055 §3.4).

The Canonical Contract World contains established authority under ADR-0063. It is not a mutable host object graph
through which other computations may revise an established result. Governance retains its own authority and composition
rules. Neither concept inserts a new stage into a running Contract pipeline.

### 3.11. Compiler Realization and V1/V2 Preservation

A collection law is defined by its legal result, not by a JVM collection class or storage algorithm. A physical
implementation can change only when the legally observable result stays the same.

For Contract compilation, this requirement applies to HIR candidates, established material, and compiler-produced
results according to their own observation laws. ADR-0075 governs comparison and reuse. A hash, HID, memory address, or
serialized representation is not automatically semantic equality.

For user Operation realization, the platform's original calculation must be preserved. An optimization may replace or
fuse collection work only when the applicable source and Contract observations permit it. An unchanged admissible
platform call can remain an unchanged call under ADR-0073 §9.6.

V1 may use mutable scratch storage before producing an immutable result. V2 may use parallel or incremental calculation.
A transformation that fuses loops or reorders a reduction must preserve every required source observation, including
reachable failure and callback behavior. ADR-0078 §9.2 governs Numeric reductions.

For incremental maintenance, a change to an earlier member can change a first-wins representative or a prefix result. A
cached or incremental result must agree with a lawful full computation under its consumer's observation law. ADR-0075
governs result validity, coherent observation and reuse; the index or retained history needed to meet that law belongs
to Design. Neither version may introduce a new Contract observation through scheduling, cache state or update history.

## 4. Consequences and Deferred Work

The shared law does not make every Java/Kotlin Collection operation legal in every 1D. An otherwise resolved operation
can fail the owning 1D's requirements. Value formation can require additional work when a source exposes aliases or
deferred results, but the compiler is not required to copy every collection. Optimization is permitted when its legality
can be established from the source and Contract observations (§3.11).

The following work remains outside this draft's decided meaning:

| Work                            | Owner or constraint                                                                                                                                                                        |
|---------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Common collection families      | Decide the exact need and domains for Sequence, Membership, Association, Multiset and structured forms. Do not infer them from host interfaces (§3.1).                                     |
| Sorting and aggregation details | Qualify source-specific tie rules, numeric reductions, collectors and parallel execution. Existing Numeric and 1D laws apply until further decisions.                                      |
| Representative refinement       | Determine per operation when source-permitted nondeterminism admits a lawful deterministic refinement (§3.3).                                                                              |
| SDK coverage and conformance    | Complete Java JDK 27 and Kotlin 2.4.21 callable-level qualification. The existing declaration inventories are not proof of complete coverage (ADR-0079; ADR-0084).                         |
| Object laws outside collections | Align the general representation/authority boundary with existing Accepted ADRs and still-Draft ADR-0083. Do not create a second object authority here.                                    |
| Input evolution                 | Reconsider ADR-0064's current collection limits after this ADR is decided. Nothing here silently changes its Accepted V1 surface.                                                          |
| IR and compiler Design          | Choose value encodings, explicit absence representation, formation procedures, protocols, optimization and incremental algorithms. Prove V1/V2 conformance under the owning compiler ADRs. |

The SDK catalogs and compiler designs are separate artifacts. This ADR states the meaning they must preserve; it neither
declares every source call qualified nor selects a JVM collection implementation.