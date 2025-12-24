package execution.domain.generator

import discovery.api.Size
import execution.exception.CollectionSizeLimitExceededException
import java.time.Clock
import java.util.Deque
import java.util.LinkedList
import java.util.Queue
import kotlin.random.Random
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CollectionTypeGeneratorTest {

    private val generator = CollectionTypeGenerator()

    private val context = GenerationContext(
        seededRandom = Random(42),
        clock = Clock.systemDefaultZone()
    )

    // =================================================================
    // 1. Test Targets
    // =================================================================
    @Suppress("UNUSED_PARAMETER")
    class CollectionTargets {
        // [Supported Types]
        fun listType(list: List<String>) {}
        fun setType(set: Set<Int>) {}
        fun mapType(map: Map<String, Int>) {}
        fun queueType(queue: Queue<String>) {}
        fun dequeType(deque: Deque<String>) {}
        fun collectionType(col: Collection<String>) {}

        // [Edge Cases: Raw Types / Star Projections]
        fun starList(list: List<*>) {}        // List<?>
        fun starMap(map: Map<*, *>) {}        // Map<?, ?>

        // [Invalid Context for Generator]
        fun <T> genericType(t: T) {}          // classifier is TypeParameter

        // [Size Logic]
        fun defaultSize(list: List<String>) {}
        fun rangeSize(@Size(min = 2, max = 4) list: List<String>) {}
        fun fixedSize(@Size(min = 3, max = 3) list: List<String>) {}
        fun minOnly(@Size(min = 10) list: List<String>) {}

        // [Safety Limits]
        fun largeLimit(@Size(min = 0, max = 2000) list: List<String>) {}
        fun explicitHuge(@Size(min = 1001) list: List<String>) {}
        fun ignoreLimit(@Size(min = 0, max = 2000, ignoreLimit = true) list: List<String>) {}
    }

    // =================================================================
    // 2. Helpers & Mock
    // =================================================================

    private fun request(func: KFunction<*>): GenerationRequest {
        val param = func.parameters.last()
        return GenerationRequest.from(param)
    }

    private var intCounter = 0
    private val mockRegenerator: (GenerationRequest, GenerationContext) -> Any? = { req, _ ->
        when (req.type.classifier) {
            String::class -> "Item-${intCounter++}"
            Int::class -> intCounter++
            else -> null // For star projection or unknown types
        }
    }

    // =================================================================
    // 3. Functional Tests
    // =================================================================

    @Test
    fun `Support Contract - verifies supported collection types`() {
        val targets = CollectionTargets()
        assertTrue(generator.supports(request(targets::listType)))
        assertTrue(generator.supports(request(targets::setType)))
        assertTrue(generator.supports(request(targets::mapType)))
        assertTrue(generator.supports(request(targets::queueType)))
        assertTrue(generator.supports(request(targets::dequeType)))

        // Fix: Generic function inference issue solved by finding function by name
        val genericFunc = CollectionTargets::class.functions.find { it.name == "genericType" }!!
        assertFalse(generator.supports(request(genericFunc)))
    }

    @Test
    fun `Generation Contract - creates correct instances`() {
        // 1. List
        val list = generator.generator(request(CollectionTargets::listType), context, mockRegenerator)
        assertIs<List<*>>(list)
        assertTrue(list.isNotEmpty())

        // 2. Set
        val set = generator.generator(request(CollectionTargets::setType), context, mockRegenerator)
        assertIs<Set<*>>(set)

        // 3. Map
        val map = generator.generator(request(CollectionTargets::mapType), context, mockRegenerator)
        assertIs<Map<*, *>>(map)
        assertTrue(map.isNotEmpty())

        // 4. Queue/Deque (LinkedList)
        val queue = generator.generator(request(CollectionTargets::queueType), context, mockRegenerator)
        assertIs<LinkedList<*>>(queue)
    }

    // =================================================================
    // 4. Exception & Edge Case Tests (High Priority)
    // =================================================================

    @Test
    fun `Exception Contract - throws CollectionSizeLimitExceededException when unsafe`() {
        val maliciousContext = GenerationContext(
            seededRandom = object : Random() {
                override fun nextBits(bitCount: Int) = 0
                override fun nextInt(from: Int, until: Int) = 1500 // Force unsafe size
            },
            clock = Clock.systemDefaultZone()
        )

        val req = request(CollectionTargets::largeLimit) // max=2000

        // [Check] Must throw exception to prevent OOM
        val ex = assertFailsWith<CollectionSizeLimitExceededException> {
            generator.generator(req, maliciousContext, mockRegenerator)
        }

        // [Fix] Check message content instead of properties (currentSize, limit)
        val msg = ex.message.orEmpty()
        assertTrue(msg.contains("1500"), "Exception message should contain actual size (1500)")
        assertTrue(msg.contains("1000"), "Exception message should contain limit (1000)")
    }

    @Test
    fun `Exception Contract - throws IllegalArgumentException for unresolved classifier`() {
        // [Fix] Resolve generic function by name to avoid "Cannot infer type parameter T" error
        val genericFunc = CollectionTargets::class.functions.find { it.name == "genericType" }!!
        val req = request(genericFunc)

        assertFailsWith<IllegalArgumentException> {
            generator.generator(req, context, mockRegenerator)
        }.also {
            // Verify message contains helpful info
            assertTrue(it.message.orEmpty().contains("Unresolved classifier"))
        }
    }

    @Test
    fun `Edge Case Contract - returns empty collection for Star Projections (Raw Types)`() {
        // 1. List<*>
        val starList = generator.generator(request(CollectionTargets::starList), context, mockRegenerator) as List<*>
        assertTrue(starList.isEmpty(), "List<*> should result in empty list due to missing type info")

        // 2. Map<*, *>
        val starMap = generator.generator(request(CollectionTargets::starMap), context, mockRegenerator) as Map<*, *>
        assertTrue(starMap.isEmpty(), "Map<*, *> should result in empty map")
    }

    // =================================================================
    // 5. Size Logic Tests
    // =================================================================

    @Test
    fun `Size Logic Contract - respects annotations`() {
        // Default
        val defaultList =
            generator.generator(request(CollectionTargets::defaultSize), context, mockRegenerator) as List<*>
        assertEquals(5, defaultList.size)

        // Range (2..4)
        val rangeReq = request(CollectionTargets::rangeSize)
        repeat(10) {
            val list = generator.generator(rangeReq, context, mockRegenerator) as List<*>
            assertTrue(list.size in 2..4)
        }

        // Fixed (3)
        val fixedList = generator.generator(request(CollectionTargets::fixedSize), context, mockRegenerator) as List<*>
        assertEquals(3, fixedList.size)
    }

    @Test
    fun `Safety Override Contract - allows huge size if explicit`() {
        // 1. Explicit Min > Limit
        val hugeMinList =
            generator.generator(request(CollectionTargets::explicitHuge), context, mockRegenerator) as List<*>
        assertEquals(1001, hugeMinList.size)

        // 2. Ignore Limit
        val maliciousContext = GenerationContext(
            seededRandom = object : Random() {
                override fun nextBits(bitCount: Int) = 0
                override fun nextInt(from: Int, until: Int) = 1500
            },
            clock = Clock.systemDefaultZone()
        )
        val ignoreList =
            generator.generator(request(CollectionTargets::ignoreLimit), maliciousContext, mockRegenerator) as List<*>
        assertEquals(1500, ignoreList.size)
    }

    // =================================================================
    // 6. Boundary & Invalid Tests
    // =================================================================

    @Test
    fun `Boundaries Contract - generates valid boundary values`() {
        val boundaries =
            generator.generateValidBoundaries(request(CollectionTargets::rangeSize), context, mockRegenerator)
        assertEquals(2, boundaries.size)
        assertEquals(2, (boundaries[0] as List<*>).size)
        assertEquals(4, (boundaries[1] as List<*>).size)
    }

    @Test
    fun `Invalid Contract - generates off-by-one errors`() {
        val invalids = generator.generateInvalid(request(CollectionTargets::rangeSize), context, mockRegenerator)
        assertEquals(2, invalids.size)
        assertEquals(1, (invalids[0] as List<*>).size) // 2-1
        assertEquals(5, (invalids[1] as List<*>).size) // 4+1
    }
}