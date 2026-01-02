# ADR 017: Worker-Based Synchronous Audit Journaling Architecture

* **Status:** Accepted
* **Date:** 2026-01-02
* **Context:** Kontrakt v3.0 Execution Safety & Audit System
* **Relies on:** [ADR-015] Reporting Output Strategy, [ADR-016] User Control Surface

## 1. Context & Problem Statement

The Kontrakt framework performs **Generative Testing**, which explores unpredictable execution paths using randomized
inputs. This domain introduces unique architectural challenges distinct from standard unit testing frameworks:

1. **Crash Risk ("Dead Data" Problem):**
   Generative testing inherently increases the risk of JVM crashes (e.g., `StackOverflowError` from deep recursion,
   `OutOfMemoryError` from massive object graphs). In a standard asynchronous logging model, log events buffered in
   memory are lost when the process crashes, leaving the user with no information about the cause of the crash.

2. **Concurrency vs. I/O Dilemma:**
   Running thousands of tests in parallel creates a conflict:
    * **Centralized Logging:** A single log file introduces a `Global Lock` contention, creating a bottleneck that
      negates the benefits of parallel execution.
    * **Per-Test Logging:** Creating a new file for every test iteration causes OS file system exhaustion (inode limits,
      I/O storms) when scaling to millions of generated cases.

3. **Requirement for Isolation:**
   Tests are Opt-in and independent. A failure or crash in one test execution must not affect the logging integrity or
   execution of parallel tests (Bulkhead Pattern).

## 2. Decision

We will adopt an **Asynchronous Choreography Architecture** with **Synchronous Worker Journaling**.

Instead of mapping files to *tests*, we map files to *execution workers*. We utilize a fixed-size pool of reusable
journals combined with a synchronous Write-Ahead Logging (WAL) strategy.

### Key Decisions:

1. **Worker-Based Recycling Journaling:**
    * We will maintain a fixed pool of log files corresponding to the number of available execution workers (e.g.,
      `worker-0.log` to `worker-7.log`).
    * Files are **never deleted and recreated** during the test run. Instead, they are **reused (rewound)** for each new
      test session.

2. **Synchronous WAL (Write-Ahead Log):**
    * Events are written to the disk synchronously (blocking I/O) before the actual test logic executes.
    * This ensures that even if the JVM crashes milliseconds later, the last "Dying Message" (e.g., the specific seed or
      parameters causing the crash) is preserved on the disk.

3. **Choreography over Orchestration:**
    * There is no central logging coordinator managing the write order.
    * Each worker exclusively owns its assigned file channel for the duration of a test session, eliminating lock
      contention between threads.

## 3. Rationale (The 3 Forces Analysis)

this architecture optimizes for the specific needs of Generative Testing:

### ① Communication: Asynchronous

* **Design:** The Test Engine dispatches test cases to the Executor asynchronously.
* **Reason:** To maximize CPU throughput for intensive object generation and reflection logic without blocking the main
  thread.

### ② Coordination: Choreography

* **Design:** Workers borrow a recorder from the pool and manage their own I/O independently.
* **Reason:**
    * **Isolation:** A crash in `worker-1` does not corrupt `worker-2`. This implements the **Bulkhead Pattern**.
    * **Scalability:** Performance scales linearly with the number of CPU cores as there is no central lock.

### ③ Consistency: Atomic Local Consistency

* **Design:** Strong consistency is enforced per file via Synchronous I/O. Eventual consistency is accepted for the
  final aggregated report.
* **Reason:** In forensic auditing, preserving the *last event before death* is more critical than aggregate throughput.

## 4. Detailed Architecture & Lifecycle

### 4.1. The "Rewind & Archive" Strategy

To manage disk usage while ensuring safety, we apply a "Delete-on-Success, Archive-on-Failure" policy using a recycling
mechanism.

1. **Borrow (Start Session):**
    * A worker borrows an idle `RecyclingJournalRecorder`.
    * **Action:** The recorder seeks to the beginning of the file (`seek(0)`) and truncates it (`setLength(0)`).
    * **Header:** A session header containing the `Test Name` and `Thread ID` is written immediately.

2. **Record (Execution Phase):**
    * All `Design`, `Execution`, and `Verification` events are written synchronously.
    * **Crash Safety:** If the JVM crashes here, the file remains in this state, effectively serving as a Black Box.

3. **Return (End Session):**
    * **On Success:** The recorder is simply returned to the pool. The content will be overwritten by the next test (
      Clean Workspace).
    * **On Failure:** The current content of the worker log is **copied (archived)** to a permanent file (e.g.,
      `failures/FAIL_TestName_Timestamp.log`) before returning the recorder to the pool.

### 4.2. Recovery & Retry Policy

Since Generative Testing relies on randomness, "Retry" implies **Deterministic Reproduction** using the same Seed.

* **Recovery Target:** Failed tests or Crashed sessions.
* **Requirement:** The log header or the first `DesignDecision` event MUST contain the **Random Seed**.
* **Actionable Output:** The reporter must parse preserved logs to provide a reproduction command (e.g.,
  `./gradlew test --args="--seed 12345"`).

## 5. Failure Handling Strategy (Resilience)

We define strict rules for handling failures *within* the auditing system itself to prevent logging errors from breaking
the test suite.

### 5.1. Event Publication Failure (Disk I/O Error)

* **Context:** Disk full, permission denied, or file lock issues.
* **Decision:** **Fast Fail & Circuit Breaker**.
    1. **No Retry:** We do not retry I/O operations to avoid blocking the worker thread.
    2. **Console Fallback:** Critical information (specifically the **Seed** and **Failure Reason**) is dumped to
       `System.err`.
    3. **Circuit Breaker:** The specific Recorder instance is marked as `BROKEN` and will silently discard future writes
       for the remainder of the session.

### 5.2. Corrupted Log Recovery

* **Context:** A partial write occurs due to a sudden power loss or crash.
* **Decision:** **Best-Effort Parsing**. The reporting parser will read valid lines up to the corruption point and
  display a "Log Truncated" warning, rather than discarding the entire file.

## 6. Consequences

### Positive

* **Crash Safety:** Guarantees forensic data availability even in the event of a catastrophic JVM crash.
* **Resource Efficiency:** Fixed number of file handles (O(N) where N = CPU Cores), regardless of running millions of
  tests.
* **High Performance:** Zero overhead for file creation/deletion during runtime; Zero lock contention between workers.

### Negative

* **I/O Latency:** Synchronous flushing introduces a micro-latency (ms range) per event, which is an accepted trade-off
  for data safety.
* **Implementation Complexity:** Requires custom implementation of a `RecorderPool` and `RandomAccessFile` management
  instead of using standard logging libraries.

## 7. Implementation Guidance

* **Core Classes:** `TraceRecorderPool`, `RecyclingJournalRecorder`.
* **I/O Mechanism:** Use `java.io.RandomAccessFile` with mode `"rwd"` (Read/Write + Synchronous Update to Disk) to
  ensure data is flushed without explicit `force()` calls.
* **Artifact Location:**
    * Active Worker Logs: `build/reports/kontrakt/logs/workers/`
    * Archived Failures: `build/reports/kontrakt/logs/failures/`