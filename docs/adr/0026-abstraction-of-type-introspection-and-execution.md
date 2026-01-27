# ADR 0026: Holistic Transition to Type Graph & Resolver Architecture

* **Status:** Accepted
* **Date:** 2026-01-28
* **Context:** The framework's entire lifecycle is critically dependent on JVM Runtime Reflection APIs.

## 1. Context and Problem Statement

The framework is built upon a "Reflection-first" architecture. This is not merely an implementation detail but a
fundamental structural flaw.

1. **Systemic Infection:** Reflection usage permeates Value Objects, Services, and Ports.
2. **KSP/AOT Blocker:** Holding references to `KClass` or `Method` makes supporting KSP or Native Image architecturally
   impossible.
3. **Identity Crisis:** The framework currently operates as a "Dynamic Runtime Tool". To evolve, it must shift its
   identity to a **"Compiler-Friendly System"**.

**We must eliminate JVM Reflection from the Domain Layer entirely.**

## 2. Decision

We will adopt the **Holistic Type Graph & Resolver Pattern**.

This is a **Big Bang** architectural shift. We are replacing the framework's fundamental data model from "Runtime Types"
to a "Pre-resolved Type Graph".

### 2.1. Irreversibility Rationale

This decision is structurally **irreversible**.
Once the Domain Layer is refactored, reintroducing runtime reflection would require rewriting the entire domain logic.

**Therefore, we accept that there is no rollback path to the old architecture.**

### 2.2. The "Zero-Reflection" Rule

To enforce this decision, strict boundary rules are applied:

* **PROHIBITED:** `java.lang.reflect.*`, `kotlin.reflect.*`, `java.lang.Class`, `instanceof KClass`.
* **ALLOWED:** `TypeDescriptor`, `TypeReference`, `TypeKind`, String FQCN.

### 2.3. The Architectural Pattern

#### A. Type Reference (The Safe Carrier)

A sealed hierarchy to explicitly categorize the source of the type.

```kotlin
sealed interface TypeReference {
    // Primary Citizen: Available via JVM Runtime
    data class Runtime(val platformType: Any) : TypeReference

    // Secondary Citizen: Provisional API for future KSP support.
    // WARNING: Using this assumes the caller accepts "Best-effort" resolution.
    // It is intentionally designed to be restrictive to prevent overuse in Phase 1.
    @RequiresOptIn(message = "This API is not fully supported until Phase 2.")
    data class Static(val fqcn: String) : TypeReference
}
```

#### B. The Resolver (The Only Gatekeeper)

The only component allowed to touch JVM Reflection.

```kotlin
interface TypeResolver {
    fun resolve(reference: TypeReference): TypeDescriptor
}
```

#### C. The Type Graph (The Canonical Model)

A recursive structure representing the type system.

**CRITICAL DESIGN NOTE:**
To prevent "Flag Explosion" (e.g., `isJson`, `isEntity`), we adopt a **`TypeKind`** classification.
The `TypeDescriptor` must remain a **Structural Fact Sheet**, not a Behavior Bucket.

```kotlin
enum class TypeKind {
    // Atomic values (Int, String, Enums) -> Generate directly
    VALUE,

    // Containers (List, Map, Array) -> Iterate and populate
    CONTAINER,

    // Data structures (Data Class, POJO) -> Traverse properties
    STRUCTURAL,

    // Abstract definitions -> Requires Strategy lookup
    INTERFACE,

    // Fallback
    UNKNOWN
}

interface TypeDescriptor {
    val simpleName: String
    val qualifiedName: String

    // --- Structural Classification (Replaces loose booleans) ---
    val kind: TypeKind
    val isNullable: Boolean // Orthogonal to Kind

    // --- Graph Edges ---
    val constructors: List<ConstructorDescriptor>
    val properties: List<PropertyDescriptor>
    val methods: List<MethodDescriptor>
    val typeArguments: List<TypeDescriptor>

    // --- Annotation Query ---
    // CULTURAL RULE: Annotations are Configuration, NOT Control Flow.
    fun hasAnnotation(fqcn: String): Boolean
    fun getAnnotationAttributes(fqcn: String): Map<String, Any?>?
}
```

## 3. Strict Invariants

1. **Context-Scoped Identity:**
    * The "Exact Same Instance" rule applies within a defined **Resolver Scope** (e.g., a single `TestDiscoverySession`
      or `FixtureGenerationContext`).
    * The Resolver **MUST NOT** rely on global static caching (to avoid ClassLoader leaks and test pollution).
2. **Determinism:** Same input = Same graph structure within the same scope.
3. **Cycle Safety:** Cyclic dependencies **MUST** be handled via object identity cycles or lazy evaluation.
4. **Complete Detachment:** No leaked reflection objects in public API.

## 4. Risks & Management Strategy

### 4.1. The "Broken Build" Phase (Valley of Death)

* **Risk:** This is a "One-Way Migration with No Safe Checkpoint".
* **Reality:** We expect the CI/Build to be unstable or broken during the migration.
* **Mitigation:** The team must treat this as a blocking priority until the new architecture stabilizes.

### 4.2. "If-Else" Hell via Flag Abuse

* **Risk:** Without `TypeKind`, generators might degrade into complex boolean logic (`if (isData && !isCollection)`).
* **Mitigation:** Logic must switch on `TypeKind`, forcing handling of all structural cases explicitly.

### 4.3. Static Reference Abuse

* **Risk:** Developers might use `Static(fqcn)` prematurely, expecting full resolution capabilities that don't exist
  yet.
* **Mitigation:** The API is marked `@RequiresOptIn`. Phase 1 implementations may throw `UnsupportedOperationException`
  for Static references to fail fast.

## 5. Success Criteria

* **Zero Imports:** `grep -r "kotlin.reflect" kotlin/*/domain` returns empty.
* **Test Stability:** Core framework tests run successfully without `mockkStatic`.
* **Structural Purity:** `TypeDescriptor` contains no methods other than getters and annotation queries.
* **KSP Readiness:** A KSP implementation could be swapped in without changing domain code.

```