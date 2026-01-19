# ADR-021: Adoption of Zero-Config Hybrid Journaling Strategy for Cloud-Native Execution

* **Status:** Accepted
* **Date:** 2026-01-11
* **Context:** Execution Engine & Infrastructure / Performance Optimization
* **Relies on:** [ADR-017] Worker-Based Synchronous Audit Journaling

## 1. Context & Problem Statement

In **ADR-017**, we adopted a "Worker-Based Synchronous Journaling" strategy (`RandomAccessFile`) to ensure forensic data
safety during JVM crashes. While this solved the "Data Loss" problem, it introduced a new bottleneck when deployed in
**Enterprise Cloud Environments**.

**The Cloud Reality (IOPS Bottleneck):**
Modern CI/CD pipelines (AWS EC2, GitHub Actions Runners, Kubernetes Pods) utilize network-attached storage (e.g., AWS
EBS). These volumes have strict **IOPS (Input/Output Operations Per Second) limits**.

* A standard Generative Test Suite runs thousands of scenarios per minute.
* If we perform a `write()` syscall for every single trace event, we generate **tens of thousands of IOPS**, easily
  exceeding standard cloud limits (e.g., 3,000 IOPS for gp3).
* This triggers **"I/O Throttling,"** causing the test suite to freeze or run 10x slower than on a local machine,
  leading to flakiness and timeout failures.

**The Usability Conflict:**
To mitigate this, we could instruct users to mount a RAM Disk (`tmpfs`). However, forcing enterprise users to configure
complex infrastructure just to run a testing framework violates our **"Zero Configuration"** philosophy. The framework
must be optimized for the cloud **by default**.

We need a strategy that drastically reduces IOPS (System Calls) without sacrificing the "Crash Safety" guarantees
established in ADR-017.

## 2. Decision

We will adopt a **"Zero-Config Hybrid Journaling"** strategy that combines **Micro-Batching** with **Smart Flushing**.

This strategy moves the optimization logic from the Infrastructure (RAM Disk) into the Application Layer, balancing
performance and safety dynamically.

### 2.1. Micro-Batching (The Accelerator)

Instead of invoking a System Call for every log line, the `RecyclingFileTraceSink` will maintain a small, fixed-size
**Memory Buffer (4KB)** per worker.

* **Mechanism:** Log events are written to this byte array first.
* **Trigger:** The buffer is flushed to disk (`write()`) only when full.
* **Impact:** Reduces IOPS by a factor of ~30x (assuming avg. log size is small), effectively eliminating cloud
  throttling risks.

### 2.2. Smart Flush (The Safety Net)

To prevent data loss during a crash, we bypass the buffer for **Critical Events**.

* **Logic:** If an incoming event is of type `EXECUTION`, `VERIFICATION`, `ERROR`, or **`ARGUMENT_RECORDING`**, the
  buffer is **force-flushed immediately** before writing the new event.
* **Guarantee:** The sequential order of logs is preserved (Buffer -> Critical Event), ensuring that the "Cause of
  Death" (specifically the input arguments) is always persisted physically to the disk.

### 2.3. The "Last Will" Shutdown Hook

We will register a JVM Shutdown Hook to handle catastrophic termination (e.g., `OutOfMemoryError`).

* **Action:** Upon JVM shutdown signal, the hook triggers a `forceFlush()` on all active sinks.
* **Goal:** To salvage the remaining non-critical data (e.g., Fixture Generation logs) sitting in the 4KB buffer.

### 2.4. Intelligent Noise Filtering

We will modify the `AuditingInterceptor` to apply **Adaptive Logging Levels**.

* **Default:** `TracePhase.DESIGN` (Fixture Generation details) events are **dropped** to save I/O bandwidth.
* **Verbose Mode:** These events are only emitted if the user explicitly enables `--verbose`.

## 3. Consequences

### ✅ Positive Consequences

* **Cloud-Native Optimization:** Drastically reduces IOPS consumption (from ~10k to ~300 IOPS), allowing the framework
  to run at maximum CPU speed even on cheap, throttled cloud instances.
* **Forensic Integrity:** Despite buffering, the **"Smart Flush"** ensures that the critical context (Input Data + Stack
  Trace) leading to a crash is never lost.
* **Zero Configuration:** Users do not need to set up `tmpfs` or RAM Disks. The framework self-optimizes for standard
  HDD/SSD storage.
* **Developer Experience (DX):** Eliminates "False Flakiness" caused by I/O timeouts in CI pipelines.

### ⚠️ Negative Consequences (Trade-offs)

* **The "Micro-Gap" Risk:** In the extremely rare event of an **OS-Level Crash** (Kernel Panic) or a hard `kill -9` (
  SIGKILL) occurring *exactly* when the buffer is full but not flushed, up to **4KB of non-critical data** (approx. last
  20 lines) may be lost.
    * *Mitigation:* This is an acceptable trade-off for a 3000% performance gain. Critical errors trigger a flush
      anyway.
* **Implementation Complexity:** The `TraceSink` logic becomes more complex. It must manage buffer pointers, handle
  overflow, and ensure thread safety during the shutdown sequence.
* **Memory Overhead:** Introduces a tiny memory footprint (4KB * Worker Count), which is negligible even for
  high-concurrency setups.

## 4. Alternatives Considered

* **Memory-Mapped Files (mmap):**
    * *Pros:* Fastest possible I/O.
    * *Cons:* Handling file resizing (growing beyond initial allocation) and handling `SIGBUS` errors adds significant
      complexity and instability risks.
    * *Verdict:* Rejected for now. Micro-batching provides sufficient performance with higher stability.
* **RAM Disk (tmpfs) Requirement:**
    * *Pros:* Zero code changes.
    * *Cons:* Poor UX. Requires users to modify CI YAML files.
    * *Verdict:* Rejected. Optimization should be internal to the framework.