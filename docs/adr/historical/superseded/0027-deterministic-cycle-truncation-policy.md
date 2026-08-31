# 27. Deterministic Cycle Truncation Policy for Recursive Types

**Date**: 2026-02-04

**Status**: Superseded

**Supersedes**: [ADR-0010](0010-strict-circular-reference-detection-strategy.md)

## Context

Unit testing environments frequently involve domain models with recursive structures. The previous "Strict Fail-Fast"
policy (ADR-010) prevented users from generating tests for common models, limiting usability. However, allowing
recursion blindly risks `StackOverflowError` or "Silent Failures".

We need a strategy that:

1. **Allows recursive type definitions** (Expression).
2. **Prevents infinite instantiation** via deterministic rules (Safety).
3. **Eliminates silent surprises** by making truncation explicit and traceable (Zero-Surprise).

## Decision

We adopt a **"Deterministic Truncation with Diagnostic Stubs"** strategy. The framework enforces a fixed set of rules to
sever cycles. **User customization is disallowed** to guarantee consistent behavior.

### 1. Architectural Separation

* **Planner (Phase 1):** Detects recursion and emits an `UnlinkedCycleNode` instead of the target node.
    * **Marker Location:** The node is emitted at the **use-site edge** (field/parameter/type-argument) where recursion
      is observed. It MUST carry `edgeKind`, `edgeName`, and `targetTypeId` (the canonical id that matched an ancestor).
    * **Cycle Identity:** Cycle detection uses the **Canonical TypeReference.id** (including generics) only.
        * **Constraint:** ID must be stable across adapters. Whitespace, type aliasing, and use-site annotations MUST
          NOT affect the ID.
        * **Nullability:** Cycle detection compares canonical IDs **ignoring nullability**. However, **truncation rule
          selection** (e.g., Rule #1 vs #5) uses the *specific edge's* resolved nullability (see §3).
* **Linker (Phase 2):** Wires a `CycleBreakingGenerator` unconditionally when encountering a cycle marker.
* **VM (Phase 3):** The generator executes the truncation logic. Failures here are classified as **Generation Failures
  **.

### 2. The 5 Truncation Rules (Priority Order)

These rules apply to the specific **edge** (field/parameter) where the cycle is detected.

| Priority | Scenario                  | Action                     | Rationale                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|:---------|:--------------------------|:---------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **1**    | **Nullable Type**         | Return `null`              | The safest truncation.                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **2**    | **Collection / Array**    | Return `empty` (size 0)    | Terminates recursion. **Overrides** value-level constraints (e.g., `@Size`).                                                                                                                                                                                                                                                                                                                                                                                                                     |
| **3**    | **Map**                   | Return `emptyMap()`        | Applied if a cycle is detected in *either* Key or Value.                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| **4**    | **Interface / Abstract**  | Return **Diagnostic Stub** | Inject a dynamic proxy/subclass.<br>• **Tech Spec:** Interfaces MUST be proxied via JDK Proxy. Abstract classes MAY require subclass-based proxying (e.g., ByteBuddy/CGLIB).<br>• **Action:** Throws `CycleTruncatedAccessException` on *any* method invocation (including primitives).<br>• **Safe Methods:** Only `toString`, `equals`, and `hashCode` are guaranteed safe defaults.<br>• **Fallback:** If proxying is impossible (e.g., `final` class, sealed type), fallback to **Rule #5**. |
| **5**    | **Concrete Non-Nullable** | **Fail-Fast**              | Includes Constructors, `lateinit var`, `non-null var`.<br>We cannot instantiate these without breaking invariants or injecting dangerous dummies. The user must refactor the model.                                                                                                                                                                                                                                                                                                              |

### 3. Nullability Resolution Order

To determine if Rule #1 applies to an edge, strict precedence is enforced. Platform types (`T!`) are treated
conservatively.

1. **Kotlin Type System:**
    * `T?` -> **Nullable**
    * `T` -> **Non-Nullable**
    * `T!` (Platform/Unknown) -> **Proceed to Step 2**
2. **Metamodel Metadata:** Explicit nullability info from the source.
3. **Annotations:** (e.g., `@Nullable`, `@javax.annotation.Nullable`)
4. **Fallback:** If status is still unknown, treat as **Non-Nullable** (triggers Rule #5).

### 4. Constraint Suppression & Diagnostics

Truncation is a deviation from the contract. It must be explicitly recorded.

* **Suppression:** Applies only to **value-level constraints** (e.g., `@Size`). Nullability/Invariant constraints are
  never suppressed.
* **Structured Trace:** The `ExecutionResult` must include a list of `TruncationRecord`s.
* **Path Format:** The `path` MUST be deterministic and use `TypeReference.signature` (not `toString`).
    * Format: `RootType -> ChildType -> ... -> TargetType`

```kotlin
data class TruncationRecord(
    val path: String,          // Deterministic signature path
    val edgeKind: EdgeKind,    // e.g., FIELD, CTOR_PARAM, MAP_KEY
    val edgeName: String?,     // e.g., "orders"
    val typeId: String,        // Canonical ID (stable & unique)
    val rule: TruncationRule,  // e.g., TRUNCATE_EMPTY_COLLECTION
    val suppressedConstraints: List<String> // e.g., ["@Size(min=1)"]
)
```

## Consequences

### Positive

* **Zero-Surprise:** Truncated objects are either safe (null/empty) or loud (throwing stubs).
* **Determinism:** Cycle handling is consistent across Kotlin, Java, and Platform types.
* **Implementation Guidance:** Explicit proxy technology requirements and fallback rules reduce ambiguity for
  implementers.

### Negative

* **Strictness:** Models with non-nullable recursive loops (including `T!` resolved as non-null) will cause **Generation
  Failures**.