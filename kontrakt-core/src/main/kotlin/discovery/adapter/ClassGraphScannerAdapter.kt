package discovery.adapter

import discovery.domain.vo.ScanScope
import discovery.spi.ClasspathScanner
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

class ClassGraphScannerAdapter : ClasspathScanner {
    override suspend fun findAnnotatedInterfaces(
        scope: ScanScope,
        annotation: KClass<out Annotation>,
    ): List<KClass<*>> =
        withContext(Dispatchers.IO) {
            scan(scope) { scanResult ->
                scanResult
                    .getClassesWithAnnotation(annotation.java.name)
                    .filter { it.isInterface }
                    .loadClasses()
                    .map { it.kotlin }
            }
        }

    override suspend fun findAnnotatedClasses(
        scope: ScanScope,
        annotation: KClass<out Annotation>,
    ): List<KClass<*>> =
        withContext(Dispatchers.IO) {
            scan(scope) { scanResult ->
                scanResult
                    .getClassesWithAnnotation(annotation.java.name)
                    .filter { !it.isInterface && !it.isAbstract }
                    .loadClasses()
                    .map { it.kotlin }
            }
        }

    override suspend fun findAllImplementations(
        scope: ScanScope,
        targetInterface: KClass<*>,
    ): List<KClass<*>> =
        withContext(Dispatchers.IO) {
            scan(scope) { scanResult ->
                scanResult
                    .getClassesImplementing(targetInterface.java.name)
                    .filter { !it.isInterface && !it.isAbstract }
                    .loadClasses()
                    .map { it.kotlin }
            }
        }

    private fun <T> scan(
        scope: ScanScope,
        block: (ScanResult) -> List<T>,
    ): List<T> {
        val classGraph =
            ClassGraph()
                .enableAnnotationInfo()
                .enableClassInfo()

        when (scope) {
            is ScanScope.All -> {
                classGraph.acceptPackages("")
            }

            is ScanScope.Packages -> {
                classGraph.acceptPackages(*scope.packageNames.toTypedArray())
            }

            is ScanScope.Classes -> {
                classGraph.acceptClasses(*scope.classNames.toTypedArray())
            }
        }
        return classGraph.scan().use(block)
    }
}
