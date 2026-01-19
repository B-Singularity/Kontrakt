# ADR-023: Hybrid Auditing Strategy for Execution Data Collection

* **Status:** Accepted
* **Date:** 2026-01-17
* **Authors:** Core Framework Team
* **Tags:** #Observability #Reliability #Architecture #Safety #CrashForensics

## 1. Context

To ensure the reliability and reproducibility of the `Kontrakt` framework, it is critical to capture **"what arguments
were used"** for every test execution. During the refactoring process, we encountered a conflict between two
architectural approaches regarding how this data should be collected.

### 1.1 The Conflict

1. **Option A: Side-Effect Tracing**
    * **Mechanism:** The `Executor` records arguments into a shared `ScenarioTrace` object during execution.
    * **Issue:** It relies on an **implicit contract**. If an implementation forgets to call `trace.record()`, the logs
      remain empty without raising any compilation errors (Silent Failure).

2. **Option B: Explicit Return**
    * **Mechanism:** The `Executor` returns an `ExecutionResult` object containing both records and arguments after
      execution.
    * **Issue:** It creates a **Crash Blind Spot**. If the JVM crashes (e.g., `StackOverflowError`, `OutOfMemoryError`)
      or an unhandled exception occurs during execution, the function never returns. Consequently, the forensic data
      regarding *what input caused the crash* is lost.

### 1.2 The Consistency Gap

Currently, `UserScenario` and `ContractAuto` modes capture arguments, but `DataCompliance` mode only returns a list of
`AssertionRecord`. This inconsistency makes it impossible to reproduce failures in data compliance tests, creating a gap
in the system's auditability.

---

## 2. Decision

We have decided to adopt a **Hybrid Auditing Architecture** that combines the strengths of both approaches. We
prioritize **Operational Certainty** (Crash Safety) over purely minimal code.

### 2.1 Dual Recording Mechanism

Data will be captured at two distinct phases:

1. **Pre-Execution Recording (Trace as "Intent"):**
    * The `Executor` must record generated arguments into the `ScenarioTrace` **immediately before** invoking the test
      method.
    * **Purpose:** To serve as a "Flight Recorder" or "Last Will," ensuring forensic data is preserved even if the
      execution crashes.

2. **Post-Execution Return (Result as "Fact"):**
    * The `Executor` must return an `ExecutionResult` (and `DataComplianceResult`) containing the arguments upon
      completion.
    * **Purpose:** To enforce an explicit contract via the type system and provide a clean data source for standard
      reporting.

### 2.2 Source of Truth Hierarchy

Since data exists in two places, we define the authoritative source to avoid ambiguity:

* **Primary Source (ExecutionResult):** If the execution completes (Success or Checked Failure), the returned result is
  the **absolute truth**.
* **Fallback Source (ScenarioTrace):** The Trace data is used **only** when an `ExecutionResult` is unavailable (e.g.,
  during a JVM crash or catastrophic failure).

### 2.3 Standardization of DataCompliance

* Introduce `DataComplianceResult` Value Object to capture arguments, ensuring `DataCompliance` mode adheres to the same
  audit standards as other modes.

---

## 3. Detailed Trade-off Analysis

| Dimension           | Pros (Benefits)                                                                                                                                                                                                | Cons (Costs/Risks)                                                                                                                                                                                                                                        |
|:--------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Reliability**     | **Crash Forensics:** This is the decisive factor. By recording inputs *before* execution, we guarantee that debugging information (arguments) is preserved even in fatal crash scenarios (OOM, StackOverflow). | **Data Duplication:** Argument data is stored in both `ScenarioTrace` (Memory) and `ExecutionResult` (Stack/Heap). However, given that these are text metadata, the memory footprint impact is negligible.                                                |
| **Maintainability** | **Compiler Enforcement:** By mandating `ExecutionResult` as the return type, developers cannot accidentally omit argument collection. The compiler acts as a safety net against silent failures.               | **Boilerplate Code:** Executor implementations become slightly more verbose. Developers must write both the `trace.record()` call and the `ExecutionResult` construction.                                                                                 |
| **Consistency**     | **Unified Behavior:** All test modes (User, Contract, Compliance) now provide the same level of diagnostic detail. No mode is a "second-class citizen" regarding observability.                                | **State Divergence Risk:** Theoretically, if arguments are mutated during execution, Trace (Intent) and Result (Fact) might differ. We mitigate this by defining `ExecutionResult` as the primary source of truth.                                        |
| **Concurrency**     | **Worker Isolation:** By returning immutable results, we reduce the reliance on shared state during the reporting phase.                                                                                       | **Internal Thread Safety:** Since users may employ multi-threading within a test, the `InMemoryScenarioTrace` implementation must be upgraded to use **Concurrent Collections** (e.g., `ConcurrentHashMap`) to prevent `ConcurrentModificationException`. |

---

## 4. Consequences

Adopting this strategy requires the following implementation changes:

1. **Trace Implementation:** `InMemoryScenarioTrace` must use `ConcurrentHashMap` and `ConcurrentLinkedQueue` to handle
   intra-test concurrency safely.
2. **Executor Logic:** `DefaultScenarioExecutor` must implement the "Record-Then-Execute-Then-Return" pattern.
3. **Data Models:** Create `DataComplianceResult` and integrate it into `ExecutionResult`.
4. **Interceptor Logic:** `AuditingInterceptor` must implement branching logic:
    * **Happy Path:** Use `ExecutionResult` for logging.
    * **Failure Path (Crash):** Fallback to `ScenarioTrace.generatedArguments` for forensic logging.
5. **Impact on ADR-009:** This decision extends the capabilities of `DataCompliance` (defined in ADR-009). Fuzzing
   inputs used for structural verification will now be traceable in failure reports, closing the auditability gap.

This decision accepts the cost of slightly increased code complexity to achieve **maximum robustness and debuggability**
in a production testing environment.