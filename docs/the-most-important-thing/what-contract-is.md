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

The most important thing in software is intention.
If you miss what the machine is for, the rest is just machinery you happen to know how to use.

I am describing the contract model that fits my intention.

It will not fit every software system, every team, or every engineering goal. The limitation is intentional. A model
that pretends to fit everything usually says nothing useful.

so I'm trying to say what kind of machine I want to build, and why the machine has to be shaped this way.

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

An aircraft is built knowing that parts will fail.
It carries instruments and records so a failure can be found and dealt with instead of guessed at afterward.

A power system is built knowing exactly how much it can take.
It protects itself before that limit turns into damage.

Serious engineering does not pretend these problems away.
It makes them visible and builds the machine around them.

Software should be held to the same standard.

A good machine should make the conditions that govern it explicit and deal honestly with the limits and failures of the
real world.

Outside material should not get to define its inside, and the machine should not move simply because the implementation
happens to let it.

It should behave consistently under the same declared conditions, leave enough evidence to explain what happened, and
let the machinery underneath change without changing what the machine promised.

Software is no exception. Most serious architectures already contain these ideas, but software has gotten very good at
tearing them apart and hiding each piece somewhere in the implementation. We got so used to that that implicit structure
started to look normal.

Software did not end up this way by accident.

As software evolved, it started serving different intentions. Mathematical elegance made recursive forms attractive.
Object-oriented programming put reuse and substitution ahead of keeping the machine flat and obvious. Frameworks made
hidden behavior through proxies, reflection, and interception feel normal. Callbacks and dynamic dispatch taught us to
accept control flow we could no longer see directly.

I think that was the wrong direction.

We optimized software for elegance, reuse, and abstraction when a good machine needs almost the opposite. Its rules
should be visible. Its movement should be understandable. Its limits should be known before they become failures.

We pushed software in the opposite direction from engineering.

That is the direction I want to reverse.

I want software to become a good machine again.
Engineering starts by facing the real obligations of the machine instead of hiding them inside the implementation.

Those obligations need to be explicit enough that we can actually see what the machine is supposed to be.
That explicit form is what I call Contract.

That is the point of this document. Contract is how I want to bring software back to engineering.


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

I mean that literally.

The machine has an obligation before we decide how to realize it. We choose an implementation to fulfill that
obligation, not the other way around.

That matters because an implementation comes with its own shape. A class may inherit. A framework may hide behavior. A
type system may build deep structure. Fine. Those are ways to build the machine. They are not the Contract.

The trouble starts when we copy that shape back into the Contract. One Contract gets its meaning from another, which
gets its meaning from another again. Before long, we are tracing a family tree just to find out what the machine
actually owes.

That is not a clearer Contract. It is the same hidden structure with better branding.

A good machine should do the opposite. Its obligations should be explicit enough to stand on their own, and rich enough
to say what actually matters.

That naturally gives us two things. We need the individual Contracts that state those obligations, and we need one place
that shows which Contracts govern the interaction we are looking at.

That is the role of the manifest.

So contract structure has to stay two-dimensional.

The first dimension contains closed Contract presentations and the coordinates they require:

```text
interface contract and its public surface
input presentation contract
canonicalization contract
admission contract
lowering contract
fact contract
invariant contract
state contract
state transition contract
explicit state machine manifest
failure contract
publication contract
output presentation contract
diagnostic evidence / retention contract
version coordinate
policy / budget / capacity / governance contract
```

The second dimension is the interaction manifest.

It does not become another parent Contract above them. It simply says which closed Contracts govern one interaction. It
simply says which of those closed Contracts govern one
interaction. That gives us one place to see what the interaction owes without making the Contracts depend on one
another.

Some of those Contracts are standing laws rather than something recreated for every interaction. Fact defines what
factual material may stand inside the core. Invariant defines the integrity law that factual material must continue to
satisfy. The manifest names them because they govern the interaction, not because the interaction owns or redefines
them.

The state machine manifest follows the same rule of flatness. It makes the legal movement between declared states
visible. It is not a
parent sitting above State and Transition, and it does not gain meaning by pulling in another state machine.

This is why the structure stays flat.

```text
interface inheritance
interface composition
same-kind contract inheritance
same-kind contract composition
recursive manifest
hidden transitive obligation
cyclic contract reference
```

If understanding one interaction means tracing a chain of other Contracts just to discover what it owes, the Contract
has already stopped being explicit. That is genealogy again, no matter what name we give it.

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

Engineering exists to achieve a purpose.

You build a machine because you want something specific to happen in the real world. Everything that follows should
serve that end.

That purpose shapes the choices you make. It decides which obligations matter, which trade-offs are acceptable, and
which Contracts the machine needs.

So a good machine cannot be a pile of individually reasonable decisions. Its choices have to stay aligned with the
purpose it was built to serve.

The designer chooses the Contracts. The implementation realizes them. Both exist to make that purpose real.

A machine that is internally consistent but drifts away from its purpose is still bad engineering.

But the software itself is utterly indifferent to your purpose.

A machine cannot discover your intention. It cannot mathematically prove it. It cannot enforce an abstract human desire.
Human purposes are too contextual, too fragmented, and too dependent on judgments that live outside the machine. If the
purpose has not been lowered by the designer into hard, machine-readable obligations, the machine has nothing honest to
judge.

The designer's intended purpose must be expressed through declared obligations and aligned consistently across the
system.

The machine can only judge what has been explicitly declared. The designer's purpose has to shape those obligations
consistently across the system.

But turning purpose into Contract does not mean turning purpose into mathematics. The Contract states what the machine
must do to serve that purpose. It does not mathematically encode the purpose itself.

The machine may judge whether those obligations hold. It does not understand the human purpose behind them, and it
certainly does not prove that purpose.

The rot starts when proving the purpose becomes the goal.

Software does this all the time. Type theory gets pushed until it is supposed to encode the purpose itself. Proof work
starts cutting away the ugly parts of the real machine until there is something neat enough to prove. Frameworks observe
behavior and start treating that observation as authority. Now AI can generate an interpretation and pretend it
discovered what the system was meant to do.

That is proof theater.

The problem is not that these tools exist. The problem is that the direction has flipped. We started with something we
wanted a real machine to achieve, then slowly changed the job into proving a model of that purpose.

A Contract is not a mathematical replacement for purpose. It is the explicit set of obligations the machine must satisfy
in order to serve that purpose.

Once proving the Contract becomes more important than building the machine the Contract was meant to serve, engineering
has already lost the plot.

A purpose guides which Contracts the designer chooses and how they fit together. The Contracts then state the
obligations the machine must actually satisfy.

If the purpose calls for predictable behavior, the designer may choose obligations that keep the machine consistent
under the same declared conditions. The purpose itself has not become a Contract or a proof. It has simply shaped what
the machine is required to do.

The same applies to any other purpose.

A machine built for safety will not make the same choices as one built for low latency. A privacy-sensitive system will
draw different lines again. The end changes, so the obligations change with it.

The machine does not discover that end for itself. The designer turns it into Contracts that keep the whole machine
pointed in the same direction.

So the line is absolute:

```text
purpose guides contract selection and arrangement
contract declares obligations
implementation realizes those obligations
```

If the purpose has not been lowered into explicit obligations, the machine has nothing to judge about that purpose.

And even when those obligations are satisfied, that does not mean the machine has proved the purpose itself. The
implementation is not proof either. It is only one way the machine happened to fulfill the Contract.

The moment we use the implementation to explain what the Contract really means, the Contract has already been swallowed
by the implementation.


---

### 6. Make Interfaces Great Again

An **Interface** is a Contract defining the interaction surface of a machine.  
An **Operation** is a Contract defining an action available through that surface.

```text
Interface Contract:
    Declares the interaction surface of the machine.

Operation Contract:
    Declares an action available through that surface.
```

Software has relied on this relationship in a familiar structure for decades.

```kotlin
interface OrderPort {
    fun submit(command: SubmitOrderCommand): SubmitOrderResult
}
```

We establish an interaction surface like `OrderPort` and make an action like `submit` available through it. An
interaction participant provides the command and receives the result. There is no need or reason for that participant to
know how the machine realizes the interaction internally.

Originally, this gave us an outstanding boundary.

The problem started when people began defining an Interface mainly as some kind of **"abstract wrapper around an
implementation."**

An Interface can hide implementations, but hiding details is not what gives it meaning as a Contract. The participant
does not need to know the realization because the machine has already declared what it promises at that interaction
surface.

**Obligation comes first. Realization is subordinate to it and has to satisfy the declared Contract without changing
what the Contract means.**

```text
Contract
    ↓ obligates
Realization
```

Contracts can also be discovered from existing codebases. Several implementations may already follow the same rule, and
writing that rule down can turn an implicit understanding into an explicit Contract.

Once the Contract is declared, though, its meaning no longer comes from those implementations. You can rewrite the
realization, move it to another platform, replace the technology underneath it, or throw the old implementation away
entirely. If the obligation stays the same, the Contract stays the same.

This matters because "abstraction" is often used as an excuse to leave the actual obligations vague.

Suppose an interaction is governed by ten rules, but the Interface only makes two of them explicit. The other eight do
not disappear. They end up scattered through annotations, middleware, configuration, callbacks, framework behavior, or
simply left in somebody's head.

A codebase can be full of Interfaces and still fail to answer a very basic question:

> **"What actually governs this interaction?"**

At that point the Interface still exists, but the interaction Contract has become implicit.

And interaction is the whole point here.

A machine does not merely expose a method. It accepts something from another participant, decides what that interaction
means, decides whether and how it may continue, performs an act, and eventually produces something that may or may not
become visible outside the machine.

A good machine has Contracts governing those things.

The exact Contracts are discussed later. What matters here is that they do not stop being part of the interaction just
because the method signature only shows an input and a result.

That surface can stay simple:

```text
input
    ↓
operation
    ↓
output
```

but the interaction itself can still be governed by a rich set of machine obligations.

We should not solve that by stuffing every obligation into the Operation Contract. Those Contracts have their own
meanings and their own authority.

What the Operation needs is one explicit declaration showing which of those independent Contracts govern that act.

That declaration is the **Interaction Manifest**.

```text
Interaction Manifest:
    Declares which independent Contracts govern one Operation.
```

For example:

```text
submit(...)
    -> interface contract
    -> input presentation contract
    -> canonicalization contract
    -> admission contract
    -> lowering contract
    -> fact contract
    -> invariant contract
    -> state contract
    -> state transition contract
    -> explicit state machine manifest
    -> failure contract
    -> publication contract
    -> output presentation contract
    -> diagnostic evidence / retention contract
    -> version coordinate
    -> policy / budget / capacity / governance contract
```

The Manifest does not absorb these Contracts, merge them into the Operation, or become the authority over what they
mean. It simply makes the relationship visible.

When `submit` happens, these are the Contracts governing that interaction.

That is the part Interfaces should have been helping us see all along.

The interaction participant can still use a very small surface. It should not need the entire machine dumped in front of
it just to invoke one Operation.

A good machine can have a lot going on inside one interaction. The participant does not need all of those internal
processes dumped onto the surface just to use it.

This is what got lost when Interface became mostly a conversation about implementation abstraction.

We spent too much time asking what an Interface hides and not enough time asking what kind of interaction it declares.

That is backwards because Interface was already a natural place for interaction.

It gives the machine a visible surface. Operations give that surface behavior. The Manifest lets one Operation point to
the full set of Contracts governing that act without destroying the independence of those Contracts.

Now the Interface becomes useful again in the way it was meant to be useful.

It gives a participant a clear place to interact with the machine, while giving the machine a clear place to say what
that interaction actually means.

The participant can still get a simple interaction surface without forcing the machine to dumb down the Contracts that
govern everything happening behind it.

There is no reason to throw Interfaces away, invent another architectural pattern to recover the interaction boundary,
and then spend the rest of the system trying to figure out where the real interaction went.

The interaction meaning of Interface was already there. We just let it fade behind a much louder story about
abstraction.

Bring that meaning back.

Make the Interface explicitly about interaction again.

Let the Operation describe the act available through that interaction, and let the Manifest show the Contracts governing
it.

Then Interface can do what it was designed to do in the first place: give software a clear, explicit Contract for
interacting with a machine.

Reducing Interfaces to superficial wrappers around implementations was **fucking backwards**.

Restore the interaction.

Make Interfaces Great Again.

### 6.1 The Surface of an Interface Contract

An Interface Contract needs a surface.

The surface is the part of the Contract an interaction participant actually uses and is allowed to depend on. It should
give the participant everything needed to use the machine correctly without making them learn how the machine happens to
work inside.

This is basic engineering almost everywhere else.

You do not learn the drivetrain before driving a car. You do not study the wiring diagram before operating a machine.
You do not need to understand quantum mechanics, semiconductor physics, memory controllers, or file systems just to use
a smartphone.

Making complicated machinery easy to use is part of building the machinery well.

Software gets fucking weird here.

Somehow, memorizing implementation details became a measure of developer skill. People learn framework lifecycles,
runtime quirks, hidden ordering rules, undocumented behavior, internal classes, configuration tricks, and whatever other
shit happens to make the thing work today. Then we look at the person who remembers the most of it and call them an
expert.

If they are actually working on the machine itself, that makes sense. Someone debugging the runtime, modifying the
framework, optimizing the implementation, or building the next version obviously needs to know how the machinery works.

But needing all of that just to use the thing is not expertise. It means the tool is badly made.

From the side of the person building the tool, that should be embarrassing. I gave you a surface because I wanted you to
use the surface. The implementation behind it is my problem. I should be able to tear it apart tomorrow and replace half
of it without forcing everyone using the machine to relearn how the machine works.

If I cannot do that because my users had to memorize the internals just to operate the thing, then I did not build some
wonderfully sophisticated tool for advanced engineers.

I built a shitty tool with a shitty surface.

Nobody thinks a car is impressive because the driver has to understand the gearbox before moving forward. Nobody thinks
an industrial machine is well designed because the operator needs the wiring diagram beside the controls. Nobody praises
a smartphone because you need to understand semiconductor behavior before making a call.

Software should not get a special exemption from basic engineering just because we got used to bad surfaces.

A surface should tell the participant what the machine offers and what can safely be relied on while using it. The
machine may have a huge amount going on behind that interaction. It may carry rich Contracts, complicated judgments,
state, resources, failures, and whatever else the machine actually needs. None of that means the participant should have
to carry the machinery around in their own head just to use the thing.

The participant can still get something simple:

```text
input
    ↓
operation
    ↓
output
```

That simplicity is not permission to make the Contract vague.

If something matters to the participant when using the machine, it belongs on the surface in a form the participant can
rely on. Hiding an actual obligation somewhere inside the implementation does not make the surface cleaner. It just
means the surface failed to say something important.

A good surface exposes enough of the Contract for the interaction to be used correctly and leaves the machinery that
realizes that Contract where it belongs.

That is what lets the machine change without dragging everyone using it along for the ride.

Change the algorithm. Replace the storage. Rewrite the realization. Move the whole thing to another platform if you
want. As long as the Contract has not changed, the interaction participant should not have to give a shit.

The participant gets something stable and understandable to use, while the machine keeps the freedom to improve
underneath it.

That is what the surface is doing.

It is not just a pretty API or a thin layer covering the "real" system. It is the part of the Interface Contract through
which another participant actually deals with the machine.

If using your machine requires learning its guts, you made a bad surface.

If you deliberately made it that way and think the required internal knowledge proves how sophisticated your engineering
is, you did a fucking bad job.

A good surface lets the participant depend on the Contract without giving a single fuck about the machinery behind it.

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

### 9. Core, Boundary, and the Fucking Bastards Outside

A good machine needs somewhere it can speak in its own terms. We call that its Core. Inside it, the machine uses its own
representations, operations, and Contracts.

Anything from outside has to cross a Boundary before it can enter that world, and that boundary has to be strict because
there are fucking bastards outside it.

Users do not use programs the way you hoped. Some send garbage by accident, some are careless, and some are actively
trying to break the machine. Attackers look for the gap between what the machine accepts and what it actually
understands—give them one crack and they will keep hitting it.

Outside material does not get trusted just because it arrived in a valid type. Before it enters the Core, the machine
has to inspect it, judge it, and turn it into something the Core understands in its own terms. If JSON comes in, the
Core does not become JSON. If a database gives us a row, that row does not automatically become our domain
representation.

Outside material comes in speaking somebody else's language. The Core should speak ours.

```text
Outside material
    -> inspect
    -> judge
    -> translate
    -> machine representation
    -> Core
```

This takes work. You have to define your own representation and write the translation. Sometimes it feels much easier to
pass whatever the framework or database gave you straight through the system. A lot of software does exactly that, which
is also how you end up with a Core that belongs to everything except you.

Doing the annoying work gives the machine control over what its own world means. If foreign material can walk straight
through the Boundary and stay foreign all the way into the Core, the Boundary is bullshit.

Users and attackers are not the only fucking bastards outside it. Libraries are outside, frameworks are outside, and the
language we used to build the machine is outside too. People forget that one all the time.

Java is not my machine. Kotlin is not my machine. The JVM is not my machine.

A language feels different because the whole system is written with it. Its standard library is always there, so its
types and behavior start feeling native. They are not—they belong to the language and the platform.

You see people talk seriously about DDD or Hexagonal Architecture, put a clean adapter around a database, and then let
platform types and runtime behavior spread through the entire Core. They noticed one outside dependency and forgot the
one sitting under every line of code. Something does not become ours because it ships with the language or because we
have used it for so long that we stopped noticing it.

Outside is outside. That does not mean we stop using outside things, as trying to build everything ourselves would be
stupid. It means we know what belongs to our machine and what we merely use to build it.

The same principle applies to Contracts. Every dependency brings rules with it—sometimes those rules are exactly what we
want, like a protocol with promises we are happy to depend on or a platform with behavior we deliberately choose to rely
on.

Giving somebody else's Contract authority over our machine should always be a conscious decision. Look at what it
promises, look at what can change, and then decide whether to keep it outside and translate at the Boundary or
deliberately accept it as a dependency of our machine.

What is unacceptable is letting a foreign Contract spread through the Core simply because calling the external API
directly was easier. When the Core quietly starts assuming somebody else's lifecycle or error meaning, external Contract
infiltration has occurred. If we chose that dependency knowingly, there is no problem, but if nobody chose it and the
Core still answers to it, the Contract has been contaminated.

```text
External dependency
    -> may provide machinery
    -> may provide an external Contract

Reviewed and declared dependency
    -> legitimate Contract relation

Foreign Contract silently deciding Core meaning
    -> Contract infiltration

Our meaning now depends on that undeclared authority
    -> Contract contamination
```

"Dependencies are dangerous" is useless advice. Of course they are dangerous—the point is knowing *what* we actually
agreed to depend on.

That is why dependency management is part of system quality. A machine that knows where a foreign Contract enters keeps
the freedom to replace it or accept it deliberately. A machine that lets foreign assumptions spread everywhere loses
that freedom.

If an external Contract changes and we depend on what changed, something real changed for us too. We deal with that
honestly.

But if the Contract stayed the same and an implementation update still breaks our machine, we were depending on an
undocumented ordering rule, a serializer quirk, or some other implementation accident we were never promised in the
first place.

That is a fucking ticking time bomb sitting quietly until somebody upgrades a library or runtime. When the machine blows
up, the dependency did not betray us—we were just stupid enough to build domain meaning on top of something it never
promised.

Importing an external interface does not make it our Contract either. Another system's Contract is a lot like another
country's law: we can recognize it and explicitly agree to depend on parts of it, but we do not replace our own
constitution just because importing the package was convenient.

```text
My Contract
    -> explicitly depends on
External Contract

External Contract
    -> does not automatically become
My Contract
```

Use what you need, declare the dependency, and stay clear about who owns the meaning.

And do not get too comfortable pointing at everybody else outside the wall.

**We are somebody else's fucking bastard outside, too.**

The moment another machine uses our API, we become part of its outside world. If we leak internal identifiers or
accidental behavior through that surface, someone out there *will* build on top of them. Then we refactor our
implementation, and their machine breaks.

We can yell *"that was never part of the Contract!"* all we want, and technically we might be right. But we still gave
them a shitty surface if our internal garbage looked stable enough to depend on. Undeclared behavior does not become a
Contract just because somebody saw it, but exposing internal garbage still creates compatibility debt and gives other
systems something to depend on that we never meant to guarantee.

Every machine is an "outside" to someone else. Machines should meet through declared Contract meanings—not by letting
their implementations bleed into each other until nobody knows who owns the rules anymore.

```text
Machine A Contract
    -> declared outward meaning
    -> explicit dependency relation
    -> Machine B Contract
```

The fucking bastards outside are dangerous in the other direction too. Engineers usually picture an attacker throwing
malicious Input at the machine, but that is only half of it.

The exact same bastard can sit outside and carefully watch what comes back. Maybe two errors reveal different internal
states, or maybe the response size changes depending on a secret. Security history is full of attacks built from tiny
outward differences like these.

An attacker does not need you to hand over the database. Sometimes one bit at a time is more than enough.

Anything visible from outside can reveal something about what happened inside. Payload matters, but payload is far from
the only thing that talks. Internal knowledge is not public knowledge, and internal material is not automatically an
Output. Just because something is useful inside the Core does not give the fucking bastards outside permission to see it
or depend on it.

> *"The machine knows this internally"* is **never** a good enough reason to return it.

Only the outward meaning the machine is actually allowed to claim should cross that line. Once something has gone
outside, sending it back does not magically preserve its old authority—it goes through the Boundary again with no
fucking VIP entrance.

A Boundary is not just a filter for bad Input. Going in, it stops foreign material and foreign Contracts from becoming
native by accident. Going out, it stops internal meaning from leaking into places where it was never supposed to matter.

```text
Outside -> Inside
    inspect
    judge
    translate into the Core's representation
    admit what the machine accepts

Inside -> Outside
    form declared outward meaning
    expose only what may be relied on
    keep internal meaning inside
```

Building a Boundary this way takes work, because building a good system takes work.

```text
Invariants:
    1. Outside material does not become Core material just because it arrived.
    2. Foreign material must enter the Core in a representation the machine owns and understands.
    3. External Contracts are reviewed before they are allowed to govern the machine.
    4. Legitimate Contract dependencies are declared explicitly.
    5. Foreign implementation accidents never become Core meaning.
    6. Internal meaning does not become public meaning by convenience.
    7. Output is an active part of the Boundary too.
    8. Our machine is also someone else's fucking outside.
```

Boundary design is Contract design.

If you do not know what crossed the line, how your Core came to understand it, which foreign Contract you chose to
depend on, or what the other side is allowed to learn from you—you do not have a Boundary.

You just drew a fucking line on an architecture diagram.

---

## 10. Applying Contract to a Good Machine

From the principles established so far, the characteristics of a good machine can be summarized as follows.

1. **A good machine serves a declared purpose.**  
   Its Contracts and engineering choices exist to serve the purpose for which the machine was built.

2. **A good machine faces reality.**  
   Failure and crash are expected and prepared for. Time, memory, capacity, dependencies, and the useful life of
   software are finite. The machine should know the limits it can govern and remain honest about where its control ends.

3. **A good machine remains function-like where reality permits it.**  
   Under the same declared conditions, the machine should behave consistently and predictably rather than changing its
   behavior through hidden or accidental conditions.

4. **A good machine has a meaningful middle and owns it explicitly.**  
   Unlike a mathematical function, a real machine has an intermediate process, and that process matters to engineering.
   The important causal structure of the machine should remain visible rather than disappearing into callbacks, proxies,
   hidden dispatch, or other implicit implementation flow.

5. **A good machine represents its condition richly, accurately, and honestly.**  
   Its State, legal movement, failures, judgments, and diagnostic evidence should describe what is actually happening in
   the machine rather than forcing that condition to be reconstructed later from implementation residue.

6. **A good machine separates Contract from Implementation.**  
   Contract declares the obligation. Implementation realizes it. The realization may be replaced, optimized, or
   rewritten without changing Contract meaning as long as the same obligations remain satisfied.

7. **A good machine owns its Core and its meaning.**  
   The Core speaks in machine-owned representations and Contracts. Language, runtime, framework, external
   representation, and foreign Contract meaning do not become native authority merely because they are convenient to
   use.

8. **A good machine controls both sides of its Boundary.**  
   Outside material does not become Core material merely because it arrived, and internal meaning does not become public
   meaning merely because the machine knows it.

9. **A good machine makes authority and control explicit.**  
   Authority should come from the Contract that owns a judgment, not from the fact that some implementation happened to
   execute, return, mutate, or expose something.

10. **A good machine respects physical engineering.**  
    Mathematical elegance and abstraction do not excuse a machine that is too slow, too expensive, too wasteful, too
    unpredictable, or too difficult to operate for its purpose. Implementation mechanisms remain replaceable, but the
    machine must satisfy the physical obligations required by the purpose it serves.

11. **A good machine remains usable from the outside.**  
    Rich internal Contracts should not force an interaction participant to understand the machinery behind them. The
    interaction surface should expose what the participant may rely on while keeping the realization behind that surface
    replaceable.

These properties cannot be expressed honestly through one method signature or one general-purpose Contract wrapped
around an implementation.

Different parts of the machine carry different obligations, so those obligations need distinct Contract authority.

At the level developed so far, that gives us the following Contract structure:

```text
Interface
    declares the interaction surface

Operation
    declares an act available through that surface

Interaction Manifest
    declares which independent Contracts govern one Operation

Input Presentation
    declares the closed form in which outside material may be presented

Canonicalization
    declares the stable machine-owned representative of declared-equivalent presentation

Admission
    declares whether presented material may continue

Lowering
    declares how admitted material becomes Core-owned candidate material

Fact
    declares what factual material may stand inside the Core

Invariant
    declares whether candidate factual material may obtain factual authority

State
    declares the machine condition governing legal movement

Transition
    declares legal movement between States

Explicit State Machine
    declares the complete State and Transition surface

Failure
    declares contract-governed refusal or failure

Publication
    declares what established internal meaning may become an outward claim

Output Presentation
    declares the closed outward form of an authorized claim

Diagnostic Evidence / Retention
    declares what explanation may be produced and retained

Version
    identifies the Contract meaning under which material and judgments exist

Policy / Budget / Capacity / Governance
    declare the criteria, limits, and validity governing the machine
```

These Contracts do not form an inheritance tree, and they do not become one large Contract owned by the Operation. They
remain independent authorities and are bound to an interaction through the Interaction Manifest.

Applied to one interaction, they produce a causal Contract pipeline of roughly this shape:

```text
Input Presentation
    ↓
Canonicalization, if declared
    ↓
Admission
    ↓
Lowering
    ↓
candidate input Fact
    ↓
Invariant judgment
    ↓
established input Fact
    ↓
Operation
    ↓
candidate result Fact
    ↓
Invariant judgment
    ↓
State / Transition judgment, where applicable
    ↓
established result Fact
    or declared Operation failure
    ↓
Publication
    ↓
Output Presentation
```

Policy, Budget, Capacity, Governance, Version, Failure, and Diagnostic obligations apply where their authority is
required. They are not necessarily separate sequential stages in the main pipeline.

This pipeline describes causal Contract meaning, not a required physical execution layout.

The implementation may fuse work, change storage, execute independent work differently, or replace the backend entirely.
Those choices belong to realization as long as the declared obligations, causal relations, and authority boundaries
remain intact.

The next question is therefore not whether something appears in the pipeline.

It is which parts of this machine actually carry Contract authority.

---

### 11. What Counts as Contract in the Pipeline

Think about a normal engineering process for a second.

A factory takes something in and sends something out, but nobody with a functioning brain thinks the factory is just raw
material directly becoming a finished product.

```text
raw material
    ->
finished product
```

The middle is the factory. Material moves deliberately through explicit processes: one stage completes its work,
material reaches the required condition for the next step, and the second stage begins based on that progress. If
something breaks, the line halts right there to handle the failure on the spot.

Visible processes give you control. Instead of guessing what happened inside the machinery, explicit intermediate
positions let you attach state and manage the machine according to that state.

When something fails, the stage retains what entered, what happened, and why the process stopped, leaving diagnostic
evidence right where the failure occurred. You fix the specific stage, redesign the problematic boundary, or rewire the
machinery without pretending the intermediate process never existed.

Ordinary engineering depends on knowing how the machine gets from one meaningful condition to another.

Software went the complete opposite way. Paradigms and frameworks spent decades hiding the middle behind functions,
objects, indirection, callbacks, recursion, and runtime magic. The intermediate process never disappeared—we just became
exceptionally good at making it implicit and unreadable.

Kontrakt is not inventing some revolutionary software concept called a Pipeline. I am doing something much less
exciting: taking an ordinary engineering reality and putting it back into software.

If the intermediate process matters, make it explicit. A realistic Contract Machine should expose the causal structure
that actually matters to the domain without forcing anyone to reconstruct it from the implementation afterward.

That explicit logical structure is the Pipeline. A Flow is one progression through it, and a Stage is a declared logical
position inside it.

```text
Pipeline
    = explicit logical structure of the machine process

Flow
    = one progression through that structure

Stage
    = one declared logical position in that structure
```

Think about the factory again.

What matters is not the particular motor, conveyor, controller, or robot arm installed today. What matters to the
process is that material reaches a stage under the right condition, intended work happens there, and only an acceptable
result moves forward.

The physical machinery can change. One machine can be replaced, two physical operations can be combined, or one step can
be split into several while the required relations between stages remain intact.

Software should get the exact same treatment. The Pipeline is not a copy of the call graph or runtime topology—those are
just ways to realize the process. The Pipeline describes the process that must remain meaningful even when those
implementation details change.

Once that process is explicit, managing the machine becomes straightforward. We know which Stage a Flow reached, what
has already been established, what is allowed next, where execution stopped, and which part owns the information
explaining why. We preserve diagnostic evidence right where something unusual happened instead of reconstructing history
from whatever garbage the implementation happened to leave in the logs.

We can also optimize against an explicit process instead of blindly tuning code. Work can move earlier when required
information already exists, bad material gets stopped before wasting more resources, physical operations get combined
without merging distinct meanings, and an implementation can be replaced completely while the Pipeline stays untouched.

Making the middle explicit does not freeze the implementation. It defines which parts of the process actually carry
domain authority, giving the implementation far more freedom to change everything else.

There is, however, an important line here: just because something appears in the Pipeline does not automatically make it
a Contract.

A Stage gives the machine an explicit logical position in the process, whereas a Contract gives the machine an
obligation.

```text
Stage
    = a declared logical position in the Pipeline

Contract
    = a declared obligation governing the machine
```

A box on a diagram has no authority just because somebody drew it there. Execution order has no authority simply because
that is how the code currently runs.

The useful question to ask is always:

> What obligation would the machine lose if this declaration disappeared?

If removing or changing a declaration alters what the machine is allowed to accept, establish, move, expose, or refuse,
then Contract meaning changed. But if the physical machinery underneath can change while those obligations remain
strictly true, that machinery belongs purely to realization. Necessity alone does not confer Contract authority.

That gives us the Pipeline, but it still does not tell us what kinds of Contract actually participate in it, which ones
belong to a Stage, which ones govern movement across several Stages, and which ones sit on another axis entirely.

That is the next problem.

---

## 12. Contract Presentations in the Pipeline

If we read software as an engineering machine with an explicit Pipeline, the Contracts that govern that machine can be
placed along that process and examined by the role each one owns.

Calling everything `contract` does not make the machine explicit. It only gives the confusion a respectable name.

This section follows material through the Pipeline and separates the obligations that govern it along the way. Those
obligations belong to one machine, but they do not answer the same question and should not be allowed to blur together.

### 12.1 Fact Contract and Immutable Fact

A machine consists of two things: operations that execute work, and facts that describe what the machine knows. In the
Core, that information exists strictly as immutable Fact.

```text
Fact Contract:
    the Contract that defines what factual information may exist inside the Core

Immutable Fact:
    the immutable expression of that factual information
```

Fact gives the machine an explicit language for information without deciding what should be done with it.

This is fundamentally different from the material moving through the Pipeline. Material is the concrete subject of a
Stage—a Stage receives material, performs work, and passes updated or transformed material forward. Fact is the
authoritative truth behind that material inside the Core.

Material changes condition from stage to stage, but Fact never changes with it. Once information is expressed as Fact,
subsequent processing cannot rewrite it. If new information emerges later in the process, the machine simply emits a new
Fact.

A Fact carries no behavior. It performs no computation, holds no logic, and makes no decisions about what happens next.
It merely states what is true, immutably.

### 12.2 Policy and Governance Contracts

A real machine cannot operate under the assumption that one fixed execution path fits every situation.

Even during normal operation, changing conditions demand different ways of operating. Pipeline outputs, active States,
diagnostic evidence, and other governing Contracts continuously yield information that alters what should be allowed or
required. Engineering handles this by explicitly declaring how the machine responds to operational information. Policy
brings those decisions into the Contract.

```text
Policy Contract:
    the Contract that declares how the machine should respond to declared information relevant to its operation

Governance Contract:
    the Contract that governs the operation of the machine using declared Policy
```

Policy connects what the machine knows with a declared response, drawing its basis from information established across
the Pipeline and its active Contracts.

A successful result might dictate one path while another requires a different one. State alters what subsequent work is
allowed to proceed. Diagnostic evidence might justify shifting execution strategy even without a failure, and when a
failure does occur, the response must belong to declared Contract rather than being invented on the fly by
implementation code.

Policy uses information produced by other Contracts without usurping their underlying meaning or overriding their
authority. Choosing a different operational path may alter which Contracts come into play, but Policy merely dictates
how the machine responds—it does not rewrite domain laws.

Without an explicit Policy, operational decisions silently collapse into ordinary implementation logic. Code inspects an
arbitrary flag and branches, runtime configuration alters behavior, or an operator manually steers the system. The
machine might still run, but the rules governing its operation cease to be part of its declared Contract.

Declaring available responses is only half the problem. The machine also needs an authority that uses Pipeline
information to enforce how those responses apply across its components. That authority is Governance.

Governance evaluates Pipeline information against Policy to establish which declared operating choice governs a specific
Scope. When multiple parts of the machine must adhere to the same operational decision, Governance enforces that
boundary consistently across them.

Governance cannot invent responses that Policy never declared, nor can it alter the Contracts active under that
response. Its job is strictly to manage the machine through choices already declared within Policy.

This does not make Governance a scheduler or runtime controller. Threads, locks, queues, process boundaries, and
deployment mechanisms may realize a governing decision, but physical mechanics do not define what the decision means.

Policy declares how the machine responds to relevant information. Governance applies those declared responses to manage
the machine.

### 12.3 Budget and Capacity Contracts

Every real machine has limits. It consumes resources, it can only sustain so much work at once, and some of what it
consumes cannot be recovered without cost. Physical machines wear out, batteries run down, and computing machines
eventually run out of time, memory, storage, or other resources if work is allowed to consume them without bound.

A machine therefore needs more than a definition of correct work. It also needs a Contract that says how much may be
consumed and how much load may be carried without crossing the limits under which the machine can still operate as
intended.

```text
Budget Contract:
    the Contract that declares a finite allowance for a governed subject or Scope

Capacity Contract:
    the Contract that declares a finite simultaneous limit for a governed subject or Scope
```

Budget deals with consumption. Work may be valid and may even be making progress, but that does not mean the machine
should allow it to consume indefinitely. A Budget puts a declared limit around how much of a resource may be spent
within the part of the machine it governs.

Time is the simplest example. A particular piece of work may have a time Budget, but the same idea can govern a larger
Flow, a subsystem, or the machine as a whole. The subject and Scope of the Budget determine what consumption belongs to
that allowance.

The important part is that Budget belongs to the machine obligation whose consumption is being bounded. It is not an
arbitrary timeout or counter added because an implementation became inconvenient. The Contract declares a finite
allowance, and exhaustion of that allowance has Contract meaning.

Capacity deals with how much the machine can bear at once. A machine may handle each individual piece of work correctly
and still fail when too much valid work or material is present together.

Memory occupancy is one example. The problem is not how much memory has been consumed over time, but how much governed
memory is held at the same time. Concurrent work has the same shape when the machine must keep the amount of active work
within a limit it can support.

Budget and Capacity therefore protect different limits of the same finite machine. Budget bounds consumption over the
subject or Scope it governs. Capacity keeps simultaneous load within what that subject or Scope can safely support.
Releasing occupied Capacity can make room again, while consumption already charged to a Budget remains part of that
Budget unless its own Contract says otherwise.

The implementation may realize these Contracts through business logic, clocks, counters, accounting records, memory
measurements, permits, or other mechanisms. Those mechanisms can change without changing what the Budget or Capacity
means.

What matters is that the machine knows its limits before crossing them. A machine that only discovers its resource
limits after exhaustion or overload has already given part of its operation to accident rather than Contract.

### 12.4 Input Presentation Contract

An Interaction gives the outside world an explicit point of entry into the machine. That entry still requires a declared
form for any material presented through it.

Input is the first Contract of the inbound Airlock. Sitting between the outside world and the Core, this boundary forces
external material to prove itself before participating in Core meaning. Without it, uncontrolled assumptions from the
outside world inevitably leak into the machine.

External material almost always arrives wrapped in technologies the machine does not own—databases, networks,
frameworks, devices, serialization protocols, or third-party libraries. While necessary to transport data to the door,
these mechanisms must never cross into the Airlock alongside the material. If understanding or using an Input requires
reaching back into the stack that produced it, the Airlock has completely failed its job.

Input gives external material a declared presentation that the machine can evaluate entirely on its own terms. Foreign
technology can dictate how information reaches the Interaction, but it surrenders all right to define what that
information means once presented.

```text
Input Presentation Contract:
    the Contract that declares how outside material may be presented at an Interaction Boundary
```

Rather than defining what the outside world is, Input declares what form the machine accepts at an Interaction.

This gives the machine a concrete baseline for judgment. Material may originate from an untrusted source, but once it
reaches the Airlock, the Input Contract dictates exactly what must be present to count as a valid presentation.

The same dependency rule applies strictly to behavior. Data can enter as Input, but callbacks, lazy evaluations,
proxies, repositories, mutable objects, or other live mechanisms that let the machine reach back past the Airlock must
stay outside. Allowing them turns the outside world into an undeclared participant in Core logic.

The Airlock draws a strict line between external authority and governed material. Outside systems can transform data all
they want before an Interaction, but Core authority begins strictly at the declared Boundary. History before
presentation carries no weight.

This does not make Input a complete judgment on whether material should proceed through the Pipeline. A value can pass
presentation checks perfectly and still fail later domain rules. Input merely verifies that material matches the
declared entry form.

Presentation material also stops short of becoming Core Fact. It remains boundary-level data within the inbound
Airlock—ready to be evaluated, mapped, or transformed by subsequent Contracts. Crossing the threshold grants admission,
not authority.

Implementation details like carriers are irrelevant here. Whether data arrives as a message, record, serialized payload,
or DTO changes nothing. A DTO is just a vehicle; it is not what creates an Input. The underlying carrier can be swapped
entirely without touching the Contract, so long as the declared presentation holds.

The same principle applies to memory allocation. The machine does not need to copy objects just to prove a boundary was
crossed. Physical data structures can pass through processing untouched while their contractual meaning evolves along
the Pipeline. Authority springs from Airlock judgments, not memory allocation.

Finally, presentation requires stability during evaluation. If an external process mutates material while the machine is
reading it, verifying what arrived becomes impossible. Ensuring that stability is an implementation detail, but the
Contract must observe a single, coherent snapshot.

Once material satisfies the Input Presentation Contract, it becomes valid Boundary material for the remaining Airlock
checks. Only after those Contracts finish their work can material cross from external presentation into Core authority.

### 12.5 Guard and Admission Judgment

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

### 12.6 Declared Failure, Admitted Material, and Diagnostic Evidence

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

### 12.7 Canonicalization Rule

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

### 12.8 Lowering Obligation

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

### 12.9 Invariant Contract

After lowering, the core may have one complete candidate Fact.

The candidate may be canonical. It may be complete. It may be readable under core meaning.

Still, Fact authority has not happened.

Lowering says the candidate can be formed without changing declared meaning. Invariant asks the next question: may this
complete candidate stand as Fact under the integrity law attached to its factual kind?

```text
Invariant Contract:
    a standing internal integrity law attached to exactly one Fact kind, judging one complete candidate Fact before
    factual authority is granted
```

Do not confuse this with the old object-oriented class invariant.

That model ties invariant to a mutable object. The object mutates itself, breaks its own invariant during a method call,
and tries to repair the mess before anyone looks. State, validation, mutation, and behavior sit in the same object and
pretend the result is discipline.

This machine uses Invariant differently.

Invariant stands with one factual kind. It is not recreated by each interaction and it is not selected as a private law
for one path. Whenever a complete candidate of that Fact kind seeks factual authority, every applicable Invariant of
that kind speaks.

```text
complete candidate Fact
    -> every applicable Invariant of that Fact kind
    -> established Fact
       or declared refusal
```

The basis is deliberately small.

```text
Invariant basis:
    the complete canonical factual values of exactly one candidate Fact
```

That is enough.

Invariant does not obtain authority from another Fact, a population of Facts, history, a repository, current State, a
proposed Transition, Publication, or outside presentation. If a law needs those things, it is not a local integrity law
of one Fact. It belongs to another declared judgment surface.

This is why Invariant, State, and Transition stay separate.

```text
Invariant:
    whether one complete candidate Fact may stand

State:
    the declared condition governing the next legal move

Transition:
    whether a declared move may occur
```

A candidate Fact can pass Lowering and still fail Invariant.

Clean shape is not truth.

Canonical candidate form is not factual authority.

Only material that survives every applicable Invariant may become established Fact.

When an Operation declares a Fact as its result, Invariant refusal means the Operation has failed to produce its
contractual result. Result material may have appeared, but the declared result does not exist under contract authority.

Fact and Invariant must stay separate.

The Fact is material.

The Invariant is the standing integrity law that decides whether candidate material of that factual kind may stand.

Mix them and the old object soup returns: data carrying rules, behavior hiding judgment, mutation pretending to be
state, and implementation accidents sneaking into factual identity.

### 12.10 State Contract

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

### 12.11 State Transition Contract

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

### 12.12 Explicit State Machine

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

### 12.13 Publication Judgment

Established Fact is not automatically public claim.

That sentence is dull. Good. A machine that forgets it starts leaking internal truth as public authority.

An established Fact says the core may stand on that factual material. It does not say the outside world may see it, rely
on it, or receive it in some outward shape. Core truth, outward claim authority, and outward presentation are different
contracts.

```text
Publication Judgment:
    the contract judgment that decides whether an established Fact may support an outward claim, what factual meaning
    that claim may assert, and which stable public meaning governs it
```

The important word is `claim`.

Publication does not send Fact outside. Established Fact and factual authority remain inside the core. Publication gives
positive outward authority only to the exact factual meaning that a declared public claim is allowed to assert.

A Fact can be true in the core and still be forbidden in public.

That is not contradiction. That is boundary discipline. The core may know more than it is allowed to say.

Publication must therefore name its established factual basis, the exact meaning that may be claimed, the finite public
conditions under which that claim applies, the stable public meaning that governs it, and the declared stop when no
outward claim is authorized. Silence is not a publication rule.

Publication does not own the outward shape in which the claim appears. It does not own the mechanism that carries the
claim or the mechanism that emits it. Those things do not grant publication authority.

This separation matters because the shape may exist while publication is forbidden. The machine may be capable of
carrying a value while lacking authority to say it. Capability is not permission.

Stable public meaning matters because a published claim must not quietly change after it leaves. If the public meaning
changes later, the old claim does not improve itself in the dark. The machine needs a new public meaning, a new claim,
or a later version coordinate. It does not get to pretend the old publication was always saying the new thing.

Diagnostic evidence is not publication just because somebody can read it. Evidence may explain why publication was
allowed or denied. It may be retained for diagnosis. But unless Publication allows it to become an outward claim, it
stays evidence. A debug-shaped leak is still a leak.

Three failures must not be confused.

```text
incoherent Publication declaration:
    the contract does not form a valid Publication judgment

valid Publication with no authorized claim:
    declared publication stop

failure to carry an already authorized claim:
    failure outside Publication judgment
```

The point is simple.

The machine may know more than it is allowed to say.

### 12.14 Output Presentation Contract

An authorized public claim still needs a declared outward shape.

That shape is not Publication.

```text
Output Presentation Contract:
    the contract that declares the closed outward shape through which an authorized public claim may appear
```

Output Presentation declares outward coordinates, value distinctions, required presence, explicit absence, finite
alternatives, outward structural bounds, and the versioned shape in which a claim may be understood.

It does not decide whether publication is permitted. It does not establish Fact, select factual basis, judge business
meaning, move State, retain diagnostics, or authorize itself.

The distinction is direct:

```text
Publication:
    whether and what may be claimed

Output Presentation:
    in what closed outward shape the authorized claim may appear
```

An outward shape may exist without any authority to form a public claim through it.

Constructing, carrying, observing, or emitting a shape does not create that authority. Output Presentation declares the
shape that an authorized claim must respect. Publication remains the judgment that gives the claim permission and
factual basis.

Input Presentation and Output Presentation are mirrors, but not reverse calculations.

```text
Input Presentation:
    makes outside material finite and judgeable

Output Presentation:
    makes an authorized claim finite and outwardly legible
```

Neither presentation grants the judgment authority that surrounds it.

### 12.15 Diagnostic Evidence

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

If retained evidence needs to leave the machine, it must pass Publication as its own public diagnostic claim and then
appear through a declared Output Presentation. Output Presentation does not make the evidence public by itself. A
debug-shaped leak is still a leak, even when the leak has a very serious incident number attached to it.

Diagnostic evidence must also remain interpretable under the contract meaning that produced it. That is why version
coordinates come next.

### 12.16 Version Coordinate

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

Publication needs the coordinate because a public claim stands under a public meaning. If that public meaning changes,
the old claim does not quietly grow a new soul.

Output Presentation needs the coordinate when outward shape meaning can change while familiar coordinates remain. The
same-looking shape must not silently acquire a different public interpretation.

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

### 12.17 Where Preconditions and Postconditions Went

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

Take admission. Input Presentation, active policy, budget, and capacity may all matter before admission can produce
admitted material. From the narrow view of that judgment, they resemble preconditions, and admitted material resembles
a postcondition.

Move one step forward and the labels shift. Admitted material is now something canonicalization or lowering receives.
The former postcondition has become part of the next judgment's starting material. A lowered candidate then reaches
Invariant judgment. An established Fact may become the input of an Operation. Candidate result material may then need
Invariant and movement judgment before the Operation has produced its declared result. Only an established result may
reach Publication, and only an authorized public claim may appear through Output Presentation. The words `pre` and
`post` keep moving because the machine keeps moving.

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

Producing result material is not a postcondition proving that an Operation succeeded. Where the declared result must
stand as Fact, Invariant and movement judgments belong to completion of the Operation itself. Failure of those judgments
means the declared result was not produced.

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

```text
Fact establishment
    != Publication

Publication
    != Output Presentation

Output Presentation
    != outward delivery
```

### 12.18 Execution Flow, Not Lifecycle Vocabulary

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
presentations name the obligations: interface surface, Input Presentation, admission, canonicalization, lowering,
Fact, Invariant, State, Transition, failure, Publication, Output Presentation, diagnostic, policy, budget, capacity, and
governance. Required coordinates name the active meaning and binding. The
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
Does candidate factual material receive authority only through the declared Fact and Invariant laws?
Does the Operation complete only after its declared result survives every applicable judgment?
Does it obey the declared State and Transition laws?
Does it publish only what Publication allows?
Does every authorized public claim close against its declared Output Presentation?
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
already declares its Input Presentation, admission, canonicalization, lowering, Fact establishment, Invariant, State
movement, Operation completion, failure, Publication, Output Presentation, diagnostic evidence, policy, budget,
capacity, and governance, then the test should not invent a private little religion about what the
machine probably means.

It should verify the declared contract surface.

That changes the target. A test over one declared judgment surface is not a ritual around a method body. It is a check
of that surface: admission, canonicalization, lowering, Fact establishment, Invariant judgment, Transition, Operation
completion, Publication, Output Presentation closure, or diagnostic retention. A test over the airlock from presented
input to admission result is a boundary test. A test over a small
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

The authority lines are equally strict:

```text
producing material
    != establishing Fact

producing result material
    != completing an Operation

forming an outward shape
    != authorizing a public claim

carrying an authorized claim
    != defining its public meaning
```

Representation may carry material. It does not grant factual authority. Operation grants declared result authority only
on successful completion. Publication grants outward claim authority. Output Presentation grants outward shape, not
permission to claim.

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

An interaction may expose only an authorized public claim, and that claim must appear through a declared Output
Presentation. Neither internal Fact shape nor an incidental outward representation becomes public contract merely
because someone can observe it.

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

established input Fact
    -> Operation
    -> established result Fact or declared Operation failure
    -> Publication
    -> Output Presentation

implementation:
    physical coordination behind the contract
```

Invariant is not a generic machine-wide validator inside that flow. It is the standing integrity law of one exact Fact
kind, applied whenever candidate material of that kind seeks factual authority.

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
keep Fact and Invariant as standing core laws that still govern each interaction rather than private interaction clauses
treat everything outside the core as untrusted
adopt external interfaces only after ratifying them against internal contracts
judge Input Presentation as outside material at the boundary
admit only what passes Admission
canonicalize declared-equivalent admitted material into the system's stable internal standard before identity becomes authoritative
lower canonical representation into complete candidate factual material
treat Fact as pure immutable factual material with no behavior
treat the same complete factual meaning under the same contract world as the same Fact
attach each Invariant to exactly one Fact kind
judge one complete candidate Fact before factual authority is granted
establish Operation input as Fact before the Operation may rely on it
complete an Operation only when its declared result survives every applicable judgment
treat establishment refusal as failure to produce the contractual result
treat State as explicitly declared contract material for governing the legality of the next machine move
allow only declared Transitions
reject through declared failures
grant outward claim authority only through Publication
separate Publication authority from Output Presentation shape
carry authorized claims only through a declared closed Output Presentation
declare failure instead of hiding it
declare policy, budget, capacity, and governance instead of hiding them
verify realization against the declared contract
keep implementation out of the contract
```

The current direction is this.