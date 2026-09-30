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

## Security

Security is documented inside the existing Kontrakt hierarchy rather than in a separate `docs/security/` tree. The
subject cuts across Contract semantics, compiler architecture, implementation, and verification, so each document stays
with the kind of authority it actually carries. This section defines where those documents belong and how they relate.

### Security Constitution

The Security Constitution is kept in `docs/constitution/security/`. It states security obligations that should remain
valid when the implementation changes, without taking ownership of Contract meaning.

Its role is to protect meaning that is already owned elsewhere. If a security review reveals that an existing Contract
rule is wrong or incomplete, the correction must go back to the Contract document that owns that rule. The Security
Constitution cannot repair such a conflict by introducing a second interpretation.

### Security ADR

Security architecture decisions are kept in `docs/adr/current/security/` and use the `SADR-xxxx` series. The separate
prefix distinguishes security architecture decisions from the historical `ADR-xxxx` sequence, while the documents still
follow the same current, historical, and migration lifecycle used by the ADR tree.

A SADR turns a constitutional security obligation into an architectural decision for a defined subject. It may constrain
how the compiler establishes trust, preserves a security judgment, or reacts when required assurance cannot be
established. It must not replace the meaning owned by the Contract or compiler document that defines the subject itself.

### Security Design

Concrete security realization belongs in `docs/design/security/`. Design documents explain how the current
implementation satisfies the obligations fixed above them.

A verifier arrangement, an isolation boundary, a cryptographic mechanism, or a build-control mechanism belongs here when
another realization could replace it without changing the obligation. Dependence on the current mechanism does not give
that mechanism semantic authority.

### Security Verification

Security evidence belongs in `docs/verification/security/`. Reviews, adversarial tests, regression material, assurance
records, and release checks belong here when their purpose is to determine whether the current system still satisfies an
existing security claim.

Evidence may expose a defect, but it does not become the rule that repairs the defect. The resulting change must be made
in the Constitution, ADR, SADR, Design, or implementation that owns the failed obligation.

### Security TODO and Reference

Unresolved security work belongs under `docs/todo/security/` while the required owner or decision is still open.
External material retained for study belongs under `docs/reference/security/` once that reference area is needed.

Research remains non-normative until its result is adopted by the document that owns the affected obligation or
architecture. A paper, incident report, standard, or tool recommendation can justify a change, but it cannot acquire
authority merely by being cited.

Taken together, these documents form one security documentation family without creating a second semantic hierarchy.
Contract documents continue to own Contract meaning. The Security Constitution states the long-lived protection
obligations around that meaning, SADRs decide how those obligations constrain architecture, Design records the current
realization, and Verification checks whether the declared guarantees still hold.

## `quality`

Testing, validation, benchmarking, determinism, and release-quality material.