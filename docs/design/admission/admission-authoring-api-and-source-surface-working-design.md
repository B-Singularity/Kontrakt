# Admission Authoring API and Source Surface — Working Design

## Status

Working Design. The Admission Authoring API is not yet fully specified. This document records its scope, existing
constraints, the reviewed declaration, helper, Input-observation, and expression/control-flow source forms, and
questions remaining for later reviews. A reviewed form is supported only within its stated qualification; mentioning
an operation family does not approve its unreviewed overloads or arbitrary platform callables.

## Date

2026-10-18

## Governing Documents

- ADR-0065: Admission Contract, Explicit Continuation Judgment, and Deterministic Evaluation Boundary.
- ADR-0064: Input Contract.
- ADR-0066: Canonicalization Contract.
- ADR-0046 and ADR-0047: interface and role selection and 1D authoring boundaries.
- ADR-0048: inbound 1D composition.
- ADR-0053: Version Contract.
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition.
- ADR-0071: Resolved Contract HIR semantic boundary.
- ADR-0073: JVM platform-native semantic ratification and external Contract infiltration boundary.
- ADR-0075: compiler result ownership, legal consumption, and deterministic realization.
- ADR-0077: Contract Relation Infrastructure and lawful relation preparation.
- `docs/design/overview/kontrakt-compiler-total-architecture-map-design-draft.md`: compiler-wide working architecture,
  for later implementation work only.

---

# 1. Purpose and Ownership

This Design concerns the **user-facing Java and Kotlin forms** by which an author expresses one Admission Definition. It
will specify the selected declaration, composition of its authored methods into one Admission judgment, admissible
Input-facing observations, locally expressed computations, recognized source and library operations, and actionable
authoring diagnostics. These are the forms that later frontend, verification, and realization designs must understand.

ADR-0065 determines which Admission judgments are legal and what they mean. This Design will determine how users may
*write* those judgments. It cannot confer authority on a host method, instance, library, generated artifact, or compiler
representation. An ordinary Java or Kotlin expression is not an accepted Admission API merely because it compiles or
evaluates to `Boolean`.

The user should write ordinary supported Java or Kotlin instead of constructing Kontrakt IR or learning a second
judgment language. Internal semantic references, operation profiles, summaries, compiler handles, and projections remain
compiler concerns. The declaration's host type, method body, and generated interface do not independently establish
Contract authority.

This is **not** the Admission frontend, middle-end, backend, runtime, verification, or optimization Design. It must
leave those systems enough exact source meaning to implement the selected API, but it does not prescribe their
algorithms, intermediate data structures, query scheduling, physical storage, or execution plans.

The concrete API for each operation family will be designed separately with review. The inventories below are work to be
covered, not a completed V1 support matrix. Earlier exploratory syntax or examples are not ratified by being mentioned
in this Design.

---

# 2. Decisions Already Governing the API

## 2.1. Declaration and selection

The selected Admission source is an ordinary Java or Kotlin **class declaration** identified exactly through IDL. One
selected declaration represents one flat Admission Contract. All of its authored methods belong to that one Admission
source; none becomes an independently selectable Contract. The class is not instantiated as a validator, and its host
name and members do not themselves establish Contract identity.

No particular method name, including `admit`, is prescribed. One Boolean method or several independent Boolean
condition methods may express the judgment. Independent conditions compose by logical conjunction; an exact-target
Boolean helper contributes where called, not again as an independent condition. Differently named private helpers may
contribute to the judgment, and reusing a method name within the selected class, including a host-language overload,
is an authoring error. No privileged root method, annotation, getter or setter member, or additional IDL method
selection is part of the Admission authoring form. ADR-0065 Section 5.2 owns this composition law.

Inheritance, overriding, dynamic specialization, object communication, and external callbacks cannot supply Admission
authority. Constructors or initializer blocks that perform application behavior are not permitted, even when their
effects do not contribute to the judgment. Closed constants may be read as declarations without executing a class or
instance. Neither `val` nor `final` alone proves such closure. ADR-0065 Sections 5 and 7.6 own these restrictions.

## 2.2. Input-facing authoring and final operand

Admission authoring remains Input-shaped whether Canonicalization is explicitly absent, selected for some coordinates,
or selected throughout the applicable Input. The legal judgment operand comes from Input Presentation and, where
selected, the final Canonicalization representatives. The complete Required Basis, attribution, and source
correspondence are already governed by ADR-0065 Sections 6.3–6.8 and ADR-0048.

The API must allow authors to describe required Input observations without making the particular Input Definition or
host carrier identity part of the reusable Admission Definition. As in the Canonicalization authoring API, an
Input-facing parameter or constituent is bound by the **exact declared Input coordinate or constituent name** in its
applicable scope. This is a source-addressing rule, not proof that the namesake values are semantically compatible:
shape, presence, sameness, Canonicalization applicability, and the existing composition law must also be qualified.
An absent, ambiguous, or mismatched name is not repaired by argument position, similar host types, source order, or
a guessed synonym. An author-facing read must correspond to a legal Input-owned distinction. The API does not expose
internal Input or Canonicalization references as ordinary user values.

## 2.3. The judgment remains closed

One legal Admission occurrence completes with exactly one `Admitted` or `Rejected`. Source-level branches, local values,
helpers, loops, and finite collection operations may contribute to that one judgment; they do not create independently
selectable Contracts or extra Admission results.

The author's computation may not depend on hidden environmental observations, application-object execution, unresolved
dispatch, or escaping shared state. A guarded partial operation is not made legal by catching a host exception and
interpreting it as `Rejected`. The full restrictions are in ADR-0065 Sections 7–11; they are not redefined here.

---

# 3. API Questions to Resolve Before Specification

## 3.1. Declaration and class-wide method composition

IDL selects the Admission **class**, not a method within it. An ordinary package-qualified Java or Kotlin class name,
or the existing lawful import resolution, must resolve to one exact source declaration under ADR-0065 Section 5.1.
Source file paths may help the frontend locate that declaration but do not establish Admission meaning or identity.
No additional IDL root selector, distinguished method name, or annotation is introduced.

All authored methods of the selected class are Admission authoring material. One Boolean method can express the entire
judgment, or multiple independent Boolean methods can contribute conditions joined by logical AND. Exact-target helper
calls supply their meaning at the call site. A Boolean method used as a helper is not automatically added once more as
an independent condition. Neither source order, visibility, nor the number of methods creates separate Admission
judgments. The resulting meaning is one complete Boolean continuation judgment, under ADR-0065 Sections 5.2 and 11.
The frontend must bind each contributing Input-facing parameter or constituent by its exact declared Input name,
under the same coordinate-naming approach already used for Canonicalization. Identical names in the same applicable
Input scope identify the same requested coordinate; a matching name does not waive shape or presentation-law checks.
Host types alone and parameter positions never select a coordinate.

The following examples show the **agreed class-level composition forms**. They are illustrative source examples, not
approval of every parameter type, callable overload, or Input carrier under the later V1 support catalog.

Independent conditions in Kotlin:

```kotlin
package example.contract

class RangeAdmission {
    fun positive(value: Int): Boolean = value > 0
    fun small(value: Int): Boolean = value < 1000
}
```

Independent conditions in Java:

```java
package example.contract;

public final class RangeAdmission {
    public boolean positive(int value) {
        return value > 0;
    }

    public boolean small(int value) {
        return value < 1000;
    }
}
```

When `value` is the exact name of a lawful Input coordinate, both parameters address that coordinate. After the
required presentation and composition checks, the independent conditions express `(value > 0) && (value < 1000)`.
The same judgment may instead be written through one Boolean method or through private helpers. For example:

```kotlin
class RangeAdmission {
    fun valid(value: Int): Boolean = positive(value) && small(value)

    private fun positive(value: Int): Boolean = value > 0
    private fun small(value: Int): Boolean = value < 1000
}
```

Here `positive` and `small` contribute through the exact calls in `valid`; they are not also independent conditions.
This is not a requirement to introduce a `valid` method or any other root. The compiler may simplify the resolved
Boolean meaning when its equivalence obligations are met, but the choice of optimizer and physical expression form
belongs to later implementation designs.

Every authored member remains subject to Admission source qualification. Getters and setters in the selected Admission
class are not permitted. An initializer with forbidden execution effects remains invalid even when no Boolean condition
calls it. Helpers must satisfy exact-target closure, and semantic cycles and hidden ambient observations remain
forbidden
under ADR-0065 Sections 7–8. An otherwise unused helper is not, solely for being unused, assigned an independent
Boolean condition or rejected under an invented call-graph reachability rule. No unique call-graph entry point is
required. The existing prohibition on duplicate method names, including overloads, remains intact.

**Remaining declaration API details to specify** include the complete language/version-specific signature and
modifier catalog, full instance/static receiver coverage, and source-only versus binary-only acquisition conditions.
Input-facing coordinate-name correspondence and the reviewed basic helper/constant forms are recorded below; their
exact qualified overloads remain subject to the later V1 catalog. Source acquisition, resolved-target analysis, and
physical computation remain assigned to the later compiler designs in Section 6.

### 3.1.1. Reviewed helper and local computation forms — bundle 2

The selected class may contain differently named private Kotlin or Java helper methods with zero or several
parameters, returning `Boolean` or another qualified closed value. A helper may call another exactly resolved helper
provided that the resulting computation is closed and has no prohibited semantic cycle. An ordinary instance method is
a supported authoring form; a Java private static helper is a **conditional** form requiring the same exact-target and
initialization checks. Kotlin extension receivers, general generic helpers, default-argument lowering, and varargs are
not approved merely because ordinary helpers are supported. The prohibition on repeated method names, including
overloads, remains unchanged.

```kotlin
class RangeAdmission {
    fun positive(amount: Int): Boolean = above(amount, 0)
    fun small(amount: Int): Boolean = below(amount, limit())

    private fun above(value: Int, bound: Int): Boolean = value > bound
    private fun below(value: Int, bound: Int): Boolean = value < bound
    private fun limit(): Int = 1000
}
```

```java
public final class RangeAdmission {
    public boolean positive(int amount) {
        return above(amount, 0);
    }

    public boolean small(int amount) {
        return below(amount, limit());
    }

    private boolean above(int value, int bound) {
        return value > bound;
    }

    private boolean below(int value, int bound) {
        return value < bound;
    }

    private int limit() {
        return 1000;
    }
}
```

In these examples, `amount` is the authored Input-facing name. The private parameters `value` and `bound` are local
helper parameters bound by the explicit call arguments, not independently matched to Input coordinates. The examples
assume an exact legal Input coordinate named `amount` and an appropriate integer operation profile.

Kotlin named local functions are supported when their closure can be resolved. Capture of an already-established,
immutable judgment-local value is supported; an arbitrary object receiver or escaping function value is not. Lambdas
and callable references are not free-standing Admission callbacks. They may be accepted inside an individually
qualified finite binder with an exact invocation law. Fresh mutable capture is conditional on that law being known,
sequential where necessary, and compatible with the judgment-local escape and alias restrictions of ADR-0065 Section
7.2. Java and Kotlin do not share the same capture restrictions; the frontend must respect the actual language form.

```kotlin
fun bounded(amount: Int): Boolean {
    val limit = 1000
    fun below(value: Int): Boolean = value < limit
    return below(amount)
}
```

Local Kotlin `val`, Java local `final` or effectively final values, and fresh non-escaping scalar accumulators are
supported under their exact value and update semantics. Local mutation does **not** admit mutation of Input material,
Definition data, class fields, static fields, shared state, or an escaping alias. A fresh mutable collection builder
requires its own subsequently qualified source and escape profile; acceptance of scalar accumulators does not approve
all mutable collections or mutating library methods.

### 3.1.2. Reviewed constants, properties, and initialization — bundle 2

An exact closed literal or constant expression may contribute a value without executing user code. Kotlin method-local
`val`, Java constant variables of primitive or `String` type initialized by constant expressions, and Kotlin
companion `const val` in a **constant-only companion** are supported source candidates subject to their exact value
law. The Kotlin companion form exposes a compile-time value; it does not authorize the companion object, its methods,
state, or initialization as runtime Admission behavior. A Java `static final` or Kotlin `val` declaration which merely
holds a reference, computes a value at runtime, or retains mutable backing does not qualify by its modifier alone.

```kotlin
class MaximumAdmission {
    private companion object {
        const val LIMIT = 1000
    }

    fun small(amount: Int): Boolean = amount < LIMIT
}
```

```java
public final class MaximumAdmission {
    private static final int LIMIT = 1000;

    public boolean small(int amount) {
        return amount < LIMIT;
    }
}
```

Ordinary class-level Kotlin properties and instance fields are not admitted as a way to store Admission calculation
state. Kotlin delegated or lazy properties, `lateinit`, mutable `var` members, Java mutable fields, and general
instance `final` fields are not covered by this closed-constant allowance. Getters and setters of the selected
Admission class remain forbidden. A constructor's mere syntactic presence is distinct from executing it; an empty
constructor is not a new Contract operation. Constructor parameters as persistent state and effectful constructor or
initializer bodies are not authorized. An initializer with hidden effects remains illegal even if its value is unused.

### 3.1.3. Whole-source legality and later reachability optimization — bundle 2

The authoring source must pass its applicable legality and closure checks independently of whether later optimizer
analysis can prove that a computation is unreachable. A closed, legal branch, constant expression, or unused private
helper may subsequently be eliminated when the compiler proves that removal preserves the complete judgment.
Unreachability does **not** license external clocks, I/O, ambient reads, forbidden object communication, or an
otherwise prohibited authored form. This is not a requirement that all private helpers contribute a Boolean condition.
An independent Boolean method is composed under Section 3.1; an unused non-condition helper is neither automatically
conjoined nor an error solely because it has no callers.

This reviewed API rule has a **normative reconciliation pending**: ADR-0065 Section 7.5 currently permits discarding a
provably unreachable branch containing unsupported syntax. The later user review requires legality qualification before
optimization and does not adopt that exception. This Working Design cannot override the ADR. The owning ADR must be
amended or the difference otherwise resolved before publishing a V1 conformance rule; no compiler optimization may be
used to silently decide this legal question.

A declared independent condition is not automatically a short-circuit guard for another condition. A partial read or
operation must have its domain established on each path that reaches it under the actual source meaning. Compiler
scheduling, reordering, and elimination must preserve that guard and the resulting Admission judgment. Whole-class
constructor/initializer restrictions continue to apply separately.

### 3.1.4. Minimum conformance cases — bundle 2

The later exact API catalog must verify private Boolean and non-Boolean helpers, multi-parameter helper argument
binding, repeated exact helper use, conditional Java static helpers, closed local function capture, closed constant
expressions, and finite fresh scalar accumulation. Negative cases include overloads with the same method name,
recursive semantic cycles, instance or static mutable state, effectful initialization, escaping captured mutation,
and a helper that performs external I/O or ambient observation. A legal unused helper is not rejected merely for being
unused; its authored legality must nevertheless be checked. For a branch proven unreachable, qualification and later
elimination are separate checks, subject to the unresolved ADR-0065 Section 7.5 discrepancy recorded above.

## 3.2. Input-facing values and collection shapes

Specify which ordinary source forms expose the exact Input Presentation, including scalar, nested, optional,
closed-choice, array, `Sequence`, `Membership`, and `Association` meanings. The source projection must preserve
Input-owned presence, constituent sameness, uniqueness, key relations, and observable order. Kotlin `List`, `Set`, and
`Map`, Java collections, getters, and generated views are candidates for an authoring surface, not automatic substitutes
for those laws.

**Collection size is not an additional Input-owned operation.** An actual finite collection's cardinality may be derived
from its complete Presentation. A Definition-time maximum cardinality is not required merely because its extent is
finite. Likewise, keyed lookup derives its meaning from exact `Association` relations and key sameness; it does not
become a fundamental Input presentation relation. The later API review must decide the lawful source spellings and their
missing-key, absence, and bounds behavior, rather than assuming host collection conventions are authoritative.

### 3.2.1. Exact authored names and direct observations — bundle 3

An Input-facing source parameter is addressed by the exact Input coordinate name, just as Canonicalization binds a
direct coordinate by its authored name. A Named Product constituent is addressed by its exact name within the
applicable product scope. Two independent methods that declare `amount` therefore request the same coordinate when
that exact coordinate exists; the language type, source position, or an inferred spelling does not supply a fallback.
Names bind **which observation is requested**, not whether its meaning is compatible. Presentation shape, absence,
selected Canonicalization representatives, applicability, and Composition must still be checked separately. A private
helper's parameter is instead bound by its explicit call arguments and does not need to repeat an Input coordinate
name. A whole-presentation parameter or nested host path may be accepted only if its mapping to exact named
observations has been separately qualified; it is not a new implicit binding mechanism.

For supported direct carriers, the proposed scalar and named-product read spellings are Kotlin access to a declared
immutable `val` constituent and Java access to a record component, for example `input.amount` or `input.amount()`.
These are *source projections* of Input-owned observations, not permission to invoke arbitrary getters. The selected
Admission class still cannot declare getters or setters. Kotlin delegated or custom getters, Java Bean getters,
reflection access, host object identity, and general user-defined record accessor behavior are not accepted merely
because the syntax resembles a read. The ordinary carrier or its property accessor cannot introduce new Input meaning
through execution.

Nested Named Product observations are semantically legitimate, but this does not automatically ratify nested DTOs as
V1 zero-adapter direct carriers. Admission may use a constituent path only after each mapping is qualified under
ADR-0064 Sections 7.9–7.11. Compiler recognition of the path must not be confused with supporting arbitrary mutable
or recursively discovered host object graphs.

### 3.2.2. Presence and closed alternatives — bundle 3

Kotlin `== null`, `!= null`, `?.`, and `?:` are supported source candidates when each possible state maps to a
**declared** Input presence or closed alternative. `!!` is conditional on a proof that its operand is present on
all reaching paths; an exception is not an Admission `Rejected` value. Kotlin `as?` or `is` is not a general license
to inspect runtime subtypes. The host language must not create an Input absence or default that its Input law did not
declare.

For Java, `Optional.isPresent()`, `Optional.isEmpty()`, and guarded `Optional.get()` are qualified candidates only when
an exact two-case Input meaning is established. `orElse` and `orElseGet` must be examined separately because the
fallback expression and supplier do not have the same evaluation semantics. `orElseThrow` is not a default V1
judgment form. An arbitrary `Optional` wrapper is not itself proof that the Input declared absence.

For closed choices, Kotlin `when` and Java `switch`, and exact Enum case comparisons, are supported where the source
cases resolve to the Input's complete nominal Case Domain. Case payload access requires the appropriate selected case
and exact constituent mapping. Host enum `ordinal`, custom `name`/`toString`-based logic, runtime subtype inspection,
and a platform enum's iteration order cannot silently become Input Case Identity or Input order. Full exhaustive-case
and language-version behavior must still be qualified for the exact `when` or `switch` form.

### 3.2.3. External Kotlin and Java enum declarations — bundle 3

An enum referenced by Admission **may be declared outside the selected Admission class**, in another file, package,
module, or dependency, without another IDL Admission selector or annotation. Exact source or classfile declaration
evidence must resolve the enum type and its finite named cases. A supported enum source contributes only the closed
nominal Case Domain needed by the Input presentation and Admission condition. Sharing the spelling of one case across
different enum declarations does not make those cases identical.

```kotlin
// order/Status.kt
package order

enum class Status { CREATED, PAID, CANCELLED }
```

```kotlin
// contract/StatusAdmission.kt
package contract

import order.Status

class StatusAdmission {
    fun paid(status: Status): Boolean = status == Status.PAID
}
```

```java
// order/Status.java
package order;

public enum Status {CREATED, PAID, CANCELLED}
```

```java
// contract/StatusAdmission.java
package contract;

import order.Status;

public final class StatusAdmission {
    public boolean paid(Status status) {
        return status == Status.PAID;
    }
}
```

The `status` source parameter must match the exact applicable Input coordinate named `status` and pass the ordinary
Input presentation checks. These are case-only examples, not blanket approval of a Java/Kotlin enum class's methods.
An external enum may declare fields, custom accessors, methods, constant-specific class bodies, constructors, or
initializers. Their existence does not forbid *case-only* use if the compiler can isolate the nominal cases without
executing that code or giving the code Admission authority. Reading a custom property, calling a user-defined method,
or observing a mutable field is not implicitly authorized by the enum's acceptance as a choice carrier.

A Java enum constant access can trigger class initialization at runtime. The compiler must not execute application
enum initialization to acquire case declarations, and the realization must not accidentally introduce initializers,
user-defined enum behavior, or other hidden effects into Core judgment execution. If this separation cannot be
verified, direct enum use is unsupported for that path; a legally formed pre-Input presentation may be used instead
under the existing boundary law. Runtime enum object identity and its `ordinal` are not authoritative Case Identity.

External enum lookup must select one exact declaration and Case Domain across the intended source and execution
inputs. Dependency or case changes, duplicate binary names, classpath shadowing, variant classfiles, and multi-release
JARs require qualification or invalidation of affected compiler evidence. A Case Domain addition or deletion requires
rechecking closed-choice compatibility. Mere case declaration order is not a Contract-visible case order. Compiler
storage or index choices, including an `ordinal`-like implementation index, are permitted only when their mapping to
the exact nominal cases has been verified; they are implementation, not Contract meaning.

### 3.2.4. Finite collection observation forms — bundle 3

A collection read is permitted only when it projects a distinction already owned by the Established Input. The table
records reviewed candidate source spellings; it does not approve all overloads, derived transforms, or any host
collection implementation simply by accepting its interface.

| Input presentation         | Kotlin source candidates                  | Java source candidates                         | Qualification required                                                               |
|----------------------------|-------------------------------------------|------------------------------------------------|--------------------------------------------------------------------------------------|
| `Sequence` cardinality     | `size`, `isEmpty()`                       | `size()`, `isEmpty()`                          | Exact finite presentation; cardinality is derived, not a new Input-owned observation |
| `Sequence` position        | `seq[index]`, `first()`, `last()`         | `list.get(index)` and qualified endpoint reads | Exact position, bounds and nonempty guards; same sequence and index law              |
| `Membership`               | `element in set`, `set.contains(element)` | `set.contains(element)`                        | Exact Input Presentation Sameness; no host equality or invented order                |
| `Association` key presence | `map.containsKey(key)`                    | `map.containsKey(key)`                         | Exact key presence and key Presentation Sameness                                     |
| `Association` value lookup | `map[key]`, guarded `getValue(key)`       | `map.get(key)`                                 | Preserve missing key versus present null-like value and partial lookup domain        |
| Derived Association views  | `keys`, `values`, `entries`               | `keySet()`, `values()`, `entrySet()`           | Derived immutable presentation; no live host view or implicit iteration order        |

Input collection cardinality comes from the complete finite presentation; a finite collection need not have a fixed
Definition-time maximum. A host `size` returning `Int` must be representationally qualified without redefining the
Contract cardinality domain. Association lookup derives from exact key relations; `Map.get(key) == null` cannot by
itself establish key absence when a null-like value can be present. `getOrDefault` or `getValue` require their own
presence, domain, and evaluation profiles. Neither ordinary `HashSet` equality, `HashMap` hashing, an implementation's
iterator, nor a live `Map` view can supply missing sameness or ordering semantics.

For `Membership` or `Association`, positional observations are not allowed under `No Observable Order`. Encounter order
or an exact declared total order may support additional operations after its law is established. A locally derived
order must be explicitly meaningful, with ties treated according to the declared relation. Actual iteration,
quantifiers, mapping, filtering, sorting, and reductions remain individually qualified collection/binder APIs to be
reviewed later. Approving `contains`, lookup, or cardinality does not ratify a host library wholesale.

### 3.2.5. Source access does not choose the physical representation

The frontend must resolve the selected Kotlin/Java member or callable to an exact Input observation requirement. It
must establish the authored name correspondence and the applicable shape, presence, constituent, order, and sameness
law. Where Canonicalization is selected, Admission observes the final lawful representative of that coordinate; the
source projection cannot reopen an original carrier read. Input and Canonicalization Definition or Occurrence
identities are not introduced into the ordinary authored value API.

The compiler may implement a verified read as a primitive load, column/slab access, immutable field projection,
specialized lookup, or another representation without making that mechanism part of Contract authority. Source
accessor execution, collection storage, JVM reflection objects, hashes, generated wrappers, and cache handles do not
own the observation's meaning. Correctness obligations include exact current binding, immutable presentation,
absence/sameness/order semantics, and correspondence to the Established Input and Canonicalization bases. The
algorithms and physical storage stay with the frontend, verification, and realization designs in Section 6.

### 3.2.6. Minimum conformance cases — bundle 3

The later conformance corpus must distinguish a valid exact-name coordinate binding from a missing name, a same-typed
but differently named coordinate, a colliding name in another product scope, and a legitimate private helper
parameter. It must test Kotlin direct immutable-carrier observation and Java record component reads separately from
custom effectful getters, delegated properties, binary-only declarations without sufficient source naming evidence,
and unauthorized runtime reflection.

External enums require separate tests for same-project and dependency-defined Case Domains, same case spelling in
different nominal enums, added or deleted cases, reordered declarations, custom enum fields/methods, effectful static
initialization, duplicate binary names, and mismatched runtime artifacts. A supported case-only observation must not
execute an enum method or initializer or acquire a contract-visible `ordinal` meaning. Nullable and Optional reads
must test true absence, present null-like values, unsafe unguarded extraction, and eager versus deferred fallback.
Finite collection observations must test empty and singleton domains, out-of-range positions, host-versus-Input
sameness differences, unspecified iteration order, absent Association keys, present null-like values, and views backed
by mutable collections. Where Canonicalization is selected, the observed value must come from the final lawful
representative. These tests qualify source mappings and realization correctness; they do not add Contract laws.

## 3.3. Expression and control-flow forms

Bundle 4 reviews Boolean and bitwise expressions, type-specific equality, conditional branches, early returns, guards,
finite loops, and loop control. Ordinary supported Kotlin and Java syntax may express the one Admission judgment without
another validation DSL. Each expression must still resolve to an exact, closed operation under ADR-0065 Sections 7–8.
The Kotlin and Java spellings below are reviewed authoring forms, not approval of every overload or of user-defined
operators with the same name. The type-specific numeric, collection, and callable catalogs remain separate work.

### 3.3.1. Boolean operations and evaluation order — bundle 4

Boolean literals `true` and `false`, unary `!`, parentheses, Kotlin and Java `&&` and `||`, and Boolean value `==` and
`!=` are supported. `&&` evaluates its right operand only if its left operand is true. `||` evaluates its right operand
only if its left operand is false. Their conditional evaluation is part of the authored computation, including any
guard it establishes. Kotlin `Boolean.and`, `Boolean.or`, and `Boolean.xor`, normally written as `a and b`, `a or b`,
and
`a xor b`, and Java `boolean` `&`, `|`, and `^` are also supported as **eager** Boolean operations: both operands are
evaluated. Kotlin infix names must resolve to those exact standard Boolean callables; they do not authorize unrelated
methods. Java boxed `Boolean` additionally requires a qualified unboxing and presence domain.

```kotlin
class DivisionAdmission {
    fun valid(denominator: Int): Boolean =
        denominator != 0 && 100 / denominator > 2
}
```

Replacing `&&` with Kotlin Boolean `and` in this example evaluates the division even when `denominator` is zero.
The eager form then fails the required partial-operation domain. Two independent class-level Boolean conditions are
conjoined into one Admission judgment under Section 3.1, but that conjunction does **not** invent a left-to-right
short-circuit guard between the independent source methods. Source evaluation dependencies and any complete ordinary-
condition Diagnostic obligations must be preserved under ADR-0065 Section 11.

### 3.3.2. Exact equality and comparison meanings — bundle 4

The token `==` does not select one universal relation. Its legal meaning depends on the resolved source operation,
operand types, and value domain. It is not automatically Input Presentation Sameness, and the same token inside a
collection predicate does not acquire a different equality merely because it is inside a collection. The following
source profiles are distinguished:

| Authored comparison                                    | Reviewed treatment                                                                                                                                                                       |
|--------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Kotlin/Java Boolean and fixed-width integer `==`, `!=` | Support the exact Boolean or integer value comparison.                                                                                                                                   |
| Kotlin/Java primitive floating `==`, `!=`              | Support the exact numeric equality profile, including NaN and signed-zero behavior; do not replace it with bitwise Presentation Sameness.                                                |
| Decimal and other structured leaves                    | Qualify the exact source type and callable before approval. Numerical comparison and Presentation-sensitive equality need not coincide.                                                  |
| A collection element `element == target`               | Use that element operation's exact qualified equality. Do not silently rewrite it to the collection's membership relation.                                                               |
| `set.contains(target)`, `map.containsKey(key)`         | Use the applicable Input constituent or key Presentation Sameness under ADR-0064, not host `equals` or `hashCode`.                                                                       |
| Whole-collection `a == b`                              | Conditionally support only an exact, complete structural comparison that preserves every distinction of the applicable presentations. Do not execute arbitrary host collection `equals`. |
| Exact external enum case `status == Status.PAID`       | Support nominal closed-case comparison under Section 3.2.3; no enum initialization or user method is thereby authorized.                                                                 |
| Kotlin `===`/`!==` or Java reference `==`/`!=`         | Do not use host object identity as Contract value equality.                                                                                                                              |

For whole-collection comparison, an Input `Sequence` preserves positions and the sameness of constituents.
`Membership` and `Association` preserve their exact members, keys, associated values, and any **Input-visible**
encounter
or exact ordered-by distinction. `No Observable Order` does not admit physical iterator order as a comparison input.
Nested presentations compare their lawful constituents according to their exact structural meaning. The spelling
`a == b` is supported for these forms only after the frontend can map it to such a complete comparison. The supported
host collection class alone is insufficient evidence. Kotlin arrays' reference-oriented `==` is not silently
reinterpreted as `contentEquals`; array-wide comparison needs its own exact profile.

For example, numeric `Double` comparison regards `-0.0` and `+0.0` as equal but Input Binary64 Presentation Sameness
preserves their distinct bits. A NaN also makes numeric equality non-reflexive, so numeric `==` cannot be the general
Membership uniqueness or hashing relation. A Decimal comparison may equate `2.0` and `2.00` numerically although their
coefficient/scale presentations differ. Kotlin equality for a statically known floating operand also needs to be
distinguished from a boxed value's `equals`. The frontend must resolve the **actual** operation rather than relying on
the visible token alone.

A compiler may not replace `values.any { it == target }` with `values.contains(target)` until the element predicate,
Input Presentation Sameness, complete finite domain, and result/guard semantics are proved equivalent. The same rule
applies to `distinct`, grouping, hash lookup, set membership, and collection equality optimizations. This review does
not yet approve the individual `any`, `distinct`, or grouping overloads; their callable and binder profiles belong to
Section 3.4. Likewise, `compareTo`, range membership, host `equals`, and generic object comparisons require their own
exact type and callable qualifications.

### 3.3.3. Integral bitwise and shift operators — bundle 4

Admission may write a closed bit-mask condition using Kotlin's integer `and`, `or`, `xor`, `inv()`, `shl`, `shr`, and
`ushr`, and Java's integral `&`, `|`, `^`, `~`, `<<`, `>>`, and `>>>`. These are integer operations, distinct from the
eager Boolean operators in Section 3.3.1. The supported baseline is exact `Int`/`Long` and Java `int`/`long` usage;
`Byte`, `Short`, unsigned Kotlin types, mixed numeric operands, conversions, and individual helper functions still
require their type-specific profiles. An operator spelling never approves an unrelated overloaded callable.

```kotlin
class PermissionAdmission {
    fun readable(flags: Int): Boolean = (flags and 0b0001) != 0
    fun writable(flags: Int): Boolean = (flags and 0b0010) != 0
}
```

```java
public final class PermissionAdmission {
    public boolean readable(int flags) {
        return (flags & 0b0001) != 0;
    }

    public boolean writable(int flags) {
        return (flags & 0b0010) != 0;
    }
}
```

Each operation must preserve the exact width, signedness, promotion, overflow, narrowing, and shift-distance law of
its ratified source profile. For 32-bit shifts the effective distance uses the low five bits; for 64-bit shifts it
uses the low six bits. Arithmetic right shift sign-extends, whereas logical right shift inserts zeros. A compiler
may not reinterpret fixed-width shifts as arbitrary-precision mathematical shifts. Java compound bitwise assignments
and equivalent Kotlin reassignment are permitted only when they update a legally fresh, non-escaping local variable
under Section 3.1.1; they do not permit mutation of Input or class state. `bitCount`, leading/trailing-zero counts,
rotations, and other bit utilities are later exact numeric callable profiles, not automatically approved here.

### 3.3.4. Conditional forms and returns — bundle 4

Kotlin and Java `if`/`else`, nested and chained branches, Kotlin `if` expressions and `when`, Java conditional `?:`,
and ordinary `return` or early `return` are supported with their actual control-flow semantics. Kotlin `when` takes
the first matching branch. Java `switch` statements and expressions, arrow rules, colon-form fall-through, and
expression `yield` are qualified by their exact Java language version, branch control, and completion behavior.
Closed enum cases and guarded payload reads still follow Section 3.2.2–3.2.3. A supported `switch` does not grant
runtime type inspection; pattern-based cases require a separate exact closed-choice profile. Missing or overlapping
branches may be accepted only if all applicable execution paths still produce a legal completed judgment.

```kotlin
class AmountAdmission {
    fun acceptable(amount: Int): Boolean = when {
        amount <= 0 -> false
        amount < 1000 -> true
        else -> false
    }
}
```

Reordering these `when` branches may change the result. A rewrite to `(amount > 0) && (amount < 1000)` is a compiler
optimization allowed only after equivalence is established. An early return from a private helper returns from that
helper's calculation; it does not establish a second Admission outcome. Kotlin local-function return, labeled
`return@label`, Java lambda return, and Kotlin inline-lambda non-local return have different targets. Ordinary local
return is supported; the latter callable- or label-dependent forms are conditional on exact target resolution and
an approved invocation profile rather than globally approved by the `return` keyword.

### 3.3.5. Guard, partial operation, and exceptional control — bundle 4

A division, index, optional extraction, case payload read, or other partial operation must be defined on **every
source path that reaches it**. Kotlin and Java short-circuit expressions, guarded `if` branches, and early returns may
establish a legal guard when their exact value and path correspondence is verified. A separate independently conjoined
Boolean method is not an implicit guard for another method's partial operation. A Boolean result on sampled inputs or
a host exception caught on a failing path is not proof of source legality.

Host exception control is not a third Admission outcome. `throw`, exception-based `try`/`catch` conversion to a
Boolean, Kotlin `require`/`check`/`error`, and Java/Kotlin `assert` are not V1 judgment authoring forms. In particular,
Java assertion enablement cannot determine whether an Admission condition is evaluated. `requireNotNull`,
`checkNotNull`, and `Objects.requireNonNull` are not implicitly approved as substitutes for an explicit presence
guard. Potentially closed specialized forms still require a separate review rather than a blanket permission for
exceptions or library methods.

The authored code must satisfy its source legality requirements **before** reachability-based optimization. Once a
closed source is legal, the compiler may simplify or eliminate branches and calculations proved not to affect the
judgment while preserving guarded domains, all required bases, result, and Diagnostic obligations. The normative
ADR-0065 Section 7.5 inconsistency about removing unsupported unreachable syntax remains recorded in Section 3.1.3;
this Working Design does not silently settle that ADR conflict.

### 3.3.6. Finite loops, `break`, and `continue` — bundle 4

Kotlin `for`, Java enhanced `for`, and counted `for` over an exactly known finite domain are supported when their
source operations and completion are qualified. Kotlin and Java `while` and `do-while` are conditionally supported
when finite progress and completion can be established for every legal input. A finite Input extent alone does not
prove a counted loop terminates: host integer overflow or a missing increment can defeat progress. Fresh scalar
accumulation obeys Section 3.1.1, and any collection iteration obeys ADR-0065 Section 7.4.

Ordinary loop `break` and `continue` are supported **as source control flow**, not as new Contract primitives.
`break` exits its exact loop target; `continue` begins the next permitted iteration under the language's actual
control-flow law. In a Java counted `for`, the update expression still runs following `continue`, unlike an increment
placed after `continue` in a `while` body. Both cases must be modeled exactly for termination and value analysis.
Java `break` from a `switch` is distinct from breaking a loop. Labeled breaks and continues, and Kotlin non-local
loop control across inline lambdas, are conditional on exact target resolution, language version, and completion
proof; their general availability is not implied by supporting the ordinary forms.

For `Membership` or `Association` with `No Observable Order`, early exit is legal only if the **final judgment is
invariant under every lawful visit order**. In particular, `break` may implement a finite existential predicate
without selecting a contract-visible first element:

```kotlin
class PositiveAdmission {
    fun exists(values: Set<Int>): Boolean {
        var found = false
        for (value in values) {
            if (value > 0) {
                found = true
                break
            }
        }
        return found
    }
}
```

This illustrates an exact, qualified finite Membership traversal, not automatic approval of every host `Set` iterator.
The result is independent of the visit order. Conversely, setting `selected = value`, breaking on the first positive
member, and returning `selected == 7` does **not** have invariant meaning for an unordered Membership containing both
`7` and `8`. That authored computation is unsupported under `No Observable Order`. An Input `Sequence` or a legally
observable encounter/total order may supply the required distinction instead. `continue` is similarly legal for a
closed filter-like computation but may invalidate a `while` loop's progress when it skips the required update.
Neither control statement is an unconditional source authorization independent of order and termination.

An optimized replacement of an existential loop by `any` or `contains` requires proof of the same element equality,
Input Presentation Sameness where applicable, guards, result, finite domain, and required Diagnostic semantics. The
source `break` does not itself authorize the backend to use a host iterator's first element, host hashing, or a
parallel callback execution policy.

### 3.3.7. Remaining callable-dependent control forms — bundle 4

Kotlin `repeat`, `let`, `run`, `also`, `apply`, `with`, `takeIf`, and `takeUnless` and their lambda/control effects are
not approved collectively. Each exact standard-library callable, receiver, overload, invocation count, escape rule,
and conditional evaluation law must be qualified before it becomes a supported Admission source form. This is also
true for Kotlin inline non-local returns and Java/Kotlin functional-interface-based helpers. A callable that permits
unknown dispatch, duplicated invocation, shared-state capture, or external effects cannot obtain authority by
resembling ordinary control-flow syntax. The full callable and binder catalog remains with Sections 3.4 and 3.5.

### 3.3.8. Minimum conformance cases — bundle 4

The exact V1 catalog and verification corpus must include short-circuit versus eager Boolean operations, division by
zero under a real guard and under a merely independent condition, Java boxed Boolean null handling, and Kotlin/Java
operator-target mismatches. Floating tests must distinguish signed zero, NaN, boxed equality, and exact Input bit
sameness. Decimal tests must distinguish numerical comparison from coefficient/scale Presentation Sameness. Collection
tests must distinguish element `==`, `contains` or `containsKey`, whole-collection equality with and without observable
order, and a prohibited optimization from one relation to another without proof.

Bitwise tests must cover 32-/64-bit width, shift counts equal to or greater than the width, arithmetic versus logical
right shift, numeric promotions, negative values, and attempted Input mutation through compound assignment. Control-
flow tests must cover overlapping Kotlin `when` cases, Java switch fall-through and `yield`, early helper returns,
guarded partial operations, illegal exception conversion, and exact return targets. Loop tests must cover a valid
order-invariant existential `break`, a first-member-dependent invalid `break` over unordered Membership, a legal
`continue`, a `while` loop whose `continue` prevents progress, Java counted-`for` update semantics, overflow that
prevents termination, and a labeled/non-local jump without exact qualification. Legal but unreachable computations
must be checked for source legality before later elimination; forbidden source in an unreachable branch does not
receive an optimization-based legality exemption under the reviewed rule, subject to the outstanding ADR-0065 Section
7.5 reconciliation.

## 3.4. Collection computations and derived relations

Review finite traversal, quantification, counting, aggregation, filtering, mapping, flattening, grouping, ordering,
cross-collection matching, and nested collection computation. The work is to establish which **authored expressions**
will be recognized, what Input relations they require, and how they express closed Admission-local derived meaning.

A host iterator cannot create observable order for unordered `Membership` or `Association`. Nor may a host default
equality, comparator, hashing rule, or callback invocation order silently determine a Contract-visible result. A derived
local relation may be used only under its own exact supported meaning. The later API design must address empty domains,
duplicates, optional elements, tie cases, short-circuit reachability, and composition of collection expressions without
requiring intermediate host collections as Contract material. Section 3.3.2 has reviewed the distinction between
element comparison, Input Presentation Sameness, and whole-collection equality; it does not approve individual
collection binder or equality overloads. Section 3.3.6 has reviewed source-level early loop exit but requires exact
order-invariant meaning when the Input supplies no observable order.

## 3.5. Helpers, callables, and platform libraries

Sections 3.1.1–3.1.3 record the reviewed Private Helper, named local function, local value/capture, restricted
mutation, and closed constant authoring forms. This section retains the **unreviewed exact callable and library
profiles**: remaining type-specific operators, binders, helper variants, receivers, overloads, implicit accessors or
comparisons, language versions, and platform semantics. Merely labeling a method `pure` or making a property read-only
does not
establish closure.

Review exact numerical, text, binary, decimal, temporal, identifier, regular-expression, and other closed-domain
operations as candidates. Do not admit entire libraries or every overload at once. ADR-0073 owns any required
ratification of platform meaning; the API document will record the author-visible qualified forms and their limitations.

## 3.6. Authoring diagnostics and support status

Define how the user learns that a declaration or expression is invalid, unsupported by V1, or cannot be resolved
precisely. Diagnostics should distinguish an authoring restriction from missing compiler qualification evidence and from
a legally entered Admission `Rejected` outcome.

The API should make it possible to relate an authored condition to exact semantic observations and source provenance. It
does not prescribe a Diagnostic Evidence schema, collection witness algorithm, execution trace, or publication policy.
Failure and Diagnostic remain owned by their governing documents.

---

# 4. Qualification Questions for Each Future API Form

When a specific API form is proposed, its review must answer the following questions. This section defines the review
obligation; it does not resolve any individual callable or syntax family.

| Review concern           | What the future API specification must make explicit                                                                                  |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| Authored form            | Exact Kotlin/Java declaration or expression, applicable overload and language/platform conditions                                     |
| Semantic input           | Input-owned observations and any closed Definition-local values required by the form                                                  |
| Result meaning           | Exact values, comparisons, ordering, equality, presence, and exceptional-domain distinctions that matter                              |
| Control and completion   | Relevant guards, reachable branches, finite domains, and legal completion conditions                                                  |
| Hidden behavior          | Getters, initialization, helpers, captures, mutation, dispatch, and other effects that require qualification                          |
| Collection relationships | Constituent identity, key sameness, order, cardinality derivation, empty-domain behavior, and derived local relations when applicable |
| Diagnostic surface       | How an authoring error can be identified without conflating it with a Contract result; necessary provenance obligations               |
| Support decision         | V1 approved, conditionally qualified, unsupported, or postponed, with clear limits rather than a blanket library claim                |
| Conformance cases        | Positive, negative, boundary, and adversarial source examples to be written and reviewed for that form                                |

A source family must not be declared supported merely because some neighboring method or overload has an established
profile. Approval must identify the exact legal surface. Unproven or unsupported source remains a compiler qualification
matter; it does not establish Admission Definition Refusal or a runtime `Rejected` result.

---

# 5. Coverage Inventory for the Later Joint Design

This inventory preserves the subjects previously identified for V1 consideration. **No listed category or API is
approved merely by appearing here.** Exact method names, signatures, sample implementations, and per-operation rules
will be drafted during subsequent reviews.

| Area for later design        | Scope to examine                                                                                            |
|------------------------------|-------------------------------------------------------------------------------------------------------------|
| Declaration and Input access | Selected class, independent Boolean conditions, helpers and constants, scalar and nested Input observation  |
| Boolean and control flow     | Reviewed Boolean/bitwise operators, exact equality, guards, branches, returns and finite loop control       |
| Numeric values               | Exact numeric types, floating/decimal relations, remaining bit utilities, conversions and overloads         |
| Closed alternatives          | Optional and absent values, enums, tagged choices, product constituents                                     |
| Text and binary              | Characters, Unicode-aware text, byte sequences, exact comparisons and finite derived operations             |
| Other closed leaves          | Temporal values, identifiers, syntactic references, and qualified value operations                          |
| Arrays and collections       | Finite arrays; Input `Sequence`, `Membership`, `Association`; traversal, cardinality, lookup and membership |
| Derived collection work      | Quantification, reduction, pipelines, ordering, grouping, matching and nested finite work                   |
| Calls and local computation  | Closed private helpers, binders, local temporary state, qualified standard-library operations               |
| Patterns                     | Exact pattern matching and regular-expression forms with qualified semantic profiles                        |

Parallel or potentially open-ended streams, unsupported recursion, unresolved dynamic behavior, hidden environmental
reads, and arbitrary user callbacks are not made legal by placement in this inventory. Their treatment must follow
ADR-0065's applicable source and completion laws. An expansion of source coverage must not silently change the Admission
operand, outcomes, Definition identity, or Contract Authority.

---

# 6. Boundaries to Other Design Work

This revision deliberately removes the former compiler-design chapters. The following work remains required for
implementation but is **not decided in the Authoring API Design**.

| Removed from this document                                                                                               | Owner of the later implementation decision                           |
|--------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| Exact source acquisition, method-relation resolution, and source-to-Input mapping                                        | Admission Frontend Design and common frontend architecture           |
| Call graph, effect and alias analysis, guard/termination proofs, and source qualification algorithms                     | Compiler Frontend and Verification Design                            |
| Formation of complete HIR Candidates, Seal, protocol materialization, and Established backing                            | ADR-0065/0071/0063 and Compiler HIR/Material Design                  |
| Runtime realization, reference execution, CFG/SSA, specialized execution and backend code generation                     | Admission Middle-end/Execution/Backend Design                        |
| Constant folding, helper inlining, loop and pipeline fusion, reduction specialization, vectorization, and cost decisions | Compiler Optimization Design and independent verification            |
| Diagnostic Evidence collection, witness selection, Failure processing, and outward disclosure                            | Diagnostic/Failure laws and their realization designs                |
| Threat model, analysis resource exhaustion, regex denial of service, runtime budget enforcement, and side channels       | Security, Compiler Verification, Budget/Capacity and Backend Designs |
| Frozen/HID caching, query layout, physical slab representation, incremental invalidation, scheduling and repair          | Compiler architecture and V1/V2 incremental designs                  |

The API must not prevent lawful optimization, but **this document does not select optimization passes**. Later
realizations must preserve the exact judgment, guards, final legal Presentation, result, Basis attribution, Failure
source, and any separately required Diagnostic Evidence. An API spelling does not require a particular intermediate
representation, host object, iterator, stream, or allocation at runtime.

When later implementation work discovers that a desired source form cannot be represented without changing the
Contract's semantic input, result, or authority, that issue must return to the owning ADR. An implementation convenience
cannot resolve it by fiat.

---

# 7. Order of the Remaining API Reviews

Bundles 1–4 have reviewed class-level composition, basic Helper/Constant/Local forms, Input-facing observation
source candidates, and Boolean, bitwise, equality, and control-flow source forms. Exact-name coordinate correspondence
and external case-only enums are recorded in Sections 3.1–3.2; the reviewed expression and loop-control rules are in
Section 3.3. Conditional forms still require their stated qualification. The whole-source legality/unreachability
difference noted in Section 3.1.3 also requires reconciliation with ADR-0065 before the conformance policy is finalized.

Next review the remaining type-specific numeric and bit-utility overloads, then finite collections and derived
operations, followed by callable/library profiles and authoring diagnostics. Finally assemble the exact V1 support
catalog and conformance corpus. Each review must produce actual Kotlin and Java source forms, allowed and rejected
examples, exact semantic obligations, and an explicit support status. The present reviews do not settle all
parameters, overloads, library methods, carrier mappings, or implementation algorithms.

The current design must not be treated as a complete public Admission API until those reviews are accepted.