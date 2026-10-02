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

ADR-0066 defines Canonicalization as an inbound Contract authority. It allows a user to select one inert
Canonicalization declaration through the IDL. That declaration names only the Input coordinates on which
Canonicalization is intended to act. Each named coordinate selects one closed Kontrakt-owned law.

The remaining question is no longer whether Canonicalization exists or how a user selects the role. Kontrakt must now
decide which built-in laws it is actually willing to own.

That decision cannot be postponed to implementation.

A built-in law does more than expose a convenient helper. It declares an equivalence relation over legal Input
presentation and chooses the representative that stands for each successful equivalence class. It also fixes the
semantic conditions under which that representative is valid. Once the law becomes public Contract meaning, later
judgments and compiler products may rely on it. They must therefore observe the law rather than reconstructing their own
normalization rule.

This creates a different obligation from an ordinary utility library.

A Java method may normalize text because that operation is useful. A database may rewrite a value because a storage
format prefers one representation. A protocol may define deterministic bytes because signatures require stable encoding.
A scientific system may normalize one domain object by consulting a reference dataset. All of these may be called
canonicalization in their own fields, but they do not automatically belong to the Kontrakt Canonicalization authority.

The built-in catalog therefore needs its own architecture decision.

The catalog must be broad enough that ordinary Java and Kotlin users can solve common representation problems without
authoring executable callbacks. It must also leave a path for professional domains where representation equality is
security-sensitive or scientifically significant. That breadth does not justify importing every operation that another
ecosystem calls normalization. Parsing, validation, encoding, ordering, and business transformation remain separate when
they do not establish the representative owned by Canonicalization.

This ADR defines the semantic catalog boundary and the first V1 catalog. It also defines how later profiles enter the
catalog. Public API projection and implementation Design remain separate layers.

---

# 2. Problem

Kontrakt has two opposite failure modes.

The first is an under-specified catalog.

If V1 only defines the authoring mechanism and leaves the law set open, the frontend cannot know what a nominal
Canonicalization type means. HIR cannot preserve the exact selected law. Establishment cannot know which semantic
material must be authoritative. PBT cannot derive normative partitions. Diagnostics cannot explain what distinction was
erased. The backend can generate a canonicalizer only by consulting implementation-specific tables that have become an
undeclared source of meaning.

The second failure mode is an over-broad catalog.

A generic name such as `StringCanonicalization`, `UrlCanonicalization`, `EmailCanonicalization`, or
`MoneyCanonicalization` hides more than it explains. Each of those domains contains several incompatible notions of
equivalence. A convenient implementation can silently choose one and make that choice appear to be Contract law.

The same danger appears when deterministic serialization is treated as semantic Canonicalization. Protocol Buffers
explicitly distinguishes deterministic serialization from canonical serialization because generic protobuf bytes do not
define one stable semantic representation. RFC 8949 likewise separates deterministic CBOR encoding from the broader and
overloaded term canonicalization. Many signature and wire standards define an exact byte representation for one specific
protocol purpose. Those standards matter to Kontrakt, but a stable byte sequence is not automatically the same semantic
subject as an inbound Canonicalization representative.

Professional domains add another problem. GA4GH VRS normalizes genomic variation against reference sequence meaning.
UCUM gives units a precise algebraic semantics. RDF Dataset Canonicalization can require graph canonical labeling and
has explicit denial-of-service concerns. InChI separates chemical normalization, graph canonicalization, and
serialization into different steps. Such domains show that Kontrakt eventually needs richer profiles, but they also show
why a law may require an exact semantic basis that cannot be replaced by the current host library or environment.

The catalog must therefore answer four questions separately.

First, which laws are genuine Canonicalization laws under ADR-0066?

Second, which of those laws are stable and bounded enough for V1?

Third, which laws require an external semantic basis and therefore need explicit Basis handling before they can be
safely published?

Fourth, which researched profiles belong to another authority or another compiler protocol instead of this catalog?

---

# 3. Decision Drivers

The catalog must preserve the authority boundary already established by ADR-0066. A built-in law exists only to
establish a representative under a declared equivalence relation. Convenience alone is not sufficient.

A law must remain understandable without reading the implementation that realizes it. The public API may give the law a
Java or Kotlin name, but that source name is only frontend evidence. The semantic law must survive a change in that API
name. It must also survive changes to compiler representation or backend realization, including a future frontend that
is not JVM-based.

The catalog must support ordinary application work first. Unicode normalization, case-insensitive identifiers,
line-ending normalization, decimal representation, and a small number of widely standardized textual identifiers are
more valuable to V1 than a large collection of specialized domain profiles.

Professional profiles must remain possible without contaminating the base catalog. A profile that needs a reference
genome, registry snapshot, scientific semantic table, or another externally versioned basis must expose that requirement
rather than reading the current machine environment.

The catalog must preserve V2 incremental freedom. A change to one law or to the semantic basis that law actually
consumes must not force unrelated Canonicalization definitions to change. The same applies when conformance material
grows without changing normative meaning. The physical catalog representation remains replaceable and cannot become the
law's semantic identity.

Security and boundedness are part of admission into the catalog. Canonicalization runs on untrusted inbound material. A
law that is semantically precise but cannot be realized within a reviewable work shape is not ready for the V1 public
catalog.

---

# 4. Decision

Kontrakt will maintain a **Canonicalization Built-In Law Catalog** as Contract-owned semantic specification material.

Catalog membership is an ADR-level decision because membership means that Kontrakt accepts responsibility for the law's
equivalence, representative, semantic basis, refusal meaning, and conformance obligations.

The catalog does not own the exact Java or Kotlin public type names. Those names belong to a separate API specification
that projects catalog laws into supported authoring languages.

The catalog also does not own the implementation algorithm. Design decides how the compiler stores law material and how
the backend computes the representative. Fast paths and implementation-specific optimization stay behind that boundary.

The relation is:

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

A public type name does not define the law.

An implementation algorithm does not define the law.

A conformance vector demonstrates the law but does not replace its semantic specification.

---

# 5. Catalog Law Qualification

## 5.1. Declared Equivalence

A catalog law must state which differences in the supported Input presentation do not change the meaning governed by
that law.

The equivalence cannot be inferred from host equality, hash collision, comparator behavior, serialization output, or an
external library's convenience method.

For a successful law `C`, declared-equivalent inputs must converge on the same representative.

```text
x equivalent-to y under C
    → representative(C, x) = representative(C, y)
```

A distinction that the law does not explicitly erase remains distinct.

## 5.2. Stable Representative

The law must define one representative for each successful equivalence class.

Repeated application must not continue moving the material.

```text
C(C(x)) = C(x)
```

This is an obligation of the semantic law. An implementation may use a fast path for already-canonical material, but the
fast path is not the reason the law is idempotent.

## 5.3. Same-Meaning Preservation

A representative must remain inside the meaning that the law declared equivalent.

A law does not become Canonicalization merely because it produces a deterministic answer. Rounding a price to a market
tick, converting a monetary amount by an exchange rate, correcting malformed input, dereferencing a resource, or
deriving a business value changes or acquires meaning rather than selecting one representation of already-declared
meaning.

Such work belongs to another Contract authority.

## 5.4. Presentation-Domain Closure

A law must state the presentation domain on which it is meaningful.

The domain may be narrower than the host carrier type. A `String` can present general text, an IPv6 address, a BCP 47
language tag, or another structured textual domain. The host type alone does not determine which law applies.

The law may refuse a legal Input presentation that lies outside its canonicalizable domain. It may not repair Input
material that never became a legal presentation.

## 5.5. Boundedness

A V1 catalog law must have a finite work shape that can be reasoned about before arbitrary application behavior is
executed.

The semantic specification must identify any bound that changes the legal domain or representative. Compiler and runtime
safety limits remain separate unless the Contract itself gives them meaning.

The implementation may stop under Budget or Capacity when one of those authorities owns the crossed wall. The stop does
not create a different representative.

## 5.6. Semantic Basis

A law that depends on external semantic data must identify the exact basis capable of changing its result.

A broad JDK version or current provider installation is not sufficient merely because the implementation happens to
obtain the data there.

A Unicode law may depend on a specific Unicode semantic dataset. A BCP 47 profile may depend on an IANA Language Subtag
Registry snapshot. A future genomic law may depend on one exact reference sequence. Each dependency belongs to the law
only when the law actually consumes that meaning.

The physical form of the basis is not fixed by this ADR.

## 5.7. Conformance

Every ratified law must have normative conformance material.

The conformance material must cover successful equivalence collapse, distinctions that must remain different,
already-canonical input, refusal where the law owns refusal, and boundary cases that are known to be security-sensitive.

A built-in implementation can use an external library as an implementation aid or differential oracle. That library does
not become the law's authority.

---

# 6. Catalog Entry Model

A catalog entry must be semantically complete enough that the compiler never needs to reopen the public source type to
discover what the selected law means.

Each ratified entry therefore defines the following information where the information applies.

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

The catalog is not required to encode this information as one runtime object or one stored record.

---

# 7. Catalog Classes

The catalog uses semantic classes to control scope. These classes do not become a runtime enum that user code observes.

## 7.1. Core General Law

A Core General Law solves a common application problem without requiring a user-selected professional domain basis.

Unicode laws may still rely on a ratified Unicode basis because that basis is part of Kontrakt's published law profile
rather than a per-application choice.

Core General Laws ship with the base V1 Canonicalization API.

## 7.2. Standard Profile Law

A Standard Profile Law adopts a narrowly identified public standard whose equivalence and representative are specific
enough to become one Kontrakt law.

The standard does not become authority merely because it is popular. Kontrakt ratifies the exact profile it depends on
and closes any versioned semantic basis required by that profile.

Standard Profile Laws may ship in V1 without being part of the minimal base set.

## 7.3. Specialized Domain Law

A Specialized Domain Law belongs to a professional domain whose meaning requires domain-specific semantic material.

The architecture may support these laws in V1, but a law is not published until its Required Basis, applicability,
bounds, and conformance can be represented under the HIR and Establishment model.

A future module may publish such a law without changing the meaning of existing base laws.

## 7.4. Canonical-Byte Profile

A Canonical-Byte Profile defines normative bytes for signing, hashing, wire interoperability, artifact identity, or
another protocol purpose.

Research into such profiles is recorded here because the same standards often use the word canonicalization. They are
not automatically members of the inbound coordinate Canonicalization catalog.

A separate protocol or API specification owns these profiles when Kontrakt decides to support them.

---

# 8. V1 Core General Catalog

The laws in this section are the initial V1 semantic catalog if this ADR is accepted.

The names below are **semantic law names**. The exact Java and Kotlin public type names are defined later by the API
specification.

## 8.1. Unicode NFC

**Semantic law id:** `text.unicode.nfc`

The presentation domain is Unicode text represented as a finite sequence of Unicode scalar values admitted by the
selected Input surface.

The law uses Unicode Normalization Form C. Canonically equivalent text converges on the NFC representative.
Compatibility distinctions remain observable because NFC does not erase them.

The law depends on the Unicode normalization specification and the Unicode semantic basis ratified for the selected
profile. Unicode normalization stability means that assigned characters retain stable normalization across later Unicode
versions under the Unicode stability policy. Unassigned code points still require an explicit basis policy because
future assignment can change their normalization behavior.

The initial V1 catalog basis is Unicode 18.0.0. The semantic profile records that basis explicitly rather than
inheriting whichever Unicode tables happen to ship with the host runtime.

A future stabilized-string profile may impose the Unicode NPSS refusal rule for unassigned code points.

Canonical bytes are not owned by this law.

## 8.2. Unicode NFD

**Semantic law id:** `text.unicode.nfd`

The law selects Unicode Normalization Form D as the representative.

It preserves the same canonical equivalence relation as NFC but chooses the decomposed representative instead of the
composed representative.

This law exists because some text-processing and scientific pipelines need decomposition as a stable form. Its existence
does not imply that NFD is the recommended form for general application storage.

The Unicode basis and unassigned-code-point policy follow the same rule as the NFC profile.

## 8.3. Unicode NFKC

**Semantic law id:** `text.unicode.nfkc`

The law applies Unicode compatibility decomposition followed by canonical composition.

It intentionally removes compatibility distinctions that NFC preserves. For this reason, selecting NFKC declares a
stronger equivalence relation and must not be treated as a harmless implementation substitute for NFC.

The profile is useful for restricted domains such as identifiers where those distinctions are intentionally irrelevant.
It is not a generic replacement for NFC on arbitrary human-readable text.

The Unicode basis is meaning-determining.

## 8.4. Unicode NFKD

**Semantic law id:** `text.unicode.nfkd`

The law selects the compatibility-decomposed Unicode representation.

It erases the same compatibility distinctions as NFKC while choosing a decomposed representative.

Like NFD, this profile is more likely to be used in specialized text processing than ordinary application storage. It
remains a genuine Canonicalization law because the representative and erased distinctions are exact.

## 8.5. Unicode NFC Case Fold

**Semantic law id:** `text.unicode.nfc-casefold`

This law declares Unicode canonical equivalence and Unicode default caseless matching to be irrelevant distinctions for
the selected coordinate.

Its representative follows the idempotent Unicode relation:

```text
Q(x) = NFC(toCasefold(NFD(x)))
```

Unicode specifies that repeated application of this form remains stable.

This law is distinct from locale-sensitive lowercasing. Turkish casing rules, current host locale, or another locale
service do not participate unless a different law explicitly declares them.

The Unicode case-folding data and normalization basis are meaning-determining.

## 8.6. Unicode NFKC Case Fold

**Semantic law id:** `text.unicode.nfkc-casefold`

This law uses Unicode `NFKC_Casefold` semantics for compatibility-insensitive caseless identifiers.

It is stronger than NFC case folding because compatibility distinctions are also erased.

The law is intended for identifier-like domains rather than unrestricted display text.

The exact Unicode basis is meaning-determining. An implementation must not replace this law with an arbitrary sequence
of `lowercase()`, NFKC, or host-library helpers unless that sequence is proven to implement the selected Unicode profile
exactly.

## 8.7. ASCII Case Fold

**Semantic law id:** `text.ascii.casefold`

The presentation domain is text for which ASCII letter case is the only distinction this law is allowed to erase.

`A` through `Z` map to their lowercase ASCII representatives. Every other code point remains unchanged.

The law deliberately avoids locale behavior and Unicode case folding. It exists for protocol fields and identifiers
whose case-insensitive meaning is explicitly ASCII-defined.

No external semantic basis is required.

## 8.8. LF Line Ending

**Semantic law id:** `text.line-ending.lf`

The law erases the representation difference among CRLF, CR, and LF line endings.

Each CRLF sequence maps to one LF. Each remaining CR maps to LF. Existing LF remains LF.

Other Unicode line-separator code points are not changed by this law.

The result is idempotent and cannot expand the source.

This law does not trim lines, remove blank lines, or alter other whitespace.

## 8.9. Unicode Boundary Whitespace Trim

**Semantic law id:** `text.unicode.whitespace-trim`

The law erases only leading and trailing code points that have the Unicode `White_Space` property under the selected
Unicode basis.

Interior whitespace remains unchanged.

This is not Java `String.trim()`, and it does not inherit the behavior of `String.strip()` by reference. The selected
Unicode property set is the Contract meaning.

The Unicode basis is meaning-determining because property membership can be version-sensitive.

This law does not collapse internal runs and does not perform line-ending normalization.

## 8.10. Decimal Numeric Value

**Semantic law id:** `number.decimal.numeric-value`

The presentation domain is a finite base-10 decimal value represented by an integer coefficient and a decimal scale.

The law declares representational scale differences irrelevant when they denote the same exact decimal number.

For non-zero values, the representative removes every trailing base-10 zero from the coefficient and adjusts the scale
so that the exact numerical value is unchanged. Zero has one representative with coefficient zero and scale zero.

Conceptually:

```text
2.0
2.00
2.000
    → one decimal numerical representative

600.0
    → coefficient 6 with scale -2

0E+10
0.000
-0.0
    → zero with scale 0
```

The law performs no rounding.

A decimal presentation that cannot be represented exactly inside the supported finite domain is outside this law rather
than silently rounded.

This law is intentionally different from monetary scale, currency minor-unit rules, or market tick-size rules. Those
domains may give scale independent business meaning.

## 8.11. Binary32 Canonical NaN

**Semantic law id:** `number.binary32.canonical-nan`

The presentation domain is an IEEE 754 binary32 value when raw NaN representation remains observable at the Input
boundary.

Every NaN bit pattern maps to one ratified quiet-NaN representative.

Every non-NaN bit pattern remains unchanged. In particular, positive zero and negative zero remain distinct under this
law.

The law therefore erases NaN payload and signaling distinctions without declaring signed zero irrelevant.

The V1 binary32 representative is the quiet-NaN bit pattern `0x7fc00000`. That bit pattern is normative law material.

## 8.12. Binary64 Canonical NaN

**Semantic law id:** `number.binary64.canonical-nan`

This law is the binary64 counterpart of `number.binary32.canonical-nan`.

Every NaN representation maps to one ratified quiet-NaN representative. Every non-NaN bit pattern remains unchanged,
including signed zero.

The V1 binary64 representative is the quiet-NaN bit pattern `0x7ff8000000000000`. That bit pattern is normative law
material.

---

# 9. V1 Standard Profile Catalog

The profiles in this section are V1-supported standard profiles, but they are not required to be present in the smallest
base runtime module.

## 9.1. IPv6 RFC 5952 Text

**Semantic law id:** `network.ipv6.rfc5952`

The presentation domain is a legal textual IPv6 address representation without an external zone identifier.

The law declares the alternate textual spellings permitted by the IPv6 address syntax equivalent when they denote the
same IPv6 address.

The representative follows RFC 5952. The profile therefore fixes hexadecimal case, leading-zero suppression, and
zero-compression choice according to that specification.

The law does not canonicalize network interface zone identifiers because those identifiers belong to a different
environment-sensitive domain.

The law performs no DNS lookup and acquires no network authority.

## 9.2. BCP 47 Language Tag

**Semantic law id:** `identifier.bcp47.rfc5646`

The presentation domain is a BCP 47 language tag that is well formed under the supported profile.

The representative follows the canonicalization rules in RFC 5646. Where the specification relies on IANA Language
Subtag Registry `Preferred-Value` data, the exact registry basis becomes meaning-determining. The initial V1 profile
uses the registry snapshot dated 2026-09-17.

The profile cannot silently read the current host registry or a mutable online service. The registry material must be a
closed semantic basis.

Extension-specific canonicalization is supported only when the selected profile has closed the applicable extension
semantics. Unknown or unsupported extension semantics do not acquire an implementation-defined representative.

This law is therefore a useful V1 exercise of Required Basis without granting ambient registry authority.

## 9.3. UUID Lowercase Text

**Semantic law id:** `identifier.uuid.rfc9562-lowercase-text`

The presentation domain is the standard textual UUID form accepted by the selected profile.

RFC 9562 permits uppercase, lowercase, or mixed hexadecimal letters in the standard text form. This Kontrakt profile
declares those case differences irrelevant and chooses lowercase hexadecimal text as its representative.

This law is useful only while the Input presentation is textual. A structured UUID value whose textual case distinction
has already disappeared does not need this law.

The profile does not change UUID version, variant, or bit content.

---

# 10. Deferred General Profiles

The profiles in this section are plausible Canonicalization laws but are not ratified into the initial V1 catalog by
this ADR.

They remain research targets. A later amendment may promote one after its semantic and security review closes.

## 10.1. Unicode Stabilized Normalization

Unicode defines the Normalization Process for Stabilized Strings. It adds an error for code points that are unassigned
in the selected Unicode version so that a successfully normalized result remains stable across past and future Unicode
versions.

This is a strong candidate for security-sensitive persistent identifiers.

It is deferred because the refusal relation and basis/version interaction should be closed together with the
Canonicalization HIR and Establishment re-audit.

Candidate semantic families include stabilized NFC and stabilized NFKC.

## 10.2. Unicode Whitespace Collapse

A law that maps each maximal run of Unicode whitespace to one fixed representative can be defined precisely.

It is deferred because several useful variants disagree about boundary whitespace. Publishing several nearly identical
profiles without evidence of real demand would create unnecessary API surface.

## 10.3. Signed-Zero Collapse

A binary floating-point law may declare positive and negative zero equivalent.

That law is semantically valid but stronger than ordinary representation cleanup. IEEE 754 operations can observe the
zero sign through later behavior.

V1 therefore does not publish a signed-zero-collapsing law until the floating-point Contract consequences are reviewed
explicitly.

## 10.4. RFC 3986 URI Syntax Profile

RFC 3986 defines syntax-based normalization techniques, but URI equivalence is purpose-dependent and stronger
normalization can be scheme-specific.

Kontrakt will not publish a generic `UriCanonicalization`.

A future profile may adopt a narrowly identified RFC 3986 syntax-level relation. Scheme-specific profiles must be
separate laws.

## 10.5. IDNA and PRECIS Profiles

IDNA, Unicode IDNA compatibility processing, and PRECIS username profiles are important security-oriented normalization
systems.

They combine mapping, normalization, and legality rules in ways that do not map one-to-one onto the Kontrakt
authorities.

A future profile may expose the representative-producing portion as Canonicalization while Input or Admission owns the
corresponding legality rule. The profile must be split according to Kontrakt authority rather than copied from the
external standard as one opaque operation.

## 10.6. Instant-Preserving Temporal Profiles

A temporal profile may declare two offset date-time presentations equivalent when they denote the same instant and
choose a UTC representative.

Such a law is valid only when original offset information is explicitly irrelevant. Zone identity, civil-time intent,
daylight-saving interpretation, and unknown-offset semantics can remain meaningful in other applications.

No generic `DateTimeCanonicalization` is published in V1.

---

# 11. Specialized Domain Research Registry

The profiles in this section are not part of the V1 base catalog.

They are recorded because the architecture should be able to support them without a future semantic rewrite.

## 11.1. UCUM Quantity

UCUM gives unit expressions a precise machine-readable algebra.

A future quantity Canonicalization law could treat different magnitude-and-unit presentations as equivalent when they
denote the same physical quantity and select one exact unit representation.

This is an aggregate law. The magnitude and unit must move together. Canonicalizing only the unit string would change
the physical quantity.

The profile therefore requires a closed quantity domain and a ratified UCUM semantic basis before it can be admitted.

## 11.2. GA4GH Variation Representation

GA4GH VRS normalizes ambiguous genomic variation so that equivalent variation can be compared consistently across
systems.

The normalization may depend on exact reference sequence material. Some VRS normalization paths can also change the
object form rather than preserve one host shape.

The domain is therefore a strong test for Required Basis and future shape-policy work, but it cannot be inserted into
the current V1 coordinate catalog until those obligations are explicit.

## 11.3. RDF Dataset Canonicalization

RDFC-1.0 canonicalizes RDF datasets by assigning deterministic identifiers to blank nodes and producing a canonicalized
dataset.

The standard is especially relevant because it explicitly discusses pathological datasets that can cause excessive work.

A future Kontrakt profile would need a domain-specific boundedness design. Budget or Capacity would still own resource
stops, while the profile would own graph equivalence and the required representative.

## 11.4. Chemical Graph Canonicalization

InChI separates normalization, canonical atom labeling, and serialization.

That separation is useful to Kontrakt. Chemical cleanup rules must not be collapsed into canonical graph labeling merely
because one external tool performs both in one pipeline.

A future chemistry profile could own canonical graph labeling after chemical normalization has already established the
domain required by that profile. Serialization to InChI or another string remains a separate responsibility.

## 11.5. Avro Parsing Canonical Form

Avro defines schema equivalence for one precise purpose: whether a reader observes the same writer schema for parsing
data.

That is a legitimate purpose-specific equivalence relation.

A future schema-domain module may adopt the exact Avro Parsing Canonical Form as a law. It does not justify a generic
`SchemaCanonicalization`.

## 11.6. Financial and Securities Domains

Finance uses exact decimal semantics, currency minor units, price ticks, identifiers, and protocol encodings. Those
concerns must not be merged into one `MoneyCanonicalization` or `PriceCanonicalization`.

Representation-only decimal equivalence is already covered by `number.decimal.numeric-value`.

Currency rounding changes value under a business rule. Market tick adjustment changes value under a venue rule.
Exchange-rate conversion derives a new value. Corporate-action adjustment derives another economic meaning. Those
operations are not Canonicalization.

A future securities module may still publish narrow profiles where an external standard genuinely defines several
representations of the same semantic identifier or quantity. Each profile requires its own law and basis review.

---

# 12. Profiles Explicitly Outside the Coordinate Catalog

## 12.1. Deterministic and Canonical Byte Encodings

The following researched standards define important canonical or deterministic byte forms:

- JSON Canonicalization Scheme, RFC 8785;
- deterministic CBOR, RFC 8949;
- ASN.1 DER and CER, ITU-T X.690 / ISO/IEC 8825-1;
- XML canonicalization;
- DAG-CBOR;
- JWK Thumbprint construction, RFC 7638;
- HTTP Message Signature base construction, RFC 9421;
- DNSSEC canonical resource-record representation;
- DKIM `simple` and `relaxed` canonicalization.

These profiles do not enter the V1 coordinate catalog merely because their standards use the word canonicalization.

When Kontrakt supports one of these profiles, the owning protocol must state what those bytes mean. They may belong to a
Contract law, an outward protocol, a signature or identity protocol, or compiler artifact formation. The word canonical
does not decide that ownership.

## 12.2. Protobuf Deterministic Serialization

Protocol Buffers explicitly states that deterministic serialization is not canonical serialization.

Unknown fields and intentionally unspecified serialization choices prevent one generic protobuf byte form from becoming
a universal semantic identity.

Kontrakt therefore must not derive Contract equality or stable identity from deterministic protobuf bytes unless a
narrower Contract profile has closed every relevant ambiguity.

## 12.3. Compiler Artifact Canonicalization

Reproducible-build systems and recent Java artifact-canonicalization research use canonicalization to remove build
representation noise.

That work belongs to compiler artifact formation.

It may improve reproducibility and supply-chain verification, but it does not create an inbound Canonicalization
Contract over user Input.

---

# 13. Generic Laws That V1 Will Not Publish

V1 will not publish broad laws whose names hide unresolved equivalence.

The following names represent categories that are too ambiguous to become built-in laws without a narrower profile:

```text
StringCanonicalization
TextCanonicalization
Normalize
Sanitize
SecureCanonicalization

UriCanonicalization
UrlCanonicalization
EmailCanonicalization
PhoneNumberCanonicalization
PathCanonicalization
FileCanonicalization

DateTimeCanonicalization
MoneyCanonicalization
PriceCanonicalization
QuantityCanonicalization

JsonCanonicalization
XmlCanonicalization
SchemaCanonicalization
FloatCanonicalization
```

The problem is not naming style alone.

For example, SMTP requires mailbox local-parts to be treated as case-sensitive even though mailbox domains are
case-insensitive. A generic lowercase-email law would therefore erase a distinction the protocol preserves.

Phone-number canonicalization often requires regional numbering metadata and lenient parsing. That is not one
ambient-free String law.

URI equivalence differs by purpose and scheme. Filesystem paths are even more environment-dependent because the same
text can be interpreted differently by the active filesystem and operating environment. Kontrakt therefore cannot infer
one portable path equivalence from the carrier string.

A generic name would conceal those determinants rather than close them.

---

# 14. No Identity or Preserve-Everything Laws

The catalog does not contain `ExactCanonicalization`.

It also does not contain built-ins whose only semantic effect is to preserve every representation distinction already
established by Input.

Examples removed from earlier candidate material include raw-bit-preserving float laws, exact binary laws, exact
sequence laws, and exact temporal laws.

An unnamed coordinate already means that Canonicalization does not apply there.

Adding an identity law would create Canonicalization semantic material without erasing any representation freedom. Every
later subsystem would then have to carry and explain a no-op authority that adds no Contract meaning. Omission already
expresses the intended result more precisely.

A law may still be a physical no-op for a particular input that is already canonical. That is different from publishing
a law whose equivalence relation is universally exact identity.

---

# 15. Ordering, Validation, and Refusal Are Not Automatically Canonicalization

Earlier candidate lists mixed several responsibilities.

An IEEE total-order relation is useful, but ordering alone does not select a representative. It therefore does not
become a Canonicalization law merely because canonicalization may later use an order internally.

`RejectNaN` and `RejectNonFinite` are also not admitted as independent Canonicalization laws by this ADR. Rejecting a
value without establishing a different representative is primarily a domain-legality decision. A later 1D review must
decide whether that legality belongs to Input, Admission, Invariant, or another exact owner for the relevant domain.

The same reasoning applies to collection ordering.

If source order is a Contract-visible distinction and the user explicitly declares that order irrelevant, a law that
selects one deterministic representative may be Canonicalization. If the source semantic type was already an unordered
set, sorting its physical storage is only representation preparation.

The catalog does not infer semantic order from a JVM collection implementation.

---

# 16. Aggregate Law Boundary

A catalog law may apply to one coordinate whose presentation domain is itself a closed aggregate.

This does not make the aggregate's child laws independent 1D Contracts.

The entire selected profile remains one law bound to one Input coordinate.

V1 does not expose arbitrary recursive law composition or an open generic tree in which application code constructs
Canonicalization semantics from nested type parameters.

A future built-in aggregate profile may be published only when the complete equivalence and representative are known for
the whole supported aggregate domain.

This rule prevents the catalog from becoming an accidental normalization programming language.

---

# 17. API Projection Boundary

The Catalog Law and the host API symbol are different identities.

The semantic relation is:

```text
public nominal type
    ↓ exact frontend mapping
Catalog Law Reference
    ↓
Canonicalization Definition Candidate meaning
```

The API specification may choose names such as `UnicodeNfc`, `DecimalNumericValue`, or another consistent public
vocabulary. Those names are not fixed by this ADR.

An API rename may be source-incompatible while leaving the underlying Catalog Law meaning unchanged.

Conversely, reusing the same public type name for a changed semantic law is not allowed merely because the source
signature still compiles.

The API specification must preserve the authoring constraints already fixed by ADR-0066:

- the IDL selects one inert Canonicalization declaration;
- the IDL does not select one built-in law directly;
- the declaration names only the Input coordinates to which Canonicalization applies;
- each named coordinate selects one exact Kontrakt-owned law;
- an unnamed coordinate remains outside Canonicalization;
- the declaration contains no executable canonicalizer.

The public type is evidence for one Catalog Law. It is not a runtime strategy object.

---

# 18. HIR and Establishment Integration

This ADR does not complete the Canonicalization HIR and Establishment re-audit that ADR-0066 deliberately leaves open.

It does impose constraints that the re-audit must preserve.

A Canonicalization Definition Candidate must be able to refer to each selected Catalog Law by semantic reference after
frontend resolution. The candidate must not need to retain the Java or Kotlin class name in order to recover law
meaning.

The selected coordinate binding remains part of one Canonicalization Definition Candidate. Catalog laws referenced by
its coordinates do not become independent child 1D Definitions merely because they have stable Catalog Law identities.

If a Catalog Law has Required Basis, that requirement must remain visible in the candidate meaning at the level where
the owning Canonicalization law makes it definition-determining.

Actual Basis Binding and Applicability remain owned by ADR-0063.

The re-audit must also decide whether a Canonicalization occurrence is established per selected coordinate, per
declaration application, or under another exact semantic unit. This ADR does not invent that occurrence law.

---

# 19. External Semantic Basis

A catalog law may depend on released external semantic material when that material is necessary to determine the
representative.

ADR-0073 already distinguishes a closed semantic basis from the current host provider. This ADR applies the same
discipline to built-in laws.

A basis is not identified merely by saying `JDK 26`, `Unicode`, `IANA`, or `current registry`.

The law must identify enough semantic material that two implementations can determine whether they are operating under
the same meaning.

The initial catalog uses three patterns.

### Fixed law with no external table

`text.ascii.casefold`, `text.line-ending.lf`, and the binary floating-point NaN laws are fully defined by the ratified
law profile and fixed numeric standards.

### Ratified versioned dataset

Unicode profiles consume Unicode semantic data. The selected Unicode basis must therefore be explicit enough for
implementation, conformance, and reuse to agree on the result.

### Registry-backed standard profile

`identifier.bcp47.rfc5646` consumes registry semantics when Preferred-Value or extension rules affect the
representative. The registry basis must therefore be closed rather than discovered from a mutable environment.

A future domain law may add a fourth pattern in which the user Contract binds a per-application basis such as a
reference genome. Such a law is not published until the Basis authoring and Establishment path is closed.

---

# 20. Versioning and Catalog Evolution

Adding a new law does not change the meaning of an existing law.

Changing the representative, equivalence relation, refusal law, or meaning-determining basis of an existing law requires
a new semantic profile when the change can affect legal results.

An editorial clarification that does not change any legal result does not create a new profile.

A stronger implementation verification suite does not change the semantic profile when normative meaning remains
unchanged.

A new public API alias may map to an existing Catalog Law without creating a second semantic law. The API specification
owns the compatibility consequences of exposing that alias.

A removed implementation may stop supporting an old profile according to product support policy. Removal does not
retroactively redefine the profile that earlier Contract material referenced.

---

# 21. V2 Incremental and Reuse Boundary

The catalog must not become one monolithic semantic dependency.

A Canonicalization Definition that references `text.unicode.nfc` does not depend on unrelated laws merely because they
live in the same catalog artifact.

V1 should preserve exact per-law semantic references and exact basis relations so V2 can invalidate only consumers whose
determining law or basis changed.

A future persistent compiler may use HID, Merkle composition, fingerprints, projection queries, or another incremental
mechanism. Those mechanisms compare already-defined semantic material. They do not define catalog identity.

The intended dependency shape is:

```text
Canonicalization Definition
    ↓ selected law reference
Catalog Law
    ↓ only when required
Semantic Basis
```

not:

```text
Canonicalization Definition
    ↓
whole catalog version
    ↓
whole platform version
```

This gives V2 a fine-grained change boundary without selecting one incremental algorithm in V1.

---

# 22. Compiler Product and Protocol Boundary

Catalog material can be compiled into tables, generated evaluators, precomputed Unicode structures, or target-specific
code.

Those products are compiler-owned material.

ADR-0075 governs their cross-responsibility consumption when another compiler subsystem requests them.

The Catalog Law remains the semantic source. A generated canonicalizer does not become the law merely because execution
uses it.

An optimizer may specialize a built-in law when established information proves part of the work unnecessary. It may fuse
adjacent implementation steps when the same Contract-visible result is preserved. It cannot replace one Catalog Law with
another because their outputs happen to match on observed training data or a limited test set.

---

# 23. Security and Adversarial Input

Built-in Canonicalization executes on material controlled by the outside world.

Every implementation must therefore treat pathological input as part of the threat model.

Text-law review must account for the ways hostile input can amplify normalization work. Combining sequences and
unassigned code points are part of that review, as are malformed host representations that can break a supposedly closed
text domain.

For structured textual profiles, review includes pathological length and grammar shape before the canonical
representative is formed.

For aggregate or graph profiles, review must consider adversarial symmetry and work amplification. RDFC-1.0 demonstrates
that a semantically meaningful canonicalization algorithm can still have pathological inputs that are dangerous to
process without operational limits.

Security suspicion does not itself create equivalence. UTS #39 confusable skeletons are useful for detecting visually
confusable identifiers, but the Unicode specification explicitly treats the skeleton as an intermediate comparison form
rather than a normalized identifier representation. Such evidence belongs to security analysis, Admission, or
diagnostics unless a separate Contract law explicitly owns it.

---

# 24. Conformance and QA

Each V1 Catalog Law must have a versioned normative vector set before the corresponding public API type is considered
stable.

The vectors must test the law rather than one implementation.

For Unicode profiles, use the applicable Unicode conformance data and additional Kontrakt vectors for preserved
distinctions and authoring boundaries.

For RFC profiles, include the standard's normative and edge-case examples plus Kontrakt-specific negative cases.

Decimal testing must cover several representations of the same numerical value and verify that distinct values never
collapse. It must also cover zero, negative scale, and very large finite coefficients.

For floating-point profiles, generate NaN payloads directly from raw bits and verify that non-NaN representations are
preserved exactly.

Property testing should verify idempotence and equivalence convergence over generated legal domains.

Fuzzing should target the frontend law resolution and the canonicalizer implementation independently.

Differential testing against a trusted external implementation is useful but cannot replace the normative vectors. A
disagreement triggers investigation; it does not automatically make the external library correct.

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

| Profile family                       | Reason for deferral                                                     |
|--------------------------------------|-------------------------------------------------------------------------|
| Stabilized Unicode normalization     | Refusal and Basis interaction should close with HIR / Establishment     |
| Unicode internal whitespace collapse | Useful variants need evidence before public API expansion               |
| Signed-zero collapse                 | Floating-point semantic consequences need explicit review               |
| RFC 3986 URI profile                 | Generic URI equivalence is purpose and scheme sensitive                 |
| IDNA / PRECIS                        | External profile mixes mapping and legality across Kontrakt authorities |
| Temporal instant profiles            | Offset and zone meaning are domain-sensitive                            |
| UCUM quantity                        | Aggregate domain and professional semantic basis                        |
| GA4GH VRS                            | Reference-sequence Basis and possible shape change                      |
| RDFC-1.0                             | Graph domain and adversarial complexity                                 |
| Chemical canonical labeling          | Requires a closed chemical normalization domain                         |
| Avro Parsing Canonical Form          | Specialized schema equivalence                                          |

---

# 26. Decisions Against Earlier Candidate Material

Earlier Canonicalization drafts contained a broad illustrative list. That list was useful for exploration but is not the
V1 catalog.

This ADR removes `ExactCanonicalization` and every preserve-everything filler profile.

It does not carry forward raw-bit-preserving float laws because omission already preserves the Input-established
representation.

It does not carry forward `IeeeTotalOrderCanonicalization` because an ordering relation is not itself a
representative-selection law.

It does not carry forward `RejectNaNCanonicalization` or `RejectNonFiniteCanonicalization` as standalone
Canonicalization laws because rejection alone does not establish a representative.

It does not carry forward generic order-preserving sequence or exact binary profiles for the same reason that Exact
Canonicalization was removed.

Aggregate set, bag, and map profiles remain deferred until the owning collection semantics are closed without relying on
JVM iteration order or user-defined recursive composition.

---

# 27. Migration and Supersession

This ADR does not supersede the Canonicalization authority defined by ADR-0066.

It supplies the catalog that ADR-0066 intentionally left open.

When this ADR is accepted, older candidate-catalog text in ADR-0048 migration material and earlier ADR-0066 revisions
must be treated as historical exploration rather than current V1 API direction.

The current Canonicalization authoring model remains:

```text
IDL selects one Canonicalization declaration

the declaration names only selected Input coordinates

each selected coordinate references one ratified Catalog Law

unselected coordinates remain outside Canonicalization

no direct built-in law selection in the IDL

no ExactCanonicalization filler

no executable user canonicalizer
```

The current project docs should be migrated so that this relation appears only once as active meaning.

---

# 28. Consequences

Kontrakt gains a finite semantic target for the V1 Canonicalization frontend.

The compiler can resolve a nominal API type to one exact Catalog Law without asking implementation code what that type
means.

HIR can preserve semantic law references rather than public class names.

Establishment can later close Basis and Applicability without reopening authoring syntax.

Generated PBT and conformance testing can use the same law partition rather than inventing independent normalization
assumptions.

The backend can optimize aggressively because the representative remains fixed while the realization is replaceable.

The base V1 API remains intentionally smaller than the research inventory. Professional canonicalization is not
rejected; it is prevented from entering the core catalog before its domain basis and authority boundaries are explicit.

The project also gains a clean place to reject misleading convenience APIs. A generic `EmailCanonicalization` or
`MoneyCanonicalization` cannot appear in Design simply because an implementation library offers one method with a
convenient name.

The cost is that some useful combinations are unavailable in V1. One coordinate selects one closed law, so application
code cannot build an arbitrary normalization pipeline. That limitation is intentional. A combination with clear demand
can become a reviewed Catalog Law, while future custom-law support can solve the broader extension problem without
making executable callbacks Contract authority.

---

# 29. Open Work After This ADR

The next Canonicalization work is not more catalog brainstorming.

ADR-0066 must complete its HIR and Establishment re-audit using the catalog boundary defined here. That pass must close
the Canonicalization Definition Candidate and its Required Basis law. It must then define Applicability, the exact
occurrence unit if one exists, and the Established Material visible through the semantic protocol.

A separate V1 API specification must then assign exact Java and Kotlin public names to the ratified Catalog Laws. It
must define package placement and source compatibility rules without changing semantic identity.

Design documents must define the implementation of each law. They own the physical semantic tables and the generated
realization that executes the law. Optimization of those mechanisms remains Design as long as the Catalog Law is
preserved.

Canonical-byte protocols need a separate decision rather than being appended to this coordinate-law catalog.

Specialized domain modules should be investigated only after their Basis and aggregate semantics can be expressed
through the common HIR and Establishment model.

---

