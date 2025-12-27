package discovery.fakes

import discovery.domain.vo.ScanScope
import discovery.spi.ClasspathScanner
import kotlin.reflect.KClass

class FakeClasspathScanner : ClasspathScanner {
    private var interfaces: List<KClass<*>> = emptyList()
    private var classes: List<KClass<*>> = emptyList()
    private var implementations: Map<KClass<*>, List<KClass<*>>> = emptyMap()

    fun setup(
        interfaces: List<KClass<*>> = emptyList(),
        classes: List<KClass<*>> = emptyList(),
        implementations: Map<KClass<*>, List<KClass<*>>> = emptyMap(),
    ) {
        this.interfaces = interfaces
        this.classes = classes
        this.implementations = implementations
    }

    override suspend fun findAnnotatedInterfaces(
        scope: ScanScope,
        annotation: KClass<out Annotation>,
    ): List<KClass<*>> = interfaces

    override suspend fun findAnnotatedClasses(
        scope: ScanScope,
        annotation: KClass<out Annotation>,
    ): List<KClass<*>> = classes

    override suspend fun findAllImplementations(
        scope: ScanScope,
        targetInterface: KClass<*>,
    ): List<KClass<*>> = implementations[targetInterface] ?: emptyList()
}
