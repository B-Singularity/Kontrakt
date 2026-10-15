# Admission Source Refinement and Realization — Working Design

## Status

Working Design

## Date

2026-10-11

## Governing Documents

- ADR-0065: Admission Contract, Explicit Continuation Judgment, and Deterministic Evaluation Boundary.
- ADR-0064: Input Contract.
- ADR-0066: Canonicalization Contract.
- ADR-0048: Inbound 1D composition.
- ADR-0053: Version Contract.
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition.
- ADR-0071: Resolved Contract HIR semantic boundary.
- ADR-0073: JVM platform-native semantic ratification and external Contract infiltration boundary.
- ADR-0075: Compiler result ownership, legal consumption, and deterministic realization.
- `docs/design/overview/kontrakt-compiler-total-architecture-map-design-draft.md`: compiler-wide working architecture.

---

# 1. Purpose and Ownership

This Design records the compiler work required to acquire ordinary Java or Kotlin Admission source, resolve it into
complete Admission meaning, expose the required HIR and Established observations, and realize the resulting judgment
without retaining host execution as authority. It also records source-coverage candidates, implementation alternatives,
evidence requirements, and decisions that must be made before V1 implementation.

ADR-0065 determines which Admission judgments are legal and what they mean. This Design determines how compiler
components may establish and realize those semantics. A source form is not supported merely because it is listed here,
compiles on the JVM, or can be executed as a Boolean callback. Every supported form must satisfy the applicable
ADR-defined meaning and the corresponding compiler qualification.

The user should write ordinary supported Java or Kotlin instead of constructing Kontrakt IR or learning a second
judgment language. Internal semantic references, operation profiles, summaries, compiler handles, and projections remain
compiler concerns. The declaration's host type, method body, and generated interface do not independently establish
Contract authority.

The selected Admission source is a Java or Kotlin class declaration identified exactly through IDL. Its methods
and private constants express one closed Admission judgment; the class is not instantiated as a validator. Section 3.1
shows the agreed authoring form in both languages. The separate Admission Authoring API Design may specify further
language-level qualification and diagnostics, but it must not reopen the class-based selection or give method names,
instances, or host call structure Contract authority.

Admission user authoring is intended to remain Input-shaped whether Canonicalization is omitted, selected on some
coordinates, or selected throughout the applicable Input. Its exact semantic operand and Basis must follow the governing
inbound Contract decisions, not the host carrier object. The existing ADR-0065 text still contains the older
direct-Input path; do not implement that obsolete ordering as if it were a new decision. The correct current order is
Input → optional Canonicalization → Admission. Operand, Observation Requirement, Basis, Occurrence attribution, and
protocol details remain to be reconciled in the owning ADRs.

---

# 2. Source Capability Qualification Record

Each Java/Kotlin source construct or recognized platform callable considered for V1 needs a qualification record. This
record supports implementation and API decisions; it does not create a new Admission semantic catalog or delegate
authority to host libraries.

## 2.1. Exact source subject

Identify the exact class selected by IDL before resolving its member declarations. For each contributing language
construct or callable, record its receiver, arguments, value domain, language/platform version, and implicit behavior.
Overloaded method names within the selected Admission class are an authoring error, not a dispatch choice. For a private
helper or closed constant, account for the complete semantically reachable computation. Record Kotlin and Java
differences when they change resolution or execution meaning.

## 2.2. Input observation and actual operand

Describe which Input-declared coordinates and constituent observations the construct requires. Distinguish Input-facing
authoring syntax from the final legally determining Boundary Presentation supplied to Admission. Check omitted,
partially selected, and fully selected Canonicalization independently. Host field compatibility or JVM assignability
cannot establish semantic compatibility.

## 2.3. Closed operation meaning

Record the exact value relations, arithmetic and comparison behavior, conditional reachability, exceptional domains,
ordering, equality, and any platform semantic profile required to resolve the source. A selected Java/Kotlin library
implementation is evidence for analysis, not an implicit Contract law. ADR-0073 governs ratification of exact platform
meaning.

## 2.4. Effect, alias, and escape closure

Account for property access, overload dispatch, initialization, capture, iteration, user-defined equality or comparison,
nested calls, temporary mutation, and observable effects. Show that no reachable operation imports a hidden external
determinant or exports local mutable state as new authority. `val`, `final`, an annotation, and a nominally pure API do
not by themselves prove closure.

## 2.5. Completion and work

Establish the relevant domain guards, completion conditions, finite iteration or progress, and supported
pattern-matching work law. Distinguish semantic finiteness from acceptable physical CPU or memory cost. Keep compiler
analysis exhaustion and runtime resource enforcement separate from Admission `Admitted` or `Rejected`.

## 2.6. Disposition and falsifiable evidence

Record whether the exact form is implemented, conditionally supportable, unsupported for V1, or deferred pending
evidence. Preserve representative positive and negative cases, adversarial cases where relevant, and the reason for any
rejected qualification. This document currently identifies candidates and qualification conditions, not a completed V1
support matrix.

---

# 3. V1 Source Coverage Candidates

The following inventory provides planning coverage for the frontend and later Admission Authoring API. The target is
ordinary value-oriented judgment code whose complete meaning can be resolved under ADR-0065; none of the entries grants
blanket support to every host expression in its category.

| Candidate area                          | Source forms to qualify                                                                              | Main qualification issue                                                                               |
|-----------------------------------------|------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| Judgment and Boolean control            | One complete class-declared Boolean judgment, conjunction, disjunction, finite choices, early return | Preserve reachability, short-circuit semantics, and exactly one Admission result                       |
| Fixed-width integers                    | Comparisons, arithmetic, bit operations, narrowing, shifts, division                                 | Close signedness, widths, overflow, shift rules, division domains, and guards                          |
| Floating and decimal values             | Arithmetic, comparison, classification, large numbers, decimal values                                | Preserve NaN, infinities, signed zero, raw bits where observable, numeric vs presentation equality     |
| Closed alternatives                     | Enum-like choices, presence, absence, nullability, closed tagged choices                             | Read only Input-exposed discriminants and payload distinctions                                         |
| Text, character, and bytes              | Exact comparison, containment, slicing, codecs, Unicode and binary operations                        | Fix encoding, position, equality, length, and external semantic profile                                |
| Closed domain values                    | Temporal values, identifiers, closed references                                                      | Determine exact versioned operation meaning and any external semantic material                         |
| Finite array and Input collection forms | Size, indexed/keyed lookup, containment, quantification, aggregation                                 | Respect Input `Sequence`, `Membership`, and `Association` semantics; do not invent order               |
| Local control and temporary state       | `for`, guarded `while`, branches, fresh accumulators, derived temporary material                     | Prove finite progress, non-escape, no Input alias, and deterministic result                            |
| Helpers and standard-library calls      | Exact-target helpers, selected library operations, locally resolved lambdas                          | Close reachable calls, initialization, captures, dispatch, implicit effects, and platform profiles     |
| Java Stream and Kotlin Sequence         | Input-derived finite pipelines, filters, mappings, reductions                                        | Recognize every stage, eliminate live host pipeline authority, preserve binder invocation and ordering |
| Patterns and regular expressions        | Matching, finite captures, supported pattern operations                                              | Close pattern syntax, Unicode behavior, capture semantics, finite supported work, and resource risk    |

Parallel or potentially open-ended streams are outside the initial V1 coverage unless the same semantic and completion
guarantees can actually be established. Open recursion and state-dependent loops remain unsupported when the required
completion proof is unavailable. Supporting an additional ordinary source form later does not itself modify Admission
meaning; adding an Admission-observable determinant or result requires an owning Contract decision.

## 3.1. Working source examples

IDL selects one exact class declaration rather than a source file or a named method. The class is an authoring
container;
its name and the names of its methods locate source but do not determine Admission Definition meaning. No particular
method name, including `admit`, is prescribed. Several differently named methods may participate in the same judgment
as closed private helpers. Reusing a method name within the selected class, including a legal host-language overload,
is an Admission authoring error. The frontend must resolve one complete judgment from the selected declaration and
its semantically contributing bodies. It must not choose a method by spelling or source order, nor combine unrelated
Boolean methods by an invented conjunction.

This Kotlin example uses an ordinary class, an immutable private constant, a private helper, and direct Input reads:

```kotlin
package example.orders

data class LineItem(val quantity: Int)
data class OrderInput(val amount: Long, val limit: Long, val items: List<LineItem>)

class PurchaseChecks {
    private val minimum = 100L

    private fun quantitiesValid(items: List<LineItem>): Boolean =
        items.all { it.quantity > 0 }

    fun permits(input: OrderInput): Boolean =
        input.amount >= minimum &&
                input.amount <= input.limit &&
                quantitiesValid(input.items)
}
```

The corresponding Java authoring form also uses an ordinary class and does not require a static method or explicit
constructor. The method names differ deliberately: they are not prescribed API entry names.

```java
package example.orders;

import java.util.List;

record LineItem(int quantity) {
}

record OrderInput(long amount, long limit, List<LineItem> items) {
}

class OrderConditions {
    private final long minimum = 100L;

    private boolean validQuantities(List<LineItem> items) {
        return items.stream().allMatch(item -> item.quantity() > 0);
    }

    boolean allows(OrderInput input) {
        return input.amount() >= minimum
                && input.amount() <= input.limit()
                && validQuantities(input.items());
    }
}
```

A corresponding IDL role selection identifies `PurchaseChecks` or `OrderConditions` in its exact source resolution
context; these are two independent example declarations, not two members of one Admission Definition. The frontend
reads the constant and helper bodies without constructing either class. It resolves Input observations and the complete
Boolean computation, including the collection binder. Java Stream and Kotlin collection syntax remain source evidence;
their host iterators, lambdas, and instances need not be retained in the realization.

The class cannot inherit judgment behavior, delegate evaluation to another application object, call an external
callback, or acquire values through a constructor or live initializer. A constant such as `private val minimum = 100L`
or `private final long minimum = 100L` is allowed because its value can be established from its declaration without
running an instance. A constructor that logs, reads the environment, or otherwise performs application behavior is not
an acceptable declaration, even when its effects do not contribute to the displayed predicate. An overload such as a
second method named `permits` in the Kotlin class is likewise rejected even if its parameter list differs. The same
restrictions apply to Java methods and constructors. Closed standard-library collection operations are not external
application callbacks; they remain eligible only when their full meaning and completion are established under Section 2.

## 3.2. Admission user-authoring expression inventory

This inventory spells out the source capabilities that the preceding coverage table leaves implicit. It covers the
ordinary Java and Kotlin forms needed to express Admission conditions, including the additional collection relations
under consideration. These are candidate expression forms within the agreed selected-class authoring model, not newly
established
Admission operations or an assertion that every listed library overload is implemented. The exact callable, source
version, legal value domain, and qualification result remain subject to Section 2.

Users read the exact Input-declared field or nested constituent in ordinary source. A Kotlin property access such as
`input.amount` and a Java record-component access such as `input.amount()` are examples of this authoring convention,
not mandatory host carrier forms. A getter, method reference, property delegate, or operator overload has no special
authority and must be resolved when its behavior contributes to the judgment. The authored Input reference does not
make the Input Definition identity part of the Admission Definition identity. Its legal Observation Requirements and
actual binding are determined under the owning Contract relations. Neither this inventory nor a matching host type
establishes that binding.

The following tables describe semantic coverage to qualify, rather than specifying a fixed set of JVM method calls.
Ordinary alternatives such as direct loops and collection helpers may express the same required judgment; Java and
Kotlin need not have identical source spellings. Each expression is eligible only if its reachable behavior is closed,
its required observations are legal, and every applicable Admission judgment completes as required by ADR-0065.

### 3.2.1. Boolean judgments and control flow

| Judgment capability                | Kotlin source candidates                  | Java source candidates                      | Qualification boundary                                                                                  |
|------------------------------------|-------------------------------------------|---------------------------------------------|---------------------------------------------------------------------------------------------------------|
| One complete Boolean judgment      | Boolean computation in the selected class | `boolean` computation in the selected class | Establish one complete judgment; class and member names are source selectors, not semantic authority    |
| Boolean literals and negation      | `true`, `false`, `!condition`             | `true`, `false`, `!condition`               | Exact Boolean value                                                                                     |
| Conjunction and disjunction        | `&&`, `\|\|`                              | `&&`, `\|\|`                                | Preserve short-circuit reachability and guards                                                          |
| Boolean equivalence and inequality | `a == b`, `a != b`                        | `a == b`, `a != b`                          | Compare the actual supported Boolean or value relation; reference identity is not Presentation Sameness |
| Relational comparisons             | `<`, `<=`, `>`, `>=`                      | `<`, `<=`, `>`, `>=`                        | Exact operand domains and comparison law                                                                |
| Conditional branches               | `if` / `else`                             | `if` / `else`                               | Finite closed paths and reachability                                                                    |
| Conditional expressions            | `if (c) a else b`                         | `c ? a : b`                                 | Same guard and branch-result meaning                                                                    |
| Finite alternatives                | `when`, closed `if` chains                | `switch`, closed `if` chains                | A complete, supported choice domain; no open runtime subtype discovery                                  |
| Early return                       | `return true`, `return false`             | `return true;`, `return false;`             | Early exits contribute only to the one complete Admission judgment                                      |
| Local immutable values             | `val result = ...`                        | `final` local variables                     | Complete local value meaning; host immutability marking alone proves nothing                            |
| Local accumulation                 | `var total = ...`                         | Ordinary local variables                    | Fresh, nonescaping state with closed reads and writes                                                   |
| Finite iteration                   | `for (item in items)`                     | Enhanced `for`, indexed `for`               | Exact finite traversal and legal binder order                                                           |
| Proven-progress iteration          | Guarded `while`                           | Guarded `while`                             | Completion must be proved from closed material                                                          |
| Guarded partial expressions        | `if (d != 0) n / d else ...`              | `d != 0 ? n / d : ...`                      | Every reachable partial operation has an established domain                                             |

### 3.2.2. Numeric and bitwise judgments

| Judgment capability                        | Kotlin source candidates                                   | Java source candidates                                  | Qualification boundary                                                                        |
|--------------------------------------------|------------------------------------------------------------|---------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| Signed fixed-width values                  | `Byte`, `Short`, `Int`, `Long` expressions                 | `byte`, `short`, `int`, `long` expressions              | Declared width, signedness, overflow, and promotion                                           |
| Unsigned integer domains                   | `UByte`, `UShort`, `UInt`, `ULong` operations              | Explicit unsigned operations and conversions            | Exact unsigned profile; a host representation is not a new Input Leaf                         |
| Arithmetic and unary signs                 | `+`, `-`, `*`, `/`, `%`, unary `+` / `-`                   | Same ordinary operators                                 | Overflow, rounding, remainder, zero division, and partial domains                             |
| Increment and decrement                    | Local `++`, `--`                                           | Local `++`, `--`                                        | Only judgment-local writes with established bounds and completion                             |
| Bit-level tests and operations             | `and`, `or`, `xor`, `inv`                                  | `&`, `\|`, `^`, `~`                                     | Bit width, signedness, and shift/promotion behavior                                           |
| Bit shifts                                 | `shl`, `shr`, `ushr`                                       | `<<`, `>>`, `>>>`                                       | Exact shift-distance and signed/unsigned behavior                                             |
| Explicit conversions                       | `toByte()`, `toInt()`, `toLong()`                          | Casts and exact conversion calls                        | Narrowing, truncation, and range behavior                                                     |
| Ranges and boundary tests                  | `x in low..high`, direct comparisons                       | Direct comparisons                                      | Endpoints and any constructed range have closed meaning                                       |
| Minimum, maximum, absolute value           | `minOf`, `maxOf`, `abs`                                    | `Math.min`, `Math.max`, `Math.abs`                      | Exact overload, exceptional domain, and integer-minimum behavior                              |
| Floating arithmetic and comparison         | `Float`, `Double`, comparisons                             | `float`, `double`, comparisons                          | NaN, infinities, signed zero, rounding, and evaluation order                                  |
| Floating classification and bit inspection | `isNaN`, `isInfinite`, supported raw-bit calls             | `Float.isNaN`, `Double.isInfinite`, supported bit calls | Preserve exact raw-bit distinctions when the selected law exposes them                        |
| Large integer and decimal computation      | `BigInteger` / `BigDecimal` methods or resolved operators  | `BigInteger` / `BigDecimal` methods                     | Exact operations, bounds, scale, precision, rounding, and partiality                          |
| Numeric vs presentation equality           | Qualified numeric comparison or exact component inspection | `compareTo`, `equals`, component access as qualified    | Never substitute host equality for Input Presentation Sameness or a selected numeric relation |

### 3.2.3. Text, characters, and bytes

| Judgment capability                        | Kotlin source candidates                                   | Java source candidates                                     | Qualification boundary                                                           |
|--------------------------------------------|------------------------------------------------------------|------------------------------------------------------------|----------------------------------------------------------------------------------|
| Exact text comparison                      | Supported `==`, `compareTo`                                | Supported `equals`, `compareTo`                            | Exact Unicode scalar sequence or separately established relation                 |
| Emptiness and length                       | `isEmpty()`, `length`, qualified scalar count              | `isEmpty()`, `length()`, qualified scalar count            | Code units, code points, and other length notions are not interchangeable        |
| Prefix, suffix, and containment            | `startsWith`, `endsWith`, `contains`                       | `startsWith`, `endsWith`, `contains`                       | Exact string/character overload and matching law                                 |
| Search and position                        | `indexOf`, `lastIndexOf`                                   | `indexOf`, `lastIndexOf`                                   | Exact position unit and not-found result                                         |
| Slicing and finite character traversal     | `substring`, finite loops, supported code-point operations | `substring`, finite loops, supported code-point operations | Legal positions, bounds, surrogate handling, and scalar interpretation           |
| Character classification and ranges        | Comparisons, supported `Char` predicates                   | Character comparisons and qualified `Character` methods    | Unicode profile and exact character domain                                       |
| Case mapping, normalization, and collation | Exact supported conversion/comparison calls                | Exact supported conversion/comparison calls                | Not provided by Input Text sameness; require a resolved, stable semantic profile |
| Byte sequence length and access            | `size`, `bytes[i]`                                         | `length`, `bytes[i]`                                       | Finite octet domain and supported bounds                                         |
| Byte sequence equality and slicing         | `contentEquals`, `copyOfRange` as qualified                | `Arrays.equals`, `Arrays.copyOfRange` as qualified         | Content versus host array identity, and exact slice bounds                       |
| Encoding and decoding                      | Qualified charset/codec calls                              | Qualified charset/codec calls                              | Exact encoding, invalid-sequence behavior, and separately ratified profile       |

### 3.2.4. Presence, closed choices, and nested values

| Judgment capability                  | Kotlin source candidates                          | Java source candidates                       | Qualification boundary                                                                                  |
|--------------------------------------|---------------------------------------------------|----------------------------------------------|---------------------------------------------------------------------------------------------------------|
| Direct and nested Input observations | `input.amount`, `input.address.zip`               | `input.amount()`, `input.address().zip()`    | Resolve exact declared coordinate and constituent; accessors are source evidence only                   |
| Presence and null-like alternatives  | `x == null`, `x != null`, `?.`, `?:`              | `x == null`, `x != null`, guarded access     | Distinguish an Input-declared presence law from absent coordinates or roles                             |
| Option-like alternatives             | Closed optional-case tests and payload reads      | Qualified `Optional` or closed case access   | Resolve the exact Input case and guard payload reads; do not assume all host option APIs are equivalent |
| Enum-like closed choices             | `when (status)`, supported enum equality          | `switch (status)`, enum equality             | Only exact declared alternatives; no implicit authority from ordinal or class identity                  |
| Closed tagged choice                 | Closed `when` or supported type/tag tests         | Closed `switch` or supported tag/type tests  | Resolve to Input-owned discriminant and payload, not runtime subtype discovery                          |
| Product constituents                 | Nested property reads and comparisons             | Nested record/accessor reads and comparisons | Each member uses its declared presence and value meaning                                                |
| Arrays and fixed extent              | Indexed access, `size`, finite loops              | Indexed access, `length`, finite loops       | Exact element meaning and established cardinality or extent                                             |
| Multiple-field conditions            | `a <= b`, conditional checks using several fields | The same value relations through accessors   | Every observation participates in the one Admission judgment                                            |

### 3.2.5. Finite collection observations

| Judgment capability                     | Kotlin source candidates                  | Java source candidates                      | Qualification boundary                                                              |
|-----------------------------------------|-------------------------------------------|---------------------------------------------|-------------------------------------------------------------------------------------|
| Cardinality and emptiness               | `size`, `isEmpty`, `isNotEmpty`           | `size()`, `isEmpty()`, array `length`       | Cardinality must be Input-observable                                                |
| Sequence index and range access         | `items[i]`, qualified `slice` / `subList` | `get(i)`, qualified `subList`, indexed loop | Position is intrinsic only to an Input `Sequence`; bounds must be guarded           |
| Membership test                         | `contains`, `in`                          | `contains`                                  | Exact Input constituent sameness or explicit derived membership relation            |
| Association key presence                | `containsKey`, `keys.contains`            | `containsKey`, `keySet().contains`          | Exact Input key relation and absent-key distinction                                 |
| Association lookup                      | `map[key]`, `getValue` as qualified       | `get`, qualified entry lookup               | Missing key must not be confused with a present null-like value                     |
| Association value and entry observation | `values`, `entries`, `keys`               | `values()`, `entrySet()`, `keySet()`        | Preserve declared key/value relations and any Input-visible encounter order         |
| Finite traversal                        | `for (x in items)`                        | Enhanced `for`, indexed `for`               | Source traversal order cannot manufacture Membership or Association order           |
| First, last, and positional neighbors   | `first`, `last`, index-based reads        | `get(0)`, last-index reads                  | Only an exact semantic or explicitly derived order may support position             |
| Nested collection traversal             | Nested `for`, nested element access       | Nested enhanced/indexed `for`               | Close every constituent domain, binder, and finite bound                            |
| Collection equality and relation tests  | Supported value comparisons               | Supported value comparisons                 | Host `equals`, ordering, or hashing does not automatically define Contract sameness |

### 3.2.6. Collection predicates, aggregation, and derived relations

The following inventory includes the additional requested areas: uniqueness, grouping, cross-collection relations,
pairwise and adjacent-element checks, Map entry relations, and collection matching. Their appearance here makes them
explicit V1 design candidates. It does not establish a new collection identity, mandatory order, equality law, or
runtime object representation.

| Judgment capability                     | Kotlin source candidates                          | Java source candidates                                      | Qualification boundary                                                                     |
|-----------------------------------------|---------------------------------------------------|-------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| Universal and existential conditions    | `all`, `any`, `none`                              | `allMatch`, `anyMatch`, `noneMatch`, finite loops           | Exact finite domain, binder, and short-circuit meaning                                     |
| Predicate count                         | `count { predicate }`                             | `filter(...).count()`, finite loops                         | Exact cardinality of qualifying members                                                    |
| Filtering and mapping                   | `filter`, `map`                                   | Stream `filter`, `map`, finite loops                        | Closed element function, no escaping or live pipeline                                      |
| Flattening nested finite domains        | `flatMap`, nested loops                           | `flatMap`, nested loops                                     | Finite nested constituents and precise traversal/invocation law                            |
| Summation and other aggregates          | `sum`, `sumOf`, `minOrNull`, `maxOrNull`          | Primitive Stream `sum`, `min`, `max`, loops                 | Exact arithmetic, empty-domain result, and order dependence                                |
| General accumulation                    | `fold`, `reduce`, local accumulator               | Stream `reduce`, finite accumulator loop                    | Seed, element order, combination law, and partiality                                       |
| Average and derived counts              | Qualified `average`, `count`, arithmetic          | Qualified Stream `average`, `count`, arithmetic             | Exact numeric type, rounding, and empty-domain result                                      |
| Pipeline composition                    | `filter(...).map(...).all(...)`                   | Stream `filter(...).map(...).allMatch(...)`                 | Entire chain is closed; intermediate host collections are not authority                    |
| Distinct elements and unique keys       | `distinct`, `distinctBy`, distinct-count equality | `distinct`, keyed projection, finite duplicate checks       | Exact equality for constituents or derived keys; hashing cannot silently redefine sameness |
| Grouping and group-wise conditions      | `groupBy`, `groupingBy`, `eachCount`, group `all` | Qualified `Collectors.groupingBy`, `counting`, loops        | Exact derived key relation, group membership, and per-group aggregation                    |
| Group-wise totals and bounds            | `groupBy(...).all { ... sumOf ... }`              | Qualified grouping and per-group reductions                 | Preserve numeric law, grouping relation, and finite nested work                            |
| Cross-collection inclusion              | `containsAll`, `all { it in other }`              | `containsAll`, `allMatch(other::contains)`, loops           | Explicit element relation; two host collection types do not determine it                   |
| Intersection and disjointness           | `intersect`, `any { it in other }`                | Qualified `retainAll` on fresh local data, membership loops | Exact membership and equality; mutation must remain local                                  |
| Adjacent-element relations              | `zipWithNext`, indexed-neighbor loop              | Indexed-neighbor `for` loop                                 | Requires Sequence or an exact derived order; preserve pair boundaries                      |
| Pairwise conditions                     | Nested `all` / `any`, indexed nested loops        | Nested `allMatch` / `anyMatch`, nested loops                | Specify whether identical occurrences and both pair directions are included                |
| Map-wide key, value, and entry rules    | `keys.all`, `values.all`, `entries.all`           | `keySet`, `values`, `entrySet` loops or Streams             | Key/value binding, missing-key distinctions, and traversal semantics                       |
| Positional collection pairing           | `zip`, indexed loops                              | Indexed loops, qualified paired traversal                   | Both Sequence domains, unequal-length handling, and exact pairing law                      |
| Key-based collection matching           | `associateBy`, qualified lookup and `all`         | Qualified `toMap`/lookup, finite matching loops             | Explicit match-key equality, duplicate-key policy, and missing match behavior              |
| Derived ordering and sorting            | `sorted`, `sortedBy`, `sortedWith`                | Qualified Stream `sorted`, local sort                       | Exact comparator, tie behavior, and any result-sensitive order                             |
| Derived slices, windows, and partitions | `take`, `drop`, `windowed`, `partition`           | Qualified bounded subranges and partition loops             | Finite extent, position law, and complete temporary result meaning                         |

### 3.2.7. Helpers, libraries, and local derived computation

| Judgment capability                      | Kotlin source candidates                                  | Java source candidates                                            | Qualification boundary                                                                                               |
|------------------------------------------|-----------------------------------------------------------|-------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| Exact-target private helper              | Direct call to a private method of the selected class     | Direct call to a private method of the selected class             | Close its body without application-object communication or dynamic dispatch                                          |
| Nested and factored helpers              | Several differently named private methods                 | Several differently named private methods                         | All contributing methods form one judgment; duplicate member method names, including overloads, are authoring errors |
| Local binder and lambda                  | `{ item -> ... }`, local lambda                           | `item -> ...`, finite stream lambda                               | Invocation and capture remain closed inside a qualified operation, never a user-supplied external callback           |
| Function or method references            | Qualified reference to a closed local helper              | Qualified private-helper reference                                | The exact binder and callable body must be known; no live callback receiver or external object                       |
| Kotlin extension or overloaded operation | Qualified extension/property/operator usage               | Qualified platform operation overloads                            | Resolve the exact supported platform meaning; external user-object dispatch is prohibited                            |
| Standard library value functions         | Qualified numeric, string, collection helpers             | Qualified `Math`, `String`, `Arrays`, collection helpers          | Exact callable and platform profile; a library entry is not an arbitrary application callback                        |
| Constants and closed initialization      | Literals, private `val` initialized from closed constants | Literals, private `final` field initialized from closed constants | Resolve the declared value without running constructors, initializers, or application objects                        |
| Fresh temporary product or collection    | Judgment-local construction and reads                     | Judgment-local construction and reads                             | Complete local meaning, no Input alias, shared state, or escape                                                      |
| Deterministic derived comparison         | Supported comparator and derived key                      | Supported comparator and derived key                              | New relation is explicit Admission meaning, not inherited from a host default                                        |

### 3.2.8. Patterns and regular expressions

| Judgment capability                  | Kotlin source candidates         | Java source candidates                  | Qualification boundary                                                   |
|--------------------------------------|----------------------------------|-----------------------------------------|--------------------------------------------------------------------------|
| Whole-text pattern match             | Qualified `Regex.matches`        | Qualified `Pattern` / `Matcher.matches` | Complete pattern syntax, Unicode basis, and exact match result           |
| Substring pattern search             | Qualified `containsMatchIn`      | Qualified `Matcher.find`                | Exact match selection, position, and completion behavior                 |
| Groups and captures                  | Qualified `MatchResult.groups`   | Qualified `Matcher.group`               | Exact captured span, optional group, and indexing law                    |
| Repeated finite matches              | Qualified finite match traversal | Qualified finite matcher iteration      | Finite advancement even for zero-length matches, plus supported work law |
| Literal pattern or character classes | Supported literal/pattern forms  | Supported literal/pattern forms         | Stable interpretation without inheriting an arbitrary host engine        |

### 3.2.9. Closed domain leaves and references

The Input semantic inventory includes the following closed Leaf families. They are not a grant to call every method on
a corresponding JVM class. The authoring candidates below are usable only when the selected operation preserves the
Leaf's exact distinctions or establishes an explicit, closed derived relation.

| Input value family       | Kotlin source candidates                                        | Java source candidates                                                     | Qualification boundary                                                          |
|--------------------------|-----------------------------------------------------------------|----------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| UUID                     | Exact value/bit comparisons and qualified component reads       | Qualified `UUID` equality and component reads                              | Exact 128-bit datum; no resource lookup                                         |
| Decimal                  | Qualified `BigDecimal` arithmetic, scale and numeric comparison | `BigDecimal` arithmetic, `scale`, `compareTo`, exact equality as qualified | Coefficient/scale presentation distinction and explicit numeric relation        |
| Binary32 / Binary64      | Qualified floating comparison, classification, raw-bit reads    | Qualified `Float` / `Double` operations and bit reads                      | Exact admitted bit datum versus numeric comparison                              |
| ISO Date                 | Qualified date components and comparisons                       | Qualified `LocalDate` component reads and comparisons                      | Fixed proleptic Gregorian meaning; no implicit current date                     |
| Local Time               | Qualified component reads and comparisons                       | Qualified `LocalTime` component reads and comparisons                      | Exact time-of-day domain and fractional resolution                              |
| Local Date-Time          | Qualified date/time components and comparisons                  | Qualified `LocalDateTime` component reads and comparisons                  | No implied offset, zone, or Instant interpretation                              |
| Numeric UTC Offset       | Qualified numeric offset comparison                             | Qualified `ZoneOffset` value comparison                                    | Exact numeric offset, not a named-zone rule                                     |
| Offset Date-Time         | Qualified local date-time/offset reads and comparisons          | Qualified `OffsetDateTime` component reads                                 | Preserve local value and offset; same-instant equivalence is separate           |
| Duration                 | Qualified duration value and fixed-unit relations               | Qualified `Duration` value and fixed-unit relations                        | Exact unit, resolution, range, and signed quantity                              |
| Instant                  | Qualified fixed-epoch coordinate relations                      | Qualified `Instant` component/coordinate relations                         | Exact epoch/unit/resolution; no clock acquisition                               |
| Zone Descriptor          | Qualified descriptor-kind and payload inspection                | Qualified descriptor-kind and payload inspection                           | No implicit time-zone database or rule lookup                                   |
| Resolved Zoned Date-Time | Qualified retained components and comparisons                   | Qualified `ZonedDateTime` component reads as qualified                     | Preserve local value, numeric offset, and descriptor; no live zone resolution   |
| URI Reference            | Qualified component-presence and text inspection                | Qualified `URI` component access as qualified                              | Syntactic closed reference; no dereference, normalization, or resource identity |

The preceding entries describe the complete candidate authoring coverage under the current scope, including
collection-relation capabilities not previously named in this Design. Neither a host return type nor a standard-library
method name alone establishes whether an expression is supported. A candidate whose meaning, finite work, or
Observation Requirements cannot be closed remains unsupported until the corresponding qualification succeeds. This
inventory does not change the Admission result vocabulary, add a user-facing Validation DSL, or resolve the separate
Authoring API declaration-shape decisions.

---

# 4. Frontend Acquisition and Refinement

## 4.1. Resolve the selected declaration

Resolve the exact Java or Kotlin class declaration selected through IDL. Validate its member-name uniqueness and
exclude inheritance, constructors that perform application behavior, object communication, and undeclared state.
Resolve one complete Admission judgment from the class's semantically contributing methods, closed private helpers,
and constant material. Member names do not select a privileged root, and several unrelated methods must not be joined
by an implicit Boolean operator. If the frontend cannot determine one unambiguous complete judgment meaning, it reports
an authoring or qualification error. Source provenance and formation dependencies remain separate from Admission
Definition determinants.

## 4.2. Resolve Input-facing observations

Translate supported field or property reads and nested constituent accesses into precise Input-declared Observation
Requirements. The frontend may use approved Input HIR observations for this work without importing the consulted Input
Candidate's identity into Admission Definition meaning. An observation is not authorized by a matching field name or
host type alone.

The mapping from those Input-facing observations to the final Boundary Presentation requires the revised inbound
composition law. Until that law is closed, the compiler must not assume that Admission always reads an uncanonicalized
`presentedInput` value.

## 4.3. Close reachable source behavior

Resolve the exact semantics of contributing private helper methods, operators, supported library calls, local lambdas,
and derived operations. Recognize closed constant declarations without executing the selected class or its initializers.
Application-object calls, external callbacks, inheritance, and execution-bearing constructors are not permitted as
Admission authoring mechanisms. Obtain platform semantic knowledge through the ADR-0073 boundary where required. A
qualified standard-library collection operation may be interpreted as a closed binder; its host callback or stream
object does not become a permitted runtime dependency. Do not execute application code to discover its meaning.

The frontend may eliminate provably unreachable source. If unreachability is not proven, rejecting unsupported source is
a permitted implementation coverage outcome rather than permission to execute it speculatively.

## 4.4. Produce complete HIR candidates

Lower closed authoring evidence into the complete Admission Definition Candidate and exact IDL Binding Candidate
governed by ADR-0071 and ADR-0065. The resulting HIR must express the full judgment law and Admission-owned Observation
Requirement. Host AST objects, host method identity, mutable runtime carriers, and transient compiler handles cannot
substitute for that meaning.

Recovery-tainted, incomplete, or unsupported source may remain available for diagnostics but must not cross the Visible
HIR boundary or enter Admission Definition Establishment. Once the Definition is established, downstream execution
formation consumes only legally exposed material and observations.

Source formatting, helper factoring, temporary allocation, analysis choices, and backend layout may change without
changing Admission Definition meaning when all legal observations remain the same. A change in Admission-owned semantic
determinants follows the existing identity and Version law.

---

# 5. Analysis and Source-Closure Work

The compiler may select CFG/SSA-based dataflow, effect analysis, exact call summaries, abstract interpretation,
guarded-domain analysis, termination reasoning, or another sound combination. This document does not prescribe one proof
technology. The implementation must nevertheless obtain sufficient evidence for every relevant legality obligation.

## 5.1. Reachability, dispatch, and initialization

Analyze the selected class before forming its complete judgment. Duplicate method names (including overloads),
explicit inheritance, object communication, and constructors or initializer blocks with application behavior are
illegal declaration forms, even if an offending member would not be reached by the eventual Boolean computation.
Within a legal declaration, analyze semantically reachable helper and library operations, including implicit field or
property access and qualified collection binders. An unresolved target makes the source unsupported; no fallback through
reflection, MethodHandle invocation, DI resolution, live class initialization, or host runtime evaluation may create
Admission meaning. Closed constant initializers are read as declarations, not executed as constructor effects.

## 5.2. Mutation, aliasing, and temporary structures

Allow fresh judgment-local accumulation only when relevant writes and reads are closed, the temporary state cannot alias
mutable Input or Definition material, and no reference escapes or becomes shared. Temporary collections or objects need
not survive execution formation. Verify these properties from the resolved behavior, not from host-language immutability
markings.

## 5.3. Partial operations, effects, and completion

Track guards and the paths on which partial operations are defined. Establish finite progress for supported loops and
bounded Input-derived traversal, and inspect exceptional behavior independently from normal Boolean control flow. An
exception handler cannot be treated as an implementation technique for manufacturing an Admission `Rejected` result.
Blocking, synchronization, external progress, and unclosed recursive cycles require explicit exclusion or separately
justified qualification.

## 5.4. Collection binders and ordering

Close each binder's invocation count, binding domain, and ordering semantics. For `Membership` and `Association`, do not
infer source order from incidental host iterators. A temporary derived order is usable only when the required ordering
and tie behavior are exact, or when the final judgment is invariant under every legal unresolved permutation. A derived
local relation is not automatically an Input-owned observation.

## 5.5. Pattern and library operation profiles

Close the exact callable or pattern semantics before selecting an implementation. Regex matching may be implemented by
an automaton, specialized matcher, or another method only if result, captures, Unicode basis, and work obligations are
preserved. Different library releases or overloads must not silently alter Admission meaning.

## 5.6. Analysis resource controls

Record the compiler's response to path explosion, deep helper structure, pattern complexity, nested collection
operations, and excessive summary growth. Source may have finite semantics while analysis is impractical. Exhausting a
compiler work limit must not be reported as Admission `Rejected` or as successful Definition Establishment.

---

# 6. HIR, Establishment, and Protocol Realization

The compiler must support the Admission Definition Candidate and IDL Binding Candidate reference domains, together with
the Established Definition and Occurrence reference domains. A physical row index, query key, object identity, address,
cache record, or fingerprint is not itself a Contract identity.

Complete and narrow logical projections need not each occupy separate objects or tables. Primitive backing may be shared
across projections when typed reference checking, producer-owned equality, completeness, and coherent visibility remain
recoverable. The same principle applies to compact basis and composition backing. A reverse index may accelerate
consumption without creating a reverse Contract relation.

ADR-0075 may govern qualification, delivery, and reuse of compiler observations. Query layout, dependency recording,
HID/fingerprints, persistence, lazy materialization, and physical access paths remain implementation choices. A
dedicated Admission manager or Composition manager is not required merely because the logical contracts are distinct.

Current Basis Binding and Applicability must be derived from legally established relations. Compiler reachability,
source type compatibility, or an earlier successful cached observation cannot establish them. The direct Input-only
Basis and occurrence attribution in the pre-revision ADR-0065 must be reconciled with Canonicalization before
implementing the final relation. Do not invent an extra Contract Authority or Material domain merely to simplify a
physical lookup.

Consumers must obtain legally required meaning through the producer-owned protocol rather than recovering it from the
author's method body, an internal table layout, a generated artifact, or an old compiler result.

---

# 7. Judgment Realization and Optimization

After legal Definition and Occurrence prerequisites are available, realization may replace host methods, helper
structure, collection pipelines, temporary objects, and a host regex engine with any implementation that preserves the
complete Admission-visible judgment and attribution.

Potential realization strategies include expression specialization, scalarization, constant evaluation, local-object
elimination, fused scans, primitive or columnar access, indexed lookup, vectorization, and alternate pattern matchers.
Input-derived pipelines need not materialize intermediate host collections. These are options for analysis and
experimentation, not mandated backend passes.

A transformation must preserve the exact result law, observed distinctions, reachable guards, partial-operation domains,
and failure/non-entry attribution. Side-effect freedom alone does not justify changing evaluation order, reassociating
floating arithmetic, parallelizing a reduction, or substituting a matcher with different Unicode or capture behavior.
Moving work across Canonicalization and Admission boundaries also requires proof that Contract-visible entry and refusal
semantics remain intact.

Establish an independently checkable reference-judgment path for comparing optimized and alternative realizations. The
verification approach may evolve, but an inconclusive proof or unsupported operation cannot be converted into a
successful Admission judgment. Resource-enforcement machinery remains owned by its applicable authority or
implementation boundary; runtime exhaustion does not become `Rejected`.

---

# 8. Diagnostics and Security Integration

The authoring API should not change merely because Canonicalization is absent, partially selected, or fully selected.
Diagnostic implementation must be able to relate the authored Input coordinate, any legally selected Canonical
Representative, the evaluated Admission condition, the actual result, and source provenance. It may expose original and
representative values only when the owning producers make those observations available and disclosure policy permits
their presentation.

Static diagnosis must distinguish a proven contradiction from a possible user-intent mismatch. Runtime diagnostics must
not claim to know which subcondition failed unless the necessary evidence is legally available. Compiler instrumentation
must not change the Admission judgment or promote diagnostic evidence into Contract authority.

The new Admission SADR will own the threat model, trust boundaries, attack paths, mitigation architecture, assurance
evidence, residual risk, and V1/V2 security scope. Design work must supply concrete implementation candidates and
negative tests for hidden source execution, ambient state, mutable aliasing, stale or incorrect basis, refusal bypass,
analysis amplification, ReDoS, invalid optimization, and diagnostic disclosure. Those controls must not silently
redefine the Admission result vocabulary.

---

# 9. V1 Realization and V2 Extension

V1 can use immutable or frozen observations, HID-based reuse, query-oriented compiler products, physical table packing,
reusable analysis, and parallel work where current qualification is exact. Reuse must be bound to the correct World,
Version, composition, Basis, and Admission Application; stale results cannot become current authority through a cache
hit.

V2 may change dependency representation, invalidation, scheduling, repair, and proof reuse without changing Admission
Definition or Occurrence meaning. Neither rustc-style pull evaluation nor a particular table schema, persistent format,
cache key, or realization manager is fixed here. Compiler product equality and physical reuse identity remain distinct
from producer-owned semantic equality.

Resource policy, verification depth, and performance targets will be specified in the owning compiler and security
designs. Finite semantic work is not a promise of constant-time execution, bounded implementation cost, or side-channel
resistance.

---

# 10. Open Implementation Decisions

| Subject                         | Decision required                                                                                                                           | Owning or dependent document                                         |
|---------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| Admission authoring surface     | Qualify exact Kotlin/Java member forms and diagnostics within the selected-class model; preserve all three Canonicalization selection cases | Admission Authoring API Design; ADR-0065 controls meaning            |
| V1 source coverage              | Which exact callable/construct variants meet the qualification record; which remain unsupported                                             | Frontend capability matrix; ADR-0073 for platform semantics          |
| Source closure                  | Practical analysis combination, summary boundaries, iteration/alias checks, negative diagnostics                                            | Compiler Frontend/Verification Design                                |
| Input observation mapping       | Map Input-facing authored observations to the final legally determining operand                                                             | ADR-0065, ADR-0066, ADR-0048; then Frontend Design                   |
| Admission Basis and attribution | Final selected/omitted Canonicalization relations, required cardinality, exact occurrence attribution                                       | ADR-0065, ADR-0063, ADR-0048; then Protocol Design                   |
| Protocol backing                | Compact primitive representation, shared projections, reverse indexes, coherent materialization                                             | ADR-0075 and Compiler Material Design                                |
| Execution and performance       | Reference evaluator, optimization candidates, cost model, matchers, resource enforcement                                                    | Compiler Execution/Optimization Design; owning Budget/Capacity rules |
| Threat mitigation               | Trusted analysis inputs, proof integrity, resource amplification, stale material, diagnostics exposure                                      | New Admission SADR and Security Design                               |
| V2 incremental support          | Dependencies, revalidation, repair, and proof reuse without new Contract identity                                                           | Compiler V2 TODO and Incremental Design                              |

A candidate can be marked implemented only after its exact meaning, negative cases, and conformance evidence are
available. Open ADR-dependent questions must not be decided implicitly by a convenient frontend representation or
backend shortcut.

---

# 11. Implementation Readiness and Conformance Evidence

Before selecting a V1 source construct or implementation strategy, test that normal and adversarial examples agree with
the governing Admission semantics. The minimum evidence must distinguish supported source from compiler-unsupported
source, correctly attributed refusal and non-entry from `Rejected`, and successful reference evaluation from optimized
realization.

Cover Input-only observations, partly Canonicalized observations, and fully Canonicalized observations once their Basis
laws are finalized. Test selected Java and Kotlin classes with different names for their judgment methods, multiple
private helpers, and closed private constants. Reject duplicate method names including overloads, explicit inheritance,
constructor or initializer side effects, external object calls, and callbacks. Also exercise supported collection
binders,
implicit getters, invalid dispatch, mutable captures, open streams, exceptional paths, nonterminating or excessive work,
unordered collection traversal, floating reductions, Unicode and regex behavior, and resource exhaustion. Test
revalidation of reused material under changed World, Version, composition, and input application context.

A completed implementation qualification should identify its exact source subject, relevant ADR obligations, supporting
semantic evidence, negative and adversarial tests, resource limits, and any unsupported corner cases. Findings that
require a different Admission operand, authority, result, or established relation must return to the owning ADR rather
than being hidden in a compiler implementation decision.