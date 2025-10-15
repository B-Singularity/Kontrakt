package discovery.adapter

import discovery.spi.ClasspathScanner
import io.github.classgraph.ClassGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

class ClassGraphScannerAdapter : ClasspathScanner {
    override suspend fun findAnnotatedInterfaces(
        rootPackage: String,
        annotation: KClass<out Annotation>,
    ): List<KClass<*>> =
        withContext(Dispatchers.IO) {
            ClassGraph()
                .acceptPackages(rootPackage)
                .enableAnnotationInfo()
                .scan()
                .use { scanResult ->
                    scanResult
                        .getClassesWithAnnotation(annotation.java.name)
                        .filter { it.isInterface }
                        .loadClasses()
                        .map { it.kotlin }
                }
        }

    override suspend fun findAllImplementations(
        rootPackage: String,
        targetInterface: KClass<*>,
    ): List<KClass<*>> =
        withContext(Dispatchers.IO) {
            ClassGraph()
                .acceptPackages(rootPackage)
                .enableClassInfo()
                .scan()
                .use { scanResult ->
                    scanResult
                        .getClassesImplementing(targetInterface.java.name)
                        .filter { !it.isInterface && !it.isAbstract }
                        .loadClasses()
                        .map { it.kotlin }
                }
        }
}
