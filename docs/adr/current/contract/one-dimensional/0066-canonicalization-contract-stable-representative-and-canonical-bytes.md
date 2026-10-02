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

Those choices are not limited to malicious input. Two outside producers can intend the same meaning while presenting it
differently. Text can differ in normalization, case, or line ending convention. Numeric values can carry different
scales or floating representations. Time can also be represented in several legal forms. A hostile producer can
deliberately exploit the same freedom. In either case, the outside representation must not acquire semantic authority
merely because it happened to arrive first.

Canonicalization gives the Contract author an explicit inbound control over that freedom. Through the IDL, the user may
select a closed Canonicalization law that says which distinctions of an already legal Input presentation remain
meaningful and which declared-equivalent distinctions must no longer control later machine judgments. Kontrakt then
establishes the representative required by that law.

The user selects meaning. The user does not supply the executable canonicalizer that becomes authority. V1 exposes only
Kontrakt-owned laws whose meaning has already been closed before application code selects them. Their public authoring
forms are inert evidence. Their semantics must also be bounded and independently verifiable so that the compiler can
replace the realization without changing the Contract.

Canonicalization therefore belongs to the inbound airlock. It exists because outside presentation is not trusted to
choose the representation that later controls machine meaning. This is an authority-boundary problem, not a cleanup
convenience.

The outward side has a different responsibility. Publication begins with material that already has Core authority, and
Output Presentation gives Publication-authorized material its declared outward shape. The producing machine already
controls that meaning. It does not need a second inbound-style Canonicalization stage merely to mirror the pipeline.

Canonicalization does not exist to make an inconvenient Input easier for implementation code to handle. It cannot repair
an illegal presentation, reinterpret another domain, or perform the transformation owned by Lowering. It operates only
after Input has established a legal presentation and may remove only the representation freedom that the selected law
explicitly declares irrelevant.

Canonicalization does not change the Contract-visible coordinate shape of the selected Input. Shape-changing formation
remains outside this authority.

Canonicalization is optional. Its omission is a semantic choice and must remain distinct from selecting an
exact-preservation law.

---

## 2. Problem

A machine loses control of its boundary when outside representation can silently decide later meaning.

The same declared meaning may arrive in several legal presentations. If later judgments can observe those differences
without an explicit Contract decision, the external producer has gained authority over decisions that belong to the
machine. The first effect may appear in comparison or lookup. Once that representation difference survives, it can also
change later authorization or identity judgments and make reusable compiler work depend on an undeclared distinction.

The opposite mistake is also dangerous. A machine that normalizes aggressively may erase distinctions the user intended
to preserve. Canonicalization is therefore not a policy of making values "more normalized." It must remove only
distinctions that the selected Contract law declares equivalent.

A boundary judgment can also become unsound when it sees one interpretation and later execution uses another. Once
Canonicalization has erased a distinction, Admission and later semantic consumers must operate on the representative
established by that law. They cannot return to the earlier presentation and use an erased difference as a second
determinant. The machine needs one declared interpretation after the canonical boundary rather than several independent
normalizers.

Canonicalization must not repair material that failed Input. Otherwise malformed or illegal outside material could be
changed into a different presentation and admitted under a meaning that Input never established. The canonicalizable
domain begins inside the legal Input presentation domain.

Application-defined canonicalizer callbacks create another problem. Their meaning is no longer closed by the Contract
declaration when execution can consult state that the declaration does not name. Host behavior can also leak into the
result through environmental defaults or runtime object behavior. Once that happens, the callback becomes an opaque
semantic authority that the compiler cannot fully verify or replace.

Unbounded Canonicalization is equally unsuitable for an airlock. The law must constrain the work needed to establish its
representative. A declaration that permits arbitrary traversal or expansion can otherwise turn one untrusted Input into
work or memory consumption with no Contract-visible limit.

Determinism also fails when the host platform is allowed to finish the law. A host equality rule can change which values
appear equivalent, while iteration or comparison behavior can change which representative is selected. Environmental
defaults can have the same effect. If those conditions are not part of the declared law, they must not determine
Canonicalization meaning.

Finally, deterministic encoding must not be confused with semantic Canonicalization. A compiler may need stable bytes
even when no Canonicalization Contract is selected. Those bytes can make compiler material reproducible and safe to
persist or verify. They still do not decide that two Input presentations are semantically equivalent.

Canonicalization therefore needs a closed law selected explicitly through the IDL. That law must define the domain in
which it applies, the equivalence it recognizes, and the representative it establishes. It must also make its semantic
bounds and any external semantic basis explicit enough for conformance to be verified.

---

## 3. Decision Drivers

The Contract author must retain control over which outside representation differences remain meaningful after the Input
boundary.

Equivalent presentation meaning under one selected Canonicalization law must converge to the representative required by
that law. Distinct meaning must not collapse merely because an implementation finds that convenient.

The selected law must be visible in the IDL. Neither source structure nor host behavior may infer a missing
Canonicalization choice. Later implementation machinery also cannot create that authority on its own.

Omission must remain explicit. Leaving the slot empty means that no Canonicalization authority exists. The compiler must
not replace that omission with an implicit exact law or with generated behavior that acts as one.

A successful Canonicalization result must become the only canonicalized semantic operand for later judgments that depend
on that boundary meaning. Raw Input may remain as separately owned diagnostic or provenance evidence, but later
Contracts must not regain erased distinctions from that evidence.

Canonicalization must never be a repair path for material that Input did not establish as legal presentation.

Users must not author executable canonicalizer behavior as Contract authority in V1. V1 instead exposes a restricted
Kontrakt-owned law catalog. Application code can name those laws through inert authoring forms, but it does not supply
their executable meaning.

A future custom-law facility must not appear by relaxing V1 type checks. It requires a separate Contract and security
design because defining a new equivalence law is itself a significant authority. The future surface must therefore make
the consequences of erasing distinctions explicit, including the effects a custom law can have on aggregate meaning and
resource use.

A selected coordinate-law declaration must completely cover the selected Input coordinate surface. No undeclared default
may decide what happens to a newly added coordinate.

Canonical output must be deterministic under the law's declared semantic basis. A resource stop or missing
implementation capability cannot silently select a different representative. Those conditions retain the result
ownership already assigned to them.

The realization is free to change how it computes the representative. It may avoid unnecessary work or use a more
specialized implementation when the selected law permits it. That freedom ends at the Contract boundary: every
replacement must preserve the representative and the Canonicalization result. It must also preserve the attribution and
bounds that the law exposes, together with exact canonical bytes when the law owns them.

---

## 4. Decision

### 4.1. Canonicalization Is an Inbound Representation-Control Contract

Canonicalization governs the representation freedom of already legal outside Input before that freedom can influence
later machine meaning.

The user selects the law in the IDL. The selected law states which distinctions remain semantically observable and which
distinctions belong to one declared equivalence class. Kontrakt owns the realization that produces the law's
representative.

A selected Canonicalization law does not grant authority to the host type that names it. The Java or Kotlin symbol is
frontend evidence. After resolution, the source artifact no longer defines Contract meaning. The resolved law must stand
independently of how that symbol happened to be represented or loaded by the host language.

Canonicalization is not a general transformation slot. It cannot derive new business meaning or reinterpret one domain
as another. It also cannot repair malformed material or obtain missing information from somewhere else. Projection and
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

Omission does not remove deterministic encoding that another compiler responsibility legitimately needs. Such encoding
may give existing material a stable physical representation for compiler work. It does not collapse Input distinctions
and therefore does not create a Canonicalization representative.

Selecting `ExactCanonicalization` is different. It is an explicit Canonicalization Contract. The law has its own
semantic identity and representative obligation even though the representative preserves the Input distinctions. Its
refusal and conformance rules therefore belong to the selected law rather than to omission.

### 4.3. Canonicalization Receives Only Legal Input Presentation

Input remains responsible for deciding whether outside material can become a legal Input Presentation. Canonicalization
cannot reopen material that Input refused, and it cannot transform an illegal carrier into a legal presentation.

Noncanonical does not mean invalid. A legal Input may use a representation variant that the selected law was
specifically declared to collapse. The exact kind of variation depends on the selected domain. Such material is ordinary
Canonicalization input.

Material that never became an established Input presentation is not ordinary Canonicalization input. A later
canonicalizer must not repair it and then claim that the repaired value was what Input established.

This boundary prevents Canonicalization from becoming a hidden parser or sanitizer placed ahead of the declared Input
law.

### 4.4. Declared Equivalence and Stable Representative

Canonicalization removes only declared representation freedom.

For every supported presentation domain, the selected law must make it possible to determine which distinctions are
preserved and which are collapsed. A distinction that has not been explicitly made irrelevant remains meaningful.

A successful application establishes the one representative required by the selected law for the applicable equivalence
class. Implementation convenience cannot select another representative. The backend must therefore adapt to the declared
law rather than substituting a form that is cheaper or easier for the host platform.

A published V1 law must be stable under repeated application. Applying the same law to a value that already satisfies
its representative law must not move that value again. A hidden second normalization therefore cannot create another
semantic result under the same law.

Implementation collisions do not define equivalence. A hash or encoded form may be useful for locating candidate
material, but exact law-defined equivalence must still justify the collapse. The same rule applies when an
implementation comparator or host equality operation happens to report equality.

Security suspicion does not create Canonicalization equivalence. A suspicious presentation can remain semantically
distinct even when another Contract later rejects it for security reasons. If Canonicalization itself erases that
distinction, the equivalence must be part of the selected law rather than an implicit security heuristic.

The fact that two inputs produce the same canonical representative establishes only the relation owned by the selected
Canonicalization law. It does not automatically give another authority the right to treat the inputs as identical for
its own purpose. Identity remains owned by the law that defines identity, and an authorization judgment remains owned by
the authority that declares it. Reuse and Fact meaning follow the same rule. A later authority may rely on the
representative, but it must do so through its own declared relation.

### 4.5. Judgment–Use Coherence

Once Canonicalization establishes a representative, later judgments that consume the canonicalized boundary meaning must
consume that representative relation.

Admission must not judge the representative and then allow Lowering to recover a canonicalized-away distinction from raw
Input. Lowering must not independently normalize the source again under another rule. The same restriction applies to
downstream host code: a library or adapter cannot silently reinterpret the representative through another normalization
rule and still claim to be using the selected Canonicalization law.

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

A selected law may replace a coordinate value with an equivalent representative. It may also impose a deterministic
order when the law explicitly says that source order carries no meaning. Any other collapsed distinction requires the
same explicit declaration.

Canonicalization cannot change the declared coordinate structure. Turning one coordinate into another domain or
rearranging the Input surface is a different obligation. Lowering, or another explicitly declared transformation
boundary, remains responsible for such formation.

### 4.7. Canonical Bytes

A selected Canonicalization law defines exact canonical bytes only when canonical byte material is part of that law.

Canonical bytes are not an arbitrary serializer result, and deterministic serialization is not automatically semantic
Canonicalization.

When canonical bytes belong to the law, the byte protocol is Contract material rather than an emitter convention. The
law must therefore close the schema and ordering needed to interpret those bytes, together with any distinction or bound
that changes the normative output. Changing one of those obligations changes the law rather than merely changing the
emitter.

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

The V1 source surface contains no executable canonicalizer. Application code does not hand Kontrakt an object whose
behavior is then treated as the law. Comparison, encoding, and transformation behavior remain owned by the Kontrakt law
selected through the declaration. A second canonical output DTO is unnecessary because Canonicalization preserves the
Input presentation surface.

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

A built-in name may describe a law whose semantics require several normalization operations, but the published name
still denotes one complete law. For example, `UnicodeNfcCaseFoldCanonicalization` cannot leave the order of
normalization and case folding to runtime convenience. An intermediate form can change what the next operation observes.
Whenever the sequence changes the representative, that sequence belongs to the declared law and its conformance
material.

V1 therefore does not expose an arbitrary user-authored pipeline of canonicalization operations. If Kontrakt publishes a
combined law, Kontrakt must have closed the complete combined semantics before the law becomes selectable. Reusing
implementation routines behind that law does not turn those routines into separately selectable Contract stages.

V1 should publish only a restricted catalog whose semantic traps have already been analyzed by Kontrakt. Before a type
enters that catalog, Kontrakt must know what it treats as equivalent and which representative it establishes. The
supported domain and refusal behavior must also be closed. If external semantic data or aggregate behavior can change
the result, those obligations must be explicit. The law must finally be bounded and accompanied by enough conformance
material to verify the implementation.

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

Application code can import and name the type where the Canonicalization authoring surface permits it. Naming the type
does not give application code a way to realize the law. The type has no application-owned construction or substitution
path, and it exposes no operation that performs canonicalization. Application wiring therefore cannot replace the
Kontrakt-owned meaning behind the selected name.

A nominal type may represent a law over a scalar presentation or over another complete closed Input presentation domain
that Kontrakt has explicitly ratified. The public type name does not imply that the law is a JVM scalar operation. What
matters is that the law for that supported presentation domain is complete before the type becomes selectable.

V1 does not let an application construct arbitrary nested Canonicalization trees from generic type expressions. It does
not support path-based overrides into a nested presentation. It does not infer a law from host object structure. If a
closed structured presentation needs Canonicalization, V1 must expose a ratified law that is complete for that supported
presentation domain rather than asking application code to assemble a recursive Contract graph.

The frontend accepts only the exact Kontrakt-owned symbol that the API catalog publishes. A look-alike application type
does not acquire the law merely because it has a related name or type relation. Dynamic replacement is equally incapable
of changing which law the IDL selected.

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

Resolution is a compile-time relation, not runtime discovery. The compiler resolves the exact selected declaration and
its coordinate bindings rather than searching host metadata or inferring authority from surrounding types.

The declaration contains no executable law value. Its source form can describe which closed law is assigned to each
coordinate, but it cannot carry behavior that later becomes the canonicalizer. Any source construct that would make the
declaration executable or replaceable falls outside this authoring role.

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

Constructor parameters are declaration evidence rather than runtime properties. They bind coordinates to closed laws
without creating runtime values or a hierarchy of nested Contract authority. The declaration also does not introduce
another user-visible presentation shape.

V1 rejects any source form that makes the declaration executable or replaceable. It cannot expose executable members or
capture runtime dependencies. The same principle rules out application-defined comparison or ordering behavior and any
type mechanism that could acquire the Canonicalization role indirectly. The selected declaration must remain inert
evidence for one flat Contract.

### 5.4. Complete Coordinate Coverage

A coordinate-law declaration must cover every direct Contract-visible Input coordinate exactly once.

A missing coordinate is invalid. So is a coordinate that does not belong to the selected Input surface. Duplicate
coverage is also invalid because it would assign more than one law to the same coordinate. The selected law must support
the presentation domain of the coordinate to which it is bound.

Changing the Input coordinate surface invalidates an old binding whenever that binding no longer describes the same
coordinates and domains. Source declaration order cannot provide a fallback for such a change; the declaration must be
ratified again.

No new coordinate receives an undeclared `Exact` default.

A whole-presentation built-in law has the same completeness obligation, but the completeness belongs to that law rather
than to a user-written parameter list.

### 5.5. No Application-Defined Canonicalization Law in V1

V1 exposes no application-defined Canonicalization law extension point.

This restriction is deliberate. A custom canonicalizer can change distinctions that later authorities expected to
observe. It can also turn an otherwise bounded boundary operation into attacker-controlled work or silently repair
material that should never have reached Canonicalization. Allowing a plain `T -> T` callback would hide those semantic
decisions behind executable behavior and make it possible for Admission or Lowering to observe a meaning that the
Contract declaration never closed.

A future version may open custom Canonicalization only through a separate design and security review. The extension must
make the semantic burden explicit rather than simply accepting executable code. A custom law would need to declare its
legal presentation domain and the distinctions it preserves. Its equivalence and representative behavior would have to
be inspectable rather than inferred from code. Any external semantic basis that can change the result would need
Contract-owned versioning. The design would also have to say when the law refuses, how aggregate collisions are
resolved, and how much work the law can require. Determinism and conformance would then be specified against that closed
meaning.

The product must also explain the security consequences to users before application-defined authority is exposed. A
custom law can change what later machine responsibilities consider the same key or value. That can affect authorization
and lookup as well as reuse or deduplication. The fact that the callback looks like a harmless normalizer does not
reduce that authority.

Future custom support must not weaken the V1 rule by treating an application callback as a nominal type implementation.
It requires an explicit Contract surface of its own.

### 5.6. Public Law Specification

Every public Canonicalization type must be backed by a complete Kontrakt-owned specification.

The specification must identify the Input presentation domain for which the law is legal. It must explain which
distinctions survive and which are collapsed, then describe the stable representative and the cases that fall outside
the canonicalizable domain. If the supported presentation has explicit absence or another finite alternative, the
specification must say how that distinction is treated. A law that depends on external semantic data must also identify
the basis whose version can change its meaning.

Aggregate laws must define what happens when Canonicalization causes previously distinct elements or keys to collide.
The result cannot depend on whichever element the implementation happened to encounter first. A host container's
iteration or replacement behavior is therefore not a substitute for the declared collision law.

The specification must state the semantic bounds that affect the legal domain or required representative. It must also
distinguish a Canonicalization refusal from a stop owned by another Contract. If exact canonical bytes belong to the
law, the specification must identify the byte protocol whose output is normative.

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

The law must identify the presentation domain on which it is valid. Within that domain it must say which distinctions
survive Canonicalization and which distinctions are treated as equivalent. The required representative must then follow
from that declaration. Domain-specific semantics need only be specified where the law can actually observe them, but no
observed distinction may be left to host convention.

A law that depends on external semantic data must pin the material that can change its result. Unicode and temporal
rules are common examples because their data can be versioned independently of the application. The same principle
applies to any other semantic profile: whichever provider happens to be installed on the host cannot silently complete
Contract meaning.

A Canonicalization type is accepted only when it resolves to the exact closed law published by Kontrakt. An
application-defined look-alike or dynamically supplied replacement does not satisfy that requirement. An incompletely
specified law is rejected for the same reason.

Executable law values are not Canonicalization authority. The law must be known without asking application behavior to
tell the compiler what it means. Host dispatch or reflection cannot complete missing meaning, and mutable runtime state
cannot supply it later. The same prohibition covers external dependencies that would have to be consulted during
canonicalization.

Host defaults cannot finish a Canonicalization law. If locale or another environment-sensitive rule affects the
representative, that rule must be explicit semantic material. Incidental container order and other host iteration
behavior likewise cannot select the representative.

A law may reject a legal Input when that Input lies outside its declared canonicalizable domain. It may not use
rejection as a substitute for Input legality, and it may not repair illegal Input into the domain.

Canonicalization must also preserve its responsibility boundary. It cannot change the Input shape or derive business
meaning while pretending to establish an equivalent representative. Parsing into another domain and resolving external
resources are examples of work that belongs elsewhere.

---

## 7. Finite-Work Law

Canonicalization is a producer Contract on an untrusted inbound path. It cannot be permission for unlimited work.

Every V1 law must have a finite, analyzable work shape for the presentation domains that the public API allows. The law
must make clear how much material it may have to inspect and how much representative material it can produce. Structured
domains also need a finite traversal boundary. Temporary work that can grow with the Input must remain bounded enough
for the implementation to realize the law safely.

The Contract must distinguish a semantic bound from a compiler resource limit. A semantic bound belongs to the law when
crossing it changes whether an Input is in the legal domain or changes which representative is required. A compiler
safety limit is different: it constrains the implementation without changing Canonicalization meaning.

General application-defined imperative iteration and recursive object-graph traversal are not available as
Canonicalization behavior in V1. A closed Kontrakt-owned law may operate over finite structured presentation, but the
law must already determine how traversal affects meaning. If ordering or duplicate handling can change the
representative, that behavior must be part of the ratified law rather than discovered during execution.

A valid realization may avoid work when the Input already satisfies the representative law. It can also reuse already
validated immutable material or substitute a faster implementation for the same closed operation. These are
implementation choices because the law specifies the result rather than the physical route used to reach it.

Deterministic implementation aids do not acquire semantic authority merely because they help compute a representative.
For example, a digest may narrow the candidates that need exact comparison, but digest equality alone cannot justify
collapsing two presentations. The selected law must still establish the equivalence.

Budget and Capacity retain their own declared authorities. Crossing one of their walls does not create a different
Canonicalization representative or redefine equivalence.

---

## 8. Canonicalizable-Domain Condition

Canonicalization starts inside the legal Input Presentation domain.

A source is not refused merely because it is not already canonical. A presentation that differs from the representative
in a way explicitly accepted by the selected law is ordinary Canonicalization input.

A Canonicalization refusal occurs only after the law has a legal Input presentation to judge. Refusal occurs when the
selected law cannot establish its required representative for that presentation under its own declared domain. The exact
reason belongs to the law rather than to a generic normalization failure.

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

There is no user-authored post-production check that can accept a different result after Kontrakt has completed the
selected law. A helper named `isCanonical` or `validateCanonical` therefore cannot become a second authority over the
established representative.

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

Runtime does not rediscover the declaration or complete semantic choices that definition-time processing left open. It
executes only Kontrakt-owned realization over already resolved law material. Any runtime mechanism needed to perform
that realization therefore receives closed semantic inputs rather than authority to select the law.

This revision deliberately stops before the Canonicalization HIR and Establishment model is reclosed. The next pass must
first define the HIR candidate and its binding relation. It must then close the Basis and Applicability that
Establishment relies on. After that, it must decide whether Canonicalization owns an Occurrence and define the
Established Material that legal consumers may observe. ADR-0071 and ADR-0063 provide the common model, while the 1D
master checklist controls the audit.

## 10. Identity and Conformance

Canonicalization identity is derived from ratified meaning.

It is not derived from the selected source class name alone.

The identity must change whenever a difference in the selected law can change which Inputs are equivalent or which
representative is required. It must therefore cover the presentation meaning to which the law applies, the law's
semantic profile, and the bindings that determine how that law is assigned. The canonicalizable domain and refusal law
also belong to the meaning being identified. When bounds or canonical bytes are Contract-visible parts of the law, their
normative protocol belongs to the same identity.

A frontend change that changes accepted meaning or generated canonical material changes Contract identity.

Every built-in law must have a versioned normative conformance-vector set and a separate implementation-verification
suite.

Changing semantic meaning or a normative expected result changes the law identity.

Changing the verification suite alone does not change Contract meaning. The suite may gain stronger adversarial and
differential coverage without changing the law. Performance coverage may also evolve independently when it does not
alter the normative result.

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

Implementation conditions do not choose a different representative. Resource availability can stop work through its own
authority, and reuse may change how much work is performed, but neither changes Canonicalization meaning. Host defaults
and scheduling choices are equally unable to complete the law.

Generated canonicalizers and byte emitters remain implementation-axis machinery. Kontrakt may replace their physical
algorithm or avoid work that has already been proven unnecessary. It may also combine implementation steps when doing so
preserves the same Contract-visible result. None of these changes may introduce observable State that Canonicalization
itself does not own.

Physical reordering across the Canonicalization boundary requires a stronger proof than ordinary local equivalence. A
later judgment may move before Canonicalization only if the compiler can prove that the moved computation cannot
distinguish two presentations that the selected Canonicalization law treats as equivalent. If it can distinguish them,
moving it across the boundary changes the declared machine.

```text
x equivalent-to y under the selected Canonicalization law
    ->
any computation moved across that boundary
    must observe the same result for x and y
```

This proof obligation constrains optimization rather than adding a new Canonicalization judgment. The logical Contract
order remains authoritative even when execution is fused or scheduled differently.

Semantic determinism does not require one physical execution shape. Timing behavior and allocation behavior belong to
separate realization or security questions unless another declared obligation makes them observable.

---

## 12. Refusal Boundary

Canonicalization receives only material that Input has already established as a legal presentation and only when the
selected Canonicalization branch is applicable.

It refuses only under a Canonicalization-owned negative condition after legal entry. A presentation may be noncanonical
and still succeed. A presentation may be legal Input yet lie outside the selected Canonicalization law's domain and
therefore be refused by Canonicalization.

Canonicalization must not convert Input refusal into Canonicalization refusal by repairing the original material. It
also cannot absorb a negative result that belongs to a later Contract or to a cross-cutting authority. Each result keeps
the owner that declared it.

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

ADR-0048 owns the final shared inbound composition. This ADR therefore does not redefine slot singularity or
whole-airlock wiring. It fixes only the Canonicalization-side requirement that the representative be established before
a later judgment relies on the meaning Canonicalization governs.

---

## 14. Open in This Section

The final IDL token spelling may change.

The exact public nominal Canonicalization type names may change.

The exact Java or Kotlin carrier syntax for an inert coordinate-law declaration may change.

The exact set of V1 built-in laws remains API and platform-support work. A candidate name in this ADR is not a ratified
promise until its semantic and security review is complete.

A future application-defined Canonicalization facility remains deferred. It requires a separate Contract design and a
Security ADR rather than an extension point hidden inside the V1 nominal type API.

The next revision must reclose Canonicalization against the common HIR and Establishment model. That work includes the
Definition Candidate and Binding Candidate, then the Required Basis and Applicability needed by Establishment. It must
also decide whether Canonicalization owns an Occurrence and what Established Material its legal consumers may observe.
Current-validity and V2 incremental treatment remain later compiler concerns.

Any frontend change must preserve the V1 authoring boundary described by this ADR. The IDL must continue to select the
law explicitly, and omission must remain a distinct choice. Source evidence must still resolve to closed Kontrakt-owned
law material. Coordinate-law authoring must cover its selected surface completely, while executable application behavior
and inferred recursive law construction remain outside the role.

---

## 15. Consequences

Canonicalization becomes an explicit inbound representation-control Contract rather than cleanup code.

The outside world may still choose how to express a legal Input within the presentation space that Input permits. It
does not automatically get to decide which of those differences remain meaningful after the Canonicalization boundary.

The user retains that semantic choice through the IDL. V1 gives the user control by exposing reviewed law selections,
not by handing over executable authority to arbitrary callbacks.

Kontrakt can therefore verify and optimize the realization against closed law material. An implementation can avoid work
for material that already satisfies the representative law, and structured laws can use more specialized realizations
when useful. Those changes remain implementation because the same representative and semantic bounds still govern the
result.

Omission remains explicit and cheap. It preserves the Input presentation distinctions rather than pretending that an
invisible identity canonicalizer ran.

The selected branch obtains one canonical interpretation for later inbound judgments. Admission and Lowering cannot
silently disagree by re-reading different normalized forms of the same outside material.

The stricter V1 authoring boundary reduces flexibility. That restriction is deliberate. A custom Canonicalization law
can change what later responsibilities consider equivalent and can therefore alter security-sensitive decisions or
resource cost. Kontrakt should not expose that authority until the Contract surface and security model make those
consequences explicit enough for users to understand what they are declaring.

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

The review also separated several responsibilities that earlier prose could blur together. Canonicalization cannot
repair material that Input never established. Later semantic consumers cannot resurrect distinctions that its selected
law erased. Canonical bytes belong only to laws that explicitly declare them, while semantic bounds remain distinct from
resource results owned by Budget, Capacity, or compiler machinery.

A final pass clarified the remaining authority boundaries. Security suspicion does not create equivalence, and a
combined built-in law owns the order of semantic operations that affects its representative. Equal representatives
establish only Canonicalization's relation rather than another authority's identity or judgment. Compiler reordering
across the stage remains legal only when the moved computation cannot distinguish members of a Canonicalization
equivalence class.

ADR-0048 remains the owner of the shared optional-Canonicalization branch and final inbound composition.
Canonicalization-specific HIR and Establishment reclosure is intentionally deferred to the next ADR-0066 revision pass.