# ADR-009: Adoption of Dual-Layer Contract Verification Strategy

**Status:** Accepted

**Date:** 2025-12-13

## Context

Ensuring domain object integrity and adherence to interface contracts is critical for system reliability. However,
manually writing unit tests to verify every field constraint (e.g., `@Email`, `@Positive`, `@NotNull`) and return value
specification is repetitive, error-prone, and reduces developer productivity.

Traditional testing often focuses on business logic but overlooks strict boundary checks on **Input** (Construction) or
adherence to interface contracts on **Output** (Return values). Furthermore, indiscriminately applying strict
constructor verification (fuzzing) to service-layer objects can lead to "False Positives" where dependency injection
parameters are incorrectly fuzzed, causing irrelevant test failures.

To maintain the **"Zero Boilerplate"** philosophy while ensuring practical stability, we need an automated mechanism
driven by declarative annotations that distinguishes between "State" and "Behavior."

## Decision

We adopt a **"Dual-Layer Contract Verification"** strategy with **Selective Application Logic**, separating testing
responsibilities into "Input Defense" and "Output Compliance."

### 1. Input Defense (Constructor Compliance)

* **Primary Component:** `ConstructorComplianceExecutor`
* **Goal:** To verify that the object correctly **rejects** malformed data (e.g., by throwing exceptions) to maintain
  internal invariants.
* **Strategy:** It utilizes `FixtureGenerator` to intentionally inject **Invalid Values** (fuzzing) into constructors.

### 2. Output Compliance (Return Value Verification)

* **Primary Component:** `ScenarioExecutor`
* **Goal:** To validate that the **Return Value** of a method execution strictly adheres to the annotations defined on
  the interface (e.g., `@Email`, `@Positive`).
* **Strategy:** It executes interface methods using valid inputs and validates the result using `ContractValidator`.

### 3. Selective Verification Strategy (The "On/Off" Logic)

We distinguish between Data Objects and Behavioral Components to apply constructor verification appropriately:

| Annotation Type     | Target Component                          | Verification Mode           | Rationale                                                                                                                                                                    |
|:--------------------|:------------------------------------------|:----------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`@DataContract`** | **DTO, Entity, VO** (State-centric)       | **Mandatory (Always On)**   | Data objects must maintain integrity from the moment of instantiation. Invalid state is never acceptable.                                                                    |
| **`@Contract`**     | **Service, Component** (Behavior-centric) | **Optional (Default: Off)** | Service constructors often receive dependencies (DI). Fuzzing dependencies creates noise. Verification can be enabled explicitly via `@Contract(verifyConstructors = true)`. |

### 4. Separation of Concerns

* **Data Supply:** `FixtureGenerator` is solely responsible for generating data (both valid and invalid) and contains no
  validation logic.
* **Rule Enforcement:** `ContractValidator` serves as the central rule engine. It is used internally by Domain
  Entities (for self-defense) and externally by the Executor (for compliance checks).

## Consequences

### Positive

* **Zero-Boilerplate Testing:** Comprehensive coverage for data integrity is achieved solely through declarative
  annotations.
* **Reduced Noise:** By defaulting constructor verification to **Off** for `@Contract` (Services), we avoid false
  positives caused by fuzzing Mock dependencies or configuration values.
* **Two-Way Reliability:** Guarantees safety at both the entry point (Construction) and exit point (Return) of the
  domain model.
* **Explicit Intent:** The distinction between `@DataContract` and `@Contract` clarifies whether a class is a pure data
  carrier or a behavioral component.

### Negative

* **Cognitive Load:** Developers must understand the semantic difference between `@DataContract` (Strict) and
  `@Contract` (Flexible) to use them correctly.
* **Performance Overhead:** The heavy reliance on reflection for dynamic instantiation and method invocation may
  increase test execution time compared to static hard-coded assertions.