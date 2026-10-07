# Canonicalization Built-In Law Catalog Design and Candidate Qualification

## Status

Draft

## Date

2026-10-06

## Governing ADRs

- ADR-0076: Canonicalization Built-In Law Catalog
- ADR-0066: Canonicalization Contract
- ADR-0053: Version Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0064: Input Contract
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary
- ADR-0075: Compiler Result Ownership, Legal Consumption, Deterministic Realization, and Contract / Implementation
  Separation

---

# 1. Purpose

This document is the working Design surface for concrete Canonicalization Built-In Law candidates. It records the
material needed to decide which exact laws Kontrakt will actually provide.

ADR-0076 owns the Catalog architecture and the law for admission. It also fixes the Authority and Version boundaries
that this Design must preserve. This document does not replace those decisions. A candidate does not gain Contract
authority merely because it is described or implemented here.

The responsibilities are intentionally separated:

```text
ADR-0076
    Catalog architecture
    Built-In Law Authority membership law
    qualification and admission law
    identity and Version boundaries

this Design area
    candidate inventory
    candidate research
    exact-law qualification records
    deferred-profile analysis
    proposed concrete Catalog population
    API and implementation design inputs

Exact Built-In Law Authority
    owns exact versioned Contract meaning

compiler realization
    realizes admitted meaning
```

A candidate becomes a Catalog member only through an explicit admission decision under ADR-0076. A public API symbol
cannot perform that admission. Neither can implementation registration, generated code, or verification evidence.

---

# 2. Candidate Qualification Record

Each candidate is reviewed as one proposed independently owned Exact Built-In Law Authority. The working record must be
rich enough to answer ADR-0076 without creating a generic property bag.

At minimum, every candidate review must close or explicitly mark unresolved:

```text
Exact Operand Requirement
Exact Equivalence Definition
Exact Representative Definition
Exact Representative Coverage

Exact Law-Specific Failure Semantics
    when Coverage is Restricted

Exact Law-Specific Semantic Determinants
    when additional meaning changes an observation owned by the law

Representative-domain closure
    whether every successful representative remains a legal value
    of the exact semantic presentation promised by the law

External semantic material
    which external distinctions actually determine Contract meaning
    which external facts are only realization or verification evidence
    without collapsing those two roles into one generic basis field

Version consequence
    what Contract-visible change requires a new law Version

Built-In suitability
    why Kontrakt can legitimately provide this as reusable built-in meaning

Authority uniqueness
    why the candidate is an independent semantic subject
    rather than another name for an already owned Authority
```

Candidate-specific typed structures may be used in Design to represent the exact meaning efficiently. They are not a
parallel semantic authority beside the exact categories above.

The review must also keep semantic qualification distinct from realization evidence:

```text
semantic qualification
    decides what the law means

realization evidence
    demonstrates that supported realizations can preserve that meaning
```

A realization constraint cannot weaken the law. For example, an inconvenient provider or backend does not justify a
different representative.

The qualification record must be precise enough for later compiler stages to know their exact semantic inputs and
validity boundary. Compiler reuse machinery remains separate from Built-In Law meaning. A cache key or result
fingerprint, for example, has no Contract meaning unless an owning Contract makes that distinction observable.

---

# 3. Cross-Candidate Review Traps

The following checks apply during candidate research.

A broad standard or library name is not a complete law. Every choice that can change the representative or an owned
failure must be explicit. A tie-break rule is one example. The treatment of unknown members is another.

A representative must remain inside the law's exact semantic presentation domain. If representative formation can leave
that domain, the candidate must close the problem explicitly. It can narrow the operand domain or use Restricted
Coverage with exact failure semantics. Otherwise it must prove domain closure.

Ambient state must not finish the law. For example, a `latest` registry or the host locale cannot select Contract
meaning. Provider defaults and implementation state are subject to the same rule.

Current semantic equality does not merge independently owned Authorities. The reverse is also true: a different public
name does not justify a new Authority. A different source spelling or implementation routine is likewise insufficient.

Validation or ordering alone is not Canonicalization. A physical storage change is not Canonicalization either. The
candidate must satisfy the representative-selection law owned by ADR-0066.

Candidate labels in this document are working labels only. They are not Authority or Version identities. They also make
no promise about public API names or compiler storage coordinates.

ADR-0066 may use several admitted Exact Built-In Laws inside one finite ordered Canonicalization composition. Candidate
qualification here still judges each Exact Built-In Law independently. Composition does not create a new Catalog member
or merge the constituent Authorities. ADR-0066 remains the owner of composition legality.

---

# 4. Current Candidate Inventory

The working inventory now includes operations that recur in ordinary developer libraries and production text
infrastructure. It also keeps important numeric and protocol profiles. Ecosystem prevalence is evidence that a semantic
subject deserves review. It does not mean that a host API already defines the Kontrakt law.

Every row remains a candidate until its exact semantics and Built-In suitability are closed under ADR-0076.

| Review area           | Candidate semantic label                    | Working status |
|-----------------------|---------------------------------------------|----------------|
| Boundary text         | `text.ascii.whitespace-trim`                | Candidate      |
| Boundary text         | `text.ascii.whitespace-trim-start`          | Candidate      |
| Boundary text         | `text.ascii.whitespace-trim-end`            | Candidate      |
| Boundary text         | `text.unicode.whitespace-trim`              | Candidate      |
| Boundary text         | `text.unicode.whitespace-trim-start`        | Candidate      |
| Boundary text         | `text.unicode.whitespace-trim-end`          | Candidate      |
| Boundary text         | `text.unicode.whitespace-collapse-space`    | Candidate      |
| Case                  | `text.ascii.lowercase`                      | Candidate      |
| Case                  | `text.ascii.uppercase`                      | Candidate      |
| Case                  | `text.ascii.casefold`                       | Candidate      |
| Case                  | `text.unicode.default-lowercase`            | Candidate      |
| Case                  | `text.unicode.default-uppercase`            | Candidate      |
| Case                  | `text.unicode.default-full-casefold`        | Candidate      |
| Unicode normalization | `text.unicode.nfc`                          | Candidate      |
| Unicode normalization | `text.unicode.nfd`                          | Candidate      |
| Unicode normalization | `text.unicode.nfkc`                         | Candidate      |
| Unicode normalization | `text.unicode.nfkd`                         | Candidate      |
| Unicode normalization | `text.unicode.nfc-casefold`                 | Candidate      |
| Unicode normalization | `text.unicode.nfkc-casefold`                | Candidate      |
| Text representation   | `text.line-ending.lf`                       | Candidate      |
| Text representation   | `text.unicode.decimal-digit-fold`           | Candidate      |
| Text representation   | `text.unicode.cjk-width-fold`               | Candidate      |
| Text representation   | `text.unicode.diacritic-fold`               | Candidate      |
| Numeric               | `number.decimal.numeric-value`              | Candidate      |
| Numeric               | `number.binary32.canonical-nan`             | Candidate      |
| Numeric               | `number.binary64.canonical-nan`             | Candidate      |
| Encoded text          | `encoding.base16.lowercase-text`            | Candidate      |
| Encoded text          | `encoding.base16.uppercase-text`            | Candidate      |
| Encoded text          | `encoding.base64.rfc4648-canonical-text`    | Candidate      |
| Encoded text          | `encoding.base64url.rfc4648-canonical-text` | Candidate      |
| Numeric text          | `text.integer.decimal-canonical`            | Candidate      |
| Protocol / Identifier | `network.ipv6.rfc5952`                      | Candidate      |
| Protocol / Identifier | `identifier.bcp47.rfc5646`                  | Candidate      |
| Protocol / Identifier | `identifier.uuid.rfc9562-lowercase-text`    | Candidate      |

No row in this table is a Catalog admission decision. The dotted labels are review coordinates only. Qualification may
show that a candidate needs to be renamed or split. It may also show that two public names should resolve to one
Authority. A candidate may still be deferred or rejected before admission.

## 4.1. Ecosystem Demand Signals

The inventory expansion is based on repeated normalization surfaces in established ecosystems rather than on one
language's convenience API.

| Developer operation              | Representative ecosystem evidence                                     | Catalog implication                                                                 |
|----------------------------------|-----------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| Trim both boundaries             | Kotlin `trim`; Elasticsearch `trim`                                   | The whitespace set must be explicit                                                 |
| Trim one boundary                | Kotlin `trimStart` / `trimEnd`; Java `stripLeading` / `stripTrailing` | Start-only and end-only forms select different representatives                      |
| Case conversion                  | Kotlin case conversion; Elasticsearch case filters                    | ASCII and Unicode mappings must be distinct; ambient locale cannot define the law   |
| Caseless matching                | Python `casefold`; ICU Case Folding                                   | Case folding is not the same semantic subject as lowercasing                        |
| Unicode normalization            | Java `Normalizer`; ICU `Normalizer2`                                  | NFC/NFD/NFKC/NFKD are established normalization families                            |
| Identifier-style Unicode folding | Unicode `NFKC_Casefold`; ICU NFKC_Casefold                            | A standardized combined profile must not be reconstructed from unrelated host calls |
| Whitespace collapse              | Apache Commons `normalizeSpace`; Guava collapse helpers               | The whitespace set and replacement must be exact                                    |
| Line-ending normalization        | Git text normalization                                                | The LF representative is common, but the accepted source separators must be exact   |
| Decimal representation reduction | Java `BigDecimal.stripTrailingZeros`; Python `Decimal.normalize`      | Numeric-value equivalence is common, but representative-domain closure is required  |
| Specialized Unicode folding      | Elasticsearch normalizers; Lucene folding filters                     | Broad search folding should be decomposed into exact narrow laws                    |
| Encoded text canonicalization    | RFC 4648; common codec libraries                                      | Encoded-text canonicalization is distinct from bytes-to-text encoding               |
| Protocol text profiles           | RFC 9562 UUID text; RFC 5952 IPv6 text                                | Protocol profiles are useful, but they are reviewed after the everyday surface      |

The table records demand evidence only. It does not import host-library semantics. A Kotlin or ICU method, for example,
can show that developers repeatedly need a distinction removed while still being unsuitable as the normative definition
of that removal.

## 4.2. Review Priority

The review order starts with operations that developers use frequently. It then moves toward candidates with harder
semantic or security boundaries. Earlier review does not imply admission.

```text
boundary whitespace laws
    ↓
ASCII case laws
    ↓
Unicode case laws
    ↓
Unicode normalization forms
    ↓
Case-fold normalization authority review
    ↓
whitespace and line-ending laws
    ↓
Decimal Numeric Value
    ↓
specialized Unicode folding laws
    ↓
encoded and numeric text representatives
    ↓
binary NaN profiles
    ↓
protocol and identifier profiles
```

This order starts with ordinary application operations. Later groups require more external semantic material or a more
specialized interpretation. Protocol and raw-bit profiles therefore come later.

---

# 5. General Candidate Working Notes

The laws in this section address common text or numeric representation choices. Presence here does not mean
ratification. Each candidate must pass ADR-0076 qualification as one exact Built-In Law Authority. If it does not, the
review must say why. A duplicate meaning or an over-broad subject are two common reasons.

Text candidates consume the `Text` presentation already established by ADR-0064, which is a sequence of Unicode scalar
values rather than arbitrary JVM UTF-16 code units. Host material outside that legal Input presentation never enters
Canonicalization.

The working labels below are not public API names.

## 5.1. Boundary Whitespace Trim Family

Current candidates:

```text
text.ascii.whitespace-trim
text.ascii.whitespace-trim-start
text.ascii.whitespace-trim-end

text.unicode.whitespace-trim
text.unicode.whitespace-trim-start
text.unicode.whitespace-trim-end
```

These candidates exist because general-purpose APIs repeatedly expose both-boundary, leading-only, and trailing-only
trimming. They must not be collapsed into one law merely because an implementation can share one scanner.

The ASCII family cannot inherit a host predicate by name. Java `trim()` is one example of behavior that must not define
the Contract. Qualification must choose the exact finite ASCII code-point set. In particular, "ASCII whitespace" and
"every code point at or below U+0020" are not synonymous.

The Unicode family is intended to use an exact Unicode whitespace property under an explicit Unicode semantic Basis.
Qualification must decide whether the candidate is defined by the Unicode `White_Space` property or by another exact
Unicode set. The property choice and any Version-sensitive membership are law meaning.

For every family member, interior characters remain unchanged. A start-only law does not remove trailing whitespace; an
end-only law does not remove leading whitespace; a both-boundary law removes both. These distinct representative
functions are separate semantic subjects unless a later Authority-uniqueness review proves that one should be expressed
only as a composition or API alias.

Empty input and all-whitespace input need explicit representative treatment. For the obvious trim relation the expected
representative is the empty Text value, but that is not considered closed until the exact operand and whitespace-set
laws
are fixed.

## 5.2. ASCII Case Conversion and Case Fold

Current candidates:

```text
text.ascii.lowercase
text.ascii.uppercase
text.ascii.casefold
```

`text.ascii.lowercase` maps ASCII `A` through `Z` to `a` through `z` and preserves every other Unicode scalar value.
`text.ascii.uppercase` applies the inverse case-direction mapping to ASCII letters and likewise preserves every other
scalar value.

The ASCII case-fold candidate declares ASCII letter case irrelevant and selects one exact representative. The current
working direction is lowercase ASCII. Qualification must determine whether this law is semantically identical to the
ASCII lowercase candidate. If it is the same exact semantic subject, the Catalog must not create two Authorities merely
because developers use the words "lowercase" and "case-insensitive" for different intents. API aliases or separate
authoring names may still project one Authority if that is the deliberate design.

None of the ASCII candidates owns locale behavior or Unicode case data.

## 5.3. Unicode Default Case Conversion and Full Case Folding

Current candidates:

```text
text.unicode.default-lowercase
text.unicode.default-uppercase
text.unicode.default-full-casefold
```

The lowercase and uppercase candidates exist because invariant case conversion is a ubiquitous developer operation.
Their meaning cannot come from ambient host behavior. Java's default locale is one example. The host's current Unicode
tables are another.

Qualification must close the exact Unicode Default Case Conversion profile. Full mappings and context-sensitive rules
must be part of that profile when they affect the result. Any Unicode Basis that changes the mapping must also be
explicit. Locale-specific conversion is separate; Turkish casing is a representative example.

Before admission, lowercasing and uppercasing must independently satisfy ADR-0066 idempotence and representative-class
requirements. The fact that a standard library exposes a transformation is not proof that it forms a legal
Canonicalization law under Kontrakt's equivalence model.

The case-fold candidate is not lowercasing. It targets Unicode Default Full Case Folding for locale-independent caseless
matching, including multi-code-point foldings where the Unicode profile requires them. Qualification must distinguish
Default Full Case Folding from Simple Case Folding and Turkic-specific folding. Those are not interchangeable profiles.

Unicode case data is meaning-determining when it changes the exact representative. Provider name or ICU/JDK version is
not itself the semantic Basis.

## 5.4. Unicode Normalization Forms

Current candidates:

```text
text.unicode.nfc
text.unicode.nfd
text.unicode.nfkc
text.unicode.nfkd
```

### NFC

The candidate selects Unicode Normalization Form C as the representative for canonical equivalence. Compatibility
distinctions remain observable.

NFC has the strongest general-text demand signal among the four normalization forms. Qualification must nevertheless
close the exact Unicode Basis dependency rather than inherit host Unicode tables. Unicode normalization stability may
preserve part of the law across later versions. Unassigned code points still require explicit treatment.

Canonical bytes are not part of this candidate law.

### NFD

NFD uses the same canonical-equivalence relation as NFC and selects the canonical decomposed representative.

The law remains useful for internal text-processing domains even though it is less common as an application-facing
default. Qualification must close the same Basis and evolution questions as NFC and justify its independent Built-In
suitability.

### NFKC

NFKC applies compatibility decomposition followed by canonical composition. Selecting it therefore declares
compatibility distinctions irrelevant in addition to canonical distinctions.

NFKC is not an implementation substitute for NFC. It has a stronger equivalence relation and is especially relevant to
restricted domains such as identifiers. Qualification must close its exact Unicode Basis and operand domain. Version
evolution must then be checked against that meaning.

### NFKD

NFKD erases the same compatibility distinctions as NFKC and selects the compatibility-decomposed representative.

Qualification must close the same Basis and evolution questions and justify why the decomposed compatibility
representative deserves an independent Built-In Authority rather than remaining a specialized internal-processing
profile.

## 5.5. Unicode NFC Case Fold

**Candidate semantic label:** `text.unicode.nfc-casefold`

This candidate is intended to collapse Unicode canonical-equivalent and default-caseless distinctions under one
independently specified exact law.

It must not be defined merely by the phrase "NFC plus case folding." ADR-0066 now permits finite ordered composition of
admitted Exact Built-In Laws, so qualification has an additional Authority-uniqueness question: does NFC Case Fold have
one independently standardized or otherwise independently owned exact semantic subject, or should ordinary use be
expressed as an explicit composition of separately admitted laws?

If an independent Authority is retained, qualification must first close the exact equivalence relation and
representative. It must then fix the case-folding profile and Unicode Basis. Unassigned-code-point behavior and
repeated-application stability require explicit review. Locale-sensitive lowercasing is not part of this law.

## 5.6. Unicode NFKC Case Fold

**Candidate semantic label:** `text.unicode.nfkc-casefold`

This candidate targets Unicode `NFKC_Casefold` semantics for identifier-like text. It is a defined semantic operation,
not an arbitrary composition chosen by an implementation. In particular, ordinary lowercasing cannot stand in for it.

The Unicode profile combines compatibility normalization with case folding. It also defines how default-ignorable
material is treated. Qualification must pin the Unicode semantic material that determines those results and review the
profile as one complete exact law.

Because Unicode and ICU expose NFKC_Casefold as a distinct semantic operation, this candidate has a stronger independent
Authority case than an ad hoc composition. That observation is evidence for review, not an admission decision.

## 5.7. Unicode Whitespace Collapse to Space

**Candidate semantic label:** `text.unicode.whitespace-collapse-space`

Whitespace collapse is common in application utility libraries and search normalization. The current candidate is
intended to remove boundary whitespace and map each maximal interior run from one exact Unicode whitespace set to one
U+0020 SPACE.

Qualification must close all of the following rather than inherit a library's `normalizeSpace` behavior:

```text
exact whitespace set
whether leading runs are removed
whether trailing runs are removed
replacement scalar value
whether line separators are part of the collapsed set
empty / all-whitespace representative
Unicode Basis and evolution when a Unicode property defines membership
```

ADR-0066 composition creates an Authority-uniqueness issue here as well. If the exact candidate meaning is nothing more
than a legal composition of boundary trim and an independently admitted run-collapse law, the Catalog must decide
whether one independent Built-In Authority is justified. Convenience alone is insufficient.

## 5.8. LF Line Ending

**Candidate semantic label:** `text.line-ending.lf`

The candidate is motivated by cross-platform text tooling and source-control systems that normalize repository text to
LF. That ecosystem behavior is demand evidence, not the law definition.

The current working relation accepts CRLF, standalone CR, and LF as line-ending spellings. LF is the representative.
Other Unicode line separators remain unchanged unless a later exact profile admits them; U+2028 and U+2029 are two
examples.

Qualification must decide the exact source separator set rather than inherit host newline behavior. Git is evidence of
demand, not semantic authority. The law does not alter ordinary text around line endings. In particular, it does not
trim
or collapse content.

## 5.9. Unicode Decimal Digit Fold

**Candidate semantic label:** `text.unicode.decimal-digit-fold`

Search and text-normalization infrastructure commonly folds native decimal digits into ASCII digits. The narrow
candidate should target Unicode decimal digits only, not every character with a numeric value.

The working representative maps a Unicode scalar with General_Category `Nd` and decimal value `0` through `9` to ASCII
`0` through `9`, preserving every other scalar value.

Qualification must close the exact Unicode property and value mapping. It must also settle Version dependence and prove
that the representative stays in the promised domain. Other numeric characters remain outside this candidate; Roman
numerals and vulgar fractions are two examples.

## 5.10. Unicode CJK Width Fold

**Candidate semantic label:** `text.unicode.cjk-width-fold`

Width folding is used by search systems to remove selected full-width / half-width presentation distinctions. The
candidate must not be defined as "whatever Elasticsearch `cjk_width` or ICU currently does."

Qualification must identify the exact supported mapping relation. A narrow width law must remain distinct from full
NFKC compatibility normalization. If full-width ASCII or half-width Katakana are included, their mappings must be
explicit. Multi-scalar cases involving combining or voiced marks need separate closure.

The law requires an explicit Unicode semantic Basis when Unicode data determines the mapping.

## 5.11. Unicode Diacritic Fold

**Candidate semantic label:** `text.unicode.diacritic-fold`

Accent and diacritic folding is common in search and application libraries. There is still no safe generic meaning
behind the phrase "strip accents."

Apache Commons and Lucene demonstrate clear demand, but their behavior does not define one shared law. Some approaches
decompose text, while others transliterate or remove combining marks. Broad ICU search folding removes still more
distinctions, so it cannot silently define this narrower candidate.

Unicode UTR #30 Character Foldings was withdrawn before a final published version. It therefore cannot be cited as an
ambient normative authority that silently completes this candidate.

Before admission this candidate must either:

```text
identify one current normative mapping source that exactly owns the desired relation
or
define a Kontrakt-owned exact finite/versioned mapping law with independently reviewable semantics
```

Until that closure exists, this row is a demand-backed candidate, not an admission-ready law.

## 5.12. Decimal Numeric Value

**Candidate semantic label:** `number.decimal.numeric-value`

The presentation domain is a finite base-10 decimal represented by an integer coefficient and a decimal scale. Scale
differences are irrelevant when they denote the same exact decimal number.

For non-zero values, the working representative removes every trailing base-10 zero from the coefficient and adjusts the
scale without changing the exact value. Zero has one representative with coefficient zero and scale zero.

```text
2.00
    → the same representative as 2

600.0
    → coefficient 6 with scale -2

0.00
    → coefficient 0 with scale 0
```

Java `BigDecimal.stripTrailingZeros` and Python `Decimal.normalize` provide strong demand evidence for this relation.
Those host APIs do not define the Contract.

Qualification must resolve representative-domain closure. A host decimal carrier can have a bounded scale even when the
mathematical representative would require a scale outside that carrier's range. Kontrakt must narrow the operand domain,
use Restricted Coverage with exact failure semantics, or choose a semantic Decimal domain whose representative is
closed. The law performs no rounding.

This profile is distinct from business scale rules. A fixed monetary scale is one example of meaning that belongs
elsewhere.

## 5.13. Binary32 Canonical NaN

**Candidate semantic label:** `number.binary32.canonical-nan`

The presentation domain is IEEE 754 binary32 only when raw NaN representation remains Contract-visible at the Input
boundary. The candidate maps every NaN bit pattern to one quiet-NaN representative while preserving every non-NaN bit
pattern, including signed zero.

The current candidate representative is `0x7fc00000`. Qualification must first prove that the raw NaN bits survive every
supported Input and backend path. In particular, NaN payload and signaling state must remain observable if the law is to
own them.

## 5.14. Binary64 Canonical NaN

**Candidate semantic label:** `number.binary64.canonical-nan`

The binary64 candidate follows the same shape as binary32. Every NaN representation maps to one quiet NaN while every
non-NaN bit pattern remains unchanged.

The current candidate representative is `0x7ff8000000000000`. Qualification carries the same raw-bit observability and
carrier-preservation requirements as binary32.

---

# 6. Encoded and Structured Text Working Notes

These candidates apply to structured textual presentations. Their exact meaning depends on a closed grammar, often from
a standard. They do not perform bytes-to-text encoding or parse the value into a new semantic domain. Validation-only
rules and resource resolution remain outside this group.

## 6.1. Base16 Lowercase Text

**Candidate semantic label:** `encoding.base16.lowercase-text`

The operand must already be legal Base16 textual presentation under an exact profile. The candidate does not encode
bytes
into hexadecimal text.

The intended equivalence declares hexadecimal letter case irrelevant and selects lowercase ASCII hex digits as the
representative while preserving the represented bit string.

Qualification must close the exact Base16 grammar. For example, it must decide whether a `0x` prefix or separators are
legal. It must also decide whether odd digit counts are legal. RFC 4648 Base16 has no padding, and a broader host parser
must not silently widen the law.

## 6.2. Base16 Uppercase Text

**Candidate semantic label:** `encoding.base16.uppercase-text`

This candidate has the same legal encoded-text boundary as the lowercase profile but selects uppercase hexadecimal
letters.

Authority uniqueness must be reviewed explicitly. Lowercase and uppercase select different representatives and therefore
cannot be aliases merely because they preserve the same decoded bytes.

## 6.3. Base64 RFC 4648 Canonical Text

**Candidate semantic label:** `encoding.base64.rfc4648-canonical-text`

The operand must already be legal Base64 text under one exact RFC 4648 profile. The law does not perform bytes-to-text
encoding and does not repair arbitrary decoder input.

RFC 4648 identifies canonical-encoding requirements around alphabet, padding, and unused pad bits. Qualification must
close:

```text
standard Base64 alphabet
padding requirement
allowed line breaks or other non-alphabet characters
unused pad-bit requirements
whether alternate but decodable spellings are legal operands
the exact representative text for each admitted encoded value
```

A liberal decoder accepting ignored characters cannot become the law by implementation accident.

## 6.4. Base64url RFC 4648 Canonical Text

**Candidate semantic label:** `encoding.base64url.rfc4648-canonical-text`

Base64url uses a different alphabet from standard Base64 and cannot be treated as an API flag on one ambiguous law.

Qualification must also select an exact padding policy. RFC 4648 permits referring specifications to omit padding in
specific circumstances, so "Base64url" alone does not close one representative. If more than one materially different
profile is required, this candidate must split before admission.

## 6.5. Decimal Integer Text Canonical Form

**Candidate semantic label:** `text.integer.decimal-canonical`

Developers frequently remove redundant leading zeros or normalize signs when handling decimal integer text. That demand
does not justify a vague "number string normalize" law.

The candidate must define one exact textual grammar and representative relation. Qualification must explicitly decide:

```text
optional or required sign
treatment of leading plus
leading-zero equivalence
negative zero
empty or sign-only text
ASCII digits only or broader Unicode digits
maximum length / semantic bounds where Contract-visible
```

Parsing text into a numeric Fact is not Canonicalization. This candidate is legal only if the operand remains Text and
the output is one same-shape representative of the declared textual equivalence class.

## 6.6. IPv6 RFC 5952 Text

**Candidate semantic label:** `network.ipv6.rfc5952`

The candidate domain is legal textual IPv6 presentation without an external zone identifier. Alternate legal spellings
are equivalent when they denote the same IPv6 address, and RFC 5952 supplies the target text form.

Qualification must keep Input legality separate from the candidate's canonicalizable domain. If the selected Input is
broader legal Text, non-IPv6 text requires exact Canonicalization Coverage and failure treatment. The grammar must not
come from a host parser. The profile must settle embedded IPv4 forms and zero-run tie breaking. It must also settle
leading-zero and letter-case rules. Zone identifiers remain a separate decision.

The law performs no DNS lookup and does not resolve interface scope.

## 6.7. BCP 47 Language Tag

**Candidate semantic label:** `identifier.bcp47.rfc5646`

The candidate domain is a well-formed BCP 47 language tag under one exact Kontrakt profile.

RFC 5646 supplies syntax and canonicalization rules, while IANA Language Subtag Registry data becomes
meaning-determining wherever the representative consumes registry fields such as Preferred-Value or deprecated
relations.

Qualification must close the exact registry Basis and its evolution consequences. Legacy or deprecated tags need an
explicit rule. Extensions and private-use material need their own treatment. Future registrations cannot silently change
the meaning through a mutable current registry.

## 6.8. UUID Lowercase Text

**Candidate semantic label:** `identifier.uuid.rfc9562-lowercase-text`

The candidate domain is one exact standard textual UUID form. Hexadecimal letter case is declared irrelevant and
lowercase text is the representative.

Qualification must confirm the accepted grammar and delimiter positions. It must separately decide whether wrappers or
URN forms belong to the operand. Alternative textual forms cannot be accepted by accident. The law changes neither UUID
bits nor version or variant meaning.

A structured UUID value that no longer contains textual case does not need this law.

---

# 7. Deferred Profile Working Notes

The following profiles are plausible or commonly requested transformations but are not part of the current
admission-ready set. Moving one into the built-in Catalog requires explicit qualification and a Catalog admission
decision under ADR-0076. A new ADR is required only if that work changes the Catalog architecture or qualification law.

## 7.1. Unicode Stabilized Normalization

Unicode Stabilized Strings reject code points that are unassigned in the selected Unicode version so that a successful
normalization result remains stable across version evolution.

Stabilized NFC and NFKC remain deferred until their exact refusal relation and semantic-Basis requirements are closed at
the Catalog level. ADR-0066 owns how those closed requirements are represented and established downstream.

## 7.2. Locale-Sensitive Case Conversion

Locale-sensitive case conversion is common in user-facing text but cannot inherit an ambient locale. The JVM process
locale is one example of state that must not become hidden Contract meaning.

A future law must identify the exact locale/profile as Contract-visible semantic material and prove that the resulting
mapping still satisfies Canonicalization's representative and idempotence laws. It remains deferred from the basic
locale-independent surface.

## 7.3. Turkic Case Folding

Unicode defines Turkic-specific case-fold behavior distinct from Default Case Folding. It is not silently selected by
language environment or locale.

A future profile must make that distinction explicit and independently justify Built-In suitability. It remains
separate from `text.unicode.default-full-casefold`.

## 7.4. Broad Search Folding

Lucene ICU folding and similar search pipelines intentionally erase several kinds of distinction at once. Case and
accent are two examples. Width and digit distinctions may also be erased.

That breadth is useful for search but is too large to import as a default Canonicalization law merely because a SOTA
search library provides it. Narrow laws are reviewed separately above. A future broad search-fold Authority must first
define its exact relation and representative. It must then close its Unicode Basis and security consequences.

## 7.5. Signed-Zero Collapse

A binary floating-point law may declare positive and negative zero equivalent, but IEEE 754 behavior can later observe
the sign. V1 therefore defers this law until those semantic consequences are reviewed explicitly.

## 7.6. RFC 3986 URI Syntax Profile

RFC 3986 defines syntax-level normalization, but URI equivalence depends on purpose and can become scheme-specific. V1
therefore publishes no generic `UriCanonicalization`.

A later law must identify the exact syntax-level or scheme-specific equivalence it owns.

## 7.7. IDNA and PRECIS Profiles

IDNA and PRECIS combine representative formation with legality rules. A future Kontrakt profile must split those
responsibilities according to Kontrakt authority rather than import an external pipeline as one opaque law.
Representative formation may belong to Canonicalization while Input or Admission owns the corresponding legality rule.

## 7.8. Instant-Preserving Temporal Profiles

A temporal law may declare two offset date-time presentations equivalent when they denote the same instant and select a
UTC representative. Such a law is valid only when the original civil-time context is explicitly irrelevant.

V1 therefore publishes no generic `DateTimeCanonicalization`.

---

# 8. Admission Decision Record

When candidate qualification is complete, the working record should end in one explicit disposition:

```text
ADMIT
    candidate satisfies ADR-0076 and an exact Built-In Law Authority
    is explicitly admitted to the Catalog

DEFER
    candidate may be valid but one or more admission questions remain unresolved

REJECT
    candidate does not satisfy the Canonicalization or Built-In Catalog boundary
    under the proposed semantic subject
```

The disposition must state the semantic reason. Implementation availability alone does not turn a semantically valid law
into a different law, and implementation registration does not turn a candidate into a member.

An `ADMIT` record must identify the exact independently owned Law Authority being admitted. Once admitted, ADR-0076
treats Built-In Law Authority Membership as historical and monotonic. A later lifecycle or Governance rule may restrict
future use without erasing or reassigning that membership.

A new exact Version under an already-admitted Authority does not create another Catalog member.

---

# 9. API and Compiler Design Follow-On

Concrete API design begins only from admitted or deliberately prototyped candidate meaning. Follow-on Design has four
main concerns:

```text
public authoring spelling and generated projection
exact Authority and Version reference projection
compiler lookup and physical representation
optimized realization and reuse
```

None of these artifacts owns Catalog membership or exact law meaning.

Source aliases must resolve to the same exact Authority rather than mint duplicate Catalog members. Similarity cannot
infer aliasing. Equal structure or shared implementation code, for example, is insufficient.

Compiler realization may physically fuse adjacent lookup and evaluation work when profitable. That optimization must
still preserve the logical Authority and Version boundary. Contract-visible observations must remain recoverable.

---

# 10. Qualification Evidence and Verification Follow-On

Admission must not precede the evidence needed to show that the proposed law is semantically closed and independently
checkable. Security-relevant representation ambiguity must also be examined before admission. When V1 feasibility is in
doubt, qualification evidence must expose that risk rather than hide it.

That evidence should cover the questions that can invalidate admission:

```text
normative and conformance evidence
adversarial or differential evidence where interpretation can diverge
representative-domain closure
resource-bound evidence when the law can amplify or traverse input substantially
```

Pre-admission evidence must expose missing semantics before a historical Catalog membership decision is made. It should
also expose security or V1 feasibility risk. This evidence does not replace the exact semantic definition. If the law is
valid but V1 realization risk remains too high, the candidate may remain `DEFER` without weakening its meaning.

After admission, implementation verification can become more realization-specific:

```text
property and differential tests
hostile-input regression tests
backend preservation checks
performance evidence
recompute-equivalence checks when results are reused
```

These artifacts can reject an implementation or show that a realization is impractical for the intended support
surface. They cannot redefine the admitted law. In particular, they cannot change `E_L` or `C_L`.

Performance engineering belongs to Design or Verification unless a Contract independently makes a distinction visible.
SIMD or caching, for example, do not become Built-In Law meaning merely because an implementation uses them.

---

# 11. Working Sequence

The concrete Catalog work proceeds in this order:

```text
candidate research
    ↓
exact semantic qualification
    ↓
pre-admission evidence review
    ↓
Built-In suitability review
    ↓
Authority uniqueness review
    ↓
explicit Catalog admission decision
    ↓
exact normative law specification
    ↓
public API projection design
    ↓
compiler realization
    ↓
post-admission realization verification
```

Pre-admission evidence does not require the production implementation to exist first. It must prevent membership from
relying on unresolved semantics or ambient provider behavior. It must also expose representation ambiguity before an
implementation assumption can harden into Contract meaning.

Research may expose a missing common Catalog rule. The work returns to ADR-0076 only when that common law must change. A
change to membership semantics or Version ownership is one example. Candidate-specific detail stays in this Design.

Ordinary candidate-specific meaning does not reopen ADR-0076.