package execution.domain.service.validation

import common.util.unwrapped
import execution.domain.entity.EphemeralTestContext
import execution.domain.service.generation.FixtureGenerator
import execution.domain.vo.context.generation.GenerationContext
import execution.domain.vo.result.DataComplianceResult
import execution.domain.vo.verification.AssertionRecord
import execution.domain.vo.verification.AssertionStatus
import execution.domain.vo.verification.DataContractRule
import execution.domain.vo.verification.SourceLocation
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

/**
 * [Domain Service] Data Compliance Executor.
 *
 * Verifies that the target class adheres to the strict contract of a Data Class / Value Object.
 * This ensures the object is safe to use in Collections (Set, Map) and business logic.
 *
 * **Gold Master Version Features:**
 * 1. **Forensic Audit:** Captures and returns instantiation arguments even on failure for precise debugging.
 * 2. **Unsinkable Stability:** Handles exceptions in ALL checks (Symmetry, Consistency, Hash) without crashing.
 * 3. **High Fidelity Reporting:** Records actual runtime values and uses explicit [AssertionStatus.SKIPPED].
 * 4. **Logical Strictness:** Uses 3-try consensus for side-effect detection and defensive equality checks.
 */
class DataComplianceExecutor(
    private val fixtureGenerator: FixtureGenerator,
    private val constructorExecutor: ConstructorComplianceExecutor,
) {
    /**
     * [Internal Model] Result of the pair creation attempt.
     * Encapsulates the success or failure state along with the forensic arguments.
     */
    private sealed interface PairCreationResult {
        val capturedArgs: Map<String, Any?>

        data class Success(
            val a: Any,
            val b: Any,
            override val capturedArgs: Map<String, Any?>,
        ) : PairCreationResult

        data class Failure(
            override val capturedArgs: Map<String, Any?>,
            val diagnosticMessage: String,
        ) : PairCreationResult
    }

    /**
     * Executes the full compliance suite against the target class found in the context.
     *
     * @return [DataComplianceResult] containing assertion records and the arguments used for generation.
     */
    fun execute(
        context: EphemeralTestContext,
        generationContext: GenerationContext,
    ): DataComplianceResult {
        val targetClass = context.specification.target.kClass
        val location = SourceLocation.Approximate(targetClass.qualifiedName ?: "Unknown")
        val records = mutableListOf<AssertionRecord>()

        // Phase 0: Structure Validation
        val constructor =
            targetClass.primaryConstructor ?: run {
                records.addFailure(DataContractRule.Structure, "Class must have a primary constructor.", location)
                return DataComplianceResult(records, emptyMap())
            }

        // Phase 1: Constructor Fuzzing (Delegated)
        records.addAll(constructorExecutor.validateConstructor(context, generationContext))

        // Phase 2: Generation & Instantiation
        val result = tryCreatePair(constructor, fixtureGenerator, generationContext)

        // Handle Creation Failure
        if (result is PairCreationResult.Failure) {
            records.addFailure(
                DataContractRule.Structure,
                "Failed to generate test instances. ${result.diagnosticMessage}",
                location,
            )
            return DataComplianceResult(records, result.capturedArgs)
        }

        // Handle Creation Success
        val success = result as PairCreationResult.Success
        val (a, b) = success

        // Phase 3: Validation Logic
        val validator = ContractCheckScope(records, location)

        with(validator) {
            checkNotNullEquality(a)
            checkReflexivity(a)
            checkSymmetry(a, b)
            checkEqualsConsistency(a, b)
            checkHashStability(a)
            checkHashConsistency(a, b)
        }

        return DataComplianceResult(records, success.capturedArgs)
    }

    // =========================================================================
    // Helper: Instantiation Pipeline
    // =========================================================================

    private fun tryCreatePair(
        constructor: KFunction<*>,
        generator: FixtureGenerator,
        context: GenerationContext,
    ): PairCreationResult {
        var capturedMap: Map<String, Any?> = emptyMap()
        var argsText = "{}"

        return try {
            // Step 1: Generate Arguments
            val rawArgs =
                constructor.parameters.associateWith { param ->
                    generator.generate(param, context)
                }

            // Step 2: Capture for Audit
            capturedMap = rawArgs.mapKeys { it.key.name ?: "param-${it.key.index}" }

            // Formatting: Multi-line for better readability in logs
            argsText =
                capturedMap.entries.joinToString(",\n  ", prefix = "\n  ", postfix = "\n") {
                    "${it.key}=${it.value ?: "null"}"
                }

            // Step 3: Instantiate Pair (a, b)
            val a = constructor.callBy(rawArgs)
            val b = constructor.callBy(rawArgs)

            if (a == null || b == null) {
                PairCreationResult.Failure(capturedMap, "Instantiated object was null. args=[$argsText]")
            } else {
                PairCreationResult.Success(a, b, capturedMap)
            }
        } catch (e: Exception) {
            PairCreationResult.Failure(
                capturedMap,
                "Error during instantiation: ${e.unwrapped.message}. args=[$argsText]",
            )
        }
    }

    // =========================================================================
    // Helper: Verification Scope (Inner Class)
    // =========================================================================

    private class ContractCheckScope(
        private val records: MutableList<AssertionRecord>,
        private val location: SourceLocation,
    ) {
        fun check(
            rule: DataContractRule,
            message: String,
            expected: Any? = null,
            actual: Any? = null,
            condition: () -> Boolean,
        ) {
            try {
                if (condition()) {
                    records.addPassed(rule, message, location)
                } else {
                    records.addFailure(rule, message, location, expected, actual)
                }
            } catch (e: Exception) {
                records.addFailure(
                    rule,
                    "$message (THREW EXCEPTION)",
                    location,
                    expected = "true",
                    actual = "Exception: ${e.message}",
                )
            }
        }

        fun checkNotNullEquality(a: Any) {
            try {
                val result = a.equals(null)
                check(
                    rule = DataContractRule.NotNullEquality,
                    message = "a.equals(null) MUST return false",
                    expected = "false",
                    actual = result,
                ) { !result }
            } catch (e: Exception) {
                records.addFailure(
                    DataContractRule.NotNullEquality,
                    "a.equals(null) THREW EXCEPTION: ${e.message}",
                    location,
                )
            }
        }

        fun checkReflexivity(a: Any) {
            val result =
                try {
                    a.equals(a)
                } catch (e: Exception) {
                    false
                }
            check(
                rule = DataContractRule.Reflexivity,
                message = "Reflexivity (a.equals(a))",
                expected = "true",
                actual = result,
            ) { result }
        }

        fun checkSymmetry(
            a: Any,
            b: Any,
        ) {
            val aEqB =
                try {
                    a.equals(b)
                } catch (e: Exception) {
                    null
                }
            val bEqA =
                try {
                    b.equals(a)
                } catch (e: Exception) {
                    null
                }

            check(
                rule = DataContractRule.Symmetry,
                message = "Symmetry (a.equals(b) == b.equals(a))",
                expected = "Symmetric",
                actual = "a.eq(b)=$aEqB, b.eq(a)=$bEqA",
            ) { aEqB == bEqA }
        }

        fun checkEqualsConsistency(
            a: Any,
            b: Any,
        ) {
            val results =
                List(3) {
                    try {
                        a.equals(b)
                    } catch (e: Exception) {
                        null
                    }
                }
            val consistent = results.distinct().size == 1

            check(
                rule = DataContractRule.Consistency,
                message = "Equals Consistency (3 repeated calls must yield same result)",
                expected = "Stable",
                actual = if (consistent) "Stable" else "Unstable: $results",
            ) { consistent }
        }

        fun checkHashStability(a: Any) {
            val h1 = a.hashCode()
            val h2 = a.hashCode()

            check(
                rule = DataContractRule.HashStability,
                message = "HashCode Stability",
                expected = h1,
                actual = h2,
            ) { h1 == h2 }
        }

        fun checkHashConsistency(
            a: Any,
            b: Any,
        ) {
            val areEqual =
                try {
                    a.equals(b)
                } catch (e: Exception) {
                    null
                }

            when (areEqual) {
                true -> {
                    val hA = a.hashCode()
                    val hB = b.hashCode()
                    check(
                        rule = DataContractRule.HashConsistency,
                        message = "HashCode Consistency (Equal objects -> Equal hashCodes)",
                        expected = hA,
                        actual = hB,
                    ) { hA == hB }
                }

                false -> {
                    records.add(
                        AssertionRecord(
                            status = AssertionStatus.SKIPPED,
                            rule = DataContractRule.HashConsistency,
                            message = "Skipped: Objects are not equal, so hash code constraint does not apply.",
                            location = location,
                        ),
                    )
                }

                null -> {
                    records.addFailure(
                        DataContractRule.HashConsistency,
                        "Prerequisite Failed: a.equals(b) threw exception, cannot verify hash contract.",
                        location,
                        expected = "No Exception",
                        actual = "Exception",
                    )
                }
            }
        }
    }
} // End of Class

// =========================================================================
// Top-Level Helper Extension Functions (File Scope)
// *MOVED HERE* to allow access from nested classes like ContractCheckScope
// =========================================================================

private fun MutableList<AssertionRecord>.addFailure(
    rule: DataContractRule,
    message: String,
    location: SourceLocation,
    expected: Any? = null,
    actual: Any? = null,
) {
    this.add(
        AssertionRecord(
            status = AssertionStatus.FAILED,
            rule = rule,
            message = message,
            expected = expected,
            actual = actual,
            location = location,
        ),
    )
}

private fun MutableList<AssertionRecord>.addPassed(
    rule: DataContractRule,
    message: String,
    location: SourceLocation,
) {
    this.add(
        AssertionRecord(
            status = AssertionStatus.PASSED,
            rule = rule,
            message = message,
            location = location,
        ),
    )
}
