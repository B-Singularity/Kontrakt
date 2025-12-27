package discovery.adapter

import discovery.domain.vo.ScanScope
import discovery.spi.ClasspathScannerTest

class ClassGraphScannerAdapterTest : ClasspathScannerTest() {
    override val scanner = ClassGraphScannerAdapter()

    override val baseScope: ScanScope = ScanScope.Packages(listOf("discovery.spi"))
}
