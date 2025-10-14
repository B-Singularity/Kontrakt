package discovery.adapter

import discovery.spi.ClasspathScanner
import discovery.spi.ClasspathScannerContract

class ClassGraphScannerAdapterTest : ClasspathScannerContract {

    override fun createScanner(): ClasspathScanner {
        return ClassGraphScannerAdapter()
    }
}