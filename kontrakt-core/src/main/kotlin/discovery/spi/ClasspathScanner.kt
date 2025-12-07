package discovery.spi

import kotlin.reflect.KClass

interface ClasspathScanner {
    suspend fun findAnnotatedInterfaces(
        rootPackage: String,
        annotation: KClass<out Annotation>,
    ): List<KClass<*>>

    suspend fun findAnnotatedClasses(
        rootPackage: String,
        annotation: KClass<out Annotation>,
    ): List<KClass<*>>

    suspend fun findAllImplementations(
        rootPackage: String,
        targetInterface: KClass<*>,
    ): List<KClass<*>>
}
