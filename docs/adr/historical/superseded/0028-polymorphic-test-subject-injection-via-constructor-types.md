# ADR 0028: Polymorphic Test Subject Injection via Constructor Types

* **Status:** Superseded
* **Date:** 2026-02-07
* **Context:** The framework needs a unified UX for Contract Testing that handles both "General Compliance" (1
  Interface, N Implementations) and "Specific Verification" (1 Implementation), while strictly enforcing the separation
  between behavior and data.

## 1. Context and Problem Statement

In a contract-based testing environment, we face a design dilemma regarding the User Experience (UX) and the semantics
of "Contract":

1. **The Cardinality Conflict:**
    * **Compliance Testing:** Verifying that *all* implementations adhere to a contract (Liskov Substitution Principle).
      This requires **1-to-N execution** (One test class runs N times).
    * **Specificity Testing:** Verifying unique behaviors of a specific technology (e.g., Redis TTL). This requires *
      *1-to-1 execution**.
2. **The Identity Crisis:**
    * Allowing `@Contract` on concrete classes blurs the line between definition and implementation, leading to fragile
      tests coupled to implementation details.

**We need a mechanism that expresses the "Scope of Execution" implicitly through the Kotlin type system and enforces a
strict separation between "Behavioral Contracts" and "Data Contracts".**

## 2. Decision

We will adopt **Type-Driven Polymorphic Injection** for the `@KontraktTest` primary constructor, governed by a **Strict
Type Policy**.

### 2.1. Strict Type Policy (The "Separation of Concerns")

We define two distinct types of contracts with mutually exclusive targets:

* **Rule 1: Behavioral Contracts (`@Contract`)**
    * **Allowed Targets:** `interface`, `abstract class` **ONLY**.
    * **Semantics:** Defines a set of behaviors that *multiple* implementations (or subclasses) must fulfill.
    * **Constraint:** Applying `@Contract` to a concrete class (open/final/data/object) is **PROHIBITED** and will cause
      a `KontraktConfigurationException`.

* **Rule 2: Data Contracts (`@DataContract`)**
    * **Allowed Targets:** `data class`, `class` (DTO/VO), `record`.
    * **Semantics:** Defines the structural integrity and invariants of a data carrier.
    * **Constraint:** Applying `@DataContract` to an interface is **PROHIBITED** in V1.

### 2.2. Subject Selection Algorithm (Discovery Logic)

The `TestDiscoverer` MUST apply the following logic steps in order:

**Step 0. Annotation Target Validation (Fail-Fast)**

* Before scope calculation, validate annotations on the parameter type:
    * If `@Contract` is present on a **Concrete Class**: Throw `KontraktConfigurationException`.
    * If `@DataContract` is present on an **Interface**: Throw `KontraktConfigurationException`.

**Step 1. Parameter Count Check**

* The primary constructor MUST have **exactly 1 parameter**.
* **If 0 parameters:** Throw `KontraktConfigurationException`.
    * **Rationale:** Kontrakt is strictly designed for **Subject-Based Testing** (enforcing graph linking and
      isolation). Stateless or utility tests fall outside the framework's scope and should use standard JUnit.
* **If > 1 parameters:** Throw `KontraktConfigurationException`. (Auxiliary injection is not supported in V1).

**Step 2. Type Analysis (Scope Calculation)**

* **Case A: The "Abstract Behavioral Contract" (Universal Scope)**
    * **Condition:** Type is `interface` OR `abstract class`.
    * **Validation:** The type **MUST** be annotated with `@Contract`. (If missing, throw
      `KontraktConfigurationException`).
    * **Scan Mechanism:**
        * For `interface`: Use `getClassesImplementing(interfaceName)`.
        * For `abstract class`: Use `getSubclasses(className)`.
    * **Filter & Order:**
        * **Include:** Concrete, Non-synthetic classes within `DiscoveryPolicy` scope.
        * **Exclude:** Abstract classes, Interfaces, Anonymous/Local classes.
        * **Ordering:** MUST be **sorted by FQCN (ascending)** to ensure deterministic execution order.
    * **Action (1:N):** Create a distinct `TestSpecification` for *each* discovered target.
    * **Fail-Fast:** If 0 implementations found, throw `KontraktConfigurationException`.

* **Case B: The "Concrete Target" (Specific Scope)**
    * **Condition:** Type is a concrete `class` (including `data class`, `record`) or `object`.
    * **Note:** `@DataContract` presence is valid here but does not alter the *scope* (remains 1:1).
    * **Resolution:**
        * `class`: Instantiate via Linker (DI).
        * `object`: Use Singleton instance.
        * Other types (Enum, Annotation): Prohibited in V1.
    * **Action (1:1):** Create a single `TestSpecification` targeting exactly this class.

### 2.3. Zero-Config Lifecycle

* **User Prohibition:** Users MUST NOT instantiate the subject manually.
* **Auto-Wiring:** The `ExpansionLinker` and `GeneratorRegistry` are responsible for resolving the full dependency graph
  of the subject (e.g., injecting `EntityManager` into `JpaRepo`).

## 3. Strict Invariants Table

The following table defines the required behavior for edge cases during discovery:

| Parameter Type      | Annotations     | Outcome           | Exception                        |
|:--------------------|:----------------|:------------------|:---------------------------------|
| `Interface`         | `@Contract`     | **1:N Execution** | -                                |
| `Abstract Class`    | `@Contract`     | **1:N Execution** | -                                |
| `Interface`         | None            | **Fail**          | `KontraktConfigurationException` |
| `Abstract Class`    | None            | **Fail**          | `KontraktConfigurationException` |
| `Concrete Class`    | `@Contract`     | **Fail**          | `KontraktConfigurationException` |
| `Concrete Class`    | `@DataContract` | **1:1 Execution** | -                                |
| `Concrete Class`    | None            | **1:1 Execution** | -                                |
| `Data Class`        | `@Contract`     | **Fail**          | `KontraktConfigurationException` |
| `Object`            | None            | **1:1 Execution** | -                                |
| `Empty Constructor` | N/A             | **Fail**          | `KontraktConfigurationException` |

## 4. Reporting Identity

To support 1:N execution without ambiguity, we separate the Internal Key from the Display Name.

* **Internal Key (Identity):** `"${SpecClass.fqcn}::${SubjectClass.fqcn}"`
    * Guaranteed unique across the system. Used for result aggregation and history tracking.
* **Display Name (Report):** `"${SpecClass.simpleName} [${SubjectClass.simpleName}]"`
    * User-friendly format for logs and HTML reports.
    * Example: `UserComplianceSpec [MemoryUserRepository]`, `UserComplianceSpec [JpaUserRepository]`.

## 5. Risks & Management

* **Risk:** Users might try to put `@Contract` on a concrete class.
* **Mitigation:** The framework explicitly blocks this with a configuration exception (Step 0), enforcing DIP
  (Dependency Inversion Principle).
* **Risk:** Blocking 0-parameter tests might alienate users wanting simple tests.
* **Mitigation:** We explicitly document that Kontrakt is for *Architectural Testing*. Standard unit tests are better
  served by existing tools.