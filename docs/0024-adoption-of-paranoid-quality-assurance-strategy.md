# 0024. Adoption of Paranoid Quality Assurance Strategy

* **Status**: Accepted
* **Date**: 2026-01-20
* **Context**: Testing Framework Reliability and "World-Class" Quality Standards

## Context

The **Kontrakt** framework aims to replace established testing ecosystems like JUnit and Spock. As a foundational
infrastructure tool, its reliability requirements are significantly higher than those of typical business applications.

1. **The Trust Deficit**: If a testing framework has bugs (e.g., false positives, missed assertions, or flaky
   execution), users lose trust immediately.
2. **The Bootstrapping Paradox**: To build a testing framework, we need a testing framework. We cannot initially use
   Kontrakt to test Kontrakt without risking circular logic errors.
3. **Complexity of Generative Logic**: The core `FixtureGenerator` and `InterceptorChain` involve complex recursion,
   reflection, and state management. Standard "Happy Path" testing is insufficient to guarantee stability.

Therefore, we need a Quality Assurance strategy that goes beyond standard industry practices—a "Paranoid" approach.

## Decision

We will adopt a **"Paranoid Quality Assurance Strategy"** with a **Tiered Execution Model** to balance reliability and
developer velocity.

### 1. The Bootstrapping Mandate

Until the release of **v1.0.0 (Self-Hosting)**, we will utilize the **JUnit 5 Platform** as our bootstrap loader.

* **Runner**: JUnit 5 Jupiter.
* **Assertions**: AssertJ (for fluent, deep introspection).
* **Mocking**: MockK (for Kotlin-native verification).
* **Objective**: Once v1.0 is stable, we will migrate the project's own tests to Kontrakt (Dogfooding).

### 2. Strict Coverage Thresholds

We enforce hard limits on code coverage to prevent regression.

* **Metric**: **Branch Coverage** (not just Line Coverage).
* **Threshold**: **Minimum 95%**.
* **Enforcement**: The CI build pipeline **MUST fail** if coverage drops below this threshold. No exceptions are allowed
  for core domains (`execution`, `discovery`).

### 3. Targeted Mutation Testing

To ensure test quality without excessive build times, we apply Mutation Testing (Pitest) strategically.

* **Scope**: Strictly enforced on **Core Domains** (`execution`, `discovery`, `generation`).
* **Exclusion**: Adapters and Reporting layers are exempted unless critical logic resides there.
* **Success Criteria**: The test suite must kill at least 80% of mutants in the core scope.

### 4. Tiered Generative Verification (Tiered CI)

To mitigate the cost of repetitive generative testing (`FixtureGenerator` reliability), we separate validation into two
tiers using System Properties (e.g., `kontrakt.profile`).

* **Tier 1 (Local/PR):** Quick verification (e.g., **100 repetitions**) to catch obvious regressions immediately. This
  allows for rapid feedback cycles during development.
* **Tier 2 (Nightly/Release):** Deep verification (e.g., **10,000 repetitions**) running on a scheduled pipeline (or via
  `./gradlew stressTest`) to detect rare edge cases and race conditions.

### 5. Meta-Testing (Testing the Failure)

We must verify that the framework correctly reports failures.

* **Strategy**: We will maintain a suite of "Intentionally Failing Scenarios."
* **Assertion**: We verify that `TestResult` status is `FAILED` and that the `AssertionRecord` contains the correct
  error message and mined source coordinates (`File:Line`).

### 6. Mandatory Meta-Verification of Contract Definitions

The framework relies on the premise that `@Contract` and `@DataContract` definitions are the source of truth. Therefore,
we must rigorously test the **validity of the contracts themselves**.

* **Principle**: "Garbage In, Error Out." The framework must never attempt to verify a logical impossibility.
* **Requirement**: We must maintain a suite of **"Invalid Contract Scenarios"** (e.g., `Range(min=10, max=5)`,
  `Size(min=-1)`).
* **Verification**: The test suite must ensure that the `ContractConfigurationValidator` correctly identifies these
  invalid definitions and throws a `KontraktConfigurationException` *before* execution begins.

## Consequences

* **Managed Build Time**: By scoping Pitest and segmenting generative tests, we keep PR build times reasonable while
  maintaining rigorous nightly checks.
* **Reproducibility**: Developers can reproduce Nightly CI failures locally by running the specific `stressTest` task.
* **Core Stability**: The most critical parts of the framework are mathematically proven to be robust via extensive
  repetition.

## Compliance

This decision is effective immediately. All subsequent Pull Requests must include tests that satisfy the 95% branch
coverage rule, and the `docs/quality/MASTER_PLAN.md` document will serve as the execution guide for this strategy.