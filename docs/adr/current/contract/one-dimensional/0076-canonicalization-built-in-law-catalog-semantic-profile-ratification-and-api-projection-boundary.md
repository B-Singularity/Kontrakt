# ADR-0076: Canonicalization Built-In Law Catalog, Semantic Profile Ratification, and API Projection Boundary

## Status

Proposed

## Date

2026-10-02

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0046: IDL-First Interface Contract Frontend and 1D Contract Catalog
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry
- ADR-0051: Budget Contract
- ADR-0052: Capacity Contract
- ADR-0053: Version Contract
- ADR-0055: Whole-Machine Pipeline Composition and Contract Concurrency
- ADR-0057: Failure Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0064: Input Contract
- ADR-0065: Admission Contract
- ADR-0066: Canonicalization Contract, Inbound Representation Control, Stable Representative, Canonical Bytes, and
  Explicit Omission
- ADR-0067: Lowering Contract
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary
- ADR-0075: Compiler Result Ownership, Legal Consumption, Deterministic Realization, and Contract / Implementation
  Separation
- `docs/design/overview/kontrakt-compiler-total-architecture-map-design-draft.md`
- `docs/todo/roadmap/kontrakt-v2-reference-architecture-and-v1-foundations.md`
- `docs/todo/compiler-engine/kontrakt-v2-incremental-architecture-research-todo.md`
- `docs/verification/checklists/kontrakt-1d-hir-establishment-master-checklist.md`
- `docs/constitution/kontrakt-semantic-stability-and-external-consumer-self-protection-principles-constitution.md`

---

# 1. Context

ADR-0066 defines Canonicalization as an inbound Contract authority. The IDL selects one inert Canonicalization
declaration, and that declaration names only the Input coordinates on which Canonicalization applies. Each named
coordinate selects one closed Kontrakt-owned law.

The remaining question is which built-in laws Kontrakt is willing to own. That decision cannot be left to implementation
because a built-in law fixes the equivalence relation, the successful representative, and the semantic conditions under
which that representative is valid. Later judgments and compiler products may rely on those established semantics
without reconstructing their own normalization rule.

The word canonicalization is broader outside Kontrakt than the authority defined by ADR-0066. Catalog membership
therefore follows Kontrakt's semantic boundary rather than terminology used elsewhere.

The catalog must cover common representation problems without turning Canonicalization into an executable extension
point. It must also leave a path for specialized domains whose representative depends on an explicit semantic basis.
Other transformations remain separate unless they satisfy the Canonicalization law itself.

This ADR defines that catalog boundary, the initial V1 catalog, and the rules for admitting later profiles. Public API
projection and implementation Design remain separate.

---

# 2. Problem

Kontrakt must avoid both an under-specified catalog and an over-broad one.

If V1 defines only the authoring mechanism, a selected nominal law has no authoritative semantic target. The missing
meaning then propagates into HIR, Establishment, and later consumers until implementation-specific material becomes the
place where the law is reconstructed. That would move Contract meaning into compiler implementation.

The opposite failure occurs when a broad name hides several incompatible equivalence relations. A name such as
`EmailCanonicalization` can look authoritative while leaving the actual same-meaning relation to implementation choice.

Deterministic serialization creates the same risk when it is confused with semantic Canonicalization. Stable bytes may
be required for a protocol without being the representative of an inbound Contract value. Canonical-byte profiles
therefore need an owner appropriate to their actual semantic purpose.

Specialized domains add one more requirement: some representatives depend on external semantic material. Such a law
cannot inherit its meaning from whichever library, registry, or dataset is present on the current machine. Its Required
Basis must be explicit before the law becomes public Contract meaning.

This ADR therefore decides which laws satisfy ADR-0066, which are ready for V1, which require explicit Basis handling,
and which belong outside the coordinate catalog.

---

# 3. Decision Drivers

The catalog must preserve the authority boundary established by ADR-0066. A built-in law exists to establish a
representative under a declared equivalence relation; convenience is not sufficient.

A law must remain understandable without its implementation. A Java or Kotlin type is only frontend evidence for the
law, so API renaming or backend replacement cannot change semantic identity.

V1 should cover common application needs before specialized domains. A professional profile remains possible, but any
externally versioned semantic basis must be declared rather than read from ambient environment state.

The catalog must also preserve fine-grained V2 reuse. A change to one law or to the Basis that law actually consumes
must not invalidate unrelated laws merely because they share one physical catalog artifact.

Finally, a public V1 law must have a reviewable work shape on untrusted Input. Boundedness and security are
qualification conditions, not implementation afterthoughts.

---

# 4. Decision

Kontrakt will maintain a **Canonicalization Built-In Law Catalog** as Contract-owned semantic specification material.
Catalog membership is an ADR-level decision because it makes the complete semantic profile a Kontrakt obligation.

The catalog does not own host-language names or implementation algorithms. API Specification maps supported source names
to catalog laws, while Design decides how those laws are stored and executed.

```text
Catalog Law
    authoritative semantic profile
        ↓
API Specification
    host-language nominal projection
        ↓
Frontend Resolution
    exact semantic law reference
        ↓
HIR / Establishment
    Canonicalization-owned meaning
        ↓
Design / Backend
    replaceable realization
```

Source projection, implementation, and conformance evidence do not define the law. They operate on semantic meaning that
the catalog already owns.

---

# 5. Catalog Law Qualification

## 5.1. Declared Equivalence

A catalog law must state which differences in the supported Input presentation are irrelevant under that law. The
relation cannot be inferred from host equality, hashing, serialization, or another implementation convenience.

For a successful law `C`, declared-equivalent inputs must converge on the same representative.

```text
x equivalent-to y under C
    → representative(C, x) = representative(C, y)
```

Any distinction that the law does not erase remains distinct.

## 5.2. Stable Representative

The law must define one representative for each successful equivalence class, and repeated application must preserve
that representative.

```text
C(C(x)) = C(x)
```

This idempotence belongs to the semantic law. An already-canonical fast path may exploit it but does not establish it.

## 5.3. Same-Meaning Preservation

Canonicalization selects a representation inside meaning already declared equivalent. An operation that changes or
acquires meaning instead belongs to another Contract authority. Rounding or value conversion are examples of such
operations.

## 5.4. Presentation-Domain Closure

A law must state the presentation domain on which it is meaningful. The host carrier type is not enough because the same
`String` carrier can represent unrelated semantic domains.

A law may refuse legal Input that lies outside its canonicalizable domain. It may not repair material that never became
a legal Input presentation.

## 5.5. Boundedness

A V1 law must have a finite work shape that can be reasoned about before arbitrary application behavior executes. Any
bound that changes the law's legal domain or representative belongs to the semantic specification; compiler safety
limits do not.

Budget or Capacity may stop an implementation when those authorities own the crossed limit. Such a stop does not create
another representative.

## 5.6. Semantic Basis

A law that depends on external semantic data must identify the exact Basis that can change its result. Broad labels such
as a JDK version or the current provider are insufficient when the law actually depends on a narrower dataset or
registry state.

The Basis belongs to the law only when the law consumes that meaning. Its physical representation remains outside this
ADR.

## 5.7. Conformance

Every ratified law must have normative conformance material. It must demonstrate both the distinctions the law collapses
and those it preserves, including owned refusal and relevant boundary cases.

An external implementation may serve as an oracle or implementation aid. It does not become semantic authority.

---

# 6. Catalog Entry Model

A catalog entry must be complete enough that the compiler never needs to reopen the public source type to discover the
selected law's meaning.

| Catalog concern         | Required meaning                                                      |
|-------------------------|-----------------------------------------------------------------------|
| Semantic law id         | Stable reference to the law family and semantic profile               |
| Presentation domain     | Exact kind of Input presentation on which the law is defined          |
| Equivalence             | The distinctions erased by the law                                    |
| Preserved meaning       | The distinctions that remain observable after Canonicalization        |
| Representative          | The required successful representative                                |
| Canonicalizable domain  | The legal subset on which the representative is defined               |
| Refusal                 | Canonicalization-owned negative result, if one exists                 |
| Required semantic basis | External or separately established meaning that can change the result |
| Bounds                  | Semantic limits that affect legality or representative meaning        |
| Canonical bytes         | Whether exact bytes are part of this law; normally none               |
| Conformance             | Normative vectors and properties                                      |
| Security note           | Representation or complexity risks that affect safe use               |
| Profile evolution       | What change requires a new semantic profile                           |

This model is logical. It does not require one runtime object or stored record containing every field.

---

# 7. Catalog Classes

The catalog uses semantic classes to control scope. They are not a runtime enum exposed to user code.

## 7.1. Core General Law

A Core General Law solves a common application problem without a user-selected professional Basis. A law may still
depend on a ratified Kontrakt basis, as Unicode profiles do.

Core General Laws ship with the base V1 Canonicalization API.

## 7.2. Standard Profile Law

A Standard Profile Law adopts a narrowly identified public standard whose equivalence and representative are precise
enough to become one Kontrakt law. Kontrakt ratifies the exact profile and closes any meaning-determining versioned
Basis.

A Standard Profile Law may ship in V1 without belonging to the minimal base set.

## 7.3. Specialized Domain Law

A Specialized Domain Law requires domain-specific semantic material. It is not published until its complete semantic
obligations can be represented through the HIR and Establishment model.

A later module may publish such a law without changing existing base-law meaning.

## 7.4. Canonical-Byte Profile

A Canonical-Byte Profile defines normative bytes for a particular protocol purpose. It is tracked separately because
deterministic bytes and inbound semantic Canonicalization are not the same authority.

When Kontrakt supports such a profile, its owning protocol or API specification defines the semantic role of those
bytes.

---

# 8. V1 Core General Catalog

The laws in this section are the initial V1 semantic catalog if this ADR is accepted. Their names are semantic law
names; the public Java and Kotlin names belong to API Specification.

## 8.1. Unicode NFC

**Semantic law id:** `text.unicode.nfc`

The presentation domain is Unicode text represented as a finite sequence of Unicode scalar values admitted by the
selected Input surface.

The law uses Unicode Normalization Form C. Canonically equivalent text converges on the NFC representative, while
compatibility distinctions remain observable.

The law depends on the Unicode normalization specification and the ratified Unicode semantic basis. Normalization
stability protects already-assigned characters across later Unicode versions, but unassigned code points still require
an explicit basis policy because future assignment can change their normalization behavior.

The initial V1 basis is Unicode 18.0.0. The profile records that Basis instead of inheriting the host runtime's Unicode
tables. A future stabilized-string profile may add the Unicode NPSS refusal rule for unassigned code points.

Canonical bytes are not owned by this law.

## 8.2. Unicode NFD

**Semantic law id:** `text.unicode.nfd`

The law uses the same canonical equivalence relation as NFC and selects Unicode Normalization Form D as the decomposed
representative.

Its Unicode Basis and unassigned-code-point policy follow the NFC profile.

## 8.3. Unicode NFKC

**Semantic law id:** `text.unicode.nfkc`

The law applies Unicode compatibility decomposition followed by canonical composition. Selecting it therefore declares
compatibility distinctions irrelevant in addition to canonical distinctions.

NFKC is not an implementation substitute for NFC. The stronger equivalence is part of the selected Contract law, and the
Unicode Basis is meaning-determining.

## 8.4. Unicode NFKD

**Semantic law id:** `text.unicode.nfkd`

The law erases the same compatibility distinctions as NFKC and selects the compatibility-decomposed representation.

Its representative and erased distinctions are exact Contract meaning rather than a storage preference.

## 8.5. Unicode NFC Case Fold

**Semantic law id:** `text.unicode.nfc-casefold`

This law declares Unicode canonical equivalence and Unicode default caseless matching irrelevant for the selected
coordinate. Its representative follows the idempotent Unicode relation:

```text
Q(x) = NFC(toCasefold(NFD(x)))
```

Locale-sensitive lowercasing is not part of this law. Unicode case-folding and normalization data are
meaning-determining Basis material.

## 8.6. Unicode NFKC Case Fold

**Semantic law id:** `text.unicode.nfkc-casefold`

This law uses Unicode `NFKC_Casefold` semantics and therefore erases compatibility distinctions as well as caseless
distinctions. It is intended for identifier-like domains that explicitly choose that stronger equivalence rather than
unrestricted display text.

The Unicode Basis is meaning-determining. Host `lowercase()` or an arbitrary helper sequence may realize the law only if
it is proven equivalent to the selected profile.

## 8.7. ASCII Case Fold

**Semantic law id:** `text.ascii.casefold`

The presentation domain is text for which ASCII letter case is the only distinction this law may erase. `A` through `Z`
map to their lowercase ASCII representatives; every other code point is preserved.

The law has no locale behavior and requires no external semantic Basis.

## 8.8. LF Line Ending

**Semantic law id:** `text.line-ending.lf`

The law treats the supported line-ending spellings as equivalent and selects LF as their representative. CRLF and
standalone CR therefore converge to LF without changing an existing LF.

Other Unicode line separators and other whitespace are preserved. The transformation does not expand the source, trim
content, or collapse blank lines.

## 8.9. Unicode Boundary Whitespace Trim

**Semantic law id:** `text.unicode.whitespace-trim`

The law erases leading and trailing code points with the Unicode `White_Space` property under the selected Unicode
Basis. Interior whitespace remains unchanged.

The Unicode property set, not Java `trim()` or `strip()`, defines the law. Because property membership can be
version-sensitive, the Unicode Basis is meaning-determining. The law does not collapse internal whitespace or perform
line-ending normalization.

## 8.10. Decimal Numeric Value

**Semantic law id:** `number.decimal.numeric-value`

The presentation domain is a finite base-10 decimal represented by an integer coefficient and a decimal scale. Scale
differences are irrelevant when they denote the same exact decimal number.

For non-zero values, the representative removes every trailing base-10 zero from the coefficient and adjusts the scale
without changing the exact value. Zero has one representative with coefficient zero and scale zero.

```text
2.00
    → the same representative as 2

600.0
    → coefficient 6 with scale -2

-0.0
    → zero with scale 0
```

The law performs no rounding. Material that cannot be represented exactly in the supported finite domain is outside the
law rather than silently rounded.

This profile is distinct from monetary scale or market tick rules, where scale or step size may carry independent
business meaning.

## 8.11. Binary32 Canonical NaN

**Semantic law id:** `number.binary32.canonical-nan`

The presentation domain is IEEE 754 binary32 when raw NaN representation remains observable at the Input boundary. Every
NaN bit pattern maps to the ratified quiet-NaN representative, so NaN payload and signaling distinctions are erased.
Every non-NaN bit pattern is preserved, including signed zero.

The V1 representative is `0x7fc00000`, and that bit pattern is normative law material.

## 8.12. Binary64 Canonical NaN

**Semantic law id:** `number.binary64.canonical-nan`

The binary64 law follows the same rule as the binary32 profile. Every NaN representation maps to one ratified quiet NaN,
while every non-NaN bit pattern remains unchanged.

The V1 representative is `0x7ff8000000000000`, and that bit pattern is normative law material.

---

# 9. V1 Standard Profile Catalog

These profiles are supported by V1 but need not be present in the smallest base runtime module.

## 9.1. IPv6 RFC 5952 Text

**Semantic law id:** `network.ipv6.rfc5952`

The presentation domain is a legal textual IPv6 address without an external zone identifier. Alternate legal spellings
are equivalent when they denote the same IPv6 address. RFC 5952 defines the representative, including hexadecimal case,
leading-zero suppression, and zero-compression choice.

Zone identifiers remain outside this profile because they belong to an environment-sensitive domain. The law performs no
DNS lookup and acquires no network authority.

## 9.2. BCP 47 Language Tag

**Semantic law id:** `identifier.bcp47.rfc5646`

The presentation domain is a BCP 47 language tag that is well formed under the supported profile. RFC 5646 defines the
representative, and IANA `Preferred-Value` data becomes meaning-determining where the profile consumes it.

The initial V1 Basis is the IANA Language Subtag Registry snapshot dated 2026-09-17. The profile cannot read a mutable
current registry as semantic authority.

Extension-specific canonicalization is supported only when the selected profile has closed the relevant extension
semantics. Unknown or unsupported extension semantics do not acquire an implementation-defined representative.

## 9.3. UUID Lowercase Text

**Semantic law id:** `identifier.uuid.rfc9562-lowercase-text`

The presentation domain is the standard textual UUID form accepted by the profile. Hexadecimal letter case is declared
irrelevant and lowercase text is the representative.

The law changes neither UUID bits nor version or variant meaning. A structured UUID value that no longer contains
textual case does not require this profile.

---

# 10. Deferred General Profiles

The following profiles are plausible Canonicalization laws but are not part of the initial V1 catalog. A later amendment
may ratify one after its remaining semantic or security question is closed.

## 10.1. Unicode Stabilized Normalization

Unicode Stabilized Strings reject code points that are unassigned in the selected Unicode version so that a successful
normalization result remains stable across version evolution.

Stabilized NFC and NFKC remain deferred until their refusal relation and Basis interaction are closed with the
Canonicalization HIR and Establishment re-audit.

## 10.2. Unicode Whitespace Collapse

A whitespace-collapse law can map each maximal Unicode whitespace run to one fixed representative. It remains deferred
because the boundary-whitespace policy must be selected before a public profile exists.

## 10.3. Signed-Zero Collapse

A binary floating-point law may declare positive and negative zero equivalent, but IEEE 754 behavior can later observe
the sign. V1 therefore defers this law until those semantic consequences are reviewed explicitly.

## 10.4. RFC 3986 URI Syntax Profile

RFC 3986 defines syntax-level normalization, but URI equivalence depends on purpose and can become scheme-specific. V1
therefore publishes no generic `UriCanonicalization`.

A later law must identify the exact syntax-level or scheme-specific equivalence it owns.

## 10.5. IDNA and PRECIS Profiles

IDNA and PRECIS combine representative formation with legality rules. A future Kontrakt profile must split those
responsibilities according to Kontrakt authority rather than import an external pipeline as one opaque law.
Representative formation may belong to Canonicalization while Input or Admission owns the corresponding legality rule.

## 10.6. Instant-Preserving Temporal Profiles

A temporal law may declare two offset date-time presentations equivalent when they denote the same instant and select a
UTC representative. Such a law is valid only when the original civil-time context is explicitly irrelevant.

V1 therefore publishes no generic `DateTimeCanonicalization`.

---

# 11. Specialized Domain Research Registry

These profiles are outside the V1 base catalog. They remain relevant because the common architecture should be able to
represent them without a semantic rewrite.

## 11.1. UCUM Quantity

A future UCUM quantity law could treat different magnitude-and-unit presentations as equivalent when they denote the
same physical quantity. Magnitude and unit must be canonicalized as one aggregate meaning rather than independently.

The profile therefore requires a closed quantity domain and ratified UCUM Basis before admission.

## 11.2. GA4GH Variation Representation

GA4GH VRS can normalize ambiguous genomic variation against exact reference sequence meaning. Because the result may
depend on that Basis and some normalization paths can alter object form, the profile remains deferred until Basis and
shape obligations are explicit.

## 11.3. RDF Dataset Canonicalization

RDFC-1.0 assigns deterministic identifiers to blank nodes and produces a canonicalized RDF dataset. A future profile
must own graph equivalence and its representative while keeping operational resource stops under Budget or Capacity.

The profile also requires an explicit boundedness design because pathological graph symmetry can amplify work.

## 11.4. Chemical Graph Canonicalization

Chemical normalization and canonical graph labeling are distinct responsibilities. A future chemistry profile may own
canonical labeling only after the chemical domain required by that law has already been established.

Serialization remains a separate concern.

## 11.5. Avro Parsing Canonical Form

Avro Parsing Canonical Form defines schema equivalence for a precise parsing purpose. A future schema module may adopt
that exact law, but it does not justify a generic `SchemaCanonicalization`.

## 11.6. Financial and Securities Domains

Financial representation concerns do not form one generic money or price law. `number.decimal.numeric-value` covers
representation-only decimal equivalence, while financial rounding or conversion changes or derives economic meaning and
remains outside Canonicalization.

A future securities module may still ratify a narrow identifier or quantity profile when an external standard defines
one exact same-meaning relation. Each such profile requires its own law and Basis review.

---

# 12. Profiles Explicitly Outside the Coordinate Catalog

## 12.1. Deterministic and Canonical Byte Encodings

Canonical-byte standards such as JCS, deterministic CBOR, DER, and signature-specific canonical constructions define
exact representation for protocol purposes. They do not enter the inbound coordinate catalog merely because they use the
word canonicalization.

When Kontrakt supports one, its owning protocol must define what the bytes mean. The same rule applies to other
canonical wire, signature, or artifact representations.

## 12.2. Protobuf Deterministic Serialization

Protocol Buffers distinguishes deterministic serialization from canonical serialization. Unknown fields and deliberately
unspecified serialization choices prevent generic protobuf bytes from defining universal semantic identity. Kontrakt
therefore cannot derive Contract equality or stable identity from deterministic protobuf bytes unless a narrower profile
closes those ambiguities.

## 12.3. Compiler Artifact Canonicalization

Canonicalization used to remove build-representation noise belongs to compiler artifact formation. It may support
reproducibility or supply-chain verification, but it does not create an inbound Canonicalization Contract over user
Input.

---

# 13. Generic Laws That V1 Will Not Publish

V1 will not publish a generic law when its name hides unresolved equivalence. The rule applies regardless of domain: the
exact same-meaning relation must be closed before a built-in profile exists.

The reason is semantic, not cosmetic. A generic email law would already be ambiguous because mailbox local-part and
domain case do not follow the same rule. URI and filesystem-path profiles have the same problem for different reasons. A
broad source name would hide those determinants rather than establish them.

---

# 14. No Identity or Preserve-Everything Laws

The catalog does not contain `ExactCanonicalization` or another profile whose only effect is to preserve every
Input-established representation distinction. Omission already expresses that Canonicalization does not apply.

Earlier preserve-everything candidates therefore do not become V1 laws. A particular already-canonical input may still
pass through a selected law without physical change; that is different from publishing universal identity as a
Canonicalization authority.

---

# 15. Ordering, Validation, and Refusal Are Not Automatically Canonicalization

Ordering alone does not establish a representative, so an IEEE total-order relation is not a Canonicalization law by
itself. Likewise, a reject-only rule such as `RejectNaN` or `RejectNonFinite` does not become Canonicalization merely
because it constrains the same domain. The owning 1D Contract must decide that legality.

Collection ordering follows the same distinction. If source order is Contract-visible and a selected law declares it
irrelevant, deterministic representative selection may be Canonicalization. If the semantic domain was already
unordered, sorting physical storage is compiler representation preparation.

The catalog never infers semantic order from JVM collection iteration.

---

# 16. Aggregate Law Boundary

A catalog law may apply to one coordinate whose presentation domain is a closed aggregate. The complete aggregate
profile remains one law bound to that coordinate; its children do not become independent 1D Contracts.

V1 does not expose arbitrary recursive law composition. A built-in aggregate profile may be added only after the
complete aggregate equivalence and representative are defined, preventing the catalog from becoming a normalization
programming language.

---

# 17. API Projection Boundary

Catalog Law identity and host API identity are separate.

```text
public nominal type
    ↓ exact frontend mapping
Catalog Law Reference
    ↓
Canonicalization Definition Candidate meaning
```

API Specification may choose names such as `UnicodeNfc` or `DecimalNumericValue`, but this ADR does not fix them. An API
rename may change source compatibility without changing the Catalog Law. Conversely, a stable source name cannot hide a
changed semantic law.

The API Specification must preserve the authoring law already fixed by ADR-0066:

- the IDL selects one inert Canonicalization declaration;
- the IDL does not select one built-in law directly;
- the declaration names only the Input coordinates to which Canonicalization applies;
- each named coordinate selects one exact Kontrakt-owned law;
- an unnamed coordinate remains outside Canonicalization;
- the declaration contains no executable canonicalizer.

The public type is evidence for one Catalog Law, not a runtime strategy object.

---

# 18. HIR and Establishment Integration

This ADR does not complete the Canonicalization HIR and Establishment re-audit left open by ADR-0066, but it constrains
that work.

After frontend resolution, a Canonicalization Definition Candidate must refer to each selected Catalog Law by semantic
reference rather than by Java or Kotlin class name. Coordinate bindings remain part of one Canonicalization Definition
Candidate; referenced Catalog Laws do not become child 1D Definitions merely because they have stable identities.

If a Catalog Law has Required Basis, the candidate must preserve that requirement wherever it is definition-determining.
Actual Basis Binding and Applicability remain owned by ADR-0063.

The re-audit must still decide the exact Canonicalization occurrence unit. This ADR does not choose whether occurrence
is per coordinate, per declaration application, or another semantic unit.

---

# 19. External Semantic Basis

A catalog law may depend on released external semantic material when that material is necessary to determine its
representative. The law must identify enough meaning that two implementations can determine whether they use the same
Basis. A broad platform or registry label is not sufficient by itself.

The initial catalog uses three Basis patterns.

### Fixed law with no external table

`text.ascii.casefold`, `text.line-ending.lf`, and the binary NaN laws are fully determined by their ratified profiles
and fixed numeric standards.

### Ratified versioned dataset

Unicode profiles consume versioned Unicode semantic data, so the selected Unicode Basis must be explicit enough for
implementation, conformance, and reuse to agree.

### Registry-backed standard profile

`identifier.bcp47.rfc5646` consumes registry semantics when registry data affects the representative. Its registry Basis
therefore remains closed instead of being discovered from a mutable environment.

A future domain law may bind a per-application Basis, such as a reference sequence, once the authoring and Establishment
path for that relation is closed.

---

# 20. Versioning and Catalog Evolution

Adding a law does not alter existing law meaning. A change that can alter legal results through the equivalence
relation, representative, refusal law, or meaning-determining Basis requires a new semantic profile.

Editorial clarification and stronger verification do not create a new profile when normative meaning remains unchanged.
API aliases likewise remain an API compatibility concern rather than new semantic laws.

An implementation may later stop supporting an old profile, but support policy cannot retroactively redefine Contract
material that already referenced it.

---

# 21. V2 Incremental and Reuse Boundary

The catalog must not become one monolithic semantic dependency. A Definition that selects `text.unicode.nfc` depends on
that law and its required Basis, not on unrelated laws stored beside it.

V1 therefore preserves exact per-law references and Basis relations. V2 may add a different reuse mechanism over those
semantics without making that mechanism part of identity.

```text
Canonicalization Definition
    ↓ selected law reference
Catalog Law
    ↓ only when required
Semantic Basis
```

The architecture must not collapse this into dependency on a whole catalog or whole platform version.

---

# 22. Compiler Product and Protocol Boundary

Catalog Law is the semantic source even when the compiler lowers it into generated or precomputed realization material.
Such products are compiler-owned and are consumed across subsystem boundaries under ADR-0075.

Optimization may specialize or fuse implementation work only when the same Contract-visible result is preserved.
Matching outputs on limited observations do not permit one Catalog Law to be substituted for another.

---

# 23. Security and Adversarial Input

Built-in Canonicalization operates on outside-controlled material, so each public law requires an adversarial work-shape
review. The review must cover the input structures that can amplify the selected algorithm and any malformed host
representation that can escape the declared presentation domain.

More complex profiles may require additional operational limits, but those limits remain owned by Budget or Capacity
unless the Canonicalization law itself gives them semantic meaning.

Security suspicion does not create equivalence. UTS #39 confusable skeletons, for example, are comparison evidence
rather than a normalized identifier representative, so they remain outside Canonicalization unless a separate Contract
law explicitly owns that relation.

---

# 24. Conformance and QA

Each V1 Catalog Law must have a versioned normative vector set before its public API projection is stable. The vectors
test semantic law, including preserved distinctions and owned refusal, rather than merely reproducing one
implementation.

Unicode and standard-backed profiles should derive vectors from the applicable normative material and add Kontrakt
boundary cases. Decimal tests must cover the finite-domain boundary while proving exact-value convergence without
collapsing distinct values. Floating-point tests must prove canonical NaN handling while preserving every non-NaN
representation.

Property testing must check idempotence and equivalence convergence over generated legal domains. Fuzzing should
exercise frontend law resolution and realization independently. Differential testing may compare an external
implementation, but disagreement is evidence to investigate rather than authority to copy.

---

# 25. Initial V1 Catalog Summary

If this ADR is accepted, the initial catalog is:

| Class            | Semantic law id                          | V1 status                           |
|------------------|------------------------------------------|-------------------------------------|
| Core             | `text.unicode.nfc`                       | Ratified                            |
| Core             | `text.unicode.nfd`                       | Ratified                            |
| Core             | `text.unicode.nfkc`                      | Ratified                            |
| Core             | `text.unicode.nfkd`                      | Ratified                            |
| Core             | `text.unicode.nfc-casefold`              | Ratified                            |
| Core             | `text.unicode.nfkc-casefold`             | Ratified                            |
| Core             | `text.ascii.casefold`                    | Ratified                            |
| Core             | `text.line-ending.lf`                    | Ratified                            |
| Core             | `text.unicode.whitespace-trim`           | Ratified                            |
| Core             | `number.decimal.numeric-value`           | Ratified                            |
| Core             | `number.binary32.canonical-nan`          | Ratified                            |
| Core             | `number.binary64.canonical-nan`          | Ratified                            |
| Standard Profile | `network.ipv6.rfc5952`                   | Ratified                            |
| Standard Profile | `identifier.bcp47.rfc5646`               | Ratified with closed registry basis |
| Standard Profile | `identifier.uuid.rfc9562-lowercase-text` | Ratified                            |

The first deferred group is:

| Profile family                       | Reason for deferral                                                 |
|--------------------------------------|---------------------------------------------------------------------|
| Stabilized Unicode normalization     | Refusal and Basis interaction should close with HIR / Establishment |
| Unicode internal whitespace collapse | Useful variants need evidence before public API expansion           |
| Signed-zero collapse                 | Floating-point semantic consequences need explicit review           |
| RFC 3986 URI profile                 | URI equivalence is purpose and scheme sensitive                     |
| IDNA / PRECIS                        | Mapping and legality cross Kontrakt authority boundaries            |
| Temporal instant profiles            | Offset and zone meaning are domain-sensitive                        |
| UCUM quantity                        | Aggregate domain and professional semantic basis                    |
| GA4GH VRS                            | Reference-sequence Basis and possible shape change                  |
| RDFC-1.0                             | Graph domain and adversarial complexity                             |
| Chemical canonical labeling          | Requires a closed chemical normalization domain                     |
| Avro Parsing Canonical Form          | Specialized schema equivalence                                      |

---

# 26. Decisions Against Earlier Candidate Material

Earlier drafts used a broader exploratory list. This ADR narrows that material according to the current Canonicalization
law.

Earlier candidates are retained only when they establish a Canonicalization representative. Preserve-everything profiles
collapse into omission, ordering-only profiles remain ordering laws, and reject-only profiles remain with the authority
that owns domain legality. Collection and binary candidates stay deferred until their semantic equivalence is closed
independently of JVM representation.

---

# 27. Migration and Supersession

This ADR does not supersede the Canonicalization authority defined by ADR-0066. It supplies the catalog that ADR-0066
intentionally left open.

When this ADR is accepted, older candidate-catalog material in ADR-0048 migration history and earlier ADR-0066 revisions
becomes historical exploration rather than active V1 direction.

The active authoring relation remains:

```text
IDL selects one Canonicalization declaration
    ↓
declaration names selected Input coordinates
    ↓
each selected coordinate references one ratified Catalog Law
```

An unselected coordinate remains outside Canonicalization. The IDL does not select a built-in law directly, no
`ExactCanonicalization` filler is inserted, and the declaration contains no executable user canonicalizer.

Project documentation should migrate to one active statement of this relation.

---

# 28. Consequences

The V1 frontend gains a finite semantic catalog. Resolution produces one Catalog Law reference that HIR can preserve and
Establishment can later use without reopening authoring syntax. PBT and conformance tooling can consume that same law
instead of inventing separate normalization assumptions.

The backend retains implementation freedom because the representative is fixed independently of realization. The base
API also remains smaller than the research inventory: specialized profiles enter only after their Basis and authority
boundaries are explicit, and broad convenience names cannot become Contract law merely because a library exposes them.

The tradeoff is that V1 does not expose arbitrary user-composed normalization pipelines. One coordinate selects one
closed law. A combination may later become a reviewed Catalog Law, while general custom-law support remains a separate
extension problem that must not turn callbacks into Contract authority.

---

# 29. Open Work After This ADR

The next semantic step is the Canonicalization HIR and Establishment re-audit in ADR-0066. It must close the Definition
Candidate and Required Basis, then carry that meaning through Applicability, occurrence, and the Established Material
exposed by the semantic protocol.

After that, a V1 API specification can close the host-language projection without changing Catalog Law identity. Design
can then choose physical storage and generated realization while preserving the same semantics.

Canonical-byte protocols remain a separate decision. Specialized domain modules should proceed only after their Basis
and aggregate semantics fit the common HIR and Establishment model.