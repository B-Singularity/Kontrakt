package execution.domain.service.orchestration

import discovery.api.Test
import discovery.domain.aggregate.TestSpecification
import exception.KontraktConfigurationException
import exception.KontraktInternalException
import execution.api.TestScenarioExecutor
import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import execution.domain.generator.GenerationContext
import execution.domain.service.generation.FixtureGenerator
import execution.domain.service.validation.ConstructorComplianceExecutor
import execution.domain.service.validation.ContractValidator
import execution.domain.service.validation.DataComplianceExecutor
import execution.domain.vo.AssertionRecord
import execution.domain.vo.SourceLocation
import execution.domain.vo.StandardAssertion
import execution.spi.MockingEngine
import execution.spi.trace.ScenarioTrace
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Method
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

/**
 * [Core Service] Default Scenario Executor.
 *
 * This component acts as the **Central Nervous System** of the test execution phase.
 * It is responsible for orchestrating the lifecycle of a test scenario based on the provided specification.
 *
 * **Responsibilities:**
 * 1. **Routing:** Dispatches execution to the appropriate strategies (User Scenario, Contract, Data Compliance).
 * 2. **Dependency Injection:** Bootstraps generators and contexts with deterministic configuration.
 * 3. **Execution:** Invokes target methods using reflection.
 * 4. **Telemetry:** Populates the [ScenarioTrace] with generated arguments.
 *
 * **Architecture Note (ADR-020):**
 * This executor follows the **Exception Transparency** principle.
 * It strictly propagates exceptions (including reflection errors) to the upstream `AuditingInterceptor`.
 */
class DefaultScenarioExecutor(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val fixtureFactory: (MockingEngine, Clock, ScenarioTrace, Long) -> FixtureGenerator =
        { engine, clock, trace, seed -> FixtureGenerator(engine, clock, trace, seed) },
    private val validatorFactory: (Clock) -> ContractValidator =
        { clock -> ContractValidator(clock) },
) : TestScenarioExecutor {
    private val logger = KotlinLogging.logger {}

    override fun executeScenarios(context: EphemeralTestContext): List<AssertionRecord> {
        // [Strategy Fix 1] Restore Deterministic Clock Strategy
        // We capture the current instant ONCE and freeze it for the duration of this test run.
        // This ensures that all time-dependent logic (Fixture generation, assertions) uses the same point in time.
        val currentInstant = Instant.now(clock)
        val fixedClock = Clock.fixed(currentInstant, ZoneId.systemDefault())

        // Use the seed from the specification or fallback to system time (preserved in the spec for reproducibility)
        val seed = context.specification.seed ?: System.currentTimeMillis()

        // Initialize Services with Fixed Context
        val fixtureGenerator = fixtureFactory(context.mockingEngine, fixedClock, context.trace, seed)
        val contractValidator = validatorFactory(fixedClock)

        val generationContext = GenerationContext(
            seededRandom = Random(seed),
            clock = fixedClock,
        )

        // Helper Executors
        val constructorExecutor = ConstructorComplianceExecutor(fixtureGenerator)
        val dataComplianceExecutor = DataComplianceExecutor(fixtureGenerator, constructorExecutor)

        val records = mutableListOf<AssertionRecord>()
        val modes = context.specification.modes

        // [Strategy Fix 2] Multi-Mode Execution Support
        // Instead of selecting a single mode via 'when', we sequentially execute all active modes.
        // This allows a single target to be tested as a User Scenario, a Contract implementation, AND a Data Class simultaneously.

        // 1. User Scenario Execution
        if (modes.contains(TestSpecification.TestMode.UserScenario)) {
            records.addAll(
                executeUserTestMethods(context, fixtureGenerator, generationContext),
            )
        }

        // 2. Contract Auto Fuzzing (Interface Contracts)
        // Restored logic: Executes methods defined in the @Contract interface and validates them.
        modes.filterIsInstance<TestSpecification.TestMode.ContractAuto>().forEach { mode ->
            records.addAll(
                executeContractFuzzing(
                    context,
                    mode.contractInterface.java,
                    fixtureGenerator,
                    contractValidator,
                    generationContext,
                ),
            )
        }

        // 3. Data Compliance (Data Class Contracts)
        // New logic: Validates equals/hashCode/structure using the new DataComplianceExecutor.
        modes.filterIsInstance<TestSpecification.TestMode.DataCompliance>().forEach { _ ->
            records.addAll(
                dataComplianceExecutor.execute(context, generationContext)
            )
        }

        return records
    }

    private fun executeUserTestMethods(
        context: EphemeralTestContext,
        fixtureGenerator: FixtureGenerator,
        generationContext: GenerationContext,
    ): List<AssertionRecord> {
        val testInstance = context.getTestTarget()
        val kClass = testInstance::class

        val testFunctions = kClass.functions.filter { it.findAnnotation<Test>() != null }

        if (testFunctions.isEmpty()) {
            logger.warn { "UserScenario mode active but no methods annotated with @Test found in ${kClass.simpleName}" }
            return emptyList()
        }

        return testFunctions.map { kFunc ->
            context.targetMethod = kFunc.javaMethod
                ?: throw KontraktInternalException(
                    "Failed to resolve Java method for Kotlin function: '${kFunc.name}'. " +
                            "This indicates a reflection issue with the target class '${kClass.simpleName}'.",
                )

            val args = createArguments(kFunc, context, fixtureGenerator, generationContext)
            captureArgumentsToTrace(context, args)

            if (kFunc.isSuspend) {
                runBlocking {
                    kFunc.callSuspendBy(args)
                }
            } else {
                kFunc.callBy(args)
            }

            AssertionRecord(
                status = AssertionStatus.PASSED,
                rule = StandardAssertion,
                message = "Test '${kFunc.name}' passed",
                expected = "Success",
                actual = "Success",
                location = SourceLocation.NotCaptured,
            )
        }
    }

    private fun executeContractFuzzing(
        context: EphemeralTestContext,
        contractClass: Class<*>,
        fixtureGenerator: FixtureGenerator,
        contractValidator: ContractValidator,
        generationContext: GenerationContext,
    ): List<AssertionRecord> {
        val testTargetInstance = context.getTestTarget()
        val implementationKClass = testTargetInstance::class

        return contractClass.methods
            .filter { !it.isBridge && !it.isSynthetic }
            .map { contractMethod ->
                val implementationFunction =
                    implementationKClass.functions.find { kFunc ->
                        if (kFunc.name != contractMethod.name) return@find false
                        val kFuncAsJava = kFunc.javaMethod ?: return@find false
                        kFuncAsJava.name == contractMethod.name &&
                                kFuncAsJava.parameterTypes.contentEquals(contractMethod.parameterTypes)
                    } ?: throw KontraktConfigurationException(
                        "Contract violation: Implementation of method '${contractMethod.name}' " +
                                "not found in '${implementationKClass.simpleName}'.\n" +
                                "Ensure the class strictly adheres to the contract interface.",
                    )

                val contractKFunc =
                    contractMethod.kotlinFunction
                        ?: throw KontraktInternalException(
                            "Reflection failure: Could not resolve Kotlin function metadata for contract method '${contractMethod.name}'.",
                        )

                context.targetMethod = contractMethod

                executeMethod(
                    contractMethod,
                    implementationFunction,
                    contractKFunc,
                    context,
                    fixtureGenerator,
                    contractValidator,
                    generationContext,
                )
            }
    }

    private fun executeMethod(
        contractMethod: Method,
        implFunc: KFunction<*>,
        contractKFunc: KFunction<*>,
        context: EphemeralTestContext,
        fixtureGenerator: FixtureGenerator,
        contractValidator: ContractValidator,
        generationContext: GenerationContext,
    ): AssertionRecord {
        val args = createArguments(implFunc, context, fixtureGenerator, generationContext)

        captureArgumentsToTrace(context, args)

        val result =
            if (implFunc.isSuspend) {
                runBlocking {
                    implFunc.callSuspendBy(args)
                }
            } else {
                implFunc.callBy(args)
            }

        contractValidator.validate(contractKFunc, result)

        return AssertionRecord(
            status = AssertionStatus.PASSED,
            rule = StandardAssertion,
            message = "Method '${contractMethod.name}' executed successfully and satisfied contract.",
            expected = "Contract Compliance",
            actual = "Compliant",
            location = SourceLocation.NotCaptured,
        )
    }

    private fun createArguments(
        function: KFunction<*>,
        context: EphemeralTestContext,
        fixtureGenerator: FixtureGenerator,
        generationContext: GenerationContext,
    ): Map<KParameter, Any?> {
        val arguments = mutableMapOf<KParameter, Any?>()

        function.parameters.forEach { param ->
            if (param.kind == KParameter.Kind.INSTANCE) {
                arguments[param] = context.getTestTarget()
                return@forEach
            }

            // [Strategy Note]
            // We deliberately skip optional parameters to respect Kotlin's default argument mechanisms.
            // TODO: Extract this logic into a 'GenerationStrategy' to allow users to override (e.g., force-fuzz optionals).
            if (param.isOptional) return@forEach
            arguments[param] = fixtureGenerator.generate(param, generationContext)
        }
        return arguments
    }

    private fun captureArgumentsToTrace(
        context: EphemeralTestContext,
        args: Map<KParameter, Any?>,
    ) {
        val valueArguments =
            args
                .filterKeys { it.kind == KParameter.Kind.VALUE }
                .entries
                .sortedBy { it.key.index }
                .map { it.value }

        context.trace.recordGeneratedArguments(valueArguments)
    }
}
