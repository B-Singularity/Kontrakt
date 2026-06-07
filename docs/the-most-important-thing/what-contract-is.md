# What Is Contract

## 0. Intention

The most important thing in software is intention.

Not class.

Not object.

Not type theory.

Not some beautiful mathematical toy that runs like shit on a real machine.

Intention comes first.

I am going to describe a contract theory and a machine that fit my intention.

Do not memorize this theory like a fucking textbook.

That is not the point.

This is engineering.

Engineering is not about worshiping a perfect abstract truth.

Engineering is about building a machine that actually works for a declared purpose.

There is no perfect answer called "the world."

There is only approximation.

There is only purpose.

There is only a machine that either fits that purpose or fails.

So remember this:

```text
I am not writing universal truth.

I am writing the shape of a machine I want to build.
```

If the machine does not fit the purpose, the theory is useless.

If the theory is beautiful but the machine is garbage, the theory is garbage for engineering.

That is the starting point.

---

## 1. Mathematics, Physics, and Engineering

Mathematics, physics, and engineering do not live under the same rules.

People mix them all the time.

That is where a lot of programming bullshit begins.

### Mathematics

Mathematics starts from axioms.

It defines objects.

It proves theorems.

It repeats strict logical steps.

Inside mathematics, this is fine.

Mathematics can search for unchanging formal truth.

It can build beautiful systems.

It can reduce a whole formal system to one primitive atom if it wants.

Good for mathematics.

But that does not mean a single fucking thing for engineering.

A formal system being consistent does not mean it is a good machine.

A formal reduction being possible does not mean it should be used.

A beautiful proof does not pay for cache misses, allocation, failure modes, debugging, or operational cost.

### Physics

Physics is different.

Physics is not some church for worshiping absolute truth.

Physics does not possess truth.

Physics is a disciplined guessing game against the world.

You look at the world and say:

```text
Maybe it works like this.
```

Then you build a theory.

Then you test it.

If the theory fits the world well enough, good.

You keep it, use it, and refine it.

If it does not fit the world, you do not worship it.

You go back and say:

```text
Then maybe it works like this instead.
```

That is physics.

A physical theory is not truth itself.

A physical theory is a useful approximation that survives contact with the world.

No amount of beauty saves a theory that fails against reality.

Reality does not give a shit about elegance.

That is the correct attitude.

```text
Maybe it works like this.
```

### Engineering

Engineering is physics with a stronger purpose.

Engineering does not ask:

```text
Is this formally beautiful?
```

Engineering asks:

```text
Can we build the machine?
Does it do the job?
Is the cost acceptable?
Can failure be diagnosed?
Can it run under real constraints?
Can it be maintained?
Can people actually use it?
```

That is the standard.

If the answer is no, then I do not care how elegant the theory is.

Call it mathematics.

Do not call it good engineering.

---

## 2. Do Not Do Mathematics When the Job Is to Build a Machine

Use mathematics.

Do not become mathematics.

That is the line.

Mathematics is a tool in engineering.

It is a language.

It is a measuring device.

It is a way to make claims precise.

But engineering must not be turned into pure mathematics.

This is where programming went wrong again and again.

From the early days of programming, people kept trying to turn machines into mathematical dreams.

Formal systems.

Combinators.

Type theory.

Higher-order functions.

Lazy evaluation.

Inheritance hierarchies.

Subtyping games.

Callback hell.

Proxy magic.

Beautiful on paper.

Fucking miserable on real machines.

If a thing is so elegant, then where is the good machine?

If nobody uses it because it is slow, opaque, hard to debug, hard to operate, and hostile to real constraints, then what
exactly did you build?

A mathematical artifact.

Not good engineering.

I am not interested in turning software into a shrine for formal elegance.

I am interested in a machine that works.

---

## 3. Contract

A contract is what the system must satisfy.

Not what a class looks like.

Not what an object happens to contain.

Not what a type theorist can encode with enough tricks.

A contract is the set of things the software must preserve for the system to be acceptable under a declared purpose.

At minimum, a contract can involve:

```text
input
boundary
DTO
guard
lowering
immutable fact
core
state
state transition
restriction
failure
output
publication
```

This is not abstract decoration.

This is the shape of a machine.

A contract says:

```text
what may enter
what must be rejected
what must become stable
what must remain true
what may change
what transition is legal
what failure is allowed
what may leave the system
```

That is the point.

A contract is what the machine must not violate.

---

That is the direction.

Still left.