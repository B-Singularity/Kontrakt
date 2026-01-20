# ADR 018: Hybrid Event-Stream Architecture & Optimization Strategy

* **Status:** Accepted
* **Date:** 2026-01-03
* **Context:** Kontrakt v3.1 Execution Engine & Reporting Layer
* **Relies on:** [ADR-014] Asynchronous Reporting, [ADR-017] Worker-Based Journaling

## 1. Context & Problem Statement

The interaction between **ADR-014 (Async Reporting)** and **ADR-017 (Synchronous Journaling)** creates a conflict in
implementation strategy regarding Data Safety versus System Throughput.

1. **The "Crash" Reality:** In Generative Testing, JVM crashes (StackOverflowError, OutOfMemoryError) are not edge cases
   but expected outcomes.
2. **The Batching Risk:** A naive implementation of ADR-014 buffers all `TraceEvents` in memory (Batching) to send them
   as a single message. If the JVM crashes, this in-memory buffer is lost, rendering the "Black Box" purpose of ADR-017
   useless.
3. **The I/O Dilemma:** While ADR-017 mandates writing to disk, doing so naively for every event (Real-time Sync)
   introduces significant I/O latency, potentially degrading the performance of high-frequency generative tests.
4. **Isolation Requirement:** The architecture relies on "Micro-DI" and stateless execution. Any optimization must not
   violate test isolation or introduce "Zombie State" (e.g., leaked ThreadLocals).

We need an architecture that guarantees **Forensic Data Safety** (logs are preserved even after a crash) while
maintaining **High Throughput** and **Low GC Overhead**.

## 2. Decision

We will adopt a **Hybrid Event-Stream Architecture** combining the **WAL (Write-Ahead Log)** and **Claim Check**
patterns, reinforced by strict optimization techniques.

This architecture prioritizes **Safety by Default**. We accept the cost of Disk I/O as a mandatory "insurance premium"
to guarantee reproducibility in crash scenarios.

### 2.1. Architectural Patterns

1. **Streaming WAL (Write-Ahead Log):**
    * The `AuditingScenarioExecutor` MUST write `TraceEvents` (Given/When/Then) directly to the `TraceSink` (Disk) as
      they occur.
    * We abandon the "In-Memory Batch" approach. Logs are never held in the heap waiting for the test to finish.

2. **Claim Check Pattern:**
    * The `TestResultEvent` sent to the Reporting Channel MUST NOT contain the actual `TraceLog` data (payload).
    * Instead, it MUST contain a **"Claim Check"** (the file path to the worker's log).
    * The Reporting Context consumes this event and lazily reads the disk only if detailed reporting (e.g., HTML
      generation for failures) is required.

### 2.2. Optimization Strategy (The 4 Pillars)

To mitigate the performance penalty of the Streaming approach, we mandate the following implementation details:

1. **Smart Buffering (64KB):**
    * We will use a `BufferedOutputStream` with a buffer size of **64KB** (up from the standard 8KB).
    * *Rationale:* This balances safety and speed. It significantly reduces System Calls (Context Switching) while
      limiting potential data loss to the last few milliseconds (kernel buffer) in a hard crash.

2. **Scoped Zero-Allocation:**
    * We prohibit the creation of temporary `TraceEvent` VO objects and intermediate JSON Strings in the hot execution
      path.
    * Instead, the `BufferedTraceSink` implementation MUST use **reusable internal buffers** (e.g., `StringBuilder` or
      `byte[]` as instance variables) to assemble JSON manually.
    * *Constraint:* To maintain isolation, these buffers must be `reset/cleared` at the start of every session (
      `startSession`).

3. **Overwrite Rotation (Rewind):**
    * Worker logs are reused. At the start of a test, the file pointer is rewound to 0 (`seek(0)` / `append=false`).
    * *Trade-off:* We accept the "Wasted I/O" for passed tests (writing then overwriting) to ensure that if a crash
      occurs *during* execution, the data is present.

4. **Micro-DI Compatibility:**
    * Optimizations must be encapsulated within the `TraceSink` (Infrastructure Layer). The `TestContext` (Domain Layer)
      remains ephemeral and unaware of these buffering mechanisms.

## 3. Rationale

This decision is based on the following analysis of forces:

| Force                   | Strategy               | Outcome                                                                              |
|:------------------------|:-----------------------|:-------------------------------------------------------------------------------------|
| **Forensic Safety**     | Streaming WAL          | **Secured.** Even if the JVM dies, the OS PageCache ensures logs are persisted.      |
| **Memory Stability**    | Claim Check            | **Secured.** JVM Heap never bloats, even with 1GB logs. Prevents self-inflicted OOM. |
| **Throughput**          | Smart Buffering (64KB) | **Optimized.** Reduces syscall overhead by ~87% compared to 8KB buffers.             |
| **Latency Consistency** | Zero-Allocation        | **Optimized.** Eliminates GC spikes ("Stop-the-world") during tight loops.           |

## 4. Consequences

### Positive

* **Absolute Crash Resilience:** Users can always determine the cause of a JVM crash (e.g., specific Seed or Input) by
  inspecting the worker log.
* **Constant Memory Footprint:** The framework's memory usage becomes predictable and flat, regardless of test
  complexity or log size.
* **Non-Blocking Reporting:** The Event Bus remains lightweight and responsive, preventing backpressure in the reporting
  system.

### Negative

* **I/O "Waste":** Successful tests (99% of cases) generate Write I/O that is immediately discarded. This consumes SSD
  bandwidth and endurance.
* **Implementation Complexity:** Manual JSON assembly (Zero-Allocation) is more error-prone and harder to maintain than
  simple Object Mapping.
* **Latency Floor:** There is a minimum latency floor dictated by the disk I/O speed, which is slower than pure
  in-memory operations.

## 5. Implementation Guidance

* **Class:** `execution.adapter.journal.BufferedTraceSink`
* **Dependency:** The `AuditingScenarioExecutor` acts as a decorator, bridging the domain execution and the
  infrastructure sink.
* **Safety Net:** While we rely on OS buffering, critical "Dying Messages" (e.g., unhandled exceptions caught at the top
  level) should trigger a `flush()` before the worker dies.