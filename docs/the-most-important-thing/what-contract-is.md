# What Is Contract

## 0. Intention

The most important thing in software is intention. Not class, not object, not type theory, and not an abstract model
that ignores the machine it has to run on.

I am describing the contract model that fits my intention.

It will not fit every software system, every team, or every engineering goal. The limitation is intentional. A model
that pretends to fit everything usually says nothing useful.

I am trying to say what kind of machine I want to build, and why the machine has to be shaped this way.

Software runs under limits. It receives bad inputs, broken environments, vague requirements, strange users, and machines
that do exactly what you told them, not what you hoped they would understand. So the question
is not, "Is this theory beautiful?" The question is, "Does this help build a machine that works for the purpose I
declared?"

```text
I am not writing a model for every possible software system.
I am writing the shape of the machine I want to build.
```

If the machine does not fit the purpose, the theory is useless. If the theory is elegant but the machine is garbage,
then
for engineering that theory is garbage too.

The document stays inside that scope.

---

## 1. The Explicit Machine

The good machine I am describing is not meant to be a universal machine for every context.

Most serious architecture already moves in this direction. Boundaries, ports, states, transitions, schemas, invariants,
policies, failures, diagnostics, versioning, publication rules, and resource limits are already inside real systems.

The problem is that modern software made implicit structure look normal.

Mathematical elegance made recursive forms look noble. Object-oriented programming made inheritance and subtyping look
like natural reuse. Frameworks made proxies, interception, reflection, and runtime decoration look harmless. Callbacks
and dynamic dispatch made hidden control flow feel normal.

Framework defaults, mutation, exceptions, tests, and configuration files are only places where the hidden contract shows
up. The deeper problem is that implicitness became a design foundation.

Here, the contracts already controlling the machine should become visible software material.

A good machine should be explicit about the things that decide its behavior: what may enter, what must be rejected,
which
state exists, which transition is legal, which failure is declared, which result may be published, which evidence
remains, which policy is active, and which limits the machine must respect.

When these things are not explicit, they do not disappear. They become hidden rule-makers. The result is bullshit: the
machine has contracts, but they are implicit, and everyone pretends the implementation is innocent.

The surface should still be simple. Making developers write legal paperwork in code would miss the point. The visible
surface should be small, readable, and ordinary, while the machine keeps enough
explicit contract material to verify, lower, diagnose, and govern the system.

The goal is not more code. The goal is less hidden meaning.

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
when the job is to build a machine, engineering should not become pure mathematics.

The annoying pattern in programming is simple: people take a mathematical shape and smuggle it into engineering as if
the machine owes it obedience. Formal systems, combinators, type theory, higher-order functions,
lazy evaluation, inheritance hierarchies, subtyping games, callback hell, proxy magic. Some of these are interesting as
mathematics. Some are useful in narrow places. When they become the foundation of how we build machines, things often
get ugly.

It may look beautiful on paper and still be fucking miserable in a real machine.

If a design is slow, opaque, hard to debug, hard to operate, and hostile to real constraints, its elegance has not built
a good machine. It has built a mathematical artifact, maybe, but not good engineering.

I do not want software to serve formal elegance. I want a machine that works.

---

## 4. Contract

```text
A contract is the declared set of obligations software must satisfy.
```

I use that definition here.

This definition is not tied to class, object, type trick, framework, or implementation style. A contract is not what a
class looks like. It is not what an object happens to contain. It is not what a type theorist can encode with enough
clever machinery.

A contract is the declared obligation.

A trap appears here. If contracts inherit other contracts, compose other contracts, include other contracts, and those
contracts do the same thing again, the whole structure turns into genealogy. It is the same family tree under a cleaner
name.

That structure does not clarify contracts. It gives inheritance better branding.

So contract structure has to stay two-dimensional.

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
nobody knows what is actually required.

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
state its own states and legal transitions and stop there. It should not inherit another state machine. It should not
compose another state machine.

The model rejects these shapes:

```text
interface inheritance
interface composition
same-kind contract inheritance
same-kind contract composition
recursive manifest
hidden transitive obligation
cyclic contract reference
```

A manifest is a flat table, not genealogy. If one interaction requires five ancestors to understand it, the contract
document has already failed. The same structure returned under a different name.

Convention is not enough here.

And no, I do not want this written as polite advice.

Inheritance is exactly the wrong instinct here. The moment two contracts look similar, people will try to pull out a
parent, move the common bits upward, let the child override the difference, and then call the mess "reuse." I hate that
shape. It is how simple obligations turn into ancestry, exception rules, and hidden meaning.

A contract is not a bloodline.

A manifest is not a family tree.

If contract inheritance is allowed, people will use it. If recursive contract composition is allowed, people will build
it. Not because it is correct, but because the habit is already burned into their hands.

So this cannot be a style guide.

Contract inheritance and recursive contract composition must be rejected by the compiler.

Not discouraged. Rejected.

The compiler should stop the shape before anyone gets clever with it.

---

## 5. Make Interfaces Great Again

Software needs a way to present contracts. One important surface is the interface.

```text
Interface:
    the software-visible contract presentation for interaction.
```

An interface is not an implementation skeleton, not a class without fields, and not a naive method list. It is the
software-visible contract document for interaction.

Modern interfaces usually fail at this. They are mostly weak method shells. A method list says, "this operation shape
exists." A method list barely counts as a contract document.

Still, the method surface should not be thrown away. The old JVM interface method gives users something familiar:

```kotlin
interface OrderPort {
    fun submit(command: SubmitOrderCommand): SubmitOrderResult
}
```

The shape is useful. It gives the operation name, input presentation, output presentation, and a familiar surface for
implementation. Remove that and the system becomes annoying to use. Nobody wants a contract theory that makes ordinary
use harder than the problem it was meant to solve.

So the method remains.

But the method is not the contract, and the call is not the contract either.

Object-oriented programming trained people to confuse method calls, callbacks, virtual dispatch, and contract meaning. A
method call is an invocation mechanism. A callback is a control-flow trick. They became so common that people started
treating them like the natural shape of software contracts.

They are not contract meaning.

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

Methods should not become another genealogy. Method inheritance must not become contract meaning, overloads must not
become contract reuse, and default methods must not become hidden contract behavior. The method is the handle. The
manifest is the contract. The call is just how
one implementation path may enter the operation.

Keep the method, but make it stop pretending to be enough. Make interfaces great again.

---

## 6. Contract and Verification

If the interface is the contract document, verification is the check that a realization satisfies the declared
obligations.

The important part is not "tests." Tests are only one late way to look at a machine after it already exists.

The better direction is earlier than that.

First make the contract material rich enough to say what the machine actually promises. The closed base contracts name
the obligations: input, admission, lowering, fact, invariant, state machine, failure, publication, diagnostic, policy,
budget, capacity, and governance. The manifest binds those closed contracts to one interaction without inheritance,
composition, or hidden ancestry.

That gives the compiler something real to guard.

Not a vague method name.

Not a class shape.

Not a comment.

A declared contract surface.

If the implementation claims to realize an interaction, the compiler or verifier should be able to ask ordinary
questions before the machine turns into runtime soup:

```text
Does the implementation expose the required operation handle?
Does it accept only the declared input presentation?
Does it apply the required admission contract?
Does it produce the declared failure outcomes?
Does it preserve the declared invariant?
Does it obey the declared state transitions?
Does it publish only what the publication contract allows?
Does it stay under the active policy, budget, capacity, and governance contracts?
Does it produce the diagnostic evidence the contract requires?
```

That is the point of making the one-dimensional contracts and the manifest explicit. The compiler cannot guard meaning
that was never declared. If the contract surface is too thin, the compiler has nothing to hold. Then people compensate
with tests, conventions, annotations, comments, and hope.

Hope is not a verification strategy.

A test can still be useful. It can check concrete behavior. It can exercise examples. It can catch mistakes in a
realization. But a test should not be the first place where the contract becomes visible.

The order should be:

```text
closed base contracts
-> flat interaction manifest
-> compiler / verifier / generated checks where possible
-> tests where needed
-> realization
```

TDD sold one ordinary fact like it had discovered fire:

```text
if you know the contract before the implementation,
you can write the verification before the implementation.
```

Fine. That part is useful. But TDD is not a theory of quality. It is just one way to write some verification before the
realization. If the contract document is explicit enough, much of that verification should be derived from the contract,
not manually guessed into existence.

The best verification is not a mountain of tests. The best verification is making invalid software impossible to write,
impossible to compile, or impossible to publish before it becomes a runtime mess.

DBC was not wrong because it wanted contracts. It went wrong because it glued contracts onto the wrong substrate:
classes, inheritance, runtime wrappers, proxies, and implementation-shaped objects. Of course that became slow and ugly.
Of course people stopped using it.

The answer is not to scatter contracts into comments, wiki pages, assertions, and test fragments. The answer is to make
the contract document real enough that the compiler can guard implementation against it.

---

## 7. Evolution and Contract

Contracts should be stable and immutable. If every small implementation change rewrites the contract, there is no
contract. There is only noise.

But stability is not the highest law. Evolution comes before immutability. If the system must evolve, the contract may
need to change. Freezing the contract just to worship immutability turns
engineering into another ritual.

For now:

```text
Contracts should remain stable unless the system evolves.
When the obligation changes, the contract changes.
When only the realization changes, the contract should not change.
```

The full versioning story is still left.

---

## 8. Good Machine and Function

A good machine should ideally behave like a function. Given the same accepted input, it should produce the same accepted
output. The clean target is still function-like behavior.

But that perfect fantasy does not exist in the real world.

A real machine cannot be treated as a pure mathematical function. It has memory, time, failure, capacity, latency,
storage, network,
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

A good machine cannot be a fantasy function. It is a function-like system that admits failure, cost, and damage.

---

## 9. Core, Boundary, and the Fucking Bastards Outside

There is one thing worth taking from object-oriented programming: disciplined separation.

Not inheritance. Not subtype games. Not callback-shaped control flow. Most of that can go to hell. But the instinct to
split responsibilities, keep things apart, and group what belongs together is useful. A real machine needs that.

Once a machine has a logical pipeline, the space between input and output cannot stay as one magical blob. It breaks
into
stages. Once there are stages, there are boundaries.

A stage is not just code. It has an outside and an inside. It receives material, performs judgment, and passes forward
only what it is allowed to pass. Each unit pipeline has a boundary and a core.

The core is where the declared contract must hold.

The boundary is where outside material is inspected, judged, rejected, or lowered into something the core is allowed to
understand.

The boundary has to be strict because there are fucking bastards outside it.

Users do not use programs the way you hoped. Some send garbage by accident. Some send garbage because they are careless.
Some send garbage because they are trying to break the machine.

Attackers are worse. They inject strange input, exploit ambiguity, abuse serialization, forge shape, poison state, and
look for every tiny crack between what the program accepts and what the program actually understands.

Users and attackers are not the only fucking bastards outside the boundary.

Frameworks, libraries, proxies, bytecode agents, reflection tools, serializers, runtime hooks, build plugins, and
instrumentation systems can also mutate what the machine thinks it received. They intercept calls, wrap objects, rewrite
bytecode, fake types, decorate behavior, delay execution, and smuggle implementation tricks into places where people
start treating them as facts.

A lot of modern software depends on these things without thinking. Everything looks fine
until one dependency, one proxy, one bytecode trick, or one hidden runtime convention changes the meaning of what
crossed
the boundary.

So the core must not trust material just because it arrived through a familiar API, because a framework handed it over,
or because a library says it is already shaped.

Everything outside the core is untrusted until the boundary has judged it.

The same goes for contracts.

One of the easiest ways to poison a core is to accidentally drag an external contract into it. A language-library
interface, framework interface, proxy interface, persistence interface, serialization interface, or test-tool interface
can look harmless because it is already typed and familiar. The trap appears when the core starts depending on that
external interface as if it were its own contract. At that
moment, the outside has entered the inside.

I call this external contract infiltration.

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
contract away from your intention. Dependencies are not just code you call. They can be foreign contracts
trying to move into your core.

Boundary work is contract work.

For now:

```text
Never use outside material directly inside the core.
Never import an external contract as a core contract without ratification.
```

Judge it. Reject it if it fails. Lower it if it passes. Adopt it only if it is coherent with the contract you declared.

If the core accepts outside material as-is, the boundary is fake. If the core accepts external interfaces as its own
contract without judgment, the contract is already infected.

---

## 10. Applying Contract to a Good Machine

Now apply the contract definition to this machine.

The interface names the interaction contract at the software surface. The method names the operation surface inside that
interface. Once the operation enters a real machine, it cannot remain a flat invocation shape. It unfolds into a causal
flow.

```text
interface:
    contract document at the surface

method:
    operation handle

pipeline:
    explicit causal shape needed to satisfy the operation contract inside the machine
```

From a purely functional view, we may care only about stable input and stable output. The middle can be ignored. A real
machine does not get to skip the middle. The middle is where admission, rejection, lowering, state, failure, evidence,
and publication actually happen.

A good machine should not hide causal flow inside nested calls, closures, lazy thunks, higher-order function tricks, or
callback-shaped control. Those forms may be fine for some formal model. They are not the machine I want.

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

The shape is still provisional. Each step needs its own explanation.

---

## 11. What Counts as Contract in the Pipeline

A pipeline does not automatically become a contract. A processing sequence is just a processing sequence until it
declares an
obligation.

The contract is what the software must satisfy at each meaningful point: what may enter, what must be rejected, what
becomes admitted material, what must become canonical, what may become fact, what transition is legal, what failure is
declared, what evidence remains, and what may be published.

Strongly contract-shaped material includes:

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

Some material is presentation:

```text
DTO
raw request shape
frontend type name
interface surface
method signature
```

Even this split is only a guide.

The real line is declaration.

A behavior does not become contract just because someone can observe it, depend on it, or break when it changes. That is
how duck typing, framework convention, accidental behavior, and test expectation sneak back in as implicit contracts.

A contract exists only when the obligation is declared as contract material and bound through the valid contract world:
a closed base contract, a flat interaction manifest, or governed contract metadata.

If a change alters a declared obligation, it is contract change.

If a change preserves the declared obligation and only changes realization, it is implementation change.

If a change breaks undeclared reliance, that reliance is compatibility debt, not contract authority.

Do not let observed behavior become contract by accident.

Contract and implementation stay on different axes. They can correspond, like mirror images, but they must not mix. The
contract names the obligation. The implementation realizes it.

As long as the declared obligation is preserved, the implementation should be replaceable. If replacing the realization
changes the contract, the realization leaked into the contract. At that point, the design has debt, not architecture.

The contract must not name the mechanism. It must name the obligation.

The tool is not the contract. The obligation the tool must satisfy is the contract.

---

## 12. Contract Presentations in the Pipeline

Collapsing every contract kind into one vague blob called "contract" only brings the old confusion back under a new
name.

The order here is intentional. First, describe what kind of factual material the core may treat as fact. Then describe
the finite machine conditions that govern judgment. After that, explain how outside presentation reaches the boundary,
how admission decides, what happens when the material is stopped or allowed to continue, and how admitted material
becomes stable enough for the core to own.

### 12.1 Fact Contract and Immutable Fact

An immutable fact needs a careful description.

Calling it ordinary data makes the concept too weak. Calling it the contract rule makes the concept wrong.

An immutable fact is not a constraint, action rule, state transition rule, publication rule, or policy. Those are
contract obligations. The fact is the factual material those obligations inspect.

```text
Fact Contract:
    the contract that defines what kind of factual material may exist inside the core

Immutable Fact:
    the immutable factual material that exists under that fact contract
```

Think of a constraint contract that says an amount must not exceed a limit. The immutable fact says what the amount is.
Think of a state machine contract that says which transition is legal. The immutable fact provides the factual material
that transition judgment may inspect. Think of a publication contract that says what may be exposed. The immutable fact
provides the factual material that publication judgment may inspect.

The fact stays dumb in this model. It must not decide, validate itself, carry the rule, or hide behavior through
methods,
callbacks, proxies, inherited behavior, or framework bullshit.

It is factual material governed by contract: fixed, referable, comparable, and usable by later contract reasoning.

The fact contract defines the laws for that material: shape, identity, version, reference, and immutability. It may also
define that invalid factual material is not allowed to exist inside the core.

Constructors, factories, builders, guard functions, validation algorithms, storage layout, and object lifecycle tricks
belong outside this contract. Those are realization mechanisms.

The contract says what kind of factual material the core may treat as fact. How the machine prevents invalid material
from being born is implementation.

```text
Fact Contract is contract.
Immutable Fact is contract-governed factual material.
```

Keeping those two apart prevents the old object-oriented mixture of rule, data, and behavior from coming back.

### 12.2 Policy, Budget, Capacity, and Governance Contracts

A good machine is finite. It has limits, and it has a lifetime.

It can accept only so much material, spend only so much effort, retain only so much evidence, expose only so much
result,
and survive only under conditions it can actually bear. Pretending otherwise is engineering fantasy.

A good machine must be honest about its own limits. It declares them, measures them, and operates under them.

A machine that does not know its limits still hits them. It will hit them by accident, under pressure, in the worst
possible place, while everyone pretends the contract was fine. The premise was false.

Policy, budget, capacity, and governance exist because the machine is finite and must be operated wisely. They are not
little stages tacked onto the end of the pipeline. They cut across boundary, admission, lowering, core judgment,
failure,
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

Policy is about judgment criteria. It says under which declared criteria material may be accepted, rejected, deferred,
failed, published, exposed, retained as evidence, or hidden. Policy is not configuration. Configuration may select a
policy, but the selected policy must be declared contract material. Policy is not a callback, not an arbitrary function,
and not hidden behavior wearing a nicer name.

A policy must be finite, named, inspectable, governed, and explicitly bound to the interaction or surface where it
applies. If it cannot be named, inspected, versioned, and governed, it is not contract. It is behavior hiding behind a
nicer word.

Policy axes may include boundary strictness, unknown material handling, external contract infiltration, capacity
overflow, budget exhaustion, diagnostic retention, publication exposure, version compatibility, duplicate or replay
handling, determinism requirement, degradation permission, priority, fairness, and trust requirement.

These axes are declared judgment criteria, not algorithms. The rule axis and the tool that later realizes it are
different things.

Budget is finite consumption. It says how much a run, operation, stage, or diagnostic path may consume before the
machine
must stop, reject, defer, or declare failure. The contract declares the allowance and the required outcome when the
allowance is exhausted.

Capacity is the machine's admissible envelope. It says how much a machine, surface, stage, or storage region may accept,
retain as bounded evidence, keep in flight, expose, or publish. Capacity should not be guessed from optimism. It should
be measured, chosen, declared, and governed. Valid material may still be rejected or deferred by capacity. A finite
machine can reject valid material when accepting it would exceed what the machine can survive.

Governance is the validity of the contract world. It says which contract set, policy set, budget profile, capacity
envelope, version, and manifest binding is valid for a machine. Without governance, nobody knows which rules are
actually
active. Systems quietly rot in that gap.

These contracts declare judgment criteria, finite allowance, admissible envelope, and validity. They do not declare
mechanisms.

The contract does not say how the machine stores work, schedules work, counts work, or physically enforces the limit.
Those are realization choices. The contract says what limit exists, where it applies, under which governance it is
valid, and what declared outcome follows when the limit is reached.

A good machine admits it is finite, declares its limits, and operates inside them.

### 12.3 DTO and Raw Presentation

Something has to arrive at the boundary. Whatever arrives there is not yet a core fact and not a contract rule. It is
just the form in which the outside world showed up.

Maybe it came as JSON. Maybe it came as a message. Maybe it came through a framework object with three layers of
annotations. The boundary still needs a shape it can look at.

That boundary-facing shape is the DTO.

A DTO is the boundary-facing presentation shape that makes outside material judgeable by the machine.

Without that shape, the machine is not judging material. It is trying to reason over arbitrary outside material.

The DTO gives the boundary something finite, named, and inspectable to judge. It is material presented to the airlock,
not material allowed to live inside the core.

```text
Input Contract:
    the contract that declares what presentation shape may appear at the boundary

DTO / Raw Presentation:
    the boundary-facing data shape presented for judgment
```

A DTO has no contract authority. It should not validate itself, decide, become a domain object, or carry framework
rules, serializer rules, persistence rules, proxy behavior, or external interface meaning into the core.

The DTO exists so the boundary can judge outside material under declared policy, budget, capacity, and governance.

In practice, a structurally valid DTO can still be rejected by policy. A meaningful DTO can still be deferred by
capacity. A malformed DTO can fail fast. A suspicious DTO may leave bounded diagnostic evidence if the diagnostic policy
allows it.

That last sentence needs discipline.

Rejected material does not get a second life just because somebody wants a debug trail. Keeping hostile material around
is not free. It consumes storage, leaks secrets, creates replay paths, gives review tools a dangerous appetite, and
tempts some later piece of code to treat rejected junk as if it were almost admitted.

So the contract should not introduce a special kind of "quarantined material" as if it were part of the pipeline.

The pipeline continues with admitted material.

Everything else stops.

What may remain is evidence, not authority.

Examples:

```text
fast-fail policy:
    reject invalid presentation immediately with declared failure

diagnostic-retention policy:
    retain only bounded, non-authoritative evidence from material that did not continue

diagnostic-only policy:
    allow evidence to remain visible for diagnosis, but never for authoritative core reasoning

reject-unknown policy:
    reject undeclared fields, metadata, shape, or external contract material

compatibility policy:
    allow legacy presentation only under a declared compatibility rule

capacity-overflow policy:
    reject or defer material when the declared capacity envelope cannot admit it

budget-exhaustion policy:
    stop, defer, or fail when the declared allowance is exhausted
```

Compressed:

```text
DTO is judgeable outside presentation.
It is not core fact.
It is not contract authority.
Rejected material may leave evidence.
It must not leave authority.
```

A DTO entering the core as-is means the boundary did no real work.

### 12.4 Guard and Admission Judgment

Once the boundary has a shape to look at, it has to decide whether the material may continue.

That decision is admission.

Admission is narrower than the domain. It is not where the machine solves the business, and it is not an excuse to drag
the entire core into the boundary. Admission is the airlock judgment. It asks a smaller question: can this presented
material move forward under the active input contract, policy, budget, capacity, and governance?

The guard is the place where the admission contract is applied; it is not the contract itself. If the guard becomes the
contract, the obligation has gone back into implementation code. The old problem returns with a better name.

```text
Admission Contract:
    the contract that declares when boundary presentation may be admitted, rejected, deferred, or failed

Admission Judgment:
    the contract-governed verdict produced at the boundary
```

Admission verdict and material disposition answer different questions.

```text
Admission Verdict:
    can this material continue through the pipeline?

Material Disposition:
    what declared handling applies to material that does not continue?
```

A useful top-level admission verdict is small:

```text
admitted
rejected
deferred
failed
```

`Admitted` means the boundary presentation may continue to normalization, canonicalization, and lowering.

`Rejected` means the material does not satisfy the boundary obligation.

`Deferred` means the material may be valid, but the machine cannot or must not process it now under declared capacity,
budget, policy, or governance.

`Failed` means the machine cannot safely complete the admission judgment itself: the active contract world is invalid,
required governance is missing, a required policy set is not valid, or the boundary cannot produce the evidence it is
obligated to produce.

Disposition is not another verdict. It is the declared handling for material that did not continue. The safe default is
simple: discard it.

If the machine keeps anything, it keeps bounded diagnostic evidence, not the material as a second pipeline.

```text
Material Disposition:
    discard
    retain bounded diagnostic evidence
    redact and expose summary
```

That is the line. Rejected material must not become a shadow pipeline. A review queue, dead-letter table, audit store,
debug file, replay buffer, or security workbench may be a useful implementation, but it is still implementation. The
contract only declares whether evidence may remain, what evidence may remain, how it is bounded, and what may be
exposed.

Admission decides whether material may continue. Disposition decides what remains after material does not continue. If
you put a retained blob beside admission as if it were another way forward, someone will eventually wire it back into
the machine. Do not give them the hole.

Admission failure should not disappear into a random exception, and a framework should not decide what failure means.
Admission failure is part of the contract.

The guard may be realized in many ways. The contract does not care. The contract only declares what the boundary must
judge, which verdicts are legal, and which dispositions may be applied to material that does not continue.

### 12.5 Declared Failure, Admitted Material, and Diagnostic Evidence

Admission has two honest directions.

The material is allowed to continue under a declared condition.

Or it is not.

When material cannot continue, the machine must produce a declared result for that path. Sometimes that result is
rejection. Sometimes it is deferral. Sometimes the admission judgment itself failed. They should not be collapsed into
one vague error bucket.

A declared failure is not a cleaned-up crash. It is not an unhandled exception. It is not whatever the framework
happened
to throw.

A declared failure is a contract-governed stop result. It states which obligation failed, under which input, admission,
policy, budget, capacity, or governance rule the failure was produced, and what may be exposed about that failure.

Failure must be declared because failure is part of the machine. A machine that hides failure is lying, and a machine
that throws failure into the void leaves the next stage to guess.

If the material is allowed to continue, it becomes admitted material.

Admitted material is not an immutable fact. It is not core truth. It is not yet canonical, not yet lowered, and not yet
something the state machine may freely reason from.

It is material that survived the boundary judgment and may continue to normalization, canonicalization, and lowering.

If the material does not continue, the material stops.

That does not mean the machine must remember nothing. It may need evidence for diagnosis, audit, security, rate-limit
enforcement, replay defense, or user-facing explanation. But evidence is not the rejected material continuing under a
different hat. Evidence is a bounded, non-authoritative remainder allowed only by declared policy.

```text
Declared Failure:
    contract-governed stop result

Admitted Material:
    boundary-admitted material that may continue toward canonicalization and lowering

Diagnostic Evidence:
    bounded, non-authoritative evidence retained under declared diagnostic policy from material that did not continue

Retention Policy:
    the policy that declares whether rejected or failed material may leave evidence, what may be retained, how it is
    bounded, and what may be exposed
```

Admitted material is not fact.

Declared failure is not an exception.

Diagnostic evidence is not admitted material.

Rejected material may leave evidence. It must not leave authority.

The boundary exists for a reason. Outside material must either continue under an admission verdict or stop under a
contract-governed outcome and disposition. It must not become a second pipeline just because a debug path, review path,
or storage mechanism exists.

### 12.6 Canonicalization Rule

Admitted material has passed the boundary. That is all.

It is not ready for the core yet.

The outside world is not stable enough to define the machine's identity. Even the same adapter can drift across
versions.
A JVM-facing source may expose slightly different text, Unicode shape, metadata order, reflection detail, or backend
material. A serializer, compiler backend, library update, or framework convention can move one tiny piece of
presentation
and still claim it is giving the same meaning.

The machine cannot trust that.

If the machine accepts external presentation as its standard, identity starts depending on whatever the outside happened
to produce today. The contract that was one thing yesterday becomes two things tomorrow. A fact gets a different handle.
A graph node changes shape. A diagnostic no longer points at the same material. Verification starts arguing with
representation noise instead of contract meaning.

That is not harmless variation. It is a threat to determinism.

This problem is not unique to this system. Databases meet it with keys, collation, and spelling. Compilers meet it when
different syntax carries the same meaning. Signature systems meet it when field order, whitespace, or byte encoding
changes the thing being signed. The details differ, but the machine problem is the same.

Same meaning must not split into many identities.

Different meaning must not be collapsed into one identity.

That is why canonicalization exists.

Canonicalization is the contract-governed act of translating admitted presentation into the system's own stable standard
when equivalence has already been declared. It is not cleanup. It is not repair. It is not trust in an adapter, a JVM, a
compiler backend, a serializer, a framework, or a Unicode library. It is the machine refusing to let outside
presentation
define internal identity.

If equivalence is declared, the machine chooses the same representative every time under its own rule. If equivalence is
not declared, the machine keeps the material distinct.

```text
Canonicalization Rule:
    the contract that declares how admitted presentation material with declared-equivalent meaning is reduced to the
    system's stable representation before identity is issued

Canonical Representation:
    the system-owned stable representative produced by that rule
```

The important word is declared.

A behavior does not become equivalent because somebody observed it behaving that way. An adapter does not get to define
equivalence just because it produced the bytes. A framework convention does not become equivalence because the framework
got there first. A test expectation does not become equivalence because the test would break otherwise.

Without declared equivalence, canonicalization becomes another hole where implicit contracts crawl back into the
machine.

A canonicalization rule must say what differences do not change meaning, which system-owned representative stands for
that meaning, which source drift is tolerated, and what failure is declared when the representative cannot be produced
safely.

```text
equivalence:
    which presentation differences do not change declared meaning

representative:
    which system-owned stable representation stands for that meaning

source drift:
    which adapter, backend, version, text, metadata, or encoding differences are tolerated as equivalent

failure:
    what declared result follows when canonicalization cannot safely complete
```

The contract does not need to name the parser, table, cache, string routine, or data structure used to do the work.
Those
belong to realization.

The contract has to name the obligation: declared-equivalent material must be translated into the system's stable
standard, and non-equivalent material must not be merged for convenience.

This is where identity starts to become safe. Before the machine can compare material, issue identifiers, derive facts,
build graph nodes, verify obligations, publish results, or produce useful diagnostics, it needs one internal handle for
one declared meaning.

Canonicalization gives declared meaning a deterministic internal handle.

### 12.7 Lowering Obligation

Canonical representation is not core material yet.

It has a stable handle now. Good. The machine has stopped the outside from deciding identity. But a stable handle is
still only a handle. The core needs material it can compare with the facts and state it already accepts.

That is the job of lowering.

Lowering takes the canonical representation and refines it into core-owned candidate material. This is not blind object
mapping. It may need declared contract facts, accepted immutable facts, reference laws, identity laws, version laws, and
governance binding to decide what the candidate actually points to.

A refund request is a simple example. The DTO may carry an order id and an amount. Canonicalization may settle the text,
unit, and identity spelling. Lowering then asks which accepted payment fact this request refers to, what money shape the
core uses, which reference law applies, and what candidate fact or candidate transition can be formed without changing
the declared meaning.

The result is not truth.

The result is a candidate the core can read.

```text
Lowering Obligation:
    the contract that declares how a canonical representation is refined into core-owned candidate material without
    changing declared meaning

Lowered Candidate Material:
    core-owned candidate material produced under that obligation
```

Ordinary mapping is too weak here. Ordinary mapping often means, "take this object and make another object that fits the
next layer." That is not enough. If the mapping carries framework behavior, serializer assumptions, reflection handles,
backend accidents, proxy tricks, or unratified external contract meaning into the core, it has not lowered the material.
It has smuggled foreign meaning across the boundary.

Lowering has to preserve the parts that make the material contract material:

```text
declared meaning
identity material
shape law
reference law
version law
governance binding
failure law
diagnostic evidence
```

It also has to block what must not cross:

```text
framework behavior
proxy behavior
serializer convention
reflection handle
backend accident
unratified external contract meaning
```

Canonicalization and lowering sit next to each other, but they do different work.

```text
canonicalization:
    gives one declared meaning one deterministic internal handle

lowering:
    uses that handle, accepted facts, and core laws to form core-owned candidate material
```

If lowering cannot preserve the declared meaning, cannot resolve the required reference, or cannot form candidate
material under the active governance, the machine must stop with a declared failure. No half-lowered object. No "the
next
stage will figure it out." That is how rotten material reaches the core.

Lowering forms the candidate.

It does not promote the candidate into accepted core material.

### 12.8 Invariant Contract

After lowering, the material is inside the core as candidate material.

It has crossed the boundary. It has a canonical handle. It has been shaped by core laws. The outside junk has been cut
away.

Still, truth has not happened.

Lowering only says that the core can form this candidate without changing declared meaning. Invariant asks the next
question: may the core accept this candidate under the law attached to this pipeline?

That judgment needs a basis.

```text
Accepted Core Basis:
    accepted core material named by the pipeline as the basis for judgment, including declared contract facts, accepted
    immutable facts, derived core facts, and current core state
```

Do not call the whole thing history.

Some of it may come from earlier accepted machine judgment. Some of it may have been declared up front by the contract
author: state definitions, transition laws, identity laws, shape laws, reference laws, publication rules, and other core
facts. Calling all of that history makes the model narrower than the machine.

The candidate does not walk into an empty room. The pipeline has already named the core basis that matters for this
judgment. The invariant does not go hunting through the machine.

The candidate may be well-shaped.

The candidate may be readable.

The candidate may have a valid canonical handle.

That still does not mean the core may accept it.

The question is whether accepting the candidate would make the core lie under the invariant attached to this pipeline.

That is invariant work.

```text
Invariant Contract:
    the contract that judges whether lowered candidate material may be accepted as core material under a pipeline-bound
    acceptance law
```

Do not confuse this with the old object-oriented class invariant.

That model ties invariant to a mutable object. The object mutates itself, breaks its own invariant during a method call,
and tries to repair the mess before anyone looks. State, validation, mutation, and behavior sit in the same object and
pretend the result is discipline.

This machine uses invariant differently.

Invariant is the core's acceptance judgment. A candidate comes in. The pipeline supplies the binding and basis. The
acceptance law says whether the candidate may stand. If the law does not hold, the candidate is not accepted. If it
holds, the machine may accept the candidate as core material.

```text
candidate fact
    -> invariant judgment
    -> accepted immutable fact
       or declared failure

candidate transition
    -> invariant / state judgment
    -> accepted transition
       or declared failure
```

The declaration must be precise enough to keep invariant from turning into a vague validator, but not so bloated that it
becomes a runtime object garden.

Use the smallest coordinates that keep the judgment honest:

```text
invariant:
    the declared invariant being applied

binding:
    the pipeline position where the invariant is attached

basis:
    the accepted core material named by the pipeline as the basis of judgment

candidate:
    the lowered candidate being judged for acceptance

acceptance law:
    what must be true for the candidate to be accepted under this invariant

failure:
    the declared result when the acceptance law does not hold
```

That is enough.

`Invariant` is the contract identity. Do not split it into a separate authority object. The declared invariant is the
authority.

`Binding` is just the place where the invariant sits in the contract pipeline. In the normal case, that place is already
obvious:

```text
lowering
-> invariant
-> accepted core material
```

That position tells the machine which candidate is being judged and which core basis may be used. Do not invent a
separate scope model to repeat what the pipeline already says. If the pipeline makes the attachment clear, the binding
is
already clear.

`Basis` is not a little object graph built for the invariant.

Do not make one.

The basis is the accepted core material named by the pipeline for this judgment. The contract does not care whether a
real machine stores that material in objects, tables, arrays, files, generated code, or some ugly metal box under the
floor. That is not the contract.

The contract says only this: the candidate is judged under the basis named by this pipeline, and the judgment must not
drag the whole machine into the room.

`Candidate` is the lowered candidate material under judgment. It should not become a nested object bundle just because
somebody wants the model to look important.

`Acceptance law` is the declared law that decides whether the lowered candidate may be accepted under this invariant.

It is not a relationship graph.

It is not a hidden query plan.

It is not a runtime object that carries old material around.

The contract names what must be true for acceptance. It does not prescribe how a realization finds, stores, addresses,
or
evaluates the material needed to decide that truth.

Presentation shape was handled before this. Core formation was handled by lowering. The acceptance law only says whether
this candidate may be accepted without making the core lie.

`Failure` folds together the contradiction and the declared result. If the acceptance law does not hold, the machine
needs a declared failure law: reject the candidate, deny the transition, stop the path, or produce another declared
result. Diagnostic evidence is not owned here; it belongs to diagnostic retention policy. The invariant may point to
that
policy, but it should not grow its own evidence object pile.

These coordinates are not runtime objects the machine must allocate per candidate.

They are contract coordinates.

The contract does not prescribe how a machine stores, addresses, evaluates, or optimizes these coordinates. That belongs
to realization. If the contract description turns into a graph of runtime objects, the design has already gone sideways.

Fact and invariant must stay separate.

The fact is material.

The invariant is the acceptance law over candidate material under a named core basis.

Mix them and the old object soup returns: data carrying rules, methods hiding validation, mutation pretending to be
state, and behavior sneaking into identity. That road is already full of wreckage.

A candidate fact can pass lowering and still fail invariant.

A candidate transition can be well-shaped and still be illegal.

A candidate publication can be available and still be forbidden.

Clean shape is not truth.

Core-owned candidate form is not truth.

Only material that survives the declared invariant can become accepted core material.

### 12.9 State Contract

`State` is a dangerous word because it arrives with luggage.

A mathematical model may call a point in a value space a state. Useful on paper. Dangerous as machine doctrine. If a
software machine imports that meaning directly, every possible combination of values starts asking to be treated as
state. The machine loses a small lifecycle vocabulary and gets a fat cloud of possibilities. That may help a proof. It
does not give the machine a clean next move.

Functional programming cleans up another mess. It may carry state as an explicit value from one function to the next.
That is often better than hiding mutation in some filthy corner. But a carried value is still only a carried value until
the contract gives it authority. Purity does not declare lifecycle. Passing a value forward does not decide whether the
next machine move is legal.

Object-oriented state is worse here. A field named `status`, a private variable behind a getter, or an object mutating
itself through methods is implementation material. If the lifecycle has to be inferred from whatever the object contains
after a method returns, the contract is doing archaeology instead of governing the machine.

This document uses the word more narrowly.

State is explicitly declared contract material for machine-move legality

It has to be declared before it governs anything. The machine cannot run first, inspect the wreckage, and then decide
what state it must have been in. That may be diagnosis. It may be explanation. It is not state authority.

```text
State Contract:
    the explicitly declared contract surface that defines the machine conditions under which a pipeline's next move is
    legal or illegal
```

This is why state feels like it runs beside the pipeline. Every pipeline move happens under some machine condition. But
the condition runs there as contract, not as implementation.

Do not draw the state surface by tracing the contract document. If every obligation becomes a state, the lifecycle turns
into a pile of nouns.

Do not draw it by tracing the implementation either. If every implementation step becomes a state, the implementation
has started writing the contract in crayon.

A contract clause does not automatically create a state. An implementation stage does not automatically create a state.
A log label, progress marker, or status word does not become state just because somebody gave it a serious name.

A label becomes state only when it changes the legality of the next machine move.

The next move may be admission, canonicalization, or some other move the contract governs. That list is not the
definition. It only shows where state starts to bite.

If the same moves remain legal before and after a label, the label is not state. It may still be useful for diagnostics
or explanation. Keep it there. Do not smuggle it into the state contract.

The same discipline applies to material already moving through the pipeline. DTOs, canonical representations, lowered
candidates, accepted facts, and diagnostic records may support a judgment. They may explain why a condition holds or why
a move was allowed. Their presence does not create lifecycle authority.

A condition discovered only after execution may explain what happened. It may help diagnosis. But it cannot become state
authority. Explicit declaration comes first. Otherwise the machine is not following a lifecycle; it is naming wreckage
after the fact.

For the surface it governs, state must be declared, finite, and closed. Open-ended lifecycle vocabulary makes the next
move slippery. Once the machine can invent, inherit, or infer new conditions while moving, legality is no longer
governed
by the state contract. It is guessed from whatever shape the run happened to leave behind.

Multiple pipelines may each carry their own explicitly declared state contract. That still does not create a
parent-child
state tree. The state surface of one pipeline does not lend meaning to another pipeline by ancestry. If pipelines must
coordinate, the coordination has to be declared explicitly. Containment is not authority. Similar shape is not
authority.
Shared names are not authority.

State also explains why invariant had to come first in this part of the document.

Invariant asks whether a lowered candidate may be accepted under a pipeline-bound acceptance law. State says which
declared machine condition that judgment is happening under, and whether the next move of that pipeline remains legal
after the judgment succeeds or fails.

Mix those together and the old swamp comes back: values pretending to be lifecycle, objects hiding state, proofs
ignoring the machine, and implementation steps dressing themselves up as contract.

A good machine declares state before movement and uses that declared state to keep movement honest.

### 12.10 State Transition Contract

State already explained the condition that governs movement.

Transition has a different problem to solve.

A good machine does not merely end up in the next condition. It moves there under declared permission. That is why
transition has to be explicit.

Movement is where the machine changes what it may do next. Before the move, one set of actions may be legal, one set of
failures may be reachable, and one set of obligations may still be waiting. After the move, that legal surface may
change. If the movement that changes those possibilities is implicit, then implementation order has become contract
authority.

That is the danger.

Hidden transition lets progress pretend to be permission. It lets a later condition inherit meaning from the fact that
an earlier step happened to finish. It lets successful execution pretend to prove that the move was legal.

A stored value can be rewritten, a status label can be moved, and an implementation step can mark progress. None of
that creates a contract transition by itself. A contract transition exists only when the contract has already permitted
a one-way machine move between flat declared conditions.

```text
State Transition Contract:
    the explicitly declared contract that permits a one-way machine move between flat declared conditions
```

This does not make transition a spare branch statement for the pipeline. The pipeline gives the movement surface.
Judgments may decide admission, acceptance, policy, failure, or some other contract result. Transition does not do that
job again. It only declares which condition-to-condition move is allowed after the relevant judgment has produced a
declared result.

The move must be declared before the machine performs it. If the machine moves first and the contract names the result
afterward, the transition did not govern movement. It only described the accident. That may be useful for diagnosis, but
it is not transition authority.

A transition declaration therefore needs more than a target label. The current condition has to be declared. The target
condition has to be declared. The permitted move between them has to be declared. If the move depends on an admission,
invariant, policy, or failure result, that result has to exist before the transition uses it. The transition does not
discover that result. It only refuses to let the machine move as if the result had appeared by magic.

If any of those terms is missing, the machine should not pretend that the move is merely incomplete or waiting for
implementation detail. The move is not a valid contract transition.

Transition is one-way inside the same pipeline movement. Regression is not a transition. A correction, retry, or
restart does not mean old material walks backward through the state surface. It enters as new contract-governed
material, a new run, or a new epoch. That distinction matters because backward movement lets the machine rewrite its own
story after the fact.

A cycle is not a disciplined transition model for the same movement surface. If the machine can move from one declared
condition to another and eventually return to the first condition inside the same surface, the contract is no longer
describing forward legality. It is hiding repetition inside state authority. When repetition is necessary, it must be
declared as governed repetition under policy, budget, capacity, failure, and diagnostic rules. It must not be smuggled
in as a state transition cycle.

A transition also does not move through a state tree. There is no parent state lending meaning to a child state. There
is no inherited transition. There is no override of a parent failure rule. States are flat declared conditions.
Transitions are explicit moves between those flat conditions.

Branching can still exist, but transition should not become a decorated `if` statement. A prior judgment may leave one
of several declared results on the table, and each result may permit a different next move. That still does not create
hierarchy. A branch is a set of permitted next moves, not a child surface.

Invariant and transition may touch near acceptance, but treating them as one big judge is how the mud comes back. The
invariant asks whether candidate material may be accepted without making the core lie. Transition does not rerun that
question. It only says which move is allowed once that declared result exists. Accepting material and moving the machine
stay close, but they do not become one vague validation step.

A good machine does not mutate itself and then search for a story that makes the mutation legal. It lets the relevant
judgment produce a declared result, then follows only the movement that the transition contract permits.

### 12.11 Explicit State Machine

State and transition are enough to form a state machine.

That is exactly why the state machine must be explicit.

If the contract declares the flat machine conditions, and it declares the one-way moves between those conditions, then
the state machine is already sitting there. A polite design document could say the state machine is implied.

I do not trust implied machinery.

Good ideas rot fast when the important part is left as an implication. People keep the name, mix the responsibility with
whatever code is nearby, and then act surprised when the machine turns into soup. Here the likely failure is boring and
obvious: the state machine gets buried inside admission code, invariant checks, policy evaluation, failure handling,
method bodies, callbacks, hooks, or some status label that happens to change at the right time.

No.

A transition should not be discovered by reading the implementation like a crime scene.

```text
Explicit State Machine:
    the declared manifest that manages only the states and one-way transitions of one state surface, including its
    initial condition and terminal conditions
```

The word `only` matters.

Acceptance, rejection, policy, and failure still belong to their own contracts. The explicit state machine keeps only
the
movement surface visible: states here, transitions here, initial condition here, terminal conditions here.

That manifest creates no new boss above state and transition. It should not become the pipeline, a workflow engine, or a
clever place to hide validation. It is the small, annoying, necessary table that prevents everyone from pretending the
state machine can be reconstructed later from whatever the implementation happened to do.

The initial condition says where movement through this state surface begins.

The state set says which flat conditions belong to this surface.

The transition set says which one-way moves exist between those conditions.

The terminal conditions say which declared conditions have no outgoing move inside this surface. A terminal condition is
not a verdict that the machine succeeded. It is not an invariant result wearing a nicer hat. It only says that, inside
this state surface, movement stops there.

The transition set must stay clean as well. For one state-to-state move, there must be one explicit transition
declaration. Not aliases scattered around the code. Not one transition in the manifest and another hidden in a callback.
Not a method that changes a label and calls the result architecture.

If the move is:

```text
A -> B
```

then the state surface declares that move once. If the machine needs a different meaning, it needs a different declared
move. If the meaning belongs to admission, invariant, policy, or failure, then it belongs there. Do not stuff it into
the state machine because the drawer was open.

The point is not ceremony.

The point is to keep state and transition from becoming implementation folklore. The explicit state machine names the
whole state surface so nobody gets to smuggle in extra movement later and call the mess a model.

---

## 13. Contract and Implementation

Contract and implementation should stay on separate axes.

Implementation still matters. A real machine still needs concrete machinery to run. It just belongs
on a different axis.

Contract and implementation move together like mirror images on different axes.

The contract declares what must be true. The implementation realizes it. They correspond, but they must not mix.

If the contract says that a result must be deterministic, the implementation may use any machinery that preserves that
obligation. If the contract says that a stage has a capacity limit, the implementation may realize that limit in many
different ways. If the contract says that a failure must be declared, the implementation may choose how to detect and
report it.

The contract must not name the mechanism. The contract must name the obligation.

As long as the declared obligation is preserved, the implementation should be replaceable. If replacing the mechanism
changes the contract, the mechanism leaked into the contract. At that point, the design has debt, not architecture.

Implementation machinery belongs outside the contract document.

The tool is not the contract. The obligation the tool must satisfy is the contract.

---

## 14. Message, Exposure, and Interaction

What the user sees and what the system uses to communicate internally should go through contracts.

If systems interact through implementation details, the contract gets mixed with implementation. That becomes debt.
Later, when you want to replace the implementation, you cannot do it freely because other parts of the system have
learned the implementation instead of the contract.

The user should know the contract. The rest of the system should communicate through contracts. Implementation should
work behind the contract like a shadow. It can be powerful, complicated, and optimized, but it should not become the
surface of interaction.

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

Machines have limits, so processing order exists. Some pipelines may need results from other pipelines before they can
continue. Waiting for those results, scheduling that work, and managing that dependency are implementation concerns.

The contract pipeline says what must be true in the logical flow. The implementation decides how to run the machine
without violating it.

For now:

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
judge DTOs as outside presentation at the boundary
admit only what passes admission
canonicalize declared-equivalent admitted material into the system's stable internal standard before identity becomes authoritative
lower canonical representation into core-owned candidate material
accept candidates only under pipeline-bound invariants and their declared acceptance laws
produce contract-governed immutable facts
treat state as explicitly declared contract material for pipeline lifecycle legality
allow only declared transitions
reject through declared failures
publish only accepted results
declare failure instead of hiding it
declare policy, budget, capacity, and governance instead of hiding them
verify realization against the declared contract
keep implementation out of the contract
```

The current direction is this.

Still left.