package execution.domain.generator

import discovery.api.Size
import execution.exception.CollectionSizeLimitExceededException
import java.time.Clock
import kotlin.random.Random
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ArrayTypeGeneratorTest {

    private val generator = ArrayTypeGenerator()

    private val context = GenerationContext(
        seededRandom = Random(42),
        clock = Clock.systemDefaultZone()
    )

    @Suppress("UNUSED_PARAMETER")
    class ArrayTargets {
        // [Type Branches]
        fun intArray(arr: IntArray) {}              // Primitive
        fun stringArray(arr: Array<String>) {}      // Object
        fun multidimensional(arr: Array<IntArray>) {} // Nested/Recursive

        // [Size Logic Branches]
        fun defaultSize(arr: IntArray) {}           // No Annotation
        fun sizeRange(@Size(min = 2, max = 4) arr: IntArray) {}
        fun fixedSize(@Size(min = 3, max = 3) arr: IntArray) {}
        fun minOnly(@Size(min = 10) arr: IntArray) {} // Implicit Max
        fun inverted(@Size(min = 5, max = 2) arr: IntArray) {} // Defensive Copy

        // [Edge Cases]
        fun minZero(@Size(min = 0, max = 2) arr: IntArray) {}
        fun maxInt(@Size(min = 1, max = Int.MAX_VALUE) arr: IntArray) {}

        // [Safety Limits]
        fun largeLimit(@Size(min = 0, max = 2000) arr: IntArray) {}
        fun explicitHuge(@Size(min = 1001) arr: IntArray) {}
        fun ignoreLimit(@Size(min = 0, max = 2000, ignoreLimit = true) arr: IntArray) {}

        // [Unsupported]
        fun listType(list: List<String>) {}
        fun stringType(str: String) {}
    }

    private fun request(func: KFunction<*>): GenerationRequest {
        val param = func.parameters.last()
        return GenerationRequest.from(param)
    }

    private val mockRegenerator: (GenerationRequest, GenerationContext) -> Any? = { req, _ ->
        when (req.type.classifier) {
            Int::class -> 1
            String::class -> "A"
            IntArray::class -> intArrayOf(9)
            else -> null
        }
    }

    @Test
    fun `Support Contract - verifies compatibility for Primitives and Objects`() {
        val targets = ArrayTargets()

        // 1. Primitive Array -> True
        assertTrue(generator.supports(request(targets::intArray)))

        // 2. Object Array -> True
        assertTrue(generator.supports(request(targets::stringArray)))

        // 3. Multidimensional Array -> True
        assertTrue(generator.supports(request(targets::multidimensional)))

        // 4. Non-Array types -> False
        assertFalse(generator.supports(request(targets::listType)))
        assertFalse(generator.supports(request(targets::stringType)))
    }

    @Test
    fun `Generation Contract - verifies Array creation and population`() {
        // [Branch 1] Primitive Type Creation (IntArray)
        val primReq = request(ArrayTargets::intArray)
        val primRes = generator.generator(primReq, context, mockRegenerator)
        assertIs<IntArray>(primRes)
        assertEquals(5, primRes.size, "Default size should be 5")
        assertEquals(1, primRes[0], "Should be populated via regenerator")

        // [Branch 2] Object Type Creation (Array<String>)
        val objReq = request(ArrayTargets::stringArray)
        val objRes = generator.generator(objReq, context, mockRegenerator)
        assertIs<Array<String>>(objRes)
        assertEquals(5, objRes.size)
        assertEquals("A", objRes[0])

        // [Branch 3] Recursion Check (Array<IntArray>)
        val multiReq = request(ArrayTargets::multidimensional)
        val multiRes = generator.generator(multiReq, context, mockRegenerator)
        assertIs<Array<IntArray>>(multiRes)
        assertEquals(5, multiRes.size)
        assertIs<IntArray>(multiRes[0])
    }

    @Test
    fun `Size Logic Contract - verifying calculation logic branches`() {
        // 1. Default Size (No Annotation)
        // Logic: maxOf(min(0), DEFAULT(5)) -> 5
        val defaultArr = generator.generator(request(ArrayTargets::defaultSize), context, mockRegenerator) as IntArray
        assertEquals(5, defaultArr.size)

        // 2. Min Only (Implicit Max)
        // Logic: maxOf(min(10), DEFAULT(5)) -> 10
        val minOnlyArr = generator.generator(request(ArrayTargets::minOnly), context, mockRegenerator) as IntArray
        assertEquals(10, minOnlyArr.size)

        // 3. Inverted Bounds (Defensive Logic)
        // Logic: max coerced to min -> size 5
        val invArr = generator.generator(request(ArrayTargets::inverted), context, mockRegenerator) as IntArray
        assertEquals(5, invArr.size)

        // 4. Range Logic (Randomness)
        // Random(42) with range [2, 4] -> Should be within bounds
        val rangeReq = request(ArrayTargets::sizeRange)
        repeat(20) {
            val res = generator.generator(rangeReq, context, mockRegenerator) as IntArray
            assertTrue(res.size in 2..4, "Size ${res.size} out of bounds [2,4]")
        }
    }

    @Test
    fun `Safety Contract - validates GLOBAL_LIMIT constraints`() {
        // Malicious Context: Always returns 1500 (Unsafe size)
        val maliciousContext = GenerationContext(
            seededRandom = object : Random() {
                override fun nextBits(bitCount: Int) = 0
                override fun nextInt(from: Int, until: Int) = 1500
            },
            clock = Clock.systemDefaultZone()
        )

        // 1. [Branch] Limit Exceeded -> Exception
        // targetSize(1500) > LIMIT(1000) && !ignoreLimit
        assertFailsWith<CollectionSizeLimitExceededException> {
            generator.generator(request(ArrayTargets::largeLimit), maliciousContext, mockRegenerator)
        }.also {
            val msg = it.message.orEmpty()
            assertTrue(msg.contains("1500"), "Exception message should contain actual size (1500)")
            assertTrue(msg.contains("1000"), "Exception message should contain limit (1000)")
        }

        // 2. [Branch] Explicit Min > Limit -> Allowed
        // min(1001) > LIMIT(1000) implies explicit intent
        val explicitArr = generator.generator(request(ArrayTargets::explicitHuge), context, mockRegenerator) as IntArray
        assertEquals(1001, explicitArr.size)

        // 3. [Branch] Ignore Limit -> Allowed
        // ignoreLimit=true skips validation
        val ignoreArr =
            generator.generator(request(ArrayTargets::ignoreLimit), maliciousContext, mockRegenerator) as IntArray
        assertEquals(1500, ignoreArr.size)
    }

    @Test
    fun `Boundaries Contract - verifies boundary generation logic`() {
        // Case 1: Normal Range (min=2, max=4)
        val list1 = generator.generateValidBoundaries(request(ArrayTargets::sizeRange), context, mockRegenerator)
        assertEquals(2, list1.size)
        assertEquals(2, (list1[0] as IntArray).size, "Min Boundary")
        assertEquals(4, (list1[1] as IntArray).size, "Max Boundary")

        // Case 2: Fixed Size (min=3, max=3) -> Single boundary
        // Logic: if (max != min) check fails
        val list2 = generator.generateValidBoundaries(request(ArrayTargets::fixedSize), context, mockRegenerator)
        assertEquals(1, list2.size)
        assertEquals(3, (list2[0] as IntArray).size)

        // Case 3: Safety Limit Check on Boundary
        // If max boundary is unsafe, it should be skipped (unless explicitly allowed)
        // Using largeLimit (max=2000). Max boundary (2000) > 1000 -> Skipped.
        val list3 = generator.generateValidBoundaries(request(ArrayTargets::largeLimit), context, mockRegenerator)
        assertEquals(1, list3.size, "Should only generate Min boundary (0)")
        assertEquals(0, (list3[0] as IntArray).size)
    }

    @Test
    fun `Invalid Contract - verifies off-by-one error generation`() {
        // Case 1: Standard Range (min=2, max=4)
        val list1 = generator.generateInvalid(request(ArrayTargets::sizeRange), context, mockRegenerator)
        assertEquals(2, list1.size)

        val underflow = list1[0] as IntArray
        val overflow = list1[1] as IntArray

        // [Branch] min > 0 -> add(min - 1)
        assertEquals(1, underflow.size, "Underflow: 2 - 1 = 1")
        // [Branch] max != MAX -> add(max + 1)
        assertEquals(5, overflow.size, "Overflow: 4 + 1 = 5")

        // Case 2: Zero Min (min=0, max=2)
        // [Branch] min > 0 is FALSE -> Underflow skipped
        val list2 = generator.generateInvalid(request(ArrayTargets::minZero), context, mockRegenerator)
        assertEquals(1, list2.size)
        assertFalse(list2.any { (it as IntArray).size < 0 }, "Negative size impossible")
        assertEquals(3, (list2[0] as IntArray).size, "Overflow only")

        // Case 3: Max Value (min=1, max=Int.MAX)
        // [Branch] max != MAX is FALSE -> Overflow skipped
        val list3 = generator.generateInvalid(request(ArrayTargets::maxInt), context, mockRegenerator)
        assertEquals(1, list3.size)
        assertEquals(0, (list3[0] as IntArray).size, "Underflow only")

        // Case 4: Safety Limit on Invalid (max=2000)
        // Logic: invalidSize(2001) > LIMIT(1000) -> isValidSize returns false -> Skipped
        val list4 = generator.generateInvalid(request(ArrayTargets::largeLimit), context, mockRegenerator)
        assertTrue(list4.isEmpty(), "Unsafe invalid size should be skipped")
    }
}