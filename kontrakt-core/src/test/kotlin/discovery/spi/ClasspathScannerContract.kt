package discovery.spi

import discovery.api.Contract
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Contract
private interface DummyContract

private class DummyContractImpl : DummyContract

private interface NonContractInterface



interface ClasspathScannerContract {

    fun createScanner(): ClasspathScanner

    private val testPackage: String
        get() = this::class.java.`package`.name

    @TestFactory
    fun `ClasspathScanner contract tests`() = listOf(
        DynamicTest.dynamicTest("should find interfaces marked with @Contract") {
            val scanner = createScanner()

            runTest {
                val interfaces = scanner.findAnnotatedInterfaces(
                    rootPackage = testPackage,
                    annotation = Contract::class
                )

                assertEquals(1, interfaces.size, "Should find exactly one @Contract interface.")
                assertEquals(DummyContract::class, interfaces.first())
            }
        },

        DynamicTest.dynamicTest("should find all implementations of a given contract interface") {
            val scanner = createScanner()

            runTest {
                val implementations = scanner.findAllImplementations(
                    rootPackage = testPackage,
                    targetInterface = Contract::class
                )

                assertEquals(1, implementations.size, "Should find exactly one implementation.")
                assertEquals(DummyContractImpl::class, implementations.first())
            }
        },

        DynamicTest.dynamicTest("should return an empty list if no implementations are found") {
            val scanner = createScanner()

            runTest {
                val implementations = scanner.findAllImplementations(
                    rootPackage = testPackage,
                    targetInterface = NonContractInterface::class
                )

                assertTrue(implementations.isEmpty(), "Should return an empty list for interfaces with no implementations.")
            }
        }
    )
}