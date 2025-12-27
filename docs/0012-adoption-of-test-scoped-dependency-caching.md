# ADR-012: Adoption of Test-Scoped Dependency Caching

* **Status:** Accepted
* **Date:** 2025-12-28

## 1. Context

Currently, the `TestInstanceFactory` in the Execution Context operates as a transient factory based on the **Prototype
Pattern**. It creates a **new instance** every time a dependency is requested during the object graph construction.

This behavior introduces two critical consistency issues, particularly when supporting user-defined test scenarios (
`@KontraktTest`):

1. **Stubbing Mismatch:**
    * When a user requests a Mock in their test constructor (e.g., `mockRepo`), the factory creates Instance A.
    * When the System Under Test (SUT) requests the same dependency, the factory creates Instance B.
    * User configurations applied to Instance A (e.g., `every { ... }`) are **ignored** by the SUT, which uses Instance
      B.
2. **The Diamond Problem:**
    * If multiple components in the dependency graph refer to the same shared dependency (e.g., `ServiceA -> Repo`,
      `ServiceB -> Repo`), the factory instantiates the `Repo` twice.
    * This splits the state, leading to logical errors where data modifications in one path are invisible to the other.

We need a mechanism to ensure that within a single test execution, a specific type always resolves to the **Same
Instance (Reference Identity)**.

## 2. Decision

We will adopt a **"Test-Scoped Dependency Caching"** strategy (Conceptually similar to the *Ephemeral Singleton
Pattern*).

### 2.1. Scope Definition

The lifecycle of all generated instances (Mocks, Fakes, and Real Objects) will be strictly bound to the *
*`EphemeralTestContext`**.

### 2.2. Mechanism

The `TestInstanceFactory` will transition from a stateless factory to a context-aware assembler. It will delegate the
caching responsibility to the `EphemeralTestContext`.

1. **Lookup:** When a type is requested, the Factory first queries the `EphemeralTestContext`'s internal registry.
2. **Cache Hit:** If the instance already exists for the given type, it is returned immediately.
3. **Cache Miss:** If not found, the Factory instantiates the object (recursively), **registers** it in the Context, and
   then returns it.

### 2.3. Isolation Policy

The `EphemeralTestContext` (and its internal cache) MUST be **discarded** immediately after the test scenario finishes.
This ensures that state changes in one test (e.g., modifying a Mock's behavior or a Fake's data) never leak into
subsequent tests ("Test Pollution").

## 3. Consequences

### ✅ Positive Consequences

* **Stubbing Consistency:** Solves the "Stubbing Mismatch" problem. The Mock object manipulated by the user in the test
  method is guaranteed to be the exact same instance injected into the SUT.
* **Graph Integrity:** Solves the "Diamond Problem". Shared dependencies properly maintain a single shared state within
  the test scope, transforming the dependency tree into a DAG (Directed Acyclic Graph).
* **Performance:** Reduces the overhead of repetitive object instantiation and proxy generation (CGLIB/ByteBuddy) for
  commonly used dependencies.

### ⚠️ Negative Consequences

* **Intra-Test Side Effects:** Since objects are shared within a test, modifying a dependency's state in one part of the
  test (e.g., `repo.save()`) will affect all other collaborators using that repo. (Note: This is technically a correct
  behavior for integration testing consistency, but users must be aware of it).
* **Memory Footprint:** The framework holds the generated object graph in memory until the test completes, rather than
  potentially allowing GC to collect intermediate objects. However, given the small scope of unit tests, this impact is
  negligible.