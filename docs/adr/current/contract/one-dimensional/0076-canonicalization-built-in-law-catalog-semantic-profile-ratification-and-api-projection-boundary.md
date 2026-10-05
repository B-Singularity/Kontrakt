# ADR-0076: Canonicalization Built-In Law Catalog

## Status

Accepted

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

The remaining architectural question is what it means for a Canonicalization law Authority to belong to the built-in
vocabulary and what semantic closure any admitted law must satisfy. That decision cannot be left to implementation
because built-in membership makes an exact Contract Authority available as part of the Kontrakt-provided vocabulary.

The word canonicalization is broader outside Kontrakt than the authority defined by ADR-0066. Catalog membership
therefore follows Kontrakt's semantic boundary rather than terminology used elsewhere.

The catalog must cover common representation problems without turning Canonicalization into an executable extension
point. It must also leave a path for specialized domains whose representative depends on an explicit semantic basis.
Other transformations remain separate unless they satisfy the Canonicalization law itself.

This ADR defines the Catalog boundary and the meaning of Built-In Law Authority membership. It also defines the
qualification law that every admitted law must satisfy.

Concrete laws are handled outside this ADR. Their qualification records and exact specifications belong to the
Canonicalization Catalog design and specification work.

API projection is downstream. Implementation and verification are downstream as well.

---

# 2. Problem

Kontrakt must avoid both an under-specified catalog and an over-broad one.

If V1 defines only the authoring mechanism, a selected nominal law has no complete exact semantic target.

The opposite failure occurs when a broad law name leaves more than one legal interpretation or representative open.
The law itself must close that ambiguity. An implementation mechanism cannot choose the missing meaning.

The same discipline applies to external standards. A referenced standard may still leave a Contract-visible choice
open. Kontrakt must close that choice whenever it can change the representative or a Canonicalization-owned refusal.

Deterministic byte production is a separate concern unless exact bytes are themselves the representative owned by the
selected Contract law. This ADR does not create a user-facing canonical-byte facility merely because other ecosystems
use the word canonicalization for signing or serialization.

This ADR decides the qualification boundary for built-in laws without becoming a mutable registry of candidates.
Candidate evaluation and Catalog population proceed in separate design and specification work.

Candidate visibility does not establish admission. Admission requires an explicit Catalog decision under Section 5.

A change to the Catalog architecture or qualification law requires a new ADR decision.

# 3. Decision Drivers

Determinism is the first qualification requirement. The same law-owned determinants and the same legal Input must
produce the same Canonicalization-owned outcome under every legal realization.

That rule requires complete determinant closure. No undeclared ambient or realization state may finish the law.
External semantic material must be explicit whenever it can change the Contract-visible result.

The Catalog must preserve the authority boundary established by ADR-0066. A built-in law establishes one representative
under one exact equivalence relation.

Usefulness does not make another operation Canonicalization. A transformation belongs here only when it satisfies that
Canonicalization law.

A law must remain understandable without its implementation. API names and backend strategies may change while the law
remains the same. Performance work may exploit established semantic properties, but target cost or implementation
convenience cannot choose a different representative.

A built-in law may be admitted only when its semantics can remain intact under the common finite-work and
resource-authority
boundaries and its conformance can be checked independently of one implementation. Specialized domains remain possible
when their semantic dependencies and authority boundaries are explicit.

# 4. Decision

Kontrakt will maintain a **Canonicalization Built-In Law Catalog** as the logical boundary for the built-in
Canonicalization vocabulary defined by this ADR.

The Catalog is a normative manifest over the built-in Canonicalization law Authorities admitted by Kontrakt. It is not
defined by one physical record or table.

The compiler may realize the manifest together with law material or keep them physically separate. Either choice is an
implementation decision. Physical layout does not merge semantic meaning or create Contract authority.

The current boundary is:

```text
Canonicalization Built-In Law Catalog
    Built-In Law Authority Membership

Exact Built-In Law Authorities admitted under this ADR
    one independently version-sensitive Contract Authority per exact law
    one exact law Definition for each Version of that Authority
```

The Catalog itself is not another version-sensitive Contract Authority. It therefore has no `CatalogVersion` and owns
no law Version history. It also does not choose an implicit current Version.

Built-In Law Authority Membership answers only whether a law Authority belongs to the built-in Canonicalization
vocabulary. A new Version under an already-admitted Authority does not change that membership.

Admitting a new built-in law Authority requires an explicit Catalog decision. Membership cannot arise from compiler or
provider state, and publishing another law Version does not create a new member.

Once admitted, membership is historical and monotonic.

The membership cannot later be used to rewrite history. In particular:

```text
it is not deleted
it is not reassigned
it is not reused for another semantic subject
```

A separately owned rule may later prohibit a particular use of that Authority or one of its Versions. Lifecycle,
Governance, selection, or support rules may own such a prohibition. The prohibition does not rewrite historical Catalog
membership. Exact law identity and Version remain owned by the member law Authority under ADR-0053 and ADR-0063.

ADR-0066 owns the common semantic shape of Canonicalization. ADR-0076 owns Catalog membership and the qualification law
for built-in admission. It also fixes the minimum semantic closure that an admitted law must provide.

Each Exact Built-In Law Authority owns its own exact versioned meaning. Its normative specification expresses that
meaning. The Catalog admission record states membership. Neither is compiler implementation.

The Catalog does not own host-language names or implementation algorithms. A candidate remains outside the Catalog
until an explicit admission decision establishes membership.

Compiler artifacts may represent or realize an admitted law. Verification material may test it. Neither can supply
missing Contract meaning or establish Catalog membership.

# 5. Catalog Law Qualification

Catalog admission is stricter than showing that one normalization routine is useful. A built-in law must close its
semantic meaning without borrowing missing meaning from implementation.

## 5.1. ADR-0066 Determinism Qualification

ADR-0066 owns the universal determinism law for Canonicalization. A proposed built-in law is ratifiable only when its
exact semantics close every determinant needed to preserve that law without relying on ambient or physical state.

The proposed law must close every semantic determinant that can change a law-owned observation.

The mandatory closure is defined by Section 6.1.1. Determinism must cover the exact equivalence relation and the
required
representative. It must also preserve representative coverage and any Canonicalization-owned failure meaning.

An external semantic source belongs to the law only when it can change that meaning. Ambient realization state cannot
complete the law.

A referenced standard may leave a semantic branch open. If that branch can change a Contract-visible observation,
Kontrakt must close it before ratification. The resulting meaning belongs to the exact law rather than to an open
property bag.

## 5.2. Exact Semantic Profile Closure

ADR-0066 owns the common Canonicalization law. ADR-0076 does not restate those common rules. This ADR defines only the
law-specific semantic content that must be closed before a built-in law can be admitted.

Every built-in candidate must identify the exact already-established Input-owned semantic presentation over which the
law operates. A host representation cannot substitute for that semantic operand requirement.

Every built-in candidate must define its exact same-meaning relation and its exact representative selection. These are
the candidate-specific `E_L` and `C_L` required by ADR-0066. Neither may be inferred from implementation behavior.

Every built-in candidate must also close its **Exact Representative Coverage**. The law must state whether every legal
operand admitted by its Exact Operand Requirement has a representative under the law, or whether representative
establishment is restricted for some otherwise legal operands. Total coverage and restricted coverage are both explicit
semantic statements; absence of failure text does not implicitly mean total coverage.

When representative coverage is restricted, the law must state the exact Canonicalization-owned condition that prevents
representative establishment. Any Contract-visible distinction among negative results must already be part of the law.
Diagnostics may explain that meaning but must not reconstruct it from implementation behavior.

ADR-0066 continues to own the common failure boundary. ADR-0076 does not introduce a generic failure field.

A law may require semantic material beyond the Input-owned operand meaning. That material belongs to the exact law only
when it can change a Contract-visible result of that law.

Implementation metadata does not become a semantic determinant merely because a realization uses it.

A built-in law carries only the Input-owned distinctions that it actually observes. Specialized meaning remains local to
the law that needs it. V1 therefore does not define one universal optional-field record for every possible domain.

The law need not duplicate meaning that is already derivable from its exact representative definition. In particular,
ADR-0076 does not require a separate `Representative Range` field.

Law-specific meaning also cannot be hidden in a generic property bag.

Canonicalization selects a representative of already-declared meaning. A transformation that acquires or changes meaning
belongs to another authority.

## 5.3. Semantic Preservation Under Resource Boundaries

ADR-0066 owns the common finite-work and authority boundaries of Canonicalization. Catalog admission does not permit a
built-in law to weaken or rewrite its semantics in order to fit one realization.

A semantic bound belongs to an Exact Built-In Law only when changing that bound would change the law's Contract-visible
meaning. A realization limit does not become Canonicalization meaning merely because the implementation needs it.

Resource protection must preserve the admitted law. It cannot change any Contract-visible semantic result owned by that
law.

For example, resource protection cannot substitute another `C_L` for the selected representative or narrow `E_L`.
Coverage and Canonicalization-owned failure meaning must also remain unchanged.

Concrete performance and hostile-input engineering belongs to Design or Verification.

## 5.4. Evolution Closure

A ratified built-in law must remain stable for the semantic observations promised by its exact versioned identity.
Implementation and projection may change across Kontrakt releases without changing that meaning.

Each Exact Built-In Law is an independently version-sensitive Kontrakt Contract Authority. If a change can alter that
Authority's Contract-visible meaning, the changed meaning cannot remain under the same Version.

ADR-0053 owns Version identity and immutable history. It also owns continuity, conflict, and claim resolution. When the
same law Authority continues with changed meaning, it must do so under an explicit new Version rather than by mutating
the old one.

A later Version under the same law Authority does not revise Catalog membership and does not replace an earlier Version.
An exact Version request must resolve to that exact Authority and Version when its use is otherwise legal.

Another Contract may prohibit the requested use before consumption. ADR-0076 does not define that prohibition. If the
resolved material does not match the requested Authority and Version, resolution fails.

Version resolution has no implicit fallback. The following forms are therefore forbidden:

```text
current
latest
preferred
nearest
silent upgrade
```

Law Version history is not Catalog history. The Catalog records Authority membership while ADR-0053 preserves immutable
law Versions.

A compiler or publication snapshot may record the exact manifest used by one artifact. The snapshot may support
reproducibility and integrity.

That snapshot is not Contract Version meaning and does not become another law-history authority.

Meaning-determining external semantic material cannot drift through ambient state. If an external distinction can alter
the Exact Built-In Law's Contract-visible meaning, the law must make that dependency explicit.

A new external release does not by itself change Contract meaning. The name or release label of an external standard is
not semantic material by itself. Realization metadata does not become semantic Authority merely because it identifies
the
material in use. An undeclared ambient revision cannot change an already versioned law.

When future or previously unknown material changes an observation owned by the law, the law must already state enough
meaning to preserve deterministic interpretation. An implementation fallback cannot invent that semantic rule.

The Catalog must avoid accidental promises about realization. Stable law meaning does not make implementation detail
part of the semantic contract.

## 5.5. Built-In Suitability and Authority Uniqueness

Catalog admission requires more than a valid Canonicalization relation. A Built-In Law Authority must express reusable
semantic meaning that Kontrakt can legitimately provide as part of its own vocabulary.

Private convention does not become Kontrakt built-in authority merely because it is deterministic. Application-owned,
tenant-owned, and organization-owned meaning remain with those owners.

A new Built-In Law Authority must represent one independently owned semantic subject. A different name or realization
does not by itself justify another Authority. The same is true when the current observable meaning happens to be equal.

Several names may resolve to one existing law Authority. Such aliasing does not create another Catalog member.

Semantic equality does not merge independently identified Authorities. Alias relations must be explicit rather than
inferred from an equality test or implementation coincidence.

Two subjects that can evolve independently under distinct Authorities are not aliases. They remain distinct even when
one current Version has equal observable meaning.

# 6. Catalog Logical Content and Semantic Families

The Catalog is a normative manifest boundary over built-in law Authorities. It is not a universal semantic record and
it does not own the Version history of its members.

## 6.1. Catalog Logical Content and Exact Law Definition Boundary

The Catalog manifest owns one logical relation:

```text
Built-In Law Authority Membership
```

`Built-In Law Authority Membership` answers which independently owned Exact Built-In Law Authorities belong to the
Kontrakt built-in Canonicalization vocabulary after qualification.

Membership identifies the law Authority rather than one preferred Version. A compiler or authoring identifier may point
to that Authority, but it does not substitute for semantic identity.

Examples of non-authoritative identifiers include:

```text
candidate review labels
host-language symbols
API names
compiler handles
persistent keys
physical lookup keys
Catalog publication snapshots
```

Each Exact Built-In Law Authority is independently version-sensitive under ADR-0053. In the current model, one Authority
owns one complete Exact Built-In Law Definition for each Version. No additional Authority-Local Definition Coordinate is
needed merely to distinguish laws inside the Catalog.

ADR-0063 therefore resolves an exact law reference through the law Authority and requested Version. A different
Authority
or Version denotes a different exact versioned Definition even when some semantic material compares equal.

A new Version of an existing member Authority does not add another Catalog member. Earlier Versions remain Version-owned
historical meaning rather than historical Catalog entries.

The Catalog does not redirect an earlier Version request. It also does not duplicate the Version law already owned by
ADR-0053.

Every admitted Exact Built-In Law Authority owns the exact semantic meaning needed to close that law under ADR-0066.
Its normative law specification expresses that versioned meaning.

Neither the Catalog nor ADR-0076 becomes a versioned super-authority over those definitions.

Downstream API and compiler concerns remain outside exact law meaning. Verification remains outside exact law meaning as
well.

### 6.1.1. Exact Law Common-Core Shape

An Exact Built-In Law contains only the law-specific semantic material needed to define that law under the common
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

ADR-0066 remains the owner of the common Canonicalization obligations. Those obligations apply to the law-specific
content above and are not duplicated by the Catalog.

The exact operand requirement is semantic rather than physical. A host carrier type does not by itself define the
operand meaning of a built-in law.

The exact equivalence definition and exact representative definition are independently normative law content. ADR-0066
requires both, and implementation behavior cannot substitute for either one.

Exact Representative Coverage is normative rather than inferred from implementation behavior. A total law states that
every legal operand has a representative. A restricted law states the exact semantic boundary at which representative
establishment fails.

Restricted coverage does not reopen law selection at occurrence time. It also does not authorize silent pass-through or
a different Canonicalization law. An unresolved representative obligation cannot be delegated to another 1D Contract.

When coverage is restricted, this Catalog records only the particular semantic condition or conditions under which the
selected exact law reaches the Canonicalization failure boundary owned by ADR-0066. If several negative reasons are
Contract-visible, they must be closed as law meaning; diagnostics may explain those reasons but do not invent them.

Law-specific semantic determinants are not a generic `determinants` collection. Only material that can actually change
the law's exact meaning belongs here.

Realization and verification metadata remain outside exact law meaning unless a separate Contract law gives them
semantic
significance.

The complete Exact Built-In Law Definition Meaning consists of the four mandatory contents above plus any conditional
Failure meaning or semantic determinant that the law actually owns. A Contract-visible change to that complete meaning
is subject to Section 5.4 and ADR-0053.

Equal Definition Meaning does not merge distinct Authority or Version identities. It also does not merge Definition
References.

A separate mandatory representative-range field is not required when it is derivable from the exact representative
definition. Preserved or collapsed distinctions may be documented to explain and verify a law, but they do not become a
second authoritative copy of the exact equivalence definition.

### 6.1.2. Law-Specific Semantic Closure

The common core must not force every law to carry irrelevant semantic categories. At the same time, a built-in law may
not leave an observed distinction unowned merely because that distinction is specialized to one domain.

Every Contract-visible distinction observed by an Exact Built-In Law must belong to one of the exact semantic categories
defined in Section 6.1.1. No observed distinction may remain semantically unowned.

A law does not acquire semantic categories that it does not observe. Conversely, domain-specific meaning cannot be left
to host behavior merely because it is specialized.

If a required distinction fits none of the categories in Section 6.1.1, the semantic shape or ownership boundary must be
re-examined. A generic catch-all bag is not the remedy.

ADR-0076 creates no parallel open-ended law-specific semantic authority. Compiler representation may still use typed
structures suited to the domain, but those structures do not create another Contract semantic category.

# 7. Catalog Population and Exact Law Specification Boundary

This ADR does not enumerate the concrete Built-In Law Authority population and does not make candidate review material
part of the Catalog architecture.

Concrete candidate work is maintained in separate Canonicalization Catalog design and specification material. That work
owns the candidate qualification record and the concrete membership record. Exact law specifications are maintained
there as normative specification work, while deferred candidates remain review material.

A working candidate label has no Contract authority merely because it appears in those documents.

Every concrete admission must apply Section 5 and preserve the exact-law shape in Section 6. The admission record must
identify the independently owned law Authority being admitted. The corresponding Exact Built-In Law Authority owns its
own versioned meaning; the Catalog records historical membership of that Authority and does not duplicate the law's
Version history.

A new law that satisfies the existing Catalog law does not require a revision of this ADR. ADR-0076 is not a registry
changelog.

A change to Catalog membership semantics or to the Authority and Version boundaries requires a new ADR decision. The
same is true when the qualification law itself changes.

Public API projection and compiler realization remain downstream work. Verification remains downstream as well.

Those layers may expose or realize admitted law meaning. Verification may test that meaning. None of these activities
can
establish Catalog membership or fill missing semantic meaning.

# 8. Canonical Bytes Are Outside This Catalog

This ADR does not create a Canonical-Byte Catalog class or a user-facing byte canonicalization facility.

Exact deterministic bytes belong to the authority that gives those bytes semantic significance. Requiring one exact
encoding for another purpose does not by itself create an inbound Canonicalization law.

A coordinate Catalog Law owns bytes only if those bytes are themselves the representative declared by that law.
Otherwise deterministic serialization remains with its protocol or implementation owner.

Compiler-internal deterministic encoding remains compiler realization. It is not Canonicalization authority.

# 9. Generic Laws That V1 Will Not Publish

V1 will not publish a generic law when its name hides unresolved equivalence. The rule applies regardless of domain: the
exact same-meaning relation must be closed before a built-in profile exists.

The reason is semantic, not cosmetic. A broad law name must not hide distinctions that change equivalence.

For example, one generic email canonicalizer would be ambiguous when different parts of the address obey different case
rules. URI and filesystem-path profiles can have analogous domain-specific ambiguity.

---

# 10. No Identity or Preserve-Everything Laws

The catalog does not contain `ExactCanonicalization` or another profile whose only effect is to preserve every
Input-established representation distinction. Omission already expresses that Canonicalization does not apply.

Earlier preserve-everything candidates therefore do not become V1 laws. A particular already-canonical input may still
pass through a selected law without physical change; that is different from publishing universal identity as a
Canonicalization authority.

---

# 11. Ordering, Validation, and Refusal Are Not Automatically Canonicalization

Ordering alone does not establish a representative and therefore is not Canonicalization by itself. A reject-only rule
is also not Canonicalization merely because it constrains the same domain. The Contract that owns legality must decide
that rejection.

Collection ordering follows the same distinction. If source order is Contract-visible and a selected law declares it
irrelevant, deterministic representative selection may be Canonicalization. If the semantic domain was already
unordered, sorting physical storage is compiler representation preparation.

The catalog never infers semantic order from JVM collection iteration.

---

# 12. Aggregate Law Boundary

A catalog law may apply to one coordinate whose presentation domain is a closed aggregate. The complete aggregate
profile remains one law bound to that coordinate; its children do not become independent 1D Contracts.

V1 does not expose arbitrary recursive law composition. A built-in aggregate profile may be added only after the
complete aggregate equivalence and representative are defined, preventing the catalog from becoming a normalization
programming language.

---

# 13. Canonicalization Contract Integration Boundary

Canonicalization-specific HIR and Establishment semantics are outside ADR-0076. Their ownership remains with ADR-0066
and the common architecture defined by ADR-0071 and ADR-0063. This Catalog ADR does not define those schemas.

A Canonicalization Definition is not the same semantic subject as one built-in law. Runtime or Establishment relations
do not become Catalog membership.

Each Exact Built-In Law remains its own version-sensitive Contract Authority. ADR-0053 and ADR-0063 own exact reference
and Version resolution. ADR-0076 adds no Catalog-specific Version mechanism or law history.

A consumer that requires an exact law Version must resolve that exact Version under the member law Authority. A separate
selection or lifecycle rule may prohibit the use, but that refusal belongs to the rule that owns it.

Otherwise the historical Version remains eligible for exact resolution. A different Authority or Version cannot satisfy
the request as a substitute.

# 14. External Semantic Material Boundary

An Exact Built-In Law may depend on semantic material that originates outside Kontrakt. If that material can change a
Contract-visible observation owned by the law, the dependency must be explicit. Ambient external state cannot complete
or change the law.

Only meaning-determining external material belongs to Exact Built-In Law Definition Meaning.

A name or provenance record does not become law meaning merely because it identifies external material. Verification
evidence remains evidence rather than semantic authority.

ADR-0076 does not prescribe one universal mechanism for external semantic material. The concrete treatment of an
external source belongs to the law's Design and Verification work unless a resulting distinction is itself Contract-
visible meaning.

If a future Exact Built-In Law uses the common Required Basis architecture, that relation remains owned by ADR-0063 and
ADR-0066. ADR-0076 creates no Catalog-specific Basis mechanism. Basis-related runtime meaning does not become Catalog
membership.

# 15. Migration and Supersession

This ADR does not supersede the Canonicalization authority defined by ADR-0066. It supplies the built-in semantic
Catalog
work that ADR-0066 intentionally leaves to this document.

With this ADR accepted, older candidate-catalog material in ADR-0048 migration history and earlier ADR-0066 revisions
becomes historical exploration rather than active V1 direction.

The active authoring relation remains:

```text
IDL selects one Canonicalization declaration
    ↓
declaration names selected Input coordinates
    ↓
each selected coordinate resolves to one exact admitted built-in semantic target
```

An unselected coordinate remains outside Canonicalization. The IDL continues to select the Canonicalization declaration
rather than selecting a built-in law directly. Omission does not insert `ExactCanonicalization`, and the declaration
does
not contain an executable user canonicalizer.

Project documentation should migrate to one active statement of this relation.

ADR-0066 retains the common Canonicalization law and its HIR/Establishment ownership. ADR-0076 retains the Built-In
Catalog membership and qualification laws closed here.

Each Exact Built-In Law Authority owns its exact versioned meaning. Concrete law specifications and Catalog population
remain outside this ADR.

# 16. Consequences

The exact built-in law shape remains explicit without becoming a universal property bag. Every exact law closes the four
mandatory semantic contents defined in Section 6.1.1. Conditional Failure meaning and semantic determinants appear only
when the particular law actually owns them.

Common Canonicalization obligations remain common. Specialized domain meaning enters an exact law only through the
semantic category that owns its effect. No parallel catch-all authority is created.

Built-In Law Authority Membership cannot drift with realization state. Once admitted, membership is historical and
monotonic. Later use restrictions do not erase that admission.

Each Exact Built-In Law Authority owns its own Contract Versions. A Contract-visible semantic change requires a new law
Version rather than a Catalog Version or mutation of an older meaning. A law Version revision does not change Catalog
membership.

V1 still does not expose arbitrary user-composed normalization pipelines. One selected coordinate resolves to one closed
built-in semantic law. General custom law support and arbitrary composition remain separate future design problems.

# 17. Closure and Follow-On Work

No Catalog-architecture semantic question remains open in this ADR.

Concrete candidate work proceeds in separate Canonicalization Catalog design and specification documents under Sections
5 through 7. That work owns exact law qualification and concrete Catalog population.

API projection, implementation, performance engineering, and verification remain downstream work.

A concrete law may be admitted without modifying this ADR when the admission preserves the Catalog law decided here.
Only a change to the Catalog architecture or qualification law requires a new ADR decision.

This ADR is therefore closed as the architectural and semantic constitution of the Canonicalization Built-In Law
Catalog.