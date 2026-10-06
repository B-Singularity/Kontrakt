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
- ADR-0076: Canonicalization Built-In Law Catalog, Semantic Profile Ratification, and API Projection Boundary
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

Canonicalization gives the Contract author an explicit inbound control over that freedom. Through the IDL, the user
selects one Canonicalization declaration. That declaration identifies the Input coordinates whose representation freedom
is being controlled. A selected coordinate may refer to one exact Kontrakt-owned law or to one finite explicit ordered
composition of exact Kontrakt-owned laws. Kontrakt then establishes the representative required by the complete declared
Canonicalization meaning for that coordinate.

The user selects meaning. The user does not supply the executable canonicalizer that becomes authority. V1 exposes only
exact Kontrakt-owned laws whose meaning has already been closed before application code selects them. V1 may also let a
Canonicalization declaration compose those exact laws in an explicit finite order. The authoring form remains evidence;
it does not become executable Contract authority. The complete selected meaning must remain bounded and independently
verifiable so that the compiler can replace the realization without changing the Contract.

Canonicalization therefore belongs to the inbound airlock. It exists because outside presentation is not trusted to
choose the representation that later controls machine meaning. This is an authority-boundary problem, not a cleanup
convenience.

The outward side has a different responsibility. Publication begins with material that already has Core authority, and
Output Presentation gives Publication-authorized material its declared outward shape. The producing machine already
controls that meaning. It does not need a second inbound-style Canonicalization stage merely to mirror the pipeline.

Canonicalization does not exist to make an inconvenient Input easier for implementation code to handle. It cannot repair
an illegal presentation, reinterpret another domain, or perform the transformation owned by Lowering. It operates only
after Input has established a legal presentation and may remove only the representation freedom that the declared
direct or composed Canonicalization meaning explicitly makes irrelevant.

Canonicalization does not change the Contract-visible coordinate shape of the selected Input. Shape-changing formation
remains outside this authority.

Canonicalization is optional. Omitting the slot means that the Operation has no Canonicalization authority. When the
slot is selected, the declaration names only the Input coordinates for which Canonicalization is intended to establish a
representative.

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

Canonicalization therefore needs an explicit declaration selected through the IDL. Each coordinate binding in that
declaration must resolve either to one exact closed law or to one finite explicit ordered composition of exact closed
laws. A composition is legal only when its complete Canonicalization meaning can itself be closed. The resulting domain,
equivalence, representative, refusal behavior, semantic bases, and bounds must be explicit enough for conformance to be
verified.

---

## 3. Decision Drivers

The Contract author must retain control over which outside representation differences remain meaningful after the Input
boundary.

Equivalent presentation meaning under one selected Canonicalization law must converge to the representative required by
that law. Distinct meaning must not collapse merely because an implementation finds that convenient.

The Canonicalization role must be selected explicitly in the IDL. The IDL names one Canonicalization declaration rather
than a built-in coordinate law directly. The declaration then names the Input coordinates on which Canonicalization is
intended to act. Neither source structure nor host behavior may infer additional bindings.

Omission must remain explicit. Leaving the slot empty means that no Canonicalization authority exists. The compiler must
not replace that omission with an implicit exact law or with generated behavior that acts as one.

A successful Canonicalization result must become the only canonicalized semantic operand for later judgments that depend
on that boundary meaning. Raw Input may remain as separately owned diagnostic or provenance evidence, but later
Contracts must not regain erased distinctions from that evidence.

Canonicalization must never be a repair path for material that Input did not establish as legal presentation.

Users must not author executable canonicalizer behavior as Contract authority in V1. V1 instead exposes a restricted
Kontrakt-owned law catalog. Application code may select exact admitted laws and may explicitly compose those laws where
this ADR permits composition, but it does not supply their executable meaning or define a new Canonicalization law by
providing code.

A future custom-law facility must not appear by relaxing V1 type checks. It requires a separate Contract and security
design because defining a new equivalence law is itself a significant authority. The future surface must therefore make
the consequences of erasing distinctions explicit, including the effects a custom law can have on aggregate meaning and
resource use.

A Canonicalization declaration is selective. Every coordinate that it names must declare exactly one Canonicalization
meaning. That meaning is either one applicable exact Kontrakt-owned law or one legal finite ordered composition of exact
Kontrakt-owned laws. Coordinates that the declaration does not name remain outside Canonicalization. No undeclared
default may turn an omitted coordinate into an implicit Canonicalization application.

Composition must remain a flat semantic declaration rather than a recursive Contract graph. A composition does not
create child 1D Contracts, inheritance, hidden transitive selection, or a user-defined law Authority. The declared order
must survive as meaning, and the compiler must reject a composition whose complete Canonicalization law cannot be
closed. It must not silently repair an invalid declaration by inserting, deleting, reordering, replacing, or weakening
selected laws.

Canonical output must be deterministic under the declared Canonicalization meaning and its semantic bases. A resource
stop or missing implementation capability cannot silently select a different representative. Those conditions retain
the result ownership already assigned to them.

The realization is free to change how it computes the representative. It may avoid unnecessary work or use a more
specialized implementation when the declared binding permits it. That freedom ends at the Contract boundary: every
replacement must preserve the representative and the Canonicalization result. It must also preserve the attribution and
bounds exposed by the complete binding, together with exact canonical bytes where the declared meaning owns them.

---

## 4. Decision

### 4.1. Canonicalization Is an Inbound Representation-Control Contract

Canonicalization governs the representation freedom of already legal outside Input before that freedom can influence
later machine meaning.

The user selects the Canonicalization declaration in the IDL. The declaration binds selected Input coordinates to exact
Kontrakt-owned law meaning. A binding may select one exact law or one legal finite ordered composition of exact laws.
The
complete binding states which distinctions remain observable for that coordinate and which distinctions belong to one
declared equivalence class. Kontrakt owns the realization that produces the required representative.

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
no selected coordinate law
no canonical representative authority
no user canonicalizer
no hidden runtime hook
no generated replacement stage
```

When Canonicalization is omitted, the exact presentation established by Input remains the semantic presentation observed
by Admission and by later inbound Contracts that legally consume it.

Omission does not remove deterministic encoding that another compiler responsibility legitimately needs. Such encoding
may give existing material a stable physical representation for compiler work. It does not collapse Input distinctions
and therefore does not create a Canonicalization representative.

V1 does not expose `ExactCanonicalization` as a filler for coordinates that the user does not want to canonicalize. An
unnamed coordinate simply remains outside Canonicalization, and its Input-established presentation continues unchanged.
If no coordinate requires Canonicalization, the Operation omits the Canonicalization slot instead of selecting an
identity law.

### 4.3. Canonicalization Receives Only Legal Input Presentation

Input remains responsible for deciding whether outside material can become a legal Input Presentation. Canonicalization
cannot reopen material that Input refused, and it cannot transform an illegal carrier into a legal presentation.

Noncanonical does not mean invalid. A legal Input may use a representation variant that the selected direct or
composed binding was declared to collapse. The exact kind of variation depends on the selected domain. Such material is
ordinary Canonicalization input.

Material that never became an established Input presentation is not ordinary Canonicalization input. A later
canonicalizer must not repair it and then claim that the repaired value was what Input established.

This boundary prevents Canonicalization from becoming a hidden parser or sanitizer placed ahead of the declared Input
law.

### 4.4. Declared Equivalence and Stable Representative

Canonicalization removes only declared representation freedom.

For any selected Canonicalization law `L`, let `E_L` denote the exact same-meaning relation owned by that law, and let
`C_L` denote representative selection over the law's successful canonicalizable domain. `E_L` is specified independently
of the procedure that forms the representative. A representative implementation therefore cannot define its own
equivalence by accidentally collapsing additional distinctions.

For every successful input in the law's domain, the common Canonicalization law requires:

```text
C_L(x) = C_L(y)
    iff
E_L(x, y)

E_L(x, C_L(x))

C_L(C_L(x)) = C_L(x)
```

`E_L` must be an equivalence relation on the domain for which the law claims same-meaning comparison. `C_L` must select
a representative from the same declared meaning class. These are properties of Canonicalization as a Contract rather
than optional implementation or optimization properties.

For every supported presentation domain, the selected law must make it possible to determine which distinctions are
preserved and which are collapsed. A distinction that has not been explicitly made irrelevant remains meaningful.

A successful application establishes the one representative required by the selected law for the applicable equivalence
class. Implementation convenience cannot select another representative. The backend must therefore adapt to the declared
law rather than substituting a form that is cheaper or easier for the host platform.

A published V1 law must be stable under repeated application. Applying the same law to a value that already satisfies
its representative law must not move that value again. A hidden second normalization therefore cannot create another
semantic result under the same law.

ADR-0076 owns the exact `E_L`, `C_L`, supported presentation relation, canonicalizable domain, determinants, Required
Basis requirements, and owned refusal of each built-in Catalog Law. This ADR owns the common semantic shape that every
Canonicalization law must satisfy.

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

### 4.4.1. Finite Ordered Composition of Exact Laws

A Canonicalization Definition may declare one selected coordinate through a finite explicit ordered composition of exact
admitted Built-In Laws.

Composition is a Canonicalization Definition relation. It does not create a new Built-In Law Authority merely because
the composition is legal. It also does not turn the referenced laws into child 1D Contracts. The Definition records one
flat ordered relation over exact law references; it does not create inheritance, recursive Contract ownership, hidden
transitive selection, or a user-defined law implementation.

For illustration only, a source surface may express a declaration such as:

```text
compose(A, B, C)
```

The spelling `compose` is non-normative. The Contract meaning is the explicit ordered relation:

```text
A -> B -> C
```

The declared order is Definition-determining meaning. `A -> B -> C` and `C -> B -> A` are not silently identified merely
because one implementation, one test set, or one current semantic basis happens to produce equal results. A later
realization may fuse or otherwise optimize physical work only when it preserves the complete declared composition
meaning.

The ordered sequence is itself required to satisfy the common Canonicalization law. The compiler must derive the
composition's complete successful same-meaning relation and representative relation from the exact constituent law
meaning, then verify the common obligations of Section 4.4. In particular, successful composition must remain
same-shape, deterministic, representative-preserving, and stable under repeated application.

Composition legality is decided before Canonicalization authority begins. The definition is invalid when the compiler
cannot close the complete composition. The check includes the presentation applicability of every constituent, the
compatibility of meaning-determining semantic bases and Required Basis requirements, deterministic refusal behavior,
whole-composition boundedness, and the absence of semantic dependency cycles. A cycle is a compilation failure. No
fixed-point or repeated-search interpretation is attempted.

The compiler does not repair an invalid composition by adding a normalization step, deleting a declared step, changing
the order, replacing one law with another law, or weakening a constituent law. A proven redundant or fully shadowed
step likewise must not disappear from the declared meaning without being surfaced. The exact diagnostic severity for a
legal but suspicious declaration belongs to Diagnostic and Design work; this ADR only forbids silent semantic repair or
silent replacement of the user's declaration.

A curated combined Built-In Law remains an independent exact Catalog Law when ADR-0076 admits it as one. Selecting such
a law is not the same declaration as composing other Built-In Laws that appear to perform similar operations. Output
equality alone does not merge their Authority, Version, or Definition meaning.

### 4.5. Judgment–Use Coherence

Once Canonicalization establishes a representative for a selected coordinate, later judgments must use that
representative wherever they consume that coordinate. Coordinates that were not selected remain the Input-established
presentation and do not acquire a Canonicalization judgment.

Admission must not judge the representative and then allow Lowering to recover a canonicalized-away distinction from raw
Input. Lowering must not independently normalize the source again under another rule. The same restriction applies to
downstream host code: a library or adapter cannot silently reinterpret the representative through another normalization
rule and still claim to be using the selected Canonicalization law.

The selected branch is therefore conceptually:

```text
Established Input Presentation
    -> Canonicalization on selected coordinates
    -> boundary presentation
         selected coordinates: established representatives
         unselected coordinates: Input-established presentation
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
requirement that every selected coordinate passes forward through its established representative, while every unselected
coordinate keeps its Input-established presentation. It also forbids later semantic resurrection of distinctions that a
selected direct or composed binding erased.

Raw Input material may be retained under separately owned provenance or diagnostic rules. Retention does not restore
semantic authority to that raw representation.

### 4.6. Same-Shape Representative

Canonicalization does not perform shape-changing Operation input formation.

Canonicalization operates only on the Input coordinates named by the selected declaration. The surrounding Input
coordinate surface remains unchanged.

For each named coordinate, the selected direct or composed binding may replace the Input-established value with the
representative required by that complete binding. A coordinate that is not named is not processed through an identity
law; it simply retains the presentation already established by Input.

Canonicalization cannot change the declared coordinate structure. A selected direct or composed binding may control
representation within the supported domain of its coordinate, but it cannot create a new coordinate or remap the Input
surface. Lowering, or another explicitly declared transformation boundary, remains responsible for shape-changing
formation.

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

A Definition-level composition does not synthesize a new canonical-byte protocol from constituent byte protocols. The
final representative remains authoritative even when no composition-owned canonical bytes exist. If a future composed
binding exposes exact canonical bytes as Contract material, the exact relation between the composition meaning and those
bytes must be closed explicitly rather than inherited from implementation order or from one constituent by convention.

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

## 5. V1 Canonicalization Authoring Contract

V1 deliberately exposes a narrow Canonicalization authoring boundary.

The IDL selects one Canonicalization declaration for the applicable role. It does not select individual Built-In Laws or
composition steps directly. The selected declaration is frontend evidence for one Canonicalization Definition; source
syntax does not independently acquire Contract authority.

The declaration names only the direct Input coordinates on which the user intends Canonicalization to act. Each named
coordinate declares one Canonicalization meaning. That meaning is either one exact admitted Built-In Law or one finite
explicit ordered composition of exact admitted Built-In Laws. Coordinates that are not named remain outside
Canonicalization and retain the presentation established by Input.

The authoring boundary does not permit application code to supply executable canonicalizer behavior. It also does not
permit runtime registration, runtime law selection, host-type inference, inheritance-based role acquisition, or
recursive Contract composition. Frontend evidence is legal only when the compiler can resolve its complete
Canonicalization meaning without executing application behavior.

The exact public Java or Kotlin API, token names, carrier forms, marker types, helper names, and other host-language
realization are Design work. They may change without changing this ADR when they preserve the authoring relations and
semantic restrictions defined here.

For illustration only, an API realization may provide a source form conceptually equivalent to:

```text
coordinate X -> exact law A
coordinate Y -> compose(A, B, C)
```

The word `compose` and the concrete source syntax are non-normative. The normative meaning is that coordinate `Y`
contains one finite ordered composition relation over the exact law references `A`, `B`, and `C`.

When the Canonicalization slot is omitted, the IDL selects no Canonicalization declaration. Omission says nothing about
values that existed before the selected Input was submitted. Kontrakt reasons only from the Input presentation that its
own boundary established.

### 5.1. Built-In Law Catalog Boundary

ADR-0076 owns Built-In Law Authority membership and the exact versioned meaning of each admitted law. One exact law
reference in a Canonicalization declaration therefore resolves to one already-admitted Built-In Law Authority and
Version under the rules owned by ADR-0076 and ADR-0053.

A Built-In Law does not inherit meaning from another law and does not obtain meaning from runtime composition. A curated
combined Built-In Law may itself own semantics that involve several familiar operations, but its exact ordering,
intermediate meaning, representative, and evolution remain owned by that one law Authority.

Definition-level composition does not change this Catalog boundary. A legal ordered composition refers to several exact
Built-In Laws without admitting another Catalog member and without merging the constituent Authorities. The composition
belongs to the Canonicalization Definition that declared it.

Implementation code may share algorithms across laws or across a composition. Such reuse does not create, merge, or
replace Contract law meaning.

V1 does not publish a generic `ExactCanonicalization` law merely to fill coordinates that require no canonicalization.
Absence of a coordinate binding already expresses that Canonicalization has no authority over that coordinate.

### 5.2. Exact Law References

Every law reference admitted by the authoring surface must resolve to one exact Kontrakt-owned Built-In Law meaning.
Application-defined look-alikes, aliases that change semantic identity, dynamically registered replacements, runtime
objects, package convention, source proximity, inheritance, or structural similarity do not acquire the law.

The authoring surface may use host-language names or values as evidence, but those artifacts are not the law. Frontend
resolution must terminate at the exact semantic law reference before Canonicalization authority begins.

A law reference may denote a law over a scalar presentation or over another complete closed presentation domain that
Kontrakt has explicitly ratified for one Input coordinate. Host type shape does not imply Canonicalization meaning, and
an application cannot construct a recursive Canonicalization tree by nesting generic or structural source forms.

### 5.3. Coordinate Binding and Composition

A Canonicalization declaration is one flat Contract declaration. Each selected coordinate has exactly one declared
Canonicalization meaning.

The direct binding form is:

```text
selected direct Input coordinate
+ exact supported presentation domain
+ one exact admitted Built-In Law
= one direct Canonicalization binding
```

The composed binding form is:

```text
selected direct Input coordinate
+ exact supported presentation domain
+ one finite ordered sequence of exact admitted Built-In Laws
+ successful Composition Legality judgment
= one composed Canonicalization binding
```

A composed binding remains one coordinate binding inside one Canonicalization Definition. Its constituent law references
do not become independently selectable 1D Contracts and do not create a nested Contract hierarchy.

The semantic composition is finite and non-recursive. The frontend may use a convenient host-language expression to
present it, but the resolved meaning must be one flat ordered sequence of exact law references. Runtime conditions,
callbacks, loops, registry lookup, ambient values, or another composition chosen dynamically cannot decide the sequence.

Source declaration order outside the explicit composition does not create a fallback semantic order. The order inside
the resolved composition is authoritative and must be preserved as Definition meaning.

### 5.4. Selective Coordinate Coverage

A Canonicalization declaration contains one or more explicit coordinate bindings. An empty declaration is invalid
because it would establish no Canonicalization judgment; an Operation that needs no bindings omits the Canonicalization
slot instead.

Every coordinate named by the declaration must resolve to exactly one direct Contract-visible Input coordinate. Naming
the same coordinate more than once is invalid. Naming a coordinate that does not belong to the selected Input surface is
also invalid. The direct law or every constituent of a composed binding must satisfy the applicable presentation-domain
requirements owned by Sections 6 and 4.4.1.

A direct Input coordinate that is not named by the declaration remains outside Canonicalization. Its Input-established
presentation passes forward unchanged. No `ExactCanonicalization` or other identity law is synthesized for it.

Adding a new Input coordinate does not by itself invalidate an existing Canonicalization declaration. If the new
coordinate is not named, it remains outside Canonicalization. The author may add a binding when that coordinate should
also lose declared representation freedom.

A selected binding does become invalid when its Input coordinate can no longer be resolved as declared. Removing or
renaming that coordinate breaks the binding. Retyping it also breaks the binding when the declared Canonicalization
meaning no longer applies to the coordinate's presentation domain. The compiler must not repair such a change by
position, source order, inferred law, or inferred composition.

Whether a wider Input-surface change also changes Canonicalization Definition identity is deliberately left to the HIR
and Establishment identity re-audit. This authoring rule decides validity of the sparse declaration, not the final
identity formula.

### 5.5. No Application-Defined Canonicalization Law in V1

V1 exposes no application-defined Canonicalization law extension point.

This restriction is deliberate. A custom canonicalizer can change distinctions that later authorities expected to
observe. It can also turn an otherwise bounded boundary operation into attacker-controlled work or silently repair
material that should never have reached Canonicalization. Allowing a plain `T -> T` callback would hide those semantic
decisions behind executable behavior and make it possible for Admission or Lowering to observe a meaning that the
Contract declaration never closed.

Finite ordered composition does not weaken this restriction. Composition may reference only exact law meaning already
admitted under the Built-In Catalog. It does not allow application code to define equivalence, representative selection,
refusal, semantic bases, or executable normalization behavior.

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
reduce
that authority.

Future custom support must not weaken the V1 rule by treating an application callback as a Built-In Law reference or as
one constituent of a legal composition. It requires an explicit Contract surface of its own.

### 5.6. Built-In Law Specification

Every Built-In Law reference available to Canonicalization authoring must be backed by a complete Kontrakt-owned
specification under ADR-0076.

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
under a direct or composed binding while the Contract-visible Input surface remains the same. A different user-visible
target shape belongs to Lowering.

## 6. Canonicalization Law and Composition Eligibility

### 6.1. Exact Built-In Law Eligibility

An exact Built-In Law may appear in a direct binding or as a constituent of a composed binding only after Kontrakt can
determine its complete meaning without executing application behavior. Publication into the V1 Built-In Catalog and the
exact qualification of that law remain owned by ADR-0076.

The law must identify the presentation domain on which it is valid. Within that domain it must say which distinctions
survive Canonicalization and which distinctions are treated as equivalent. The required representative must then follow
from that declaration. Domain-specific semantics need only be specified where the law can actually observe them, but no
observed distinction may be left to host convention.

Applicability is positive. A Canonicalization law may govern a coordinate only when the law explicitly admits that exact
Input presentation meaning. Absence of an admitted relation is not permission, and an unknown or incompatible
law/presentation pairing is a definition-time error rather than a runtime fallback. This selection applicability is
separate from the canonicalizable value domain: applicability decides whether the law may govern that kind of
presentation, while the canonicalizable domain decides whether one legal occurrence succeeds or receives a
Canonicalization-owned refusal.

ADR-0076 owns the positive applicability facts of Built-In Catalog Laws. This ADR owns the common rule that such a fact
must exist before a direct or composed reference is legal.

A law that depends on external semantic data must close every semantic input that can change its result. Material fixed
by the law itself is Definition-determining meaning. Material that must be supplied through a later legal basis relation
is an explicit Required Basis requirement. Ambient provider state cannot substitute for either form.

ADR-0076 specifies that distinction for each Built-In Law. The Canonicalization Definition Candidate must preserve any
Required Basis requirement that a referenced law actually needs; Basis Resolution, Basis Binding, and Applicability of
an actual basis remain owned by ADR-0063.

A law reference is accepted only when it resolves to the exact closed law admitted by Kontrakt. Application-defined
look-alikes and dynamically supplied replacements do not satisfy that requirement. An incompletely specified law is
rejected for the same reason.

Executable law values are not Canonicalization authority. The law must be known without asking application behavior to
tell the compiler what it means. Host dispatch or reflection cannot complete missing meaning, and mutable runtime state
cannot supply it later. The same prohibition covers external dependencies that would have to be consulted during
Canonicalization without an explicit Contract-owned basis relation.

Host defaults cannot finish a Canonicalization law. If locale or another environment-sensitive rule affects the
representative, that rule must be explicit semantic material. Incidental container order and other host iteration
behavior likewise cannot select the representative.

A law may reject a legal Input when that Input lies outside its declared canonicalizable domain. It may not use
rejection as a substitute for Input legality, and it may not repair illegal Input into the domain.

Canonicalization must also preserve its responsibility boundary. It cannot change the Input shape or derive business
meaning while pretending to establish an equivalent representative. Parsing into another domain and resolving external
resources are examples of work that belongs elsewhere.

### 6.2. Composition Eligibility

A composition is eligible only when every constituent reference is an exact eligible Built-In Law and the complete
ordered relation can itself satisfy the Canonicalization law of Section 4.4.

The compiler must establish composition legality from semantic law material rather than from API names, implementation
functions, observed sample outputs, or host call structure. Pairwise type compatibility alone is insufficient when a
later law interprets a distinction differently from the law that precedes it.

At minimum, composition legality requires all of the following to be closed:

```text
finite ordered constituent sequence
presentation applicability at every step
same-shape intermediate and final representatives
complete successful equivalence and final representative relation
deterministic constituent and whole-composition refusal behavior
compatibility of Definition-determining semantic material
compatibility of explicit Required Basis requirements
whole-composition repeated-application stability
whole-composition finite-work and expansion bounds
absence of semantic dependency cycles
```

A legal composition may contain a constituent whose canonicalizable value domain can refuse a particular legal
occurrence. That is different from a definition-time incompatibility. Definition-time legality asks whether the declared
composition has a complete deterministic meaning; occurrence-time refusal asks whether one legal Input lies inside the
successful domain of that already-valid composition.

A composition is invalid when later meaning depends on information that an earlier step erased but the complete
composition does not explicitly and coherently define how that dependency is satisfied. It is also invalid when
reapplication can move an already established final representative, when semantic bases cannot be bound coherently, or
when the whole work shape cannot satisfy the finite-work law.

A semantic dependency cycle exposed by the composition is a definition error. Kontrakt does not search for a fixed
point, iterate until a cap, or accept a partial representative.

A proven redundancy or shadowing relation does not authorize the compiler to rewrite the declaration into another
composition. Such evidence may support a diagnostic or a physical optimization, but the declared ordered relation
remains the Definition meaning unless a Contract-level rule explicitly says otherwise.

## 7. Finite-Work Law

Canonicalization is a producer Contract on an untrusted inbound path. It cannot be permission for unlimited work.

Every V1 law must have a finite, analyzable work shape for the presentation domains that the authoring contract allows.
The law
must make clear how much material it may have to inspect and how much representative material it can produce. Structured
domains also need a finite traversal boundary. Temporary work that can grow with the Input must remain bounded enough
for the implementation to realize the law safely.

A composed binding must satisfy the same requirement as one complete unit. Bounded constituents do not by themselves
prove a bounded composition because one step may expand material or create a worst-case input for a later step. The
composition review must therefore account for intermediate growth, final representative growth, traversal, and any
other work amplification that follows from the declared sequence.

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
collapsing two presentations. The complete direct or composed binding must still establish the equivalence.

Budget and Capacity retain their own declared authorities. Crossing one of their walls does not create a different
Canonicalization representative or redefine equivalence.

---

## 8. Canonicalizable-Domain Condition

Canonicalization starts inside the legal Input Presentation domain.

A source is not refused merely because it is not already canonical. A presentation that differs from the representative
in a way explicitly accepted by the selected direct or composed binding is ordinary Canonicalization input.

A Canonicalization refusal occurs only after the declared binding has a legal Input presentation to judge. Refusal
occurs when the valid direct or composed binding cannot establish its required representative for that presentation
under its declared successful domain. The exact reason remains attributable to the exact Canonicalization meaning that
refused rather than to a generic normalization failure.

Input illegality is not Canonicalization refusal. Compiler inability is not Canonicalization refusal. Budget or Capacity
stops retain their own result ownership.

An illegal composition is also not Canonicalization refusal. If the compiler cannot establish the complete semantic
legality of a declared composition, the Canonicalization Definition is rejected before runtime. Refusal belongs only to
an already-valid Canonicalization meaning applied to one legal occurrence.

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
declared Canonicalization meaning. A helper named `isCanonical` or `validateCanonical` therefore cannot become a second
authority over the established representative.

---

## 9. Definition-Time Processing, HIR, and Establishment Boundary

Definition-time processing preserves the IDL-first and sparse coordinate authoring boundary defined above. The runtime
does not rediscover source syntax, choose composition members, or complete semantic choices left open by the frontend.

The Canonicalization definition path is:

```text
resolve the exact Canonicalization declaration selected by the IDL
-> acquire the selected declaration through the frontend
-> verify one flat selectable Canonicalization declaration
-> require at least one declared coordinate binding
-> bind every declared coordinate reference to one legal direct Input coordinate
-> reject duplicate, unknown, removed, or incompatible selected coordinates
-> for each binding, resolve either:
       one exact Built-In Law reference
   or  one finite ordered sequence of exact Built-In Law references
-> perform Composition Legality judgment for every composed binding
-> leave unselected Input coordinates outside Canonicalization
-> reject executable, dynamic, recursive, or ambient law-selection behavior
-> preserve each referenced law's presentation applicability and canonicalizable-domain requirements
-> preserve Definition-determining semantic material and explicit Required Basis requirements
-> preserve declared composition order as Definition meaning
-> derive stable Canonicalization Definition Candidate material
```

After frontend resolution, HIR refers to exact semantic law references rather than to Java or Kotlin API artifacts. A
direct binding carries one exact law reference. A composed binding carries the flat ordered law-reference relation and
the semantic material required to preserve the established composition meaning. The exact storage representation of
that relation remains HIR Design rather than Contract law.

Coordinate bindings remain part of one Canonicalization Definition Candidate. Referenced Built-In Laws do not become
child 1D Definitions merely because they have stable Authorities and Versions. A user-declared composition likewise does
not create another Built-In Law Authority or Catalog member.

A curated combined Built-In Law crosses the boundary as one exact law reference. HIR does not reconstruct such a law by
expanding a public name into helper operations. In contrast, a Definition-level composition must preserve its own
explicit ordered law-reference relation because that order is part of the declaring Definition meaning.

If any referenced law has a Required Basis requirement, the Definition Candidate preserves that requirement wherever the
law needs it. Semantic material fixed by a law itself remains Definition-determining meaning instead. A composed binding
must also preserve whatever relation is required to establish that its constituent basis requirements can coexist
legally. Actual Basis Resolution, Basis Binding, and Applicability remain owned by ADR-0063.

Establishment must judge the Canonicalization Definition from the exact selected law meaning, any declared composition
meaning, and the legal binding context. It must not derive authority from host type identity, API helper identity,
implementation topology, Catalog row position, generated evaluator shape, or runtime object identity.

The exact Canonicalization occurrence unit remains OPEN. This ADR does not yet decide whether occurrence is per selected
coordinate, per declaration application, or another semantic unit. The Established Material shape, Established Semantic
Protocol, current-validity relation, precise Definition identity consequences of wider Input-surface change, and exact
HIR storage form for composition remain OPEN under the ADR-0071 / ADR-0063 / 1D master-checklist re-audit.

Runtime receives only Kontrakt-owned realization over already resolved Canonicalization meaning. It performs no
declaration lookup, reflection-based law selection, dynamic composition, ambient semantic-table selection, or
reconstruction of Catalog meaning from executable helpers.

## 10. Identity and Conformance

Canonicalization identity is derived from ratified meaning.

It is not derived from the selected source class name alone.

The identity must change whenever a difference in a selected coordinate binding can change which presentations are
equivalent or which representative is required. It must therefore cover the selected coordinate relation and the
semantic profile of every exact law referenced by that binding. For a composed binding, the ordered law-reference
relation is Definition-determining meaning. Two differently ordered declarations do not merge merely because their
current representatives happen to coincide. The canonicalizable domain and refusal law of the complete binding also
belong to the meaning being identified.

An Input coordinate that is not selected does not become an implicit exact-law determinant merely because it exists
beside the selected coordinates. The exact effect of wider Input-surface changes on Definition identity remains part of
the HIR and Establishment re-audit. When bounds or canonical bytes are Contract-visible parts of a selected law or legal
composition, their normative protocol belongs to the same identity. Host API spelling, helper identity, vararg layout,
source line, runtime marker identity, and compiler lowering shape do not.

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

Canonicalization result meaning depends only on semantic material that the selected Canonicalization binding declares
relevant.

For one exact law `L`, one legal presentation `x`, and any two legal realizations `R1` and `R2` operating under the same
law meaning and the same applicable Required Basis binding when one is required, the Canonicalization-owned outcome must
be identical:

```text
Outcome(R1, L, x) = Outcome(R2, L, x)
```

The outcome is the exact representative or the exact Canonicalization-owned refusal of `L`. This universal law is owned
here; ADR-0076 supplies the exact determinants and outcome definition for each built-in law.

For one legal composed binding `S = [L1, L2, ..., Ln]`, the same rule applies to the complete ordered composition.
Changing realization strategy cannot change the final representative, the exact Canonicalization-owned refusal and its
attribution, the semantic basis relation, or any other Contract-visible result owned by the composition.

The declaration-level law is conceptually:

```text
same Canonicalization declaration meaning
+ same meanings for every selected Input coordinate
+ same meaning-determining versioned semantic bases, where applicable
= same representatives for the selected coordinates
+ same Canonicalization outcome
+ same Contract-owned attribution
+ same canonical bytes, only where the complete binding owns canonical bytes
```

Implementation conditions do not choose a different representative. Resource availability can stop work through its own
authority, and reuse may change how much work is performed, but neither changes Canonicalization meaning. Host defaults
and scheduling choices are equally unable to complete the law.

Generated canonicalizers and byte emitters remain implementation-axis machinery. Kontrakt may replace their physical
algorithm or avoid work that has already been proven unnecessary. For a composed binding it may fuse constituent work,
share intermediate material, or remove physical work whose absence is proven unobservable. Such optimization does not
rewrite the declared ordered relation, replace it with a different Built-In Law, or change Definition identity. None of
these changes may alter the representative, refusal behavior, attribution, semantic basis relation, or observable State
owned by the Contract.

Physical reordering across the Canonicalization boundary requires a stronger proof than ordinary local equivalence. A
later judgment may move before Canonicalization only if the compiler can prove that the moved computation cannot
distinguish two presentations that the selected direct or composed binding treats as equivalent. If it can distinguish
them, moving it across the boundary changes the declared machine.

```text
x equivalent-to y under the selected Canonicalization binding
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

For a legal composed binding, a Canonicalization-owned refusal arising from one constituent must remain
deterministic and attributable to the exact constituent meaning that refused. The composition must not flatten a
constituent refusal into an unrelated generic error or continue with later steps after the declared sequence has failed.

An invalid composition never reaches this refusal surface. Composition illegality is a definition-time rejection, not a
runtime Canonicalization result.

A Budget or Capacity stop observed while realizing Canonicalization remains owned by Budget or Capacity. Compiler
resource exhaustion or implementation unavailability is not a new Canonicalization semantic result.

When Canonicalization is omitted, there is no Canonicalization refusal surface.

---

## 13. Relationship to Admission and Lowering

A selected Canonicalization declaration preserves the Input coordinate surface. For each coordinate named by the
declaration, Canonicalization establishes the representative required by its direct or composed binding. Only the final
representative of that complete binding becomes the boundary meaning consumed by later inbound judgments. Intermediate
composition material does not become a separately consumable Contract stage. An unnamed coordinate remains the
presentation already established by Input.

Admission therefore judges one boundary presentation. Selected coordinates appear through their Canonicalization
representatives, while unselected coordinates appear through their Input-established presentations. When the
Canonicalization slot is omitted entirely, every coordinate reaches Admission directly from Input.

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

No public Java or Kotlin Canonicalization API shape is fixed by this ADR. Exact type names, marker forms, helper names,
initializer shapes, packages, visibility choices, and other host-language authoring realization belong to
Canonicalization Design. A Design may use an illustrative operation such as `compose(A, B, C)`, but that spelling is not
Contract law.

The exact set of V1 Built-In Laws and their ratification state are owned by ADR-0076. This ADR defines the common
Canonicalization law, the authoring semantic boundary, and the legality requirements for Definition-level composition.
A candidate name outside ADR-0076 is not a ratified Catalog promise.

A future application-defined Canonicalization facility remains deferred. It requires a separate Contract design and a
Security ADR rather than an extension point hidden inside the V1 authoring surface.

Section 9 fixes the Canonicalization-side HIR and Establishment ownership boundary: exact semantic law references,
sparse
coordinate bindings, ordered composition meaning, Definition-determining material, and Required Basis requirements must
survive frontend resolution without being reconstructed from implementation. The exact HIR storage shape, Binding
Candidate form, Canonicalization occurrence unit, Established Material, Established Semantic Protocol,
current-validity relation, and precise Definition identity consequences of wider Input-surface change remain OPEN for
the dedicated re-audit under ADR-0071, ADR-0063, and the 1D master checklist. V2 incremental treatment remains a later
compiler concern.

The exact diagnostic severity for a composition that is legal but provably redundant, fully shadowed, or otherwise
suspicious remains outside this ADR. The fixed semantic rule is that the compiler cannot silently rewrite such a
declaration into a different Contract meaning.

Any frontend realization must preserve the V1 authoring boundary described by this ADR. The IDL must continue to select
one Canonicalization declaration explicitly, and omission must remain a distinct choice. The declaration may name only
the coordinates on which Canonicalization is intended to act. Every named coordinate must resolve exactly to one direct
or composed closed Kontrakt-owned meaning. Executable application canonicalizers, inferred law selection, recursive
Contract graphs, and runtime composition remain outside the role.

## 15. Consequences

Canonicalization becomes an explicit inbound representation-control Contract rather than cleanup code.

The outside world may still choose how to express a legal Input within the presentation space that Input permits. It
does not automatically get to decide which of those differences remain meaningful after the Canonicalization boundary.

The user retains that semantic choice by selecting the Canonicalization declaration through the IDL and referring only
to reviewed exact Built-In Laws inside that declaration. A selected coordinate may use one exact law or a legal explicit
ordered composition of exact laws. V1 therefore gives the user control over where Canonicalization applies and how
admitted law meaning is ordered without handing executable authority to arbitrary callbacks.

Kontrakt can therefore verify and optimize the realization against closed law material. For a composed binding it
first verifies the whole ordered relation rather than assuming that individually valid laws compose safely. A legal
realization can then fuse or specialize physical work while preserving the same Definition meaning, representative,
refusal behavior, semantic bases, and bounds.

Omission remains explicit and cheap. If the whole slot is absent, no Canonicalization authority exists for the
Operation. Within a selected declaration, an unnamed coordinate likewise remains outside Canonicalization instead of
being routed through an invisible identity law.

The selected branch obtains one coherent boundary presentation for later inbound judgments. Every selected coordinate is
represented by the final result of its declared direct or composed Canonicalization meaning, while every unselected
coordinate retains the Input-established presentation. Intermediate composition material does not become another
judgment surface. Admission and Lowering cannot silently disagree by re-reading an earlier form of a coordinate that
Canonicalization changed.

The stricter V1 authoring boundary reduces flexibility. That restriction is deliberate. A custom Canonicalization law
can change what later responsibilities consider equivalent and can therefore alter security-sensitive decisions or
resource cost. Kontrakt should not expose that authority until the Contract surface and security model make those
consequences explicit enough for users to understand what they are declaring.

Budget and Capacity remain separate authorities instead of being folded into generic Canonicalization failure. Compiler
resource mechanisms remain implementation unless another Contract explicitly owns them.

Canonical bytes remain separate from generic deterministic compiler encoding. A direct law owns exact bytes only when
it explicitly declares that obligation. A composed binding does not acquire a new canonical-byte protocol merely because
one of its constituents owns bytes; any composition-owned byte relation must be closed explicitly before it becomes
Contract material.

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
catalog. A later authoring revision removed direct built-in law selection from the IDL. The IDL now selects one inert
Canonicalization declaration, and that declaration names only the Input coordinates on which Canonicalization is
intended to act. Application-defined executable canonicalizers remain unavailable in V1. Future custom-law support
requires a separate Contract and security design rather than a callback extension point.

The sparse-declaration revision also removed generic `ExactCanonicalization` and complete-coordinate coverage. An
unnamed coordinate now means that Canonicalization does not apply there. Adding an unrelated Input coordinate therefore
does not invalidate the declaration by itself, while a broken binding for an explicitly selected coordinate remains a
definition error.

The review also separated several responsibilities that earlier prose could blur together. Canonicalization cannot
repair material that Input never established. Later semantic consumers cannot resurrect distinctions that its selected
law erased. Canonical bytes belong only to laws that explicitly declare them, while semantic bounds remain distinct from
resource results owned by Budget, Capacity, or compiler machinery.

A final pass clarified the remaining authority boundaries. Security suspicion does not create equivalence, and a
combined built-in law owns the order of semantic operations that affects its representative. Equal representatives
establish only Canonicalization's relation rather than another authority's identity or judgment. Compiler reordering
across the stage remains legal only when the moved computation cannot distinguish members of a Canonicalization
equivalence class.

A later Catalog-boundary reallocation separated the common Canonicalization meta-law from built-in Catalog population.
ADR-0066 now owns the universal equivalence/representative closure, positive applicability rule, semantic-basis
requirement, determinism law, and Canonicalization-specific HIR / Establishment preservation boundary. ADR-0076 owns the
exact `E_L` and `C_L` of each built-in law, its exact presentation applicability, domain, determinants, Required Basis
requirements, refusal semantics, Catalog relations, ratification, evolution, and initial population.

A later composition review reopened only the Definition-level use of already-admitted Built-In Laws. V1 now permits one
selected coordinate to declare either one exact Built-In Law or one finite explicit ordered composition of exact
Built-In Laws. This does not create user-defined executable canonicalizers, child 1D Contracts, recursive Contract
composition, or automatic Catalog membership. The compiler must establish the complete composed Canonicalization law
before authority begins, preserve declared order as Definition meaning, reject semantic cycles and other illegal
compositions, and never silently repair the declaration by changing its selected laws.

The same review removed concrete public Java/Kotlin Canonicalization API realization from this ADR. The ADR now owns the
authoring semantic contract only. Exact public symbols, helper spellings, carrier forms, and other host-language API
choices move to Canonicalization Design. Non-normative notation such as `compose(A, B, C)` may still be used here only
to
explain the ordered semantic relation.

ADR-0048 remains the owner of the shared optional-Canonicalization branch and final inbound composition. The remaining
Canonicalization-specific HIR and Establishment questions are the OPEN items recorded in Sections 9 and 14 of this ADR.