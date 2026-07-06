# What Is Contract

## Table of Contents

- [0. Intention](#0-intention)
- [1. The Explicit Machine](#1-the-explicit-machine)
- [2. Mathematics, Physics, and Engineering](#2-mathematics-physics-and-engineering)
- [3. Use Mathematics. Do Not Become Mathematics.](#3-use-mathematics-do-not-become-mathematics)
- [4. Contract](#4-contract)
- [5. Purpose](#5-purpose)
- [6. Make Interfaces Great Again](#6-make-interfaces-great-again)
    - [6.1 The Surface of an Interface Contract](#61-the-surface-of-an-interface-contract)
- [7. Evolution and Contract](#7-evolution-and-contract)
- [8. Good Machine and Function](#8-good-machine-and-function)
- [9. Core, Boundary, and the Fucking Bastards Outside](#9-core-boundary-and-the-fucking-bastards-outside)
- [10. Applying Contract to a Good Machine](#10-applying-contract-to-a-good-machine)
- [11. What Counts as Contract in the Pipeline](#11-what-counts-as-contract-in-the-pipeline)
- [12. Contract Presentations in the Pipeline](#12-contract-presentations-in-the-pipeline)
- [13. Contract and Verification](#13-contract-and-verification)
- [14. Contract and Implementation](#14-contract-and-implementation)
- [15. Message, Exposure, and Interaction](#15-message-exposure-and-interaction)
- [16. Whole Machine](#16-whole-machine)
- [17. Object Orientation and Inheritance](#17-object-orientation-and-inheritance)
    - [17.1 How Inheritance Fucked Up Software](#171-how-inheritance-fucked-up-software)
    - [17.2 Polymorphism, Substitution, Segregation, and Inversion](#172-polymorphism-substitution-segregation-and-inversion)
    - [17.3 Abstraction](#173-abstraction)
    - [17.4 The JVM Is What Happens When Implementation Becomes Contract](#174-the-jvm-is-what-happens-when-implementation-becomes-contract)
    - [17.5 Type Is a Contract Name, Not Contract Authority](#175-type-is-a-contract-name-not-contract-authority)
    - [17.6 Class Is Where Roles Collapsed](#176-class-is-where-roles-collapsed)
    - [17.7 Rust Exposes Implementation as Contract](#177-rust-exposes-implementation-as-contract)
- [18. Current Working Definition](#18-current-working-definition)

---

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

In this document, a good machine means a machine that actually works in reality, knows its limits, prepares for them,
fulfills its declared purpose, and makes the conditions that govern its behavior explicit.

Most serious architecture already moves in this direction. Real systems already depend on contract-shaped structure;
they just leave too much of it implicit.

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

The first dimension contains closed contract presentations and required coordinates:

```text
interface contract and its public surface
input contract
admission contract
canonicalization contract
lowering contract
fact contract
invariant contract
state contract
state transition contract
explicit state machine manifest
failure contract
publication contract
diagnostic evidence / retention contract
version coordinate
policy / budget / capacity / governance contract
```

The second dimension contains interaction manifests. A manifest does not inherit contracts. It does not compose
interfaces. It does not pull in another manifest, which pulls in another manifest, which pulls in another one until
nobody knows what is actually required.

It binds a flat list of closed contract presentations and required coordinates for one interaction.

```text
interaction manifest
    -> interface contract and its public surface
    -> input contract
    -> admission contract
    -> canonicalization contract
    -> lowering contract
    -> fact contract
    -> invariant contract
    -> state contract
    -> state transition contract
    -> explicit state machine manifest
    -> failure contract
    -> publication contract
    -> diagnostic evidence / retention contract
    -> version coordinate
    -> policy / budget / capacity / governance contract
```

An explicit state machine manifest may feel like orchestration, but in this model it is only the declared surface of
states and one-way transitions. It is listed here because the movement surface must be explicit, not because it becomes
a parent contract above state and transition. It should state its own conditions and legal moves and stop there. It
should not inherit another state machine. It should not compose another state machine.

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

I do not trust you.

So this cannot be a style guide.

Contract inheritance and recursive contract composition must be rejected by the compiler.

Not discouraged. Rejected.

The compiler should stop the shape before anyone gets clever with it.

---

## 5. Purpose

Machines are built for a purpose.

But that does not mean "purpose" is some magical contract feature the software can just figure out for you.

Purpose comes before the contract. It is the compass. It explains why the designer chose these specific obligations,
why certain trade-offs are forbidden, and why a machine may prefer one kind of failure, diagnostic, publication rule,
or governance rule over another.

But the software itself is utterly indifferent to your purpose.

A machine cannot discover your intention. It cannot mathematically prove it. It cannot enforce an abstract human desire.
Human purposes are too contextual, too fragmented, and too dependent on judgments that live outside the machine. If the
purpose has not been lowered by the designer into hard, machine-readable obligations, the machine has nothing honest to
judge.

Aligning the contract to the purpose is the designer's job.

Not the compiler's.

The machine can only judge what has been explicitly declared. If the purpose has not shaped concrete input rules,
admission rules, lowering obligations, fact laws, invariants, state movement, failure rules, publication discipline,
diagnostic retention, version coordinates, policy, budget, capacity, or governance, then the machine must not pretend
that it verified the purpose.

This is where the rot usually starts.

The moment purpose is treated as something the machine can prove by itself, the architecture drifts into bullshit. Type
theorists will try to encode the purpose into some unreadable formal proposition. Proof-zealots will shrink the real
machine into a toy small enough to prove. Framework cultists will build proxy layers, observe runtime behavior, and
call that observation a proof. An AI may generate an interpretation and try to pass it off as authority.

That is proof theater.

Purpose is not a type-level costume. It is not an oracle hiding behind a friendly API. It is not a proxy proof. It is
not an AI interpretation. And it is absolutely not an excuse to weld the contract to a specific implementation just
because that implementation happens to work today.

A purpose guides contract selection and arrangement.

The contract must still declare the actual obligations.

A system whose declared purpose includes determinism may choose contracts that forbid backend order from becoming
semantic order, forbid cache state from changing meaning, require canonical ordering, require exact collision handling,
require fail-closed judgment, require explicit state movement, and restrict publication to stable declared claims.
Those are contracts. The purpose itself did not become a proof. It guided which obligations had to be declared.

The same applies to any other purpose. A safety-oriented system, a privacy-oriented system, a low-latency system, a
regulatory system, or a financial settlement system may arrange different contracts because it is built for a different
end. The machine does not discover that end. The designer declares obligations that make the end visible to the machine.

So the line is absolute:

```text
purpose guides contract selection and arrangement
contract declares obligations
implementation realizes those obligations
```

If the purpose has not been lowered into explicitly declared obligations, the machine verifies nothing.

And if the implementation is presented as the proof that the purpose was satisfied, the contract has already been
swallowed by the implementation.

---

## 6. Make Interfaces Great Again

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

For one method, the interaction manifest is still flat:

```text
submit(...)
    -> interface contract and its public surface
    -> input contract
    -> admission contract
    -> canonicalization contract
    -> lowering contract
    -> fact contract
    -> invariant contract
    -> state contract
    -> state transition contract
    -> explicit state machine manifest
    -> failure contract
    -> publication contract
    -> diagnostic evidence / retention contract
    -> version coordinate
    -> policy / budget / capacity / governance contract
```

Methods should not become another genealogy. Method inheritance must not become contract meaning, overloads must not
become contract reuse, and default methods must not become hidden contract behavior. The method is the handle. The
manifest is the contract. The call is just how
one implementation path may enter the operation.

Keep the method, but make it stop pretending to be enough. Make interfaces great again.

### 6.1 The Surface of an Interface Contract

An interface contract needs a surface.

The surface is the public reliance boundary of that interface contract. It tells the outside user exactly what they are
allowed to touch: which operation they can select, what input presentation they can hand over, what output claim they
should expect, and which public failures or limits they can safely rely on.

The surface is not the whole machine. It is the strict part of the interface contract the user is allowed to depend on.

And that distinction matters.

A good machine does not make the user dissect its guts just to use it correctly. You do not need to understand
combustion timing, fuel injection, or brake hydraulics to drive a car. A smartphone user does not need to learn
semiconductor physics, file systems, or memory controllers just to make a call.

That is what a surface is for.

It gives the user a stable, usable contract. Press this. Turn this. Submit this. Receive that. Stop here. Retry there.
The user relies on the promised surface, not on the plumbing hiding behind it.

If the machine forces the user to understand its internal mechanisms just to use it correctly, the surface is garbage.

And there is another failure mode that is worse.

If internal machinery bleeds into the surface, the implementation loses its freedom to move. Imagine a car where the
public driving contract forces the driver to rely on the exact gearbox mechanism and hydraulic response curve. The
second the manufacturer tries to replace the transmission or move to an electric drivetrain, the driving contract
breaks. You permanently destroyed your freedom to replace the backend because you taught the user to rely on your
plumbing.

Software is exactly the same.

A surface must never expose the current implementation as public meaning. It can expose presentation shapes, operation
names, public failures, public limits, and public claims. It can say what the user is allowed to rely on. But it must
absolutely not force the user to rely on whatever temporary machinery happens to realize the contract today.

Spring is a useful warning here.

Its surface looks deceptively simple: slap an annotation on a class, inject a dependency, and let the magic handle the
request. But the minute you do serious engineering, you are forced to understand the hidden machinery: when the
framework is speaking for the program, when the program is actually speaking for itself, and which invisible convention
is quietly deciding the result.

That is not a clean surface. That is internal machinery leaking straight into the user's contract.

The problem is not that Spring uses machinery. Every real machine needs machinery. The problem is that the user is
forced to reverse-engineer that machinery just to use the surface correctly.

A good machine does not play that game.

This is also where hexagonal architecture meets the same wall.

Hexagonal architecture wants the core protected from outside technology. That instinct is useful. The problem appears
when the outside technology is not a small tool, but a platform.

Put Spring inside the core, and the core starts speaking through Spring. The operation is no longer only the operation
you declared. It becomes the operation as shaped by Spring's runtime machinery. The contract meaning has been handed to
an outside machine.

Push Spring out to an adapter, and the core stays cleaner. But Spring stops being the platform it was designed to be.
Its strongest force now sits at the edge, away from the place where the system's main obligations are supposed to be
declared.

That is the dilemma.

Hexagonal architecture does not remove it. It names the place where it hurts.

A port can separate code. It cannot, by itself, purify authority. If the platform decides the meaning, the core is
contaminated. If the platform is kept away from meaning, the platform becomes outer machinery.

Kontrakt takes the harder line. The contract must not be donated to the platform, and the platform must not be smuggled
into the contract just because it is convenient. The machine may use outside machinery behind a boundary, but the
declared obligation must be lowered into contract-owned material before that machinery gets to act.

The surface declares the public obligation. It does not force the user to learn the implementation.

This is exactly why a naive interface is not enough.

An interface may present a software-visible operation surface. A method may give the user a familiar handle. But the
surface of the interface contract must still explicitly declare what the user can depend on, without demanding that they
learn the inside of the machine.

The interaction manifest does not arrive later as another runtime step. It binds the selected interface surface to the
full contract world: input, admission, canonicalization, lowering, fact, invariant, state, transition, failure,
publication, diagnostic retention, version coordinate, policy, budget, capacity, and governance.

The user sees the surface.

The verifier sees the manifest.

The implementation realizes the manifest behind the surface.

If the surface leaks implementation, the implementation has stolen contract authority.

If the surface hides a public obligation inside internal machinery, the contract has failed in the other direction.

A good surface lets the user depend on the contract without giving a single fuck about the machinery.

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

The version coordinate appears later in the pipeline discussion.

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
-> public claim or publication denial

diagnostic evidence:
    may be offered at declared judgment stages
    retained only through the diagnostic retention boundary
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
declares an obligation.

The distinction is simple enough to miss.

A stage says that something happens. A contract says what the machine must preserve, permit, reject, produce, or refuse
while it happens.

This means a pipeline name is not contract authority. Neither is execution order. A stage becomes contract-shaped only
when changing or removing its declared obligation would change what the machine is allowed to accept, believe, move,
retain, or publish.

The same test works in the other direction. If a step can be replaced, fused with another step, or removed without
changing any declared obligation, that step belongs to realization. It may be necessary machinery. It is still not a
contract merely because the pipeline contains it.

So the useful question is not:

```text
Is this part of the pipeline?
```

It is:

```text
What obligation would the machine lose if this declaration disappeared?
```

If there is no clear answer, the declaration is probably describing implementation.

The real line is still declaration.

A behavior does not become contract just because someone can observe it, depend on it, or break when it changes. That is
how duck typing, framework convention, accidental behavior, and test expectation sneak back in as implicit contracts.

A contract exists only when the obligation is declared as contract material and bound through the valid contract world:
a closed contract presentation, a flat interaction manifest, a required coordinate, or governed contract metadata.

If a change alters a declared obligation, it is contract change.

If a change preserves the declared obligation and only changes realization, it is implementation change.

If a change breaks undeclared reliance, that reliance is compatibility debt, not contract authority.

Do not let observed behavior become contract by accident.

Contract and implementation stay on different axes. The contract names the obligation. The implementation realizes it.
As long as the obligation survives, the realization may change. If changing the realization also changes the contract,
the mechanism has leaked into authority.

The following section applies this distinction to the contract presentations that appear in the pipeline.

---

## 12. Contract Presentations in the Pipeline

Calling everything `contract` does not make the machine explicit. It only gives the confusion a respectable name.

This section follows material through the pipeline and separates the obligations that govern it along the way. Those
obligations belong to one machine, but they do not answer the same question and should not be allowed to blur together.

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
Think of a state transition contract that says which move is legal. The immutable fact provides the factual material
that transition judgment may inspect. Think of a publication contract that says what public claim may be formed. The
immutable fact provides the factual material that publication judgment may inspect.

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

A real machine is not an idea floating outside the world. It can bear only so much load before its operation begins to
degrade or fails altogether.

A good machine admits this before reality demonstrates it the hard way. Its usable capacity and operating limits belong
in the contract because pretending the machine can endure anything does not make it stronger. It only makes the failure
dishonest.

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
    the contract that declares the admissible limit of a machine, surface, stage, or storage region

Governance Contract:
    the contract that declares how contract sets, policy sets, budget profiles, capacity limits, versions, and
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

Capacity is the machine's declared admissible limit. It says how much a machine, surface, stage, or storage region may
accept,
retain as bounded evidence, keep in flight, expose, or publish. Capacity should not be guessed from optimism. It should
be measured, chosen, declared, and governed. Valid material may still be rejected or deferred by capacity. A finite
machine can reject valid material when accepting it would exceed what the machine can survive.

Governance is the validity of the contract world. It says which contract set, policy set, budget profile, capacity
limit, version, and manifest binding is valid for a machine. Without governance, nobody knows which rules are
actually
active. Systems quietly rot in that gap.

These contracts declare judgment criteria, finite allowance, admissible limit, and validity. They do not declare
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
    bound how much diagnostic evidence remains and how long it remains available

diagnostic-use policy:
    allow retained evidence to support diagnosis, but never authoritative core reasoning or publication

reject-unknown policy:
    reject undeclared fields, metadata, shape, or external contract material

compatibility policy:
    allow legacy presentation only under a declared compatibility rule

capacity-overflow policy:
    reject or defer material when the declared capacity limit cannot admit it

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

A contract can govern a failure only while some software remains able to carry out that governance.

The machine may declare what happens under bounded shortage, an expected interruption, or a failure from which another
execution can recover. Those cases still leave machinery capable of producing a result, preserving already committed
material, or beginning a declared recovery path.

There is another kind of ending. The process may be killed without a final instruction. The kernel may fail. Power may
disappear. The physical substrate may stop existing in a form the software can use. The dead execution cannot perform
one last judgment, publish one last failure, or carefully finish its diagnostic evidence. Asking it to do so would be
asking the missing machinery to operate.

This document does not turn that physical ending into another contract result. It marks the point where the authority
of the running software ends.

The machine may still contract for what must become durable before such an ending, and another execution may contract
for how that durable material is inspected or recovered afterward. That is different. Those obligations belong to the
living execution before the loss and to the recovering execution after it, not to an imaginary final act performed by
the execution that was destroyed.

A good machine should admit this boundary. Otherwise `declared failure` quietly becomes a promise that software will
remain in control after the means of control are gone.

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
    bounded, non-authoritative explanation retained under declared diagnostic policy from a declared judgment result

Retention Policy:
    the policy that declares whether a declared judgment result may leave evidence, what may be retained, how it is
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

A raw presentation may carry a public reference, a value shape, and a claimed operation. Canonicalization may settle the
spelling, ordering, and representation of that presentation. Lowering then asks which accepted core material the
reference may point to, which reference law applies, and what candidate fact or candidate transition can be formed
without changing the declared meaning.

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

Lowering has to preserve the contract meaning carried into candidate material:

```text
declared meaning
identity material
candidate kind
shape law
reference law
version coordinate
governance binding
candidate authority
```

It also has to obey the obligations governing the lowering judgment:

```text
failure law
diagnostic obligation
```

`Candidate kind` says whether lowering formed a candidate fact, a candidate transition, or another declared candidate
form.

`Candidate authority` says what lowering did not do. The result is core-readable candidate material. It is not accepted
fact, established state, or permitted transition merely because lowering succeeded.

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
state. The machine loses a small movement vocabulary and gets a fat cloud of possibilities. That may help a proof. It
does not give the machine a clean next move.

Functional programming cleans up another mess. It may carry state as an explicit value from one function to the next.
That is often better than hiding mutation in some filthy corner. But a carried value is still only a carried value until
the contract gives it authority. Purity does not declare machine condition. Passing a value forward does not decide
whether the
next machine move is legal.

Object-oriented state is worse here. A field named `status`, a private variable behind a getter, or an object mutating
itself through methods is implementation material. If the machine condition has to be inferred from whatever the object
contains
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

Do not draw the state surface by tracing the contract document. If every obligation becomes a state, the movement
surface turns into a pile of nouns.

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
a move was allowed. Their presence does not create movement authority.

A condition discovered only after execution may explain what happened. It may help diagnosis. But it cannot become state
authority. Explicit declaration comes first. Otherwise the machine is not following a declared movement surface; it is
naming wreckage
after the fact.

For the surface it governs, state must be declared, finite, and closed. Open-ended movement vocabulary makes the next
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

Mix those together and the old swamp comes back: values pretending to be movement authority, objects hiding state,
proofs
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

### 12.12 Publication Judgment

Accepted material is not automatically public material.

That sentence is dull. Good. A machine that forgets it starts leaking internal truth as public claim.

An accepted immutable fact says the core may stand on that material. It does not say the outside world may see it, rely
on it, or receive it in some public shape. Core truth and public claim are different surfaces.

Publication is the point where the machine speaks outward.

```text
Publication Judgment:
    the contract judgment that decides whether a public claim may be formed from accepted material, what that claim is
    allowed to say, and which stable public meaning governs it
```

The important word is `claim`.

Publication does not mean the accepted fact walks outside wearing a public hat. The public claim may have its own public
shape. That shape is not the core fact.

Publication is not the machine dumping whatever it has. A published result is the machine saying something to an outside
surface under contract authority. If the contract has not allowed that outward claim, then the machine has not
published. It has leaked.

A fact can be true in the core and still be forbidden in public.

That is not contradiction. That is boundary discipline. The core may need material that the outside surface must not
receive. The outside may need a shape that the core should never treat as its own material. Publication judgment
protects
that crossing.

The judgment has to name the accepted material it is speaking from, the public surface it is speaking to, the claim
shape
it wants to expose, and the public meaning under which the claim is allowed. If publication is denied, the denial must
also be declared. Silence is not a publication rule.

Stable public meaning matters because a published claim must not quietly change after it leaves. If the public meaning
changes later, the old claim does not improve itself in the dark. The machine needs a new public meaning, a new claim,
or a later version coordinate. It does not get to pretend the old publication was always saying the new thing.

Diagnostic evidence is not publication just because somebody can read it. Evidence may explain why publication was
allowed or denied. It may be retained for diagnosis. But unless the publication judgment allows it to become an outward
claim, it stays evidence. A debug-shaped leak is still a leak.

Publication judgment also keeps the pipeline honest. A value returned, written, emitted, or displayed by implementation
machinery has no publication authority by itself. The authority is the declared judgment that this public claim may be
formed from accepted material under this public meaning.

The point is simple.

The machine may know more than it is allowed to say.

### 12.13 Diagnostic Evidence

A good machine should be able to describe its own condition.

Not because logging is accountability. A pile of records is often just a landfill with timestamps.

Other engineering disciplines do not treat failure as mystical weather. An aircraft does not become safer because
everyone shrugs at the wreckage; it carries flight records so the event can be reconstructed. A car does not ask the
mechanic to guess the mood of the engine; it exposes diagnostic trouble codes because "something went wrong somewhere"
is not engineering. A power system does not merely say the lights went out; its protection records help explain which
condition tripped and why the rest of the system was protected.

Software should not get a cheaper standard just because its wreckage is made of bits.

A good software machine must be able to name its own situation: which declared judgment spoke, what result it produced,
and what contract reason made that result honest. If it rejects, defers, denies movement, refuses publication, or fails,
it should not leave the next reader staring at smoke and inventing a theory after the fact.

This is not about logs as a fetish. It is about refusing to let failure become folklore.

```text
Diagnostic Evidence:
    declared explanatory material retained from a contract judgment so the machine can account for a declared result
    without making the evidence the authority that produced the result
```

Diagnostic evidence explains.

It does not govern.

The pipeline location matters. Evidence should appear near declared judgment stages, not at the end as a bag of guesses.
Admission may need to explain why material continued, stopped, failed, or was deferred. Lowering may need to explain why
a candidate could or could not be formed. Invariant judgment may need to explain why material became accepted core
material or did not. State movement may need to explain why a declared move was followed or denied. Publication judgment
may need to explain why a public claim was formed or refused.

The common shape is the same:

```text
declared judgment stage
    -> declared result
    -> diagnostic evidence candidate
    -> diagnostic retention boundary
        -> retained diagnostic evidence
        -> discarded diagnostic material
```

The judgment stage may offer explanation material for the result it produced. That does not mean the explanation
survives the run. A thing existed during a pipeline run. So what? Existence is not retention.

The diagnostic retention boundary decides what crosses out of the run as retained diagnostic evidence. That boundary is
contract material: it says what kind of explanation may remain, what must be reduced or discarded, and which declared
result the retained evidence is allowed to explain.

This is where discipline matters.

Diagnostic evidence is dangerous. It can leak internal meaning. It can retain hostile input. It can expose policy shape.
It can become a denial-of-service surface if the machine keeps every helpful little scrap. A machine that records
everything has not become transparent. It has become easier to attack.

So the evidence must be bounded by contract. The machine should preserve enough declared explanation to account for its
result, and no more authority than that explanation is allowed to carry.

Do not turn this into surveillance.

A proxy layer is not a conscience. Wrapping every call, hooking every method, monitoring every object, and scraping
every
exception does not make the machine honest. It creates another implicit control system and then asks that system to
explain the first one.

The alternative is boring, which is usually a good sign.

Each declared judgment carries a diagnostic obligation. When the judgment produces a declared result, the contract also
declares what kind of explanation may be offered for that result and what kind of material must not survive as evidence.
The explanation is tied to the judgment, not stolen from the side by an observer.

Operational telemetry may still exist. It may be useful. It may even be necessary. But it lives on the implementation
axis. Contract diagnostic evidence must come from declared judgment stages and pass the declared retention boundary.
Otherwise it is just observation wearing a contract hat.

Retained diagnostic evidence is still not publication.

If retained evidence needs to leave the machine, it must pass publication judgment as its own public diagnostic claim.
A debug-shaped leak is still a leak, even when the leak has a very serious incident number attached to it.

Diagnostic evidence must also remain interpretable under the contract meaning that produced it. That is why version
coordinates come next.

### 12.14 Version Coordinate

A version number is not magic dust.

Versioning is not bookkeeping. It becomes contract material when meaning can move while the material still looks
familiar.

That distinction matters because this document is not talking about release labels. A build version, library version,
deployment version, storage format version, or migration script version may be useful to a realization. Fine. Let the
implementation keep its paperwork. The contract needs something narrower.

A lowered candidate may be cached. An accepted fact may be reused. A public claim may be read later. Diagnostic evidence
may be opened long after the run that produced it. If contract meaning changed in the meantime, the machine must not
read
that old material as if nothing happened.

That is the first reason for a version coordinate.

The second reason is stronger: a good machine should own its meaning.

Without versioned meaning, the latest code becomes king. The newest adapter, schema, deployment, framework behavior, or
migration script starts deciding what old material means. That is backwards. The machine should decide which contract
meanings remain authority, which meanings require compatibility, which meanings must be judged again, and which
meanings are no longer accepted.

```text
Version Coordinate:
    the declared coordinate that tells the machine which contract meaning was active when a judgment, material, claim, or
    evidence was produced
```

The important word is `meaning`.

The coordinate is needed when the machine must decide whether older material is still allowed to participate in the
current contract world. Can it be lowered? Can it still count as accepted? Can it form a public claim? Can its
diagnostic
evidence still explain anything? Should it be reused, rejected, or judged again?

Those are not clerical questions. They change what the machine is allowed to claim, trust, expose, or preserve. That is
why the coordinate belongs to the contract basis.

Without a version coordinate, the machine starts doing a nasty little trick: it reads yesterday's material with today's
meaning and calls the result consistency.

That is not consistency.

That is a costume change.

Lowering needs the coordinate because raw presentation does not carry canonical meaning by itself. The machine must know
which lowering meaning produced the candidate it is about to discuss.

Invariant needs the coordinate because accepted material must remember the meaning under which it was accepted. If the
acceptance law changes later, the old material does not automatically become accepted under the new law just because the
machine likes continuity.

Publication needs the coordinate because a public claim is not just a shape leaving the machine. It is a claim under a
public meaning. If that public meaning changes, the old claim does not quietly grow a new soul.

Diagnostic evidence needs the coordinate because explanation rots when meaning moves. A retained reason, rule name,
state label, transition name, or publication denial can be read honestly only under the meaning that made it true.

Version change should not be smuggled into state transition, pipeline progress, or mutation. When contract meaning
changes, old material does not walk forward wearing a new badge. The machine needs a new judgment, a compatibility
decision, a new public claim, or a declared refusal to reuse the old material. If old material is allowed to survive
under new meaning, the machine has to say so through contract-governed compatibility. Otherwise it is just
reinterpretation with a better haircut.

The version coordinate should not become a wrapper either. Do not build a tiny version object and hang it around every
value like a decorative tag. The contract asks for a declared coordinate of meaning. A realization may lower that into
facts, manifests, tables, generated images, identifiers, or some uglier machinery. The mechanism is not the point.

The point is to stop the machine from confusing stable-looking material with stable meaning, and to keep authority over
which meanings the machine is still willing to recognize.

### 12.15 Where Preconditions and Postconditions Went

Someone familiar with DBC will eventually ask an obvious question:

```text
Where are the preconditions and postconditions?
```

`Precondition`, `postcondition`, and `invariant` make sense when a routine or callable boundary is the center of the
picture. Something must hold before the call. Something must hold after it. Something must remain true while the object
or operation does its work. That vocabulary is useful in the machine it was made for.

Nothing was forgotten here. The pipeline simply changed the picture enough that those three names stopped being useful
as contract kinds of their own. There is no single useful moment called `before`, followed by one clean moment called
`after`. Material reaches a declared judgment, receives a declared result, and that result may become material for the
next judgment. What looks like a postcondition from one position may look like a precondition from the next.

```text
judgment result
-> material for the next judgment
```

Take admission. Raw presentation, active policy, budget, and capacity may all matter before admission can produce
admitted material. From the narrow view of that judgment, they resemble preconditions, and admitted material resembles
a postcondition.

Move one step forward and the labels shift. Admitted material is now something canonicalization or lowering receives.
The former postcondition has become part of the next judgment's starting material. A lowered candidate then reaches
invariant judgment. An accepted fact may later reach state movement or publication judgment. The words `pre` and `post`
keep moving because the machine keeps moving.

That moving viewpoint is exactly why those words are too relative to organize the contract pipeline. A good machine
needs each obligation where it has authority. The entrance question belongs to admission. Whether candidate material may
become core truth belongs to invariant. Whether accepted material may leave the machine belongs to publication. Calling
all three `conditions` is possible, but it throws away the reason they were separated.

Calling the first half `preconditions` and the second half `postconditions` would not make the machine clearer. It would
put several different judgments into two large boxes, then force the reader to open the boxes and sort everything out
again.

Worse, declaring separate pipeline preconditions and postconditions would repeat obligations already declared where
they belong. Now the machine has two descriptions of the same law. Sooner or later they disagree, and somebody gets the
excellent job of deciding which lie is official.

Preconditions and postconditions may still be useful inside a realization or a narrow proof. They simply add no new
authority to the contract pipeline.

If someone insists on translating this machine back into that vocabulary, the translation is possible:

```text
precondition:
    whatever declared material and authority a particular judgment requires before it can decide

postcondition:
    whatever declared result that judgment produces

invariant:
    the acceptance law applied where candidate material seeks core authority
```

That translation is a view of the pipeline, not its structure. The useful declaration is already sitting at the
judgment where the machine needs it. Wrapping the same obligation in another `before` or `after` would only give the
machine a second place to contradict itself.

### 12.16 Execution Flow, Not Lifecycle Vocabulary

This section is not a new contract type.

If someone reads this and builds a `Lifecycle Contract` or a `Scope Contract`, they have found a very expensive way to
miss the point.

The issue is language. `Scope` and `lifecycle` already carry too much baggage. Some of it comes from ordinary
programming-language visibility and storage rules. Some of it comes from objects, callbacks, framework phases, ownership
models, containers, inheritance, and subtype tricks. Whatever the source, the words pull attention toward the life,
reach, activation, disposal, and managed existence of things.

That is the wrong axis here.

When execution is hidden inside objects, callbacks, framework phases, and indirect dispatch, that vocabulary may help
people keep the mess from eating itself. Fine. Let that world have its tools.

It is not the language of this machine.

This machine is not trying to manage a crowd of objects whispering to each other through callbacks. It is trying to run
a declared pipeline. The useful questions are simpler and less theatrical:

```text
which pipeline run is this?
which declared stage is speaking?
which judgment point produced a result?
did the flow continue, stop, defer, fail, move, publish, or retain evidence?
```

That is enough.

If a machine has an explicit pipeline, it does not need a little mythology of object birth and death to explain what is
happening. The run enters. A declared stage acts. A judgment produces a declared result. The flow either continues,
stops, waits, fails, publishes, or leaves retained evidence under a declared boundary.

No hidden ceremony is required.

This matters for diagnostic evidence. Diagnostic material does not need the managed existence of an object to explain
itself. It appears at a declared judgment point in the pipeline flow. Most explanation material should end with the run.
Only material admitted by the diagnostic retention boundary remains as retained diagnostic evidence. Crossing that
boundary still does not make it publication. It only means the machine kept enough internal explanation to account for
what happened.

So when this document talks about a run, a stage, a judgment point, a result, or a retention boundary, it is not
smuggling
scope and lifecycle back under better branding. It is choosing the vocabulary of a straight machine over the vocabulary
of an object jungle.

Good.

The jungle had its chance.

---

## 13. Contract and Verification

If the interface is the contract document, verification is the check that a realization satisfies the declared
obligations.

The important part is not "tests." Tests are only one late way to look at a machine after it already exists.

The better direction is earlier than that.

First make the contract material rich enough to say what the machine actually promises. The closed contract
presentations name the obligations: interface surface, input, admission, canonicalization, lowering, fact, invariant,
state, transition, failure,
publication,
diagnostic, policy, budget, capacity, and governance. Required coordinates name the active meaning and binding. The
explicit state machine manifest names the closed state surface for that interaction. The manifest binds those materials
to one interaction without inheritance, composition, or hidden ancestry.

That gives the compiler something real to guard.

Not a vague method name.

Not a class shape.

Not a comment.

A declared contract surface.

If the implementation claims to realize an interaction, the compiler or verifier should be able to ask ordinary
questions before the machine turns into runtime soup:

```text
Does the implementation expose the required interface surface and operation handle without leaking implementation machinery?
Does it accept only the declared input presentation?
Does it apply the required admission contract?
Does it apply the required canonicalization contract?
Does it produce the declared failure outcomes?
Does it preserve the declared invariant?
Does it obey the declared state transitions?
Does it publish only what the publication contract allows?
Does it stay under the active policy, budget, capacity, and governance contracts?
Does it produce the diagnostic evidence the contract requires?
```

That is the point of making the contract presentations, coordinates, and manifest explicit. The compiler cannot guard
meaning that was never declared. If the contract surface is too thin, the compiler has nothing to hold. Then people
compensate with tests, conventions, annotations, comments, and hope.

Hope is not a verification strategy.

A test can still be useful. It can check concrete behavior. It can exercise examples. It can catch mistakes in a
realization.

But once the contract has been made explicit, tests lose one old excuse.

A test is a small verification document. It says, "under this condition, this machine should do this." If the machine
already declares its input, admission, canonicalization, lowering, invariant, state movement, failure, publication,
diagnostic evidence,
policy, budget, capacity, and governance, then the test should not invent a private little religion about what the
machine probably means.

It should verify the declared contract surface.

That changes the target. A test over one declared judgment surface is not a ritual around a method body. It is a check
of that surface: admission, canonicalization, lowering, invariant judgment, transition, publication, or diagnostic
retention. A test over the airlock from presented input to admission result is a boundary test. A test over a small
logical pipeline from declared input to declared result is already an integration test for that pipeline.

The labels are not the important part.

The contract tells the test where to look. If a test verifies a declared obligation, it is helping verify the contract.
If it only freezes incidental behavior, object shape, call order, framework output, or whatever state happened to be
left behind this week, then it is not protecting the contract. It is preserving implementation residue and calling the
fossil a quality strategy.

The order should be:

```text
interface surface, closed contract presentations, and required coordinates
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
realization. When the contract is explicit enough, tests should be derived from it where possible, not manually guessed
into existence around whatever the implementation happened to do.

The best verification is not a mountain of tests. The best verification is making invalid software impossible to write,
impossible to compile, or impossible to publish before it becomes a runtime mess.

Traditional DBC usually describes verification around what must hold before and after a callable operation.

That vocabulary is not used as the organizing structure here. The contract authority has been placed at the declared
judgment where each obligation actually belongs, so verification follows those contract surfaces instead of
reconstructing one large `before` and `after` around a method.

The pipeline consequences of that choice are easier to see after the individual contract presentations have been
introduced.

The answer is not to scatter contracts into comments, wiki pages, assertions, and test fragments. The answer is to make
the contract document real enough that the compiler can guard implementation against it.

---

## 14. Contract and Implementation

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

That rule reaches all the way down to the language.

What Contract Is is the contract discipline. Kontrakt is one machine built from that discipline. The current Kontrakt
implementation uses Kotlin/JVM as its host target. That is not a sacred fact.

Kotlin/JVM is not the contract. It is the current way this machine runs.

Another implementation may replace it only by carrying the same contract. Not the same classes. Not the same runtime
habits. The same contract.

If changing the host changes the meaning, the host was carrying authority.

Implementation machinery belongs outside the contract document.

The tool is not the contract. The obligation the tool must satisfy is the contract.

---

## 15. Message, Exposure, and Interaction

What the user sees and what the system uses to communicate internally should go through contracts.

If systems interact through implementation details, the contract gets mixed with implementation. That becomes debt.
Later, when you want to replace the implementation, you cannot do it freely because other parts of the system have
learned the implementation instead of the contract.

The user should know the public surface of the contract. The rest of the system should communicate through contracts.
Implementation should work behind the contract like a shadow. It can be powerful, complicated, and optimized, but it
should not become the
surface of interaction.

```text
user sees contract
system communicates through contract
implementation realizes contract behind the surface
```

If communication needs an implementation detail to make sense, the design is already leaking.

---

## 16. Whole Machine

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

## 17. Object Orientation and Inheritance

### 17.1 How Inheritance Fucked Up Software

Let's cut the philosophical bullshit.

Inheritance is a cheap trick for code reuse. That is the whole fucking gig. Strip away the lectures about abstraction,
taxonomy, extensibility, polymorphism, and elegant modeling. The pressure underneath is pathetic and ordinary: some code
was duplicated, and programmers were too lazy to write it twice.

Then software started treating that convenience as sacred.
Reuse stopped being a trade-off and became a religion people worshiped before asking what it would cost.

The disaster is the physical price we paid for it.
They chose the dirtiest possible shape for reuse: shove the common body up top, puke the variations down below, and
force the rest of the program to walk through this incestuous vertical structure. A few shared methods stopped being a
local hack. They became a class hierarchy.

From there, the damage spread.

Object-oriented software was already fragmented before inheritance made it worse. Behavior did not move through a clean
pipeline. Objects called other objects, methods bounced through message-shaped control flow, and both the reader and the
machine had to follow little jumps across the program just to understand what actually happened.

Inheritance dropped a second massive burden on top of that.

Now, the reader doesn't just chase object-to-object movement. They have to climb the parent-child structure: which
behavior came from the parent, which part the child butchered, which hook was meant to be called, which override
hijacked the path, and which inherited assumption still survived underneath. Callback-shaped fragmentation mutated into
ancestry-shaped recursion.

And the hardware is forced to pay the bill for this garbage.
Every time the machine tries to do something simple, it has to chase pointers through virtual method tables (v-tables)
just to find out which mutated child is actually running. It destroys memory locality. It blows out JIT inline caches
because the runtime types keep shifting. We voluntarily sacrificed CPU cycles and mechanical predictability just to save
a few lines of boilerplate.

That is a stupid amount of machinery for avoiding duplicated code.

Even the academics had to admit it was shit. Cook, Hill, and Canning proved the part that should have been painfully
obvious: inheritance is not subtyping. Stealing a parent's guts does not mean the child keeps the parent's obligations.
Implementation reuse and substitutability are completely different animals.

That should have killed the magic.
It did not.

Instead of treating inheritance like the dirty shortcut it is, OOP cultists tried to make the child safe under the
parent. Barbara Liskov's substitution principle is the cleanest version of that desperate attempt. It says, in effect,
that a subtype should be usable where the supertype was expected without breaking the program.

Useful rule.
Also a massive confession.

If your parent-child hierarchy needs a mathematical thesis to stop it from lying, then it was never contract authority.
It was a ticking bomb that needed a leash.

The Fragile Base Class problem is just reality hitting back without the theory. A base class changes its own internal
plumbing, and the child completely shits the bed without touching a single line of its own code. The parent moved inside
its private little kingdom, and the child still got fucked.

That is not abstraction.
That is parasite-level coupling wearing a clean name.

Now look at the actual bill.
For the sake of reuse, software accepted virtual dispatch, inherited memory layout, pointer chasing, parent-child
coupling, override traps, and fragile initialization paths. Then contract theory walked in and started asking how to
preserve obligations across that mess.

Wrong question.
The useful question was why that mess was allowed to carry obligations in the first place.

A contract should not have to crawl through ancestry to find meaning. It should not depend on whether an override stayed
polite. It should not trust a subtype relation just because the compiler accepted the class header. It should not pay
runtime and reasoning costs for a structure whose original purpose was saving duplicated typing.

That is the obscene part.
We sacrificed the entire fucking machine to save five lines of code.

And the goal wasn't even worth it.
Most software change is not a clean little variation under a stable parent. A policy changes. A boundary locks down. A
failure meaning splits. A state move becomes illegal. That is not "child specializes parent." That is a completely
different physical obligation.

Inheritance is bad at that kind of change because it wants the old body to remain above the new meaning. It wants the
shared implementation to stay sacred while the obligation underneath starts moving. So the child hides the shifting
reality
inside an override, the parent keeps its respectable name, and everyone pretends the tree still explains the software.

Bullshit.
The tree explains the reuse. It does not explain the contract.

Overriding does not modify a contract.

Overloading does not reuse a contract.

If the obligation changed, a new contract has appeared. If the operation shape changed, the machine needs a declared
operation surface, not another method trick hiding under the same family name.

A better contract model does not need this inheritance drama.

It does not start by hunting for duplicated code and promoting it into a rule. It starts with explicit declaration. You
declare the obligation first. If multiple interactions happen to enforce the exact same rule, you compose explicit
contract
clauses. But composition is just a mechanic; the explicit declaration is the authority.

When the obligation changes, you declare a new contract or bump a version coordinate.
When the implementation wants to avoid typing the same logic twice, it can share whatever machinery it wants—but
strictly in the dark, behind the boundary, after the contract is already locked in place.

No bloodline required.
No child class needs to cosplay as a legal variation.
No parent class gets to act like a constitution.

Hiding a changed obligations inside a child class is not abstraction.
It is contract fraud with inheritance syntax.

### 17.2 Polymorphism, Substitution, Segregation, and Inversion

Let’s get the physical timeline straight before we look at this mess.

A contract must be declared FIRST. It is the explicit, law. Because that law exists up front, you can have
ten different implementations. You can swap them, delete them, or rewrite them from scratch at 3 AM. Nobody gives a
single fuck, as long as they satisfy the contract.

That is how a sane machine works.

But object-oriented programming started with inheritance as its holy grail, completely fucking up this timeline.
Treating a parent-child implementation shape as the center of the universe is complete bullshit. Once they did that, the
machine started collapsing. The "principles" we are about to look at—Polymorphism, LSP, ISP, and DIP—are not profound
pieces of software wisdom floating down from the heavens.

They are frantic repair jobs.
They are duct tape applied to keep the inheritance lie from exploding in everyone's faces. And then, these goons had the
absolute audacity to sell their duct tape as "Design Principles."

We can leave Open-Closed mostly aside. At its best, it says a simple refactoring thing: when the obligation has not
changed, adding a new realization should not force old dependent code to be rewritten.

Fine.

The problem begins when people use that rule after the obligation has changed. If the contract changed, the contract
must be modified. Hiding the new reality inside another subclass is not extension. It is avoiding the contract change.

Polymorphism was the first trick to hide the rot.
In OOP, it let an inherited surface speak for several implementations at once. The problem isn't that multiple
implementations exist—as we established, that's perfectly normal under a contract. The problem is that OOP made the
machine ask the completely wrong question. Instead of asking the clean, rigorous question: "Does this implementation
satisfy the declared obligation?", the machine started asking the incestuous question: "Can this mutant child be treated
as its parent?" Once the inherited surface starts acting like the contract, polymorphism stops being a simple dispatch
convenience. It becomes a massive grift allowing inheritance to pretend it carries contract authority.

Liskov Substitution (LSP) is the exact same disease wearing a nicer suit.
It asks whether a child can safely stand where the parent was expected. Do you see the problem? This question assumes
the hierarchy has already won. It assumes the parent class is allowed to hold the obligations, and then writes a
mathematical thesis on how polite the child needs to be so it doesn’t hurt the parent's feelings.
What an absolute cope.
If you explicitly declare the contract first, the real question has nothing to do with replacing a parent. It’s simply:
Does this implementation fulfill the contract? If it fulfills the obligations, use it. If it doesn't, throw it in the
trash. No parent class needs to be protected. No child class needs to cosplay as a lawful variation. Implementation does
not inherit authority. It fulfills obligation.

Interface Segregation (ISP) is an absurd concept if you actually know what a contract is.
In the old world, an interface was usually just a bloated, garbage bag of methods. Because it didn't clearly state a
real obligation, people started slicing it up based on what the client happened to use. "This client calls these two
methods, so let's split the interface here."
That is completely ass-backwards.
If you explicitly declare your contract based on the interaction's actual obligations from the start, ISP is a
non-issue. A real contract surface is not dictated by which client happens to touch which method today. It is strictly
shaped by what the interaction must promise. If duties don't belong to that interaction, they shouldn't be on the
surface in the first place. ISP only exists because your initial contract was shit.

Dependency Inversion (DIP) is the most pathetic confession of them all.
The very word "inversion" proves these theorists were living in an upside-down world. The whole academic jargon about "
high-level modules not depending on low-level modules" is completely braindead. The only reason they talk about "
inverting" dependencies is because they started by worshiping concrete implementations first. It is fucking ridiculous.
They built the machine around classes and frameworks, realized the coupling was poisonous, and screamed, "Invert it!
Depend on abstractions!"
If you build a contract-first machine, the word "inversion" shouldn't even exist. There is nothing to flip. The
implementation was never supposed to be the king. It was always the slave. A concrete class doesn't magically gain
authority just to get humbled by an abstraction later. It starts below the obligation, or it doesn't enter the machine
at all.
Naming an architectural principle "Dependency Inversion" is like setting your own house on fire, putting it out, and
calling yourself a genius for discovering "Smoke Removal."

These ideas are not equally wrong in every local use. Some of them can be practical bandages when you are stuck in a
rotten, legacy codebase. Fine. Bad worlds need bandages.

But do not confuse the bandage with the body.
Polymorphism let an inherited surface cosplay as a contract. Liskov substitution desperately tried to keep children from
betraying their parents. Interface segregation chopped up weak method bags after the contract was already left implicit.
Dependency inversion tried to reverse the fact that implementation had illegally stolen the center of the architecture.

They are all orbiting the exact same original sin.

The contract explicitly carries the obligation. The implementation either satisfies it, or it doesn't. Everything else
is just academic theater trying to cover up a bad structure.

### 17.3 Abstraction

Abstraction should have been the only good idea in this mess. At its core, it is just contract work. The problem, as we
already established, is that software tried to extract these contracts from implicit implementation residue.

But the deeper failure is the explanation itself.

The definition of abstraction is abstract as hell. "Hide the details." "Expose the essence." Fine. But how the fuck do
you actually abstract? Which detail disappears? Which part becomes the strict contract? The academics abstracted the
explanation of abstraction. They left a massive void, and nobody actually knew what to do.

Because the definition was empty, object-oriented programming filled it with inheritance.

The rigorous work of defining a physical boundary mutated into the lazy act of building a parent class. This is how it
is still taught today: developers learn abstraction right next to inheritance. The lesson becomes simple and fatal: to
abstract is to build a parent.

That lesson is pure poison.

Engineering is driven by purpose. There is no universal, divine shape floating above the machine. Very few developers
understand this. Most just memorize the first inheritance pattern they see. Years pass, they get a "Senior" title, and
suddenly they act like Jesus Christ handing down sacred laws. They preach this same brain-dead bullshit to the next
generation, ensuring the industry stays completely fucked.

Abstraction is not the enemy.
Implicit, inheritance-shaped abstraction is.

### 17.4 The JVM Is What Happens When Implementation Becomes Contract

This is not just a theoretical debate.

The JVM is the ultimate proof of what happens when implementation shape is allowed to become platform contract.

From its birth, Java failed to keep the two cleanly separated. It took the class/object/reference model of
object-oriented programming and elevated that machinery into public law. It exposed class files, classes, objects,
references, identity, virtual dispatch, boxed primitives, and arrays of references as the world Java programs could
depend on.

Over time, that model stopped being just implementation machinery.

It became the fundamental law.

That is the trap.

Once a platform exposes implementation as contract, the implementation can no longer freely move. Every future
optimization must crawl around the old promise.

Project Valhalla is the bill arriving decades later.

The problem Valhalla attacks is physical. A system cannot perform at its peak when every small value has to drag pointer
chasing, identity machinery, heap allocation, and reference-shaped storage everywhere it goes. A date, an integer
wrapper, an optional value, or a small immutable domain value does not need object identity. The machine would breathe
easier if those values could be stored flat, dense, and cheap, closer to primitives.

To truly escape the trap, the JVM would have to admit the ugly thing plainly:

```text
Object identity was the wrong default.

Boxed values should never have been ordinary identity objects.

Reference layout should not have been the universal surface.

The physical class/object structure should never have been the contract everyone depended on.
```

Admitting that would be honest.

It would also break the world.

So the platform has to do the painful thing instead. It adds new categories. It gives some classes a way to opt out of
identity. It tries to grant the virtual machine more freedom to flatten data, improve locality, and reduce allocation,
while desperately avoiding invalidating old programs.

That is why Valhalla cannot be a clean fix.

It is not replacing the old contract. It is negotiating with it.

A mechanical optimization that should have been straightforward becomes a decade-long reconstruction project because
the platform cannot simply decouple representation from the public model. Every step forward must preserve the old world
well enough that existing code still believes the same machine is underneath it.

The burden of time makes it worse.

Java is not only a language. It is decades of binaries, libraries, frameworks, reflection tricks, serializers, agents,
bytecode generators, application servers, and production systems. Java 8 code still exists. Java 11 code still exists.
Java 17 and 21 systems will remain for years. Some systems will not move because they cannot. Some will not move because
nobody wants to touch them. Some will run until the companies that built them die.

That means the old contract keeps voting.

This is the true cost of treating implementation as contract. You do not merely make today's machine ugly. You force
tomorrow's machine to negotiate with yesterday's mistake.

Valhalla is not proof that the JVM engineers are incompetent.

It is proof that once a platform lets implementation become contract, even brilliant engineers can spend more than a
decade trying to buy back representation freedom without breaking the world, and still not be finished.

That is the lesson.

Rip the contract completely out of the implementation.
When the contract stands as the absolute center of the machine, the implementation is reduced to a mere backend—a
mechanical detail you can swap out, rewrite, or discard at any time.
But if you bind your system's legal authority to the physical layout of its memory, you permanently destroy that
freedom.
Keep the contractual rules and the memory structure strictly isolated. Fail to do this, and you will spend decades
paying the massive engineering cost of trying to escape a structural nightmare you locked yourself into.

### 17.5 Type Is a Contract Name, Not Contract Authority

Type theory is too massive to settle here. I am not writing a thesis on every branch of it, nor am I pretending to pass
final judgment on the entire field. That would be dishonest and miss the point entirely.
The parts of type theory that matter to this model will need separate treatment later.

This section only needs to establish two things: what a type means in this machine, and why subtyping must never be
treated as contract reuse.

Type is the next respectable place where software engineers pretend they have found a contract. It looks safer than a
class. It looks cleaner than an object. It wears the intimidating costume of pure mathematics. Because the compiler can
use it to reject static nonsense, developers blindly elevate it to the level of ultimate authority.

That is a delusion.

Use the type system. It is a good tool. But never confuse the tool with the authority.

A type is a classifier. It is a static use rule that tells the compiler, "This thing may be treated as this kind of
thing here."

In this document, a type may be a contract name, a static surface, or a handle for later judgment. Names like OrderId,
AcceptedFact, or DiagnosticEvidence are useful because they point at different contract surfaces.

But the name does not create the obligation.

A type named OrderId does not tell the machine how to validate raw input, what the canonical identity law is, or how it
must fail when rejected. A type checker proving that a phrase fits a static surface does not prove state movement,
failure meaning, or publication authority.

The type points.

The contract must declare.

Subtyping becomes the same trap when people let it stand in for contract preservation.

A subtype relation says that one type may be used where another type is expected under the type system's substitution
rule. Whether that machinery is nominal, structural, inferred, bounded, intersection-shaped, or union-shaped, it is
still a mechanical permission rule.

That is useful.

It is not contract reuse.

Subtype acceptance is not contract permission.

If an obligation changes, the contract has changed. You do not get to hide a semantic mutation by slipping it through as
a child type, a narrower generic bound, or a clever intersection alias. A changed obligation demands a different
contract, a new version coordinate, or an explicit compatibility rule.

This does not make types useless.

They are excellent handles. They catch garbage early. They can carry names, shapes, bounds, and equality material that
may help later judgment.

But useful material is not authority.

A type-level equality check may help compare two presented surfaces. It does not decide that the contract obligation is
the same. A subtype relation may help decide that one static surface can stand where another was expected. It does not
decide that one contract has preserved, inherited, or reused another contract.

If type machinery is allowed to affect contract meaning, it must be judged under declared contract material first.

The contract carries the declared obligation.

The type carries the name, surface, or static handle.

Keep those two strictly separated. If you let the type system act as the authority, the old disease of object-oriented
inheritance will return. It will just have more complicated math to hide behind.

### 17.6 Class Is Where Roles Collapsed

I respect a class for its exact mechanical purpose—nothing more, nothing less. When you need to organize implementation,
define the physical shape of an object, or hand the runtime a construction template to instantiate, a class does the
job. None of that is the problem.

The disaster starts the moment you ask a class to carry contract authority.

Historically, a class defined the shape of objects that held mutable data, exposed behavior through methods, and
participated in message-driven control flow. Developers then looked at that massively overloaded construct and treated
it as the natural place to define software contracts. That is exactly where the roles collapsed.

This was not an accidental bug. The object-oriented model intentionally bound data and behavior together. It wanted
mutable local cells communicating through distributed control. The resulting damage is not a side effect of bad
programming; it is the physical cost of the model doing exactly what it was designed to do.

By jamming data and the operations that mutate it into the same artifact, mutation stops being a rare accident and
becomes the default reality. Once you accept that, you inevitably hit the "class invariant" problem. If an object is
allowed to scramble its own variables, you desperately need a consistency rule to ensure the data still makes logical
sense afterward.

The Design by Contract practitioners tried to formalize this rot by enforcing preconditions, postconditions, and class
invariants. But they made a fatal compromise: they tied the contract directly to the receiver object and the method
execution boundary.

That local boundary is useless against the reality of the cell-shaped model. If software is a network of objects
hoarding mutable data and passing references, callbacks and dynamic dispatch are not edge cases—they are the natural
movement of the system. Because the contract is tied to the receiver object, the moment a callback, proxy, or leaked
reference physically bypasses that expected boundary, the contract goes blind.

You cannot use a local restriction to govern a distributed control path. The invariant sits blindly on one class, while
the actual meaning of the system leaks everywhere through hidden, implicit links. It shares the exact same architectural
flaw as a neural network: meaning is distributed across the graph instead of being declared on one explicit machine
surface.

No wonder the academic repair work never ends. Encapsulation tries to hide the mutating variables, invariants try to
keep them sound, and callback rules or ownership types try to stop the graph from collapsing under its own weight. These
are heavy bandages placed on a broken substrate. Add enough restrictions, and the object model's reason for existing is
destroyed. Add too few, and it cannot safely carry contract authority.

The machine we are building here does not waste time trying to make an inherently flawed object graph honest. We
surgically remove contract authority from the object entirely.

There are two strict, physical reasons for this absolute line.

First, a class almost never declares its obligations with enough authority. Attaching a name to a class and checking an
invariant against mutable fields tells the machine absolutely nothing about admission, lowering, state transitions,
failure modes, budgets, or governance.

Second, whatever tiny fragment of contract meaning does appear is tied far too tightly to the actual plumbing. The data
layout, method bodies, callback paths, and invariant checks all sit inside the exact same artifact.

When the supposed contract is expressed through that artifact, a simple change in the implementation plumbing can
completely shake what people thought the contract actually meant. And if a contract shakes just because the underlying
plumbing moved, it was never an authority in the first place. It was merely a byproduct of the implementation.

A class is allowed to be useful mechanical machinery.
It is not allowed to be the authority.

### 17.7 Rust Exposes Implementation as Contract

This is not a debate about whether Rust is a good or bad language. That is entirely irrelevant. The only architectural
question that matters is whether the contract and the implementation remain strictly isolated.

In a good machine, the contract is the absolute communication surface. The implementation is nothing more than a
swappable backend. If the hardware, the compiler, the memory model, or the verification method changes, you must be able
to discard the implementation and replace it entirely, as long as the declared contract still holds.

Rust fails to keep that line clean.

It takes low-level implementation rules—ownership, borrowing, lifetimes, trait bounds, pinning rules, sendability,
and aliasing—and exposes them directly as the public contract. Yes, inside the Rust compiler, those rules are exactly
how the implementation is controlled. But architecturally, they are still just implementation. They are not the
contract.

Look at a public Rust API. It rarely just states what obligation must hold. Instead, it dictates the exact shape of the
current implementation: who owns, who borrows, which lifetime is attached, which trait bound is required, which unsafe
invariant is hidden, and exactly how memory is allowed to move.

The implementation has merged directly with the contract surface.

Once the public ecosystem learns and depends on those exact implementation details, the implementation ceases to be
free. A
future hardware architecture, compiler strategy, or memory model might demand a completely different backend, but you
are stuck. You cannot simply swap it out, because the public surface has already locked the users into depending on
those old implementation details.

The JVM did exactly this with classes, object identity, references, inheritance, and virtual dispatch.
Rust is doing the exact same thing with ownership, borrowing, lifetimes, traits, unsafe invariants, and aliasing rules.

Different implementation. Same architectural leak.

If Rust expects to survive the next generation of hardware and software models, it must strictly isolate its contract
from the implementation rules that currently enforce it. The contract declares the obligation. The implementation
remains a hidden, replaceable detail behind that surface.

Otherwise, Rust is going to age exactly like the JVM.
Not because it lacked intention.
Because it made its implementation too public to ever replace.


---

## 18. Current Working Definition

For now:

```text
A contract is the declared set of obligations software must satisfy.
```

And the machine I want is simple in spirit:

```text
state the contract
make hidden obligations explicit
make the interface a real contract document
make the interface surface a public reliance boundary
preserve methods as operation handles
keep implementation machinery out of the surface
keep contract structure two-dimensional
bind selected interface surfaces, closed contract presentations, and required coordinates through a flat interaction manifest
treat everything outside the core as untrusted
adopt external interfaces only after ratifying them against internal contracts
judge DTOs as outside presentation at the boundary
admit only what passes admission
canonicalize declared-equivalent admitted material into the system's stable internal standard before identity becomes authoritative
lower canonical representation into core-owned candidate material
accept candidates only under pipeline-bound invariants and their declared acceptance laws
produce contract-governed immutable facts
treat state as explicitly declared contract material for governing the legality of the next machine move
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