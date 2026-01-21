package discovery.adapter

import discovery.domain.vo.ScanScope
import discovery.port.outcoming.ClasspathScanner
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * [ADR-025] Test Interface Pattern.
 * Defines the contract for [ClasspathScanner] behavior.
 *
 * This contract ensures 100% branch coverage for:
 * 1. [Filtering Logic]: Distinguishing Interfaces, Abstract Classes, and Concrete Classes.
 * 2. [Scope Logic]: Respecting ScanScope constraints (Packages, Classes, All).
 * 3. [Annotation Logic]: Correctly identifying targets based on annotations.
 */
interface ClasspathScannerContract {
    /**
     * The Subject Under Test (SUT).
     */
    val scanner: ClasspathScanner

    /**
     * [Fix] Share the TestDispatcher between runTest and the Adapter.
     */
    val testDispatcher: TestDispatcher

    /**
     * Defines the default scope for general filtering tests.
     */
    val testScope: ScanScope
        get() = ScanScope.Packages(setOf("discovery.adapter"))

    // region Function: findAnnotatedInterfaces

    @Test
    fun `findAnnotatedInterfaces - returns interfaces that have the specified annotation`() =
        runTest(testDispatcher) {
            // Branch: Filter -> isInterface == True
            val result = scanner.findAnnotatedInterfaces(testScope, ScannableAnnotation::class)
            assertThat(result).contains(ScannableInterface::class)
        }

    @Test
    fun `findAnnotatedInterfaces - does not return concrete classes even if they have the annotation`() =
        runTest(testDispatcher) {
            // Branch: Filter -> isInterface == False (Class)
            val result = scanner.findAnnotatedInterfaces(testScope, ScannableAnnotation::class)
            assertThat(result).doesNotContain(ScannableClass::class)
        }

    @Test
    fun `findAnnotatedInterfaces - does not return interfaces that do not have the annotation`() =
        runTest(testDispatcher) {
            // Branch: Annotation Check -> False
            val result = scanner.findAnnotatedInterfaces(testScope, ScannableAnnotation::class)
            assertThat(result).doesNotContain(PlainInterface::class)
        }

    // endregion

    // region Function: findAnnotatedClasses

    @Test
    fun `findAnnotatedClasses - returns concrete classes that have the specified annotation`() =
        runTest(testDispatcher) {
            // Branch: Filter -> !isInterface && !isAbstract (Concrete Class)
            val result = scanner.findAnnotatedClasses(testScope, ScannableAnnotation::class)
            assertThat(result).contains(ScannableClass::class)
        }

    @Test
    fun `findAnnotatedClasses - does not return interfaces even if they have the annotation`() =
        runTest(testDispatcher) {
            // Branch: Filter -> isInterface == True
            val result = scanner.findAnnotatedClasses(testScope, ScannableAnnotation::class)
            assertThat(result).doesNotContain(ScannableInterface::class)
        }

    @Test
    fun `findAnnotatedClasses - does not return abstract classes even if they have the annotation`() =
        runTest(testDispatcher) {
            // Branch: Filter -> isAbstract == True
            val result = scanner.findAnnotatedClasses(testScope, ScannableAnnotation::class)
            assertThat(result).doesNotContain(AbstractScannableClass::class)
        }

    @Test
    fun `findAnnotatedClasses - respects Classes scope (ScanScope_Classes)`() =
        runTest(testDispatcher) {
            // Branch: Scope -> ScanScope.Classes
            // Scenario: Explicitly request ONLY 'ScannableClass'.
            val specificScope = ScanScope.Classes(setOf(ScannableClass::class.qualifiedName!!))

            val result = scanner.findAnnotatedClasses(specificScope, ScannableAnnotation::class)

            assertThat(result)
                .contains(ScannableClass::class)
                .doesNotContain(OtherScannableClass::class)
        }

    // endregion

    // region Function: findAllImplementations

    @Test
    fun `findAllImplementations - returns concrete classes implementing the target interface`() =
        runTest(testDispatcher) {
            // Branch: Filter -> Concrete Implementation
            val result = scanner.findAllImplementations(testScope, TargetInterface::class)
            assertThat(result).contains(ConcreteImplementation::class)
        }

    @Test
    fun `findAllImplementations - does not return the interface itself`() =
        runTest(testDispatcher) {
            // Branch: Filter -> Interface itself (implicitly handled by ClassGraph logic, but verification is good)
            val result = scanner.findAllImplementations(testScope, TargetInterface::class)
            assertThat(result).doesNotContain(TargetInterface::class)
        }

    @Test
    fun `findAllImplementations - does not return abstract implementations`() =
        runTest(testDispatcher) {
            // Branch: Filter -> Abstract Implementation
            val result = scanner.findAllImplementations(testScope, TargetInterface::class)
            assertThat(result).doesNotContain(AbstractImplementation::class)
        }

    @Test
    fun `findAllImplementations - does not return unrelated classes`() =
        runTest(testDispatcher) {
            // Branch: Logic -> Unrelated Class
            val result = scanner.findAllImplementations(testScope, TargetInterface::class)
            assertThat(result).doesNotContain(UnrelatedClass::class)
        }

    // endregion

    // region Function: Scope Coverage

    @Test
    fun `scan - respects All scope (Branch Coverage)`() =
        runTest(testDispatcher) {
            // Branch: Scope -> ScanScope.All
            // Verifies that the 'All' branch in the scan method is executed.
            val result = scanner.findAnnotatedClasses(ScanScope.All, ScannableAnnotation::class)
            assertThat(result).contains(ScannableClass::class)
        }

    // endregion
}

/**
 * [ADR-025] Concrete Implementation.
 */
class ClassGraphScannerAdapterTest : ClasspathScannerContract {
    override val testDispatcher = StandardTestDispatcher()

    override val scanner: ClasspathScanner = ClassGraphScannerAdapter(testDispatcher)
}

// -----------------------------------------------------------------------------
// Test Fixtures
// -----------------------------------------------------------------------------

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScannableAnnotation

@ScannableAnnotation
interface ScannableInterface

@ScannableAnnotation
class ScannableClass

@ScannableAnnotation
class OtherScannableClass

@ScannableAnnotation
abstract class AbstractScannableClass

interface PlainInterface

interface TargetInterface

class ConcreteImplementation : TargetInterface

abstract class AbstractImplementation : TargetInterface

class UnrelatedClass