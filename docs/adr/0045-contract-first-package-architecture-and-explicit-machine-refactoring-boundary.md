# ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary

## Status

Draft

## Date

2026-06-25

## Related

- `docs/what-contract-is.md`
- ADR-0044: Unified Runtime Memory Envelope and Pipeline Lifecycle Governance
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation
- ADR-0042: Mechanical Sympathy, Primitive Lifecycle, and Async Ownership Governance
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure
- ADR-0037: Cycle Identity Preflight and Deferred Raw Fact Resolution
- ADR-0035: Deterministic M:N Dispatch Lanes for Tier-2 Join Completion Delivery
- ADR-0034: Explicit Dual-Axis L2 Join Lifecycle State Machine and Single Terminalization Authority
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics
- ADR-0031: Two-Tier Transactional Memoization and Structural Interning
- ADR-0026: Abstraction of Type Introspection and Execution
- ADR-0025: Interface-First Design and Test Interface Pattern
- `docs/constitution/compiler-core-protocols.md`
- `docs/design/canonical-ir-stage-and-lowering-protocol.md`
- `docs/design/stable-metadata-identity-protocol.md`
- `docs/design/protocol-owned-metadata-interning.md`
- `docs/release-readiness-todo.md`

---

## 1. Context

Kontrakt has moved beyond its original shape as a test automation framework.

Earlier architecture decisions introduced discovery, linking, execution, reporting, tracing, planning, frozen metamodel
material, L1 planner structures, L2 interning, runtime policy, lifecycle governance, adapter-neutral acquisition, and
stable identity protocols.

Much of that work remains valuable.

The current implementation already contains strong deterministic laws:

- backend handles are erased before frozen planning material;
- adapter discovery order is not trusted as semantic order;
- canonical material must be separated from physical representation;
- transitional hash values are not persistent identity;
- frozen material is published only after validation;
- lifecycle states are explicit in several runtime subsystems;
- L1 and L2 planning structures avoid hidden semantic dependence on cache behavior;
- runtime policy is resolved before expensive work begins;
- failure, quarantine, continuation, and bounded diagnostics are treated as governed paths.

However, the current package structure was created before the current contract theory was clarified in
`What Contract Is`.

The current codebase still exposes older implementation vocabulary near the top of the architecture:

``````text
discovery
linking
execution
reporting
metamodel
planning
runtime
interceptor
trace
test result
scenario
``````

Some of these names are still useful, but only as implementation vocabulary. Others are just old transitional names. A
few now point the reader toward the wrong architecture.

After `What Contract Is`, the authority model is clearer: Kontrakt should be read first as a contract machine. Test
execution, compiler-style processing, runtime management, reflection, metadata acquisition, and planning are all
important, but they are machinery under that contract machine. They are not the product domain itself.

A contract is the declared set of obligations that software must satisfy. Implementation may read, lower, normalize,
canonicalize, verify, execute, publish, cache, and report, but implementation machinery must not become contract
authority.

The package architecture must now reflect that rule.

If package roots are organized around compiler machinery, interceptor flow, execution plumbing, reflection surfaces, or
legacy test framework vocabulary, the codebase teaches the wrong architecture.

The structure must say what Kontrakt is before it says how Kontrakt realizes that meaning.

## 2. Problem

The current package architecture has four structural problems.

### 2.1. Implementation vocabulary is too high in the package tree

Packages such as `execution`, `planning`, `metamodel`, `reporting` are not wrong by themselves. The
problem is their position. When they sit near the top of the package tree, they start to look like product-domain
authority, even though they are mostly realization machinery.

This matters because Kontrakt deliberately uses compiler-like architecture. That architecture is useful, but it must
stay in its lane: it realizes declared contract authority. It does not define that authority.

### 2.2. Callback and interceptor flow hide machine authority

Some earlier pipeline code uses interceptor-style flow control. In that structure, the next legal move depends on
whether an interceptor calls `proceed`. That makes the transition implicit. The machine still moves, but the rule for
movement is hidden inside callback behavior instead of being declared as machine law.

That does not match `What Contract Is`. A good machine has to expose the conditions under which it may move. Legal
movement should not be buried in callback chains, recursive interceptor delegation, method-local orchestration tricks,
or framework-style continuation calls.

### 2.3. Deterministic implementation work is not yet aligned under contract vocabulary

Several implementation areas already move in the correct direction. Examples include frozen metamodel material,
adapter-neutral raw facts, canonical type references, explicit type shape ratification, planner session primitive
structures, L2 lifecycle laws, runtime policy epochs, dispatch lane lifecycle states, and bounded diagnostic/reporting
paths.

So the problem is not that the implementation has to be thrown away. The problem is that good deterministic
implementation is still sitting under package names that do not explain its role in the contract machine.

The codebase must separate:

``````text
contract authority
machine law
boundary law
material law
judgment law
governance law
diagnostic evidence
publication claim
realization machinery
outside adapters
``````

### 2.4. Package movement and semantic replacement are being conflated

The architecture must change substantially, but package relocation and semantic replacement are different operations.
Moving files affects package declarations, imports, directory structure, visibility, dependency rules, build
configuration, and architecture tests. Replacing interceptor flow changes execution semantics.

Those two changes must not be mixed in the first pass. The first pass should make the architectural boundary visible
without changing behavior. Incompatible flow structures should be replaced only after the project compiles and current
behavior is preserved.

## 3. Decision

Kontrakt will adopt a contract-first package architecture.

The top-level package structure must be organized around Kontrakt's product domain and contract theory, not around
compiler phases, test framework machinery, reflection surfaces, or runtime implementation techniques.

The top-level architectural vocabulary is:

``````text
contract
machine
boundary
material
judgment
governance
diagnostics
publication
realization
adapter
``````

The central rule is:

``````text
contract authority first
realization machinery second
outside technology behind adapters
``````

Compiler architecture, planning, execution, metamodel acquisition, identity derivation, graph operations, runtime
storage,
and reporting engines are realization machinery unless a specific type is explicitly promoted into contract, machine,
material, judgment, governance, diagnostics, or publication authority.

Compiler architecture is retained, but it belongs under `realization.compiler`. Interceptor-style flow is also kept for
the first pass, but only as transitional machinery: it must be moved, quarantined, and later replaced by explicit
machine/state/transition flow.

## 4. Authority and Boundaries

ADR-0045 owns:

- contract-first top-level package law;
- package authority boundaries;
- realization boundary law;
- adapter boundary law;
- compiler-as-realization placement law;
- interceptor transition quarantine law;
- behavior-preserving relocation sequence;
- dependency direction rules for the new package architecture;
- and architecture-test requirements for package dependency enforcement.

ADR-0045 does not own:

- final contract fact taxonomy;
- final public contract syntax;
- exact compiler frontend implementation;
- exact IR schema;
- canonical metadata byte encoding;
- HID derivation;
- frozen acquisition state machine internals;
- L1 planner primitive data structure mechanics;
- L2 interner storage mechanics;
- runtime memory envelope vector;
- report artifact schema;
- or the explicit execution machine replacement itself.

Ownership split:

| Surface                                    | Owner                                |
|--------------------------------------------|--------------------------------------|
| top-level contract meaning                 | `What Contract Is`                   |
| package authority law                      | ADR-0045                             |
| compiler constitution and low-level rules  | compiler-core protocols              |
| canonical metadata identity                | ADR-0041 and identity protocol notes |
| primitive lifecycle and physical substrate | ADR-0042                             |
| contract graph identity                    | ADR-0043                             |
| unified memory envelope                    | ADR-0044                             |
| frozen acquisition lifecycle               | ADR-0040                             |
| explicit L2 lifecycle                      | ADR-0034 / ADR-0035                  |
| package relocation execution               | ADR-0045 implementation plan         |
| interceptor semantic replacement           | future explicit machine ADR          |

## 5. Vocabulary

### 5.1. Contract authority package

A contract authority package contains declared obligation meaning.

It must not depend on compiler passes, runtime storage, reflection handles, test framework concepts, or adapter-specific
objects.

### 5.2. Machine package

A machine package contains explicit machine law.

It may define states, transitions, manifests, stage legality, lifecycle closure, and legal movement between declared
machine conditions.

Machine law is not callback flow.

### 5.3. Boundary package

A boundary package contains outside-facing material admission surfaces.

It owns input, DTO, admission, disposition, and external-material boundary concepts.

### 5.4. Material package

A material package contains software material after it is brought under Kontrakt-owned law.

It may contain raw, normalized, canonical, lowered, frozen, identity, and versioned material.

### 5.5. Judgment package

A judgment package contains decision results made by the machine under declared contract law.

Admission judgment, invariant judgment, state judgment, transition judgment, failure judgment, and publication judgment
belong here when they are domain judgments rather than implementation mechanisms.

### 5.6. Governance package

A governance package contains policy, budget, capacity, capability, epoch, and resource admission law.

Governance is not arbitrary configuration.

Governance is resolved machine law.

### 5.7. Diagnostics package

A diagnostics package contains bounded evidence, trace, retention, redaction, and diagnostic summary material.

Diagnostic material explains judgment.

It is not contract authority by itself.

### 5.8. Publication package

A publication package contains public claim, denial, exposure, report, and artifact material.

Publication is a judgment-controlled exposure surface.

It is not a raw dump of internal implementation state.

### 5.9. Realization package

A realization package contains implementation machinery used to realize contract authority.

Compiler, metamodel, identity, graph, planning, execution, runtime, and reporting engines belong here unless a specific
piece is promoted into one of the authority packages.

### 5.10. Adapter package

An adapter package contains outside-world technology bindings.

Reflection, KSP, ClassGraph, JUnit, Mockito, JSON, file, console, and platform-specific code belong here.

Adapters may feed the system.

Adapters must not become contract authority.

## 6. Package Authority Law

The package tree MUST obey this law:

``````text
contract authority must not depend on realization architecture.
realization architecture may depend on contract authority.
adapters may feed realization machinery.
adapters must not become contract authority.
``````

This implies:

- `contract` must not depend on `realization`;
- `contract` must not depend on `adapter`;
- `machine` must not depend on `adapter`;
- `material` authority must not depend on backend handles;
- `judgment` must not depend on framework callbacks;
- `governance` must not depend on environment inspection inside core law;
- `diagnostics` must not smuggle implementation authority into public claims;
- `publication` must not publish raw internal state as a claim;
- `realization` may depend inward;
- `adapter` may depend inward and outward as needed to isolate outside technology.

## 7. Target Package Shape

The target package shape is:

``````text
io.kontrakt

├── contract
│   ├── presentation
│   ├── obligation
│   ├── interfacecontract
│   ├── operation
│   ├── invariant
│   ├── state
│   ├── transition
│   ├── failure
│   ├── diagnostic
│   ├── publication
│   ├── governance
│   └── version
│
├── machine
│   ├── manifest
│   ├── state
│   ├── transition
│   ├── pipeline
│   ├── stage
│   └── lifecycle
│
├── boundary
│   ├── input
│   ├── dto
│   ├── admission
│   ├── disposition
│   └── external
│
├── material
│   ├── raw
│   ├── normalized
│   ├── canonical
│   ├── lowered
│   ├── frozen
│   ├── identity
│   └── version
│
├── judgment
│   ├── admission
│   ├── invariant
│   ├── state
│   ├── transition
│   ├── publication
│   └── failure
│
├── governance
│   ├── policy
│   ├── budget
│   ├── capacity
│   ├── capability
│   └── epoch
│
├── diagnostics
│   ├── evidence
│   ├── trace
│   ├── retention
│   ├── redaction
│   └── summary
│
├── publication
│   ├── claim
│   ├── denial
│   ├── exposure
│   ├── report
│   └── artifact
│
├── realization
│   ├── compiler
│   │   ├── frontend
│   │   ├── syntax
│   │   ├── semantic
│   │   ├── ir
│   │   ├── pass
│   │   ├── analysis
│   │   ├── checking
│   │   ├── generation
│   │   └── emission
│   │
│   ├── metamodel
│   ├── identity
│   ├── graph
│   ├── planning
│   ├── execution
│   ├── runtime
│   └── reporting
│
└── adapter
    ├── reflection
    ├── ksp
    ├── junit
    ├── mockito
    ├── classgraph
    ├── file
    ├── json
    └── console
``````

The exact JVM group prefix may be adjusted by module policy.

The architectural vocabulary is not optional.

## 8. Compiler Boundary Law

Compiler architecture is accepted only as realization machinery.

The following package shape is allowed under `realization.compiler`:

``````text
realization.compiler
├── frontend
├── syntax
├── semantic
├── ir
├── pass
├── analysis
├── checking
├── generation
└── emission
``````

The compiler may:

- read syntax;
- interpret declared contract presentations;
- lower syntax into internal material;
- run static checks;
- generate enforcement machinery;
- emit generated artifacts;
- compare generated projection behavior with reference judgment behavior.

The compiler must not create contract authority; it only realizes declared contract authority.

## 9. Interceptor Quarantine Law

Interceptor-style flow is not accepted as the final machine model.

The following are transitional:

``````text
ScenarioInterceptor
ScenarioExecutionChain
AuditingInterceptor
ResultResolverInterceptor
callback-driven proceed flow
recursive interceptor chain delegation
``````

This ADR does not delete those files. It only classifies them and requires them to be moved under a transitional
realization package during the relocation pass.

Suggested temporary package:

``````text
io.kontrakt.realization.execution.transitional.interceptor
``````

That package must not be treated as final architecture.

The issue is not that interceptors are always invalid in ordinary software. The issue is narrower: Kontrakt's machine
law requires explicit movement. A `proceed` call is not a transition manifest, a recursive chain is not an explicit
machine, and a callback is not state authority.

The replacement direction is:

``````text
explicit machine manifest
-> explicit state set
-> explicit transition set
-> explicit stage law
-> explicit judgment result
-> explicit diagnostic evidence
-> explicit publication claim or denial
``````

The exact replacement belongs to a later ADR.

## 10. Behavior-Preserving Relocation Law

The first implementation pass MUST be movement-only.

Allowed changes:

- directory movement;
- package declaration updates;
- import updates;
- build configuration updates required by package movement;
- temporary compatibility aliases when required;
- architecture test scaffolding;
- deprecation or transition markers for quarantined files.

Forbidden changes in the first pass:

- deleting interceptor files;
- changing execution semantics;
- changing planning behavior;
- changing metamodel identity law;
- changing frozen publication behavior;
- changing canonical ordering behavior;
- changing L1/L2 runtime behavior;
- changing diagnostics retention behavior;
- changing report output semantics;
- mixing algorithmic changes with package relocation.

Reason:

``````text
Move first.
Then replace.
``````

A package-authority refactor touches too many files to be safely mixed with semantic changes.

## 11. Initial Relocation Guide

This guide is non-exhaustive.

It exists to make the first movement pass reviewable.

### 11.1. Discovery

Current area:

``````text
discovery.api
discovery.adapter
discovery.domain
``````

Target direction:

``````text
contract.presentation
boundary.input
boundary.admission
realization.compiler.frontend
adapter.classgraph
adapter.jvm
``````

Discovery that reads classpath, annotations, or outside runtime surfaces belongs to adapters or compiler frontend.

Discovery results that become software-visible contract presentation material must move toward contract, boundary, or
material packages according to authority.

### 11.2. Linking

Current area:

``````text
linking
``````

Target direction:

``````text
realization.graph
realization.execution
material.lowered
judgment.admission
``````

Binding and linking are realization mechanics unless the linked object becomes a ratified material or judgment concept.

### 11.3. Metamodel and frozen material

Current area:

``````text
metamodel.domain
metamodel.adapter.reflection
metamodel.domain.frozen
``````

Target direction:

``````text
material.raw
material.normalized
material.canonical
material.frozen
material.identity
realization.metamodel
adapter.reflection
``````

Reflection-specific material must stay outside authority.

Adapter-neutral frozen material may move under `material.frozen` when it expresses ratified material law.

Acquisition mechanics may remain under `realization.metamodel`.

### 11.4. Planning

Current area:

``````text
planning.domain.expansion
planning.domain.projection
planning.domain.interner
planning.domain.runtime
planning.infrastructure.runtime
``````

Target direction:

``````text
realization.planning
realization.runtime
material.canonical
material.lowered
material.identity
governance.budget
governance.capacity
machine.lifecycle
``````

Planning algorithms are realization machinery.

Canonical or lowered material produced by planning may live under `material` when it becomes ratified material.

Lifecycle law may move under `machine` when it expresses legal machine movement rather than physical storage mechanics.

### 11.5. Execution

Current area:

``````text
execution.domain
execution.port
execution.infrastructure
``````

Target direction:

``````text
realization.execution
machine
judgment
diagnostics
publication
adapter.junit
``````

Execution is realization.

Result resolution is judgment.

Trace and audit evidence are diagnostics.

Externally visible reports and results are publication.

### 11.6. Runtime and policy

Current area:

``````text
planning.domain.runtime
planning.infrastructure.runtime
runtime policy files
worker lifecycle files
``````

Target direction:

``````text
realization.runtime
governance.policy
governance.budget
governance.capacity
governance.epoch
machine.lifecycle
``````

Policy resolution belongs to governance when it defines machine law.

Worker backing, storage, lanes, and physical lifecycle belong to realization runtime unless promoted to explicit machine
law.

### 11.7. Reporting

Current area:

``````text
reporting
console reporter
json reporter
html reporter
trace sinks
``````

Target direction:

``````text
publication.report
publication.artifact
diagnostics.summary
diagnostics.trace
adapter.console
adapter.file
adapter.json
``````

A report is not contract authority. It is publication or diagnostic material derived from accepted judgment/evidence.

## 12. Dependency Direction Law

The following dependencies are forbidden:

``````text
contract -> realization
contract -> adapter
machine -> adapter
material.frozen -> adapter.reflection
material.frozen -> adapter.ksp
judgment -> adapter
governance -> adapter
publication core claim -> adapter
``````

The following dependencies are allowed:

``````text
realization -> contract
realization -> machine
realization -> material
realization -> judgment
realization -> governance
realization -> diagnostics
realization -> publication
adapter -> realization
adapter -> contract presentation surfaces when required to read external declarations
``````

Any allowed dependency must still respect the narrower law of the target package.

Allowed direction does not imply unrestricted coupling.

## 13. Determinism Law

This refactor must preserve and strengthen determinism.

Package structure must make deterministic authority visible.

The following rules remain binding:

- input order is not semantic order unless ratified;
- backend enumeration order is not semantic order;
- reflection order is not semantic order;
- hash match is not equality;
- JVM `hashCode()` is not persistent identity;
- adapter handles are not contract material;
- callback continuation is not transition authority;
- wall-clock time is not semantic identity;
- publication requires validation;
- failure must be declared and diagnosable;
- resource exhaustion must fail closed;
- budget exhaustion must fail closed;
- ordering law must be explicit, stable, and testable.

This refactor is not aesthetic. Its purpose is to make deterministic laws difficult to violate accidentally.

## 14. Explicit Machine Reservation

This ADR reserves a follow-up explicit machine refactoring.

That work should define or ratify concepts such as:

``````text
MachineManifest
StateManifest
TransitionManifest
StageManifest
StageJudgment
FailureJudgment
DiagnosticEvidence
PublicationJudgment
``````

Names may change, but the authority rule may not:

``````text
legal movement belongs to the explicit machine manifest,
not to callback behavior.
``````

## 15. Architecture Tests

The package refactor must introduce architecture tests.

Required test categories:

1. **forbidden dependency tests**

   Verify that authority packages do not depend on realization or adapter packages.

2. **adapter isolation tests**

   Verify that reflection, KSP, ClassGraph, JUnit, Mockito, JSON, file, and console implementation details do not leak
   into contract authority packages.

3. **transitional interceptor containment tests**

   Verify that transitional interceptor packages are not depended on by contract, machine, material, judgment,
   governance, diagnostics, or publication authority packages.

4. **material purity tests**

   Verify that frozen/canonical material packages do not depend on backend handles or adapter-specific types.

5. **realization inward dependency tests**

   Verify that realization depends inward on authority packages rather than authority packages depending outward on
   realization.

## 16. Compliance Rules

A change complies with this ADR only if all of the following are true:

1. New top-level package names use contract-first vocabulary.
2. Compiler-specific names remain under `realization.compiler`.
3. Runtime, planning, graph, metamodel, identity, execution, and reporting machinery remain under `realization` unless
   explicitly promoted by a later ADR.
4. Adapter-specific code remains under `adapter`.
5. No behavior changes are mixed into the first relocation pass.
6. Interceptor-style flow is moved only as transitional machinery.
7. No interceptor-style file is deleted by this ADR.
8. Architecture tests protect the new dependency direction.
9. Existing deterministic material, identity, planning, runtime, and publication laws remain intact.
10. Any semantic replacement after relocation receives its own decision boundary.

## 17. Alternatives Considered

### 17.1. Use compiler architecture as the top-level package structure

Rejected.

A compiler-style root such as:

``````text
frontend
middleend
backend
optimizer
emitter
``````

would explain implementation mechanics while hiding Kontrakt's product domain.

Kontrakt uses compiler architecture, but that architecture is not contract authority.

### 17.2. Keep the current package structure and only rename individual classes

Rejected.

The problem is not only naming. The current structure still groups responsibilities according to older test-framework,
discovery, execution, and interceptor-era architecture, so small renames would preserve the same authority error.

### 17.3. Delete incompatible files during package movement

Rejected for this ADR.

Some files are conceptually incompatible with the final direction, especially interceptor-driven flow. Deleting or
rewriting them during package relocation would combine two large changes, so the first pass must stay reviewable and
reversible.

### 17.4. Put all contract-theory vocabulary at the root without a realization boundary

Rejected.

A pure contract-vocabulary root without `realization` would eventually turn authority packages into implementation trash
bins.

Kontrakt needs both:

``````text
contract authority vocabulary
realization machinery boundary
``````

That boundary must stay explicit.

### 17.5. Treat pipeline as the top-level package authority

Rejected.

Pipeline is a useful orchestration concept.

Pipeline is not automatically contract authority.

A pipeline stage becomes contract-relevant only when a declared obligation, judgment, material law, or explicit machine
transition is attached to it.

Pipeline must live under machine or realization according to its role.

## 18. Consequences

### 18.1. Positive consequences

- The package structure will reflect `What Contract Is`.
- Contract authority will be separated from compiler/runtime realization.
- Deterministic laws will become easier to locate and protect.
- Adapter-specific technology will be isolated more clearly.
- Interceptor-style hidden flow will be quarantined before replacement.
- Large refactoring becomes reviewable because movement and behavior changes are separated.
- Future contributors will see that Kontrakt is a contract machine, not a test framework or compiler clone.

### 18.2. Negative consequences

- The refactor will touch many files.
- Imports will churn heavily.
- Existing ADR references and documentation may temporarily point to old package names.
- Tests may require broad import updates.
- Transitional packages may feel redundant until the explicit machine replacement is complete.
- Architecture tests must be added before the package boundary is fully stable.

### 18.3. Accepted cost

The churn is accepted because the current package structure still tells an outdated architectural story. Keeping it
would preserve short-term convenience, but it would keep pushing the project back toward the wrong mental model.

Kontrakt's package structure must make the machine's authority visible.

## 19. Implementation Plan

### 19.1. Ratify package law

Create the target package tree and dependency direction law from this ADR.

### 19.2. Add architecture tests

Add package dependency tests before or during movement so violations are visible early.

### 19.3. Move files without semantic edits

Move files according to the target package structure.

Only package declarations, imports, directory paths, build configuration, and temporary compatibility wrappers are
allowed.

### 19.4. Quarantine transitional interceptor files

Move interceptor-era files into a transitional realization package.

Do not delete them in this pass.

### 19.5. Compile and run current tests

The movement pass is complete only after current behavior is preserved.

### 19.6. Prepare explicit machine ADR

After relocation, prepare a follow-up ADR for replacing callback/interceptor flow with explicit machine/state/transition
flow.

## 20. Final Rule

Kontrakt's package structure must describe contract authority before it describes realization machinery.

Compiler architecture is internal machinery.

Planning, execution, runtime, metamodel, identity, graph, and reporting are realization machinery unless explicitly
promoted by contract authority.

Adapters isolate outside technology.

Interceptor-style flow is transitional.

The first refactoring pass moves files only.

No incompatible file is deleted or semantically rewritten until the package relocation is complete and reviewed.