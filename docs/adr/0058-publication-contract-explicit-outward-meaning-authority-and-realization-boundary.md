# ADR-0058: Publication Contract, Explicit Public Claim Authority, and Realization Boundary

## Status

Proposed

## Date

2026-08-17

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0057: Failure Contract, Explicit Machine Failure, Attribution, and Realization Boundary
- ADR-0056: Governance Contract, Policy-World Control, and Selection Boundary
- ADR-0055: Whole Machine, Pipeline Composition, and Contract Concurrency
- ADR-0054: Policy Contract, Explicit Operating Modes, Self-Contained Contract Worlds, and Interface Binding Boundary
- ADR-0053: Version Contract, Sovereign Contract Revision History, and Realization Boundary
- ADR-0052: Capacity Contract, Explicit Safe Operating Memory Limits, and Realization Boundary
- ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror

---

## 1. Context

A Contract Machine must distinguish what is authoritative inside its Core from what it is allowed to claim publicly.
Accepted material is not automatically public material.

The inbound side already follows the same discipline. External material does not gain Contract authority merely because
an adapter supplied it. It must enter through an explicit Input boundary and become authoritative only through the
Contract processing that owns its meaning.

The outward side needs a corresponding boundary.

```text
outside material
    -> adapter / realization
    -> Input Contract
    -> Contract Machine

Contract Machine
    -> Publication
    -> Public Claim Set
    -> Output Presentation
    -> adapter / realization
    -> outside world
```

The adapter is not Contract Authority on either side. An inbound adapter cannot decide what external material means to
the Core, and an outbound adapter cannot decide what Core material may become a public claim.

`What Contract Is` introduced Publication for this reason: the machine may know more than it is allowed to say. ADR-0049
then separated accepted Fact authority from Publication and Output Presentation. A successfully completed Operation
establishes its declared result as an immutable Fact inside the Core, while Publication controls which public claim may
be formed from that accepted material.

Later Contract work expanded the machine beyond successful Operation result Facts. Failure is now explicit Contract
meaning, and some Failures must remain internal while others may support a public failure claim. This does not give
Publication authority to rewrite Failure or to become another Core judgment system. It extends the set of established
Contract material from which an explicitly declared public claim may be formed.

Experience from software systems, information-release models, build and language surfaces, and engineering release
practice supports the same separation when the roles are kept distinct. Internal determination belongs to the authority
that owns that determination. Publication controls the outward claim surface after the relevant material is already
authoritative. Physical delivery remains a later realization concern.

Publication therefore sits between Core authority and external presentation. It may expose nothing, expose only selected
material, or form a reduced or outward-specific claim from explicitly declared Core material. The result is a closed
public-claim surface. Output Presentation consumes that surface without receiving authority to inspect the Core or the
Publication sources behind it.

ADR-0058 refines Publication around that boundary.

---

## 2. Problem

Without a separate Publication Contract, internal material can become public merely because implementation makes it easy
to expose.

A host-language return value may be serializable, a Failure carrier may be reachable, a logger may be able to print an
internal value, or an adapter may be able to send it. None of those implementation facts grants public Contract
authority.

```text
implementation can expose X
    -> X becomes public
```

must instead be:

```text
Core establishes authoritative Contract material
        ↓
Publication forms the authorized public claim surface
        ↓
Output Presentation selects only from that surface
        ↓
replaceable realization carries the closed presentation outside
```

The earlier Publication model was too closely coupled to direct coordinate transfer from one Operation result Fact into
one selected Output Presentation. That coupling creates two problems. It makes Publication aware of presentation
structure, and it gives Output Presentation a path back toward Core coordinates.

The opposite expansion is also wrong. Publication must not become a second decision system that re-establishes Core
truth, redoes Invariant or State-Machine judgment, or synthesizes new internal meaning from material that another
authority already owns.

Publication needs a narrower authority. It must explicitly control which public claims may be formed from already-
established Contract material, how much of that material may appear in those claims, and which outward-only
transformation is part of forming the claim. Anything outside that public-claim surface remains unavailable to Output
Presentation.

The Publication law must also be coherent before execution. Runtime order, backend preference, or external consumer
choice cannot repair contradictory public claims declared by the Contract.

Kontrakt therefore needs an explicit public-claim boundary that preserves Core authority, closes outward disclosure,
keeps Output Presentation independent from Core structure, and leaves physical realization replaceable.

---

## 3. Decision Drivers

Publication must remain Contract meaning rather than implementation behavior. The ability to read, return, serialize,
log, or transmit material grants no publication authority.

The Core is sealed from public-claim visibility by default. Internal authority and public authority are different
surfaces.

Publication dependencies must be explicit. Material that is not declared as a Publication source cannot influence a
public claim merely because implementation can reach it.

Participation as Publication source does not itself grant disclosure. A source may be used to form a public claim while
some or all of its internal detail remains sealed.

Publication may form an outward-specific claim from established material, but that claim must remain public-only. It
cannot become a new Core Fact, Failure, Invariant result, State, Transition meaning, or hidden input to another Core
judgment.

The public-claim surface must be closed before Output Presentation begins. Publication must not depend on a particular
Output Presentation, and Output Presentation must not see the Core, Publication sources, or source-to-claim relations.

One declared Publication relation establishes one public claim. If several compatible declared relations apply, each
claim is required. Publication does not choose one by runtime priority or declaration order.

Governance selects and binds the Policy World, and Policy defines the Contract composition of that World. Publication
performs only the public-claim judgment already bound in that World.

Failure remains internal Failure meaning even when it supports a public failure claim. Diagnostic Evidence remains
explanation rather than public Contract authority.

Determinism remains mandatory. Equivalent authoritative source material under the same bound Contract must produce the
same public claims across valid compiler and backend executions.

The semantic model must remain independent of frontend syntax, runtime carriers, output DTOs, adapters, transports, and
backend IR.

---

## 4. Contract Decision

### 4.1. Publication Is Public Claim Authority

Publication is the Contract authority that decides which public claim may be formed from explicitly declared,
already-established Contract material.

```text
established Contract material
        ↓
Publication judgment
        ↓
authorized public claim
```

The important result is the `public claim`. Publication does not make the source material more true, convert it into a
new Core Fact, or transfer the source authority outside the Core. It establishes only what the machine is contractually
allowed to claim on its public surface.

A public claim may carry less detail than the Core material from which it was formed and may use outward-specific
material that the Core does not treat as its own Fact surface. That difference does not change the source meaning.

### 4.2. The Core Is Sealed from Public-Claim Visibility by Default

No Core material becomes public merely by existing, being returned, or being reachable by implementation.

Only material that Publication explicitly admits into a public claim receives public Contract authority. Everything else
remains outside the public-claim surface without requiring a negative list of hidden fields or meanings.

This sealing concerns public Contract visibility. It does not prevent separate Diagnostic processing from observing
material under its own Contract authority.

### 4.3. Publication Sources Are Explicit and Do Not Grant Disclosure

Publication may use only Contract material explicitly declared as a source of its judgment.

A successfully completed Operation result is already an immutable Fact inside the Core. Publication may use only the
result material named by its Contract. Failure or other established Contract material may participate only where that
participation is explicitly declared.

Source participation and public disclosure are separate authorities. Publication may depend on internal material without
exposing that material in the resulting public claim. Undeclared Core material cannot affect the judgment, and declared
source material that is not admitted into the claim remains sealed from public Contract visibility.

Publication may not scan the Core, follow undeclared relations, or acquire additional source material through backend
reachability.

### 4.4. Publication Forms Claims Without Re-establishing Core Meaning

Publication is not restricted to copying Core material unchanged. A public claim may omit internal detail, reduce
precision, rename public meaning, or otherwise transform explicitly declared source material when that transformation
exists only to form the outward claim.

A public claim may depend on several explicitly declared source items when its formation requires them together. Their
participation in one claim does not merge, replace, or rewrite the source meanings.

That authority ends at the public-claim surface. Publication cannot use the same transformation to establish a new Core
Fact, revise Failure, redo Admission or Invariant judgment, decide State or Transition meaning, or supply hidden input
to another Core authority.

A public claim therefore has no direct re-entry path into Core or State-Machine judgment. If material later returns from
the outside world, it is external material again and must enter through the ordinary inbound Contract boundary.

Publication formation is declarative Contract law. Arbitrary callbacks, external lookup, hidden implementation state, or
host-language control flow cannot become Publication authority.

### 4.5. Publication Produces a Closed Public Claim Set

Publication closes the public Contract surface before Output Presentation begins.

The `Public Claim Set` is the closed set of public claims and public claim material available to the following outward
presentation contract. It is a semantic Contract concept, not a required runtime collection, carrier, or generated type.

Publication knows the established sources from which its claims are formed. It does not know which Output Presentation
will represent those claims, which external fields that presentation will use, or which transport will carry them.

The source-to-claim relation therefore ends at the Public Claim Set.

### 4.6. Output Presentation May See Only the Public Claim Set

Output Presentation consumes the closed Public Claim Set. It has no independent authority to inspect Core Facts,
Failure, Publication sources, or the relations by which claims were formed.

```text
Core material
    ↓
Publication
    ↓
closed Public Claim Set

---------------- public-claim boundary ----------------

closed Public Claim Set
    ↓
Output Presentation
    ↓
closed external representation
```

Output Presentation may select only from the public claims and material available on that surface. It cannot recover,
reconstruct, or directly reference sealed Core material merely because that material existed before Publication.

Publication and Output Presentation therefore depend in one direction only. Publication does not target Output fields,
and Output Presentation does not reach backward through Publication into Core structure.

### 4.7. One Publication Relation Establishes One Public Claim

One declared Publication relation establishes exactly one public claim when its declared condition is satisfied.

When several compatible relations are satisfied, every such relation establishes its own claim. The user declared all of
those relations, so Publication has no authority to select a winner, apply first-match behavior, or suppress one by
runtime order.

An applicable relation is an obligation, not a runtime permission. Once its declared condition is satisfied, the public
claim required by that relation must be established.

A Publication Contract that can require incompatible public claims under the same declared conditions is invalid and
must be rejected by compilation or verification rather than repaired at runtime.

### 4.8. Publication Uses Already-Established Source Material

Publication may use only declared source material that has already been established by the authority that owns that
meaning. If the source is Failure, it must already be established under ADR-0057.

Material that has not become authoritative Contract meaning is not a Publication source. Publication cannot create
missing source material or replace it with another Core result.

This ADR does not define the exact processing schedule around Publication. It requires only that the source of a
Publication judgment already exists before that judgment uses it.

### 4.9. Established Public Claims Are Non-Retroactive

Once Publication establishes a public claim, later changes to Contract material do not rewrite that claim.

Later processing may establish a different claim under different authoritative material or a different bound Contract.
The earlier claim remains what the earlier Publication judgment established.

### 4.10. Publication Does Not Create a Recipient, Channel, or Scope Ontology

Publication does not choose who the consumer is, invent an outward-channel identity, or select which Policy World should
apply.

Governance selects and binds the Policy World. Policy defines the Contract composition of that World, including its
Publication law. Existing interface and machine bindings determine where that law participates.

Publication therefore judges only under the already-bound Contract. Recipient, adapter, network route, or runtime
topology cannot become hidden Publication selectors.

### 4.11. Success and Failure Use the Same Publication Authority

Successful Contract material and Failure may both support public claims when the bound Publication Contract explicitly
allows them to participate.

```text
successful established material
        ↓
Publication
        ↓
public success claim

Failure
        ↓
Publication
        ↓
public failure claim
```

Publication does not rewrite the internal Failure or create an aggregate Failure. The Failure remains the exact internal
failure meaning established under ADR-0057; Publication controls only what public claim may be formed from it.

A host return path and an exception path therefore cannot create separate implicit publication channels.

### 4.12. Publication and Diagnostic Meaning Are Separate

Diagnostic Evidence explains machine processing. Its existence does not make it a public Contract claim.

An established Failure can support Diagnostic processing without Publication. If diagnostic material is ever to become
part of a public Contract claim, that public use requires explicit Publication authority rather than leakage from logs,
traces, or retained evidence.

The later Diagnostic Evidence / Retention ADR decides what evidence exists and what may survive. Publication remains the
authority over public claims.

### 4.13. Publication Does Not Own External Effects

Publication ends before external implementation acquires physical control over the outside world.

```text
Contract Machine
    -> Publication
    -> Public Claim Set
    -> Output Presentation
    -> adapter / realization
    -> external system
```

An external side effect is not Publication Contract meaning. The receiving system decides what action to take under its
own authority without changing the public claim Kontrakt established.

An external acknowledgement or physical side effect cannot retroactively establish, cancel, or rewrite Publication. If
external material later matters to the Contract Machine, it must enter again through an explicit inbound Contract
boundary.

### 4.14. Inbound and Outbound Authority Remain Separate

External representation has no inward Contract authority merely because it arrives, and Core material has no public
Contract authority merely because it can be emitted.

Input governs how outside material begins declared inward processing. Publication governs which public claim can be
formed from already-established internal material. Adapters remain replaceable realization around both boundaries.

### 4.15. Publication Judges Within the Bound Policy World

Within the already-bound Policy World, Publication applies the Publication law included by that World's Contract
composition.

It does not replace that binding or infer consumer identity from runtime context. Its authority begins only after the
relevant Governance and Policy decisions are already fixed.

### 4.16. Unsuccessful Required Publication Is Failure

Publication does not define a separate denial, stop, partial-publication, or conflict result family. The absence of a
satisfied Publication relation is not by itself Failure.

When a declared Publication relation is satisfied, the public claim required by that relation must be established. If
the owning Publication judgment determines that this required claim cannot be established, ADR-0057 governs the
resulting Contract Failure.

Failure of later physical realization remains separate. If an already-established public claim cannot be realized and
Kontrakt can establish that required realization did not complete, ADR-0057 governs that Realization Failure rather than
rewriting Publication.

---

## 5. Authored and Canonical Publication Material

### 5.1. Publication Is Explicitly Authored Contract Meaning

Publication cannot be inferred from Core truth alone. The existence of an immutable Result Fact, Failure, or other
Contract material does not determine what the machine may claim publicly.

Every Publication declaration must therefore identify the exact source material on which it depends and the public claim
relation it establishes. If only part of declared source material may appear in the claim, that outward selection must
be explicit rather than inferred from representation convenience.

Publication authoring must not name Output Presentation fields or transport structure. The frontend needs to express the
public-claim law while keeping the later presentation contract outside Publication authority.

The final user API and IDL syntax remain open until the semantic model is complete.

### 5.2. Canonical Publication Material

Any canonical form of Publication must preserve every semantic distinction needed to reproduce the declared public claim
judgment.

At minimum, it must preserve:

```text
exact Publication declaration
exact declared source material
exact declared source coordinates where applicable
exact declarative claim-formation law
exact public claim produced by each relation
exact public material admitted into that claim
```

One relation must remain distinguishable from every other relation even when several relations share the same source or
condition. Canonicalization must not introduce runtime priority or merge distinct public claims merely because one
backend can compute them together.

The canonical form does not include Output Presentation structure as Publication authority. It preserves the closed
public-claim surface that Output Presentation may consume, not the representation chosen after that surface.

Publication does not own a universal `applicable context` object. Governance, Policy, Version, State, Failure, and other
authorities retain their own meaning; Publication preserves only dependencies declared by its own Contract.

### 5.3. Backend Vocabulary Is Not Publication Authority

No host-language, carrier, serializer, adapter, or backend vocabulary defines Publication meaning.

A backend may carry stable references to canonical Publication material, but replacing the implementation must not
change the Contract while the same public-claim law remains valid.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Meaning and Representation

Fact objects, Failure carriers, canonical tables, runtime records, generated classes, HIDs, and backend handles may
represent material used by Publication. None of those representations grants publication authority.

The Contract owns the declared source, claim-formation law, and closed Public Claim Set. A backend may change how those
semantics are represented as long as the same public claims remain recoverable.

### 6.2. Publication Realization

A backend realizes an already-declared Publication law.

It may lower source access and claim formation into any deterministic mechanism that preserves the same Contract. It may
fuse checks, precompute mappings, specialize value transformation, share evaluation among relations, or eliminate a
runtime Publication object entirely.

Those implementation choices cannot widen the declared sources, create additional public claims, suppress required
claims, or expose source material that Publication did not admit into the Public Claim Set.

Publication is not modeled as a host-language wrapper with pre/post checks, proxy interception, or arbitrary user
callbacks. External lookup and undeclared machine state cannot become hidden public-claim authority.

### 6.3. Handoff to Output Presentation

Publication and Output Presentation may be physically fused by an optimized backend, but their Contract authorities
remain distinct.

The semantic handoff is only the closed Public Claim Set. Output Presentation realization may consume that surface; it
must not receive a back-reference to Publication sources as additional Contract authority.

```text
semantic Publication
        -> closed Public Claim Set

semantic Output Presentation
        -> closed external representation

optimized backend
        -> may realize both in one physical path
```

Fusion is valid only when the Publication boundary remains recoverable and the resulting external representation uses no
material outside the public-claim surface.

### 6.4. Adapters and External Systems

Adapters stand outside the Contract authority defined here.

An adapter may carry a closed Output Presentation through the mechanism expected by an external system. It may also
translate later external material toward a new Input boundary. Those translations are implementation.

The adapter cannot widen the Public Claim Set, recover sealed Core material, or treat an external acknowledgement as a
change to Publication. Realization Failure and Crash remain governed by ADR-0057 where their conditions are established.

### 6.5. Backend Architecture Constraints

This ADR does not choose the concrete Publication IR, Public Claim Set representation, or runtime carrier.

Any backend design must preserve the semantic distinctions established here and the exact dependencies needed to
reproduce them deterministically.

Caching is reuse rather than authority. A cached Publication compiler product may be reused only while the canonical
material on which its claim judgment depends remains valid.

V1 may realize Publication through ordinary generated JVM boundaries. Later backends may specialize or incrementally
reuse those results, but the Publication Contract must remain unchanged across backend choices.

---

## 7. Verification, Determinism, and Incremental Extensibility

The compiler or verifier must resolve every source and source coordinate named by Publication to exact declared Contract
material. Implementation-only values cannot substitute for a missing source, and undeclared Core material cannot become
relevant because a backend can reach it.

The verifier must reject any Publication declaration that attempts to establish new Core or State-Machine authority
instead of a public claim. A public claim cannot directly participate as input to Fact establishment, Admission,
Canonicalization, Lowering, Invariant, State, Transition, or another Core judgment. Later inward use must begin again as
external material through an explicit Input boundary.

Each declared Publication relation must remain one relation to one public claim. When several compatible relations are
satisfied, every required claim must be preserved. Declaration order, first-match behavior, execution priority, worker
arrival, hash iteration, or adapter preference have no Contract authority.

The compiler must reject a Publication Contract whose declared relations can require incompatible claims under the same
valid declared conditions. Publication does not collect such claims into a runtime conflict state and does not ask an
external consumer to choose which claim should count.

How a compiler proves contradiction or validates the eventual claim-formation language is implementation. The Contract
law is that malformed public authority is rejected before it becomes machine behavior.

Output Presentation must not acquire a path to Core material through Publication metadata. The following Output
Presentation ADR will define the complete authoring and closure rules, but any realization that lets Output bypass the
Public Claim Set already violates this Publication boundary.

Once the same declared source material and Publication relations apply, every valid compiler and backend execution must
produce the same public claims.

```text
clean full compilation
incremental compilation
cache reuse
recomputation
single-threaded execution
parallel execution
```

remain semantically equivalent for Publication.

Future incremental invalidation follows exact declared dependencies. Adding unrelated Core material cannot change an
existing Publication judgment until the Publication Contract itself acquires a dependency on that material.

Persisted Publication IR, generated code, caches, indexes, and verification products remain implementation material.
Their storage or cache versions are separate from Contract Version.

Malformed Publication declarations are compile-time invalidity. Runtime Failure remains reserved for unsuccessful
machine judgments and realizations established during actual Contract Machine processing under ADR-0057.

---

## 8. Deferred Decisions

The final Publication authoring syntax remains open. User API and IDL work must express declared sources,
claim-formation relations, and public claim material without exposing backend structure or binding Publication directly
to Output fields.

The exact closed expression language allowed for outward-only claim transformation remains open. The language must be
rich enough to support deliberate reduction and external shaping without becoming a general-purpose Core judgment or
host-language execution surface.

Whether every bound Publication context must always establish at least one public claim remains open. This ADR decides
that every relation whose declared condition is satisfied is mandatory, but it does not yet require all possible source
material to satisfy some Publication relation.

Output Presentation owns the next boundary. The following ADR must define how it selects from the closed Public Claim
Set, how several claims may share one external representation, and how the presentation remains closed without gaining
visibility into Core or Publication sources.

The exact scheduling relation between Publication and active processing boundary completion remains Whole-Machine work.
This ADR requires only that Publication never use source material before its owning authority has established it.

Diagnostic Evidence / Retention may later define how diagnostic material is retained and observed. That work must
preserve the rule that diagnostic availability does not itself create a public claim.

Once the frontend and canonical representation are designed, ADR-0049 and older documents must be revised where they
still couple Publication directly to Output Presentation coordinates, restrict Publication to the older successful-
result-only path, or introduce Publication-specific stop and realization assumptions superseded here.

These deferred decisions do not reopen the separation between Core authority, Publication, Public Claim Set, Output
Presentation, and external realization.

---

## 9. Consequences

### Positive

Publication returns to its original role as explicit public-claim authority. Core truth remains distinct from what an
external contract may rely on.

Sealed-by-default public visibility lets Publication expose nothing, expose only selected material, or form a smaller
outward-specific claim without allowing Output Presentation to inspect the internal source.

The Public Claim Set creates a closed semantic boundary between Publication and Output Presentation. Output can be
designed and optimized without gaining Core authority, while Publication can evolve without knowing external field or
transport structure.

Failure remains exact internal Failure meaning. Public failure claims may differ in detail without rewriting Failure or
using Diagnostic Evidence as a publication shortcut.

Multiple compatible user-declared Publication relations remain explicit and deterministic. Contradictory public
requirements are rejected before execution rather than resolved by runtime priority.

### Negative

The Publication declaration language must support outward claim formation without becoming a second Core rule language.
Its permitted transformation surface therefore needs careful closure and verification.

The compiler must preserve a hard visibility boundary between Core sources, Public Claim Set, and Output Presentation.
This requires stricter dependency resolution than a direct serializer or source-to-output mapping model.

ADR-0049 is now too coupled to direct Operation-result-to-Output relations and retained Publication realization details.
Those parts require revision after ADR-0058 is accepted.

### Neutral

Publication remains user-authored because public authority cannot be inferred from Core truth alone. Public Claim Set is
semantic Contract material, not a required runtime object or carrier. Output Presentation and adapters remain separate,
replaceable authorities and realizations after Publication.