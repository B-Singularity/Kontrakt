# ADR-006: Adoption of Scoped Discovery Mechanism for Performance Optimization

**Status:** Accepted

**Date:** 2025-12-09

## Context

Current implementation of `TestDiscoverer` performs a **Full Classpath Scan** regardless of user intent.

* Even when a user wants to run a single test file in the IDE, the framework scans the entire project.
* In large-scale projects (Monorepo, MSA Multi-module), this causes significant delays in the feedback loop (Cold Start
  latency).

We need a mechanism to limit the scanning range based on the execution context (IDE Selection, Gradle Task, etc.).

## Decision

We will introduce a **`ScanScope`** Value Object to explicitly define the range of discovery.

### 1. Introduction of `ScanScope` Sealed Interface

Instead of passing a raw `String` root package, we pass a structured scope object:

* `ScanScope.All`: Scans the entire classpath (e.g., CI/CD, `./gradlew test`).
* `ScanScope.Packages`: Scans specific packages (e.g., Module testing).
* `ScanScope.Classes`: Scans specific classes (e.g., IDE 'Run File').

### 2. Adapter Responsibility

The `KontraktTestEngine` (JUnit Adapter) is responsible for translating JUnit's `EngineDiscoveryRequest` (Selectors)
into our `ScanScope`.

### 3. Core Optimization

`ClassGraphScannerAdapter` will utilize `acceptPackages()` and `acceptClasses()` APIs to physically optimize I/O
operations based on the provided `ScanScope`.

## Consequences

### Positive

* **Performance:** Single file execution becomes instantaneous (O(1) lookup vs O(N) scan).
* **Scalability:** The framework remains fast even as the project grows into a massive Monorepo.
* **Flexibility:** Future scopes like "Git Changed Files" or "Failed Tests Only" can be easily added to the `ScanScope`
  interface.

### Negative

* **Complexity:** The `TestDiscoverer` and `Scanner` interfaces become slightly more complex than just taking a
  `String`.
* **Adapter Logic:** The JUnit adapter needs logic to parse and map `Selectors` to `ScanScope`, increasing the
  maintenance burden of the adapter layer.