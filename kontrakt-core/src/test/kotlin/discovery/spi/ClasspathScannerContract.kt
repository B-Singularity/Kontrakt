package com.bsingularity.kontrakt.discovery.spi

import com.bsingularity.kontrakt.testfixtures.ContractToFind
import com.bsingularity.kontrakt.testfixtures.ImplementationToFind
import discovery.api.Contract
import discovery.spi.ClasspathScanner
import discovery.testfixtures.NonContractInterface
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

interface ClasspathScannerContract {
    fun createScanner(): ClasspathScanner

    private val testPackage: String
        get() = "com.bsingularity.kontrakt.testfixtures"

    @TestFactory
    fun `ClasspathScanner contract tests`() =
        listOf(
            DynamicTest.dynamicTest("should find interfaces marked with @Contract") {
                val scanner = createScanner()

                runTest {
                    val interfaces =
                        scanner.findAnnotatedInterfaces(
                            rootPackage = testPackage,
                            annotation = Contract::class,
                        )

                    assertEquals(1, interfaces.size, "Should find exactly one @Contract interface.")
                    assertEquals(ContractToFind::class, interfaces.first())
                }
            },
            DynamicTest.dynamicTest("should find all implementations of a given contract interface") {
                val scanner = createScanner()

                runTest {
                    val implementations =
                        scanner.findAllImplementations(
                            rootPackage = testPackage,
                            targetInterface = ContractToFind::class,
                        )

                    assertEquals(1, implementations.size, "Should find exactly one implementation.")
                    assertEquals(ImplementationToFind::class, implementations.first())
                }
            },
            DynamicTest.dynamicTest("should return an empty list if no implementations are found") {
                val scanner = createScanner()

                runTest {
                    val implementations =
                        scanner.findAllImplementations(
                            rootPackage = testPackage,
                            targetInterface = NonContractInterface::class,
                        )

                    assertTrue(implementations.isEmpty(), "Should return an empty list for interfaces with no implementations.")
                }
            },
        )
}
