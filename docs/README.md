# Kontrakt Documentation

This directory contains the design, decisions, implementation planning, and theoretical foundation of Kontrakt.

## `the-most-important-thing`

The theoretical foundation of Kontrakt.
`What Contract Is` defines the highest-level Contract principles, with supporting research kept alongside it.

## `adr`

Accepted and proposed architecture decisions. ADR numbers preserve the historical order of decisions.

### `contract`

Decisions that define Contract authority and meaning.

* `interface-api` — User-facing Interface, Interaction, Operation, and Contract declaration boundaries.
* `data-model` — Shared value, aggregate, collection, equality, and structural data laws.
* `one-dimensional` — Individual 1D Contracts such as Input, Admission, Canonicalization, Lowering, Fact, and Invariant.
* `establishment` — Definition, Occurrence, Required Basis, Applicability, and authority establishment.
* `governance` — Policy, Governance, Versioning, and control over applicable Contract worlds.
* `composition` — How independent Contracts, flows, cores, and whole machines compose.
* `outcomes` — Failure, Publication, Output, and other Contract-established results.

### Other ADR areas

* `compiler-structure` — Compiler-wide structural and package decisions.
* `frontend` — Source acquisition, resolution, and formation of resolved compiler-semantic material.
* `ir` — Decisions for HIR, MIR, LIR, and their semantic boundaries.
* `realization` — How user implementation enters and is related to the Kontrakt compiler.
* `optimization` — Compiler analysis and meaning-preserving optimization decisions.
* `backend` — Target lowering and JVM-oriented realization.
* `compiler-engine` — Query execution, dependency tracking, reuse, caching, generations, and incremental computation.
* `diagnostics` — Compiler and user diagnostic architecture.
* `verification` — Compiler verification, reference checking, PBT, and QA-related decisions.
* `runtime` — Execution-time machine behavior and runtime lifecycle.
* `infrastructure` — Shared low-level compiler mechanisms such as identity, storage, and primitive data structures.

## `design`

Current compiler design documents.
These describe how accepted decisions are organized and may evolve without changing Contract authority.

## `todo`

Open design and implementation work.
TODOs are grouped by the area that owns the unresolved work.

## `compiler-protocols`

Shared compiler protocols used across multiple subsystems.
These are compiler rules and interfaces, not Contract authority.

## `quality`

Testing, validation, benchmarking, determinism, and release-quality material.
