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

Replacing these operations with one generic collection calculation can change the author's source meaning. Moreover, an
API may allow multiple observable results even when one current JDK happens to return the same result repeatedly. A
governed Contract judgment cannot acquire determinism from that implementation accident.

The risks also extend to work that is interrupted. An operation can change an existing destination before failing; a
consumer can observe a constituent or an absence assertion before a larger result is complete. Those observations can
propagate to later calculations even if the original change is subsequently withdrawn. Whether those consumers must be
rejected, reconsidered or rolled back belongs to their owning result and protection laws. Collection interpretation
nonetheless has to preserve the distinctions on which they relied.

At runtime, external mutable carriers and live views can change while their data is being acquired. The problem ends at
the appropriate boundary once a complete, lawful Input presentation has been formed; the original object need not travel
through the Contract pipeline or remain part of an established Fact. During compilation, the corresponding issue is
whether authoritative definition material and compiler results preserve their meaning, not whether the compiler's
working collections are physically mutable.

Requiring authors to use immutable collection classes solves neither problem. The design must preserve mandatory source
observations and determinate Contract results while allowing V1 and V2 to replace internal algorithms and physical
representations.

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

Failure at an intermediate element is particularly significant for operations that mutate an existing destination.
The source law may leave earlier removals, substitutions or additions in place; another collection implementation may
leave the destination unchanged at the same failing callback. Such behavior is qualified for the exact callable and
implementation guarantee, not inferred from the common interface. This ADR does not define the Failure result or the
subsequent rollback procedure.

**A governed Contract judgment must establish one determinate legally observable result for the same governing law and
meaning-determining semantic inputs.** If the exact source API guarantees that result, Kontrakt preserves it. If the API
permits several results, the judgment must either be invariant under every legally permitted result or use a lawful
deterministic refinement to fix any observable distinction that matters. Repeated behavior of a particular JDK,
sequential execution alone, or a particular thread schedule does not supply that guarantee. Where neither an invariant
judgment nor a lawful refinement can be established, the operation is unsupported for that judgment.

Refinement must remain within every required source observation, including mandatory evaluation, completion and failure
behavior (ADR-0073 §5.7; ADR-0078 §3.3 for Numeric operations). The fixed obligation is the result and any mandatory
semantic process condition, **not** a sort, reduction tree, scheduling order, reference algorithm, or other realization
strategy. An unchanged admissible platform operation in user realization remains subject to its own platform law; the
preceding deterministic-judgment requirement does not rewrite that operation.

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
does not create that order when the input does not own it. A source operation that permits an unordered choice can leave
the output's membership or representative underdetermined, not merely its iteration order. Such use must satisfy §3.3.

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
alias and resource effects cannot be presumed restored by restoring one collection value. The corresponding security
and fault scenarios are developed in SADR-0002.

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

An extreme-value operation may determine the minimum or maximum comparison value without determining an observable
representative among ties. Where the selected API guarantees first-wins or another tie law, that law is preserved. A
host operation with unspecified ties does not acquire a first-wins guarantee from its current implementation (ADR-0084).

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
constituents. Java Stream and Kotlin Sequence differ in evaluation, reuse and one-shot behavior. Repeated traversal is
not proof that repeated results are equal.

The selected SDK operation owns mandatory callback observations and any permitted elision, including associated failure
reachability. A source-allowed omission must not be converted into a required invocation or exception. Callback
ordering, invocation cardinality, abrupt completion and resource lifetime are preserved where the source and owning 1D
make them obligations; result order alone does not establish execution order. Prefix calculations can expose
intermediate values, but mutable accumulator aliasing cannot be misrepresented as independently frozen source results.

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
transforms or fuses platform collection work only when all mandatory source observations, including guarded failure and
callback obligations, are preserved; it need not force a callback or error the source permits it to elide (ADR-0073
§§5.7, 9.6).

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

The shared law does not make every Java/Kotlin Collection operation legal in every 1D. A resolved operation can fail its
owner's closure, determinism or completion requirements. The compiler need not materialize every intermediate collection
or copy a carrier at every phase. It must preserve the source observations still required and establish each Contract
result at its actual boundary (§§3.3, 3.5 and 3.11).

The following work remains outside this draft's decided meaning:

| Work                                                | Owner or constraint                                                                                                                                                                                                                                                                       |
|-----------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Common collection families                          | Decide the exact need and domains for Sequence, Membership, Association, Multiset and structured forms. Do not infer them from host interfaces (§3.1).                                                                                                                                    |
| Per-operation sorting and aggregation qualification | Record tie selection, stable ordering, numeric reductions, Collector and parallel guarantees in the applicable SDK profile. The governing determinism and preservation requirements are already stated (§§3.3, 3.4 and 3.8; ADR-0078).                                                    |
| Per-operation deterministic refinement              | Establish when a source-permitted range of outcomes can be lawfully refined for governed judgments. The applicable result law must not be replaced by the behavior of one JDK (§3.3).                                                                                                     |
| Deferred and mutable source forms                   | Qualify one-shot traversal, callback elision, backed views, concurrent access and partial failure per callable; separate those source properties from runtime Ingress acquisition and completed Contract material (§§3.5 and 3.9).                                                        |
| Partial completion and protected changes            | Test whether a selected operation promises one completed result or independent results; preserve the owning protection law when a collection change is canceled. Scope, commit, recovery and external participation remain with Failure/Governance/Transaction owners (§3.5; SADR-0002).  |
| Collection observation and incremental validity     | Exercise absence, completeness, duplicate counts, representative changes, nested constituents and failed pending reads. ADR-0075 owns currentness and valid reuse; the security attack and fault cases are specified in SADR-0002 (§§3.4, 3.11).                                          |
| SDK coverage and conformance                        | Complete Java JDK 27 and Kotlin 2.4.21 callable-level qualification. The existing declaration inventories are not proof of complete coverage (ADR-0079; ADR-0084).                                                                                                                        |
| Object laws outside collections                     | Align the general representation/authority boundary with existing Accepted ADRs and still-Draft ADR-0083. Do not create a second object authority here.                                                                                                                                   |
| Input evolution                                     | Reconsider ADR-0064's current collection limits after this ADR is decided. Nothing here silently changes its Accepted V1 surface.                                                                                                                                                         |
| IR, runtime Ingress and compiler Design             | Choose value encodings, explicit absence representation, acquisition and formation mechanisms, protocols, optimization and incremental algorithms. Keep compile-time definition material distinct from runtime Input and Fact occurrences; demonstrate conformance under the owning ADRs. |

The SDK catalogs and compiler designs are separate artifacts. This ADR states the meaning they must preserve; it neither
declares every source call qualified nor selects a JVM collection implementation.