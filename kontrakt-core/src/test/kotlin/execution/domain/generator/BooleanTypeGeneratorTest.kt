package execution.domain.generator

import discovery.api.AssertFalse
import discovery.api.AssertTrue
import java.time.Clock
import kotlin.random.Random
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BooleanTypeGeneratorTest {

    private val generator = BooleanTypeGenerator()
    private val context = GenerationContext(
        seededRandom = Random(42),
        clock = Clock.systemDefaultZone()
    )

    // =================================================================
    // 1. Test Targets
    // =================================================================

    @Suppress("UNUSED_PARAMETER")
    fun plainBoolean(arg: Boolean) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun withAssertTrue(@AssertTrue arg: Boolean) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun withAssertFalse(@AssertFalse arg: Boolean) {
    }
    

    @Suppress("UNUSED_PARAMETER")
    fun notBoolean(arg: String) {
    }

    // =================================================================
    // 2. Helpers & Data Structures
    // =================================================================

    private fun request(func: KFunction<*>): GenerationRequest {
        val param = func.parameters.first()
        return GenerationRequest.from(param)
    }

    data class BooleanContractScenario(
        val description: String,
        val targetFunction: KFunction<*>,
        val expectedValid: Set<Boolean>,
        val expectedInvalid: Set<Boolean>
    )

    // =================================================================
    // 3. Contract Tests
    // =================================================================

    @Test
    fun `Support Contract - verifies type compatibility`() {
        val booleanReq = request(::plainBoolean)
        val stringReq = request(::notBoolean)

        assertTrue(generator.supports(booleanReq), "Should support Boolean type")
        assertFalse(generator.supports(stringReq), "Should not support non-Boolean type")
    }

    @Test
    fun `Generation Contract - verifies all branching logic (Table-Driven)`() {
        val scenarios = listOf(
            BooleanContractScenario(
                description = "Plain Boolean (No Annotations)",
                targetFunction = ::plainBoolean,
                expectedValid = setOf(true, false),
                expectedInvalid = emptySet()
            ),
            BooleanContractScenario(
                description = "Annotated with @AssertTrue",
                targetFunction = ::withAssertTrue,
                expectedValid = setOf(true),
                expectedInvalid = setOf(false)
            ),
            BooleanContractScenario(
                description = "Annotated with @AssertFalse",
                targetFunction = ::withAssertFalse,
                expectedValid = setOf(false),
                expectedInvalid = setOf(true)
            )
        )

        for (scenario in scenarios) {
            val req = request(scenario.targetFunction)
            val caseName = "[Scenario: ${scenario.description}]"

            // 1. Generate
            val generated = generator.generate(req, context)
            assertIs<Boolean>(generated, "$caseName Generate should return Boolean")
            assertTrue(
                scenario.expectedValid.contains(generated),
                "$caseName Generated value '$generated' should be in expected valid set"
            )

            // 2. Valid Boundaries
            val boundaries = generator.generateValidBoundaries(req, context)
            assertEquals(
                scenario.expectedValid,
                boundaries.toSet(),
                "$caseName Boundaries should match expected valid set"
            )

            // 3. Invalid Values
            val invalids = generator.generateInvalid(req, context)
            assertEquals(
                scenario.expectedInvalid,
                invalids.toSet(),
                "$caseName Invalid values should match expected invalid set"
            )
        }
    }

}