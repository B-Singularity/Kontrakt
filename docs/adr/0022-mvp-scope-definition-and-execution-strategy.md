# ADR-022: MVP Scope Definition and Execution Strategy

* **Status:** Accepted
* **Date:** 2026-01-13
* **Deciders:** Core Team, Architect
* **Technical Context:** `DefaultScenarioExecutor`, `FixtureGenerator`

## 1. Context

As we build the MVP (Minimum Viable Product) for the **Kontrakt** framework, we face a critical trade-off between *
*Feature Completeness** (handling all edge cases, complex fuzzing) and **Deterministic Stability** (reproducibility,
speed, debuggability).

Features like probabilistic parameter injection (coin-toss), infinite fuzzing loops, or complex object graph
resolution (auto-mocking) can significantly increase architectural complexity and reduce the reproducibility of test
failures.

We need to define strict boundaries and simplification strategies to ensure the framework remains lightweight,
predictable, and strictly isolated.

## 2. Decisions

We adopt a **"Simplification Strategy"** that prioritizes reproducibility over exhaustive coverage for the MVP.

### A. Parameter Generation: "Default-First" Strategy

* **Decision:** For parameters declared with **Kotlin Default Arguments** (Optional Parameters), the `FixtureGenerator`
  will **SKIP** generation and rely on the language's default value.
* **Rationale:**
    * We rejected the "Coin-Flip" strategy (randomly choosing between default vs. generated value) because it introduces
      non-determinism within the same seed.
    * If a user provides a default value, we assume it represents the primary use case to be verified.
* **Future Scope:** A `GenerationStrategy` interface will be introduced later to allow users to opt-in to
  `ForceFuzzOptionals`.

### B. UserScenario Verdict: "Implicit Success"

* **Decision:** In `UserScenario` mode (`@Test`), if a method executes to completion without throwing an exception, it
  is recorded as **`PASSED`**, even without explicit assertion DSL usage.
* **Rationale:**
    * Aligns with the *de facto* standard of xUnit frameworks (JUnit, etc.).
    * Supports "Smoke Testing" scenarios where the goal is ensuring the code does not crash.

### C. Fuzzing Loop: "Delegated Responsibility"

* **Decision:** The `ContractAuto` mode executes **exactly once** per provided Seed. The Executor does not contain
  internal iteration loops.
* **Rationale:**
    * **Separation of Concerns:** Iteration/Repetition is the responsibility of the upper-layer Runner or CI
      environment, not the core Executor.
    * **1 Seed = 1 Run:** This ensures strictly deterministic behavior, making bug reproduction trivial compared to
      debugging a failure in the Nth iteration of a loop.

### D. Asynchronous Support: "Blocking Adaptation"

* **Decision:** We support Kotlin `suspend` functions by executing them via **`runBlocking`** within the worker thread.
* **Rationale:**
    * Leveraging our **Worker-Based Isolation (ADR-017)**, blocking a single worker thread is safe and does not affect
      the overall system throughput.
    * This enables testing of modern asynchronous business logic (e.g., Ktor, Spring WebFlux) without complicating the
      synchronous state machine of the Executor.

## 3. Technical Constraints & Intentional Limitations

To maintain architectural purity, we explicitly define the limitations of the current implementation.

### 1. Circular Dependencies: "Defensive Fail-Fast"

* **Status:** Limited Support (Defensive)
* **Implementation Details:**
    * The `FixtureGenerator` tracks the visitation history of the object graph.
    * **Nullable Cycles (`A -> B -> A?`):** Resolved by injecting `null` to break the cycle.
    * **Non-Nullable Cycles (`A -> B -> A`):** The generator throws a `RecursiveGenerationFailedException` immediately.
* **Limitation:** We do not support complex graph resolution strategies (e.g., Proxy injection or delayed setting) for
  strong cycles. Users should design DTOs as DAGs (Directed Acyclic Graphs).

### 2. Interfaces & Abstract Classes: "Explicit Contract Only"

* **Status:** **Supported (Conditional)**
* **Implementation Details:**
    * **Raw Interfaces:** Generating raw interfaces or abstract classes without metadata is **Unsupported** (throws
      exception).
    * **Annotated Interfaces:** If an interface is annotated with **`@Contract(implementingClass = ...)`**, the
      generator will resolve and instantiate the specified concrete class.
* **Limitation:** We do not include an implicit Auto-Mocking engine (like Mockito) in the MVP. All interface
  dependencies must be explicitly mapped to a concrete implementation via the `@Contract` annotation.

### 3. Lifecycle Hooks: "Pure Isolation"

* **Status:** Unsupported
* **Implementation Details:**
    * No mechanisms for `@Before`, `@After`, `Setup`, or `Teardown`.
* **Limitation:** All tests must be independent. We discourage shared mutable state patterns that rely on lifecycle
  callbacks.

### 4. Execution Order: "Non-Deterministic"

* **Status:** Not Guaranteed
* **Implementation Details:**
    * Test execution order depends on JVM reflection and is not guaranteed.
* **Limitation:** We explicitly avoid supporting ordering annotations (`@Order`) to prevent hidden state dependencies
  between tests.

## 4. Consequences

### Positive

* **High Reproducibility:** A specific Seed guarantees the exact same execution path, parameter values, and result every
  time.
* **Simple Debugging:** "Single-Pass" execution eliminates the noise of loop-based failures.
* **Flexibility:** Users can test interface-based designs by simply providing a `@Contract` mapping, preserving
  decoupling while enabling fuzzing.

### Negative

* **Coverage Gaps:** Bugs that only appear when optional parameters are explicitly set to non-default values will not be
  detected by default.
* **Boilerplate:** Testing interfaces requires adding `@Contract` annotations, which creates a slight coupling between
  the interface and its test implementation details (though this is often acceptable in DTO-heavy codebases).