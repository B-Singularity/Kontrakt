# ADR 015: Reporting Output Strategy & Artifact Management

* **Status:** Accepted
* **Date:** 2025-12-31
* **Context:** Kontrakt v3.0 Reporting Context
* **Relies on:** [ADR-014] Asynchronous Event-Driven Reporting Architecture

## Context

In ADR-014, we decided to process test results asynchronously. However, we must now define **how** these results are
presented to the user and **where** they are stored.

**The Problem:**

1. **Information Overload:** Generative testing produces massive amounts of data. Printing everything to the Console
   renders it unusable ("Wall of Text").
2. **File Clutter:** Creating report files in the project root or source directories pollutes the workspace and version
   control.
3. **Persistence Dilemma:** Users want a clean workspace (don't pile up files) but occasionally need to save specific
   run histories for debugging or auditing.
4. **CI/CD Integration:** CI systems expect specific standard formats (JUnit XML) and locations to display test results
   automatically.

## Decision

We will adopt a **"Console-as-Dashboard, File-as-Detail"** strategy with strict artifact lifecycle management.

1. **Separation of Concerns (View Strategy):**
    * **Console:** Acts as a real-time **Status Dashboard**. It displays progress, summarized statistics, and "Failure
      Teasers" (brief error + seed). It MUST provide a **Deep Link** (file URL) to the full report.
    * **File:** Acts as the **Deep Analysis Tool**. It contains the full object graphs, synthetic reproduction code, and
      navigation features.

2. **Standardized Artifact Location:**
    * All reports MUST be generated in the build tool's standard output directory (e.g., `build/reports/kontrakt/` for
      Gradle, `target/` for Maven).
    * **Rationale:** This ensures reports are ephemeral and automatically cleaned up via standard commands (e.g.,
      `./gradlew clean`), preventing "Zombie Files."

3. **Overwrite-by-Default (Retention Policy):**
    * **Default:** The framework will generate a single `index.html` (and `TEST-*.xml`), strictly **overwriting** the
      previous run's file.
    * **Archiving:** Users can opt-in to historical storage via a flag (e.g., `-Dkontrakt.archive=true`). Only then will
      the framework save a timestamped copy (e.g., `history/report-{timestamp}.html`).

4. **Dual-Format Export:**
    * The Reporting Context will generate a human-readable **HTML (SPA)** for developers and a machine-readable **JUnit
      XML** for CI tools simultaneously.

## Consequences

### Positive

* **Clean UX:** The console remains readable and actionable, while deep data is just one click away.
* **Zero Maintenance:** Users do not need to manually delete old report files; the build system handles hygiene.
* **CI Ready:** Out-of-the-box compatibility with Jenkins, GitHub Actions, and TeamCity via standard XML paths.
* **Predictability:** The user always knows exactly where to find the result of the *latest* run.

### Negative

* **Data Loss Risk:** If a user runs tests twice in rapid succession without the archive flag, the first result is
  irretrievably lost (Overwrite). This is considered an acceptable trade-off for workspace cleanliness.
* **Browser Dependency:** Detailed analysis requires a web browser, unlike pure text logs which can be viewed in a
  terminal editor (vim/nano).