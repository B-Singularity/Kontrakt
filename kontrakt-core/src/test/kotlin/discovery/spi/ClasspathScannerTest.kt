package discovery.spi

import discovery.domain.vo.ScanScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class ClasspathScannerTest {
    protected abstract val scanner: ClasspathScanner

    protected abstract val baseScope: ScanScope

    @Target(AnnotationTarget.CLASS)
    annotation class ContractMarker

    @ContractMarker
    interface TargetInterface

    @ContractMarker
    class TargetClass

    @ContractMarker
    abstract class AbstractTargetClass

    interface ParentInterface

    class ChildImpl : ParentInterface

    abstract class AbstractChild : ParentInterface

    @Test
    fun `findAnnotatedInterfaces should find only interfaces annotated`() =
        runTest {
            val result = scanner.findAnnotatedInterfaces(baseScope, ContractMarker::class)

            assertContains(result, TargetInterface::class, "Should find the interface annotated with @ContractMarker")
            assertTrue(result.none { it == TargetClass::class }, "Should NOT find classes, even if annotated")
        }

    @Test
    fun `findAnnotatedClasses should find only classes annotated`() =
        runTest {
            val result = scanner.findAnnotatedClasses(baseScope, ContractMarker::class)

            assertContains(result, TargetClass::class, "Should find the class annotated with @ContractMarker")
            assertTrue(result.none { it == TargetInterface::class }, "Should NOT find interfaces")
        }

    @Test
    fun `findAllImplementations should find concrete implementations`() =
        runTest {
            val result = scanner.findAllImplementations(baseScope, ParentInterface::class)

            assertContains(result, ChildImpl::class, "Should find the concrete implementation")
            assertTrue(result.none { it == AbstractChild::class }, "Should NOT find abstract implementations")
        }

    @Test
    fun `ScanScope Classes - should find only the specifically listed class`() =
        runTest {
            val targetName = TargetClass::class.java.name
            val specificScope = ScanScope.Classes(listOf(targetName))

            val result = scanner.findAnnotatedClasses(specificScope, ContractMarker::class)

            assertEquals(1, result.size, "Should find exactly one class")
            assertEquals(TargetClass::class, result.first(), "Should find the specified class")

            val interfaceResult = scanner.findAnnotatedInterfaces(specificScope, ContractMarker::class)
            assertTrue(interfaceResult.isEmpty(), "Should not find objects outside the specified class scope")
        }

    @Test
    fun `ScanScope Packages - should not find classes in excluded packages`() =
        runTest {
            val invalidScope = ScanScope.Packages(listOf("com.non.existent.package"))

            val result = scanner.findAnnotatedClasses(invalidScope, ContractMarker::class)

            assertTrue(result.isEmpty(), "Should return empty list for out-of-scope packages")
        }
}
