# ADR-008: Adoption of Generative Fallback for Stateless Mocks

**Status:** Accepted

**Date:** 2025-12-09

## Context

Standard mocking libraries (like Mockito) typically return default values such as `null`, `0`, or `false` when an
unstubbed method is called.
This behavior leads to critical issues in a "Zero Boilerplate" environment:

1. **NPE Hell:** Service logic often crashes with a `NullPointerException` immediately upon consuming a return value
   from a Mock.
2. **Forced Configuration:** Users are forced to write `when(...).thenReturn(...)` for every trivial method call just to
   prevent the test from crashing, even if that method is irrelevant to the test scenario.

## Decision

We adopt a **"Generative Fallback"** strategy to ensure test continuity.

1. **Smart Stub:** When a method is called on a Mock object and no explicit user configuration exists, the framework
   intervenes instead of returning null.
2. **Auto-Generation:** The framework analyzes the **Return Type** of the method and uses the `FixtureGenerator` to
   instantaneously create a **"Valid Filled Object"**.
    * `List`: Returns a non-empty list (e.g., `[User(name="A"), User(name="B")]`) to ensure loops are executed.
    * `Boolean`: Returns `true` or `false` (Randomly).
    * `UserDto`: Returns a fully populated object via recursive constructor injection.
3. **No Null Policy:** The framework guarantees that it will never return `null`, except for `Unit` or `Void` return
   types.

## Consequences

### Positive

* **Happy Path Guarantee:** Business logic continues to execute without interruption even without any mock
  configuration, drastically improving the developer experience (DX).
* **Zero Setup:** Eliminates the need for boilerplate stubbing for irrelevant dependencies.

### Negative

* **Unintended Branching:** Randomly generated values might unintentionally trigger specific branches in the business
  logic (e.g., an `if` statement). In such cases, the user must explicitly control the behavior using `ScenarioContext`.