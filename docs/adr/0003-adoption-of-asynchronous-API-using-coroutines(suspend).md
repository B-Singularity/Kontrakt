# ADR-003: Adoption of Asynchronous API Port Contracts

**Status:** Accepted

**Date:** 2025-10-11

## Context

Several core operations within the `Kontrakt` framework, particularly classpath scanning in the
`Discovery & Planning Context`, are I/O-bound. These operations can be slow, especially in large projects.

If these operations are implemented synchronously (as regular functions), they will block the calling thread until
completion. This poses a significant risk to the framework's responsiveness and efficiency. For example, a synchronous
scan could cause the UI to freeze when integrated into an IDE plugin, or it could inefficiently hold onto thread
resources in a concurrent test runner.

## Decision

We will adopt an **asynchronous-first design** for the framework's core APIs by defining **asynchronous port contracts**
at the hexagonal boundary.

All public API methods that perform potentially long-running or I/O-bound operations **must** expose an asynchronous
contract at the port boundary.

- The `TestDiscoverer.discover` method will be the first and primary application of this principle.
- The framework does **not** mandate a single implementation mechanism such as Kotlin Coroutines for all ports.
- Instead, the architectural requirement is that the port contract remain asynchronous and non-blocking in its
  semantics, and that only implementations which faithfully satisfy that contract may be used.
- The concrete async mechanism is an implementation concern of the adapter or module that fulfills the port contract.

## Consequences

### Positive

- **Responsiveness:** By not blocking the calling thread, we ensure the framework can be safely used in UI-sensitive
  environments like IDE plugins without causing freezes. This is critical for future extensibility.
- **Efficiency:** Asynchronous port contracts allow for more efficient use of system resources. Threads are not blocked
  waiting for I/O, freeing them up to perform other work.
- **Future-Proof & Hexagonal Design:** This decision preserves a clean architectural boundary by defining async behavior
  at the port level rather than coupling the core to a specific concurrency mechanism. It allows different adapters or
  modules to choose the most appropriate implementation strategy while remaining compliant with the same contract.

### Negative

- **Increased Complexity for Consumers:** Callers of `Kontrakt`'s core API (including our own runner modules and future
  plugins) must integrate with asynchronous contracts rather than assuming direct synchronous return values. This
  introduces some additional architectural and testing complexity for contributors.
- **Initial Development Overhead:** Requires more careful thought about concurrency, thread safety, completion
  semantics, and the correct separation between port contracts and implementation mechanisms during initial development.

### Implementation Guidelines

To ensure this architectural decision is implemented correctly and efficiently, contributors must follow these
guidelines:

- The core must depend on the **asynchronous contract of the port**, not on a specific implementation mechanism chosen
  by an adapter.
- Internal CPU-bound operations (e.g., reflection, dependency graph building) should not be unnecessarily offloaded or
  wrapped purely for stylistic consistency. They should remain direct unless asynchronous execution is required by the
  surrounding contract or execution model.
- Only true blocking I/O operations (e.g., reading `.class` files from disk or JARs) should be isolated appropriately by
  the implementing adapter or module.
- When in doubt, prefer keeping the architectural boundary clear: define asynchronous behavior at the port, and keep
  implementation-specific execution details behind that boundary.