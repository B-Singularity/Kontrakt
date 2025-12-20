package execution.adapter.junit

import discovery.adapter.ClassGraphScannerAdapter
import discovery.api.Contract
import discovery.domain.service.TestDiscovererImpl
import discovery.domain.vo.ScanScope
import exception.KontraktInternalException
import execution.domain.TestStatus
import execution.domain.vo.TestResult
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.PackageSelector

class KontraktTestEngine(
    private val runtimeFactory: KontraktRuntimeFactory = DefaultRuntimeFactory()
) : TestEngine {
    private val logger = KotlinLogging.logger {}

    override fun getId(): String = "kontrakt-engine"

    override fun discover(
        request: EngineDiscoveryRequest,
        uniqueId: UniqueId,
    ): TestDescriptor {
        val engineDescriptor = KontraktTestDescriptor(uniqueId, "kontrakt")
        val scanScope = resolveScanScope(request)

        val scanner = ClassGraphScannerAdapter()
        val discoverer = TestDiscovererImpl(scanner)

        val specs =
            runBlocking {
                discoverer
                    .discover(scanScope, Contract::class)
                    .onFailure { logger.error(it) { "Discovery failed" } }
                    .getOrDefault(emptyList())
            }

        specs.forEach { spec ->
            val specDescriptor =
                KontraktTestDescriptor(
                    uniqueId.append("spec", spec.target.fullyQualifiedName),
                    spec.target.displayName,
                    spec,
                )
            engineDescriptor.addChild(specDescriptor)
        }

        return engineDescriptor
    }

    override fun execute(request: ExecutionRequest) {
        val engineDescriptor = request.rootTestDescriptor
        val listener = request.engineExecutionListener

        listener.executionStarted(engineDescriptor)

        engineDescriptor.children.forEach { descriptor ->
            if (descriptor is KontraktTestDescriptor && descriptor.spec != null) {
                executeSpec(descriptor, listener)
            }
        }

        listener.executionFinished(engineDescriptor, TestExecutionResult.successful())
    }

    private fun executeSpec(
        descriptor: KontraktTestDescriptor,
        listener: EngineExecutionListener,
    ) {
        listener.executionStarted(descriptor)

        try {

            val scenarioExecutor = runtimeFactory.createExecutor()

            val execution = runtimeFactory.createExecution(descriptor.spec!!, scenarioExecutor)

            val result = execution.execute()

            reportResult(descriptor, result, listener)
        } catch (e: Exception) {
            logger.error(e) { "💥 Framework CRASH: ${descriptor.displayName}" }
            listener.executionFinished(descriptor, TestExecutionResult.failed(e))
        }
    }

    private fun reportResult(
        descriptor: TestDescriptor,
        result: TestResult,
        listener: EngineExecutionListener,
    ) {
        when (val status = result.finalStatus) {
            is TestStatus.Passed -> {
                logger.info { "✅ PASSED: ${descriptor.displayName}" }
                listener.executionFinished(descriptor, TestExecutionResult.successful())
            }

            is TestStatus.AssertionFailed -> {
                val error =
                    AssertionError(
                        "❌ ASSERTION FAILED: ${descriptor.displayName}\n" +
                                "   Expected: ${status.expected}\n" +
                                "   Actual:   ${status.actual}\n" +
                                "   Message:  ${status.message}",
                    )
                logger.error { error.message }
                listener.executionFinished(descriptor, TestExecutionResult.failed(error))
            }

            is TestStatus.ExecutionError -> {
                logger.error(status.cause) { "❌ EXECUTION ERROR: ${descriptor.displayName}" }
                listener.executionFinished(descriptor, TestExecutionResult.failed(status.cause))
            }

            else -> {
                val error = KontraktInternalException("Unknown Test Status encountered: $status")
                logger.error(error) { "💥 INTERNAL ERROR" }
                listener.executionFinished(descriptor, TestExecutionResult.failed(error))
            }
        }
    }

    private fun resolveScanScope(request: EngineDiscoveryRequest): ScanScope {
        val classSelector = request.getSelectorsByType(ClassSelector::class.java)
        if (classSelector.isNotEmpty()) {
            return ScanScope.Classes(classSelector.map { it.className })
        }

        val packageSelectors = request.getSelectorsByType(PackageSelector::class.java)
        if (packageSelectors.isNotEmpty()) {
            return ScanScope.Packages(packageSelectors.map { it.packageName })
        }

        return ScanScope.All
    }
}
