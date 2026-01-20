# [ADR-019] Adoption of Interceptor Pattern for Execution Pipeline Topology

* **Status:** Accepted
* **Date:** 2026-01-05
* **Context:** Execution Engine / Cross-cutting Concerns

## 1. Context and Problem Statement

As the `Kontrakt` framework evolves, the `TestScenarioExecutor` (the core execution engine) requires various
cross-cutting concerns such as:

1. **Auditing:** Logging execution traces and verification results (Current Requirement).
2. **Metrics:** Measuring execution latency and throughput.
3. **Resilience:** Retry logic for flaky tests or chaos engineering.
4. **Resource Management:** Timeout handling and circuit breaking.

Initially, the **Decorator Pattern** was considered to wrap the `DefaultScenarioExecutor`. However, as the number of
features grows, the Decorator pattern introduces significant structural rigidity and complexity, particularly regarding
**Kotlin Symbol Processing (KSP)** based code generation.

We need a topology that allows for easy extensibility, loose coupling, and simplified code generation for future
framework enhancements.

## 2. Decision

We will adopt the **Interceptor Pattern** (also known as the Chain of Responsibility) to manage the execution pipeline,
replacing the proposed Decorator approach.

* We will define a `ScenarioInterceptor` SPI.
* We will implement a `RealInterceptorChain` to manage the flow of control.
* The `RuntimeFactory` will assemble the pipeline as a **flat list** of interceptors rather than a nested hierarchy of
  objects.

## 3. Detailed Analysis

### 3.1. Future Extensibility & KSP Compatibility

The primary driver for this decision is the complexity of code generation using KSP in the future.

* **Decorator Approach (The "Nesting Hell"):**
    * To compose decorators, the factory must instantiate objects recursively:
      `new Audit(new Retry(new Metrics(new Core())))`.
    * **KSP Impact:** Generating this code requires the processor to understand the dependency graph and the specific
      constructor signature of every decorator. It must algorithmically construct a "Russian Doll" structure, which is
      error-prone and brittle.
* **Interceptor Approach (The "Flat List"):**
    * The pipeline is constructed as a collection: `listOf(Audit, Retry, Metrics)`.
    * **KSP Impact:** The processor simply needs to identify all implementations of `ScenarioInterceptor` and generate a
      single line of code to add them to a list. This reduces the complexity of the code generator from **O(N) recursive
      complexity** to **O(1) collection gathering**.

### 3.2. Coupling and Interfaces

* **Decorator:** Requires strict adherence to the `TestScenarioExecutor` interface. The wrapper is tightly coupled to
  the specific contract of the wrapped object.
* **Interceptor:** Decouples the "Cross-cutting Logic" from the "Execution Contract". Interceptors implement a separate
  `ScenarioInterceptor` interface, interacting only with the generic `Chain`. This allows infrastructure logic to evolve
  independently of the core business logic.

### 3.3. Runtime Control

* **Decorator:** The execution order is statically fixed at instantiation time. Reordering requires re-instantiating the
  entire object graph.
* **Interceptor:** Execution order is determined by the order of elements in the list. This allows for dynamic
  reordering at runtime (e.g., via configuration files or annotations) without recompilation or complex factory logic.

## 4. Consequences (Trade-offs)

### Positive Consequences (Pros)

* **Simplified Topology:** The pipeline is flattened. Adding a new feature is as simple as `.add(interceptor)`.
* **Zero-Overhead Codegen:** KSP logic becomes trivial, significantly reducing technical debt in the compiler plugin
  layer.
* **Plugin Architecture:** This structure lays the foundation for a 3rd-party plugin system, where external developers
  can inject custom logic without modifying the core executor.
* **Separation of Concerns:** Infrastructure logic (Chain management) is completely separated from Business logic (
  Interceptor implementation).

### Negative Consequences (Cons)

* **Initial Infrastructure Cost:** Requires implementing supporting classes (`ScenarioInterceptor`, `Chain`,
  `RealInterceptorChain`) before writing the actual logic.
* **Stack Trace Depth:** Similar to decorators, interceptors add depth to the call stack, which can make debugging
  slightly more involved (though this is mitigated by clear class naming).
* **Cognitive Load:** Developers must understand the "Chain.proceed()" concept, which is slightly more abstract than
  direct method delegation.

## 5. Implementation Strategy

1. Define `execution.api.interceptor.ScenarioInterceptor`.
2. Implement `execution.domain.service.RealInterceptorChain`.
3. Refactor `AuditingScenarioExecutor` to `AuditingInterceptor`.
4. Update `RuntimeFactory` to assemble the chain.