package discovery.spi

import discovery.domain.vo.ScanScope
import kotlin.reflect.KClass

interface ClasspathScanner {
    suspend fun findAnnotatedInterfaces(
        scope: ScanScope,
        annotation: KClass<out Annotation>,
    ): List<KClass<*>>

    suspend fun findAnnotatedClasses(
        scope: ScanScope,
        annotation: KClass<out Annotation>,
    ): List<KClass<*>>

    suspend fun findAllImplementations(
        scope: ScanScope,
        targetInterface: KClass<*>,
    ): List<KClass<*>>
}
