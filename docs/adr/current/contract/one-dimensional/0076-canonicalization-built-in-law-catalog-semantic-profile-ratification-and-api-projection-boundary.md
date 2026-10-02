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

This ADR defines that catalog boundary, the initial V1 catalog, and the rules for admitting later profiles. Because a
ratified Catalog Law can become a semantic dependency of another system, it also fixes the outward stability boundary of
that law. Public API projection and implementation Design remain separate.

---

# 2. Problem

Kontrakt must avoid both an under-specified catalog and an over-broad one.

If V1 defines only the authoring mechanism, a selected nominal law has no authoritative semantic target. HIR and
Establishment would then have to recover meaning from implementation material, which would move Contract authority below
the semantic boundary.

The opposite failure occurs when a broad law name leaves several legal representatives or interpretations open. A
built-in law cannot delegate those choices to a host library, parser, iteration order, locale, or backend.

The same discipline applies to external standards. A standard can be precise enough to guide a Kontrakt law while still
leaving versioned data, optional behavior, or purpose-specific interpretation unresolved. Kontrakt must close every
distinction that can change its own representative or owned refusal.

Deterministic byte production is a separate concern unless exact bytes are themselves the representative owned by the
selected Contract law. This ADR does not create a user-facing canonical-byte facility merely because other ecosystems
use the word canonicalization for signing or serialization.

This ADR therefore decides the qualification boundary for built-in laws and records the current V1 candidates. A
candidate becomes ratified only after its semantic, determinism, evolution, adversarial, and evidence obligations are
closed.

# 3. Decision Drivers

Determinism is the first qualification requirement. The same law-owned determinants and the same legal Input must
produce the same Canonicalization-owned outcome under every legal realization.

That rule requires complete determinant closure. Meaning cannot depend on ambient locale, current provider state, host
iteration order, scheduling, cache history, or another undeclared source. External semantic material becomes explicit
Basis when it can change the result.

The catalog must also preserve the authority boundary established by ADR-0066. A built-in law establishes one
representative under one exact equivalence relation; convenience, validation, ordering, serialization, or business
transformation do not become Canonicalization merely because they are useful.

A law must remain understandable without its implementation. API names and backend strategies may change while the law
remains the same. Performance work may exploit established semantic properties, but target cost or implementation
convenience cannot choose a different representative.

V1 should admit only laws whose hostile-input behavior can be reviewed and whose conformance can be checked
independently of one implementation. Specialized domains remain possible when their Basis and authority boundaries are
explicit.

The catalog must also preserve fine-grained reuse. One law or Basis change must not force unrelated Canonicalization
material to change merely because the implementation stores them together.

A ratified Catalog Law is also an outward semantic surface. Another system may persist its law reference, derive its own
indexes or keys from the representative, or otherwise rely on the legal observations that Kontrakt deliberately
publishes. Kontrakt must therefore keep those semantic promises stable without turning incidental catalog layout,
generated API shape, or realization details into compatibility obligations.

# 4. Decision

Kontrakt will maintain a **Canonicalization Built-In Law Catalog** as Contract-owned semantic specification material.

Catalog membership is an ADR-level decision. A ratified entry owns an exact semantic profile, including the
representative, the distinctions it erases, the determinants that can affect the result, and any Canonicalization-owned
refusal.

The catalog does not own host-language names or implementation algorithms. API Specification maps source names to
Catalog Laws, while Design decides how a ratified law is realized.

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

A candidate law is not ratified merely because it appears in this ADR draft. It must first satisfy the qualification
gate in Section 5.

Source projection, implementation, test vectors, and optimization remain downstream of the Catalog Law. None of them may
complete missing Contract meaning on the law's behalf.

Once ratified and published, a Catalog Law also defines the semantic observations that an independent consumer may
legitimately rely on. That outward promise follows the Catalog Law identity rather than the current compiler
implementation. Internal catalog ordering, storage coordinates, generated evaluator names, cache keys, or other
realization artifacts remain outside that promise unless another specification explicitly publishes them.

# 5. Catalog Law Qualification

Catalog admission is stricter than showing that one normalization routine is useful. A V1 built-in law must close
determinism first, then prove that its semantic relation, evolution behavior, hostile-input realization, and
verification surface are sufficiently exact for Kontrakt to own.

## 5.1. Determinism Closure

A Catalog Law is ratifiable only when its Canonicalization-owned outcome is determined entirely by explicit law-owned
determinants.

For a law `L`, legal Input `x`, and any two legal realizations `R1` and `R2` operating under the same law identity and
the same Required Basis binding:

```text
Outcome(R1, L, x) = Outcome(R2, L, x)
```

The outcome is the exact representative or the exact refusal owned by `L`.

Ambient state cannot participate invisibly. Any external semantic source that can change the outcome must be closed as a
determinant or Required Basis. Physical execution state cannot choose Contract-visible meaning.

A referenced standard may leave implementation freedom that Kontrakt does not. Any choice capable of changing the
representative or owned refusal must be fixed by the Kontrakt profile. When representative selection requires ordering
or tie-breaking, that choice must follow semantic material rather than encounter order, hash layout, or another physical
artifact.

Budget and Capacity remain separate 1D Contracts. This section does not redefine their judgments or turn physical
resource behavior into Canonicalization meaning.

## 5.2. Semantic Closure

The law-defined relation must be an exact equivalence relation over the law's successful domain. Its representative must
identify that partition without accidental over-collapse.

For successful inputs:

```text
x equivalent-to y under L
    iff
C_L(x) = C_L(y)
```

The representative must remain inside the same equivalence class and must be stable.

```text
x equivalent-to C_L(x) under L

C_L(C_L(x)) = C_L(x)
```

The law must also close the presentation interpretation it consumes. A structured textual profile cannot validate one
interpretation and later rely on another parser that gives the same source different meaning. The successful domain and
any Canonicalization-owned refusal boundary must therefore be part of the profile.

Canonicalization selects a representative of already-declared meaning. A transformation that acquires or changes meaning
belongs to another authority.

## 5.3. Composition and Reuse Closure

A law must state any semantic property on which safe reuse can depend.

Canonical material is not assumed to remain canonical after arbitrary composition. If concatenation, aggregation,
slicing, or another domain operation can invalidate the representative relation, the compiler must not infer otherwise
merely because each input fragment was canonical.

A law may instead establish a narrower locality property that permits local repair or incremental recomputation. Such a
property belongs to the semantic profile only when it is true across legal realizations. The algorithm that exploits it
remains Design.

When no useful composition property exists, the law may state only whole-value re-establishment. That is preferable to
an unsound optimization seam.

## 5.4. Adversarial Realizability

A V1 built-in law must have a hostile-input work shape that can be reviewed before it is exposed as a default Contract
facility.

The review asks whether legal inputs can cause disproportionate traversal, buffering, expansion, or search. A law with
difficult worst cases is not automatically invalid, but its profile must not hide those cases behind an apparently cheap
API.

A mitigation may stop work under the authority that owns that stop. It may not silently change the equivalence relation
or representative in order to make the implementation cheaper. A mitigation that changes semantic outcome defines a
different Contract obligation.

This section does not set Budget or Capacity values. Those remain with their owning 1D Contracts.

## 5.5. Evolution and External Stability Closure

A ratified Catalog Law must remain stable for the legal observations that its semantic identity publishes. Another
Kontrakt release may replace the implementation, reorganize the catalog, or change a host-language projection without
assigning different Canonicalization meaning to the same law identity.

A change to equivalence, representative, successful domain, owned refusal, or another Contract-visible determinant is a
semantic change when it can alter a legal observation. Such a change requires a new semantic profile or an explicit
migration or compatibility relation; it cannot be hidden behind the old Catalog Law identity.

A meaning-determining Basis, registry, or standard revision cannot drift through the host environment. Version pinning
is required when the outcome can actually vary, while a ratified stability guarantee may justify a narrower dependency.
A later Basis may remain compatible with an earlier law use only when the relevant legal observation is established to
be unchanged. Compatibility is therefore scoped to the exact observation and may be directional rather than inferred
from matching names, versions, or shapes.

When material can acquire new meaning under a later Basis, the profile must define how currently unknown or unassigned
material behaves. That rule is part of the law rather than an implementation fallback.

The catalog must also avoid creating accidental promises. Stable semantic meaning does not make catalog enumeration
order, internal numeric identifiers, generated evaluator shape, or other observable realization details part of the law.
If an outward detail is intentionally left outside the promise, the public surface should not require consumers to
recover semantic meaning from it.

Deprecation or removal from a future support set does not retroactively rewrite the meaning already promised by a
ratified law. Support lifetime and semantic identity remain separate questions.

## 5.6. Evidence Closure

A law is not ratified until its specification can be verified independently of one implementation.

Normative examples and generated properties must establish exact convergence and preservation of non-equivalent
distinctions. They must also verify representative stability and any owned refusal. Hostile-input cases must exercise
the complexity assumptions on which V1 admission relies.

Kontrakt must also compare materially different realization paths. A clean reference path and optimized paths must
expose the same legal outcome. Parallelism, caching, persistence, or a supported backend change cannot alter the result.

External implementations are useful for differential testing. A disagreement is evidence to investigate, not authority
to copy.

# 6. Catalog Entry Model

A catalog entry must be complete enough that the compiler never needs to reopen a public source type or an
implementation library to discover the selected law's meaning.

| Catalog concern             | Required meaning                                                                                                |
|-----------------------------|-----------------------------------------------------------------------------------------------------------------|
| Semantic law id             | Stable reference to the law family and semantic profile                                                         |
| Presentation domain         | Exact Input presentation domain and interpretation consumed by the law                                          |
| Equivalence                 | Exact same-meaning partition owned by the law                                                                   |
| Preserved distinctions      | Distinctions that remain observable after Canonicalization                                                      |
| Representative              | Exact successful representative                                                                                 |
| Determinants                | Complete law-owned inputs that can change the outcome                                                           |
| Required Basis              | Separately established external semantic material, when required                                                |
| Refusal                     | Exact Canonicalization-owned negative outcome, if one exists                                                    |
| Composition                 | Semantic facts that permit reuse, local repair, or require whole-value re-establishment                         |
| Adversarial envelope        | Hostile-input characteristics that constrain safe realization                                                   |
| External legal observations | Exact Catalog Law semantics that independent consumers may rely on                                              |
| Evolution and compatibility | Changes that preserve those observations, require a new profile, or require explicit compatibility or migration |
| Conformance                 | Evidence required to ratify and continuously verify the law                                                     |

This model is logical. It does not require one runtime object, one table row, or one persistent record containing every
item.

# 7. Catalog Classes

The catalog uses semantic classes to control scope. They are not a runtime enum exposed to user code.

## 7.1. Core General Law

A Core General Law solves a common application problem without requiring a user-selected professional Basis. A law may
still depend on a ratified Kontrakt Basis when that material is part of the law's exact semantics.

A ratified Core General Law may ship with the base V1 Canonicalization API.

## 7.2. Standard Profile Law

A Standard Profile Law adopts a narrowly identified public standard whose equivalence and representative are precise
enough to become one Kontrakt law.

The external standard is evidence and semantic input, not an automatic authority transfer. Kontrakt must close every
option, version dependency, and interpretation that can change its own outcome.

## 7.3. Specialized Domain Law

A Specialized Domain Law requires domain-specific semantic material or a domain model that is not appropriate for the
base catalog.

It is not published until its Basis, domain, representative, adversarial properties, and HIR / Establishment obligations
can be expressed without weakening the common qualification gate.

# 8. V1 Core General Candidate Catalog

The laws in this section are the current V1 Core candidates. Presence in this section does not mean ratification. Each
law must pass Section 5 before its public API projection becomes stable.

The names below are semantic law names. Public Java and Kotlin names belong to API Specification.

## 8.1. Unicode NFC

**Semantic law id:** `text.unicode.nfc`

The presentation domain is Unicode text admitted by the selected Input surface.

The candidate selects Unicode Normalization Form C as the representative for canonical equivalence. Compatibility
distinctions remain observable.

Ratification must close the exact Unicode Basis dependency rather than inherit host Unicode tables. Unicode
normalization stability may allow part of the profile to remain stable across later versions, while unassigned code
points still require an explicit evolution policy. That distinction must be settled before this candidate becomes
ratified.

Canonical bytes are not part of this candidate law.

## 8.2. Unicode NFD

**Semantic law id:** `text.unicode.nfd`

The candidate uses the same canonical-equivalence domain as NFC and selects Unicode Normalization Form D as the
decomposed representative.

Ratification must close the same Basis and unassigned-code-point questions as NFC. It must also justify retaining NFD in
the base V1 surface rather than treating it as a specialized text-processing profile.

## 8.3. Unicode NFKC

**Semantic law id:** `text.unicode.nfkc`

The candidate applies Unicode compatibility decomposition followed by canonical composition. Selecting it therefore
declares compatibility distinctions irrelevant in addition to canonical distinctions.

NFKC is not an implementation substitute for NFC. Ratification must close its exact Unicode Basis, evolution behavior,
and intended presentation domain before this stronger equivalence becomes a base V1 law.

## 8.4. Unicode NFKD

**Semantic law id:** `text.unicode.nfkd`

The candidate erases the same compatibility distinctions as NFKC and selects the compatibility-decomposed
representation.

Its law is semantically clear only after the same Basis and evolution questions are closed. V1 value must also be
demonstrated because a decomposed compatibility form is less common as an application boundary representative.

## 8.5. Unicode NFC Case Fold

**Semantic law id:** `text.unicode.nfc-casefold`

This candidate is intended to collapse Unicode canonical-equivalent and default-caseless distinctions while selecting
one stable Unicode-defined representative.

Ratification must close the exact transformation order, case-folding profile, Unicode Basis, unassigned-code-point
behavior, and composition properties. Locale-sensitive lowercasing is not part of the law and cannot substitute for the
ratified profile.

## 8.6. Unicode NFKC Case Fold

**Semantic law id:** `text.unicode.nfkc-casefold`

This candidate targets Unicode `NFKC_Casefold` semantics for an identifier-like domain in which compatibility and
caseless distinctions are intentionally erased.

Ratification must use the exact Unicode profile rather than an arbitrary sequence of host normalization and lowercasing
calls. Its Basis, treatment of default-ignorable material, evolution behavior, and composition properties remain part of
the qualification review.

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

The presentation domain is IEEE 754 binary32 when raw NaN representation remains observable at the Input boundary. The
candidate maps every NaN bit pattern to one quiet-NaN representative while preserving every non-NaN bit pattern,
including signed zero.

The current candidate representative is `0x7fc00000`. Ratification must confirm that this exact bit-level observation
survives every supported Input and backend path before the bit pattern becomes normative law material.

## 8.12. Binary64 Canonical NaN

**Semantic law id:** `number.binary64.canonical-nan`

The binary64 candidate follows the same rule as the binary32 profile. Every NaN representation maps to one quiet NaN,
while every non-NaN bit pattern remains unchanged.

The current candidate representative is `0x7ff8000000000000`. Ratification carries the same bit-observability
requirement as binary32.

---

# 9. V1 Standard Profile Candidate Catalog

These profiles are V1 candidates backed by external standards. They are not ratified until the Kontrakt profile closes
every interpretation and Basis dependency that can change the outcome.

## 9.1. IPv6 RFC 5952 Text

**Semantic law id:** `network.ipv6.rfc5952`

The candidate domain is legal textual IPv6 presentation without an external zone identifier. Alternate legal spellings
are equivalent when they denote the same IPv6 address, and RFC 5952 supplies the target text form.

Ratification must keep parsing legality separate from representative selection. The accepted textual domain and the
owner of malformed-input rejection must be explicit so that a host parser cannot silently enlarge or narrow the law.
Zone identifiers remain outside this profile, and the law performs no DNS lookup.

## 9.2. BCP 47 Language Tag

**Semantic law id:** `identifier.bcp47.rfc5646`

The candidate domain is a well-formed BCP 47 language tag under the selected Kontrakt profile. RFC 5646 supplies
canonicalization rules, while registry data becomes meaning-determining where the representative consumes it.

Ratification must close the exact registry Basis and its evolution consequences. The law cannot read a mutable current
registry as authority, and unsupported extension semantics cannot receive an implementation-defined representative.

## 9.3. UUID Lowercase Text

**Semantic law id:** `identifier.uuid.rfc9562-lowercase-text`

The candidate domain is the standard textual UUID form accepted by the profile. Hexadecimal letter case is declared
irrelevant and lowercase text is the representative.

Ratification must confirm that the accepted textual grammar is exact and that the profile adds enough V1 value to
justify a distinct law. It changes neither UUID bits nor version or variant meaning. A structured UUID value that no
longer contains textual case does not require this profile.

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

# 12. Canonical Bytes Are Outside This Catalog

This ADR does not create a Canonical-Byte Catalog class or a user-facing byte canonicalization facility.

Exact deterministic bytes belong to the authority that gives those bytes semantic significance. A signing protocol, wire
format, artifact identity scheme, or compiler persistence format may require one exact encoding without making that
encoding an inbound Canonicalization law.

A coordinate Catalog Law owns bytes only if those bytes are themselves the representative declared by that law.
Otherwise deterministic serialization remains with its protocol or implementation owner.

Compiler-internal deterministic encoding used for HID, caching, persistence, or artifact formation is therefore not
Canonicalization authority.

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
changed semantic law. Catalog semantic compatibility and host API compatibility are separate stability domains; neither
may be inferred from the other.

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

External semantic material belongs to a Catalog Law only when changing that material can change the law's outcome.

A ratified profile must identify that dependency narrowly enough for two implementations to determine whether they
operate under the same meaning. A broad platform label is not sufficient when the law actually depends on a particular
Unicode dataset, registry state, or domain reference.

Some laws need no external Basis because the profile fully defines their result. Other laws depend on ratified versioned
data. A future specialized law may require a per-application Basis established through ADR-0063.

The physical representation of a Basis remains replaceable. What matters here is semantic determinant closure and the
consequence of Basis change, not how the compiler stores the data.

# 20. Versioning, External Consumer Stability, and Catalog Evolution

A ratified Catalog Law is a public semantic dependency surface. Independent systems may rely on its declared
equivalence, representative, successful domain, owned refusal, and Basis obligations without depending on Kontrakt's
internal realization. The Catalog Law identity therefore names a stable semantic promise rather than a moving
implementation target.

Adding an unrelated law does not change an existing law. Reordering the physical catalog, changing generated evaluators,
replacing tables, or changing host API projection also does not change that law unless another outward specification
separately promises those details. External consumers must not need compiler-private structure to identify or interpret
Catalog meaning.

A change that can alter a legal observation under an existing law identity is a semantic change. Kontrakt must represent
that change through a new semantic profile or an explicit migration or compatibility relation rather than silently
reinterpreting the old identity. An implementation bug fix that restores the already-declared law does not create new
Contract meaning; the declared law remains the reference.

Compatibility is judged against the exact observation being consumed. A later Basis or profile may be compatible for one
use while remaining incompatible for another, and matching source names, version labels, binary shapes, or successful
decoding do not establish that judgment. Where compatibility is unknown and correctness depends on it, the old
observation is not assumed valid.

A Basis or standard revision does not automatically require a new profile when a ratified stability guarantee proves the
relevant result unchanged. When the result may change, directly dependent material must be reconsidered. Downstream
invalidation may stop once the consumer-visible Canonicalization observation is established to be unchanged.

Deprecation, support withdrawal, and semantic identity remain separate. A future release may stop offering a profile
according to an explicit support policy, but that does not retroactively assign a different meaning to material
established under the earlier ratified profile.

Editorial clarification and stronger verification do not create a new semantic profile when normative meaning remains
unchanged.

# 21. V2 Incremental and Reuse Boundary

The catalog must not become one monolithic dependency. A Definition that selects one law depends on that law and its
actual Required Basis, not on unrelated entries stored beside it.

V1 therefore preserves exact per-law references, determinant relations, and any composition property that later reuse
may rely on. V2 may exploit those facts through a different incremental architecture without changing Contract meaning.

```text
Canonicalization Definition
    ↓ selected law reference
Catalog Law
    ↓ only when required
Semantic Basis
```

Fragment reuse or local repair is legal only when the selected law's Section 5.3 obligations justify it. Otherwise clean
whole-value re-establishment remains the safe semantic path.

HID, fingerprints, Merkle structure, dependency graphs, and cache layout remain implementation mechanisms. They may
recognize or accelerate already-defined equality and validity; they do not define either.

# 22. Compiler Product and Protocol Boundary

Catalog Law is the semantic source even when the compiler realizes it through generated code, precomputed tables,
vectorized routines, or another optimized form.

Every legal realization must preserve Section 5.1. Target profitability may change the work performed but cannot select
a different Contract representative.

Compiler-owned products derived from the law remain subject to the compiler product protocol. Reuse, caching,
persistence, and parallel evaluation may change execution history while preserving the same legal observation.

A compiler-private representation does not become an external Catalog protocol merely because tooling can inspect it. If
Kontrakt later publishes machine-readable catalog metadata for independent consumers, that artifact needs its own
declared stability domain and must project Catalog meaning without exposing private realization as semantic identity.

This separation is what allows aggressive optimization without making one implementation technique part of
Canonicalization authority.

# 23. Security and Adversarial Input

Built-in Canonicalization operates on outside-controlled material, so hostile input is part of catalog admission rather
than a later implementation concern.

A law must expose enough of its difficult input shape for Design and verification to defend the machine without changing
the law. Pathological structure may justify operational refusal or limits only through the authority that owns that
judgment.

Security suspicion does not create equivalence. A visually confusable or otherwise suspicious value remains semantically
distinct unless the selected Catalog Law explicitly declares the distinction irrelevant.

The same interpretation must also survive downstream use. A parser or library cannot re-read the representative under
incompatible semantics and thereby resurrect or invent distinctions after Canonicalization.

# 24. Conformance and QA

Ratification requires evidence for the law, not merely for one implementation.

Normative vectors and generated properties must prove exact equivalence convergence, preservation of non-equivalent
distinctions, representative stability, and any owned refusal boundary. Evolution-sensitive laws must also test the
Basis transitions that the profile claims to support. Stable law identities additionally require regression evidence
that a compiler or catalog rewrite has not changed the published legal observation.

Kontrakt must compare a clean reference realization with materially different legal realizations. Optimized, parallel,
cached, restored, or target-specific paths must expose the same Canonicalization-owned outcome when their semantic
determinants are the same.

Adversarial tests must exercise the difficult input shapes identified by Section 5.4. Performance regressions remain
Design and QA concerns, but a performance workaround is invalid if it changes the representative or semantic domain.

Differential testing against an external implementation is useful evidence. It never replaces the ratified law or its
normative vectors.

# 25. Current V1 Candidate Summary

This draft does not mark a law `Ratified` before its Section 5 review is complete.

| Class            | Semantic law id                          | Current status |
|------------------|------------------------------------------|----------------|
| Core             | `text.unicode.nfc`                       | Candidate      |
| Core             | `text.unicode.nfd`                       | Candidate      |
| Core             | `text.unicode.nfkc`                      | Candidate      |
| Core             | `text.unicode.nfkd`                      | Candidate      |
| Core             | `text.unicode.nfc-casefold`              | Candidate      |
| Core             | `text.unicode.nfkc-casefold`             | Candidate      |
| Core             | `text.ascii.casefold`                    | Candidate      |
| Core             | `text.line-ending.lf`                    | Candidate      |
| Core             | `text.unicode.whitespace-trim`           | Candidate      |
| Core             | `number.decimal.numeric-value`           | Candidate      |
| Core             | `number.binary32.canonical-nan`          | Candidate      |
| Core             | `number.binary64.canonical-nan`          | Candidate      |
| Standard Profile | `network.ipv6.rfc5952`                   | Candidate      |
| Standard Profile | `identifier.bcp47.rfc5646`               | Candidate      |
| Standard Profile | `identifier.uuid.rfc9562-lowercase-text` | Candidate      |

The first detailed ratification batch is NFC, NFD, NFKC, NFKD, and NFC Case Fold. Each law must pass the complete
Section 5 gate before it becomes `Ratified`.

The deferred profile families in Section 10 and the specialized domains in Section 11 remain outside this candidate
table until their current blocking issue is resolved.

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

The V1 frontend gains a finite semantic target without granting authority to host libraries or implementation
algorithms. Once a Catalog Law is ratified, resolution can produce one semantic reference that HIR preserves and
Establishment can consume.

Determinism becomes an admission property of the law rather than an expectation placed on individual implementations.
This gives the backend more optimization freedom because different legal realizations may change physical work while
remaining observationally identical at the Contract boundary.

The stronger qualification gate also makes the base catalog smaller until evidence is complete. That is intentional. A
useful profile stays a candidate rather than becoming public Contract meaning while its Basis, evolution behavior,
adversarial properties, or outward stability obligations remain unresolved.

Once a law is ratified, external systems can depend on its declared semantic observations without inheriting Kontrakt's
table layout, generated implementation, or compiler version as part of that dependency. This protects consumers from
silent semantic drift and protects Kontrakt from accidental compatibility debt.

V1 still does not expose arbitrary user-composed normalization pipelines. One coordinate selects one closed law. General
custom-law support remains a separate extension problem that must not turn callbacks into Contract authority.

# 29. Open Work After This ADR

The immediate work is to apply Section 5 to the first Unicode batch rather than add more catalog candidates.

NFC, NFD, NFKC, NFKD, and NFC Case Fold must each be checked against the current Unicode specification and stability
guarantees. The remaining Basis and evolution questions must be closed first. Composition and refusal behavior must then
be verified before V1 Core membership is decided.

The next batch covers NFKC Case Fold, ASCII Case Fold, LF Line Ending, Unicode Boundary Whitespace Trim, and Decimal
Numeric Value. Binary NaN profiles and the standard-backed candidates follow only after the same gate is applied.

Once ADR-0076 is closed, ADR-0075 is the next ADR to close. Its compiler-product protocol should then be checked against
the concrete Catalog Law, established semantic material, and realization boundaries produced here.

The Canonicalization HIR and Establishment re-audit remains necessary after those semantic boundaries are stable. API
Specification follows ratification; Design follows the API-independent semantic law. Canonical-byte protocols remain a
separate decision rather than an extension of this coordinate catalog.