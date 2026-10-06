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

---

# 5. Current Candidate Inventory

The current working inventory is:

| Review group          | Candidate semantic label                 | Working status |
|-----------------------|------------------------------------------|----------------|
| General               | `text.unicode.nfc`                       | Candidate      |
| General               | `text.unicode.nfd`                       | Candidate      |
| General               | `text.unicode.nfkc`                      | Candidate      |
| General               | `text.unicode.nfkd`                      | Candidate      |
| General               | `text.unicode.nfc-casefold`              | Candidate      |
| General               | `text.unicode.nfkc-casefold`             | Candidate      |
| General               | `text.ascii.casefold`                    | Candidate      |
| General               | `text.line-ending.lf`                    | Candidate      |
| General               | `text.unicode.whitespace-trim`           | Candidate      |
| General               | `number.decimal.numeric-value`           | Candidate      |
| General               | `number.binary32.canonical-nan`          | Candidate      |
| General               | `number.binary64.canonical-nan`          | Candidate      |
| Protocol / Identifier | `network.ipv6.rfc5952`                   | Candidate      |
| Protocol / Identifier | `identifier.bcp47.rfc5646`               | Candidate      |
| Protocol / Identifier | `identifier.uuid.rfc9562-lowercase-text` | Candidate      |

No row in this table is a Catalog admission decision.

The first detailed semantic review batch remains NFC, NFD, NFKC, NFKD, and NFC Case Fold. NFKC Case Fold remains in the
working inventory and must likewise be reviewed as one complete exact law rather than reconstructed from arbitrary
implementation operations.

---

# 6. General Candidate Working Notes

The current detailed V1 review tranche is:

- `text.unicode.nfc` — Unicode NFC
- `text.unicode.nfd` — Unicode NFD
- `text.unicode.nfkc` — Unicode NFKC
- `text.unicode.nfkd` — Unicode NFKD
- `text.unicode.nfc-casefold` — Unicode NFC Case Fold
- `text.ascii.casefold` — ASCII Case Fold
- `text.line-ending.lf` — LF Line Ending
- `text.unicode.whitespace-trim` — Unicode Boundary Whitespace Trim
- `number.decimal.numeric-value` — Decimal Numeric Value
- `number.binary32.canonical-nan` — Binary32 Canonical NaN
- `number.binary64.canonical-nan` — Binary64 Canonical NaN

The laws in this section are grouped together for V1 review because they address general presentation or value
normalization. Presence here does not mean ratification, and each law must pass ADR-0076 Section 5. Each candidate is
reviewed as
one exact built-in law rather than as permission for users to sequence implementation operations.

Text candidates consume the `Text` presentation already established by ADR-0064, which is a sequence of Unicode scalar
values rather than arbitrary JVM UTF-16 code units. Host material outside that legal Input presentation never enters
Canonicalization.

The dotted spellings below are candidate review labels.

## 6.1. Unicode NFC

**Candidate semantic label:** `text.unicode.nfc`

The presentation domain is Unicode text admitted by the selected Input surface.

The candidate selects Unicode Normalization Form C as the representative for canonical equivalence. Compatibility
distinctions remain observable.

Ratification must close the exact Unicode Basis dependency rather than inherit host Unicode tables. Unicode
normalization stability may allow part of the profile to remain stable across later versions, while unassigned code
points still require an explicit evolution policy. That distinction must be settled before this candidate becomes
ratified.

Canonical bytes are not part of this candidate law.

## 6.2. Unicode NFD

**Candidate semantic label:** `text.unicode.nfd`

The candidate uses the same canonical-equivalence domain as NFC and selects Unicode Normalization Form D as the
decomposed representative.

Ratification must close the same Basis and unassigned-code-point questions as NFC and must state the exact V1 semantic
use for which NFD is published.

## 6.3. Unicode NFKC

**Candidate semantic label:** `text.unicode.nfkc`

The candidate applies Unicode compatibility decomposition followed by canonical composition. Selecting it therefore
declares compatibility distinctions irrelevant in addition to canonical distinctions.

NFKC is not an implementation substitute for NFC. Ratification must close its exact Unicode Basis, evolution behavior,
and intended presentation domain before this stronger equivalence becomes a base V1 law.

## 6.4. Unicode NFKD

**Candidate semantic label:** `text.unicode.nfkd`

The candidate erases the same compatibility distinctions as NFKC and selects the compatibility-decomposed
representation.

Its law is semantically clear only after the same Basis and evolution questions are closed. Ratification must also state
the exact V1 semantic use for which this decomposed compatibility representative is published.

## 6.5. Unicode NFC Case Fold

**Candidate semantic label:** `text.unicode.nfc-casefold`

This candidate is intended to collapse Unicode canonical-equivalent and default-caseless distinctions under one
independently specified exact law. It is not defined merely by sequencing the existing NFC and case-fold Catalog
entries,
and it must not be described as a Unicode-defined profile unless ratification identifies an exact normative Unicode
profile that owns the same relation.

Ratification must close the independent equivalence relation, exact representative definition, case-folding profile,
Unicode Basis, and unassigned-code-point behavior. Locale-sensitive lowercasing is not part of the law and cannot
substitute for the ratified profile.

## 6.6. Unicode NFKC Case Fold

**Candidate semantic label:** `text.unicode.nfkc-casefold`

This candidate targets Unicode `NFKC_Casefold` semantics for an identifier-like domain in which compatibility and
caseless distinctions are intentionally erased.

Ratification must use the exact Unicode profile rather than an arbitrary sequence of host normalization and lowercasing
calls. Its Basis, treatment of default-ignorable material, and evolution behavior remain part of the qualification
review.

## 6.7. ASCII Case Fold

**Candidate semantic label:** `text.ascii.casefold`

The presentation domain is text for which ASCII letter case is the only distinction this law may erase. `A` through `Z`
map to their lowercase ASCII representatives; every other code point is preserved.

The law has no locale behavior and requires no external semantic Basis.

## 6.8. LF Line Ending

**Candidate semantic label:** `text.line-ending.lf`

The law treats the supported line-ending spellings as equivalent and selects LF as their representative. CRLF and
standalone CR therefore converge to LF without changing an existing LF.

Other Unicode line separators and other whitespace are preserved. The transformation does not expand the source, trim
content, or collapse blank lines.

## 6.9. Unicode Boundary Whitespace Trim

**Candidate semantic label:** `text.unicode.whitespace-trim`

The law erases leading and trailing code points with the Unicode `White_Space` property under the selected Unicode
Basis. Interior whitespace remains unchanged.

The Unicode property set, not Java `trim()` or `strip()`, defines the law. Because property membership can be
version-sensitive, the Unicode Basis is meaning-determining. The law does not collapse internal whitespace or perform
line-ending normalization.

## 6.10. Decimal Numeric Value

**Candidate semantic label:** `number.decimal.numeric-value`

The presentation domain is a finite base-10 decimal represented by an integer coefficient and a decimal scale. Scale
differences are irrelevant when they denote the same exact decimal number.

For non-zero values, the representative removes every trailing base-10 zero from the coefficient and adjusts the scale
without changing the exact value. Zero has one representative with coefficient zero and scale zero.

```text
2.00
    → the same representative as 2

600.0
    → coefficient 6 with scale -2

0.00
    → coefficient 0 with scale 0
```

The law performs no rounding and does not parse floating-point or textual material into Decimal. It operates only on the
legal finite Decimal presentation established by Input.

This profile is distinct from monetary scale or market tick rules, where scale or step size may carry independent
business meaning.

## 6.11. Binary32 Canonical NaN

**Candidate semantic label:** `number.binary32.canonical-nan`

The presentation domain is IEEE 754 binary32 when raw NaN representation remains observable at the Input boundary. The
candidate maps every NaN bit pattern to one quiet-NaN representative while preserving every non-NaN bit pattern,
including signed zero.

The current candidate representative is `0x7fc00000`. Ratification must confirm that this exact bit-level observation
survives every supported Input and backend path before the bit pattern becomes normative law material.

## 6.12. Binary64 Canonical NaN

**Candidate semantic label:** `number.binary64.canonical-nan`

The binary64 candidate follows the same rule as the binary32 profile. Every NaN representation maps to one quiet NaN,
while every non-NaN bit pattern remains unchanged.

The current candidate representative is `0x7ff8000000000000`. Ratification carries the same bit-observability
requirement as binary32.

# 7. Protocol and Identifier Candidate Working Notes

These candidates are grouped together for review because their current definitions directly name protocol or identifier
standards. This is a document review group. They are not ratified until the
Kontrakt profile closes every interpretation and semantic dependency that can change the outcome.

## 7.1. IPv6 RFC 5952 Text

**Candidate semantic label:** `network.ipv6.rfc5952`

The candidate domain is legal textual IPv6 presentation without an external zone identifier. Alternate legal spellings
are equivalent when they denote the same IPv6 address, and RFC 5952 supplies the target text form.

Ratification must keep Input legality separate from the candidate's canonicalizable domain. Material that is illegal
under the selected Input presentation never reaches this law. If the selected Input is broader legal Text, text that is
not a legal IPv6 presentation lies outside this candidate's canonicalizable domain and is a Canonicalization refusal.
The exact grammar interpretation must therefore be fixed by the profile rather than inherited from a host parser. Zone
identifiers remain outside this profile, and the law performs no DNS lookup.

## 7.2. BCP 47 Language Tag

**Candidate semantic label:** `identifier.bcp47.rfc5646`

The candidate domain is a well-formed BCP 47 language tag under the selected Kontrakt profile. RFC 5646 supplies
canonicalization rules, while registry data becomes meaning-determining where the representative consumes it.

Ratification must close the exact registry Basis and its evolution consequences. The law cannot read a mutable current
registry as authority, and unsupported extension semantics cannot receive an implementation-defined representative.

## 7.3. UUID Lowercase Text

**Candidate semantic label:** `identifier.uuid.rfc9562-lowercase-text`

The candidate domain is the standard textual UUID form accepted by the profile. Hexadecimal letter case is declared
irrelevant and lowercase text is the representative.

Ratification must confirm that the accepted textual grammar, equivalence relation, and representative are exact. It
changes neither UUID bits nor version or variant meaning. A structured UUID value that no longer contains textual case
does not require this profile.

---

# 8. Deferred Profile Working Notes

The following profiles are plausible Canonicalization laws but are not part of the current admission-ready set. Moving
one into the built-in Catalog requires explicit qualification and a Catalog admission decision under ADR-0076. A new ADR
is required only if that work changes the Catalog architecture or qualification law.

## 8.1. Unicode Stabilized Normalization

Unicode Stabilized Strings reject code points that are unassigned in the selected Unicode version so that a successful
normalization result remains stable across version evolution.

Stabilized NFC and NFKC remain deferred until their exact refusal relation and semantic-Basis requirements are closed at
the Catalog level. ADR-0066 owns how those closed requirements are represented and established downstream.

## 8.2. Unicode Whitespace Collapse

A whitespace-collapse law can map each maximal Unicode whitespace run to one fixed representative. It remains deferred
because the boundary-whitespace policy must be selected before a public profile exists.

## 8.3. Signed-Zero Collapse

A binary floating-point law may declare positive and negative zero equivalent, but IEEE 754 behavior can later observe
the sign. V1 therefore defers this law until those semantic consequences are reviewed explicitly.

## 8.4. RFC 3986 URI Syntax Profile

RFC 3986 defines syntax-level normalization, but URI equivalence depends on purpose and can become scheme-specific. V1
therefore publishes no generic `UriCanonicalization`.

A later law must identify the exact syntax-level or scheme-specific equivalence it owns.

## 8.5. IDNA and PRECIS Profiles

IDNA and PRECIS combine representative formation with legality rules. A future Kontrakt profile must split those
responsibilities according to Kontrakt authority rather than import an external pipeline as one opaque law.
Representative formation may belong to Canonicalization while Input or Admission owns the corresponding legality rule.

## 8.6. Instant-Preserving Temporal Profiles

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