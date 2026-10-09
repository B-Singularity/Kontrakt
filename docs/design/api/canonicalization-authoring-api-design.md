# Canonicalization Authoring API Design

## Status

Working Design

## Date

2026-10-10

## Governing ADRs

- ADR-0066: Canonicalization Contract, Inbound Representation Control, Stable Representative, Canonical Bytes, and
  Explicit Omission
- ADR-0076: Canonicalization Built-In Law Catalog
- ADR-0046: IDL-First Interface Contract Frontend and 1D Contract Catalog
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0064: Input Contract
- ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary
- ADR-0053: Version Contract
- ADR-0054: Policy Contract
- ADR-0056: Governance Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition

---

# 1. Purpose

This document owns the user-facing Canonicalization authoring API design.

ADR-0066 owns Canonicalization meaning and the authoring semantic boundary. ADR-0076 owns the exact Built-In Law
Authorities and their versioned meanings. This Design chooses how application source names those already-defined
semantics on Kotlin/JVM.

The API is not Contract authority by itself. Changing a public class, object, helper name, package, or other
host-language
shape does not change Canonicalization meaning when the same ADR-defined declaration meaning is resolved.

This document contains user-facing API and CLI authoring workflow design only. Frontend extraction, HIR storage,
Establishment, evaluator generation, classfile handling, cache representation, and runtime canonicalizer implementation
belong to their respective compiler and realization designs.

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

An ordinary user should not need to research external-standard releases or Kontrakt-internal law Versions to write a
Canonicalization declaration. The CLI should generate a complete, explicit law selection at the Canonicalization 1D
declaration site. Once authored, that selection must remain independent of the CLI recommendation that produced it.

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
law only when the Built-In Catalog admits the corresponding exact law and the API projection is approved. In particular,
an unqualified symbol such as `UnicodeNfc` is valid as a final declaration only if its approved public meaning already
identifies one immutable exact law Version. It must not mean whichever Unicode or Kontrakt law Version is currently
recommended. The version-sensitive public spelling remains an open Kotlin API decision in Section 16.

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
For a version-sensitive law, the authored reference must resolve to one immutable exact Authority and Version; the
frontend must not supply a missing version from a project setting, another Policy World, or a current recommendation.

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

Each argument is a direct reference to one exact Kontrakt-provided Built-In Law symbol. Version-sensitive composition
steps must each denote one exact meaning before composition legality is judged. A common recommendation for several
steps does not become a shared semantic default or a hidden composition member.

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
not a runtime configuration object. Each selected coordinate must remain complete in its own declared binding.
Authoring assistance may repeat the same versioned selection across several declarations; it must not introduce
inheritance or fallback between Policy Worlds.

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
missing exact public version selection where the law requires one
unknown or ambiguous public semantic revision
unsupported or ineligible exact law version
source edit conflict after the source has changed
version migration that would redefine an established Contract Version
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
exact Kotlin spelling of a version-sensitive public law reference in type position and as a composition argument
public distinction when one external revision admits more than one immutable exact law meaning
CLI source-edit application format and rollback snapshot encoding
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

---

# 16. Exact Version Selection and CLI Source Generation

## 16.1. Authoring Rule

The ordinary workflow is to generate a Canonicalization 1D declaration through the Kontrakt CLI. The CLI proposes
versions supported and recommended by Kontrakt and writes the resulting exact public law selections into that
declaration.
Users who have no special compatibility requirement may accept those selections without separately managing a version
registry. A user who needs another supported version edits the explicit declaration. The same procedure applies to
every direct law reference and every constituent of an ordered composition.

A law with a permanently fixed public meaning may require no separate external revision at the use site. A law whose
meaning varies with Unicode data, an external specification, a registry snapshot, or another semantic determinant needs
an exact public selector sufficient to recover the intended law. Some externally specified laws can use a permanently
pinned specification/profile as part of their public law identity instead of a repeated revision argument. These are
case-by-case API decisions, not defaults inferred by compilation. A public selector must identify one immutable exact
Built-In Law Authority and Version. If the external standard revision alone does not distinguish two Kontrakt-owned
meanings, the public spelling must distinguish them too. It must never quietly remap an old public selection to a
changed law Version.

Users do not author Kontrakt's internal Law Version, canonical Definition Identity, HID, fingerprint, or Semantic
Material address merely to select a Built-In Law. Those remain internal exact binding and realization information.
This concealment does not excuse an ambiguous public selection: the CLI must generate a complete public reference,
and the compiler must reject one that cannot determine an exact law Version.

## 16.2. Compatibility with the Working Kotlin Surface

Sections 3–7 currently place a direct law selection in a Kotlin parameter type and each composed step in a
`compose(...)` argument. A call such as `UnicodeNfc("17.0")` cannot simply replace a Kotlin type in that surface.
The final version-sensitive spelling must work in both positions while preserving a flat, static declaration.

One possible projection is a distinct public, immutable marker symbol for each approved semantic selection. Another
is a narrowly constrained compile-time source form that carries an exact public revision without introducing executable
user behavior. Neither is approved by this Design yet. The final form must compile as ordinary Kotlin, remain directly
inspectable by the frontend, and not allow a variable, runtime helper, mutable configuration, or ambient provider to
complete the version. For a combined Built-In Law, the CLI selects the exact approved combined law; it does not invent
an unadmitted composite Authority.

The source generated by the CLI must contain the exact public selection directly in the Canonicalization 1D API
declaration. The IDL continues to select that declaration, not a project-wide law set. The IDL cannot silently fill
missing versions for the declaration. A different Policy World may explicitly bind another fully defined declaration;
it does not inherit law selections from a default World.

## 16.3. Recommendation and Source-Edit Boundary

The CLI's recommendation is an authoring suggestion, not a Contract default. It should prefer a verified, supported,
security-eligible law selection compatible with the intended external semantics. Performance evidence may distinguish
viable recommendations, but it cannot justify a different contract meaning without making that choice visible in the
source. The recommended release need not be the newest external release. The relevant support and security evidence
should be inspectable without requiring an ordinary user to review it before accepting a suggestion.

The generator may create complete new source declarations. A separate explicit CLI fix action may offer to complete
version-sensitive selections in already authored declarations. The ordinary check or compile action must neither edit
user source nor resolve an omitted exact selection from the latest recommendation. The user can review the proposed
change and choose a different supported version before applying it.

Source edits must be anchored to the exact declaration and the source revision from which they were derived. The
applicator must refuse a stale or conflicting edit, must not modify unrelated coordinates or Policies, and must
re-analyze the result. A partial write or unsuccessful revalidation must not leave the source in an unreported state.
The exact CLI command spelling and source-transaction mechanism remain implementation Design decisions. IDE support
may consume the same diagnostic and edit proposal later; the CLI is not dependent on an IDE integration.

## 16.4. Binding and Trust

The internal Exact Law Version is resolved under ADR-0053, ADR-0063, ADR-0066, and ADR-0076 after the source
selection is complete. Catalog membership never chooses a current member Version. An unsupported, unknown, ambiguous,
corrupt, or unavailable exact binding fails rather than falling back to another law. Compilation does not reselect a
more recent recommended version. Established Material preserves the selected exact meaning under the existing
Establishment architecture.

Recommendation data, published public-reference mappings, and version-specific semantic data must not be accepted
from unverified or mutable ambient sources. The delivery and integrity checks are implementation/security Design work;
they do not create a new Catalog Authority or substitute an artifact digest for Contract meaning. The law's historical
public reference must remain tied to its original meaning even when the recommendation changes or old compilation
artifacts are no longer available. If that meaning cannot be recovered and checked, authoritative compilation fails.

The project does not introduce a compatibility Profile, project-wide default, Policy-World inheritance, or fallback
version resolution for this workflow. The CLI may use one current recommendation set to author many declarations at
once, but the resulting declarations are individually explicit and independently resolved. Source-bound selection
preserves exact meaning when earlier compilation artifacts are lost, provided the public meaning remains available.
Deliberate destruction of all source continuity and historical trust material still cannot be distinguished from a
genuinely new project without an external trust source, as ADR-0053 already states. A CLI migration snapshot is not
such an external trust anchor.

# 17. Version Migration, Snapshots, and Rollback

Version migration is an explicit source-authoring operation controlled by the user through the CLI. It must not occur
as a side effect of a normal compile, backend optimization, fresh environment, new Kontrakt release, or changed
recommendation. The CLI may prepare an upgrade proposal and show the exact affected Canonicalization 1D declarations,
selected public revisions, and a source diff. Only approved changes are written.

Before applying a migration, the tool should preserve a restorable snapshot of the affected authored source, together
with the proposed exact selections and enough provenance to detect a stale or partial migration. These source-recovery
snapshots are operational migration materials, not substitutes for immutable Contract Version History. The
implementation
may reuse existing project-history and source-control facilities where appropriate; a new semantic Snapshot Authority
is not required. The exact snapshot format, retention, and atomic source-edit mechanism remain open.

A rollback restores a prior source selection and reruns exact resolution and verification. It is not a reset of
ADR-0053 History, a deletion of established law Versions, or permission to rewrite a previously established Contract
Version with changed meaning. If the restored source identifies an existing immutable Contract Version and its meaning
is unchanged, the normal historical re-selection rule applies. Otherwise the change must satisfy the owning Contract's
Version rules before it can be established. If a past selection is no longer legally usable or its exact material is
unavailable, rollback must fail rather than force a substitute or bypass a security restriction.

A migration proposal should distinguish a mere implementation update from a law-meaning update. For the latter, the
impact report should examine changed equivalence classes, representatives, coverage, owned failures, and semantic
determinants. It should identify affected direct bindings, compositions, and explicitly distinct Policy Worlds without
assuming those Worlds share a base contract. The CLI does not guarantee migration of application-owned storage or of
normalization performed outside the Kontrakt pipeline. Any migration effect on material already established under
Kontrakt remains subject to the existing Contract and Versioning obligations.

# 18. Authoring Security and Internal Responsibility

Authoring support must not expand the user's ability to introduce a new Built-In Authority, executable custom
canonicalizer, runtime law registry, or dynamic version selector. Missing exact selections remain compile-time errors.
The CLI's proposal is never a substitute for Catalog qualification, exact semantic closure, security eligibility, or
realization verification. Known-unsafe Versions may remain historically identifiable while a separate legal selection
or security rule prohibits their new use. Such prohibition must not silently change the exact Meaning of an older law.

Within the Kontrakt pipeline, the compiler and supported realization must preserve the selected `E_L`, `C_L`,
coverage, failures, bound semantic material, and ordered-composition legality. Version-specific conformance,
regression, and resource-bound verification remain internal responsibilities. These checks should remain independent
of the user-facing convenience mechanism. Canonicalization performed outside the Kontrakt pipeline is not made
Kontrakt's responsibility by this API.