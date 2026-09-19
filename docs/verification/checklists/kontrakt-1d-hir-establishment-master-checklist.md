# Kontrakt 1D Contract HIR–Establishment Master Derivation and Verification Checklist

> Recommended repository location: `docs/verification/checklists/kontrakt-1d-hir-establishment-master-checklist.md`
>
> Purpose: apply the common laws of ADR-0071 `Resolved Contract HIR` and ADR-0063 `Establishment` to every 1D Contract
> without semantic omissions, while preserving the authority boundary between frontend resolution, HIR, Establishment,
> Established Material, and downstream compiler products.
>
> This document creates no new Contract authority. It is an audit and derivation document. It detects decisions that are
> missing from an owning 1D ADR and sends those decisions back to that ADR rather than silently defining them here.
>
> Re-audit basis: the current project material for ADR-0046, ADR-0047, ADR-0055, ADR-0059, ADR-0063, ADR-0071, the
> proposed ADR-0074 boundary, `What Contract Is`, and the existing `kontrakt-hir-protocol-review-checklist-updated.md`
> were cross-checked again before this revision.
>
> Translation rule: this English edition preserves every substantive checklist family and decision from the Korean
> re-audited edition. New items added during this pass are explicitly additive; they do not replace or compress the prior
> material.

---

# 0. Re-Audit Conclusion

The common semantic architecture of ADR-0071 and ADR-0063 is now substantially closed. However, a per-1D derivation
checklist still needs stronger controls than a direct reading of those ADRs alone provides. Several omissions,
cross-document inconsistencies, and implementation traps remain possible if each 1D ADR is reviewed independently.

This re-audit therefore preserves the prior checklist and strengthens the following areas:

1. **1D catalog source-of-truth and the Output Presentation drift.**
2. **Authority scope, selection scope, and explicit cross-scope relations.**
3. **The boundary between Definition determinants and Binding, Basis, Applicability, attribution, and provenance.**
4. **Duplicate, collision, coverage, merge, completeness, and singularity laws.**
5. **Run/world pinning and the prohibition on ambient `current` reads.**
6. **The boundary between one complete Established unit and partial visibility.**
7. **The distinction between semantic existence, current validity, applicability, retention, visibility, and outward
   publishability.**
8. **The prohibition on ambient nondeterminism as a hidden semantic determinant.**
9. **The dependency of Accepted ADR-0071 on compiler-result rules still associated with Proposed ADR-0074.**
10. **Terminology collisions between compiler vocabulary and Contract-authority vocabulary.**
11. **Protocol evolution, unknown required semantic extensions, compatibility, and downgrade prevention.**
12. **Membership-closure validity for reused complete-set projections.**
13. **Source, provenance, HIR, and Established snapshot coherence.**
14. **Independent conformance paths and clean-path equivalence.**
15. **Complete separation between semantic distinctions and physical materialization.**
16. **HIR-to-Establishment handoff integrity: Establishment must neither recover missing candidate meaning nor add
    hidden semantic inputs.**
17. **Reference-kind safety: exact typed references must not become interchangeable because one physical handle format
    can encode them all.**
18. **Adversarial and resource-safety QA seams: malformed, oversized, deeply nested, cyclic, corrupted, or hostile
    compiler inputs must not force semantic compromises.**

At the common-architecture level, no additional major semantic axis is currently obvious outside this document. That
does **not** mean the 1D ADRs are complete. If applying this checklist reveals a new authority-specific semantic
question, that question belongs in the owning ADR and must not be absorbed into this checklist as a universal schema.

---

# 1. Vulnerabilities and Preconditions That Must Be Resolved or Explicitly Marked

## 1.1. CRITICAL — 1D Catalog Drift: Output Presentation

The currently available ADR-0046 catalog includes `Interface Surface Contract` but omits `Output Presentation Contract`.

The currently available ADR-0047 Section 12 also proceeds from `Publication` directly to `Diagnostic Evidence`, with no
`Output Presentation` entry.

However, the following treat Output Presentation as an independent Contract authority:

- the `What Contract Is` family of documents;
- Accepted ADR-0059;
- later project handoff and architecture material.

The row set used by the 1D matrix therefore cannot be considered formally closed until the source-of-truth drift is
repaired.

### Recommended Working Rule

Keep `Interface Surface Contract` under ADR-0046 as a separate frontend control surface. Use the following **19 1D
authorities** as the working matrix:

```text
Input
Admission
Canonicalization
Lowering
Fact
Invariant
State
State Transition
Explicit State Machine Manifest
Failure
Publication
Output Presentation
Diagnostic Evidence
Diagnostic Retention
Version
Policy
Budget
Capacity
Governance
```

Audit `Interface Surface Contract` outside those 19 rows as a **Control / Primary Semantic Surface**.

Formal use of this matrix should be preceded by a repair of the ADR-0046/0047 catalog drift.

---

## 1.2. HIGH — Accepted ADR-0071 Depends on Compiler-Result Rules Still Associated with Proposed ADR-0074

ADR-0071 delegates HIR seal rejection, unentered later stages, recovery, availability consequences, and the Compiler
Result Protocol boundary to ADR-0074.

The current project material still identifies ADR-0074 as `Proposed`.

The following checklist areas therefore have a strong common direction but should not be treated as finally Accepted
through ADR-0074 until that dependency is resolved:

```text
compiler-owned unsuccessful result
unentered later judgment
recovery ownership
availability / boundary-completion result
multiple unsuccessful-result aggregation
trust-loss / indeterminate compiler result
Compiler Result Protocol observation
```

### Status Rule

Use one of the following where appropriate:

```text
PROVISIONAL-COMMON
BLOCKED-BY-ADR-0074
```

Perform a final re-audit after ADR-0074 reaches an accepted state or its responsibilities are moved elsewhere.

---

## 1.3. HIGH — Governance Exact Scope and Singularity Still Contain Owner-Specific Open Decisions

ADR-0063 intentionally does not define Governance selection, scope, validity, singularity, or local Binding shape.

The common checklist must therefore not pre-establish any of the following:

```text
one global Governance
one universal governed scope
one universal current Policy World
one implicit cross-scope compatibility law
```

Those decisions must be closed by the Governance-owning ADR.

---

## 1.4. HIGH — Do Not Assume One Universal Atomic Establishment Transaction

ADR-0063 requires each authority to establish its own complete meaning. It does not require one global transaction
spanning every authority, and it does not permit a semantically incomplete result to masquerade as complete merely
because some coordinates have already been written.

Avoid both extremes:

```text
A. Every Established result must participate in one global atomic transaction.
B. A semantically single complete result may expose partially authoritative coordinates during construction.
```

Each 1D must answer:

- What is the independently established semantic unit?
- If several coordinates form one complete Established result, when is that unit complete?
- Can an ordinary consumer ever observe partial authoritative state?
- May independent sibling Definitions become visible independently?

The **semantic completeness unit is owned by Contract law**. The physical commit, publication, epoch, lock, CAS, or
snapshot mechanism remains Design.

---

## 1.5. HIGH — Ambient `current` and Implicit World Reads Are Dangerous

ADR-0063 rejects universal ambient context such as `currentVersion`, `currentPolicy`, `currentState`,
`currentGovernance`, and `currentEverything` as an Applicability model.

This matters especially for Policy, Governance, Budget, Capacity, Version, State, Failure, Publication, and any
authority whose result depends on a run/world context.

Each such 1D must state:

- whether an exact run/world binding is a semantic input;
- whether that input must be an exact Established Reference;
- whether runtime singleton, mutable global, thread-local, service locator, or ambient `current` lookup is forbidden;
- who establishes a run-pinned binding when pinning itself is Contract meaning.

---

## 1.6. HIGH — Duplicate, Collision, Coverage, Merge, and Singularity Laws Must Be Owned Per 1D

The common Establishment law already rejects conflicting Definition Meaning at the same exact authoritative identity
coordinate. That is not enough to close each 1D.

Every 1D may still need rules for:

```text
missing coordinate
duplicate coordinate
unknown coordinate
duplicate selector
overlapping selector
multiple applicable candidate
same identity + conflicting meaning
same payload + distinct identity
implicit merge
explicit merge
complete coverage vs partial coverage
```

A generic compiler deduplication policy must not decide these semantics.

Each owning 1D ADR must close its own collision, coverage, merge, completeness, and singularity laws.

---

## 1.7. MEDIUM-HIGH — Semantic Existence Is Not Current Usability

Once Established, source-owned meaning is semantically immutable. That does not imply that the material is currently
usable everywhere.

The following states are distinct:

```text
exists as Established Material
retained physically
current-valid for a compiler product
applicable to this use
selected in this run/world
visible to this consumer
allowed to be Published outward
```

Historical Established meaning does not disappear merely because it is not applicable to the current context.

---

## 1.8. MEDIUM-HIGH — Ambient Nondeterminism Must Not Become a Hidden Determinant

The deterministic Establishment law implies a strong audit rule: if any of the following can change Established meaning,
the relevant value must become explicit semantic material, an exact Established context reference, or material governed
by an owning law.

```text
wall clock
randomness
process environment
thread identity
worker order
filesystem discovery order
classpath order
network timing
cache state
runtime profile
```

Otherwise the same semantic basis can produce different Established meaning.

---

## 1.9. MEDIUM — Terminology Collisions Need an Explicit Gate

Kontrakt deliberately uses terms that are also common compiler terms. The authority domains must remain distinct.

```text
Contract Publication
    != compiler visibility / product publication

Canonicalization Contract
    != compiler canonical form / normalization

Lowering Contract
    != compiler IR lowering

Failure Contract
    != compiler unsuccessful result / exception / crash

Diagnostic Evidence / Retention Contract
    != compiler diagnostics product / recovery evidence

Version Contract
    != compiler generation / schema / artifact revision

State / State Transition Contract
    != HIR/compiler lifecycle state

Fact Contract
    != compiler-derived fact / analysis result

Policy / Budget / Capacity / Governance Contract
    != compiler option / scheduler policy / internal resource budget

Input / Output Presentation
    != host method parameter / return value by itself

Operation semantic subject
    != generated host method / JVM method identity
```

---

## 1.10. HIGH — HIR-to-Establishment Handoff Must Be Semantically Non-Amplifying

The earlier checklist strongly specified HIR and Establishment independently but did not make the bridge explicit
enough.

For every 1D, Establishment must be able to prove that each semantic input belongs to exactly one of these classes:

```text
legal observation from the Resolved HIR Candidate Protocol
separately Established prerequisite admitted by the owning law
explicit authority-owned judgment input formed at the Establishment boundary
```

Establishment must not obtain meaning from:

```text
authored source reopening
host-object topology
IDL parent traversal that was not part of the legal Binding Candidate
compiler query topology
cache state
physical HIR containment
backend realization state
ambient mutable globals
```

Conversely, HIR must not pre-establish later authority-owned results merely to make Establishment convenient.

This is the handoff non-amplification law:

```text
legal HIR observation
+ separately owned authoritative prerequisites
+ owning Establishment law
    -> Established meaning

not

incomplete HIR
+ hidden compiler reconstruction
    -> apparently Established meaning
```

---

## 1.11. MEDIUM-HIGH — Typed Reference Domains Must Remain Disjoint Even If the Encoding Is Shared

Kontrakt may encode several semantic reference kinds with one primitive integer width, one HID family, or one compact
tagged handle. Physical encoding reuse must not erase reference-kind distinctions.

The implementation must not make the following interchangeable merely because their backing type is the same:

```text
Interface Reference
Interaction Reference
Operation Reference
Operation Parameter Reference
Definition Candidate Reference
IDL Binding Candidate Reference
Established Definition Reference
Established Occurrence Reference
Version Reference
Basis Binding Reference
authority-specific Established Reference
```

Cross-kind casts, unchecked integer substitution, or universal untyped handle APIs create a high-risk semantic confusion
channel.

---

## 1.12. MEDIUM-HIGH — Hostile and Resource-Amplifying Inputs Need a Separate QA Seam

This master checklist is primarily semantic. It must still leave an explicit verification seam for inputs that are
semantically invalid or physically dangerous.

Examples include:

```text
extreme nesting or depth
oversized collection/cardinality
integer length/count overflow
malformed persistent records
corrupted reference tags
cyclic physical graphs presented where the semantic model is acyclic
hash-flooding or adversarial lookup patterns
pathological duplicate declarations
compressed or encoded material with explosive expansion
very large provenance/evidence fanout
```

The Contract law must not be weakened to make those cases easier to implement. The compiler instead needs bounded
parsing, bounded acquisition, explicit limits, iterative traversal where needed, overflow-safe arithmetic, corruption
detection, and adversarial QA.

These controls are generally `REALIZATION-ONLY`, `DESIGN`, or `QA`, unless a Contract authority explicitly owns a
semantic bound.

---

# 2. Checklist Status System

Every item must receive one of the following statuses. Blank status is not allowed.

```text
DEFINED
    The owning law has closed the exact semantic meaning.

NOT-APPLICABLE
    The semantic category does not exist under the owning law.

FORBIDDEN
    Creating the meaning or relation is structurally forbidden.

OPEN
    The owning ADR still has to decide it.

PROVISIONAL-COMMON
    A common direction exists, but a dependency ADR is not yet Accepted.

BLOCKED-BY-ADR
    The item cannot be closed before a prerequisite ADR is settled.

DERIVED-COMPILER-PRODUCT
    The item is compiler-derived knowledge/product, not Contract authority.

REALIZATION-ONLY
    The item is a physical/compiler implementation concern.

CATALOG-DRIFT
    Source-of-truth documents disagree about catalog membership or naming.
```

---

# 3. Protocol Exposure Classification

For every relevant HIR and Established Semantic Protocol item, record one of the following separately from semantic
status.

```text
EXPOSE
    A legal consumer must be able to observe it directly.

GUARANTEE
    It need not appear as a direct payload field, but a legal consumer may rely on it.

RETAIN-HIDE
    The producer product needs it, but the current consumer must not observe it.

SEPARATE
    It belongs to another relation, compiler product, or protocol.

ERASE-PERMITTED
    It may be removed only after no legal observer remains and semantic preservation is established.

FORBIDDEN
    It must not enter the semantic protocol.
```

---

# 4. Per-1D Result Recording Template

Every 1D row should record at least the following metadata.

```text
1D Authority:
Owning ADR:
Status:
Selection Scope:
Authority Scope:
Definition Exists?:
Occurrence Exists?:
Other Established Material?:
Definition Candidate Reference:
Definition Determinants:
Binding Candidate:
Version Sensitivity:
Basis Requirement Law:
Applicability Law:
HIR Projection Family:
HIR Seal Requirements:
Establishment Judgments:
Established Reference Family:
World / Backing Placement:
Established Protocol Projections:
Failure / Non-entry Boundary:
Current-Validity / Reuse Law:
Cross-Scope Relations:
Open Decisions:
Evidence / ADR Sections:
```

---

# 5. Catalog / Authority / Scope Checklist

## CAT-01 — Catalog Membership

- [ ] Is this authority an exact member of the current 1D catalog?
- [ ] Does the catalog name match the owning ADR name?
- [ ] Has another document split, merged, or renamed it?

## CAT-02 — Interface Surface vs 1D Authority

- [ ] Is `Interface Surface Contract` prevented from becoming a parent authority over this 1D?
- [ ] Is the Interface subject retained as a Primary HIR semantic subject without absorbing local 1D authority?

## CAT-03 — Owning Authority

- [ ] What exact Contract authority owns the meaning?
- [ ] Is the authority kind sufficient, or is an exact direct sub-authority required?

## CAT-04 — Authority Scope

Select the exact applicable scope.

```text
Interface
Interaction
Operation
Flow
Core
State-Machine Surface
Whole-Machine
Run
Diagnostic retention domain
other explicitly declared scope
```

- [ ] Is the scope independent of source containment inference?

## CAT-05 — Selection Scope

- [ ] Is selection operation-local through a slot?
- [ ] Is selection an enclosing-interface binding?
- [ ] Is selection owned by a State-Machine selection surface?
- [ ] Is it a standing interface-level declaration?
- [ ] Is another explicit selection law used?

## CAT-06 — Role Grant

- [ ] What exact declaration grants the role?
- [ ] Is the role prevented from being inferred from carrier shape, annotation, class inheritance, or runtime type?

## CAT-07 — Cross-Scope Relation

- [ ] May the authority consume material from another scope?
- [ ] If yes, who owns the explicit relation?
- [ ] Is the cross-scope relation prevented from arising through ambient lookup or implicit compatibility?

## CAT-08 — No Scope Inheritance

- [ ] Is a child authority prevented from being inherited automatically from a parent scope?
- [ ] Is Policy/Governance/State from one scope prevented from becoming ambiently applicable to another scope?

## CAT-09 — Declared Absence

- [ ] Is absence legal for this 1D?
- [ ] Is explicit absence semantic material?
- [ ] Are omission and explicit absence distinct?

## CAT-10 — Default

- [ ] Does a default exist?
- [ ] Does the owning law define that default explicitly?
- [ ] Is a backend-convenience default prevented from becoming a semantic default?

---

# 6. Source / Acquisition / Frontend Input Checklist

This section audits the point at which pre-HIR material enters HIR meaning.

## SRC-01 — Authoring Source

- [ ] What legal authored source forms exist for this 1D?
- [ ] Are source families such as `.kontrakt`, selected carrier material, and sibling coordinate declarations explicit?

## SRC-02 — Carrier Law

- [ ] If a host carrier exists, is it a data-only boundary?
- [ ] Are control flow, callbacks, getter behavior, and hidden lookup prevented from becoming sources of meaning?

## SRC-03 — Source Identity vs Semantic Identity

- [ ] Are file path, source line, and AST-node identity excluded from semantic identity?

## SRC-04 — Source Provenance

- [ ] Is provenance a separate relation?
- [ ] Is a one-subject-one-span model avoided unless explicitly required?

## SRC-05 — Mutable Acquisition

- [ ] If mutable source/carrier material is accepted, is there an exact snapshot/copy/freeze boundary?
- [ ] Can post-acquisition mutation no longer change HIR meaning?

## SRC-06 — TOCTOU / Source Coherence

- [ ] If the source revision changes during resolution, is a coherent source view guaranteed?
- [ ] Is stale provenance prevented from being presented as an exact current relation to semantic material?

## SRC-07 — Lexical Resolution

- [ ] Is lexical ambiguity removed before Visible HIR?
- [ ] Does name resolution produce exact semantic targets?

## SRC-08 — Host Structure Erasure

- [ ] Are source-only nesting and host-type containment prevented from becoming Definition identity?

## SRC-09 — Recovery Separation

- [ ] Are parser recovery nodes, incomplete syntax, and poison material excluded from valid HIR Candidates?

## SRC-10 — Generated Artifact

- [ ] Is generated host API material prohibited as a source from which Contract meaning is reconstructed?

---

# 7. HIR Definition Candidate Checklist

## HIR-D01 — Definition Existence

- [ ] Does this 1D have reusable Definition meaning?
- [ ] If not, is it explicitly marked `NOT-APPLICABLE`?

## HIR-D02 — Primary Definition Subject

- [ ] What exactly does one Definition define?

## HIR-D03 — Complete Candidate Meaning

- [ ] Does the HIR Candidate preserve the complete pre-authority meaning required by the owning Establishment law?
- [ ] Can Establishment judge the Definition without reopening authored source?

## HIR-D04 — Declared Determinants

- [ ] Which declared material determines Definition meaning?

## HIR-D05 — Context Determinants

- [ ] Which Interface, Interaction, Operation, Machine, resolved Version candidate, or other context actually determines
  Definition meaning?

## HIR-D06 — Non-Determinant Context Exclusion

- [ ] Does context that does not determine Definition meaning remain in Binding, Basis, Applicability, attribution,
  provenance, or another separately owned category?

## HIR-D07 — Candidate Reference

- [ ] What is the exact component set of `Definition Candidate Reference`?
- [ ] Is a universal reference tuple avoided?

## HIR-D08 — Authority-Local Candidate Coordinate

- [ ] Is an additional coordinate required to distinguish independent Candidates under the same authority?

## HIR-D09 — Direct Semantic References

- [ ] Are all exact direct references required by Candidate meaning preserved?
- [ ] Is arbitrary reference chasing prohibited as a way to recover meaning omitted by the frontend?

## HIR-D10 — Presence

- [ ] Is presence itself a semantic distinction?

## HIR-D11 — Absence Taxonomy

Distinguish at least the following where applicable.

```text
not owned
explicit permitted absence
required but unresolved
not materialized
not retained
unavailable
unknown / unsupported
corrupt
```

## HIR-D12 — Cardinality

- [ ] If cardinality is semantic, is the exact law declared?

## HIR-D13 — Ordering

- [ ] Is ordering semantic?
- [ ] If not, are declaration, storage, hash, and traversal order prevented from becoming meaning?

## HIR-D14 — Multiplicity

- [ ] If multiplicity is semantic, are set, bag, sequence, tuple, and related distinctions closed explicitly?

## HIR-D15 — Alternatives / Variant Domain

- [ ] Is the variant domain closed when the owning law requires a closed set?
- [ ] Is unknown-variant behavior defined?

## HIR-D16 — Candidate Semantic Equality

- [ ] Is Candidate semantic equality producer-owned and explicitly defined?

## HIR-D17 — Reference Equality vs Semantic Equality

- [ ] Are Candidate Reference equality and Candidate meaning equality distinct?

## HIR-D18 — Equal Payload Does Not Imply Merge

- [ ] Is equal payload insufficient to merge independently identified Candidates?

## HIR-D19 — Duplicate / Collision Law

- [ ] Are duplicate declarations legal?
- [ ] Are duplicate coordinates legal?
- [ ] What happens when the same coordinate carries conflicting meaning?

## HIR-D20 — Coverage Law

- [ ] Is complete coverage required?
- [ ] Is partial coverage itself meaningful?
- [ ] Does adding a new coordinate avoid silently widening an existing Definition unless the owning law explicitly says
  otherwise?

## HIR-D21 — Merge Law

- [ ] Does semantic merge exist?
- [ ] If yes, is there an exact owning law?
- [ ] Are compiler deduplication and interning prevented from becoming semantic merge?

## HIR-D22 — Authoring Refinement

- [ ] Which alias spellings, import spellings, syntax sugar, and source-only nesting may be erased after resolution?

## HIR-D23 — Semantic Convergence

- [ ] Under what exact conditions may different authored forms converge to the same HIR meaning?

## HIR-D24 — History Separation

- [ ] Is current Candidate identity independent of previous-generation Candidate identity?

## HIR-D25 — Ambient Nondeterminism Exclusion

- [ ] Are time, randomness, worker identity/order, cache state, and process environment excluded as hidden determinants?

## HIR-D26 — Information-Loss Burden

- [ ] For every authored distinction erased before Visible HIR, can the producer show that no owning 1D law and no later
  legal HIR observer requires that distinction to recover candidate meaning?
- [ ] If a later semantic consumer would have to reopen source to recover the distinction, is erasure prohibited?

---

# 8. HIR Binding Candidate Checklist

## HIR-B01 — Binding Candidate Existence

- [ ] Does this 1D authoring/selection model require a distinct Binding Candidate?

## HIR-B02 — Selecting Semantic Subject / Context

- [ ] What exact semantic subject or context owns the selection occurrence?

## HIR-B03 — Role Coordinate

- [ ] What exact slot, role, or selection coordinate identifies the binding occurrence?

## HIR-B04 — Target Candidate

- [ ] What exact Definition Candidate Reference is selected?

## HIR-B05 — Binding-Owned Qualifier

- [ ] Does the selection relation independently own any qualifier?

## HIR-B06 — Context Minimization

- [ ] Is the full reachable surrounding IDL context excluded unless it independently belongs to Binding meaning?

## HIR-B07 — Definition vs Binding Determinant

- [ ] If one coordinate appears in both Definition and Binding, does it independently determine both meanings?

## HIR-B08 — Reusable Definition

- [ ] May several Binding Candidates select one Definition Candidate?
- [ ] Are the conditions for that reuse explicit?

## HIR-B09 — Binding Collision

- [ ] May one selection coordinate resolve to several targets?
- [ ] Is the allow/reject/arbitrate rule explicit?

## HIR-B10 — Authority Separation

Do not conflate:

```text
IDL Binding Candidate
Version Binding
Basis Binding
Governance Binding
Established runtime binding
```

---

# 9. HIR Coordinate / Shape / Aggregate Checklist

## SHAPE-01 — Nominal vs Positional vs Structural Coordinate

- [ ] Is each coordinate kind explicit?

## SHAPE-02 — Coordinate Identity

- [ ] Which of name, ordinal, path, or another coordinate is semantic?

## SHAPE-03 — Nested Structure

- [ ] Is nested shape allowed?
- [ ] If yes, is the exact nested-coordinate law defined?

## SHAPE-04 — Recursive Structure

- [ ] Is semantic recursion forbidden or explicitly bounded by the owning law?
- [ ] Is deep recursion prevented from becoming an accidental compiler traversal property?

## SHAPE-05 — Collection Family

- [ ] Are sequence, set, bag, map, fixed tuple, and other relevant collection meanings distinguished?

## SHAPE-06 — Element Authority

- [ ] Is the container prevented from being accepted while elements remain hidden unjudged material?

## SHAPE-07 — Key / Value Relation

- [ ] For map-like shapes, are key identity, equality, uniqueness, and ordering laws explicit?

## SHAPE-08 — Cardinality Bounds

- [ ] Are semantic min/max/exact cardinality bounds explicit when they exist?
- [ ] Are semantic cardinality bounds distinct from compiler resource limits?

## SHAPE-09 — Complete Snapshot

- [ ] Is an aggregate judged over one coherent semantic snapshot?

## SHAPE-10 — Alias / Sharing

- [ ] Is source aliasing excluded from Contract identity?
- [ ] If intentional shared semantic reference is required, is that relation explicit?

---

# 10. Version Boundary Checklist

## VER-01 — Version Sensitivity

- [ ] Is this 1D Definition Contract-Version-sensitive?

## VER-02 — Version Claim

- [ ] Is there an authored Version Claim?

## VER-03 — Resolved Version Candidate Coordinate

- [ ] Does HIR resolve the claim to the exact authority-scoped Version candidate coordinate required by Establishment?

## VER-04 — Definition Determinant

- [ ] Is the resolved Version candidate coordinate actually a determinant of this 1D Definition meaning?

## VER-05 — Version Binding Boundary

- [ ] Is the HIR Version coordinate distinct from authoritative `Version Binding`?

## VER-06 — No Implicit Selection

- [ ] Are `current`, `latest`, `preferred`, `nearest`, and `first available` prohibited as implicit Version authority?

## VER-07 — Stability Domains

Keep these distinct:

```text
Contract Version
authority-specific HIR surface evolution
common HIR Protocol evolution
persistent artifact format
compiler generation
backend format
```

## VER-08 — Version History vs Current Identity

- [ ] Is current identity independent of historical predecessor identity?

---

# 11. HIR Basis / Applicability Declaration Checklist

## HIR-BA01 — Basis Requirement Law

- [ ] Does Definition meaning contain a Basis Requirement Law?

## HIR-BA02 — Required Semantic Kind

- [ ] What semantic kind or meaning does the law require?

## HIR-BA03 — Requirement Coordinate Shape

- [ ] What exact requirement coordinates exist?

## HIR-BA04 — Requirement Cardinality

- [ ] Is there an exact cardinality or completeness law?

## HIR-BA05 — Permitted Absence

- [ ] Is absence legal for this requirement?

## HIR-BA06 — Definition-Time vs Occurrence-Time

- [ ] At what semantic boundary is the exact Required Basis instance formed?

## HIR-BA07 — Applicability Law

- [ ] Is a future Applicability Law part of Definition meaning?

## HIR-BA08 — Declared Applicability Coordinate Kinds

- [ ] Which semantic coordinate kinds may participate in the future law?

## HIR-BA09 — No Premature Result

HIR must not contain these as future authority-owned results:

```text
actual Required Basis instance owned later
Basis Binding
Applicable / Inapplicable result
Applicable Context instance
Complete Basis
Established Occurrence result
```

---

# 12. Resolved HIR Candidate Protocol Checklist

Common families:

```text
Typed HIR Semantic Reference Domain
Definition Candidate Projection
IDL Binding Candidate Projection
Fine-Grained Semantic Projection
```

## HIR-P01 — Typed Semantic Reference Domain

- [ ] Are all exact HIR reference kinds used by this 1D enumerated?

## HIR-P02 — Definition Candidate Projection

- [ ] Does the projection expose the exact Candidate Reference?
- [ ] Does it expose complete producer-owned Candidate meaning?

## HIR-P03 — Binding Candidate Projection

- [ ] Does the projection expose complete exact Binding Candidate meaning?

## HIR-P04 — Fine-Grained Projection Catalog

- [ ] Are all necessary producer-defined narrow semantic observations defined?

## HIR-P05 — Projection Completeness

- [ ] For what exact observation is each projection complete?

## HIR-P06 — Projection Equality

- [ ] What producer-owned semantic equality law governs each projection?

## HIR-P07 — Omission Is Not Absence

- [ ] Is unexposed material prevented from being interpreted as semantic absence?

## HIR-P08 — Direct Exactness

- [ ] Can exact targets and references be identified from the legal projection without implementation topology?

## HIR-P09 — No Source Reopening

- [ ] Is reopening source or host topology prohibited as a way to recover missing Candidate meaning?

## HIR-P10 — No Arbitrary Graph Traversal

- [ ] Are `parent`, `children`, `reachable`, and similar traversal topology prevented from defining Candidate meaning?

## HIR-P11 — Provenance Separation

- [ ] Is provenance exposed as a separate relation where needed?

## HIR-P12 — Physical Coordinate Exclusion

None of the following is a semantic reference by itself:

```text
dense handle
HID
fingerprint
table row
page / segment
memory address
storage key
query key
```

## HIR-P13 — No Future Authority

- [ ] Does Protocol observation avoid pre-establishing Version Binding, Basis Binding, Established Definition, or other
  later authority-owned material?

## HIR-P14 — Protocol Crossing Does Not Imply Copy

- [ ] Is the logical boundary independent of wrapper allocation or a full physical copy?

## HIR-P15 — Shared Immutable Backing Is Allowed

- [ ] May HIR and later compiler products share immutable physical backing while preserving distinct semantic kinds?

## HIR-P16 — Visible Representation Preservation Beyond Payload Equality

- [ ] Does a physical HIR representation preserve not only semantic accessor values but also the required generation
  coherence, reference lifetime, provenance relation correctness, stale-reference rejection, and applicable persistence
  compatibility obligations?

---

# 13. HIR Seal / Recovery / Visibility Checklist

## HIR-S01 — Resolution Invariant

- [ ] Is every required semantic subject resolved?

## HIR-S02 — Exact References

- [ ] Is every reference required to interpret the Candidate exact?

## HIR-S03 — 1D-Specific Seal Condition

- [ ] What completeness and integrity conditions must this 1D Candidate satisfy before it becomes Visible HIR?

## HIR-S04 — Seal Is Not Contract Validity

- [ ] Is HIR seal verification prevented from replacing the owning Contract judgment?

## HIR-S05 — Recovery Exclusion

- [ ] Are recovery, poison, and unresolved semantic placeholders excluded from valid Visible HIR?

## HIR-S06 — Known Required Extension Closure

- [ ] Is a consumer that does not understand required semantic material prohibited from claiming a complete sealed view?

## HIR-S07 — Private Construction

- [ ] Is incomplete transient construction state separated from Visible HIR?

## HIR-S08 — Read-Only Visible Meaning

- [ ] Is visible HIR semantic meaning immutable in place?

## HIR-S09 — Partial Visibility

- [ ] Are partially formed Candidates and Bindings hidden from ordinary legal consumers?

## HIR-S10 — Failure Isolation

- [ ] Can an independent Candidate outside the determinant closure of an invalid Candidate still be formed and sealed?

## HIR-S11 — Entered Seal Rejection

- [ ] When the seal judgment is legally entered and rejects, is the result compiler-owned rather than Contract Failure?

## HIR-S12 — Unentered Later Judgment

- [ ] If an earlier unsuccessful compiler result prevents entry, is a synthetic later rejection prohibited?

---

# 14. HIR Lifecycle / Snapshot / Lifetime Checklist

## HIR-L01 — Construction / Visibility Separation

- [ ] Are construction state and Visible HIR state distinct?

## HIR-L02 — Supersession

- [ ] Does a new generation supersede old visible meaning rather than mutate it in place?

## HIR-L03 — Reclamation

- [ ] Are semantic supersession and physical reclamation separate events?

## HIR-L04 — Fine-Grained Lifetime

- [ ] Can stable semantic products/projections have a lifetime smaller than the whole frontend generation?

## HIR-L05 — Reference Lifetime

- [ ] Is the validity domain of each exact HIR reference defined?

## HIR-L06 — Dense Handle Lifetime

- [ ] Is generation-local handle lifetime separate from semantic reference validity?

## HIR-L07 — No Stale Reinterpretation

- [ ] Can stale references/handles never be silently reinterpreted as another generation's material?

## HIR-L08 — Snapshot Coherence

- [ ] Does one semantic computation observe one coherent visible generation or validated projection?

## HIR-L09 — Cross-Generation Reuse

- [ ] Is reuse allowed only after current-generation semantic equivalence and validity are established?

## HIR-L10 — Provenance Coherence

- [ ] When semantic material and provenance are observed together, are they valid for the same logical observation?

## HIR-L11 — Retention After Establishment

- [ ] Does forming the Canonical Contract World avoid automatically forcing immediate HIR reclamation or permanent HIR
  retention?

## HIR-L12 — Post-Establishment Authority Source

- [ ] Do authoritative downstream consumers use Established semantics rather than treating HIR as a substitute authority
  source?

## HIR-L13 — Failed New Generation Does Not Replace the Current Valid Generation

- [ ] If formation, verification, migration, or repair of a new HIR generation fails, does the previously valid visible
  generation remain semantically intact until an independently valid replacement becomes visible?

---

# 15. HIR Protocol Evolution / Compatibility Checklist

## HIR-E01 — Separate Evolution Domains

- [ ] Are common Protocol evolution and authority-specific HIR surface evolution distinct?

## HIR-E02 — Required Semantic Extension

- [ ] Are required, optional, and explicitly non-semantic extensions distinguishable?

## HIR-E03 — Unknown Required Extension

- [ ] Does the system fail closed when required semantic material is unknown?

## HIR-E04 — Unsupported Known Extension

- [ ] Is silent downgrade forbidden when material is understood but unsupported?

## HIR-E05 — Compatible Projection

- [ ] Is a smaller view considered compatible only when it is a producer-defined complete projection for the requested
  observation?

## HIR-E06 — Migration

- [ ] Does migration preserve legal semantic observations losslessly?

## HIR-E07 — Fabricated Default

- [ ] Are unknown semantic distinctions prevented from receiving fabricated default meaning?

## HIR-E08 — Opaque Retention

- [ ] Is skipping uninterpretable material distinct from retaining it opaquely without exposing it as understood
  semantics?

## HIR-E09 — Stable Extension Coordinate

- [ ] Is an existing extension coordinate prevented from being reused for incompatible meaning?

## HIR-E10 — Equality Participation

- [ ] Does the owning authority decide whether an extension participates in semantic equality?

## HIR-E11 — External ABI Separation

- [ ] Is the internal HIR Protocol prevented from becoming an external stable ABI by default?

---

# 16. HIR-to-Establishment Handoff Integrity Checklist

This section is additive in the English re-audit. It makes explicit a boundary that was previously distributed across
HIR and Establishment checks.

## XFER-01 — Legal Input Classes

- [ ] Can every semantic input to Establishment be classified as a legal HIR Protocol observation, a separately
  Established prerequisite, or an explicitly formed authority-owned judgment input?

## XFER-02 — No Hidden Reconstruction

- [ ] Is Establishment prohibited from reopening authored source, host structure, query topology, backing containment,
  or implementation state to recover missing Candidate meaning?

## XFER-03 — No Premature Authority in HIR

- [ ] Is HIR prohibited from precomputing later authority-owned results merely to make Establishment mechanically
  simpler?

## XFER-04 — Determinant Traceability

- [ ] For every Established result determinant, can its semantic source be traced to a legal HIR observation or exact
  authoritative prerequisite?

## XFER-05 — Binding Use Discipline

- [ ] If a Binding Candidate participates in Establishment, is it used only for the selection relation that the owning
  law actually requires?

## XFER-06 — Handoff Completeness

- [ ] Can the owning Establishment judgment execute without a private semantic frontend or undocumented fallback
  resolution path?

## XFER-07 — No Semantic Amplification

- [ ] Does Establishment create only meaning owned by the establishing authority, with no implicit reachability,
  optimization, diagnostic, realization, transitive, or consumer-specific conclusions?

## XFER-08 — Handoff Conformance Path

- [ ] Is there a verification seam that can compare the legal HIR observation actually consumed by Establishment with
  the declared Establishment input contract?

---

# 17. Establishment Judgment Inventory Checklist

Do not assume that one 1D has exactly one judgment.

## EST-J01 — Judgment Catalog

Enumerate every applicable judgment.

```text
Definition-time judgment
Occurrence-time judgment
runtime Contract judgment
composition judgment
higher-scope judgment
State-Machine judgment relation
no judgment / pure declaration authority
```

## EST-J02 — Judgment Owner

- [ ] Who is the exact owner of each judgment?

## EST-J03 — Exact Subject

- [ ] What exact semantic subject does each judgment evaluate?

## EST-J04 — Legal Entry Condition

- [ ] What complete prerequisites must exist before the judgment is legally entered?

## EST-J05 — HIR Inputs

- [ ] Which Candidate or Fine-Grained HIR projections does the judgment consume?

## EST-J06 — Binding Candidate Input

- [ ] Does the judgment actually require the IDL Binding Candidate?

## EST-J07 — Established Prerequisites

- [ ] Which separately Established materials are required?

## EST-J08 — External Semantic Inputs

- [ ] If time, State, Policy, world, or another runtime semantic input participates, is it represented by exact
  Established material or an explicitly owned semantic input rather than ambient state?

## EST-J09 — Result Vocabulary

- [ ] Are all exact results that an entered judgment may produce enumerated?

## EST-J10 — Result Owner

- [ ] Which authority owns each result?

## EST-J11 — Judgment Is Not Necessarily Establishment

- [ ] Where judgment success and target authority grant are distinct, is that distinction explicit?

## EST-J12 — Multiple-Judgment Chain

- [ ] If final authority requires several independent judgments, as with Fact-related flows, are ownership and
  prerequisite relations separated?

## EST-J13 — Complete Established Unit

- [ ] What is the minimum complete semantic unit established by this judgment?

## EST-J14 — Partial Result Visibility

- [ ] Unless the owning law defines a partial result as complete meaning of its own, is partial authoritative visibility
  forbidden?

---

# 18. Compiler Unsuccessful / Contract Negative / Failure Boundary Checklist

> Because the current ADR-0074 dependency remains Proposed in the available project material, mark relevant items
> `PROVISIONAL-COMMON` or `BLOCKED-BY-ADR-0074` where appropriate.

## RES-01 — Never Entered

- [ ] Is a judgment that never legally entered represented distinctly from an entered negative result?

## RES-02 — Compiler-Owned Unsuccessful Result

- [ ] Does the exact first compiler judgment that owns the failed compiler requirement own the unsuccessful result?

## RES-03 — Contract-Owned Negative Result

- [ ] Does the owning Contract law define the exact vocabulary for reject, refuse, inapplicable, stop, or another
  negative result where such meaning exists?

## RES-04 — Failure Contract Boundary

- [ ] Is a negative-looking result prevented from automatically becoming ADR-0057 Failure?

## RES-05 — Indeterminate / Trust Loss

- [ ] If crash, corruption, or trust loss prevents the compiler from determining the exact result, is success/failure
  fabrication prohibited?

## RES-06 — Later Compiler Failure

- [ ] Can later compiler machinery failure neither rewrite nor revoke already Established Contract meaning?

## RES-07 — Recovery

- [ ] Does recovery preserve rather than rewrite the original unsuccessful compiler result?

## RES-08 — Diagnostic

- [ ] Is Diagnostic a consumer/explainer rather than the owner that creates result meaning?

## RES-09 — Aggregation

- [ ] Are aggregation and availability of several independent compiler results prevented from being collapsed into 1D
  Contract Failure?

## RES-10 — Protocol Violation

- [ ] Is an invalid HIR handoff or Protocol violation distinct from a Contract Establishment rejection?

---

# 19. Established Material Family Classification Checklist

Every 1D must answer every category with `DEFINED`, `NOT-APPLICABLE`, or another explicit status.

## MAT-01 — Established Definition Material

- [ ] Does this authority produce Established Definition Material?

## MAT-02 — Established Occurrence Material

- [ ] Does this authority produce Established Occurrence Material?

## MAT-03 — Other Authority-Specific Established Material

- [ ] Is there complete source-owned Established meaning that is neither Definition nor Occurrence material?

## MAT-04 — Relation-Only Distinction

- [ ] Which semantic distinctions are coordinates or relations on existing Established meaning rather than separate
  Established Material?

## MAT-05 — Material Split Criterion

- [ ] Is separate Established Material created only when an owning law establishes independently complete meaning?

## MAT-06 — No Universal Schema

- [ ] Is a universal semantic schema such as `EstablishedMaterial { type, id, payload }` avoided as a normative
  requirement?

---

# 20. Established Definition Identity Checklist

Apply when the 1D has Definition meaning.

## DEF-01 — Complete Source-Owned Meaning

- [ ] What exact Definition Meaning is established?

## DEF-02 — Source Authority

- [ ] Does source authority remain with the original authority even when downstream consumers use the material?

## DEF-03 — Owning Authority Binding

- [ ] Is exactly one exact owning authority directly recoverable by a semantic consumer?

## DEF-04 — Authority-Owned Definition Identity

- [ ] Are Definition identity determinants defined by the owning law?

## DEF-05 — Version Binding

- [ ] If the authority is version-sensitive, is the exact Version Binding preserved?

## DEF-06 — Authority-Local Definition Coordinate

- [ ] Does an authority-local Definition coordinate exist only where required by the owning identity law?

## DEF-07 — Definition Reference

- [ ] Does the Definition Reference identify one exact authoritative Definition?

## DEF-08 — CandidateRef Is Not DefinitionRef

- [ ] Is a universal cast, upgrade, or identity-preserving conversion from CandidateRef to DefinitionRef avoided?

## DEF-09 — Candidate-to-Definition Cardinality

- [ ] Is a universal one-to-one mapping between Candidate References and authoritative Definitions avoided?

## DEF-10 — Identity Without HIR

- [ ] Can Definition Reference be interpreted without retaining the original HIR generation?

## DEF-11 — Conflicting Meaning

- [ ] What exact law rejects or resolves conflicting Definition Meaning at the same authoritative identity coordinate?

## DEF-12 — Direct Established Relations

- [ ] What exact direct Established relations are owned by the Definition itself?

## DEF-13 — Provenance Separation

- [ ] Is source provenance separate from Definition identity and Definition Meaning?

## DEF-14 — Semantic Immutability

- [ ] After Establishment, can the same Established Material never change its semantic meaning?

## DEF-15 — Linking Preservation

- [ ] Can physical relocation, aggregation, linking, and artifact regeneration occur without changing Definition
  identity?

## DEF-16 — Supersession / History

- [ ] Are history and supersession relations prevented from granting current identity by inheritance?

---

# 21. Current Establishment / History / Retention Checklist

## HIST-01 — Current Establishment vs Durable History

- [ ] Is Establishment in the current compiler run distinct from durable project-history publication or retention?

## HIST-02 — Intermediate Success

- [ ] Is intermediate compiler success insufficient to create a durable Contract history revision?

## HIST-03 — Historical Material

- [ ] Does retained historical Established Material avoid automatically acquiring current applicability?

## HIST-04 — Replacement / Succession

- [ ] If replacement or succession is Contract meaning, is there an explicit owning relation?

## HIST-05 — Retention Is Not Authority

- [ ] Are persisted and retained states prevented from creating Contract authority?

## HIST-06 — Reclamation Is Not Semantic Deletion

- [ ] Is physical reclamation distinct from the semantic fact that historical meaning once existed?

---

# 22. Observation / Retention / Outward-Authority Integrity Checklist

These checks make explicit the ADR-0063 integrity boundaries that are easy to blur in implementation.

## OBS-01 — Observation Is Not Establishment

- [ ] Does observing compiler/runtime state never establish Contract meaning by itself?

## OBS-02 — Realization State Must Be Semanticized Before Contract Use

- [ ] If realization state matters to a Contract judgment, is the required semantic meaning first established by an
  owning authority rather than consumed as raw implementation state?

## OBS-03 — No Universal Observation Contract

- [ ] Is a generic observation layer prevented from becoming a new umbrella Contract authority over all observed state?

## OBS-04 — Retention Does Not Decide Whether Establishment Occurred

- [ ] Can material be reclaimed or cease to be retained without retroactively undoing earlier authority?

## OBS-05 — Internal Authority Is Not Outward Authority

- [ ] Is internal Establishment insufficient to authorize external exposure, leaving outward authorization to
  Publication and final outward shape to Output Presentation?

---

# 23. Required Basis / Basis Binding Checklist

## BASIS-01 — Requirement Owner

- [ ] Who owns the exact Required Basis instance?

## BASIS-02 — Requirement Coordinate

- [ ] How are several requirements under the same judgment distinguished?

## BASIS-03 — Required Meaning

- [ ] What exact semantic kind or meaning is required?

## BASIS-04 — Cardinality

- [ ] What exact cardinality or completeness requirement applies?

## BASIS-05 — Absence

Distinguish the following semantic states where applicable.

```text
Not Owned
Permitted Absence
Required, Unresolved
Bound, Inapplicable
Bound, Applicable
Complete
```

## BASIS-06 — Producer Independence

- [ ] Does the requirement describe required meaning rather than naming a compiler producer, query, implementation
  function, or physical location?

## BASIS-07 — Basis Resolution Owner

- [ ] Which exact semantic law owns Basis Resolution?

## BASIS-08 — Exact Basis Binding

- [ ] Does Basis Binding relate one exact Required Basis to one exact already-Established source material?

## BASIS-09 — Binding Is Not Applicability

- [ ] Is a bound source prevented from being assumed applicable or satisfying merely because the Binding exists?

## BASIS-10 — No Hidden Lookup

None of the following may create Basis Binding authority:

```text
first match
nearest
lowest HID
declaration order
discovery order
storage order
query traversal order
```

## BASIS-11 — Shared Source

- [ ] If one source material satisfies several requirements, is each exact semantic relation preserved independently?

## BASIS-12 — Identity Significance

- [ ] Does the owning law decide whether an exact resolved source relation participates in the consuming Definition's
  meaning or identity?

## BASIS-13 — Semantic Prerequisite Order

- [ ] Is the semantic prerequisite partial order explicit and separate from compiler pass/scheduling order?

## BASIS-14 — Circular Establishment

- [ ] Is an unresolved semantic prerequisite cycle rejected?
- [ ] Is fixed-point authority formation prohibited?

---

# 24. Applicability Checklist

## APP-01 — Owning Applicability Law

- [ ] Can the exact owning Applicability Law be identified?

## APP-02 — Exact Basis Binding

- [ ] Does Applicability consume an already-established exact Basis Binding rather than selecting another source
  independently?

## APP-03 — Dependent Application

- [ ] What exact semantic use identifies the dependent application?

## APP-04 — Applicable Context

- [ ] Does Applicable Context contain only meaning-determining Contract coordinates?

## APP-05 — Sparse Context

- [ ] Are unrelated global coordinates excluded?

## APP-06 — Complete Context

- [ ] Are all coordinates required by the owning law present?

## APP-07 — None vs Unavailable

- [ ] Is `no additional context required` distinct from `required context unavailable`?

## APP-08 — No Ambient Current

The judgment must not read any of the following as implicit semantic input:

```text
currentVersion
currentPolicy
currentState
currentGovernance
currentEverything
```

## APP-09 — Compiler Context Exclusion

Do not include the following in Applicable Context:

```text
provenance
compiler generation
cache state
storage identity
worker state
query state
```

## APP-10 — Result Vocabulary

- [ ] What exact meanings do `Applicable` and `Inapplicable` carry under this law?

## APP-11 — Use-Specific

- [ ] Is Applicability prevented from becoming a permanent property of the source material?

## APP-12 — Non-Entry

- [ ] If required context is unavailable, is `Inapplicable` fabrication prohibited because the judgment has not legally
  entered?

## APP-13 — Multiple Applicable Bindings

- [ ] May several Basis Bindings be Applicable to the same dependent application?

## APP-14 — Singularity Owner

- [ ] If exactly one applicable source is required, which law owns that singularity requirement?

## APP-15 — Arbitration Owner

- [ ] If arbitration is required, is there an explicit owning arbitration law rather than hidden first/nearest/order
  selection?

---

# 25. Completeness / Coverage / Singularity / Composition Checklist

## COMP-01 — Complete Basis Definition

- [ ] What exactly constitutes completeness for this application or judgment?

## COMP-02 — Completeness Owner

- [ ] Which exact law owns completeness?

## COMP-03 — Required Coverage

- [ ] Are all required semantic relations covered?

## COMP-04 — Permitted Absence Handling

- [ ] Is permitted absence distinct from unresolved required material?

## COMP-05 — Singularity

- [ ] If at-most-one, exactly-one, or many is semantic, is that law explicit?

## COMP-06 — Incomplete Prerequisite

- [ ] Is incomplete prerequisite state prevented from being converted into downstream Contract Failure?

## COMP-07 — Non-Entry

- [ ] Does the dependent judgment remain unentered while its required basis is incomplete?

## COMP-08 — Composition Authority

- [ ] If several source meanings are related to establish new meaning, is there an exact authority that owns that
  composition?

## COMP-09 — Whole-Machine Relation

- [ ] Is cross-Core or higher-scope meaning prevented from arising without a Whole-Machine owning law?

## COMP-10 — Physical Linking

- [ ] Are linking, co-location, import, and artifact aggregation prevented from establishing composition authority?

## COMP-11 — No Implicit Merge

- [ ] Are two sources prevented from merging automatically because they share a payload, hash, key, or physical
  representation?

## COMP-12 — Closure Claim

- [ ] If a law claims `all`, `complete`, `exactly one`, or `no other member exists`, is the closure determinant
  explicit?

## COMP-13 — Negative-Space Closure

- [ ] When semantic meaning depends on the absence of another legal member, is the domain over which absence is asserted
  explicitly closed?

## COMP-14 — Coverage Change Sensitivity

- [ ] If the legal membership domain changes, does the owning law define whether an existing complete result remains
  valid, becomes incomplete, or requires re-establishment?

## COMP-15 — No Double Ownership of Completeness

- [ ] Is the same completeness/singularity decision prevented from being independently re-decided by HIR seal,
  Establishment, Governance, and a downstream compiler consumer?

---

# 26. Established Occurrence Checklist

Apply only to authorities whose owning law gives one semantic application independent meaning.

## OCC-01 — Existence

- [ ] Does one application result actually have independent semantic meaning?

## OCC-02 — Occurrence Subject

- [ ] What exactly is one semantic application?

## OCC-03 — Occurrence Determinants

- [ ] Which exact coordinates distinguish occurrences?

## OCC-04 — Occurrence Reference

- [ ] Does the Occurrence Reference identify one exact semantic application?

## OCC-05 — Definition Reference

- [ ] Does the occurrence identify the exact applied Definition?

## OCC-06 — Determining Semantic Basis

- [ ] Is exact occurrence attribution preserved?

## OCC-07 — Applicable Basis Attribution

- [ ] Are the exact Required Basis and Basis Binding relations retained when they determine occurrence meaning?

## OCC-08 — Applicable Context Attribution

- [ ] Are only the exact meaning-determining Established context coordinates retained?

## OCC-09 — Applicability Law Attribution

- [ ] If ambiguity would otherwise exist, can the exact Applicability Law be recovered?

## OCC-10 — Established Result

- [ ] What exact result meaning is owned by the occurrence?

## OCC-11 — Equal Result Does Not Imply Same Occurrence

- [ ] Are distinct occurrences preserved when the owning law distinguishes them even if result values are equal?

## OCC-12 — Runtime / Query Distinction

- [ ] Is runtime call identity or compiler query identity prevented from automatically defining occurrence identity?

## OCC-13 — No Transitive World Copy

- [ ] Is the occurrence prevented from copying the entire transitive semantic world merely to preserve attribution?

## OCC-14 — Historical Attribution

- [ ] Can later Version, Policy, State, Governance, or other current material never rewrite old occurrence attribution?

---

# 27. Run / World Binding Checklist

This section is not necessarily semantically applicable to every 1D, but every 1D must answer each item with an explicit
status.

## RUN-01 — Run-Specific Meaning

- [ ] Is any meaning owned by this authority attached to one exact run?

## RUN-02 — World Binding

- [ ] Are selected Policy World, Governance world, Version, State, or related coordinates exact Established inputs where
  required?

## RUN-03 — Pinning

- [ ] Which semantic bindings must remain fixed for the duration of a run or occurrence?

## RUN-04 — Mid-Run Replacement

- [ ] If context changes, does the same run/occurrence change meaning, or must a new run/occurrence be formed?

## RUN-05 — Ambient Global Prohibition

- [ ] Is semantic judgment independent of singleton, thread-local, service-locator, or global `current` lookup?

## RUN-06 — Cross-Run Identity

- [ ] Are run-local occurrence identity and reusable Definition identity kept distinct?

## RUN-07 — Retry / Restart

- [ ] Does the owning law decide whether retry/restart continues the same semantic occurrence or creates a new one?

---

# 28. Canonical Contract World / Backing Placement Checklist

## WORLD-01 — Definition Placement

- [ ] Is Established Definition Material observable through the Canonical Contract World or its legal
  established-semantic access surface?

## WORLD-02 — Occurrence Placement

- [ ] Is occurrence material prevented from being placed universally into the Definition world merely for consumer
  convenience?

## WORLD-03 — Other Established Backing

- [ ] Is the backing/ownership boundary for other authority-specific Established Material explicit?

## WORLD-04 — World Is Not Authority Creation

- [ ] Is World visibility prevented from creating Establishment or transferring authority?

## WORLD-05 — Exact Binding Surface

- [ ] Can Definition Reference, Definition Meaning, and owned direct Established relations be recovered exactly?

## WORLD-06 — Relation States

Distinguish:

```text
relation not owned
owned and validly absent
owned and present
required but unresolved
```

## WORLD-07 — Unresolved Exclusion

- [ ] Is a required unresolved relation prevented from appearing as a successful authoritative World state?

## WORLD-08 — Future Occurrence Absence

- [ ] Is the fact that a future occurrence does not yet exist distinct from Definition incompleteness?

## WORLD-09 — Physical Membership

- [ ] Is co-location in the same table, allocation, segment, or World backing prevented from creating semantic relation?

## WORLD-10 — Coherent View

- [ ] Can a consumer never combine stale and current backing as though it were one coherent current observation without
  validation?

## WORLD-11 — Partial World Coverage

- [ ] Is a World that exposes only some independently complete Definitions distinct from a partial Definition?

## WORLD-12 — Consumer Closure

- [ ] If a consumer requires a larger semantic closure that is incomplete, is that consumer prevented from entering its
  dependent judgment?

## WORLD-13 — Provenance Relation

- [ ] Is provenance an adjacent relation rather than Definition identity?

## WORLD-14 — Excluded Compiler Products

Do not absorb the following into World authority meaning:

```text
diagnostics
verification results
analysis
optimization knowledge
query state
cache state
backend state
runtime profile
```

---

# 29. Established Semantic Protocol Checklist

Common families:

```text
Typed Established Semantic Reference Domains
Authority-Owned Established Projection Families
Direct Established Relation Projection
Fine-Grained Established Semantic Projection
```

## ESP-01 — Typed Established References

- [ ] Is the Definition Reference domain defined?
- [ ] Is the Occurrence Reference domain defined where occurrences exist?
- [ ] Is an authority-specific exact reference domain required?

## ESP-02 — Established Definition Projection

- [ ] Does the projection expose an exact Definition Reference and complete local Definition meaning?

## ESP-03 — Established Occurrence Projection

- [ ] Where occurrence meaning exists, does the projection expose complete occurrence-owned meaning?

## ESP-04 — Authority-Specific Projection

- [ ] Does any Established result require a projection that cannot be reduced to Definition or Occurrence shape?

## ESP-05 — Direct Established Relation Projection

- [ ] Can exact source-owned direct relations be observed without recreating them as derived conclusions?

## ESP-06 — Fine-Grained Established Projection

- [ ] Are necessary producer-defined narrow semantic observations defined?

## ESP-07 — Projection Completeness

- [ ] For what exact observation is each Established projection complete?

## ESP-08 — Projection Equality

- [ ] Is producer-owned semantic equality defined for each reusable Established projection?

## ESP-09 — No Transitive World Embedding

- [ ] Is a local projection prevented from recursively embedding the entire transitive semantic graph?

## ESP-10 — Reverse Index Authority Leak

- [ ] Is a reverse lookup/reindexing structure prevented from creating a new inverse Contract relation?

## ESP-11 — Derived Judgment Exclusion

The following are normally new derived compiler products, not Established Protocol projections:

```text
transitive reachability
cycle analysis
conflict analysis
Whole-Machine summary
proof
ranking
optimization judgment
cost model
prediction
```

## ESP-12 — Generic Navigation Ban

- [ ] Are APIs such as `getParent`, `getChildren`, `walkGraph`, and `allReachable` prevented from becoming sources of
  semantic meaning?

## ESP-13 — Protocol Observation Is Not Authority Transfer

- [ ] Does observation leave source authority with the authority that established the material?

## ESP-14 — Publication Contract Separation

- [ ] Is observing Established semantics distinct from granting outward Publication authority?

---

# 30. Protocol Consumption Checklist

## CON-01 — Producer-Defined Projection

- [ ] Does the producer own projection semantics?

## CON-02 — Consumer Selection Only

- [ ] Does the consumer merely select among legal producer-defined projections?

## CON-03 — Completeness Ownership

- [ ] Is the consumer prevented from redefining projection completeness?

## CON-04 — Equality Ownership

- [ ] Is the consumer prevented from redefining semantic equality?

## CON-05 — No Consumer-Specific Contract Meaning

- [ ] Are names such as `DiagnosticProjection`, `PBTProjection`, or `BackendProjection` prevented from creating new
  Contract meaning merely for consumer convenience?

## CON-06 — Derived Composition

- [ ] If a consumer combines several projections and establishes a new judgment, is that result classified as a derived
  compiler product or another explicitly owning authority rather than smuggled back into the source Protocol?

## CON-07 — Product Dependency Is Not Authority Chain

- [ ] Is query/product dependency prevented from transferring or changing Contract ownership?

## CON-08 — Physical Read-Count Independence

- [ ] Is there no assumption that one semantic projection equals one method, object, query, cache entry, or physical
  read?

---

# 31. Protocol Availability / Coherence Checklist

## AVAIL-01 — Coherent Logical Observation

- [ ] Is each legal observation one coherent logical view?

## AVAIL-02 — Generation Mix

- [ ] Are backings from different generations prevented from being combined without current-validity proof?

## AVAIL-03 — Availability Taxonomy

Distinguish:

```text
semantic absence
not retained
not materialized
backing unavailable
unsupported Protocol
corrupt storage
stale relation
unknown required extension
```

## AVAIL-04 — Observation Failure Is Not Semantic Revocation

- [ ] Can a backing/access failure never revoke already Established meaning?

## AVAIL-05 — Downstream Non-Entry

- [ ] If a required legal observation is unavailable, does the dependent downstream judgment remain unentered rather
  than fabricate Contract meaning?

## AVAIL-06 — Cache Miss

- [ ] Are cache miss and reuse rejection prevented from becoming Contract Failure?

---

# 32. Reuse / Persistence / Incremental Validity Checklist

## REUSE-01 — Semantic Equality Is Not Reuse Validity

- [ ] Are semantic equality and current reuse validity distinct?

## REUSE-02 — Retained Is Not Current-Valid

- [ ] Is retained material prevented from being reused as current meaning without validity proof?

## REUSE-03 — Persisted Is Not Authoritative

- [ ] Is a persisted artifact prevented from becoming a source of authority merely because it exists?

## REUSE-04 — Current Reference Correspondence

- [ ] Are persisted or reused references remapped or revalidated against exact current semantic references?

## REUSE-05 — Determinant Validity

- [ ] Are all semantic determinants of a reused projection current-valid?

## REUSE-06 — Membership Validity

- [ ] Is current-generation membership independent of storage survival, old-page reachability, or fingerprint match?

## REUSE-07 — Closure Validity

If a reused projection claims `all`, `complete`, `exactly one`, or `no other member`:

- [ ] Are all retained members still valid?
- [ ] Is it proven that no new legal member has appeared?
- [ ] Is it proven that removed members are not still included?

## REUSE-08 — Closure Evidence Separation

The following may be compiler validity evidence but are not Contract meaning by themselves:

```text
HID
fingerprint
Merkle summary
count/index summary
dependency evidence
selective validation
compatibility proof
```

## REUSE-09 — Projection-Level Reuse

- [ ] Can reuse operate at a stable semantic projection level rather than forcing whole-World reuse?

## REUSE-10 — Product Granularity Independence

- [ ] Is one projection independent of one query, cache entry, heap object, or persistent record?

## REUSE-11 — Lazy Physical Materialization

- [ ] Is only physical materialization lazy after semantic meaning and current validity are already complete?

## REUSE-12 — No Lazy Semantic Completion

- [ ] Is an incomplete semantic projection prevented from becoming visible while its meaning or current validity is
  still unresolved?

## REUSE-13 — Clean-Path Equivalence

All of the following must be capable of exposing the same legal semantic observation:

```text
clean formation
cached reuse
persistent reload
incremental repair
parallel formation
```

## REUSE-14 — Algorithm Independence

- [ ] Are red-green, Salsa, DBSP, Merkle early cutoff, delta maintenance, selective repair, and similar algorithms
  prevented from becoming Contract law?

---

# 33. Determinism / Concurrency / Ordering Checklist

## DET-01 — Exact Determinant Set

- [ ] Is the complete semantic determinant set of the Established result explicit?

## DET-02 — Semantic Determinism

- [ ] Does the same semantic basis produce the same Established meaning?

## DET-03 — Hidden Compiler State

- [ ] Are cache state, worker identity, scheduler choice, and other compiler-only state prevented from changing result
  meaning?

## DET-04 — Physical Completion Order

- [ ] Is physical completion order distinct from semantic prerequisite order?

## DET-05 — Physical Traversal Order

- [ ] Is deterministic bulk/traversal order prevented from becoming Contract semantic order merely because it is stable?

## DET-06 — Explicit Semantic Order

- [ ] If order changes meaning, does the owning law declare that order explicitly?

## DET-07 — Parallel Formation

- [ ] Can independent work be scheduled in different orders without changing semantic meaning?

## DET-08 — Concurrent Duplicate Formation

- [ ] If the same semantic result is computed concurrently, is duplicate authority prevented?
- [ ] Is semantic identity law distinct from the physical publication/deduplication mechanism?

## DET-09 — Partial Authoritative Visibility

- [ ] Is one complete Established unit hidden until its semantic completeness condition is satisfied?

## DET-10 — Independent Sibling Visibility

- [ ] Are independent Established siblings allowed to become visible independently rather than being forced into one
  universal global transaction?

## DET-11 — Reference Path

- [ ] Can a clean/full recomputation or independently derived semantic projection path serve as a correctness reference?

---

# 34. Semantic Dependency / Cycle Checklist

## DEP-01 — Semantic Dependency

- [ ] Is a dependency semantic only when exact source meaning is actually required by the owning law?

## DEP-02 — Compiler Dependency Separation

- [ ] Are query edges, call edges, physical adjacency, and storage relations prevented from becoming semantic dependency
  automatically?

## DEP-03 — Direct Dependency Observation

- [ ] Can downstream consumers recover exact direct semantic relations without reparsing/reanalyzing the source?

## DEP-04 — Transitive Dependency

- [ ] If transitive closure is required, is it explicitly classified as an owning Contract relation or as a derived
  compiler product?

## DEP-05 — Cycle Prohibition

- [ ] Is circular semantic establishment prohibited?

## DEP-06 — Independent Compiler Cycles

- [ ] Are compiler/query recursion cycles distinct from Contract semantic cycles?

## DEP-07 — Whole-Machine Connection

- [ ] Is a cross-unit semantic connection owned by an explicit Whole-Machine composition law?

---

# 35. Negative Authority-Leak Checklist

For every 1D, confirm that none of the following becomes a semantic authority source merely because the implementation
uses it.

- [ ] source file path
- [ ] source line / column
- [ ] AST node identity
- [ ] parser node identity
- [ ] source containment
- [ ] generated host type
- [ ] generated API signature
- [ ] JVM descriptor
- [ ] JVM parameter slot
- [ ] runtime object identity
- [ ] reflection result
- [ ] proxy / interception behavior
- [ ] implementation call graph
- [ ] ordinary helper graph
- [ ] dense handle
- [ ] ordinal
- [ ] table row
- [ ] HID
- [ ] fingerprint
- [ ] hash
- [ ] Merkle node
- [ ] query key
- [ ] query graph
- [ ] Analysis Manager topology
- [ ] cache state
- [ ] persistent storage key
- [ ] worker identity
- [ ] scheduler order
- [ ] discovery order
- [ ] allocator address
- [ ] page / slab position
- [ ] backend capability
- [ ] compiler optimization knowledge
- [ ] runtime profile
- [ ] diagnostic wording
- [ ] compiler verification result
- [ ] wall clock unless explicitly semanticized by an owning law
- [ ] randomness unless explicitly semanticized by an owning law

The same physical value may encode a semantic coordinate. Even then, the semantic role must be independently recoverable
from the owning law rather than inherited from the physical representation.

---

# 36. Physical Representation Freedom Checklist

## PHY-01 — No Object-per-Concept Requirement

- [ ] Is a separate object avoided as a normative requirement for every semantic distinction?

## PHY-02 — No Table-per-Concept Requirement

- [ ] Is a separate table avoided as a normative requirement for every semantic distinction?

## PHY-03 — No Query-per-Projection Requirement

- [ ] Is one projection independent of one query?

## PHY-04 — No Persistent-Record-per-Semantic-Unit Requirement

- [ ] Is persistent layout prevented from becoming semantic topology?

## PHY-05 — Split / Fuse Freedom

- [ ] Can physical representation be split or fused while preserving every semantic distinction and exact relation?

## PHY-06 — Primitive-Friendly Realization

- [ ] May implementations use primitive arrays, typed slabs, dense generation-local handles, columnar storage, and
  similar compiler-native forms?

## PHY-07 — Co-Location

- [ ] May hot paths use co-location, pre-resolution, flattening, and contiguous ranges without acquiring authority?

## PHY-08 — Derived Denormalization

- [ ] Is a denormalized cached value prevented from becoming a second authority source?

## PHY-09 — Materialization Freedom

- [ ] Can eager, lazy, mmap-backed, persistent, segmented, compressed, or other physical forms change without changing
  Protocol semantics?

## PHY-10 — Resource Policy Separation

- [ ] Is compiler memory/performance policy prevented from stealing the meaning of Contract Budget or Capacity?

---

# 37. Terminology Collision Checklist

## TERM-01 — Publication

- [ ] Are `Publication Contract` and compiler visibility/product publication kept in distinct semantic contexts?

## TERM-02 — Canonicalization

- [ ] Is Contract Canonicalization distinct from compiler representation normalization or canonical storage preparation?

## TERM-03 — Lowering

- [ ] Is Contract Lowering distinct from HIR→MIR→LIR/JVM compiler lowering?

## TERM-04 — Failure

- [ ] Is Failure Contract distinct from compiler unsuccessful result, exception, crash, panic, or corruption?

## TERM-05 — Diagnostic

- [ ] Are Diagnostic Evidence/Retention Contracts distinct from compiler diagnostic products and recovery evidence?

## TERM-06 — Version

- [ ] Is Contract Version distinct from compiler generation, schema version, and artifact revision?

## TERM-07 — State / Transition

- [ ] Are Contract State and State Transition distinct from HIR/compiler lifecycle states?

## TERM-08 — Fact

- [ ] Is Fact Contract distinct from compiler-derived facts or analysis results?

## TERM-09 — Policy / Budget / Capacity / Governance

- [ ] Are machine Contract authorities distinct from compiler configuration, scheduler policy, memory budget, and
  internal resource control?

## TERM-10 — Input / Output

- [ ] Are Input/Output Presentations distinct from host method parameter/return carrier shapes by themselves?

---

# 38. Conformance / Self-Protection / QA Seam Checklist

The exact implementation belongs to Design/Verification, but the architecture must leave these seams possible.

## QA-01 — Producer / Consumer Drift

- [ ] Is the risk reduced that producer and consumer independently reimplement the same semantic law and drift apart?

## QA-02 — Structural Common Validator

- [ ] Is the common HIR/Protocol validator kept structural, without reimplementing owning 1D semantic judgments?

## QA-03 — Independent Reference Path

- [ ] Can production output be compared against an independently derived semantic reference path?

## QA-04 — Differential Path

Can these paths be cross-compared?

```text
clean
cached
parallel
persistent
incremental
representation A
representation B
```

## QA-05 — Translation Validation Seam

- [ ] Is there a seam to verify that a pre-seal representation transform preserves Candidate meaning?

## QA-06 — Checker Is Not Authority

- [ ] Is verifier/checker output prevented from becoming Contract authority?

## QA-07 — Golden / Property-Based Testing Seam

- [ ] Can golden vectors and property-based tests be derived from producer-owned semantics?

## QA-08 — Corruption

- [ ] Are malformed or corrupt persisted HIR/Established backings rejected rather than admitted as semantic material?

## QA-09 — Decode Success Is Not Semantic Validity

- [ ] Is successful deserialization insufficient to establish current validity or semantic correctness?

## QA-10 — Performance Regression Guard

- [ ] Can benchmarks detect whether the logical Protocol boundary accidentally forces per-read allocation, repeated
  hashing, object chasing, or unnecessary indirection?

## QA-11 — Reference-Kind Confusion

- [ ] Do tests intentionally substitute same-width but wrong-kind references/handles and verify fail-closed behavior?

## QA-12 — Stale-Generation Confusion

- [ ] Do tests inject stale generation-local handles and stale semantic observations to ensure they cannot resolve as
  current material without revalidation?

## QA-13 — Adversarial Cardinality and Depth

- [ ] Are extreme cardinality, deep nesting, long paths, and pathological duplicate sets tested without relying on
  recursive host call stacks or unbounded temporary allocation?

## QA-14 — Overflow and Bounds

- [ ] Are size/count/range calculations overflow-safe, and are malformed lengths rejected before allocation or
  traversal?

## QA-15 — Corrupt Persistent-Artifact Fuzzing

- [ ] Can persistence reload be fuzzed for truncated, reordered, duplicated, mistagged, and internally inconsistent
  records?

## QA-16 — Deterministic Schedule Perturbation

- [ ] Can worker count, task order, hash iteration order, cache state, and randomized scheduling be varied while
  checking invariant semantic output?

## QA-17 — Closure-Invalidation Tests

- [ ] Do reuse tests cover the appearance of a new member, disappearance of an old member, and changes to the legal
  membership domain rather than validating only existing members?

## QA-18 — Resource-Failure Containment

- [ ] Can OOM-prevention limits, allocation refusal, I/O failure, and other realization failures be injected without
  converting them into fabricated Contract meaning?

## QA-19 — Handoff Differential Check

- [ ] Can the exact legal HIR observations consumed by Establishment be captured or independently reconstructed for QA
  and compared with the declared handoff contract?

## QA-20 — Semantic Negative-Space Testing

- [ ] Do tests cover legal absence, illegal absence, required-but-unresolved, unsupported, unavailable, and corrupt
  states separately rather than collapsing them into a single null/missing path?

## QA-21 — Change Classification

- [ ] Can a representation-only change, semantic Contract change, migration, compatibility adaptation, and compiler bug
  fix be distinguished so that implementation churn does not silently mint Contract revisions or hide semantic changes?

## QA-22 — Failed-Replacement Visibility

- [ ] Do tests verify that a failed new generation, failed migration, or failed incremental repair cannot displace the
  last valid visible semantic product?

---

# 39. Per-1D Final Output Bundles

Completing an owning 1D ADR should produce at least the following 14 bundles. The added handoff and adversarial QA
checks are incorporated into Bundles 8 and 14 rather than creating a new semantic layer.

## Bundle 1 — Authority / Catalog / Scope Specification

```text
exact authority
catalog membership
selection scope
authority scope
cross-scope law
absence/default law
```

## Bundle 2 — Source / Acquisition Specification

```text
authoring source
carrier law
source resolution
snapshot / mutability boundary
provenance relation
recovery exclusion
```

## Bundle 3 — HIR Definition Candidate Specification

```text
Candidate meaning
Candidate Reference
Definition determinants
direct HIR references
presence/absence/cardinality/order
collision/coverage/merge law
semantic equality
```

## Bundle 4 — HIR Binding Candidate Specification

```text
selecting subject
role coordinate
target CandidateRef
binding-owned qualifier
binding equality/collision law
```

## Bundle 5 — Coordinate / Shape Specification

```text
nominal coordinates
aggregate/collection shape
nested/recursive law
element/key/value relation
cardinality/ordering law
```

## Bundle 6 — Resolved HIR Protocol Catalog

```text
typed reference domain
Definition Candidate Projection
Binding Candidate Projection
Fine-Grained Projection catalog
projection completeness
projection equality
```

## Bundle 7 — HIR Seal / Visibility / Evolution Specification

```text
seal requirements
recovery exclusion
visibility unit
snapshot coherence
lifetime
Protocol evolution / unknown-extension law
```

## Bundle 8 — Establishment Judgment and Handoff Specification

```text
judgment inventory
owner
legal entry
exact HIR observations
separately Established prerequisites
handoff determinant traceability
hidden-reconstruction prohibition
result vocabulary
complete Established unit
non-entry behavior
```

## Bundle 9 — Established Identity / Material Specification

```text
Established family classification
Definition Meaning
Owning Authority Binding
Version Binding
Authority-local coordinate
Definition Reference
direct Established relations
```

## Bundle 10 — Basis / Applicability / Composition Specification

```text
Basis Requirement Law
Required Basis instance law
Basis Resolution
Basis Binding
Applicable Context
Applicability Result
Complete Basis
singularity/arbitration
composition
semantic prerequisite order/cycle law
```

## Bundle 11 — Occurrence Specification

```text
Occurrence existence
Occurrence Reference
Definition Reference
Determining Semantic Basis
Established Result
historical attribution
```

## Bundle 12 — World / Backing Placement Specification

```text
Canonical Contract World membership
separate occurrence backing
other authority-specific backing
coherent visibility unit
relation-state taxonomy
```

## Bundle 13 — Established Semantic Protocol Catalog

```text
typed Established reference domain
Definition Projection
Occurrence Projection
authority-specific Projection
Direct Established Relation Projection
Fine-Grained Established Projection
completeness/equality law
```

## Bundle 14 — Determinism / Failure / Reuse / Representation / QA Conformance

```text
semantic determinant set
ordering law
compiler-result boundary
Contract Failure boundary
run/world binding
reuse validity / closure validity
physical representation freedom
reference-kind safety
adversarial resource/corruption QA seam
independent conformance seam
V2 replaceability
```

---

# 40. Final Per-1D Closure Gate

If any applicable question remains `OPEN`, and that question can change semantic meaning or legal observation, the
owning ADR is not closed.

1. What exactly is this authority?
2. Where does it belong in the current catalog?
3. What explicitly selects or grants this role?
4. What is the exact authority scope?
5. Are there explicit cross-scope relations?
6. Does a reusable Definition exist?
7. What exactly is the Definition Candidate?
8. What are all determinants of Candidate meaning?
9. What context is Binding-only?
10. What context belongs to Basis, Applicability, attribution, or provenance instead?
11. What are the exact components of Candidate Reference?
12. How do Candidate identity and Candidate semantic equality differ?
13. What exact direct HIR references exist?
14. How are presence, valid absence, missing, unresolved, unsupported, unavailable, and corrupt states distinguished?
15. Is cardinality semantic?
16. Is ordering semantic?
17. What aggregate, collection, nested, and recursive shape laws apply?
18. What duplicate and collision laws apply?
19. What coverage law applies?
20. Does semantic merge exist, and who owns it?
21. Is the authority Version-sensitive?
22. Where is the boundary between the resolved HIR Version candidate and authoritative Version Binding?
23. Does a Basis Requirement Law exist?
24. Does an Applicability Law exist?
25. What does the Resolved HIR Candidate Protocol EXPOSE, GUARANTEE, RETAIN-HIDE, SEPARATE, or FORBID?
26. Is the Definition Candidate Projection complete?
27. Is the Binding Candidate Projection complete?
28. What Fine-Grained HIR Projection catalog exists?
29. Who defines projection completeness?
30. Who defines projection equality?
31. Can complete HIR meaning be observed without reopening source or provenance as an authority source?
32. What invariants must be closed before HIR seal?
33. Are recovery/poison materials separate from valid HIR?
34. Is partial HIR construction hidden from ordinary consumers?
35. What is the lifetime of the HIR generation/reference/projection?
36. How is snapshot coherence guaranteed semantically?
37. Do unknown required semantic extensions fail closed?
38. Are authority-specific HIR surface evolution and Contract Version separate stability domains?
39. How many Establishment judgments does this 1D own or participate in?
40. Who owns each judgment?
41. What are the legal-entry conditions?
42. What separately Established prerequisites are required?
43. Are ambient `current` and other hidden semantic inputs prohibited?
44. What exact result vocabulary may an entered judgment produce?
45. How are Judgment and Establishment related for this authority?
46. What is the minimum complete Established unit?
47. Is partial authoritative visibility possible, and if so, why is that partial material complete meaning of its own?
48. What must never be fabricated when the judgment does not legally enter?
49. Are compiler unsuccessful results distinct from Contract-owned negative results?
50. Are Failure Contract results distinct from other negative results?
51. Is indeterminate/trust-loss state prevented from being forged into success or failure?
52. Does Established Definition Material exist?
53. Does other authority-specific Established Material exist?
54. What is the exact Source Authority?
55. What is the Owning Authority Binding?
56. What is the Authority-Owned Definition Identity?
57. What is the exact Definition Reference?
58. What is the relation between CandidateRef and DefinitionRef?
59. How is same-identity-coordinate plus conflicting meaning handled?
60. Is meaning immutable after Establishment?
61. Are history/supersession and current identity separate?
62. Who owns each exact Required Basis instance?
63. Who owns Basis Resolution?
64. What is the exact Basis Binding?
65. Are Basis Binding and Applicability distinct?
66. Is Applicable Context sparse but complete?
67. Are `no context required` and `required context unavailable` distinct?
68. Can several Basis Bindings be Applicable?
69. Who owns singularity and arbitration?
70. What constitutes Complete Basis?
71. Does an incomplete prerequisite prevent downstream legal entry rather than fabricate Failure?
72. Are circular semantic establishment dependencies prohibited?
73. If composition exists, who exactly owns it?
74. Is a local 1D prevented from inventing Whole-Machine relations?
75. Does Established Occurrence meaning exist?
76. What determinants distinguish occurrences?
77. What is the Occurrence Reference?
78. What is the Determining Semantic Basis?
79. Are distinct occurrences preserved even when result values are equal?
80. Can later current State/Version/Policy/Governance never rewrite historical occurrence attribution?
81. Is run/world binding semantic for this authority?
82. What material must be pinned for the duration of a run or occurrence?
83. Does the owning law define whether retry/restart is the same occurrence or a new one?
84. What enters the Canonical Contract World?
85. What remains in separate occurrence backing?
86. What other authority-specific backing exists?
87. Are relation-not-owned, valid absence, present, and required-unresolved states distinct?
88. What Typed Established Semantic Reference Domains exist?
89. What is the Established Definition Projection?
90. What is the Established Occurrence Projection?
91. Is an authority-specific Established Projection required?
92. What Direct Established Relation Projections exist?
93. What Fine-Grained Established Projections exist?
94. Is the consumer prohibited from redefining projection meaning, equality, or completeness?
95. Is generic graph navigation prevented from becoming an authority surface?
96. Are semantic absence, not retained, not materialized, unavailable, unsupported, stale, and corrupt states distinct?
97. Is current validity of retained/persisted material verified independently?
98. Is membership closure verified for reused complete-set projections?
99. Is lazy materialization physical-only after semantic completion?
100. Do clean, cached, persistent, incremental, and parallel paths expose the same legal observation?
101. Does the same semantic basis produce the same Established meaning?
102. Can worker, cache, scheduler, discovery, and storage order never change meaning?
103. Are physical completion order and semantic prerequisite order distinct?
104. Can concurrent duplicate computation never create duplicate authority?
105. Are source path, AST identity, runtime object, JVM shape, HID, handle, query, cache, and storage excluded as
     identity authority?
106. Does semantic distinction avoid forcing one object/table/query per concept?
107. Are compiler normalization/canonical storage and the Canonicalization Contract distinct?
108. Are compiler IR lowering and the Lowering Contract distinct?
109. Are compiler diagnostics and Diagnostic Evidence/Retention Contracts distinct?
110. Are compiler unsuccessful results and Failure Contract distinct?
111. Are compiler lifecycle states and Contract State/Transition distinct?
112. Are Contract Version and compiler schema/generation distinct?
113. Can an independent conformance/reference path exist?
114. Can the V1 physical representation change while exposing the same Protocol observation?
115. Can V2 persistence/incremental algorithms change without changing semantic law?
116. Can every Establishment determinant be traced to legal HIR observation or an exact authoritative prerequisite?
117. Is Establishment prohibited from reconstructing missing Candidate meaning from source, host, query, or backing
     topology?
118. Is HIR prohibited from pre-establishing later authority merely for implementation convenience?
119. Are typed reference domains protected against cross-kind substitution even when physical encodings share a
     primitive representation?
120. If meaning asserts the absence of another member, is the domain of that absence explicitly closed?
121. If the legal membership domain changes, is the effect on prior completeness explicitly defined?
122. Is completeness/singularity owned once rather than independently re-decided by several layers?
123. Can malformed/oversized/deeply nested/adversarial material be rejected or bounded without weakening semantic law?
124. Are count/range/size calculations overflow-safe before allocation or traversal?
125. Can corrupt persistence, stale handles, and wrong-kind reference substitution be tested fail-closed?
126. Can scheduling, worker count, cache state, and traversal perturbation be tested while preserving identical semantic
     output?
127. Can HIR-to-Establishment handoff be independently checked against the declared legal observation contract?
128. For every erased source distinction, is there an explicit information-loss justification showing that no legal
     semantic observer still requires it?
129. Does physical representation preservation include provenance, generation coherence, lifetime, stale-reference
     rejection, and persistence obligations rather than payload equality alone?
130. Can a failed new HIR generation or repair never replace the last valid visible generation?
131. Is raw observation of runtime/compiler state prevented from creating Contract meaning?
132. If realization state affects a Contract judgment, is that state first converted into exact authority-owned semantic
     material?
133. Can retention/reclamation never retroactively change whether Establishment occurred?
134. Is internal Established authority kept separate from Publication and Output Presentation outward authority?

---

# 41. Working Matrix Rows for the 19 1D Authorities

> Working set follows the current semantic model including `Output Presentation`. The ADR-0046/0047 catalog drift still
> requires a separate repair.

|  # | 1D Authority                    | Owning ADR           | HIR Bundle | Establishment Bundle | Established Protocol | Status                     |
|---:|---------------------------------|----------------------|------------|----------------------|----------------------|----------------------------|
|  1 | Input                           | ADR-0064             | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
|  2 | Admission                       | ADR-0065             | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
|  3 | Canonicalization                | ADR-0066             | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
|  4 | Lowering                        | ADR-0067             | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
|  5 | Fact                            | ADR-0068             | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
|  6 | Invariant                       | ADR-0069             | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
|  7 | State                           | ADR-0050 family      | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
|  8 | State Transition                | ADR-0050 family      | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
|  9 | Explicit State Machine Manifest | ADR-0050 family      | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
| 10 | Failure                         | ADR-0057             | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
| 11 | Publication                     | ADR-0058             | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
| 12 | Output Presentation             | ADR-0059             | TBD        | TBD                  | TBD                  | CATALOG-DRIFT + OPEN AUDIT |
| 13 | Diagnostic Evidence             | ADR-0060/0061 family | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
| 14 | Diagnostic Retention            | ADR-0060/0062 family | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
| 15 | Version                         | ADR-0053             | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
| 16 | Policy                          | ADR-0054             | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
| 17 | Budget                          | ADR-0051             | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
| 18 | Capacity                        | ADR-0052             | TBD        | TBD                  | TBD                  | OPEN AUDIT                 |
| 19 | Governance                      | ADR-0056             | TBD        | TBD                  | TBD                  | OWNER-SPECIFIC OPEN ITEMS  |

### Control Row — Interface Surface Contract

Audit `Interface Surface Contract` separately from the 19 1D rows.

```text
Contract Interface Subject
Interaction Subject
Operation Subject
Operation Parameter Coordinates
Operation Result Position
IDL role/slot binding surface
Generated API separation
```

This control surface supplies Primary HIR semantic subjects and exact coordinates. It does not replace or absorb the
meaning owned by any 1D authority.

---

# 42. Recommended Execution Order

## Phase 0 — Catalog Repair

1. Confirm and repair the missing Output Presentation entry in ADR-0046/0047.
2. Close the formal catalog cardinality as `Interface Surface control surface + 19 1D authorities`, or document another
   explicit result if the project decides otherwise.
3. Establish one source of truth for matrix membership and naming.

## Phase 1 — Common Dependency Status

1. Confirm the current status and ownership boundary of ADR-0074.
2. Mark compiler-result checklist items provisional until their owning ADR is accepted.
3. Mark Governance scope/singularity questions as owner-specific open decisions rather than inventing a common answer.

## Phase 2 — First-Pass Inventory Across All 19 Rows

Do not start by writing full prose answers for every checklist item. First classify the major bundles.

```text
Authority/Scope
Source/Acquisition
HIR Definition Candidate
HIR Binding Candidate
Coordinate/Shape
HIR Protocol
HIR Seal/Visibility/Evolution
Establishment Judgment/Handoff
Established Identity/Material
Basis/Applicability/Composition
Occurrence
World/Backing Placement
Established Protocol
Determinism/Failure/Reuse/Representation/QA
```

Mark each bundle `DEFINED / NOT-APPLICABLE / OPEN / BLOCKED`.

## Phase 3 — Extract OPEN Clusters

Group recurring open decisions across the 19 rows.

Examples:

```text
Occurrence law missing in 8 authorities
Definition determinant unclear in 5 authorities
scope/singularity missing in Governance family
collection/aggregate coordinate missing in Input/Output
compiler-result handoff blocked by ADR-0074
handoff determinant traceability missing in several authorities
```

If the same OPEN recurs because a true common law is missing, re-audit ADR-0071/0063.

If the OPEN exists only for one authority, it belongs to that owning 1D ADR.

## Phase 4 — Revise 1D ADRs

Proceed in small batches. For each batch:

```text
evidence gathering
-> detailed proposal
-> explicit decision
-> owning ADR revision
-> checklist status update
```

Do not edit several 1D ADRs by copying a generic payload schema.

## Phase 5 — Reverse Audit

After 1D revisions, audit downstream flow in the reverse direction:

```text
1D ADR
    ↓
HIR Candidate surface
    ↓
Resolved HIR Candidate Protocol
    ↓
Establishment input / handoff
    ↓
Established Material
    ↓
Established Semantic Protocol
    ↓
Verifier / Diagnostics / PBT / Reference Judgment / Execution consumers
```

The key question is whether any consumer must reconstruct missing semantic meaning privately.

## Phase 6 — Derive Executable Verification and QA

For every closed semantic law, create traceability into executable verification.

Recommended pattern:

```text
Owning ADR clause
    ↓
Checklist ID
    ↓
Conformance property
    ↓
Test oracle / reference path
    ↓
PBT / differential / fuzz / stress / golden vector
    ↓
production implementation
```

A test ID should be able to point back to the semantic law it protects, and a semantic law should be able to identify
the verification coverage that protects it.

---

# 43. Decisions This Checklist Intentionally Does Not Make

This document must not freeze the following as common semantic law:

```text
Kotlin class hierarchy
one universal HIR node
one universal EstablishedMaterial schema
one object per semantic coordinate
one table per authority
one query per projection
one cache entry per Definition
one physical snapshot implementation
one epoch/lease algorithm
one persistence format
one HID/fingerprint algorithm
one Merkle structure
one incremental algorithm
one pass manager
one query scheduler
one allocator
one mmap / FFM / heap choice
one diagnostic renderer
one verifier implementation
one fuzzer implementation
one benchmark harness
one serialization library
```

These remain Design, Verification, QA, or realization concerns unless an owning Contract law explicitly makes a
particular distinction semantic.

---

# 44. Final Assessment

## Common Semantic Checklist Level

After this re-audit, the document covers the following common axes from ADR-0071 and ADR-0063 while keeping
authority-specific answers in their owning ADRs:

```text
HIR nature
frontend resolution
1D-owned determinants
Candidate / Binding separation
reference identity and reference-kind separation
shape / multiplicity
Version boundary
Basis Requirement Law
Applicability Law
Resolved HIR Candidate Protocol
HIR-to-Establishment handoff integrity
seal / recovery
visibility / lifecycle
snapshot coherence
Protocol evolution
Establishment entry
Judgment / Establishment separation
source authority
Established Definition identity
Required Basis
Basis Binding
Applicability
Complete Basis
coverage / closure / singularity
composition
semantic prerequisite order
cycle prohibition
Occurrence
run/world binding
Canonical Contract World placement
Established Semantic Protocol
consumer law
availability
reuse / closure validity
determinism / concurrency
representation freedom
compiler-result boundary
observation / retention / outward-authority integrity
information-loss burden
terminology / authority collision
independent conformance seam
adversarial / corruption / resource QA seam
V1/V2 replaceability
```

At this point the document is suitable as the **common HIR–Establishment derivation and verification checklist** for the
1D ADR family.

## Remaining Issues Outside the Common Checklist That Still Must Be Closed

1. Repair the ADR-0046/0047 Output Presentation catalog drift.
2. Close the ADR-0074 compiler-result dependency or relocate its responsibilities to an Accepted owner.
3. Close Governance-specific scope, Binding, validity, singularity, and occurrence/decision structure.
4. Define the exact Candidate payload, determinant set, and Candidate Reference component set for every 1D.
5. Define the exact Established projection catalog for every 1D.
6. Decide whether each authority has independent occurrence meaning.
7. Close authority-specific collision, coverage, merge, completeness, and singularity laws.
8. Close exact shape, aggregate, collection, depth, and element-authority laws for aggregate-heavy authorities such as
   Input and Output.
9. Build the physical HIR/Established access APIs, query/cache/storage realization, conformance harness, and executable
   QA architecture without turning them into authority.
10. Derive traceable QA suites from each closed semantic law before production implementation becomes the de facto
    specification.

The next step should therefore not be another round of abstract checklist expansion. The checklist should now be applied
across the 19 1D rows, with every `OPEN` backed by evidence and routed either to a true common-law gap or to the exact
owning ADR.