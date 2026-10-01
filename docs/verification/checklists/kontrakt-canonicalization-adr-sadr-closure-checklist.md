# Kontrakt Canonicalization ADR / Security SADR Closure Checklist

> **Purpose**
>
> This document is the working closure checklist for re-auditing and revising ADR-0066 `Canonicalization Contract`.
> It is intentionally broader than ADR-0066 prose. Its purpose is to detect omissions before semantic decisions are
> accepted, route each decision to the correct owner, preserve the HIR–Establishment–Protocol architecture, and keep
> V1 implementation choices replaceable for V2.
>
> This checklist **does not itself create Contract authority**. Any semantic answer discovered here must be written back
> to the owning ADR. Security findings belong to a separate Security SADR review and must be routed back to the owning
> semantic or compiler architecture document when they reveal a real defect.
>
> **Critical separation rule**
>
> A concern is not removed from the ADR checklist merely because it also has security value.
> Determinism, immutable publication, protocol boundaries, current-validity/reuse, cache and persistence discipline,
> compiler resource boundedness, independent reference paths, transformation preservation, and V1/V2 replaceability
> remain in the ADR/compiler-architecture checklist whenever they are required for compiler correctness or architectural
> integrity. The Security SADR audits the same mechanisms again under attacker, misuse, trust-boundary, replay,
> corruption, and exceptional-state models.

---

# 0. Source Basis and Re-Audit Scope

This checklist was re-audited against the current project material available for this review, especially:

- `ADR-0066: Canonicalization Contract, Stable Representative, Canonical Bytes, and Explicit Omission`
- `ADR-0063: Contract Establishment, Identity, Applicability, and Composition`
- `ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse`
- `ADR-0064: Input Contract`
- `ADR-0065: Admission Contract`
- `ADR-0067: Lowering Contract`
- `ADR-0051: Budget Contract`
- `ADR-0052: Capacity Contract`
- `ADR-0057: Failure Contract`
- `ADR-0048: Inbound Airlock Composition / Boundary Refinement`
- `What Contract Is`
- `kontrakt-1d-hir-establishment-master-checklist`
- `kontrakt-compiler-total-architecture-map-design-draft`
- `kontrakt-v1-commercial-compiler-foundation-candidate-architecture`
- `kontrakt-v2-reference-architecture-and-v1-foundations`
- compiler reuse / incremental architecture material
- current compiler-result architecture material including ADR-0074 dependencies and ADR-0075 downstream-consumption work
- current Kontrakt Security Architecture material

The current ADR-0066 still contains the old logical ordering:

```text
Input
-> Admission
-> Canonicalization
-> Lowering
```

The re-audit must evaluate the candidate ordering already identified elsewhere:

```text
Canonicalization selected:

Established Input Presentation
-> Canonicalization
-> Canonical Representative
-> Admission
-> Lowering

Canonicalization omitted:

Established Input Presentation
-> Admission
-> Lowering
```

This checklist does **not** pre-decide every consequence of that candidate ordering.

---

# 1. Checklist Status System

Every applicable item should eventually receive one semantic status and one ownership classification.

## 1.1 Semantic status

```text
DEFINED
NOT-APPLICABLE
FORBIDDEN
OPEN
PROVISIONAL
BLOCKED-BY-ADR
SUPERSEDED
```

## 1.2 Ownership classification

```text
ADR-SEMANTIC
    Canonicalization Contract meaning owned by ADR-0066.

ADR-COMPILER-SEAM
    Compiler architecture obligation that ADR-0066 must leave possible or explicitly preserve,
    without fixing one physical implementation.

OTHER-ADR
    Decision owned by another Contract ADR.

COMPOSITION
    Final wiring / branch / selection relation owned by the composition layer, primarily ADR-0048.

DESIGN
    Replaceable physical realization or implementation design.

VERIFICATION
    Reference, PBT, conformance, differential, fuzz, benchmark, or executable checking work.

SADR
    Security-specific threat/trust/assurance decision.

BLOCKED
    Cannot be closed until another owner is settled.
```

## 1.3 Protocol exposure classification

For every HIR and Established Semantic Protocol item, record separately:

```text
EXPOSE
GUARANTEE
RETAIN-HIDE
SEPARATE
ERASE-PERMITTED
FORBIDDEN
```

---

# PART I — ADR-0066 CANONICALIZATION CLOSURE CHECKLIST

# A. Authority, Role, Scope, and Ownership

## A01 — Exact Canonicalization Authority

- [ ] Canonicalization Authority is named exactly.
- [ ] Canonicalization owns declared equivalence rather than generic cleanup.
- [ ] Canonicalization owns representative selection rather than generic transformation.
- [ ] Canonicalization does not inherit authority from host classes, annotations, inheritance, proxies, callbacks, or runtime objects.
- [ ] Canonicalization does not obtain authority from HIR existence, cache hits, generated artifacts, or backend shape.

## A02 — Authority Scope

- [ ] Exact semantic scope is defined.
- [ ] Operation-local vs Interaction-local vs Interface-local scope is explicit.
- [ ] Source containment does not infer scope.
- [ ] Cross-scope consumption requires an explicit relation.

## A03 — Selection Scope

- [ ] Exact `canonicalization` role/slot selection law is defined.
- [ ] Selection and Definition identity are separate.
- [ ] Multiple selection occurrences of one reusable Definition are either allowed or rejected explicitly.
- [ ] Selection does not create runtime object authority.

## A04 — Optionality and Omission

- [ ] Canonicalization may be omitted only if omission is legal under the owning composition law.
- [ ] Omission means no Canonicalization Contract.
- [ ] Omission means no Canonicalization occurrence.
- [ ] Omission means no Canonical Representative authority.
- [ ] Omission does not synthesize `ExactCanonicalization`.
- [ ] Omission is not equivalent to explicit exact-preservation Canonicalization.
- [ ] Omission is not represented as a fake identity canonicalizer.
- [ ] Omission is not confused with permitted absence inside a selected Canonicalization law.

## A05 — Neighboring Authority Boundaries

- [ ] Input owns boundary presentation.
- [ ] Canonicalization does not rewrite Input authority.
- [ ] Admission owns continuation.
- [ ] Canonicalization does not own Admission.
- [ ] Lowering owns shape-changing relation toward Operation inputs / Fact coordinates.
- [ ] Canonicalization does not own Lowering.
- [ ] Fact sameness remains Fact-owned.
- [ ] Failure remains Failure-owned.
- [ ] Budget remains Budget-owned.
- [ ] Capacity remains Capacity-owned.
- [ ] Diagnostic meaning remains Diagnostic-owned.
- [ ] Canonicalization does not become an umbrella authority over inbound processing.

---

# B. Canonicalization Semantic Subject

## B01 — Definition Subject

- [ ] One Canonicalization Definition defines one exact complete Canonicalization law.
- [ ] The reusable Definition subject is independent of one particular runtime occurrence.
- [ ] The Definition subject is independent of one compiler query or generated implementation.

## B02 — Application Subject

- [ ] The exact semantic application to which one Canonicalization law is applied is defined.
- [ ] The source occurrence relation is explicit.
- [ ] Runtime call identity is not assumed to define the application.
- [ ] Compiler query identity is not assumed to define the application.

## B03 — Judgment Subject

- [ ] The Definition-time judgment subject is explicit.
- [ ] The occurrence-time judgment subject is explicit.
- [ ] Any separate compatibility or binding judgment is separately owned.

## B04 — Source Presentation Subject

- [ ] Exact upstream Established meaning consumed by Canonicalization is defined.
- [ ] The semantic observation consumed from Input is producer-defined.
- [ ] Canonicalization does not reopen the host carrier or source syntax.
- [ ] Canonicalization does not privately reconstruct nested Input meaning.

## B05 — Result Subject

- [ ] Exact semantic status of Canonical Representative is defined.
- [ ] Exact semantic status of Canonical Bytes is defined.
- [ ] Result attribution to Definition / occurrence / source basis is explicit.

---

# C. Declared Equivalence Law

## C01 — Equivalence Domain

- [ ] Legal source domain over which equivalence is defined is closed.
- [ ] Illegal, malformed, unresolved, unsupported, unavailable, and corrupt states are distinguished from ordinary non-equivalence.
- [ ] Equivalence is not extended outside the declared domain by implementation convenience.

## C02 — Equivalence Relation Laws

- [ ] Reflexivity is defined where required.
- [ ] Symmetry is defined where required.
- [ ] Transitivity is defined where required.
- [ ] Equivalence-class closure is defined.
- [ ] Any deliberate deviation from mathematical equivalence terminology is explicitly justified.

## C03 — Equivalence Ownership

- [ ] Canonicalization equivalence is Canonicalization-owned.
- [ ] Input Presentation Sameness is not overwritten.
- [ ] Fact sameness is not overwritten.
- [ ] Definition semantic equality is not overwritten.
- [ ] Occurrence identity is not overwritten.
- [ ] JVM equality is not adopted.
- [ ] Host comparator zero-equivalence is not adopted.
- [ ] HID/fingerprint equality is not adopted.
- [ ] Serialized-byte equality is not adopted.

## C04 — Relationship to Input Presentation Sameness

- [ ] Relationship between Input Presentation Sameness and Canonicalization equivalence is explicit.
- [ ] Canonicalization may collapse Input-preserved distinctions only when declared.
- [ ] Canonicalization does not retroactively change what Input established.
- [ ] A host carrier that already lost an Input distinction cannot be repaired by pretending Canonicalization owned the earlier loss.

## C05 — Equivalence-Class Determinism

- [ ] Same legal semantic basis yields the same equivalence judgment.
- [ ] Locale, timezone, provider state, iteration order, worker order, cache state, and runtime object identity cannot alter the class.
- [ ] Versioned/external semantic material used by the law is explicit.

---

# D. Preserved and Collapsed Distinctions

## D01 — Complete Distinction Inventory

- [ ] Every distinction observed by the selected source domain is classified.
- [ ] Each distinction is explicitly preserved or explicitly collapsed where the law is allowed to decide it.
- [ ] No distinction disappears merely because a backend representation omits it.

## D02 — Preserved Distinctions

- [ ] Preserved distinctions remain available to legal downstream consumers.
- [ ] Protocol projections retain enough meaning to preserve them.
- [ ] Compiler normalization cannot erase them early.
- [ ] Optimization cannot assume they are irrelevant.

## D03 — Collapsed Distinctions

- [ ] Collapse is explicitly authorized by the selected law.
- [ ] Collapsed meaning cannot be silently reintroduced by Admission.
- [ ] Collapsed meaning cannot be silently reintroduced by Lowering.
- [ ] Collapsed meaning cannot be recovered from raw backing as a semantic determinant.
- [ ] Collapsed meaning may remain as provenance/evidence only under a separate non-authoritative relation.

## D04 — Information-Loss Burden

- [ ] Every semantic distinction erased by Canonicalization has an explicit owning law.
- [ ] Every compiler distinction erased before or after Canonicalization has an information-loss justification.
- [ ] No later legal consumer requires an erased distinction.
- [ ] If a later consumer still needs it, the semantic model is repaired at the owning authority rather than reconstructed privately.

---

# E. Representative Law

## E01 — Representative Existence

- [ ] Every successful equivalence class has a representative.
- [ ] Conditions under which representative formation is impossible are explicit.

## E02 — Representative Uniqueness

- [ ] One successful application produces one unique representative under the selected law.
- [ ] Tie-breaking cannot depend on host order, hash order, allocation order, or discovery order.

## E03 — Representative Domain Membership

- [ ] Representative belongs to the legal Canonicalization result domain.
- [ ] Relationship between source presentation domain and representative domain is explicit.
- [ ] If "same-shape" is retained, its exact meaning is defined against current Input presentation algebra.

## E04 — Representative Equivalence

- [ ] Source is equivalent to its representative.
- [ ] Equivalent sources yield Presentation-Same / law-same representatives under the exact selected result-equality law.
- [ ] Non-equivalent classes cannot silently collapse to one representative unless the declared relation actually says they are equivalent.

## E05 — Fixed Point and Idempotence

- [ ] Representative is stable under reapplication where reapplication is semantically meaningful.
- [ ] `C(C(x)) = C(x)` or the exact producer-owned equivalent law is stated.
- [ ] Idempotence is distinguished from physical memoization.

## E06 — Representative Equality

- [ ] Representative semantic equality is defined.
- [ ] Representative equality does not collapse occurrence identity.
- [ ] Representative equality does not automatically become Definition identity.
- [ ] Byte equality is not assumed to define representative equality unless explicitly owned.

---

# F. Shape and Presentation Preservation

## F01 — Same-Presentation-Domain Law

- [ ] Canonicalization does not create a second user-authored output presentation.
- [ ] Exact boundary between same-domain representative formation and Lowering is defined.

## F02 — Direct Coordinate Surface

- [ ] Direct Input coordinate addition is forbidden unless explicitly moved to another authority.
- [ ] Direct coordinate removal is forbidden.
- [ ] Direct coordinate rename is forbidden.
- [ ] Direct coordinate retyping is forbidden.
- [ ] Direct coordinate remapping is forbidden.

## F03 — Presence and Absence

- [ ] Required presence distinctions are preserved or explicitly governed.
- [ ] Coordinate absence is distinct from null-like value.
- [ ] Coordinate absence is distinct from empty aggregate.
- [ ] Whole-Input absence is distinct from coordinate absence.

## F04 — Closed Tagged Choice

- [ ] Selected case identity is preserved unless an explicit Canonicalization law legitimately owns a coarser equivalence.
- [ ] Runtime subtype discovery cannot select or rewrite the case.

## F05 — Sequence

- [ ] Positional meaning is preserved unless an explicit law is allowed to collapse order.
- [ ] Multiplicity law is explicit.
- [ ] Representative ordering law is deterministic.

## F06 — Membership

- [ ] Input-owned uniqueness law remains understood.
- [ ] Canonicalization-specific coarser equivalence and duplicate consequences are explicitly defined.
- [ ] No silent first-wins / last-wins / hash-order deduplication occurs.

## F07 — Association

- [ ] Key equivalence is explicit.
- [ ] Consequence of previously distinct keys collapsing into one Canonicalization class is explicitly defined.
- [ ] Duplicate-key / collision outcome is explicit.
- [ ] Value relation remains complete.
- [ ] Entry ordering law is explicit when observable.

## F08 — Nested Presentation

- [ ] V1 support for nested Canonicalization is explicitly decided.
- [ ] Whole-coordinate law vs constituent law is explicitly decided.
- [ ] Nested constituent laws do not accidentally create independent Contract authorities.
- [ ] Nested representative completeness is defined.
- [ ] Closed finite acyclic Input structure remains respected.

## F09 — Semantic Recursion

- [ ] Semantic recursive Canonicalization structures are forbidden or explicitly justified.
- [ ] Fixed-point authority formation is forbidden.
- [ ] Physical iterative traversal is separated from semantic recursion.

---

# G. Authoring and Frontend Boundary

## G01 — Legal Authoring Forms

- [ ] Direct built-in law selection is defined.
- [ ] Coordinate-law declaration is defined if retained.
- [ ] Any other source form is explicitly rejected or separately specified.

## G02 — Flat Law vs Composition

- [ ] V1 flat-law policy is explicitly retained, revised, or removed.
- [ ] Built-in implementation reuse does not become Contract inheritance.
- [ ] Built-in implementation reuse does not become recursive Contract composition.

## G03 — No Executable User Canonicalizer Authority

- [ ] User callbacks are prohibited.
- [ ] Lambdas are prohibited.
- [ ] custom comparator behavior is prohibited as authority.
- [ ] user-defined equality is prohibited as authority.
- [ ] factory calls are prohibited as authority.
- [ ] constructor execution is prohibited as authority.
- [ ] property initialization is prohibited as authority.
- [ ] dependency injection is prohibited as authority.
- [ ] runtime repository/environment lookup is prohibited as authority.

## G04 — Exact Nominal Resolution

- [ ] Exact semantic symbols are resolved before Visible HIR.
- [ ] Simple-name imitation does not grant law meaning.
- [ ] aliasing does not silently create authority.
- [ ] subtype/assignability does not grant authority.
- [ ] package convention does not grant authority.
- [ ] runtime registration does not grant authority.

## G05 — Source Evidence vs Authority

- [ ] class symbol is source evidence, not authority.
- [ ] source location is provenance, not identity.
- [ ] parameter declaration is source evidence, not physical runtime interface.
- [ ] generated API is not source of truth.

## G06 — Complete Coordinate Coverage

- [ ] Missing direct coordinate handling is explicit.
- [ ] duplicate direct coordinate handling is explicit.
- [ ] unknown direct coordinate handling is explicit.
- [ ] incompatible law assignment is explicit.
- [ ] coordinate-set changes invalidate the relevant binding/compatibility result.
- [ ] no undeclared default applies.

## G07 — Public Law Specification

- [ ] Every published built-in law has a complete human-readable specification.
- [ ] Specification names supported source domain.
- [ ] Specification names preserved distinctions.
- [ ] Specification names collapsed distinctions.
- [ ] Specification names representative law.
- [ ] Specification names refusal domain.
- [ ] Specification names semantic bounds.
- [ ] Specification names external semantic-data version where applicable.
- [ ] Specification points to normative conformance material.
- [ ] Documentation does not replace authoritative semantic material.

---

# H. Version and External Semantic Basis

## H01 — Contract Version

- [ ] Canonicalization Definition is or is not Contract-Version-sensitive explicitly.
- [ ] Version Claim and authoritative Version Binding remain distinct.
- [ ] `current`, `latest`, `nearest`, `preferred` do not select Contract Version implicitly.

## H02 — Canonicalization Law Version

- [ ] Law revision domain is explicit.
- [ ] Meaning change vs compatible implementation change is distinguished.
- [ ] Built-in name alone is not the complete version identity.

## H03 — External Semantic Data

- [ ] Unicode data version is fixed where meaning depends on it.
- [ ] timezone/TZDB version is fixed where meaning depends on it.
- [ ] collation data version is fixed where meaning depends on it.
- [ ] URI/grammar profile version is fixed where meaning depends on it.
- [ ] decimal/floating semantic profile is fixed where meaning depends on it.
- [ ] other provider tables are explicit.

## H04 — External Semantic Data Classification

- [ ] Meaning-determining external data is classified as Definition determinant or separately Established semantic basis under an owning law.
- [ ] Ambient provider state cannot complete Definition meaning at runtime.
- [ ] External data is not mislabeled as compiler implementation state if it actually changes Contract meaning.

## H05 — Stability-Domain Separation

- [ ] Contract Version is separate from HIR Protocol revision.
- [ ] Contract Version is separate from Established Protocol revision.
- [ ] Contract Version is separate from compiler generation.
- [ ] Contract Version is separate from persistent schema version.
- [ ] Contract Version is separate from canonical-byte protocol version unless owning law explicitly couples them.

---

# I. HIR Definition Candidate

## I01 — Definition Existence

- [ ] Canonicalization has reusable Definition meaning.
- [ ] Exact Definition subject is explicit.

## I02 — Complete Candidate Meaning

- [ ] HIR Candidate contains every Definition determinant required by Canonicalization Establishment.
- [ ] Establishment does not reopen authored source.
- [ ] Establishment does not inspect host object topology.

## I03 — Determinant Set

- [ ] Complete Definition determinant set is listed.
- [ ] Operation/Interaction/Interface context is included only if Canonicalization law makes it Definition-determining.
- [ ] Exact selected Input Definition is not added merely because occurrence-time consumption later requires Input.
- [ ] Version material is included only when meaning-determining.
- [ ] external semantic data is included only under its true owning relation.

## I04 — Non-Determinant Context Exclusion

- [ ] Binding-only context stays Binding-only.
- [ ] occurrence basis stays occurrence basis.
- [ ] Applicability context stays Applicability context.
- [ ] provenance stays provenance.
- [ ] compiler cache/query state stays compiler state.

## I05 — Candidate Reference

- [ ] Exact component set of Canonicalization CandidateRef is defined.
- [ ] CandidateRef is typed.
- [ ] CandidateRef is not source AST identity.
- [ ] CandidateRef is not dense ordinal identity.
- [ ] CandidateRef is not HID identity.
- [ ] CandidateRef is not DefinitionRef.

## I06 — Candidate Equality

- [ ] Candidate semantic equality is producer-owned.
- [ ] CandidateRef equality and Candidate semantic equality are separate.
- [ ] Equal payload does not silently merge independently identified candidates.

## I07 — Presence / Absence / Cardinality

- [ ] Candidate presence is explicit where semantic.
- [ ] `not owned`, `omitted`, `required unresolved`, `unsupported`, `unavailable`, `corrupt` are separated.
- [ ] cardinality law is explicit.
- [ ] closed variant domain is explicit.

## I08 — Ordering and Multiplicity

- [ ] Definition ordering is semantic only when owned.
- [ ] source declaration order does not become meaning automatically.
- [ ] set/bag/sequence semantics are explicit where relevant.

## I09 — Collision / Coverage / Merge

- [ ] duplicate Definition declaration law is explicit.
- [ ] duplicate coordinate law is explicit.
- [ ] same identity + conflicting meaning law is explicit.
- [ ] complete coverage is explicit.
- [ ] partial coverage is either forbidden or semantically defined.
- [ ] compiler deduplication does not become semantic merge.

## I10 — Source Refinement

- [ ] syntax sugar may be erased only when Candidate meaning is preserved.
- [ ] source-only nesting may be erased only when no semantic observer needs it.
- [ ] different authoring forms may converge only under producer-owned semantic equality.
- [ ] HIR frontend normalization does not perform Contract Canonicalization.

---

# J. HIR Binding Candidate

## J01 — Binding Existence

- [ ] Canonicalization selection requires a distinct IDL Binding Candidate.
- [ ] Definition Candidate and Binding Candidate are separate.

## J02 — Selecting Subject

- [ ] Exact subject that owns the selection occurrence is explicit.
- [ ] Exact slot/role coordinate is explicit.

## J03 — Target

- [ ] Binding points to one exact Canonicalization CandidateRef.
- [ ] Binding does not copy Definition identity.

## J04 — Binding-Owned Qualifiers

- [ ] Any qualifier independently owned by the Binding relation is explicit.
- [ ] Surrounding IDL context is excluded unless genuinely part of Binding meaning.

## J05 — Reuse

- [ ] Several bindings may or may not select one Canonicalization Definition explicitly.
- [ ] Input compatibility is checked by the owning binding/composition relation rather than silently entering Definition meaning.

## J06 — Binding Collision and Singularity

- [ ] zero target handling is explicit.
- [ ] multiple-target handling is explicit.
- [ ] ambiguity handling is explicit.
- [ ] no first/nearest/discovery-order resolution.

## J07 — Binding Family Separation

- [ ] IDL Binding is not Version Binding.
- [ ] IDL Binding is not Basis Binding.
- [ ] IDL Binding is not Governance Binding.
- [ ] IDL Binding is not runtime realization binding.

---

# K. Resolved HIR Candidate Protocol

## K01 — Typed Reference Domains

- [ ] Every Canonicalization HIR semantic reference kind is enumerated.
- [ ] Same-width physical handle reuse does not make reference kinds interchangeable.

## K02 — Definition Candidate Projection

- [ ] Exposes exact CandidateRef.
- [ ] Exposes complete producer-owned Candidate meaning.
- [ ] Does not expose future Established authority.

## K03 — Binding Candidate Projection

- [ ] Exposes exact selection subject.
- [ ] Exposes role coordinate.
- [ ] Exposes exact target CandidateRef.
- [ ] Exposes only binding-owned qualifiers.

## K04 — Fine-Grained HIR Projection Catalog

- [ ] Definition law projection.
- [ ] source-domain requirement projection.
- [ ] equivalence-law projection.
- [ ] preserved/collapsed distinction projection.
- [ ] representative-law projection.
- [ ] refusal-domain projection.
- [ ] semantic-bound projection.
- [ ] external semantic-basis/version projection where required.
- [ ] canonical-byte-law projection if canonical bytes remain Contract material.

## K05 — Projection Completeness

- [ ] Completeness is defined by the producer.
- [ ] Each projection states the observation for which it is complete.
- [ ] A smaller projection is not interpreted as a weaker Canonicalization law.

## K06 — Projection Equality

- [ ] Equality is producer-owned.
- [ ] Consumer-specific equality does not rewrite Candidate meaning.

## K07 — Provenance Separation

- [ ] Source provenance is separately observable where needed.
- [ ] provenance is not Candidate identity.
- [ ] provenance is not Definition meaning.

## K08 — No Source / Topology Recovery

- [ ] no source reopening.
- [ ] no host-carrier traversal.
- [ ] no parent/children graph inference.
- [ ] no query topology inference.
- [ ] no physical table containment inference.

## K09 — Protocol Physical Freedom

- [ ] Protocol does not require wrapper allocation.
- [ ] Protocol does not require full copy.
- [ ] shared immutable backing is allowed.
- [ ] zero-copy is allowed where coherent.
- [ ] physical representation may be primitive/table/slab/columnar.

---

# L. HIR Seal, Visibility, Lifecycle, and Evolution

## L01 — Resolution Invariant

- [ ] all semantic references exact before Visible HIR.
- [ ] no unresolved lexical ambiguity.
- [ ] no runtime lookup required to interpret Candidate meaning.

## L02 — Canonicalization-Specific Seal Conditions

- [ ] Definition candidate is complete.
- [ ] Binding candidate is complete.
- [ ] required external semantic law/version material is resolved.
- [ ] no recovery/poison semantics remain in valid Candidate.

## L03 — Seal Is Not Contract Judgment

- [ ] HIR seal success does not establish Canonicalization Definition.
- [ ] HIR seal rejection is compiler-owned.
- [ ] unentered Contract judgment gets no synthetic Canonicalization Refusal.

## L04 — Private Construction and Visibility

- [ ] incomplete transient state is private.
- [ ] ordinary consumers observe only complete sealed HIR.
- [ ] visible HIR meaning is immutable in place.

## L05 — Generation and Snapshot Coherence

- [ ] one semantic computation observes one coherent generation/projection.
- [ ] stale HIR references are rejected or revalidated.
- [ ] failed replacement does not displace last valid generation.
- [ ] generation identity does not become Contract identity.

## L06 — Lifecycle

- [ ] Construction
- [ ] Validated
- [ ] Sealed
- [ ] Visible
- [ ] Superseded
- [ ] Retired
- [ ] Reclaimed

## L07 — Protocol Evolution

- [ ] common HIR Protocol revision separated from Canonicalization-specific surface revision.
- [ ] required extension is distinguishable from optional/non-semantic extension.
- [ ] unknown required semantic extension fails closed at the compiler boundary.
- [ ] unsupported known extension cannot silently downgrade.
- [ ] compatible projection is producer-defined and complete.
- [ ] migration is lossless for legal observations.
- [ ] opaque retention is distinct from interpretation.
- [ ] stable extension coordinates are not repurposed incompatibly.
- [ ] internal HIR Protocol does not become public ABI by accident.

---

# M. HIR-to-Establishment Handoff

## M01 — Legal Input Classes

Every Establishment input must be classified as one of:

- [ ] legal HIR Protocol observation.
- [ ] separately Established prerequisite.
- [ ] explicit authority-owned judgment input.

## M02 — No Hidden Reconstruction

- [ ] no authored-source reopening.
- [ ] no host topology reconstruction.
- [ ] no IDL parent traversal beyond legal binding meaning.
- [ ] no query topology reconstruction.
- [ ] no cache/backing inference.
- [ ] no backend-state reconstruction.

## M03 — No Premature Authority

- [ ] HIR does not precompute Canonicalization success.
- [ ] HIR does not precompute canonical representative authority.
- [ ] HIR does not precompute occurrence result.
- [ ] HIR does not pre-establish Basis Binding or Applicability result.

## M04 — Determinant Traceability

- [ ] every Definition Establishment determinant traces to legal HIR observation or exact authoritative prerequisite.
- [ ] every occurrence determinant traces to exact Established basis / authority-owned input.
- [ ] hidden compiler state cannot enter determinant set.

## M05 — Binding Discipline

- [ ] Binding Candidate is consumed only for the relation it actually owns.
- [ ] surrounding context is not smuggled into Definition meaning through Binding.

## M06 — Handoff Completeness

- [ ] Establishment can execute without private frontend logic.
- [ ] no undocumented fallback lookup path exists.

## M07 — Non-Amplification

- [ ] Establishment creates only Canonicalization-owned meaning.
- [ ] no compiler reachability conclusion is established.
- [ ] no optimization fact is established.
- [ ] no diagnostic explanation is established.
- [ ] no realization conclusion is established.
- [ ] no downstream consumer-specific projection is established.

---

# N. Canonicalization Definition Establishment

## N01 — Judgment Inventory

- [ ] Definition-time judgments are enumerated.
- [ ] occurrence-time judgments are enumerated separately.
- [ ] compatibility/composition judgments are not silently folded into Definition Establishment.

## N02 — Definition Judgment Owner

- [ ] exact owner is Canonicalization authority.
- [ ] compiler validator does not become owner.

## N03 — Legal Entry

- [ ] complete sealed Candidate required.
- [ ] exact references required.
- [ ] required semantic determinants available.
- [ ] compiler-unsuccessful preconditions remain non-entry rather than Canonicalization Refusal.

## N04 — Definition Result Vocabulary

- [ ] Established Canonicalization Definition.
- [ ] Canonicalization Definition Refusal if such a negative result is owned.
- [ ] non-entry.
- [ ] compiler unsuccessful result.
- [ ] unsupported compiler capability.
- [ ] resource stop remains separately owned.

## N05 — Complete Established Unit

- [ ] minimum complete Definition unit is explicit.
- [ ] partial Definition authority is forbidden.
- [ ] independent sibling Definitions may become visible independently where allowed.

## N06 — Definition Authority Grant Point

- [ ] exact semantic boundary at which Definition becomes authoritative is stated.
- [ ] physical write/commit/publication mechanism does not define authority.

---

# O. Required Basis, Basis Binding, and Applicability

## O01 — Basis Requirement Law

- [ ] Canonicalization Definition states what occurrence-time source meaning it requires.
- [ ] Requirement names semantic meaning, not compiler producer.
- [ ] requirement cardinality is explicit.
- [ ] requirement completeness is explicit.
- [ ] permitted absence is explicit.

## O02 — Input Observation Requirement

- [ ] exact required producer observation is defined.
- [ ] requirement does not automatically name one exact Input Definition as Canonicalization Definition identity.
- [ ] complete Input-observation requirement belongs to Canonicalization Definition if required by its meaning.

## O03 — Required Basis Instance

- [ ] exact owner of each Required Basis instance is explicit.
- [ ] occurrence-time formation point is explicit.
- [ ] requirement coordinate is explicit if several basis relations exist.

## O04 — Basis Resolution

- [ ] exact law that resolves the source is explicit.
- [ ] no first/nearest/lowest-HID/discovery-order lookup.
- [ ] compiler dependency edges do not establish Basis Binding.

## O05 — Basis Binding

- [ ] one exact Required Basis relates to one exact already-Established source.
- [ ] exact Presented Input Occurrence / Established Input Presentation relation is represented where selected.
- [ ] Binding does not imply Applicability.

## O06 — Applicability

- [ ] Applicability Law exists or is explicitly NOT-APPLICABLE.
- [ ] source-domain compatibility is not confused with ADR-0063 Applicability.
- [ ] Applicable Context is sparse but complete.
- [ ] `no additional context required` differs from `required context unavailable`.
- [ ] ambient `current` lookup is forbidden.
- [ ] compiler generation/cache/query/provenance do not enter Applicable Context.
- [ ] result vocabulary is explicit.
- [ ] multiple applicable bindings / singularity / arbitration are explicitly owned.

## O07 — Complete Basis

- [ ] exact condition for complete occurrence basis is defined.
- [ ] incomplete required basis prevents legal entry.
- [ ] incomplete basis does not fabricate Failure or Canonicalization Refusal.

## O08 — Semantic Dependency

- [ ] direct semantic prerequisite relation is explicit.
- [ ] compiler dependency is separate.
- [ ] semantic establishment cycles are rejected.
- [ ] fixed-point authority formation is forbidden.

---

# P. Canonicalization Occurrence

## P01 — Occurrence Existence

- [ ] Canonicalization application result has independent occurrence meaning or is explicitly NOT-APPLICABLE.

## P02 — Occurrence Subject

- [ ] one exact semantic application is defined.

## P03 — Occurrence Determinants

- [ ] exact applied DefinitionRef.
- [ ] exact determining source occurrence/basis.
- [ ] exact meaning-determining applicable context if any.
- [ ] any additional owner-defined coordinate.

## P04 — Occurrence Reference

- [ ] typed exact Canonicalization OccurrenceRef exists if occurrences exist.
- [ ] physical runtime call/query identity is not the reference.

## P05 — Freshness

- [ ] fresh eligible upstream Input occurrence produces a fresh Canonicalization application.
- [ ] equal presentations do not merge occurrences.
- [ ] equal representatives do not merge occurrences.
- [ ] repeated physical evaluation of one exact occurrence does not create extra semantic occurrences.

## P06 — Historical Attribution

- [ ] occurrence retains exact DefinitionRef.
- [ ] occurrence retains exact determining semantic basis.
- [ ] later current Version/Policy/Governance/State cannot rewrite old attribution.
- [ ] provenance remains separate.

## P07 — Retry / Restart

- [ ] retry of physical computation vs new semantic application is explicitly distinguished.
- [ ] cancelled incomplete computation does not mint occurrence authority.

---

# Q. Occurrence Judgment and Result Vocabulary

## Q01 — Legal Entry

- [ ] exact Established Canonicalization Definition available.
- [ ] complete applicable Required Basis available.
- [ ] source Input occurrence has the required `Presented` result.
- [ ] required semantic context available.
- [ ] no earlier owner-specific stop prevents entry.

## Q02 — Successful Outcome

- [ ] `Canonicalized` or equivalent positive result vocabulary is explicit.
- [ ] representative relation is complete.
- [ ] attribution is complete.
- [ ] canonical bytes relation is complete if part of semantic result.

## Q03 — Canonicalization Refusal

- [ ] refusal occurs only after legal entry.
- [ ] source-outside-domain case is explicit.
- [ ] undefined representative case is explicit.
- [ ] non-unique representative case is explicit.
- [ ] missing semantic basis is classified correctly rather than automatically becoming Refusal.
- [ ] Refusal does not hide Input Refusal.
- [ ] Refusal does not hide Admission Rejection.
- [ ] Refusal does not hide Lowering Refusal.
- [ ] Refusal does not hide Budget/Capacity/resource/compiler results.

## Q04 — Successful-but-Noncanonical Result

- [ ] classified as Kontrakt defect / compiler inconsistency rather than alternate Contract meaning.

## Q05 — No Post-Canonical User Validator

- [ ] no user `isCanonical`.
- [ ] no user `validateCanonical`.
- [ ] no user callback that can acquire post-canonical authority.

---

# R. Established Material Classification

## R01 — Established Definition Material

- [ ] complete Canonicalization Definition meaning is specified.
- [ ] exact DefinitionRef is specified.
- [ ] direct established Definition relations are specified.

## R02 — Established Occurrence Material

- [ ] complete occurrence meaning is specified if occurrences exist.
- [ ] exact OccurrenceRef is specified.
- [ ] exact DefinitionRef relation is specified.
- [ ] determining semantic basis attribution is specified.
- [ ] exact result relation is specified.

## R03 — Canonical Representative Classification

- [ ] explicitly decide whether representative is:
  - [ ] occurrence result relation,
  - [ ] separate independently Established Material,
  - [ ] another authority-specific material.
- [ ] no separate material is introduced merely because implementation wants a wrapper/object.

## R04 — Canonical Bytes Classification

- [ ] explicitly decide whether canonical bytes are:
  - [ ] Contract-observable part of Definition/occurrence law,
  - [ ] authority-specific Established Material,
  - [ ] compiler-only deterministic encoding.
- [ ] exact classification is consistent with ADR-0063 "bytes become Contract material only when owning law says so."

## R05 — No Universal Schema

- [ ] no normative `EstablishedMaterial {type,id,payload}` requirement.
- [ ] semantic distinctions do not force one object/table per distinction.

---

# S. Established Definition Identity

## S01 — Complete Definition Meaning

- [ ] exact source-owned Canonicalization Definition meaning is enumerated.

## S02 — Owning Authority Binding

- [ ] exact Canonicalization authority recoverable directly by legal semantic consumer.

## S03 — Definition Identity

- [ ] exact authority-owned identity determinants are defined.
- [ ] identity does not come from source class name.
- [ ] identity does not come from source location.
- [ ] identity does not come from Input Definition merely through adjacency.
- [ ] identity does not come from HID/fingerprint/object identity.

## S04 — Version Binding

- [ ] exact Version Binding is included only if semantic law is version-sensitive.

## S05 — Authority-Local Definition Coordinate

- [ ] local coordinate exists only if owning identity law requires it.
- [ ] declaration order/table row does not substitute.

## S06 — DefinitionRef

- [ ] identifies one exact authoritative Canonicalization Definition.
- [ ] remains interpretable without retaining original HIR generation.

## S07 — CandidateRef / DefinitionRef Boundary

- [ ] no universal cast or upgrade.
- [ ] one CandidateRef is not assumed one-to-one with one DefinitionRef universally.

## S08 — Conflict

- [ ] same exact identity coordinate + conflicting meaning has explicit reject/resolve law.

## S09 — Immutability and History

- [ ] Established Definition meaning immutable.
- [ ] linking/relocation does not change Definition identity.
- [ ] history/supersession does not grant current identity.

---

# T. Canonical Contract World and Occurrence Placement

## T01 — Definition Placement

- [ ] Established Canonicalization Definition Material is available through Canonical Contract World legal surface.

## T02 — Occurrence Placement

- [ ] occurrence-specific material is not universally inserted into Definition World merely for convenience.

## T03 — Representative Placement

- [ ] exact backing/semantic placement follows the classification decided in R03.

## T04 — Canonical Bytes Placement

- [ ] exact backing/semantic placement follows R04.

## T05 — Relation-State Taxonomy

Distinguish:

- [ ] relation not owned.
- [ ] owned and validly absent.
- [ ] owned and present.
- [ ] required but unresolved.
- [ ] not retained.
- [ ] not materialized.
- [ ] unavailable.
- [ ] unsupported.
- [ ] stale.
- [ ] corrupt.

## T06 — World Coherence

- [ ] consumer cannot combine stale/current backing without validity proof.
- [ ] physical co-location creates no semantic relation.
- [ ] World visibility does not itself create authority.

## T07 — Retention and Reclamation

- [ ] semantic existence is separate from physical retention.
- [ ] reclamation does not retroactively erase earlier Establishment.
- [ ] retention does not extend authority.
- [ ] occurrence lifetime and Definition lifetime are independently classifiable.

---

# U. Established Canonicalization Semantic Protocol

## U01 — Typed Established Reference Domains

- [ ] Canonicalization DefinitionRef.
- [ ] Canonicalization OccurrenceRef if applicable.
- [ ] authority-specific reference type if genuinely required.
- [ ] no generic untyped universal reference.

## U02 — Established Definition Projection

- [ ] exact DefinitionRef.
- [ ] Owning Authority Binding.
- [ ] Version Binding if applicable.
- [ ] complete local Definition meaning.
- [ ] direct Definition-owned relations.

## U03 — Established Occurrence Projection

- [ ] exact OccurrenceRef.
- [ ] exact DefinitionRef.
- [ ] exact determining semantic basis.
- [ ] exact occurrence result.
- [ ] representative relation on successful outcome.
- [ ] canonical-byte relation if semantic.

## U04 — Fine-Grained Projection Catalog

- [ ] Definition Law Projection.
- [ ] Source Requirement Projection.
- [ ] Equivalence Law Projection.
- [ ] Preserved/Collapsed Distinction Projection.
- [ ] Representative Law Projection.
- [ ] Representative Result Projection.
- [ ] Occurrence Outcome Projection.
- [ ] Refusal Projection where legal.
- [ ] Canonical Bytes Projection if semantic.
- [ ] external semantic-basis/version projection where legal.

## U05 — Projection Completeness

- [ ] every projection declares its complete observation domain.
- [ ] projection omission is not semantic absence.
- [ ] consumer cannot redefine completeness.

## U06 — Projection Equality

- [ ] equality is producer-owned.
- [ ] equal projection payload does not merge DefinitionRefs.
- [ ] equal projection payload does not merge OccurrenceRefs.

## U07 — Direct Relation Projection

- [ ] exact producer-owned relations are observable without recreating them through analysis.

## U08 — No Transitive World Embedding

- [ ] local projection does not recursively embed whole semantic world.

## U09 — No Generic Navigation Authority

- [ ] no `getParent`.
- [ ] no generic `getChildren`.
- [ ] no `walkGraph`.
- [ ] no `allReachable` as semantic authority.

## U10 — Observation Is Not Authority Transfer

- [ ] consumer observation leaves Canonicalization authority with Canonicalization.

---

# V. Protocol Consumption and Availability

## V01 — Producer-Defined Projection

- [ ] producer owns projection semantics.
- [ ] consumer selects only legal producer-defined views.

## V02 — Consumer Non-Ownership

- [ ] consumer cannot redefine projection equality.
- [ ] consumer cannot redefine completeness.
- [ ] consumer cannot fabricate a consumer-specific Canonicalization meaning.

## V03 — Derived Knowledge

- [ ] summary, conflict analysis, optimization knowledge, ranking, reachability, proof, and cost models are derived compiler products unless another Contract authority owns them.

## V04 — No Backing Bypass

- [ ] consumer does not read raw producer table to obtain additional semantic distinctions.
- [ ] shared backing does not authorize wider observation.
- [ ] universal handle cannot escape narrow reference domain.

## V05 — Availability Taxonomy

Distinguish:

- [ ] semantic absence.
- [ ] projection not retained.
- [ ] projection not materialized.
- [ ] backing unavailable.
- [ ] unsupported protocol.
- [ ] corrupt storage.
- [ ] stale relation.
- [ ] unknown required extension.

## V06 — Downstream Non-Entry

- [ ] required observation unavailable -> dependent judgment remains unentered.
- [ ] no fabricated Refusal / Rejection / Failure.

## V07 — Coherent Observation

- [ ] one legal read corresponds to one coherent logical semantic state.
- [ ] cross-generation mixing requires explicit current-validity proof.

---

# W. Composition Boundary

> ADR-0066 must specify what Canonicalization requires and guarantees.
> ADR-0048 or the final composition owner still owns the complete inbound branch wiring.

## W01 — Composition Responsibility

- [ ] Canonicalization-owned semantic prerequisites are defined.
- [ ] Canonicalization does not silently own whole pipeline composition.
- [ ] final branch wiring is marked for ADR-0048/WCI repair later.

## W02 — Definition Selection vs Composition

- [ ] selecting Canonicalization Definition is separate from connecting it to Input/Admission.
- [ ] Definition identity is not derived from one incidental composition.

## W03 — Binding Candidate vs Composition

- [ ] IDL-use relation is distinct from authoritative runtime/Established composition relation.

## W04 — Basis Binding vs Composition

- [ ] occurrence Required Basis is Canonicalization-owned where appropriate.
- [ ] exact source connection is formed by the owning composition/binding law.

## W05 — Selected Branch

- [ ] Established Input Presentation -> Canonicalization.
- [ ] Canonicalization positive result -> Canonical Representative.
- [ ] Canonical Representative -> Admission.
- [ ] Admission observes the same representative relation later used downstream.
- [ ] Lowering does not silently return to raw presentation as semantic source.

## W06 — Omitted Branch

- [ ] Established Input Presentation -> Admission.
- [ ] no Canonicalization Definition application.
- [ ] no Canonicalization occurrence.
- [ ] no canonical representative.
- [ ] no synthetic identity law.

## W07 — Branch Exclusivity

- [ ] selected and omitted semantics cannot coexist ambiguously for one slot occurrence.
- [ ] exactly-one/zero-or-one cardinality is explicit.

## W08 — Composition Completeness

- [ ] required upstream basis is complete.
- [ ] required downstream relation is complete.
- [ ] incomplete composition prevents entry rather than fabricating a Contract result.

## W09 — Composition Collision and Ambiguity

- [ ] multiple selected Canonicalization definitions.
- [ ] multiple possible source Input bindings.
- [ ] incompatible source presentation.
- [ ] incompatible downstream Admission requirement.
- [ ] no first/nearest/order arbitration unless explicitly owned.

## W10 — Semantic vs Physical Order

- [ ] semantic prerequisite order is explicit.
- [ ] compiler may fuse stages physically without changing ownership.
- [ ] physical schedule is not semantic pipeline authority.

---

# X. Selected-Branch Judgment–Use Coherence

## X01 — Admission Operand

- [ ] selected branch Admission consumes Canonicalization-owned representative observation.
- [ ] Admission does not use raw Input as an alternate determinant once the law has collapsed that distinction.

## X02 — Lowering Operand

- [ ] Lowering consumes the exact semantic relation Admission judged.
- [ ] Lowering does not independently recanonicalize.
- [ ] Lowering does not recover erased raw distinctions.

## X03 — Raw Input Retention

- [ ] raw presentation may remain for provenance/diagnostics where permitted.
- [ ] raw presentation does not regain semantic determinant status after selected Canonicalization.
- [ ] raw backing access is not a legal semantic bypass.

## X04 — Distinction Compatibility

- [ ] downstream authority requirements are checked against preserved/collapsed distinctions.
- [ ] later contract cannot demand a distinction that Canonicalization made semantically unavailable.
- [ ] incompatible binding is rejected by the proper owner.

## X05 — Omission Branch

- [ ] raw/Established Input meaning remains available to Admission according to Input-owned protocol.
- [ ] omission branch may legally distinguish values that an explicitly selected Canonicalization law would collapse.
- [ ] this difference is intentional and testable.

---

# Y. Canonical Bytes and Deterministic Encoding

## Y01 — Semantic Status Decision

- [ ] decide whether canonical bytes are mandatory for every Canonicalization law.
- [ ] decide whether canonical bytes are optional per law.
- [ ] decide whether some deterministic compiler encodings are explicitly non-Contract material.

## Y02 — Ownership

- [ ] Canonicalization owns canonical bytes only when law explicitly says so.
- [ ] compiler storage encoding does not become Contract canonical bytes by reuse.
- [ ] HID encoding does not become Canonicalization law.
- [ ] backend encoding does not become Canonicalization law.

## Y03 — Representative-to-Bytes Relation

- [ ] representative meaning exists prior to or independently from bytes as appropriate.
- [ ] bytes do not create semantic equivalence unless explicitly owned.
- [ ] equivalent representative relation and byte relation are not circularly defined without a clear owner.

## Y04 — Byte Protocol

- [ ] protocol version.
- [ ] schema identity.
- [ ] framing.
- [ ] scalar encoding.
- [ ] presence encoding.
- [ ] aggregate encoding.
- [ ] ordering.
- [ ] duplicate encoding.
- [ ] external semantic-data version interactions.
- [ ] output bound.

## Y05 — Decoder / Round-Trip

- [ ] exact decoder contract exists if bytes are meant for round-trip semantic observation.
- [ ] decode success alone does not establish authority.
- [ ] round-trip obligation is explicit if required.
- [ ] cross-version decode/migration behavior is explicit.

## Y06 — Collision and Ambiguity

- [ ] byte collision law is explicit.
- [ ] framing ambiguity is impossible under declared protocol.
- [ ] concatenation/domain boundaries are unambiguous.

## Y07 — Byte Reproducibility vs Semantic Determinism

- [ ] exact byte reproducibility requirement is distinguished from semantic determinism.
- [ ] artifact/backend reproducibility is owned by the relevant producer, not silently by Canonicalization.

---

# Z. Refusal, Compiler Result, Failure, Budget, and Capacity Boundary

## Z01 — Result Taxonomy

Distinguish:

- [ ] Canonicalization Definition Refusal.
- [ ] Canonicalization occurrence Refusal.
- [ ] never entered.
- [ ] compiler unsuccessful result.
- [ ] unavailable.
- [ ] unsupported.
- [ ] corrupt.
- [ ] stale.
- [ ] indeterminate / trust loss.
- [ ] Budget result.
- [ ] Capacity result.
- [ ] compiler resource stop.
- [ ] ADR-0057 Failure result.

## Z02 — Refusal Ownership

- [ ] only Canonicalization-owned semantic negative conditions become Canonicalization Refusal.
- [ ] Input Refusal is preserved.
- [ ] Admission Rejection is preserved.
- [ ] Lowering Refusal is preserved.
- [ ] Failure authority is not fabricated.

## Z03 — Budget

- [ ] semantic Canonicalization result does not vary by available Budget unless Budget is explicitly part of an owning semantic relation.
- [ ] Budget stop remains Budget-owned.
- [ ] required semantic work is not silently omitted due to compiler budget.

## Z04 — Capacity

- [ ] Capacity stop remains Capacity-owned.
- [ ] capacity limit does not redefine equivalence or representative.
- [ ] compiler memory limit does not become Capacity Contract automatically.

## Z05 — Compiler Resource Limits

- [ ] compiler CPU limit.
- [ ] compiler memory envelope.
- [ ] scratch/intermediate-storage limit.
- [ ] cancellation.
- [ ] I/O/materialization failure.
- [ ] optimization abandonment.
- [ ] these are separated from Contract Budget/Capacity unless another law explicitly connects them.

## Z06 — Recovery

- [ ] recovery preserves original result ownership.
- [ ] retry/fallback does not convert compiler failure to Contract Refusal.
- [ ] failed generation cannot publish partial Canonicalization authority.

---

# AA. Semantic Bounds and Finite Completion

## AA01 — Semantic Bounds

- [ ] source-domain finiteness required by the Canonicalization law.
- [ ] semantic maximum source size if genuinely part of law.
- [ ] semantic maximum cardinality if genuinely part of law.
- [ ] semantic depth if genuinely part of law.
- [ ] semantic output bound if genuinely part of law.
- [ ] canonical expansion law.
- [ ] these bounds participate in Definition meaning only when they are actually semantic.

## AA02 — Compiler Resource Bounds

- [ ] implementation work bound is separate from semantic meaning where appropriate.
- [ ] temporary allocation bound is compiler policy unless semantic.
- [ ] stack-recursion avoidance is design/verification unless semantic.
- [ ] iterative traversal remains implementation freedom.
- [ ] overflow-safe sizing is compiler correctness.
- [ ] diagnostic amplification bound is compiler correctness.

## AA03 — Finite Semantic Completion

- [ ] every legal occurrence either completes its owning semantic judgment or is stopped by a separately owned result.
- [ ] no unbounded user-controlled computation is authorized as Canonicalization meaning.
- [ ] built-in law implementation may change while preserving the same semantic completion relation.

---

# AB. Determinism and Ambient-State Exclusion

## AB01 — Exact Determinant Set

- [ ] complete semantic determinant set for Definition.
- [ ] complete semantic determinant set for occurrence.
- [ ] complete compiler-result determinant set where downstream derived products exist.

## AB02 — Ambient State Exclusion

None of the following may silently alter Canonicalization meaning:

- [ ] wall clock.
- [ ] randomness.
- [ ] process environment.
- [ ] default locale.
- [ ] default timezone.
- [ ] default charset.
- [ ] current Unicode provider.
- [ ] current TZDB provider.
- [ ] filesystem discovery order.
- [ ] classpath order.
- [ ] thread identity.
- [ ] worker identity.
- [ ] scheduler order.
- [ ] hash iteration order.
- [ ] allocation order.
- [ ] cache state.
- [ ] runtime profile.
- [ ] object identity.
- [ ] reflection enumeration order.

## AB03 — Physical Order Separation

- [ ] physical completion order is not semantic prerequisite order.
- [ ] deterministic traversal order is not automatically Contract-visible order.
- [ ] semantic ordering is explicit where it exists.

## AB04 — Clean Reference Path

- [ ] clean deterministic computation exists or can be independently reconstructed for correctness comparison.
- [ ] reused/cached/parallel paths must agree with it.

---

# AC. Concurrency and Publication

## AC01 — Parallel Formation

- [ ] independent work can execute in different schedules without semantic change.
- [ ] worker count cannot change result.

## AC02 — Duplicate Computation

- [ ] duplicate physical computation does not create duplicate Contract authority.
- [ ] same-result concurrent formation can converge.
- [ ] conflicting concurrent results are compiler inconsistency.
- [ ] no last-writer-wins semantic rule.

## AC03 — Complete Visibility

- [ ] no partial Established Definition visibility.
- [ ] no partial Established Occurrence visibility.
- [ ] no partial representative observation.
- [ ] no partial canonical-byte observation if semantic.

## AC04 — Independent Siblings

- [ ] independently complete Definitions may become visible independently.
- [ ] no universal global establishment transaction is required unless another law owns one.

## AC05 — Cancellation

- [ ] cancelled candidate computation does not publish success.
- [ ] retry after cancellation preserves semantic ownership rules.

---

# AD. Current Validity, Reuse, Cache, and Persistence

## AD01 — Equality vs Reuse Validity

- [ ] semantic equality is distinct from current reuse validity.
- [ ] byte equality is distinct from semantic equality.
- [ ] object identity is distinct from semantic equality.
- [ ] HID/fingerprint is evidence, not authority.

## AD02 — Retained vs Current

- [ ] retained material is not current-valid automatically.
- [ ] persisted material is not authoritative automatically.
- [ ] current Definition/Version/reference correspondence is validated.

## AD03 — Complete-Set Closure

When reused material claims complete coverage:

- [ ] all retained members still valid.
- [ ] no new legal member appeared.
- [ ] removed member is not retained as current.
- [ ] membership-domain changes invalidate/revalidate closure.

## AD04 — Definition Reuse

- [ ] Definition reuse validates every semantic determinant.
- [ ] binding compatibility is re-evaluated independently where required.
- [ ] provenance-only change may preserve semantic reuse only under producer-owned validity rule.

## AD05 — Occurrence Non-Reuse

- [ ] fresh semantic application does not reuse an old OccurrenceRef.
- [ ] physical computation/representative storage may be reused only without reusing occurrence authority.

## AD06 — Representative Physical Reuse

- [ ] semantic preconditions for reuse are explicit.
- [ ] reused representative equals clean representative under producer law.
- [ ] attribution remains current and exact.

## AD07 — Canonical Bytes Physical Reuse

- [ ] current semantic/protocol revision is validated.
- [ ] bytes alone do not establish current authority.

## AD08 — Cache Semantics

- [ ] cache hit and clean path expose same legal result.
- [ ] cache miss is semantically neutral.
- [ ] rejected reuse returns to clean path when possible.
- [ ] cache corruption produces compiler-owned unsuccessful result, not Canonicalization Refusal.

## AD09 — Persistence

- [ ] persistent schema revision separate from Contract Version.
- [ ] stale reference remapping/revalidation.
- [ ] corrupt persistence rejected.
- [ ] persistent reload agrees with clean recomputation.

---

# AE. V1 Query-Oriented Compiler Seam

## AE01 — Major Result Boundary

For every independently consumed Canonicalization compiler product:

- [ ] logical subject.
- [ ] producer.
- [ ] explicit inputs.
- [ ] complete result guarantee.
- [ ] legal read surface.
- [ ] unsuccessful-result boundary.
- [ ] current-validity rule.

## AE02 — Query Boundary

- [ ] query is computation/demand/dependency mechanism, not Contract authority.
- [ ] one semantic projection does not require one query.
- [ ] one query result does not define semantic identity.
- [ ] query key does not become DefinitionRef.

## AE03 — Dependency Recording

- [ ] semantic dependency is separate from compiler dependency.
- [ ] old dynamic dependency trace does not become future semantic law.
- [ ] dependencies may be rediscovered/revalidated.

## AE04 — Projection as Change-Propagation Boundary

- [ ] stable producer-owned projection may allow downstream reuse when aggregate backing changes.
- [ ] projection equality remains producer-owned.

## AE05 — Existing L1/L2

- [ ] planning-specific L1/L2 structures do not become Canonicalization architecture by analogy.
- [ ] any future Canonicalization cache owns its own result/input/validity law.

---

# AF. V2 Incremental Evolution Seam

## AF01 — No Algorithm Constitutionalization

- [ ] red/green is not Contract law.
- [ ] Salsa-like pull is not Contract law.
- [ ] DBSP/delta maintenance is not Contract law.
- [ ] Merkle early cutoff is not Contract law.
- [ ] push/pull/hybrid strategy remains replaceable.

## AF02 — Persistent Product State

- [ ] Definition projections can be persisted without changing semantics.
- [ ] compiler-derived products can be persisted under producer-owned validity rules.
- [ ] occurrence authority is not accidentally persisted/replayed as a fresh occurrence.

## AF03 — Cross-Session Reuse

- [ ] stable semantic reference seam exists.
- [ ] cross-session physical reference remapping is possible.
- [ ] cache/persistence identity stays below semantic identity.

## AF04 — Fine-Grained Dependency / Invalidation

- [ ] determinant boundaries are explicit enough to support precise invalidation.
- [ ] protocol projections can act as dependency boundaries.
- [ ] provenance invalidation can differ from semantic invalidation where legal.

## AF05 — Early Cutoff

- [ ] cutoff occurs only after producer-owned result equivalence/current-validity is re-established.
- [ ] upstream source equality/hash alone is not enough.
- [ ] hash collision cannot redefine semantic equality.

## AF06 — Incremental Repair

- [ ] changed result may be recomputed, repaired, or revalidated.
- [ ] stale derived knowledge is not consumed.
- [ ] failed repair cannot replace last valid visible generation.

## AF07 — Multiple Immutable Generations

- [ ] old readers can remain coherent.
- [ ] semantic identity is separate from generation identity.
- [ ] reclamation strategy remains implementation.

## AF08 — Lazy Materialization

- [ ] lazy physical materialization allowed after semantic completion/current validity.
- [ ] lazy semantic completion is not exposed as complete meaning.

## AF09 — Full-Rebuild Reference

- [ ] full clean path remains semantic reference.
- [ ] V2 result must agree with clean V1-compatible semantics.

---

# AG. Contract / Compiler Implementation Separation

## AG01 — Logical vs Physical Boundary

- [ ] semantic Definition does not require one JVM object.
- [ ] semantic occurrence does not require one occurrence object.
- [ ] semantic relation does not require pointer.
- [ ] semantic coordinate does not require field.
- [ ] semantic projection does not require query.
- [ ] protocol crossing does not require copy.

## AG02 — Physical Representation Freedom

- [ ] primitive arrays allowed.
- [ ] typed slabs allowed.
- [ ] dense generation-local handles allowed.
- [ ] columnar storage allowed.
- [ ] shared immutable backing allowed.
- [ ] FFM/off-heap allowed.
- [ ] mmap-backed representation allowed.
- [ ] split/fuse allowed.
- [ ] co-location allowed.
- [ ] pre-resolution allowed.
- [ ] denormalized derived caches allowed.
- [ ] eager/lazy physical materialization allowed under semantic completeness.

## AG03 — Authority Exclusion

None of the following creates Canonicalization authority:

- [ ] source file path.
- [ ] AST node.
- [ ] class object.
- [ ] generated host type.
- [ ] JVM descriptor.
- [ ] runtime object identity.
- [ ] reflection result.
- [ ] proxy/interceptor.
- [ ] dense handle.
- [ ] table row.
- [ ] HID.
- [ ] fingerprint.
- [ ] hash.
- [ ] Merkle node.
- [ ] query key.
- [ ] query graph.
- [ ] cache state.
- [ ] persistent storage key.
- [ ] allocator address.
- [ ] backend capability.
- [ ] compiler optimization knowledge.

## AG04 — Terminology Collision

- [ ] Contract Canonicalization != compiler normalization.
- [ ] Contract Canonicalization != HIR canonical form.
- [ ] Contract Canonicalization != interning.
- [ ] Contract Canonicalization != deduplication.
- [ ] Contract Canonicalization != deterministic storage encoding.
- [ ] Contract Canonicalization != content addressing.
- [ ] Contract Canonicalization != backend canonical layout.

---

# AH. Downstream Compiler Consumers

## AH01 — Admission

- [ ] Admission legal observation surface is explicit.
- [ ] selected branch Admission consumes representative.
- [ ] omitted branch Admission consumes Established Input Presentation.
- [ ] Admission Definition does not automatically acquire Canonicalization Definition identity.
- [ ] occurrence determining-basis relation is exact.
- [ ] no private normalization/parser interpretation escapes as established meaning.

## AH02 — Lowering

- [ ] Lowering consumes the same semantic relation Admission judged.
- [ ] Lowering cannot recanonicalize independently.
- [ ] Lowering cannot recover erased distinctions.
- [ ] shape-changing work remains Lowering-owned.

## AH03 — Diagnostics

- [ ] structured semantic subject available.
- [ ] exact result owner available.
- [ ] provenance separately available.
- [ ] raw Input evidence may be referenced without becoming authority.
- [ ] compiler diagnostics do not create Contract Diagnostic Evidence meaning.

## AH04 — Reference Judgment

- [ ] can consume legal producer projections.
- [ ] does not become Canonicalization authority.

## AH05 — PBT / Fixture / Coverage

- [ ] exact Contract obligation identity can be referenced.
- [ ] producer-owned equivalence/representative law is observable enough for test derivation.
- [ ] test engine does not invent semantics.

## AH06 — Optimizer

- [ ] optimizer consumes legal Established projection / derived knowledge.
- [ ] legality and profitability are separate.
- [ ] optimization cannot enlarge Canonicalization equivalence.
- [ ] optimization cannot restore collapsed distinction as semantic determinant.
- [ ] optimizer may use equivalence-class invariance only when proven.
- [ ] moving an Admission-derived predicate before Canonicalization requires proof that predicate is invariant over the Canonicalization equivalence classes it crosses.
- [ ] fusion preserves result, attribution, refusal ownership, and observable semantics.
- [ ] memoization preserves occurrence/attribution law.
- [ ] vectorization preserves law.
- [ ] allocation elimination preserves law.

## AH07 — Execution Formation

- [ ] consumes Established semantics rather than HIR/source as authority.
- [ ] pre-resolved hot path may fuse physical stages while preserving logical owner boundaries.

## AH08 — JVM Backend

- [ ] backend cannot redefine equivalence.
- [ ] backend cannot infer Contract meaning from JVM type layout.
- [ ] generated artifact remains non-authoritative.
- [ ] backend representation may change without Contract revision.

---

# AI. Verification, Reference, PBT, and Conformance

## AI01 — Independent Reference Path

- [ ] independent/reference Canonicalization judgment path exists or is structurally possible.
- [ ] production implementation is not sole oracle.
- [ ] reference path is simpler where practical.

## AI02 — Equivalence Properties

- [ ] reflexivity.
- [ ] symmetry.
- [ ] transitivity.
- [ ] same-class witness.
- [ ] different-class witness.
- [ ] boundary-domain witness.

## AI03 — Representative Properties

- [ ] source equivalent to representative.
- [ ] same class -> same representative.
- [ ] fixed point.
- [ ] idempotence.
- [ ] unique representative.
- [ ] non-equivalent class non-collapse.

## AI04 — Input Relationship Properties

- [ ] Presentation Sameness compatibility.
- [ ] Input-preserved distinction only collapses when law says so.
- [ ] occurrence identity preserved despite equal presentation/result.

## AI05 — Branch Properties

- [ ] omission != ExactCanonicalization.
- [ ] selected branch uses canonical representative.
- [ ] omitted branch preserves Input semantics.
- [ ] raw distinction resurrection regression witness.
- [ ] judgment-use coherence regression witness.

## AI06 — Refusal Properties

- [ ] legal canonicalizable noncanonical source succeeds.
- [ ] out-of-domain source refuses under owner law.
- [ ] resource stop not mislabeled Refusal.
- [ ] unavailable/corrupt/compiler failure not mislabeled Refusal.

## AI07 — Canonical Bytes Vectors

- [ ] normative bytes vectors if semantic.
- [ ] framing vectors.
- [ ] scalar vectors.
- [ ] aggregate vectors.
- [ ] version/migration vectors.
- [ ] collision/ambiguity vectors.

## AI08 — Domain-Specific Vectors

Where applicable:

- [ ] Unicode normalization.
- [ ] case folding.
- [ ] floating NaN.
- [ ] signed zero.
- [ ] decimal scale/cohort.
- [ ] temporal/zone.
- [ ] sequence.
- [ ] membership.
- [ ] association.
- [ ] duplicates.
- [ ] nested presentation.

## AI09 — Differential Matrix

- [ ] clean vs cached.
- [ ] clean vs parallel.
- [ ] clean vs persistent reload.
- [ ] clean vs incremental repair.
- [ ] physical representation A vs B.
- [ ] generated optimized vs reference.
- [ ] worker-count perturbation.
- [ ] traversal-order perturbation.
- [ ] hash-order perturbation.
- [ ] locale/timezone/environment perturbation.

## AI10 — Conformance Material Versioning

- [ ] normative expected-result change is classified as semantic law change where appropriate.
- [ ] adding test coverage without semantic change does not mint new Contract meaning.
- [ ] law version and conformance vector revision relation is explicit.
- [ ] public specification and normative vectors cannot silently diverge.

## AI11 — Traceability

Each closed semantic law should map:

```text
ADR clause
-> checklist ID
-> protocol observation
-> conformance property
-> reference/test oracle
-> executable QA
-> production implementation
```

---

# AJ. Public Specification and Law Registry

## AJ01 — Built-In Law Registry

- [ ] exact law identity.
- [ ] semantic profile/version.
- [ ] source presentation domain.
- [ ] preserved distinctions.
- [ ] collapsed distinctions.
- [ ] representative law.
- [ ] canonicalizable domain.
- [ ] refusal domain.
- [ ] semantic bounds.
- [ ] canonical bytes status.
- [ ] conformance vector set.
- [ ] external semantic data/version.

## AJ02 — Registry Authority

- [ ] registry is a compiler/frontend realization of ratified law, not an alternate source of semantic authority.
- [ ] runtime dynamic registration cannot redefine law.

## AJ03 — Published Specification

- [ ] user documentation matches authoritative law.
- [ ] generated API naming does not replace law identity.
- [ ] deprecated/retired law behavior is explicit.

## AJ04 — Change Classification

Distinguish:

- [ ] Contract semantic change.
- [ ] Canonicalization Definition identity change.
- [ ] law version change.
- [ ] HIR surface revision.
- [ ] Established Protocol revision.
- [ ] canonical-byte protocol revision.
- [ ] compiler representation-only change.
- [ ] optimizer implementation change.
- [ ] cache schema change.
- [ ] persistence schema change.
- [ ] HID/fingerprint change.
- [ ] conformance-coverage-only change.
- [ ] bug fix.
- [ ] security hardening without semantic change.
- [ ] migration requirement.
- [ ] compatibility requirement.

---

# AK. Cross-Document Impact Tracking

Do not necessarily edit all documents immediately. Record every required follow-up.

- [ ] ADR-0063 Establishment impact.
- [ ] ADR-0071 HIR impact.
- [ ] ADR-0064 Input impact.
- [ ] ADR-0065 Admission impact.
- [ ] ADR-0067 Lowering impact.
- [ ] ADR-0051 Budget impact.
- [ ] ADR-0052 Capacity impact.
- [ ] ADR-0057 Failure impact.
- [ ] compiler unsuccessful-result owner / ADR-0074 dependency status.
- [ ] ADR-0075 compiler consumption architecture impact/status.
- [ ] Diagnostic ADR family impact.
- [ ] ADR-0048 Composition impact.
- [ ] What Contract Is impact.
- [ ] compiler architecture map impact.
- [ ] V1 compiler foundation impact.
- [ ] V2 incremental architecture impact.
- [ ] Reference Judgment impact.
- [ ] PBT/fixture/coverage impact.
- [ ] optimizer legality impact.
- [ ] generated API boundary impact.
- [ ] protocol evolution impact.
- [ ] persistence/cache schema impact.
- [ ] security SADR follow-up required.

---

# AL. ADR-0066 Final Closure Gate

ADR-0066 is not closed while an applicable semantic or legal-observation item remains `OPEN`.

- [ ] Authority closed.
- [ ] Scope closed.
- [ ] selection/omission closed.
- [ ] semantic subject closed.
- [ ] equivalence law closed.
- [ ] preserved/collapsed distinction law closed.
- [ ] representative law closed.
- [ ] shape law closed.
- [ ] nested/aggregate law closed.
- [ ] authoring law closed.
- [ ] external semantic-basis/version law closed.
- [ ] HIR Definition Candidate closed.
- [ ] HIR Binding Candidate closed.
- [ ] HIR Candidate Protocol closed.
- [ ] HIR seal/lifecycle/evolution closed.
- [ ] HIR-to-Establishment handoff closed.
- [ ] Definition Establishment closed.
- [ ] Required Basis closed.
- [ ] Basis Binding closed.
- [ ] Applicability closed.
- [ ] occurrence law closed.
- [ ] occurrence result vocabulary closed.
- [ ] Established Material classification closed.
- [ ] Definition identity closed.
- [ ] World/occurrence placement closed.
- [ ] Established Semantic Protocol closed.
- [ ] Protocol consumer law closed.
- [ ] availability taxonomy closed.
- [ ] composition requirements closed.
- [ ] selected branch closed.
- [ ] omitted branch closed.
- [ ] Canonical Bytes status closed.
- [ ] Refusal/compiler-result/Failure boundary closed.
- [ ] Budget/Capacity/compiler-resource boundary closed.
- [ ] finite semantic completion closed.
- [ ] determinism closed.
- [ ] concurrency/publication closed.
- [ ] reuse/current-validity closed.
- [ ] V1 query/cache seam closed.
- [ ] V2 incremental seam closed.
- [ ] Contract/implementation separation closed.
- [ ] downstream consumers require no private semantic reconstruction.
- [ ] optimizer legality seam closed.
- [ ] Reference/PBT/conformance seam closed.
- [ ] public law registry/specification closed.
- [ ] change classification closed.
- [ ] cross-document follow-up recorded.
- [ ] Security SADR review can consume ADR results without needing to invent missing Canonicalization semantics.

---

# PART II — CANONICALIZATION SECURITY SADR CHECKLIST

> This section is intentionally separate from ADR-0066 semantic closure.
>
> Security SADR asks whether the already-defined Canonicalization architecture remains trustworthy under hostile inputs,
> malicious or buggy components, corruption, replay, exceptional states, capability leakage, and trust-boundary crossing.
>
> A Security SADR finding may reveal that ADR-0066 or another ADR is incomplete. The fix then returns to the owning ADR.
> The Security SADR must not silently create a new equivalence law, representative law, Definition identity rule,
> occurrence rule, or composition rule.

# S-A. Security Scope and Assets

## S-A01 — Protected Semantic Assets

- [ ] Canonicalization Definition meaning.
- [ ] Canonicalization Definition identity.
- [ ] occurrence identity.
- [ ] equivalence law.
- [ ] preserved/collapsed distinction law.
- [ ] representative.
- [ ] canonical bytes where semantic.
- [ ] Required Basis / Basis Binding attribution.
- [ ] result/refusal attribution.
- [ ] protocol projections.

## S-A02 — Compiler/System Assets

- [ ] HIR product.
- [ ] Established semantic backing.
- [ ] cache.
- [ ] persistent artifacts.
- [ ] dependency metadata.
- [ ] conformance vectors.
- [ ] external semantic-data bundles.
- [ ] generated artifacts.
- [ ] diagnostics/audit evidence.
- [ ] build/release artifact.
- [ ] compiler resources.

---

# S-B. Threat Model

- [ ] malicious Contract source.
- [ ] malicious Input data.
- [ ] malformed host carrier evidence.
- [ ] malicious repository content.
- [ ] corrupt HIR/persistent storage.
- [ ] poisoned cache entry.
- [ ] stale cache entry.
- [ ] malicious/buggy downstream consumer.
- [ ] malicious/buggy plugin or build tool.
- [ ] malicious runtime realization.
- [ ] concurrent race/fault.
- [ ] environmental manipulation.
- [ ] external semantic-data substitution.
- [ ] supply-chain substitution.
- [ ] out-of-scope attacker capabilities explicitly documented.

---

# S-C. Trust Boundary Inventory

- [ ] source acquisition boundary.
- [ ] host-carrier acquisition boundary.
- [ ] source -> HIR boundary.
- [ ] HIR Protocol boundary.
- [ ] HIR -> Establishment boundary.
- [ ] Established Semantic Protocol boundary.
- [ ] Input -> Canonicalization basis boundary.
- [ ] Canonicalization -> Admission boundary.
- [ ] Admission -> Lowering boundary.
- [ ] cache load boundary.
- [ ] persistence decode boundary.
- [ ] external semantic-data load boundary.
- [ ] generated artifact boundary.
- [ ] diagnostic/log boundary.
- [ ] build/release boundary.

---

# S-D. No Authority Invention

- [ ] parser cannot invent Canonicalization authority.
- [ ] recovery material cannot invent authority.
- [ ] reflection cannot invent authority.
- [ ] generated API cannot invent authority.
- [ ] cache hit cannot invent authority.
- [ ] persistent bytes cannot invent authority.
- [ ] HID/fingerprint cannot invent authority.
- [ ] runtime object cannot invent authority.
- [ ] serializer/decoder cannot invent authority.
- [ ] verifier result cannot invent authority.
- [ ] optimizer cannot invent authority.
- [ ] backend cannot invent authority.

---

# S-E. Complete Semantic Mediation

- [ ] every semantic consumer uses a legal producer result/projection.
- [ ] no raw shared-table bypass.
- [ ] no reflection bypass.
- [ ] no universal-handle bypass.
- [ ] no raw source reopening.
- [ ] no raw Input semantic bypass after selected Canonicalization.
- [ ] no cache-backing semantic bypass.
- [ ] no provenance-as-authority bypass.
- [ ] no diagnostic evidence used as semantic substitute.

---

# S-F. Judgment–Use Coherence

- [ ] Canonicalization judges the exact source material attributed to its result.
- [ ] Admission judges the exact representative relation that downstream use relies on.
- [ ] Lowering uses the exact relation Admission allowed.
- [ ] verified generation equals consumed generation.
- [ ] decoded protocol revision equals validated protocol revision.
- [ ] representative checked equals representative propagated.
- [ ] canonical bytes checked equal bytes later relied upon.
- [ ] no TOCTOU substitution between judgment and use.

---

# S-G. Single Interpretation / Differential Interpretation

- [ ] source parser and alternate parser cannot disagree silently.
- [ ] persistent decoder and clean producer cannot assign different meaning to same claimed schema.
- [ ] duplicate field/key behavior is explicit.
- [ ] unknown field behavior is explicit.
- [ ] ordering interpretation is exact.
- [ ] Unicode interpretation is exact.
- [ ] temporal interpretation is exact.
- [ ] numeric interpretation is exact.
- [ ] canonical-byte framing has one interpretation.
- [ ] reparse that creates a new semantic interpretation is treated as a new judgment boundary rather than transparent transport.

---

# S-H. Context Binding

- [ ] Definition binding.
- [ ] Contract Version binding.
- [ ] law/profile version binding.
- [ ] external semantic-data version binding.
- [ ] Input occurrence binding.
- [ ] Canonicalization occurrence binding.
- [ ] run/world binding where applicable.
- [ ] compiler generation binding.
- [ ] protocol revision binding.
- [ ] no cross-context substitution.

---

# S-I. Freshness and Replay

- [ ] stale Definition replay.
- [ ] stale occurrence replay.
- [ ] stale representative replay.
- [ ] stale canonical bytes replay.
- [ ] stale Basis Binding replay.
- [ ] stale Protocol projection replay.
- [ ] old generation substitution.
- [ ] old Version substitution.
- [ ] external semantic-data rollback.
- [ ] source edit -> edit -> revert sequences.
- [ ] replay does not become fresh occurrence authority.

---

# S-J. Monotonic Observation / Least Semantic Surface

- [ ] narrow projection cannot recover wider producer surface.
- [ ] typed reference cannot escalate to universal reference.
- [ ] physical backing address does not grant new observations.
- [ ] opaque retained extension cannot be exposed as interpreted meaning.
- [ ] provenance does not enlarge semantic authority.
- [ ] collapsed distinction cannot be recovered through a privileged implementation path and reused semantically.

---

# S-K. Fail-Closed Security Behavior

- [ ] unknown required semantic extension.
- [ ] unsupported known revision.
- [ ] corrupt HIR.
- [ ] corrupt Established backing.
- [ ] corrupt cache.
- [ ] corrupt persistent artifact.
- [ ] wrong-kind reference.
- [ ] stale generation.
- [ ] missing external semantic data.
- [ ] missing required basis.
- [ ] verifier inconclusive.
- [ ] decoder mismatch.
- [ ] resource exhaustion.
- [ ] failed migration.
- [ ] failed incremental repair.
- [ ] fallback cannot silently select weaker semantics.
- [ ] graceful degradation cannot invent success.

---

# S-L. Exceptional-State Integrity

- [ ] OOM.
- [ ] allocation refusal.
- [ ] cancellation.
- [ ] I/O failure.
- [ ] worker crash.
- [ ] partial decode.
- [ ] timeout.
- [ ] corruption discovered after partial work.
- [ ] failed cache validation.
- [ ] failed migration.
- [ ] failed incremental repair.
- [ ] partial publication attempt.
- [ ] retry/fallback preserves original result owner.

---

# S-M. Resource-Amplification Threats

- [ ] extreme nesting.
- [ ] extreme cardinality.
- [ ] oversized scalar.
- [ ] canonical expansion bomb.
- [ ] encoded/decompression expansion.
- [ ] pathological duplicate sets.
- [ ] adversarial hash distribution.
- [ ] long coordinate paths.
- [ ] cyclic physical carrier.
- [ ] integer count/range overflow.
- [ ] provenance fanout.
- [ ] diagnostic amplification.
- [ ] repeated-equivalence recomputation amplification.
- [ ] temporary-memory amplification.
- [ ] bounded parsing.
- [ ] bounded decoding.
- [ ] bounded traversal.

> The corresponding bounded compiler behavior remains in Part I because it is also compiler correctness/architecture.
> This SADR section audits deliberate adversarial exploitation of those same seams.

---

# S-N. Cache and Persistence Security

- [ ] cache poisoning.
- [ ] stale cache replay.
- [ ] cache key confusion.
- [ ] context-mismatched reuse.
- [ ] persistent schema confusion.
- [ ] persistent artifact corruption.
- [ ] persistent reference substitution.
- [ ] HID/hash collision handling.
- [ ] invalid closure reuse.
- [ ] missing negative-space invalidation.
- [ ] cross-workspace reuse conditions.
- [ ] remote-cache future threat boundary.
- [ ] clean recomputation fallback remains trustworthy.

---

# S-O. Incremental Security

- [ ] clean == incremental semantic result.
- [ ] clean == incremental security result.
- [ ] dependency omission.
- [ ] stale dynamic dependency trace.
- [ ] edit/revert sequence.
- [ ] Version switch.
- [ ] law/profile switch.
- [ ] external data version switch.
- [ ] dependency removal/addition.
- [ ] cache eviction.
- [ ] corrupted retained state.
- [ ] early-cutoff unsoundness.
- [ ] partial-repair publication.
- [ ] stale representative reuse.
- [ ] stale refusal/authorization reuse.

---

# S-P. Protocol Security

- [ ] minimum legal observation surface.
- [ ] typed reference confusion resistance.
- [ ] malformed projection rejection.
- [ ] protocol downgrade prevention.
- [ ] unknown required extension handling.
- [ ] unsupported revision handling.
- [ ] decoder version binding.
- [ ] stale protocol replay.
- [ ] universal handle non-exposure.
- [ ] raw backing non-exposure.
- [ ] opaque retention correctness.
- [ ] compatibility/migration cannot silently erase required meaning.

---

# S-Q. Confidentiality and Disclosure

- [ ] raw Input disclosure.
- [ ] representative disclosure.
- [ ] canonical-byte disclosure.
- [ ] provenance disclosure.
- [ ] rejected hostile Input logging.
- [ ] diagnostic leakage.
- [ ] cache leakage.
- [ ] persistence leakage.
- [ ] crash-dump leakage.
- [ ] trace/telemetry leakage.
- [ ] internal protocol material disclosure.
- [ ] bounded/redacted evidence strategy where needed.

---

# S-R. Verifier and TCB

- [ ] Canonicalization implementation trust assumptions.
- [ ] HIR validator trust assumptions.
- [ ] Establishment verifier trust assumptions.
- [ ] protocol decoder/checker trust assumptions.
- [ ] independent reference path.
- [ ] production implementation is not sole verifier.
- [ ] small-checker opportunity identified where useful.
- [ ] verifier timeout/inconclusive does not become success.
- [ ] checker result does not become Contract authority.
- [ ] future proof/certificate producer separated from checker.

---

# S-S. Transformation Security Preservation

- [ ] source/HIR representation transforms.
- [ ] HIR storage migration.
- [ ] Established backing migration.
- [ ] canonicalizer specialization.
- [ ] vectorization.
- [ ] memoization.
- [ ] fusion.
- [ ] Admission predicate reordering.
- [ ] Execution Formation.
- [ ] optimizer transformations.
- [ ] JVM lowering.
- [ ] backend emission.
- [ ] functional preservation.
- [ ] authority preservation.
- [ ] context binding preservation.
- [ ] refusal attribution preservation.
- [ ] security-property preservation where in scope.

> Transformation legality and Contract preservation remain in Part I. This section asks whether a transform introduces
> a security-relevant observation or weakening even when ordinary functional output looks equivalent.

---

# S-T. External Semantic Data Security

- [ ] Unicode data authenticity/integrity.
- [ ] timezone data authenticity/integrity.
- [ ] other shipped semantic tables.
- [ ] exact version binding.
- [ ] update process.
- [ ] rollback behavior.
- [ ] missing data behavior.
- [ ] incompatible data behavior.
- [ ] generated semantic-data artifact provenance.
- [ ] build-time vs runtime acquisition boundary.

---

# S-U. Build and Supply Chain

- [ ] source revision closure.
- [ ] generated sources.
- [ ] generated semantic tables.
- [ ] conformance vector files.
- [ ] Gradle/Kotlin plugins.
- [ ] annotation processors/KSP processors where applicable.
- [ ] downloaded build artifacts.
- [ ] third-party actions/scripts.
- [ ] immutable dependency references where practical.
- [ ] build manifest/provenance.
- [ ] reproducible build evidence.
- [ ] release artifact/source correspondence.
- [ ] signing/attestation boundary.
- [ ] release authority separation.

---

# S-V. Security Diagnostics and Audit

- [ ] Contract Diagnostic Evidence separated from implementation/security audit evidence.
- [ ] security event owner.
- [ ] stable event classification.
- [ ] bounded hostile-input evidence.
- [ ] sensitive material redaction.
- [ ] audit log integrity assumptions.
- [ ] cache corruption event.
- [ ] protocol downgrade/unsupported event.
- [ ] failed semantic-data verification event.
- [ ] security finding does not rewrite Contract result attribution.

---

# S-W. Security Regression Corpus

At minimum include architectural witnesses for:

- [ ] raw Admission vs canonical representative mismatch.
- [ ] raw distinction resurrection.
- [ ] stale representative.
- [ ] stale occurrence.
- [ ] wrong generation.
- [ ] wrong reference kind.
- [ ] protocol downgrade.
- [ ] corrupt cache.
- [ ] corrupt persistence.
- [ ] external semantic-data mismatch.
- [ ] resource amplification.
- [ ] partial publication after OOM/cancellation.
- [ ] incremental-only stale result.
- [ ] unsupported fallback.
- [ ] decoder differential.
- [ ] canonical byte ambiguity.
- [ ] diagnostic leakage.
- [ ] failed new generation replacing old valid generation.
- [ ] optimizer moved a non-equivalence-invariant predicate across Canonicalization.

---

# S-X. Security Assumptions and Limitations

Explicitly record whether V1 does or does not claim:

- [ ] malicious OS resistance.
- [ ] malicious JVM resistance.
- [ ] malicious hardware resistance.
- [ ] constant-time behavior.
- [ ] cache side-channel resistance.
- [ ] speculative side-channel resistance.
- [ ] full noninterference.
- [ ] distributed Byzantine resistance.
- [ ] hostile remote cache.
- [ ] compromised build service resistance.
- [ ] compromised package registry resistance.
- [ ] reproducible-build guarantee.
- [ ] artifact signing guarantee.
- [ ] external semantic-data authenticity guarantee.

Unsupported does not mean ignored. The limitation and its trust assumption must be visible.

---

# S-Y. Security Finding Routing

For every finding:

- [ ] semantic defect -> ADR-0066.
- [ ] Input semantic defect -> ADR-0064.
- [ ] Admission semantic defect -> ADR-0065.
- [ ] Lowering semantic defect -> ADR-0067.
- [ ] composition defect -> ADR-0048 / composition owner.
- [ ] Establishment defect -> ADR-0063.
- [ ] HIR defect -> ADR-0071.
- [ ] compiler-result defect -> compiler-result owner.
- [ ] Budget/Capacity/Failure attribution defect -> owning Contract ADR.
- [ ] protocol implementation defect -> Design/compiler architecture.
- [ ] optimizer defect -> optimizer legality/design.
- [ ] persistence/cache defect -> reuse/incremental design.
- [ ] verification weakness -> Verification.
- [ ] pure threat/assumption decision -> Security SADR.

---

# S-Z. Security SADR Final Gate

- [ ] assets closed.
- [ ] threat model closed.
- [ ] trust boundaries closed.
- [ ] authority-invention review closed.
- [ ] complete semantic mediation reviewed.
- [ ] judgment-use coherence reviewed.
- [ ] single-interpretation review closed.
- [ ] context binding reviewed.
- [ ] freshness/replay reviewed.
- [ ] monotonic observation reviewed.
- [ ] fail-closed behavior reviewed.
- [ ] exceptional-state integrity reviewed.
- [ ] adversarial resource behavior reviewed.
- [ ] cache/persistence reviewed.
- [ ] incremental security reviewed.
- [ ] protocol security reviewed.
- [ ] confidentiality/disclosure reviewed.
- [ ] verifier/TCB reviewed.
- [ ] transformation security preservation reviewed.
- [ ] external semantic data reviewed.
- [ ] build/supply-chain reviewed where in scope.
- [ ] security diagnostics/audit reviewed.
- [ ] regression corpus defined.
- [ ] assumptions/limitations documented.
- [ ] every finding routed to an owner.
- [ ] SADR did not silently invent Canonicalization semantics.

---

# PART III — RE-AUDIT ADDITIONS FOUND DURING THIS PASS

The following areas were strengthened after re-reading the current project documents. They should not be dropped when
the checklist is shortened later.

## R-A. External Semantic Data Is a First-Class Closure Question

The current Input work already distinguishes fixed semantic law material from ambient provider state. Canonicalization
needs the same explicit check for Unicode, timezone, collation, numeric, URI, and other versioned external data. The
checklist must decide whether each item is a Definition determinant, separately Established semantic basis, or
implementation-only data. "Library version happened to be X" is not sufficient.

## R-B. Definition Law vs Exact Upstream Definition

The current Admission rework deliberately avoids making one exact Input Definition a reusable Admission Definition
determinant merely because Admission later consumes Input occurrences. Canonicalization must make this decision explicitly
rather than inheriting the old ADR-0066 coordinate-binding model. Required source observation law and actual composition
binding must remain separable unless Canonicalization semantics proves otherwise.

## R-C. Result Availability Must Be Separate from Semantic Absence

The common checklist and current compiler-result architecture distinguish semantic absence from not-retained,
not-materialized, unsupported, stale, corrupt, and unavailable compiler states. Canonicalization Protocol must carry the
same distinction so a missing backing never fabricates `Refused` or `no Canonicalization`.

## R-D. Lifecycle / Retention / Reclamation Need Canonicalization-Specific Answers

Established Definition meaning, occurrence meaning, representative backing, and canonical-byte backing may have different
physical lifetimes. The ADR must define semantic placement and legal observation while leaving storage lifetime replaceable.

## R-E. Optimizer Reordering Needs an Equivalence-Class Invariance Rule

The logical pipeline may be physically fused or optimized, but moving a predicate from after Canonicalization to before it
is legal only when the predicate is proven invariant over every Canonicalization equivalence class it crosses. This is
compiler correctness even before it is treated as a security concern.

## R-F. Semantic Bounds Must Be Split from Compiler Resource Bounds

Current ADR-0066 mixes finite semantic law, work bounds, Budget, Capacity, and implementation safety. The rework must
separate:
- meaning-defining domain/output bounds,
- Budget/Capacity-owned Contract limits,
- compiler resource envelopes and implementation boundedness.

## R-G. Normative Conformance Material Is Part of Law Governance, Not Merely QA

A built-in Canonicalization law must specify how normative vectors relate to law identity/version. Changing a normative
expected result may be a semantic change; merely adding attack cases or broader implementation tests need not be.

## R-H. ADR-0074 / ADR-0075 Ownership Status Must Be Re-Audited

The common checklist was written while compiler unsuccessful-result ownership depended on ADR-0074. Current project
material also contains ADR-0075 downstream compiler-result consumption work. Before final ADR-0066 acceptance, use the
actual current Accepted/Proposed status and owner instead of copying an old document number mechanically.

## R-I. Negative-Space / Closure Validation Matters for Canonicalization Law Sets

Where a Definition or Binding claims complete coordinate coverage, reuse validation must establish not only that old
members remain equal but that no new legal member has appeared and no member has disappeared. This is required for safe
incremental reuse and cannot be replaced by validating only surviving rows.

## R-J. Canonical Bytes Need Their Own Stability Domain

Canonical Representative semantics, Contract Version, law version, canonical-byte protocol revision, persistent encoding
revision, HID/fingerprint algorithm, and backend artifact format must not be collapsed into one version coordinate.

## R-K. Protocol Is a Logical Observation Boundary, Not a DTO Mandate

Both HIR Candidate Protocol and Established Semantic Protocol must be closed semantically, while physical implementation
may use shared immutable slabs/tables/primitive arrays and may fuse storage. This is necessary both for V1 performance and
for V2 persistence/incremental work.

## R-L. Security Duplication Is Intentional Only Where the Base Architecture Already Needs the Property

Items such as deterministic recomputation, current-validity proof, immutable complete publication, bounded compiler work,
typed-reference safety, protocol narrowing, independent reference paths, and transform preservation remain in Part I.
Part II re-evaluates them only under hostile conditions. Pure threat assumptions, confidentiality, supply-chain policy,
security audit policy, and attack-specific assurance remain SADR-owned.

---

# PART IV — RECOMMENDED DECISION ORDER FOR ADR-0066

Do not answer the entire checklist at once. Close the semantic core in dependency order.

```text
1. Authority / subject / scope
2. Definition meaning and determinant set
3. Input-observation requirement
4. Equivalence law
5. preserved / collapsed distinction law
6. representative law
7. shape and nested-presentation law
8. Definition identity / Version / external semantic basis
9. HIR Definition Candidate
10. HIR Binding Candidate
11. HIR Candidate Protocol
12. HIR seal / handoff
13. Definition Establishment
14. Required Basis / Basis Binding / Applicability
15. occurrence law
16. occurrence judgment / result vocabulary
17. Established Material classification
18. World placement
19. Established Semantic Protocol
20. selected / omitted composition requirements
21. Canonical Bytes semantic status
22. Refusal / Budget / Capacity / compiler-result boundary
23. determinism / finite completion / concurrency
24. reuse / cache / V1 query seam
25. V2 incremental seam
26. optimizer / downstream-consumer legality
27. Reference / PBT / conformance
28. public law registry / change classification
29. cross-document impact record
30. ADR-0066 closure gate
31. separate Canonicalization Security SADR review
32. route SADR findings back to owners
33. final reverse audit from consumers back to Canonicalization
```

---

# PART V — FINAL REVERSE AUDIT

After ADR-0066 decisions are written, trace the result in both directions.

## Semantic formation direction

```text
Authored Canonicalization Material
-> frontend acquisition / resolution
-> Canonicalization HIR Definition Candidate
-> Canonicalization HIR Binding Candidate
-> Resolved HIR Candidate Protocol
-> Canonicalization Definition Establishment
-> Established Canonicalization Definition Material
-> Canonical Contract World
-> Established Canonicalization Semantic Protocol
-> occurrence Required Basis / Basis Binding / Applicability
-> Canonicalization Occurrence
-> Canonical Representative
-> Admission
-> Lowering
```

## Consumer reverse direction

For each consumer:

- [ ] Admission can operate using only legal Canonicalization/Input protocol observations.
- [ ] Lowering can operate without reopening raw material that is no longer semantic.
- [ ] Diagnostics can explain results without inventing meaning.
- [ ] Reference Judgment can reproduce the owning law independently.
- [ ] PBT can derive obligations from producer-owned semantics.
- [ ] optimizer can prove legality without treating implementation representation as authority.
- [ ] cache/reuse can validate current result without redefining equality.
- [ ] V2 incremental machinery can track dependencies without becoming Contract law.
- [ ] backend can realize the result without reconstructing Contract meaning.
- [ ] Security SADR can model threats without discovering that core semantic ownership is still undefined.

If any consumer must privately reconstruct missing Canonicalization meaning, ADR-0066 is not closed.
