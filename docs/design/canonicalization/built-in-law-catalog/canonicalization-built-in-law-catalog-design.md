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

This document is the working Design surface for concrete Canonicalization Built-In Law candidates and the material
needed
to decide which exact laws Kontrakt will actually provide.

ADR-0076 owns the Catalog architecture, membership law, qualification law, Authority boundary, Version boundary, and the
minimum semantic closure required for admission. This Design document does not replace those decisions and does not
create Contract authority merely by listing, naming, implementing, or testing a candidate.

The responsibilities are intentionally separated:

```text
ADR-0076
    Catalog architecture
    Built-In Law Authority membership law
    qualification / admission law
    identity / alias / Version boundaries

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

A candidate becomes a Catalog member only through an explicit admission decision under ADR-0076. A public API symbol,
host-language nominal type, implementation registration, generated evaluator, test corpus, or compiler release does not
perform that admission.

---

# 2. Working Directory Boundary

The current repository organization uses one Canonicalization built-in-law Catalog Design area:

```text
docs/design/canonicalization/built-in-law-catalog/
    canonicalization-built-in-law-catalog-design.md
```

Candidate research, qualification records, admission decisions, API follow-on notes, and verification follow-on notes
remain in this Design area for now. Separate `candidates/`, `admitted/`, `api/`, or `verification/` subdirectories are
not
created until an actual repository-management need appears.

Later file splitting may separate exact normative law specifications, public API design, or verification material
without
changing Catalog membership, law identity, or semantic ownership. The concrete publication format is not decided here.

Physical compiler tables, dense handles, cache keys, evaluator dispatch, persistence, and backend layout remain compiler
Design rather than Catalog semantics.

---

# 3. Candidate Qualification Record

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
    when additional meaning changes E / C / Coverage / Failure

Representative-domain closure
    whether every successful representative remains a legal value
    of the exact semantic presentation promised by the law

External semantic material
    which external distinctions actually determine Contract meaning
    and which provider / library / provenance / verification details do not
    without collapsing Contract determinants, Required Basis, and compiler-side
    Platform Semantic Basis or implementation evidence into one generic basis field

Version / evolution consequence
    what Contract-visible change requires a new law Version

Built-In suitability
    why Kontrakt can legitimately provide this as reusable built-in meaning

Authority uniqueness
    why the candidate is an independent semantic subject
    rather than an alias, source spelling, API synonym, or duplicate Authority
```

Candidate-specific typed structures may be used in Design to represent the exact meaning efficiently. They are not a
parallel semantic authority beside the exact categories above.

The review must also keep semantic qualification distinct from realization evidence:

```text
semantic qualification
    decides what the law means

realizability / verification evidence
    demonstrates that supported realizations can preserve that meaning
```

A law is not weakened because one provider, backend, algorithm, or optimization is inconvenient.

The qualification record must close semantic determinants precisely enough that later compiler producers can declare
exact inputs and validity boundaries. Query keys, dependency edges, result fingerprints, cache schema, generation-local
handles, and persistence metadata remain compiler reuse machinery rather than Built-In Law meaning unless an owning
Contract independently makes a distinction Contract-visible.

---

# 4. Cross-Candidate Review Traps

The following checks apply during candidate research.

A broad standard or library name is not a complete law. Every option, tie-break, ordering rule, unknown-member behavior,
revision-sensitive semantic input, and other branch that can change the representative or owned failure must be closed
by
the exact law.

A representative must remain inside the law's exact semantic presentation domain. If normalization, scale adjustment,
decomposition, expansion, or another representative operation can produce material outside that domain, the candidate
must narrow its operand law, declare Restricted Coverage and exact failure semantics, or prove domain closure.

A mutable ambient `current`, `latest`, provider default, platform registry, host parser, locale, iteration order, cache
state, or implementation choice must not finish the law.

Current semantic equality does not merge independently owned Authorities. Conversely, a different source spelling,
public name, API alias, implementation routine, or documentation label does not justify another Authority by itself.

A candidate that only rejects material, only orders material, only validates legality, or only changes physical storage
does not become Canonicalization unless it also satisfies the representative-selection law owned by ADR-0066.

Candidate labels in this document are working labels only. They are not semantic Authority identities, Version
identities, API promises, package names, persistent keys, or dense handles.

ADR-0066 may use several admitted Exact Built-In Laws inside one finite ordered Canonicalization composition. Candidate
qualification in this document remains per Exact Built-In Law. A legal composition does not create a Catalog member,
merge the constituent Authorities, or move composition legality into this Design area.

---

# 5. Current Candidate Inventory

The working inventory is expanded from standards-oriented candidates to include normalization operations that recur
across general-purpose language libraries, text libraries, search infrastructure, source-control infrastructure, numeric
libraries, and protocol libraries. Ecosystem prevalence is evidence that a semantic subject deserves review. It is not
evidence that a host API already defines the Kontrakt law.

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

No row in this table is a Catalog admission decision. The dotted labels remain review coordinates only. A candidate can
be renamed, split, merged as an API alias onto another Authority, deferred, or rejected before admission when the
qualification work shows that the proposed semantic subject was not exact enough.

## 5.1. Ecosystem Demand Signals

The inventory expansion is based on repeated normalization surfaces in established ecosystems rather than on one
language's convenience API.

| Developer operation                   | Repeated ecosystem surfaces observed during review                                                                                          | Catalog implication                                                                                                  |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| Trim both boundaries                  | Kotlin `trim`, Java `trim` / `strip`, ECMAScript `trim`, Go `TrimSpace`, .NET `Trim`, Apache Commons `trim` / `strip`, Elasticsearch `trim` | Boundary removal is common, but the whitespace set differs and must be explicit                                      |
| Trim one boundary                     | Kotlin `trimStart` / `trimEnd`, Java `stripLeading` / `stripTrailing`, ECMAScript `trimStart` / `trimEnd`, Go left/right trim families      | Start-only, end-only, and both-boundary laws have different representatives                                          |
| Lowercase / uppercase                 | Kotlin, Java, .NET, Go, Rust and search/database text APIs                                                                                  | ASCII and Unicode mappings must be distinct; ambient locale cannot finish the law                                    |
| Caseless matching                     | Python `casefold`, ICU Case Folding, Go `x/text/cases.Fold`, Unicode identifier guidance                                                    | Case folding is a different semantic subject from lowercasing                                                        |
| Unicode normalization                 | Java `Normalizer`, .NET normalization, ECMAScript `normalize`, ICU `Normalizer2`, Unicode-aware language libraries                          | NFC/NFD/NFKC/NFKD are established exact normalization families                                                       |
| Identifier-style Unicode folding      | Unicode `NFKC_Casefold`, ICU NFKC_Casefold                                                                                                  | Standardized combined semantics must not be reconstructed as arbitrary lowercasing plus normalization                |
| Whitespace collapse                   | Apache Commons `normalizeSpace`, Guava trim/collapse helpers, search normalization pipelines                                                | Exact whitespace set, boundary handling, and replacement character must be closed                                    |
| Line-ending normalization             | Git text normalization and cross-platform text tooling                                                                                      | LF representative is a common infrastructure boundary but its accepted source separators must be exact               |
| Decimal representation reduction      | Java `BigDecimal.stripTrailingZeros`, Python `Decimal.normalize`, Rust decimal normalization                                                | Numeric-value equivalence is a strong Canonicalization candidate, with scale-domain closure still required           |
| Digit / width / accent folding        | Elasticsearch normalizers, Lucene ICU and ASCII folding filters                                                                             | Strong usage signal, but broad search folding must be decomposed into exact narrow laws rather than copied wholesale |
| Base16 / Base64 text canonicalization | RFC 4648 and common codec libraries                                                                                                         | Only legal encoded-text-to-encoded-text representative selection belongs here; bytes-to-text encoding does not       |
| UUID / network / language-tag text    | UUID, IPv6, BCP 47 protocol libraries and standards                                                                                         | Protocol profiles remain useful but follow the everyday text/value surface in review priority                        |

The table records demand evidence only. It does not import the semantics of Java, Kotlin, ICU, Elasticsearch, Lucene,
Git, Apache Commons, Guava, Python, or another library. A host method can be evidence that developers repeatedly need a
distinction removed while still being unsuitable as the normative definition of that removal.

## 5.2. Review Priority

The review order is based on a combination of developer frequency, semantic clarity, security impact, and ability to
stress different parts of the Catalog model. It is not a promise that an earlier candidate will be admitted.

```text
boundary whitespace semantics
    ↓
ASCII lowercase / uppercase / case fold
    ↓
Unicode default lowercase / uppercase / full case fold
    ↓
Unicode NFC / NFKC / NFD / NFKD
    ↓
NFKC Case Fold and NFC Case Fold authority-uniqueness review
    ↓
whitespace collapse and LF line endings
    ↓
Decimal Numeric Value
    ↓
decimal-digit / width / diacritic folding
    ↓
Base16 / Base64 / integer textual representatives
    ↓
binary NaN and protocol / identifier profiles
```

This order deliberately starts with operations that ordinary application code frequently performs and then moves into
profiles with stronger external semantic dependencies, parser boundaries, raw-bit constraints, or protocol-specific
meaning.

---

# 6. General Candidate Working Notes

The laws in this section address common text or numeric representation choices. Presence here does not mean
ratification. Each candidate must pass ADR-0076 qualification as one exact Built-In Law Authority or be rejected as an
unnecessary duplicate, over-broad semantic subject, or operation that belongs to another authority.

Text candidates consume the `Text` presentation already established by ADR-0064, which is a sequence of Unicode scalar
values rather than arbitrary JVM UTF-16 code units. Host material outside that legal Input presentation never enters
Canonicalization.

The working labels below are not public API names.

## 6.1. Boundary Whitespace Trim Family

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

The ASCII family cannot inherit the meaning of Java `trim()`, C `isspace`, POSIX blank handling, or another host
predicate by name. Qualification must choose the exact finite ASCII code-point set. In particular, "ASCII whitespace"
and "every code point at or below U+0020" are not synonymous.

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

## 6.2. ASCII Case Conversion and Case Fold

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

## 6.3. Unicode Default Case Conversion and Full Case Folding

Current candidates:

```text
text.unicode.default-lowercase
text.unicode.default-uppercase
text.unicode.default-full-casefold
```

The lowercase and uppercase candidates exist because invariant/default case conversion is a ubiquitous developer
operation. They are not defined by Java's ambient default locale, Kotlin implementation behavior, ICU provider choice,
or the current host Unicode tables.

Qualification must close the exact Unicode Default Case Conversion profile, including full mappings, context-sensitive
rules, expansions, and the Unicode semantic Basis that can change the result. Locale-specific Turkish, Azeri,
Lithuanian, or other language-sensitive case conversion is not silently inherited by these candidates.

Before admission, lowercasing and uppercasing must independently satisfy ADR-0066 idempotence and representative-class
requirements. The fact that a standard library exposes a transformation is not proof that it forms a legal
Canonicalization law under Kontrakt's equivalence model.

The case-fold candidate is not lowercasing. It targets Unicode Default Full Case Folding for locale-independent caseless
matching, including multi-code-point foldings where the Unicode profile requires them. Qualification must distinguish
Default Full Case Folding from Simple Case Folding and Turkic-specific folding. Those are not interchangeable profiles.

Unicode case data is meaning-determining when it changes the exact representative. Provider name or ICU/JDK version is
not itself the semantic Basis.

## 6.4. Unicode Normalization Forms

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
close the exact Unicode Basis dependency rather than inherit host Unicode tables. Normalization stability may preserve
part of the law across later Unicode versions, while unassigned code points and Version evolution still require an
explicit semantic treatment.

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
restricted domains such as identifiers. Qualification must close its exact Unicode Basis, evolution behavior, and
intended operand domain before admission.

### NFKD

NFKD erases the same compatibility distinctions as NFKC and selects the compatibility-decomposed representative.

Qualification must close the same Basis and evolution questions and justify why the decomposed compatibility
representative deserves an independent Built-In Authority rather than remaining a specialized internal-processing
profile.

## 6.5. Unicode NFC Case Fold

**Candidate semantic label:** `text.unicode.nfc-casefold`

This candidate is intended to collapse Unicode canonical-equivalent and default-caseless distinctions under one
independently specified exact law.

It must not be defined merely by the phrase "NFC plus case folding." ADR-0066 now permits finite ordered composition of
admitted Exact Built-In Laws, so qualification has an additional Authority-uniqueness question: does NFC Case Fold have
one independently standardized or otherwise independently owned exact semantic subject, or should ordinary use be
expressed as an explicit composition of separately admitted laws?

If an independent Authority is retained, qualification must close its exact equivalence relation, representative,
case-folding profile, Unicode Basis, unassigned-code-point behavior, and repeated-application stability.
Locale-sensitive
lowercasing is not part of this law.

## 6.6. Unicode NFKC Case Fold

**Candidate semantic label:** `text.unicode.nfkc-casefold`

This candidate targets Unicode `NFKC_Casefold` semantics for identifier-like text. It is not equivalent to arbitrary
`NFKC`, lowercasing, or case-folding calls assembled by an implementation.

The Unicode profile combines compatibility normalization, case folding, and treatment of default-ignorable material.
Qualification must pin the exact Unicode semantic material that determines those mappings and must review the profile as
one complete exact law.

Because Unicode and ICU expose NFKC_Casefold as a distinct semantic operation, this candidate has a stronger independent
Authority case than an ad hoc composition. That observation is evidence for review, not an admission decision.

## 6.7. Unicode Whitespace Collapse to Space

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

## 6.8. LF Line Ending

**Candidate semantic label:** `text.line-ending.lf`

The candidate is motivated by cross-platform text tooling and source-control systems that normalize repository text to
LF. That ecosystem behavior is demand evidence, not the law definition.

The current working relation treats CRLF, standalone CR, and LF as supported line-ending spellings and selects LF as the
representative. Other Unicode line separators such as NEL, LINE SEPARATOR, and PARAGRAPH SEPARATOR are preserved unless
a
later exact profile explicitly admits them.

Qualification must decide the exact source separator set rather than inherit Git, a platform text reader, or a standard
library's newline interpretation. The law does not trim content, collapse blank lines, or normalize other whitespace.

## 6.9. Unicode Decimal Digit Fold

**Candidate semantic label:** `text.unicode.decimal-digit-fold`

Search and text-normalization infrastructure commonly folds native decimal digits into ASCII digits. The narrow
candidate should target Unicode decimal digits only, not every character with a numeric value.

The working representative maps a Unicode scalar with General_Category `Nd` and decimal value `0` through `9` to ASCII
`0` through `9`, preserving every other scalar value.

Qualification must close the exact Unicode property/value material, Version dependence, representative-domain closure,
and the effect of future assigned decimal digits. Roman numerals, vulgar fractions, superscripts, and other numeric
characters are outside the candidate unless explicitly added by a different law.

## 6.10. Unicode CJK Width Fold

**Candidate semantic label:** `text.unicode.cjk-width-fold`

Width folding is used by search systems to remove selected full-width / half-width presentation distinctions. The
candidate must not be defined as "whatever Elasticsearch `cjk_width` or ICU currently does."

Qualification must identify the exact supported mapping relation. In particular, it must distinguish a narrow width law
from full NFKC compatibility normalization, which collapses many more distinctions. Full-width ASCII forms,
half-width Katakana, combining behavior, voiced marks, and every multi-scalar representative case must be closed
explicitly if they belong to the law.

The law requires an explicit Unicode semantic Basis when Unicode data determines the mapping.

## 6.11. Unicode Diacritic Fold

**Candidate semantic label:** `text.unicode.diacritic-fold`

Accent / diacritic folding is common in search, indexing, and application utility libraries, but there is no safe
generic
meaning behind the phrase "strip accents."

Apache Commons, Lucene ASCII folding, and ICU-based search folding demonstrate demand while also demonstrating that
implementations collapse different sets of distinctions. Some approaches use compatibility decomposition; some
transliterate only toward ASCII; some remove combining marks; broad ICU search folding also removes case, width, symbol,
digit, and other distinctions.

Unicode UTR #30 Character Foldings was withdrawn before a final published version. It therefore cannot be cited as an
ambient normative authority that silently completes this candidate.

Before admission this candidate must either:

```text
identify one current normative mapping source that exactly owns the desired relation
or
define a Kontrakt-owned exact finite/versioned mapping law with independently reviewable semantics
```

Until that closure exists, this row is a demand-backed candidate, not an admission-ready law.

## 6.12. Decimal Numeric Value

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

Java `BigDecimal.stripTrailingZeros`, Python `Decimal.normalize`, and comparable decimal libraries provide strong demand
evidence for this relation, but none of those host APIs defines the Contract.

Qualification must resolve representative-domain closure. A host decimal carrier can have a bounded scale even when the
mathematical representative would require a scale outside that carrier's range. Kontrakt must narrow the operand domain,
use Restricted Coverage with exact failure semantics, or choose a semantic Decimal domain whose representative is
closed. The law performs no rounding.

This profile is distinct from monetary scale, currency exponent, market tick, or fixed-precision business rules.

## 6.13. Binary32 Canonical NaN

**Candidate semantic label:** `number.binary32.canonical-nan`

The presentation domain is IEEE 754 binary32 only when raw NaN representation remains Contract-visible at the Input
boundary. The candidate maps every NaN bit pattern to one quiet-NaN representative while preserving every non-NaN bit
pattern, including signed zero.

The current candidate representative is `0x7fc00000`. Qualification must confirm that sign, payload, signaling/quiet
distinction, and the exact raw bits survive every supported Input and backend path before this bit pattern can become
normative law material.

## 6.14. Binary64 Canonical NaN

**Candidate semantic label:** `number.binary64.canonical-nan`

The binary64 candidate follows the same shape as binary32. Every NaN representation maps to one quiet NaN while every
non-NaN bit pattern remains unchanged.

The current candidate representative is `0x7ff8000000000000`. Qualification carries the same raw-bit observability and
carrier-preservation requirements as binary32.

---

# 7. Encoded Text, Numeric Text, Protocol, and Identifier Working Notes

These candidates are common representation profiles whose exact meaning depends on a textual grammar, standard, or
protocol. They remain distinct from bytes-to-text encoding, parsing into a new semantic domain, validation-only rules,
and network/resource resolution.

## 7.1. Base16 Lowercase Text

**Candidate semantic label:** `encoding.base16.lowercase-text`

The operand must already be legal Base16 textual presentation under an exact profile. The candidate does not encode
bytes
into hexadecimal text.

The intended equivalence declares hexadecimal letter case irrelevant and selects lowercase ASCII hex digits as the
representative while preserving the represented bit string.

Qualification must close whether prefixes such as `0x`, separators, whitespace, odd digit counts, or mixed case are
legal
operands. RFC 4648 Base16 has no padding; a broader host parser must not silently widen the law.

## 7.2. Base16 Uppercase Text

**Candidate semantic label:** `encoding.base16.uppercase-text`

This candidate has the same legal encoded-text boundary as the lowercase profile but selects uppercase hexadecimal
letters.

Authority uniqueness must be reviewed explicitly. Lowercase and uppercase select different representatives and therefore
cannot be aliases merely because they preserve the same decoded bytes.

## 7.3. Base64 RFC 4648 Canonical Text

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

## 7.4. Base64url RFC 4648 Canonical Text

**Candidate semantic label:** `encoding.base64url.rfc4648-canonical-text`

Base64url uses a different alphabet from standard Base64 and cannot be treated as an API flag on one ambiguous law.

Qualification must also select an exact padding policy. RFC 4648 permits referring specifications to omit padding in
specific circumstances, so "Base64url" alone does not close one representative. If more than one materially different
profile is required, this candidate must split before admission.

## 7.5. Decimal Integer Text Canonical Form

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

## 7.6. IPv6 RFC 5952 Text

**Candidate semantic label:** `network.ipv6.rfc5952`

The candidate domain is legal textual IPv6 presentation without an external zone identifier. Alternate legal spellings
are equivalent when they denote the same IPv6 address, and RFC 5952 supplies the target text form.

Qualification must keep Input legality separate from the candidate's canonicalizable domain. If the selected Input is
broader legal Text, text that is not a legal IPv6 presentation lies outside this candidate's canonicalizable domain and
requires exact Canonicalization Coverage / failure treatment. The exact grammar interpretation must not be inherited
from
a host parser. Embedded IPv4 forms, zero-run tie breaking, leading zeros, letter case, and zone identifiers must be
reviewed explicitly.

The law performs no DNS lookup and does not resolve interface scope.

## 7.7. BCP 47 Language Tag

**Candidate semantic label:** `identifier.bcp47.rfc5646`

The candidate domain is a well-formed BCP 47 language tag under one exact Kontrakt profile.

RFC 5646 supplies syntax and canonicalization rules, while IANA Language Subtag Registry data becomes
meaning-determining wherever the representative consumes registry fields such as Preferred-Value or deprecated
relations.

Qualification must close the exact registry Basis and evolution consequences. Grandfathered tags, extensions,
private-use material, suppress-script behavior, deprecated subtags, and unknown future registrations cannot be left to a
mutable current registry or host library.

## 7.8. UUID Lowercase Text

**Candidate semantic label:** `identifier.uuid.rfc9562-lowercase-text`

The candidate domain is one exact standard textual UUID form. Hexadecimal letter case is declared irrelevant and
lowercase text is the representative.

Qualification must confirm the accepted grammar, delimiter positions, optional wrappers or URN prefixes, version/variant
text treatment, and whether alternative textual forms belong to the operand at all. The law changes neither UUID bits
nor
version or variant meaning.

A structured UUID value that no longer contains textual case does not need this law.

---

# 8. Deferred Profile Working Notes

The following profiles are plausible or commonly requested transformations but are not part of the current
admission-ready set. Moving one into the built-in Catalog requires explicit qualification and a Catalog admission
decision under ADR-0076. A new ADR is required only if that work changes the Catalog architecture or qualification law.

## 8.1. Unicode Stabilized Normalization

Unicode Stabilized Strings reject code points that are unassigned in the selected Unicode version so that a successful
normalization result remains stable across version evolution.

Stabilized NFC and NFKC remain deferred until their exact refusal relation and semantic-Basis requirements are closed at
the Catalog level. ADR-0066 owns how those closed requirements are represented and established downstream.

## 8.2. Locale-Sensitive Case Conversion

Locale-sensitive lowercasing or uppercasing is common in user-facing text but cannot inherit the process locale,
default JVM locale, request locale, or UI locale as hidden meaning.

A future law must identify the exact locale/profile as Contract-visible semantic material and prove that the resulting
mapping still satisfies Canonicalization's representative and idempotence laws. It remains deferred from the basic
locale-independent surface.

## 8.3. Turkic Case Folding

Unicode defines Turkic-specific case-fold behavior distinct from Default Case Folding. It is not silently selected by
language environment or locale.

A future profile must make that distinction explicit and independently justify Built-In suitability. It remains
separate from `text.unicode.default-full-casefold`.

## 8.4. Broad Search Folding

Lucene ICU folding and similar search pipelines intentionally erase many distinctions at once, including combinations of
case, accent, diacritic, width, symbol, digit, spacing, and compatibility differences.

That breadth is useful for search but is too large to import as a default Canonicalization law merely because a SOTA
search library provides it. Narrow constituent laws are reviewed separately above. A broad search-fold Authority remains
deferred until its exact purpose, relation, representative, Unicode Basis, and security consequences are independently
justified.

## 8.5. Signed-Zero Collapse

A binary floating-point law may declare positive and negative zero equivalent, but IEEE 754 behavior can later observe
the sign. V1 therefore defers this law until those semantic consequences are reviewed explicitly.

## 8.6. RFC 3986 URI Syntax Profile

RFC 3986 defines syntax-level normalization, but URI equivalence depends on purpose and can become scheme-specific. V1
therefore publishes no generic `UriCanonicalization`.

A later law must identify the exact syntax-level or scheme-specific equivalence it owns.

## 8.7. IDNA and PRECIS Profiles

IDNA and PRECIS combine representative formation with legality rules. A future Kontrakt profile must split those
responsibilities according to Kontrakt authority rather than import an external pipeline as one opaque law.
Representative formation may belong to Canonicalization while Input or Admission owns the corresponding legality rule.

## 8.8. Instant-Preserving Temporal Profiles

A temporal law may declare two offset date-time presentations equivalent when they denote the same instant and select a
UTC representative. Such a law is valid only when the original civil-time context is explicitly irrelevant.

V1 therefore publishes no generic `DateTimeCanonicalization`.

---

# 9. Admission Decision Record

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
treats Built-In Law Authority Membership as historical and monotonic. Later lifecycle, selection, support, or Governance
rules may restrict future use without erasing or reassigning that membership.

A new exact Version under an already-admitted Authority does not create another Catalog member.

---

# 10. API and Compiler Design Follow-On

Concrete API design begins only from admitted or deliberately prototyped candidate meaning. Public design may include:

```text
IDL-facing spelling
generated API projection
Java / Kotlin nominal type spelling
source aliases that resolve to one exact Authority
Version-reference surface where required
compiler lookup and dispatch
dense-handle mapping
storage / table layout
specialized realization
quick checks and zero-copy fast paths
cache and persistence strategy
```

None of these artifacts owns Catalog membership or exact law meaning.

Source aliases must resolve to the same exact Authority rather than mint duplicate Catalog members. Structural equality,
hash equality, shared implementation code, or current extensional equality cannot infer aliasing.

Compiler realization may physically fuse Catalog lookup, law material, evaluator selection, or downstream execution when
profitable, provided logical Authority, Version, and observation boundaries remain recoverable.

---

# 11. Qualification Evidence, Verification, and Performance Follow-On

Admission must not precede evidence needed to determine whether the proposed law is semantically closed, independently
checkable, secure against known representation ambiguity, and realistically supportable in V1. Pre-admission
qualification evidence is therefore part of candidate review when relevant.

That evidence may include:

```text
normative examples and official conformance material
independently derived reference or conformance vectors
security and parser / provider differential cases
representative-domain closure checks
worst-case output amplification analysis
worst-case traversal and intermediate-material analysis
reference-data size and access analysis
streaming / bounded-work analysis where relevant
```

The purpose of pre-admission evidence is to expose missing semantics, unsafe ambiguity, or V1 feasibility risk before a
historical Catalog membership decision is made. It does not replace the exact semantic definition. If the exact law is
valid but V1 realization risk remains too high, the candidate may remain `DEFER` without weakening its meaning.

After admission, implementation verification may add:

```text
property-based tests
differential tests
hostile-input regression corpora
reference implementation where useful
backend preservation checks
performance and profiling evidence
provider-conformance checks
cache / clean-recompute equivalence checks where the result is reused
```

These artifacts can reject an implementation or expose that an admitted realization is not practical for the intended
support surface. They do not silently redefine `E_L`, `C_L`, Coverage, Failure, or semantic determinants.

Performance thresholds, scratch-space strategies, provider selection, SIMD/vectorization, memoization, caching, and
physical layout belong to Design or Verification unless a distinction is independently Contract-visible meaning.

---

# 12. Working Sequence

The concrete Catalog work proceeds in this order:

```text
candidate research
    ↓
exact semantic qualification
    ↓
pre-admission qualification evidence
    normative / conformance / security / feasibility review
    ↓
Built-In suitability / Authority uniqueness review
    ↓
explicit Catalog admission decision
    ↓
exact normative law specification
    ↓
public API projection design
    ↓
compiler realization
    ↓
post-admission conformance / adversarial / performance verification
```

Pre-admission evidence does not require the production implementation to exist first. It must be sufficient to prevent a
historical Catalog membership decision from relying on unresolved semantics, ambient provider behavior, unexamined
representation ambiguity, or an implementation assumption that later becomes Contract meaning.

Research may expose a missing common Catalog rule. In that case the work returns to ADR-0076 only if the missing rule
changes Catalog architecture, qualification law, membership semantics, identity, Version, or another ADR-owned boundary.

Ordinary candidate-specific meaning does not reopen ADR-0076.