package execution.domain.service

import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import execution.domain.util.ExceptionHelper
import execution.domain.vo.AssertionRecord
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

class ConstructorComplianceExecutor(
    private val fixtureGenerator: FixtureGenerator
) {
    fun validateConstructor(context: EphemeralTestContext): List<AssertionRecord> {
        val targetClass = context.specification.target.kClass
        val constructor = targetClass.primaryConstructor ?: return emptyList()

        val records = mutableListOf<AssertionRecord>()

        records.add(testValidConstructor(constructor))

        constructor.parameters.forEach { param ->
            val invalidValues = fixtureGenerator.generateInvalid(param)

            invalidValues.forEach { badValue ->
                records.add(testInvalidContructor(constructor, param.name, badValue))
            }
        }
        return records
    }

    private fun testValidConstructor(
        constructor: KFunction<*>
    ): AssertionRecord {
        return try {
            val args = constructor.parameters.associateWith { fixtureGenerator.generate(it) }
            constructor.callBy(args)
            AssertionRecord(
                AssertionStatus.PASSED,
                "Constructor Sanity Check: Instance created successfully.",
                "Success",
                "Success"
            )
        } catch (e: Throwable) {
            val cause = ExceptionHelper.unwrap(e)
            AssertionRecord(
                AssertionStatus.FAILED,
                "Constructor Sanity Check Failed: ${cause.message}",
                "Success",
                cause.javaClass.simpleName
            )
        }
    }

    private fun testInvalidContructor(
        constructor: KFunction<*>,
        paramName: String?,
        badValue: Any?
    ): AssertionRecord {
        return try {
            val args = constructor.parameters.associateWith { fixtureGenerator.generate(it) }.toMutableMap()

            val targetParam = constructor.parameters.find { it.name == paramName }!!
            args[targetParam] = badValue

            constructor.callBy(args)

            AssertionRecord(
                AssertionStatus.FAILED,
                "Defensive Check Failed: Constructor accepted invalid '$paramName' = $badValue",
                "Exception Thrown",
                "Instance Created"
            )
        } catch (e: Throwable) {
            val cause = ExceptionHelper.unwrap(e)
            AssertionRecord(
                AssertionStatus.PASSED,
                "Defensive Check Passed: Constructor rejected invalid '$paramName' = $badValue",
                "Exception",
                cause.javaClass.simpleName
            )
        }
    }
}