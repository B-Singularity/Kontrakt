# Kontrakt Documentation

This directory contains Kontrakt's design, architecture decisions, implementation planning, quality material, and
theoretical foundation.

The documents do not all carry the same authority. A document should be read according to the responsibility it owns
rather than merely by how detailed or recent it is.

## `the-most-important-thing`

This directory contains the theoretical foundation of Kontrakt.

`What Contract Is` defines the highest-level principles of Contract meaning. Compiler architecture, generated artifacts,
runtime mechanisms, caches, storage layouts, host-language structures, and other implementation choices must not
silently replace the meaning owned here.

## `adr`

This directory contains architecture decisions.

ADR numbering is historical and remains stable. Current documents are organized by responsibility so that Contract
semantics and compiler architecture are easier to navigate without renumbering established decisions.

The current Contract-oriented categories include:

- `interface-api`
- `data-model`
- `one-dimensional`
- `establishment`
- `governance`
- `composition`
- `outcomes`

Compiler-oriented categories include:

- `compiler-structure`
- `frontend`
- `ir`
- `realization`
- `optimization`
- `backend`
- `compiler-engine`
- `diagnostics`
- `verification`
- `runtime`
- `infrastructure`

The category does not create authority. Each ADR owns only the decision stated by that ADR and remains constrained by
`What Contract Is` and any higher-level Constitution that applies to its subject.

## `design`

This directory contains current compiler and system design.

Design documents describe replaceable architecture and implementation structure beneath accepted Contract and ADR
obligations. They may define data structures, algorithms, storage organization, query and cache realization, verifier
structure, backend machinery, build mechanisms, or other concrete engineering choices.

A Design document must not acquire Contract authority merely because several subsystems depend on it.

## `todo`

This directory contains open work, unresolved architecture research, implementation plans, migration work, and
later-version investigation.

TODO documents are not accepted semantic or architectural authority. They may identify problems that require changes to
an owning ADR, Constitution, Design, or implementation.

## `compiler-protocols`

This directory contains shared compiler protocols used across compiler responsibilities.

A compiler protocol defines a legal compiler-facing observation, exchange, or compatibility boundary where that common
boundary is useful across multiple compiler subsystems. Compiler protocols do not create Contract authority and do not
replace the semantic owner of the material they expose.

Physical access, storage, caching, scheduling, and transport remain replaceable unless a protocol explicitly owns a
compiler-level compatibility obligation.

## Security documentation

Security is not maintained as a separate top-level documentation universe. Security documents remain in the existing
Kontrakt document families according to the kind of responsibility they own.

The Security Constitution belongs under `docs/constitution/security/`. It defines architecture-neutral security
obligations that must survive replacement of compiler stages, storage, caches, backends, verification tools, build
systems, and other mechanisms. It does not redefine Contract meaning or create a second Contract authority.

Security Architecture Decision Records belong under `docs/adr/security/` and use the independent identifier series
`SADR-xxxx`. The separate identifier distinguishes security architecture decisions from the historical `ADR-xxxx`
series; it does not make security architecture a parallel semantic system. A SADR applies the Security Constitution to
an architectural subject and must defer to the Contract or compiler document that already owns the underlying meaning.

Concrete security mechanisms belong with Design under `docs/design/security/`. This includes implementation choices such
as verifier structure, disclosure mediation, cache and persistence protection, isolation, cryptographic mechanisms,
build and release controls, and other replaceable security machinery.

Security verification and assurance material belongs under the existing verification family, for example
`docs/verification/security/`. Review records, adversarial and regression suites, assumption ledgers, release gates,
fault-injection evidence, and similar artifacts provide evidence about the architecture and implementation. They are not
source authority and do not become the judgment they support.

Security research and external reference material belongs under `docs/reference/security/` when retained in the
repository. Research may reveal a defect or justify a change, but it remains non-normative until the finding is adopted
by the document that owns the affected obligation or architecture. Open security work may remain under the corresponding
`docs/todo/` area until that ownership is resolved.

The intended relation is:

```text
What Contract Is / applicable Constitution
        ↓
owning Contract or compiler meaning
        ↓
Security Constitution
        ↓
SADR
        ↓
Security Design
        ↓
Implementation / Verification / QA
```

This diagram does not mean that the Security Constitution can overwrite Contract meaning. When a security requirement
conflicts with an existing Contract obligation, the conflict must be resolved by the document that owns that meaning or
at the applicable constitutional level. A SADR or Security Design must not patch the conflict by introducing shadow
semantics.

No `docs/security/` root is required. The root documentation index remains this `docs/README.md`, while security
material is placed by document role in the existing hierarchy.

## `quality`

This directory contains testing, validation, benchmarking, determinism, and release-quality material.

Quality documents provide evidence that the implementation satisfies its declared obligations. They do not redefine
Contract meaning or architecture merely because a test, benchmark, or validation procedure depends on a particular
current realization.