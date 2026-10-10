# Canonicalization Built-In Law Catalog Design and Candidate Qualification

## Status

Draft

## Date

2026-10-10

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

This Design records the original 75 Canonicalization Built-In Law candidates and the separately admitted
CRLF Line-Ending Law, for 76 reviewed subjects. It preserves the domain research and counterexamples
behind their decisions and the exact `Initial` specifications of admitted laws.
ADR-0076 owns Catalog membership and qualification; ADR-0066 owns the common Canonicalization law.
Each admitted Exact Built-In Law Authority owns its versioned meaning. Sections 5–7 preserve research
and unresolved domain boundaries; Section 8 owns the final dispositions and normative specifications.
API projection, compiler realization, and implementation conformance remain downstream work.

---

# 2. Candidate Qualification Record

The following fields guide candidate-specific research rather than duplicate ADR-0066 or ADR-0076.
Section 8 records the actual meaning, evidence, and disposition of each reviewed candidate.

## 2.1. Normative Source and Scope

Identify the domain authority and the exact publication or immutable source profile, accounting for any
material errata or superseding standard. Record unresolved choices that could affect meaning. Host libraries
and permissive parsers are comparative evidence, not normative sources. Base32 padding and HTTP-date
interpretation illustrate why a fixed source profile matters (Sections 6 and 8).

**References:** ADR-0076 Sections 5.1–5.2, 5.4; candidate-specific standards and distinctions in
Sections 5–8.

## 2.2. Exact Semantic Closure

For each candidate, identify the Input-owned presentation and its exact equivalence relation `E_L`. State the
representative `C_L`. Record Total or Restricted Coverage, including any Law-owned refusal. Section 8 contains
the approved meanings and their evidence; this section does not restate the common Canonicalization rules.

**References:** ADR-0076 Sections 5.2 and 6.1.1–6.1.2 (law-specific content); ADR-0066 Sections 4.4,
6.1, 8, and 12 (common obligations and refusal).

## 2.3. Semantic Determinants, External Material, and Evolution

Identify any external semantic material that actually determines the candidate's result. A fixed standard
profile belongs to the Law; an occurrence-specific determinant may require an explicit Required Basis. Record
unresolved source or mapping choices with the disposition in Section 8. The two deferred candidates are
explained in Section 8.2.

**References:** ADR-0076 Sections 5.1, 5.4, and 14 (determinants and evolution); ADR-0066 Sections
6.1 and 11 (basis and determinism); ADR-0053 Section 3 (Version authority).

## 2.4. Finite Work and V1 Realizability

Justify finite work for the candidate's exact input. For structured values, account for traversal depth. Where
a transformation expands its data, explain the expansion and its resource implications. Record unresolved V1
feasibility with the disposition. Resource limits cannot change Law meaning. Relevant evidence appears in
Sections 8.6.9 and 8.13.10.

**References:** ADR-0066 Section 7 (finite-work law); ADR-0076 Section 5.3 (resource-boundary
qualification). Security and evidence criteria remain in Sections 2.6–2.7.

## 2.5. Built-In Suitability and Authority Uniqueness

Explain why the candidate requires its own Built-In Authority instead of reusing an admitted Law or lawful
composition. A different standard name is insufficient. Section 8.3 rejects the duplicate case-only MAC-48,
UUID and DOI Authorities. Sections 8.4–8.5 justify the independently admitted ASCII representatives and the
limited boundary-trim exceptions. Section 8.11 admits separate LF and CRLF representatives
under one exact newline-equivalence relation.

**References:** ADR-0076 Section 5.5; ADR-0066 Section 4.4.1; decisions in Sections 8.3–8.5.

## 2.6. Security Qualification

Security research is relevant where a proposed equivalence can erase a distinction required by
a protocol or by a later consumer. Record the concrete risk and a discriminating witness, rather
than repeat the common security and failure rules. Relevant evidence includes:

- **Parser disagreement and double decoding:** strict versus permissive syntax, `%2F` versus `/`,
  encoded dot segments, HTTP request smuggling, and `CWE-180` / `CWE-174`
  (Sections 6.9, 6.21–6.22, 8.7).
- **Incorrect identity collapse:** NaN payloads, signed zero, URI routing, MAC-48/UUID/DOI
  Authority duplication, and case or whitespace distinctions in identifiers (Sections 5, 6, 8).
- **Authenticated representations:** original octets or text required by a signature, MAC,
  digest, or protocol transcript; Base64url and RFC 7468 are contrasting cases (Sections 6.3–6.4,
  6.32, 8.3, 8.13).
- **Untrusted input and external material:** adversarial size, deep structure, mutable registries,
  time-dependent interpretation, and diagnostic disclosure (Sections 6.12, 6.18, 6.30, 6.33, 8.2).

Record a nearby non-equivalent case as well as a permitted equivalent pair wherever an unsafe
collapse or parser differential is plausible. Security evidence can limit applicability or justify
refusal or deferral; it does not itself define an extra equivalence. Constant-time handling and
sensitive-data disclosure require review when the specific operand or consumer makes them relevant.

**References:** Kontrakt Security Architecture; ADR-0066 Sections 4.3–4.5, 12; ADR-0076
Sections 5.2–5.3. Domain-specific security evidence remains in Sections 5–8.

## 2.7. Evidence and Admission Readiness

Retain evidence that can falsify a candidate's claims. Use the normative specification and, where available,
official conformance vectors. Add an independent reference calculation or a malformed-input counterexample
where it tests a material distinction. Adversarial and cross-version tests are needed only when the candidate
raises those risks. Bind the evidence to the exact Law Version. Functional tests alone cannot settle parser
disagreement or finite-work feasibility.

Sections 8.4–8.13 record pre-admission arguments and explicit decisions. Section 8.2 explains
why the BCP 47 registry-dependent and Kubernetes Quantity candidates are still deferred.
A production backend's separate conformance evidence belongs to Verification, not to historical
Catalog membership.

**References:** ADR-0076 Sections 5–7 and the Section 8 membership decisions; ADR-0066
Sections 7–8. Post-admission realization checks are summarized in Section 10.

---

# 3. Cross-Candidate Review Traps

Three problems recur in the candidate reviews. First, validity is not representative selection: strict Base64
validation does not itself justify a Canonicalization Law (Sections 6.1–6.6). Second, a representative must
stay in its exact domain; the Decimal-Comma rejection illustrates this problem (Sections 5.16 and 8.3). Third,
sharing a case-conversion operation does not establish a new Authority. The MAC-48, UUID and DOI rejections
document that case (Section 8.3). A protocol's comparison rule must still be respected, even when generic Text
comparison looks similar.

The remaining recurring risks concern disagreement among external parsers and repeated percent-decoding. Some
protocols also authenticate the original representation. Sections 5–8 retain concrete counterexamples. The
general rules for provenance and composition belong to ADR-0066 and ADR-0076.

---

# 4. Current Candidate Inventory

The original 75 candidates have final dispositions. A separately reviewed CRLF Line-Ending Law
is also formally admitted. Across all 76 subjects, there are **62 formal `ADMIT`, two `DEFER`, and
twelve `REJECT`**. There are no outstanding `ADMIT (proposed)` records. Sections 8.2–8.13 own the decisions; Sections
4–7
preserve their research background. Investigating a new candidate does not confer Catalog membership.

## 4.1. Demand and Standard Signals

The inventory does not use one evidence source for every domain. General text candidates are often motivated by repeated
developer practice. Protocol candidates are stronger when the owning standard already defines equivalence or a preferred
representation.

| Candidate area            | Representative evidence                    | Catalog implication                                                                         |
|---------------------------|--------------------------------------------|---------------------------------------------------------------------------------------------|
| Boundary trim and case    | Kotlin, Java, ICU                          | Repeated use justifies review, but the host operation does not define the law               |
| Line-ending text          | WHATWG Infra; RFC 5198; Git                | LF and CRLF may select different representatives for one explicit newline-equivalence law   |
| Unicode normalization     | Unicode normalization forms                | The external semantic profile is already precise enough to ground exact review              |
| Encoded text              | RFC 4648                                   | Canonical encoded text must remain separate from bytes-to-text encoding                     |
| Numeric text              | Decimal and scientific notation practice   | Grouping, radix, scale markers, and exponent spelling need exact self-contained grammars    |
| Bit representation        | Two's-complement and fixed-width bit forms | Numeric value, byte sequence, and fixed-width bit-vector meaning must remain distinct       |
| Historical IPv4 text      | BSD/Unix `inet_aton` numbers-and-dots      | Legacy multi-radix and abbreviated forms need an explicit profile before normalization      |
| HTTP-date                 | RFC 9110                                   | Multiple accepted date spellings have one required generated form                           |
| HTTP and CoAP URIs        | RFC 9110 and RFC 7252                      | Scheme-specific standards already define important normal-form choices                      |
| `tel:` URI                | RFC 3966                                   | Visual separators and parameter ordering have protocol-defined comparison rules             |
| Network prefixes          | RFC 9911                                   | Clearing non-prefix bits gives an exact representative problem                              |
| International identifiers | DOI and ISO-backed identifier profiles     | Identifier equivalence must be separated from validation and registry ownership             |
| Retail identifiers        | GS1 Digital Link and GTIN rules            | Shorter GTIN forms can map to one 14-digit representation under an exact profile            |
| Cloud-native quantity     | Kubernetes Quantity                        | The API datatype already distinguishes legal non-canonical text from canonical output       |
| Software supply chain     | ECMA-427 Package URL                       | Core syntax and type-specific normalization must not be collapsed into one hidden rule      |
| Security textual encoding | RFC 7468                                   | Parser tolerance and generator form are distinct and can expose a representative problem    |
| Schema meaning            | Avro Parsing Canonical Form                | Domain semantics, not raw JSON spelling, determine which schema distinctions are irrelevant |

A standard name is still not enough. The qualification record must identify the exact semantic subject that Kontrakt
would own. It must also identify which parts remain owned by the external protocol or registry.

## 4.2. Review Priority

The initial review began with self-contained Text and encoded-text Laws, then examined Unicode and protocol
syntax. Numeric and bit representations followed, before identifier and structured-domain candidates. This
sequence guided research; it establishes no Authority hierarchy. All 75 candidates have been reviewed. Section
8.2 records outstanding evidence for the deferred cases.

## 4.3. Domain Coverage Discovery

Sections 5–6 cover the initial candidates across Text, numeric, network, identifier and structured-schema
domains. The sweep also exposed additional subjects outside the initial 75, such as RFC 3339/9557 and RDF
canonicalization. Section 7 preserves the reasons these subjects remain outside the initial dispositions.

Coverage of one representative does not imply completeness of its entire domain. Candidate
selection follows the independently justified semantic subject, not the number of APIs in an
ecosystem.

---

# 5. General Candidate Working Notes

This section records research on Text and numeric representation. Its working notes do not confer membership.
Sections 8.4–8.13 own the final admissions and exact meanings. Earlier open questions remain here as research
history, not as unresolved obligations of admitted Laws. Rejection grounds are recorded with the affected
candidates. The labels below are not public API names.

Text candidates consume the `Text` presentation already established by ADR-0064, which is a sequence of Unicode scalar
values rather than arbitrary JVM UTF-16 code units. Host material outside that legal Input presentation never enters
Canonicalization.

The working labels below are not public API names.

## 5.1. Boundary Whitespace Trim Family

Section 8.4 formally admits the three ASCII trim forms with complete initial-version normative specifications.
Section 8.5 admits the corresponding Unicode trim Authorities. The original research below
records the reasons considered during qualification. None of these descriptions fixes a public API name.

General-purpose APIs expose all three trimming behaviors, so each has an observable use case. The
review must not merge them simply because one scanner can implement every behavior.

The ASCII family cannot inherit a host predicate by name. Java `trim()` is one example of behavior that must not define
the Contract. Qualification must choose the exact finite ASCII code-point set. In particular, "ASCII whitespace" and
"every code point at or below U+0020" are not synonymous.

The Unicode family is intended to use an exact Unicode whitespace property under an explicit Unicode semantic Basis.
Qualification must decide whether the candidate is defined by the Unicode `White_Space` property or by another exact
Unicode set. The property choice and any Version-sensitive membership are law meaning.

For every family member, interior characters remain unchanged. A start-only law does not remove trailing whitespace; an
end-only law does not remove leading whitespace; a both-boundary law removes both. The admitted ASCII boundary law is
an expressly approved independent Built-In Authority despite being exactly expressible as a composition of the two
one-sided laws. The matching Unicode exception is approved in Section 8.5.4. Neither admission is a general exemption
for
other composition-equivalent candidates.

Empty input and all-whitespace input require explicit representative treatment. Section 8.4 fixes the empty Text
representative for those cases under each admitted ASCII trim law. The exact Unicode whitespace set, Version-sensitive
meaning, and corresponding representatives are
formally fixed for the initial Unicode trim Versions in Section 8.5.

## 5.2. ASCII Case Conversion and Case Fold

Section 8.4 formally admits the ASCII lowercase and uppercase laws as independent Built-In Authorities.
The ASCII case-fold proposal remains `REJECT`ed as an independent Built-In Authority because it
duplicates the admitted lowercase law. These labels are not public API names.

The lowercase candidate maps ASCII `A` through `Z` to `a` through `z` and preserves every other Unicode scalar value.
The uppercase candidate applies the inverse case-direction mapping to ASCII letters and likewise preserves every other
scalar value.

The [WHATWG Infra Standard](https://infra.spec.whatwg.org/#ascii-lowercase) defines ASCII lowercase and uppercase
as separate operations. It also defines ASCII case-insensitive matching by equality after ASCII lowercasing. For these
candidates, the equivalence relation can be stated without invoking a conversion procedure: two `Text` values are
equivalent exactly when corresponding Unicode scalars are identical or differ only as one of the 26 ASCII upper/lower
letter pairs. The scalar sequences must have the same length. All non-ASCII scalars remain distinct unless already
identical.

Both case Laws use the same equivalence relation, but choose different representatives: `"AbC"` becomes
`"abc"` or `"ABC"`. Non-ASCII case variants such as `İ` remain untouched. Both mappings preserve scalar count
and are idempotent. Coverage is Total, with no Law-owned refusal. Neither mapping depends on Unicode tables or
the host locale.

The common equivalence does not automatically merge the candidates into one Authority. Their representative
obligations are different and cannot be substituted for one another. This justifies the two independently admitted
law subjects in Section 8.4; separate API names alone would not have justified the decision. The relation is
appropriate only where ASCII letter case is declared irrelevant.
For example, a protocol's case-sensitive command or an identifier requiring exact spelling must not inherit it merely
because another protocol uses case-insensitive names. Downstream judgments must use the established representative,
not an alternate Unicode or locale-sensitive case conversion.

The ASCII case-fold proposal declares ASCII letter case irrelevant and selects lowercase ASCII as
its representative. Section 8 records that this duplicates the admitted ASCII lowercase law's exact
meaning. It therefore does not justify a separate Catalog Authority. An explicit API alias may
still project that Authority under the downstream Authoring API rules.

None of the ASCII candidates owns locale behavior or Unicode case data.

## 5.3. Unicode Default Case Conversion and Full Case Folding

The Unicode default lowercase and uppercase candidates have `REJECT` review outcomes as independent
Canonicalization Built-In Laws in Section 8. Default full case folding was subsequently admitted in Section 8.6.
These names do not determine public API spelling.

The lowercase and uppercase candidates exist because invariant case conversion is a ubiquitous developer operation.
Their meaning cannot come from ambient host behavior. Java's default locale is one example. The host's current Unicode
tables are another.

Unicode Default Case Conversion has standard-defined full mappings and context-sensitive rules. A precise realization
would also require the Unicode semantic data that determines the result, rather than the host's ambient tables.
Locale-specific conversion is separate; Turkish casing is a representative example.

The reviewed operations are useful case conversions, but neither is the representative of Unicode Default Caseless
Matching. Lowercase can leave caseless-equivalent strings distinct, such as `ß` and `ss`. Uppercase can merge strings
that Default Caseless Matching distinguishes, such as ASCII `i` and U+0131 DOTLESS I. Equal conversion output could
be used to define a mathematical equivalence relation, but it would not by itself justify an independently owned
same-meaning relation for the general `Text` domain. That semantic ownership gap is the reason for `REJECT`, not an
assertion that these conversions are nondeterministic or unusable outside Canonicalization. A narrower, independently
justified semantic subject would require a new candidate review.

The case-fold candidate is not lowercasing. It targets Unicode Default Full Case Folding for locale-independent caseless
matching, including multi-code-point foldings where the Unicode profile requires them. Qualification must distinguish
Default Full Case Folding from Simple Case Folding and Turkic-specific folding. Those are not interchangeable profiles.

Unicode case data is meaning-determining when it changes the exact representative. Provider name or ICU/JDK version is
not itself the semantic Basis.

## 5.4. Unicode Normalization Forms

The exact initial NFC, NFD, NFKC, and NFKD laws and their membership decisions appear in
Section 8.6. The following candidate research remains for traceability.

The four normalization-form candidates are Unicode NFC, Unicode NFD, Unicode NFKC, and Unicode NFKD.
These are descriptive review names, not public API spellings.

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

The exact initial law and membership decision appear in Section 8.6. The working notes below
record the preceding qualification, not an outstanding admission decision.

This candidate is intended to collapse Unicode canonical-equivalent and default-caseless distinctions under one
independently specified exact law. Section 8.6 records its later formal Catalog admission.

Unicode D145 defines Canonical Caseless Matching using canonical decomposition and Default Full Case Folding. The
working equivalence compares the NFD forms obtained after folding canonically decomposed inputs. The proposed NFC
representative applies NFC after folding the canonically decomposed input. This must not be shortened to an
unspecified "NFC plus case folding" operation: the order and the Unicode semantic Basis are part of the exact meaning.
Locale-sensitive lowercasing is not part of this law.

ADR-0066 permits finite ordered composition of separately admitted laws, but it also permits an independently admitted
curated combined Built-In Law. A standardized, repeatedly useful canonical-caseless relation can justify one
independently selectable and versioned Law Authority even where a lawful composition could produce the same meaning.
The candidate is not admitted merely as a convenience alias; its exact relation and representative must be owned by
that Authority rather than inherited from runtime composition.

These were pre-admission questions. Section 8.6 owns the approved Unicode Basis, formal
representative relation, and recorded verification argument. A production realization must still
pass its own version-pinned conformance and bounded-work verification.

## 5.6. Unicode NFKC Case Fold

The exact initial law and membership decision appear in Section 8.6. The working notes below
record the preceding qualification, not an outstanding admission decision.

This candidate targets Unicode `NFKC_Casefold` semantics for identifier-like text. It is a defined semantic operation,
not an arbitrary composition chosen by an implementation. In particular, ordinary lowercasing cannot stand in for it.
Section 8.6 records its later formal Catalog admission.

Unicode R5 defines `toNFKC_Casefold`, while D147 defines Identifier Caseless Matching using that operation after
canonical decomposition. The working equivalence compares `toNFKC_Casefold(NFD(x))` results. The proposed
representative is that same exact Unicode-defined form. The preliminary NFD step must not be omitted when the law
claims D147 equivalence. The exact Unicode data and profile that determine these results must be fixed by the Law
Version, not inherited from a host library.

The Unicode profile combines compatibility normalization with case folding. It also defines how default-ignorable
material is treated. This provides useful, independently standardized identifier-caseless meaning that a generic
composition of NFKC and Full Case Folding must not silently claim to reproduce. ICU support is implementation and
demand evidence, not authority to change the Unicode profile.

The proposed operand remains already-established `Text`; this law does not establish that a Text value is a valid or
secure identifier. Its use in an identifier domain must be selected explicitly. A legality rule that needs to observe
an original distinction erased by this law must already be established at an authorized earlier boundary; otherwise
the law must not be selected for that input. Admission receives the representative and cannot restore erased Input
distinctions. In particular, compatibility characters and default-ignorable code points can collapse to the same
representative as otherwise distinct input. Signing, authorization, and protocol validation must not silently import
this equivalence.

These were pre-admission requirements. Section 8.6 owns the approved semantic profile and
its admission evidence. A provider API remains insufficient as independent conformance evidence.

## 5.7. WHATWG ASCII Whitespace Collapse

Whitespace collapse is common in application text processing. The earlier Unicode-wide whitespace-collapse proposal
remains historical research; it is no longer an active candidate in Section 8. Its scope could erase distinctions among
Unicode separators without a sufficiently justified general Built-In meaning. The replacement candidate follows the
WHATWG Infra Standard's `strip and collapse ASCII whitespace` algorithm. Five code points belong to **one** proposed
Built-In Law, not five independent Authorities.

Normative source: WHATWG Infra, §4.7, [`ASCII whitespace`](https://infra.spec.whatwg.org/#ascii-whitespace) and
[`strip and collapse ASCII whitespace`](https://infra.spec.whatwg.org/#strip-and-collapse-ascii-whitespace).

The operand is an already-established Input `Text`, interpreted as Unicode scalar values under ADR-0064. The exact set
`W` is fixed to the following five code points:

```text
U+0009  TAB
U+000A  LINE FEED
U+000C  FORM FEED
U+000D  CARRIAGE RETURN
U+0020  SPACE
```

`E_L` relates two Text values exactly when the ordered sequences of their nonempty maximal runs of scalars outside `W`
are identical. This declares the kind and length of each intervening `W` run irrelevant, including runs at either
boundary. It does not collapse any distinction between scalars outside `W`. In particular, removing a `W` run between
two nonempty runs would change the relation: `"AB"` is not equivalent to `"A B"`.

`C_L` joins the non-`W` runs with exactly one U+0020 SPACE between adjacent runs. All leading and trailing `W` scalars
are removed. An empty operand, or one containing only `W`, has the empty `Text` representative. This fixes the
representative without consulting a host whitespace predicate. It gives one representative per equivalence class;
`C_L` is idempotent and remains in the same `Text` domain.

Representative Coverage is Total for legal established Input `Text`. The law does not own a refusal for any such
operand. A single finite scan suffices, and the representative cannot contain more scalar values than the operand.
The fixed literal set and algorithm determine the proposed Law Version. A later change to the WHATWG Living Standard
cannot silently change that Version's meaning. Host locale, JDK or ICU Unicode data, and mutable external registries
are not semantic determinants.

For example, `"  A\t B\r\nC  "` becomes `"A B C"`. The escape notation in this example denotes the respective
control characters. U+000B VERTICAL TAB, U+00A0 NO-BREAK SPACE, and U+2028 LINE SEPARATOR remain untouched.
The admitted ASCII trim laws use a six-character set that includes U+000B; those laws
must not be substituted for this exact five-character relation.

This law intentionally erases line and control-character distinctions. It is unsuitable wherever such characters
separate fields, records, or security-relevant protocol elements. WHATWG's generic ASCII whitespace set must not be
mistaken for XML, JSON, or HTTP whitespace grammar. Canonicalization cannot repair illegal Input or replace protocol
validation. When selected, Admission judges the established representative, and Lowering consumes that same meaning.
Raw provenance may support diagnostics but cannot reintroduce an erased distinction as a later semantic determinant.

The admitted boundary-trim laws do not collapse interior whitespace. The standardized algorithm supplies a
reusable reason to examine an independent Built-In. Nevertheless, ADR-0066 composition and Authority uniqueness must
were checked before formal Catalog admission. Section 8.11 records this Law's approved `Initial` specification,
qualification basis, and explicit membership decision.

## 5.8. LF and CRLF Line-Ending Representatives

Cross-platform text tools and source-control systems motivate line-ending normalization. Git commonly
uses LF in repository content and can select CRLF in a working tree. These practices support separate
representative choices; they do not define universal protocol equivalence. WHATWG Infra's
[`normalize newlines`](https://infra.spec.whatwg.org/#normalize-newlines) specifies the LF form.
[RFC 5198 §2](https://www.rfc-editor.org/rfc/rfc5198.html#section-2) requires CRLF where its
Net-Unicode profile uses lines. It does not authorize every protocol to accept standalone CR or LF.

Both admitted Laws treat CRLF as one line break. A standalone CR or LF is also one line break. The
same input may contain any mixture of these forms. Neither Law changes other characters or the
number of line breaks. U+0085, U+2028, and U+2029 retain their exact values.

The LF Law selects U+000A for every line break. The CRLF Law selects U+000D U+000A. For example,
`"A\r\nB\rC\nD"` becomes `"A\nB\nC\nD"` under LF and
`"A\r\nB\r\nC\r\nD"` under CRLF. A `CR` followed by `CRLF` represents two breaks,
not one. The exact shared equivalence and separate representatives are fixed in Section 8.11.

Both Laws have Total Coverage over their admitted legal `Text`. CRLF can produce more scalars than
its operand. This does not introduce a Law-owned length limit or refusal. Admission judges the
established representative where a continuation policy applies. Budget and Capacity retain their
resource responsibilities. Selection must still satisfy the Input presentation's own semantic
applicability requirements under ADR-0064 and ADR-0066.

## 5.9. Unicode Decimal Digit Fold

The exact law and initial membership decision appear in Section 8.5. The candidate research
that follows remains as the original qualification record.

Search and text-normalization infrastructure commonly folds native decimal digits into ASCII digits. The narrow
candidate should target Unicode decimal digits only, not every character with a numeric value.

The working representative maps a Unicode scalar with General_Category `Nd` and decimal value `0` through `9` to ASCII
`0` through `9`, preserving every other scalar value.

Qualification must close the exact Unicode property and value mapping. It must also settle Version dependence and prove
that the representative stays in the promised domain. Other numeric characters remain outside this candidate; Roman
numerals and vulgar fractions are two examples.

## 5.10. Unicode CJK Width Fold

Width folding is used by search systems to remove selected full-width / half-width presentation distinctions. The
candidate must not be defined as "whatever Elasticsearch `cjk_width` or ICU currently does."

Section 8 records `REJECT` for this broad independent Built-In Law. Unicode width properties and compatibility
mappings do not by themselves define one narrow CJK width-fold equivalence and representative. The proposal leaves
open which full-width ASCII and half-width Katakana distinctions are removed and how multi-scalar cases involving
combining or voiced marks are handled. Applying NFC to resolve those cases could also remove unrelated canonical
distinctions. An implementation's current behavior cannot settle that Contract meaning.

The rejection does not imply that width conversion is nondeterministic. Exact width mappings and combining
rules could support separate full-width ASCII or half-width Katakana profiles. Each would still need an
independently justified equivalence relation and security review. Neither may silently inherit the broader
NFKC relation. Such research does not reopen this rejected candidate.

## 5.11. Unicode Diacritic Fold

Accent and diacritic folding is common in search and application libraries. There is still no safe generic meaning
behind the phrase "strip accents."

Apache Commons and Lucene show that developers need diacritic-related transformations, but their
behavior does not define one shared law. Some implementations decompose text before removing marks.
Others transliterate instead. Broad ICU search folding erases additional distinctions and therefore
cannot silently define this candidate.

Unicode UTR #30 Character Foldings was withdrawn before a final published version. It therefore cannot be cited as an
ambient normative authority that silently completes this candidate. Neither the Unicode `Diacritic` property nor a
general combining-mark category establishes that removing those characters preserves meaning. The distinction may be
linguistically or security-significant in the original Text domain.

Section 8 records `REJECT` for the generic independent Built-In proposal. It cannot claim one standards-defined
equivalence and representative merely from the prevalence of accent-insensitive search or host folding functions.
Deterministic mark removal would not resolve this semantic ownership defect.

A future narrowly scoped law would need either:

```text
one current normative mapping source that exactly owns the desired relation
or
one independently justified Kontrakt-owned finite/versioned mapping with reviewable semantic meaning
```

Such a law would require separate operand, representative, and security qualification. This possibility does not
change the rejection of the present generic proposal.

## 5.12. Decimal Numeric Value

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

The Java `BigDecimal` cohort and stripping behavior are documented by the
[Java Platform API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/math/BigDecimal.html).

Section 8.9.1 formally admits the exact numeric-cohort relation, with Restricted Coverage selected for
representative-domain closure. The equivalence relates two legal Decimal presentations when their exact
values `coefficient × 10^(-scale)` are equal. For each nonzero value, the proposed representative removes all trailing
base-10 coefficient zeroes and adjusts the scale exactly. For zero, it selects `(coefficient=0, scale=0)`.

The Law establishes the fully reduced coefficient and scale only if both are legal in the same Decimal domain.
Otherwise it refuses. It never stops reduction early, clamps the scale or rounds. Equivalent operands have the
same fully reduced mathematical form and therefore the same Coverage outcome. Host `BigDecimal` overflow does
not define this refusal. Section 8.9.1 closes the bound-sensitive rule, including the zero representative.

This profile is distinct from business scale rules. A fixed monetary scale is one example of meaning that belongs
elsewhere.

## 5.13. Binary32 Canonical NaN

The presentation domain is IEEE 754 binary32 only when raw NaN representation remains Contract-visible at the Input
boundary. The candidate maps every NaN bit pattern to one quiet-NaN representative while preserving every non-NaN bit
pattern, including signed zero.

Section 8.9.2 formally admits this Law. All binary32 NaN bits become `0x7fc00000`; other bits are unchanged.
This collapses the NaN sign, payload and signaling distinction but preserves both signed zeros. The
representative is idempotent and stays in the binary32 domain. Coverage is Total.

Raw-bit acquisition must precede the law. A lossless 32-bit carrier can realize the fixed exponent/fraction mask test
without executing floating-point arithmetic. Passing signaling NaN through a JVM `Float` path before Input
Establishment cannot be assumed lossless. This is a platform-carrier qualification and verification obligation under
ADR-0073, not permission to weaken ADR-0064 Input sameness or the proposed Law. A domain that needs NaN payload or
signaling distinctions for diagnostics, integrity, or control must not select this collapse. The admitted Law requires
each supported Input path to preserve the admitted bit distinctions before
Canonicalization; verification of a concrete platform path remains separate.

## 5.14. Binary64 Canonical NaN

The binary64 candidate follows the same shape as binary32. Every NaN representation maps to one quiet NaN while every
non-NaN bit pattern remains unchanged.

Section 8.9.3 records formal `ADMIT`. The admitted representative is `0x7ff8000000000000`, consistent with Java
`Double.doubleToLongBits`. All binary64 NaN bit patterns belong to one equivalence class; every non-NaN pattern is
only equivalent to the identical 64-bit pattern. The law preserves both signed zeroes, infinities, and finite-value
bits exactly. It is Total, idempotent, and uses fixed masks rather than host floating-point equality or arithmetic.

The realization must preserve the complete binary64 bit datum at Input acquisition, for example through a lossless
64-bit carrier. A JVM `Double` carrier cannot be presumed to retain every signaling NaN representation. The
independent binary64 Law does not inherit the binary32 representative or its bit-width semantics. NaN payload and
signaling-state loss is intentional only after the selected Canonicalization establishes its representative. The
admitted `Initial` Law fixes that boundary; supported-path preservation and security evidence
remain realization verification duties.

## 5.15. ASCII Grouped-Decimal Text

This candidate targets one self-contained ASCII grammar. It uses `,` for grouping and `.` for the
decimal separator. The operand requirement must specify valid grouping positions. An ambient locale
cannot change that grammar. Host settings and external numbering profiles have no authority over it.

The representative removes only valid grouping separators. It does not silently add the stronger distinctions owned by
fixed-point or scientific-notation candidates. Section 8.8.4 fixes the admitted exact grammar and representative.

```text
1,234,567.89
    -> 1234567.89
```

Qualification must decide the exact sign and fractional grammar. It must also review whether this subject deserves an
independent Authority or should be expressed through composition with another admitted decimal-text law.

## 5.16. Decimal-Comma Grouped Text

This candidate addresses an exact decimal-comma grammar without using a locale profile. The operand grammar itself must
fix `,` as the decimal separator and `.` as the grouping separator. Other separators or culturally inferred variants are
not accepted by implication.

The original working representative changes the decimal separator to `.`, as illustrated below:

```text
12.345,67
    -> 12345.67
```

Section 8 rejects this Law because the proposed output changes numeric meaning under the input grammar. There
`,` is the decimal separator and `.` groups digits. Thus `1,234` means 1.234, but the proposed output `1.234`
means 1234. The failure is same-domain closure, not the mechanics of punctuation replacement. Neither an
implicit locale change nor a second parser may supply a new interpretation.

A narrower grouping-removal profile might preserve the comma decimal separator, as in `12.345,67` -> `12345,67`.
That is a different exact representative and requires its own admission review; it is not admitted by this rejection.
This candidate must not become a general international-number parser. A different grouping grammar requires a separate
exact review rather than ambient locale selection.

## 5.17. Percentage / Per-Mille Rate Text

This candidate treats percent and per-mille text as presentations of an exact rate. The symbols carry scale semantics;
they are not decoration to be stripped mechanically.

```text
85.5%
855‰
0.855
    -> 0.855
```

The admitted exact grammar and arithmetic are fixed in Section 8.8.5; the earlier qualification required
exact arithmetic rather than binary floating-point rounding. The selected plain-decimal operand form
ensures same-domain closure, and the exact rate relation remains independent of the other decimal-text laws.

## 5.18. Scientific-Notation Text

Scientific notation allows several spellings of the same exact decimal value. The law must decide how
to write the exponent marker and whether to retain a positive exponent sign. It must also handle
leading zeros in the exponent and redundant zeros in the significand. Each decision must lead to one
exact representative.

The current direction keeps the operand inside scientific notation and chooses one scientific representative. It does
not make ordinary fixed-point text equivalent merely because both spellings denote the same decimal value.

```text
1.23e+04
    -> 1.23E4
```

A broader law that equates fixed-point and exponent notation would have a different equivalence relation. It must be
qualified separately rather than hidden behind a formatter choice. The `Initial` scientific-only law is admitted in
Section 8.8.6.

## 5.19. Binary32 Decimal Lexical Form

This candidate concerns decimal Text that denotes an IEEE 754 binary32 value. Two legal spellings are equivalent only
when the law's exact conversion relation produces the same binary32 value.

Section 8.9.5 records formal `ADMIT` for the exact
[W3C XML Schema 1.1](https://www.w3.org/TR/xmlschema11-2/) `float` lexical/canonical profile. The already-established
`Text` operand is interpreted under the selected `floatRep` grammar and the specifically chosen
`floatLexicalMap`; equivalence means identity of the resulting binary32 value, not equality of exact decimal rationals.
The representative is the result of the chosen `floatCanonicalMap` for that value. Both maps must be frozen as the
Exact Law Version's meaning, since XML Schema permits other conforming mappings that need not emit identical Text.

This XSD profile retains the identity distinction between positive and negative zero. NaN has one lexical
representative. The fixed maps also decide infinity and exponent parsing, including exact rounding ties.
Reapplication must preserve the representative. Text outside the grammar is refused, not repaired by the host.
The interpreted binary32 value defines Text equivalence; it is neither a new Input presentation nor a Lowering
result.

The law cannot use the current JVM floating-point formatter as semantic authority. Exact rational/integer-based
rounding or another independently verified implementation must produce the specified binary32 result, without
intermediate binary64 double-rounding. Very long numerals and extreme exponents require bounded-work evidence; any
safety limit must not silently change the selected rounding or representative.

## 5.20. Binary64 Decimal Lexical Form

The binary64 candidate has the same semantic shape as binary32 but a different value space and precision boundary. That
difference prevents one host formatting routine from implicitly defining both laws.

Section 8.9.6 records formal `ADMIT` for the separate W3C XML Schema 1.1 `double` lexical/canonical profile. Legal
`Text` under the selected `doubleRep` grammar is interpreted by the specifically chosen `doubleLexicalMap`; operands
are equivalent when they have the same binary64 value identity. Their one Text representative is selected by the
corresponding `doubleCanonicalMap`. The Law Version must fix both exact maps rather than inherit the current JVM or
an interchangeable IEEE 754 formatter.

Value identity preserves the distinction between positive and negative zero; NaN has one lexical identity within this
Text law. The grammar and mapping specify infinities and exact rounding independently of binary32. The proposed
representative remains in the same lexical Text domain. The formal law must close coverage or refusal outside the
exact grammar, prove reapplication stability, and verify finite work for adversarially long numerals and exponents.
Its equivalence does not assert that two different exact decimal rationals are equal: they may round to the same
binary64 value. A downstream consumer must not reinterpret the established representative under another floating-point
or exact-decimal profile.

## 5.21. Radix Integer Text Family

Three working candidates cover mathematical integers written in hexadecimal, binary, or octal Text.
They do not encode an octet sequence, so they are distinct from Base16 and other encoded-byte laws. The
operand grammar must decide when leading zeros can be ignored. It must also define prefixes and signs,
including negative zero. Hexadecimal letter case needs its own exact rule.

Section 8.10.1 admits the three distinct Authorities with fixed independent radix grammars and
representatives. A host parser's auto-radix rules cannot define the operand.

The representative must not accidentally import fixed-width integer meaning. In an integer law, redundant leading zero
digits can be irrelevant. In a bit-vector law, the same digits may carry width.

## 5.22. Fixed-Width Bit-Vector Text Family

The hexadecimal and binary candidates represent an exact-width bit sequence. Width is part of the semantic subject.
Canonicalization may remove allowed separators or normalize hexadecimal case, but it must preserve every bit and the
established width.

```text
0000_0000_1010_1111
    -> 0000000010101111
```

This family is not an integer formatter. `0000000010101111` cannot become `10101111` when the operand is a 16-bit
vector.
Section 8.10.2–8.10.3 closes the exact separator and prefix grammars and records both independent
Authorities, distinct from Base16 by their exact-width Bit-Vector subject.

## 5.23. Minimal Two's-Complement Signed-Integer Bytes

This candidate operates on a signed integer presented as a big-endian two's-complement octet sequence. Equivalent
operands decode to the same mathematical signed integer. The representative uses the shortest octet sequence that
preserves that integer and its sign.

Redundant sign-extension octets may disappear. A leading zero that is required to keep a positive value positive may
not.

```text
00 7F
    -> 7F

00 80
    -> 00 80
```

The admitted `Initial` law and its exact nonempty-Bytes Coverage are fixed in Section 8.10.4. Input must preserve
the octet sequence as semantic material; this law does not reinterpret arbitrary memory or protocol buffers.

## 5.24. POSIX File-Mode Octal Text

A POSIX-style numeric file mode is a bitmask presentation, not merely an octal spelling of an unrelated mathematical
integer. The candidate therefore remains domain-specific even if its implementation can share radix parsing machinery.

Section 8.10.5 fixes the admitted 12-bit mode-mask domain and four-octal-digit representative.
Redundant leading zero digits may be canonicalized only under that selected profile. Symbolic mode expressions such as
`u+rwx` are not part of this law because they can describe operations rather than one already-established mode value.

---

# 6. Structured, Protocol, and Domain Candidate Working Notes

These candidates have a closed textual or structured presentation that is usually defined by a protocol or domain
standard. A candidate may depend on parsing rules, but parsing does not itself become Canonicalization authority. The
Input side must first establish the legal semantic presentation on which the representative law operates.

When a candidate changes structured fields together, it must still preserve the presentation shape promised by
ADR-0066. A standard that produces signing bytes or a different wire representation belongs to another owner even when
it uses the word canonicalization.

## 6.1. Base16 Lowercase Text

The operand must already be legal Base16 textual presentation under an exact profile. The candidate does not encode
bytes
into hexadecimal text.

The intended equivalence declares hexadecimal letter case irrelevant and selects lowercase ASCII hex digits as the
representative while preserving the represented bit string.

Qualification must close the exact Base16 grammar. It must decide whether the `0x` prefix is legal and
whether separators are permitted. Odd digit counts need an explicit rule too. RFC 4648 Base16 has no
padding. A broader host parser cannot silently expand the operand domain. Section 8.7 closes these
questions in the admitted `Initial` Version.

## 6.2. Base16 Uppercase Text

This candidate has the same legal encoded-text boundary as the lowercase profile but selects uppercase hexadecimal
letters.

Authority uniqueness must be reviewed explicitly. Lowercase and uppercase select different representatives and therefore
cannot be aliases merely because they preserve the same decoded bytes. Section 8.7 records both independent admissions.

## 6.3. Base64 RFC 4648 Canonical Text

Section 8 records `REJECT` for this proposed independent Built-In
Law. [RFC 4648](https://www.rfc-editor.org/rfc/rfc4648.html) defines the Base64 alphabet, padding requirements, and
canonical zero pad bits. Under the strict standard profile, each octet sequence already has one legal encoded
representation. The corresponding Input grammar can recognize that representation without creating a second
Canonicalization Authority that preserves everything.

Some Base64 decoders ignore line breaks or excess padding. Others tolerate nonzero unused pad bits. These
behaviors do not establish an equivalence Law. RFC 4648 distinguishes the strict encoding rules from latitude
granted by a referring protocol. Treating every decodable spelling as equivalent could erase a distinction
used by a signature or a validation boundary.

The rejected subject is generic RFC 4648 Base64 canonicalization, not Base64 processing in every protocol. A separately
specified domain such as MIME line-wrapped Base64 may supply its own exact accepted presentations and representative.
Its scope would require new qualification instead of silently widening this candidate.

## 6.4. Base64url RFC 4648 Canonical Text

Section 8 records `REJECT` for this generic independent Built-In
Law. [RFC 4648](https://www.rfc-editor.org/rfc/rfc4648.html) defines the URL-safe alphabet but leaves omission of
padding to the referring specification. Neither a universal padded representative nor a universal unpadded
representative follows from the Base64url name alone.

The strict padded and strict unpadded profiles each have a single canonical spelling for an octet sequence when their
grammar and zero pad-bit requirements are fixed. A law that equates `YQ==` with `YQ` would add a cross-profile
equivalence not granted by every consumer. This is particularly unsafe where the encoded text itself participates in a
signature or another byte-exact protocol rule; JWS, for example, specifies unpadded Base64url for its own use.

Input or the relevant protocol Authority must enforce the selected alphabet and padding rule. A future protocol-specific
law may be qualified when that protocol explicitly admits more than one representation of the same meaning. Decoder
permissiveness cannot supply the missing semantic authority.

## 6.5. Base32 RFC 4648 Canonical Text

The operand must already be legal Base32 text under one exact RFC 4648 profile. The candidate does not decode arbitrary
text and then re-encode it under implementation defaults.

RFC 4648 defines a Base32 alphabet and canonical pad-bit requirements. Qualification must close the padding policy and
the accepted grammar. Case-insensitive behavior from a permissive decoder cannot silently widen the operand. Section 8.7
fixes the
permitted ASCII case variants and requires exact padding and zero unused bits.

## 6.6. Base32hex RFC 4648 Canonical Text

Base32hex uses a different alphabet from ordinary Base32. It therefore needs its own exact semantic review rather than
an
implementation flag on one ambiguous candidate.

The same canonical pad-bit and padding questions apply. Shared codec code does not make the two alphabets one Authority.
Section 8.7 fixes a distinct Base32hex alphabet and the same strict padding profile.

## 6.7. Decimal Integer Text Canonical Form

Developers frequently remove redundant leading zeros or normalize signs when handling decimal integer text. That demand
does not justify a vague "number string normalize" law.

The candidate qualification identified the following details, now fixed by the formally admitted `Initial`
grammar and representative in Section 8.8.2:

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

## 6.8. Fixed-Point Decimal Text Canonical Form

This candidate covers decimal quantities that arrive as Text and remain Text after Canonicalization. It is distinct from
the structured Decimal Numeric Value candidate in Section 5.12.

The working scope is fixed-point notation rather than exponent notation. Qualification must decide
whether redundant integer leading zeros are equivalent. It must separately decide whether trailing
zeros in the fraction are irrelevant. Legal spellings such as `.5` and `12.` need exact treatment. The
representative must also account for an explicit plus sign and negative zero.

```text
12.50
    -> 12.5

.5
    -> 0.5
```

These examples were candidate evidence. The exact grammar, representative, and formal membership are now fixed
by Section 8.8.3.

Business scale remains outside this candidate. If `12.50` carries a Contract-visible scale distinct from `12.5`, the two
texts are not equivalent under this law.

## 6.9. RFC 3986 Percent-Encoding Syntax Representative

Section 8.7 records formal `ADMIT` for the narrow syntax representative described
by [RFC 3986, Sections 2.1 and 6.2.2](https://www.rfc-editor.org/rfc/rfc3986.html). The operand must be an
already-established `Text` presentation whose exact URI-component context and percent-triplet grammar are fixed. This
Law does not accept an arbitrary whole URI and infer its component boundaries.

Two legal component presentations are equivalent only when they differ by the hexadecimal letter case of a percent
triplet or by the choice between an unreserved ASCII character and its percent-encoded spelling. The representative uses
uppercase hexadecimal letters in retained triplets and direct spelling for encoded unreserved characters. Reserved
characters stay encoded when supplied as encoded octets.

```text
%2f
    -> %2F

%7e
    -> ~

%25
    -> %25
```

The representative remains in the same exact component-presentation domain and must be stable when the Law is applied
again. Valid percent triplets and the allowed direct character repertoire must be defined by the operand grammar. Other
established `Text` lies outside successful coverage and must not be repaired by a host URI decoder. The exact Refusal
and Coverage relation is fixed in the admitted versioned Law specification in Section 8.7.

The component Law must not become another URI parser. Decoding `%2F` or decoding a second time after Admission
can change path boundaries. Applying this Law before component parsing is likewise unsafe. Encoded unreserved
dots can affect later dot-segment interpretation. Component boundaries must therefore be legally fixed, and
downstream consumers must use the established result. Path routing and signature meanings remain with their
own Authorities. No ambient library may decide the operation order.

## 6.10. IPv6 RFC 5952 Text

Section 8.11 admits the restricted [RFC 5952 Section 4](https://www.rfc-editor.org/rfc/rfc5952.html#section-4)
all-hexadecimal profile. The operand is a legal IPv6 Text address without a zone identifier. Two operands are
equivalent when they encode the same 128 bits. The representative lowercases hexadecimal digits and removes
redundant zeros within fields. It compresses the longest run of zero fields, choosing the leftmost on a tie. A
single zero field is not compressed.

IPv4-embedded spellings are legal inputs when admitted by the exact IPv6 operand grammar, but the representative is
always the RFC 5952 Section 4 hexadecimal form. Section 5 recommends mixed IPv4 notation for certain address classes;
this narrower all-hexadecimal choice is an explicit Kontrakt profile, not a claim that every RFC 5952 recommendation
mandates all-hexadecimal output. A deployment-dependent prefix list must not influence it. The grammar and the Section 4
tie rules are fixed by the Law, not a host address formatter.

This Law does not establish a DNS name, an interface scope, or equality of network endpoints. Text outside the legal
address grammar has exact non-repairing Coverage and refusal treatment. Canonical formation must remain in the same
textual domain and be idempotent.

## 6.11. BCP 47 Registry-Independent Casing

This candidate is narrower than full BCP 47 canonicalization. RFC 5646 states that case distinctions do not carry
language-tag meaning and describes a conventional casing form that can be reproduced without consulting the IANA
Language Subtag Registry.

The working representative follows that registry-independent convention. Most subtags are lowercase. Syntactically
identified two-letter and four-letter subtags use the conventional uppercase and titlecase forms where RFC 5646
specifies
that treatment. Locale-sensitive host casing must not be used.

This law would change presentation case only. It would not apply registry `Preferred-Value` mappings or other
registry-dependent canonicalization.

## 6.12. BCP 47 Registry-Dependent Canonical Representative

Section 8.2 records `DEFER` for the registry-dependent language-tag canonicalization described
by [RFC 5646 Section 4.5](https://www.rfc-editor.org/rfc/rfc5646.html#section-4.5). The operand is an
already-established well-formed BCP 47 language-tag Text admitted by one precisely defined Registry profile. The Law
applies RFC 5646 Preferred-Value and applicable canonical subtag rules, including the RFC-defined ordering of extension
singletons, and selects one exact casing and replacement sequence. The registry-independent casing Law in Section 6.11
remains narrower.

The [IANA Language Subtag Registry](https://www.iana.org/assignments/language-subtag-registry/language-subtag-registry)
snapshot used to determine meaning must be immutable and identified by the Exact Law Version and a verifiable content
identity. Kontrakt must not read the current IANA Registry, an OS locale database, or a newer ICU dataset during
canonicalization. A new snapshot that changes the mapping requires a new semantic Version; it does not silently redefine
the existing Law.

This profile leaves registered extension contents and private-use values untouched. It does not remove
`Suppress-Script` subtags or reorder variants without an explicit RFC rule. Admission still requires exact
Preferred-Value replacement order and a legal fixed-point representative. The input grammar and finite-work
proof must also be closed.

## 6.13. UUID Lowercase Text

The candidate domain is one exact standard textual UUID form. Hexadecimal letter case is declared irrelevant and
lowercase text is the representative.

Qualification must confirm the accepted grammar and delimiter positions. It must separately decide whether wrappers or
URN forms belong to the operand. Alternative textual forms cannot be accepted by accident. The law changes neither UUID
bits nor version or variant meaning.

A structured UUID value that no longer contains textual case does not need this law.

The independent Authority is `REJECT`ed in Section 8.3; UUID validation plus the existing ASCII Lowercase Law covers its
case-only representative.

## 6.14. RFC 9911 MAC-48 Lowercase Text

This candidate is narrower than a generic MAC-address normalizer. RFC 9911 defines a 48-bit IEEE 802 MAC text profile as
six hexadecimal octets separated by colons and uses lowercase hexadecimal characters for its canonical representation.

Qualification must decide whether Kontrakt adopts that exact textual profile as the operand. Other address lengths and
separator conventions are not silently included.

The independent Authority is `REJECT`ed in Section 8.3; lawful MAC-48 Input may reuse the already-admitted ASCII
Lowercase Law.

## 6.15. IPv4 Network Prefix

The candidate operand is legal IPv4 prefix text with an exact prefix length. Two operands are equivalent when they
denote
the same prefix bits at the same prefix length.

The working representative clears every address bit outside the prefix. RFC 9911 uses this canonical form for its IPv4
prefix type.

```text
192.0.2.1/24
    -> 192.0.2.0/24
```

Qualification must keep strict dotted-decimal syntax separate from legacy IPv4 parser behavior. Prefix-length legality
belongs to the operand profile rather than to a backend parser.

## 6.16. IPv6 Network Prefix

Section 8.11 records formal `ADMIT` for the [RFC 9911](https://www.rfc-editor.org/rfc/rfc9911.html) IPv6 network-prefix
representative. The operand is an already-established legal IPv6-prefix Text with a prefix length from 0 through 128.
Two operands are equivalent only when their prefix lengths and the address bits covered by that length are equal. The
representative clears every non-prefix bit and formats the remaining 128-bit address using the Section 6.10 RFC 5952
Section 4 all-hexadecimal profile.

```text
2001:db8::1/64
    -> 2001:db8::/64
```

The prefix length is retained. A shorter and a longer prefix are not equivalent merely because their displayed address
portions agree. Embedded IPv4 presentation is interpreted only through the precise IPv6 operand grammar, and no ambient
prefix policy or host formatting rule selects the result. This Law's normative meaning includes its relation to the
admitted IPv6 textual profile; shared realization code does not establish Authority ownership.

## 6.17. EUI-48 / MAC-48 Multi-Format Text

Section 8 records `REJECT` for the proposed generic multi-format Authority. This candidate is broader than the
separately proposed [RFC 9911](https://www.rfc-editor.org/rfc/rfc9911.html) lowercase colon-separated MAC-48 Text Law in
Section 6.14. Several forms are widely used, including hyphen-separated octets and vendor-specific dotted notation, but
their prevalence does not establish one universal standard input grammar and interpretation rule for Kontrakt.

Accepting all such formats under a single Law without fixing their octet interpretation, any relevant bit-order
convention, and the supported source standard would make its equivalence depend on an inferred origin. It would also
overlap the existing RFC 9911 Authority without proving a distinct and safely bounded need. This is not a claim that
conversions between individually specified formats are inherently nondeterministic.

A new narrowly named candidate may explicitly translate a designated IEEE EUI-48 textual profile, or a separately
specified dotted form, into the RFC 9911 representation after its grammar and octet-order semantics are established.
That proposal requires a separate Catalog decision; the present broad multi-format Law is not admitted.

## 6.18. HTTP-Date Text

Section 8.7 records formal `ADMIT` for
the [RFC 9110 Section 5.6.7](https://www.rfc-editor.org/rfc/rfc9110.html#section-5.6.7) HTTP-date profile. The standard
recognizes IMF-fixdate, obsolete RFC 850 date, and obsolete asctime date forms for recipients, but requires an
IMF-fixdate form when a sender generates a date. These admitted spellings can represent one HTTP date without giving
Kontrakt a generic timestamp-normalization Authority.

The proposed equivalence relates exact legal spellings that denote the same UTC calendar date and time under the
required interpretation context. The representative is that date's IMF-fixdate Text. The obsolete RFC 850 spelling has a
two-digit year whose interpretation depends on the reference time specified by RFC 9110. That time must therefore be an
explicitly resolved and bound UTC Required Basis. It cannot be the compiler's clock, a mutable runtime default, or the
moment at which a cached result happens to be reused.

```text
Sun, 06 Nov 1994 08:49:37 GMT
Sunday, 06-Nov-94 08:49:37 GMT
Sun Nov  6 08:49:37 1994
    -> Sun, 06 Nov 1994 08:49:37 GMT
```

The `Initial` specification must verify calendar validity and weekday consistency. It separately fixes the RFC
850 two-digit-year rule and leap-second handling. Dates outside the permitted year range or legal HTTP-date
grammar refuse. None of those choices may be delegated to the JVM date parser.

## 6.19. HTTP Media Type Text

Section 8.7 records formal `ADMIT` for a restricted HTTP media-type *syntax* representative based
on [RFC 9110 Section 8.3.1](https://www.rfc-editor.org/rfc/rfc9110.html#section-8.3.1)
and [RFC 6838 Section 4.3](https://www.rfc-editor.org/rfc/rfc6838.html#section-4.3). It does not claim to canonicalize
every semantic equivalence of a registered media type. The already-established `Text` operand must be parsed under one
exact HTTP media-type grammar, with one type/subtype and a finite set of named parameters.

The common equivalence treats ASCII letter case in type, subtype, and parameter names as irrelevant. RFC 6838 gives no
meaning to parameter order and prohibits repeated parameter names. Within the selected HTTP quoted-string grammar,
equivalent quoted and token spellings of an unchanged parameter value may share a representative. The Law preserves
every parameter value's exact content and case rather than assuming the value's registered comparison semantics.

The representative lowercases type, subtype, and parameter names; orders parameters by a fixed ASCII name order; emits
an unquoted token only when the value is a legal token; and otherwise uses one exactly specified quoted-string and
escape spelling. For example, the following are equivalent under the proposed common syntax relation:

```text
Application/Example;Z=ABC;A="xyz"
    -> application/example;a=xyz;z=ABC
```

No parameter registration or ambient IANA registry lookup may silently extend this equivalence. In particular,
`charset=UTF-8` is not converted to `charset=utf-8` merely because that particular parameter may have a case-insensitive
value. Duplicate names after ASCII case normalization, invalid escapes, malformed parameter syntax, or unsupported
extensions must have exact non-repairing coverage and refusal rules. Host map iteration order cannot select the output.
The Law remains a same-domain `Text` representative and does not establish media-type-specific processing meaning.

## 6.20. HTTP Qvalue Text

HTTP qvalues have a small numeric domain and several legal lexical spellings for the same value. This makes them a
useful
narrow numeric-text candidate.

Qualification must first close the exact RFC grammar and precision bound. It must then choose one representative
spelling
for each legal value. The standard constrains the value and syntax but does not by itself justify an arbitrary host
floating-point formatter.

The operand remains qvalue Text. Parsing the value into an unrelated numeric Fact is outside this law.

## 6.21. HTTP (S) URI Normal Form

Section 8.7 records formal `ADMIT` for a restricted HTTP (S) scheme-based URI representative
using [RFC 9110 Section 4.2.3](https://www.rfc-editor.org/rfc/rfc9110.html#section-4.2.3) and the
applicable [RFC 3986](https://www.rfc-editor.org/rfc/rfc3986.html) syntax rules. This is not an assertion that URI
normalization proves two origin resources are the same, and it does not admit generic whole-URI canonicalization for
every scheme.

The proposed V1 operand is an already-established absolute `http` or `https` URI `Text` under a strict ASCII-host
profile. Userinfo, internationalized host interpretation requiring IDNA, and IP-literal profiles whose canonical
spelling has not been closed are outside this operand. DNS resolution, origin-specific query rules, and platform
URL-parser recovery do not establish legal alternate presentations. HTTP and HTTPS remain distinct schemes.

Within that domain the Law declares only the selected standards-defined presentation differences equivalent. Its
representative lowercases the scheme and ASCII host, omits the matching default port (`80` for HTTP or `443` for HTTPS),
applies the exact RFC 3986 percent-encoding and dot-segment rules in an expressly fixed order, and preserves query
contents and ordering rather than treating them as an unordered map. The ordinary empty-path representative is `/` only
where the applicable HTTP request-target context permits that equivalence.

```text
http://EXAMPLE.com:80/%7Euser
    -> http://example.com/~user
```

RFC 9110 makes empty-path handling context-dependent for OPTIONS requests. A bare `Text` cannot supply that
context. Selection must therefore exclude an incompatible request context or bind it explicitly. Consumers
must not reparse or decode the established result under different rules. In particular, signed URIs and
path-routing decisions require their own validation and authorization.

The admitted profile fixes the ASCII host grammar and URI-component boundaries. It also fixes percent-encoding
and dot-segment processing order. Exact Coverage and any required request context are part of the Law, not
backend choices. Unsupported host syntax refuses. A permissive URL parser cannot widen the fixed Version.

## 6.22. CoAP URI Normal Form

Section 8.11 records formal `ADMIT` for a restricted `coap` / `coaps` URI normal-form Law grounded
in [RFC 7252 Section 6.3](https://www.rfc-editor.org/rfc/rfc7252.html#section-6.3)
and [RFC 3986](https://www.rfc-editor.org/rfc/rfc3986.html). The operand is an already-established whole URI Text whose
scheme, authority, path, and query are parsed under one exact CoAP URI profile. The relation is the selected standard
scheme-specific normalization, not proof that two requests ultimately reach the same resource.

The representative lowercases the scheme and ASCII host. It removes the CoAP default port: `5683` for `coap`
or `5684` for `coaps`. It then applies the admitted percent-encoding and path rules and writes `/` for a
qualifying empty path. Scheme security (`coap` versus `coaps`) remains significant. The Law neither sorts
application-defined query items nor decodes reserved path separators. A later consumer must not decode the
result again. IPv4 and IPv6 literal rules, including the selected IPv6 profile, remain explicit.

V1 selects a fixed ASCII-host and supported IP-literal grammar. Other host profiles are outside Coverage; zone
identifiers and ambiguous URI spellings also refuse. Parse the URI before applying component transformations.
The Law fixes percent-encoding and dot-segment order rather than importing a host URL library's defaults.
Shared code with HTTP URI processing does not merge the two Authorities.

## 6.23. Generic URN Lexical Representative

Section 8.12 records formal `ADMIT` for the restricted **RFC 8141 assigned-name-only** Text
representative. [RFC 8141 Section 3.1](https://www.rfc-editor.org/rfc/rfc8141.html#section-3.1) defines generic
URN-equivalence for the assigned name. The operand is a legal `urn:` assigned-name Text without `r-component`,
`q-component`, or `f-component`; full URNs carrying those components are outside this Law's successful Coverage.

Its equivalence disregards only scheme and Namespace Identifier ASCII letter case and percent-triplet hexadecimal letter
case in the Namespace-Specific String. The representative uses `urn:`, a lowercase NID, and uppercase hexadecimal
letters within retained NSS percent triplets. It must not decode percent-encoded octets, infer namespace-specific
identity, or reinterpret the assigned name through generic RFC 3986 unreserved-decoding rules.

RFC 8141 ignores optional components for the generic URN-equivalence comparison while permitting them to affect
resolution requests. Discarding such components from a full input URN could change operational meaning. Restricting the
operand to assigned names avoids this problem without claiming the whole-URN identity or resolution semantics.
Namespace-specific stronger relations require independently qualified Laws.

## 6.24. RFC 3966 `tel:` URI Representative

RFC 3966 gives a much better Contract subject than generic user-entered telephone-number normalization. Visual
separators
do not affect URI comparison, and parameters are compared independently of their source order. The standard also gives a
preferred parameter ordering that can guide one representative.

A local number is not complete without its `phone-context`. That material is part of the URI meaning and cannot be
supplied from an ambient default region.

Qualification must keep global and local numbers distinct. It must also preserve the standard's parameter-specific
comparison rules instead of applying one blanket text fold to every parameter value.

## 6.25. XML Schema Date/Time Canonical Lexical Family

XML Schema Datatypes explicitly separates a value space from its lexical space and defines canonical lexical mappings.
That architecture is close to the representative problem that this Catalog is qualifying.

The current working family contains three candidates: `date`, `time`, and `dateTime`. They require
independent admission when their semantic subjects or Version histories differ. Shared implementation
code does not merge them.

The exact XSD profile determines timezone normalization and fractional-second spelling. Qualification must preserve the
profile's own timezone semantics rather than importing RFC 3339 or RFC 9557 meaning. Similar-looking timestamp strings
can
belong to different equivalence relations under those standards.

## 6.26. DOI Textual Representative

DOI names have an unusual but useful comparison rule. Basic Latin `A` through `Z` compare case-insensitively, while
non-ASCII case differences are not collapsed by that rule. DOI comparison also does not perform generic Unicode
normalization.

That makes DOI a stronger domain candidate than applying Unicode case folding to an identifier. Qualification must
choose
one exact representative for the Basic Latin case-insensitive relation and preserve every distinction outside it.

The DOI grammar and any URI or proxy presentation remain separate questions. A DOI-name law must not silently absorb the
syntax of a resolver URL.

The independent Authority is `REJECT`ed in Section 8.3; the DOI name retains the Basic Latin-only equivalence of the
admitted ASCII Lowercase Law.

## 6.27. IBAN Electronic Text

Section 8.13 records formal `ADMIT` for the paper-to-electronic spacing distinction
under [ISO 13616-1](https://www.iso.org/standard/81089.html). The operand is an already-established Text in the exact
electronic IBAN presentation or its specified paper grouping with `U+0020 SPACE`. The equivalence removes only
authorized printed grouping spaces while preserving all account characters exactly. Its representative is the
corresponding unspaced electronic Text.

```text
DE89 3704 0044 0532 0130 00
    -> DE89370400440532013000
```

The Law does not uppercase the BBAN or apply case-insensitive Unicode folding. Country-specific BBAN rules may make
letter case meaningful. Nor does it silently remove arbitrary Unicode whitespace, repair check digits, or infer country
formats from an ambient registry. Country-specific length and BBAN legality, MOD-97 check-digit judgment, and account
validity remain separate Input / Admission concerns. The precise paper-group grammar and supported domain must be fixed
so no invalid spacing is repaired by canonicalization.

## 6.28. GTIN 14-Digit Representative

GS1 Digital Link syntax uses a 14-digit representation for GTIN values. Shorter forms are padded with
leading zeroes under that profile. This applies to GTIN-8, GTIN-12, and GTIN-13.

This is not a generic integer-leading-zero law. The zeros are part of a domain representation rule for a GS1 identifier.
Qualification must preserve that domain meaning and keep check-digit validity outside Canonicalization.

A later GS1 Digital Link URI candidate may consume this representative without turning the GTIN Authority into a URI
law.

## 6.29. Package URL Canonical Representative

Section 8.13 records formal `ADMIT` only for an
**[ECMA-427](https://ecma-international.org/publications-and-standards/standards/ecma-427/) core PURL syntax**
representative. The operand is a legal Package URL Text in an exact core grammar. The Law owns only representation
distinctions for which the ECMA-427 core specification supplies common rules. Package-type-specific identity and
normalization are not silently included.

The ECMA-427 core representative uses `pkg:` and a lowercase package type. It removes only the slashes that
the core standard declares insignificant. Component-specific percent-encoding and one fixed qualifier-key
order complete the representative. Ecosystem-specific case rules for the package name or version are not
inferred. Invalid qualifiers and percent-encoding produce the exact Law refusal, as do forms requiring
unsupported type-specific interpretation.

A Maven, npm, or PyPI rule may create additional equivalence not established by the core syntax. Such a rule needs its
own qualified Law or explicit versioned semantic material. Canonicalizing the core PURL Text does not establish that two
package versions are the same dependency or may be treated as the same vulnerability target. The normative specification
must verify same-domain closure and idempotence, including the exact percent-encoding and qualifier-order algorithm.

## 6.30. Kubernetes Quantity Representative

Section 8.2 defers this Kubernetes Quantity Law pending a fixed source profile. Its basis is the
[Kubernetes Quantity API documentation](https://kubernetes.io/docs/reference/kubernetes-api/definitions/quantity-resource/)
and a verifiable `k8s.io/apimachinery/pkg/api/resource` baseline. Kubernetes distinguishes DecimalSI from
BinarySI and DecimalExponent suffix families. It preserves that family when formatting and applies the
specified precision and range rules. Kontrakt must reproduce this behavior, not replace it with exact-value
DecimalSI formatting.

```text
1.5
    -> 1500m

1.5Gi
    -> 1536Mi
```

A numeric comparison is not the Law's equivalence: `1Ki` and `1024` can compare as the same numeric quantity while their
Kubernetes canonical Text remains different because the source suffix families are retained. The proposed `E_L` relates
exactly those legal input spellings that produce the same Text under the fixed Kubernetes parse-and-canonicalize
profile. `C_L` is that profile's actual canonical Text. The exact Law must establish that the result is a legal
same-domain operand and a fixed point under repeated application; neither property may be assumed merely because a Go
`String()` method exists.

Kubernetes can round fine-grained input: the documented `0.1m` becomes `1m`. It may also cap out-of-range
values. Those choices are part of the proposed Kubernetes meaning, not a license for Kontrakt to invent a
refusal or use exact-rational equality instead. The immutable profile must settle suffix interpretation and
rounding, including zero and boundary values. It must also determine the canonical output spelling. Text
acquisition must be lossless; floating-point host defaults are not semantic authority.

The Law Version identifies the immutable semantic profile and its source/test-vector provenance. Kubernetes releases are
compatibility evidence, not necessarily distinct Law Versions: different releases may be supported under one Version
when their relevant Quantity behavior is proven equivalent. A changed parser or canonical representative requires a new
semantic Version and must not silently alter an existing Authority. Developers should not have to select a cluster
release when several verified releases share that profile. This Quantity Law does not establish general Kubernetes
resource-policy validity or numeric-comparison semantics.

## 6.31. `geo:` URI Representative

Section 8.13 admits the [RFC 5870](https://www.rfc-editor.org/rfc/rfc5870.html) WGS-84 core `geo:` URI Law.
Its operand is a legal WGS-84 URI using only the supported core parameters. Two operands are equivalent if
their exact decimal coordinates agree, subject to the RFC's special geographic comparisons. Longitudes at the
antimeridian are equivalent at +180 and -180. At a pole, longitude is irrelevant. Explicit and omitted default
CRS spellings also compare equal.

The representative uses minimal exact decimal coordinates. It writes longitude `180` at the antimeridian and
`0` at either pole. An explicit default `crs=wgs84` is omitted. Altitude and uncertainty `u` retain both their
presence and exact values: absence is never the same as zero.

V1 excludes extension parameters and non-default CRS relations, which RFC 5870 does not define globally. The
exact latitude/longitude bounds and lexical grammar must be fixed. Component order and signed-zero spelling
also affect the representative. The Law uses exact Decimal meaning, not binary floating-point approximation or
a mutable geodetic registry.

## 6.32. RFC 7468 Textual-Encoding Representative

Section 8.13 records formal `ADMIT` for a **single [RFC 7468](https://www.rfc-editor.org/rfc/rfc7468.html)
textual-encoding instance**. Its operand is one legal established encapsulation Text with an explicit BEGIN/END Label
and Base64-encoded octets. The equivalence relates only permitted parser spellings carrying exactly the same Label and
octet sequence. The representative selects one exact strict-generator presentation with Base64 lines of 64 characters
except the final line, no extraneous whitespace, and a fixed LF line ending.

The admitted parser profile is narrower than a permissive PEM reader. BEGIN and END Labels must match exactly,
including case. Base64 padding and unused bits are validated. Only the specified line-layout variations are
equivalent. Mismatched labels or unsupported framing refuse; the Law never repairs the input. The canonical
output's final newline is fixed so that reapplication is stable.

A file with several encapsulations is a different subject. This Law neither reorders instances nor changes
their decoded security objects. In particular, ASN.1 interpretation and certificate or key validation remain
outside it. RFC 7468 permits a generator form and defined parser latitude independently of the rejected
generic Base64 Law.

## 6.33. Avro Parsing Canonical Form

Section 8.13 admits the versioned
[Apache Avro Parsing Canonical Form](https://avro.apache.org/docs/current/specification/#parsing-canonical-form-for-schemas)
Law. Operand and representative are Text of a legal Avro Schema under the pinned specification. `E_L` is
equality of Parsing Canonical Form Text. `C_L` applies the seven transformations in normative order:
`PRIMITIVES → FULLNAMES → STRIP → ORDER → STRINGS → INTEGERS → WHITESPACE`. The sequence is exact and cannot
be reordered.

This is not general JSON Canonicalization. Parsing Canonical Form removes properties irrelevant to Avro
parsing, but those properties may affect schema evolution or application defaults. Logical-type and
application-policy meaning also lies outside the comparison. The Law proves only parsing equivalence, not full
substitutability of schemas.

The frozen Avro version determines the schema grammar and name/namespace resolution. It also fixes string
escaping and integer spelling. The output must be a legal, idempotent same-domain Schema Text. Deep nesting
requires bounded work. Named references must be resolved through explicit names rather than host object
identity. A fingerprint may accelerate lookup, but exact canonical content remains the equality witness.

## 6.34. POSIX IPv4 Numbers-and-Dots Text

This candidate covers the historical POSIX IPv4 numbers-and-dots syntax as one explicit operand profile. That profile
can
admit one- through four-part forms and radix forms that a strict dotted-decimal IPv4 grammar intentionally rejects.

Equivalent operands denote the same 32-bit IPv4 address under that exact POSIX interpretation. The working
representative is the ordinary four-octet decimal dotted form.

```text
127.1
0x7f.1
0177.0.0.1
    -> 127.0.0.1
```

This law must be selected explicitly. It does not authorize a strict IPv4 Input to begin accepting legacy syntax. It
also cannot delegate interpretation to an OS resolver or a host parser whose accepted forms may differ.

WHATWG URL-host IPv4 parsing remains a different semantic domain. Similar visible input does not make the two parsing
relations one Authority.

---

# 7. Deferred and Explicit Boundary Working Notes

The following profiles are plausible or commonly requested transformations that are not part of the current
admission-ready set. This section also records familiar requests that belong to a different authority unless a narrower
semantic subject is defined. Moving a deferred profile into the built-in Catalog requires explicit qualification and a
Catalog admission decision under ADR-0076. A new ADR is required only if that work changes the Catalog architecture or
qualification law.

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

A future profile must make that distinction explicit and independently justify Built-In suitability. It remains separate
from the Unicode default full case-fold candidate.

## 7.4. Broad Search Folding

Lucene ICU folding and similar search pipelines intentionally erase several kinds of distinction at once. Case and
accent are two examples. Width and digit distinctions may also be erased.

That breadth is useful for search but is too large to import as a default Canonicalization law merely because a SOTA
search library provides it. Narrow laws are reviewed separately above. A future broad search-fold Authority must first
define its exact relation and representative. It must then close its Unicode Basis and security consequences.

Folding U+00A0 NO-BREAK SPACE into U+0020 SPACE belongs to this review unless a narrower law is independently justified.
The two code points have different line-breaking semantics, so visual similarity alone cannot establish equivalence.

## 7.5. Signed-Zero Collapse

A binary floating-point law may declare positive and negative zero equivalent, but IEEE 754 behavior can later observe
the sign. V1 therefore defers this law until those semantic consequences are reviewed explicitly.

## 7.6. Whole-URI Canonicalization

RFC 3986 defines generic syntax normalization, but whole-URI equivalence can become scheme-specific. V1 therefore admits
no generic whole-URI canonicalization law.

Section 6.9 isolates percent-encoding syntax because that relation can be reviewed without importing the rest of URI
meaning. Sections 6.21 and 6.22 review HTTP and CoAP only because those schemes provide their own normalization rules.
Dot-segment removal remains outside the generic component candidate because path interpretation creates a different
boundary.

## 7.7. IDNA and PRECIS Profiles

IDNA and PRECIS combine representative formation with legality rules. A future Kontrakt profile must split those
responsibilities according to Kontrakt authority rather than import an external pipeline as one opaque law.
Representative formation may belong to Canonicalization while Input or Admission owns the corresponding legality rule.

## 7.8. Instant-Preserving Temporal Profiles

A temporal law may declare two offset date-time presentations equivalent when they denote the same instant and select a
UTC representative. Such a law is valid only when the original civil-time context is explicitly irrelevant.

V1 therefore admits no generic date-time canonicalization law.

## 7.9. Strict IPv4 Text and Non-POSIX Legacy Forms

A strict four-octet dotted-decimal IPv4 presentation can already be representationally singular when Input rejects
redundant or legacy spellings. In that case Canonicalization has no additional representative to select.

Section 6.34 now isolates the POSIX numbers-and-dots relation as an explicit candidate. That decision does not make
other
legacy parser behavior part of the same law. A parser that accepts additional spellings needs its own exact semantic
review before those spellings can enter a Canonicalization operand domain.

WHATWG URL-host IPv4 parsing remains a separate semantic domain because it deliberately preserves its own legacy IPv4
interpretation rules. If Kontrakt later supports that behavior, it must be reviewed as a URL-host profile rather than as
generic IPv4 text.

## 7.10. Order-Insensitive Finite Aggregates

Sorting and duplicate removal are common in application-defined collections, but the generic operation
is not yet a Built-In Law candidate. Tags are one example. Identifier collections and permission-like
lists raise the same question. The Contract must first establish whether order and multiplicity are
distinctions Canonicalization may erase.

If a future aggregate law needs an element order to choose one representative, that order must be Contract-defined. A
host comparator cannot supply it implicitly. Text ordering is particularly sensitive because UTF-16 code-unit order is
not automatically the same relation as Unicode scalar-value order.

## 7.11. Email Address Normalization

Generic email normalization remains deferred. SMTP mailbox local-parts cannot be assumed case-insensitive, while the
domain portion follows different comparison rules. Internationalized domains add a separate IDNA boundary.

A single lowercasing or Unicode-folding law therefore cannot safely define mailbox equivalence. A future candidate must
start from a narrower semantic subject.

## 7.12. User-Entered Telephone Numbers

User-entered telephone numbers remain deferred because national notation can require numbering-plan context before it
has
one global interpretation. A hidden default region cannot become a Canonicalization determinant.

Section 6.24 treats RFC 3966 `tel:` URI as a different semantic subject because the protocol carries the context
required
for its own comparison rules. That candidate does not justify a generic phone-number normalizer.

## 7.13. RFC 3339 Lexical Normalization

A narrow RFC 3339 textual profile may be useful even when the original offset is preserved. Candidate work can examine
permitted lexical variation without converting every timestamp to UTC.

The offset distinction must remain exact. RFC 3339 `-00:00` means that the local offset is unknown and is not equivalent
to `Z` or `+00:00`. Any future representative must preserve that distinction.

RFC 9557 adds named time-zone and additional suffix semantics on top of RFC 3339-style timestamps. Those fields create a
separate candidate problem and must not be folded into a generic instant-only law.

## 7.14. Canonical JSON and JCS

JSON Canonicalization Scheme work does not belong to this inbound Built-In Law Catalog merely because it uses the word
canonicalization. JCS defines deterministic JSON serialization and requires parsed JSON string data to be preserved
rather than Unicode-normalized.

That authority belongs to the protocol or serialization layer that owns the representation. Kontrakt Canonicalization
must not absorb JCS as an inbound text law.

## 7.15. HTML Character References

HTML character-reference resolution is not a generic Text Canonicalization law. In raw HTML source, replacing a
character
reference with the literal character can change parsing because characters such as `<` participate in markup syntax.

After an HTML parser has established character data, the source spelling distinction has already been consumed by that
parser. The correct owner is therefore HTML interpretation at the Input boundary, not a later law that rewrites
arbitrary
Text.

## 7.16. Currency Symbols and Currency Codes

A currency symbol does not identify one currency without additional context. The symbol `$` is the obvious example. A
locale or market convention can change what it means, so a generic symbol-to-ISO-code law would have an undeclared
semantic determinant.

An alphabetic currency code that only needs the existing ASCII case representative should reuse that law if the operand
profile permits it. The Catalog must not create a currency-specific Authority merely to rename an existing case rule.

## 7.17. Time-Zone Identifier Aliases

Time-zone databases contain aliases, but the meaning of a preferred identifier is not as simple as selecting the newest
IANA spelling. CLDR maintains stable canonical identifiers for its own purposes, and those choices can differ from a
current TZDB preference.

A future candidate must therefore name the authority that owns the representative. It must also make the relevant data
version explicit when that version can change the result.

## 7.18. SIP and SIPS URI Profiles

SIP URI comparison has domain-specific rules that cannot be inferred from generic URI normalization.
Case handling depends on the relevant component. Parameters and headers have their own comparison
semantics. Percent-encoding must also follow the SIP rules.

A future candidate should begin from the exact RFC comparison relation. The work remains deferred until the
representative
and the relationship to generic URI constituent laws are closed.

## 7.19. GS1 Digital Link Canonical URI

GS1 Digital Link defines a canonical URI profile, not merely a GTIN text format. It requires HTTPS and
selects a canonical host. It uses the current 14-digit GTIN representation and restricts which query
material remains. This is stronger than the GTIN candidate in Section 6.28.

The full URI law remains deferred because it combines several GS1 identifier rules with URI-level semantics. The
candidate must first prove that these rules form one legitimate Built-In Authority rather than a protocol-owned output
projection or a composition of narrower laws.

## 7.20. UCUM Quantity Canonicalization

UCUM gives units exact semantics and defines relationships to canonical units. That makes it a strong aggregate research
case for a pair such as `(value, unit)`.

A unit cannot be changed without changing the numeric value consistently. Qualification must therefore close conversion
precision and exact arithmetic before any representative is admitted. A text-only unit fold would not preserve the
quantity meaning.

## 7.21. RDF Dataset Canonicalization

RDFC-1.0 is a W3C canonicalization algorithm for RDF datasets. It is useful as a stress case because graph isomorphism
and
blank-node assignment require substantially more work than ordinary text normalization.

The standard produces a canonical serialization of a dataset and discusses denial-of-service risk for difficult inputs.
Kontrakt must first decide whether that output remains inside the Canonicalization presentation boundary or belongs to a
serialization/signing owner. The resource problem must remain separate from semantic equivalence.

## 7.22. LDAP and X.509 String Preparation

LDAP string matching and X.509 name comparison can require preparation before comparison. Their rules
may normalize Unicode or alter case. Some rules also treat insignificant spaces specially. Legality
checks are part of those protocols but are not automatically Canonicalization.

A future candidate must split representative formation from prohibited-input handling before admission. The profile must
not become a shortcut for importing an entire security-sensitive matching pipeline as one opaque law.

## 7.23. SPDX License Expressions

SPDX defines exact syntax and operator meaning for license expressions, but it does not provide one universal canonical
ordering or parenthesization for every semantically equivalent expression.

The current subject therefore remains deferred. Kontrakt should not invent a broad Boolean-algebra normal form merely
because applications would find one convenient.

## 7.24. Signing and Wire Canonicalization

Many important standards use canonicalization to produce signing or wire material. Their existence is evidence for
strict
representation control, but it does not make them inbound Canonicalization laws.

DER and XML canonicalization are examples of standards that control serialization. JWK Thumbprint
instead constructs a hash preimage from selected key material. The relevant serialization, signing, or
identity-derivation authority owns those outputs. A separate same-shape inbound law would need
independent justification.

## 7.25. Generic Filesystem Paths

Generic path normalization remains outside the Catalog. Lexically removing `.` or `..` is not
equivalent to resolving a filesystem path. Symbolic links can change the resolved object. Mount points
and platform rules can change it as well.

A future path candidate needs a much narrower semantic subject. Ambient filesystem state cannot become hidden
Canonicalization meaning.

## 7.26. Generic SQL and Database Identifiers

Database identifier comparison depends on the database and its configured semantics. Case folding and collation can also
depend on deployment configuration.

A generic SQL identifier law would therefore hide ambient semantic state. A database-specific profile could be reviewed
later if its equivalence and representative are closed independently of one running database instance.

## 7.27. Single-Shape International Identifiers

Some international identifiers primarily need validation rather than normalization because their legal representation is
already singular. A fixed-width identifier with no alternate legal spelling does not need a Canonicalization law merely
because it is widely used.

LEI is a useful example of this boundary. Similar identifier families should first prove that multiple legal
representations of the same meaning actually exist before entering the candidate inventory.

## 7.28. Charset Labels

Charset names and aliases are a real interoperability problem, but there is more than one relevant semantic universe.
The
IANA charset registry and the WHATWG Encoding model do not expose exactly the same alias policy.

A future candidate must choose one authority and Version basis before mapping aliases to a representative name. Until
then, a generic "charset label normalize" law would hide an external semantic choice.

## 7.29. Fixed-Width Integer Byte Order

Big-endian and little-endian byte sequences can represent the same fixed-width integer when the byte order is already
known. The raw octets alone do not reveal that order, so a generic byte-order Canonicalization law would require hidden
context.

This remains a stress case rather than a current candidate. Qualification must first decide whether byte order is part
of
Input presentation meaning and whether conversion to an integer belongs to Lowering instead of Canonicalization.

## 7.30. Finite Bit Sets and CPU-Affinity Text

Finite sets of bit positions can have several textual presentations. A dense binary mask and a
hexadecimal mask are common. An index list may instead enumerate the set bits or compress consecutive
indexes into ranges. CPU-affinity tooling demonstrates that these forms occur in real system input.

There is no universal representative across those forms. Sorting a list does not by itself establish
Canonicalization meaning. Compressing ranges or choosing a mask format has the same problem. CPU affinity remains a
useful stress case. An admitted law needs an exact domain profile. Its
representative must be owned by a relevant standard or independently justified by Kontrakt.

## 7.31. Bit Numbering and Bit Order

Bit numbering cannot be inferred from a byte sequence. APIs and protocols differ on whether bit zero is associated with
the least-significant or most-significant position of an octet.

A Built-In law must not select one convention from ambient platform behavior. This subject remains outside the current
candidate set until an exact domain presentation makes the numbering convention explicit. In many cases that convention
belongs to Input interpretation or Lowering rather than Canonicalization.

## 7.32. IPv6 Scoped Address and Zone Identifier

An IPv6 zone identifier can depend on host-local network state. An interface name and numeric interface
index may refer to the same zone only on a particular node. The active network namespace can change
that relationship. The running machine therefore cannot decide the law's equivalence relation.

The RFC 5952 candidate remains limited to the IPv6 address text itself. Scoped-address canonicalization is deferred
unless a future semantic subject supplies a stable explicit zone identity without ambient interface lookup.

## 7.33. Other Researched Domain Cases

The following subjects were investigated during candidate discovery. They are recorded here so that absence from the
current candidate table is deliberate rather than accidental.

| Subject                                       | Current disposition                         | Reason                                                                                                                                |
|-----------------------------------------------|---------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| ISBN and ISSN textual profiles                | Further research                            | Display punctuation and namespace-specific rules must be separated from identifier validity before one representative is proposed     |
| LDAP GeneralizedTime                          | Deferred                                    | RFC 4517 defines equality by the same UTC instant but explicitly does not define canonical LDAP encodings                             |
| OAuth scope sets                              | Aggregate stress case                       | Scope order is not semantic, but the protocol does not define one canonical sort order                                                |
| HTTP Structured Fields                        | Owner review                                | The standard mainly serializes an already parsed abstract field value, so protocol serialization may own the result                   |
| OCI digest text                               | Authority-uniqueness review                 | Much of the visible normalization may already be covered by exact digest grammar plus an existing Base16 law                          |
| ORCID presentation                            | Further research                            | The relationship between bare identifier text, grouped display text, and the recommended HTTPS URI needs one exact operand definition |
| CPE 2.3 names                                 | Owner review                                | The standard separates a semantic name model from string bindings, which may make binding a projection rather than Canonicalization   |
| Protobuf deterministic serialization          | Excluded as evidence of a different problem | Protobuf explicitly warns that deterministic serialization is not a canonical byte representation                                     |
| Binary32 / Binary64 hexadecimal lexical forms | Further research                            | Exact hexadecimal float text is viable, but V1 demand is lower than the decimal lexical candidates                                    |
| Delimited hexadecimal octet sequences         | Authority-uniqueness review                 | Separator removal may already be expressible by a narrower Base16 profile rather than a new Authority                                 |
| CIDR prefix versus dotted netmask             | Boundary / owner review                     | Prefix length is well-defined, but dotted-netmask syntax is not one universal canonical network-prefix presentation                   |
| IP socket-endpoint text                       | Authority-uniqueness review                 | Address and port syntax depend on the enclosing protocol; one universal endpoint textual Authority is not yet justified               |
| DNS name presentation                         | Boundary / composition review               | ASCII case may reuse an existing law, while a trailing dot can distinguish absolute from context-dependent relative interpretation    |
| Service name versus numeric port              | Outside generic Canonicalization            | Registry lookup or protocol defaulting is not a stable equivalence relation between two endpoint presentations                        |
| CRS WKT                                       | Deferred                                    | The standard defines the representation language but does not provide one universal canonical writer                                  |

---

# 8. Admission Decision Record

This is the disposition record for the original 75 candidates and the additional CRLF Law, making
76 subjects. A favorable review alone does not establish historical Catalog membership: only the explicit `ADMIT`
decisions and exact `Initial`
Law specifications in Sections 8.4–8.13 do so. Section 8.2 records two unresolved candidates,
and Section 8.3 records twelve rejected independent Authorities. These decisions are governed by
ADR-0076; candidate-specific background remains in Sections 5–7.

## 8.1. Initial Candidate Review Index

No reviewed candidate remains `ADMIT (proposed)`. The original review notes are retained in
Sections 5–7, the formal normative Law specifications in Sections 8.4–8.13, and the reasons
for unresolved or rejected subjects in Sections 8.2–8.3. This index is not another
Authority or disposition register.

| Candidate family                                        | Research and comparison              | Final decision / exact meaning                                   |
|---------------------------------------------------------|--------------------------------------|------------------------------------------------------------------|
| ASCII text trim and case                                | Sections 5.1–5.2                     | Section 8.4                                                      |
| Unicode whitespace, digits, normalization, case folding | Sections 5.1, 5.3–5.6, 5.9           | Sections 8.5–8.6                                                 |
| WHATWG whitespace collapse, LF and CRLF                 | Sections 5.7–5.8                     | Section 8.11                                                     |
| Decimal text, values, IEEE NaN and XSD float            | Sections 5.12–5.20, 6.7–6.8          | Sections 8.8–8.9                                                 |
| Radix integers and bit representations                  | Sections 5.21–5.24                   | Section 8.10                                                     |
| Base16, Base32 and Web/HTTP syntax                      | Sections 6.1–6.6, 6.9, 6.18–6.21     | Section 8.7; Base64 rejections in 8.3                            |
| Network and CoAP                                        | Sections 6.10, 6.14–6.17, 6.22, 6.34 | Section 8.11; MAC-48 rejection in 8.3                            |
| BCP 47, URN, UUID, `tel:`, DOI                          | Sections 6.11–6.13, 6.23–6.24, 6.26  | Section 8.12; BCP 47 deferral in 8.2; UUID/DOI rejections in 8.3 |
| XML Schema dates and financial identifiers              | Sections 6.25, 6.27–6.28             | Section 8.13                                                     |
| PURL, Kubernetes Quantity, `geo:`, RFC 7468, Avro       | Sections 6.29–6.33                   | Section 8.13; Kubernetes deferral in 8.2                         |

The reviews also reject broad Unicode folds and decimal-comma grouping (Sections 5.10–5.11,
5.16 and 8.3). Additional unratified profiles and future research subjects remain
in Section 7. New favorable reviews require a separate formal membership decision under
ADR-0076.

## 8.2. DEFER

Two candidates in the initial 75 remain `DEFER`; **neither has historical Catalog Membership**.

| Review area          | Candidate                                            | Disposition | Exact unresolved admission gate                                                           |
|----------------------|------------------------------------------------------|-------------|-------------------------------------------------------------------------------------------|
| Identifiers / BCP 47 | Registry-dependent canonical representative          | DEFER       | IANA Registry snapshot content identity and Preferred-Value closure evidence              |
| Cloud-native         | Kubernetes Quantity canonical textual representative | DEFER       | Frozen Kubernetes parse/round/canonicalize baseline and representative-stability evidence |

**BCP 47.** [RFC 5646 §4.5](https://www.rfc-editor.org/rfc/rfc5646.html#section-4.5)
provides the applicable Registry-dependent canonicalization steps. An IANA Language Subtag
Registry with `File-Date: 2026-06-14` was inspected as a *candidate* initial Basis, including
`bh → bih`; however that mutable-location file has **not** been archived and fixed by a
verifiable immutable byte/content identity in this Catalog. Neither the historical Version
basis nor its complete transitive `Preferred-Value`, extension and replacement stability
has therefore been independently verified. `Initial` is not admitted by merely naming
`2026-06-14`, by reading the latest IANA file, or by trusting a provider's current tables.
A future admission must preserve the exact snapshot content identity and validate the
full fixed RFC 5646 normalization pipeline and output-domain closure. The favorable
semantic recommendation in Section 6.12 remains research, not membership.

**Kubernetes Quantity.** The candidate preserves Kubernetes' source suffix family,
precision, rounding and range/capping rules rather than general decimal numeric equality.
Its selected `k8s.io/apimachinery/pkg/api/resource` source version, the exact relationship
between parse-time cached spellings and canonical output, and `C_L(C_L(x)) = C_L(x)`
for that frozen profile must be established before admission. Implementation convenience,
provider version drift, and a different host formatter may not complete this Law.
These missing qualification facts are not Budget/Capacity stops or new law-owned refusals.

## 8.3. REJECT

Rejection concerns the proposed independent law, not necessarily every possible authoring alias.

### Network and IoT

| Review area        | Law working name                                    | Review outcome | Reason for rejection                                                                                                                                                        |
|--------------------|-----------------------------------------------------|----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Protocol / Network | EUI-48 / MAC-48 multi-format textual representative | REJECT         | The broad candidate has no one fixed standard for all accepted source forms and octet interpretations, and its independent Authority boundary against RFC 9911 is unproved. |

This rejection leaves the standards-backed RFC 9911 MAC-48 Law intact. Explicitly scoped additional source-format
conversions may be reviewed independently; common vendor usage does not by itself establish a universal equivalence.

### Text and Unicode

| Review area | Law working name                         | Review outcome | Reason for rejection                                                                                                                                                    |
|-------------|------------------------------------------|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Case        | ASCII case-fold representative           | REJECT         | Its exact ASCII equivalence relation and lowercase representative duplicate the admitted ASCII lowercase law. An alias may reuse that Authority.                        |
| Case        | Unicode default lowercase representative | REJECT         | Default lowercase is case conversion, not a representative of Unicode Default Caseless Matching. No separate general-text equivalence is justified.                     |
| Case        | Unicode default uppercase representative | REJECT         | Default uppercase can erase distinctions retained by Unicode Default Caseless Matching. An independent general-text relation is unjustified.                            |
| Text        | Unicode CJK width fold                   | REJECT         | The broad profile has no one exact width-equivalence rule; multi-scalar composition and unrelated NFC effects remain unresolved. Narrower laws need separate review.    |
| Text        | Unicode diacritic fold                   | REJECT         | No general Unicode accent-removal equivalence exists. Broad removal can erase language- or security-significant distinctions. A narrower profile needs separate review. |

The Unicode default lowercase and uppercase operations are standardized and useful outside this admission decision.
Rejecting these proposed Built-In Authorities does not reject their algorithms or prevent a future narrowly scoped law.
The Catalog does not derive Contract equivalence merely from equality of conversion output. The Unicode Default Full
Case Folding Law is independently admitted in Section 8.6 for its explicitly defined caseless-matching purpose.

The CJK width-fold rejection concerns the unresolved broad subject, not the possibility of separately qualified
full-width ASCII or half-width Katakana laws. The diacritic-fold rejection likewise concerns the proposed generic
same-meaning relation, not accent-sensitive search or narrower standards-backed comparisons. Neither rejection means
that these transformations are inherently nondeterministic; a fixed implementation cannot replace missing Contract
meaning or justify unsafe equivalence.

### Numeric and Numeric Text

| Review area  | Law working name                             | Review outcome | Reason for rejection                                                                                                                                                                                    |
|--------------|----------------------------------------------|----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Numeric text | Decimal-comma grouped textual representative | REJECT         | Changing a comma decimal separator to `.` creates a Text that can denote a different number under the same grouping/decimal grammar. The proposed same-domain representative does not preserve meaning. |

This rejection applies to the original comma-to-dot representative, not to all decimal-comma processing. A separately
qualified Law could remove only valid grouping periods while retaining the comma decimal separator. That narrower
relation must receive its own exact grammar, equivalence, security review, and Catalog disposition before use.

### Encoded Text

| Review area  | Law working name                                    | Review outcome | Reason for rejection                                                                                                                                                                                     |
|--------------|-----------------------------------------------------|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Encoded text | Base64 RFC 4648 canonical textual representative    | REJECT         | The strict RFC 4648 grammar already has one representation per octet sequence. Adding permissive decoder spellings lacks one generic normative equivalence and may erase security-relevant distinctions. |
| Encoded text | Base64url RFC 4648 canonical textual representative | REJECT         | Padding choices are owned by referring protocols. Strict individual profiles already have singular representations, while generic padded/unpadded equivalence is not universally authorized.             |

These rejections concern the proposed generic independent Canonicalization Authorities, not Base64 codecs or
protocol-specific text profiles. A future MIME or other standards-backed profile must independently define the legal
operand spellings and canonical representative. Neither RFC 4648 decoder latitude nor a provider's fallback rule can
supply that meaning. In particular, signature-bearing Base64url text must retain the spelling mandated by its protocol.

### Case-Only Domain Authorities Reusing Existing ASCII Lowercase

| Review area              | Law working name                                 | Review outcome | Reason for rejection                                                                                                                                                               |
|--------------------------|--------------------------------------------------|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Network / Identifier     | RFC 9911 MAC-48 lowercase textual representative | REJECT         | For a properly established MAC-48 Text, the only collapsed distinction and lowercase representative are already owned by the admitted ASCII Lowercase Law.                         |
| Identifier               | RFC 9562 UUID lowercase textual representative   | REJECT         | UUID grammar and validity belong to Input/Admission; canonicalizing its ASCII hexadecimal letter case reuses the exact admitted ASCII Lowercase Authority.                         |
| International identifier | DOI textual representative                       | REJECT         | DOI's Basic Latin-only case equivalence and lowercase representative reuse the exact ASCII Lowercase Authority; non-ASCII and Unicode-normalization distinctions remain preserved. |

These decisions reject *new independent Authorities*, not support for MAC-48, UUID or DOI
Text domains. A legal domain-specific Input may select the already-admitted ASCII
Lowercase Law. The existing Law is not narrowed or re-versioned by such use; a new
protocol grammar or public-facing alias does not create Catalog Membership. Input must
establish valid source syntax before any such selection. In particular, no MAC address
separator recovery, UUID wrapper conversion, or DOI resolver-URL interpretation is implied.

## 8.4. Formal ADMIT — Initial ASCII Text Laws (2026-10-10)

The project owner has approved the five exact ASCII Text laws below for formal Catalog admission. The normative
specifications in this section are approved with the membership decision, rather than inferred from a candidate
review outcome or from implementation registration. Each named law is an independently owned Exact Built-In Law
Authority. These Authority designations identify Contract subjects; they do not fix Kotlin API spellings.

| Independently owned Exact Built-In Law Authority | Initial Version Identity | Formal decision |
|--------------------------------------------------|--------------------------|-----------------|
| ASCII boundary whitespace trim                   | `Initial`                | ADMIT           |
| ASCII leading whitespace trim                    | `Initial`                | ADMIT           |
| ASCII trailing whitespace trim                   | `Initial`                | ADMIT           |
| ASCII lowercase representative                   | `Initial`                | ADMIT           |
| ASCII uppercase representative                   | `Initial`                | ADMIT           |

For each Authority, `Initial` is its own case-sensitive Version Identity. It is neither a compiler-generated
fallback nor a global default. The Authority and Version jointly identify immutable meaning under ADR-0053.
Reusing the spelling under another Authority does not merge histories. Later versions remain under their
owning Authority. Catalog Membership is a historical decision and does not itself create a Version history.

### 8.4.1. Shared Exact Operand and Fixed ASCII Vocabulary

Each of these five Laws accepts already-established legal ADR-0064 Text, including empty Text. The operand is
the exact ordered sequence of Unicode scalars. Its length is also part of that presentation. Neither JVM
`String` behavior nor an arbitrary UTF-16 code-unit sequence defines the Text meaning. Surrogates are not
legal scalars.

The trim Laws use precisely six scalars in the fixed set `W6`: U+0009 TAB through U+000D CR, and U+0020 SPACE.
The inclusive range covers TAB, LF, VT, FF and CR. No other scalar belongs to `W6`. In particular, U+0000 NULL
and U+00A0 NO-BREAK SPACE are excluded, as is U+0085 NEXT LINE. Other Unicode whitespace does not enter by
host classification.

`W6` is a deliberate Kontrakt six-scalar profile.
The [WHATWG Infra Standard](https://infra.spec.whatwg.org/#ascii-whitespace)
defines a different five-scalar ASCII whitespace set that excludes U+000B VT. The
[POSIX locale whitespace classification](https://pubs.opengroup.org/onlinepubs/009695399/basedefs/xbd_chap07.html)
provides comparative evidence for all six scalars but does not supply ambient locale semantics to these laws.
[Java `String.trim()`](https://docs.oracle.com/en/java/javase/26/docs/api/java.base/java/lang/String.html#trim())
removes a broader set of code points at or below U+0020 and is not an implementation specification for `W6`.

### 8.4.2. ASCII Boundary Whitespace Trim — `Initial`

**Exact Equivalence `E_L`.** Two legal Text values are equivalent exactly when each can be written as
`p + t + q`, where `p` and `q` are in `W6*` and both values share the same middle sequence `t`.
The middle sequence is empty, or its first and last scalars are outside `W6`. In the two values,
`p` and `q` may differ independently. Every all-`W6` sequence is therefore equivalent to empty Text.
An interior `W6` scalar between two non-`W6` scalars is retained as a distinction.

**Exact Representative `C_L`.** Remove the maximal initial `W6` prefix and maximal final `W6` suffix
from the operand. Preserve every scalar and its order between those boundaries. Empty Text and any
all-`W6` Text produce empty Text. The representative is unique for the declared equivalence class.

**Exact Representative Coverage.** Total for all legal `Text`. There is no law-owned Canonicalization
refusal for a legal operand. The law owns no external semantic determinants or Required Basis beyond
its fixed `W6` meaning and the established operand.

**Independent Authority decision.** This law is exactly expressible in result as a legal composition of
ASCII leading and trailing trim, in either order. Nevertheless the project owner explicitly approves
one separate Built-In Authority here because direct both-boundary selection is exceptionally common
and has independent declarative value. The admission is a specific, recorded Boundary Trim family exception to the usual
preference for expressing a composition without adding another curated Authority. Unicode Boundary
Trim receives the same limited treatment in Section 8.5.4. Neither admission waives semantic
qualification, authorizes alias inference, or creates a general precedent for other composed laws.
An explicit composition remains its own declared Definition, not an alias of this Authority.

### 8.4.3. ASCII Leading Whitespace Trim — `Initial`

**Exact Equivalence `E_L`.** Two legal Text values are equivalent exactly when both can be written
as `p + t` with `p` in `W6*` and the same remaining sequence `t` across both values, where `t` is
empty or starts with a scalar outside `W6`. Differences at the trailing boundary or in the interior
are not erased.

**Exact Representative `C_L`.** Remove only the maximal initial `W6` prefix and leave the exact
remaining scalar sequence unchanged. Empty Text and all-`W6` Text produce empty Text.

**Exact Representative Coverage.** Total for all legal `Text`, with no law-owned refusal and no
external semantic determinants or Required Basis.

### 8.4.4. ASCII Trailing Whitespace Trim — `Initial`

**Exact Equivalence `E_L`.** Two legal Text values are equivalent exactly when both can be written
as `t + q` with `q` in `W6*` and the same preceding sequence `t` across both values, where `t` is
empty or ends with a scalar outside `W6`. Differences at the leading boundary or in the interior
are not erased.

**Exact Representative `C_L`.** Remove only the maximal final `W6` suffix and leave the exact
preceding scalar sequence unchanged. Empty Text and all-`W6` Text produce empty Text.

**Exact Representative Coverage.** Total for all legal `Text`, with no law-owned refusal and no
external semantic determinants or Required Basis.

### 8.4.5. ASCII Lowercase Representative — `Initial`

**Exact Equivalence `E_L`.** Two legal Text values are equivalent if and only if their scalar
sequences have equal length and, at every position, the scalars are identical or form one of the
26 ASCII case pairs (`U+0041`/`U+0061` through `U+005A`/`U+007A`). Any unequal pair involving
a non-ASCII scalar remains non-equivalent. This relation does not implement Unicode case folding.

**Exact Representative `C_L`.** At each position replace U+0041 through U+005A with the corresponding
U+0061 through U+007A scalar. Preserve all other scalars, positions, and scalar count. In particular
non-ASCII `İ`, `ı`, `ß`, and `K` are unchanged.

**Exact Representative Coverage.** Total on legal Text. There is no Canonicalization-owned refusal. No
external Unicode table or locale participates. The Law has no mutable registry or occurrence-time Required
Basis.

### 8.4.6. ASCII Uppercase Representative — `Initial`

**Exact Equivalence `E_L`.** Use the same 26 ASCII case-pair equivalence specified in Section 8.4.5.
The relation is normatively shared, not inferred from equal implementation results.

**Exact Representative `C_L`.** At each position replace U+0061 through U+007A with the corresponding
U+0041 through U+005A scalar. Preserve every other scalar, position, and scalar count. In particular
non-ASCII `İ`, `ı`, `ß`, and `K` are unchanged.

**Exact Representative Coverage.** Total on legal Text, with no Law-owned refusal. Neither Unicode data nor
the host locale supplies meaning. No external registry or Required Basis is used. The representative differs
from lowercase on ASCII letters, justifying separate Authorities despite shared `E_L`.

### 8.4.7. Pre-Admission Qualification and Verification Evidence

For the trim laws, deleting a uniquely determined maximal boundary sequence yields a unique residual
Text. The independently stated `E_L` for each law is equality of that residual Text. This establishes
reflexivity, symmetry, and transitivity. Each `C_L` preserves the declared equivalence class;
identical representatives coincide exactly with equivalent inputs. A representative cannot retain a
removable boundary scalar, so applying its `C_L` again leaves it unchanged. The same reasoning
covers empty and all-`W6` input.

For both case laws, the 26 disjoint ASCII two-scalar classes and the singleton classes for all
other Unicode scalars induce an equivalence relation at each sequence position. Equality of length
and the position classes induces `E_L` over complete Text. Each representative chooses exactly one
member of every per-position class. No mapping introduces a new ASCII letter requiring another
change; repeated application is stable. Neither law changes Text length or merges non-ASCII scalars.

All five laws admit a single finite scan over an input of `n` Unicode scalars. Work is bounded by
`O(n)` and their output never exceeds `n` scalars. Trim does not expand output; ASCII case mapping
preserves its length. Input's own finite size bounds and the compiler's physical resource handling
remain separate from the Total Coverage statement. No implementation resource stop is a new
Canonicalization-owned refusal.

The following witness pairs distinguish the admitted equivalences from nearby non-equivalences.
Backslash escapes denote the stated Unicode scalar, not literal backslash characters.

| Law                 | Equivalent Text pair     | Non-equivalent Text pair |
|---------------------|--------------------------|--------------------------|
| ASCII boundary trim | `"\u000Babc\t"`, `"abc"` | `"a b"`, `"ab"`          |
| ASCII leading trim  | `"\tabc "`, `"abc "`     | `"abc "`, `"abc"`        |
| ASCII trailing trim | `" abc\r"`, `" abc"`     | `" abc"`, `"abc"`        |
| ASCII lowercase     | `"Ab"`, `"aB"`           | `"ß"`, `"ss"`            |
| ASCII uppercase     | `"Ab"`, `"aB"`           | `"K"`, `"K"`             |

The initial review used independently implemented reference procedures to compare all five
representatives on 19,608 Text strings over a seven-symbol adversarial alphabet at lengths 0 through 5.
It checked idempotence, non-expansion, and both-boundary trim against either order of one-sided
trim. Independently specified equivalence predicates were compared with representative equality
for 160,000 ordered string pairs **per law**, with no mismatches. It also checked ASCII singleton
code-point classification and explicit VT, NULL, NO-BREAK SPACE, Kelvin sign, and dotless-i
regressions. These finite tests supplement, but do not replace, the structural arguments above.
They do not attest to a future Kotlin or JVM implementation.

Security review identified a concrete cross-standard difference: WHATWG ASCII whitespace excludes
U+000B VT, while this exact `W6` law includes it. The difference is explicitly part of the Law
meaning, not a case for silently using a host whitespace predicate. Generic trim or ASCII case
conversion must not repair invalid protocol syntax, alter authenticated raw bytes, or override
protocol-specific comparison rules. The laws do not confer identity or authorization semantics
outside the exact Canonicalization relation selected by the owning Contract. Later Admission and
Lowering consume the established representative through their declared legal relations.

The approved specification and pre-admission evidence satisfy the ADR-0066/0076 gates for these five subjects.
They establish semantic closure and bounded work, together with the independent Authority decisions. They do
not prove that every implementation conforms or every application use is secure. API projection and backend
conformance remain separate.

### 8.4.8. Explicit Membership Decision

**Decision (2026-10-10): ADMIT all five Authority subjects and their approved `Initial` exact
Version meanings in Sections 8.4.2–8.4.6.** The project owner has explicitly approved independent
Catalog membership for each Authority, including the expressly limited Boundary Trim
family exception further recorded for Unicode in Section 8.5.4.
Their membership is now historical and monotonic under ADR-0076. No `ADMIT (proposed)` entry in
Section 8.1 gains membership through this decision. The nine `REJECT` decisions and all remaining
favorable-but-unratified candidates are unchanged. The Catalog does not provide an implicit latest
Version or a fallback to another Authority.

---

## 8.5. Formal ADMIT — Initial Unicode Whitespace and Decimal-Digit Laws (2026-10-10)

The project owner approves four independently owned Unicode Text Authorities for Catalog membership.
The initial exact version of each is bound to the **Unicode Standard 17.0.0**, including the
Unicode Character Database of that version. `Initial` is an exact Kontrakt Law Version Identity
under each Authority, not a request to consult the latest Unicode release. The Unicode standard
release is fixed external semantic material, not an ambient JDK/ICU version selector.

| Independently owned Exact Built-In Law Authority | Initial Version Identity | Decision |
|--------------------------------------------------|--------------------------|----------|
| Unicode boundary whitespace trim                 | `Initial`                | ADMIT    |
| Unicode leading whitespace trim                  | `Initial`                | ADMIT    |
| Unicode trailing whitespace trim                 | `Initial`                | ADMIT    |
| Unicode decimal-digit fold                       | `Initial`                | ADMIT    |

### 8.5.1. Exact Text Domain and Unicode 17.0.0 Basis

Each Law consumes finite, already-established ADR-0064 Text, including empty Text. These Laws do not repair
invalid UTF-16 or reinterpret application source. Their fixed `W17` is exactly Unicode 17.0.0 `White_Space` in
`PropList.txt`. The 25 scalars consist of U+0009–U+000D and U+0020; U+0085, U+00A0 and U+1680; U+2000–U+200A;
U+2028–U+2029; and U+202F, U+205F and U+3000. U+200B, U+FEFF and U+001C are excluded. `W17*` is the set of
finite sequences over this set.

The `Nd` law uses exactly the Unicode 17.0.0 `General_Category=Decimal_Number` (`Nd`)
classification and the corresponding exact decimal digit value 0 through 9. Its meaning excludes
other numeric properties such as Numeric_Type=Digit or Numeric_Type=Numeric. No runtime Unicode
database refresh can change these first Versions. The authoritative sources are
[Unicode 17.0.0 UCD](https://www.unicode.org/Public/17.0.0/ucd/),
[UAX #44](https://www.unicode.org/reports/tr44/), and the
[Unicode 17.0.0 Core Specification](https://www.unicode.org/versions/Unicode17.0.0/core-spec/chapter-3/).

### 8.5.2. Unicode Leading Whitespace Trim — `Initial`

**Exact Equivalence `E_L`.** Legal Text values `x` and `y` are equivalent if and only if
removing each one's maximal initial `W17` prefix yields the same residual scalar sequence.
Their trailing and interior distinctions remain observable.

**Exact Representative `C_L`.** Remove the maximal initial `W17` prefix and retain the
remaining scalars unchanged. Empty and all-`W17` Text map to empty Text.

**Exact Representative Coverage.** Total on its declared legal Text domain. There is no
Canonicalization-owned refusal and no occurrence-time external semantic Basis.

### 8.5.3. Unicode Trailing Whitespace Trim — `Initial`

**Exact Equivalence `E_L`.** Legal `x` and `y` are equivalent if and only if removing each
maximal final `W17` suffix yields identical preceding scalar sequences.

**Exact Representative `C_L`.** Remove the maximal final `W17` suffix. Preserve the complete
initial and interior sequences. Empty and all-`W17` Text map to empty Text.

**Exact Representative Coverage.** Total on its declared legal Text domain, with no
Canonicalization-owned refusal or occurrence-time external Basis.

### 8.5.4. Unicode Boundary Whitespace Trim — `Initial`

**Exact Equivalence `E_L`.** Legal `x` and `y` are equivalent if and only if deleting each
maximal initial `W17` prefix and maximal final `W17` suffix yields the same scalar sequence.
Interior whitespace distinctions remain observable.

**Exact Representative `C_L`.** Remove exactly those maximal boundary sequences. All-`W17`
input and empty input yield empty Text. Other scalar values remain in the same order.

**Exact Representative Coverage.** Total on its declared legal Text domain, with no
Canonicalization-owned refusal or occurrence-time external Basis.

**Independent Authority decision.** Boundary trim is result-equivalent to either order of the
explicitly declared leading and trailing Unicode trim laws. The project owner nevertheless
approves direct both-boundary selection as an independent Authority on the same practical basis
as the admitted ASCII boundary trim. The exceptional allowance is specific to the ASCII and
Unicode *boundary trim family*. It is not a generic exemption for any frequently composed laws.
The direct Law and an explicit composition still have different declared Authority/Definition
relations even where they yield the same representative.

### 8.5.5. Unicode Decimal-Digit Fold — `Initial`

**Exact Equivalence `E_L`.** Two legal Text values are equivalent if and only if they have
the same scalar count and, at each position, either their scalars are identical or both are
Unicode 17.0.0 `Nd` scalars having the same decimal digit value. No other character is
considered equal to an ASCII digit merely by looking numeric.

**Exact Representative `C_L`.** Map each Unicode 17.0.0 `Nd` scalar to its ASCII U+0030–U+0039 digit value.
Other scalars retain their exact position and value. Arabic-Indic and fullwidth `Nd` digits therefore fold.
Circled digits and Roman numerals do not, because they are not `Nd`. The mapping is not a generic
numeric-character conversion.

**Exact Representative Coverage.** Total on legal Text. Each scalar maps to one scalar, so
length is preserved and there is no Canonicalization-owned refusal or occurrence-time Basis.
This law does not validate identifiers or detect mixed-number-script security problems.

### 8.5.6. Qualification, Security, and Membership Decision

For each trim, equality of the uniquely determined residual scalar sequence defines `E_L`.
Removing the specified boundary once leaves no removable scalar on that boundary. The three
relations are equivalence relations, their representatives are unique, and each representative
is idempotent. Their implementation may inspect a finite `n`-scalar input once with bounded
per-scalar membership work, producing no more than `n` scalars. Leading and trailing trim
commute; their combined output is exactly Unicode boundary trim, including empty and all-
whitespace inputs.

For digit folding, the ten possible `Nd` digit values define disjoint equivalence classes. Non-`Nd` scalars
remain singleton classes. Position-wise mapping therefore produces one idempotent ASCII representative without
expansion. Trim tests must show that U+00A0 is removed but U+200B is retained. Digit tests must distinguish
Arabic-Indic U+0661 from circled U+2460: only the former is equivalent to ASCII `1`. Verification uses pinned
Unicode 17.0.0 property files, not Java or ICU defaults.

Unicode UTS #39 discusses mixed-number-script identifier confusion. This is evidence about
law selection in particular identifier/security contexts, not authority to alter the declared
`Nd` equivalence. These laws perform no parser repair, authentication decision, or
identifier validation. Their exact fixed mappings admit finite reference validation;
production backend conformance remains a separately verified realization duty.

**Decision (2026-10-10): ADMIT these four independent Authority subjects and their approved
`Initial` meanings in Sections 8.5.2–8.5.5.** The Unicode Boundary Trim exception is approved
alongside the ASCII Boundary Trim exception; it does not change the Authority Uniqueness
criterion for unrelated law families. Their membership is historical under ADR-0076 and
cannot be reset through later Version or API work.

---

## 8.6. Formal ADMIT — Initial Unicode Normalization and Case Folding Laws (2026-10-10)

The project owner approves seven independently owned Exact Built-In Law Authorities with
Unicode Standard **17.0.0** as the fixed external semantic Basis of each `Initial` Version.
One Unicode version shared by several laws does not merge their independent Authorities or
Version histories. Implementation support for a newer Unicode version is not implicit rebinding.

| Independently owned Exact Built-In Law Authority | Initial Version Identity | Decision |
|--------------------------------------------------|--------------------------|----------|
| Unicode NFC normalization                        | `Initial`                | ADMIT    |
| Unicode NFD normalization                        | `Initial`                | ADMIT    |
| Unicode NFKC normalization                       | `Initial`                | ADMIT    |
| Unicode NFKD normalization                       | `Initial`                | ADMIT    |
| Unicode default full case-fold representative    | `Initial`                | ADMIT    |
| Unicode NFC case-fold profile                    | `Initial`                | ADMIT    |
| Unicode NFKC case-fold profile                   | `Initial`                | ADMIT    |

### 8.6.1. Exact Semantic Basis, Domain, and Coverage

Each law acts on an already-established legal `Text` of Unicode scalar values, including empty
Text. Its `E_L` and `C_L` are defined by the algorithms and data fixed in Unicode 17.0.0.
The four normalization forms follow that version of [UAX #15](https://www.unicode.org/reports/tr15/)
and its normative UCD data. Full Case Folding uses Unicode 17.0.0
[`CaseFolding.txt`](https://www.unicode.org/Public/17.0.0/ucd/CaseFolding.txt), using the
`C` and `F` statuses under Unicode R4, not the `T` Turkic or `S` simple variants.
Identifier Case Folding uses the `NFKC_CF` mapping in Unicode 17.0.0
[`DerivedNormalizationProps.txt`](https://www.unicode.org/Public/17.0.0/ucd/DerivedNormalizationProps.txt),
with the R5 algorithm. D144, D145, and D147 below refer to Unicode 17.0.0
[Core Specification §3.13](https://www.unicode.org/versions/Unicode17.0.0/core-spec/chapter-3/).

The declared operand is finite Text, and the exact representatives are again finite Unicode
scalar sequences. They remain in the same semantic Text family and preserve the surrounding
Input coordinate structure. An individual representative may contain more scalars than its
operand. That fact alone does not create a Restricted Coverage branch. **All seven Laws have
Total Representative Coverage over their declared legal Text domain and no law-owned refusal
for an otherwise legal operand.** No Law may truncate, round, replace or reject a representative
merely because a host buffer was too small. Any exactly narrower Input-owned presentation
meaning must still pass the normal positive selection-applicability and same-domain checks of
ADR-0064/0066 before the Law is used. A hypothetical application-specific maximum length does
not silently redefine the intrinsic Unicode mapping or create an occurrence-time Law refusal.
Where a distinct continuation length policy is declared, Admission evaluates the established
canonical representative. Budget and Capacity own resource limits, including those required
during Canonicalization itself; they do not decide `E_L`, `C_L`, or successful Coverage.

No ambient locale or mutable Unicode setting contributes meaning. Host JVM/ICU data may realize the algorithm
only if it matches the pinned version. Unassigned scalars follow Unicode 17.0.0 data, not a newer release's
classification. A Contract-visible change requires a different Exact Law Version.

### 8.6.2. Unicode NFC Normalization — `Initial`

**Exact Equivalence `E_L`.** `x` and `y` are equivalent exactly when their Unicode 17.0.0
NFD results are scalar-identical. Compatibility-only distinctions remain significant.

**Exact Representative `C_L`.** The Unicode 17.0.0 NFC result of `x`, with decomposition,
canonical combining order, and canonical composition exactly as specified by UAX #15.

**Exact Representative Coverage.** Total on declared legal Text. U+0344 can expand to
U+0308 U+0301 even in NFC; such expansion alone is not Law failure.

### 8.6.3. Unicode NFD Normalization — `Initial`

**Exact Equivalence `E_L`.** Same Unicode 17.0.0 canonical equivalence as Section 8.6.2.

**Exact Representative `C_L`.** The exact Unicode 17.0.0 NFD result. Do not recompose.

**Exact Representative Coverage.** Total on declared legal Text. U+00E9 decomposes into
U+0065 U+0301 without making the operand ineligible.

### 8.6.4. Unicode NFKC Normalization — `Initial`

**Exact Equivalence `E_L`.** Unicode 17.0.0 compatibility equivalence: the inputs' NFKD
representatives are scalar-identical. Canonical equivalence is included.

**Exact Representative `C_L`.** The exact Unicode 17.0.0 NFKC result. Compatibility
replacement is part of the selected Law, not an implementation repair.

**Exact Representative Coverage.** Total on declared legal Text. A compatibility decomposition
may expand one scalar into many; the exact result is not truncated to fit an implementation
buffer. The Law does not assert that compatibility distinctions are irrelevant to every protocol.

### 8.6.5. Unicode NFKD Normalization — `Initial`

**Exact Equivalence `E_L`.** The same Unicode 17.0.0 compatibility equivalence as Section 8.6.4.

**Exact Representative `C_L`.** The Unicode 17.0.0 NFKD result, retaining its decomposed
rather than recomposed representative.

**Exact Representative Coverage.** Total on declared legal Text, including expanding inputs.

### 8.6.6. Unicode Default Full Case Fold — `Initial`

Let `F17(x)` be the exact Unicode 17.0.0 R4 `toCasefold` result, using the complete fixed
Default Full Case Folding mapping. It is not general lowercasing, a locale-sensitive operation,
or Simple/Turkic Case Folding.

**Exact Equivalence `E_L`.** `E_L(x,y)` if and only if `F17(x) = F17(y)` by exact scalar
sequence comparison (Unicode D144). Canonical-equivalent spellings need not match under D144.

**Exact Representative `C_L`.** `F17(x)`. For example U+00DF `ß` becomes the two scalars
`ss`; Unicode-defined Cherokee folding does not have to produce lowercase spelling.

**Exact Representative Coverage.** Total on declared legal Text, including mappings that
expand scalar count.

### 8.6.7. Unicode NFC Case Fold — `Initial`

Let `Q17(x) = NFC17(F17(NFD17(x)))`, where all three operations use Unicode 17.0.0.

**Exact Equivalence `E_L`.** `E_L(x,y)` if and only if
`NFD17(F17(NFD17(x))) = NFD17(F17(NFD17(y)))` by scalar equality, exactly as Unicode D145
Canonical Caseless Matching specifies.

**Exact Representative `C_L`.** `Q17(x)`, the exact NFC representative of that relation.
Unicode §5.18.5 gives the closure property `Q17(Q17(x)) = Q17(x)`. `NFD` must precede folding;
a shortcut treating this law as an unspecified `NFC + lowercase` sequence is not equivalent.

**Exact Representative Coverage.** Total on declared legal Text. Canonical decomposition and
full folding may expand the scalar sequence; expansion alone does not constitute refusal.

### 8.6.8. Unicode NFKC Case Fold — `Initial`

Let `H17(x) = toNFKC_Casefold17(NFD17(x))`. The inner operation uses Unicode R5:
map each scalar through Unicode 17.0.0 `NFKC_CF`, concatenate, and normalize the result to NFC.

**Exact Equivalence `E_L`.** `E_L(x,y)` if and only if `H17(x) = H17(y)`, exactly as Unicode
D147 Identifier Caseless Matching specifies.

**Exact Representative `C_L`.** `H17(x)`. The mapping intentionally collapses some
compatibility and Default_Ignorable distinctions. A generic `NFKC` followed by a host
lowercase or casefold operation may not reproduce R5 or D147.

**Exact Representative Coverage.** Total on declared legal Text. This law is not an
identifier validator, a confusable-character detector, or an authorization decision.

### 8.6.9. Qualification, Security, and Finite-Work Evidence

NFC and NFD have one canonical equivalence relation but different representatives. The NFKC/NFKD pair
similarly shares compatibility equivalence. UAX #15 fixes normalization semantics, including combining order
and composition exclusions. Hangul normalization also follows its prescribed algorithm. Equivalence
preservation and idempotence do not depend on a host normalization method.

Unicode R4 and D144 define the Full Case Folding comparison. Unicode D145 supplies the
canonical-caseless relation, and Unicode §5.18.5 explicitly identifies the idempotent NFC
representative `Q`. Unicode R5/D147 specify the identifier-caseless relation and its fixed
`NFKC_CF` mapping. For R5/D147, the Law's required self-stability and non-equivalence
separation remain verification obligations against the version-pinned normative mapping;
conformance testing does not permit an implementation to choose a different `C_L`.

There is finite work for a finite input with these finite, version-fixed tables and specified
normalization procedures. Combining-mark reordering and intermediate output expansion need
explicit physical analysis. An implementation must not infer constant-time or constant-memory
behavior from bounded input length. Budget/Capacity can stop untrusted-input resource
amplification while retaining their own result and failure attribution; a stop does not turn
Total Coverage into Restricted Coverage. Every supported V1 realization must establish the
exact result when it succeeds and must fail closed on resource inability.

Version-pinned regression witnesses cover NFC expansion at U+0344 and NFD decomposition of U+00E9.
Compatibility folding of `ﬁ` and full folding of `ß` exercise different Law families. D145 also needs the
U+00C5 versus U+0041 U+030A canonical-caseless case; R5 must cover U+200D default-ignorable treatment.
Normative conformance sources are
[NormalizationTest.txt](https://www.unicode.org/Public/17.0.0/ucd/NormalizationTest.txt),
[CaseFolding.txt](https://www.unicode.org/Public/17.0.0/ucd/CaseFolding.txt) and
[DerivedNormalizationProps.txt](https://www.unicode.org/Public/17.0.0/ucd/DerivedNormalizationProps.txt).
Earlier exploratory tests used Unicode 15.1 data. They do **not** prove full Unicode 17.0.0 conformance.

Unicode UAX #31 and UTS #39 explain security risks when identifier distinctions are erased. Compatibility
mapping and case folding are relevant examples; UTS #39 also addresses visually confusable text.
[PRECIS RFC 8264](https://www.rfc-editor.org/rfc/rfc8264.html) warns against indiscriminate compatibility
normalization. These references govern the safety of *using* a Law, not its `E_L`. The Laws do not repair
malformed protocol input or authorize identifier equality. Original-byte authentication remains separately
owned, and Admission cannot restore an erased distinction.

### 8.6.10. Explicit Membership Decision and Remaining Realization Duties

**Decision (2026-10-10): ADMIT these seven independent Authority subjects and their
approved Unicode 17.0.0-based `Initial` exact meanings in Sections 8.6.2–8.6.8.**
Separate formal versions are owned by the seven Authorities; none is a hidden alias of
another. The same `E_L` used by NFC/NFD or NFKC/NFKD does not erase their different
representative obligations. The curated Case Fold profiles own Unicode-standardized
meaning rather than a runtime guess about composing unrelated APIs.

This admission does not establish production-backend conformance with Unicode 17.0.0. Independent conformance
and differential tests remain implementation obligations. Resource-envelope and compiler-preservation checks
are also required. None of those implementation results changes historical Membership or the immutable Law
meaning.

## 8.7. Formal ADMIT — Initial Encoded Text and Web/HTTP Laws (2026-10-10)

**Decision (2026-10-10): ADMIT the nine independent Exact Built-In Law Authorities below.** This section fixes
their separate `Initial` meanings. The four RFC 4648 Laws and five Web/HTTP Laws may share a `Text` carrier or
implementation routines; their Authorities remain distinct. `Initial` never means the newest host parser or
RFC revision.

| Independently owned Exact Built-In Law Authority    | Initial Version Identity | Decision |
|-----------------------------------------------------|--------------------------|----------|
| Base16 lowercase textual representative             | `Initial`                | ADMIT    |
| Base16 uppercase textual representative             | `Initial`                | ADMIT    |
| Base32 RFC 4648 canonical textual representative    | `Initial`                | ADMIT    |
| Base32hex RFC 4648 canonical textual representative | `Initial`                | ADMIT    |
| RFC 3986 percent-encoding syntax representative     | `Initial`                | ADMIT    |
| HTTP-date textual representative                    | `Initial`                | ADMIT    |
| HTTP media-type textual representative              | `Initial`                | ADMIT    |
| HTTP qvalue textual representative                  | `Initial`                | ADMIT    |
| HTTP(S) URI normal-form representative              | `Initial`                | ADMIT    |

### 8.7.1. Common Semantic Boundary, Coverage, and Failure Ownership

Each Law consumes already-established ADR-0064 `Text` and returns a same-coordinate `Text` representative.
Selection must satisfy the specified syntax profile. Coverage is **Restricted**; legal Text outside that
profile receives a deterministic Law-owned refusal, without repair. The parser may not compensate for
malformed punctuation or padding. Nor may it perform an unapproved case fold or percent decode. A refusal here
is distinct from Input illegality or invalid Definition binding. Budget/Capacity stops and implementation
failures keep their own owners.

The fixed normative sources are [RFC 4648 (October 2006)](https://www.rfc-editor.org/rfc/rfc4648.html) for
encoded text and [RFC 3986 (January 2005)](https://www.rfc-editor.org/rfc/rfc3986.html) for URI syntax. HTTP
meaning is fixed by [RFC 9110 (June 2022)](https://www.rfc-editor.org/rfc/rfc9110.html) and
[RFC 6838 (January 2013)](https://www.rfc-editor.org/rfc/rfc6838.html). The specified sections, not later
ambient errata, define each `Initial` Version. Base32 case variants are an express Kontrakt choice; other
decoder aliases are not implied.

### 8.7.2. Base16 Lowercase Text — `Initial`

**Exact Operand.** Empty Text or an even number of ASCII hexadecimal digits `[0-9A-Fa-f]`. Each pair
represents one octet in sequence. Empty Text represents zero octets. The grammar excludes `0x` prefixes and
any separators or whitespace. Base16 uses no padding.

**Exact Equivalence `E_L`.** Two successful operands are equivalent exactly when they
denote the same ordered octet sequence. Letter case is irrelevant; octet count and leading
zero octets remain significant.

**Exact Representative `C_L`.** Replace ASCII `A`–`F` by `a`–`f` and preserve all digits.
Thus `00Af → 00af`; `0` and `0xAB` refuse. The representative is legal and fixed by
reapplication. **Coverage:** Restricted to the stated Base16 grammar.

### 8.7.3. Base16 Uppercase Text — `Initial`

The operand, octet equivalence, and Restricted Coverage are precisely those in Section
8.7.2. **Exact Representative `C_L`:** replace ASCII `a`–`f` by `A`–`F`, preserving
all digits. Thus `00af → 00AF`. Distinct lowercase and uppercase representatives
require distinct Authorities despite their shared `E_L`.

### 8.7.4. RFC 4648 Base32 Text — `Initial`

**Exact Operand.** Empty Text or RFC 4648 Section 6 Base32 using `A`–`Z` and `2`–`7`. ASCII lowercase variants
of letters are also legal by explicit Kontrakt choice. Every nonempty input has length divisible by eight. The
last quantum requires exactly 0, 1, 3, 4 or 6 padding `=` characters as determined by the octet count. A full
quantum has no padding. All unused low-order bits must be zero. Whitespace and `0`/`1` aliases are forbidden.
Padding cannot be missing, early or excessive. Any nonalphabet character or nonzero unused bit also refuses.

**Exact Equivalence `E_L`.** Two successful inputs are equivalent exactly when they
represent identical ordered octets. Their only permitted textual variance for a
fixed octet sequence is ASCII letter case.

**Exact Representative `C_L`.** ASCII-uppercase the Base32 letters and leave digits
and the already-correct padding unchanged. `my====== → MY======`; `MY` and
`MZ======` refuse. The same rule applies to every quantum, including empty Text. **Coverage:** Restricted to the exact
grammar and zero-pad-bit condition.

### 8.7.5. RFC 4648 Base32hex Text — `Initial`

The exact operand and Restricted Coverage use RFC 4648 Section 7 Base32hex instead,
with alphabet `0`–`9`, `A`–`V` and explicitly permitted ASCII lowercase variants.
The same eight-character quantum, exact required padding, and zero unused low-order
bits apply. The value of an encoded symbol is determined only by the Base32hex alphabet,
never by the ordinary Base32 alphabet.

**Exact Equivalence `E_L`.** Equal ordered decoded octets under that exact alphabet. **Exact Representative `C_L`.**
ASCII-uppercase legal letters and preserve digits,
correct padding, and symbol count. Thus `co====== → CO======`; `C5======` refuses
because its unused bits are nonzero, and `CW======` refuses because `W` is outside
the alphabet. No other spelling is repaired.

### 8.7.6. RFC 3986 Percent-Encoding Syntax — `Initial`

**Exact Operand.** An already-established ASCII URI-component Text whose kind is fixed in the selecting
Definition or its legal binding. Supported kinds are `userinfo`, `reg-name` and `segment`, together with
`path-abempty`, `query` and `fragment`. Each uses its RFC 3986 Section 3 grammar and the Section 2.1
`pct-encoded` production. A whole URI cannot determine the kind implicitly. Each `%` must have exactly two
following ASCII hexadecimal digits. Other component kinds are not admitted by `Initial`.

**Exact Equivalence `E_L`.** For one fixed component kind, two successful operands
are equivalent if and only if replacing each percent-encoded unreserved ASCII octet
by its direct unreserved character and uppercasing the hex letters in all remaining
triplets produces identical scalar sequences. Different component-kind bindings
cannot establish cross-kind equivalence.

**Exact Representative `C_L`.** Apply that deterministic transformation once,
without percent-decoding reserved or non-ASCII octets. `%7e → ~`, `%2f → %2F`,
`%25 → %25`; `%GG` refuses. The output must still satisfy the same component grammar. **Coverage:** Restricted to the
chosen legal component profile. Neither whole-URI
parsing nor dot-segment removal belongs to this Law. A later decode, path parser,
or signature consumer must not assume that this Law established resource identity.

### 8.7.7. HTTP-Date Text — `Initial`

**Exact Operand.** Already-established Text matching exactly one RFC 9110 Section 5.6.7 form: `IMF-fixdate`,
`rfc850-date` or `asctime-date`. The RFC's spelling and whitespace grammar is exact. Calendar dates must be
Gregorian-valid, including the supplied weekday. The UTC year lies in `0001`–`9999`. Seconds `00`–`60` are
admitted; `60` is preserved as a leap-second notation rather than carried into the next minute. No leap-second
history lookup or Unix timestamp conversion occurs. Invalid civil fields or a resolved year outside the domain
refuse.

**Exact Required Basis.** One explicitly established UTC Reference Instant used
only when interpreting the RFC 850 two-digit year. Apply RFC 9110's rule by selecting the greatest Gregorian year ending
in the
supplied two digits whose resulting UTC civil date/time is not more than fifty
calendar years after the bound Reference Instant. The fifty-year comparison uses
the reference UTC civil fields with the year advanced by fifty; it is a civil
date comparison, not a floating-point duration estimate. The decoded year must
remain within `0001`–`9999`. This resolves century rollover without relying on
an initial guess based on the host calendar's current century. The basis must
be fixed before evaluation; the compiler clock, current
host clock, and cache reuse time are not semantic inputs. Without a legal Basis,
selection cannot establish this Law.

**Exact Equivalence `E_L`.** Under the same Required Basis, compare the exact UTC civil date and time fields.
Year, month and day must agree; hour and minute must agree; seconds must agree, including the distinct value
`60`. **Exact Representative `C_L`.** Use IMF-fixdate with its three-letter weekday and month, two-digit day
and time fields, four-digit year and literal `GMT`. Under a Basis resolving `94` as 1994,
`Sunday, 06-Nov-94 08:49:37 GMT` becomes `Sun, 06 Nov 1994 08:49:37 GMT`. **Coverage:** Restricted to the RFC
grammar and legal values under the Basis. This Law does not judge freshness or authorization.

### 8.7.8. HTTP Media-Type Text — `Initial`

**Exact Operand.** Already-established ASCII Text under RFC 9110 Section 8.3.1 `media-type`. Parse `token` and
`parameters` using the RFC's `quoted-string` and `quoted-pair` definitions. Type and subtype must be nonempty
tokens. Parameter names must be ASCII tokens and unique after ASCII case folding. OWS is accepted only where
the grammar allows it. The profile excludes empty delimiter-only parameters and `obs-text`, as well as
non-ASCII and extended-parameter forms. Quoted-pairs do not authorize general input repair. No IANA registry
lookup contributes equivalence.

**Exact Equivalence `E_L`.** Type/subtype and parameter names compare without ASCII
case; parameter order is insignificant. Parameter values compare by their exact
resulting ASCII character sequence after legal quoted-pair interpretation, with
case preserved. A token value and a quoted spelling of the same value are equivalent.
Parameters with names equal after ASCII lowercasing are duplicates and refuse.

**Exact Representative `C_L`.** Emit lowercase type/subtype and parameter names.
Sort parameters by ASCII lexicographic order of their normalized names. Emit each
value as an unquoted token if possible; otherwise use double quotes and escape
only literal `\` and `"` with a leading `\`, retaining other admitted value
characters in their original order. Emit `;` between parameters, without OWS.
For example, `Application/Example;Z=ABC;A="xyz"` becomes
`application/example;a=xyz;z=ABC`. The representative's reparse must recover
exactly the same values. **Coverage:** Restricted to the specified common HTTP
syntax. Registration-specific value comparisons, including case-insensitive
`charset` values, are not part of `Initial`.

### 8.7.9. HTTP Qvalue Text — `Initial`

**Exact Operand.** Already-established Text matching the exact RFC 9110 Section 12.4.2 grammar:
`("0" [ "." 0*3DIGIT ]) / ("1" [ "." 0*3"0" ])`. For instance, `0.` and `1.` are valid, while `.5` is not. The
grammar rejects `1.001` and any fourth fractional digit. Signs, whitespace and non-ASCII digits are also
illegal.

**Exact Equivalence `E_L`.** Equality of the exact value in thousandths, from 0 to 1000. No binary
floating-point interpretation is used. **Exact Representative `C_L`.** Write the shortest permitted decimal
spelling. Thus `0.500 → 0.5` and `1.000 → 1`; zero fractional parts disappear. **Coverage:** Restricted. An
out-of-range value refuses rather than being rounded or clamped.

### 8.7.10. HTTP (S) URI Normal-Form Text — `Initial`

**Exact Operand.** An absolute `http` or `https` URI Text parsed under RFC 9110 Section 4.2 and RFC 3986, with
an explicit non-OPTIONS request context. Host is a nonempty ASCII DNS hostname of LDH labels. Each label is
1–63 octets, begins and ends with an alphanumeric character, and contains no empty label or trailing dot.
Total hostname length is at most 253 octets. Numeric-looking legacy IPv4 host names are excluded, as are IP
literals. IDNA and percent-encoded hosts are unsupported. The grammar also excludes userinfo, fragments and
backslash recovery. A port may be absent or empty, or contain an ASCII decimal integer 1–65535 with optional
leading zeros. Empty ports and the relevant default port are legal. Path and query use their RFC 3986
grammars. An empty query delimiter remains distinct from no query.

**Exact Equivalence `E_L`.** Two legal URIs are equivalent when the uniquely defined representative Texts
below agree under this same scheme profile and non-OPTIONS context. This asserts syntax-based normal-form
equivalence only. It does not assert that a server returns the same resource or that an authorization or
signature subject is identical.

**Exact Representative `C_L`.** Parse the complete RFC 3986 component boundaries
first. Lowercase the scheme and ASCII host. Canonicalize an explicit port by
removing leading zeros; omit an empty port and the scheme default (80 for HTTP,
443 for HTTPS). For path and query, uppercase percent-triplet hex letters and
decode only triplets corresponding to RFC 3986 unreserved ASCII characters.
Next, remove path dot segments by RFC 3986 Section 5.2.4, including dots exposed
by that unreserved-only decoding. For a remaining empty path, emit `/` in the
fixed non-OPTIONS request context. Preserve every other path separator, encoded
reserved character, query character and query ordering, and preserve the
presence/absence of `?`. Retain neither a fragment nor userinfo because they
are excluded from the operand.

`HTTP://EXAMPLE.com:80/%7Euser → http://example.com/~user` and
`https://EXAMPLE.com:443/a/./b → https://example.com/a/b` are normal cases;
`http://example.com/a/%2E%2E/b → http://example.com/b` must not first decode
`%2F` or use a permissive URL parser. The representative must pass the same
operand grammar and yield itself on reapplication. **Coverage:** Restricted
to this fixed URI profile. Absence of legal non-OPTIONS context is a selection
applicability failure, not a guess made from runtime request state.

### 8.7.11. Qualification Evidence and Implementation Cautions

[RFC 4648 Sections 3.2–3.5 and 6–8](https://www.rfc-editor.org/rfc/rfc4648.html) define the alphabet and
padding requirements, including zero unused bits. Section 10 supplies test vectors. Verification must reject
permissive-decoder aliases rather than normalize them silently.

The URI syntax and path algorithm are fixed by
[RFC 3986 Sections 2–3 and 5.2.4](https://www.rfc-editor.org/rfc/rfc3986.html).
[RFC 9110 Sections 4.2.3 and 5.6.7](https://www.rfc-editor.org/rfc/rfc9110.html) control HTTP URI and date
meaning; Sections 8.3.1 and 12.4.2 cover media types and qvalues. Tests must preserve encoded slashes and the
distinction between an empty and an absent query. Encoded dot segments and empty/default ports are additional
boundary cases. The non-OPTIONS condition is mandatory.

The following cautions concern implementation and verification, not new `E_L` or `C_L` rules. Earlier ASCII
and Unicode admissions already contain their own evidence. The new Laws require strict parsing and exact
representative formation. A host codec that accepts more spellings is not authoritative.

### 8.7.12. Explicit Membership Decision

These nine Authorities are admitted with the immutable `Initial` profiles in Sections 8.7.2–8.7.10. Each has
its own Restricted Coverage and Law-owned refusal. This decision establishes neither production backend
conformance nor a new authority for general URI resources.

---

## 8.8. Formal ADMIT — Initial Decimal Text Laws (2026-10-10)

**Decision (2026-10-10): ADMIT five independent Decimal Text Authorities** with the
complete `Initial` meanings specified in this section. They were previously recorded as
`ADMIT (proposed)` in Section 8.1. Each Authority owns its own exact Version history;
shared decimal-digit scanning or arithmetic does not merge their meanings. None of these
laws acquires a numeric Fact or changes the already-established Input `Text` coordinate.

| Independently owned Exact Built-In Law Authority   | Initial Version Identity | Decision |
|----------------------------------------------------|--------------------------|----------|
| Decimal integer textual representative             | `Initial`                | ADMIT    |
| Fixed-point decimal textual representative         | `Initial`                | ADMIT    |
| ASCII grouped-decimal textual representative       | `Initial`                | ADMIT    |
| Percentage / per-mille rate textual representative | `Initial`                | ADMIT    |
| Scientific-notation textual representative         | `Initial`                | ADMIT    |

### 8.8.1. Shared Decimal Text Boundary

Each operand is finite, already-established ADR-0064 Text. Its selected Input Presentation must admit the
exact canonical representative. An incompatible narrower presentation cannot be made applicable by deferring
its legality check to Admission. The Laws use fixed grammars; no ambient locale or numeric parser supplies
meaning.

Laws that compare numeric values use exact base-10 mathematics. They do not use binary floating-point
conversion or host Decimal rounding contexts. A selected Text Law always returns Text, not a numeric Fact. Its
own grammar and representative determine equivalence.

### 8.8.2. Decimal Integer Text — `Initial`

**Exact Operand.** Grammar `[+-]?[0-9]+`: at least one ASCII digit, with an optional sign and any number of
leading zeros. No whitespace or Unicode decimal digits are admitted. Decimal points and exponent markers are
likewise excluded.

**Exact Equivalence `E_L`.** Two successful Text operands are equivalent if and
only if their signed base-10 **mathematical integers** are equal. In particular,
`+00012` and `12` are equivalent; a decimal identifier with meaningful leading
zeros must not select this Law merely because its characters are digits.

**Exact Representative `C_L`.** Remove every redundant leading zero, remove the
leading `+`, and keep `-` only for a nonzero negative integer. The unique zero
representative is `0`. Thus `+000123 → 123`, `-00123 → -123`, and `-000 → 0`. **Coverage:** Restricted to the complete
grammar above; otherwise refuse without
repair. Work and output are at most linear in the Input digit count, without
converting the value into a bounded JVM integer.

### 8.8.3. Fixed-Point Decimal Text — `Initial`

**Exact Operand.** Grammar `[+-]?(?:[0-9]+(?:\.[0-9]*)?|\.[0-9]+)`. Exponent notation is excluded. Integers
such as `12` and signed fractional forms such as `-.5` are admitted. `12.` is also valid. No group separators
or Unicode-digit aliases are accepted.

**Exact Equivalence `E_L`.** Equality of exact finite decimal values denoted by the admitted Text. The
relation disregards a redundant sign or leading zero. It also ignores trailing zeros in the fractional part
and collapses negative zero. This is not business-scale equivalence.

**Exact Representative `C_L`.** Omit a positive sign and leading integer zeros, retaining at least one integer
digit. Keep a fractional part only if it is nonzero, and remove its trailing zeros. Negative zero becomes `0`.
For example, `000.0500 → 0.05` and `12. → 12`.

### 8.8.4. ASCII Grouped-Decimal Text — `Initial`

**Exact Operand.** Use an optional ASCII `+` or `-`. The unsigned body is either
the unsigned fixed-point body `[0-9]+(?:\.[0-9]*)?|\.[0-9]+`, or a grouped
integer part `[0-9]{1,3}(?:,[0-9]{3})+` followed by an optional `.` and zero
or more ASCII decimal digits. The `,` is the sole grouping separator, `.` the sole
decimal point, and every group after the first contains exactly three digits.
An ungrouped `.5` is admitted; a grouped form always has an integer part.
No locale-specific grouping, whitespace, or exponent is admitted.

**Exact Equivalence `E_L`.** Two successful operands are equivalent exactly when
deleting their grouping commas gives **scalar-identical Text**. This Law does not
declare `001234.50` equivalent to `1234.5` or collapse a signed zero.

**Exact Representative `C_L`.** Remove only well-placed grouping commas. All other scalars, including signs
and fractional zeros, remain unchanged. Thus `1,234,567.89 → 1234567.89`. Malformed groups such as `12,34`
refuse; they are not repaired.

### 8.8.5. Percentage / Per-Mille Rate Text — `Initial`

**Exact Operand.** A complete Section 8.8.3 fixed-point decimal body, optionally followed by one ASCII `%` or
one PER MILLE SIGN `‰`. An unsuffixed body is also legal. No combined or repeated suffix is permitted.
Exponent notation and extra whitespace refuse.

**Exact Equivalence `E_L`.** Parse the numeric body as exact decimal `d`. With no suffix its rate is `d`. `%`
means `d/100`; `‰` means `d/1000`. Two operands are equivalent only when those exact rational rates agree.
Signed zero is collapsed.

**Exact Representative `C_L`.** Use the unique unsuffixed minimal fixed-point spelling from Section 8.8.3.
Decimal-point movement must be exact, without binary floating-point rounding. For example, `85.5% → 0.855` and
`855‰ → 0.855`.

### 8.8.6. Scientific-Notation Text — `Initial`

**Exact Operand.** Grammar `[+-]?(?:[0-9]+(?:\.[0-9]*)?|\.[0-9]+)[Ee][+-]?[0-9]+`. An exponent marker and at
least one exponent digit are mandatory. Leading signs and zeros are permitted by this grammar. Plain
fixed-point Text is outside Coverage.

**Exact Equivalence `E_L`.** Two successful operands are equivalent exactly when
they denote the same finite mathematical decimal value, with the exponent treated
as an arbitrary-magnitude signed base-10 integer. Signed zero has one meaning.

**Exact Representative `C_L`.** Zero becomes `0E0`. For a nonzero value, put exactly one nonzero digit before
the decimal point. Keep the remaining significant digits through the final nonzero digit, omitting an empty
fractional part. Use uppercase `E` followed by a signed integer exponent in minimal spelling, without a
leading `+` or redundant zeros. The value is unchanged and always remains in scientific notation.

**Coverage:** Restricted to the stated scientific grammar. The exponent must be
handled as an exact decimal integer, not narrowed to JVM `Int`/`Long`. The Law
must not expand `1E999999999999` into fixed-point Text; it is already a valid
representative. A finite scan and digit-string exponent adjustment suffice.

### 8.8.7. Qualification, Security, and Membership Evidence

Each numeric-value Text Law uses exact base-10 arithmetic and chooses a unique spelling. Its transformations
are idempotent. Grouped-Decimal is different: `E_L` compares the strings after comma removal rather than just
numeric values. This preserves leading zeros and fractional-scale spellings. Scientific-Notation also keeps
its own grammar instead of emitting fixed-point Text.

The preliminary review used 5,000 reference samples for each of the five Decimal Text candidates: 25,000
total. It checked grammar, representative stability and value preservation. These were finite reference
checks, not exhaustive backend conformance. Negative tests included invalid groupings and oversized exponents.
A production realization must also test boundary inputs independently.

**Membership decision.** The project owner approves all five Authorities and
their distinct `Initial` meanings above. Their Restricted Coverage refusals
and grammars are part of those meanings. Later backends must prove preservation
of the exact representative and may not broaden the operand through parser
leniency.

---

## 8.9. Formal ADMIT — Initial Numeric Value, IEEE and XSD Floating-Point Laws (2026-10-10)

**Decision (2026-10-10): ADMIT five independent numeric Authorities** with the
complete `Initial` meanings below. The input meaning is different for an
ADR-0064 `Decimal` cohort, a bit-exact binary32/64 datum, and an XSD lexical
`Text`. Sharing a value converter does not create one Authority or permit
substitution of another equality relation.

| Independently owned Exact Built-In Law Authority | Initial Version Identity | Decision |
|--------------------------------------------------|--------------------------|----------|
| Decimal numeric-value representative             | `Initial`                | ADMIT    |
| Binary32 canonical NaN                           | `Initial`                | ADMIT    |
| Binary64 canonical NaN                           | `Initial`                | ADMIT    |
| Binary32 decimal lexical representative          | `Initial`                | ADMIT    |
| Binary64 decimal lexical representative          | `Initial`                | ADMIT    |

### 8.9.1. Decimal Numeric Value — `Initial`

**Exact Operand.** An already-established legal ADR-0064 `Decimal` with an
integer coefficient `c` and exact integral scale `s`, subject to the applicable
Input-owned closed Decimal bounds. Its value is exactly `c × 10^(-s)`.
Coefficient and scale remain distinct in Input Presentation Sameness.

**Exact Equivalence `E_L`.** Two legal Decimal presentations in the same
selected exact Decimal domain are equivalent when their mathematical decimal
values are equal. The relation is not equality of the `(c,s)` pair and does not
perform rounding or application-specific scale/precision policy.

**Exact Representative `C_L`.** For nonzero coefficient `c`, remove every factor of 10 and decrease scale `s`
by the number removed. Zero selects `(0,0)`. Thus `(200,2) → (2,0)`; the other scale-bound cases follow the
same exact arithmetic. The fully reduced result is required, never a partially reduced substitute.

**Coverage and Law-owned refusal.** **Restricted:** the fully reduced pair must remain legal in the same Input
Decimal domain. This includes any bound on coefficient or scale. If not, the Law refuses; it does not stop
reduction early or clip the scale. The rule also applies if `(0,0)` is outside the legal zero domain.

### 8.9.2. Binary32 Canonical NaN — `Initial`

**Exact Operand.** One already-established ADR-0064 IEEE binary32 interchange
datum whose complete 32 bits remain Contract-visible after Input acquisition.
An integer carrier is only a physical realization of those bits.

**Exact Equivalence `E_L`.** Two inputs are equivalent when both bit patterns
are NaNs, or when neither is NaN and all 32 bits are identical. A NaN has all
eight exponent bits set and a nonzero 23-bit fraction. Ordinary IEEE arithmetic
`==` is not this reflexive Contract equivalence relation.

**Exact Representative `C_L`.** Replace every NaN payload, NaN sign, and
signaling/quiet variant by **`0x7FC00000`**. Preserve every non-NaN bit
exactly, including both infinities and signed zero: `0x00000000` remains
distinct from `0x80000000`. **Coverage:** Total over all legal binary32 bit
data; no Law-owned refusal or external semantic Basis.

### 8.9.3. Binary64 Canonical NaN — `Initial`

**Exact Operand.** One already-established ADR-0064 IEEE binary64 interchange
datum with its complete 64-bit Input-visible representation.

**Exact Equivalence `E_L`.** Both inputs are NaN, or both are non-NaN and
all 64 bits are identical. NaN has all eleven exponent bits set and a nonzero
52-bit fraction. Do not use host floating-point comparison.

**Exact Representative `C_L`.** Every NaN maps to **`0x7FF8000000000000`**. All non-NaN bit patterns are unchanged,
including
positive/negative zero and both infinities. **Coverage:** Total, with no
Law-owned refusal or occurrence-time semantic Basis. This Authority has a
separate domain and Version history from binary32.

### 8.9.4. Shared XSD Lexical Profile and Exact Interpretation

These two Laws consume established ADR-0064 Text, not binary floating-point Input. Their fixed source is
[W3C XML Schema 1.1 Part 2, Recommendation of 5 April 2012](https://www.w3.org/TR/2012/REC-xmlschema11-2-20120405/).
Section 3.3.4 fixes the `float` profile; Section 3.3.5 fixes `double`. Appendix E.1 supplies the selected
mapping algorithms. No host parser or newer standard silently changes `Initial`.

Both Laws admit exactly the respective XSD `floatRep` or `doubleRep` complete
lexical grammar, equivalent here to:

```text
[+-]?(?:[0-9]+(?:\.[0-9]*)?|\.[0-9]+)(?:[Ee][+-]?[0-9]+)?|[+-]?INF|NaN
```

The shared grammar admits forms such as `+.5E-2` and `1.`, together with `INF` and `NaN`. It does not admit
`+NaN` or `Infinity`. Whitespace-padded numbers and grouped numbers likewise refuse. XSD's Schema-level
`whiteSpace=collapse` is not imported into this Text Law.

`E_L` is identity of the chosen mapping's **XSD value**, not IEEE arithmetic
`==` and not equality of source exact decimal rationals. Positive and negative
zero are distinct identities; the single XSD notANumber identity is reflexive
under this relation. A numeric string whose exact decimal value differs from
another can still be equivalent to it after rounding to the selected value
space. `C_L` always stays legal Text under the same lexical grammar and
is reapplied without change.

### 8.9.5. XSD Binary32 Decimal Lexical — `Initial`

**Exact Equivalence `E_L`.** Apply the exact selected XSD 1.1 `floatLexicalMap`
to each legal operand and compare the resulting binary32 value identities.
The mapping converts the exact decimal rational directly to binary32 using
the W3C `floatingPointRound` parameters `(24, -149, 104)` and the specified
round-to-nearest, ties-to-even choice, without intermediate binary64 rounding.
The mapping gives distinct signed zero identities where the input sign requires
and one identity for `NaN`.

**Exact Representative `C_L`.** Apply the exact selected
`floatCanonicalMap` to the resulting value. For example the canonical special
forms are positive zero `0.0E0`, negative zero `-0.0E0`, positive infinity
`INF`, negative infinity `-INF`, and notANumber `NaN`. The finite nonzero
representative follows the entire pinned W3C algorithm, including its
significand and exponent spelling; no Java `Float.toString` default can
replace that algorithm by coincidence. A numerical string representing
16,777,217 rounds to the same binary32 value as `16777216` under the chosen
mapping. **Coverage and failure:** Restricted exactly as Section 8.9.4;
all in-grammar inputs succeed.

### 8.9.6. XSD Binary64 Decimal Lexical — `Initial`

**Exact Equivalence `E_L`.** Apply the selected XSD 1.1 `doubleLexicalMap`
and compare binary64 value identities. Decimal-to-binary rounding uses the
exact W3C `floatingPointRound` parameters `(53, -1074, 971)`, with nearest,
ties-to-even and no intermediary narrower or differently rounded host value.
For example the two decimal strings `9007199254740992` and
`9007199254740993` map to the same binary64 finite value.

**Exact Representative `C_L`.** Apply the frozen W3C `doubleCanonicalMap`. Its special representations are
`0.0E0` for +0 and `-0.0E0` for -0. The infinity spellings are `INF` and `-INF`; NaN is `NaN`. Finite nonzero
values use the specified mapping, not RFC 8785 JSON or host `Double.toString`.

### 8.9.7. Qualification, Security, and Membership Evidence

For Decimal Numeric Value, integer division by ten until no factor remains
produces the same unique reduced coefficient/scale pair for a given exact
finite decimal value. The exact Domain-membership check defines the one owned
refusal; Input may not be widened and the reduction may not be approximated.
For each binary NaN Law, finite exponent/fraction bit masks partition the
entire bit domain into one NaN class and singleton non-NaN classes. Choosing
one quiet-NaN bit pattern makes the relation reflexive, symmetric, transitive,
and fixed by reapplication. Raw signaling NaNs must not be quieted by a JVM
floating-point carrier before Input establishes their full bits.

For XSD, the named Mapping algorithms and fixed Value Identity define a unique Text representative.
Verification needs an independent exact-rational oracle for both binary formats. Test subnormal values and
half-ULP ties separately. Also check overflow and sign-preserving underflow. Long decimal operands must not
cause silent approximation.

Very long numeral/exponent Text and Decimal coefficient material require
finite-work and memory-envelope controls during evaluation. Compiler resource
stops are owned by Budget/Capacity or realization failure, not additional
numeric semantics. Signed-zero collapse is intentionally absent from Binary
NaN and XSD value-identity Laws; importing JSON or PostgreSQL equality would
change this decision. IEEE bit arithmetic, Java `BigDecimal` scale overflow,
and platform NaN handling may supply test cases, not normative law meaning.

**Membership decision.** The project owner admits the five independently
versioned Authorities in Sections 8.9.1–8.9.6. Their exact bit, value,
Text, Coverage and refusal meanings are fixed under their respective
`Initial` Versions. Any production implementation must subsequently prove
that it preserves these meanings.

---

## 8.10. Formal ADMIT — Initial Radix and Bit Representation Laws (2026-10-10)

**Decision (2026-10-10): ADMIT seven independent Radix and Bit Representation
Authorities**, with the exact `Initial` meanings below. Equality of a
mathematical integer, equality of an exact-width bit sequence, equality of a
signed integer represented as Bytes, and equality of a POSIX mode mask are
distinct Contract subjects.

| Independently owned Exact Built-In Law Authority            | Initial Version Identity | Decision |
|-------------------------------------------------------------|--------------------------|----------|
| Hexadecimal integer textual representative                  | `Initial`                | ADMIT    |
| Binary integer textual representative                       | `Initial`                | ADMIT    |
| Octal integer textual representative                        | `Initial`                | ADMIT    |
| Fixed-width hexadecimal bit-vector textual representative   | `Initial`                | ADMIT    |
| Fixed-width binary bit-vector textual representative        | `Initial`                | ADMIT    |
| Minimal two's-complement signed-integer byte representative | `Initial`                | ADMIT    |
| POSIX file-mode octal textual representative                | `Initial`                | ADMIT    |

### 8.10.1. Radix Integer Text — Three Separate `Initial` Laws

**Exact Operand.** The hexadecimal, binary and octal Authorities each consume legal Text. Their respective
grammars are `[+-]?[0-9A-Fa-f]+`, `[+-]?[01]+` and `[+-]?[0-7]+`. Signs and leading zeros are permitted.
Prefix-based radix inference is not. Each Law interprets digits in its fixed radix.

**Exact Equivalence `E_L`.** For each distinct Authority, the two legal Text
values are equivalent if and only if their exact signed mathematical integers
in **that** radix agree. For example `10` represents 2 in binary, 8 in octal,
and 16 in hexadecimal; there is no cross-Law equality relation.

**Exact Representative `C_L`.** Remove redundant leading zeros and leading
`+`; retain `-` exactly for a nonzero negative value, and select `0` for zero.
The hexadecimal Law also maps `A`–`F` to lowercase `a`–`f`. Witnesses:
hex `+000FF → ff`, binary `-0000 → 0`, octal `+000755 → 755`. **Coverage:** Restricted to the respective exact grammar,
with Law-owned
non-repairing refusal on grammar mismatch. Each remains Text and may be
formed by scanning digits rather than narrowing an arbitrarily large integer
to a JVM numeric carrier.

### 8.10.2. Fixed-Width Hexadecimal Bit-Vector Text — `Initial`

**Exact Operand.** A nonempty sequence of ASCII hexadecimal digits, optionally
separated by single `_` characters between adjacent digits. Every `_` must
have one hexadecimal digit immediately before and after it, so leading,
trailing, or consecutive underscores are illegal. Prefixes, signs,
whitespace, and `0x` are excluded. The represented bit-vector width is **four times the number of hexadecimal digits**,
excluding underscores.

**Exact Equivalence `E_L`.** Two successful inputs are equivalent if and only
if they have identical bit widths and bit sequences. Hexadecimal letter case
and legal underscore separators carry no meaning. No zero extension or
truncation is permitted; `F` (4 bits) and `000F` (16 bits) are not equivalent.

**Exact Representative `C_L`.** Remove underscore separators and lowercase ASCII `A`–`F`, preserving all
hexadecimal digits and their width. For example, `00_Af → 00af`. A leading zero digit is never discarded.

### 8.10.3. Fixed-Width Binary Bit-Vector Text — `Initial`

**Exact Operand.** A nonempty sequence of ASCII `0` and `1` digits with only
single `_` separators between adjacent digits. Leading, trailing, repeated
underscores, prefixes, signs, and whitespace are illegal. Width is exactly
the number of binary digits, excluding underscores.

**Exact Equivalence `E_L`.** Two legal operands are equivalent if and only if
the ordered bits and their exact width are identical. Separator placement is
the only ignored distinction. `01` (2 bits) and `1` (1 bit) are distinct.

**Exact Representative `C_L`.** Delete underscores and preserve every bit,
including leading zeros: `0000_1111 → 00001111` and `0_1 → 01`. **Coverage:** Restricted to the complete grammar, with
exact non-repairing
failure on malformed separators or digits.

### 8.10.4. Minimal Two's-Complement Signed-Integer Bytes — `Initial`

**Exact Operand.** Nonempty ADR-0064 `Bytes`, interpreted as a signed big-endian two's-complement integer of
arbitrary finite width. The input is an octet sequence, not a host integer or ASN.1 DER encoding. Empty Bytes
are outside Coverage.

**Exact Equivalence `E_L`.** Two nonempty Byte sequences are equivalent if
and only if they represent the same signed mathematical integer under that
big-endian 2's-complement interpretation. No tag, length field, or separate
ASN.1 object is parsed.

**Exact Representative `C_L`.** Choose the shortest nonempty big-endian two's-complement bytes for the same
integer. Remove a leading `00` only while the next octet's top bit is 0. Remove a leading `FF` only while that
bit is 1. A necessary sign-preserving byte and the final octet must remain. For example, `00 80 → 00 80`
retains the positive sign, while `FF FF 80 → 80` removes redundant negative sign extension.

**Coverage:** Restricted exactly by the nonempty requirement. Empty Bytes
causes one Law-owned refusal. General DER INTEGER is a different protocol
subject: [ITU-T X.690](https://www.itu.int/rec/T-REC-X.690/) already requires
minimum 2's-complement INTEGER contents in its legal encoding. This Law
normalizes general input Bytes and does not validate or emit ASN.1 framing.

### 8.10.5. POSIX File-Mode Octal Text — `Initial`

**Exact Operand.** Nonempty Text of ASCII octal digits `0`–`7`, interpreted as one POSIX 12-bit mode mask.
Signs and `0o` prefixes are excluded. Underscores, whitespace and symbolic permission expressions also refuse.

**Exact Equivalence `E_L`.** Two successful operands are equivalent exactly
when they denote the same **12-bit POSIX-style mode mask**. It is not merely
matching the lowest nine access bits: set-user-ID, set-group-ID, and the
sticky bit must be retained. `0755` is not equivalent to `4755`.

**Exact Representative `C_L`.** Write exactly four ASCII octal digits for the same mask, adding leading zeros
without discarding mode bits. For example, `755 → 0755`, while `4755` remains `4755`. A value above `7777`
refuses; so do `0o755` and symbolic expressions such as `u+rwx`. **Coverage:** Restricted to the exact syntax
and 12-bit range. The Law does not consult filesystem state or the process umask. It also does not inherit
YAML's implicit integer parsing.

### 8.10.6. Qualification, Security, and Membership Evidence

The three radix Laws select a unique sign/zero/digit spelling per exact
integer under a **fixed** radix. The two Bit-Vector Laws preserve every
bit-width distinction while removing permitted textual separators and hex
letter case; their representatives cannot lose width and are already fixed
points. For signed Bytes, the two redundant-sign-prefix tests yield a unique
minimal nonempty 2's-complement representation for every signed integer;
negating or modifying a necessary sign byte would change its value. For POSIX
Mode, the exact 12-bit range determines one four-digit representation without
collapsing privilege-related bits. All seven `C_L` functions choose one
representative for each claimed `E_L` class within their respective successful
domains, preserve that class, and are idempotent.

The pre-admission independent references covered 15,000 radix integer samples and 20,000 Bit-Vector samples.
Every one of the 65,536 two-octet Signed-Integer inputs was checked. A further 12,288 POSIX-mode and
leading-zero samples were exercised. No disagreement was observed for the checked properties. These samples
are not a proof for arbitrary lengths or backend conformance.
[SMT-LIB FixedSizeBitVectors](https://smt-lib.org/theories-FixedSizeBitVectors.shtml) and LLVM `APInt` support
independent width checks; X.690 supplies a separate signed-integer reference. None overrides the exact operand
grammar.

Implementation must preserve the distinction between signed integer value,
bit-vector width, and POSIX authorization-related mode bits. Signed bytes
must never pass through an automatically truncated JVM integer; leading
`00` and `FF` may be removed only under the explicit next-sign-bit tests.
No radix auto-parser or YAML default may decide POSIX mode meaning. The
supported algorithms inspect only finite legal Text/Bytes and need no
unbounded recursive interpretation. Budget/Capacity own resource stops;
they do not enlarge Restricted Coverage or supply additional failure codes.

**Membership decision.** The project owner admits seven independent `Initial` Authorities. Their exact
grammars and representatives are fixed above, including the distinctions of width and signedness. Coverage and
refusals remain Law-owned. Any semantic change requires a new exact Version. Shared implementation code cannot
merge the Authorities; realization conformance is verified separately.

---

## 8.11. Formal ADMIT — Initial Text and Network Laws (2026-10-10)

**Decision (2026-10-10): ADMIT the eight independent Authorities below** with the exact `Initial` meanings established
in this section. Each owns its own historical Catalog Membership and immutable Law Version. Shared scanners, address
parsers, or URI processing routines do not merge Authorities. An already-established ADR-0064 `Text` is the operand of
each Law; a malformed or unadmitted presentation is never repaired into a legal Input.

| Independently owned Exact Built-In Law Authority | Initial Version | Decision | Coverage   |
|--------------------------------------------------|-----------------|----------|------------|
| WHATWG ASCII whitespace collapse                 | `Initial`       | ADMIT    | Total      |
| LF line-ending normalization                     | `Initial`       | ADMIT    | Total      |
| CRLF line-ending normalization                   | `Initial`       | ADMIT    | Total      |
| Historical IPv4 numbers-and-dots Text            | `Initial`       | ADMIT    | Restricted |
| IPv4 network-prefix Text                         | `Initial`       | ADMIT    | Restricted |
| RFC 5952 IPv6 all-hex Text                       | `Initial`       | ADMIT    | Restricted |
| IPv6 network-prefix Text                         | `Initial`       | ADMIT    | Restricted |
| CoAP URI normal-form Text                        | `Initial`       | ADMIT    | Restricted |

### 8.11.1. WHATWG ASCII Whitespace Collapse — `Initial`

The immutable source is
[WHATWG Infra,
`strip and collapse ASCII whitespace`](https://infra.spec.whatwg.org/#strip-and-collapse-ascii-whitespace).
This `Initial` Law consumes already-established Unicode-scalar Text. Its `W5` contains U+0009 TAB, U+000A LF,
U+000C FF, U+000D CR and U+0020 SPACE. Vertical Tab U+000B is **not** included. This exact difference from
`W6` is intentional.

**`E_L`:** two Text values are equivalent exactly when the nonempty maximal runs of characters outside `W5` agree
positionally, with intervening `W5` runs regarded as one separator and leading/trailing `W5` runs disregarded. Thus
`"AB"` and `"A B"` are *not* equivalent. **`C_L`:** remove leading and trailing `W5`, replace each nonempty interior
`W5` run by exactly one U+0020 SPACE, and preserve all remaining scalars. Empty and all-`W5` Text yield empty Text.
**Coverage:** Total; no Law-owned refusal or occurrence-time Basis. The representative is idempotent, cannot grow, and
requires a finite linear scan.

**Implementation caution:** never substitute JVM `trim`, `\s`, or a Unicode whitespace predicate; include the U+000B and
U+00A0 non-collapse regressions. A protocol delimiter erased by this Law cannot be recovered by Admission.

### 8.11.2. LF Line-Ending Normalization — `Initial`

The fixed selected rule is equivalent to [WHATWG Infra,
`normalize newlines`](https://infra.spec.whatwg.org/#normalize-newlines), restricted to CRLF, standalone CR, and LF. **
`E_L`:** equality after replacing every U+000D U+000A pair with one U+000A and every remaining U+000D with U+000A. **
`C_L`:** that exact replacement, performed without treating the LF of a former CRLF pair as a second line break.
`"A\r\r\nB"` maps to `"A\n\nB"`; U+0085 and U+2028/U+2029 are preserved. The operand may mix CRLF, standalone CR,
and standalone LF in any order. **Coverage:** Total on legal `Text`, with no Law-owned refusal
or Required Basis. The transformation is nonexpanding, deterministic, and idempotent.

**Implementation caution:** match CRLF as a pair before converting lone CR, including adjacent CR and empty input. This
Law does not trim line content or impose source-control configuration.

### 8.11.3. CRLF Line-Ending Normalization — `Initial`

**Exact Operand.** Every already-established legal ADR-0064 `Text`, including empty Text. A single
operand may contain CRLF, standalone CR, and standalone LF in any mixture. The interpretation is
fixed by this Law, not by the host operating system or a protocol parser.

**Exact Equivalence `E_L`.** Read Text from left to right. A U+000D CR immediately followed by
U+000A LF is one line-break token, consuming both scalars. A remaining standalone CR or LF is
one line-break token. Every other scalar is an exact, unchanged token. Two Text values are
equivalent if and only if their complete token sequences are identical. This is the same
`E_L` as the admitted LF Law in Section 8.11.2.

**Exact Representative `C_L`.** Emit exactly U+000D U+000A for each line-break token and preserve
every other scalar in order. No line break is inserted or removed. Empty Text remains empty.
The representative of `"A\r\nB\rC\nD"` is `"A\r\nB\r\nC\r\nD"`.
The operand `"A\r\r\nB"` contains two breaks and becomes `"A\r\n\r\nB"`.
No Unicode line separator other than the specified CR and LF participates in this relation.

**Exact Representative Coverage.** Total for admitted legal `Text`, with no Law-owned refusal
or Required Basis. Each finite input has one finite representative, even when the output expands.
The Law fixes no output-length threshold. Admission owns any declared continuation-length
judgment, while Budget and Capacity own resource exhaustion. An Input-owned semantic restriction
that makes the required representative inapplicable is resolved through positive selection
applicability, not a new occurrence-time Canonicalization refusal.

**Independent Authority decision.** LF and CRLF remove precisely the same representation
freedom but select different exact representatives. The CRLF Law cannot be an alias of LF.
[RFC 5198 §2](https://www.rfc-editor.org/rfc/rfc5198.html#section-2) supports CRLF as a required
line-ending presentation in its own network-text profile. It does not extend that protocol's
input grammar or confer validity on an otherwise illegal message. This Law owns only the
explicitly selected Text equivalence above.

**Implementation caution.** Consume a CRLF pair before interpreting standalone CR or LF.
Do not normalize each scalar independently or merge adjacent line breaks. No host newline
setting may choose `C_L`. Output expansion requires finite work planning, but an implementation
resource stop must retain Budget/Capacity failure ownership. The Law does not make an invalid
HTTP or other protocol message legal.

### 8.11.4. Historical IPv4 Numbers-and-Dots Text — `Initial`

The fixed source is the historic BSD/Unix `inet_aton` numbers-and-dots profile documented in
[Linux `inet(3)`](https://man7.org/linux/man-pages/man3/inet.3.html). It is not the language of every POSIX
parser. Input has one to four nonempty unsigned numeric parts separated by dots, without spaces or signs. Each
part is decimal by default. A multi-digit part beginning with `0` is octal; a `0x` or `0X` prefix selects
hexadecimal and must be followed by hex digits. All characters must be consumed; `08` is invalid octal. With
four parts the widths are `(8,8,8,8)`. Three parts use `(8,8,16)`, two use `(8,24)`, and one uses all 32 bits.
Each part must fit its own unsigned width, without truncation.

**`E_L`:** exact equality of the resulting 32-bit IPv4 address. **`C_L`:** four minimal unsigned decimal
octets separated by periods. For example, `127.1 → 127.0.0.1` and `0x7f.1 → 127.0.0.1`. The representative is
itself a legal four-part operand and a fixed point. **Coverage:** Restricted to the specified grammar and part
bounds. Malformed input and overwide parts refuse.

**Implementation caution:** reject any `inet_aton` host extension beyond the fixed grammar. Signed numbers and
trailing garbage are invalid. Legacy octal `8` or `9` and overwide parts also refuse. Selection must be
explicit; this Law does not broaden strict IPv4 Input.

### 8.11.5. IPv4 Network-Prefix Text — `Initial`

This Law selects the [RFC 9911](https://www.rfc-editor.org/rfc/rfc9911.html) strict IPv4 prefix representation. The
operand is an ASCII dotted-decimal four-octet address with an explicit `/` and an ASCII decimal prefix length from `0`
through `32`. Each octet is `0`–`255`, written in strict decimal without legacy octal/hex interpretations; the
representative uses minimal digits. **`E_L`:** equality of the prefix length and the address's first `length` bits. **
`C_L`:** clear the remaining 32−`length` host bits, format the four octets in strict decimal, and retain the minimal
decimal length. `192.0.2.129/24 → 192.0.2.0/24`, and `255.255.255.255/0 → 0.0.0.0/0`. **Coverage:** Restricted; illegal
octets, prefix lengths, or delimiters refuse.

A `/24` and a `/25` cannot be equated even when their text starts with the same address. **Implementation caution:** do
not parse the address through the admitted historical IPv4 Law; mask bits only after strict parsing and prefix-length
validation.

### 8.11.6. RFC 5952 IPv6 All-Hex Text — `Initial`

The operand is one unscoped IPv6 address under [RFC 4291](https://www.rfc-editor.org/rfc/rfc4291.html), with
RFC 5952 Section 4 chosen as its sole representative. **`E_L`:** equality of the complete 128-bit address. **`C_L`:**
lowercase the hexadecimal digits and remove redundant zeros in each field. Compress the longest
consecutive run of at least two zero fields; a tie chooses the leftmost run. The representative remains
all-hexadecimal, including for an IPv4-embedded input. Zone IDs and URI brackets are outside Coverage.

RFC 5952 Section 5's suggested mixed IPv4 notation does not override this Section 4 all-hex representative.
**Implementation caution:** test longest-zero-run ties and single zero fields. Embedded IPv4 and all-zero
inputs also require regression cases. Neither DNS nor interface lookup may affect `C_L`.

### 8.11.7. IPv6 Network-Prefix Text — `Initial`

The operand is the above exact legal IPv6 address Text followed by `/` and a strict decimal prefix length `0`–`128`,
under the fixed [RFC 9911](https://www.rfc-editor.org/rfc/rfc9911.html) prefix profile. **`E_L`:** equality of prefix
length and the first `length` bits of the 128-bit address. **`C_L`:** clear all non-prefix bits, emit the address with
this Catalog's RFC 5952 Section 4 all-hex rule, and append the minimal decimal `/length`.
`2001:db8::1/64 → 2001:db8::/64`, and `ffff::1/0 → ::/0`. **Coverage:** Restricted to exact IPv6-address/prefix syntax
and range; out-of-profile Text refuses.

The IPv6 address Authority and the IPv6 prefix Authority remain distinct because the latter erases host-bit distinctions
that the former preserves. **Implementation caution:** handle bit boundaries not divisible by eight or sixteen,
especially `/1`, `/63`, `/65`, `/127` and `/128`; avoid implicit host IPv6 mixed formatting.

### 8.11.8. CoAP URI Normal Form — `Initial`

The immutable sources are [RFC 7252 §6.3](https://www.rfc-editor.org/rfc/rfc7252.html#section-6.3) and RFC

3986. Input is a complete absolute `coap` or `coaps` URI Text under a restricted host profile. The host may be
      strict ASCII DNS, dotted-decimal IPv4, or bracketed unscoped IPv6. It must be nonempty. Userinfo and fragments
      are excluded; so are zone identifiers and non-ASCII/IDNA or percent-encoded hosts. Backslash recovery and
      ambiguous legacy numeric hosts are forbidden. Parse the exact RFC 3986 authority, path and query. Percent
      triplets must be legal, and any unsigned decimal port must lie in the permitted range.

**`E_L`:** equality of the scheme-based RFC 7252 normal form, not equality of resolved resources. **`C_L`:**
lowercase the scheme and ASCII DNS host. IPv6 literals use the admitted RFC 5952 all-hex form. Elide empty or
default ports (`5683` for `coap`; `5684` for `coaps`). Decode only permitted percent-encoded unreserved
octets. Uppercase hex digits in remaining percent triplets, then apply the RFC 3986 dot-segment rule in the
fixed order. Empty path becomes `/`; query byte distinctions and order are preserved. Thus
`coap://EXAMPLE.com:5683/%7Esensors → coap://example.com/~sensors`. The schemes `coap` and `coaps` are
distinct. **Coverage:** Restricted; illegal or ambiguous components refuse.

**Implementation caution:** parse URI components once. Never decode reserved `%2F` as a path separator or
decode the representative again. Query parameters must retain their original order. DNS lookup is not part of
this Law.

### 8.11.9. Shared Qualification and Membership Evidence

The whitespace-collapse Law defines an equivalence relation by its unique representative.
The LF and CRLF Laws share the exact newline-token `E_L`. Each selects one legal representative
of every token sequence. Parsing CRLF as one token preserves the number of line breaks in mixed
input. A second application of either Law leaves its own representative unchanged. For example,
`"A\r\nB"` and `"A\rB"` are equivalent, while `"A\r\n\r\nB"` has two breaks and
is not equivalent to either. An exhaustive reference check covered 19,531 Text strings of lengths
zero through six over `A`, `B`, CR, LF, and U+2028. It checked both Laws' fixed points and
preservation of their shared equivalence. These checks do not attest to a Kotlin/JVM backend.
The network Laws depend on
finite-width bit calculations and fixed RFC parsing. These operations have bounded work over a finite legal
input. Unsupported host syntax must refuse rather than be recovered by a permissive URL parser.

Their fixed source profiles and exact representatives are the approved `Initial` meanings.
The newline Laws require one finite scan. CRLF replacement can expand Text but introduces no
Law-owned resource cap or refusal. Admission and Budget/Capacity retain their respective
judgments. Bounded parsing and finite output work are realizable; host-specific and stress tests
remain implementation-verification duties. Backend failure cannot change Catalog Membership.

**Explicit Membership decision:** ADMIT all eight exact Authority subjects in the table. Neither the rejected RFC 9911
MAC-48 lowercase convenience subject nor another domain-specific name can duplicate the existing ASCII Lowercase
Authority; its rejection is recorded separately in Section 8.3.

---

## 8.12. Formal ADMIT — Initial Language-Tag Casing, URN and `tel:` URI Laws (2026-10-10)

**Decision (2026-10-10): ADMIT the three independent Authorities below.** Each owns an `Initial` Version. A
common Text carrier or parser routine does not create cross-domain equivalence. Language tags, URN assigned
names and telephone URIs retain their separate semantics.

| Independently owned Exact Built-In Law Authority | Initial Version | Decision | Coverage   |
|--------------------------------------------------|-----------------|----------|------------|
| BCP 47 registry-independent casing               | `Initial`       | ADMIT    | Restricted |
| RFC 8141 URN assigned-name text                  | `Initial`       | ADMIT    | Restricted |
| RFC 3966 restricted `tel:` URI text              | `Initial`       | ADMIT    | Restricted |

### 8.12.1. BCP 47 Registry-Independent Casing — `Initial`

The exact operand is an ASCII Text matching [RFC 5646 §§2.1–2.2](https://www.rfc-editor.org/rfc/rfc5646.html),
including the recognized grandfathered spellings. This is a registry-independent casing Law: it does not
consult IANA during evaluation. **`E_L`:** ASCII case-insensitive comparison of two legal tags without
changing subtag order or content. **`C_L`:** choose RFC 5646 conventional casing according to each parsed
subtag's role.

Do not apply Script or Region casing inside extensions and private-use sequences. Preferred-Value replacement
belongs to the separate Registry-dependent candidate. This Law also performs no extension sorting or Variant
reordering. `Suppress-Script` is never implicitly removed.

### 8.12.2. RFC 8141 URN Assigned-Name Text — `Initial`

The operand is one syntactically legal
[RFC 8141 §3.1](https://www.rfc-editor.org/rfc/rfc8141.html#section-3.1) URN assigned name: scheme, NID and
NSS. The optional `?+` r-component and `?=` q-component are excluded, as is the `#` f-component. **`E_L`:**
ignore only scheme/NID ASCII case and percent-triplet hex case in NSS. Namespace-specific equivalence is not
inferred. **`C_L`:** write `urn:` and lowercase NID; uppercase hex digits in NSS triplets. Other NSS
characters and percent-encoding remain exact.

**Implementation caution:** this Law neither percent-decodes unreserved NSS characters nor determines namespace-specific
identity or URN resolution. A whole URN with resolver/query/fragment components must not lose those components through
an implicit narrowing operation.

### 8.12.3. RFC 3966 Restricted `tel:` URI Text — `Initial`

The fixed source is [RFC 3966 §§3–5](https://www.rfc-editor.org/rfc/rfc3966.html). V1 admits a strict ASCII
`tel:` subset. A global number starts with `+`; a local number must have exactly one explicit `phone-context`.
One optional `ext` parameter is supported. `isub`, other mandatory or extension parameters, and unknown
parameters refuse. The grammar follows RFC `phonedigit` and `phonedigit-hex`. Its visual separators are `-`,
`.`, `(` and `)`, and the RFC rules admit local `*` and `#`. The admitted `descriptor` and `ext` productions
remain exact. The context is either a valid ASCII DNS domain or a global number prefix; no host locale or
caller region is inferred.

**`E_L`:** use RFC 3966 equality on the admitted fields. Global and local numbers stay distinct. Remove
allowed visual separators from numbers and numeric contexts. Domain contexts compare by ASCII case folding.
Parameter names and `ext` contents follow the RFC's comparison rules; absent `ext` differs from present `ext`. **
`C_L`:** lowercase the `tel:` scheme. Remove those visual separators and lowercase a domain context. Emit
the selected order with `ext` before `phone-context`. For example, `TEL:+1-212-555-0100 → tel:+12125550100`.
**Coverage:** Restricted. Unknown or repeated parameters, invalid context and missing required local context
refuse. No dial-string repair is permitted.

**Implementation caution:** numeric `phone-context` is not automatically appended to form a global number. This Law
makes no claim about actual number allocation, current dialing plan, or whether dialing succeeds. Routing and protocol
authorization remain separate.

### 8.12.4. Qualification and Membership Evidence

RFC 5646 grammatical subtag roles and RFC 8141 generic assigned-name equivalence fix stable normal forms independent of
a mutable provider. RFC 3966 fixes the admitted core comparisons, visual-separator set, and parameter meaning; excluding
extension parameters prevents a new deployment from changing the Law's equivalence. Each successful result is legal
under its own exact grammar, and a second application cannot change the representative. Finite parsers with
component-boundary checks can realize these Text rules without callbacks, current clock, or runtime registry lookup.
Near-miss tests must cover language-tag extension context, URN `%2F` versus slash, and telephone local/global and
context distinctions.

**Explicit Membership decision:** ADMIT these three Authorities and their `Initial` meanings. The separate
*registry-dependent* BCP 47 candidate is **not** included: its actual IANA Registry byte snapshot is not archived or
content-pinned in this Design, so its pre-admission Version determinant remains unclosed. Section 8.2 records that
candidate as `DEFER` without representing the favorable research recommendation as historical Membership.

---

## 8.13. Formal ADMIT — Initial Temporal, Identifier, Supply-Chain, Geographic, Security-Text and Avro Laws (2026-10-10)

Unless expressly stated otherwise, each of these Laws uses already-established ADR-0064 Text and returns Text.
Coverage is Restricted to the specified exact grammar. Out-of-profile Text receives a deterministic Law-owned
refusal. Input illegality and missing Required Basis remain separately owned; so do Budget/Capacity stops and
realization failures. This section specifies representatives, not generic serialization or authorization.

| Independently owned Exact Built-In Law Authority | Initial Version | Decision | Coverage   |
|--------------------------------------------------|-----------------|----------|------------|
| XSD 1.1 `date` canonical lexical Text            | `Initial`       | ADMIT    | Restricted |
| XSD 1.1 `time` canonical lexical Text            | `Initial`       | ADMIT    | Restricted |
| XSD 1.1 `dateTime` canonical lexical Text        | `Initial`       | ADMIT    | Restricted |
| GTIN 14-digit textual representative             | `Initial`       | ADMIT    | Restricted |
| IBAN electronic textual representative           | `Initial`       | ADMIT    | Restricted |
| ECMA-427 core PURL syntax representative         | `Initial`       | ADMIT    | Restricted |
| RFC 5870 WGS-84 core `geo:` URI                  | `Initial`       | ADMIT    | Restricted |
| RFC 7468 single textual-encoding instance        | `Initial`       | ADMIT    | Restricted |
| Avro 1.12.0 Parsing Canonical Form Text          | `Initial`       | ADMIT    | Restricted |

### 8.13.1. XSD 1.1 `date` Canonical Lexical Text — `Initial`

The immutable source is
[W3C XML Schema 1.1 Part 2, 5 April 2012 Recommendation](https://www.w3.org/TR/2012/REC-xmlschema11-2-20120405/).
This Law uses `dateLexicalRep`, `dateLexicalMap` and `dateCanonicalMap`. **`E_L`:** XSD date Value Identity,
not mere overlap of date intervals. It distinguishes an absent timezone from a present one and retains an
exact nonzero offset. **`C_L`:** the named Canonical Mapping, which writes `Z` for zero offset but otherwise
does not convert the date to a UTC day. Thus `2026-10-10+00:00 → 2026-10-10Z`. **Coverage:** Restricted. The
XSD date grammar and calendar rules apply; invalid offsets or year spellings refuse.

### 8.13.2. XSD 1.1 `time` Canonical Lexical Text — `Initial`

The exact source is the same frozen XSD Recommendation's `timeLexicalRep`, `timeLexicalMap`, and `timeCanonicalMap`. **
`E_L`:** same mapped `time` Value Identity, preserving absent versus present timezone and differing offsets even when
UTC interpretation would coincide. **`C_L`:** `timeCanonicalMap` output, including exact fractional-second normalization
and legal `24:00:00` interpretation; `12:00:00.5000+00:00 → 12:00:00.5Z`. **Coverage:** Restricted to legal `time`
lexical forms and values. The Law does not bind an ambient date, timezone database or leap-second history to decide
equality.

### 8.13.3. XSD 1.1 `dateTime` Canonical Lexical Text — `Initial`

The exact source is that Recommendation's `dateTimeLexicalRep`, `dateTimeLexicalMap` and
`dateTimeCanonicalMap`. **`E_L`:** mapped XSD dateTime Value Identity, not just same UTC instant. A present
offset remains identity-significant, and an absent offset stays absent. **`C_L`:** the exact Canonical
Mapping. For example, `2026-10-10T24:00:00Z → 2026-10-11T00:00:00Z`; fractional seconds and zero offset follow
the same frozen mapping. **Coverage:** Restricted to legal XSD lexical and value forms. Invalid civil time,
timezone and year forms refuse. No general `Instant` conversion belongs to this Law.

**XSD implementation caution (all three Laws):** use XSD 1.1 mapping, not RFC 3339 or JVM timezone defaults.
XSD Schema-level whitespace collapse is not implicit. Test absence of timezone separately from explicit `Z`;
other offsets remain identity-significant. Validate date boundaries and `24:00:00`. XSD's negative-year and
year-zero rules also need direct regression tests. Verify idempotence against the frozen mapping.

### 8.13.4. GTIN 14-Digit Text — `Initial`

The fixed domain is the [GS1 Digital Link](https://ref.gs1.org/standards/digital-link/) 14-digit GTIN
presentation. Operand Text has exactly 8, 12, 13 or 14 ASCII digits. **`E_L`:** equality after left-padding
with zero to 14 digits. The indicator and check-digit positions are retained. **`C_L`:** that exact 14-digit
Text; e.g. `95200002 → 00000095200002`. **Coverage:** Restricted to the digit repertoire and supported widths.
This Law neither repairs check digits nor establishes issued-code status. Country allocation and business
identity have other owners.

**Implementation caution:** do not use the Decimal Integer representative, which removes significant leading zeros;
preserve the 14-digit result as Text and validate original digit count before padding.

### 8.13.5. IBAN Electronic Text — `Initial`

The subject is the electronic versus paper IBAN spelling governed by
[ISO 13616-1](https://www.iso.org/standard/81089.html) and the
[SWIFT IBAN Registry](https://www.swift.com/standards/data-standards/iban-international-bank-account-number).
Operand is electronic ASCII IBAN Text or its exact printed grouping: groups of four separated by one U+0020
SPACE, with a shorter final group allowed. **`E_L`:** identical electronic strings after removing *only*
correctly placed grouping spaces. Letter case and account characters stay exact. **`C_L`:** unspaced
electronic Text, for example `DE89 3704 0044 0532 0130 00 → DE89370400440532013000`. **Coverage:** Restricted.
Bad group boundaries or repeated spaces refuse; tabs and other Unicode whitespace also refuse.

**Implementation caution:** grouping normalization must not supply an ambient country BBAN registry or uppercase account
characters; country-specific BBAN validity and MOD-97 check remain separately owned. Do not accept arbitrary printed
whitespace as a recoverable spelling.

### 8.13.6. ECMA-427 Core PURL Syntax Text — `Initial`

The fixed source
is [ECMA-427, First Edition, December 2025](https://ecma-international.org/publications-and-standards/standards/ecma-427/),
restricted to **common core PURL syntax** without package-type-dependent identity. The operand is a legal absolute
`pkg:` URL Text whose type, optional namespace, name, version, qualifiers and subpath satisfy the exact ECMA core rules.
**`E_L`:** equality after applying only core-authorized case, insignificant slash, UTF-8 percent-encoding and
qualifier-order equivalences. Package name, namespace, version, qualifier values and subpath retain all distinctions not
explicitly erased by core rules. **`C_L`:** lowercase `pkg:` and type, remove only core-insignificant namespace slashes,
generate exact component-specific UTF-8 Percent-Encoding with uppercase hex digits, and emit distinct qualifiers ordered
by their ASCII lowercase keys with no implicit rewriting of values. Other separators and subpath boundaries remain as
prescribed by the ECMA core canonical form. **Coverage:** Restricted: invalid type, malformed UTF-8/percent encoding,
empty or duplicate qualifier keys, and invalid component structure refuse.

**Implementation caution:** this is neither npm nor Maven package identity; do not apply type-specific
namespace/name/version folding or download registry data. Check percent-triplet decoding/reencoding and qualifier
collisions under lowercase-key comparison, then reapply the law to prove fixed-point stability.

### 8.13.7. RFC 5870 WGS-84 Core `geo:` URI — `Initial`

The fixed source is [RFC 5870 §§3.3–3.4](https://www.rfc-editor.org/rfc/rfc5870.html). Operand is a legal core
WGS-84 `geo:` URI with finite exact decimal coordinates. Latitude lies in `[-90,90]` and longitude in
`[-180,180]`. Exact decimal altitude is optional, as is nonnegative uncertainty `u`; default `crs=wgs84` may
be explicit. Non-default CRS and extension parameters are outside V1. **`E_L`:** exact coordinate equality
under the RFC's exceptions. Longitudes +180 and -180 are equivalent; at either pole longitude is irrelevant.
Explicit default CRS and its omission compare equal. Altitude and `u` retain their presence as well as exact
value, so absent differs from zero. **`C_L`:** minimal exact decimal spelling, removing redundant sign and
leading/fractional zeros. Write longitude `0` at poles and `180` at the antimeridian. Omit default CRS,
retaining altitude and `u`. Thus `geo:90,-22.43;crs=WGS84 → geo:90,0`. **Coverage:** Restricted; malformed
grammar or out-of-range values refuse.

**Implementation caution:** use exact decimal comparison rather than binary floating-point approximation; do not equate
missing altitude or uncertainty with zero. No geolocation lookup, proximity tolerance, CRS registry or coordinate
transformation is authorized.

### 8.13.8. RFC 7468 Single Textual-Encoding Instance — `Initial`

The fixed source is [RFC 7468](https://www.rfc-editor.org/rfc/rfc7468.html) under a deliberately restricted
profile. **Exact Operand:** one BEGIN/END enclosure with legal Label syntax and exactly matching
case-sensitive Labels. Base64 must have the RFC 4648 alphabet, valid padding and zero unused bits. Every
enclosure and body line uses the admitted LF or CRLF layout; each Base64 line holds 64 characters except
possibly the last. A final line ending may be absent. Extra spaces and nonalphabet characters are forbidden.
No broader permissive-parser rules are imported. **`E_L`:** equality of Labels and ordered decoded octets,
ignoring only the admitted line endings and optional final newline. **`C_L`:** preserve the Label and encoded
octets. Emit strict canonical Base64 at 64 characters per line with LF throughout and exactly one final LF.
**Coverage:** Restricted. Mismatched Labels and multiple instances refuse; unsupported wrapping or corrupt
Base64 also refuses.

**Implementation caution:** Base64 pad-bit checking is mandatory, even when a decoder accepts the payload. Enclosure
Canonicalization does not parse DER, validate certificates or keys, merge multiple PEM blocks, or change signed bytes. A
strict generator form and a broader parser latitude must not be conflated.

### 8.13.9. Avro 1.12.0 Parsing Canonical Form Text — `Initial`

The exact source is
[Apache Avro Specification 1.12.0, Parsing Canonical Form](https://avro.apache.org/docs/1.12.0/specification/#parsing-canonical-form-for-schemas).
Operand and representative are Text of one legal Avro Schema under the pinned grammar. **`E_L`:** exact
equality of the two Parsing Canonical Form Texts. **`C_L`:** apply the seven normative steps in order:
`PRIMITIVES → FULLNAMES → STRIP → ORDER → STRINGS → INTEGERS → WHITESPACE`. This includes the specification's
name and namespace resolution and exact spelling rules. **Coverage:** Restricted to valid finite schemas.
Illegal definitions or unresolved named references refuse. The resulting Text must be a legal schema and
stable under reapplication.

**Implementation caution:** field order is significant and must not be sorted. Named reference resolution must be
finite, explicit and independent of JVM object identity; do not use unbounded recursion for deeply nested inputs.
Removed `doc`, `default` and other non-parsing properties can remain relevant to evolution or application semantics,
which this Law does not claim. Fingerprints may index results but hash equality cannot replace exact canonical Text
equality.

### 8.13.10. Qualification Evidence and Explicit Membership

These nine Laws have exact representative algorithms from the cited standards or expressly narrowed profiles.
Each `E_L` compares either the selected representative or the fixed datatype identity. Every successful `C_L`
remains a legal fixed point. Restricted Coverage reflects a grammar/value-profile failure, never resource
exhaustion. PURL, RFC 7468 and Avro can expand intermediate data; their V1 realizations need bounded work over
legal finite inputs. Budget/Capacity retain exhaustion ownership.

This admission freezes the cited standard editions and their specific profiles. Realization verification must
address XSD timezone boundaries and GS1 digit widths. It must also test IBAN spacing and PURL escaping.
Geographic pole/date-line comparisons, RFC 7468 framing and Avro named-reference traversal have distinct
regression obligations. These witnesses support qualification but do not establish Kotlin/JVM backend
conformance.

**Membership decision (2026-10-10): ADMIT the nine Authorities in the table with their separate `Initial` Versions.**
Kubernetes Quantity is deliberately **not** part of this decision; its remaining pre-admission obligations and `DEFER`
status appear in Section 8.2.

---

# 9. API and Compiler Design Follow-On

The **62 admitted Authorities** in Sections 8.4–8.13 provide the exact meanings for downstream
API and compiler design. The two deferred subjects have no Catalog membership; three rejected
case-only subjects may instead reuse the existing ASCII Lowercase Authority (Section 8.3).
The working API source is `docs/design/api/canonicalization-authoring-api-design.md`.

That Design owns public selection syntax and exact Authority/Version projection. It also owns CLI guidance for
supported source declarations and version choices. Migration and rollback behavior must preserve legally
selected Versions; a CLI recommendation cannot supply a missing Law selection or rewrite Version History.
Exact Kotlin API spelling remains a downstream design choice.

Compiler lookup, physical representation, fusion, and reuse belong to Compiler Design and
Verification. They may share work but cannot mint a new Authority or change its observable
versioned meaning. API aliases project an existing Authority rather than creating members.

---

# 10. Qualification Evidence and Verification Follow-On

Section 8 contains candidate-specific pre-admission evidence; Section 2.7 explains the evidence approach.
Unicode requires version-pinned conformance data. Numeric Text and protocol parsers need independent
differential tests. Registries and structured schemas require fixed-source and finite-work verification. These
checks do not change the admitted semantic meaning.

Post-admission Verification checks the supported realization independently. It covers semantic properties and
malformed inputs, then tests adversarial resource behavior and compiler preservation. Recomputed results must
remain consistent under reuse. A realization can fail verification without revising the admitted Law. The CLI
may recommend only supported and separately verified selections; caching and performance remain implementation
matters.

**References:** ADR-0076 Sections 5–7; ADR-0066 Sections 7–8, 10; detailed evidence and
outstanding cases in Sections 8.2 and 8.4–8.13.

---

# 11. Working Sequence

The initial 75-candidate review is complete, and the separately reviewed CRLF Law is admitted.
Across 76 reviewed subjects there are 62 formal admissions, twelve independent-Authority
rejections and two deferrals (Section 8). New candidates use ADR-0076's qualification rule. This Design
records their evidence and disposition, adding an approved Version specification only if admitted. The
remaining BCP 47 Registry Snapshot and Kubernetes Quantity questions stay in Section 8.2, not as runtime
fallbacks.

A change to the Catalog membership *law* or the qualification law requires new ADR
consideration. Ordinary admission of another Authority under ADR-0076 does not. A
candidate-specific semantic decision or exact Law Version belongs to its owning Authority
and this Design, not an automatic revision of ADR-0076. API projection and backend
verification follow admission rather than supplying its missing meaning.