# ADR-025: Interface-Driven Contract Verification and Test Interface Pattern

* **Status:** Revised
* **Date:** 2026-01-30
* **Context:** Core Architecture & Test Strategy
* **Relies on:** [ADR-001] Hexagonal Architecture, [ADR-009] Dual-Layer Contract Verification Strategy

## 1. Context

In a polymorphic system, interfaces define the behavioral contract. However, verifying strict adherence to these
contracts (Liskov Substitution Principle) across all implementations is tedious and error-prone.

We need a mechanism where:

1. **Constraints are defined directly on the Interface** (Single Source of Truth).
2. **Tests are written once against the Interface**, and the framework automatically runs them against all
   implementations.
3. **Developer Experience (DX) is seamless**, avoiding boilerplate like `@get:` prefixes or manual subject retrieval.

## 2. Decision

We adopt an **Interface-First Design** with a **declarative contract testing model**.

### 2.1. Clean Constraint Declaration

* **Property-Level Annotations:** All validation annotations (e.g., `@StringLength`, `@IntRange`) defined in
  `discovery.api` MUST target `AnnotationTarget.PROPERTY`.
* **No Syntax Noise:** This allows developers to annotate interface properties directly (e.g., `@StringLength(5)`)
  without the Kotlin `@get:` prefix.
* **Planner Responsibility:** The `Planner` extracts these annotations from the interface properties to build the
  validation plan.

### 2.2. The `Contract<T>` Base Interface

We introduce a standard `Contract<T>` interface that all contract tests must extend.

* **Automatic Injection:** The framework injects the system under test (SUT) into the `subject` property of
  `Contract<T>`.
* **Polymorphic Execution:** When a developer writes `interface UserTest : Contract<User>`, the `Linker`:
    1. Scans for all implementations of `User` (e.g., `Admin`, `Customer`).
    2. Expands the test suite to run for *each* implementation.
    3. Injects the specific implementation instance into `subject` for each run.

### 2.3. Generative Boundary Testing

The `subject` provided to the test is not just a random object.

* **Constraint-Based Generation:** The framework generates the `subject` instance using the constraints defined on the
  interface itself.
* **Constructor Injection:** Generated values (e.g., a String of length 5) are injected into the implementation's
  constructor to ensure it can be instantiated validly before testing behaviors.

## 3. Example Usage (Zero Boilerplate)

This represents the final, approved UX for defining and testing contracts.

```kotlin

// 1. The Interface (Clean Constraints)
interface User {
    // No '@get:' needed. Just pure intent.
    @StringLength(min = 5, max = 20)
    val username: String

    @IntRange(min = 18, max = 100)
    val age: Int
}

// 2. The Implementation
data class AdminUser(
    override val username: String,
    override val age: Int
) : User {
    init {
        // Business logic ensuring constraints are met
        require(username.length >= 5)
    }
}

// 3. The Contract Test (No 'subject()' implementation needed)
// User just extends Contract<T>, and 'subject' is magically available.
interface UserSpec : Contract<User> {

    @Test
    fun `must maintain username constraints`() {
        // 'subject' is automatically injected by the framework
        // for EVERY implementation found (AdminUser, GuestUser, etc.)
        assertTrue(subject.username.length >= 5)
    }
}
```

## 4. Consequences

### ✅ Positive Consequences

* **Developer Experience:** Removes all syntactic noise (`@get:`) and boilerplate (`fun subject()`). Writing a contract
  test feels like writing a standard spec.
* **LSP Enforcement:** Automatically verifies that all implementations honor the interface's annotations.
* **Zero-Config Discovery:** The developer doesn't need to manually register implementations; the `Linker` finds them.

### ⚠️ Negative Consequences

* **Framework Complexity:** The `Linker` and `VM` must handle the complex logic of finding implementations and injecting
  them into the generic `Contract<T>` interface.
* **Annotation Targeting:** We must strictly ensure all custom annotations in `Constraints.kt` include
  `AnnotationTarget.PROPERTY` to support this syntax.