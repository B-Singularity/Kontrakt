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

- `/0068-fact-contract-explicit-immutable-core-information-sameness-and-uniqueness.md`
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
distinct Binding Candidate.

A Binding Candidate preserves only the exact selection occurrence, selected slot or role, target Definition Candidate
Reference, and any qualifier required by the binding law. It neither copies nor inherits Definition identity.
Surrounding context does not become Binding meaning merely because it is accessible.

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
role coordinate: a selected IDL Binding Candidate Reference or Explicit Absence. Absence means non-selection, not
permission to omit the role, and does not require a universal absence field.

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

Each applicable 1D role's selection must be observable from its owning HIR subject and exact role coordinate as either:

- The selected IDL Binding Candidate Reference.
- **Explicit Absence.**

Missing or unresolved selection is not Explicit Absence. This observation does not determine whether omission is legal
or establish Contract material.

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

Before Resolved HIR becomes visible, the frontend may keep private working state while it resolves and refines authored
material. A resolver may use temporary symbol maps, worklists, intern tables, arenas, or another bounded representation
to perform that work.

This working state is compiler implementation material. It is not Resolved HIR. Establishment, query consumers, tooling,
and other independent downstream work must not depend on a half-resolved builder or on the shape of the temporary
structures used to produce HIR.

The frontend may retain source and working material for diagnostics, tooling, or later frontend work. Retention does not
make that material part of visible HIR meaning.

---

## 11.2. Visible Generation

A HIR generation becomes visible only after the producer has completed resolution, satisfied the HIR resolution
invariant, and sealed the generation.

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

Visibility is a compiler lifecycle boundary. It adds no Contract authority.

A visible generation is immutable to ordinary consumers. The physical realization may use immutable structures,
sealed tables, overlays, snapshots, persistent structures, or another mechanism that preserves this rule.

## 11.3. Visibility Granularity and Materialization

A coherent HIR generation does not require one monolithic physical visibility barrier.

The compiler may make a generation visible as a stable manifest over independently sealed semantic units or projections.
A unit becomes observable only after that unit satisfies the HIR invariant and is associated with the same coherent
generation inputs.

Semantic formation and physical materialization are different. A visible semantic scope must already have closed
current membership and validated HIR candidate meaning for the scope it claims to expose. Physical backing for that
meaning may still be materialized eagerly or on demand.

Demand-driven semantic formation is permitted only when the compiler exposes the smaller semantic scope that has
actually been formed and validated. It must not advertise an unresolved larger generation as complete. When a consumer
receives a visible unit, the same HIR resolution and visibility laws apply as they do to eagerly materialized HIR.

This allows V1 to use a simple eager implementation while preserving a path to fine-grained queries, IDE demand, and
V2 incremental materialization without changing HIR meaning.

The exact visibility and materialization granularity remains a compiler design decision.

---

# 12. HIR Lifecycle and Transition Model

HIR lifecycle is compiler operational meaning. It is not the Contract State / Transition authority defined elsewhere.

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

A failed or cancelled construction candidate is discarded. It does not become a partially visible HIR. If an older
generation is already visible, that failed transition does not invalidate the older generation merely because a
replacement attempt began.

Only a successfully completed visibility transition may supersede the previous current generation. Making the newer
generation visible does not mutate the semantic meaning of the older generation. It creates a new visible result for a
new
explicit input set.

Supersession and reclamation are separate transitions. An older generation may remain readable while a newer generation
is already current.

The compiler must not require one mutable lifecycle enum on every HIR node. Lifecycle may be represented by generation
ownership, product metadata, generation handles, or another mechanism.

HIR architecture must permit lifecycle management at a finer semantic granularity than the whole frontend world when
that is useful. Fine-grained lifecycle units are stable semantic products or projections, not arbitrary physical fields.

A visible semantic product may outlive its construction scratch state while cold derived indexes or source-oriented
material are discarded earlier. The HIR contract does not require every projection, index, provenance expansion, or
decoded body to
remain simultaneously materialized. Selective retention must not make a consumer-visible semantic unit unavailable
while that unit is still visible, valid, and required.

The exact reclamation mechanism remains open.

---

# 13. Snapshot Coherence

A semantic computation that requires one coherent HIR world observes one visible generation.

It must not combine a new definition, an old resolution index, and a half-built relation merely because those pieces are
physically reachable at the same time.

Cross-generation reuse is allowed when an equivalent semantic projection has been validated for the new generation. The
logical consumer still observes that projection as valid input to its current generation. The reuse relation does not
create identity continuity between generations.

Current-generation membership is established from current explicit semantic inputs. Reused physical backing, a live old
page, a matching fingerprint, or a retained previous product does not by itself make that subject a member of the new
generation. Reuse may preserve storage or computation; it does not preserve membership by inheritance.

The same coherence law applies when a consumer combines semantic HIR with provenance. Provenance presented as belonging
to an exact semantic observation must be valid for that observation. Semantic material and provenance may be reused or
refreshed independently, but an invalid or stale relation must not be presented as one coherent current HIR view.
Tooling may deliberately display older semantic material beside newer source or recovery state, but that mixed state is
not one coherent current Resolved HIR observation.

This rule preserves snapshot coherence without requiring the compiler to copy every unchanged definition into every
new physical generation or to use one particular snapshot, epoch, generation-tag, or provenance-storage mechanism.

---

# 14. Query, Product, and Manager Compatibility

Resolved HIR is a compiler product. It is not a query object and it is not owned by a Manager.

A query-oriented compiler may request whole HIR generations or smaller HIR projections. An Analysis Manager may attach
derived results to an exact HIR subject and validity context. A local Pass Manager may orchestrate HIR-preserving
representation preparation before sealing or over a private generation.

None of those infrastructures defines HIR meaning.

HIR must therefore provide stable semantic subjects, clear generation validity, and an observable read surface without
requiring one query scheduler, one Analysis Manager API, or one pass pipeline.

Derived knowledge remains outside HIR. If several consumers need the same expensive calculation, Kontrakt may expose a
shared derived product rather than storing the result inside unrelated HIR definitions.

Query and analysis scope should follow explicit Definition, Binding, and projection boundaries when that gives a
smaller valid dependency boundary. A consumer that needs one Definition projection should not be forced to depend on the
full frontend generation merely because the first physical implementation stores both in one allocation domain.

---

# 15. Dependency Observation

Query and incremental infrastructure may record which HIR semantic products or projections a computation consumed.

Dependency observation must occur at a semantic product boundary rather than at an accidental storage boundary.

A read of slab offset `N`, object field `x`, or table row `R` is not by itself a permanent compiler dependency law.
Physical representation must remain replaceable.

The following graphs remain different:

```text
HIR semantic relations
compiler query dependencies
build / artifact dependencies
source provenance relations
```

A query edge does not create Required Basis, Basis Binding, Applicability, or another Contract relation.

Relevant external or ambient inputs to a HIR-producing computation must be explicit. Hidden clock, locale, random state,
mutable global state, or undeclared environment state cannot silently change the HIR result.

---

# 16. Cache and Reuse Law

Cache is work avoidance.

It is not HIR authority and it is not Contract authority.

A cached HIR product may be reused only after the compiler has established compatibility with the requested semantic
product. A stale, corrupt, missing, or incompatible entry falls back to valid computation or produces an explicit
compiler failure when recomputation itself is impossible.

Deleting the cache must not change HIR meaning.

Warm-cache and cold-cache compilation must produce the same observable HIR result for the same explicit inputs.

The same law applies to persistent CAS-like storage, in-memory memoization, generation-local caches, and future remote
reuse. Their retention policy does not become HIR semantics.

Semantic equality and physical reuse eligibility are separate questions. Two semantic projections may be equal while a
previous physical product remains ineligible for reuse because compiler-owned compatibility or current-validity
requirements are not satisfied. Conversely, physical availability never substitutes for producer-owned semantic
equality when that equality is required.

Reuse of one HIR semantic product does not imply reuse of separately owned provenance, diagnostic, Compiler Result, or
other compiler products. Each product retains its own validity and observation law.

---

# 17. Early Cutoff

HIR supports semantic early cutoff.

A previous product may be considered for reuse only after the current computation has resolved the current 1D role, the
complete current semantic determinant set required for the product, and the exact current semantic references on which
that product depends. Compiler-owned reuse-validity inputs must also permit comparison with the previous product.

Current reuse validity does not require re-executing every computation that originally produced or verified the previous
product. It requires sufficient current-validity evidence. Kontrakt may establish that evidence through retained
dependency evidence, producer-owned semantic equality, validated summaries, explicit compatibility judgments, selective
revalidation, or another sound mechanism. The exact routing and validation mechanism is compiler design.

Validation may proceed from cheaper evidence toward narrower semantic work. When coarse evidence is sufficient to prove
that the relevant semantic projection remains valid, deeper validation is unnecessary. When coarse evidence changes or
is insufficient, Kontrakt may descend only into the affected dependency region or semantic projection. A mismatch in a
fingerprint, HID, Merkle root, epoch, or other routing summary means that deeper validation may be required; it does not
by itself prove that Contract or HIR meaning changed.

If the current semantic projection is equivalent to the previous projection under that projection's producer-owned
semantic equality law, downstream products whose exact semantic inputs are limited to that projection need not be
invalidated further.

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

Early cutoff is based on consumer-visible semantic equality, not on the fact that a cache entry exists, a fingerprint
matches, or the aggregate HIR generation changed. A fingerprint or another compact summary may route validation or
reject
a mismatch quickly. It does not prove HIR or Contract equality by itself.

Selecting a previous product for comparison is compiler reuse work and does not create a cross-generation identity
relation.

A provenance-only change may therefore refresh source projection and diagnostics while allowing semantic downstream
products to remain reusable.

Early cutoff is an optimization. Failing to take the cutoff may cost time. Taking an unsound cutoff is a correctness
failure. When reuse validity cannot be established with sufficient current evidence, Kontrakt performs the required
revalidation or follows the deterministic clean recomputation path.

# 18. Incremental Architecture Boundary

V1 uses query-oriented compiler orchestration and may use generation-bound in-memory reuse, explicit dependency
recording, and local early cutoff.

V1 does not need to implement the final V2 incremental engine.

V2 may add persistent dependency state, cross-session HIR reuse, incremental parsing and resolution, change-frontier
propagation, delta maintenance for suitable domains, lazy repair, or other algorithms.

No one repair strategy is required across the whole frontend.

A small semantic projection may use memoized validation while another domain recomputes from scratch. A relational
analysis may later justify delta maintenance. A cheap frontend product may simply rerun because dependency bookkeeping
would cost more than recomputation.

Every strategy is constrained by the same HIR result law.

```text
incremental result
    == HIR semantic result required by clean computation
```

If that equivalence cannot be established, Kontrakt uses the deterministic clean path.

---

# 19. Full-Recompute Reference Path

HIR correctness must not depend on persistent incremental state.

Kontrakt must retain a conceptually complete path that can rebuild HIR from the explicit frontend inputs without using
previous HIR products.

This path is required for corruption recovery, differential validation, cache-off testing, and incremental-equivalence
testing.

A future persistent compiler may optimize how often this path runs. It may not make old incremental state the only
source from which correct HIR meaning can be recovered.

## 19.1. HIR Protocol Conformance and Independent Verification Seam

Every product exposed as Visible Resolved HIR must remain checkable against the common HIR Protocol and the applicable
authority-specific HIR surface. Common HIR conformance checks own Protocol and well-formedness invariants. They do not
reimplement the owning 1D Contract semantics or become a second Contract authority.

The deterministic clean formation path is the reference correctness realization for comparing optimized compiler paths.
Cached, parallel, restored, persistent, or incremental formation must preserve the same producer-defined legal HIR
observations as valid clean formation. Conformance is therefore defined over legal semantic observations rather than
physical bytes, table layout, dense handles, allocation order, cache identity, or storage representation.

Clean-versus-optimized differential comparison is necessary but is not assumed to detect a semantic bug shared by both
paths. The architecture must permit independently derived conformance checks against the legal HIR observation surface.
Reference judgments, golden vectors, differential execution, metamorphic relations, property-based generation, or
translation-validation-like checks may realize that seam. No one oracle technology or test harness is part of HIR
semantics.

A conformance checker observes already-defined HIR law. Its success does not establish HIR or Contract meaning. A
product
that fails a required HIR Protocol conformance check cannot be exposed as valid Visible Resolved HIR. Compiler-side
unsuccessful-result handling remains governed by ADR-0074.

The exact checker implementation, oracle mix, execution schedule, fuzzing strategy, and QA harness remain compiler
design
and verification work.

---

# 20. HIR Formation, Transformation, and Optimization Law

HIR formation has a semantic plane and a physical realization plane.

The semantic plane is fixed by Sections 4.12 through 4.16. It resolves the explicit IDL role, projects the owning 1D
Definition determinants, refines authored material under that 1D law, forms exact candidate references and meaning,
completes the IDL Binding Candidate, verifies the result, and makes Resolved HIR visible.

The physical realization may fuse those logical stages. Fusion does not remove their invariants.

## 20.1. Authority-Specific Authoring Refinement

Frontend refinement may erase syntax-only or host-only structure after its meaning is known. It may resolve aliases,
replace lexical references with exact candidate references, remove authoring sugar, and form the explicit absence or
relation material already required by the owning 1D law.

Frontend refinement must not invent a new semantic equivalence. It must not collapse a distinction merely because the
compiler can store the result more compactly. The `Canonicalization Contract`, Admission judgment, Fact Establishment,
Applicability judgment, Policy selection, Governance judgment, and other Contract authorities remain at their owning
boundaries.

## 20.2. Compiler Representation Preparation

Compiler-owned representation preparation may change how resolved HIR material is encoded or stored only when the
visible HIR obligations in Section 8.10 remain preserved.

Legal representation work may include columnar layout, primitive-width packing with exact value preservation, range
compaction, hot/cold column separation, segmentation, paging, lossless compression, out-of-line storage, deterministic
encoding, dictionary encoding with exact recovery, immutable payload interning, and physical payload deduplication.

Physical sharing does not merge semantic entities. Two Definition Candidates may share immutable payload bytes, interned
material, or a compressed backing block while remaining distinct Definition Candidates with distinct references and
provenance relations.

A compiler representation rule must not perform value case folding, Unicode normalization, floating-point distinction
collapse, implicit default insertion, semantic relation inference, semantic declaration elimination, duplicate semantic
edge elimination, Definition Candidate merging, or another transformation that requires an owning Contract law to decide
that a distinction is irrelevant.

The term `Canonicalization` remains reserved for the Contract authority when semantic equivalence is involved. Compiler
storage may use canonical encoding or deterministic observation rules without claiming Contract canonical meaning.

## 20.3. Physical Formation Optimization

Compiler-owned formation optimization may reduce work or improve locality without changing HIR meaning.

Legal techniques include direct-to-slab formation, role-partitioned work ranges, dense current-generation handles,
primitive columnar storage, compact relation ranges, collision-safe interning, parallel fill behind deterministic
visibility, and early release of producer scratch that has no remaining semantic, provenance, diagnostic-evidence, or
reader obligation.

Exact pre-count and exact sizing are permitted when profitable. They are not HIR semantic requirements. A realization
may instead use bounded sizing, deterministic segmented slabs, deterministic chunked formation, or another
representation
that preserves the same visible result.

The compiler may share formation computation or physical payload storage across uses when the relevant current inputs
are equivalent. That sharing does not merge two distinct Definition Candidates. Semantic entity identity remains owned
by the applicable 1D law.

## 20.4. Formation Reuse

Formation reuse is work avoidance. It does not change the logical formation law.

Formation-time reuse is compiler formation machinery rather than a consumer-defined HIR observation surface. It may use
current explicit semantic determinants, exact current references, and separately compiler-owned validity evidence before
a current Visible HIR projection exists. That machinery does not define a new HIR projection or semantic equality law.

The compiler may reuse a previous Definition or projection only after current semantic determinants, exact references,
current semantic membership, and compiler-owned validity inputs establish that the previous product is a valid
comparison candidate. Reuse never supplies a missing current determinant, establishes current identity, or carries
membership forward from the previous generation.

When those conditions cannot be proven, Kontrakt follows the valid clean formation path.

## 20.5. Visible HIR Boundary

Visible Resolved HIR is read-only to ordinary consumers. A compiler optimization does not mutate visible candidate
meaning in place.

Authoring refinement, representation preparation, compaction, interning, verification, or other work used to produce one
visible HIR product occurs before sealing. Later compiler work that needs a different optimized semantic product
exposes a derived compiler product or a later IR instead of silently changing already-visible HIR meaning.

Visible HIR is not required to have one semantic normal form that collapses equivalent Contract material. It is a
verified deterministic semantic baseline that preserves every distinction owned by the applicable Contract laws.

When persistence or reproducible persistence requires canonical compiler bytes, that encoding is a separate compiler
persistence law. Optional compression or storage packaging occurs outside the semantic identity and equality law.

The legal optimization relation is therefore:

```text
same explicit semantic inputs
    ↓
clean / cached / incremental / parallel formation
    ↓
same Visible Resolved HIR obligations
```

A physical optimization that cannot preserve that relation is not a legal HIR optimization.

# 21. Concurrency

Parallel frontend work is allowed only behind deterministic visibility.

Workers may build independent private candidate material. Their completion order must not determine semantic identity,
exact candidate references, visible semantic relations, provenance assignment, deterministic observation order,
persistent product content, or another declared observable result.

Private dense handles, scratch-table positions, worker-local interner IDs, and other ephemeral physical coordinates may
differ across legal executions when those differences remain private. They must not leak into semantic references,
persistent keys, fingerprints that claim stable meaning, diagnostic evidence ordering, serialized products, or another
deterministic boundary.

A deterministic partition, merge, or equivalent visibility law resolves concurrent candidate work into one coherent HIR
generation. Concurrent interning or first-writer races must not decide semantic equality, observable ordering, or stable
persistent output.

Cancellation must not expose a partial generation. A stale worker result for an older input or generation must not
replace a newer valid visible generation.

The exact lock, epoch, persistent-structure, actor, work-stealing, transaction, or parallel-fill mechanism remains
implementation.

# 22. Persistence and Cross-Session Products

A future implementation may persist HIR products across compiler sessions.

Persistence has its own protocol. Runtime HIR representation equivalence does not by itself establish persistent
artifact compatibility. A persisted product must define the compiler schema or product version, integrity rules,
reference remapping law, and any canonical compiler encoding required for reproducibility. Contract Version remains a
separate semantic concern.

An incompatible persistent product is discarded or migrated by an explicit compiler product rule. It is not silently
reinterpreted as current HIR meaning. Persistent-format compatibility and HIR Protocol compatibility are separate. A
format migration may make bytes readable without proving that the restored material can provide every semantic
observation required by the current Protocol. Conversely, a Protocol-compatible semantic product may be stored in a new
persistent format without changing Contract Version or HIR meaning.

Content-addressed storage may be used to deduplicate immutable products. A CAS address remains a storage identity, not a
Contract identity or HIR semantic law. Compression or packaging bytes also remain storage realization. If a canonical
persistent encoding is used as content-addressed input, optional physical compression should not redefine semantic
identity or HIR equality.

A restored persistent HIR product must pass the same semantic resolution boundary as a newly computed product.
Persistent-product integrity, persistent-format compatibility, and current reuse validity are separate questions.
Readable bytes, valid checksums, schema compatibility, successful deserialization, a content-address match, or the
existence of prior verification do not independently establish that a restored product is valid for the current
compilation.

Prior verification is reusable compiler evidence. It may replace re-execution of an expensive check only when Kontrakt
can establish that the semantic determinants, exact semantic references, and compiler-owned reuse-validity inputs on
which that verification depended remain valid for the current product. Current-validity evidence may come from retained
dependency evidence, producer-owned semantic equality, validated summaries, explicit compatibility judgments, selective
revalidation, or another sound mechanism. Reuse does not require re-running every original resolution or verification
step when sufficient current-validity evidence is available.

Validation should descend only as far as required to establish current validity. A cheap higher-level summary may permit
direct reuse when it soundly covers the relevant dependency set. If that evidence changes or is insufficient, Kontrakt
may narrow the affected dependency region and revalidate only the necessary semantic projections. HID, fingerprints,
Merkle roots, epochs, or similar summaries may accelerate routing and validation. They remain compiler evidence and do
not become HIR semantic equality, Contract identity, or authority.

The current validity set must be complete for the product but need not contain unrelated ambient environment. A
compiler,
platform, schema, provider, or environment input participates only when it can affect that product's semantic result,
reference interpretation, verification validity, or other declared reuse condition. A broad version label does not
substitute for a more specific meaning-affecting input when that distinction matters.

When current reuse validity cannot be established, Kontrakt performs the required revalidation or follows the
deterministic clean formation path. Persistent state may make that path unnecessary in the common case. It may not make
old persistent state the only source of correct HIR meaning.

A persistent product must remap or validate current exact HIR references before it exposes generation-local dense
handles. Raw dense handles, page locations, or physical offsets are not persistent semantic references.

---

# 23. Diagnostics and Tooling

Frontend diagnostics may observe exact sealed HIR semantic subjects or producer-defined legal projections and combine
them with the separate provenance relation. When a diagnostic explains an unsuccessful HIR-related compiler judgment,
it observes the unsuccessful meaning through the Compiler Result Protocol defined by ADR-0074 rather than reconstructing
that meaning from HIR internals, exception state, stack shape, or diagnostic text. A diagnostic that reports another
compiler condition does not require a synthetic Compiler Result.

A diagnostic may combine HIR observations with separately owned compiler evidence when those relations are valid for the
occurrence. HIR preserves only the semantic and provenance observations that its own Protocol promises. Formatted
messages, severity or warning policy, source-anchor selection, conflict reduction, fix construction, root-cause
analysis,
rendering, and engineering evidence remain Diagnostic or other compiler-subsystem products. Engineering diagnostic mode
does not widen the HIR legal observation surface.

A diagnostic renderer does not own HIR meaning or HIR rejection meaning. A source location does not own HIR identity. A
diagnostic occurrence may fail to render or may require provenance refresh without changing the underlying compiler
result. A pre-HIR rejection does not require fabricated HIR merely to support diagnostics. Its owning compiler result
and
source or recovery material remain the diagnostic basis until valid HIR exists.

Tooling and IDE features may observe exact sealed HIR subjects or producer-defined legal projections together with valid
provenance when resolved meaning exists. They may separately consume source, recovery, incomplete, speculative, or other
editing-time frontend products. Those products do not extend the Resolved HIR Protocol and are not partially valid HIR.

The batch compiler and an IDE must not become two different Contract semantic engines. When material is fully resolved,
both use the same HIR semantic law. Tooling-derived completion, ranking, repair, suggestion, navigation products, or
other new judgments remain tooling products rather than HIR semantic projections. Source-to-HIR lookup may locate
already-resolved HIR meaning; it does not become an independent semantic-resolution authority.

Tooling may retain or display stale semantic material beside newer source or recovery state for continuity, but that
mixed state must not be represented as the current coherent Resolved HIR observation for the newer source.

---

# 24. Non-Normative Reference Semantic Model

The normative sections above define HIR meaning. This section gives one concrete compiler-oriented realization of that
meaning. It is intentionally close to the physical direction already used elsewhere in Kontrakt so that the semantic law
can be implemented without first translating it into a heap object model.

The example does not define the required storage schema. It demonstrates the required separation.

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

The reference model uses typed columnar families and generation-local dense handles.

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

Each array name denotes a logical column. A JVM realization may back high-cardinality columns with primitive arrays or
FFM `MemorySegment` storage. Small metadata may use another compact representation when that is cheaper. The semantic
contract does not depend on the backing choice.

A hot relation such as `admissionBindingTargetRef[bindingHandle]` may physically contain one dense Admission Definition
handle. Semantically it still means that one exact Binding Candidate targets one exact Definition Candidate. The handle
is valid only in the current generation.

The current-reference index is separate from the hot dense-handle path. Operation references are nominal semantic
references scoped by the exact Contract Interface, and Operation parameter references are nominal coordinates scoped by
the exact Operation. Their physical encoding remains open. Other candidate-reference coordinates remain owned by the
relevant 1D laws. HIR does not require one universal tuple or one fixed-width key. HID or fingerprint may accelerate an
index without becoming the reference meaning.

Each 1D authority keeps its own semantic payload family. Kontrakt must not replace those authority-specific meanings
with
one generic property map or universal edge record.

The important shape is:

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

This gives consumers a typed semantic surface without requiring a pointer-heavy object graph or making physical storage
into semantic ownership.

---

# 25. Non-Normative End-to-End Reference Realization

This example follows one selected Interaction and its Operation from source acquisition to a visible HIR generation.
It shows how the Interaction Manifest binds flat 1D selections for that interaction, how one IDL slot grants a 1D role,
how Definition Candidate and Binding Candidate material stay separate, and how the compiler can still use direct
primitive formation, verification, visibility, early cutoff, and reclamation.

The source shape follows the existing IDL decisions.

```text
interface DepositContract {
    policy        DepositPolicy
    governance    DepositGovernance
    budget        DepositBudget
    capacity      DepositCapacity
    facts         DepositFacts
    invariants    DepositInvariants

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

`DepositAdmission` is authoring material until the explicit `admission` slot selects it for the Admission role. The
selected source form is evidence for frontend refinement. The visible HIR surface is the resolved Admission candidate
meaning and the exact binding from the selected Interaction context to that candidate. The Operation remains the exact
selectable realization handle related to that Interaction; it does not own the flat 1D Contract pipeline.

## 25.1. Source Acquisition and Frontend Working Storage

The parser may retain source-oriented syntax in a compact arena. The exact parser representation is not HIR.

One possible working layout is:

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

The source buffer and token index remain separate. `slotTargetTokenRef` still points to lexical material. It is
therefore
not a valid HIR reference and the source declaration object is not the HIR Definition Candidate reference.

Resolution may use bounded transient structures such as an open-addressed symbol index, work queues, scratch ordinals,
or intern tables. Those structures belong to the producer episode. They are not visible HIR.

The frontend may release scratch structures as soon as the information has been formed into resolved HIR candidate
material.

## 25.2. Deterministic Layout Planning and Direct HIR Formation

The reference path does not require a graph of candidate objects that is later copied into production tables.

The frontend first resolves exact IDL slot selections and partitions work by 1D role. Each authority-specific formation
kernel then projects only the Definition-determining context owned by that 1D law.

```text
IDL slot selections
    ↓
role-partitioned work ranges
    ↓
1D Definition determinant projection
    ↓
authority-specific direct HIR formation
```

A V1 implementation may pre-count Definitions, Binding Candidates, relation rows, and provenance ranges when that avoids
reallocation at acceptable scan cost. It may then allocate exact or bounded-capacity column storage once.

```text
optional pre-count / capacity planning
    ↓
allocate or reserve typed columns
    ↓
deterministic current-generation handle assignment
    ↓
direct-to-slab formation
```

Exact pre-count is not required by the HIR contract. A deterministic segmented or chunked layout is equally valid when
it produces the same observable HIR result.

For the selected `deposit` Interaction, the final hot relation is conceptually two-step.

```text
ix = interactionHandle(DepositContract.deposit.interaction)

ab = admissionBindingHandle(deposit.admission)
ad = admissionDefinitionHandle(DepositAdmission)

interactionAdmissionBindingRef[ix] = ab
admissionBindingTargetRef[ab]      = ad
```

The Binding Candidate is completed only after `ad` denotes a complete resolved Admission Definition Candidate. Before
that point the frontend may retain the role-selected source handle and the Admission-owned determinant input as private
working state.

If another IDL explicitly selects the same Admission declaration and the Admission law says that the differing context
does not change Definition meaning, the compiler may share the Definition formation result or its physical payload while
retaining each exact Binding Candidate. If the two uses denote distinct Definition Candidates under the owning identity
law, equal payload does not merge those semantic entities.

If an owning 1D law makes the context Definition-determining, its determinant projection forms the distinct Definition
Candidate required by that law.

The Lowering payload remains a contiguous relation range in this reference realization.

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

These handles are current-generation access coordinates. The semantic relations they realize are exact HIR references.
The source spellings are no longer consulted by ordinary downstream semantic consumers.

Original spelling and source ranges remain reachable through provenance.

The candidate remains non-authoritative. Direct formation does not perform Contract Establishment.

## 25.3. HIR Seal Verification Boundary

Before visibility, the candidate product is checked against the Resolved Contract HIR boundary. This reference model
does not prescribe the verifier subsystem, scan structure, diagnostic code layout, or authority-specific check catalog.

The only requirement fixed here is the boundary:

```text
private candidate material
    ↓
HIR seal verification
    ↓ success
Visible Resolved Contract HIR
```

Seal verification does not establish `DepositAdmission`, `DepositLowering`, or another Contract authority. The
detailed verifier architecture remains owned by separate verifier ADRs and designs.

## 25.4. Reference V1 Physical Layout

A production-oriented V1 baseline should not require one heap object per HIR subject or relation.

A representative JVM layout is:

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

High-cardinality material should prefer primitive columnar storage. FFM-backed slabs are the default physical direction
when off-heap storage, explicit lifetime, or larger contiguous regions make them profitable. Heap primitive arrays
remain
a replaceable option where measurement favors them.

One efficient V1 path may use exact pre-count and pre-sizing, but the architecture does not require that strategy:

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

`layout planning` may mean exact pre-sizing, bounded capacity, or deterministic segmented/chunked storage. There is no
required intermediate object graph between resolved frontend facts and visible HIR storage.

Vertical partitioning remains important. Establishment for Admission should not have to touch provenance bytes,
diagnostic strings, Lowering edge slabs, or unrelated authority payloads merely because they share one HIR generation.
The physical partition does not become semantic ownership.

## 25.5. Visibility and Consumer Access

After verification succeeds, Kontrakt makes the sealed slab set visible as one coherent HIR generation.

The visibility transition changes reader access. It does not rewrite semantic rows in place.

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

A consumer obtains a typed semantic projection. When hot access is needed, the implementation resolves or validates the
current exact candidate reference and maps it to a current-generation dense handle.

```text
Admission Definition Candidate Reference R
    ↓ current-generation reference index
Admission handle 17
    ↓
Admission-owned semantic columns
```

The number `17` is not the reference meaning and it is not recorded as cross-generation identity.

The IDL use remains separately observable as a Binding Candidate.

```text
DepositContract.deposit.interaction.admission
    ↓ Binding Candidate B
B
    ↓ exact target relation
Admission Definition Candidate R
```

Conceptually, compiler products consume boundaries such as:

```text
Admission Definition Establishment
    consumes AdmissionDefinitionProjection(R)

Binding / composition work
    consumes AdmissionBindingProjection(B)

Frontend diagnostic
    consumes AdmissionDefinitionProjection(R)
    consumes ProvenanceProjection(R)
```

Query infrastructure records those semantic product reads. It does not make a raw handle, slab offset, or FFM address
the
permanent dependency law.

## 25.6. Semantic Early Cutoff

Assume a comment moves the authored `DepositAdmission` declaration to another source range without changing its current
Admission role, Definition-determining inputs, exact semantic references, Definition Candidate meaning, or exact IDL
binding.

The new generation may contain different provenance while the semantic projections remain equivalent.

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

`R42` and `R43` are current-generation references. The compiler does not claim that one inherited the identity of the
other.

For G43, the frontend first resolves the current role, current Admission Definition determinants, and current exact
references. Previous-product infrastructure may then select the G42 projections for comparison if compiler-owned
validity inputs also permit that comparison.

A fast fingerprint may reject equality quickly when values differ. A matching fingerprint is not by itself Contract or
HIR semantic authority. Reuse must follow the HIR projection equality law or another collision-safe validation rule.

When semantic equality and reuse validity are established, propagation may stop at the Definition and Binding projection
boundaries. Products that consume provenance still observe `P43`.

If the binding target changes, the Binding projection changes even when both target definitions happen to have equal
payload meaning. If the Definition meaning changes while the binding remains structurally the same, Definition-dependent
products recompute. These are separate change dimensions.

If any required determinant or reuse-validity input is uncertain, the compiler recomputes the affected product.

This is why the HIR product boundary is finer than one serialized generation blob even when physical storage is grouped
into large slabs.

## 25.7. Generation Transition and Reclamation

A new generation is produced beside the old one. The old visible slabs are not mutated into the new meaning.

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

A failed W43 formation or verification leaves G42 intact. Visibility and reclamation are separate lifecycle operations.

The exact pinning or reclamation mechanism remains open. Reference counting, epochs, arenas, RCU-like retirement, or
another deterministic-safe strategy may realize the same lifecycle law.

## 25.8. Partition-Local Formation and Failure Isolation

The compiler does not need one mutable HIR tree for the whole project.

A physical HIR partition may contain one semantic product or a deterministic group of related products. Its purpose is
formation locality, visibility, reuse, and lifetime management. The partition does not become Definition ownership or
Binding meaning.

A query that needs `DepositLowering` resolves the exact current candidate reference to its current dense handle and
reads
only the required Lowering ranges.

If one Definition Candidate fails resolution, no poison object is inserted into unrelated valid definition slabs. A
Binding Candidate that depends on the failed target cannot satisfy the HIR invariant. Independent semantic products may
remain valid when their own determinant closure is complete.

V1 may form all partitions eagerly. V2 may materialize or repair selected products on demand. Both paths must expose the
same HIR semantic meaning for the same valid inputs.

---

# 26. Non-Normative Implementation Reading Guide

The reference realization should be read as a compiler storage model, not as a proposed Kotlin domain model.

The parser owns source-oriented syntax arenas. Resolution owns conversion from lexical references to exact HIR targets.
HIR formation writes resolved candidate meaning into typed slabs. HIR verification checks compiler-semantic
well-formedness. Visibility exposes sealed generations. Provenance owns source origin. Query and analysis infrastructure
own dependency, validity, and reuse state. Establishment owns Contract authority.

The production baseline should avoid a per-node heap object graph for high-cardinality HIR material. The preferred
physical direction is deterministic dense ordinals, primitive columns, direct ranges, compact indexes, and FFM-backed or
heap-primitive slabs selected by measured cost.

Temporary frontend objects are permissible only when they are bounded construction aids and do not become the visible
HIR model. The normal hot path should be able to form resolved working material directly into typed HIR storage without
allocating one wrapper object per semantic row or relation. Exact pre-sizing is one optimization, not a semantic
requirement.

Consumers should see typed semantic projections rather than raw backing storage. A projection implementation may
reduce to a few handle-indexed slab reads. The consumer contract must survive a later replacement of heap primitive
arrays by FFM segments, a different local handle assignment, or a different current-reference index.

A good implementation test is therefore stronger than class-shape compatibility. Replacing the physical HIR backing
must not require Establishment, diagnostics, query consumers, or generated-product consumers to reinterpret source
syntax
or change the semantic dependency they declare.

---

# 27. Non-Normative Engineering Validation

The HIR implementation should have a deterministic inspection path for compiler QA. The inspection form is a derived
debug product, not HIR authority.

It should be possible to compare clean and reused compilation at the Primary HIR semantic boundary. A comment-only edit
should be able to demonstrate semantic equality with changed provenance. A semantic edit should identify the projections
that changed. Randomized worker completion order should not change visible semantic observations, provenance relations,
or deterministic observation order. Private dense-handle values may differ when they remain outside those boundaries.

Common HIR conformance checks should also be runnable in compiler tests after HIR formation and after any private
representation preparation that claims to preserve the HIR contract. Differential tests should compare Primary HIR
semantic observation, provenance relation, visibility/generation behavior, and persisted encoding when the product
claims a canonical persistent form. Authority-specific semantic conformance remains owned by the corresponding semantic
owner rather than by a compiler-wide generic HIR verifier. Valid HIR remains a precondition for visibility to later
consumers.

Compiler QA should also move unrelated declarations and reorder independent construction work while checking that
current semantic references and HIR projections remain correct. Tests should reject use of a generation-local handle in
another generation unless current reference material is resolved or validated into a new handle. An invalid Definition
Candidate should not create poison HIR for an independent semantic product. Eager and demand-driven materialization,
exact pre-count and segmented formation, clean and reused formation, and different legal worker schedules must produce
the same semantic inspection result. When a persisted format claims canonical encoding, those tests should also compare
its canonical bytes.

These checks realize the independent conformance seam in Section 19.1. A debug inspector, golden vector, differential
harness, metamorphic relation, or other verification mechanism does not become HIR semantic authority.

---

# 28. Consequences

The frontend gains a strict semantic checkpoint before Contract authority.

Establishment can remain authority-focused because it receives exact candidate meaning rather than frontend search
problems. Diagnostics and tooling can share the same resolved meaning without becoming semantic authorities of their
own.

The compiler also gains a stable product boundary for query orchestration and future persistence. Whole-generation
snapshot coherence can coexist with Definition- and Binding-level reuse. Provenance can refresh independently from
semantic meaning.

The cost is explicit architecture work. HIR needs stable semantic references, generation visibility, projection
boundaries, equality rules, and lifecycle ownership. Those costs are accepted because leaving them implicit would move
the same complexity into every downstream subsystem and make V2 invalidation depend on accidental representation.

This ADR does not require HIR to be physically object-heavy. The stronger semantic boundary allows the implementation to
use more aggressive tables, slabs, interning, persistent structures, or other compact representations behind the
visible-product contract. It also allows formation stages to be physically fused, skipped through validated reuse, or
realized
with different layout strategies as long as their logical invariants and visible result remain unchanged.

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

V1 must provide a real Resolved Contract HIR boundary between frontend resolution and Establishment.

Visible HIR must satisfy the resolution invariant and remain read-only to ordinary consumers. The compiler must keep
source provenance separate from semantic equality. Contract Interface, Interaction, Operation, Definition Candidate,
and Binding Candidate semantic units must remain independently addressable where their semantic relations require it,
even if the first physical implementation groups their storage.

V1 must expose a Primary HIR Semantic Surface that contains Contract Interface, Interaction, and Operation subjects,
complete 1D-owned Definition Candidate meaning, complete IDL Binding Candidate meaning, exact direct semantic
references,
and owning-law distinctions required by valid consumers. Generated host APIs are downstream products and do not become
Primary HIR meaning. Reverse indexes, summaries, query edges, fingerprints, and other derived material do not become
Primary HIR meaning merely because V1 chooses to materialize them.

V1 must preserve provenance relation, visibility/generation behavior, and any V1 persistence obligations independently
from semantic equality. HIR semantic access is the semantic observation boundary, not the only visibility-preservation
obligation.

V1 frontend formation must obtain a 1D role from explicit IDL binding rather than from user API shape alone. It must
project Definition-determining context through the owning 1D law before forming Definition Candidate meaning. A complete
Binding Candidate is formed only after its exact Definition Candidate target exists.

V1 must resolve a Contract Version Claim to the exact authority-scoped Version candidate coordinate required by HIR
while
leaving authoritative `Version Binding` to Establishment. Basis Requirement Law and Applicability Law remain HIR meaning
when owned by a Definition Candidate; actual Required Basis instances, Basis Binding, Applicability results, and
Complete
Basis remain later authority-owned material.

The same role-qualified declaration may be reused by several IDL bindings when the owning 1D determinant and identity
laws permit one reusable Definition Candidate. Equal payload alone does not merge distinct Definition Candidates.

V1 must preserve exact current Contract Interface, Interaction, Operation, Operation-parameter, Operation-result,
Definition Candidate, and Binding Candidate references separately from generation-local dense handles. Operation and
parameter coordinates are nominal semantic coordinates, and the result reference is an Operation-scoped semantic result
position. None is established by source path, declaration ordinal, generated host symbol, JVM parameter slot, or JVM
descriptor. A dense handle may realize a hot relation inside one generation but cannot become semantic identity,
persistent reference, or history.

V1 may physically fuse role resolution, determinant projection, authority-specific refinement, Definition Formation,
and Binding completion. Fusion must preserve the logical formation order and each stage invariant.

V1 query orchestration must be able to identify explicit HIR inputs, observe product dependencies at stable semantic
boundaries, and reuse a visible result only under a valid generation or equivalent validity rule.

V1 must support deterministic semantic early cutoff where current determinants, exact references, current membership,
producer-owned semantic equality, and reuse validity are already available and the cutoff is profitable. It need not
incrementalize every frontend computation.

V1 may use exact pre-count and pre-sized primitive slabs. It may also use deterministic bounded, segmented, or chunked
formation when measurement favors them. No one layout-planning strategy is part of HIR meaning.

V1 may choose physical partitions for locality, formation, visibility, and lifetime. Those partitions must not create
Definition ownership or Binding meaning, and ordinary consumers must not depend on their topology.

Invalid source must be isolated at the smallest sound semantic dependency boundary. Unaffected semantic products may
remain available to frontend diagnostics and tooling even when the overall compilation cannot succeed. An entered HIR or
frontend judgment that rejects its owned requirement preserves an explicit producer-owned unsuccessful compiler result;
an unentered later HIR judgment receives no synthetic result.

V1 HIR-related compiler results must follow ADR-0074. The exact unsuccessful meaning and bounded Direct Basis remain
producer-owned, diagnostics remain downstream consumers, recovery remains separate, and a legal consumer must not
reconstruct rejection meaning through recursive reference chasing, stack inspection, query topology, or producer-private
storage.

V1 must expose Visible Resolved HIR through the Resolved HIR Candidate Protocol realized by the HIR Semantic Access
Boundary. Establishment-facing observation must preserve the typed HIR semantic reference domain, Definition Candidate
Projection, IDL Binding Candidate Projection, and valid fine-grained semantic projections defined in Section 8.9 without
requiring ordinary consumers to depend on the physical HIR layout. Projection semantics, completeness, and equality are
producer-owned; diagnostics, tooling, reuse, and other consumers may select legal projections but do not define
consumer-specific HIR meaning. The first backing representation may be primitive slabs without making that choice part
of HIR meaning.

V1 must obey the Protocol evolution and extension laws in Sections 8.9.1 and 8.9.2. Required semantic material cannot be
silently ignored or downgraded. V1 need not persist one numeric Protocol revision or extension registry when its
internal
producer and consumer boundary can establish compatibility by other compiler-owned means.

V1 may use interning, physical payload sharing, deterministic encoding, compression, hot/cold partitioning, or another
representation optimization only under the HIR representation-preservation law. Equal payload does not merge semantic
Definitions, and compiler representation preparation does not perform Contract Canonicalization.

Cache-off and clean-recompute execution must remain valid and must agree with reused execution. Worker count, legal
scheduling order, cache state, and layout strategy must not change observable Visible HIR meaning. V1 must preserve the
Protocol conformance seam in Section 19.1 so producer-defined legal observations can be compared across clean, reused,
parallel, or representation-changed formation without making the checker a second semantic authority.

# 31. V2 Evolution Seam

V2 may add persistent HIR products and finer frontend repair without changing this ADR.

Possible work includes persistent semantic projections, demand-driven materialization, incremental lexing and parsing,
incremental resolution, determinant-local Definition Formation, provenance-only refresh, cross-session early cutoff,
dynamic dependency repair, domain-local delta maintenance, and adaptive switching between repair and rebuild.

Previous-product metadata may help locate comparison products or avoid work. It remains compiler reuse infrastructure.
It does not create predecessor identity, successor identity, or semantic lineage for current HIR.

V2 must not constitutionalize HID-, fingerprint-, or Merkle-equality as HIR semantic equality or as the only legal early
cutoff mechanism. Compact identities and hierarchical summaries may route validation, localize likely change, or provide
cheap evidence under a producer-owned validity regime. A mismatch means only that the cheap evidence cannot justify the
old cutoff without further validation. A match permits cutoff only when the producer-owned equality and current-validity
law soundly establishes that the consumer-visible semantic projection is unchanged. Otherwise Kontrakt narrows the
affected region, revalidates, or uses the deterministic clean path.

A V2 formation engine may avoid rebuilding one Definition Candidate only after current role, current 1D semantic
determinants, exact current references, and compiler-owned reuse-validity inputs are sufficient to validate the reused
result. Missing history or persistent state falls back to clean formation.

V2 may choose a different physical formation architecture from V1. Persistent segmented slabs, mapped immutable pages,
content-addressed chunks, database-like product storage, or another representation remain legal behind the same Primary
HIR semantic, provenance, visibility/generation, and persistence laws.

V2 may also carry explicit Protocol revision, capability, extension, or migration metadata when persistent or
distributed
products require it. Such metadata is compatibility and routing evidence. It does not merge Contract Version,
authority-specific HIR surface evolution, common Protocol evolution, persistent-format evolution, or compiler generation
into one revision domain. Unknown required semantic extensions and unsupported surfaces remain fail-closed unless an
explicit compatible projection or validated migration preserves the required observation.

Those techniques remain compiler realization.

V2 must preserve the same resolution invariant, current-reference law, semantic equality, authority boundary, logical
formation law, visibility law, Protocol compatibility law, extension-handling law, and determinism-first rule.

# 32. Intentionally Open

This ADR does not freeze the HIR physical schema.

It does not decide whether V1 uses objects, tables, primitive arrays, slabs, persistent structures, or mixed storage. It
does not require exact pre-count, one global allocation pass, one segmented layout, or one chunking policy. Those are
physical formation choices constrained by the logical formation and determinism laws.

It fixes Contract Interface, Interaction, Operation, Definition Candidate, and IDL Binding Candidate as primary HIR
semantic subject categories for the current frontend model. It also fixes Operation reference as an Interface-scoped
nominal semantic reference, Operation parameter reference as an Operation-scoped nominal semantic coordinate, and the
Operation result reference as an Operation-scoped semantic result position. Section 8.9 additionally fixes the logical
Establishment-facing Protocol families: the typed HIR semantic reference domain, Definition Candidate Projection, IDL
Binding Candidate Projection, and valid fine-grained semantic projections.

It does not fix the remaining complete per-1D payload, 1D candidate reference coordinates, the complete concrete
projection catalog, concrete object or method API shape of the HIR Semantic Access Boundary, bulk-access forms, or
physical handle encoding. It also does not require one numeric HIR Protocol version, semantic-versioning scheme,
capability bitmap, extension registry, extension-coordinate encoding, or compatibility-negotiation wire format. It does
not select a query scheduler, Analysis Manager API, Pass Manager API, fingerprint algorithm, CAS implementation,
reclamation algorithm, serialization format, compression strategy, canonical persistent encoding, or V2 repair
algorithm.

It also does not define the complete semantic payload of each 1D Contract. The owning 1D ADR must still state the
candidate meaning, Definition determinants, and semantic distinctions that HIR has to preserve.

It does not require every deterministic projection to be stored. A projection may be recomputed, memoized, materialized,
or persisted according to compiler product policy without becoming Primary HIR meaning. Each reusable semantic product
or projection still has one producer-owned semantic equality law.

The detailed HIR seal-verifier architecture and authority-specific verification catalog remain outside this ADR.
Separate verifier ADRs and designs may refine those checks without moving owning Contract judgment into HIR seal
verification.

This ADR also does not define the common physical or logical representation of compiler availability effects, boundary
completion, multiple unsuccessful-result membership, recovery decisions, trust-domain invalidation, or deep compiler
evidence. Those are common compiler-result concerns owned by ADR-0074. HIR fixes only the HIR-specific producer
boundary:
valid Visible HIR is published on success, while an entered HIR-owned rejecting judgment preserves its exact
unsuccessful
compiler result without fabricating later HIR meaning.

The exact component set of each 1D Definition Candidate Reference is not decided by this ADR. It remains owned by the
applicable 1D identity and reference law. This ADR does not require one universal `Authority + Contract Id + Version +
Local Coordinate` tuple. It also does not decide which Binding context is Definition-determining for a 1D unless the
owning ADR already says so.

The exact reclamation algorithm, lease or epoch mechanism, memory policy, and retention cost policy remain design work.
The semantic lifetime law itself is fixed by Sections 8.9.3 and 12: a legal observation cannot outlive the validity of
the
HIR generation or projection that owns it, stale observations cannot be reinterpreted, and physical storage cannot be
reclaimed while a legal HIR consumer still requires that observation.

# 33. Non-Normative Engineering Basis

This decision is consistent with several production systems, but none of them is a template for Kontrakt.

`rustc` lowers AST into HIR after removing syntax structure that later analysis does not need. Its HIR uses local
compiler addressing and separate stable forms for incremental work rather than treating one physical ID as every kind of
identity. Its query system also shows why larger products need smaller semantic projections and why query dependency
must
not be confused with the semantic relation represented by HIR. Kontrakt adopts the separation lesson without adopting
cross-generation lineage identity or Rust source-containment rules as Contract semantics.

Kotlin K2 FIR shows that logical resolution phases can strengthen one frontend semantic representation without requiring
one physically separate full IR for every phase. The relevant architectural lesson is the phase invariant, not FIR's
object model. FIR symbol access also shows the value of giving consumers a stable semantic access surface instead of
requiring them to reach through a symbol into mutable backing declarations.

LLVM and MLIR make IR validity explicit. Their verifier boundaries let later passes assume well-formed input and require
transformations to return valid IR. MLIR also separates a common IR substrate from dialect-owned semantics, which is a
useful comparison for preserving distinct 1D Contract vocabularies over shared compiler infrastructure. Its pass
isolation rules also demonstrate why independently processed units need explicit mutation and partition boundaries for
parallel work. Kontrakt adopts these invariant and isolation lessons without adopting LLVM or MLIR's universal operation
model or symbol-table containment as HIR Definition identity.

Swift's Request Evaluator separates derived computations, dependency tracking, and cached results from the underlying
semantic representation. This supports keeping query and analysis state outside HIR meaning while still allowing
fine-grained demand-driven compiler work.

Bazel Skyframe uses immutable computed values and change pruning. Its documentation also records a deliberate preference
for repeatable clean-build equivalence over incremental mutation that is hard to validate. Kontrakt adopts the same
priority at the HIR boundary: incrementality is useful only inside deterministic equivalence.

LLVM CAS demonstrates immutable content-addressed compiler products and deduplication. Kontrakt may use the same kind of
storage technique without turning a content address into semantic authority.

Database MVCC and Linux RCU show a more general visibility lesson. Readers need a coherent view, while making a
new version visible and reclaiming an old version are separate problems. Kontrakt uses that principle for HIR
generations
without adopting database transactions or RCU as compiler semantics.

DBSP and recent incremental data-flow research show that delta maintenance can be powerful when a domain has a suitable
update algebra. They do not justify one compiler-wide incremental algorithm. Recent empirical work on incremental
program analysis also reports meaningful memory and consistency costs, reinforcing the need for a clean deterministic
fallback and domain-specific repair choices.

The *Modern Compiler Architecture 01-15* material provides the same general constraints: stage boundaries are defined by
new invariants, logical stages are separate from physical materialization, semantic identity should be separated from
volatile provenance, visible material should not expose partial construction, and incremental architecture begins with
stable result and dependency boundaries rather than a cache implementation.

---

# 34. Final Law

Resolved Contract HIR is the deterministic visible compiler-semantic form of fully resolved Contract candidates before
Contract authority. It is Kontrakt's highest Contract-semantic IR because established Contract meaning constrains later
realization compilation, machine formation, optimization, and target lowering. Its level is defined by semantic
altitude,
not by proximity to user realization source.

Contract Interface, Interaction, and Operation are primary Contract-semantic subjects in HIR. They are distinct from the
generated Interaction and Operation host APIs that later project established Contract meaning into realization-facing
compiler products.

Explicit IDL binding grants the 1D role. HIR keeps reusable Definition Candidate meaning separate from contextual
Binding
Candidate meaning. Operation and Operation-parameter references are nominal current semantic coordinates scoped by their
Contract semantic subjects, and the Operation result is an Operation-scoped semantic result position. Exact current
references remain separate from dense handles, compiler history, and physical storage.

Contract Version claims are resolved in HIR to exact authority-scoped Version candidate coordinates without creating
`Version Binding`. Basis Requirement Law and Applicability Law remain in HIR when they belong to Definition meaning;
actual Required Basis instances, Basis Binding, Applicability results, and Complete Basis remain at the later authority
boundaries that own them.

It preserves all candidate meaning required by the owning Contract laws and removes frontend ambiguity that later
semantic work must not repeat.

Its Primary HIR Semantic Surface is independent of physical layout, query topology, manager topology, cache state,
generation-local addressing, and incremental algorithm. Authority-Owned Establishment observes that surface through the
Resolved HIR Candidate Protocol realized by the HIR Semantic Access Boundary. The Protocol exposes a typed semantic
reference domain, Definition Candidate Projection, IDL Binding Candidate Projection, and valid fine-grained semantic
projections without imposing one universal candidate record or one physical access mechanism. Provenance remains a
separate visible relation rather than part of candidate payload or HIR semantic equality. Visibility/generation behavior
and applicable persistence rules remain separate obligations of the same Visible HIR product.

HIR generations may expose deterministic semantic projections so compiler products can be reused without turning
consumer views or physical storage into producer meaning. Each projection uses its producer-owned semantic equality law;
equal projection payload does not merge distinct semantic subjects. Derived analysis and query knowledge remains outside
Primary HIR semantics even when it is materialized for performance.

Lifecycle and transition belong to compiler visibility and validity. They do not become Contract State or Transition.
A new generation is made visible instead of mutating already-visible meaning in place, and old-generation reclamation is
separate from visibility.

HIR formation follows a logical semantic order from explicit IDL role through 1D determinant projection and
authority-specific refinement to complete Definition and Binding candidates. A physical implementation may fuse those
stages or avoid work through validated reuse, but it cannot make layout, scheduling, cache state, or optimization policy
part of HIR meaning.

Compiler representation preparation may change encoding, layout, compression, interning, or physical sharing only when
Primary semantic observation, provenance relation, visibility/generation behavior, and applicable persistence behavior
are preserved. It may not create a compiler-owned semantic normal form that preempts Contract Canonicalization or
another
owning authority.

Protocol evolution keeps Contract Version, authority-specific HIR semantic-surface evolution, common HIR Candidate
Protocol evolution, persistent-product format evolution, and compiler generation separate. Compatibility is established
by preservation of the legal semantic observation required by the consumer, not by revision equality alone. Unknown or
unsupported required semantic material is fail-closed unless an explicit compatible projection or validated migration
preserves the required observation. Only material already classified by an understood Protocol rule as ignorable may be
skipped, and semantic meaning may not be silently downgraded, fabricated from absence, or reassigned through a reused
extension coordinate.

HIR-related unsuccessful compiler results follow ADR-0074. The first exact owning compiler judgment establishes the
result. Result meaning remains owned by that producer and is observed through the Compiler Result Protocol. Diagnostics,
recovery, and orchestration do not create or rewrite that meaning. A later HIR or Establishment stage that was never
entered has no synthetic unsuccessful result. Recovery, poison, unresolved placeholders, and invalid partial material do
not become Visible Resolved HIR merely because later tooling or diagnostics need to continue.

One Visible HIR meaning may expose several producer-defined legal projection families, and different consumers may
select the projections required by their own work without defining new HIR semantics. Establishment consumes the
Resolved
HIR Candidate Protocol. Frontend diagnostics consume sealed HIR subjects or legal projections plus separate provenance
and, when they explain an unsuccessful compiler judgment, the owning Compiler Result Protocol. Tooling consumes sealed
HIR observations plus separately owned provenance while keeping recovery or partial editor material outside HIR. Reuse
infrastructure selects producer-defined semantic projections and combines their producer-owned equality relations with
separately compiler-owned validity evidence.

After Establishment, authoritative downstream consumers use established or canonical surfaces rather than reinterpreting
HIR candidate material as authority. A HIR Protocol view is valid only within its owning visible generation or
projection
validity domain, and cross-generation use requires explicit current-reference remapping or validation. Canonical
Contract
World creation alone neither forces HIR retention nor makes stale HIR observation legal.

Caching, persistence, and incremental repair may avoid work. HID, fingerprints, Merkle summaries, epochs, and similar
compact evidence may accelerate validation and change localization, but they do not define HIR semantic equality and do
not prove semantic change merely because they differ. Early cutoff may stop propagation when a producer-defined semantic
projection is unchanged under its equality law and the separately owned current-validity requirements are satisfied.
Reused backing does not inherit semantic identity or current generation membership. Lazy physical materialization is
permitted, but a visible semantic scope cannot claim unresolved or unvalidated membership. None of those mechanisms may
change the result that clean deterministic computation would produce.

Every Visible HIR product remains subject to the common Protocol and its applicable authority-specific surface. Common
HIR conformance checking verifies Protocol and well-formedness obligations without reimplementing owning 1D Contract
semantics. Clean, reused, parallel, persistent, and incremental paths are compared through producer-defined legal HIR
observations rather than physical representation. Independently derived conformance mechanisms may test that boundary,
but they observe existing HIR law and do not establish new HIR or Contract authority.

The authority boundary remains:

```text
Resolved Contract HIR
    ↓
Authority-Owned Establishment
    ↓
Canonical Contract World
```