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

A collection result is defined by what it makes observable, not merely by its element values. The applicable form
and Source Operation determine those observations.

| Aspect          | Distinctions that may matter                                                            |
|-----------------|-----------------------------------------------------------------------------------------|
| Constituents    | Occurrence order, index coordinates, multiplicity and membership                        |
| Relationships   | Key/value correspondence, grouping and representative selection                         |
| Structure       | Ranges, nested constituents and absence                                                 |
| Result behavior | A completed value, a destination mutation, a source-backed view or deferred computation |

These aspects are not interchangeable merely because operations share a host interface.

A specification may guarantee one result or permit several. Kontrakt must preserve a guaranteed result. Where the
Source leaves a choice open, a governed Contract judgment uses the lawful deterministic refinement of §3.3.
Observing the same choice repeatedly from one host implementation does not establish that law.

An intermediate failure may leave Source effects behind. A consumer may also have used an observation before its
producer is withdrawn. Section 3.5 addresses the collection observations that must be preserved, while the owning
protection and Failure laws determine their handling.

At runtime, an external mutable carrier cannot remain authoritative over a formed Input presentation or Fact.
At compile time, retaining a working collection does not establish authoritative Definition material.

Neither prohibiting mutable source objects nor selecting one collection implementation solves these problems. **The
common Contract law belongs here; the Java/Kotlin implementation surfaces, source-callable catalog, source
profiles and conformance evidence belong to the Collection SDK Interpretation and Calculation Design.**

## 3. Decision

### 3.1. Shared Collection Meaning and Authority

This ADR defines the meaning of completed collection and structured values; it does not create a new 1D or a shared
judgment authority. The owning 1D admits each form and determines its judgment and Establishment.

| Boundary              | Rule                                                                 |
|-----------------------|----------------------------------------------------------------------|
| Contract authority    | No new 1D Contract or independent judgment is created                |
| Runtime and API       | No Collection runtime or universal host API is required              |
| Input and Fact        | This ADR cannot establish their material; their owning 1D laws apply |
| Sameness and Identity | The owning 1D retains its own rules                                  |

In particular, Bag and Positional Product do not enlarge ADR-0064 §7.9's V1 Input presentation forms.

A family describes what a value must preserve, independently of the class or storage used to realize it. A source
operation may return a value that combines several such laws. The laws below describe **five basic forms of completed
value**, followed by closed forms and compositions that retain their own exact observations. They are not a claim that
five is a unique mathematical minimum, five public classes, five IR variants or five kinds of Contract authority.
Their use depends on the exact source meaning and the authoring law that admits it.

#### 3.1.1. Common formation and the five basic forms

A completed value has the exact schema selected by its form. Each present constituent must have an admitted,
completed meaning, and the relationships required by the form must be complete.

| Completion obligation      | What the selected form settles                             |
|----------------------------|------------------------------------------------------------|
| Constituent law            | Admitted meaning of every present constituent              |
| Structure                  | Legal empty form and required presence                     |
| Repetition and distinction | Repetitions and the applicable discrimination relation     |
| Closedness                 | Any selected case and all legally observable relationships |

Missing required material, an unresolved Source Operation, a pending producer and a failed calculation are not
completed values. Completion does not mandate retention of a particular host object or physical materialization (§§3.5
and 3.9).

The applicable use determines discrimination. There is no global equality across the following relations:

| Relation                             | Authority                     |
|--------------------------------------|-------------------------------|
| Host `equals` or a Source comparator | Exact Source Operation (§3.3) |
| Input Presentation Sameness          | ADR-0064                      |
| A later 1D comparison                | That 1D's own law             |

Full constituent agreement preserves the observations of the selected completed form. It is not a universal
collection `equals` (§§3.1.4 and 3.7). Source-defined representatives can remain observable even within one
discrimination class (§3.8). Presence and admitted null-like constituents are governed by §3.6.

The five basic forms are:

| Basic form                    | Required completed-value meaning                                                                                          |
|-------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| **Ordered Occurrence**        | A finite number of constituent occurrences, their complete linear encounter order, and the value at each occurrence       |
| **Finite Relational Extent**  | A finite relation over exact declared roles, with presence or nonnegative multiplicity for each distinguished tuple       |
| **Functional Correspondence** | A lawful key domain, its present-key domain, and one mapped value per present key; partial and total laws remain distinct |
| **Declared Product**          | A finite closed schema of named roles or fixed slots, with each role's presence and constituent-value obligations         |
| **Closed Tagged Alternative** | A finite closed case schema, exactly one case selected by each completed value, and the required case payload             |

**Ordered Occurrence.** A completed ordered value has finitely many separate occurrences. Each occurrence has
one completed constituent value and a position in one complete linear order.

| Observation           | Law                                                                                |
|-----------------------|------------------------------------------------------------------------------------|
| Relative rank         | For `n` occurrences, positions are `0..n-1`                                        |
| Duplicate values      | Equal values can occur at different positions                                      |
| Empty form            | The unique empty Sequence has zero occurrences under its selected constituent form |
| Constituent agreement | Same extent and corresponding ordered occurrences under the applicable relation    |

Thus `[A, B, A]` has three occurrences and differs from `[A, A, B]`. Converting these values to Membership may lose
order and repetition only under an explicit conversion law (§3.4.2.1). Relative rank is not a Contract Identity,
Host address or separately declared Input coordinate. A pending iterator does not establish an empty completed
Sequence. A short-circuit operation can complete its own result without consuming the entire producer (§3.9).

Encounter order does not imply random-access support. An **Indexed Sequence** additionally defines:

- The exact valid position domain.
- The correspondence between position and occurrence.
- Any Source-defined access or boundary law.

A Source-backed, reverse or live view keeps its qualified lifetime and effects (§§3.3, 3.5 and 3.9). It is not
thereby part of an immutable Sequence's semantic Identity. Ordered constituent agreement is not a mandatory
host `equals` implementation.

**Finite Relational Extent.** A completed relation has a declared closed role schema and exact arity. Its
support contains finitely many distinguishable tuples, with no intrinsic traversal order.

| Mode              | Meaning                                                             |
|-------------------|---------------------------------------------------------------------|
| Membership Extent | Presence or absence for each distinguished tuple                    |
| Bag Extent        | Exact nonnegative integer multiplicity for each distinguished tuple |

Bag multiplicity `0` means absence. Positive counts preserve repetition but do not create an order. Distinct
member count and total multiplicity remain different (§3.4.1.1). Signed incremental deltas are not negative
multiplicities in a completed Bag (§3.4.2.10).

Each declared role retains its exact constituent meaning. The schema survives an empty relation: an empty binary and
empty ternary relation are different forms. If a
zero-arity relation is admitted, its empty tuple is either present or absent. Its resemblance to a Boolean
value grants no Boolean Contract authority.

Membership or Bag discrimination must be an equivalence relation over the lawful domain: reflexive, symmetric
and transitive. A nontransitive approximate comparison cannot establish unique members.

A Source comparator can partition constituents differently from host `equals`. The exact retained
representative may also remain observable. For example, decimal values may compare equal while retaining a
Source-visible difference in scale; equality of the Membership class alone does not permit discarding that
representative (§3.8).

The owning 1D decides whether the Source discrimination is admissible (§3.7). Relational constituent agreement
requires compatible role schemas and matching distinguished support. Bag agreement also requires matching
multiplicities. Representative and additional form-specific observations remain binding. Agreement does not
merge independently identified Contract subjects or occurrences.

**Functional Correspondence.** A correspondence defines mappings over an exact lawful key domain.

| Obligation     | Law                                                       |
|----------------|-----------------------------------------------------------|
| Discrimination | Qualified key relation                                    |
| Presence       | Exact subset of keys with present mappings                |
| Functionality  | Exactly one completed value per distinguished present key |
| Partiality     | Missing mappings may be legal                             |
| Totality       | Every lawful key must have a mapping                      |

An admissible absent key is not invalid, and a present null-like value is not an absent mapping (§3.6).
Two different mapped values cannot coexist for one distinguished key. Repeated Source keys follow the exact
construction law; reject, retain, replace and aggregate are not family defaults (§§3.3 and 3.8).

A completed finite Association also admits a binary-relational description with key and value roles, provided
the relation satisfies functional uniqueness. Both descriptions must agree on:

- Exact key discrimination and present-key domain.
- Mapped values and all legally observable representatives.

This overlap creates no second Mapping authority.

A qualified total function may also have a non-enumerated or unbounded key domain. That requires a closed law
that completes the mapped meaning and lawful observations for every key. An arbitrary callback, unresolved
predicate or host storage default cannot replace it. A mathematically described function need not support
arbitrary decidable equality or full-domain observation; such results need their own exact completion.
Agreement between correspondences also requires compatible key domains, matching presence and the additional
form-specific totality observations. Physical entry order is irrelevant unless declared.

**Declared Product.** A Product has a finite, closed schema of roles. Each role owns a value domain and a
presence obligation.

| Role kind | Completion condition                                         |
|-----------|--------------------------------------------------------------|
| Required  | One present completed constituent                            |
| Optional  | Either explicit absence or one present completed constituent |

Presence is independent of nullability. A required role can admit a present null-like value; an optional role
may admit absence without permitting that value. Both possibilities are legal together only if the value domain
admits the null-like constituent (§3.6). Completion settles every role's presence and, when present, its value.

A **Named Product** distinguishes roles by exact declared names or nominal coordinates. Declaration order does
not automatically form an encounter order.

A **Positional Product** uses fixed declared slots. Slots can have different value domains; they are not
occurrences of a general Sequence. Matching field spellings and types do not establish nominal schema equality.
Structural correspondence requires a separate explicit law.

The closed zero-role schema has one empty Product when its 1D admits that form. It is not an empty Sequence or
Association. Cross-role conditions require an explicit law of the closed form or the owning Contract, not an
arbitrary executable predicate within the basic Product. ADR-0079 owns Source Type binding; neither Java records
nor Kotlin data classes automatically determine Contract constituents.

Product agreement requires compatible exact schemas with corresponding presence and value meanings. It is
not inferred from generated Source `equals`.

**Closed Tagged Alternative.** A Choice has a finite, explicitly closed case schema. Each completed value
selects exactly one case.

| Case property      | Law                                              |
|--------------------|--------------------------------------------------|
| Payloadless case   | No payload is present                            |
| Payload case       | One completed value of the exact admitted domain |
| Structured payload | May itself be a Declared Product                 |
| Unselected case    | Contributes no payload                           |

A payloadless case is different from a present payload containing an admitted null-like value (§3.6). A schema
with no cases has no completed Choice; it is not a valid empty instance.

Case Identity comes from the exact declaration, never from a Source enum ordinal, physical address, inheritance
or matching spelling. Choice agreement requires compatible schemas, the same case and the corresponding
payload meaning. An open subtype search cannot introduce a case. Qualified `Optional`, `Result` or enum-like
Sources can be interpreted, but a Choice grants no separate 1D Absence, Failure, Identity or judgment authority.

The five forms are laws for completed values, not public classes or new Contract authorities. Their IR and storage
representations remain implementation choices. A shared representation or lawful conversion does not prove equal
meaning. Conversion losses and newly formed observations follow §3.4.2.

#### 3.1.2. Closed value forms and compositional obligations

The five basic forms do not replace the closed forms an author may use. Each completed form below must satisfy its
own schema and observable result law, even when it reuses a basic form. A composition must also establish the exact
relationship between its constituents. Merely placing two completed forms side by side does not complete their
coupling. The conversion rules are in §3.4.2.

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

An Indexed Sequence can derive relative position from encounter order. Any additional Source-defined index unit,
allowed index, boundary or access rule still applies.

Element indexes and inter-element boundaries differ; neither creates an Input nominal coordinate, Text scalar
position or physical Dense Ordinal (§3.1.1). Indexed Membership also distinguishes present indexes from the
entire valid index domain.

Java `BitSet` illustrates three distinct observations:

| Method        | Meaning                      |
|---------------|------------------------------|
| `cardinality` | Present-member count         |
| `length`      | Index-related extent         |
| `size`        | Storage-capacity observation |

Their exact Source results do not establish a universal Membership cardinality (§3.4.1.1).

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

A composed result is complete only when its constituent forms and the coupling in the tables are complete. In
particular, Sequenced Membership must agree with its Membership about which distinct members exist. The specific
rules for absent versus empty groups and partitions are in §3.4.1.9; borrowed windows and prefix aliases are
covered in §§3.3, 3.4.1.11 and 3.9. The exact selected Source Operation determines whether those results form (§3.3).

#### 3.1.3. Interval, progression, coordinate and shaped-value laws

There is no generic `Domain` or `Coordinate Region` Family that can accept an arbitrary predicate and confer Contract
meaning on it. An ordinary value's admitted domain follows its owning Type, Numeric, Text and 1D laws. A range,
progression or shaped result instead supplies a **specific completed meaning** with the following obligations.

An **Ordered Interval** is complete under its declared range law. It requires:

| Obligation             | Meaning                                                          |
|------------------------|------------------------------------------------------------------|
| Carrier and comparison | Exact ordered carrier and lawful endpoint/member comparisons     |
| Bounds                 | Each present lower or upper bound and its inclusion or exclusion |
| Result                 | Exact empty and membership conditions                            |

Bounds and members are distinct observations where the selected form exposes both. For example, `[1,5]` and
`(0,6)` contain the same integers but have different declared endpoints. Conversely, syntactically different
expressions are not kept distinct when they resolve to the same exact range value. Enumeration or host `equals`
alone does not determine every Source-guaranteed Range observation. A lawful Interval may be complete without
enumerating an unbounded or continuous carrier; it does not establish a completed infinite Sequence.

A **Discrete Progression** defines the generation rule for an ordered sequence of values. The rule covers its
start, step and direction, as well as stopping and last-member behavior. It also defines when the progression is
empty, when a step is valid and which arithmetic boundaries apply.

The rule and its completed enumeration are different results if the Source exposes both. Overflow and
Source-specific stepping follow ADR-0078 and the resolved Source Operation; a generic sequence algorithm cannot
replace its last-member calculation.

An **Index or Axis Schema** establishes the exact axes of a shaped result.

| Aspect            | Required meaning                              |
|-------------------|-----------------------------------------------|
| Axis              | Exact role and coordinate domain of each axis |
| Axis observations | Any declared order or label law               |
| Domain            | Permitted coordinate tuples                   |

An element index differs from an inter-element boundary, logical extent and storage capacity. In a two-element
zero-based List, indexes are `0,1` but insertion boundaries can be `0,1,2`; neither grants an Input nominal
coordinate.

A **Shaped Indexed Aggregate** attaches values to valid coordinates under a total or partial correspondence.
Its rank, axis meaning and shape remain observable when declared. Thus `0×3` and `3×0` have no coordinate values
but different shapes. A rank-zero total scalar has one empty coordinate, unlike a shape with an empty axis.
Named-axis position and coordinate label also differ. Axis reordering is equivalent only under the selected
axis-identity and alignment law, not a universal memory-layout normalization.

A **Sparse or Masked Aggregate** preserves whichever of the following states its form admits:

| State                   | Meaning                                                  |
|-------------------------|----------------------------------------------------------|
| Invalid coordinate      | Outside the lawful domain                                |
| Absent mapping          | Valid coordinate without a present mapping               |
| Explicit zero           | Present constituent whose value is zero                  |
| Implicit logical zero   | Zero supplied by a qualified dense-equivalent sparse law |
| Present null-like value | Admitted present constituent, not absence                |
| Mask state              | Independently declared masking observation               |

Physical nonstorage proves neither absence nor zero, and a mask is not automatically deletion, null or zero.

A **Ragged Aggregate** requires a closed rule relating an earlier coordinate to the lawful domain of a later axis.
Two nested rows of lengths two and one do not form a rectangular `2×2` value with invented padding. Merely
enumerating coordinates through an arbitrary callback does not establish an unbounded or dependent axis law.
These obligations do not grant automatic Tensor eligibility in V1 or V2. ADR-0078 §14.1 retains the Numeric Format,
Numeric Operation and V2 Tensor-support boundaries.

A **Graph or Hypergraph** may use declared Node and Edge constituents with exact Incidence relations. Edge
coordinates are semantic only if the selected graph law declares them; two edges with the same endpoints may
remain distinct without relying on physical addresses.

| Form                         | Further obligations                                |
|------------------------------|----------------------------------------------------|
| Directed or undirected Graph | Exact direction and self-loop law                  |
| Parallel-edge Graph          | Exact law for distinct edges sharing endpoints     |
| Hypergraph                   | Exact incidence-role law                           |
| Partial order                | Its own order constraints                          |
| Rooted tree or forest        | Applicable parent, root and acyclicity constraints |

These conditions cannot be inferred from binary Membership or physical traversal. Cyclic graph data does not
authorize cyclic Contract Establishment dependencies.

A **Grouped Collection** declares the existing groups and each group's completed constituent law. An empty present
group differs from an absent group when that distinction is observed (§3.4.1.9).

A **Partitioned Collection** preserves every required alternative, including an empty one. Ordinary grouping
does not remove a declared alternative (§3.4.1.9).

A **Weighted Relation** declares its admitted weight domain separately from tuple Membership and Bag multiplicity.
Weights may represent different quantities under different laws:

| Example meaning          | Not automatically equivalent to |
|--------------------------|---------------------------------|
| Cost or probability      | Occurrence count                |
| Priority                 | Occurrence count                |
| Signed incremental delta | Completed Bag multiplicity      |

Each weight result also follows its governing Numeric or other value law.

These forms can use the five basic formation laws without making range bounds, shape, axis labels, edge distinction
or required group presence optional metadata. Each claimed form must close its own legal domain, result
observations, partiality and completion conditions before it is used as a completed Contract-visible value. A family
name alone is insufficient qualification.

#### 3.1.4. Observation, composition and authority limits

Two completed results may agree under one comparison but differ under another lawful observation. Full constituent
agreement covers everything the selected form makes observable (§§3.1.1–3.1.3). This is not universal `equals`
or 1D Sameness. A shared host interface, storage representation or mathematical conversion does not establish
full equivalence; the required Source Operation conditions must also be preserved (§3.3).

The common model keeps Source discrimination distinct from Contract Sameness (§3.7). It also distinguishes value
Identity from an occurrence, semantic position and current Membership. Encounter order and comparison order do
not automatically determine one another; nor does a priority head create encounter order.

The selected form fixes what cardinality counts (§3.4.1.1). Section 3.6 distinguishes Absence from null and empty;
§3.9 covers pending or failed computations and temporarily unavailable Source items.

A completed value is not the same subject as a stateful or deferred Source Operation. The exact Source profile
owns its effects and lifetime (§3.3), while §3.9 governs lawful completion. A short-circuit calculation can
complete its own result without completing the larger producer. These operations do not become new completed
Value Families merely because their carrier is called a collection.

The family names describe meaning rather than public host classes, IR encodings or Contract identities.
Serialization codes likewise do not create a Contract authority. The
companion Design owns the SDK inventories and qualification evidence (§3.3). An inventoried candidate is not
thereby a supported or 1D-eligible callable.

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

Source operators retain their exact meaning:

| Source expression or form                     | Contract implication                                              |
|-----------------------------------------------|-------------------------------------------------------------------|
| Java reference `==`                           | Cannot be silently rewritten as value equality                    |
| Kotlin `==`                                   | Calling `equals` does not automatically make it Contract equality |
| Comparison requiring Host reference Identity  | Not a lawful Contract judgment                                    |
| Null comparison                               | May express lawful presence testing                               |
| Closed nominal case, such as an enum constant | May be interpreted as a declared case when Source meaning permits |

ADR-0079 and the owning 1D determine whether each exact comparison is admissible.

An object-identity dependency may be eliminated only when the applicable source and 1D laws justify the resulting
calculation. Removing a dependency is not permission to introduce object identity into Contract meaning.

User Operation realization is separate. It may use the JVM object model subject to ADR-0073 and the existing realization
laws. This section does not rewrite the semantics of an unchanged, already admissible platform call.

### 3.3. Exact Java and Kotlin Collection Operations

Kontrakt interprets the selected source callable before assigning collection calculation meaning. ADR-0079 owns type
interpretation, overload resolution, generics, receivers, and language interoperability. ADR-0084 owns qualification of
the selected standard-library operation.

An exact Source profile must preserve the result and the mandatory observations of the resolved callable. The
qualification responsibilities are:

| Subject              | Exact meaning required                                   |
|----------------------|----------------------------------------------------------|
| Result               | Composition, cardinality and selected constituents       |
| Comparison or choice | Applicable equality, order and selection law             |
| Evaluation           | Callback execution and any Source-permitted elision      |
| Effects              | Mutation, alias relations and other required effects     |
| Completion           | Normal and abrupt outcomes                               |
| Lifetime             | Source observation lifetime and environmental conditions |

These meanings depend on the exact receiver and overload. Host type, return signature and broad Family name are
insufficient. Qualification distinguishes required observations from Source-permitted choices, while the owning
1D separately determines admissibility. The companion Design retains:

- The Value/Operation/Observation and producer/consumer catalogs.
- Exact versioned callable and overload profiles, choice-law evidence and coverage states.

Source conversion methods must be qualified by their actual ownership and effect laws, not their result types.
The main distinctions include:

| Source form          | Example                                     | Qualification concern                              |
|----------------------|---------------------------------------------|----------------------------------------------------|
| Independent result   | Java `List.copyOf`                          | Result formation and retained mutable constituents |
| Backed view          | Java `List.subList`                         | Origin dependence and observation lifetime         |
| Wrapper              | Java `Collections.unmodifiableList`         | Restriction of writes without deep immutability    |
| Destination mutation | Kotlin `associateByTo` versus `associateBy` | Existing destination and failure effects           |

An unmodifiable outer collection does not make mutable `Map.Entry` or nested constituents independent.
Translation into a Kontrakt-owned calculation is legal only when the exact result and mandatory Source
observations agree (§§3.4.2 and 3.11). These distinctions do not create additional Value Families.

An intermediate failure can leave a destination partly changed. Whether earlier changes remain depends on the
exact Source Operation and its guaranteed behavior; a shared host interface cannot settle it.

Effects may also occur without a destination-mutating method:

| Source action           | Potential effect to qualify |
|-------------------------|-----------------------------|
| Lookup                  | Changing access order       |
| Comparison              | Invoking user code          |
| Transformation callback | Changing another object     |

The returned value alone does not establish these effects. This ADR does not decide Failure formation or rollback.

A Source Operation may make a platform observation part of its result, but that observation cannot become
Contract Identity or a universal storage requirement. It cannot become intrinsic Family cardinality. The operation's
exact meaning governs it.

Randomness must be distinguished from an unspecified choice. An operation that consumes a random source retains
its qualified randomness, state consumption and invocation effects. Java `Stream.findAny()` instead permits a
member choice without requiring a random generator. A lawful deterministic selection does not turn `findAny()`
into `findFirst()`.

**The same governing law and meaning-determining semantic inputs yield one determined legally observable Contract
judgment.** Source-guaranteed results retain their exact meaning. Where the Source permits several outcomes,
Kontrakt applies a qualified lawful deterministic refinement by default.

| Not an admissible choice law            | Reason                                       |
|-----------------------------------------|----------------------------------------------|
| One JDK's usual choice                  | Not the Source guarantee                     |
| Hash-table layout or physical address   | Physical representation, not semantic input  |
| Accidental traversal or thread schedule | Not a lawful determinant                     |
| Incremental update history              | Reuse path cannot change established meaning |

Refinement selects only an outcome allowed by the exact Source Operation. A qualified choice law uses lawful
semantic inputs; it need not borrow an order from an unordered Source. ADR-0084 owns its qualification. That
choice law does not become a new Java/Kotlin specification guarantee.

If the selection affects a 1D judgment, the 1D's complete Judgment Meaning retains the qualified law and its
determinants. Qualification does not itself grant 1D eligibility. If every permitted outcome is indistinguishable
to that judgment, physical representative selection may vary without changing the Contract observation.
Otherwise, without either a lawful deterministic choice or judgment-invariance, the operation is unsupported
for that governed use.

Refinement preserves the selected Source's mandatory observations (ADR-0073 §5.7), including:

- Callback execution and any permitted elision.
- Required state effects.
- Exact normal or abrupt completion, including Source-authored exception handling.

A guaranteed exception cannot be removed merely to prefer a Collection result. ADR-0057 owns machine Failure
meaning and Establishment; this ADR does not decide them. Numeric reductions follow ADR-0078 §3.3.

These are semantic obligations, not mandated sorting, reduction trees or scheduling. An unchanged admissible
platform call in user realization retains its platform law.

### 3.4. Collection Values and Observable Results

A collection calculation preserves the distinctions required by its selected source law and the legal observations of
its consuming Contract. The output of a transformation need not preserve every distinction of its input. It must
preserve the result that the transformation actually defines.

Section 3.1 defines the completed forms and their constituent relations. A conversion must preserve the
observations its **result** actually requires; it need not retain unrelated observations of the source (§3.4.2).
A stateful operation may also expose a head, transfer or time-gated eligibility under its exact Source profile (§3.3).

Two completed collection results may be equal under one declared relation while differing under another legally
observable ordering or selection law. Equality under a narrower relation is therefore insufficient proof of
observational equivalence.

A lawful negative or complete-set observation can change when a new qualifying member appears, even if no
previously visited member changes. The selected operation and owning 1D determine which observations matter;
those observations gain no independent Identity or authority (§§3.1, 3.11).

Nested results must preserve the constituent meaning required by §3.1. Host aliases and temporary views may affect
Source execution without defining Contract constituent Identity (§3.3). A completed result cannot rely on later
changes to the Source object; the boundary is established in §3.5.

A derived traversal index does not create an Input coordinate or order an unordered Input Membership (ADR-0065
§7.4). A stable sort preserves an existing prior order among ties; it cannot invent that order. Other qualified
choices follow §3.3 and do not make a new positional law on the original input.

No universal sort or tie-breaking procedure is imposed to manufacture determinism. The compiler may discard a
distinction only when no remaining legal calculation needs it. ADR-0078 §4.3 and ADR-0080 §3.7 govern that preservation
obligation.

### 3.4.1. Common Collection Calculations and Their Results

The common calculations below observe or form the completed values defined in §3.1. Their result laws are part of
collection meaning, while exact Java/Kotlin callable selection and any required evaluation effects remain with §3.3
and ADR-0079/0084. Each calculation is available only for a value whose selected form supplies its required
constituents and relations. A calculation may have a lawful result without being available to every 1D; the owning
1D decides its permitted use. Conversely, the absence of a particular host method or compiler implementation does
not remove a lawful common calculation.

The names below distinguish results, not required public methods, HIR instructions or runtime algorithms. One
implementation may share work between several calculations, and another may realize them separately. Where a result
can be determined from another completed observation, that relationship is binding, but it does not require one
calculation to call the other. A source method with additional guaranteed behavior is not interchangeable merely
because its returned value agrees. Sections 3.3 and 3.9 govern those source conditions.

#### 3.4.1.1. Cardinality, Count and Empty (R02-1)

The term **count** refers to the constituents named by the calculation, not a generic `size`.

| Calculation               | Counted subject                                                   | Example                      |
|---------------------------|-------------------------------------------------------------------|------------------------------|
| Occurrence count          | Every occurrence of an Ordered Occurrence, including equal values | `[A,A,B]` has `3`            |
| Member count              | Distinct present Membership members under its discrimination      | —                            |
| Tuple count               | Present distinguished tuples of a Membership Relation             | Declared role schema applies |
| Bag total multiplicity    | Each tuple weighted by its nonnegative multiplicity               | `{A×2, B×1}` gives `3`       |
| Bag distinct member count | Present distinguished tuples counted once                         | `{A×2,B×1}` gives `2`        |

These remain distinct calculations even when their results coincide.

**Mapping count** of a completed finite Association counts its present distinguished keys, equivalently its current
mappings. Distinct keys mapped to equal values remain separate mappings. No finite mapping count is implied for a
qualified total correspondence over a domain that is not established to have a finite, countable extent. A lawfully
completed value may therefore support direct value access without supplying this count.

**Empty** tests whether the selected completed form has no constituent of the kind it observes.

| Completed form     | Empty when          |
|--------------------|---------------------|
| Ordered Sequence   | No occurrences      |
| Membership or Bag  | No present members  |
| Finite Association | No present mappings |

Where the corresponding exact finite count exists, Empty is true exactly when that count is zero. It remains
an independent observation: deciding it need not calculate the full count. An unevaluated producer does not
establish a completed empty value (§3.9).

Not every quantity exposed as `size` is the same count.

| Form               | Distinctions                                                                               |
|--------------------|--------------------------------------------------------------------------------------------|
| Product            | Declared roles versus present optional roles                                               |
| Choice             | Selected case is not a general collection cardinality over its cases                       |
| Indexed Membership | Member count versus index extent                                                           |
| Shaped result      | Valid coordinates versus present mappings; rank-zero total scalar has one empty coordinate |

The empty shapes `0×3` and `3×0` have no value coordinates but remain different (§3.1.3). Each source
operation must declare its counted subject instead of inferring it from a generic `size`.

**Matching-occurrence count** counts occurrences satisfying the selected lawful condition. It is not a
Membership's distinct-member count. For example:

| Calculation                            | Result on the given value                      |
|----------------------------------------|------------------------------------------------|
| Occurrences matching `A` in `[A,A,B]`  | `2`                                            |
| Presence of `A` in Membership `{A, B}` | `true`                                         |
| Count of `A` in a Bag                  | The member's multiplicity, not the Bag's total |

Source callbacks retain their exact evaluation conditions (§3.3). No particular traversal algorithm is required.

A completed count denotes the exact nonnegative quantity specified by the calculation. Formation of a numeric
result, its admitted format and representability follow ADR-0078. A source method that saturates, overflows or
reports capacity according to its own specification is qualified as that source calculation, not automatically as
exact common cardinality. Java `BitSet.cardinality`, `length` and `size` illustrate respectively a member count, an
index-related extent and a storage-capacity observation; their distinct source results remain governed by §3.3.

#### 3.4.1.2. Member Presence and Matching Existence (R02-2)

**Member Presence** answers whether a lawful candidate belongs to a completed Membership under that Membership's
selected discrimination relation (§3.7). For a Bag it answers whether the candidate's multiplicity is positive;
it does not report that multiplicity. For a finite Relation it tests a tuple formed under the declared role schema.
A candidate with the wrong role schema or outside the lawful comparison domain does not become an absent member
merely because the calculation cannot compare it.

**Matching Existence** on an Ordered Occurrence answers whether at least one of its occurrences matches the candidate
under the comparison law selected for that calculation. Its result is Boolean when that law determines the required
comparisons. `[A, B, A]` contains two matching occurrences of `A`, but Matching Existence returns true once; it does
not select a representative or remove duplicate occurrences. A Sequence's encounter order alone does not establish
Membership's discrimination relation.

A presence-related calculation must state both the subjects it compares and its comparison law.

| Calculation                 | Subject of the test                                                  |
|-----------------------------|----------------------------------------------------------------------|
| All requested members       | Each requested candidate under the applicable relation               |
| Disjointness                | Two completed extents and their lawful discrimination                |
| Association value matching  | Present mapped values, not key presence                              |
| Interval membership         | Declared bounds and ordering, without necessary enumeration (§3.1.3) |
| Indexed Membership presence | A lawful index, not a storage-capacity bound                         |

`containsAll` and `disjoint` cannot be replaced by repeated host `contains` calls unless the exact Source
comparison and evaluation behavior agrees (§3.3).

A selected condition over occurrences or tuples may support Exists, All or None results. The condition's domain and
result interpretation must be established for that exact calculation. In particular, SQL's three-valued `IN` or
`WHERE` and a source mask's null handling do not create a universal Collection Boolean or Absence conversion.
A complete false existence result requires sufficient lawful knowledge that no qualifying member exists; an early
true result may be complete when the selected operation permits short-circuiting (§3.9).

#### 3.4.1.3. Association Presence and Lookup (R02-3)

**Key Presence** reports whether a lawful key has a present mapping in a completed Functional Correspondence. **Mapped
Value Access** returns the value associated with a present key. A lawful but unmapped key supplies no
successful mapped value for direct access; it does not manufacture a default constituent. An invalid key is not
simply a missing mapping. A present mapping whose value admits a null-like meaning remains present (§3.6).

A selected **absence-reporting lookup** may form a completed result that distinguishes a found mapping and its value
from a missing mapping. Such a result is governed by its exact declared result form, which may use a Closed Tagged
Alternative; it does not create a universal Optional or Null Family. **Default lookup** may instead choose a
specified fallback value when no mapping is present, without inserting that value into the correspondence. The
same correspondence must yield compatible Key Presence, direct access and absence-reporting results for the same
lawful key and completed observation. These calculations remain separately expressible; the relation does not
require a particular combined lookup implementation.

**Key, value and entry selection** are different observations. Selecting the stored key representative requires
the representative law already owned by §3.8; selecting a mapped value does not silently determine that key object.
`Map.get(k)` returning source `null` cannot by itself prove whether the mapping is absent or maps to an admissible
null-like value. A source implementation combining `get` and `containsKey` must satisfy its actual observation and
concurrency conditions before claiming a coherent result (§§3.3, 3.6 and 3.9).

#### 3.4.1.4. Index, Boundary and Selection (R02-4)

**Positional access** selects a constituent under an Indexed Sequence's declared position-to-occurrence relation.
An Ordered Occurrence without lawful indexed access does not acquire that operation by virtue of its linear order.
The exact index domain and the result for a position outside it follow the selected calculation. Indexes of an
Indexed Membership or coordinates of a Shaped Aggregate use their own declared domains (§§3.1.2–3.1.3).

A **boundary** is a position between occurrences, not an additional element. In a four-occurrence zero-based
Indexed Sequence, element indexes are `0..3` while an insertion operation can admit boundaries `0..4`.
Boundary `4` follows the final occurrence; it is not an element or a physical tail address.

An empty completed Sequence can have insertion boundary `0` without having a last element. The implementation
may represent boundaries using length, a cursor, a Node reference or another structure. This does not affect
the semantic boundary law.

**Contiguous selection** uses exact bounds and endpoint inclusion. **Indexed gather** selects declared indexes,
including repetitions when the selected operation permits them. The selected Source indexes `2,0,2` repeat a position.

| Calculation on `[A,B,C,D]` | Result                            |
|----------------------------|-----------------------------------|
| Half-open `[1,3)`          | `[B,C]`                           |
| Half-open `[2,2)`          | Empty result in the selected form |
| Gather `[2,0,2]`           | `[C,A,C]`                         |

`Take`, `Drop`, strided selection and filled ranges each retain their Source-defined rules. An end beyond the
input may fail, truncate or form prescribed filler values. No universal Slice rule follows from its name.

**First and last selection** use the selected ordered value's actual order and are applicable only where the
relevant result exists; they do not define a last constituent for an empty Sequence. **Navigable selection** uses
the qualified comparison relation and its strict or inclusive neighbor rule. It is not positional selection under
encounter order. An operation returning an index, insertion-point encoding, selected value, matching extent or
representative states that result explicitly; Java `binarySearch`'s encoded absent result is not a generic Index
law. Where the source returns a backed view, its origin relationship and observation lifetime remain with §§3.3
and 3.9 rather than becoming an immutable slice by assumption.

#### 3.4.1.5. Construction and Composition (R02-5)

**Ordered concatenation** forms a new ordered value by taking every occurrence of the left input in order and then
every occurrence of the right input in order. Neither equal values nor repeated occurrences are discarded:
`[A,B]` concatenated with `[B,C]` produces `[A,B,B,C]`. Append and prepend place new occurrences at the declared
end or beginning of the result without requiring a boundary object in the implementation. A construction from an
unordered source cannot claim a determined encounter order unless the selected source or lawful choice rule
supplies it (§3.3).

**Positional composition**, including Zip, declares how occurrences correspond. A shortest-input Zip of
`[A,B,C]` and `[1,2]` produces `[(A,1),(B,2)]`.

| Alternative               | Additional law                    |
|---------------------------|-----------------------------------|
| Require equal lengths     | Exact treatment of unequal inputs |
| Add fill elements         | Filler and result extent          |
| Use a different pair form | Exact result schema               |

A resulting pair may be a Declared Product, but relative positions do not create Input nominal coordinates.

**Flatten** follows the selected outer and inner constituent laws. Under ordered concatenation, `[[A,B],[C]]`
produces `[A,B,C]`. It does not authorize arbitrary recursion or removal of Product, Choice, Graph or Shape
schema obligations.

**Membership, Bag and Association construction** use their selected completed-form laws (§§3.1 and 3.8).

| Construction   | Possible information change                                                           |
|----------------|---------------------------------------------------------------------------------------|
| Set/Membership | Repeated Source occurrences can be removed                                            |
| Bag            | Source multiplicities are retained as prescribed                                      |
| Association    | Repeated keys follow exact key-collision and Representative law; no default last-wins |

A Graph, Grouped result or Shaped Aggregate additionally requires its cross-form relationships to be complete.
Union, Intersection, Difference and Join are separate operations (§3.4.1.7). The term `composition` selects
none of them.

#### 3.4.1.6. Value Transformation and Source Mutation (R02-6)

A **completed-value update** forms a new completed value. The selected change law determines what is replaced,
inserted or removed; it does not mutate the original completed value.

| Update category      | Possible selected meanings                                                 |
|----------------------|----------------------------------------------------------------------------|
| Ordered position     | Replace a position or insert an occurrence under the declared boundary law |
| Matching occurrences | Remove one match or every matching occurrence                              |
| Membership           | Exclude a distinct member under its discrimination                         |
| Association          | Replace a mapping under its key and collision laws                         |

For example, removing the first matching `A` from `[A,B,A]` produces `[B,A]`; removing all matches produces
`[B]`. These are different result laws.

A **mutable Source Operation** may modify its destination and return a separately specified result. The Source
can return an old element, a Boolean, a previous mapping, an updated collection or another prescribed result.

Java `List.remove(int)` and `List.remove(Object)` differ in target selection and returned meaning. Similarly,
`Map.put` returning `null` need not distinguish a missing previous key from a present-null mapping.

Source-prescribed intermediate observations and partial changes remain governed by §3.3. Failed mutations need
not be atomic or automatically rolled back. Their protection and Failure handling belong to the respective
owners (§3.5).

#### 3.4.1.7. Set, Bag and Relation Operations (R02-7)

On finite Membership values with compatible role schemas and an applicable common discrimination law, **Set Union**
contains each distinguished member present in either input, **Set Intersection** contains each member present in
both, and **Set Difference** contains each member present in the left input but absent from the right. Each result
is a completed Membership under the selected law. When two source collections use incompatible discrimination
relations, a common result must first establish an explicit lawful comparison; the name `union` or `difference`
does not silently choose one. Source-guaranteed order or retained representatives remain additional observations
where applicable (§§3.3 and 3.8).

For a finite Bag, the following are **distinct calculations** on the multiplicity of each distinguished member:

| Bag calculation        | Multiplicity in the result when left has `3` and right has `2`                                             |
|------------------------|------------------------------------------------------------------------------------------------------------|
| Additive Union         | `5`, the sum of the two multiplicities                                                                     |
| Maximum Union          | `3`, the greater multiplicity                                                                              |
| Minimum Intersection   | `2`, the lesser multiplicity                                                                               |
| Subtractive Difference | `1`, the positive remainder after subtracting the right multiplicity, or zero if the remainder is negative |
| Remove-all Difference  | `0`, because the right Bag contains that member                                                            |

The same rules apply independently to each distinguished member. The calculation names express different results;
a generic `Bag Union` or `Bag Difference` does not select among them. Completed Bag multiplicities remain
nonnegative (§3.1.1). A signed delta used by an incremental implementation is not a completed Bag constituent.

**Relation Union, Intersection and Difference** require compatible exact role schemas. The selected result mode
determines whether presence or Bag multiplicity is computed. Even an empty result retains its role schema.

**Relation Join** declares the matching roles or condition and the resulting role schema. Its output mode
specifies presence or multiplicity. In a Bag equijoin, each matched tuple pair contributes the product of its
input multiplicities. Contributions to the same output tuple are added. Thus `(x,1)×2` joined with `(1,y)×3`
on the common role produces `(x,1,y)×6`. A Membership result records only the presence of matching tuples.

An inner join, outer join, semijoin and antijoin do not have one interchangeable result law. Each must specify what
happens to unmatched tuples and which roles are present in the result. In particular, outer-join fill is not an
implicit Contract Absence or Null conversion (§3.6). Weighted Relation combination follows the selected weight
law; a numeric weight cannot automatically be treated as Bag multiplicity (§3.1.3). Hash, merge and nested-loop
join are possible implementation methods, not Contract results.

#### 3.4.1.8. Map, Projection and Filter (R02-8)

**Element-wise Map** on Ordered Occurrences forms one result occurrence per input occurrence, preserving their
correspondence and order when that is the selected law. Equal output values do not merge occurrences:
`["A","BB","C"]` mapped to string lengths becomes `[1,2,1]`.

Zero-or-many-output mapping has a different result law and may require Flatten or Composition (§3.4.1.5).
Required callback evaluation, permitted elision and failures remain Source-specific (§3.3).

**Projection** specifies which roles or constituents it retains and the result form. An ordered projection preserves
selected occurrence order. A Bag Projection combines the multiplicities of source tuples that become
indistinguishable in the projected result; projecting `{(x,1)×1,(x,2)×1}` onto the first role yields `{x×2}`.
A Membership Projection retains `x` once under its applicable discrimination. The selected projection fixes the
result role schema; it does not silently identify nominally distinct roles. Projection of a Weighted Relation
combines weights only as allowed by its declared weight law, not by Bag addition by default.

An Association's **Key Projection** observes its present distinguished keys. **Value Projection** observes the
mapped value of every present entry and must preserve repeated values across different keys whenever the selected
result form exposes their occurrence or multiplicity. **Entry Projection** retains each key/value correspondence.
For `{K1→X,K2→X}`, Value Projection as a Bag produces `{X×2}`, not `{X×1}`. **Re-keying** forms new keys and must
apply the selected collision and representative laws (§3.8); Kotlin `mapKeys`'s later-value behavior is an exact
Source Operation rule, not a universal Association rule. A mapped null-like value remains a present value (§3.6).

**Filter** selects constituents under the result form's law. The selected operation must identify the unit
being retained:

| Input form       | Possible retained unit                         |
|------------------|------------------------------------------------|
| Ordered result   | Occurrence                                     |
| Relation         | Distinguished tuple                            |
| Association      | Entry, with its key/value correspondence       |
| Declared Product | Declared role, where such a result is admitted |
| Shaped value     | Valid coordinate under the declared axis law   |

Retained constituents keep their required observations; excluded ones do not contribute. Filtering Graph edges
does not implicitly delete Nodes. Filtering a shaped value does not silently revise its axis schema or invent
coordinates. Group-presence consequences belong to §3.4.1.9.

A source condition may be Boolean, three-valued or otherwise qualified; its exact result interpretation is part
of the selected calculation, not a universal rule that a null-like result means false. Where a Membership class
admits multiple source representatives, a transformation sensitive to their representation requires an actually
lawful representative choice or a result that is independent of that choice (§§3.3 and 3.8). These requirements
neither impose a proof language nor grant a runtime callback unrestricted Contract authority.

#### 3.4.1.9. Grouping, Partition and Association Formation (R02-9)

**Group Formation** applies a selected lawful key calculation to the input constituents and determines which
groups exist and which original occurrences or tuples belong to each group. A grouping formed only from keys
observed in its input does not invent groups for keys that never occur. A different calculation may preserve
previously declared or already existing groups. The presence of an existing empty group remains distinct from the
absence of that group (§§3.1.2–3.1.3).

**Group Constituent Formation** specifies the result form of each group's contents. For an Ordered Occurrence,
source-guaranteed encounter order is preserved within each group; for a Bag or Relation, each selected tuple's
multiplicity is retained. No order is invented for an unordered input. Group-key encounter order and the order of
members within a group are separate observations. A group-value transformation further follows the Map and Filter
laws above; the Group's key presence does not change merely because its constituent values are transformed.

A Grouping performed **after** filtering may omit groups whose keys no longer appear. Filtering **within groups**
that have already been formed may leave their present groups empty. Thus grouping `[a1,b1]` and then removing every
member from each group can yield `{a→[],b→[]}`, while filtering away all input occurrences before grouping yields
`{}` under an observed-keys-only grouping. The selected operation determines whether it retains an empty group,
including when an existing destination is supplied; it does not receive an implicit universal empty-group policy.

**Partition Calculation** assigns constituents to declared alternatives according to its selected classification
law. When a partition is declared complete and exclusive, every input occurrence belongs to exactly one selected
partition and each required alternative remains present even if it receives none. Kotlin `partition` represents
its Boolean alternatives as the two positions of a Product of lists; Java `partitioningBy` represents its
`true` and `false` alternatives as present Map keys. The value formations are not automatically identical merely
because both classify with a Boolean result. Partitions that admit overlap or omission require their own explicit
formation law rather than being called complete, exclusive partitions.

**Associate Calculation** forms a Functional Correspondence from selected key/value results. Where input constituents
yield the same distinguished key, the exact selected construction law determines the final mapped value and any
observable key representative (§3.8). An operation that retains all matched values is a Grouping or
Multi-Association calculation, not the same result as an Associate operation that chooses one mapped value per key.
Kotlin `associateBy`'s later-value selection and Java `Collectors.toMap`'s duplicate-key failure or explicit merge
are qualified source differences, not defaults imposed on every Association.

A source `Grouping` producer that has not been consumed is not a completed Grouped Collection. An operation
updating an existing grouping or association destination may additionally change its source state and must retain
its qualified effects. The corresponding callback, completion and concurrency conditions remain under §§3.3 and
3.9. Aggregation inside a completed group, including its empty-input and Numeric results, is not decided by group
formation alone; its result follows §3.4.1.11 and the owning Numeric or Source law.

#### 3.4.1.10. Sort, Distinct and Representative-Sensitive Selection (R02-10)

**Sort** forms the result order prescribed by the selected comparison and result law while retaining the occurrences
that calculation sorts. Equal comparison keys do not, by themselves, identify the occurrences or authorize their
removal. A stable sort of an already ordered input preserves the prior relative order of comparison ties; a sort
without that guarantee does not. Sorting an unordered input cannot borrow its accidental traversal order as an
established encounter order. A calculation that returns sorting indexes rather than reordered values also retains
that distinct result form. The selected comparison must be lawful over the values being ordered (§3.7); its
comparison-equivalence is not automatically the Membership discrimination relation.

**Distinct** forms a result under a selected discrimination and retention law. It must decide:

| Question        | Legal alternatives to distinguish                                     |
|-----------------|-----------------------------------------------------------------------|
| Comparison      | Full values, selected derived keys or another already lawful relation |
| Duplicate scope | Entire input or adjacent occurrences only                             |
| Representative  | First, last, any lawful one, or another stated rule                   |
| Output          | Ordered occurrences, Membership or another declared form              |
| Multiplicity    | Exact treatment for a Bag or Relation                                 |

A `distinctBy`-style operation can compare derived keys while retaining the original values. The key does not
itself select the original representative. First- or last-occurrence selection requires an owned order and a
matching exact Source law. Retaining any representative and eliminating **every** member of a duplicated class
are different operations. An Ordered result may expose selected representatives and order that a Membership
does not (§3.8). No universal first-wins rule is introduced.

**Extremum Calculation** must distinguish the comparison result from the constituent that attains it.

| Question                 | Source-specific law                                                |
|--------------------------|--------------------------------------------------------------------|
| Extreme comparison value | Minimum or maximum under the declared comparison                   |
| Candidate result         | Comparison value, one representative or all attaining members      |
| Ties                     | First occurrence only if Source guarantees it under a lawful order |
| Empty input              | Permitted absence, Source failure or another exact result          |

Several distinguishable constituents can share one extreme comparison value. Physical evaluation cannot
supply an undeclared first-wins law. Numeric and Text comparisons follow ADR-0078 and ADR-0080.

**Top-K Selection** must state what is selected at the cutoff.

| Case                       | Required decision                                                            |
|----------------------------|------------------------------------------------------------------------------|
| `k=0` or empty input       | Exact empty-input result                                                     |
| Fewer than `k` occurrences | Selected extent                                                              |
| Ties at the boundary       | Exactly `k`, all boundary ties, or all extrema as prescribed                 |
| Result representation      | Ordered occurrences, unordered Membership, indexes or another completed form |

An implementation may use a partial sort or heap, but that does not define the result. These calculations do
not silently change Group keys, Graph Node/Edge Identity or shaped coordinates. Qualified tied-representative
selection follows §3.3 and the applicable ADR-0065 §10 decision, not physical evaluation order.

#### 3.4.1.11. Fold, Reduce, Scan and Aggregate (R02-11)

**Fold** determines a final accumulated result. **Reduce** may instead start from an input constituent or use
another exact Source reduction rule.

| Aspect                | Distinction                                                                 |
|-----------------------|-----------------------------------------------------------------------------|
| Initial result        | Explicit Fold initial value versus Reduce's Source-defined start            |
| Participation         | Which constituents are combined                                             |
| Combining calculation | Exact operation and meaningful direction                                    |
| Empty input           | Initial result, failure, explicit absence or another Source-defined outcome |

Left and right accumulation can differ. An unordered Membership or Bag provides no implicit iteration order
for a direction-sensitive result; the order must be lawfully supplied or the result must be independent of it.
There is no universal null, zero or failure rule.

**Scan and running accumulation** produce prescribed intermediate values, not merely the final Fold result.

| Calculation on `[1,2,3]` using addition      | Completed result |
|----------------------------------------------|------------------|
| Initial-inclusive running Fold, initial `10` | `[10,11,13,16]`  |
| Running Reduce, first input as first result  | `[1,3,6]`        |

Their empty-input outcomes and intermediate-result counts differ. Each completed Prefix retains all selected
steps (§3.1.2). Partial consumption does not establish the complete result. A Source callback reusing a mutable
accumulator can alias earlier values; they are not separate immutable Contract results (§§3.5 and 3.9).

**Aggregate** declares both its input subject and its exact result. A shared name does not make all aggregate
calculations interchangeable.

| Aggregate aspect  | What must be fixed                                                                            |
|-------------------|-----------------------------------------------------------------------------------------------|
| Kind              | Sum, product, average, count, statistic, text or collection combination, or grouped reduction |
| Value semantics   | Result domain and null-like treatment                                                         |
| Completion        | Empty-input meaning and required order                                                        |
| Bag               | All occurrences with multiplicity, or distinct members                                        |
| Association       | Mapped values while keeping different entries distinct                                        |
| Group             | Presence of an empty Group remains distinct from no Group (§3.4.1.9)                          |
| Structured source | Exactly which roles, weights, Graph constituents or shaped coordinates participate            |

If the result is a Product, Collection or shaped value, its existing formation and completeness laws apply.

Numeric aggregates follow ADR-0078, including their format and any required overflow, rounding or intermediate
failure meaning. Text concatenation and Unicode follow ADR-0080. A Java/Kotlin or SQL Source may define a
different empty-input value or permitted Numeric outcome; its exact Source profile governs that choice (§3.3).

Callback effects and accumulator lifetime follow the selected Source operation (§§3.3 and 3.9).
The same applies to any required sequential or parallel combination behavior. These laws
do not require a collector implementation, a new 1D associativity proof or a mandatory reduction tree. Reordering
and fusion require preservation of the result and mandatory observations (§3.11; ADR-0078 §9.2).

#### 3.4.1.12. Window, Chunk and Prefix Selection (R02-12)

**Window Formation** determines which windows exist and which source occurrences each contains.

| Parameter or observation | Selected law                                                |
|--------------------------|-------------------------------------------------------------|
| Starting points          | Lawful indexes or selection boundaries                      |
| Extent                   | Width and movement/step                                     |
| Trailing input           | Treatment of incomplete suffix                              |
| Participation            | Overlap, disjoint chunks or gaps                            |
| Result order             | Window order and each window's internal order, when exposed |

An input occurrence may appear in several result windows without being duplicated in the original input.
Position-based windows cannot invent indexes for unordered Membership (§§3.1.2 and 3.4.1.4).

**Partial Window Formation** depends on the chosen operation. For `[A,B]` with width `3`:

| Selected rule                       | Example completed result |
|-------------------------------------|--------------------------|
| Full windows only                   | `[]`                     |
| Emit short suffix windows           | `[[A,B],[B]]`            |
| One short-input special-case window | `[[A,B]]`                |

The completed empty collection of windows is neither a missing result nor one empty window. Each Source defines
valid width, step and termination behavior. Premature Source termination does not automatically establish a
completed partial window (§3.9).

**Chunk** forms nonoverlapping successive parts under its own extent and trailing-input rules. It can agree
with a particular Window result without becoming synonymous with every Window calculation.

**Prefix Selection** returns an initial segment of an ordered value. It does not produce moving windows or
Scan's intermediate accumulated values (§3.4.1.11).

A Slice or Window retains origin correspondence only when its exact result or Source law requires it.
Temporary callback windows, backed views and separately completed values have different lifetime and
completion rules (§§3.1.2, 3.3, 3.5 and 3.9). No copying or buffer strategy is prescribed.

The word `window` does not prescribe one selection law. The selected calculation may use:

- Positions in an ordered input.
- Comparison peers or relational frames.
- Time-based assignment.
- Multiple shaped axes.

Each has its own domain and completion conditions; a shaped window also supplies the resulting axis schema and
coordinate correspondence (§3.1.3). An incomplete time/window producer does not become complete by its label.
These forms do not expand V1 Input eligibility.

#### 3.4.1.13. Range, Progression and Shaped Calculations (R02-13)

A **Range Calculation** uses exact bounds and endpoint inclusion. Its selected operation determines the result
form, rather than the term `range` imposing one.

| Operation                | Required result distinction                                   |
|--------------------------|---------------------------------------------------------------|
| Selection or containment | Members or Boolean result under the carrier law               |
| Intersection or union    | One Interval, noncontiguous Relation or another admitted form |
| Clamping                 | Exact produced value                                          |

The integer union of `[1,2]` and `[4,5]` is not automatically `[1,5]`. Conversely, intervals can share
members but expose different endpoints (§3.1.3).

A **Discrete Progression Calculation** forms actual occurrences under its selected generation rule.

| Condition                              | Example or consequence                 |
|----------------------------------------|----------------------------------------|
| Start, step, direction and termination | `1..9 step 3` generates `[1,4,7]`      |
| Requested endpoint                     | Need not be generated                  |
| Empty progression                      | No last generated constituent          |
| Source type's stored `last` property   | May have a different qualified meaning |

Source step restrictions and overflow effects remain binding. Only a lawfully determined finite enumeration
forms a completed Ordered Sequence. Interval Membership does not itself create enumeration. Exact occurrence
count and its Numeric representation remain distinct (§3.4.1.1; ADR-0078).

**Shaped Result Transformation** changes coordinates under an exact selected mapping.

| Calculation              | Required result law                                  |
|--------------------------|------------------------------------------------------|
| Reshape                  | Destination rank and shape with value correspondence |
| Axis permutation         | Axis identity and coordinate reordering              |
| Axis selection           | Retained axes and lawful coordinate selection        |
| Flatten                  | Exact order of source coordinates in the output      |
| Other coordinate changes | Their own selected coordinate correspondence         |

Equal element counts determine none of these transformations by themselves. Both `0×3` and `3×0` have zero
value coordinates yet different axes; a rank-zero total scalar contains one empty coordinate (§3.1.3).
Physical stride or memory order cannot choose the result mapping.

**Broadcast and Axis Alignment** define which positional or named axes correspond and how each output coordinate
obtains its value. Several output coordinates may lawfully observe the same input constituent without changing the
source value's constituent identity. A positional trailing-axis broadcasting rule from a particular library is
not a default named-axis Contract rule. Coordinate selection, masking and alignment retain their own exact
result and axis-presence distinctions.

**Sparse, masked and ragged calculations** preserve the states defined in §3.1.3. Dense conversion and Ragged
padding require an exact rule for newly formed coordinates and fill values. A Ragged value does not silently
become rectangular.

| Quantity              | Observation                                    |
|-----------------------|------------------------------------------------|
| Stored-entry count    | Physical entries used by the representation    |
| Physical `nnz`        | The Source-defined nonzero-storage observation |
| Logical nonzero count | Nonzero logical values under the selected law  |

These quantities may differ; none automatically defines Contract cardinality. Completion and 1D eligibility
follow §§3.3, 3.9 and the owning 1D. No new Value Family, IR layout or algorithm is required.

### 3.4.2. Conversion Between Completed Collection Forms (R03 — Reviewed Cases)

A conversion produces the result prescribed by its exact selected operation. It can preserve information, lose
information, or lawfully form new information. The source and result are not thereby identical.

**Recovery** of an earlier completed value is a stronger claim. It requires the result and any separately
admitted information to determine every observation of that earlier form. The information must come from a
lawful input or selected calculation. The following cannot silently replace missing information:

- Source locations or temporary row numbers.
- Physical iteration order.
- Retained compiler caches.

Forming a valid new destination is not recovery; agreement on one projection or enumeration is insufficient.

An authored Java/Kotlin conversion is resolved and qualified before its result is mapped (§3.3; ADR-0079;
ADR-0084). Its Source profile retains required effects and result guarantees. Kontrakt's common calculations,
defined in §3.4.1, need not correspond to a single SDK method.

In either case, an internal calculation or optimized realization must preserve its actual result law and all
mandatory Source observations. A qualified callable does not thereby become eligible in every 1D. The companion
Design owns the exact inventory, evidence and Conformance status; candidate collection is not qualification.

#### 3.4.2.1. Ordered Occurrence, Bag and Membership

Conversion from Ordered Occurrence to a Bag preserves each distinguished member's multiplicity but loses the
source's occurrence order. Conversion to Membership also loses multiplicity.

| Source               | Destination | Observed result |
|----------------------|-------------|-----------------|
| `[A,A,B]`            | Bag         | `{A×2,B×1}`     |
| `[A,B,A]`            | Bag         | `{A×2,B×1}`     |
| Either ordered input | Membership  | `{A,B}`         |

A Bag-to-Membership conversion likewise retains presence but loses the original counts. These outcomes use
§§3.1.1 and 3.4.1.8, not an invented representative rule.

Conversion in the reverse direction needs new, lawful information when the source has already lost it.

| Conversion                | What it can form                                   | What it cannot recover                         |
|---------------------------|----------------------------------------------------|------------------------------------------------|
| Membership → Bag          | Unit multiplicity per member                       | An earlier Bag's repeated members              |
| Bag → Ordered Sequence    | An order selected by a lawful calculation          | An earlier encounter order absent from the Bag |
| Kotlin `Iterable.toSet()` | A result whose iteration preserves encounter order | An intrinsic order for every Membership        |

Java `Collectors.toSet()` and `Set.copyOf()` retain their own Source guarantees (§3.3). Where representatives
remain unspecified, the qualified choice rule of §3.3 applies; no arbitrary first-occurrence or sorting rule
is imposed.

#### 3.4.2.2. Relation and Bag to Functional Correspondence

To form a Functional Correspondence from a relation, the selected conversion must identify which exact role supplies
the key and how the other role or roles form the mapped value. Key discrimination belongs to the destination's
qualified key law. If two source tuples yield the same distinguished key, a direct functional conversion succeeds
only when the selected operation determines one value for that key. A qualified conversion may reject the collision,
select a source-prescribed value or apply an expressly selected combining calculation. The common Association form
itself chooses none of those behaviors (§§3.1.1, 3.4.1.9 and 3.8).

Converting a Bag Relation to an Association can lose tuple multiplicity even if duplicate tuples map to the
same value. For instance, `{(K,X)×3}` may become `K→X` without preserving the earlier count.

| Source situation                                    | Additional requirement                                       |
|-----------------------------------------------------|--------------------------------------------------------------|
| `(K,X)` and `(K,Y)` collide                         | Qualify value choice or conversion failure                   |
| Keys distinct in Source, equivalent in destination  | Qualify newly created collisions                             |
| Reference-identity Map → equality-based destination | Preserve the exact Source key discrimination (§§3.3 and 3.7) |

A finite Association's present entries can be projected to a binary Relation with declared key and value roles.
This preserves their correspondence when the projection retains the applicable key discrimination, mapped values
and legally required representatives. It does not by itself reconstruct a source Bag's former multiplicities, an
undeclared entry order or additional lawful key-domain constraints. In particular, listing the present entries of a
partial correspondence cannot establish the domain or totality of a different, possibly non-enumerated total
correspondence. The selected destination must possess its own completed schema and domain law (§3.1.1).

#### 3.4.2.3. Association Key, Value and Entry Projections

Key Projection retains the present distinguished keys. Value Projection can retain the multiplicity of mapped
values across different keys, or only their distinct Membership, according to its destination form. Entry
Projection retains the actual key/value pairs (§3.4.1.8). These results expose different information: for
`{K1→X,K2→X,K3→Y}`, Value Bag Projection is `{X×2,Y×1}`, whereas Value Membership is `{X,Y}`.

Even complete, separate Key and Value projections cannot generally recover their pairing. For example,
`{K1→X,K2→Y}` and `{K1→Y,K2→X}` have the same separate projections but different correspondences.

A qualified Entry Projection retains the pairing, including required Source role, presence and Representative
observations. A present null-like value remains distinct from a missing mapping (§3.6). Backed Entry Views
and mutable constituents retain their Source lifetime and alias laws (§§3.3, 3.5 and 3.9).

A Sequenced Association can project its entry encounter order into an Ordered Entry result when the chosen operation
guarantees that observation. An unordered Entry Relation instead need retain the correspondence without that order.
Likewise, an Entry Relation alone need not retain a correspondence's otherwise observable lawful key domain or
partial-versus-total form. An inverse formation succeeds as recovery only when those additional observations are
actually established, not merely because an implementation can construct a Map from entries.

#### 3.4.2.4. Grouping, Partition and Aggregate Results

Grouping may preserve order inside each Group and a qualified order of Group keys, yet lose the original
interleaving between Groups.

| Source Sequence | Possible same Grouped result |
|-----------------|------------------------------|
| `[a1,b1,a2]`    | `{a→[a1,a2],b→[b1]}`         |
| `[a1,a2,b1]`    | `{a→[a1,a2],b→[b1]}`         |

The result does not recover the original encounter order without another lawful position or correspondence
relation. Ordinary Group formation does not require one (§3.4.1.9).

Converting a Grouped Collection into a complete and exclusive Partition must supply every declared alternative,
including one that has no constituents, and must preserve the prescribed assignment of each input occurrence.
Kotlin `partition` forms a Product with two present list positions, while Java `Collectors.partitioningBy` forms a
Map with present `true` and `false` alternatives. A grouping that creates only observed keys can instead produce
`{}` on empty input. Forming a Partition from such a grouping requires the selected formation law to account for
the alternatives that grouping did not create. Conversely, converting a Partition into a grouping may discard an
empty alternative only if the selected result law actually prescribes that omission. The completed forms are not
made interchangeable by a Boolean classification alone (§§3.1.2 and 3.4.1.9).

A Group Aggregate forms a result from each present group's constituents. For example, group-count calculation
can turn `{a→[a1,a2],b→[b1]}` into `{a→2,b→1}`. The count result preserves Group presence and occurrence
quantities, not member values or encounter order.

A Grouping that retains occurrences may therefore supply Partition assignments, whereas the reduced counts
cannot reconstruct them. Filtering within an existing Group can leave it empty; filtering before Group formation
can prevent its creation (§3.4.1.9).

An operation updating an existing Group destination may retain pre-existing empty groups and partial changes
when failure occurs. Its exact Source effects follow §3.3. No separate aggregate or rollback authority follows
from these results (§3.4.1.11).

#### 3.4.2.5. Range, Progression and Enumerated Sequence

Range bounds, Progression generation rules and completed Sequence occurrences are different observations (§§3.1.3 and
3.4.1.13). Kotlin's `1..9 step 3` enumerates to `[1,4,7]`. Its last generated value is `7`,
not the original requested endpoint `9`.

The resulting Sequence need not retain the previous expression's endpoint, step or spelling. Distinct Source
expressions can already denote one exact Progression. Different empty ranges can enumerate to the same empty
Sequence while still exposing different bounds in their original forms.

Sequence-to-Range or Sequence-to-Progression conversion requires a new exact destination law.

| Destination information         | Meaning                |
|---------------------------------|------------------------|
| Carrier and bounds              | Range domain           |
| Endpoint inclusion              | Interval Membership    |
| Step, direction and termination | Progression generation |

`[1,4,7]` may form a step-three Progression but cannot recover the earlier Source expression. `[1,4,8]`
does not form one ordinary constant-step Progression without another selected law. Empty Sequence likewise
cannot recover earlier range bounds. New valid formation is not recovery.

An Interval need not enumerate its members to be complete. A qualified finite Progression can determine an ordered
enumeration, but a deferred Java Stream or Kotlin Sequence producer is not thereby an already completed result (§3.9).
The exact number of occurrences can exceed the source element type's numeric range; the number, its numeric
representation and the ability to allocate a particular concrete container are separate matters (§3.4.1.1;
ADR-0078). A Range's member bounds also do not become the positions of an Indexed Sequence produced by its
enumeration (§3.1.2). Any lowering, iteration or optimized enumeration must retain the exact source termination,
last-member and overflow behavior under §§3.3 and 3.11.

#### 3.4.2.6. Indexed, Sequenced and Navigable Conversions

An Indexed Sequence formed by Slice, Gather or Reverse has its **result's** own position domain.

| Operation on `[A,B,C,D]`     | Result      | Result observation                             |
|------------------------------|-------------|------------------------------------------------|
| Slice Source indexes `[1,2]` | `[B,C]`     | Indexes `0,1`                                  |
| Reverse                      | `[D,C,B,A]` | Index `0` selects `D`                          |
| Gather `[2,0,2]`             | `[C,A,C]`   | Repeating a Source index repeats an occurrence |

The Gather result is not Membership. Recovering original positions needs an admitted correspondence beyond the
reindexed result. Empty selection and invalid access remain different under the exact boundary law (§3.4.1.4).

A backed Slice or reversed View may continue to read or change its origin through a source-defined position
mapping. An independently formed List need not retain that connection even if its initial occurrences match the
View. Java `List.reversed()` and Kotlin `List.asReversed()` must therefore be qualified separately from Kotlin
`Iterable.reversed()` and `slice` result formation. Any restrictions arising from structural changes to the origin,
mutable aliases, unsupported updates or access-order reads belong to the selected Source Operation and its
observation lifetime (§§3.3, 3.5 and 3.9). They are not properties of all completed Indexed Sequences.

An Encounter Order can be used to form an Indexed Sequence only when the selected calculation determines the
result's occurrences and positions. A Sequenced Membership or Association does not acquire arbitrary index lookup
merely because its members or entries have a sequence of encounters. Conversely, conversion of an Indexed Sequence
to a Sequenced Membership may remove repeated occurrences under the destination discrimination; the new order must
correspond to the retained members, including any qualified representative choice (§§3.1.2, 3.7 and 3.8).

Enumerating a Navigable Membership or Association can form an Ordered result in the direction guaranteed by the
selected operation. That result does not automatically retain the source comparator, its neighbor queries or its
range-selection capabilities. Forming a new Navigable result from an Ordered Sequence requires a qualified comparison
and destination discrimination, and may combine constituents that the input distinguished. A List containing
`[2,5,9]`, for example, does not by itself determine which comparison law should answer a subsequent `floor(7)`
query. A sorted traversal alone does not complete a Navigable form (§3.1.2).

A Navigable range operation follows the comparison and direction of the **current Source view**. Its endpoint
inclusion and any inherited parent restriction still apply.

For example, on a descending view of `1..5`, `subMap(5,true,2,false)` can produce `5,4,3`. Reversing the numeric
arguments is not the same range. A bound outside the legal range, a valid empty range and an empty result for
another Source-defined reason remain distinct when the Callable distinguishes them.

Comparator-governed Sequenced collections cannot automatically reposition first or last entries. Java sorted
collections have different mutation laws from insertion- or access-ordered collections (§3.3).

#### 3.4.2.7. Declared Product, Tagged Choice and Nested Results

A Named Product can form a Positional Product or Ordered result when the operation specifies a role order.
The new positions carry values, not the original names, optionality or complete schema.

For example, `{customerId=17,quantity=3}` can yield `(17,3)`, but the tuple alone cannot identify which slot was
`customerId`. Reverse formation requires a lawful role-to-slot mapping and every required destination type and presence
condition. Missing optional roles cannot be replaced by present null-like values merely to fill slots (§§3.1.1
and 3.6).

Java records and Kotlin data classes may supply exact Source components, but cannot establish Contract Role
Identity by themselves (§§3.2–3.3; ADR-0079).

Projecting the payload or another result from a Closed Tagged Choice may discard the selected case. Such a
projection is lawful when defined by the chosen operation, but it does not generally permit recovery of the Choice.
For instance, Kotlin `Result.success(null)` and `Result.failure(exception)` can both project to `null` through
`getOrNull()` while having different cases. Java `Optional.ofNullable(null)` instead forms an empty Optional; its
source case and null rules must not be borrowed for Kotlin Result or for a Contract Alternative. A conversion
claiming to retain the Choice must determine the exact case and its admitted payload, including a payloadless case
and an absent versus present optional role. Host success/failure wrappers do not thereby acquire 1D Failure
authority (§§3.1.1, 3.3 and 3.6).

Flattening a Nested Ordered value can preserve outer-then-inner encounter order while removing segment
boundaries. For example:

| Original Nested value | Flattened result |
|-----------------------|------------------|
| `[[A,B],[],[C]]`      | `[A,B,C]`        |
| `[[A],[B,C]]`         | `[A,B,C]`        |

The results do not reveal the original segmentation or which empty inner Sequence existed. Recovery requires
separately admitted segment boundaries, such as per-segment extents. An implementation can use one flat buffer
with logical segment ranges without allocating every inner collection. Completion and stable-value obligations
remain with §§3.1, 3.5 and 3.9.

#### 3.4.2.8. Graph, Hypergraph and Relational Projections

A Graph can be described by Node, Edge and Incidence relations when they jointly retain the Graph's actual
observations. Binary endpoints alone can be a valid projection without recovering the Graph.

| Original distinction                 | Possible loss                                                         |
|--------------------------------------|-----------------------------------------------------------------------|
| Parallel edges `e1:A→B` and `e2:A→B` | Endpoint Membership has only `(A,B)`                                  |
| Endpoint Bag                         | Count `2` survives, but not individual Edge coordinates or attributes |
| Isolated Node                        | Cannot be reconstructed from endpoints                                |
| Source Edge order                    | Not determined by endpoint Membership                                 |

The selected Graph form decides which observations are required (§3.1.3).

Recovering a Graph requires its declared Node and Edge domains, direction, incidence roles and constituent
correspondence. One sufficient logical description for some Graph forms uses separate Node and Edge relations,
with an Incidence relation joining an Edge coordinate, endpoint role and Node coordinate. This is not a required
physical schema.

Reverse formation must satisfy the chosen Graph's incidence constraints. If every endpoint must be a present
Node, `(e1,target,C)` cannot form a Graph whose Node domain excludes `C`. No universal Graph rule creates a
missing endpoint or overrides the selected self-loop and parallel-edge conditions.

Hypergraph conversion preserves the Source's declared incidence law. One hyperedge connecting `A`, `B` and
`C` is not generally three independent binary edges.

A Set of `(edge,node)` pairs may lose information when the Source permits:

- Repeated participation by the same Node.
- Positional incidence roles.
- An Edge with no incidence.

Recovering the earlier Hypergraph requires the relevant grouping, roles, counts or positions. A deliberately
reduced projection remains lawful without claiming full Source equivalence.

A Path or Reachability result is another projection. Membership of reachable node pairs does not determine which
parallel edge was traversed, how many distinct paths exist, or the exact node/edge occurrence sequence of a selected
Path. Where an operation chooses one of several permissible paths, its selection follows the qualified source
meaning and §3.3; R03 does not choose a default path. Graph Data cycles remain independent of prohibited circular
Contract Establishment (§3.1.3). CSR, adjacency indexes and relationship tables are interchangeable physical
candidates only when they preserve the selected logical result (§3.11).

#### 3.4.2.9. Shaped, Sparse, Ragged and Masked Conversions

Linear enumeration of a Shaped Indexed Aggregate forms the Ordered occurrences in the order determined by the
selected coordinate traversal. It does not automatically retain rank, axis roles, shape or the original
coordinate-to-value relation. A `2×3` value and a `3×2` value can enumerate the same six elements in row-major
order; `0×3` and `3×0` can both enumerate to `[]` while exposing different axis extents. Inverse formation
requires the destination's axis schema and a correspondence for every required coordinate. A count of six alone
cannot determine a shape or recover a previous one (§3.1.3).

Each shaped conversion requires a selected coordinate mapping. Equal destination shape does not establish
equal values:

| Conversion of row-major `[[1,2,3],[4,5,6]]` (`2×3`) | `3×2` result          |
|-----------------------------------------------------|-----------------------|
| Reshape                                             | `[[1,2],[3,4],[5,6]]` |
| Exchange axes                                       | `[[1,4],[2,5],[3,6]]` |

Slicing has its own selected correspondence. Broadcasting may map several destination coordinates to one
source coordinate; it need not have an inverse. Axis identity and value correspondence belong to the selected
form, not row-major storage (§3.4.1.13).

Sparse-to-Dense conversion must determine a value for each coordinate required by the destination. A source that
defines absent storage as logical zero can form that zero; a source whose missing mapping denotes Absence cannot
silently substitute zero. Explicit zero, an admissible present null-like value and a Mask state retain their
different meanings where the selected form exposes them (§§3.1.3 and 3.6). Dense-to-Sparse conversion may omit
zero-valued stored entries under its qualified rule, but the resulting logical value's agreement does not recover
which entries were explicitly stored in the original representation. A storage-entry distinction is relevant to
Contract recovery only when the source and admitted completed form make that distinction observable; otherwise it
remains representation (§3.2). Duplicate coordinate entries cannot be summed, overwritten or rejected by a
universal Sparse rule; the exact conversion must define how they form a destination correspondence, using an
admitted combining operation if one is required (§3.4.1.13).

Padding a Ragged value may introduce fill coordinates while losing its original domain.

| Ragged source | Possible padded result |
|---------------|------------------------|
| `[[0],[]]`    | `[[0],[0]]`            |
| `[[],[0]]`    | `[[0],[0]]`            |

The padded result cannot identify which segment held the original zero. Recovery needs lawful segment extents
or a mask that actually distinguishes the original coordinates. Recognizable fillers such as zero, null and
NaN cannot on their own distinguish original constituents from padding.

Masked-to-Unmasked conversion follows the exact mask law. A Boolean mask is not universally deletion,
zero-fill or null-fill. No Tensor buffer layout or new V1 Input form follows (ADR-0064; ADR-0078 §14.1).

#### 3.4.2.10. Weighted Relations, Deltas and Completed Collection Results

A Weighted Relation assigns a weight under its selected weight law; a Bag assigns a nonnegative occurrence
multiplicity. The values correspond only if the chosen calculation actually interprets that weight as the
required occurrence count. `{A→weight 3}` can form `A×3` under a counting weight law; the same numeric value as
a cost or priority cannot. A source weight domain admitting fractional or negative values is not converted to
Bag multiplicity merely by using a numeric Map. A Bag can form a Weighted result when the destination weight
law gives an appropriate meaning to its counts, but this need not recover an earlier unrelated weighting (§§3.1.3 and
3.4.1.7).

When a Weighted Projection combines contributions with the same output tuple, the required combination follows
the selected weight law. For instance, two contributions to `X` weighted `2` and `3` form weight `5` under a
counting-addition law, but that addition is not prescribed for costs, priorities or probabilities. Nor does a
common Weighted form require every weight domain to be a semiring. The exact result and its numeric or other
value obligations remain with §§3.4.1.7–3.4.1.8 and the applicable owner.

A signed Delta can describe a change without being a completed Bag.

| Earlier Bag | Counting Delta | Result                                              |
|-------------|----------------|-----------------------------------------------------|
| `{A×2,B×1}` | `{A→−1,B→+2}`  | `{A×1,B×3}`                                         |
| `{A×2}`     | `{A→−3}`       | Cannot publish multiplicity `−1` as a completed Bag |

An intermediate signed accumulator remains calculation state, not an authoritative completed Bag. Other
completed forms also require their own presence and constituent laws; matching Bag counts do not establish
Membership, Association, Grouping, Ordered or Shaped results.

A final value need not record its change history. Adding and then removing `A` may leave the same Membership as
doing nothing, yet the event sequences can differ.

If the Source requires effects, intermediate observations or callbacks, equal final values do not permit their
elision (§3.3). A compiler-owned Delta may coalesce work only when the required result and observations remain
intact. A valid completed batch is not automatically a sequence of separately published intermediate values,
or vice versa (§§3.9 and 3.11).

Java `Map.merge` and Stream collectors retain their own Source qualification; neither is a universal relation-
delta operator. Differential engines and upsert stores belong to compiler Design. Scheduling and early cutoff follow
ADR-0075.

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

When a protection law requires an earlier Collection observation to survive withdrawal, restoration must preserve
**every observation owned by the selected completed form** (§3.1). Restoring only the element count is insufficient.
This is observational preservation, not byte-for-byte reconstruction or preservation of Host Object Identity.

The protection owner determines the following matters; this ADR does not define a general transaction or rollback
algorithm:

- Scope, participating changes and completion.
- Rollback or withdrawal and their legal observers.
- Treatment of Source effects outside the declared protection.

Restoring one Collection also does not presume restoration of external aliases or resources. A Source operation
may require an intermediate read-after-write: a later callback can observe earlier additions to the destination,
even if the larger protected change has not committed. Deferring every addition until commit could change the
Source result or suppress a required failure. These intermediate reads do not authorize an unrelated observer
to see pending changes. If the Source's external aliases or callbacks make both obligations impossible, the
owning protection and realization laws decide legality. An ordinary unprotected JVM operation retains its
Source effects. SADR-0002 develops the relevant security and fault cases.

### 3.6. Source Null, Contract Absence, and Lookup

**Absence is declared nonpresence under the owning Contract law.** It is not automatically a value in every
domain. The relevant distinctions are:

| Situation                                          | Why it is not universal Absence               |
|----------------------------------------------------|-----------------------------------------------|
| Missing optional Product role                      | Governed by that role's presence law          |
| Valid but unmapped Association key                 | Governed by mapping presence                  |
| Payloadless selected Choice case                   | The Choice itself is present                  |
| Empty Collection                                   | Completed value with no selected constituents |
| Whole-Input absence                                | Governed by the Input law                     |
| Unevaluated or temporarily unavailable Source item | Not yet a completed observation               |
| Invalid coordinate                                 | Outside the admitted coordinate domain        |
| Unresolved meaning or failed calculation           | Not established constituent Absence           |

See ADR-0064 §§7.6 and 7.9.4, ADR-0063 and ADR-0071.

Source-language `null` has its exact Source meaning. It is not automatically Contract Absence or a universal
Kontrakt Null value. A present constituent may be null-like only if its admitted domain permits that meaning.

| Declared role law                     | Possible interpretation                                      |
|---------------------------------------|--------------------------------------------------------------|
| Required and nullable-like            | Present null-like constituent                                |
| Optional without null-like permission | Absent or present non-null constituent                       |
| Optional with null-like permission    | Absence and present null-like value are both distinguishable |

A Host nullable type or `Optional` wrapper does not establish these Contract permissions.

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

A deferred Source carrier is not a completed Contract value merely because it exposes a read-only interface.
Completion depends on the exact result the consumer requires (§3.1). Short-circuit judgments can sometimes
finish after partial traversal; a completed Collection needs all its legally observable constituents.

Deferred Sources differ in evaluation, reuse and one-shot behavior. Repeated traversal does not guarantee equal
results. These subjects must remain separate:

| Subject                             | Constraint                                                          |
|-------------------------------------|---------------------------------------------------------------------|
| Completed reusable Contract value   | Its own established meaning may be reused lawfully                  |
| Retained Source presentation        | Governed by its exact observation and lifetime law                  |
| Previously consumed one-shot Source | Cannot be consumed again merely because another result was reusable |
| Destination-mutating Source call    | Retry may repeat effects surviving earlier failure                  |

Source Qualification establishes consumption and effects. The owning execution and protection laws decide
lawful retry (§3.3).

The selected SDK operation owns required callback behavior and any permitted elision (§3.3). Elision permitted
by the Source cannot be turned into a mandatory invocation or exception.

The qualified Source and owning 1D determine callback order, count, abrupt completion and resource lifetime.
Result order alone does not establish execution order. Prefix values may be observable, but mutable accumulator
aliases cannot be presented as independently frozen earlier results (§§3.4.1.11 and 3.5).

A constituent can be present without its value having been evaluated. Evaluation may also establish a persistent
Source-defined failure state or be triggered by an ordinary-looking object method.

| State                           | Distinction                                           |
|---------------------------------|-------------------------------------------------------|
| Present but unevaluated         | Presence does not imply a completed constituent value |
| Evaluated value                 | Completed under the selected law                      |
| Stored Source failure           | Not a completed normal value                          |
| Temporarily invalid Source view | Governed by its qualified lifetime                    |

Short-circuit evaluation is lawful only when the consumer's result law permits it. Temporary builders, windows,
entries and backed views retain their Source-defined validity periods. Exact version and Preview Qualification
belongs to ADR-0084 and the Design.

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

At runtime, changing the representation between Input acquisition and Fact realization is permitted only if all
required observations and the lawful authority handoff survive (§3.5).

User Operation calls retain their admissible JVM Source meaning (ADR-0073 §§5.7 and 9.6). Fusion or other
transformation must preserve the mandatory observations, including failure conditions, callback behavior and
state effects. A callback or exception the Source permits the implementation to elide need not be forced.

Cache reuse or incremental computation is not permission to skip mandatory state effects. Replaying a
non-idempotent Source operation may itself be unlawful. An Operation may exist only for its effects, without an
Input or outward Result; effects cannot be inferred from the return value.

A reduction may be reassociated, vectorized or parallelized only when its legally observable result and mandatory
process constraints remain intact (ADR-0078 §9.2). The numerical method, partition, merge tree and execution order are
implementation choices unless some aspect is itself source- or Contract-mandated. Reproducible use of one implementation
does not replace the deterministic semantic guarantee of §3.3.

Clean compilation and every valid reuse or repair path must produce the same determined Contract observation
under the same established law and semantic inputs (§3.3; ADR-0075).

An earlier member's change can alter a first-wins representative, Group or Prefix. The update engine must
preserve the owning semantic result. Scheduling, cache state, thread count and update history cannot select
another Contract outcome. The result's validity and coherent observation are governed by ADR-0075, not the
internal data structure.

Reuse validity must account for negative and complete-set observations, including changes caused by a newly
qualifying member (§3.4). An intermediate consumer may already have used an observation before its producer
failed or a protected change was withdrawn.

The owning protection and producer-result laws decide whether such a consumer is canceled, reconsidered or
accepted with a new valid proof. A retained value, old read set or matching bytes do not alone establish
currentness. Full recomputation, validated reuse, incremental repair and early cutoff remain implementation
choices under ADR-0075.

## 4. Consequences and Deferred Work

This ADR fixes the five basic completed-value laws and their closed compositions in §3.1. The corresponding
calculation and conversion laws are in §§3.4.1–3.4.2. Each closed form retains its own additional obligations;
a common formation law does not erase them.

These laws do not automatically admit every Java/Kotlin operation into every 1D. Nor do they require one Host
representation or runtime strategy. The exact Source and 1D obligations govern realization (§§3.3 and 3.11).

The **Collection SDK Interpretation and Calculation Design** owns the detailed SDK and implementation evidence,
as described in §3.3. Its G15–G16 work also covers Source-representation observations, stochastic operations,
temporary builders and lazy object methods. ADR-0079 and ADR-0084 retain exact binding and interpretation
authority. The Design cannot amend this Contract law or admit new Input/Fact forms.

The R02-1 through R02-13 common calculations are defined in §3.4.1. The R03 conversions and their information-
loss and recovery conditions are defined in §3.4.2. Those sections remain the authoritative descriptions of
what R02 and R03 reviewed; this conclusion does not restate their catalogs.

R03's semantic review does not certify all JDK 27 or Kotlin 2.4.21 Callables or their 1D eligibility. It also
imposes no universal Conversion IR, lossless-conversion assumption or storage scheme.

R04's exact Source-specific refinement law is in §3.3, under the existing ADR-0079/0084 qualification path.
Accepted ADR-0065's Admission restrictions require their own proposed amendment; this Draft does not supersede
them.

The remaining independent reviews are:

| Review | Scope                                                                                       |
|--------|---------------------------------------------------------------------------------------------|
| R05    | Completed composite-value coupling and completion                                           |
| R06    | Exact selected Source Callables and Overloads; gaps in the common formation/conversion laws |
| R07    | Frontend, HIR, Establishment and runtime boundaries                                         |
| R08    | V1/V2 implementation preservation                                                           |

R06 requires semantic Qualification, not merely a declaration census. Conformance must cover partial Source
failure, protection-sensitive observations and deferred or concurrent acquisition. The evidence also covers
clean, reused, optimized and incremental calculations.

SADR-0002 owns security and fault-injection scenarios. ADR-0075 owns currentness and early cutoff. Rollback,
Scope, external Transaction and Failure remain governed by their own laws. The companion Design tracks the
detailed work without introducing new Contract authority.

The distinction between Source-permitted choice and actual randomness is defined in §3.3. The proposed
ADR-0065 amendment addresses Admission Judgment Meaning and authored exception handling; ADR-0057 retains
machine Failure decisions. ADR-0064's V1 Input allow-list is unchanged by the common forms (§3.1). Future
Family and Source Profile revisions remain subject to their owning laws.