# ADR-0029: Runtime Link Handle Protocol & Integrity

* **Status:** Superseded
* **Date:** 2026-02-11
* **Context:** MVP Phase. Single Producer Deployment (Reflection-only OR KSP-only).

## Context

Kontrakt follows a Compiler Pipeline architecture (Discovery → Linking → Execution). These layers utilize a shared
module, `kontrakt-ir`, to communicate.

In previous iterations, `kontrakt-ir` was loosely defined as a "Passive Protocol." However, in a JVM environment, the
ambiguity between **Source Names** (e.g., `Outer.Inner`) and **Binary Names** (e.g., `Outer$Inner`) leads to complex "
Adaptive Loading" logic and non-deterministic behavior in the Linker.

Shared modules often degenerate into "Bloated Shared Kernels." To achieve **Zero Debt**, **Byte-for-byte Reproducibility
**, and **Operational Predictability**, the IR must be strictly defined not just as a data container, but as a
deterministic **Runtime Link Handle**.

## Decision

We redefine `kontrakt-ir` as a **"Runtime Link Handle Protocol"**. It is optimized for direct consumption by the JVM
ClassLoader, enforcing strict runtime naming conventions and eliminating guessing logic.

To preserve its integrity, we enforce the following **Constitutional Rules**:

### 1. Definition: "Runtime Link Handle Protocol"

The IR is **NOT** a generic Intermediate Representation for source code analysis.

* **Concept:** It carries identifiers exactly as `Class.getName()` returns them in a JVM Runtime.
* **Implication:** The Linker does not attempt to reverse-engineer source code structures or guess file paths. It
  strictly loads the provided handle via the ClassLoader.
* **Scope:** This protocol is strictly bound to the JVM execution model.

### 2. Execution Model: "Single Producer, No Mixing"

To guarantee determinism, we enforce a strict deployment policy.

* **Rule:** A single execution run MUST use **exactly ONE** Discovery Provider (Producer).
    * **Forbidden:** Mixing Reflection-based discovery and KSP-based discovery in the same classpath/run.
* **Enforcement (Artifact-Level Guard):**
    * The Runtime Guard checks the `ServiceLoader` at the **entry point of scanning** (Lazy Check).
    * **0 providers:** Crash (Missing Dependency).
    * **>1 providers:** Crash (Classpath Conflict).
    * **Loading Failure:** Any `ServiceConfigurationError` is caught and rethrown as a framework-specific
      `RuntimeIntegrityException` with the root cause preserved.
* **Rationale:** This ensures a **Single Source of Truth**, eliminating the need for cross-producer equivalence logic.

### 3. Naming Standard: "JVM Runtime Name"

The `TypeId` is a direct handle to a loadable runtime class.

* **Rule:** `TypeId` MUST represent a valid **JVM Runtime Name**.
* **Producer Contract:**
    * **Enforcement:** Producers MUST emit the runtime name obtained from Reflection (`Class.getName()`) or an
      equivalent KSP binary-name algorithm.
    * **Linker Behavior:** The Linker treats ClassLoader load failures as **Linkage/Runtime Integrity failures** (never
      silent), reporting the failing `TypeId` as the primary evidence.
    * **BANNED Behavior:** **Synthesizing runtime names by source-notation concatenation** (e.g., producing nested names
      via "Outer.Inner") is strictly forbidden. Nested classes MUST be encoded as `Outer$Inner`.
* **Validation Policy (Strict Security & Hygiene):**
    * **Allow:** `$` in any position (Compliance with JVM Spec).
    * **Forbid:** Characters that imply Descriptors, Signatures, or Internal Names, which are technically possible in
      bytecode but unsupported by Kontrakt's handle protocol.
        * **Block List:** `/` (Internal Name Separator).
        * **Block List:** `[` `;` (Array/Object Descriptors).
        * **Block List:** `<` `>` (Generic Signatures).
        * **Block List:** `(` `)` `:` (Method Descriptors / Obfuscation patterns).
        * **Block List:** Whitespace, Control Characters.
* **Rationale:** We explicitly **DO NOT support** obfuscated names that collide with JVM descriptor syntax, as this
  compromises the protocol's safety.

### 4. Scope of Logic: "No Semantic Logic" (The Golden Rule)

The IR MUST NOT contain logic that influences downstream control flow or strategy selection.

* **Strictly Forbidden (Semantic Logic):**
    * **Branching:** `if (type.isScenario) ...` inside the IR.
    * **Semantic Defaulting:** `val timeout get() = input ?: 5000`.
    * **Lossy Normalization:** Trimming strings or changing casing.
* **Required (Canonicalization):**
    * **Invariants:** **Protocol Violations** MUST be enforced via explicit checks that throw
      `IrProtocolViolationException`.
        * **Forbidden:** `require()`, `check()`, `error()`, `assert()` (must throw custom protocol exceptions to ensure
          consistent error handling).
    * **Determinism:** **Sorting** (Total Order) and **Deduplication** are **MANDATORY** for all collections within the
      IR to ensure reproducibility.

### 5. Determinism & Immutability: "The Sovereign Protocol"

The IR object (`TestSpecification`) must be physically impossible to misuse, mutate, or bypass.

* **Encapsulation:** MUST be a plain `class` with a `private constructor` and a Companion Factory.
    * **`data class` is FORBIDDEN** to prevent the `copy()` backdoor.
* **Deep Immutability:** All collections MUST be defensively copied into a new structure (e.g., `ArrayList`) and then
  wrapped in `Collections.unmodifiableList/Map`.
* **Platform Dependency:** `java.util.TreeMap` is explicitly allowed (and required) for metadata to ensure byte-for-byte
  reproducibility on the JVM.

### 6. Discovery Policy: "Context-Aware Collect & Explode"

Discovery must be robust against environment noise and clear about failure causes.

* **Rule:** If a class name violates the Protocol, the Scanner MUST **accumulate** the error with **Context** (e.g., "
  Scenario Candidate", "Implementation of X") and **crash** the run at the end ("Report & Die").
    * **Forbidden:** Silent skipping of invalid classes.
* **Violation Classification:**
    * `PROTOCOL_VIOLATION`: The class name is invalid per TypeId rules (User/Compiler issue).
    * `SCANNER_CORRUPTION`: The scanner infrastructure returned unusable data or threw unexpected exceptions
      (Environment issue).
* **Filters:**
    * **Base Filter:** Applies to ALL scans. Rejects Synthetic, Anonymous, Local classes, and Compiler Artifacts (
      `$DefaultImpls`, `$WhenMappings`, `$$Lambda`).
    * **Heuristic Filter:** Applies ONLY to Auto-Discovery. Rejects likely non-test classes (e.g., `$Companion`,
      Proxies), unless explicitly annotated.

## Schema Definition (Allow/Deny List)

| Category        | Status         | Examples                                                                                                                           |
|:----------------|:---------------|:-----------------------------------------------------------------------------------------------------------------------------------|
| **Identity**    | ✅ **Allowed** | `TypeId` (Strict JVM Runtime Name Wrapper)                                                                                         |
| **Structure**   | ✅ **Allowed** | `TestSpecification` (Plain Class, Private Ctor)                                                                                    |
| **Modes**       | ✅ **Allowed** | `TestMode` (Sealed Interface)                                                                                                      |
| **Diagnostics** | ✅ **Allowed** | `metadata: TreeMap<String, String>` (Sorted, JVM-based)                                                                            |
| **Logic**       | ❌ **BANNED**  | Branching, Defaulting, `copy()`                                                                                                    |
| **Format**      | ❌ **BANNED**  | Internal names (`/`), descriptor/signature tokens, and source-notation synthesis for nested types (e.g., producing `Outer.Inner`). |
| **Mutability**  | ❌ **BANNED**  | `MutableList`, `var`, Exposed Mutable References                                                                                   |
| **Descriptors** | ❌ **BANNED**  | `Ljava/lang/String;`, `(I)V`                                                                                                       |
| **Exceptions**  | ❌ **BANNED**  | Standard `IllegalArgumentException` (Must use Custom Ex)                                                                           |

## Consequences

* **Positive:** **Operational Predictability.** The system fails fast and loudly if the environment is misconfigured.
* **Positive:** **Debugging Clarity.** Error reports distinguish between protocol violations and environment corruption
  with full context.
* **Positive:** **Architecture Simplicity.** No complex "Adaptive Loading" logic is required in the Linker.
* **Negative:** JVM-Coupling. This IR is strictly bound to the JVM execution model. Migrating to non-JVM platforms will
  require a new IR version/definition.
* **Negative:** "Single Producer" limits incremental migration scenarios (must switch fully to KSP or Reflection per
  deployment).