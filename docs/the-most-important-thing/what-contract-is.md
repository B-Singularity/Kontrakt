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

## 1. The Explicit Machine

The good machine I am describing is not a magical new invention.

Most serious architecture already moves in this direction: boundaries, ports, states, transitions, schemas, invariants,
policies, failures, diagnostics, versioning, publication rules, and resource limits. They are already inside the system.
The real problem is that modern software learned to worship implicit structure.

Mathematical elegance made recursive forms look noble. Object-oriented programming made inheritance and subtyping look
like natural reuse. Frameworks made proxies, interception, reflection, and runtime decoration look harmless. Callbacks
and
dynamic dispatch made hidden control flow feel normal.

So contracts did not merely get hidden in framework defaults, mutation, exceptions, tests, and configuration files.

Those are symptoms.

The deeper disease is that implicitness became a design foundation.

That is the part I reject.

So this work is not about inventing contracts from thin air. It is about dragging the contracts that already control the
machine into visible software material.

A good machine should be explicit about the things that decide its behavior: what may enter, what must be rejected,
which state exists, which transition is legal, which failure is declared, which result may be published, which evidence
remains, which policy is active, and which limits the machine must respect.

If these things are not explicit, they do not disappear. They become hidden rule-makers. That is where software turns
into bullshit. Not because the machine has no contracts, but because the contracts are implicit and everyone pretends
the
implementation is innocent.

The surface should still be simple. I am not trying to make developers write legal paperwork in code. That would be
another kind of stupidity. The point is to make the visible surface small, readable, and ordinary, while the machine
keeps
enough explicit contract material to verify, lower, diagnose, and govern the system.

The goal is not more code.

The goal is less hidden meaning.

---

## 2. Mathematics, Physics, and Engineering

Mathematics, physics, and engineering do not play the same game. People mix them together all the time, and a lot of
programming bullshit starts exactly there.

Mathematics is allowed to start from axioms, define objects, prove theorems, and live inside formal consistency. Inside
mathematics, that is fine. Mathematics can search for formal truth. It can reduce a whole system to one primitive symbol
if it wants. Good for mathematics.

But that does not mean a single fucking thing for engineering.

A formal system being consistent does not mean it is a good machine. A formal reduction being possible does not mean it
should be used. A beautiful proof does not pay for failure modes, debugging, operational cost, or the poor bastard who
has to maintain the thing at 3 a.m.

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

A physical theory is not truth itself. It is a useful approximation that survives contact with the world. No amount of
beauty saves a theory that fails against reality. Reality does not give a shit about elegance.

Engineering is physics with a stronger purpose. It asks whether we can build the machine, whether it does the job,
whether the cost is acceptable, whether failure can be diagnosed, whether it runs under real constraints, whether it can
be maintained, and whether people can actually use it.

If the answer is no, I do not care how elegant the theory is. Call it mathematics. Do not call it good engineering.

---

## 3. Use Mathematics. Do Not Become Mathematics.

Mathematics is useful. Use it as a tool. Use it as a language. Use it to measure, describe, constrain, and reason. But
when the job is to build a machine, do not turn engineering into pure mathematics.

This is the part that keeps annoying me in programming. Again and again, people take a mathematical shape and smuggle it
into engineering as if the machine owes it obedience. Formal systems, combinators, type theory, higher-order functions,
lazy evaluation, inheritance hierarchies, subtyping games, callback hell, proxy magic. Some of these are interesting as
mathematics. Some of them are useful in narrow places. But when they become the foundation of how we build machines,
things often get ugly.

Beautiful on paper. Fucking miserable in real machines.

If something is so elegant, then where is the good machine? If nobody uses it because it is slow, opaque, hard to debug,
hard to operate, and hostile to real constraints, then what exactly did we build? A mathematical artifact, maybe. Not
good engineering.

I am not interested in turning software into a shrine for formal elegance. I am interested in a machine that works.

---

## 4. Contract

```text
A contract is the declared set of obligations software must satisfy.
```

That is the definition I am using.

This definition is intentionally not tied to class, object, type trick, framework, or implementation style. A contract
is
not what a class looks like. It is not what an object happens to contain. It is not what a type theorist can encode with
enough clever machinery.

A contract is the declared obligation.

But contract structure has a trap. If contracts can inherit other contracts, compose other contracts, include other
contracts, and then those contracts do the same thing again, the whole thing becomes genealogy. It becomes the same old
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
policy / budget / capacity / governance contract
```

The second dimension contains interaction manifests. A manifest does not inherit contracts. It does not compose
interfaces. It does not pull in another manifest, which pulls in another manifest, which pulls in another one until
nobody knows what the hell is actually required.

It binds a flat list of closed base contracts for one interaction.

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
    -> policy / budget / capacity / governance contract
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

I do not trust you. The compiler should stop you.

---

## 5. Make Interfaces Great Again

Software needs a way to present contracts. One important surface is the interface.

```text
Interface:
    the software-visible contract presentation for interaction.
```

An interface is not an implementation skeleton. It is not a class without fields. It is not a naive method list. It is
the
software-visible contract document for interaction.

The sad thing is that modern interfaces usually do not do this. They are mostly weak method shells. They say almost
nothing. A method list says, "this operation shape exists." That is barely a contract document.

But the method surface should not be thrown away. The old JVM interface method gives users something familiar:

```kotlin
interface OrderPort {
    fun submit(command: SubmitOrderCommand): SubmitOrderResult
}
```

This shape is useful. It gives the operation name, input presentation, output presentation, and a familiar surface for
implementation. If we remove that, the system becomes annoying to use. Nobody wants a contract theory that makes
software
feel like filing taxes.

So the method remains.

But the method is not the contract.

And the call is not the contract either.

That distinction matters because object-oriented programming trained people to confuse method calls, callbacks, virtual
dispatch, and contract meaning. A method call is an implementation-level invocation mechanism. A callback is an
object-oriented control-flow trick. These things became so common that people started treating them like the natural
shape of software contracts.

They are not.

```text
Method:
    the operation selector and presentation handle of an interface contract.
```

A method signature is only the weakest shell of an operation contract. It tells us the operation name, the input
presentation, and the output presentation. It does not tell us what the input must satisfy, what admission means, which
state allows the operation, what fact is produced, what failure is declared, what may be published, what diagnostic
evidence remains, or which policy, budget, capacity, and governance rules apply.

For one method, the operation manifest is still flat:

```text
submit(...)
    -> input contract
    -> admission contract
    -> lowering contract
    -> fact contract
    -> invariant contract
    -> state machine contract
    -> failure contract
    -> publication contract
    -> diagnostic contract
    -> policy / budget / capacity / governance contract
```

Do not turn methods into another genealogy. No method inheritance as contract meaning. No overload as contract reuse. No
default method as hidden contract behavior. The method is the handle. The manifest is the contract. The call is just how
one implementation path may enter the operation.

Do not confuse the handle with the contract.

Do not confuse the call with the contract.

Do not confuse callback-shaped control flow with contract flow.

Do not remove methods.

Make methods stop pretending they are enough.

Make interfaces great again.

---

## 6. Contract and Verification

If the interface is the contract document, verification is the check that a realization satisfies the declared
obligations.

Verification does not mean "tests" by default. A test is only one form of verification, and usually a late and limited
one. A test does not create quality. It does not make bad software good. It only checks whether the checked case appears
to satisfy the contract.

If the contract is rich enough, some verification should happen before tests. A compiler, a contract compiler, a
verifier, or generated checks should be able to resolve part of it earlier. The better the contract document is, the
less
we need to guess through scattered tests.

TDD sold one ordinary fact like it had discovered fire:

```text
if you know the contract before the implementation,
you can write the verification before the implementation.
```

Fine. That part is useful. But TDD is not a theory of quality. It is not a replacement for contract. It is just writing
one verification document before the realization. If the contract is explicit enough, that verification should be
derived from the contract as much as possible, not manually guessed into existence.

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

---

## 7. Evolution and Contract

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

## 8. Good Machine and Function

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

## 9. Core, Boundary, and the Fucking Bastards Outside

There is one thing worth taking from object-oriented programming: disciplined separation.

Not inheritance. Not subtype games. Not callback-shaped control flow. Most of that can go to hell. But the instinct to
split responsibilities, keep things apart, and group what belongs together is useful. A real machine needs that.

Once a machine has a logical pipeline, the space between input and output cannot stay as one magical blob. It naturally
breaks into stages. And once there are stages, there are boundaries.

A stage is not just code. It has an outside and an inside. It has material it receives, judgment it performs, and
material
it is allowed to pass forward. In that sense, each unit pipeline has a boundary and a core.

The core is where the declared contract must hold.

The boundary is where outside material is inspected, judged, rejected, or lowered into something the core is allowed to
understand.

Why so strict?

Because there are fucking bastards outside the boundary.

Users do not use programs the way you hoped. Some users send garbage by accident. Some send garbage because they are
careless. Some send garbage because they are trying to break the machine.

Attackers are worse. They inject strange input, exploit ambiguity, abuse serialization, forge shape, poison state, and
look for every tiny crack between what the program accepts and what the program actually understands.

Fine. Everyone knows that part.

The more annoying part is that users and attackers are not the only fucking bastards outside the boundary.

Frameworks, libraries, proxies, bytecode agents, reflection tools, serializers, runtime hooks, build plugins, and
instrumentation systems can also mutate what the machine thinks it received. They intercept calls, wrap objects, rewrite
bytecode, fake types, decorate behavior, delay execution, and smuggle implementation tricks into places where people
start treating them as facts.

A lot of modern software depends on these things without thinking. It is like a latent infection. Everything looks fine
until one dependency, one proxy, one bytecode trick, or one hidden runtime convention changes the meaning of what
crossed
the boundary.

So the core must not trust material just because it arrived through a familiar API. It must not trust material just
because a framework handed it over. It must not trust material just because a library says it is already shaped.

Everything outside the core is untrusted until the boundary has judged it.

This also applies to contracts.

One of the easiest ways to poison a core is to accidentally drag an external contract into it. A language-library
interface, framework interface, proxy interface, persistence interface, serialization interface, or test-tool interface
can look harmless because it is already typed and familiar. That is the trap. The moment the core starts depending on
that external interface as if it were its own contract, the outside has entered the inside.

That is external contract infiltration.

You cannot build everything from zero. That would be stupid. External libraries and frameworks are useful. But their
interfaces must not be accepted as core contracts just because they exist. They must be judged, mapped, and adopted only
when they match the internal contract you actually meant to declare.

The rule is not:

```text
This framework gives me an interface, so I will use it as my contract.
```

The rule is:

```text
This external interface may be used only if it can be ratified against my internal contract.
```

If it matches, adopt it explicitly. If it does not match, isolate it behind a boundary. If it changes the obligation, do
not pretend nothing happened. Your contract has been touched.

External dependencies can change the meaning of your system without asking permission. A library update, framework
proxy, generated adapter, bytecode agent, serializer rule, reflection convention, or default method can quietly bend the
contract away from your intention. Remember that. Dependencies are not just code you call. They can be foreign contracts
trying to move into your core.

That is why boundary work is not decoration. It is contract work.

For now, the important rule is simple:

```text
Never use outside material directly inside the core.
Never import an external contract as a core contract without ratification.
```

Judge it. Reject it if it fails. Lower it if it passes. Adopt it only if it is coherent with the contract you declared.

If the core accepts outside material as-is, the boundary is fake. If the core accepts external interfaces as its own
contract without judgment, the contract is already infected.

---

## 10. Applying Contract to a Good Machine

Now I want to apply the contract definition to a good machine and ask: what counts as contract inside that machine?

The interface names the interaction contract at the software surface. The method names the operation surface inside that
interface. When that contract is applied to a good machine, the operation cannot remain a flat invocation shape. It
unfolds into a causal flow.

```text
interface:
    contract document at the surface

method:
    operation handle

pipeline:
    explicit causal shape needed to satisfy the operation contract inside the machine
```

From a purely functional view, we may care only about stable input and stable output. The middle can be ignored. But
that
is not enough for a real machine. A machine does not get to skip the middle. The middle is where admission, rejection,
lowering, state, failure, evidence, and publication actually happen.

A good machine should not hide causal flow inside nested calls, closures, lazy thunks, higher-order function tricks, or
callback-shaped control. Maybe those are fine for some formal model. They are not the machine I want.

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

Policy, budget, capacity, and governance are not one naive stage at the end. They cut across the whole flow:

```text
policy / budget / capacity / governance:
    applies across boundary, admission, execution, failure, publication, and diagnostic
```

This is still a working shape. Each step needs its own explanation later.

---

## 11. What Counts as Contract in the Pipeline

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
policy / budget / capacity / governance
```

Some parts are usually presentation:

```text
DTO
raw request shape
frontend type name
interface surface
method signature
```

But even this split is not the real law.

The real line is whether changing it changes the declared obligation of the software. If it changes what the software
may accept, reject, assert, transition, fail, expose, retain, diagnose, or publish, it is contract. If it only changes
how the same obligation is realized, it is implementation.

Contract and implementation must be kept on different axes. They can correspond, like mirror images, but they must not
mix. The contract names the obligation. The implementation realizes it.

As long as the declared obligation is preserved, the implementation should be replaceable. If replacing the realization
changes the contract, the realization was allowed to leak into the contract. That is not architecture. That is debt.

The contract must not name the mechanism.

The contract must name the obligation.

Do not contract the tool.

Contract the obligation the tool must satisfy.

---

## 12. Contract Presentations in the Pipeline

Each contract kind in the pipeline needs its own explanation. Do not collapse them into one magic blob called
"contract." That is how people smuggle confusion back in.

Input, admission, lowering, fact, invariant, state machine, failure, publication, diagnostic, policy, budget, capacity,
and governance contracts are not the same thing. They may appear together in one operation manifest, but they do
different work.

Before DTOs and admission, the cross-cutting contracts have to be named. Otherwise people will pretend the machine
judges
material in empty space. It does not.

### 12.1 Policy, Budget, Capacity, and Governance Contracts

A good machine is not infinite.

It has limits.

It has a lifetime.

It can accept only so much material, spend only so much effort, retain only so much evidence, expose only so much
result,
and survive only under conditions it can actually bear. Pretending otherwise is not elegance. It is engineering fantasy.

So a good machine must be honest about its own limits.

It must declare them.

It must measure them.

It must operate under them.

A machine that does not know its limits will still hit them. It will just hit them by accident, under pressure, in the
worst possible place, while everyone pretends the contract was fine.

That is bullshit.

Policy, budget, capacity, and governance exist because the machine is finite and must be operated wisely. They are not
naive extra stages at the end of the pipeline. They cut across boundary, admission, lowering, core judgment, failure,
publication, and diagnostic.

```text
Policy Contract:
    the contract that declares which judgment criteria are active for a given machine context

Budget Contract:
    the contract that declares the finite consumable allowance of an operation, run, stage, or diagnostic path

Capacity Contract:
    the contract that declares the admissible envelope of a machine, surface, stage, or storage region

Governance Contract:
    the contract that declares how contract sets, policy sets, budget profiles, capacity envelopes, versions, and
    manifest bindings become valid for a machine
```

Policy is about judgment criteria.

It says under which declared criteria material may be accepted, rejected, deferred, failed, published, exposed,
retained,
or hidden. Policy is not configuration. Configuration may select a policy, but the selected policy must be declared
contract material. Policy is not a callback, not an arbitrary function, and not hidden behavior wearing a nicer name.

A policy must be finite, named, inspectable, governed, and explicitly bound to the interaction or surface where it
applies. If it cannot be named, inspected, versioned, and governed, it is not contract. It is behavior hiding behind a
nicer word.

Budget is about finite consumption.

It says how much a run, operation, stage, or diagnostic path may consume before the machine must stop, reject, defer, or
declare failure. The contract declares the allowance and the required outcome when the allowance is exhausted.

Capacity is about the machine's admissible envelope.

It says how much a machine, surface, stage, or storage region may accept, retain, keep in flight, expose, or publish.
Capacity should not be guessed from optimism. It should be measured, chosen, declared, and governed. Valid material may
still be rejected or deferred by capacity. That is not a bug. That is a finite machine refusing to lie about what it can
survive.

Governance is about validity of the contract world.

It says which contract set, policy set, budget profile, capacity envelope, version, and manifest binding is valid for a
machine. Without governance, nobody knows which rules are actually active. That is how systems quietly rot.

These contracts declare judgment criteria, finite allowance, admissible envelope, and validity.

They do not declare mechanisms.

The contract does not say how the machine stores work, schedules work, counts work, or physically enforces the limit.
Those are realization choices. The contract says what limit exists, where it applies, under which governance it is
valid,
and what declared outcome follows when the limit is reached.

A good machine does not pretend to be infinite.

It declares its limits and operates inside them.

### 12.2 Fact Contract and Immutable Fact

An immutable fact must be described carefully.

Do not say it is just ordinary data. That is too weak.

Do not say it is the contract rule either. That is also wrong.

An immutable fact is not a constraint, not an action rule, not a state transition rule, not a publication rule, and not
a
policy. Those are contract obligations.

An immutable fact is contract-governed factual material inside the core.

```text
Fact Contract:
    the contract that defines what kind of factual material may exist inside the core

Immutable Fact:
    the immutable factual material that exists under that fact contract
```

A constraint contract may say that an amount must not exceed a limit. The immutable fact says what the amount is.

A state machine contract may say which transition is legal. The immutable fact provides factual material that the
transition judgment may inspect.

A publication contract may say what may be exposed. The immutable fact provides factual material that the publication
judgment may inspect.

So the fact must stay dumb.

It must not decide.

It must not validate itself.

It must not carry the rule.

It must not hide behavior through methods, callbacks, proxies, inherited behavior, or framework bullshit.

It is factual material governed by contract: fixed, referable, comparable, and usable by later contract reasoning.

The fact contract defines the laws for that material: shape, identity, version, reference, and immutability. It may also
define the law that invalid factual material is not allowed to exist inside the core.

But do not put constructors into this contract. Do not put factories, builders, guard functions, validation algorithms,
storage layout, or object lifecycle tricks into this contract. Those are realization mechanisms.

The contract only says what kind of factual material the core is allowed to treat as fact. How the machine prevents
invalid material from being born is implementation.

The short rule is this:

```text
Fact Contract is contract.
Immutable Fact is contract-governed factual material.
```

Keep those two apart, or the whole thing turns back into object-oriented mud.

---

## 13. Contract and Implementation

Contract and implementation must not be mixed.

This must be said carefully, because implementation is not unimportant. A real machine still needs concrete machinery to
run. But that machinery belongs on a different axis.

Contract and implementation move together like mirror images on different axes.

The contract declares what must be true.

The implementation realizes it.

They correspond, but they must not mix.

If the contract says that a result must be deterministic, the implementation may use any machinery that preserves that
obligation.

If the contract says that a stage has a capacity limit, the implementation may realize that limit in many different
ways.

If the contract says that a failure must be declared, the implementation may choose how to detect and report it.

The contract must not name the mechanism.

The contract must name the obligation.

That is the line.

As long as the declared obligation is preserved, the implementation should be replaceable. If replacing the mechanism
changes the contract, then the mechanism was allowed to leak into the contract. That is not architecture. That is debt.

So do not drag implementation machinery into the contract document.

Do not contract the tool.

Contract the obligation the tool must satisfy.

---

## 14. Message, Exposure, and Interaction

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

## 15. Whole Machine

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
    physical coordination behind the contract
```

This needs more work later.

---

## 16. Still Left

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
method signature is not the full operation contract
external interface is not core contract
external dependency can mutate contract meaning
function value is not contract material
closure is not contract flow
thunk is not accepted fact
lazy evaluation hides cost
callback hides control flow
proxy hides boundary
recursion hides machine state
DTO and raw request shape
admission judgment and declared failure
canonicalization and lowering
publication and diagnostic evidence
```

Still left.

---

## 17. Current Working Definition

For now:

```text
A contract is the declared set of obligations software must satisfy.
```

And the machine I want is simple in spirit:

```text
state the contract
make hidden obligations explicit
make the interface a real contract document
preserve methods as operation handles
keep contract structure two-dimensional
bind closed base contracts through a flat operation manifest
treat everything outside the core as untrusted
adopt external interfaces only after ratifying them against internal contracts
admit only what passes the boundary
produce contract-governed immutable facts
allow only declared transitions
reject through declared failures
publish only accepted results
declare failure instead of hiding it
declare policy, budget, capacity, and governance instead of hiding them
verify realization against the declared contract
keep implementation out of the contract
```

That is the direction.

Still left.