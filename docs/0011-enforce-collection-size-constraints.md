# 11. Enforce Collection Size Constraints

* **Status:** Accepted
* **Date:** 2025-12-21

## Context

The current execution engine processes collections in test scenarios without any boundaries.
If a test definition includes a large dataset or an infinite generator, it causes high memory consumption and
unpredictable execution times.
There is no safeguard to prevent the framework runner from crashing due to `OutOfMemoryError` when handling excessive
elements.

## Decision

We will implement a mandatory size limit for all collection handling within the framework.

1. **Hard Limit Enforcement:** The framework's core engine must check the size of any list or map it processes.
2. **Default Cap:** Set a default maximum size (e.g., 100 items) for collections to ensure safe execution defaults.
3. **Configurability:** Provide a mechanism for users to explicitly override this limit in their test configuration if
   larger datasets are required.
4. **Error Handling:** Throw a specific exception (e.g., `CollectionLimitExceededException`) immediately when the limit
   is breached.

## Consequences

### Positive

* **Stability:** Prevents the test runner from crashing due to unexpected large data.
* **Predictability:** Ensures tests finish within a reasonable timeframe.

### Negative

* **Implementation Effort:** Logic to intercept and count collection elements must be added to the core loop.
* **User Friction:** Users testing large datasets must manually configure the limit override.