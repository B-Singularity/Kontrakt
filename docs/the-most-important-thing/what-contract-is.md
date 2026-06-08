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

## 3. Good Machine and Function

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

That is the direction.

Still left.