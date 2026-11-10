# Collection and Structured Data SDK Interpretation and Shared Calculation Design

## Status

**Working Design — Java/Kotlin source-surface catalog, semantic qualification, implementation mapping and conformance;
not an Accepted Contract decision.**

## Date

2026-11-10

## Scope and Ownership

This implementation-facing Design follows *What Contract Is*, applicable Accepted ADRs, and the resolved provisions of
Draft ADR-0081. **ADR-0081 owns shared Collection Family meaning, legal constituent distinctions, deterministic
refinement and Contract-boundary preservation.** This Design owns concrete source API inventories, Java/Kotlin
implementation mappings, callable/overload qualification records, coverage evidence, realization candidates and
conformance procedures. It cannot create an independent Collection authority, approve an unqualified operation, expand
an owning 1D, or amend an Accepted ADR.

The investigated standard-library profiles are **JDK 27 / Java SE 27** and **Kotlin/JVM 2.4.21**. These are research
baselines, not declarations that Kontrakt already supports all corresponding APIs. The supplied JDK 27 `src.zip` and
`java.base.jmod`, Kotlin Standard Library 2.4.21 source JAR and compiler archive are evidence inputs. The host runtime
available during the G15–G16 study was Java 21; it does not prove JDK 27 Preview execution behavior.

This Design follows the document separation used by
`docs/design/character-text/kontrakt-character-text-sdk-interpretation-and-calculation-design.md`.
Its source navigation index and representative API mapping are incorporated in the appendices below. The mechanically
found source-declaration candidate CSV remains an independent *audit input*, not a qualified API catalog.

---

# 1. Purpose, Boundaries and Completion Claim

## 1.1. Task of this Design

The compiler must be able to distinguish every source-visible Collection meaning that matters to a lawful authored
calculation, even when that meaning is absent from the current Input allow-list. It first resolves an exact Java/Kotlin
callable under ADR-0079, qualifies that callable's SDK behavior under ADR-0084, and only then maps the understood result
to the shared constituent meaning provided by ADR-0081. The receiving 1D independently decides admissibility.

Collection coverage includes ordinary List/Set/Map interfaces, concrete and legacy implementations, arrays (including
primitive and unsigned arrays), Range and Progression, queues and concurrent structures, Collections and Kotlin
extensions, Stream/Sequence/Iterator/Spliterator/Grouping/Collector/Gatherer, source views, builders, lazy and
structured values. Adjacent producer/consumer interfaces such as Java Flow and NIO buffers must be classified, not
mistaken for automatically completed Collection Values. An implementation class that has no public-facing extra
meaning does not need a new Family; an API whose meaning cannot be represented must not be silently dropped.

Neither source inventory nor Common Family availability proves that a caller can use the operation in Admission,
Invariant, Input or another 1D. Source closure, completion, equality, determinism, effects and the actual legal domain
are determined by the owning laws, not by a generic JVM Collection whitelist.

## 1.2. Meaning preserved from Contract sources

- Source comparison (host equality, comparator equality, reference identity and others) is distinct from the owning
  1D's Sameness and exact Contract Identity.
- Source result selection, cardinality, constituent order and failure are interpreted before projecting a legal 1D
  result. A Source Operation that exposes a live view is not thereby an established closed value.
- The deterministic refinement default belongs to ADR-0081 §3.3: preserve results guaranteed by the specification;
  refine a specification-permitted choice lawfully and deterministically while retaining mandatory execution effects.
- The current Input presentation allow-list remains that of ADR-0064 §7.9. A shared Multiset, Positional Product,
  source View or Queue protocol has no automatic V1 Input authority.
- Compile-time Resolved Contract HIR, Establishment and Canonical Contract World are distinct from runtime Interaction
  Ingress, Input/Fact occurrences and JVM user Operation execution. Catalog metadata is not a Contract semantic
  reference, HID, source-path identity or compiler-produced authoritative result.
- Rollback Scope, commit, failure propagation, external participant transaction policy and diagnostics remain with
  their actual owners. The threat and fault-injection catalog is SADR-0002, not this Design.

## 1.3. Completion gate

**G10–G14 is a semantic coverage design, not proof of a complete qualified SDK.** G15–G16 has 102 curated source
operation records with navigable source locations; each is explicitly **not overload-qualified**. The remaining source
candidates require visibility filtering, exact overload binding, semantic proof and conformance before being called
supported. Each missing source or unclassified operation must be reported rather than excluded without record.

# 2. Standard-Library Profiles and Evidence

## 2.1. Source scope and evidence levels

| Investigation surface                       | Evidence in the 2026-11-10 inventory                             | Claim deliberately not made                                          |
|---------------------------------------------|------------------------------------------------------------------|----------------------------------------------------------------------|
| JDK 27 `java.util` top-level                | 129 `.java` files                                                | Every file or class is a relevant public Collection API              |
| JDK 27 `java.util.concurrent` top-level     | 68 `.java` files                                                 | Every concurrent API is a Collection Value                           |
| JDK 27 `java.util.stream` top-level         | 40 `.java` files                                                 | Every Stream overload has been semantically qualified                |
| JDK 27 `java.nio` top-level                 | 85 `.java` files                                                 | Buffer protocols are ordinary Collection Families                    |
| JDK 27 `java.util.function` top-level       | 44 `.java` files                                                 | All callback signatures/effects are closed                           |
| JDK 27 `java.util.regex` top-level          | 9 `.java` files                                                  | Regex owns Collection Value meaning                                  |
| Kotlin 2.4.21 Stdlib                        | Common, generated, JVM, unsigned and extension files inventoried | All K2-visible public overloads and expect/actual have been resolved |
| Curated Java + Kotlin mapping               | 102 representative rows, Java 67 / Kotlin 35                     | Full behavioral proof of 102 overloads                               |
| Mechanically found Java declaration lines   | 7,610 candidate rows                                             | Accurate public Java 27 API count                                    |
| Mechanically found Kotlin declaration lines | 4,365 candidate rows                                             | Accurate K2 resolved callable count                                  |
| Combined declaration candidate rows         | 11,975 rows                                                      | Public callable inventory, let alone support proof                   |

The supplied source archive spans more modules than these selected package directories; the package inventory is a
source-navigation baseline. JDK 27 binary signature, inherited methods and Kotlin compiler binding metadata must be
reconciled before making any exhaustive coverage claim. Source-line hints can refer to documentation or usage rather
than a declaration. Do not infer supported Java 27 Preview behavior from execution on a different JDK.

## 2.2. Exact profile determinants

A qualification record is distinguished by source language and language version; actual compiler resolution, receiver
and dispatch target where relevant; overload, type arguments, nullability/boxing, default/named arguments and imports;
SDK artifact/version and Preview/Experimental state; caller/element types; selected comparison and ordering relation;
callback identity and its relevant closed meaning; source state, aliases and observation lifetime; concurrent,
localization, time or GC-dependent conditions where an API exposes them; and the owning 1D's admitted domain.

Deterministic refinement must be governed by lawful semantic determinants rather than memory addresses, thread
scheduling, arbitrary hash iteration or incremental execution history. It is an exact semantic qualification, not a
`sort()` helper always applied after an SDK call.

## 2.3. Evidence and implementation-support labels

| Evidence label                    | What has been shown                                                                  | What has **not** been shown                          |
|-----------------------------------|--------------------------------------------------------------------------------------|------------------------------------------------------|
| Inventoried                       | Candidate declared source surface is recorded                                        | Actual public visibility or behavioral understanding |
| Recognized                        | Compiler knows the source callable kind                                              | Correct binding at a use site                        |
| Resolved                          | Exact source binding and overload were determined                                    | Exact result and effect meaning                      |
| Interpreted                       | SDK semantics, legal input domain and relevant effects are known                     | 1D admissibility or legal closure                    |
| Mapped                            | The needed meaning corresponds to ADR-0081 families and selected shared calculations | Universal family equality or source-call replacement |
| Eligible / Ineligible / Undecided | The owning 1D's lawful use has been checked                                          | Other 1Ds' permission                                |
| Implemented                       | A compiler realization exists for the exact profile                                  | Source differential conformance                      |
| Conformant                        | The declared profile passed applicable evidence gates                                | Another SDK version/overload is conformant           |

These labels are implementation evidence states. They are not an extra Contract Admission, ratification or
Establishment pipeline, and an unsupported legal expression is not automatically an invalid author declaration.

---

# 3. Value and Structured Data Family Catalog

The identifiers `F01`–`F21` are **Design catalog review handles**, not Contract Identity, HIR node codes, serialized
representation or public API names. ADR-0081 §3.1 owns the constituent laws; this catalog maps observed SDK surfaces
to those laws. Family merging requires proof that constituent and legal operation observations are unchanged.

## 3.1. Core constituent families (`F01`–`F11`)

| ID  | Catalog family       | Source distinction and mapping obligations                                                 | Representative source surfaces                                                   |
|-----|----------------------|--------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| F01 | Encounter Sequence   | Ordered occurrences and multiplicity; do not infer arbitrary indexed access                | `SequencedCollection`, ordered traversal                                         |
| F02 | Indexed Sequence     | Exact index domain, repeated occurrences and positional access                             | `List`, Java/Kotlin arrays, primitive/unsigned arrays                            |
| F03 | Multiset / Bag       | Per-member multiplicity without intrinsic position; distinguish projection and snapshot    | `Map.values()`, duplicate-preserving grouping/relations                          |
| F04 | Membership           | Presence and absence under an explicit discrimination law                                  | `Set`, `EnumSet`                                                                 |
| F05 | Association          | Distinguished keys and each key/value correspondence                                       | `Map`, Kotlin `Map`, qualified specialized maps                                  |
| F06 | Indexed Membership   | Bit or indexed membership including range operations; count vs extent vs capacity          | `BitSet`                                                                         |
| F07 | Range Domain         | Bounds, endpoint inclusion, member domain                                                  | Kotlin `ClosedRange`, `OpenEndRange`                                             |
| F08 | Progression          | Start, direction, step, stopping and emitted occurrences                                   | Kotlin `IntProgression`, `LongProgression`, `CharProgression`, unsigned variants |
| F09 | Named Product        | Named constituents under actual declared law; no automatic host property enumeration       | Java Record, declared Input Named Product                                        |
| F10 | Positional Product   | Fixed constituent positions; not automatic V1 Input tuple admission                        | Kotlin `Pair`, `Triple`, tuple-shaped source results                             |
| F11 | Closed Tagged Choice | Closed alternatives, selected case and payload; no host Failure/Absence authority transfer | Java `Optional`, Kotlin `Result`, qualified sealed-source cases                  |

**Distinguishing examples:** `Map.keySet()` retains key Membership, `values()` retains repeated Values (a Bag when no
order is guaranteed), while `entrySet()` carries distinct key/value entries. A Map mapping `A → X` and `B → X` has two
value occurrences even when both compare equal. Mapping all three projections to `Set` drops a multiplicity distinction;
mapping an unordered values view to an indexed Sequence creates an unintended position distinction. The Projection
operation's alias and View lifetime additionally belong to E06.

`List` does not require `RandomAccess` to own indexes: a linked List and an array-backed List may have very different
access costs yet both expose the List index law. `Range` specifies a domain, while `Progression` specifies actually
produced members and step, so one does not automatically substitute for the other. Host `Optional` or `Result` is
choice-like only as Source meaning: Contract Failure, Absence and case authority are owned elsewhere.

## 3.2. Composed family catalog (`F12`–`F21`)

| ID  | Catalog composition          | Meaning that must survive mapping                                          | Example sources                           |
|-----|------------------------------|----------------------------------------------------------------------------|-------------------------------------------|
| F12 | Sequenced Membership         | Membership and guaranteed encounter order, distinct from intrinsic indexes | `LinkedHashSet`, `SequencedSet`           |
| F13 | Navigable Membership         | Membership, comparator order, bounds and neighbor selection                | `SortedSet`, `NavigableSet`, `TreeSet`    |
| F14 | Sequenced Association        | Key/value correspondence and Entry encounter order                         | `LinkedHashMap`, `SequencedMap`           |
| F15 | Navigable Association        | Association with key comparator, range and neighbor relations              | `SortedMap`, `NavigableMap`, `TreeMap`    |
| F16 | Grouped Collection           | Group existence, group keys, elements, group-local order and multiplicity  | Kotlin `groupBy`, Java `groupingBy`       |
| F17 | Partitioned Collection       | Declared partitions, inclusion/exclusion and explicit empty partitions     | Kotlin `partition`, Java `partitioningBy` |
| F18 | Multi-Association            | Several value occurrences related to a distinguished key                   | `Map<K,List<V>>` and compound results     |
| F19 | Slice / Window Result        | Selected constituents, window bounds and any origin relation               | `subList`, `windowed`, `chunked`          |
| F20 | Prefix Result                | Each progressive intermediate result and step relation                     | `runningFold`, `runningReduce`, `scan`    |
| F21 | Nested Structured Collection | All lawful constituent relations within each nested value                  | Nested lists/maps/arrays/products         |

A Composition can be represented by relations among core families; a separately stored collection node is not
mandatory. Source grouping with downstream filtering can retain an empty group while filtering before grouping may
omit that group. Consequently Map equality or final Value class alone cannot stand in for Grouped meaning.

Named/Positional Product and Choice are deliberately included in the common interpretation inventory, but the legal
set of Input presentation constructors remains ADR-0064 §7.9. Being representable does not imply being authored,
accepted, established or usable as an independent Authority.

---

# 4. Stateful Operation and Observation Protocol Catalogs

The `P` and `E` catalogs cover source semantics needed to interpret SDK calculations. They are not Contract Value
Family variants; a stateful queue or one-shot producer does not become a complete immutable value merely by having a
source type name.

## 4.1. State and operation groups (`P01`–`P12`)

| ID  | Group                     | Mandatory investigation distinction                                   | Source examples                              |
|-----|---------------------------|-----------------------------------------------------------------------|----------------------------------------------|
| P01 | Positional Update         | Index-based addition, substitution, removal, position movement        | `List.add`, `set`, positional removal        |
| P02 | Membership Update         | Insert/delete according to selected discrimination relation           | `Set.add`, `remove`                          |
| P03 | Association Update        | Key presence, conflict, conditional update, deletion and callback     | `Map.put`, `compute`, `merge`                |
| P04 | Bulk Mutation             | Element-by-element changes, callback order, partial abrupt completion | `removeIf`, `replaceAll`, Kotlin `mapTo`     |
| P05 | FIFO / LIFO               | Next item selection and storage/update direction                      | Queue / Stack operations                     |
| P06 | Deque                     | Front/back insertion, selection and removal                           | Java/Kotlin `ArrayDeque`                     |
| P07 | Priority Selection        | Comparator head, unspecified ties versus independent iterator order   | `PriorityQueue`, `PriorityBlockingQueue`     |
| P08 | Capacity / Blocking       | Waiting, interruption, capacity, timeout, bulk transfer failure       | `BlockingQueue`, `BlockingDeque`             |
| P09 | Transfer / Rendezvous     | Producer/consumer matching and handoff completion                     | `TransferQueue`, `SynchronousQueue`          |
| P10 | Time-gated Selection      | Element presence distinct from current removal eligibility            | `DelayQueue`                                 |
| P11 | Access-dependent Update   | Lookup changes source order or state                                  | Access-order `LinkedHashMap.get`             |
| P12 | Computed / Default Lookup | Absent key vs present null, callback firing, result adoption          | `getOrPut`, `computeIfAbsent`, `withDefault` |

**Counterexamples:** `PriorityQueue.peek()` selects by priority but does not grant priority-ordered iteration.
`DelayQueue.peek()` may observe an unexpired head while `poll()` returns no item. `SynchronousQueue` can complete a
producer/consumer transfer without ever holding a stored Collection element. The word Queue alone proves none of
these behaviors. Kotlin `mapTo` may have modified the Destination before a Transform fails; a different SDK
implementation may provide a stronger partial-failure guarantee. The exact operation and selected implementation law
must be qualified.

## 4.2. Evaluation and observation groups (`E01`–`E11`)

| ID  | Group                             | Required interpretation and lifetime                                                   | Source examples                                |
|-----|-----------------------------------|----------------------------------------------------------------------------------------|------------------------------------------------|
| E01 | Traversal Cursor                  | Progress, removal, invalidation, one-shot consumption                                  | `Iterator`, `ListIterator`, `Enumeration`      |
| E02 | Splittable Traversal              | Split, size, distinctness and encounter characteristics                                | `Spliterator`                                  |
| E03 | Deferred Pipeline                 | Lazy evaluation, short-circuit, terminal operation, callback elision                   | Java Stream, Kotlin `Sequence`                 |
| E04 | Stateful Gathering                | Intermediate groups/folds/windows and terminal consumption                             | Java `Collector`/`Gatherer`, Kotlin `Grouping` |
| E05 | Lazy Constituent                  | Per-index/key/member evaluation, reentrancy and stored failure, object method triggers | JDK 27 Preview `ofLazy`                        |
| E06 | Backed / Reversed View            | Origin linkage, update visibility, supported operations and validity period            | `subList`, `reversed`, `asReversed`, Map views |
| E07 | Snapshot Observation              | Stable previously obtained observation and new-source changes                          | Copy-on-write iterator                         |
| E08 | Concurrent Observation            | Atomic per-op semantics vs weak traversal, synchronization prerequisites               | Concurrent collections                         |
| E09 | Environment-dependent Observation | GC or another external event changes membership                                        | `WeakHashMap`                                  |
| E10 | Asynchronous Producer             | Subscription/demand/delivery/termination, separate from a complete result              | Java `Flow`                                    |
| E11 | Indexed Buffer Cursor             | Position, limit, index, slicing and shared backing                                     | Java NIO Buffer                                |

Temporary Builder Action receivers, a callback-scoped Window or a backed Map Entry can have a different valid
lifetime from the completed value eventually produced. Lifetime differences do not automatically require deep copy or
reference preservation. Kotlin Sequence and Java Stream can both defer work but differ in legal reuse and precise
callback behavior. No evaluation group grants source object identity Contract authority.

## 4.3. Extra interpretation dimensions, not Value Families

| Dimension                     | Examples                                                                      | Reason to keep distinct                                                            |
|-------------------------------|-------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| Comparison and discrimination | host `equals`, comparator zero, Java reference identity, enum domain          | Distinct source meanings; not universal Contract Sameness                          |
| Order and selection           | No order, encounter, comparator, priority-head                                | A deterministic representative need not create a sequence index                    |
| Cardinality / multiplicity    | member count, total occurrences, groups, evaluated constituents, capacity     | One `size` API name does not imply one Collection cardinality                      |
| Absence and null              | missing key, mapped null, empty Collection, optional empty, unevaluated value | Different source and owning 1D meanings                                            |
| Failure / partial completion  | throw, short-circuit, incomplete Destination, stored evaluation error         | May preserve prior state; not Contract Failure automatically                       |
| Alias and lifetime            | live views, temporary windows, cached snapshots, retained mutable elements    | Source's mutable graph not necessarily an established value                        |
| Explicit randomness           | seeded/randomized shuffle, `random()`                                         | Random-source effects distinct from unspecified representative selection           |
| Representation observation    | `BitSet.size()` and similar                                                   | Platform-visible capacity is not intrinsic Family cardinality or Contract Identity |
| Concurrency and environment   | weak consistency, atomic compute, GC                                          | A thread-safe operation is not a coherent whole-collection presentation            |

---

# 5. Source Operation and Transformation Catalog

These are **Source Qualification work groups**. Similar operation names across Java and Kotlin do not imply the same
constituents, effects, callbacks, error conditions or result lifetime.

| Work group                   | Representative operations                                                     | Main qualification question                        |
|------------------------------|-------------------------------------------------------------------------------|----------------------------------------------------|
| Construction/conversion      | `of`, `copyOf`, `asList`, `toList`, `toSet`, `toMap`, `toTypedArray`          | Duplicates, null, snapshot/backing, order          |
| Map/flatten                  | `map`, `mapIndexed`, `mapMulti`, `flatMap`                                    | Mapping order, index, output cardinality, Callback |
| Filter/truncation            | `filter`, `take`, `drop`, `takeWhile`, `dropWhile`                            | Guaranteed prefix vs legal arbitrary subset        |
| Distinct/Set-like            | `distinct`, `distinctBy`, `union`, `intersect`, `subtract`, `plus`, `minus`   | Relation, multiplicity, retained representative    |
| Zip/neighbor                 | `zip`, `zipWithNext`                                                          | Correspondence and mismatched lengths              |
| Group/partition              | `groupBy`, `groupingBy`, `partition`, `partitioningBy`                        | Group existence, empty group, terminal completion  |
| Key/value mapping            | `associate`, `associateBy`, `mapKeys`, `mapValues`, `toMap`                   | Duplicate key and first/last/merge policy          |
| Sort/selection               | `sorted`, `sortWith`, `minBy`, `maxWith`, `findAny`                           | Comparator, ties, result selection                 |
| Aggregation                  | `count`, `sum`, `average`, `reduce`, `fold`, `collect`                        | Numeric result, order/effects, source elision      |
| Prefix/window                | `runningFold`, `runningReduce`, `scan`, `windowed`, `chunked`                 | Intermediate values and view lifetime              |
| Lookup/default               | `first`, `single`, `get`, `getOrDefault`, `getOrPut`, `compute`               | Source null, absence, callback, partiality         |
| Mutation/bulk                | `addAll`, `removeIf`, `replaceAll`, `merge`, `drainTo`                        | Prior changes surviving error, reentrancy          |
| Views/copies                 | `slice`, `subList`, `reversed`, `keySet`, `values`, `copyOfRange`             | Origin alias, backing, stability                   |
| Arrays/comparison/randomness | `fill`, `copyInto`, `contentEquals`, `contentDeepEquals`, `random`, `shuffle` | Reference vs structural equality, random effects   |
| Parallel/concurrent          | `parallel`, `forEach`, concurrent Map calls                                   | Allowed schedules, guarantees, atomicity           |

## 5.1. Exact-callable counterexample register

| Case                                                   | Observed difference                                                                      | Required result of Qualification                        |
|--------------------------------------------------------|------------------------------------------------------------------------------------------|---------------------------------------------------------|
| Java `Set.of` vs `Set.copyOf`                          | Duplicate rejection vs permitted retained representative                                 | Different construction/duplicate law                    |
| Java `Map.of` vs Kotlin `mapOf`                        | Duplicate-key rejection vs last-value construction policy                                | Language/overload source semantics, not generic Map law |
| Kotlin `distinctBy` vs `associateBy`                   | First representative vs last Value for key                                               | Choice/merge position and source Encounter Order        |
| Kotlin `Iterable.minus(element)` vs `.minus(elements)` | Remove first equal occurrence vs exclude all matching occurrences                        | Receiver and parameter overload distinguished           |
| Kotlin `Sequence.minus(...)`                           | Lazy evaluation/retraversal may observe a different source                               | Deferred consumption required                           |
| Kotlin `mapKeys`                                       | Transformed Key collision replaces earlier associated value                              | Preserve source replacement and Key/Value relation      |
| `groupBy` vs `groupingBy`                              | Eager Map vs deferred Grouping source                                                    | Different completion and evaluation time                |
| Downstream Collector `filtering` vs upstream `filter`  | Existing empty group vs omitted group                                                    | Group presence preserved                                |
| `runningFold` vs `fold`                                | Prefix sequence vs final accumulator                                                     | F20 vs final result; mutable accumulator alias          |
| Access-order `LinkedHashMap.get`                       | Read can change encounter order                                                          | P11 state effect                                        |
| `Map.compute` failure                                  | Mapping may remain unchanged but callback can change another object                      | Map effect vs external callback effect                  |
| Kotlin `mapTo`                                         | Adds incrementally to existing destination                                               | Intermediate read-your-writes and partial failure       |
| `TreeSet` comparator vs `HashSet` equality             | Comparator-zero versus `equals` discrimination                                           | No host-global collection equality                      |
| `IdentityHashMap`                                      | Reference identity used for Key                                                          | Source identity distinct from Contract Identity         |
| `Map.keySet/values/entrySet`                           | Membership, Bag projection, Entry composition                                            | F04/F03/F09 with E06                                    |
| `PriorityQueue.peek` vs iterator                       | Priority head not sorted traversal                                                       | P07 + E01                                               |
| `DelayQueue.peek` vs `poll`                            | Unexpired head visible but cannot be removed                                             | P10 time eligibility                                    |
| `SynchronousQueue.put`                                 | Direct handoff, no stored members                                                        | P09 rendezvous completion                               |
| `ConcurrentHashMap.computeIfAbsent`                    | Atomicity and recursion requirements differ from Map default                             | Concrete target qualification                           |
| `CopyOnWriteArrayList.iterator`                        | Stable snapshot view of prior list contents                                              | E07                                                     |
| `WeakHashMap` traversal                                | GC-sensitive Membership                                                                  | E09 acquisition evidence                                |
| Kotlin `buildList`/`buildMap`                          | Temporary Builder Receiver not valid outside Action                                      | Lifetime distinct from result                           |
| Kotlin `windowed(transform)`                           | Callback-scoped temporary Window can be reused                                           | Lifetime/escape requirements                            |
| Java Stream `count` / `peek`                           | Permitted elision of intermediate actions                                                | Must not invent obligatory Callback                     |
| Java Stream `findAny`                                  | Any permitted element, unspecified selection                                             | Default deterministic refinement                        |
| Unordered Stream `takeWhile` / `dropWhile`             | Source can permit different subsets, not merely reordered results                        | Refine result membership legally                        |
| Java `Set.copyOf`                                      | If duplicate equal instances differ in other fields, retained representative unspecified | Result observation and lawful tie choice                |
| `BitSet.cardinality/length/size`                       | Set-bit count vs extent vs storage capacity                                              | Representation observation distinct from F06 count      |
| Kotlin `getOrPut`/`getOrPutIfMissing`                  | Present-null vs missing-key default behavior                                             | P12 exact SDK/experimental profile                      |
| Kotlin `random` / `shuffled(random)`                   | Randomness is selected computation, not mere unspecified outcome                         | Preserve Random state and effects                       |
| Java `Collections.shuffle`                             | Explicit randomized permutation                                                          | No fixed representative substitution by default         |
| Java 27 `Map.ofLazy`                                   | `equals/hashCode/toString` can trigger deferred evaluation                               | E05 object method triggers/failure states               |
| Java 27 `List.ofLazy` / `Set.ofLazy`                   | Element / Membership may be unevaluated or failed                                        | Complete-result vs partial observation                  |

## 5.2. Ordinary source API versus implementation-specific guarantee

A default interface method may have weaker mutation atomicity than a concrete collection implementation. For example,
separate `removeIf` implementations can leave different partial state at the same throwing predicate. Likewise a
concurrent map's computation may have stronger atomicity or recursive-update constraints than the default Map
operation. The exact selected target and profile decide which guarantee is actually present. A source implementation
quirk beyond the specification is not a Contract requirement.

## 5.3. Strictly separate stochastic operations and unspecified result selection

A source specification that promises encounter-first selection must be followed. A result such as `findAny`, an
unspecified duplicate representative, tie or unordered permissible subset is subject to lawful Kontrakt deterministic
refinement for governed judgments. This refinement must have a stable semantic choice rule independent of physical
hash layout, incidental traversal or scheduler history. Judgment invariance can remove the need to distinguish
physical choices, but it is not the entrance condition for refinement.

A call expressly using a pseudo-random generator is different: random source behavior, generator state and mandated
callbacks may be part of the selected calculation. Qualify `random`, `shuffle`, `shuffled(random)`, seeded and default
sources separately. In 1Ds that cannot close those determinants, the operation may be inadmissible even though its
outcomes belong to a finite set. Source-permitted incidental choice is not permission to erase a required random
source effect. The Numeric law owns any reduction-result accuracy and reordering permissions (ADR-0078).

---

# 6. Exact Callable Interpretation and Mapping Design

## 6.1. Required qualification record

| Field group        | Minimum content                                                                                                                                              |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Source identity    | Language + version, SDK artifact/version, exact callable and overload, receiver, visibility/inheritance                                                      |
| Resolution         | Imports/extension-vs-member selection, generics, variance, nullable receiver, primitive/unsigned types, default/named args, bridge/expect/actual as relevant |
| Result             | Value/Composite family, constituents, emptiness, duplicate policy, index/order, key/value/group relations                                                    |
| Source relation    | Actual comparison, comparator, reference identity, custom Key and representative rules                                                                       |
| Evaluation         | Immediate/deferred, short circuit, Callback invocation/omission, terminal, recursion, one-shot                                                               |
| Effects            | Existing destination, external callback effects, alias/backed view, state changes on read                                                                    |
| Failure/completion | Constructor rejection, exceptions, partial Destination, per-constituent error, termination/resources                                                         |
| Context            | Concurrency, GC, time, locale/provider where actually observed; random source state; Preview/Experimental                                                    |
| Mapping            | F/P/E candidate and proof obligations; whether there is a lossy conversion or unjustified new observation                                                    |
| Owning 1D          | Permitted forms and exact domain, Source closure, deterministic refinement, judgment and Material owner                                                      |
| Evidence           | Spec section, source file and signature, differential fixture, conformance status, unresolved discrepancy                                                    |

A source path or compiler cache key is not the semantic identity of a Contract definition. A row carrying a class
and method name is not a resolved overload. The Kotlin generated extension surface requires correct K2 receiver and
argument binding; a Java interface method can be inherited, overridden or specialized in a concrete implementation.

## 6.2. Mapping example: `Map.values()`

The Source's Map Value View retains values associated with every present mapping, including duplicates. Its legal
Encounter Order depends on the source Map; its live/backed nature is independent of the multiplicity structure. The
mapping therefore includes **F03 + E06** for an unordered live Values View and **F03 + source-defined order + E06**
when an exact Map supports that order. Only after the selected view is consumed and a complete lawful result is formed
may an owning 1D establish a stable value. An arbitrary sort to fabricate an intrinsic index would change meaning.

## 6.3. Mapping example: an indexed bitmap representation observation

`BitSet.cardinality()` counts set bits, `length()` provides highest set bit plus one, while `size()` can expose the
storage capacity in bits. Two BitSets can have the same Membership and cardinality while differing in `size()` due to
capacity. The `size()` observation is a qualified *platform representation observation*, not F06 member cardinality,
Contract value Identity, or a reason to store every Contract Membership in a BitSet. An owning 1D requiring
representation-independent meaning may reject that operation. A Java 27 source/profile check is needed before
stating exact support; do not use a Java 21 runtime as JDK 27 proof.

## 6.4. Mapping example: `Sequence` versus `Iterable` transform

Kotlin `Iterable.map` forms an eager List, while Kotlin `Sequence.map` defers execution until consumption. If a
subsequent legal calculation consumes no items, a deferred Transform may not run at all whereas an eager Transform
already did. The compiler must preserve each qualified Source behavior, required effects and failure reachability.
Compiler Product reuse does not justify replaying an already consumed one-shot Source, or replaying an
Operation that modified external state.

## 6.5. Compiler protocol and realization boundary

The Frontend resolves and qualifies Source meaning before Resolved Contract HIR. The HIR carries the legal meaning
required by its subjects and owning 1D rather than a Java object graph or a stringified catalog row. Establishment
owns authority over Definition material; SDK qualification cannot ratify an unresolved Source call by executing it.
At runtime, acquisition and Ingress must form a lawful stable Input presentation before Input judgment; a live view or
one-shot stream is not that presentation by itself. Lowering and Fact authority remain separate. User JVM Operations
may use platform Collection behavior and external systems under their own realization and protection laws.

The implementation may use an interner, primitive slabbing, query/cache, structural sharing, specialization or other
replaceable physical representation. No chosen F/P/E code is required to be a HIR tag, JVM class, metadata handle,
transaction manager or optimization pass. Internal compiler scratch state is governed by its internal correctness
contracts, not the user's 1D Collection authority.

---

# 7. Failure, Protected Observation and Security Interfaces

The G09-A2 investigation established that a partially processed Collection is not automatically a completed
Collection result. Independent successfully established results survive unrelated failure unless a separate governing
protected Scope law applies. If a protected change is withdrawn, its legally relevant Collection observation includes
presence/absence, multiplicity, order, key/value correspondence, representative and admitted nested constituents,
not merely count or byte equality. These are Contract meaning requirements in ADR-0081 §§3.4–3.5; this Design does
not add a transaction or select a rollback implementation.

A source-mutating transformation may require **read-your-writes within its ongoing calculation**. In particular, if
a Callback reads the Destination after preceding items have been added, deferring all additions until final commit
would change its source behavior. Yet an unrelated observer may be forbidden to see those pending effects by the
owning Scope law. Exact source and alias qualification must reveal both obligations. Unsupported participation in a
protected execution is distinct from making the normal unprotected JVM call illegal. SADR-0002 holds threat and fault
injection cases for dirty reads, cascading abort, alias escape, stale generation, incomplete material and external
transaction mismatch; this Design consumes those assurance requirements.

A `void` or `Unit` Operation can change state without exposing an Input or outward Result. Its source effects are
not inferred from a return value. Result equality is not permission to omit, duplicate or replay a mandatory
source effect. A Rollback in a Collection alone need not undo a Callback's external state, DB operation, message send
or nonparticipating Alias. Participation, external transaction adoption, scope and failure propagation remain with
Failure, Governance, Transaction and Realization owners.

---

# 8. V1/V2 Compiler Preservation and Optimization Design

The compiler may evaluate a complete Collection, reuse a valid product, or incrementally maintain it. ADR-0075 owns
currentness and Early Cutoff; ADR-0063 owns Established World meaning. This Design compares implementations without
stating their scheduling, propagation graph or storage layout as a user Collection Contract.

| Technique / case                 | Proof obligation or design check                                                               |
|----------------------------------|------------------------------------------------------------------------------------------------|
| Clean recompute                  | Produces the lawful selected source and owning Contract observations                           |
| Frozen / HID-backed reuse        | Retained bytes/handles do not prove current Contract Identity, Membership or sufficient basis  |
| Pull validation                  | Detect current meaning determinants, including absence and complete-set dependencies           |
| Push frontier                    | Propagate changed meaning without equating physical reachability with Rollback scope           |
| Delta maintenance                | Preserve multiplicity, group presence, keys, representatives and nested relations              |
| Early Cutoff                     | Stop propagation only after current legally observable result equivalence is justified         |
| Fusion / E-graph                 | Preserve required callback, failure and effect observations, not just final value              |
| SIMD / parallel reduction        | Numeric result law and any mandated process observation remain intact                          |
| Cancellation / failure injection | Prevent invalid pending results or stale worker outputs from becoming current                  |
| External effects                 | Separate internal recompute/reuse from actual user Operation retries and external participants |

V1 may use its existing Query-oriented architecture, Frozen Material and HID where lawful. V2 remains free to use
pull, push, hybrid, delta, differential or full-recompute choices, and to change the internal storage model. Primitive
Slabbing, buffers, generational ordinals, caches and rollback algorithms are Design/SADR evidence and implementation
choices. They are **not** families, Contract Identity, or mandatory user-visible fields.

---

# 9. Conformance and Evidence Gates

## 9.1. Qualification gates

1. **Inventory completeness.** Produce Java 27 signature-accurate public and inherited API surfaces (including
   interfaces, default methods, specialized arrays and Preview methods) and Kotlin 2.4.21 callable bindings under
   actual K2 resolution, receiver overloads, JVM interop, unsigned and generated extensions. Remove raw parser
   false positives and record exclusions.
2. **Semantic completeness.** For each relevant *exact overload*, record the source law, necessary observations,
   effects, errors, closure, result composition, deterministic refinement and 1D eligibility. Uninterpretable calls
   remain unqualified, not invented as generic List/Set/Map equivalents.
3. **Family completeness.** Check whether every supported outcome is representable by F01–F21 and the applicable
   P/E/source distinction. Expand the model when a legal observation is lost, or demonstrate its observational
   equivalence to an existing composition before merging.
4. **Differential and version conformance.** Verify a precise source SDK profile against correct reference
   behavior where an executable environment exists; for newer Preview APIs use documented law and tested fixtures
   only when the requisite JDK/compiler is available.
5. **Owning 1D admissibility.** Verify Input, Admission, Fact, Invariant or other legal consumers independently of
   Source Mapping. Contract-only computations must meet the actual owning domain and totality requirements.
6. **Optimizer and incremental preservation.** Compare Clean, cached, fused, parallel and incrementally repaired
   outcomes under the same legal determinants and mandatory source effects; distinguish allowed nondeterministic
   Source alternatives from Contract's default deterministic refinement.

## 9.2. Essential conformance fixtures

| Fixture                                           | Failure to detect                                                        |
|---------------------------------------------------|--------------------------------------------------------------------------|
| Duplicate-sensitive Kotlin `minus` overloads      | First occurrence vs all occurrences silently conflated                   |
| `BitSet.cardinality`, `length`, `size`            | Member count vs extent vs capacity conflated                             |
| `Map.keySet`, `values`, `entrySet`                | Duplicate multiplicity or Entry identity lost                            |
| `Set.copyOf` retained representative              | Accidental JDK iteration gives unstable Contract selection               |
| `findAny` and unordered `takeWhile`               | Unspecified subset/representative mapped to ordered first                |
| Explicit `random` / `shuffle`                     | Required random source state/effect removed by refinement                |
| `TreeSet` comparator/`IdentityHashMap`            | Source discrimination imported as Contract Sameness                      |
| `groupBy` vs `groupingBy`, downstream `filtering` | Group existence and completion lost                                      |
| `Map.compute`, `mapTo`, `removeIf`                | Callback effects and partial Mutation misreported                        |
| Access-order Map read                             | A read effect silently omitted by optimization                           |
| JDK 27 Lazy Collection equals/hash/toString       | Implicit evaluation and failure state not tested                         |
| Kotlin Builder and Window escape                  | Temporary view used beyond allowed lifetime                              |
| One-shot Sequence/Stream                          | Reuse or replay changes failure and effects                              |
| Concurrent/GC-sensitive Collection                | Thread-safe point query confused with coherent snapshot                  |
| Nested and aliased elements                       | Established result continues to change after external mutation           |
| Protected internal read-your-writes               | Deferring Mutation changes required later Callback observation           |
| Rollback dependent result                         | Pending value or negative membership read reused after producer abort    |
| Whole World/Incremental repair                    | Newly applicable candidate or changed complete set bypasses invalidation |
| `Unit`/`void` state-changing Operation            | A missing return result hides the actual effect                          |
| Source Version upgrade                            | Unreviewed differences implicitly inherit old qualification              |

An implementation may use stronger conservative invalidation or recomputation. Fine-grained dependencies are a
performance choice, but absent or false evidence cannot justify a current Compiler Product. SADR-0002 owns the
security threat enumeration and fault injection program.

---

# 10. Work Packages and Open Decisions

| Package | Ownership and next work                                                                   | State                                                                |
|---------|-------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| G09     | Collection-specific failure, incompleteness, nondeterminism, external state               | General Contract direction investigated; exact SDK profiles pending  |
| G10–G14 | Value/composition families and operation/observation coverage                             | Draft semantic proposal; not all overloads verified                  |
| G15     | JDK 27 exact source-callable inventory and qualification                                  | Curated examples + raw candidates; exhaustive completion pending     |
| G16     | Kotlin 2.4.21 generated/receiver/overload qualification                                   | Curated examples + raw candidates; exhaustive completion pending     |
| G17–G18 | Frontend, HIR, Establishment, runtime acquisition/protocol mapping                        | Not closed in this Design                                            |
| G19–G20 | MIR, SDK lowering, optimization, cache, V1/V2 incremental validation                      | Not closed in this Design                                            |
| G21–G22 | Conformance, governance/ADR conflict audit, release acceptance                            | Not closed                                                           |
| G23     | Security/physical failure extraction, fault injection, currentness, dependent propagation | Tracked in SADR-0002 and Design/TODO; not a new Collection authority |

Open decisions requiring the actual owners include: reconciliation of ADR-0065 §10's wording about compiler
preference with ADR-0081's default **qualified** deterministic refinement; exact qualification of JDK 27 Preview
`ofLazy`, Kotlin Experimental `getOrPutIfNull`/`getOrPutIfMissing`; permissible controlled stochastic Source
calculations per 1D; signature-accurate public/inherited SDK coverage; selected source-specific relation proofs; and
actual compiler-protocol bindings of the legal meaning.

Nothing in these open decisions changes the already agreed protected-change direction: Kontrakt-controlled protected
mutations and explicitly integrated participants can be subject to owner-defined Rollback Scope, while unrelated
external JVM state does not become automatically transactional.

# 11. References and Source Artifact Provenance

| Artifact                                                                                        | Role and authority                                                                                  |
|-------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| `docs/adr/current/contract/data-types/0081-collections-and-structured-data.md`                  | Owning Draft Collection Contract law                                                                |
| `docs/adr/current/contract/one-dimensional/0065-admission-contract-continuation-judgment.md`    | Accepted Admission totality and source-use limits                                                   |
| ADR-0064, ADR-0063, ADR-0071, ADR-0073, ADR-0075, ADR-0078, ADR-0079                            | Existing owning semantic, result and realization laws                                               |
| ADR-0084                                                                                        | Source Library Semantic Acquisition / Interpretation law                                            |
| `docs/sadr/0002-collection-observation-integrity-and-protected-change-security-architecture.md` | Security threats and assurance, not Contract semantics                                              |
| `/mnt/data/src.zip`                                                                             | Supplied JDK 27 source archive; version profile requires independent signature check                |
| `/mnt/data/java.base.jmod`                                                                      | Supplied JDK module binary artifact; binary/ABI check still pending                                 |
| `/mnt/data/kotlin-stdlib-2.4.21-sources.jar`                                                    | Supplied Kotlin 2.4.21 sources                                                                      |
| `/mnt/data/kotlin-compiler-2.4.21.zip`                                                          | Supplied compiler archive; exact K2 resolution not yet proven                                       |
| `G10-G14_java27_kotlin2421_source_coverage_index_20261110.md`                                   | Prior raw package/file census, reproduced in Appendix B                                             |
| `G15-G16_sdk_semantic_qualification_research_audit_20261110.md`                                 | Prior representative investigation and identified gaps                                              |
| `G15-G16_representative_semantic_mapping_20261110.csv`                                          | Source mapping rows, reproduced in Appendix A                                                       |
| `G15-G16_source_declaration_candidates_20261110.csv`                                            | 11,975 regex candidate rows; separately retained raw navigation data, not an SDK public API catalog |

The machine-specific `/mnt/data` paths identify evidence files from the supplied session only. Repository integration
must place or reference these inputs through the project's normal Design/TODO evidence mechanism; no runtime product
may depend on these paths.

---

# Appendix A. Representative Java/Kotlin Source Mapping (102 audit rows)

These rows preserve the earlier G15–G16 mapping candidates. **None is a completed overload qualification.** File-line
values are source navigation hints and can point to comments or other mentions of an operation. `F`, `P`, `E`
identifiers refer solely to this Design's catalog; `Source match = true` means the operation name was found, not that
all declarations were resolved.

| ID   | Language / owner                                  | Operation              | Source path:line                                                 | F/P/E / qualifier                                  | Semantic concern                                                  | Status                 |
|------|---------------------------------------------------|------------------------|------------------------------------------------------------------|----------------------------------------------------|-------------------------------------------------------------------|------------------------|
| Q001 | Java `java.util.List`                             | `ofLazy`               | `java.base/java/util/List.java:96`                               | F02+E05 / Preview                                  | per-index deferred evaluation/error state                         | Not overload-qualified |
| Q002 | Java `java.util.List`                             | `copyOf`               | `java.base/java/util/List.java:1195`                             | F02 / Signature profile                            | source iteration-order copy; shallow elements                     | Not overload-qualified |
| Q003 | Java `java.util.List`                             | `subList`              | `java.base/java/util/List.java:111`                              | F02+E06 / Signature profile                        | backed view and validity after structural change                  | Not overload-qualified |
| Q004 | Java `java.util.List`                             | `reversed`             | `java.base/java/util/List.java:895`                              | F02+E06 / Signature profile                        | reverse-backed view not stable copy                               | Not overload-qualified |
| Q005 | Java `java.util.List`                             | `sort`                 | `java.base/java/util/List.java:510`                              | F02+P01 / Signature profile                        | stable sort, comparator, in-place effects                         | Not overload-qualified |
| Q006 | Java `java.util.Set`                              | `ofLazy`               | `java.base/java/util/Set.java:824`                               | F04+E05 / Preview                                  | candidate membership state; recursive failure; preview            | Not overload-qualified |
| Q007 | Java `java.util.Set`                              | `of`                   | `java.base/java/util/Set.java:72`                                | F04 / Overload family                              | duplicate/null rejection vs unspecified order                     | Not overload-qualified |
| Q008 | Java `java.util.Set`                              | `copyOf`               | `java.base/java/util/Set.java:733`                               | F04 / Signature profile                            | duplicate representative unspecified and no order                 | Not overload-qualified |
| Q009 | Java `java.util.Map`                              | `ofLazy`               | `java.base/java/util/Map.java:121`                               | F05+E05 / Preview                                  | key set snapshot; per-value failures; object methods may evaluate | Not overload-qualified |
| Q010 | Java `java.util.Map`                              | `of`                   | `java.base/java/util/Map.java:118`                               | F05 / Overload family                              | null/key collision rejection and unspecified order                | Not overload-qualified |
| Q011 | Java `java.util.Map`                              | `copyOf`               | `java.base/java/util/Map.java:630`                               | F05 / Signature profile                            | source mapping snapshot vs mutable element references             | Not overload-qualified |
| Q012 | Java `java.util.Map`                              | `compute`              | `java.base/java/util/Map.java:1167`                              | F05+P03 / Signature profile                        | callback effects vs mapping update on failure                     | Not overload-qualified |
| Q013 | Java `java.util.Map`                              | `computeIfAbsent`      | `java.base/java/util/Map.java:1004`                              | F05+P12 / Signature profile                        | absent/present-null semantics                                     | Not overload-qualified |
| Q014 | Java `java.util.Map`                              | `merge`                | `java.base/java/util/Map.java:1168`                              | F05+P03 / Signature profile                        | remap callback, null removal and failure                          | Not overload-qualified |
| Q015 | Java `java.util.Map`                              | `keySet`               | `java.base/java/util/Map.java:358`                               | F04+E06 / Signature profile                        | live membership projection                                        | Not overload-qualified |
| Q016 | Java `java.util.Map`                              | `values`               | `java.base/java/util/Map.java:217`                               | F03+E06 / Signature profile                        | duplicate-preserving live values projection                       | Not overload-qualified |
| Q017 | Java `java.util.Map`                              | `entrySet`             | `java.base/java/util/Map.java:393`                               | F04+F09+E06 / Signature profile                    | entries and backed-entry lifetime                                 | Not overload-qualified |
| Q018 | Java `java.util.Map.Entry`                        | `copyOf`               | `java.base/java/util/Map.java:630`                               | F09 / Signature profile                            | independent entry snapshot vs backed entry                        | Not overload-qualified |
| Q019 | Java `java.util.Collection`                       | `removeIf`             | `java.base/java/util/Collection.java:576`                        | P04 / Signature profile                            | partial change depends on implementation and callback failure     | Not overload-qualified |
| Q020 | Java `java.util.SequencedCollection`              | `reversed`             | `java.base/java/util/SequencedCollection.java:90`                | F01+E06 / Signature profile                        | reverse sequence / backed view                                    | Not overload-qualified |
| Q021 | Java `java.util.SequencedMap`                     | `sequencedEntrySet`    | `java.base/java/util/SequencedMap.java:54`                       | F14+E06 / Signature profile                        | ordered entry view and liveness                                   | Not overload-qualified |
| Q022 | Java `java.util.SortedSet`                        | `comparator`           | `java.base/java/util/SortedSet.java:118`                         | F13 / Signature profile                            | ordering basis, equality mismatch                                 | Not overload-qualified |
| Q023 | Java `java.util.NavigableMap`                     | `floorEntry`           | `java.base/java/util/NavigableMap.java:146`                      | F15 / Signature profile                            | neighbor selection and comparator                                 | Not overload-qualified |
| Q024 | Java `java.util.NavigableMap`                     | `subMap`               | `java.base/java/util/NavigableMap.java:56`                       | F15+E06 / Overload family                          | bounded live view / range                                         | Not overload-qualified |
| Q025 | Java `java.util.LinkedHashMap`                    | `get`                  | `java.base/java/util/LinkedHashMap.java:535`                     | F14+P11 / Implementation selected                  | access-order read is also a reordering                            | Not overload-qualified |
| Q026 | Java `java.util.IdentityHashMap`                  | `containsKey`          | `java.base/java/util/IdentityHashMap.java:362`                   | F05 / Implementation selected                      | source reference identity not Contract identity                   | Not overload-qualified |
| Q027 | Java `java.util.WeakHashMap`                      | `size`                 | `java.base/java/util/WeakHashMap.java:261`                       | F05+E09 / Implementation selected                  | GC-sensitive membership and size                                  | Not overload-qualified |
| Q028 | Java `java.util.BitSet`                           | `size`                 | `java.base/java/util/BitSet.java:1053`                           | F06+representation observation / Signature profile | storage capacity, not member cardinality                          | Not overload-qualified |
| Q029 | Java `java.util.BitSet`                           | `length`               | `java.base/java/util/BitSet.java:293`                            | F06 / Signature profile                            | highest set bit + 1, not count                                    | Not overload-qualified |
| Q030 | Java `java.util.BitSet`                           | `cardinality`          | `java.base/java/util/BitSet.java:901`                            | F06 / Signature profile                            | count of set bits                                                 | Not overload-qualified |
| Q031 | Java `java.util.Arrays`                           | `asList`               | `java.base/java/util/Arrays.java:4159`                           | F02+E06 / Signature profile                        | fixed-size array backed List                                      | Not overload-qualified |
| Q032 | Java `java.util.Arrays`                           | `parallelSort`         | `java.base/java/util/Arrays.java:398`                            | F02+sort / Overload family                         | partition/parallel algorithm with ordering guarantee              | Not overload-qualified |
| Q033 | Java `java.util.Arrays`                           | `parallelPrefix`       | `java.base/java/util/Arrays.java:1383`                           | F20+P04 / Overload family                          | in-place prefix with associative operation expectation            | Not overload-qualified |
| Q034 | Java `java.util.Collections`                      | `unmodifiableList`     | `java.base/java/util/Collections.java:1477`                      | F02+E06 / Signature profile                        | read-only backed live view                                        | Not overload-qualified |
| Q035 | Java `java.util.Collections`                      | `synchronizedList`     | `java.base/java/util/Collections.java:2670`                      | F02+E08 / Signature profile                        | external synchronization for traversal                            | Not overload-qualified |
| Q036 | Java `java.util.Collections`                      | `checkedList`          | `java.base/java/util/Collections.java:3844`                      | F02+E06 / Signature profile                        | runtime insertion checks vs contents through aliased original     | Not overload-qualified |
| Q037 | Java `java.util.Collections`                      | `shuffle`              | `java.base/java/util/Collections.java:432`                       | F02+randomness effect / Overload family            | explicit randomized permutation or provided Random state          | Not overload-qualified |
| Q038 | Java `java.util.PriorityQueue`                    | `peek`                 | `java.base/java/util/PriorityQueue.java:249`                     | P07 / Implementation selected                      | priority head, unspecified tie                                    | Not overload-qualified |
| Q039 | Java `java.util.PriorityQueue`                    | `iterator`             | `java.base/java/util/PriorityQueue.java:60`                      | P07+E01 / Implementation selected                  | not priority sorted traversal                                     | Not overload-qualified |
| Q040 | Java `java.util.Queue`                            | `poll`                 | `java.base/java/util/Queue.java:67`                              | P05 / Signature profile                            | empty returns null vs remove throws                               | Not overload-qualified |
| Q041 | Java `java.util.Deque`                            | `removeFirst`          | `java.base/java/util/Deque.java:84`                              | P06 / Signature profile                            | end-specific removal and failure                                  | Not overload-qualified |
| Q042 | Java `java.util.concurrent.BlockingQueue`         | `take`                 | `java.base/java/util/concurrent/BlockingQueue.java:75`           | P08 / Signature profile                            | waits/interruption vs nonblocking poll                            | Not overload-qualified |
| Q043 | Java `java.util.concurrent.BlockingQueue`         | `drainTo`              | `java.base/java/util/concurrent/BlockingQueue.java:349`          | P08+P04 / Overload family                          | bulk transfer may fail partly                                     | Not overload-qualified |
| Q044 | Java `java.util.concurrent.TransferQueue`         | `transfer`             | `java.base/java/util/concurrent/TransferQueue.java:105`          | P09 / Signature profile                            | rendezvous with matching consumer                                 | Not overload-qualified |
| Q045 | Java `java.util.concurrent.SynchronousQueue`      | `put`                  | `java.base/java/util/concurrent/SynchronousQueue.java:263`       | P09 / Implementation selected                      | no stored value; handoff completion                               | Not overload-qualified |
| Q046 | Java `java.util.concurrent.DelayQueue`            | `poll`                 | `java.base/java/util/concurrent/DelayQueue.java:73`              | P10 / Implementation selected                      | unexpired head is not pollable                                    | Not overload-qualified |
| Q047 | Java `java.util.concurrent.CopyOnWriteArrayList`  | `iterator`             | `java.base/java/util/concurrent/CopyOnWriteArrayList.java:1065`  | E07 / Implementation selected                      | snapshot iterator vs current list changes                         | Not overload-qualified |
| Q048 | Java `java.util.concurrent.ConcurrentHashMap`     | `computeIfAbsent`      | `java.base/java/util/concurrent/ConcurrentHashMap.java:143`      | P03+E08 / Implementation selected                  | atomic invocation and recursive-update restriction                | Not overload-qualified |
| Q049 | Java `java.util.concurrent.ConcurrentSkipListMap` | `subMap`               | `java.base/java/util/concurrent/ConcurrentSkipListMap.java:1906` | F15+E08 / Implementation selected                  | concurrent navigable range view                                   | Not overload-qualified |
| Q050 | Java `java.util.Spliterator`                      | `trySplit`             | `java.base/java/util/Spliterator.java:97`                        | E02 / Signature profile                            | split semantics and characteristics                               | Not overload-qualified |
| Q051 | Java `java.util.stream.Stream`                    | `findAny`              | `java.base/java/util/stream/Stream.java:1417`                    | E03 / Signature profile                            | source-permitted representative choice                            | Not overload-qualified |
| Q052 | Java `java.util.stream.Stream`                    | `findFirst`            | `java.base/java/util/stream/Stream.java:1397`                    | E03 / Signature profile                            | encounter-first where ordered                                     | Not overload-qualified |
| Q053 | Java `java.util.stream.Stream`                    | `takeWhile`            | `java.base/java/util/stream/Stream.java:740`                     | E03 / Signature profile                            | unordered subset freedom; ordered prefix                          | Not overload-qualified |
| Q054 | Java `java.util.stream.Stream`                    | `dropWhile`            | `java.base/java/util/stream/Stream.java:806`                     | E03 / Signature profile                            | ordered suffix vs unordered subset                                | Not overload-qualified |
| Q055 | Java `java.util.stream.Stream`                    | `unordered`            | `java.base/java/util/stream/BaseStream.java:141`                 | E03 / Signature profile                            | discard encounter order                                           | Not overload-qualified |
| Q056 | Java `java.util.stream.Stream`                    | `count`                | `java.base/java/util/stream/Stream.java:77`                      | E03 / Signature profile                            | pipeline and callback elision                                     | Not overload-qualified |
| Q057 | Java `java.util.stream.Stream`                    | `forEachOrdered`       | `java.base/java/util/stream/Stream.java:870`                     | E03 / Signature profile                            | mandatory encounter-order terminal effects                        | Not overload-qualified |
| Q058 | Java `java.util.stream.Stream`                    | `gather`               | `java.base/java/util/stream/Stream.java:1077`                    | E04 / Signature profile                            | gatherer invocation and life cycle                                | Not overload-qualified |
| Q059 | Java `java.util.stream.Collectors`                | `toMap`                | `java.base/java/util/stream/Collectors.java:1394`                | F05+E04 / Overload family                          | duplicate key failure/merge/supplier overloads                    | Not overload-qualified |
| Q060 | Java `java.util.stream.Collectors`                | `groupingBy`           | `java.base/java/util/stream/Collectors.java:92`                  | F16+E04 / Overload family                          | group existence and downstream collector                          | Not overload-qualified |
| Q061 | Java `java.util.stream.Collectors`                | `groupingByConcurrent` | `java.base/java/util/stream/Collectors.java:992`                 | F16+E04+E08 / Overload family                      | concurrency/unordered group effects                               | Not overload-qualified |
| Q062 | Java `java.util.stream.Collectors`                | `partitioningBy`       | `java.base/java/util/stream/Collectors.java:101`                 | F17+E04 / Overload family                          | true/false partitions even when empty                             | Not overload-qualified |
| Q063 | Java `java.util.stream.Collectors`                | `filtering`            | `java.base/java/util/stream/Collectors.java:498`                 | F16+E04 / Overload family                          | downstream filtering preserves upstream group                     | Not overload-qualified |
| Q064 | Java `java.util.stream.Gatherers`                 | `windowFixed`          | `java.base/java/util/stream/Gatherers.java:68`                   | F19+E04 / Signature profile                        | window and stateful gathering                                     | Not overload-qualified |
| Q065 | Java `java.util.stream.Gatherers`                 | `mapConcurrent`        | `java.base/java/util/stream/Gatherers.java:350`                  | E04+E08 / Signature profile                        | bounded concurrent mapping                                        | Not overload-qualified |
| Q066 | Java `java.util.concurrent.Flow.Publisher`        | `subscribe`            | `java.base/java/util/concurrent/Flow.java:70`                    | E10 / Signature profile                            | backpressure, demand, termination                                 | Not overload-qualified |
| Q067 | Java `java.nio.ByteBuffer`                        | `slice`                | `java.base/java/nio/ByteBuffer.java:605`                         | E11 / Signature profile                            | position/limit and backing storage sharing                        | Not overload-qualified |
| Q068 | Kotlin `kotlin.collections.Iterable`              | `map`                  | `commonMain/generated/_Collections.kt:1744`                      | F02+transform / Receiver overload                  | eager list and callback evaluation                                | Not overload-qualified |
| Q069 | Kotlin `kotlin.sequences.Sequence`                | `map`                  | `commonMain/generated/_Sequences.kt:1263`                        | E03+transform / Receiver overload                  | deferred execution                                                | Not overload-qualified |
| Q070 | Kotlin `kotlin.collections.Iterable`              | `minus`                | `commonMain/generated/_Collections.kt:3581`                      | F02+subtract / Overload family                     | single element removes first; collection removes all              | Not overload-qualified |
| Q071 | Kotlin `kotlin.sequences.Sequence`                | `minus`                | `commonMain/generated/_Sequences.kt:3050`                        | E03+subtract / Overload family                     | deferred single-element vs multi-element removal                  | Not overload-qualified |
| Q072 | Kotlin `kotlin.collections.Iterable`              | `mapTo`                | `commonMain/generated/_Collections.kt:1745`                      | P04 / Receiver overload                            | mutates destination incrementally                                 | Not overload-qualified |
| Q073 | Kotlin `kotlin.collections.Iterable`              | `filterTo`             | `commonMain/generated/_Collections.kt:777`                       | P04 / Receiver overload                            | destination alias, callback failure                               | Not overload-qualified |
| Q074 | Kotlin `kotlin.collections.Iterable`              | `associateBy`          | `commonMain/generated/_Collections.kt:1378`                      | F05 / Overload family                              | last matching key retains value                                   | Not overload-qualified |
| Q075 | Kotlin `kotlin.collections.Map`                   | `mapKeys`              | `commonMain/kotlin/collections/Maps.kt:624`                      | F05 / Receiver overload                            | later collision overwrites earlier                                | Not overload-qualified |
| Q076 | Kotlin `kotlin.collections.Iterable`              | `groupBy`              | `commonMain/generated/_Collections.kt:1670`                      | F16 / Receiver overload                            | eager grouping, group member order                                | Not overload-qualified |
| Q077 | Kotlin `kotlin.collections.Iterable`              | `groupingBy`           | `commonMain/generated/_Collections.kt:1731`                      | E04 / Receiver overload                            | grouping source deferred until eachCount/fold                     | Not overload-qualified |
| Q078 | Kotlin `kotlin.collections.Grouping`              | `eachCount`            | `jvmMain/kotlin/collections/GroupingJVM.kt:20`                   | E04+F16 / Receiver overload                        | terminal aggregate grouping                                       | Not overload-qualified |
| Q079 | Kotlin `kotlin.collections.Iterable`              | `windowed`             | `commonMain/generated/_Collections.kt:3556`                      | F19 / Overload family                              | transient window transform vs list result                         | Not overload-qualified |
| Q080 | Kotlin `kotlin.sequences.Sequence`                | `windowed`             | `commonMain/generated/_Sequences.kt:3021`                        | F19+E03 / Overload family                          | transient sliding-window buffer in transform                      | Not overload-qualified |
| Q081 | Kotlin `kotlin.collections.Iterable`              | `runningFold`          | `commonMain/generated/_Collections.kt:3296`                      | F20 / Receiver overload                            | successive accumulator values including initial                   | Not overload-qualified |
| Q082 | Kotlin `kotlin.sequences.Sequence`                | `runningFold`          | `commonMain/generated/_Sequences.kt:2744`                        | F20+E03 / Receiver overload                        | deferred prefix calculations                                      | Not overload-qualified |
| Q083 | Kotlin `kotlin.collections.Iterable`              | `distinctBy`           | `commonMain/generated/_Collections.kt:1854`                      | F12 / Receiver overload                            | first representative by encounter                                 | Not overload-qualified |
| Q084 | Kotlin `kotlin.collections.Map`                   | `withDefault`          | `commonMain/kotlin/collections/MapWithDefault.kt:39`             | F05+P12 / Receiver overload                        | default lookup wrapper vs stored mapping                          | Not overload-qualified |
| Q085 | Kotlin `kotlin.collections.MutableMap`            | `getOrPut`             | `commonMain/kotlin/collections/Maps.kt:459`                      | P12 / Receiver overload                            | present null vs default, effect and adoption                      | Not overload-qualified |
| Q086 | Kotlin `kotlin.collections.Map`                   | `buildMap`             | `commonMain/kotlin/collections/Maps.kt:153`                      | F05+builder lifetime / Overload family             | builder receiver invalid after action                             | Not overload-qualified |
| Q087 | Kotlin `kotlin.collections.Iterable`              | `buildList`            | `commonMain/kotlin/collections/Collections.kt:186`               | F02+builder lifetime / Overload family             | builder receiver invalid after action                             | Not overload-qualified |
| Q088 | Kotlin `kotlin.collections.List`                  | `asReversed`           | `commonMain/kotlin/collections/ReversedViews.kt:77`              | F02+E06 / Receiver overload                        | backed reversed view                                              | Not overload-qualified |
| Q089 | Kotlin `kotlin.collections.List`                  | `reversed`             | `commonMain/generated/_Collections.kt:1151`                      | F02 / Receiver overload                            | independent reversed result vs backed view                        | Not overload-qualified |
| Q090 | Kotlin `kotlin.collections.Array`                 | `asList`               | `jvmMain/generated/_ArraysJvm.kt:133`                            | F02+E06 / Receiver overload                        | array-backed list                                                 | Not overload-qualified |
| Q091 | Kotlin `kotlin.collections.Collection`            | `random`               | `commonMain/generated/_Collections.kt:561`                       | randomness effect / Overload family                | default random and explicit Random receiver                       | Not overload-qualified |
| Q092 | Kotlin `kotlin.collections.Iterable`              | `shuffled`             | `commonMain/kotlin/collections/Collections.kt:306`               | randomness effect / Overload family                | randomized order, optional Random                                 | Not overload-qualified |
| Q093 | Kotlin `kotlin.sequences.Sequence`                | `constrainOnce`        | `commonMain/kotlin/collections/Sequences.kt:31`                  | E03 / Receiver overload                            | one-shot producer cannot be replayed                              | Not overload-qualified |
| Q094 | Kotlin `kotlin.sequences.Sequence`                | `onEach`               | `commonMain/generated/_Sequences.kt:2608`                        | E03 / Receiver overload                            | callback effects on consumption                                   | Not overload-qualified |
| Q095 | Kotlin `kotlin.collections.ArrayDeque`            | `removeFirstOrNull`    | `commonMain/kotlin/collections/ArrayDeque.kt:161`                | P06 / Receiver overload                            | empty endpoint result vs throwing variants                        | Not overload-qualified |
| Q096 | Kotlin `kotlin.ranges.IntProgression`             | `step`                 | `commonMain/generated/_Ranges.kt:997`                            | F08 / Receiver overload                            | step magnitude and direction                                      | Not overload-qualified |
| Q097 | Kotlin `kotlin.collections.IntArray`              | `contentEquals`        | `commonMain/generated/_Arrays.kt:8244`                           | F02 / Receiver overload                            | element equality distinct from reference equality                 | Not overload-qualified |
| Q098 | Kotlin `kotlin.collections.Array`                 | `contentDeepEquals`    | `commonMain/generated/_Arrays.kt:8161`                           | F21 / Receiver overload                            | nested array equality, possibly cyclic references                 | Not overload-qualified |
| Q099 | Kotlin `kotlin.collections.Map`                   | `mapValues`            | `commonMain/kotlin/collections/Maps.kt:609`                      | F05 / Receiver overload                            | key set retained but values changed                               | Not overload-qualified |
| Q100 | Kotlin `kotlin.collections.MutableCollection`     | `minusAssign`          | `commonMain/kotlin/collections/MutableCollections.kt:84`         | P04 / Overload family                              | in-place minus resolution and repeated occurrences                | Not overload-qualified |
| Q101 | Kotlin `kotlin.collections.MutableMap`            | `getOrPutIfNull`       | `commonMain/kotlin/collections/Maps.kt:493`                      | P12 / Experimental                                 | present null invokes default and replaces mapped null             | Not overload-qualified |
| Q102 | Kotlin `kotlin.collections.MutableMap`            | `getOrPutIfMissing`    | `commonMain/kotlin/collections/Maps.kt:522`                      | P12 / Experimental                                 | absent key vs present-null difference                             | Not overload-qualified |

---

# Appendix B. Preserved G10–G14 Source Coverage Index

The following source-path census is reproduced as collected. It contains non-Collection neighboring files and is not
a filtered public signature inventory. The file paths, version scope, candidate counts and evidence limitations are
retained to prevent silent loss during the ADR/Design extraction.

## G10–G14 Java/Kotlin Collection Source Coverage Index

**Date:** 2026-11-10  
**Status:** Source-file census and coverage candidate register; **NOT** a completed callable-by-callable semantic
qualification.  
**Sources:** user-provided JDK 27 `src.zip` and Kotlin 2.4.21
`../../../../../Downloads/kotlin-stdlib-2.4.21-sources.jar`.

### Reading rules

- Top-level Java file counts include private/internal implementation files and package documentation; file count is not
  public API count.
- Kotlin function counts are approximate source-declaration-pattern matches, not an ABI, exported API, or exact overload
  count; aliases, generated code, expect/actual and inline functions require cross-reference.
- Not all files in packages below are collections (e.g. Calendar, Executors, regex). The census prevents silent omission
  when narrowing public surfaces.
- Sources in other JDK modules and outside Kotlin stdlib, where relevant to collection-like producer/consumer APIs, need
  separate scope decisions.
- Collection semantics, operation side effects, and 1D admissibility are separate decisions under ADR-0079 / ADR-0084 /
  receiving 1D.

### JDK 27: `java.util` (129 top-level `.java` files)

| Source file                                   | Exact path                                                        |
|-----------------------------------------------|-------------------------------------------------------------------|
| `AbstractCollection.java`                     | `java.base/java/util/AbstractCollection.java`                     |
| `AbstractList.java`                           | `java.base/java/util/AbstractList.java`                           |
| `AbstractMap.java`                            | `java.base/java/util/AbstractMap.java`                            |
| `AbstractQueue.java`                          | `java.base/java/util/AbstractQueue.java`                          |
| `AbstractSequentialList.java`                 | `java.base/java/util/AbstractSequentialList.java`                 |
| `AbstractSet.java`                            | `java.base/java/util/AbstractSet.java`                            |
| `ArrayDeque.java`                             | `java.base/java/util/ArrayDeque.java`                             |
| `ArrayList.java`                              | `java.base/java/util/ArrayList.java`                              |
| `ArrayPrefixHelpers.java`                     | `java.base/java/util/ArrayPrefixHelpers.java`                     |
| `Arrays.java`                                 | `java.base/java/util/Arrays.java`                                 |
| `ArraysParallelSortHelpers.java`              | `java.base/java/util/ArraysParallelSortHelpers.java`              |
| `Base64.java`                                 | `java.base/java/util/Base64.java`                                 |
| `BitSet.java`                                 | `java.base/java/util/BitSet.java`                                 |
| `Calendar.java`                               | `java.base/java/util/Calendar.java`                               |
| `Collection.java`                             | `java.base/java/util/Collection.java`                             |
| `Collections.java`                            | `java.base/java/util/Collections.java`                            |
| `ComparableTimSort.java`                      | `java.base/java/util/ComparableTimSort.java`                      |
| `Comparator.java`                             | `java.base/java/util/Comparator.java`                             |
| `Comparators.java`                            | `java.base/java/util/Comparators.java`                            |
| `ConcurrentModificationException.java`        | `java.base/java/util/ConcurrentModificationException.java`        |
| `Currency.java`                               | `java.base/java/util/Currency.java`                               |
| `Date.java`                                   | `java.base/java/util/Date.java`                                   |
| `Deque.java`                                  | `java.base/java/util/Deque.java`                                  |
| `Dictionary.java`                             | `java.base/java/util/Dictionary.java`                             |
| `DoubleSummaryStatistics.java`                | `java.base/java/util/DoubleSummaryStatistics.java`                |
| `DualPivotQuicksort.java`                     | `java.base/java/util/DualPivotQuicksort.java`                     |
| `DuplicateFormatFlagsException.java`          | `java.base/java/util/DuplicateFormatFlagsException.java`          |
| `EmptyStackException.java`                    | `java.base/java/util/EmptyStackException.java`                    |
| `EnumMap.java`                                | `java.base/java/util/EnumMap.java`                                |
| `EnumSet.java`                                | `java.base/java/util/EnumSet.java`                                |
| `Enumeration.java`                            | `java.base/java/util/Enumeration.java`                            |
| `EventListener.java`                          | `java.base/java/util/EventListener.java`                          |
| `EventListenerProxy.java`                     | `java.base/java/util/EventListenerProxy.java`                     |
| `EventObject.java`                            | `java.base/java/util/EventObject.java`                            |
| `FormatFlagsConversionMismatchException.java` | `java.base/java/util/FormatFlagsConversionMismatchException.java` |
| `Formattable.java`                            | `java.base/java/util/Formattable.java`                            |
| `FormattableFlags.java`                       | `java.base/java/util/FormattableFlags.java`                       |
| `Formatter.java`                              | `java.base/java/util/Formatter.java`                              |
| `FormatterClosedException.java`               | `java.base/java/util/FormatterClosedException.java`               |
| `GregorianCalendar.java`                      | `java.base/java/util/GregorianCalendar.java`                      |
| `HashMap.java`                                | `java.base/java/util/HashMap.java`                                |
| `HashSet.java`                                | `java.base/java/util/HashSet.java`                                |
| `Hashtable.java`                              | `java.base/java/util/Hashtable.java`                              |
| `HexFormat.java`                              | `java.base/java/util/HexFormat.java`                              |
| `IdentityHashMap.java`                        | `java.base/java/util/IdentityHashMap.java`                        |
| `IllegalFormatArgumentIndexException.java`    | `java.base/java/util/IllegalFormatArgumentIndexException.java`    |
| `IllegalFormatCodePointException.java`        | `java.base/java/util/IllegalFormatCodePointException.java`        |
| `IllegalFormatConversionException.java`       | `java.base/java/util/IllegalFormatConversionException.java`       |
| `IllegalFormatException.java`                 | `java.base/java/util/IllegalFormatException.java`                 |
| `IllegalFormatFlagsException.java`            | `java.base/java/util/IllegalFormatFlagsException.java`            |
| `IllegalFormatPrecisionException.java`        | `java.base/java/util/IllegalFormatPrecisionException.java`        |
| `IllegalFormatWidthException.java`            | `java.base/java/util/IllegalFormatWidthException.java`            |
| `IllformedLocaleException.java`               | `java.base/java/util/IllformedLocaleException.java`               |
| `ImmutableCollections.java`                   | `java.base/java/util/ImmutableCollections.java`                   |
| `InputMismatchException.java`                 | `java.base/java/util/InputMismatchException.java`                 |
| `IntSummaryStatistics.java`                   | `java.base/java/util/IntSummaryStatistics.java`                   |
| `InvalidPropertiesFormatException.java`       | `java.base/java/util/InvalidPropertiesFormatException.java`       |
| `Iterator.java`                               | `java.base/java/util/Iterator.java`                               |
| `JapaneseImperialCalendar.java`               | `java.base/java/util/JapaneseImperialCalendar.java`               |
| `JumboEnumSet.java`                           | `java.base/java/util/JumboEnumSet.java`                           |
| `KeyValueHolder.java`                         | `java.base/java/util/KeyValueHolder.java`                         |
| `LazyCollections.java`                        | `java.base/java/util/LazyCollections.java`                        |
| `LinkedHashMap.java`                          | `java.base/java/util/LinkedHashMap.java`                          |
| `LinkedHashSet.java`                          | `java.base/java/util/LinkedHashSet.java`                          |
| `LinkedList.java`                             | `java.base/java/util/LinkedList.java`                             |
| `List.java`                                   | `java.base/java/util/List.java`                                   |
| `ListIterator.java`                           | `java.base/java/util/ListIterator.java`                           |
| `ListResourceBundle.java`                     | `java.base/java/util/ListResourceBundle.java`                     |
| `Locale.java`                                 | `java.base/java/util/Locale.java`                                 |
| `LocaleISOData.java`                          | `java.base/java/util/LocaleISOData.java`                          |
| `LongSummaryStatistics.java`                  | `java.base/java/util/LongSummaryStatistics.java`                  |
| `Map.java`                                    | `java.base/java/util/Map.java`                                    |
| `MissingFormatArgumentException.java`         | `java.base/java/util/MissingFormatArgumentException.java`         |
| `MissingFormatWidthException.java`            | `java.base/java/util/MissingFormatWidthException.java`            |
| `MissingResourceException.java`               | `java.base/java/util/MissingResourceException.java`               |
| `NavigableMap.java`                           | `java.base/java/util/NavigableMap.java`                           |
| `NavigableSet.java`                           | `java.base/java/util/NavigableSet.java`                           |
| `NoSuchElementException.java`                 | `java.base/java/util/NoSuchElementException.java`                 |
| `Objects.java`                                | `java.base/java/util/Objects.java`                                |
| `Observable.java`                             | `java.base/java/util/Observable.java`                             |
| `Observer.java`                               | `java.base/java/util/Observer.java`                               |
| `Optional.java`                               | `java.base/java/util/Optional.java`                               |
| `OptionalDouble.java`                         | `java.base/java/util/OptionalDouble.java`                         |
| `OptionalInt.java`                            | `java.base/java/util/OptionalInt.java`                            |
| `OptionalLong.java`                           | `java.base/java/util/OptionalLong.java`                           |
| `PrimitiveIterator.java`                      | `java.base/java/util/PrimitiveIterator.java`                      |
| `PriorityQueue.java`                          | `java.base/java/util/PriorityQueue.java`                          |
| `Properties.java`                             | `java.base/java/util/Properties.java`                             |
| `PropertyPermission.java`                     | `java.base/java/util/PropertyPermission.java`                     |
| `PropertyResourceBundle.java`                 | `java.base/java/util/PropertyResourceBundle.java`                 |
| `Queue.java`                                  | `java.base/java/util/Queue.java`                                  |
| `Random.java`                                 | `java.base/java/util/Random.java`                                 |
| `RandomAccess.java`                           | `java.base/java/util/RandomAccess.java`                           |
| `RegularEnumSet.java`                         | `java.base/java/util/RegularEnumSet.java`                         |
| `ResourceBundle.java`                         | `java.base/java/util/ResourceBundle.java`                         |
| `ReverseOrderDequeView.java`                  | `java.base/java/util/ReverseOrderDequeView.java`                  |
| `ReverseOrderListView.java`                   | `java.base/java/util/ReverseOrderListView.java`                   |
| `ReverseOrderSortedMapView.java`              | `java.base/java/util/ReverseOrderSortedMapView.java`              |
| `ReverseOrderSortedSetView.java`              | `java.base/java/util/ReverseOrderSortedSetView.java`              |
| `Scanner.java`                                | `java.base/java/util/Scanner.java`                                |
| `SequencedCollection.java`                    | `java.base/java/util/SequencedCollection.java`                    |
| `SequencedMap.java`                           | `java.base/java/util/SequencedMap.java`                           |
| `SequencedSet.java`                           | `java.base/java/util/SequencedSet.java`                           |
| `ServiceConfigurationError.java`              | `java.base/java/util/ServiceConfigurationError.java`              |
| `ServiceLoader.java`                          | `java.base/java/util/ServiceLoader.java`                          |
| `Set.java`                                    | `java.base/java/util/Set.java`                                    |
| `SimpleTimeZone.java`                         | `java.base/java/util/SimpleTimeZone.java`                         |
| `SortedMap.java`                              | `java.base/java/util/SortedMap.java`                              |
| `SortedSet.java`                              | `java.base/java/util/SortedSet.java`                              |
| `Spliterator.java`                            | `java.base/java/util/Spliterator.java`                            |
| `Spliterators.java`                           | `java.base/java/util/Spliterators.java`                           |
| `SplittableRandom.java`                       | `java.base/java/util/SplittableRandom.java`                       |
| `Stack.java`                                  | `java.base/java/util/Stack.java`                                  |
| `StringJoiner.java`                           | `java.base/java/util/StringJoiner.java`                           |
| `StringTokenizer.java`                        | `java.base/java/util/StringTokenizer.java`                        |
| `TimSort.java`                                | `java.base/java/util/TimSort.java`                                |
| `TimeZone.java`                               | `java.base/java/util/TimeZone.java`                               |
| `Timer.java`                                  | `java.base/java/util/Timer.java`                                  |
| `TimerTask.java`                              | `java.base/java/util/TimerTask.java`                              |
| `TooManyListenersException.java`              | `java.base/java/util/TooManyListenersException.java`              |
| `TreeMap.java`                                | `java.base/java/util/TreeMap.java`                                |
| `TreeSet.java`                                | `java.base/java/util/TreeSet.java`                                |
| `Tripwire.java`                               | `java.base/java/util/Tripwire.java`                               |
| `UUID.java`                                   | `java.base/java/util/UUID.java`                                   |
| `UnknownFormatConversionException.java`       | `java.base/java/util/UnknownFormatConversionException.java`       |
| `UnknownFormatFlagsException.java`            | `java.base/java/util/UnknownFormatFlagsException.java`            |
| `Vector.java`                                 | `java.base/java/util/Vector.java`                                 |
| `WeakHashMap.java`                            | `java.base/java/util/WeakHashMap.java`                            |
| `package-info.java`                           | `java.base/java/util/package-info.java`                           |

### JDK 27: `java.util.concurrent` (68 top-level `.java` files)

| Source file                        | Exact path                                                        |
|------------------------------------|-------------------------------------------------------------------|
| `AbstractExecutorService.java`     | `java.base/java/util/concurrent/AbstractExecutorService.java`     |
| `ArrayBlockingQueue.java`          | `java.base/java/util/concurrent/ArrayBlockingQueue.java`          |
| `BlockingDeque.java`               | `java.base/java/util/concurrent/BlockingDeque.java`               |
| `BlockingQueue.java`               | `java.base/java/util/concurrent/BlockingQueue.java`               |
| `BrokenBarrierException.java`      | `java.base/java/util/concurrent/BrokenBarrierException.java`      |
| `Callable.java`                    | `java.base/java/util/concurrent/Callable.java`                    |
| `CancellationException.java`       | `java.base/java/util/concurrent/CancellationException.java`       |
| `CompletableFuture.java`           | `java.base/java/util/concurrent/CompletableFuture.java`           |
| `CompletionException.java`         | `java.base/java/util/concurrent/CompletionException.java`         |
| `CompletionService.java`           | `java.base/java/util/concurrent/CompletionService.java`           |
| `CompletionStage.java`             | `java.base/java/util/concurrent/CompletionStage.java`             |
| `ConcurrentHashMap.java`           | `java.base/java/util/concurrent/ConcurrentHashMap.java`           |
| `ConcurrentLinkedDeque.java`       | `java.base/java/util/concurrent/ConcurrentLinkedDeque.java`       |
| `ConcurrentLinkedQueue.java`       | `java.base/java/util/concurrent/ConcurrentLinkedQueue.java`       |
| `ConcurrentMap.java`               | `java.base/java/util/concurrent/ConcurrentMap.java`               |
| `ConcurrentNavigableMap.java`      | `java.base/java/util/concurrent/ConcurrentNavigableMap.java`      |
| `ConcurrentSkipListMap.java`       | `java.base/java/util/concurrent/ConcurrentSkipListMap.java`       |
| `ConcurrentSkipListSet.java`       | `java.base/java/util/concurrent/ConcurrentSkipListSet.java`       |
| `CopyOnWriteArrayList.java`        | `java.base/java/util/concurrent/CopyOnWriteArrayList.java`        |
| `CopyOnWriteArraySet.java`         | `java.base/java/util/concurrent/CopyOnWriteArraySet.java`         |
| `CountDownLatch.java`              | `java.base/java/util/concurrent/CountDownLatch.java`              |
| `CountedCompleter.java`            | `java.base/java/util/concurrent/CountedCompleter.java`            |
| `CyclicBarrier.java`               | `java.base/java/util/concurrent/CyclicBarrier.java`               |
| `DelayQueue.java`                  | `java.base/java/util/concurrent/DelayQueue.java`                  |
| `DelayScheduler.java`              | `java.base/java/util/concurrent/DelayScheduler.java`              |
| `Delayed.java`                     | `java.base/java/util/concurrent/Delayed.java`                     |
| `Exchanger.java`                   | `java.base/java/util/concurrent/Exchanger.java`                   |
| `ExecutionException.java`          | `java.base/java/util/concurrent/ExecutionException.java`          |
| `Executor.java`                    | `java.base/java/util/concurrent/Executor.java`                    |
| `ExecutorCompletionService.java`   | `java.base/java/util/concurrent/ExecutorCompletionService.java`   |
| `ExecutorService.java`             | `java.base/java/util/concurrent/ExecutorService.java`             |
| `Executors.java`                   | `java.base/java/util/concurrent/Executors.java`                   |
| `Flow.java`                        | `java.base/java/util/concurrent/Flow.java`                        |
| `ForkJoinPool.java`                | `java.base/java/util/concurrent/ForkJoinPool.java`                |
| `ForkJoinTask.java`                | `java.base/java/util/concurrent/ForkJoinTask.java`                |
| `ForkJoinWorkerThread.java`        | `java.base/java/util/concurrent/ForkJoinWorkerThread.java`        |
| `Future.java`                      | `java.base/java/util/concurrent/Future.java`                      |
| `FutureTask.java`                  | `java.base/java/util/concurrent/FutureTask.java`                  |
| `Helpers.java`                     | `java.base/java/util/concurrent/Helpers.java`                     |
| `Joiners.java`                     | `java.base/java/util/concurrent/Joiners.java`                     |
| `LinkedBlockingDeque.java`         | `java.base/java/util/concurrent/LinkedBlockingDeque.java`         |
| `LinkedBlockingQueue.java`         | `java.base/java/util/concurrent/LinkedBlockingQueue.java`         |
| `LinkedTransferQueue.java`         | `java.base/java/util/concurrent/LinkedTransferQueue.java`         |
| `Phaser.java`                      | `java.base/java/util/concurrent/Phaser.java`                      |
| `PriorityBlockingQueue.java`       | `java.base/java/util/concurrent/PriorityBlockingQueue.java`       |
| `RecursiveAction.java`             | `java.base/java/util/concurrent/RecursiveAction.java`             |
| `RecursiveTask.java`               | `java.base/java/util/concurrent/RecursiveTask.java`               |
| `RejectedExecutionException.java`  | `java.base/java/util/concurrent/RejectedExecutionException.java`  |
| `RejectedExecutionHandler.java`    | `java.base/java/util/concurrent/RejectedExecutionHandler.java`    |
| `RunnableFuture.java`              | `java.base/java/util/concurrent/RunnableFuture.java`              |
| `RunnableScheduledFuture.java`     | `java.base/java/util/concurrent/RunnableScheduledFuture.java`     |
| `ScheduledExecutorService.java`    | `java.base/java/util/concurrent/ScheduledExecutorService.java`    |
| `ScheduledFuture.java`             | `java.base/java/util/concurrent/ScheduledFuture.java`             |
| `ScheduledThreadPoolExecutor.java` | `java.base/java/util/concurrent/ScheduledThreadPoolExecutor.java` |
| `Semaphore.java`                   | `java.base/java/util/concurrent/Semaphore.java`                   |
| `StructureViolationException.java` | `java.base/java/util/concurrent/StructureViolationException.java` |
| `StructuredTaskScope.java`         | `java.base/java/util/concurrent/StructuredTaskScope.java`         |
| `StructuredTaskScopeImpl.java`     | `java.base/java/util/concurrent/StructuredTaskScopeImpl.java`     |
| `SubmissionPublisher.java`         | `java.base/java/util/concurrent/SubmissionPublisher.java`         |
| `SynchronousQueue.java`            | `java.base/java/util/concurrent/SynchronousQueue.java`            |
| `ThreadFactory.java`               | `java.base/java/util/concurrent/ThreadFactory.java`               |
| `ThreadLocalRandom.java`           | `java.base/java/util/concurrent/ThreadLocalRandom.java`           |
| `ThreadPerTaskExecutor.java`       | `java.base/java/util/concurrent/ThreadPerTaskExecutor.java`       |
| `ThreadPoolExecutor.java`          | `java.base/java/util/concurrent/ThreadPoolExecutor.java`          |
| `TimeUnit.java`                    | `java.base/java/util/concurrent/TimeUnit.java`                    |
| `TimeoutException.java`            | `java.base/java/util/concurrent/TimeoutException.java`            |
| `TransferQueue.java`               | `java.base/java/util/concurrent/TransferQueue.java`               |
| `package-info.java`                | `java.base/java/util/concurrent/package-info.java`                |

### JDK 27: `java.util.stream` (40 top-level `.java` files)

| Source file                     | Exact path                                                 |
|---------------------------------|------------------------------------------------------------|
| `AbstractPipeline.java`         | `java.base/java/util/stream/AbstractPipeline.java`         |
| `AbstractShortCircuitTask.java` | `java.base/java/util/stream/AbstractShortCircuitTask.java` |
| `AbstractSpinedBuffer.java`     | `java.base/java/util/stream/AbstractSpinedBuffer.java`     |
| `AbstractTask.java`             | `java.base/java/util/stream/AbstractTask.java`             |
| `BaseStream.java`               | `java.base/java/util/stream/BaseStream.java`               |
| `Collector.java`                | `java.base/java/util/stream/Collector.java`                |
| `Collectors.java`               | `java.base/java/util/stream/Collectors.java`               |
| `DistinctOps.java`              | `java.base/java/util/stream/DistinctOps.java`              |
| `DoublePipeline.java`           | `java.base/java/util/stream/DoublePipeline.java`           |
| `DoubleStream.java`             | `java.base/java/util/stream/DoubleStream.java`             |
| `FindOps.java`                  | `java.base/java/util/stream/FindOps.java`                  |
| `ForEachOps.java`               | `java.base/java/util/stream/ForEachOps.java`               |
| `Gatherer.java`                 | `java.base/java/util/stream/Gatherer.java`                 |
| `GathererOp.java`               | `java.base/java/util/stream/GathererOp.java`               |
| `Gatherers.java`                | `java.base/java/util/stream/Gatherers.java`                |
| `IntPipeline.java`              | `java.base/java/util/stream/IntPipeline.java`              |
| `IntStream.java`                | `java.base/java/util/stream/IntStream.java`                |
| `LongPipeline.java`             | `java.base/java/util/stream/LongPipeline.java`             |
| `LongStream.java`               | `java.base/java/util/stream/LongStream.java`               |
| `MatchOps.java`                 | `java.base/java/util/stream/MatchOps.java`                 |
| `Node.java`                     | `java.base/java/util/stream/Node.java`                     |
| `Nodes.java`                    | `java.base/java/util/stream/Nodes.java`                    |
| `PipelineHelper.java`           | `java.base/java/util/stream/PipelineHelper.java`           |
| `ReduceOps.java`                | `java.base/java/util/stream/ReduceOps.java`                |
| `ReferencePipeline.java`        | `java.base/java/util/stream/ReferencePipeline.java`        |
| `Sink.java`                     | `java.base/java/util/stream/Sink.java`                     |
| `SliceOps.java`                 | `java.base/java/util/stream/SliceOps.java`                 |
| `SortedOps.java`                | `java.base/java/util/stream/SortedOps.java`                |
| `SpinedBuffer.java`             | `java.base/java/util/stream/SpinedBuffer.java`             |
| `Stream.java`                   | `java.base/java/util/stream/Stream.java`                   |
| `StreamOpFlag.java`             | `java.base/java/util/stream/StreamOpFlag.java`             |
| `StreamShape.java`              | `java.base/java/util/stream/StreamShape.java`              |
| `StreamSpliterators.java`       | `java.base/java/util/stream/StreamSpliterators.java`       |
| `StreamSupport.java`            | `java.base/java/util/stream/StreamSupport.java`            |
| `Streams.java`                  | `java.base/java/util/stream/Streams.java`                  |
| `TerminalOp.java`               | `java.base/java/util/stream/TerminalOp.java`               |
| `TerminalSink.java`             | `java.base/java/util/stream/TerminalSink.java`             |
| `Tripwire.java`                 | `java.base/java/util/stream/Tripwire.java`                 |
| `WhileOps.java`                 | `java.base/java/util/stream/WhileOps.java`                 |
| `package-info.java`             | `java.base/java/util/stream/package-info.java`             |

### JDK 27: `java.nio` (85 top-level `.java` files)

| Source file                       | Exact path                                           |
|-----------------------------------|------------------------------------------------------|
| `Bits.java`                       | `java.base/java/nio/Bits.java`                       |
| `Buffer.java`                     | `java.base/java/nio/Buffer.java`                     |
| `BufferCleaner.java`              | `java.base/java/nio/BufferCleaner.java`              |
| `BufferMismatch.java`             | `java.base/java/nio/BufferMismatch.java`             |
| `BufferOverflowException.java`    | `java.base/java/nio/BufferOverflowException.java`    |
| `BufferUnderflowException.java`   | `java.base/java/nio/BufferUnderflowException.java`   |
| `ByteBuffer.java`                 | `java.base/java/nio/ByteBuffer.java`                 |
| `ByteBufferAsCharBufferB.java`    | `java.base/java/nio/ByteBufferAsCharBufferB.java`    |
| `ByteBufferAsCharBufferL.java`    | `java.base/java/nio/ByteBufferAsCharBufferL.java`    |
| `ByteBufferAsCharBufferRB.java`   | `java.base/java/nio/ByteBufferAsCharBufferRB.java`   |
| `ByteBufferAsCharBufferRL.java`   | `java.base/java/nio/ByteBufferAsCharBufferRL.java`   |
| `ByteBufferAsDoubleBufferB.java`  | `java.base/java/nio/ByteBufferAsDoubleBufferB.java`  |
| `ByteBufferAsDoubleBufferL.java`  | `java.base/java/nio/ByteBufferAsDoubleBufferL.java`  |
| `ByteBufferAsDoubleBufferRB.java` | `java.base/java/nio/ByteBufferAsDoubleBufferRB.java` |
| `ByteBufferAsDoubleBufferRL.java` | `java.base/java/nio/ByteBufferAsDoubleBufferRL.java` |
| `ByteBufferAsFloatBufferB.java`   | `java.base/java/nio/ByteBufferAsFloatBufferB.java`   |
| `ByteBufferAsFloatBufferL.java`   | `java.base/java/nio/ByteBufferAsFloatBufferL.java`   |
| `ByteBufferAsFloatBufferRB.java`  | `java.base/java/nio/ByteBufferAsFloatBufferRB.java`  |
| `ByteBufferAsFloatBufferRL.java`  | `java.base/java/nio/ByteBufferAsFloatBufferRL.java`  |
| `ByteBufferAsIntBufferB.java`     | `java.base/java/nio/ByteBufferAsIntBufferB.java`     |
| `ByteBufferAsIntBufferL.java`     | `java.base/java/nio/ByteBufferAsIntBufferL.java`     |
| `ByteBufferAsIntBufferRB.java`    | `java.base/java/nio/ByteBufferAsIntBufferRB.java`    |
| `ByteBufferAsIntBufferRL.java`    | `java.base/java/nio/ByteBufferAsIntBufferRL.java`    |
| `ByteBufferAsLongBufferB.java`    | `java.base/java/nio/ByteBufferAsLongBufferB.java`    |
| `ByteBufferAsLongBufferL.java`    | `java.base/java/nio/ByteBufferAsLongBufferL.java`    |
| `ByteBufferAsLongBufferRB.java`   | `java.base/java/nio/ByteBufferAsLongBufferRB.java`   |
| `ByteBufferAsLongBufferRL.java`   | `java.base/java/nio/ByteBufferAsLongBufferRL.java`   |
| `ByteBufferAsShortBufferB.java`   | `java.base/java/nio/ByteBufferAsShortBufferB.java`   |
| `ByteBufferAsShortBufferL.java`   | `java.base/java/nio/ByteBufferAsShortBufferL.java`   |
| `ByteBufferAsShortBufferRB.java`  | `java.base/java/nio/ByteBufferAsShortBufferRB.java`  |
| `ByteBufferAsShortBufferRL.java`  | `java.base/java/nio/ByteBufferAsShortBufferRL.java`  |
| `ByteOrder.java`                  | `java.base/java/nio/ByteOrder.java`                  |
| `CharBuffer.java`                 | `java.base/java/nio/CharBuffer.java`                 |
| `CharBufferSpliterator.java`      | `java.base/java/nio/CharBufferSpliterator.java`      |
| `DirectByteBuffer.java`           | `java.base/java/nio/DirectByteBuffer.java`           |
| `DirectByteBufferR.java`          | `java.base/java/nio/DirectByteBufferR.java`          |
| `DirectCharBufferRS.java`         | `java.base/java/nio/DirectCharBufferRS.java`         |
| `DirectCharBufferRU.java`         | `java.base/java/nio/DirectCharBufferRU.java`         |
| `DirectCharBufferS.java`          | `java.base/java/nio/DirectCharBufferS.java`          |
| `DirectCharBufferU.java`          | `java.base/java/nio/DirectCharBufferU.java`          |
| `DirectDoubleBufferRS.java`       | `java.base/java/nio/DirectDoubleBufferRS.java`       |
| `DirectDoubleBufferRU.java`       | `java.base/java/nio/DirectDoubleBufferRU.java`       |
| `DirectDoubleBufferS.java`        | `java.base/java/nio/DirectDoubleBufferS.java`        |
| `DirectDoubleBufferU.java`        | `java.base/java/nio/DirectDoubleBufferU.java`        |
| `DirectFloatBufferRS.java`        | `java.base/java/nio/DirectFloatBufferRS.java`        |
| `DirectFloatBufferRU.java`        | `java.base/java/nio/DirectFloatBufferRU.java`        |
| `DirectFloatBufferS.java`         | `java.base/java/nio/DirectFloatBufferS.java`         |
| `DirectFloatBufferU.java`         | `java.base/java/nio/DirectFloatBufferU.java`         |
| `DirectIntBufferRS.java`          | `java.base/java/nio/DirectIntBufferRS.java`          |
| `DirectIntBufferRU.java`          | `java.base/java/nio/DirectIntBufferRU.java`          |
| `DirectIntBufferS.java`           | `java.base/java/nio/DirectIntBufferS.java`           |
| `DirectIntBufferU.java`           | `java.base/java/nio/DirectIntBufferU.java`           |
| `DirectLongBufferRS.java`         | `java.base/java/nio/DirectLongBufferRS.java`         |
| `DirectLongBufferRU.java`         | `java.base/java/nio/DirectLongBufferRU.java`         |
| `DirectLongBufferS.java`          | `java.base/java/nio/DirectLongBufferS.java`          |
| `DirectLongBufferU.java`          | `java.base/java/nio/DirectLongBufferU.java`          |
| `DirectShortBufferRS.java`        | `java.base/java/nio/DirectShortBufferRS.java`        |
| `DirectShortBufferRU.java`        | `java.base/java/nio/DirectShortBufferRU.java`        |
| `DirectShortBufferS.java`         | `java.base/java/nio/DirectShortBufferS.java`         |
| `DirectShortBufferU.java`         | `java.base/java/nio/DirectShortBufferU.java`         |
| `DoubleBuffer.java`               | `java.base/java/nio/DoubleBuffer.java`               |
| `FloatBuffer.java`                | `java.base/java/nio/FloatBuffer.java`                |
| `HeapByteBuffer.java`             | `java.base/java/nio/HeapByteBuffer.java`             |
| `HeapByteBufferR.java`            | `java.base/java/nio/HeapByteBufferR.java`            |
| `HeapCharBuffer.java`             | `java.base/java/nio/HeapCharBuffer.java`             |
| `HeapCharBufferR.java`            | `java.base/java/nio/HeapCharBufferR.java`            |
| `HeapDoubleBuffer.java`           | `java.base/java/nio/HeapDoubleBuffer.java`           |
| `HeapDoubleBufferR.java`          | `java.base/java/nio/HeapDoubleBufferR.java`          |
| `HeapFloatBuffer.java`            | `java.base/java/nio/HeapFloatBuffer.java`            |
| `HeapFloatBufferR.java`           | `java.base/java/nio/HeapFloatBufferR.java`           |
| `HeapIntBuffer.java`              | `java.base/java/nio/HeapIntBuffer.java`              |
| `HeapIntBufferR.java`             | `java.base/java/nio/HeapIntBufferR.java`             |
| `HeapLongBuffer.java`             | `java.base/java/nio/HeapLongBuffer.java`             |
| `HeapLongBufferR.java`            | `java.base/java/nio/HeapLongBufferR.java`            |
| `HeapShortBuffer.java`            | `java.base/java/nio/HeapShortBuffer.java`            |
| `HeapShortBufferR.java`           | `java.base/java/nio/HeapShortBufferR.java`           |
| `IntBuffer.java`                  | `java.base/java/nio/IntBuffer.java`                  |
| `InvalidMarkException.java`       | `java.base/java/nio/InvalidMarkException.java`       |
| `LongBuffer.java`                 | `java.base/java/nio/LongBuffer.java`                 |
| `MappedByteBuffer.java`           | `java.base/java/nio/MappedByteBuffer.java`           |
| `MappedMemoryUtils.java`          | `java.base/java/nio/MappedMemoryUtils.java`          |
| `ReadOnlyBufferException.java`    | `java.base/java/nio/ReadOnlyBufferException.java`    |
| `ShortBuffer.java`                | `java.base/java/nio/ShortBuffer.java`                |
| `StringCharBuffer.java`           | `java.base/java/nio/StringCharBuffer.java`           |
| `package-info.java`               | `java.base/java/nio/package-info.java`               |

### JDK 27: `java.util.function` (44 top-level `.java` files)

| Source file                 | Exact path                                               |
|-----------------------------|----------------------------------------------------------|
| `BiConsumer.java`           | `java.base/java/util/function/BiConsumer.java`           |
| `BiFunction.java`           | `java.base/java/util/function/BiFunction.java`           |
| `BiPredicate.java`          | `java.base/java/util/function/BiPredicate.java`          |
| `BinaryOperator.java`       | `java.base/java/util/function/BinaryOperator.java`       |
| `BooleanSupplier.java`      | `java.base/java/util/function/BooleanSupplier.java`      |
| `Consumer.java`             | `java.base/java/util/function/Consumer.java`             |
| `DoubleBinaryOperator.java` | `java.base/java/util/function/DoubleBinaryOperator.java` |
| `DoubleConsumer.java`       | `java.base/java/util/function/DoubleConsumer.java`       |
| `DoubleFunction.java`       | `java.base/java/util/function/DoubleFunction.java`       |
| `DoublePredicate.java`      | `java.base/java/util/function/DoublePredicate.java`      |
| `DoubleSupplier.java`       | `java.base/java/util/function/DoubleSupplier.java`       |
| `DoubleToIntFunction.java`  | `java.base/java/util/function/DoubleToIntFunction.java`  |
| `DoubleToLongFunction.java` | `java.base/java/util/function/DoubleToLongFunction.java` |
| `DoubleUnaryOperator.java`  | `java.base/java/util/function/DoubleUnaryOperator.java`  |
| `Function.java`             | `java.base/java/util/function/Function.java`             |
| `IntBinaryOperator.java`    | `java.base/java/util/function/IntBinaryOperator.java`    |
| `IntConsumer.java`          | `java.base/java/util/function/IntConsumer.java`          |
| `IntFunction.java`          | `java.base/java/util/function/IntFunction.java`          |
| `IntPredicate.java`         | `java.base/java/util/function/IntPredicate.java`         |
| `IntSupplier.java`          | `java.base/java/util/function/IntSupplier.java`          |
| `IntToDoubleFunction.java`  | `java.base/java/util/function/IntToDoubleFunction.java`  |
| `IntToLongFunction.java`    | `java.base/java/util/function/IntToLongFunction.java`    |
| `IntUnaryOperator.java`     | `java.base/java/util/function/IntUnaryOperator.java`     |
| `LongBinaryOperator.java`   | `java.base/java/util/function/LongBinaryOperator.java`   |
| `LongConsumer.java`         | `java.base/java/util/function/LongConsumer.java`         |
| `LongFunction.java`         | `java.base/java/util/function/LongFunction.java`         |
| `LongPredicate.java`        | `java.base/java/util/function/LongPredicate.java`        |
| `LongSupplier.java`         | `java.base/java/util/function/LongSupplier.java`         |
| `LongToDoubleFunction.java` | `java.base/java/util/function/LongToDoubleFunction.java` |
| `LongToIntFunction.java`    | `java.base/java/util/function/LongToIntFunction.java`    |
| `LongUnaryOperator.java`    | `java.base/java/util/function/LongUnaryOperator.java`    |
| `ObjDoubleConsumer.java`    | `java.base/java/util/function/ObjDoubleConsumer.java`    |
| `ObjIntConsumer.java`       | `java.base/java/util/function/ObjIntConsumer.java`       |
| `ObjLongConsumer.java`      | `java.base/java/util/function/ObjLongConsumer.java`      |
| `Predicate.java`            | `java.base/java/util/function/Predicate.java`            |
| `Supplier.java`             | `java.base/java/util/function/Supplier.java`             |
| `ToDoubleBiFunction.java`   | `java.base/java/util/function/ToDoubleBiFunction.java`   |
| `ToDoubleFunction.java`     | `java.base/java/util/function/ToDoubleFunction.java`     |
| `ToIntBiFunction.java`      | `java.base/java/util/function/ToIntBiFunction.java`      |
| `ToIntFunction.java`        | `java.base/java/util/function/ToIntFunction.java`        |
| `ToLongBiFunction.java`     | `java.base/java/util/function/ToLongBiFunction.java`     |
| `ToLongFunction.java`       | `java.base/java/util/function/ToLongFunction.java`       |
| `UnaryOperator.java`        | `java.base/java/util/function/UnaryOperator.java`        |
| `package-info.java`         | `java.base/java/util/function/package-info.java`         |

### JDK 27: `java.util.regex` (9 top-level `.java` files)

| Source file                   | Exact path                                              |
|-------------------------------|---------------------------------------------------------|
| `ASCII.java`                  | `java.base/java/util/regex/ASCII.java`                  |
| `CharPredicates.java`         | `java.base/java/util/regex/CharPredicates.java`         |
| `IntHashSet.java`             | `java.base/java/util/regex/IntHashSet.java`             |
| `MatchResult.java`            | `java.base/java/util/regex/MatchResult.java`            |
| `Matcher.java`                | `java.base/java/util/regex/Matcher.java`                |
| `Pattern.java`                | `java.base/java/util/regex/Pattern.java`                |
| `PatternSyntaxException.java` | `java.base/java/util/regex/PatternSyntaxException.java` |
| `PrintPattern.java`           | `java.base/java/util/regex/PrintPattern.java`           |
| `package-info.java`           | `java.base/java/util/regex/package-info.java`           |

### JDK 27 adjacent APIs requiring classification

- `java.lang.Iterable`, arrays and array reflection (`java.lang.reflect.Array`); API surface may be split across
  packages.
- `java.nio.Buffer` and typed buffers: indexed mutable view/cursor semantics; not directly a `Collection` interface.
- `java.util.Optional`, primitive optionals, `Map.Entry`, `EnumSet`, `BitSet`, `Properties`, `ServiceLoader`, `Scanner`,
  `StringTokenizer`: each needs semantic scope classification.
- `java.util.concurrent.Flow` and JDK stream producer APIs: asynchronous / deferred execution, not automatically
  complete Contract values.
- The entire 65-module JDK source archive contains further collection-like APIs; the core inventory below does not
  assert their completeness.

### Kotlin 2.4.21 source candidates

| Source file                                                  | Approximate `fun` candidates |
|--------------------------------------------------------------|-----------------------------:|
| `commonMain/generated/_Arrays.kt`                            |                         1411 |
| `commonMain/generated/_Collections.kt`                       |                          214 |
| `commonMain/generated/_Ranges.kt`                            |                          150 |
| `commonMain/generated/_Sequences.kt`                         |                          159 |
| `commonMain/generated/_UArrays.kt`                           |                          624 |
| `commonMain/generated/_UCollections.kt`                      |                            8 |
| `commonMain/generated/_URanges.kt`                           |                           50 |
| `commonMain/generated/_USequences.kt`                        |                            4 |
| `commonMain/kotlin/Array.kt`                                 |                            3 |
| `commonMain/kotlin/Arrays.kt`                                |                           24 |
| `commonMain/kotlin/Collections.kt`                           |                           56 |
| `commonMain/kotlin/SequencesH.kt`                            |                            1 |
| `commonMain/kotlin/UByteArray.kt`                            |                           10 |
| `commonMain/kotlin/UIntArray.kt`                             |                           10 |
| `commonMain/kotlin/ULongArray.kt`                            |                           10 |
| `commonMain/kotlin/UShortArray.kt`                           |                           10 |
| `commonMain/kotlin/collections/AbstractCollection.kt`        |                            7 |
| `commonMain/kotlin/collections/AbstractIterator.kt`          |                            6 |
| `commonMain/kotlin/collections/AbstractList.kt`              |                           24 |
| `commonMain/kotlin/collections/AbstractMap.kt`               |                           22 |
| `commonMain/kotlin/collections/AbstractMutableCollection.kt` |                           10 |
| `commonMain/kotlin/collections/AbstractMutableList.kt`       |                           17 |
| `commonMain/kotlin/collections/AbstractMutableMap.kt`        |                            8 |
| `commonMain/kotlin/collections/AbstractMutableSet.kt`        |                           10 |
| `commonMain/kotlin/collections/AbstractSet.kt`               |                            4 |
| `commonMain/kotlin/collections/ArrayDeque.kt`                |                           46 |
| `commonMain/kotlin/collections/ArrayList.kt`                 |                           22 |
| `commonMain/kotlin/collections/Arrays.kt`                    |                            1 |
| `commonMain/kotlin/collections/Collections.kt`               |                           60 |
| `commonMain/kotlin/collections/CollectionsH.kt`              |                            8 |
| `commonMain/kotlin/collections/Grouping.kt`                  |                            2 |
| `commonMain/kotlin/collections/HashMap.kt`                   |                            8 |
| `commonMain/kotlin/collections/HashSet.kt`                   |                           10 |
| `commonMain/kotlin/collections/IndexedValue.kt`              |                            0 |
| `commonMain/kotlin/collections/Iterables.kt`                 |                            6 |
| `commonMain/kotlin/collections/Iterators.kt`                 |                            5 |
| `commonMain/kotlin/collections/LinkedHashMap.kt`             |                            8 |
| `commonMain/kotlin/collections/LinkedHashSet.kt`             |                           10 |
| `commonMain/kotlin/collections/MapAccessors.kt`              |                            0 |
| `commonMain/kotlin/collections/MapWithDefault.kt`            |                           21 |
| `commonMain/kotlin/collections/Maps.kt`                      |                           28 |
| `commonMain/kotlin/collections/MutableCollections.kt`        |                           12 |
| `commonMain/kotlin/collections/PrimitiveIterators.kt`        |                           40 |
| `commonMain/kotlin/collections/ReversedViews.kt`             |                           29 |
| `commonMain/kotlin/collections/Sequence.kt`                  |                            1 |
| `commonMain/kotlin/collections/SequenceBuilder.kt`           |                           13 |
| `commonMain/kotlin/collections/Sequences.kt`                 |                           78 |
| `commonMain/kotlin/collections/Sets.kt`                      |                           26 |
| `commonMain/kotlin/collections/SlidingWindow.kt`             |                           15 |
| `commonMain/kotlin/collections/UArraySorting.kt`             |                           12 |
| `commonMain/kotlin/collections/UByteValueSet.kt`             |                            1 |
| `commonMain/kotlin/ranges/PrimitiveRanges.kt`                |                           15 |
| `commonMain/kotlin/ranges/ProgressionIterators.kt`           |                            6 |
| `commonMain/kotlin/ranges/Progressions.kt`                   |                           18 |
| `commonMain/kotlin/ranges/Range.kt`                          |                            4 |
| `commonMain/kotlin/ranges/Ranges.kt`                         |                           42 |
| `jvmMain/generated/_ArraysJvm.kt`                            |                          244 |
| `jvmMain/generated/_CollectionsJvm.kt`                       |                           10 |
| `jvmMain/generated/_SequencesJvm.kt`                         |                            9 |
| `jvmMain/generated/_UArraysJvm.kt`                           |                           56 |
| `jvmMain/jdk8/kotlin/collections/Collections.kt`             |                            0 |
| `jvmMain/kotlin/Array.kt`                                    |                            3 |
| `jvmMain/kotlin/Arrays.kt`                                   |                           24 |
| `jvmMain/kotlin/Collections.kt`                              |                           58 |
| `jvmMain/kotlin/collections/AbstractMutableCollection.kt`    |                            1 |
| `jvmMain/kotlin/collections/AbstractMutableList.kt`          |                            3 |
| `jvmMain/kotlin/collections/AbstractMutableMap.kt`           |                            1 |
| `jvmMain/kotlin/collections/AbstractMutableSet.kt`           |                            1 |
| `jvmMain/kotlin/collections/ArraysJVM.kt`                    |                            4 |
| `jvmMain/kotlin/collections/CollectionsJVM.kt`               |                           14 |
| `jvmMain/kotlin/collections/GroupingJVM.kt`                  |                            0 |
| `jvmMain/kotlin/collections/IteratorsJVM.kt`                 |                            3 |
| `jvmMain/kotlin/collections/MapsJVM.kt`                      |                            8 |
| `jvmMain/kotlin/collections/MutableCollectionsJVM.kt`        |                            6 |
| `jvmMain/kotlin/collections/SequencesJVM.kt`                 |                            2 |
| `jvmMain/kotlin/collections/SetsJVM.kt`                      |                            8 |
| `jvmMain/kotlin/collections/TypeAliases.kt`                  |                            0 |
| `jvmMain/kotlin/collections/builders/ListBuilder.kt`         |                           99 |
| `jvmMain/kotlin/collections/builders/MapBuilder.kt`          |                           89 |
| `jvmMain/kotlin/collections/builders/SetBuilder.kt`          |                           12 |

**Counted source files: 80. Approximate function declaration matches: 4,054.** These values were recovered by
counting the 80 rows and summing their recorded candidate counts in this preserved index; the earlier index text
contained unexpanded count placeholders. Do not treat these as semantically qualified overloads.

### Family and operator coverage checkpoints

#### Value-shape candidates

- Ordered occurrence sequence (encounter and indexed distinctions); unordered bag/multiset; membership; association;
  indexed-domain membership; range and progression.
- Named and positional products; tagged choice; nested and grouped/partitioned composition; prefix/window shape and
  parent correspondence.
- Host Equality, comparator equivalence, reference identity, representative, key/value and group distinctions belong to
  source meaning or receiving owner; not universal Contract sameness.

#### State/operation protocols

- FIFO/LIFO/deque, priority head, bounded/blocking queue, rendezvous/transfer, delay/time-gating.
- Per-element and bulk mutation; read-modifies-state, callback effects, rejection and partial failure.
- Iterator/list iterator/enumeration/spliterator; Stream/Gatherer/Collector; Kotlin Sequence and Grouping.
- Lazy-element evaluation and error state; backed, reversed, checked, synchronized, copy-on-write and snapshot views.
- Concurrent/weakly consistent, GC-sensitive, identity-sensitive, default-computed lookup.
- `java.nio` indexed buffer/cursor and `Flow` asynchronous publisher are adjacent surfaces, not automatically values.

### Required evidence before asserting exhaustive semantic qualification

1. Obtain public/API visibility from actual JDK signatures and Kotlin compiler metadata (not source regex alone),
   including inherited methods and version/preview status.
2. Assign **every exact overload** a source profile with input domain, output shape, comparison/ordering, evaluation,
   alias, partial failure and environment constraints.
3. Assign a complete Value/Operation/Observation family or explicitly classify as non-collection adjacent; track
   unclassified symbols to zero.
4. Demonstrate source-spec qualification and cases where platform allows multiple results; apply deterministic
   refinement without substituting implementation accident.
5. Map use to owning 1D laws independently; maintain unsupported and unqualified statuses rather than declaring support.
6. Validate against full recomputation, incremental reuse, failure injection and platform-version changes.

---

# Appendix C. Evidence Preservation and Migration Check

| Origin                                            | New authoritative or evidence location                               | Retention check                                                                    |
|---------------------------------------------------|----------------------------------------------------------------------|------------------------------------------------------------------------------------|
| Earlier ADR-0081 §3.1.1 core F01–F11 catalogue    | ADR-0081 §3.1 constituent laws; Design §3.1 complete mapping         | Every constituent distinction retained                                             |
| Earlier ADR-0081 §3.1.1 F12–F21 composed table    | ADR-0081 §3.1 composition law; Design §3.2 complete mapping          | Group/partition, range, multiplicity, nested and lifetime preserved                |
| Earlier ADR-0081 §3.1.2 P01–P12 table             | Design §4.1                                                          | All 12 State/Operation groups retained                                             |
| Earlier ADR-0081 §3.1.2 E01–E11 table             | Design §4.2                                                          | All 11 Observation groups retained                                                 |
| Earlier ADR-0081 §3.3 operation group table       | Design §5                                                            | All 15 source-work groups retained                                                 |
| Earlier ADR-0081 §2 illustrative operations       | Design §§4–5                                                         | Distinctions retained as examples, not Contract laws                               |
| Earlier ADR-0081 §3.4 result observation matrix   | ADR-0081 §3.4 abstract law; Design §§3–5                             | No legal constituent distinction removed                                           |
| Earlier ADR-0081 §3.9 JDK 27 `ofLazy` example     | ADR-0081 §3.9 abstract result/evaluation law; Design §§4.2, 5.1, 9.2 | Preview/state/object method caveats retained                                       |
| Earlier ADR-0081 §4 implementation/deferred table | ADR-0081 §4 owner boundaries; Design §§6–10                          | SDK/IR/V1/V2/Conformance work retained                                             |
| G15–G16 new Q01–Q08 findings                      | ADR-0081 §§3.3/3.9 abstract law; Design §§4.3, 5.1, 6, 9             | BitSet capacity, randomness, builder lifetime, lazy evaluation and others retained |
| G15–G16 102 curated mapping records               | Design Appendix A                                                    | Source paths, family concerns and unqualified status retained                      |
| G10–G14 539-line source-path index                | Design Appendix B                                                    | Included as audit input; no semantic support claim                                 |
| G15–G16 11,975 declaration-line candidates        | Design §2.1 and §11; existing CSV remains separate evidence          | Candidate counts, non-coverage caveat, source file provenance retained             |
| SADR-0002 security/fault scenarios                | Unchanged SADR; Design §§7, 9                                        | Not recast as additional Collection Contract                                       |

**This migration preserves the *known semantic contents*, not an assertion of exhaustive Java/Kotlin public-callable
qualification.** Unresolved API signatures, unused libraries and SDK evolutions remain explicit evidence gaps.
