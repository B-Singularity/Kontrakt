package execution.domain.service.validation

import common.util.unwrapped
import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import execution.domain.generator.GenerationContext
import execution.domain.service.generation.FixtureGenerator
import execution.domain.vo.AssertionRecord
import execution.domain.vo.DataContractRule
import execution.domain.vo.SourceLocation
import kotlin.reflect.full.primaryConstructor

/**
 * [Domain Service] Data Compliance Executor.
 *
 * Verifies that the target class adheres to the contract of a Data Class / Value Object.
 * This ensures the object is safe to use in Collections (Set, Map) and business logic.
 *
 * **Checks implemented:**
 * 1. **Structure Check:** Ensures a primary constructor exists.
 * 2. **Constructor Compliance:** Fuzzing validation via [ConstructorComplianceExecutor].
 * 3. **Equals Contract:** Verifies Reflexivity, Symmetry, Non-nullity, and Consistency.
 * 4. **HashCode Contract:** Verifies consistency with Equals and Stability over time.
 */
class DataComplianceExecutor(
    private val fixtureGenerator: FixtureGenerator,
    private val constructorExecutor: ConstructorComplianceExecutor,
) {
    fun execute(
        context: EphemeralTestContext,
        generationContext: GenerationContext,
    ): List<AssertionRecord> = buildList {
        val targetClass = context.specification.target.kClass
        val location = SourceLocation.Approximate(targetClass.qualifiedName ?: "Unknown")

        // --- Helper DSL for concise assertions ---
        fun check(
            rule: DataContractRule,
            message: String,
            expected: Any? = null,
            actual: Any? = null,
            condition: () -> Boolean,
        ) {
            val passed = try {
                condition()
            } catch (e: Exception) {
                false
            }
            if (passed) {
                add(AssertionRecord(AssertionStatus.PASSED, rule, message, location = location))
            } else {
                add(AssertionRecord(AssertionStatus.FAILED, rule, message, expected, actual, location))
            }
        }
        // -----------------------------------------

        // [Check 0] Structure Validation
        val constructor = targetClass.primaryConstructor ?: run {
            add(
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    rule = DataContractRule.Structure,
                    message = "Class '${targetClass.simpleName}' must have a primary constructor.",
                    location = location
                )
            )
            return@buildList
        }

        // [Check 1] Fuzzing (Delegate)
        addAll(constructorExecutor.validateConstructor(context, generationContext))

        // [Check 2] Generate Equivalence Pair
        // runCatching makes exception handling expression-based
        val instancePair = runCatching {
            val args = constructor.parameters.associateWith {
                fixtureGenerator.generate(it, generationContext)
            }
            // Create 'a' and 'b' (distinct objects, same content)
            constructor.callBy(args) to constructor.callBy(args)
        }.getOrElse { e ->
            add(
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    rule = DataContractRule.Structure,
                    message = "Failed to generate test instances: ${e.unwrapped.message}",
                    location = location
                )
            )
            return@buildList
        }

        val (a, b) = instancePair

        // [Check 3] Run Equality Checks

        // 3.1: Not Null Equality
        if (a != null) {
            // Explicit try-catch needed here to catch exceptions inside equals()
            try {
                if (a.equals(null)) {
                    add(
                        AssertionRecord(
                            AssertionStatus.FAILED,
                            DataContractRule.NotNullEquality,
                            "a.equals(null) MUST return false",
                            "false",
                            "true",
                            location
                        )
                    )
                } else {
                    add(
                        AssertionRecord(
                            AssertionStatus.PASSED,
                            DataContractRule.NotNullEquality,
                            "a.equals(null) returned false",
                            location = location
                        )
                    )
                }
            } catch (e: Exception) {
                add(
                    AssertionRecord(
                        AssertionStatus.FAILED,
                        DataContractRule.NotNullEquality,
                        "a.equals(null) threw exception: ${e.message}",
                        location = location
                    )
                )
            }
        }

        // 3.2: Reflexivity
        check(
            rule = DataContractRule.Reflexivity,
            message = if (a == a) "a.equals(a) returned true" else "a.equals(a) MUST return true",
            expected = "true",
            actual = "false"
        ) { a == a }

        // 3.3: Symmetry
        check(
            rule = DataContractRule.Symmetry,
            message = if (a == b && b == a) "Symmetry verified" else "Symmetry broken: a.equals(b)=${a == b}, b.equals(a)=${b == a}",
            expected = "true/true",
            actual = "${a == b}/${b == a}"
        ) { a == b && b == a }

        // 3.4: Consistency (Repeated Calls)
        val eq1 = (a == b)
        val eq2 = (a == b)
        check(
            rule = DataContractRule.Consistency,
            message = if (eq1 == eq2) "Equals Consistency verified" else "Equals Consistency broken ($eq1 vs $eq2)",
            expected = "Stable",
            actual = "Unstable"
        ) { eq1 == eq2 }

        // [Check 4] HashCode Contract

        // 4.1: Stability
        val h1 = a?.hashCode()
        val h2 = a?.hashCode()
        check(
            rule = DataContractRule.Consistency,
            message = if (h1 == h2) "HashCode Stability verified" else "HashCode Stability broken ($h1 vs $h2)",
            expected = h1,
            actual = h2
        ) { h1 == h2 }

        // 4.2: Consistency with Equals
        if (a == b) {
            val hashA = a?.hashCode()
            val hashB = b?.hashCode()
            check(
                rule = DataContractRule.HashCodeConsistency,
                message = if (hashA == hashB) "Consistent: a==b implies hash(a)==hash(b)" else "Inconsistent: a==b but hash codes differ",
                expected = hashA,
                actual = hashB
            ) { hashA == hashB }
        }
    }
}