# ADR-0074: Compiler Result, Explicit Unsuccessful Result, Recovery, and Observation Boundary

## Status

Proposed

## Date

2026-09-18

## Related

- `/what-contract-is.md`
- `/0057-failure-contract-explicit-machine-failure-attribution-and-realization-boundary.md`
- `/0060-diagnostic-evidence-and-retention-contract.md`
- `/0061-kontrakt-compiler-diagnostic-architecture.md`
- `/0063-contract-establishment-occurrence-applicability-and-semantic-dependency.md`
- `/0071-resolved-contract-hir-semantic-boundary-deterministic-visibility-lifecycle-and-reuse.md`
- `/0073-kontrakt-jvm-platform-external-technology-and-realization-boundary.md`
- `/kontrakt-compiler-total-architecture-map-design-draft.md`
- `/kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`
- `/kontrakt-v2-reference-architecture-and-v1-foundations.md`
- `/kontrakt-v2-incremental-architecture-research-todo.md`
- *Modern Compiler Architecture 01-15*

---

## 1. Context

Kontrakt already requires user Contract Machine failure to become explicit machine meaning.

ADR-0057 does not allow an authoritative unsuccessful Contract or State-Machine judgment to disappear into an exception,
log entry, sentinel, backend status, stack trace, or implementation-specific control path. The authority that owns the
requirement establishes the unsuccessful result. Failure preserves that result without judging the requirement again.

The Kontrakt compiler itself has a related but different problem.

The compiler performs many judgments and product-forming computations before, between, and after Contract authority
boundaries. Examples include parsing, exact resolution, HIR formation, HIR seal verification, compiler-side
Establishment
machinery, protocol compatibility checks, verifier judgments, persistent-product validation, optimization legality,
backend capability checks, IR verification, and code-generation preparation.

These compiler judgments can also be unsuccessful.

If compiler failure is represented only by implementation control flow, later machinery must reconstruct what happened
from exception type, call stack, query state, mutable manager state, logs, diagnostic text, or a chain of wrapper
errors.
That recreates the same reconstruction problem that ADR-0057 rejects for Contract Failure.

The compiler cannot solve this by declaring every internal problem to be an ADR-0057 Failure. ADR-0057 owns user
Contract
Machine Failure. A compiler implementation error, an HIR seal rejection, an unsupported compiler protocol surface, a
transient cache result, a cancelled query, or an invalid compiler IR is not automatically a user Contract Failure.

The compiler also cannot solve the problem by making the Diagnostic subsystem the owner of compiler failure. Diagnostics
explain and present compiler results. A diagnostic may itself be stale, incomplete, suppressed, rendered differently, or
unavailable. Those facts must not decide whether the compiler judgment succeeded.

Kontrakt therefore needs one common compiler law for explicit compiler results, explicit unsuccessful compiler results,
legal result observation, recovery separation, diagnostic separation, failure containment, and deterministic reuse.

This ADR defines that common law.

It does not define the exact unsuccessful meaning of every compiler subsystem. Each producer remains responsible for its
own judgments and result vocabulary.

---

## 2. Scope and Authority

This ADR governs **Kontrakt compiler-owned result semantics**.

It applies when the compiler itself owns a judgment or compiler-product boundary whose result must remain explicit after
the local implementation call returns.

Typical subjects include:

- source and syntax processing,
- exact resolution,
- Resolved Contract HIR formation,
- HIR seal verification,
- compiler protocol compatibility,
- compiler-side Establishment machinery,
- Canonical Contract World construction machinery,
- realization acquisition and admission machinery,
- Realization Body IR construction and verification,
- Contract-aware analysis products,
- execution formation,
- optimization legality and transformation products,
- persistent-product validation,
- incremental reuse validation,
- backend capability judgments,
- JVM-oriented IR and classfile-production machinery,
- compiler diagnostics,
- compiler recovery and orchestration.

This ADR does **not** take semantic authority from a Contract, State-Machine, or realization source.

When Kontrakt compiler code physically performs an owning Contract judgment, the result meaning remains owned by that
Contract law. ADR-0057 remains authoritative when that owning judgment establishes Contract Failure.

This ADR may govern how compiler machinery transports, observes, diagnoses, reuses, or reacts to that result. It does
not
rename a Contract Failure as a compiler failure or replace the source-owned Failure meaning.

The distinction is:

```text
user Contract / State-Machine / realization authority
    → owns authoritative machine result

compiler-owned judgment
    → owns compiler result

compiler machinery executing an authority-owned judgment
    → does not acquire that authority merely because it executes the code
```

Each 1D ADR continues to own its own candidate meaning, judgment law, successful meaning, and failure distinctions.

ADR-0063 continues to own common Contract Establishment semantics.

ADR-0061 continues to own compiler Diagnostic architecture, subject to the result ownership law in this ADR.

ADR-0071 continues to own Resolved Contract HIR meaning and HIR Candidate Protocol law, subject to the compiler-result
law
for unsuccessful HIR-related compiler judgments.

---

## 3. Problem

Compiler failure is commonly represented too late and too indirectly.

A low-level operation observes a problem. An exception, status code, `null`, poison value, callback, or query result
carries
the condition upward. Each layer may wrap it. A top-level handler later inspects the accumulated implementation history
and
tries to infer the real failed requirement.

```text
physical symptom
    ↓
implementation propagation
    ↓
wrapper
    ↓
wrapper
    ↓
stack / cause / query graph
    ↓
late reconstruction
    ↓
"what actually failed?"
```

This design has semantic and engineering costs.

The failure owner is unclear.

The meaning of the failure depends on implementation topology.

Changing call structure, query boundaries, worker layout, async scheduling, or storage can change the apparent failure
chain even when the compiler judgment is unchanged.

A user may receive thousands of stack frames and still have to discover which compiler requirement was unsatisfied.

The compiler may repeatedly chase references or traverse transitive cause chains merely to reconstruct information that
was already known at the failure boundary.

The opposite design is also wrong.

If every helper, function, query, probe, cache lookup, iterator, and speculative branch creates a first-class result
object,
explicitness becomes a new source of overhead. The compiler replaces exception propagation with result propagation and
creates a large generic error ontology.

A third failure mode appears when diagnostics own the result.

```text
compiler condition
    ↓
diagnostic recognition
    ↓
diagnostic occurrence
    ↓
compiler failure inferred from diagnostic state
```

This makes rendering, diagnostic suppression, provenance refresh, or diagnostic bugs participate in compiler truth.

A fourth failure mode appears when recovery rewrites failure.

A retry, clean recomputation, migration, alternate lowering path, new source revision, or fresh worker may later
succeed.
That later success must not make the earlier unsuccessful result disappear or retroactively turn it into success.

A fifth failure mode appears under incremental compilation.

An old rejection can remain cached after the relevant source, compiler capability, protocol surface, producer law, or
validity basis changes. A negative cached result must not become permanent compiler truth merely because it is cheap to
reuse.

Kontrakt needs explicit compiler results without turning the compiler into a recursive error graph.

---

## 4. Decision Drivers

Compiler result meaning must be explicit at the judgment that owns it.

A compiler result must not have to be reconstructed from stack shape, exception hierarchy, query topology, worker state,
log ordering, diagnostic text, or another implementation artifact.

Explicitness must not require a result object for every implementation call.

The compiler must remain free to use tables, primitive arrays, slabs, compact tags, ranges, interned references, mapped
pages, content-addressed products, or another representation.

Semantic distinction must not imply physical object nesting or repeated reference traversal.

A later compiler layer must not manufacture a wrapper failure merely because an earlier required result was
unsuccessful.

An unreached computation is not a failed computation.

Recovery must remain separate from the result it reacts to.

Diagnostics must remain consumers of compiler result meaning rather than owners of that meaning.

Stack traces, query traces, IR dumps, pass histories, reproducers, and deep causal graphs are engineering evidence. They
may
be retained and inspected when useful. They must not define compiler result meaning.

Normal compiler failure handling must remain bounded enough for malformed or adversarial input not to force unbounded
secondary analysis.

Independent compiler work may continue when its required inputs remain available and trustworthy.

An internal invariant failure may invalidate a larger trust domain than an ordinary user-source rejection. Continuation
policy must account for that distinction.

Clean deterministic computation remains the correctness reference path for reusable compiler products.

Incremental reuse, cache state, persistent artifacts, HID, fingerprints, Merkle roots, epochs, query state, or compiler
history may accelerate validation. They do not create compiler-result authority.

Worker scheduling, discovery order, physical completion order, memory address, and hash-table iteration must not decide
which compiler results exist or which result is semantically primary.

---

## 5. Core Distinctions

The following concepts are not interchangeable.

```text
Contract Failure
    ≠ Compiler Unsuccessful Result

Compiler Unsuccessful Result
    ≠ Compiler Diagnostic

Compiler Unsuccessful Result
    ≠ Recovery

Compiler Unsuccessful Result
    ≠ Crash

Compiler Unsuccessful Result
    ≠ Cancellation

Compiler Unsuccessful Result
    ≠ Supersession

Compiler Unsuccessful Result
    ≠ Unreached Computation

Compiler Unsuccessful Result
    ≠ Reuse Miss

Compiler Unsuccessful Result
    ≠ Not-Reusable Result

Compiler Result
    ≠ Query Node

Compiler Result
    ≠ Stack Frame

Compiler Result
    ≠ Diagnostic Code
```

A subsystem may define a more specific distinction when its law requires one.

This ADR does not force all of these concepts into one enum, object hierarchy, or universal record.

---

## 6. Compiler Judgment and Result Boundary

### 6.1. Result Boundaries Are Owned Compiler Boundaries

An explicit compiler result belongs to an owned compiler judgment or compiler-product boundary.

An implementation function does not become a result boundary merely because it can return unsuccessfully.

A helper call, query, table probe, cache lookup, iterator, traversal step, speculative candidate test, or temporary
computation does not become a first-class compiler-result boundary merely because it can observe a local problem.

A compiler boundary is a result boundary when the result has independent compiler meaning and at least one of the
following is true:

- another compiler responsibility must consume the result without re-running the producer,
- the result decides whether later compiler work is legal or reachable,
- the result decides whether a compiler product may become visible,
- the result participates in reuse or current-validity judgment,
- the result may be an input to recovery or containment,
- the result occurrence must remain distinguishable after the producing implementation call ends.

The exact physical query boundary may be finer or coarser than the compiler-result boundary.

```text
query boundary
    = computation / scheduling / reuse concern

compiler-result boundary
    = owned judgment / observable compiler-product concern
```

A future incremental engine may create many internal query nodes without creating the same number of compiler-result
kinds.

### 6.2. Result Granularity Follows Owned Meaning

A whole compiler phase is not automatically one result boundary.

If independent semantic or compiler-product units can succeed or fail independently, the producer may establish their
results independently.

The granularity is the smallest owned judgment or product occurrence that must remain independently observable under the
producer law.

This does not mean the smallest function, table row, field, query node, or physical allocation.

### 6.3. Success and Unsuccessful Result Are Producer-Owned

A producer defines what counts as successful completion of its owned judgment.

A producer also defines the exact unsuccessful meanings that its judgment can establish.

This ADR does not create one compiler-wide failure taxonomy that replaces those producer-owned meanings.

---

## 7. Establishment of an Unsuccessful Compiler Result

### 7.1. First Exact Owning Judgment

An unsuccessful compiler result is established by the first owning compiler judgment that has enough information to
state
exactly that its owned requirement is not satisfied.

The first physical symptom is not necessarily the result owner.

A deep helper may discover an empty table slot. That does not by itself establish a Resolution Rejection when other
legal
resolution alternatives remain.

The Resolution judgment establishes the rejection only when it can state the exact resolution requirement and determine
that the requirement is unsatisfied.

```text
local symptom
    ↓
owner continues the judgment required by its law
    ↓
owner can state exact unsatisfied requirement
    ↓
explicit unsuccessful compiler result
```

The physical detection site may differ from the semantic compiler-result owner.

That difference must not make result meaning depend on reconstructing the call stack.

### 7.2. Speculative and Provisional Outcomes Are Not Established Results

A speculative branch, trial candidate, solver branch, temporary probe, profitability experiment, or fallback attempt
does
not establish an unsuccessful compiler result merely because that local attempt was rejected.

A result becomes an unsuccessful compiler result only when the owning judgment commits that outcome as the result of its
owned requirement.

This prevents search-space failures from becoming compiler failure explosions.

### 7.3. No Wrapper Failure Propagation

A higher compiler layer does not establish a new unsuccessful result merely because a lower unsuccessful result
prevented
entry.

```text
Resolution Rejection R
    ↓
HIR formation requires successful Resolution
    ↓
HIR formation is not entered
```

This does not automatically establish an HIR Formation Rejection.

A higher layer may establish its own unsuccessful result only when:

- the higher judgment was actually entered,
- it owns a distinct requirement,
- that requirement was actually judged,
- the higher owner can state that requirement as unsatisfied exactly.

A prerequisite-success condition that exists only to gate entry into a later judgment does not create a second failure
meaning when the prerequisite is unsuccessful.

### 7.4. Unreached Is Not Failed

A computation that was never entered has no synthetic unsuccessful result.

The compiler may retain an availability or orchestration relation explaining why the computation was not entered.

That relation is not a fabricated producer result.

This rule is consistent with ADR-0063 Non-Establishment and ADR-0057 Failure containment.

---

## 8. Compiler Result Material and Legal Observation Protocol

### 8.1. Result Material and Result Protocol Are Different

The producer owns compiler result material.

A legal consumer observes that result through the **Compiler Result Protocol**.

The Protocol is a logical observation law. It is not a required DTO, heap object, class hierarchy, serialization format,
copy step, virtual interface, or one accessor call per field.

```text
Compiler Judgment
    ↓
Compiler Result Material
    ↓
Compiler Result Access Boundary
    ↓
Compiler Result Protocol
    ↓
legal consumer
```

The same immutable physical backing may support producer logic and legal observation when the observation law remains
preserved.

### 8.2. Common Law Plus Producer-Specific Typed Projection

Kontrakt does not define one universal `CompilerFailure` record with every possible field.

The Protocol uses:

```text
common compiler-result observation law
    +
producer-specific typed result projection
```

Examples of producer-specific unsuccessful projections may include:

- Resolution Rejection,
- HIR Seal Rejection,
- compiler protocol compatibility rejection,
- verifier rejection,
- persistent-product validation result,
- backend capability rejection,
- internal compiler invariant failure.

The producer ADR or design that owns the judgment defines the exact unsuccessful meaning.

The common protocol must not replace those meanings with one generic `ErrorCode`, `FailureKind`, or property bag.

### 8.3. Unsuccessful Result Observation

For an unsuccessful compiler result, the Protocol must make the following logical information directly observable when
it
is part of that producer result:

- the owning compiler judgment,
- the exact compiler subject,
- the exact producer-owned unsuccessful meaning,
- the result occurrence or active compiler boundary needed to distinguish the occurrence,
- the producer-defined Direct Basis required to interpret the result,
- the directly retained provenance promised by this ADR or the producer law.

A producer may make one of these components structural rather than storing a redundant scalar field.

For example, a typed HIR Seal Rejection projection need not store a separate `kind = HIR_SEAL_REJECTION` value when the
projection type already fixes that role.

### 8.4. Direct Observation Is Not Physical Duplication

Directly observable does not mean physically inline.

A physical representation may use primitive tables, columnar storage, slabs, interned references, compact tags, ranges,
side tables, shared immutable products, or another deterministic layout.

The requirement is that a legal consumer can obtain the exact result meaning through the declared Protocol without
recursive semantic reconstruction.

```text
bounded indexed access
    → permitted

recursive traversal to discover what failed
    → forbidden as the normal meaning path
```

### 8.5. Consumer Bypass Is Forbidden

A legal consumer must not bypass the Result Protocol to reconstruct result meaning from:

- exception type,
- stack shape,
- diagnostic text,
- query graph,
- worker state,
- mutable manager state,
- backing-table containment,
- allocator identity,
- cache state,
- persistent storage path,
- source syntax that the producer already resolved.

A consumer may access separate evidence or provenance protocols when its own responsibility requires them.

That access does not make those products the source of result meaning.

---

## 9. Direct Basis

### 9.1. Purpose

An unsuccessful result must be interpretable without rebuilding its reason from arbitrary compiler history.

The producer therefore defines a **Direct Basis** for each unsuccessful-result kind that needs such context.

The Direct Basis contains the bounded producer-owned material needed to interpret the unsuccessful result at its own
compiler boundary.

It is not automatically the full determinant closure.

It is not automatically a globally minimal explanation.

It is not automatically the material a human diagnostic should display.

### 9.2. Direct Basis Requirements

A mandatory Direct Basis must be:

- sufficient to interpret the producer-owned unsuccessful result,
- deterministic,
- bounded according to the producer's result law,
- obtainable from information already owned or directly produced by the judgment,
- independent of an unbounded secondary root-cause search,
- independent of stack or arbitrary transitive query traversal.

The Direct Basis need not be globally minimal.

A producer must not make normal result establishment perform conflict minimization, whole-graph slicing, minimal
unsat-core
search, transitive root-cause ranking, or another unbounded explanation computation merely to create the result.

### 9.3. Bounded Does Not Mean One Global Numeric Limit

This ADR does not impose one compiler-wide maximum such as eight references per unsuccessful result.

Different judgments have different sufficient witnesses.

A uniqueness violation may need two conflicting alternatives.

A structural mismatch may need expected and observed shape.

A cycle may have an arbitrarily long full witness.

A no-applicable-candidate judgment may depend on a larger producer computation whose exact search result is represented
by
a separate producer product.

The producer must define a bounded direct observation that does not make the core result grow automatically with an
unbounded transitive compiler graph.

### 9.4. Full Evidence Is Separate

When complete determinant, conflict, proof, cycle, trace, or candidate material is larger than the mandatory Direct
Basis,
that material belongs to a separate compiler evidence or analysis product.

```text
Explicit Result F
    exact meaning already observable
    bounded Direct Basis already observable

optional Evidence Reference
    ↓
full conflict set
full cycle witness
constraint material
query trace
IR snapshot
reproducer
other deep evidence
```

The result may reference such a product.

The consumer must not need to traverse that product merely to discover what the unsuccessful result means.

---

## 10. Direct Provenance and Deep Evidence

### 10.1. Result Provenance Is Shallow by Default

The compiler should retain enough direct provenance to identify where the result was produced and to support later
bounded
explanation or forensic analysis.

Direct provenance may include an exact producer boundary, current compiler occurrence/generation relation, and directly
relevant predecessor relation when the producer law requires it.

This ADR does not require the normal result to retain a full causal graph, call stack, whole query history, or
transitive
pass history.

### 10.2. Evidence Does Not Define the Result

Stack traces, query traces, dependency paths, IR dumps, pass histories, proof objects, source excerpts, reproducers,
profiling data, operating-system records, and backend exceptions may be useful evidence.

They do not define compiler-result meaning.

Retaining evidence does not require eagerly traversing, symbolizing, formatting, or rendering that evidence.

```text
retain
    ≠ traverse
    ≠ symbolize
    ≠ explain
    ≠ render
```

### 10.3. Evidence Acquisition May Be Conditional

A normal user-source rejection should not pay the cost of capturing every engineering detail.

Internal compiler errors, debug builds, explicit diagnostic depth, reproducer generation, crash handling, or targeted
investigation may request richer evidence.

That richer evidence must not change the underlying compiler result.

---

## 11. Propagation and Containment

### 11.1. Result Meaning Is Source-Local

An unsuccessful compiler result remains owned by the judgment that established it.

The result does not acquire wrapper identities as it crosses compiler layers.

```text
Resolution Rejection
    ↛ HIR Failure wrapper
    ↛ Establishment Failure wrapper
    ↛ Backend Failure wrapper
    ↛ Driver Failure wrapper
```

### 11.2. Failure Effect May Propagate

The fact that result meaning remains source-local does not mean control consequences remain local.

A dependent compiler product may become unavailable.

A boundary may be unable to complete successfully.

A scheduler may need to stop work, continue independent work, invoke recovery, or terminate the compilation request.

Those **availability and completion consequences** may propagate as far as orchestration requires.

```text
source-local unsuccessful result
        ↓
product availability consequence
        ↓
boundary completion consequence
        ↓
scheduler / driver decision
```

The propagated consequence is not a copied unsuccessful meaning.

### 11.3. Independent Work

An unsuccessful result does not automatically stop compiler work that is independent of that result and remains within a
trustworthy compiler domain.

Continuation requires both:

- the required input meaning to remain available, and
- the relevant compiler state or product domain to remain trustworthy.

Exact keep-going policy may vary by compiler boundary.

### 11.4. Open Representation Decision

This ADR intentionally does not yet freeze the exact physical or logical representation of:

- product availability,
- boundary completion,
- multiple unsuccessful-result membership,
- blocked requested products,
- compiler-run completion.

Those decisions must preserve the source-local result law above.

They are listed as open work in Section 25.

---

## 12. Recovery

### 12.1. Recovery Is Separate from the Result

An unsuccessful compiler result states the result of one compiler judgment.

It does not decide what happens afterward.

Recovery is a separate compiler responsibility.

```text
Compiler Judgment J1
    ↓
Unsuccessful Result R1
    ↓
Recovery Decision
    ↓
new attempt J2
    ↓
new Result R2
```

A later successful result does not rewrite R1.

### 12.2. Recovery May Create a Later Attempt

Depending on the producer and compiler boundary, legal recovery may include:

- deterministic clean recomputation,
- selective recomputation,
- retry under the same semantic requirements,
- validated migration,
- an alternate semantics-preserving compiler realization,
- a new source revision,
- a fresh isolated worker or session after a contained internal failure.

This list does not grant automatic legality to every recovery mechanism.

The producer or recovery owner must define the preconditions under which the later attempt is valid.

### 12.3. Recovery Must Not Invent Missing Meaning

Recovery may not fabricate a compiler or Contract meaning that the applicable owning law did not establish.

A fallback may choose a less optimized but semantically valid compiler path.

A fallback may not silently drop a required semantic distinction merely because a cheaper implementation is available.

A migration may preserve a compatible result surface.

A migration may not invent required meaning that the old product never contained.

### 12.4. Recovery Must Be Bounded

Recovery must not create an unbounded retry or fallback loop.

The exact representation of a Recovery Decision, attempt identity, retry budget, and recovery chain is intentionally
open
for later work.

This ADR fixes only that recovery is separate, later, non-rewriting, and semantics-preserving.

---

## 13. Diagnostics

### 13.1. Diagnostic Is a Consumer of Result Meaning

A compiler diagnostic does not establish the underlying compiler result merely by recognizing or rendering it.

The direction is:

```text
compiler judgment
    ↓
explicit compiler result
    ↓
legal result observation
    ↓
diagnostic analysis / projection
    ↓
structured diagnostic occurrence
    ↓
CLI / IDE / CI / machine rendering
```

ADR-0061 must follow this direction.

### 13.2. Diagnostics May Add Explanation

Diagnostics may join the exact result with:

- source provenance,
- related declarations,
- conflict reduction,
- source excerpts,
- notes,
- fix suggestions,
- bounded causal explanation,
- optional deeper evidence.

Those additions do not become the source result.

### 13.3. Root-Cause Analysis Is a Derived Product

The first exact compiler result is not necessarily the best human root-cause explanation.

A type checker, verifier, optimizer, or compiler bug may detect a contradiction at one boundary while a useful
source-level
explanation points elsewhere.

Transitive causal traversal, slicing, conflict minimization, counterexample ranking, multi-trace analysis, compiler-step
analysis, and similar root-cause work are derived diagnostic or forensic computations.

They must not be required merely to establish the exact compiler result.

### 13.4. Diagnostic Failure Does Not Rewrite Compiler Result

A diagnostic may fail to render, lose optional provenance, exceed an error budget, be suppressed by policy, or be
invalidated by a source-only edit.

Those events do not turn an unsuccessful compiler result into success.

Likewise, emitting a diagnostic does not by itself prove that a producer judgment is unsuccessful.

---

## 14. Contract Failure and Compiler Unsuccessful Result

### 14.1. ADR-0057 Remains Authoritative for Contract Failure

ADR-0057 owns user Contract Machine Failure.

This ADR does not add a fourth Contract Failure origin called `Compiler Failure`.

A compiler-owned HIR rejection, verifier rejection, unsupported compiler protocol, internal compiler invariant failure,
or
persistent-product validation result is not an ADR-0057 Failure merely because compilation cannot proceed.

### 14.2. Compiler Machinery May Carry Contract Failure Without Owning It

When compiler or backend machinery represents an already-established Contract Failure, it must preserve the exact
ADR-0057 meaning.

The compiler may expose that established Failure through an appropriate compiler observation surface.

The compiler may not replace it with a compiler-owned generic error taxonomy.

### 14.3. Authority-Owned Judgment Executed by Compiler Code

Kontrakt code may physically perform an authority-owned Establishment or other Contract judgment.

Physical execution does not transfer semantic ownership to the compiler.

If that authority establishes a Contract Failure, ADR-0057 governs the Failure.

If the compiler machinery itself cannot correctly perform the required compiler operation because of its own invalid
state, unsupported implementation capability, corrupted product, or internal invariant break, this ADR governs the
compiler-side result.

The two results must not be conflated.

---

## 15. Establishment Boundary

ADR-0063 defines Establishment as the boundary at which Contract material becomes authoritative.

A resolved candidate can therefore fail to become Established Material for more than one fundamentally different reason.

One possibility is an authority-owned unsuccessful judgment whose semantics belong to the owning Contract law and, where
applicable, ADR-0057.

Another possibility is a compiler-side failure to correctly provide or execute the machinery required for that judgment.

This ADR governs the second category and the compiler observation mechanics around both categories.

The compiler must not reinterpret:

```text
Candidate not established
```

as one universal compiler failure kind.

ADR-0063 Non-Establishment remains valid:

```text
not established
    ↛ automatic synthetic result
```

A specific owning law must establish the relevant result.

---

## 16. HIR Integration

### 16.1. HIR Seal Rejection Is a Compiler Result

ADR-0071 already states that HIR seal verification may reject incomplete, unresolved, recovery-tainted, structurally
inconsistent, or otherwise invalid material without running an owning Contract judgment.

Such rejection is compiler material, not Contract Failure meaning.

Under this ADR, an entered HIR seal judgment that rejects its candidate establishes an explicit HIR-owned compiler
result.

Its exact rejection vocabulary belongs to the HIR verifier law, not to this ADR.

### 16.2. Rejected Resolution Does Not Become Diagnostic Material Directly

The HIR frontend path must not model rejection as:

```text
resolution attempt
    ↓
rejected
    ↓
diagnostic material
```

The common direction is:

```text
resolution judgment
    ↓
explicit producer-owned unsuccessful compiler result
    ↓
legal Result Protocol observation
    ↓
diagnostic / recovery / availability consumer
```

### 16.3. Recovery Material Remains Outside Visible Resolved HIR

Parser recovery, unresolved placeholders, poisoned source nodes, speculative invalid candidates, and other recovery
material remain outside Visible Resolved HIR.

This ADR does not weaken ADR-0071's HIR visibility and recovery laws.

An unsuccessful HIR-related compiler result may exist even though no valid Visible HIR product exists for the rejected
candidate.

### 16.4. HIR Candidate Protocol and Compiler Result Protocol Are Different

```text
Resolved HIR Candidate Protocol
    = legal observation of resolved candidate meaning

Compiler Result Protocol
    = legal observation of compiler judgment result
```

The two protocols may share low-level implementation utilities.

They do not share semantic ownership merely because a single compiler subsystem produces both kinds of material.

---

## 17. Other IR and Compiler-Product Integration

Each IR or compiler product remains responsible for defining its own validity and unsuccessful result kinds.

Examples may include:

- Realization Body IR verification,
- Contract-Aware Execution IR verification,
- optimization legality judgments,
- JVM Plan / IR verification,
- classfile emission capability judgments,
- persistent-product decode and compatibility judgments.

The common law is:

```text
producer owns result meaning
    ↓
this ADR owns cross-cutting compiler-result law
    ↓
consumer observes through legal protocol
```

This ADR does not create one generic IR Failure node that every IR must embed.

---

## 18. Internal Compiler Failure, Trust, Crash, and Indeterminate State

### 18.1. User Rejection and Internal Compiler Failure Are Different

A user-source rejection can be a successful execution of the compiler judgment whose result is rejection.

An internal compiler failure means the compiler machinery itself violated or could not maintain a compiler requirement.

The two should not share one undifferentiated error category.

### 18.2. Trust Domain

An internal invariant failure may leave the affected compiler product or state untrustworthy.

Continuation therefore depends on more than graph independence.

A compiler task may continue only when the inputs it requires remain available and the relevant compiler domain remains
trustworthy enough for that task.

The exact trust-domain representation and invalidation law are intentionally open.

### 18.3. Minimal Safe Capture

After an internal compiler failure, the compiler must not assume that every nearby product is safe to traverse merely to
produce a rich diagnostic.

The compiler should preserve a minimal safe failure core when possible:

- detecting compiler boundary,
- failed compiler invariant when safely known,
- current subject when safely known,
- last trusted boundary when safely known.

Optional enrichment may include stack trace, IR dump, query trace, worker state, reproducer, or other evidence when that
evidence can be captured without trusting compromised material.

### 18.4. Crash Is Separate

A compiler process, worker, JVM, native component, or other critical machinery may disappear before an explicit compiler
result can be established.

The surviving event is Crash, not a fabricated final compiler unsuccessful result.

A later supervisor or restarted compiler run may reason from surviving durable material.

That later reasoning belongs to the later run.

### 18.5. Indeterminate Outcome

When the compiler can establish neither success nor an exact unsuccessful result, it must not invent certainty solely to
produce a binary result.

The exact common model for a compiler Indeterminate Outcome is intentionally open.

---

## 19. Concurrency and Determinism

### 19.1. Discovery Order Is Not Semantic Priority

Parallel compilation may discover several independent unsuccessful results in different physical orders.

Worker completion order, thread schedule, coroutine schedule, queue order, memory address, or hash iteration does not
make
one result semantically primary.

A renderer may choose a deterministic display order.

That display order does not become result priority.

### 19.2. Duplicate Discovery

The same producer result may be reached through multiple compiler analysis paths.

The compiler may deduplicate physically equivalent observations under producer-owned identity and occurrence law.

It must not collapse independent unsuccessful results merely because they share a diagnostic code, source line, text, or
similar presentation property.

### 19.3. Independent Results

A compiler boundary may contain more than one independent unsuccessful result.

This ADR does not require one root failure to replace that set.

The exact completion and membership representation remains open.

---

## 20. Incremental Reuse and Persistence

### 20.1. Cached Unsuccessful Result Is Not Permanent Truth

An unsuccessful compiler result may be reusable only while the complete producer-owned current-validity conditions for
that result remain valid.

A cached rejection does not become authoritative merely because the key still exists.

Compiler capability, protocol surface, relevant source meaning, exact references, producer law, compiler schema, backend
capability, or another declared validity input may change the result.

### 20.2. Producer-Owned Equality and Validity

The producer owns the semantic equality and reuse-validity law of its result product.

HID, fingerprint, Merkle root, epoch, schema revision, content address, cache key, or storage identity may accelerate
routing and validity checks.

They do not become compiler-result meaning or equality authority by themselves.

### 20.3. Transient Results

A producer may classify some compiler results as usable only within the current compiler generation or active
computation.

Such transient reuse does not grant cross-generation validity.

The exact V1/V2 persistence policy for unsuccessful results remains open.

### 20.4. Diagnostic Reuse Is Separate

A semantic compiler result may remain reusable while its source projection or diagnostic provenance becomes stale.

A stable source location does not keep a diagnostic valid after the underlying compiler result changed.

Compiler result validity and diagnostic validity must therefore remain separable.

### 20.5. Clean Recompute Reference

When reuse validity cannot be established, the compiler must be able to fall back to the required deterministic
revalidation or clean recomputation path.

Persistent state must not become the only source of correct compiler-result meaning.

---

## 21. Representation Freedom

This ADR does not choose:

- one result object hierarchy,
- one `Result<T, E>` API,
- one exception policy,
- one sealed class hierarchy,
- one table schema,
- one slab layout,
- one result tag encoding,
- one query engine,
- one scheduler,
- one persistence format,
- one evidence store,
- one recovery manager,
- one diagnostic manager,
- one protocol serialization format.

A valid realization may use primitive arrays, dense handles, immutable tables, typed slabs, generated accessors, inlined
primitive access, contiguous ranges, or another representation.

The physical form must preserve the legal observation and ownership laws in this ADR.

Distinct compiler meanings do not require distinct heap objects or repeated reference hops.

---

## 22. Rejected Directions

### 22.1. Exception or Stack as Result Meaning

Rejected:

```text
exception type
stack shape
handler placement
throw site

    → compiler failure meaning
```

These are implementation evidence.

### 22.2. Universal Compiler Failure Object

Rejected:

```text
CompilerFailure {
    kind
    subject
    source
    version
    cause
    recovery
    stack
    dependencyGraph
    diagnosticCode
    ...
}
```

Different producers own different result meanings.

### 22.3. Every Function Returns First-Class Compiler Result Material

Rejected:

```text
every helper
    → first-class compiler Result material
```

Implementation call boundaries are not automatically compiler-result boundaries.

### 22.4. Query Node as Result Authority

Rejected:

```text
query node identity
query dependency
query evaluation order

    → compiler-result meaning
```

### 22.5. Wrapper Failure Chain

Rejected:

```text
Resolution Rejection
    → HIR Failure("resolution failed")
    → Establishment Failure("HIR failed")
    → Driver Failure("compilation failed")
```

An unentered layer does not acquire a synthetic unsuccessful result.

### 22.6. Diagnostic as Compiler Truth

Rejected:

```text
diagnostic emitted
    → therefore compiler judgment failed
```

The compiler result precedes the diagnostic.

### 22.7. Recovery Rewrites Result

Rejected:

```text
R1 unsuccessful
    ↓
retry succeeds
    ↓
R1 becomes success / disappears
```

### 22.8. Mandatory Deep Root-Cause Search

Rejected as normal result-establishment law:

- whole dependency closure,
- globally minimal conflict set,
- full causal slicing,
- full stack traversal,
- complete query history,
- transitive root-cause ranking.

These may exist as bounded or on-demand explanation and forensic products.

### 22.9. Recovery Placeholder as Valid HIR Meaning

Rejected:

```text
poison / recovery placeholder
    → Visible Resolved HIR semantic meaning
```

### 22.10. First Observed Failure as Semantic Root

Rejected:

```text
first worker to report
first exception to reach driver
first hash iteration result

    → primary compiler failure
```

### 22.11. Cached Negative Result as Permanent Truth

Rejected:

```text
old rejection cached
    → current rejection without current-validity law
```

---

## 23. V1 Requirements

V1 must establish the following seams directly.

A compiler judgment or product boundary that owns an independently observable result can expose that result explicitly.

An unsuccessful result is owned by the first exact owning judgment, not by a later stack or diagnostic reconstruction.

Speculative local failures do not become established compiler results until the owning judgment commits them.

An unentered downstream judgment has no synthetic unsuccessful result.

A legal consumer can observe an unsuccessful compiler result through a Result Protocol rather than producer internals.

The Protocol exposes exact producer-owned meaning without requiring recursive semantic reconstruction.

Mandatory Direct Basis is deterministic, producer-owned, bounded, and obtainable without an unbounded secondary search.

Rich explanation and engineering evidence remain separate from the core result.

Recovery does not rewrite the earlier result.

Compiler Diagnostic is downstream of compiler result establishment.

Contract Failure remains owned by ADR-0057.

User-source rejection remains distinguishable from internal compiler invariant failure.

Crash remains distinguishable from an established compiler unsuccessful result.

Worker order and discovery order do not decide result meaning or semantic priority.

Negative and unsuccessful result reuse is subject to current producer-owned validity.

Clean deterministic revalidation or recomputation remains available when reuse cannot be validated.

---

## 24. V2 Evolution Seam

V2 may add finer-grained persistent result products, cross-session reuse, dependency-local invalidation, distributed
caches,
lazy evidence materialization, richer recovery, compiler-step provenance, automated reproducer reduction, and deeper
root-cause analysis.

Those techniques remain compiler realization.

V2 may persist unsuccessful-result material only when the persisted product carries enough current-validity information
to
prevent stale negative results from becoming compiler truth.

V2 may add compact dependency or provenance summaries without making those summaries the meaning owner.

V2 may perform selective explanation slicing, conflict reduction, counterexample aggregation, or forensic analysis on
demand.

V2 must not make correctness depend on a persistent graph that clean deterministic computation cannot replace.

V2 may use a different scheduling or repair engine from V1 without redefining the result laws in this ADR.

---

## 25. Intentionally Open

This ADR intentionally leaves the following decisions open for the next review batches.

### 25.1. Availability Effect Representation

The exact logical representation of:

```text
Product X unavailable because Result R
```

is not fixed.

The design must avoid copied failure meaning and avoid forcing every dependent producer to create another unsuccessful
result.

### 25.2. Boundary Completion

The exact representation of a compiler boundary that contains:

- successful independent products,
- one or more unsuccessful results,
- unavailable requested products,
- recovery eligibility,
- final request viability,

is not fixed.

### 25.3. Multiple Failure Membership

The ADR does not yet decide how one requested product records several independent unsuccessful results that jointly or
independently prevent availability.

The final design must not create discovery-order priority.

### 25.4. Trust-Domain Representation

The exact way an internal compiler invariant failure invalidates a compiler product, region, worker, generation, or
other
trust scope remains open.

### 25.5. Recovery Decision Material

The ADR does not yet decide whether all recovery choices share one explicit common Recovery Decision material or whether
producer-local recovery protocols are sufficient.

### 25.6. Compiler Evidence Protocol

The exact identity, retention, persistence, and legal observation surface of deep compiler evidence remain open.

The result law already requires that result meaning not depend on that future evidence protocol.

### 25.7. Result Protocol Evolution

The ADR does not yet choose a common protocol revision number, capability bitmap, extension registry, wire format, or
persistent encoding.

Common Result Protocol evolution, producer-specific result-surface evolution, compiler generation, and
persistent-product
format must remain separable.

### 25.8. Compiler Indeterminate Outcome

The complete model of an outcome whose success or exact unsuccessful meaning cannot be established remains open.

### 25.9. Positive Success Establishment

This draft requires producer-owned success law but does not yet define a compiler-wide rule for when absence of an error
is
insufficient and positive success evidence must be explicit.

This issue is related to silent-failure resistance and should be decided separately.

---

## 26. Verification and Conformance

The compiler must test the result architecture independently of diagnostic presentation.

At minimum, verification should cover the following equivalences.

```text
stack capture on
    vs
stack capture off
    → same compiler result meaning
```

```text
short diagnostic depth
    vs
deep diagnostic depth
    → same compiler result meaning
```

```text
single-thread execution
    vs
parallel execution
    → same complete producer result set
```

```text
clean computation
    vs
valid incremental reuse
    → same compiler result meaning
```

```text
cache hit
    vs
cache miss + deterministic recomputation
    → same compiler result meaning
```

```text
recovery enabled
    vs
reference path before recovery
    → prior unsuccessful result remains unchanged
```

```text
source-only provenance edit
    → semantic result may remain reusable
    → diagnostic projection may require refresh
```

```text
producer validity changed
    → stale cached unsuccessful result rejected
```

Tests must also verify that an unreached downstream compiler judgment does not receive a synthetic unsuccessful result.

Tests must verify that independent unsuccessful results are not collapsed because of common text, source line,
diagnostic
code, or physical discovery order.

Internal-failure tests must verify that richer forensic capture can be disabled without changing the minimal established
compiler result when that result can still be established safely.

Malformed-input and fuzz tests should measure failure amplification, Direct Basis size, evidence growth, recovery depth,
and diagnostic work separately from semantic compiler work.

---

## 27. Consequences

Compiler failure becomes explicit at the judgment that owns the requirement instead of being reconstructed from
implementation history.

Normal user diagnostics no longer need a stack trace to determine which compiler requirement failed.

Diagnostics can become richer without becoming compiler-result authority.

Recovery can evolve independently because it consumes an already-explicit result instead of redefining the original
failure.

HIR can reject invalid material without inserting poison into Visible HIR and without treating the diagnostic as the
rejection itself.

Establishment remains authority-owned. Compiler machinery surrounding Establishment gains explicit result handling
without
absorbing Contract authority.

V2 can persist and reuse negative results only under explicit current-validity law.

The compiler pays additional bookkeeping cost for explicit result occurrences, Direct Basis, and legal observation.

That cost is constrained by result-boundary selection and bounded Direct Basis rather than applying to every helper
call.

The architecture requires later work for availability, completion, trust invalidation, recovery material, and deep
evidence
protocols.

Those open decisions are intentional. This ADR first establishes the common result law so HIR and other compiler
products
can close their own boundaries without inventing incompatible local failure systems.

---

## 28. Immediate ADR-0071 Consequences

ADR-0071 can now close its HIR-specific failure boundary against this common draft.

The required HIR changes are limited in scope.

First, HIR seal verification rejection becomes an explicit HIR-owned compiler result rather than merely `compiler
correctness or frontend validity material`.

Second, the current Section 6 flow:

```text
resolved  rejected
   ↓         ↓
 HIR     diagnostic material
```

must become logically:

```text
resolved  rejected
   ↓         ↓
 HIR     explicit producer-owned compiler result
                ↓
         Result Protocol observation
                ↓
        diagnostic / recovery / availability
```

Third, HIR diagnostics remain consumers of HIR subjects, source provenance, and explicit compiler results. They do not
own
HIR rejection meaning.

Fourth, an HIR-related failure that prevents HIR formation does not create a synthetic later HIR or Establishment
failure
for work that was never entered.

Fifth, parser recovery, poison, unresolved placeholders, and incomplete material remain outside Visible HIR.

Sixth, the HIR Candidate Protocol remains separate from the Compiler Result Protocol.

These changes do not require ADR-0071 to define the complete compiler availability/completion architecture. Those common
questions remain open in this ADR and can be resolved before the final compiler-result ADR is Accepted.

---

## 29. Non-Normative Research Basis

The decisions above are Kontrakt architecture decisions. External systems and papers are evidence, not authority.

Current production references examined for this draft include:

- LLVM/MLIR pass failure and verifier behavior, especially the distinction between a signaled pass failure and
  potentially
  invalid IR state,
- rustc structured diagnostics and `-Z track-diagnostics`, which separates diagnostic creation location from requiring a
  full stack trace,
- Bazel Skyframe/StateMachine error bubbling, which demonstrates that orchestration consequences may need to reach a
  scheduler even when semantic failure meaning should remain local,
- Buck2/DICE producer-owned equality and `validity`, including transient values that may be shared within one
  computation
  but not reused across later computations,
- Clang recovery material, which preserves tooling continuity without making recovery placeholders ordinary valid
  semantic
  material.

Recent research examined for this draft includes:

- *Isolating Compiler Bugs through Compilation Steps Analysis* (CompSCAN, 2025), which treats compilation-step sequences
  as
  useful forensic evidence rather than requiring stack history to define compiler failure,
- *Using a Sledgehammer to Crack a Nut? Revisiting Automated Compiler Fault Isolation* (2025), which shows that more
  complex
  global fault-localization machinery is not automatically superior to simpler baselines,
- *Error Localization, Certificates, and Hints for Probabilistic Program Verification via Slicing* (2025), which
  separates
  error-reporting slices, proof-simplification slices, and success-preserving slices and makes slice minimization a
  separate
  analysis,
- *Improving Debugging in Verification-Aware Languages Through Automated Fault Localization: A Case Study in Dafny*
  (2026), which distinguishes an exact verifier failure from later structured root-cause localization over one or
  several
  traces.

These references support the separation between exact result establishment, availability/control propagation, recovery,
diagnostic explanation, and engineering forensics. They do not determine Kontrakt semantics.

---

## 30. Draft Law Summary

The common compiler law is:

```text
owned compiler requirement
    ↓
first exact owning compiler judgment
    ↓
explicit compiler result
    ↓
legal Result Protocol observation
```

For an unsuccessful result:

```text
exact producer-owned unsuccessful meaning
    +
bounded Direct Basis
    +
shallow direct provenance
```

must be sufficient to identify and interpret the result without reconstructing it from implementation history.

The result remains source-local.

```text
unsuccessful result
    ↛ wrapper failure chain
```

Dependent compiler work receives availability or completion consequences instead of copied failure meaning.

An unentered compiler judgment has no synthetic unsuccessful result.

Diagnostics explain compiler results.

Recovery reacts to compiler results.

Evidence investigates compiler results.

Caching may reuse compiler results only under current producer-owned validity.

Crash and indeterminate state remain separate from an established unsuccessful result.

Contract Failure remains governed by ADR-0057 and the Contract authority that owns the failed requirement.

Physical representation remains replaceable.