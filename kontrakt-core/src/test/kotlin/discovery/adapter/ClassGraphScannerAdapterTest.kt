package discovery.adapter

import com.bsingularity.kontrakt.discovery.spi.ClasspathScannerContract
import discovery.spi.ClasspathScanner

class ClassGraphScannerAdapterTest : ClasspathScannerContract {
    override fun createScanner(): ClasspathScanner = ClassGraphScannerAdapter()
}
