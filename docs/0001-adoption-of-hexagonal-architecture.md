# ADR-001: Adoption of Hexagonal Architecture

**Status:** Accepted

**Date:** 2025-10-05

## Context

`Kontrakt` is a testing framework designed for **long-term maintenance and evolution**. The core logic must remain independent of specific, volatile implementation technologies to ensure it can be easily updated as new, better technologies emerge. This project is viewed as a long-term initiative, and its architecture must support this vision.

We anticipate several key technology choices that may evolve over the project's lifecycle:

* **Annotation Processing:** The initial version will use **runtime reflection** for rapid prototyping and ease of implementation. However, to optimize performance and provide compile-time safety, we plan to support or migrate to a **compile-time approach (e.g., KSP)** in the future.
* **Classpath Scanning:** The initial version will leverage a well-established library like **ClassGraph** for its robustness and rich feature set. However, we may desire to implement a **custom, highly-optimized scanner** in the future to meet specific performance needs or reduce external dependencies.

Tightly coupling the core logic to any of these specific, interchangeable implementations would make future evolution difficult, slow, and risky. We need an architecture that explicitly treats these technologies as "plugins" or "adapters" from day one.

## Decision

We will adopt **Hexagonal Architecture** (also known as **Ports and Adapters**) as the primary architecture for the project.

1.  The framework's core logic (`kontrakt-core`) will be the **'Hexagon.'** It will contain no knowledge of the outside world (e.g., JUnit, KSP, ClassGraph).
2.  All interactions between the Hexagon and the outside world will be defined by **'Ports'** (Kotlin interfaces).
3.  Concrete implementations using specific technologies (e.g., a JUnit 5 integration, a ClassGraph-based scanner, a reflection-based processor) will be implemented as **'Adapters'** that conform to these ports.

## Consequences

### Positive
- **Flexibility for Evolution:** The architecture directly supports our long-term vision. Replacing the reflection-based annotation processor with a KSP-based one, or swapping ClassGraph for a custom scanner, can be done without altering the core framework logic.
- **Pluggability:** New technologies can be supported by simply creating new adapters. For example, we could add a TestNG runner in the future alongside the JUnit 5 runner.
- **Testability:** The core logic (the Hexagon) can be tested in isolation by injecting simple, fake adapters, making our own unit tests fast, reliable, and independent of external libraries.
- **Clear Separation of Concerns:** A strict boundary is enforced between the framework's "what" (the core logic) and "how" (the technology-specific implementations), leading to better maintainability.

### Negative
- **Increased Initial Complexity:** Requires creating interfaces (ports) and implementations (adapters) for all external interactions, which can mean more files and boilerplate for simple features.
- **Potential for Over-engineering:** This structure might feel like over-engineering in the very early stages of the project, but this is a deliberate trade-off for long-term flexibility.