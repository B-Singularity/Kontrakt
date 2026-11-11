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
- ADR-0067: Lowering Contract, especially §§4, 6, and 17
- ADR-0068: Fact Contract, especially §10
- ADR-0069: Invariant Contract, especially §15
- ADR-0071: Resolved Contract HIR
- ADR-0073: JVM Platform-Native Contract Ratification
- ADR-0075: Compiler-Produced Material, Legal Consumption, and Reuse
- ADR-0078: Deterministic Numeric Value and Operation Semantics
- ADR-0079: Type Interpretation and Semantic Binding
- ADR-0080: Characters, Strings, and Unicode
- ADR-0084: One-Dimensional Standard-Library Semantic Acquisition
- `docs/sadr/0002-collection-observation-integrity-and-protected-change-security-architecture.md` — collection-specific
  security threats and assurance; non-authoritative for Contract meaning
- `docs/design/collections/kontrakt-collection-sdk-interpretation-and-calculation-design.md` — source/implementation
  catalogs, SDK qualification, evidence and conformance; non-authoritative for Contract meaning

---

## 1. Context

Java and Kotlin offer extensive collection libraries. A user may construct a collection through a mutable builder, use a
library view, or compose several operations before obtaining the value needed by a 1D Contract. These are ordinary ways
to express a calculation. They do not confer Contract authority on a Java or Kotlin object.

Kontrakt must interpret the exact source operation selected by the author while establishing its own Contract meaning.
The selected Java or Kotlin operation determines its source calculation; the owning 1D determines which result and
observations it may lawfully use. The calculation's source meaning is not automatically the meaning of a Contract
result.

There are two distinct times at which this separation matters. Contract compilation resolves definitions, establishes
authoritative definition material, and produces compiler results. At runtime, an already-established Contract governs
each fresh Input occurrence and the subsequent formation and establishment of Fact material. Temporary mutable
computation in either implementation does not make host objects authoritative.

A result may use a host representation without adopting its identity, lifecycle, or class hierarchy. ADR-0078 and
ADR-0080 establish the corresponding separation for Numeric and Text calculations. ADR-0079 and ADR-0084 govern source
resolution and standard-library interpretation. This ADR applies those boundaries to collection calculations and results
without imposing a collection algorithm on either compiler or runtime realization.

## 2. Problem

Collection results differ not only in their element values, but in the constituents and relationships they make
observable. Occurrence order, index coordinates, multiplicity, membership, key/value correspondence, grouping,
representative selection, ranges, nested constituents, and absence may be independently significant. An operation may
produce a complete value, modify an existing destination, create a view tied to its origin, or defer computation until
consumption. Sharing a host interface does not make these meanings interchangeable.

A specification may guarantee a particular result or allow a set of results. Kontrakt must preserve the former and
establish a lawful deterministic refinement for the latter when used in a governed Contract judgment. Repeating one
host implementation's usual result cannot establish the refinement law.

Intermediate failure may leave source mutations or other effects, while a consumer can use a pending observation
before its producer is withdrawn. This ADR determines the collection distinctions that have to be preserved; the
validity, protection, rollback and propagation of dependent results remain with their owning laws. At runtime, an
external mutable carrier must not become the continuing authority over an already formed Input presentation or Fact.
At compile time, a working collection must not become authoritative Definition material merely by being retained.

Neither prohibiting mutable source objects nor selecting one collection implementation solves these problems. **The
common Contract law belongs here; the Java/Kotlin implementation surfaces, source-callable catalog, source
profiles and conformance evidence belong to the Collection SDK Interpretation and Calculation Design.**

## 3. Decision

### 3.1. Shared Collection Meaning and Authority

This ADR defines the constituent meaning of completed collection and structured values. It creates no new 1D
Contract, independent judgment, Collection runtime or universal host API, and gives no shared collection calculation
authority to establish Input, Fact or other Contract material. The owning 1D decides which completed meanings it
admits and how its own Sameness, judgment, Identity and Establishment apply. In particular, describing a Bag or
Positional Product does not change ADR-0064 §7.9's V1 Input
presentation forms.

A family describes what a value must preserve, independently of the class or storage used to realize it. A source
operation may return a value that combines several such laws. The laws below describe **five basic forms of completed
value**, followed by closed forms and compositions that retain their own exact observations. They are not a claim that
five is a unique mathematical minimum, five public classes, five IR variants or five kinds of Contract authority.
Their use depends on the exact source meaning and the authoring law that admits it.

#### 3.1.1. Common formation and the five basic forms

A completed collection or structured value has the exact constituent schema required by its selected form, the
lawfully admitted meaning of every present constituent, and a complete account of the relationships that form makes
observable. The form fixes its legal empty values, presence obligations, repetitions, discrimination, and completion.
Missing required material, an unresolved Source Operation, a pending producer, and a failed calculation are not
completed values. A value need not retain a particular host object or physical materialization after its meaning has
been lawfully completed (§§3.5 and 3.9).

Discrimination belongs to the use and law that require it. Host `equals`, a source comparator, Input Presentation
Sameness, and a subsequent 1D relation are not one global equality. Full constituent agreement must preserve all
observations owned by the completed form; it does not introduce a universal collection `equals` (§§3.1.4 and 3.7).
Source-defined representatives can remain observable even when multiple candidates belong to one discrimination
class (§3.8). Presence and an admissible null-like constituent meaning are governed once, in §3.6.

The five basic forms are:

| Basic form                    | Required completed-value meaning                                                                                          |
|-------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| **Ordered Occurrence**        | A finite number of constituent occurrences, their complete linear encounter order, and the value at each occurrence       |
| **Finite Relational Extent**  | A finite relation over exact declared roles, with presence or nonnegative multiplicity for each distinguished tuple       |
| **Functional Correspondence** | A lawful key domain, its present-key domain, and one mapped value per present key; partial and total laws remain distinct |
| **Declared Product**          | A finite closed schema of named roles or fixed slots, with each role's presence and constituent-value obligations         |
| **Closed Tagged Alternative** | A finite closed case schema, exactly one case selected by each completed value, and the required case payload             |

**Ordered Occurrence.** A completed ordered value contains a finite number `n` of separate occurrences. Each has one
completed constituent value; the occurrences possess one complete linear order. Consequently every occurrence has a
relative rank in `0..n-1`, but that rank is not its identity, a host address, or an independently declared Input
coordinate. Equal constituent values may occupy distinct occurrences. `[A, B, A]` therefore has three occurrences
and differs in its ordered construction from `[A, A, B]`; converting either to Membership may discard that order and
its repetitions only under an explicit conversion law. The unique empty Sequence has zero occurrences under its
selected constituent form. A pending iterator or partly consumed producer does not thereby establish that empty
result. An individual short-circuit operation can complete its own result without completing the entire source (§3.9).

The existence of a linear order does not imply a supported random-access operation. An **Indexed Sequence**
additionally defines the valid element-position domain, the correspondence between each position and occurrence,
and any exact source-defined access and boundary law (§3.1.2). A source-backed, reverse, or live view retains its
qualified observation lifetime and effects under §§3.3, 3.5 and 3.9 rather than becoming part of an immutable
Sequence's semantic identity. Ordered constituent agreement requires equal extent, corresponding ordered occurrences,
and agreement of their constituent meanings under the applicable declared relation; it is not a mandatory host
`equals` implementation.

**Finite Relational Extent.** A relation declares a closed, finite role schema and an exact arity, including the
constituent meaning of every role. Its completed support consists of finitely many distinguishable tuples, without an
intrinsic traversal order. A **Membership Extent** records presence or absence of each distinguishable tuple; a **Bag
Extent** records an exact nonnegative integer multiplicity. Multiplicity zero means nonmembership, one means one
occurrence, and larger values preserve repetition without creating an order. Distinct-member count and the sum of
multiplicities are not interchangeable. Signed deltas used in a transformation cannot be treated as negative counts
in a completed Bag. The role schema survives an empty result: an empty binary relation and an empty ternary relation
are different completed forms. When a zero-arity relation is explicitly admitted, the empty tuple is either present
or absent; this resemblance to truth or falsehood grants no Boolean Contract authority.

The discrimination used for Membership or Bag uniqueness must be an equivalence relation on the lawful constituent
or tuple domain: reflexive, symmetric and transitive. A nontransitive approximate comparison does not establish
unique members. A qualified source comparator may give a different equivalence partition from host `equals`; its
lawful equivalence and any legally observable exact retained representative must both be preserved. For example,
two decimal representations may be comparator-equivalent while retaining different source-observable scale. It is
not lawful to discard the actual representative solely because the membership class is unchanged. The owning 1D,
not this common form, decides whether that source discrimination can participate in its judgment (§§3.7–3.8).
Relational constituent agreement requires compatible exact role schemas, the same distinguishable support, the same
multiplicities where provided, and agreement of any representative or additional observation owned by the selected
completed form. It does not identify physically distinct Contract subjects or occurrences.

**Functional Correspondence.** A correspondence declares the lawful key domain, its qualified key-discrimination
law, the subset of keys with present mappings, and exactly one completed value at each distinguished present key.
An admissible but absent key is not an invalid key. A **partial** correspondence permits missing mappings; a **total**
correspondence requires one mapping for every lawful key. A present mapping whose value has an admissible
null-like meaning differs from an absent mapping (§3.6). Two different values cannot coexist as separate mappings
for one distinguished key. Repeated input keys are handled according to the exact construction operation: reject,
retain, replace or aggregate are not implicit Family defaults (§§3.3 and 3.8).

A completed finite Association can also be described as a finite binary Relational Extent with a declared key role
and functional uniqueness. Where both descriptions apply, the exact key discrimination, present-key set, mapped
values and required representatives must agree; the overlap does not create two conflicting Mapping authorities.
The correspondence law also permits a qualified total function over a non-enumerated or unbounded key domain, but
only when a closed law determines the mapped value for every lawful key and establishes the required meaning,
completion and legal observations under the owning Contract. An arbitrary callback, unresolved predicate or host
storage default is not such a law. A function may be mathematically described without arbitrary equality or
full-domain observations being decidable; an operation claiming those results must separately establish their
exact, lawful completion. Agreement of two correspondences requires compatible key-domain and discrimination laws,
matching presence, corresponding mapped values and all additional legally observed representative or totality
information; physical entry order supplies none unless separately declared.

**Declared Product.** A Product declares a finite, closed schema of constituent roles. Each role has an exact
admissible value meaning and an independently determined presence obligation. A **required** role must have a present,
completed constituent; an **optional** role may be explicitly absent. Presence is not nullability: a required role
may admit a null-like value, while an optional role may admit absence without admitting such a value. A role can
admit both only when its value law permits the latter (§3.6). Every role's presence and, when present, its value
must be settled for Product completion.

A **Named Product** distinguishes roles by exact declared names or nominal coordinates; their declaration order
is not automatically constituent encounter order. A **Positional Product** distinguishes fixed declared slots, each
of which may have its own value domain; its slots are not occurrences of an arbitrary Sequence. Two nominal schemas
are not equivalent merely because their field spellings and value types match. Structural correspondence between
schemas requires its own explicit legal meaning. A closed schema with no roles admits one completed empty Product
when the owning 1D permits that form; it is not an empty Sequence or Association. Relations between multiple roles,
such as one role's presence depending on another role's value, require an explicit law of the specific closed form
or owning Contract rather than an arbitrary executable predicate built into the basic Product. Source Type binding
remains with ADR-0079; neither a Java record nor a Kotlin data class automatically defines Contract constituents.
Product constituent agreement requires the same or expressly compatible schema and corresponding role-presence and
value meanings; it is not inferred from the source's generated `equals`.

**Closed Tagged Alternative.** A Choice has a finite, explicitly closed schema of declared cases. Every completed
Choice selects exactly one case. A case prescribes either no payload or one completed value with an exact admitted
domain; a multi-field payload may itself be a Declared Product. Unselected cases contribute no payload, and the
absence of a payload in a payloadless case differs from a present payload whose law admits a null-like value (§3.6).
A schema with no cases has no completed Choice value; it does not create an empty Choice instance. Case identity is
determined by its exact declaration, not by a source enum ordinal, host address, class inheritance or matching case
spelling. Two completed Choices agree only under compatible exact case schemas, the same selected case and the
corresponding required payload meaning. An open subtype search or undeclared runtime subtype cannot enlarge or
select a Contract case. Qualified source `Optional`, `Result` and enum-like meanings may be represented, but the
choice of a case creates no 1D Absence, Failure, Identity or judgment authority of its own.

These five forms are distinct complete-value formation laws, not public classes, IR encodings or separate Contract
authorities. A shared representation or lawful conversion is not a proof that the values themselves have the same
meaning. A conversion that removes order, folds multiplicity, chooses a representative, manufactures a position, or
introduces a new case must be defined by its own exact result law rather than silently changing the source value.

#### 3.1.2. Closed value forms and compositional obligations

The five forms do not replace the names and obligations by which a user expresses a particular collection. A
completed Membership, Bag, Association, Indexed Sequence or structured result must be recognized under its own **closed
form law**, even when its construction reuses a basic form. Each such law fixes its additional schema,
observations, completion and legal conversion. Merely placing two base descriptions beside one another is not a
completed composite value: the same lawful constituents must be related consistently across their descriptions.

| Completed form           | Basic formation                                 | Additional meaning that must be preserved                                                                   |
|--------------------------|-------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| Encounter Sequence       | Ordered Occurrence                              | Repeated occurrences and source-guaranteed encounter order; no implicit indexed access                      |
| Indexed Sequence         | Ordered Occurrence with a declared position law | The exact valid positions, position-to-occurrence correspondence, and lawful boundary/access distinctions   |
| Multiset / Bag           | Finite Relational Extent, Bag mode              | Nonnegative multiplicity per distinguishable member, without an invented order                              |
| Membership               | Finite Relational Extent, Membership mode       | Presence and absence under the selected lawful member-discrimination relation                               |
| Association              | Functional Correspondence                       | Key presence, exactly one mapped value per distinguished present key, and required key representatives      |
| Indexed Membership       | Membership with a declared index law            | Which valid indexes are present, its range observations, and the distinction between cardinality and extent |
| Ordered Interval / Range | Closed interval law                             | Ordered carrier, exact bounds, endpoint inclusion or exclusion, empty condition and lawful membership       |
| Discrete Progression     | Closed progression law                          | Start, step, direction, termination, last-value and generated-occurrence laws                               |
| Named Product            | Declared Product, named roles                   | Exact declared constituent coordinates and their presence laws                                              |
| Positional Product       | Declared Product, fixed slots                   | Per-slot roles and values, without treating the slots as a generic V1 Input tuple                           |
| Closed Tagged Choice     | Closed Tagged Alternative                       | Exact selected case and conditional payload                                                                 |

An Indexed Sequence's relative position may be derived from its encounter order, but any source-defined index
units, allowed indexes, boundaries or accesses retain their exact law. For a zero-based List of length `n`, element
indexes and inter-element boundaries are not the same domain. Those relative positions are not Input nominal
coordinates, Text scalar positions or physical Dense Ordinals. Indexed Membership likewise distinguishes its
included indexes from the entire valid index domain. For a Java `BitSet`, the results of `cardinality`, `length` and
`size` must follow their distinct exact source meanings; reserved storage capacity is not a universal Membership
cardinality.

Additional completed forms combine relations without creating additional Contract authorities:

| Composed form            | Required coupling                                                                                                                                                  |
|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Sequenced Membership     | The ordered occurrences correspond exactly to the distinct present members; neither missing nor repeated members may be invented by the order component            |
| Navigable Membership     | Membership, exact comparison order and source-defined bounds and neighbor selection; a sorted traversal alone is insufficient                                      |
| Sequenced Association    | Encounter order is defined over the actual mapping entries; an access-order view or modification has its separately qualified source effects                       |
| Navigable Association    | The same mapping keys participate in the qualified comparison, range and neighbor laws, including comparator-equivalent keys and retained representatives          |
| Grouped Collection       | Group keys and group presence are defined; each present group's constituents retain their own occurrence, multiplicity and order laws                              |
| Partitioned Collection   | Declared partition alternatives and their presence are respected, including an explicitly existing empty partition                                                 |
| Multi-Association        | A key can have multiple value occurrences; their multiplicity and any source-defined order are preserved without imposing functional uniqueness on each occurrence |
| Slice / Window result    | Selected constituents, exact selection boundaries and any source-required origin relationship or temporary observation lifetime                                    |
| Prefix result            | Each prescribed intermediate result and any initial result, with the exact source calculation order and completion law                                             |
| Nested structured result | Each contained value's formation, required presence and completeness; nested references do not themselves create Contract identities                               |

A Group with an empty constituent and a Group that does not exist are different wherever the applicable law exposes
that difference. A Window result that borrows a temporary view is not thereby a persistently completed immutable
Contract value. A prefix calculation that returns intermediate values remains subject to its actual evaluation and
alias conditions (§3.9). The tables define required distinctions; whether a specific Source Operation forms each
result and how it does so remains governed by §3.3. A composite is complete only when the meaning of each constituent
form is complete and its cross-form coupling is satisfied. A Sequenced Membership therefore cannot contain an ordered
occurrence absent from its Membership or repeat a member that is unique under its applicable discrimination.
A partition with declared alternatives retains the existence of an empty partition; a Group absent from a grouping
is not silently formed from a default empty group.

#### 3.1.3. Interval, progression, coordinate and shaped-value laws

There is no generic `Domain` or `Coordinate Region` Family that can accept an arbitrary predicate and confer Contract
meaning on it. An ordinary value's admitted domain follows its owning Type, Numeric, Text and 1D laws. A range,
progression or shaped result instead supplies a **specific completed meaning** with the following obligations.

An **Ordered Interval** declares the exact ordered carrier, a comparison that determines every required lawful
endpoint/member ordering, lower and upper bounds where
present, the inclusion or exclusion of each endpoint, and the resulting empty and membership conditions. Bounds and
included members are separate observations where the completed form exposes both. For example, `[1,5]` and `(0,6)`
include the same integer members but expose different endpoints if their selected forms retain those endpoints.
Conversely, two different source expressions that resolve to the same exact range value are not kept distinct merely
for their syntax. Neither enumerating the included members nor a host `equals` operation determines all
source-guaranteed
Range observations. An Interval may be complete by its law without enumerating an unbounded or continuous carrier;
it does not thereby establish a completed infinite Sequence.

A **Discrete Progression** declares its start, step, direction, stopping and last-member law, including the exact
conditions for emptiness, valid step and arithmetic boundaries. It determines the values produced in their lawful
order when enumerated. The progression rule and its completed enumeration are different results where the source
exposes the rule itself. Numeric overflow or source-specific stepping follows ADR-0078 and the resolved Source
Operation; a generic sequence algorithm must not substitute its own last-member calculation.

An **Index or Axis Schema** defines the exact role and coordinate domain of each axis, any ordering or label law,
and the permitted coordinate tuples. An element index, a boundary position between elements, the logical extent and
physical storage capacity are different notions. For a two-element zero-based List, the element indexes are `0,1`
while insertion boundaries can be `0,1,2`; neither law grants a nominal Input coordinate. A **Shaped Indexed
Aggregate** binds the exact axis schema to a value correspondence over valid coordinates and declares whether the
mapping is total or partial. Rank, axis meaning and shape are preserved when the form exposes them: `0×3` and
`3×0` have no coordinate values but different shapes, while a rank-zero total scalar-shaped aggregate has one empty
coordinate and differs from an
aggregate with an empty axis. Named-axis position and coordinate label are separate observations; whether
reordering axes preserves a value is decided by the selected form's exact axis-identity and alignment law, not
physical memory layout or a universal normalization.

A **Sparse or Masked Aggregate** distinguishes an invalid coordinate, an absent mapping at a valid coordinate, an
explicit zero value, an admissible null-like value and a separately defined mask state wherever those distinctions
are supported. Physical nonstorage does not establish semantic absence or zero: a qualified dense-equivalent sparse
form can assign an implicit logical zero, whereas a partial mapping can declare the same coordinate absent. A mask
is not automatically a deletion, null or zero. A **Ragged Aggregate** requires a closed law for how a coordinate of
an earlier axis determines the lawful domain of a later axis; a two-row nested Sequence of lengths two and one is
not automatically a rectangular `2×2` value with a manufactured filler. Arbitrary callbacks do not establish an
unbounded or dependent axis law merely because they enumerate coordinates. These are formation obligations when a
specific form is claimed, not automatic V1 or V2 Tensor eligibility. ADR-0078 §14.1 retains Numeric Format, numeric
operations and its stated V2 Tensor-support boundary.

A **Graph or Hypergraph** may be formed from declared node and edge constituents and exact incidence relations.
Two edges with the same endpoints remain distinguishable when the selected graph law declares separate semantic edge
coordinates; a physical object address cannot create such a coordinate. Direction, self-loop, parallel-edge and
hyperedge-role laws must be stated by the particular graph form, not inferred from binary Membership. Cyclic graph
data does not authorize cyclic Contract Establishment dependencies. Partial orders, rooted trees and forests require
their own relation constraints, including lawful parent, root and acyclicity conditions where applicable; the
physical traversal representation does not supply those constraints.

A **Grouped Collection** declares which groups exist and the complete constituent law for each present group. An
existing group with an empty constituent differs from an absent group where the result exposes that distinction.
A **Partitioned Collection** declares its alternatives and preserves each prescribed partition, including an empty
one; no default grouping operation may remove it. A **Weighted Relation** declares the admitted weight domain and
weight meaning separately from tuple Membership and Bag multiplicity. Cost, probability, priority and signed
incremental delta are not automatically occurrence counts, and an exact weight result additionally follows its
own Numeric or other governing value law.

These forms can use the five basic formation laws without making range bounds, shape, axis labels, edge distinction
or required group presence optional metadata. Each claimed form must close its own legal domain, result
observations, partiality and completion conditions before it is used as a completed Contract-visible value. A family
name alone is insufficient qualification.

#### 3.1.4. Observation, composition and authority limits

Completed results may agree under one comparison and differ under another legal observation. Full constituent
agreement requires every distinction the selected completed form actually owns, including any relevant occurrence,
role, multiplicity, key, representative, ordering, boundary, shape or case information. This is not an additional
universal `equals` operation or an owning 1D's Sameness. Two named forms may be unified only when their complete
legal observations and the required conditions of their operations are preserved; sharing a host interface, a
physical representation or a convertible mathematical description does not establish that equivalence.

The common model keeps source discrimination, Contract Sameness, value Identity, occurrence, semantic position and
current membership separate. Encounter order, source comparison order, declared coordinate order and priority-head
selection remain different observations. Cardinality refers to what the selected completed form counts, not
platform storage capacity, number of evaluations or number of waiting operations. Absence, present null, empty
collection, deferred constituent, failed computation and a source item temporarily unavailable for consumption are
different where the selected laws distinguish them (§§3.6 and 3.9).

A completed value form is distinct from a stateful collection operation, traversal protocol or observation lifetime.
A source may mutate a destination, block or hand off an item, invoke callbacks, depend on time, expose a live view or
defer evaluation. Those behaviors remain part of the exact Source Operation under §§3.3, 3.5 and 3.9. A lawful
short-circuit calculation may complete its own result without completing a larger source collection. Such source
operations do not become new immutable Value Families merely because their carrier is called a collection.

The common family names are semantic descriptions and can be composed without creating separate host classes, HIR
nodes, serialization codes or Contract identities. The companion Design retains the Java/Kotlin operation and
producer/consumer inventories, source-version profiles and qualification evidence. Candidate inventories, including
mechanically collected public declarations, do not establish that every resolved callable is eligible or supported.

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

An exact source profile must preserve every legally relevant constituent and calculation distinction: result
composition and cardinality; applicable equality, order and selection; evaluation and permissible callback elision;
source mutation, aliases and effects; completion and abrupt failure; source observation lifetime and environmental
conditions. These obligations apply to the exact resolved callable, receiver and overload. They cannot be deduced
solely from a host type, return signature or broad family classification. Source result formation and 1D admissibility
remain separate determinations. The companion Design owns the complete operation-group inventory, exact
Java/Kotlin catalog, versioned profiles and coverage-evidence states.

Failure at an intermediate element is particularly significant for operations that mutate an existing destination.
The source law may leave earlier removals, substitutions or additions in place; another collection implementation may
leave the destination unchanged at the same failing callback. Such behavior is qualified for the exact callable and
implementation guarantee, not inferred from the common interface. The effects may also arise indirectly: a lookup can
change access order, a comparison can invoke user code, and a transformation callback can change a separate object.
The returned collection or value alone does not establish which state effects occurred. This ADR does not define the
Failure result or the subsequent rollback procedure.

A Source Operation may expose facts tied to its platform representation; such an observation must be interpreted as
that operation's result, not promoted into Contract Identity, intrinsic Family cardinality or a mandatory storage
representation. An operation that actually uses a random source is not equivalent merely because its output belongs
to an otherwise unspecified result set. Qualified randomness and its required state or invocation effects remain
source meaning; lawful deterministic refinement of *unspecified selection* may not erase them.

**For the same governing law and meaning-determining semantic inputs, a governed Contract judgment establishes one
determinate legally observable result.** A result or selection determined by the exact Java/Kotlin specification is
preserved as the author's source meaning. Where the specification instead allows a choice among several results,
**Kontrakt applies lawful deterministic refinement by default** within that permitted outcome space. A particular JDK's
usual choice, hash-table layout, physical address, traversal accident, thread schedule, or incremental update history
is not a semantic choice law.

Refinement may choose a stable representative, subset or other source-permitted outcome as appropriate to the exact
operation. Its selection must be determined by lawful semantic inputs and a qualified Kontrakt choice law; it need not
borrow an order or position from an unordered source. Where all permitted outcomes are indistinguishable to the owning
judgment, physical representative selection may vary without changing the determined Contract observation. This
judgment-invariance case simplifies realization; it is not a prerequisite to deterministic refinement. If a qualified,
legally observable deterministic outcome cannot yet be defined, the call remains unqualified for that governed use
rather than acquiring meaning from one host implementation's accident.

Refinement preserves every mandatory source observation, including required callback evaluation, completion, failure
and state effects; it also respects source-permitted elision (ADR-0073 §5.7). It may narrow an unspecified result
without
rewriting a guaranteed one. Numeric reduction laws remain owned by ADR-0078 §3.3. The obligation fixes the result and
any mandatory semantic process condition, **not** the sort, reduction tree, scheduling order, reference algorithm, or
other realization strategy. An unchanged admissible platform operation in user realization retains its platform law;
this deterministic-judgment rule does not rewrite that operation.

### 3.4. Collection Values and Observable Results

A collection calculation preserves the distinctions required by its selected source law and the legal observations of
its consuming Contract. The output of a transformation need not preserve every distinction of its input. It must
preserve the result that the transformation actually defines.

Section 3.1 defines the constituent relations available from each completed form. A transformation preserves the
observations actually defined by its result, including required position, multiplicity, membership, correspondence,
representative, shape and constituent completeness; it does not carry unrelated source distinctions into that result.
A stateful source operation may additionally have a head, transfer or time-gated eligibility observation under its
own qualified source law.

Two completed collection results may be equal under one declared relation while differing under another legally
observable ordering or selection law. Equality under a narrower relation is therefore insufficient proof of
observational equivalence.

A lawful observation may concern absence, the complete set of qualifying members, multiplicity, a selected
representative, or a derived aggregate. A newly applicable member may change the truth of an earlier uniqueness or
absence judgment without changing any member that the earlier calculation actually visited. Conversely, two
representations that differ physically can support the same legal collection observation. The selected operation and
consuming 1D determine which distinctions matter; the observation does not acquire a new identity or authority from
this ADR.

Nested results retain every constituent distinction that their owning value law admits. Shared host references,
backed views and temporary windows can affect a source computation without becoming the identity of a Contract
constituent. A complete result cannot continue to obtain its established meaning from later changes to such a source
object. Section 3.5 states the boundary at which that separation must hold.

A derived index belongs to the traversal that produced it. It does not create an Input coordinate or give an unordered
Input Membership an undeclared position (ADR-0065 §7.4). A stable sort preserves the relevant prior order among ties; it
does not create that order when the input does not own it. An unordered operation may permit different membership,
multiplicity or representatives, not just different traversal orders. Kontrakt makes those permitted choices
deterministic for governed judgments under §3.3 without treating them as a new positional order on the input.

No universal sort or tie-breaking procedure is imposed to manufacture determinism. The compiler may discard a
distinction only when no remaining legal calculation needs it. ADR-0078 §4.3 and ADR-0080 §3.7 govern that preservation
obligation.

### 3.5. Immutable Results at Contract Boundaries

Physical mutation during compilation or runtime realization is not itself a Contract property. The obligation concerns
the material lawfully established or made available under the owning boundary, not the working collection or algorithm
used to produce it.

During Contract compilation, the Frontend and Establishment may compute with mutable representations. Established
definition material and compiler-produced results retain their authoritative meaning under ADR-0063, ADR-0071 and
ADR-0075. Their consumers do not acquire an alias through which the producer's later working-state mutations can revise
that meaning.

At runtime, the external Java or Kotlin collection is a carrier of Input evidence, not the Input occurrence or a Fact.
Kontrakt-controlled acquisition may form the required stable presentation **before** the Input judgment. ADR-0064 §7.3
requires that judgment to receive an already-formed complete immutable presentation; Input cannot make an otherwise
illegal carrier lawful by copying, snapshotting, normalizing or freezing it during judgment. The formed presentation
must correspond to one lawful observation and preserve all declared constituent distinctions (ADR-0073 §5.2).

After successful Input and the subsequent legally selected judgments, Lowering forms candidate Operation-parameter
material. The required standing and movement judgments must succeed before immutable Fact authority is established
(ADR-0067 §§4, 6 and 17; ADR-0068). The original Input object does not have to be retained as the Fact's representation.
A different representation may replace it as soon as all still-required observations remain preserved; physical disposal
of the carrier does not itself terminate Input authority or establish Fact authority (ADR-0073 §5.11).

Read-only interfaces, unmodifiable wrappers, snapshot iterators and thread-safe collections are evidence only for what
their exact source laws guarantee. None alone establishes coherent, immutable Contract meaning; mutable constituent
objects or external aliases may still affect a carrier. Once a lawful presentation or Fact meaning has been established
independently of those aliases, subsequent changes to the external object cannot revise that occurrence.

A source operation may mutate an existing destination or leave earlier changes in place before failing. Those facts
remain part of its interpreted source meaning, but a failed calculation does not produce a successful Contract result.
The owning 1D determines permissible temporary computation and failure (ADR-0065 §7.2, §7.5; ADR-0073).

The completion of a collection calculation is not the establishment of every result that may eventually consume it.
An operation that promises one complete collection cannot present the elements processed before an interruption as
that completed result. Independently defined and lawfully completed results remain distinct from the intermediate
constituents of a larger result. For a protected change defined by its owning law, a successfully
calculated collection may still be pending the change's wider completion conditions. If that change is withdrawn, its
pending collection effects cannot be represented as committed collection meaning. Conversely, an unrelated result
that was independently established does not lose its authority merely because this calculation failed.

Where the owning protection law requires a previous collection observation to be preserved after withdrawal, the
relevant observation includes every distinction the selected collection law actually protects: presence and absence,
position and multiplicity, key/value correspondence, chosen representatives, and admitted nested constituents.
Restoring an equal element count is insufficient if those distinctions differ. This requirement concerns lawful
observations rather than byte-for-byte restoration or host object identity. The protection owner determines the
change's scope, participation, completion and rollback; this ADR neither supplies a general transaction nor chooses a
rollback algorithm. A source operation outside the declared protection retains its normal platform effects. External
alias and resource effects cannot be presumed restored by restoring one collection value. A protected execution must
also preserve any intermediate observation that the selected source operation requires within that execution. For
example, when a destination-mutating transformation reads the destination in a later callback, that callback may need
to observe additions made by earlier callbacks even though the encompassing change has not been committed. Deferring
all additions until completion would change the source result and could suppress a source-required failure. A
protection claim cannot treat such internal read-after-write behavior as permission to expose the pending change to an
unrelated observer. Where an external alias or callback makes both requirements impossible to support, the governing
protection and realization laws determine whether that participation is lawful; the ordinary unprotected JVM call
retains its source effects. The corresponding security and fault scenarios are developed in SADR-0002.

### 3.6. Source Null, Contract Absence, and Lookup

**Absence is the explicitly declared nonpresence of a role, coordinate or correspondence under its owning Contract
law, not a universal value that is automatically added to every value domain.** A missing optional Product role, a
valid but unmapped Association key, and a payloadless selected Choice case have distinct owners and observations.
The last is a present Choice with no payload, not the absence of that Choice. Empty Collection, whole-Input absence,
an unevaluated constituent, invalid coordinate, unresolved meaning, failure, and temporarily unavailable source item
are likewise not interchangeable with a declared constituent absence (ADR-0064 §7.6 and §7.9.4; ADR-0063; ADR-0071).

A source-language `null` is an exact Source observation, not automatically Absence and not a universal Kontrakt Null
value. Where the admitted constituent domain explicitly supports a null-like value, a **present** constituent may
have that meaning. Presence and such a value are independent distinctions: a required role may admit a present
null-like value, an optional role may permit absence but forbid a null-like value, and a declared role may support
both. Nullability or an Optional wrapper in the host type does not by itself establish any of these Contract laws.

The selected Source Operation may use `null` to express a declared Absence only when the owning 1D has admitted that
interpretation and its mapping from Source observations to the required Contract distinctions is complete and
unambiguous. For example, an exact permitted nullable Input presentation may map source `null` to a declared absent
optional coordinate when no separate present-null meaning is required. This is a boundary-specific interpretation,
not a rule that all other uses of that source `null` express Absence. If the Contract must distinguish Absent from
Present (null-like), its lawful source presentation must preserve that distinction; a lone null-like observation
cannot be assigned to both meanings or used to invent an unobserved distinction.

A Java or Kotlin Map can contain a key mapped to `null` while another key is absent, yet a selected `get` operation
may return `null` in both cases. The Map's complete correspondence retains mapping presence separately from the
value; `get` retains only the result guaranteed by its exact Source Operation. Kontrakt does not infer key presence
from that return value. If a source computation combines `get` with `containsKey`, it must also respect the actual
source-state and concurrency conditions: two separate observations do not automatically provide an atomic snapshot
(§§3.3 and 3.9). A missing Map key is not thereby a missing Input coordinate.

Source defaults and partial lookups retain their qualified behavior. A default result does not create an Input
constituent or silently modify a completed mapping. An exception does not become Admission `Rejected` or `Admitted`
(ADR-0065 §7.5). A declaration of Absence does not supply a new universal Nullable Family, enforce one physical
sentinel, or determine an IR encoding; physical representation remains realization.

### 3.7. Equality, Ordering, and Membership

No universal host equality becomes the common collection sameness law.

The selected SDK operation keeps its actual comparison semantics. Equality-based, comparator-based and
reference-identity-based source operations can discriminate constituents differently. Those distinctions must be
understood before the source result is used in a governed judgment.

The receiving 1D owns its own relation. In particular, ADR-0064 §7.9.1 owns Input Presentation Sameness, and ADR-0068
§10 owns Fact sameness. Host `equals`, `hashCode`, a comparator, or a collection constructor cannot replace either law.

An Admission-local calculation may use a different explicit comparison when ADR-0065 §7 permits it. That comparison does
not revise Input's membership or ordering law. ADR-0069 §15 separately restricts comparisons and collection operations
in Invariant source; this ADR does not broaden that surface.

A comparator's equality with `equals` is not assumed. If a relation is to serve as Contract sameness or a total
ordering, it must satisfy the properties required by the owning law on its actual legal domain. A source comparator
that violates its own required comparison properties is different from a lawful comparator whose zero-equivalence
differs from host `equals`. In the latter case the source collection may be valid, but the receiving Contract may still
reject the relation for its own constituents. Host inheritance, dynamic dispatch, or mutable comparator state cannot
supply an undeclared Contract relation.

Presence and order are independent semantic dimensions. Membership may have no observable order, encounter order or a
qualified comparison order. Navigable operations require more than a sorted iteration: bounds and neighbor selection
are part of their exact source law. Priority head selection does not make all queue elements a sorted Sequence.

### 3.8. Duplicate Handling and Representative Selection

Duplicate handling follows the selected operation. A constructor may reject duplicate keys. A conversion may retain one
representative. A sequence operation may preserve repeated occurrences. These are different results.

In an association or grouping result, the key representative, selected value, entry order and presence of a group are
separate observations. A transformation may retain an existing empty group while another omits it; a key collision
may replace the mapped value without guaranteeing the representative key object. Exact Source Qualification must
preserve these differences.

An extreme-value operation may determine the minimum or maximum comparison value without determining an observable
representative among ties. Where the selected API guarantees first-wins or another tie law, that law is preserved. A
host operation with unspecified ties receives a lawful deterministic representative refinement for governed judgments,
not an accidental first-wins guarantee from its current implementation (§3.3; ADR-0084).

In a governed calculation, an observably different representative must meet §3.3. The result cannot depend on accidental
hash layout, source traversal order that is not part of the declared meaning, thread completion, or incremental update
history. If candidates are indistinguishable under every permitted observation, their physical selection need not become
Contract meaning. Neither case specifies a representative-selection algorithm.

This does not amend Input's rule against silent deduplication or representative choice at its boundary (ADR-0064
§7.9.1).

### 3.9. Deferred and Unlawful Calculations

A recognized SDK call can still be unlawful for a 1D. Its calculation may require host object identity, an unclosed
callback, external state or an observation that the receiving Contract does not admit. ADR-0084 §3.4 governs incomplete
source interpretation. The owning 1D decides legal use and judgment failure.

A deferred operation is not a complete Contract value merely because its carrier exposes a read-only collection
interface. Completion is judged against the actual result required by its lawful consumer. A short-circuit result may
need only part of a finite traversal; a complete collection result requires the full set of its legally observable
constituents. Deferred producers can differ in evaluation, reuse and one-shot behavior. Repeated traversal is
not proof that repeated results are equal. A completed, legally reusable Contract value, retained source presentation,
and previously consumed source are different subjects: permission to recompute from the first or to validate a cached
compiler result does not permit the last to be consumed again. Likewise, rerunning an operation that changes an
existing destination may repeat effects that survived an earlier failure. Source qualification determines the actual
consumption and effect semantics; the owning execution and protection laws determine lawful retry.

The selected SDK operation owns mandatory callback observations and any permitted elision, including associated failure
reachability. A source-allowed omission must not be converted into a required invocation or exception. Callback
ordering, invocation cardinality, abrupt completion and resource lifetime are preserved where the source and owning 1D
make them obligations; result order alone does not establish execution order. Prefix calculations can expose
intermediate values, but mutable accumulator aliasing cannot be misrepresented as independently frozen source results.

A deferred constituent may be positionally or relationally present without its value having been computed.
Evaluation may also establish a source-defined failure state, and a seemingly ordinary object operation may trigger
further evaluation. Presence, unevaluated state, completed result, and stored failure must not be collapsed.
A short-circuit consumer may complete without evaluating every constituent only when that consumer's own result law
allows it; a consumer requiring the entire result must establish its required constituents. Temporary builders,
windows, entries and backed views also have source-defined validity periods that must not be enlarged by inference.
The exact source/version/Preview qualification belongs to the Design and ADR-0084.

A concurrent individual lookup does not establish a coherent snapshot of a whole collection. Weakly consistent
traversal, GC-sensitive membership and callbacks reading external state must be judged against the actual acquired
observation, not a presumed stable container. Acquisition and permissible protection of that observation remain with
the owning boundary (§3.5; ADR-0064; ADR-0073).

External live resources and capabilities do not become Closed Values because a Stream, Sequence or collection wrapper
refers to them (ADR-0073 §§5.2–5.4). The existing partial-operation, finite-completion and resource laws govern
admissibility (ADR-0065 §7.4–7.5; ADR-0073). No Collection-specific runtime policy engine, proxy verifier or fallback
execution of unknown user code is introduced.

### 3.10. Contract Pipeline and Composition

At compile time, IDL selects the applicable 1D subjects and the Frontend resolves permitted collection expressions into
their exact meaning. The Resolved Contract HIR carries sufficient meaning for Definition Establishment; Establishment
does not rerun Java/Kotlin source resolution or SDK qualification. The Canonical Contract World contains the resulting
established Contract authority, not a mutable host object graph (ADR-0071; ADR-0063).

At runtime, an Interaction enters through Kontrakt-controlled Ingress. An already-formed presentation is judged by its
selected Input Definition; the succeeding 1D judgments and explicit Lowering relation form and establish the appropriate
Fact material before user Operation invocation. Runtime Input and Fact occurrences are not compile-time Definition
material, and the source Input carrier is not itself a Fact (ADR-0064; ADR-0067; ADR-0068).

A shared collection calculation may support several 1D definitions or runtime judgments. It does not combine their
authority, select an additional stage or change their legal consumption order. Whole Machine composition relates
declared Contract surfaces. It cannot enter another Core's internal stages or share mutable collection state as Contract
authority across flows; lawful material entering another Core begins through that Core's declared Input boundary
(ADR-0055 §3.4).

Governance keeps its own authority and composition laws. Neither it nor a collection helper can retroactively change
already established meaning.

### 3.11. Compiler Realization and V1/V2 Preservation

Collection obligations are specified by semantic results and mandatory source or Contract observations, never by a JVM
collection class, algorithm, or physical scheduling policy. Different implementations may employ mutable scratch
storage, structural sharing, parallel execution or incremental maintenance where the owning law permits them.

For Contract compilation, HIR candidates, established definition material and compiler-produced results retain their
legal observations and currentness under ADR-0071, ADR-0063 and ADR-0075. A hash, HID, address or serialized
representation does not establish semantic equality or reuse validity. Internal mutation is permitted where it does not
revise authoritative meaning.

A collection value, a constituent's semantic identity, its occurrence or position in a particular result, and its
current membership are separate questions. Equal values do not merge independent Contract identities; an old position
or retained physical reference does not grant current membership. A later lawful candidate is evaluated under its
own current determinants, even if an earlier candidate used equal values or the same internal storage.

At runtime, representations used for acquisition, Input evaluation, Lowering and Fact realization may change without
preserving the original Input object, provided every still-required observation and the authority handoff remain lawful
(§3.5). For an admitted user Operation realization, an unchanged platform call retains its platform meaning. Kontrakt
transforms or fuses platform collection work only when all mandatory source observations, including guarded failure,
callback and state-effect obligations, are preserved; it need not force a callback or error the source permits it to
elide (ADR-0073 §§5.7, 9.6). A cached result or an incremental computation that produces an equal value is not, by
itself, permission to omit a mandatory state effect or replay a source operation with a non-idempotent effect. A
state-changing Operation may have no Input or outward Result; its effects are not inferred from a return value.

A reduction may be reassociated, vectorized or parallelized only when its legally observable result and mandatory
process constraints remain intact (ADR-0078 §9.2). The numerical method, partition, merge tree and execution order are
implementation choices unless some aspect is itself source- or Contract-mandated. Reproducible use of one implementation
does not replace the deterministic semantic guarantee of §3.3.

For the same established law and meaning-determining inputs, clean compilation, valid cache reuse, full recomputation
and V2 incremental maintenance must provide the same determined legally observable Contract result. A change to an
earlier member may change a first-wins representative, grouping or prefix result; the update engine must preserve the
owning semantic law, not a particular data structure. ADR-0075 owns result validity and coherent observation.
Scheduling, cache state, thread count or update history cannot choose a different Contract result.

Validity checks must account for the actual legal collection observation, including negative and complete-set
observations that can change when a new qualifying member appears. A consumer may have used a pending collection
observation through an intermediate result before its producer failed or its protected change was withdrawn. Whether
that consumer is canceled, reconsidered or accepted after a new valid proof is decided by the owning protection and
producer-result laws, not by collection identity or physical dependency reachability. When reuse is lawful, the
current result must be justified under those laws; the existence of a retained value, prior read set or equality of
output bytes is not sufficient evidence. These conditions permit full recomputation, validated reuse, incremental
repair and early cutoff without assigning a particular propagation algorithm to Contract meaning (ADR-0075).

## 4. Consequences and Deferred Work

This ADR fixes five basic completed-value formation laws, including the roles, legal empty forms, presence,
discrimination, constituent agreement and completion obligations of Ordered Occurrence, Finite Relational Extent,
Functional Correspondence, Declared Product and Closed Tagged Alternative. It also fixes the required formation and
coupling distinctions of their closed structured compositions (§3.1). These common laws do not eliminate the
independent obligations of a Sequence, Bag, Membership, Association, Range, Progression or Shaped Aggregate. The
Contract laws in §§3.1–3.11 do not make every Java/Kotlin operation legal
in every 1D, compel materialization or copying of temporary results, or require a particular storage layout,
algorithm, state engine, runtime proxy, IR node, cache scheme or incremental scheduler. Completed Contract meaning
and mandatory source observations remain independent of those implementation choices.

The **Collection SDK Interpretation and Calculation Design** owns detailed Value/Operation/Observation catalogs,
Java/Kotlin type and implementation mappings, precise public-callable and overload inventories, SDK version/Preview
profiles, qualification evidence, test fixtures and unsupported/unresolved entries. It also records the G15–G16
findings on source-representation observations, stochastic operations, temporary builders and lazy object methods.
ADR-0079 and ADR-0084 retain exact binding and interpretation authority. The Design is implementation-facing and
cannot amend this ADR or admit a new Input/Fact law.

The remaining work is to finish exact-source qualification; verify that the five basic formation laws and their
closed compositions cover each resolved Source Operation without losing its legal observations, including currently
unclassified source surfaces; complete the exact result and partiality laws of individual common Collection
calculations; verify deterministic refinement against each specified outcome set and its mandatory execution effects;
exercise partial failure, protected internal observations, views, lazy and concurrent acquisition; and compare clean,
reused, optimized and incrementally maintained results.
Security and fault-injection obligations remain in SADR-0002. ADR-0075 owns result currentness and early cutoff;
Rollback, Scope, external Transaction and Failure owners govern protection and recovery. The companion Design tracks
these work items and their evidence without turning them into new Collection Contract authority.

**Accepted ADR-0065 §10 remains a reconciliation task:** arbitrary compiler implementation preference is not a
lawful meaning determinant, whereas a *qualified, Kontrakt-owned deterministic semantic refinement* is this ADR's
default within a Source-permitted outcome space (§3.3). This Draft does not silently change the Accepted ADR.
Likewise, the existence of a common Multiset or Positional Product does not amend ADR-0064's V1 Input allow-list.
Family and Source Profile evolution must be reviewed under their actual owning laws.