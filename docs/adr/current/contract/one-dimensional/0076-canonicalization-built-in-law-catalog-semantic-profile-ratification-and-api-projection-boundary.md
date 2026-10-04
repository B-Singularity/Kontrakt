# ADR-0076: Canonicalization Built-In Law Catalog

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
coordinate must resolve to one closed built-in Canonicalization semantic target.

The remaining question is which built-in semantic laws Kontrakt will admit and what exact meaning each target carries.
That decision cannot be left to implementation because a built-in law fixes the equivalence relation, the successful
representative, and the semantic conditions under which that representative is valid.

The word canonicalization is broader outside Kontrakt than the authority defined by ADR-0066. Catalog membership
therefore follows Kontrakt's semantic boundary rather than terminology used elsewhere.

The catalog must cover common representation problems without turning Canonicalization into an executable extension
point. It must also leave a path for specialized domains whose representative depends on an explicit semantic basis.
Other transformations remain separate unless they satisfy the Canonicalization law itself.

A single Input coordinate may also need a meaning that combines more than one familiar canonicalization concern.
Kontrakt does not treat that need as permission to expose an arbitrary normalization pipeline. When a combination is
admitted, it is represented as one complete Composite Catalog Law semantic subject whose equivalence and representative
are qualified as one exact profile.

This ADR defines that catalog boundary, the initial V1 catalog, and the rules for admitting later profiles.

---

# 2. Problem

Kontrakt must avoid both an under-specified catalog and an over-broad one.

If V1 defines only the authoring mechanism, a selected nominal law has no complete exact semantic target.

The opposite failure occurs when a broad law name leaves several legal representatives or interpretations open. A
built-in law cannot delegate those choices to a host library, parser, iteration order, locale, or backend.

The same discipline applies to external standards. A standard can be precise enough to guide a Kontrakt law while still
leaving versioned data, optional behavior, or purpose-specific interpretation unresolved. Kontrakt must close every
distinction that can change its own representative or owned refusal.

Deterministic byte production is a separate concern unless exact bytes are themselves the representative owned by the
selected Contract law. This ADR does not create a user-facing canonical-byte facility merely because other ecosystems
use the word canonicalization for signing or serialization.

This ADR therefore decides the qualification boundary for built-in laws and records the current V1 candidates.
`Candidate` is a working state, not an accepted Catalog state. During this ADR's Proposed lifecycle, each initial V1
candidate must be reviewed against Section 5 before the ADR can become Accepted. V1 may admit curated Composite Catalog
Laws only after the complete combination has passed the same semantic qualification gate. Arbitrary user composition
remains outside this ADR. A later built-in addition requires an explicit semantic Catalog decision rather than an
implementation update or mutable registry entry.

# 3. Decision Drivers

Determinism is the first qualification requirement. The same law-owned determinants and the same legal Input must
produce the same Canonicalization-owned outcome under every legal realization.

That rule requires complete determinant closure. Meaning cannot depend on ambient locale, current provider state, host
iteration order, scheduling, cache history, or another undeclared source. External semantic material that can change the
result must be explicit under the law that owns that meaning.

The catalog must also preserve the authority boundary established by ADR-0066. A built-in law establishes one
representative under one exact equivalence relation; convenience, validation, ordering, serialization, or business
transformation do not become Canonicalization merely because they are useful.

A law must remain understandable without its implementation. API names and backend strategies may change while the law
remains the same. Performance work may exploit established semantic properties, but target cost or implementation
convenience cannot choose a different representative.

V1 should admit only laws whose hostile-input behavior can be reviewed and whose conformance can be checked
independently of one implementation. Specialized domains remain possible when their Basis and authority boundaries are
explicit.

Composition must preserve the same boundary. A Composite Catalog Law is not identified by an implementation pipeline, by
a tuple of component API names, or by the order in which helper routines happen to run. Its semantic identity belongs to
the independently ratified composite profile.

# 4. Decision

Kontrakt will maintain a **Canonicalization Built-In Law Catalog** as the logical boundary for the built-in
Canonicalization vocabulary defined by this ADR.

The Catalog is not one `CatalogEntry` record and it is not defined by one physical table. It is a logical boundary over
typed semantic families. The semantic families may be represented together or separately by the compiler, but physical
co-location, one row layout, one generated enum, or one lookup structure does not merge their meanings.

The current Catalog structure is:

```text
Canonicalization Built-In Law Catalog
    Built-In Membership
    Exact Built-In Law Definitions
    Exact Law-to-Law Semantic Relations              [vocabulary OPEN]
```

This structure fixes the decomposition rule, not the final contents of every family.

ADR-0066 owns the common semantic shape of Canonicalization. This ADR owns the exact semantics of built-in laws and the
Catalog-specific relations needed to make the built-in vocabulary closed.

The catalog does not own host-language names or implementation algorithms.

A candidate law is not ratified merely because it appears in this ADR draft. It must satisfy the qualification gate in
Section 5 and every still-open Catalog admission question that applies to it.

Source projection, implementation, test vectors, verification evidence, optimization knowledge, and physical lookup
remain distinct from law meaning. None of them may complete missing Contract meaning on the law's behalf.

# 5. Catalog Law Qualification

Catalog admission is stricter than showing that one normalization routine is useful. A V1 built-in law must close its
semantic meaning without borrowing missing meaning from implementation.

## 5.1. ADR-0066 Determinism Qualification

ADR-0066 owns the universal determinism law for Canonicalization. A built-in Catalog candidate is ratifiable only when
its
exact semantics supply enough closed material to prove that law without relying on ambient or physical state.

The candidate must therefore close every semantic determinant that can change its equivalence classes, representative,
or another law-owned observation. An external semantic source that can change the law must be explicit semantic
material.
Host locale, provider state, encounter order, hash layout, cache history, scheduling, physical catalog revision, and
other
realization artifacts cannot finish the law.

A referenced standard may leave choices that Kontrakt does not. Any option, tie-break, ordering rule, unknown-member
rule, revision choice, or other semantic branch that can change the legal observation must be closed before
ratification.
The exact representation of those choices must remain typed and law-specific rather than becoming an open property bag.

## 5.2. Exact Semantic Profile Closure

ADR-0066 requires every Canonicalization law to have one independently declared same-meaning relation `E_L` and one
representative selection `C_L` satisfying the common equivalence, representative, and idempotence laws. ADR-0076 does
not
redefine those meta-laws. It supplies the exact built-in semantics that satisfy them.

The current re-audit also requires every candidate to answer what exact already-established semantic operand the law
observes and which material actually determines its `E_L` and `C_L`.

The law must not create a second Input presentation ontology. Input owns the legal presentation meaning. An Exact
Built-In Law must instead identify the exact Input-owned semantic presentation over which its `E_L` and `C_L` are
meaningful. Java or Kotlin carrier identity, parser choice, source syntax, runtime object topology, or another host
classification cannot substitute for that semantic operand.

A built-in law may observe semantic distinctions beyond the common scalar core. Presence, explicit absence, finite
alternatives, ordering, multiplicity, duplicates, aggregate collision, semantic bounds, unassigned material, or another
domain-specific distinction enters the exact law only when that law actually observes it. V1 does not create one
universal optional-field record containing every such concern.

The law's negative semantics must be closed as exact outcome meaning rather than as a generic `error` field. A legal
Input may establish the representative or may reach a Canonicalization-owned negative condition when the exact law owns
one. Input illegality, an unentered judgment, HIR incompleteness, unavailable backing, Budget or Capacity stop, compiler
resource exhaustion, and implementation failure are not silently converted into that outcome vocabulary.

A separate mandatory `Representative Range` is not introduced merely because it can be derived from `C_L`. It becomes
independent semantic material only if a particular law owns an additional distinction that cannot be recovered from its
exact representative semantics.

A combined built-in law must provide the same complete exact meaning as any other law. Sequentially applying two
existing
laws does not itself create a third law. If order or intermediate semantic state changes the legal observation, an
admitted result is a distinct Composite Catalog Law unless a separately ratified profile establishes another exact
meaning.

Canonicalization selects a representative of already-declared meaning. A transformation that acquires or changes meaning
belongs to another authority.

## 5.3. Composition and Semantic-Relation Closure

Ratification must establish the semantic relations that a built-in law actually owns. Canonical material is not assumed
to remain canonical after concatenation, aggregation, slicing, partial update, or another domain operation merely
because
source fragments were canonical.

Where two laws have a useful exact semantic interaction, this ADR may ratify a typed law-to-law relation such as an
exact
composition, proven order independence, order sensitivity, incompatibility, or another relation that is itself
meaningful
for Catalog qualification. The final vocabulary remains `OPEN` in Section 6.2.1.

Composition qualification is stricter than showing equality of final values for one implementation. Any claimed
order-independence must preserve the complete Canonicalization-owned observation under the same legal determinants.
Representative value, successful or negative outcome, and Contract-owned attribution cannot vary because legal
realization machinery happened to execute approved concerns in another order.

The Catalog does not generate the Cartesian closure of all primitive laws. V1 publishes only curated composite meanings
that are independently complete and have passed whole-profile qualification. Pairwise evidence does not establish an
arbitrary N-ary composite when domain, refusal, Basis, or other interactions arise only in the whole combination.

## 5.4. Adversarial Realizability

ADR-0066 owns the common finite-work and authority boundaries of Canonicalization. Catalog ratification must
additionally
show, for each built-in candidate, that its hostile-input work shape is understood well enough for Kontrakt to publish
the
law without weakening its semantics under load.

Semantic bounds remain distinct from compiler resource limits. A bound belongs to the law only when crossing it changes
the law's legal domain, exact outcome, or representative obligation. CPU ceilings, temporary-memory caps, cancellation,
worker limits, or implementation scratch limits do not become Canonicalization meaning merely because the realization
needs protection.

A mitigation may stop work only through the authority that owns that stop. It cannot silently narrow `E_L`, substitute a
different `C_L`, change duplicate or collision semantics, truncate traversal, or alter the successful domain to make one
implementation cheaper.

## 5.5. Evolution Closure

A ratified built-in law must remain stable for the semantic observations its identity actually promises. Another
Kontrakt
release may replace the implementation, reorganize physical catalog material, or change a host-language projection
without assigning different Canonicalization meaning to the same exact law.

A change to equivalence, representative, law-specific negative condition, meaning-determining semantic material,
law-specific distinction, or another actual determinant is a semantic change when it can alter the law's meaning. Such a
change requires a distinct semantic profile; it cannot be hidden behind the prior law.

A meaning-determining Basis, registry, or standard revision cannot drift through the host environment. Version pinning
is
required only when the exact semantic material can change the law. A ratified stability guarantee may justify a narrower
dependency. Provider version, library version, standard label, and semantic Basis identity are not interchangeable
merely
because they often change together.

When material can acquire new meaning under a later semantic Basis, the exact law must define how currently unknown,
unassigned, or future material behaves wherever that distinction is observable. An implementation fallback cannot invent
the rule.

The Catalog must also avoid accidental promises. Stable law meaning does not make enumeration order, internal numeric
identifiers, generated evaluator shape, or other realization details part of the semantic contract.

# 6. Catalog Logical Content and Semantic Families

The Catalog is a logical boundary over typed semantic families. It is not a universal semantic record.

## 6.1. Catalog Logical Content and Exact Law Definition Boundary

The Catalog currently admits the following logical family candidates:

```text
Built-In Membership
Exact Built-In Law Definitions
Exact Law-to-Law Semantic Relations              [partially OPEN]
```

`Built-In Membership` answers which exact semantic law definitions belong to the built-in selectable population after
Catalog qualification has been satisfied. Membership is not law meaning merely because a compiler stores the two
together.

`Exact Built-In Law Definitions` contain the candidate-specific semantic meaning needed to close the built-in law under
ADR-0066. They do not absorb API naming, assurance evidence, compiler optimization permissions, realization algorithms,
or physical lookup coordinates.

Law-to-law relations are recorded only when an exact normative relation is actually required. This ADR does not create a
relation family merely because tooling would find one convenient.

### 6.1.1. Exact Law Common-Core Shape

**OPEN**

The HIR–Establishment re-audit and the closed Input model show that one Exact Built-In Law must be complete without
becoming a generic property bag. The following are therefore mandatory review questions for every candidate, while the
final common-core decomposition remains open:

| Semantic concern                      | Current boundary                                                                                                                                                     |
|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Semantic operand meaning              | Must identify the exact already-established Input-owned meaning over which the law operates; no second Input ontology                                                |
| Law-Defined Equivalence               | Concrete `E_L` required by ADR-0066                                                                                                                                  |
| Representative Law                    | Concrete `C_L` required by ADR-0066                                                                                                                                  |
| Meaning-Determinant Closure           | Every semantic input that can change `E_L`, `C_L`, or a law-specific negative condition must be explicit                                                             |
| Direct Meaning-Determining References | Preserved only when exact external or sibling semantic meaning is actually required                                                                                  |
| Law-Specific Typed Meaning            | Presence, absence, finite alternatives, ordering, multiplicity, duplicate/collision, semantic bounds, unassigned behavior, or another distinction only when observed |

A separate mandatory representative-range field is not required when it is merely derivable from `C_L`. A generic
`options`, `profile`, `flags`, or `conditionalClauses` property bag is also rejected as the default representation of
law-specific meaning. Exact semantic choices remain typed under the law that owns them.

### 6.1.2. Typed Law-Specific Meaning

**OPEN**

The common core must not force every law to carry irrelevant semantic categories. A scalar Unicode law need not own map
collision semantics. An aggregate law cannot omit collision, ordering, duplicate, element, or key semantics when those
distinctions are observable under the law.

The final V1 typed family vocabulary must therefore be derived from the candidate laws that survive qualification rather
than invented as one universal optional schema. A candidate is incomplete when a distinction it actually observes is
left to host equality, parser behavior, collection order, provider defaults, or implementation convention.

## 6.2. Composition Relations and Composite Catalog Laws

V1 may publish a curated Composite Catalog Law when one Input coordinate needs a complete meaning that combines more
than
one already-approved canonicalization concern. The Catalog-visible selection meaning remains one exact law. A composite
therefore does not weaken ADR-0066's rule that one selected coordinate resolves to one closed law.

A Composite Catalog Law must close its own exact semantic meaning under Section 5 and Section 6.1. Sequentially applying
component implementations does not substitute for the complete composite meaning.

The Catalog may record typed semantic relations between exact laws when such relations are needed for qualification or
for another explicitly owned semantic purpose. Those relations do not construct arbitrary new laws and do not grant
users
open composition authority.

A V1 composition review concentrates only on laws that can meaningfully interact over one coordinate. Proven
order-independent interaction may eliminate redundant ordered candidates. Order-sensitive interaction requires an exact
curated profile for each ordered meaning that Kontrakt chooses to publish. V1 does not generate every subset or
permutation merely because it is mechanically expressible.

### 6.2.1. Composition Relation Vocabulary

OPEN

### 6.2.2. V1 Curated Composite Admission Set

OPEN

### 6.2.3. N-Ary Composition Qualification

OPEN

# 7. Initial V1 Review Group

The current detailed V1 review tranche is:

- `text.unicode.nfc` — Unicode NFC
- `text.unicode.nfd` — Unicode NFD
- `text.unicode.nfkc` — Unicode NFKC
- `text.unicode.nfkd` — Unicode NFKD
- `text.ascii.casefold` — ASCII Case Fold
- `text.line-ending.lf` — LF Line Ending
- `text.unicode.whitespace-trim` — Unicode Boundary Whitespace Trim
- `number.decimal.numeric-value` — Decimal Numeric Value
- `number.binary32.canonical-nan` — Binary32 Canonical NaN
- `number.binary64.canonical-nan` — Binary64 Canonical NaN

Composite candidates are intentionally omitted from this overview and are reviewed under their own composition
obligations below.

The laws in this section are grouped together for V1 review because they address general presentation or value
normalization. Presence here does not mean ratification, and each law must pass Section 5. A candidate may be primitive
or composite. `text.unicode.nfc-casefold` and `text.unicode.nfkc-casefold` are reviewed as complete composite profiles
rather than as permission for users to sequence their component operations.

Text candidates consume the `Text` presentation already established by ADR-0064, which is a sequence of Unicode scalar
values rather than arbitrary JVM UTF-16 code units. Host material outside that legal Input presentation never enters
Canonicalization.

The dotted spellings below are candidate review labels.

## 7.1. Unicode NFC

**Candidate semantic label:** `text.unicode.nfc`

The presentation domain is Unicode text admitted by the selected Input surface.

The candidate selects Unicode Normalization Form C as the representative for canonical equivalence. Compatibility
distinctions remain observable.

Ratification must close the exact Unicode Basis dependency rather than inherit host Unicode tables. Unicode
normalization stability may allow part of the profile to remain stable across later versions, while unassigned code
points still require an explicit evolution policy. That distinction must be settled before this candidate becomes
ratified.

Canonical bytes are not part of this candidate law.

## 7.2. Unicode NFD

**Candidate semantic label:** `text.unicode.nfd`

The candidate uses the same canonical-equivalence domain as NFC and selects Unicode Normalization Form D as the
decomposed representative.

Ratification must close the same Basis and unassigned-code-point questions as NFC and must state the exact V1 semantic
use for which NFD is published.

## 7.3. Unicode NFKC

**Candidate semantic label:** `text.unicode.nfkc`

The candidate applies Unicode compatibility decomposition followed by canonical composition. Selecting it therefore
declares compatibility distinctions irrelevant in addition to canonical distinctions.

NFKC is not an implementation substitute for NFC. Ratification must close its exact Unicode Basis, evolution behavior,
and intended presentation domain before this stronger equivalence becomes a base V1 law.

## 7.4. Unicode NFKD

**Candidate semantic label:** `text.unicode.nfkd`

The candidate erases the same compatibility distinctions as NFKC and selects the compatibility-decomposed
representation.

Its law is semantically clear only after the same Basis and evolution questions are closed. Ratification must also state
the exact V1 semantic use for which this decomposed compatibility representative is published.

## 7.5. Unicode NFC Case Fold

**Candidate semantic label:** `text.unicode.nfc-casefold`

This candidate is a combined Kontrakt profile intended to collapse Unicode canonical-equivalent and default-caseless
distinctions under one independently specified law. It is not defined merely by composing the existing NFC and case-fold
Catalog entries, and it must not be described as a Unicode-defined profile unless ratification identifies an exact
normative Unicode profile that owns the same relation.

Ratification must close the independent equivalence relation, exact transformation order, fixed-point behavior,
case-folding profile, Unicode Basis, unassigned-code-point behavior, and composition property. Locale-sensitive
lowercasing is not part of the law and cannot substitute for the ratified profile.

## 7.6. Unicode NFKC Case Fold

**Candidate semantic label:** `text.unicode.nfkc-casefold`

This candidate targets Unicode `NFKC_Casefold` semantics for an identifier-like domain in which compatibility and
caseless distinctions are intentionally erased.

Ratification must use the exact Unicode profile rather than an arbitrary sequence of host normalization and lowercasing
calls. Its Basis, treatment of default-ignorable material, evolution behavior, and composition properties remain part of
the qualification review.

## 7.7. ASCII Case Fold

**Candidate semantic label:** `text.ascii.casefold`

The presentation domain is text for which ASCII letter case is the only distinction this law may erase. `A` through `Z`
map to their lowercase ASCII representatives; every other code point is preserved.

The law has no locale behavior and requires no external semantic Basis.

## 7.8. LF Line Ending

**Candidate semantic label:** `text.line-ending.lf`

The law treats the supported line-ending spellings as equivalent and selects LF as their representative. CRLF and
standalone CR therefore converge to LF without changing an existing LF.

Other Unicode line separators and other whitespace are preserved. The transformation does not expand the source, trim
content, or collapse blank lines. The law is composition-sensitive at fragment boundaries, so qualification must state
the exact Section 5.3 semantic
composition relation.

## 7.9. Unicode Boundary Whitespace Trim

**Candidate semantic label:** `text.unicode.whitespace-trim`

The law erases leading and trailing code points with the Unicode `White_Space` property under the selected Unicode
Basis. Interior whitespace remains unchanged.

The Unicode property set, not Java `trim()` or `strip()`, defines the law. Because property membership can be
version-sensitive, the Unicode Basis is meaning-determining. The law does not collapse internal whitespace or perform
line-ending normalization. Its boundary operation is not assumed to compose over independently canonicalized fragments;
ratification must record the exact Section 5.3 property.

## 7.10. Decimal Numeric Value

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

## 7.11. Binary32 Canonical NaN

**Candidate semantic label:** `number.binary32.canonical-nan`

The presentation domain is IEEE 754 binary32 when raw NaN representation remains observable at the Input boundary. The
candidate maps every NaN bit pattern to one quiet-NaN representative while preserving every non-NaN bit pattern,
including signed zero.

The current candidate representative is `0x7fc00000`. Ratification must confirm that this exact bit-level observation
survives every supported Input and backend path before the bit pattern becomes normative law material.

## 7.12. Binary64 Canonical NaN

**Candidate semantic label:** `number.binary64.canonical-nan`

The binary64 candidate follows the same rule as the binary32 profile. Every NaN representation maps to one quiet NaN,
while every non-NaN bit pattern remains unchanged.

The current candidate representative is `0x7ff8000000000000`. Ratification carries the same bit-observability
requirement as binary32.

## 7.13. V1 Composition Interaction Matrix

OPEN

## 7.14. V1 Additional Curated Composite Candidates

OPEN

---

# 8. V1 Protocol and Identifier Candidate Review Group

These candidates are grouped together for review because their current definitions directly name protocol or identifier
standards. This is a document review group. They are not ratified until the
Kontrakt profile closes every interpretation and semantic dependency that can change the outcome.

## 8.1. IPv6 RFC 5952 Text

**Candidate semantic label:** `network.ipv6.rfc5952`

The candidate domain is legal textual IPv6 presentation without an external zone identifier. Alternate legal spellings
are equivalent when they denote the same IPv6 address, and RFC 5952 supplies the target text form.

Ratification must keep Input legality separate from the candidate's canonicalizable domain. Material that is illegal
under the selected Input presentation never reaches this law. If the selected Input is broader legal Text, text that is
not a legal IPv6 presentation lies outside this candidate's canonicalizable domain and is a Canonicalization refusal.
The exact grammar interpretation must therefore be fixed by the profile rather than inherited from a host parser. Zone
identifiers remain outside this profile, and the law performs no DNS lookup.

## 8.2. BCP 47 Language Tag

**Candidate semantic label:** `identifier.bcp47.rfc5646`

The candidate domain is a well-formed BCP 47 language tag under the selected Kontrakt profile. RFC 5646 supplies
canonicalization rules, while registry data becomes meaning-determining where the representative consumes it.

Ratification must close the exact registry Basis and its evolution consequences. The law cannot read a mutable current
registry as authority, and unsupported extension semantics cannot receive an implementation-defined representative.

## 8.3. UUID Lowercase Text

**Candidate semantic label:** `identifier.uuid.rfc9562-lowercase-text`

The candidate domain is the standard textual UUID form accepted by the profile. Hexadecimal letter case is declared
irrelevant and lowercase text is the representative.

Ratification must confirm that the accepted textual grammar, equivalence relation, and representative are exact. It
changes neither UUID bits nor version or variant meaning. A structured UUID value that no longer contains textual case
does not require this profile.

---

# 9. Deferred General Profiles

The following profiles are plausible Canonicalization laws but are not part of the initial V1 catalog. After this ADR is
Accepted, adding one as a ratified Catalog Law requires a later catalog ADR that preserves the identities and legal
observations already established here.

## 9.1. Unicode Stabilized Normalization

Unicode Stabilized Strings reject code points that are unassigned in the selected Unicode version so that a successful
normalization result remains stable across version evolution.

Stabilized NFC and NFKC remain deferred until their exact refusal relation and semantic-Basis requirements are closed at
the Catalog level. ADR-0066 owns how those closed requirements are represented and established downstream.

## 9.2. Unicode Whitespace Collapse

A whitespace-collapse law can map each maximal Unicode whitespace run to one fixed representative. It remains deferred
because the boundary-whitespace policy must be selected before a public profile exists.

## 9.3. Signed-Zero Collapse

A binary floating-point law may declare positive and negative zero equivalent, but IEEE 754 behavior can later observe
the sign. V1 therefore defers this law until those semantic consequences are reviewed explicitly.

## 9.4. RFC 3986 URI Syntax Profile

RFC 3986 defines syntax-level normalization, but URI equivalence depends on purpose and can become scheme-specific. V1
therefore publishes no generic `UriCanonicalization`.

A later law must identify the exact syntax-level or scheme-specific equivalence it owns.

## 9.5. IDNA and PRECIS Profiles

IDNA and PRECIS combine representative formation with legality rules. A future Kontrakt profile must split those
responsibilities according to Kontrakt authority rather than import an external pipeline as one opaque law.
Representative formation may belong to Canonicalization while Input or Admission owns the corresponding legality rule.

## 9.6. Instant-Preserving Temporal Profiles

A temporal law may declare two offset date-time presentations equivalent when they denote the same instant and select a
UTC representative. Such a law is valid only when the original civil-time context is explicitly irrelevant.

V1 therefore publishes no generic `DateTimeCanonicalization`.

---

# 10. Canonical Bytes Are Outside This Catalog

This ADR does not create a Canonical-Byte Catalog class or a user-facing byte canonicalization facility.

Exact deterministic bytes belong to the authority that gives those bytes semantic significance. A signing protocol, wire
format, artifact identity scheme, or compiler persistence format may require one exact encoding without making that
encoding an inbound Canonicalization law.

A coordinate Catalog Law owns bytes only if those bytes are themselves the representative declared by that law.
Otherwise deterministic serialization remains with its protocol or implementation owner.

Compiler-internal deterministic encoding used for HID, caching, persistence, or artifact formation is therefore not
Canonicalization authority.

# 11. Generic Laws That V1 Will Not Publish

V1 will not publish a generic law when its name hides unresolved equivalence. The rule applies regardless of domain: the
exact same-meaning relation must be closed before a built-in profile exists.

The reason is semantic, not cosmetic. A generic email law would already be ambiguous because mailbox local-part and
domain case do not follow the same rule. URI and filesystem-path profiles have the same problem for different reasons. A
broad source name would hide those determinants rather than establish them.

---

# 12. No Identity or Preserve-Everything Laws

The catalog does not contain `ExactCanonicalization` or another profile whose only effect is to preserve every
Input-established representation distinction. Omission already expresses that Canonicalization does not apply.

Earlier preserve-everything candidates therefore do not become V1 laws. A particular already-canonical input may still
pass through a selected law without physical change; that is different from publishing universal identity as a
Canonicalization authority.

---

# 13. Ordering, Validation, and Refusal Are Not Automatically Canonicalization

Ordering alone does not establish a representative, so an IEEE total-order relation is not a Canonicalization law by
itself. Likewise, a reject-only rule such as `RejectNaN` or `RejectNonFinite` does not become Canonicalization merely
because it constrains the same domain. The owning 1D Contract must decide that legality.

Collection ordering follows the same distinction. If source order is Contract-visible and a selected law declares it
irrelevant, deterministic representative selection may be Canonicalization. If the semantic domain was already
unordered, sorting physical storage is compiler representation preparation.

The catalog never infers semantic order from JVM collection iteration.

---

# 14. Aggregate Law Boundary

A catalog law may apply to one coordinate whose presentation domain is a closed aggregate. The complete aggregate
profile remains one law bound to that coordinate; its children do not become independent 1D Contracts.

V1 does not expose arbitrary recursive law composition. A built-in aggregate profile may be added only after the
complete aggregate equivalence and representative are defined, preventing the catalog from becoming a normalization
programming language.

An aggregate law and a Composite Catalog Law are different concerns. Aggregate describes the presentation domain owned
by one coordinate. Composite describes one exact law whose meaning combines more than one canonicalization concern over
its admitted domain. Either may exist without the other, and neither creates child 1D Contracts.

---

# 15. Canonicalization Contract Integration Boundary

Canonicalization-specific HIR, Definition Candidate, Binding Candidate, Establishment, occurrence, Established Material,
and Established Semantic Protocol semantics are owned by ADR-0066 together with ADR-0071, ADR-0063, and the 1D master
checklist. This Catalog ADR does not define their final schema.

A Canonicalization Definition is not the same semantic subject as one built-in law. Actual Basis Binding, Applicability,
occurrence meaning, and Established Material are not Catalog membership.

# 16. External Semantic Basis

`external dependency` is not one semantic category. This ADR distinguishes external normative specification or profile,
meaning-determining semantic material, provider or implementation identity, and conformance or assurance evidence.

A standard title, provider name, library version, or platform version does not become semantic meaning merely because it
is convenient to record.

When a built-in law itself fixes exact external semantic material and changing that material can change its meaning,
that
material participates in the law's determinant closure. It is not replaced by ambient provider state merely because the
material originated outside Kontrakt.

The exact classification of external material is **OPEN per built-in law**. Unicode, registry-backed identifiers,
temporal data, scientific reference data, and other standards must not be forced into one universal external model
before their actual semantic dependency is established.

# 17. Current V1 Candidate Summary

The entries below are review candidates only. `Candidate` means that the semantic profile is still under qualification.
The dotted strings are candidate review labels, not ratified semantic IDs, package names, public API names, persistent
keys, dense handles, or exact reference encodings.

| Current review group  | Candidate semantic label                 | Current status |
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

The first detailed semantic review batch remains NFC, NFD, NFKC, NFKD, and NFC Case Fold. NFC Case Fold and NFKC Case
Fold
also exercise the curated Composite Catalog Law model and must be reviewed as complete laws rather than as user-visible
sequences of primitives.

Before this ADR can become Accepted, every entry retained in the initial V1 set must receive an explicit terminal
Catalog decision. That decision must not be inferred from implementation registration.

Deferred profile families in Section 9 remain outside this candidate table until their current blocking issues are
resolved.

# 18. Decisions Against Earlier Candidate Material

Earlier drafts used a broader exploratory list. This ADR narrows that material according to the current Canonicalization
law.

Earlier candidates are retained only when they establish a Canonicalization representative. Preserve-everything profiles
collapse into omission, ordering-only profiles remain ordering laws, and reject-only profiles remain with the authority
that owns domain legality. Collection and binary candidates stay deferred until their semantic equivalence is closed
independently of JVM representation.

The following names were previously kept as illustrative ADR-0066 Catalog exploration. They are transferred here so that
removing Catalog population from ADR-0066 does not erase review history. These spellings are not active API promises or
Ratified law identities; each survives only through a current semantic candidate or a later explicit disposition.

```text
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

---

# 19. Migration and Supersession

This ADR does not supersede the Canonicalization authority defined by ADR-0066. It supplies the built-in semantic
Catalog
work that ADR-0066 intentionally leaves to this document.

When this ADR is accepted, older candidate-catalog material in ADR-0048 migration history and earlier ADR-0066 revisions
becomes historical exploration rather than active V1 direction.

The active authoring relation remains:

```text
IDL selects one Canonicalization declaration
    ↓
declaration names selected Input coordinates
    ↓
each selected coordinate resolves to one exact admitted built-in semantic target
```

An unselected coordinate remains outside Canonicalization. The IDL does not select a built-in law directly, no
`ExactCanonicalization` filler is inserted, and the declaration contains no executable user canonicalizer. A selected
exact law may be primitive or composite; that distinction does not change the authoring relation.

Project documentation should migrate to one active statement of this relation.

ADR-0066 retains the common Canonicalization law and Canonicalization-specific HIR, Establishment, occurrence, and
Protocol ownership. ADR-0076 retains the exact built-in semantic definitions, built-in membership, qualification rules,
and exact law-to-law semantic relations that are closed here.

# 20. Consequences

The exact built-in law shape remains explicit without becoming a universal property bag. Common Canonicalization
obligations remain common, while presence, alternatives, ordering, multiplicity, collision, semantic bounds, unknown
member behavior, and other specialized distinctions enter a law only when that law actually observes them.

V1 still does not expose arbitrary user-composed normalization pipelines. One selected coordinate resolves to one closed
built-in semantic law. Curated Composite Catalog Laws remain possible after whole-profile qualification. General custom
law support and arbitrary composition remain separate future design problems.

# 21. Open Work After This ADR

The remaining work in this ADR is limited to the built-in Catalog itself.

The first decision batch is:

```text
1. Exact Law Common-Core Shape
   Section 6.1.1

2. Typed Law-Specific Meaning
   Section 6.1.2

3. Composition Relation Vocabulary
   Section 6.2.1

4. V1 Curated Composite Admission Set
   Section 6.2.2

5. N-Ary Composition Qualification
   Section 6.2.3
```

The external semantic material split in Section 16 must then be applied candidate by candidate without collapsing exact
semantic material, provider identity, or conformance evidence into one generic `externalDependency` or version field.

After those Catalog questions are closed, detailed candidate qualification resumes. NFC, NFD, NFKC, NFKD, and NFC Case
Fold remain the first semantic review batch. Their exact semantic operand, `E_L`, `C_L`, meaning-determining external
material, unknown or unassigned behavior where relevant, and composite interactions must be checked against the final
Catalog model rather than forcing the model to fit one candidate.

The following ADR-0076 items remain explicitly OPEN in this revision:

```text
6.1.1  Exact Law Common-Core Shape
6.1.2  Typed Law-Specific Meaning
6.2.1  Composition Relation Vocabulary
6.2.2  V1 Curated Composite Admission Set
6.2.3  N-Ary Composition Qualification
7.13    V1 Composition Interaction Matrix
7.14    V1 Additional Curated Composite Candidates
16      exact external-semantic-material classification per law
17      final Candidate Catalog decision
```

For project sequencing, ADR-0076 remains the current Catalog closure target.