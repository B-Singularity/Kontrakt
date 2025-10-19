# ADR-005: Adoption of a Three-Pillar Strategy for Test Automation

**Status:** Accepted

**Date:** 2025-10-19

## Context

The primary goal of the `Kontrakt` framework is to automate unit testing and reduce boilerplate. The "Test Oracle Problem" and the repetitive nature of writing tests are the main challenges to overcome. A single approach to automation is insufficient. We need a multi-faceted strategy that provides different levels of automation for different kinds of testing needs.

## Decision

We will adopt a **three-pillar strategy** for test automation, combining fully automated contract/validation testing with developer-assisted behavior testing.

### 1. Fully Automated Boundary Testing (for Interface Preconditions)

- **Mechanism:** Users declare constraints on `Contract` interface methods using annotations (e.g., `@IntRange`, `@NotEmpty`).
- **`Kontrakt`'s Role:** The framework reads these annotations and **automatically generates, executes, and asserts** test cases for boundary conditions.
- **User Effort:** Zero test code required.

### 2. Assisted Behavior Testing (for Complex Business Logic)

- **Mechanism:** For complex logic where the expected outcome is non-trivial, users write test methods in a separate `src/test` file, annotating the test class with `@KontraktFor` and the method with `@KontraktTest`.
- **`Kontrakt`'s Role:** The framework handles the entire **Arrange** phase, automatically instantiating the Test Target and all its mock dependencies, then injecting them into the user's test method.
- **User's Role:** The developer focuses only on the **Act** and **Assert** phases.
- **User Effort:** Minimal; only the core verification logic is written.

### 3. Fully Automated Value Object (VO) Contract Testing

- **Mechanism:** Users annotate their Value Object classes (e.g., `data class`es) with a `@ValueObjectContract` annotation.
- **`Kontrakt`'s Role:** The framework reads this annotation and **automatically generates and runs** a standard suite of contract tests for the VO. This includes:
    - Verifying the `equals()` and `hashCode()` contract.
    - Verifying constructor validation by calling factory methods with invalid inputs (e.g., blank strings, out-of-range numbers) and asserting that they fail correctly.
- **User Effort:** A single annotation. Zero test code is required.

## Consequences

### Positive
- **Comprehensive Coverage:** This three-pillar strategy addresses the most common and tedious aspects of unit testing: boundary conditions, VO correctness, and complex logic setup.
- **Exceptional Developer Experience (DX):** Developers are freed from writing repetitive, boilerplate tests, allowing them to focus on the truly unique and valuable aspects of their business logic.
- **Strong Value Proposition:** The combination of these three automation strategies gives `Kontrakt` a unique and highly competitive position among testing frameworks.

### Negative
- **Increased Framework Complexity:** The framework's internal engine must now support three distinct modes of test discovery, generation, and execution.
- **Documentation Challenge:** We must clearly document the three pillars and provide clear guidance to users on when and how to use each approach effectively.