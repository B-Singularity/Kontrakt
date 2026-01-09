package execution.domain.service.validation

import common.util.unwrapped
import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import execution.domain.generator.GenerationContext
import execution.domain.service.generation.FixtureGenerator
import execution.domain.vo.AssertionRecord
import execution.domain.vo.ConstructorSanityRule
import execution.domain.vo.DefensiveCheckRule
import execution.domain.vo.SourceLocation
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor


/**
 * [Domain Service] Constructor Compliance Executor.
 *
 * Verifies the structural integrity and defensive logic of the target class's constructor.
 *
 * **Design Note**:
 * Unlike the `DefaultScenarioExecutor`, this component **MUST catch exceptions**.
 * For defensive checks (Fuzzing), an exception thrown by the constructor is often
 * the **expected outcome (Success)**, indicating that the class correctly rejected invalid input.
 */
class ConstructorComplianceExecutor(
    private val fixtureGenerator: FixtureGenerator,
) {

    /**
     * orchestrates the validation process:
     * 1. **Sanity Check**: Ensures the constructor works with valid data.
     * 2. **Defensive Check**: Ensures the constructor throws exceptions for invalid data.
     */
    fun validateConstructor(
        context: EphemeralTestContext,
        generationContext: GenerationContext
    ): List<AssertionRecord> {
        val targetClass = context.specification.target.kClass
        val constructor = targetClass.primaryConstructor ?: return emptyList()

        val records = mutableListOf<AssertionRecord>()

        records.add(testValidConstructor(constructor, generationContext))

        constructor.parameters.forEach { param ->
            val invalidValues = fixtureGenerator.generateInvalid(param)

            invalidValues.forEach { badValue ->
                records.add(
                    testInvalidConstructor(constructor, param.name, badValue, generationContext)
                )
            }
        }
        return records
    }

    private fun testValidConstructor(
        constructor: KFunction<*>,
        generationContext: GenerationContext
    ): AssertionRecord =
        try {
            val args = constructor.parameters.associateWith {
                fixtureGenerator.generate(it, generationContext)
            }

            constructor.callBy(args)

            AssertionRecord(
                status = AssertionStatus.PASSED,
                rule = ConstructorSanityRule,
                message = "Constructor Sanity Check: Instance created successfully.",
                expected = "Instance Created",
                actual = "Success",
                location = SourceLocation.NotCaptured
            )
        } catch (e: Throwable) {
            val cause = e.unwrapped
            AssertionRecord(
                status = AssertionStatus.FAILED,
                rule = ConstructorSanityRule,
                message = "Constructor Sanity Check Failed: ${cause.message}",
                expected = "Instance Created",
                actual = cause.javaClass.simpleName,
                location = SourceLocation.NotCaptured
            )
        }

    private fun testInvalidConstructor(
        constructor: KFunction<*>,
        paramName: String?,
        badValue: Any?,
        generationContext: GenerationContext
    ): AssertionRecord =
        try {
            val args = constructor.parameters.associateWith {
                fixtureGenerator.generate(it, generationContext)
            }.toMutableMap()

            val targetParam = constructor.parameters.find { it.name == paramName }!!
            args[targetParam] = badValue

            constructor.callBy(args)

            AssertionRecord(
                status = AssertionStatus.FAILED,
                rule = DefensiveCheckRule,
                message = "Defensive Check Failed: Constructor accepted invalid '$paramName' = $badValue",
                expected = "Exception Thrown",
                actual = "Instance Created",
                location = SourceLocation.NotCaptured
            )
        } catch (e: Throwable) {
            val cause = e.unwrapped
            AssertionRecord(
                status = AssertionStatus.PASSED,
                rule = DefensiveCheckRule,
                message = "Defensive Check Passed: Constructor rejected invalid '$paramName' = $badValue",
                expected = "Exception",
                actual = cause.javaClass.simpleName,
                location = SourceLocation.NotCaptured
            )
        }
}
