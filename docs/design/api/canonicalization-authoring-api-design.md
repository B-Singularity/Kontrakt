# Canonicalization Authoring API Design

## Status

Working Design

## Date

2026-10-07

## Governing ADRs

- ADR-0066: Canonicalization Contract, Inbound Representation Control, Stable Representative, Canonical Bytes, and
  Explicit Omission
- ADR-0076: Canonicalization Built-In Law Catalog
- ADR-0046: IDL-First Interface Contract Frontend and 1D Contract Catalog
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0064: Input Contract
- ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary

---

# 1. Purpose

This document owns the user-facing Canonicalization authoring API design.

ADR-0066 owns Canonicalization meaning and the authoring semantic boundary. ADR-0076 owns the exact Built-In Law
Authorities and their versioned meanings. This Design chooses how application source names those already-defined
semantics on Kotlin/JVM.

The API is not Contract authority by itself. Changing a public class, object, helper name, package, or other
host-language
shape does not change Canonicalization meaning when the same ADR-defined declaration meaning is resolved.

This document contains API design only. Frontend extraction, HIR storage, Establishment, evaluator generation,
classfile handling, cache representation, and runtime canonicalizer implementation belong to their respective compiler
and realization designs.

---

# 2. API Goals

The Canonicalization API should make the common case short and keep composition explicit.

A user should be able to express three cases directly:

```text
coordinate not declared
    -> Canonicalization does not apply to that coordinate

coordinate declares one exact Built-In Law
    -> direct binding

coordinate declares an ordered composition of exact Built-In Laws
    -> composed binding
```

The API should not require a second Canonicalization DSL, generic type-list construction, callback implementation,
custom
canonicalizer object, descriptor builder, or runtime registration.

The source should remain valid ordinary Kotlin so that ordinary tooling can parse and compile it. The source form is an
authoring surface only; it is not intended to be executed by application code.

---

# 3. Working Kotlin Surface

The current working shape is:

```kotlin
class CustomerCanonicalization private constructor(
    customerId: UnicodeNfc,

    displayName: CanonicalComposition = compose(
        UnicodeWhitespaceTrim,
        UnicodeNfc,
        UnicodeDefaultCaseFold,
    ),

    regionCode: AsciiUppercase,
)
```

This example fixes only the API shape being explored here. The law names are illustrative. A name becomes a real public
law only when the Built-In Catalog admits the corresponding exact law and the API projection is approved.

The IDL continues to select the declaration itself:

```text
canonicalization CustomerCanonicalization
```

The IDL does not list the individual laws or composition steps.

---

# 4. Direct Binding API

A direct binding uses the exact Built-In Law symbol as the coordinate parameter type.

```kotlin
class CustomerCanonicalization private constructor(
    customerId: UnicodeNfc,
    regionCode: AsciiUppercase,
)
```

Only coordinates that require Canonicalization appear in this declaration. An Input coordinate omitted from the
Canonicalization declaration is not routed through an identity law.

The API therefore does not provide an `ExactCanonicalization` filler merely to represent absence.

A direct binding must use the exact concrete law symbol. A common marker type is not itself a selectable law.

The following shape is not a direct law declaration:

```kotlin
class CustomerCanonicalization private constructor(
    customerId: CanonicalizationLawRef,
)
```

---

# 5. Composition API

A composed binding uses one dedicated composition result type and one dedicated authoring operation.

The working names are:

```text
CanonicalComposition
compose(...)
```

The working source form is:

```kotlin
class CustomerCanonicalization private constructor(
    displayName: CanonicalComposition = compose(
        UnicodeWhitespaceTrim,
        UnicodeNfc,
        UnicodeDefaultCaseFold,
    ),
)
```

Argument order is source-visible and intentional. For illustration, the two ordered forms below denote different
declarations:

```text
compose(A, B, C)
compose(C, B, A)
```

The API does not expose a fluent chain such as:

```text
A.then(B).then(C)
```

The flat `compose(...)` form keeps one visible composition boundary and avoids making each law symbol look like a
runtime object that owns executable chaining behavior.

The API also does not use generic type lists such as:

```text
Compose<A, B, C>
```

Canonicalization composition should not turn Kotlin generic nesting into the Contract composition language.

---

# 6. Public Marker Shape

A compile-valid Kotlin projection can use sealed marker types and exact law objects.

The working public shape is conceptually:

```kotlin
public sealed interface CanonicalizationLawRef

public object UnicodeNfc : CanonicalizationLawRef
public object UnicodeWhitespaceTrim : CanonicalizationLawRef
public object UnicodeDefaultCaseFold : CanonicalizationLawRef
public object AsciiUppercase : CanonicalizationLawRef

public class CanonicalComposition internal constructor()

public fun compose(
    first: CanonicalizationLawRef,
    second: CanonicalizationLawRef,
    vararg rest: CanonicalizationLawRef,
): CanonicalComposition = CanonicalComposition()
```

Using an `object` gives one symbol that can appear both as an exact nominal type in a direct binding and as a direct
source reference in a composition expression.

The marker interface is sealed so application code cannot add a new Built-In Law implementation through ordinary
subtyping.

`compose` requires at least two law references. A one-law declaration should use the direct binding form instead.

`CanonicalComposition` has no public construction surface other than the authoring operation and exposes no
canonicalizer
methods.

The exact package names, visibility refinements, annotations, binary stubs, and documentation annotations remain open in
this Design.

---

# 7. Accepted Composition Source Shape

The intended V1 authoring form is deliberately narrow.

Accepted shape:

```kotlin
class CustomerCanonicalization private constructor(
    displayName: CanonicalComposition = compose(
        UnicodeWhitespaceTrim,
        UnicodeNfc,
        UnicodeDefaultCaseFold,
    ),
)
```

Each argument is a direct reference to one exact Kontrakt-provided Built-In Law symbol.

The following forms are outside the V1 API contract even when Kotlin itself can type-check them:

```text
val selected = UnicodeNfc
compose(selected, UnicodeWhitespaceTrim)
```

```text
compose(
    if (flag) UnicodeNfc else UnicodeNfd,
    UnicodeWhitespaceTrim,
)
```

```text
compose(
    chooseLaw(),
    UnicodeWhitespaceTrim,
)
```

```text
compose(
    registry.currentLaw,
    UnicodeWhitespaceTrim,
)
```

The public API may be capable of expressing some of these as ordinary Kotlin values, but they are not supported
Canonicalization authoring forms.

---

# 8. Flat Composition

Composition is flat in V1.

`CanonicalComposition` does not implement `CanonicalizationLawRef`. That keeps a composition result from being supplied
as another composition element through the ordinary API type relation.

The following therefore does not belong to the supported surface:

```text
compose(
    UnicodeNfc,
    compose(
        UnicodeWhitespaceTrim,
        UnicodeDefaultCaseFold,
    ),
)
```

A curated combined Built-In Law is different. If the Catalog publishes one exact combined law, that law is one ordinary
`CanonicalizationLawRef` and may be selected directly or used as one constituent in another legal Definition-level
composition.

---

# 9. Declaration Shape

The Canonicalization declaration remains a sparse declaration over direct Input coordinates.

Example Input:

```kotlin
data class CustomerInput(
    val customerId: String,
    val displayName: String,
    val regionCode: String,
    val note: String,
)
```

Example Canonicalization declaration:

```kotlin
class CustomerCanonicalization private constructor(
    customerId: UnicodeNfc,

    displayName: CanonicalComposition = compose(
        UnicodeWhitespaceTrim,
        UnicodeNfc,
        UnicodeDefaultCaseFold,
    ),

    regionCode: AsciiUppercase,
)
```

`note` is absent. The API does not require a placeholder for it.

The declaration itself remains non-instantiable application source. Its constructor parameters are authoring
coordinates,
not a runtime configuration object.

The API does not introduce a second canonical output carrier.

---

# 10. Unsupported API Shapes

The V1 public surface should not provide these forms:

```text
CanonicalSequence<A, B, C>
```

```text
canonicalize {
    trim()
    if (condition) lowercase()
}
```

```text
registerCanonicalizer(...)
```

```text
class MyLaw : CanonicalizationLawRef
```

```text
compose(lawsFromRuntime)
```

```text
UnicodeNfc.apply(value)
```

The Built-In Law symbols are authoring references. They are not public executable canonicalizer instances.

---

# 11. Common Combined Laws

The API should continue to expose a single exact Built-In Law when a standard or Kontrakt Catalog decision owns the
combined meaning as one law.

For example, if the Catalog admits one exact combined Unicode law, the preferred API is conceptually:

```kotlin
class AccountCanonicalization private constructor(
    username: UnicodeNfkcCaseFold,
)
```

rather than requiring every user to reconstruct that standard-defined meaning through `compose(...)`.

A Built-In combined law and a user-declared composition remain different API declarations even when their current output
appears equal.

---

# 12. API Diagnostics Expected by the Surface

The authoring API should support precise diagnostics for at least these mistakes:

```text
unknown coordinate
coordinate not present in selected Input
unsupported law for coordinate presentation
composition with fewer than two laws
non-exact law reference
application-defined law symbol
dynamic or computed composition member
nested composition
illegal law ordering or incompatible composition
semantic cycle
unstable whole composition
whole-composition boundedness failure
```

Provably redundant or fully shadowed steps must not disappear silently. The final severity and wording of those
diagnostics remain outside this API Design.

---

# 13. Java Projection

The semantic authoring model must remain projectable to Java, but this Design does not yet choose the exact Java source
spelling for composition.

A Java projection must preserve the same visible distinctions:

```text
direct exact law binding
finite ordered composition
sparse coordinate declaration
no executable user canonicalizer
no runtime law selection
```

The Java surface should be designed separately rather than forcing Kotlin into a shape chosen only because it maps more
literally to Java syntax.

---

# 14. Open API Decisions

The following remain Design decisions:

```text
exact package names
exact public law symbol names
whether the marker is named CanonicalizationLawRef or another term
whether the composition result is named CanonicalComposition or another term
whether the authoring operation is named compose or another term
annotations or IDE metadata used to mark authoring-only symbols
exact Java projection
source-level misuse diagnostics
binary compatibility policy for authoring symbols
```

These decisions may change without changing ADR-0066 when the resulting API still expresses the same Contract meaning.

---

# 15. Current Working Direction

The working Kotlin API direction is:

```kotlin
class CustomerCanonicalization private constructor(
    customerId: UnicodeNfc,

    displayName: CanonicalComposition = compose(
        UnicodeWhitespaceTrim,
        UnicodeNfc,
        UnicodeDefaultCaseFold,
    ),

    regionCode: AsciiUppercase,
)
```

This keeps direct law selection in the type position, introduces composition only where composition is needed, avoids
semantic generics, keeps the IDL unchanged, and leaves Canonicalization authority in the ADR-defined law rather than in
the public Kotlin mechanism used to express it.