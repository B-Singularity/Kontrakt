# ADR-0066: Canonicalization Contract, Stable Representative, Canonical Bytes, and Explicit Omission

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

Admission only decides that a boundary presentation may continue.

Outside presentation can still contain distinctions that the Contract declares equivalent or representation noise that
must not define stable machine identity.

Canonicalization exists to establish one stable system-owned representative when equivalence has been declared.

Canonicalization is not cleanup, repair, parsing, arbitrary transformation, or business computation.

It does not change the Contract-visible coordinate shape of the selected Input.

It operates over admitted presentation and may establish a stable same-shape representative together with exact
canonical bytes.

When the `canonicalization` slot is selected, its law receives the same immutable Contract-visible presentation after
Admission and deterministically produces the one representative permitted by that law. Successful production grants
stable representative authority immediately. There is no user-authored post-canonical validation Contract. If a
successful Kontrakt realization produces noncanonical material, Kontrakt is defective.

Canonicalization is optional.

Its omission is meaningful and must remain distinct from selecting an exact-preservation law.

---

## 2. Problem

If external presentation defines internal identity directly, the same declared meaning may split across host
representation differences.

If Canonicalization is implicit, the machine hides a meaning-changing stage.

If users can provide arbitrary canonicalizer callbacks, executable source behavior becomes Contract authority.

If canonical work is unbounded, a Canonicalization Contract can become permission for unlimited computation and memory.

If host equality, hashing, iteration order, locale, timezone, Unicode implementation, comparator behavior, object
identity, or platform defaults define canonical material, determinism becomes backend-dependent.

Canonicalization therefore needs closed versioned law material, explicit selection, bounded production, exact byte
meaning, and conformance material.

---

## 3. Decision Drivers

Equivalent meaning must produce one stable representative under one declared law.

Distinct meaning must not be collapsed without declaration.

Canonicalization selection must be explicit.

Omission must create no hidden `ExactCanonicalization`, callback, proxy, interceptor, or replacement stage.

Users must not author executable canonicalizer behavior as Contract authority.

A selected law must completely cover the selected Input coordinate surface.

Canonical output must be deterministic, bounded, versioned, and independently testable.

Budget and Capacity remain separate authorities when their walls are crossed.

Generated canonicalization machinery may be optimized only when representative meaning, exact bytes, result,
attribution, bounds, and State-visible behavior are preserved.

---

## 4. Decision

### 4.1. Optional Contract

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

The admitted Input presentation is passed unchanged to the selected Lowering relation, its compiler-derived plan, and
the explicitly bound realization behind the generated port.

Deterministic encoding of Kontrakt-owned material remains mandatory, but that protocol is not an implicit
Canonicalization Contract. Fixed scalar encodings, coordinate order, presence markers, framing, schema identity, and
Version material may still be required for identity, ordering, hashing, caching, Publication, or verification. They
determine how existing material is represented inside Kontrakt; they do not collapse semantic distinctions or produce a
new representative value.

Selecting `ExactCanonicalization` is different.

When selected, it is an explicit Canonicalization Contract with its own identity, conformance, refusal domain,
representative law, and canonical-material production.

### 4.2. Same-Shape Representative

Canonicalization does not perform shape-changing Operation input formation.

The selected Input coordinate surface remains the Canonicalization surface.

A selected law may preserve or collapse distinctions only as the declared law permits.

Canonicalization may replace a coordinate value with an equivalent representative, impose deterministic order where the
selected Contract declares source order irrelevant, and collapse only distinctions that the selected law explicitly
names as irrelevant. It does not add, remove, rename, retype, parse, flatten, project, or otherwise remap
Contract-visible coordinates. Those are shape-changing obligations and belong to Lowering or another explicit
transformation boundary.

Lowering remains responsible for the later relation from boundary coordinates to Operation-parameter Fact coordinates.

### 4.3. Canonical Bytes

A selected Canonicalization law defines exact canonical bytes where canonical byte material is part of the law.

Canonical bytes are not an arbitrary serialization result.

Their protocol version, schema identity, ordering, distinction law, and bounds are Contract material.

Generated byte emitters remain implementation.

---

## 5. V1 Authoring Boundary

V1 supports two selected source forms.

The first is one complete flat Kontrakt-owned built-in Canonicalization symbol.

The second is one uninstantiable Java or Kotlin coordinate-law signature declaration whose parameter names identify
selected Input coordinates and whose parameter types identify exact Kontrakt-owned nominal canonical type symbols.

The user does not declare a canonical output DTO, encoder object, transformation method, callback, descriptor instance,
IR object, or runtime law value.

The independently resolved `canonicalization` slot names exactly one Java or Kotlin symbol as external source evidence
for one flat Canonicalization Contract. A source-layout label such as `flow` grants no meaning. The selected symbol,
file, package, method name, annotation, type relation, and source location do not own Canonicalization authority.

The slot supplies the role. Frontend refinement decides whether the selected source can be ratified. The resulting
Kontrakt-owned material supplies authority.

The two selected source forms are:

```text
Direct law selection:
    one complete Kontrakt-provided built-in Canonicalization symbol

Coordinate-law type declaration:
    one inert Java or Kotlin signature declaration whose parameter names bind every
    directly named Input coordinate and whose parameter types are exact Kontrakt-owned
    nominal canonical type symbols
```

When present, the manifest names one declaration only.

```text
canonicalization  UnicodeNfcCaseFoldCanonicalization
```

or:

```text
canonicalization  CustomerCanonicalization
```

When absent, the manifest simply continues from Admission to Lowering.

```text
input          CustomerInput
admission      CustomerAdmission
lowering       CustomerLowering
```

The omission says nothing about values that may have existed before `CustomerInput` was submitted. Kontrakt observes
only the submitted Input presentation.

### 5.1. Built-In Law

One built-in symbol names one flat closed versioned Canonicalization law.

A built-in law does not inherit, override, recursively compose, or acquire meaning from another built-in law.

Implementation reuse is allowed behind the Contract boundary. It must not appear as Contract inheritance or composition.

Representative built-in families may include:

```text
ExactCanonicalization
RecursiveExactCanonicalization

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

The list is illustrative.

A law must not be published merely because a host library offers a convenient operation.

Its semantics, bounds, byte law, refusal domain, security properties, and conformance material must be complete first.

### 5.2. Coordinate-Law Nominal-Type Declaration

A coordinate-law declaration is not a canonicalizer implementation.

It is an uninstantiable frontend signature.

The binding law is:

```text
selected Input coordinate name
+ selected Input coordinate sort
+ one exact Kontrakt-owned nominal canonical type symbol
= one coordinate law inside one flat Canonicalization Contract
```

Matching occurs only within the Operation that independently selected the Input and Canonicalization declarations.

Resolution uses exact source symbols, parameter names, and exact nominal parameter types at definition time.

It is not runtime string lookup, reflection, annotation scan, assignability, package convention, or type-wide Contract
inference.

The declaration contains no law value, enum constant, singleton instance, factory call, constructor call, method body,
callback, property initializer, or descriptor object.

The public authoring vocabulary consists only of nominal type names supplied by Kontrakt. A public canonical type is a
closed name rather than a usable runtime abstraction. It has no accessible constructor, factory, singleton value,
method, callback, mutable state, extension point, or user-implementable interface. It is final and cannot be subclassed
or implemented by application code.

The frontend accepts the exact fully qualified Kontrakt-owned symbol. A matching simple name, user-defined imitation,
subtype, alias, or dynamically registered replacement is not sufficient.

The source form is conceptually equivalent to the following compilable Kotlin declaration.

```kotlin
// Kontrakt-provided authoring API. Application code imports these names;
// it does not define, instantiate, implement, or extend them.
class ExactText private constructor()
class UnicodeNfcCaseFold private constructor()
class AsciiUppercase private constructor()

data class CustomerInput(
    val customerId: String,
    val name: String,
    val regionCode: String,
)

// User-authored source declaration. It is never instantiated.
class CustomerCanonicalization private constructor(
    customerId: ExactText,
    name: UnicodeNfcCaseFold,
    regionCode: AsciiUppercase,
)
```

The Kontrakt-provided declarations exist in the real authoring API rather than in user source. They are shown only to
make the example compiler-complete. The user writes the Input and the selected coordinate-law declaration.

The private constructor prevents formation of a `CustomerCanonicalization` object. Its parameters are not properties.
Only their names and exact nominal types are declaration evidence. The frontend resolves and erases the declaration
class, inaccessible constructor, parameter symbols, generic signatures, and every referenced host type before authority
begins.

The coordinate-law signature follows the already-ratified flat Input presentation. Each constructor parameter binds one
directly named Input coordinate to one exact nominal canonical type. The signature does not create values, runtime
descriptors, nested Canonicalization Contracts, parent-child authority, inheritance, recursive Contract composition, or
a second presentation shape.

V1 rejects declaration properties, `val` or `var` law bindings, enum law categories, enum constants, law singleton
objects, public constructors, factory calls, arbitrary generic types, executable initializers, methods, callbacks,
lambdas, property references, custom comparators, user-defined equality or ordering, inheritance, interface-based role
acquisition, annotations on the Input DTO, mutable fields, lazy or delegated values, captured dependencies, and values
acquired from runtime execution.

A canonical type is accepted only when its complete semantics, Version, applicable sorts, preserved and collapsed
distinctions, representative law, bounded-work law, canonical-byte law, refusal domain, and conformance material are
known before ContractImage publication. V1 exposes no application-defined canonical-type extension point.

For every public canonical type, the Kontrakt API specification and user documentation must state at least:

```text
supported Input sort and direct-coordinate position
preserved and collapsed distinctions
unique representative law
semantic profile and version
null, absence, finite-alternative, ordering, and duplicate behavior where applicable
work, source-size, expansion, and output bounds
canonical byte law and protocol version
refusal domain and cross-contract stop attribution
normative examples and conformance-vector reference
```

The type name and documentation are public projections of the same versioned Kontrakt-owned canonical law material.
Documentation explains the law but does not replace that material as authority.

V1 does not require or permit a second user-authored canonical output presentation. The Contract-visible shape remains
the Input shape. Representative values may change under the selected equivalence law, while Kontrakt-owned physical
material may use another frozen layout or canonical byte encoding. A different user-visible output shape belongs to
Lowering.

### 5.3. Complete Coordinate Coverage

Every Contract-visible Input coordinate must appear exactly once.

A missing coordinate, duplicate coordinate, unknown coordinate, renamed coordinate, or incompatible canonical type
rejects the Contract definition.

Adding, removing, renaming, reordering, or retyping an Input coordinate invalidates the old binding and requires
re-ratification.

No undeclared default applies to a new coordinate.

V1 uses exact nominal canonical type selection.

It does not infer Canonicalization from shape-directed generic or nested-signature structure.

---

## 6. Canonical-Type Applicability

Every public nominal canonical type must resolve to enough ratified law material to determine its meaning before
ContractImage publication.

That material must cover the supported source sort, preserved and collapsed distinctions, representative law, absence
and finite-choice behavior where applicable, numeric or textual semantics where applicable, bounds, canonical-byte
schema, refusal domain, attribution, and normative conformance material.

Unknown, application-defined, aliased, imitated, dynamically registered, or incompletely specified canonical type
symbols are rejected.

Executable law values, callbacks, lambdas, helper execution, virtual dispatch, inheritance-dependent behavior, runtime
subtype selection, mutable state, lazy observation, dependency injection, repository access, environment access, time,
randomness, synchronization, reflection, object identity, or runtime reference traversal are not Canonicalization
authority.

Host default locale, timezone, charset, collation, normalization table, path behavior, hash-table order, acquisition
order, or unstable sorting tie-break must not silently define canonical meaning.

Shape-changing output, parsing, projection, flattening, business derivation, or arbitrary repair is outside
Canonicalization.

---

## 7. Finite-Work Law

Canonicalization is a producer Contract. It is not permission for unbounded work.

Every accepted law must establish finite source-size, depth, cardinality where applicable, intermediate-storage,
output-size, and work bounds before publication.

General imperative iteration and recursive object-graph traversal are forbidden as user-defined Canonicalization
behavior.

Where a closed built-in law operates on bounded structured material, its traversal and aggregation semantics must
already be part of the ratified law.

A valid implementation may use canonical child bytes, domain-separated hashes, exact byte comparison, and declared
duplicate law to realize deterministic ordering when that strategy is compatible with the selected law.

That strategy is implementation. It is not frontend syntax or additional Contract meaning.

Capacity and Budget own their declared walls.

Exceeding those walls produces the owning Capacity or Budget result rather than a generic Canonicalization refusal.

---

## 8. Canonicalizable-Domain Condition

A source is not refused merely because it is not already canonical. Noncanonical spelling, order, scale, case,
normalization form, or other declared drift is ordinary Input to Canonicalization.

A Canonicalization refusal occurs only when the source lies outside the selected canonicalizable domain, the selected
law cannot define one unique representative, an operation is undefined under that law, or required semantic material is
unavailable. Capacity and Budget stops remain separate.

```text
noncanonical but canonicalizable source:
    produce the representative

source outside the selected canonicalizable domain:
    Canonicalization refusal

successful result that is not canonical:
    Kontrakt defect
```

There is no `isCanonical`, `validateCanonical`, or user-supplied verifier step after successful production.

---

## 9. Definition-Time Processing

The Canonicalization definition path is:

```text
resolve the exact slot-selected built-in symbol or coordinate-law declaration
-> acquire the selected source through the existing frozen frontend machinery
-> determine direct-law or coordinate-law nominal-type source form
-> verify one flat selectable Contract and an uninstantiable signature declaration when applicable
-> bind every declared parameter name to exactly one ratified Input coordinate
-> verify complete coordinate coverage, declared order, and sort compatibility
-> resolve every exact nominal canonical type to pinned semantic material
-> reject law values, constructors, factories, execution, environment, dispatch, State, effect, imitation, and dynamic-registration paths
-> ratify preserved and collapsed distinctions for every coordinate and the complete presentation
-> prove declared work, source-size, expansion, intermediate-storage, and output bounds
-> derive the canonical byte schema and protocol version
-> derive stable Contract identity and ContractImage material
-> generate the deterministic canonicalizer and byte emitter
```

Runtime performs no declaration lookup, reflection, member discovery, method dispatch, operator resolution, locale or
Unicode-table selection, comparator acquisition, hash-policy selection, or byte-schema construction. It executes only
the generated realization over fixed ratified coordinates and declared cross-Contract bounds.

## 10. Identity and Conformance

Canonicalization identity is derived from ratified meaning.

It is not derived from the selected source class name alone.

Identity material includes the law kind, semantic profile version, source presentation identity, preserved and collapsed
distinctions, coordinate-law bindings, representative-law versions, applicable scalar-law meaning, canonicalizable
domain, refusal law, work and output bound law, canonical byte protocol version and schema identity.

A frontend change that changes accepted meaning or generated canonical material changes Contract identity.

Every built-in law must have a versioned normative conformance-vector set and a separate implementation-verification
suite.

Changing semantic meaning or a normative expected result changes the law identity.

Adding attack cases, property tests, differential tests, or implementation verification coverage without changing the
law does not change Contract meaning.

---

## 11. Determinism Law

The determinism law is:

```text
same ratified ContractImage
+ same immutable admitted Input presentation
+ same applicable Budget and Capacity world
= same canonical representative
+ same canonical bytes
+ same Canonicalization outcome
+ same Contract-owned attribution
```

Generated canonicalizers and byte emitters are implementation-axis machinery.

They may be specialized, fused, vectorized, allocation-disciplined, or replaced only when the representative, exact
bytes, outcome, attribution, work walls, and State-visible behavior remain identical.

---

## 12. Refusal Boundary

Canonicalization receives only material that Admission allowed to continue.

It refuses only when the selected Canonicalization law cannot establish the required unique bounded representative
within its declared domain.

A Budget or Capacity stop observed during canonical production remains a Budget or Capacity result.

When Canonicalization is omitted, there is no Canonicalization refusal surface.

Canonicalization failure must not be used to hide Input refusal, Admission rejection, Lowering refusal, or cross-cutting
resource results.

---

## 13. Relationship to Lowering

A selected Canonicalization law preserves the Input coordinate surface.

Its stable representative values become Lowering source values.

Canonicalization does not define Operation parameter targets and does not create Fact meaning.

Lowering owns the explicit source-to-target relation and the shape-changing representation formation boundary.

When Canonicalization is omitted, Lowering receives the admitted Input coordinates unchanged.

---

## 14. Open in This Section

The final token spelling and exact public nominal canonical type names may change.

The exact Java or Kotlin carrier syntax for the uninstantiable coordinate-law declaration may change.

Those frontend changes must preserve the fixed V1 authoring boundary: explicit optional slot, complete flat selected
law, exact nominal type resolution, complete coordinate coverage, no implicit Exact law, no executable user
canonicalizer, and no shape-directed generic or nested-signature inference.

---

## 15. Consequences

Canonicalization becomes a declared equivalence and stable-representation Contract rather than cleanup code.

Omission is explicit and cheap.

Selected laws can support aggressive generated optimization because their meaning, bytes, bounds, and conformance are
closed.

The price is a deliberately strict authoring boundary. Application-defined canonicalizer behavior cannot silently enter
Contract authority.

Budget and Capacity remain honest separate limits instead of being folded into generic canonicalization failure.

---

## 16. Migration History

This ADR was extracted mechanically from the Canonicalization-owned material of ADR-0048.

The extraction itself does not change the accepted Canonicalization Contract semantics.

ADR-0048 remains the owner of the shared optional-Canonicalization branch and its handoff to Lowering.