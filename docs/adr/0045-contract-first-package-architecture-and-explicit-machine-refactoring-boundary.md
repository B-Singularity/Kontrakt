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

```text
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
```

Some of these names are still useful, but only as implementation vocabulary. Others are old transitional names. A few
now point the reader toward the wrong architecture.

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

The current package architecture has five structural problems.

### 2.1. Implementation vocabulary is too high in the package tree

Packages such as `execution`, `planning`, `metamodel`, `reporting`, and `runtime` are not wrong by themselves. The
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

```text
contract authority
machine law
stage-local contract domains
stage-local material law
stage-local judgment law
governance law
diagnostic evidence
publication claim
realization machinery
outside adapters
```

### 2.4. Stage-local contract roles are at risk of being collapsed into global buckets

A pipeline stage is not just a function call or a folder. A substantial stage may have its own contract declarations,
boundary material, admitted material, rejected material, judgment result, failure vocabulary, diagnostic evidence, and
publication eligibility.

If packages are organized only by role at the root, then all stages eventually share the same global buckets:

```text
boundary
material
judgment
diagnostics
publication
```

That looks tidy, but it hides which stage owns which obligation.

Admission judgment, lowering judgment, invariant judgment, state judgment, transition judgment, and publication judgment
are not the same thing just because they are all judgments. Each exists at a different machine point and protects a
different transition.

Therefore, the package architecture must be stage-first for pipeline-specific material.

### 2.5. Package movement and semantic replacement are being conflated

The architecture must change substantially, but package relocation and semantic replacement are different operations.
Moving files affects package declarations, imports, directory structure, visibility, dependency rules, build
configuration, and architecture tests. Replacing interceptor flow changes execution semantics.

Those two changes must not be mixed in the first pass. The first pass should make the architectural boundary visible
without changing behavior. Incompatible flow structures should be identified as removal/replacement work, not renamed
into the new architecture as if they were accepted design elements.

## 3. Decision Process

The package decision is not only a naming decision. It is also a boundary decision.

The question was whether stage boundaries should be enforced as physical build modules, as packages inside the current
core module, or as global role packages shared by every stage.

The decision was evaluated against these criteria:

- whether contract authority stays visible;
- whether each stage can hide its internal implementation;
- whether deterministic laws remain easy to test;
- whether the first refactor can preserve behavior;
- whether the structure avoids speculative future layers;
- whether Kotlin/JVM limitations are acknowledged instead of hidden;
- whether the project can later grow into multiple pipeline modules without another conceptual rewrite.

### 3.1. Alternative A: one module per stage now

This would make each stage a Gradle module.

Example shape:

```text
kontrakt-stage-admission
kontrakt-stage-canonicalization
kontrakt-stage-lowering
kontrakt-stage-publication
```

The advantage is strong physical isolation.

A module can expose only its intended API. Kotlin `internal` visibility becomes module-scoped. Gradle `api` and
`implementation` dependencies can prevent some illegal access at compile time. This would make it harder for one stage
to reach another stage's internal helpers by accident.

That is attractive, but it is too early for the current refactor.

The current codebase has one primary pipeline, and the stage vocabulary is still being aligned with `What Contract Is`.
Turning every stage into a module now would freeze a physical build boundary before the conceptual boundary is stable
enough. It would also mix a package-authority refactor with Gradle graph design, API surface design, dependency
publication, source-set movement, and build-performance concerns.

The expected costs are high:

- too many modules too early;
- more Gradle configuration during an already large refactor;
- more API/implementation churn while names are still moving;
- premature module dependency edges;
- possible circular dependency pressure before material boundaries are fully extracted;
- harder review because package movement and build-boundary design would be mixed;
- more friction when files still need to move again after explicit machine replacement.

Runtime efficiency is not the main objection. A module boundary does not automatically make runtime execution slower.
The issue is architectural timing: module boundaries are harder to move than package boundaries, and this ADR is still
establishing the vocabulary and stage ownership law.

Decision: rejected for the first pass.

Stage modules may be introduced later when a pipeline boundary is stable enough to deserve a physical build boundary.

### 3.2. Alternative B: role-first packages

This would make roles the first package split.

Example shape:

```text
material.admission
material.lowering
material.publication
judgment.admission
judgment.lowering
judgment.publication
```

The advantage is that shared role vocabulary is easy to see. All material types live under `material`, all judgment
types live under `judgment`, and so on.

The problem is that a pipeline stage stops being visible as a coherent contract domain.

Admission material, admission judgment, admission diagnostics, and admission failure vocabulary belong together because
they protect one machine point. Splitting them across global role packages makes the reader reconstruct the stage
mentally. It also makes global buckets grow over time.

Decision: rejected.

Role names should repeat under stage packages. The repetition is intentional because each stage owns its own local
contract world.

### 3.3. Alternative C: pipeline-name package layer now

This would introduce a pipeline layer even though the current core has one primary pipeline.

Example shape:

```text
io.kontrakt.pipeline.core.stage.admission.contract
io.kontrakt.pipeline.core.stage.lowering.material
```

The advantage is future readability if Kontrakt later contains several independent pipeline families.

The problem is that this is speculative. The current project does not yet need multiple pipeline modules. Adding a
pipeline-name layer now would make every package longer while pretending that a future boundary already exists.

Decision: rejected for the current codebase.

If multiple independent pipeline families appear later, the pipeline boundary should be introduced as a module boundary
first. Inside that module, the same stage-first package law applies.

### 3.4. Alternative D: package-first stage separation inside the current core module

This is the accepted first-pass strategy.

The current core remains one module for now. Stages are separated by package structure:

```text
io.kontrakt.stage.<stage-name>.<role>
```

A stage may expose only narrow public surfaces:

```text
stage.<stage>.contract
stage.<stage>.boundary
stage.<stage>.material
stage.<stage>.judgment
stage.<stage>.diagnostics
stage.<stage>.publication
stage.<stage>.machine
```

Its implementation helpers must stay out of that public surface and must not become dependency targets for other stages.

The advantage is that this preserves the conceptual boundary without prematurely committing to build modules. It keeps
the first refactor reviewable: package names, imports, architecture tests, and dependency direction can be changed
without redesigning the Gradle graph at the same time.

The weakness is that Kotlin/JVM packages are not a perfect access-control boundary. Kotlin has module-scoped `internal`,
not package-private visibility. Therefore, package structure alone cannot prevent every illegal dependency.

The accepted mitigation is architecture testing.

Architecture tests must reject imports from another stage's internal implementation area and must reject forbidden
dependency directions. A package boundary is therefore treated as a declared architectural boundary enforced by tests,
not as a hard JVM security boundary.

Decision: accepted for this ADR.

### 3.5. Future promotion rule

A stage may later be promoted into a separate module only when the boundary is stable enough to justify that cost.

The trigger is not aesthetic package cleanliness. The trigger is one of these conditions:

- multiple independent pipeline families exist;
- one pipeline needs to be built, tested, or released independently;
- a stage's public surface is stable enough to expose as a module API;
- module-level `internal` visibility is needed to prevent repeated boundary violations;
- build or dependency analysis shows that physical separation is worth the additional Gradle complexity.

Until then, the rule is:

```text
single pipeline:
    package-first stage separation
    architecture tests enforce forbidden dependencies

multiple pipelines:
    module-level pipeline separation
    stage packages inside each pipeline module
```

This preserves the current refactor's intent without pretending that today's single-pipeline core already has the shape
of a future multi-pipeline system.

## 4. Decision

Kontrakt will adopt a contract-first and stage-first package architecture.

The top-level package structure must be organized around Kontrakt's product domain and contract theory, not around
compiler phases, test framework machinery, reflection surfaces, or runtime implementation techniques.

The top-level architectural vocabulary is:

```text
contract
stage
governance
realization
adapter
```

`machine` is not listed here as a peer of `stage` in the current package layout. In this ADR, machine law is a role that
appears inside the stage that owns the movement. A shared machine vocabulary may be introduced only when a concept is
genuinely pipeline-wide and cannot be owned by one stage.

The central rule is:

```text
contract authority first
stage-local contract domains second
realization machinery third
outside technology behind adapters
```

The current codebase has one primary pipeline. Therefore, this ADR does not introduce a multi-pipeline package root yet.

For the current system, pipeline stages live under `stage.<stage-name>`. Each substantial stage may then contain its own
role packages, such as `contract`, `boundary`, `material`, `judgment`, `diagnostics`, `publication`, and, where the
stage owns legal movement, `machine`.

Future multi-pipeline architecture must not be modeled by dumping several pipelines into the same package tree. If
Kontrakt later grows multiple independent pipeline families, each pipeline should be separated by a module boundary
first. Inside that module, the same rule applies: pipeline module -> stages -> stage-local contract packages.

Compiler architecture, planning, execution, metamodel acquisition, identity derivation, graph operations, runtime
storage,
and reporting engines are realization machinery unless a specific type is explicitly promoted into shared contract
vocabulary,
stage-local material, stage-local judgment, stage-local machine law, governance, diagnostics, or publication authority.

Compiler architecture is retained, but it belongs under `realization.compiler`. Interceptor-style flow is different. It
is not part of the target architecture, and it should not be described as a package layer to keep. Existing interceptor
files are only evidence of the old execution model; they must be removed or replaced by an explicit
machine/state/transition solution after the relocation boundary is clear.

## 5. Authority and Boundaries

ADR-0045 owns:

- contract-first top-level package law;
- stage-first package law for pipeline-specific domains;
- single-pipeline package assumption for the current codebase;
- future multi-pipeline module boundary rule;
- package authority boundaries;
- realization boundary law;
- adapter boundary law;
- compiler-as-realization placement law;
- interceptor removal and replacement boundary law;
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
| stage-first package law                    | ADR-0045                             |
| future multi-pipeline module boundary      | ADR-0045                             |
| compiler constitution and low-level rules  | compiler-core protocols              |
| canonical metadata identity                | ADR-0041 and identity protocol notes |
| primitive lifecycle and physical substrate | ADR-0042                             |
| contract graph identity                    | ADR-0043                             |
| unified memory envelope                    | ADR-0044                             |
| frozen acquisition lifecycle               | ADR-0040                             |
| explicit L2 lifecycle                      | ADR-0034 / ADR-0035                  |
| package relocation execution               | ADR-0045 implementation plan         |
| interceptor removal/replacement solution   | future explicit machine ADR          |

## 6. Vocabulary

### 6.1. Shared contract authority package

A shared contract authority package contains declared obligation meaning that is not owned by one specific stage.

It must not depend on compiler passes, runtime storage, reflection handles, test framework concepts, or adapter-specific
objects.

A contract package is intentionally narrow. It may contain contract-facing interfaces, immutable declaration objects,
stable
value records, enums, sealed declaration shapes, and small structural types needed to name obligations. It must not
contain
algorithms that perform discovery, traversal, lowering, planning, caching, execution, publication, reflection,
scheduling,
I/O, or environment inspection.

JVM and Kotlin still require some implementation shape to represent immutable objects. Constructors, property accessors,
and private structural guards may therefore remain in contract packages as a platform representation limit. They do not
become contract authority by existing there. They must stay boring: accept already supplied material, store it, and
protect
the object from being malformed. Any meaningful computation belongs outside `contract`, usually under `stage`,
`realization`,
or `governance`, depending on what the computation does.

### 6.2. Stage package

A stage package contains the contract domain for one point in the current Kontrakt pipeline.

A stage package is not just an implementation folder. It is the place where a specific pipeline point names the
obligations, material, judgments, failures, evidence, publication conditions, and legal movements that belong to that
point.

A substantial stage may contain:

```text
stage.<stage-name>
├── contract
├── boundary
├── material
├── judgment
├── diagnostics
├── publication
└── machine
```

A stage should include only the roles it actually owns.

`machine` appears here as a stage-local role, not as a package peer of `stage`. This matters because the legal movement
of a stage should be read together with that stage's material, judgment, failure, and diagnostic vocabulary. It must not
be pulled upward into a global bucket unless the concept is deliberately shared across the whole pipeline.

If a type is shared across stages, it must be promoted deliberately into a shared package with a clear reason. Shared
placement must not be used just to avoid repeated package names.

### 6.3. Stage-local contract package

A stage-local contract package contains the obligations that apply to one specific stage.

It follows the same narrow rule as the shared `contract` package: interfaces, immutable declaration structures, enums,
sealed declaration shapes, stable value records, and small structural types are allowed. Algorithms are not allowed.

### 6.4. Stage-local boundary package

A stage-local boundary package contains the boundary surface for that stage.

For an early input or admission stage, this may include DTO and external-material admission surfaces. For later stages,
it may include the accepted input material that the stage is allowed to consume.

### 6.5. Stage-local material package

A stage-local material package contains material owned by that stage.

Examples include raw, admitted, normalized, canonical, lowered, frozen, rejected, failed, or publication-eligible
material, depending on the stage.

### 6.6. Stage-local judgment package

A stage-local judgment package contains the decisions made at that stage under declared contract law.

Admission judgment, lowering judgment, invariant judgment, state judgment, transition judgment, and publication judgment
should remain stage-local unless a shared judgment vocabulary is intentionally promoted.

### 6.7. Stage-local machine package

A stage-local machine package contains the explicit movement law owned by that stage.

It may define the states, transition names, stage legality, terminal conditions, and legal movement rules that belong to
that particular stage.

Stage-local machine law is not callback flow. It is also not a global package layer that sits beside `stage`. If the
reader sees `machine` before the stage that owns it, the structure is already pointing in the wrong direction.

### 6.8. Governance package

A governance package contains policy, budget, capacity, capability, epoch, and resource admission law.

Governance is not arbitrary configuration.

Governance is resolved machine law.

### 6.9. Stage-local diagnostics package

A stage-local diagnostics package contains bounded evidence, trace, retention, redaction, and diagnostic summary
material for that stage.

Diagnostic material explains judgment.

It is not contract authority by itself.

### 6.10. Stage-local publication package

A stage-local publication package contains public claim, denial, exposure, report, and artifact material produced by
that stage when that stage owns publication responsibility.

Publication is a judgment-controlled exposure surface.

It is not a raw dump of internal implementation state.

### 6.11. Realization package

A realization package contains machinery used to realize contract authority.

Compiler passes, metamodel acquisition, planning, runtime storage, graph algorithms, identity derivation, execution
engines,
and reporting engines belong here unless a later ADR promotes a specific piece into one of the authority packages or a
stage-local domain package.

### 6.12. Adapter package

An adapter package contains outside-world bindings.

Reflection, KSP, ClassGraph, JUnit, Mockito, file systems, JSON libraries, console output, and platform APIs belong here
unless they are erased before reaching Kontrakt-owned material.

## 7. Package Authority Law

The package tree MUST obey this law:

```text
contract authority must not depend on realization architecture.
stage-local contract domains must not depend on realization architecture.
realization architecture may depend on contract authority and stage-local domains.
adapters may feed realization machinery.
adapters must not become contract authority.
```

This implies:

- `contract` must not depend on `realization`;
- `contract` must not depend on `adapter`;
- `contract` must not contain realization algorithms;
- `contract` may contain interfaces and immutable declaration structures only in the limited sense described in Section
  6.1;
- `stage.<stage>.contract` must not contain realization algorithms;
- `stage.<stage>.material` must not depend on backend handles after material is accepted as Kontrakt-owned material;
- `stage.<stage>.judgment` must not depend on framework callbacks;
- `stage.<stage>.machine` must not depend on `adapter`;
- `governance` must not depend on environment inspection inside core law;
- `stage.<stage>.diagnostics` must not smuggle implementation authority into public claims;
- `stage.<stage>.publication` must not publish raw internal state as a claim;
- `realization` may depend inward;
- `adapter` may depend inward and outward as needed to isolate outside technology.

## 8. Stage-First Package Law

The package architecture MUST NOT collapse all stage-specific contract roles into one global role bucket.

Many stages repeat the same role names. That repetition is intentional.

A stage may have its own:

```text
contract declarations
boundary surfaces
input material
output material
rejected material
judgment result
failure vocabulary
diagnostic evidence
publication eligibility
machine transition law
```

These roles must remain stage-local unless a type is deliberately promoted into shared vocabulary.

The default shape for a substantial stage is:

```text
stage.<stage-name>
├── contract
├── boundary
├── material
├── judgment
├── diagnostics
├── publication
└── machine
```

Not every stage must contain every role package. A stage should only contain what it owns.

The package tree should repeat role names under stage packages rather than force unrelated stages into shared global
buckets.

## 9. Current Single-Pipeline Law and Future Multi-Pipeline Rule

The current Kontrakt core is treated as one primary pipeline for this ADR.

Therefore, the current target structure is:

```text
io.kontrakt.stage.<stage-name>.<role>
```

not:

```text
io.kontrakt.pipeline.<pipeline-name>.stage.<stage-name>.<role>
```

A future multi-pipeline architecture must be introduced through module boundaries first.

The future shape should be:

```text
<new pipeline module>
└── stage
    └── <stage-name>
        ├── contract
        ├── boundary
        ├── material
        ├── judgment
        ├── diagnostics
        ├── publication
        └── machine
```

This ADR does not design those future modules.

It only reserves the rule: if multiple independent pipeline families exist later, separate the pipeline first by module,
then define stages inside that module, then define stage-local contract packages inside each stage.

Do not simulate multi-pipeline architecture today by adding a pipeline-name layer that the current system does not need.

## 10. Stage Boundary Enforcement Law

Stages must not know each other's internal implementation.

A stage may depend on another stage only through that stage's published contract-facing surface. That surface may
include declared contract types, accepted material types, judgment result types, diagnostic evidence types,
publication-facing types, and stage-local machine law when that law is intentionally public.

Allowed dependency shape:

```text
stage.A
    -> stage.B.contract
    -> stage.B.material
    -> stage.B.judgment
    -> stage.B.diagnostics
    -> stage.B.publication
    -> stage.B.machine
```

Forbidden dependency shape:

```text
stage.A
    -> stage.B.internal
    -> stage.B.helper
    -> stage.B.realization
    -> stage.B.adapter
    -> stage.B.implementation detail
```

This ADR does not require one Gradle module per stage. The current boundary is package-first, enforced by architecture
tests.

If a stage needs implementation helpers, they must be placed so that architecture tests can identify them as non-public
stage implementation. The exact folder name may be `internal`, `support`, or another project-standard name, but the rule
is fixed: other stages must not depend on it.

Kotlin/JVM package structure alone cannot fully enforce this boundary. Therefore, architecture tests are part of the
architecture, not optional test decoration.

## 11. Target Package Shape

The target package shape for the current single-pipeline core is:

```text
io.kontrakt

├── contract
│   ├── presentation
│   ├── obligation
│   ├── clause
│   ├── operation
│   └── version
│
├── stage
│   ├── input
│   │   ├── contract
│   │   ├── boundary
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   │
│   ├── admission
│   │   ├── contract
│   │   ├── boundary
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   │
│   ├── normalization
│   │   ├── contract
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   │
│   ├── canonicalization
│   │   ├── contract
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   │
│   ├── lowering
│   │   ├── contract
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   │
│   ├── invariant
│   │   ├── contract
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   │
│   ├── state
│   │   ├── contract
│   │   ├── machine
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   │
│   ├── transition
│   │   ├── contract
│   │   ├── machine
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   │
│   ├── publication
│   │   ├── contract
│   │   ├── material
│   │   ├── judgment
│   │   ├── diagnostics
│   │   └── exposure
│   │
│   └── diagnostic
│       ├── contract
│       ├── material
│       ├── judgment
│       ├── evidence
│       ├── retention
│       └── redaction
│
├── governance
│   ├── policy
│   ├── budget
│   ├── capacity
│   ├── capability
│   └── epoch
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
```

The exact JVM group prefix may be adjusted by module policy.

The architectural vocabulary is not optional.

## 12. Compiler Boundary Law

Compiler architecture is accepted only as realization machinery.

The following package shape is allowed under `realization.compiler`:

```text
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
```

The compiler may:

- read syntax;
- interpret declared contract presentations;
- lower syntax into internal material;
- run static checks;
- generate enforcement machinery;
- emit generated artifacts;
- compare generated projection behavior with reference judgment behavior.

The compiler must not create contract authority; it only realizes declared contract authority.

## 13. Interceptor Removal Boundary Law

Interceptor-style flow is not accepted as a machine model and is not part of the target package architecture.

The following are incompatible with the target architecture:

```text
ScenarioInterceptor
ScenarioExecutionChain
AuditingInterceptor
ResultResolverInterceptor
callback-driven proceed flow
recursive interceptor chain delegation
```

This ADR does not define the replacement implementation. It only marks these files and patterns as removal/replacement
targets. They should not be given a normal target package, because that would make the old callback model look like an
accepted layer of the new architecture.

If a temporary compatibility bridge is absolutely required to keep the project compiling during the relocation work, it
must be treated as a short-lived migration artifact, not as part of the target package tree. It must not be depended on
by `contract`, `stage`, `stage.<stage>.machine`, or `governance` packages.

The issue is not that interceptors are always invalid in ordinary software. The issue is narrower: Kontrakt's machine
law requires explicit movement. A `proceed` call is not a transition manifest, a recursive chain is not an explicit
machine, and a callback is not state authority.

The replacement direction is:

```text
explicit machine manifest
-> explicit state set
-> explicit transition set
-> explicit stage law
-> explicit judgment result
-> explicit diagnostic evidence
-> explicit publication claim or denial
```

The exact replacement belongs to a later ADR.

## 14. Behavior-Preserving Relocation Law

The first implementation pass MUST be movement-only.

Allowed changes:

- directory movement;
- package declaration updates;
- import updates;
- build configuration updates required by package movement;
- temporary compatibility aliases when required;
- architecture test scaffolding;
- temporary migration markers for files that are already marked for removal or replacement.

Forbidden changes in the first pass:

- deleting interceptor files as part of the movement-only pass;
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

```text
Move first.
Then replace.
```

A package-authority refactor touches too many files to be safely mixed with semantic changes.

## 15. Initial Relocation Guide

This guide is non-exhaustive.

It exists to make the first movement pass reviewable.

### 15.1. Discovery

Current area:

```text
discovery.api
discovery.adapter
discovery.domain
```

Target direction:

```text
contract.presentation
stage.input.contract
stage.input.boundary
stage.admission.contract
stage.admission.material
stage.admission.judgment
stage.admission.diagnostics
realization.compiler.frontend
adapter.classgraph
adapter.jvm
```

Discovery that reads classpath, annotations, or outside runtime surfaces belongs to adapters or compiler frontend.

Discovery results that become software-visible contract presentation material must move toward shared contract or
stage-local packages according to authority.

### 15.2. Linking

Current area:

```text
linking
```

Target direction:

```text
realization.graph
realization.execution
stage.lowering.material
stage.lowering.judgment
stage.admission.judgment
```

Binding and linking are realization mechanics unless the linked object becomes ratified material or a stage-local
judgment concept.

### 15.3. Metamodel and frozen material

Current area:

```text
metamodel.domain
metamodel.adapter.reflection
metamodel.domain.frozen
```

Target direction:

```text
stage.input.material
stage.normalization.material
stage.canonicalization.material
stage.lowering.material
stage.invariant.material
realization.metamodel
adapter.reflection
```

Reflection-specific material must stay outside authority.

Adapter-neutral frozen material may move under the stage that owns its ratified meaning.

Acquisition mechanics may remain under `realization.metamodel`.

### 15.4. Planning

Current area:

```text
planning.domain.expansion
planning.domain.projection
planning.domain.interner
planning.domain.runtime
planning.infrastructure.runtime
```

Target direction:

```text
realization.planning
realization.runtime
stage.canonicalization.material
stage.lowering.material
stage.transition.material
stage.transition.machine
governance.budget
governance.capacity
```

Planning algorithms are realization machinery.

Canonical or lowered material produced by planning may live under a stage-local `material` package when it becomes
ratified material.

Lifecycle law may move under `stage.<stage>.machine` when it expresses legal machine movement rather than physical
storage mechanics.

### 15.5. Execution

Current area:

```text
execution.domain
execution.port
execution.infrastructure
```

Target direction:

```text
realization.execution
stage.transition.machine
stage.transition.judgment
stage.diagnostic.material
stage.diagnostic.judgment
stage.publication.judgment
stage.publication.exposure
adapter.junit
```

Execution is realization.

Result resolution is stage-local judgment.

Trace and audit evidence are diagnostics.

Externally visible reports and results are publication.

### 15.6. Runtime and policy

Current area:

```text
planning.domain.runtime
planning.infrastructure.runtime
runtime policy files
worker lifecycle files
```

Target direction:

```text
realization.runtime
governance.policy
governance.budget
governance.capacity
governance.epoch
stage.transition.machine
```

Policy resolution belongs to governance when it defines machine law.

Worker backing, storage, lanes, and physical lifecycle belong to realization runtime unless promoted to stage-local
machine
law.

### 15.7. Reporting

Current area:

```text
reporting
console reporter
json reporter
html reporter
trace sinks
```

Target direction:

```text
stage.publication.exposure
stage.publication.material
stage.publication.judgment
stage.diagnostic.evidence
stage.diagnostic.retention
realization.reporting
adapter.console
adapter.file
adapter.json
```

A report is not contract authority. It is publication or diagnostic material derived from accepted judgment/evidence.

## 16. Dependency Direction Law

The following dependencies are forbidden:

```text
contract -> realization
contract -> adapter
stage.<stage>.contract -> realization
stage.<stage>.contract -> adapter
stage.<stage>.stage.<stage>.machine -> adapter
stage.<stage>.material.accepted -> adapter.reflection
stage.<stage>.material.accepted -> adapter.ksp
stage.<stage>.judgment -> adapter
governance -> adapter
stage.publication.exposure core claim -> adapter
```

The following dependencies are allowed:

```text
realization -> contract
realization -> stage.<stage>.machine
realization -> stage
realization -> governance
adapter -> realization
adapter -> contract presentation surfaces when required to read external declarations
adapter -> stage.input.boundary when required to deliver raw external input
```

Any allowed dependency must still respect the narrower law of the target package.

Allowed direction does not imply unrestricted coupling.

## 17. Determinism Law

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

## 18. Explicit Machine Reservation

This ADR reserves a follow-up explicit machine refactoring.

That work should define or ratify concepts such as:

```text
MachineManifest
StateManifest
TransitionManifest
StageManifest
StageJudgment
FailureJudgment
DiagnosticEvidence
PublicationJudgment
```

Names may change, but the authority rule may not:

```text
legal movement belongs to the explicit machine manifest,
not to callback behavior.
```

## 19. Architecture Tests

The package refactor must introduce architecture tests.

Required test categories:

1. **forbidden dependency tests**

   Verify that authority packages do not depend on realization or adapter packages.

2. **stage-local role tests**

   Verify that pipeline-specific contract, material, judgment, diagnostic, and publication types live under the owning
   `stage.<stage-name>` package unless deliberately promoted into shared vocabulary.

3. **adapter isolation tests**

   Verify that reflection, KSP, ClassGraph, JUnit, Mockito, JSON, file, and console implementation details do not leak
   into contract authority packages or stage-local authority packages.

4. **contract package content tests**

   Verify that shared and stage-local contract packages contain only contract-facing interfaces, immutable declaration
   objects, stable value records, enums, sealed declaration shapes, and small structural types. They must not contain
   discovery, traversal, lowering, planning, execution, caching, publication, reflection, scheduling, I/O, or
   environment-inspection algorithms.

5. **interceptor dependency isolation tests**

   Verify that target authority packages do not depend on interceptor-style flow. If any temporary migration bridge
   exists, it must remain outside the target architecture and must have no inbound dependency from contract, stage,
   stage-local machine, or governance packages.

6. **material purity tests**

   Verify that accepted material packages do not depend on backend handles or adapter-specific types.

7. **realization inward dependency tests**

   Verify that realization depends inward on authority packages rather than authority packages depending outward on
   realization.

## 20. Compliance Rules

A change complies with this ADR only if all of the following are true:

1. New top-level package names use contract-first vocabulary.
2. Pipeline-specific domains are organized stage-first under `stage.<stage-name>`.
3. Stage-local role packages are not collapsed into global buckets merely to avoid repeated package names.
4. Shared `contract` packages contain only narrow authority material: interfaces, immutable declaration structures,
   stable value records, enums, sealed declaration shapes, and small structural types. They must not contain realization
   algorithms.
5. Stage-local `contract` packages follow the same narrow content rule.
6. Constructor/property machinery inside contract packages is treated as a JVM/Kotlin representation limit, not as
   contract authority or a place for meaningful computation.
7. Compiler-specific names remain under `realization.compiler`.
8. Runtime, planning, graph, metamodel, identity, execution, and reporting machinery remain under `realization` unless
   explicitly promoted by a later ADR.
9. Adapter-specific code remains under `adapter`.
10. No behavior changes are mixed into the first relocation pass.
11. Interceptor-style flow is not treated as a target package layer.
12. Interceptor-style files are marked for removal or replacement by a follow-up explicit-machine decision, not
    preserved as accepted transitional machinery.
13. Architecture tests protect the new dependency direction.
14. Existing deterministic material, identity, planning, runtime, and publication laws remain intact.
15. Any semantic replacement after relocation receives its own decision boundary.
16. Future multi-pipeline architecture is introduced by module boundary first, not by adding unnecessary pipeline-name
    layers to the current single-pipeline core.

## 21. Alternatives Considered

### 21.1. Use compiler architecture as the top-level package structure

Rejected.

A compiler-style root such as:

```text
frontend
middleend
backend
optimizer
emitter
```

would explain implementation mechanics while hiding Kontrakt's product domain.

Kontrakt uses compiler architecture, but that architecture is not contract authority.

### 21.2. Keep the current package structure and only rename individual classes

Rejected.

The problem is not only naming. The current structure still groups responsibilities according to older test-framework,
discovery, execution, and interceptor-era architecture, so small renames would preserve the same authority error.

### 21.3. Delete incompatible files during package movement

Rejected for this ADR.

Some files are conceptually incompatible with the final direction, especially interceptor-driven flow. This ADR does not
preserve them as a target structure. It only separates package relocation from the later removal/replacement work so the
first pass remains reviewable and reversible.

### 21.4. Put all contract-theory vocabulary at the root without a realization boundary

Rejected.

A pure contract-vocabulary root without `realization` would eventually turn authority packages into implementation trash
bins.

Kontrakt needs both:

```text
contract authority vocabulary
realization machinery boundary
```

That boundary must stay explicit.

### 21.5. Treat pipeline as the top-level package authority

Rejected.

Pipeline is a useful orchestration concept.

Pipeline is not automatically contract authority.

A pipeline stage becomes contract-relevant only when a declared obligation, judgment, material law, or explicit machine
transition is attached to it.

Pipeline must be represented through stage-local contract domains and explicit machine law, not as a global bucket.

### 21.6. Use role-first packages for pipeline-specific material

Rejected.

A role-first tree such as:

```text
material.admission
material.lowering
material.publication
judgment.admission
judgment.lowering
judgment.publication
```

makes role vocabulary visible, but it splits one stage across several distant package areas.

For Kontrakt, the more important boundary is the stage. A stage owns the local contract terms, accepted material,
judgment, failure vocabulary, evidence, and movement law for one machine point.

Therefore, stage-first is preferred:

```text
stage.admission.contract
stage.admission.material
stage.admission.judgment
stage.admission.diagnostics
```

### 21.7. Introduce pipeline-name packages now

Rejected for the current codebase.

The current core has one primary pipeline. A package layer such as `pipeline.<name>.stage.<stage>` would add abstraction
before the system needs it.

If multiple independent pipeline families appear later, they should be separated by module first. Inside each pipeline
module, the stage-first rule applies.

## 22. Consequences

### 22.1. Positive consequences

- The package structure will reflect `What Contract Is`.
- Contract authority will be separated from compiler/runtime realization.
- Stage-local obligations will remain visible instead of being collapsed into global role buckets.
- Deterministic laws will become easier to locate and protect.
- Adapter-specific technology will be isolated more clearly.
- Interceptor-style hidden flow will be identified as removal/replacement work instead of being normalized as a target
  package layer.
- Large refactoring becomes reviewable because movement and behavior changes are separated.
- Future contributors will see that Kontrakt is a contract machine, not a test framework or compiler clone.

### 22.2. Negative consequences

- The refactor will touch many files.
- Imports will churn heavily.
- Existing ADR references and documentation may temporarily point to old package names.
- Tests may require broad import updates.
- The stage-first tree repeats role package names under several stages.
- Temporary migration artifacts may feel awkward until the explicit machine replacement is complete.
- Architecture tests must be added before the package boundary is fully stable.

### 22.3. Accepted cost

The churn is accepted because the current package structure still tells an outdated architectural story. Keeping it
would preserve short-term convenience, but it would keep pushing the project back toward the wrong mental model.

Repeated stage-local package names are also accepted. The repetition is not noise. It preserves the fact that each stage
owns different obligations, material, judgments, failures, and evidence.

Kontrakt's package structure must make the machine's authority visible.

## 23. Implementation Plan

### 23.1. Ratify package law

Create the target package tree and dependency direction law from this ADR.

### 23.2. Add architecture tests

Add package dependency tests before or during movement so violations are visible early.

### 23.3. Move files without semantic edits

Move files according to the target package structure.

Only package declarations, imports, directory paths, build configuration, and temporary compatibility wrappers are
allowed.

### 23.4. Keep pipeline module boundary out of the current move

Do not add a pipeline-name package layer during this refactor.

The current codebase is treated as one primary pipeline. Future multi-pipeline structure must be introduced by module
boundary when it becomes real.

### 23.5. Mark interceptor-style flow as removal/replacement work

Do not give interceptor-era files a normal target package. They are not an accepted layer of the new architecture.

If the movement-only pass cannot compile without a temporary bridge, keep that bridge visibly short-lived and outside
the target package law. The actual solution is a later explicit machine/state/transition replacement, not an interceptor
package rename.

### 23.6. Compile and run current tests

The movement pass is complete only after current behavior is preserved.

### 23.7. Prepare explicit machine ADR

After relocation, prepare a follow-up ADR for replacing callback/interceptor flow with explicit machine/state/transition
flow.

## 24. Final Rule

Kontrakt's package structure must describe contract authority before it describes realization machinery.

Current pipeline-specific domains are stage-first.

A stage owns its own local contract, boundary, material, judgment, diagnostic, publication, and machine role packages as
needed.

Shared packages are allowed only for deliberately shared vocabulary. They must not become dumping grounds for
stage-specific material.

Compiler architecture is internal machinery.

Planning, execution, runtime, metamodel, identity, graph, and reporting are realization machinery unless explicitly
promoted by contract authority or stage-local authority.

Adapters isolate outside technology.

Contract packages are deliberately small. They hold contract-facing interfaces and immutable declaration material, not
algorithms. Constructors and property machinery that remain there are accepted only as JVM/Kotlin representation limits.

Interceptor-style flow is outside the target architecture and is marked for removal or replacement.

The first refactoring pass moves compatible files only and does not pretend the interceptor model is part of the new
package law.

Future multi-pipeline architecture should be introduced through module boundaries first. Inside each pipeline module,
stages own their own stage-local contract packages.

No incompatible flow is normalized as target architecture before the explicit machine replacement is designed and
reviewed.