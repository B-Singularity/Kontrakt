# 🛡️ Kontrakt Quality Assurance Strategy

> **"We do not write tests to find bugs. We write tests to prove the mathematical correctness of the framework."**

- **Framework Version:** 0.5.0-SNAPSHOT
- **Last Updated:** 2026-01-20
- **Status:** Active

---

This document outlines the testing standards, tools, and methodologies required to maintain the **World-Class
Reliability** of the Kontrakt Framework.

## 1. 🎯 The "95%" Standard

We enforce a strict **Branch Coverage** policy. Line coverage is insufficient for complex logic; we must verify every
decision path.

| Metric              | Threshold       | Enforcement                              |
|:--------------------|:----------------|:-----------------------------------------|
| **Branch Coverage** | **Target: 95%** | **Build Failure** (Automated via Gradle) |
| **Line Coverage**   | N/A             | Monitored, but not enforced              |
| **Mutation Score**  | **High**        | Verified via Pitest (Periodic)           |

### ⚠️ The Ratcheting Rule (Progressive Enforcement)

Since we are in the initial development phase, coverage thresholds are managed via a **Ratcheting Strategy**:

1. **Start:** The baseline is set to `0.00` to establish the CI pipeline.
2. **Lock:** As tests are written, the threshold is raised to match the current coverage (e.g., `0.00` → `0.50` →
   `0.80`).
3. **No Regression:** We never lower the standard. Once a threshold is set, dropping below it fails the build.
4. **Goal:** The ultimate requirement for v1.0 release is **0.95 (95%)**.

---

## 2. 🛠️ The Arsenal (Tech Stack)

We use a modern, Kotlin-first testing stack to ensure expressiveness and precision.

- **[JUnit 5](https://junit.org/junit5/)**: The execution engine.
- **[MockK](https://mockk.io/)**: Kotlin-native mocking. **Do not use Mockito** for internal Kotlin code (Mockito is
  strictly for legacy support adaptation).
- **[AssertJ](https://assertj.github.io/doc/)**: Fluent assertions. Use `assertThat(actual).isEqualTo(expected)` instead
  of standard JUnit assertions.
- **[JaCoCo](https://www.eclemma.org/jacoco/)**: Coverage analysis and enforcement.
- **[Pitest](https://pitest.org/)**: Mutation testing (Fault injection).

---

## 3. 🧬 Testing Tactics by Layer

### A. Core Domain (Generators & Logic)

* **Goal:** 100% Determinism & High Coverage.
* **Technique:**
    * **Property-Based Testing:** Verify that invariants hold true for a wide range of inputs.
    * **Seed Control:** All random generation **must** be reproducible via a specific `seed`.
    * **Recursive Comparison:** Use `assertThat(obj).usingRecursiveComparison()` to verify deep object equality.

### B. Discovery & Reflection

* **Goal:** Branch Coverage (Handling edge cases).
* **Technique:**
    * Simulate various class structures (Interfaces, Abstract classes, Data classes).
    * Test "Missing Annotation" and "Invalid Configuration" scenarios explicitly.

### C. Adapters & Infrastructure (The "Humble Object")

* **Goal:** Integration Verification.
* **Policy:** **Excluded from strict Branch Coverage enforcement.**
* **Reasoning:** Adapters (e.g., `ClassGraphAdapter`, `JUnitEngineAdapter`) implement the **Humble Object Pattern**.
  They should contain **zero logic** and strictly wire internal components to external libraries.
* **Verification:** Validated via **Integration Tests** or **Smoke Tests**, not Unit Tests.

---

## 4. 🧟 Mutation Testing (The "Paranoid" Check)

Coverage proves code was executed, but it doesn't prove the code is *correct*.
We use **Pitest** to inject "mutants" (artificial bugs) into the code to ensure tests are robust.

* **Process:** Pitest changes logic (e.g., `if (a > b)` → `if (a >= b)`) and runs the tests.
* **Success:** The test suite **fails** (The mutant is killed).
* **Failure:** The test suite passes (The mutant survived).
* **Action:** If a mutant survives, the test case is weak and must be improved.

To run mutation tests:

```bash
./gradlew pitest -Pmutation
```

---

## 5. 📝 Developer Guidelines

### 1. Naming Conventions

Use Kotlin's backtick syntax for descriptive test names.

* **Bad:** `testGenerate()`
* **Good:** `` `should generate identical objects when the same seed is provided` ``

### 2. Determinism is Law

Any test involving `FixtureGenerator` must be deterministic.

```kotlin
// ✅ Correct
val context = GenerationContext(seed = 12345L)

// ❌ Forbidden
val context = GenerationContext(seed = System.nanoTime()) // Flaky!
```

### 3. Assertions

Avoid standard JUnit assertions (`assertEquals`). Use **AssertJ** for better readability and error messages.

```kotlin
// ✅ Correct
assertThat(result.items).hasSize(5).contains("Target")

// ❌ Avoid
assertEquals(5, result.items.size)
```

---

## 6. 🚀 CI Pipeline Workflow

Every Pull Request triggers the following pipeline:

1. **Ktlint Check**: Enforces code style.
2. **Unit Tests**: Runs all tests in parallel.
3. **JaCoCo Verification**: Fails if coverage drops below the current **ratcheted threshold**.
4. **Build**: Packages the JAR only if all above steps pass.