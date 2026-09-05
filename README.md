# Kontrakt

[![CI](https://github.com/B-Singularity/Kontrakt/actions/workflows/ci.yml/badge.svg)](https://github.com/B-Singularity/Kontrakt/actions)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.bsingularity.kontrakt/kontrakt-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:com.bsingularity.kontrakt)

# Make Interfaces Great Again!

**Kontrakt is a compiler. Turns explicit machine contracts into validated, tested, optimized software. Nobody does this. Nobody. We do it, and we do it right.**

Everybody says "contract." Software people especially. They love the word, believe me — they just don't mean it. Total con job. Been going on for years.

Take this Java interface. You've seen it a thousand times, maybe more:

```java
public interface AccountService {
    WithdrawOutput withdraw(WithdrawInput input);
}
```

Beautiful. Very clean lines. People love this interface.

Now ask it one question. Just one. What's actually allowed here — what can move, what has to stay true, what failure is even permitted?

Nothing. Silence. The interface doesn't know. Sad, actually, when you think about it.

Kontrakt fixes that. Fast. And it fixes it right — not like the other guys.

---

## We Took "Interface" Literally

Java's own tutorial calls an interface a contract. A contract! Right there in writing, for years, and nobody read it — nobody took it seriously. We did. Probably the only ones, frankly.

The idea was never "a pile of method signatures." It was an agreement — two sides, don't even need to know each other's implementation. Tremendous idea. Just sitting there, unused.

A normal interface gives you this:

```java
WithdrawOutput withdraw(WithdrawInput input);
```

That's a call boundary. Nothing more. Kontrakt turns it into a real **Interaction**, governed by an actual contract. Not comments nobody reads. Not annotations stacked ten deep. Not the same rule copy-pasted into five files by five different people who don't talk to each other.

The contract itself — and this is the good part:

```kotlin
interface AccountService {
    facts AccountFacts
    invariants AccountInvariants

    operation withdraw(
        command: WithdrawCommand
    ): WithdrawalRecorded {
        manifest {
            flow:
                input             WithdrawInput
                admission         WithdrawAdmission
                canonicalization  WithdrawCanonicalization
                lowering          WithdrawLowering
                publication       WithdrawPublication
                output            WithdrawOutput

            failure:
                WithdrawFailure

            movement:
                state             AccountState
                transitions       AccountTransitions
                machine           AccountStateMachine

            bounds:
                version           AccountContractVersion
                policy            AccountPolicy
                budget            WithdrawalBudget
                capacity          AccountCapacity
                governance        AccountGovernance

            diagnostics:
                evidence          WithdrawDiagnostics
                retention         AccountDiagnosticRetention
        }
    }
}
```

Now the interface is finally doing its job — its real job, the one it was always supposed to do. It's not one vague contract pretending to cover everything. It's several real, independent contracts meeting at a single point. That's a big difference. Huge, actually.

**Kontrakt gives the interface its job back. Nobody else was ever going to do it. We did.**

---

## What Do You Actually Write?

A rich contract should never hand you seventeen interfaces to implement. Seventeen! That would be a total disaster, and frankly, some frameworks out there do exactly that. Not us.

Kontrakt compiles the contract down. You get a completely normal interface out the other side:

```java
// GENERATED
public interface AccountService {
    WithdrawOutput withdraw(WithdrawInput input);
}
```

Use it like any interface, because that's exactly what it is:

```java
public final class AccountController {

    private final AccountService accounts;

    public AccountController(AccountService accounts) {
        this.accounts = accounts;
    }

    public WithdrawOutput withdraw(WithdrawInput input) {
        return accounts.withdraw(input);
    }
}
```

You implement the actual business Operation. That part's yours, and honestly, it should be:

```java
// GENERATED
public interface AccountServiceOperation {
    WithdrawalRecorded withdraw(WithdrawCommand command);
}
```

```java
public final class AccountApplication
        implements AccountServiceOperation {

    @Override
    public WithdrawalRecorded withdraw(
            WithdrawCommand command
    ) {
        long remainingBalance =
            Math.subtractExact(
                command.balanceMinor,
                command.amountMinor
            );

        return new WithdrawalRecorded(
            command.accountId,
            command.amountMinor,
            remainingBalance
        );
    }
}
```

That's it. You write the algorithm — that's your job, do it well. Kontrakt builds the machine around it. Everything the Operation hands back goes straight under contract control before it's allowed anywhere near the outside world. Nothing sneaks out. Nothing. We don't allow it.

You don't rewrite the contract as validators. You don't restate it in some mapper class nobody wants to maintain. You don't build a second state machine — one was already plenty, believe me, and building a second one is how projects die.

One declared machine. That's the whole ballgame.

---

## "Couldn't I Just Do This Myself?"

Sure. Absolutely. Go build your own linker too while you're at it, why not.

Real question is whether you can do it with the same engineering quality. Once every judgment has to agree — identity, ordering, failure ownership, state legality, diagnostics, test generation, all of it — your cute little validation layer stops being little. Fast.

Now it needs compiler-grade resolution. Then verification. Then evidence. Then deterministic execution material. Then generated tests that don't just invent their own opinion of what's correct. Then backend work so none of this becomes a performance tax nobody asked for.

At some point you look around the room and you realize something:

**You're building a compiler.**

And not a small one. Never a small one.

Kontrakt already did that work. In the compiler. Once. So your team never has to do it again, in every service, forever. That's the deal, and it's a great deal.

---

## Kontrakt Checks the Machine Against the Contract

The contract isn't some PDF sitting next to the code, ignored, gathering dust while everybody does whatever they want. No. It's compiler input — Kontrakt reads it, and Kontrakt does the checking. That's the whole arrangement.

Some problems Kontrakt catches before a single line ever runs. Others only make sense once the Interaction is actually live — Kontrakt checks those the moment they become decidable, not before, not after. Kontrakt doesn't pretend everything's knowable in advance, and it doesn't dump everything onto runtime either. Smart. Very smart, actually.

Implementation can't lawfully participate in the declared machine? Kontrakt catches it. A call breaks a judgment that only resolves at runtime? Kontrakt catches that one too, right when it happens, no delay.

And when something fails, Kontrakt's diagnostics tell you exactly **which declared meaning failed** — straight from the contract, not guessed at. Not just "an exception happened somewhere, good luck finding it." Much better. Much smarter. The best diagnostics out there, and I'm not just saying that.

---

## Rich Contracts Make Property-Based Testing Possible

Give a normal test tool this:

```java
WithdrawOutput withdraw(WithdrawInput input);
```

It knows nothing. Nothing! Maybe a type, maybe a name, maybe some comment somebody wrote back in 2023 and forgot about. It doesn't know what input's legal, what invariants hold, what states can move, what failures even exist. So humans rebuild all those rules by hand, in test code, differently than the validator did it, differently than production does it. Three different interpretations of the same system. Great system. Really great.

Kontrakt already has the rules explicit — right there in the contract. Same material drives **Property-Based Testing** and contract-oriented unit tests, both, from one source. Valid cases generated straight from admissibility. Invalid cases pushed right at the boundary, where they belong. Transitions explored as actual transitions, not guessed at from a method name. Invariants become properties. Declared failures become test targets.

Not magic. Never magic. Just what happens when the contract's finally rich enough to describe the whole machine instead of just naming a method and hoping for the best.

---

## The Compiler Does the Expensive Thinking Once

Without a shared contract, everybody rediscovers the same system, separately, badly. The validator builds one mental model. The tests build another. Diagnostics figure out what happened only after it already went wrong. Optimization looks at code and finds almost no declared intent anywhere.

Kontrakt establishes the contract once — one time, up front. Verification checks against it. Testing derives straight from it. Diagnostics explain failures through it. The backend uses it without ever letting some implementation accident promote itself into law.

One source. Leverage everywhere across the whole machine. Much smarter than generating a few classes and calling it a day, which, let's be honest, is what a lot of tools do.

---

## You Shouldn't Need a Compiler Team to Use Compiler-Grade Machinery

Nobody buys a smartphone and studies semiconductor fabrication first. Nobody. The hard engineering is supposed to live inside the product, not on your desk at 2 AM.

Software forgot that somewhere along the way. Want real verification? Better go build a verification architecture. Want deterministic generated tests? Better get real interested in shrinking, generators, state exploration, coverage theory. Want good diagnostics? Hope you designed evidence retention before production taught you the hard way why you needed it. Want performance too? Wonderful — now go learn memory layout, allocation behavior, dispatch, cache locality, specialization, backend design. A tremendous hobby, if you've got a few spare decades lying around.

Kontrakt puts all of that in the compiler. You understand your domain. You write the contract carefully. You implement the Operation correctly. You do not rebuild an entire compiler toolchain every time you need one serious interface. That part's already done. Right here.

---

## We Build Real Machines, Not Fairytales

Inputs are bad, a lot of the time. States conflict. Budgets run out. Capacity's finite, always has been. Failures show up at the worst possible moment — every single time, it's like clockwork.

Kontrakt treats all of that as contract material, not some miscellaneous detail bolted on at the end because somebody remembered at the last minute. A serious machine knows when it may proceed, what it has to preserve, where it may move, and what failure actually means when it can't continue. And it can explain the refusal using the real contract that governed it — not a stack trace, not a shrug.

Not pessimism. That's engineering. The real kind.

---

## Contracts Stay. Backends Get Better.

Contract owns meaning. Backend owns realization. Keep those two apart and something great happens: the compiler gets better without anybody rewriting the agreement.

A future backend gets better specialization, better caching, stronger incremental compilation, denser representations, a completely different target — fine. Great. Let it improve. Let it improve a lot.

Same contract, better machine. Every time. That's the future-proofing we're after, and frankly, nobody else is building it this way.

---

## Eventually, the Contract Should Read Like a Real Contract

Here's the big one, folks — the really big one. A machine-readable contract can become a human-readable contract. This is future compiler work, beyond the current V1 architecture, but it's planned, and it's coming.

Today, documentation gets written *about* the program. Then the program changes. The document doesn't. Classic. Everybody's seen this movie, and it never ends well.

Kontrakt starts from a better source. If the established contract already holds the machine's real rules, a future compiler renders those rules straight into a document for actual people to read. Not source code. Not compiler IR. Not an annotation dump wearing a tie, pretending to be legal writing. Real language. English. Korean. Probably more, down the road. The machine-readable contract stays authoritative — the natural-language document is just derived from it, always.

Something like this, conceptually:

> A withdrawal may proceed only when the submitted material satisfies the declared admission conditions.
>
> The withdrawal must preserve the applicable account invariants.
>
> Only declared state movement is permitted.
>
> When an applicable contract judgment cannot be satisfied, failure is established under the authority that owns that judgment.
>
> Only material authorized for publication may cross the outward boundary.

Now hand that to somebody who's never opened the source code in their life. They read the agreement, plain and simple. The compiler executes the same agreement. Both come from the exact same contract — no drift, no daylight between the two.

That's when the same compiler contract starts working far beyond the API boundary. Big league stuff, and it's only getting bigger.

---

## The Point

An interface was always supposed to describe how software interacts. Always. Kontrakt just took that promise literally — probably the only one that ever did.

The IDL makes the Interaction explicit. You implement the Operation. Kontrakt builds the machinery, checks it against the contract, derives tests from it, explains every failure through it, and keeps the backend improving without ever touching the meaning. Later, that same contract becomes something a real human being can actually read.

The interface stops being a thin little method signature and becomes what it was always supposed to be:

**the declared contract of an Interaction.**

That's the whole idea. Simple. Strong. Nobody does it better.

**Let Contracts Be Contracts.**
**Make Interfaces Great Again!**
