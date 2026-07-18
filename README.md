# Kontrakt

[![CI](https://github.com/B-Singularity/Kontrakt/actions/workflows/ci.yml/badge.svg)](https://github.com/B-Singularity/Kontrakt/actions)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.bsingularity.kontrakt/kontrakt-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:com.bsingularity.kontrakt)

> Make Interfaces Great Again!

**Implicit code is the arch-enemy of software.** A total disaster. 

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

Look at this beautiful, perfect Factual State Machine:

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

No more guessing. No more implicit garbage. We are bringing order back to the core. 

**Let Contracts be Contracts.**
**Make Interfaces Great Again!**


## License

Kontrakt is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.
