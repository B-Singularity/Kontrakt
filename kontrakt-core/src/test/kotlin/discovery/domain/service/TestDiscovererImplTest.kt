package discovery.domain.service

import discovery.api.TestDiscoverer
import discovery.api.TestDiscovererTest
import discovery.domain.vo.ScanScope
import discovery.spi.ClasspathScanner
import kotlin.reflect.KClass

class TestDiscovererImplTest : TestDiscovererTest() {
    // Uses the inner FakeClasspathScanner
    private val fakeScanner = FakeClasspathScanner()

    override val discoverer: TestDiscoverer = TestDiscovererImpl(fakeScanner)

    override fun setupScanResult(
        interfaces: List<KClass<*>>,
        classes: List<KClass<*>>,
        implementations: Map<KClass<*>, List<KClass<*>>>,
    ) {
        fakeScanner.setup(interfaces, classes, implementations)
    }

    // --- Fake Scanner Implementation ---
    class FakeClasspathScanner : ClasspathScanner {
        private var interfaces: List<KClass<*>> = emptyList()
        private var classes: List<KClass<*>> = emptyList()
        private var implementations: Map<KClass<*>, List<KClass<*>>> = emptyMap()

        fun setup(
            interfaces: List<KClass<*>>,
            classes: List<KClass<*>>,
            implementations: Map<KClass<*>, List<KClass<*>>>,
        ) {
            this.interfaces = interfaces
            this.classes = classes
            this.implementations = implementations
        }

        override suspend fun findAnnotatedInterfaces(
            scope: ScanScope,
            annotation: KClass<out Annotation>,
        ): List<KClass<*>> {
            // In a real fake, we might filter by annotation,
            // but for this test setup, we return what was pre-configured.
            return interfaces
        }

        override suspend fun findAnnotatedClasses(
            scope: ScanScope,
            annotation: KClass<out Annotation>,
        ): List<KClass<*>> {
            return classes
        }

        override suspend fun findAllImplementations(
            scope: ScanScope,
            targetInterface: KClass<*>,
        ): List<KClass<*>> {
            return implementations[targetInterface] ?: emptyList()
        }
    }
}