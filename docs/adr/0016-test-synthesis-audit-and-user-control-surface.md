# ADR 016: Test Synthesis Audit & User Control Surface

* **Status:** Accepted
* **Date:** 2025-12-31
* **Context:** Kontrakt v3.1 Execution Engine & Reporting Interface
* **Relies on:** [ADR-015] Reporting Output Strategy

## Context

The **Kontrakt** framework utilizes "Generative Testing" (Auto-Fixtures, Dynamic Mocking), which involves complex
internal logic often perceived as "Black Box Magic" by developers.
When a test behaves unexpectedly (or passes suspiciously), developers struggle to understand:

1. **Why** a specific strategy (Mock vs Real) was chosen.
2. **What** data was actually generated for the inputs.
3. **Whether** the assertions were truly executed or skipped.

Furthermore, configuration options for running tests (filtering, logging, archiving) need to be consolidated into a
unified interface to improve the User Experience (UX).

## Decision

We will implement a **"Synthetic BDD Audit" (Trace Mode)** to provide full transparency, and consolidate all execution
options into a standardized **User Control Surface**.

### 1. The User Control Surface (Execution Options)

The framework MUST support the following command-line arguments and configuration properties to control the test
lifecycle.

| Category            | Option / Flag           | Description                                                                                                        | Default Behavior                    |
|:--------------------|:------------------------|:-------------------------------------------------------------------------------------------------------------------|:------------------------------------|
| **🔍 Transparency** | **`--trace`**           | Activates **Synthetic BDD Audit**. Generates a detailed "Design-Execution-Verification" timeline in the report.    | `Disabled` (Standard execution)     |
|                     |                         | *Use Case:* Debugging "Magic", understanding framework decisions.                                                  |                                     |
| **🎯 Targeting**    | **`--tests <Pattern>`** | Runs only specific classes or methods matching the pattern (Wildcard supported).                                   | Run `All` discovered tests          |
|                     |                         | *Example:* `--tests "com.shop.*Order*"`                                                                            |                                     |
|                     | **`--package <Name>`**  | Scopes scanning to a specific package for faster feedback.                                                         | Root package scan                   |
| **🗄️ Artifacts**   | **`--archive`**         | Enables **History Mode**. Saves the report with a timestamp (`history/report-{time}.html`) instead of overwriting. | `Overwrite` (`index.html` only)     |
|                     |                         | *Use Case:* CI/CD pipelines, keeping debugging logs.                                                               |                                     |
| **📢 Verbosity**    | **`--verbose` / `-v`**  | **Console Detail Level.** Shows detailed success logs in the console.                                              | `Normal` (Smart Summary & Failures) |
|                     | **`--quiet` / `-q`**    | **Minimal Mode.** Shows only failure summaries and final stats.                                                    |                                     |
| **🎲 Reproduction** | **`--seed <Long>`**     | Forces the Random Generator to use a specific seed for deterministic reproduction.                                 | Random Seed                         |

### 2. Audit Structure (Trace Mode UX)

When `--trace` is active, the report MUST be organized into three cognitive phases to match the developer's mental
model (BDD Style):

* **🧠 DESIGN (Given):** Explains *why* the environment was built this way.
    * *Contents:* Strategy selection, Mocking decisions, Fixture generation details.
* **⚙️ EXECUTION (When):** Details the runtime actions.
    * *Contents:* Method invocation, Execution time, State transitions.
* **🔍 VERIFICATION (Then):** Lists all implicit and explicit checks performed.
    * *Contents:* Contract Clauses, Invariant checks, Mock verifications.

### 3. "Summary First" Visual Principle

To maintain readability in Trace Mode:

* **Default View:** Show human-readable summaries (e.g., "Mocked UserRepository").
* **Code Expansion:** The actual generated code (e.g., `val mock = mock<User>()`) MUST be collapsible or hidden by
  default, accessible only on demand.
* **Minimalist Style:** Use emojis strictly for **Section Headers** and **Status Indicators** (✅/❌) to maintain an
  Enterprise-Grade professional look.

## Consequences

### Positive

* **Unified UX:** Users have a consistent set of tools to Control (Targeting), Observe (Tracing), and Preserve (
  Archiving) their tests.
* **Trust & Transparency:** Eliminates the "Magic" fear by providing a clear Audit Trail of how the test was
  synthesized.
* **Reproducibility:** Explicit Seed control allows exact replay of edge cases found during generative testing.

### Negative

* **Performance Cost:** Trace mode incurs significant object allocation overhead (capturing decision events). It should
  be strictly used for debugging/auditing, not for standard high-throughput CI runs.