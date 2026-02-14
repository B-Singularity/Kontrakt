package discovery.adapter

import discovery.domain.exception.RuntimeIntegrityException
import discovery.domain.vo.ScanIndex
import discovery.domain.vo.ScanScope
import discovery.port.outgoing.ClasspathScanner
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult

class ClassGraphScannerAdapter : ClasspathScanner {

    override fun scan(scope: ScanScope): ScanIndex {
        RuntimeProducerGuard.ensureSingleProvider()

        return scanClassPath(scope) { scanResult ->
            val taxonomy = DiscoveryTaxonomy()
            val collector = ScanResultCollector(scanResult, taxonomy)

            try {
                // 1. Collect Scenarios
                val scenarios = collector.collectScenarios()

                // 2. Collect Contracts & Implementations
                val contracts = collector.collectContracts()

                // 3. Scan Data Contracts (Validation only)
                collector.collectDataContracts()

                // 4. Report any collected user errors
                taxonomy.throwIfFailed()

                // 5. Build Index (Enforces Determinism & Freeze internally)
                ScanIndex.of(scenarios, contracts)

            } catch (e: LinkageError) {
                // [Context-aware Failure]
                taxonomy.wrapInfrastructureError(collector.currentPhase, collector.currentTarget, e)
            }
        }
    }

    private fun <T> scanClassPath(scope: ScanScope, processor: (ScanResult) -> T): T {
        val graph = ClassGraph()
            .enableClassInfo()
            .enableAnnotationInfo()
            .ignoreClassVisibility()

        if (scope is ScanScope.Packages) {
            graph.acceptPackages(*scope.packageNames.toTypedArray())
        } else if (scope is ScanScope.Classes) {
            graph.acceptClasses(*scope.classNames.toTypedArray())
        }

        val scanResult = try {
            graph.scan()
        } catch (t: Throwable) {
            throw RuntimeIntegrityException("Classpath scan failed unexpectedly (Infrastructure).", t)
        }

        return scanResult.use(processor)
    }
}