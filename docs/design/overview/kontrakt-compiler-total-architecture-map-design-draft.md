# Kontrakt Compiler Architecture Map

## Status

**Working design draft. Not an ADR.**

This document gives one concise view of the current Kontrakt compiler architecture.

It does not define new Contract semantics.

`What Contract Is` and Accepted ADRs remain authoritative.

The purpose is to keep the whole V1 compiler visible before individual frontend, IR, verifier, optimizer, backend, QA,
and V2 decisions are finalized.

---

# 1. Core Direction

Kontrakt separates Contract meaning from compiler realization.

```text
Contract
    ↓ constrains
Realization
```

Compiler structures may represent, verify, optimize, lower, cache, or publish Contract meaning.

They do not define it.

```text
JVM shape
IR storage
query graph
cache
object topology
backend layout
    ≠
Contract authority
```

---

# 2. Stage and IR Rule

A compiler stage exists because a new invariant becomes true.

A new IR level is justified when one of these changes:

```text
semantic vocabulary
equivalence relation
persistent invariant
information that may now be discarded
target vocabulary
```

The following alone do not justify a new IR level:

```text
verification completed
optimization ran
material was frozen
storage changed
a cache entry was created
a generation changed
```

Logical stages and physical materialization are separate.

---

# 3. Total Architecture

The compiler has a semantic material graph and a build / product dependency graph. It also has a realization graph that
represents user implementation. These graphs interact through explicit products, but one graph does not become another
kind of semantic relation.

The Contract frontend accepts more than one authoring form. `.kontrakt` source and selected 1D Contract carrier source
are
frontend inputs. A carrier is an immutable authoring data surface. Its host topology does not remain the semantic model
merely because the frontend acquired Contract evidence from it.

```text
┌───────────────────────────────────────────────────────────────────────────┐
│                        Compiler Driver / Session                          │
│ request / target / product demand / resources / generations / reuse      │
│ worker ownership / cancellation / diagnostics / artifact publication     │
└──────────────────────────────────┬────────────────────────────────────────┘
                                   │
                  ┌────────────────┴────────────────┐
                  │                                 │
                  ▼                                 ▼
          Contract Frontend                 Realization Acquisition
                  │                                 │
      Contract Authoring Inputs             User JVM classfiles
          │               │                         │
          │               │                         ▼
      .kontrakt      selected 1D             Realization Body IR
        source       carrier source                   │
          │               │                  Local Structural Analysis
          └───────┬───────┘                          │
                  ▼                                  │
      Source / Syntax / Carrier Material             │
                  │                                  │
      Resolution / Binding / Normalization           │
                  │                                  │
        Resolved Contract HIR                        │
                  │                                  │
      Authority-Owned Establishment                  │
                  │                                  │
                  ▼                                  │
       Canonical Contract World                      │
                  │                                  │
                  │                    Admitted Realization Binding
                  │                                  │
                  └────────────────┬─────────────────┘
                                   ▼
                        Contract-Aware Analysis
                                   │
                  ┌────────────────┼────────────────┐
                  │                │                │
                  ▼                ▼                ▼
         Closure Verification  Specialization  Whole-Machine /
                  │             Knowledge       IPA Knowledge
                  ▼                │                │
        Verification Overlay       └────────┬───────┘
                  │                         │
                  └───────────────┬─────────┘
                                  ▼
                         Execution Formation
                                  │
                     static alternatives omitted
                     runtime judgments retained
                                  │
                                  ▼
                    Contract-Aware Execution IR
                                  │
                        Analysis / Transform Loop
                                  │
                                  ▼
                     Execution IR New Generation
                                  │
                                  ▼
                            JVM Plan / IR
                                  │
                                  ▼
                          Classfile Product
                                  │
                                  ▼
                           HotSpot / Graal
```

This diagram is an overview. Section 4 owns the finer semantic production protocol. In particular, it separates
Definition
Candidates from IDL Binding Candidates and keeps occurrence-specific material outside the Definition world unless the
owning
Contract gives an occurrence result authority of its own.

The frontend boxes are logical architecture boundaries. They do not require one heap object graph or one physical
product
per box. Frontend processing may desugar, normalize, intern, deduplicate, pre-resolve, or compact representation before
Establishment when the published HIR invariant in Section 6 remains true.

The build / product graph has an additional dependency through generated APIs.

```text
.kontrakt
    ↓
Contract Frontend
    ↓
Canonical Contract World
    ↓
Generated Operation / Interaction API Product
    ↓
Host Compiler
    ↓
User Classfiles
    ↓
Realization Acquisition
```

A clean build therefore does not require the Contract frontend and realization acquisition to execute as two fully
independent schedules. Compatible generated APIs and user classfiles may let the compiler reuse existing products. That
is
a scheduling and product-reuse opportunity. It does not add a Contract semantic dependency.

The `Canonical Contract World` must exist before Contract-aware verification or optimization can use authoritative
Contract
Definition meaning.

---

# 4. Main Material Flow

Section 3 shows the whole compiler. This section owns the detailed semantic material flow used by 1D ADR review. It
keeps
Definition production, occurrence application, and realization material separate because they have different authority
rules.

## 4.1. Definition production

Authored Contract material becomes authoritative only after frontend resolution and the owning Definition judgment.

```text
Contract Authoring Inputs
    ↓
Source / Syntax / Carrier Material
    ↓
Resolution / Role Binding / Semantic Normalization
    ↓
Published Resolved Contract HIR
    ├── Definition Candidates
    ├── IDL Binding Candidates
    ├── exact semantic references
    └── adjacent provenance references
    ↓
Authority-Owned Definition Establishment
    ↓
Established Definition Material
    ↓
Canonical Contract World
```

`Definition Candidate` describes the resolved meaning that one authority may establish. `IDL Binding Candidate`
describes
the exact resolved use that selects or relates that Definition in the authored Contract surface. The owning 1D ADR
decides
which contextual differences are Definition determinants and which remain binding-only meaning.

These subjects do not require separate physical objects. They are logical semantic products inside the HIR boundary.

## 4.2. Occurrence application

Definition meaning and application meaning remain separate. An occurrence path exists only when the owning authority
gives
one semantic application result meaning of its own.

```text
Established Definition Reference
    +
exact application / binding
    +
Required Basis where owned
    +
Applicable Context where owned
    +
actual candidate material
        ↓
Owning Occurrence Judgment
        ├── success
        │      ↓
        │  Established Occurrence Material
        │
        └── unsuccessful judgment
               ↓
           exact unsuccessful result meaning
```

Established Occurrence Material does not automatically enter the Canonical Contract World. The owning ADR decides
whether
occurrence meaning exists and which semantic coordinates determine it. A runtime call, query execution, or carrier
object
does not create occurrence identity by itself.

Failure and diagnostics may consume an exact unsuccessful judgment when their own laws permit it. They do not re-run the
source 1D law to reconstruct another answer.

## 4.3. Realization material

The realization path remains non-authoritative.

```text
User Realization
    ↓
Realization Body IR
    ↓
Admitted Realization Binding

Canonical Contract World
+
Realization Body IR
+
Admitted Realization Binding
+
Derived Knowledge
    ↓
Verified Realization
    ↓
Contract-Aware Execution IR
    ↓
JVM Plan / IR
    ↓
Classfile
```

Compiler analysis may derive new realization knowledge from established meaning. That knowledge does not alter the
Definition or occurrence that justified the analysis.

## 4.4. Material-family responsibility

| Material family                    | Responsibility                                                                                        |
|------------------------------------|-------------------------------------------------------------------------------------------------------|
| Source / Syntax / Carrier Material | Preserve authored and frontend-acquired information before exact semantic resolution                  |
| Resolved Contract HIR              | Publish resolved compiler-semantic candidate meaning and exact use relations before authority         |
| Established Definition Material    | Preserve authority-owned Definition meaning after successful Establishment                            |
| Canonical Contract World           | Provide one compiler substrate through which downstream products read Established Definition Material |
| Established Occurrence Material    | Preserve the result of one semantic application when the owning authority defines occurrence meaning  |
| Exact Unsuccessful Judgment Result | Preserve an authority-owned unsuccessful application meaning where the owning law defines one         |
| Derived Knowledge                  | Hold recomputable compiler analysis, summaries, and projections without becoming Contract authority   |
| Realization / Execution IR         | Represent implementation and executable semantics under the invariants of their own IR levels         |

The exact number of physical representations remains open. A logical material family may be physically split or fused
when its authority, information-loss, publication, and consumer-visible invariants remain intact.

## 4.5. Published Semantic Producer Protocol

Every published semantic producer must define its own consumer-visible result before query, cache, or storage design is
chosen. This is a compiler architecture protocol. It is not a new Contract kind.

| Producer responsibility    | Required decision                                                                                       |
|----------------------------|---------------------------------------------------------------------------------------------------------|
| Semantic subject           | What logical subject does this producer compute?                                                        |
| Explicit inputs            | Which semantic or compiler inputs determine the result?                                                 |
| Produced meaning           | What may a consumer rely on after publication?                                                          |
| Equality / equivalence     | When is a recomputed result unchanged for that producer's consumers?                                    |
| Validity boundary          | Which input change makes the published result stale?                                                    |
| Publication boundary       | When is the result complete enough for independent readers?                                             |
| Stable references          | How may later products refer to the result without using object identity or storage address as meaning? |
| Provenance relation        | What source relation remains separately addressable?                                                    |
| Information-loss guarantee | Which distinctions has the producer preserved, and which distinctions has it lawfully erased?           |

Section 39 adds query and dependency orchestration to this producer protocol. It does not redefine the semantic product.

---

# 5. Contract Frontend

The Contract frontend converts authored Contract inputs into resolved compiler-semantic material.

Current authoring inputs include `.kontrakt` source and selected immutable 1D Contract carrier source.

The carrier is a frontend authoring form.

Its host-language class shape, object identity, field layout, and construction topology are not Contract authority.

The two input forms need different acquisition work before they meet at resolution.

```text
.kontrakt
    ↓
Source Manager / Provenance
    ↓
Lexical / Syntax / Recovery Material
    ┐
    │
    ├───────────────┐
    │               │
    │        selected 1D carrier source
    │               ↓
    │        Carrier Acquisition Material
    │               │
    └───────┬───────┘
            ↓
Module / Name / Symbol Resolution
            ↓
Slot / Role-Constrained Binding
            ↓
Frontend Validation / Semantic Normalization
            ↓
Resolved Contract HIR
```

The names above describe logical responsibilities.

They do not require one class, pass, object graph, or compiler product per line.

Its main outputs include:

```text
source identity
source provenance
parsed Contract structure
carrier-acquired frontend facts
recovery / poison state where required
exact authority references
resolved slot bindings
resolved semantic relations
module / import resolution
```

Source location is not semantic identity.

Parser recovery does not establish Contract meaning.

Recovered or poisoned source material must not silently become authoritative semantic material.

Downstream consumers should not repeat source-name lookup or host-carrier inspection to recover meaning already resolved
by the frontend.

Frontend processing may improve representation before Establishment.

Possible work includes:

```text
desugaring
syntax-only distinction removal
semantic normalization allowed by the source language
exact-reference formation
interning
deduplication
pre-resolution
compact compiler representation
```

This is compiler realization work.

It may reduce compile-time work or improve locality.

It must preserve the resolved candidate meaning presented to Establishment.

It must not infer or create Contract authority.

The exact frontend transform set remains open.

The frontend language version is separate from Contract Version.

```text
IDL Language Version
    = grammar and frontend-language compatibility

Contract Version
    = Contract semantic coordinate
```

Feature gates, language compatibility, source spans, and origin chains belong to frontend infrastructure.

They do not define Contract meaning.

---

# 6. Resolved Contract HIR

`Resolved Contract HIR` is the published high-level compiler-semantic representation before Establishment. Section 4
owns
the material-flow role of HIR. This section defines the invariant that a HIR producer must preserve.

HIR is not temporary parser output. It is the stable result of frontend resolution that later compiler work may consume
without reopening source syntax or repeating semantic resolution.

The HIR preserves rich Contract vocabulary. It may represent exact references to:

```text
Input
Admission
Canonicalization
Lowering
Fact
Invariant
State / Transition
Budget
Capacity
Version
Policy
Governance
Failure
Publication
Output
```

One published HIR generation may contain several logical semantic subjects. Their physical storage may still be shared.

| HIR semantic subject                        | Required meaning                                                                                          |
|---------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| Contract / Interaction / Operation subjects | Exact resolved subjects needed by later Contract processing                                               |
| Definition Candidates                       | Authority-qualified candidate meaning after 1D-owned determinant projection                               |
| IDL Binding Candidates                      | Exact use relation that selects a Definition Candidate in the resolved IDL context                        |
| Exact semantic references                   | Resolved targets required to interpret a candidate without lexical lookup                                 |
| Basis Requirement Law                       | The declared requirement when that law is part of Definition meaning; not the future Basis Binding result |
| Applicability Law                           | The declared applicability rule when Definition-owned; not a future applicability judgment result         |
| Platform preservation obligations           | Only resolved platform-visible obligations still required by later authority or legal host observation    |
| Provenance references                       | Adjacent source relation that remains outside HIR semantic identity                                       |

Definition Candidate meaning and IDL Binding Candidate meaning remain distinct. The same Definition Candidate may be
used
by more than one binding when the owning 1D ADR says that the different use context does not change Definition meaning.

The main HIR invariant is:

```text
source ambiguity resolved
+
required semantic references exact
+
owning candidate meaning complete enough for Establishment
+
syntax-only distinctions erased only when later semantic work no longer needs them
```

This creates an information-loss boundary. If Establishment, generated API projection, legal platform reprojection, or a
later authority still needs a distinction, frontend processing must preserve that distinction before Visible HIR is
published. Establishment must not repair an incomplete HIR by reopening Java, Kotlin, `.kontrakt`, or another authoring
surface.

Recovery or poison information may remain available to frontend diagnostics, but it cannot masquerade as a complete
valid
Definition Candidate. Material that cannot satisfy the HIR publication invariant does not become authoritative merely by
being assigned a HIR row or handle.

HIR owns compiler-resolved meaning. It does not own Contract authority. Resolution success therefore does not establish
a
Contract Definition.

HIR semantic meaning and source provenance remain separate. A source-only change may update provenance while leaving the
resolved HIR meaning unchanged. The compiler may reuse the semantic result when the producer's equality and validity
rules
prove that it is unchanged.

The HIR should be published behind a stable read boundary before independent consumers share it. Ordinary consumers do
not
mutate a published HIR generation in place.

A later compiler may use immutable backing, phase-qualified material, overlays, or replacement generations. Those
choices
remain implementation as long as the published invariant is preserved.

One logical HIR generation does not imply one monolithic dependency unit. The producer may expose stable Definition,
binding, or provenance projections over physically aggregated material. This keeps V2 incremental granularity open
without
making query structure part of HIR semantics.

---

# 7. Authority-Owned Establishment

Establishment is the semantic boundary where an owning Contract or State-Machine law grants authority to resolved
candidate
meaning. It is not ordinary compiler lowering, and it does not discover semantic information that the frontend failed to
resolve.

Definition Establishment follows this protocol:

```text
Authority-qualified Definition Candidate
    +
exact semantic subject
    +
exact required semantic references
    +
Definition-time Required Basis meaning where owned
    +
Definition-time Applicable Context where owned
        ↓
Owning Definition Judgment
        ↓
Established Definition Material
```

The candidate must already contain the meaning required by its owning ADR. Establishment must not reopen source names,
inspect host-object containment, discover platform behavior, or infer meaning from realization topology.

Successful Definition Establishment produces the authority-specific material defined by ADR-0063.

```text
Established Definition

Definition Meaning
    → exact authoritative meaning owned by this Definition

Definition Reference
    → exact authoritative Definition identity

Direct Established Relations
    → only relations owned directly by this Definition meaning
```

Source provenance remains adjacent to this material. It is not part of Definition Meaning or Definition Reference.

If Definition Establishment does not succeed, no Established Definition Material is created for that candidate. The
compiler must retain enough exact subject and rejection information for the owning refusal rule and diagnostics to
preserve
the distinction between an unresolved frontend problem, an unsuccessful authority judgment, and a later realization
failure. A Contract-specific unsuccessful result exists only when the owning law defines one.

Occurrence Establishment is a separate application relation. Where an authority owns occurrence meaning, it follows the
application protocol in Section 4.2 and ADR-0063. It is not folded into Definition Establishment merely because the same
compiler subsystem happens to execute both judgments.

Kontrakt must not replace the authority-specific results with one universal `EstablishedMaterial` semantic model.

---

# 8. Canonical Contract World

The `Canonical Contract World` is the compiler substrate for already-established Contract Definition meaning. It is not
an
optimization IR and it does not replace the authorities that established the material.

The world provides one coherent read surface over authority-owned Definition material. Its logical contents are
constrained
by the owning ADRs and ADR-0063.

| World surface                       | Meaning                                                                                               |
|-------------------------------------|-------------------------------------------------------------------------------------------------------|
| Established Definition Material     | Exact authoritative meaning produced by each owning Contract or State-Machine authority               |
| Definition References               | Exact references to authoritative Definitions                                                         |
| Direct Established Relations        | Relations already owned and established by the relevant Definition law                                |
| Basis relations                     | Basis Resolution or Binding relations only after the responsible composition law has established them |
| Applicability relations             | Authoritative applicability material only where an owning law has established it                      |
| Version-aware meaning               | Exact Version Binding and Version-sensitive Definition meaning owned by the Contract model            |
| Policy / Governance / State context | Established context only where the respective authority owns that meaning                             |
| Provenance handles                  | Compact relation to source provenance; provenance is not semantic identity                            |

The world must not infer a transitive Contract relation merely because the compiler can derive one. Reachability,
closure,
summary indexes, and consumer-specific projections remain derived compiler knowledge unless an owning law establishes
the
same meaning directly.

Established Occurrence Material is separate from the Canonical Contract World by default. An authority may define
occurrence meaning as described in Section 4.2, but a diagnostic or runtime consumer does not justify inserting every
occurrence into the Definition world.

The Canonical Contract World also does not contain the realization call graph, query dependency graph, diagnostic
provenance graph, or optimization summaries as Contract authority. Those structures keep the owners defined elsewhere in
this document.

The physical world representation remains replaceable. A unified logical read surface does not require one universal
`EstablishedMaterial` object model or one giant object graph.

---

# 9. Frozen Publication

Establishment and compiler publication are different.

```text
Establishment
    = Contract authority boundary

Freeze / Seal / Publish
    = compiler publication boundary
```

The publication rule is not limited to the Canonical Contract World.

Published HIR, Realization Body IR, analysis results, summaries, and other shared compiler products may use the same
pattern when stable shared reads are required.

```text
private construction
    ↓
producer validation
    ↓
seal / freeze / publish
    ↓
read-only consumers
```

Publication does not add Contract authority.

A separate `Frozen IR` is not required merely because material is immutable.

A logical publication boundary also does not require a full physical copy.

Backing storage may be shared when the published invariant remains protected.

Existing HID, frozen publication, dense storage, and slab work may be reused behind appropriate publication boundaries.

The exact publication granularity remains a compiler design choice.

---

# 10. Canonical Contract World Products

The Canonical Contract World feeds sibling compiler products. Section 8 owns the authoritative read surface. This
section
only fixes the direction of consumption.

| Consumer                           | Authoritative input                                                                                         | Derived product                       |
|------------------------------------|-------------------------------------------------------------------------------------------------------------|---------------------------------------|
| Reference Judgment                 | Established Definition Material plus the exact candidate or occurrence inputs required by the judgment      | Reference Result                      |
| PBT / Fixture / Unit-Test Planning | Exact established obligations and semantic partitions                                                       | Deterministic Test Plan and witnesses |
| Contract Coverage                  | Exact obligation and semantic-subject references                                                            | Coverage product                      |
| Diagnostics                        | Exact semantic references, adjacent provenance, and the evidence appropriate to the failure class           | Structured Diagnostic                 |
| Generated APIs                     | Established definitions and any platform-facing obligation that must remain observable at the host boundary | Interaction / Operation API Product   |
| Contract-Aware Analysis            | Established Contract meaning together with realization material and valid local analysis                    | Derived Contract-relative knowledge   |
| Verification                       | Established Contract meaning together with admitted realization and valid analysis                          | Verification Result / Overlay         |
| Execution Formation                | Established Contract meaning, verified realization, and valid specialization knowledge                      | Contract-Aware Execution IR           |

A consumer must not reopen source or host declarations to reconstruct meaning that the frontend and Establishment
already
produced. A consumer also must not add its own convenience field to Established Definition Material merely to simplify
one
implementation.

`Shared Contract-Derived Knowledge` remains an optional compiler seam. It is useful when several consumers need the same
recomputable projection.

```text
Canonical Contract World
    ↓
Shared Contract-Derived Knowledge
    ├── verifier
    ├── optimizer
    ├── diagnostics
    └── execution formation
```

The exact summary families remain open. A consumer may read the exact Canonical Contract World directly when that is the
better boundary.

Sibling products may reuse validated derived knowledge, but that reuse does not create a semantic authority chain. The
owning Contract remains the source of Contract meaning.

---

# 11. Realization Frontend

The realization frontend acquires the user implementation as compiler facts.

A classfile-centered V1 path is the production direction.

```text
Generated Operation / Interaction API
    ↓
Host compilation
    ↓
User JVM classfiles
    ↓
Realization acquisition
    ↓
Published Realization Body IR
```

External composition and implementation binding are separate from classfile acquisition.

```text
External Composition / DI
        ↓
Effective implementation binding
        ↓
Realization Admission Boundary
        ↓
Admitted Realization Binding
```

The admission boundary is also the realization airlock.

External technology may exist outside the governed core through explicit adapters.

Hidden technology must not become an undeclared factual ingress into the governed realization.

Examples include:

```text
reflection / dynamic proxy
MethodHandle / invokedynamic
JNI / FFM downcall
filesystem / clock / network / DB access
dynamic class loading
framework interception
```

The exact supported feature matrix remains a verifier decision.

The architecture rule is stable:

```text
external technology
    ↓
explicit adapter / composition boundary
    ↓
realization admission
    ↓
inspectable governed realization
```

The exact acquisition mechanism remains replaceable.

User implementation structure does not create Contract meaning.

---

# 12. Realization Body IR

The Realization Body IR is the published analyzable representation of user implementation structure.

It exists for implementation analysis and verification.

It should expose enough structure for:

```text
control flow
value definition/use
calls
effects
origin
unsupported constructs
```

A working representation may contain:

```text
method/type tables
per-function CFG
value relations
call/effect sites
summary references
```

SSA is not fixed yet.

The important requirement is explicit analyzable control and data relation.

Acquisition-local mutable state is not the published realization product.

Publication should produce stable read-only realization material before shared analysis consumes it.

---

# 13. Early Realization Processing

Some work can happen before Contract-aware verification.

Examples:

```text
classfile normalization
structural validation
CFG formation
def-use construction
obvious constant cleanup
contract-independent canonicalization
```

This work must not hide a realization violation.

Aggressive Contract-based elimination should not occur before the Contract context and required legality are known.

The factual realization should remain recoverable for verification and diagnostics.

---

# 14. Local Structural Analysis

Realization-local analysis does not require Contract meaning.

Possible products include:

```text
reachability
dominance
call graph
call targets
SCC
raw effect summary
raw origin summary
def-use
basic escape information
```

Analysis identity should include its logical subject and validity context.

A working view is:

```text
analysis kind
+
subject / scope
+
analysis context
+
input generation / identity
```

Possible scopes include:

```text
method
Operation realization
Core
Whole-Machine
```

These are derived compiler knowledge.

They are not Contract authority.

---

# 15. Contract-Aware Analysis

Contract-aware analysis begins only after both sides are available.

```text
Canonical Contract World
+
Realization Body IR
+
Local Structural Analysis
    ↓
Contract-Aware Analysis
```

This is a central Kontrakt middle-end boundary.

It may derive:

```text
fixed Policy World context
fixed Contract Version context
Governance Binding context
known applicable State surface
exact admitted realization binding
Contract-relative effect classification
Contract-relative origin classification
known impossible alternatives
static judgment candidates
specialization contexts
closed call-target knowledge
```

This knowledge is shared by verification, execution formation, and optimization.

---

# 16. Core Realization Closure Verification

Closure verification depends on established Contract meaning.

```text
Canonical Contract World
+
Realization Body IR
+
Admitted Realization Binding
+
valid Contract-Aware Analysis
    ↓
Core Realization Closure Verification
```

Possible results:

```text
verified
proven violation
unsupported / inconclusive
```

Closure reasoning must distinguish different target classes.

```text
exact admitted target
known finite target set
open / unresolved virtual target
opaque external target
unsupported dynamic target
```

Unknown optimization opportunity and unknown verification evidence are different.

```text
optimizer cannot prove exact target
    → keep dynamic behavior

verifier cannot prove required closure
    → may refuse under the V1 support rule
```

Reflection, native calls, dynamic loading, framework interception, and other opaque capabilities therefore need an
explicit support rule.

Under the selected V1 support rule, unsupported realization may cause compile refusal.

Verification machinery remains compiler realization.

It does not define Contract meaning.

---

# 17. Verification Overlay

Verification does not require a full second IR.

A working model is:

```text
Realization Body IR Generation G
+
Verification Result / Overlay
```

The overlay may publish verified summaries such as:

```text
exact call target
closed reachable graph
no external factual dependency
non-escaping value
stable realization binding
origin/effect proof result
```

Downstream consumers should reuse valid verified knowledge instead of repeating closure analysis.

---

# 18. Specialization Knowledge

Some established Contract context is fixed before governed execution.

Examples:

```text
Policy World
Contract Version
Governance Binding
known State surface
exact realization binding
```

These are strong specialization inputs.

Example:

```text
selected Policy World = A
    ↓
Policy World B impossible
```

The compiler may use this knowledge to avoid materializing B-specific execution machinery.

The optimizer consumes the selection.

It does not make the selection.

---

# 19. Formation-Time Pruning

Not every removable path needs to be generated and later deleted.

When established Contract meaning already proves an alternative impossible:

```text
static Contract context
    ↓
Execution Formation
    ↓
only applicable execution material
```

This is better understood as formation-time pruning or specialization.

It is not general DCE.

Examples:

```text
unselected Policy worlds
impossible Version alternatives
unselected Governance bindings
statically impossible Contract machinery
known State-surface alternatives
```

---

# 20. CFG-Based Elimination

Some dead paths cannot be removed from Contract meaning alone.

They require realization control-flow analysis.

Typical flow:

```text
Realization CFG
+
fixed Contract context
    ↓
constant / applicability propagation
    ↓
infeasible edge
    ↓
CFG simplification
    ↓
unreachable block removal
```

The CFG therefore remains an important proof and optimization substrate.

---

# 21. General DCE

General dead-code elimination usually needs more than Contract context.

Typical knowledge:

```text
CFG
def-use
liveness
effect knowledge
reachability
```

DCE may run several times.

```text
specialization
    ↓
constant propagation
    ↓
CFG simplification
    ↓
DCE
    ↓
inlining
    ↓
new dead material
    ↓
DCE
```

The exact pass order remains open.

---

# 22. Execution Formation

Execution Formation is where established Contract meaning and verified user realization become executable compiler
material.

```text
Canonical Contract World
+
Verified Realization
+
Specialization Knowledge
    ↓
Execution Formation
    ↓
Contract-Aware Execution IR
```

Execution Formation may already:

```text
omit impossible alternatives
resolve exact bindings
materialize only runtime-required judgments
preserve exact authority references
```

It should not lower away high-level Contract knowledge too early.

---

# 23. Contract-Aware Execution IR

This IR is distinct from both the Canonical Contract World and the Realization Body IR.

It represents executable Contract-aware semantics.

Possible vocabulary includes:

```text
runtime Contract judgment
realization call
value
control flow
Failure relation
State movement reference
Publication relation
Output relation
exact Contract authority reference
```

Important distinctions remain visible.

```text
State Transition
    ≠ CFG edge

Contract Failure
    ≠ JVM exception

Publication
    ≠ return instruction
```

JVM stack/local details do not belong here.

---

# 24. Execution IR Analysis

Execution IR has its own analysis layer.

Possible analyses include:

```text
CFG reachability
dominance
SSA / value relations where useful
effect refinement
alias / escape where justified
Contract-context propagation
specialization opportunities
cost inputs
```

Analysis results have explicit validity.

They are reusable only while their input generation remains valid.

---

# 25. Contract-Specific Optimization

Kontrakt should optimize where it has knowledge that the JVM does not naturally have.

Candidate V1 transformations include:

```text
static judgment discharge
fixed-context specialization
exact binding
unreachable Contract-path removal
generated wrapper removal
temporary carrier elimination
simple stage fusion
dead realization alternative removal
cheap verification-derived scalarization
```

These transformations preserve Contract meaning.

They do not establish new Contract meaning.

---

# 26. Generic Cleanup

Contract-specific transforms may expose ordinary compiler opportunities.

Examples:

```text
constant propagation
CFG simplification
DCE
small inlining
allocation removal
GVN-like simplification where useful
```

Kontrakt should use generic cleanup when it is cheap and useful.

It should not attempt to reimplement the full HotSpot/Graal optimizer.

---

# 27. Legality and Profitability

Every optimization has two separate decisions.

```text
Legality
    = does it preserve required meaning?

Profitability
    = is the legal transform worth applying?
```

A cost model cannot make an illegal transform legal.

Runtime profile information is not static Contract truth.

---

# 28. Analysis Reuse and Invalidation

Analysis is shared while valid.

```text
Material Generation G
    ↓
Analysis Result
    ├── Verifier
    ├── Optimizer
    └── other consumers
```

After a transform, an analysis result has three possible validity paths.

```text
preserved
    → reuse

incrementally maintained
    → publish updated analysis result

not preserved
    → invalidate
```

Invalidated analysis may be recomputed on demand.

Stale analysis is not valid compiler knowledge.

The exact Analysis Manager implementation remains open.

V2 may use domain-specific repair rather than one universal invalidation algorithm.

---

# 29. Optimization Generations

Optimization does not automatically create a new IR level.

```text
Execution IR Generation N
    ↓
meaning-preserving transform
    ↓
Execution IR Generation N+1
```

If vocabulary and equivalence remain the same, both generations satisfy the same Execution IR Contract.

---

# 30. Whole-Machine Analysis

Whole-Machine work should not require one giant full IR.

Working direction:

```text
Core A Summary ┐
Core B Summary ├──→ Whole-Machine Summary / Index
Core C Summary ┘
```

The summary is derived compiler knowledge.

It is not Contract authority.

Whole-Machine analysis may support:

```text
closure decisions
cross-Core dependency reasoning
fixed-context specialization
global target pruning
local optimization decisions
```

The summary should also drive selective body opening.

```text
Whole-Machine Summary / Index
        ↓
global decision
        ↓
required body set
        ↓
selective materialization
        ↓
local / parallel work
```

Full body material is opened only where needed.

Summary identity and body identity remain separate.

---

# 31. Verification and Optimization Summaries

Verification and optimization may need different summaries.

```text
Verification Summary
    = soundness-critical verifier knowledge

Optimization Summary
    = transform planning knowledge
```

They may share backing storage.

They should not be treated as one authority.

---

# 32. JVM Backend Boundary

JVM-specific lowering begins after Contract-specific simplification.

```text
Optimized Contract-Aware Execution IR
    ↓
JVM Capability / Legalization
    ↓
JVM Plan / IR
```

The backend does not re-resolve Contract meaning.

It lowers already-resolved execution material into JVM vocabulary.

Kontrakt owns the correctness of the classfile product it emits.

A JVM verifier, class loader, JIT, or external classfile API is not Contract authority and is not a substitute for
Kontrakt backend correctness.

External encoding facilities may remain replaceable adapters.

JDK or third-party object models should not become the semantic model of the backend.

---

# 33. JVM Plan / IR

The JVM Plan / IR is target-specific.

A useful logical unit is a typed JVM method plan.

Possible vocabulary:

```text
JVM value forms
invocation forms
branches
returns
throws
locals
exception regions
CFG relations
type states
required frame states
constant-pool references
classfile constraints
```

The same backend analysis should be reused where possible.

```text
JVM legalization / planning
    ↓
Typed JVM Method Plan
    ├── instructions
    ├── CFG
    ├── type states
    ├── exception edges
    └── required frame states
```

This avoids discarding compiler knowledge and reconstructing it later only for classfile completion.

This is a separate logical level because target vocabulary has changed.

The exact physical schema remains open.

---

# 34. Classfile Emission

The V1 production backend direction is direct classfile construction.

```text
Typed JVM Method Plan
    ↓
Classfile Construction
    ├── bytecode
    ├── StackMapTable
    ├── exception table
    ├── constant pool
    └── attributes
    ↓
internal structural / compliance checks
    ↓
classfile encoding
    ↓
valid JVM classfile
    ↓
JVM verifier / loader
```

Frame and metadata derivation should reuse JVM planning facts where possible.

It should not require an avoidable second reconstruction of CFG and type-state knowledge.

A generated Java / Kotlin source backend may exist as:

```text
bootstrap path
reference backend
debug path
differential-testing backend
```

It is not the V1 production backend baseline.

The exact direct encoder remains replaceable.

---

# 35. JVM Handoff

Kontrakt should perform semantic simplification that depends on Kontrakt knowledge.

The JVM should keep its strengths.

Kontrakt:

```text
Contract specialization
static discharge
exact Contract binding
generated machinery simplification
```

HotSpot / Graal:

```text
runtime speculation
profile-guided inlining
generic escape analysis
register allocation
instruction selection
machine optimization
```

Target-aware physical planning remains a separate derived optimization seam.

```text
Derived Access Profile
+
Target Hardware Profile
    ↓
Physical Layout Plan
    ↓
FFM-backed / slab realization
```

Target layout does not become Contract meaning.

The target profile is not assumed to be the compiler host profile.

---

# 36. IR Classification

| Material                        |                   IR? | Role                                                                                                    |
|---------------------------------|----------------------:|---------------------------------------------------------------------------------------------------------|
| Source / Syntax Material        | source representation | authored structure                                                                                      |
| **Resolved Contract HIR**       |               **Yes** | resolved Contract semantics before Establishment                                                        |
| Established Definition Material |                    No | authority-owned Definition meaning                                                                      |
| **Canonical Contract World**    |                **No** | compiler substrate for established Definition meaning                                                   |
| Established Occurrence Material |                    No | authority-owned result of one semantic application where the owning Contract defines occurrence meaning |
| Frozen World Generation         |                    No | compiler publication state                                                                              |
| **Realization Body IR**         |               **Yes** | analyzable user realization                                                                             |
| Local / Contract-Aware Analysis |                    No | derived compiler knowledge                                                                              |
| Verification Overlay            |                    No | verified property over one realization generation                                                       |
| **Contract-Aware Execution IR** |               **Yes** | executable Contract + realization representation                                                        |
| Optimized Execution Material    |       usually same IR | new Execution IR generation                                                                             |
| Whole-Machine Summary           |                    No | derived global index                                                                                    |
| **JVM Plan / IR**               |               **Yes** | target-specific representation                                                                          |
| Classfile                       |                    No | target artifact                                                                                         |
| Query / Product Result          |         not by itself | compiler product                                                                                        |
| HID / Dense Ordinal             |                    No | lookup / addressing / equality evidence mechanism                                                       |
| Source Provenance               |                    No | source relation                                                                                         |

Established Definition Material and Established Occurrence Material are semantic authority products, not IR levels. The
Canonical Contract World provides a compiler substrate for Definition material. Occurrence material follows the separate
boundary described in Sections 4.2 and 8.

---

# 37. Current Strongest IR Family

```text
Contract Authoring Inputs
    ↓
Source / Syntax / Carrier Material
    ↓
Resolved Contract HIR
    ↓
Establishment
    ↓
Canonical Contract World

User Implementation
    ↓
Realization Body IR

Canonical Contract World
+
Verified Realization
    ↓
Contract-Aware Execution IR
    ↓
JVM Plan / IR
    ↓
Classfile
```

The HIR is a compiler-semantic baseline.

The Canonical Contract World is authority-bearing definition substrate.

Derived summary or projection material may exist between these major families without becoming another IR level.

The exact number of internal sublevels remains open.

---

# 38. Lowering Map

Compiler lowering and the 1D `Lowering Contract` are different.

```text
Contract Authoring Inputs
    ↓ acquire / parse / resolve / normalize
Resolved Contract HIR

Resolved Contract HIR
    ↓ Establishment
Canonical Contract World

User Classfile
    ↓ realization acquisition
Realization Body IR

Canonical Contract World
+
Verified Realization
    ↓ Execution Formation
Contract-Aware Execution IR

Execution IR
    ↓ JVM target lowering
JVM Plan / IR

JVM Plan / IR
    ↓ emission
Classfile
```

`acquire / parse / resolve / normalize` may contain several logical frontend responsibilities.

They need not create several physical IR levels.

Pre-Establishment normalization preserves resolved candidate meaning.

Execution optimization occurs later under the invariants of its own IR level.

---

# 39. Product and Query Orchestration

Kontrakt produces several major compiler products. Published semantic products follow the producer protocol defined in
Section 4.5. Query orchestration does not replace that protocol.

A query-oriented V1 interface is currently selected because some products benefit from demand-driven computation, reuse,
and dependency tracking. Passes remain local processing mechanisms, and not every calculation needs to become a query.

For a product that participates in query orchestration, the query layer adds dependency information to the
producer-owned
result.

```text
Published Product
    +
Recorded Dependencies
    +
Generation Validity
        ↓
Reusable Query Result
```

The query identity is compiler realization. It does not replace the semantic subject or identity defined by the product
producer.

Logical material size and query granularity remain separate decisions. A physically aggregated HIR or Contract World may
expose stable projections without turning every internal row into an independent query.

```text
Aggregate frontend material
    ├── Definition projection A
    ├── Definition projection B
    └── Provenance projection P
```

A change to the aggregate representation does not invalidate every downstream product when the exact semantic input to a
projection is unchanged. This gives V1 a dependency seam without fixing V2 to one incremental algorithm.

Persistent or externally stored compiler products need additional artifact lifecycle checks. Those checks may cover
schema
compatibility, corruption, stale-generation rejection, and target compatibility. They remain product-publication rules
rather than Contract semantics.

Semantic products and provenance products may have different validity boundaries. Persistent compiler products are
derived material. Deleting them may reduce performance, but it must not change Contract meaning.

---

# 40. Query Architecture Is Replaceable

The current query-oriented V1 decision does not make one traversal algorithm permanent.

Keep these roles separate.

```text
HID
    → identity / equality evidence / early cutoff

Merkle structure
    → hierarchical change localization

Query
    → computation / demand / dependency interface

Projection
    → fine-grained stable view over larger material

Cache tier
    → reusable-result retention

Incremental repair
    → how changed derived material is repaired
```

Projection boundaries may act as change-propagation firewalls.

For example, a large HIR generation may change physically while an unchanged definition-level projection remains a
valid input for later work.

The following repair strategies must remain replaceable:

```text
pull validation
push invalidation
change-frontier propagation
delta maintenance
hybrid scheduling
domain-local repair
priority worklists
incremental / full-rebuild switching
```

V1 should preserve product and dependency boundaries.

V2 is not assumed to use one compiler-wide repair algorithm.

Different compiler domains may use different algorithms.

---

# 41. Reuse Layers

Reuse occurs at several levels.

```text
Source / Provenance
    → source acquisition and diagnostic reuse

Resolved Contract HIR
    → frontend semantic reuse

Canonical Contract World
    → shared authoritative semantic substrate

Shared Contract-Derived Knowledge
    → repeated Contract-side analysis reuse

Shared Analysis
    → verifier / optimizer reuse

Verification Overlay
    → no full verified IR copy

IR Backing
    → shared immutable storage / overlay
```

Semantic reuse and provenance reuse need not have the same validity boundary.

```text
provenance-only change
    +
resolved semantic result unchanged
        ↓
semantic downstream may remain reusable
```

The compiler must prove the unchanged semantic result under the producer's validity rule.

A source offset or object address is not sufficient evidence.

The currently implemented L1 / L2 structure is planning-specific.

```text
Planning L1
    → worker / session-local hot planning state

Planning L2
    → wider canonical planning-result reuse
```

They are not yet one compiler-wide generic cache hierarchy.

Future frontend, analysis, verifier, optimizer, and backend domains may reuse the same tiering principle.

Each domain must own its own:

```text
key meaning
equality / validation rule
lifetime
invalidation boundary
physical representation
retention policy
```

A future persistent tier may provide cross-session reuse for selected products.

Cache is work avoidance.

It is not authority.

---

# 42. Identity Separation

Keep these separate:

```text
source content / revision identity
source provenance identity
frontend semantic identity
Contract semantic identity
IR semantic identity
HID / fingerprint
generation identity
dense ordinal
table row
memory address
JVM object identity
```

Typical roles:

```text
Source / Revision Identity
    → authored input revision

Source Provenance Identity
    → where authored material came from

Frontend Semantic Identity
    → equality of resolved compiler-semantic material

Contract Semantic Identity
    → authoritative Contract meaning

HID
    → lookup / equality evidence

Dense Ordinal
    → local addressing

Generation
    → validity boundary
```

Equal source position does not imply equal semantic meaning.

Different source position does not imply different resolved or established meaning.

Frontend semantic identity may therefore support early cutoff before later Contract or compiler products are rebuilt.

---

# 43. Graph Separation

Kontrakt contains several different graphs.

```text
Contract semantic graph
realization call/effect/origin graph
CFG / data-flow graph
analysis dependency graph
compiler product dependency graph
build / artifact dependency graph
diagnostic provenance graph
Whole-Machine summary graph
incremental propagation graph
```

They must not collapse into one universal graph.

```text
CFG edge
    ≠ Contract State Transition

query dependency
    ≠ Contract dependency

realization call edge
    ≠ Required Basis relation

build dependency
    ≠ semantic dependency
```

A compressed propagation graph, SCC-condensed graph, reachability index, or transitive reduction is derived compiler
structure.

It must not silently replace semantic dependency meaning.

---

# 44. Diagnostics

Diagnostics are structured compiler products.

They may consume:

```text
frontend result
Established Contract material
verification result
optimization result
backend result
source provenance
```

Working shape:

```text
Diagnostic Code
Semantic Reference
Provenance Reference
Arguments
Related Notes
```

Rendering is separate.

Evidence kind depends on the failure class.

```text
Contract Judgment Failure
    → Contract authority evidence is primary

Realization Verification Failure
    → deterministic provenance witness is primary

User Implementation Failure
    → implementation provenance / stack trace is primary

Compiler / runtime Crash
    → operational failure evidence
```

A verification provenance witness is not a runtime stack trace.

A Contract Failure is not a JVM exception.

Contract Diagnostic Evidence remains distinct from compiler diagnostics.

---

# 45. Reference Judgment

Reference Judgment is a sibling of the optimized path.

It should remain simpler and sufficiently independent.

Definition material alone is not enough for an actual judgment.

```text
Canonical Contract World
+
actual candidate / established occurrence material
+
applicable execution context
    ↓
Reference Judgment
    ↓
Reference Result
```

It may intentionally recompute selected judgments.

Its purpose is validation independence, not runtime performance.

Reference Judgment does not create Contract authority.

---

# 46. PBT / Fixture / Unit-Test Products

Generated tests come from exact Contract obligations.

```text
Established Obligation
    ↓
Deterministic Test Plan
    ↓
Cases
    ↓
Reference Judgment
    ↓
Expected Result
    ↓
Generated Test Artifact
```

Expected behavior must not be learned from user realization.

PBT is not verifier authority.

---

# 47. Compiler QA

Compiler QA is separate from generated user Contract tests.

V1 should include:

```text
frontend regression
parser fuzzing
semantic regression
Establishment regression
realization closure regression
IR verifier tests
analysis tests
optimizer source-target comparison
JVM codegen tests
reference-vs-optimized differential tests
determinism tests
cache-on / cache-off equivalence
performance baselines
```

Production compiler development also needs observability.

```text
stage / query timing
memory and allocation metrics
optimization remarks
invalidation / reuse trace
backend diagnostics
crash reproducer
reducer support where practical
```

Observability is not Contract authority.

Compiler correctness must not depend on one verifier.

---

# 48. Transform Validation

IR verification is an architecture boundary, not only a test category.

Possible checkpoints include:

```text
Realization Body IR publication
Execution Formation
meaning-preserving transform
JVM planning
classfile construction
```

Important transforms also need independent preservation checks.

Possible mechanisms:

```text
IR verifier
local validator
translation validation
differential execution
Reference comparison
metamorphic testing
```

The validator should not simply reuse the transform's own legality code.

Heavy checks may be configurable.

The invariant itself is not optional.

---

# 49. Determinism and Publication

The compiler should prefer:

```text
immutable published material
worker-local mutation
explicit merge
stable identity
deterministic publication
```

Product dependency and physical scheduling dependency are different.

```text
product A depends on product B
    ≠
worker A must run immediately after worker B
```

Parallel work should preserve deterministic semantic publication.

Worker completion order must not decide:

```text
Contract meaning
semantic identity
verification result
required output ordering
```

The Driver / Session also owns compilation resources.

```text
memory envelope
worker ownership
scratch lifetime
cancellation
published generation
old-generation reclamation
artifact publication
```

Cancellation must not expose a partially published generation as a successful compiler product.

Clean, cached, parallel, cancelled-and-retried, and reused compilation must preserve the same semantic result for the
same valid inputs.

---

# 50. V1 Required Skeleton

V1 should preserve at least these architecture boundaries.

```text
Source Manager / Provenance
Lexical / Syntax / Recovery
1D Carrier Acquisition Boundary
Module / Name / Symbol Resolution
Slot / Role-Constrained Binding
Frontend Validation / Semantic Normalization
Contract Frontend
Resolved Contract HIR Publication
Definition Candidate / IDL Binding Candidate Distinction
Authority-Owned Definition Establishment
Established Definition Material / Canonical Contract World
Occurrence Candidate / Judgment Result Boundary where owned
Unsuccessful Judgment Meaning Boundary where owned
Frozen Publication
Generated API Product Boundary

Realization Acquisition
Realization Admission / Airlock
Admitted Realization Binding
Realization Body IR
Local Structural Analysis
Contract-Aware Analysis
Core Closure Verification
Verification Overlay
Dynamic / Opaque Capability Support Rule

Execution Formation
Contract-Aware Execution IR
IR Verification
Analysis / Transform Infrastructure
Contract-Specific Optimization
Generic Cleanup
Whole-Machine Summary / Selective Body-Opening Seam

JVM Legalization / Planning
Typed JVM Method Plan
Direct Classfile Construction / Emission

Reference Judgment
PBT / Test Planning
Structured Diagnostics
Compiler QA
Observability / Reproducer

Driver / Session
Resource Ownership
Stable Identity
Generation Validity
Product / Query Boundaries
Fine-Grained Projection Seam
Dependency Recording
Semantic / Provenance Validity Separation
Artifact / Product Publication
Existing Planning L1 / L2 Reuse
V2 Incremental Evolution Seam
```

The Definition Candidate, binding, occurrence, and unsuccessful-result boundaries above are logical semantic boundaries.
They do not require one physical product or object graph for each line.

The exact physical split remains open where a semantic or target-level boundary does not require another representation.
The listed frontend responsibilities likewise do not require one implementation class or pass per line.

---

# 51. V2 Evolution Seam

V2 should extend this architecture rather than replace it.

Possible additions:

```text
persistent product state
cross-session reuse
multiple immutable generations
incremental lexing / parsing
incremental name / symbol resolution
persistent frontend semantic results
provenance-only refresh
fine-grained HIR projections
frontend semantic early cutoff
Merkle change localization
incremental analysis repair
change-frontier propagation
delta-maintained analysis
lazy materialization
summary persistence
artifact reuse
incremental test planning
incremental / full-rebuild switching
advanced scheduling
prediction-guided profitability or scheduling
profile-guided profitability
```

No one incremental algorithm is fixed.

Different compiler domains may use different repair strategies.

The common requirement is smaller:

```text
explicit inputs
stable product identity
deterministic computation
frozen publication
clear dependency boundary
semantic / provenance separation
fine-grained stable access where useful
replaceable reuse / repair policy
```

A source revision may change while a resolved semantic result remains reusable.

A resolved semantic result may change while unrelated definition-level products remain reusable.

V1 should not prevent either boundary.

Prediction or historical telemetry may change work order or profitability decisions.

It must not change compiler correctness or Contract meaning.

---

# 52. Intentionally Open

This document does not freeze:

```text
exact authoring carrier acquisition mechanism
exact lexer / parser representation
exact frontend normalization set
exact HIR count
exact HIR schema
exact HIR physical publication granularity
in-place phase-qualified HIR vs immutable HIR generations
exact fine-grained frontend projection set
exact Execution IR operations
SSA form
CFG physical layout
analysis manager API
pass manager API
query scheduler
pull / push / hybrid incremental execution
red-green adoption
fingerprint algorithm
final HID encoding
exact FFM / slab layout
Whole-Machine summary schema
shared Contract-derived summary schema
frozen table layout
direct classfile encoder implementation
optimizer pass order
cost model
persistent cache design
prediction / scheduling model
```

Some directions are no longer open at the same level.

```text
V1 production backend
    → direct classfile path

Generated source backend
    → reference / bootstrap / debug / differential role

high-cardinality physical direction
    → FFM-backed primitive slabbing by default
```

The exact physical layout, migration boundary, target profile, and encoder remain replaceable.

---

# 53. 1D ADR Review Map

Each 1D ADR must define enough semantic material for the frontend, Establishment, and later compiler consumers to work
without reconstructing that Contract from source syntax or host implementation. The ADR should define only meaning owned
by that authority. It must not add fields only because one current compiler subsystem finds them convenient.

A question may have the answer `not owned`. That is a complete answer when the Contract truly does not own the concept.
The review must distinguish that case from an accidental omission.

## 53.1. Definition and HIR completeness

| Question                      | 1D ADR must decide                                                                                                          |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| Authority                     | What exact judgment does this Contract own, and where does that ownership stop?                                             |
| Authoring surface             | What must the author state explicitly before frontend resolution can begin?                                                 |
| Definition meaning            | What does one Definition mean after authoring syntax has been removed?                                                      |
| Definition determinants       | Which semantic differences make two Definition Candidates different?                                                        |
| Resolved HIR surface          | What exact resolved meaning must the frontend produce for this authority?                                                   |
| HIR information-loss boundary | Which distinctions must still exist when Visible Resolved HIR is published?                                                 |
| Definition / use separation   | Which meaning belongs to the reusable Definition Candidate, and which meaning belongs only to the exact IDL use or binding? |
| Required semantic references  | Which referenced definitions or semantic subjects must already be exact before Establishment?                               |
| Provenance relation           | What source origin must remain addressable without becoming semantic identity?                                              |
| HIR equivalence               | When may two resolved frontend products be treated as the same consumer-visible HIR meaning?                                |

The HIR questions do not require the 1D ADR to define a table layout, Kotlin class, query key, or serialization format.
They define the semantic payload that ADR-0071 must be able to represent.

## 53.2. Definition Establishment completeness

| Question                         | 1D ADR must decide                                                                                                     |
|----------------------------------|------------------------------------------------------------------------------------------------------------------------|
| Establishment input              | What must be complete before the owning Definition judgment may begin?                                                 |
| Required Basis law               | Does the Definition declare required basis meaning, and if so what meaning is required?                                |
| Definition-time applicability    | Does Establishment depend on an applicability relation owned by this Contract?                                         |
| Establishment judgment           | What does the owning authority actually decide over the resolved candidate?                                            |
| Established Definition Material  | What exact authoritative meaning enters the Canonical Contract World?                                                  |
| Direct established relations     | Which relations are owned directly by this Definition rather than derived later?                                       |
| Definition identity              | Which authority, Version, and authority-local coordinates identify the Definition under ADR-0063?                      |
| Unsuccessful Definition judgment | If Definition Establishment does not succeed, what exact subject and unsuccessful meaning must remain distinguishable? |

Established Definition Material must remain authority-specific. The review must not invent a universal payload shared by
all 1D Contracts.

## 53.3. Application and occurrence completeness

| Question                        | 1D ADR must decide                                                                                           |
|---------------------------------|--------------------------------------------------------------------------------------------------------------|
| Occurrence model                | Does this Contract give one semantic application result meaning of its own?                                  |
| Application context             | Which exact use, binding, scope, or already-established context determines one legal application?            |
| Occurrence candidate            | What material may be presented to the occurrence judgment?                                                   |
| Candidate stability             | What must remain stable or coherent while the judgment depends on that candidate?                            |
| Occurrence determinants         | Which semantic coordinates distinguish one occurrence from another?                                          |
| Occurrence judgment             | What exact result does the authority decide for one application?                                             |
| Established Occurrence Material | If occurrence meaning exists, what exact result becomes authoritative?                                       |
| Unsuccessful occurrence result  | What exact unsuccessful judgment meaning exists before Failure or diagnostics consume it?                    |
| Preservation lifetime           | Which distinctions must remain available after the judgment, and when may realization erase or replace them? |

A runtime call does not define occurrence identity by itself. A physical carrier does not become occurrence authority
merely
because it contains the judged value.

## 53.4. Cross-Contract and compiler-consumer completeness

| Consumer concern    | 1D ADR must make possible                                                                                         |
|---------------------|-------------------------------------------------------------------------------------------------------------------|
| Failure             | Failure can consume the exact unsuccessful judgment meaning without re-running the source 1D law.                 |
| Policy / Governance | Selection or binding can address the exact Definition or application relation without redefining the 1D meaning.  |
| Diagnostics         | A semantic subject, result, and related provenance can be referenced without reopening source syntax.             |
| Reference Judgment  | The judgment can be reproduced independently from the same authoritative meaning and exact application inputs.    |
| PBT / test planning | Legal, illegal, and boundary partitions can be derived from declared obligations rather than host behavior.       |
| Generated APIs      | A host projection can preserve every Contract-visible and still-required platform-visible distinction.            |
| Verification        | Realization obligations can be stated without making verifier internals part of the Contract.                     |
| Execution Formation | The exact runtime-required judgment and authority references can be preserved when executable material is formed. |
| Optimization        | Static knowledge can remove physical work without removing or rewriting the Contract judgment.                    |
| Summary derivation  | Shared summaries can be derived without adding summary-only fields to authority material.                         |

These are consumer-read requirements. They do not require the producer 1D to create separate verifier, PBT, diagnostic,
or optimizer payloads.

## 53.5. Evolution and representation completeness

| Question                         | 1D ADR must decide                                                                                               |
|----------------------------------|------------------------------------------------------------------------------------------------------------------|
| Semantic equality                | What result equality is owned by this Contract, and what equalities belong to another layer?                     |
| Incremental determinant set      | Which semantic inputs can invalidate the resolved or established result?                                         |
| Reuse boundary                   | Which unchanged producer-visible result may allow downstream early cutoff?                                       |
| Semantic / provenance separation | Can source-only change refresh provenance while leaving semantic material reusable?                              |
| Platform preservation            | Which externally observable platform obligations remain live, and where may they be discharged?                  |
| Representation freedom           | Which carrier, storage, snapshot, table, cache, layout, and optimization choices remain replaceable realization? |

If downstream work needs semantic material that is absent from this review, return to the owning ADR.

Do not invent the missing meaning in HIR, the Canonical Contract World, a verifier summary, an execution IR, or a
backend
product.

---

# 54. Final Working View

The final view combines the material flow in Section 4 with the realization path in Sections 11 through 35.

```text
Contract Authoring Inputs
    ↓
Contract Frontend
    ↓
Published Resolved Contract HIR
    ↓
Authority-Owned Definition Establishment
    ↓
Canonical Contract World
    ├── Generated API Products
    ├── sibling Contract-derived products
    └── Contract-aware compiler consumers

Generated API Product
    ↓
Host Compilation
    ↓
Realization Acquisition / Admission
    ↓
Contract-Aware Analysis and Verification
    ↓
Execution Formation
    ↓
Contract-Aware Execution IR
    ↓
Analysis / Transform Loop
    ↓
JVM Legalization / Typed Method Planning
    ↓
Direct Classfile Product
```

Definition Candidate, IDL Binding Candidate, Definition Establishment, and occurrence-specific material follow the
semantic production protocol defined in Section 4. This summary does not collapse those boundaries.

The Contract frontend may improve its own compiler representation before Establishment. Section 6 owns the HIR invariant
that limits that work. Later optimization follows the same rule at its own semantic level: representation may change,
but the meaning owned by that level must be preserved.

The supporting compiler architecture is defined in Sections 39 through 51. Those sections own product orchestration,
identity, graph separation, diagnostics, validation, publication, V1 boundaries, and V2 evolution. This final view does
not restate those rules.

The central rule remains:

```text
Contract meaning first.

Compiler semantic material may exist before authority.

Authority is created only by the owning Contract or State-Machine law.

Compiler-derived knowledge consumes established meaning where authority is required.

Optimization preserves the meaning owned by its input level.

Physical realization remains replaceable.
```

---

# 55. Basis of This Draft

This draft is aligned with the current Kontrakt direction from:

```text
What Contract Is
ADR-0063
ADR-0070

Kontrakt Established Contract World Architecture TODO
Kontrakt Contract-Aware Realization Optimization TODO
Kontrakt IR Subsystem Contract / Implementation Separation Design
Kontrakt V1 Commercial Compiler Foundation Candidate Architecture
Kontrakt V2 Reference Architecture and V1 Foundations
Kontrakt V2 Incremental Architecture Research TODO
Kontrakt Query-Oriented Compiler Design
Kontrakt Verifier Candidate Implementation Plan

Modern Compiler Architecture 01–15
```

The modern compiler material contributes general engineering principles:

```text
stage invariants
multi-level IR
logical / physical separation
frontend source / semantic separation
semantic checkpoint publication
CFG / SSA / data-flow analysis
analysis / transformation separation
analysis reuse and invalidation
context-sensitive and interprocedural analysis
summary-driven whole-program work
progressive lowering
legality / profitability separation
Reference and differential checking
structured diagnostics
compiler QA
resource ownership
incremental architecture as a cross-cutting concern
JVM / JIT handoff
```

Additional external architecture references were used only as engineering evidence.

```text
rustc
    → AST-to-HIR desugaring
    → compiler-friendly HIR
    → query / incremental dependency tracking
    → fine-grained projection over larger material

Kotlin K2 / FIR
    → frontend IR with phase invariants
    → logical resolution phases without requiring one new IR per phase

Swift compiler request evaluator
    → immutable declaration direction
    → lazy derived semantic requests
    → cached dependency-aware results

Clang frontend / serialized AST
    → source provenance retention
    → reusable frontend semantic material
    → lazy loading of persisted frontend state

MLIR
    → analysis / transformation separation
    → explicit preservation / invalidation
    → canonicalization as an optimization-enabling IR concern
```

These systems are references for isolated architecture principles.

They are not templates for Kontrakt.

They do not override Kontrakt Contract semantics or current accepted ADRs.