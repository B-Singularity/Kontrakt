# ADR-0066: Canonicalization Contract, Inbound Representation Control, Stable Representative, Canonical Bytes, and Explicit Omission

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0067: Lowering Contract
- ADR-0065: Admission Contract
- ADR-0064: Input Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0052: Capacity Contract
- ADR-0051: Budget Contract
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary

---

## 1. Context

External material reaches a Contract Machine under an authority that the machine does not own. Input Presentation closes
the form in which that material may first appear, but a legal Input presentation can still contain representation
choices that the Contract author does not want to control later machine meaning.

Those choices are not limited to malicious input. Two outside producers can intend the same meaning while using
different legal spellings or orderings. Unicode normalization, decimal scale, case, line endings, floating
representation, and temporal representation are familiar examples. A hostile producer can deliberately exploit the same
freedom. In either case, the outside representation must not acquire semantic authority merely because it happened to
arrive first.

Canonicalization gives the Contract author an explicit inbound control over that freedom. Through the IDL, the user may
select a closed Canonicalization law that says which distinctions of an already legal Input presentation remain
meaningful and which declared-equivalent distinctions must no longer control later machine judgments. Kontrakt then
establishes the representative required by that law.

The user selects meaning. The user does not supply the executable canonicalizer that becomes authority. V1 exposes a
restricted Kontrakt-owned catalog and inert nominal authoring types whose semantics are already closed, versioned,
bounded, documented, and independently verifiable. The compiler may replace or optimize their realization without
changing the Contract.

Canonicalization therefore belongs to the inbound airlock. It exists because outside presentation is not trusted to
choose the representation that later controls machine meaning. This is an authority-boundary problem, not a cleanup
convenience.

The outward side has a different responsibility. Publication begins with material that already has Core authority, and
Output Presentation gives Publication-authorized material its declared outward shape. The producing machine already
controls that meaning. It does not need a second inbound-style Canonicalization stage merely to mirror the pipeline.

Canonicalization is not cleanup, repair, sanitization, parsing, validation, arbitrary transformation, business
computation, or Lowering. It does not make invalid external material valid. It operates only after Input has established
a legal presentation. It may remove only the representation freedom that the selected law explicitly declares
irrelevant.

Canonicalization does not change the Contract-visible coordinate shape of the selected Input. Shape-changing formation
remains outside this authority.

Canonicalization is optional. Its omission is a semantic choice and must remain distinct from selecting an
exact-preservation law.

---

## 2. Problem

A machine loses control of its boundary when outside representation can silently decide later meaning.

The same declared meaning may arrive in several legal presentations. If those differences flow into equality, lookup,
authorization, identity, caching, ordering, or later judgments without an explicit Contract decision, outside
representation has become an undeclared authority.

The opposite mistake is also dangerous. A machine that normalizes aggressively may erase distinctions the user intended
to preserve. Canonicalization is therefore not a policy of making values "more normalized." It must remove only
distinctions that the selected Contract law declares equivalent.

A boundary judgment can also become unsound when it sees one interpretation and later execution uses another. If
Canonicalization collapses a distinction, Admission and later semantic consumers must not recover the original spelling,
order, scale, case, or other erased distinction and use it as a second determinant. The machine needs one declared
interpretation after the canonical boundary rather than several independent normalizers.

Canonicalization must not repair material that failed Input. Otherwise malformed or illegal outside material could be
changed into a different presentation and admitted under a meaning that Input never established. The canonicalizable
domain begins inside the legal Input presentation domain.

Application-defined canonicalizer callbacks create another problem. Their meaning is no longer closed by the Contract
declaration when execution may consult mutable or ambient state. The same problem appears when a callback inherits
locale, provider versions, collection order, hidden parsing, or object identity from the host environment. The callback
then becomes an opaque semantic authority that the compiler cannot fully verify or replace.

Unbounded Canonicalization is equally unsuitable for an airlock. A law that allows arbitrary recursion, expansion,
comparison, sorting, or user computation can turn one untrusted Input into unbounded work or memory consumption.

Determinism also fails when the host platform is allowed to finish the law. Host equality or hashing may change the
observed classes. Iteration order or comparator behavior may choose another representative. Locale, timezone, Unicode
data, filesystem rules, or object identity may inject ambient meaning that the IDL never declared.

Finally, deterministic encoding must not be confused with semantic Canonicalization. A compiler may need deterministic
bytes for identity, caching, persistence, verification, or artifact production even when no Canonicalization Contract is
selected. Those bytes represent existing meaning. They do not decide which Input distinctions are equivalent.

Canonicalization therefore needs an explicit IDL selection, a closed semantic law, a declared canonicalizable domain, a
stable representative law, bounded production, exact treatment of preserved and collapsed distinctions, versioned
external semantic bases where they matter, and normative conformance material.

---

## 3. Decision Drivers

The Contract author must retain control over which outside representation differences remain meaningful after the Input
boundary.

Equivalent presentation meaning under one selected Canonicalization law must converge to the representative required by
that law. Distinct meaning must not collapse merely because an implementation finds that convenient.

The selected law must be visible in the IDL. Source type shape, host library behavior, annotations, runtime
registration, or downstream implementation must not infer it silently.

Omission must remain explicit. No omitted slot may synthesize `ExactCanonicalization`, a hidden callback, a proxy, an
interceptor, a generated normalization stage, or another replacement authority.

A successful Canonicalization result must become the only canonicalized semantic operand for later judgments that depend
on that boundary meaning. Raw Input may remain as separately owned diagnostic or provenance evidence, but later
Contracts must not regain erased distinctions from that evidence.

Canonicalization must never be a repair path for material that Input did not establish as legal presentation.

Users must not author executable canonicalizer behavior as Contract authority in V1. V1 should let users choose from a
restricted, reviewed, Kontrakt-owned API catalog through nominal types and complete built-in law symbols.

A future custom-law facility must not appear by relaxing V1 type checks. It requires a separate Contract and security
design. Before application code can own such a law, the product surface must explain what it means to define equivalence
and erase distinctions. It must also make aggregate collisions, versioned external semantic data, and resource
amplification visible as part of the responsibility the user is taking on.

A selected coordinate-law declaration must completely cover the selected Input coordinate surface. No undeclared default
may decide what happens to a newly added coordinate.

Canonical output must be deterministic under the law's declared semantic basis. Budget, Capacity, compiler resource
exhaustion, and implementation availability do not choose a different representative.

The realization may use specialized algorithms, quick checks, zero-copy reuse, vectorization, deterministic sorting,
memoization, or other optimizations. These mechanisms remain replaceable when they preserve the declared representative,
result, attribution, observable bounds, and any canonical bytes owned by the selected law.

---

## 4. Decision

### 4.1. Canonicalization Is an Inbound Representation-Control Contract

Canonicalization governs the representation freedom of already legal outside Input before that freedom can influence
later machine meaning.

The user selects the law in the IDL. The selected law states which distinctions remain semantically observable and which
distinctions belong to one declared equivalence class. Kontrakt owns the realization that produces the law's
representative.

A selected Canonicalization law does not grant authority to the host type that names it. The Java or Kotlin symbol is
frontend evidence. After resolution, the Contract meaning must no longer depend on the source class, constructor,
package, reflection shape, runtime object, or executable behavior associated with that source form.

Canonicalization is not a general transformation slot. It cannot derive new business meaning or reinterpret one domain
as another. It cannot repair malformed material, invent defaults, or acquire information through lookup. Projection and
shape-changing formation remain separate responsibilities, so Canonicalization cannot be used as a disguised Lowering
step.

### 4.2. Optional Contract

An Operation may omit Canonicalization.

Omission means:

```text
no Canonicalization Contract
no canonical representative authority
no implicit ExactCanonicalization
no user canonicalizer
no hidden runtime hook
no generated replacement stage
```

When Canonicalization is omitted, the exact presentation established by Input remains the semantic presentation observed
by Admission and by later inbound Contracts that legally consume it.

Omission does not mean that the compiler may skip deterministic encoding required elsewhere. Fixed scalar encodings,
coordinate order, presence markers, framing, schema identity, Version material, or other deterministic compiler
protocols may still be required for identity, ordering, hashing, caching, persistence, Publication, or verification.
Those protocols represent existing material. They do not collapse Input distinctions or produce a Canonicalization
representative.

Selecting `ExactCanonicalization` is different. It is an explicit Canonicalization Contract with its own identity,
conformance obligations, refusal domain, representative law, and any canonical material that the selected law explicitly
owns.

### 4.3. Canonicalization Receives Only Legal Input Presentation

Input remains responsible for deciding whether outside material can become a legal Input Presentation. Canonicalization
cannot reopen material that Input refused, and it cannot transform an illegal carrier into a legal presentation.

Noncanonical does not mean invalid. A legal Input may contain a spelling, ordering, scale, case, normalization form, or
other representation variant that the selected Canonicalization law is specifically intended to collapse. Such material
is ordinary Canonicalization input.

Malformed, structurally illegal, unavailable, or otherwise unestablished Input material is not ordinary Canonicalization
input. A later canonicalizer must not repair it and then claim that the repaired value was what Input established.

This boundary prevents Canonicalization from becoming a hidden parser or sanitizer placed ahead of the declared Input
law.

### 4.4. Declared Equivalence and Stable Representative

Canonicalization removes only declared representation freedom.

For every supported presentation domain, the selected law must make it possible to determine which distinctions are
preserved and which are collapsed. A distinction that has not been explicitly made irrelevant remains meaningful.

A successful application establishes the one representative required by the selected law for the applicable equivalence
class. An implementation is not permitted to choose a different representative because another form is cheaper, more
familiar to a host library, easier to serialize, or more convenient for a backend.

A published V1 law must be stable under repeated application. Applying the same law to a value that already satisfies
its representative law must not move that value again. A hidden second normalization therefore cannot create another
semantic result under the same law.

The law must not collapse two presentations merely because a hash, cache key, byte encoding, comparator, or host
equality implementation collides. Any collapse must follow the declared equivalence itself.

Security suspicion does not create Canonicalization equivalence. Two presentations do not become the same meaning merely
because they are visually confusable, spoofable, unusual, risky, or likely to trigger a security rule. A
security-sensitive distinction may be rejected by Admission, Policy, or another authority that owns that judgment while
Canonicalization continues to preserve the distinction. If a Canonicalization law does collapse that distinction, the
collapse must be part of the law's declared equivalence rather than an implicit security heuristic.

The fact that two inputs produce the same canonical representative establishes only the relation owned by the selected
Canonicalization law. It does not by itself establish the same identity, authorization result, cache authority, Fact
meaning, or any other downstream relation. Another authority may rely on the representative only through its own
declared law. Canonicalization therefore cannot be used as a shortcut that silently transfers one authority's
equivalence into another authority's identity or judgment.

### 4.5. Judgment–Use Coherence

Once Canonicalization establishes a representative, later judgments that consume the canonicalized boundary meaning must
consume that representative relation.

Admission must not judge the representative and then allow Lowering to recover a canonicalized-away distinction from raw
Input. Lowering must not independently normalize the source again under another rule. A downstream library, adapter, or
host operation also cannot silently redefine the Canonicalization law by interpreting the representative through a
different normalization rule.

The selected branch is therefore conceptually:

```text
Established Input Presentation
    -> Canonicalization
    -> stable representative
    -> Admission
    -> Lowering
```

When Canonicalization is omitted, the conceptual branch is:

```text
Established Input Presentation
    -> Admission
    -> Lowering
```

ADR-0048 remains the owner of final whole-airlock composition and slot wiring. This ADR fixes the Canonicalization-side
requirement that the established representative, when selected, is the semantic boundary meaning passed forward. It also
forbids later semantic resurrection of distinctions that the selected law erased.

Raw Input material may be retained under separately owned provenance or diagnostic rules. Retention does not restore
semantic authority to that raw representation.

### 4.6. Same-Shape Representative

Canonicalization does not perform shape-changing Operation input formation.

The selected Input coordinate surface remains the Canonicalization surface.

A selected law may replace a coordinate value with an equivalent representative. It may impose deterministic order where
the law declares source order irrelevant. It may collapse a distinction only when the law explicitly says that the
distinction does not survive Canonicalization.

Canonicalization does not add a Contract-visible coordinate. It does not remove one. It does not rename or retype one.
It does not parse one coordinate into another domain, flatten a product, project a subset, or otherwise remap the Input
surface.

Those are different obligations and remain owned by Lowering or another explicitly declared transformation boundary.

### 4.7. Canonical Bytes

A selected Canonicalization law defines exact canonical bytes only when canonical byte material is part of that law.

Canonical bytes are not an arbitrary serializer result, and deterministic serialization is not automatically semantic
Canonicalization.

When a law owns canonical bytes, its protocol version, schema identity, ordering rules, distinction law, and semantic
bounds are Contract material. A change to those obligations is not hidden as an emitter implementation change.

Generated byte emitters remain implementation. A law that does not declare canonical bytes may still be fully valid
Canonicalization when its representative obligation is complete.

### 4.8. Inbound and Outbound Boundaries Are Not Stage-Symmetric

Canonicalization exists on the inbound side because outside representation reaches the machine before it has Core
authority. The Contract author may need to remove outside representation freedom before later judgments rely on the
material.

The outward path starts from the opposite condition. Publication sees material that already has Core authority, and
Output Presentation gives authorized material its declared outward structure. Output does not need a second
Canonicalization Contract merely to mirror the inbound pipeline.

A protocol-specific deterministic encoding may still be required outside the Core. That requirement belongs to the
authority that owns the outward protocol or its realization. It does not turn Output into another inbound-style
Canonicalization stage.

If the outward result later enters another Contract Machine, it is outside material for that other machine. That machine
applies its own Input and any Canonicalization law selected for its own boundary.

---

## 5. V1 IDL Authoring Boundary

V1 deliberately exposes a narrow Canonicalization authoring surface.

The user declares the Canonicalization choice through the IDL. The IDL selects one exact Canonicalization declaration
for the applicable role. Java or Kotlin nominal types provide restricted frontend evidence for that declaration; they do
not independently acquire the role because they exist in source.

V1 supports two selected source forms.

The first is one complete Kontrakt-owned built-in Canonicalization law symbol.

The second is one inert Java or Kotlin coordinate-law signature declaration. Its parameter names identify the direct
Input coordinates covered by the declaration. Its parameter types select exact Kontrakt-owned nominal Canonicalization
laws from the supported V1 catalog.

The V1 source surface contains no executable canonicalizer. A user does not provide a transformation method, callback,
comparator, equality implementation, encoder object, or runtime descriptor and ask Kontrakt to treat its behavior as the
law. A second canonical output DTO is unnecessary because Canonicalization preserves the Input presentation surface.

The IDL selection supplies the role. Frontend refinement determines whether the selected source evidence denotes one
complete legal Canonicalization Contract. Only the resulting Kontrakt-owned law material may later carry
Canonicalization authority.

The two source forms are:

```text
Direct law selection:
    the IDL selects one complete Kontrakt-provided Canonicalization law

Coordinate-law declaration:
    the IDL selects one inert Java or Kotlin declaration
    whose direct parameter names bind Input coordinates
    and whose parameter types select exact Kontrakt-owned nominal laws
```

A direct law selection may conceptually appear as:

```text
canonicalization  UnicodeNfcCaseFoldCanonicalization
```

A coordinate-law declaration may be selected as:

```text
canonicalization  CustomerCanonicalization
```

When the slot is omitted, the IDL does not select any Canonicalization declaration.

```text
input          CustomerInput
admission      CustomerAdmission
lowering       CustomerLowering
```

Omission says nothing about values that existed before `CustomerInput` was submitted. Kontrakt reasons only from the
Input presentation that its own boundary established.

### 5.1. Built-In Law Catalog

One built-in symbol names one closed, versioned Canonicalization law.

A built-in law does not inherit its meaning from another law. It does not override another law. It does not recursively
compose Contract authority from a hierarchy of child laws. Implementation code may share algorithms behind the boundary,
but that reuse must not appear as Contract inheritance or runtime dispatch.

A built-in name may describe a law whose semantics involve more than one familiar normalization step, but the published
name still denotes one complete law. `UnicodeNfcCaseFoldCanonicalization`, for example, must not mean that the runtime
may compose an NFC helper and a case-fold helper in whichever order is convenient. Normalization operations are not
generally commutative, and an intermediate result can change what a later step observes. Whenever order, intermediate
normalization, or reapplication can change the representative, that sequence is part of the law's declared meaning and
conformance material.

V1 therefore does not expose an arbitrary user-authored pipeline of canonicalization operations. If Kontrakt publishes a
combined law, Kontrakt must have closed the complete combined semantics before the law becomes selectable. Reusing
implementation routines behind that law does not turn those routines into separately selectable Contract stages.

V1 should publish only a restricted catalog whose traps have already been analyzed by Kontrakt. A type enters that
catalog only after its equivalence, representative, legal domain, refusal behavior, version dependencies, aggregate
behavior where applicable, boundedness, and conformance obligations are closed.

Candidate families may include:

```text
ExactCanonicalization

UnicodeNfcCanonicalization
UnicodeNfdCanonicalization
UnicodeNfkcCanonicalization
UnicodeNfkdCanonicalization
AsciiCaseFoldCanonicalization
UnicodeCaseFoldCanonicalization
UnicodeNfcCaseFoldCanonicalization
LineEndingLfCanonicalization

RawBitFloatCanonicalization
RawBitDoubleCanonicalization
CanonicalNaNPreserveSignedZeroCanonicalization
CanonicalNaNCollapseSignedZeroCanonicalization
RejectNaNCanonicalization
RejectNonFiniteCanonicalization
IeeeTotalOrderCanonicalization

DecimalScalePreservingCanonicalization
DecimalNumericValueCanonicalization
DecimalFixedScaleCanonicalization

OrderPreservingSequenceCanonicalization
OrderAgnosticSetCanonicalization
OrderAgnosticBagCanonicalization
CanonicalMapKeyOrderCanonicalization
ExactBinaryCanonicalization

ExactZonedTimeCanonicalization
InstantPreservingZonedTimeCanonicalization
InstantOnlyCanonicalization
FixedPrecisionInstantCanonicalization
```

This list is illustrative. A name in this ADR is not enough to ratify a public API law.

The former illustrative `RecursiveExactCanonicalization` name is not carried forward as a V1 candidate. V1 may support a
closed law over finite structured presentation, but it does not expose recursive Contract composition as a
Canonicalization authoring feature.

A host library operation does not become a Canonicalization law merely because it performs a familiar normalization.
Kontrakt must understand the exact semantic profile rather than delegate authority to the library implementation.

### 5.2. Nominal Canonicalization Types

A public Canonicalization type is a closed authoring name, not a runtime abstraction.

Application code can import and name the type where the Canonicalization authoring surface permits it. The application
cannot instantiate the type, implement it, subclass it, replace it through dependency injection, construct a descriptor
from it, register a new implementation dynamically, or call a method on it to perform canonicalization.

A nominal type may represent a law over a scalar presentation or over another complete closed Input presentation domain
that Kontrakt has explicitly ratified. The public type name does not imply that the law is a JVM scalar operation. What
matters is that the law for that supported presentation domain is complete before the type becomes selectable.

V1 does not let an application construct arbitrary nested Canonicalization trees from generic type expressions. It does
not support path-based overrides into a nested presentation. It does not infer a law from host object structure. If a
closed structured presentation needs Canonicalization, V1 must expose a ratified law that is complete for that supported
presentation domain rather than asking application code to assemble a recursive Contract graph.

The frontend accepts only the exact Kontrakt-owned symbol that the API catalog publishes. A user-defined imitation,
subtype, alias that changes identity, dynamically registered replacement, or type with merely similar spelling does not
acquire the law.

### 5.3. Coordinate-Law Nominal-Type Declaration

A coordinate-law declaration is not a canonicalizer implementation. It is an inert frontend signature selected through
the IDL.

The binding form is:

```text
selected direct Input coordinate
+ exact supported presentation domain for that coordinate
+ one exact Kontrakt-owned nominal Canonicalization law
= one law assignment inside one flat Canonicalization declaration
```

The declaration remains one Canonicalization Contract. Its parameters do not become independent child Contracts merely
because several nominal laws appear in the source signature.

Resolution uses exact source symbols and exact direct coordinate names. It also verifies that the selected nominal law
supports the presentation domain of the coordinate to which it is assigned.

Resolution is not runtime string lookup. It does not use reflection, annotation scanning, assignability, package
convention, source proximity, or type-wide inference to discover Canonicalization authority.

The declaration contains no executable law value. It contains no enum constant or singleton that the runtime switches
over as authority. It contains no factory call, method body, callback, property initializer, mutable descriptor, custom
comparator, user-defined equality, or other behavior that could become a hidden canonicalizer.

The source form is conceptually equivalent to the following Kotlin declaration:

```kotlin
// Kontrakt-provided authoring API. Application code may name these types,
// but cannot instantiate, implement, extend, or execute them.
class ExactText private constructor()
class UnicodeNfcCaseFold private constructor()
class AsciiUppercase private constructor()

data class CustomerInput(
    val customerId: String,
    val name: String,
    val regionCode: String,
)

// User-authored source evidence. The IDL selects this declaration.
// Kontrakt never instantiates it.
class CustomerCanonicalization private constructor(
    customerId: ExactText,
    name: UnicodeNfcCaseFold,
    regionCode: AsciiUppercase,
)
```

The Kontrakt-provided nominal types belong to the real authoring API. The application writes its Input declaration and,
when it needs per-coordinate selection, the inert Canonicalization declaration that the IDL names.

Constructor parameters are declaration evidence rather than runtime properties. The declaration does not create values,
runtime descriptors, nested Canonicalization Contracts, parent-child authority, inheritance, recursive Contract
composition, or a second user-visible presentation shape.

V1 rejects any source form that turns the declaration into executable or replaceable behavior. Public constructors,
factories, methods, callbacks, lambdas, executable initializers, mutable or delegated state, and captured runtime
dependencies are therefore outside the authoring surface. The same restriction excludes custom comparators, user-defined
equality or ordering, inheritance-based role acquisition, arbitrary generic law construction, enum or singleton law
values, and annotations on the Input carrier that attempt to acquire Canonicalization authority indirectly.

### 5.4. Complete Coordinate Coverage

A coordinate-law declaration must cover every direct Contract-visible Input coordinate exactly once.

A missing coordinate is invalid. A duplicate or unknown coordinate is invalid. A renamed coordinate that no longer
resolves to the selected Input surface is invalid. A nominal law that does not support the coordinate's presentation
domain is invalid.

Adding, removing, renaming, or retyping an Input coordinate invalidates the old coordinate-law binding and requires the
declaration to be ratified again. Source declaration order does not silently provide semantic fallback for a changed
coordinate set.

No new coordinate receives an undeclared `Exact` default.

A whole-presentation built-in law has the same completeness obligation, but the completeness belongs to that law rather
than to a user-written parameter list.

### 5.5. No Application-Defined Canonicalization Law in V1

V1 exposes no application-defined Canonicalization law extension point.

This restriction is deliberate. A custom canonicalizer can erase a distinction that later authorization expected to
preserve, or it can repair material that should never have entered the stage. It can collapse aggregate keys, depend on
an unstable semantic table, or make resource cost attacker-controlled. It can also disagree with Admission or Lowering
about what the value means. Allowing a `T -> T` callback would conceal those decisions behind executable behavior.

A future version may open custom Canonicalization only through a separate design and security review. The extension must
make the semantic burden explicit rather than simply accepting executable code. At minimum, a custom law would need a
closed presentation domain and an explicit account of which distinctions survive. Its equivalence and representative
behavior would have to be inspectable rather than inferred from code. Versioned semantic bases, refusal conditions,
aggregate collision behavior, boundedness, determinism, and conformance would also need contract-owned definitions.

The product must also explain the security consequences to users before application-defined authority is exposed. A
custom law can change authorization keys, cache keys, equality classes, lookup behavior, deduplication, or downstream
interpretations even when the code appears to be a harmless normalizer.

Future custom support must not weaken the V1 rule by treating an application callback as a nominal type implementation.
It requires an explicit Contract surface of its own.

### 5.6. Public Law Specification

Every public Canonicalization type must be backed by a complete Kontrakt-owned specification.

The specification must identify the Input presentation domain for which the law is legal. It must explain which
distinctions survive and which are collapsed, then describe the stable representative and the cases that fall outside
the canonicalizable domain. Null-like values, explicit absence, and finite alternatives require their own declared
behavior whenever the supported presentation can observe those distinctions. If the law depends on a versioned semantic
table or profile, that dependency must be explicit rather than inherited from the running platform.

Aggregate laws must define what happens when Canonicalization causes two previously distinct elements or keys to
collide. The implementation must not choose a first-wins, last-wins, iteration-order, or hash-table behavior unless the
Contract law explicitly owns that result.

The specification must state the law's meaningful bounds and refusal behavior, including the attribution of stops that
belong to another Contract rather than Canonicalization. It must also state whether exact canonical bytes belong to the
law and, if so, which byte protocol version applies.

Normative examples and conformance vectors must be versioned with the law. User documentation explains the same law in
usable terms, but documentation is not an alternate source of authority.

V1 does not require or permit a second user-authored canonical output presentation. Representative values may change
under the selected equivalence law while the Contract-visible Input surface remains the same. A different user-visible
target shape belongs to Lowering.

---

## 6. Canonicalization Law Eligibility

A public nominal Canonicalization type is selectable only after Kontrakt can determine its complete meaning without
executing application behavior. Publication into the V1 catalog also requires the security review appropriate to an
inbound law; the detailed threat model belongs to the separate Canonicalization Security ADR rather than to this
document.

The law must identify the supported Input presentation domain and the distinctions it preserves. It must also identify
the distinctions it collapses and the representative required for each successful class. Numeric, textual, temporal,
binary, finite-choice, ordering, duplicate, and aggregate semantics must be closed wherever the law observes them.

A law that depends on external semantic data must pin the material that can change its result. Unicode or temporal data
are obvious examples, but the same rule applies to collation, numeric, URI, or other semantic profiles. Whatever
provider happens to be installed on the host cannot silently complete Contract meaning.

Unknown, application-defined, imitated, dynamically registered, or incompletely specified canonical type symbols are
rejected in V1.

Executable law values are not Canonicalization authority. V1 does not complete a law by invoking application callbacks
or by following virtual dispatch, inheritance, runtime subtype discovery, reflection, or object identity. Nor may the
law consult mutable state or acquire missing meaning through dependency injection, repositories, the environment, time,
randomness, synchronization, or lazy runtime traversal.

Host defaults cannot finish a Canonicalization law. Locale, timezone, charset, collation, or normalization data must be
explicit when they affect meaning. Filesystem rules, hash-table order, acquisition order, and unstable sorting
tie-breaks likewise cannot choose a representative merely because the implementation exposes them.

A law may reject a legal Input when that Input lies outside its declared canonicalizable domain. It may not use
rejection as a substitute for Input legality, and it may not repair illegal Input into the domain.

Shape-changing output, business derivation, arbitrary parsing, projection, flattening, capability acquisition,
external-resource resolution, or general repair remains outside Canonicalization.

---

## 7. Finite-Work Law

Canonicalization is a producer Contract on an untrusted inbound path. It cannot be permission for unlimited work.

Every V1 law must have a finite, analyzable work shape for the presentation domains that the public API allows. The
review must cover the size and depth the law can consume, the aggregate cardinality it can traverse, and the maximum
expansion of the representative. Temporary storage and output growth also need known bounds whenever they can become
material to safe realization.

The Contract must distinguish a semantic bound from a compiler resource limit. A semantic bound is part of the law when
crossing it changes whether an Input belongs to the law's legal domain or changes the required representative. Compiler
CPU limits, temporary allocation ceilings, cancellation, worker limits, or implementation-specific scratch space do not
become Canonicalization meaning merely because the implementation must remain safe.

General application-defined imperative iteration and recursive object-graph traversal are not available as
Canonicalization behavior in V1. A closed Kontrakt-owned law may operate over finite structured presentation when its
traversal, ordering, duplicate behavior, collision behavior, and completion rules are already part of the ratified law.

A valid realization may avoid work when the Input already satisfies the representative law. It may reuse immutable
backing, use a quick check, perform zero-copy projection, specialize scalar operations, vectorize, sort with a
law-preserving algorithm, or memoize reusable computation. The Contract requires the same semantic result, not the same
amount or shape of physical work.

A realization may use canonical child material, domain-separated hashes, exact comparison, or another deterministic
implementation technique when that technique preserves the selected law. Hash equality alone never becomes permission to
collapse distinctions.

Budget and Capacity retain their own declared authorities. Crossing one of their walls does not create a different
Canonicalization representative or redefine equivalence.

---

## 8. Canonicalizable-Domain Condition

Canonicalization starts inside the legal Input Presentation domain.

A source is not refused merely because it is not already canonical. A noncanonical spelling, order, scale, case,
normalization form, or other variation that the selected law explicitly accepts is ordinary Canonicalization input.

A Canonicalization refusal occurs only after the law has a legal Input presentation to judge. Refusal may occur when the
presentation lies outside the selected canonicalizable domain, when the law cannot establish the required representative
for that presentation, or when another Canonicalization-owned condition makes the application invalid under the declared
law.

Input illegality is not Canonicalization refusal. Compiler inability is not Canonicalization refusal. Budget or Capacity
stops retain their own result ownership.

```text
legal but noncanonical presentation:
    establish the selected representative

legal presentation outside the selected canonicalizable domain:
    Canonicalization refusal

Input material that never became a legal presentation:
    Canonicalization does not enter

successful result that violates the selected representative law:
    Kontrakt defect
```

There is no user-authored `isCanonical`, `validateCanonical`, or post-production verifier that can accept a different
result after Kontrakt has completed the selected law.

---

## 9. Definition-Time Processing

This revision does not yet re-open the HIR or Establishment model for Canonicalization. The existing definition-time
frontend obligations remain, but their implementation must preserve the IDL-first and restricted V1 authoring boundary
defined above.

The Canonicalization definition path is:

```text
resolve the exact IDL-selected built-in symbol or coordinate-law declaration
-> acquire the selected source through the existing frozen frontend machinery
-> determine direct-law or coordinate-law nominal-type source form
-> verify one flat selectable Contract and an inert signature declaration when applicable
-> bind every declared parameter name to exactly one legal direct Input coordinate
-> verify complete coordinate coverage and presentation-domain compatibility
-> resolve every exact nominal canonical type to pinned semantic material
-> reject law values, constructors, factories, execution, environment, dispatch, State, effect, imitation, and dynamic-registration paths
-> ratify preserved and collapsed distinctions for every selected law and the complete presentation
-> verify the declared semantic bounds and the implementation safety obligations required before publication
-> derive canonical byte material only when the selected law owns canonical bytes
-> derive stable Contract identity and the existing Canonicalization definition material
-> generate or select the deterministic realization
```

Runtime performs no declaration lookup, reflection, member discovery, method dispatch, operator resolution, locale or
Unicode-table selection, comparator acquisition, hash-policy selection, or byte-schema construction. It executes only
Kontrakt-owned realization over already resolved law material.

The exact HIR Definition Candidate, Binding Candidate, Establishment handoff, Required Basis, Applicability, Occurrence,
Established Material, and Established Semantic Protocol are deliberately not defined by this revision. They remain the
next reclosure step under ADR-0071, ADR-0063, and the 1D master checklist.

## 10. Identity and Conformance

Canonicalization identity is derived from ratified meaning.

It is not derived from the selected source class name alone.

Identity material includes the law kind, semantic profile version, source presentation identity, preserved and collapsed
distinctions, coordinate-law bindings, representative-law versions, applicable scalar-law meaning, canonicalizable
domain, refusal law, work and output bound law, canonical byte protocol version and schema identity where canonical
bytes belong to the law.

A frontend change that changes accepted meaning or generated canonical material changes Contract identity.

Every built-in law must have a versioned normative conformance-vector set and a separate implementation-verification
suite.

Changing semantic meaning or a normative expected result changes the law identity.

Changing the verification suite alone does not change Contract meaning. Attack cases, property tests, differential
checks, performance tests, and additional implementation coverage may grow while the normative law stays the same.

The exact Definition identity law will be re-audited when Canonicalization HIR and Establishment are reopened. This
section preserves the existing rule that source symbols and implementation artifacts do not own semantic identity.

---

## 11. Determinism Law

Canonicalization result meaning depends only on semantic material that the selected law declares relevant.

The law is conceptually:

```text
same Canonicalization law meaning
+ same legal Established Input presentation meaning
+ same meaning-determining versioned semantic bases, where applicable
= same canonical representative
+ same Canonicalization outcome
+ same Contract-owned attribution
+ same canonical bytes, only when the law owns canonical bytes
```

Budget availability, Capacity availability, cache state, worker count, traversal schedule, allocation order, host
locale, current provider state, and other implementation conditions do not choose a different representative.

Generated canonicalizers and byte emitters remain implementation-axis machinery. Kontrakt may specialize or fuse them,
eliminate allocation, add a quick-check path, reuse validated work, or replace one algorithm with another. Those choices
are legal only while the Contract-visible result remains identical under the selected law. Canonicalization itself owns
no mutable State, so an optimization must not introduce state-visible behavior as a hidden side effect of realization.

Physical reordering across the Canonicalization boundary requires a stronger proof than ordinary local equivalence. A
later predicate, guard, lookup, or other judgment may be evaluated before Canonicalization only when the compiler can
prove that its result is invariant over every Canonicalization equivalence class that the movement crosses. If two
presentations are equivalent under the selected Canonicalization law but the moved computation can distinguish them,
moving that computation before Canonicalization changes the declared machine and is illegal.

```text
x equivalent-to y under the selected Canonicalization law
    ->
any computation moved across that boundary
    must observe the same result for x and y
```

This proof obligation constrains optimization rather than adding a new Canonicalization judgment. The logical Contract
order remains authoritative even when execution is fused or scheduled differently.

Semantic determinism does not imply constant-time execution, identical allocation behavior, or one mandatory physical
algorithm. Those are separate security or realization questions.

---

## 12. Refusal Boundary

Canonicalization receives only material that Input has already established as a legal presentation and only when the
selected Canonicalization branch is applicable.

It refuses only under a Canonicalization-owned negative condition after legal entry. A presentation may be noncanonical
and still succeed. A presentation may be legal Input yet lie outside the selected Canonicalization law's domain and
therefore be refused by Canonicalization.

Canonicalization must not convert Input refusal into Canonicalization refusal by repairing the original material. It
also must not hide Admission rejection, Lowering refusal, Failure, or cross-cutting resource results under a generic
canonicalization error.

A Budget or Capacity stop observed while realizing Canonicalization remains owned by Budget or Capacity. Compiler
resource exhaustion or implementation unavailability is not a new Canonicalization semantic result.

When Canonicalization is omitted, there is no Canonicalization refusal surface.

---

## 13. Relationship to Admission and Lowering

A selected Canonicalization law preserves the Input coordinate surface and establishes the representative that later
inbound judgments must use for the distinctions governed by that law.

Admission therefore judges the canonical representative when Canonicalization is selected. When Canonicalization is
omitted, Admission judges the Input-established presentation directly.

Lowering receives the same semantic boundary meaning that Admission judged. It must not return to an earlier raw
presentation and recover distinctions that Canonicalization already erased. It must not apply another hidden
normalization and treat the result as the same Canonicalization Contract.

Canonicalization does not define Operation parameter targets and does not create Fact meaning. Lowering continues to own
the explicit source-to-target relation and the shape-changing representation-formation boundary.

The final shared inbound branch, slot singularity, and whole-airlock composition remain owned by ADR-0048. This ADR
fixes only the Canonicalization-side semantic requirement that selected Canonicalization occurs before later judgments
that are meant to rely on its representative.

---

## 14. Open in This Section

The final IDL token spelling may change.

The exact public nominal Canonicalization type names may change.

The exact Java or Kotlin carrier syntax for an inert coordinate-law declaration may change.

The exact set of V1 built-in laws remains API and platform-support work. A candidate name in this ADR is not a ratified
promise until its semantic and security review is complete.

A future application-defined Canonicalization facility remains deferred. It requires a separate Contract design and a
Security ADR rather than an extension point hidden inside the V1 nominal type API.

HIR Definition Candidate, Binding Candidate, Establishment, Required Basis, Applicability, Occurrence, Established
Material, Established Semantic Protocol, current-validity, and V2 incremental details remain intentionally outside this
revision pass.

Any frontend change must preserve the fixed V1 authoring boundary: explicit IDL selection, explicit omission, closed
Kontrakt-owned law material, exact nominal type resolution, complete coordinate coverage where coordinate-law authoring
is used, no implicit `ExactCanonicalization`, no executable user canonicalizer, and no arbitrary shape-directed or
nested user law inference.

---

## 15. Consequences

Canonicalization becomes an explicit inbound representation-control Contract rather than cleanup code.

The outside world may still choose how to express a legal Input within the presentation space that Input permits. It
does not automatically get to decide which of those differences remain meaningful after the Canonicalization boundary.

The user retains that semantic choice through the IDL. V1 gives the user control by exposing reviewed law selections,
not by handing over executable authority to arbitrary callbacks.

Kontrakt can therefore verify and optimize the realization against closed law material. Already-canonical input may use
a fast path or reuse immutable backing without changing the semantic application. More expensive structured laws may use
specialized implementation techniques while remaining subject to the same representative and boundedness obligations.

Omission remains explicit and cheap. It preserves the Input presentation distinctions rather than pretending that an
invisible identity canonicalizer ran.

The selected branch obtains one canonical interpretation for later inbound judgments. Admission and Lowering cannot
silently disagree by re-reading different normalized forms of the same outside material.

The stricter V1 authoring boundary reduces flexibility. That restriction is deliberate. Custom Canonicalization can
change security-sensitive equality classes, cache behavior, lookup keys, authorization behavior, duplicate handling, and
resource cost. Kontrakt should not expose that authority until its Contract surface and security model are explicit
enough for users to understand what they are declaring.

Budget and Capacity remain separate authorities instead of being folded into generic Canonicalization failure. Compiler
resource mechanisms remain implementation unless another Contract explicitly owns them.

Canonical bytes remain separate from generic deterministic compiler encoding. A selected law owns exact bytes only when
it explicitly declares that obligation.

Output remains asymmetric with Input. Publication and Output Presentation control authoritative material leaving the
machine; they do not acquire another Canonicalization stage solely for symmetry.

---

## 16. Migration History

This ADR was extracted mechanically from the Canonicalization-owned material of ADR-0048.

The extraction itself did not change the accepted Canonicalization Contract semantics.

A later rework reframed Canonicalization around the inbound authority boundary already implied by `What Contract Is`.
Canonicalization now explicitly controls which representation freedoms of an established outside Input may continue to
influence later machine meaning. The review also moved the selected conceptual order to Input Presentation ->
Canonicalization -> Admission -> Lowering so that judgment and later use share one declared interpretation.

The same review tightened the V1 authoring surface around IDL selection and a restricted Kontrakt-owned nominal law
catalog. Application-defined executable canonicalizers remain unavailable in V1. Future custom-law support requires a
separate Contract and security design rather than a callback extension point.

The review also made explicit that Canonicalization cannot repair illegal Input, that erased distinctions cannot be
resurrected by later semantic consumers, that canonical bytes are law-specific rather than an implicit property of every
deterministic encoding, and that semantic bounds remain distinct from Budget, Capacity, and compiler resource
mechanisms.

A final pass clarified four boundaries that follow from the same model. Security suspicion does not create equivalence.
A combined built-in law owns the exact ordering of its semantic steps rather than exposing an arbitrary normalization
pipeline. Equal canonical representatives do not silently merge identity, authorization, cache authority, or another
downstream judgment. Compiler reordering across Canonicalization is legal only when the moved computation is invariant
over the Canonicalization equivalence classes it crosses.

ADR-0048 remains the owner of the shared optional-Canonicalization branch and final inbound composition.
Canonicalization-specific HIR and Establishment reclosure is intentionally deferred to the next ADR-0066 revision pass.