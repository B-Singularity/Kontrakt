# Contributing to Kontrakt

Thank you for your interest in contributing to Kontrakt! We welcome contributions of all kinds, from bug reports and documentation improvements to new features.

To ensure the project remains consistent and well-structured, please follow the guidelines below.

---

## 1. Architectural Principles

Before writing any code, it is crucial to understand the core architectural principles that guide this project. All contributions must respect these principles.

- **Hexagonal Architecture:** `Kontrakt` is built upon a Hexagonal Architecture (Ports and Adapters). The core domain logic is kept completely independent of external technologies like test runners or classpath scanners.
    - **For more details, please read our Wiki page: [ADR-001: Adoption of Hexagonal Architecture](docs/adr/0001-adoption-of-hexagonal-architecture.md)**

- **Domain-Driven Design (DDD):** The core logic is designed using DDD principles to manage complexity and create an expressive model.
    - **Before contributing, please familiarize yourself with our [[DDD in Kontrakt|DDD Overview]] and the project's [[DDD: Ubiquitous Language|Ubiquitous Language]].**

---

## 2. Testing Guidelines

**All contributions that change or add code logic (features, bug fixes) MUST include corresponding tests.** This is non-negotiable and ensures the long-term stability of the framework.

We have two primary locations for tests, each with a different purpose:

### `kontrakt-core/src/test`
- **Purpose:** For **internal unit tests** of the framework's own components.
- **Perspective:** From the viewpoint of the **framework developer**.
- **What to test here:** Individual classes, methods, and logic within the `kontrakt-core` module (e.g., "Does the `TestDiscoverer` correctly parse dependencies?").

### `kontrakt-examples/src/test`
- **Purpose:** For **integration and acceptance tests** that demonstrate how to use the framework.
- **Perspective:** From the viewpoint of the **framework user**.
- **What to test here:** Public-facing APIs and annotations. Use this module to create real-world examples that show a feature works from end-to-end (e.g., "Does a class using `@KontraktTestable` get tested correctly?").

---

## 3. Code Style

This project uses **`ktlint`** to enforce the official Kotlin style guide.

Our CI pipeline runs `ktlintCheck` on every pull request, and **PRs with style violations will be blocked from merging.**

Before committing your changes, please run the auto-formatter to fix any style issues. This ensures that code reviews can focus on logic, not on formatting.

```bash
# This command will automatically format your code to match the style guide.
./gradlew ktlintFormat
```
---
## 4. Development Workflow

We follow a standard development workflow based on **GitHub Flow**.

### Branching Strategy

1.  Always start your work from an up-to-date `main` branch.
2.  Create a new branch for your work with a descriptive name, prefixed by its type (e.g., `feature/`, `fix/`, `docs/`).

### Commit Message & PR Title Convention

We follow the **[Conventional Commits](https://www.conventionalcommits.org/)** specification. This is critical as it allows for automated versioning and changelog generation.

The format for your commit messages should be:
`<type>(<scope>): <subject>`

**Important:** The title of your Pull Request **must** also follow this Conventional Commits format. When we merge Pull Requests using "Squash and merge," the PR title becomes the final commit message on the `main` branch.

**Common Types:**
- `feat`: A new feature for the user.
- `fix`: A bug fix for the user.
- `docs`: Documentation only changes.
- `refactor`: A code change that neither fixes a bug nor adds a feature.
- `test`: Adding or correcting tests.
- `chore`: Changes to the build process or auxiliary tools.

**Example PR Title:** `feat(core): Add support for nested test classes`

---

## 5. Submitting a Pull Request

1.  Format your code by running `./gradlew ktlintFormat`.
2.  Ensure all new and existing tests pass locally by running `./gradlew build`.
3.  Push your branch and open a Pull Request against the `main` branch.
4.  Fill out the Pull Request template as completely as possible.
5.  Wait for all CI checks (Build, Ktlint, CLA, etc.) to pass.
6.  A maintainer will then review your code.

We look forward to your contributions!