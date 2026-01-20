# ADR-025: Adoption of Interface-First Design and Test Interface Pattern

* **Status:** Accepted
* **Date:** 2026-01-20
* **Context:** Core Architecture & Test Strategy
* **Relies on:** [ADR-001] Hexagonal Architecture, [ADR-005] Unified Two-Pillar Strategy

## 1. Context

As `Kontrakt` evolves, we need a consistent guideline for defining component boundaries and sharing test logic.
In traditional Java ecosystems, developers often rely on **Abstract Base Classes** (e.g., `BaseRepositoryTest`) to share
common logic.

However, this "Inheritance-First" approach introduces critical architectural limitations:

1. **"Is-A" vs "Can-Do" Mismatch:** Abstract classes force a rigid "Is-A" taxonomy. However, capabilities like
   `Traceable` or `Verifiable` are "Can-Do" behaviors that should be composed (Mixed-in), not inherited linearly.
2. **State Coupling:** Abstract classes often hold mutable state (e.g., `protected Database db`), creating hidden
   coupling between the parent and children. This leads to flaky tests and hinders parallel execution.
3. **Forced Environment (All-or-Nothing):** Subclasses inherit the *entire* environment of the parent. They cannot "
   opt-out" of expensive setups or conflicting rules defined in the base class.

Since Kotlin Interfaces support **Default Implementations**, we can decouple "Contract Definition" from "Implementation
Details."

## 2. Decision

We will adopt an **"Interface-First"** Design Strategy and the **"Test Interface Pattern"** as the standard.

### 2.1. Design Rule: Prefer Interfaces over Abstract Classes

* **Default Choice:** All contracts, shared behaviors, and domain boundaries MUST be defined as **Interfaces**.
* **Exception (When to use Abstract Class):** Use `abstract class` **ONLY IF**:
    * **Shared Mutable Infrastructure** is required (e.g., TestContainers, Embedded DB setup, heavy shared test
      infrastructure).
    * **Protected State** must be strictly preserved and shared across subclasses.
    * *Note:* Even in these cases, **prefer composition** (e.g., JUnit Extensions, Delegates) first before resorting to
      inheritance.

### 2.2. Adoption of Test Interface Pattern

We reject "Base Test Classes" for behavioral verification in favor of **Test Interfaces**.

* **Definition:** A test suite verifying a functional contract must be declared as an `interface` containing `@Test`
  methods.
* **Scope & Restrictions:** Test Interfaces should avoid `@Disabled`, `@Nested`, or stateful fields to ensure they
  remain pure contracts.
* **Lifecycle Policy:**
    * **Interface:** Strictly focus on **Behavior Verification** (`@Test`).
    * **Implementation:** Handle **Lifecycle Management** (`@BeforeEach`) and state initialization.
* **Composition (Mixin):** Concrete test classes should implement multiple Test Interfaces to verify all aspects of the
  component.

#### Example: Composition (Mixin) of Contracts

This demonstrates the power of the Interface pattern: verifying multiple behaviors on a single SUT.

```kotlin
// Contract A: Verifies tracing capability ("Can-Do")
interface TraceableContract {
    fun traceEnabledComponent(): Traceable

    @Test
    fun `must record execution trace`() {
        val sut = traceEnabledComponent()
        val trace = sut.run()
        assertTrue(trace.events.isNotEmpty())
    }
}

// Concrete Test: Implements BOTH contracts (Mixin)
class DefaultExecutorTest :
    ScenarioExecutorContract,
    TraceableContract {

    // Lifecycle management is explicit here (Opt-in)
    private val resources = ResourcePool()

    @BeforeEach
    fun setup() {
        resources.init()
    } // Explicitly controlled setup

    @AfterEach
    fun tearDown() {
        resources.close()
    }

    override fun executor() = DefaultScenarioExecutor(resources)
    override fun traceEnabledComponent() = DefaultScenarioExecutor(resources)
}
```

## 3. Consequences

### ✅ Positive Consequences

* **Perfect Isolation via Opt-in:** Unlike inheritance where the parent's environment is forced upon children,
  interfaces allow an **"Opt-in"** approach. The implementation explicitly selects which contracts to fulfill and which
  resources to initialize. This prevents "Hidden State Pollution" from unused parent features.
* **Composition over Inheritance:** We can verify complex objects by mixing in multiple contract tests (e.g.,
  `class SmartExecutorTest : ExecutorContract, LoggingContract`).
* **Decoupling State from Behavior:** Tests define *what* needs to happen (Interface), while implementations define
  *how* the environment is set up (Class).

### ⚠️ Negative Consequences

* **Infrastructure Limits:** Interfaces cannot reliably manage stateful JUnit extensions or shared mutable
  infrastructure. Heavy infrastructure tests (Integration Tests) will still strictly require `abstract class` or JUnit
  Extensions.
* **Trade-off Justification:** This tradeoff is acceptable because **behavioral correctness is prioritized over
  infrastructure convenience** in our framework design.
* **JUnit Constraints:** Using `@BeforeEach` inside interfaces can sometimes lead to unexpected behavior. We mitigate
  this by pushing lifecycle hooks to the concrete class.