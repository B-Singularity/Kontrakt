# ADR 014: Asynchronous Event-Driven Reporting Architecture

* **Status:** Accepted
* **Date:** 2025-12-30
* **Context:** Kontrakt v3.0 Execution Engine, Reporting Context

## Context

The **Execution Context** in Kontrakt v3.0 is designed as a high-throughput generative simulation engine. It generates
fixtures and executes tests rapidly within strictly isolated `EphemeralContexts`.

Currently, or in a traditional synchronous model, the execution engine waits for the reporting layer to process
results (e.g., printing to Console, writing to XML/JSON, updating IDE UI) before moving to the next test or cleaning up
the current context.

**The Problem:**

1. **I/O Blocking:** File I/O and Console rendering are significantly slower than the in-memory execution of logic
   tests. This creates backpressure, slowing down the entire test suite.
2. **Delayed Resource Release:** The `EphemeralContext` (and the heavy object graphs within it) cannot be destroyed
   until the reporting phase is finished. This increases memory pressure during large-scale simulations.
3. **Coupling:** The execution logic is tightly coupled to the speed and availability of the output consumers.

## Decision

We will decouple the **Execution Context** from the **Reporting Context** by adopting an **Asynchronous Event-Driven
Architecture**.

1. **Producer-Consumer Pattern:**
    * The `Execution Context` will act as a **Producer**. It will **not** return a result object to the caller but will
      instead emit a lightweight `TestResultEvent` DTO to a bounded channel (or queue).
    * The `Reporting Context` will act as a **Consumer**. It will run on a separate thread (or coroutine) to process
      these events asynchronously.

2. **Fire-and-Forget Execution:**
    * Once the `TestResultEvent` is emitted, the Execution Context is free to immediately destroy the `EphemeralContext`
      and proceed to the next simulation. It does not wait for I/O operations.

3. **Immutable Event DTOs:**
    * The events passed between contexts must be strictly immutable `TestResultEvent` objects containing only primitive
      data or snapshots (e.g., `Synthetic Snapshot` strings) to prevent concurrent modification issues.

## Consequences

### Positive

* **High Throughput:** Test execution speed is limited only by CPU and memory, not by I/O latency.
* **Immediate Cleanup:** Enables the immediate destruction of `EphemeralContext`, keeping the memory footprint low even
  during long-running generative tests.
* **Scalability:** Multiple reporting backends (Console, File, Network) can subscribe to the event stream without
  impacting the execution engine's performance.

### Negative

* **Complexity:** Requires implementing a graceful shutdown mechanism (a "Drain" process) to ensure all pending events
  in the channel are processed before the JVM exits.
* **Error Handling:** Exceptions occurring during the reporting phase (e.g., Disk Full) are harder to propagate back to
  the main execution flow and must be handled within the consumer.
* **Determinism:** The order of console output is generally preserved by the queue, but strict synchronization between a
  specific test execution and its log appearance is loosened.