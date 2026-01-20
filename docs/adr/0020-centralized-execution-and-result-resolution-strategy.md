# ADR-020: Centralized Execution and Result Resolution Strategy

* **Status:** Accepted
* **Date:** 2026-01-08
* **Context:** Execution Engine & Error Handling
* **Relies on:** [ADR-019] Interceptor Pattern

## 1. Context & Problem Statement

The `Kontrakt` framework differs significantly from traditional testing libraries (like JUnit or Kotest) because it
operates as a **Generative Framework**. Before a test method can even be invoked, the framework must perform complex
operations: scanning the classpath, resolving dependency graphs, and synthesizing recursive object fixtures.

This "Heavy Setup" nature introduces specific challenges regarding error handling and result reporting:

1. **Ambiguity of Failure (The "Blame" Problem):**
   When an exception occurs, it is difficult for the user to distinguish between a **"Setup Failure"** (User
   configuration error) and an actual **"Execution Failure"** (Business logic error). Furthermore, unexpected crashes
   within the framework (e.g., NPE in the Generator) often look like user errors, leading to confusion.

2. **Stack Trace Pollution:**
   The adoption of the **Interceptor Pattern [ADR-019]** and extensive use of **Reflection** means that raw stack traces
   are flooded with internal framework calls (`java.lang.reflect.*`, `execution.domain.interceptor.*`). This "Noise"
   makes it frustrating for users to find their own code in the logs.

3. **Scattered Exception Logic:**
   Currently, `try-catch` blocks are scattered across the `TestInstanceFactory`, `FixtureGenerator`, and
   `DefaultScenarioExecutor`. This leads to inconsistent error reporting and coordinate resolution.

4. **Missing Source Coordinates:**
   Since `Kontrakt` invokes tests dynamically, the IDE often loses the link to the original source code. We need a
   robust mechanism to identify exactly **where** (File and Line Number) an error occurred.

We need a unified strategy that strictly separates **Setup** from **Execution**, centralizes result processing, assigns
blame correctly (User vs. Framework), and sanitizes output for a better Developer Experience (DX).

## 2. Decision

We will adopt a **"Phased Responsibility & Centralized Resolution"** strategy. This includes a strict lifecycle split, a
smart interceptor for result normalization, and a dedicated stack trace filtering policy.

### 2.1. Strict Separation of Lifecycle Phases

We will bifurcate the `TestExecution` flow into two strictly isolated phases with different error handling policies.

* **Phase 1: Setup (The Creation Phase)**
    * **Scope:** Includes `TestInstanceFactory`, `FixtureGenerator`, and `DependencyInjection`.
    * **Responsibility:** The `KontraktTestEngine` (Container).
    * **Failure Policy:** Any exception escaping this phase is fatal. The test method is **never executed**.
    * **Outcome:** Reporting marks the test as `Setup Failed` pointing to the class definition.

* **Phase 2: Execution (The Runtime Phase)**
    * **Scope:** Includes the `Interceptor Chain`, `Scenario Context`, and `Target Method Invocation`.
    * **Responsibility:** The `TestScenarioExecutor` and `Interceptors`.
    * **Failure Policy:** Exceptions here are considered **Test Failures** (Logic/Assertion).
    * **Outcome:** The exception is caught, analyzed, and wrapped in a `TestResult`.

### 2.2. Automated Blame Assignment Policy

We will implement logic to automatically categorize failures based on the exception type and origin.

| Category                       | Definition                      | Identifiers / Logic                                                              | Outcome                                          |
|:-------------------------------|:--------------------------------|:---------------------------------------------------------------------------------|:-------------------------------------------------|
| **Setup Failure** (User)       | Invalid usage or configuration. | `KontraktConfigurationException`, `CircularDependencyException`.                 | Report as **Setup Failed**. Show hint.           |
| **Assertion Failure** (User)   | Business logic mismatch.        | `AssertionError`, `ContractViolationException`.                                  | Report as **Failed**. Show Expected/Actual.      |
| **Internal Error** (Framework) | Framework bug or crash.         | Unhandled `RuntimeException` (NPE, OOB) originating from `execution.*` packages. | Report as **Internal Error**. Ask to report bug. |

### 2.3. Adoption of `ResultResolverInterceptor`

We will introduce a specialized interceptor, the `ResultResolverInterceptor`, placed at the **top** of the execution
chain.

* **Role:** The "Central Brain" for result processing. It is the only component permitted to catch unhandled exceptions
  during Phase 2.
* **Coordinate Mining:**
    * **On Failure:** Utilizes `ExceptionUtils` to analyze the stack trace and extract the exact `FileName` and
      `LineNumber` of the crash.
    * **On Success:** Applies a "Lazy" strategy. Injects basic metadata (Class/Method name) to optimize performance,
      only calculating reflection-based lines if `TraceMode` is enabled.

### 2.4. Smart Stack Trace Filtering Strategy

To address "Stack Trace Pollution," the Reporting Context will apply a filtering layer before displaying errors to the
user.

* **Filtering Logic:** The framework will programmatically filter out stack trace elements belonging to:
    * `execution.domain.*` (Internal Interceptors)
    * `java.lang.reflect.*` / `jdk.internal.*` (Reflection overhead)
    * `org.junit.*` (Runner infrastructure)
* **Visibility Rules:**
    * **Default:** Show only User Code and the immediate Exception cause.
    * **Verbose/Debug Mode:** Show the full raw stack trace for debugging framework issues.

### 2.5. Internal "Swallow & Retry" Policy

To protect the Generative Fallback logic:

* **Internal Components (Generators):** MUST handle recoverable exceptions internally. They must **never** throw an
  exception unless all fallback strategies (e.g., Real -> Mock) have been exhausted.
* **Interceptors:** MUST NOT catch exceptions unless implementing specific logic like Retries.

## 3. Consequences (Trade-off Analysis)

### 3.1. Positive Consequences (Benefits)

* **Explicit Blame:** Users no longer waste time debugging the framework when their configuration is wrong, and vice
  versa.
* **Clean Logs:** Stack trace filtering removes 80%+ of noise, making the console output readable and actionable
  immediately.
* **Deep IDE Integration:** Centralized coordinate mining ensures every error provides a clickable link to the exact
  line of code.
* **Architectural Hygiene:** The `DefaultScenarioExecutor` remains stateless and pure, delegating complex error handling
  to the infrastructure layer.

### 3.2. Negative Consequences (Risks & Costs)

* **Debugging Framework Bugs:** Filtering stack traces makes it harder to debug the framework itself. Developers must
  remember to enable `Verbose Mode` when working on `Kontrakt` internals.
* **Risk of Masking:** If the "Blame Assignment" logic is flawed, a legitimate framework bug might be reported as a user
  error, confusing the developer.
* **Performance Overhead:** Analyzing and filtering stack traces is CPU-intensive. However, this only occurs on
  *failure*, which is an acceptable cost.

## 4. Implementation Strategy

1. **Refactor Engine:** Split `KontraktTestEngine` execution logic into strictly distinct Phase 1 and Phase 2 blocks.
2. **Implement Interceptor:** Create `ResultResolverInterceptor` to handle Phase 2 exceptions and perform "Coordinate
   Mining."
3. **Enhance ExceptionUtils:** Add `filterStackTrace(Throwable)` functionality to strip internal packages based on
   `UserControlOptions`.
4. **Update Reporting:** Ensure the "Failure Card" renderer uses the Blame Assignment logic to display appropriate
   headers (e.g., "💥 Internal Error" vs "❌ Test Failed").