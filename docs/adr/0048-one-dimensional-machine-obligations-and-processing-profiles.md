# ADR-0048: One-Dimensional Machine Obligations and Processing Profiles

## Status

Draft

## Date

2026-07-09

## Related

- `docs/what-contract-is.md`
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Generated Host Interface Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure

---

## 1. Context

ADR-0046 fixes the first frontend boundary.

The interface remains the user-facing contract surface. The method remains the operation handle. A generated host
interface may make that handle pleasant to call, but it is still machinery. It does not own the contract.

ADR-0047 fixes the selection boundary after that.

Once an operation exists, Kontrakt must not ask the user's material to mark itself as a Kontrakt contract. The role does
not come from the carrier. The role comes from the operation slot. The operation gives the boundary. The slot gives the
role.

That still leaves one question.

After a slot selects a role, Kontrakt must know how to process the material that fills that role. `Input` cannot be
processed like `Invariant`. `State` cannot be processed like `Publication`. `Budget` cannot be processed like
`Diagnostic Evidence`. Their names alone do not tell the machine what to acquire, what to lower, or where implementation
must stop speaking.

This ADR defines those processing profiles for the remaining eighteen one-dimensional presentations.

It does not choose the final authoring syntax or host API shape. It decides the boundary of meaning: what kind of
material a role may receive, what Kontrakt must lower into its own canonical material, and which part is only backend
realization.

---

## 2. Problem

The remaining one-dimensional presentations are all contract presentations, but they do not have the same physical
character.

Some describe the shape of material at a boundary. Some produce a judgment. Some form stable meaning. Some govern
movement. Some explain what happened. Some declare the limits and the valid contract world under which the machine may
operate.

If Kontrakt processes all of them as ordinary host code, implementation regains authority. If it treats them as backend
features, the contract becomes a feature catalog. If it forces every small body into one new language surface, the
surface becomes heavier than the obligation.

The machine needs role-specific processing without letting role-specific processing become implementation authority.

---

## 3. Decision Drivers

A processing profile must start from the machine obligation.

The source may be carried by software, but the contract meaning must become Kontrakt-owned material before backend
machinery acts on it. Data-like roles should stay data-like. Judgment roles may need richer expression, but opaque host
behavior must not become the source of truth.

Lowering and invariant must stay apart. Lowering forms a candidate. Invariant decides whether the machine may believe
what the candidate claims.

The cross-cutting roles must stay on their own surfaces. They touch the same machine, but they do not answer the same
question.

Backend machinery may change whenever the canonical material does not change.

---

## 4. Decision

Kontrakt will define a processing profile for each remaining one-dimensional presentation.

The Interface Surface Contract is already decided by ADR-0046. This ADR covers the other eighteen presentations.

A processing profile decides the nature of the role, the character of the source material it may receive, the canonical
material Kontrakt must own after lowering, and the line after which backend machinery is only realization.

The common authority path is:

```text
slot-selected source
-> acquisition under the slot role
-> lowering into Kontrakt-owned material
-> backend realization behind that material
```

This is not a runtime schedule. It is the path by which meaning stops belonging to the carrier and starts belonging to
Kontrakt.

---

## 5. Presentation Profiles

### 5.1. Input Contract

Input is controlled entry through a slot-bound JVM declaration source.

A Kotlin or Java declaration is not an input contract by itself. Binding it to the `input` slot only selects a source
candidate. Kontrakt must ratify that candidate under the input source profile before any input material exists.

For V1, the accepted input source profile is deliberately narrow: Kotlin data classes, Kotlin final classes whose input
parts are primary-constructor `val` properties, and Java records. The source of an input part is the closed declaration
surface: Kotlin primary-constructor `val` properties or Java record components. Their declared order becomes the input
part order.

Kontrakt does not construct input. A primary constructor or record constructor is declaration coordinate, not runtime
authority. Constructor execution, `init` blocks, secondary constructors, default-argument execution, factories,
builders, setters, body properties, delegated properties, custom getters, open or inherited properties, JavaBean
conventions, serializer behavior, framework binding, object identity, `equals`, and `hashCode` are not input contract
material.

Accessor methods may be used only as read mechanisms for parts already ratified from the declaration source. A Kotlin
property getter generated for a primary-constructor `val`, or a Java record component accessor, may realize capture. A
getter-discovered property does not create an input part.

Input formation captures runtime values from the ratified parts into formed boundary material. This capture is a
stability boundary, not a canonical image and not core material. Mutable JVM objects, mutable containers, and mutable
referenced objects are not trusted as input material unless Kontrakt snapshots the accepted boundary values. V1 may
accept ordered lists only by snapshotting elements in declared order. Unordered sets and maps remain outside the V1
input
source profile until a later canonical ordering law exists.

If the declaration cannot be ratified, the contract source is rejected. If runtime material cannot be captured from the
ratified parts without ambiguity, the machine stops with declared input failure before admission begins.

Spring, Jackson, Bean Validation, JavaBean DTOs, framework request objects, and other compatibility surfaces are adapter
machinery. They may deliver or project material into a strict JVM input source. They do not define input meaning.

### 5.2. Admission Contract

Admission is the continuation judgment over formed input.

Admission does not make input. It may read formed input material, but it must not discover parts, parse outside
material,
coerce values, construct input, resolve absence, or decide what the input is.

A Kotlin or Java declaration is not an admission contract by itself. Binding it to the `admission` slot only selects a
source candidate. Kontrakt accepts that candidate only when the Kotlin/JVM or Java/JVM frontend can lower its source
body
into finite admission judgment material.

For V1, Kotlin may carry admission through a source-visible top-level function, object function, or companion object
function. Java may carry admission through a source-visible static method on a final class. The function or method is
not
executed as contract authority. The generated Kontrakt gate may execute the lowered judgment.

Java lambdas, Kotlin lambdas, method references, `Predicate`, `Function`, functional-interface instances, and
bytecode-only methods are not admission sources in V1. They are runtime function objects or implementation carriers, and
they can capture state or depend on language/runtime lowering profiles.

Admission is compiled against the ratified input part table of the selected operation. Every read must resolve to a
ratified input part. Kotlin primary-constructor property access and Java record component access are allowed only in
that
sense. JavaBean getter discovery and arbitrary method calls are not admission material.

V1 admission lowers finite boolean judgment material. It may use ratified input part reads, literals, primitive
comparisons, primitive equality, enum equality, null checks, bounded boolean combinations, and primitive integral bit
operations. Kotlin bit-operation functions are accepted only when the frontend lowers them as primitive integer bit
operations, not as arbitrary method calls.

A V1 boolean admission maps `true` to admitted and `false` to rejected. Deferred or capacity-shaped outcomes must come
from an explicit capacity, budget, policy, or later admission-result profile; they must not be hidden inside a user
method body.

Admission must not depend on constructors, service calls, IO, time, randomness, global state, mutable state, object
identity, user-defined equality, exception control flow, framework validators, reflection-discovered behavior, loops, or
recursion.

If the admission source cannot be lowered into finite judgment material, the contract source is rejected. If formed
input fails the lowered judgment, admission returns the declared rejection result. A backend may realize admission as a
gate. The gate is wrong if it disagrees with the lowered admission material.

### 5.3. Canonicalization Contract

Canonicalization gives one declared meaning one stable representative.

It is not cleanup. It is the machine refusing to let outside presentation decide internal identity.

Kontrakt lowers canonicalization into the law that decides which presentation differences preserve meaning and which
representative stands for that meaning. It must also know when a representative cannot be produced safely.

A backend may choose the algorithm. It may not merge different meanings for convenience.

### 5.4. Lowering Contract

Lowering forms core-readable candidate material.

Canonicalization gives the machine a stable representative. Lowering uses that representative to form the candidate the
core can discuss. The result is not truth.

Kontrakt lowers this role into candidate material with a declared claim. The candidate may claim to be factual material,
a movement candidate, or another closed candidate form named by the pipeline.

Lowering says what the candidate claims to be. It does not make the machine believe the claim.

### 5.5. Fact Contract

Fact is core factual material.

A fact is not the object, row, message, or value that may carry it in software. Those are carriers or realizations. The
fact is the material the core may stand on.

Kontrakt lowers the fact contract into the law for factual material: how it is identified, what meaning it belongs to,
and why it remains immutable once accepted.

A candidate fact becomes an accepted immutable fact only after the invariant accepts it.

### 5.6. Invariant Contract

Invariant is the acceptance judgment over candidate material.

Lowering says what the candidate claims to be. Invariant decides whether the machine may believe that claim.

A lowered candidate must arrive with a declared meaning. It may claim to stand under a named fact contract, with
identity
material, version meaning, and the core coordinates this pipeline has already named for judgment. Those coordinates are
not object references. They do not invite graph wandering. They only name the accepted core material this judgment is
allowed to consider.

The invariant decides whether the claim is coherent, whether identity collides with accepted material, and whether
accepting the candidate would make the core lie. If no declared meaning fits, or more than one meaning fits, the machine
must stop with declared failure.

Invariant is not a validator drawer. It is the point where a candidate either becomes believable core material or stops.

### 5.7. State Contract

State is the declared condition that governs legal next movement.

It is not a status word discovered after the fact. A label becomes state only when it changes what the machine may
legally do next.

Kontrakt lowers state into a closed, flat movement vocabulary. The vocabulary must be known before it governs movement.

A backend may realize the state surface with compact physical machinery. That machinery is not state authority.

### 5.8. State Transition Contract

Transition is permission to move.

A machine does not earn permission by having already moved. The move must be declared before the machine follows it.

Kontrakt lowers transition into one-way movement law inside a declared state surface. If a move needs a different
meaning, it needs a different transition. Repetition must be governed as repetition, not hidden as a cycle.

A backend may build transition tables or checkers. It may not invent movement.

### 5.9. Explicit State Machine Manifest

The explicit state machine manifest makes the movement surface visible.

State and transition already form a machine. This manifest prevents that machine from being reconstructed later from
implementation residue.

Kontrakt lowers the manifest into the closed map of one state surface: where it begins, where it can stop, and which
one-way moves are allowed.

A backend may analyze or compact that map. It does not become a new authority above state and transition.

### 5.10. Failure Contract

Failure is a declared stop result.

It is not a cleaned-up crash. It is the machine saying which obligation could not be satisfied and what consequence is
allowed after the stop.

Kontrakt lowers failure into material tied to the obligation that produced it. A destroyed execution does not perform a
final judgment after it is gone; only durable obligations before loss and recovery obligations after loss can be
contracted honestly.

A backend may translate platform failure only when a failure law allows that translation.

### 5.11. Publication Contract

Publication is the outward claim.

Accepted core material is not automatically public material. The machine may know more than it is allowed to say.

Kontrakt lowers publication into the law that permits or denies a public claim from accepted material. Diagnostic
material remains internal unless publication allows a public diagnostic claim.

A backend may serialize or emit the claim. Emission is not publication authority.

### 5.12. Diagnostic Evidence Contract

Diagnostic evidence is declared explanation.

It explains a judgment result. It does not produce the judgment, continue rejected material, or become another path
through the machine.

Kontrakt lowers evidence into candidate explanation material tied to the judgment that produced it. Evidence should come
from the judgment point, not from a later observer scraping machinery.

A backend may record or display evidence. It may not turn evidence into authority.

### 5.13. Diagnostic Retention Contract

Retention decides what evidence survives.

A judgment may create explanation during a run. That does not mean the explanation may remain after the run.

Kontrakt lowers retention into the boundary that decides what explanation may survive, what must be reduced, and what
must disappear. If retained material later leaves the machine, publication must judge that separately.

A backend may choose storage. The store is not the retention contract.

### 5.14. Version Coordinate

Version is a coordinate of meaning.

It matters when familiar-looking material may be read under different contract meaning.

Kontrakt lowers version into meaning coordinates attached to the material that needs them. The coordinate tells the
machine which meaning governed the material it is reading.

A backend may use tables or key material. It may not let the newest code silently reinterpret old material.

### 5.15. Policy Contract

Policy declares active judgment criteria.

Configuration may select policy, but it does not become policy. Runtime behavior may realize policy, but it does not
write the policy.

Kontrakt lowers policy into named, governed judgment material. Other judgments can read that material without depending
on an implementation option bag.

A backend may dispatch on policy. Dispatch is not policy meaning.

### 5.16. Budget Contract

Budget is consumable allowance.

It tells the machine how much a governed run may spend before a declared result is required.

Kontrakt lowers budget into allowance material with scope and exhaustion meaning. Exhaustion must be part of the
contract before the machine overruns it.

A backend may implement cheap accounting. The contract says what may be consumed and what happens when it is gone.

### 5.17. Capacity Contract

Capacity is admissible limit.

A finite machine may refuse valid material if accepting it would exceed what the machine can bear. That is not a
validation failure. It is capacity discipline.

Kontrakt lowers capacity into limit material and the declared outcome when the limit is reached. The limit must exist
before damage proves it was needed.

A backend may realize the limit physically. The physical shape is not the contract.

### 5.18. Governance Contract

Governance declares the active contract world.

Without governance, the machine does not know which law book it is reading.

Kontrakt lowers governance into the material that says what contract world is active, where it is active, and under
which coordinate it may be used.

An administration tool may install or replace governed material. The tool is machinery. Governance is the rule that
decides which world is valid.

---

## 6. Cross-Profile Boundaries

### 6.1. Candidate and Accepted Material

Lowering may produce candidate material. It does not produce accepted truth.

A candidate fact is still only a candidate. A candidate transition is still only a candidate move. Invariant and state
movement decide whether the machine may accept or follow those claims.

### 6.2. Presentation Equivalence and Core Coherence

Canonicalization handles declared equivalence in external presentation.

Invariant handles core coherence after lowering. It decides whether the candidate can stand under the named core basis.
These are different judgments.

### 6.3. Evidence and Retention

Evidence and retention stay separate.

Evidence explains a declared result. Retention decides what explanation may survive. Publication decides whether any
surviving explanation may become an outward claim.

### 6.4. Limits and Validity

Policy, budget, capacity, and governance are not late runtime decorations.

They are contract material because a finite machine must know its active rules and limits before it acts.

---

## 7. Unresolved

This ADR does not decide final authoring syntax for the eighteen bodies.

It also does not decide the future judgment language or the final host-facing API. Those decisions must follow the
boundary fixed here: source may carry contract material, judgment must be lowerable, and backend machinery must remain
replaceable.

---

## 8. Consequences

The remaining one-dimensional presentations are no longer only a catalog of names. They now have processing profiles.

Each profile begins with a machine obligation. Software realization comes only after the role has been selected by a
slot and lowered into Kontrakt-owned material.

This keeps ADR-0047's slot-selection boundary intact. It also keeps `What Contract Is` from shrinking into a Kotlin
framework vocabulary.

A user can still write ordinary software-facing material. Kontrakt may still remove boilerplate and build efficient
backend machinery. The authority sits in the lowered contract material, not in the carrier or the generated machinery.

The next ADR may decide authoring syntax or frontend carrier forms for these profiles. It must not change the processing
boundary decided here.