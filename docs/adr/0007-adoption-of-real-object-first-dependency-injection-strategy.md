# ADR-007: Adoption of "Real Object First" Dependency Injection Strategy

**Status:** Accepted

**Date:** 2025-12-09

## Context

Traditional unit testing practices (Solitary Unit Testing) often advocate replacing all dependencies of the System Under
Test (SUT) with **Mocks**.
However, this approach introduces several significant issues:

1. **False Positives:** Tests may pass even when there are integration bugs because Mocks are configured to behave
   differently from the actual implementation.
2. **Setup Fatigue:** Developers are forced to write verbose mocking code even for simple Helper or Utility classes that
   contain trivial logic.

Since `Kontrakt` aims for "Zero Boilerplate," excessive mocking becomes an impediment rather than a benefit.

## Decision

We adopt a **"Real Object First"** strategy for dependency injection.

1. **Default Strategy (Real Object):** The `TestInstanceFactory` will analyze the constructor of the requested type and
   attempt to instantiate the **actual object (Real Object)** by recursively resolving its dependencies.
2. **Fallback Strategy (Exceptions):**
    * **Interfaces/Abstract Classes:** Since they cannot be instantiated, a **Mock** will be generated automatically.
    * **Explicit Isolation (`@Stateful`):** For components requiring strict isolation, such as Databases or external
      I/O, a **Fake** or **Mock** will be generated if explicitly marked.
3. **Circular Dependency Resolution:** The 'Real Object' strategy carries the risk of `StackOverflowError` in cases of
   mutual dependency (`A <-> B`). To mitigate this, we implement a **'Path Tracking'** algorithm. If a cycle is detected
   during the resolution graph traversal, the cycle is immediately broken by substituting the dependency with a **Mock
   **.

## Consequences

### Positive

* **Increased Reliability:** Tests verify the actual logic execution path, reducing false positives.
* **Reduced Boilerplate:** Drastically reduces the need for `when(...)` setup code for stateless dependencies.

### Negative

* **Performance Overhead:** Object instantiation costs may increase compared to creating simple Mocks.
* **Debugging Complexity:** Debugging deep dependency trees could become complex if an exception occurs deep within the
  graph.