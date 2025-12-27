package discovery.domain.service

import discovery.api.TestDiscoverer
import discovery.api.TestDiscovererTest
import discovery.fakes.FakeClasspathScanner
import kotlin.reflect.KClass

class TestDiscovererImplTest : TestDiscovererTest() {
    private val fakeScanner = FakeClasspathScanner()

    override val discoverer: TestDiscoverer = TestDiscovererImpl(fakeScanner)

    override fun setupScanResult(
        interfaces: List<KClass<*>>,
        classes: List<KClass<*>>,
        implementations: Map<KClass<*>, List<KClass<*>>>,
    ) {
        fakeScanner.setup(interfaces, classes, implementations)
    }
}
