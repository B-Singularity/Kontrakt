package execution.domain.generator

import discovery.api.Email
import discovery.api.NotBlank
import discovery.api.Pattern
import discovery.api.StringLength
import discovery.api.Url
import discovery.api.Uuid
import java.util.UUID
import kotlin.random.Random
import kotlin.reflect.full.starProjectedType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StringTypeGeneratorTest {

    private lateinit var generator: StringTypeGenerator
    private lateinit var context: GenerationContext
    private val fixedSeed = 12345L

    @BeforeTest
    fun setup() {
        generator = StringTypeGenerator()
        context = GenerationContext(
            seededRandom = Random(fixedSeed),
            clock = java.time.Clock.systemUTC()
        )
    }

    @Test
    fun `supports returns true for String`() {
        assertTrue(generator.supports(createRequest(String::class)))
    }

    @Test
    fun `supports returns false for Int`() {
        assertFalse(generator.supports(createRequest(Int::class)))
    }

    @Test
    fun `generate regex patterns`() {
        val reqAlpha = createRequest(String::class, listOf(Pattern(regexp = "[a-zA-Z]+")))
        val resAlpha = generator.generate(reqAlpha, context) as String
        assertTrue(resAlpha.isNotEmpty())
        assertTrue(resAlpha.all { it.isLetterOrDigit() }, "Should allow alphanumeric for simple string generation")

        // \w+
        val reqWord = createRequest(String::class, listOf(Pattern(regexp = "\\w+")))
        val resWord = generator.generate(reqWord, context) as String
        assertTrue(resWord.length >= 5)
        assertTrue(resWord.all { it.isLetterOrDigit() })

        // Digits
        val reqDigit = createRequest(String::class, listOf(Pattern(regexp = "\\d+")))
        assertTrue((generator.generate(reqDigit, context) as String).all { it.isDigit() })

        // Ranges (Case sensitive)
        val reqUpper = createRequest(String::class, listOf(Pattern(regexp = "^[A-Z]+$")))
        assertTrue((generator.generate(reqUpper, context) as String).all { it.isUpperCase() })

        val reqLower = createRequest(String::class, listOf(Pattern(regexp = "^[a-z]+$")))
        assertTrue((generator.generate(reqLower, context) as String).all { it.isLowerCase() })
    }

    @Test
    fun `generate boundaries max huge`() {
        val req = createRequest(String::class, listOf(StringLength(min = 2, max = Int.MAX_VALUE)))
        val res = generator.generateValidBoundaries(req, context)
        assertEquals(2, res.size, "Should contain both min and calculated max boundary")
        assertTrue(res.contains("aa"))
    }

    @Test
    fun `generate url truncation logic`() {
        // 1. Guard Clause: remaining < 5
        // http:// (7). Max 8. Remaining 1. -> "http://a.co"
        val reqGuard = createRequest(String::class, listOf(Url(protocol = arrayOf("http")), StringLength(max = 8)))
        assertEquals("http://a.co", generator.generate(reqGuard, context))

        // 2. Truncation Logic: remaining < 6 (strict truncation)
        // Scheme "http://" (7). Host "verylong.com" (12). Max 12.
        // Remaining = 5
        // host.take(5) -> "veryl"
        // Result: "http://veryl"
        val reqTrunc = createRequest(
            String::class,
            listOf(Url(protocol = arrayOf("http"), hostAllow = arrayOf("verylong.com")), StringLength(max = 12))
        )
        assertEquals("http://veryl", generator.generate(reqTrunc, context))
    }

    @Test
    fun `generate url path and query discarding branches`() {
        // 1. Path Discarding
        // Scheme+Host=14. Max=17. Space=3. Path min=4.
        // 14+3 < 17 (False). Path logic skipped.
        val reqPath = createRequest(
            String::class,
            listOf(Url(protocol = arrayOf("http"), hostAllow = arrayOf("abc.com")), StringLength(max = 17))
        )
        repeat(10) {
            val res = generator.generate(reqPath, context) as String
            assertEquals(2, res.count { it == '/' }, "Path should be discarded/skipped")
        }

        // 2. Query Discarding
        // Scheme+Host=14. Max=20.
        // Query check: 14+5 < 20 (True)
        // Query min=8. 14+8 <= 20 (False). Discard.
        val reqQuery = createRequest(
            String::class,
            listOf(Url(protocol = arrayOf("http"), hostAllow = arrayOf("abc.com")), StringLength(max = 20))
        )
        repeat(20) {
            val res = generator.generate(reqQuery, context) as String
            assertFalse(res.contains("?key="), "Query should be discarded")
        }
    }

    @Test
    fun `generate invalid logic string length`() {
        val req = createRequest(String::class, listOf(StringLength(min = 3, max = 5)))
        val res = generator.generateInvalid(req, context)
        assertTrue(res.contains("xx"))
        assertTrue(res.contains("xxxxxx"))
    }

    @Test
    fun `generate invalid logic not blank`() {
        val req = createRequest(String::class, listOf(NotBlank()))
        val res = generator.generateInvalid(req, context)
        assertTrue(res.contains(""))
        assertTrue(res.contains("   "))
    }

    @Test
    fun `generate invalid logic email`() {
        val req = createRequest(String::class, listOf(Email()))
        val res = generator.generateInvalid(req, context)
        assertTrue(res.contains("not-an-email"))
        assertTrue(res.contains("@domain.com"))
    }

    // =================================================================
    // Standard Tests
    // =================================================================

    @Test
    fun `generate string with explicit length`() {
        val request = createRequest(String::class, listOf(StringLength(min = 5, max = 5)))
        val result = generator.generate(request, context) as String
        assertEquals(5, result.length)
    }

    @Test
    fun `generate string with max value length`() {
        val request = createRequest(String::class, listOf(StringLength(min = 10)))
        val result = generator.generate(request, context) as String
        assertTrue(result.length in 10..200)
    }

    @Test
    fun `generate string with NotBlank only`() {
        val request = createRequest(String::class, listOf(NotBlank()))
        val result = generator.generate(request, context) as String
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `generate email logic branches`() {
        val reqAllow = createRequest(String::class, listOf(Email(allow = arrayOf("co.uk"))))
        assertTrue((generator.generate(reqAllow, context) as String).endsWith("@co.uk"))

        val reqShort = createRequest(String::class, listOf(Email(), StringLength(max = 5)))
        val resShort = generator.generate(reqShort, context) as String
        assertTrue(resShort.startsWith("a@"))
    }

    @Test
    fun `generate url protocol branches`() {
        val reqProto = createRequest(String::class, listOf(Url(protocol = arrayOf("ftp"))))
        assertTrue((generator.generate(reqProto, context) as String).startsWith("ftp://"))
    }

    @Test
    fun `generate url blocked host fallback`() {
        val reqBlock = createRequest(String::class, listOf(Url(hostBlock = arrayOf("com"))))
        var hitFallback = false
        repeat(50) {
            if ((generator.generate(reqBlock, context) as String).contains("example.com")) {
                hitFallback = true
            }
        }
        assertTrue(hitFallback)
    }

    @Test
    fun `generate uuid`() {
        val req = createRequest(String::class, listOf(Uuid()))
        val res = generator.generate(req, context) as String
        assertNotNull(UUID.fromString(res))
    }

    @Test
    fun `generate boundaries basic`() {
        val req = createRequest(String::class, listOf(StringLength(min = 2, max = 5)))
        val res = generator.generateValidBoundaries(req, context)
        assertTrue(res.contains("aa"))
        assertTrue(res.contains("aaaaa"))
    }

    @Test
    fun `generate boundaries NotBlank logic`() {
        val req1 = createRequest(String::class, listOf(NotBlank()))
        val res1 = generator.generateValidBoundaries(req1, context)
        assertTrue(res1.contains("a"))

        val req2 = createRequest(String::class, listOf(NotBlank(), StringLength(min = 1, max = 5)))
        val res2 = generator.generateValidBoundaries(req2, context)
        assertEquals(2, res2.size)
        assertTrue(res2.contains("a"))
    }

    @Test
    fun `generate boundaries specialized`() {
        val req = createRequest(String::class, listOf(Email(), Uuid()))
        val res = generator.generateValidBoundaries(req, context)
        assertTrue(res.contains("test@example.com"))
        assertTrue(res.any { (it as String).length == 36 })
    }

    @Test
    fun `calculate effective length inverted`() {
        val req = createRequest(String::class, listOf(StringLength(min = 10, max = 5)))
        val res = generator.generate(req, context) as String
        assertEquals(10, res.length)
    }

    private fun createRequest(
        kClass: kotlin.reflect.KClass<*>,
        annotations: List<Annotation> = emptyList()
    ): GenerationRequest {
        return GenerationRequest.from(
            type = kClass.starProjectedType,
            annotations = annotations,
            name = "testParam"
        )
    }
}