# ADR-010: Strict Circular Reference Detection Strategy

* **Status:** Accepted
* **Date:** 2025-12-16
* **Author:** [Your Name/Team Name]

## 1. Context

When generating test fixtures automatically using `FixtureGenerator`, object graphs often contain **Circular References
** (e.g., Bidirectional JPA relationships like `User` ↔ `Order`) or **Self-References** (e.g., Recursive structures like
`Category` → `parent`).

Without a defensive mechanism, the generator attempts to instantiate these objects infinitely. This results in a
`StackOverflowError`, which causes the JVM to crash. Unlike standard assertion failures, a stack overflow is
catastrophic: it terminates the entire test suite immediately, leaves no meaningful logs, and hinders the Continuous
Integration (CI) process.

We considered allowing a limited recursion depth or silently returning `null`/Mock objects. However, silent failures can
lead to "False Negative" tests where logic passes only because data was missing, hiding potential bugs.

## 2. Decision

We adopt a **Strict Cycle Detection (Fail-Fast)** strategy using a `Set`-based history check.

### 2.1. Mechanism

The `GenerationContext` will maintain a `Set<KClass<*>>` named `history` to track the types visited in the current
generation branch.

### 2.2. Policy

If the generator encounters a type that is already present in the `history` set:

* It **ABORTS** the generation process immediately.
* It **THROWS** a `CircularDependencyException`.
* The exception message must clearly state the detected path (e.g., `User -> Order -> User`) to aid debugging.

### 2.3. Handling Valid Recursion

For scenarios where recursive data structures (e.g., Tree traversal) are strictly required for testing:

* **Manual Assembly:** The developer must explicitly construct the object graph in the test code (manually linking
  nodes).
* **Custom Generators:** Developers can register a specific generator for that type to override the default strict
  behavior.

## 3. Consequences

### ✅ Positive Consequences

* **System Stability:** Guarantees that the test suite will never crash due to infinite recursion, ensuring a reliable
  CI pipeline.
* **Fail-Fast & Explicit:** Developers receive a clear error message explaining *why* the generation failed, rather than
  debugging a cryptic crash or a `NullPointerException` caused by silently missing data.
* **Design Feedback:** Immediate failure on circular dependencies highlights potentially tight coupling in the domain
  model, encouraging better design (e.g., referencing by ID instead of object).

### ⚠️ Negative Consequences

* **Reduced Convenience:** Common bidirectional patterns (e.g., JPA Entities) will not work out-of-the-box and require
  manual handling or explicit exclusion configurations.
* **Limited Automation:** Deep hierarchical structures (Trees) cannot be auto-generated to arbitrary depths; only the
  root level is generated before the safeguard triggers.