# ADR-013: Transition to Standalone Execution Ecosystem and Native IntelliJ Plugin Development

## Status

Accepted

## Context

The "Kontrakt" framework is approaching its **v1.0 Official Release**. The current dependency on JUnit 5 limits the
framework's potential for high-performance optimizations (v2.0+) and fails to provide a premium Developer Experience (
DX).

To achieve the goal of "replacing JUnit," reliance on workaround solutions (like console scraping via TeamCity messages)
is insufficient. We require a robust, first-class integration that treats Kontrakt as a native citizen of the IDE.

## Decision

We will decouple "Kontrakt" entirely from the JUnit ecosystem and build a fully vertically integrated environment. We
reject "console-based workarounds" and choose the **Orthodox Method** of developing a full-featured IntelliJ Platform
Plugin.

The architecture consists of four distinct components:

1. **Dedicated Reporting Domain:**
    * Establish a pure domain module responsible for capturing, aggregating, and analyzing test execution results.
    * Serves as the protocol-agnostic source of truth for all runners and plugins.

2. **Standalone Console Runner (The Engine Driver):**
    * A pure JVM application that acts as the headless execution entry point.
    * Responsible for **Automated Custom Test Setup** and invoking the `TestScenarioExecutor`.
    * Designed to be invoked programmatically by the IntelliJ Plugin or Gradle, exposing structured execution events (
      IPC) rather than simple text logs.

3. **Gradle Plugin (The Build Integrator):**
    * Provides a custom task (`:kontraktTest`) to replace the standard `:test` task.
    * Ensures seamless integration with CI/CD pipelines via standard XML report generation using the Reporting Domain.

4. **Native IntelliJ Platform Plugin (The Core DX Provider):**
    * **Strict Adherence to IntelliJ SDK:** We will implement the full suite of IntelliJ's testing infrastructure
      interfaces instead of relying on generic console output.
    * **Components to Implement:**
        * `TestFramework`: To identify Kontrakt tests and provide setup/teardown logic inspection.
        * `RunLineMarkerContributor`: To render "Gutter Icons" (Play buttons) directly in the editor source code.
        * `ConfigurationFactory` & `RunConfiguration`: To define persistent run configurations specific to Kontrakt.
        * `ProgramRunner` & `TestConsoleProperties`: To control the execution process and bind a custom
          `SMTRunnerConsoleView` for a rich, interactive test tree UI.

## Consequences

### Positive

* **First-Class Citizenship:** Kontrakt will behave exactly like (or better than) built-in frameworks (JUnit/TestNG)
  within the IDE.
* **Deep Integration:** Allows for advanced features like "Rerun Failed Tests," "Navigate to Source," and custom test
  filters that simple console runners cannot support.
* **Total Control:** Full authority over the entire stack—from the UI button click to the JVM execution—enabling future
  optimizations like KSP-based pre-compilation and custom classloaders.

### Negative

* **High Development Complexity:** Requires deep knowledge of the IntelliJ Platform SDK and Program Structure
  Interface (PSI).
* **Development Time:** Implementation time is significantly higher compared to simple console output solutions.

## Implementation Strategy

1. **Phase 1 (Core & Reporting):** Implement the **Reporting Domain** and the **Standalone Runner** logic (headless).
2. **Phase 2 (IDE Integration - Priority):** Develop the **Native IntelliJ Plugin**. Focus on implementing
   `TestFramework` identification and `RunConfiguration` to allow execution from the IDE.
3. **Phase 3 (Build & CI):** Develop the Gradle Plugin to wrap the runner for CI/CD environments.