# Kontrakt

[![CI](https://github.com/B-Singularity/Kontrakt/actions/workflows/ci.yml/badge.svg)](https://github.com/B-Singularity/Kontrakt/actions)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.bsingularity.kontrakt/kontrakt-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:com.bsingularity.kontrakt)

> A modern, annotation-driven testing framework for Kotlin, based on the principles of Design by Contract.

**Kontrakt**'s goal is to liberate developers from the tedious, repetitive boilerplate of writing unit tests, allowing them to focus only on the most important thing: **verifying the contract**.

---

## 📚 Documentation

All detailed documentation for Kontrakt is managed on our **[GitHub Wiki](https://github.com/B-Singularity/Kontrakt/wiki)**.

**Key Pages:**
* **[[🚀 Quick Start (5-Minute Tutorial)|Quick Start]]**
* **[[💡 The Philosophy of Kontrakt|Philosophy]]**

## 🚀 Quick Start

### 1. Add Dependency

Add the following dependency to your `build.gradle.kts` file.

```kotlin
dependencies {
    testImplementation("com.bsingularity.kontrakt:kontrakt-core:0.1.0") // Replace with the latest version
}
```

### 2. Write Your First Test

Create a test class and add the `@KontraktTest` annotation to your test method. That's it. Kontrakt handles the rest.

```kotlin
import com.bsingularity.kontrakt.KontraktTest
import kotlin.test.assertEquals

class SimpleCalculatorTest {

    @KontraktTest(contract = "The sum of 2 and 2 must be 4.")
    fun `addition should work correctly`() {
        val calculator = SimpleCalculator()
        val result = calculator.add(2, 2)
        assertEquals(4, result)
    }
}
```

## 🙌 Contributing

Contributions are always welcome! Please read our **[[Contribution Guide]]** on the Wiki to get started.

## 📝 License

Kontrakt is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.
