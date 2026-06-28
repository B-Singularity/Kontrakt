# ADR-0045: Contract Pipeline Package Architecture and Explicit State-Machine Axis

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

Kontrakt started with test automation vocabulary, but the current direction is broader. The project is now being shaped
as a deterministic contract system whose authority comes from declared obligations, canonical material, explicit machine
movement, governed failure, and controlled publication.

Earlier ADRs remain relevant. They introduced adapter-neutral acquisition, frozen metamodel material, stable identity
protocols, L1 planner structures, L2 interning, runtime policy, lifecycle governance, bounded diagnostics, and
backend-handle erasure. Those decisions already push the implementation toward determinism.

The remaining problem is architectural language. The current package tree still exposes older implementation terms near
the top:

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

Some of these names are still useful inside realization code. Some are transitional. Some now lead readers toward the
wrong architecture.

`What Contract Is` gives the stronger rule: implementation machinery must not become contract authority. The package
structure should make that visible. It should show stage-owned contract boundaries first, and compiler/runtime/adapters
as realization or outside technology.

## 2. Problem

The current package architecture has five problems.

### 2.1. Implementation vocabulary is too high in the tree

Packages such as `execution`, `planning`, `metamodel`, `reporting`, and `runtime` are useful implementation areas, but
their current position makes them look like primary product domains.

That is not accurate. Planning, execution, metamodel acquisition, identity derivation, runtime storage, and reporting
are realization machinery unless a specific type is deliberately promoted into contract authority, stage-local material,
stage-local judgment, governance, diagnostics, or publication.

### 2.2. Interceptor flow hides movement

Some older execution code uses interceptor-style flow. In that model, the next move depends on whether an interceptor
calls `proceed`. The transition is real, but the rule for the transition is hidden inside callback behavior.

That conflicts with the explicit machine direction. Legal movement must be declared as state, transition, stage law,
judgment, failure, diagnostic evidence, and publication decision. It must not be hidden in recursive chain delegation or
callback continuation.

### 2.3. Good deterministic work is not aligned under the right vocabulary

The implementation already contains useful deterministic mechanisms:

- adapter order is not trusted as semantic order;
- backend handles are erased before frozen planning material;
- canonical material is separated from physical representation;
- transitional hash values are not persistent identity;
- frozen material is published only after validation;
- lifecycle states are explicit in several runtime subsystems;
- planning structures avoid semantic dependence on cache behavior;
- runtime policy is resolved before expensive work begins;
- failure, continuation, quarantine, and diagnostics are governed.

Those parts should be preserved. The refactor is not a rewrite of the deterministic core. It is a package-authority
refactor: the existing work must be placed under names that explain whether it is contract authority, stage-local
material, judgment, governance, realization, or adapter code.

### 2.4. Stage-local roles can collapse into global buckets

A pipeline stage is a contract point. A substantial stage may own its own contract declarations, boundary material,
accepted material, rejected material, judgment result, failure vocabulary, diagnostic evidence, publication condition,
and movement law.

If the top-level package tree uses only global role packages such as `material`, `judgment`, `diagnostics`, and
`publication`, stage ownership becomes unclear. Admission judgment, lowering judgment, invariant judgment, state
judgment, transition judgment, and publication judgment are not interchangeable just because each is a judgment.

The package structure must be stage-first for pipeline-specific material.

### 2.5. File movement and semantic replacement are separate changes

Package relocation will touch many files. Interceptor removal will change behavior. Those changes must be separated.

The first pass is movement-only: package declarations, imports, directory paths, build configuration, and architecture
tests. Incompatible structures are marked for later replacement, not normalized into the new package law.

## 3. Decision Drivers

The package decision is evaluated by these criteria:

- contract authority remains visible;
- each stage can hide internal implementation;
- deterministic laws remain easy to locate and test;
- the first refactor can preserve behavior;
- the design does not add speculative future layers;
- Kotlin/JVM visibility limits are acknowledged;
- the current single pipeline can later grow into multiple pipeline modules without another conceptual rewrite.

## 4. Alternatives

### 4.1. One Gradle module per stage now

This would create modules such as:

```text
kontrakt-stage-admission
kontrakt-stage-canonicalization
kontrakt-stage-lowering
kontrakt-stage-publication
```

Advantages:

- strong physical isolation;
- module-scoped Kotlin `internal`;
- Gradle `api` / `implementation` boundaries;
- fewer accidental imports from another stage's internals.

Disadvantages:

- too much module churn during an already large refactor;
- stage vocabulary is not stable enough to freeze as build modules;
- Gradle graph design would be mixed with package-authority work;
- API surfaces would be forced before explicit state-machine replacement;
- circular dependency pressure may appear before material boundaries are clean.

Runtime performance is not the main objection. The issue is timing. Module boundaries are harder to move than package
boundaries.

Decision: rejected for the first pass. A stage may be promoted to a module later if the boundary becomes stable and the
isolation benefit justifies the build cost.

### 4.2. Role-first packages

This would organize by role first:

```text
material.admission
material.lowering
material.publication
judgment.admission
judgment.lowering
judgment.publication
```

Advantages:

- role vocabulary is easy to find;
- cross-stage concepts are easy to group.

Disadvantages:

- one stage is scattered across many package areas;
- stage ownership becomes a mental reconstruction task;
- global role packages tend to become dumping grounds;
- the same word, such as `judgment`, hides different machine points.

Decision: rejected. For pipeline-specific material, the stage is the stronger boundary.

### 4.3. Pipeline-name package layer now

This would introduce a pipeline layer immediately:

```text
Kontrakt.pipeline.core.stage.admission.contract
Kontrakt.pipeline.core.stage.lowering.material
```

Advantages:

- future multi-pipeline shape is visible;
- package names reserve a place for pipeline families.

Disadvantages:

- the current core has one primary pipeline;
- the layer is speculative;
- every package gets longer without adding current isolation;
- it simulates a future boundary instead of waiting for a real one.

Decision: rejected for the current codebase. If independent pipeline families appear later, the pipeline boundary should
be introduced as a module boundary first. Inside that module, the same stage-first rule applies.

### 4.4. Package-first stage separation inside the current core module

This keeps the current core as one module and organizes the package tree as:

```text
Kontrakt.stage.<stage-name>.<role>
```

Advantages:

- stage ownership is explicit;
- the first pass can preserve behavior;
- package movement can be reviewed without Gradle module redesign;
- architecture tests can enforce forbidden dependencies;
- the design can later promote stable pipeline boundaries into modules.

Disadvantages:

- Kotlin/JVM packages are not hard access-control boundaries;
- `internal` is module-scoped, not package-scoped;
- architecture tests become part of the boundary enforcement;
- role package names repeat under stages.

Decision: accepted.

## 5. Decision

Kontrakt adopts a contract-pipeline package architecture and a parallel explicit state-machine contract axis.

The explicit state machine is still contract authority. It is separated from `stage` because it is a special axis that
carries state, transition, and explicit state-machine manifest material beside the logical contract pipeline.

The current top-level vocabulary is:

```text
stage
statemachine
versioning
governance
realization
adapter
```

For the current single-pipeline core, stage-owned contract domains live under:

```text
Kontrakt.stage.<stage-name>.<role>
```

The explicit state machine is a separate contract axis. It does not duplicate every stage name. Its package shape is:

```text
Kontrakt.statemachine.<manifest|state|transition>.<role>
```

Version coordinates are also contract authority. They are separated from `stage` because version meaning is not a
pipeline processing step, and they are separated from `realization` because version meaning ownership must not be hidden
inside implementation machinery.

The versioning package shape is:

```text
Kontrakt.versioning.coordinate.<role>
Kontrakt.versioning.coordinate.contract.<frozen|planning|seed>
```

A substantial stage may contain role packages such as:

```text
stage.<stage-name>
├── contract
├── boundary
├── material
├── judgment
├── diagnostics
└── publication
```

Not every stage needs every role.

`machine` is not a package role under `stage`. `state` and `transition` are not ordinary stage packages either. They
belong under `statemachine` because the explicit state machine is itself a contract axis: state contract, state
transition contract, and explicit state-machine manifest.

The current rule is:

```text
contract pipeline authority first
explicit state-machine movement surface beside it
realization machinery behind both
outside technology behind adapters
```

Compiler-related implementation is treated as realization, but this ADR does not create a compiler package tree.
Existing compiler-domain code should keep its current domain vocabulary inside the appropriate realization area.
Planning, execution, runtime, metamodel, identity, graph, and reporting belong under `realization` unless a specific
type is deliberately promoted into contract authority, a stage-local domain, governance, diagnostics, or publication.

Older DDD, hexagonal, and port vocabulary may remain only when it names implementation machinery without becoming
authority. The words are not imported as theory. `port` and `state` must not become stable subpackages under
`realization.execution` because contract-facing ports and explicit state have already been separated into the contract
axes. Execution may keep implementation terms such as aggregate, factory, service, strategy, orchestration, plan,
generation, context, or vm when they describe realization mechanics. Those names do not define contract authority and
must be revised later if they start acting as the single public entry point or legal surface of the machine.

Interceptor-style flow is outside the target architecture. Existing interceptor files are evidence of the old execution
model and are removal/replacement targets. The first movement pass may keep temporary migration support only where
required to compile.

## 6. Authority and Boundaries

ADR-0045 owns:

- contract-first authority law;
- contract-pipeline package law for the stage axis;
- explicit state-machine axis law;
- current single-pipeline package assumption;
- future multi-pipeline module boundary rule;
- realization boundary law;
- adapter boundary law;
- compiler-related realization boundary;
- interceptor removal boundary;
- behavior-preserving relocation sequence;
- dependency direction rules;
- architecture-test requirements.

ADR-0045 does not own:

- final public contract syntax;
- final IR schema;
- canonical metadata byte encoding;
- HID derivation;
- frozen acquisition internals;
- L1 planner primitive mechanics;
- L2 interner storage mechanics;
- runtime memory envelope vector;
- report artifact schema;
- explicit execution machine replacement.

Ownership split:

| Surface                                    | Owner                                |
|--------------------------------------------|--------------------------------------|
| contract meaning                           | `What Contract Is`                   |
| package authority law                      | ADR-0045                             |
| stage-first package law                    | ADR-0045                             |
| future multi-pipeline module boundary      | ADR-0045                             |
| compiler low-level rules                   | compiler-core protocols              |
| canonical metadata identity                | ADR-0041 and identity protocol notes |
| primitive lifecycle and physical substrate | ADR-0042                             |
| contract graph identity                    | ADR-0043                             |
| unified memory envelope                    | ADR-0044                             |
| frozen acquisition lifecycle               | ADR-0040                             |
| explicit L2 lifecycle                      | ADR-0034 / ADR-0035                  |
| package relocation execution               | ADR-0045 implementation plan         |
| interceptor replacement                    | future explicit state-machine ADR    |

## 7. Vocabulary

### 7.1. `stage` package

A top-level `contract` package is not part of the current target architecture.

The reason is boundary clarity. In the current single-pipeline design, contract obligations are owned by stages. A
top-level `contract` package would easily become a common bucket for declarations that actually belong under
`stage.<stage>.contract`. This ADR therefore keeps contract packages stage-local by default.

A later ADR may introduce a project-wide contract package only if there is a concrete non-stage concept that cannot
honestly belong to any stage. This ADR does not define such a package.

### 7.2. `stage`

A `stage` package contains the local contract world for one declared judgment position in the logical pipeline.

A stage may own:

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
```

These roles remain local by default. Moving a concept out of a stage requires a separate reason; convenience is not
enough.

A stage does not own the explicit state machine as an internal role package. State-machine material that defines
condition, transition, terminality, or legality belongs to the parallel `statemachine` axis.

### 7.3. `stage.<stage>.contract`

A stage-local contract package contains obligations for one specific stage.

It is intentionally narrow: interfaces, immutable declarations, enums, sealed shapes, stable value records, and small
structural types are allowed. Algorithms are not.

### 7.4. `stage.<stage>.boundary`

A stage-local boundary package contains the surface through which outside material enters that stage.

For early stages, this may include DTO or external input surfaces. For later stages, this is still a boundary: upstream
output is not trusted as already-accepted material.

### 7.5. `stage.<stage>.material`

A stage-local material package contains material owned by that stage.

Examples include raw, admitted, normalized, canonical, lowered, frozen, rejected, failed, or publication-eligible
material, depending on the stage.

### 7.6. `stage.<stage>.judgment`

A stage-local judgment package contains decisions made by that stage under declared law.

Admission judgment, lowering judgment, invariant judgment, state judgment, transition judgment, and publication judgment
should remain separate unless a later decision defines a specific top-level package placement for a cross-stage concept.

### 7.7. `stage.<stage>.diagnostics`

A stage-local diagnostics package contains bounded evidence, trace, retention, redaction, and diagnostic summary
material for that stage.

Diagnostics explain judgment. They are not authority by themselves.

### 7.8. `stage.<stage>.publication`

A stage-local publication package contains public claim, denial, report, artifact, or stage-output presentation material
owned by that stage.

Publication is public-claim formation under judgment. It is not a dump of internal state.

### 7.9. `statemachine`

The `statemachine` package contains the explicit state-machine contract axis.

It owns the state contract, the state transition contract, and the explicit state-machine manifest. It may define
declared conditions, transition names, initial conditions, terminal conditions, legality, and legal movement rules.

It is not callback flow, interceptor flow, runtime orchestration, or the whole Good Machine. It is contract authority,
but it is not an ordinary stage in the logical contract pipeline. It is separated because this contract surface runs
beside the pipeline and governs legal movement.

### 7.10. `versioning`

The `versioning` package contains the version-coordinate contract axis.

It owns version coordinates. It may define which contract meaning was active when material, judgment, claim, or
diagnostic evidence was produced. It does not currently define a compatibility package; old material under a different
coordinate is not current authority unless a later ADR explicitly introduces a compatibility law.

It is not release bookkeeping, build metadata, or implementation migration machinery. Versioning is contract authority
when meaning can move while material still looks familiar.

### 7.11. `governance`

A governance package contains policy, budget, capacity, capability, epoch, and resource admission law.

Governance is resolved law, not arbitrary configuration.

### 7.12. `realization`

A realization package contains machinery used to realize contract authority.

Metamodel acquisition, identity derivation, graph operations, planning, execution, runtime storage, reporting engines,
and other compiler-style implementation work belong here unless a later decision promotes a specific piece into an
authority package or a stage-local domain.

Realization vocabulary is allowed only as implementation vocabulary. DDD and hexagonal terms may appear here when they
name concrete machinery, but they do not carry their original architectural authority into Kontrakt. `aggregate` may
remain as implementation grouping if it does not become contract meaning. `port` is not a stable realization package
because contract surfaces and adapter boundaries have explicit homes. `state` is not a stable execution package because
explicit state belongs to `statemachine`. If old files use those names, the relocation must split them into stage,
statemachine, adapter, reporting, runtime support, or temporary migration locations instead of normalizing the old
vocabulary.

### 7.13. `adapter`

An adapter package contains outside-world bindings.

Reflection, KSP, ClassGraph, JUnit, Mockito, file systems, JSON libraries, console output, and platform APIs belong here
unless their information has been erased and lowered into Kontrakt-owned material.

## 8. Package Authority Law

The package tree must obey these rules:

```text
stage-local contract domains must not depend on realization architecture.
state-machine contract declarations must not depend on realization architecture or adapters.
version-coordinate contract declarations must not depend on realization architecture or adapters.
realization may depend on stage-local domains, state-machine declarations, versioning declarations, and governance.
adapters may feed realization machinery.
adapters must not become contract authority.
```

Consequences:

- `stage.<stage>.contract` must not depend on `realization` or `adapter`;
- `stage.<stage>.contract` must not contain realization algorithms;
- accepted stage material must not depend on backend handles;
- stage judgment must not depend on framework callbacks;
- state-machine law must not depend on adapters;
- version-coordinate law must not depend on adapters or realization machinery;
- governance law must not inspect the environment directly;
- diagnostics must not smuggle implementation authority into public claims;
- publication must not expose raw internal state as a claim.

## 9. Stage-First Package Law

Pipeline-specific material is stage-first.

The default shape for a substantial stage is:

```text
stage.<stage-name>
├── contract
├── boundary
├── material
├── judgment
├── diagnostics
└── publication
```

A stage includes only the roles it owns.

Role names may repeat under different stages. The repetition is intentional because each stage owns different
obligations, material, judgments, failures, evidence, and publication surfaces.

Movement law is not removed. It belongs to the parallel `statemachine` axis, not to a `machine` role inside each stage.
Version meaning is not removed either. It belongs to the parallel `versioning` axis, not to stage-local implementation
material.

A cross-stage type must not be created just to avoid repeated package names. If a concept cannot belong to one stage, it
needs an explicit later decision that explains the new package boundary. The expected default is no promotion.

## 10. Current Single-Pipeline Law and Future Multi-Pipeline Rule

The current Kontrakt core is treated as one primary pipeline.

Current shape:

```text
Kontrakt.stage.<stage-name>.<role>
```

Do not introduce this shape yet:

```text
Kontrakt.pipeline.<pipeline-name>.stage.<stage-name>.<role>
```

If independent pipeline families appear later, the pipeline boundary should be a module boundary first:

```text
<new pipeline module>
├── stage
│   └── <stage-name>
│       ├── contract
│       ├── boundary
│       ├── material
│       ├── judgment
│       ├── diagnostics
│       └── publication
├── statemachine
│   ├── manifest
│   ├── state
│   └── transition
└── versioning
    └── coordinate
```

This ADR reserves that direction but does not design those future modules.

## 11. Stage Boundary and Acquisition Law

Stages do not transfer trusted material by direct package dependency.

A stage may finish with accepted output material inside its own contract world. If that result is exposed to another
stage, it becomes an output presentation at the first stage's publication boundary.

For the receiving stage, that presentation is external material. It is not already-accepted receiving-stage material.

Legal movement:

```text
stage.A accepted output
    -> stage.A publication boundary
    -> external output presentation
    -> stage.B boundary
    -> stage.B guard / admission
    -> stage.B lowering
    -> stage.B judgment
    -> stage.B-owned material
       or stage.B-declared rejection / failure / deferral
```

Stage A's material identity, judgment status, and state-machine condition do not carry into Stage B. Stage B may record
provenance or diagnostic reference, but provenance is not acceptance.

Forbidden shortcut:

```text
stage.A output presentation
    -> stage.B.material
```

Forbidden dependency shape:

```text
stage.A
    -> stage.B.contract
    -> stage.B.material
    -> stage.B.judgment
    -> stage.B.diagnostics
    -> stage.B.publication
    -> stage.B.internal
    -> stage.B.helper
    -> stage.B.realization
    -> stage.B.adapter
```

If several stages appear to need the same concept, first check whether they are using the same word for different
stage-local meanings. The expected default is to keep the concept stage-local. If a genuinely non-stage concept appears,
its package boundary requires a separate ADR. That decision still does not allow a stage to skip another stage's guard,
lowering, or judgment.

This ADR does not require one Gradle module per stage. The current boundary is package-first and must be enforced by
architecture tests.

## 12. Target Package Shape

Current single-pipeline target:

```text
Kontrakt

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
│   │   ├── boundary
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
│   ├── publication
│   │   ├── contract
│   │   ├── material
│   │   ├── judgment
│   │   ├── diagnostics
│   │   └── claim
│   │
│   └── diagnostic
│       ├── contract
│       ├── material
│       ├── judgment
│       ├── evidence
│       ├── retention
│       └── redaction
│
├── statemachine
│   ├── manifest
│   │   ├── contract
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   │
│   ├── state
│   │   ├── contract
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   │
│   └── transition
│       ├── contract
│       ├── material
│       ├── judgment
│       └── diagnostics
│
├── versioning
│   └── coordinate
│       ├── contract
│       │   ├── frozen
│       │   │   └── image
│       │   ├── planning
│       │   └── seed
│       ├── material
│       ├── judgment
│       └── diagnostics
│
├── governance
│   ├── policy
│   ├── budget
│   ├── capacity
│   ├── capability
│   └── epoch
│
├── realization
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
    ├── normalization
    ├── jvm
    ├── file
    ├── json
    └── console
```

The JVM group prefix may change by module policy. The architectural vocabulary must not.

`statemachine` is a top-level contract axis, not a generic `machine` root. It exists because state contract, state
transition contract, and explicit state-machine manifest are contract authority, but they are not ordinary
contract-pipeline stages.

`versioning` is a top-level contract-coordinate axis. It exists because version coordinates determine which contract
meaning was active for material, judgment, claim, or evidence. Versioning is not an ordinary pipeline stage and not an
implementation detail.

## 13. Compiler-Related Realization Law

Compiler architecture is an implementation method, not a package taxonomy chosen by this ADR.

This ADR does not create `realization.compiler` or prescribe compiler-layer packages. Compiler-related code should keep
the existing Kontrakt domain vocabulary and be placed in the realization area that owns the work, such as metamodel
acquisition, identity, graph, planning, execution, runtime, or reporting.

Do not introduce generic compiler packages such as `frontend`, `syntax`, `semantic`, `pass`, `analysis`, `generation`,
or `emission` merely because conventional compiler architecture uses those names.

The allowed first-pass movement is limited to placing compiler-related implementation behind the realization boundary
while preserving the existing domain structure.

Compiler-style work may acquire presentations, lower material, run checks, prepare generated machinery, and emit
artifacts. Those activities remain realization. They do not create contract authority.

## 14. Interceptor Removal Boundary Law

Interceptor-style flow is not part of the target architecture.

Incompatible forms include:

```text
ScenarioInterceptor
ScenarioExecutionChain
AuditingInterceptor
ResultResolverInterceptor
callback-driven proceed flow
recursive interceptor chain delegation
```

This ADR does not define the replacement. It only marks these forms as removal/replacement targets.

If temporary migration support is required during relocation, it must remain visibly temporary and outside the target
package law. It must not be depended on by `contract`, `stage`, `statemachine`, `versioning`, or `governance`.

The replacement direction is explicit state-machine flow:

```text
explicit state-machine manifest
-> explicit state set
-> explicit transition set
-> explicit stage law
-> explicit judgment result
-> explicit diagnostic evidence
-> explicit publication claim or denial
```

The exact replacement belongs to a later ADR.

## 15. Behavior-Preserving Relocation Law

The first implementation pass is movement-only.

Allowed:

- directory movement;
- package declaration updates;
- import updates;
- build configuration updates required by package movement;
- temporary migration aliases when required;
- architecture test scaffolding;
- temporary migration markers for files marked for removal or replacement.

Forbidden in the first pass:

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

Rule:

```text
Move first.
Then replace.
```

## 16. Initial Relocation Guide

This guide is non-exhaustive.

### 16.1. Discovery

Current:

```text
discovery.api
discovery.adapter
discovery.domain
```

Target:

```text
stage.input.contract
stage.input.boundary
stage.admission.contract
stage.admission.material
stage.admission.judgment
stage.admission.diagnostics
adapter.classgraph
adapter.jvm
```

Classpath, annotation, or runtime-surface reading belongs to adapters or the existing realization domain that owns that
work. Results that become contract presentation material belong under the stage that owns their admission and lowering
path. This ADR does not create a top-level contract package for such material.

### 16.2. Linking

Current:

```text
linking
```

Target:

```text
realization.graph
realization.execution
stage.lowering.material
stage.lowering.judgment
stage.admission.judgment
```

Binding and linking are realization mechanics unless the linked result becomes ratified material or stage-local
judgment.

### 16.3. Metamodel and frozen material

Current:

```text
metamodel.domain
metamodel.adapter.reflection
metamodel.domain.frozen
```

Target:

```text
stage.input.material
stage.normalization.material
stage.canonicalization.material
stage.lowering.material
stage.invariant.material
realization.metamodel
adapter.reflection
```

Reflection-specific material stays outside authority. Adapter-neutral frozen material may move under the stage that owns
its ratified meaning. Acquisition mechanics may remain under `realization.metamodel`.

### 16.4. Planning

Current:

```text
planning.domain.expansion
planning.domain.projection
planning.domain.interner
planning.domain.runtime
planning.infrastructure.runtime
```

Target:

```text
realization.planning
realization.runtime
stage.canonicalization.material
stage.lowering.material
statemachine.transition.material
statemachine.transition.contract
governance.budget
governance.capacity
```

Planning algorithms are realization machinery. Ratified canonical or lowered material may live under stage-local
`material`. Lifecycle law may move under `statemachine.state` or `statemachine.transition` when it expresses legal
movement rather than physical storage.

### 16.5. Execution

Current:

```text
execution.domain
execution.port
execution.infrastructure
```

Target:

```text
realization.execution
realization.execution.context
realization.execution.mocking
realization.execution.scenario
realization.runtime.support
realization.reporting
statemachine.transition.contract
statemachine.transition.judgment
stage.diagnostic.material
stage.diagnostic.judgment
stage.publication.judgment
stage.publication.claim
adapter.junit
```

Execution is realization. Result resolution is stage-local judgment. Trace and audit evidence are diagnostics.
Externally visible reports and results are publication.

`execution.port` is old hexagonal vocabulary and is not a target package. Incoming execution surfaces must move to the
stage boundary that owns entry. Outgoing reporting surfaces must move to publication, diagnostic, reporting, or adapter
packages according to what they actually do. Mocking and scenario support may remain under realization as implementation
machinery, but not as contract-facing ports.

`execution.adapter.state` and similar state-shaped execution packages are not target vocabulary. If the file expresses
explicit movement legality, it belongs under `statemachine`. If it stores runtime context or thread-local machinery, it
belongs under realization context or runtime support.

### 16.6. Runtime and policy

Current:

```text
planning.domain.runtime
planning.infrastructure.runtime
runtime policy files
worker lifecycle files
```

Target:

```text
realization.runtime
governance.policy
governance.budget
governance.capacity
governance.epoch
statemachine.transition
```

Policy resolution belongs to governance when it defines law. Worker backing, storage, lanes, and physical lifecycle
belong to realization runtime unless promoted to state-machine law.

### 16.7. Reporting

Current:

```text
reporting
console reporter
json reporter
html reporter
trace sinks
```

Target:

```text
stage.publication.claim
stage.publication.material
stage.publication.judgment
stage.diagnostic.evidence
stage.diagnostic.retention
realization.reporting
adapter.console
adapter.file
adapter.json
```

A report is publication or diagnostic material derived from accepted judgment/evidence. It is not contract authority.

## 17. Dependency Direction Law

Forbidden:

```text
stage.<stage>.contract -> realization
stage.<stage>.contract -> adapter
statemachine.<manifest|state|transition> -> adapter
stage.<stage>.material.accepted -> adapter.reflection
stage.<stage>.material.accepted -> adapter.ksp
stage.<stage>.judgment -> adapter
governance -> adapter
stage.publication.claim -> adapter
```

Allowed, subject to narrower package laws:

```text
realization -> stage
realization -> governance
adapter -> realization
adapter -> stage.input.boundary when required to deliver raw external input
```

Allowed direction does not mean unrestricted coupling.

## 18. Determinism Law

The refactor must preserve and strengthen determinism.

Rules that remain binding:

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

The package structure exists to make these laws harder to violate accidentally.

## 19. Explicit State-Machine Axis Reservation

This ADR reserves a follow-up explicit state-machine refactoring.

That work should define or ratify concepts such as:

```text
StateMachineManifest
StateManifest
TransitionManifest
StageManifest
StageJudgment
FailureJudgment
DiagnosticEvidence
PublicationJudgment
```

Names may change. The authority rule may not:

```text
legal movement belongs to the explicit state-machine manifest,
not to callback behavior.
```

## 20. Architecture Tests

The package refactor must introduce architecture tests.

Required categories:

1. **forbidden dependency tests**

   Authority packages must not depend on realization or adapter packages.

2. **stage-local role tests**

   Pipeline-specific contract, material, judgment, diagnostic, and publication types must live under the owning stage.
   Explicit state-machine types must live under the parallel `statemachine` axis. Version-coordinate types must live
   under the parallel `versioning` axis. Promotion to a top-level package requires a separate reason and should be rare.

   `realization.execution.port` and `realization.execution.state` must not be introduced as stable target packages.
   Existing files with those old names must be split or placed under more precise packages during relocation.

3. **inter-stage acquisition tests**

   One stage must not import another stage's local role packages as ordinary dependencies. Upstream output is treated as
   external input by the receiving stage and must pass through the receiving stage's boundary, guard/admission,
   lowering, and judgment.

4. **adapter isolation tests**

   Reflection, KSP, ClassGraph, JUnit, Mockito, JSON, file, console, and other platform details must not leak into
   authority packages.

5. **contract package content tests**

   Stage-local contract packages may contain only interfaces, immutable declaration objects, stable value records,
   enums, sealed declaration shapes, and small structural types. They must not contain discovery, traversal, lowering,
   planning, execution, caching, publication, reflection, scheduling, I/O, or environment-inspection algorithms.

6. **interceptor isolation tests**

   Target authority packages must not depend on interceptor-style flow. Temporary migration bridges must remain outside
   the target architecture.

7. **material purity tests**

   Accepted material packages must not depend on backend handles or adapter-specific types.

8. **realization inward dependency tests**

   Realization depends inward on authority packages. Authority packages do not depend outward on realization.

## 21. Compliance Rules

A change complies with this ADR only if:

1. Top-level packages do not create a global contract bucket.
2. Pipeline-specific domains are stage-first under `stage.<stage-name>`.
3. Stage-local role packages are not collapsed into global buckets for convenience.
4. Inter-stage movement follows publication-to-boundary acquisition. The receiving stage must guard, lower, and judge
   external input before downstream-owned material may exist.
5. A stage does not depend on another stage's local contract, material, judgment, diagnostics, publication, helper,
   realization, or adapter packages as ordinary peer dependencies.
6. Stage-local `contract` packages contain only narrow declaration vocabulary and no realization algorithms.
7. Constructor and property machinery inside contract packages is treated as a JVM/Kotlin representation limit, not as
   contract authority.
8. Compiler-related implementation stays behind the realization boundary and keeps the existing Kontrakt domain
   vocabulary unless changed by a later compiler-specific ADR.
9. Runtime, planning, graph, metamodel, identity, execution, and reporting machinery remain under `realization` unless
   explicitly promoted by a later ADR.
10. Adapter-specific code remains under `adapter`.
11. The first relocation pass does not change behavior.
12. Interceptor-style flow is not treated as a target package layer.
13. Interceptor-style files are marked for later removal or replacement.
14. Architecture tests protect the new dependency direction.
15. Existing deterministic material, identity, planning, runtime, and publication laws remain intact.
16. Future multi-pipeline architecture is introduced by module boundary first.

## 22. Consequences

### 22.1. Positive

- Package names align with `What Contract Is`.
- Stage-local obligations remain visible.
- Deterministic laws become easier to locate and protect.
- Realization machinery is separated from authority packages.
- Adapter-specific technology is isolated.
- Interceptor-style flow is marked as replacement work rather than normalized.
- The first pass remains reviewable because movement and behavior changes are separated.
- Future multi-pipeline structure can be introduced without changing the stage-first rule.

### 22.2. Negative

- Many files and imports will move.
- Documentation and ADR references may temporarily point to old package names.
- Stage-first packages repeat role names.
- Kotlin/JVM packages do not fully enforce access boundaries.
- Architecture tests become required enforcement, not optional hygiene.
- Temporary migration artifacts may remain until explicit state-machine replacement is complete.

### 22.3. Accepted cost

The churn is accepted because the current package structure still tells an outdated architectural story.

Repeated role names under stages are accepted because they preserve stage ownership.

## 23. Implementation Plan

### 23.1. Ratify package law

Create the target package tree and dependency direction law from this ADR.

### 23.2. Add architecture tests

Add dependency tests before or during file movement.

### 23.3. Move files without semantic edits

Move files according to the target package structure.

Allowed edits are package declarations, imports, directory paths, build configuration, and temporary migration wrappers.

### 23.4. Keep pipeline module boundary out of the current move

Do not add a pipeline-name package layer during this refactor.

### 23.5. Mark interceptor-style flow as removal/replacement work

Do not give interceptor-era files a normal target package.

Temporary bridges, if required, must remain visibly short-lived and outside the target package law.

### 23.6. Compile and run current tests

The movement pass is complete only after current behavior is preserved.

### 23.7. Prepare explicit state-machine ADR

After relocation, prepare a follow-up ADR for replacing callback/interceptor flow with explicit
state-machine/state/transition flow.

## 24. Final Rule

Kontrakt's package structure must describe contract authority before realization machinery.

Current pipeline-specific domains are stage-first.

A stage owns its own local contract, boundary, material, judgment, diagnostics, and publication role packages as needed.

Stage movement is not peer package dependency. One stage may expose an output presentation at its publication boundary.
The next stage must treat that presentation as external input and run its own boundary, guard/admission, lowering, and
judgment before creating its own material.

This ADR does not create a top-level `contract` package. Contract packages are stage-local unless a later ADR introduces
a concrete non-stage package boundary.

Compiler-style architecture is internal realization machinery. This ADR does not create a compiler package tree.

Planning, execution, runtime, metamodel, identity, graph, and reporting are realization machinery unless explicitly
promoted by contract authority or stage-local authority.

Adapters isolate outside technology.

Contract packages are deliberately small. They hold contract-facing interfaces and immutable declaration material, not
algorithms. Constructors and property machinery that remain there are accepted only as JVM/Kotlin representation limits.

Interceptor-style flow is outside the target architecture and is marked for removal or replacement.

The first refactoring pass moves compatible files only.

Future multi-pipeline architecture should be introduced through module boundaries first. Inside each pipeline module,
stages own their own stage-local contract packages.