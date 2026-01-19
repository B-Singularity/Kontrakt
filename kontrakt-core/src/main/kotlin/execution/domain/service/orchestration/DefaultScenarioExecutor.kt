package execution.domain.service.orchestration

import discovery.api.Test
import discovery.domain.aggregate.TestSpecification
import exception.KontraktConfigurationException
import exception.KontraktInternalException
import execution.domain.entity.EphemeralTestContext
import execution.domain.service.generation.FixtureGenerator
import execution.domain.service.validation.ConstructorComplianceExecutor
import execution.domain.service.validation.ContractValidator
import execution.domain.service.validation.DataComplianceExecutor
import execution.domain.vo.context.generation.GenerationContext
import execution.domain.vo.result.ExecutionResult
import execution.domain.vo.trace.ExecutionTrace
import execution.domain.vo.verification.AssertionRecord
import execution.domain.vo.verification.AssertionStatus
import execution.domain.vo.verification.SourceLocation
import execution.domain.vo.verification.StandardAssertion
import execution.port.incoming.TestScenarioExecutor
import execution.port.outgoing.MockingEngine
import execution.port.outgoing.ScenarioTrace
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Method
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.TreeMap
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
 * It is responsible for orchestrating the lifecycle of a test scenario based on the provided [TestSpecification].
 *
 * **Core Responsibilities:**
 * 1. **Orchestration:** Dispatches execution to specific strategies (User Scenario, Contract Fuzzing, Data Compliance).
 * 2. **Determinism:** Establishes a controlled environment with a Fixed Clock and Reproducible Seed.
 * 3. **Forensics:** Ensures that all execution arguments—including skipped defaults—are captured in the [ScenarioTrace].
 *
 * **Architecture Note (ADR-023):**
 * This executor implements a **Namespaced Auditing Strategy**. It separates the logs of different test modes
 * (e.g., "user.x", "data.x") into a single trace without collision, ensuring a unified but distinct audit trail.
 */
class DefaultScenarioExecutor(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val fixtureFactory: (MockingEngine, Clock, ScenarioTrace, Long) -> FixtureGenerator =
        { engine, clock, trace, seed -> FixtureGenerator(engine, clock, trace, seed) },
    private val validatorFactory: (Clock) -> ContractValidator =
        { clock -> ContractValidator(clock) },
) : TestScenarioExecutor {
    private val logger = KotlinLogging.logger {}

    /**
     * Centralized constants for Trace Namespaces to prevent "Magic String" errors and ensure log consistency.
     */
    private object TraceNamespace {
        const val USER = "user"
        const val CONTRACT = "contract"
        const val DATA = "data"
    }

    /**
     * Executes the test scenarios defined in the context's specification.
     *
     * This method guarantees **Execution Determinism** by:
     * 1. Freezing the [Clock] to a single instant for the duration of the run.
     * 2. Using a locally determined seed (without mutating the immutable spec).
     *
     * @param context The ephemeral context containing the test target and mocking engine.
     * @return [ExecutionResult] containing records, forensic arguments, and the seed used.
     */
    override fun executeScenarios(context: EphemeralTestContext): ExecutionResult {
        // [Strategy] Time Determinism
        // Capture the current instant ONCE and freeze it. All components (Generators, Validators) will see the same time.
        val currentInstant = Instant.now(clock)
        val fixedClock = Clock.fixed(currentInstant, ZoneId.systemDefault())

        // [Strategy] Seed Reproducibility (Immutable Blueprint)
        // If the spec has a seed, use it. Otherwise, generate a new one locally.
        // Crucially, we DO NOT mutate context.specification.seed to preserve the purity of the input blueprint.
        val effectiveSeed = context.specification.seed ?: System.currentTimeMillis()

        // Initialize Domain Services with the deterministic context
        val fixtureGenerator = fixtureFactory(context.mockingEngine, fixedClock, context.trace, effectiveSeed)
        val contractValidator = validatorFactory(fixedClock)
        val generationContext = GenerationContext(Random(effectiveSeed), fixedClock)

        // Initialize Helper Executors
        val constructorExecutor = ConstructorComplianceExecutor(fixtureGenerator)
        val dataComplianceExecutor = DataComplianceExecutor(fixtureGenerator, constructorExecutor)

        val records = mutableListOf<AssertionRecord>()
        val modes = context.specification.modes

        // =====================================================================
        // Mode 1: User Scenario Execution
        // Trace Namespace: "user."
        // Description: Runs methods annotated with @Test defined by the user.
        // =====================================================================
        if (modes.contains(TestSpecification.TestMode.UserScenario)) {
            records.addAll(
                executeUserTestMethods(context, fixtureGenerator, generationContext),
            )
        }

        // =====================================================================
        // Mode 2: Contract Auto Fuzzing
        // Trace Namespace: "contract."
        // Description: Fuzzes methods defined in the interface contract using property-based testing.
        // =====================================================================
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

        // =====================================================================
        // Mode 3: Data Compliance
        // Trace Namespace: "data."
        // Description: Verifies data class contracts (equals, hashCode, symmetry, serializability).
        // =====================================================================
        modes.filterIsInstance<TestSpecification.TestMode.DataCompliance>().forEach { _ ->
            val result = dataComplianceExecutor.execute(context, generationContext)
            records.addAll(result.records)

            // [Forensics] Pipe captured arguments to the centralized Trace.
            // We apply the "data." namespace here to keep it distinct from user/contract args.
            if (result.capturedArgs.isNotEmpty()) {
                val namespacedArgs =
                    result.capturedArgs.mapKeys { entry ->
                        applyNamespace(TraceNamespace.DATA, entry.key)
                    }
                // Use TreeMap to guarantee deterministic ordering before adding to trace
                context.trace.addGeneratedArguments(TreeMap(namespacedArgs))
            }
        }

        // [Result] Return comprehensive result including the seed used for this run.
        // This ensures the run is reproducible even if the original Spec didn't have a seed.
        return ExecutionResult(
            records = records,
            arguments = context.trace.generatedArguments,
            seed = effectiveSeed,
        )
    }

    /**
     * Finds and executes all user-defined test methods (@Test).
     *
     * **Safety Note:** Methods are sorted by name to ensure that the execution order
     * (and thus the Random sequence) remains consistent across runs, preventing "Flaky Tests"
     * caused by non-deterministic reflection order.
     */
    private fun executeUserTestMethods(
        context: EphemeralTestContext,
        fixtureGenerator: FixtureGenerator,
        generationContext: GenerationContext,
    ): List<AssertionRecord> {
        val testInstance = context.getTestTarget()
        val kClass = testInstance::class

        // [Stability] Sort methods by name to guarantee deterministic execution order.
        val testFunctions =
            kClass.functions
                .filter { it.findAnnotation<Test>() != null }
                .sortedBy { it.name }

        if (testFunctions.isEmpty()) {
            logger.warn { "UserScenario mode active but no methods annotated with @Test found in ${kClass.simpleName}" }
            return emptyList()
        }

        return testFunctions.map { kFunc ->
            context.targetMethod = kFunc.javaMethod
                ?: throw KontraktInternalException("Reflection failed for '${kFunc.name}' in ${kClass.simpleName}")

            // 1. Prepare Call Args (Actual execution arguments, skipping optionals)
            val callArgs = createCallArguments(kFunc, context, fixtureGenerator, generationContext)

            // 2. Record Forensic State (Audit args including "[Default]" markers)
            val formattedArgs = captureAllParameters(context, kFunc, callArgs, prefix = TraceNamespace.USER)

            // 3. Execute with Observability Wrapper
            // Uses try-finally to ensure the ExecutionTrace is recorded even if the test throws an exception.
            executeWithRecording(context, kFunc.name, formattedArgs) {
                if (kFunc.isSuspend) {
                    runBlocking { kFunc.callSuspendBy(callArgs) }
                } else {
                    kFunc.callBy(callArgs)
                }
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

    /**
     * Executes property-based fuzzing against the Contract Interface.
     *
     * **Optimization:** Uses Java Reflection (`getMethod`) for robust O(1) method lookup
     * instead of O(N) Kotlin sequence filtering, which significantly improves performance
     * on large classes.
     */
    private fun executeContractFuzzing(
        context: EphemeralTestContext,
        contractClass: Class<*>,
        fixtureGenerator: FixtureGenerator,
        contractValidator: ContractValidator,
        generationContext: GenerationContext,
    ): List<AssertionRecord> {
        val implementationKClass = context.getTestTarget()::class
        val implementationJavaClass = implementationKClass.java

        return contractClass.methods
            .filter { !it.isBridge && !it.isSynthetic }
            .sortedBy { it.name } // Deterministic Order
            .map { contractMethod ->

                // [Optimization] Direct Method Lookup via Java Reflection
                val implementationMethod =
                    try {
                        implementationJavaClass.getMethod(contractMethod.name, *contractMethod.parameterTypes)
                    } catch (e: NoSuchMethodException) {
                        throw KontraktConfigurationException(
                            "Contract violation: Implementation of '${contractMethod.name}' not found in '${implementationKClass.simpleName}'",
                            e,
                        )
                    }

                // Metadata Resolution for Kotlin-specific features (Suspend, etc.)
                val implementationKFunc =
                    implementationMethod.kotlinFunction
                        ?: throw KontraktInternalException("Kotlin metadata missing for implementation '${contractMethod.name}'")
                val contractKFunc =
                    contractMethod.kotlinFunction
                        ?: throw KontraktInternalException("Kotlin metadata missing for contract '${contractMethod.name}'")

                context.targetMethod = contractMethod

                executeMethod(
                    contractMethod,
                    implementationKFunc,
                    contractKFunc,
                    context,
                    fixtureGenerator,
                    contractValidator,
                    generationContext,
                )
            }
    }

    /**
     * Helper to execute a single contract method and validate it against the contract constraints.
     */
    private fun executeMethod(
        contractMethod: Method,
        implFunc: KFunction<*>,
        contractKFunc: KFunction<*>,
        context: EphemeralTestContext,
        fixtureGenerator: FixtureGenerator,
        contractValidator: ContractValidator,
        generationContext: GenerationContext,
    ): AssertionRecord {
        val callArgs = createCallArguments(implFunc, context, fixtureGenerator, generationContext)
        val formattedArgs = captureAllParameters(context, implFunc, callArgs, prefix = TraceNamespace.CONTRACT)

        var result: Any? = null

        // [Reliability] Execution is wrapped to guarantee trace recording.
        executeWithRecording(context, contractMethod.name, formattedArgs) {
            result =
                if (implFunc.isSuspend) {
                    runBlocking { implFunc.callSuspendBy(callArgs) }
                } else {
                    implFunc.callBy(callArgs)
                }
        }

        // Post-execution Contract Validation
        contractValidator.validate(contractKFunc, result)

        return AssertionRecord(
            status = AssertionStatus.PASSED,
            rule = StandardAssertion,
            message = "Method '${contractMethod.name}' executed successfully.",
            expected = "Contract Compliance",
            actual = "Compliant",
            location = SourceLocation.NotCaptured,
        )
    }

    // =========================================================================
    // Helper Functions
    // =========================================================================

    /**
     * Executes the given [block] while measuring execution time.
     * GUARANTEES that an [ExecutionTrace] is recorded to the [context] via `try-finally`,
     * ensuring observability even if the test method throws an exception.
     */
    private inline fun executeWithRecording(
        context: EphemeralTestContext,
        methodName: String,
        formattedArgs: Map<String, Any?>,
        block: () -> Unit,
    ) {
        val start = clock.millis()
        try {
            block()
        } finally {
            val duration = clock.millis() - start
            recordExecutionTrace(context, methodName, formattedArgs, duration)
        }
    }

    /**
     * Formats and appends an [ExecutionTrace] event to the scenario trace.
     * The arguments are sorted alphabetically to ensure a deterministic log output.
     */
    private fun recordExecutionTrace(
        context: EphemeralTestContext,
        methodName: String,
        formattedArgs: Map<String, Any?>,
        durationMs: Long,
    ) {
        val argList =
            formattedArgs.entries
                .sortedBy { it.key }
                .map { "${it.key}=${it.value}" }

        val event =
            ExecutionTrace(
                methodSignature = methodName,
                arguments = argList,
                durationMs = durationMs,
                timestamp = clock.millis(),
            )
        context.trace.add(event)
    }

    /**
     * Generates the **Minimum Viable Arguments** required for execution.
     *
     * **Strategy:**
     * - **Instance Parameters:** Injected from context (System Under Test).
     * - **Value Parameters:** Generated via FixtureGenerator.
     * - **Optional Parameters:** SKIPPED explicitly. This allows Kotlin's default argument mechanism
     * to take over at runtime, ensuring we test the "natural" behavior of the method as intended by the developer.
     */
    private fun createCallArguments(
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
            if (param.isOptional) return@forEach
            arguments[param] = fixtureGenerator.generate(param, generationContext)
        }
        return arguments
    }

    /**
     * Captures a **Full Forensic Snapshot** of all parameters for the Audit Log.
     *
     * **Strategy:**
     * - **Generated Args:** Logged as is.
     * - **Skipped Optionals:** Logged with the marker `[Default]` to clearly indicate that
     * the system used the default value defined in the function signature.
     * - **Namespace:** Applied to all keys (e.g., "user.amount") to prevent collisions between modes.
     *
     * @return A sorted TreeMap of the captured arguments (for consistency).
     */
    private fun captureAllParameters(
        context: EphemeralTestContext,
        function: KFunction<*>,
        generatedArgs: Map<KParameter, Any?>,
        prefix: String,
    ): Map<String, Any?> {
        val fullTraceMap = mutableMapOf<String, Any?>()

        function.parameters.forEach { param ->
            if (param.kind != KParameter.Kind.VALUE) return@forEach

            val paramName = param.name ?: "arg-${param.index}"
            val namespacedKey = applyNamespace(prefix, paramName)

            if (generatedArgs.containsKey(param)) {
                // Case A: Argument was explicitly generated and passed
                fullTraceMap[namespacedKey] = generatedArgs[param]
            } else if (param.isOptional) {
                // Case B: Argument was skipped (Optional) -> Log as Default
                // This is critical for debugging: knowing that a default was used is as important as knowing a value.
                fullTraceMap[namespacedKey] = "[Default]"
            } else {
                // Case C: Missing (Should generally not happen if createCallArguments works correctly)
                fullTraceMap[namespacedKey] = "[Missing]"
            }
        }

        // Use TreeMap to guarantee explicit Sorting and Map interface compatibility
        val sortedMap = TreeMap(fullTraceMap)
        context.trace.addGeneratedArguments(sortedMap)
        return sortedMap
    }

    /**
     * Helper to apply a namespace prefix consistently.
     * Prevents double prefixing if keys are already manually constructed (Defensive Coding).
     */
    private fun applyNamespace(
        prefix: String,
        key: String,
    ): String = if (key.startsWith("$prefix.")) key else "$prefix.$key"
}
