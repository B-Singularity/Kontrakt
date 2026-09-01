# Kontrakt Architecture Decision Records

This directory contains the Architecture Decision Records of Kontrakt.

An ADR records an architectural decision that the project intends to preserve together with the reason for that
decision. In Kontrakt, an ADR may also own Contract law when the decision defines Contract meaning or authority.

Implementation detail may appear when it is necessary to explain a decision or its trade-off. Detailed realization that
does not define or justify an architectural decision belongs in `design/`.

## 1. ADR Authority

An ADR is authoritative only according to its declared status.

The directory containing an ADR makes its authority state visible, but the `Status` field inside the document remains
the source of truth.

A historical ADR remains part of the project record. It must not be used as current architectural or Contract authority.

A migration ADR may still own material that has not yet moved. Its remaining authority must be stated explicitly.

## 2. Directory Structure

```text
docs/adr/
├── README.md
├── index.md
├── current/
│   └── index.md
├── migration/
│   └── index.md
└── historical/
    ├── index.md
    ├── superseded/
    │   └── index.md
    ├── deprecated/
    │   └── index.md
    ├── rejected/
    │   └── index.md
    └── withdrawn/
        └── index.md
```

`current/` contains ADRs that still participate in the current architecture. A document may be incomplete and still
belong here when its decision is under active design rather than migration.

`migration/` contains ADRs whose ownership or structure is being changed. This directory exists to prevent an authority
gap while material moves to its new owner.

`historical/` contains records that no longer provide current authority.

## 3. Status

Every ADR must declare one status near the beginning of the document.

### Draft

The decision is being formed and is not yet ready for formal review.

A Draft provides no current authority beyond material that another Accepted ADR already establishes.

### Proposed

The decision is sufficiently formed for review but has not been accepted.

A Proposed ADR is part of the current design space. It must not be treated as established architecture unless an
Accepted ADR explicitly relies on material already established elsewhere.

### Accepted

The decision is current architectural authority.

An Accepted ADR may be used as a normative basis by later ADRs, design documents, compiler work, and Contract reasoning
within the scope that it owns.

### Migration Pending

The ADR contains current material whose ownership or document structure is scheduled to change.

The document remains authoritative only for material that has not yet migrated. The migration target must be recorded as
soon as it is known.

### Partially Superseded

Part of the ADR has already been replaced by later authority while another part remains current.

The document must state which material has moved and where current ownership now resides. Material not identified as
remaining authoritative must not be inferred to remain current.

### Superseded

A later decision has replaced the ADR's architectural authority.

The ADR remains as historical provenance and must identify its successor.

### Deprecated

The decision was once current but is no longer part of the architecture.

A Deprecated ADR does not require a direct successor. This status is used when the former decision has been removed
rather than replaced by one equivalent owner.

### Rejected

The proposal was considered but never accepted.

Rejected material may explain why an alternative was not chosen. It provides no current authority.

### Withdrawn

The proposal was intentionally abandoned before acceptance.

Withdrawal records that the work stopped without establishing the proposed decision. It does not imply that the proposal
was technically disproven.

## 4. Status and Path

Status determines the expected path.

```text
Draft                  -> current/
Proposed               -> current/
Accepted               -> current/

Migration Pending      -> migration/
Partially Superseded   -> migration/

Superseded             -> historical/superseded/
Deprecated             -> historical/deprecated/
Rejected               -> historical/rejected/
Withdrawn              -> historical/withdrawn/
```

A normal transition from Draft to Proposed or Accepted does not move the file. Stable paths are preferred while a
decision remains current.

A file moves when its authority class changes. Migration and historical records are physically separated because stale
normative language must not appear indistinguishable from current law.

## 5. Numbering

ADR numbers are permanent historical identities.

An issued number is never reused. Existing ADRs are not renumbered to make the current architecture appear sequential.

A new decision receives a new number even when it replaces, extracts, or reorganizes an older ADR.

Semantic reading order belongs in the indexes. It is not encoded by ADR numbering.

## 6. Migration

Migration changes document ownership without rewriting history.

A migration must first identify the material currently owned by the source ADR. Material then moves to the ADR that
should own it under the current architecture.

Migration by itself must not introduce new Contract semantics. A semantic change requires its own review in the
destination ADR.

Until material has moved, authority remains with the source ADR. Once material has moved, the source must identify the
new owner.

If all current authority leaves the source ADR, the source moves to the appropriate historical state.

If an architectural decision remains after extraction, the original ADR may stay current with a narrower scope.

## 7. ADR Relationships

Relationships are recorded only when they describe an actual decision history.

Use `Supersedes` when the current ADR replaces the authority of an earlier ADR.

Use `Superseded by` in the historical ADR to identify its current successor.

Use `Extracted from` when material is separated from an earlier combined ADR without claiming that the new ADR replaces
every decision in the source.

Use `Related` for important context that does not transfer authority.

Relationships must identify exact ADR numbers. A relationship must not be inferred from file order or numbering
proximity.

## 8. Document Header

Each ADR should begin with a compact metadata block.

```markdown
# ADR-XXXX: Title

**Status:** Accepted

**Date:** YYYY-MM-DD
```

Add relationship fields only when they apply.

```markdown
**Extracted from:** ADR-0048
```

or

```markdown
**Superseded by:** ADR-0064, ADR-0065
```

The header should describe document state. Rationale belongs in the body.

## 9. Titles and File Names

The title states the decision subject rather than its implementation history.

The file name begins with the permanent ADR number and uses a concise lowercase description.

```text
0064-input-contract.md
```

Renaming a file does not change ADR identity.

A title should not claim broader authority than the document actually owns.

## 10. ADR Content

An ADR should explain the problem before stating the decision.

The decision must identify the architectural law or trade-off being preserved. It should not read as a changelog of
implementation work.

Consequences should describe the meaningful cost of the decision where that cost helps explain why the choice matters.

Implementation material is appropriate when it makes the architectural trade-off concrete. Operational detail that can
change without changing the decision should move to `design/`.

Contract law must state meaning directly. It must not derive authority from implementation structure, runtime objects,
backend mechanisms, or document organization.

## 11. Ownership

One ADR should have a coherent decision subject.

Independent Contract authorities should not be combined merely because they are adjacent in a pipeline. They may remain
together when the architectural decision is genuinely about their shared relationship and is clearer as one unit.

A later ADR may narrow or extract ownership when the original document became too broad.

Document structure must follow semantic ownership rather than preserve an accidental historical grouping.

## 12. Indexes

`docs/adr/index.md` is the complete ADR registry.

It includes current, migrating, and historical ADRs. The registry exists to locate an ADR by permanent identity and to
show its current status and path.

`current/index.md` presents the current architecture in a useful semantic reading order. It does not need to follow ADR
number order.

`migration/index.md` records active ownership movement. It should make clear what still belongs to the source and what
has already moved.

`historical/index.md` provides the entry point for non-current decisions. Each historical subdirectory maintains its own
local index.

Indexes are navigation and status projections. They do not create architectural authority.

## 13. References

Current work should prefer current ADRs as normative references.

A historical ADR may be cited to explain provenance, an abandoned alternative, or the reason a later decision exists.
The citation must not present historical material as current law.

When a current ADR supersedes or extracts material from an older ADR, new normative references should point to the
current owner.

## 14. Changes to Accepted ADRs

Accepted means that the decision is established. It does not make the document immutable.

A non-material change may be made directly. This includes correction of wording, examples, terminology, references,
formatting, and explanation when the decision itself remains the same.

A material correction may also be made directly when the correction remains within the decision that the ADR already
owns. This includes correcting an earlier mistake even when the corrected statement is materially different from the
previous text.

A material correction should leave a concise correction or amendment record when the previous accepted meaning would
otherwise be difficult to reconstruct from the document itself.

A correction stops being a local edit when it changes the basis of other architectural decisions. If other ADRs must
change their meaning, authority, scope, or important trade-off because of the correction, the change must be recorded as
a new architectural decision.

The new ADR owns that decision. Affected ADRs enter migration when their current material must be reorganized or
rewritten around the new basis.

The size of the textual change does not determine whether a new ADR is required. The relevant boundary is whether the
change remains inside one decision or requires other decisions to be established again.

## 15. Repository Rule

ADR history is preserved.

Current authority must remain easy to identify.

The repository therefore keeps current law, active migration, and historical records separate without discarding the
decisions that led to the present architecture.