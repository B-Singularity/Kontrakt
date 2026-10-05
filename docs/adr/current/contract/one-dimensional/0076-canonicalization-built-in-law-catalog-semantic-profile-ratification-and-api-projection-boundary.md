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
candidate must be reviewed against Section 5 before the ADR can become Accepted. A later built-in addition requires an
explicit semantic Catalog decision rather than an implementation update or mutable registry entry.

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

# 4. Decision

Kontrakt will maintain a **Canonicalization Built-In Law Catalog** as the logical boundary for the built-in
Canonicalization vocabulary defined by this ADR.

The Catalog is not one `CatalogEntry` record and it is not defined by one physical table. It is a normative manifest
boundary over the built-in Canonicalization law Authorities admitted by Kontrakt. The manifest may be realized together
with law material or separately by the compiler, but physical co-location, one row layout, one generated enum, or one
lookup structure does not merge their meanings or create Contract authority.

The current boundary is:

```text
Canonicalization Built-In Law Catalog
    Built-In Law Authority Membership

Exact Built-In Law Authorities specified by this ADR
    one independently version-sensitive Contract Authority per exact law
    one exact law Definition for each Version of that Authority
```

The Catalog itself is not another version-sensitive Contract Authority. It owns no `CatalogVersion`, no law Version
history, and no implicit current-Version selection. Built-In Law Authority Membership answers which law Authorities
belong to the built-in Canonicalization vocabulary. A new Version under an already-admitted law Authority does not
change
Catalog membership.

Adding or removing a built-in law Authority requires an explicit Catalog decision; compiler release, implementation
registration, physical discovery, provider availability, or a newly published law Version cannot change membership
implicitly. Exact law identity and Version remain owned by the member law Authority under ADR-0053 and ADR-0063.

ADR-0066 owns the common semantic shape of Canonicalization. This ADR owns the exact semantics of built-in laws needed
to
make the built-in vocabulary closed.

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
representative coverage, law-specific failure meaning, or another law-owned observation. An external semantic source
that
can change the law must be explicit semantic material.
Host locale, provider state, encounter order, hash layout, cache history, scheduling, physical catalog revision, and
other
realization artifacts cannot finish the law.

A referenced standard may leave choices that Kontrakt does not. Any option, tie-break, ordering rule, unknown-member
rule, revision choice, or other semantic branch that can change the legal observation must be closed before
ratification.
The exact representation of those choices must remain typed and law-specific rather than becoming an open property bag.

## 5.2. Exact Semantic Profile Closure

ADR-0066 owns the common Canonicalization law. ADR-0076 does not restate its equivalence, representative, determinism,
idempotence, judgment, failure, or occurrence rules. This Catalog ADR supplies only the exact semantic content needed to
specify each built-in law under those common rules.

Every built-in candidate must identify the exact already-established Input-owned semantic presentation over which the
law operates. Java or Kotlin carrier identity, parser choice, source syntax, runtime object topology, or another host
classification cannot substitute for that semantic operand requirement.

Every built-in candidate must define its exact same-meaning relation and its exact representative selection. Those are
the candidate-specific `E_L` and `C_L` required by ADR-0066. The Catalog does not allow the implementation to infer one
from host equality, parser behavior, provider defaults, or the behavior of a normalization routine.

Every built-in candidate must also close its **Exact Representative Coverage**. The law must state whether every legal
operand admitted by its Exact Operand Requirement has a representative under the law, or whether representative
establishment is restricted for some otherwise legal operands. Total coverage and restricted coverage are both explicit
semantic statements; absence of failure text does not implicitly mean total coverage.

When representative coverage is restricted, the exact Canonicalization-owned semantic condition that prevents
representative establishment is part of that law's definition. If the law distinguishes several Contract-visible
negative
reasons, those reasons must be closed well enough that a later diagnostic does not have to reconstruct them from parser
or
implementation behavior. This ADR does not create a generic failure field or redefine the common failure boundary owned
by ADR-0066.

When a particular law requires semantic material beyond the Input-owned operand meaning, and that material can change
the law's exact equivalence, representative, representative coverage, or law-specific failure meaning, the exact
material
is part of that law's definition. Provider identity, library version, conformance evidence, provenance, and physical
realization do not become semantic determinants merely because an implementation uses them.

A built-in law may observe additional Input-owned distinctions such as presence, explicit absence, finite alternatives,
ordering, multiplicity, duplicates, aggregate collision, semantic bounds, or unassigned material only when that exact
law actually observes them. V1 does not create one universal optional-field record containing every such concern.

A separate mandatory `Representative Range` is not introduced merely because it can be derived from the exact
representative definition. A generic `options`, `profile`, `flags`, `determinants`, or `conditionalClauses` property bag
is
also rejected as the default representation of law-specific meaning.

Canonicalization selects a representative of already-declared meaning. A transformation that acquires or changes meaning
belongs to another authority.

## 5.3. Adversarial Realizability

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

## 5.4. Evolution Closure

A ratified built-in law must remain stable for the semantic observations its identity actually promises. Another
Kontrakt release may replace the implementation, reorganize physical catalog material, or change a host-language
projection without assigning different Canonicalization meaning to the same exact versioned law.

Each Exact Built-In Law is an independently version-sensitive Kontrakt Contract Authority. A change to the Exact Operand
Requirement, Exact Equivalence Definition, Exact Representative Definition, Exact Representative Coverage, law-specific
failure meaning, meaning-determining semantic material, typed law-specific distinction, or another actual determinant is
a semantic change when it can alter that Authority's Contract-visible meaning. That changed meaning cannot be published
under the same Version. ADR-0053 owns Version identity, continuity, conflict, immutable history, and claim resolution;
when the same law Authority continues with changed meaning, the change must be represented by an explicit new Version
rather than by mutation of the old one.

A later Version under the same law Authority does not revise Catalog membership and does not replace an earlier Version.
When a consumer requires an exact earlier Version, resolution must return the exact meaning owned by that Authority and
Version if it remains legally selectable for that use. A separately established lifecycle, Governance, or other
selection rule may refuse that use before the Version is consumed; this ADR does not define that prohibition. If the
resolved or supplied law material carries a different Authority or Version from the exact request, resolution fails. No
`current`, `latest`, `preferred`, nearest-Version, or silent-upgrade fallback is permitted.

Law Version history is therefore not Catalog history. The Version architecture preserves earlier immutable law Versions;
the Catalog manifest records only membership of the law Authorities. A compiler or publication system may retain the
exact Catalog snapshot used for reproducibility or integrity, but such snapshot identity, generation, provenance, or
fingerprint is not Contract Version meaning and does not become a second law-history authority.

A meaning-determining Basis, registry, or standard revision cannot drift through the host environment. Exact
version-or-snapshot pinning is required only when that exact revision can change the law's meaning. A ratified stability
guarantee may justify a narrower semantic dependency that is not tied to every external release number. Provider
version, library version, standard label, and semantic Basis identity are not interchangeable merely because they often
change together. `current`, `latest`, ambient registry state, or another undeclared external revision cannot silently
change the meaning of an already-versioned law.

When material can acquire new meaning under a later semantic Basis, the exact law must define how currently unknown,
unassigned, or future material behaves wherever that distinction is observable. An implementation fallback cannot invent
the rule.

The Catalog must also avoid accidental promises. Stable law meaning does not make enumeration order, internal numeric
identifiers, generated evaluator shape, or other realization details part of the semantic contract.

# 6. Catalog Logical Content and Semantic Families

The Catalog is a normative manifest boundary over built-in law Authorities. It is not a universal semantic record and
it does not own the Version history of its members.

## 6.1. Catalog Logical Content and Exact Law Definition Boundary

The Catalog manifest owns one logical relation:

```text
Built-In Law Authority Membership
```

`Built-In Law Authority Membership` answers which independently owned Exact Built-In Law Authorities belong to the
Kontrakt built-in Canonicalization vocabulary after Catalog qualification has been satisfied. Membership identifies the
law Authority, not one current or preferred Version of that Authority. A candidate review label, host-language symbol,
API name, compiler row, dense handle, physical lookup key, or Catalog publication snapshot cannot substitute for that
semantic Authority identity.

Each Exact Built-In Law Authority is independently version-sensitive under ADR-0053. In the current model one such
Authority owns one complete Exact Built-In Law Definition for each of its Versions, so no additional Authority-Local
Definition Coordinate is required merely to distinguish several laws inside the Catalog. The exact authoritative law
reference therefore resolves through the law Authority and the exact requested Version under ADR-0063. A different law
Authority or a different Version denotes a different exact versioned law Definition even when some resolved semantic
material compares equal.

A new Version of an existing member Authority does not add another Catalog member. Earlier Versions remain Version-owned
historical meaning rather than historical Catalog entries. The Catalog neither redirects an earlier Version request to a
newer Version nor duplicates Version continuity, conflict, history, or claim resolution.

The Exact Built-In Law Definitions specified by this ADR contain the candidate-specific semantic meaning needed to close
each member Authority under ADR-0066. They are owned by those law Authorities rather than by a Catalog-wide versioned
super-authority. They do not absorb API naming, assurance evidence, compiler optimization permissions, realization
algorithms, or physical lookup coordinates.

### 6.1.1. Exact Law Common-Core Shape

An Exact Built-In Law contains only the candidate-specific semantic material needed to define that law under the common
Canonicalization rules owned by ADR-0066. The Catalog does not duplicate those common rules as per-law fields.

Every Exact Built-In Law must close the following semantic content:

| Exact law content                        | Required meaning                                                                                                                                                                                   |
|------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Exact Operand Requirement                | The exact already-established Input-owned semantic presentation over which this built-in law is defined                                                                                            |
| Exact Equivalence Definition             | The concrete same-meaning relation that this built-in law declares under ADR-0066                                                                                                                  |
| Exact Representative Definition          | The concrete representative selection that this built-in law declares under ADR-0066                                                                                                               |
| Exact Representative Coverage            | An explicit statement that representative establishment is total for every legal operand, or restricted for an exact semantic subset of otherwise legal operands                                   |
| Exact Law-Specific Failure Semantics     | Required when Exact Representative Coverage is restricted; the exact Canonicalization-owned semantic condition or conditions that prevent representative establishment must be closed              |
| Exact Law-Specific Semantic Determinants | Required only when semantic material beyond the Input-owned operand meaning can change this particular law's equivalence, representative, representative coverage, or law-specific failure meaning |

The first four items are required for every built-in law. Law-specific failure semantics are required whenever coverage
is
restricted. Law-specific semantic determinants exist only when the particular law actually owns that additional meaning.
Their absence is not represented by generic empty fields.

ADR-0066 remains the owner of the common equivalence laws, representative laws, determinism, idempotence, judgment
shape,
common failure boundary, Input-sameness compatibility, and occurrence semantics. Those common obligations are applied to
the exact content above; they are not duplicated in this Catalog definition.

The exact operand requirement is semantic rather than physical. A JVM carrier type such as `String`, `Float`, or
`ByteArray` does not by itself define the operand meaning of a built-in law.

The exact equivalence definition and exact representative definition are independently normative law content because
ADR-0066 requires both. A backend routine, host equality relation, or observed implementation result cannot substitute
for
either definition.

Exact Representative Coverage is also normative rather than inferred from implementation behavior. A total law
explicitly
states that every legal operand covered by its Exact Operand Requirement has a representative. A restricted law
explicitly
states the semantic boundary at which representative establishment fails. Restricted coverage does not cause selection
to
be retried at occurrence time and does not authorize pass-through, fallback to another law, or delegation to Admission
or
Lowering.

When coverage is restricted, this Catalog records only the particular semantic condition or conditions under which the
selected exact law reaches the Canonicalization failure boundary owned by ADR-0066. If several negative reasons are
Contract-visible, they must be closed as law meaning; diagnostics may explain those reasons but do not invent them.

Law-specific semantic determinants are not a generic `determinants` collection. Only material that can actually change
the particular law's exact meaning belongs here. Provider identity, implementation version, verification evidence,
provenance, cache state, and physical catalog representation remain outside exact law meaning unless a separate semantic
argument establishes otherwise.

The complete Exact Built-In Law Definition Meaning is the combination of the four mandatory semantic contents above and
any conditional law-specific failure, external semantic, or typed law-specific meaning that actually determines the law.
A Contract-visible change to that complete meaning is therefore subject to Section 5.4 and ADR-0053. Equal Definition
Meaning does not by itself merge different law identities, Authorities, Versions, or Definition References.

A separate mandatory representative-range field is not required when it is derivable from the exact representative
definition. Preserved or collapsed distinctions may be documented to explain and verify a law, but they do not become a
second authoritative copy of the exact equivalence definition.

### 6.1.2. Typed Law-Specific Meaning

**OPEN**

The common core must not force every law to carry irrelevant semantic categories. A scalar Unicode law need not own map
collision semantics. An aggregate law cannot omit collision, ordering, duplicate, element, or key semantics when those
distinctions are observable under the law.

Typed law-specific meaning is not a parallel authority beside the Exact Equivalence Definition, Exact Representative
Definition, Exact Representative Coverage, or law-specific failure meaning. When a law family needs additional typed
semantic constituents, those constituents must complete those exact meanings rather than create a second conflicting
summary of them. Explanatory or verification-only material remains non-authoritative.

The final V1 typed family vocabulary must therefore be derived from the candidate laws that survive qualification rather
than invented as one universal optional schema. A candidate is incomplete when a distinction it actually observes is
left to host equality, parser behavior, collection order, provider defaults, or implementation convention. A generic
`options`, `flags`, `properties`, or other open property bag does not substitute for typed law-specific meaning.

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

The laws in this section are grouped together for V1 review because they address general presentation or value
normalization. Presence here does not mean ratification, and each law must pass Section 5. Each candidate is reviewed as
one exact built-in law rather than as permission for users to sequence implementation operations.

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

This candidate is intended to collapse Unicode canonical-equivalent and default-caseless distinctions under one
independently specified exact law. It is not defined merely by sequencing the existing NFC and case-fold Catalog
entries,
and it must not be described as a Unicode-defined profile unless ratification identifies an exact normative Unicode
profile that owns the same relation.

Ratification must close the independent equivalence relation, exact representative definition, case-folding profile,
Unicode Basis, and unassigned-code-point behavior. Locale-sensitive lowercasing is not part of the law and cannot
substitute for the ratified profile.

## 7.6. Unicode NFKC Case Fold

**Candidate semantic label:** `text.unicode.nfkc-casefold`

This candidate targets Unicode `NFKC_Casefold` semantics for an identifier-like domain in which compatibility and
caseless distinctions are intentionally erased.

Ratification must use the exact Unicode profile rather than an arbitrary sequence of host normalization and lowercasing
calls. Its Basis, treatment of default-ignorable material, and evolution behavior remain part of the qualification
review.

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
content, or collapse blank lines.

## 7.9. Unicode Boundary Whitespace Trim

**Candidate semantic label:** `text.unicode.whitespace-trim`

The law erases leading and trailing code points with the Unicode `White_Space` property under the selected Unicode
Basis. Interior whitespace remains unchanged.

The Unicode property set, not Java `trim()` or `strip()`, defines the law. Because property membership can be
version-sensitive, the Unicode Basis is meaning-determining. The law does not collapse internal whitespace or perform
line-ending normalization.

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

---

# 15. Canonicalization Contract Integration Boundary

Canonicalization-specific HIR, Definition Candidate, Binding Candidate, Establishment, occurrence, Established Material,
and Established Semantic Protocol semantics are owned by ADR-0066 together with ADR-0071, ADR-0063, and the 1D master
checklist. This Catalog ADR does not define their final schema.

A Canonicalization Definition is not the same semantic subject as one built-in law. Actual Basis Binding, Applicability,
occurrence meaning, and Established Material are not Catalog membership. Each Exact Built-In Law is its own
version-sensitive Contract Authority. Exact law reference formation, Version claims, authoritative Version Binding,
historical reselection, and mismatch rejection follow ADR-0053 and ADR-0063; this ADR introduces no Catalog-specific
Version mechanism or Catalog-owned law history.

A consumer that requires an exact law Version must resolve that exact Version under the member law Authority. If a
separately established selection or lifecycle rule prohibits that Version, the use is refused under the authority that
owns that rule. Otherwise an available earlier Version remains eligible for exact historical resolution. Material from a
different Authority or Version cannot satisfy the request merely because it is newer, structurally similar, or currently
preferred.

# 16. External Semantic Basis

`external dependency` is not one semantic category. This ADR distinguishes external normative specification or profile,
meaning-determining semantic material, a Required Basis requirement, provider or implementation identity, stability
guarantees, and conformance or assurance evidence. These distinctions do not imply one universal external-dependency
record.

A standard title, provider name, library version, platform version, or external release number does not become semantic
meaning merely because it is convenient to record. `current`, `latest`, ambient registry state, or another undeclared
external revision cannot complete an Exact Built-In Law.

Each external item relevant to a candidate must be classified by the relation it actually has to that law:

```text
non-semantic realization or evidence
    -> provider, library, provenance, test or conformance material

law-fixed normative determinant
    -> exact external material is part of the Exact Built-In Law Definition Meaning

Required Basis requirement
    -> the law requires a basis under the common ADR-0066 / ADR-0063 architecture
    -> actual Basis Binding remains outside Catalog membership and exact law definition material

stability-scoped external semantics
    -> a ratified stability guarantee closes the relevant semantic observations
    -> every external release number need not become a determinant when the law's meaning is unchanged
```

These relations may coexist where a law depends on more than one external item. The classification is semantic, not a
request to encode the four cases as an enum or generic property bag.

When a built-in law itself fixes exact external semantic material and changing that material can change its Exact
Equivalence Definition, Exact Representative Definition, Exact Representative Coverage, or law-specific failure meaning,
that material participates in the law's determinant closure. An exact external version or immutable snapshot is required
when that exact revision is meaning-determining; it is not required merely because an external organization published a
new release. A ratified stability guarantee may justify a narrower dependency.

The exact classification of external material remains **OPEN per built-in law**. Unicode, registry-backed identifiers,
temporal data, scientific reference data, and other standards must not be forced into one universal external model
before
their actual semantic dependency is established.

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
Fold must each be reviewed as complete exact laws rather than as user-visible sequences of implementation operations.

Before this ADR can become Accepted, every entry retained in the initial V1 set must receive an explicit terminal
Catalog decision. That decision must not be inferred from implementation registration.

Deferred profile families in Section 9 remain outside this candidate table until their current blocking issues are
resolved.

# 18. Migration and Supersession

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
`ExactCanonicalization` filler is inserted, and the declaration contains no executable user canonicalizer.

Project documentation should migrate to one active statement of this relation.

ADR-0066 retains the common Canonicalization law and Canonicalization-specific HIR, Establishment, occurrence, and
Protocol ownership. ADR-0076 retains the exact built-in semantic definitions, built-in membership, and qualification
rules that are closed here.

# 19. Consequences

The exact built-in law shape remains explicit without becoming a universal property bag. Every exact law closes four
mandatory semantic contents: Exact Operand Requirement, Exact Equivalence Definition, Exact Representative Definition,
and Exact Representative Coverage. Law-specific failure meaning, external semantic material, and typed family meaning
appear only when the particular law actually owns them.

Common Canonicalization obligations remain common, while presence, alternatives, ordering, multiplicity, collision,
semantic bounds, unknown member behavior, and other specialized distinctions enter a law only when that law actually
observes them. Typed meaning cannot become a parallel source of authority beside the exact equivalence, representative,
coverage, or failure meaning.

Built-In Law Authority Membership and exact law meaning cannot drift because a compiler release, provider, registry, or
implementation changed. Each Exact Built-In Law Authority owns its own Contract Versions. Contract-visible semantic
change creates a new Version under that Authority rather than a new Catalog Version or an implicit mutation of an old
Version. A law Version revision by itself does not change Catalog membership.

V1 still does not expose arbitrary user-composed normalization pipelines. One selected coordinate resolves to one closed
built-in semantic law. General custom law support and arbitrary composition remain separate future design problems.

# 20. Open Work After This ADR

The remaining work in this ADR is limited to the built-in Catalog itself.

Section 6.1 now fixes the Catalog / law identity boundary. The Catalog is a built-in law Authority manifest. Each Exact
Built-In Law is an independent version-sensitive Contract Authority, one complete law Definition is owned per Version,
and Version history remains under ADR-0053 rather than under the Catalog.

The next decision sequence is:

```text
1. Typed Law-Specific Meaning
   Section 6.1.2

2. External semantic material
   Apply Section 16 candidate by candidate
   and classify each relevant item as law-fixed determinant, Required Basis requirement,
   stability-scoped semantics, or non-semantic realization / evidence

3. Candidate qualification
   Complete the exact operand requirement, equivalence, representative, representative coverage,
   law-specific failure semantics when coverage is restricted, and exact semantic determinants where present

4. Final V1 Built-In Membership decisions
   Give every retained candidate an explicit terminal Catalog decision
```

The external semantic material split in Section 16 must be applied candidate by candidate without collapsing exact
semantic material, Required Basis, provider identity, implementation version, provenance, stability evidence, or
conformance evidence into one generic `externalDependency` or version field.

Detailed candidate qualification should stress the Catalog model against materially different laws rather than close the
remaining model from one easy candidate. Decimal Numeric Value and Binary32 Canonical NaN exercise self-contained exact
representatives; Unicode NFC exercises external semantic material, stability, and unassigned behavior; IPv6 RFC 5952
exercises exact operand ownership and restricted-coverage pressure; BCP 47 exercises mutable registry dependence; NFC
Case Fold exercises a law whose exact meaning must not be reconstructed from an arbitrary implementation sequence.

The following ADR-0076 items remain explicitly OPEN in this revision:

```text
6.1.2  Typed Law-Specific Meaning
16     exact external-semantic-material classification per law
17     final Candidate Catalog decision
```

For project sequencing, ADR-0076 remains the current Catalog closure target.