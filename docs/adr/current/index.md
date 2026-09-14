# Current ADRs

This index lists ADRs that currently participate in the Kontrakt architecture.

`Accepted` ADRs provide current authority within their scope. `Proposed` and `Draft` ADRs remain active work but do not
provide Accepted authority.

- [Documentation overview](../../README.md)
- [Complete ADR registry](../index.md)

## Contract

### Interface & API

| ADR                                                                                                                                        | Title                                                                                               | Status   |
|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|----------|
| [ADR-0046](contract/interface-api/0046-idl-first-interface-contract-frontend-1d-catalog-backend-discipline.md)                             | IDL-First Interface Contract Frontend, Generated Host Interface, and Operation Realization Boundary | Accepted |
| [ADR-0047](contract/interface-api/0047-one-dimensional-contract-presentations-pipeline-slot-selection-and-backend-realization-boundary.md) | One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary   | Accepted |

### Data Model

| ADR                                                                                                                         | Title                                                                          | Status   |
|-----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|----------|
| [ADR-0072](contract/data-model/0072-jvm-collection-contract-preservation-aggregate-semantics-and-deterministic-equality.md) | Input Collection Presentation, Aggregate Semantics, and Deterministic Equality | Proposed |

### One-Dimensional Contracts

| ADR                                                                                                                                    | Title                                                                                                           | Status   |
|----------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|----------|
| [ADR-0050](contract/one-dimensional/0050-state-transition-explicit-state-machine-and-state-machine-axis.md)                            | State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis                            | Accepted |
| [ADR-0051](contract/one-dimensional/0051-budget-contract-explicit-allowance-accounting-allocation-and-backend-realization-boundary.md) | Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary          | Accepted |
| [ADR-0052](contract/one-dimensional/0052-capacity-contract-safe-operating-limits-simultaneous-load-and-admission-boundary.md)          | Capacity Contract, Explicit Safe Operating Memory Limits, and Realization Boundary                              | Accepted |
| [ADR-0064](contract/one-dimensional/0064-input-contract-explicit-boundary-presentation.md)                                             | Input Contract, Explicit Boundary Presentation, and External-Authority Boundary                                 | Accepted |
| [ADR-0065](contract/one-dimensional/0065-admission-contract-continuation-judgment.md)                                                  | Admission Contract, Explicit Continuation Judgment, and Deterministic Evaluation Boundary                       | Accepted |
| [ADR-0066](contract/one-dimensional/0066-canonicalization-contract-stable-representative-and-canonical-bytes.md)                       | Canonicalization Contract, Stable Representative, Canonical Bytes, and Explicit Omission                        | Accepted |
| [ADR-0067](contract/one-dimensional/0067-lowering-contract-explicit-relation-compiler-derived-realization-and-core-entry.md)           | Lowering Contract, Explicit Source-to-Operation Relation, Compiler-Derived Realization, and Core Entry Boundary | Accepted |
| [ADR-0068](contract/one-dimensional/0068-fact-contract-explicit-immutable-core-information-sameness-and-uniqueness.md)                 | Fact Contract, Explicit Immutable Core Information, Sameness, Uniqueness, and Vocabulary                        | Accepted |
| [ADR-0069](contract/one-dimensional/0069-invariant-contract-fact-local-standing-integrity-law.md)                                      | Invariant Contract, Fact-Local Standing Integrity Law, Deterministic Judgment, and Establishment Gate           | Accepted |

### Establishment

| ADR                                                                                                                | Title                                                            | Status   |
|--------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------|----------|
| [ADR-0063](contract/establishment/0063-contract-establishment-occurrence-applicability-and-semantic-dependency.md) | Contract Establishment, Identity, Applicability, and Composition | Accepted |

### Governance

| ADR                                                                                                                                            | Title                                                                                                        | Status   |
|------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|----------|
| [ADR-0053](contract/governance/0053-version-contract-sovereign-meaning-identity-version-claims-and-authority-boundary.md)                      | Version Contract, Sovereign Contract Revision History, and Realization Boundary                              | Accepted |
| [ADR-0054](contract/governance/0054-policy-contract-explicit-operating-modes-self-contained-contract-worlds-and-interface-binding-boundary.md) | Policy Contract, Explicit Operating Policies, Self-Contained Contract Worlds, and Interface Binding Boundary | Accepted |
| [ADR-0056](contract/governance/0056-governance-contract-policy-world-control-whole-machine-coordination-and-selection-boundary.md)             | Governance Contract, Policy-World Control, Whole-Machine Scope, and Selection Boundary                       | Proposed |

### Composition

| ADR                                                                                                  | Title                                                         | Status   |
|------------------------------------------------------------------------------------------------------|---------------------------------------------------------------|----------|
| [ADR-0055](contract/composition/0055-whole-machine-pipeline-composition-and-contract-concurrency.md) | Whole Machine, Pipeline Composition, and Contract Concurrency | Accepted |

### Outcomes

| ADR                                                                                                                            | Title                                                                                  | Status   |
|--------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|----------|
| [ADR-0057](contract/outcomes/0057-failure-contract-explicit-machine-failure-attribution-scope-and-realization-boundary.md)     | Failure Contract, Explicit Machine Failure, Attribution, and Realization Boundary      | Accepted |
| [ADR-0058](contract/outcomes/0058-publication-contract-explicit-outward-meaning-authority-and-realization-boundary.md)         | Publication Contract, Explicit Outward Exposure Authority, and Core Exit Boundary      | Accepted |
| [ADR-0059](contract/outcomes/0059-output-presentation-contract-and-explicit-outward-result-shape-and-machine-exit-boundary.md) | Output Presentation Contract, Explicit Outward Result Shape, and Machine Exit Boundary | Accepted |
| [ADR-0060](contract/outcomes/0060-diagnostic-evidence-and-retention-contract.md)                                               | Diagnostic Evidence and Retention Contract                                             | Proposed |

## Compiler Structure

| ADR                                                                                                                                       | Title                                                                                                | Status   |
|-------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|----------|
| [ADR-0045](compiler-structure/0045-contract-pipeline-package-architecture-explicit-state-machine-axis-and-compiler-realization-mirror.md) | Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror | Accepted |

## Frontend

| ADR                                                                                                                          | Title                                                                                             | Status   |
|------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|----------|
| [ADR-0037](frontend/0037-cycle-identity-preflight-and-deferred-raw-fact-resulution.md)                                       | Cycle Identity Preflight and Deferred Raw Fact Resolution                                         | Accepted |
| [ADR-0039](frontend/0039-adapter-neutral-metamodel-acquisition-frozen-fact-image-and-backend-handle-erasure.md)              | Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure              | Accepted |
| [ADR-0040](frontend/0040-deterministic-frozen-acquisition-pipeline-explicit-readiness-and-memory-disciplined-publication.md) | Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication | Accepted |

## IR

### HIR

| ADR                                                                                                              | Title                                                                                   | Status   |
|------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|----------|
| [ADR-0071](ir/hir/0071-resolved-contract-hir-semantic-boundary-deterministic-publication-lifecycle-and-reuse.md) | Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse | Accepted |

## Compiler Engine

| ADR                                                                                                                         | Title                                                                                   | Status   |
|-----------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|----------|
| [ADR-0031](compiler-engine/0031-two-tier-transactional-memoization-and-structural-interning.md)                             | Two-Tier Transactional Memoization and Structural Interning                             | Accepted |
| [ADR-0034](compiler-engine/0034-explicit-dual-axis-l2-join-lifecycle-state-machine-and-single-terminalization-authority.md) | Explicit Dual-Axis L2 Join Lifecycle State Machine and Single Terminalization Authority | Accepted |
| [ADR-0035](compiler-engine/0035-deterministic-m:n-dispatch-lanes-for-tier-2-join-completion-delivery.md)                    | Deterministic M:N Dispatch Lanes for Tier-2 Join Completion Delivery                    | Accepted |
| [ADR-0036](compiler-engine/0036-joined-wait-planning-run-suspension-bridge-and-fresh-session-restart-authority.md)          | Joined-Wait Planning-Run Suspension Bridge and Fresh-Session Restart Authority          | Accepted |

## Diagnostics

| ADR                                                                       | Title                                                                         | Status   |
|---------------------------------------------------------------------------|-------------------------------------------------------------------------------|----------|
| [ADR-0061](diagnostics/0061-kontrakt-compiler-diagnostic-architecture.md) | Kontrakt Compiler Diagnostic Architecture                                     | Proposed |
| [ADR-0062](diagnostics/0062-contract-machine-diagnostic-realization.md)   | Contract-Machine Diagnostic Realization and Operational Evidence Architecture | Proposed |

## Runtime

| ADR                                                                                                                         | Title                                                                                             | Status   |
|-----------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|----------|
| [ADR-0032](runtime/0032-capacity-law-resource-policy-resolution-identity-hierarchy-and-zero-residue-semantics.md)           | Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics          | Accepted |
| [ADR-0033](runtime/0033-bootstrap-runtime-policy-ratification-storage-governance-and-deferred-platform-aware-autotuning.md) | Bootstrap Runtime Policy Ratification, Storage Governance, and Deferred Platform-Aware Autotuning | Accepted |
| [ADR-0044](runtime/0044-unified-runtime-memory-envelop-and-pipeline-lifecycle-governance.md)                                | Unified Runtime Memory Envelope and Pipeline Lifecycle Governance                                 | Draft    |

## Infrastructure

| ADR                                                                                                       | Title                                                                    | Status   |
|-----------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------|----------|
| [ADR-0041](infrastructure/0041-stable-metadata-identity-blake3-hid-and-protocol-owned-interning.md)       | Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning      | Proposed |
| [ADR-0042](infrastructure/0042-mechanical-sympathy-primitive-lifecycle-and-async-ownership-governance.md) | Mechanical Sympathy, Primitive Lifecycle, and Async Ownership Governance | Proposed |

ADR-0041 also has an [extracted maintenance edition](infrastructure/0041-extracted-maintenance-edition.md) under the
same ADR identity.

## Count

Current ADR identities: **37**

ADRs under `migration/` and `historical/` are intentionally excluded from this index.