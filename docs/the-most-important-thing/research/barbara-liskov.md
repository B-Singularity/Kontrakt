# Notes on Barbara-Style Modules, Subproblems, Taxonomy, Contract Flow, and Kontrakt Pipeline Decomposition

## Status

This is an incomplete working draft.

It records only the current position on modules, subproblems, taxonomy, abstraction, contract flow, pipeline
decomposition, and contract dependency.

It should not be read as a final judgment on Barbara's full argument.

The document will be extended while the source text is read further.

---

## 1. Initial Position

Barbara's warning about decomposition is useful.

A large problem can be split into smaller subproblems, and each subproblem can appear to be solved locally, while the
composed result still fails to solve the original problem.

That warning matters for Kontrakt.

However, this contract theory does not accept object-oriented modules as the primary answer to that problem.

The issue is not merely how to divide code.

The issue is how to divide software obligations into contract-preserving units.

Kontrakt should not treat the primary decomposition unit as:

```text
class
object
callback
method group
subtype hierarchy
implementation owner
```

Those are host-language implementation structures.

They may help organize source code, but they are not sufficient as contract structure.

Kontrakt should decompose work as explicit contract dependencies, material transitions, and pipeline stages.

---

## 2. Module Decomposition Is Not Enough

A module usually answers:

```text
Which code owns this responsibility?
```

A Kontrakt contract decomposition must answer:

```text
What must the software promise?
Which contract terms belong together?
Where does one contract stop and another begin?
Which implementation details are excluded?
Which downstream contract depends on this result?
Which changes would break the contract?
```

A machine realization may later answer:

```text
Which material state is accepted?
Which material state is emitted?
Which obligations are preserved across the transition?
Which failure closes the path?
Which resource envelope bounds the transition?
Which diagnostic evidence proves the transition?
```

These are not the same level.

The general abstraction problem is contract selection.

The machine problem is contract realization.

A module may realize a selected contract.

A module may support a stage.

A module may organize code used by a stage.

But a module does not define contract authority.

Implementation is only a backend realization of contract meaning.

---

## 3. Subproblem Decomposition Is Not Contract-Independent

Barbara-style decomposition can be read as if subproblems are independently solvable units.

That assumption is unsafe for a contract system.

A subproblem is not merely a local task.

In Kontrakt, a subproblem is a contract-bearing part of a larger dependency structure.

When the contract of a subproblem changes, the effect is not local.

It may change:

```text
admission rules
rejection rules
required facts
emitted facts
canonical material shape
invariant terms
state transition legality
policy terms
diagnostic terms
downstream input requirements
publication eligibility
```

Therefore, subproblems are not contract-independent.

They are conditionally composable.

A stage may be implementation-independent, but it is not contract-independent.

Its emitted contract terms must still satisfy the required terms of downstream contracts.

---

## 4. The Incorrect Model

The incorrect model is:

```text
Subproblem A is solved independently.
Subproblem B is solved independently.
Subproblem C is solved independently.

Then A, B, and C are wired together.
```

This is too weak.

It treats integration as a later assembly task.

It assumes that local correctness of subproblems is enough.

Kontrakt rejects that assumption.

Local subproblem success does not imply global contract success.

---

## 5. The Kontrakt Model

The Kontrakt model is:

```text
Contract Part A requires input terms IA.
Contract Part A emits output terms OA.

Contract Part B may consume OA only if OA satisfies B's required terms.
Contract Part B emits output terms OB.

Contract Part C may consume OB only if OB satisfies C's required terms.
```

In logical form:

```text
ensures(A) => requires(B)
ensures(B) => requires(C)
```

At the whole-system level:

```text
A + B + C
=> Original Problem Contract
```

If any subproblem contract changes, the downstream edges must be reverified.

A change to one subproblem may invalidate the entire dependency graph.

This is not because the implementation is coupled.

It is because the contract terms are connected.

---

## 6. Subproblem as Contract-Bearing Transition

A subproblem should not be modeled as an isolated implementation problem.

Rejected model:

```text
Subproblem
= local algorithmic task
= implemented by one module
= later wired to other modules
```

Accepted model:

```text
Subproblem
= contract-bearing transition
= one node in a contract dependency DAG
= valid only through edge compatibility
```

A subproblem is not independent once its output becomes another contract's required input.

The moment a result crosses a stage edge, the subproblem participates in the global contract.

---

## 7. DTO and Lowering Consequences

A subproblem contract change is not merely an internal change.

If the subproblem accepts different facts, emits different facts, or changes what it preserves, then its DTO and
lowering consequences change.

Example:

```text
Stage A originally emits CanonicalCustomerId.
Stage B requires CanonicalCustomerId.
```

If Stage A changes:

```text
Stage A now emits RawCustomerId.
```

Then Stage A may still claim to solve its local task.

But the pipeline is no longer valid.

Stage B's requirement is not satisfied.

The issue is not source-code integration.

The issue is contract-material incompatibility.

Therefore:

```text
A success != pipeline success
```

and:

```text
subproblem correctness != original problem correctness
```

---

## 8. Biological Taxonomy Is Not Machine Abstraction

Barbara's mammal example treats abstraction as a hierarchy of common characteristics.

At one level, the concept is `mammal`.

At a lower level, the model may distinguish groups such as primates or rodents.

At a still lower level, it may distinguish species or individuals.

This is a biological taxonomy model.

It may be useful when the domain itself is a relatively stable classification hierarchy.

But it is a dangerous default model for software machines.

A machine is not a biological family tree.

A machine is a system of contract selection, material admission, state transition, obligation preservation, failure
closure, and publication.

The mammal example encourages a parent-child view of abstraction:

```text
Mammal
    -> Primate
        -> Human
        -> Chimpanzee
    -> Rodent
```

This is not how a contract machine should be understood.

A contract machine should not begin with:

```text
general class
-> specialized subclass
-> lower-level subclass
-> instance
```

It should begin with:

```text
software promise
-> contract term
-> dependency
-> boundary of responsibility
-> realization
```

The biological hierarchy model is therefore limited.

It may describe a stable vocabulary.

It may describe a closed taxonomy.

It may help name concepts.

But it must not become the architectural principle of the machine.

---

## 9. Why the Biological Model Misleads Machine Design

The biological model assumes that abstraction primarily means:

```text
find common characteristics
ignore differences
organize concepts into levels
```

This can easily lead to software principles centered on:

```text
class hierarchy
inheritance
subtype relation
common method surface
object taxonomy
```

That is the wrong center for Kontrakt.

The machine does not need a family tree.

The software first needs selected contracts.

The machine later needs explicit ways to realize and enforce those selected contracts.

A useful software abstraction should answer:

```text
What must the software promise?
What may other units rely on?
Which terms belong together?
Which terms must remain separate?
Where should a contract boundary be drawn?
Which implementation details must be excluded?
```

A machine realization may then answer:

```text
Which facts are admitted?
Which material state exists after this boundary?
Which transitions are legal?
Which failures close the path?
What can be published?
What evidence proves it?
```

The biological hierarchy does not naturally answer these questions.

It answers a different question:

```text
What group does this thing belong to?
```

That may be useful for classification.

It is not sufficient for contract authority.

---

## 10. Abstraction as Contract Selection, Not Machine Artifact Selection

The more general position is:

```text
Abstraction is the act of deciding what contract must remain after implementation detail is removed.
```

This should not be stated first in machine-runtime terms.

The primary question is not:

```text
Which boundary, state, failure, or publication mechanism should the machine implement?
```

The primary question is:

```text
What must the software promise?
What must remain true even if the implementation changes?
What may users, downstream stages, or other software units safely rely on?
What should be stated as contract, and what should remain implementation detail?
```

Abstraction is therefore the act of selecting, refining, and grouping contract meaning.

It decides:

```text
which software properties are essential enough to become contract terms
which terms belong together as one contract
which terms should remain separate contracts
where one contract should stop and another should begin
which changes are contract-breaking
which details must be excluded as implementation
```

This is the general theory.

A contract may later be realized by a good machine through boundaries, material states, legal transitions, failure
closure, and publication rules.

But those are machine realization concerns.

They are not the first definition of abstraction.

The general rule is:

```text
Abstraction selects the contract.
The machine realizes and enforces the selected contract.
```

Therefore, the mammal-style hierarchy is not the general model of abstraction.

At most, it is a domain vocabulary example.

Kontrakt's abstraction model is contract-first, not taxonomy-first.

---

## 11. Limited Use of Taxonomy

A taxonomy may be tolerated only in limited cases.

Examples:

```text
closed vocabulary
stable classification
diagnostic category
failure category
state label
domain naming
```

Even then, taxonomy does not own contract authority.

For example:

```text
PaymentFailure
    -> InsufficientFunds
    -> InvalidCurrency
    -> PolicyRejected
```

This may be a useful diagnostic classification.

But the actual contract is not the inheritance tree.

The actual contract is:

```text
which failure is emitted
under what condition
from which contract boundary or stage
with what diagnostic evidence
and whether the result may proceed
```

The hierarchy may name the failure.

It does not define the software obligation.

---

## 12. Contract Flow Exists Before Machine Realization

The contract world is not flat.

Even before machine realization, contracts create logical flow.

One contract term may require another term.

One accepted fact may allow another fact to be derived.

One contract may emit material required by another contract.

A publication decision may depend on several accepted facts.

This logical flow is contract meaning.

However, it is not yet a runtime lane.

The contract world owns the dependency DAG.

The compiler lowers that DAG into stages, gates, slots, and possibly physical lanes.

The runtime executes the lowered plan.

Therefore, the working distinction is:

```text
Contract Flow
    logical dependency between contract terms and emitted facts

Runtime Lane
    physical execution path selected by compiler/runtime lowering
```

The contract should acknowledge contract flow.

It should not contain runtime lanes as physical implementation.

---

## 13. Contract Dependency DAG, Not Physical Lane

A contract dependency graph may look like a rail or pipeline because facts move from input to output.

That does not mean the contract contains physical rails.

The contract should say:

```text
Fact B requires Fact A.
Decision C requires Fact A and Fact B.
Publication P requires Decision C.
If Fact A is rejected, Decision C cannot be published.
```

The contract should not say:

```text
Fact A is produced in worker lane 3.
Fact B waits on sequence barrier 7.
Decision C reads ring buffer offset 128.
```

The first group is contract meaning.

The second group is runtime realization.

Use the term:

```text
Contract Dependency DAG
```

for the logical structure.

Reserve:

```text
Lane
Rail
Ring buffer
Worker
Barrier
Offset
```

for compiler/runtime realization.

---

## 14. Machine Abstraction Is Boundary-Oriented

At the machine realization level, abstraction becomes boundary-oriented.

For example:

```text
RawPaymentRequest
-> Boundary Admission
-> AdmittedPaymentCommand
-> Policy Check
-> ApprovalDecision
-> Publication
```

This is not a species hierarchy.

It is a sequence of material states and legal transitions.

The abstraction is not:

```text
Payment is a kind of Request.
PaymentApproval is a kind of Payment.
PublishedPayment is a kind of PaymentApproval.
```

The machine realization is:

```text
this boundary admits these terms
this stage preserves these obligations
this transition is legal only under these conditions
this result may proceed only after these checks
```

That is the machine-native form.

But this remains a realization of the selected contract.

It is not the first principle of abstraction.

---

## 15. Separating the Three Worlds

The working separation is:

```text
Contract World:
    contract terms
    selected software promises
    contract grouping
    contract boundaries in the semantic sense
    required terms
    emitted terms
    dependency DAG
    admissibility conditions
    publishability conditions
    declared failure meanings

Compiler / Lowering World:
    canonical naming
    stable identity
    dependency DAG extraction
    stage formation
    lowering plan
    slot assignment
    memory layout
    static gate generation
    physical lane planning

Runtime World:
    lanes
    workers
    arrays
    offsets
    barriers
    counters
    evidence segments
```

The contract world may contain logical flow.

It may contain a dependency DAG.

It may contain requirements that one fact must exist before another fact can be derived or published.

But the contract world must not contain physical memory layout.

The contract world must not contain runtime lanes as implementation.

The runtime world must obey the lowered contract material.

The compiler connects the two.

---

## 16. Lane-Based Thinking as Runtime Realization

Kontrakt may express runtime execution as lanes and stages.

A lane is a controlled path of material with a specific authority level.

Example runtime lanes:

```text
RawInputLane
AdmittedMaterialLane
LoweredMaterialLane
FrozenMaterialLane
ExecutablePlanLane
PublishedResultLane
FailureDiagnosticLane
```

A lane is not a class.

A lane is not a queue of arbitrary objects.

A lane is a runtime realization of a contract-controlled material path.

Each lane determines what is allowed to exist there.

Example:

```text
RawInputLane:
    may contain hostile or untrusted material

AdmittedMaterialLane:
    may contain only boundary-accepted material

FrozenMaterialLane:
    may contain only immutable canonical material

PublishedResultLane:
    may contain only material that passed publication gates
```

A stage moves material between lanes.

The stage is valid only if the movement preserves the required obligations.

However, the lane is a realization artifact.

The contract-level structure is the dependency DAG.

---

## 17. DAG Instead of Object Graph

The decomposition target should be a DAG of contract dependencies and material transitions.

Not:

```text
objects calling objects
classes owning state
callbacks wiring subproblems
```

But:

```text
contract terms depending on contract terms
material moving through realized stages
edge contracts preserving obligations
pipeline composition proving the original problem contract
```

The nodes are contract-bearing stages or facts.

The edges are dependency obligations.

The graph is not a class graph.

The graph is not an inheritance graph.

The graph is not a callback graph.

It is a contract dependency DAG.

---

## 18. Parallel Contract Flow and Join Stages

Some contract paths can proceed independently.

Some contract paths must join several upstream facts.

Example:

```text
CreditCheck
    emits CreditFact

InventoryCheck
    emits InventoryFact

PaymentApproval
    requires CreditFact
    requires InventoryFact
    emits ApprovalDecision
```

Physically, CreditCheck and InventoryCheck may run in separate lanes.

Logically, PaymentApproval depends on both.

Therefore, the contract dependency graph is a DAG.

The runtime may implement that DAG using rails, queues, barriers, or tables.

But the contract is the dependency between facts, not the physical barrier itself.

The join rule is:

```text
A join contract may proceed only when all required upstream facts are available and accepted.
```

If a required upstream fact is absent or rejected, the join must not invent it.

It must reject, wait according to policy, or emit a declared dependency failure.

---

## 19. Integration Is Not Wiring

Integration should not mean wiring modules together.

Integration means verifying that composed contract dependencies still satisfy the original problem contract.

A dependency edge is valid only if:

```text
the upstream emitted terms satisfy the downstream required terms
```

In logical form:

```text
ensures(upstream) => requires(downstream)
```

If the upstream contract changes, this implication must be checked again.

If the downstream contract changes, this implication must be checked again.

If the original problem contract changes, the whole DAG must be checked again.

This means integration is continuous contract composition verification.

It is not a final wiring step.

---

## 20. Callback-Based Decomposition Is Suspicious

Callback-based module decomposition is especially dangerous.

A callback hides control flow.

It may not clearly state:

```text
when it runs
which material state it receives
which invariants it assumes
which failures it may emit
which resource budget it consumes
which evidence it must preserve
```

Therefore, a callback is not a contract edge.

If callback-like frontend syntax is used, it must be transformed into explicit stage identity before canonical material
is formed.

The surviving form must be:

```text
StageId
OperationId
PredicateId
TransitionId
PolicyRuleId
```

not a callback value.

---

## 21. Cycles, Waiting, and Callback Loops

The circular dependency problem is largely a control-flow problem.

Object-centered designs often create cycles because objects call each other and then call back into each other.

That creates hidden loops in control flow:

```text
Object A calls Object B.
Object B calls Object A back.
```

In a pipeline-centered model, a downstream stage does not call upstream stages.

It consumes accepted material from upstream contract paths or runtime lanes.

If required material is not available, it waits, rejects, or emits a declared dependency failure according to policy.

This avoids callback loops.

The contract dependency graph for one material lifecycle should remain acyclic.

If business logic appears cyclic, it should not be implemented by reversing the pipeline or calling upstream objects.

It should be represented as a new upstream fact or a new material instance.

---

## 22. Retry Is Not Reverse Flow

A retry should not be implemented by a downstream worker pushing material backward into an upstream stage.

That would break the one-way structure.

A failed stage should emit a failure fact or diagnostic material.

Example:

```text
PaymentApproval failed because InventoryFact was stale.
```

The worker should not call the upstream inventory stage.

It should publish the failure or retry-needed fact according to policy.

A later retry is a new upstream event or a new material instance.

For the pipeline, that retry is not the old material traveling backward.

It is new material entering the upstream boundary.

Working rule:

```text
Failure is a fact.
Retry is a new upstream fact.
The pipeline does not run backward.
```

This keeps workers simple.

A worker processes the current material, emits accepted output or declared failure, and moves on.

---

---

---

## 23. Implementation as Shadow Realization

Implementation must be described carefully.

In this contract theory, implementation is not a parallel source of meaning.

Implementation is a shadow realization of the contract.

The contract owns meaning.

Implementation realizes that meaning in a backend-specific way.

All meaningful flow should pass through contract material:

```text
accepted facts
declared boundaries
declared transitions
declared failures
publication decisions
```

An implementation may use objects, callbacks, channels, repositories, platform APIs, or framework mechanisms internally.

But those mechanisms must not become contract authority.

The implementation should behave like a black box:

```text
accepted contract material
-> implementation realization
-> candidate fact / candidate transition
-> Kontrakt judgment
-> accepted fact / accepted transition / declared failure
```

The implementation does not publish truth directly.

It produces candidates.

Kontrakt judges whether those candidates can become accepted contract material.

This is why contract and implementation must not be mixed.

If implementation and contract are mixed, replacing an implementation changes the contract meaning.

That is rejected.

A realization must remain replaceable as long as it preserves the same contract obligations.

Working rule:

```text
Implementation computes.
Contract defines.
Kontrakt judges.
Only accepted contract material flows forward.
```

## 24. Package Hiding as Contract Entry Protection

Barbara's package discussion can be useful if it is not treated as contract authority.

The useful point is hiding.

The rejected interpretation is:

```text
package boundary
= contract authority
```

A package should not define contract meaning.

A package should not own the contract.

A package should not prove that the implementation satisfies the contract.

However, package-level hiding can still be useful as a structural protection mechanism.

In Kontrakt, the package should expose only the official contract entrypoint of a pipeline or use case.

Everything else should remain hidden as realization support.

The purpose is not merely information hiding.

The purpose is to reduce non-contractual entry paths.

### 24.1 Public Entry Should Be the Contract Boundary

A pipeline or use case should have one intended public ingress.

That public ingress may be:

```text
interface contract surface
boundary port
operation contract surface
service contract facade
state-machine command entrypoint
```

The public entrypoint is the point where Kontrakt can attach or verify:

```text
admission rule
required terms
emitted terms
state transition rule
failure vocabulary
realization binding
conformance tests
```

Other helpers should not be equally public.

If internal helpers are public, callers can bypass the intended contract path.

That makes the package structure hostile to contract verification.

### 24.2 Internal Realization Should Stay Behind the Boundary

Internal realization support may include:

```text
guard helper
lowering helper
mapper
transition helper
repository adapter
diagnostic mapper
service realization
generated test fixture
generated static gate
```

These may be necessary.

But they should not become public contract entrypoints.

A caller should not be encouraged to call:

```text
internal transition helper
internal lowering function
repository adapter directly
guard bypass path
diagnostic mapper as execution path
```

Those are realization details.

They support the contract path.

They do not define the contract.

They must remain replaceable backend realization details.

### 24.3 Package Hiding as Anti-Bypass Hygiene

Package hiding is useful because it makes the correct path the convenient path.

The package should make this easy:

```text
caller
-> public contract boundary
-> Kontrakt-controlled verification path
-> implementation realization
```

The package should make this difficult:

```text
caller
-> internal helper
-> realization detail
-> contract bypass
```

This is not absolute security.

JVM visibility can be bypassed through reflection, framework access, test exposure, proxies, agents, or module opens.

Therefore, package hiding must not be treated as a hard trust boundary.

It is structural hygiene.

The authority remains in canonical contract material and verifier evidence.

### 24.4 Package Is Implementation Packaging

The package is a realization packaging device.

It may help align code with contract boundaries.

It may help reduce accidental misuse.

It may help keep generated guard, lowering, transition, and diagnostic code out of the public surface.

But the package does not decide:

```text
what the software must promise
which contract terms belong together
which emitted terms satisfy downstream requirements
which state transition is legal
which result may be published
```

Those are contract questions.

The package only protects the selected public surface.

### 24.5 Kontrakt Rule for Packages

The working rule is:

```text
A package should expose the contract-controlled entrypoint.

A package should hide realization details.

Package hiding protects the path to the contract boundary.

Package hiding does not define the contract.
```

For example:

```text
order.submit public surface:
    SubmitOrderContract
    SubmitOrderPort
    SubmitOrderBoundary

order.submit internal realization:
    SubmitOrderGuard
    SubmitOrderLowering
    SubmitOrderTransitionCheck
    SubmitOrderHandler
    SubmitOrderRepositoryAdapter
    SubmitOrderDiagnostics
```

The public surface is where contract interaction begins.

The internal realization is how the contract is realized, checked, lowered, diagnosed, or tested.

This preserves Barbara's useful hiding idea while rejecting package-level contract authority.

## 25. Current Working Rule

The current working rule is:

```text
Do not decompose by class ownership.
Do not decompose by callback structure.
Do not decompose by implementation convenience.
Do not make mechanical layout part of the contract.
Do not import biological taxonomy as the default model of machine abstraction.
Do not treat abstraction as class genealogy.
Do not treat package hiding as contract authority.
Do not expose non-contractual bypass paths as public API.
Do not let implementation publish truth directly.

Decompose by contract selection and contract dependency.
Represent dependencies as a DAG of contract terms, facts, and stages.
Recognize contract flow as contract meaning.
Lower the DAG into physical rails only after contract meaning is fixed.
Treat runtime lanes as realization, not as primary contract meaning.
```

At the general contract level, each abstraction must clarify:

```text
what the software must promise
which contract terms belong together
where one contract ends and another begins
which implementation details are excluded
which changes would break the contract
which downstream terms depend on the emitted terms
```

At the machine realization level, each stage may then define:

```text
what material it accepts
what material it emits
what it preserves
what it rejects
what resource envelope it consumes
what evidence it leaves
```

The composed DAG must be checked against the original problem contract.

---


This draft stops here.