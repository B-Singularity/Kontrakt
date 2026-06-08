# What Is Contract

## 0. Intention

The most important thing in software is intention. Not class, not object, not type theory, and not some beautiful
mathematical toy that looks clean on paper and runs like crap on a real machine.

I am going to describe a contract theory and a machine that fit my intention. Do not memorize this like a textbook. That
is not the point. I am not trying to hand down a holy law of software. I am trying to describe the kind of machine I
think should exist, and why that machine should be shaped this way.

Software is not built in heaven. It is built under limits, with bad inputs, broken environments, unclear requirements,
strange users, and machines that do exactly what you told them, not what you hoped they would understand. So the
question is not, "Is this theory beautiful?" The question is, "Does this help build a machine that works for the purpose
I declared?"

```text
I am not writing universal truth.
I am writing the shape of a machine I want to build.
```

If the machine does not fit the purpose, the theory is useless. If the theory is elegant but the machine is garbage,
then for engineering that theory is garbage too. That is where this starts.

---

## 1. Mathematics, Physics, and Engineering

Mathematics, physics, and engineering do not play the same game. People mix them together all the time, and a lot of
programming bullshit starts exactly there.

Mathematics is allowed to start from axioms, define objects, prove theorems, and live inside formal consistency. Inside
mathematics, that is fine. Mathematics can search for formal truth. It can reduce a whole system to one primitive symbol
if it wants. Good for mathematics.

But that does not mean a single fucking thing for engineering.

A formal system being consistent does not mean it is a good machine. A formal reduction being possible does not mean it
should be used. A beautiful proof does not pay for cache misses, allocation, failure modes, debugging, operational cost,
or the poor bastard who has to maintain the thing at 3 a.m.

Physics is different. Physics is not some church for worshiping absolute truth. Physics is a disciplined guessing game
against the world. You look at reality and say:

```text
Maybe it works like this.
```

Then you build a theory and test it. If the theory fits the world well enough, good. You keep it, use it, and refine it.
If it does not fit the world, you do not worship it. You go back and say:

```text
Then maybe it works like this instead.
```

That is the whole point. A physical theory is not truth itself. It is a useful approximation that survives contact with
the world. No amount of beauty saves a theory that fails against reality. Reality does not give a shit about elegance.

Engineering is physics with a stronger purpose. It does not ask whether the model is formally beautiful. It asks whether
we can build the machine, whether it does the job, whether the cost is acceptable, whether failure can be diagnosed,
whether it runs under real constraints, whether it can be maintained, and whether people can actually use it.

If the answer is no, I do not care how elegant the theory is. Call it mathematics. Do not call it good engineering.

---

## 2. Use Mathematics. Do Not Become Mathematics.

Mathematics is useful. I am not saying to throw it away. Use it as a tool. Use it as a language. Use it to measure,
describe, constrain, and reason. But when the job is to build a machine, do not turn engineering into pure mathematics.

This is the part that keeps annoying me in programming. Again and again, people try to take a mathematical shape and
smuggle it into engineering as if the machine owes it obedience. Formal systems, combinators, type theory, higher-order
functions, lazy evaluation, inheritance hierarchies, subtyping games, callback hell, proxy magic. Some of these are
interesting as mathematics. Some of them are useful in narrow places. But when they become the foundation of how we
build machines, things often get ugly.

Beautiful on paper. Fucking miserable on real machines.

If something is so elegant, then where is the good machine? If nobody uses it because it is slow, opaque, hard to debug,
hard to operate, and hostile to real constraints, then what exactly did we build? A mathematical artifact, maybe. Not
good engineering.

I am not interested in turning software into a shrine for formal elegance. I am interested in a machine that works.

---

## 3. Contract

```text
A contract is the declared set of obligations software must satisfy.
```

That is the definition I am using.

This definition is intentionally not tied to class, object, type trick, framework, or implementation style. A contract
is not what a class looks like. It is not what an object happens to contain. It is not what a type theorist can encode
with enough clever machinery.

A contract is the declared obligation.

Now, there is a structural problem. If contracts can inherit other contracts, compose other contracts, include other
contracts, and then those contracts do the same thing again, the whole thing becomes a genealogy. It becomes the same
old
family tree with a cleaner name.

That is not contract clarity. That is inheritance cosplay.

So the contract structure should be kept two-dimensional.

The first dimension contains closed base contract presentations:

```text
input contract
admission contract
lowering contract
fact contract
invariant contract
state machine contract
failure contract
publication contract
diagnostic contract
policy / budget / capacity contract
```

The second dimension contains interaction manifests. An interaction manifest does not inherit contracts. It does not
compose interfaces. It does not pull in another manifest, which pulls in another manifest, which pulls in another one
until nobody knows what the hell is actually required.

It simply binds a flat list of closed base contracts for one interaction.

```text
interaction manifest
    -> input contract
    -> admission contract
    -> lowering contract
    -> fact contract
    -> invariant contract
    -> state machine contract
    -> failure contract
    -> publication contract
    -> diagnostic contract
    -> policy / budget / capacity contract
```

A state machine may feel like orchestration, but in this model it is still a closed base contract presentation. It
should
not inherit another state machine. It should not compose another state machine. It should state its own states and legal
transitions and stop there.

The rule is simple:

```text
No interface inheritance.
No interface composition.
No same-kind contract inheritance.
No same-kind contract composition.
No recursive manifest.
No hidden transitive obligations.
No cyclic contract reference.
```

A manifest is not genealogy. A manifest is a flat table. If I need to open five ancestors to understand one interaction,
this is not a contract document. It is the same garbage coming back through the side door.

And no, I do not trust people to follow this politely.

People are too used to inheritance. If this is only written as advice, someone will bring inheritance and composition
back into contracts and call it "reuse" or "clean design." I know how this shit goes.

Normally, a contract theory should not talk about implementation here. But I will say it once.

Contract inheritance and recursive contract composition must be rejected by the compiler.

Not discouraged.

Rejected.

No interface inheritance. No interface composition. No same-kind contract inheritance. No same-kind contract
composition.

I do not trust you. The compiler should stop you.
---

## 4. Make Interfaces Great Again

Software needs a way to present contracts. One of the most important surfaces for that is the interface.

```text
Interface:
    the software-visible contract presentation for interaction.
```

An interface is not an implementation skeleton. It is not a class without fields. It is not a cute method list.

It is the software-visible contract document for interaction.

The sad thing is that modern interfaces usually do not do this. They are mostly weak method shells. They say almost
nothing. A method list says, "you can call this shape." That is barely a contract document.

The rest of the contract gets scattered into external documents, comments, validation code, tests, wiki pages, hidden
conventions, and runtime behavior. Then everyone acts surprised when the system becomes hard to reason about.

A real interface should not only say what can be called. It should expose the contract structure of the interaction:

```text
input contract
admission contract
lowering contract
fact contract
invariant contract
state machine contract
failure contract
publication contract
diagnostic contract
policy / budget / capacity contract
```

But it must expose them as a flat manifest, not as an inheritance tree and not as recursive composition.

The first goal is to turn the interface from a weak method shell into a rich contract document.

Make interfaces great again.
---

## 5. Contract and Verification

If the interface is the contract document, verification is the check that a realization satisfies the declared
obligations.

Verification does not mean "tests" by default. A test is only one form of verification, and usually a late and limited
one. A test does not create quality. It does not make bad software good. It only checks whether the checked case appears
to satisfy the contract.

If the contract is rich enough, some verification should happen before tests. A compiler, a contract compiler, a
verifier, or generated checks should be able to resolve part of it earlier. The better the contract document is, the
less we need to guess through scattered tests.

A test is what we use when the compiler or verifier cannot see enough. Sometimes that is necessary. But do not worship
it.

TDD sold one ordinary fact like it had discovered fire:

```text
if you know the contract before the implementation,
you can write the verification before the implementation.
```

Fine. That part is useful. If verification exists first, the realization can be checked immediately while it is being
built. But that is all it is.

TDD is not a theory of quality. TDD is not a replacement for contract. TDD is not design by itself. It is just writing
one verification document before the realization. And if the contract is explicit enough, even that verification should
be derived from the contract as much as possible, not manually guessed into existence.

The real problem is that modern programming often forgot to write the actual contract document. So the contract gets
scattered into test cases. Then people start worshiping tests and say:

```text
but the tests pass
```

So what?

Passing tests only means the checked cases passed. It does not mean the design is good. It does not mean the contract
was complete. It does not mean the machine is well-shaped.

The order should be simple:

```text
contract document
-> compiler / verifier / generated checks where possible
-> tests where needed
-> realization
```

The best verification is not a mountain of tests. The best verification is making invalid software impossible to write,
impossible to compile, or impossible to publish before it becomes a runtime mess.

DBC was not wrong because it wanted contracts. It went wrong because it glued contracts onto the wrong substrate:
classes, inheritance, runtime wrappers, proxies, and implementation-shaped objects. Of course that shit became slow and
ugly. Of course people stopped using it.

The answer is not to scatter contracts into comments, wiki pages, assertions, and test fragments. The answer is to make
the contract document real.

The interface should carry the contract. The verifier should derive what it can from the declared obligations. Tests
should check what cannot be resolved earlier. All of them should come from the same declared contract, not from
scattered human guesses.

That is the part modern programming scattered all over the floor.

Make interfaces great again.
---

## 6. Evolution and Contract

Contracts should be stable and immutable. If every small implementation change rewrites the contract, then there is no
contract. There is only noise.

But stability is not the highest law. Evolution comes before immutability. If the system must evolve, the contract may
need to change. Do not reject evolution just to worship immutability. That is another stupid way to turn engineering
into a shrine.

The rule is simple for now:

```text
Contracts should remain stable unless the system evolves.
When the obligation changes, the contract changes.
When only the realization changes, the contract should not change.
```

This is not finished. The full versioning story is still left.

---

## 7. Good Machine and Function

A good machine should ideally behave like a function. Given the same accepted input, it should produce the same accepted
output. That is the clean shape. That is what we want.

But that perfect fantasy does not exist in the real world.

A real machine is not a pure mathematical function. It has memory, time, failure, capacity, latency, storage, network,
concurrency, corruption, hostile input, and broken dependencies. So the model is not simply this:

```text
Input -> Output
```

The real shape is closer to this:

```text
input
+ environment
+ resource state
+ time
+ policy
-> accepted output
   or rejected input
   or declared failure
   or deferred work
   or diagnostic material
```

A good machine should still try to be function-like. It should be stable. It should be repeatable where repeatability is
required. It should not randomly change its mind like a drunk bastard.

But a good machine must also admit reality. Users are hostile. Inputs are garbage. Networks die. Disks lie. Memory runs
out. Threads race. Dependencies timeout. The environment is a mess. If your machine assumes the happy path, your machine
is trash.

A good machine is not a fantasy function. It is a function-like system that admits failure, cost, and damage.

---

## 8. Applying Contract to a Good Machine

Now I want to apply the contract definition to a good machine and ask: what counts as contract inside that machine?

The interface names the interaction contract at the software surface. When that contract is applied to a good machine,
the interaction cannot remain a flat method call. It unfolds into a causal flow.

The interface is the contract document at the surface.

The pipeline is the explicit causal shape needed to satisfy that contract inside the machine.

From a purely functional view, we may care only about stable input and stable output. The middle can be ignored. But
that is not enough for a real machine. A machine does not get to skip the middle. The middle is where admission,
rejection, lowering, state, failure, evidence, and publication actually happen.

A more advanced machine splits and joins this middle causally. That looks like a logical pipeline. The machine is not
one magic function. It is closer to a composition of staged functions, where each stage has its own input, output,
judgment, and failure path.

Mathematicians often hide the middle in implicit forms: nested calls, closures, lazy thunks, higher-order functions,
reduction graphs. That may be fine for a formal model. It is not the machine I want.

A good machine should not hide causal flow like that. It should make the causal process explicit.

Each stage and each causal edge can be understood as a small function with a domain and a codomain. A pipeline with
explicit flow is the good form of composition here. Not a naive expression that hides everything inside notation, but an
actual causal line the machine can control.

For control, each pipeline should have a single entry and a single exit. If everything can enter from everywhere and
leave from anywhere, you do not have a pipeline. You have a mess.

A rough contract pipeline looks like this:

```text
input
-> DTO / raw presentation
-> boundary
-> guard / admission judgment
-> declared failure or admitted material
-> normalization / canonicalization
-> lowering
-> core
-> candidate fact / candidate transition
-> invariant / state judgment
-> accepted immutable fact or declared failure
-> publication judgment
-> published result
-> diagnostic evidence
```

Policy, budget, and capacity are not one naive stage at the end. They cut across the whole flow:

```text
policy / budget / capacity:
    applies across boundary, execution, failure, and publication
```

This is still a working shape. Each step needs its own explanation later.

---

## 9. What Counts as Contract in the Pipeline

The pipeline itself is not automatically contract. Do not confuse a processing sequence with a contract.

The contract is the declared obligation inside the flow. It is what the software must satisfy at each meaningful point:
what may enter, what must be rejected, what becomes admitted material, what must become canonical, what may become fact,
what transition is legal, what failure is declared, what evidence remains, and what may be published.

Some parts are strongly contract-shaped:

```text
boundary
guard / admission judgment
declared failure
admitted material
canonicalization rule
lowering obligation
invariant
state
state transition
accepted immutable fact
publication judgment
diagnostic evidence
policy / budget / capacity
```

Some parts are usually presentation:

```text
DTO
raw request shape
frontend type name
interface surface
```

Some parts are usually implementation:

```text
algorithm
data structure
library
cache
thread
queue
scheduler
waiting mechanism
physical execution order
```

The line is not "is this step in the pipeline?" The line is whether changing it changes the declared obligation of the
software. If it changes what the software may accept, reject, assert, transition, fail, expose, or publish, it is
contract. If it only changes how the same obligation is realized, it is implementation.

That distinction matters. Without it, the contract becomes polluted with backend choices, and the backend starts
pretending to be the contract.

---

## 10. Contract and Implementation

Contract and implementation must not be mixed.

Algorithms, time complexity, space complexity, hardware details, libraries, data structures, queues, threads, caches,
and scheduling tricks do not belong inside the contract definition. They may matter a lot for the machine, but they are
not the contract itself.

The contract and implementation should move together like mirror images on different axes. They correspond, but they are
not the same thing.

The core should contain obligations that do not depend on implementation. The implementation should realize those
obligations from the back side, isolated enough that it can be replaced when the contract does not change.

If implementation changes the meaning, then the implementation was allowed to leak into the contract. That is exactly
the kind of debt this theory is trying to avoid.

---

## 11. Message, Exposure, and Interaction

What the user sees and what the system uses to communicate internally should go through contracts.

If systems interact through implementation details, the contract gets mixed with implementation. That becomes debt.
Later, when you want to replace the implementation, you cannot do it freely because other parts of the system have
learned the implementation instead of the contract.

The user should know the contract. The rest of the system should communicate through contracts. Implementation should
work behind the contract like a shadow. It can be powerful, complicated, and optimized, but it should not become the
surface of interaction.

This is the point of the separation:

```text
user sees contract
system communicates through contract
implementation realizes contract behind the surface
```

If communication needs an implementation detail to make sense, the design is already leaking.

---

## 12. Whole Machine

A whole machine is made of pipelines.

Those pipelines should flow downstream. They should not backflow, loop around, or create cycles inside the contract
pipeline. A logical contract pipeline has no business reversing direction. If you build cycles into the actual machine,
you are basically recreating callback-riddled object circulation with a nicer name. No thanks.

Because machines have limits, processing order exists. Some pipelines may need results from other pipelines before they
can continue. Waiting for those results, scheduling that work, and managing that dependency are implementation concerns.

The contract pipeline says what must be true in the logical flow. The implementation decides how to run the machine
without violating it.

For now, the rough rule is:

```text
contract pipeline:
    logical, causal, downstream

implementation:
    scheduling, waiting, execution, physical coordination
```

This needs more work later.

---

## 13. Still Left

There is a lot left.

Later sections will handle the parts I am not finishing here:

```text
type as contract presentation name
class as frontend presentation
object as temporary frontend/runtime instance
object identity is not contract identity
inheritance is not contract reuse
subtyping is not contract preservation
overriding is not contract modification
overloading is not contract reuse
function value is not contract material
closure is not contract flow
thunk is not accepted fact
lazy evaluation hides cost
callback hides control flow
proxy hides boundary
recursion hides machine state
```

Still left.

---

## 14. Current Working Definition

For now:

```text
A contract is the declared set of obligations software must satisfy.
```

And the machine I want is simple in spirit:

```text
state the contract
make the interface a real contract document
keep contract structure two-dimensional
bind closed base contracts through a flat interaction manifest
admit only what passes the boundary
produce immutable facts
allow only declared transitions
reject through declared failures
publish only accepted results
declare failure instead of hiding it
declare resource limits instead of pretending they do not exist
verify realization against the declared contract
keep implementation out of the contract
```

That is the direction.

Still left.