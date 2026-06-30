# ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror

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
-
`0045-file-placement-map-relative.phased.statemachine-version-coordinate-contract-subdomains.realization-vocabulary-cleanup.md`

---

## 1. Context

Kontrakt started with test automation vocabulary, but the current direction is broader. The project is now being shaped
as a deterministic contract system whose authority comes from declared obligations, canonical material, explicit machine
movement, governed failure, diagnostic evidence, version-coordinate control, and controlled publication.

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

Some of these names are still useful. Some are compiler/runtime implementation vocabulary. Some are transitional. Some
lead readers toward the wrong architecture when placed too high in the package tree.

`What Contract Is` gives the stronger rule: implementation machinery must not become contract authority. A package tree
that raises implementation vocabulary into authority position creates the same kind of failure that platform
architectures suffer when implementation substrate becomes public law. Later refactoring then stops being a mechanical
implementation change and becomes compatibility debt or accidental contract evolution.

Kontrakt must therefore separate three surfaces:

```text
contract authority
    stage / statemachine / versioning / governance / diagnostic

compiler/runtime realization
    metamodel / normalization / identity / graph / planning / linking / execution / runtime / cache / storage / reporting

outside technology
    adapter
```

The package structure must show that relationship without pretending that the compiler/runtime implementation is itself
the contract.

## 2. Problem

The current package architecture has seven problems.

### 2.1. Implementation vocabulary is too high in the tree

Packages such as `execution`, `planning`, `metamodel`, `reporting`, and `runtime` are useful implementation areas, but
their current position makes them look like primary product domains.

That is not accurate. Planning, execution, metamodel acquisition, identity derivation, graph traversal, runtime storage,
caching, and reporting are realization machinery unless a specific type is deliberately promoted into contract
authority, stage-local material, stage-local judgment, governance, diagnostics, publication, or version-coordinate
authority.

### 2.2. The word `stage` was ambiguous

Two possible definitions appeared during design review.

The first definition was:

```text
stage = Kontrakt execution phase or compiler/runtime work unit
```

Under that definition, packages such as these become plausible:

```text
stage.discovery
stage.acquisition
stage.planning
stage.linking
stage.execution
stage.reporting
stage.caching
```

That definition gives good developer locality, but it promotes implementation phases into contract authority. It makes
discovery, planning, execution, and reporting look like declared contract gates.

The second definition was:

```text
stage = logical contract-pipeline gate
```

Under that definition, packages such as these become the stable stage axis:

```text
stage.input
stage.admission
stage.canonicalization
stage.lowering
stage.invariant
stage.publication
```

This ADR adopts the second definition.

A `stage` is not an arbitrary implementation phase. A `stage` is a contract-owned unit of the logical contract pipeline
where material crosses a boundary, is judged under declared law, and either becomes stage-owned material / stage
publication or stops as declared failure with diagnostic evidence.

Execution phases are still real. They are just not `stage` packages. They belong under `realization`.

### 2.3. Developer locality and authority locality conflict

Developers often work by implementation flow:

```text
discovery
acquisition
planning
linking
execution
reporting
cache
runtime
```

Contract authority, however, is organized by declared obligation:

```text
input
admission
canonicalization
lowering
invariant
publication
state
transition
version coordinate
governance
diagnostic evidence
```

A package tree cannot make both axes primary at once. If implementation flow becomes primary, implementation becomes
contract. If contract authority becomes primary but material is too flat, stage-local `material` packages become dumping
grounds.

The answer is not to promote implementation phases into `stage`. The answer is to keep contract authority under `stage`
and give developer work locality under `realization`.

### 2.4. Material packages can become implementation buckets

A flat package such as this is not enough once the system grows:

```text
stage.canonicalization.material
stage.lowering.material
stage.publication.material
```

It can accumulate unrelated material, physical representations, implementation artifacts, and compiler output. That is
still cleaner than implementation-first packaging, but it is not precise enough for Kontrakt.

The material package must be subdivided only by contract-governed material meaning. It must not be subdivided by
implementation process names.

Allowed material vocabulary is conservative:

```text
presentation
surface
carrier
origin
provenance
subject
declaration
candidate
admitted
deferred
representative
reference
fact
basis
transition
accepted
claim
evidence
disposition
availability
```

Risky names are rejected under `stage.*.material` by default:

```text
identity
frozen
image
table
record
sequence
order
schema
key
handle
bytes
digest
hash
intern
ordinal
route
cache
scanner
reader
collector
projector
planner
executor
writer
artifact
```

Those names may be legitimate implementation terms under `realization`. They may become contract-governed material only
through a later ADR that proves the name denotes declared material rather than replaceable machinery.

### 2.5. `identity` and `frozen` are especially dangerous words

`identity` can mean:

```text
semantic equivalence law
representative stability law
collision verification law
reference law
hash digest
HID word layout
intern id
table address
cache key
JVM object identity
```

Only some of these are contract. Most are implementation.

Contract-side vocabulary should therefore say:

```text
equivalence
distinction
representation
representative
reference
collision
stability
backend_erasure
```

Implementation-side vocabulary may say:

```text
realization.identity.derivation
realization.identity.digest
realization.identity.encoding
realization.identity.interning
realization.identity.collision
```

`frozen` is also dangerous. The obligation is not `frozen`. The obligation may be:

```text
immutability after acceptance
mutation prohibition after publication
backend-handle erasure
deterministic replay
publication stability
version-bound meaning
```

Frozen images, frozen tables, frozen records, and frozen sequences are implementation-owned physical forms unless a
later ADR explicitly promotes one as contract-governed material.

### 2.6. Interceptor flow hides movement

Some older execution code uses interceptor-style flow. In that model, the next move depends on whether an interceptor
calls `proceed`. The transition is real, but the rule for the transition is hidden inside callback behavior.

That conflicts with the explicit machine direction. Legal movement must be declared as state, transition, stage law,
judgment, failure, diagnostic evidence, and publication decision. It must not be hidden in recursive chain delegation or
callback continuation.

### 2.7. File movement and semantic replacement are separate changes

Package relocation will touch many files. Interceptor removal will change behavior. Risky type names may also need
semantic renaming later.

Those changes must be separated.

The first pass is movement-only: package declarations, imports, directory paths, build configuration, and architecture
tests. Incompatible structures are marked for later replacement, not normalized into the new package law.

## 3. Decision Drivers

The package decision is evaluated by these criteria:

- contract authority remains visible;
- stage means contract-pipeline gate, not implementation phase;
- implementation phases remain available as developer work locality under realization;
- material packages express contract-governed material, not physical representation;
- deterministic laws remain easy to locate and test;
- current ADR/domain vocabulary is preserved where it is already accurate;
- compiler/runtime implementation is allowed to be compiler-like behind realization;
- the design does not add speculative future layers;
- the first refactor can preserve behavior;
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

### 4.3. Implementation-phase packages as stage packages

This would organize by Kontrakt execution phases:

```text
stage.discovery
stage.acquisition
stage.planning
stage.linking
stage.execution
stage.reporting
```

Advantages:

- very strong developer locality;
- the package tree follows the way contributors often work;
- current code movement may look easier.

Disadvantages:

- implementation phases become contract authority;
- a future implementation refactor becomes compatibility debt;
- package names teach the wrong theory;
- `What Contract Is` obligations are pushed down or scattered;
- the structure repeats the failure mode where implementation substrate becomes public law.

Decision: rejected.

The concepts remain valid under `realization`:

```text
realization.discovery
realization.acquisition
realization.planning
realization.linking
realization.execution
realization.reporting
realization.cache
```

### 4.4. Pipeline-name package layer now

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

### 4.5. Contract-stage packages with compiler/runtime realization mirror

This keeps the current core as one module and organizes the package tree as:

```text
Kontrakt.stage.<contract-stage>.<role>
Kontrakt.statemachine.<axis>.<role>
Kontrakt.versioning.<axis>.<role>
Kontrakt.governance.<axis>.<role>
Kontrakt.diagnostic.<axis>.<role>
Kontrakt.realization.<implementation-domain>.<implementation-role>
Kontrakt.adapter.<outside-technology>
```

Advantages:

- contract authority is visible;
- developer work locality remains available;
- existing Kontrakt compiler/runtime vocabulary is preserved under realization;
- implementation can be compiler-like without becoming authority;
- the first pass can preserve behavior;
- package movement can be reviewed without Gradle module redesign;
- architecture tests can enforce forbidden dependencies;
- the design can later promote stable pipeline boundaries into modules.

Disadvantages:

- Kotlin/JVM packages are not hard access-control boundaries;
- `internal` is module-scoped, not package-scoped;
- architecture tests become part of the boundary enforcement;
- role package names repeat under stages;
- realization packages need bridge discipline so they do not declare new authority.

Decision: accepted.

## 5. Decision

Kontrakt adopts a contract-pipeline package architecture, a parallel explicit state-machine contract axis, and a
compiler/runtime realization mirror.

The current top-level vocabulary is:

```text
stage
statemachine
versioning
governance
diagnostic
realization
adapter
```

For the current single-pipeline core, stage-owned contract domains live under:

```text
Kontrakt.stage.<stage-name>.<role>
```

The accepted stage names are logical contract-pipeline gates:

```text
input
admission
canonicalization
lowering
invariant
publication
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
Kontrakt.versioning.<coordinate|compatibility|validity>.<role>
```

Governance is a contract axis for policy, budget, capacity, capability, and validity:

```text
Kontrakt.governance.<policy|budget|capacity|capability|validity>.<role>
```

Diagnostics are a contract evidence axis, not logging:

```text
Kontrakt.diagnostic.<evidence|retention|redaction>.<role>
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

Compiler-related implementation is realization. Realization may be compiler-like and may use existing Kontrakt domain
vocabulary:

```text
realization.metamodel
realization.normalization
realization.identity
realization.graph
realization.planning
realization.linking
realization.execution
realization.runtime
realization.cache
realization.storage
realization.reporting
```

Realization is the mirror image of contract authority, not a competing authority tree. Each realization domain may
include `bridge` packages that identify which contract stage or contract axis it realizes.

Outside technology belongs under `adapter`.

The current rule is:

```text
contract authority first
explicit state-machine movement beside it
version/governance/diagnostic axes beside the stage pipeline
compiler/runtime realization behind them
outside technology behind adapters
```

## 6. Authority and Boundaries

ADR-0045 owns:

- contract-stage package law;
- definition of `stage` as logical contract-pipeline gate;
- explicit state-machine axis law;
- version-coordinate axis law;
- governance axis law;
- diagnostic evidence axis law;
- realization mirror law;
- current single-pipeline package assumption;
- future multi-pipeline module boundary rule;
- adapter boundary law;
- compiler-related realization boundary;
- material naming safety law;
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
| realization mirror law                     | ADR-0045                             |
| future multi-pipeline module boundary      | ADR-0045                             |
| compiler low-level rules                   | compiler-core protocols              |
| canonical metadata identity                | ADR-0041 and identity protocol notes |
| primitive lifecycle and physical substrate | ADR-0042                             |
| contract graph identity                    | ADR-0043                             |
| unified memory envelope                    | ADR-0044                             |
| frozen acquisition lifecycle               | ADR-0040                             |
| explicit L2 lifecycle                      | ADR-0034 / ADR-0035                  |
| package relocation execution               | ADR-0045 placement map               |
| interceptor replacement                    | future explicit state-machine ADR    |

## 7. Vocabulary

### 7.1. No top-level `contract` bucket

A top-level `contract` package is not part of the current target architecture.

The reason is boundary clarity. In the current single-pipeline design, contract obligations are owned by stages or by
explicit cross-cutting contract axes. A top-level `contract` package would easily become a common bucket for
declarations that actually belong under `stage.<stage>.contract`, `statemachine`, `versioning`, `governance`, or
`diagnostic`.

A later ADR may introduce a project-wide contract package only if there is a concrete non-stage, non-axis concept that
cannot honestly belong to any existing authority axis. This ADR does not define such a package.

### 7.2. `stage`

A `stage` package contains the local contract world for one declared judgment position in the logical contract pipeline.

A stage may own:

```text
contract declarations
boundary surfaces
input material
output material
candidate material
admitted material
judgment result
failure vocabulary
diagnostic evidence
publication eligibility
```

A stage does not own the explicit state machine as an internal role package. State-machine material that defines
condition, transition, terminality, or legality belongs to the parallel `statemachine` axis.

### 7.3. `phase`

`phase` is not a target package. It is a conceptual name for implementation flow.

Discovery, acquisition, normalization, planning, linking, execution, reporting, caching, and runtime orchestration may
be implementation phases. They are not contract stages unless a later ADR explicitly promotes one into a declared
contract pipeline of its own.

### 7.4. `stage.<stage>.contract`

A stage-local contract package contains obligations for one specific stage.

It is intentionally narrow. Interfaces, immutable declarations, enums, sealed declaration shapes, stable value records,
and small structural types are allowed. Algorithms are not.

Contract subpackages may use rich obligation vocabulary when it names declared law:

```text
presentation
surface
carrier
origin
provenance
authority
admissibility
ratification
compatibility
policy
capacity
budget
equivalence
distinction
representation
representative
reference
determinism
collision
stability
backend_erasure
meaning
preservation
shape
basis
vacancy
obstruction
consistency
contradiction
exposure
claim
redaction
concealment
audience
denial
failure
diagnostic
```

### 7.5. `stage.<stage>.boundary`

A stage-local boundary package contains the surface through which outside material enters that stage.

For early stages, this may include DTO or external input surfaces. For later stages, this is still a boundary: upstream
output is not trusted as already-accepted material.

Allowed boundary vocabulary includes:

```text
ingress
entry
inspection
isolation
airlock
refinement
judgment
exposure
exit
```

### 7.6. `stage.<stage>.material`

A stage-local material package contains contract-governed material owned by that stage.

A material package name must describe what the material is under contract authority. It must not name how that material
is stored, computed, hashed, sorted, frozen, interned, cached, encoded, addressed, scanned, projected, rendered, or
written.

Allowed material vocabulary includes:

```text
presentation
surface
carrier
origin
provenance
subject
declaration
external
candidate
admitted
deferred
disposition
representative
reference
availability
fact
basis
transition
accepted
claim
evidence
```

The following are not valid material package names by default:

```text
identity
frozen
image
table
record
sequence
order
schema
key
handle
bytes
digest
hash
intern
ordinal
route
cache
scanner
reader
collector
projector
planner
executor
writer
artifact
```

They may be used under `realization`. They may be used under `stage.*.material` only if a later ADR explicitly proves
that the name denotes contract-governed material rather than replaceable realization machinery.

### 7.7. `stage.<stage>.judgment`

A stage-local judgment package contains decisions made by that stage under declared law.

Admission judgment, lowering judgment, invariant judgment, and publication judgment should remain separate unless a
later decision defines a specific top-level package placement for a cross-stage concept.

Allowed judgment vocabulary includes:

```text
presence
shape
origin
contamination
authority
admission
rejection
deferral
refusal
disposition
equivalence
distinction
representation
reference
collision
stability
preservation
candidate
obstruction
invariant
acceptance
consistency
contradiction
exposure
claim
redaction
concealment
denial
failure
```

### 7.8. `stage.<stage>.diagnostics`

A stage-local diagnostics package contains bounded evidence, reason, retention, redaction, drift, contamination,
rejection, deferral, and diagnostic summary material for that stage.

Diagnostics explain judgment. They are not authority by themselves.

### 7.9. `stage.<stage>.publication`

A stage-local publication package contains the output presentation that this stage may expose to the next stage.

It is not final public reporting unless the stage is `stage.publication`. Publication is controlled exposure under
judgment. It is not a dump of internal state.

### 7.10. `stage.publication.claim`

The publication stage owns final public claims.

```text
stage.publication.claim.public
stage.publication.claim.diagnostic
stage.publication.claim.denial
```

Reports, files, JSON, console output, and JUnit output are realization or adapter concerns. The contract side speaks in
claims, exposure, redaction, concealment, denial, and evidence.

### 7.11. `statemachine`

The `statemachine` package contains the explicit state-machine contract axis.

It owns the state contract, the state transition contract, and the explicit state-machine manifest. It may define
declared conditions, transition names, initial conditions, terminal conditions, completeness, closure, legality,
permission, one-way movement, and refusal.

It is not callback flow, interceptor flow, runtime orchestration, or the whole Good Machine. It is contract authority,
but it is not an ordinary stage in the logical contract pipeline. It is separated because this contract surface runs
beside the pipeline and governs legal movement.

### 7.12. `versioning`

The `versioning` package contains the version-coordinate contract axis.

It owns meaning coordinates, compatibility decisions, and validity rules. It may define which contract meaning was
active when material, judgment, claim, or diagnostic evidence was produced. It may also define reuse, migration,
rejudgment, refusal, active, pinned, expired, and revoked judgments.

It is not release bookkeeping, build metadata, or implementation migration machinery. Versioning is contract authority
when meaning can move while material still looks familiar.

`epoch` is not used as a default contract-axis package name because it can become runtime/cache vocabulary. Use
`validity` unless a later ADR proves that an epoch is contract-coordinate material.

### 7.13. `governance`

A governance package contains policy, budget, capacity, capability, and validity law.

Governance is resolved law, not arbitrary configuration. Worker pools, lanes, queues, meters, schedulers, telemetry
stores, and environment probes are realization.

### 7.14. `diagnostic`

The top-level `diagnostic` package contains diagnostic evidence, retention, and redaction law/material that is not owned
by a single stage.

Diagnostic is not logging. It is the contract evidence plane that lets the machine explain why judgment happened, what
evidence is retained, what is discarded, and what may be exposed.

### 7.15. `realization`

A realization package contains machinery used to realize contract authority.

Realization may use compiler-style implementation vocabulary because Kontrakt is implemented as compiler/runtime
machinery. However, compiler vocabulary is not top-level product authority.

Realization should keep existing Kontrakt domain language:

```text
metamodel
normalization
identity
graph
planning
linking
execution
runtime
cache
storage
reporting
```

Generic compiler packages such as `frontend`, `syntax`, `semantic`, `analysis`, `pass`, `backend`, `generation`, or
`emission` must not replace existing Kontrakt domain vocabulary unless a later compiler-specific ADR deliberately
introduces that layer.

A realization package may include a `bridge` package that names which contract stage or contract axis it realizes:

```text
realization.identity.bridge.canonicalization
realization.planning.bridge.lowering
realization.planning.bridge.invariant
realization.execution.bridge.statemachine
realization.reporting.bridge.publication
```

A bridge package may contain mapping, assembly, adapter-erasure, and emission boundary code. It must not declare new
contract authority.

DDD and hexagonal terms may appear in realization when they name concrete machinery, but they do not carry their
original architectural authority into Kontrakt. `aggregate` may remain as implementation grouping if it does not become
contract meaning. `port` is not a stable realization package because contract surfaces and adapter boundaries have
explicit homes. `state` is not a stable execution package because explicit state belongs to `statemachine`.

### 7.16. `adapter`

An adapter package contains outside-world bindings.

Reflection, KSP, ClassGraph, JUnit, Mockito, file systems, JSON libraries, console output, and platform APIs belong here
unless their information has been erased and lowered into Kontrakt-owned material.

## 8. Package Authority Law

The package tree must obey these rules:

```text
stage-local contract domains must not depend on realization architecture.
state-machine contract declarations must not depend on realization architecture or adapters.
version-coordinate contract declarations must not depend on realization architecture or adapters.
governance declarations must not depend on realization architecture or adapters.
diagnostic evidence law must not depend on adapters.
realization may depend on stage-local domains, state-machine declarations, versioning declarations, governance, and diagnostic law.
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
- publication must not expose raw internal state as a claim;
- realization bridge packages must not declare new contract authority.

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

Cross-stage diagnostics are not forced into stage packages. They may belong to the top-level `diagnostic` axis when
their law is not owned by one stage.

A cross-stage type must not be created just to avoid repeated package names. If a concept cannot belong to one stage or
one existing axis, it needs an explicit later decision that explains the new package boundary. The expected default is
no promotion.

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
    ├── coordinate
    ├── compatibility
    └── validity
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
│   │   │   ├── presentation
│   │   │   ├── surface
│   │   │   ├── carrier
│   │   │   ├── origin
│   │   │   ├── provenance
│   │   │   ├── authority
│   │   │   ├── unknown
│   │   │   ├── omission
│   │   │   ├── unavailable
│   │   │   ├── malformed
│   │   │   ├── contamination
│   │   │   ├── failure
│   │   │   └── diagnostic
│   │   ├── boundary
│   │   │   ├── ingress
│   │   │   ├── inspection
│   │   │   ├── isolation
│   │   │   └── exit
│   │   ├── material
│   │   │   ├── presentation
│   │   │   ├── surface
│   │   │   ├── carrier
│   │   │   ├── origin
│   │   │   ├── subject
│   │   │   ├── declaration
│   │   │   └── external
│   │   ├── judgment
│   │   │   ├── presence
│   │   │   ├── shape
│   │   │   ├── origin
│   │   │   ├── contamination
│   │   │   ├── authority
│   │   │   ├── refusal
│   │   │   └── failure
│   │   ├── diagnostics
│   │   │   ├── evidence
│   │   │   ├── provenance
│   │   │   ├── reason
│   │   │   ├── contamination
│   │   │   └── retention
│   │   └── publication
│   │       ├── presentation
│   │       ├── candidate
│   │       └── failure
│   │
│   ├── admission
│   │   ├── contract
│   │   │   ├── admissibility
│   │   │   ├── authority
│   │   │   ├── ratification
│   │   │   ├── compatibility
│   │   │   ├── policy
│   │   │   ├── capacity
│   │   │   ├── budget
│   │   │   ├── unknown
│   │   │   ├── omission
│   │   │   ├── contamination
│   │   │   ├── disposition
│   │   │   ├── failure
│   │   │   └── diagnostic
│   │   ├── boundary
│   │   │   ├── entry
│   │   │   ├── airlock
│   │   │   ├── isolation
│   │   │   └── exit
│   │   ├── material
│   │   │   ├── presentation
│   │   │   ├── candidate
│   │   │   ├── admitted
│   │   │   ├── subject
│   │   │   └── disposition
│   │   ├── judgment
│   │   │   ├── admission
│   │   │   ├── rejection
│   │   │   ├── deferral
│   │   │   ├── refusal
│   │   │   ├── disposition
│   │   │   └── failure
│   │   ├── diagnostics
│   │   │   ├── evidence
│   │   │   ├── reason
│   │   │   ├── rejection
│   │   │   ├── deferral
│   │   │   ├── failure
│   │   │   └── retention
│   │   └── publication
│   │       ├── admitted
│   │       ├── disposition
│   │       └── failure
│   │
│   ├── canonicalization
│   │   ├── contract
│   │   │   ├── presentation
│   │   │   ├── equivalence
│   │   │   ├── distinction
│   │   │   ├── representation
│   │   │   ├── representative
│   │   │   ├── reference
│   │   │   ├── determinism
│   │   │   ├── collision
│   │   │   ├── stability
│   │   │   ├── backend_erasure
│   │   │   ├── failure
│   │   │   └── diagnostic
│   │   ├── boundary
│   │   │   ├── entry
│   │   │   ├── equivalence
│   │   │   ├── representation
│   │   │   └── exit
│   │   ├── material
│   │   │   ├── presentation
│   │   │   ├── candidate
│   │   │   ├── representative
│   │   │   ├── reference
│   │   │   ├── subject
│   │   │   ├── availability
│   │   │   └── fact
│   │   ├── judgment
│   │   │   ├── equivalence
│   │   │   ├── distinction
│   │   │   ├── representation
│   │   │   ├── reference
│   │   │   ├── collision
│   │   │   ├── stability
│   │   │   └── failure
│   │   ├── diagnostics
│   │   │   ├── evidence
│   │   │   ├── reason
│   │   │   ├── equivalence
│   │   │   ├── collision
│   │   │   ├── drift
│   │   │   └── retention
│   │   └── publication
│   │       ├── representative
│   │       ├── reference
│   │       └── fact
│   │
│   ├── lowering
│   │   ├── contract
│   │   │   ├── meaning
│   │   │   ├── preservation
│   │   │   ├── reference
│   │   │   ├── candidate
│   │   │   ├── shape
│   │   │   ├── authority
│   │   │   ├── basis
│   │   │   ├── vacancy
│   │   │   ├── obstruction
│   │   │   ├── failure
│   │   │   └── diagnostic
│   │   ├── boundary
│   │   │   ├── entry
│   │   │   ├── refinement
│   │   │   └── exit
│   │   ├── material
│   │   │   ├── representative
│   │   │   ├── reference
│   │   │   ├── candidate
│   │   │   ├── subject
│   │   │   ├── basis
│   │   │   ├── fact
│   │   │   ├── transition
│   │   │   └── claim
│   │   ├── judgment
│   │   │   ├── preservation
│   │   │   ├── reference
│   │   │   ├── candidate
│   │   │   ├── shape
│   │   │   ├── authority
│   │   │   ├── obstruction
│   │   │   └── failure
│   │   ├── diagnostics
│   │   │   ├── evidence
│   │   │   ├── reason
│   │   │   ├── reference
│   │   │   ├── obstruction
│   │   │   └── retention
│   │   └── publication
│   │       ├── candidate
│   │       ├── fact
│   │       ├── transition
│   │       └── claim
│   │
│   ├── invariant
│   │   ├── contract
│   │   │   ├── invariant
│   │   │   ├── basis
│   │   │   ├── acceptance
│   │   │   ├── consistency
│   │   │   ├── contradiction
│   │   │   ├── preservation
│   │   │   ├── authority
│   │   │   ├── failure
│   │   │   └── diagnostic
│   │   ├── boundary
│   │   │   ├── entry
│   │   │   ├── judgment
│   │   │   └── exit
│   │   ├── material
│   │   │   ├── subject
│   │   │   ├── basis
│   │   │   ├── candidate
│   │   │   ├── accepted
│   │   │   ├── fact
│   │   │   ├── transition
│   │   │   └── claim
│   │   ├── judgment
│   │   │   ├── invariant
│   │   │   ├── acceptance
│   │   │   ├── consistency
│   │   │   ├── contradiction
│   │   │   ├── refusal
│   │   │   └── failure
│   │   ├── diagnostics
│   │   │   ├── evidence
│   │   │   ├── reason
│   │   │   ├── contradiction
│   │   │   ├── refusal
│   │   │   └── retention
│   │   └── publication
│   │       ├── accepted
│   │       ├── fact
│   │       ├── transition
│   │       └── claim
│   │
│   └── publication
│       ├── contract
│       │   ├── exposure
│       │   ├── claim
│       │   ├── presentation
│       │   ├── redaction
│       │   ├── concealment
│       │   ├── compatibility
│       │   ├── audience
│       │   ├── denial
│       │   ├── failure
│       │   └── diagnostic
│       ├── boundary
│       │   ├── entry
│       │   ├── exposure
│       │   └── exit
│       ├── material
│       │   ├── subject
│       │   ├── candidate
│       │   ├── presentation
│       │   ├── claim
│       │   └── evidence
│       ├── judgment
│       │   ├── exposure
│       │   ├── claim
│       │   ├── redaction
│       │   ├── concealment
│       │   ├── denial
│       │   └── failure
│       ├── diagnostics
│       │   ├── evidence
│       │   ├── reason
│       │   ├── denial
│       │   ├── redaction
│       │   └── retention
│       └── claim
│           ├── public
│           ├── diagnostic
│           └── denial
│
├── statemachine
│   ├── manifest
│   │   ├── contract
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   ├── state
│   │   ├── contract
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   └── transition
│       ├── contract
│       ├── material
│       ├── judgment
│       └── diagnostics
│
├── versioning
│   ├── coordinate
│   │   ├── contract
│   │   │   ├── meaning
│   │   │   ├── binding
│   │   │   ├── validity
│   │   │   ├── reuse
│   │   │   ├── rejudgment
│   │   │   ├── refusal
│   │   │   ├── failure
│   │   │   └── diagnostic
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   ├── compatibility
│   │   ├── contract
│   │   ├── material
│   │   ├── judgment
│   │   └── diagnostics
│   └── validity
│       ├── contract
│       ├── material
│       ├── judgment
│       └── diagnostics
│
├── governance
│   ├── policy
│   ├── budget
│   ├── capacity
│   ├── capability
│   └── validity
│
├── diagnostic
│   ├── evidence
│   ├── retention
│   └── redaction
│
├── realization
│   ├── metamodel
│   ├── normalization
│   ├── identity
│   ├── graph
│   ├── planning
│   ├── linking
│   ├── execution
│   ├── runtime
│   ├── cache
│   ├── storage
│   └── reporting
│
└── adapter
    ├── reflection
    ├── ksp
    ├── classgraph
    ├── junit
    ├── mockito
    ├── jvm
    ├── normalization
    ├── file
    ├── json
    └── console
```

The JVM group prefix may change by module policy. The architectural vocabulary must not.

## 13. Compiler-Related Realization Law

Compiler architecture is an implementation method, not a contract package taxonomy.

This ADR does not create `realization.compiler` or prescribe generic compiler-layer packages. Compiler-related code
should keep existing Kontrakt domain vocabulary and be placed in the realization area that owns the work.

Allowed realization domains:

```text
realization.metamodel
realization.normalization
realization.identity
realization.graph
realization.planning
realization.linking
realization.execution
realization.runtime
realization.cache
realization.storage
realization.reporting
```

Do not introduce generic compiler packages such as:

```text
frontend
syntax
semantic
analysis
pass
backend
generation
emission
```

unless a later compiler-specific ADR deliberately introduces that layer.

Compiler-style work may acquire presentations, normalize text, derive identity, traverse graphs, plan, link, execute,
validate cache entries, store images/tables/records/sequences, render reports, and export artifacts. Those activities
remain realization. They do not create contract authority.

### 13.1. Realization metamodel

```text
realization.metamodel
├── acquisition
├── fact
├── erasure
├── ratification
└── bridge
    ├── input
    ├── admission
    └── canonicalization
```

### 13.2. Realization normalization

```text
realization.normalization
├── text
├── identifier
├── descriptor
└── bridge
    ├── input
    ├── admission
    └── canonicalization
```

### 13.3. Realization identity

```text
realization.identity
├── derivation
├── digest
├── encoding
├── interning
├── collision
└── bridge
    ├── canonicalization
    ├── versioning
    └── diagnostic
```

### 13.4. Realization graph

```text
realization.graph
├── node
├── edge
├── traversal
├── closure
└── bridge
    ├── lowering
    ├── invariant
    └── statemachine
```

### 13.5. Realization planning

```text
realization.planning
├── preflight
├── selection
├── projection
├── ordering
├── expansion
├── breakpoint
├── metering
├── publication
└── bridge
    ├── admission
    ├── lowering
    ├── invariant
    ├── governance
    └── diagnostic
```

### 13.6. Realization linking

```text
realization.linking
├── binding
├── resolution
├── materialization
├── verification
└── bridge
    ├── lowering
    ├── invariant
    ├── statemachine
    └── governance
```

### 13.7. Realization execution

```text
realization.execution
├── vm
├── scenario
├── generator
├── verification
├── trace
└── bridge
    ├── invariant
    ├── statemachine
    ├── publication
    └── diagnostic
```

### 13.8. Realization runtime

```text
realization.runtime
├── session
├── scheduler
├── memory
├── gate
├── integrity
└── bridge
    ├── governance
    ├── versioning
    ├── statemachine
    └── diagnostic
```

### 13.9. Realization cache

```text
realization.cache
├── lookup
├── admission
├── retention
├── eviction
├── validation
└── bridge
    ├── versioning
    ├── governance
    ├── lowering
    └── diagnostic
```

### 13.10. Realization storage

```text
realization.storage
├── image
├── table
├── record
├── sequence
└── arena
```

Storage may use physical vocabulary. That vocabulary must not move into `stage.*.material` unless a later ADR promotes
it as contract-governed material.

### 13.11. Realization reporting

```text
realization.reporting
├── renderer
├── writer
├── export
├── redaction
└── bridge
    ├── publication
    ├── diagnostic
    └── governance
```

## 14. Material Naming Law

Material package names must describe contract-governed material, not the physical method used to store, derive, freeze,
hash, encode, address, cache, or publish that material.

Allowed by default under stage material:

```text
presentation
surface
carrier
origin
provenance
subject
declaration
external
candidate
admitted
deferred
disposition
representative
reference
availability
fact
basis
transition
accepted
claim
evidence
```

Require explicit later ADR before use under stage material:

```text
identity
frozen
image
table
record
sequence
order
schema
key
handle
bytes
digest
hash
intern
ordinal
route
cache
scanner
reader
collector
projector
planner
executor
writer
artifact
```

Examples:

```text
stage.canonicalization.contract.equivalence              OK
stage.canonicalization.contract.representation           OK
stage.canonicalization.contract.collision                OK
stage.canonicalization.material.representative           OK
stage.canonicalization.material.reference                OK
stage.canonicalization.material.availability             OK
realization.identity.digest                              OK
realization.storage.frozen.image                         OK

stage.canonicalization.material.identity                 rejected by default
stage.canonicalization.material.hash                     rejected
stage.canonicalization.material.frozen.image             rejected
stage.lowering.material.projection                       rejected
stage.lowering.material.expansion                        rejected
stage.publication.material.report                        rejected by default
```

The package map may move existing risky file names into safe realization locations without renaming the type
immediately. Type renaming is semantic cleanup and must be separated from movement unless compilation requires it.

## 15. Interceptor Removal Boundary Law

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
package law. It must not be depended on by `stage`, `statemachine`, `versioning`, `governance`, or `diagnostic`.

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

## 16. Behavior-Preserving Relocation Law

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
- mixing algorithmic changes with package relocation;
- renaming risky types as part of package movement unless compilation requires it.

Rule:

```text
Move first.
Then replace.
Then rename.
```

## 17. Initial Relocation Guide

This guide is non-exhaustive. The companion placement map is the executable movement plan.

### 17.1. Discovery

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
stage.input.material
stage.admission.contract
stage.admission.material
stage.admission.judgment
stage.admission.diagnostics
realization.metamodel
adapter.classgraph
adapter.jvm
```

Classpath, annotation, or runtime-surface reading belongs to adapters or realization. Results that become contract
presentation material belong under the stage that owns their admission and lowering path.

### 17.2. Linking

Current:

```text
linking
```

Target:

```text
realization.linking
realization.graph
stage.lowering.material
stage.lowering.judgment
stage.admission.judgment
```

Binding and linking are realization mechanics unless the linked result becomes ratified material or stage-local
judgment.

### 17.3. Metamodel and frozen material

Current:

```text
metamodel.domain
metamodel.adapter.reflection
metamodel.domain.frozen
```

Target:

```text
stage.input.material
stage.canonicalization.material
stage.lowering.material
stage.invariant.material
realization.metamodel
realization.normalization
realization.identity
realization.storage
adapter.reflection
```

Reflection-specific material stays outside authority. Adapter-neutral material may move under the stage that owns its
ratified meaning. Physical frozen image/table/record/sequence forms belong under realization storage unless later
promoted.

### 17.4. Planning

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
realization.identity
realization.graph
realization.runtime
realization.cache
stage.canonicalization.material
stage.lowering.material
stage.invariant.material
statemachine.transition.material
statemachine.transition.contract
governance.budget
governance.capacity
```

Planning algorithms are realization machinery. Ratified canonical or lowered material may live under stage-local
`material`. Lifecycle law may move under `statemachine.state` or `statemachine.transition` when it expresses legal
movement rather than physical storage.

### 17.5. Execution

Current:

```text
execution.domain
execution.port
execution.infrastructure
```

Target:

```text
realization.execution
realization.runtime
realization.reporting
statemachine.transition.contract
statemachine.transition.judgment
diagnostic.evidence
stage.invariant.judgment
stage.publication.judgment
stage.publication.claim
adapter.junit
```

Execution is realization. Result resolution is stage-local judgment. Trace and audit evidence are diagnostics.
Externally visible reports and results are publication.

`execution.port` is old hexagonal vocabulary and is not a target package. Incoming execution surfaces must move to the
stage boundary that owns entry. Outgoing reporting surfaces must move to publication, diagnostic, reporting, or adapter
packages according to what they actually do.

### 17.6. Runtime and policy

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
realization.cache
governance.policy
governance.budget
governance.capacity
governance.validity
statemachine.transition
```

Policy resolution belongs to governance when it defines law. Worker backing, storage, lanes, and physical lifecycle
belong to realization runtime unless promoted to state-machine law.

### 17.7. Reporting

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
diagnostic.evidence
diagnostic.retention
realization.reporting
adapter.console
adapter.file
adapter.json
```

A report is publication or diagnostic material derived from accepted judgment/evidence. It is not contract authority.

## 18. Dependency Direction Law

Forbidden:

```text
stage.<stage>.contract -> realization
stage.<stage>.contract -> adapter
statemachine.<manifest|state|transition> -> adapter
stage.<stage>.material.accepted -> adapter.reflection
stage.<stage>.material.accepted -> adapter.ksp
stage.<stage>.judgment -> adapter
governance -> adapter
diagnostic.evidence.contract -> adapter
stage.publication.claim -> adapter
```

Allowed, subject to narrower package laws:

```text
realization -> stage
realization -> statemachine
realization -> versioning
realization -> governance
realization -> diagnostic
adapter -> realization
adapter -> stage.input.boundary when required to deliver raw external input
```

Allowed direction does not mean unrestricted coupling.

## 19. Determinism Law

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
- ordering law must be explicit, stable, and testable;
- physical storage form must not become contract meaning;
- realization locality must not define authority.

The package structure exists to make these laws harder to violate accidentally.

## 20. Explicit State-Machine Axis Reservation

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

## 21. Architecture Tests

The package refactor must introduce architecture tests.

Required categories:

1. **forbidden dependency tests**

   Authority packages must not depend on realization or adapter packages.

2. **stage-local role tests**

   Pipeline-specific contract, material, judgment, diagnostic, and publication types must live under the owning stage.
   Explicit state-machine types must live under the parallel `statemachine` axis. Version-coordinate types must live
   under the parallel `versioning` axis. Governance types must live under `governance`. Cross-stage evidence law must
   live under `diagnostic` when not owned by one stage. Promotion to a top-level package requires a separate reason and
   should be rare.

   `realization.execution.port` and `realization.execution.state` must not be introduced as stable target packages.
   Existing files with those old names must be split or placed under more precise packages during relocation.

3. **material naming tests**

   Stage material packages must not introduce risky implementation names by default: `identity`, `frozen`, `image`,
   `table`, `record`, `sequence`, `order`, `schema`, `key`, `handle`, `bytes`, `digest`, `hash`, `intern`, `ordinal`,
   `route`, `cache`, `scanner`, `reader`, `collector`, `projector`, `planner`, `executor`, `writer`, or `artifact`.

4. **inter-stage acquisition tests**

   One stage must not import another stage's local role packages as ordinary dependencies. Upstream output is treated as
   external input by the receiving stage and must pass through the receiving stage's boundary, guard/admission,
   lowering, and judgment.

5. **adapter isolation tests**

   Reflection, KSP, ClassGraph, JUnit, Mockito, JSON, file, console, and other platform details must not leak into
   authority packages.

6. **contract package content tests**

   Stage-local contract packages may contain only interfaces, immutable declaration objects, stable value records,
   enums, sealed declaration shapes, and small structural types. They must not contain discovery, traversal, lowering,
   planning, execution, caching, publication, reflection, scheduling, I/O, or environment-inspection algorithms.

7. **interceptor isolation tests**

   Target authority packages must not depend on interceptor-style flow. Temporary migration bridges must remain outside
   the target architecture.

8. **material purity tests**

   Accepted material packages must not depend on backend handles or adapter-specific types.

9. **realization inward dependency tests**

   Realization depends inward on authority packages. Authority packages do not depend outward on realization.

10. **bridge discipline tests**

Realization bridge packages may map and assemble between implementation and contract surfaces. They must not declare
contract authority.

## 22. Compliance Rules

A change complies with this ADR only if:

1. Top-level packages do not create a global contract bucket.
2. Pipeline-specific domains are stage-first under `stage.<stage-name>`.
3. `stage` names contract-pipeline gates, not implementation phases.
4. Stage-local role packages are not collapsed into global buckets for convenience.
5. Inter-stage movement follows publication-to-boundary acquisition. The receiving stage must guard, lower, and judge
   external input before downstream-owned material may exist.
6. A stage does not depend on another stage's local contract, material, judgment, diagnostics, publication, helper,
   realization, or adapter packages as ordinary peer dependencies.
7. Stage-local `contract` packages contain only narrow declaration vocabulary and no realization algorithms.
8. Stage-local `material` packages use contract-governed material names, not implementation or storage names.
9. Constructor and property machinery inside contract packages is treated as a JVM/Kotlin representation limit, not as
   contract authority.
10. Compiler-related implementation stays behind the realization boundary and keeps existing Kontrakt domain vocabulary
    unless changed by a later compiler-specific ADR.
11. Runtime, planning, graph, metamodel, identity, execution, cache, storage, and reporting machinery remain under
    `realization` unless explicitly promoted by a later ADR.
12. Adapter-specific code remains under `adapter`.
13. The first relocation pass does not change behavior.
14. Interceptor-style flow is not treated as a target package layer.
15. Interceptor-style files are marked for later removal or replacement.
16. Architecture tests protect the new dependency direction.
17. Existing deterministic material, identity, planning, runtime, and publication laws remain intact.
18. Future multi-pipeline architecture is introduced by module boundary first.

## 23. Consequences

### 23.1. Positive

- Package names align with `What Contract Is`.
- Stage-local obligations remain visible.
- The word `stage` has one meaning.
- Developer work locality remains available under realization.
- Realization may be compiler-like without becoming authority.
- Deterministic laws become easier to locate and protect.
- Material packages no longer become physical-storage buckets by default.
- Realization machinery is separated from authority packages.
- Adapter-specific technology is isolated.
- Interceptor-style flow is marked as replacement work rather than normalized.
- The first pass remains reviewable because movement, behavior changes, and semantic renaming are separated.
- Future multi-pipeline structure can be introduced without changing the stage-first rule.

### 23.2. Negative

- Many files and imports will move.
- Documentation and ADR references may temporarily point to old package names.
- Stage-first packages repeat role names.
- Kotlin/JVM packages do not fully enforce access boundaries.
- Architecture tests become required enforcement, not optional hygiene.
- Some existing filenames contain risky vocabulary that cannot be fully corrected by movement alone.
- Temporary migration artifacts may remain until explicit state-machine replacement is complete.

### 23.3. Accepted cost

The churn is accepted because the current package structure still tells an outdated architectural story.

Repeated role names under stages are accepted because they preserve stage ownership.

Asymmetric contract/realization structure is accepted because implementation is the mirror image of contract authority,
not a competing authority tree.

## 24. Implementation Plan

### 24.1. Ratify package law

Create the target package tree and dependency direction law from this ADR.

### 24.2. Add architecture tests

Add dependency tests before or during file movement.

### 24.3. Move files without semantic edits

Move files according to the target package structure and companion placement map.

Allowed edits are package declarations, imports, directory paths, build configuration, and temporary migration wrappers.

### 24.4. Keep pipeline module boundary out of the current move

Do not add a pipeline-name package layer during this refactor.

### 24.5. Keep realization compiler-like but bounded

Move compiler/runtime implementation into `realization` with existing Kontrakt domain language. Do not introduce generic
compiler taxonomies as top-level architectural authority.

### 24.6. Mark interceptor-style flow as removal/replacement work

Do not give interceptor-era files a normal target package.

Temporary bridges, if required, must remain visibly short-lived and outside the target package law.

### 24.7. Compile and run current tests

The movement pass is complete only after current behavior is preserved.

### 24.8. Prepare explicit state-machine ADR

After relocation, prepare a follow-up ADR for replacing callback/interceptor flow with explicit
state-machine/state/transition flow.

### 24.9. Prepare semantic renaming pass

After movement is stable, prepare a separate cleanup pass for risky type names that still contain implementation
vocabulary in contract-facing packages.

## 25. Final Rule

Kontrakt's package structure must describe contract authority before realization machinery.

Current pipeline-specific domains are stage-first.

A stage is a logical contract-pipeline gate, not a compiler/runtime phase.

A stage owns its own local contract, boundary, material, judgment, diagnostics, and publication role packages as needed.

Stage movement is not peer package dependency. One stage may expose an output presentation at its publication boundary.
The next stage must treat that presentation as external input and run its own boundary, guard/admission, lowering, and
judgment before creating its own material.

This ADR does not create a top-level `contract` package. Contract packages are stage-local unless a later ADR introduces
a concrete non-stage package boundary.

State, transition, and explicit manifest authority belong under `statemachine`.

Version-coordinate authority belongs under `versioning`.

Policy, budget, capacity, capability, and validity law belong under `governance`.

Evidence, retention, and redaction law belong under `diagnostic` when not stage-local.

Compiler-style architecture is internal realization machinery. Realization may be compiler-like and should keep existing
Kontrakt domain vocabulary.

Planning, linking, execution, runtime, metamodel, normalization, identity, graph, cache, storage, and reporting are
realization machinery unless explicitly promoted by contract authority or stage-local authority.

Adapters isolate outside technology.

Contract packages are deliberately small. They hold contract-facing interfaces and immutable declaration material, not
algorithms. Constructors and property machinery that remain there are accepted only as JVM/Kotlin representation limits.

Material packages must not smuggle physical representation or implementation method into authority vocabulary.

Interceptor-style flow is outside the target architecture and is marked for removal or replacement.

The first refactoring pass moves compatible files only.

Future multi-pipeline architecture should be introduced through module boundaries first. Inside each pipeline module,
stages own their own stage-local contract packages.