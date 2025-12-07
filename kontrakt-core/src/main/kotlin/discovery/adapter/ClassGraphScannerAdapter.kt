package discovery.adapter

import discovery.spi.ClasspathScanner
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

class ClassGraphScannerAdapter : ClasspathScanner {

    override suspend fun findAnnotatedInterfaces(
        rootPackage: String,
        annotation: KClass<out Annotation>,
    ): List<KClass<*>> = withContext(Dispatchers.IO) {
        scan(rootPackage) { scanResult ->
            scanResult
                .getClassesWithAnnotation(annotation.java.name)
                .filter { it.isInterface }
                .loadClasses()
                .map { it.kotlin }
        }
    }

    override suspend fun findAnnotatedClasses(
        rootPackage: String,
        annotation: KClass<out Annotation>
    ): List<KClass<*>> = withContext(Dispatchers.IO) {
        scan(rootPackage) { scanResult ->
            scanResult
                .getClassesWithAnnotation(annotation.java.name)
                .filter { !it.isInterface && !it.isAbstract }
                .loadClasses()
                .map { it.kotlin }
        }
    }

    override suspend fun findAllImplementations(
        rootPackage: String,
        targetInterface: KClass<*>,
    ): List<KClass<*>> = withContext(Dispatchers.IO) {
        scan(rootPackage) { scanResult ->
            scanResult
                .getClassesImplementing(targetInterface.java.name)
                .filter { !it.isInterface && !it.isAbstract }
                .loadClasses()
                .map { it.kotlin }
        }
    }

    private fun <T> scan(rootPackage: String, block: (ScanResult) -> List<T>): List<T> {
        return ClassGraph()
            .acceptPackages(rootPackage)
            .enableAnnotationInfo()
            .enableClassInfo()
            .scan()
            .use(block)
    }
}