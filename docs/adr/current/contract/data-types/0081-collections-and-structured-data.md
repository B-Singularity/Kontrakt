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
- `docs/design/collection/kontrakt-collection-sdk-interpretation-and-calculation-design.md` — source/implementation
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

This ADR establishes shared collection and structured-value **meaning**, not a new 1D Contract, independent judgment,
collection runtime, source API or universal `List`/`Set`/`Map` type. A value family specifies the constituent relations
that a completed result makes available. The owning 1D decides whether those relations are legal, which distinctions
it admits, and what its own Sameness, judgment, Identity and Establishment mean.

A common family must not silently add a position, remove a duplicate, choose a representative, invent an absent value,
or transform a source-defined effect into Contract authority. The semantic coverage model must account for every
relevant distinction of a supported Java/Kotlin source operation. Family availability does **not** expand the forms
already allowed by ADR-0064 §7.9 in V1 Input.

#### 3.1.1. Constituent families

The common model distinguishes the following completed-result meanings:

| Family               | Required semantic distinction                                                                        |
|----------------------|------------------------------------------------------------------------------------------------------|
| Encounter Sequence   | Ordered constituent occurrences and multiplicity without inventing an indexed-access law             |
| Indexed Sequence     | A defined position domain with a constituent at each position and repeated occurrences where present |
| Multiset / Bag       | Each distinguishable member's occurrence multiplicity without intrinsic position                     |
| Membership           | Presence and absence of members under the applicable discrimination relation                         |
| Association          | Distinguishable keys and each key's value correspondence                                             |
| Indexed Membership   | Presence and absence over a defined index domain and its ranges                                      |
| Range Domain         | Bounds, endpoint inclusion and domain membership                                                     |
| Progression          | Starting point, step, direction, stopping rule and resulting occurrences                             |
| Named Product        | Individually declared named constituents and their meanings                                          |
| Positional Product   | Individually defined constituent positions without inventing names                                   |
| Closed Tagged Choice | A closed alternative domain, the selected alternative and its associated payload                     |

A Multiset cannot be reduced to Membership without losing multiplicity, or to Sequence without adding position. A
Range Domain and a Progression are not interchangeable. A Positional Product exists in the common meaning space but
is not thereby permitted as an arbitrary tuple in V1 Input. A Source result that resembles a Product or Choice does
not acquire Contract Sameness, Failure authority or case Identity from its host class.

#### 3.1.2. Composed family meaning

A family may combine constituent relations without becoming an additional Contract authority. The common model must
preserve, when defined by the applicable source and admitted by its owner:

- ordered and navigable Membership or Association, including source-defined encounter order, comparison order,
  range and neighbor relations;
- group and partition existence, their keys, empty constituents where significant, and each group's lawful
  multiplicity and order;
- multiple value occurrences associated with a key;
- slice, window and prefix results, including each distinct result and any legitimate relationship to its origin;
- nested structured constituents, with all lawful relations of the contained family.

These are **semantic compositions**, not a mandate to mint separately stored Contract objects, host classes or HIR
nodes for every named form. Two family descriptions may be unified only if their legal observations remain exactly
representable after composition, including presence, absence, multiplicity, order, representative, bounded selection
and constituent completeness. Merely sharing a result interface or host `equals` is not proof of equivalence.

#### 3.1.3. Common distinctions and limits

The family model must keep source discrimination, Contract Sameness, value Identity, occurrence, position and current
membership separate. The comparison law needed by a family is not a global equality: the selected Source Operation
uses its own qualified relation, while the owning 1D uses its own declared law. Encounter order, explicit position,
comparison order and priority-head selection are different observations. Cardinality means the number of constituents
specified by the relevant family; it is not automatically a platform capacity, number of completed evaluations, or
number of waiting operations. Absence, present null, empty collection, unevaluated constituent, failed evaluation and
unavailable-but-present source item are distinct where the governing laws distinguish them.

A *value family* is not the same thing as a stateful collection **operation**, traversal **protocol** or **observation
lifetime**. The latter may change a destination, block or hand off an item, invoke callbacks, depend on time, expose a
live view or defer evaluation. Those exact source meanings must still be interpreted under §§3.3, 3.5 and 3.9; they
are not additional immutable Contract Value Families merely because the SDK calls them collections. The Design owns
their inventory and classification, including producer/consumer surfaces adjacent to the Collection Framework.

Family names in this Draft are semantic descriptions, not fixed IR discriminants, physical encoding, serialization
codes or source type aliases. The Source Coverage Index and callable mapping in the companion Design remain evidence
of investigated candidates, **not** proof that every public Java/Kotlin overload is supported.

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

The relevant observations follow the selected result family and operation: position and occurrence multiplicity;
member inclusion and exclusion; mapping and key discrimination; group and partition presence; a source-guaranteed
order, range or neighbor; a selected representative; a prefix/window constituent and its lawful lifetime; a
Progression's actual generated occurrences; and admitted nested Product or Choice relations. A stateful source
operation may additionally have a head, transfer or time-gated eligibility observation. None of these distinctions
is automatically present in every Family.

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

This ADR fixes the common constituent and relational meaning that qualified Collection calculations must preserve.
The Contract laws in §§3.1–3.11 do not make every Java/Kotlin operation legal in every 1D, compel materialization or
copying of temporary results, or require a particular storage layout, algorithm, state engine, runtime proxy, IR node,
cache scheme or incremental scheduler. Completed Contract meaning and mandatory source observations remain
independent of those implementation choices.

The **Collection SDK Interpretation and Calculation Design** owns detailed Value/Operation/Observation catalogs,
Java/Kotlin type and implementation mappings, precise public-callable and overload inventories, SDK version/Preview
profiles, qualification evidence, test fixtures and unsupported/unresolved entries. It also records the G15–G16
findings on source-representation observations, stochastic operations, temporary builders and lazy object methods.
ADR-0079 and ADR-0084 retain exact binding and interpretation authority. The Design is implementation-facing and
cannot amend this ADR or admit a new Input/Fact law.

The remaining work is to finish exact-source qualification; prove the completeness and compositional sufficiency of
the Family model, including currently unclassified source surfaces; verify deterministic refinement against each
specified outcome set and its mandatory execution effects; exercise partial failure, protected internal observations,
views, lazy and concurrent acquisition; and compare clean, reused, optimized and incrementally maintained results.
Security and fault-injection obligations remain in SADR-0002. ADR-0075 owns result currentness and early cutoff;
Rollback, Scope, external Transaction and Failure owners govern protection and recovery. The companion Design tracks
these work items and their evidence without turning them into new Collection Contract authority.

**Accepted ADR-0065 §10 remains a reconciliation task:** arbitrary compiler implementation preference is not a
lawful meaning determinant, whereas a *qualified, Kontrakt-owned deterministic semantic refinement* is this ADR's
default within a Source-permitted outcome space (§3.3). This Draft does not silently change the Accepted ADR.
Likewise, the existence of a common Multiset or Positional Product does not amend ADR-0064's V1 Input allow-list.
Family and Source Profile evolution must be reviewed under their actual owning laws.