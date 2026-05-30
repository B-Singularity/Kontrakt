# Floyd as Limited Reference: The Confusion Between Mathematical Proof and Machine Reality

## Position

Floyd is not accepted as a foundation for this contract theory.

Floyd may be useful only in a very limited sense.

Three points are worth retaining:

1. the command body `C` can be treated as a black box;
2. a result is valid only when the required conditions around a boundary or transition are satisfied;
3. invariants matter only when repeated execution is also bounded by machine resource governance.

The useful part is not Floyd's deductive system.

The useful part is the placement of obligations around transitions.

The meaning is not inside the command body.

The command body may remain hidden.

What must remain visible is:

```text
entry obligation
-> black-box command
-> exit obligation
```

A transition is valid only when its entrance condition is satisfied and its exit obligation is preserved.

A loop is not valid merely because a logical invariant is preserved.

A loop is valid only when repeated execution preserves its invariant within the resource, capacity, allocation, timeout,
and failure boundaries allowed by the machine.

This supports three contract-theory ideas:

- implementation body is not contract authority;
- boundary and transition conditions matter;
- repetition requires both invariant preservation and resource governance.

Everything beyond this limited insight is rejected when it confuses mathematical proof with machine reality.

The core objection is this:

> Floyd confuses the mathematical world with the machine world.

A mathematical proof system is not a machine.

A substitution calculus is not memory.

A conjunction of predicates is not mechanical orthogonality.

An abstract command `C` is not an executable engine with finite capacity.

A loop invariant is not a resource budget.

## What Is Retained

### 1. `C` as a black box

Floyd's useful move is that the command body `C` can be treated as a black box.

This agrees with the central contract idea:

> The contract matters.  
> The implementation body does not define the contract.

A contract should not care about the internal algorithm, internal data structure, internal execution order, or private
implementation strategy of `C`.

The relevant structure is:

```text
precondition
-> C
-> postcondition
```

The body `C` may be hidden.

The obligation around `C` must remain visible.

This is useful because it separates:

```text
contract obligation
```

from:

```text
implementation mechanism
```

The implementation may change.

The contract obligation must remain stable.

### 2. Obligations around transitions

Floyd is also useful where he places obligations around a command rather than inside the command body itself.

The important point is not the box.

The important point is the edge.

A result is valid only if the required conditions around the boundary or transition are satisfied.

This fits boundary and transition thinking.

A boundary is not crossed by raw data alone.

A boundary is crossed only by material that satisfies the boundary's admission obligations.

This connects to:

- DTO admission;
- guard conditions;
- lowering preconditions;
- canonicalization requirements;
- validation before publication;
- legal state transition;
- accepted and rejected material.

The retained idea is:

```text
material
-> condition / guard / ratification
-> accepted material
-> valid transition / valid result
```

This is worth keeping.

But it must not be tied to Floyd's deductive system as the authority.

### 3. Invariant control is not enough without resource governance

Floyd is also useful where repeated execution is not treated as free movement.

A loop is dangerous because it repeatedly drives the machine through transitions.

The useful point is that repetition must preserve something stable.

But a loop invariant alone is not enough.

A logical invariant may remain true while the machine is being destroyed.

A loop can preserve its mathematical relation and still consume memory, exhaust allocation budget, monopolize CPU, miss
timeout deadlines, overflow diagnostic buffers, trigger contention, or force the runtime into failure.

Therefore, repetition cannot be judged only by logical invariant preservation.

A real machine needs more than:

```text
before iteration
-> invariant preserved by transition
-> after iteration
```

A real machine needs:

```text
before iteration
-> invariant preserved by transition
-> resource budget preserved or lawfully consumed
-> capacity boundary respected
-> allocation rule respected
-> timeout rule respected
-> failure boundary defined
-> after iteration
```

The retained idea is not that a loop invariant makes repetition safe.

The retained idea is that repetition must be controlled.

In this contract theory, repeated execution is controlled by both:

```text
logical invariant
```

and:

```text
machine resource governance
```

The machine may allow a loop to continue only while its invariant remains valid and its resource envelope remains
lawful.

If the loop preserves its invariant but violates resource governance, the loop is not valid.

It must be forced into a defined failure state.

The accepted rule is:

```text
invariant preservation is necessary
resource governance is necessary
neither is sufficient alone
```

This does not mean time complexity or space complexity become contract meaning.

Algorithmic complexity remains implementation behavior.

Resource governance is different.

Resource governance defines the machine boundary within which execution is allowed to proceed.

The loop is not safe because mathematics says its invariant holds.

The loop is safe only if the machine can keep executing it within bounded, governed, diagnosable limits.

## What Is Rejected

## 1. Deductive system as machine authority

Floyd introduces a deductive system `D` and uses first-order predicate calculus as the proof surface.

That may be valid as a mathematical proof model.

It is not the machine.

The machine does not run because a proposition is derivable.

The machine runs because an executable engine reads memory, evaluates inputs, performs transitions, consumes resources,
publishes results, and fails under real constraints.

A proof system can describe something about a machine.

It must not be confused with the machine itself.

The rejected move is:

```text
logical derivability
= machine validity
```

That is not accepted.

A machine is not a theorem prover merely because a proof calculus can be written about it.

## 2. Mathematical substitution is not assignment

Floyd's substitution notation can make assignment look like a clean symbolic transformation.

But assignment in a real machine is not merely replacing a symbol in a formula.

A machine assignment touches memory.

It may involve:

- addressability;
- aliasing;
- mutation;
- allocation;
- publication;
- visibility;
- failure;
- bounds;
- ordering;
- resource consumption.

Symbolic substitution does not contain these machine facts.

The mathematical operation:

```text
substitute f for x in Φ
```

is not the same thing as the machine operation:

```text
write value into machine-owned storage
```

The former belongs to the mathematical world.

The latter belongs to the machine world.

Collapsing them is an error.

## 3. Logical orthogonality is not machine orthogonality

Floyd's proof model can combine predicates by conjunction.

For example:

```text
P ∧ P'
```

In mathematics, this can be treated as a clean logical composition.

But a conjunction of predicates is not an architecture.

Logical independence does not imply mechanical independence.

`P` and `P'` may look independent on paper, but checking, preserving, publishing, diagnosing, or recovering them on a
real machine may interfere through shared physical and runtime resources.

The machine world has:

- finite memory;
- cache lines;
- memory bandwidth;
- instruction cache;
- allocation limits;
- scheduling;
- timeout;
- diagnostic buffer limits;
- publication ordering;
- failure paths.

Therefore, the fact that two predicates can be written side by side does not mean the corresponding machine work is
orthogonal.

Machine orthogonality must be constructed.

It must be created through ownership, layout, scheduling, resource bounds, isolation, and failure containment.

It must not be assumed from the shape of a formula.

The rejected move is:

```text
logical conjunction
= mechanical orthogonality
```

That is false.

## 4. Abstract command `C` is not an executable engine

In a proof calculus, command `C` can grow as a symbolic object.

On paper, it can contain more facts, more conditions, more substitutions, and more proof obligations.

But a real machine cannot execute an unbounded symbolic object for free.

A real command consumes:

- instruction space;
- memory;
- cache;
- registers;
- time;
- scheduler budget;
- diagnostic capacity;
- failure-handling surface.

A mathematical system can pretend that `C` expands without cost.

A machine cannot.

This matters because a contract theory must not treat the executable body as an infinite logical container.

An executable engine is finite.

It has capacity.

It runs under policy.

It runs under governance.

It can time out.

It can run out of memory.

It can fail.

The rejected move is:

```text
abstract command in proof calculus
= executable machine with unlimited capacity
```

That is not accepted.

## 5. Axioms are not reality

Axioms are useful inside mathematics.

They define a formal world.

But a machine does not become correct because an axiom says so.

A machine must be built, bounded, executed, observed, diagnosed, and recovered.

The mathematical world can define truth by axiom.

The machine world cannot.

The machine world requires:

- accepted inputs;
- rejected inputs;
- explicit state;
- legal transition;
- bounded execution;
- publication boundary;
- failure classification;
- diagnostic obligation;
- recovery behavior.

A proof system may reason about these things.

It must not replace them.

## 6. Loop invariant is not machine safety

Floyd-style loop reasoning can show that a relation remains true across repeated execution.

That is not the same thing as showing that the machine remains safe.

A loop may preserve its invariant and still be a bad machine loop.

Examples:

- the invariant holds while memory allocation grows without bound;
- the invariant holds while execution exceeds its time budget;
- the invariant holds while contention prevents useful progress;
- the invariant holds while diagnostic output exceeds its bounded sink;
- the invariant holds while repeated publication violates resource policy;
- the invariant holds while the machine approaches a hardware or runtime limit.

Therefore, a loop invariant is not a substitute for resource governance.

It must be placed under governance.

The rejected move is:

```text
loop invariant holds
= repeated execution is safe
```

That is false.

A real loop is safe only when its invariant, resource budget, lifecycle state, and failure boundary remain lawful
together.

## Core Distinction

This contract theory adopts the following distinction:

```text
Mathematical world:
    proposition
    axiom
    inference
    substitution
    derivability
    logical conjunction
    loop invariant

Machine world:
    input
    output
    memory
    boundary
    state
    transition
    resource
    budget
    capacity
    failure
    publication
    diagnosis
```

These worlds can interact.

But they are not the same world.

A mathematical model may describe a machine.

It does not become the machine.

A proof obligation may support a contract.

It does not define the whole contract.

A symbolic substitution may help reason about assignment.

It is not assignment.

A conjunction may help combine logical conditions.

It is not mechanical orthogonality.

A loop invariant may help reason about repetition.

It is not machine safety.

## Final Judgment

Floyd is retained only as a limited historical reference.

Accepted:

1. `C` may be treated as a black-box implementation body.
2. Obligations around boundaries and transitions matter.
3. Valid results require satisfied entrance and exit obligations.
4. Repetition requires invariant control, but only under resource governance.

Rejected:

1. first-order deductive system as contract authority;
2. axioms as machine truth;
3. substitution calculus as machine assignment;
4. logical conjunction as mechanical orthogonality;
5. abstract command `C` as executable engine;
6. loop invariant as sufficient machine safety;
7. mathematical proof surface as a replacement for machine design.

The final judgment is:

> Floyd saw that conditions around transitions matter.  
> Floyd did not define the machine.

This contract theory keeps the boundary-condition insight.

It keeps invariant control only when bounded by machine resource governance.

It rejects the confusion between mathematical proof and machine reality.