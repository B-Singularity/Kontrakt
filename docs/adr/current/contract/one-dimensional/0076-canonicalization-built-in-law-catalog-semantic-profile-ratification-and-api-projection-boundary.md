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

A single Input coordinate may also need a meaning that combines more than one familiar canonicalization concern.
Kontrakt does not treat that need as permission to expose an arbitrary normalization pipeline. When a combination is
admitted, the Catalog owns one complete Composite Catalog Law whose equivalence, representative, ordering semantics,
determinant closure, refusal surface, and security properties are ratified as one semantic profile.

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

This ADR therefore decides the qualification boundary for built-in laws and records the current V1 candidates.
`Candidate` is a working state, not an accepted catalog state. During this ADR's Proposed lifecycle, each initial V1
candidate must be reviewed against Section 5 and its closed result recorded here before the ADR can become Accepted. V1
also admits a curated set of Composite Catalog Laws formed only from Kontrakt-approved law material after the complete
combination has passed the same qualification gate. Arbitrary user composition remains outside this ADR. A later
addition to an already accepted catalog requires a new catalog ADR rather than an implementation update or a mutable
registry entry.

# 3. Decision Drivers

Determinism is the first qualification requirement. The same law-owned determinants and the same legal Input must
produce the same Canonicalization-owned outcome under every legal realization.

That rule requires complete determinant closure. Meaning cannot depend on ambient locale, current provider state, host
iteration order, scheduling, cache history, or another undeclared source. External semantic material that can change the
result must be closed either as Definition-determining law material or as an explicit Required Basis requirement under
its owning law.

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

Composition must preserve the same boundary. A Composite Catalog Law is not identified by an implementation pipeline, by
a tuple of component API names, or by the order in which helper routines happen to run. Its semantic identity belongs to
the independently ratified composite profile. Component references may support specification, verification, or
realization reuse without becoming a substitute for that identity.

# 4. Decision

Kontrakt will maintain a **Canonicalization Built-In Law Catalog** as Contract-owned semantic specification material.

Catalog membership is an ADR-level decision. A ratified Catalog Law owns an exact semantic profile, including the
representative, the distinctions it erases, the determinants that can affect the result, and any Canonicalization-owned
refusal. The catalog record, table, generated index, or other carrier does not acquire that authority by containing the
law.

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
the same applicable Required Basis binding when one is required:

```text
Outcome(R1, L, x) = Outcome(R2, L, x)
```

The outcome is the exact representative or the exact refusal owned by `L`.

Ambient state cannot participate invisibly. Any external semantic source that can change the outcome must be closed as
Definition-determining law material or through an explicit Required Basis requirement and binding under its owning law.
Physical execution state cannot choose Contract-visible meaning.

A referenced standard may leave implementation freedom that Kontrakt does not. Any choice capable of changing the
representative or owned refusal must be fixed by the Kontrakt profile. When representative selection requires ordering
or tie-breaking, that choice must follow semantic material rather than encounter order, hash layout, or another physical
artifact.

Budget and Capacity remain separate 1D Contracts. This section does not redefine their judgments or turn physical
resource behavior into Canonicalization meaning.

## 5.2. Semantic Closure

A Catalog Law must define its equivalence relation independently of the representative procedure. Let `E_L` be the exact
same-meaning relation owned by law `L`, and let `C_L` be the representative function over the law's successful domain.
`E_L` must be an equivalence relation before `C_L` is used to test or implement it.

The representative must realize exactly that independently specified partition.

```text
C_L(x) = C_L(y)
    iff
E_L(x, y)

E_L(x, C_L(x))

C_L(C_L(x)) = C_L(x)
```

This prevents a defective representative procedure from defining its own equivalence by accidental over-collapse. The
specification of `E_L` is the authority against which representative formation and conformance are checked.

The law must also close the presentation interpretation it consumes. A structured textual profile cannot validate one
interpretation and later rely on another parser that gives the same source different meaning. Canonicalization begins
only after Input has established a legal presentation. A legal Input may still lie outside the selected law's
canonicalizable domain and receive a Canonicalization-owned refusal; material that never became legal Input does not
enter Canonicalization.

Selection applicability is positive and law-owned. A law may be selected only for an exact Input presentation meaning
that its profile explicitly admits. Absence of an admitted relation does not become permission, and an unknown or
incompatible presentation rejects the Contract definition before runtime. This selection boundary is distinct from the
canonicalizable value domain: the former decides whether the law may govern that presentation kind, while the latter
decides whether one legal occurrence can establish a representative or must receive a Canonicalization-owned refusal.

A combined built-in law must define its own `E_L`, `C_L`, determinant set, canonicalizable domain, owned refusal, and
exact ordering semantics. Sequentially applying two existing Catalog Laws does not by itself create a third Contract
law.

When two or more approved laws are candidates for one coordinate, implementation order is never allowed to complete
missing Contract meaning. If the complete legal observation is order-sensitive, each admitted ordered meaning is a
distinct Composite Catalog Law unless one separately ratified profile defines another exact result. If the Catalog
claims that order is irrelevant, that claim must be established for the complete composite observation rather than
inferred from apparently independent implementations.

Canonicalization selects a representative of already-declared meaning. A transformation that acquires or changes meaning
belongs to another authority.

## 5.3. Composition and Reuse Closure

Ratification must establish which reuse claims, if any, are sound consequences of the law. Canonical material is not
assumed to remain canonical after concatenation, aggregation, slicing, or another domain operation merely because the
source fragments were canonical.

A candidate may expose a certified property that permits composition closure, boundary-local re-establishment, or
another narrower reuse rule. That property is compiler-consumable knowledge derived from the authoritative law rather
than a second Canonicalization judgment. Adding stronger proof or a new optimization property does not by itself change
the Catalog Law identity when equivalence, representative, domain, determinants, and refusal remain unchanged.

Composition qualification is stricter than showing equality of final values for one implementation. Any
order-independence claim used to admit or optimize a composite must preserve the complete Canonicalization-owned
observation under the same legal determinants. Representative value, successful or refused outcome, and Contract-owned
attribution cannot vary merely because a legal realization evaluates approved component machinery in another order.
Cross-cutting Budget or Capacity results remain owned by those Contracts and therefore require separate treatment before
an implementation reordering can be certified as legal.

In the absence of an applicable certified property, compiler work may assume only whole-value re-establishment. The
physical algorithm that exploits a certified property remains Design.

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

# 6. Catalog Law Model and Consumer Projections

A ratified Catalog Law needs one authoritative semantic definition. Compiler properties, ratification evidence, outward
stability material, and physical catalog representation may be associated with that law, but they do not become one
undifferentiated Contract record merely because one implementation stores them together.

The compiler must be able to resolve the selected law without reopening a public source type or implementation library.
That requirement does not justify copying every downstream concern into semantic identity.

## 6.1. Authoritative Semantic Law Definition

The authoritative law definition contains only material that can determine Canonicalization meaning.

| Semantic concern                    | Required meaning                                                                                                                           |
|-------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| Exact Catalog Law Reference         | Stable reference to one exact semantic law profile, not to a family, class, API symbol, table row, or ordinal                              |
| Supported Input Presentation Domain | Exact already-established Input presentation meanings for which this law may be selected                                                   |
| Canonicalizable Domain              | Exact subset of admitted legal Input occurrences for which the law can establish its representative                                        |
| Law-Defined Equivalence             | Independent same-meaning relation `E_L` owned by the law                                                                                   |
| Representative Law                  | Exact representative function `C_L` for the successful domain                                                                              |
| Definition Determinants             | Closed law material whose value can change the semantic outcome                                                                            |
| Required Basis Requirements         | Semantic meaning that must be supplied through the applicable ADR-0063 basis relation when the law does not close it inside the definition |
| Owned Refusal                       | Exact Canonicalization-owned negative outcome after legal Input entry, when one exists                                                     |

`Supported Input Presentation Domain` references Input-owned semantic presentation rather than Java or Kotlin carrier
identity, assignability, package membership, or a global host-type category. Selection is therefore a positive
`Catalog Law × exact Input presentation meaning` relation. A missing relation means that the law is not selectable for
that coordinate; a blacklist may explain a diagnostic but cannot define legality by exception. `Canonicalizable Domain`
begins inside the admitted presentation domain and governs actual legal occurrences after selection. Material that Input
never established does not become a Canonicalization refusal.

The Catalog does not need one global `canonicalizable=true` bit for an Input type. Different laws may admit different
presentation meanings over the same broad family, and one host carrier may represent several semantic presentations.
Conversely, a legal Input presentation may have no applicable V1 Canonicalization law and remain legal when
Canonicalization is omitted.

The law may expose a preserved-distinction projection for specification, diagnostics, and verification, but that
projection must agree with `E_L`. It cannot become a second source of equivalence authority.

An exact semantic table, registry snapshot, or other versioned material may be a Definition determinant when the profile
itself fixes that material. A Required Basis requirement is different: it states meaning that must be supplied by a
later legal basis relation. Actual Basis Resolution, Basis Binding, and Applicability remain owned by ADR-0063 and are
not Catalog fields.

## 6.2. Certified Compiler Properties

A compiler may exploit only properties that are sound consequences of the authoritative law. Composition locality,
fragment reuse, fast-path preconditions, or another optimization-relevant fact belongs here when Kontrakt has
established that every legal realization preserving the law may rely on it.

A certified compiler property does not acquire Canonicalization authority. Strengthening the proof or adding a newly
established property may change compiler-product validity without changing the Catalog Law identity when the underlying
Contract meaning is unchanged. Absence of a property requires conservative behavior rather than inference from Catalog
class, implementation shape, or historical success.

The property surface must remain typed and producer-owned. It must not become an open property bag in which a backend
can assign new semantic meaning to an arbitrary flag.

### 6.2.1. Certified Compiler Property Vocabulary

OPEN

## 6.3. Composition Relations and Composite Catalog Laws

V1 may publish a curated Composite Catalog Law when one Input coordinate needs a meaning that combines more than one
already-approved canonicalization concern. The public and HIR selection unit remains one exact Catalog Law. A composite
therefore does not weaken ADR-0066's rule that one selected coordinate resolves to one closed law.

A Composite Catalog Law is independently authoritative. Its semantic identity is not the ordered tuple of component law
identities, even when its reference realization reuses those laws. The composite must pass Section 5 as a whole and must
close its own equivalence, representative, canonicalizable domain, determinants, Required Basis requirements, refusal
behavior, evolution law, and adversarial realization. Component relations are specification and verification material
unless the composite law explicitly makes one such relation part of its own meaning.

The Catalog may also record a relation between approved laws when that relation is useful for deciding whether a
composite profile can be ratified or whether an implementation may legally reorder work. Such a relation is evidence
about exact law interaction; it is not an automatic law constructor and does not grant users arbitrary composition
authority. Pairwise evidence also does not establish an arbitrary larger composite when domain, refusal, Basis, or other
semantic interactions can arise only in the whole combination.

A V1 composition review should concentrate on laws that can interact over the same presentation domain. Laws whose
domains or semantic effects cannot participate in one coordinate do not create a useful permutation space merely because
they coexist in the Catalog. Proven order-independent interaction may eliminate redundant ordered variants, while an
order-sensitive combination requires an exact ratified composite profile for every ordered meaning that Kontrakt chooses
to publish. The Catalog is curated semantic vocabulary rather than the algebraic closure of all primitive laws, so V1
does not generate every subset or permutation merely because it is mechanically expressible.

### 6.3.1. Composition Relation Vocabulary

OPEN

### 6.3.2. V1 Curated Composite Admission Set

OPEN

### 6.3.3. N-Ary Composition Qualification

OPEN

## 6.4. Ratification Assurance

Adversarial analysis and conformance evidence are required for ratification, but they are not themselves the law's
semantic identity. The law owns semantic bounds when crossing a bound changes domain, representative, or refusal.
Security analysis separately records hostile-input work shape, amplification risk, buffering pressure, or other
realization threats that must be tested and contained without changing that meaning.

Normative vectors, generated properties, differential checks, reference realizations, and adversarial corpora provide
evidence that the law and its realizations conform. Evidence may grow as the implementation and threat model improve.
Adding stronger evidence does not create a new semantic profile unless the normative law itself changes.

Assurance material may be packaged with compiler or release tooling, but ordinary semantic consumers do not depend on
the entire assurance corpus merely because they select the law.

## 6.5. External Legal Observation and Compatibility

The legal observations on which independent consumers may rely are a producer-owned projection of the authoritative law.
That projection may expose the exact law reference and the stable semantic obligations needed by an external consumer,
but it cannot contradict or extend the law through a second source of meaning.

Compatibility and migration are relations between exact semantic profiles and exact observation scopes. They may be
directional. They are not intrinsic fields that one Catalog Law can completely define in isolation, because a later
profile or consumer obligation may not exist when the original law is ratified.

### 6.5.1. Compatibility and Migration Relation Schema

OPEN

## 6.6. Consumer and Dependency Boundary

Compiler and external consumers must consume producer-owned projections rather than one monolithic Catalog record. HIR
and Establishment need the exact law meaning and any applicable basis requirements. A consumer of a Composite Catalog
Law depends on that composite semantic observation rather than automatically depending on every physical component
implementation used to realize it. Optimization and incremental reuse may additionally consume certified compiler
properties or composition evidence when their legality relies on those facts. Verification and QA consume assurance
material. External tooling consumes only the legal observation surface that Kontrakt deliberately publishes.

These are logical boundaries, not a requirement for separate runtime objects or files. One physical table may co-locate
several projections, and several tables may realize one projection. Physical co-location does not widen semantic
dependency.

This separation is also the V2 invalidation boundary. A conformance-corpus addition does not invalidate Contract
meaning. A new certified compiler property invalidates only products that depend on that property when their validity
requires reconsideration. A semantic-law change reaches semantic dependents. Catalog order, table layout, sharding, or
another representation change does not become semantic invalidation merely because the same storage carries the law.

# 7. Catalog Classification and Publication Boundary

Catalog classification organizes review and publication. It does not define Canonicalization meaning, security,
complexity, determinism, or optimization legality. Those properties belong to the exact ratified law and to the
producer-owned projections defined in Section 6. Primitive and Composite Catalog Laws pass through the same
classification and qualification boundary; composition provenance is not an authority tier.

The Catalog itself is also not Contract authority. A law is authoritative because its semantic profile has been ratified
under this ADR and the owning Contract architecture. A generated catalog row, numeric id, declaration order, module, or
lookup table merely represents or locates that law.

## 7.1. Orthogonal Classification Rule

One class axis must not carry unrelated claims. Domain scope and normative origin describe different facts and are
treated separately. Neither axis changes the Section 5 qualification gate.

A scope classification describes whether the law belongs to generally reusable presentation semantics or requires a
specialized professional or scientific domain model. A normative-origin classification describes whether the exact
profile is defined directly by Kontrakt or is materially backed by one or more externally published standards whose
remaining choices Kontrakt has closed.

The classification does not delegate authority to an external standard and does not imply that a standard-backed law is
safer, cheaper, or more deterministic. It also does not imply that a general law has weaker Basis, adversarial, or
conformance obligations than a specialized law.

## 7.2. Scope Classification

`Core General` denotes a broadly reusable presentation law that does not require a domain-specific professional model.
It may still consume exact ratified semantic material when that material is part of the law.

`Specialized Domain` denotes a law whose meaning requires a domain-specific model, reference material, or semantic
provisioning that is not appropriate as a general presentation assumption. This changes neither its authority level nor
its qualification standard.

## 7.3. Normative-Origin Classification

`Kontrakt-Defined` denotes a profile whose normative equivalence and representative are closed by the Kontrakt law
itself. External research or standards may inform the decision without becoming the profile's normative source.

`Standard-Backed` denotes a profile for which an exact external standard materially defines the domain, equivalence,
representative, or another semantic obligation. Kontrakt still owns the Contract profile and must close every standard
option, version dependency, and interpretation that can change its legal observation.

## 7.4. Per-Law Classification Assignment

OPEN

## 7.5. Class-Independent Qualification

Every selectable law passes Section 5 in full. Classification is not a trust label and cannot substitute for determinant
closure, adversarial review, conformance, or external-stability analysis.

Compiler consumers likewise cannot derive optimization facts from classification. A backend may use only the exact
semantic law and certified compiler properties it legally observes. `Core General`, `Specialized Domain`,
`Kontrakt-Defined`, or `Standard-Backed` does not by itself prove cost, locality, safety, or reuse legality.

## 7.6. Compilation and Publication Boundary

Expensive ratification belongs to Catalog authoring, release qualification, and platform-support work rather than the
ordinary compilation hot path. This includes composition interaction analysis, order-sensitivity review, conformance,
and any proof used to certify a composite or a legal reordering. Normal compilation resolves the exact selected law and
verifies the positive selection relation against the exact Input presentation meaning at that coordinate. It does not
infer support from carrier type, search a blacklist, search the permutation space, solve composition, or revalidate the
complete Catalog.

The physical Catalog may therefore be generated, partitioned, sharded, lazily materialized, or otherwise reorganized
without changing Contract meaning. Those choices remain Design. Dynamic application registration cannot manufacture a V1
Catalog Law merely by adding a row to that representation.

## 7.7. Incremental and Consumer Boundary

Catalog classification, declaration order, physical module placement, and storage coordinates are not semantic
determinants, semantic identity components, reuse keys, or optimization facts. Adding an unrelated law or reorganizing
the Catalog must not invalidate a Definition that observes only another unchanged law.

A compiler product records dependency only on the exact producer-owned observation it consumes. A semantic consumer may
depend on the law projection. A product that exploits a certified compiler property may additionally depend on that
property. A product that consumes Basis-derived meaning depends on the applicable Basis relation. The implementation may
track these dependencies more finely, but physical storage topology cannot become their semantic source.

## 7.8. Public Support and Packaging Policy

OPEN

# 8. Candidate Universe and Initial V1 Review Group

Before the initial V1 Catalog is narrowed, candidate review starts from a broader semantic universe. Inclusion in this
inventory means only that the profile is worth qualification under Sections 5 through 7. It does not establish V1
support, Catalog classification, applicability to a particular Input presentation, public API availability, or
ratification.

The inventory excludes arbitrary combinations whose meaning would be created by sequencing two or more Catalog laws.
Curated Composite Catalog Laws remain governed by Sections 5.3, 5.7, 6.2.1, 8.13, and 8.14. A standard-defined profile
may still appear below when the standard itself defines one closed semantic profile rather than leaving the composition
order to the user.

Candidates whose primary product is canonical bytes, signature material, or another protocol encoding are retained in a
separate boundary-review group. Their presence does not decide that the 1D Canonicalization Contract is the correct
owner. The same rule applies to domain-specific profiles whose meaning may require a Specialized Domain Catalog or an
exact Required Basis.

The broader candidate universe is:

**General Unicode and text**

```text
text.unicode.nfc
text.unicode.nfd
text.unicode.nfkc
text.unicode.nfkd
text.unicode.casefold-full
text.unicode.casefold-simple
text.ascii.casefold
text.ascii.lowercase
text.ascii.uppercase
text.line-ending.lf
text.line-ending.crlf
text.whitespace.ascii-trim
text.whitespace.ascii-collapse
text.whitespace.xml-replace
text.whitespace.xml-collapse
text.unicode.whitespace-trim
text.unicode.whitespace-collapse
text.unicode.default-ignorable-remove
text.unicode.confusable-skeleton
text.unicode.identifier-skeleton
```

**Identifier and naming profiles**

```text
identifier.bcp47.rfc5646
identifier.uuid.rfc9562-lowercase-text
identifier.uuid.rfc9562-uppercase-text
identifier.uuid.rfc9562-hex-and-dash
identifier.unicode-locale.cldr-canonical
identifier.unicode-locale.cldr-maximal-canonical
identifier.unicode-locale.cldr-minimal-canonical
identifier.time-zone.iana-link-canonical
identifier.time-zone.cldr-canonical
identifier.precis.username-case-mapped
identifier.precis.username-case-preserved
identifier.precis.opaque-string
identifier.precis.nickname
identifier.idna.uts46-nontransitional
identifier.idna.uts46-transitional
identifier.idna2008.a-label
identifier.idna2008.u-label
identifier.python-project.pep503
identifier.python-version.pep440
identifier.package-url.canonical
identifier.purl.pypi
identifier.purl.npm
identifier.purl.maven
identifier.purl.nuget
identifier.purl.golang
identifier.purl.rpm
identifier.purl.deb
identifier.purl.apk
identifier.purl.alpm
identifier.purl.cargo
identifier.purl.gem
identifier.purl.composer
identifier.purl.conan
identifier.purl.cocoapods
identifier.purl.swift
identifier.purl.pub
identifier.purl.hackage
identifier.purl.hex
identifier.purl.cran
```

**Typed lexical value forms**

```text
lexical.xsd.boolean
lexical.xsd.decimal
lexical.xsd.integer
lexical.xsd.non-positive-integer
lexical.xsd.negative-integer
lexical.xsd.long
lexical.xsd.int
lexical.xsd.short
lexical.xsd.byte
lexical.xsd.non-negative-integer
lexical.xsd.unsigned-long
lexical.xsd.unsigned-int
lexical.xsd.unsigned-short
lexical.xsd.unsigned-byte
lexical.xsd.positive-integer
lexical.xsd.float
lexical.xsd.double
lexical.xsd.duration
lexical.xsd.year-month-duration
lexical.xsd.day-time-duration
lexical.xsd.date-time
lexical.xsd.date-time-stamp
lexical.xsd.date
lexical.xsd.time
lexical.xsd.g-year-month
lexical.xsd.g-year
lexical.xsd.g-month-day
lexical.xsd.g-day
lexical.xsd.g-month
lexical.xsd.hex-binary
lexical.xsd.base64-binary
lexical.xsd.normalized-string
lexical.xsd.token
```

`QName` is intentionally absent from this block. Its context-sensitive namespace interpretation is a control case for
the applicability and ownership review rather than evidence that every XSD lexical form has a Canonicalization law.

**Numeric value representations**

```text
number.decimal.numeric-value
number.decimal.fixed-scale
number.decimal.trailing-zero-collapse
number.binary16.canonical-nan
number.binary32.canonical-nan
number.binary64.canonical-nan
number.binary128.canonical-nan
number.rational.reduced-positive-denominator
number.integer.decimal-lexical
number.unsigned-integer.decimal-lexical
```

**Base-N and binary-text presentations**

```text
encoding.base16.rfc4648
encoding.base32.rfc4648
encoding.base32hex.rfc4648
encoding.base64.rfc4648
encoding.base64url.rfc4648
encoding.base64.rfc4648-padded
encoding.base64url.unpadded
encoding.hex.lowercase
encoding.hex.uppercase
```

**URI, IRI, and URL presentations**

```text
uri.rfc3986.percent-hex-uppercase
uri.rfc3986.unreserved-percent-decode
uri.rfc3986.scheme-lowercase
uri.rfc3986.host-lowercase
uri.rfc3986.remove-dot-segments
uri.http.default-port-elide
uri.https.default-port-elide
iri.rfc3987.nfc
iri.percent-hex-uppercase
iri.unreserved-percent-decode
url.whatwg.canonical
url.whatwg-host.canonical
url.whatwg-ipv4.canonical
email.domain.idna
email.domain.ascii-lowercase
```

A generic whole-email-address profile is intentionally absent. Local-part semantics and provider-specific interpretation
prevent the Catalog from treating one convenient normalization rule as universal equality.

**IP, DNS, and network presentations**

```text
network.ipv4.dotted-decimal
network.ipv6.rfc5952
network.ipv6.rfc5952-mixed-ipv4
network.ip-prefix.network-address
network.ipv4-prefix.network-address
network.ipv6-prefix.network-address
dns.name.dnssec-canonical
dns.name.dnssec-canonical-order
network.port.decimal-lexical
```

**HTTP, MIME, and directory-protocol values**

```text
http.field-name.lowercase
http.media-type.canonical
http.media-type.type-subtype-lowercase
http.structured-field.rfc9651
ldap.directory-string.case-ignore
ldap.directory-string.case-exact
ldap.telephone-number.match
ldap.numeric-string.match
```

LDAP matching profiles remain research candidates until each one demonstrates a unique representative rather than only
an equivalence or comparison procedure.

**Temporal presentations**

```text
time.rfc3339.utc-z
time.rfc3339.zero-offset-z
time.rfc3339.fractional-second
time.instant.fixed-precision
time.instant.utc
time.zone-id.iana-canonical
time.zone-id.cldr-canonical
```

**Scientific, professional, and domain-specific profiles**

```text
science.unit.ucum-canonical
science.unit.ucum-semantic-canonical
genomics.ga4gh-vrs.normalize
genomics.ga4gh-vrs.allele-fully-justified
genomics.ga4gh-vrs.reference-length-normalized
genomics.ga4gh-refget.sequence
genomics.ga4gh-refget.sequence-identifier
chemistry.inchi.standard
chemistry.inchi.canonical-labeling
chemistry.smiles.canonical
chemistry.smiles.standard-form
graph.canonical-labeling
telecom.e164.international-number
```

These names do not imply base-Catalog placement. Several require a Specialized Domain classification, an exact external
semantic Basis, or further proof that a stable unique representative exists independently of implementation convention.

**Canonical-byte and protocol boundary review**

```text
serialization.json.jcs.rfc8785
serialization.cbor.rfc8949-core-deterministic
serialization.cbor.rfc8949-length-first-deterministic
serialization.cbor.ctap2-canonical
serialization.ipld.dag-cbor
serialization.ipld.dag-json
serialization.ipld.dag-pb
serialization.avro.schema-parsing-canonical-form
serialization.xml.c14n10
serialization.xml.c14n11
serialization.xml.exclusive-c14n10
serialization.asn1.der
serialization.asn1.cer
graph.rdf.rfdc-1.0
dns.rr.dnssec-canonical-wire
dns.rrset.dnssec-canonical-order
security.jwk.rfc7638-thumbprint-input
mail.dkim.header-simple
mail.dkim.header-relaxed
mail.dkim.body-simple
mail.dkim.body-relaxed
mail.openpgp.text-signature-canonical
mail.smime.text-canonical
security.oauth1.base-string-uri
security.oauth1.parameter-normalization
security.oauth1.signature-base-string
security.http-message-signature.component
security.http-message-signature.base
security.aws-sigv4.uri
security.aws-sigv4.query
security.aws-sigv4.headers
security.aws-sigv4.request
security.google-v4-signing.request
build.nix.derivation-canonical-encoding
```

This block is deliberately not promoted into the general 1D Catalog. Some entries are deterministic encodings of an
already-defined meaning, some establish signature input rather than a same-shape representative, and some may belong to
Publication, protocol realization, or another specialized authority. Section 5 qualification must therefore begin with
ownership rather than with implementation convenience.

The current detailed V1 review tranche is narrower. Its independent candidates are:

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
obligations below. Public Java and Kotlin API names remain owned by API Specification.

The laws in this section are grouped together for V1 review because they address general presentation or value
normalization. This is a document review group rather than a final Catalog classification. Presence here does not mean
ratification, and each law must pass Section 5 before its public API projection becomes stable. A candidate may be
primitive or composite. `text.unicode.nfc-casefold` and `text.unicode.nfkc-casefold` are reviewed as complete composite
profiles rather than as permission for users to sequence their component operations.

Text candidates consume the `Text` presentation already established by ADR-0064, which is a sequence of Unicode scalar
values rather than arbitrary JVM UTF-16 code units. Host material outside that legal Input presentation never enters
Canonicalization. Until a candidate closes its Section 5.3 composition property, whole-value re-establishment is the
only reusable assumption.

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

Ratification must close the same Basis and unassigned-code-point questions as NFC and must state the exact V1 semantic
use for which NFD is published.

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

Its law is semantically clear only after the same Basis and evolution questions are closed. Ratification must also state
the exact V1 semantic use for which this decomposed compatibility representative is published.

## 8.5. Unicode NFC Case Fold

**Semantic law id:** `text.unicode.nfc-casefold`

This candidate is a combined Kontrakt profile intended to collapse Unicode canonical-equivalent and default-caseless
distinctions under one independently specified law. It is not defined merely by composing the existing NFC and case-fold
Catalog entries, and it must not be described as a Unicode-defined profile unless ratification identifies an exact
normative Unicode profile that owns the same relation.

Ratification must close the independent equivalence relation, exact transformation order, fixed-point behavior,
case-folding profile, Unicode Basis, unassigned-code-point behavior, and composition property. Locale-sensitive
lowercasing is not part of the law and cannot substitute for the ratified profile.

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
content, or collapse blank lines. The law is composition-sensitive at fragment boundaries, so ratification must state
the exact Section 5.3 reuse property rather than permit fragment-wise canonicalization by default.

## 8.9. Unicode Boundary Whitespace Trim

**Semantic law id:** `text.unicode.whitespace-trim`

The law erases leading and trailing code points with the Unicode `White_Space` property under the selected Unicode
Basis. Interior whitespace remains unchanged.

The Unicode property set, not Java `trim()` or `strip()`, defines the law. Because property membership can be
version-sensitive, the Unicode Basis is meaning-determining. The law does not collapse internal whitespace or perform
line-ending normalization. Its boundary operation is not assumed to compose over independently canonicalized fragments;
ratification must record the exact Section 5.3 property.

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

0.00
    → coefficient 0 with scale 0
```

The law performs no rounding and does not parse floating-point or textual material into Decimal. It operates only on the
legal finite Decimal presentation established by Input.

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

## 8.13. V1 Composition Interaction Matrix

OPEN

## 8.14. V1 Additional Curated Composite Candidates

OPEN

---

# 9. V1 Protocol and Identifier Candidate Review Group

These candidates are grouped together for review because their current definitions directly name protocol or identifier
standards. This is a document review group rather than a final Catalog classification. They are not ratified until the
Kontrakt profile closes every interpretation and semantic dependency that can change the outcome.

## 9.1. IPv6 RFC 5952 Text

**Semantic law id:** `network.ipv6.rfc5952`

The candidate domain is legal textual IPv6 presentation without an external zone identifier. Alternate legal spellings
are equivalent when they denote the same IPv6 address, and RFC 5952 supplies the target text form.

Ratification must keep Input legality separate from the candidate's canonicalizable domain. Material that is illegal
under the selected Input presentation never reaches this law. If the selected Input is broader legal Text, text that is
not a legal IPv6 presentation lies outside this candidate's canonicalizable domain and is a Canonicalization refusal.
The exact grammar interpretation must therefore be fixed by the profile rather than inherited from a host parser. Zone
identifiers remain outside this profile, and the law performs no DNS lookup.

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

Ratification must confirm that the accepted textual grammar, equivalence relation, and representative are exact. It
changes neither UUID bits nor version or variant meaning. A structured UUID value that no longer contains textual case
does not require this profile.

---

# 10. Deferred General Profiles

The following profiles are plausible Canonicalization laws but are not part of the initial V1 catalog. After this ADR is
Accepted, adding one as a ratified Catalog Law requires a later catalog ADR that preserves the identities and legal
observations already established here.

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

An aggregate law and a Composite Catalog Law are different concerns. Aggregate describes the presentation domain owned
by one coordinate. Composite describes one exact law whose meaning combines more than one canonicalization concern over
its admitted domain. Either may exist without the other, and neither creates child 1D Contracts.

The Catalog does not enumerate every Java or Kotlin generic instantiation as a separate applicability entry. Aggregate
applicability must be stated in semantic presentation terms and may use explicit constituent requirements when the
owning aggregate law makes them part of its qualification. Physical generic shape or assignability cannot manufacture
applicability.

## 16.1. Aggregate Applicability Closure

OPEN

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

The public type is evidence for one Catalog Law, not a runtime strategy object. The same rule applies to a Composite
Catalog Law. API Specification may publish a nominal name for an already-ratified composite meaning, but it cannot
create a new meaning by sequencing primitive API symbols, choosing an implementation order, or inferring a combination
that the Catalog has not ratified.

API Specification may expose a law for a coordinate only when the Catalog's positive selection relation admits that
exact Input presentation meaning. This projection may support discovery or diagnostics, but host type compatibility is
not itself the applicability law and the API cannot make an unsupported pairing legal.

### 17.1. User Composition Surface

OPEN

---

# 18. HIR and Establishment Integration

This ADR does not complete the Canonicalization HIR and Establishment re-audit left open by ADR-0066, but it constrains
that work.

After frontend resolution, a Canonicalization Definition Candidate must refer to each selected Catalog Law by semantic
reference rather than by Java or Kotlin class name. Coordinate bindings remain part of one Canonicalization Definition
Candidate; referenced Catalog Laws do not become child 1D Definitions merely because they have stable identities. A
Composite Catalog Law crosses this boundary as one exact law reference; HIR does not reconstruct its meaning by
expanding a public API name into an implementation pipeline.

If a Catalog Law has a Required Basis requirement, the candidate must preserve that requirement wherever the law needs
it. Definition-determining material that the profile itself closes remains Definition meaning instead. Actual Basis
Resolution, Basis Binding, and Applicability remain owned by ADR-0063.

The re-audit must still decide the exact Canonicalization occurrence unit. This ADR does not choose whether occurrence
is per coordinate, per declaration application, or another semantic unit.

---

# 19. External Semantic Basis

External semantic material belongs to a Catalog Law only when changing that material can change the law's outcome. A
broad platform label is not sufficient when the law actually depends on a particular Unicode dataset, registry state, or
domain reference.

When a profile itself fixes exact versioned semantic material, that material is Definition-determining law meaning. It
is preserved through the exact Catalog Law definition and is not converted into an occurrence-time ADR-0063 Required
Basis merely because the material originated outside Kontrakt.

A law uses ADR-0063 Required Basis only when the Canonicalization judgment genuinely requires semantic material to be
supplied through a later legal basis relation. In that case the Catalog carries the requirement while Basis Resolution,
Basis Binding, and Applicability remain with ADR-0063.

The physical representation of either kind of semantic material remains replaceable. What matters here is determinant
closure, exact dependency, and the consequence of semantic change rather than how the compiler stores the data.

# 20. Versioning, External Consumer Stability, and Catalog Evolution

Section 5.5 owns the semantic stability rule. Applied to the Catalog, it means that adding unrelated laws, changing
review or classification metadata, reorganizing physical catalog material, replacing generated realizations, or changing
host API projection does not change an existing Catalog Law.

An implementation defect is different from a semantic revision. Correcting a realization so that it again conforms to
the already-declared law does not mint a new law identity. Material previously derived from the defective realization
may nevertheless be non-conforming. Any affected persistent key, index, cache, artifact, or other external derivative
must be detected and revalidated, rebuilt, or migrated under the protocol that owns that material rather than being
treated as valid merely because the Catalog Law identity did not change.

When a Basis or standard changes, only material whose legal observation actually depends on that change is reconsidered.
Downstream invalidation may stop after recomputation establishes that the published Canonicalization observation is
unchanged.

Deprecation, support withdrawal, and semantic identity remain separate. Editorial clarification and stronger
verification likewise leave the law identity unchanged when normative meaning remains unchanged.

# 21. V2 Incremental and Reuse Boundary

The Catalog must not become one monolithic dependency. A Definition that selects one law consumes the exact semantic
projection it needs, not every field or artifact stored beside that law. A consumer that additionally uses a certified
compiler property or Basis-derived meaning records those dependencies separately.

V1 therefore preserves exact law references, determinant relations, Required Basis requirements, and producer-owned
compiler properties without collapsing them into physical Catalog identity. A Composite Catalog Law remains one semantic
dependency for consumers that observe only its published meaning; physical reuse of component evaluators does not widen
that dependency. A compiler product that specifically relies on a certified composition or reordering property records
that additional producer-owned dependency separately. V2 may exploit those logical boundaries through a different
incremental architecture without changing Contract meaning.

```text
Canonicalization Definition
    ↓
Law Semantic Projection

optional compiler consumer
    ↓
Certified Compiler Property Projection

when required
    ↓
Basis Requirement / Applicable Basis relation
```

Fragment reuse or local repair is legal only when an applicable certified property establishes it. Otherwise clean
whole-value re-establishment remains the safe path. A change to assurance evidence, Catalog classification, or physical
layout does not become semantic invalidation merely because the implementation co-locates that material.

HID, fingerprints, Merkle structure, dependency graphs, and cache layout remain implementation mechanisms. They may
recognize or accelerate already-defined equality and validity; they do not define either.

# 22. Compiler Product and Protocol Boundary

The Catalog Law semantic definition is the source of Canonicalization meaning even when the compiler realizes it through
generated code, precomputed tables, vectorized routines, or another optimized form. A Composite Catalog Law may
initially use a simple reference realization that invokes reusable component machinery in the law-defined order. A later
backend may fuse, table-drive, vectorize, or otherwise replace that realization without changing the composite law.

Every legal realization must preserve Section 5.1. Target profitability may change the work performed but cannot select
a different Contract representative. An optimization may reorder component work only when the Catalog has certified that
reordering for the complete legal observation; otherwise the semantic order of the composite remains fixed.

Compiler-owned products derived from the law remain subject to the compiler product protocol. A product may consume a
certified compiler property when that property is part of its legality proof, but Catalog classification or physical
representation cannot substitute for that property. Reuse, caching, persistence, and parallel evaluation may change
execution history while preserving the same legal observation.

A compiler-private representation does not become an external Catalog protocol merely because tooling can inspect it. If
Kontrakt later publishes machine-readable catalog metadata for independent consumers, that artifact needs its own
declared stability domain and must project Catalog meaning without exposing private realization as semantic identity.

This separation is what allows aggressive optimization without making one implementation technique part of
Canonicalization authority.

# 23. Security and Adversarial Input

Built-in Canonicalization operates on outside-controlled material, so hostile input is part of catalog admission rather
than a later implementation concern.

Section 5.4 requires each candidate review to identify the hostile-input characteristics that matter to safe
realization. Design and verification may defend the machine against those cases without changing the law. Pathological
structure may justify operational refusal or limits only through the authority that owns that judgment.

Security suspicion does not create equivalence. A visually confusable or otherwise suspicious value remains semantically
distinct unless the selected Catalog Law explicitly declares the distinction irrelevant.

The same interpretation must also survive downstream use. A parser or library cannot re-read the representative under
incompatible semantics and thereby resurrect or invent distinctions after Canonicalization.

Composite laws add an additional differential risk. Intermediate values, component execution order, parser choice, or a
supposedly equivalent alternate pipeline cannot become a second interpretation surface. Security-sensitive order must
therefore be fixed by the Composite Catalog Law itself, while an order-independence claim requires qualification of the
complete legal observation before a backend may exploit it.

# 24. Conformance and QA

Ratification requires evidence for the law, not merely for one implementation.

Normative vectors and generated properties must prove exact equivalence convergence, preservation of non-equivalent
distinctions, representative stability, and any owned refusal boundary. Evolution-sensitive laws must also test the
Basis transitions that the profile claims to support. Stable law identities additionally require regression evidence
that a compiler or catalog rewrite has not changed the published legal observation.

Kontrakt must compare a clean reference realization with materially different legal realizations. Optimized, parallel,
cached, restored, or target-specific paths must expose the same Canonicalization-owned outcome when their semantic
determinants are the same.

Adversarial tests must exercise the difficult input characteristics recorded by the Section 5.4 review. Performance
regressions remain Design and QA concerns, but a performance workaround is invalid if it changes the representative or
semantic domain.

Differential testing against an external implementation is useful evidence. It never replaces the ratified law or its
normative vectors.

Composite qualification additionally requires adversarial order tests over the admitted interaction domain. Where the
Catalog claims order independence, conformance must cover both legal evaluation orders and any optimized fused
realization against the same composite reference meaning. Where order is semantic, tests must prove that the published
ordered profile does not silently accept another ordering as equivalent. Larger composites require whole-profile tests
rather than pairwise evidence alone.

# 25. Current V1 Candidate Summary

This draft does not mark a law `Ratified` before its Section 5 review is complete. `Candidate` is not a terminal status
for an Accepted initial V1 catalog. Ratification occurs by revising this Proposed ADR so that the entry itself contains
the closed semantic profile and the summary records the result; it does not occur through a mutable implementation
registry.

| Current review group  | Semantic law id                          | Current status |
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

The first detailed ratification batch is NFC, NFD, NFKC, NFKD, and NFC Case Fold. Each law must pass the complete
Section 5 gate before it becomes `Ratified`. NFC Case Fold and NFKC Case Fold also exercise the V1 Composite Catalog Law
model and must be qualified as complete laws rather than as sequences of independently selectable primitives.

Before this ADR can become Accepted, every entry that remains designated as an initial V1 candidate must complete that
review and receive an explicit terminal catalog decision in this document. After acceptance, new Catalog Laws are added
by later ADRs rather than by rewriting an existing law identity in place.

The review-group labels in this table are organizational only. Final per-law Catalog classification remains OPEN under
Section 7.4. The deferred profile families in Section 10 and the specialized domains in Section 11 remain outside this
candidate table until their current blocking issue is resolved.

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
`ExactCanonicalization` filler is inserted, and the declaration contains no executable user canonicalizer. A selected
exact law may be primitive or composite; that distinction does not change the authoring relation.

Project documentation should migrate to one active statement of this relation.

---

# 28. Consequences

When the initial V1 catalog has completed ratification and this ADR is Accepted, the V1 frontend gains a finite semantic
target without granting authority to host libraries or implementation algorithms. For each ratified Catalog Law,
resolution can produce one semantic reference that HIR preserves and Establishment can consume.

Determinism becomes an admission property of the law rather than an expectation placed on individual implementations.
This gives the backend more optimization freedom because different legal realizations may change physical work while
remaining observationally identical at the Contract boundary.

The stronger qualification gate keeps unresolved profiles in `Candidate` state until their evidence is complete.
Candidate status preserves the profile under review without turning incomplete Basis, evolution, adversarial, or
outward-stability assumptions into public Contract meaning.

Once a law is ratified, external systems can depend on its declared semantic observations without inheriting Kontrakt's
table layout, generated implementation, or compiler version as part of that dependency. This protects consumers from
silent semantic drift and protects Kontrakt from accidental compatibility debt.

V1 still does not expose arbitrary user-composed normalization pipelines. One coordinate selects one closed law. V1 may
nevertheless publish curated Composite Catalog Laws that have passed the same semantic, security, evolution, and
conformance qualification as primitive laws. This expands the semantic vocabulary without exposing implementation
ordering as user authority. General custom-law support and arbitrary composition remain separate extension problems that
must not turn callbacks into Contract authority.

# 29. Open Work After This ADR

The immediate work is to apply Section 5 to the first Unicode batch and to build the V1 composition interaction matrix
before adding unbounded candidate combinations.

NFC, NFD, NFKC, NFKD, and NFC Case Fold must each be checked against the current Unicode specification and stability
guarantees. The remaining Basis and evolution questions must be closed first. NFC Case Fold must additionally be
reviewed as one complete Composite Catalog Law. The matrix must then identify which approved laws can interact over the
same presentation domain, which interactions are provably order-independent, which are order-sensitive, and which are
unsupported or still unknown. Only useful combinations that survive that review become additional V1 composite
candidates.

The next batch covers NFKC Case Fold, ASCII Case Fold, LF Line Ending, Unicode Boundary Whitespace Trim, and Decimal
Numeric Value. Binary NaN profiles and the protocol / identifier review group follow only after the same gate is
applied.

For project sequencing, ADR-0076 is closed only when the initial V1 candidate set has no unresolved `Candidate` state
and every published entry has a complete Section 5 profile recorded here. After that closure, ADR-0075 is the next ADR
to close. Its compiler-product protocol should then be checked against the concrete Catalog Law, established semantic
material, and realization boundaries produced here.

The OPEN items in Sections 6.2.1, 6.3.1, 6.3.2, 6.3.3, 6.5.1, 7.4, 7.8, 8.13, 8.14, 16.1, and 17.1 must be resolved
before acceptance or explicitly transferred to the owning compiler-product, API, or Design document without weakening
the semantic boundaries fixed here.

The Canonicalization HIR and Establishment re-audit remains necessary after those semantic boundaries are stable. API
Specification follows ratification and must expose only Catalog-approved single-law or composite-law selections; it does
not own arbitrary composition semantics. Design follows the API-independent semantic law and may reuse or fuse component
realizations only when the exact composite observation is preserved. Canonical-byte protocols remain a separate decision
rather than an extension of this coordinate catalog.