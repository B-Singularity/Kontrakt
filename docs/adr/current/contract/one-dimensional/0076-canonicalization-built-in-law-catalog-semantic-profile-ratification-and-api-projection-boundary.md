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
coordinate must resolve to one closed built-in Canonicalization semantic target.

The remaining question is which built-in semantic laws Kontrakt will admit and what exact meaning each target carries.
That decision cannot be left to implementation because a built-in law fixes the equivalence relation, the successful
representative, and the semantic conditions under which that representative is valid. The exact authority stage, Catalog
admission relation, semantic identity, and reference formation are not assumed by this context; Section 6.1.4 keeps them
OPEN. Later judgments and compiler products must nevertheless be able to consume the closed meaning without
reconstructing their own normalization rule.

The word canonicalization is broader outside Kontrakt than the authority defined by ADR-0066. Catalog membership
therefore follows Kontrakt's semantic boundary rather than terminology used elsewhere.

The catalog must cover common representation problems without turning Canonicalization into an executable extension
point. It must also leave a path for specialized domains whose representative depends on an explicit semantic basis.
Other transformations remain separate unless they satisfy the Canonicalization law itself.

A single Input coordinate may also need a meaning that combines more than one familiar canonicalization concern.
Kontrakt does not treat that need as permission to expose an arbitrary normalization pipeline. When a combination is
admitted, it is represented as one complete Composite Catalog Law semantic subject whose equivalence, representative,
ordering semantics, determinant closure, outcome surface, and security-relevant obligations are qualified as one exact
profile. The exact authority stage of that subject remains OPEN with the rest of the Catalog ontology.

This ADR defines that catalog boundary, the initial V1 catalog, and the rules for admitting later profiles. Because a
ratified Catalog Law can become a semantic dependency of another system, it also fixes the outward stability boundary of
that law. Public API projection and implementation Design remain separate.

---

# 2. Problem

Kontrakt must avoid both an under-specified catalog and an over-broad one.

If V1 defines only the authoring mechanism, a selected nominal law has no complete exact semantic target. HIR and
Establishment would then have to recover meaning from implementation material, which would move semantic authority below
the intended Contract boundary.

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
candidate must be reviewed against Section 5 and the still-open Catalog ontology before the ADR can become Accepted. V1
may admit curated Composite Catalog Laws only after the complete combination has passed the same semantic and security
qualification gate. Arbitrary user composition remains outside this ADR. A later built-in addition requires an explicit
semantic Catalog decision rather than an implementation update or mutable registry entry; the exact admission mechanism
remains OPEN under Section 6.1.4.

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

The Catalog must preserve fine-grained semantic granularity so compiler reuse and incremental machinery can avoid
unrelated invalidation. One law or Basis change must not force unrelated Canonicalization meaning to change merely
because the implementation stores the material together. ADR-0075 remains the owner of actual reuse and invalidation
legality.

A ratified Catalog Law is also an outward semantic surface. Another system may persist its law reference, derive its own
indexes or keys from the representative, or otherwise rely on the legal observations that Kontrakt deliberately
publishes. Kontrakt must therefore keep those semantic promises stable without turning incidental catalog layout,
generated API shape, or realization details into compatibility obligations.

Composition must preserve the same boundary. A Composite Catalog Law is not identified by an implementation pipeline, by
a tuple of component API names, or by the order in which helper routines happen to run. Its semantic identity belongs to
the independently ratified composite profile. Component references may support specification, verification, or
realization reuse without becoming a substitute for that identity.

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
    Exact Applicability Relations                     [placement OPEN]
    Exact Law-to-Law Semantic Relations              [vocabulary OPEN]
    Catalog Lifecycle / Current-Selectability Relations [placement OPEN]
```

This structure fixes the decomposition rule, not the final contents of every family. Applicability, lifecycle, exact
identity, reference form, ratification state, and the final boundary between Catalog membership and authoritative law
formation remain open where this ADR marks them `OPEN`.

ADR-0066 owns the common semantic shape of Canonicalization: declared equivalence, representative closure, positive
applicability, canonicalizable-domain and refusal boundaries, determinant closure, and realization determinism. This ADR
owns the exact semantics of built-in laws and the Catalog-specific relations needed to make the built-in vocabulary
closed. ADR-0066 also owns the Canonicalization-side HIR, Establishment, and Catalog-observation Protocol that consumes
those semantics. ADR-0063 and ADR-0071 retain their common identity, Establishment, HIR, Basis, Applicability, and
Protocol laws.

The logical separation in this ADR does not require one object, table, query, allocation, persistent record, or physical
lookup per semantic family. Design may split, fuse, flatten, denormalize, pre-resolve, or co-locate Catalog material
when
the same semantic distinctions and legal observations remain recoverable. Semantic separation therefore does not imply
recursive lookup or pointer-heavy representation.

Conversely, physical fusion cannot merge ownership, identity, equality, applicability, lifecycle, current validity, or
legal observation. A compiler optimization must not turn one implementation layout into the Catalog ontology.

The exact authority stage of a built-in law remains **OPEN**. This ADR does not yet decide whether a complete Exact
Built-In Law Definition itself is the final authoritative semantic unit, whether Catalog admission or ratification forms
a distinct authoritative relation, or how that relation is represented through the common Establishment architecture.
A candidate, source record, generated row, physical Catalog image, or successful lookup does not acquire Contract
authority merely because it exists.

The catalog does not own host-language names or implementation algorithms. API Specification maps public source forms to
exact built-in semantic targets. Design decides how those targets are stored, looked up, checked, and realized.

```text
Built-In Semantic Meaning
    ↓
ADR-0066 Catalog / Canonicalization Protocol
    ↓
Canonicalization Definition Candidate and Establishment

API Specification
    ↓ exact frontend projection
Built-In Semantic Target

Design
    ↓
replaceable physical Catalog and realization
```

A candidate law is not ratified merely because it appears in this ADR draft. It must satisfy the qualification gate in
Section 5 and every still-open Catalog admission question that applies to it.

Source projection, implementation, test vectors, verification evidence, optimization knowledge, and physical lookup
remain distinct from law meaning. None of them may complete missing Contract meaning on the law's behalf.

If Kontrakt deliberately publishes a stable machine-readable Catalog observation to an independent consumer, that
observation must have its own declared stability domain. Publication does not make internal catalog ordering, storage
coordinates, generated evaluator names, cache keys, or other realization artifacts part of built-in law meaning.

# 5. Catalog Law Qualification

Catalog admission is stricter than showing that one normalization routine is useful. A V1 built-in law must close its
semantic meaning first, then survive determinism, applicability, Basis, hostile-input, evolution, and verification
review
without borrowing missing meaning from implementation.

## 5.1. ADR-0066 Determinism Qualification

ADR-0066 owns the universal determinism law for Canonicalization. A built-in Catalog candidate is ratifiable only when
its
exact semantics supply enough closed material to prove that law without relying on ambient or physical state.

The candidate must therefore close every semantic determinant that can change its equivalence classes, representative,
Canonicalization-owned outcome, or another Contract-visible observation. An external semantic source that can change the
outcome must either participate as exact Definition-determining material or be expressed through an exact Required Basis
requirement when later Basis Binding is genuinely part of the law. Host locale, provider state, encounter order, hash
layout, cache history, scheduling, physical catalog revision, and other realization artifacts cannot finish the law.

A referenced standard may leave choices that Kontrakt does not. Any option, tie-break, ordering rule, unknown-member
rule, revision choice, or other semantic branch that can change the legal observation must be closed before
ratification.
The exact representation of those choices must remain typed and law-specific rather than becoming an open property bag.

Budget and Capacity remain separate 1D Contracts. Catalog qualification may record hostile work characteristics and
semantic bounds, but it does not redefine either authority.

## 5.2. Exact Semantic Profile Closure

ADR-0066 requires every Canonicalization law to have one independently declared same-meaning relation `E_L` and one
representative selection `C_L` satisfying the common equivalence, representative, and idempotence laws. ADR-0076 does
not
redefine those meta-laws. It supplies the exact built-in semantics that satisfy them.

The current re-audit also requires every candidate to answer what exact already-established semantic operand the law
observes, what legal values are inside its canonicalizable domain, what Canonicalization-owned outcomes are possible,
which material actually determines those semantics, and which direct semantic references are required to interpret the
law. These questions are part of exact semantic closure even where the final common-field decomposition remains `OPEN`.

The law must not create a second Input presentation ontology. Input owns the legal presentation meaning. An Exact
Built-In Law must instead identify the exact Input-owned semantic presentation over which its `E_L` and `C_L` are
meaningful. Java or Kotlin carrier identity, parser choice, source syntax, runtime object topology, or another host
classification cannot substitute for that semantic operand.

`Canonicalizable Domain` is distinct from the broader legal Input presentation domain. The law must also close the
domain
boundary well enough that unknown, future, unassigned, or otherwise outside material cannot acquire meaning through an
implementation fallback. When a law claims a closed set, complete coverage, or negative-space fact, the closure domain
that makes that claim meaningful must be exact.

A built-in law may observe semantic distinctions beyond the common scalar core. Presence, explicit absence, finite
alternatives, ordering, multiplicity, duplicates, aggregate collision, semantic bounds, unassigned material, or another
domain-specific distinction enters the exact law only when that law actually observes it. V1 does not create one
universal optional-field record containing every such concern.

The law's negative semantics must be closed as exact outcome meaning rather than as a generic `error` field. A legal
Input may establish the representative or may reach a Canonicalization-owned negative condition when the exact law owns
one. Input illegality, an unentered judgment, HIR incompleteness, unavailable backing, Budget or Capacity stop, compiler
resource exhaustion, and implementation failure are not silently converted into that outcome vocabulary.

A Required Basis requirement is conditional, not mandatory Catalog decoration. When one exists, its required semantic
kind, requirement coordinate, cardinality or completeness rule, permitted absence, and relevant definition-time or
occurrence-time boundary must be closed by the law. Actual Basis Resolution, Basis Binding, Applicable Context,
Applicability result, and Complete Basis remain later relations under ADR-0063 and ADR-0066.

The exact placement of built-in selection applicability remains **OPEN**. ADR-0066 owns the positive-applicability
meta-law. This ADR still must decide whether `Exact Law × exact Input presentation meaning → selectable` is intrinsic
law
meaning, a separate Catalog relation, or another exact relation observed through the ADR-0066 Protocol. This question
must be closed before the current `Supported Input Presentation` material is treated as an authoritative field.

An Exact Law Reference is likewise not assumed to be a field inside the law meaning. Exact semantic identity, exact
reference formation, and the relation between Catalog law meaning and the ADR-0063 / ADR-0071 reference architecture
remain **OPEN**. A public API token, package name, dense handle, HID, table row, or ordinal cannot settle that question.

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
for Catalog qualification. The final vocabulary remains `OPEN` in Section 6.3.1.

Such a semantic relation is not by itself a compiler optimization permission. ADR-0075 owns compiler-product legality,
dependency, reuse, invalidation, and optimization-side consumption. A compiler may exploit a Catalog semantic theorem
only when its own producer-consumer law establishes that the theorem is sufficient for the transformation being applied.

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

The review must identify candidate-specific traversal depth, aggregate cardinality, comparison work, sorting or search
shape, buffering, temporary allocation, output expansion, repeated interpretation, collision behavior, and any other
source of attacker-controlled amplification that materially affects safe realization. A difficult worst case does not
automatically disqualify a specialized law, but a nominal API must not conceal the risk.

Semantic bounds remain distinct from compiler resource limits. A bound belongs to the law only when crossing it changes
the law's legal domain, exact outcome, or representative obligation. CPU ceilings, temporary-memory caps, cancellation,
worker limits, or implementation scratch limits do not become Canonicalization meaning merely because the realization
needs protection.

A mitigation may stop work only through the authority that owns that stop. It cannot silently narrow `E_L`, substitute a
different `C_L`, change duplicate or collision semantics, truncate traversal, or alter the successful domain to make one
implementation cheaper.

This section does not set Budget or Capacity values.

## 5.5. Evolution and External Stability Closure

A ratified built-in law must remain stable for the semantic observations its identity actually promises. Another
Kontrakt
release may replace the implementation, reorganize physical catalog material, or change a host-language projection
without assigning different Canonicalization meaning to the same exact law.

A change to equivalence, representative, canonicalizable domain, exact owned outcome, meaning-determining semantic
reference, law-specific distinction, or another actual determinant is a semantic change when it can alter a legal
observation. Such a change requires a distinct semantic profile or an explicit compatibility or migration relation; it
cannot be hidden behind the prior law.

A meaning-determining Basis, registry, or standard revision cannot drift through the host environment. Version pinning
is
required only when the exact semantic material can change the law. A ratified stability guarantee may justify a narrower
dependency. Provider version, library version, standard label, and semantic Basis identity are not interchangeable
merely
because they often change together.

When material can acquire new meaning under a later semantic Basis, the exact law must define how currently unknown,
unassigned, or future material behaves wherever that distinction is observable. An implementation fallback cannot invent
the rule.

Semantic existence, current Catalog membership, current selectability, deprecation, retained physical material, compiler
current-validity, and outward support are distinct states. Their exact Catalog lifecycle relations remain `OPEN` in
Section 6.1 and Section 7.8. Support withdrawal does not retroactively rewrite an already-defined semantic law.

The Catalog must also avoid accidental promises. Stable law meaning does not make enumeration order, internal numeric
identifiers, generated evaluator shape, or other realization details part of the semantic contract.

## 5.6. Evidence Closure

A law is not qualified merely because one implementation passes its own tests. Ratification requires evidence that can
check the exact semantic obligations independently of one realization.

Normative examples, exact expected results, generated properties, negative boundaries, and law-specific conformance
material must cover convergence, preservation of non-equivalent distinctions, representative stability, domain closure,
and any Canonicalization-owned negative semantics that the law declares.

Kontrakt must also retain a clean or independently checkable path against which optimized, parallel, cached, persistent,
or target-specific realizations can be compared when that assurance is required. External implementations are useful for
differential testing. A disagreement is evidence to investigate, not authority to copy.

Verification evidence, adversarial corpora, performance tests, proof artifacts, and implementation coverage do not
become
law identity merely because ratification requires them. Their exact packaging and retention remain Verification, QA, or
Design concerns unless an owning semantic law explicitly says otherwise.

# 6. Catalog Logical Content and Semantic Families

The Catalog is a logical boundary over typed semantic families. It is not a universal semantic record and it is not a
physical lookup topology.

The accepted decomposition rule is:

```text
semantic distinction
    remains explicit

consumer observation
    must be bounded and complete for the declared observation

physical representation
    may split or fuse according to measured compiler needs
```

A semantic family does not imply one separate object, table, query, cache entry, persistent record, allocation, or
physical read. Conversely, one physical record may carry several semantic families without merging them.

## 6.1. Catalog Logical Content and Exact Law Definition Boundary

The Catalog currently admits the following logical family candidates:

```text
Built-In Membership
Exact Built-In Law Definitions
Exact Applicability Relations                     [OPEN]
Exact Law-to-Law Semantic Relations              [partially OPEN]
Catalog Lifecycle / Current-Selectability Relations [OPEN]
```

`Built-In Membership` answers which exact semantic law definitions belong to the built-in selectable population after
the applicable admission law has been satisfied. Membership is not law meaning merely because a compiler stores the two
together.

`Exact Built-In Law Definitions` contain the candidate-specific semantic meaning needed to close the built-in law under
ADR-0066. They do not absorb API naming, assurance evidence, compiler optimization permissions, realization algorithms,
or physical lookup coordinates.

Applicability, law-to-law relations, and lifecycle are separate relation families when they are semantic at all. Their
final ownership and exact relation vocabulary remain open as marked below. This ADR does not create a relation family
merely because tooling would find one convenient.

### 6.1.1. Exact Law Common-Core Shape

**OPEN**

The HIR–Establishment re-audit and the closed Input model show that one Exact Built-In Law must be complete without
becoming a generic property bag. The following are therefore mandatory review questions for every candidate, while the
final common-core decomposition remains open:

| Semantic concern                         | Current boundary                                                                                                                                                                              |
|------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Semantic operand meaning                 | Must identify the exact already-established Input-owned meaning over which the law operates; no second Input ontology                                                                         |
| Canonicalizable Domain                   | Must close the successful semantic domain inside legal Input presentation                                                                                                                     |
| Domain closure / unknown-member behavior | Required whenever the law depends on a closed set, negative space, assigned/unassigned status, or future-member rule                                                                          |
| Law-Defined Equivalence                  | Concrete `E_L` required by ADR-0066                                                                                                                                                           |
| Representative Law                       | Concrete `C_L` required by ADR-0066                                                                                                                                                           |
| Exact Outcome Semantics                  | Must distinguish successful representative from exact Canonicalization-owned negative conditions; non-entry and other-authority stops remain outside                                          |
| Definition Determinant Closure           | Every semantic input that can change the law must be explicit; unrelated context remains outside                                                                                              |
| Direct Meaning-Determining References    | Preserved only when exact external or sibling semantic meaning is actually required                                                                                                           |
| Law-Specific Typed Meaning               | Presence, absence, finite alternatives, ordering, multiplicity, duplicate/collision, semantic bounds, unassigned behavior, or another domain-specific distinction only when actually observed |
| Basis Requirement Law                    | Present only when later Basis Binding is genuinely required; required kind, coordinate, cardinality/completeness, permitted absence, and timing must then be exact                            |
| Supported Input Selection Applicability  | Placement remains OPEN under Section 6.1.3                                                                                                                                                    |
| Exact Law Reference                      | Reference/identity formation remains outside this meaning table and OPEN under Section 6.1.4                                                                                                  |

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

### 6.1.3. Applicability Placement

**OPEN**

ADR-0066 owns the positive-applicability meta-law. This ADR still must decide the exact Catalog-side semantic form of
`built-in law × exact Input presentation meaning → selectable`.

The decision must preserve the distinction between the law's intrinsic semantic operand and canonicalizable domain on
one
side and the legality of selecting that law for one exact Input presentation meaning on the other. It must also avoid
forcing ordinary compilation to infer applicability from host assignability, API package structure, broad family names,
blacklists, or recursive metadata traversal.

### 6.1.4. Law Identity, Reference, Admission, and Authority Stage

**OPEN**

The following are not assumed to be the same thing:

```text
complete Exact Built-In Law meaning
Catalog membership
Catalog admission or ratification judgment
exact semantic law identity
exact semantic law reference
public API symbol
physical lookup key
```

The exact relation among these concepts must be aligned with ADR-0063, ADR-0066, and ADR-0071 before this ADR is
Accepted. The decision must not make source names, package structure, row ordinals, HIDs, fingerprints, dense handles,
module placement, or generated symbols semantic identity by accident.

### 6.1.5. Bounded Semantic Observation and Non-Recursive Lookup

A logical semantic decomposition must not require an ordinary compiler consumer to reconstruct one law by recursively
chasing an unbounded graph of references.

A legal consumer must be able to obtain the complete meaning required for its judgment through a bounded producer-owned
observation. Direct exact semantic references may remain visible where the law actually owns those relations, but their
existence does not permit arbitrary `parent`, `children`, `reachable`, fallback-registry, or transitive graph traversal
as
an authority path.

This is a semantic and Protocol constraint, not a physical-copy requirement. ADR-0066 owns the exact Catalog /
Canonicalization Protocol projection. Design may realize that projection through dense handles, primitive arrays, slabs,
ranges, contiguous immutable images, pre-resolution, shared backing, zero-copy views, or another measured
compiler-native
layout.

The lookup depth required to obtain one declared observation must be bounded by the Protocol schema rather than by
Catalog size, user composition depth, source nesting, or an arbitrary semantic graph. Semantic prerequisite cycles
remain
illegal under the common Establishment law. Ordinary symmetric or typed law-to-law relations are not prerequisite cycles
merely because they form graph cycles.

## 6.2. Catalog-Adjacent Certified Compiler Properties

Compiler optimization knowledge must remain distinguishable from Catalog law meaning. A sound consequence of an exact
law may support quick-check, composition locality, fragment reuse, vectorization preconditions, or another optimization,
but the compiler-side permission to exploit that fact belongs to the compiler-product architecture rather than becoming
an extra Canonicalization authority.

The exact owner and publication form of certified compiler properties remain **OPEN** between ADR-0075, Verification,
and
Design. This ADR retains only the firewall: a compiler property may be derived from exact Catalog semantics, but it does
not enlarge or redefine those semantics, and its absence requires conservative compiler behavior rather than semantic
inference from implementation shape or historical success.

The property surface, if retained, must be typed and producer-owned. It must not become an open property bag in which a
backend can attach new Contract meaning to arbitrary flags.

### 6.2.1. Certified Compiler Property Vocabulary

OPEN

## 6.3. Composition Relations and Composite Catalog Laws

V1 may publish a curated Composite Catalog Law when one Input coordinate needs a complete meaning that combines more
than
one already-approved canonicalization concern. The Catalog-visible selection meaning remains one exact law. A composite
therefore does not weaken ADR-0066's rule that one selected coordinate resolves to one closed law.

A Composite Catalog Law must close its own exact semantic meaning under Section 5 and Section 6.1. Its meaning is not
reconstructed during ordinary compilation by recursively expanding a component tree or replaying an implementation
pipeline. Component relations may remain direct specification, verification, provenance, or realization relations where
needed, but they do not substitute for the complete composite meaning.

The Catalog may record typed semantic relations between exact laws when such relations are needed for qualification or
for another explicitly owned semantic purpose. Those relations do not construct arbitrary new laws and do not grant
users
open composition authority.

A V1 composition review concentrates only on laws that can meaningfully interact over one coordinate. Proven
order-independent interaction may eliminate redundant ordered candidates. Order-sensitive interaction requires an exact
curated profile for each ordered meaning that Kontrakt chooses to publish. V1 does not generate every subset or
permutation merely because it is mechanically expressible.

### 6.3.1. Composition Relation Vocabulary

OPEN

### 6.3.2. V1 Curated Composite Admission Set

OPEN

### 6.3.3. N-Ary Composition Qualification

OPEN

## 6.4. Ratification Assurance

Adversarial analysis and conformance evidence are required for qualification, but they are not themselves the law's
semantic identity. The law owns semantic bounds when crossing a bound changes domain, representative, exact outcome, or
another law-owned observation. Security analysis separately records hostile-input work shape, amplification risk,
buffering pressure, collision pressure, or other realization threats that must be tested and contained without changing
that meaning.

Normative vectors, generated properties, differential checks, reference realizations, adversarial corpora, and
performance suites provide evidence. Evidence may grow as implementations and the threat model improve. Adding stronger
evidence does not create a new semantic profile unless normative meaning changes.

Assurance material may be packaged with compiler or release tooling, but ordinary semantic consumers do not depend on
the entire assurance corpus merely because they select the law.

The exact relation between a qualification judgment, Catalog admission, Catalog membership, and authoritative semantic
formation remains **OPEN** under Section 6.1.4.

## 6.5. External Legal Observation and Compatibility

The legal observations on which independent consumers may rely must be producer-owned projections of exact semantic
meaning. A published projection cannot contradict, extend, or repair the underlying law through a second source of
meaning.

Compatibility and migration are relations between exact semantic profiles and exact observation scopes. They may be
directional. They are not intrinsic fields that one law can completely define in isolation, because a later profile or
consumer obligation may not exist when the original law is introduced.

The exact external machine-readable Catalog surface, if any, is separate from ADR-0066's internal Canonicalization
Protocol. Publication of such a surface is not required merely because the compiler has an internal Catalog.

### 6.5.1. Compatibility and Migration Relation Schema

OPEN

## 6.6. Consumer and Dependency Boundary

The Catalog distinguishes exact law meaning, Catalog relations, compiler-derived properties, ratification assurance,
external-stability material, and physical representation so that no consumer must treat one monolithic record as the
semantic unit.

ADR-0066 owns Canonicalization HIR, Establishment, Established Material, and the legal observation Protocol by which
exact
built-in meaning is consumed. ADR-0075 owns compiler-product dependency, reuse, invalidation, caching, persistence, and
incremental-consumption protocol. This ADR states only the Catalog-side preservation law.

An ordinary semantic consumer must not recover missing meaning from source reopening, API naming, table adjacency,
Catalog classification, assurance packaging, component implementation topology, reverse indexes, global registries, or
fallback discovery. If one downstream judgment needs several Catalog-owned semantic families, ADR-0066 may expose a
bounded complete projection without physically copying or semantically merging those families.

A Composite Catalog Law is observed as one exact complete semantic law. Physical reuse of component evaluators does not
make component topology part of the composite identity or force a recursive observation path.

# 7. Catalog Classification and Publication Boundary

Catalog classification organizes review and publication. It does not define Canonicalization meaning, security,
complexity, determinism, applicability, or compiler optimization legality. Primitive and Composite laws pass through the
same qualification boundary.

A Catalog classification, generated row, numeric id, declaration order, module, package, source file, or lookup table is
not semantic authority merely because it contains or locates a law. The exact authority stage of the complete built-in
law remains open under Section 6.1.4; no classification decision resolves it implicitly.

## 7.1. Orthogonal Classification Rule

One class axis must not carry unrelated claims. Domain scope and normative origin describe different facts and are
separate. Neither axis changes the Section 5 qualification gate.

A scope classification describes whether the law belongs to broadly reusable presentation semantics or requires a
specialized professional or scientific domain model. A normative-origin classification describes whether the exact
profile is primarily defined by Kontrakt or materially depends on externally published semantic specifications whose
remaining choices Kontrakt has closed.

Classification does not imply safety, cost, applicability, reuse legality, or implementation strategy.

## 7.2. Scope Classification

`Core General` denotes a broadly reusable presentation law that does not require a domain-specific professional model.
It may still depend on exact semantic material when that material is genuinely part of the law.

`Specialized Domain` denotes a law whose meaning requires a domain-specific model, reference material, or semantic
provisioning that is not appropriate as a general presentation assumption. This changes neither its semantic obligations
nor its qualification standard.

## 7.3. Normative-Origin Classification

`Kontrakt-Defined` denotes a profile whose exact Canonicalization meaning is closed by the Kontrakt law itself. External
research or standards may inform that decision without becoming an ambient runtime authority.

`Standard-Backed` denotes a profile for which an exact external standard materially participates in the semantic
specification. The exact relation between such normative material, Definition-determining semantic material, Required
Basis, provider implementation, and verification evidence must still be closed per law under Sections 5.2 and 19.

The label does not decide that an external standard or library is itself Contract authority.

## 7.4. Per-Law Classification Assignment

OPEN

## 7.5. Class-Independent Qualification

Every selectable law passes Section 5 in full. Classification is not a trust label and cannot substitute for determinant
closure, domain closure, applicability, adversarial review, conformance, or external-stability analysis.

Compiler consumers cannot derive optimization facts merely from classification. `Core General`, `Specialized Domain`,
`Kontrakt-Defined`, or `Standard-Backed` does not by itself prove cost, locality, safety, compatibility, or reuse
legality.

## 7.6. Compilation and Publication Boundary

Expensive Catalog qualification belongs to Catalog authoring, release qualification, and support work rather than the
ordinary compilation hot path. This includes candidate semantic closure, composition interaction review, hostile-input
analysis, conformance, and any proof required to admit a composite or a semantic relation.

Normal compilation should resolve the exact selected built-in semantic target and perform only the exact legality checks
owned by the relevant semantic law and ADR-0066 Protocol. It must not scan the whole Catalog, search a blacklist, infer
support from carrier type, solve composition, re-run qualification, follow unbounded fallback registries, or recursively
reconstruct a law from component metadata.

The physical Catalog may therefore use generated immutable images, dense typed handles, primitive arrays, ranges,
indexes, hot/cold partitioning, pre-resolution, shared backing, lazy physical materialization, or another measured
compiler-native layout. These are Design choices. They do not make physical lookup topology semantic.

A semantic decomposition must not force one hash lookup, allocation, virtual dispatch, table hop, or physical read per
semantic distinction. Physical co-location and bounded direct projections are explicitly permitted when they preserve
the
same meaning.

Dynamic application registration cannot manufacture a V1 built-in law merely by adding a row to a physical registry.

## 7.7. Incremental and Consumer Boundary

Catalog classification, declaration order, physical module placement, storage coordinates, cache topology, and lookup
layout are not semantic determinants or law identity components.

Adding an unrelated law or reorganizing physical Catalog material must not change another law's meaning. The exact
compiler-product dependency and current-validity rules are owned by ADR-0075. This ADR requires only that those
mechanisms
be able to depend on the exact producer-owned semantic observation actually consumed rather than on incidental Catalog
co-location.

A retained or persisted Catalog representation does not become current or authoritative merely because it can still be
located. Current semantic reference correspondence, determinant validity, membership validity, and coherent observation
must be established by their owning architecture before reuse.

## 7.8. Public Support and Packaging Policy

OPEN

# 8. Candidate Universe and Initial V1 Review Group

Before the initial V1 Catalog is narrowed, candidate review starts from a broader semantic universe. Inclusion in this
inventory means only that the profile is worth qualification under Sections 5 through 7. It does not establish V1
support, Catalog classification, applicability to a particular Input presentation, public API availability, or
ratification.

The inventory excludes arbitrary combinations whose meaning would be created by sequencing two or more Catalog laws.
Curated Composite Catalog Laws remain governed by Sections 5.3, 6.3, 8.13, and 8.14. A standard-defined profile may
still appear below when the standard itself defines one closed semantic profile rather than leaving the composition
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
Canonicalization. Until a candidate closes any required Section 5.3 semantic composition relation, this ADR supplies no
finer-grained semantic theorem that compiler reuse machinery may assume. Actual reuse legality remains owned by
ADR-0075.

The dotted spellings below are candidate review labels. They do not establish semantic identity, exact reference
encoding, package structure, or public Java/Kotlin names. Public source naming belongs to API Specification, while the
exact semantic identity/reference law remains OPEN under Section 6.1.4.

## 8.1. Unicode NFC

**Candidate semantic label:** `text.unicode.nfc`

The presentation domain is Unicode text admitted by the selected Input surface.

The candidate selects Unicode Normalization Form C as the representative for canonical equivalence. Compatibility
distinctions remain observable.

Ratification must close the exact Unicode Basis dependency rather than inherit host Unicode tables. Unicode
normalization stability may allow part of the profile to remain stable across later versions, while unassigned code
points still require an explicit evolution policy. That distinction must be settled before this candidate becomes
ratified.

Canonical bytes are not part of this candidate law.

## 8.2. Unicode NFD

**Candidate semantic label:** `text.unicode.nfd`

The candidate uses the same canonical-equivalence domain as NFC and selects Unicode Normalization Form D as the
decomposed representative.

Ratification must close the same Basis and unassigned-code-point questions as NFC and must state the exact V1 semantic
use for which NFD is published.

## 8.3. Unicode NFKC

**Candidate semantic label:** `text.unicode.nfkc`

The candidate applies Unicode compatibility decomposition followed by canonical composition. Selecting it therefore
declares compatibility distinctions irrelevant in addition to canonical distinctions.

NFKC is not an implementation substitute for NFC. Ratification must close its exact Unicode Basis, evolution behavior,
and intended presentation domain before this stronger equivalence becomes a base V1 law.

## 8.4. Unicode NFKD

**Candidate semantic label:** `text.unicode.nfkd`

The candidate erases the same compatibility distinctions as NFKC and selects the compatibility-decomposed
representation.

Its law is semantically clear only after the same Basis and evolution questions are closed. Ratification must also state
the exact V1 semantic use for which this decomposed compatibility representative is published.

## 8.5. Unicode NFC Case Fold

**Candidate semantic label:** `text.unicode.nfc-casefold`

This candidate is a combined Kontrakt profile intended to collapse Unicode canonical-equivalent and default-caseless
distinctions under one independently specified law. It is not defined merely by composing the existing NFC and case-fold
Catalog entries, and it must not be described as a Unicode-defined profile unless ratification identifies an exact
normative Unicode profile that owns the same relation.

Ratification must close the independent equivalence relation, exact transformation order, fixed-point behavior,
case-folding profile, Unicode Basis, unassigned-code-point behavior, and composition property. Locale-sensitive
lowercasing is not part of the law and cannot substitute for the ratified profile.

## 8.6. Unicode NFKC Case Fold

**Candidate semantic label:** `text.unicode.nfkc-casefold`

This candidate targets Unicode `NFKC_Casefold` semantics for an identifier-like domain in which compatibility and
caseless distinctions are intentionally erased.

Ratification must use the exact Unicode profile rather than an arbitrary sequence of host normalization and lowercasing
calls. Its Basis, treatment of default-ignorable material, evolution behavior, and composition properties remain part of
the qualification review.

## 8.7. ASCII Case Fold

**Candidate semantic label:** `text.ascii.casefold`

The presentation domain is text for which ASCII letter case is the only distinction this law may erase. `A` through `Z`
map to their lowercase ASCII representatives; every other code point is preserved.

The law has no locale behavior and requires no external semantic Basis.

## 8.8. LF Line Ending

**Candidate semantic label:** `text.line-ending.lf`

The law treats the supported line-ending spellings as equivalent and selects LF as their representative. CRLF and
standalone CR therefore converge to LF without changing an existing LF.

Other Unicode line separators and other whitespace are preserved. The transformation does not expand the source, trim
content, or collapse blank lines. The law is composition-sensitive at fragment boundaries, so qualification must state
the exact Section 5.3 semantic composition relation before any compiler product may infer fragment-wise legality. Actual
fragment reuse remains owned by ADR-0075.

## 8.9. Unicode Boundary Whitespace Trim

**Candidate semantic label:** `text.unicode.whitespace-trim`

The law erases leading and trailing code points with the Unicode `White_Space` property under the selected Unicode
Basis. Interior whitespace remains unchanged.

The Unicode property set, not Java `trim()` or `strip()`, defines the law. Because property membership can be
version-sensitive, the Unicode Basis is meaning-determining. The law does not collapse internal whitespace or perform
line-ending normalization. Its boundary operation is not assumed to compose over independently canonicalized fragments;
ratification must record the exact Section 5.3 property.

## 8.10. Decimal Numeric Value

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

## 8.11. Binary32 Canonical NaN

**Candidate semantic label:** `number.binary32.canonical-nan`

The presentation domain is IEEE 754 binary32 when raw NaN representation remains observable at the Input boundary. The
candidate maps every NaN bit pattern to one quiet-NaN representative while preserving every non-NaN bit pattern,
including signed zero.

The current candidate representative is `0x7fc00000`. Ratification must confirm that this exact bit-level observation
survives every supported Input and backend path before the bit pattern becomes normative law material.

## 8.12. Binary64 Canonical NaN

**Candidate semantic label:** `number.binary64.canonical-nan`

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

**Candidate semantic label:** `network.ipv6.rfc5952`

The candidate domain is legal textual IPv6 presentation without an external zone identifier. Alternate legal spellings
are equivalent when they denote the same IPv6 address, and RFC 5952 supplies the target text form.

Ratification must keep Input legality separate from the candidate's canonicalizable domain. Material that is illegal
under the selected Input presentation never reaches this law. If the selected Input is broader legal Text, text that is
not a legal IPv6 presentation lies outside this candidate's canonicalizable domain and is a Canonicalization refusal.
The exact grammar interpretation must therefore be fixed by the profile rather than inherited from a host parser. Zone
identifiers remain outside this profile, and the law performs no DNS lookup.

## 9.2. BCP 47 Language Tag

**Candidate semantic label:** `identifier.bcp47.rfc5646`

The candidate domain is a well-formed BCP 47 language tag under the selected Kontrakt profile. RFC 5646 supplies
canonicalization rules, while registry data becomes meaning-determining where the representative consumes it.

Ratification must close the exact registry Basis and its evolution consequences. The law cannot read a mutable current
registry as authority, and unsupported extension semantics cannot receive an implementation-defined representative.

## 9.3. UUID Lowercase Text

**Candidate semantic label:** `identifier.uuid.rfc9562-lowercase-text`

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

Stabilized NFC and NFKC remain deferred until their exact refusal relation and semantic-Basis requirements are closed at
the Catalog level. ADR-0066 owns how those closed requirements are represented and established downstream.

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

Catalog semantics and host API projection are separate stability domains.

```text
public nominal source form
    ↓ exact frontend mapping
exact built-in semantic target
    ↓
Canonicalization Definition Candidate meaning
```

This diagram does not decide the final exact-reference encoding or identity law. Section 6.1.4 leaves that relation
open.
The API may expose names such as `UnicodeNfc` or `DecimalNumericValue`, but those names, packages, classes, or generated
symbols do not define built-in semantic identity.

An API rename may change source compatibility without changing semantic meaning. Conversely, a stable source name cannot
hide a changed law. Public API compatibility, built-in semantic compatibility, Catalog lifecycle, and physical lookup
compatibility remain separate domains.

API Specification must preserve the authoring law already fixed by ADR-0066. The IDL selects one inert
Canonicalization declaration rather than selecting a built-in law directly. The declaration names only the Input
coordinates on which Canonicalization applies. Each named coordinate resolves to one exact built-in semantic target. An
unnamed coordinate remains outside Canonicalization. The declaration contains no executable application canonicalizer.

A public nominal type is authoring evidence for an existing semantic target, not a runtime strategy object and not a
Catalog membership constructor. The same rule applies to a curated Composite Catalog Law. API Specification may project
an already-admitted composite meaning, but it cannot create a new meaning by sequencing primitive symbols, selecting an
implementation order, or inferring a combination that the Catalog has not admitted.

API Specification also does not own applicability. It may expose or diagnose only the legal selection relation
established
by the owning semantic architecture. The exact Catalog-side placement of that relation remains `OPEN` under Section
6.1.3. Host assignability, package grouping, class hierarchy, overload selection, or IDE discoverability cannot make an
unsupported pairing legal.

User-visible capability remains a completeness test for this ADR. A Catalog design that cannot support exact selection,
compile-time legality, precise diagnostics, stable migration, or an admitted composite without reopening implementation
is incomplete. Those requirements do not, however, permit API syntax to define the Catalog ontology.

### 17.1. User Composition Surface

OPEN

---

# 18. Canonicalization Contract Integration Boundary

Canonicalization-specific HIR, Definition Candidate, Binding Candidate, Establishment, occurrence, Established Material,
and Established Semantic Protocol semantics are owned by ADR-0066 together with ADR-0071, ADR-0063, and the 1D master
checklist. This Catalog ADR does not define their final schema.

The downstream re-audit must preserve the same decomposition discipline already applied to Input. A Canonicalization
Definition is not the same semantic subject as one built-in law. An IDL Binding Candidate is not the law definition.
Actual Basis Binding, Applicability, occurrence meaning, and Established Material are not Catalog membership merely
because one compiler may store related handles together.

The current downstream shape is therefore constrained, but not yet fully decided:

```text
complete built-in semantic meaning
        ↓ exact legal observation
Canonicalization Definition Candidate
        + sparse direct Input-coordinate selections
        ↓ Canonicalization-owned Definition judgment
Established Canonicalization Definition
        ↓ fresh application, if occurrence meaning exists
Canonicalization occurrence judgment                [exact occurrence unit OPEN]
        ↓
Established occurrence / representative relation    [shape OPEN]
```

The exact Candidate and Established surfaces remain **OPEN**. The dedicated ADR-0066 re-audit must answer at least:

```text
complete Canonicalization Definition Candidate meaning
exact sparse coordinate-to-law relation
exact IDL Binding Candidate meaning
Definition determinants and non-determinants
Definition-time or occurrence-time Basis Requirement, if any
whether Canonicalization owns an independent Applicability judgment
minimum complete Established Definition unit
whether one fresh application has independent Occurrence meaning
exact success and negative occurrence relations
Established Definition and Occurrence reference domains
Direct Established Relation projections
Fine-Grained Established projections
coherent snapshot and current-validity law
```

The Catalog imposes several preservation constraints on that later work. A selected law must cross downstream semantic
boundaries as the exact law meaning defined here, not as a Java or Kotlin type, Catalog row coordinate, generated
evaluator,
implementation pipeline, or recursively expanded component graph. A Composite Catalog Law likewise crosses as one exact
complete law.

The HIR Candidate must contain enough resolved Canonicalization meaning for Establishment to judge the Definition
without
reopening authored source, host topology, private Catalog storage, implementation code, or an ambient registry. Where a
built-in law owns an exact direct semantic reference, that relation may remain explicit. It must not force arbitrary
transitive reference chasing to recover meaning that the producer failed to resolve.

Definition-determining semantic material and Required Basis requirements must remain distinguishable. HIR may preserve a
Basis Requirement Law when Definition meaning owns one, but it must not pre-establish actual Basis Binding,
Applicability, Complete Basis, or a future occurrence result merely for implementation convenience.

Established Protocol projections must remain local. A projection may expose the exact complete local meaning and direct
relations that its declared observation requires. It must not recursively embed the entire transitive semantic world.
Derived reachability, compatibility analysis, conflict analysis, optimization knowledge, and reverse indexes remain
separately owned products unless another Contract law explicitly establishes them.

Logical Protocol crossing does not require a copy, wrapper allocation, object graph, or one physical read per semantic
relation. Shared immutable backing, primitive arrays, typed slabs, dense handles, contiguous ranges, pre-resolution, and
zero-copy observation remain available to Design when the same legal observations are preserved.

# 19. External Semantic Basis

`external dependency` is not one semantic category. This ADR keeps at least the following distinctions separate:

```text
external normative specification or profile
Definition-determining semantic material
Required Basis Requirement
actual Basis Binding
provider / implementation library
conformance or assurance evidence
```

A standard title, provider name, library version, or platform version does not become a semantic Basis merely because it
is convenient to record.

When a built-in law itself fixes exact external semantic material and changing that material can change its meaning,
that
material participates in Definition determinant closure. It is not converted into an occurrence-time Required Basis
merely because the material originated outside Kontrakt.

A law uses the common Required Basis machinery only when its meaning genuinely requires a later exact Basis relation. In
that case the law must close the requirement's semantic kind, coordinate, cardinality or completeness, permitted
absence,
and the semantic boundary at which the exact requirement must be satisfied. Actual Basis Resolution, Basis Binding,
Applicable Context, Applicability result, and Complete Basis remain owned by ADR-0063 and ADR-0066.

The exact classification of external material is **OPEN per built-in law**. Unicode, registry-backed identifiers,
temporal data, scientific reference data, and other standards must not be forced into one universal external-Basis model
before their actual semantic dependency is established.

Semantic prerequisite relations must remain acyclic. A built-in law cannot complete its own meaning through a cycle of
unresolved external requirements or through fixed-point discovery. Compiler query recursion or implementation sharing is
not semantic dependency merely because it follows a cyclic call or cache graph.

The physical representation of external semantic material remains replaceable. What matters here is exact determinant
closure, direct dependency meaning, compatibility, and change consequences rather than how a compiler stores or obtains
bytes.

# 20. Versioning, Lifecycle, External Consumer Stability, and Catalog Evolution

Section 5.5 owns the semantic stability rule. Catalog evolution must additionally keep several states distinct:

```text
semantic law exists
law is a current Catalog member
law is currently selectable
law is deprecated or superseded
physical Catalog material is retained
compiler representation is current-valid
public API projection is supported
an external machine-readable projection is supported
```

The final Catalog lifecycle relation family and its identity consequences remain **OPEN**. None of these states is
allowed
to rewrite exact law meaning merely because support policy changed.

Adding unrelated laws, changing review or classification metadata, reorganizing physical catalog material, replacing a
generated realization, or renaming a public source projection does not by itself change another law's semantic meaning.
Whether a membership or support change requires a new Catalog-level relation is decided by the later lifecycle closure,
not by physical storage churn.

An implementation defect is different from a semantic revision. Correcting a realization so that it again conforms to an
unchanged law does not mint a new law meaning. Material derived through the defective realization may nevertheless be
non-conforming and must be revalidated, rebuilt, or migrated under the protocol that owns that material.

When an exact semantic Basis changes, only consumers whose legal observation actually depends on the changed meaning are
reconsidered. Invalidation may stop after legal recomputation establishes the same published observation. Provider or
standard version labels alone are insufficient evidence of semantic incompatibility or compatibility.

Deprecation, support withdrawal, semantic identity, semantic compatibility, and migration remain separate questions.
Editorial clarification and stronger verification likewise leave meaning unchanged when no normative semantic
obligation changes.

# 21. Incremental, Reuse, and Physical Granularity Boundary

ADR-0075 owns compiler-product dependency, reuse, invalidation, persistence, and incremental protocol. ADR-0076 does not
choose red-green, Merkle, query-graph, delta, DBSP, or another incremental algorithm.

This ADR contributes only semantic granularity constraints. One law or relation change must not force unrelated built-in
meaning to change merely because the implementation stores the material together. Conversely, semantic distinction does
not require one cache entry, query, object, persistent record, table, or invalidation node per distinction.

A compiler product should be able to depend on the producer-owned semantic observation it actually consumes. If a later
compiler product also relies on a certified compiler property, Basis-derived fact, compatibility relation, or lifecycle
fact, that additional dependency remains separately owned. The exact recording mechanism belongs to ADR-0075 and Design.

Retained or persisted Catalog bytes do not become current meaning merely because they decode successfully. Current
semantic reference correspondence, determinant validity, membership validity where applicable, required relation
validity, and coherent observation must be re-established by the owning architecture before reuse.

Lazy physical materialization is allowed only after the semantic observation it represents is already complete and
current-valid. Lazy semantic completion, fallback reconstruction, or on-demand traversal into source or provider state
is
not a reuse optimization.

HID, fingerprints, Merkle structure, dependency traces, cache layout, and physical co-location may accelerate validity
checks. They do not define semantic equality, Catalog identity, membership, or authority.

# 22. Catalog Protocol and Compiler Product Boundary

The exact **Catalog Protocol consumed by Canonicalization belongs to ADR-0066**, aligned with ADR-0071 and ADR-0063.
This
ADR defines the semantic material that such a Protocol may need to observe and the constraints that the Protocol must
preserve; it does not freeze the final projection catalog or physical access API.

The Protocol must not be a generic `getFullCatalogRow()` surface from which every consumer privately interprets fields.
It must also not be so fragmented that one semantic judgment requires an unbounded chain of `LawRef → DomainRef →
ProfileRef → BasisRef → ...` lookups or a return to global registries.

The target is a producer-owned bounded observation. A consumer must be able to obtain the exact complete meaning
required
for its judgment through a fixed semantic surface whose depth is bounded by Protocol structure rather than by Catalog
size, source nesting, composite depth, or data-dependent graph traversal.

The exact projections remain **OPEN in ADR-0066**. The later design must consider at least the following observation
needs without assuming one projection per physical read:

```text
exact built-in semantic target observation
exact complete law-meaning observation
selection-applicability observation, if separately owned
Basis Requirement observation, when present
exact direct law-relation observation, when legally needed
```

Fine-grained projections may exist for consumers that need only part of an already-complete producer-owned meaning. A
fine-grained projection may narrow observation but cannot redefine completeness or semantic equality, reopen source, or
synthesize missing meaning.

Physical implementation may answer a logical projection from one fused immutable image, several SoA ranges, generated
tables, shared backing, or another representation. One semantic projection is not one method, one object, one query, one
hash lookup, one cache entry, or one copy boundary.

Physical addresses, dense handles, ordinals, HIDs, fingerprints, storage keys, reverse indexes, and debug lookup
namespaces are not semantic references by themselves. Production semantic consumers must not gain broader authority
merely because a global debug or introspection index can find more material.

Compiler-derived optimization properties remain subject to ADR-0075. A Catalog semantic theorem may be input to a
compiler legality proof, but the theorem and the compiler permission are not the same semantic product.

An external machine-readable Catalog artifact, if Kontrakt later publishes one, is a separate outward stability surface.
It must not silently turn the internal ADR-0066 Protocol or physical Catalog schema into an external ABI.

# 23. Security and Adversarial Input

Built-in Canonicalization operates on outside-controlled material, so hostile input is part of Catalog qualification.
Security review must protect exact Contract meaning rather than simplify the law until one realization becomes easy.

The Catalog and ADR-0066 Protocol must specifically defend against interpretation drift. Input establishes one legal
presentation meaning. Canonicalization cannot validate one interpretation and later canonicalize another parser's view
of
the same source. A law whose security meaning depends on a particular semantic interpretation must close that
interpretation before Catalog qualification.

Incomplete semantic material is also dangerous. A known label, partial candidate, placeholder, unsupported required
extension, stale relation, unavailable backing, or physical row must not become a selectable law merely because lookup
succeeds. Ordinary consumers must fail closed or remain unentered according to the owning boundary rather than fabricate
missing semantic meaning.

Reference confusion is prohibited. Law meaning, Catalog membership, API symbol, dense handle, table row, HID, Basis
Binding, Definition Reference, and occurrence reference must not become interchangeable because one primitive encoding
or
lookup service can address them all. Stale generation-local handles and wrong-kind references must not silently resolve
to current semantic material.

Ambient authority is prohibited. Host locale, current provider tables, thread-local registries, service locators,
classpath order, first-match search, nearest-match resolution, fallback discovery, cache state, or a global registry may
not complete law meaning or select a semantic relation unless an owning law explicitly makes the relevant exact value a
semantic input.

Recursive semantic lookup is both an authority and resource risk. A malicious or pathological Catalog relation must not
force ordinary compilation into unbounded reference chasing, recursive object-graph reconstruction, fixed-point
resolution, or data-dependent fallback chains. Semantic prerequisite cycles are rejected. Physical traversal must be
bounded and iterative where implementation safety requires it without changing semantic meaning.

Aggregate laws require additional review. Canonicalization-induced key or element collisions, duplicate behavior,
ordering, uniqueness, and element closure cannot fall through to first-wins, last-wins, insertion order, hash iteration,
or another implementation convention. If an aggregate law observes such distinctions, its exact typed semantics must
own them.

Hostile size, depth, cardinality, comparison patterns, hash-flooding, expansion, malformed persistent material,
corrupted reference tags, and cyclic physical carriers require bounded acquisition and adversarial QA. Semantic bounds
remain distinct from compiler resource protections unless crossing the bound itself changes Contract meaning.

Composite laws add differential risk. Intermediate values, component execution order, parser choice, component
realization, or a supposedly equivalent alternate pipeline cannot become a second interpretation surface. Security-
sensitive order belongs to the exact composite meaning. An order-independence claim requires qualification of the
complete legal observation before any compiler transformation may exploit it.

# 24. Conformance and QA

Qualification requires evidence for exact semantic law, not merely for one implementation. Verification must remain
traceable from the owning ADR clause through a conformance property to a test oracle or independent reference path.

Normative vectors and generated properties must cover exact equivalence convergence, preservation of non-equivalent
distinctions, representative stability, canonicalizable-domain boundaries, unknown or unassigned behavior where
relevant, law-specific ordering or collision semantics, and every Canonicalization-owned negative outcome.

Basis-sensitive laws must test the exact semantic changes and compatibility claims they publish. A provider version bump
or a different implementation is not an adequate oracle by itself.

Kontrakt must compare materially different legal realization paths where that assurance matters. Clean formation,
optimized realization, parallel evaluation, validated cache reuse, persistent reload, future incremental repair, and
target-specific implementation must expose the same legal semantic observation when the determinants are the same.

Adversarial suites must exercise the work-shape risks recorded by Section 5.4 and Section 23. Performance regressions
remain Design and QA concerns, but a performance workaround is invalid when it changes `E_L`, `C_L`, domain, exact
outcome, ordering, collision, or another semantic obligation.

Differential testing against an external implementation is useful evidence. It never replaces exact built-in semantics.
A conformance corpus, fuzz corpus, proof artifact, benchmark result, or security review may evolve without changing law
meaning when the normative obligations remain unchanged.

Composite qualification requires whole-profile tests. Pairwise evidence is insufficient when N-ary domain, Basis,
refusal, ordering, or interaction effects exist only in the complete composite.

# 25. Current V1 Candidate Summary

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

The exact transition from Candidate to admitted built-in semantic meaning remains tied to the OPEN admission,
ratification, membership, identity, and authority questions in Section 6.1.4. Appearing in this table is not enough to
create authority or public support.

The first detailed semantic review batch remains NFC, NFD, NFKC, NFKD, and NFC Case Fold. NFC Case Fold and NFKC Case
Fold
also exercise the curated Composite Catalog Law model and must be reviewed as complete laws rather than as user-visible
sequences of primitives.

Before this ADR can become Accepted, every entry retained in the initial V1 set must receive an explicit terminal
Catalog
decision under the final admission model. That decision must not be inferred from implementation registration or from a
public API type existing in source.

The review-group labels in this table are organizational only. Final per-law Catalog classification remains OPEN under
Section 7.4. Deferred profile families in Section 10 and specialized domains in Section 11 remain outside this candidate
table until their current blocking issues are resolved.

# 26. Decisions Against Earlier Candidate Material

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

# 27. Migration and Supersession

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

This diagram does not freeze the exact semantic reference encoding or Catalog authority stage. Those remain OPEN under
Section 6.1.4 and ADR-0066's HIR/Establishment re-audit.

An unselected coordinate remains outside Canonicalization. The IDL does not select a built-in law directly, no
`ExactCanonicalization` filler is inserted, and the declaration contains no executable user canonicalizer. A selected
exact law may be primitive or composite; that distinction does not change the authoring relation.

Project documentation should migrate to one active statement of this relation.

ADR-0066 retains the common equivalence/representative meta-law, positive-applicability rule, realization determinism,
Canonicalization authoring law, and Canonicalization-specific HIR/Establishment/Protocol ownership. ADR-0076 retains the
exact built-in semantic definitions and whatever Catalog membership, applicability, law-to-law, lifecycle,
qualification,
and evolution relations are finally closed here. The final placement of several of those families remains OPEN in
Section

6.

# 28. Consequences

The accepted Catalog decomposition prevents the project from collapsing all built-in information into one mega-entry or
from reproducing the same semantics as a recursive reference graph. Exact law meaning, membership, applicability,
lifecycle, compiler-derived properties, assurance, API projection, and physical realization can remain logically
distinct
without forcing distinct objects, tables, cache entries, or lookup hops.

This preserves compiler performance freedom. Hot-path material may be flattened, co-located, pre-resolved, indexed, or
served from shared immutable backing. Cold assurance, lifecycle, classification, documentation, or other adjacent
material may be kept elsewhere. Those choices do not change semantic ownership.

It also narrows the security surface. A successful lookup is not authority. A public name is not identity. A provider is
not semantic Basis merely because it supplies data. A partial or stale law cannot become selectable through fallback
lookup. An ordinary consumer does not gain permission to walk the Catalog graph until it reconstructs whatever meaning
it
wants.

The exact built-in law shape is now more explicit without becoming a universal property bag. Common Canonicalization
obligations remain common, while presence, alternatives, ordering, multiplicity, collision, semantic bounds, unknown
member behavior, and other specialized distinctions enter a law only when that law actually observes them.

V1 still does not expose arbitrary user-composed normalization pipelines. One selected coordinate resolves to one closed
built-in semantic law. Curated Composite Catalog Laws remain possible after whole-profile qualification. General custom
law support and arbitrary composition remain separate future design problems.

Several architectural details deliberately remain open. In particular, this revision does not settle Catalog authority
formation, semantic identity/reference encoding, applicability placement, lifecycle relations, the final Exact Law
common
core, the certified compiler-property owner, or the Canonicalization HIR/Established Protocol shapes. Leaving those
questions explicit is preferable to freezing them indirectly through a table schema or API name.

# 29. Open Work After This ADR

The next work is no longer to ratify Unicode candidates immediately. The Catalog semantic model must first close the
remaining ontology and handoff questions exposed by the HIR–Establishment audit.

The first decision batch is:

```text
1. Exact Law Common-Core Shape
   Section 6.1.1

2. Typed Law-Specific Meaning
   Section 6.1.2

3. Applicability Placement
   Section 6.1.3

4. Law Identity / Reference / Admission / Authority Stage
   Section 6.1.4

5. ADR-0066 Bounded Catalog / Canonicalization Protocol
   exact projection catalog and HIR / Establishment observation boundary
```

The external semantic material split in Section 19 must then be applied candidate by candidate. Each law must
distinguish
Definition-determining semantic material, an actual Required Basis Requirement when one exists, provider or
implementation
identity, and conformance evidence. These categories must not be collapsed into one `externalDependency` or version
field.

After that, the Catalog relation questions can be closed: composition-relation vocabulary, curated composite admission,
N-ary qualification, compatibility/migration relations, lifecycle/current-selectability relations, classification,
public-support policy, aggregate applicability, and the user composition surface.

Only after those structural questions are closed should the detailed candidate qualification resume. NFC, NFD, NFKC,
NFKD, and NFC Case Fold remain the first semantic review batch. Their Unicode semantic material, stability guarantees,
unknown or unassigned behavior, exact domain closure, and composite interactions must be checked against the final
Catalog model rather than forcing the model to fit one candidate.

The following ADR-0076 items are explicitly OPEN in this revision:

```text
6.1.1  Exact Law Common-Core Shape
6.1.2  Typed Law-Specific Meaning
6.1.3  Applicability Placement
6.1.4  Law Identity / Reference / Admission / Authority Stage
6.2     exact owner/publication form of Catalog-adjacent compiler properties
6.2.1   Certified Compiler Property Vocabulary
6.3.1   Composition Relation Vocabulary
6.3.2   V1 Curated Composite Admission Set
6.3.3   N-Ary Composition Qualification
6.5.1   Compatibility and Migration Relation Schema
7.4     Per-Law Classification Assignment
7.8     Public Support and Packaging Policy
8.13    V1 Composition Interaction Matrix
8.14    V1 Additional Curated Composite Candidates
16.1    Aggregate Applicability Closure
17.1    User Composition Surface
19      exact external-semantic-material classification per law
20      Catalog lifecycle / current-selectability relation family
25      final Candidate admission / terminal Catalog decision under 6.1.4
```

The Canonicalization HIR and Establishment re-audit remains owned by ADR-0066 with ADR-0071, ADR-0063, and the 1D master
checklist. It must close Definition Candidate meaning, Binding Candidate meaning, Basis and Applicability placement,
occurrence law, Established Definition and Occurrence material, exact reference domains, Direct Relation projections,
Fine-Grained projections, coherent observation, current-validity, and reuse without reopening Catalog or source meaning.

API Specification follows semantic closure. It may project only admitted exact meanings and may not settle identity,
applicability, composition, or lifecycle by naming convention. Design follows the semantic and Protocol boundaries and
may
optimize physical lookup, storage, generated realization, caching, vectorization, or fusion without constitutionalizing
one implementation.

For project sequencing, ADR-0076 remains the current closure target. ADR-0075 is re-audited after ADR-0076 closes so
that
its compiler-product protocol can be checked against the final Catalog semantic granularity, bounded observation, and
current-validity seams. Canonical-byte protocols remain a separate decision rather than an extension of this coordinate
Catalog.