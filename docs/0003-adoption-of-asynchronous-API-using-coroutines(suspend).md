# ADR-003: Adoption of Asynchronous API using Coroutines (suspend)

**Status:** Accepted

**Date:** 2025-10-11

## Context

Several core operations within the `Kontrakt` framework, particularly classpath scanning in the `Discovery & Planning Context`, are I/O-bound. These operations can be slow, especially in large projects.

If these operations are implemented synchronously (as regular functions), they will block the calling thread until completion. This poses a significant risk to the framework's responsiveness and efficiency. For example, a synchronous scan could cause the UI to freeze when integrated into an IDE plugin, or it could inefficiently hold onto thread resources in a concurrent test runner.

## Decision

We will adopt an **asynchronous-first design** for the framework's core APIs using **Kotlin Coroutines**.

All public API methods that perform potentially long-running or I/O-bound operations **must** be declared as `suspend` functions.

- The `TestDiscoverer.discover` method will be the first and primary application of this principle.
- Internal I/O work within these functions will be explicitly dispatched to an appropriate context (e.g., `withContext(Dispatchers.IO)`).

## Consequences

### Positive
- **Responsiveness:** By not blocking the calling thread, we ensure the framework can be safely used in UI-sensitive environments like IDE plugins without causing freezes. This is critical for future extensibility.
- **Efficiency:** Coroutines allow for more efficient use of system resources. Threads are not blocked waiting for I/O, freeing them up to perform other work.
- **Future-Proof & Idiomatic Design:** This decision aligns `Kontrakt` with modern, idiomatic Kotlin development practices. It simplifies future integration with other asynchronous libraries and frameworks within the Kotlin ecosystem.

### Negative
- **Increased Complexity for Consumers:** Callers of `Kontrakt`'s core API (including our own runner modules and future plugins) will be required to operate within a coroutine context (e.g., using `runBlocking` or another `suspend` function). This introduces a slight learning curve for contributors.
- **Initial Development Overhead:** Requires more careful thought about concurrency, thread safety, and the correct use of `CoroutineContext` and `Dispatchers` during initial development.