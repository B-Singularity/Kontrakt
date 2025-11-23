# ADR-005: Adoption of a Unified Two-Pillar Strategy for Test Automation

**Status:** Accepted

**Date:** 2025-11-23

## Context

The primary goal of the `Kontrakt` framework is to achieve "Zero Boilerplate" testing. Previously, we considered
segregating testing strategies based on class types (e.g., VO specific annotations).

However, contracts exist everywhere. A "Rich Interface" defines behavioral contracts through parameters. A "Value
Object" or "Entity" defines data contracts through fields and constructors. Furthermore, all data-holding objects must
adhere to structural rules like `equals`/`hashCode` consistency and constructor safety.

## Decision

We will adopt a **Unified Two-Pillar Strategy** driven by a **Single Annotation (`@Contract`)**.

The framework applies a **"Constraint-First & Structure-Aware"** approach. It verifies explicit user-defined constraints
everywhere and enforces implicit structural rules for data objects.

### Pillar 1: Automated Integrity Verification (Implicit Mode)

* **Target:** **ANY** class or interface marked with `@Contract` (Interfaces, VOs, Entities, DTOs).
* **Mechanism:** The **Stateless Constraint Engine** performs two layers of verification:

  #### Layer A: Explicit Constraint Fuzzing (The Business Contract)
    * **Trigger:** Presence of constraint annotations (e.g., `@IntRange`, `@Pattern`, `@Length`) on any Parameter,
      Field, or Constructor Argument.
    * **Action:**
        * **Validity Check:** Generates random valid inputs and asserts the method/constructor accepts them.
        * **Boundary Check:** Injects edge cases (Min/Max values) to verify robustness.
        * **Violation Check:** Injects invalid inputs (e.g., `age = -1`) and asserts that the code throws a domain
          exception (e.g., `IllegalArgumentException`), not a system error (e.g., `NPE`).

  #### Layer B: Structural Compliance (The Code Contract)
    * **Trigger:** The target is identified as a Data Container (Data Class, Entity, Record).
    * **Action:**
        * **Equality Contract:** Verifies reflexive, symmetric, transitive, and consistent properties of `equals()` and
          `hashCode()`.
        * **Constructor Safety:** Fuzzes the constructor with `null`s (for non-null types) and default type values to
          ensure the object cannot be instantiated in an invalid state.
        * **Immutability Check:** (Optional) Verifies that fields in Value Objects are not modified unexpectedly.

* **User Effort:** Just adding `@Contract` and standard constraint annotations. No test code.

### Pillar 2: Zero-Setup Logic Verification (Explicit Mode)

* **Target:** Complex Business Logic (Services, Use Cases) requiring behavioral verification.
* **Mechanism:** Users write a separate **Spec File** (e.g., `RecruitServiceSpec.kt`) containing only assertion logic.
* **`Kontrakt`'s Role (The Assistant):**
    * **Micro-DI:** Automatically analyzes constructors and wires dependencies.
    * **Semantic Mocking:** Automatically injects **Stateful Fakes** (Maps) for Repositories and **Stateless Mocks** for
      services.
    * **Ephemeral Context:** Ensures perfect isolation by creating and discarding the environment per scenario.
* **User Effort:** Users focus solely on the **Behavior Verification** (The "Why" and "How" of the logic) using simple
  assertions.

## Consequences

### Positive

* **Rich Interfaces & Domains:** Encourages developers to make their interfaces and entities "Rich" by adding
  constraints directly to them. The test coverage comes for free.
* **Universal Protection:** Whether it's a controller input (DTO), a database entity, or a service interface, `Kontrakt`
  guards the boundaries automatically.
* **Standard Compliance:** Eliminates the common bugs where `equals()` is implemented incorrectly or constructors allow
  invalid nulls.

### Negative

* **Execution Time:** Validating `equals` contracts and fuzzing constructors for every entity can be CPU-intensive. We
  need to implement a "Smart Sampling" strategy to maintain speed during local development.
* **False Positives:** Some legacy entities might rely on "invalid state" temporarily (e.g., setters called after
  construction). The framework might break these patterns, forcing a refactor to safer designs (which is ultimately
  good, but painful).