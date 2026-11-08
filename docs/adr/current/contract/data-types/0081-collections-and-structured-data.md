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

| Source operation                             | Relevant difference                                               |
|----------------------------------------------|-------------------------------------------------------------------|
| Java `Set.of` / `Set.copyOf`                 | Duplicate rejection versus an unspecified retained representative |
| Java `Map.of` / Kotlin `mapOf`               | Duplicate-key rejection versus retaining the last value           |
| Kotlin `distinctBy` / `associateBy`          | First representative versus last value for a selected key         |
| Java `Arrays.asList` / Kotlin `asReversed()` | A backed view rather than an independent value                    |
| Kotlin `Iterable.map` / `Sequence.map`       | Eager calculation versus deferred calculation                     |
| `HashSet` / `TreeSet` / `IdentityHashMap`    | Different element or key discrimination rules                     |

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

| Aspect       | Examples of required interpretation                            |
|--------------|----------------------------------------------------------------|
| Construction | Accepted inputs, duplicate handling, null handling             |
| Contents     | Positions, multiplicity, key/value association                 |
| Comparison   | The equality or ordering relation actually used                |
| Traversal    | Encounter order and selection of an element                    |
| Evaluation   | Eager or deferred execution; callback invocation               |
| State        | Mutations, backed views, aliasing, concurrent observation      |
| Completion   | Exceptional outcomes, termination, resource-dependent behavior |

A familiar type name or return signature is not sufficient evidence of any row in this table. One generic collection
model cannot replace operations whose source laws differ.

When a source operation permits several behaviors, ADR-0078 §3.3 and ADR-0073 §5.7 govern any deterministic refinement.
The compiler cannot invent an otherwise unsupported choice and call it the source operation's meaning.

### 3.4. Collection Value and Observable Distinctions

A collection result preserves every distinction required by the selected calculation and by the legal observations of
its consuming Contract. This is the collection application of the information-preservation principle used by ADR-0078
and ADR-0080.

The relevant distinctions depend on the operation. They can include position, repeated occurrence, membership, key/value
association, encounter order, and the retained representative. **Collection equality alone does not prove that all those
distinctions agree.**

For example, two Kotlin Sets can compare equal while exposing different encounter orders. If an admitted calculation
selects the first element, that order can affect its result. If the consuming Contract observes only membership, the
order need not become part of that Contract's value.

A source operation does not grant the consumer observations that its 1D law does not permit. Conversely, the compiler
must not discard a distinction while an admitted subsequent calculation still needs it.

The absence of a declared order is not an implicit order selected by a hash table or iterator. No universal sort is
imposed to make collection results deterministic. Where a law requires order, the exact ordering must be preserved.

### 3.5. Immutable Results at Contract Boundaries

A collection may be constructed or calculated through temporary mutable state. That state is not automatically Contract
material.

**Any collection result made available to an authorized later Contract stage or other consumer must be a complete,
immutable semantic value.** Later mutation of the producer's working state, original carrier, or another alias must not
change the result. This obligation covers every constituent the consumer may legally observe.

The result must represent one coherent observation of its source. Combining incompatible observations into an immutable
container does not satisfy this rule. A read-only wrapper, `final` field, or Kotlin `val` does not establish closure by
itself.

This boundary is semantic, not a requirement to copy a container at every physical phase. A compiler may use local
mutable buffers, structural sharing, or another representation when the exposed result has the required meaning.

The existing Input boundary is narrower: ADR-0064 §7.3 requires an immutable presentation **before** Input judgment.
Input does not repair a mutable carrier by freezing it during judgment. Kontrakt-controlled acquisition or formation may
establish a legal value before that boundary, subject to ADR-0073 §5.2.

An incomplete lazy view or a live mutable alias cannot be treated as an established collection value merely because its
current contents were once observed. Its use requires a lawful completed result under the owning boundary.

### 3.6. Equality, Ordering, and Membership

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

### 3.7. Duplicate Handling and Representative Selection

Duplicate handling is part of the selected operation's meaning. A constructor that rejects duplicates is not equivalent
to one that retains a value. A grouping operation is not equivalent to one that discards repeated occurrences.

When an operation retains a representative, its selection law must be preserved wherever the representative can affect a
legal later observation. This applies to source-defined first-wins, last-wins, stable tie handling, and other qualified
selection behavior.

Kontrakt-owned calculations must not let a physical traversal order, thread completion order, or hash-table layout
decide an observably different representative. If legally distinguishable representatives remain possible, the owning
calculation needs a sufficient selection law or a justified refinement under §3.3.

No global first-wins, last-wins, or lowest-value rule is introduced. If every legally permitted observer treats
candidate representatives identically, their physical choice need not become Contract meaning.

This does not amend Input's existing rule against silent deduplication or representative choice at its boundary
(ADR-0064 §7.9.1).

### 3.8. Incomplete and Unlawful Calculations

A recognized SDK call can still be unlawful for a 1D. The source may require object identity, live external state, an
unqualified callback, or an observation that the receiving Contract does not admit. The existing 1D law decides that
use.

The compiler must also distinguish an unqualified source operation from a fully interpreted operation that violates a
Contract. ADR-0084 §3.4 governs incomplete interpretation. The owning 1D and existing failure laws govern Contract
refusal and unsuccessful calculation.

A deferred calculation does not yield an immutable Contract result until the value needed by the consumer has been
lawfully completed. Evaluation time, short-circuit behavior, and source failure cannot be erased to manufacture a
convenient result.

There is no Collection-specific runtime policy engine, proxy verifier, or general fallback to executing unknown user
code.

### 3.9. Contract Pipeline and Composition

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

### 3.10. Compiler Realization and V1/V2 Preservation

A collection law is defined by its legal result, not by a JVM collection class or storage algorithm. A physical
implementation can change only when the legally observable result stays the same.

For Contract compilation, this requirement applies to HIR candidates, established material, and compiler-produced
results according to their own observation laws. ADR-0075 governs comparison and reuse. A hash, HID, memory address, or
serialized representation is not automatically semantic equality.

For user Operation realization, the platform's original calculation must be preserved. An optimization may replace or
fuse collection work only when the applicable source and Contract observations permit it. An unchanged admissible
platform call can remain an unchanged call under ADR-0073 §9.6.

V1 may use mutable scratch storage before producing an immutable result. V2 may use parallel evaluation or incremental
maintenance. Neither may introduce a new Contract observation through its scheduling, iteration order, cache state, or
update history. Physical strategies remain Design choices.

## 4. Consequences and Remaining Decisions

### 4.1. Consequences

- Authors may use ordinary Java/Kotlin collection expressions without first converting all source objects to an
  immutable library type. Legality is still judged by the owning 1D.
- The Contract pipeline consumes closed values, not live JVM collections. An object may represent a value but never
  supplies its Contract authority.
- Source collection behavior remains distinguishable where its result or evaluation affects an admitted calculation. A
  common implementation cannot silently erase those differences.
- Internally mutable algorithms and future collection engines remain possible because physical representation is not the
  result law.

### 4.2. Decisions Not Closed by This Draft

The following questions require their own investigation. The preceding sections must not be read as implicit answers.

| Question                            | Required decision or follow-up owner                                                                                                                                          |
|-------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Common collection families          | Whether Sequence, Membership, Association, Multiset, and structured product/choice require separate common value laws; their exact domains and operations                     |
| Presence and lookup                 | Missing key versus present null; empty results; default calculation and index failure                                                                                         |
| Traversal and aggregation           | Exact rules for lazy/one-shot sources, callbacks, termination, grouping, reduction, and order-sensitive results                                                               |
| Representative eligibility          | Which source-specific nondeterministic selections permit lawful deterministic refinement on a governed domain                                                                 |
| Object semantics beyond collections | General Host object/reference law belongs with the existing source boundary and the still-Draft ADR-0083, not a new collection authority                                      |
| Input coverage                      | Reconsider ADR-0064's historical collection forms and carrier limits **after** this ADR is decided; no change occurs here                                                     |
| Protocol and implementation         | SDK qualification catalog, value formation, HIR projections, internal collection engines, verification, and V1/V2 conformance belong to Design and their owning compiler ADRs |

The Java JDK 27 and Kotlin 2.4.21 source inventories are research inputs, not completed per-operation qualification.
ADR-0079 and ADR-0084 govern their remaining coverage and meaning verification. This draft neither claims blanket SDK
admissibility nor fixes a specific backend implementation.