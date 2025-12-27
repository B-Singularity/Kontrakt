package execution.domain.service

import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstructorComplianceExecutorTest {
    // Subject under test and collaborators
    private lateinit var fixtureGenerator: FixtureGenerator
    private lateinit var executor: ConstructorComplianceExecutor
    private lateinit var context: EphemeralTestContext

    @BeforeEach
    fun setup() {
        // Create mocks using Mockito
        fixtureGenerator = mock<FixtureGenerator>()
        executor = ConstructorComplianceExecutor(fixtureGenerator)
        context = mock<EphemeralTestContext>()
    }

    // =================================================================
    // 1. [Branch] Primary Constructor Existence
    // =================================================================
    @Test
    fun `validateConstructor returns empty list when no primary constructor exists`() {
        setupContextTarget(NoPrimaryConstructorClass::class)

        val results = executor.validateConstructor(context)

        assertTrue(results.isEmpty(), "Should return empty list if primary constructor is missing.")
        verifyNoInteractions(fixtureGenerator)
    }

    // =================================================================
    // 2. [Logic] Sanity Check (Valid Arguments)
    // =================================================================
    @Test
    fun `testValidConstructor returns PASSED when construction succeeds`() {
        setupContextTarget(SimpleClass::class)

        whenever(fixtureGenerator.generate(any<KParameter>())).thenReturn("valid_data")

        whenever(fixtureGenerator.generateInvalid(any())).thenReturn(emptyList())

        val results = executor.validateConstructor(context)

        val sanityRecord = results.first()
        assertEquals(AssertionStatus.PASSED, sanityRecord.status)
        assertTrue(sanityRecord.message.contains("Instance created successfully"))
    }

    @Test
    fun `testValidConstructor returns FAILED when construction throws exception`() {
        setupContextTarget(AlwaysThrowingClass::class)

        whenever(fixtureGenerator.generate(any<KParameter>())).thenReturn("valid_data")
        whenever(fixtureGenerator.generateInvalid(any())).thenReturn(emptyList())

        val results = executor.validateConstructor(context)

        val sanityRecord = results.first()
        assertEquals(AssertionStatus.FAILED, sanityRecord.status) // Should fail
        assertEquals("IllegalArgumentException", sanityRecord.actual) // Verify exact exception
    }

    // =================================================================
    // 3. [Logic] Defensive Check (Invalid Arguments)
    // =================================================================
    @Test
    fun `testInvalidConstructor returns PASSED when constructor rejects invalid value`() {
        setupContextTarget(ValidatedClass::class)

        whenever(fixtureGenerator.generate(any<KParameter>())).thenReturn("valid")
        whenever(fixtureGenerator.generateInvalid(any())).thenReturn(listOf("invalid"))

        val results = executor.validateConstructor(context)

        assertEquals(2, results.size)

        val defensiveRecord = results[1]
        assertEquals(AssertionStatus.PASSED, defensiveRecord.status)
        assertEquals("IllegalArgumentException", defensiveRecord.actual)
    }

    @Test
    fun `testInvalidConstructor returns FAILED when constructor accepts invalid value`() {
        setupContextTarget(OpenClass::class)

        whenever(fixtureGenerator.generate(any<KParameter>())).thenReturn("valid")
        whenever(fixtureGenerator.generateInvalid(any())).thenReturn(listOf("bad_value"))

        val results = executor.validateConstructor(context)

        val defensiveRecord = results[1]
        assertEquals(AssertionStatus.FAILED, defensiveRecord.status)
        assertTrue(defensiveRecord.message.contains("Constructor accepted invalid"))
    }

    // =================================================================
    // 4. [Coverage] Loop & Parameter Iteration
    // =================================================================
    @Test
    fun `validateConstructor iterates over all parameters and invalid values`() {
        setupContextTarget(MultiParamClass::class)

        whenever(fixtureGenerator.generate(any<KParameter>())).thenReturn("valid")

        whenever(fixtureGenerator.generateInvalid(argThat { name == "p1" })).thenReturn(listOf("inv1-a", "inv1-b"))
        whenever(fixtureGenerator.generateInvalid(argThat { name == "p2" })).thenReturn(listOf("inv2-a"))

        val results = executor.validateConstructor(context)

        // Expected count:
        // 1 (Sanity Check)
        // + 2 (Defensive Checks for p1)
        // + 1 (Defensive Check for p2)
        // = Total 4 records
        assertEquals(4, results.size)

        // The first record is always Sanity Check
        assertEquals(AssertionStatus.PASSED, results[0].status)
    }

    // =================================================================
    // Helper Methods & Mock Setup
    // =================================================================

    private fun setupContextTarget(kClass: KClass<*>) {
        val spec = mock<discovery.domain.aggregate.TestSpecification>()
        val target = mock<discovery.domain.vo.DiscoveredTestTarget>()

        whenever(context.specification).thenReturn(spec)
        whenever(spec.target).thenReturn(target)
        whenever(target.kClass).thenReturn(kClass)
    }

    // =================================================================
    // Dummy Classes for Testing
    // =================================================================

    // Class with no primary constructor (Secondary only)
    class NoPrimaryConstructorClass {
        constructor(a: Int)
    }

    // Standard class
    class SimpleClass(
        val a: String,
    )

    // Class that always fails construction
    class AlwaysThrowingClass(
        val a: String,
    ) {
        init {
            throw IllegalArgumentException("Boom")
        }
    }

    // Class with validation logic
    class ValidatedClass(
        val a: String,
    ) {
        init {
            if (a == "invalid") throw IllegalArgumentException()
        }
    }

    // Class with no validation (Vulnerable)
    class OpenClass(
        val a: String,
    )

    // Class with multiple parameters
    class MultiParamClass(
        val p1: String,
        val p2: String,
    )
}
