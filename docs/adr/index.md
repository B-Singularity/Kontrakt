# Kontrakt ADR Index

This is the registry of Kontrakt Architecture Decision Records.

ADR numbers preserve decision history. Category paths show the current primary ownership of each decision. Status and
location determine whether an ADR provides current authority.

## Current

### Contract

#### Interface & API

| ADR                                                                                                                                                | Title                                                                                               | Status   |
|----------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|----------|
| [ADR-0046](current/contract/interface-api/0046-idl-first-interface-contract-frontend-1d-catalog-backend-discipline.md)                             | IDL-First Interface Contract Frontend, Generated Host Interface, and Operation Realization Boundary | Accepted |
| [ADR-0047](current/contract/interface-api/0047-one-dimensional-contract-presentations-pipeline-slot-selection-and-backend-realization-boundary.md) | One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary   | Accepted |

#### Data Model

| ADR                                                                                                                                 | Title                                                                          | Status   |
|-------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|----------|
| [ADR-0072](current/contract/data-model/0072-jvm-collection-contract-preservation-aggregate-semantics-and-deterministic-equality.md) | Input Collection Presentation, Aggregate Semantics, and Deterministic Equality | Proposed |

#### One-Dimensional Contracts

| ADR                                                                                                                                            | Title                                                                                                           | Status   |
|------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|----------|
| [ADR-0050](current/contract/one-dimensional/0050-state-transition-explicit-state-machine-and-state-machine-axis.md)                            | State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis                            | Accepted |
| [ADR-0051](current/contract/one-dimensional/0051-budget-contract-explicit-allowance-accounting-allocation-and-backend-realization-boundary.md) | Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary          | Accepted |
| [ADR-0052](current/contract/one-dimensional/0052-capacity-contract-safe-operating-limits-simultaneous-load-and-admission-boundary.md)          | Capacity Contract, Explicit Safe Operating Memory Limits, and Realization Boundary                              | Accepted |
| [ADR-0064](current/contract/one-dimensional/0064-input-contract-explicit-boundary-presentation.md)                                             | Input Contract, Explicit Boundary Presentation, and External-Authority Boundary                                 | Accepted |
| [ADR-0065](current/contract/one-dimensional/0065-admission-contract-continuation-judgment.md)                                                  | Admission Contract, Explicit Continuation Judgment, and Deterministic Evaluation Boundary                       | Accepted |
| [ADR-0066](current/contract/one-dimensional/0066-canonicalization-contract-stable-representative-and-canonical-bytes.md)                       | Canonicalization Contract, Stable Representative, Canonical Bytes, and Explicit Omission                        | Accepted |
| [ADR-0067](current/contract/one-dimensional/0067-lowering-contract-explicit-relation-compiler-derived-realization-and-core-entry.md)           | Lowering Contract, Explicit Source-to-Operation Relation, Compiler-Derived Realization, and Core Entry Boundary | Accepted |
| [ADR-0068](current/contract/one-dimensional/0068-fact-contract-explicit-immutable-core-information-sameness-and-uniqueness.md)                 | Fact Contract, Explicit Immutable Core Information, Sameness, Uniqueness, and Vocabulary                        | Accepted |
| [ADR-0069](current/contract/one-dimensional/0069-invariant-contract-fact-local-standing-integrity-law.md)                                      | Invariant Contract, Fact-Local Standing Integrity Law, Deterministic Judgment, and Establishment Gate           | Accepted |

#### Establishment

| ADR                                                                                                                        | Title                                                            | Status   |
|----------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------|----------|
| [ADR-0063](current/contract/establishment/0063-contract-establishment-occurrence-applicability-and-semantic-dependency.md) | Contract Establishment, Identity, Applicability, and Composition | Accepted |

#### Governance

| ADR                                                                                                                                                    | Title                                                                                                        | Status   |
|--------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|----------|
| [ADR-0053](current/contract/governance/0053-version-contract-sovereign-meaning-identity-version-claims-and-authority-boundary.md)                      | Version Contract, Sovereign Contract Revision History, and Realization Boundary                              | Accepted |
| [ADR-0054](current/contract/governance/0054-policy-contract-explicit-operating-modes-self-contained-contract-worlds-and-interface-binding-boundary.md) | Policy Contract, Explicit Operating Policies, Self-Contained Contract Worlds, and Interface Binding Boundary | Accepted |
| [ADR-0056](current/contract/governance/0056-governance-contract-policy-world-control-whole-machine-coordination-and-selection-boundary.md)             | Governance Contract, Policy-World Control, Whole-Machine Scope, and Selection Boundary                       | Proposed |

#### Composition

| ADR                                                                                                          | Title                                                         | Status   |
|--------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------|----------|
| [ADR-0055](current/contract/composition/0055-whole-machine-pipeline-composition-and-contract-concurrency.md) | Whole Machine, Pipeline Composition, and Contract Concurrency | Accepted |

#### Outcomes

| ADR                                                                                                                                    | Title                                                                                  | Status   |
|----------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|----------|
| [ADR-0057](current/contract/outcomes/0057-failure-contract-explicit-machine-failure-attribution-scope-and-realization-boundary.md)     | Failure Contract, Explicit Machine Failure, Attribution, and Realization Boundary      | Accepted |
| [ADR-0058](current/contract/outcomes/0058-publication-contract-explicit-outward-meaning-authority-and-realization-boundary.md)         | Publication Contract, Explicit Outward Exposure Authority, and Core Exit Boundary      | Accepted |
| [ADR-0059](current/contract/outcomes/0059-output-presentation-contract-and-explicit-outward-result-shape-and-machine-exit-boundary.md) | Output Presentation Contract, Explicit Outward Result Shape, and Machine Exit Boundary | Accepted |
| [ADR-0060](current/contract/outcomes/0060-diagnostic-evidence-and-retention-contract.md)                                               | Diagnostic Evidence and Retention Contract                                             | Proposed |

### Compiler Structure

| ADR                                                                                                                                               | Title                                                                                                | Status   |
|---------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|----------|
| [ADR-0045](current/compiler-structure/0045-contract-pipeline-package-architecture-explicit-state-machine-axis-and-compiler-realization-mirror.md) | Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror | Accepted |

### Frontend

| ADR                                                                                                                                  | Title                                                                                             | Status   |
|--------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|----------|
| [ADR-0037](current/frontend/0037-cycle-identity-preflight-and-deferred-raw-fact-resulution.md)                                       | Cycle Identity Preflight and Deferred Raw Fact Resolution                                         | Accepted |
| [ADR-0039](current/frontend/0039-adapter-neutral-metamodel-acquisition-frozen-fact-image-and-backend-handle-erasure.md)              | Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure              | Accepted |
| [ADR-0040](current/frontend/0040-deterministic-frozen-acquisition-pipeline-explicit-readiness-and-memory-disciplined-publication.md) | Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication | Accepted |

### IR

#### HIR

| ADR                                                                                                                      | Title                                                                                   | Status   |
|--------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|----------|
| [ADR-0071](current/ir/hir/0071-resolved-contract-hir-semantic-boundary-deterministic-publication-lifecycle-and-reuse.md) | Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse | Accepted |

### Compiler Engine

| ADR                                                                                                                                 | Title                                                                                   | Status   |
|-------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|----------|
| [ADR-0031](current/compiler-engine/0031-two-tier-transactional-memoization-and-structural-interning.md)                             | Two-Tier Transactional Memoization and Structural Interning                             | Accepted |
| [ADR-0034](current/compiler-engine/0034-explicit-dual-axis-l2-join-lifecycle-state-machine-and-single-terminalization-authority.md) | Explicit Dual-Axis L2 Join Lifecycle State Machine and Single Terminalization Authority | Accepted |
| [ADR-0035](current/compiler-engine/0035-deterministic-m:n-dispatch-lanes-for-tier-2-join-completion-delivery.md)                    | Deterministic M:N Dispatch Lanes for Tier-2 Join Completion Delivery                    | Accepted |
| [ADR-0036](current/compiler-engine/0036-joined-wait-planning-run-suspension-bridge-and-fresh-session-restart-authority.md)          | Joined-Wait Planning-Run Suspension Bridge and Fresh-Session Restart Authority          | Accepted |

### Diagnostics

| ADR                                                                               | Title                                                                         | Status   |
|-----------------------------------------------------------------------------------|-------------------------------------------------------------------------------|----------|
| [ADR-0061](current/diagnostics/0061-kontrakt-compiler-diagnostic-architecture.md) | Kontrakt Compiler Diagnostic Architecture                                     | Proposed |
| [ADR-0062](current/diagnostics/0062-contract-machine-diagnostic-realization.md)   | Contract-Machine Diagnostic Realization and Operational Evidence Architecture | Proposed |

### Runtime

| ADR                                                                                                                                 | Title                                                                                             | Status   |
|-------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|----------|
| [ADR-0032](current/runtime/0032-capacity-law-resource-policy-resolution-identity-hierarchy-and-zero-residue-semantics.md)           | Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics          | Accepted |
| [ADR-0033](current/runtime/0033-bootstrap-runtime-policy-ratification-storage-governance-and-deferred-platform-aware-autotuning.md) | Bootstrap Runtime Policy Ratification, Storage Governance, and Deferred Platform-Aware Autotuning | Accepted |
| [ADR-0044](current/runtime/0044-unified-runtime-memory-envelop-and-pipeline-lifecycle-governance.md)                                | Unified Runtime Memory Envelope and Pipeline Lifecycle Governance                                 | Draft    |

### Infrastructure

| ADR                                                                                                               | Title                                                                    | Status   |
|-------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------|----------|
| [ADR-0041](current/infrastructure/0041-stable-metadata-identity-blake3-hid-and-protocol-owned-interning.md)       | Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning      | Proposed |
| [ADR-0042](current/infrastructure/0042-mechanical-sympathy-primitive-lifecycle-and-async-ownership-governance.md) | Mechanical Sympathy, Primitive Lifecycle, and Async Ownership Governance | Proposed |

ADR-0041 also has an extracted maintenance edition under the same ADR identity:
[ADR-0041 Extracted Maintenance Edition](current/infrastructure/0041-extracted-maintenance-edition.md).

## Migration

These ADRs are not listed as current category owners while their authority or document ownership is being migrated.

| ADR                                                                                                                            | Title                                                                                              | Status            |
|--------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|-------------------|
| [ADR-0001](migration/0001-adoption-of-hexagonal-architecture.md)                                                               | Adoption of Hexagonal Architecture                                                                 | Migration Pending |
| [ADR-0003](<migration/0003-adoption-of-asynchronous-API-using-coroutines(suspend).md>)                                         | Adoption of Asynchronous API Port Contracts                                                        | Migration Pending |
| [ADR-0013](migration/0013-transition-to-standalone-execution-ecosystem-and-native-ide-integration.md)                          | Transition to Standalone Execution Ecosystem and Native IntelliJ Plugin Development                | Migration Pending |
| [ADR-0024](migration/0024-adoption-of-paranoid-quality-assurance-strategy.md)                                                  | Adoption of Paranoid Quality Assurance Strategy                                                    | Migration Pending |
| [ADR-0030](migration/0030-edge-aware-deterministic-cycle-truncation-strategy.md)                                               | Edge-Aware Deterministic Cycle Truncation Strategy                                                 | Migration Pending |
| [ADR-0038](migration/0038-interface-contract-polymorphic-expansion-and-non-composite-type-expansion.md)                        | Interface Contract Polymorphic Expansion and Non-Composite Type Expansion Completion               | Migration Pending |
| [ADR-0043](migration/0043-contract-graph-canonicalization-sealed-structural-references-and-incremental-identity-derivation.md) | Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation | Migration Pending |
| [ADR-0048](migration/0048-flow-contract-processing-boundary-refinement-and-core-entry.md)                                      | Flow Contract Processing — Boundary Refinement and Core Entry                                      | Migration Pending |
| [ADR-0049](migration/0049-flow-contract-processing-fact-acceptance-and-publication.md)                                         | Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation                   | Migration Pending |
| [ADR-0070](migration/0070-realization-axis-core-realization-closure-and-jvm-ahead-optimization.md)                             | Realization Axis, Core Realization Closure, and JVM-Ahead Optimization                             | Migrated          |

## Historical — Superseded

| ADR                                                                                                       | Title                                                             |
|-----------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|
| [ADR-0010](historical/superseded/0010-strict-circular-reference-detection-strategy.md)                    | Strict Circular Reference Detection Strategy                      |
| [ADR-0011](historical/superseded/0011-enforce-collection-size-constraints.md)                             | Enforce Collection Size Constraints                               |
| [ADR-0019](historical/superseded/0019-adoption-of-intercepter-pattern-for-execution-pipeline-topology.md) | Interceptor Execution Pipeline                                    |
| [ADR-0020](historical/superseded/0020-centralized-execution-and-result-resolution-strategy.md)            | Centralized Execution and Result Resolution                       |
| [ADR-0025](historical/superseded/0025-adoption-of-interface-first-design-and-test-interface-pattern.md)   | Interface-Driven Contract Verification and Test Interface Pattern |
| [ADR-0026](historical/superseded/0026-abstraction-of-type-introspection-and-execution.md)                 | Abstraction of Type Introspection and Execution                   |
| [ADR-0027](historical/superseded/0027-deterministic-cycle-truncation-policy.md)                           | Deterministic Cycle Truncation Policy                             |
| [ADR-0028](historical/superseded/0028-polymorphic-test-subject-injection-via-constructor-types.md)        | Polymorphic Test Subject Injection via Constructor Types          |
| [ADR-0029](historical/superseded/0029-runtime-link-handle-protocol-and-integrity.md)                      | Runtime Link Handle Protocol and Integrity                        |

## Historical — Deprecated

| ADR                                                                                                                     | Title                                                       |
|-------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------|
| [ADR-0002](historical/deprecated/0002-adoption-of-domain-driven-design.md)                                              | Adoption of Domain-Driven Design                            |
| [ADR-0004](historical/deprecated/0004-definition-of-the-test-execution-status-state-machine.md)                         | Definition of the Test Execution Status State Machine       |
| [ADR-0005](historical/deprecated/0005-adoption-of-dual-strategy-for-test-automation.md)                                 | Adoption of Dual Strategy for Test Automation               |
| [ADR-0006](historical/deprecated/0006-adoption-of-scoped-discovery-mechanism.md)                                        | Adoption of Scoped Discovery Mechanism                      |
| [ADR-0007](historical/deprecated/0007-adoption-of-real-object-first-dependency-injection-strategy.md)                   | Adoption of Real Object First Dependency Injection Strategy |
| [ADR-0008](historical/deprecated/0008-adoption-of-generative-fallback-for-stateless-mocks.md)                           | Adoption of Generative Fallback for Stateless Mocks         |
| [ADR-0009](historical/deprecated/0009-dual-layer-contract-verification-strategy.md)                                     | Dual-Layer Contract Verification Strategy                   |
| [ADR-0012](historical/deprecated/0012-adoption-of-test-scoped-dependency-caching.md)                                    | Test-Scoped Dependency Caching                              |
| [ADR-0014](historical/deprecated/0014-asynchronous-event-driven-reporting-architecture.md)                              | Asynchronous Event-Driven Reporting Architecture            |
| [ADR-0015](historical/deprecated/0015-reporting-output-strategy-and-artifact-management.md)                             | Reporting Output Strategy and Artifact Management           |
| [ADR-0016](historical/deprecated/0016-test-synthesis-audit-and-user-control-surface.md)                                 | Test Synthesis Audit and User Control Surface               |
| [ADR-0017](historical/deprecated/0017-worker-based-synchronous-audit-journaling-architecture.md)                        | Worker-Based Synchronous Audit Journaling Architecture      |
| [ADR-0018](historical/deprecated/0018-hybrid-event-stream-architecture-and-optimization-strategy.md)                    | Hybrid Event-Stream and Audit Optimization                  |
| [ADR-0021](historical/deprecated/0021-adoption-of-zero-config-hybrid-journaling-strategy-for-cloud-native-execution.md) | Zero-Config Hybrid Journaling                               |
| [ADR-0022](historical/deprecated/0022-mvp-scope-definition-and-execution-strategy.md)                                   | MVP Scope Definition and Execution Strategy                 |
| [ADR-0023](historical/deprecated/0023-hybrid-auditing-strategy-for-execution-data-collection.md)                        | Hybrid Auditing Strategy for Execution Data Collection      |

No ADRs are currently classified as Rejected or Withdrawn.

## Registry

The registry covers issued ADR identities through ADR-0072.

ADR-0041's extracted maintenance edition is part of ADR-0041 and does not create another ADR identity.