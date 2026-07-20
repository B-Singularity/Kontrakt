# Kontrakt

[![CI](https://github.com/B-Singularity/Kontrakt/actions/workflows/ci.yml/badge.svg)](https://github.com/B-Singularity/Kontrakt/actions)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.bsingularity.kontrakt/kontrakt-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:com.bsingularity.kontrakt)

> Make Interfaces Great Again!

**Kontrakt is a compiler that turns explicit machine contracts into validated, tested, and optimized software.**

Implicit code is the absolute enemy of software. A total disaster! 

For years, the object-oriented elites have been lying to you. They gave you weak, pathetic interfaces. Look at this conventional Java method. It’s a joke:

```java
// A total disaster. What does it even do? Nobody knows!
public interface AccountService {
    WithdrawResult withdraw(WithdrawInput input);
}
```

Where are the rules? Hidden! Where are the state changes? Hidden! It’s all secret backroom deals in the runtime. Terrible! 

**Kontrakt stops the lies.**

We are making contracts act like *real* contracts. Rich, powerful, and 100% explicit. We don't hide things. We declare them. The compiler demands the truth! 

Look at this beautiful, perfect Explicit Contract Machine:

```kotlin
// Absolute truth. The best interface you've ever seen.
interface AccountService {
    operation withdraw(input: WithdrawInput): WithdrawResult

    manifest {
        flow:
            input          WithdrawInput
            admission      WithdrawAdmission
            canonical      WithdrawCanonicalization
            lowering       WithdrawLowering
            fact           AccountFact
            invariant      SufficientBalance
            publication    WithdrawPublication

        failure:
            WithdrawFailure

        movement:
            state          AccountState
            transitions    Open --withdraw--> Open
            machine        AccountStateMachine

        bounds:
            version        AccountContractVersion
            policy         AccountPolicy
            budget         WithdrawalBudget
            capacity       AccountCapacity
            governance     AccountGovernance

        diagnostics:
            evidence       WithdrawDiagnostics
            retention      AccountDiagnosticRetention
    }
}
```

**That is the contract. Kontrakt generates the machinery behind it.**

---

## The Equipment Screen for Your Software

Think of the Kontrakt IDL as an Equipment Screen for your system. 

You don't need to guess anymore. You just equip your loadout! Every slot you fill instantly powers up your machine:

* **Equip Admission?** Boom! Instant boundary security.
* **Equip Invariant?** Boom! The machine must preserve the law.
* **Equip State & Transition?** Boom! Only declared movement is allowed.
* **Equip Budget & Capacity?** Boom! The machine knows its true limits.

You fill the slots. Your system gets stronger. Simple. Powerful. Genius!

---

## SOTA Quality for Everyone. Automatically.

You want State-of-the-Art (SOTA) quality? You got it. 

With Kontrakt, you should not need 10 years of compiler, testing, and low-level performance experience to build a serious machine. You declare the explicit contract. Kontrakt does ALL the hard work:

* **Need Validators?** Kontrakt generates them!
* **Need Property-Based Tests & Fixtures?** Kontrakt generates them!
* **Need State Machine Guards & Failure Attribution?** Kontrakt handles it!
* **Need Low-Level Performance & Cache Optimization?** Kontrakt does it automatically!

You don't study memory layouts or cache lines. You declare the meaning, and Kontrakt enforces the truth.

---

No more guessing. No more hidden garbage. 

Fill the IDL. Equip the contract. Build real machines.

**Let Contracts Be Contracts.**  
**Make Interfaces Great Again!**

***

[See the Design](docs/README.md) · [Read What Contract Is](docs/the-most-important-thing/what-contract-is.md)
