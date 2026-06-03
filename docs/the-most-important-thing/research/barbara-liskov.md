# Notes on Barbara-Style Modules, Subproblems, Rails, and Kontrakt Pipeline Decomposition

## Status

This is an incomplete working draft.

It records only the current position on modules, subproblems, rails, pipeline decomposition, and contract dependency.

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

The issue is how to divide a machine's contract-preserving work.

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

They may help organize source code, but they are not sufficient as contract-machine structure.

Kontrakt should decompose work as explicit material transitions in a pipeline or DAG.

---

## 2. Module Decomposition Is Not Enough

A module usually answers:

```text
Which code owns this responsibility?
```

A Kontrakt pipeline stage must answer:

```text
Which material state is accepted?
Which material state is emitted?
Which obligations are required before the transition?
Which obligations are guaranteed after the transition?
Which obligations are preserved across the transition?
Which failures close the transition?
Which resource envelope bounds the transition?
Which diagnostic evidence proves the transition?
```

Therefore, a module is not the primary unit.

A material transition is the primary unit.

A module may implement a stage.

A module may support a stage.

A module may organize code used by a stage.

But a module does not define contract authority.

---

## 3. Subproblem Decomposition Is Not Contract-Independent

Barbara-style decomposition can be read as if subproblems are independently solvable units.

That assumption is unsafe for a contract machine.

A subproblem is not merely a local task.

In Kontrakt, a subproblem is a material transition with contract consequences.

When the contract of a subproblem changes, the effect is not local.

It may change:

```text
DTO admission rules
boundary rejection rules
lowering facts
canonical material shape
invariant facts
state transition legality
policy budget
diagnostic evidence
downstream input requirements
publication eligibility
```

Therefore, subproblems are not contract-independent.

They are conditionally composable.

A stage may be implementation-independent, but it is not contract-independent.

Its output obligations must still satisfy the input obligations of downstream stages.

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
Stage A accepts material M0.
Stage A emits material M1 with obligations O1.

Stage B accepts material M1 only if O1 satisfies B's requirements.
Stage B emits material M2 with obligations O2.

Stage C accepts material M2 only if O2 satisfies C's requirements.
```

In logical form:

```text
ensures(Stage A) => requires(Stage B)
ensures(Stage B) => requires(Stage C)
```

At the whole-pipeline level:

```text
Stage A + Stage B + Stage C
=> Original Problem Contract
```

If any stage contract changes, the downstream edges must be reverified.

A change to one subproblem may invalidate the entire pipeline.

This is not because the implementation is coupled.

It is because the contract material is connected.

---

## 6. Subproblem as Material Transition

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
= contract-bearing material transition
= one node in a material-transition DAG
= valid only through edge compatibility
```

A subproblem is not independent once its output becomes another stage's input material.

The moment material crosses a stage edge, the subproblem participates in the global contract.

---

## 7. DTO and Lowering Consequences

A subproblem contract change is not merely an internal change.

If the subproblem accepts different material, emits different material, or changes what it preserves, then its DTO and
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

## 8. Contract Dependency Is Logical DAG, Not Object Tree

A real system may not be best understood as a simple tree.

Merge sort is a useful algorithmic decomposition example, but it is not enough as a system architecture example.

A system can split work into parallel rails and later join the results.

The logical structure is usually closer to a DAG:

```text
Fact A ----            -> Join Stage -> Decision Fact
Fact B ----/
```

or:

```text
Boundary
    -> Credit Fact
    -> Inventory Fact
    -> Risk Fact
        -> Approval Decision
```

The key point is not whether the graph visually looks like a tree.

The key point is that the dependency graph must be explicit and acyclic for one material lifecycle.

A downstream stage must not secretly call upstream stages through callbacks.

A downstream stage must wait for required upstream facts or reject the missing dependency.

---

## 9. Physical Rails Are Implementation, Not Contract Meaning

The rail/pipeline implementation idea is useful, but it must not contaminate the contract.

The contract should not say:

```text
offset 0 contains customer id
offset 8 contains amount
this lane is 16 bytes wide
worker A writes byte range 0..7
worker B writes byte range 8..15
```

Those are backend lowering decisions.

They belong to the compiler or runtime substrate.

The contract should say:

```text
PaymentApproval requires CustomerId.
PaymentApproval requires PaymentAmount.
PaymentAmount must be positive.
Currency must be present if the policy requires currency validation.
Approval may be published only if required facts are present and accepted.
```

The pure contract contains facts and rules.

The compiler may lower those facts into:

```text
SoA tables
slots
offsets
ring buffers
sequence barriers
static gates
worker lanes
```

But those are not contract authority.

They are implementation strategy.

---

## 10. Separating the Three Worlds

The working separation is:

```text
Contract World:
    domain facts
    obligations
    rules
    dependencies
    accepted/rejected material
    publication conditions

Compiler / Lowering World:
    canonical naming
    stable identity
    dependency DAG extraction
    lowering plan
    slot assignment
    memory layout
    static gate generation

Runtime World:
    lanes
    workers
    arrays
    offsets
    barriers
    counters
    evidence segments
```

The contract world must not contain physical memory layout.

The runtime world must obey the lowered contract material.

The compiler connects the two.

---

## 11. Lane-Based Thinking

Kontrakt should express decomposition as lanes and stages.

A lane is a controlled path of material with a specific authority level.

Example lanes:

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

A lane is a contract-controlled material path.

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

---

## 12. DAG Instead of Object Graph

The decomposition target should be a DAG of material transitions.

Not:

```text
objects calling objects
classes owning state
callbacks wiring subproblems
```

But:

```text
material moving through lanes
stages transforming material
edge contracts preserving obligations
pipeline composition proving the original problem contract
```

The nodes are stages.

The edges are material contracts.

The graph is not a class graph.

The graph is not an inheritance graph.

The graph is not a callback graph.

It is a contract dependency DAG.

---

## 13. Parallel Rails and Join Stages

Some stages can run independently.

Some stages must join several upstream facts.

Example:

```text
CreditCheck Stage
    emits CreditFact

InventoryCheck Stage
    emits InventoryFact

PaymentApproval Stage
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
A join stage may execute only when all required upstream facts are available and accepted.
```

If a required upstream fact is absent or rejected, the join stage must not invent it.

It must reject, wait according to policy, or emit a declared failure.

---

## 14. Integration Is Not Wiring

Integration should not mean wiring modules together.

Integration means verifying that the composed material transitions still satisfy the original problem contract.

A stage edge is valid only if:

```text
the upstream output obligations satisfy the downstream input obligations
```

In logical form:

```text
ensures(upstream) => requires(downstream)
```

If the upstream stage changes, this implication must be checked again.

If the downstream stage changes, this implication must be checked again.

If the original problem contract changes, the whole DAG must be checked again.

This means integration is continuous contract composition verification.

It is not a final wiring step.

---

## 15. Callback-Based Decomposition Is Suspicious

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

## 16. Cycles, Waiting, and Callback Loops

The circular dependency problem is largely a control-flow problem.

Object-centered designs often create cycles because objects call each other and then call back into each other.

That creates hidden loops in control flow:

```text
Object A calls Object B.
Object B calls Object A back.
```

In a pipeline-centered model, a downstream stage does not call upstream stages.

It consumes accepted material from upstream lanes.

If required material is not available, it waits, rejects, or emits a declared dependency failure according to policy.

This avoids callback loops.

The pipeline graph for one material lifecycle should remain acyclic.

If business logic appears cyclic, it should not be implemented by reversing the pipeline or calling upstream objects.

It should be represented as a new upstream fact or a new material instance.

---

## 17. Retry Is Not Reverse Flow

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

## 18. Current Working Rule

The current working rule is:

```text
Do not decompose by class ownership.
Do not decompose by callback structure.
Do not decompose by implementation convenience.
Do not make mechanical layout part of the contract.

Decompose by material state transition.
Represent dependencies as a DAG of facts and stages.
Lower the DAG into physical rails only after contract meaning is fixed.
```

Each stage must state:

```text
what it accepts
what it emits
what it preserves
what it rejects
what resource envelope it consumes
what evidence it leaves
```

The composed DAG must be checked against the original problem contract.

---

## 19. Open Thread

The next question is how far Barbara's module theory can be retained once the module is no longer the primary contract
unit.

Possible directions to examine later:

```text
Can a module be treated only as implementation packaging?
Can a module realize a pipeline stage without owning contract authority?
Can module boundaries be aligned with stage boundaries?
Can object-oriented modules be tolerated only on the cold authoring side?
Can callback-based modularity be rejected while preserving useful decomposition?
How should Kontrakt describe subproblem dependency in the canonical material model?
How should the logical DAG be represented before backend lowering?
Where should join-stage dependency policy live?
How should retry facts be modeled without reverse flow?
```

This draft stops here.