# ADR-004: Definition of the Test Execution Final Status

**Status:** Accepted
**Date:** 2025-10-18

## Context

The `TestExecution` aggregate produces a final, immutable `TestResult` which must contain a clear status. To ensure consistency, we must formally define the possible **final statuses** a test can have.

## Decision

The final outcome of a test execution will be represented by the **`TestStatus` sealed interface**. A `TestExecution` begins in an initial state and must always transition to one of the following terminal states.

### Final States (Defined by `TestStatus`)

- **`Passed`**: The test executed successfully.
- **`AssertionFailed`**: The test executed, but an assertion failed.
- **`ExecutionError`**: The test crashed due to an unexpected exception.
- **`Disabled`**: The test was intentionally not executed.
- **`Aborted`**: The test was skipped during execution because a runtime assumption failed.

### State Transition Logic

The `TestExecution` aggregate is responsible for determining the correct final `TestStatus`.

- If a "disabled" condition is met, the result is `Disabled`.
- If a "skip" condition is met, the result is `Aborted`.
- Otherwise, the test is executed:
  - If an unexpected exception is caught, the result is `ExecutionError`.
  - If one or more assertions fail, the result is `AssertionFailed`.
  - If all assertions pass, the result is `Passed`.

A `TestExecution`'s internal, transient state (e.g., `RUNNING`) is an implementation detail of the aggregate and is not part of the final `TestStatus` model.

## Consequences

- **Clarity & Predictability:** Provides a clear and unambiguous model for the `TestExecution` lifecycle.
- **Robustness:** The `TestExecution` aggregate can actively prevent invalid state transitions (e.g., a `PASSED` test becoming `FAILED`), making the framework more robust.
- **Foundation for Future Features:** Establishes a solid foundation for implementing more complex features like test retries (which would require explicitly defining a new state or transition).
- **Increased Rigidity:** The state machine is intentionally rigid. Adding new states or transitions will require a formal update to this ADR and the core logic. This is a deliberate trade-off for stability.