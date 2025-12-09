package execution.adapter

import discovery.adapter.ClassGraphScannerAdapter
import discovery.domain.service.TestDiscovererImpl
import execution.adapter.junit.KontraktTestDescriptor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.UniqueId


class KontraktTestEngine : TestEngine {

    private val logger = KotlinLogging.logger {}

    override fun getId(): String = "kontrakt-engine"

    override fun discover(request: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {
        val engineDescriptor = KontraktTestDescriptor(uniqueId, "kontrakt")

        val scanner = ClassGraphScannerAdapter()
        val discoverer = TestDiscovererImpl(scanner)

        val rootPackage = ""

        
    }
}