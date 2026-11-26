# ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse

## Status

Accepted

## Date

2026-09-12

## Related

- `/what-contract-is.md`
- `/0046-idl-first-interface-contract-frontend-1d-catalog-backend-discipline.md`
-

`/0047-one-dimensional-contract-presentations-pipeline-slot-selection-and-backend-realization-boundary.md`

- `/0053-version-contract-sovereign-meaning-identity-version-claims-and-authority-boundary.md`
- `/0063-contract-establishment-occurrence-applicability-and-semantic-dependency.md`
- `/0064-input-contract-explicit-boundary-presentation.md`
- `/0065-admission-contract-continuation-judgment.md`
- `/0066-canonicalization-contract-stable-representative-and-canonical-bytes.md`
-

`/0067-lowering-contract-explicit-relation-compiler-derived-realization-and-core-entry.md`

- `/0068-fact-contract-explicit-immutable-core-information-sameness-and-vocabulary.md`
- `/0069-invariant-contract-fact-local-standing-integrity-law.md`
- `/0074-compiler-result-explicit-unsuccessful-result-recovery-and-observation-boundary.md`
- `/kontrakt-compiler-total-architecture-map-design-draft.md`
- `/kontrakt-compiler-material-and-ir-architecture-review-checklist.md`
- `/kontrakt-established-contract-world-architecture-todo.md`
- `/kontrakt_IR_subsystem_contract_implementation_separation_discussion.md`
- `/Kontrakt_Query_Oriented_Compiler_and_Object_Free_Core_Design.md`
- `/kontrakt-compiler-reuse-incremental-v1-v2-todo.md`
- `/kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`
- `/kontrakt-v2-reference-architecture-and-v1-foundations.md`
- `/kontrakt-v2-incremental-architecture-research-todo.md`
- *Modern Compiler Architecture 01-15*

---

## 1. Context

Kontrakt accepts `.kontrakt` source and selected immutable 1D carrier source as frontend inputs. Downstream consumers
must not reinterpret these sources independently.

ADR-0063 defines the boundary following Resolved Contract HIR:

```text id="9s8z8w"
Resolved Contract HIR
    ↓
Authority-Owned Establishment
    ↓
Established Definition Material
    ↓
Canonical Contract World
```

This ADR defines the preceding boundary. **Resolved Contract HIR** must provide exact candidate meaning without frontend
ambiguity, allowing Establishment to apply the owning Contract law without reinterpreting source material. HIR itself
does not hold Contract authority.

This ADR defines HIR's semantics, visibility, and lifecycle. Compiler implementation and reuse mechanisms are outside
its scope.

---

## 2. Problem

Two opposite failures undermine the frontend boundary:

1. **Incomplete HIR.** Downstream consumers must independently resolve source meaning, potentially producing
   inconsistent interpretations.
2. **Overloaded HIR.** Unrelated compiler state becomes part of HIR, allowing its physical representation to govern
   invalidation, lifetime, or authority.

HIR must provide complete candidate meaning without claiming Contract authority. It must also support precise reuse
through stable semantic units and coherent generations, independently of physical storage or any particular query or
database architecture.

Cache reuse, parallel scheduling, and incremental repair must preserve the same resolved meaning as clean deterministic
computation.

---

## 3. Decision Drivers

- **Semantic fidelity.** HIR must preserve every resolved candidate-semantic distinction required by Establishment.
  Authored differences may be discarded only when they no longer affect candidate meaning.

- **Single interpretation.** Downstream consumers must not re-examine source or host-carrier structure to recover
  meaning already resolved by the frontend.

- **Coherent publication.** HIR construction may be mutable, but consumers must observe only fully resolved generations.

- **Independent reuse.** HIR must support fine-grained products without tying semantic identity to physical storage or
  query structure. V1 requires query-oriented reuse, while V2 must remain extensible to persistent, cross-session, and
  domain-specific incremental mechanisms without changing HIR semantics.

- **Determinism.** Correctness and accepted Contract law are non-negotiable. Among valid implementations, determinism
  takes priority over performance and incremental efficiency. Any implementation that changes the observable HIR result
  compared with clean deterministic computation must be rejected.

---

# 4. Decision

## 4.1. Resolved Contract HIR Role

Resolved Contract HIR is the Contract-side high-level compiler-semantic representation produced after frontend
resolution and before Authority-Owned Establishment.

```text
Contract Authoring Material
    ↓
parse / acquire
    ↓
resolution / semantic formation
    ↓
Resolved Contract HIR
    ↓
Authority-Owned Establishment
```

"High-Level" refers to the semantic level of Contract meaning, which constrains downstream realization compilation. It
does not refer to proximity to user implementation source.

HIR owns **fully resolved candidate meaning**, not Contract authority. Resolution makes a candidate exactly
interpretable, but its owning Contract law may still reject it.

User realization processing belongs to separate compiler products and does not extend Contract HIR.

HIR is a distinct IR level because it establishes semantic invariants and an information-loss boundary absent from
source material, not merely because its representation is frozen or cached.

---

## 4.2. Determinism-First Law

Identical explicit valid frontend inputs must produce identical observable Resolved HIR meaning, regardless of execution
order, scheduling, or reuse.

Any semantically observable ordering must follow the owning law or an explicit deterministic compiler observation rule
that does not invent Contract meaning. Unobservable physical layout may differ.

Compiler optimizations and incremental mechanisms are permitted only when they preserve this equivalence. Clean
computation with reuse disabled remains the reference for HIR semantic correctness.

---

## 4.3. HIR Resolution Invariant

HIR becomes visible only when every semantic reference required to interpret a candidate has been resolved exactly.
Ambiguous or unresolved references prevent the affected semantic unit from satisfying this invariant.

Downstream consumers must receive exact resolved targets without repeating source lookup. The physical representation of
those references is not prescribed.

An explicit IDL binding determines the selected material's 1D role, not the shape of a host API declaration. The
material must be interpretable under its owning 1D frontend law.

---

## 4.4. Resolved Semantic Surface

HIR must preserve the complete resolved candidate meaning required by Establishment. The owning 1D ADR determines which
distinctions are Contract-semantic; HIR neither redefines them nor merges separate Contract authorities into a generic
node schema.

Every distinction required for Establishment, including explicitly declared absence and Required Basis, must remain
available without reinterpreting authored syntax.

HIR must not include fields solely for downstream convenience.

---

## 4.5. Exact Candidate References Before Authority

HIR references denote exact resolved candidate targets in the current semantic world. They do not carry Contract
authority.

```text
authored Contract material
    ↓ IDL role binding / resolution
exact HIR candidate target
    ↓ owning Establishment / Composition law
possible authoritative relation
```

A candidate reference denotes semantic meaning, not source location or physical storage identity. Provenance remains
available when required. References do not inherit identity or lineage from previous HIR generations; each generation
resolves its own references from explicit inputs.

Every Visible HIR reference must denote one valid, exact target within its semantic reference domain. Shared physical
encoding does not permit reinterpretation between reference kinds, nor does it require a universal runtime kind field.
Invalid references cannot become semantic absence; absence must be explicitly defined by the owning law.

HIR references have no universal semantic ordering. Order is meaningful only when defined by the owning law. The
compiler may provide deterministic observation order without making physical ordering semantic.

References may identify other candidates before either has Contract authority. Exact resolution does not establish the
referenced target or relation. Authority-bearing references and bindings arise only through their owning Establishment
or Composition law.

### Operation References and Coordinates

An `OperationRef` identifies one nominal Operation handle within an exact Contract Interface semantic scope,
independently of source containment or physical representation.

An `OperationParameterRef` identifies one exact, nominal Operation-local coordinate. Renaming a parameter changes that
coordinate. Declaration order or JVM parameter index does not determine its identity unless the owning Contract law
explicitly makes order semantic. Lowering may target the coordinate but cannot create it.

The Operation result position is also resolved independently of generated host signatures or JVM descriptors. Parameter
and result payload and type relations remain governed by their applicable frontend and Contract laws.

---

## 4.6. Version and Basis Boundary

A Version Claim that participates in candidate meaning must resolve to an exact, authority-scoped **Version candidate
coordinate** before HIR becomes visible. Authored spelling may remain in provenance.

This coordinate is not the authoritative `Version Binding`. Contract Version is independent of compiler and storage
versions. Historical or persistent material may support later validation but cannot create Version meaning.

A Definition Candidate preserves its complete **Basis Requirement Law** without selecting the Established material that
satisfies it.

Occurrence-owned or higher-scope Required Basis instances are not Definition HIR material. However, Definition HIR
retains any occurrence-basis law belonging to the Definition. When a Definition-time judgment owns a Required Basis, its
exact requirement may be formed from the resolved candidate as Establishment input.

Actual Basis Resolution, Basis Binding, source selection, and Complete Basis belong to the applicable Establishment or
Composition law over Established Material. Compiler dependencies and HIR references do not establish Basis Binding.

---

## 4.7. Applicability Boundary

HIR preserves the Applicability Law and its declared semantic inputs when they belong to candidate meaning. It does not
establish future `Applicable` results or future Established context instances.

Under ADR-0063, Applicability is a later legality judgment over the exact binding, dependent application, Established
context, and owning law. It does not perform implicit producer selection or arbitration.

The Applicability Law, context instance, and judgment result must remain distinct semantic categories.

---

## 4.8. HIR Seal Verification Boundary

Every Resolved HIR product must satisfy the HIR boundary before sealing and publication to independent consumers.

Private construction may contain incomplete material. Seal verification may reject material that fails to satisfy the
Resolved Contract HIR boundary. Only fully resolved and valid material may be sealed.

Seal verification does not perform an owning Contract judgment or establish Contract meaning.

When an entered seal judgment determines that its requirement is unsatisfied, it produces an **HIR-owned unsuccessful
compiler result** under ADR-0074, not a Contract refusal or Contract Failure. The exact rejection vocabulary belongs to
the HIR seal-verifier law.

If an earlier required compiler result fails before the seal judgment is entered, no synthetic HIR Seal Rejection is
created. The original producer retains failure ownership, and ADR-0074 governs the downstream consequences.

Other verification responsibilities remain with their respective ADRs and designs. This ADR defines only the HIR seal,
visibility, and HIR-specific result boundary.

## 4.9. Authoring Refinement and Semantic Convergence

Different authoring forms may produce the same consumer-visible HIR candidate meaning when their resolved meanings are
equivalent under the applicable frontend and 1D laws.

Frontend refinement may remove authoring-only distinctions after resolving them. The original forms may remain available
through provenance or source material.

Refinement must preserve all Contract-owned distinctions and must not perform authority-owned judgments. It is not the
`Canonicalization Contract` and does not require a single byte-level HIR representation.

Two authored forms with the same exact candidate reference and meaning do not acquire different HIR meaning merely
because of their authoring routes. Equal payload alone, however, cannot merge distinct candidate references or
Definitions.

Semantic ordering must follow the owning law or frontend language. Otherwise, physical construction order must not
affect observable meaning.

## 4.10. HIR Semantic Determinants and Frontend Confluence

Each HIR semantic unit's observable meaning is determined by its explicit role-qualified material and exact resolution
environment.

Additional context contributes to candidate meaning only under the applicable owning law:

- **Binding context:** Only when the owning 1D law makes it Definition-determining.
- **Frontend language or semantic profile version:** Only when its owning law makes it relevant.

How a result is computed, scheduled, cached, or stored must not affect HIR meaning. Compiler reuse-validity inputs may
invalidate a stored product without becoming semantic determinants.

The convergence rule in §4.9 also applies across `.kontrakt` source and selected 1D carriers. Under the same applicable
frontend law, equivalent role-qualified resolved candidate meaning must not differ because of its authoring route.
Source origin may remain distinct provenance.

Authoring-route differences must not affect Establishment, equality, or reuse.

## 4.11. Typed Extension Boundary

HIR provides shared compiler infrastructure, but each Contract authority owns its candidate semantics.

Adding a new 1D Contract family must not require existing candidate meanings to be rewritten into a weaker universal
property model.

Shared HIR infrastructure may provide reference, visibility, Binding representation, provenance, storage, and projection
mechanisms. The semantic payload remains defined by the owning 1D ADR.

Generic tags or property bags may serve as physical encoding, but cannot define HIR semantics.

## 4.12. Authored Contract Material and 1D Role Grant

A user API declaration is an authoring carrier, not inherently a 1D Contract. Its host-language representation does not
grant Contract meaning.

An explicit IDL binding assigns the 1D role. The frontend interprets the selected material under that role's law to
produce resolved candidate meaning.

```text id="sqw05f"
user-authored carrier
    ↓ explicit IDL binding
role-qualified declared material
    ↓ 1D frontend refinement
Resolved HIR candidate material
```

An unselected carrier does not become a 1D HIR candidate merely because it has a supported user API shape. It may remain
ordinary source material.

The authoring representation remains replaceable. A future frontend may express the same role-qualified meaning without
using the current host-language carrier shape.

## 4.13. Definition Candidate and Binding Candidate

HIR distinguishes two candidate meanings:

- **Definition Candidate:** Complete resolved 1D Definition meaning determined by its owning 1D law.
- **Binding Candidate:** The exact IDL-use relation selecting a Definition Candidate for a specific slot or role. It is
  not an authority-bearing Binding established later.

```text id="3k8bym"
IDL A ── Binding Candidate A ──┐
                               ├──> Definition Candidate D
IDL B ── Binding Candidate B ──┘
```

Multiple IDL contexts may select the same authored declaration. They may share a Definition Candidate only when the
owning 1D law determines that their contextual differences do not affect Definition meaning. Each selection retains its
distinct Binding Candidate. A role that permits multiple selections retains each selection occurrence at that scope,
including repeated selections of the same exact target, until its owning law judges the complete relation.

A Binding Candidate preserves only the exact selection occurrence, selected slot or role, target Definition Candidate
Reference, and any qualifier required by the binding law. It neither copies nor inherits Definition identity.
Surrounding context does not become Binding meaning merely because it is accessible.

A shared IDL authoring position does not merge role selections. In `facts : invariants` (ADR-0046 Section 12),
resolution preserves the distinct selections and their declared subject association under the owning laws. The
Invariant selection does not become a qualifier of the Fact Binding Candidate (ADR-0068 Section 19.4).

Definition Candidate meaning remains complete independently of consumer-specific projections.

A semantic coordinate belongs to both Definition Candidate and Binding Candidate meaning only when it independently
determines each. Physical duplication does not establish semantic membership.

## 4.14. 1D-Owned Definition Determinants

The owning 1D law determines which declared and contextual material defines its Definition Candidate meaning. HIR must
not introduce unrelated context merely to form a universal coordinate tuple or global key.

Context that does not determine Definition meaning remains under its separately owned relation.

The shared HIR reference protocol therefore imposes no universal field set. Authority kind may be represented
structurally, while Version and authority-local coordinates appear only when required by the owning law.

Before a Definition Candidate becomes Visible HIR, all Definition-determining material must be available through its
legal Definition Candidate Projection. Establishment must not reconstruct that meaning from a Binding Candidate or
surrounding source and implementation structures. Selection-only context remains Binding meaning.

A Definition Candidate may contain exact direct semantic references to separately owned Primary HIR subjects when those
references are part of its meaning. Such references preserve the other subjects' ownership and cannot substitute for
unresolved meaning that downstream consumers would have to reconstruct.

ADR-0071 defines this common completeness requirement. Each owning 1D ADR defines its exact candidate vocabulary,
determinants, and semantic components.

## 4.15. Current Identity and History Boundary

Current HIR identity is determined from current explicit inputs, never inherited from a previous HIR generation.

Earlier compiler products may be compared for reuse or change analysis, but cannot establish current identity or
semantic lineage.

When historical or succession meaning is Contract-semantic, its owning Contract establishes an explicit relation between
independently identified material.

Missing previous products may reduce reuse efficiency, but must not affect the semantic completeness of current HIR.

## 4.16. Logical HIR Formation Order

HIR formation follows a logical dependency order, even when physical implementation combines stages.

```text id="19ggdy"
Authored Contract Material
        ↓
Exact Source / Symbol Resolution
        ↓
Resolved Contract Surface Formation
    Contract Interface Subject
    Interaction Subject
    Operation Subject
    exact direct relations
        ↓
Explicit IDL Slot Selection
        ↓
Resolved 1D Role Selection
        ↓
1D-Owned Definition Determinant Projection
        ↓
Authority-Specific Authoring Refinement
        ↓
Definition Candidate Reference and Meaning Formation
        ↓
IDL Binding Candidate Completion
        ↓
HIR Seal Verification
        ↓
Visible Resolved Contract HIR
```

Contract Interface, Interaction, and Operation are semantic subjects, not generated host API artifacts. Their identities
are established through their own semantic laws, not source containment or physical arrangement.

Operation formation establishes its independent nominal callable and parameter coordinates before Lowering. Lowering may
reference these coordinates but cannot create their identities.

Explicit IDL role selection precedes 1D interpretation. Carrier shape cannot assign a role. Definition determinants are
then projected according to the owning 1D law, while selection-only context remains outside Definition Candidate
meaning.

A Binding Candidate becomes complete only after its exact Definition Candidate target exists. Earlier internal selection
material may support formation but cannot be published as a partial Binding Candidate.

This order defines logical invariants, not mandatory physical stages or separate materializations. Fused, lazy, or
incremental implementations must preserve these invariants.

Definition, Binding, and Occurrence remain distinct. Definition formation does not establish an Occurrence; when applicable, Occurrence meaning arises only through its owning later Basis, Applicability, and Occurrence judgments.
---

# 5. Information Retention and Loss

## 5.1. What HIR May Erase

HIR may erase authored distinctions that no longer affect resolved candidate meaning under the owning Contract law.

Whitespace and comments are not HIR semantics. Resolved syntactic differences and incidental host-carrier acquisition
details need not be preserved.

Source nesting does not establish implicit Contract ancestry.

---

## 5.2. What HIR Must Retain

HIR must preserve every distinction required to interpret the candidate under its owning Contract law.

It must also retain sufficient exact semantic anchoring for later diagnostics and compiler products, independently of
physical storage.

Once HIR becomes visible, Establishment must not reconstruct candidate meaning from source material. Later lowering must
likewise preserve high-level Contract distinctions required by subsequent semantic boundaries.

---

# 6. Recovery and Invalid Source

Parser recovery and invalid source material remain outside Resolved Contract HIR. The frontend may retain such material
for diagnostics or tooling, but only fully resolved and valid candidates may become Visible HIR.

```text id="ef72hh"
Source / Recovery Material
        ↓
resolution judgment
       / \
      /   \
resolved  rejected
   ↓         ↓
 HIR     explicit producer-owned compiler result
                 ↓
          Compiler Result Protocol
                 ↓
      diagnostic / recovery / availability
```

A rejected resolution result belongs to the exact frontend judgment that determined the failure. Diagnostics cannot
create that rejection, and recovery cannot rewrite it. A later HIR judgment that was never entered must not receive a
synthetic unsuccessful result. ADR-0074 governs these compiler results.

Missing or unresolved source material cannot be treated as explicit Contract absence or an empty semantic reference.
Recovery nodes carry no Contract authority.

Tooling may expose a separate incomplete frontend view, but Establishment must not consume it as Resolved Contract HIR.

**Invalid source must not invalidate unrelated resolved semantic units.** An invalid candidate and units whose meaning
depends on it cannot become Visible HIR. Independent candidates may still be formed and verified when their determinant
closure is complete.

The compiler must not fabricate HIR placeholders to continue processing. The unsuccessful result remains owned by its
producer and is available to consumers through legal Protocol projections.

Local HIR availability does not imply compilation success. A batch build may fail because a required source unit is
invalid, even when other valid HIR units remain available to diagnostics or tooling. The common compiler-result
architecture, not HIR semantics, owns availability, boundary-completion, and orchestration representation.

---

# 7. Establishment Handoff

Authority-Owned Establishment must receive complete resolved candidate meaning without reopening authored source.

It observes Visible HIR through the **Resolved HIR Candidate Protocol**, realized by the HIR Semantic Access Boundary
(§8.9). This Protocol defines legal observation, not a separate IR, compiler stage, Contract authority, or mandatory
physical material layer.

The Definition Candidate provides the resolved 1D meaning. The Binding Candidate identifies the exact current IDL
selection. They remain distinct semantic products.

```text id="y5e7f9"
Resolved Definition Candidate
    +
Definition-time semantic basis allowed by the owning law
    ↓
Authority-Owned Definition Establishment
    ↓
Established Definition
```

HIR retains the Basis Requirement Law and Applicability Law when they belong to Definition Candidate meaning. Their
concrete satisfaction, binding, and judgment results belong to the later owning boundaries. Establishing a Definition
does not turn its Binding Candidate into an Established Occurrence.

When application has separate semantic meaning under the owning 1D law, the later path is:

```text id="rbjffa"
Established Definition
    +
exact application context
    +
Required Basis resolution / Basis Binding where owned
    +
Applicability judgment where owned
        ↓
Occurrence judgment
        ↓
Established Occurrence
```

Not every 1D requires an Established Occurrence. The owning Contract law determines whether it exists.

Any IDL context that determines Definition meaning must already be resolved during Definition Candidate formation
(§4.14). Establishment neither rediscovers this from IDL topology nor repairs incomplete frontend resolution.

HIR visibility grants no Contract authority. Establishment is an owning judgment, not a compiler refinement or
representation pass. The Canonical Contract World is a later representation of already-established Definition meaning.

# 8. Visible HIR Surfaces

## 8.1. Consumer Contract

HIR is producer-independent and consumer-aware. The frontend must expose sufficient resolved meaning for valid consumers
without encoding their implementation topology or requiring independent source interpretation.

Authority-Owned Establishment is the primary semantic consumer. Other compiler consumers may observe HIR through legal
projections. Later realization stages normally consume established or lowered material, but HIR must not prematurely
discard meaning they require.

Generated Interaction and Operation APIs derive from established Contract meaning, not directly from HIR authority. HIR
preserves their underlying Contract-semantic subjects and relations, not their generated host API shape.

---

## 8.2. Primary HIR Semantic Surface

Visible Resolved HIR has one **Primary HIR Semantic Surface** containing HIR-owned semantic subjects and direct
relations.

Its primary material comprises:

| Material              | Required meaning                                                         |
|-----------------------|--------------------------------------------------------------------------|
| Contract subjects     | Contract Interface, Interaction, and Operation resolved from IDL meaning |
| Definition Candidates | Typed membership and complete 1D-owned Definition Candidate meaning      |
| Binding Candidates    | Typed membership and complete IDL Binding Candidate meaning              |
| Direct relations      | Exact semantic references and distinctions required by the owning law    |

Contract subjects are not generated host APIs or realization artifacts.

Operation owns independent nominal coordinates. Its exact Operation reference, parameter coordinates, and result
position are Primary HIR meaning. Lowering and generated APIs consume these coordinates but cannot establish their
identity. Host signatures and physical addressing do not determine that identity.

The Primary Surface is not a universal record schema. Each 1D owns its candidate vocabulary, and semantic role may be
represented structurally without redundant tags.

Definition determinants govern HIR formation. They become visible semantic material only as part of the meaning defined
by the owning law, not as generic determinant metadata.

Each applicable 1D role, including Input and Output, has exactly one observable selection state at its owning scope and
role coordinate. Its owning law defines whether a present selection contains one exact IDL Binding Candidate Reference
or a complete multi-selection relation. Explicit Absence means non-selection, not permission to omit the role, and
does not require a universal absence field.

---

## 8.3. Stable Semantic Units

HIR must expose semantic units independently of physical layout.

A complete frontend generation is a coherent visibility unit, but need not be the only dependency or reuse unit.
Semantically independent subjects may be addressed and consumed separately.

The primary semantic subject categories are defined in §8.2. This ADR does not fix their exact payloads, reference
coordinates, projection sets, or physical representations.

---

## 8.4. HIR Semantic Projections and Derived Knowledge

A **Semantic Projection** is a deterministic legal observation of existing Primary HIR meaning, not a separate source of
meaning.

A Fine-Grained Semantic Projection may narrow or reorganize existing semantic material but must not introduce a new
judgment.

Every projected observation must be reducible to explicit Primary HIR meaning and remain complete for its declared
purpose. The producer owns the observation set and semantic equality; consumers cannot weaken them.

Projections must not reconstruct missing meaning from authored source, repeat 1D refinement, or depend on hidden mutable
compiler state. Projections may derive from other legal projections only when they remain reducible to the same Primary
HIR meaning without introducing new judgments.

The distinction is:

| Semantic Projection               | Derived Compiler Knowledge                         |
|-----------------------------------|----------------------------------------------------|
| Re-expresses existing HIR meaning | Establishes new information through computation    |
| May reorganize direct relations   | May compute inferred relations or analysis results |
| Introduces no new judgment        | Has its own computation and validity requirements  |

For example, a reverse lookup over an existing direct relation may be a projection, while transitive closure is derived
knowledge.

Logical projection meaning is independent of physical materialization. A projection may be computed on demand or stored
for reuse without changing its meaning or creating Contract authority.

Semantic ordering follows the owning law. Where semantic order is absent, the producer may define deterministic
observation order without making that order semantic. Physical ordering has no semantic authority.

Projection catalogs, access mechanisms, and storage strategies remain compiler design decisions. A projection is neither
a new Contract authority nor automatically a new IR level.

---

## 8.5. Reference Direction and Ownership

A resolved HIR reference identifies another semantic subject without implying ownership of that subject.

Source nesting and physical storage arrangement do not determine semantic ownership. Consumers must follow exact
reference relations rather than infer ownership from physical containment.

This separation permits independent storage and reuse of referenced semantic units without copying their entire
referenced object graphs.

## 8.6. Contract Structure and Compiler Partition

HIR does not create a semantic ownership hierarchy beyond the Contract's Definition and Binding relations. Definition
Candidate meaning remains governed by its owning 1D law, while each IDL selection retains a distinct Binding Candidate.

The compiler may partition HIR to manage formation, visibility, reuse, and storage. Partitions may contain one or
multiple semantic units, but do not determine Contract ownership, Definition identity, or Binding meaning.

Partition topology is a physical design choice and must not alter consumer-visible semantics.

## 8.7. Exact Semantic Reference and Generation-Local Dense Handle

HIR semantic references and physical access handles serve different purposes.

A Definition Candidate Reference or Binding Candidate Reference identifies an exact current semantic target. The
compiler may map it to a compact generation-local dense handle for efficient access.

```text
exact current HIR reference
    ↓ current-generation mapping
dense primitive handle
    ↓
typed slab / table / segment
```

A dense handle is not semantic identity. Its numeric value is not observable HIR meaning and is valid only within its
assigned generation. Equivalent meaning in another generation may use a different handle.

Identifiers used for lookup or persistence, including HID and fingerprints, do not replace semantic references. Physical
storage coordinates are likewise distinct from both semantic references and dense handles.

Persistent products must not treat serialized dense handles as semantic references. Restoration requires current
semantic reference resolution or validation before obtaining a current-generation handle.

This mapping does not establish cross-generation identity or lineage.

## 8.8. Consumer Access Discipline

Consumers observe HIR through typed semantic subjects, exact references, and legal projections. Physical containment or
mutable producer state cannot grant semantic authority.

Specialized internal access may exist for compiler implementation purposes, but it is not the stable consumer contract.

Derived indexes and cached lookup structures remain recomputable compiler products unless an owning semantic law
explicitly assigns them another status.

Consumers must remain independent of physical storage and materialization strategies.

## 8.9. HIR Semantic Access Boundary and Resolved HIR Candidate Protocol

Visible Resolved HIR exposes a stable semantic access boundary independent of physical representation.

```text
HIR Semantic Contract
    ↓ observed through
HIR Semantic Access Boundary
    ↓ realized by
Physical HIR Representation
```

For Authority-Owned Establishment, this boundary realizes the **Resolved HIR Candidate Protocol**. The Protocol defines
legal observation of candidate meaning without creating another IR, semantic stage, Contract authority, or mandatory
material copy.

```text
Resolved HIR Candidate Protocol
    ├── Typed HIR Semantic Reference Domain
    ├── Definition Candidate Projection
    ├── IDL Binding Candidate Projection
    └── Fine-Grained Semantic Projection
```

The four Protocol components have distinct responsibilities:

| Component                           | Required observation                                                                                                  |
|-------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| Typed HIR Semantic Reference Domain | Exact, kind-preserving references to current Primary HIR subjects (§8.2), including Operation-local coordinates       |
| Definition Candidate Projection     | Complete resolved Definition Candidate meaning under its owning 1D law                                                |
| IDL Binding Candidate Projection    | Exact selection occurrence, selected role or slot, target Definition Candidate, and independently required qualifiers |
| Fine-Grained Semantic Projection    | Producer-defined legal observations of existing Primary HIR meaning (§8.4)                                            |

### Definition Candidate Projection

The owning 1D law determines the candidate's semantic components and required context. The Projection must expose its
complete meaning, including exact direct references to separately owned subjects when required.

A consumer cannot weaken the complete Definition Candidate Projection for convenience. It must use a legal Fine-Grained
Semantic Projection when less information is needed.

All meaning required by Establishment must already be resolved. Direct references preserve the referenced subjects'
ownership and cannot substitute for missing candidate meaning.

This ADR does not impose a universal candidate record or redundant fields for distinctions already represented
structurally.

### IDL Binding Candidate Projection

A Binding Candidate identifies the exact IDL selection independently of Definition Candidate meaning.

It exposes only the selecting subject or context needed to identify that occurrence, the selected role or slot, the
exact target Definition Candidate Reference, and qualifiers owned by the binding law.

The Projection must not copy the Definition payload or absorb surrounding IDL context merely because it is accessible. A
coordinate belongs to both projections only when it independently determines both meanings.

A Binding Candidate becomes legally visible only when its target relation is complete and referenceable under HIR seal
and visibility laws.

It remains pre-authority material, not an Established Binding of any kind.

### Fine-Grained Semantic Projection

Fine-Grained Semantic Projections follow §8.4. They expose complete, producer-defined observations of existing Primary
HIR meaning without introducing new judgments or reconstructing missing meaning.

Their equality remains producer-owned. They do not create another source of HIR meaning or require a particular Query
architecture.

Each applicable 1D role's complete selection must be observable from its owning HIR subject and exact role coordinate.
A single-selection role exposes the selected IDL Binding Candidate Reference. A multi-selection role exposes every
exact Binding Candidate Reference occurrence in the complete selection relation, preserving the multiplicity and any
ordering distinctions defined by the owning law. For example, the Fact Vocabulary Selection Projection in ADR-0068
preserves each occurrence without giving declaration order semantic meaning. A role may instead expose **Explicit
Absence** under the applicable selection law.

Missing, unresolved or unavailable selection is not Explicit Absence and cannot be observed as a completed smaller
selection. This observation does not determine whether omission, cardinality or repeated selection is legal, or
establish Contract material.

### Protocol Visibility and Authority

Establishment may observe only material satisfying the HIR seal, recovery, visibility, and snapshot-coherence laws
(§§4.8, 6, 11, 13).

Ordinary Establishment observation is read-only. Consumers cannot bypass the Protocol to reconstruct meaning from source
or internal compiler representation.

Source provenance remains a separate relation (§§8.10, 10), unless an owning Contract law explicitly makes a source
coordinate part of candidate meaning.

Physical implementation metadata and recovery material are not candidate meaning. The Protocol also cannot pre-establish
later authority-owned results.

The access boundary is logical rather than a required physical layer. Implementations may use specialized access paths
and shared backing storage when they preserve the same legal observations without merging semantic kinds.

This ADR fixes the Protocol families and Primary HIR subject categories, not their concrete APIs, payload schemas,
reference encodings, or storage strategies.

### 8.9.1. Protocol Evolution and Compatibility

HIR Protocol evolution has independent stability domains:

```text
Contract Version
    !=
authority-specific HIR semantic-surface evolution
    !=
common HIR Candidate Protocol evolution
    !=
persistent-product format evolution
    !=
compiler generation
```

Changing compiler representation or storage does not change Contract Version. A new Contract Version does not require a
Protocol revision if the existing Protocol supports its required observations. Authority-specific semantic surfaces may
also evolve independently.

**Protocol compatibility depends on preserved legal semantic observation, not matching revision identifiers.**

```text
revision equality
format equality
compiler-version equality
decode success
    !=
Protocol compatibility by themselves
```

A producer and consumer are compatible only when every required observation can be supplied under the current Protocol
law, directly or through validated migration or projection.

Revision and capability metadata may assist compatibility checks but cannot prove compatibility by themselves or become
Contract authority.

Migration must preserve every required semantic observation without inventing missing meaning. Successful migration
alone does not establish Contract identity, Version equality, Definition equality, or HIR semantic equality.

The logical compatibility rules are fixed here. Specific version encodings and negotiation mechanisms remain compiler
design decisions.

### 8.9.2. Unknown Extensions, Unsupported Surfaces, and Downgrade

Protocol handling distinguishes:

- **Unknown material:** Its interpretation is not understood by the consumer.
- **Unsupported material:** Its requirement is recognized but cannot be completely provided by the consumer.

Neither may be silently interpreted as older or simpler semantic meaning.

Extension handling follows a Protocol rule already understood by the consumer. Unknown material cannot define its own
safe handling rules.

```text
required semantic extension
    unknown or unsupported
        -> fail closed, migrate, or recompute under a compatible path

semantic material outside this consumer's legal observation
        -> RETAIN-HIDE or SEPARATE

explicitly ignorable non-semantic extension
        -> skip permitted

preservation-required unknown material
        -> retain opaquely without interpretation
```

Unknown material may be skipped only when an already-understood Protocol rule establishes that it is unnecessary for the
consumer's legal observation.

Material may remain semantic in HIR while hidden from a particular consumer. Hidden does not mean non-semantic, and
ignored does not mean erased.

Preservation-required unknown material may be retained opaquely for later legal consumption or persistence. It must not
be exposed as interpreted meaning. Its participation in semantic equality remains determined by the owning semantic law.

Unsupported material does not authorize automatic downgrade. A smaller legal observation set is permitted when it
preserves everything required by that consumer.

```text
complete legal projection
    -> allowed

lossless representation conversion
    -> allowed

validated meaning-preserving migration
    -> allowed

silent semantic downgrade
lossy fallback
unknown-field loss that changes required meaning
fabricated default for missing meaning
    -> forbidden
```

Missing meaning cannot be replaced with a default unless the owning semantic law explicitly defines that default or
absence.

Stable extension coordinates must not be reused for incompatible meanings, including after retirement.

Compatibility failures may remain local to the smallest sound semantic dependency boundary. Unaffected products need not
be invalidated.

When compatibility cannot be established, the affected observation must be rejected or reconstructed through a valid
migration, re-resolution, or deterministic clean formation path.

### 8.9.3. Consumer Scope, Reference Lifetime, and Post-Establishment Retention

Visible HIR has one producer-owned semantic meaning. Legal consumers may select different producer-defined projections,
but cannot redefine their semantics, completeness, or equality.

Consumer responsibilities remain distinct:

| Consumer                      | Legal observation                                                                                            |
|-------------------------------|--------------------------------------------------------------------------------------------------------------|
| Authority-Owned Establishment | Resolved candidate meaning through the Candidate Protocol                                                    |
| Diagnostics                   | Sealed HIR meaning and separate provenance; unsuccessful compiler judgments through ADR-0074 when applicable |
| Tooling / IDE                 | Valid sealed HIR projections and provenance; recovery material through separate surfaces                     |
| Reuse infrastructure          | Producer-defined projections and semantic equality with compiler-owned validity evidence                     |

Diagnostics must not fabricate unsuccessful Compiler Results. Reuse metadata may assist validity checking but does not
define HIR identity or equality.

After Establishment, authoritative downstream consumers must use the Canonical Contract World or another appropriate
established surface. Retained HIR cannot substitute for established meaning.

HIR may remain available for other legal consumers, subject to their observation rules.

**A Protocol view or generation-local handle is valid only while its owning generation or independently visible
projection remains valid.** Cross-generation use requires explicit current-reference resolution, remapping, or
validation. Stable coordinates may assist but cannot transfer generation membership implicitly.

HIR storage may be reclaimed when no legal consumer requires its observation and any independently required retained
material is available through the product responsible for it.

Retention and physical reachability do not extend HIR authority.

Protocol compatibility remains governed by legal observation even when no explicit numeric Protocol revision is stored.

## 8.10. Visible HIR Representation Preservation

The HIR Semantic Access Boundary defines semantic observation, but accessor equality alone does not guarantee valid
Visible HIR representation.

A physical HIR representation must preserve all applicable visible-product obligations:

```text
Primary HIR Semantic Surface
+ Provenance Relation Surface
+ Visibility / Generation Protocol
+ Persistence Protocol, when persistence applies
```

Physical representations may differ as long as these obligations remain unchanged. Such changes must not alter or
establish Contract-visible meaning and do not authorize Contract Canonicalization.

Semantic order is governed by the owning Contract law. Where no semantic order exists, the compiler may define
deterministic observation order. Physical storage order may differ from both.

Private implementation state may be nondeterministic only when it cannot affect any declared observation or
deterministic boundary. Deterministic private handle assignment remains a valid implementation choice, not HIR meaning.

Resource use, memory cost, and physical failure strategies concern implementation admissibility rather than HIR semantic
equality. Compiler-wide determinism, capacity, corruption, and failure requirements still apply.

---

# 9. HIR Identity and Equality

HIR distinguishes **exact current references** from **semantic equality**.

A reference identifies one exact current HIR subject. Reference equality means that two legal references within the same
applicable semantic reference domain identify that same subject. It does not establish equality of complete candidate
meaning.

HIR candidate references are not authoritative Contract Definition or Occurrence References. Those belong to their
owning Establishment and Contract laws.

The distinctions are:

| Concept                    | Rule                                                                                        |
|----------------------------|---------------------------------------------------------------------------------------------|
| Reference equality         | Identifies the same exact current semantic subject within its reference domain              |
| Semantic equality          | Compares the meaning of the relevant HIR product or projection under its producer-owned law |
| Physical equality evidence | May accelerate comparison but cannot define or weaken semantic equality                     |

Equal payload projections do not merge distinct semantic subjects or Definitions. Conversely, inequality of complete HIR
generations does not imply inequality of every contained semantic unit.

Current HIR identity is established from current explicit inputs, never inherited from previous generations. Equal
semantic projections may recur across generations despite changes in provenance or physical representation. HIR has no
lineage identity.

Semantic equality is independent of source and physical identity. Generic object equality or serialized-byte equality
cannot replace the producer-owned semantic law.

HID and fingerprints may support efficient equality checks, but collisions or cache hits must never establish false
equality. The exact collision-safe comparison strategy is outside this ADR.

Persistent formats may use canonical encoding for storage or reproducibility without making that encoding HIR semantic
equality.

---

# 10. Provenance

Source provenance relates HIR meaning to authored material but remains separate from HIR semantic equality unless the
owning semantic law explicitly makes origin part of that meaning.

Consumers may observe provenance only through a legal, separate relation:

```text
HIR semantic subject or legal projection
    ── provenance relation ──> source material
```

Provenance does not require inline source fields or one-to-one source spans. Its relation may be many-to-many. The exact
provenance vocabulary and physical representation remain producer and compiler design concerns.

Semantic and provenance validity may change independently. Source movement may change provenance without changing HIR
meaning, while unchanged source coordinates do not guarantee unchanged semantic meaning.

**Provenance must correspond to the exact semantic observation it accompanies.** Coincidental identifier matches or
independently joined compiler records cannot establish that relationship.

Synthetic compiler material without a direct authored origin must remain distinguishable from missing or unavailable
provenance. No universal synthetic-origin representation is required.

Provenance cannot reconstruct meaning missing from Visible HIR. Explanatory compiler history and formatted diagnostics
are not automatically HIR provenance; they may exist as separate compiler products. Formatted diagnostic text must not
become HIR meaning.

Provenance creates neither Contract authority nor HIR semantic identity. Its encoding, revision tracking, retention, and
refresh mechanisms remain compiler design decisions.

---

# 11. Visibility

## 11.1. Frontend Working State

Before HIR becomes visible, the frontend may maintain private, bounded working material for source resolution and
refinement.

This material is not Resolved HIR. Establishment and other independent consumers must not depend on incomplete working
state or its physical representation.

Source and working material may be retained for frontend, diagnostic, or tooling purposes without becoming Visible HIR
meaning.

---

## 11.2. Visible Generation

A HIR generation becomes visible only after resolution is complete, the HIR invariant is satisfied, and the generation
is sealed.

```text
private construction
    ↓
resolution complete
    ↓
HIR invariant validation
    ↓
seal generation G
    ↓
make generation G visible
    ↓
read-only consumers
```

Visibility is a compiler lifecycle boundary, not Contract authority.

A visible generation is immutable to ordinary consumers, regardless of its physical implementation.

## 11.3. Visibility Granularity and Materialization

A coherent HIR generation does not require one monolithic physical visibility barrier.

The compiler may publish a stable manifest of independently sealed semantic units or projections. Each visible unit must
satisfy the HIR invariant and belong to the same coherent generation inputs.

**Semantic formation and physical materialization are distinct.** A visible semantic scope must have complete current
membership and validated candidate meaning. Its physical backing may be materialized eagerly or on demand.

Demand-driven semantic formation may expose only the smaller scope that has actually been formed and validated. It must
not present an unresolved larger scope as complete.

Every visible unit follows the same HIR resolution and visibility laws, regardless of how it was formed.

V1 may use eager formation, while finer-grained implementations remain possible without changing HIR meaning. The exact
granularity remains a compiler design decision.

---

# 12. HIR Lifecycle and Transition Model

HIR lifecycle governs compiler operations, not Contract State or Transition authority.

The logical lifecycle is:

```text
construction candidate
    ↓ successful validation
validated candidate
    ↓ sealing
sealed generation
    ↓ visibility transition
visible generation
    ↓ newer generation made visible
superseded generation
    ↓ no legal consumer requires it
retired generation
    ↓ physical lifetime ends
reclaimed storage
```

A failed or cancelled construction candidate is discarded without becoming partially visible. If an older generation is
visible, a failed replacement attempt does not invalidate it.

Only a successful visibility transition may supersede the current generation. The new generation represents new explicit
inputs without changing the older generation's semantic meaning.

**Supersession and reclamation are separate transitions.** An older generation may remain readable after a newer
generation becomes current.

Lifecycle does not require a mutable state field on every HIR node. It may be managed through generation ownership or
compiler product metadata.

Lifecycle management may operate on finer-grained stable semantic units or projections, but not arbitrary physical
fields.

A visible semantic product may outlive its construction state. Auxiliary material may be discarded independently,
provided every still-visible, valid, and required semantic unit remains available to its legal consumers.

The exact reclamation mechanism remains open.

---

# 13. Snapshot Coherence

A computation requiring one coherent HIR world must observe a valid, visible generation. Material from different
generations or incomplete construction must not be presented as one coherent current HIR observation.

Current-generation membership is established from current explicit semantic inputs, not inherited from previous products
or reused storage.

Cross-generation reuse is permitted when an equivalent semantic projection has been validated for the current
generation. This does not establish identity continuity between generations.

Provenance accompanying a semantic observation must be valid for that exact observation. Semantic material and
provenance may be refreshed independently, but stale relations cannot be presented as one coherent current view.

Tooling may display older HIR alongside newer source or recovery material, provided it does not present them as one
coherent current Resolved HIR observation.

Snapshot coherence does not require physical copying or any particular generation-management mechanism.

---

# 14. Query, Product, and Manager Compatibility

Resolved HIR is a compiler product, not a Query object or Manager-owned representation.

Compiler infrastructure may consume or process HIR without defining its meaning:

- **Query infrastructure:** Requests complete generations or smaller semantic projections.
- **Analysis Manager:** Associates derived results with exact HIR subjects and validity contexts.
- **Pass Manager:** Coordinates HIR-preserving representation preparation before sealing or within private generations.

HIR must expose stable semantic subjects, generation validity, and legal observations without depending on a particular
compiler orchestration architecture.

Derived knowledge remains separate from HIR. Shared computations may produce reusable derived products rather than
embedding their results in unrelated HIR definitions.

Dependency scope should follow the smallest valid semantic boundary. A consumer requiring one Definition projection must
not depend on the entire frontend generation solely because of physical storage organization.

---

# 15. Dependency Observation

Query and incremental infrastructure may record dependencies on consumed HIR semantic products or projections.

**Dependencies must follow semantic product boundaries, not physical storage boundaries.** Physical reads do not
establish permanent compiler dependencies.

The following relations remain distinct:

```text id="z8xlhi"
HIR semantic relations
compiler query dependencies
build / artifact dependencies
source provenance relations
```

Compiler query dependencies do not establish Contract relations, including Required Basis, Basis Binding, or
Applicability.

All inputs that may determine an HIR-producing computation's result must be explicit. Hidden environmental or mutable
state must not change observable HIR meaning.

---

# 16. Cache and Reuse Law

Cache avoids computation. It creates neither HIR nor Contract authority.

A cached HIR product may be reused only after the compiler establishes compatibility with the requested semantic
product. An invalid, stale, corrupt, missing, or incompatible cache entry requires valid recomputation, or an explicit
compiler failure when recomputation is impossible.

Deleting or warming the cache must not change observable HIR meaning for identical explicit inputs. This applies to all
cache and persistence mechanisms; their retention policies do not define HIR semantics.

**Semantic equality and physical reuse eligibility are independent.** Equal semantic projections may still be ineligible
for physical reuse when compiler-owned validity or compatibility requirements are unsatisfied. Physical availability
cannot replace producer-owned semantic equality.

Reusing an HIR product does not automatically permit reuse of separately owned compiler products. Each retains its own
validity and observation law.

---

# 17. Early Cutoff

HIR supports semantic early cutoff.

Before considering a previous product for reuse, Kontrakt must resolve the current 1D role, complete semantic
determinants, and exact semantic references required by that product. Compiler-owned reuse-validity inputs must also
permit comparison.

Reuse validity requires sufficient current evidence, not necessarily re-execution of every previous computation.

Validation may start with cheaper evidence and proceed into affected dependencies or projections only when necessary.
Routing summaries can guide validation, but neither their equality nor inequality alone proves semantic equality or
change.

If the current projection is equivalent to the previous one under its producer-owned semantic equality law, downstream
products depending only on that projection need not be invalidated.

```text
current explicit inputs
    ↓
cheap current-validity evidence
    ↓
sufficient?
    ├── yes → validate previous product compatibility
    └── no  → narrow affected dependency / projection
                  ↓
              selective revalidation
                  ↓
              validate previous product compatibility
    ↓
compare consumer-visible HIR result where required
    ↓
meaning unchanged
    ↓
semantic propagation may stop
```

Early cutoff depends on consumer-visible semantic equality, not cache availability or aggregate generation changes.
Selecting a previous product for comparison does not establish cross-generation identity.

A provenance-only change may require updated provenance and diagnostics without invalidating unchanged semantic
downstream products.

Early cutoff is optional optimization. When sufficient current validity cannot be established, Kontrakt must revalidate
or use deterministic clean recomputation. An unsound cutoff is a correctness failure.

---

# 18. Incremental Architecture Boundary

V1 uses query-oriented compiler orchestration. It may reuse generation-bound in-memory results and record explicit
dependencies to support local early cutoff. The final V2 incremental engine is not required.

V2 may persist dependency state and reuse HIR across compilation sessions. Its frontend may track change frontiers and
update affected results incrementally. Delta maintenance and lazy repair are permitted where appropriate.

No single incremental strategy is required across the frontend.

A small semantic projection may use memoized validation while another domain recomputes from scratch. A relational
analysis may justify delta maintenance, while a cheap frontend product may simply rerun when dependency bookkeeping
costs more than recomputation.

Every strategy must preserve the same HIR semantic result:

```text
incremental result
    == HIR semantic result required by clean computation
```

If equivalence cannot be established, Kontrakt uses the deterministic clean path.

---

# 19. Full-Recompute Reference Path

HIR correctness must not depend on persistent incremental state.

Kontrakt must retain a conceptually complete path that reconstructs HIR from explicit frontend inputs without previous
HIR products.

This path is required for:

- Corruption recovery.
- Differential and incremental-equivalence validation.
- Cache-off testing.

Persistent compilation may reduce how often the clean path runs, but previous incremental state cannot become the sole
source of correct HIR meaning.

## 19.1. HIR Protocol Conformance and Independent Verification Seam

Every Visible Resolved HIR product must remain checkable against the common HIR Protocol and its applicable
authority-specific semantic surface.

Common HIR conformance checks own Protocol and well-formedness invariants. They do not reinterpret owning 1D semantics
or establish Contract authority.

The deterministic clean formation path provides the reference for comparing alternative formation paths. All such paths
must preserve the same producer-defined legal semantic observations. Conformance is determined by those observations,
not physical representation.

Clean-versus-optimized differential comparison is necessary but cannot detect errors shared by both paths. Therefore,
the architecture must permit **independently derived conformance checks** against the legal HIR observation surface. No
particular oracle or testing mechanism is mandated.

A conformance checker verifies existing HIR law. Its success does not establish HIR or Contract meaning.

A product failing a required conformance check cannot become valid Visible Resolved HIR. ADR-0074 governs unsuccessful
compiler results.

The specific checker implementation and verification strategy remain compiler design responsibilities.

---

# 20. HIR Formation, Transformation, and Optimization Law

Semantic formation and authoring refinement follow §§4.9 and 4.12–4.16. Physical transformations must preserve the
Visible HIR obligations defined in §8.10.

This section defines the additional rules for formation optimization and reuse.

## 20.1. Physical Formation Optimization

The compiler may fuse logical formation stages and optimize their physical realization without changing Visible HIR
meaning.

Exact pre-counting and sizing are optional. Bounded or segmented formation is equally permitted when it preserves
deterministic visibility.

Formation computation and immutable payload storage may be shared when the relevant current inputs are equivalent. Such
sharing must not merge distinct Definition Candidates or their semantic references and provenance.

Producer scratch may be released only after all required observations and evidence have been preserved and no remaining
reader depends on it.

## 20.2. Formation-Time Reuse

Formation-time reuse may occur before a current Visible HIR projection exists. It is compiler machinery, not a new
semantic projection or equality law.

A previous product may be reused only when current semantic determinants, exact references, membership, and
compiler-owned validity evidence establish its eligibility (§§16–17).

Reuse must not supply missing current meaning or inherit identity from an earlier generation. Otherwise, Kontrakt
follows the valid clean formation path.

## 20.3. Optimization Boundary

All work producing a Visible HIR product must complete before sealing. Later optimization cannot mutate its candidate
meaning in place.

An optimization requiring a different semantic product must publish a separate compiler product or later IR.

Visible HIR does not require a single semantic normal form that collapses Contract-owned distinctions. Compiler encoding
remains separate from Contract Canonicalization.

The legal optimization relation is:

```text
same explicit semantic inputs
    ↓
clean / cached / incremental / parallel formation
    ↓
same Visible Resolved HIR obligations
```

A physical optimization that cannot preserve this relation is not legal.

# 21. Concurrency

Parallel frontend work is permitted only when Visible HIR remains deterministic.

Workers may construct private candidate material, but execution order must not affect semantic identity or any declared
observable result, including provenance and persistent output.

Private physical coordinates may differ between executions. They must not escape into stable semantic references,
fingerprints, diagnostic evidence, or persisted products.

Concurrent work must be resolved through a deterministic merge or equivalent visibility rule. Interning races or
first-writer order cannot determine semantic equality or observable results.

Cancellation must not publish a partial generation. A stale worker result cannot replace a newer valid visible
generation.

The concurrency mechanism remains an implementation decision.

---

# 22. Persistence and Cross-Session Products

HIR products may be persisted across compiler sessions.

Persistence requires its own protocol. A persistent product must define its format, integrity rules, reference
remapping, and any canonical encoding required for reproducibility. These remain separate from Contract Version.

Three validation boundaries must be distinguished:

| Boundary           | Requirement                                                          |
|--------------------|----------------------------------------------------------------------|
| Persistent product | Integrity and format compatibility                                   |
| HIR Protocol       | Ability to provide every required legal semantic observation         |
| Current reuse      | Validity of the product and its evidence for the current compilation |

Successful decoding or format migration does not establish HIR Protocol compatibility. Conversely, Protocol-compatible
meaning may use a different persistence format.

An incompatible persistent product must be rejected or migrated under an explicit compiler product rule. It cannot be
silently reinterpreted.

Content-addressed storage may deduplicate immutable products. Its addresses and physical encoding remain storage
identities, not HIR or Contract identities. Optional compression must not redefine semantic equality.

A restored product must satisfy the same HIR semantic resolution boundary as a newly computed product. Prior
verification may be reused only when sufficient current evidence establishes that its semantic determinants, exact
references, and reuse-validity conditions remain valid.

**Reuse does not require repeating every original computation when current validity is already established.**

Validation may begin with a sound summary and descend into affected dependencies only when necessary. If the summary is
insufficient, the compiler may selectively revalidate the required semantic projections.

Fingerprints and other summaries may accelerate this process, but cannot themselves establish HIR semantic equality or
Contract authority.

The current validity set must include every input capable of affecting the product's meaning or reuse eligibility.
Unrelated environment information is not required. Broad version labels cannot replace more precise validity inputs when
those distinctions matter.

If validity cannot be established, Kontrakt must revalidate or use deterministic clean formation. Persistent state
cannot become the sole source of correct HIR meaning.

Restored products must also remap or validate current semantic references before exposing generation-local dense
handles. Physical storage coordinates are not persistent semantic references.

---

# 23. Diagnostics and Tooling

Diagnostics may observe sealed HIR meaning through legal projections and combine it with valid provenance or separately
owned compiler evidence.

When explaining an unsuccessful HIR-related compiler judgment, diagnostics must use the producer-owned result through
the **Compiler Result Protocol (ADR-0074)**. Other diagnostic conditions do not require a synthetic Compiler Result.

Diagnostic presentation and analysis remain separate compiler products. Neither the renderer nor the source location
owns HIR meaning or rejection meaning. Engineering diagnostic mode does not expand the legal HIR observation surface.

A rendering failure or provenance refresh does not change the underlying compiler result. A pre-HIR rejection must be
explained from its owning result and source material without fabricating HIR.

Tooling and IDE consumers may use sealed HIR when resolved meaning exists. Incomplete or speculative frontend material
may be used separately for editing, but cannot become partially valid HIR.

**Batch compilation and IDE tooling must follow the same HIR semantic law.** Tooling-specific judgments remain separate
products. Source-to-HIR lookup may locate existing resolved meaning but cannot independently establish it.

Tooling may display stale HIR alongside newer source for continuity, provided the combination is not presented as one
coherent current HIR observation.

---

# 24. Non-Normative Reference Semantic Model

This section illustrates one compact physical realization of Resolved HIR. It does not prescribe a storage schema.

The example preserves the separation between semantic meaning and compiler implementation:

```text
resolved Definition Candidate meaning
    ≠
IDL Binding Candidate meaning
    ≠
source provenance
    ≠
query / cache state
    ≠
physical storage identity
```

One possible implementation uses typed columnar families and generation-local dense handles.

```text
HIR Generation G

Contract Interface Subject columns
    interfaceOperationRefBase[]
    interfaceOperationRefCount[]

Contract Interface -> Operation relation columns
    interfaceOperationRef[]

Interaction Subject columns
    interactionOperationRef[]
    interactionInputBindingRef[]
    interactionAdmissionBindingRef[]
    interactionCanonicalizationBindingRef[]
    interactionLoweringBindingRef[]

Operation Subject columns
    operationParameterRefBase[]
    operationParameterRefCount[]
    operationResultRef[]
    operation-owned resolved semantic material

Operation Parameter Coordinate columns
    operationParameterRef[]
    operationParameterNominalCoordinate[]

Input Binding Candidate columns
    inputBindingTargetRef[]

Admission Binding Candidate columns
    admissionBindingTargetRef[]

Canonicalization Binding Candidate columns
    canonicalizationBindingTargetRef[]

Lowering Binding Candidate columns
    loweringBindingTargetRef[]

Authority-specific Definition Candidate columns
    Input definition slabs
    Admission definition slabs
    Canonicalization definition slabs
    Lowering definition slabs

Lowering relation columns
    loweringEdgeBase[]
    loweringEdgeCount[]

Lowering-edge columns
    loweringSourceCoordinateRef[]
    loweringTargetParameterRef[]
    loweringTargetFactCoordinateRef[]

Current-reference indexes
    authority-specific candidate-reference material
    current reference -> generation-local dense handle

Provenance sidecar
    provenanceSubjectRef[]
    provenanceSourceRef[]
    provenanceStart[]
    provenanceEnd[]
```

Each array represents a logical column. A JVM implementation may use primitive arrays or FFM `MemorySegment` storage for
high-cardinality data and another compact representation for smaller metadata. The backing choice does not affect HIR
meaning.

For example, `admissionBindingTargetRef[bindingHandle]` may store a dense Admission Definition handle. Semantically, it
represents an exact Binding Candidate targeting an exact Definition Candidate. The handle is valid only within its
generation.

The current-reference index remains separate from hot dense-handle lookup (§8.7). Operation and parameter references
retain their nominal scopes (§4.5). Other candidate-reference coordinates remain governed by their owning 1D laws.

Physical encoding remains open. No universal reference tuple or fixed-width key is required. HID and fingerprints may
accelerate lookup without defining reference meaning.

Each 1D retains its own semantic payload. Shared infrastructure must not replace authority-specific meaning with a
universal property model (§4.11).

The resulting structure is:

```text
shared compact HIR infrastructure
+
Contract Interface / Interaction / Operation subject material
+
typed Definition Candidate slabs
+
exact IDL Binding Candidate relations
+
current-generation dense handles
+
separate current-reference material
+
separate provenance
```

This provides a typed semantic surface without requiring a pointer-heavy object graph or making physical storage a
source of semantic ownership.

---

# 25. Non-Normative End-to-End Reference Realization

This non-normative example follows one selected `deposit` Interaction from authored IDL through HIR formation,
visibility, reuse, and reclamation. The layouts and handles illustrate one realization; they do not prescribe a storage
schema.

The source follows the established flat 1D selection model:

```text
interface DepositContract {
    policy        DepositPolicy
    governance    DepositGovernance
    budget        DepositBudget
    capacity      DepositCapacity
    facts : invariants {
        DepositCommand  : ValidDepositCommand
        DepositRecorded
    }

    operation deposit(command: DepositCommand): DepositRecorded {
        manifest {
            flow:
                input             DepositInput
                admission         DepositAdmission
                canonicalization  DepositCanonicalization
                lowering          DepositLowering
        }

        lowering DepositLowering {
            accountIdText -> command.accountId
            amountText    -> command.amountMinor
        }
    }
}
```

`DepositAdmission` remains authored material until the explicit `admission` slot grants it the Admission role. Resolved
HIR contains the resulting Definition Candidate and the distinct Binding Candidate for this Interaction. The Operation
is its selectable realization handle, not the owner of the 1D Contract pipeline (§§4.12–4.14).

## 25.1. Source Acquisition and Frontend Working Storage

A parser may use a compact source arena as private working storage:

```text
byte[]  declKind
int[]   declNameTokenRef
int[]   declFirstSlot
int[]   declSlotCount

byte[]  slotKind
int[]   slotTargetTokenRef

int[]   loweringFirstEdge
int[]   loweringEdgeCount
int[]   loweringSourceTokenRef
int[]   loweringTargetTokenRef
```

The source buffer and token index remain separate. `slotTargetTokenRef` names lexical material, not an exact HIR
reference; neither the authored declaration object nor its token reference is a Definition Candidate reference.

Transient resolution indexes and scratch ordinals are not Visible HIR. They may be released when their remaining
semantic, provenance, and reader obligations are satisfied (§20.1).

## 25.2. Deterministic Layout Planning and Direct HIR Formation

The frontend may form HIR directly in typed storage without first building a candidate-object graph. It resolves IDL
selections and projects the Definition determinants owned by each selected 1D law:

```text
IDL slot selections
    ↓
role-partitioned work ranges
    ↓
1D Definition determinant projection
    ↓
authority-specific direct HIR formation
```

V1 may pre-count Definition Candidates, Binding Candidates, relation rows, and provenance ranges when the saved
allocation cost justifies the scan:

```text
optional pre-count / capacity planning
    ↓
allocate or reserve typed columns
    ↓
deterministic current-generation handle assignment
    ↓
direct-to-slab formation
```

Exact pre-count is optional. Deterministic segmented or chunked storage is also valid (§20.1).

For `deposit`, the hot Binding-to-Definition relation has two steps:

```text
ix = interactionHandle(DepositContract.deposit.interaction)

ab = admissionBindingHandle(deposit.admission)
ad = admissionDefinitionHandle(DepositAdmission)

interactionAdmissionBindingRef[ix] = ab
admissionBindingTargetRef[ab]      = ad
```

The Binding Candidate becomes complete only when `ad` identifies a complete resolved Admission Definition Candidate.
Until then, its selected source handle and Admission determinants remain private working material.

Two IDL selections may share Definition formation or payload when the Admission law makes their differing context
non-determining, while retaining distinct Binding Candidates. Equal payload cannot merge candidates that the owning
identity law distinguishes. Definition-determining context instead produces the distinct Definition Candidate required
by that law (§4.14).

This realization stores Lowering edges as one contiguous relation range:

```text
lower = loweringDefinitionHandle(DepositLowering)
base  = loweringEdgeBase[lower]

loweringSourceCoordinateRef[base + 0]      = coordinateHandle(DepositInput.accountIdText)
loweringTargetParameterRef[base + 0]        = operationParameterHandle(deposit.command)
loweringTargetFactCoordinateRef[base + 0]   = factCoordinateHandle(DepositCommand.accountId)

loweringSourceCoordinateRef[base + 1]      = coordinateHandle(DepositInput.amountText)
loweringTargetParameterRef[base + 1]        = operationParameterHandle(deposit.command)
loweringTargetFactCoordinateRef[base + 1]   = factCoordinateHandle(DepositCommand.amountMinor)

loweringEdgeCount[lower] = 2
```

These handles are generation-local access coordinates for exact semantic references. Ordinary semantic consumers do not
need the source spellings; provenance retains their source locations and spellings.

Direct formation does not perform Contract Establishment.

## 25.3. HIR Seal Verification Boundary

The formed candidate must pass HIR seal verification before becoming visible. The verifier's internal structure remains
outside this example (§§4.8, 19.1):

```text
private candidate material
    ↓
HIR seal verification
    ↓ success
Visible Resolved Contract HIR
```

Passing the check does not establish `DepositAdmission`, `DepositLowering`, or any other Contract authority.

## 25.4. Reference V1 Physical Layout

A V1 implementation need not allocate a heap object for each HIR subject or relation. One JVM layout is:

```text
Generation header
    primitive counts
    product/schema version
    current-reference index metadata
    provenance index metadata

Hot semantic slabs
    Contract Interface / Interaction / Operation subject material
    Interaction -> Binding Candidate handles
    Binding Candidate -> Definition Candidate handles
    authority-specific Definition Candidate columns
    authority-specific relation ranges

Current-reference material
    authority-specific resolved candidate coordinates
    indexes from exact current references to dense handles

Cold side material
    provenance ranges
    authored spelling references
    diagnostic-only source relations

Derived infrastructure outside HIR meaning
    query dependency records
    cached projection fingerprints
    previous-product comparison metadata
    persistent-product metadata
```

High-cardinality data should favor primitive columnar storage. FFM-backed slabs are the preferred option when off-heap
storage, explicit lifetime, or contiguous allocation is beneficial; heap primitive arrays remain valid when measurement
favors them.

One V1 formation path is:

```text
role / determinant resolution
    ↓
layout planning
    ↓
deterministic current-generation handle assignment
    ↓
direct typed formation
    ↓
release only scratch whose semantic / provenance / diagnostic obligations are complete
    ↓
batch HIR verification
    ↓
seal
    ↓
seal generation
    ↓
make generation visible
```

`layout planning` may use exact sizing, bounded capacity, or deterministic segmented storage. No intermediate object
graph is required.

Vertical partitioning lets Admission Establishment read Admission-owned material without scanning provenance,
diagnostics, Lowering edges, or unrelated payloads. Physical partitioning does not transfer semantic ownership.

## 25.5. Visibility and Consumer Access

After successful verification, the sealed slabs become one coherent visible generation. Visibility changes reader access
without rewriting semantic rows:

```text
working slabs W42
    ↓
complete direct formation
    ↓
verify W42
    ↓
seal slabs
    ↓
make manifest G42 visible
```

A consumer reads a typed semantic projection. Hot access may map its exact current reference to a generation-local dense
handle:

```text
Admission Definition Candidate Reference R
    ↓ current-generation reference index
Admission handle 17
    ↓
Admission-owned semantic columns
```

`17` is an access coordinate, not cross-generation semantic identity. The IDL selection remains separately visible as a
Binding Candidate:

```text
DepositContract.deposit.interaction.admission
    ↓ Binding Candidate B
B
    ↓ exact target relation
Admission Definition Candidate R
```

The compiler's semantic consumers can therefore distinguish their declared dependencies:

```text
Admission Definition Establishment
    consumes AdmissionDefinitionProjection(R)

Binding / composition work
    consumes AdmissionBindingProjection(B)

Frontend diagnostic
    consumes AdmissionDefinitionProjection(R)
    consumes ProvenanceProjection(R)
```

Query infrastructure records these semantic reads, not raw handles, slab offsets, or FFM addresses as permanent
dependencies.

## 25.6. Semantic Early Cutoff

Suppose a comment moves `DepositAdmission` to a new source range but leaves its current role, Definition determinants,
exact references, Definition meaning, and IDL Binding unchanged. Only provenance changes:

```text
G42
    AdmissionDefinitionProjection(R42) = S
    AdmissionBindingProjection(B42)    = B
    Provenance(R42)                     = P42

G43
    AdmissionDefinitionProjection(R43) = S
    AdmissionBindingProjection(B43)    = B
    Provenance(R43)                     = P43
```

`R42` and `R43` are distinct current-generation references; neither inherits identity from the other. Before considering
G42 results for comparison, G43 resolves its current role, determinants, and exact references, and establishes
sufficient compiler-owned reuse-validity evidence (§§16–17).

A fingerprint mismatch may rule out equality cheaply, but a match does not prove it. Reuse requires the applicable HIR
projection equality law or collision-safe validation.

When Definition and Binding projections are equivalent, propagation may stop at those boundaries; provenance consumers
still observe `P43`. A changed Binding target invalidates its Binding projection even if the target Definitions have
equal payload. Changed Definition meaning independently affects Definition consumers.

Uncertain determinants or validity inputs require revalidation or recomputation. Thus semantic product dependencies can
be finer than a serialized generation blob.

## 25.7. Generation Transition and Reclamation

A new generation is built beside the existing visible generation; the old slabs are never rewritten in place:

```text
G42 visible and readable
        │
        ├──────── readers may still pin G42
        │
source revision 43
        ↓
construct W43
        ↓
verify W43
        ↓
seal W43
        ↓
make G43 visible
        ↓
G42 superseded for new requests
        ↓
last legal G42 reader releases its pin
        ↓
G42 retired
        ↓
G42 slabs reclaimed
```

Failed W43 formation or verification leaves G42 intact. Reclamation waits until the last permitted G42 reader releases
it; the pinning and retirement mechanism is not prescribed.

## 25.8. Partition-Local Formation and Failure Isolation

A physical HIR partition may contain one semantic product or a deterministic group of related products for local
formation, visibility, reuse, and lifetime management. It does not own Definition or Binding semantics.

A `DepositLowering` query resolves its exact current reference to a local dense handle and reads only the required
Lowering range.

If a Definition Candidate fails resolution, no poison object is inserted into unrelated valid slabs. A Binding Candidate
requiring that failed target cannot satisfy the HIR invariant; independent products remain valid when their determinant
closure is complete (§6).

V1 may form partitions eagerly; V2 may form or repair selected products on demand. Both must expose the same legal HIR
meaning (§18).

---

# 26. Non-Normative Implementation Reading Guide

The reference realization in §25 is a compiler storage example, not a proposed Kotlin domain model or a required
physical schema.

For high-cardinality HIR, the preferred implementation permits direct formation into compact typed storage without
allocating a wrapper object for every semantic row. Temporary frontend objects may assist construction but must not
become the Visible HIR model. Storage choices should follow measured cost, and exact pre-sizing remains optional
(§20.1).

Consumers access typed semantic projections rather than raw storage. Replacing the physical backing, local handle
assignment, or reference index must not require consumers to reinterpret source syntax or change their declared semantic
dependencies.

Implementation compatibility is therefore determined by preserved HIR observations and consumer obligations, not by
class or storage layout compatibility.

---

# 27. Non-Normative Engineering Validation

Compiler QA should provide a deterministic inspection path for HIR. The inspection result is a derived debug product,
not HIR authority.

Validation should cover the following distinct conditions:

- **Edit locality:** A comment-only change preserves semantic projections while updating provenance. A semantic edit
  identifies the affected projections. Moving unrelated declarations must not corrupt current semantic references.
- **Formation equivalence:** Clean and reused formation must produce equivalent semantic observations. Eager and
  demand-driven materialization, exact pre-counting and segmented formation, and different legal worker schedules must
  preserve the same result.
- **Generation safety:** A generation-local handle must not be reused in another generation without resolving or
  validating its current reference. An invalid Definition Candidate must not produce poison HIR in an independent
  semantic product.

Randomized worker completion and reordered independent construction should also preserve deterministic observation order
and provenance relations. Private dense handles may differ when those differences remain unobservable.

Common HIR conformance checks should run after formation and after any private representation preparation claiming to
preserve HIR. Differential validation should compare Primary HIR observations, provenance, and generation visibility.
When persistence claims canonical encoding, its canonical bytes should also be compared.

Authority-specific semantic conformance remains with the owning authority. Independent checks implement the verification
seam of §19.1; neither the inspector nor the testing mechanism establishes HIR meaning.

---

# 28. Consequences

The frontend gains a resolved semantic checkpoint before Contract Establishment. Establishment can consume exact
candidate meaning without repeating frontend resolution, while diagnostics and tooling observe the same meaning without
acquiring Contract authority.

The compiler gains a stable boundary for incremental reuse and future persistence. Semantic products may be reused
independently while provenance changes, without sacrificing coherent generation visibility.

This architecture requires explicit reference identity, projection equality, visibility, and lifetime management. The
cost is accepted to prevent those responsibilities from becoming implicit dependencies throughout the compiler.

The semantic boundary does not prescribe an object-heavy implementation. Compact storage and alternative formation
strategies remain available when they preserve the established HIR obligations.

---

# 29. Rejected Alternatives

## 29.1. Parsed Source Directly to Establishment

This would keep source lookup and syntax interpretation inside authority-owned judgment. It would also make multiple
authoring frontends harder to converge on one semantic boundary.

Rejected.

## 29.2. One Mutable Mega-HIR

A representation that stores syntax, resolved meaning, diagnostics, analysis results, cache state, backend state, and
incremental metadata in one mutable graph would couple unrelated lifetimes and invalidation rules.

Rejected.

## 29.3. Recovery Nodes Inside Visible Resolved HIR

This weakens the meaning of `Resolved` and forces every consumer to distinguish complete meaning from parser poison.
Recovery remains a sibling frontend concern.

Rejected for the visible HIR semantic surface.

## 29.4. One Whole-HIR Dependency Unit

This makes small semantic changes invalidate unrelated consumers and blocks definition-level early cutoff.

Rejected as the only dependency model.

## 29.5. Physical Field Reads as Incremental Dependencies

This would turn layout into incremental architecture and make later storage replacement expensive.

Rejected.

## 29.6. Fingerprint or CAS Identity as Semantic Identity

A hash or storage address can accelerate equality and lookup. It cannot replace the semantic equality law.

Rejected.

## 29.7. Incremental State as Correctness Authority

A compiler that can only produce correct HIR when previous cache or dependency state is present has lost the semantic
source of truth.

Rejected.

## 29.8. Global Compiler Lifecycle State Machine

The compiler does not move linearly from `parsed` to `HIR` to `established` to `optimized` as one mutable global state.
Different products may exist at different generations and lifetimes.

HIR keeps its own visibility lifecycle without becoming the global compiler state machine.

Rejected.

## 29.9. Source Containment as HIR Ownership

Using parser nesting, IDL containment, or host object containment as the permanent HIR semantic ownership relation would
make authoring shape control Definition meaning, visibility, reuse, and lifetime. The same reusable 1D declaration may
be selected by more than one IDL context.

Rejected.

## 29.10. Generation-Local Handles as Persistent Identity

A dense ordinal, arena index, row number, or local interner ID may be efficient inside one generation. Reusing that
value
as a cross-generation or cross-session semantic reference would couple persistence to physical construction order.

Rejected.

## 29.11. User API Shape as 1D Authority

Treating a supported class or carrier shape as a 1D Contract before explicit IDL binding would make the current
authoring
API define Contract role. It would also make future authoring frontends harder to converge on the same HIR meaning.

Rejected.

## 29.12. Binding Context Always Creates a New Definition Candidate

Cloning Definition Candidate meaning for every IDL use would fold Binding context into Definition meaning even when the
owning 1D law does not make that context semantic. It would duplicate reusable definitions and enlarge invalidation.

Rejected.

## 29.13. Cross-Generation Lineage Identity

Carrying predecessor, successor, inherited identity, or transferred identity from one HIR generation into another would
make current meaning depend on compiler history. Previous products may be compared for reuse, but they do not establish
current HIR identity.

Rejected.

## 29.14. One Universal Definition Candidate Coordinate Tuple

Forcing every 1D candidate into one tuple such as authority kind, Contract ID, Version, and local coordinate would move
identity law out of the owning 1D semantics. Some authorities are Interface-local, Operation-local, Machine-local, or
use
other version and coordinate rules.

Rejected.

## 29.15. Compiler Normalization as Hidden Contract Canonicalization

Allowing frontend optimization to collapse distinctions merely because two forms are convenient to store would let
compiler representation decide Contract equivalence. Contract-owned Canonicalization and other authority judgments must
remain explicit.

Rejected.

## 29.16. Merging Definition Candidates by Equal Payload

Two Definition Candidates may have equal current payload without being one semantic entity. Formation computation or
physical payload may be shared when valid, but semantic Definition identity remains owned by the applicable 1D law.

Rejected.

## 29.17. Mandatory Exact Pre-Count Before HIR Formation

Exact pre-count can reduce allocation and copying, but requiring it as architecture would force additional scans and
constrain lazy, segmented, or incremental formation even when another deterministic layout is better.

Rejected as a semantic or universal physical requirement.

## 29.18. Reuse Before Current Determinants Are Resolved

Using a previous product because its key or fingerprint appears unchanged before current role, semantic determinants,
and exact references are known can hide changed inputs and produce stale HIR.

Rejected.

## 29.19. One Semantic Normal Form for Visible HIR

Forcing all semantically equivalent Contract material into one compiler-chosen HIR normal form would let frontend
optimization collapse distinctions owned by Canonicalization or another Contract authority.

Rejected. Visible HIR is a deterministic semantic baseline, not a compiler-owned Contract normal form.

## 29.20. Semantic Access Equality as the Only Representation-Preservation Law

Two backing representations may return the same semantic values while differing in provenance correctness, generation
coherence, stale-reference behavior, or persistent compatibility. Semantic access is central but is not the whole
visible-product contract.

Rejected. Representation preservation also respects provenance, visibility/generation, and applicable persistence laws.

## 29.21. Generic Determinant Metadata as Primary HIR Meaning

Copying formation dependency metadata into every HIR semantic unit would duplicate 1D meaning and make compiler reuse
structure part of HIR semantics.

Rejected. Definition determinants remain an owning 1D formation law, while compiler dependency records remain derived
reuse infrastructure.

## 29.22. Derivable Consumer Views as Primary HIR Fields

Adding reverse indexes, transitive relations, summaries, or consumer-specific convenience fields to HIR because a later
consumer may need them would turn derived knowledge into producer meaning.

Rejected. Such material may be materialized as projections or derived compiler products without becoming Primary HIR
semantic material.

---

# 30. V1 Requirements

V1 must implement the complete Visible Resolved HIR semantic boundary defined in §§4–13. Authority-Owned Establishment
must be able to observe that meaning through the Resolved HIR Candidate Protocol (§8.9), independently of physical
storage.

V1 must preserve the Protocol compatibility and extension laws (§§8.9.1–8.9.2). An explicit numeric Protocol revision or
extension registry is not required when compatibility can be established through other valid compiler-owned means.

V1 must use query-oriented orchestration with explicit HIR-producing inputs and dependency observation at
semantic-product boundaries (§15). It must support valid generation-bound reuse (§16) and deterministic semantic early
cutoff when sufficient current evidence is available and the cutoff is profitable (§17). Incremental processing of every
frontend computation is not required.

Invalid source must be isolated at the smallest sound semantic dependency boundary, allowing unaffected resolved
products to remain available (§6). Entered rejecting judgments must preserve their producer-owned unsuccessful results
under ADR-0074. Unentered later judgments must not receive synthetic results.

V1 must retain a valid clean-recompute path (§19) and the independent HIR Protocol conformance verification seam
(§19.1). Reuse, parallel execution, and physical optimization must preserve the same legal Visible HIR observations as
deterministic clean formation.

---

# 31. Intentionally Open

This ADR fixes HIR's semantic and visibility laws, not its physical schema or formation algorithm. Storage layout,
scheduling, reuse implementation, and reclamation mechanisms remain compiler design decisions.

The HIR Protocol fixes legal semantic observations and compatibility requirements, not concrete accessor APIs, a
complete projection catalog, reference encoding, or compatibility metadata format. Legal projections need not all be
materialized.

Each owning 1D ADR determines its complete Definition Candidate payload, semantic determinants, reference coordinates,
and any Binding context that determines Definition meaning. This ADR imposes no universal candidate record or reference
tuple.

The detailed seal-verifier implementation and authority-specific verification catalog remain outside this ADR. They must
satisfy the conformance obligations of §19.1 without assuming Contract authority.

Common compiler-result representation and recovery mechanisms remain governed by ADR-0074.

---